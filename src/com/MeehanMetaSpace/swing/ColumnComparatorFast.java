package com.MeehanMetaSpace.swing;

import java.util.*;

/**
 * Unlike ColumnComparatorFast this assumes all objects being compared are instances of Row with the *SAME* metaRow
 * @author Meehan
 *
 */
public class ColumnComparatorFast implements Comparator<Row> {
	private final SortInfo[] sortInfoArray;
	private final Comparator<Object>[] cmp;
	private final Map<Object,Object[]> cache;
	private final List<Row>dsr;
	private final PersonalizableDataSource ds;
	public ColumnComparatorFast(final PersonalizableDataSource ds, final SortInfo[] sortInfoArray) {
		this.sortInfoArray = sortInfoArray;
		cmp=new Comparator[sortInfoArray.length];
		for (int i=0;i<sortInfoArray.length;i++){
			sortInfoArray[i].setValueGetter();
			cmp[i]=sortInfoArray[i].ascending?ascendingCmp:descendingCmp;			
		}
		this.ds=ds;
		dsr=ds.getDataRows();
		originalSize=ds.size();
		//System.out.println("Fast caching sort on "+size+" rows and "+sortInfoArray.length+" columns");
		cache=new HashMap<Object,Object[]>(originalSize);
		for (int i=0;i<originalSize;i++){
			setCache(dsr.get(i));
		}
	}
	final int originalSize;

	private Object[]setCache(final Row row){
		Object[]l=new Object[sortInfoArray.length];			
		for (int i=0;i<sortInfoArray.length;i++) {
			l[i]= sortInfoArray[i].valueGetter.get((Row) row);
		}
		cache.put(row,l);
		return l;
	}

	private static final Comparator<Object> ascendingCmp=new Comparator<Object>(){
		public int compare(Object l, Object r){
			if (l instanceof String
					&& r instanceof String) {
				return ((String) l)
						.compareToIgnoreCase((String) r)
						;				
			}
			if (l instanceof Comparable
					&& r instanceof Comparable) {
					return ( (Comparable<Object>)l).compareTo(r);
			} else if (l instanceof Comparable) {
				return -1;
			} else if (r instanceof Comparable) {
				return 1;
			}	
			return 0;
		}
	};
	
	private static final Comparator<Object> descendingCmp=new Comparator<Object>(){
		public int compare(Object l, Object r){
			if (l instanceof String
					&& r instanceof String) {
				return ((String) r)
						.compareToIgnoreCase((String) l)
						;				
			}
			if (l instanceof Comparable
					&& r instanceof Comparable) {
					return ( (Comparable<Object>)r).compareTo(l);
			} else if (l instanceof Comparable) {
				return 1;
			} else if (r instanceof Comparable) {
				return -1;
			}	
			return 0;
		}
	};
	

	public int compare(Row one, Row two) {
		int result = 0;
		Object[] l, r;
		l = cache.get(one);
		r = cache.get(two);
		if (r==null){ // some threading issue?
			r=setCache(one);
		}
		if (l==null){
			l=setCache(two);
		}
		for (int i = 0; result == 0 && i < cmp.length; i++) {
			result = cmp[i].compare(l[i], r[i]);
		}
		return result;
	}
}

