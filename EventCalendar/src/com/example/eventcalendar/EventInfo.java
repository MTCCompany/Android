package com.example.eventcalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

public class EventInfo {
	// データベース名
	public final static String DB_NAME = "events";
	// フィールド名
	public final static String ID = BaseColumns._ID;
	public final static String CHANGED = "changed";
	public final static String DELETED = "deleted";
	public final static String MODIFIED = "modified";
	public final static String ETAG = "etag";
	public final static String EVENT_ID = "calendar_id";
	public final static String PUBLISHED =  "published";
	public final static String UPDATED = "updated";
	public final static String CATEGORY = "category";
	public final static String TITLE = "title";
	public final static String CONTENT = "content";
	public final static String EVENT_STATUS = "gd_eventStatus";
	public final static String WHERE = "gd_where";
	public final static String EMAIL = "gd_who_email";
	public final static String RELATION = "gd_who_rel";
	public final static String WHO = "gd_who";
	public final static String END_TIME = "gd_when_endTime";
	public final static String START_TIME = "gd_when_startTime";
	public final static String EDIT_URL = "edit_url";
	public final static String NEW_ETAG = "New";
	public final static String ALARM = "alarm";
	public final static String ALARM_LIST = "alarm_list";

	// 一時間の分数
	public final static int HOUR_BY_MINUTES = 60;
	// 一分の秒数
	public final static int MINUTE_BY_SECONDS = 60;
	//　1秒のミリ秒数
	public final static int SECOND_BY_MILLI = 1000;
	// 1分のミリ秒数
	public final static int MINUTE_BY_MILLI = MINUTE_BY_SECONDS*SECOND_BY_MILLI;
	// データベース処理の為のResolver
	private ContentResolver mContentResolver = null;
	// レコードを保持する為のメンバ変数
	private long mId = 0;
	private long mDeleted = 0;
	private long mModified = 0;
	private String mTitle = null;
	private String mWhere = null;
	private GregorianCalendar mStart = null;
	private GregorianCalendar mEnd = null;
	private String mContent = null;
	private GregorianCalendar mPublished = null;
	private GregorianCalendar mUpdated = null;
	private String mCategory = null;
	private String mEditUrl = null;
	private String mEventStatus = null;
	private String mEventId = null;
	private String mEtag = null;
	private String mRecurrence = null;
	private long mAlarm = 0;
	private HashMap<String,ArrayList<String>> mAlarmMap = null;
	
	/**
	 * コンストラクタ
	 *  コンテントリゾルバを設定
	 * @param resolver
	 */
	public EventInfo(ContentResolver resolver) {
		mContentResolver = resolver;
	}

	/**
	 * toString
	 *  データベースに含まれる情報を一つの文字列として出力
	 */
	public String toString(){
		if(mStart.get(Calendar.HOUR_OF_DAY) == 0 && mStart.get(Calendar.MINUTE) == 0){
			GregorianCalendar startCal = (GregorianCalendar)mStart.clone();
			startCal.add(Calendar.DAY_OF_MONTH, 1);
			if(startCal.equals(mEnd)){
				// 開始時刻が00:00で終了日時が翌日の00:00の場合
				// 終日の予定と判断して開始日のみ表示する
				return getTitle()+"\n"
				+getStartDateString()+"\n"
				+getWhere()+"\n"
				+getContent();
			}
		}
		return getTitle()+"\n"
		+getStartDateString()+" "+getStartTimeString()+"\n"
		+getEndDateString()+" "+getEndTimeString()+"\n"
		+getWhere()+"\n"
		+getContent();
	}

	// ここから　setter/getter
	public void setId(long mId) {
		this.mId = mId;
	}

