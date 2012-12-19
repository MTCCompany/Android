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
	// �f�[�^�x�[�X��
	public final static String DB_NAME = "events";
	// �t�B�[���h��
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

	// �ꎞ�Ԃ̕���
	public final static int HOUR_BY_MINUTES = 60;
	// �ꕪ�̕b��
	public final static int MINUTE_BY_SECONDS = 60;
	//�@1�b�̃~���b��
	public final static int SECOND_BY_MILLI = 1000;
	// 1���̃~���b��
	public final static int MINUTE_BY_MILLI = MINUTE_BY_SECONDS*SECOND_BY_MILLI;
	// �f�[�^�x�[�X�����ׂ̈�Resolver
	private ContentResolver mContentResolver = null;
	// ���R�[�h��ێ�����ׂ̃����o�ϐ�
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
	 * �R���X�g���N�^
	 *  �R���e���g���]���o��ݒ�
	 * @param resolver
	 */
	public EventInfo(ContentResolver resolver) {
		mContentResolver = resolver;
	}

	/**
	 * toString
	 *  �f�[�^�x�[�X�Ɋ܂܂�������̕�����Ƃ��ďo��
	 */
	public String toString(){
		if(mStart.get(Calendar.HOUR_OF_DAY) == 0 && mStart.get(Calendar.MINUTE) == 0){
			GregorianCalendar startCal = (GregorianCalendar)mStart.clone();
			startCal.add(Calendar.DAY_OF_MONTH, 1);
			if(startCal.equals(mEnd)){
				// �J�n������00:00�ŏI��������������00:00�̏ꍇ
				// �I���̗\��Ɣ��f���ĊJ�n���̂ݕ\������
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

	// ��������@setter/getter
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

	// ��������́@�����o�ϐ���setter/getter�֐�
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
	 * mUpdated�ɕ�����Œl��ݒ肵�܂�
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
	 * EventId��
	 * http://www.google.com/calendar/feeds/default/events/xxxxxxxxxxxxxxxxxxxxxxxxx
	 * �̗l��URL�̌`�ɂȂ��Ă��܂������ۂɂ́Axxxx�̕����݂̂�Id�Ȃ̂ŁA
	 * ���̕����݂̂����o���܂��B
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
	 * EventInfo�̃����o�ϐ��Ɋ�Â��ăf�[�^�x�[�X���X�V����
	 */
	public void updateDB(){
		// mRecurrence�����݂���f�[�^�ɂ��Ă͂��̃A�v���ł͖�������
		if(mRecurrence != null){
			return;
		}
		Log.d("CALENDAR","UpdateDB"+mTitle+":"+getStartTimeString());
		// �^�C�g���ƍX�V�������擾����
		String[] projection={TITLE,UPDATED};
		String selection = EVENT_ID+" = ?";
		String[] selectionArgs = {mEventId};
		// �ǂݍ��܂ꂽ�f�[�^��ID�������ƂɃf�[�^�x�[�X������
		Cursor c = mContentResolver.query(EventCalendarActivity.RESOLVER_URI, 
				projection, selection, selectionArgs, null);
		if(c.moveToNext()){
			// ���łɑ��݂��Ă���f�[�^�̏ꍇ�͍X�V����
			String dbUpdated = c.getString(c.getColumnIndex(UPDATED));
			c.close();
			GregorianCalendar cal = CommonCls.toCalendar(dbUpdated);
			// �X�V�������r����(Calendar#compareTo)
			//  ��������΁A0
			//  Google�J�����_�[�̍X�V����(mUpdated) ���V�����ꍇ�� 1
			//  �f�[�^�x�[�X�̍X�V����(cal)���V�����ꍇ�� -1
			int comp = mUpdated.compareTo(cal);
			if(comp==0){
				// �A�b�v�f�[�g�̓�����������
			}else if(comp>0){
				// Google�J�����_�[�����V�����̂ōX�V����B
				if(isCanceled()){
					// Google�J�����_�[�ō폜���ꂽ���̂Ȃ�A�f�[�^�x�[�X����폜
					mContentResolver.delete(EventCalendarActivity.RESOLVER_URI, selection, selectionArgs);
				}else{
					mContentResolver.update(EventCalendarActivity.RESOLVER_URI,getValues(),selection,selectionArgs);
				}
			}else if(comp<0){
				// DB�����V������Ό��Google�J�����_�[�ɃA�b�v���[�h���邽�߂�record��MODIFIED�t���O���Z�b�g
				ContentValues cv = new ContentValues();
				cv.put(MODIFIED,1);
				mContentResolver.update(EventCalendarActivity.RESOLVER_URI,cv,selection,selectionArgs);
			}
		}else{
			// DB�ɓ����f�[�^��������Ȃ������Ƃ�
			c.close();
			if(isCanceled()){
				// Google�J�����_�[���ō폜����Ă���Ή������Ȃ�
				return;
			}
			// �V�K�f�[�^�Ƃ���Insert
			mContentResolver.insert(EventCalendarActivity.RESOLVER_URI, getValues());
		}
	}

	/**
	 * EventInfo�̃f�[�^������ContentValues ���쐬����B
	 *
	 * @return ContentValues resolver��update,insert�ȂǂŎg�p����ContentValues�N���X
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
		// Alarm���X�g��ǉ�
		cv.put(ALARM_LIST, getAlarmMapString());
		// mAlarmList�̒l�����ɒ��߂̃A���[���v�Z����
		calcAlarm();
		// ���߂�Alarm�̒l��o�^
		cv.put(ALARM, mAlarm);
		return cv;
	}

	/**
	 * Cursor����EventInfo�̒l���Z�b�g����
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
		// Alarm�̃��X�g��ݒ肷��
		setAlarmMap(c.getString(c.getColumnIndex(ALARM_LIST)));
		// Alaram�̒l���v�Z������
		calcAlarm();
		mRecurrence = null;
	}

	public void setAlarm(long mAlarm) {
		this.mAlarm = mAlarm;
	}

	/**
	 * �X�^�[�g�����̉����O���Ƃ����l������ۂ̃~���b�̒l���v�Z���ăZ�b�g����B
	 *
	 * @param String before �A���[���ݒ�̕���
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
	 * Alarm Map�ɒl��ǉ�����B
	 *
	 * @param String key    �L�[
	 * @param String value  �l
	 */
	public void addToAlarmMap(String key,String value) {
		if(key == null || key.equals("")){
			return ;
		}
		if(value == null){
			value = "";
		}
		if(mAlarmMap == null){
			// �}�b�v�����݂��Ȃ��ꍇ��HashMap�̃C���X�^���X���쐬
			mAlarmMap = new HashMap<String,ArrayList<String>>();
		}
		if(mAlarmMap.containsKey(key)){
			// Map��Key�����݂�����A����Key�ɒl��ǉ�
			mAlarmMap.get(key).add(value);
		}else{
			// Map��Key�����݂��Ȃ��̂�Key��List�̃Z�b�g��ǉ�����B
			ArrayList<String>al = new ArrayList<String>();
			al.add(value);
			mAlarmMap.put(key, al);
		}
	}

	public HashMap<String,ArrayList<String>> getAlarmMap() {
		return mAlarmMap;
	}

	/**
	 * AlarmMap�𕶎���Ŏ��o��
	 *
	 * @return String Alarm�̒l��������������
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
	 * Alarm�̕����񂩂�Map�ɒl��ݒ肷��
	 *
	 * @param String alarm���̕�����
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
	 * Alarm�̃f�[�^���v�Z���āA���߂�Alarm�̒l���Z�b�g����
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
