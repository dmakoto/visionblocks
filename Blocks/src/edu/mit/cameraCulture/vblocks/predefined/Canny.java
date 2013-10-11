package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.Module.OutputBool;
import edu.mit.cameraCulture.vblocks.Module.OutputInt;
import edu.mit.cameraCulture.vblocks.Module.OutputMat;

public class Canny extends Module {

	public static final String REGISTER_SERVICE_NAME = "Canny";
	
	// This view is printed over the camera with the treated image in bitmapImg
	private CannyView view;
	private Bitmap bitmapImg;
	
	// Attributes used in Canny
	private int thresholdMin = 80;
	private int thresholdMax = 90;
	
	// Enable/Disable blur
	private boolean enableBlur = true;
	
	// Mat used by the module
	Mat mYUV_Mat;
	Mat mRgb_Mat;
	Mat mGray_Mat;
	Mat mEdges_Mat;
	
	// Image Properties
	private int imgWidth;  
	private int imgHeight;
	
	private Rect srcRect; // Source Rectangle
	private Rect dstRect; // Destination Rectangle
	
	Size kSize;
	int blurSize = 3;
	
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
		
		// Creates mYUV_Mat from image Sample
		if(mYUV_Mat == null){
			
			imgWidth = image.getWidth();
			imgHeight =  image.getHeight();		
			mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
			mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			mGray_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
			
			// Create a Rect same size as image
			srcRect = new Rect(0, 0, bitmapImg.getWidth(), bitmapImg.getHeight());
		}
		else if( ( image.getWidth() != imgWidth) 
				|| ( image.getHeight() != imgHeight) ){
			
			imgWidth = image.getWidth();
			imgHeight =  image.getHeight();
			
			// Initialize Mats
			mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
			mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );	
			
			bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
			srcRect.set(0, 0, bitmapImg.getWidth(), bitmapImg.getHeight());
		}

		// Applying the effect
		synchronized (mYUV_Mat) {
			// Put image data in mYUV_Mat
			mYUV_Mat.put(0, 0, image.getImageData());		
			
			// Convert YUV to RGB, put it in mRgb_Mat
			Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
		    
			if(enableBlur){
			// Apply Blur (doubles processing time)
				Imgproc.blur(mRgb_Mat, mRgb_Mat, kSize);
			}
			
			// Apply Canny (OpenCV native)
			// Commitable view to change thresholds?
			Imgproc.Canny(mRgb_Mat, mRgb_Mat, thresholdMin, thresholdMax);
			
			// Add the Canny Mat as Mask to image
			image.setMat("Mask", mRgb_Mat);
			
			// Convert mRgb_Mat to bitmap to be printed on Screen
			Utils.matToBitmap(mRgb_Mat, bitmapImg);
			System.gc();
		}
		
		view.postInvalidate();
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
		
		// Create a view over the camera, to display the image with Canny.
		view = new CannyView(Context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		Context.getLayout().addView(view,lp);
		
		WindowManager wm = (WindowManager) Context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		dstRect = new Rect(0,0, size.x, size.y);
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
	
	class CannyView extends View{
		Paint paint;
	
		public CannyView(Context Context) {
			super(Context);
			paint = new Paint();
		}

		protected void onDraw(Canvas canvas) {
			
			if(bitmapImg != null){		
				canvas.drawBitmap(bitmapImg, srcRect, dstRect, paint); 
				
			}
		}
	}
	
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
}