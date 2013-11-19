package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class Canny extends Module {

	public static final String REGISTER_SERVICE_NAME = "Canny";
	
	// Attributes used in Canny
	private int thresholdMin = 80;
	private int thresholdMax = 90;
	
	// Enable/Disable blur
	private boolean enableBlur = true;
	
	// Enable/Disable mask
	private boolean enableMask = false;
	
	// Mat
	private Mat mRgb_Mat;
	
	private Size kSize;
	private int blurSize = 3;
	
	public Canny() {
		super(REGISTER_SERVICE_NAME);
	}
	
	@OutputBool( vars = {"OUT_OF_BOUNDS"})
	@OutputMat( vars = {"RECT"})
	@OutputInt( vars = {"Int"})

	@Override
	public ExecutionCode execute(Sample image) {
		
		// Initialize attributes for blur
		kSize.height = blurSize;
		kSize.width = blurSize;
		
		mRgb_Mat = image.getRgbMat();
		
		// Applying the effect - Good to note that copying Mats have insignificant time practically
		synchronized (mRgb_Mat) {
			Mat mIntermediate_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			mRgb_Mat.copyTo(mIntermediate_Mat);
			
			if(enableBlur){
			// Apply Blur (doubles processing time)
				Imgproc.blur(mRgb_Mat, mIntermediate_Mat, kSize);
			}
			
			// Apply Canny (OpenCV native)
			// Commitable view to change thresholds?
			Imgproc.Canny(mIntermediate_Mat, mIntermediate_Mat, thresholdMin, thresholdMax);
			
			if(enableMask) {
				// Add the Canny Mat as Mask to image
				image.setMat("Mask", mIntermediate_Mat);
			} else {
				mIntermediate_Mat.copyTo(mRgb_Mat);
			}
			
			System.gc();
		}
		
		return null;
	}

	@Override
	public String getName() {
		return REGISTER_SERVICE_NAME;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public	void onCreate(EngineActivity Context) {
		super.onCreate();
		 
		kSize = new Size(3,3);
	}

	public static String getModuleName() {
		return REGISTER_SERVICE_NAME;
	}
	
	public static final Parcelable.Creator<Canny> CREATOR = new
			  Parcelable.Creator<Canny>() {
			      public Canny createFromParcel(Parcel in) {
			          Log.v("ParcelableTest","Creating from parcel");
			              return new Canny();
			      }

			      public Canny[] newArray(int size) {
			              return new Canny[size];
			      }
			     
	};

	
	public CommitableView getConfigurationView(Context context) {
		return new CannyConfig(context, this);
	}

	public int getThresholdMin() {
		// TODO Auto-generated method stub
		return this.thresholdMin;
	}
	
	public int getThresholdMax() {
		// TODO Auto-generated method stub
		return this.thresholdMax;
	}
	
	public void setThreshold(int min, int max) {
		// TODO Auto-generated method stub
		this.thresholdMin = min;
		this.thresholdMax = max;
	}
	
	public boolean getEnableBlur() {
		return this.enableBlur;
	}
	
	public void setEnableBlur(boolean b) {
		this.enableBlur = b;
	}
	
	public boolean getEnableMask() {
		return this.enableMask;
	}
	
	public void setEnableMask(boolean b) {
		this.enableMask = b;
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}
}