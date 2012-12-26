package jp.gr.java_conf.BusinessCalendar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class EventCalendarActivity extends Activity implements OnClickListener{
	// 一週間の日数
	private static final int DAYS_OF_WEEK = 7;
	// GridViewのインスタンス
	private GridView mGridView = null;
	// DateCellAdapterのインスタンス
	private DateCellAdapter mDateCellAdapter = null;
	// 現在注目している年月日を保持する変数
	private GregorianCalendar mCalendar = null;
	// カレンダーの年月を表示するTextView
	private TextView mYearMonthTextView = null;
	// ContentResolverのインスタンス
	private ContentResolver mContentResolver = null;
	// EventProviderのUri
	public static final Uri RESOLVER_URI = Uri.parse("content://jp.gr.java_conf.BusinessCalendar.eventprovider");
	// EventDetailActivityを呼び出すためのrequestコード
	protected static final int EVENT_DETAIL = 2;
	// 前月ボタンのインスタンス
	private Button mPrevMonthButton = null;
	// 次月ボタンのインスタンス
	private Button mNextMonthButton = null;
	// Activityでデータベースが更新されたことを伝えるためのタグ
	public static final String CHANGED = "changed";
	// 前回更新日時の為のタグ
	public static final String LAST_UPDATE = "LastUpdate";
	// 今回の更新開始日時
	private String mUpdateStartTime = null;
	// 前回更新日時
	private String mLastUpdate = null;

	// 認証用トークンを保持する変数
	private String mAccessToken;
	private String mRefreshToken;
	private long mAccessTokenExpire;
	// AuthCodeを保持する変数
	private String mAuthCode;
	// 認証用トークンの名前
	private static final String AUTH_INFO = "authInfo";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String ACCESS_TOKEN_EXPIRE = "access_token_expire";

	// クライアントシークレット　Google APISから取得
	public static final String CLIENT_SECRET = "1ET09oOy2CEezagy8lmOZdNe";
	// OAUTH用URL
	public static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	// クライアントID　Google APISから取得
	public static final String CLIENT_ID = "155205884318.apps.googleusercontent.com";
	// リダイレクトURI　Google APIから取得
	public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	// カレンダーのスコープ
	public static final String SCOPE = "https://www.google.com/calendar/feeds/";
	//　レスポンスコード　スタンドアロンアプリケーションなので、コードで取得する
	public static final String RESPONSE_CODE = "code";
	// OAUTHに使うURI　
	public static final Uri OAUTH_URI = Uri.parse(OAUTH_URL
			+"?client_id="+CLIENT_ID
			+"&redirect_uri="+REDIRECT_URI
			+"&scope="+SCOPE
			+"&response_type="+RESPONSE_CODE);
	// onActivityResultで使用するActivityのrequestコード（ブラウザ起動用）
	public static final int BROWSER = 1;
	// onActivityResultで使用するActivityのrequestコード（AuthCode入力用）
	public static final int AUTH_CODE_ACTIVITY = 3;
	// AuthCodeを受け取る為のタグ
	public static final String AUTH_CODE = "AuthCode";
	
	// 
	private CalendarHttpClient mCalendarHttpClient = new CalendarHttpClient();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventcalendar);
		mGridView = (GridView)findViewById(R.id.gridView1);
		// Gridカラム数を設定する
		mGridView.setNumColumns(DAYS_OF_WEEK);
		// DateCellAdapterのインスタンスを作成する
		mDateCellAdapter = new DateCellAdapter(this);
		// GridViewに「DateCellAdapter」をセット
		mGridView.setAdapter(mDateCellAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
				// カレンダーをコピー
				Calendar cal = (Calendar)mCalendar.clone();
				// positionから日付を計算
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.add(Calendar.DAY_OF_MONTH, position-cal.get(Calendar.DAY_OF_WEEK)+1);
				// 日付文字列を生成
				String dateString = DateStrCls.dateFormat.format(cal.getTime());
				// Intent  を作成
				Intent intent = new Intent(EventCalendarActivity.this,EventDetailActivity.class);
				// 日付をExtraにセット
				intent.putExtra("date", dateString);
				// Activityを実行
				startActivityForResult(intent,EVENT_DETAIL);
			}
		});

		mYearMonthTextView = (TextView)findViewById(R.id.yearMonth);
		// 「GregorianCalendar」のインスタンスの作成
		mCalendar = new GregorianCalendar();
		// 年月の取得
		int year = mCalendar.get(Calendar.YEAR);
		int month = mCalendar.get(Calendar.MONTH)+1;
		// 年月のビューへの表示
		mYearMonthTextView.setText(year+"/"+month);
		// ContentResolverの取得
		mContentResolver = getContentResolver();
		// 前月ボタンにListenerを設定
		mPrevMonthButton = (Button)findViewById(R.id.prevMonth);
		mPrevMonthButton.setOnClickListener(this);
		// 次月ボタンにListenerを設定
		mNextMonthButton = (Button)findViewById(R.id.nextMonth);
		mNextMonthButton.setOnClickListener(this);
	}

	/**
	 * onClick
	 *  前月、次月ボタンでクリックされたとき呼び出される
	 */
	public void onClick(View v) {
		// 現在の注目している日付を当月の1日に変更する
		mCalendar.set(Calendar.DAY_OF_MONTH,1);
		if(v == mPrevMonthButton){
			// 1ヶ月減算する
			mCalendar.add(Calendar.MONTH, -1);
		}else if(v == mNextMonthButton){
			// 1ヶ月加算する
			mCalendar.add(Calendar.MONTH, 1);
		}
		mYearMonthTextView.setText(mCalendar.get(Calendar.YEAR)+"/"+(mCalendar.get(Calendar.MONTH)+1));
		mDateCellAdapter.notifyDataSetChanged();
	}

	/**
	 * onActivityResult
	 *  呼び出したEditorの処理が完了したとき呼び出される
	 * @param requestCode 起動時に指定したrequestCode
	 * @param resultCode 呼び出したActivityが終了時に設定した終了コード
	 * @param data 呼び出したActivityが終了時に設定したIntent
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == EVENT_DETAIL && resultCode == RESULT_OK){
			if(data.getBooleanExtra(EventCalendarActivity.CHANGED,false)){
				// EVENT_DETAILの処理結果がOKでChangedがtrueなら、データベース更新を通知
				mDateCellAdapter.notifyDataSetChanged();
				// Widgetの内容を更新する通知をおこなう。
				Intent serviceIntent = new Intent(this,WidgetService.class);
				startService(serviceIntent);
			}
		}else if(requestCode == BROWSER){
			//ブラウザが終了したら、コードを入力するアクティビティを起動する。
			Intent intent = new Intent(EventCalendarActivity.this,AuthCodeActivity.class);
			startActivityForResult(intent,AUTH_CODE_ACTIVITY);
		}else if(requestCode == AUTH_CODE_ACTIVITY){
			if(resultCode == RESULT_OK){
				// AuthCodeが正常終了したらIntentからAuthCodeを取得
				mAuthCode = data.getStringExtra(AUTH_CODE);
				// AuthCodeが正しく取得できた場合は、再度syncCalendarを実行
				syncCalendar();
			}
		}
	}

	/**
	 * メニューボタンが押されたときの処理
	 *
	 * @param Menu 現在のメニュー
	 * @return メニューの生成に成功したらtrue
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		// MenuInflaterを取得する
		MenuInflater menuInflater = getMenuInflater();
		// MenuInflaterを使用してメニューをリソースから作成する
		menuInflater.inflate(R.menu.menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * メニューが選択されたときの処理
	 *
	 * @param MenuItem 選択されたメニューアイテム
	 * @return 処理を行った場合はtrue 
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		if(item.getItemId() == R.id.syncMenu){
			syncCalendar();
			return true;
		}
		return false;
	}
	// プログレスダイアログのインスタンス
	private ProgressDialog mProgressDialog = null;
	/**
	 * getGoogleCalendar と updateGoogleCalendarを呼び出す
	 * プログレスダイアログを表示する為に別スレッドで実行する。
	 */
	public void syncGoogleCalendar(){
		// プログレスダイアログの作成
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(getString(R.string.nowOnSync));
		// 途中で停止することはできない
		mProgressDialog.setCancelable(false);
		// プログレスダイアログの表示
		mProgressDialog.show();
		// 実際の処理を行うスレッドを作成
		Thread thread = new Thread(runSyncGoogleCalendar);
		// スレッドの実行開始
		thread.start();
	}

	// notifyDataSetChangedを受け取るhandler
	private Handler mNotifyHandler = new Handler();
	/**
	 * getGoogleCalendarの実行を行うスレッド
	 */
	private Runnable runSyncGoogleCalendar = new Runnable(){
		public void run() {
			// 更新処理本体
			getGoogleCalendar();
			updateGoogleCalendar();
			// イベントデータが更新されたのでアラームを更新
			AlarmReceiver.updateAlarm(EventCalendarActivity.this);
			// Widgetの内容を更新する通知を行う
			Intent serviceIntent = new Intent(EventCalendarActivity.this,WidgetService.class);
			startService(serviceIntent);
			// 更新が終わったらプログレスダイアログを消去
			mProgressDialog.dismiss();
			// notifyhandlerにnotifyDataSetChangedの処理をポスト
			mNotifyHandler.post(new Runnable(){
				public void run(){
					// mNotifyHandlerを持っているプロセスで実行される。
					mDateCellAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	/**
	 * Googleカレンダーと同期する
	 */
	public void syncCalendar(){
		// 更新開始時刻を保存
		mUpdateStartTime = DateStrCls.toUTCString(new GregorianCalendar());
		// プリファレンス確認のためにsharedPreferencesを取得
		SharedPreferences sharedPreferences  = getSharedPreferences(AUTH_INFO, MODE_PRIVATE);
		// 第二引数はデフォルト値：指定した名前のデータが無かったときに返ってくる値
		mAccessToken  = sharedPreferences.getString(ACCESS_TOKEN, null);
		mRefreshToken  = sharedPreferences.getString(REFRESH_TOKEN,null);
		mAccessTokenExpire = sharedPreferences.getLong(ACCESS_TOKEN_EXPIRE, 0);
		// 前回更新時刻を取得
		mLastUpdate = sharedPreferences.getString(LAST_UPDATE,null);
		if(mRefreshToken == null){
			// RefreshToken が無いので、認証処理を開始
			if(mAuthCode == null || mAuthCode.equals("")){
				// AuthCodeがなければstartOAuthを実行
				startOAuth();
			}else{
				// AuthCodeがあれば、すでにWebでのAuth処理は行っているので、AccessTokenを取得
				if(getAccessToken()){
					syncGoogleCalendar();
				}
			}
		}else if(mAccessTokenExpire<Calendar.getInstance().getTimeInMillis()){
			// AccessToken が期限切れなのでAccessTokenを再取得してから、getGoogleCalendarを実行
			if(refreshToken()){
				syncGoogleCalendar();

			}
		}else{
			// AccessTokenが有効なので、そのままgetGoogleCalendar()を実行
			syncGoogleCalendar();
		}
	}
	// カレンダーAPIに特定のFeedではないデータにアクセスするためのURL
	public static final String DEFAULT_URL = "https://www.google.com/calendar/feeds/default/private/full";
	/**
	 * Googleカレンダーにデータをアップロードする
	 */
	public void updateGoogleCalendar(){
		// MODIFIEDフラグの立っているデータを検索
		String selection = EventInfo.MODIFIED + " = 1";
		Cursor c = mContentResolver.query(RESOLVER_URI, null, selection, null, null);

		// insert/update/deleteそれぞれのデータのリスト
		ArrayList<EventInfo> insertEvents = new ArrayList<EventInfo>();
		ArrayList<EventInfo> updateEvents = new ArrayList<EventInfo>();
		ArrayList<EventInfo> deleteEvents = new ArrayList<EventInfo>();

		while(c.moveToNext()){
			EventInfo eventInfo = new EventInfo(mContentResolver);
			eventInfo.setValues(c);
			if(eventInfo.getEventId() == null){
				// eventIdが設定されていないデータは、Googleカレンダーにはない新規データなので
				// insertリストに登録
				insertEvents.add(eventInfo);
			}else if(eventInfo.getDeleted() == 1){
				// 削除フラグが立っているのでdelteリストに登録
				deleteEvents.add(eventInfo);
			}else {
				// それ以外はupdateリストに登録
				updateEvents.add(eventInfo);
			}
		}
		c.close();
		// パーサを作成
		CalendarParser cp = new CalendarParser(mContentResolver);
		// insert処理
		for(EventInfo e : insertEvents){
			// insert用のデータを作成しPOSTして結果を取得
			// 結果をparseして新しいデータとして登録
			cp.parse(mCalendarHttpClient.httpPost(DEFAULT_URL+"?oauth_token="+mAccessToken,cp.insertSerializer(e)));
			if(mCalendarHttpClient.mHttpSucceeded){
				// 古いデータを削除
				selection = EventInfo.ID + " = " + e.getId();
				mContentResolver.delete(RESOLVER_URI, selection, null);
			}
		}
		// update処理
		for(EventInfo e : updateEvents){
			// 編集用URLを取得
			String url = e.getEditUrl()+"?oauth_token="+mAccessToken;
			// アップデート結果をパースして現在のデータを更新
			cp.parse(mCalendarHttpClient.httpPut(url,cp.updateSerializer(mCalendarHttpClient.httpGet(url),e)));
		}
		// delete処理
		for(EventInfo e : deleteEvents){
			// 編集用URLを使って、httpDeleteの処理
			mCalendarHttpClient.httpDelete(e.getEditUrl()+"?oauth_token="+mAccessToken);
			if(mCalendarHttpClient.mHttpSucceeded){
				// データベースからも削除
				selection = EventInfo.ID + " = " + e.getId();
				mContentResolver.delete(RESOLVER_URI, selection, null);
			}
		}
	}

	/**
	 * startOAuth
	 * OAuth 2.0に従った認証処理の為にブラウザを使用して
	 * Googleの認証ページを開く
	 */
	private void startOAuth(){
		// URIを指定してブラウザを起動する為のIntentを作成する。
		// 暗黙的インテントなので、ユーザの選択したブラウザでURIを開く
		Intent intent = new Intent(Intent.ACTION_VIEW, OAUTH_URI);
		startActivityForResult(intent,BROWSER);
	}

	/**
	 * サーバからTokenを取得する
	 *
	 * @param ArrayList<NameValuePair> 名前と値のペアのリスト
	 * @return Map<String,String> 名前と文字列のMap (実体はHashMap)
	 */
	private Map<String,String> getToken(ArrayList<NameValuePair> nameValuePair) {
		HashMap<String,String> results = new HashMap<String,String>();
		// HttpPostクラスのオブジェクトをURLを指定して作成
		HttpPost httpPost  = new HttpPost("https://accounts.google.com/o/oauth2/token");
		// DefaultHttpClientメソッドでHttpClientを取得し、ヘッダやEntityを設定してexecuteします。
		HttpClient httpClient = new DefaultHttpClient();
		httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			HttpResponse response = httpClient.execute(httpPost);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				// executeの結果のHttpResponseから受信したデータを取得
				BufferedReader br = new BufferedReader(
						new InputStreamReader(
								new BufferedInputStream(
										response.getEntity().getContent())));
				String json = null;
				try{
					StringBuilder sb = new StringBuilder();
					String line;
					while((line=br.readLine()) != null){
						sb.append(line);
					}
					json = sb.toString();
					// 取り出したデータをJSONパーサで解釈して値を取得
					JSONObject rootObject = new JSONObject(json);
					if(rootObject.has(ACCESS_TOKEN)){
						results.put(ACCESS_TOKEN,  rootObject.getString(ACCESS_TOKEN));
					}
					if(rootObject.has(REFRESH_TOKEN)){
						results.put(REFRESH_TOKEN,  rootObject.getString(REFRESH_TOKEN));
					}
					if(rootObject.has(EXPIRES_IN)){
						results.put(EXPIRES_IN,  rootObject.getString(EXPIRES_IN));
					}
				} catch (JSONException e) {
					return null;
				}
			}
		} catch (UnsupportedEncodingException e) {
			return null;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return results;
	}
	/**
	 * AccessTokenを取得する
	 *
	 * @return 成功したらtrue 失敗したら false
	 */
	private boolean getAccessToken(){
		mAccessToken = null;
		mRefreshToken = null;
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		// サーバに渡すパラメータをnameValuePairにセット
		// client_id,client_secretはGoogle APISで取得したもの
		// codeはstartOAuthで取得した認証コード(AuthCode)
		nameValuePair.add(new BasicNameValuePair("client_id",CLIENT_ID));
		nameValuePair.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
		nameValuePair.add(new BasicNameValuePair("redirect_uri","urn:ietf:wg:oauth:2.0:oob"));
		nameValuePair.add(new BasicNameValuePair("grant_type","authorization_code"));
		nameValuePair.add(new BasicNameValuePair("code",mAuthCode));
		// getToken を実行し、token列を取得
		Map<String,String> tokens = getToken(nameValuePair);
		if(tokens != null){
			// tokensに格納されたに格納された値を変数に格納
			mAccessToken = tokens.get(ACCESS_TOKEN);
			mRefreshToken = tokens.get(REFRESH_TOKEN);
			mAccessTokenExpire = Long.valueOf(tokens.get(EXPIRES_IN));
			//プリファレンスの編集をするためにSharedPreferences.Editorオブジェクトを作成
			SharedPreferences.Editor editor = getSharedPreferences(AUTH_INFO, MODE_PRIVATE).edit();
			//ShraedPreferences.Editorに値をセット
			editor.putString(ACCESS_TOKEN, mAccessToken);
			editor.putString(REFRESH_TOKEN, mRefreshToken);
			editor.putLong(ACCESS_TOKEN_EXPIRE,mAccessTokenExpire);
			//プリファレンスを保存
			editor.commit();
			return true;
		}
		return false;
	}
	/**
	 * RefreshTokenを使用してAccessTokenを更新する
	 *
	 * @return 成功したらtrue 失敗したらfalse
	 */
	private boolean refreshToken(){
		mAccessToken = null;
		// サーバに渡すパラメータをnameValuePairにセット
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("client_id",CLIENT_ID));
		nameValuePair.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
		nameValuePair.add(new BasicNameValuePair("refresh_token",mRefreshToken));
		nameValuePair.add(new BasicNameValuePair("grant_type","refresh_token"));
		// getToken を実行し、token列を取得
		Map<String,String> tokens = getToken(nameValuePair);
		if(tokens != null){
			mAccessToken = tokens.get(ACCESS_TOKEN);
			mAccessTokenExpire = Long.valueOf(tokens.get(EXPIRES_IN));
			SharedPreferences.Editor editor = getSharedPreferences(AUTH_INFO, MODE_PRIVATE).edit();
			editor.putString(ACCESS_TOKEN, mAccessToken);
			editor.putLong(ACCESS_TOKEN_EXPIRE,mAccessTokenExpire);
			editor.commit();
			return true;
		}
		return false;
	}

	// GoogleCalendarのFEEDを取得するURL
	// showdeleted=trueをつけることでキャンセルされた（削除された）イベントの情報も取得する
	public static final String CALENDAR_FEED_URL = 
			"https://www.google.com/calendar/feeds/default/private/full?showdeleted=true";
	/**
	 * Googleカレンダーの情報を取得する
	 */
	public void getGoogleCalendar(){
		String updatedQuery = null;
		if(mLastUpdate!=null){
			// 前回更新日時が存在していれば、それをupdated-minにセットし
			// それ以降の更新データのみ取得
			updatedQuery = "&updated-min="+mLastUpdate;
		}else{
			// 一度も更新していなければ、updateQueryは指定しない
			updatedQuery = "";
		}
		// 認証のためにAccessTokenをoauth_tokenとしてURLに追加
		String AuthParam = "&oauth_token="+mAccessToken;
		String nextUrl = CALENDAR_FEED_URL;
		CalendarParser cp = new CalendarParser(mContentResolver);
		// nextUrlがあるかぎり繰り返す
		while(nextUrl != null){
			nextUrl = cp.parse(mCalendarHttpClient.httpGet(nextUrl+AuthParam+updatedQuery));
		}
		mLastUpdate = mUpdateStartTime;
		saveLastUpdate();
	}
	/**
	 * プリファレンスに直前の更新日時を記録
	 */
	public void saveLastUpdate(){
		SharedPreferences.Editor e = getSharedPreferences(AUTH_INFO, MODE_PRIVATE).edit();
		e.putString(LAST_UPDATE, mLastUpdate);
		e.commit();
	}

	/**
	 * DateCellAdapterクラス
	 *  BaseAdapterを継承する。
	 */
	public class DateCellAdapter extends BaseAdapter {
		private static final int NUM_ROWS = 6;
		private static final int NUM_OF_CELLS = DAYS_OF_WEEK*NUM_ROWS;
		private LayoutInflater mLayoutInflater = null;
		/**
		 * コンストラクタではパラメタで受け取ったcontextを使用して
		 * 「LayoutInflater」のインスタンスを作成する。
		 * @param context アクティビティ
		 */
		DateCellAdapter(Context context){
			// getSystemServiceでContextからLayoutInflaterを取得
			mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		/**
		 * getCount
		 * 「NUM_OF_CELLS」 (42)を返す
		 */
		public int getCount() {
			return NUM_OF_CELLS;
		}
		/**
		 * getItem
		 * 必要ないのでnullを返す
		 */
		public Object getItem(int position) {
			return null;
		}
		/**
		 * getItemId
		 * 必要ないので0を返す
		 */
		public long getItemId(int position) {
			return 0;
		}
		/**
		 * getView
		 *  DateCellのViewを作成して返すためのメソッド
		 *  @param int position セルの位置
		 *  @param View convertView 前に使用したView
		 *  @param ViewGroup parent 親ビュー　ここではGridView
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mLayoutInflater.inflate(R.layout.datecell,null);
			}
			// Viewの最小の高さを設定する
			convertView.setMinimumHeight(parent.getHeight()/NUM_ROWS-1);
			TextView dayOfMonthView = (TextView)convertView.findViewById(R.id.dayOfMonth);
			Calendar cal = (Calendar)mCalendar.clone();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.DAY_OF_MONTH, position-cal.get(Calendar.DAY_OF_WEEK)+1);
			dayOfMonthView.setText(""+cal.get(Calendar.DAY_OF_MONTH));
			if(position%7 == 0){
				dayOfMonthView.setBackgroundResource(R.color.red);
			}else if(position%7 == 6){
				dayOfMonthView.setBackgroundResource(R.color.blue);
			}else {
				dayOfMonthView.setBackgroundResource(R.color.gray);
			}
			TextView scheduleView = (TextView)convertView.findViewById(R.id.schedule);
			// Queryパラメータの設定
			String[] projection = {EventInfo.TITLE};
			// 削除フラグのついているデータは検索しない
			String selection = EventInfo.DELETED + " = 0 and " + EventInfo.START_TIME+" LIKE ?";
			String[] selectionArgs = {DateStrCls.dateFormat.format(cal.getTime())+"%"};
			String sortOrder = EventInfo.START_TIME;
			// Queryの実行
			Cursor c = mContentResolver.query(RESOLVER_URI,projection,selection,selectionArgs,sortOrder);
			// 結果の文字列を作成しscheduleViewにセット
			// StringBuilder(追加可能な文字列クラス）
			StringBuilder sb = new StringBuilder();
			while(c.moveToNext()){
				// StringBuilderにスケジュールのタイトルを追加
				sb.append(c.getString(c.getColumnIndex(EventInfo.TITLE)));
				sb.append("\n");
			}
			c.close();
			// scheduleViewに予定のリストを追加
			scheduleView.setText( sb.toString());

			return convertView;		
		}
	}
}