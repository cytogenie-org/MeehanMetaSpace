
package com.MeehanMetaSpace.swing;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.MeehanMetaSpace.Basics;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.MeehanMetaSpace.Condition;
import com.MeehanMetaSpace.Pel;
import com.MeehanMetaSpace.swing.DefaultFilterable.Filter;
import com.MeehanMetaSpace.swing.MultiQueryFilter.QuerySet;


public class RotateTable extends JTable{

	final boolean alterEditorTable, alterEditorIndexes, alterRendererTable, alterRendererIndexes;
	public interface SpecialCases{
		public boolean alterRendererTable();
		public boolean alterRendererIndexes();
		public boolean alterEditorTable();
		public boolean alterEditorIndexes();
	}

	private final JTable sourceTable;
	private final TableModel sourceModel;
	private final int []sourceVisualColumnIndexes, sourceVisualRowIndexes;
	private final Boolean []overrideCellEditable;
	private final Model model;
	private final Collection autoCompleteEditors=new ArrayList();

    void dispose(){
	  for (final Iterator it=autoCompleteEditors.iterator();it.hasNext();){
		final AutoComplete.CellEditor ace=(AutoComplete.CellEditor)it.next();
		ace.autoComplete.specialFocusTable=null;
	  }
  }

	private int getSourceRowIndexFromModelIndex(final int modelIndex){
		return sourceVisualRowIndexes[ modelIndex - 1];
	}

	private int getSourceRowIndexFromVisualIndex(final int visualIndex){
		// rotater column is source row
		return getSourceRowIndexFromModelIndex(SwingBasics.getModelIndexFromVisualIndex(this, visualIndex));
	}


	private int getSourceModelIndex(final int rowIndex){
		// rotater row is source column
		return sourceTable.getColumnModel().
			 getColumn( sourceVisualColumnIndexes[ rowIndex ] ).
			 getModelIndex();
	}

	private int getSourceVisualIndex(final int rowIndex){
		return sourceVisualColumnIndexes[rowIndex];
	}

	private class Model
		 extends DefaultTableModel{


		private Model(){
		 /*
		  IN the rotating table each row represents a column in the source (rotated)
		  table.  The columns of the rotating table are the rows of the source table.
		  But the first column of the rotated table is the column names of the
		  source table, each subsequent column is a row from the source table */
			super(sourceVisualColumnIndexes.length, columnCount);
		}

		public Class getColumnClass(final int modelIndex){
			final Class returnValue;
			if (modelIndex==0){
				returnValue=String.class;
			} else if (modelIndex==checkedModelIndex){
				returnValue=Boolean.class;
			} else{
				returnValue=sourceModel.getColumnClass(modelIndex);
			}
			return returnValue;
		}

		public String getColumnName(final int modelIndex){
			return headers[modelIndex];
		}

		public Object getValueAt(final int rowIndex, final int modelIndex){
			final int sourceModelIndex=getSourceModelIndex(rowIndex);
			if (modelIndex==0){ // the rotater table is looking at the source column names
				if ( ! (sourceModel instanceof PersonalizableTableModel)){
					return sourceModel.getColumnName(sourceModelIndex);
				} else {
					final PersonalizableTableModel m=(PersonalizableTableModel)sourceModel;
					return m.getColumnAbbreviation( m.getDataColumnIndex(sourceModelIndex));
				}
			} else if (modelIndex==checkedModelIndex){
				return checked[rowIndex];
			} else{
				final int
					 sourceRowIndex=getSourceRowIndexFromModelIndex(modelIndex);
				final Object o=sourceModel.getValueAt(sourceRowIndex, sourceModelIndex);
				return o;
			}
		}

		public boolean isCellEditable(final int rowIndex, final int modelIndex){
			if (modelIndex==0){ // the rotater table is looking at the source column names
				return false;
			} else if (modelIndex==checkedModelIndex){
				return true;
			} else{
				final int sourceModelIndex=getSourceModelIndex(rowIndex),
					 sourceRowIndex=getSourceRowIndexFromModelIndex(modelIndex);

				return overrideCellEditable==null || overrideCellEditable[modelIndex] == null ?
					 sourceModel.isCellEditable(sourceRowIndex, sourceModelIndex) :
					 overrideCellEditable[modelIndex].booleanValue();
			}
		}

		public void setValueAt(final Object value, final int rowIndex,
				final int modelIndex) {
			if (modelIndex > 0) { // the rotater table is NOT looking at the
				// source column names
				if (modelIndex == checkedModelIndex) {
					checked[rowIndex] = (Boolean) value;
				} else {
					final int sourceModelIndex = getSourceModelIndex(rowIndex), sourceRowIndex = getSourceRowIndexFromModelIndex(modelIndex);

					if (isUsedForFindSeeOnly()) {
						final Object opValue;
						if (modelIndex == 2) {
							opValue = value;
						} else {
							opValue = getValueAt(rowIndex, 2);
						}
						String op = "";
						if (modelIndex == 1) {
							op = (String) value;
						} else {
							op = (String) getValueAt(rowIndex, 1);
							if (Basics.isEmpty(op) && !Basics.isEmpty(opValue)) {
								op = DefaultFilterable
										.getDefaultFilterOpValue(opValue
												.getClass());
								sourceModel.setValueAt(op,
										getSourceRowIndexFromModelIndex(1),
										sourceModelIndex);
								repaint();
							}
						}
						final PersonalizableTableModel m = (PersonalizableTableModel) sourceModel;

						final int dataColumnIndex = m
								.getDataColumnIndex(sourceModelIndex);
						final Filter filter = new Filter(dataColumnIndex, m
								.translateOp(op, opValue == null ? null
										: opValue.getClass()), opValue);
						final QuerySet set = m.multiQuerySet
								.getCurrentQuerySet();
						if (!Basics.isEmpty(op) && !Basics.isEmpty(opValue)) {
							set.updateFilter(dataColumnIndex, filter);
						} else {
							set.removeFilter(dataColumnIndex);
						}
						m.multiQuerySet.updateCurrentQuerySet(set);
						m.updateMultiQueryText();
					}
					sourceModel.setValueAt(value, sourceRowIndex,
							sourceModelIndex);

				}
			}
		}

	}

