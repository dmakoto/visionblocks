package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Calendar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.graphics.Bitmap;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.utils.Storage;

public class TakePicture extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Take Picture";
	
	private Calendar calend;
	private Bitmap bitmapImg;
	
	// Mat used by the module
	Mat mYUV_Mat;
	Mat mRgb_Mat;
	Mat mGray_Mat;
	Mat mEdges_Mat;
	
	// Image Properties
	private int imgWidth; 
	private int imgHeight;
	
	public TakePicture() {
		super(REGISTER_SERVICE_NAME);
	}
	
	public ExecutionCode execute(Sample image) {
		//convert to bitmap and save
		
		Storage mStorage = new Storage(this.mContext);
		mStorage.save(image.getImageData());
		imgWidth = image.getWidth();
		imgHeight =  image.getHeight();		
		
		mYUV_Mat = new Mat(imgHeight + imgHeight / 2, imgWidth, CvType.CV_8UC1);
		mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
		
		bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
		
		// Put image data in mYUV_Mat
		mYUV_Mat.put(0, 0, image.getImageData());		
		
		// Convert YUV to RGB, put it in mRgb_Mat
		Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
		
		// Convert mRgb_Mat to bitmap to be printed on Screen
		Utils.matToBitmap(mRgb_Mat, bitmapImg);
		
		mStorage.save(bitmapImg);
		
		System.gc();
		return null;
	}
	
	public String getName() {
		return "Take Picture";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub		
	}
	
	public static String getModuleName(){
		return "Take Picture";
	}
	
	public void onCreate(EngineActivity context){
		super.onCreate(context);
		calend = Calendar.getInstance();
		
	}

}