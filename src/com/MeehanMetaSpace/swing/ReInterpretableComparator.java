package com.MeehanMetaSpace.swing;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.util.*;
import com.MeehanMetaSpace.*;

public class ReInterpretableComparator
  implements Comparator
{
	final SortValueReinterpreter svr;
	public ReInterpretableComparator(final SortValueReinterpreter svr){
		this.svr=svr;
	}

	public int compare(final Object one, final Object two /* buckle my shoe*/)  {
	  final Comparable v1=svr.reinterpret(one), v2=svr.reinterpret(two);
	  if (v1 instanceof String && v2 instanceof String){
			return ((String)v1).compareToIgnoreCase( (String) v2 ) ;
	  }
	  return Basics.compareTo(v1, v2);
  }
}

