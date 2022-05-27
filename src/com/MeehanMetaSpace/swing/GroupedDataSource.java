

package com.MeehanMetaSpace.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.SearchAndReplace;
import com.MeehanMetaSpace.TimeKeeper;
import com.MeehanMetaSpace.swing.PersonalizableTableModel.Operation;
import com.MeehanMetaSpace.swing.PersonalizableTableModel.NodeSelectStyle;
import com.MeehanMetaSpace.swing.PersonalizableTableModel.PopupMenuItem;

public class GroupedDataSource extends DefaultPersonalizableDataSource {
    private final ArrayList ungroupListeners = new ArrayList();
    private DisabledExplainer alterSortSequenceDisabled;
    
    class MyMouseListener extends MouseAdapter {
        private Cursor cu=tree.getCursor();
        private int draggedDataColumnIndex=-1,draggedVisualColumnIndex=-1;
        public void mouseEntered(final MouseEvent e) {
        	if (groupSeivedModel.table.maybeColumnsAreMoving){
        		tree.setCursor(DragSource.DefaultMoveDrop);
        		draggedDataColumnIndex=groupSeivedModel.clickedDataColumnIndex;
        		draggedVisualColumnIndex=groupSeivedModel.clickedVisualColumnIndex;
        	}
        }

        public void mouseExited(final MouseEvent e) {
        	tree.setCursor(cu);
			draggedVisualColumnIndex = draggedDataColumnIndex = -1;
        }

        public void mousePressed(final MouseEvent event) { // or maybe mouseClicked()?
            if (event.isPopupTrigger()) {                
                popup(event);
            }
        }

        private void popup(final MouseEvent event) {
        	if (delete != null){
        		delete.operationStarted(false);
        	}
        	if (remove != null){
        		remove.operationStarted(false);
        	}
			treeMouseEvent = event;
			ungroupedModel.notifyTreePopupMenuListeners(customMenuItems);
			final Node node = ungroupedModel.getMouseOverNode();
			if (alterSortSequenceDisabled != null) {
				ungroupedModel
						.setSortSequenceMenuText(
								alterSortSequenceDisabled,
								node == null || node.sortIndexThatDiffers == -1 ? -1
										: sortInfo[node.sortIndexThatDiffers].dataColumnIndex);
			}
			seeAllItem.setVisible(ungroupedModel.hasSeeOnlySettings());
			queryFavoriteColumns.setText(ungroupedModel.getQueryFavoriteText());
			popup.show(event.getComponent(), event.getX(), event.getY());
		}
        
