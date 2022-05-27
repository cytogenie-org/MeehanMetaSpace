package com.MeehanMetaSpace.swing;
import java.util.*;
import com.MeehanMetaSpace.*;

/**
 * Creates abstract and/or summary of a column
 * @author Meehan
 *
 */
public class ColumnGroupAbstract {
	private int distinctGroups;
	private final int []groupIdPerRow;
	private Set<Composite> values=new HashSet<Composite>();
	public final int []dataColumnIndexes;
	public final String name;
	private final PersonalizableTableModel tableModel;

	public ColumnGroupAbstract(final PersonalizableTableModel tableModel, final Collection<Integer> dataColumnIndexes) {
		this.tableModel=tableModel;
		final PersonalizableDataSource dataSource=tableModel.getDataSource();
		abbreviations=new ArrayList<String>();

		this.dataColumnIndexes = new int[dataColumnIndexes.size()];
		int i = 0;
		for (final int dataColumnIndex : dataColumnIndexes) {
			this.dataColumnIndexes[i++] = dataColumnIndex;
			abbreviations.add(tableModel.getColumnAbbreviation(dataColumnIndex));
		}
		name=Basics.toString(abbreviations);
		final List<Row> rows=dataSource.getDataRows();
		groupIdPerRow=new int[rows.size()];
		Composite prior=null;
		distinctGroups=0;
		i=0;
		for (final Row row : rows) {
			final ArrayList l = new ArrayList();
			for (final int dataColumnIndex : this.dataColumnIndexes) {
				final Object o = row.get(dataColumnIndex);
				l.add(o);
			}

			final Composite value = new Composite(l);
			if (!value.equals(prior)) {
				distinctGroups++;
				prior = value;
			}
			values.add(value);
			groupIdPerRow[i++] = 100000+distinctGroups;
		}
	}

	public final List<String> abbreviations;
	/*
     * This method helps avoid setting the value specific to AssayTypeIndex to "name" variable"
     */
	public ColumnGroupAbstract(final PersonalizableTableModel tableModel, final Collection<Integer> dataColumnIndexes, final int ignoreThisOne) {
		this.tableModel=tableModel;
		final PersonalizableDataSource dataSource=tableModel.getDataSource();
		abbreviations=new ArrayList<String>();

		this.dataColumnIndexes = new int[dataColumnIndexes.size()];
		int i = 0;
		for (final int dataColumnIndex : dataColumnIndexes) {
			this.dataColumnIndexes[i++] = dataColumnIndex;
			if(dataColumnIndex != ignoreThisOne) {
				abbreviations.add(tableModel.getColumnAbbreviation(dataColumnIndex));
			}
		}
		name=Basics.toString(abbreviations);
		final List<Row> rows=dataSource.getDataRows();
		groupIdPerRow=new int[rows.size()];
		Composite prior=null;
		distinctGroups=0;
		i=0;
		for (final Row row : rows) {
			final ArrayList l = new ArrayList();
			for (final int dataColumnIndex : this.dataColumnIndexes) {
				final Object o = row.get(dataColumnIndex);
				l.add(o);
			}

			final Composite value = new Composite(l);
			if (!value.equals(prior)) {
				distinctGroups++;
				prior = value;
			}
			values.add(value);
			groupIdPerRow[i++] = 100000+distinctGroups;
		}
	}

	public String getAnomalyWithGroupingsRowOrderAndDistinctValues() {
		if (values.size()<distinctGroups) {
			final StringBuilder sb=new StringBuilder();
			sb.append("There are <b>");
			sb.append(values.size());
			if (dataColumnIndexes.length>1) {
				sb.append("</b> distinct sets of values for ");
			} else {
				sb.append("</b> distinct values for ");
			}
			sb.append(name);
			sb.append("<br> but there are <b>");
			sb.append(distinctGroups);
			sb.append("</b> groupings based on how you<br>haved ordered the rows!");
			return sb.toString();
		}
		return null;
	}

	public Row getRow(final int dataColumnIndex, final Object searchArgument) {
		final int idx=indexOf(0, dataColumnIndex,searchArgument);
		if (idx>=0) {
			final List<Row> rows=tableModel.getDataSource().getDataRows();
			return rows.get(idx);
		}
		return null;
	}

	public int indexOf(final int dataColumnIndex, final Object searchArgument) {
		return indexOf(0, dataColumnIndex,searchArgument);
	}
	public int indexOf(final int startingIndex, final int dataColumnIndex, final Object searchArgument) {
		final List<Row> rows=tableModel.getDataSource().getDataRows();
		for (int i=startingIndex;i<rows.size();i++) {
			final Row row=rows.get(i);
			final Object value = row.get(dataColumnIndex);
			if (Basics.equals(value, searchArgument)) {
				return i;
			}
		}
		return -1;
	}

	public String encodeGroupId(final int dataColumnIndex, final Object searchArgument) {
		final int idx=indexOf(dataColumnIndex, searchArgument);
		if (idx>=0) {
			return ""+groupIdPerRow[idx];
		}
		return null;
	}

	public String encodeGroupAndIndexOf(final int dataColumnIndex, final Object searchArgument) {
		return encodeGroupAndIndexOf(0, dataColumnIndex, searchArgument);
	}

	public String encodeGroupAndIndexOf(final int startingIndex, final int dataColumnIndex, final Object searchArgument) {
		final int idx=indexOf(startingIndex, dataColumnIndex, searchArgument);
		if (idx>=0) {
			return groupIdPerRow[idx]+"."+(100000+idx);
		}
		return null;
	}
	
	public PersonalizableTableModel getTableModel(){
		return tableModel;
	}
}
