package com.example.eventcalendar;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EventProvider extends ContentProvider {
	// EventDatabaseHelper�̃C���X�^���X
	private EventDatabaseHelper mEventDatabaseHelper = null;
	// ���݂̃f�[�^�x�[�X�̃o�[�W�����@�����Ȃ̂łP
	private static final int CURRENT_DATABASE_VERSION = 2;

	public boolean onCreate() {
		// EventDatabaseHelper�̃C���X�^���X���쐬����
		mEventDatabaseHelper = new EventDatabaseHelper(getContext());
		return false;
	}

	/**
	 * getType
	 * ���ɕK�v�͂Ȃ��̂ŁAnull��Ԃ�
	 */
	public String getType(Uri arg0) {
		return null;
	}

	/**
	 * query
	 *  �f�[�^�x�[�X���������Ēl��Ԃ�
	 */
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// �ǂݏo����p�Ńf�[�^�x�[�X���J��
		SQLiteDatabase db = mEventDatabaseHelper.getReadableDatabase();
		// �����̃p�����[�^�[��query�����s����
		Cursor c = db.query(EventInfo.DB_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		return c;
	}

	/**
	 * update
	 *  �f�[�^�x�[�X�̃��R�[�h���X�V����
	 */
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
		// �������݉\�ȏ�ԂŃf�[�^�x�[�X���J��
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// �����̃p�����[�^�[�ɏ]����update�����s����
		int numUpdated = db.update(EventInfo.DB_NAME, values, selection, selectionArgs);
		// ���ʂƂ��čX�V���ꂽ���R�[�h����Ԃ�
		return numUpdated;
	}

	/**
	 * insert
	 *  �f�[�^�x�[�X�Ƀ��R�[�h��ǉ�����
	 */
	public Uri insert(Uri uri, ContentValues values) {
		// �������݉\�ȏ�ԂŃf�[�^�x�[�X���J��
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// �����̃p�����[�^�[�ɏ]����insert�����s����
		long newId = db.insert(EventInfo.DB_NAME, null, values);
		// �V�K�ɒǉ����ꂽ���R�[�h��ID���A���Ă���̂ł��������Uri���쐬����
		Uri newUri =  Uri.parse(uri+"/"+newId);
		// �V�K���R�[�h���w��Uri��Ԃ�
		return newUri;
	}

	/**
	 * delete
	 *  �f�[�^�x�[�X�̃��R�[�h���폜����
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// �������݉\�ȏ�ԂŃf�[�^�x�[�X���J��
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// �����̃p�����[�^�ɏ]����delete�����s����
		int numDeleted = db.delete(EventInfo.DB_NAME,selection,selectionArgs);
		// �폜���ꂽ���R�[�h����Ԃ�
		return numDeleted;
	}

	/**
	 * EventDatabaseHelper
	 *  SQLiteOpenHelper���p�����A�f�[�^�x�[�X�t�@�C���̍쐬�Ȃǂ��s��
	 *
	 */
	public class EventDatabaseHelper extends SQLiteOpenHelper {

		public EventDatabaseHelper(Context context) {
			// super�N���X�̃R���X�g���N�^���Ăяo������
			super(context,EventInfo.DB_NAME+".db",null,CURRENT_DATABASE_VERSION);
		}

		/**
		 * onCreate�ł̓e�[�u�����쐬����
		 * EventDatabaseHelper��onCreate�̓e�[�u�������݂��Ȃ����ɌĂяo�����
		 */
		public void onCreate(SQLiteDatabase db) {
			// �e�[�u�����쐬����SQL�X�e�[�g�����g�𕶎���Ƃ��č쐬
			String sql = "CREATE TABLE "+EventInfo.DB_NAME+"("
					+ EventInfo.ID + " INTEGER PRIMARY KEY,"
					+ EventInfo.DELETED + " INTEGER,"
					+ EventInfo.MODIFIED + " INTEGER,"
					+ EventInfo.ALARM + " INTEGER,"
					+ EventInfo.ALARM_LIST + " TEXT,"
					+ EventInfo.TITLE + " TEXT,"
					+ EventInfo.CONTENT + " TEXT,"
					+ EventInfo.WHERE + " TEXT,"
					+ EventInfo.END_TIME + " TEXT,"
					+ EventInfo.START_TIME + " TEXT,"
					+ EventInfo.PUBLISHED + " TEXT,"
					+ EventInfo.UPDATED + " TEXT,"
					+ EventInfo.CATEGORY + " TEXT,"
					+ EventInfo.EDIT_URL + " TEXT,"
					+ EventInfo.EVENT_STATUS + " TEXT,"
					+ EventInfo.EVENT_ID + " TEXT,"
					+ EventInfo.ETAG + " TEXT"
					+ ");";
			// SQL�X�e�[�g�����g�����s
			db.execSQL(sql);
		}

		/**
		 * onUpgrade
		 *  onUpgrade�̓A�v���̃o�[�W�������オ���ăf�[�^�x�[�X�̃��R�[�h�ɕύX���������ꍇ�Ȃ�
		 *  Database�̃o�[�W����������Ă���Ƃ��ɌĂяo�����
		 *  
		 */
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// SQL�Ńe�[�u�����폜����X�e�[�g�����g�����s
			db.execSQL("DROP TABLE IF EXISTS " + EventInfo.DB_NAME);
			// onCreate���R�[�����ăe�[�u�����č쐬
			onCreate(db);
		}

	}

}