        public void mouseReleased(final MouseEvent event) {
            if (event.isPopupTrigger()) {
            	popup(event);
            } else
            if (event.getClickCount() == 2) {
            	if (tree.getPathForLocation(event.getX(), event.getY()) == null) {
                 	//If the double click did not happen on the node 
                 	return;
                }
                treeMouseEvent = event;               
                if (ungroupedModel.isInTreeBuildingMode) {
                    int dc = 0;
                    final Node node = ungroupedModel.getMouseOverNode();
                    if (node != null) {
                        dc = sortInfo[node.sortIndexThatDiffers].dataColumnIndex;
                        ungroupedModel.adjustTree(dc, true);
                    }
                } else if (ungroupedModel.treeDoubleClickUsesUngroupedTable) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ungroupedModel.handleDoubleClick(ungroupedModel.
                              getFirstSelectedRow(true), true);
                        }
                    });

                }
            }
        }
        
        void handleDraggingIfNecessary() {
			if (draggedDataColumnIndex >= 0) {
				tree.setCursor(cu);
				groupSeivedModel.table.moveColumn(0, groupSeivedModel.table.columnListener.start);
				remember=seeOnlyTokens;
				ungroupedModel.adjustTree(draggedDataColumnIndex, true);
				draggedVisualColumnIndex = draggedDataColumnIndex = -1;
				
			}
		}
    }
    
    MyMouseListener myMouseListener;



    public void addUngroupListener(final UngroupListener ungroupListener) {
        ungroupListeners.add(ungroupListener);
    }

    public void removeUngroupListener(final UngroupListener ungroupListener) {
        ungroupListeners.remove(ungroupListener);
    }

    public interface UngroupListener {
        public void ungrouping();
    }


    final boolean hasGUI;

    public boolean isCreatable() {
        return false;
    }

    public boolean isDeletable() {
        return false;
    }

    public boolean isRemovable() {
        return false;
    }

    public boolean isAddable() {
        return false;
    }

    public boolean isSaveable() {
        return false;
    }

    public class MetaRow extends AbstractMetaRow {
        final com.MeehanMetaSpace.swing.MetaRow sourceMetaRow;
        final ColumnComparator cc;

        public Icon getIcon(
          final Iterator selectedRows,
          final int dataColumn,
          final boolean isExpanded,
          final boolean isLeaf) {
            return null;
        }
        
    	public Integer getSpecialHorizontalAlignment(final int dataColumnIndex){
    		return null;
    	}

        public SortValueReinterpreter getSortValueReinterpreter(final int
          dataColumnIndex) {
            return sourceMetaRow.getSortValueReinterpreter(
              getSourceDataColumnIndex(
                dataColumnIndex));
        }

        public MetaRow() {
            super(null);
            sourceMetaRow = groupSeivedModel==null?ungroupedModel.metaRow:groupSeivedModel.metaRow;
            final SortInfo[] si = new SortInfo[sortInfo.length];
            for (int i = 0; i < si.length; i++) {
                si[i] = new SortInfo(ungroupedModel.metaRow, i, sortInfo[i].sortOrder);
                si[i].ascending = sortInfo[i].ascending;
                si[i].sequence = sortInfo[i].sequence;
            }
            cc = new ColumnComparator(si);
            for (int i = 0; i < si.length; i++) {
                final String name = sourceMetaRow.
                                    getDataColumnIdentifier(sortInfo[i].
                  dataColumnIndex);
                dataColumnIdentifiers.add(name);
                if (hasGUI) {
                    final int dividerLocation = groupSeivedModel.getProperty(
                      PersonalizableTableModel.PROPERTY_DIVIDER_LOCATION, -1);
                    if (dividerLocation == -1) {
                        if (ungroupedModel.columnsForGroupSeive != null) {
                            groupSeivedModel.initModelColumns(ungroupedModel.
                              columnsForGroupSeive);
                        } else {
                        	final List<String>l=groupSeivedModel.getModelColumnIdentifiers();
                            if (l.size()>si.length){                            	
                                groupSeivedModel.initModelColumns(l);                                
                            } else {
                            	groupSeivedModel.initModelColumns(ungroupedModel.
                            			getSortedAndVisibleDataColumns());
                            }
                        }
                    }
                    boolean seiveFreely = ungroupedModel.getProperty(
                      PersonalizableTableModel.PROPERTY_SEIVE_SORT_FREELY, true);
                    if (!seiveFreely) {
                        final SortInfo[] osi = ungroupedModel.getAllSortInfo();
                        if (!Basics.startsWith(groupSeivedModel.getAllSortInfo(),
                                               osi)) {
                            groupSeivedModel.sort(osi);
                            groupSeivedModel.sort();
                        }
                    }
                }
            }
        }

        public Class getClass(final int dataColumnIndex) {
            return sourceMetaRow.getClass(sortInfo[dataColumnIndex].
                                          dataColumnIndex);
        }

        public class Row implements com.MeehanMetaSpace.swing.Row, Comparable<Row> {
            
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
                return null;
            }
            
            public Icon getIcon(final int dataColumnIndex) {
                return null;
            }

            public void endImport() {
            }

            public String toString() {
                final StringBuilder sb = new StringBuilder("(");
                sb.append(firstUngroupedRowIndex);
                sb.append(") ");
                for (int i = 0; i < sortInfo.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(get(i));
                }
                return sb.toString();
            }

            public final int firstUngroupedRowIndex;
            public final com.MeehanMetaSpace.swing.Row firstUngroupedRow;

            public com.MeehanMetaSpace.swing.MetaRow getMetaRow() {
                return MetaRow.this;
            }

            MetaRow getGroupedMetaRow() {
                return MetaRow.this;
            }

            public boolean isDeletable() {
                return false;
            }

            private String getLabel(final int dataColumnIndex) {
                return groupSeivedModel.getColumnLabel(sortInfo[dataColumnIndex].
                  dataColumnIndex);
            }

            public Collection getUnselectableValues(final int dataColumnIndex) {
            	return Basics.UNMODIFIABLE_EMPTY_LIST;
        	}

            public Collection getAllowedValues(final int dataColumnIndex) {
            	return Basics.UNMODIFIABLE_EMPTY_LIST;
        	}

            public boolean allowNewValue(final int columnDataIndex) {
                return false;
            }

            private Row(final int ungroupedRowIndex) {
                this.firstUngroupedRowIndex = ungroupedRowIndex;
                this.firstUngroupedRow = (com.MeehanMetaSpace.swing.Row)
                                         allUngroupedRows.get(ungroupedRowIndex);
            }

            public int getColumnCount() {
                return MetaRow.this.size();
            }

            public void set(final int dataColumnIndex, final Object element) {
                firstUngroupedRow.set(sortInfo[dataColumnIndex].dataColumnIndex,
                                      element);
            }
            
            int getDataColumnIndex(final int sortInfoIndex){
            	return sortInfo[sortInfoIndex].dataColumnIndex;
            }
            
            public String getString(final int sortInfoIndex) {
            	final Object o;
            	final int dataColumnIndex=sortInfo[sortInfoIndex].dataColumnIndex;
                if (firstUngroupedRow instanceof Row.GroupSensitive) {
                    o=((Row.GroupSensitive) firstUngroupedRow).get(
                      ungroupedDataSource, dataColumnIndex);
                } else {
                    o=firstUngroupedRow.get(dataColumnIndex);
                }
                return PersonalizableTableModel.toSequenceableString(o, ungroupedModel.useRenderOnlyValueForTree ?
                		firstUngroupedRow.getRenderOnlyValue( dataColumnIndex, false, false):null);
            }


            public Object get(final int sortInfoIndex) {
                if (firstUngroupedRow instanceof Row.GroupSensitive) {
                    return ((Row.GroupSensitive) firstUngroupedRow).get(
                      ungroupedDataSource, sortInfo[sortInfoIndex].
                      dataColumnIndex);
                } else {
                    return firstUngroupedRow.get(sortInfo[sortInfoIndex].
                                                 dataColumnIndex);
                }
            }

            public boolean isEditable(final int dataColumnIndex) {
                return firstUngroupedRow.isEditable(sortInfo[dataColumnIndex].
                  dataColumnIndex);
            }

            public boolean isExplorable(final int dataColumnIndex) {
                return firstUngroupedRow.isExplorable(sortInfo[dataColumnIndex].
                  dataColumnIndex);
            }

            public void explore(final int dataColumnIndex) {
                firstUngroupedRow.explore(sortInfo[dataColumnIndex].
                                          dataColumnIndex);
            }

            public boolean setAdvice(final int dataColumnIndex,
                                     final CellAdvice cellAdvice) {
                return firstUngroupedRow.setAdvice(sortInfo[dataColumnIndex].
                  dataColumnIndex, cellAdvice);
            }

            public int hashCode() {
                int result = 17;

                for (int i = 0; i < sortInfo.length; i++) {
                    int n;
                    final Object value=sortInfo[i].getSortValue(firstUngroupedRow);
                    if (value==null){
                    	n = 0;
                    } else {
                    	n=value.hashCode();
                    }
                    result = 37 * result + n;
                }
                return result;
            }

            public boolean equals(final Row row, final int lastIdx) {
                for (int i = 0; i < lastIdx; i++) {
                    final Object left = get(i), right = row.get(i);
                    if (!Basics.equals(left, right)) {
                        return false;
                    }
                }
                return true;
            }

            public boolean equals(final Object o) {
                final Row row = (Row) o;
                for (int i = 0; i < sortInfo.length; i++) {
                	final Object left=sortInfo[i].getSortValue(firstUngroupedRow), right=sortInfo[i].getSortValue(row.firstUngroupedRow);
                	
                    if (left == null || right == null) {
                        if (left != null || right != null) {
                            return false;
                        }
                    } else if (!left.equals(right)) {
                        return false;
                    }
                }
                return true;
            }

            private int getDifferingColumn(final Row row) {
                for (int i = 0; i < sortInfo.length; i++) {
                	final Object o1=sortInfo[i].getSortValue(firstUngroupedRow), o4=sortInfo[i].getSortValue(row.firstUngroupedRow);
                	
                    if (!Basics.equals(o1,o4)) {
                        return i;
                    }
                }
                return -1;
            }

            boolean isFiltered(final com.MeehanMetaSpace.swing.Row row, final int sortIndexThatDiffers) {
            	if (row instanceof Row){
            		return equals(row);
            	}
            	if (row == null){
            		return false;
            	}
                for (int i = 0; i <= sortIndexThatDiffers; i++) {
                	final Object o1=sortInfo[i].getSortValue(firstUngroupedRow), o4=sortInfo[i].getSortValue(row);
                	
                    if (!Basics.equals(o1,o4)) {
                        return false;
                    }
                }
                return true;
            }

            public void filter() {
                filter(sortInfo.length);
            }

            void filter(final int lastColumn) {
                if (groupSeivedModel.dataSource.isFilterable()) {
                    for (int i = 0; i < sortInfo.length; i++) {
                        groupSeivedModel.filter(sortInfo[i].dataColumnIndex,
                                                i <= lastColumn ? get(i) : null,
                                                Filterable.opEquals);
                    }
                    groupSeivedModel.syncFilter(true);
                    refreshSizeInfo();
                    groupSeivedModel.table.clearSelection();
                }
            }

            public int compareTo(final Row other) {
                return cc.compare(this, other);
            }
    		public Object getFilterableValue(final int sortInfoIndex) {
                    if (firstUngroupedRow instanceof Row.GroupSensitive) {
                        return ((Row.GroupSensitive) firstUngroupedRow).get(
                          ungroupedDataSource, sortInfo[sortInfoIndex].
                          dataColumnIndex);
                    } else {
                        return firstUngroupedRow.getFilterableValue(sortInfo[sortInfoIndex].
                                                     dataColumnIndex);
                    }
                

    		}

			public Collection getForbiddenValues(int dataColumnIndex) {
				return Basics.UNMODIFIABLE_EMPTY_LIST;
			}

			public boolean shouldAllowedValuesBeSorted(int dataColumnIndex) {
				return true;
			}

        }
    }


    public int getSourceDataColumnIndex(final int idx) {
        return sortInfo[idx].dataColumnIndex;
    }

    final static int relationTypeSibling = 0, relationTypeDirectParent = 1,
    relationTypeChild = -1, relationTypeGrandChild = -2;

    final java.util.List<Row> allUngroupedRows;
    final int ungroupedRowCount;
    final PersonalizableTableModel groupSeivedModel;
    final PersonalizableTableModel ungroupedModel;
    private final PersonalizableDataSource ungroupedDataSource;
    SortInfo sortInfo[];
    int []tableColumns;

    public boolean isSortedBy(final int[] dataColumnIndexes) {
        if (dataColumnIndexes != null &&
            dataColumnIndexes.length <= sortInfo.length) {
            for (int i = 0; i < dataColumnIndexes.length; i++) {
                if (sortInfo[i].dataColumnIndex != dataColumnIndexes[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    CanEditPriorPicks priorPickEditor;
    private Node lastNode;

    private void buildTree() {
        if (hasApplicationSpecificTreeSort &&
            ungroupedModel.dataSource instanceof CanEditPriorPicks) {
            priorPickEditor = (CanEditPriorPicks) ungroupedModel.dataSource;
        }
        final Node[] levels = new Node[sortInfo.length];
        root = new Node(null, levels);
        if (dataRows.size() > 0) {
            for (Node node= root; true;
               node = new Node(lastNode, levels)) {
                if (!node.isEndOfTree()){
                    lastNode=node;
                } else {
                    break;
                }
            }
        }
    }
    
    private Timer doubleClickTimer;
    private boolean needDoubleClickHelpMessage;
    private final JMenu recentTreeMenu = new JMenu(), favoriteTreeMenu=new JMenu();
    public GroupedDataSource(
      final PersonalizableTableModel groupSeivedModel,
      final PersonalizableTableModel ungroupedModel,
      final Properties properties,
      final boolean paintNow) {
        ungroupedModel.recentTrees.addCurrentTree(true);
        allUngroupedRows = ungroupedModel.dataSource.getFilteredDataRows();
        ungroupedRowCount = allUngroupedRows.size();

        this.groupSeivedModel = groupSeivedModel;
        ungroupedDataSource = ungroupedModel.getDataSource();
        this.ungroupedModel = ungroupedModel;
        if (ungroupedModel.readOnly){
            canEdit=false;
            canCreate=false;
        }else{
            canEdit = ungroupedModel.dataSource.canEditFromTreeContext() && ungroupedModel.dataSource.isEditable();
            canCreate = ungroupedModel.dataSource.isCreatable();
        }
        this.sortInfo = ungroupedModel.getAllSortInfo();
        this.tableColumns=PersonalizableTableModel.getSort(sortInfo);
        if (sortInfo.length==0){
        	if (ungroupedModel.buildTreeFromDropDownListOfColumns){
            PersonalizableTableModel.setTreeBuildingMode(groupSeivedModel, ungroupedModel, true);
        } else {
        	SwingUtilities.invokeLater(new Runnable(){
        		
        	public void run(){
        	tree.setToolTipText(Basics.toHtmlUncentered("Build by drag & drop", "Drag and drop columns from the <br>table to the right "));
        	showToolTipLater(root,false,false);
        	}});
        }
    }
        hasGUI = (ungroupedModel.table != null);
        if (hasGUI) {
        	final PersonalizableTable table=new PersonalizableTable(groupSeivedModel, this);
        	table.setAnticipateHtml(ungroupedModel.table.anticipateHtml);
        	table.setBackgroundImage(ungroupedModel.table.backgroundImage, ungroupedModel.table.columnsThatMustBeOpaque);
        	if (table.refreshBackgroundImage != null){
        		table.treatTheBackgroundImageAsModifyingAdvice();
        	}
            groupSeivedModel.table.specialFontDataColumnIndex =ungroupedModel.table.specialFontDataColumnIndex;
            groupSeivedModel.table.nestedTableFactories.addAll(ungroupedModel.
              table.
              nestedTableFactories);
            //groupSeivedModel.table.headerLines = ungroupedModel.table.headerLines;
            groupSeivedModel.table.getSelectionModel().setSelectionMode(ungroupedModel.table.getSelectionModel().getSelectionMode());
    }
        TimeKeeper tk=new TimeKeeper();
        tk.reset("Building the tree");
        tk.announce("building GroupedRow rows");
        if (OPTIMIZE){
        	buildRowsFast();
        }else{
        	buildRows();
        }
        tk.announce("Rows built ... nodes next");
            hasApplicationSpecificTreeSort = isSortedBy(
              ungroupedModel.applicationSpecificTreeSort);
            allowCollapsingToRetainHiddenSelection=hasApplicationSpecificTreeSort;
            buildTree();
            tk.announce("Nodes done ... GUI next");
            if (hasGUI && paintNow) {
                doTheGUI(getTreeComponent(), ungroupedModel.verticalSplit,
                         "Tree: " + ungroupedModel.getCurrentSort());
                if (!FocusFreeze.isFrozen()) {
                	tree.requestFocus();
                }
            }
            tk.announce("GUI done ... table models next");
            
        if (groupSeivedModel != null){
        syncTableModels();
        SwingUtilities.invokeLater(new Runnable(){    		
        	public void run(){
        		refreshSizeInfo();
        	}
        });
        }
        tk.stop();
    }

    int currentGroupedRow = -1;

    public void filterOutAllRows() {
        groupSeivedModel.setEmptyView();
    }

    private void syncTableModels() {
        groupSeivedModel.key = ungroupedModel.key;
        ungroupedModel.group(this, groupSeivedModel);
        groupSeivedModel.group(this, groupSeivedModel);
        groupSeivedModel.addActions();
        //groupSeivedModel.editInPlace = ungroupedModel.editInPlace;
        groupSeivedModel.allowEditInPlaceControl = ungroupedModel.
          allowEditInPlaceControl;
    }

    private void buildRowsFast(){
        final MetaRow metaRow = new MetaRow();
        final int n=allUngroupedRows.size();
        final Collection<MetaRow.Row> extinguishDuplicates = new HashSet<MetaRow.Row>(n);
        for (int i = 0; i < n; i++) {
            extinguishDuplicates.add(metaRow.new Row(i)); // duplicates extinguished by hashCode 
        }
        final ArrayList l=new ArrayList(extinguishDuplicates);
        Collections.sort(l);
		setDataRows(l);
        setMetaRow(metaRow);
    }
    
    
	private void buildRows() {
		final MetaRow metaRow = new MetaRow();
		final Collection<Row> extinguishDuplicates = new TreeSet<Row>();
		for (int i = 0; i < allUngroupedRows.size(); i++) {
			final MetaRow.Row r = metaRow.new Row(i);
			extinguishDuplicates.add(r); // duplicates extinguished by tree set
		}
		copyRowReferences(extinguishDuplicates);
		setMetaRow(metaRow);
	}

    JSplitPane splitPane;

    public PersonalizableTableModel getUngroupedTableModel() {
        return ungroupedModel;
    }
    public void ungroup() {
    	seeOnlyTokens=null;
        ungroup(true);
    }

    SeeOnlyTokens seeOnlyTokens;
    void ungroup(final boolean saveProperties) {
        if (hasGUI) {
            ToolTipOnDemand.getSingleton().hideTipWindow();
            if (ungroupedModel.getDataSource().getFilteringContext()!= PersonalizableDataSource.FILTER_CONTEXT_QUERY_CONCLUSION){
            	ungroupedModel.getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_UNGROUPING);
            }
            ungroupedModel.syncFilter(true);
            ungroupedModel.removeActiveFiltered(false);
            ungroupedModel.getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_NORMAL);
            PersonalizableTable.isAddingAncestor = true;
            if (ungroupedModel.table.scrollPane == null) {
                SwingBasics.switchContaineesWithinContainer(splitPane, ungroupedModel.table);
            } else if (splitPane != null){
            	SwingBasics.switchContaineesWithinContainer(splitPane,
                                       ungroupedModel.table.scrollPane);
            }
            PersonalizableTable.isAddingAncestor = false;
            if (saveProperties){
                ungroupedModel.updatePropertiesWithPersonalizations(false);
            }
            ungroupedModel.setGroupOption(PersonalizableTableModel.NO_GROUPING);
            _dispose();
        }
    }

    private void _dispose() {
        ungroupedModel.group(null, null);
        for (final Iterator it = ungroupListeners.iterator(); it.hasNext(); ) {
            ((UngroupListener) it.next()).ungrouping();
        }
        if (!FocusFreeze.isFrozen() && !ungroupedModel.isInTreeBuildingMode) {
            ungroupedModel.table.requestFocus();
        }
        // allow garbage collector to work
        if (tree != null) {
            ungroupedModel.recentTrees.unregisterActions(tree, recentTreeMenu);
            ungroupedModel.favoriteTrees.unregisterActions(tree, favoriteTreeMenu);
        }
        dispose();
    }

    void dispose(){
        	if (splitPane != null){
            // encourage easy memory profiling by OptimizeIt
        		splitPane.removeAll();
        	}
            splitPane = null;
            ungroupedModel.treePickHandler = null;
            // allow garbage collector to work
            if (tree != null) {
                tree=null;
            }
            if (popup != null) {
                popup.removeAll();
                popup=null;
            }
            purgeDoubleClickTimer();
            purgeMatchTimer();
    }

    private void purgeDoubleClickTimer() {
		if (doubleClickTimer != null) {
			doubleClickTimer.cancel();
			doubleClickTimer.purge();
			doubleClickTimer=null;
		}
    }
    
    static final boolean DOING_MATCH_TIMER=false;
    private void purgeMatchTimer() {
    	if (matchTimer != null) {
    		matchTimer.cancel();
    		matchTimer.purge();
    		matchTimer=null;
    	}
    }
    
    public boolean handleDoubleClick(final TableCellContext context) {
        MetaRow.Row r = (MetaRow.Row) context.row;
        if (r != null) {
            return ungroupedModel.dataSource.handleDoubleClick(
            		new TableCellContext(context.tableModel, context.dataColumnIndex, r.firstUngroupedRow));
        }
        return false;
    }

    public static GroupedDataSource activate(
      final PersonalizableTableModel ungroupedModel,
      final int viewOption,
      final boolean paintNow) {
        return activate(ungroupedModel, viewOption, paintNow,
                        ungroupedModel.getSeivedPrefix(), true);
    }


    public static GroupedDataSource activate(
      final PersonalizableTableModel ungroupedModel,
      final int viewOption) {
        String s = ungroupedModel.getSeivedPrefix();
        return activate(ungroupedModel, viewOption, true, s);
    }
    public static GroupedDataSource activate(
    	      final PersonalizableTableModel ungroupedModel,
    	      final int groupOption,
    	      final boolean paintNow,
    	      final String seivedPropertyPrefix) {
    	return activate(ungroupedModel, groupOption, paintNow, seivedPropertyPrefix, false);
    }

    private static GroupedDataSource activate(
      final PersonalizableTableModel ungroupedModel,
      final int groupOption,
      final boolean paintNow,
      final String seivedPropertyPrefix,
      final boolean ignoreCompanionTable) {
        final Properties properties =
          ungroupedModel.updatePropertiesWithPersonalizations(false);
        DefaultPersonalizableDataSource dataSource = new DefaultPersonalizableDataSource(ungroupedModel.dataSource, !paintNow);
        dataSource.setHiddenFilter(ungroupedModel.dataSource.getHiddenFilter());
        dataSource.setHiddenRows(ungroupedModel.dataSource.getHiddenRows());
        final PersonalizableTableModel groupSeivedModel;
        if (ignoreCompanionTable){
        	groupSeivedModel=null;
        } else {
        groupSeivedModel=
          PersonalizableTableModel.activateCompanionTable(
        		  dataSource, ungroupedModel.readOnly);
        groupSeivedModel.firstTimeAncestorWasAdded=false;
        groupSeivedModel.setIsPickList(ungroupedModel.isPickList);
        markPropertyPrefix(
          ungroupedModel,
          groupSeivedModel,
          properties,
          seivedPropertyPrefix);
        
        groupSeivedModel.setAutoFilter(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (groupSeivedModel != null && groupSeivedModel.table != null){
                    for (final int dataColumnIndex:ungroupedModel.dataSource.getMustHideColumns()){
                    	groupSeivedModel.hideColumn(dataColumnIndex);
                    }
                    groupSeivedModel.table.scrollToVisible(0, 0);
                } 
            }
        });
        }
        //groupSeivedModel.setFilterRows(false);
        return new GroupedDataSource(groupSeivedModel, ungroupedModel,
                                     properties, paintNow);
    }

    void printNodeRow(final Node node, final String id,
                      final java.io.PrintStream ps) {
        if (node != null) {
            int kiddies = node.getChildCount();
            if (node.groupedRow != null) {
                ps.print(id);
                ps.print(" [");
                ps.print(node.groupedRow.get(node.sortIndexThatDiffers));
                ps.println("]");
            } else {
                ps.println("root");
            }
            if (kiddies > 0) {
//				ps.println("; " +kiddies+" child nodes:");
                for (int i = 0; i < kiddies; i++) {
                    Node kiddy = (Node) node.getChildAt(i);
                    if (kiddy != null && node.groupedRow != null &&
                        !node.groupedRow.equals(kiddy.groupedRow,
                                                kiddy.sortIndexThatDiffers)) {
                        ps.println();
                        ps.println("PARENT/CHILD Anomaly???");
                        PersonalizableTableModel.print(kiddy.groupedRow, ps);
                        ps.println();
                    }
                    printNodeRow(kiddy, id + i + ".", ps);
                }
            } else {
//				System.out.println();
            }
        }
    }

    public void printTree(final java.io.PrintStream ps) {
        printNodeRow(root, "", ps);
    }

    private String title;

    private int dividerChanges = 0;
    private JPanel emptyTreePanel;
    void setTableSide(final int rowCnt){
        if (ungroupedModel.applicationSpecificTreeSort==null){
            final int div=splitPane.getDividerLocation();

            if (rowCnt>0 || ungroupedModel.dataSource.getFilteredDataRows().size()==0){
                final Component r=splitPane.getRightComponent();
                if (!Basics.equals(r, scrollSeived)){
                    splitPane.setRightComponent(scrollSeived);
                    GradientBasics.setTransparentChildren(scrollSeived, false);
                    splitPane.setDividerLocation(div);
                }
            } else {
                final Component r=splitPane.getRightComponent();
                if (!Basics.equals(r, emptyTreePanel)){
                    splitPane.setRightComponent(emptyTreePanel);
                    splitPane.setDividerLocation(div);
                }
            }
        }
    }
    private JScrollPane scrollSeived;
    public void doTheGUI(
      final JComponent leftSide,
      final boolean verticalSplit,
      final String title) {
        if (hasGUI) {
            this.title = title;
            scrollSeived = groupSeivedModel.table.
                                             makeHorizontalScrollPane();
            // refresh position after internal data members are fully set
            SwingUtilities.invokeLater(
            		new Runnable() {
            			public void run() {
            				groupSeivedModel.fireIfNoneSelected();
            			}
            		});
            splitPane = new JSplitPane(verticalSplit ?
                                       JSplitPane.VERTICAL_SPLIT :
                                       JSplitPane.HORIZONTAL_SPLIT, true,
                                       leftSide,
                                       null);
            emptyTreePanel=new JPanel();
            emptyTreePanel.addMouseListener(new MouseAdapter(){
                public void mouseReleased(final MouseEvent event) {
                    groupSeivedModel.isMouseEventWithEmptyLabel=true;
                    groupSeivedModel.handleMouseEvent(event, true);
                    groupSeivedModel.isMouseEventWithEmptyLabel=false;
                }

                public void mousePressed(final MouseEvent event) {
                    groupSeivedModel.isMouseEventWithEmptyLabel=true;
                    groupSeivedModel.handleMouseEvent(event, false);
                    groupSeivedModel.isMouseEventWithEmptyLabel=false;

                }
            })
            ;
            emptyTreePanel.add(ungroupedModel.emptyTreeLabel.get());
            setTableSide(groupSeivedModel.dataSource.size());
            if (ungroupedModel.applicationSpecificTreeSort != null){
                splitPane.setDividerSize(1);
            }
            if (ungroupedModel.squeezeCondensedRatio != 0.0) {
                // Get toolkit
                final Toolkit toolkit = Toolkit.getDefaultToolkit(); // Get size
                final Dimension dimension = toolkit.getScreenSize();
                if (verticalSplit) {
                    dimension.width *= ungroupedModel.squeezeCondensedRatio;
                    if (dimension.width > ungroupedModel.squeezeCondensedMax) {
                        dimension.width = ungroupedModel.squeezeCondensedMax;
                    }
                    dimension.height /= 10;
                    dimension.height *= 6;
                    leftSide.setPreferredSize(dimension);
                    dimension.height /= 2;
                    scrollSeived.setPreferredSize(dimension);
                } else {
                    dimension.height *= ungroupedModel.squeezeCondensedRatio;
                    if (dimension.height > ungroupedModel.squeezeCondensedMax) {
                        dimension.height = ungroupedModel.squeezeCondensedMax;
                    }
                    dimension.width /= 3;
                    dimension.width *= 2;
                    leftSide.setPreferredSize(dimension);
                    dimension.width /= 2;
                    scrollSeived.setPreferredSize(dimension);

                }
                splitPane.setResizeWeight(0.85);
            }
            splitPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            int dividerLocation = groupSeivedModel.getProperty(
              PersonalizableTableModel.PROPERTY_DIVIDER_LOCATION, -1);
            if (dividerLocation != -1) {
                splitPane.setDividerLocation(dividerLocation);
            } else {
                splitPane.setDividerLocation(150);
            }
            final BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.
              getUI()).
                                                  getDivider();
            divider.addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    int dividerLocation = splitPane.getDividerLocation();
                    groupSeivedModel.setProperty(PersonalizableTableModel.
                                                 PROPERTY_DIVIDER_LOCATION,
                                                 dividerLocation);
                    dividerChanges++;
                }
            });
            GradientBasics.setTransparentChildren(splitPane, false);
            PersonalizableTable.isAddingAncestor = true;
            if (ungroupedModel.table.scrollPane == null) {
                SwingBasics.switchContaineesWithinContainer(ungroupedModel.table, splitPane);
            } else {
                SwingBasics.switchContaineesWithinContainer(ungroupedModel.table.scrollPane,
                                       splitPane);
            }
            PersonalizableTable.isAddingAncestor = false;
            

        }
    }

    public SortInfo[] getSortInfo() {
        return sortInfo;
    }

    public int[] getSortUngroupedDataColumnIndexes() {
        return PersonalizableTableModel.getSort(sortInfo);
    }

    private Node root;

    public Node getRoot() {
        return root;
    }

    public class Node extends DefaultMutableTreeNode {
        public final int sortIndexThatDiffers, rowIndex;
        public final MetaRow.Row groupedRow;
        
        boolean isBeingSearched() {
        	return seeOnlyTokens != null && sortIndexThatDiffers>=0 && seeOnlyTokens.isDataColumnIndexSeen(sortInfo[sortIndexThatDiffers].dataColumnIndex);
        }

        boolean isFound() {
        	return sortIndexThatDiffers>=0 && 
        	seeOnlyTokens.isDataColumnIndexSeen(sortInfo[sortIndexThatDiffers].dataColumnIndex) && 
        	groupedRow != null &&
        	seeOnlyTokens.matches(groupedRow.firstUngroupedRow);
        }

        void setFromContext(final Row newRow, final Set<Integer> forbidden){
			final int[] si = getSortUngroupedIndexes();
			if (groupedRow != null) {// not root
				final Row contextRow = groupedRow.firstUngroupedRow;
				for (int i = 0; i <= sortIndexThatDiffers; i++) {
					final int dataColumnIndex=si[i];
					if (!ungroupedModel.dataSource.isPartOfPrimaryKey(dataColumnIndex) && (forbidden==null||!forbidden.contains(dataColumnIndex))) {
						final Object o = contextRow.getFilterableValue(dataColumnIndex);
						newRow.set(dataColumnIndex, o);
					}
				}
			}
        }
        
        String getColumnLabel(){
        	final int[] si = getSortUngroupedIndexes();
			if (groupedRow != null) {// not root
				final int dataColumnIndex=si[sortIndexThatDiffers];
				return ungroupedModel.getColumnLabel(dataColumnIndex);
			}
			return null;
        }
        Object get(final int dataColumnIndex){
        	Object value=null;
			final int[] si = getSortUngroupedIndexes();
			if (groupedRow != null) {// not root
				final Row contextRow = groupedRow.firstUngroupedRow;
				for (int i = 0; i <= sortIndexThatDiffers; i++) {
					if (si[i]==dataColumnIndex) {
						value = contextRow.getFilterableValue(si[i]);
						break;
					}
				}
			}
			return value;
        }


        int getUngroupedColumnThatDiffers() {
            return getSourceDataColumnIndex(sortIndexThatDiffers);
        }

        public int[] getSortUngroupedIndexes() {
            final int[] r = new int[sortIndexThatDiffers + 1];
            for (int i = 0; i < r.length; i++) {
                r[i] = sortInfo[i].dataColumnIndex;
            }
            return r;
        }

        public GroupedDataSource getDataSource() {
            return GroupedDataSource.this;
        }

        public boolean isRoot() {
            return groupedRow == null;
        }

        public int getFirstUngroupedIndex() {
            return isRoot() ? 0 : groupedRow.firstUngroupedRowIndex;
        }

        public final class UngroupedChildRowIterator implements Iterator{
            private final int endUngroupedIdx;
            //private final java.util.List allUngroupedRows;
            private final int startUngroupedIdx;
            private int currentIdx;

            public int size() {
                return endUngroupedIdx - startUngroupedIdx;
            }

            UngroupedChildRowIterator() {
                if (isRoot()) {
                    startUngroupedIdx = 0;
                    endUngroupedIdx = allUngroupedRows.size();
                } else {
                    java.util.List allGroupedRows = GroupedDataSource.this.
                      getDataRows();
                    startUngroupedIdx = groupedRow.firstUngroupedRowIndex;
                    if (rowIndex == allGroupedRows.size() - 1) {
                        endUngroupedIdx = allUngroupedRows.size();
                    } else {
                        final Node nextNode = (Node) SwingBasics.
                                              getNextSiblingOrAncestralSibling(
                          GroupedDataSource.Node.this);
                        if (nextNode == null) {
                            endUngroupedIdx = allUngroupedRows.size();
                        } else {
                            endUngroupedIdx = nextNode.groupedRow.
                                              firstUngroupedRowIndex;
                        }
                    }
                }
                currentIdx = startUngroupedIdx;
            }

            public void remove() {
                throw new UnsupportedOperationException(
                  "Can not remove from tree");
            }

            public Object next() {
                return allUngroupedRows.size()>0&&allUngroupedRows.size()>=currentIdx && currentIdx < endUngroupedIdx ?
                  allUngroupedRows.get(currentIdx++) : null;
            }

            public boolean hasNext() {
                return currentIdx < endUngroupedIdx;
            }

            public void reset() {
                currentIdx = startUngroupedIdx;
            }
        }
        public Collection<Row>getChildRows(){
        	final Collection<Row>rows=new ArrayList<Row>();
        	for (final Iterator it = ungroupedChildRowIterator(); it.hasNext(); ) {
                rows.add((Row) it.next());
        	}
        	return rows;
        }
        public UngroupedChildRowIterator ungroupedChildRowIterator() {
            return new UngroupedChildRowIterator();
        }
        
        public Row getFirstUngroupedRow() {
            final java.util.List rows = ungroupedModel.dataSource.
                                        getFilteredDataRows();
            final int n=rows.size();
            if (n>0 && (isRoot()||groupedRow.firstUngroupedRowIndex<n)) {
                return (Row) rows.get(isRoot() ? 0 :
                                      groupedRow.firstUngroupedRowIndex);
            }
            System.err.print("Node=");
            System.err.print(toString());
            System.err.print(";table size=");
            System.err.print(n);
            System.err.print(" rows; firstUngroupedRow=");
            System.err.print(groupedRow == null ? null:groupedRow.firstUngroupedRowIndex);
            System.err.print(" isRoot()=");
            System.err.print(isRoot());
            System.err.println("????");
            return null;
        }
        
        String getString(){
        	return groupedRow.getString(sortIndexThatDiffers);
        }
        
        int getDataColumnIndex(){
        	return groupedRow.getDataColumnIndex(sortIndexThatDiffers);
        }

        public String toString() {
            if (groupedRow != null) {
            	return getString();
            }
            return super.toString();
        }

        String getToolTip() {
            if (isRoot()) {
                return null;
            }
            final int udci = sortInfo[sortIndexThatDiffers].dataColumnIndex;
            return ungroupedModel.getNodeEnabledText(this, udci);
        }

        public String getAnomaly(final boolean useCache) {
            if (sortIndexThatDiffers >= 0) {
                if (ungroupedModel.singleSelectForDescendentOfColumn >= 0) {
                    final Node p = getAncestor(ungroupedModel.
                                               singleSelectForDescendentOfColumn);
                    if (p != null) {
                        final TreePath[] treePaths = tree.getSelectionPaths();
                        if (treePaths != null) {
                            for (int i = 0; i < treePaths.length; i++) {
                                final Node node = (Node) treePaths[i].
                                                  getLastPathComponent();
                                if (node != this) {
                                    if (p == node
                                        ||
                                        p ==
                                        node.getAncestor(ungroupedModel.
                                      singleSelectForDescendentOfColumn)) {
                                        return "Only 1 selection under " + p +
                                          "!!";
                                    }
                                }
                            }
                        }
                    }
                }
                if (ungroupedModel.useDisabling &&
                    hasApplicationSpecificTreeSort) {
                    return ungroupedModel.getDisabledText(this, useCache);
                }
            }
            return null;
        }

        Node getAncestor(final int column) {
            Node ancestor = null;
            if (this.sortIndexThatDiffers >= column) {
                ancestor = this;
                while (ancestor != null &&
                       ancestor.sortIndexThatDiffers != column) {
                    ancestor = (Node) ancestor.getParent();
                }
            }
            return ancestor;
        }
        final PersonalizableTable getTable(){
        	return groupSeivedModel.table;
        }
        Node(final Node lastNode, final Node[] levels) {
            if (lastNode == null) {
                groupedRow = null;
                sortIndexThatDiffers = -1;
                rowIndex = 0;
                super.setUserObject("root");
            } else {
                if (lastNode.sortIndexThatDiffers == sortInfo.length - 1) {
                    rowIndex = lastNode.rowIndex + 1;
                } else {
                    rowIndex = lastNode.rowIndex;
                }
                if (rowIndex == size()) {
                    groupedRow = null;
                    sortIndexThatDiffers = sortInfo.length;
                } else {
                    groupedRow = (MetaRow.Row) dataRows.get(rowIndex);
                    /*					if (groupedRow.firstUngroupedRowIndex < lastNode.getFirstUngroupedIndex()){
                                  String here=this.toString(), prior=lastNode == null ? "null" : lastNode.toString();
                     System.out.println("debug anomaly, prior node idx < current");
                             }
                     */if (lastNode.isRoot()) {
                        sortIndexThatDiffers = 0;
                    } else {
                        if (rowIndex == lastNode.rowIndex) {
                            sortIndexThatDiffers = lastNode.
                              sortIndexThatDiffers + 1;
                        } else {
                            sortIndexThatDiffers = groupedRow.
                              getDifferingColumn(lastNode.
                                                 groupedRow);
                        }
                    }
                    levels[sortIndexThatDiffers].add(this);
                    //super.setUserObject(row.getLabel(columnThatDiffers)+"="+row.get(columnThatDiffers));
                }
            }
            if (sortIndexThatDiffers < sortInfo.length - 1) {
                levels[sortIndexThatDiffers + 1] = this;
            }

        }

        Icon getSpecialIcon(final boolean expanded, final boolean isLeaf) {
            if (groupedRow != null) {
                final Icon icon = SwingBasics.getIcon(
                  expanded,
                  isLeaf,
                  ungroupedModel.metaRow,
                  ungroupedChildRowIterator(),
                  sortInfo[sortIndexThatDiffers].dataColumnIndex);
                return icon;
            }
            return null;
        }

        void resetPick(final ArrayList<Object> priorPicks) {
            if (groupedRow != null) {
                if (priorPickEditor.wasNodePriorPick(this,
                  groupedRow.firstUngroupedRow,
                  ungroupedModel.
                  applicationSpecificTreeSort,
                  sortIndexThatDiffers)
                  ) {
                    priorPicks.add(this);
                }
            }
        }

        public TreePath getPathFromRoot() {
        	TreePath fromRoot = null;
            if (fromRoot == null) {
                TreeNode[] os = new TreeNode[sortIndexThatDiffers + 2];
                os[0] = root;
                os[sortIndexThatDiffers + 1] = this;
                for (int j = sortIndexThatDiffers; j > 0; j--) {
                    os[j] = os[j + 1].getParent();
                }
                fromRoot = new TreePath(os);
            }
            return fromRoot;
        }

        boolean isEndOfTree() {
            return groupedRow == null &&
              sortIndexThatDiffers == sortInfo.length;
        }
    }


    public void reallyRepaintTree() {
        if (tree != null) {
    /*        final DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
            dtm.reload();
      */      tree.repaint();
            tree.revalidate();
        }

    }

    void updateUI() {
        if (tree != null) {
            SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (tree != null) {
						tree.setUI(myUI);
						reallyRepaintTree();
					}

				}
			});
        }
    }

