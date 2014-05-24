package com.realBall;

import java.util.ArrayList;

import com.realBall.R;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


/**
 * Adapted from AccelerometerPaly sample code
 * Activity for free style. No wining condition. User can press back button to finish.
 * @author Yu, Marty and Lingchen
 *
 */
//Level one is frictionless. The user must have acceleration in the opposite direciton of movement.
public class FreeStyleLevel extends Activity {
    //added by Marty Keegan
    public Boolean levelConditions = false;
    public Boolean firstTime = true;
    private SimulationView mSimulationView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private WakeLock mWakeLock;
    private boolean isStarted = false;


    
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelOne.velocityList.clear();
        LevelOne.accelerationList.clear();
		//
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
//        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
//                .getName());
//        //ADDED
//        mWakeLock.acquire();

        // instantiate our simulation view and set it as the activity's content
        mSimulationView = new SimulationView(this);
        setContentView(mSimulationView);
		//create a new timer to count down the time. User should finish this level in 30sec
        new AlertDialog.Builder(this)
		.setMessage("Welcome to Free Style. Press back to end the session.")
		//.setMessage("Play another game?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                mSimulationView.startSimulation();
	           }
	       })
		.show();
       
    }
    

//    @Override
 //   protected void onResume() {
  //      super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
      //  mWakeLock.acquire();

        // Start the simulation
  //      mSimulationView.startSimulation();
   // }

    @Override
	protected void onPause() {
		Log.e("onPause", "onPause is called");
		super.onPause();
		/*
		 * Stop the simulation. First make sure the simulation is started to
		 * prevent errors.
		 */
		if (isStarted) {
			mSimulationView.stopSimulation();
		}
		new AlertDialog.Builder(this)
				.setMessage("Paused")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton("Continue",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								mSimulationView.startSimulation();
							}
						}).show();
	}

    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		super.onBackPressed();
		if(levelConditions){
			finish();
			startActivity(new Intent("com.realBall.MENULIST"));
		}
		levelConditions = true;
		//startActivity(new Intent("com.realBall.MENULIST"));

	}


	class SimulationView extends View implements SensorEventListener {
        // diameter of the balls in meters
        private static final float sBallDiameter = 0.004f;
        private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;

        // friction of the virtual table and air
        private static final float sFriction = 0.1f;

        private Sensor mAccelerometer;
        private long mLastT;
        private float mLastDeltaT;

        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        private Bitmap mBitmap;
//        private Bitmap mWood;
        private Bitmap mRoad;
        private float mXOrigin;
        private float mYOrigin;
        private float mSensorX;
        private float mSensorY;
        private long mSensorTimeStamp;
        private long mCpuTimeStamp;
        private float mHorizontalBound;
        private float mVerticalBound;
        private final ParticleSystem mParticleSystem = new ParticleSystem();
        //add by Yu Qiao
        private float velocity, acceleration;

        

        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */
        class Particle {
            private float mPosX;
            private float mPosY;
            private float mAccelX;
            private float mAccelY;
            private float mLastPosX;
            private float mLastPosY;
            private float mOneMinusFriction;

            Particle() {
                // make each particle a bit different by randomizing its
                // coefficient of friction
//                final float r = ((float) Math.random() - 0.5f) * 0.2f;
                //mOneMinusFriction = 1.0f - sFriction + r;
                mOneMinusFriction = 1.0f;
            }

            public void computePhysics(float sx, float sy, float dT, float dTC) {
                // Force of gravity applied to our virtual object
                final float m = 1000.0f; // mass of our virtual object
                final float gx = -sx * m;
                final float gy = -sy * m;

                /*
                 * 锟�= mA <=> A = 锟�/ m We could simplify the code by
                 * completely eliminating "m" (the mass) from all the equations,
                 * but it would hide the concepts from this sample code.
                 */
                final float invm = 1.0f / m;
                 float ax = gx * invm;
                 float ay = gy * invm;

                if(Math.abs(ay) < 0.01){
                	ay = 0;
                }
                /*
                 * Time-corrected Verlet integration The position Verlet
                 * integrator is defined as x(t+锟� = x(t) + x(t) - x(t-锟� +
                 * a(t)锟斤拷 However, the above equation doesn't handle variable
                 * 锟�very well, a time-corrected version is needed: x(t+锟� =
                 * x(t) + (x(t) - x(t-锟�) * (锟�锟�prev) + a(t)锟斤拷 We also add
                 * a simple friction term (f) to the equation: x(t+锟� = x(t) +
                 * (1-f) * (x(t) - x(t-锟�) * (锟�锟�prev) + a(t)锟斤拷
                 */
                final float dTdT = dT * dT;
                final float x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) + mAccelX
                        * dTdT;
                
                final float y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) + mAccelY
                        * dTdT;
                // added by Marty Keegan
                velocity = (y - mLastPosY)/dT;
                if(Math.abs(velocity) < 0.01){
                	velocity = 0;
                }
                acceleration = ay;
                LevelOne.velocityList.add(velocity);
                LevelOne.accelerationList.add(ay);
                if(levelConditions){
     
                	try{
                	Thread.sleep(50);
                	}
                	catch(InterruptedException e){
                		
                	}
                	finally{
                	};
             
                	stopSimulation();
                	finishLevel();
                }
                
                mLastPosX = mPosX;
                mLastPosY = mPosY;
                mPosX = x;
                mPosY = y;
                mAccelX = ax;
                mAccelY = ay;
                /*
                 * Make arrayList here and store all mPosX values for graph.
                 * Make another arrayList to store all mAccelX.
                 * For graph, plot all points over time where time is the position in the array
                 * Somehow connect with a line?
                 */
            }

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            public void resolveCollisionWithBounds() {
                final float xmax = mHorizontalBound;
                final float ymax = mVerticalBound;
                final float x = mPosX;
                final float y = mPosY;
                if (x > xmax) {
                    mPosX = xmax;
                    //make velocity 0 if ball is at the wall
                    velocity = 0;
                } else if (x < -xmax) {
                    mPosX = -xmax;
                    velocity = 0;
                }
                if (y > ymax - .008f) {
					// do not let the position go off screen
					mPosY = ymax-.008f;
					// make velocity 0 if ball is at the wall
					velocity = 0;	
                } else if (y < -ymax) {
                    mPosY = -ymax;
                    velocity = 0;
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        class ParticleSystem {
            static final int NUM_PARTICLES = 1;
            private Particle mBalls[] = new Particle[NUM_PARTICLES];

            ParticleSystem() {
                /*
                 * Initially our particles have no velocity or acceleration
                 */
                for (int i = 0; i < mBalls.length; i++) {
                    mBalls[i] = new Particle();
                }
            }

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private void updatePositions(float sx, float sy, long timestamp) {
                final long t = timestamp;
                if (mLastT != 0) {
                    final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
                    if (mLastDeltaT != 0) {
                        final float dTC = dT / mLastDeltaT;
                        final int count = mBalls.length;
                        for (int i = 0; i < count; i++) {
                            Particle ball = mBalls[i];
                            ball.computePhysics(sx, sy, dT, dTC);
                        }
                    }
                    mLastDeltaT = dT;
                }
                mLastT = t;
            }

            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            public void update(float sx, float sy, long now) {
                // update the system's positions
            	sx=0;
            	//Check if it has meet the requirement 

                updatePositions(sx, sy, now);

                // We do no more than a limited number of iterations
                final int NUM_MAX_ITERATIONS = 10;

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                boolean more = true;
                final int count = mBalls.length;
                for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
                    more = false;
                    for (int i = 0; i < count; i++) {
                        Particle curr = mBalls[i];
                        for (int j = i + 1; j < count; j++) {
                            Particle ball = mBalls[j];
                            float dx = ball.mPosX - curr.mPosX;
                            float dy = ball.mPosY - curr.mPosY;
                            float dd = dx * dx + dy * dy;
                            // Check for collisions
                            if (dd <= sBallDiameter2) {
                                /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                                dx += ((float) Math.random() - 0.5f) * 0.0001f;
                                dy += ((float) Math.random() - 0.5f) * 0.0001f;
                                dd = dx * dx + dy * dy;
                                // simulate the spring
                                final float d = (float) Math.sqrt(dd);
                                final float c = (0.5f * (sBallDiameter - d)) / d;
                                curr.mPosX -= dx * c;
                                curr.mPosY -= dy * c;
                                ball.mPosX += dx * c;
                                ball.mPosY += dy * c;
                                more = true;
                            }
                        }
                        /*
                         * Finally make sure the particle doesn't intersects
                         * with the walls.
                         */
                        curr.resolveCollisionWithBounds();
                    }
                }
            }

            public int getParticleCount() {
                return mBalls.length;
            }

            public float getPosX(int i) {
                return mBalls[i].mPosX;
            }

            public float getPosY(int i) {
                return mBalls[i].mPosY;
            }
        }

        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            isStarted = true;
        }

        public void stopSimulation() {
            mSensorManager.unregisterListener(this);
            isStarted = false;
        }

        public SimulationView(Context context) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

            // rescale the ball so it's about 0.5 cm on screen
            Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            Bitmap car = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        //    final int dstWidth = (int) (sBallDiameter * mMetersToPixelsX + 0.5f);
            final int dstWidth = (int) (50);
        //    final int dstHeight = (int) (sBallDiameter * mMetersToPixelsY + 0.5f);
            final int dstHeight = (int) (50);
            mBitmap = Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true);

            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
