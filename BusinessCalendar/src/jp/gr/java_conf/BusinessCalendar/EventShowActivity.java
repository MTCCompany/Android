package jp.gr.java_conf.BusinessCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * EventEditorActivity
 * イベントの情報を修正するための画面
 * ボタンの処理をするために「onCliciListener」をインプリメントする
 */
public class EventShowActivity extends Activity implements OnClickListener {
	// Viewのインスタンス
	private TextView mTitleTextView = null;
	private TextView mWhereTextView = null;
	private TextView mContentTextView  = null;
	private TextView mStartDateTextView = null;
	private TextView mStartTimeTextView = null;
	private TextView mEndDateTextView = null;
	private TextView mEndTimeTextView = null;
	private Button mEditButton = null;
	private Button mDeleteButton = null;
	
	// IntentでもらったデータベースID
	private long mId = 0;
	// 日付の文字列
	private String mDateString = null;  

	/**
	 * onCreate
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// リソースからViewを作成する
		setContentView(R.layout.eventshow);
		
		// コントローラを取得する
		mTitleTextView = (TextView)findViewById(R.id.title);
		mWhereTextView = (TextView)findViewById(R.id.where);
		mContentTextView = (TextView)findViewById(R.id.content);
		mStartDateTextView = (TextView)findViewById(R.id.startDate);
		mStartTimeTextView = (TextView)findViewById(R.id.startTime);
		mEndDateTextView = (TextView)findViewById(R.id.endDate);
		mEndTimeTextView = (TextView)findViewById(R.id.endTime);
		mEditButton = (Button)findViewById(R.id.edit);
		mDeleteButton = (Button)findViewById(R.id.delete);
		
		// ボタンイベント処理設定
		mEditButton.setOnClickListener(this);
		mDeleteButton.setOnClickListener(this);
		
		// インテントのExtraからデータのIDと日付を取得する
		Intent intent = getIntent();
		mId = intent.getLongExtra(EventInfo.ID, 0);
		mDateString = intent.getStringExtra("date");
		
		// データベースからデータを取得し、データの内容を各テキストに設定する
		ContentResolver contentResolver = getContentResolver();
		String selection = EventInfo.ID+" = "+mId;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
		if(c.moveToNext()){
			mTitleTextView.setText(c.getString(c.getColumnIndex(EventInfo.TITLE)));
			mWhereTextView.setText(c.getString(c.getColumnIndex(EventInfo.WHERE)));
			mContentTextView.setText(c.getString(c.getColumnIndex(EventInfo.CONTENT)));

			String startTime = c.getString(c.getColumnIndex(EventInfo.START_TIME));
			Calendar startCal = DateStrCls.toCalendar(startTime);
			mStartDateTextView.setText(DateStrCls.dateFormat.format(startCal.getTime()));
			mStartTimeTextView.setText(DateStrCls.timeFormat.format(startCal.getTime()));
			String endTime = c.getString(c.getColumnIndex(EventInfo.END_TIME));
			Calendar endCal = DateStrCls.toCalendar(endTime);
			mEndDateTextView.setText(DateStrCls.dateFormat.format(endCal.getTime()));
			mEndTimeTextView.setText(DateStrCls.timeFormat.format(endCal.getTime()));
			if(startCal.get(Calendar.HOUR_OF_DAY) == 0 && 
					startCal.get(Calendar.MINUTE) == 0){
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				if(startCal.equals(endCal)){
					// 開始時刻が00:00で終了が翌日の00:00の場合
					// 終日の予定と判断する
					mStartTimeTextView.setVisibility(View.INVISIBLE);
					mEndDateTextView.setVisibility(View.INVISIBLE);
					mEndTimeTextView.setVisibility(View.INVISIBLE);
				}
			}
			// 値が設定されていない場合、以下の文字を設定する
			if(mWhereTextView.getText() == null || mWhereTextView.getText().equals(""))
				mWhereTextView.setText(R.string.noWhere);
			if(mContentTextView.getText() == null || mContentTextView.getText().equals(""))
				mContentTextView.setText(R.string.noContent);
		}
		c.close();
		
		
		
	}

	/**
	 * onClick
	 *  ボタンのどれかがタップされたときの処理
	 */
	public void onClick(View v) {
		
	}

}
