package com.MeehanMetaSpace.swing;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import com.MeehanMetaSpace.*;

public final class NestedTableCellEditor
	 extends AbstractCellEditor
	 implements TableCellEditor{

	public boolean stopCellEditing(){
		final TableCellEditor tce=nestedTable.getCellEditor();
		if (tce!=null){
			tce.stopCellEditing();
		}
		return super.stopCellEditing();
	}

	public static int getPreferredWidth(
		 final Font font,
		 final String[] columnNames,
		 final int[] preferredWidths){
		final int[] w=preferredWidths==null?
			 computePreferredWidthFromColumnNames(font, columnNames):
			 preferredWidths;
		int preferredWidth=0;
		for (int i=0; i<w.length; i++){
			preferredWidth+=w[i];
		}
		return preferredWidth;
	}

	private static String []makeNames(Object [][]data){
		final String[] columnNames=new String[data[0].length];
		for (int i=0; i<columnNames.length; i++){
			columnNames[i]=""+data[0][i];
		}
		return columnNames;
	}

	public static int getPreferredWidth(
			final JTable nestingTable,
			final Object[][] data){
		return getPreferredWidth(
				  nestingTable.getFont(),
				  makeNames(data),
				  null);
	}

	public static int[] computePreferredWidthFromColumnNames(
		 final Font font,
		 final String[] columnNames){
		final int[] w=new int[columnNames.length];
		final int z=font.getSize();
		for (int i=0; i<columnNames.length; i++){
			w[i]= (z*columnNames[i].length());
		}
		return w;
	}

	public static class StringConverterRenderer
		extends DefaultTableCellRenderer{


	  public Component getTableCellRendererComponent(
		  final JTable table,
		  final Object value,
		  final boolean isSelected,
		  final boolean hasFocus,
		  final int row,
		  final int visualColumnIndex){
		StringConverter sc=(StringConverter) DefaultStringConverters.get(value == null ? Object.class : value.getClass());
		if (sc == null){
			sc=(StringConverter) DefaultStringConverters.get(Object.class);
		}
		final int ha=sc.getHorizontalAlignment();
		setHorizontalTextPosition(ha);
		setHorizontalAlignment(ha);
		final String txt=value == null ? "" : sc.toString(value);
		setText(txt);
		return this;
	  }
	}

	public static class Table
		 extends JTable{
		final boolean hideHeader, resizeParentColumn;
		final int maxRows, columns, widthOfParentContainingColumn;
		final Object []columnNames;
		final private JScrollPane pane;
		int rows=-1;

		public int getRowCount(){
			return rows==-1 ? super.getRowCount() : rows;
		}

		public Table(
			 final Object[][] data,
			 final Object[] columnNames,
			 final boolean hideHeader,
			 final int widthOfParentContainingColumn){
			super(data, columnNames);
			this.columnNames=columnNames;
			this.hideHeader=hideHeader;
			this.rows=data.length;
			this.maxRows=rows;
			this.columns=data[0].length;
			this.widthOfParentContainingColumn=widthOfParentContainingColumn;
			this.resizeParentColumn=widthOfParentContainingColumn>=0;
			setDefaultRenderer(Object.class, new StringConverterRenderer());
			pane=new JScrollPane(this);
		}

		public Table(final Object[][] data){
			this(data, makeNames(data), true, -1);
		}

		public Table(
				  final Object[][] data,
				  final String []columnNames){
			this( data,
						columnNames,
						false,
						getPreferredWidth(
								 PersonalizableTable.baseEditableFont,
								 columnNames,null) );
		}

		void sizeParent(
				final JTable nestingTable,
				final int row,
				final int column){
			nestingTable.setRowHeight((maxRows + (hideHeader?0:1))*23);
			if (resizeParentColumn && column>=0){
				final TableColumnModel tcm=nestingTable.getColumnModel();
				tcm.getColumn(column).setPreferredWidth( widthOfParentContainingColumn);
			}
		}

		void setValues(final Object[][] data){
			final TableModel tm=getModel();
			int row=0;
			for (; row<data.length; row++){
				final Object[] rowOfValues=data[row];
				for (int column=0; column<rowOfValues.length; column++){
					tm.setValueAt(data[row][column], row, column);
				}
			}
			this.rows=data.length;
/*			for(;row<rows;row++){
				for (int column=0;column<columns;column++){
					tm.setValueAt(null, row, column);
				}
			}*/
		}

		protected void configureEnclosingScrollPane(){
			Container p=getParent();
			if (p instanceof JViewport){
				Container gp=p.getParent();
				if (gp instanceof JScrollPane){
					JScrollPane scrollPane= (JScrollPane) gp;
					// Make certain we are the viewPort's view and not, for
					// example, the rowHeaderView of the scrollPane -
					// an implementor of fixed columns might do this.
					JViewport viewport=scrollPane.getViewport();
					if (viewport==null||viewport.getView()!=this){
						return;
					}
					if (!hideHeader){
						scrollPane.setColumnHeaderView(getTableHeader());
					}
					scrollPane.getViewport().setBackingStoreEnabled(true);
					scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
				}

			}
		}


			public Object[][] getValues(){
				final TableModel tm=getModel();
				final int rows=tm.getRowCount(), columns=tm.getColumnCount();
				final Object[][] data=new Object[rows][columns];
				for (int row=0; row<rows; row++){
					for (int column=0; column<columns; column++){
						data[row][column]=tm.getValueAt(row, column);
					}
				}
				return data;
			}

};

	public Component getTableCellEditorComponent(
		 final JTable nestingTable,
		 final Object value,
		 final boolean isSelected,
		 final int row,
		 final int column){
		return nestedTable.pane;
	}

	static final class Factory
		 implements TableCellRenderer{

		Factory(final int rows, int columns){
			this(new Object[rows][columns]);
		}

		Factory(final Object [][]data){
			renderer=new Table(data);
			editor=new NestedTableCellEditor( new Table(data));
		}

		Factory(final Object [][]data, final String []columnNames){
			renderer=new Table(data, columnNames);
			editor=new NestedTableCellEditor( new Table(data, columnNames));
		}

		private final NestedTableCellEditor editor;
		private final Table renderer;

		Factory(final Table editor){
			renderer= new Table(
								 editor.getValues(),
								 editor.columnNames,
								 editor.hideHeader,
								 editor.widthOfParentContainingColumn);
			this.editor=new NestedTableCellEditor(editor);
		}

		boolean isSuitable(final JTable nestingTable, int row, int column){
			final Object value=nestingTable.getValueAt(row, column);
			if ( value instanceof Object[][]){
				final Object [][]oa=(Object[][])value;
				return oa.length>0 && oa[0].length==renderer.columns;
			}
			return false;
		}

		NestedTableCellEditor getTableCellEditor(
				  final JTable nestingTable,
				  final int row,
				  final int column){
			editor.nestedTable.setValues( (Object[][])nestingTable.getValueAt(row, column));
			return editor;
		}


		private final Map resized=new HashMap();

		public Component getTableCellRendererComponent(
			 final JTable nestingTable,
			 final Object value,
			 final boolean isSelected,
			 final boolean hasFocus,
			 final int row,
			 final int visualColumn){
			renderer.setValues( (Object[][]) value);
			//final String key=nestingTable.hashCode()+""+row;
			final String key=""+nestingTable.hashCode();//+""+row;
			if (!resized.containsKey(key)){
				resized.put(key, "yes");
				renderer.sizeParent(nestingTable, row, visualColumn);
			}
			if (nestingTable instanceof PersonalizableTable){
				int modelColumn=SwingBasics.getModelIndexFromVisualIndex(nestingTable, visualColumn);
				( (PersonalizableTable) nestingTable).decorate(
					 nestingTable, renderer, row, modelColumn, isSelected, hasFocus);
			} else{
				if (isSelected){
					renderer.setForeground(nestingTable.getSelectionForeground());
					renderer.setBackground(nestingTable.getSelectionBackground());
				} else{
					renderer.setForeground(nestingTable.getForeground());
					renderer.setBackground(nestingTable.getBackground());
				}
			}
			return renderer.pane;
		}
	}

	final private Table nestedTable;

	NestedTableCellEditor(final Table nestedTable){
		this.nestedTable=nestedTable;
	}

	public boolean isCellEditable(EventObject evt){
		if (evt instanceof MouseEvent){
			return ( (MouseEvent) evt).getClickCount()>=2;
		}
		nestedTable.setVisible(true);
		return true;
	}

	public Object getCellEditorValue(){
		return nestedTable.getValues();
	}

}
