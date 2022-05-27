package com.MeehanMetaSpace.swing;

import java.util.Collection;
import java.util.Properties;

import javax.swing.Icon;

public interface CollectionBasket {

	String getItemName();
	boolean isDoubleClickObedient();
	
	void setDoubleClickObedient(boolean isDoubleClickObedient);
	
	Icon getDropIcon();
	
	String getCheckOutLabel();
	
	String getBasketWindowTitle();

	String getPurpose();
	String getButtonText();
	char getButtonMnemonic();
	String getPropertyPrefix();

	String getKeyDataColumnName();

	Collection<Integer> getDataColumnIndexes();

	boolean closeOnCheckOut();
	
	void cleanup();
	
	Properties getProperties();
	
	void saveProperties(PersonalizableTableModel tableModel);

	Collection<Row> checkOutPerformed(Collection<Row> rowsInCart, PersonalizableTableModel tableModel, boolean runBackground);
	
	public interface SourceAction {
		void getSource(PersonalizableTableModel tableModel);
	}
	
	public interface UploadAction {
		void putSource();
	}
	
}
