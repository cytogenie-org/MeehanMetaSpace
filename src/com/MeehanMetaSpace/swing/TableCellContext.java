package com.MeehanMetaSpace.swing;

public class TableCellContext {
	public final PersonalizableTableModel tableModel;
	public final int dataColumnIndex;
	public final Row row;
	public final boolean wasTreeDoubleClicked;
	public TableCellContext(final PersonalizableTableModel tableModel, final int dataColumnIndex, final Row row) {
		this(tableModel, dataColumnIndex, row, false);
		
	}
	
	public TableCellContext(final PersonalizableTableModel tableModel, final int dataColumnIndex, final Row row, final boolean wasTreeDoubleClicked) {
		this.tableModel=tableModel;
		this.dataColumnIndex=dataColumnIndex;
		this.row=row;
		this.wasTreeDoubleClicked=wasTreeDoubleClicked;
	}
}
