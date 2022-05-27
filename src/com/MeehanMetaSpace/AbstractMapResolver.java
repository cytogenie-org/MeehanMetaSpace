package com.MeehanMetaSpace;
import java.util.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public abstract class AbstractMapResolver
	implements MapResolver{
  ArrayList listeners=new ArrayList();

  public void addKeySelectionListener(Listener k){
	listeners.add(k);
  }
  
  private Object lastSelectedKey;
  public void valueSelected(final Object key){
	  lastSelectedKey=key;
	  refreshPossibilities(key);	
  }
  
  public final void refreshLastSelected() {
	  if (lastSelectedKey!=null){
		  refreshPossibilities(lastSelectedKey);
	  }
  }
  
  public void refreshPossibilities(final Object key) {
	  for (final Iterator it=listeners.iterator(); it.hasNext(); ){
		  final Listener l=(Listener) it.next();
		  l.notifyKeySelected(this, key);
		}
  }

  public void notifyDeactualized(Object key, Object value){
	for (final Iterator it=listeners.iterator(); it.hasNext(); ){
	  final Listener l=(Listener) it.next();
	  l.notifyDeactualized(this, key, value);
	}
  }

  public void notifyActualized(Object key, Object value){
	for (final Iterator it=listeners.iterator(); it.hasNext(); ){
	  final Listener l=(Listener) it.next();
	  l.notifyActualized(this, key, value);
	}
  }
}

