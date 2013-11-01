package edu.mit.cameraCulture.vblocks.predefined;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.CommitableView;

public class CannyConfig extends CommitableView {

	private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

	private Canny cannyMod;
	
	private final TextView thresholdMinView;
	private final TextView thresholdMaxView;
	private SeekBar thresholdMinCBox;
	private SeekBar thresholdMaxCBox;
	private CheckBox blurCheckBox;
	private CheckBox maskCheckBox;
	
	
	public CannyConfig(Context context, Canny module) {
		super(context);
		
		cannyMod = module;
		
		/* Minimum Threshold ****************
		 * Text updated according to Slider */
		thresholdMinView = new TextView(context);
		thresholdMinView.setText("Min: "+cannyMod.getThresholdMin()+" pixels");
		this.addView(thresholdMinView,params);
		
		// Minimum Threshold Slider (SeekBar)
		thresholdMinCBox = new SeekBar(context);
		thresholdMinCBox.setMax(200);
		thresholdMinCBox.setProgress(cannyMod.getThresholdMin());
		thresholdMinCBox.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					thresholdMinView.setText("Min: "+seekBar.getProgress()+" pixels");
				}
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		this.addView(thresholdMinCBox,params);

	
		/* Maximum Threshold ****************
		 * Text updated according to Slider */
		thresholdMaxView = new TextView(context);
		thresholdMaxView.setText("Min: "+cannyMod.getThresholdMax()+ " pixels");
		this.addView(thresholdMaxView,params);
		
		// Minimum Threshold Slider (SeekBar)
		thresholdMaxCBox = new SeekBar(context);
		thresholdMaxCBox.setMax(200);
		thresholdMaxCBox.setProgress(cannyMod.getThresholdMax());
		thresholdMaxCBox.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					thresholdMaxView.setText("Max: "+seekBar.getProgress()+" pixels");
				}
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		this.addView(thresholdMaxCBox,params);
		
		
		// Blur Checkbox *******************
		blurCheckBox = new CheckBox(context);
		blurCheckBox.setText("Enable Blur");
		blurCheckBox.setChecked(cannyMod.getEnableBlur());
		this.addView(blurCheckBox, params);
		
		// Mask Checkbox *******************
		maskCheckBox = new CheckBox(context);
		maskCheckBox.setText("Set as mask");
		maskCheckBox.setChecked(cannyMod.getEnableMask());
		this.addView(maskCheckBox, params);
	}
	
	public void commit() {
		
		cannyMod.setThreshold(thresholdMinCBox.getProgress(),
								thresholdMaxCBox.getProgress() );
		cannyMod.setEnableBlur(blurCheckBox.isChecked());
		cannyMod.setEnableMask(maskCheckBox.isChecked());
	}

}
