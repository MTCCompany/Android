package com.example.eventcalendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * EventEditorActivity
 * イベントの情報を修正するための画面
 * ボタンの処理をするために「onCliciListener」をインプリメントする
 */
public class EventEditorActivity extends Activity implements OnClickListener {
	// Viewのインスタンス
	private EditText mTitleEditText = null;
	private EditText mWhereEditText = null;
	private EditText mContentEditText = null;
	private TextView mStartDateTextView = null;
	private TextView mStartTimeTextView = null;
	private TextView mEndDateTextView = null;
	private TextView mEndTimeTextView = null;
	private Button mDiscardButton = null;
	private Button mSaveButton = null;
	private CheckBox mAllDayCheckBox = null;
	// IntentでもらったデータベースID
	private long mId = 0;
	// 日付の文字列
	private String mDateString = null;  

	/**
	 * onCreate
	 * IDが０なら新規、１以上ならデータベースから情報を取得し格フィールドにセットする
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// リソースからViewを作成する
		setContentView(R.layout.eventeditor);
		// TextEditなどのビューを取得する
		mTitleEditText = (EditText)findViewById(R.id.title);
		mWhereEditText = (EditText)findViewById(R.id.where);
		mContentEditText = (EditText)findViewById(R.id.content);
		mStartDateTextView = (TextView)findViewById(R.id.startDate);
		mStartTimeTextView = (TextView)findViewById(R.id.startTime);
		mEndDateTextView = (TextView)findViewById(R.id.endDate);
		mEndTimeTextView = (TextView)findViewById(R.id.endTime);
		mDiscardButton = (Button)findViewById(R.id.discard);
		mSaveButton = (Button)findViewById(R.id.save);
		mAllDayCheckBox = (CheckBox)findViewById(R.id.allDay);

		// 「OnClickListener」に「EventEditorActivity」をセットする
		mDiscardButton.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		Intent intent = getIntent();
		// インテントのExtraからデータのIDを取得する
		mId  = intent.getLongExtra(EventInfo.ID,0);
		// インテントのExtraから日付を取得する
		mDateString = intent.getStringExtra("date");
		if(mId==0){
			// タップした日付で今の時刻からの予定としてデータを作成する
			//　引数でもらった日付をカレンダーに変換
			Calendar targetCal = CommonCls.toCalendar(mDateString);
			// 今の時刻を取得
			Calendar nowCal = new GregorianCalendar();
			// 開始日はタップした日付
			mStartDateTextView.setText(CommonCls.dateFormat.format(targetCal.getTime()));
			// 開始時刻は今乃時刻
			mStartTimeTextView.setText(CommonCls.timeFormat.format(nowCal.getTime()));
			// 時刻を１時間加算
			nowCal.add(Calendar.HOUR, 1);
			// 終了日は開始日と同じ
			mEndDateTextView.setText(CommonCls.dateFormat.format(targetCal.getTime()));
			//　終了時刻は開始から１時間後
			mEndTimeTextView.setText(CommonCls.timeFormat.format(nowCal.getTime()));
		}else{
			// データベースからデータを取得し、データの内容を編集エリアに設定する
			ContentResolver contentResolver = getContentResolver();
			String selection = EventInfo.ID+" = "+mId;
			Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, null, null);
			if(c.moveToNext()){
				mTitleEditText.setText(c.getString(c.getColumnIndex(EventInfo.TITLE)));
				mWhereEditText.setText(c.getString(c.getColumnIndex(EventInfo.WHERE)));
				mContentEditText.setText(c.getString(c.getColumnIndex(EventInfo.CONTENT)));

				String startTime = c.getString(c.getColumnIndex(EventInfo.START_TIME));
				Calendar startCal = CommonCls.toCalendar(startTime);
				mStartDateTextView.setText(CommonCls.dateFormat.format(startCal.getTime()));
				mStartTimeTextView.setText(CommonCls.timeFormat.format(startCal.getTime()));
				String endTime = c.getString(c.getColumnIndex(EventInfo.END_TIME));
				Calendar endCal = CommonCls.toCalendar(endTime);
				mEndDateTextView.setText(CommonCls.dateFormat.format(endCal.getTime()));
				mEndTimeTextView.setText(CommonCls.timeFormat.format(endCal.getTime()));
				if(startCal.get(Calendar.HOUR_OF_DAY) == 0 && 
						startCal.get(Calendar.MINUTE) == 0){
					startCal.add(Calendar.DAY_OF_MONTH, 1);
					if(startCal.equals(endCal)){
						// 開始時刻が00:00で終了が翌日の00:00の場合
						// 終日の予定と判断する
						mStartTimeTextView.setVisibility(View.INVISIBLE);
						mEndDateTextView.setVisibility(View.INVISIBLE);
						mEndTimeTextView.setVisibility(View.INVISIBLE);
						mAllDayCheckBox.setChecked(true);
					}
				}
			}
			c.close();
		}
		// 日時の編集用のリスナーを日時のテキストにセット
		mStartDateTextView.setOnClickListener(new DateOnClickListener(this));
		mEndDateTextView.setOnClickListener(new DateOnClickListener(this));
		mStartTimeTextView.setOnClickListener(new TimeOnClickListener(this));
		mEndTimeTextView.setOnClickListener(new TimeOnClickListener(this));
		// AllDayチェックボックスにリスナーをセット
		mAllDayCheckBox.setOnClickListener(new AllDayOnClickListener());
	}

	/**
	 * onClick
	 *  ボタンのどれかがタップされたときの処理
	 */
	public void onClick(View v) {
		if(v == mDiscardButton){
			// Discardボタンがタップされたら何もせずアクティビティを終了する
			Log.d("CALENDAR","Discard");
			finish();
		}else if(v == mSaveButton){
			// Saveボタンがタップされたら編集中のデータをデータベースに保存する
			ContentResolver contentResolver = getContentResolver();
			ContentValues values = new ContentValues();
			values.put(EventInfo.TITLE, mTitleEditText.getText().toString());
			values.put(EventInfo.WHERE, mWhereEditText.getText().toString());
			values.put(EventInfo.CONTENT, mContentEditText.getText().toString());
			values.put(EventInfo.UPDATED,CommonCls.toDBDateString(new GregorianCalendar()));
			values.put(EventInfo.MODIFIED, 1);
			values.put(EventInfo.DELETED,0);
			if(mAllDayCheckBox.isChecked()){
				// 終日が設定されていたら　終了日は翌日、時刻はともに00:00にする
				GregorianCalendar c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mStartDateTextView.getText().toString(),
								"00:00"));
				values.put(EventInfo.START_TIME,CommonCls.toDBDateString(c));
				c.add(Calendar.DAY_OF_MONTH, 1);
				values.put(EventInfo.END_TIME,CommonCls.toDBDateString(c));
			}else{
				values.put(EventInfo.START_TIME,CommonCls.toDBDateString(
						mStartDateTextView.getText().toString(),
						mStartTimeTextView.getText().toString()));
				values.put(EventInfo.END_TIME,CommonCls.toDBDateString(
						mEndDateTextView.getText().toString(),
						mEndTimeTextView.getText().toString()));
			}
			if(mId == 0){
				//　IDが０なら新規なのでInsert
				contentResolver.insert(EventCalendarActivity.RESOLVER_URI, values);
				Log.d("CALENDAR","Insert:"+mId);
			}else{
				// IDが１以上なら更新なのでUpdate
				String where = EventInfo.ID+" = "+mId;
				contentResolver.update(EventCalendarActivity.RESOLVER_URI, values, where, null);
				Log.d("CALENDAR","Update: "+mId);
			}
			// 呼び出しもとに値を返すためのIntentを作成
			Intent intent = new Intent();
			// Extraに値をセット
			intent.putExtra(EventCalendarActivity.CHANGED,true);
			// 処理結果をセット
			setResult(RESULT_OK,intent);

