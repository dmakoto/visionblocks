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

	private int imgWidth;  
	private int imgHeight;
	
	private Mat mYUV_Mat;
	private Mat mRgb_Mat;
	private Mat mIntermediate_Mat;
	
	private HistogramEqualizationView view;
	private Bitmap bitmapImg;
	
	private Rect srcRect; // Source Rectangle
	private Rect dstRect; // Destination Rectangle
	
	private int mode = 0;
	
	public HistogramEqualization() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExecutionCode execute(Sample image) {
		// TODO Auto-generated method stub
		if(mYUV_Mat == null){
			
			imgWidth = image.getWidth();
			imgHeight =  image.getHeight();		
			mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
			mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			mIntermediate_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
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
			mIntermediate_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			srcRect.set(0, 0, bitmapImg.getWidth(), bitmapImg.getHeight());
			
			bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
		}


		synchronized (mYUV_Mat) {
			// mode 0: YCrCb
			// mode 1: HSV
			List<Mat> channels = new ArrayList<Mat>();
			
			// Put image data in mYUV_Mat
			mYUV_Mat.put(0, 0, image.getImageData());
			
			switch(mode) {
			
			case 0:
		        Imgproc.cvtColor(mYUV_Mat,mRgb_Mat,Imgproc.COLOR_YUV420sp2RGB, 4);
		        Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat, Imgproc.COLOR_RGB2YCrCb, 4);
	
		        org.opencv.core.Core.split(mIntermediate_Mat,channels);
		        Imgproc.equalizeHist(channels.get(0), channels.get(0));
	
		        org.opencv.core.Core.merge(channels,mIntermediate_Mat);
	
		        Imgproc.cvtColor(mIntermediate_Mat,mRgb_Mat,Imgproc.COLOR_YCrCb2RGB, 4);
		        break;
		        
			case 1:
				Imgproc.cvtColor(mYUV_Mat,mRgb_Mat,Imgproc.COLOR_YUV420sp2RGB, 4);
		        Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat, Imgproc.COLOR_RGB2HSV, 4);
	
		        org.opencv.core.Core.split(mIntermediate_Mat,channels);
		        Imgproc.equalizeHist(channels.get(2), channels.get(2));
	
		        org.opencv.core.Core.merge(channels,mIntermediate_Mat);
	
		        Imgproc.cvtColor(mIntermediate_Mat,mRgb_Mat,Imgproc.COLOR_HSV2RGB, 4);
		        break;
		        
			case 2:
				Imgproc.cvtColor(mYUV_Mat,mRgb_Mat,Imgproc.COLOR_YUV420sp2RGB, 4);
		        Imgproc.cvtColor(mRgb_Mat, mIntermediate_Mat, Imgproc.COLOR_RGB2HSV, 4);
	
		        org.opencv.core.Core.split(mIntermediate_Mat,channels);
		        Imgproc.equalizeHist(channels.get(1), channels.get(1)); // Equalize the Saturation Channel too
		        Imgproc.equalizeHist(channels.get(2), channels.get(2));
	
		        org.opencv.core.Core.merge(channels,mIntermediate_Mat);
	
		        Imgproc.cvtColor(mIntermediate_Mat,mRgb_Mat,Imgproc.COLOR_HSV2RGB, 4);
		        break;
		        
			case 3:
				Imgproc.cvtColor(mYUV_Mat,mIntermediate_Mat,Imgproc.COLOR_YUV420sp2GRAY, 1);

				Imgproc.equalizeHist(mIntermediate_Mat, mIntermediate_Mat);
	
		        Imgproc.cvtColor(mIntermediate_Mat,mRgb_Mat,Imgproc.COLOR_GRAY2RGB, 4);
		        break;
		        
			default:
				break;
			}

			// Convert mRgb_Mat to bitmap to be printed on Screen
			Utils.matToBitmap(mRgb_Mat, bitmapImg);
			System.gc();
		}
		
		view.postInvalidate();		
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
	public	void onCreate(EngineActivity Context) {
		super.onCreate();
		
		view = new HistogramEqualizationView(Context);
		
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		Context.getLayout().addView(view,lp);
		
		// Configure view to become invisible when toching
		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN: // PRESSED
					Log.d("SurfaceView", "Down");
					v.setVisibility(View.INVISIBLE);
					return true; // if you want to handle the touch event
				case MotionEvent.ACTION_UP: // RELEASED
					Log.d("SurfaceView", "Up");
					v.setVisibility(View.VISIBLE);
					return true; // if you want to handle the touch event
				}

				return false;
			}
		});
		WindowManager wm = (WindowManager) Context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		dstRect = new Rect(0,0, size.x, size.y);
	}
	
	public static final Parcelable.Creator<HistogramEqualization> CREATOR = new
			Parcelable.Creator<HistogramEqualization>() {
		@Override
		public HistogramEqualization createFromParcel(Parcel in) {
			Log.v("ParcelableTest","Creating from parcel");
			return new HistogramEqualization();
		}

		public HistogramEqualization[] newArray(int size) {
			return new HistogramEqualization[size];
		}

	};
	
	class HistogramEqualizationView extends View{
		Paint paint;
	
		public HistogramEqualizationView(Context Context) {
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
		return new HistogramEqualizationConfig(context, this);
	}
	
	public void setMode(int m){
		this.mode = m;
	}
	
	public int getMode(){
		return this.mode;
	}
}
