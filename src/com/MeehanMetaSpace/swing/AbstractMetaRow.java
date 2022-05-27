package com.MeehanMetaSpace.swing;

import java.util.*;
import javax.swing.Icon;
import com.MeehanMetaSpace.*;

/**
 * Title:        Meehan Meta Space Software
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Stephen Meehan
 * @version 1.0
 */

public abstract class AbstractMetaRow implements MetaRow {
    public String getEditMask(final int dataColumnIndex) {
        return null;
    }
	public Integer getSpecialHorizontalAlignment(final int dataColumnIndex){
		return null;
	}

    public String getKey() {
    	final String n=getClass().getName();
    	return n;
    }

    public String getDecimalFormat(final int dataColumnIndex) {
        return null;
    }

    public Class getDecimalClass(final int dataColumnIndex) {
        return null;
    }

    public String getDateFormat(final int dataColumnIndex) {
        return null;
    }

    public StringConverter getStringConverter(final int dataColumn) {
        return DefaultStringConverters.get(getClass(dataColumn));
    }

    public boolean isEditable(int dataColumnIndex) {
        return true;
    }


    public SortValueReinterpreter getSortValueReinterpreter(final int dataColumnIndex) {
        return null;
    }

    public AbstractMetaRow(final List<String> names) {
        dataColumnIdentifiers = names == null ? new ArrayList() : names;
    }

    protected final List<String> dataColumnIdentifiers;

    public boolean containsAllIdentifiers(final List<String> identifiers) {
        return dataColumnIdentifiers.containsAll(identifiers);
    }

    public List<String> cloneDataColumnIdentifiers() {
        final ArrayList copy = new ArrayList();
        for (String name:dataColumnIdentifiers) {
            copy.add(name);
        }
        return copy;
    }

    public final String getDataColumnIdentifier(final int dataColumnIndex) {
        return dataColumnIdentifiers.get(dataColumnIndex);
    }

    /**
     * Sub classes override this method if the label differs from the name
     *
     * @param dataColumn
     * @return the label which differs from the name
     */
    public String getLabel(final int dataColumnIndex) {
        return dataColumnIdentifiers.get(dataColumnIndex);
    }

    public Icon getIcon(final int dataColumnIndex) {
        return null;
    }

    public Icon getIcon(
      final Iterator selectedRows,
      final int dataColumnIndex,
      final boolean isExpanded,
      final boolean isLeaf) {
        return null;
    }

    public final int size() {
        return dataColumnIdentifiers.size();
    }

    public final int indexOf(final String dataColumnIdentifier) {
        return dataColumnIdentifiers.indexOf(dataColumnIdentifier);
    }
    public boolean hasNonAlphabeticSort(final int dataColumnIndex){
    	return false;
    }
   
}
