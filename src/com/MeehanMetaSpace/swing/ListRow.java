package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.Basics;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class ListRow implements Row {
	
	public boolean isActive() {
		return true;
	}
	 
	public String getRowId() {
		return null;
	}
	
	public String getRowType() {
		return null;
	}
	
  public Object getRenderOnlyValue(final int dataColumnIndex, boolean isSelected, boolean hasFocus){
	return null;
  }
  public void endImport(){
}


	public Icon getIcon(int dataColumn){
		return null;
	}


	public boolean isDeletable(){
		return true;
	}


	public com.MeehanMetaSpace.swing.MetaRow getMetaRow() {
		return null;
	}

	public Collection getUnselectableValues(final int dataColumnIndex) {
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	public java.util.Collection getAllowedValues(int dataColumnIndex) {
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	public boolean allowNewValue(int columnDataIndex) {
		return false;
	}


	public boolean isExplorable(int dataIndex)	{
		return false;
	}

	public void explore(int dataIndex) {
	}

	public boolean isEditable(int dataIndex)
	{
		return true;
	}

	java.util.List l;
	public ListRow(java.util.List l)
	{
		this.l=l;
	}

	public int getColumnCount()
	{
		return l.size();
	}
	public void set(int index, Object element)
	{
		this.l.set(index, element);
	}

	public boolean contains(final String value){
		for (final Object o:l){
			if ( o != null && o.toString().contains(value)){
				return true;
			}
		}
		return false;
	}
	public Object get(final int dataColumnIndex)
	{
		if (dataColumnIndex<0){
			return null;
		}
		return this.l.get(dataColumnIndex);
	}

	public Class getClass(int index)
	{
		Object o=this.l.get(index);
		if (o==null)return String.class;
		return o.getClass();
	}


	static public Object [] toObjectArray(Row r)
	{
		final int n=r.getColumnCount();
		final Object []a=new Object[n];
		for (int i=0;i<n;i++){
			a[i]=r.get(i);
		}
		return a;
	}

	public boolean setAdvice(int dataIndex, CellAdvice cellAdvice){
		//cellAdvice.set(dataIndex%3, "Enter a value, or pop up menu for more options");
		cellAdvice.set("Enter a value, or right-click for more options.");

		return true;
	}
	public Object getFilterableValue(int dataColumnIndex) {
		return get(dataColumnIndex);
	}
	public Collection getForbiddenValues(int dataColumnIndex) {
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	public boolean shouldAllowedValuesBeSorted(int dataColumnIndex) {
		return true;
	}

}
