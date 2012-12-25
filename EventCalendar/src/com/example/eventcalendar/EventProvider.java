package com.example.eventcalendar;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EventProvider extends ContentProvider {
	// EventDatabaseHelperのインスタンス
	private EventDatabaseHelper mEventDatabaseHelper = null;
	// 現在のデータベースのバージョン　初期なので１
	private static final int CURRENT_DATABASE_VERSION = 2;

	public boolean onCreate() {
		// EventDatabaseHelperのインスタンスを作成する
		mEventDatabaseHelper = new EventDatabaseHelper(getContext());
		return false;
	}

	/**
	 * getType
	 * 特に必要はないので、nullを返す
	 */
	public String getType(Uri arg0) {
		return null;
	}

	/**
	 * query
	 *  データベースを検索して値を返す
	 */
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// 読み出し専用でデータベースを開く
		SQLiteDatabase db = mEventDatabaseHelper.getReadableDatabase();
		// 引数のパラメーターでqueryを実行する
		Cursor c = db.query(EventInfo.DB_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		return c;
	}

	/**
	 * update
	 *  データベースのレコードを更新する
	 */
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
		// 書き込み可能な状態でデータベースを開く
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// 引数のパラメーターに従ってupdateを実行する
		int numUpdated = db.update(EventInfo.DB_NAME, values, selection, selectionArgs);
		// 結果として更新されたレコード数を返す
		return numUpdated;
	}

	/**
	 * insert
	 *  データベースにレコードを追加する
	 */
	public Uri insert(Uri uri, ContentValues values) {
		// 書き込み可能な状態でデータベースを開く
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// 引数のパラメーターに従ってinsertを実行する
		long newId = db.insert(EventInfo.DB_NAME, null, values);
		// 新規に追加されたレコードのIDが帰ってくるのでそれを元にUriを作成する
		Uri newUri =  Uri.parse(uri+"/"+newId);
		// 新規レコードを指すUriを返す
		return newUri;
	}

	/**
	 * delete
	 *  データベースのレコードを削除する
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// 書き込み可能な状態でデータベースを開く
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		// 引数のパラメータに従ってdeleteを実行する
		int numDeleted = db.delete(EventInfo.DB_NAME,selection,selectionArgs);
		// 削除されたレコード数を返す
		return numDeleted;
	}

	/**
	 * EventDatabaseHelper
	 *  SQLiteOpenHelperを継承し、データベースファイルの作成などを行う
	 *
	 */
	public class EventDatabaseHelper extends SQLiteOpenHelper {

		public EventDatabaseHelper(Context context) {
			// superクラスのコンストラクタを呼び出す処理
			super(context,EventInfo.DB_NAME+".db",null,CURRENT_DATABASE_VERSION);
		}

		/**
		 * onCreateではテーブルを作成する
		 * EventDatabaseHelperのonCreateはテーブルが存在しない時に呼び出される
		 */
		public void onCreate(SQLiteDatabase db) {
			// テーブルを作成するSQLステートメントを文字列として作成
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
			// SQLステートメントを実行
			db.execSQL(sql);
		}

		/**
		 * onUpgrade
		 *  onUpgradeはアプリのバージョンが上がってデータベースのレコードに変更があった場合など
		 *  Databaseのバージョンが違っているときに呼び出される
		 *  
		 */
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// SQLでテーブルを削除するステートメントを実行
			db.execSQL("DROP TABLE IF EXISTS " + EventInfo.DB_NAME);
			// onCreateをコールしてテーブルを再作成
			onCreate(db);
		}

	}

}
