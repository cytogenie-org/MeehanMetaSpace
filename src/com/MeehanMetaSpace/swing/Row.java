package com.MeehanMetaSpace.swing;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Icon;

public interface Row {
	public interface IconGetter{
		Icon get(Iterator selectedRows, int dataColumnIndex);
	}
    public void endImport();

    public MetaRow getMetaRow();

    public void set(int dataColumnIndex, Object columnValue);

    public Object get(int dataColumnIndex);
    public Object getFilterableValue(int dataColumnIndex);

    public Object getRenderOnlyValue(final int dataColumnIndex, boolean isSelected, boolean hasFocus);

    public int getColumnCount();

    public boolean isEditable(int dataColumnIndex);

    public boolean isDeletable();

    public boolean isExplorable(int dataColumnIndex);

    public void explore(int dataColumnIndex);

    public boolean setAdvice(int dataColumnIndex, CellAdvice cellAdvice);

    public Collection getAllowedValues(int dataColumnIndex);
    public Collection getUnselectableValues(int dataColumnIndex);
    public Collection getForbiddenValues(int dataColumnIndex);
    public boolean shouldAllowedValuesBeSorted(int dataColumnIndex);
    public boolean allowNewValue(int dataColumnIndex);

    public Icon getIcon(int dataColumnIndex);
    

    public interface GroupSensitive extends Row {
        public Object get(PersonalizableDataSource ungroupedDataSource,
                          int dataColumnIndex);
    }
    
    public boolean isActive();
    
    public interface SelfExplanatory{
    	boolean getDeletableAnomalies(final Collection<String> c);            
        Collection<String> getEditableAnomalies(int dataColumnIndex);
    }
    
    public String getRowId();
    
    public String getRowType();
}
