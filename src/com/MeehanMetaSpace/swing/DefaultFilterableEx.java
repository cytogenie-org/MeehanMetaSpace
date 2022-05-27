
package com.MeehanMetaSpace.swing;

import java.util.*;
import com.MeehanMetaSpace.swing.DefaultFilterable.Filter;

/**
 * Title: Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author
 * @version 1.0
 */

 class DefaultFilterableEx {

	 static class Op {
		private final DefaultFilterableEx[] filterable;
		
		public Op(final PersonalizableDataSource dataSource,
				final Filter[][] query) {
			filterable = new DefaultFilterableEx[query.length];
			int i=0;
			for (final Filter[]filters:query) {
				filterable[i++] = new DefaultFilterableEx(filters);
			}
		}

		public boolean or(final Row row) {
			for (int j = 0; j < filterable.length; j++) {
				if (isFiltered(row, filterable[j])) {
					return true;
				}
			}
			return false;
		}

		public List<Row> or(final List<Row> listOfRows) {
			final ArrayList<Row> al = new ArrayList<Row>();
			for (int i = 0; i < listOfRows.size(); i++) {
				final Row row = listOfRows.get(i);
				if (or(row)) {
					al.add(row);
				}
			}
			return al;
		}


	}


	public void clear(Row filteringRow) {
		int nCols = filteringRow.getColumnCount();
		for (int dataColumnIndex = 0; dataColumnIndex < nCols; dataColumnIndex++) {
			filteringRow.set(dataColumnIndex, null);
		}
	}

	public DefaultFilterableEx(final Filter[]ops) {
		this.ops=ops;
	}

	private final Filter [] ops;
	
	private static boolean isFiltered(final Row filteredRow,
			final DefaultFilterableEx cmp) {
		for (final Filter filter:cmp.ops) {
				final Object filteredObject = filteredRow.getFilterableValue(filter.dataColumnIndex);
				if (!DefaultFilterable.isFiltered(filter.value, filteredObject, filter.op)) {
					return false;
				}
			
		}
		return true;
	}


}
