package jp.gr.java_conf.BusinessCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class AlarmReceiver extends BroadcastReceiver {
	// アラームを実行するリクエストコード 
	private static final int DO_ALARM = 0;
	// アラームを停止するサービスのアクション
	public static final String ACTION_STOP_ALARM = "jp.gr.java_conf.BusinessCalendar.service.stop";
	// アラームを開始するサービスのアクション
	public static final String ACTION_START_ALARM = "jp.gr.java_conf.BusinessCalendar.service.start";
	// NotificationManagerで使用するID（適当な値）
	public static final int NOTIFICATION_ID = 0x10;

	// 時間がくるとこのメソッドがコールされる
	public void onReceive(Context context, Intent intent) {
		long alarm  = 0;
		String action = intent.getAction();
		if(action != null){
			if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
				// Action がBOOT_COMPLETEDだった場合
				alarm = Calendar.getInstance().getTimeInMillis();
				// 再起動が実施されたので、以前登録したAlarmが消えているので、DBを更新しAlarmを再設定する
				updateDbAlarm(context,alarm);
				updateAlarm(context);
			}else if(action.equals(ACTION_STOP_ALARM)){
				// ActionがSTOP_ALARMだった場合
				// ノーティフィケーションを消す
				NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(NOTIFICATION_ID);
				// 再生を停止
				Intent serviceIntent = new Intent(context,AlarmService.class);
				context.stopService(serviceIntent);
			}else if(action.equals(ACTION_START_ALARM)){
				// ActionがSTART_ALARMだった場合
				// Alarm時刻なので、Alarmを開始
				alarm = intent.getLongExtra(EventInfo.ALARM,0);
				// ノーティフィケーションを表示
				showNotification(context,alarm);
				// データベースを更新し、次のアラームをセット
				updateDbAlarm(context,alarm);
				updateAlarm(context);
			}
		}
	}

	/**
	 * 通知バーにノーティフィケーションを表示する
	 *
	 * @param Context コンテキスト
	 * @param long alarm アラーム時刻
	 */
	private void showNotification(Context context, long alarm) {
		if(alarm == 0){
			return;
		}
		// データベースから設定していたAlarm時刻からその時刻にアラームをセットしていたイベント取得
		ContentResolver contentResolver = context.getContentResolver();
		String selection = EventInfo.DELETED + " = 0  and " + EventInfo.ALARM + " = " + alarm;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
		while(c.moveToNext()){
			// イベントは複数存在する可能性が有るので、複数回処理
			String title = context.getString(R.string.notification);
			String text = c.getString(c.getColumnIndex(EventInfo.TITLE));
			String message = title +":"+ text ;
			// イベントのタイトルを通知に表示
			Notification notification = new Notification(R.drawable.icon,message,alarm);
			// 消去されるとAlarmを停止できないので、一括消去できないようにする
			notification.flags =Notification.FLAG_NO_CLEAR;
			// ノーティフィケーションのアイテムをタッチされたときは、ACTION_STOP_ALARMを発行する様に設定
			Intent intent = new Intent(context,AlarmReceiver.class);
			intent.setAction(ACTION_STOP_ALARM);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context,title,text,pendingIntent);
			NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(NOTIFICATION_ID,notification);
			// アラーム音を鳴らすサービスを起動
			Intent serviceIntent = new Intent(context,AlarmService.class);
			context.startService(serviceIntent);
		}
		c.close();
	}

	/**
	 * データベースを検索し、最新のアラーム情報を設定する
	 *
	 * @param Context
	 * @param long alarm 今注目しているAlarm時刻
	 */
	public void updateDbAlarm(Context context,long alarm){
		if(alarm == 0){
			return;
		}
		ContentResolver contentResolver = context.getContentResolver();
		// Alarmを設定しているデータの中で、今注目している時刻以前の値を持っているものを検索
		String selection = EventInfo.DELETED + " = 0  and " 
				+ EventInfo.ALARM + " <> 0 and "
				+ EventInfo.ALARM + " <= " + alarm;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
		ArrayList<EventInfo> events = new ArrayList<EventInfo>();
		// Alarm時刻が過去になってしまったEventInfoをリストに取得
		while(c.moveToNext()){
			EventInfo eventInfo  = new EventInfo(contentResolver);
			eventInfo.setValues(c);
			events.add(eventInfo);
		}
		c.close();
		// EventInfoのAlarm情報を更新し、データベースを更新
		for(EventInfo eventInfo : events){
			ContentValues cv = eventInfo.getValues();
			selection = EventInfo.ID + " = " + eventInfo.getId();
			contentResolver.update(EventCalendarActivity.RESOLVER_URI,cv,selection,null);
		}
	}

	/**
	 * Alarm設定を更新する
	 *
	 * @param Context
	 */
	public static void updateAlarm(Context context){
		// 直近のAlarm時刻を持っているEventを検索
		ContentResolver contentResolver = context.getContentResolver();
		long now  = Calendar.getInstance().getTimeInMillis();
		String[] projection = {EventInfo.ALARM,EventInfo.TITLE};
		String selection = EventInfo.DELETED + " = 0 and "+EventInfo.ALARM + " > " + now;
		String sortOrder = EventInfo.ALARM;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, projection, selection, null, sortOrder);
		// セットするのは1件でいいので　whileでは回さない
		if(c.moveToNext()){
			AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
			long alarm = c.getLong(c.getColumnIndex(EventInfo.ALARM));
			GregorianCalendar cal = new GregorianCalendar();
			// Alarmをならす時刻をセット
			cal.setTimeInMillis(alarm);
			Intent intent = new Intent(context,AlarmReceiver.class);
			intent.putExtra(EventInfo.ALARM,alarm);
			intent.setAction(ACTION_START_ALARM);
			PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, DO_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// すでにAlarmイベントが登録されていた場合うまく動作しないので、すべて削除
			alarmManager.cancel(alarmReceiver);
			// 新しいAlarmイベントを登録
			alarmManager.set(AlarmManager.RTC_WAKEUP,alarm,alarmReceiver);
		}
		c.close();
	}
}
