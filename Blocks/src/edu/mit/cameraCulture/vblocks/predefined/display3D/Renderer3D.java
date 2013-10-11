package edu.mit.cameraCulture.vblocks.predefined.display3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.qualcomm.QCAR.QCAR;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.R;
import edu.mit.cameraCulture.vblocks.Sample;

public class Renderer3D extends Module {

	public static final String REGISTER_SERVICE_NAME = "Renderer3D";
	
	public Renderer3D() {
		super(REGISTER_SERVICE_NAME);
	}

	@Override
	public ExecutionCode execute(Sample image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(EngineActivity context) {
		super.onCreate(context);
		
	}
	
	
	@Override
	public String getName() {
		return getModuleName();
	}
	
	public static String getModuleName(){
		return "3D renderer";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		

	}

	
	
}
