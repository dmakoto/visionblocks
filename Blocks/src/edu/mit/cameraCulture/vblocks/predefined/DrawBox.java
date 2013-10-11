package edu.mit.cameraCulture.vblocks.predefined;

import org.opencv.core.Mat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;
import edu.mit.cameraCulture.vblocks.Module.ExecutionCode;
import edu.mit.cameraCulture.vblocks.predefined.ColorBlobDetector.ColorBlobView;

public class DrawBox extends Module{
	
	public static final String REGISTER_SERVICE_NAME = "Draw Box";
	
	
	private Mat box;
	
	private DrawBoxView view;	

	public DrawBox() {
		super(REGISTER_SERVICE_NAME);
		// TODO Auto-generated constructor stub
	}
	
	public ExecutionCode execute(Sample image) {
		
		if(image.getMat("RECT") != null){
			
			box = image.getMat("RECT");
			view.postInvalidate();
		}	
		
		return null;
	}
	
	@Override
	public String getName() {
		return "Draw Box";
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub		
	}
		
	public static String getModuleName(){
		return "Draw Box";
	}
	
	public void onCreate(EngineActivity context){
		super.onCreate(context);
		
		view = new DrawBoxView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		context.getLayout().addView(view,lp);
		
	}
	
	public class DrawBoxView extends View{
		
		private Paint paint;
		
		private float[] posMin = new float[2]; 
		private float[] posMax = new float[2]; 
		
		
		public DrawBoxView(Context context) {
			
			super(context);
		
			paint = new Paint();
			paint.setARGB(100, 255, 255, 0);
			paint.setStrokeWidth(3);
			
		}
		

		protected void onDraw(Canvas canvas) {
			
			if(box != null){
								
				box.get(0, 0, posMin);
				box.get(1, 0, posMax);
					
				//Log.d("debug", "rect = (" + posMin[0] + ", " + posMin[1] + "), (" + posMax[0] + "," + posMax[1] + ")");				
								
				if((posMin[0] < 1) && (posMin[1] < 1) && (posMax[0] < 1) && (posMax[1] < 1))
					canvas.drawRect(canvas.getWidth()*posMin[0], canvas.getHeight()*posMin[1], canvas.getWidth()*posMax[0], canvas.getHeight()*posMax[1], paint);
				else canvas.drawRect(posMin[0], posMin[1], posMax[0], posMax[1], paint);
				
			}
			
		}
		
		
		
	}
	
}
