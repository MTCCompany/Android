package com.example.eventcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class CalendarWidgetProvider extends AppWidgetProvider {
	// Widget���X�V���邽�߂�ACTION�̒�`
	public static final Object ACTION_UPDATE_WIDGET = "com.example.eventcalendar.update_widget";

	public void onEnabled(Context context){
		// ��ʂɕ\�����ꂽ�Ƃ�
		super.onEnabled(context);
		updateEvents(context);
	}

	public void onUdate(Context context, AppWidgetManager manager, int[] ids){
		// �X�V�C�x���g�������Ƃ�
		super.onUpdate(context, manager, ids);
		updateEvents(context);
	}

	public void onReceive(Context context,Intent intent){
		// �C���e���g����M�����Ƃ�
		super.onReceive(context,intent);
		String action = intent.getAction();
		if(action!=null && action.equals(ACTION_UPDATE_WIDGET)){
			updateEvents(context);
		}
	}

	/**
	 * �C�x���g���̍X�V
	 * @param Context
	 */
	public void updateEvents(Context context){
		// WidgetService�@���N������
		Intent intent = new Intent(context,WidgetService.class);
		context.startService(intent);
	}
}
