package com.MeehanMetaSpace.swing;

import java.util.*;

public class SortInfo {
    int dataColumnIndex;
    int sortOrder;
    boolean ascending;

    public boolean equals(final Object other) {
        if (!(other instanceof SortInfo)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final SortInfo that = (SortInfo) other;
        if (this.dataColumnIndex != that.dataColumnIndex) {
            return false;
        }
        if (this.sortOrder != that.sortOrder) {
            return false;
        }
        if (this.ascending != that.ascending) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (dataColumnIndex);
        result = 37 * result + (sortOrder);
        result = 37 * result + (ascending ? 29 : 13);
        return result;
    }
    
    abstract class ValueGetter{
    	abstract Object get(final Row row);
    }
    
    ValueGetter valueGetter;
    private final MetaRow metaRow;
    void setValueGetter(){
    	if (sequence!=null){
    		valueGetter = new ValueGetter(){
				Object get(final Row row) {
					return sequence.indexOf(PersonalizableTableModel.toSequenceableString(row, dataColumnIndex));
				}    			
    		};
    	} else {
    		final SortValueReinterpreter svr = metaRow == null ? null :
    			metaRow.getSortValueReinterpreter(dataColumnIndex);
    		if (svr != null) {
    			valueGetter=new ValueGetter(){
					Object get(Row row) {
						 return svr.reinterpret(row.get(dataColumnIndex));
					}    				
    			};
    		} else {
    			valueGetter=new ValueGetter(){
    				Object get(Row row){
    					return row.get(dataColumnIndex);
    				}
    			};
    		}
    	}
    }
    
    Object getSortValue(final Row row) {
		if (sequence != null) {
			final String s1=PersonalizableTableModel.toSequenceableString(row, dataColumnIndex);
			final Integer i1=sequence.indexOf(s1);
			return i1;
		}else {
			final Object value=TableBasics.getSortableValue(row, dataColumnIndex);
			return value;
		}
	}
    List sequence;
    void initSequence() {
    	if (metaRow == null || dataColumnIndex < 0 || dataColumnIndex >= metaRow.size()) {
    		sequence=null;
    	}else {
			final String columnIdentifier=metaRow.getDataColumnIdentifier(dataColumnIndex);
	    	sequence=PersonalizableTableModel.getSortSequence(metaRow, columnIdentifier);
    	}
    }
    public SortInfo(final MetaRow metaRow, final int dataColumnIndex, final int sortOrder) {
    	this.dataColumnIndex = dataColumnIndex;
        this.sortOrder = sortOrder;
        this.ascending = true;
        this.metaRow=metaRow;
        initSequence();
    }

    public SortInfo(final MetaRow metaRow, final int dataColumnIndex, final int sortOrder,
                    final boolean ascending) {
        this(metaRow,dataColumnIndex,sortOrder);
        this.ascending = ascending;
    }

    public static SortInfo[] New(final MetaRow metaRow, final int[] dataColumns,
                                 final boolean[] ascending) {
        final SortInfo[] si = new SortInfo[dataColumns.length];
        for (int i = 0; i < si.length; i++) {
            si[i] = new SortInfo(metaRow, dataColumns[i], i + 1, ascending[i]);
        }
        return si;
    }

    public static SortInfo[] New(final MetaRow metaRow, final int[] dataColumnIndexes) {
        final boolean[] ascending = new boolean[dataColumnIndexes.length];
        for (int i = 0; i < dataColumnIndexes.length; i++) {
            ascending[i] = true;
        }
        return New(metaRow, dataColumnIndexes, ascending);
    }

    public static int[] convert(final SortInfo[] sortInfo) {
        final int[] sortColumns = new int[sortInfo.length];
        for (int i = 0; i < sortInfo.length; i++) {
            sortColumns[i] = sortInfo[i].dataColumnIndex;
        }
        return sortColumns;
    }

    public static List sort(final PersonalizableTableModel rootModel, final Collection c, final SortInfo[] sortInfo) {
        return sort(rootModel, new ArrayList(c), sortInfo);
    }

    public static List sort(final List list, final SortInfo[] sortInfo) {
        Collections.sort(list, new ColumnComparator(sortInfo));
        return list;
    }

    public int getDataColumnIndex() {
        return dataColumnIndex;
    }
    
    public boolean getAscending(){
    	return ascending;
    }
    
    public int getSortOrder(){
    	return sortOrder;
    }
}

