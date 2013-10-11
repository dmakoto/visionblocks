package edu.mit.cameraCulture.vblocks.ui;

import java.util.List;

import edu.mit.cameraCulture.vblocks.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

public class ModuleSpinnerAdapter extends ArrayAdapter<TemplateView> {

	private List<TemplateView> items;
	private Activity activity;
	
	public ModuleSpinnerAdapter(Activity activity, List<TemplateView> items) {
	    super(activity, android.R.layout.simple_list_item_1, items);
	    this.items = items;
	    this.activity = activity;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
	    //TextView v = (TextView) super.getView(position, convertView, parent);
		LayoutInflater inflater = activity.getLayoutInflater();
		LinearLayout v = (LinearLayout) inflater.inflate(R.layout.module_spinner_template, null);
		//v.addView(TemplateView.CreateTemplate(activity, items.get(position).getColor(),R.drawable.module_filter, items.get(position).getModuleType() ));
	    return v;
	}

	@Override
	public TemplateView getItem(int position) {
	    return items.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;//super.getView(position, convertView, parent);

	    if (v == null) {
	    	LayoutInflater inflater = activity.getLayoutInflater();
	        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.module_spinner_template, null);
//	        layout.addView(TemplateView.CreateTemplate(activity, items.get(position).getColor(),R.drawable.module_filter, items.get(position).getModuleType() ));
	        v =layout;
	    }
	    return v;
	}

}
