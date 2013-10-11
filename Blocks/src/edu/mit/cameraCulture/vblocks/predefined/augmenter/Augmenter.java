/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

@file
    Augmenter.java

@brief
    Sample for Augmenter

==============================================================================*/

package edu.mit.cameraCulture.vblocks.predefined.augmenter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qualcomm.QCAR.QCAR;

import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;


/** The main activity for the Augmenter sample. */
public class Augmenter extends Module {

	public static final String REGISTER_SERVICE_NAME = "Augmenter";

    // Application status constants:
	private static final int APPSTATUS_UNINITED = -1;

	private static final int APPSTATUS_INIT_APP = 0;
    private static final int APPSTATUS_INIT_QCAR = 1;
    private static final int APPSTATUS_INIT_TRACKER = 2;
    private static final int APPSTATUS_INIT_APP_AR = 3;
    private static final int APPSTATUS_LOAD_TRACKER = 4;
    private static final int APPSTATUS_INITED = 5;
    private static final int APPSTATUS_CAMERA_STOPPED = 6;
    private static final int APPSTATUS_CAMERA_RUNNING = 7;

    // Application current UI Status
    private static final int UISTATUS_SCANNING_MODE = 0;
    private static final int UISTATUS_BUILD_TARGET_MODE = 1;

    // FOCUS MODES
    private static final int FOCUS_MODE_NORMAL = 0;
    private static final int FOCUS_MODE_CONTINUOUS_AUTO = 1;

    // Name of the native dynamic libraries to load:
    private static final String NATIVE_LIB_APP = "vision_blocks";
    private static final String NATIVE_LIB_QCAR = "QCAR";

    // Manages displaying instructions screen only first time
    private static final String SHOW_PREFERENCES = "show_preferences";

    // Constants for Hiding/Showing Loading dialog
    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;

    // Module Parameters
    private String mModelPath = "/sdcard/flayer.3ds"; 
    
    public String getModelPath() {
		return mModelPath;
	}

	public void setModelPath(String modelPath) {
		mModelPath = modelPath;
	}

	// Our OpenGL view:
    private QCARSampleGLView mGlView;

    // Our renderer:
    private AugmenterRenderer mRenderer;

    // Display size of the device
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    // Constant representing invalid screen orientation to trigger a query:
    private static final int INVALID_SCREEN_ROTATION = -1;

    // Last detected screen rotation:
    private int mLastScreenRotation = INVALID_SCREEN_ROTATION;

    // The current application status
    private int mAppStatus = APPSTATUS_UNINITED;
    private int mUIStatus = UISTATUS_SCANNING_MODE;

    // The async tasks to initialize the QCAR SDK
    private InitQCARTask mInitQCARTask;
    private LoadTrackerTask mLoadTrackerTask;

    // An object used for synchronizing QCAR initialization, dataset loading and
    // the Android onDestroy() life cycle event. If the application is destroyed
    // while a data set is still being loaded, then we wait for the loading
    // operation to finish before shutting down QCAR.
    private Object mShutdownLock = new Object();

    // QCAR initialization flags
    private int mQCARFlags = 0;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mNewTargetButton;
    private View mCameraButton;
    private View mCloseButton;
    private View mLoadingDialogContainer;

    // Scanning mode help Overlay
    private View mBuildTargetHelp;

    // Alert dialog for displaying SDK errors
    private AlertDialog mDialog;

    // Detects the double tap gesture for launching the Camera menu
    private GestureDetector mGestureDetector;

    // Contextual Menu Options for Camera Flash - Autofocus
    private boolean mFlash = false;
    private boolean mContAutofocus = false;

    // Indicates if the instructions screen is being displayed or not
    private boolean mIsShowingIntsructions = false;
    //private View mInstructionsView;
    private SharedPreferences mPreferences;

    /** Static initializer block to load native libraries on start-up. */
    static
    {
        loadLibrary(NATIVE_LIB_QCAR);
        loadLibrary(NATIVE_LIB_APP);
    }

    /** Native tracker initialization and deinitialization. */
    public native int initTracker();

    public native void deinitTracker();

    /** Native functions to load and destroy tracking data. */
    public native int loadTrackerData();

    public native void destroyTrackerData();

    /** Native sample initialization. */
    public native void onQCARInitializedNative();

