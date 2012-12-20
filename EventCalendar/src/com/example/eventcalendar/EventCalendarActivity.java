package com.example.eventcalendar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
	// ��T�Ԃ̓���
	private static final int DAYS_OF_WEEK = 7;
	// GridView�̃C���X�^���X
	private GridView mGridView = null;
	// DateCellAdapter�̃C���X�^���X
	private DateCellAdapter mDateCellAdapter = null;
	// ���ݒ��ڂ��Ă���N������ێ�����ϐ�
	private GregorianCalendar mCalendar = null;
	// �J�����_�[�̔N����\������TextView
	private TextView mYearMonthTextView = null;
	// ContentResolver�̃C���X�^���X
	private ContentResolver mContentResolver = null;
	// EventProvider��Uri
	public static final Uri RESOLVER_URI = Uri.parse("content://com.example.eventcalendar.eventprovider");
	// EventDetailActivity���Ăяo�����߂�request�R�[�h
	protected static final int EVENT_DETAIL = 2;
	// �O���{�^���̃C���X�^���X
	private Button mPrevMonthButton = null;
	// �����{�^���̃C���X�^���X
	private Button mNextMonthButton = null;
	// Activity�Ńf�[�^�x�[�X���X�V���ꂽ���Ƃ�`���邽�߂̃^�O
	public static final String CHANGED = "changed";
	// �O��X�V�����ׂ̈̃^�O
	public static final String LAST_UPDATE = "LastUpdate";
	// ����̍X�V�J�n����
	private String mUpdateStartTime = null;
	// �O��X�V����
	private String mLastUpdate = null;

	// �F�ؗp�g�[�N����ێ�����ϐ�
	private String mAccessToken;
	private String mRefreshToken;
	private long mAccessTokenExpire;
	// AuthCode��ێ�����ϐ�
	private String mAuthCode;
	// �F�ؗp�g�[�N���̖��O
	private static final String AUTH_INFO = "authInfo";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String ACCESS_TOKEN_EXPIRE = "access_token_expire";

	// �N���C�A���g�V�[�N���b�g�@Google APIS����擾
	public static final String CLIENT_SECRET = "1ET09oOy2CEezagy8lmOZdNe";
	// OAUTH�pURL
	public static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	// �N���C�A���gID�@Google APIS����擾
	public static final String CLIENT_ID = "155205884318.apps.googleusercontent.com";
	// ���_�C���N�gURI�@Google API����擾
	public static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	// �J�����_�[�̃X�R�[�v
	public static final String SCOPE = "https://www.google.com/calendar/feeds/";
	//�@���X�|���X�R�[�h�@�X�^���h�A�����A�v���P�[�V�����Ȃ̂ŁA�R�[�h�Ŏ擾����
	public static final String RESPONSE_CODE = "code";
	// OAUTH�Ɏg��URI�@
	public static final Uri OAUTH_URI = Uri.parse(OAUTH_URL
			+"?client_id="+CLIENT_ID
			+"&redirect_uri="+REDIRECT_URI
			+"&scope="+SCOPE
			+"&response_type="+RESPONSE_CODE);
	// onActivityResult�Ŏg�p����Activity��request�R�[�h�i�u���E�U�N���p�j
	public static final int BROWSER = 1;
	// onActivityResult�Ŏg�p����Activity��request�R�[�h�iAuthCode���͗p�j
	public static final int AUTH_CODE_ACTIVITY = 3;
	// AuthCode���󂯎��ׂ̃^�O
	public static final String AUTH_CODE = "AuthCode";

	// Http�ʐM�p�̃N���X�̃C���X�^���X
	private CalendarHttpClient mCalendarHttpClient = new CalendarHttpClient();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mGridView = (GridView)findViewById(R.id.gridView1);
		// Grid�J��������ݒ肷��
		mGridView.setNumColumns(DAYS_OF_WEEK);
		// DateCellAdapter�̃C���X�^���X���쐬����
		mDateCellAdapter = new DateCellAdapter(this);
		// GridView�ɁuDateCellAdapter�v���Z�b�g
		mGridView.setAdapter(mDateCellAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
				// �J�����_�[���R�s�[
				Calendar cal = (Calendar)mCalendar.clone();
				// position������t���v�Z
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.add(Calendar.DAY_OF_MONTH, position-cal.get(Calendar.DAY_OF_WEEK)+1);
				// ���t������𐶐�
				String dateString = DateStrCls.dateFormat.format(cal.getTime());
				// Intent  ���쐬
				Intent intent = new Intent(EventCalendarActivity.this,EventDetailActivity.class);
				// ���t��Extra�ɃZ�b�g
				intent.putExtra("date", dateString);
				// Activity�����s
				startActivityForResult(intent,EVENT_DETAIL);
			}
		});

		mYearMonthTextView = (TextView)findViewById(R.id.yearMonth);
		// �uGregorianCalendar�v�̃C���X�^���X�̍쐬
		mCalendar = new GregorianCalendar();
		// �N���̎擾
		int year = mCalendar.get(Calendar.YEAR);
		int month = mCalendar.get(Calendar.MONTH)+1;
		// �N���̃r���[�ւ̕\��
		mYearMonthTextView.setText(year+"/"+month);
		// ContentResolver�̎擾
		mContentResolver = getContentResolver();
		// �O���{�^����Listener��ݒ�
		mPrevMonthButton = (Button)findViewById(R.id.prevMonth);
		mPrevMonthButton.setOnClickListener(this);
		// �����{�^����Listener��ݒ�
		mNextMonthButton = (Button)findViewById(R.id.nextMonth);
		mNextMonthButton.setOnClickListener(this);
	}

	/**
	 * onClick
	 *  �O���A�����{�^���ŃN���b�N���ꂽ�Ƃ��Ăяo�����
	 */
	public void onClick(View v) {
		// ���݂̒��ڂ��Ă�����t�𓖌���1���ɕύX����
		mCalendar.set(Calendar.DAY_OF_MONTH,1);
		if(v == mPrevMonthButton){
			// 1�������Z����
			mCalendar.add(Calendar.MONTH, -1);
		}else if(v == mNextMonthButton){
			// 1�������Z����
			mCalendar.add(Calendar.MONTH, 1);
		}
		mYearMonthTextView.setText(mCalendar.get(Calendar.YEAR)+"/"+(mCalendar.get(Calendar.MONTH)+1));
		mDateCellAdapter.notifyDataSetChanged();
	}

	/**
	 * onActivityResult
	 *  �Ăяo����Editor�̏��������������Ƃ��Ăяo�����
	 * @param requestCode �N�����Ɏw�肵��requestCode
	 * @param resultCode �Ăяo����Activity���I�����ɐݒ肵���I���R�[�h
	 * @param data �Ăяo����Activity���I�����ɐݒ肵��Intent
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == EVENT_DETAIL && resultCode == RESULT_OK){
			if(data.getBooleanExtra(EventCalendarActivity.CHANGED,false)){
				// EVENT_DETAIL�̏������ʂ�OK��Changed��true�Ȃ�A�f�[�^�x�[�X�X�V��ʒm
				mDateCellAdapter.notifyDataSetChanged();
				// Widget�̓��e���X�V����ʒm�������Ȃ��B
				Intent serviceIntent = new Intent(this,WidgetService.class);
				startService(serviceIntent);
			}
		}else if(requestCode == BROWSER){
			//�u���E�U���I��������A�R�[�h����͂���A�N�e�B�r�e�B���N������B
			Intent intent = new Intent(EventCalendarActivity.this,AuthCodeActivity.class);
			startActivityForResult(intent,AUTH_CODE_ACTIVITY);
		}else if(requestCode == AUTH_CODE_ACTIVITY){
			if(resultCode == RESULT_OK){
				// AuthCode������I��������Intent����AuthCode���擾
				mAuthCode = data.getStringExtra(AUTH_CODE);
				// AuthCode���������擾�ł����ꍇ�́A�ēxsyncCalendar�����s
				syncCalendar();
			}
		}
	}

	/**
	 * ���j���[�{�^���������ꂽ�Ƃ��̏���
	 *
	 * @param Menu ���݂̃��j���[
	 * @return ���j���[�̐����ɐ���������true
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		// MenuInflater���擾����
		MenuInflater menuInflater = getMenuInflater();
		// MenuInflater���g�p���ă��j���[�����\�[�X����쐬����
		menuInflater.inflate(R.menu.menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ���j���[���I�����ꂽ�Ƃ��̏���
	 *
	 * @param MenuItem �I�����ꂽ���j���[�A�C�e��
	 * @return �������s�����ꍇ��true 
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		if(item.getItemId() == R.id.syncMenu){
			syncCalendar();
			return true;
		}
		return false;
	}
	// �v���O���X�_�C�A���O�̃C���X�^���X
	private ProgressDialog mProgressDialog = null;
	/**
	 * getGoogleCalendar �� updateGoogleCalendar���Ăяo��
	 * �v���O���X�_�C�A���O��\������ׂɕʃX���b�h�Ŏ��s����B
	 */
	public void syncGoogleCalendar(){
		// �v���O���X�_�C�A���O�̍쐬
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(getString(R.string.nowOnSync));
		// �r���Œ�~���邱�Ƃ͂ł��Ȃ�
		mProgressDialog.setCancelable(false);
		// �v���O���X�_�C�A���O�̕\��
		mProgressDialog.show();
		// ���ۂ̏������s���X���b�h���쐬
		Thread thread = new Thread(runSyncGoogleCalendar);
		// �X���b�h�̎��s�J�n
		thread.start();
	}

	// notifyDataSetChanged���󂯎��handler
	private Handler mNotifyHandler = new Handler();
	/**
	 * getGoogleCalendar�̎��s���s���X���b�h
	 */
	private Runnable runSyncGoogleCalendar = new Runnable(){
		public void run() {
			// �X�V�����{��
			getGoogleCalendar();
			updateGoogleCalendar();
			// �C�x���g�f�[�^���X�V���ꂽ�̂ŃA���[�����X�V
			AlarmReceiver.updateAlarm(EventCalendarActivity.this);
			// Widget�̓��e���X�V����ʒm���s��
			Intent serviceIntent = new Intent(EventCalendarActivity.this,WidgetService.class);
			startService(serviceIntent);
			// �X�V���I�������v���O���X�_�C�A���O������
			mProgressDialog.dismiss();
			// notifyhandler��notifyDataSetChanged�̏������|�X�g
			mNotifyHandler.post(new Runnable(){
				public void run(){
					// mNotifyHandler�������Ă���v���Z�X�Ŏ��s�����B
					mDateCellAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	/**
	 * Google�J�����_�[�Ɠ�������
	 */
	public void syncCalendar(){
		// �X�V�J�n������ۑ�
		mUpdateStartTime = DateStrCls.toUTCString(new GregorianCalendar());
		// �v���t�@�����X�m�F�̂��߂�sharedPreferences���擾
		SharedPreferences sharedPreferences  = getSharedPreferences(AUTH_INFO, MODE_PRIVATE);
		// �������̓f�t�H���g�l�F�w�肵�����O�̃f�[�^�����������Ƃ��ɕԂ��Ă���l
		mAccessToken  = sharedPreferences.getString(ACCESS_TOKEN, null);
		mRefreshToken  = sharedPreferences.getString(REFRESH_TOKEN,null);
		mAccessTokenExpire = sharedPreferences.getLong(ACCESS_TOKEN_EXPIRE, 0);
		// �O��X�V�������擾
		mLastUpdate = sharedPreferences.getString(LAST_UPDATE,null);
		if(mRefreshToken == null){
			// RefreshToken �������̂ŁA�F�؏������J�n
			if(mAuthCode == null || mAuthCode.equals("")){
				// AuthCode���Ȃ����startOAuth�����s
				startOAuth();
			}else{
				// AuthCode������΁A���ł�Web�ł�Auth�����͍s���Ă���̂ŁAAccessToken���擾
				if(getAccessToken()){
					syncGoogleCalendar();
				}
			}
		}else if(mAccessTokenExpire<Calendar.getInstance().getTimeInMillis()){
			// AccessToken �������؂�Ȃ̂�AccessToken���Ď擾���Ă���AgetGoogleCalendar�����s
			if(refreshToken()){
				syncGoogleCalendar();

			}
		}else{
			// AccessToken���L���Ȃ̂ŁA���̂܂�getGoogleCalendar()�����s
			syncGoogleCalendar();
		}
	}
	// �J�����_�[API�ɓ����Feed�ł͂Ȃ��f�[�^�ɃA�N�Z�X���邽�߂�URL
	public static final String DEFAULT_URL = "https://www.google.com/calendar/feeds/default/private/full";
	/**
	 * Google�J�����_�[�Ƀf�[�^���A�b�v���[�h����
	 */
	public void updateGoogleCalendar(){
		// MODIFIED�t���O�̗����Ă���f�[�^������
		String selection = EventInfo.MODIFIED + " = 1";
		Cursor c = mContentResolver.query(RESOLVER_URI, null, selection, null, null);

		// insert/update/delete���ꂼ��̃f�[�^�̃��X�g
		ArrayList<EventInfo> insertEvents = new ArrayList<EventInfo>();
		ArrayList<EventInfo> updateEvents = new ArrayList<EventInfo>();
		ArrayList<EventInfo> deleteEvents = new ArrayList<EventInfo>();

		while(c.moveToNext()){
			EventInfo eventInfo = new EventInfo(mContentResolver);
			eventInfo.setValues(c);
			if(eventInfo.getEventId() == null){
				// eventId���ݒ肳��Ă��Ȃ��f�[�^�́AGoogle�J�����_�[�ɂ͂Ȃ��V�K�f�[�^�Ȃ̂�
				// insert���X�g�ɓo�^
				insertEvents.add(eventInfo);
			}else if(eventInfo.getDeleted() == 1){
				// �폜�t���O�������Ă���̂�delte���X�g�ɓo�^
				deleteEvents.add(eventInfo);
			}else {
				// ����ȊO��update���X�g�ɓo�^
				updateEvents.add(eventInfo);
			}
		}
		c.close();
		// �p�[�T���쐬
		CalendarParser cp = new CalendarParser(mContentResolver);
		// insert����
		for(EventInfo e : insertEvents){
			// insert�p�̃f�[�^���쐬��POST���Č��ʂ��擾
			// ���ʂ�parse���ĐV�����f�[�^�Ƃ��ēo�^
			cp.parse(mCalendarHttpClient.httpPost(DEFAULT_URL+"?oauth_token="+mAccessToken,cp.insertSerializer(e)));
			if(mCalendarHttpClient.mHttpSucceeded){
				// �Â��f�[�^���폜
				selection = EventInfo.ID + " = " + e.getId();
				mContentResolver.delete(RESOLVER_URI, selection, null);
			}
		}
		// update����
		for(EventInfo e : updateEvents){
			// �ҏW�pURL���擾
			String url = e.getEditUrl()+"?oauth_token="+mAccessToken;
			// �A�b�v�f�[�g���ʂ��p�[�X���Č��݂̃f�[�^���X�V
			cp.parse(mCalendarHttpClient.httpPut(url,cp.updateSerializer(mCalendarHttpClient.httpGet(url),e)));
		}
		// delete����
		for(EventInfo e : deleteEvents){
			// �ҏW�pURL���g���āAhttpDelete�̏���
			mCalendarHttpClient.httpDelete(e.getEditUrl()+"?oauth_token="+mAccessToken);
			if(mCalendarHttpClient.mHttpSucceeded){
				// �f�[�^�x�[�X������폜
				selection = EventInfo.ID + " = " + e.getId();
				mContentResolver.delete(RESOLVER_URI, selection, null);
			}
		}
	}


	/**
	 * startOAuth
	 * OAuth 2.0�ɏ]�����F�؏����ׂ̈Ƀu���E�U���g�p����
	 * Google�̔F�؃y�[�W���J��
	 */
	private void startOAuth(){
		// URI���w�肵�ău���E�U���N������ׂ�Intent���쐬����B
		// �ÖٓI�C���e���g�Ȃ̂ŁA���[�U�̑I�������u���E�U��URI���J��
		Intent intent = new Intent(Intent.ACTION_VIEW, OAUTH_URI);
		startActivityForResult(intent,BROWSER);
	}

	/**
	 * �T�[�o����Token���擾����
	 *
	 * @param ArrayList<NameValuePair> ���O�ƒl�̃y�A�̃��X�g
	 * @return Map<String,String> ���O�ƕ������Map (���̂�HashMap)
	 */
	private Map<String,String> getToken(ArrayList<NameValuePair> nameValuePair) {
		HashMap<String,String> results = new HashMap<String,String>();
		// HttpPost�N���X�̃I�u�W�F�N�g��URL���w�肵�č쐬
		HttpPost httpPost  = new HttpPost("https://accounts.google.com/o/oauth2/token");
		// DefaultHttpClient���\�b�h��HttpClient���擾���A�w�b�_��Entity��ݒ肵��execute���܂��B
		HttpClient httpClient = new DefaultHttpClient();
		httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			HttpResponse response = httpClient.execute(httpPost);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				// execute�̌��ʂ�HttpResponse�����M�����f�[�^���擾
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
					// ���o�����f�[�^��JSON�p�[�T�ŉ��߂��Ēl���擾
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
	 * AccessToken���擾����
	 *
	 * @return ����������true ���s������ false
	 */
	private boolean getAccessToken(){
		mAccessToken = null;
		mRefreshToken = null;
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		// �T�[�o�ɓn���p�����[�^��nameValuePair�ɃZ�b�g
		// client_id,client_secret��Google APIS�Ŏ擾��������
		// code��startOAuth�Ŏ擾�����F�؃R�[�h(AuthCode)
		nameValuePair.add(new BasicNameValuePair("client_id",CLIENT_ID));
		nameValuePair.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
		nameValuePair.add(new BasicNameValuePair("redirect_uri","urn:ietf:wg:oauth:2.0:oob"));
		nameValuePair.add(new BasicNameValuePair("grant_type","authorization_code"));
		nameValuePair.add(new BasicNameValuePair("code",mAuthCode));
		// getToken �����s���Atoken����擾
		Map<String,String> tokens = getToken(nameValuePair);
		if(tokens != null){
			// tokens�Ɋi�[���ꂽ�Ɋi�[���ꂽ�l��ϐ��Ɋi�[
			mAccessToken = tokens.get(ACCESS_TOKEN);
			mRefreshToken = tokens.get(REFRESH_TOKEN);
			mAccessTokenExpire = Long.valueOf(tokens.get(EXPIRES_IN));
			//�v���t�@�����X�̕ҏW�����邽�߂�SharedPreferences.Editor�I�u�W�F�N�g���쐬
			SharedPreferences.Editor editor = getSharedPreferences(AUTH_INFO, MODE_PRIVATE).edit();
			//ShraedPreferences.Editor�ɒl���Z�b�g
			editor.putString(ACCESS_TOKEN, mAccessToken);
			editor.putString(REFRESH_TOKEN, mRefreshToken);
			editor.putLong(ACCESS_TOKEN_EXPIRE,mAccessTokenExpire);
			//�v���t�@�����X��ۑ�
			editor.commit();
			return true;
		}
		return false;
	}
	/**
	 * RefreshToken���g�p����AccessToken���X�V����
	 *
	 * @return ����������true ���s������false
	 */
	private boolean refreshToken(){
		mAccessToken = null;
		// �T�[�o�ɓn���p�����[�^��nameValuePair�ɃZ�b�g
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("client_id",CLIENT_ID));
		nameValuePair.add(new BasicNameValuePair("client_secret",CLIENT_SECRET));
		nameValuePair.add(new BasicNameValuePair("refresh_token",mRefreshToken));
		nameValuePair.add(new BasicNameValuePair("grant_type","refresh_token"));
		// getToken �����s���Atoken����擾
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

	// GoogleCalendar��FEED���擾����URL
	// showdeleted=true�����邱�ƂŃL�����Z�����ꂽ�i�폜���ꂽ�j�C�x���g�̏����擾����
	public static final String CALENDAR_FEED_URL = 
			"https://www.google.com/calendar/feeds/default/private/full?showdeleted=true";
	/**
	 * Google�J�����_�[�̏����擾����
	 */
	public void getGoogleCalendar(){
		String updatedQuery = null;
		if(mLastUpdate!=null){
			// �O��X�V���������݂��Ă���΁A�����updated-min�ɃZ�b�g��
			// ����ȍ~�̍X�V�f�[�^�̂ݎ擾
			updatedQuery = "&updated-min="+mLastUpdate;
		}else{
			// ��x���X�V���Ă��Ȃ���΁AupdateQuery�͎w�肵�Ȃ�
			updatedQuery = "";
		}
		// �F�؂̂��߂�AccessToken��oauth_token�Ƃ���URL�ɒǉ�
		String AuthParam = "&oauth_token="+mAccessToken;
		String nextUrl = CALENDAR_FEED_URL;
		CalendarParser cp = new CalendarParser(mContentResolver);
		// nextUrl�����邩����J��Ԃ�
		while(nextUrl != null){
			nextUrl = cp.parse(mCalendarHttpClient.httpGet(nextUrl+AuthParam+updatedQuery));
		}
		mLastUpdate = mUpdateStartTime;
		saveLastUpdate();
	}
	/**
	 * �v���t�@�����X�ɒ��O�̍X�V�������L�^
	 */
	public void saveLastUpdate(){
		SharedPreferences.Editor e = getSharedPreferences(AUTH_INFO, MODE_PRIVATE).edit();
		e.putString(LAST_UPDATE, mLastUpdate);
		e.commit();
	}

	/**
	 * DateCellAdapter�N���X
	 *  BaseAdapter���p������B
	 */
	public class DateCellAdapter extends BaseAdapter {
		private static final int NUM_ROWS = 6;
		private static final int NUM_OF_CELLS = DAYS_OF_WEEK*NUM_ROWS;
		private LayoutInflater mLayoutInflater = null;
		/**
		 * �R���X�g���N�^�ł̓p�����^�Ŏ󂯎����context���g�p����
		 * �uLayoutInflater�v�̃C���X�^���X���쐬����B
		 * @param context �A�N�e�B�r�e�B
		 */
		DateCellAdapter(Context context){
			// getSystemService��Context����LayoutInflater���擾
			mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		/**
		 * getCount
		 * �uNUM_OF_CELLS�v (42)��Ԃ�
		 */
		public int getCount() {
			return NUM_OF_CELLS;
		}
		/**
		 * getItem
		 * �K�v�Ȃ��̂�null��Ԃ�
		 */
		public Object getItem(int position) {
			return null;
		}
		/**
		 * getItemId
		 * �K�v�Ȃ��̂�0��Ԃ�
		 */
		public long getItemId(int position) {
			return 0;
		}
		/**
		 * getView
		 *  DateCell��View���쐬���ĕԂ����߂̃��\�b�h
		 *  @param int position �Z���̈ʒu
		 *  @param View convertView �O�Ɏg�p����View
		 *  @param ViewGroup parent �e�r���[�@�����ł�GridView
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mLayoutInflater.inflate(R.layout.datecell,null);
			}
			// View�̍ŏ��̍�����ݒ肷��
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
			// Query�p�����[�^�̐ݒ�
			String[] projection = {EventInfo.TITLE};
			// �폜�t���O�̂��Ă���f�[�^�͌������Ȃ�
			String selection = EventInfo.DELETED + " = 0 and " + EventInfo.START_TIME+" LIKE ?";
			String[] selectionArgs = {DateStrCls.dateFormat.format(cal.getTime())+"%"};
			String sortOrder = EventInfo.START_TIME;
			// Query�̎��s
			Cursor c = mContentResolver.query(RESOLVER_URI,projection,selection,selectionArgs,sortOrder);
			// ���ʂ̕�������쐬��scheduleView�ɃZ�b�g
			// StringBuilder(�ǉ��\�ȕ�����N���X�j
			StringBuilder sb = new StringBuilder();
			while(c.moveToNext()){
				// StringBuilder�ɃX�P�W���[���̃^�C�g����ǉ�
				sb.append(c.getString(c.getColumnIndex(EventInfo.TITLE)));
				sb.append("\n");
			}
			c.close();
			// scheduleView�ɗ\��̃��X�g��ǉ�
			scheduleView.setText( sb.toString());

			return convertView;		
		}
	}
}