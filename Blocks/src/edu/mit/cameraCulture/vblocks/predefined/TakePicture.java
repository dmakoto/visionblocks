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
import edu.mit.cameraCulture.vblocks.utils.Storage;

public class TakePicture extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Take Picture";
	
	private Bitmap bitmapImg;
	
	// Mat used by the module
	Mat mRgb_Mat;
	
	// Image Properties
	private int imgWidth; 
	private int imgHeight;
	
	public TakePicture() {
		super(REGISTER_SERVICE_NAME);
	}
	
	public ExecutionCode execute(Sample image) {
		//convert to bitmap and save
		
		// Initialize an instance of Storage, used to save the image in the phone
		Storage mStorage = new Storage(this.mContext);
		mStorage.save(image.getImageData());
		imgWidth = image.getWidth();
		imgHeight =  image.getHeight();
		
		mRgb_Mat = image.getRgbMat();
		
		bitmapImg = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
		
		// Convert mRgb_Mat to bitmap
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
		
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}

}