    /** Native methods for starting and stopping the camera. */
    private native void startCamera();

    private native void stopCamera();

    private native void initRefFreeNative(Augmenter self);

    /** Triggers Autofocus */
    private native boolean autofocus();

    /** Activates the Flash */
    private native boolean activateFlash(boolean flash);

    /** Setups the focus mode */
    private native boolean setFocusMode(int mode);

    /** Functions to control the image target builder */
    private native boolean startAugmenter();

    private native boolean stopAugmenter();

    /** Checks if UserDefiniedTargets is running */
    private native boolean isAugmenterRunning();

    /** Builds a new Target */
    private native void startBuild();

    /** Native function to deinitialize the application. */
    private native void deinitApplicationNative();

    /** Tells native code whether we are in portrait or landscape mode */
    private native void setActivityPortraitMode(boolean isPortrait);

    private native void setProjectionMatrix();

    /** Native function to initialize the application. */
    private native void initApplicationNative(int width, int height);


    /**
     * Creates a handler to update the status of the Loading Dialog from an UI
     * thread
     */
    static class LoadingDialogHandler extends Handler
    {
        private final WeakReference<Augmenter> mUserDefTarget;

        LoadingDialogHandler(Augmenter userDefTarget)
        {
            mUserDefTarget = new WeakReference<Augmenter>(
                    userDefTarget);
        }

        public void handleMessage(Message msg)
        {
            Augmenter userDefTarget = mUserDefTarget.get();
            if (userDefTarget == null)
            {
                return;
            }

            if (msg.what == SHOW_LOADING_DIALOG)
            {
                userDefTarget.mLoadingDialogContainer
                        .setVisibility(View.VISIBLE);

            }
            else if (msg.what == HIDE_LOADING_DIALOG)
            {
                userDefTarget.mLoadingDialogContainer.setVisibility(View.GONE);
            }
        }
    }

    private Handler loadingDialogHandler = new LoadingDialogHandler(this);


    /** An async task to initialize QCAR asynchronously. */
    private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>
    {
        // Initialize with invalid value
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock)
            {
                QCAR.setInitParameters(mContext, mQCARFlags);

                do
                {
                    // QCAR.init() blocks until an initialization step is
                    // complete,
                    // then it proceeds to the next step and reports progress in
                    // percents (0 ... 100%)
                    // If QCAR.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = QCAR.init();

                    // Publish the progress value:
                    publishProgress(mProgressValue);

                    // We check whether the task has been canceled in the
                    // meantime
                    // (by calling AsyncTask.cancel(true))
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that started
                    // is.
                } while (!isCancelled() && mProgressValue >= 0
                        && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }


        protected void onProgressUpdate(Integer... values)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }


        protected void onPostExecute(Boolean result)
        {
            // Done initializing QCAR, proceed to next application
            // initialization status:
            if (result)
            {
            	Log.d("QCAR","InitQCARTask::onPostExecute: QCAR initialization"
                        + " successful");

                updateApplicationStatus(APPSTATUS_INIT_TRACKER);
            }
            else
            {
                // Create dialog box for display error:
       //         AlertDialog dialogError = new AlertDialog.Builder(
       //                 Augmenter.this).create();
       //         dialogError.setButton(DialogInterface.BUTTON_POSITIVE,
       //                 "Close",
       //                 new DialogInterface.OnClickListener()
       //                 {
       //                     public void onClick(DialogInterface dialog,
       //                             int which)
       //                     {
       //                         // Exiting application
       //                         System.exit(1);
       //                     }
       //                 });

                String logMessage;

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                {
                    logMessage = "Failed to initialize QCAR because this "
                            + "device is not supported.";
                }
                else
                {
                    logMessage = "Failed to initialize QCAR.";
                }

                // Log error:
                Log.e("QCAR","InitQCARTask::onPostExecute: " + logMessage
                        + " Exiting.");

                // Show dialog box with error message:
                //dialogError.setMessage(logMessage);
                //dialogError.show();
            }
        }
    }


    /** An async task to load the tracker data asynchronously. */
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock)
            {
                // Load the tracker data set:
                return (loadTrackerData() > 0);
            }
        }

        protected void onPostExecute(Boolean result)
        {
        	Log.d("QCAR","LoadTrackerTask::onPostExecute: execution "
                    + (result ? "successful" : "failed"));

            // Done loading the tracker, update application status:
            updateApplicationStatus(APPSTATUS_INITED);

            // Hides the loading dialog
            loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

            // Initialize the app in the ViewFinder mode
            initializeViewFinderModeViews();
            
            ImageButton newTarget = (ImageButton)mContext.findViewById(R.id.new_target_button);
    	    newTarget.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				onNewTargetButtonClick(v);
    			}
    		});
    	    
    	    ImageButton cameraClick = (ImageButton)mContext.findViewById(R.id.camera_button);
    	    cameraClick.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				onCameraClick(v);
    			}
    		});
        }
    }


    /** Stores screen dimensions */
    private void storeScreenDimensions()
    {
        // Query display dimensions
        DisplayMetrics metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }


    public Augmenter() {
		super(REGISTER_SERVICE_NAME);
	}
    
