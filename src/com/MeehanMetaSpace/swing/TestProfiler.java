package com.MeehanMetaSpace.swing;
import java.util.*;
import com.MeehanMetaSpace.Basics;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TestProfiler {
  final static Collection retainer=new ArrayList();
  final String []temp;
  public TestProfiler( final int n, final boolean retain) {
	temp=new String[n];
	for (int i=0;i<n;i++){
	  temp[i]=new String("Term # "+i+", the time is " + new Date().toGMTString());
}
	if (retain){
	  retainer.add(this);
}

  }

  public static void main(String []args){
	Integer times=new Integer(120);
	boolean retain=true;

	while ( !PopupBasics.getBoolean("Done?", false)){
	  times=PopupBasics.getIntegerFromUser("How many?", times);
	  retain=PopupBasics.getBoolean("Retain?", retain);
	  for (int i=0;i<times.intValue();i++){
		new TestProfiler(times.intValue(), retain);
	  }
}

}

}
