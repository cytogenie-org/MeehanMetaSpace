package com.MeehanMetaSpace.swing;

import com.MeehanMetaSpace.*;
import java.awt.Window;
import java.util.*;

import javax.swing.Icon;

/**
 * Title:        Meehan Meta Space Software
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Stephen Meehan
 * @version 1.0
 */

public abstract class DefaultMetaRow implements MetaRow {
    public boolean isEditable(final int dataColumnIndex) {
        return true;
    }

    public StringConverter getStringConverter(final int dataColumnIndex) {
        return DefaultStringConverters.get(getClass(dataColumnIndex));
    }

    public SortValueReinterpreter getSortValueReinterpreter(final int
      dataColumnIndex) {
        return null;
    }

	public Integer getSpecialHorizontalAlignment(final int dataColumnIndex){
		return ((Column) columns.get(dataColumnIndex)).getSpecialHorizontalAlignment();
	}

    public String getEditMask(final int dataColumnIndex) {
        return ((Column) columns.get(dataColumnIndex)).getEditMask();
    }

    public String getDecimalFormat(final int dataColumnIndex) {
        return ((Column) columns.get(dataColumnIndex)).getDecimalFormat();
    }

    public Class getDecimalClass(final int dataColumnIndex) {
        return ((Column) columns.get(dataColumnIndex)).getDecimalClass();
    }

    public String getDateFormat(final int dataColumnIndex) {
        return ((Column) columns.get(dataColumnIndex)).getDateFormat();
    }

    /**
     * Immutable class.
     *
     * <p>Title: FacsXpert</p>
     * <p>Description: </p>
     * <p>Copyright: Copyright (c) 2003</p>
     * <p>Company: Herzenberg Lab, Stanford University</p>
     * @author Stephen Meehan
     * @version 1.0
     */
    public abstract static class Column implements Comparable {
    	protected String label;
    	
    	public Integer getSpecialHorizontalAlignment(){
    		return null;
    	}
        public Object getRenderOnlyValue(final Object rowObject) {
            return null;
        }

        public String getEditMask() {
            return null;
        }

        public String getDecimalFormat() {
            return null;
        }

        public Class getDecimalClass() {
            return null;
        }

        public String getDateFormat() {
            return null;
        }

        public final String name;
        public final Class cls;

        private int dataColumnIndex;

        protected Column(final String name, final Class cls) {
            this.name = name;
            this.cls = cls;
        }

        public final int getDataColumnIndex() {
            return dataColumnIndex;
        }

        public final String toString() {
            return name;
        }

