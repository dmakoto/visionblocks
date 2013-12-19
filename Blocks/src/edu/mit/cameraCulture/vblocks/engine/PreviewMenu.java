package edu.mit.cameraCulture.vblocks.engine;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.ui.BlockGroupView;
import edu.mit.cameraCulture.vblocks.ui.MainView;

public class PreviewMenu implements OnItemClickListener, OnItemLongClickListener {

	private String[] mModuleNames;
	private ListView mDrawerList;
	private PreviewActivity mContext;
	
	public PreviewMenu(PreviewActivity activity, int id) {
		
		mContext = activity;
		
		mDrawerList = (ListView) mContext.findViewById(id);

		// Set itself as a listener for all the actions in the menu
		mDrawerList.setOnItemClickListener(this);
		mDrawerList.setOnItemLongClickListener(this);

		// Get name of all modules
		mModuleNames = mContext.getModuleNames();

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(mContext,
				android.R.layout.simple_list_item_1, mModuleNames));
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int blockPosition, long arg3) {
		
		 // Set the view of mMainLayout to pass the touch to the block selected.
		MainView main = (MainView) mContext.getLayout();
		main.setModuleTouchListener(mContext.getModuleList().get(blockPosition).getModuleTouchListener());
		mContext.getDrawerLayout().closeDrawers();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int blockPosition,
			long arg3) {

		// Depending on which view was clicked, set the touch
		Log.d("GUI", "Long Click on "+mContext.getModuleNames()[blockPosition]);
		
		final CommitableView confView = mContext.getModuleList().get(blockPosition).getConfigurationView(mContext);
		if(confView != null) {
			final Dialog dialog = new Dialog(mContext);
			
			View main = ((Activity)mContext).getLayoutInflater().inflate(R.layout.configuration_dialog,null);
			
			ScrollView sv = (ScrollView) main.findViewById(R.id.config_content);
			//final CommitableView configPanel = m.getConfigurationView(c);
			sv.addView(confView);
			
			Button confirm = (Button) main.findViewById(R.id.cfg_confirm_button);
			
			confirm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					confView.commit();
					dialog.dismiss();
				}
			});
			dialog.setContentView(main);
			dialog.setTitle(mContext.getModuleList().get(blockPosition).getName());
			dialog.show();
		}
		
		mContext.getDrawerLayout().closeDrawers();
		
		return true;
	}
}
