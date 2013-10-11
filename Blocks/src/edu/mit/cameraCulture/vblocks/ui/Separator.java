package edu.mit.cameraCulture.vblocks.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class Separator extends View {

	
	
	public Separator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Separator(Context context) {
		super(context);
	}
	public static View CreateSeparator(Context context){
		View v = new Separator(context);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (metrics.density * 3 + 0.5f));
		
		v.setLayoutParams(lp);
		v.setOnDragListener(new BlockDragListener(context));
		return v;
	}
}
