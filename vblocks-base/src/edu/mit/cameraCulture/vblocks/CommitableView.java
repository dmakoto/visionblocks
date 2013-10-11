package edu.mit.cameraCulture.vblocks;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * An abstract class that displays the module configuration view.
 * Each module that needs a configuration view needs to have a Config
 * class that extends CommitableView.
 * The method <code>commit</code> is called when the user submit
 * the configuration by pressing the OK button on screen.
 * @author Camera Culture
 *
 */
public abstract class CommitableView extends LinearLayout {
	public CommitableView(Context context) {
		super(context);
		this.setOrientation(LinearLayout.VERTICAL);
	}

	public abstract void commit();
}
