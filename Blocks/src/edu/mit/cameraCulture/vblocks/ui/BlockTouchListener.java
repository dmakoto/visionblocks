package edu.mit.cameraCulture.vblocks.ui;

import java.util.Date;

import edu.mit.cameraCulture.vblocks.MainActivity;
import edu.mit.cameraCulture.vblocks.ui.ModuleMenu.DismissableLayout;
import android.content.ClipData;
import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class BlockTouchListener implements OnTouchListener {

	private static final int TIME_TO_DRAG = 300;
	private static long [] vibrationPattern = { 0, 50, 0 };

	private long mMilliseconds = 0;
	private Vibrator mVibrator;
	private Context mContext; 
	
	
	public BlockTouchListener(Context c){
		mVibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		mContext = c;
	} 
	
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch(motionEvent.getAction()){
		case MotionEvent.ACTION_DOWN:
			mMilliseconds = new Date().getTime();
			return true;
		case MotionEvent.ACTION_UP:
			if( ((view instanceof BlockView)||(view instanceof BlockGroupView)) && ((mMilliseconds + TIME_TO_DRAG) > new Date().getTime())){
				view.performClick();
			}
		case MotionEvent.ACTION_HOVER_EXIT:
			mMilliseconds = 0;
			return true;
		default:
			if( (view instanceof TemplateView)||					
				((mMilliseconds + TIME_TO_DRAG) < new Date().getTime())){
				mVibrator.vibrate(vibrationPattern, -1);
				ClipData data = ClipData.newPlainText("", "");
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				//start dragging the item touched
				
				closeParrentIfInflated(view);
				view.startDrag(data, shadowBuilder, view, 0);
				if(((view instanceof BlockView)||(view instanceof BlockGroupView)) && MainActivity.class.isInstance(mContext)){
					((MainActivity)mContext).setVisibilityOfTrash(View.VISIBLE);
				}
			}
			return true;
		}
	}
	
	private void closeParrentIfInflated(View v){
		if(v.getParent() instanceof LinearLayout){
			LinearLayout menu  = (LinearLayout) v.getParent();
			if(menu.getParent() instanceof DismissableLayout){
				DismissableLayout layout = (DismissableLayout) menu.getParent();
				layout.dismiss();
			}
		}
	}

}
