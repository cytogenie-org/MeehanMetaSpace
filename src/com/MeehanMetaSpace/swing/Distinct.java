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

final class Distinct{
    private final int[] dataColumns;
    private final ColumnComparator cc;
    private final Set set = new HashSet();

    private Distinct(final MetaRow metaRow, final int[] dataColumns, final Collection rows) {
        this.dataColumns = dataColumns;
        cc = new ColumnComparator(metaRow, dataColumns);
        setRows(rows);
    }

    private final class Row implements Comparable {

        private final com.MeehanMetaSpace.swing.Row row;

        private Row(final com.MeehanMetaSpace.swing.Row row) {
            this.row = row;
        }

        public int compareTo(final Object other) {
            if (other instanceof Row) {
                return cc.compare(row, ((Row) other).row);
            }
            return 0;
        }

        public int hashCode() {
            int result = 17;
            for (int i = 0; i < dataColumns.length; i++) {
                int dataColumn = dataColumns[i];
                final Object thisValue = row.get(dataColumn);
                result = 37 * result + (thisValue == null ? 49 : thisValue.hashCode());
            }
            return result;
        }

        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Row)) {
                return false;
            }
            final com.MeehanMetaSpace.swing.Row thisRow = this.row,
              thatRow = ((Row) other).row;
            for (int i = 0; i < dataColumns.length; i++) {
                int dataColumn = dataColumns[i];
                final Object thisValue = thisRow.get(dataColumn),
                  thatValue = thatRow.get(dataColumn);
                if (!Basics.equals(thisValue, thatValue)) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            return TableBasics.toString(row, dataColumns);
        }
    }


    private final void setRows(final Collection c) {
        for (final Iterator it = c.iterator(); it.hasNext(); ) {
            set.add(new Row((com.MeehanMetaSpace.swing.Row) it.next()));
        }
    }

    private final Collection getRows() {
        final Collection returnValue = new ArrayList();
        for (final Iterator it = set.iterator(); it.hasNext(); ) {
            Row row = (Row) it.next();
            returnValue.add(row.row);
        }
        return returnValue;
    }

    public static Collection compute(final MetaRow metaRow, final int[] dataColumns, final Collection rows) {
        final Distinct distinct = new Distinct(metaRow, dataColumns, rows);
        return distinct.getRows();
    }
}
