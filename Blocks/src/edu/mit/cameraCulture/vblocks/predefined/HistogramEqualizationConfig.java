package edu.mit.cameraCulture.vblocks.predefined;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import edu.mit.cameraCulture.vblocks.CommitableView;

public class HistogramEqualizationConfig extends CommitableView {

	private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

	private HistogramEqualization mod;
	
	private RadioGroup radioGroup;
	private RadioButton radioButton;
	
	private int mode = 0;
	
	public HistogramEqualizationConfig(Context context, HistogramEqualization module) {
		super(context);
		
		mod = module;
		radioGroup = new RadioGroup(context);
		radioButton = new RadioButton(context);
		radioButton.setId(0);
		radioButton.setText("YCrCb Format");
		if(radioButton.getId() == mod.getMode())
			radioButton.setChecked(true);
		radioGroup.addView(radioButton);
		
		radioButton = new RadioButton(context);
		radioButton.setId(1);
		radioButton.setText("HSV format");
		if(radioButton.getId() == mod.getMode())
			radioButton.setChecked(true);
		radioGroup.addView(radioButton);
		
		radioButton = new RadioButton(context);
		radioButton.setId(2);
		radioButton.setText("HSV format (With saturation)");
		if(radioButton.getId() == mod.getMode())
			radioButton.setChecked(true);
		radioGroup.addView(radioButton);
		
		radioButton = new RadioButton(context);
		radioButton.setId(3);
		radioButton.setText("Gray format");
		if(radioButton.getId() == mod.getMode())
			radioButton.setChecked(true);
		radioGroup.addView(radioButton);
		
		this.addView(radioGroup,params);
	}
	
	public void commit() {
		
		mod.setMode(radioGroup.getCheckedRadioButtonId());
	}
}
