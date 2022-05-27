package com.MeehanMetaSpace.swing;

import java.util.Map;

public class TabFilter {
	
	
	public TabFilter(){
		filterColumnName=null;		
	}
	
	public boolean matches(final Map<String,String> namedValues){
		throw new UnsupportedOperationException("You must override TabFilter with a method matches(final Map<String,String> namedValues) ");
	}
	public boolean isFinished(){
		throw new UnsupportedOperationException("You must override TabFilter with a method isFinished() ");
	}
	
	private int filterColumnId = -1;
	private final String filterColumnName;
	private String filterColumnValues[];
	
	public TabFilter(String columnName, String[] values) {
		this.filterColumnName = columnName;
		this.filterColumnValues = values;
		
	}
	
	public boolean contains(String value) {
		for (int i = 0; i < filterColumnValues.length; i++) {
			if (filterColumnValues[i] != null && filterColumnValues[i].equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
	
	public int getFilterColumnId() {
		return filterColumnId;
	}
	
	public void setFilterColumnId(int filterColumnId) {
		this.filterColumnId = filterColumnId;
	}
	
	public String getFilterColumnName() {
		return filterColumnName;
	}
	
	
	public String[] getFilterColumnValues() {
		return filterColumnValues;
	}
	
	public void setFilterColumnValues(String[] filterColumnValues) {
		this.filterColumnValues = filterColumnValues;
	}
	
}