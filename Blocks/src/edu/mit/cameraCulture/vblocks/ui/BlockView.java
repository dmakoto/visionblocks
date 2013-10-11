package edu.mit.cameraCulture.vblocks.ui;

import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Represents each selected module that will be executed by PreviewActivity.
 * These views are located in the middle of the screen, and sometimes
 * they are configurable. It shows a menu when clicked, enabling the user
 * to modify some properties of the module.
 * @author Camera Culture
 *
 */
public class BlockView extends TextView {

	private Module mModule;
	
	public BlockView(Context context, Module m) {
		super(context);
		setModule(m); 
 	}
	
	public BlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Module getModule() {
		return mModule;
	}

	private void setModule(Module module) {
		this.mModule = module;
	}
	
	/**
	 * Creates a block based on a module. Prepares its View
	 * to be presented on-screen.
	 * @param context
	 * @param module
	 * @return The block itself.
	 */
	public static BlockView createBlock(Context context, Module module){
		final Context c = context;
		final Module m = module;
		BlockView bv = new BlockView(context, m);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (metrics.density * 60 + 0.5f));
		bv.setLayoutParams(lp);
		bv.setBackgroundResource(R.drawable.bg_block);
		bv.setGravity(Gravity.CENTER);
		lp.setMargins(0, 0, 0, (int) (metrics.density * -11 + 0.5f));
		bv.setText(m.getName());
		bv.setTextColor(Color.WHITE);
		bv.setTypeface(null, Typeface.BOLD);
		bv.setClickable(true);
		bv.setEnabled(true);
		bv.setFocusable(true);
		bv.setFocusableInTouchMode(true);
		bv.setOnTouchListener(new BlockTouchListener(context));
		bv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v instanceof BlockView){
					final CommitableView confView = ((BlockView)v).getModule().getConfigurationView(c);
					if(confView != null){
						final Dialog dialog = new Dialog(c);
						View main = ((Activity)c).getLayoutInflater().inflate(R.layout.configuration_dialog,null);
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
						dialog.setTitle(m.getName());
						dialog.show();
					}
				}
			}
		});
		
		return bv; 
	}
}
