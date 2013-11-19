package edu.mit.cameraCulture.vblocks.engine;

import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.widget.RelativeLayout;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.VBlocksApplication;
import edu.mit.cameraCulture.vblocks.engine.MainMenu.OnMainMenuSelectedListener;
import edu.mit.cameraCulture.vblocks.ui.PreviewGUI;

/**
 * Responsible for running the custom application devised by the user.
 * The application is defined by the sequence of modules in <code>mProgram</code>.
 * @author CameraCulture
 *
 */
public class PreviewActivity extends EngineActivity implements OnMainMenuSelectedListener {
	
	protected RelativeLayout mMainLayout = null;
	protected Module mProgram = null;
	protected PreviewGUI mGUI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		// Following 3 lines are important
		setContentView(R.layout.activity_main);
		mProgram = VBlocksApplication.getProgram(); 
		mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		
		// Instantiate a GUI using the xml elements: R.id.paneMenu and R.id.paneContent
		// Also, instantiate the SlidingPaneLayout in R.id.pane
		mGUI = new PreviewGUI(this, R.id.pane);
		mGUI.createMenu(mProgram);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// Place the touchable pane to the front
		mGUI.bringToFront();
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

	@Override
	public void onMenuOptionSelected(OPTION option) {
		// TODO Auto-generated method stub
		
	}
}