			// 保存が完了したらアクティビティを終了する
			finish();
		}
	}
	
	/**
	 * DateOnClickListener
	 *  日付の文字列にセットされるリスナー
	 */
	private class DateOnClickListener implements OnClickListener{
		private Context mContext = null;
		public DateOnClickListener(Context c){
			// Contextが必要なので、コンストラクタで渡して覚えておく
			mContext = c;
		}
		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View v) {
			GregorianCalendar c = null;
			if(v == mStartDateTextView){
				//　開始日でクリックされた場合
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mStartDateTextView.getText().toString(),
								mStartTimeTextView.getText().toString()));
			}else if(v == mEndDateTextView){
				// 終了日でクリックされた場合
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mEndDateTextView.getText().toString(),
								mEndTimeTextView.getText().toString()));
			}else{
				return;
			}
			// DatePickerDialogを作成し表示する
			DatePickerDialog datePickerDialog = new DatePickerDialog(
					mContext,
					new DateSetListener(v),
					c.get(Calendar.YEAR),
					c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		}
	}
	/**
	 * DateSetListener
	 *  DatePickerDialogにセットする、設定時に呼び出されるリスナー
	 */
	private class DateSetListener implements OnDateSetListener{
		private View mView = null;
		public DateSetListener(View v){
			// どのビューをクリックされてい開かれたダイアログかを覚えておくために
			// コンストラクタでViewを保持しておく
			mView = v;
		}
		/**
		 * DatePickerDialogで設定が押されたとき呼び出されるメソッド
		 * 
		 * @param int y 年
		 * @param int m 月
		 * @param int d 日
		 */
		public void onDateSet(DatePicker picker, int y, int m, int d) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(y,m,d);
			// 引数で渡された年月日を該当するViewにセットする
			if(mView == mStartDateTextView){
				mStartDateTextView.setText(CommonCls.dateFormat.format(c.getTime()));
			}else if(mView == mEndDateTextView){
				mEndDateTextView.setText(CommonCls.dateFormat.format(c.getTime()));
			}
		}
	}

	/**
	 * TimeOnClickListener
	 *  時刻の文字列にセットされるリスナー
	 */
	private class TimeOnClickListener implements OnClickListener{
		private Context mContext = null;
		public TimeOnClickListener(Context c){
			// Contextが必要なので、コンストラクタで渡して覚えておく
			mContext = c;
		}

		/**
		 * クリックされた時呼び出される
		 * @param View クリックされたビュー
		 */
		public void onClick(View v) {
			GregorianCalendar c = null;
			if(v == mStartTimeTextView){
				//　開始時刻でクリックされた場合
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mStartDateTextView.getText().toString(),
								mStartTimeTextView.getText().toString()));
			}else if(v == mEndTimeTextView){
				//　終了時刻でクリックされた場合
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mEndDateTextView.getText().toString(),
								mEndTimeTextView.getText().toString()));
			}else{
				return;
			}
			// TimePickerDialogを作成し表示する
			TimePickerDialog timePickerDialog = new TimePickerDialog(
					mContext,
					new TimeSetListener(v),
					c.get(Calendar.HOUR_OF_DAY),
					c.get(Calendar.MINUTE),true);
			timePickerDialog.show();
		}
	}

	/**
	 * TimeSetListener
	 *  TimePickerDialogにセットする、設定時に呼び出されるリスナー
	 */
	private class TimeSetListener implements OnTimeSetListener{
		private View mView = null;
		public TimeSetListener(View v){
			// どのビューをクリックされて開かれたダイアログかを覚えておくために
			// コンストラクタでViewを保持しておく
			mView = v;
		}

		/**
		 * TimePickerDialogで設定が押されたとき呼び出されるメソッド
		 * 
		 * @param int h 時
		 * @param int m 分
		 */
		public void onTimeSet(TimePicker picker, int h, int m) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY,h);
			c.set(Calendar.MINUTE,m);
			// 引数で渡された年月日を該当するViewにセットする
			if(mView == mStartTimeTextView){
				mStartTimeTextView.setText(CommonCls.timeFormat.format(c.getTime()));
			}else if(mView == mEndTimeTextView){
				mEndTimeTextView.setText(CommonCls.timeFormat.format(c.getTime()));
			}
		}
	}
	/**
	 * AllDayチェックボックスのリスナー
	 */
	private class AllDayOnClickListener implements OnClickListener{
		/**
		 * AllDayチェックボックスをクリックされたとき呼び出される
		 */
		public void onClick(View v) {
			if(((CheckBox)v).isChecked()){
				// AllDay ONの時
				// 開始日以外のテキストを隠す
				mStartTimeTextView.setVisibility(View.INVISIBLE);
				mEndDateTextView.setVisibility(View.INVISIBLE);
				mEndTimeTextView.setVisibility(View.INVISIBLE);
			}else{
				// AllDay OFFの時
				// 開始日以外のテキストも表示する
				mStartTimeTextView.setVisibility(View.VISIBLE);
				mEndDateTextView.setVisibility(View.VISIBLE);
				mEndTimeTextView.setVisibility(View.VISIBLE);
			}
		}
	}

}
