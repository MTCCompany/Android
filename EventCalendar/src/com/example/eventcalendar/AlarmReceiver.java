package com.example.eventcalendar;

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
	// �A���[�������s���郊�N�G�X�g�R�[�h 
	private static final int DO_ALARM = 0;
	// �A���[�����~����T�[�r�X�̃A�N�V����
	public static final String ACTION_STOP_ALARM = "com.example.eventcalendar.service.stop";
	// �A���[�����J�n����T�[�r�X�̃A�N�V����
	public static final String ACTION_START_ALARM = "com.example.eventcalendar.service.start";
	// NotificationManager�Ŏg�p����ID�i�K���Ȓl�j
	public static final int NOTIFICATION_ID = 0x10;

	// ���Ԃ�����Ƃ��̃��\�b�h���R�[�������
	public void onReceive(Context context, Intent intent) {
		long alarm  = 0;
		String action = intent.getAction();
		if(action != null){
			if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
				// Action ��BOOT_COMPLETED�������ꍇ
				alarm = Calendar.getInstance().getTimeInMillis();
				// �ċN�������{���ꂽ�̂ŁA�ȑO�o�^����Alarm�������Ă���̂ŁADB���X�V��Alarm���Đݒ肷��
				updateDbAlarm(context,alarm);
				updateAlarm(context);
			}else if(action.equals(ACTION_STOP_ALARM)){
				// Action��STOP_ALARM�������ꍇ
				// �m�[�e�B�t�B�P�[�V����������
				NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(NOTIFICATION_ID);
				// �Đ����~
				Intent serviceIntent = new Intent(context,AlarmService.class);
				context.stopService(serviceIntent);
			}else if(action.equals(ACTION_START_ALARM)){
				// Action��START_ALARM�������ꍇ
				// Alarm�����Ȃ̂ŁAAlarm���J�n
				alarm = intent.getLongExtra(EventInfo.ALARM,0);
				// �m�[�e�B�t�B�P�[�V������\��
				showNotification(context,alarm);
				// �f�[�^�x�[�X���X�V���A���̃A���[�����Z�b�g
				updateDbAlarm(context,alarm);
				updateAlarm(context);
			}
		}
	}

	/**
	 * �ʒm�o�[�Ƀm�[�e�B�t�B�P�[�V������\������
	 *
	 * @param Context �R���e�L�X�g
	 * @param long alarm �A���[������
	 */
	private void showNotification(Context context, long alarm) {
		if(alarm == 0){
			return;
		}
		// �f�[�^�x�[�X����ݒ肵�Ă���Alarm�������炻�̎����ɃA���[�����Z�b�g���Ă����C�x���g�擾
		ContentResolver contentResolver = context.getContentResolver();
		String selection = EventInfo.DELETED + " = 0  and " + EventInfo.ALARM + " = " + alarm;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
		while(c.moveToNext()){
			// �C�x���g�͕������݂���\�����L��̂ŁA�����񏈗�
			String title = context.getString(R.string.notification);
			String text = c.getString(c.getColumnIndex(EventInfo.TITLE));
			String message = title +":"+ text ;
			// �C�x���g�̃^�C�g����ʒm�ɕ\��
			Notification notification = new Notification(R.drawable.icon,message,alarm);
			// ����������Alarm���~�ł��Ȃ��̂ŁA�ꊇ�����ł��Ȃ��悤�ɂ���
			notification.flags =Notification.FLAG_NO_CLEAR;
			// �m�[�e�B�t�B�P�[�V�����̃A�C�e�����^�b�`���ꂽ�Ƃ��́AACTION_STOP_ALARM�𔭍s����l�ɐݒ�
			Intent intent = new Intent(context,AlarmReceiver.class);
			intent.setAction(ACTION_STOP_ALARM);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context,title,text,pendingIntent);
			NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(NOTIFICATION_ID,notification);
			// �A���[������炷�T�[�r�X���N��
			Intent serviceIntent = new Intent(context,AlarmService.class);
			context.startService(serviceIntent);
		}
		c.close();
	}

	/**
	 * �f�[�^�x�[�X���������A�ŐV�̃A���[������ݒ肷��
	 *
	 * @param Context
	 * @param long alarm �����ڂ��Ă���Alarm����
	 */
	public void updateDbAlarm(Context context,long alarm){
		if(alarm == 0){
			return;
		}
		ContentResolver contentResolver = context.getContentResolver();
		// Alarm��ݒ肵�Ă���f�[�^�̒��ŁA�����ڂ��Ă��鎞���ȑO�̒l�������Ă�����̂�����
		String selection = EventInfo.DELETED + " = 0  and " 
				+ EventInfo.ALARM + " <> 0 and "
				+ EventInfo.ALARM + " <= " + alarm;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
		ArrayList<EventInfo> events = new ArrayList<EventInfo>();
		// Alarm�������ߋ��ɂȂ��Ă��܂���EventInfo�����X�g�Ɏ擾
		while(c.moveToNext()){
			EventInfo eventInfo  = new EventInfo(contentResolver);
			eventInfo.setValues(c);
			events.add(eventInfo);
		}
		c.close();
		// EventInfo��Alarm�����X�V���A�f�[�^�x�[�X���X�V
		for(EventInfo eventInfo : events){
			ContentValues cv = eventInfo.getValues();
			selection = EventInfo.ID + " = " + eventInfo.getId();
			contentResolver.update(EventCalendarActivity.RESOLVER_URI,cv,selection,null);
		}
	}

	/**
	 * Alarm�ݒ���X�V����
	 *
	 * @param Context
	 */
	public static void updateAlarm(Context context){
		// ���߂�Alarm�����������Ă���Event������
		ContentResolver contentResolver = context.getContentResolver();
		long now  = Calendar.getInstance().getTimeInMillis();
		String[] projection = {EventInfo.ALARM,EventInfo.TITLE};
		String selection = EventInfo.DELETED + " = 0 and "+EventInfo.ALARM + " > " + now;
		String sortOrder = EventInfo.ALARM;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, projection, selection, null, sortOrder);
		// �Z�b�g����̂�1���ł����̂Ł@while�ł͉񂳂Ȃ�
		if(c.moveToNext()){
			AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
			long alarm = c.getLong(c.getColumnIndex(EventInfo.ALARM));
			GregorianCalendar cal = new GregorianCalendar();
			// Alarm���Ȃ炷�������Z�b�g
			cal.setTimeInMillis(alarm);
			Intent intent = new Intent(context,AlarmReceiver.class);
			intent.putExtra(EventInfo.ALARM,alarm);
			intent.setAction(ACTION_START_ALARM);
			PendingIntent alarmReceiver = PendingIntent.getBroadcast(context, DO_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// ���ł�Alarm�C�x���g���o�^����Ă����ꍇ���܂����삵�Ȃ��̂ŁA���ׂč폜
			alarmManager.cancel(alarmReceiver);
			// �V����Alarm�C�x���g��o�^
			alarmManager.set(AlarmManager.RTC_WAKEUP,alarm,alarmReceiver);
		}
		c.close();
	}
}
