package edu.mit.cameraCulture.vblocks;

import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Module.PixelEncoding;
import edu.mit.cameraCulture.vblocks.Sample;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Runs the activity related to the application devised by the client.
 * Uses the chain of modules selected by the client, creating a thread
 * where the application will run.
 * @author CameraCulture
 */
public abstract class EngineActivity extends Activity {
	
	protected Thread mWorker;
	protected boolean mIsRunning;
	
	public abstract RelativeLayout getLayout();
	
	public abstract Module getProgram();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onStart() {
		// Initialize module ? and start it in a new thread.
		
		super.onStart();
		initializeModules(this.getProgram());
		mIsRunning = true;
		mWorker = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(mIsRunning){
					executeModules(getProgram());
				}
			}
		});
		mWorker.start();
	};
	
	@Override
	protected void onStop() {
		// Stop all running module and uninitialize it
		mIsRunning = false;
		uninitializeModules(this.getProgram());
		super.onStop();
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Close the activity if the BACK button is pressed
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			mIsRunning = false;
			finish();
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	private void initializeModules(Module program){
		program.onCreate(this);
	}
	
	protected void executeModules(Module program){
		Sample s = new Sample(); 
		program.execute(s);
	}
	
	private void uninitializeModules(Module program){
		program.onDestroyModule();
	}
	
	protected abstract void convertColor(Sample s, PixelEncoding dstEncoding);
}