//            mWood = BitmapFactory.decodeResource(getResources(), R.drawable.wood, opts);
            mRoad = BitmapFactory.decodeResource(getResources(), R.drawable.road,opts);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
            mXOrigin = (w - mBitmap.getWidth()) * 0.5f;
            mYOrigin = (h - mBitmap.getHeight()) * 0.5f;
            mHorizontalBound = ((w / mMetersToPixelsX - sBallDiameter) * 0.5f);
            mVerticalBound = ((h / mMetersToPixelsY - sBallDiameter) * 0.5f);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;

            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                    mSensorX = event.values[0];
                    mSensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    mSensorX = -event.values[1];
                    mSensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    mSensorX = -event.values[0];
                    mSensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    mSensorX = event.values[1];
                    mSensorY = -event.values[0];
                    break;
            }

            mSensorTimeStamp = event.timestamp;
            mCpuTimeStamp = System.nanoTime();
            
            
            
            //add by Yu 
            //velocity = mSensorX + mSensorY;
            //acceleration = mSensorX * mSensorY;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {

        	Paint paint = new Paint();

			paint.setColor(Color.RED);
			paint.setTextSize(30);
			paint.setStrokeWidth(5);
			float disvelocity = (float)Math.round(velocity * 1000) / 10;
			float disAccel = (float)Math.round(acceleration * 1000) / 10;
			
			canvas.drawBitmap(mRoad, 0, 0, null);
			canvas.drawText("velocity", 70, 30, paint);
			canvas.drawText(Float.toString(disvelocity)+" m/s", 70, 70, paint);
			canvas.drawText("Acceleration", 280, 30, paint);
			canvas.drawText(Float.toString(disAccel)+" m/s^2", 300, 70, paint);
			paint.setColor(Color.BLACK);
			paint.setTextSize(40);
			canvas.drawText("∞", 230, 50, paint);
			

			
			/*
			 * draw the arrow
			 */
			paint.setColor(Color.RED);
			if(velocity>0)
				upArrow(20, 10, paint, canvas);
			else if(velocity<0){
				downArrow(20, 10, paint, canvas);
			}
			if(acceleration>0){
				upArrow(470, 10, paint, canvas);
			}
			else if(acceleration<0){
				downArrow(470, 10, paint, canvas);
			}
           

 /*           // add by Yu Qiao 
            float velocity, acceleration;
            
            setContentView(R.layout.main);
            TextView  displayvelocity, displayAcc;
            
            displayvelocity = (TextView)findViewById(R.id.tvvelocity);
            displayAcc = (TextView)findViewById(R.id.tvAcceleration);
            
 */          
            
            /*
             * compute the new position of our object, based on accelerometer
             * data and present time.
             */
            
            
            

            final ParticleSystem particleSystem = mParticleSystem;
            final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
            final float sx = mSensorX/100;
            final float sy = mSensorY/100;