	public long getId() {
		return mId;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setWhere(String mWhere) {
		this.mWhere = mWhere;
	}

	public String getWhere() {
		return mWhere;
	}
	public void setStart(GregorianCalendar mStart) {
		this.mStart = mStart;
	}

	public void setStart(String dateString){
		this.mStart = CommonCls.toCalendar(dateString);
	}

	public Calendar getStart() {
		return mStart;
	}
	public String getStartString(){
		return CommonCls.toDBDateString(mStart);
	}
	public String getStartDateString(){
		return CommonCls.dateFormat.format(mStart.getTime());
	}

	public String getStartTimeString(){
		return CommonCls.timeFormat.format(mStart.getTime());
	}

	public void setEnd(GregorianCalendar mEnd) {
		this.mEnd = mEnd;
	}

	public String getEndString(){
		return CommonCls.toDBDateString(mEnd);
	}

	public void setEnd(String dateString){
		this.mEnd = CommonCls.toCalendar(dateString);
	}

	public Calendar getEnd() {
		return mEnd;
	}

	public String getEndDateString(){
		return CommonCls.dateFormat.format(mEnd.getTime());
	}

	public String getEndTimeString(){
		return CommonCls.timeFormat.format(mEnd.getTime());
	}

	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	public String getContent() {
		return mContent;
	}

	// ここからは　メンバ変数のsetter/getter関数
	public void setDeleted(long mDeleted) {
		this.mDeleted = mDeleted;
	}

	public long getDeleted() {
		return mDeleted;
	}

	public void setModified(long mModified) {
		this.mModified = mModified;
	}

	public long getModified() {
		return mModified;
	}

	public void setPublished(GregorianCalendar mPublished) {
		this.mPublished = mPublished;
	}

	public void setPublished(String date) {
		this.mPublished = CommonCls.toCalendar(date);
	}

	public GregorianCalendar getPublished() {
		return mPublished;
	}

	public String getPublishedString() {
		return CommonCls.toDBDateString(mPublished);
	}

	public void setUpdated(GregorianCalendar mUpdated) {
		this.mUpdated = mUpdated;
	}

	/**
	 * mUpdatedに文字列で値を設定します
	 * @param date
	 */
	public void setUpdated(String date) {
		this.mUpdated = CommonCls.toCalendar(date);
	}

	public GregorianCalendar getUpdated() {
		return mUpdated;
	}

	public String getUpdatedString() {
		return CommonCls.toDBDateString(mUpdated);
	}

	public void setCategory(String mCategory) {
		this.mCategory = mCategory;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setEditUrl(String mEditUrl) {
		this.mEditUrl = mEditUrl;
	}

	public String getEditUrl() {
		return mEditUrl;
	}

	public void setEventStatus(String mEventStatus) {
		this.mEventStatus = mEventStatus;
	}

	public String getEventStatus() {
		return mEventStatus;
	}

	public boolean isConfirmed(){
		return mEventStatus.contains("confirmed");
	}

	public boolean isCanceled(){
		return mEventStatus.contains("canceled");
	}

	public boolean isTentative(){
		return mEventStatus.contains("tentative");
	}

	/**
	 * EventIdは
	 * http://www.google.com/calendar/feeds/default/events/xxxxxxxxxxxxxxxxxxxxxxxxx
	 * の様にURLの形になっていますが実際には、xxxxの部分のみがIdなので、
	 * この部分のみを取り出します。
	 * 
	 * @param calendarId
	 */
	public void setEventId(String calendarId) {
		if(calendarId != null && calendarId.contains("/")){
			String[] splited = calendarId.split("/");
			this.mEventId = splited[splited.length-1];
		}else{
			this.mEventId = calendarId;
		}
	}

	public String getEventId() {
		return mEventId;
	}

	public void setEtag(String mEtag) {
		this.mEtag = mEtag;
	}

	public String getEtag() {
		return mEtag;
	}

	public void setRecurrence(String mRecurrence) {
		this.mRecurrence = mRecurrence;
	}

	public String getRecurrence() {
		return mRecurrence;
	}

	/**
	 * EventInfoのメンバ変数に基づいてデータベースを更新する
	 */
	public void updateDB(){
		// mRecurrenceが存在するデータについてはこのアプリでは無視する
		if(mRecurrence != null){
			return;
		}
		Log.d("CALENDAR","UpdateDB"+mTitle+":"+getStartTimeString());
		// タイトルと更新日時を取得する
		String[] projection={TITLE,UPDATED};
		String selection = EVENT_ID+" = ?";
		String[] selectionArgs = {mEventId};
		// 読み込まれたデータのID情報をもとにデータベースを検索
		Cursor c = mContentResolver.query(EventCalendarActivity.RESOLVER_URI, 
				projection, selection, selectionArgs, null);
		if(c.moveToNext()){
			// すでに存在しているデータの場合は更新処理
			String dbUpdated = c.getString(c.getColumnIndex(UPDATED));
			c.close();
			GregorianCalendar cal = CommonCls.toCalendar(dbUpdated);
			// 更新日時を比較する(Calendar#compareTo)
			//  等しければ、0
			//  Googleカレンダーの更新日時(mUpdated) が新しい場合は 1
			//  データベースの更新日時(cal)が新しい場合は -1
			int comp = mUpdated.compareTo(cal);
			if(comp==0){
				// アップデートの日時が等しい
			}else if(comp>0){
				// Googleカレンダー側が新しいので更新する。
				if(isCanceled()){
					// Googleカレンダーで削除されたものなら、データベースから削除
					mContentResolver.delete(EventCalendarActivity.RESOLVER_URI, selection, selectionArgs);
				}else{
					mContentResolver.update(EventCalendarActivity.RESOLVER_URI,getValues(),selection,selectionArgs);
				}
			}else if(comp<0){
				// DB側が新しければ後でGoogleカレンダーにアップロードするためにrecordにMODIFIEDフラグをセット
				ContentValues cv = new ContentValues();
				cv.put(MODIFIED,1);
				mContentResolver.update(EventCalendarActivity.RESOLVER_URI,cv,selection,selectionArgs);
			}
		}else{
			// DBに同じデータが見つからなかったとき
			c.close();
			if(isCanceled()){
				// Googleカレンダー側で削除されていれば何もしない
				return;
			}
			// 新規データとしてInsert
			mContentResolver.insert(EventCalendarActivity.RESOLVER_URI, getValues());
		}
	}

	/**
	 * EventInfoのデータを元にContentValues を作成する。
	 *
	 * @return ContentValues resolverのupdate,insertなどで使用するContentValuesクラス
	 */
	public ContentValues getValues(){
		ContentValues cv = new ContentValues();
		cv.put(DELETED, mDeleted);
		cv.put(MODIFIED,mModified);
		cv.put(TITLE,mTitle);
		cv.put(WHERE, mWhere);
		cv.put(START_TIME,getStartString());
		cv.put(END_TIME, getEndString());
		cv.put(CONTENT, mContent);
		cv.put(PUBLISHED, getPublishedString());
		cv.put(UPDATED, getUpdatedString());
		cv.put(CATEGORY,mCategory);
		cv.put(EDIT_URL, mEditUrl);
		cv.put(EVENT_STATUS, mEventStatus);
	    cv.put(EVENT_ID, mEventId);
		cv.put(ETAG, mEtag);
		// Alarmリストを追加
		cv.put(ALARM_LIST, getAlarmMapString());
		// mAlarmListの値を元に直近のアラーム計算する
		calcAlarm();
		// 直近のAlarmの値を登録
		cv.put(ALARM, mAlarm);
		return cv;
	}

	/**
	 * CursorからEventInfoの値をセットする
	 */
	public void setValues(Cursor c){
		mId = c.getLong(c.getColumnIndex(ID));
		mDeleted = c.getLong(c.getColumnIndex(DELETED));
		mAlarm = c.getLong(c.getColumnIndex(ALARM));
		mModified = c.getLong(c.getColumnIndex(MODIFIED));
		mTitle = c.getString(c.getColumnIndex(TITLE));
		mWhere = c.getString(c.getColumnIndex(WHERE));
		setStart(c.getString(c.getColumnIndex(START_TIME)));
		setEnd(c.getString(c.getColumnIndex(END_TIME)));
		mContent = c.getString(c.getColumnIndex(CONTENT));
		setPublished(c.getString(c.getColumnIndex(PUBLISHED)));
		setUpdated(c.getString(c.getColumnIndex(UPDATED)));
		mCategory = c.getString(c.getColumnIndex(CATEGORY));
		mEditUrl = c.getString(c.getColumnIndex(EDIT_URL));
		mEventStatus = c.getString(c.getColumnIndex(EVENT_STATUS));
	    mEventId = c.getString(c.getColumnIndex(EVENT_ID));
		mEtag = c.getString(c.getColumnIndex(ETAG));
		// Alarmのリストを設定する
		setAlarmMap(c.getString(c.getColumnIndex(ALARM_LIST)));
		// Alaramの値を計算し直す
		calcAlarm();
		mRecurrence = null;
	}

	public void setAlarm(long mAlarm) {
		this.mAlarm = mAlarm;
	}

	/**
	 * スタート時刻の何分前かという値から実際のミリ秒の値を計算してセットする。
	 *
	 * @param String before アラーム設定の分数
	 */
	public void setAlarmBefore(String before) {
		if(before == null || before.equals("")){
			this.mAlarm = 0;
		}
		this.mAlarm = mStart.getTimeInMillis()-Integer.valueOf(before)*MINUTE_BY_MILLI;
	}

	public long getAlarm() {
		return mAlarm;
	}

	/**
	 * Alarm Mapに値を追加する。
	 *
	 * @param String key    キー
	 * @param String value  値
	 */
	public void addToAlarmMap(String key,String value) {
		if(key == null || key.equals("")){
			return ;
		}
		if(value == null){
			value = "";
		}
		if(mAlarmMap == null){
			// マップが存在しない場合はHashMapのインスタンスを作成
			mAlarmMap = new HashMap<String,ArrayList<String>>();
		}
		if(mAlarmMap.containsKey(key)){
			// MapにKeyが存在したら、そのKeyに値を追加
			mAlarmMap.get(key).add(value);
		}else{
			// MapにKeyが存在しないのでKeyとListのセットを追加する。
			ArrayList<String>al = new ArrayList<String>();
			al.add(value);
			mAlarmMap.put(key, al);
		}
	}

	public HashMap<String,ArrayList<String>> getAlarmMap() {
		return mAlarmMap;
	}

	/**
	 * AlarmMapを文字列で取り出す
	 *
	 * @return String Alarmの値を持った文字列
	 */
	public String getAlarmMapString(){
		if(mAlarmMap == null || mAlarmMap.isEmpty()){
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = mAlarmMap.keySet();
		for(String k : keys){
			List<String>values = mAlarmMap.get(k);
			for(String v : values){
				sb.append(""+k+":"+v+",");
			}
		}
		return sb.toString();
	}

	/**
	 * Alarmの文字列からMapに値を設定する
	 *
	 * @param String alarm情報の文字列
	 */
	public void setAlarmMap(String s){
		if(s==null || s.equals("")){
			return;
		}
		String[] pairs = s.split(",");
		for(String p : pairs){
			if(!p.equals("")){
				String[] kv = p.split(":");
				addToAlarmMap(kv[0],kv[1]);
			}
		}
	}
	/**
	 * Alarmのデータを計算して、直近のAlarmの値をセットする
	 */
	public void calcAlarm(){
		mAlarm = 0;
		if(mAlarmMap == null){
			return;
		}
		long now = Calendar.getInstance().getTimeInMillis();
		long start = mStart.getTimeInMillis();
		if(start<now){
			return;
		}
		long min = start;
		Set<String> keys = mAlarmMap.keySet();
		for(String k : keys){
			if(k.equals(CalendarParser.VAL_ALERT)){
				List<String>values = mAlarmMap.get(k);
				for(String v : values){
					long alarm = start - Integer.valueOf(v)*MINUTE_BY_MILLI;
					if(now < alarm  && alarm < min){
						min=alarm;
					}
				}
			}
		}
		if(min < start){
			mAlarm = min;
		}
	}
}
