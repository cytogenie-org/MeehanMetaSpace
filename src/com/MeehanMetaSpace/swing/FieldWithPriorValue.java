package com.MeehanMetaSpace.swing;
import javax.swing.text.JTextComponent;

public interface FieldWithPriorValue {
	Object getPriorValue();
	void setPriorValue(Object o);
	VoodooTableCell getVoodoo();
	JTextComponent getTextComponent();
}
