package edu.mit.cameraCulture.vblocks;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import edu.mit.cameraCulture.vblocks.R.id;
import edu.mit.cameraCulture.vblocks.engine.PreviewActivity;
import edu.mit.cameraCulture.vblocks.predefined.Blur;
import edu.mit.cameraCulture.vblocks.predefined.Camera;
import edu.mit.cameraCulture.vblocks.predefined.Canny;
import edu.mit.cameraCulture.vblocks.predefined.ColorBlobDetector;
import edu.mit.cameraCulture.vblocks.predefined.DistanceMeter;
import edu.mit.cameraCulture.vblocks.predefined.DrawBox;
import edu.mit.cameraCulture.vblocks.predefined.EMailNotifier;
import edu.mit.cameraCulture.vblocks.predefined.GrayscaleJava;
import edu.mit.cameraCulture.vblocks.predefined.GrayscaleJavascript;
import edu.mit.cameraCulture.vblocks.predefined.GrayscaleNative;
import edu.mit.cameraCulture.vblocks.predefined.HistogramEqualization;
import edu.mit.cameraCulture.vblocks.predefined.If;
import edu.mit.cameraCulture.vblocks.predefined.IfOnce;
import edu.mit.cameraCulture.vblocks.predefined.LetterBasedGrader;
import edu.mit.cameraCulture.vblocks.predefined.OCR;
import edu.mit.cameraCulture.vblocks.predefined.OpticalFlow;
import edu.mit.cameraCulture.vblocks.predefined.Pixelize;
import edu.mit.cameraCulture.vblocks.predefined.RemoteVideo;
import edu.mit.cameraCulture.vblocks.predefined.ScreenOutput;
import edu.mit.cameraCulture.vblocks.predefined.TakePicture;
import edu.mit.cameraCulture.vblocks.predefined.augmenter.Augmenter;
import edu.mit.cameraCulture.vblocks.predefined.display3D.Renderer3D;
import edu.mit.cameraCulture.vblocks.ui.BlockDragListener;
import edu.mit.cameraCulture.vblocks.ui.BlockGroupView;
import edu.mit.cameraCulture.vblocks.ui.BlockView;
import edu.mit.cameraCulture.vblocks.ui.ModuleMenu;
import edu.mit.cameraCulture.vblocks.ui.TemplateView;
import edu.mit.cameraCulture.vblocks.ui.Trash;

/**
 * The Activity launched when Vision Blocks app starts.
 * It handles the interface where the user can select
 * blocks, and program their own Computer Vision enabled
 * application.
 * @author Camera Culture
 *
 */
public class MainActivity extends Activity implements View.OnClickListener {

	private Trash mDeleteButton;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i("VB", "OpenCV loaded successfully");
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
		
	private Context getActivityContext() { return this; }
	
