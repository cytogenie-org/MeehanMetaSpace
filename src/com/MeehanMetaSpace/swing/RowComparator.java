package com.MeehanMetaSpace.swing;

import java.util.*;

public class RowComparator implements Comparator<Row> {
	private final List listOfOrders_Or_Comparators;
	private final int[] dcs;
	final int n;

	public RowComparator(final List listOfOrders_Or_Comparators,
			final List<Integer> dataColumnIndexes) {
		this.listOfOrders_Or_Comparators = listOfOrders_Or_Comparators;
		n = dataColumnIndexes.size();
		dcs = new int[n];
		for (int i = 0; i < n; i++) {
			dcs[i] = dataColumnIndexes.get(i);
		}
	}

	private int getCmpValue(final Object value, final List l) {
		final int idx = l.indexOf(value);
		return idx < 0 ? Integer.MAX_VALUE : idx;
	}

	public int compare(final Row o1, final Row o2) {
		for (int i = 0; i < n; i++) {
			final int dataColumnIndex = dcs[i];
			final Object o = listOfOrders_Or_Comparators.get(i);
			final Object value1 = o1.get(dataColumnIndex);
			final Object value2 = o2.get(dataColumnIndex);
			if (o instanceof List) {
				final List l = (List) o;
				final int idx1 = getCmpValue(value1, l);
				final int idx2 = getCmpValue(value2, l);
				if (idx1 != idx2) {
					return idx1 - idx2;
				}
			} else if (o instanceof Comparator) {
				int cmp=((Comparator) o).compare(value1, value2);
				if (cmp!=0){
					return cmp;
				}
			}
		}
		return 0;
	}

}
