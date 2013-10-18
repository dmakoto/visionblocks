package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Calendar;

import org.opencv.core.Mat;

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
	private android.hardware.Camera mCamera;
	
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
