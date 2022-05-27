package com.MeehanMetaSpace.swing;

import com.MeehanMetaSpace.Basics;
import java.util.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public final class Sifter {
	public static abstract class AbstractSiftee {
		protected final Row firstRowFound;
		protected final String key;
		protected final ArrayList<Row> rows = new ArrayList<Row>();

		protected AbstractSiftee(final String key, final Row firstRowFound) {
			this.key=key;
			this.firstRowFound = firstRowFound;
			rows.add(firstRowFound);
		}

		void associate(final Row row) {
			if (!rows.contains(row)) {
				rows.add(row);
			}
		}

		public abstract void sift(int useCase, Row row);
	}

	public interface SifteeFactory {
		/**
		 * return new siftee or sNULL if no siftee is appropraite
		 * @param firstRowFound
		 * @return
		 */
		public AbstractSiftee newSiftee(String key, Row firstRowFound);
	}

	final TreeMap<String, AbstractSiftee> mapOfSiftees = new TreeMap<String,AbstractSiftee>();
	final int[] primaryKeyColumns;
	final SifteeFactory sifteeFactory;

	/**
	 *
	 * @param primaryKeyColumns the primary keys on which to distinctly index
	 * @param howManyPrimaryKeyColumns how many primary key columns? must be < primaryKeyColumns.length
	 * @throw  IndexOutOfBoundsException if howManyPrimaryKeys > primaryKeyColumns.length
*/
	public Sifter(final SifteeFactory sifteeFactory, final int[] primaryKeyColumns, final int howManyPrimaryKeyColumns) {
		this.sifteeFactory = sifteeFactory;
		this.primaryKeyColumns = new int[howManyPrimaryKeyColumns];
		for (int i = 0; i < howManyPrimaryKeyColumns; i++) {
			this.primaryKeyColumns[i] = primaryKeyColumns[i];
		}
	}

	/**
	 *
	 * @param row
	 * @throw IllegalStateException if called after a call to sift().
	 * Check with isSiftingStarted() returns true
	 * */
	public void sift(final int useCase, final Row row) {
		final String key = TableBasics.encode(row, primaryKeyColumns);
		AbstractSiftee preExisting = mapOfSiftees.get(key);
		if (preExisting == null) {
			preExisting = sifteeFactory.newSiftee(key, row);
			if (preExisting == null) { //factory rejected for whatever reason
				return;
			}
			mapOfSiftees.put(key, preExisting);
		}
		else {
			preExisting.associate(row);
		}
		preExisting.sift(useCase, row);
	}

	public void sift(final int useCase, final List rows) {
		boolean retVal = true;
		for (final Iterator it = rows.iterator(); it.hasNext(); ) {
			Object o = it.next();
			sift(useCase, (Row) o);
		}
	}

	public String getKey(final Row row) {
		return TableBasics.encode(row, primaryKeyColumns);
	}

	public int getIndexedRowCnt(final Row row) {
		AbstractSiftee siftee = getSiftee(row);
		return siftee == null ? 0 : siftee.rows.size();
	}

	public AbstractSiftee getSiftee(final Row row) {
		return mapOfSiftees.get(getKey(row));
	}

	public Collection<AbstractSiftee> getSiftees() {
		return mapOfSiftees.values();
	}

	public AbstractSiftee[] getSifteeArray() {
		final Collection<AbstractSiftee> c = getSiftees();
		return c.toArray(new AbstractSiftee[c.size()]);
	}

    public Object[]getKeys(){
        return (Object[])mapOfSiftees.keySet().toArray();
    }
}
