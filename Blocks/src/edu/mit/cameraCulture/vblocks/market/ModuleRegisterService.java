package edu.mit.cameraCulture.vblocks.market;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ModuleRegisterService extends IntentService {

	public static final String REGISTER_SERVICE_NAME = "ModuleRegisterService";
	
	public ModuleRegisterService() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
	      //long endTime = System.currentTimeMillis() + 5*1000;
	      Log.d("ModuleRegisterService", "Processing intent request");
	      //while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	        //          wait(endTime - System.currentTimeMillis());
	              } catch (Exception e) {
	              }
	          }
	      //}
	}

}
