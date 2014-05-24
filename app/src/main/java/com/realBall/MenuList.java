package com.realBall;

import com.realBall.R;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
/**
 * Activity for main menu. 
 * @author Vinky
 *
 */
public class MenuList extends Activity {
	
	private Button levelSelect;
	private Button freeStyle;
	private Button option;
	private Button checkGragh;
	private Button help;
	private Button quit;
	//create a new instance
	public static MediaPlayer mp = new MediaPlayer();
	static boolean firstTime = true;
	/** Called when the activity is first created. */
    @Override

    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
    	
    	//set the button view
    	levelSelect = (Button) findViewById(R.id.BlevelSelect);
    	freeStyle = (Button) findViewById(R.id.BfreeStyle);
    	option = (Button) findViewById(R.id.Boption);
    	checkGragh = (Button) findViewById(R.id.BcheckGragh);
    	help = (Button) findViewById(R.id.Bhelp);
    	quit = (Button) findViewById(R.id.Bquit);
    	
    	if(firstTime){
    		Log.e("mp","fisrtTime");
    		//set music player
    		mp = MediaPlayer.create(this, R.raw.game);
        	mp.setLooping(true);
        	mp.start();
        	//Clear the velocity and acceleration data
        	LevelOne.velocityList.clear();
    		LevelOne.accelerationList.clear();
    		//set the flag
        	firstTime = false;
    	}
    	
    	
    	//set the click listener
    	help.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openHelp = new Intent ("com.realBall.HELP");
				startActivity(openHelp);
				
			}
		});
    	levelSelect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openLevel = new Intent ("com.realBall.SELECTLEVEL");
				startActivity(openLevel);
				finish();
			}
		});
    	option.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent openOption = new Intent ("com.realBall.OPTION");
				startActivity(openOption);
				
			}
		});
        quit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
					mp.release();
					Log.e("mp","released!");
					firstTime = true;
				
				finish();
			}
		});
        
        checkGragh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent("com.realBall.SHOWGRAPH"));
				finish();
			}
		});
        freeStyle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent("com.realBall.FREESTYLELEVEL"));
				finish();
			}
		});
		
		
    }
	@Override
	public void onBackPressed() {
		//when quit this activity, release the media player
		if(mp!=null){
			mp.release();
			firstTime = true;
		}
		finish();
	}
    
}