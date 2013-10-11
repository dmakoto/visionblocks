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

	
	public class Blur extends Module {

	
	public static final String REGISTER_SERVICE_NAME = "Blur";
	
	private Size kSize;
	private int blurSize = 20;

	private int imgWidth;  
	private int imgHeight;
	
	private Mat mYUV_Mat;
	private Mat mRgb_Mat;
	
	private BlurView view;
	private Bitmap bitmapImg;
	
	
	private Rect srcRect;
	private Rect dstRect;
	
	
	public Blur() {
		super(REGISTER_SERVICE_NAME);	
		
	}
	
	
	public ExecutionCode execute(Sample image) {
		
		kSize.height = blurSize;
		kSize.width = blurSize;
		
		if((image.getWidth() > 0) && (image.getHeight() > 0) ){
		
		if(mYUV_Mat == null){
			
			imgWidth = image.getWidth();
			imgHeight =  image.getHeight();		
			mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
			mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			
			bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
			srcRect = new Rect(0, 0, bitmapImg.getWidth(), bitmapImg.getHeight());
			
		}
		else if( ( image.getWidth() != imgWidth) 
				|| ( image.getHeight() != imgHeight) ){
			
			imgWidth = image.getWidth();
			imgHeight =  image.getHeight();
			
			mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
			mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );	
			
			bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
			srcRect.set(0, 0, bitmapImg.getWidth(), bitmapImg.getHeight());
		
		}
		
		
		synchronized (mYUV_Mat) {	 
			mYUV_Mat.put(0, 0, image.getImageData());			
			Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
		    
			Imgproc.blur(mRgb_Mat, mRgb_Mat, kSize);			
			
			Utils.matToBitmap(mRgb_Mat, bitmapImg);		
			System.gc();					
			
		}			
		view.postInvalidate();
		}
		
		return null;
		
	}
	
	
	@Override
	public String getName() {
		return "Blur";
	}	
	
	public static String getModuleName(){
		return "Blur";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
	}
	
	public void onCreate(EngineActivity context){
		
		super.onCreate(context);
		
		kSize = new Size(20,20);
		
		view = new BlurView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view,lp);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		dstRect = new Rect(0,0, size.x, size.y);
		
		
	}
	
	public CommitableView getConfigurationView(Context context) {
		return new BlurConfig(context, this);
	}
	
	public int getBlurSize(){return blurSize;}
	public void setBlurSize(int size){
		blurSize = size;
	}
	
	public void onDestroyModule() {
		super.onDestroyModule();
	}
	
	public static final Parcelable.Creator<Blur> CREATOR = new
			  Parcelable.Creator<Blur>() {
			      public Blur createFromParcel(Parcel in) {
			          Log.v("ParcelableTest","Creating from parcel");
			              return new Blur();
			      }

			      public Blur[] newArray(int size) {
			              return new Blur[size];
			      }
			     
	};
	
	class BlurView extends View{
		
		private Paint paint; 
		
		public BlurView(Context context) {
			super(context);
			paint = new Paint();	
			
		}

		protected void onDraw(Canvas canvas) {
			
			if(bitmapImg != null){		
									
				canvas.drawBitmap(bitmapImg, srcRect, dstRect, paint); 
				
			}
					
		}
		
				
		
	}
	

}
