package com.example.eventcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class CalendarWidgetProvider extends AppWidgetProvider {
	// Widgetを更新するためのACTIONの定義
	public static final Object ACTION_UPDATE_WIDGET = "com.example.eventcalendar.update_widget";

	public void onEnabled(Context context){
		// 画面に表示されたとき
		super.onEnabled(context);
		updateEvents(context);
	}

	public void onUdate(Context context, AppWidgetManager manager, int[] ids){
		// 更新イベントが来たとき
		super.onUpdate(context, manager, ids);
		updateEvents(context);
	}

	public void onReceive(Context context,Intent intent){
		// インテントを受信したとき
		super.onReceive(context,intent);
		String action = intent.getAction();
		if(action!=null && action.equals(ACTION_UPDATE_WIDGET)){
			updateEvents(context);
		}
	}

	/**
	 * イベント情報の更新
	 * @param Context
	 */
	public void updateEvents(Context context){
		// WidgetService　を起動する
		Intent intent = new Intent(context,WidgetService.class);
		context.startService(intent);
	}
}
