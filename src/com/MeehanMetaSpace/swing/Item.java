

package com.MeehanMetaSpace.swing;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import com.MeehanMetaSpace.*;

public class Item implements Comparable {

	public static final String CMD_ADD="Add", CMD_DELETE="Delete", CMD_REMOVE="Remove",
		   CMD_NEW="New", CMD_VIEW="View";


	static final String SEPARATOR = "SEPARATOR";
	final Object obj;
	final String cmd, toString;
	final StringConverter sc;
	String anomaly;


	void setAnomaly(final String anomaly){
		this.anomaly=anomaly;
	}

	public Item(
			final Object obj,
			final String  cmd,
			final boolean displayCmd,
			final boolean displayCls) {
		this.obj=obj;
		this.cmd=cmd;
		sc=DefaultStringConverters.get(obj.getClass());
		toString=makeToString(displayCmd, displayCls);
	}

	public Item(
			final Object obj,
			final String  cmd) {
		this.obj=obj;
		this.cmd=cmd;
		sc=DefaultStringConverters.get(obj.getClass());
		toString=makeToString(true, true);
	}

	public Item(final String cmd) {
		this.cmd=cmd;
		obj=null;
		sc=null;
		toString=makeToString(true, true);
	}

	public Item() {
		cmd=SEPARATOR;
		obj=null;
		sc=null;
		toString=makeToString(true, true);
	}


	public int compareTo(Object o){
		if (o instanceof Item){
			return toString().compareToIgnoreCase( o.toString());
		}
		return 0;
	}

	public int hashCode(){
		return toString().hashCode();
	}

	public boolean equals (Object o){
		if (o instanceof Item){
			return ( toString().equals(o.toString()));
		}
		return false;
	}

	final String getObjString(){
		return sc.toString(obj);
	}

	private String makeToString(final boolean displayCmd, final boolean displayObj){
		String str="";
		if (displayCmd && cmd!=null) {
			str = cmd;
			if (obj != null )  {
				str += " ";
			}
		}
		if (displayObj) {
		   str += getObjString();
		}
		return str;
	}

	public String toString(){
		return toString;
	}
}