	public RotateTable(
				final JTable table,
				final int [] visualRowIndexes,
				final String []headers){
			this(
					 table,
					 SwingBasics.getVisualIndexes(table),
					 visualRowIndexes,
					 headers,
					 null
			);
	}

	public RotateTable( final JTable table, final int [] visualRowIndexes){
			this(
					 table,
					 SwingBasics.getVisualIndexes(table),
					 visualRowIndexes,
					 getDefaultHeaders(visualRowIndexes.length),
					 null);
	}

	private static String [] getDefaultHeaders(int rows){
		String []headers=new String[rows+1];
		headers[0]="Column name";
		for (int i=1;i<(rows + 1);i++){
			headers[i]="Row " + i;
		}
		return headers;
	}
	public RotateTable(
				final JTable table,
				final int [] visualRowIndexes,
				final String []headers,
				final Boolean []overrideCellEditable){
			this(
					 table,
					 SwingBasics.getVisualIndexes(table),
					 visualRowIndexes,
					 headers,
					 overrideCellEditable,
					 null
			);
	}

	final String []headers;

	public RotateTable(
			final JTable table,
			final int [] visualColumnIndexes,
			final int [] visualRowIndexes,
			final String [] headers,
			final String checkBoxHeader){
		this(table, visualColumnIndexes, visualRowIndexes, headers, null, checkBoxHeader);
	}

	final String checkBoxHeader;
	final Boolean []checked;
	final int checkedModelIndex;
	final int columnCount;
	final static int MIN_WIDTH_FOR_BUTTONS=195;

	public boolean getChecked(int vi){
		return checked[vi] == null ? false : checked[vi].booleanValue();
	}
	public RotateTable(
			final JTable table,
			final int [] visualColumnIndexes,
			final int [] visualRowIndexes,
			final String [] headers,
			final Boolean [] overrideCellEditable,
			final String checkBoxHeader){
		  this(table, visualColumnIndexes, visualRowIndexes,headers,overrideCellEditable, checkBoxHeader, null);
		}

