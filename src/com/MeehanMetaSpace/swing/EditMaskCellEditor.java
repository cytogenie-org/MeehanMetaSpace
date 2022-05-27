package com.MeehanMetaSpace.swing;

import com.MeehanMetaSpace.*;
import java.text.DateFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class EditMaskCellEditor
	 extends DefaultCellEditor
	 implements TableCellEditor {

	final FormattedTextField field;
	final String format;

	// Initializes the spinner.
	public EditMaskCellEditor(final FormattedTextField f, final String format) {
		super(f);
		field = f;
		this.format=format;
	      super.delegate = new EditorDelegate() {
	          public void setValue(final Object value) {
	          	VoodooTableCell.startCellEditing(value, field, null);
	          }
	      };

	}



	// Enables the editor only for double-clicks.
	public boolean isCellEditable(java.util.EventObject evt) {
		if (evt instanceof MouseEvent) {
			return ( (MouseEvent) evt).getClickCount() >= 2;
		}
		return true;
	}

	// Returns the spinners current value.
	public Object getCellEditorValue() {
		return Editor.Mask.getValue(format,
											 field, "Enter a string with format:  " + format);
	}

}


