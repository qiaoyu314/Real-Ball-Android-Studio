package com.realBall;


import com.realBall.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
/**
 * Activity for splashing the game.
 * It shows the name of app with animation
 * @author Yu, Marty and Lingchen
 *
 */
public class splash extends Activity{
	
	protected int splashTime = 5000;
    protected int timeIncrement = 100;
    protected int sleepTime = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		ImageView title = (ImageView) findViewById(R.id.ivRB);
		//load the animation
		Animation animation = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.anim);
		title.setAnimation(animation);
		//start the animation
		animation.start();
		
		//create a new thread to count the time
		Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int elapsedTime = 0;
                    while(elapsedTime < splashTime) {
                        sleep(sleepTime);
                        elapsedTime = elapsedTime + timeIncrement;
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    startActivity(new Intent("com.realBall.LOGIN"));
                }
            }
        };
        splashThread.start();
    
	}
	

}
