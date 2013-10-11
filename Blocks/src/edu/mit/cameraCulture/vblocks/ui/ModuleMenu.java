package edu.mit.cameraCulture.vblocks.ui;

import java.util.List;

import edu.mit.cameraCulture.vblocks.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

public class ModuleMenu extends ImageButton {

	private static DismissableLayout currentMenu = null;
	
	private LinearLayout mContent;
	
	public class DismissableLayout extends LinearLayout {
		
		
		
		public DismissableLayout(Context context, DisplayMetrics metrics) {
			super(context);
			this.setOrientation(VERTICAL);
			this.setBackgroundColor(Color.TRANSPARENT);
			int padding = (int) (metrics.density * 1 + 0.5f);
			
			mContent = new LinearLayout(context);
			mContent.setPadding(padding,padding,padding,0);
			mContent.setOrientation(VERTICAL);
			mContent.setBackgroundResource(R.color.menuBackground);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			super.addView(mContent, params);
		}
		
		@Override
		public void addView(View child, android.view.ViewGroup.LayoutParams params) {
			mContent.addView(child, params);
		};
		
		public void dismiss(){
			this.setVisibility(GONE);
			currentMenu = null;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			this.dismiss();
			return super.onTouchEvent(event);
		}
		
		@Override
		protected void onVisibilityChanged(View changedView, int visibility) {
			super.onVisibilityChanged(changedView, visibility);
			
		}
	} 
	
	
	private List<TemplateView> mItems;
	private Activity mActivity;
	//private RelativeLayout mFrame;
	private DismissableLayout mWindow;
	
	public ModuleMenu(Activity activity, RelativeLayout frame, List<TemplateView> templates,int backgroundResId, int resId ) {
		super(activity);
		mItems = templates;
		mActivity = activity;
		//mFrame = frame;
		DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
		
		mWindow = new DismissableLayout(mActivity,metrics);
		
		this.setBackgroundResource(backgroundResId);
		this.setImageResource(resId);
		this.setScaleType(ScaleType.FIT_START);
		
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				(int) (metrics.density * 180 + 0.5f),LayoutParams.WRAP_CONTENT);
		params.bottomMargin = (int) (metrics.density * 1 + 0.5f);
				//PopupWindow pw = new PopupWindow(layout, 300, 470, true);
		//layout.setWindow(pw);
        // display the popup in the center
        //pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
		
        for(TemplateView v: mItems){
        	mWindow.addView(v,params);
        }
        
        
        params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		frame.addView(mWindow,params);
        
		mWindow.setVisibility(GONE);
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(currentMenu != null) {
					currentMenu.dismiss();
				}
				mWindow.setVisibility(VISIBLE);
				currentMenu = mWindow;
			}
		});
	}

}
