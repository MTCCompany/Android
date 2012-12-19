package com.example.eventcalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * �{�^���̃N���b�N�������s�����߂�
 * OnClickListener���C���v�������g����B
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
		// �{�^����AuthCodeActivity��Listener�Ƃ��ēo�^����B
		mSaveCode.setOnClickListener(this);
		mDiscardCode.setOnClickListener(this);
	}

	/**
	 * �{�^�����N���b�N�����Ƃ��̏���
	 * @param View �N���b�N���ꂽ�r���[
	 */
	public void onClick(View v) {
		Intent intent = new Intent();
		if(v == mDiscardCode){
			// �j�����N���b�N���ꂽ��RESULT_CANCELED��Ԃ��ďI��
			setResult(RESULT_CANCELED,intent);
		}else if(v == mSaveCode){
			// save���N���b�N���ꂽ��resultIntent��AuthCode�̕�������Z�b�g
			intent.putExtra(EventCalendarActivity.AUTH_CODE,
					mAuthCode.getText().toString());
			setResult(RESULT_OK,intent);
		}
		finish();
	}
}
