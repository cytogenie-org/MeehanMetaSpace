package com.MeehanMetaSpace.swing;

import java.util.ArrayList;
import com.MeehanMetaSpace.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import com.MeehanMetaSpace.swing.DefaultFilterable.Filter;

class MultiQueryFilter {
	
	private final PersonalizableTableModel tableModel;
	private final ArrayList<QuerySet> querySets=new ArrayList<QuerySet>();
	MultiQueryFilter(final PersonalizableTableModel model){
		tableModel=model;
		QuerySet set=new QuerySet(null);
		set.setFilter(new LinkedHashMap<Integer, Filter>());
		querySets.add(set);
	}
	
	
	
	boolean isMultiFilterSet() {
		if (querySets.size() > 1) {
			if (querySets.size() == 2) {
				QuerySet set = querySets.get(1);
				return set.isValidQuerySet();
			}
			return true;
		}
		return false;
	}
	
	QuerySet getCurrentQuerySet(){
		if(querySets.size()>0){
			return querySets.get(current);
		}		
		return null;
	}
	private int current=0;
	
	void updateCurrentQuerySet(final QuerySet set){
		if(querySets.size()>0) {
			querySets.set(current,set);
		}
	}
	
	boolean isFilterable() {
		for (final QuerySet set : querySets) {
			if (set.isValidQuerySet()) {
				return true;
			}
		}
		return false;
	}
	
	void addQuerySet(final Map<Integer,Filter> filter, final String operator){
		final QuerySet set=new QuerySet(operator);
		set.setFilter(filter);
		querySets.add(set);
		current=querySets.size()-1;
	}
	
	void clearFilters(){
		querySets.clear();
		current=0;
		final QuerySet set=new QuerySet(null);
		set.setFilter(new LinkedHashMap<Integer, Filter>());
		querySets.add(set);
	}
	
	String getText() {
		final Filter[][] filterSets = compile();
		final StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		int all=0;
		if (filterSets != null) {
			final int n = filterSets.length;
			int i = 0;
			for (final Filter[] filterSet : filterSets) {
				if (filterSets.length > 1) {
					sb.append("(");
				}
				final int n2 = filterSet.length;
				int i2 = 0;
				for (final Filter filter : filterSet) {
					if (all==firstCurrent){
						sb.append("<b>");
					}
					sb
							.append(" <u>"
									+ tableModel
											.getColumnAbbreviation(filter.dataColumnIndex)
									+ "</u>  <i>"
									+ Basics.encodeHtml(DefaultFilterable.allFilterOp[filter.op])
									+ "</i> <font color='blue'>" + filter.value +"</font>");
					if (i2 < n2 - 1) {
						sb.append("&nbsp;&nbsp;"+QuerySet.LOGICAL_AND+"&nbsp;&nbsp;" );
					}
					if (all==lastCurrent){
						sb.append("</b>");
					}
					all++;
					i2++;
				}
				if (filterSets.length > 1) {
					sb.append(")");
				}
				if (i++ < n - 1) {
					sb.append("&nbsp;&nbsp;" + QuerySet.LOGICAL_OR + "&nbsp;&nbsp;");
				}
			}
		}
		sb.append("</html>");
		return sb.toString();
	}
	
	
	static class QuerySet{
		static final String LOGICAL_AND="AND";
		static final String LOGICAL_OR="OR";
		private Map<Integer,Filter> filters=new LinkedHashMap<Integer,Filter>();
		private final String logicOperator;
		
		QuerySet(final String lOperator){
			this.logicOperator=lOperator;
		}
		
		boolean isValidQuerySet() {

			if (filters.size() > 0) {

				for (final Filter filter : filters.values()) {
					if (filter.op < 0 || filter.dataColumnIndex < 0
							|| Basics.isEmpty(filter.value)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		void setFilter(final Map<Integer,Filter> filters){
			this.filters=filters;
		}
		
		void updateFilter(final int dataColumnIndex, final Filter filter){
			filters.put(dataColumnIndex,filter);
		}
		
		void removeFilter(final int dataColumnIndex){
			filters.remove(dataColumnIndex);
		}
						
		Map<Integer,Filter> getFilters(){
			return filters;
		}
		
		
		String getLogicalOperator(){
		    return logicOperator==null?"":logicOperator; 	
		}
		
	}
	
	
	private int firstCurrent, lastCurrent;
	final Filter[][] compile() {
		firstCurrent=lastCurrent=-1;
		int ors = 0;
		int sets = 0;
		final int n = querySets.size();
		for (int j = 0; j < n; j++) {
			final QuerySet set = querySets.get(j);
			if (set.isValidQuerySet()) {
				sets++;
				final String logicalOperator = set.getLogicalOperator();
				if (QuerySet.LOGICAL_OR.equals(logicalOperator) && (j < n-1 && querySets.get(j+1).isValidQuerySet())) {
					ors++;
				}
			}
		}
		int all=0;
		Filter[][] v = null;
		if (sets > 0) {
			v = new Filter[ors + 1][];
			final List<Filter> l = new ArrayList<Filter>();
			int i = 0;
			for (int j = 0; j < n; j++) {
				
				final QuerySet set = querySets.get(j);
				if (set.isValidQuerySet()) {
					if (n>1 && j==current){
						firstCurrent=all;
						
					}
					for (final Integer key : set.filters.keySet()) {
						l.add(set.filters.get(key));
						all++;
					}
					if (n>1 && j==current){
						lastCurrent=all-1;
						
					}
					final boolean isLastOne=j == n - 1;
					if (!isLastOne) {
						if (QuerySet.LOGICAL_OR.equals(set.getLogicalOperator()) && (j < n-1 && querySets.get(j+1).isValidQuerySet())) {
							v[i++] = l.toArray(new Filter[l.size()]);
							l.clear();
						}
					}
				}
			}
			v[i] = l.toArray(new Filter[l.size()]);
		}
		return v;
	}
	
	QuerySet getCurrent(){
		return querySets.get(current);
	}
	
	boolean hasNext(){
		return current < querySets.size()-1;		
	}
	
	boolean hasPrevious(){
		return current >0;		
	}
	
	boolean next(final boolean delete){
		if (hasNext()){
			if (!delete){
			current++;
			} else{
				querySets.remove(current);
			}
			return true;
		}
		return false;
	}
	
	boolean previous(final boolean delete){
		if (hasPrevious()){
			current--;
			if (delete){
				querySets.remove(current+1);
			}
			return true;
		}
		return false;
	}

}
