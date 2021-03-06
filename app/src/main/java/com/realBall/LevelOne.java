package com.realBall;

import java.util.ArrayList;
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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
/**
 * Activity for level one
 * Adapted from AccelerometerPaly sample code
 * In level one, the user must have acceleration in the opposite direciton of movement.
 * @author Yu, Marty and Lingchen
 *
 */
public class LevelOne extends Activity {
	protected int SCREEN_WIDTH;
    protected int SCREEN_HEIGHT;
    protected int DENSITY;
    private int arrowSize;


    public Boolean firstTime = true;
	private SimulationView mSimulationView;
	private SensorManager mSensorManager;
	private WindowManager mWindowManager;
	private Display mDisplay;
	private boolean isStarted = false;
	private Timer myTimer;
	private long timeRemaining = 30;

	// Used to keep track of the points for the graph
	public static ArrayList<Float> velocityList = new ArrayList<Float>();
	public static ArrayList<Float> accelerationList = new ArrayList<Float>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Clear the points to start the graph fresh
		LevelOne.velocityList.clear();
		LevelOne.accelerationList.clear();

        //get the screen size
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SCREEN_HEIGHT = metrics.heightPixels;
        SCREEN_WIDTH = metrics.widthPixels;
        DENSITY = metrics.densityDpi;
        arrowSize = (int)(SCREEN_WIDTH * 0.15);

		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();