// add by Yu Qiao
            
          //  velocity = sx + sy;
            //acceleration = sx * sy;
 //           displayvelocity.setText("velocity");
//           displayAcc.setText("velocity");
            
//          
            particleSystem.update(sx, sy, now);

            final float xc = mXOrigin;
            final float yc = mYOrigin;
            final float xs = mMetersToPixelsX;
            final float ys = mMetersToPixelsY;
            final Bitmap bitmap = mBitmap;
            final int count = particleSystem.getParticleCount();
            for (int i = 0; i < count; i++) {
                /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */

                final float x = xc + particleSystem.getPosX(i) * xs;
                final float y = yc - particleSystem.getPosY(i) * ys;
                canvas.drawBitmap(bitmap, x, y, null);
            }

            
            
            // and make sure to redraw asap
            
            //invalidate();

            
            
        }
        private void upArrow(float x, float y, Paint paint, Canvas canvas){
			paint.setStrokeWidth(2);
			canvas.drawLine(x, y, x, y+50, paint);
			canvas.drawLine(x, y, x-10, y+10, paint);
			canvas.drawLine(x, y, x+10, y+10, paint);
			
		}
		private void downArrow(float x, float y, Paint paint, Canvas canvas){

			paint.setStrokeWidth(2);
			canvas.drawLine(x, y+50, x-10, y+40, paint);
			canvas.drawLine(x, y+50, x+10, y+40, paint);
			canvas.drawLine(x, y, x, y+50, paint);
		}

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    //Added by Marty keegan
    public void finishLevel(){
    if(firstTime){
    	firstTime = false;
    	new AlertDialog.Builder(this)
		.setTitle("End")
		//.setMessage("Play another game?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setNegativeButton("Menu", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent("com.realBall.MENULIST"));
				finish();
			}
		}).setNeutralButton("Graph", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent("com.realBall.SHOWGRAPH"));
				finish();
			}
		})
		.show();
    }
    }

}
