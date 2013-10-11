package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Calendar;

import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.Module.ExecutionCode;
import edu.mit.cameraCulture.vblocks.predefined.DrawBox.DrawBoxView;

public class TakePicture extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Take Picture";
	
	private Calendar calend;
	
	public TakePicture() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}
	
	public ExecutionCode execute(Sample image) {
		
		//convert to bitmap and save
		return null;
		
	}
	
	public String getName() {
		return "Take Picture";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub		
	}
	
	public static String getModuleName(){
		return "Take Picture";
	}
	
	public void onCreate(EngineActivity context){
		super.onCreate(context);
	
		calend = Calendar.getInstance();
		
	}

}
