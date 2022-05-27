package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.Basics;
import java.util.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
public final class Indexer{
	final TreeMap<String,ArrayList<Row>> map = new TreeMap<String,ArrayList<Row>>();
	final int[] primaryKeyColumns;
	/**
	 *
	 * @param primaryKeyColumns the primary keys on which to distinctly index
	 * @param howManyPrimaryKeyColumns how many primary key columns? must be < primaryKeyColumns.length
	 * @throw  IndexOutOfBoundsException if howManyPrimaryKeys > primaryKeyColumns.length
	 */
	public Indexer(final int[] primaryKeyColumns, final int howManyPrimaryKeyColumns ){
		this.primaryKeyColumns = new int[howManyPrimaryKeyColumns];
		for (int i = 0; i < howManyPrimaryKeyColumns; i++) {
			this.primaryKeyColumns[i] = primaryKeyColumns[i];
		}
	}

	public void index(final Row row) {
		final String key = TableBasics.encode(row, primaryKeyColumns);
		ArrayList<Row> preExisting =  map.get(key);
		if (preExisting==null){
			preExisting=new ArrayList<Row>();
			map.put(key, preExisting);
		}
		if (!preExisting.contains( row )){
			preExisting.add(row);
		}
	}

	public String getKey(final Row row){
		return TableBasics.encode(row, primaryKeyColumns);
	}

	public int getIndexedRowCnt(final Row row){
		return getIndexedRowCnt( getKey(row));
	}

	public ArrayList getIndexedRows(final Row row) {
		return getIndexedRows(getKey(row));
	}

	public int getIndexedRowCnt(final String key){
		final ArrayList<Row> rows=getIndexedRows(key);
		return rows == null ? 0:rows.size();
	}

	public ArrayList<Row> getIndexedRows(final String key) {
		return map.get(key);
	}

	public float accumulate(final Row row, final int columnToAccumulate){
		return accumulate( getKey(row), columnToAccumulate);
	}

	public float accumulate(final String key, final int columnToAccumulate){
		float retVal=0;
		final ArrayList rows=getIndexedRows(key);
		if (rows != null ){
			for (final Iterator it=rows.iterator();it.hasNext();){
				retVal+=Basics.toFloat( (Float)( (Row)it.next()).get(columnToAccumulate) );
			}
		}
		return retVal;
	}

	public int size(){
		return map.size();
	}

	public Row getFirstRow(final String key){
		final ArrayList<Row> l=map.get(key);
		return l!=null && l.size()>0 ? l.get(0) : null;
	}


	public String []getKeys(){
		final Collection<String> c=map.keySet();
		return c.toArray(new String[c.size()]);
	}
}




