package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;


public class OpticalFlow extends Module {
	
	class BorderView extends View {
		private Rect rect = new Rect();
	    private Point mStartingPoint = new Point();
	    private Point mEndingPoint = new Point();
		private Paint p = new Paint();
		private Paint pTracked = new Paint();
		private Paint pTrackedFill = new Paint();
		private boolean isValid = false; 
		
		private BorderView getView(){
	    	return this;
	    }
	    
	    public BorderView(Context context) {
	        super(context);
	        p.setColor(Color.RED);
	        p.setStrokeWidth(2);
	        pTracked.setColor(Color.GREEN);
	        pTracked.setStrokeWidth(3);
	        pTrackedFill.setColor(Color.argb(0x44, 0, 0xff, 0));
	        this.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(mImageView.getVisibility() != GONE){
						mImageView.setVisibility(GONE);
						return true;
					}
					if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
						isValid = false;
						mStartingPoint.x = (int)event.getX();
						mStartingPoint.y = (int)event.getY();
					} if(event.getAction() == android.view.MotionEvent.ACTION_UP) {
						isValid = true;
					}
					
					if(mStartingPoint.x == 0 && mStartingPoint.y == 0){
						isValid = false;
						return true;
					}
			
					
					mEndingPoint.x = (int)event.getX();
					mEndingPoint.y = (int)event.getY();
					rect.left = Math.min(mStartingPoint.x,mEndingPoint.x);
					rect.top = Math.min(mStartingPoint.y, mEndingPoint.y);
					rect.right = Math.max(mStartingPoint.x,mEndingPoint.x);
					rect.bottom = Math.max(mStartingPoint.y, mEndingPoint.y);
					getView().invalidate();
					return true;
				}
			});
	    }
	    
	    public boolean isValid(){
	    	return isValid;
	    }
	    
	    public Rect getRegion(){
	    	return rect;
	    }
	    
	    public void setRegion(float x1, float y1, float x2, float y2){
			rect.left = (int)(Math.min(x1,x2)*this.getWidth());
			rect.top = (int)(Math.min(y1,y2)*this.getHeight());
			rect.right = (int)(Math.max(x1,x2)*this.getWidth());
			rect.bottom = (int)(Math.max(y1,y2)*this.getHeight());
			this.invalidate();
	    }

	    @Override
	    protected void onDraw(Canvas canvas){
	    	if(isValid){
	    		canvas.drawRect(rect, pTrackedFill);
	    	}
	    	canvas.drawLines(new float[]{
	    		    rect.left, rect.top, rect.right, rect.top, 
	    		    rect.right, rect.top, rect.right, rect.bottom,
	    		    rect.right, rect.bottom, rect.left, rect.bottom,
	    		    rect.left, rect.bottom, rect.left, rect.top}
	    		    , 0, 16, isValid?pTracked: p);
	    	
	    }

	}
	
	
	public static final String REGISTER_SERVICE_NAME = "OpticalFlow";
	private BorderView mArea;
	private ImageView mImageView;
	private float [] frect = new float[4]; 
    
	
	public OpticalFlow() {
		super(REGISTER_SERVICE_NAME);
	}
	
	@Override
	public void onCreate(EngineActivity context){
		super.onCreate(context);
		
		mImageView = new ImageView(context);
		mImageView.setImageResource(R.drawable.drawing_helper);
		context.getLayout().addView(mImageView);
		
		mArea = new BorderView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(mArea,lp);
	}
	
	@Override
	public void onDestroyModule(){
		super.onDestroyModule();
		clean();
	}
	
	@ImagePixelType( encoding = PixelEncoding.yuv420 )
	@OutputBool( vars = {"OUT_OF_BOUNDS"})
	@OutputMat( vars = {"RECT"})
	@OutputInt( vars = {"Int"})
	@Override
	public ExecutionCode execute(Sample image) {

		if(mArea.isValid()){
			Rect r = mArea.getRegion();
			frect[0] = ((float)r.left)/mArea.getWidth();
			frect[1] = ((float)r.top)/mArea.getHeight();
			frect[2] = ((float)r.right)/mArea.getWidth();
			frect[3] = ((float)r.bottom)/mArea.getHeight();
			
			Mat mYUV_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
			Imgproc.cvtColor(image.getRgbMat(), mYUV_Mat, Imgproc.COLOR_RGB2YUV_I420);

			byte[] imgData = new byte[image.getImageData().length];
			
			mYUV_Mat.get(0,0,imgData);
			
			processImage(imgData,image.getWidth(),image.getHeight(),frect);
			
			// Works, but is not using the new information
//			 processImage(image.getImageData(),image.getWidth(),image.getHeight(),frect);

			Mat m = new Mat(2,2,CvType.CV_32FC1);
			m.put(0, 0, frect);
			image.setMat("RECT", m);

			float mid_x = (frect[0] + frect[2])/2;
			float mid_y = (frect[1] + frect[3])/2;
			
			if(mid_x < 0 || mid_y < 0 || mid_x > 1 || mid_y >1){
				image.setMat("OUT_OF_BOUNDS", new Mat());
				Log.d("Test", "OUT_OF_BOUNDS");
			}
			
			if(frect[3]>0)
			mArea.post(new Runnable() {
				
				@Override
				public void run() {
					mArea.setRegion(frect[0], frect[1],frect[2], frect[3]);
					
				}
			});
		}
		return ExecutionCode.NONE;
	}
	
	private native int processImage(byte [] data, int  width, int height, float [] rect); 
	private native int clean();
	
	
	@Override
	public String getName(){
		return getModuleName();
	}
	
	public static String getModuleName(){
		return "Object Tracker";
	}
	
	//@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	//@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<OpticalFlow> CREATOR = new
			  Parcelable.Creator<OpticalFlow>() {
			      public OpticalFlow createFromParcel(Parcel in) {
			          Log.v("ParcelableTest","Creating from parcel");
			              return new OpticalFlow();
			      }

			      public OpticalFlow[] newArray(int size) {
			              return new OpticalFlow[size];
			      }
			     
	};

	@Override
	protected void onHandleIntent(Intent intent) {
		
	}
}
