package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class ScreenOutput extends Module {

	public static final String REGISTER_SERVICE_NAME = "OutputModule";
	private ImageView mImageView;
	private int width;
	private int height;
	Bitmap bitmapImg;
	private Mat mRgb_Mat;
	
	public ScreenOutput() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	public void onCreate(EngineActivity context){
		super.onCreate(context);
		mImageView = new ImageView(context);
		mImageView.setScaleType(ScaleType.FIT_XY);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		RelativeLayout l = context.getLayout();
		l.addView(mImageView, l.getChildCount() > 0 ? 1:0,lp); 
	}
	
	@Override
	public void onDestroyModule(){
		super.onDestroyModule();
	}
	
	@Override
	public ExecutionCode execute(Sample image) {
		mRgb_Mat = image.getRgbMat();
		width = image.getWidth();
		height = image.getHeight();
		
		mImageView.post(new Runnable() {
			@Override
			public void run() {
				synchronized (mRgb_Mat) {
					// Convert mRgb_Mat to bitmap to be printed on Screen
					bitmapImg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(mRgb_Mat, bitmapImg);
					mImageView.setImageBitmap(bitmapImg);
				}
			}
		});
		return null;
	}
	
	private static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;

	    for (int j = 0, yp = 0; j < height; j++) {
	        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	        for (int i = 0; i < width; i++, yp++) {
	            int y = (0xff & ((int) yuv420sp[yp])) - 16;
	            if (y < 0) y = 0;
	            if ((i & 1) == 0) {
	                v = (0xff & yuv420sp[uvp++]) - 128;
	                u = (0xff & yuv420sp[uvp++]) - 128;
	            }
	            int y1192 = 1192 * y;
	            int r = (y1192 + 1634 * v);
	            int g = (y1192 - 833 * v - 400 * u);
	            int b = (y1192 + 2066 * u);

	            if (r < 0) r = 0; else if (r > 262143) r = 262143;
	            if (g < 0) g = 0; else if (g > 262143) g = 262143;
	            if (b < 0) b = 0; else if (b > 262143) b = 262143;

	            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	        }
	    }
	}

	@Override
	public String getName() {
		return getModuleName();
	}
	
	public static String getModuleName(){
		return REGISTER_SERVICE_NAME;
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}

}
