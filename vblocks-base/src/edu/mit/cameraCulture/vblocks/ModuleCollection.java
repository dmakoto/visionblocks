package edu.mit.cameraCulture.vblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

/**
 * A Collection of <code>Module</code>. Has methods from Collection class.
 * Also behaves as a single <code>Module</code>, implementing
 * the behavior of a Module, but with more instances.
 * @author CameraCulture
 *
 */
public class ModuleCollection extends Module implements Collection<Module> {
	private ArrayList<Module> modules;
	private String mName;
	
	public ModuleCollection(String serviceName) {
		super(serviceName);
		modules = new ArrayList<Module>();
	}
	
	//
	//	Methods related to Collection interface
	//  Just wrapping  
	//
	
	@Override
	public boolean add(Module o) {
		return modules.add(o);
	}

	@Override
	public boolean addAll(Collection<? extends Module> os) {
 		return modules.addAll(os);
	}

	@Override
	public void clear() {
		modules.clear();
	}

	@Override
	public boolean contains(Object o) {
		return modules.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> os) {
		return modules.containsAll(os);
	}

	@Override
	public boolean isEmpty() {
		return modules.isEmpty();
	}

	@Override
	public Iterator<Module> iterator() {
		return modules.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return modules.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> os) {
		return modules.removeAll(os);
	}

	@Override
	public boolean retainAll(Collection<?> os) {
		return retainAll(os);
	}

	@Override
	public int size() {
		return modules.size();
	}

	@Override
	public Object[] toArray() {
		return modules.toArray();
	}

	@Override
	public <T> T[] toArray(T[] os) {
		return modules.toArray(os);
	}
	
	/**
	 * Search the <code>Module</code> in the i-th position.
	 * Requires that the object has at least i elements.
	 * @param i Index of the Module
	 * @return Module in the i-th position
	 */
	public Module get(int i) {
		return modules.get(i);
	}
	
	//
	//	
	//

	@Override
	public void onCreate(EngineActivity context) {
		super.onCreate(context);
		for(Module m: this){
			m.onCreate(context);
		}
	};
	
	@Override
	public ExecutionCode execute(Sample image) {
		for(int i = 0; i < this.size(); i++){
			get(i).execute(image);
		}
		return ExecutionCode.NONE;
	}
	
	@Override
	public void onDestroyModule() {
		for(Module m: this){
			m.onDestroyModule();
		}
	};
	

	@Override
	public String getName() {
		return mName;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public HashMap<String, Class> getVariables(){
		HashMap<String, Class> vars = new HashMap<String, Class>();
		for(int i = 0; i < this.size(); i++){
			vars.putAll(get(i).getVariables());
		}
		return vars;
	}
	
	public List<Module> getModuleList() {
		List<Module> moduleList = new ArrayList<Module>();
		
		for(Module m : this) {
			
			if(m instanceof ModuleCollection)
				moduleList.add(m);
		
			List<Module> sublist = m.getModuleList();
			for(Module sub : sublist)
				moduleList.add(sub);
		}
		
		return moduleList;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}
}
