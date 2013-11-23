package edu.mit.cameraCulture.vblocks.predefined;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class HistogramEqualization extends Module {

	public static final String REGISTER_SERVICE_NAME = "Histogram Equalization";

	private Mat mRgb_Mat;

	private int mode = 0;

	public HistogramEqualization() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExecutionCode execute(Sample image) {

		mRgb_Mat = image.getRgbMat();

		synchronized (mRgb_Mat) {
			// mode 0: YCrCb
			// mode 1: HSV
			// mode 2: HSV with Saturation channel
			// mode 3: Gray

			List<Mat> channels = new ArrayList<Mat>();
			Mat mIntermediate_Mat = new Mat(image.getHeight(),
					image.getWidth(), CvType.CV_8UC4);

			switch (mode) {

			case 0:
				Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat,
						Imgproc.COLOR_RGB2YCrCb, 4);

				org.opencv.core.Core.split(mIntermediate_Mat, channels);
				Imgproc.equalizeHist(channels.get(0), channels.get(0));

				org.opencv.core.Core.merge(channels, mIntermediate_Mat);

				Imgproc.cvtColor(mIntermediate_Mat, mRgb_Mat,
						Imgproc.COLOR_YCrCb2RGB, 4);
				break;

			case 1:
				Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat,
						Imgproc.COLOR_RGB2HSV, 4);

				org.opencv.core.Core.split(mIntermediate_Mat, channels);
				Imgproc.equalizeHist(channels.get(2), channels.get(2));

				org.opencv.core.Core.merge(channels, mIntermediate_Mat);

				Imgproc.cvtColor(mIntermediate_Mat, mRgb_Mat,
						Imgproc.COLOR_HSV2RGB, 4);
				break;

			case 2:
				Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat,
						Imgproc.COLOR_RGB2HSV, 4);

				org.opencv.core.Core.split(mIntermediate_Mat, channels);
				Imgproc.equalizeHist(channels.get(1), channels.get(1)); // Equalize
																		// the
																		// Saturation
																		// Channel
																		// too
				Imgproc.equalizeHist(channels.get(2), channels.get(2));

				org.opencv.core.Core.merge(channels, mIntermediate_Mat);

				Imgproc.cvtColor(mIntermediate_Mat, mRgb_Mat,
						Imgproc.COLOR_HSV2RGB, 4);
				break;

			case 3:
				Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat,
						Imgproc.COLOR_RGB2GRAY, 1);

				Imgproc.equalizeHist(mIntermediate_Mat, mIntermediate_Mat);

				Imgproc.cvtColor(mIntermediate_Mat, mRgb_Mat,
						Imgproc.COLOR_GRAY2RGB, 4);
				break;

			default:
				break;
			}
			System.gc();
		}

		return null;
	}

	@Override
	public String getName() {
		return "Histogram Equalization";
	}

	public static String getModuleName() {
		return REGISTER_SERVICE_NAME;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate(EngineActivity Context) {
		super.onCreate();
	}

	public static final Parcelable.Creator<HistogramEqualization> CREATOR = new Parcelable.Creator<HistogramEqualization>() {
		@Override
		public HistogramEqualization createFromParcel(Parcel in) {
			Log.v("ParcelableTest", "Creating from parcel");
			return new HistogramEqualization();
		}

		public HistogramEqualization[] newArray(int size) {
			return new HistogramEqualization[size];
		}

	};

	public CommitableView getConfigurationView(Context context) {
		return new HistogramEqualizationConfig(context, this);
	}

	public void setMode(int m) {
		this.mode = m;
	}

	public int getMode() {
		return this.mode;
	}
}
