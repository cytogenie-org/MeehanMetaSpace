package com.MeehanMetaSpace.swing;

import java.util.List;

public interface ComputedColumn {
	void init(List<String> uncomputedColumns);
	Object getValue(final Row row);
	String getColumnIdentifier();
}
