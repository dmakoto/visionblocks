package edu.mit.cameraCulture.vblocks.predefined;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.CommitableView;

public class EMailNotifierConfiguration extends CommitableView {

	private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

	private EMailNotifier mModule;
	private EditText mMailEditTextView;
	private EditText mEditTextView;
	
	public EMailNotifierConfiguration(Context context, EMailNotifier module) {
		super(context);
		
		mModule = module;
		
		TextView mailTxtView = new TextView(context);
		mailTxtView.setText("E-mail address:");
		this.addView(mailTxtView,params);
		
		mMailEditTextView = new EditText(context);
		mMailEditTextView.setText(mModule.getMailAddress());
		this.addView(mMailEditTextView,params);
		
		TextView txtView = new TextView(context);
		txtView.setText("Text:");
		this.addView(txtView,params);
		
		mEditTextView = new EditText(context);
		mEditTextView.setText(mModule.getMailText());
		this.addView(mEditTextView,params);
	}

	@Override
	public void commit() {
		mModule.setMailAddress(mMailEditTextView.getText().toString());
		mModule.setMailText(mEditTextView.getText().toString());
	}

}
