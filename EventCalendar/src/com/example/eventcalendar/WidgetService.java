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
		// Widget�̍X�V���s��
		updateWidget(getApplicationContext());
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	public void updateWidget(Context context){
		// �J�����_�[�̃f�[�^�x�[�X���璼�߂̗\������o���B
		GregorianCalendar cal = new GregorianCalendar();
		String nowDateStr = DateStrCls.toDBDateString(cal);
		ContentResolver resolver = context.getContentResolver();
		String[] projection = {EventInfo.START_TIME,EventInfo.TITLE};
		String selection = EventInfo.DELETED + "= 0 and " + EventInfo.START_TIME + "> ?";
		String[] selectionArgs = {nowDateStr};
		String sortOrder = EventInfo.START_TIME;
		Cursor c = resolver.query(EventCalendarActivity.RESOLVER_URI, projection, selection, selectionArgs, sortOrder);
		// ���߂̗\��̓��e���當������쐬���AView�ɕ\������
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
		// Widget�̕`���RemoteView���g���ĕ`�悷��̂�
		// �ʏ�̕`��Ƃ͏����قȂ�
		RemoteViews rViews = new RemoteViews(getPackageName(),R.layout.calendarwidget);
		// RemoveView��setTextViewText��ǂ�ŕ������`�悷��
		rViews.setTextViewText(R.id.widgetText, events.toString());

		setOnClick(context,rViews);
		ComponentName thisWidget = new ComponentName(this, CalendarWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, rViews);
	}
	/**
	 * �E�B�W�F�b�g���N���b�N������C�x���g�J�����_�[���N������l�ɐݒ肷��
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