	public RotateTable(final JTable table, final int[] visualColumnIndexes,
			final int[] visualRowIndexes, final String[] headers,
			final Boolean[] overrideCellEditable, final String checkBoxHeader,
			final int[] colWidth) {
		setRowHeight(table.getRowHeight());
		this.sourceTable = table;
		this.sourceVisualColumnIndexes = visualColumnIndexes;
		this.sourceVisualRowIndexes = visualRowIndexes;
		this.overrideCellEditable = overrideCellEditable;
		this.checkBoxHeader = checkBoxHeader;
		checked = new Boolean[checkBoxHeader == null ? 0
				: visualColumnIndexes.length];
		this.checkedModelIndex = checkBoxHeader == null ? -1
				: visualRowIndexes.length + 1;
		this.columnCount = sourceVisualRowIndexes.length
				+ (checkBoxHeader == null ? 1 : 2);
		this.headers = new String[columnCount];
		int j = 0;
		if (headers.length == visualRowIndexes.length) {
			this.headers[0] = "Column name";
			j = 1;
		}
		for (int i = 0; i < headers.length; i++, j++) {
			this.headers[j] = headers[i];
		}
		if (checkBoxHeader != null) {
			this.headers[columnCount - 1] = checkBoxHeader;
		}
		this.sourceModel = sourceTable.getModel();
		this.model = new Model();

		// no table will show without a model
		super.setModel(model);
		final TableCellEditor tce = table.getCellEditor();
		if (tce != null) {
			tce.stopCellEditing();
		}
		if (sourceTable instanceof SpecialCases) {
			alterEditorIndexes = ((SpecialCases) sourceTable)
					.alterEditorIndexes();
			alterEditorTable = ((SpecialCases) sourceTable).alterEditorTable();
			alterRendererIndexes = ((SpecialCases) sourceTable)
					.alterRendererIndexes();
			alterRendererTable = ((SpecialCases) sourceTable)
					.alterRendererTable();
		} else {
			alterRendererTable = alterRendererIndexes = alterEditorIndexes = alterEditorTable = false;
		}

		// Create a column model for the main table. This model ignores the
		// first
		// column added, and sets a minimum width of 150 pixels for all others.
		TableColumnModel tcm = new DefaultTableColumnModel() {
			boolean first = true;

			public void addColumn(TableColumn tc) {
				// Drop the first column . . . that'll be the row header
				if (first) {
					first = false;
					return;
				}
				// tc.setMinWidth(150);
				super.addColumn(tc);
			}
		};
		setColumnModel(tcm);
		createDefaultColumnsFromModel();

		// Create a column model that will serve as our row header table. This
		// model picks a maximum width and only stores the first column.
		TableColumnModel rowHeaderModel = new DefaultTableColumnModel() {
			boolean first = true;

			public void addColumn(TableColumn tc) {
				if (first) {
					// tc.setMaxWidth(35);
					super.addColumn(tc);
					first = false;
				}
				// Drop the rest of the columns . . . this is the header column
				// only
			}
		};
		// Set up the header column and get it hooked up to everything
		rowHeaderTable = new JTable(model, rowHeaderModel) {
			public int getRowHeight() {
				return RotateTable.this.getRowHeight();
			}

			public int getRowMargin() {
				return RotateTable.this.getRowMargin();
			}
		};
		rowHeaderTable.createDefaultColumnsFromModel();

		max = getMaxPreferredWidth();
		// Make the header column look pretty
		rowHeaderTable.setMaximumSize(new Dimension(colWidth != null
				&& colWidth.length > 0 ? colWidth[0] : 50, 10000));
		rowHeaderTable.setBackground(sourceTable.getTableHeader().getBackground()	);

		for (int i = 0; i < columnCount; i++) {
			final int w;
			final TableColumn tc = i == 0 ? rowHeaderModel.getColumn(0) : tcm
					.getColumn(i - 1);
			if (colWidth == null || i >= colWidth.length) {
				if (i > 0) {
					w = max;
				} else {
					w = 50;
				}
			} else {
				if (colWidth[i] >= 0) {
					w = colWidth[i];
				} else {
					if (i > 0) {
						w = max;
					} else {
						w = 50;
					}
				}
			}
			
			tc.setPreferredWidth(w);
		}
		this.setSurrendersFocusOnKeystroke(true);
	}


	final int max;

	int getMaxPreferredWidth(){
	  final TableColumnModel sourceTcm=sourceTable.getColumnModel();
	  int max=0;
		for (int i=0;i<sourceVisualColumnIndexes.length;i++){
			final int modelIndex=getSourceModelIndex(i);
			final TableColumn tc=sourceTcm.getColumn(modelIndex);
			final int n=tc.getPreferredWidth();
			if (n>max){
				max=n;
			}
		}
		return max;

	}

	

