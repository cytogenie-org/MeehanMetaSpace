package com.MeehanMetaSpace.swing;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import com.MeehanMetaSpace.Basics;

public class ColumnValueEncoder implements ComputedColumn {
	private final String[]columnNames;
	private final String pattern;
	public ColumnValueEncoder(final String pattern, final String[]columnNames) {
		this.pattern=pattern;
		this.columnNames=columnNames;
	}
	public String getColumnIdentifier() {
		return substitutedPattern ;
	}

	public Object getValue(final Row row) {
		String displayValue = replacementPattern;
		final StringTokenizer tokenizer = new StringTokenizer(replacementPattern,"{}");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();			
			try {
				final int dataColumnIndex = Integer.parseInt(token);
				String columnValue = Basics.toString(row.get(dataColumnIndex));
				if (columnValue != null) {
					displayValue = displayValue.replace("{" + token + "}", columnValue);							
				}
				else {
					displayValue = displayValue.replace("{" + token + "}", "");
				}
			}
			catch(final NumberFormatException e) {
				//e.printStackTrace(System.err);
			}//Continue as it not the column index
		}
		return displayValue;
	}
	
	
	private String substitutedPattern, replacementPattern ;
	
	public void init(List<String> uncomputedColumns) {
		substitutedPattern = pattern;
		replacementPattern = pattern;
		final StringTokenizer tokenizer = new StringTokenizer(pattern,"{}");
		while (tokenizer.hasMoreTokens()) {
			final String token = tokenizer.nextToken();			
			try {
				int columnIndex = Integer.parseInt(token);
				String column = columnNames[columnIndex];
				int dcI = getDataColumnIndex(uncomputedColumns, column);
				replacementPattern = replacementPattern.replace("{" + token + "}", "{" + dcI + "}");
				substitutedPattern = substitutedPattern.replace("{" + token + "}", column);
			}
			catch(NumberFormatException e) {}//Continue as it not the column index
		}
		return;
	}
	
	private int getDataColumnIndex(Collection<String>list, String columnName) {
		for (String cName: list) {
			if (cName.equalsIgnoreCase(columnName)) {
				return ((List)list).indexOf(cName);
			}
		}
		return -1;
	}

}
