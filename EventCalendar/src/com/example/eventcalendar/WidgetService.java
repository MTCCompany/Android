package com.example.eventcalendar;

import java.util.Date;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.widget.RemoteViews;

public class WidgetService extends Service {
	public static final String ACTION_UPDATE_WIDGET = "com.example.eventcalendar.update_widget";
	@Override
	public void onStart(Intent intent, int request){
		super.onStart(intent,request);
		// Widgetの更新を行う
		updateWidget(getApplicationContext());
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	public void updateWidget(Context context){
		// カレンダーのデータベースから直近の予定を取り出す。
		GregorianCalendar cal = new GregorianCalendar();
		String nowDateStr = DateStrCls.toDBDateString(cal);
		ContentResolver resolver = context.getContentResolver();
		String[] projection = {EventInfo.START_TIME,EventInfo.TITLE};
		String selection = EventInfo.DELETED + "= 0 and " + EventInfo.START_TIME + "> ?";
		String[] selectionArgs = {nowDateStr};
		String sortOrder = EventInfo.START_TIME;
		Cursor c = resolver.query(EventCalendarActivity.RESOLVER_URI, projection, selection, selectionArgs, sortOrder);
		// 直近の予定の内容から文字列を作成し、Viewに表示する
		StringBuilder events = new StringBuilder();
		if(c.moveToNext()){
			String t = c.getString(c.getColumnIndex(EventInfo.START_TIME));
			Date startTime = DateStrCls.toCalendar(t).getTime();
			events.append(DateStrCls.dateFormat.format(startTime));
			events.append(" ");
			events.append(DateStrCls.timeFormat.format(startTime));
			events.append("\n");
			events.append(c.getString(c.getColumnIndex(EventInfo.TITLE)));
		}else{
			events.append(context.getString(R.string.noevents));
		}
		c.close();
		// Widgetの描画はRemoteViewを使って描画するので
		// 通常の描画とは少し異なる
		RemoteViews rViews = new RemoteViews(getPackageName(),R.layout.calendarwidget);
		// RemoveViewのsetTextViewTextを読んで文字列を描画する
		rViews.setTextViewText(R.id.widgetText, events.toString());

		setOnClick(context,rViews);
		ComponentName thisWidget = new ComponentName(this, CalendarWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, rViews);
	}
	/**
	 * ウィジェットをクリックしたらイベントカレンダーを起動する様に設定する
	 *
	 * @param Context
	 * @param RemoteView
	 */
	public void setOnClick(Context context,RemoteViews rViews){
		Intent intent = new Intent(context,EventCalendarActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		rViews.setOnClickPendingIntent(R.id.widgetText, pendingIntent);
		rViews.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
	}
}
