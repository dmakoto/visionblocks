package edu.mit.cameraCulture.vblocks.predefined;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class ColorBlobDetector extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Color Blob Detection";
	
	
	private int imgWidth;  
	private int imgHeight;
	
    private Scalar mLowerBound;
    private Scalar mUpperBound;
    
    private Mat mRgb_Mat;
    private Mat mPyrDownMat;
    private Mat mHsvMat;
    private Mat mMask;
    private Mat mDilatedMask;
    
    private Mat outPut_area;
    private Mat outPut_found;
    private Mat outPut_rect;

    private List<MatOfPoint> mContours;

    private Mat mHierarchy;
    
    private float minX, maxX, minY, maxY;
    private int[] pos = new int[2];
    private float[] posOutMin = new float[2];
    private float[] posOutMax = new float[2];    
    private float[] outArea = new float[1];
    
    
    private Mat mYuv;
    
    
    private ColorBlobView view;
    
	public ColorBlobDetector() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}

	
	@OutputBool( vars = {"Found"})
	@OutputMat( vars = {"RECT"})
	@OutputInt( vars = {"Area"})
	
	public ExecutionCode execute(Sample image) {
		
		mRgb_Mat = image.getRgbMat();
		
		synchronized (mRgb_Mat) {	 
			
			Imgproc.pyrDown(mRgb_Mat, mPyrDownMat);
	        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

	        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

	        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
	        Imgproc.dilate(mMask, mDilatedMask, new Mat());

	        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	        
	        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        
	        // Find max contour area
	        double maxArea = 0;
	        Iterator<MatOfPoint> each = contours.iterator();
	        while (each.hasNext()) {
	            MatOfPoint wrapper = each.next();
	            double area = Imgproc.contourArea(wrapper);
	            if (area > maxArea)
	                maxArea = area;
	        }
	        
	        mContours.clear();
	        if(maxArea > 10){      	
	        	
	        	outArea[0] = (float)maxArea;
	        	outPut_area.put(0, 0, outArea);
	        	image.setMat("Area", outPut_area);	        	
	        	//outPut_found.put(0, 0, 1);
	        	image.setMat("Found", outPut_found);
	        		        	
	        	
		        each = contours.iterator();
		        
		        
		        while (each.hasNext()) {
		            MatOfPoint contour = each.next();
		            if (Imgproc.contourArea(contour) > 0.9*maxArea) {
		                Core.multiply(contour, new Scalar(4,4), contour);
		                mContours.add(contour);
		                
		                contour.get(0, 0, pos);
		                minX = pos[0]; maxX = pos[0];  minY = pos[1]; maxY = pos[1];
       
		                for(int i = 1; i < contour.rows(); i++){
		                	
		                	contour.get(i, 0, pos);
			                if (minX > pos[0]) minX = pos[0]; 
			                if (maxX < pos[0]) maxX = pos[0];
			                //contour.get(i, 0, pos);
			                if (minY > pos[1]) minY = pos[1]; 
			                if (maxY < pos[1]) maxY = pos[1];
			                		                	
		                }		                
		                
		                //outPut_rect.put(0, 0, minX); outPut_rect.put(0, 1, minY);
		                //outPut_rect.put(1, 0, maxX); outPut_rect.put(1, 1, maxY);
			        	
		                posOutMin [0] = minX; posOutMin  [1] = minY;
		                outPut_rect.put(0, 0, posOutMin);
		                
		                posOutMax [0] = maxX; posOutMax  [1] = maxY;
		                outPut_rect.put(1, 0, posOutMax);
		                
		                //Log.d("debug", "rect = (" + posOutMin[0] + ", " + posOutMin[1] + "), (" + posOutMax[0] + "," + posOutMax[1] + ")");
		                
		                image.setMat("RECT", outPut_rect); 	
		                		                
		            }
		        }
	        }
	        else {
	        	
	        }
	            
	        
	        view.postInvalidate();
	        			
		}	
		
		return null;
	}

	@Override
	public String getName() {
		return "Color Blob";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub		
	}
	
	public static String getModuleName(){
		return "Color Blob";
	}
	
	public void onCreate(EngineActivity context){
		
		super.onCreate(context);
		 
	    mPyrDownMat = new Mat();
	    mHsvMat = new Mat();
	    mMask = new Mat();
	    mDilatedMask = new Mat();
		
	    mHierarchy = new Mat();
	    
	    mLowerBound = new Scalar(0, 100, 100);
	    mUpperBound = new Scalar(30, 255, 255);
	    
	    mContours = new ArrayList<MatOfPoint>();
	    
	    outPut_area = new Mat(1,1,CvType.CV_32FC1);
	    outPut_found = new Mat(1,1,CvType.CV_32FC1);
	    outPut_rect = new Mat(2,1,CvType.CV_32FC2);	    
	    
		view = new ColorBlobView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view,lp);
	    
	}
	
	class ColorBlobView extends View{
		
		Paint paint;
		
		public ColorBlobView(Context context) {
			super(context);
		
			paint = new Paint();
			paint.setARGB(255, 0, 255, 0);
			paint.setStrokeWidth(3);
			
		}

		protected void onDraw(Canvas canvas) {
			
			if(mYuv != null){
			
				
				float scaleH = canvas.getHeight()/imgHeight;
				float scaleW = canvas.getWidth()/imgWidth;
				
				float centerX = 0;
				float centerY = 0;
				
				for(int i = 0; i < mContours.size(); i++){
					
					centerX = 0;
					centerY = 0;
					
					Point[] points = mContours.get(i).toArray();
					int nbPoints = points.length;
					for( int j = 0 ; j < nbPoints ; j++ )
					{
						
					    Point v = points[j];
					    
					    centerX += v.x;
					    centerY += v.y;
					    
					}
					
					centerX /= nbPoints;
					centerY /= nbPoints;
					
					centerX *= scaleW;
					centerY *= scaleH;
					
					canvas.drawCircle(centerX, centerY, 30, paint);
					
				}
				
				
			}			
					
		}
	}
	
}


