package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class Pixelize extends Module {

	
	public static final String REGISTER_SERVICE_NAME = "Pixelize";

	private Mat mRgb_Mat;
	
	public Pixelize() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExecutionCode execute(Sample image) {
		// Apply Pixelize by resizing mRgb_Mat to a smaller size, and then
		// resizing it again to the original size.
		// resize is OpenCV native.
		
		mRgb_Mat = image.getRgbMat();
		synchronized(mRgb_Mat) {
			Mat mIntermediate_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			Imgproc.resize(mRgb_Mat, mIntermediate_Mat, new Size(0,0), 0.1, 0.1, Imgproc.INTER_NEAREST);
	        Imgproc.resize(mIntermediate_Mat, mRgb_Mat, mRgb_Mat.size(), 0., 0., Imgproc.INTER_NEAREST);
	        System.gc();
		}
		return null;
	}

	@Override
	public String getName() {
		return "Pixelize";
	}
	
	public static String getModuleName() {
		return REGISTER_SERVICE_NAME;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public	void onCreate(EngineActivity Context) {
		super.onCreate();
	}
	
	public static final Parcelable.Creator<Pixelize> CREATOR = new
			Parcelable.Creator<Pixelize>() {
		public Pixelize createFromParcel(Parcel in) {
			Log.v("ParcelableTest","Creating from parcel");
			return new Pixelize();
		}

		public Pixelize[] newArray(int size) {
			return new Pixelize[size];
		}

	};
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}
}
