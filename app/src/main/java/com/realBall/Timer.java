package com.realBall;

import android.os.CountDownTimer;
/**
 * Class for count down the time.
 * @author Vinky
 *
 */
public class Timer extends CountDownTimer {
	//the remaining time. unit is second. It's updated when onTick() is triggered 
	private long timeRemaining;
	//indicates whether the timer is finished. It's updated when onFinish() is triggered
	private boolean hasFinished;

	public Timer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.timeRemaining = millisInFuture;
		hasFinished = false;
	}
	/**
	 * It's automatically triggered when time is up
	 */
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		this.hasFinished = true;
	}
	/**
	 * It's automatically triggered after each interval
	 */
	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		this.timeRemaining = millisUntilFinished / 1000;
		
	}
	//getters
	public long getTimeRemaining(){
		return this.timeRemaining;
	}
	public boolean hasFinished(){
		return this.hasFinished;
	}
	

}