        public final boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Column)) {
                return false;
            }
            Column c = (Column) o;
            return name.equals(c.name);
        }

        public final int hashCode() {
            return name.hashCode();
        }

        public final int compareTo(final Object o) {
            if (o instanceof Column) {
                return name.compareTo(((Column) o).name);
            }
            return -1;
        }

        /**
         * Override to extract the value for the table
         * @param rowObject the object from which Column understands how to
         * extract a row value
         * @return
         */
        protected abstract Object get(Object rowObject);

        /**
         * Override if cell value for column can be edited
         *
         * @param rowObject data for row as understood by super class
         * @param newCellValueForObject new value to put in rowObject
         *
         * @Throws UnsupportedOperationException if isEditable()==false
         */
        protected void set(Object rowObject, Object newCellValueForObject) {
            throw new UnsupportedOperationException("Read only by default");
        }

        /**
         * Override if rowObject value for the column is not read only.
         *
         * @param rowObject
         * @return true if can edit, otherwise false
         */
        protected boolean isEditable(final Object rowObject) {
            return false;
        }

        /**
         * Override if rowObject value for the column is explorable
         *
         * @param rowObject
         * @return true/false if can/can't explore
         */
        protected boolean isExplorable(final Object rowObject) {
            return false;
        }

        /**
         * Override to explore rowObject value for the column
         *
         * @param rowObject
         * @return true/false if explored/not explored
         */
        protected boolean explore(final Object rowObject) {
            throw new UnsupportedOperationException(
              "Can not explore by default");
        }

        /**
         * Override if there is relevant cell advice
         *
         * @param cellAdvice
         * @return true/false if advise was/wasn't set
         */
        public boolean setAdvice(final Object rowObject,
                                 final CellAdvice cellAdvice) {
            return false;
        }

    }


    public class Row implements com.MeehanMetaSpace.swing.Row {
    	
		public boolean isActive() {
			return true;
		}
		
		public String getRowId() {
    		return null;
    	}
		
		public String getRowType() {
			return null;
		}

        public Object getRenderOnlyValue(final int dataColumnIndex, boolean isSelected, boolean hasFocus) {
            return ((Column) columns.get(dataColumnIndex)).getRenderOnlyValue(
              rowObject);
        }

        public void endImport() {
        }

        public Icon getIcon(final int dataColumn) {
            return null;
        }

        public boolean equals(final Object that) {
            if (this == that) {
                return true;
            }
            if (that instanceof Row) {
                return Basics.equals(((Row) that).rowObject, rowObject);
            }
            return false;
        }

        public int hashCode() {
            return rowObject != null ? rowObject.hashCode() : 0;
        }

        public com.MeehanMetaSpace.swing.MetaRow getMetaRow() {
            return DefaultMetaRow.this;
        }
        
        public Collection getUnselectableValues(final int dataColumnIndex) {
        	return Basics.UNMODIFIABLE_EMPTY_LIST;
        }

        public Collection getAllowedValues(final int dataColumnIndex) {
            return null;
        }

        public boolean allowNewValue(final int columnDataIndex) {
            return false;
        }

        public boolean isDeletable() {
            return true;
        }

        public final Object rowObject;
        public Row(final Object rowObject) {
            this.rowObject = rowObject;
        }

        public final boolean isExplorable(final int dataColumnIndex) {
            return ((Column) columns.get(dataColumnIndex)).isExplorable(
              rowObject);
        }

        public final void explore(final int dataColumnIndex) {
            ((Column) columns.get(dataColumnIndex)).explore(rowObject);
        }

        public final boolean setAdvice(final int dataColumnIndex,
                                       final CellAdvice cellAdvice) {
            return ((Column) columns.get(dataColumnIndex)).setAdvice(rowObject,
              cellAdvice);
        }

        public final boolean isEditable(final int dataColumnIndex) {
            return ((Column) columns.get(dataColumnIndex)).isEditable(rowObject);
        }

        public final void set(final int dataColumnIndex,
                              final Object newValueForCell) {
            ((Column) columns.get(dataColumnIndex)).set(rowObject,
              newValueForCell);
        }

        public final Object get(final int dataColumnIndex) {
            return ((Column) columns.get(dataColumnIndex)).get(rowObject);
        }

        public final int getColumnCount() {
            return columns.size();
        }

		public Object getFilterableValue(int dataColumnIndex) {
			return get(dataColumnIndex);
		}

		public Collection getForbiddenValues(int dataColumnIndex) {
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}

		public boolean shouldAllowedValuesBeSorted(int dataColumnIndex) {
			return true;
		}
    }


    public static Column[] concatenate(
      final Column[] a1,
      final Column[] a2) {
        final Column[] newArray = new Column[a1.length + a2.length];
        return (Column[]) Basics.toArray(newArray, a1, a2);
    }

    public final Class getClass(final int dataColumnIndex) {
        final Class cls = ((Column) columns.get(dataColumnIndex)).cls;
        //final Class test=cls.getSuperclass();
        return cls;
    }

    private List<Column> columns;

    public final boolean containsAllIdentifiers(final List<String> names) {
        for (final Iterator it = names.iterator(); it.hasNext(); ) {
            final String name = (String) it.next();
            if (indexOf(name) < 0) {
                return false;
            }
        }
        return true;
    }

    public final List cloneDataColumnIdentifiers() {
        ArrayList al = new ArrayList();
        Iterator it = columns.iterator();
        while (it.hasNext()) {
            al.add(it.next().toString());
        }
        return al;
    }

    public Icon getIcon(final int dataColumn) {
        return null;
    }

    public Icon getIcon(
      final Iterator selectedRows,
      final int dataColumn,
      final boolean isExpanded,
      final boolean isLeaf) {
        return null;
    }

    public final String getDataColumnIdentifier(final int dataColumnIndex) {
        return columns.get(dataColumnIndex).toString();
    }

    /**
     * Sub classes override this method if the label differs from the name
     *
     * @param dataColumnIndex
     * @return the label which differs from the name
     */

    public String getLabel(final int dataColumnIndex) {
    	final Column column=columns.get(dataColumnIndex);
        return column.label==null ? getDataColumnIdentifier(dataColumnIndex):column.label;
    }

    public final int size() {
        return columns.size();
    }

    public final int getDataColumnIndex(final Column column) {
        return indexOf(column.name);
    }

    public final int indexOf(final String name) {
        final int n = columns.size();

        for (int i = 0; i < n; i++) {
            final Column column = (Column) columns.get(i);
            if (column.name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public final boolean equals(final Column column, final int dataColumnIndex) {
        return (((Column) columns.get(dataColumnIndex)).equals(column));
    }

    public final Column getColumn(final int dataColumnIndex) {
        return (Column) columns.get(dataColumnIndex);
    }

    public final Column getColumn(final String name) {
        final int index = indexOf(name);
        if (index >= 0) {
            return getColumn(index);
        }
        return null;
    }

    public boolean isCreatable() {
        return true;
    }

    public String getKey() {
        return columns.get(0).toString();
    }

    protected DefaultMetaRow() {

    }

    protected DefaultMetaRow(final Column[] columns) {
        setColumns(columns);
    }

    /**
     * Can not override, as it is called inside a constructor!
     * @param columns
     */
    protected final void setColumns(final Column[] columns) {
        if (columns == null) {
            return;
        }
        if (Basics.isEmpty(columns) || this.columns != null) {
            throw new IllegalArgumentException(
              "DefaultMetaRow is immutable, thus columns MUST be provided to constructor");
        } else {
            final ArrayList al = new ArrayList();
            for (int i = 0; i < columns.length; i++) {
                if (al.contains(columns[i])) {
                    throw new IllegalArgumentException("Duplicate column # " +
                      (i + 1) +
                      ":  " + columns[i].name);
                }
                al.add(columns[i]);
                columns[i].dataColumnIndex = i;
            }
            this.columns = Collections.unmodifiableList(al);
        }
    }

    /**
     * Override if you need a new subclass to DefaultMetaRow.DataSource
     *
     * @return data source for rows of meta row
     */
    protected DataSource newDataSource() {
        return new DataSource();
    }

    protected DataSource dataSource = null;

    protected synchronized final void setDataSource(final DataSource dataSource) {
        if (this.dataSource != null) {
            throw new UnsupportedOperationException(
              "DefaultMetaRow.dataSource can only be set once");
        }
        this.dataSource = dataSource;
    }

    public synchronized final DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = newDataSource();
        }
        return dataSource;
    }

    public class DataSource extends DefaultPersonalizableDataSource.Picker {

        /**
         * Override to assist in constructing from JTable GUI
         * @param recommendedCount
         * @return
         */
        protected java.util.List getObjectsForNewRows(int recommendedCount) {
            return null;
        }

        /**
         * Override if different Row characteristics are needed
         * @param newObject
         * @return
         */
        protected Row newRow(final Object newObject) {
            return DefaultMetaRow.this.new Row(newObject);
        }

        private java.util.List objectsForNewRows = null;

        protected DataSource() {
            super(DefaultMetaRow.this);
        }

        protected final Row[] create(final Object rowObject) {
            objectsForNewRows = Collections.singletonList(rowObject);
            return (Row[]) create((PersonalizableTableModel )null);
        }

        public final Row[] create(final Object[] rowObjects) {
            if (rowObjects != null) {
                objectsForNewRows = Arrays.asList(rowObjects);
                return (Row[]) create((PersonalizableTableModel )null);
            }
            return new Row[0];
        }

        protected final void setMetaRow(MetaRow metaRow) {
            throw new UnsupportedOperationException("Can not alter meta row");
        }

        public final com.MeehanMetaSpace.swing.Row[] create(final PersonalizableTableModel modelShowing) {
            if (Basics.isEmpty(objectsForNewRows)) {
                objectsForNewRows = getObjectsForNewRows(1);
            }
            Row[] created = null;
            if (!Basics.isEmpty(objectsForNewRows)) {
                final int n = objectsForNewRows.size();
                created = new Row[n];
                if (modelShowing != null) {
					modelShowing.delayReSyncResolution();
				}
                for (int i = 0; i < n; i++) {
                    created[i] = newRow(objectsForNewRows.get(i));
                    if (modelShowing != null) {
						modelShowing.setFromContext(created[i]);
						modelShowing.addCreationToViewedTable(created[i]);
					}
                    add(created[i]);
                }
                if (modelShowing != null) {
					modelShowing.undelayReSyncResolution(true);
				}
            }
            objectsForNewRows = null;
            return created;
        }
    }


    protected PersonalizableTableModel tableModel = null;

    public synchronized final void setTableModel(final PersonalizableTableModel
                                                 tableModel) {
        if (tableModel != null) {
            throw new UnsupportedOperationException(
              "DefaultMetaRow.tableModel can only be set once");
        }
        this.tableModel = tableModel;
    }

    public synchronized final PersonalizableTableModel getTableModel() {
        return getTableModel((Window)null);
    }

    
    protected String fileName;
    public void savePersonalizations() {
        PropertiesBasics.saveProperties(tableModel.
                                        updatePropertiesWithPersonalizations(false),
                                        fileName, "");
    }
    public synchronized PersonalizableTableModel getTableModel(
    	      final Properties properties) {
    	        if (tableModel == null) {
    	            tableModel = PersonalizableTableModel.activate(getDataSource(), properties, false);
    	// must call tableModel.setKey() BEFORE tableModel.getPropertyDir()
    	            tableModel.setKey(getKey());
    	            final PersonalizableTable jpt = new PersonalizableTable(tableModel);
    	            tableModel.setAutoFilter(true);
    	        }
    	        return tableModel;
    	    }

    public synchronized PersonalizableTableModel getTableModel(
      final Window window) {
        if (tableModel == null) {
            tableModel = PersonalizableTableModel.activate(getDataSource(), false);
// must call tableModel.setKey() BEFORE tableModel.getPropertyDir()
            tableModel.setKey(getKey());
            fileName = tableModel.getPropertyFolder() + "default.properties";
            tableModel.setProperties(PropertiesBasics.loadProperties(fileName));
            final PersonalizableTable jpt = new PersonalizableTable(tableModel);
            tableModel.setAutoFilter(true);
            if (window != null) {
                tableModel.setPersonalizableWindowOwner(window);
                window.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        savePersonalizations();
                    }
                });
            }
        }
        return tableModel;
    }
    public boolean hasNonAlphabeticSort(final int dataColumnIndex){
    	return false;
    }
   
}
