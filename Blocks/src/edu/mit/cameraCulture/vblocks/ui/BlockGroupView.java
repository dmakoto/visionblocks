package edu.mit.cameraCulture.vblocks.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.ModuleCollection;
import edu.mit.cameraCulture.vblocks.R;

/**
 * A GroupView of BlockViews. It has instances of modules, which can be
 * obtained by calling packModules().
 * @author Camera Culture
 *
 */
public class BlockGroupView extends LinearLayout {

	private LinearLayout mContent;
	private ModuleCollection mModule = null;
	private TextView mTxtView;
	
	public BlockGroupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
		mModule = new ModuleCollection("Program");
	}

	public BlockGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
		mModule = new ModuleCollection("Program");
	}

	public BlockGroupView(Context context) {
		super(context);
		initialize(context);
		mModule = new ModuleCollection("Program");
	}
	
	public void addBlock(BlockView block, Context c){
		mContent.addView(block);
		mContent.addView(Separator.CreateSeparator(c));
	}
	
	public void addSeparator(Context c){
		mContent.addView(Separator.CreateSeparator(c));
	}
	
	public void clearAllChildren(){
		mContent.removeAllViews();
		// Clear modules in the BlockGroupView
		mModule = new ModuleCollection("Program");
		System.gc();
	} 
	
	
	public void addBlock(BlockGroupView blockGroup, Context c){
		mContent.addView(blockGroup);
		mContent.addView(Separator.CreateSeparator(c));
	}
	
	/**
	 * Prepares the environment to show the View of blocks.
	 * @param context
	 */
	private void initialize(Context context){
		this.setOrientation(LinearLayout.HORIZONTAL);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (metrics.density * 30 + 0.5f),LayoutParams.MATCH_PARENT);
		View rightBar = new View(context);
		rightBar.setBackgroundResource(R.color.blockBackground);
        this.addView(rightBar,params);
		
		LinearLayout content = new LinearLayout(context);
		content.setOrientation(LinearLayout.VERTICAL);
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		this.addView(content,params);
		
		mTxtView = new TextView(context);
		mTxtView.setGravity(Gravity.CENTER);
		mTxtView.setPadding(0, 0, 0, (int) (metrics.density * 10 + 0.5f));
		mTxtView.setBackgroundResource(R.color.blockBackground);
		content.addView(mTxtView,params);
		
		mContent = new LinearLayout(context);
		mContent.setOrientation(LinearLayout.VERTICAL);
		mContent.setPadding(0, 0, 0, (int) (metrics.density * 10 + 0.5f));
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		params.setMargins((int) (metrics.density * 2 + 0.5f), 0, 0, 0);
		content.addView(mContent,params);
		
		TextView txtView2 = new TextView(context);
		txtView2.setGravity(Gravity.CENTER);
		txtView2.setPadding(0, 0, 0, (int) (metrics.density * 10 + 0.5f));
		txtView2.setBackgroundResource(R.color.blockBackground);
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		content.addView(txtView2,params);
		
		mContent.addView(Separator.CreateSeparator(context));
	}
	
	public static BlockGroupView createBlock(Context context, ModuleCollection module){
		final Context c = context;
		final Module m = module;
		BlockGroupView bv = new BlockGroupView(context);
		bv.setModule(module);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		bv.setLayoutParams(lp);
		bv.setClickable(true);
		bv.setEnabled(true);
		bv.setFocusable(true);
		bv.setFocusableInTouchMode(true);
		bv.setOnTouchListener(new BlockTouchListener(context));
		bv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v instanceof BlockGroupView){
					final CommitableView confView = ((BlockGroupView)v).packModules().getConfigurationView(c);
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

	/**
	 * Creates a pack of Modules according to what exists in the
	 * <code>mContent</code> Layout. Iterates over all children
	 * of <code>mContent</code> and select only Views that are
	 * <code>BlockView</code>. Pack all the corresponding Modules
	 * and retrieve them.
	 * Note: ModuleCollection extends Module.
	 * @return Collection of Modules collected in the LinearLayout <code>mContent</code>
	 */
	public Module packModules() {
		for(int i = 0; i < mContent.getChildCount(); i++ ){
            View v = mContent.getChildAt(i);
            if( v instanceof BlockView){
                if(((BlockView)v).getModule() != null)
                    mModule.add(((BlockView)v).getModule());
            } else if(v instanceof BlockGroupView){
            	Module m = ((BlockGroupView)v).packModules();
                if(m != null)
                    mModule.add(m);
            }
		}
		return mModule;
	}

	/**
	 * Set the collection of modules to <code>mModule</code>.
	 * Also, set the text to the name of the mModule.
	 * @param mModule
	 */
	public void setModule(ModuleCollection mModule) {
		this.mModule = mModule;
		mTxtView.setText(mModule.getName());
	}
	
}
