package edu.mit.cameraCulture.vblocks.predefined;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera.Parameters;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

public class Camera extends Module implements android.hardware.Camera.PreviewCallback{

	public static final String REGISTER_SERVICE_NAME = "CameraBlock"; 
	private CameraPreview mPreview;
	private byte [] mCurrentFrame;
	private int mFrameWidth;
	private int mFrameHeight;
	
	private final Object lock = new Object();
	
	public Camera(){
		super(REGISTER_SERVICE_NAME);
	}
	
	public void takePicture() {
		mPreview.takePicture();
	}
	
	@Override
	public void onCreate(EngineActivity context){
		super.onCreate(context);
		mPreview = new CameraPreview(context);
		context.getLayout().addView(mPreview);
	}
	
	@Override
	public void onDestroyModule(){
		mContext.getLayout().removeView(mPreview);
		super.onDestroyModule();
	}
	
	@Override
	public ExecutionCode execute(Sample image) {
		synchronized(lock) {
			try {
				lock.wait();
				// Sets the image data
				image.setImageData(mCurrentFrame, mFrameWidth, mFrameHeight);
			} catch (InterruptedException e) {
				Log.e(REGISTER_SERVICE_NAME,e.getMessage());
			}
			if(image.getMat("TAKE_RAW_PICTURE") != null) {
				image.removeMat("TAKE_RAW_PICTURE");
				this.takePicture();
				Log.d("Camera", "TAKE_RAW_PICTURE");
			}
		}
		return ExecutionCode.NONE;
	}
	

	@Override
	public String getName() {
		return getModuleName();
	}
	
	public static String getModuleName(){
		return "Camera";
	}

	//@Override
	public int describeContents() {
		return 0;
	}

	//@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		
	}
	
	public static final Parcelable.Creator<Camera> CREATOR = new
			  Parcelable.Creator<Camera>() {
			      public 	Camera createFromParcel(Parcel in) {
			          Log.v("ParcelableTest","Creating from parcel");
			              return new Camera();
			      }
			      public Camera[] newArray(int size) {
			              return new Camera[size];
			      }
	};
	
	private static android.hardware.Camera getCameraInstance(){
		 	android.hardware.Camera c = null;
	        try {
	            c =  android.hardware.Camera.open(); // attempt to get a Camera instance
	        }
	        catch (Exception e){
	            Log.e(REGISTER_SERVICE_NAME, "Camera - unable to initialize camera. \n" + e.getMessage());
	        }
	        return c; // returns null if camera is unavailable
	 }

	@Override
	public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
		Parameters ps = camera.getParameters();
		mCurrentFrame = data;
		mFrameHeight = ps.getPreviewSize().height;
		mFrameWidth = ps.getPreviewSize().width;
		synchronized(lock) {
			lock.notify();
		}
	}
	
