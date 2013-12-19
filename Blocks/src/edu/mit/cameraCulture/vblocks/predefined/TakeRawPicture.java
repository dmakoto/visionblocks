package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.Module.ExecutionCode;
import edu.mit.cameraCulture.vblocks.utils.Storage;

public class TakeRawPicture extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Take Raw Picture";
	
	private Bitmap bitmapImg;
	
	// Mat used by the module
	Mat mYUV_Mat;
	
	// Image Properties
	private int imgWidth; 
	private int imgHeight;
	
	public TakeRawPicture() {
		super(REGISTER_SERVICE_NAME);
	}
	
	public ExecutionCode execute(Sample image) {
		//convert to bitmap and save
		
		// Initialize an instance of Storage, used to save the image in the phone
		Storage mStorage = new Storage(this.mContext);
		mStorage.save(image.getImageData());
		imgWidth = image.getWidth();
		imgHeight =  image.getHeight();
		mYUV_Mat = image.getYUVMat();
		
		// Transform mYUV_Mat to RGB. Since YUV Mat is unchanged, it has the raw image.
		Mat mRgb_Mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4 );
		Imgproc.cvtColor(mYUV_Mat, mRgb_Mat, Imgproc.COLOR_YUV420sp2RGB, 4);
		
		bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
		
		// Convert mRgb_Mat to bitmap
		Utils.matToBitmap(mRgb_Mat, bitmapImg);
		mStorage.save(bitmapImg);
		
		System.gc();
		return null;
	}
	
	public String getName() {
		return "Take Raw Picture";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub		
	}
	
	public static String getModuleName(){
		return "Take Raw Picture";
	}
	
	public void onCreate(EngineActivity context){
		super.onCreate(context);
	}
}