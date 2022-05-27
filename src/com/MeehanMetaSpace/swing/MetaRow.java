package com.MeehanMetaSpace.swing;
import java.util.List;
import java.util.Iterator;
import javax.swing.Icon;
import com.MeehanMetaSpace.*;
import com.MeehanMetaSpace.swing.DefaultMetaRow.Column;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public interface MetaRow {
	public String getKey();
	public boolean isEditable(int dataColumnIndex);
	public Class getClass(int dataColumnIndex);
	public int indexOf(String dataColumnIdentifier);
	public String getDataColumnIdentifier(int dataColumnIndex);
	public String getLabel(int dataColumnIndex);
	public List<String> cloneDataColumnIdentifiers();
	public boolean containsAllIdentifiers(List<String> identifiers);
	public int size();
	public StringConverter getStringConverter(int dataColumnIndex);
	public String getEditMask(int dataColumnIndex);
	public String getDateFormat(int dataColumnIndex);
	public String getDecimalFormat(int dataColumnIndex);
	public Class getDecimalClass(int dataColumnIndex);
	public SortValueReinterpreter getSortValueReinterpreter(int dataColumnIndex);
	public Icon getIcon(int dataColumnIndex);
	public Icon getIcon(Iterator selectedRows, int dataColumnIndex, boolean isExpanded, boolean isLeaf);
	public Integer getSpecialHorizontalAlignment(int dataColumnIndex);
    boolean hasNonAlphabeticSort(final int dataColumnIndex);

}
