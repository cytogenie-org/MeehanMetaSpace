package com.MeehanMetaSpace.swing;

import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public interface PersonalizableDataSource {
	
	
	int FILTER_CONTEXT_NORMAL=0, FILTER_CONTEXT_QUERY_PROPOSING=1, FILTER_CONTEXT_UNGROUPING=2, FILTER_CONTEXT_QUERY_CONCLUSION=3;
	void notifyFilterAppiedListeners(String cmd);
	void setFilteringContext(int id);
	int getFilteringContext();
	void addFilterAppliedListener(ActionListener al);
    
	
	
	boolean canCreateInPlaceWithKeyStroke();

    String getDeleteMessageSuffix();

    boolean isMovable();

    void move(int filteredRowIndex, int direction);

    void fireSortOrderChanged();

    int getMinimumCardinality();

    int getMaximumCardinality();
    
    void triggerDeletion();

    boolean startImport(TabImporter ti);

    void endImport(TabImporter ti);

    void importTextLine(TabImporter ti);

    boolean defineSchemaForImportedRelatives(TabImporter ti);

    void startExport(TabExporter te);

    void endExport(TabExporter te);

    void export(TabExporter te, Row row);

    void configure(PersonalizableTable table);

    String getPropertyPrefix();
    void setPropertyPrefix(String propertyPrefix);
    MetaRow getMetaRow();

    java.util.List<Row> getDataRows();

    int size();

    java.util.List<Row> getFilteredDataRows();

    void finishedDeleting(int count);

    void filterOutAllRows();

    boolean isFiltering();

    void removeAll();

    boolean isFilterable();

    Row getBlankFilter();

    void filter(Row filteringRow, Filterable filterable) throws
      UnsupportedOperationException;
    
	void filter(SeeOnlyTokens sot);

    void filter(Filterable filterable,MultiQueryFilter mfilter) throws
    UnsupportedOperationException;

    void setControlPanelSuspension(JCheckBox suspendControlPanel);
    void showQueryResults(AbstractButton b, String defaultToolTipText);
    String describeHiddenQueryResults();
    
    void setActiveFilterName(String name);

    void setFilter(java.util.List<Row> rows);
    
    void removeFilter();

    void handleNewColumn(javax.swing.table.TableColumn tc,
                                int dataColumnIndex);

    javax.swing.table.TableCellEditor getCellEditor(Row row,
      int dataColumnIndex);
    AutoComplete.FoundListener getFoundListener(Row row,
    	      int dataColumnIndex);

    /**
     *
     * @return true if consumer can create new row
     */
    boolean isCreatable();

    /**
     * Called to create new row(s)
     *
     * @param mdoel the table model create
     * @return the rows that got created
     */
    Row[] create(PersonalizableTableModel modelShowing) throws UnsupportedOperationException;

    /**
     *
     * @return true if resort should happen when consumer creates row
     */
    boolean resortAfterCreate();


    /**
     * @return true if consumer can add a row
     */
    boolean isAddable();

    /**
     * adds row(s)
     */
    int add(Row row);

    /**
     * adds row(s)
     */
    void add() throws UnsupportedOperationException;

    /**
     *
     * @return true if consumer can remove a row
     */
    boolean isRemovable();

    /**
     *
     * @param filteredDataRowIndex remove a specific row
     */
    Row remove(int filteredDataRowIndex) throws
      UnsupportedOperationException;

    /**
     *
     * @return true if consumer can delete an item.
     */
    boolean isDeletable();

    /**
     * Delete specific row.
     *
     * @param filteredDataRowIndex
     */
    void delete(int filteredDataRowIndex) throws
      UnsupportedOperationException;

    boolean proceedWithDeletions(PersonalizableTableModel model, int []selected, int count);

    /**
     *
     * @return true if consumer can save data source
     */
    boolean isSaveable();

    /**
     * Save data source
     *
     * @return true if data source was saved
     */
    boolean save() throws UnsupportedOperationException;

    /**
     *
     * @param row that got double clicked
     * @return true if double click handled
     */
    boolean handleDoubleClick(TableCellContext context);
    
    boolean isDecorator();
    void decorate(TableCellContext context, JComponent component, boolean isSelected, boolean hasFocus);
    void prepareRenderer(final JComponent component, int row, int column);
    boolean canEditFromTreeContext();
    
    void setMultiRowChangeOperationFlag(boolean ok) ;
    
    boolean editAll(PersonalizableTableModel model,
                           PersonalizableTableModel.MultiRowEditRule rule, final int updatedVisualRowIndex);

    boolean editFromTreeContext(PersonalizableTableModel model,
                                       PersonalizableTableModel.
                                       MultiRowEditRule rule, final int updatedVisualRowIndex);


    boolean editFromTreeContext(PersonalizableTableModel model,
                                       String title, RotateTable viewOfRow, final int updatedVisualRowIndex);

    interface CanDisable {
        String getSelectedNodeDisabledText(TreeNode node, Row row);

        Collection getNodeDisabledText(GroupedDataSource.Node node, Row row, int[] sortOrder,
                                              int columnThatDiffers);

        String getNodeEnabledText(TreeNode node, Row row,
                                         int[] sortOrder, int columnThatDiffers,
                                         int uncondensedDataColumnIndex);

        Collection getRowDisabledText(Row row);
    }


    interface CanEditPriorPicks extends CanPick {
        void initPicks();

        boolean wasNodePriorPick(TreeNode node, Row row, int[] sortOrder,
                                        int columnThatDiffers);

        int[] getSelectedRows();
    }


    interface CanPick extends CanDisable {
        Object createPick(TreeNode node, Row row, int columnThatDiffers);

        Object createPick(Row row);

        void resetPicks();

        void tryPicks(java.util.List<Object> picks);

        void completePicks(
      Collection<GroupedDataSource.Node> originalPickNodes,
      Collection<Row> originalPickRows,
          Collection<Row> rejectedRows);

        TreePath[] reorderPicks(TreePath[] tp);
    }



    //new methods to support table TABS for distinct column values
    List<Row> setActiveFilter(String key, ActiveFilter activeFilter);
    void setHiddenFilter(ActiveFilter activeFilter);
    ActiveFilter getHiddenFilter();
    List<Row> applyActiveFilters();
    List<Row> getHiddenRows();
    void setHiddenRows(java.util.List<Row> rows);
    Map<String, ActiveFilter> getActiveFilters();
    boolean removeActiveFilter(String key);
    boolean hasActiveFilter(String key);
    boolean hasActiveFilter();
    List<Row> setActiveFilters(Map<String, ActiveFilter> activeFiltersByKey);
    boolean isEditable();
    boolean checkCardinality(DisabledExplainer b, int changeInCount);
    boolean doFilterRemovalWhenRefiltering();
    void adjustActiveFilters(Row row);
    boolean meetsCriteria(Row row);
    boolean describeCriteriaFailures(Row row, Collection<String> failureDescriptions);
    boolean describeSelectedCriteriaFailures(Row row, boolean isCriteriaFilterGroup, Collection<String> failureDescriptions);
    boolean refilter(Row row);
    boolean isPartOfPrimaryKey(int dataColumnIndex);
	boolean isTemporaryForPrimaryKeySelecting(Row row);
	void applyExternalFilters();
	boolean handleRowButtonClick(JButton button, TableCellContext context);
	Collection<Integer>getMustHideColumns();
	boolean isCaseSensitive(Row row, int dataColumnIndex);
    Object getRenderOnlyValueForLastRow(int dataColumnIndex, boolean isSelected, boolean hasFocus);
    void useHeaderLabelAsRenderOnlyValueForLastRow(int dataColumnIndex, String value);

}
