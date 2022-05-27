package com.MeehanMetaSpace.swing;

import java.awt.event.KeyEvent;
import com.MeehanMetaSpace.*;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.MeehanMetaSpace.Basics;

public class VoodooTableCell {
	    String firstCharValue = "";
		private final boolean isVoodooNeeded = SwingBasics.isMac
				&& !Basics.isPowerPC;
		private final JTextField jt;

		VoodooTableCell(final JTextField jt) {
			this.jt = jt;
		}

		void handle(final KeyStroke ks, final KeyEvent e, final int condition,
				final boolean pressed) {
			if (isVoodooNeeded) {
				if (condition == jt.WHEN_FOCUSED && pressed) { // the important event
					final char c = e.getKeyChar();
					if (c >= ' ' && c <= '~') {
						if (Basics.isEmpty(jt.getText())) {
							ensureFirstChar(c);
						} else if (firstCharValue.length() == 0
								&& jt.getText().length() == 1 
								&& jt.getCaretPosition() == 1) {
							firstCharValue = jt.getText();
							jt.setCaretPosition(jt.getText().length());
						} else if (jt.getText().length() == 1 
								&& jt.getCaretPosition() == 1 
								&& !jt.getText().equals(firstCharValue)) {
							jt.setText(firstCharValue + jt.getText());
							firstCharValue = "";
						} else {
							firstCharValue = "";
						}
					}
					else {
						firstCharValue = "";
					}
				}
			}

		}

		private void ensureFirstChar(final char c) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jt.setText("" + c);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							jt.setCaretPosition(jt.getText().length());
						}
					});
				}
			});
		}
		
		static void startCellEditing(final Object value, final FieldWithPriorValue f, final StringConverter sc){
	        final JTextComponent field=f.getTextComponent();
            f.setPriorValue(value);
            String txt = null;
            if (sc==null){
            	if (f instanceof FormattedTextField){
            	 ((FormattedTextField)field).setValue(value);
            	 txt=field.getText();
            	} else if (value instanceof String){
            	txt=(String)value;
            	field.setText(txt);
            	}
            }else {
            	txt=sc.toString(value);
            	field.setText(txt);
            }
            field.setCaretPosition(0);
            f.getVoodoo().firstCharValue ="";
            field.setSelectionStart(0);
            field.setSelectionEnd(txt.length());

	}

	}


