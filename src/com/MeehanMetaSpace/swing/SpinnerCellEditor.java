package com.MeehanMetaSpace.swing;


import com.MeehanMetaSpace.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class SpinnerCellEditor extends AbstractCellEditor
            implements TableCellEditor {
        final JSpinner spinner;
		  final SpinnerListModel list;

		  // Initializes the spinner.
		  public SpinnerCellEditor(java.util.Collection items) {
			  if (items instanceof java.util.List) {
				  list=new SpinnerListModel( (java.util.List) items);
				  spinner = new JSpinner(list);
			  }
			  else {
				  list=new SpinnerListModel(Basics.toList(items));
				  spinner = new JSpinner(list);
			  }
		  }

		  // Prepares the spinner component and returns it.
		  public Component getTableCellEditorComponent(
			  JTable table, Object value,boolean isSelected,
			  int row, int column) {
			  if (value != null && list.getList().contains(value)) {
				  spinner.setValue(value);

			  }
			  spinner.requestFocus();
			  return spinner;
        }

        // Enables the editor only for double-clicks.
        public boolean isCellEditable(java.util.EventObject evt) {
            /*if (evt instanceof MouseEvent) {
                return ((MouseEvent)evt).getClickCount() >= 2;
            }*/
            return true;
        }

        // Returns the spinners current value.
        public Object getCellEditorValue() {
			  try{
				  spinner.commitEdit();
				  return spinner.getValue();
			  } catch (java.text.ParseException pe){
				  Pel.log.print(pe);
				  java.awt.Toolkit.getDefaultToolkit().beep();
			  }
			  return null;
		}


	public static void main(String []args){

		JTable table = new JTable();
		DefaultTableModel model = (DefaultTableModel)table.getModel();

		// Add some columns
		model.addColumn("A", new Object[]{"item1"});
		model.addColumn("B", new Object[]{"item2"});

		// These are the spinner values
		String[] values = new String[]{"item1", "item2", "item3"};

		// Set the spinner editor on the 1st visible column
		int vColIndex = 0;
		TableColumn col = table.getColumnModel().getColumn(vColIndex);
		col.setCellEditor(new SpinnerCellEditor(java.util.Arrays.asList( values)));

		// If you want to make the cell appear like a spinner in its
		// non-editing state, also set the spinner renderer
		//col.setCellRenderer(new SpinnerRenderer(values));
	}
    }


