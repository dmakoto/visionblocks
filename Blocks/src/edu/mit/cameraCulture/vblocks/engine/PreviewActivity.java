package edu.mit.cameraCulture.vblocks.engine;

import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.VBlocksApplication;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module.PixelEncoding;

import android.os.Bundle;
import android.widget.RelativeLayout;

/**
 * Responsible for running the custom application devised by the user.
 * The application is defined by the sequence of modules in <code>mProgram</code>.
 * @author CameraCulture
 *
 */
public class PreviewActivity extends EngineActivity {
	protected RelativeLayout mMainLayout = null;
	protected Module mProgram = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		// Following 3 lines are important
		setContentView(R.layout.activity_main);
		mProgram = VBlocksApplication.getProgram(); 
		mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	};
	
	@Override
	protected void onStop() {
		super.onStop();
	};
	
	@Override
	public RelativeLayout getLayout(){
		return mMainLayout;
	}
	
	@Override
	public Module getProgram(){
		return mProgram;
	};
	
	@Override
	protected void convertColor(Sample s, Module.PixelEncoding dstEncoding) {
		
	}
}
