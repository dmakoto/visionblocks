package edu.mit.cameraCulture.vblocks.predefined.augmenter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.Module;

public class AugmenterConfigurationNew extends CommitableView {

	private static final int FILE_SELECT_CODE = 0;

	final EditText txtEdit;
	
	public class ForwarderActivity extends Activity {
	    private boolean started = false;
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        if (!started) {
	            started = true;
	    		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
		        intent.setType("*/*"); 
		        intent.addCategory(Intent.CATEGORY_OPENABLE);

		        try {
		            this.startActivityForResult(
		                    Intent.createChooser(intent, "Select a File to Upload"),
		                    FILE_SELECT_CODE);	
		            
		        } catch (android.content.ActivityNotFoundException ex) {
		            // Potentially direct the user to the Market with a Dialog
		            Toast.makeText(this, "Please install a File Manager to be able browse file system.", 
		                    Toast.LENGTH_SHORT).show();
		        }

	        }
	    }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	System.out.println("bla");
	        finish();
	    }
	}
	
	private Augmenter mModule;
	//private CameraPreview mPreview;
	
	public AugmenterConfigurationNew(Context context, Augmenter module) {
		super(context);
		mModule = module;
		final Context cntxt = context; 
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		TextView txtLabel = new TextView(context);
		txtLabel.setText("Choose model:");
		this.addView(txtLabel,params);
		
		txtEdit = new EditText(context);
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		txtEdit.setClickable(true);
		txtEdit.setOnFocusChangeListener( new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.d("TEST","TEST");
				
			}
		});
		txtEdit.setText(module.getModelPath());
		txtEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final FileChooserDialog dialog = new FileChooserDialog(cntxt);
				dialog.setIFolderItemListener(new FileChooserDialog.IFolderItemListener() {
					
					@Override
					public void OnFileSelected(String file) {
						txtEdit.setText(file);
						
					}
					
					@Override
					public void OnCannotFileRead(File file) {
						// TODO Auto-generated method stub
						
					}
				});
				
				dialog.show();
			}
		});
		
		this.addView(txtEdit,params);
		
//		mPreview = //new CameraPreview(context);
//		
//		
//		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//		
//		LinearLayout.LayoutParams params;
//		switch(display.getRotation()){
//			case Surface.ROTATION_90:
//			case Surface.ROTATION_270:
//				params = new LinearLayout.LayoutParams((int)mPreview.getCameraWidth(),(int)mPreview.getCameraHeight());
//				break;
//			default:
//				params = new LinearLayout.LayoutParams((int)mPreview.getCameraHeight(),(int)mPreview.getCameraWidth());
//				break;
//		}
		
//		this.addView(mPreview,params);
	}

	@Override
	public void commit() {
		mModule.setModelPath(txtEdit.getText().toString());
	}
	
	
	
//	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
//		public static final String TAG = "FILTER_PREVIEW"; 
//	 	private SurfaceHolder mHolder;
//	    private android.hardware.Camera mCamera;
//	    private Context mContext;
//	    private byte [] bimage = null;
//	    
//	    private float mHeight;
//	    private float mWidth;
//	    
//	    public float getCameraWidth(){
//	    	return mWidth;
//	    } 
//	    
//	    public float getCameraHeight(){
//	    	return mHeight;
//	    }
//	    
//	    public ArrayList<Module> list = null;
//	    
//	    public CameraPreview(Context context) {
//	        super(context);
//	        try {
//	            mCamera = android.hardware.Camera.open();
//	            Parameters ps = mCamera.getParameters();
//	            mWidth = ps.getPreviewSize().width;
//				mHeight = ps.getPreviewSize().height;
//				mCamera.release();
//	        } catch (Exception e) {}
//	        mContext = context;
//	        mHolder = getHolder();
//	        mHolder.addCallback(this);
//	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//	    }
//	    
//	    public void surfaceCreated(SurfaceHolder holder) {
//	        // The Surface has been created, now tell the camera where to draw the preview.
//	        try {
//	            mCamera = android.hardware.Camera.open();
//	            mCamera.setPreviewDisplay(holder);
//	            mCamera.startPreview();
//	            Parameters ps = mCamera.getParameters();
//	            if(bimage == null){
//					bimage = new byte[ps.getPreviewSize().width* ps.getPreviewSize().height * 3];
//				}
//	            mCamera.addCallbackBuffer(bimage);
//	            //mCamera.setPreviewCallbackWithBuffer(getCamereCallback());
//	        } catch (IOException e) {
//	            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
//	        }
//	    }
//
//	    public void surfaceDestroyed(SurfaceHolder holder) {
//	        mCamera.stopPreview();
//	    	mCamera.setPreviewCallback(null);
//	    	mCamera.release();
//	    }
//	    
//	    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//	        if (mHolder.getSurface() == null){
//	          // preview surface does not exist
//	          return;
//	        }
//
//	        // stop preview before making changes
//	        try {
//	            mCamera.stopPreview();
//	            mCamera.setPreviewCallback(null);
//	        } catch (Exception e){
//	          // ignore: tried to stop a non-existent preview
//	        }
//
//	        // set preview size and make any resize, rotate or
//	        // reformatting changes here
//
//	        Parameters parameters = mCamera.getParameters();
//	        
//	        Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
//	        
//	        if(display.getRotation() == Surface.ROTATION_0)
//	        {
//	            parameters.setPreviewSize(height, width);                           
//	            mCamera.setDisplayOrientation(90);
//	        }
//
//	        if(display.getRotation() == Surface.ROTATION_90)
//	        {
//	            parameters.setPreviewSize(width, height);                           
//	        }
//
//	        if(display.getRotation() == Surface.ROTATION_180)
//	        {
//	            parameters.setPreviewSize(height, width);               
//	        }
//
//	        if(display.getRotation() == Surface.ROTATION_270)
//	        {
//	            parameters.setPreviewSize(width, height);
//	            mCamera.setDisplayOrientation(180);
//	        }
//	     //   mCamera.setParameters(parameters);
//	        // start preview with new settings
//	        try {
//	        	mCamera.setPreviewDisplay(mHolder);
//	            //mCamera.setPreviewCallback(getCamereCallback());
//	            mCamera.startPreview();
//	        } catch (Exception e){
//	            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//	        }
//	    }
//	    
//	    /** Check if this device has a camera */
//	    private boolean checkCameraHardware(Context context) {
//	        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//	            // this device has a camera
//	            return true;
//	        } else {
//	            // no camera on this device
//	            return false;
//	        }
//	    }
//}

}
