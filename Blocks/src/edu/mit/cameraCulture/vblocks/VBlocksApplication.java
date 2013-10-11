package edu.mit.cameraCulture.vblocks;

import android.app.Application;

/**
 * An Android Application class that has modules in it.
 * @author CameraCulture
 *
 */
public class VBlocksApplication extends Application {

	private static Module mCvProgram = null;
	
	public static void setProgram(Module sequence){
		mCvProgram = sequence;
	}

	public static Module getProgram(){
		return mCvProgram;
	}

	
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
