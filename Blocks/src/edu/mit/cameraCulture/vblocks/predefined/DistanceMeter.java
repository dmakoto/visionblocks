package edu.mit.cameraCulture.vblocks.predefined;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.predefined.OpticalFlow.BorderView;

public class DistanceMeter extends Module implements SensorEventListener {
	class CrossView extends View {
		private Paint p = new Paint();

		public CrossView(Context context) {
			super(context);
			p.setColor(Color.RED);
			p.setStrokeWidth(2);
		}

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawLine(0, this.getHeight() / 2, this.getWidth(),
					this.getHeight() / 2, p);
			canvas.drawLine(this.getWidth() / 2, 0, this.getWidth() / 2,
					this.getHeight(), p);
		}
	}

	public static final String REGISTER_SERVICE_NAME = "DistanceMeter";

	private Button mTrigger;
	private TextView mSpeedLabel;
	private TextView mObjectDistanceLabel;
	private TextView mDistanceLabel;
	private SensorManager mSensorManager;
	private float[] mAccelerometervalues = null;
	private float[] mGeomagneticmatrix = null;
	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];

	private float[] mInitialOrientation = null;
	private long mInitTime;
	private static String distanceLabel = "Distance from camera: ";

	public DistanceMeter() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	public void onCreate(EngineActivity context) {
		super.onCreate(context);

		mSensorManager = (SensorManager) context
				.getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_NORMAL);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		RelativeLayout layout = context.getLayout();
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				(int) (metrics.density * 30 + 0.5f),
				(int) (metrics.density * 30 + 0.5f));
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
		CrossView v = new CrossView(context);
		layout.addView(v, lp);

		mDistanceLabel = new TextView(context);
		mDistanceLabel.setText(distanceLabel);
		mDistanceLabel.setTextColor(Color.RED);
		mDistanceLabel.setTextSize((int) (metrics.density * 12 + 0.5f));
		layout.addView(mDistanceLabel);

		lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.topMargin = (int) (metrics.density * 25 + 0.5f);
		mObjectDistanceLabel = new TextView(context);
		mObjectDistanceLabel.setText("Distance");
		mObjectDistanceLabel.setTextColor(Color.RED);
		mObjectDistanceLabel.setTextSize((int) (metrics.density * 12 + 0.5f));
		mObjectDistanceLabel.setVisibility(View.GONE);
		layout.addView(mObjectDistanceLabel, lp);

		lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.topMargin = (int) (metrics.density * 50 + 0.5f);

		mSpeedLabel = new TextView(context);
		mSpeedLabel.setText(distanceLabel);
		mSpeedLabel.setTextColor(Color.RED);
		mSpeedLabel.setTextSize((int) (metrics.density * 12 + 0.5f));
		mSpeedLabel.setVisibility(View.GONE);
		layout.addView(mSpeedLabel, lp);

		lp = new RelativeLayout.LayoutParams(
				(int) (metrics.density * 100 + 0.5f),
				(int) (metrics.density * 60 + 0.5f));
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, -1);
		mTrigger = new Button(context);
		mTrigger.setText("Measure distance");
		layout.addView(mTrigger, lp);

		mTrigger.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mInitialOrientation == null) {
					mInitialOrientation = new float[3];

					System.arraycopy(mOrientation, 0, mInitialOrientation, 0,
							mOrientation.length);
					Log.d("Distance meter", "Checked");
					mInitTime = new Date().getTime();
					mObjectDistanceLabel.setVisibility(View.VISIBLE);
					mSpeedLabel.setVisibility(View.VISIBLE);
				} else {
					// compute speed
					double startDistance = 1.5 / Math.tan((Math.PI / 2)
							- mInitialOrientation[2]);
					double endDistance = 1.5 / Math.tan((Math.PI / 2)
							- mOrientation[2]);

					double angle = Math.max(mInitialOrientation[0],
							mOrientation[0])
							- Math.min(mInitialOrientation[0], mOrientation[0]);

					double size = (startDistance * startDistance)
							+ (endDistance * endDistance)
							- (2 * startDistance * endDistance * Math
									.cos(angle));

					size = Math.sqrt(size);

					Log.d("Distance meter", "Distance:" + size);
					mInitialOrientation = null;
					long time = new Date().getTime() - mInitTime;
					time /= 1000;
					double speed = size / time;

				}
			}
		});
	}

	@Override
	public void onDestroyModule() {
		mSensorManager.unregisterListener(this);
		super.onDestroyModule();
	}

	@Override
	public ExecutionCode execute(Sample image) {

		return ExecutionCode.NONE;
	}

	@Override
	public String getName() {
		return getModuleName();
	}

	public static String getModuleName() {
		return "Speed meter";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				mAccelerometervalues = event.values.clone();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mGeomagneticmatrix = event.values.clone();
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				break;
			}
			if (mGeomagneticmatrix != null && mAccelerometervalues != null) {
				SensorManager.getRotationMatrix(mR, null, mAccelerometervalues,
						mGeomagneticmatrix);
				SensorManager.getOrientation(mR, mOrientation);
				// double distance =
				// Math.toDegrees(mOrientation[0])+","+Math.toDegrees(mOrientation[1])+","+Math.toDegrees(mOrientation[2]));
				mDistanceLabel.setText(distanceLabel
						+ new DecimalFormat("#.##").format(-1.5
								/ Math.tan((Math.PI / 2) - mOrientation[2]))
						+ " m");
				// Log.d("Test", "             "+ new
				// DecimalFormat("#.##").format()+ " m");

				if (mInitialOrientation != null) {
					double startDistance = 1.5 / Math.tan((Math.PI / 2)
							- mInitialOrientation[2]);
					double endDistance = 1.5 / Math.tan((Math.PI / 2)
							- mOrientation[2]);

					double angle = Math.max(mInitialOrientation[0],
							mOrientation[0])
							- Math.min(mInitialOrientation[0], mOrientation[0]);

					double size = (startDistance * startDistance)
							+ (endDistance * endDistance)
							- (2 * startDistance * endDistance * Math
									.cos(angle));

					size = Math.sqrt(size);

					Log.d("Distance meter", "Distance:" + size);
					long time = new Date().getTime() - mInitTime;
					time /= 1000;
					double speed = size / time;
					mObjectDistanceLabel.setText("Distance: "
							+ new DecimalFormat("#.##").format(size) + " m");
					mSpeedLabel.setText("Speed: "
							+ new DecimalFormat("#.##").format(speed * 3.6)
							+ " km/h");
				}
			}
		}

	}
}
