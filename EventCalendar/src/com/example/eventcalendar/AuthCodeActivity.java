package com.example.eventcalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * ボタンのクリック処理を行うために
 * OnClickListenerをインプリメントする。
 */
public class AuthCodeActivity extends Activity implements OnClickListener {
	private EditText mAuthCode = null;
	private Button mSaveCode = null;
	private Button mDiscardCode = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authcode);
		mAuthCode = (EditText)findViewById(R.id.authCode);
		mSaveCode = (Button)findViewById(R.id.saveCode);
		mDiscardCode = (Button)findViewById(R.id.discardCode);
		// ボタンにAuthCodeActivityをListenerとして登録する。
		mSaveCode.setOnClickListener(this);
		mDiscardCode.setOnClickListener(this);
	}

	/**
	 * ボタンをクリックしたときの処理
	 * @param View クリックされたビュー
	 */
	public void onClick(View v) {
		Intent intent = new Intent();
		if(v == mDiscardCode){
			// 破棄をクリックされたらRESULT_CANCELEDを返して終了
			setResult(RESULT_CANCELED,intent);
		}else if(v == mSaveCode){
			// saveがクリックされたらresultIntentにAuthCodeの文字列をセット
			intent.putExtra(EventCalendarActivity.AUTH_CODE,
					mAuthCode.getText().toString());
			setResult(RESULT_OK,intent);
		}
		finish();
	}
}