//    /**
//     * Called when the activity first starts or needs to be recreated after
//     * resuming the application or a configuration change.
//     */
//    protected void onCreate(Bundle savedInstanceState)
//    {
//    	Log.d("QCAR","Augmenter::onCreate");
//        super.onCreate(savedInstanceState);
//
//        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        //changeShowInstructions(true);
//        //enterBuildTargetMode();
//
//        // Load any sample specific textures:
//        mTextures = new Vector<Texture>();
//        loadTextures();
//
//        // Query the QCAR initialization flags:
//        mQCARFlags = getInitializationFlags();
//
//        // Creates the GestureDetector listener for processing double tap
//        mGestureDetector = new GestureDetector(this, new GestureListener());
//
//        // Update the application status to start initializing application
//        updateApplicationStatus(APPSTATUS_INIT_APP);
//    }


    /**
     * We want to load specific textures from the APK, which we will later use
     * for rendering.
     */
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
        		mContext.getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
        		mContext.getAssets()));
    }


    /** Configure QCAR with the desired version of OpenGL ES. */
    private int getInitializationFlags()
    {
        return QCAR.GL_20;
    }


    /** Shows error message in a system dialog box */
    private void showErrorDialog()
    {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();

        mDialog = new AlertDialog.Builder(Augmenter.this).create();
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.instructions_overlay_button_ok), clickListener);

        mDialog.setTitle(getString(R.string.target_quality_error_title));

        String message = getString(R.string.target_quality_error_desc);

        // Show dialog box with error message:
        mDialog.setMessage(message);
        mDialog.show();
    }

    @Override
    public void onCreate(EngineActivity context) {
    	super.onCreate(context);
    	
    	
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      //changeShowInstructions(true);
      //enterBuildTargetMode();

      // Load any sample specific textures:
      mTextures = new Vector<Texture>();
      loadTextures();

      // Query the QCAR initialization flags:
      mQCARFlags = getInitializationFlags();

      // Creates the GestureDetector listener for processing double tap
      mGestureDetector = new GestureDetector(mContext/*this*/, new GestureListener());

      // Update the application status to start initializing application
      updateApplicationStatus(APPSTATUS_INIT_APP);
      
	    // QCAR-specific resume operation
	    QCAR.onResume();
	
	    // We may start the camera only if the QCAR SDK has already been
	    // initialized
	    if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
	        updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
	
	    // Resume the GL view:
	    if (mGlView != null)
	    {
	        mGlView.setVisibility(View.VISIBLE);
	        mGlView.onResume();
	    }
    }

    @Override
    public void onDestroyModule() {
      if (mGlView != null)
      {
          mGlView.setVisibility(View.INVISIBLE);
          mGlView.onPause();
      }

      if (mAppStatus == APPSTATUS_CAMERA_RUNNING)
      {
          updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
      }

      if (mDialog != null && mDialog.isShowing())
          mDialog.dismiss();

      // Disable flash when paused
      if (mFlash)
      {
          mFlash = false;
          activateFlash(mFlash);
      }

      // QCAR-specific pause operation
      QCAR.onPause();

	    // Cancel potentially running tasks
	    if (mInitQCARTask != null
	            && mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
	    {
	        mInitQCARTask.cancel(true);
	        mInitQCARTask = null;
	    }
	
	    if (mLoadTrackerTask != null
	            && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
	    {
	        mLoadTrackerTask.cancel(true);
	        mLoadTrackerTask = null;
	    }
	
	    // Ensure that all asynchronous operations to initialize QCAR and
	    // loading
	    // the tracker datasets do not overlap:
	    synchronized (mShutdownLock)
	    {
	        // Do application deinitialization in native code
	        deinitApplicationNative();
	
	        // Unload texture
	        mTextures.clear();
	        mTextures = null;
	
	        // Destroy the tracking data set:
	        destroyTrackerData();
	
	        // Deinit the tracker:
	        deinitTracker();
	
	        // Deinitialize QCAR SDK
	        QCAR.deinit();
	    }

    	System.gc();

    	mRenderer.release();
    	super.onDestroyModule();
    }
    
    /** Shows error message in a system dialog box on the UI thread */
    public void showErrorDialogInUIThread()
    {
        mContext.runOnUiThread(new Runnable()
        {
            public void run()
            {
                showErrorDialog();
            }
        });
    }


//    /** Called when the activity will start interacting with the user. */
//    protected void onResume()
//    {
//    	Log.d("QCAR","Augmenter::onResume");
//        super.onResume();
//
//        // QCAR-specific resume operation
//        QCAR.onResume();
//
//        // We may start the camera only if the QCAR SDK has already been
//        // initialized
//        if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
//            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
//
//        // Resume the GL view:
//        if (mGlView != null)
//        {
//            mGlView.setVisibility(View.VISIBLE);
//            mGlView.onResume();
//        }
//    }


//    /** Called when the system is about to start resuming a previous activity. */
//    protected void onPause()
//    {
//    	Log.d("QCAR","Augmenter::onPause");
//        super.onPause();
//
//        if (mGlView != null)
//        {
//            mGlView.setVisibility(View.INVISIBLE);
//            mGlView.onPause();
//        }
//
//        if (mAppStatus == APPSTATUS_CAMERA_RUNNING)
//        {
//            updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
//        }
//
//        if (mDialog != null && mDialog.isShowing())
//            mDialog.dismiss();
//
//        // Disable flash when paused
//        if (mFlash)
//        {
//            mFlash = false;
//            activateFlash(mFlash);
//        }
//
//        // QCAR-specific pause operation
//        QCAR.onPause();
//    }


//    /** The final call you receive before your activity is destroyed. */
//    protected void onDestroy()
//    {
//    	Log.d("QCAR","Augmenter::onDestroy");
//        super.onDestroy();
//
//        // Cancel potentially running tasks
//        if (mInitQCARTask != null
//                && mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
//        {
//            mInitQCARTask.cancel(true);
//            mInitQCARTask = null;
//        }
//
//        if (mLoadTrackerTask != null
//                && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
//        {
//            mLoadTrackerTask.cancel(true);
//            mLoadTrackerTask = null;
//        }
//
//        // Ensure that all asynchronous operations to initialize QCAR and
//        // loading
//        // the tracker datasets do not overlap:
//        synchronized (mShutdownLock)
//        {
//            // Do application deinitialization in native code
//            deinitApplicationNative();
//
//            // Unload texture
//            mTextures.clear();
//            mTextures = null;
//
//            // Destroy the tracking data set:
//            destroyTrackerData();
//
//            // Deinit the tracker:
//            deinitTracker();
//
//            // Deinitialize QCAR SDK
//            QCAR.deinit();
//        }
//
//        System.gc();
//    }


    /**
     * NOTE: this method is synchronized because of a potential concurrent
     * access by Augmenter::onResume() and
     * InitQCARTask::onPostExecute().
     */
    private synchronized void updateApplicationStatus(int appStatus)
    {
        // Exit if there is no change in status
        if (mAppStatus == appStatus)
            return;

        // Store new status value
        mAppStatus = appStatus;

        // Execute application state-specific actions
        switch (mAppStatus)
        {
        case APPSTATUS_INIT_APP:
            // Initialize application elements that do not rely on QCAR
            // initialization
            initApplication();

            // Proceed to next application initialization status
            updateApplicationStatus(APPSTATUS_INIT_QCAR);
            break;

        case APPSTATUS_INIT_QCAR:
            // Initialize QCAR SDK asynchronously to avoid blocking the
            // main (UI) thread.
            // This task instance must be created and invoked on the UI
            // thread and it can be executed only once!
            try
            {
                mInitQCARTask = new InitQCARTask();
                mInitQCARTask.execute();
            }
            catch (Exception e)
            {
            	Log.e("QCAR","Initializing QCAR SDK failed");
            }
            break;

        case APPSTATUS_INIT_TRACKER:
            // Initialize the ImageTracker
            if (initTracker() > 0)
            {
                // Proceed to next application initialization status
                updateApplicationStatus(APPSTATUS_INIT_APP_AR);
            }
            break;

        case APPSTATUS_INIT_APP_AR:
            // Initialize Augmented Reality-specific application elements
            // that may rely on the fact that the QCAR SDK has been
            // already initialized
            initApplicationAR();

            // Proceed to next application initialization status
            updateApplicationStatus(APPSTATUS_LOAD_TRACKER);
            break;

        case APPSTATUS_LOAD_TRACKER:
            // Load the tracking data set
            //
            // This task instance must be created and invoked on the UI
            // thread and it can be executed only once!
            try
            {
                mLoadTrackerTask = new LoadTrackerTask();
                mLoadTrackerTask.execute();
            }
            catch (Exception e)
            {
            	Log.e("QCAR","Loading tracking data set failed");
            }
            break;

        case APPSTATUS_INITED:
            // Hint to the virtual machine that it would be a good time to
            // run the garbage collector.
            //
            // NOTE: This is only a hint. There is no guarantee that the
            // garbage collector will actually be run.
            System.gc();

            // Native post initialization:
            onQCARInitializedNative();

            // Activate the renderer
            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            mContext.getLayout().addView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            mUIStatus = UISTATUS_SCANNING_MODE;

            // Start the camera:
            updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);

            break;

        case APPSTATUS_CAMERA_STOPPED:
            // Call the native function to stop the camera
            stopCamera();
            break;

        case APPSTATUS_CAMERA_RUNNING:

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            // Call the native function to start the camera
            startCamera();

            // Set continuous auto-focus if supported by the device,
            // otherwise default back to regular auto-focus mode.
            // This will be activated by a tap to the screen in this
            // application.
            if (!setFocusMode(FOCUS_MODE_CONTINUOUS_AUTO))
            {
                setFocusMode(FOCUS_MODE_NORMAL);
                mContAutofocus = false;
            }
            else
            {
                mContAutofocus = true;
            }
            break;

        default:
            throw new RuntimeException("Invalid application state");
        }
    }


    /** Initialize application GUI elements that are not related to AR. */
    private void initApplication()
    {
        // Set the screen orientation:
        // NOTE: Use SCREEN_ORIENTATION_LANDSCAPE or SCREEN_ORIENTATION_PORTRAIT
        //       to lock the screen orientation for this activity.
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

        // This is necessary for enabling AutoRotation in the Augmented View
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
        {
            // NOTE: We use reflection here to see if the current platform
            // supports the full sensor mode (available only on Gingerbread
            // and above.
            try
            {
                // SCREEN_ORIENTATION_FULL_SENSOR is required to allow all 
                // 4 screen rotations if API level >= 9:
                Field fullSensorField = ActivityInfo.class
                        .getField("SCREEN_ORIENTATION_FULL_SENSOR");
                screenOrientation = fullSensorField.getInt(null);
            }
            catch (NoSuchFieldException e)
            {
                // App is running on API level < 9, do nothing.
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Apply screen orientation
        //setRequestedOrientation(screenOrientation);

        updateActivityOrientation();

        // Query display dimensions:
        storeScreenDimensions();

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        //        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /** Callback for configuration changes the activity handles itself */
    public void onConfigurationChanged(Configuration config)
    {
    	Log.e("QCAR","Augmenter::onConfigurationChanged");
        super.onConfigurationChanged(config);

        // updates screen orientation
        updateActivityOrientation();

        // Removes the current layout and inflates a proper layout
        // for the new screen orientation

        if (mUILayout != null)
        {
            mUILayout.removeAllViews();
            ((ViewGroup) mUILayout.getParent()).removeView(mUILayout);

        }

        addOverlayView(false);

        storeScreenDimensions();

        // Invalidate screen rotation to trigger query upon next render call:
        mLastScreenRotation = INVALID_SCREEN_ROTATION;
    }


    /** Updates screen orientation to enable AutoRotation */
    private void updateActivityOrientation()
    {
        Configuration config = mContext.getResources().getConfiguration();

        boolean isPortrait = false;

        switch (config.orientation)
        {
        case Configuration.ORIENTATION_PORTRAIT:
            isPortrait = true;
            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            isPortrait = false;
            break;
        case Configuration.ORIENTATION_UNDEFINED:
        default:
            break;
        }

        Log.i("QCAR","Activity is in "
                + (isPortrait ? "PORTRAIT" : "LANDSCAPE"));
        setActivityPortraitMode(isPortrait);
    }


    /**
      * Updates projection matrix and viewport after a screen rotation
      * change was detected.
      */
    public void updateRenderView()
    {
        int currentScreenRotation =
            mContext.getWindowManager().getDefaultDisplay().getRotation();

        if (currentScreenRotation != mLastScreenRotation)
        {
            // Set projection matrix if there is already a valid one:
            if (QCAR.isInitialized() &&
                (mAppStatus == APPSTATUS_CAMERA_RUNNING))
            {
            	Log.d("QCAR","Augmenter::updateRenderView");

                // Query display dimensions:
                storeScreenDimensions();

                // Update viewport via renderer:
                mRenderer.updateRendering(mScreenWidth, mScreenHeight);

                // Update projection matrix:
                setProjectionMatrix();

                // Cache last rotation used for setting projection matrix:
                mLastScreenRotation = currentScreenRotation;
            }
        }
    }


    /** Initializes AR application components. */
    private void initApplicationAR()
    {
        // Do application initialization in native code (e.g. registering
        // callbacks, etc.)
        initApplicationNative(mScreenWidth, mScreenHeight);

        initRefFreeNative(this);

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = QCAR.requiresAlpha();

        mGlView = new QCARSampleGLView(mContext);
        mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);

        mRenderer = new AugmenterRenderer(mContext,this);
        mRenderer.mActivity = mContext;
        mGlView.setRenderer(mRenderer);
        addOverlayView(true);
    }


    /** Adds the Overlay view to the GLView */
    private void addOverlayView(boolean initLayout)
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);

        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
        if (initLayout)
        {
            mUILayout.setBackgroundColor(Color.BLACK);
        }

        // Adds the inflated layout to the view
        mContext.getLayout().addView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        // Gets a reference to the bottom navigation bar
        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);

        // Gets a reference to the instructions view
        //mInstructionsView = mUILayout.findViewById(R.id.instructions);

        // Gets a reference to the build target help
        mBuildTargetHelp = mUILayout
                .findViewById(R.id.overlay_build_target_help);

        // Gets a reference to the NewTarget button
        mNewTargetButton = mUILayout.findViewById(R.id.new_target_button);

        // Gets a reference to the CloseBuildTargetMode button
        mCloseButton = mUILayout.findViewById(R.id.close_button);

        // Gets a reference to the Camera button
        mCameraButton = mUILayout.findViewById(R.id.camera_button);

        // Gets a reference to the loading dialog container
        mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_layout);

        // Checks if it needs to show the instructions view or not
        if (mIsShowingIntsructions)
        {
      //      mInstructionsView.setVisibility(View.VISIBLE);
            mNewTargetButton.setEnabled(false);
        }

        // Checks the UIStatus to initialize the navigation bar
        // with the proper UI
        if (mUIStatus == UISTATUS_SCANNING_MODE)
        {
            initializeViewFinderModeViews();
        }
        else
        {
            initializeBuildTargetModeViews();
        }

        mUILayout.bringToFront();
    }


    /** Button Close/Cancel clicked */
    public void onCloseClick(View v)
    {
        // Goes back to the ViewFinderMode
        enterViewFinderMode();

    }


    /** Button Camera clicked */
    public void onCameraClick(View v)
    {
        if (isAugmenterRunning())
        {
            // Shows the loading dialog
            loadingDialogHandler.sendEmptyMessage(SHOW_LOADING_DIALOG);

            // Builds the new target
            startBuild();
        }
    }


    /** Button NewTarget clicked - Enters Build target Mode */
    public void onNewTargetButtonClick(View v)
    {
        // Checks if the instructions view needs to be displayed
        //if (shouldShowInstructions())
        //{
            // Shows the instructions view and returns
        //    mInstructionsView.setVisibility(View.VISIBLE);
        //    mNewTargetButton.setEnabled(false);
        //    mIsShowingIntsructions = true;
        //    return;
        //}

        // Enter the Build New Target Mode
        enterBuildTargetMode();
    }


    /** Instructions button OK clicked */
    public void instructionsOnOkClick(View v)
    {
        // Updates the Show Instructions control flag
        changeShowInstructions(false);

        // Hides the instructions view
       // mInstructionsView.setVisibility(View.GONE);
        mNewTargetButton.setEnabled(true);
        mIsShowingIntsructions = false;

        // Calls to the newTargetButtonClick Method
        // to enter the BuildTargetMode
        onNewTargetButtonClick(null);
    }


    /** Instructions button Cancel clicked */
    public void instructionsOnCancelClick(View v)
    {
        // Hides the instructions view without
        // updating the instructions flag
      //  mInstructionsView.setVisibility(View.GONE);
        mIsShowingIntsructions = false;
        mNewTargetButton.setEnabled(true);
    }


    /**
     * Returns if the instructions screen need to be displayed
     */
    private boolean shouldShowInstructions()
    {
        return mPreferences.getBoolean(SHOW_PREFERENCES, true);
    }


    /** Updates the show Instructions control flag */
    private void changeShowInstructions(boolean value)
    {
        Editor editor = mPreferences.edit();
        editor.putBoolean(SHOW_PREFERENCES, value);
        while (!editor.commit())
            ;
    }


    /** Returns the number of registered textures. */
    public int getTextureCount()
    {
        return mTextures.size();
    }


    /** Returns the texture object at the specified index. */
    public Texture getTexture(int i)
    {
        return mTextures.elementAt(i);
    }


    /** Creates a texture given the filename */
    public Texture createTexture(String nName)
    {
        return Texture.loadTextureFromApk(nName, mContext.getAssets());
    }


    /**
     * Callback function called from Native when the target creation finished
     */
    public void targetCreated()
    {
        // Hides the loading dialog
        loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

        // In the UI thread updates the navigation bar status
        mContext.runOnUiThread(new Runnable()
        {
            public void run()
            {
                mUIStatus = UISTATUS_SCANNING_MODE;
                initializeViewFinderModeViews();
            }
        });
    }


    /** Enters the ViewFinder Mode */
    private void enterViewFinderMode()
    {
        // Checks that UDT is running
        if (isAugmenterRunning())
        {
            // Updates current UI Status
            mUIStatus = UISTATUS_SCANNING_MODE;

            // Stops the UDT
            stopAugmenter();

            // Updates the navigation bar status
            initializeViewFinderModeViews();
        }
    }


    /** Initialize the ViewFinder mode views */
    private void initializeViewFinderModeViews()
    {
        // Shows the bottom bar with the new Target Button
        mBottomBar.setVisibility(View.VISIBLE);
        mNewTargetButton.setVisibility(View.VISIBLE);

        // Hides the target build controls
        mBuildTargetHelp.setVisibility(View.GONE);
        mCameraButton.setVisibility(View.INVISIBLE);
        mCloseButton.setVisibility(View.INVISIBLE);
    }


    /** Enters the BuildTarget mode */
    private void enterBuildTargetMode()
    {
        // Checks that UDT is not already running
        if (!isAugmenterRunning())
        {
            // Updates current UI status
            mUIStatus = UISTATUS_BUILD_TARGET_MODE;

            // Starts UDT
            startAugmenter();

            // Updates the navigation bar status
            initializeBuildTargetModeViews();
        }
    }


    /** Initialize Build Target mode views */
    private void initializeBuildTargetModeViews()
    {
        // Shows the bottom bar with the Build target options
        mBuildTargetHelp.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.VISIBLE);

        // Hides the new target control
        mNewTargetButton.setVisibility(View.INVISIBLE);
    }


    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName)
    {
        try
        {
            System.loadLibrary(nLibName);
            Log.i("QCAR","Native library lib" + nLibName + ".so loaded");
            return true;
        }
        catch (UnsatisfiedLinkError ulee)
        {
        	Log.e("QCAR","The library lib" + nLibName
                    + ".so could not be loaded");
        }
        catch (SecurityException se)
        {
        	Log.e("QCAR","The library lib" + nLibName
                    + ".so was not allowed to be loaded");
        }

        return false;
    }


    /** Shows the Camera Options Dialog when the Menu Key is pressed */
