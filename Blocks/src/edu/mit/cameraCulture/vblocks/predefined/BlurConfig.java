package edu.mit.cameraCulture.vblocks.predefined;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.CommitableView;

public class BlurConfig extends CommitableView {

	private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

	private Blur blurMod;
	
	private EditText blurVal;
	
	
	public BlurConfig(Context context, Blur module) {
		super(context);
		
		blurMod = module;

		TextView blurTxtView = new TextView(context);
		blurTxtView.setText("blur box size:");
		this.addView(blurTxtView,params);
		
		blurVal = new EditText(context);
		blurVal.setText(""+blurMod.getBlurSize());
		blurVal.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_NORMAL);
		this.addView(blurVal,params);
		
		
	}
	
	public void commit() {
		
		blurMod.setBlurSize(Integer.parseInt(blurVal.getText().toString()));
		
	}

}
