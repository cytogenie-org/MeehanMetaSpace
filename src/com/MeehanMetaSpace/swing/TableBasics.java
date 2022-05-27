package com.MeehanMetaSpace.swing;

import java.util.*;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.MeehanMetaSpace.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public final class TableBasics {
	public static void fireListSelection(final JTable fromTable, 
			final ListSelectionListener []lsl, final ListSelectionEvent e, final JTable toTable){
		toTable.clearSelection();
		final int[]rs=fromTable.getSelectedRows();
		for (int i=0;i<rs.length;i++){
			toTable.addRowSelectionInterval(rs[i], rs[i]);
		}
		if (lsl==null){
			return;
		}
		final ListSelectionEvent ne=new ListSelectionEvent(toTable, e.getFirstIndex(), 
				e.getLastIndex(), false);
		for (int i=0;i<lsl.length;i++){
			lsl[i].valueChanged(ne);
		}
	}

  public static boolean hasBadAdvice(final PersonalizableTableModel model) {
	final CellAdvice cellAdvice = new CellAdvice();
	for (int visualRowIndex = 0; visualRowIndex < model.getRowCount(); visualRowIndex++) {
	  final Row row = model.getRowAtVisualIndex(visualRowIndex);
	  for (int columnIndex = 0; columnIndex < row.getColumnCount(); columnIndex++) {
		row.setAdvice(columnIndex, cellAdvice);
		if (cellAdvice.type == CellAdvice.TYPE_ERROR) {
		  return true;
		}
	  }
	}
	return false;
  }

  public static boolean hasBadAdvice(
	  final PersonalizableTableModel model,
	  final int dataColumnIndex) {
	  final CellAdvice cellAdvice = new CellAdvice();
	  for (int visualRowIndex = 0; visualRowIndex < model.getRowCount(); visualRowIndex++) {
		final Row row = model.getRowAtVisualIndex(visualRowIndex);
		row.setAdvice(dataColumnIndex, cellAdvice);
		if (cellAdvice.type == CellAdvice.TYPE_ERROR) {
			return true;
		}
	  }
	  return false;
	}

  public static String toString(
	  final StringConverter sc,
	  final Collection p_c,
	  final String newDelimiter,
	  final boolean sort,
	  final SortValueReinterpreter svr) {
	// OPTIMIZE the 80% use case
	if (p_c.size() == 1) {
	  return sc.toString(p_c.iterator().next());
	}

	final Collection c;
	if (!sort) {
	  c = new ArrayList();
	  for (final Iterator it = p_c.iterator(); it.hasNext(); ) {
		c.add(sc.toString(it.next()));
	  }
	}
	else if (svr == null) {
	  ArrayList sorted = new ArrayList();
	  for (final Iterator it = p_c.iterator(); it.hasNext(); ) {
		final Object value = sc.toString(it.next());
		sorted.add(svr == null ? value : svr.reinterpret(value));
	  }
	  Collections.sort(sorted);
	  c = sorted;
	}
	else {
	  final TreeMap sorted = new TreeMap();
	  for (final Iterator it = p_c.iterator(); it.hasNext(); ) {
		final Object value = sc.toString(it.next());
		sorted.put(svr.reinterpret(value), value);
	  }
	  c = new ArrayList();
	  for (final Iterator it = sorted.keySet().iterator(); it.hasNext(); ) {
		c.add(sorted.get(it.next()));
	  }
	}
	final StringBuilder sb = new StringBuilder(c.size() * 5);
	int i = 0;
	for (final Iterator it = c.iterator(); it.hasNext(); i++) {
	  if (i > 0) {
		sb.append(newDelimiter);
	  }
	  sb.append(it.next());
	}
	return sb.toString();
  }

  public static StringConverter getBestStringConverter(final MetaRow metaRow,
	  final int dataColumnIndex) {
	StringConverter sc = metaRow.getStringConverter(dataColumnIndex);
	return sc == null ?
		DefaultStringConverters.get(metaRow.getClass(dataColumnIndex)) : sc;
  }

  public static String toString(final Row row) {
	final StringBuilder sb = new StringBuilder();
	final int n = row.getColumnCount();

	for (int i = 0; i < n; i++) {
	  if (i > 0) {
		sb.append(", ");
	  }
	  sb.append(row.get(i));
	}
	return sb.toString();
  }

  public static String toString(
	  final Row row,
	  final int[] dataColumns) {
	final StringBuilder sb = new StringBuilder();
	final int n = row.getColumnCount();

	for (int i = 0; i < dataColumns.length; i++) {
	  if (i > 0) {
		sb.append(", ");
	  }
	  sb.append(row.get(dataColumns[i]));
	}
	return sb.toString();
  }

  public static class Sum {
	Sum(final int cnt, int[] columnsToSummarize, Object[] values,
		int[] childCnt) {
	  this.cnt = new int[childCnt.length];
	  this.cnt[this.cnt.length - 1] = cnt;
	  for (int i = 0; i < this.cnt.length - 1; i++) {
		this.cnt[i] = childCnt[i];
	  }
	  this.columnsToSummarize = columnsToSummarize;
	  this.values = values;
	}

	public final int[] columnsToSummarize;
	public final Object[] values;
	public final int[] cnt;
  }

  private static Map getMapOfSums(
	  final GroupedDataSource cds,
	  final int[] sortColumns,
	  final int[] columnsToSummarize,
	  boolean keyIsRow) {
	assert sortColumns != null:"cols must not be NULL";
	assert sortColumns.length > 0:"cols.length must be greater than 0";

	final HashMap map = new HashMap();

	// With next 3 array definitions, add one to account for root
	int levelCnt = sortColumns.length + 1;
	GroupedDataSource.Node[] nodes = new GroupedDataSource.Node[levelCnt];
	int[] childCnts = new int[levelCnt];
	int[] childPositions = new int[levelCnt];

	boolean hasAllLevels = true;
// set up root
	nodes[0] = cds.getRoot();
	childCnts[0] = nodes[0].getChildCount();
	childPositions[0] = 0;
	for (int level = 1; level < levelCnt && hasAllLevels; level++) {
	  childPositions[level] = 0;
	  if (childCnts[level - 1] > 0) {
		nodes[level] = (GroupedDataSource.Node) nodes[level - 1].getChildAt(0);
		childCnts[level] = nodes[level].getChildCount();
	  }
	  else {
		hasAllLevels = false;
	  }
	}

	assert childCnts[levelCnt - 1] == 0 && nodes[levelCnt - 1].isLeaf():
		"not a leaf?";
	if (hasAllLevels) {
	  for (int level = levelCnt - 1; ; ) {
		if (level == levelCnt - 1) {
		  GroupedDataSource.Node.UngroupedChildRowIterator it =
			  nodes[level].ungroupedChildRowIterator();

		  final Sum sum = new Sum(it.size(), columnsToSummarize,
								  new Object[columnsToSummarize.length],
								  childCnts);
		  for (int column = 0; column < columnsToSummarize.length; column++) {
			it.reset(); ;
			sum.values[column] = new Float(
				sum(it, columnsToSummarize[column]));
		  }
		  final Object key;
		  final Row row = nodes[level].getFirstUngroupedRow();
		  if (!keyIsRow) {
			key = encode(row, sortColumns);
		  }
		  else {
			key = row;
		  }
//					int h=key.hashCode();
		  map.put(key, sum);
		  // cause pior level to iterate to next node for this level (nodes[level])
		  level--;
		}
		else {
		  childPositions[level]++;
		  if (childPositions[level] < childCnts[level]) {
			GroupedDataSource.Node newChild =
				(GroupedDataSource.Node) nodes[level].getChildAt(childPositions[
				level]);
			level++;
			nodes[level] = newChild;
			childCnts[level] = newChild.getChildCount();
			childPositions[level] = -1; // cause next level to setup next level
		  }
		  else {
			if (level == 0) {
			  break;
			}
			level--; // cause pior level to iterate nodes[level]
		  }
		}
	  }
	}
	return map;

  }

  public static Map getMapOfSums(
	  final PersonalizableDataSource dataSource,
	  final int[] sortColumns,
	  boolean keyIsRow) {
	return getMapOfSums(dataSource, sortColumns, null, keyIsRow);
  }

  public static Map getMapOfSums(
	  final PersonalizableDataSource dataSource,
	  final int[] sortColumns,
	  final int[] columnsToSummarize,
	  boolean keyIsRow) {
	assert dataSource != null:"dataSource must not be NULL";
	return getMapOfSums(getTree(dataSource, sortColumns), sortColumns,
						columnsToSummarize == null ? new int[0] :
						columnsToSummarize,
						keyIsRow);
  }

  public static Map getMapOfSums(
	  final PersonalizableDataSource dataSource,
	  final SortInfo[] sortInfo,
	  final int[] columnsToSummarize,
	  boolean keyIsRow) {
	assert dataSource != null:"dataSource must not be NULL";
	final int[] sortColumns = SortInfo.convert(sortInfo);
	return getMapOfSums(getTree(dataSource, sortInfo), sortColumns,
						columnsToSummarize, keyIsRow);
  }

  public static Map getMapOfSums(
	  final PersonalizableTableModel tableModel,
	  final int[] columnsToSummarize,
	  boolean keyIsRow) {
	final SortInfo[] sortInfo = tableModel.getAllSortInfo();
	if (Basics.isEmpty(sortInfo)) {
	  return null;
	}
	else {
	  PersonalizableDataSource ds = tableModel.getDataSource();
	  if (ds instanceof DefaultPersonalizableDataSource) {
		try {
		  PersonalizableDataSource pds = (PersonalizableDataSource) ( (
			  DefaultPersonalizableDataSource) ds).clone();
		  return getMapOfSums(pds, sortInfo, columnsToSummarize, keyIsRow);
		}
		catch (Exception e) {
		  Pel.log.warn(e);
		}
	  }
	  return null;
	}
  }

  public static String encode(final Row row, final int[] cols) {
	final StringBuilder sb = new StringBuilder();
	for (int i = 0; i < cols.length; i++) {
	  if (i > 0) {
		sb.append('.');
	  }
	  sb.append(row.get(cols[i]));
	}
	return sb.toString();
  }

  public static String encode(final Row row, final int[] cols,
							  final int colLength) {
	final StringBuilder sb = new StringBuilder();
	for (int i = 0; i < colLength; i++) {
	  sb.append(row.get(cols[i]));
	  sb.append('.');
	}
	return sb.toString();
  }

  public static Object[] toArray(
	  final Row row,
	  final int[] cols) {
	Object[] array = new Object[cols.length];
	for (int i = 0; i < cols.length; i++) {
	  array[i] = row.get(cols[i]);
	}
	return array;
  }

  public static Row copy(
		  final int[] dataColumnIndexes,
		  final int[] modelColumnIndexes,
	  final PersonalizableTableModel model,
	  final int visualRowIndex,
	  final Row from) {
	  final Row to=model.getRowAtVisualIndex(visualRowIndex);
      if (to != null){
          for (int i = 0; i < modelColumnIndexes.length; i++) {
              if (to.isEditable(dataColumnIndexes[i])) {
                  final Object columnValue = from.get(dataColumnIndexes[i]);
                  model.setValueAt(columnValue, visualRowIndex, modelColumnIndexes[i]);
              }
          }
      }
	return to;
  }

  public static GroupedDataSource getTree(
	  final PersonalizableDataSource ds,
	  final int[] cols) {
	final PersonalizableTableModel tableModel =
		PersonalizableTableModel.activate(ds, false);
	for (int i = 0; i < cols.length; i++) {
	  tableModel.sort(cols[i], true);
	}
	tableModel.sort();
	return GroupedDataSource.activate(
		tableModel,
		PersonalizableTableModel.GROUP_BY_TREE,
		false);
  }

  public static GroupedDataSource getTree(
	  final PersonalizableDataSource ds,
	  final SortInfo[] sortInfo) {
	final PersonalizableTableModel tableModel =
		PersonalizableTableModel.activate(ds, false);
	for (int i = 0; i < sortInfo.length; i++) {
	  tableModel.sort(sortInfo[i].dataColumnIndex, sortInfo[i].ascending);
	}
	tableModel.sort();
	return GroupedDataSource.activate(
		tableModel,
		PersonalizableTableModel.GROUP_BY_TREE,
		false);
  }

  public static float sum(final Iterator it, final int columnIdx) {
	float sum = 0;
	while (it.hasNext()) {
	  final Row row = (Row) it.next();
	  final Object o = row.get(columnIdx);
	  if (o != null) {
		float next;
		if (o instanceof Integer) {
		  next = ( (Integer) o).floatValue();
		}
		else {
		  next = ( (Float) o).floatValue();
		}
		sum += next;
	  }
	}
	return sum;
  }

  public static boolean hasZero(
	  final Iterator it,
	  final int[] inspectColumns
	  ) {
	float sum = 0;
	while (it.hasNext()) {
	  final Row row = (Row) it.next();
	  for (int i = 0; i < inspectColumns.length; i++) {
		final Object o = row.get(inspectColumns[i]);
		if (o != null && o instanceof Number) {
		  double next = ( (Number) o).doubleValue();
		  if (next == 0.00) {
			return true;
		  }
		}
	  }
	}
	return false;
  }

  public static boolean alterZeroOrNullValues(
	  final Iterator it,
	  final int[] inspectColumns,
	  final Number number
	  ) {
	boolean retVal = false;
	float sum = 0;
	while (it.hasNext()) {
	  final Row row = (Row) it.next();
	  for (int i = 0; i < inspectColumns.length; i++) {
		final Object o = row.get(inspectColumns[i]);
		if (o instanceof Number) {
		  double next = ( (Number) o).doubleValue();
		  if (next == 0.00) {
			row.set(inspectColumns[i], number);
			retVal = true;
		  }
		} else if (o==null){
            row.set(inspectColumns[i], number);
			retVal = true;
        }
	  }
	}
	return retVal;
  }

  public static String debug(final List l, final SortInfo[] s) {
	final StringBuilder all = new StringBuilder();
    int idx=0;
	for (final Iterator it = l.iterator(); it.hasNext(); ) {
	  final StringBuilder sb = new StringBuilder();
	  final Row r = (Row) it.next();
      sb.append(++idx);
      sb.append(".  ");
	  sb.append("{");
	  for (int i = 0; i < s.length; i++) {
		sb.append(r.get(s[i].dataColumnIndex));
		sb.append(",");
	  }
	  sb.append("} ");
	  System.out.println(sb.toString());
	  all.append(sb.toString());

	}
	return all.toString();
  }

  public static String toHtml(final String title, final Row row,
							  final int[] dataColumns) {
	final StringBuilder sb = new StringBuilder(Basics.startHtml(title));
	final MetaRow mr = row.getMetaRow();
	if (mr != null){
	  sb.append("<table border='1'>"); //<tr><td><b><i>Name</i></b></td><td><b><i>Value</i></b></td></tr>");
	  for (int i=0; i < dataColumns.length; i++){
		sb.append("<tr><td><i>");
		sb.append(mr.getLabel(dataColumns[i]));
		sb.append("</i></td><td>");
		sb.append(row.get(dataColumns[i]));
		sb.append("</td></tr>");

	  }
	  sb.append("</table>");
	}
	sb.append(Basics.endHtml());
	return sb.toString();
  }

  public static Collection getAllowedValues(final Row row, final int dataColumnIndex) {
	Collection c = row==null ? Collections.EMPTY_LIST: row.getAllowedValues(dataColumnIndex);
	if (!Basics.isEmpty(c) && row.shouldAllowedValuesBeSorted(dataColumnIndex)){
	  final MetaRow mr=row.getMetaRow();
	  if (mr != null){
		final SortValueReinterpreter svr=mr.getSortValueReinterpreter(
			dataColumnIndex);
		if (svr != null){
			final Collection c2=row.getUnselectableValues(dataColumnIndex);
			if (c2.size()==0){
		  final ReInterpretableComparator cvc=new ReInterpretableComparator(svr);
		  if (c instanceof java.util.List){
			Collections.sort((java.util.List) c, cvc);
		  }
		  else{
			final ArrayList al=new ArrayList(c);
			Collections.sort(al, cvc);
			c=al;
		  }
		}
		}
	  }
	}
	return c;
  }

  public static Object getSortableValueInDropDownList(final Row row, final int dataColumnIndex, final Object value) {
		final MetaRow metaRow = row.getMetaRow();
		final SortValueReinterpreter svr = metaRow == null ? null : metaRow.getSortValueReinterpreter(dataColumnIndex);
		if (svr != null && svr.useInDropDownList()) {
				return svr.reinterpret(value);
		} 
		return value;
	}

  public static Object getSortableValue(final Row row,
										final int dataColumnIndex) {
	final Object retVal;
	final MetaRow metaRow = row.getMetaRow();
	final SortValueReinterpreter svr = metaRow == null ? null :
		metaRow.getSortValueReinterpreter(dataColumnIndex);
	if (svr != null) {
	  final Object o = row.get(dataColumnIndex);
	  retVal = svr.reinterpret(o);
	}
	else {
	  retVal = row.get(dataColumnIndex);
	}
	return retVal;
  }

  public static Class reinterpretClass(final MetaRow metaRow,
										final int dataColumnIndex,
										final Class clas) {
	final Class retVal;
	final SortValueReinterpreter svr = metaRow == null ? null :
		metaRow.getSortValueReinterpreter(dataColumnIndex);
	if (svr != null) {
	  retVal = svr.reinterpret(clas);
	}
	else {
	  retVal = clas;
	}
	return retVal;
  }

  public static void print(final java.io.PrintStream out, final Iterator<Row> it, final int []dataColumnIndexes, final String title) {
		out.println();
		out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
		out.println(title);
		while (it.hasNext()) {
			final Row r=it.next();
			for (int i=0;i<dataColumnIndexes.length;i++) {
				if (i>0) {
					out.print(", ");
				}
				out.print(r.get(dataColumnIndexes[i]));
			}
			out.println();
		}
	}

  public static void setListTable(final PersonalizableTableModel tableModel){
		tableModel.rememberUserPreferredColumnWidth=false;
		tableModel.canEditCellForMultipleRows = false;
		tableModel.isTreeOn = false;
		tableModel.isCreateTabsOn = false;
		tableModel.canSortArrangeColumns = false;
		tableModel.setCondensedRatioOfScreen(1.0 / 3.0, 350);
		tableModel.showUrlText = true;
		tableModel.setCanImportExport(false);
		tableModel.setEditInPlace(true);		
		tableModel.setAllowEditInPlaceControl(false);
  }
  
  public static void focusOnFirstCellLater(final PersonalizableTableModel tableModel, final boolean widen){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final PersonalizableTable pt=tableModel.getModelShowing().getTable();
				if (pt==null){
					return;
				}
				if (pt.getRowCount()>0){
					pt.setRowSelectionInterval(0, 0);
					pt.setColumnSelectionInterval(0, 0);
					pt.removeRowSelectionInterval(0, 0);
				}
				pt.setAutoResizeMode(
						widen?
								JTable.AUTO_RESIZE_ALL_COLUMNS:
								JTable.AUTO_RESIZE_OFF);
			}
		});

  }
}
