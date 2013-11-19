package edu.mit.cameraCulture.vblocks.engine;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.VBlocksApplication;
import edu.mit.cameraCulture.vblocks.engine.MainMenuFragment.OnMainMenuSelectedListener;

/**
 * Responsible for running the custom application devised by the user.
 * The application is defined by the sequence of modules in <code>mProgram</code>.
 * @author CameraCulture
 *
 */
public class PreviewActivity extends EngineActivity implements OnMainMenuSelectedListener {
	
	protected RelativeLayout mMainLayout = null;
	protected Module mProgram = null;
	protected List<Module> mModuleList;
	protected String[] mModuleNames;
	protected PreviewGUI mGUI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		// Following 3 lines are important
		setContentView(R.layout.activity_main);
		mProgram = VBlocksApplication.getProgram(); 
		mModuleList = mProgram.getModuleList();
		mModuleNames = getNameList(mModuleList);
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
	public void onMenuOptionSelected(int id) {
		Log.d("MENU", "Selected item number "+id);
		MainContentFragment fragment =	(MainContentFragment) mGUI.getMain();
		fragment.setActiveView(mModuleList.get(id));
	}
	
	private String[] getNameList(List<Module> moduleList) {
		List<String> nameList = new ArrayList<String>();
		
		// Create a list of names
		for(Module m : moduleList)
			nameList.add(m.getName());
		
		// Create an array of the names from the list of names
		String[] values = nameList.toArray(new String[nameList.size()]);
		
		return values;
	}
	
	public String[] getModuleNames() {
		return mModuleNames;
	}
}
