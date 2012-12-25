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
// バイブレータをシステムサービスから取得する
    mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
// 音声リソースを指定してMediaPlayerを作成する
    mPlayer  = MediaPlayer.create(this, R.raw.alarm);
// 音はループするように設定する。
    mPlayer.setLooping(true);
  }
  @Override
  public void onStart(Intent intent,int startID){
// バイブレータのパターン
// 振動する時間、止まっている時間を交互にミリ秒で指定
// 以下の例では、1秒振動して、2秒停止を5回繰り返す
    long[] pattern = {1000,2000,1000,2000,1000,2000,1000,2000,1000,2000};
// バイブレータを実行
    mVibrator.vibrate(pattern,-1);
// 音声の再生を開始
    mPlayer.seekTo(0);
    mPlayer.start();
  }
  @Override
  public void onDestroy(){
// 終了するときは音声を停止し、MediaPlayer をリリース
    mPlayer.stop();
    mPlayer.release();
  }
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

}
