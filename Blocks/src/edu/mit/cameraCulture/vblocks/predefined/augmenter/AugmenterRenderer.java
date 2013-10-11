/*==============================================================================
            Copyright (c) 2012-2013 QUALCOMM Austria Research Center GmbH.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary

@file
    AugmenterRenderer.java

@brief
    Sample for Augmenter

==============================================================================*/


package edu.mit.cameraCulture.vblocks.predefined.augmenter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.qualcomm.QCAR.QCAR;

import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.R;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;


/** The renderer class for the ImageTargetsBuilder sample. */
public class AugmenterRenderer implements GLSurfaceView.Renderer
{
    public boolean mIsActive = false;

    /** Reference to main activity **/
    public EngineActivity mActivity;
    public Augmenter mAugmenter;


    /** Native function for initializing the renderer. */
    public native void initRendering();


    /** Native function to update the renderer. */
    public native void updateRendering(int width, int height);


    /** Called when the surface is created or recreated. */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
    	Log.d("QCAR","GLRenderer::onSurfaceCreated");

        // Call native function to initialize rendering:
        initRendering();

        // Call QCAR function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        QCAR.onSurfaceCreated();
    }


    /** Called when the surface changed size. */
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
    	Log.d("QCAR","GLRenderer::onSurfaceChanged");

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
    public native int renderFrame();


    /** Called to draw the current frame. */
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Update render view (projection matrix and viewport) if needed:
        mAugmenter.updateRenderView();

        // Call our native function to render content
        int numMarkers = renderFrame();
        
        
        updateCamera();
        if(numMarkers > 0) {
        	world.renderScene(fb);
        	world.draw(fb);
        	fb.display(); 
        }
        
    }
    
	private World world = null;
	private FrameBuffer fb = null;
	private Object3D cube = null;
	private Light sun = null;
	private Camera cam = null; 
    
    public AugmenterRenderer(EngineActivity activity, Augmenter augmenter){
        this.mActivity = activity;
        this.mAugmenter = augmenter;
        world = new World();
        world.setAmbientLight(20, 20, 20);

        sun = new Light(world);
        sun.setIntensity(250, 250, 250);
        
        // Create a texture out of the icon...:-)
        com.threed.jpct.Texture texture = new com.threed.jpct.Texture(
				BitmapHelper.rescale(
						BitmapHelper.convert(mActivity.getResources().getDrawable(R.drawable.ic_launcher)), 64, 64));
		TextureManager.getInstance().addTexture("texture", texture);
	
		Resources res = activity.getResources();
		
		
		Object3D[] model;
		
		try {
			model = Loader.load3DS( new BufferedInputStream(new FileInputStream(augmenter.getModelPath())),10f);
		} catch (Exception e){
			model = Loader.load3DS( res.openRawResource(R.raw.flayer), 10f);
		}
		
        Object3D scene = Object3D.mergeAll(model);
        scene.rotateX((float)Math.PI);
        scene.rotateZ((float)Math.PI);
        scene.translate(0, 0, -70);
        scene.invert();
        scene.setCulling(false);
        scene.strip();
        scene.build();
        
        world.addObject(scene);
         
		
//        Object3D[] model = Loader.loadOBJ(
//        		res.openRawResource(R.raw.tensor_object), 
//        		res.openRawResource(R.raw.tensor_material), 10);
//        Object3D o3d = model[0];//new Object3D(0);
//        o3d.build();
//        o3d.strip();
        
		//cube = Primitives.getCube(10);
		//cube.calcTextureWrapSpherical();
		//cube.setTexture("texture");
		//cube.strip();
		//cube.build();
        
        world.addObject(scene);
		//world.addObject(cube);
        cam = world.getCamera();
	
		SimpleVector sv = new SimpleVector();
		sv.set(scene.getTransformedCenter());//cube.getTransformedCenter());
		sv.y -= 100;
		sv.z -= 100;
		sun.setPosition(sv);
		MemoryHelper.compact();
    }
    
    public void release() {
    	TextureManager.getInstance().removeTexture("texture");
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
	