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
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.predefined.Blur.BlurView;
import edu.mit.cameraCulture.vblocks.predefined.Canny.CannyView;

public class EffectBase extends Module {

	
	public static final String REGISTER_SERVICE_NAME = "Pixelize";

	private int imgWidth;  
	private int imgHeight;
	
	private Mat mYUV_Mat;
	private Mat mRgb_Mat;
	private Mat mIntermediate_Mat;
	
	private PixelizeView view;
	private Bitmap bitmapImg;
	
	private Rect srcRect; // Source Rectangle
	private Rect dstRect; // Destination Rectangle
	
	public EffectBase() {
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
			// Put image data in mYUV_Mat
			mYUV_Mat.put(0, 0, image.getImageData());		
			
			// Convert YUV to RGB, put it in mRgb_Mat
			Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
			
			// Apply Pixelize by resizing mRgb_Mat to a smaller size, and then
			// resizing it again to the original size.
			// resize is OpenCV native.
			Imgproc.resize(mRgb_Mat, mIntermediate_Mat, new Size(0,0), 0.1, 0.1, Imgproc.INTER_NEAREST);
	        Imgproc.resize(mIntermediate_Mat, mRgb_Mat, mRgb_Mat.size(), 0., 0., Imgproc.INTER_NEAREST);
			
			// Convert mRgb_Mat to bitmap to be printed on Screen
			Utils.matToBitmap(mRgb_Mat, bitmapImg);
			System.gc();
		}
		
		view.postInvalidate();		
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
		
		view = new PixelizeView(Context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		Context.getLayout().addView(view,lp);
		
		WindowManager wm = (WindowManager) Context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		dstRect = new Rect(0,0, size.x, size.y);
	}
	
	public static final Parcelable.Creator<EffectBase> CREATOR = new
			  Parcelable.Creator<EffectBase>() {
			      public EffectBase createFromParcel(Parcel in) {
			          Log.v("ParcelableTest","Creating from parcel");
			              return new EffectBase();
			      }

			      public EffectBase[] newArray(int size) {
			              return new EffectBase[size];
			      }
			     
	};
	
	class PixelizeView extends View{
		Paint paint;
	
		public PixelizeView(Context Context) {
			super(Context);
			paint = new Paint();
		}

		protected void onDraw(Canvas canvas) {
			
			if(bitmapImg != null){		
				canvas.drawBitmap(bitmapImg, srcRect, dstRect, paint); 
			}
		}
	}

}
