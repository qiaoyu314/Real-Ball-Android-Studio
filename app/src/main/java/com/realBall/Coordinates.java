package com.realBall;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
/**
 * It's the view of coordinates system. 
 * @author Yu, Marty and Lingchen
 *
 */
public class Coordinates extends View{
	//These fields are the coordinates helping draw x-y axis 
	private final float left = 40f;
	private final float right = 450f;
	private final float top = 50f;
	private final float bottom = 500f;
	
	
	private List<Float> time = new ArrayList<Float>();
	private List<Float> acceleration = new ArrayList<Float>();
	private List<Float> velocity = new ArrayList<Float>();
	

	public Coordinates(Context context) {
		super(context);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Paint whitePaint = new Paint();
		Paint redPaint = new Paint();
		Paint bluePaint = new Paint();
		Paint textPaint = new Paint();
		//set the color of the paint
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStrokeWidth(5f);
		redPaint.setColor(Color.RED);
		redPaint.setStrokeWidth(3f);
		bluePaint.setColor(Color.BLUE);
		bluePaint.setStrokeWidth(3f);
		
		//set the background
		canvas.drawColor(Color.BLACK);
		
		//draw some text
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(20f);
		canvas.drawText("This graph is base on the data", left, top - 30f, textPaint);
		canvas.drawText("from the last game you played.", left, top - 10f, textPaint);
		textPaint.setTextSize(40f);
		textPaint.setColor(Color.RED);		
		canvas.drawText("Velocity (m/s)", left, bottom+80f, textPaint);
		textPaint.setColor(Color.BLUE);
		canvas.drawText("Acceleration (m/s^2)", left, bottom+130f, textPaint);
		
		//draw coordinates
		//y axis
		canvas.drawLine(left, top, left, bottom,whitePaint);
		canvas.drawLine(left, top, left-20f, top+20f,whitePaint);
		canvas.drawLine(left, top, left+20f, top+20f,whitePaint);
		//x axis
		canvas.drawLine(left, bottom, right, bottom,whitePaint);
		canvas.drawLine(right, bottom, right-20f, bottom-20f,whitePaint);
		canvas.drawLine(right, bottom, right-20f, bottom+20f,whitePaint);
		whitePaint.setTextSize(30f);
		canvas.drawText("Time (s)", right-100, bottom+50, whitePaint);
		//place "velocity" and "acceleration"points
		int i;
		for(i=0;i<velocity.size();i++){
			canvas.drawPoint(time.get(i), velocity.get(i), redPaint);
			canvas.drawPoint(time.get(i), acceleration.get(i), bluePaint);
			if(i>0){
				canvas.drawLine(time.get(i), velocity.get(i), time.get(i-1), velocity.get(i-1), redPaint);
				canvas.drawLine(time.get(i), acceleration.get(i), time.get(i-1), acceleration.get(i-1), bluePaint);
			}
		}
		
		if(velocity.size()>0){
			//place the velocity origin
			redPaint.setTextSize(40);
			bluePaint.setTextSize(40);
			canvas.drawText("0", left-20, velocity.get(0), redPaint);
			//place the acceleration origin
			canvas.drawText("0", left-20, acceleration.get(0), bluePaint);
		}
		
	}
	/**
	 * This method is passed the lists of velocity and acceleration, and it will redraw the view.
	 * @param s: the list storing the data of velocity
	 * @param a: the list storing the data of acceleration 
	 */
	public void setData(List<Float> v, List<Float> a){
		//get the min and max value in the lists in order to calculate the proper coordinates
		float minvelocity = findMin(v);
		float minAcceleration = findMin(a);
		float timeStep = (right-left)/a.size();
		float velocityStep = (top-bottom)/(findMax(v)-minvelocity);
		float accelerationStep = (top-bottom)/(findMax(a)-minAcceleration);
		
		//calculate the exact coordinates of the points for velocity and acceleration
		//These points will be scaled on coordinates system no matter how many data there are.
		int i;
		for(i=0;i<a.size();i++){
			time.add(left+i*timeStep);
			velocity.add(bottom + velocityStep * (v.get(i) - minvelocity));
			acceleration.add(bottom+accelerationStep*(a.get(i)-minAcceleration));
		}
		invalidate();
	}
	/**
	 * 
	 * @param a
	 * @return the max element in the list
	 */
	float findMax(List <Float> a){
		if(a.size() == 0){
			return 0;
		}
			
		else{
			int i;
			float max = a.get(0);
			for(i=2;i<a.size();i++){
				if(a.get(i)>max)
					max = a.get(i);
			}
			return max;
		}
	}
	/**
	 * 
	 * @param a
	 * @return the min element in the list
	 */
	float findMin(List <Float> a){
		if(a.size() == 0)
			return 0;
		else{
			int i;
			float min = a.get(0);
			for(i=2;i<a.size();i++){
				if(a.get(i)<min)
			min = a.get(i);
			}
			return min;
		}
	}
	
}
