package edu.mit.cameraCulture.vblocks.ui;

import java.lang.reflect.Type;

import edu.mit.cameraCulture.vblocks.MainActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.ModuleCollection;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class BlockDragListener implements OnDragListener {
	
	private Context mContext;
	private LinearLayout.LayoutParams separatorNormal;
	private LinearLayout.LayoutParams separatorSelected;
	
	
	public BlockDragListener(Context context){
		mContext = context;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		separatorNormal = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (metrics.density * 3 + 0.5f));
		separatorSelected = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (metrics.density * 30 + 0.5f));
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		Separator sep;
		switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				//no action necessary
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				if(v instanceof Separator){
					sep = (Separator) v;
					sep.setLayoutParams(separatorSelected);
				}
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				if(v instanceof Separator){
					sep = (Separator) v;
					sep.setLayoutParams(separatorNormal);
				}
				break;
			case DragEvent.ACTION_DROP:
				
				if(v instanceof Separator){	
					Separator dropTarget = (Separator) v;
					dropTarget.setLayoutParams(separatorNormal);
					
					View view = (View) event.getLocalState();
					
					if (view instanceof BlockView){
						BlockView dropped = (BlockView) view;
						int line  = ((LinearLayout)dropped.getParent()).indexOfChild(dropped)-1;
						if(line >= 0){
							((LinearLayout)dropped.getParent()).removeViewAt(line);
						}
						((LinearLayout)dropped.getParent()).removeView(dropped);
					} else if (view instanceof BlockGroupView) {
						BlockGroupView dropped = (BlockGroupView) view;
						int line  = ((LinearLayout)dropped.getParent()).indexOfChild(dropped)-1;
						if(line >= 0){
							((LinearLayout)dropped.getParent()).removeViewAt(line);
						}
						((LinearLayout)dropped.getParent()).removeView(dropped);
					} else if (view instanceof TemplateView) {
						try{
							Type t = ((TemplateView)view).getModuleType();
							Module m  = (Module)((Class<?>)t).newInstance();
							if(m instanceof ModuleCollection){
								view = BlockGroupView.createBlock(mContext,(ModuleCollection) m);
							} else {
								view = BlockView.createBlock(mContext, m);
							}
						} catch (Exception e){
							Log.e("VisionBlocks",e.getMessage());
						}
					} 
					
					try{
						int targetIndex = ((LinearLayout)dropTarget.getParent()).indexOfChild(dropTarget);
						((LinearLayout)dropTarget.getParent()).addView(view,targetIndex+1);
						
						((LinearLayout)dropTarget.getParent()).addView(Separator.CreateSeparator(mContext),targetIndex+2);
					} catch(Exception e){
							Log.e("VisionBlocks",e.getMessage());
					}
				} else if(v instanceof Trash){
					View view = (View) event.getLocalState();
					if (view instanceof BlockView || view instanceof BlockGroupView){
						int line  = ((LinearLayout)view.getParent()).indexOfChild(view)-1;
						if(line >= 0){
							((LinearLayout)view.getParent()).removeViewAt(line);
						}
						((LinearLayout)view.getParent()).removeView(view);
					} 

				}
				if(MainActivity.class.isInstance(mContext)){
					((MainActivity)mContext).setVisibilityOfTrash(View.GONE);
				}
				break;
				
			case DragEvent.ACTION_DRAG_ENDED:
				if(MainActivity.class.isInstance(mContext)){
					((MainActivity)mContext).setVisibilityOfTrash(View.GONE);
				}
				break;
			default:
				break;
		}
		return true;
	}

}
