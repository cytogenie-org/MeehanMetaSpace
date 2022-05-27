package com.MeehanMetaSpace.swing;

import java.util.*;

import javax.swing.JButton;

import com.MeehanMetaSpace.*;

/**
 * Title: Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class DefaultFilterable implements Filterable {

	public static class Filter {
		final int dataColumnIndex;
		final int op;
		final Object value;

		public Filter(final int dataColumnIndex, final int op,
				final Object value) {
			this.value = value;
			this.dataColumnIndex = dataColumnIndex;
			this.op = op;
		}
	}

	public static class Op {
		private final Row[] filteringRow;
		private final DefaultFilterable[] filterable;


		public Op(final PersonalizableDataSource dataSource,
				final DefaultFilterable.Filter[][] filters) {
			filteringRow = new Row[filters.length];
			filterable = new DefaultFilterable[filters.length];
			for (int i = 0; i < filters.length; i++) {
				filteringRow[i] = dataSource.getBlankFilter();
				filterable[i] = new DefaultFilterable();
				filterable[i].apply(filteringRow[i], filters[i]);
			}
		}

		public boolean or(final Row row) {
			for (int j = 0; j < filteringRow.length; j++) {
				if (isFiltered(row, filteringRow[j], filterable[j])) {
					return true;
				}
			}
			return false;
		}

		public List<Row> or(final List<Row> listOfRows) {
			ArrayList<Row> al = new ArrayList<Row>();
			for (int i = 0; i < listOfRows.size(); i++) {
				final Row row = listOfRows.get(i);
				if (or(row)) {
					al.add(row);
				}
			}
			return al;
		}

		public boolean and(final Row row) {
			int j;
			for (j = 0; j < filteringRow.length; j++) {
				if (!isFiltered(row, filteringRow[j], filterable[j])) {
					break;
				}
			}
			if (j == filteringRow.length) {
				return true;
			}
			return false;

		}

		public List<Row> and(final List<Row> listOfRows) {
			ArrayList<Row> al = new ArrayList<Row>();
			for (int i = 0; i < listOfRows.size(); i++) {
				final Row row = listOfRows.get(i);
				if (and(row)) {
					al.add(row);
				}
			}
			return al;
		}
	}

	public void apply(final Row filteringRow, final Filter[] filter) {
		for (int i = 0; i < filter.length; i++) {
			final Filter f = filter[i];
			filteringRow.set(f.dataColumnIndex, f.value);
			setColumnOp(f.op, f.dataColumnIndex);
		}
	}

	public void clear(Row filteringRow) {
		int nCols = filteringRow.getColumnCount();
		for (int dataColumnIndex = 0; dataColumnIndex < nCols; dataColumnIndex++) {
			filteringRow.set(dataColumnIndex, null);
			setColumnOp(Filterable.none, dataColumnIndex);
		}
	}

	public DefaultFilterable() {
	}

	static final int defaultBooleanFilterOp=Filterable.equals, defaultNumericFilterOp = Filterable.equals,
			defaultDateFilterOp = Filterable.greaterEquals,
			defaultStringFilterOp = Filterable.startsWith,
			defaultButtonFilterOp=Filterable.equals;
	private final Map<Integer, Integer> ops = new HashMap<Integer, Integer>();

	public void setColumnOp(int op, int dataColumnIndex) {
		if (dataColumnIndex >= 0) {
			ops.put(new Integer(dataColumnIndex), new Integer(op));
		}
	}

	private int getFilterOp(final int dataColumnIndex, final Class cl) {
		Integer op = (Integer) ops.get(new Integer(dataColumnIndex));
		if (op != null) {
			return op.intValue();
		}
		return getDefaultFilterOp(cl);
	}

	public final static String getDefaultFilterOpValue(final Class cl) {
		return Filterable.allFilterOp[getDefaultFilterOp(cl)];
	}

	public final static int getDefaultFilterOp(final Class cl) {
		if (cl.equals(Float.class) || cl.equals(Integer.class)) {
			return defaultNumericFilterOp;
		} else if (cl.equals(Date.class)) {
			return defaultDateFilterOp;
		} else if (cl.equals(ComparableBoolean.class) || cl.equals(Boolean.class)){
			return defaultBooleanFilterOp;
		}else if (cl.equals(JButton.class)){
		return defaultButtonFilterOp;
	}
	return defaultStringFilterOp;
	}

	public static boolean isFiltered(final Object filteringObject,
			final Object filteredObject, final int op) {
		boolean ok = false;

		if (filteredObject instanceof Date || filteredObject instanceof Float
				|| filteredObject instanceof Integer) {
			int result = ((Comparable) filteredObject)
					.compareTo(filteringObject);
			switch (op) {
			case Filterable.none:
				ok = true;
				break;
			case Filterable.equals:
				ok = result == 0;
				break;
			case Filterable.notEquals:
				ok = result != 0;
				break;
			case Filterable.greater:
				ok = result > 0;
				break;
			case Filterable.greaterEquals:
				ok = result >= 0;
				break;
			case Filterable.lesser:
				ok = result < 0;
				break;
			case Filterable.lesserEquals:
				ok = result <= 0;
				break;
			}

		} else {
			final String s1 = PersonalizableTableModel.toSequenceableString(
					filteredObject, null);
			final String s2 = PersonalizableTableModel
			.toSequenceableString(filteringObject, null);
	
			switch (op) {
			case Filterable.none:
				ok = true;
				break;
			case Filterable.startsWith:
				ok = s1.startsWith(filteringObject.toString());
				break;
			case Filterable.notStartsWith:
				ok = !s1.startsWith(filteringObject.toString());
				break;
			case Filterable.endsWith:
				ok = s1.endsWith(filteringObject.toString());
				break;
			case Filterable.notEndsWith:
				ok = !s1.endsWith(filteringObject.toString());
				break;
			case Filterable.contains:
				ok = s1.indexOf(filteringObject.toString()) >= 0;
				break;
			case Filterable.notContains:
				ok = s1.indexOf(filteringObject.toString()) < 0;
				break;
			default:
				final int result;
				if ((filteredObject!=null && !filteredObject.getClass().isInstance(filteringObject))
						|| !(filteredObject instanceof Comparable)
						|| !(filteringObject instanceof Comparable)) {
					result = s1.compareTo(s2);
				} else {
					result = ((Comparable) filteringObject)
							.compareTo(filteredObject);
				}
				switch (op) {
				case Filterable.equals:
					ok = result == 0;
					break;
				case Filterable.notEquals:
					ok = result != 0;
					break;

				case Filterable.greater:
					ok = result < 0;
					break;
				case Filterable.greaterEquals:
					ok = result <= 0;
					break;
				case Filterable.lesser:
					ok = result > 0;
					break;
				case Filterable.lesserEquals:
					ok = result >= 0;
					break;
				}
			}
		}

		return ok;
	}

	public boolean filters(final Object filteringObject,
			final Object filteredObject, final int filteredDataIndex) {
		return isFiltered(
				filteringObject, 
				filteredObject, 
				getFilterOp(filteredDataIndex, filteringObject.getClass()));
	}

	public static boolean isFiltered(final Row filteredRow,
			final Row filteringRow, final Filterable cmp) {
		for (int i = 0; i < filteringRow.getColumnCount(); i++) {
			final Object filteringObject = filteringRow.getFilterableValue(i);
			if (!Basics.isEmpty(filteringObject)) {
				final Object filteredObject = filteredRow.getFilterableValue(i);
				if (filteredObject == null
						|| !cmp.filters(filteringObject, filteredObject, i)) {
					return false;
				}
			}
		}
		return true;
	}


}
