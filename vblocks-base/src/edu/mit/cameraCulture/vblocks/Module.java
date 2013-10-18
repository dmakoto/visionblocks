package edu.mit.cameraCulture.vblocks;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

import org.opencv.core.Mat;

import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Dictates the blocks behavior. It represents the shape of the blocks.
 * A sample from the camera is given to the block through the <code>execute</code>
 * method as a Sample object.
 * @author CameraCulture
 *
 */
public abstract class Module extends IntentService {
	
	public enum PixelEncoding {
		yuv420,
		argb
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OutputBool { String[] vars(); }
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OutputFloat { String[] vars(); }
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OutputInt { String[] vars(); }
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OutputMat { String[] vars(); }
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ImagePixelType { PixelEncoding encoding(); }
	
	public enum ExecutionCode {
		NONE,
		EXIT_IMMEDIATELY,
		FINISH_CYCLE_AND_EXIT
	}
	
	
	protected EngineActivity mContext;

	public void onCreate(EngineActivity context){
		mContext = context;
	}
	
	public void onDestroyModule(){
		mContext = null;
	}
	
	public abstract ExecutionCode execute(Sample image);
	
	public abstract String getName();
	
	public CommitableView getConfigurationView(Context context){
		return null;
	}
	
	/**
	 * Constructor: Defines the name of the module as serviceName.
	 * @param serviceName the name of the Service
	 */
	public Module(String serviceName) {
		super(serviceName);
	}

	@Override
	protected abstract void onHandleIntent(Intent intent);
	
	
	public HashMap<String, Class> getVariables(){
		return Module.getVariables(this.getClass());
	}
	
	public static HashMap<String, Class> getVariables(Class classType){
		HashMap<String, Class> values = new HashMap<String, Class>();
		 	
		try {
			Annotation [] annotations = classType.getDeclaredMethod("execute",Sample.class).getAnnotations();
	        if (annotations != null) {	
            	for (Annotation annotation : annotations) {
            		if(annotation instanceof OutputBool){
            			addAllToMap(values,annotation,Boolean.class);
            		} else if(annotation instanceof OutputInt) {
            			addAllToMap(values,annotation,Integer.class);
            		} else if(annotation instanceof OutputFloat) {
            			addAllToMap(values,annotation,Float.class);
            		} else if(annotation instanceof OutputMat) {
            			addAllToMap(values,annotation,Mat.class);
            		}
				}
	        }
		} catch (Exception ex) {
        	Log.e("VisionBlocks", "Module: " + ex.toString());
        }
		return values;
	}
	
	private static void addAllToMap(HashMap<String, Class> map, Annotation annotation, Class type){
		try {
			String [] names = (String []) annotation.annotationType().getMethod("vars").invoke(annotation);
			for (String name : names) {
				map.put(name, type);
			}
		} catch (Exception e){
			Log.e("VisionBlocks", "If block: incorrect annotation for execute method ");
		}
	}
}
