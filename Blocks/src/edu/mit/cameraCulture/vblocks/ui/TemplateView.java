package edu.mit.cameraCulture.vblocks.ui;

import java.lang.reflect.Type;

import edu.mit.cameraCulture.vblocks.R;
import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TemplateView extends LinearLayout {

	//public TemplateView(Context context, AttributeSet attrs, int defStyle) {
	//	super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	//}

	//public TemplateView(Context context, AttributeSet attrs) {
	//	super(context, attrs);
		
		// TODO Auto-generated constructor stub
	//}

	private Type mModuleType;
	private int mColor;
	
	public TemplateView(Context context) {
		super(context);
	}
	
	public static TemplateView CreateTemplate(Context context, int iconResId, int resId, Type moduleType, String text){
		TemplateView view = new TemplateView(context);
		
		view.setModuleType(moduleType);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (metrics.density * 50 + 0.5f), (int) (metrics.density * 50 + 0.5f));
		LinearLayout.LayoutParams lp_component = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (metrics.density * 50 + 0.5f));
		//LinearLayout.LayoutParams lp_text = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(lp_component);
		view.setBackgroundResource(R.color.blockBackground);//Color(Color.rgb(0x58,0x59,0x5b));;
		
		ImageView newView = new ImageView(context);
		//lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(metrics.density * 43 + 0.5f));
		newView.setLayoutParams(lp);
		newView.setBackgroundResource(iconResId);
		newView.setImageResource(resId);
		
		view.addView(newView);
		
		TextView txt = new TextView(context);
		txt.setText(text);
		view.addView(txt);
		
		view.setOrientation(HORIZONTAL);
		view.setOnTouchListener(new BlockTouchListener(context));
		return view;
	}

	public int getColor() {
		return mColor;
	}
	
	public void setColor(int c) {
		setBackgroundColor(c);
		mColor = c;
	}
	
	public Type getModuleType() {
		return mModuleType;
	}

	public void setModuleType(Type mModuleType) {
		this.mModuleType = mModuleType;
	}
	
}
