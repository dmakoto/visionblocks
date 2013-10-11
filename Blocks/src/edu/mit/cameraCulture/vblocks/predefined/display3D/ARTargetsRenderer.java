package edu.mit.cameraCulture.vblocks.predefined.display3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;

import com.qualcomm.QCAR.QCAR;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.R;

public class ARTargetsRenderer implements GLSurfaceView.Renderer
{
    public boolean mIsActive = false;

    /** Reference to main activity **/
    private EngineActivity mActivity;

    public void SetActivity(EngineActivity activity){
    	mActivity = activity;
    }

    /** Native function for initializing the renderer. */
    public native void initRendering();


    /** Native function to update the renderer. */
    public native void updateRendering(int width, int height);


    /** Called when the surface is created or recreated. */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        // Call native function to initialize rendering:
        initRendering();

        // Call QCAR function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        QCAR.onSurfaceCreated();
    }


    /** Called when the surface changed size. */
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        if (fb != null) {
    		fb.dispose();
    	}
    	fb = new FrameBuffer(width, height);

        // Call native function to update rendering when render surface
        // parameters have changed:
        updateRendering(width, height);

        // Call QCAR function to handle render surface size changes:
        QCAR.onSurfaceChanged(width, height);
    }


    /** The native render function. */
    public native void renderFrame();


    /** Called to draw the current frame. */
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Update render view (projection matrix and viewport) if needed:
        //mActivity.updateRenderView();

        // Call our native function to render content
        renderFrame();
        
        updateCamera();
        world.renderScene(fb);
        world.draw(fb);
        fb.display(); 
    }
    
	private World world = null;
	private FrameBuffer fb = null;
	private Object3D cube = null;
	private Light sun = null;
	private Camera cam = null; 
    
    public ARTargetsRenderer(EngineActivity activity){
        this.mActivity = activity;
        world = new World();
        world.setAmbientLight(20, 20, 20);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);
        
        // Create a texture out of the icon...:-)
        com.threed.jpct.Texture texture = new com.threed.jpct.Texture(
				BitmapHelper.rescale(
						BitmapHelper.convert(activity.getResources().getDrawable(R.drawable.ic_launcher)), 64, 64));
		TextureManager.getInstance().addTexture("texture", texture);
	
		
         
		Resources res = activity.getResources();
 //       Object3D[] model = Loader.loadOBJ(
 //       		res.openRawResource(R.raw.tensor_object), 
 //       		res.openRawResource(R.raw.tensor_material), 10);
 //       Object3D o3d = model[0];//new Object3D(0);
 //       o3d.build();
 //       o3d.strip();
        
		cube = Primitives.getCube(1);
		cube.calcTextureWrapSpherical();
		cube.setTexture("texture");
		cube.strip();
		cube.build();
        
        //world.addObject(o3d);
		world.addObject(cube);
        cam = world.getCamera();
	
		SimpleVector sv = new SimpleVector();
		sv.set(cube.getTransformedCenter());
		sv.y -= 100;
		sv.z -= 100;
		sun.setPosition(sv);
		MemoryHelper.compact();
    }
    
    private float [] modelViewMat = null;
    private float mFov = 0;
    private float  mFovy = 0;
    
    public void updateCamera() {
    	if(modelViewMat != null){
    		Matrix m = new Matrix();
	    	m.setDump(modelViewMat);
	        cam.setBack(m);
	        cam.setFOV(mFov);
    		cam.setYFOV(mFovy);
    	}
    }
    
    public void setFov(float m) {
    	mFov = m;
    }
    
    public void setFovy(float m) {
    	mFovy = m;
    }
    
    public void updateModelviewMatrix(float mat[]) {
        modelViewMat = mat;
    }
}