package edu.mit.cameraCulture.vblocks.ui;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.ModuleTouchListener;

/**
 * Represents the top-most view when the devised application is running. It is a
 * RelativeLayout, but all touches are made so it can pass the touch information
 * to a module, which can be set by the client.
 * 
 * It implements all ModuleTouchListener methods. Any changes made in the
 * ModuleTouchListener will effect here. When adding another listener to the
 * ModuleTouchListener interface, it is mandatory to implement the touch event
 * delegation to the current ModuleListener instance.
 * 
 * @author CameraCulture
 * 
 */
public class MainView extends RelativeLayout implements ModuleTouchListener {

	private ModuleTouchListener mCurrentListener;

	/**
	 * Constructor method. Creates a RelativeLayout. The only difference is that
	 * all touchListeners are this object itself.
	 * 
	 * @param context the Context that has this view.
	 */
	public MainView(Context context) {
		super(context);

		// Configure itself as a listener for all touch actions in
		// ModuleTouchListener
		this.setOnClickListener(this);
		this.setOnTouchListener(this);
		// TODO: other touch things.
	}

	/**
	 * Sets which module will receive all touch events recognized by this class.
	 * 
	 * @param listener
	 *            the ModuleListener. Each module has one
	 */
	public void setModuleTouchListener(ModuleTouchListener listener) {
		mCurrentListener = listener;
	}

	/*
	 * Recursively calls the methods of mCurrentListener. This way it delegates
	 * the touch event forward.
	 */

	@Override
	public void onClick(View arg0) {
		Log.d("MainView", "onClick");
		if (mCurrentListener != null)
			mCurrentListener.onClick(arg0);
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		Log.d("MainView", "onDoubleTap");
		if (mCurrentListener != null)
			return mCurrentListener.onDoubleTap(arg0);
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		Log.d("MainView", "onDoubleTapEvent");
		if (mCurrentListener != null)
			return mCurrentListener.onDoubleTapEvent(arg0);
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		Log.d("MainView", "onSingleTapConfirmed");
		if (mCurrentListener != null)
			return mCurrentListener.onSingleTapConfirmed(arg0);
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("MainView", "onTouch");
		if (mCurrentListener != null)
			return mCurrentListener.onTouch(arg0, arg1);
		return false;
	}
}