//    public boolean onKeyUp(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_MENU)
//        {
//            showCameraOptionsDialog();
//            return true;
//        }
//
//        return super.onKeyUp(keyCode, event);
//    }


    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        return mGestureDetector.onTouchEvent(event);
    }


    /** Process Double Tap event for showing the Camera options menu */
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        public boolean onSingleTapUp(MotionEvent e)
        {
            // Calls the Autofocus Native Method
            autofocus();

            // Triggering manual auto focus disables continuous
            // autofocus
            mContAutofocus = false;

            return true;
        }


        // Event when double tap occurs
        public boolean onDoubleTap(MotionEvent e)
        {

            // Shows the Camera options
            showCameraOptionsDialog();
            return true;
        }
    }


    /** Shows an AlertDialog with the camera options available */
    private void showCameraOptionsDialog()
    {
        // Only show camera options dialog box if app has been
        // already initialized
        if (mAppStatus < APPSTATUS_INITED)
        {
            return;
        }

        final int itemCameraIndex = 0;
        final int itemAutofocusIndex = 1;

        AlertDialog cameraOptionsDialog = null;

        CharSequence[] items =
        { getString(R.string.menu_flash_on),
                getString(R.string.menu_contAutofocus_off) };

        // Updates list titles according to current state of the options
        if (mFlash)
        {
            items[itemCameraIndex] = (getString(R.string.menu_flash_off));
        }
        else
        {
            items[itemCameraIndex] = (getString(R.string.menu_flash_on));
        }

        if (mContAutofocus)
        {
            items[itemAutofocusIndex] = (getString(R.string.menu_contAutofocus_off));
        }
        else
        {
            items[itemAutofocusIndex] = (getString(R.string.menu_contAutofocus_on));
        }

        // Builds the Alert Dialog
        AlertDialog.Builder cameraOptionsDialogBuilder = new AlertDialog.Builder(
                Augmenter.this);
        cameraOptionsDialogBuilder
                .setTitle(getString(R.string.menu_camera_title));
        cameraOptionsDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {

                        if (item == itemCameraIndex)
                        {
                            // Turns focus mode on/off by calling native
                            // method
                            if (activateFlash(!mFlash))
                            {
                                mFlash = !mFlash;
                            }
                            else
                            {
                                Toast.makeText
                                (
                                    Augmenter.this,
                                    "Unable to turn " + 
                                    (mFlash ? "off" : "on") + " flash",
                                    Toast.LENGTH_SHORT
                                ).show();
                            }

                            // Dismisses the dialog
                            dialog.dismiss();
                        }
                        else if (item == itemAutofocusIndex)
                        {
                            if (mContAutofocus)
                            {
                                // Sets the Focus Mode by calling the native
                                // method
                                if (setFocusMode(FOCUS_MODE_NORMAL))
                                {
                                    mContAutofocus = false;
                                }
                                else
                                {
                                    Toast.makeText
                                    (
                                        Augmenter.this,
                                        "Unable to deactivate Continuous Auto-Focus",
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                            else
                            {
                                // Sets the focus mode by calling the native
                                // method
                                if (setFocusMode(FOCUS_MODE_CONTINUOUS_AUTO))
                                {
                                    mContAutofocus = true;
                                }
                                else
                                {
                                    Toast.makeText
                                    (
                                        Augmenter.this,
                                        "Unable to activate Continuous Auto-Focus",
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            // Dismisses the dialog
                            dialog.dismiss();
                        }
                    }
                });

        // Shows the dialog box
        cameraOptionsDialog = cameraOptionsDialogBuilder.create();
        cameraOptionsDialog.show();
    }

	@Override
	public ExecutionCode execute(Sample image) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getModuleName(){
		return "Augmenter";
	}
	
	@Override
	public String getName() {
		return getModuleName();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public CommitableView getConfigurationView(Context context) {
		return new AugmenterConfigurationNew(context, this);
	}
	
	
}