	private class HeaderRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				  final JTable table,
				  final Object value,
				  final boolean isSelected,
				  final boolean hasFocus,
				  final int row,
				  final int column)
		{
			final Component c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setForeground(headerForeground);
			c.setBackground(headerBackground);
			return c;
		}
	}

	private final HeaderRenderer hr=new HeaderRenderer();
	private final Color
		 headerBackground=UIManager.getColor("TableHeader.background"),
		 headerForeground=UIManager.getColor("TableHeader.foreground");


	public TableCellRenderer getCellRenderer(
		 final int rowIndex,
		 final int visualIndex){
		int modelIndex=SwingBasics.getModelIndexFromVisualIndex(this, visualIndex);
		if (modelIndex==0){ // the rotater table is looking at the source column names
			return hr;
		}
		if (modelIndex==checkedModelIndex){
			return super.getCellRenderer(rowIndex, visualIndex);
		}
		final int sourceRowIndex=getSourceRowIndexFromVisualIndex(visualIndex),
			 sourceVisualIndex=getSourceVisualIndex(rowIndex);
		final TableCellRenderer sourceTcr=sourceTable.getCellRenderer(
				  sourceRowIndex, sourceVisualIndex);
		final TableCellRenderer returnValue;
		if (alterRendererIndexes || alterRendererTable){
				TableCellRenderer tcr= (TableCellRenderer) renderers.get(sourceTcr);
				if (tcr==null){
					tcr=new Renderer(sourceTcr);
					renderers.put(sourceTcr, tcr);
				}
				returnValue=tcr;
		} else {
			returnValue=sourceTcr;
		}
		return returnValue;
	}

	public TableCellEditor getCellEditor(
		 final int rowIndex,
		 final int visualIndex){
		int modelIndex=SwingBasics.getModelIndexFromVisualIndex(this, visualIndex);
		if (modelIndex==checkedModelIndex || modelIndex==0){
// the rotater table is looking at the source column names
			Pel.log.print(Condition.WARNING, "Not clear on why we are editing the column names in a rotated table?");
			return super.getCellEditor(rowIndex, visualIndex);
		}
		if (checked.length>rowIndex && checked[rowIndex]==null){
			checked[rowIndex]=Boolean.TRUE;

		}
		final int sourceRowIndex=getSourceRowIndexFromVisualIndex(visualIndex),
			 sourceVisualIndex=getSourceVisualIndex(rowIndex);
		final TableCellEditor sourceTce=sourceTable.getCellEditor(
			 sourceRowIndex, sourceVisualIndex);
		final TableCellEditor returnValue;
		if (sourceTce instanceof AutoComplete.CellEditor){
		  final AutoComplete.CellEditor ace = (AutoComplete.CellEditor) sourceTce;
		  ace.autoComplete.specialFocusTable = this;
		  if (!autoCompleteEditors.contains(ace)) {
			autoCompleteEditors.add(ace);
		  }
		}
		if (alterEditorIndexes || alterEditorTable){

				TableCellEditor tce= (TableCellEditor) editors.get(sourceTce);
				if (tce==null){
					tce=new Editor(sourceTce);
					editors.put(sourceTce, tce);
				}
				returnValue=tce;
		} else {
			returnValue=sourceTce;
		}


		return returnValue;
	}

	public void supressEscape(){
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(KeyStroke.getKeyStroke("ESCAPE"));
	}

	final HashMap editors=new HashMap(), renderers=new HashMap();

	private class Renderer
		 implements TableCellRenderer{
		private final TableCellRenderer source;

		private Renderer(final TableCellRenderer source){
			this.source=source;
		}

		public Component getTableCellRendererComponent(
			 final JTable table,
			 final Object value,
			 final boolean isSelected,
			 final boolean hasFocus,
			 final int p_row,
			 final int p_visualColumn){
			final int row, visualColumn;
			if (alterRendererIndexes && !(source instanceof NestedTableCellEditor.Factory)){
				row=getSourceRowIndexFromVisualIndex(p_visualColumn);
				visualColumn=getSourceVisualIndex(p_row);
			} else{
				row=p_row;
				visualColumn=p_visualColumn;
			}

			return source.getTableCellRendererComponent(
				 alterRendererTable && !(source instanceof NestedTableCellEditor.Factory) ? sourceTable : table,
				 value,
				 isSelected,
				 hasFocus,
				 row,
				 visualColumn);
		}
	}

	class Editor
		 implements TableCellEditor{
		final TableCellEditor source;

		private Editor(final TableCellEditor source){
			this.source=source;
		}

		public Component getTableCellEditorComponent(
			 final JTable table,
			 final Object value,
			 final boolean isSelected,
			 final int rowIndex,
			 final int visualIndex){
			final int theRowIndex, theVisualIndex;
			if (alterEditorIndexes){
				theRowIndex=getSourceRowIndexFromVisualIndex(visualIndex);
				theVisualIndex=getSourceVisualIndex(rowIndex);
			}else{
				theRowIndex=rowIndex;
				theVisualIndex=visualIndex;
			}
			return source.getTableCellEditorComponent(
				 alterEditorTable ? sourceTable:table,
				 value,
				 isSelected,
				 theRowIndex,
				 theVisualIndex);
		}

		public Object getCellEditorValue(){
		  sourceTable.repaint();
			final Object o=source.getCellEditorValue();
			SwingUtilities.invokeLater( new Runnable(){
			  public void run(){
				RotateTable.this.invalidate();
				RotateTable.this.requestFocus();
		}
	  });
			return o;
		}

		public boolean isCellEditable(EventObject anEvent){
			return source.isCellEditable(anEvent);
		}

		public boolean shouldSelectCell(EventObject anEvent){			
			return source.shouldSelectCell(anEvent);
		}

		public boolean stopCellEditing(){
			final boolean b=source.stopCellEditing();
			RotateTable.this.requestFocus();
			return b;
		}

		public void cancelCellEditing(){
			source.cancelCellEditing();
			RotateTable.this.requestFocus();

		}

		public void addCellEditorListener(CellEditorListener l){
			source.addCellEditorListener(l);
		}

		public void removeCellEditorListener(CellEditorListener l){
			source.removeCellEditorListener(l);
		}
	}

	private boolean isUsedForFindSeeOnly() {
		if (sourceModel instanceof PersonalizableTableModel) {
			if (((PersonalizableTableModel) sourceModel).showFilterUI) {
				if (sourceVisualRowIndexes.length == 2) {
					if (sourceVisualRowIndexes[0] == PersonalizableTable.ROW_IDX_FILTER_OP) {
						if (sourceVisualRowIndexes[1] == PersonalizableTable.ROW_IDX_FILTER_VALUE) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}
	
	private final JTable rowHeaderTable;
	
	final JScrollPane getScrollPane(){
	    // Put it in a viewport that we can control a bit
	    final JViewport jv = new JViewport();
	    jv.setView(rowHeaderTable);
	    jv.setPreferredSize(rowHeaderTable.getMaximumSize());

	    // Without shutting off autoResizeMode, our tables won't scroll
	    // correctly (horizontally, anyway)
	    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    rowHeaderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	    // We have to manually attach the row headers, but after that, the scroll
	    // pane keeps them in sync
	    final JScrollPane jsp = new JScrollPane(this);
	    final Dimension d1=rowHeaderTable.getMaximumSize(), d2=getPreferredSize();
		jsp.setPreferredSize(new Dimension( d1.width+d2.width+40, d2.height+60));
		
	    jsp.setRowHeader(jv);
		final JTableHeader h=rowHeaderTable.getTableHeader();
		jsp.setCorner(JScrollPane.UPPER_LEFT_CORNER, h);
	    return jsp;
	}
	
	void clear(final Map<Integer,Filter> filters){
		for (int i=0;i<getRowCount();i++){
			int sourceModelIndex=getSourceModelIndex(i);
			final PersonalizableTableModel m = (PersonalizableTableModel) sourceModel;

			final int dataColumnIndex = m
					.getDataColumnIndex(sourceModelIndex);
			boolean found=false;
			for (int key:filters.keySet()){
				if (key==dataColumnIndex){
					final Filter filter=filters.get(key);
					setValueAt(filter.value, i, 1);
					setValueAt((String) Filterable.allFilterOp[filter.op], i,0);
					found=true;
					break;
				}
			}
			if (!found){
				setValueAt(null,i,1);
				setValueAt(Filterable.opNone, i, 0);
			}
		}
	}
	
    public void stopCellEditing() {
        final TableCellEditor tce = getCellEditor();
        if (tce != null) {
            tce.stopCellEditing();
        }
    }

}

