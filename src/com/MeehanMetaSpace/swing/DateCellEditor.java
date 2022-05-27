
package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.*;
import java.util.*;
import java.text.DateFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class DateCellEditor
	extends AbstractCellEditor
	implements TableCellEditor {

	final JSpinner field;
	final DateFormat dateFormat;
	final String format;

	// Initializes the spinner.
	public DateCellEditor(final DateFormat df, final String formatHelp) {
		dateFormat=df;
		format=formatHelp;
		field = Editor.DateSpinner.New(df);

	}


			public static final String defaultFormat="MMM dd yyyy";
			public static String dflt(final String format){
				return format == null ? defaultFormat:format;
			}


	public DateCellEditor(String format) {
		this(new java.text.SimpleDateFormat(dflt(format)), dflt(format));
	}

	boolean convertingStringToDate;

	// Prepares the spinner component and returns it.
	public Component getTableCellEditorComponent(
			final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		if (! (value instanceof Date)) {
			convertingStringToDate = true;
			if (value != null) {
				final Date date=GmtFormat.parse( value.toString() );
				if (date != null ){
					field.setValue(date);
					field.setToolTipText(toolTip());
				}
			}
		}
		else {
			convertingStringToDate = false;
			field.setValue(value);
		}
		return field;
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
		try{
			field.commitEdit();
			Object value = field.getValue();
			if (convertingStringToDate) {
				value = GmtFormat.format((Date)value);
			}
			return value;

		} catch (Exception e){
			Pel.log.warn(e);
			PopupBasics.alertAsync(toolTip(), true);
		}
		return null;
	}

	String toolTip(){
		return "Enter a date with format " +format;
	}

	}