// If expand is true, expands all nodes in the tree.
// Otherwise, collapses all nodes in the tree.
    public int resetPicks() {
        resettingPicks = true;
        int n = 0;
        currentFoundPath=null;
        tree.removeTreeSelectionListener(treeSelectionListener);
        tree.clearSelection();
        if (priorPickEditor != null) {

            ungroupedModel.treePickHandler.treePickHistory = new ArrayList();
            ungroupedModel.treePickHandler.undone = -1;
            if (ungroupedModel.treeSupportsUndo) {
                ungroupedModel.treePickHandler.setDo(undoItem, redoItem);
            }
            final ArrayList picks = new ArrayList();
            final Node root = (Node) tree.getModel().getRoot();

            // Traverse tree from root
            resetPicks(new TreePath(root), picks);
            n = picks.size();
            if (n == 0) {
                tree.setSelectionPath(new TreePath(root));
            } else {
                for (final Iterator it = picks.iterator(); it.hasNext(); ) {
                    final Node node = (Node) it.next();
                    tree.addSelectionPath(node.getPathFromRoot());
                    tree.expandPath(((Node) node.getParent()).getPathFromRoot());
                }
            }

            if (ungroupedModel.treePickHandler != null &&
                ungroupedModel.treeSupportsUndo) {
                ungroupedModel.treePickHandler.recordUserSelections(tree.
                  getSelectionPaths());
            }
            tree.addTreeSelectionListener(treeSelectionListener);

        } else {
            tree.addTreeSelectionListener(treeSelectionListener);
            if (ungroupedModel.isInTreeBuildingMode){
                pickRootLater();
            } else {
                switch (ungroupedModel.autoSelectTreeRoot) {
                case SELECT_ROOT_NODE_AND_SHOW_ALL_ROWS:
                    pickRootLater();
                    break;
                case SELECT_NO_NODE_AND_SHOW_NO_ROWS:
                    ((DefaultPersonalizableDataSource) groupSeivedModel.dataSource).
                    copyRowReferences(Basics.UNMODIFIABLE_EMPTY_LIST);
                    break;
                case SELECT_NO_NODE_BUT_SHOW_ALL_ROWS:
                    // table side of split pane already set in doTheGui()
                }
            }
        }

        tree.repaint();
        tree.revalidate();
        resettingPicks = false;
        return n;
    }
    private boolean firstTime=true;

    public void pickRootLater() {
        SwingUtilities.invokeLater(
          new Runnable() {
            public void run() {
            	pickRoot();
            }
        });
    }
    
    public void pickRoot(){
        if (tree != null && splitPane != null) { // not disposed?
            tree.setSelectionPath(new TreePath(root));
        }

    }

    boolean resettingPicks = false;

    private void resetPicks(final TreePath parent, final ArrayList<Object> picks) {
        // Traverse children
        final Node node = (Node) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                final Node n = (Node) e.nextElement();
                final TreePath path = parent.pathByAddingChild(n);
                resetPicks(path, picks);
            }
        }
        node.resetPick(picks);
    }

    private static void markPropertyPrefix(
      final PersonalizableTableModel ungroupedModel,
      final PersonalizableTableModel groupSeivedModel,
      final Properties properties,
      final String prefix) {
        groupSeivedModel._propertyPrefix = prefix;
        groupSeivedModel.ungroupedModel=ungroupedModel;
        final String property=groupSeivedModel.getPropertyName(PersonalizableTableModel.PROPERTY_SAME_TREE_SHAPE);
        boolean wasPreviouslyGrouped = PersonalizableTableModel.getProperty(
          properties,
          property,
          false);
        if (wasPreviouslyGrouped) {// use previous GROUPED table properties ?
            groupSeivedModel.setProperties(properties);
        } else {// use current UNGROUPED table properties
            groupSeivedModel._propertyPrefix = null;
            groupSeivedModel.ungroupedModel=null;
            final int oldGroupOption = groupSeivedModel.groupOption;
            groupSeivedModel.setProperties(properties);
            if (ungroupedModel.getTabbedDataColumnIndex()>=0){
            	groupSeivedModel.dataSource.setPropertyPrefix(ungroupedModel.dataSource.getPropertyPrefix());
            	groupSeivedModel.initSortInfo();
            	groupSeivedModel.dataSource.setPropertyPrefix(null);
            }
            groupSeivedModel.groupOption = oldGroupOption;
            groupSeivedModel._propertyPrefix = prefix;
            groupSeivedModel.ungroupedModel=ungroupedModel;
    		final List<String>l=groupSeivedModel.getModelColumnIdentifiers();
            groupSeivedModel.initModelColumns(l);
            groupSeivedModel.saveEditInPlaceSetting();
        }
        if (!ungroupedModel.allowEditInPlaceControl){
        	groupSeivedModel.editInPlace=ungroupedModel.editInPlace;
        }
        groupSeivedModel.showUrlText=ungroupedModel.showUrlText;
        PersonalizableTableModel.setProperty(properties, property,true);
    }


    JTree tree;

    boolean hasApplicationSpecificTreeSort;
    ArrayList priorPicks = new ArrayList();

    JMenuItem undoItem, redoItem;

    //final int rowHeight = 21;
    //Collection userSelected=new ArrayList();
    private TreeSelectionListener treeSelectionListener;
    TreePath[] oldSelections = new TreePath[0];
    TreePath worthyAnchor = null;

    private MouseEvent lastMouseEvent;
    private boolean adjustingSelections = false;
    boolean supressSelections(final boolean ok){
        final boolean prev=adjustingSelections;
        adjustingSelections=ok;
        return prev;
    }

    private boolean isToggleSelectionEvent() {
        return lastMouseEvent == null ? false :
          SwingBasics.isToggleSelectionEvent(lastMouseEvent);
    }

    private boolean isMultiSelectionEvent() {
        return lastMouseEvent == null ? false :
          SwingBasics.isMultiSelectionEvent(lastMouseEvent);
    }
    private boolean initializingAutoComplete=false;
    private boolean sizeFrozen = false;
    private JComboBox ac;
    private AutoComplete.FoundListener treeSync;
    JScrollPane scrollPane;
    JPanel searchNodePanel, findPanel, treePanel;
    private JComponent getTreeComponent() {
        treeSync=new AutoComplete.FoundListener() {
            public void completionFound(final Object nextSelection) {
                if (!initializingAutoComplete){
                    if (nextSelection != null) {
                        searchArgument = nextSelection.toString();
                        foundPaths = GroupedDataSource.this.resolveSearchResults(
                          findNodeEquals(searchArgument), false);
                        setFindNext();
                    } else {
                        foundPaths = null;
                        searchArgument = null;
                        setFindNext();
                    }
                }
            }
        };
        ac = PersonalizableTableModel.getReadOnlyComboBox(Basics.UNMODIFIABLE_EMPTY_LIST, null, treeSync, 150);
        scrollPane = initTree();
        treePanel = new JPanel(new BorderLayout());
        treePanel.add(scrollPane, BorderLayout.CENTER);
        if (ungroupedModel.showToolBarPanelInTree) {
        	 JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
             final JButton wrenchButton = SwingBasics.getButton(null);
             wrenchButton.addMouseListener(new MouseAdapter() {
         		public void mousePressed(MouseEvent e) {
         			groupSeivedModel.popupMenu(wrenchButton, e.getX(), e.getY()+(wrenchButton.getHeight()/2));
         		}
         	});
             wrenchButton.setBorder(null);
             wrenchButton.setBorderPainted(false);
             wrenchButton.setMargin(new Insets(0,0,0,0));
             wrenchButton.setContentAreaFilled(false);
             wrenchButton.setText("");
             wrenchButton.setToolTipText("Click to see the context menu");
             wrenchButton.setIcon(MmsIcons.getWrenchIcon());
             JButton closeButton = SwingBasics.getButton(new ActionListener() {
             	public void actionPerformed(ActionEvent e) {
             		ungroupedModel.ungroupIfNecessary();
             	}
             });
             closeButton.setText("");
             closeButton.setBorder(null);
             closeButton.setBorderPainted(false);
             closeButton.setMargin(new Insets(0,0,0,0));
             closeButton.setContentAreaFilled(false);
             closeButton.setToolTipText("Click to close this Tree view");
             closeButton.setIcon(MmsIcons.getNoRedIcon());
             northPanel.add(wrenchButton);
             northPanel.add(closeButton);
             treePanel.add(northPanel, BorderLayout.SOUTH);
        }
       
        setFindPanel(true);
        setSearchNodePanel(true);
        setExpandState(true);
        ComboCellEditor.addFocusListener(ac,new FocusAdapter() {
            public void focusLost(final FocusEvent fe) {
                ac.hidePopup();
            }
        });
        findAgainButton.setEnabled(false);
        return treePanel;
    }

    private Collection nodesWithPartialChildSelections = new ArrayList();
    private void setAutoCompleteItems(){
        initializingAutoComplete = true;
        SwingBasics.setItems(ac, getAutoCompleteItems());
        ac.setSelectedItem(null);
        initializingAutoComplete = false;
    }
    private void findAgain() {
    	if (foundPaths != null){
            currentFindIdx = (currentFindIdx + 1) % foundPaths.length;
            currentFoundPath = foundPaths[currentFindIdx];
            lettersTypedByUser="";
            lettersFoundFromTyped="";
            tree.scrollPathToVisible(currentFoundPath);
            setFindNext();
            tree.repaint();
        }
    }
    
    public void search(final String arg) {
		if (!isSearchNodePanelShowing()) {
			setSearchNodePanel(false);
		}
		if (Basics.equals(searchArgument, arg)) {
			findAgain();
		} else {
			ac.setSelectedItem(arg);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
			treeSync.completionFound(arg);
				}
			});
		}
	}

    public Collection getAutoCompleteItems() {
        final TreeNode node = (TreeNode) tree.getModel().getRoot();
        final TreeSet c = new TreeSet();
        gatherAutoCompleteItems(node, c);
        return c;
    }

    private void gatherAutoCompleteItems(final TreeNode node,
                                         final Collection c) {
        if (node.getChildCount() >= 0) {
            for (final Enumeration e = node.children(); e.hasMoreElements(); ) {
                final TreeNode _node = (TreeNode) e.nextElement();
                c.add(_node.toString());
                gatherAutoCompleteItems(_node, c);
            }
        }
    }

    private TreePath[] findNodeEquals(final String nodeName) {
        final TreeNode node = (TreeNode) tree.getModel().getRoot();
        final ArrayList al = new ArrayList();
        findNodeEquals(node, nodeName, al);
        return (TreePath[]) al.toArray(new TreePath[al.size()]);

    }

    boolean allowCollapsingToRetainHiddenSelection=false;

    private void findNodeEquals(final TreeNode node, final String nodeName,
                                final Collection c) {
        final String s = node.toString();
        if (s.equals(nodeName)) {
            c.add(((Node) node).getPathFromRoot());
        }

        // Traverse children
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                findNodeEquals((Node) e.nextElement(), nodeName, c);
            }
        }
    }

    final Collection<TreePath> selectedButCollapsed=new ArrayList<TreePath>();
    TreePath selectedByCollapsing=null;
    boolean isPathAdded = false;
    private HashMap<Integer,Integer> treePathCount = new HashMap<Integer,Integer>();
    //private TreePath currentlyCollapsing;
    private  JScrollPane initTree() {
        tree = new JTree(root) {
			public void scrollPathToVisible(final TreePath path) {
				if (path != null) {
					makeVisible(path);
					Rectangle bounds = getPathBounds(path);
					if (bounds != null) {
						final int n = path.getPathCount();
						final int adjust=n*11;
						bounds.x -= (adjust);
						bounds.y -= (adjust)-10;
						bounds.height+=adjust+22;
						bounds.width+=adjust+22;
						scrollRectToVisible(bounds);
						if (accessibleContext != null) {
							((AccessibleJTree) accessibleContext)
									.fireVisibleDataPropertyChange();
						}
					}
				}
			}

		    public void collapsePath(final TreePath path) {
		        final boolean old = adjustingSelections;
                adjustingSelections=true;
                super.collapsePath(path);
                getSelectionPaths();
                adjustingSelections=old;
		    }
		    
            public TreePath[] getSelectionPaths() {
                TreePath[] tp = super.getSelectionPaths();
                if (allowCollapsingToRetainHiddenSelection) {
                    final boolean old = adjustingSelections;
                    if (selectedButCollapsed.size() > 0) {
                        adjustingSelections = true;
                        for (final TreePath t : selectedButCollapsed) {
                            super.addSelectionPath(t);
                        }
                        adjustingSelections = old;
                        selectedButCollapsed.clear();
                    }
                    if (selectedByCollapsing != null) {
                        adjustingSelections = true;
                    	super.removeSelectionPath(selectedByCollapsing);
                        adjustingSelections = old;
                    }
                    tp = super.getSelectionPaths();
                }
                return tp;
            }

            public void addSelectionPath(final TreePath path) {
            	op = SelectionOp.ADD;
				if (saveForLater && (selectedByCollapsing == null || !path.equals(selectedByCollapsing))) {
					savedSelectionPath = path;					
				} else {
					if (allowCollapsingToRetainHiddenSelection && selectedByCollapsing != null) {
						if (path.equals(selectedByCollapsing)) {
							/*
							 * prevent selection of collapsed node per Jeff
							 * Croissant's requirements this is dangerous
							 * behavior dependent on the implementation of
							 * JTree.setExpandedState() in JDK 1.5
							 */
							selectedByCollapsing = null;
							return;
						}
					}
					isPathAdded = true;
					super.addSelectionPath(path);
					isPathAdded = false;
				}
			}

            public void fireTreeWillCollapse(final TreePath path)
                          throws ExpandVetoException{
                            if (allowCollapsingToRetainHiddenSelection) {
                                boolean wasSelected = false;
                                final TreePath[] tp = super.getSelectionPaths();
                                if (tp != null){
                                    for (final TreePath p : tp) {
                                        if (path.equals(p)) {
                                            wasSelected = true;
                                            break;
                                        }
                                        if (path.isDescendant(p)) {
                                            selectedButCollapsed.add(p);
                                            System.out.println("preserving " + p.toString());
                                        }
                                    }
                                }
                                if (!wasSelected) {
                                    selectedByCollapsing = path;
                                }
                            }
                            super.fireTreeWillCollapse(path);
                        }
            
        	
        	
        	
            public void setSelectionPath(final TreePath path) {
    			op=SelectionOp.SET;
        		if (saveForLater) {
        			savedSelectionPath = path;
        		} else {
        			super.setSelectionPath(path);
        		}
        	}

            public void removeSelectionPath(final TreePath path) {
    			op=SelectionOp.REMOVE;
        		if (saveForLater) {
        			savedSelectionPath = path;
        		} else {
                	super.getSelectionModel().removeSelectionPath(path);
        		}
            }

			protected void processMouseEvent(final MouseEvent e) {
				final Point point = e.getPoint();
				final TreePath path = getPathForLocation(point.x, point.y);
				boolean ignoreMouseEvent=false;
				if (path != null) {
					ungroupedModel.mostRecentlyClickedNode = (GroupedDataSource.Node) path
							.getLastPathComponent();
				} else {
					ungroupedModel.mostRecentlyClickedNode = null;
				}
				selectedByCollapsing=null;
				final int id=e.getID();
				if (ungroupedModel.nodeSelectStyle == NodeSelectStyle.ON_DOUBLE_CLICK) {
					if (id == MouseEvent.MOUSE_RELEASED && SwingUtilities.isLeftMouseButton(e)) {
						saveForLater=false;
					}
					else if (id == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(e)) {
						if (e.getClickCount() < 2) {
							needDoubleClickHelpMessage = true;
							saveForLater = true;
							purgeDoubleClickTimer();
				    		doubleClickTimer=new Timer();
							doubleClickTimer.schedule(new TimerTask() {
								public void run() {
									if (!PersonalizableTableModel.ignoreNextTreeTimerToolTip){
									try {
										SwingUtilities
												.invokeAndWait(new Runnable() {

													public void run() {
														final boolean isDragging=dragAndDrop !=null && dragAndDrop.draggedPath != null ;
														if (needDoubleClickHelpMessage && !isDragging) {
															saveForLater=false;
															String txt;
															if (dragAndDrop == null) {
																txt="<html><table><tr><td><ul><li>Double click to select/unselect a node.<li>Type any letters to search.</ul></td></tr></table></html>";
															} else {
																txt="<html><table><tr><td><ul><li>Double click to select/unselect a node.<li>Hold button down to drag.<li>Type any letters to search.</ul></td></tr></table></html>";
															}
															showTextWithoutCloseButton(
																	ungroupedModel.mostRecentlyClickedNode,
																	txt,
																	false);
														}
													}
												});
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (InvocationTargetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}else{
									PersonalizableTableModel.ignoreNextTreeTimerToolTip=false;
								}
								}
							}, 750);
						} else {
							needDoubleClickHelpMessage = false;
							saveForLater = false;
							if (savedSelectionPath != null) {
								boolean select = true;
								if (ungroupedModel.mostRecentlyClickedNode != null) {
									GroupedDataSource.Node n = (GroupedDataSource.Node) path
											.getLastPathComponent();
									final String a = n.getAnomaly(true);
									if (a != null) {
										final TreePath pr=n.getPathFromRoot();
										if (!Basics.contains(getSelectionPaths(), pr) ) {
											showDisabledText(n, a, true);
											select = false;
											ignoreMouseEvent=true;
										}  else if (ungroupedModel.ignoreSelections!=null && ungroupedModel.ignoreSelections.contains(pr)) {
											showText(n, ungroupedModel.ignoreSelectionAnomaly, true);
											select = false;
											ignoreMouseEvent=true;
										}

									}
								}
								if (select) {
									if (op == SelectionOp.SET) {
										setSelectionPath(savedSelectionPath);
									} else if (op == SelectionOp.ADD) {
										addSelectionPath(savedSelectionPath);
									} else if (op == SelectionOp.REMOVE) {// in
										// case
										// of
										// toggle
										// selection
										removeSelectionPath(savedSelectionPath);
									}
								}
								savedSelectionPath = null;
							}
						}
					}
				} else if (ungroupedModel.nodeSelectStyle == NodeSelectStyle.ON_MOUSE_RELEASE) {
					if (id == MouseEvent.MOUSE_PRESSED) {
						saveForLater = true;
					} else if (id == MouseEvent.MOUSE_RELEASED) {
						saveForLater = false;
						if (savedSelectionPath != null) {
							boolean select = true;
							if (ungroupedModel.mostRecentlyClickedNode != null) {
								GroupedDataSource.Node n = (GroupedDataSource.Node) path
										.getLastPathComponent();
								final String a = n.getAnomaly(true);
								if (a != null) {
									final TreePath pr=n.getPathFromRoot();
									if (!Basics.contains(getSelectionPaths(), pr)) {
										showDisabledText(n, a, true);
										select = false;
									}  else if (ungroupedModel.ignoreSelections!=null && ungroupedModel.ignoreSelections.contains(pr)) {
										showText(n, ungroupedModel.ignoreSelectionAnomaly);
										select = false;
									}
								}
							}
							if (select) {
								if (op == SelectionOp.SET) {
									setSelectionPath(savedSelectionPath);
								} else if (op == SelectionOp.ADD) {
									addSelectionPath(savedSelectionPath);
								} else if (op == SelectionOp.REMOVE) {// in
									// case
									// of
									// toggle
									// selection
									removeSelectionPath(savedSelectionPath);
								}
							}
							savedSelectionPath = null;
						}
					}
				} else if (ungroupedModel.nodeSelectStyle == NodeSelectStyle.ONLY_BY_DRAGGING){
					if (id == MouseEvent.MOUSE_PRESSED) {
						saveForLater = true;
					} else if (id == MouseEvent.MOUSE_RELEASED && !e.isPopupTrigger()) {
						savedSelectionPath = null;
						saveForLater = false;
						String txt="<html><table><tr><td><ul><li>Selecting is <b><i>only</i></b> by dragging.<li>Type any letters to search.</ul></td></tr></table></html>";

						showTextWithoutCloseButton(
								ungroupedModel.mostRecentlyClickedNode,
								txt,
								false);

					}

				}

				op = SelectionOp.NONE;
				lastMouseEvent = e;
				// System.out.println(isMultiSelectEvent(e) ? "debug: contiguous
				// select occuring":"");
				if (!ignoreMouseEvent) {
					super.processMouseEvent(e);
				}
			}

            public void setAnchorSelectionPath(TreePath newPath) {
                //System.out.println("Setting anchor to " + newPath +", new anchor="+worthyAnchor);
                if (getRowForPath(newPath) < 0 && worthyAnchor != null) {
                    newPath = worthyAnchor;
                }
                super.setAnchorSelectionPath(newPath);
            }


        };
        for (final MouseListener ml:ungroupedModel.treeMouseListeners){
        	tree.addMouseListener(ml);
        }
        if (ungroupedModel.table != null){
            final TransferHandler th=ungroupedModel.table.getTransferHandler();                
            if (th instanceof RowDragAndDrop.Draggable){
                dragAndDrop=new TreeDragAndDrop();
            }
        }
        if (ungroupedModel.selectionsPropagateToLeaf || hasApplicationSpecificTreeSort) {
            tree.setExpandsSelectedPaths(false);
        }

       if (ungroupedModel.isAutoTreeResizeNeeded) {
    	   tree.addTreeExpansionListener(new TreeExpansionListener() {
       	    public void treeExpanded(TreeExpansionEvent event) {
       	    	final int count = event.getPath().getPathCount();
       	    	final Integer oldCount = treePathCount.get(count);
       	    	if (oldCount != null && oldCount > 0) {
       	    		treePathCount.put(count, oldCount + 1);    	    		
       	    	}
       	    	else {
       	    		treePathCount.put(count, 1);
       	    		if (splitPane != null) {
       	    		splitPane.setDividerLocation(splitPane.getDividerLocation() + 50);
       	    		}
       	    	}    	    	
       	    }
       	    
       	    public void treeCollapsed(TreeExpansionEvent event) {
       	    	final int count = event.getPath().getPathCount();
       	    	final Integer oldCount = treePathCount.get(count);
       	    	if (oldCount != null && oldCount > 1) {
       	    		treePathCount.put(count, oldCount - 1);   
       	    	}
       	    	else {
       	    		treePathCount.remove(count);
          	    		splitPane.setDividerLocation(splitPane.getDividerLocation() - 50);
       	    	}
       	    }
          });
       }
       
        
        treeSelectionListener = new TreeSelectionListener() {

            /* If a node or leaf is selected, display
             the attributes of that entry. */
            public void valueChanged(final TreeSelectionEvent e) {
            	if (adjustingSelections) {
                    return;
                }
                currentFoundPath=null;
                lettersTypedByUser="";
                lettersFoundFromTyped="";
                if (ungroupedModel.selectionsPropagateToLeaf) {

                    //debugSelections(e);

// The BIG problem to solve on April 19/20 2004 swas handling SHIFT click multi-select
// behaviors which are peculiar when we have set tree.setExpandsSelectedPaths(false);
// NOTE: see line 2169 in BasicTreeUI.java to understand shift click reasoning
                    TreePath[] mouseClickSelections = tree.getSelectionPaths();
                    if (mouseClickSelections == null) {
                        mouseClickSelections = new TreePath[0];
                    }
                    final Set computedSelections = Basics.toSet(
                      mouseClickSelections);
                    final Collection removedSelections, addedSelections;

                    worthyAnchor = tree.getAnchorSelectionPath();
                    final TreePath[] eventTreePaths = e.getPaths();
                    if (mouseClickSelections.length == 1) {
                        removedSelections = new ArrayList();
                        addedSelections = Basics.toList(mouseClickSelections);
                    } else {
                        removedSelections = new ArrayList();
                        addedSelections = new ArrayList();

                        for (int i = 0; i < eventTreePaths.length; i++) {
                            if (e.isAddedPath(eventTreePaths[i])) {
                                addedSelections.add(eventTreePaths[i]);
                            } else {
                                if (tree.getRowForPath(eventTreePaths[i]) >= 0
                                  //&& !eventTreePaths[i].equals(anchor)
                                  ) {
                                    //BasicTreeUI interpretation of a non visual removal is incorrect
                                    removedSelections.add(eventTreePaths[i]);

                                } else {
                                    //computedSelections.add(eventTreePaths[i]);
                                }
                            }
                        }
                    }

                    if (Basics.equalsAny(eventTreePaths, worthyAnchor) &&
                        !e.isAddedPath(worthyAnchor)) {
                        worthyAnchor = null;
                        for (int i = 0; i < eventTreePaths.length; i++) {
                            if (e.isAddedPath(eventTreePaths[i])) {
                                worthyAnchor = eventTreePaths[i];
                                break;
                            }
                        }
                    }
                    if ((worthyAnchor == null ||
                         !Basics.equalsAny(mouseClickSelections, worthyAnchor))
                        && mouseClickSelections.length > 0) {
                        worthyAnchor = mouseClickSelections[0];
                    }

                    if (isToggleSelectionEvent()) {
                        tree.repaint();
                    }

                    /*		  System.out.println(
                                                  mouseClickSelections.length
                                                  + " MOUSE selections = "
                     + Basics.toString(mouseClickSelections));
                                          System.out.println(
                                                  removedSelections.size()
                                                  + " REMOVED selections = "
                     + Basics.toString(removedSelections));
                                          System.out.println(
                                                  addedSelections.size()
                                                  + " ADDED selections = "
                     + Basics.toString(addedSelections));
                     */
                    // do not remove removed if it is a child of added
                    SwingBasics.removeOrphans(addedSelections,
                                              removedSelections);

                    final DefaultTreeModel model = (DefaultTreeModel) tree.
                      getModel();
                    SwingBasics.addAllChildIfNoneInBucket(model,
                      addedSelections,
                      computedSelections);
                    if (isMultiSelectionEvent()) {
                        SwingBasics.removeIfAnyChildNotInBucket(model,
                          addedSelections,
                          computedSelections);
                    }
                    SwingBasics.removeAllChildren(
                      model, removedSelections, computedSelections);
                    SwingBasics.removeParent(removedSelections,
                                             computedSelections);
                    SwingBasics.addParentIfAllChildrenInBucket(model,
                      addedSelections,
                      computedSelections);
                    adjustingSelections = true;
                    final int n = computedSelections.size();
                    final TreePath[] currentSelections = (TreePath[])
                      computedSelections.
                      toArray(new TreePath[n]);

//		  System.out.println(n + " COMPUTED selections");

                    tree.setSelectionPaths(currentSelections);
                    oldSelections = currentSelections;
                    nodesWithPartialChildSelections =
                      SwingBasics.getNodesWithSomeKidsInBucket(
                        model,
                        computedSelections,
                        computedSelections);

                    /*		  System.out.println(
                     nodesWithPartialChildSelections.size()
                     + " nodes with partial child selections:  "
                     + Basics.toString(nodesWithPartialChildSelections));
                     */
                    if (worthyAnchor != null) {
                        //debugAnchor();
                        //System.out.println("Resetting anchor to " + worthyAnchor.toString());
                        tree.setAnchorSelectionPath(worthyAnchor);
                    }
                    adjustingSelections = false;

                    //debugAnchor();
                }
                //metaRow.show(treePath==null?0:entry.sortIndexThatDiffers);
                final Collection rowsForTreeSelections;
                if (ungroupedModel.columnsForGroupSeive != null &&
                    ungroupedModel.distinctRowsForGroupSeive) {
                    rowsForTreeSelections = Distinct.compute(
                      ungroupedModel.metaRow,
                      ungroupedModel.columnsForGroupSeive,
                      ungroupedModel.getSelectionsInDescendingOrder());
                } else {
                	final int []rs=tree.getSelectionRows();
                	if(rs!=null&&rs.length==1 && rs[0]==0){
                		// root .. avoid copies
                		rowsForTreeSelections=ungroupedModel.dataSource.getFilteredDataRows();
                	} else {
                    rowsForTreeSelections = ungroupedModel.
                                            getSelectionsInDescendingOrder();
                }
                }
                final TreePath treePath = e.getNewLeadSelectionPath();
                final Node entry = treePath == null ? null :
                                   (Node) treePath.getLastPathComponent();

                groupSeivedModel.table.stopCellEditing();
                ((DefaultPersonalizableDataSource) groupSeivedModel.dataSource).
                  copyRowReferences(rowsForTreeSelections);
                setTableSide(rowsForTreeSelections.size());
                final boolean was=ungroupedModel.isUserPicking;
                ungroupedModel.isUserPicking=false;
                if (!hasApplicationSpecificTreeSort) {
					groupSeivedModel.syncFilter(tree.getSelectionCount() > 1
							|| !Basics.equals(groupSeivedModel.getSort(),
									ungroupedModel.getSort()) && !isPathAdded);
					refreshSizeInfo();
					groupSeivedModel.table.clearSelection();
				}
                if (ungroupedModel.autoSelectTableFromTree){
                    if (
                      (!ungroupedModel.isInTreeBuildingMode && ungroupedModel.autoSelectTreeRoot!=PersonalizableTableModel.TreeStartupNode.SELECT_ROOT_NODE_AND_SHOW_ALL_ROWS)
                        || !firstTime){
                        final int n = groupSeivedModel.dataSource.getFilteredDataRows().size();
                        if (n > 0) {
                            final int start=groupSeivedModel.getVisualRowIndex(0),
                                            cnt=groupSeivedModel.getVisualRowIndex(n-1);
                            int mode=groupSeivedModel.getTable().getSelectionModel().getSelectionMode();
                            if (mode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION){
                            	groupSeivedModel.table.setRowSelectionInterval(start, cnt);
                            } else {
                            	groupSeivedModel.table.setRowSelectionInterval(start, start);
                            }
                        }
                    } else {
                        firstTime=false;
                    }
                }
                ungroupedModel.isUserPicking=was;
                if (ungroupedModel.treePickHandler != null) {
                    if (firstSelection && !ungroupedModel.supressAnomalyPopupIfPicked){
                        firstSelection=false;
                        final String problemOfChoice=ungroupedModel.treePickHandler.notifySelection(
                          (GroupedDataSource.Node)entry);
                        if (e.isAddedPath() && problemOfChoice != null && !alreadyTooling){
                        	alreadyTooling=true;
                            final Rectangle rec=tree.getPathBounds(treePath);
							if (rec != null) {
								tree.setToolTipText(problemOfChoice);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										alreadyTooling = false;
										ToolTipOnDemand.getSingleton().show(
												tree, true, rec.x + rec.width,
												rec.y + rec.height, true);										
									}
								});
							}
                        }
                        firstSelection=true;
                    } else{
                        ungroupedModel.treePickHandler.notifySelection(null);
                    }
                    if (ungroupedModel.treeSupportsUndo) {
                        ungroupedModel.treePickHandler.setDo(undoItem, redoItem);
                    }
                    ungroupedModel.resetDisabledCache();
                    tree.repaint();
                }
                
                if (!groupSeivedModel.refreshIfCreatedInPlace() && !FocusFreeze.isFrozen() && !ungroupedModel.isInTreeBuildingMode) {
                	SwingUtilities.invokeLater(
            			new Runnable() {
            			public void run() {	
            				if (tree != null && !FocusFreeze.isFrozen())  {
            					tree.requestFocus();	                
            				}
            			}
            			}
                );
                }
            }
        };
        initRowHeight(1.8);
        if (hasApplicationSpecificTreeSort) {
            final TreeSelectionModel tm = tree.getSelectionModel();
            tm.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            ungroupedModel.treePickHandler = ungroupedModel.new PickHandler(
              tree);
        }
        tree.setUI(myUI);
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new Renderer());
        setTreePopupMenu();
        resetPicks();
        if (ungroupedModel.table.getDragEnabled()) {            
            groupSeivedModel.table.setDragAndDrop(ungroupedModel.table.
              getTransferHandler());
        }
        treeImage = sortInfo.length==0?MmsIcons.getDragColumnsIcon():ungroupedModel.treeBackgroundImage;
        return new JScrollPane(tree) {
            // Hard coded value. In your sub-class add a function for this.

            public void paint(final Graphics g) {
                // Now let the regular paint code do it's work
            	if (getViewport().getGraphics() != null) {
            		super.paint(g);
            	}
                // First draw the background image - tiled
                SwingBasics.draw(treeImage, g, this);
            }
        };
    }
    ImageIcon treeImage;
    boolean alreadyTooling=false;
    private void initRowHeight(final double p){
        final int rh1 = (int) (tree.getFont().getSize() * p);
        tree.setRowHeight(rh1);
    }


    private void refreshSizeInfo(){
        if (ungroupedModel.table != null && ungroupedModel.table.sizeInfo != null) {
            final String s=ungroupedModel.sizeInfo(
              groupSeivedModel.dataSource.getFilteredDataRows().size(),
              ungroupedModel.dataSource.getDataRows().size());
            ungroupedModel.table.sizeInfo.setText(s);
        }
    }

    private boolean firstSelection=true;
    public static boolean allowExpandingMenuItem=false;
    void debugAnchor() {
        final TreePath anchor = tree.getAnchorSelectionPath();
        if (anchor != null) {
            System.out.println("anchor=" + anchor.toString() + ", " +
                               tree.getRowForPath(anchor));
        }
    }

    int debugCall = 0;
    void debugSelections(final TreeSelectionEvent e) {
        System.out.println(debugCall + "-------------------------");
        debugAnchor();
        debugCall++;
        TreePath[] etp = e.getPaths();
        System.out.println(etp.length + " event paths");
        for (int i = 0; i < etp.length; i++) {
            System.out.println("\t" +
                               (e.isAddedPath(i) ? "adding " : "removing ") +
                               etp[i].toString());
        }
        TreePath[] mouseClickSelections = tree.getSelectionPaths();
        if (mouseClickSelections == null) {
            mouseClickSelections = new TreePath[0];
        }
        System.out.println(mouseClickSelections.length + " mouse selections = " +
                           Basics.toString(mouseClickSelections));

    }
    MouseEvent treeMouseEvent;
    final boolean canEdit, canCreate;
    private JPopupMenu popup = null;
    private JCheckBoxMenuItem treeMultiSelectWithKeyItem, treeShowCountsItem;
    private Collection<PersonalizableTableModel.PopupMenuItem> customMenuItems;
    final String seeAllText="See all";
    final JMenuItem seeAllItem= new JMenuItem(seeAllText, MmsIcons.getCancelIcon());
    private JMenuItem alterSortSequenceItem;
    private JMenuItem queryFavoriteColumns;
    private void setTreePopupMenu() {
    	final ActionListener seeAllAction=new ActionListener() {				
			public void actionPerformed(final ActionEvent e) {					
				ungroupedModel.releaseSeeOnlySettings();
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
		                ungroupedModel.groupedDataSource.tree.requestFocus();							
					}});
				}
			};
        final JComponent[] cc = new JComponent[] {
                                ac, tree, tfFind};
        popup = new JPopupMenu();
        customMenuItems = ungroupedModel.addMenuItemsToTree(tree, false,
                popup);
              for (final PopupMenuItem pmi:customMenuItems){
              	pmi.registerKeyboardAction(tfFind);
              }
            seeAllItem.addActionListener(seeAllAction);
            seeAllItem.setMnemonic('n');
            popup.add(seeAllItem);
        
			alterSortSequenceItem = new JMenuItem(
					PersonalizableTableModel.MENU_TEXT_SEQUENCE);
			alterSortSequenceItem.setIcon(MmsIcons.getSortCustomDown16Icon());
			alterSortSequenceDisabled = new DisabledExplainer(
					alterSortSequenceItem);

			SwingBasics.echoAction(cc, alterSortSequenceItem,
					new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							final GroupedDataSource.Node node = ungroupedModel
									.getMouseOverNode();
							if (node != null) {
								final int dc = sortInfo[node.sortIndexThatDiffers].dataColumnIndex;
								ungroupedModel.alterSortSequence(dc, tree);
								
							}
						}
					}

					, KeyStroke.getKeyStroke(KeyEvent.VK_Q,
							InputEvent.CTRL_MASK), 'q');
			popup.add(alterSortSequenceItem);
		
        if (customMenuItems.size() > 0) {
            popup.addSeparator();
        }
        if (canEdit || canCreate) {
            if (canEdit) {
                if (ungroupedModel.treeEditRules.size() > 0) {
                    final JMenu editMenu = new JMenu("Edit");
                    editMenu.setIcon(MmsIcons.getEditIcon());
                    for (final Iterator it = ungroupedModel.treeEditRules.
                                             iterator();
                                             it.hasNext(); ) {
                        final PersonalizableTableModel.MultiRowEditRule
                          treeEditRule =
                            (PersonalizableTableModel.MultiRowEditRule) it.next();
                        editMenu.add(treeEditRule.menuItem);
                    }
                    editMenu.setMnemonic('e');
                    popup.add(editMenu);
                } else {
                    final JMenuItem editItem = new JMenuItem("Edit");
                    editItem.setIcon(MmsIcons.getEditIcon());
                    SwingBasics.echoAction(
                      cc,
                      editItem,
                      new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            ungroupedModel.dataSource.editFromTreeContext(
                              ungroupedModel, null, -1);
                        }
                    }

                    ,
                    SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_E),
                      'e');
                    popup.add(editItem);
                }
            }
            if (canCreate && !ungroupedModel.useCustomNewItemOnly) {
                final JMenuItem newItem = new JMenuItem(ungroupedModel.getNewText());
                newItem.setIcon(MmsIcons.getNewIcon());
                SwingBasics.echoAction(
                  cc,
                  newItem,
                  new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        groupSeivedModel.newItem();
                    }
                }

                ,
                SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_N),
                  'n');
                popup.add(newItem);
            }
        	remove=groupSeivedModel.createRemoveOperation(tree, false);
            if (ungroupedModel.dataSource.isRemovable()) {
            	popup.add(remove.da.getMenuItem());
            }

            delete=groupSeivedModel.createDeleteOperation(tree, false);
            if (ungroupedModel.dataSource.isDeletable()
					&& !ungroupedModel.isFavoritesSelection()) {
            	popup.add(delete.da.getMenuItem());
            }
            if (ungroupedDataSource.isAddable() && ungroupedModel.canHavePickMenu){
                final JMenuItem newItem = new JMenuItem(ungroupedModel.getAddMenuText());
                SwingBasics.echoAction(
                  cc,
                  newItem,
                  new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        ungroupedModel.dataSource.add();
                    }
                }

                ,
                SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_L),
                  'l');
                popup.add(newItem);

            }

            popup.addSeparator();
        }

        if (PersonalizableTableModel.supportIgnoreInvalid &&
            ungroupedModel.dataSource instanceof PersonalizableDataSource.
            CanPick) {
            final JMenuItem useDisablingItem = new JMenuItem();
            ActionListener useDisablingAction = new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    ungroupedModel.useDisabling = !ungroupedModel.useDisabling;
                    tree.repaint();
                    ungroupedModel.notifyViewChanged();
                    useDisablingItem.setText(!ungroupedModel.useDisabling ?
                                             "Disable invalid" :
                                             "Ignore invalid");
                    groupSeivedModel.table.repaint();
                }
            };
            useDisablingItem.setText(!ungroupedModel.useDisabling ?
                                     "Disable invalid" :
                                     "Ignore invalid");
            popup.add(useDisablingItem);
            useDisablingItem.addActionListener(useDisablingAction);
            popup.addSeparator();
        }

        final JMenuItem ungroupItem = new JMenuItem();
        ungroupItem.setIcon(MmsIcons.getRestoreTableIcon());
        ungroupedModel.recentTrees.registerActions(tree, recentTreeMenu);
        ungroupedModel.favoriteTrees.registerActions(tree, favoriteTreeMenu);
        if ( Basics.isEmpty( ungroupedModel.applicationSpecificTreeSort)) {
            SwingBasics.echoAction(cc, null, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	ungroupedModel.buildTreeByDraggingAndDroppingColumns();
                }
            }
            , SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_T), 't');
            SwingBasics.echoAction(cc, null, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	ungroupedModel.buildTreeFromDropDownListOfColumns=true;
                    ungroupedModel.newTree();
                }
            }
            , SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_G), 'g');
            SwingBasics.echoAction(cc, ungroupItem, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	final Collection<Row> reselect=groupSeivedModel.getSelectedRowsInTable();
                	ungroup();
                    ungroupedModel.notifyViewChanged();
                    ungroupedModel.finishTreeBuilding();
                    if (reselect.size()>0){
                    	ungroupedModel.reselect(reselect, -1);
                    } else {
                    ungroupedModel.fireListSelection();
                    }
					// no FocusFreeze.isFrozen check necessary
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							ungroupedModel.	table.requestFocus();
						}
						
					});
                }
            }
            , SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_T, true), 't');

            popup.add(ungroupedModel.getViewMenu(tree));
        }
        customMenuItems.addAll(ungroupedModel.addMenuItemsToTree(tree, true, popup));

        tree.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                ungroupedModel.notifyTreePopupMenuListeners(customMenuItems);
            	popup.show(tree, tree.getX(), tree.getY());                	
            }
        }

        , KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK),
          JComponent.WHEN_FOCUSED);

        abstract class _Action implements SwingBasics.AutoCompleteActionListener{
        	public boolean isFound(){
        		return currentFoundPath != null;
        	}
        	
        	public void resetAsUnfound(){
        		currentFoundPath=null;
        	}
        };
        SwingBasics.registerAutoCompleteKeyboardAction(
        		tree,
        		new _Action(){
        	
        	public void actionPerformed(final ActionEvent ae){
        		if (currentFoundPath != null){
        			final FocusFreeze ff = new FocusFreeze();
					
        			if (!tree.isPathSelected(currentFoundPath)){
        				tree.setSelectionPath(currentFoundPath);
        				
        			} else {
        				tree.removeSelectionPath(currentFoundPath);
        			}
        			ff.thawLater(tree);
        		}
        	}
        }, new _Action(){
        	public void actionPerformed(final ActionEvent ae){
        		
        		if (currentFoundPath != null){
        			final FocusFreeze ff = new FocusFreeze();
					
        			if (!tree.isPathSelected(currentFoundPath)){
        				tree.addSelectionPath(currentFoundPath);
        				
        			} else {
        				tree.removeSelectionPath(currentFoundPath);
        			}
        			ff.thawLater(tree);
        		}
        	}
        }, new ActionListener(){
        	public void actionPerformed(final ActionEvent e){
        		autoCompleter.keyTyped(null, true);
        	}
        }, new ActionListener(){
        	public void actionPerformed(final ActionEvent e){
        		autoCompleter.keyTyped(null, false);
        	}
        },
        null,
        null,true
        );

        DisabledExplainer undoDisabled;
        DisabledExplainer redoDisabled;
        myMouseListener= new MyMouseListener();
        tree.addMouseListener(myMouseListener);
        tfFind.addMouseListener(myMouseListener);
        if (ungroupedModel.treeSupportsUndo) {
            popup.addSeparator();
            undoItem = new JMenuItem("Undo");
            undoDisabled = new DisabledExplainer(undoItem);
            popup.add(undoItem);
            undoItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ungroupedModel.treePickHandler.undoTree(undoItem, redoItem);
                }
            });
            undoDisabled.setEnabled(false,"Undo","This operation is ONLY enabled if you first perform operations");

            redoItem = new JMenuItem("Redo");
            redoDisabled = new DisabledExplainer(redoItem);
            popup.add(redoItem);
            redoItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ungroupedModel.treePickHandler.redoTree(undoItem, redoItem);
                }
            });
            redoDisabled.setEnabled(false,"Redo",PersonalizableTableModel.REDO_NO_CAN_DO);

        }


        treeShowCounts=PersonalizableTableModel.getTreeShowCounts();
        treeShowCountsItem = new JCheckBoxMenuItem(
          "View counts",treeShowCounts);
        treeShowCountsItem.setIcon(MmsIcons.getSumIcon());
        SwingBasics.echoAction(cc,treeShowCountsItem,new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                treeShowCountsItem.setState(PersonalizableTableModel.
                  setTreeShowCounts());
                treeShowCounts=PersonalizableTableModel.getTreeShowCounts();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (treeShowCounts){
                            initRowHeight(2.5);
                        }
                        tree.setUI(myUI);
                        reallyRepaintTree();
                    }
                });
            }
        }, SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_V), 'v');
        popup.add(treeShowCountsItem);
        
        final JMenu find=new JMenu("Find");
        find.setMnemonic('f');
        find.setIcon(MmsIcons.getFindIcon());
        popup.add(find);

		SwingBasics.echoAction(cc, findItem, findAction, SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_F), 'f');
        find.add(findItem);
        final JMenuItem seeAllItem = new JMenuItem(seeAllText, MmsIcons.getEyeIcon());
		SwingBasics.echoAction(cc, seeAllItem, seeAllAction, SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_F, true), 'f');
        find.add(seeAllItem);

        queryFavoriteColumns = new JMenuItem(ungroupedModel.getQueryFavoriteText(), MmsIcons.getHeart16Icon());
        
        SwingBasics.echoAction(cc, queryFavoriteColumns, 
		new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				ungroupedModel.limitTreeToContain(PersonalizableTableModel.QUERY_TYPE.FAVORITE);				
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK ), 'q');
		find.add(queryFavoriteColumns);
        final JMenuItem queryAllColumnsItem = new JMenuItem("Query all columns (find/see only)..", MmsIcons.getWorldSearchIcon());
        queryAllColumnsItem.setMnemonic('q');
        queryAllColumnsItem.addActionListener( 
		new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				ungroupedModel.limitTreeToContain(PersonalizableTableModel.QUERY_TYPE.ALL);				
			}
		});
		find.add(queryAllColumnsItem);


		SwingBasics.echoAction(cc, showSearchNodePanelItem, showSearchNodePanelAction, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), 'p');
        find.add(showSearchNodePanelItem);
        showSearchNodePanelItem.setIcon(MmsIcons.getSearchIcon());
        final JMenuItem enterPathDelimitedSearchStringItem = new JMenuItem("Enter path-delimited search string");
        enterPathDelimitedSearchStringItem.setIcon(MmsIcons.getFindIcon());
        find.add(enterPathDelimitedSearchStringItem); ;
        SwingBasics.echoAction(
          cc,
          enterPathDelimitedSearchStringItem,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchArgument = PopupBasics.getStringFromUser(tree,
                  "Enter values (use / to mark tree levels):",
                  "Search tree for..",
                  35);
                if (!Basics.isEmpty(searchArgument)) {
                    foundPaths = findInTree(searchArgument);
                    setFindNext();
                }
                tree.requestFocus();
            }
        }

        ,
          KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), 'e');

        find.add(findAgainMenuItem);
        SwingBasics.echoAction(cc, findAgainMenuItem, findAgainAction,
                               KeyStroke.
                               getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK),
                               'f');
        findAgainDisabled.setEnabled(false, "Find Again", "This operation is ONLY enabled if you first search");
       

        
        if (allowExpandingMenuItem && Basics.isEmpty( ungroupedModel.applicationSpecificTreeSort)){
            expandItem.addActionListener(expandAction);
            popup.add(expandItem);
        }
        treeMultiSelectWithKeyItem = new JCheckBoxMenuItem(
          "<html>Require " +
          (SwingBasics.usesMacMetaKey()? "apple" : "ctrl") +
          " key to select<br>multiples discontiguously</html>",
          ungroupedModel.TREE_MULTI_SELECT_WITH_KEY);
        treeMultiSelectWithKeyItem.setIcon(MmsIcons.getWideBlankIcon());
        treeMultiSelectWithKeyItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                treeMultiSelectWithKeyItem.setState(ungroupedModel.
                  setTreeMultiSelectWithKey());
            }
        });
        if (!ungroupedModel.allowOneClickMultiSelect){
            popup.add(treeMultiSelectWithKeyItem);
        }

        if ( Basics.isEmpty( ungroupedModel.applicationSpecificTreeSort)) {

              popup.add(ungroupedModel.createTreeMenu(
              null,
              null,
              ungroupItem,
              recentTreeMenu,
          favoriteTreeMenu));
        }

        //SwingBasics.setFontAllMenuElements(popup, PersonalizableTable.FONT_POPUP_MENU);
		if (ungroupedModel.supportsTearAway()) {
			ungroupedModel.tearAwayHandler.setTableModel(ungroupedModel);
			ungroupedModel.tearAwayHandler.echoAction(tree, null);
		}

    }

    private TreePath[] findInTree(final String searchArg) {
        return resolveSearchResults(SwingBasics.find(tree, searchArg), true);
    }

    TreePath[] findInTree(final String[] searchArgForEachTreeLevel, final boolean alertIfNotFound) {
        return resolveSearchResults(
          SwingBasics.findInSubtree(tree, searchArgForEachTreeLevel),
          alertIfNotFound);
    }

    private TreePath[] resolveSearchResults(
      final TreePath[] found,
      final boolean popupIfNotFound) {
        if (found.length > 0) {
            currentFindIdx = 0;
            currentFoundPath = (TreePath) found[0];
            lettersTypedByUser="";
            lettersFoundFromTyped="";
            if (ungroupedModel.scrollTreeIfFound){
            	tree.scrollPathToVisible(currentFoundPath);
            }
            tree.repaint();
        } else {
            currentFoundPath = null;
            if (popupIfNotFound) {
                PopupBasics.alert(Basics.toHtmlErrorUncentered("Not found!", "Change tree view/filter to locate."), true);
            }
        }
        return found;
    }

    int currentFindIdx;
    private String searchArgument;
    private TreePath[] foundPaths;
    private TreePath currentFoundPath=null;
    JButton nextFeasibleSelection;
    private JButton prevFeasibleSelection;
    final String nextFeasibleToolTip=Basics.toHtmlUncentered(
                "Next feasible selection",
                "Click this button to go to <br>the next feasible selection"),
    prevFeasibleToolTip=Basics.toHtmlUncentered(
                "Previous feasible selection",
                "Click this button to go to <br>the previous feasible selection"),
    nextFeasibleError=Basics.toHtmlErrorUncentered(
      "No feasible choice",
      "You can not select any choice in the tree<br>that is feasible... try and see");

    private void setFeasible(final boolean ok){
        if (!ok){
            nextFeasibleSelection.setToolTipText(nextFeasibleError);
            ToolTipOnDemand.getSingleton().showLater(nextFeasibleSelection);
        } else {
            nextFeasibleSelection.setToolTipText(nextFeasibleToolTip);
        }
    }

    private Node currentFeasibleSelection;
    boolean hasCurrentEnabledNode(){
        return currentFeasibleSelection!=null && currentFeasibleSelection != root;
    }
    private final JButton findAgainButton = new JButton(
    		MmsIcons.getFindAgainIcon());

    private final JMenuItem expandItem = new JMenuItem();

    private void setExpandState(final boolean initializing){
        final boolean was=groupSeivedModel.getProperty(PersonalizableTableModel.PROPERTY_EXPAND_ALL_AT_STARTUP, false);
        if (initializing){
            if (seeOnlyTokens != null){
            	expandFound();
            } else  if (ungroupedModel.expandToLevel > 0 ) {
            	SwingBasics.expandToLevel(tree, ungroupedModel.expandToLevel );
            } else if (was){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SwingBasics.expandOrCollapse(tree, true,null);
                    }
                });
            }
        } else {
            if (!was) {
                if (PopupBasics.ask("<html>Show all levels when the tree first renders<br>...<i>and now <b>too</b></i>??</html>")){
                    SwingBasics.expandOrCollapse(tree, true,null);
                } else {
                    return;
                }
            }
        }
        if (!initializing){
            groupSeivedModel.setProperty(PersonalizableTableModel.PROPERTY_EXPAND_ALL_AT_STARTUP, !was);
            expandItem.setText(was ? "All levels at startup":"One level at startup");
        }  else {
            expandItem.setText(!was ? "All levels at startup":"One level at startup");
        }
        expandItem.setIcon(MmsIcons.getWideBlankIcon());
    }

    private final ActionListener expandAction = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setExpandState(false);
            }
        };

        private void initSearchNodePanel(){
        	if (searchNodePanel==null){
        	searchNodePanel = new JPanel();
            searchNodePanel.addMouseListener(myMouseListener);
            ac.setBorder(BorderFactory.createMatteBorder(1, 2, 1, 2, Color.BLUE));
        //ac.getTextField().setColumns(10);
            searchNodePanel.add(new JLabel(MmsIcons.getSearchIcon()));
            searchNodePanel.add(ac);
            searchNodePanel.add(findAgainButton);

            findAgainButton.addActionListener(findAgainAction);
            if (hasApplicationSpecificTreeSort) {
                prevFeasibleSelection = new JButton(MmsIcons.getLeftIcon());
                prevFeasibleSelection.setToolTipText(prevFeasibleToolTip);
                prevFeasibleSelection.addActionListener(
                  new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        int count = 0;
                        while (count < 2) { // wrap
                            if (currentFeasibleSelection == null) {
                                currentFeasibleSelection = lastNode == null ? root : lastNode;
                            }
                            for (currentFeasibleSelection = (Node) currentFeasibleSelection.
                              getPreviousNode();
                              currentFeasibleSelection != null;
                              currentFeasibleSelection = (Node)
                                                         currentFeasibleSelection.getPreviousNode()) {
                                final String debug = currentFeasibleSelection.toString();
                                if (currentFeasibleSelection.sortIndexThatDiffers >=
                                    ungroupedModel.minimumPickLevel) {
                                    final String s = currentFeasibleSelection.getAnomaly(true);
                                    if (Basics.isEmpty(s)) {
                                        currentFoundPath = currentFeasibleSelection.
                                          getPathFromRoot();
                                        lettersTypedByUser="";
                                        lettersFoundFromTyped="";
                                        tree.scrollPathToVisible(currentFoundPath);
                                        reallyRepaintTree();
                                        setFeasible(true);
                                        return;
                                    }
                                }
                            }
                            count++;
                        }
                        setFeasible(false);
                    }

                });
                searchNodePanel.add(prevFeasibleSelection);
                nextFeasibleSelection = new JButton(MmsIcons.getRightIcon());

                nextFeasibleSelection.setToolTipText(nextFeasibleToolTip);
                nextFeasibleSelection.addActionListener(
                  new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        int count = 0;
                        while (count < 2) { // wrap
                            if (currentFeasibleSelection == null) {
                                currentFeasibleSelection = root;
                            }
                            for (currentFeasibleSelection = (Node) currentFeasibleSelection.
                              getNextNode();
                              currentFeasibleSelection != null;
                              currentFeasibleSelection = (Node)
                                                         currentFeasibleSelection.getNextNode()) {
                                final String debug = currentFeasibleSelection.toString();
                                if (currentFeasibleSelection.sortIndexThatDiffers >=
                                    ungroupedModel.minimumPickLevel) {
                                    final String s = currentFeasibleSelection.getAnomaly(true);
                                    if (Basics.isEmpty(s)) {
                                        currentFoundPath = currentFeasibleSelection.
                                          getPathFromRoot();
                                        lettersTypedByUser="";
                                        lettersFoundFromTyped="";
                                        tree.scrollPathToVisible(currentFoundPath);
                                        reallyRepaintTree();
                                        setFeasible(true);
                                        return;
                                    }
                                }
                            }
                            count++;
                        }
                        setFeasible(false);

                    }
                });
                searchNodePanel.add(nextFeasibleSelection);
            }
        	}
        }

    private final JMenuItem showSearchNodePanelItem = new JMenuItem();
    private boolean isSearchNodePanelShowing() {
    	return ungroupedModel.getProperty(
    	          PersonalizableTableModel.PROPERTY_SHOW_SEARCH_NODE_PANEL, false);	
    }
    
    private void setSearchNodePanel(final boolean initializing){
        final boolean was = isSearchNodePanelShowing();
        if (initializing) {
            if (was) {
                initSearchNodePanel();
                if (ungroupedModel.showToolBarPanelInTree) {
               	 treePanel.add(searchNodePanel, BorderLayout.NORTH);
               }
               else {
               	 treePanel.add(searchNodePanel, BorderLayout.SOUTH);
               }
                if (!FocusFreeze.isFrozen()  && !ungroupedModel.isInTreeBuildingMode) {
                    SwingUtilities.invokeLater(
                      new Runnable() {
                        public void run() {
                            ac.requestFocus();
                        }
                    });
                }
                setAutoCompleteItems();
            }
        } else {
            initSearchNodePanel();
            if (!was && !FocusFreeze.isFrozen()) {
                SwingUtilities.invokeLater(
                  new Runnable() {
                    public void run() {
                        ac.requestFocus();
                    }
                });

                if (ac.getItemCount() == 0) {
                    setAutoCompleteItems();
                }
                if (ungroupedModel.showToolBarPanelInTree) {
                	 treePanel.add(searchNodePanel, BorderLayout.NORTH);
                }
                else {
                	 treePanel.add(searchNodePanel, BorderLayout.SOUTH);
                }
               
                searchNodePanel.setVisible(true);
            } else {
                treePanel.remove(searchNodePanel);
                searchNodePanel.setVisible(false);
            }
            treePanel.updateUI();
        }

        if (!initializing) {
            ungroupedModel.setProperty(PersonalizableTableModel.
                                         PROPERTY_SHOW_SEARCH_NODE_PANEL, !was);
            ungroupedModel.notifyViewChanged();
            showSearchNodePanelItem.setText(was ? "Show search node panel" :
                                            "Hide search node panel");
        } else {
            showSearchNodePanelItem.setText(!was ? "Show search node panel" :
                                            "Hide search node panel");
            ac.setToolTipText(
              Basics.toHtmlUncentered(
                "Search tree",
                "To search the tree<br>" +
                "enter a value here"
              )
              );

        }
    }
    
    
    private final ActionListener showSearchNodePanelAction = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setSearchNodePanel(false);
            }
        };

    static SeeOnlyTokens remember;
    
    boolean hasSeeOnlyText() {
    	return !Basics.isEmpty(tfFind.getText());
    }

    String getSeeOnlyText() {
    	final String txt=tfFind.getText();
    	return txt;
    }
    

	private void seeOnly() {
		final String txt=tfFind.getText();
		ungroupedModel.showOneMomentDisplay();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!Basics.isEmpty(txt)) {
					seeOnlyTokens = new SeeOnlyTokens(tfFind.getText(),
							ungroupedModel, true);
					ungroupedModel
							.getDataSource()
							.setFilteringContext(
									PersonalizableDataSource.FILTER_CONTEXT_QUERY_CONCLUSION);
					ungroupedModel.refresh();
					ungroupedModel.groupedDataSource.tfFind.requestFocus();
					ungroupedModel.fireTreeSearchListeners();
					ungroupedModel.getDataSource().setFilteringContext(
							PersonalizableDataSource.FILTER_CONTEXT_NORMAL);
				} else {
					seeAll();
				}
				ungroupedModel.hideOneMomentDisplay();
			}
		});
	}

	static boolean removingSeeOnlyTokens=false;
	private void seeAll() {
		final String txt=tfFind.getText();
		boolean wasEmpty=Basics.isEmpty(txt) ;
		if (!wasEmpty) {
			tfFind.setText("");
			tfFind.requestFocus();
		} else  {
			setFindPanel(false);
		}
		if (seeOnlyTokens != null) {
			seeOnlyTokens = null;
			removingSeeOnlyTokens = true;
			ungroupedModel.refresh();
			removingSeeOnlyTokens = false;
		}
		ungroupedModel.fireTreeSearchListeners();
	}
	
	private int priorWidth=-1; 
	private TextAreaWithHint tfFind = new TextAreaWithHint(3, 15);
	private boolean findButtonsAreWest=true;
	private void setFindTextArea(final boolean updateUI){
		if (findPanel != null ){
			final Dimension d=findPanel.getSize();
			if (priorWidth == -1 || priorWidth != d.width ){
	    		priorWidth=d.width;
	    		final int i=(priorWidth-20)/11;
	    		SwingUtilities.invokeLater(new Runnable(){
	    			public void run(){
	    				final int columns;
	    	    		if (i < 6){
	    	    			columns=5;
	    	    			if (findButtonsAreWest){
	    	    				findButtonsAreWest=false;
	    	    				tfFind.setRows(2);
	    	    				setFindButtons(1,2+(ungroupedModel.additionalTreeSearch==null?0:1), BorderLayout.SOUTH);
	    	    			}	    	    			
	    	    		} else {
	    	    			columns=i;
	    	    			if (!findButtonsAreWest){
	    	    				findButtonsAreWest=true;
	    	    				tfFind.setRows(3);
	    	    				setFindButtons(2+(ungroupedModel.additionalTreeSearch==null?0:1), 1, BorderLayout.EAST);
	    	    			}
	    	    		}
	    	    		tfFind.setColumns(columns);
	    	    		if (updateUI){
	    	    			findPanel.updateUI();
	    	    		}
	    			}
	    		});
	    	}
		}
	}
	
	private JButton seeOnly, seeAll;
	
	private void setFindButtons(final int row, final int col, final String where){
		final JPanel findButtons = new JPanel(new GridLayout(row, col));
			final JPanel jp=new JPanel();
			jp.add(seeOnly);
			findButtons.add(jp);
			JPanel jp2=new JPanel();
			jp2.add(seeAll);
			findButtons.add(jp2);
			if (ungroupedModel.additionalTreeSearch != null) {
				jp2=new JPanel();
				SwingBasics.setToolBarStyle(ungroupedModel.additionalTreeSearch);
				
				jp2.add(ungroupedModel.additionalTreeSearch);
				findButtons.add(jp2);
			}
			findPanel.add(findButtons, where);		
			findPanel.addMouseListener(myMouseListener);
	}
			
	private void initFindPanel() {
		if (findPanel == null) {
				seeOnlyTokens=remember;
				if (remember != null){
					remember=null;				
				} 
			tfFind.registerKeyboardAction(new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						seeOnly.doClick();
					}}, SwingBasics.addAltOrMetaIfMac(KeyEvent.VK_L), JComponent.WHEN_FOCUSED);
			tfFind.setDocumentChangeListener(new TextAreaWithHint.DocumentChangeListener() {
				
				public void onChange(TextAreaWithHint hta) {
					ungroupedModel.treeSeeOnlyText=tfFind.getText();
					ungroupedModel.treeSeeOnlyTextSort=ungroupedModel.getSort();
					PersonalizableTableModel.enableSeeOnly(
							!Basics.isEmpty(ungroupedModel.treeSeeOnlyText), 
							seeOnly, 
							seeAll);	
				}
			});
			final JScrollPane jspFind=ungroupedModel.initFindYellowSticky(tfFind, ungroupedModel.additionalTreeSearchToolTip);
			findPanel = new JPanel(new BorderLayout());
			findPanel.add(jspFind, BorderLayout.CENTER);
			seeOnly = new SwingBasics.ImageButton(MmsIcons.getMagnifyIcon());
			seeOnly.addActionListener(
					new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					seeOnly();
				}
			});
			
			seeOnly.setMnemonic('l');
			seeOnly.setToolTipText(PersonalizableTableModel.seeOnlyToolTip);
			seeAll = new SwingBasics.ImageButton(
					Basics.isEmpty(tfFind.getText()) ?MmsIcons.getCancelIcon():MmsIcons.getEyeIcon());
			seeAll.setMnemonic('a');
			seeAll.addActionListener(			
					new ActionListener() {
		public void actionPerformed(final ActionEvent ae) {
			seeAll();
		}
	});
			
			seeAll.setToolTipText(PersonalizableTableModel.seeAllToolTip);
			setFindButtons(2 +(ungroupedModel.additionalTreeSearch==null?0:1),1, BorderLayout.EAST);
			findPanel.addComponentListener(new ComponentListener() {
				public void componentHidden(ComponentEvent e) {					
				}

				public void componentMoved(ComponentEvent e) {				
				}

				public void componentResized(ComponentEvent e) {
					final Dimension d=findPanel.getSize();
					if (d.width>0)
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									setFindTextArea(true);
								}
							});
	
				}

				public void componentShown(ComponentEvent e) {			
				}
			});
			if (seeOnlyTokens != null){
				tfFind.setText(seeOnlyTokens.searchEntry);
			} else if (!Basics.isEmpty(ungroupedModel.treeSeeOnlyText) && Basics.equals(ungroupedModel.getSort(), ungroupedModel.treeSeeOnlyTextSort)) {
				tfFind.setText(ungroupedModel.treeSeeOnlyText);
			}
			tfFind.setHint(
					ungroupedModel.applicationSpecificTreeSearch==null || !Basics.containsAll(ungroupedModel.getSort(), ungroupedModel.applicationSpecificTreeSearch)?
							"Enter search values":ungroupedModel.textAreaSearchHint);
			PersonalizableTableModel.enableSeeOnly(!Basics.isEmpty(tfFind.getText()), seeOnly, seeAll);			
		}
	}

	private static String PROPERTY_FIND_PANEL = "findPanel";
	private final JMenuItem findItem = new JMenuItem("Find", MmsIcons
			.getSearchIcon());

	boolean isFindPanelShowing() {
		return ungroupedModel.getProperty(PROPERTY_FIND_PANEL, false);
	}

	void focusOnYellowSticky() {
		if (isFindPanelShowing()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					tfFind.requestFocus();
				}
			});
		}
	}
	
	void setFindPanel(final boolean initializing) {
		if (ungroupedModel.treeHasSeeOnlyAbility) {
			final boolean was = isFindPanelShowing();
			if (initializing) {
				if (was) {
					initFindPanel();
					treePanel.add(findPanel, BorderLayout.NORTH);
					if (!FocusFreeze.isFrozen()
							&& !ungroupedModel.isInTreeBuildingMode) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								tfFind.requestFocus();
							}
						});
					}
				}
			} else {
				initFindPanel();
				if (!was && !FocusFreeze.isFrozen()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tfFind.requestFocus();
						}
					});

					treePanel.add(findPanel, BorderLayout.NORTH);
					findPanel.setVisible(true);
				} else {
					treePanel.remove(findPanel);
					findPanel.setVisible(false);
				}
				treePanel.updateUI();
			}

			if (!initializing) {
				ungroupedModel.setProperty(PROPERTY_FIND_PANEL, !was);
				ungroupedModel.notifyViewChanged();
				findItem.setText(was ? "Find" : "Hide find panel");
			} else {
				findItem.setText(!was ? "Find" : "Hide find panel");

			}
		}
	}

	private final ActionListener findAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			setFindPanel(false);
		}
	};

    private final JMenuItem findAgainMenuItem = new JMenuItem(
      "Find again",
      MmsIcons.getFindAgainIcon());
    DisabledExplainer findAgainDisabled = new DisabledExplainer(findAgainMenuItem);

    private final ActionListener findAgainAction = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            findAgain();
        }
    };

    private void setFindNext() {
        if (foundPaths == null || foundPaths.length < 2) {
            findAgainMenuItem.setText("Find again");
            findAgainDisabled.setEnabled(false, "Find Again", "This operation is ONLY enabled if you have already searched");
            findAgainButton.setEnabled(false);
            final String toolTip = searchArgument == null ? "" :
                                   Basics.toHtmlUncentered(
                                     "Find again",
                                     "The search for <i>" + searchArgument +
                                     "</i><br>found " +
                                     (foundPaths == null ? 0 :
                                      foundPaths.length) +
                                     " items");
            findAgainMenuItem.setToolTipText(toolTip);
            findAgainButton.setToolTipText(toolTip);
            ToolTipOnDemand.getSingleton().hideTipWindow();
        } else {
            final int idx = (currentFindIdx + 1) % foundPaths.length;
            final String txt = "" + (idx == 0 ? foundPaths.length : idx) +
                               " of " + foundPaths.length;
            final String text = "Find again (" + txt + ")";
            final String toolTip = Basics.toHtmlUncentered(
              "Find again",
              "Click this button to find the next <br>instance of <i>" +
              searchArgument +
              "</i>.<br><br>We are currntly looking at instance #<b>" + txt +
              "</b>.");
            findAgainMenuItem.setText(text);
            findAgainMenuItem.setToolTipText(toolTip);
            findAgainDisabled.setEnabled(true,null,null);
            findAgainButton.setToolTipText(toolTip);
            ToolTipOnDemand.getSingleton().show(findAgainButton, false);
            findAgainButton.setEnabled(true);
        }
    }

    final static Border BORDER_FOUND_UNSELECTED = new LineBorder(UIManager.
      getColor(
        "Tree.textForeground"), 1, true),
    BORDER_FOUND_SELECTED = new LineBorder(Color.red, 1, true);

    private final static Font FONT_FOUND, FONT_NORMAL;
    static {
        FONT_NORMAL = UIManager.getFont("Tree.font");
        FONT_FOUND = new Font(FONT_NORMAL.getName(), Font.ITALIC | Font.BOLD,
                              FONT_NORMAL.getSize());
    }

    public final static Font baseFont = UIManager.getFont("Tree.font"),
    boldFont = new Font(baseFont.getName(), Font.BOLD, baseFont.getSize()),
    italicFont=new Font(baseFont.getName(), Font.ITALIC, baseFont.getSize());

    static final GraydIcon
      grayedLeaf = new GraydIcon(UIManager.getIcon("Tree.leafIcon")),
    grayedOpen = new GraydIcon(UIManager.getIcon("Tree.openIcon")),
    grayedClosed = new GraydIcon(UIManager.getIcon("Tree.closedIcon"));

    private class Renderer extends DefaultTreeCellRenderer {
        private Icon disabledLeafIcon, disabledOpenIcon, disabledClosedIcon;

        private Renderer() {
            this(grayedLeaf,
                 grayedOpen,
                 grayedClosed);
        }

        public Renderer(final Icon leafIcon, final Icon openIcon,
                        final Icon closedIcon) {
            setDisabledLeafIcon(leafIcon);
            setDisabledOpenIcon(openIcon);
            setDisabledClosedIcon(closedIcon);
        }

        private Map disabledIcons = new HashMap();
        

        public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean isSelected,
				final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
			final Node node = (Node) value;
			final Row _row=node.groupedRow != null ? node.groupedRow.firstUngroupedRow:null;
			final boolean rootLimitedByQuery= node==root && ungroupedModel.hasSeeOnlySettings();
			String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
			final boolean hasCustomHtmlIcon=stringValue.startsWith("<html><img src='");
			if (rootLimitedByQuery && stringValue.equals("root")){
				stringValue="<html>root <i>(limited by query)</i></html>";
			}
			boolean isNull=false;
			if ("<null>".equals(stringValue)){
				sb.setLength(0);
				sb.append("(No \"");
				sb.append(Basics.stripSimpleHtml(node.getColumnLabel()));
				sb.append("\")");
				stringValue=sb.toString();
				isNull=true;
				setFont(italicFont);
			} else {
				setFont(baseFont);
			}
			final boolean isFound = currentFoundPath != null && tree.getRowForPath(currentFoundPath) == row;
			final boolean isFoundByAutoComplete = isFound && !Basics.isEmpty(lettersFoundFromTyped);
			if (isFound && !isFoundByAutoComplete) {
				setBorder(isSelected ? BORDER_FOUND_SELECTED : BORDER_FOUND_UNSELECTED);
			} else {
				setBorder(PersonalizableTable.BORDER_EMPTY);
			}
			String anomaly = null;
			if (_row!= null) {
					boolean isActive = _row.isActive();
					if (!isActive && Basics.isEmpty(ungroupedModel.applicationSpecificTreeSort) ){
						final Node.UngroupedChildRowIterator it = node.new UngroupedChildRowIterator();
						while (!isActive && it.hasNext()){
							final Row r=(Row)it.next();
							isActive=r.isActive();							
						}
						if (!isActive){
							setBorder(PersonalizableTable.BORDER_EMPTY);
							anomaly = Basics.toHtmlUncentered("Inactive Item",PersonalizableTable.ANOMALY_INACTIVE);
						}
					}
				
			}
			final boolean treeIsEnabled = tree.isEnabled();
			if (anomaly == null && !resettingPicks) {
				if (isSelected) {
					if (_row != null
							&& ungroupedModel.dataSource instanceof PersonalizableDataSource.CanDisable) {
						anomaly = ((PersonalizableDataSource.CanDisable) ungroupedModel.dataSource)
								.getSelectedNodeDisabledText(node, _row);
					} else {
						anomaly = null;
					}
				} else if (SHOW_ANOMALY){
					anomaly = node.getAnomaly(true);
				}
			}
	        Color selectedColor=ungroupedModel.table.getSelectionForeground(),color=getTextNonSelectionColor();
	        String textSelectedColor = SwingBasics.toHtmlRGB(selectedColor),
	                textColor =SwingBasics.toHtmlRGB(color),
	                disabledColor=SwingBasics.toHtmlRGB(SystemColor.textInactiveText);

			final boolean nodeIsEnabled = anomaly == null;
			final boolean isEnabled = (treeIsEnabled && nodeIsEnabled);
			final Color[] bg=ungroupedModel.getSelectionColorsBasedOnSelectionsInActiveTree(node, node.groupedRow, 
					ungroupedModel.table.getSelectionForeground(), 
					ungroupedModel.table.getSelectionBackground(), false);
			final String foregroundColor, backgroundColor;
			if (isFoundByAutoComplete) {
			if (isSelected) {
				foregroundColor = textSelectedColor;
				backgroundColor=SwingBasics.toHtmlRGB(bg[1]);
			} else {
				if (isEnabled) {
					backgroundColor="black";
					foregroundColor = "white";
				} else {
					foregroundColor = "rgb(220,220,220)";
					backgroundColor="gray";
				}
			}
			} else {
				backgroundColor=null;
				if (isSelected) {
					foregroundColor = textSelectedColor;
				} else {
					if (isEnabled) {
						foregroundColor = textColor;
					} else {
						foregroundColor = disabledColor;
					}
				}
			}
			sb.setLength(0);
			if (treeShowCounts) {
				final Basics.HtmlBody hb=new Basics.HtmlBody(stringValue);
				final Node.UngroupedChildRowIterator it = node.new UngroupedChildRowIterator();
				final String _countColor;
				if (isSelected) {
					_countColor = textSelectedColor;
				} else {
					if (isEnabled) {
						_countColor = disabledColor;
					} else {
						_countColor = textColor;
					}
				}
				if (isFoundByAutoComplete) {					
					sb.append("<html><body bgcolor='");
					sb.append(backgroundColor);
					sb.append("'><font color='");
					sb.append(foregroundColor);
					sb.append( "'>" );
					Basics.highlightWithYellow(sb, null, isEnabled, hb, lettersFoundFromTyped);
					sb.append( "<sup><font color='");
					sb.append(_countColor);
					sb.append("'>&nbsp;&nbsp;"); 
					sb.append(it.size());
					sb.append("</font></sup></font></body></html>");

				} else {
					stringValue=hb.getHtmlEncoded();
					if (node.isBeingSearched()){
						if (isSelected){
							stringValue=seeOnlyTokens.highlightFgBg(_row, isEnabled ?SearchAndReplace.YELLOW:SearchAndReplace.YELLOW_DISABLED, "black", stringValue);
						}else{
							stringValue=seeOnlyTokens.highlightBg(_row, isEnabled ?SearchAndReplace.YELLOW:SearchAndReplace.YELLOW_DISABLED, stringValue);
						}
					} 
					sb.append("<html><font color='");
					sb.append(foregroundColor );
					sb.append("'>" );
					sb.append(hb.prefix);
					sb.append(stringValue );
					sb.append(hb.suffix);
					sb.append("<sup><font color='");
					sb.append(_countColor );
					sb.append("'>&nbsp;&nbsp;" );
					sb.append( Basics.encode(it.size()) );
					sb.append("</font></sup></font></html>");
				}
				stringValue=sb.toString();
			} else {
				if (isFoundByAutoComplete) {					
					sb.append("<html><body bgcolor='"); 
					sb.append(backgroundColor);
					sb.append( "'><font color='");
					sb.append( foregroundColor );
					sb.append( "'>");
					Basics.highlightWithYellow(sb,null, isEnabled,
							new Basics.HtmlBody(stringValue), lettersFoundFromTyped);					
					sb.append("</font></body></html>");
					stringValue=sb.toString();
				} else if (node.isBeingSearched()){
					final Basics.HtmlBody hb=new Basics.HtmlBody(stringValue);
					stringValue=hb.getHtmlEncoded();					
					if (isSelected){
						stringValue=seeOnlyTokens.highlightFg(_row, isEnabled ?SearchAndReplace.YELLOW:SearchAndReplace.YELLOW_DISABLED, stringValue);
					}else{
						stringValue=seeOnlyTokens.highlightBg(_row, isEnabled ?SearchAndReplace.YELLOW:SearchAndReplace.YELLOW_DISABLED, stringValue);
					}
					sb.append("<html><body bgcolor='"); 
					sb.append(backgroundColor);
					sb.append( "'><font color='" );
					sb.append(foregroundColor );
					sb.append("'>");
					sb.append( hb.prefix  );
					sb.append(stringValue );
					sb.append(hb.suffix );
					sb.append("</font></body></html>");
					stringValue=sb.toString();
				} else {
					int ch='c';
				}
			}
			setText(stringValue);
			if (ungroupedModel.selectionsPropagateToLeaf) {
				if (nodesWithPartialChildSelections.contains(node)) {
					setFont(boldFont);
				
				}else {
					setFont(baseFont);
				}
			}

			setEnabled(isEnabled);
			selected = isEnabled && isSelected;
			final boolean isDragging=dragAndDrop !=null && dragAndDrop.draggedPath != null && dragAndDrop.draggedPath .equals(node.getPathFromRoot());
			if (isDragging){
				setIcon(SwingBasics.pressedIcon);
			} else if (isFoundByAutoComplete) {
				setIcon(MmsIcons.getPointIcon());
			} else {
			final Icon icon = node.getSpecialIcon(expanded, leaf);
			if (icon != null) {
				setIcon(icon);
				Icon disabledIcon = (Icon) disabledIcons.get(icon);
				if (icon == null) {
					disabledIcon = new GraydIcon(icon);
					disabledIcons.put(icon, disabledIcon);
				}
				setDisabledIcon(disabledIcon==null?icon:disabledIcon);
			} else {
				if (rootLimitedByQuery){
					setIcon(MmsIcons.getMagnifyIcon());
					
				}else
				if (isEnabled) {
					if (hasCustomHtmlIcon) {
						setIcon(null);
					} else if (leaf) {
						setIcon(getLeafIcon());
					} else if (expanded) {
						setIcon(getOpenIcon());
					} else {
						setIcon(getClosedIcon());
					}
				} else {
					if (hasCustomHtmlIcon) {
						setIcon(null);
					} else if (leaf) {
						if (nodeIsEnabled) {
							setDisabledIcon(getLeafIcon());
						} else {
							setDisabledIcon(disabledLeafIcon);
						}
					} else if (expanded) {
						if (nodeIsEnabled) {
							setDisabledIcon(getOpenIcon());
						} else {
							setDisabledIcon(disabledOpenIcon);
						}
					} else {
						if (nodeIsEnabled) {
							setDisabledIcon(getClosedIcon());
						} else {
							setDisabledIcon(disabledClosedIcon);
						}
					}
				}
			}
        }
			if (isSelected) {
				if (isDragging){
					setForeground(Color.WHITE);
					setBackgroundSelectionColor(DRAG_BG_COLOR);
					
				}else{
					setForeground(bg[0]);
					setBackgroundSelectionColor(bg[1]);
				}
			} else {
				if (isDragging){
					setForeground(DRAG_BG_COLOR);
				} else {
					setForeground(color);
				}
			}

			if (anomaly != null) {
				setToolTipText(anomaly);
			} else {
				String s = node.getToolTip();
				if (Basics.isEmpty(s) && ungroupedModel.nodeSelectStyle != NodeSelectStyle.ON_DOUBLE_CLICK) {
					final StringBuilder sb = new StringBuilder(166);
					final String multiSelectKey = !SwingBasics.usesMacMetaKey()? "<i>ctrl</i>"
							: "<i>apple</i>";
					sb.append("Currently ");
					sb.append(tree.getSelectionCount());
					sb.append(" tree selections filter/select ");
					sb.append(groupSeivedModel.getRowCount());
					sb.append(" rows of ");
					sb.append(ungroupedRowCount);
					sb.append(" total rows.");
					sb.append("<br>Double-click a column heading of the filtered table to edit all (");
					sb.append(groupSeivedModel.getRowCount());
					sb.append(") of these rows at the same time.<br><br><b>General tree selection tips:</b>");
					sb.append("<ul>");
					sb.append("<li>Select nodes on tree to filter rows");
					sb.append("<li>Select root node of tree filter all rows");
					sb.append("<li>Hold down the ");
					sb.append(multiSelectKey);
					sb.append(" key to do a NON contiguous select/unselect");
					sb.append("<li>Hold down the shift key to do a contiguous select/unselect");
					sb.append("<li>To unselect an item click on it while pressing ");
					sb.append(multiSelectKey);
					sb.append("<li>To see a menu of action choices ");
					if (SwingBasics.usesMacMetaKey()) {
						sb.append("click while holding the <ctrl> key<li>or right click on 2 button mouse</ul>");
					} else {
						sb.append("click the right mouse button");
					}
					sb.append("</ul>");
					if (title == null) {
						s = sb.toString();
					} else {
						s = Basics.toHtmlUncentered(title, sb.toString());
					}
				}
				setToolTipText(s);
			}
	        if (ungroupedModel.dataSource.isDecorator() && node.sortIndexThatDiffers>=0) {
	        	final int dc=sortInfo[node.sortIndexThatDiffers].dataColumnIndex;
	        	final TableCellContext context=new TableCellContext(ungroupedModel, dc, node.getFirstUngroupedRow());
	        	ungroupedModel.dataSource.decorate(context, this, isSelected, hasFocus);
	        }

			return this;
		}

        // workaround to SUN documented bug in HTML rendering of JTree from URL
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4373575
        protected void firePropertyChange(String propertyName, Object oldValue,
                                          Object newValue) {
            if (propertyName.equals("text")) {
                super.firePropertyChange(propertyName, oldValue, newValue);
                this.updateUI(); // this is the line that made the difference
            }
        }


        public void setDisabledLeafIcon(final Icon icon) {
            disabledLeafIcon = icon;
        }

        public void setDisabledOpenIcon(final Icon icon) {
            disabledOpenIcon = icon;
        }

        public void setDisabledClosedIcon(final Icon icon) {
            disabledClosedIcon = icon;
        }

    }

    void showDisabledText(final Node node, final String _txt, final boolean beep){
        final String txt=node.getAnomaly(false);
        if (txt==null){
        	if (_txt.startsWith("<html>") ){
        		if (_txt.contains("Can not select")){
            	tree.setToolTipText(_txt);        	
        		} else {
        			tree.setToolTipText(Basics.toHtmlUncentered("Can not select "+ungroupedModel.encodeNode(node)+" because:", Basics.stripBodyHtml(_txt)));
        		}
        	} else{
        		tree.setToolTipText(Basics.toHtmlUncentered("Can not select "+ungroupedModel.encodeNode(node)+" because:", _txt));
        	}
        } else {
        	tree.setToolTipText(txt);
        }
        showToolTipLater(node, beep,true);
    }

    public void showText(final Node node, final String _txt){
    	showText(node,_txt,false);
    }
    
    public void showText(final Node node, final String _txt, final boolean beep){
        tree.setToolTipText(_txt);
        showToolTipLater(node, beep,true);
    }
    
    void showTextWithoutCloseButton(final Node node, final String _txt, final boolean beep){
    	if (tree != null){
        tree.setToolTipText(_txt);
        showToolTipLater(node, beep, false);
    	}
    }

    void showToolTipLater(final Node node, final boolean beep, final boolean userMustClose){
    	showToolTipLater(node, beep, userMustClose, false);
    }
    
    void showToolTipLater(final Node node, final boolean beep, final boolean userMustClose, final boolean showCancelButtonAtTopRight){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				if (node != null && tree != null) {
					final Rectangle rec = tree.getPathBounds(node
							.getPathFromRoot());
					if (rec != null) {
						ToolTipOnDemand.getSingleton().show(tree, false,
								rec.x + rec.width/2, rec.y + rec.height,
								userMustClose, showCancelButtonAtTopRight);
					}
					if (beep) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
            }
        });

    }

    public TreeDragAndDrop dragAndDrop;
    public class TreeDragAndDrop implements DropTargetListener, DragSourceListener, DragGestureListener {

        private final RowDragAndDrop.Draggable _th;
        private final DragSource _source;

        
        private final TransferHandler th;

        private TreeDragAndDrop() {
            th=ungroupedModel.table.getTransferHandler();
            if (th instanceof RowDragAndDrop.Draggable ){
                _th=(RowDragAndDrop.Draggable)th;
                if (!hasApplicationSpecificTreeSort ){
                	ungroupedModel.autoSelectTableFromTree=true;
                }
            } else {
                _th=null;
            }
            _source = new DragSource();
            _source.createDefaultDragGestureRecognizer(tree,
              DnDConstants.ACTION_COPY_OR_MOVE, this);
            new DropTarget(tree, this);
        }
        private TreePath draggedPath; 
        /*
		 * Drag Gesture Handler
		 */
        public void dragGestureRecognized(final DragGestureEvent dge) {
			if (op == SelectionOp.ADD || op == SelectionOp.SET || op==SelectionOp.REMOVE) {
				final java.awt.Point p = dge.getDragOrigin();
				final TreePath _draggedPath = tree.getClosestPathForLocation(p.x, p.y);
				final Node node = (Node) _draggedPath
						.getLastPathComponent();
				startDragging(_source, node, dge, _draggedPath);
				
			}
        	saveForLater=false;
			
		}
        
        public void startDragging(final DragSource source, final DragGestureEvent dge){
			Transferable transferable=_th.getDraggedRows(
					null,
					groupSeivedModel.table);

			if (transferable != null) {
				if (source.isDragImageSupported()) {
					BasicStroke linestyle = new BasicStroke(3);
					Rectangle scribbleBox = new Rectangle(new Dimension(40,20));
					Image dragImage=new BufferedImage(40, 20,BufferedImage.TYPE_4BYTE_ABGR);						
					Graphics2D g = (Graphics2D) dragImage.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, scribbleBox.width, scribbleBox.height);
					g.setColor(Color.black);
					g.setStroke(linestyle);
					g.translate(-scribbleBox.x, -scribbleBox.y);
					g.draw(scribbleBox);
					Point hotspot = new Point(-scribbleBox.x, -scribbleBox.y);
					source.startDrag(dge, null, dragImage, hotspot, transferable, this);
				}  else {
					source.startDrag(dge, null, transferable, this);				
				}
			}	
        }

        public void startDragging(final DragSource source, Node draggedNode, final DragGestureEvent dge, final TreePath _draggedPath){
        	final String debug = draggedNode.toString();
        	saveForLater=false;
        	ToolTipOnDemand.getSingleton().hideTipWindow();
        	if (ungroupedModel.treeDragListener != null ){
        		draggedNode=ungroupedModel.treeDragListener.getDragStartingNode(draggedNode, op==SelectionOp.REMOVE);
        		if (draggedNode==null){
        			return;
        	}
        		
        	}
        	draggedNodeWasAlreadySelected=op==SelectionOp.REMOVE;
			if (ungroupedModel.treeDragListener != null){
				final String anomaly=ungroupedModel.treeDragListener.getDragGestureAnomaly( draggedNode, op==SelectionOp.REMOVE);
				if (anomaly != null){
					if (anomaly.length() > 0) {
						if (anomaly.contains("<html>")) {
							showText(draggedNode, anomaly, false);
						} else {
							showText(draggedNode, Basics.toHtmlUncentered("Can not drag and drop ...", anomaly), false);
						}
						if (draggedNode.getAnomaly(true) == null && op!=SelectionOp.REMOVE && ungroupedModel.nodeSelectStyle!=NodeSelectStyle.ONLY_BY_DRAGGING) {
							PopupBasics
									.alert(tree, 
											Basics.concatHtmlUncentered(Basics.NOTE_BOLDLY,
											draggedNode.toString(),
											" is still selectable by <u>", 
											(ungroupedModel.nodeSelectStyle==NodeSelectStyle.ON_DOUBLE_CLICK?"double":""), 
											" clicking</u>.<br>But you can not select it by <u>dragging and dropping</u>."));
						}
					}
					if (draggedNodeWasAlreadySelected){
						draggedNodeWasAlreadySelected=false;
						ungroupedModel.treeDragListener.stopDraggingPriorSelectedNode(draggedNode, false);
					}
					needDoubleClickHelpMessage=false;
					return;					
				}
			}
			draggedPath=_draggedPath;
			SwingBasics.repaint(tree, draggedPath);
			final Transferable transferable = _th.getDraggedRows(
					(GroupedDataSource.Node) draggedNode,
					groupSeivedModel.table);
			if (transferable != null) {
				if (source.isDragImageSupported()) {
					BasicStroke linestyle = new BasicStroke(3);
					Rectangle scribbleBox = new Rectangle(new Dimension(40,20));
					Image dragImage=new BufferedImage(40, 20,BufferedImage.TYPE_4BYTE_ABGR);						
					Graphics2D g = (Graphics2D) dragImage.getGraphics();
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, scribbleBox.width, scribbleBox.height);
					g.setColor(Color.black);
					g.setStroke(linestyle);
					g.translate(-scribbleBox.x, -scribbleBox.y);
					g.draw(scribbleBox);
					Point hotspot = new Point(-scribbleBox.x, -scribbleBox.y);
					source.startDrag(dge, null, dragImage, hotspot, transferable, this);
				}  else {
					source.startDrag(dge, null, transferable, this);				
				}
			}	
        }

        /*
		 * Drag Event Handlers
		 */
        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
            System.out.println("Action: " + dsde.getDropAction());
            System.out.println("Target Action: " + dsde.getTargetActions());
            System.out.println("User Action: " + dsde.getUserAction());
        }

        public void dragDropEnd(final DragSourceDropEvent dsde) {
            /*
             * to support move or copy, we have to check which occurred:
             */
            System.out.println("Drop Action: " + dsde.getDropAction());
            if (dsde.getDropSuccess()
                && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
            }
    		savedSelectionPath=null;
    		SwingBasics.repaint(tree, draggedPath);
    		saveForLater = false;
    		if (draggedNodeWasAlreadySelected){
				ungroupedModel.treeDragListener.stopDraggingPriorSelectedNode((Node)draggedPath.getLastPathComponent(), true);
				draggedNodeWasAlreadySelected=false;
			}
			draggedPath=null;
    		
    		if (ungroupedModel.treeDragListener != null){
    			ungroupedModel.treeDragListener.dragDropEnd();
    		}

            /*
             * to support move only... if (dsde.getDropSuccess()) {
             * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
             */
        }
        boolean draggedNodeWasAlreadySelected=false;

        private void selectNodeForEvent(final DropTargetDragEvent dtde) {
			final Transferable t = dtde.getTransferable();
			if (th.canImport(groupSeivedModel.table, dtde.getCurrentDataFlavors())) {
				java.awt.Point p = dtde.getLocation();
				DropTargetContext dtc = dtde.getDropTargetContext();
				final JTree tree = (JTree) dtc.getComponent();
				final TreePath path = tree.getClosestPathForLocation(p.x, p.y);
				if (Basics.contains(tree.getSelectionPaths(), path)) {
					final int[] selected = groupSeivedModel.table
							.getSelectedRows();
					final int n1 = groupSeivedModel
							.getSelectedDataRowCount(selected);
					final int n2 = groupSeivedModel.getDataRowCount();
					if (n1 != n2) {
						tree.clearSelection();
					}
				}
				tree.setSelectionPath(path);
			}
		}

        private void handleDraggingOnTree(DropTargetDragEvent dtde) {
			if (ungroupedModel.treeDragListener != null ) {
        		if (!ungroupedModel.treeDragListener.canDropOnTree()) {
        			dtde.rejectDrag();
        		} else {
            		dtde.acceptDrag(dtde.getDropAction());
        		}
        	} else {
        		dtde.acceptDrag(dtde.getDropAction());
        	}			

        }
		public void dragEnter(DropTargetDragEvent dtde) {
			handleDraggingOnTree(dtde);
		}

		public void dragExit(DropTargetEvent dte) {			
			
		}

		public void dragOver(final DropTargetDragEvent dtde) {			
			selectNodeForEvent(dtde);
			handleDraggingOnTree(dtde);
		}

		public void drop(DropTargetDropEvent dtde) {
			final Transferable  t=dtde.getTransferable();
			if (ungroupedModel.treeDragListener != null ) {
				ungroupedModel.treeDragListener.dropOnTree(t);
			} else {
			th.importData(groupSeivedModel.table, t);
			}
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			
			
		}
    }

    private boolean treeShowCounts;

    private final TreeUI myUI = new BasicTreeUI() {
            protected boolean isToggleSelectionEvent(final MouseEvent event) {
                if (!ungroupedModel.allowOneClickMultiSelect && ungroupedModel.TREE_MULTI_SELECT_WITH_KEY) { // the standard idiom
                    if (!SwingBasics.usesMacMetaKey()) {
                        return super.isToggleSelectionEvent(event);
                    } else {
                        return (SwingUtilities.isLeftMouseButton(event) &&
                                event.isMetaDown());
                    }
                }
                if (super.isToggleSelectionEvent(event)) {
                    return true;
                }
                
                return !isMultiSelectEvent(event) &&
                  SwingUtilities.isLeftMouseButton(event);
            }
            
            /**
             * Creates the listener reponsible for getting key events from
             * the tree.
             */
            protected KeyListener createKeyListener() {
                return autoCompleter;
            }
                      
        };
    	private String searchFor, lettersTypedByUser = "", lettersFoundFromTyped = "";
        
    	private AutoCompleteKeyHandler autoCompleter=new AutoCompleteKeyHandler();
    private class AutoCompleteKeyHandler implements KeyListener { 
     	private String typedString = "";
     	private long lastTime = 0L;
     	private int lastPromptedRow = -1;
     	private int timeFactor = 0;
    	
     	public AutoCompleteKeyHandler() {
     		this.timeFactor = 1000;
     	}
     	
     	public AutoCompleteKeyHandler(int timeFactor) {
     		this.timeFactor = timeFactor;
     	}
     	/**
     	 * Invoked when a key has been typed.
     	 * 
     	 * Moves the keyboard focus to the first element whose prefix matches the
     	 * sequence of alphanumeric keys pressed by the user with delay less
     	 * than value of <code>timeFactor</code> property (or 500 milliseconds
     	 * if it is not defined). Subsequent same key presses move the keyboard
     	 * focus to the next object that starts with the same letter until another
     	 * key is pressed, then it is treated as the prefix with appropriate number
     	 * of the same letters followed by first typed another letter.
     	 */

    	public void keyTyped(final KeyEvent e) {
    		keyTyped(e, true);
    	}
    	
    	private void keyTyped(final KeyEvent e, final boolean forward) {

			// handle first letter navigation
			if (tree != null && tree.getRowCount() > 0 && tree.hasFocus()
					&& tree.isEnabled()) {
				if (e != null
						&& (e.isAltDown() || e.isControlDown()
								|| e.isMetaDown() || isNavigationKey(e))) {
					return;
				}
				final char c = e == null ? '\0' : e.getKeyChar();
				final long time = e == null ? System.currentTimeMillis() : e
						.getWhen();
				int startingRow = 0;
				if (lastPromptedRow >= 0) {
					// Ensure that this row is available for view/display
					final TreePath path = tree.getPathForRow(lastPromptedRow);
					if (path != null) {
						// Start from last prompted row
						startingRow = lastPromptedRow;
					} else {
						startingRow = forward ? 0 : tree.getRowCount()-1;
					}
				} else if (tree.getSelectionCount() > 0) {
					// Start from the last selected row
					final int[] s = tree.getSelectionRows();
					final int n = tree.getSelectionCount();
					if (n != s.length) {
						System.out.println("getSelectionCount()=" + n
								+ ", selected row count=" + s.length);
					}
					startingRow = s[s.length - 1];
				}
				if (forward) {
					if (!searchForward(c, time, startingRow)) {
						return;
					}
				} else {			
					if (!searchBackward(startingRow)) {
						return;
					}
				}

				// Node is not found. Check its parents
				if (searchResultNode == null) {
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) searchStartNode
							.getParent();
					while (parentNode != null) {
						if (matches(parentNode,lettersTypedByUser)) {
							searchResultNode = parentNode;
							break;
						}
						parentNode = (DefaultMutableTreeNode) parentNode
								.getParent();
					}
				}

				if (searchResultNode != null) {
					// Match found. make the node visible by scroll to it
					TreeNode[] nodes = ((DefaultTreeModel) tree.getModel())
							.getPathToRoot(searchResultNode);
					TreePath path = new TreePath(nodes);
					if (path != null) {
						// Set currentFoundPath (used by search node panel) to
						// set box border
						currentFoundPath = path;

						tree.scrollPathToVisible(currentFoundPath);
						// To set the border on the current visible node
						updateUI();
						// this is the last prompted now for next search
						lastPromptedRow = tree.getRowForPath(currentFoundPath);
						lettersFoundFromTyped = lettersTypedByUser;
						
						if (PersonalizableTable.showAgain.isSelected() && e != null) {
							final Rectangle rec = tree.getPathBounds(path);
							final String txt = "\""
									+ Basics.encodeHtml(typedString) + "\"";
							final String title = "Found "
									+ txt
									+ " in node <b>"
									+ Basics.HtmlBody.getHtmlEncoded(searchResultNode
											.toString()) + "</b>; now press:";
							final String msg = "<ul><li>"
									+ SwingBasics.searchDownUp
									+ " for next<font color='red'><b>/</b></font>prev " + txt
									+ " <i>anywhere</i> in this tree<li>"
									+(ungroupedModel.nodeSelectStyle == NodeSelectStyle.ONLY_BY_DRAGGING?"":SwingBasics.searchSelectKeyText+ " to select the found row.<li>")
									
									+ SwingBasics.searchAddSelectKeyText
									+ " to add selection</ul>";
							tree.setToolTipText(Basics.toHtmlUncenteredSmall(
									title, msg));
							ToolTipOnDemand.getSingleton().showLater(tree,
									false, PersonalizableTable.showAgain, rec.x + rec.width+30, rec.y + rec.height+10);
						}
					}
				} else {
					// No result found. Remove the current selection
					currentFoundPath = null;
					lastPromptedRow = -1;
					PopupBasics.beep();
					updateUI();
				}
			}
		}

		DefaultMutableTreeNode searchResultNode = null;
		DefaultMutableTreeNode searchStartNode = null;

		private boolean searchForward(final char c, final long time,
				int startingRow) {
			final int endRow=tree.getRowCount()-1;
			searchResultNode = null;

			// Check if it is a continued characters or a new search character
			if (time - lastTime < timeFactor && c != '\0') {
				typedString += c;
				if ((lettersTypedByUser.length() == 1)
						&& (c == lettersTypedByUser.charAt(0))) {
					// Subsequent same key presses move the keyboard focus to
					// the next
					// object that starts with the same letter.
					// search only the content of the current node and if not
					// found set the next row.
					// Because the rowCount works only with the visible rows.
					TreePath path = tree.getPathForRow(startingRow);
					DefaultMutableTreeNode searchCurrentNode = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					searchResultNode = searchNode(searchCurrentNode,true);
					startingRow++;
				} else {
					lettersTypedByUser = typedString;
					searchFor = typedString.toLowerCase();
					lettersFoundFromTyped = "";
				}
			} else {
				if (c != '\0') {
					typedString = "" + c;
				} else if (Basics.isEmpty(typedString)) {
					return false;
				}
				lettersTypedByUser = typedString;
				searchFor = typedString.toLowerCase();
				lettersFoundFromTyped = "";
				// search only the content of the current node and if not found
				// set the next row.
				// Because the rowCount works only with the visible rows.
				TreePath path = tree.getPathForRow(startingRow);
				DefaultMutableTreeNode searchCurrentNode = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				searchResultNode = searchNode(searchCurrentNode, true);
				if (searchResultNode == null) {
					startingRow++;
				}
			}
			lastTime = time;
			searchStartNode = null;
			boolean startingFromSelection = true;
			if (searchResultNode == null) {
				// if starting row is invalid
				if (startingRow < 0 || startingRow > endRow) {
					startingFromSelection = false;
					startingRow = 0;
				}
				// Retain to search till the start row
				int originalStartingRow = startingRow;
				// Decide on where to start the serach from
				if (startingFromSelection) {
					TreePath path = tree.getPathForRow(startingRow);
					searchStartNode = (DefaultMutableTreeNode) path
							.getLastPathComponent();
				} else {
					searchStartNode = getRoot();
				}
				searchResultNode = searchNode(searchStartNode,false);

				if (startingRow  == endRow) {
					startingRow = -1;
				}

				// Start from current selection to the end and again from the
				// start to the current selection
				while (searchResultNode == null
						&& ++startingRow != originalStartingRow) {
					TreePath path = tree.getPathForRow(startingRow);
					if (path != null) {
						searchStartNode = (DefaultMutableTreeNode) path
								.getLastPathComponent();
						searchResultNode = searchNode(searchStartNode,false);
						if (startingRow == endRow) {
							if (originalStartingRow == 0) {
								// started at 0. so stop
								break;
							}
							// Reset on end of tree
							startingRow = 0;
						}
					} else {
						System.out.println("Un recognized Tree Row: "
								+ startingRow);
						if (originalStartingRow == 0) {
							// started at 0. so stop
							break;
						}
						// Reset on end of tree
						startingRow = 0;
					}
				}
			}
			return true;
		}

		private boolean searchBackward(int startingRow) {
			if (Basics.isEmpty(typedString)) {
				return false;
			}
			lettersTypedByUser = typedString;
			lettersFoundFromTyped = "";
			searchFor = typedString.toLowerCase();
			final TreePath path = tree.getPathForRow(startingRow);
			DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			final TreePath pathRoot = tree.getPathForRow(0);
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) pathRoot
					.getLastPathComponent();


			final Collection<TreeNode> c = getParentAndLeftFirst(rootNode,
					new ArrayList<TreeNode>());
			final TreeNode[] a = c.toArray(new TreeNode[c.size()]);
	        			int i = 0;
			for (final TreeNode tn : a) {
				if (tn == firstNode) {
					break;
				}
				i++;
			}
			final int start = i;
			searchResultNode = null;
			i--;
			for (;; i--) {
				if (i < 0) {
					i = a.length;
					continue;
				}
				if (matches(a[i],searchFor)) {
					searchResultNode = (DefaultMutableTreeNode) a[i];
					break;
				}
				if (i == start) {
					break;
				}
			}
			lastTime = System.currentTimeMillis();
			
			return true;
		}
		

     	/**
		 * Invoked when a key has been pressed.
		 * 
		 * Checks to see if the key event is a navigation key to prevent
		 * dispatching these keys for the first letter navigation.
		 */
    	public void keyPressed(final KeyEvent e) {
     	    if ( isNavigationKey(e) ) {
     		lettersTypedByUser = "";
     		lettersFoundFromTyped = "";
     		typedString = "";
     		lastTime = 0L;
     	    }
     	    if(!e.isAltDown() && !ungroupedModel.allowArrowKeySelection){
     	    	KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
     	    	if(key.getKeyCode() == KeyEvent.VK_UP || key.getKeyCode() == KeyEvent.VK_DOWN
     	    			|| key.getKeyCode() == KeyEvent.VK_LEFT || key.getKeyCode() == KeyEvent.VK_RIGHT){
     	    		e.setKeyCode(KeyEvent.VK_CANCEL);
     	    	}
     	    }
     	    if (ungroupedModel.nodeSelectStyle == NodeSelectStyle.ONLY_BY_DRAGGING && !e.isControlDown() && !e.isMetaDown() && !e.isAltDown()) {
     	   	KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
 	    	if(key.getKeyCode() == KeyEvent.VK_SPACE || key.getKeyCode() == KeyEvent.VK_ENTER){
 	    		e.setKeyCode(KeyEvent.VK_CANCEL);
 	    	}
 	    	
     	    }
    	}

    	public void keyReleased(KeyEvent e) {
    	}
    	
    	private boolean isNavigationKey(KeyEvent event) {
     	    InputMap inputMap = tree.getInputMap(
     	    		JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     	    KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
     	    if (inputMap != null && inputMap.get(key) != null) {
     	    	return true;
     	    }
     	    return false;
     	}  
    	
    	

    	private Collection<TreeNode> getParentAndLeftFirst(
    			TreeNode searchNode, Collection<TreeNode> c){
    		final int n=searchNode.getChildCount();
    		c.add(searchNode);
    		for (int i=0;i<n;i++){
    			final TreeNode tn=searchNode.getChildAt(i);
    			getParentAndLeftFirst(tn, c);    			
    		}
    		return c;
    	}
    	
	    

    	public DefaultMutableTreeNode searchNode(
    			final DefaultMutableTreeNode searchNode,
    			
    			final boolean searchOnlyContents){ 
	        DefaultMutableTreeNode node = null; 
        	

	        //Depth First search. Get the enumeration 
	        Enumeration enumNodes = searchNode.depthFirstEnumeration();
	        
	        
	        //iterate through the enumeration 
	        while(enumNodes.hasMoreElements()){ 
	            //get the node 
	            node = (DefaultMutableTreeNode)enumNodes.nextElement(); 
	            //match the string with the user-object of the node
	            if (searchFor != null) { //Check the 
	            	if (node.toString() != null && (searchOnlyContents ? node != searchNode : true)) {
	            		if(matches(node, searchFor)){ 
    	            		if (node == searchNode) {//&& tree.getModel().getChildCount(node) == 0
    	            			return node;
    	            		}    	            
    	            		
    	            		//Check whether any of this ancestor matches this 
    	            		//as this enumeration returns value in reverse order
    	            		boolean isReachedSearchNode = false;
    	            		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
    	            		while (parentNode != null  && !isReachedSearchNode) {
	            				if (parentNode == searchNode) {
	            					isReachedSearchNode = true;
	            				}	
	            				if (parentNode.toString() != null && 
	            						(searchOnlyContents ? parentNode != searchNode : true)) { 
	            					if(matches(parentNode, searchFor)) { 
	            						node = parentNode;
	            					}
	            				}
    	            			parentNode = (DefaultMutableTreeNode)parentNode.getParent();
    	            		}
   	            			return node;               	            			
    	            	}
	            	}
	            }
	        }
	        	       
	        
	        return null; 
	    }
    	    	
    }



	public TreePath getCurrentFoundPath() {
		return currentFoundPath;
	}

	public void setCurrentFoundPath(TreePath currentFoundPath) {
		this.currentFoundPath = currentFoundPath;
		lettersTypedByUser="";
		lettersFoundFromTyped="";
	}
	enum SelectionOp{
		NONE,
		REMOVE, ADD, SET
	};
	private SelectionOp op;
	private boolean saveForLater;
	private TreePath savedSelectionPath;
	
	private static Color DRAG_BG_COLOR=new Color(153,153,0);
	

	public void setRemoveMode(){
		op=SelectionOp.REMOVE;
	}

	private static final boolean OPTIMIZE=true;
	private final StringBuilder sb=new StringBuilder(500);

	private boolean matchSwitch=true;
	private int matchCount=0;
    private Timer matchTimer;
    
	private boolean matches(final TreeNode node, final String arg) {
		if (Basics.HtmlBody.contains(node.toString().toLowerCase(), arg
				.toLowerCase())) {
			if (DOING_MATCH_TIMER){
			purgeMatchTimer();
			matchTimer = new Timer();
			matchTimer.schedule(new TimerTask() {
				public void run() {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								if (currentFoundPath != null) {
									if (matchSwitch) {
										tree
												.setRowHeight(tree
														.getRowHeight() + 1);
									} else {
										tree
												.setRowHeight(tree
														.getRowHeight() - 1);
									}
									matchSwitch = !matchSwitch;
						            tree.scrollPathToVisible(currentFoundPath);
								}
							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, 250);
			}
			return true;
		}
		return false;
	}
	
	private void expandFound(){
	        if (tree != null) {
	            final TreeNode root = (Node) tree.getModel().getRoot();

	            // Traverse tree from root which is level 1
	            expandFound(new TreePath(root));
	        }
	    }

	    private void expandFound( final TreePath path) {
	            final Node node = (Node) path.getLastPathComponent();
	            final int n=node.getChildCount();
	            for (int i=0;i<n;i++){
	            	final Node childNode=(Node)node.getChildAt(i);
	            	final TreePath tp=path.pathByAddingChild(childNode);
	            	if (childNode.isFound()){
	            		tree.expandPath(path);
	            		if (seeOnlyTokens==null || ungroupedModel.applicationSpecificTreeSearch == null){
	            			expandFound(tp);
	            		}
	            	} else {
	            		expandFound(tp);
	            	}
	            }
	        
	    }

	    void removeSelectionSilently(final TreePath tp){
	    	final boolean prev=adjustingSelections;
	        adjustingSelections=true;	        
	    	tree.getSelectionModel().removeSelectionPath(tp);
	    	adjustingSelections=prev;
	    }
        private Operation delete,remove;        
        public static boolean SHOW_ANOMALY=true;

        public boolean isDragging(){
        	return dragAndDrop != null && dragAndDrop.draggedPath != null;
        }
}

