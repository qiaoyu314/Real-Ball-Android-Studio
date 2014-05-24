package com.realBall;

import com.realBall.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
/**
 * Activity for selecting levels.
 * User can select the level based on the highest level he has reached
 * @author Yu, Marty and Lingchen
 *
 */
public class SelectLevel extends Activity implements OnClickListener{
	
	private final int INVISIBLE = 4;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_level);
		
		ImageButton level1 = (ImageButton) findViewById(R.id.imn1);
		ImageButton level2 = (ImageButton) findViewById(R.id.imn2);
		ImageButton level2Lock = (ImageButton) findViewById(R.id.imn2lock);
		ImageButton level3 = (ImageButton) findViewById(R.id.imn3);
		ImageButton level3Lock = (ImageButton) findViewById(R.id.imn3lock);
		ImageButton level4 = (ImageButton) findViewById(R.id.imn4);
		ImageButton level4Lock = (ImageButton) findViewById(R.id.imn4lock);
		ImageButton level5 = (ImageButton) findViewById(R.id.imn5);
		ImageButton level5Lock = (ImageButton) findViewById(R.id.imn5lock);
		ImageButton level6 = (ImageButton) findViewById(R.id.imn6);
		ImageButton level6Lock = (ImageButton) findViewById(R.id.imn6lock);
		
		//set the level's visibility according to the level information of the user
		switch (Login.nowLevel){
		//if user's level is 1. He can only see the icon of level one. Other levels are locked
		case 1:
			level2.setVisibility(INVISIBLE);
			level3.setVisibility(INVISIBLE);
			level4.setVisibility(INVISIBLE);
			level5.setVisibility(INVISIBLE);
			level6.setVisibility(INVISIBLE);
			
			break;
		case 2:
			level2Lock.setVisibility(INVISIBLE);
			level3.setVisibility(INVISIBLE);
			level4.setVisibility(INVISIBLE);
			level5.setVisibility(INVISIBLE);
			level6.setVisibility(INVISIBLE);
			break;
		case 3:
			level2Lock.setVisibility(INVISIBLE);
			level3Lock.setVisibility(INVISIBLE);
			level4.setVisibility(INVISIBLE);
			level5.setVisibility(INVISIBLE);
			level6.setVisibility(INVISIBLE);
			break;
		case 4:
			level2Lock.setVisibility(INVISIBLE);
			level3Lock.setVisibility(INVISIBLE);
			level4Lock.setVisibility(INVISIBLE);
			level5.setVisibility(INVISIBLE);
			level6.setVisibility(INVISIBLE);
			break;
		case 5:
			level2Lock.setVisibility(INVISIBLE);
			level3Lock.setVisibility(INVISIBLE);
			level4Lock.setVisibility(INVISIBLE);
			level5Lock.setVisibility(INVISIBLE);
			level6.setVisibility(INVISIBLE);
		case 6:
			level2Lock.setVisibility(INVISIBLE);
			level3Lock.setVisibility(INVISIBLE);
			level4Lock.setVisibility(INVISIBLE);
			level5Lock.setVisibility(INVISIBLE);
			level6Lock.setVisibility(INVISIBLE);
		}
		
		//set click listener
		level1.setOnClickListener(this);
		level2.setOnClickListener(this);
		level3.setOnClickListener(this);
		level4.setOnClickListener(this);
		level5.setOnClickListener(this);
		level6.setOnClickListener(this);


	
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.imn1:
			startActivity(new Intent("com.realBall.LEVELONE"));
			break;
		case R.id.imn2:
			startActivity(new Intent("com.realBall.LEVELTWO"));
			break;
		case R.id.imn3:
			startActivity(new Intent("com.realBall.LEVELTHREE"));
			break;
		case R.id.imn4:
			startActivity(new Intent("com.realBall.LEVELFOUR"));
			break;
		case R.id.imn5:
			startActivity(new Intent("com.realBall.LEVELFIVE"));
			break;
		case R.id.imn6:
			startActivity(new Intent("com.realBall.LEVELSIX"));
			break;
		}
		finish();
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		startActivity(new Intent("com.realBall.MENULIST"));
	}
	
	

}
