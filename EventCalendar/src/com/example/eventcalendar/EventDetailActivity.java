package com.example.eventcalendar;

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
 *  1�����̃C�x���g�̏ڍׂ�\������
 */
public class EventDetailActivity extends Activity  implements OnItemLongClickListener {
	// ���t�̕�����
	private String mDateString = null;
	// �C�x���g�G�f�B�^�N���̂��߂�RequestCode
	public static final int EVENT_EDITOR = 2;
	// EventListView�̃C���X�^���X
	private ListView mEventListView = null;
	// �V�K�C�x���g�ǉ����j���[�pID
	private static final int NEW_EVENT_MENU_ID = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventdetail);
		// �Ăяo�����Ƃ��瑗��ꂽIntent���擾
		Intent intent = getIntent();
		// Intent�́@Extra������t��������擾
		mDateString  = intent.getStringExtra("date");
		// dateView�ɓ��t���Z�b�g
		TextView dateView = (TextView)findViewById(R.id.detailDate);
		dateView.setText(mDateString);
		mEventListView = (ListView)findViewById(R.id.eventList);
		// �usetListAdapter�v���\�b�h���쐬����
		setListAdapter();
		// eventListView�̃A�C�e�����N���b�N���ꂽ���̏������Z�b�g
		mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
				// Intent���쐬
				Intent intent = new Intent(EventDetailActivity.this,EventEditorActivity.class);
				// ArrayAdapter�̌^���uEventInfo�v�N���X�ɂ܂Ƃ߂�
				EventInfo event = (EventInfo)parent.getAdapter().getItem(position);
				// ID�Ɠ��t�̕������Extra�ɃZ�b�g
				intent.putExtra(EventInfo.ID, event.getId());
				intent.putExtra("date",mDateString);
				// EventEditorActivity���N��
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
	 *  ���t���w�肵��Event�̏ڍׂ�Array��Ԃ�
	 * @param date ���t������
	 * @return ArrayList<EventInfo> EventInfo��Array
	 */
	private ArrayList<EventInfo> getEventDetail(String date){
		ArrayList<EventInfo> events = new ArrayList<EventInfo>();
		// ���t���w�肵�ď����擾
		ContentResolver contentResolver = getContentResolver();
		// �폜�t���O�̂��Ă���f�[�^�͌������Ȃ�
	    String selection = EventInfo.DELETED + " = 0 and "+EventInfo.START_TIME+" LIKE ?";
		String[] selectionArgs = {date+"%"};
		String sortOrder = EventInfo.START_TIME;
		Cursor c = contentResolver.query(EventCalendarActivity.RESOLVER_URI, null, selection, selectionArgs, sortOrder);
		while(c.moveToNext()){
			// �����i�[���邽�߂�EventInfo�̃C���X�^���X���쐬
			EventInfo event = new EventInfo(contentResolver);
			// �����i�[����
			event.setId(c.getLong(c.getColumnIndex(EventInfo.ID)));
			event.setTitle(c.getString(c.getColumnIndex(EventInfo.TITLE)));
			event.setStart(c.getString(c.getColumnIndex(EventInfo.START_TIME)));
			event.setEnd(c.getString(c.getColumnIndex(EventInfo.END_TIME)));
			event.setWhere(c.getString(c.getColumnIndex(EventInfo.WHERE)));
			event.setContent(c.getString(c.getColumnIndex(EventInfo.CONTENT)));
			// Array�ɒǉ�
			events.add(event);
		}
		c.close();
		return events;
	}
	/**
	 * onActivityResult
	 *  �Ăяo����Editor�̏��������������Ƃ��Ăяo�����
	 * @param requestCode �N�����Ɏw�肵��requestCode
	 * @param resultCode �Ăяo����Activity���I�����ɐݒ肵���I���R�[�h
	 * @param data �Ăяo����Activity���I�����ɐݒ肵��Intent
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(requestCode == EVENT_EDITOR && resultCode == RESULT_OK){
			if(data.getBooleanExtra(EventCalendarActivity.CHANGED,false)){
				// �A�N�e�B�r�e�B����CHANGED��true���Ԃ��Ă��Ă�����listAdapter���X�V����
				setListAdapter();
				Intent intent = new Intent();
				intent.putExtra(EventCalendarActivity.CHANGED, true);
				// EventCalendarActivity �ł��X�V���K�v�Ȃ̂�
				// EventDetailActivity�̌��ʂƂ��ē��l�̒l���Z�b�g����
				setResult(RESULT_OK,intent);
			}
		}
	}

	/**
	 * ���j���[�{�^���������ꂽ�Ƃ��Ăяo����郁�\�b�h
	 *  ���j���[�A�C�e������������
	 */
	public boolean onCreateOptionsMenu (Menu menu){
		// �A�C�e����ǉ�
		menu.add(Menu.NONE,NEW_EVENT_MENU_ID,Menu.NONE,R.string.newEvent);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ���j���[���J���āA�I�����ꂽ�Ƃ��Ăяo����郁�\�b�h
	 */
	public boolean onOptionsItemSelected (MenuItem item){
		if(item.getItemId() == NEW_EVENT_MENU_ID){
			// ���j���[��ID����v������
			// ID=0��EventEditorActivity���N��
			Intent intent = new Intent(EventDetailActivity.this,EventEditorActivity.class);
			intent.putExtra(EventInfo.ID, 0);
			intent.putExtra("date", mDateString);
			startActivityForResult(intent,EVENT_EDITOR);
		}
		return true;
	}
	// �폜���ׂ����R�[�h��Id
	private long mDeleteId = 0;
	/**
	 * ListView��Item�Œ��������ꂽ�Ƃ��ɌĂяo����郊�X�i�[
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		EventInfo event = (EventInfo)parent.getAdapter().getItem(position);
		mDeleteId = event.getId();
		// �uAlertDialog.Builder�v���쐬����
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// ���\�[�X����^�C�g���������ݒ�
		alertDialogBuilder.setTitle(R.string.deleteConfirm);
		// OK�{�^���i����͍폜�{�^���j�̕�����Ə����֐��̐ݒ�
		alertDialogBuilder.setPositiveButton(R.string.deleteOK, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				ContentResolver contentResolver = getContentResolver();
				String selection = EventInfo.ID+" = "+mDeleteId;
				// Deleted�t���O��Modified�t���O���Z�b�g��Update
				// Delete�͍s��Ȃ�
				ContentValues cv = new ContentValues();
				cv.put(EventInfo.DELETED, 1);
				cv.put(EventInfo.MODIFIED, 1);
				contentResolver.update(EventCalendarActivity.RESOLVER_URI, cv, selection, null);
				// listAdapter ���X�V����
				setListAdapter();
				Intent intent = new Intent();
				intent.putExtra(EventInfo.CHANGED, true);
				setResult(RESULT_OK,intent);
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// Cancel���N���b�N���ꂽ�牽�����Ȃ�
			}
		});
		// �_�C�A���O���L�����Z���ł���悤�ɂ���
		alertDialogBuilder.setCancelable(true);
		// �_�C�A���O�𐶐�����
		AlertDialog alertDialog = alertDialogBuilder.create();
		// �_�C�A���O�{�b�N�X�̕\��
		alertDialog.show();
		return true;
	}

}
