package com.MeehanMetaSpace.swing;

import java.awt.SystemColor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.swing.TextAreaWithHint.DocumentChangeListener;

public class TextFieldWithHint extends javax.swing.JTextField{
	private boolean isHinting=false;
	private String hint=null;
	public void setHint(final String hint) {
		this.hint=hint;
		if (Basics.isEmpty(super.getText())) {
			showHint();
		}
	}
	public void setDocumentChangeListener(final DocumentChangeListener dcl) {
		this.dcl=dcl;
	}
	interface DocumentChangeListener{
		public void onChange(TextFieldWithHint hta);
	}
	private DocumentChangeListener dcl=null;
	public TextFieldWithHint(final int cols){
		super(cols);
		addFocusListener(new FocusAdapter() {
		    public void focusGained(final FocusEvent e) {
		    	if (isHinting) {
		    		setCaretPosition(0);
		    	}
		    }
		});
		setDocument(new PlainDocument() {
			
			public void insertString(int offs, String str, AttributeSet a) {
				try {
					super.insertString(offs, str, a);
					handleChange(str);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			public void remove(int offs, int len) {
				try {
					super.remove(offs, len);
					handleChange( "" );
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	private boolean isSettingHere=false;
	private boolean showHint() {
		if (!Basics.isEmpty(hint)) {
			isHinting=true;
			isSettingHere=true;
			super.setText(hint);
			isSettingHere=false;
			setForeground(SystemColor.textInactiveText);
			if (this.hasFocus()) {
				setCaretPosition(0);
			}
			return true;
		}
		return false;
	}
	private boolean hideHint(final String realText) {
		if (isHinting) {
			setForeground(SystemColor.textText);
			isHinting = false;
			isSettingHere=true;
			super.setText(realText);
			isSettingHere=false;
			return true;
		}
		return false;
	}
	
	public void setText(final String str) {
		if (!Basics.isEmpty(str)) {
			if (!hideHint(str)) {
				this.isSettingHere=true;
				super.setText(str);
				this.isSettingHere=false;
				
			}
			if (dcl != null) {
				dcl.onChange(this);
			}
		} else {
			this.isSettingHere=true;
			super.setText("");
			this.isSettingHere=false;						
			if (dcl != null) {
				dcl.onChange(this);
			}
			showHint();
		}
		
	}

	public String getText() {
		if (isHinting) {
			return "";
		}
		return super.getText();
	}

	private void handleChange(final String str) {
		if (!isSettingHere) {
			if (Basics.isEmpty(super.getText())) {
				showHint();
			} else {
				hideHint(str);
			}
			if (dcl != null) {
				dcl.onChange(this);
			}
		}
	}
}

