package com.example.calender;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.view.LayoutInflater;

public class MainActivity extends Activity {

	private static final int DAYS_OF_WEEK = 7;			// 1週間の日数
	private GridView mGridView = null;					// GridViewのインスタンス
	private DateCellAdapter mDateCellAdapter = null;	// DateCellAdapterのインスタンス
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGridView = (GridView)findViewById(R.id.gridView1);
        // Gridカラム数を設定
        mGridView.setNumColumns(DAYS_OF_WEEK);
        // DateCellAdapterのインスタンスを作成
        mDateCellAdapter = new DateCellAdapter(this);
        // GridViewに「DataCellAdapter」をセット
        mGridView.setAdapter(mDateCellAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    /**
     *  DateCellAdapterクラス
     */
    public class DateCellAdapter extends BaseAdapter{
    	private static final int NUM_ROWS = 6;							// 行数
    	private static final int NUM_OF_CELLS = DAYS_OF_WEEK*NUM_ROWS;	// セル数
    	private LayoutInflater mLayoutInflater = null;
    	
    	DateCellAdapter(Context context){
    		// getSystemServiceでXmlからViewオブジェクトを取得する。
    		mLayoutInflater = 
    		 (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}
    	public int getCount(){ return NUM_OF_CELLS;}
    	public Object getItem(int arg0){ return null;}
    	public long getItemId(int arg0){ return 0;}
    	public View getView(int position,View convertView,ViewGroup parent){
    		if(convertView == null){
				convertView = mLayoutInflater.inflate(R.layout.datecell,null);
			}
    		//Viewの最小の高さを設定する
    		convertView.setMinimumHeight(parent.getHeight()/NUM_ROWS-1);
    		// 日付の欄に仮に現在のpositionを表示しておく
    		TextView dayOfMonthVeiw = (TextView)convertView.findViewById(R.id.dayOfMonth);
    		dayOfMonthVeiw.setText(""+position);
    		// スケジュール欄にはSchedule+posionと表示する。
    		TextView scheduleView = (TextView)convertView.findViewById(R.id.schedule);
    		scheduleView.setText("Schedule" + position);
  
    		return convertView;
    	}
    }
     
}
