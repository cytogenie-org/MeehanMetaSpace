package com.MeehanMetaSpace.swing;

import java.awt.event.KeyEvent;

import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.*;
import javax.swing.event.*;


public class FormattedTextField extends JFormattedTextField implements FieldWithPriorValue{
	VoodooTableCell voodoo=new VoodooTableCell(this);
	
	public FormattedTextField(final MaskFormatter maskf){
		super(maskf);
		getDocument().addDocumentListener(new KeyAndDocEventCoordinator());		
	}

	public FormattedTextField(final JFormattedTextField.AbstractFormatter f){
		super(f);
	}
	public FormattedTextField(final Object o){
		super(o);
	}

	private class KeyAndDocEventCoordinator implements DocumentListener {
	    
	    public void insertUpdate(DocumentEvent e) {
	        postInsertKey();
	    }
	    public void removeUpdate(DocumentEvent e) {
	    }
	    public void changedUpdate(DocumentEvent e) {
	    }

	    public void postInsertKey() {
	        if (c != '\0'){
	        	keyPressedAndDocUpdated(c);
	        	c='\0';
	        }
	    }
	}
	
	void keyPressedAndDocUpdated(final char c) {	
	}
	
	char c='\0';;
	protected boolean processKeyBinding(
            final KeyStroke ks,
            final KeyEvent e,
            final int condition,
            final boolean pressed) {
              final boolean b = super.processKeyBinding(ks, e, condition, pressed);
              if (pressed){
            	  c=e.getKeyChar();
              }
              voodoo.handle(ks, e, condition, pressed);
                 return b;
    }
	
	private Object priorValue;
	
    public javax.swing.text.JTextComponent getTextComponent(){
    	return this;
    }
    public void setPriorValue(final Object value) {
        priorValue = value;
    }

    public Object getPriorValue() {
        return priorValue;
    }
    
    public VoodooTableCell getVoodoo(){
    	return voodoo;
    }

}
