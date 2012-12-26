package jp.gr.java_conf.BusinessCalendar;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * EventDetailActivity
 *  1日分のイベントの詳細を表示する
 */
public class EventDetailActivity extends Activity  implements OnItemLongClickListener {
	// 日付の文字列
	private String mDateString = null;
	// イベントエディタ起動のためのRequestCode
	public static final int EVENT_EDITOR = 2;
	// EventListViewのインスタンス
	private ListView mEventListView = null;
	// 新規イベント追加メニュー用ID
	private static final int NEW_EVENT_MENU_ID = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventdetail);
		// 呼び出しもとから送られたIntentを取得
		Intent intent = getIntent();
		// Intentの　Extraから日付文字列を取得
		mDateString  = intent.getStringExtra("date");
		// dateViewに日付をセット
		TextView dateView = (TextView)findViewById(R.id.detailDate);
		dateView.setText(mDateString);
		mEventListView = (ListView)findViewById(R.id.eventList);
		// 「setListAdapter」メソッドを作成する
		setListAdapter();
		// eventListViewのアイテムをクリックされた時の処理をセット
		mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
				// Intentを作成
				Intent intent = new Intent(EventDetailActivity.this,EventShowActivity.class);
				// ArrayAdapterの型を「EventInfo」クラスにまとめる
				EventInfo event = (EventInfo)parent.getAdapter().getItem(position);
				// IDと日付の文字列をExtraにセット
				intent.putExtra(EventInfo.ID, event.getId());
				intent.putExtra("date",mDateString);
				// EventEditorActivityを起動
				startActivityForResult(intent,EVENT_EDITOR);
			}
		});
		mEventListView.setOnItemLongClickListener(this);
	}

	private void setListAdapter(){
		mEventListView.setAdapter(new ArrayAdapter<EventInfo>(this,android.R.layout.simple_list_item_1,getEventDetail(mDateString)));
	}

	/**
	 * getEventDetail
	 *  日付を指定してEventの詳細のArrayを返す
	 * @param date 日付文字列
	 * @return ArrayList<EventInfo> EventInfoのArray
	 */
	private ArrayList<EventInfo> getEventDetail(String date){
		ArrayList<EventInfo> events = new ArrayList<EventInfo>();
		// 日付を指定して情報を取得
		ContentResolver contentResolver = getContentResolver();
		// 削除フラグのついているデータは検索しない
	    String selection = EventInfo.DELETED + " = 0 and "+EventInfo.START_TIME+" LIKE ?";
		String[] selectionArgs = {date+"%"};
		String sortOrder = EventInfo.START_TIME;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, selectionArgs, sortOrder);
		while(c.moveToNext()){
			// 情報を格納するためのEventInfoのインスタンスを作成
			EventInfo event = new EventInfo(contentResolver);
			// 情報を格納する
			event.setId(c.getLong(c.getColumnIndex(EventInfo.ID)));
			event.setTitle(c.getString(c.getColumnIndex(EventInfo.TITLE)));
			event.setStart(c.getString(c.getColumnIndex(EventInfo.START_TIME)));
			event.setEnd(c.getString(c.getColumnIndex(EventInfo.END_TIME)));
			event.setWhere(c.getString(c.getColumnIndex(EventInfo.WHERE)));
			event.setContent(c.getString(c.getColumnIndex(EventInfo.CONTENT)));
			// Arrayに追加
			events.add(event);
		}
		c.close();
		return events;
	}
	/**
	 * onActivityResult
	 *  呼び出したEditorの処理が完了したとき呼び出される
	 * @param requestCode 起動時に指定したrequestCode
	 * @param resultCode 呼び出したActivityが終了時に設定した終了コード
	 * @param data 呼び出したActivityが終了時に設定したIntent
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == EVENT_EDITOR && resultCode == RESULT_OK){
			if(data.getBooleanExtra(EventCalendarActivity.CHANGED,false)){
				// アクティビティからCHANGEDにtrueが返ってきていたらlistAdapterを更新する
				setListAdapter();
				Intent intent = new Intent();
				intent.putExtra(EventCalendarActivity.CHANGED, true);
				// EventCalendarActivity でも更新が必要なので
				// EventDetailActivityの結果として同様の値をセットする
				setResult(RESULT_OK,intent);
			}
		}
	}

	/**
	 * メニューボタンが押されたとき呼び出されるメソッド
	 *  メニューアイテムを準備する
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		// アイテムを追加
		menu.add(Menu.NONE,NEW_EVENT_MENU_ID,Menu.NONE,R.string.newEvent);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * メニューを開いて、選択されたとき呼び出されるメソッド
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		if(item.getItemId() == NEW_EVENT_MENU_ID){
			// メニューのIDが一致したら
			// ID=0でEventEditorActivityを起動
			Intent intent = new Intent(EventDetailActivity.this,EventEditorActivity.class);
			intent.putExtra(EventInfo.ID, 0);
			intent.putExtra("date", mDateString);
			startActivityForResult(intent,EVENT_EDITOR);
		}
		return true;
	}
	// 削除すべきレコードのId
	private long mDeleteId = 0;
	/**
	 * ListViewのItemで長押しされたときに呼び出されるリスナー
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		EventInfo event = (EventInfo)parent.getAdapter().getItem(position);
		mDeleteId = event.getId();
		// 「AlertDialog.Builder」を作成する
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// リソースからタイトル文字列を設定
		alertDialogBuilder.setTitle(R.string.deleteConfirm);
		// OKボタン（今回は削除ボタン）の文字列と処理関数の設定
		alertDialogBuilder.setPositiveButton(R.string.deleteOK, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				ContentResolver contentResolver = getContentResolver();
				String selection = EventInfo.ID+" = "+mDeleteId;
				// DeletedフラグとModifiedフラグをセットしUpdate
				// Deleteは行わない
				ContentValues cv = new ContentValues();
				cv.put(EventInfo.DELETED, 1);
				cv.put(EventInfo.MODIFIED, 1);
				contentResolver.update(EventCalendarActivity.RESOLVER_URI, cv, selection, null);
				// listAdapter を更新する
				setListAdapter();
				Intent intent = new Intent();
				intent.putExtra(EventInfo.CHANGED, true);
				setResult(RESULT_OK,intent);
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Cancelがクリックされたら何もしない
			}
		});
		// ダイアログをキャンセルできるようにする
		alertDialogBuilder.setCancelable(true);
		// ダイアログを生成する
		AlertDialog alertDialog = alertDialogBuilder.create();
		// ダイアログボックスの表示
		alertDialog.show();
		return true;
	}

}