	/**
	 * Creates the Drag and Drop screen and its components
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drag_drop);
		CreateMenu();
//		LinearLayout layout= (LinearLayout)findViewById(id.component);
//		for(int i = 0; i < layout.getChildCount(); i++ ){
//			View v = layout.getChildAt(i);
//			if(v instanceof Separator){
//				((Separator)v).setOnDragListener(new BlockDragListener(this));
//			}
//			
//			if(v instanceof BlockView){
//				((BlockView)v).setOnTouchListener(new BlockTouchListener(this));
//			}
//		}
		
		Button start = (Button)findViewById(id.buttonStart);
		start.setOnClickListener(this);
		
		mDeleteButton = (Trash) findViewById(R.id.delete_button);
		mDeleteButton.setOnDragListener(new BlockDragListener(this));
		//searchForModules();
	}
	
//	private void searchForModules(){
//		Intent i = new Intent("edu.mit.cameraCulture.vblocks.Module");
//		//startService(i);
//		List<ResolveInfo> result =  getPackageManager().queryIntentServices(i, 0);
//		
//		
//		if(result.size() > 0){
//            for(ResolveInfo info : result){
//                ServiceInfo servInfo = info.serviceInfo;
//                ComponentName name = new ComponentName(servInfo.applicationInfo.packageName, servInfo.name);
//
//                final String packageName = servInfo.applicationInfo.packageName;
//                final String className = servInfo.name;
//                
//                BlockReceiver receiver = new BlockReceiver(new Handler());
//        		receiver.setReceiver(new BlockReceiver.Receiver() {
//        			@Override
//        			public void onReceiveResult(int resultCode, Bundle resultData) {
//        				Log.d("TEST", "RECEIVER");
//        				try{
//	        				//Parcelable p = resultData.getParcelable("ServiceTag");
//	        				//String packageName = "com.example.mypackage";
//	        			    //String className = "com.example.mypackage.MyClass";
//	
//	        			    String apkName = getPackageManager().getApplicationInfo(packageName, 0).sourceDir;
//	        			    PathClassLoader myClassLoader =
//	        			        new dalvik.system.PathClassLoader(
//	        			                    apkName,
//	        			                ClassLoader.getSystemClassLoader());
//	        			    Class<?> handler = Class.forName(className, true, myClassLoader);
//	        			    Module m = (Module) handler.newInstance();
//	        			    if(m instanceof Module){
//	        			    	
//	        			    	Log.d("TEST", "GREAT");
//	        			    }
//	        			    else {
//	        			    	Log.d("TEST", "SAAD");
//	        			    }
//	        			    Log.d("TEST", "RECEIVER");
//        				} catch (Exception e){
//        					Log.e("VBLocks", e.getMessage());
//        				}
//        				Log.d("TEST", "RECEIVED");	
//        			}
//        		});
//        		i.putExtra("receiverTag", receiver);
//                
//                i.setComponent(name);
//
//                startService(i);
//            }
//        }
//		//this.getIntent().getExtras().getParcelable("VisionBlock");
//		
//		Log.d("TEST", "" + result.size());
//	}
	
	/**
	 * Set visibility of button <code>delete</code> according to <code>v</code>.
	 * @param v VISIBLE, INVISIBLE, or GONE
	 */
	public void setVisibilityOfTrash(int v){
		mDeleteButton.setVisibility(v);
	}
	