		// instantiate our simulation view and set it as the activity's content
		mSimulationView = new SimulationView(this);
		setContentView(mSimulationView);
		//create a new timer to count down the time. User should finish this level in 30sec
		myTimer = new Timer(30000, 1000);
		//create a dialog to describe the goal of this level
		new AlertDialog.Builder(this)
				.setMessage(
						"In this level, the goal is to have the ball accelerating in the opposite direction of its motion.")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton("Continue",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								mSimulationView.startSimulation();
								myTimer.start();
							}
						}).show();
		
		
	}



	@Override
	protected void onPause() {
		Log.e("onPause", "onPause is called");
		super.onPause();
		//stop the timer
		myTimer.cancel();
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
								myTimer = new Timer(timeRemaining * 1000, 1000);
								myTimer.start();
							}
						}).show();
	}

	/*
	 * We have no need to override onResume() because
	 * mSimulationView.startSimulation() is taken care of in onPause(). By doing
	 * it this way, we prevent errors involving the ball being able to roll
	 * before the user presses "continue" at the start of the level or during a
	 * pause screen.
	 */

	@Override
	public void onBackPressed() {
		// Return to the menu
		finish();
		startActivity(new Intent("com.realBall.MENULIST"));
	}

	class SimulationView extends View implements SensorEventListener {
		// diameter of the balls in meters
		private static final float sBallDiameter = 0.004f;
		private static final float sBallDiameter2 = sBallDiameter
				* sBallDiameter;

		// friction of the virtual table and air

		private Sensor mAccelerometer;
		private long mLastT;
		private float mLastDeltaT;

		private float mXDpi;
		private float mYDpi;
		private float mMetersToPixelsX;
		private float mMetersToPixelsY;
		private Bitmap ballBitMap;
		private Bitmap mRoad;
        private Bitmap redArrow;
        private Bitmap greenArrow;
		private float mXOrigin;
		private float mYOrigin;
		private float mSensorX;
		private float mSensorY;
		private long mSensorTimeStamp;
		private long mCpuTimeStamp;
		private float mHorizontalBound;
		private float mVerticalBound;
		private final ParticleSystem mParticleSystem = new ParticleSystem();
		private float velocity = 0, acceleration = 0;
		private Boolean levelConditions = false;

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
				// Set the coefficient of friction.
				mOneMinusFriction = 1.0f;
			}

			public void computePhysics(float sx, float sy, float dT, float dTC) {
				// Force of gravity applied to our virtual object
				final float m = 1000.0f; // mass of our virtual object
				final float gx = -sx * m;
				final float gy = -sy * m;

				/*
				 * F= mA <=> A = F/m We could simplify the code by completely
				 * eliminating "m" (the mass) from all the equations, but it
				 * would hide the concepts from this sample code.
				 */
				final float invm = 1.0f / m;
				float ax = gx * invm;
				float ay = gy * invm;

				// Get rid of extremely low accelerations to make the sensor less sensitive
				if (Math.abs(ay) < 0.01f) {
					ay = 0;
				}
				/*
				 * Time-corrected Verlet integration. This computes the changing
				 * position of the ball with friction accounted for.
				 */
				final float dTdT = dT * dT;
				final float x = mPosX + mOneMinusFriction * dTC
						* (mPosX - mLastPosX) + mAccelX * dTdT;

				final float y = mPosY + mOneMinusFriction * dTC
						* (mPosY - mLastPosY) + mAccelY * dTdT;

				// Check the change in position over time to get the velocity
				velocity = (y - mLastPosY) / dT;

				// Set velocity to 0 if it is very small.
				if (Math.abs(velocity) < 0.001) {
					velocity = 0;
				}
				acceleration = ay;

				// Keep track of velocitys and accelerations for the graph
				velocityList.add(velocity);
				accelerationList.add(ay);

				// Set the specific conditions for this level
				Boolean levelConditions = ((velocity < 0 && acceleration > 0) || (velocity > 0 && acceleration < 0));

				// Check if the level is beaten
				if (levelConditions) {
					Log.e("computePhysics", "levelConditions are reached");
					try {
						// Sleep briefly so level ends smoothly
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
					}
					;
					// Stop the simulation and end
					stopSimulation();
					finishLevel();
				}

				mLastPosX = mPosX;
				mLastPosY = mPosY;
				mPosX = x;
				mPosY = y;
				mAccelX = ax;
				mAccelY = ay;

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
					// do not let the position go off screen
					mPosX = xmax;
					// make velocity 0 if ball is at the wall
					velocity = 0;
				} else if (x < -xmax) {
					// do not let the position go off screen
					mPosX = -xmax;
					// make velocity 0 if ball is at the wall
					velocity = 0;
				}
				if (y > ymax - .008f) {
					// do not let the position go off screen
					mPosY = ymax-.008f;
					// make velocity 0 if ball is at the wall
					velocity = 0;
				} else if (y < -ymax) {
					// do not let the position go off screen
					mPosY = -ymax;
					// make velocity 0 if ball is at the wall
					velocity = 0;
				}
			}
		}

		/*
		 * A particle system is just a collection of particles
		 */
		class ParticleSystem {
			// We will have just one particle
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
					final float dT = (float) (t - mLastT)
							* (1.0f / 1000000000.0f);
					if (mLastDeltaT != 0) {
						final float dTC = dT / mLastDeltaT;
						final int count = mBalls.length;
						// Update the position of each ball (in this case just
						// 1)
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
				// Lock in x coordinate to make the ball roll in 1 dimension
				sx = 0;

				// Update the system's positions
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
								final float c = (0.5f * (sBallDiameter - d))
										/ d;
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
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_UI);
			// Keep track of whether the simulation is started or not
			isStarted = true;
		}

		public void stopSimulation() {
			mSensorManager.unregisterListener(this);
			// Keep track of whether the simulation is started or not
			isStarted = false;
		}

		public SimulationView(Context context) {
			super(context);
			mAccelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			mXDpi = metrics.xdpi;
			mYDpi = metrics.ydpi;
			mMetersToPixelsX = mXDpi / 0.0254f;
			mMetersToPixelsY = mYDpi / 0.0254f;

			// rescale the ball so it's about 0.5 cm on screen
			ballBitMap = BitmapFactory.decodeResource(getResources(),
					R.drawable.ball);
			final int dstWidth = 50;
			final int dstHeight = 50;
			ballBitMap = Bitmap
					.createScaledBitmap(ballBitMap, dstWidth, dstHeight, true);

			Options opts = new Options();
			opts.inDither = true;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			mRoad = BitmapFactory.decodeResource(getResources(),
					R.drawable.wood, opts);
            mRoad = Bitmap.createScaledBitmap(mRoad,SCREEN_WIDTH,SCREEN_HEIGHT,true);

            redArrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_red);
            redArrow = Bitmap.createScaledBitmap(redArrow, arrowSize, arrowSize, true);

            greenArrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_green);
            greenArrow = Bitmap.createScaledBitmap(greenArrow,arrowSize, arrowSize, true);

		}


		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			// compute the origin of the screen relative to the origin of
			// the bitmap
			mXOrigin = (w - ballBitMap.getWidth()) * 0.5f;
			mYOrigin = (h - ballBitMap.getHeight()) * 0.5f;
			mHorizontalBound = ((w / mMetersToPixelsX - sBallDiameter) * 0.5f);
			mVerticalBound = ((h / mMetersToPixelsY - sBallDiameter) * 0.5f);
		}

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        @Override
		public void onSensorChanged(SensorEvent event) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;
			//check if the time is up
			if(myTimer.hasFinished()){
				stopSimulation();
				loseLevel();
			}
			else{
				timeRemaining = myTimer.getTimeRemaining();
			}
			
			
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
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//draw background
			canvas.drawBitmap(mRoad, 0, 0, null);
			
			Paint paint = new Paint();
			//set some property of paint
			paint.setColor(Color.WHITE);
            int textSize = (int)(DENSITY * 0.3);
			paint.setTextSize(textSize);

			//round the value to 2 decimal digits
			//float disvelocity = (float)Math.round(velocity * 1000) / 10;
			//float disAccel = (float)Math.round(acceleration * 1000) / 10;

            //draw round rect
            //canvas.drawRoundRect();

			//draw text
