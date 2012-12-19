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
 * �C�x���g�̏����C�����邽�߂̉��
 * �{�^���̏��������邽�߂ɁuonCliciListener�v���C���v�������g����
 */
public class EventEditorActivity extends Activity implements OnClickListener {
	// View�̃C���X�^���X
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
	// Intent�ł�������f�[�^�x�[�XID
	private long mId = 0;
	// ���t�̕�����
	private String mDateString = null;  

	/**
	 * onCreate
	 * ID���O�Ȃ�V�K�A�P�ȏ�Ȃ�f�[�^�x�[�X��������擾���i�t�B�[���h�ɃZ�b�g����
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���\�[�X����View���쐬����
		setContentView(R.layout.eventeditor);
		// TextEdit�Ȃǂ̃r���[���擾����
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

		// �uOnClickListener�v�ɁuEventEditorActivity�v���Z�b�g����
		mDiscardButton.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		Intent intent = getIntent();
		// �C���e���g��Extra����f�[�^��ID���擾����
		mId  = intent.getLongExtra(EventInfo.ID,0);
		// �C���e���g��Extra������t���擾����
		mDateString = intent.getStringExtra("date");
		if(mId==0){
			// �^�b�v�������t�ō��̎�������̗\��Ƃ��ăf�[�^���쐬����
			//�@�����ł���������t���J�����_�[�ɕϊ�
			Calendar targetCal = CommonCls.toCalendar(mDateString);
			// ���̎������擾
			Calendar nowCal = new GregorianCalendar();
			// �J�n���̓^�b�v�������t
			mStartDateTextView.setText(CommonCls.dateFormat.format(targetCal.getTime()));
			// �J�n�����͍��T����
			mStartTimeTextView.setText(CommonCls.timeFormat.format(nowCal.getTime()));
			// �������P���ԉ��Z
			nowCal.add(Calendar.HOUR, 1);
			// �I�����͊J�n���Ɠ���
			mEndDateTextView.setText(CommonCls.dateFormat.format(targetCal.getTime()));
			//�@�I�������͊J�n����P���Ԍ�
			mEndTimeTextView.setText(CommonCls.timeFormat.format(nowCal.getTime()));
		}else{
			// �f�[�^�x�[�X����f�[�^���擾���A�f�[�^�̓��e��ҏW�G���A�ɐݒ肷��
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
						// �J�n������00:00�ŏI����������00:00�̏ꍇ
						// �I���̗\��Ɣ��f����
						mStartTimeTextView.setVisibility(View.INVISIBLE);
						mEndDateTextView.setVisibility(View.INVISIBLE);
						mEndTimeTextView.setVisibility(View.INVISIBLE);
						mAllDayCheckBox.setChecked(true);
					}
				}
			}
			c.close();
		}
		// �����̕ҏW�p�̃��X�i�[������̃e�L�X�g�ɃZ�b�g
		mStartDateTextView.setOnClickListener(new DateOnClickListener(this));
		mEndDateTextView.setOnClickListener(new DateOnClickListener(this));
		mStartTimeTextView.setOnClickListener(new TimeOnClickListener(this));
		mEndTimeTextView.setOnClickListener(new TimeOnClickListener(this));
		// AllDay�`�F�b�N�{�b�N�X�Ƀ��X�i�[���Z�b�g
		mAllDayCheckBox.setOnClickListener(new AllDayOnClickListener());
	}

	/**
	 * onClick
	 *  �{�^���̂ǂꂩ���^�b�v���ꂽ�Ƃ��̏���
	 */
	public void onClick(View v) {
		if(v == mDiscardButton){
			// Discard�{�^�����^�b�v���ꂽ�牽�������A�N�e�B�r�e�B���I������
			Log.d("CALENDAR","Discard");
			finish();
		}else if(v == mSaveButton){
			// Save�{�^�����^�b�v���ꂽ��ҏW���̃f�[�^���f�[�^�x�[�X�ɕۑ�����
			ContentResolver contentResolver = getContentResolver();
			ContentValues values = new ContentValues();
			values.put(EventInfo.TITLE, mTitleEditText.getText().toString());
			values.put(EventInfo.WHERE, mWhereEditText.getText().toString());
			values.put(EventInfo.CONTENT, mContentEditText.getText().toString());
			values.put(EventInfo.UPDATED,CommonCls.toDBDateString(new GregorianCalendar()));
			values.put(EventInfo.MODIFIED, 1);
			values.put(EventInfo.DELETED,0);
			if(mAllDayCheckBox.isChecked()){
				// �I�����ݒ肳��Ă�����@�I�����͗����A�����͂Ƃ���00:00�ɂ���
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
				//�@ID���O�Ȃ�V�K�Ȃ̂�Insert
				contentResolver.insert(EventCalendarActivity.RESOLVER_URI, values);
				Log.d("CALENDAR","Insert:"+mId);
			}else{
				// ID���P�ȏ�Ȃ�X�V�Ȃ̂�Update
				String where = EventInfo.ID+" = "+mId;
				contentResolver.update(EventCalendarActivity.RESOLVER_URI, values, where, null);
				Log.d("CALENDAR","Update: "+mId);
			}
			// �Ăяo�����Ƃɒl��Ԃ����߂�Intent���쐬
			Intent intent = new Intent();
			// Extra�ɒl���Z�b�g
			intent.putExtra(EventCalendarActivity.CHANGED,true);
			// �������ʂ��Z�b�g
			setResult(RESULT_OK,intent);

			// �ۑ�������������A�N�e�B�r�e�B���I������
			finish();
		}
	}
	
	/**
	 * DateOnClickListener
	 *  ���t�̕�����ɃZ�b�g����郊�X�i�[
	 */
	private class DateOnClickListener implements OnClickListener{
		private Context mContext = null;
		public DateOnClickListener(Context c){
			// Context���K�v�Ȃ̂ŁA�R���X�g���N�^�œn���Ċo���Ă���
			mContext = c;
		}
		/**
		 * �N���b�N���ꂽ���Ăяo�����
		 * @param View �N���b�N���ꂽ�r���[
		 */
		public void onClick(View v) {
			GregorianCalendar c = null;
			if(v == mStartDateTextView){
				//�@�J�n���ŃN���b�N���ꂽ�ꍇ
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mStartDateTextView.getText().toString(),
								mStartTimeTextView.getText().toString()));
			}else if(v == mEndDateTextView){
				// �I�����ŃN���b�N���ꂽ�ꍇ
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mEndDateTextView.getText().toString(),
								mEndTimeTextView.getText().toString()));
			}else{
				return;
			}
			// DatePickerDialog���쐬���\������
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
	 *  DatePickerDialog�ɃZ�b�g����A�ݒ莞�ɌĂяo����郊�X�i�[
	 */
	private class DateSetListener implements OnDateSetListener{
		private View mView = null;
		public DateSetListener(View v){
			// �ǂ̃r���[���N���b�N����Ă��J���ꂽ�_�C�A���O�����o���Ă������߂�
			// �R���X�g���N�^��View��ێ����Ă���
			mView = v;
		}
		/**
		 * DatePickerDialog�Őݒ肪�����ꂽ�Ƃ��Ăяo����郁�\�b�h
		 * 
		 * @param int y �N
		 * @param int m ��
		 * @param int d ��
		 */
		public void onDateSet(DatePicker picker, int y, int m, int d) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(y,m,d);
			// �����œn���ꂽ�N�������Y������View�ɃZ�b�g����
			if(mView == mStartDateTextView){
				mStartDateTextView.setText(CommonCls.dateFormat.format(c.getTime()));
			}else if(mView == mEndDateTextView){
				mEndDateTextView.setText(CommonCls.dateFormat.format(c.getTime()));
			}
		}
	}

	/**
	 * TimeOnClickListener
	 *  �����̕�����ɃZ�b�g����郊�X�i�[
	 */
	private class TimeOnClickListener implements OnClickListener{
		private Context mContext = null;
		public TimeOnClickListener(Context c){
			// Context���K�v�Ȃ̂ŁA�R���X�g���N�^�œn���Ċo���Ă���
			mContext = c;
		}

		/**
		 * �N���b�N���ꂽ���Ăяo�����
		 * @param View �N���b�N���ꂽ�r���[
		 */
		public void onClick(View v) {
			GregorianCalendar c = null;
			if(v == mStartTimeTextView){
				//�@�J�n�����ŃN���b�N���ꂽ�ꍇ
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mStartDateTextView.getText().toString(),
								mStartTimeTextView.getText().toString()));
			}else if(v == mEndTimeTextView){
				//�@�I�������ŃN���b�N���ꂽ�ꍇ
				c = CommonCls.toCalendar(
						CommonCls.toDBDateString(
								mEndDateTextView.getText().toString(),
								mEndTimeTextView.getText().toString()));
			}else{
				return;
			}
			// TimePickerDialog���쐬���\������
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
	 *  TimePickerDialog�ɃZ�b�g����A�ݒ莞�ɌĂяo����郊�X�i�[
	 */
	private class TimeSetListener implements OnTimeSetListener{
		private View mView = null;
		public TimeSetListener(View v){
			// �ǂ̃r���[���N���b�N����ĊJ���ꂽ�_�C�A���O�����o���Ă������߂�
			// �R���X�g���N�^��View��ێ����Ă���
			mView = v;
		}

		/**
		 * TimePickerDialog�Őݒ肪�����ꂽ�Ƃ��Ăяo����郁�\�b�h
		 * 
		 * @param int h ��
		 * @param int m ��
		 */
		public void onTimeSet(TimePicker picker, int h, int m) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY,h);
			c.set(Calendar.MINUTE,m);
			// �����œn���ꂽ�N�������Y������View�ɃZ�b�g����
			if(mView == mStartTimeTextView){
				mStartTimeTextView.setText(CommonCls.timeFormat.format(c.getTime()));
			}else if(mView == mEndTimeTextView){
				mEndTimeTextView.setText(CommonCls.timeFormat.format(c.getTime()));
			}
		}
	}
	/**
	 * AllDay�`�F�b�N�{�b�N�X�̃��X�i�[
	 */
	private class AllDayOnClickListener implements OnClickListener{
		/**
		 * AllDay�`�F�b�N�{�b�N�X���N���b�N���ꂽ�Ƃ��Ăяo�����
		 */
		public void onClick(View v) {
			if(((CheckBox)v).isChecked()){
				// AllDay ON�̎�
				// �J�n���ȊO�̃e�L�X�g���B��
				mStartTimeTextView.setVisibility(View.INVISIBLE);
				mEndDateTextView.setVisibility(View.INVISIBLE);
				mEndTimeTextView.setVisibility(View.INVISIBLE);
			}else{
				// AllDay OFF�̎�
				// �J�n���ȊO�̃e�L�X�g���\������
				mStartTimeTextView.setVisibility(View.VISIBLE);
				mEndDateTextView.setVisibility(View.VISIBLE);
				mEndTimeTextView.setVisibility(View.VISIBLE);
			}
		}
	}

}