	/**
	 * Creates upper menu where you can select the blocks
	 * that will be used in client's program.
	 */
	private void CreateMenu(){
		LinearLayout menu = (LinearLayout)findViewById(id.menu);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		LinearLayout.LayoutParams lp = new LayoutParams((int) (metrics.density * 45 + 0.5f),(int) (metrics.density * 50 + 0.5f));
		lp.setMargins((int)(metrics.density * 3 + 0.5f), 0, (int)(metrics.density * 3 + 0.5f), 0);
		RelativeLayout frame = (RelativeLayout)findViewById(id.main_frame);

		// INPUT
		ArrayList<TemplateView> inputItems = new ArrayList<TemplateView>();
		inputItems.add(TemplateView.CreateTemplate(this, R.drawable.module_input_button,R.drawable.module_videocamera, Camera.class,Camera.getModuleName()));
		inputItems.add(TemplateView.CreateTemplate(this, R.drawable.module_input_button,R.drawable.module_videocamera, RemoteVideo.class,RemoteVideo.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	inputItems,R.drawable.module_input_button,R.drawable.module_input), lp);

		// FILTERS
		ArrayList<TemplateView> filterItems = new ArrayList<TemplateView>();
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, GrayscaleJavascript.class,GrayscaleJavascript.getModuleName()));
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, GrayscaleJava.class, GrayscaleJava.getModuleName()));
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, GrayscaleNative.class, GrayscaleNative.getModuleName()));
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, Canny.class, Canny.getModuleName()));
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, Pixelize.class, Pixelize.getModuleName()));
		filterItems.add(TemplateView.CreateTemplate(this, R.drawable.module_filter_button,R.drawable.module_vision, HistogramEqualization.class, HistogramEqualization.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	filterItems,R.drawable.module_filter_button,R.drawable.module_vision), lp);
		
		// PROCCESS ITEMS
		ArrayList<TemplateView> processItems = new ArrayList<TemplateView>();
		processItems.add(TemplateView.CreateTemplate(this, R.drawable.module_vision_button,R.drawable.module_vision, Augmenter.class,Augmenter.getModuleName()));
		processItems.add(TemplateView.CreateTemplate(this, R.drawable.module_vision_button,R.drawable.module_vision, Renderer3D.class,Renderer3D.getModuleName()));
		processItems.add(TemplateView.CreateTemplate(this, R.drawable.module_vision_button,R.drawable.module_vision, OpticalFlow.class,OpticalFlow.getModuleName()));
		processItems.add(TemplateView.CreateTemplate(this, R.drawable.module_vision_button,R.drawable.module_vision, ScreenOutput.class,ScreenOutput.getModuleName()));
		processItems.add(TemplateView.CreateTemplate(this, R.drawable.module_vision_button,R.drawable.module_vision, DistanceMeter.class,DistanceMeter.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	processItems,R.drawable.module_vision_button,R.drawable.module_vision), lp);

		// STRUCTURE ITEMS
		ArrayList<TemplateView> structureItems = new ArrayList<TemplateView>();
		structureItems.add(TemplateView.CreateTemplate(this, R.drawable.module_structure_button,R.drawable.module_if, If.class,If.getModuleName()));
		structureItems.add(TemplateView.CreateTemplate(this, R.drawable.module_structure_button,R.drawable.module_if, IfOnce.class,IfOnce.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	structureItems,R.drawable.module_structure_button,R.drawable.module_logic), lp);
		
		// ALERT ITEMS
		ArrayList<TemplateView> alertItems = new ArrayList<TemplateView>();
		alertItems.add(TemplateView.CreateTemplate(this, R.drawable.module_alert_button,R.drawable.module_alert, EMailNotifier.class,EMailNotifier.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	alertItems,R.drawable.module_alert_button,R.drawable.module_alert), lp);
		
		// APPLICATION ITEMS
		ArrayList<TemplateView> applicationsItems = new ArrayList<TemplateView>();
		//applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_filter, Grader.class, Grader.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, LetterBasedGrader.class, LetterBasedGrader.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, OCR.class, OCR.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, Blur.class, Blur.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, ColorBlobDetector.class, ColorBlobDetector.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, DrawBox.class, DrawBox.getModuleName()));
		applicationsItems.add(TemplateView.CreateTemplate(this, R.drawable.module_applications_button,R.drawable.module_shape, TakePicture.class, TakePicture.getModuleName()));
		menu.addView(new ModuleMenu(this,frame,	applicationsItems,R.drawable.module_applications_button,R.drawable.module_shape), lp);
		
	}

	/**
	 * Prepare the <code>id.component</code> to receive modules.
	 * Also, restore previous module collection.
	 */
	@Override
	protected void onStart() {
		Module program = VBlocksApplication.getProgram();
		if(program != null){
			BlockGroupView blockGroup = (BlockGroupView)findViewById(id.component);
			blockGroup.clearAllChildren();
			blockGroup.addSeparator(this);
			restoreModuleCollection((ModuleCollection)program, blockGroup);
		}
		VBlocksApplication.setProgram(null);
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	/**
	 * Save the current modules in the <code>VBlocksApplication</code>
	 * so it can be restarted later.
	 */
	@Override
	protected void onStop() {
		if(VBlocksApplication.getProgram() == null){
			VBlocksApplication.setProgram(getModules());
		}
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/** Treats a click in an object.
	 * For now only click handled is when Start Button is pressed.
	 * Creates a new <code>EngineActivity</code> with the blocks
	 * chosen by the user.
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getActivityContext(), PreviewActivity.class);
		VBlocksApplication.setProgram(getModules());
		startActivity(intent);
	}
	
	/**
	 * Pack all modules present in <code>R.id.component</code> and return it.
	 * @return Collection of all modules in the BlockGroupView.
	 */
	public Module getModules(){
		BlockGroupView layout = (BlockGroupView)findViewById(R.id.component);
		return layout.packModules();
	}
	
	private void restoreModuleCollection(ModuleCollection collection, BlockGroupView blocks){
		for(int i = 0; i < collection.size(); i++){
			Module m = collection.get(i);
			if(m instanceof ModuleCollection) {
				BlockGroupView bg = BlockGroupView.createBlock(this, (ModuleCollection)m);
				restoreModuleCollection((ModuleCollection)m, bg);
				blocks.addBlock(bg,this);
			} else {
				blocks.addBlock(BlockView.createBlock(this, m),this);
			}
		}
		//collection.clear();
		
	}
	
}
