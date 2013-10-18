package edu.mit.cameraCulture.vblocks.predefined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.MainActivity;
import edu.mit.cameraCulture.vblocks.ModuleCollection;
import edu.mit.cameraCulture.vblocks.Sample;

public class IfOnce extends ModuleCollection {

	public static final String REGISTER_SERVICE_NAME = "If Once";
	
	private String mExpression = "OUT_OF_BOUNDS"; 
	private HashMap<String, Class> mVariableMap;
	private List<String> mListOfVars;
	
	private boolean mWasTaken;
	public IfOnce() {
		super(REGISTER_SERVICE_NAME);
		mWasTaken = false;
	}

	@Override
	public ExecutionCode execute(Sample image) {
		if(image.getMat(mExpression) != null && !mWasTaken){
			mWasTaken = true;
			super.execute(image);
		}
		return ExecutionCode.NONE;
	}

	@Override
	public String getName(){
		return getModuleName() + ": " + mExpression;
	}
	
	public static String getModuleName(){
		return "IF ONCE";
	}
	
	@Override
	public CommitableView getConfigurationView(Context context) {
		return new IfConfigurableView(context);
	}
	
	private class IfConfigurableView extends CommitableView {
		private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		private Spinner mVariableSpinner;
		private Context mContext; 
		
		public IfConfigurableView(Context context) {
			super(context);
			mContext = context;
			TextView label = new TextView(context);
			label.setText("If");
			this.addView(label,params);
			
			mVariableSpinner = new Spinner(context);
			mVariableMap = ((MainActivity)context).getModules().getVariables();
			mListOfVars = new ArrayList<String>(mVariableMap.keySet());
			Collections.sort(mListOfVars);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, mListOfVars);
			mVariableSpinner.setAdapter(adapter);
			if(mVariableMap.containsKey(mExpression)){
				int i=0;
				while(!mListOfVars.get(i).equals(mExpression) && mListOfVars.size() > i++);
				mVariableSpinner.setSelection(i);
			}
			mVariableSpinner.setOnItemSelectedListener(new OperationSelector());
			this.addView(mVariableSpinner,params);
		}

		@Override
		public void commit() {
			mExpression =  (String) mVariableSpinner.getSelectedItem();
		}
		
		
		private class OperationSelector implements AdapterView.OnItemSelectedListener {
			private LinearLayout mOperationControl = null;
			
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int pos, long arg3) {
				Class c = mVariableMap.get(mListOfVars.get(pos));
				
				if(c == Boolean.class && !(mOperationControl instanceof BoolConfigurator)){
					if(mOperationControl != null) IfConfigurableView.this.removeView(mOperationControl);
					mOperationControl = new BoolConfigurator(IfConfigurableView.this.mContext);
					IfConfigurableView.this.addView(mOperationControl,params);
				} else if(c == Integer.class && !(mOperationControl instanceof NumberConfigurator)){
					if(mOperationControl != null) IfConfigurableView.this.removeView(mOperationControl);
					mOperationControl = new BoolConfigurator(IfConfigurableView.this.mContext);
					IfConfigurableView.this.addView(mOperationControl,params);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) { }
		}
		
		private class BoolConfigurator extends LinearLayout {

			private Spinner mBoolSpinner;
			
			public BoolConfigurator(Context context) {
				super(context);
				this.setOrientation(LinearLayout.VERTICAL);
				TextView label = new TextView(context);
				label.setText("Is equal to");
				this.addView(label,params);
				mBoolSpinner = new Spinner(context);
				ArrayAdapter<Boolean> adapter = new ArrayAdapter<Boolean>(context,android.R.layout.simple_spinner_item, new Boolean [] { true, false });
				mBoolSpinner.setAdapter(adapter);
				this.addView(mBoolSpinner,params);
			}
		} 
		
		private class NumberConfigurator extends LinearLayout {

			private Spinner mNumberOperationSpinner;
			
			public NumberConfigurator(Context context) {
				super(context);
				this.setOrientation(LinearLayout.VERTICAL);
				mNumberOperationSpinner = new Spinner(context);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, 
						new String [] { "is equal to (==)", 
										"is different than (!=)", 
										"is lower than (<)",
										"is lower equal than (<=)",
										"is greater than (>)",
										"is grather equal than (>=)"});
				mNumberOperationSpinner.setAdapter(adapter);
				this.addView(mNumberOperationSpinner,params);
				
				EditText numberBox = new EditText(context);
				this.addView(numberBox,params);
				
				
			}
		}
	}
}
