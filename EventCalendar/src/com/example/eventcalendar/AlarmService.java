package com.example.eventcalendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;

public class AlarmService extends Service {
  private Vibrator mVibrator = null;
  private MediaPlayer mPlayer = null;
  @Override
  public void onCreate(){
// �o�C�u���[�^���V�X�e���T�[�r�X����擾����
    mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
// �������\�[�X���w�肵��MediaPlayer���쐬����
    mPlayer  = MediaPlayer.create(this, R.raw.alarm);
// ���̓��[�v����悤�ɐݒ肷��B
    mPlayer.setLooping(true);
  }
  @Override
  public void onStart(Intent intent,int startID){
// �o�C�u���[�^�̃p�^�[��
// �U�����鎞�ԁA�~�܂��Ă��鎞�Ԃ����݂Ƀ~���b�Ŏw��
// �ȉ��̗�ł́A1�b�U�����āA2�b��~��5��J��Ԃ�
    long[] pattern = {1000,2000,1000,2000,1000,2000,1000,2000,1000,2000};
// �o�C�u���[�^�����s
    mVibrator.vibrate(pattern,-1);
// �����̍Đ����J�n
    mPlayer.seekTo(0);
    mPlayer.start();
  }
  @Override
  public void onDestroy(){
// �I������Ƃ��͉������~���AMediaPlayer �������[�X
    mPlayer.stop();
    mPlayer.release();
  }
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

}