//	protected void setupCamera()
//	   {
//	      Log.v( TAG, "GraphicalActivity: setupCamera()");
//	      // Now that the size is known, set up the camera parameters and begin
//	      // the preview.
//	      android.hardware.Camera.Parameters parameters = mCamera.getParameters();
//	      List<android.hardware.Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
//	      android.hardware.Camera.Size list = sizes.get(0);
//	      mDesiredWidth = list.width;
//	      mDesiredHeight = list.height;
//	      for (android.hardware.Camera.Size s: sizes)
//	      {   if (s.height == 480)  
//	           {
//	    	     mDesiredWidth = s.width;
//	    	     mDesiredHeight = s.height;
//	    	     break;
//	           }
//	      }
//
//	      parameters.setPreviewFrameRate( 30 );
//	      parameters.setPreviewSize( mDesiredWidth, mDesiredHeight );
//	      //
//	      //  Set Focus mode depending on what is supported. MODE_INFINITY is 
//	      //  preferred mode.
//	      // 
//	      List<String> supportedFocusModes = parameters.getSupportedFocusModes();
//	      if( supportedFocusModes!= null ) 
//	      {
//	         if( supportedFocusModes.contains
//	             (
//	            		 android.hardware.Camera.Parameters.FOCUS_MODE_INFINITY
//	             ) )
//	         {
//	            parameters.setFocusMode( android.hardware.Camera.Parameters.FOCUS_MODE_INFINITY );
//	            Log.v( TAG, "Set focus mode INFINITY" );
//	         }
//	         else if( supportedFocusModes.contains
//	                  (
//	                		  android.hardware.Camera.Parameters.FOCUS_MODE_FIXED
//	                  ) )
//	         {
//	            parameters.setFocusMode( android.hardware.Camera.Parameters.FOCUS_MODE_FIXED );
//	            Log.v( TAG, "Set focus mode FIXED" );
//	         }
//	         else if( supportedFocusModes.contains
//	                  (
//	                		  android.hardware.Camera.Parameters.FOCUS_MODE_AUTO
//	                  ) )
//	         {
//	            parameters.setFocusMode( android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
//	            Log.v( TAG, "Set focus mode AUTO" );
//	         }
//	      }
//	      //
//	      // Set White Balance to Auto if supported.
//	      // 
//	      List<String> supportedWhiteBalance = 
//	         parameters.getSupportedWhiteBalance();
//	      if( supportedWhiteBalance != null &&
//	          supportedWhiteBalance.contains
//	          (
//	        	android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO
//	          ) )
//	      {
//	         parameters.setWhiteBalance( android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO );
//	         Log.v( TAG, "Set white balance AUTO" );
//	      }
//
//	      try
//	      {
//	         mCamera.setParameters( parameters );
//	      }
//	      catch( RuntimeException re )
//	      {
//	         //
//	         // NOTE that we shouldn't be here as we check where our specified 
//	         // parameters are supported or not. 
//	         // 
//	         re.printStackTrace();
//	         Log.e( TAG,"Unable to set Camera Parameters" );
//	         Log.i( TAG,"Falling back to setting just the camera preview" );
//	         parameters = mCamera.getParameters();
//	         parameters.setPreviewSize( mDesiredWidth, mDesiredHeight );         
//	         try
//	         {
//	            mCamera.setParameters( parameters );
//	         }
//	         catch( RuntimeException re2 )
//	         {
//	            re2.printStackTrace();
//	            Log.e( TAG, "Problem with camera configuration, unable to set "+ 
//	                   "Camera Parameters. Camera not available." );
//	            
//	         }
//	      }
//
//	      mPreviewWidth = mCamera.getParameters().getPreviewSize().width;
//	      mPreviewHeight = mCamera.getParameters().getPreviewSize().height;
//
//	   }
	
	
	private android.hardware.Camera.PreviewCallback getCameraCallback(){
		return this;
	}
	
	public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
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
    
    public static void decodeYUV420SPtoRGB(byte[] rgb, byte[] yuv420sp, int width, int height) {
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

	            int it = yp*4;
	            rgb[it+3] = (byte)0xff;
	            rgb[it+2] = (byte)((r >> 10) & 0xff);
	            rgb[it+1] = (byte)((g >> 10) & 0xff); 
	            rgb[it] = (byte)((b >> 10) & 0xff);
	        }
	    }
	}
    
    public static void decodeYUV420SPMono(int[] rgb, byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;
	    	
	    for (int yp = 0; yp < frameSize; yp++) {
	        int p = (0xff & ((int) yuv420sp[yp]));
	        rgb[yp] = Color.rgb(p, p, p);
	    }
	}

	
	
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		public static final String TAG = "FILTER_PREVIEW"; 
	 	private SurfaceHolder mHolder;
	    private android.hardware.Camera mCamera;
	    private Context mContext;
	    private byte [] bimage = null;
	    
	    public ArrayList<Module> list = null;
	    
	    public CameraPreview(Context context) {
	        super(context);
	        mContext = context;
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	            mCamera = getCameraInstance();
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	            Parameters ps = mCamera.getParameters();
	            if(bimage == null){
					bimage = new byte[ps.getPreviewSize().width* ps.getPreviewSize().height * 3];
				}
	            mCamera.addCallbackBuffer(bimage);
	            mCamera.setPreviewCallbackWithBuffer(getCameraCallback());
	        } catch (IOException e) {
	            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	    	mCamera.stopPreview();
	    	mCamera.setPreviewCallback(null);
	    	mCamera.release();
	    }
	    
	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // If your preview can change or rotate, take care of those events here.
	        // Make sure to stop the preview before resizing or reformatting it.

	    	
	        if (mHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	        }

	        // stop preview before making changes
	        try {
	            mCamera.stopPreview();
	            mCamera.setPreviewCallback(null);
	        } catch (Exception e){
	          // ignore: tried to stop a non-existent preview
	        }

	        // set preview size and make any resize, rotate or
	        // reformatting changes here

	        Parameters parameters = mCamera.getParameters();
	        
	        Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
	        
	        // TODO Rotate display onRotation Changed

	        //mCamera.setDisplayOrientation(90);
	        // start preview with new settings
	        try {
	        	mCamera.setPreviewDisplay(mHolder);
	            mCamera.setPreviewCallback(getCameraCallback());
	            mCamera.startPreview();
	        } catch (Exception e){
	            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
	        }
	    }
	    
	    /** Check if this device has a camera */
	    private boolean checkCameraHardware(Context context) {
	        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	            // this device has a camera
	            return true;
	        } else {
	            // no camera on this device
	            return false;
	        }
	    }
		public void takePicture() {
			mCamera.takePicture(null, null,
			        new PhotoHandler(mContext));
		}
	}



	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}
}