//			canvas.drawText("Velocity", SCREEN_WIDTH*0.1f, SCREEN_HEIGHT*0.03f, paint);
//			canvas.drawText(Float.toString(disvelocity)+" m/s", 70, 70, paint);
//			canvas.drawText("Acceleration", SCREEN_WIDTH*0.9f - 5*textSize, SCREEN_HEIGHT*0.03f, paint);
//			canvas.drawText(Float.toString(disAccel)+" m/s^2", 300, 70, paint);


			paint.setColor(Color.WHITE);
			paint.setTextSize(textSize);
            String timeText = timeRemaining<10?"0"+String.valueOf(timeRemaining):String.valueOf(timeRemaining);
            float ascent = -paint.ascent();
			canvas.drawText(timeText, SCREEN_WIDTH/2 - textSize/2, ascent, paint);

            //test arrow bitmap
            float arrowMargin = SCREEN_WIDTH * 0.1f;

            //draw a rectangle
            float strokeWidth = DENSITY/20;
            paint.setStrokeWidth(strokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            RectF regBox = new RectF(SCREEN_WIDTH*0.05f, strokeWidth/2, SCREEN_WIDTH*0.95f, arrowSize+strokeWidth);
            canvas.drawRoundRect(regBox, 50, 50, paint);

			
			/*
			 * draw the arrow
			 */
			paint.setColor(Color.RED);
			if(velocity>0)
                upRedArrow(redArrow, arrowMargin, strokeWidth, arrowSize, canvas);
			else if(velocity<0){
				downRedArrow(redArrow, arrowMargin, strokeWidth, arrowSize, canvas);
			}
			if(acceleration>0){
				upGreenArrow(greenArrow, SCREEN_WIDTH - arrowSize - arrowMargin, strokeWidth, arrowSize, canvas);
			}
			else if(acceleration<0){
				downGreenArrow(greenArrow, SCREEN_WIDTH - arrowSize - arrowMargin, strokeWidth, arrowSize, canvas);
			}
			/*
			 * compute the new position of our object, based on accelerometer
			 * data and present time.
			 */

			final ParticleSystem particleSystem = mParticleSystem;
			final long now = mSensorTimeStamp
					+ (System.nanoTime() - mCpuTimeStamp);
			final float sx = mSensorX / 100;
			final float sy = mSensorY / 100;

			particleSystem.update(sx, sy, now);

			final float xc = mXOrigin;
			final float yc = mYOrigin;
			final float xs = mMetersToPixelsX;
			final float ys = mMetersToPixelsY;
			final Bitmap bitmap = ballBitMap;
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
		}
		private void upRedArrow(Bitmap arrow, float leftMargin, float topMargin, int size, Canvas canvas){
            //original arrow is down
            Matrix matrix = new Matrix();
            matrix.preRotate(180);
            arrow = Bitmap.createBitmap(arrow,0, 0, arrowSize, size, matrix, true);
            canvas.drawBitmap(arrow,leftMargin, topMargin, null);
			
		}
		private void downRedArrow(Bitmap arrow, float leftMargin, float topMargin, int size, Canvas canvas){
            //original arrow is down
            canvas.drawBitmap(arrow, leftMargin, topMargin, null);
		}

        private void downGreenArrow(Bitmap arrow, float rightMargin, float topMargin, int size, Canvas canvas){
            //original arrow is down
            Matrix matrix = new Matrix();
            matrix.preRotate(180);
            arrow = Bitmap.createBitmap(arrow,0, 0, size, arrowSize, matrix, true);
            canvas.drawBitmap(arrow, rightMargin, topMargin, null);

        }
        private void upGreenArrow(Bitmap arrow, float rightMargin, float topMargin, int size, Canvas canvas){
            //original arrow is up
            canvas.drawBitmap(arrow, rightMargin,topMargin, null);
        }

	}

	public void finishLevel() {
		Log.e("finishLevel", "finishLevel is reached");

		// Make sure this is not called more than once by checking a boolean
		if (firstTime) {
			firstTime = false;

			// If this is the user's first time beating level 1, set the highest
			// level the user can play as 2. If the user has already beaten
			// level 1 previously, do nothing.
			if (Login.nowLevel == 1) {
				DatabaseHelper dh = new DatabaseHelper(this);
				dh.open();
				dh.updateLevel(Login.nowUserName);
				dh.close();
				// The highest level the user can play has moved up to 2
				Login.nowLevel = 2;
			}
			// Allow the user to go to the next level immediately, view the
			// graph of what they just played, or return to the menu.
			new AlertDialog.Builder(this)
					.setTitle("You WIN!")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Next Level",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// callNextLevel();
									startActivity(new Intent(
											"com.realBall.LEVELTWO"));
									finish();
								}
							})
					.setNegativeButton("Menu",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											"com.realBall.MENULIST"));
									finish();
								}
							})
					.setNeutralButton("Graph",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											"com.realBall.SHOWGRAPH"));
									finish();
								}
							}).show();

		}
	}
	public void loseLevel() {

		// Make sure this is not called more than once by checking a boolean
		if (firstTime) {
			firstTime = false;
			
			// Allow the user to go to the next level immediately, view the
			// graph of what they just played, or return to the menu.
			new AlertDialog.Builder(this)
					.setTitle("You LOSE!")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Try Again",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											"com.realBall.LEVELONE"));
									finish();
								}
							})
					.setNegativeButton("Menu",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											"com.realBall.MENULIST"));
									finish();
								}
							})
					.setNeutralButton("Graph",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											"com.realBall.SHOWGRAPH"));
									finish();
								}
							}).show();

		}
	}
}
