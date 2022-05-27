package com.MeehanMetaSpace.swing;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class CellAdvice {
	public final static int TYPE_NORMAL=0, TYPE_INCOMPLETE=1, TYPE_ERROR=2;

	int type=TYPE_NORMAL;
	public String toolTip="";
	boolean isEditing=false;
public int getType(){
	  return type;
  }
	public CellAdvice() {
		this.type=type;
		this.toolTip=toolTip;
	}

	public void set(final int type, final String toolTip){
		this.type=type;
		this.toolTip=toolTip;
	}

	public void setToolTip(final String toolTip){
		this.toolTip = toolTip;
	}

	public void set(String toolTip){
		this.toolTip=toolTip;
		this.type=TYPE_NORMAL;
	}

	public void clear(){
		this.type=TYPE_NORMAL;
		this.toolTip=null;
	}
}