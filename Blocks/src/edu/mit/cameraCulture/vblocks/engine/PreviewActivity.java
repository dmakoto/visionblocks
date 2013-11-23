package edu.mit.cameraCulture.vblocks.engine;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.View;
import android.widget.RelativeLayout;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.VBlocksApplication;
import edu.mit.cameraCulture.vblocks.ui.MainView;

/**
 * Responsible for running the custom application devised by the user.
 * The application is defined by the sequence of modules in <code>mProgram</code>.
 * @author CameraCulture
 *
 */
public class PreviewActivity extends EngineActivity {
	
	protected RelativeLayout mMainLayout = null;
	protected Module mProgram = null;
	protected List<Module> mModuleList;
	protected String[] mModuleNames;
	private DrawerLayout mDrawerLayout;
	private PreviewGUIMenu mMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	
		// Following 3 lines are important
		setContentView(R.layout.activity_main);
		
		mProgram = VBlocksApplication.getProgram(); 
		mModuleList = mProgram.getModuleList();
		mModuleNames = getNameList(mModuleList);
		
		mMenu = new PreviewGUIMenu(this, R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		RelativeLayout mainContent = (RelativeLayout) findViewById(R.id.main_content);
		
		mMainLayout = new MainView(this);
		mainContent.addView(mMainLayout);

		setDrawerLayoutListener();
	}
	
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
	
	public DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}
	
	public List<Module> getModuleList() {
		return mModuleList;
	}
	
	public String[] getModuleNames() {
		return mModuleNames;
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

	private void setDrawerLayoutListener() {
		// TODO Auto-generated method stub
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				arg0.bringToFront();
			}

			@Override
			public void onDrawerOpened(View arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDrawerClosed(View arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
}
