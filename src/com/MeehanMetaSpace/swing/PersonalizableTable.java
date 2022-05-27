/*
 =====================================================================
  PersonalizableTable.java
  Created by Stephen Meehan
  Copyright (c) 2002
 =====================================================================
 */
package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.BooleanConvertor;
import com.MeehanMetaSpace.ComparableBoolean;
import com.MeehanMetaSpace.Condition;
import com.MeehanMetaSpace.DateTime;
import com.MeehanMetaSpace.DefaultStringConverters;
import com.MeehanMetaSpace.Pel;
import com.MeehanMetaSpace.SearchAndReplace;
import com.MeehanMetaSpace.StringConverter;

public class PersonalizableTable extends JTable implements AncestorListener,
  MouseListener, RotateTable.SpecialCases {

	boolean isEditing=false;
    boolean anticipateHtml = false;
    public void setAnticipateHtml(final boolean ok) {
        anticipateHtml = ok;
    }

    ArrayList<NestedTableCellEditor.Factory> nestedTableFactories = new ArrayList<NestedTableCellEditor.Factory>();

    public void prepareForNestedTables(final int rows, final int columns) {
        nestedTableFactories.add(new NestedTableCellEditor.Factory(rows,
          columns));
    }

    public void prepareForNestedTables(final Object[][] data) {
        nestedTableFactories.add(new NestedTableCellEditor.Factory(data));
    }

    public void prepareForNestedTables(final NestedTableCellEditor.Table table) {
        nestedTableFactories.add(new NestedTableCellEditor.Factory(table));
    }

    public void stopCellEditing() {
        final TableCellEditor tce = getCellEditor();
        if (tce != null) {	
            tce.stopCellEditing();
        }
    }

    static final class CellHighlighter {

        final PersonalizableTable table;

        int[] getSelectedColumns() {
            return modelIndexOfSelectedColumns;
        }

        private final Color 
        	columnSelectionBackground,
        	columnSelectionForeground;

        static Color disabledForeground, 
        			disabledSelectedBackground, disabledSelectedForeground;

        
        private int[] modelIndexOfSelectedColumns = new int[0];
        private ArrayList<Integer> listOfModelIndexes = new ArrayList<Integer>(),
        listOfVisualIndexes = new ArrayList<Integer>();

        private void add(final int modelColumn,final int visualColumn) {
        	listOfModelIndexes.add(new Integer(modelColumn));
            listOfVisualIndexes.add(new Integer(visualColumn));
            if (table.model.columnSelectionListener != null) {
            	table.model.columnSelectionListener.selected(table, table.model.getDataColumnIndex(modelColumn));
            }
        }

        private void remove(final int i) {
        	final Integer modelColumn=listOfModelIndexes.remove(i);
            listOfVisualIndexes.remove(i);
            if (modelColumn != null && table.model.columnSelectionListener != null) {
            	table.model.columnSelectionListener.unselected(table, table.model.getDataColumnIndex(modelColumn));
            }
        }

        void reset() {
            modelIndexOfSelectedColumns = new int[0];
            listOfModelIndexes = new ArrayList<Integer>();
            listOfVisualIndexes = new ArrayList<Integer>();
            table.repaint();
			if (table.scrollPane != null) {
				table.scrollPane.repaint();
			}
        }

        private CellHighlighter(final PersonalizableTable table) {
            this.table = table;
            columnSelectionBackground=Color.LIGHT_GRAY;
            columnSelectionForeground=filterForeground;
        }

        private void move(final int fromIdx, final int toIdx) {
            if (!supressMoveEcho) {
                TableColumnModel tcm = table.getColumnModel();
                int change = toIdx - fromIdx;
                boolean toRight = change > 0;
                for (int i = 0; i < listOfVisualIndexes.size(); i++) {
                    int idx = ((Integer) listOfVisualIndexes.get(i)).intValue();
                    if (idx == fromIdx) {
                        listOfVisualIndexes.set(i, new Integer(toIdx));
                    } else if (toRight) {
                        System.out.print("idx=" + idx + ", fromIdx=" + fromIdx);
                        if (idx < fromIdx) {
                            tcm.moveColumn(idx, idx + change);
                            listOfVisualIndexes.set(i, new Integer(idx + change));
                            System.out.print("...moved...");
                        }
                        System.out.println();
                    } else if (idx > fromIdx) {
                        tcm.moveColumn(idx, idx + change);
                        listOfVisualIndexes.set(i, new Integer(idx + change));
                    }
                }
            }
        }


        private void setColors(
          final Component component,
          final Row row,
          final int visualRowIndex,
          final int modelIndex,
          final boolean isSelected,
          final boolean isDisabled) {
            if (modelIndexOfSelectedColumns.length > 0) { // are columns selected ?
                for (int i = 0; i < modelIndexOfSelectedColumns.length; i++) {
                    if (modelIndexOfSelectedColumns[i] == modelIndex) { // is current column selected?
                    	final Color []bg;
                    	if (isSelected) {
                            if (!isDisabled) {
                            	bg=table.model.getUngroupedModel().getSelectionColorsBasedOnSelectionsInActiveTree(null, row, table.getSelectionForeground(), table.getSelectionBackground(), false);
                            } else {
                            	bg=table.model.getSelectionColorsBasedOnSelectionsInActiveTree(null, row, disabledSelectedForeground, disabledSelectedBackground, false);
                            }
                            component.setBackground(dither(bg[1], 6));
                        } else {
                        	bg=new Color[2];
                        	bg[1]=isDisabled ?
                                    disabledSelectedBackground :
                                        columnSelectionBackground;
                            component.setBackground(bg[1]);
                        	bg[0]=isDisabled ?
                                    disabledSelectedForeground :
                                        columnSelectionForeground;
                        }
                        component.setForeground(bg[0]);
                        return;
                    }
                }
            }
            if (isSelected) {
                if (!isDisabled) {
                	final Color []bg=table.model.getUngroupedModel().getSelectionColorsBasedOnSelectionsInActiveTree(null, row, table.getSelectionForeground(), table.getSelectionBackground(), false);
					component.setBackground(bg[1]);					
                	component.setForeground(bg[0]);
                } else {
                	final Color []bg=table.model.getSelectionColorsBasedOnSelectionsInActiveTree(null, row, disabledSelectedForeground, disabledSelectedBackground, false);
                    component.setBackground(bg[1]);
                    component.setForeground(bg[0]);
                }
                return;
            }

            component.setForeground(isDisabled ? disabledForeground :
                                    _foreground);
            component.setBackground(((visualRowIndex % 2) == 0) ? _background :
            	table.getAlternatingRowColor());
        }

        boolean supressMoveEcho = false;

        void shiftSelectedLeft() {
            final TableColumnModel tcm = table.getColumnModel();
            final int []sortable=table.model.getSelectedModelColumns(true);
            final int lov = sortable.length;
            supressMoveEcho = true;
            for (int i = 0; i < lov; i++) {
            	final int v=SwingBasics.getVisualIndexFromModelIndex(table, sortable[i]);
                tcm.moveColumn(v, 0 + i);
                adjustSelectedColumnsAfterMove();
            }
            supressMoveEcho = false;
            if (table.model.focusVisualRowIndex>=0) {
            	table.scrollToVisible(table.model.focusVisualRowIndex, 0);
            }
        }

        public void shiftSelectedRight() {
            final TableColumnModel tcm = table.getColumnModel();
            final int n = tcm.getColumnCount();
            final int []sortable=table.model.getSelectedModelColumns(true);
            final int lov = sortable.length;
            supressMoveEcho = true;
            for (int i = lov - 1, j = 0; i >= 0; i--, j++) {
            	final int v=SwingBasics.getVisualIndexFromModelIndex(table, sortable[i]);
                tcm.moveColumn(v,n - (1 + j));
                adjustSelectedColumnsAfterMove();
            }
            supressMoveEcho = false;
            if (table.model.focusVisualRowIndex>=0) {
            	table.scrollToVisible(table.model.focusVisualRowIndex, n-1);
            }
        }

        void adjustSelectedColumnsAfterMove() {
            final TableColumnModel tcm = table.getColumnModel();
            for (int i = 0; i < modelIndexOfSelectedColumns.length; i++) {
                final int mi = modelIndexOfSelectedColumns[i];
                final int n = tcm.getColumnCount();
                for (int j = 0; j < n; j++) {
                    if (tcm.getColumn(j).getModelIndex() == mi) {
                        listOfVisualIndexes.set(i, new Integer(j));
                        break;
                    }
                }
            }
        }

        boolean selectColumn(
                final int modelColumn,
                final int visualColumn,
                final MouseEvent event) {
        	return selectColumn(modelColumn, visualColumn, event, false);
        }
        boolean selectColumn(
          final int modelColumn,
          final int visualColumn,
          final MouseEvent event,
          final boolean automaticallyAdd) {
            boolean found = false;
            if (!automaticallyAdd && (event == null
                || (!event.isShiftDown()
                    && !((SwingBasics.usesMacMetaKey()
                          && event.isMetaDown()) || event.isControlDown()
                    )
                )
              )) {
                boolean removeSingle = modelIndexOfSelectedColumns.length == 1
                                       &&
                                       modelIndexOfSelectedColumns[0] ==
                                       modelColumn;
                modelIndexOfSelectedColumns = new int[0];
                listOfModelIndexes = new ArrayList<Integer>();
                listOfVisualIndexes = new ArrayList<Integer>();
                if (removeSingle) {
                    return false;
                }
            }
            if (modelIndexOfSelectedColumns.length == 0) {
                add(modelColumn, visualColumn);
            } else {
                for (int i = 0;
                             !found && i < modelIndexOfSelectedColumns.length;
                             i++) {
                    if (modelIndexOfSelectedColumns[i] == modelColumn) {
                        remove(i);                        
                        if (listOfModelIndexes.size() > 0) {
                            int visualStart = ((Integer) listOfVisualIndexes.
                                               get(0)).
                                              intValue();
                            if (visualStart == visualColumn) {
                                visualStart = ((Integer) listOfVisualIndexes.
                                               get(0)).intValue();
                            }
                        }
                        found = true;
                    }
                }
                if (!found) {
                    if (event == null || !event.isShiftDown()) {
                        add(modelColumn, visualColumn);                        
                    } else { // user is holding shift key to select a contiguous group of columns

                        // where did we start?
                        final int visualStart = ((Integer) listOfVisualIndexes.
                                                 get(0)).
                                                intValue();

                        // does user want this contigous group to be additive to the start?
                        if (!((SwingBasics.usesMacMetaKey() &&
                               event.isMetaDown()) ||
                              event.isControlDown())) {
                            modelIndexOfSelectedColumns = new int[0];
                            listOfModelIndexes = new ArrayList<Integer>();
                            listOfVisualIndexes = new ArrayList<Integer>();
                        }
                        final TableColumnModel tcm = table.getColumnModel();
                        final int change = visualColumn > visualStart ? 1 : -1;
                        for (int i = visualStart; i != (visualColumn + change);
                                     i += change) {
                            Integer candidate = new Integer(tcm.getColumn(i).
                              getModelIndex());
                            if (!listOfModelIndexes.contains(candidate)) {
                                add(candidate, i);
                            }
                        }
                    }
                }
            }
            modelIndexOfSelectedColumns = Basics.toIntArray(listOfModelIndexes);
            table.repaint();
            return!found;
        }
    }


    static String[] getOps(final Class<?> cl) {
        if (cl.equals(String.class) || cl.equals(URL.class)) {
            return Filterable.stringFilterOp;
        }
        if (cl.equals(ComparableBoolean.class) || cl.equals(Boolean.class)) {
            return Filterable.comparableBooleanFilterOp;
        }
        if (cl.equals(Boolean.class)) {
            return Filterable.canNotFilterOp;
        }
        if (cl.equals(JButton.class)){
        	return Filterable.buttonFilterOp;
        }

        return Filterable.numericFilterOp;

    }

    static boolean paintCellAdvice(
      final JComponent component,
      final boolean haveTip,
      final CellAdvice cellAdvice,
      final boolean hasFocus) {
        boolean borderSet = false;
        switch (cellAdvice.type) {
        case CellAdvice.TYPE_ERROR:
            if (COLOR_B_ERROR != null) {
                component.setBackground(COLOR_B_ERROR);
            }
            component.setBorder(hasFocus ? BORDER_ERROR_FOCUS : BORDER_ERROR);
            borderSet = true;
            break;
        case CellAdvice.TYPE_INCOMPLETE:
            if (COLOR_B_INCOMPLETE != null) {
                component.setBackground(COLOR_B_INCOMPLETE);
            }
            component.setBorder(BORDER_INCOMPLETE);
            borderSet = true;
            break;

        }
        if (haveTip) {
            component.setToolTipText(cellAdvice.toolTip);
        }
        return borderSet;
    }

    boolean maybeColumnsAreMoving = false;

    class ColumnListener implements TableColumnModelListener {
        private boolean resized = false, moved = false;
        boolean moving=false;
        int start;
        void reset() {
            resized = false;
            moved = false;
            moving=false;
            if (!maybeColumnsAreMoving){
            	start=model.clickedVisualColumnIndex;
            }
            maybeColumnsAreMoving = true;
        }

        boolean hasChanged() {
        	if (resized) {
    				int i=start;
    				final TableColumn tc = tcm.getColumn(i);
    				final String name = model.modelColumnIdentifiers.get(tc
    							.getModelIndex());
    				final int w = tc.getPreferredWidth();
    				model.setProperty(name + "." + PersonalizableTableModel.PROPERTY_HEADER_WIDTH, w);
    				return true;
        	}
            return moved;
        }

        public void columnAdded(TableColumnModelEvent e) {
        }

        public void columnRemoved(TableColumnModelEvent e) {
        }

        public void columnMoved(final TableColumnModelEvent e) {
            int fromIndex = e.getFromIndex();
            int toIndex = e.getToIndex();
            moving=true;
            if (model.getUngroupedModel().columnMovedListener!=null) {
            	model.getUngroupedModel().columnMovedListener.actionPerformed(PersonalizableTable.this,PersonalizableTable.this.getTableHeader().getDraggedDistance(),false);
            }
            // fromIndex and toIndex identify the range of columns
            // being moved. In the case of a user dragging a column,
            // this event is fired as the column is being dragged
            // to its new position. Also, if the column displaces
            // another during dragging, the fromIndex and toIndex
            // show its new position; this new position is only
            // temporary until the user stops dragging the column.
            if (fromIndex != toIndex) {
                //System.out.println("from="+fromIndex+", to="+toIndex);
                moved = true;
                cellHighlighter.move(fromIndex, toIndex);
            }
        }

        public void columnMarginChanged(ChangeEvent e) {
            // The width of some column has changed.
            // The event does not identify which column.
            resized = true;
            if (maybeColumnsAreMoving && model.getUngroupedModel().columnResizedListener!=null) {
            	model.getUngroupedModel().columnResizedListener.actionPerformed(PersonalizableTable.this);
            }

        }

        public void columnSelectionChanged(ListSelectionEvent e) {
            // See e963 Listening for Selection Events in a JTable Component
        }
    }


    public void resizeAndRepaint() {
        super.resizeAndRepaint();
    }


    void editFirstEditable(
      final int visualRowIndex,
      final int tryThisVisualColumnIndexFirstOtherwiseStartAtZero,
      final EventObject anEvent,
      final boolean useJTableMethod,
      final boolean select) {
        int col = tryThisVisualColumnIndexFirstOtherwiseStartAtZero;
        final int cnt = getColumnCount();
        if (!isCellEditable(visualRowIndex, col)) {
            for (col = 0; col < cnt; col++) {
                if (isCellEditable(visualRowIndex, col)) {
                    break;
                }
            }
        }
        if (col != tryThisVisualColumnIndexFirstOtherwiseStartAtZero){
        	
        if (col < cnt) {
				if (useJTableMethod) {
					PersonalizableTable.super.editCellAt(
							visualRowIndex, col, anEvent);
				} else {
					editCellAt(visualRowIndex, col, anEvent);
				}
			}
		}
		if (select) {
			if (col < cnt) {
				setColumnSelectionInterval(col, col);
			}
			if (visualRowIndex >= 0)
				setRowSelectionInterval(visualRowIndex, visualRowIndex);
		}        
    }
    public boolean isInitializingCellEditor(final int dataColumnIndex) {
    	return initializingUpCellEditorForDataColumnIndex==dataColumnIndex;
    }
    
    boolean isInitializingUpCellEditor = false;
    private int initializingUpCellEditorForDataColumnIndex=-1;
    
    boolean _editCellAt(
      final int _visualRowIndex,
      final int visualColumnIndex,
      final EventObject anEvent) {
    	if (!model.invokingNewProgrammatically && anEvent instanceof KeyEvent){
    		int code=((KeyEvent) anEvent).getKeyCode();
    			if (code== KeyEvent.VK_ESCAPE 
    					|| (code >= KeyEvent.VK_F3 && code <= KeyEvent.VK_F12)
    					|| (code >= KeyEvent.VK_F13 && code <= KeyEvent.VK_F24)) {
    	    		return false;
    				
    			}
    			final int modifiers=((KeyEvent)anEvent).getModifiers();
    			if ( (modifiers & InputEvent.ALT_MASK) !=0) {
    				return false;
    			}
    			if ( (modifiers & InputEvent.META_MASK) !=0) {
    				return false;
    			}
    			if ( (modifiers & InputEvent.CTRL_MASK) !=0) {
    				return false;
    			}
    	}
        isInitializingUpCellEditor = true;
        initializingUpCellEditorForDataColumnIndex=model.getDataColumnIndexFromVisualIndex(visualColumnIndex);
        lastTableEdited=this;
        if (_visualRowIndex == model.getRowCountMinusEmptyLastRowForCreating()) {
            if (model.showingEmptyLastRowForCreating()) {
                if (!(anEvent instanceof MouseEvent) ||
                    ((MouseEvent) anEvent).getClickCount() >= 2) {
                	final boolean prev=supressUpdateUIBecauseItKillsCursorPositionAndOtherStuff;
                	if (!scrollPane.getVerticalScrollBar().isVisible()) {
						scrollPane
								.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					}
                	if (Basics.isPowerPC || !SwingBasics.isMac){
                		supressUpdateUIBecauseItKillsCursorPositionAndOtherStuff=true;
                	}
                	creatingWhileCellEditing=this;
                	final boolean b=model.createInPlace();
                	creatingWhileCellEditing=null;
                	supressUpdateUIBecauseItKillsCursorPositionAndOtherStuff=prev;
                    if (b) {
                        // could have expanded the # of rows
                        final int n = model.
                                      getRowCountMinusEmptyLastRowForCreating();
                        if (n - 1 != _visualRowIndex) {
                        	newRowCreatedWhileCellEditing= n - 1;
                        } else {
                        	newRowCreatedWhileCellEditing = _visualRowIndex;
                        }
                        if (model.dataSource.canCreateInPlaceWithKeyStroke()) {
                            editFirstEditable(newRowCreatedWhileCellEditing, visualColumnIndex,anEvent, true, true);

                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    editFirstEditable(newRowCreatedWhileCellEditing,
                                      visualColumnIndex,
                                      anEvent, true, true);
                                }
                            });
                        }
                    } else {
                        isInitializingUpCellEditor = false;
                        initializingUpCellEditorForDataColumnIndex=-1;
                        return false;
                    }
                }
            }
        }
        lastEditedRow=_visualRowIndex;
        lastEditedColumn=visualColumnIndex;
        lastCellEditEvent=anEvent;
        final boolean b = super.editCellAt(_visualRowIndex, visualColumnIndex, anEvent);
        ComboCellEditor.considerSingleClick = false;
        if (!b && (!(anEvent instanceof MouseEvent))) {
        	final Collection<String>c=model.getReadOnlyExplanation(_visualRowIndex, visualColumnIndex);
        	if (!Basics.isEmpty(c)) {
        		final String msg=Basics.toHtmlErrorUncentered("You can't edit this because", Basics.toUlHtml(c));
        		model.showTextInPopupWindow(msg, false, false, false, Integer.valueOf(-15), null, anEvent instanceof MouseEvent, true, null);
        	}
         }
        isInitializingUpCellEditor = false;
        initializingUpCellEditorForDataColumnIndex=-1;
        return b;
    }


    private int lastEditedRow, lastEditedColumn;
    private EventObject lastCellEditEvent;
    
    public boolean isLastCellEditByMouseEvent(){
    	return lastCellEditEvent instanceof MouseEvent;
    }
    public void editLastCellLater(){
    	if (lastCellEditEvent != null){
    	SwingUtilities.invokeLater(new Runnable(){
    		public void run(){
    			SwingUtilities.invokeLater(new Runnable(){
    	    		public void run(){
    	    			AutoComplete.CellEditor.showPopupOnFirstFocus=true;
    	    			_editCellAt(lastEditedRow, lastEditedColumn, lastCellEditEvent);
    	    		}
    	    	});
    		}
    	});
    	}
    }
    
    public boolean editCellAt(
      final int visualRowIndex,
      final int visualColumnIndex,
      final EventObject e) {
        return _editCellAt(visualRowIndex, visualColumnIndex, e);
    }

    public boolean editCellAt(
      final int visualRowIndex,
      final int visualColumnIndex) {
        // trusting knowledge of JTable's implementation for this
        return _editCellAt(visualRowIndex, visualColumnIndex, eventObject);
    }

    TableColumnModel tcm;
    final PersonalizableTableModel model;
    private Boolean useDisabling;
    private PersonalizableTableModel ungroupedModel;


    public PersonalizableTableModel getPersonalizableTableModel() {
        return (PersonalizableTableModel) getModel();
    }


    private void setAutoResize() {
    	if (model.getUngroupedModel().allowAutoResizeControl) {
        boolean autoResize = model.getProperty(PersonalizableTableModel.
                                               PROPERTY_AUTO_RESIZE, false);
        this.setAutoResizeMode(autoResize ? JTable.AUTO_RESIZE_ALL_COLUMNS :
                               JTable.AUTO_RESIZE_OFF);
    	}
    }

    JScrollPane scrollPane;
    
    public ImageIcon getBackgroundImage(){
    	return backgroundImage;
    }
    private boolean justTurnedBackgroundOff=false;
    ImageIcon backgroundImage=null;
    public void setBackgroundImage(final ImageIcon image, final int []dataColumnIndexesThatMustBeOpaue){
    	justTurnedBackgroundOff=image ==null;
    	backgroundImage=image;
    	columnsThatMustBeOpaque=dataColumnIndexesThatMustBeOpaue;
    	updateUI();
    }
    

    int[]columnsThatMustBeOpaque;
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		final Component c = super.prepareRenderer( renderer, row, column);
		// We want renderer component to be transparent so background image is visible
		if( backgroundImage != null && c instanceof JComponent ) {	
			final boolean opaque=model.getModelShowing().
            isBackgroundOkWithImg(c.getBackground());
			if (opaque || Basics.contains(getSelectedRows(), row)){
				((JComponent)c).setOpaque(true);
			} else if (columnsThatMustBeOpaque == null ){
				((JComponent)c).setOpaque(false);
			} else {
				final int dc=model.getDataColumnIndexFromVisualIndex(column);
				if (!Basics.contains(columnsThatMustBeOpaque, dc)){
					((JComponent)c).setOpaque(false);
				} else {
					((JComponent)c).setOpaque(true);
				}
			}
 		}  else if (justTurnedBackgroundOff && c instanceof JComponent ){
			((JComponent)c).setOpaque(true);
		}
		if(ungroupedModel != null){
			ungroupedModel.dataSource.prepareRenderer((JComponent)c, row, column);
		}
		return c;
	}


    public JScrollPane makeHorizontalScrollPane() {
        setAutoResize();
        scrollPane = new JScrollPane(this){
        	public void paint(Graphics g)     	{
        		super.paint( g );
        		SwingBasics.draw(backgroundImage, g, this);
        	}

        };
        scrollPane.addMouseListener(this);
        scrollPane.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL));
       setParentDragAndDrop();
        return scrollPane;
    }

    public void scrollRowAndColToVisible(int rowIdx, final int colIdx) {
        if (rowIdx >= 0 && rowIdx < model.getRowCount()) {
            final Object o = getParent();
            if (o instanceof JViewport) {
                final JViewport viewport = (JViewport) o;
                for (int i = 0; i < 3 && rowIdx >= 0; i++) {
                    rowIdx--;
                }
                final Rectangle rect = getCellRect(rowIdx, colIdx, true);
                viewport.setViewPosition(rect.getLocation());
            }
        }
    }


	public void ensureColumnVisible(final int visualColumnIdx) {
		final Object o = getParent();
		if (o instanceof JViewport) {
			final JViewport viewport = (JViewport) o;
			final Point p = viewport.getViewPosition();
			int rowIdx = rowAtPoint(p);
			final Rectangle rect = getCellRect(rowIdx, visualColumnIdx, true);
			SwingBasics.scrollRectToVisibleForAllParents(this, rect);
		}
	}


    private StringConverterRenderer baseRenderer;

    public PersonalizableTable(final PersonalizableTableModel model) {
        this(model, null);
    }
    
    private String beforeSearchToolTip;
    private void setSearchToolTip(final String toolTip, final int wOffset, final int hOffset, final boolean usingFindWindow){
    	if (beforeSearchToolTip==null){
    		beforeSearchToolTip=searchStatusComponent.getToolTipText();
    	}
    	searchStatusComponent.setToolTipText(toolTip);
		final int old=ToolTipOnDemand.getSingleton().getDismissDelay();
		ToolTipOnDemand.getSingleton().setDismissDelay(usingFindWindow?7000:14000);
		ToolTipOnDemand.getSingleton().show(
				searchStatusComponent, false, wOffset, hOffset,
						usingFindWindow && hasTipShown?null:showAgain, null);
		ToolTipOnDemand.getSingleton().setDismissDelay(old);		
		hasTipShown=true;
    }
    
    void resetAsNotFound(){
		isAutoCompleteItemFound=false;
		autoCompleteVisualRowIndex=-1;
		autoCompleteSearchEntry="";
		autoCompleteDataColumnIndex=-1;
		if (searchStatusComponent!=null && beforeSearchToolTip != null){
			searchStatusComponent.setToolTipText(beforeSearchToolTip);
		}
    }
    
    private boolean isFound(){
		return isAutoCompleteItemFound && autoCompleteVisualRowIndex>=0;
    }

    public PersonalizableTable(final PersonalizableTableModel model,
                               final GroupedDataSource gds) {
        super(model);
        setAutoscrolls(false);
        this.model = model;
        
        header = getTableHeader();
        initSortHeader();
        showHorizontalLines = false;
        showVerticalLines = false;
        setupObjectEditor();
        setupDateEditor();
        setupDateTimeEditor();
        setupFloatEditor();
        setupIntegerEditor();
        setupLongEditor();
        setDefaultRenderer(Condition.class,
                           new StringConverterRenderer(Condition.class));
        baseRenderer = new StringConverterRenderer(Object.class);
        final BooleanRenderer bfr = new BooleanRenderer(true);
        setDefaultRenderer(ComparableBoolean.class, bfr);
        setDefaultRenderer(BooleanConvertor.class, bfr);
        
        setDefaultRenderer(Boolean.class, bfr);
        setDefaultRenderer(URL.class, new UrlRenderer());
        setDefaultEditor(URL.class, new UrlEditor());

        setDefaultRenderer(File.class, new FileRenderer());
        setDefaultEditor(File.class, new FileEditor());
        
        setDefaultRenderer(JButton.class, new ButtonRenderer());
        setDefaultEditor(JButton.class, new ButtonEditor());

        setDefaultEditor(ComparableBoolean.class,
                         new ComparableBooleanCellEditor(new JCheckBox()));
        setDefaultEditor(BooleanConvertor.class,
                new ComparableBooleanCellEditor(new JCheckBox()));
        cellHighlighter = new CellHighlighter(this);
        model.setTable(this);
        showVerticalLines = true;
        /*if (SwingBasics.isQuaQuaLookAndFeel()){
        final int gapWidth = 3;
        final int gapHeight = 5;
        setIntercellSpacing(new Dimension(gapWidth, gapHeight));
        }*/
        addAncestorListener(this);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);

        tcm.addColumnModelListener(columnListener);
        getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	public void valueChanged(final ListSelectionEvent e) {
                //nore extra messages.
            if (!PersonalizableTable.this.model.initializingPicks) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                scrollAllParentViewportsIfSelectingByKeyboard();
            }
        }});
        final ListSelectionModel rowSM = getSelectionModel();        
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e) {
                //Ignore extra messages.
                if (!PersonalizableTable.this.model.initializingPicks) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    selectedSinceLastSearch=true;
                    if (model.getModelType() !=
                        PersonalizableTableModel.TYPE_GROUPED &&
                        model.getGroupOption() !=
                        PersonalizableTableModel.GROUP_BY_TREE) {
                        model.getTablePickHandler().notifySelection(null/*no need to problem inspect*/);
                    }
                    model.fireListSelection(e);
                    scrollAllParentViewportsIfSelectingByKeyboard();
                    if (!FocusFreeze.isFrozen()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (!FocusFreeze.isFrozen()) {
									requestFocus();
								}
							}
						});
					}
               }
            }
        });
        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                model.popup();
            }
        }

        , KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK),
          JComponent.WHEN_FOCUSED);

        
        abstract class _Action implements SwingBasics.AutoCompleteActionListener{
        	public boolean isFound(){
        		return PersonalizableTable.this.isFound();
        	}
        	
        	public void resetAsUnfound(){
        		PersonalizableTable.this.resetAsNotFound();
        	}
        }        
        SwingBasics.registerAutoCompleteKeyboardAction(
        		this,
        		new _Action() {
			public void actionPerformed(final ActionEvent ae) {
				if (!isRowSelected(autoCompleteVisualRowIndex)) {
					setRowSelectionInterval(autoCompleteVisualRowIndex,
							autoCompleteVisualRowIndex);
				} else {
					removeRowSelectionInterval(autoCompleteVisualRowIndex,
							autoCompleteVisualRowIndex);
				}
				autoCompleteSearchEntry="";
				autoCompleteDataColumnIndex=-1;
			}
		}, new _Action() {
			public void actionPerformed(final ActionEvent ae) {
				if (!isRowSelected(autoCompleteVisualRowIndex)) {
					addRowSelectionInterval(autoCompleteVisualRowIndex,
							autoCompleteVisualRowIndex);
				} else {
					removeRowSelectionInterval(autoCompleteVisualRowIndex,
							autoCompleteVisualRowIndex);
				}
				autoCompleteSearchEntry="";
				autoCompleteDataColumnIndex=-1;
			}
		}, searchDown,searchUp,searchRight,searchLeft, false);

        model.initActions(gds);
        setSurrendersFocusOnKeystroke(true);
        // Disable autoCreateColumnsFromModel to prevent
        // the reappearance of columns that have been removed but
        // whose data is still in the table model
        setAutoCreateColumnsFromModel(false);
        //supressEnter() is now handled by registerAutoCompleteKeyboardAction 
		// Allow escape key to cancel the window
        //supressEscape();
        getTableHeader().setReorderingAllowed(model.getUngroupedModel().columnsAreDynamic);
        getTableHeader().setResizingAllowed(model.getUngroupedModel().columnsAreDynamic);
        
    }
    public void setColumnModel(final TableColumnModel columnModel) {
        columnModel.addColumnModelListener(columnListener);
        super.setColumnModel(columnModel);
    	
    }

    void echoAction(
      final JMenuItem menuItem,
      final ActionListener anAction,
      final int keyEvent,
      int inputEvent,
      final char mnemonic) {
    	final KeyStroke ks;
    	if ( (inputEvent & InputEvent.CTRL_MASK) != 0){
    		int macInputEvent = inputEvent & ~InputEvent.CTRL_MASK;
    		ks=SwingBasics.getKeyStrokeWithMetaIfMac(keyEvent, inputEvent, macInputEvent);
    	} else {
			ks=KeyStroke.getKeyStroke(keyEvent, inputEvent);
    	}
        echoAction(menuItem, anAction,
                   ks,
                   mnemonic);
    }

    void echoAction(
      final JMenuItem menuItem,
      final ActionListener anAction,
      final KeyStroke keyStroke,
      final char mnemonic) {
        SwingBasics.echoAction(this, menuItem, anAction, keyStroke, mnemonic);
        //header.registerKeyboardAction(anAction, keyStroke, JComponent.WHEN_FOCUSED);
    }

    final ColumnListener columnListener = new ColumnListener();
    
    static {
        resetDefaultFonts();
        initColors();
    }
    
   public static void resetDefaultFonts(){
        FONT_BASE = UIManager.getFont("Table.font");
        final int size=FONT_BASE.getSize();
        System.out.println("Table.font size=="+size);
		
        baseEditableFont = new Font(FONT_BASE.getName(), Font.BOLD,
                                    size);
        specialFont = new Font(FONT_BASE.getName(), Font.ITALIC,
                               size);
        smallFont = new Font(FONT_BASE.getName(), Font.PLAIN, size-3);
        smallEditableFont = new Font(FONT_BASE.getName(), Font.BOLD, size-3);
        specialEditableFont = new Font(FONT_BASE.getName(), Font.ITALIC | Font.BOLD,
                                       size);
        FONT_FILTER= new Font(
        	      FONT_BASE.getFontName(),
        	      Font.ITALIC | Font.BOLD,
        	      size+2);
    }

   static Color specialForeground=Color.blue;
   static Font FONT_BASE, baseEditableFont;
   private static Font smallFont, smallEditableFont, specialFont,
    specialEditableFont;

    final CellHighlighter cellHighlighter;

    public void ancestorRemoved(AncestorEvent fe) {
    }

    public void ancestorMoved(AncestorEvent e) {
    }

    void refreshPosition() {
        if (model.getUngroupedModel().usePriorSelectedRow) {
            int idx = model.getProperty(PersonalizableTableModel.
                                        PROPERTY_SELECTED_ROW, -1);
            if (idx >= 0 && idx < model.getRowCount()) {
                setRowSelectionInterval(idx, idx);
                scrollRowAndColToVisible(idx,0);
                //		   System.out.println(model.propertyPrefix +"  refreshing position " + idx);
                header.resizeAndRepaint();
                return;
            }
        }
        //Below entry is removed as it causes auto selection/addition 
        //if the filter returns only one match (Stainset)
        /*if (model.getDataRowCount()==1) {
        	model.select(0, true);
            
        }*/
    }

    public static boolean isAddingAncestor = false;
    public boolean buildingTreeMessage = false;

    public static boolean isDisposing = false;
    public void ancestorAdded(final AncestorEvent fe) {
        if (!isAddingAncestor &&
            model == model.getUngroupedModel() &&
            model.firstTimeAncestorWasAdded) {
        	Runnable run = new Runnable() {
				public void run() {
					final FocusFreeze ff=new FocusFreeze();
					refreshPosition();
					ff.restorePrevValue();
					final int group = model.getGroupOption();
					if (group == PersonalizableTableModel.NO_GROUPING) {
						model.handleTabsWhenFirstDisplaying();
					}

				}
			};
        	if (isDisposing) {
        		run.run();
        	}
        	else {
        		SwingUtilities.invokeLater(run);
        	}        	
			isAddingAncestor = true;
            final boolean hasCustomGroupView = model.hasCustomGroupView();
            final int group = model.getGroupOption();
            if (buildingTreeMessage ||
                group != PersonalizableTableModel.NO_GROUPING ||
                hasCustomGroupView
              ) {
                if (buildingTreeMessage) {
                    model.showOneMomentDisplay();
                }
                final Boolean prevUseDisabling=useDisabling;
                useDisabling=Boolean.FALSE;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (buildingTreeMessage) {
                            model.hideOneMomentDisplay();
                        }
                        if (model.groupedDataSource == null) {
                            if (model.groupOption == PersonalizableTableModel.GROUP_BY_TREE || hasCustomGroupView){
                                if (model.dataSource.getDataRows().size()>model.treeOneMomentThreshold){
                                    model.showOneMomentDisplay();
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            if (model.groupIfNecessary()) {
                                                if (invokeAfterFirstGrouping != null) {
                                                    invokeAfterFirstGrouping.run();
                                                }
                                            }
                                            useDisabling=prevUseDisabling;
                                            model.hideOneMomentDisplay();
                                        }
                                    });
                                } else {
                                    if (model.groupIfNecessary()) {
                                        if (invokeAfterFirstGrouping != null) {
                                            invokeAfterFirstGrouping.run();
                                        }
                                    }
                                }
                            }
                            model.firstTimeAncestorWasAdded = false;

                        }
                        useDisabling=prevUseDisabling;
                    }
                });
                //    }
            }else{
                model.firstTimeAncestorWasAdded = false;

            }
            
            isAddingAncestor = false;
        }
    }

    public Runnable invokeAfterFirstGrouping=null;

    public void removeColumn(final TableColumn tc) {
        if (tc.equals(header.getDraggedColumn())) { //addresses issue with Mac JVM 1.4.1
            header.setDraggedColumn(null);
        }
        cellHighlighter.reset();
        super.removeColumn(tc);
    }

    final JTableHeader header;
    public static final Color YELLOW_STICKY_COLOR = new Color(255, 255, 188
    		);

    private static Color _selectionForeground, _selectionBackground, 
    	_foreground, _background, 
    	filterBackground, filterForeground, rotateFilterBackground;
    
    public Color getSelectionForeground() {
        return _selectionForeground;
    }

    public Color getSelectionBackground() {
        return _selectionBackground;
    }

    public Color getForeground() {
        return _foreground;
    }

    public Color getBackground() {
        return _background;
    }

    public static void initColors(){
    	_selectionBackground=UIManager.getColor("Table.selectionBackground");
    	_selectionForeground=UIManager.getColor("Table.selectionForeground");
    	_background=UIManager.getColor("Table.background");
    	_foreground=UIManager.getColor("Table.foreground");
    	filterBackground = UIManager.getColor("TableHeader.background");
    	filterForeground = UIManager.getColor("TableHeader.foreground");
    	rotateFilterBackground = UIManager.getColor("Table.background");
        CellHighlighter.disabledForeground = UIManager.getColor("Label.disabledForeground");
        CellHighlighter.disabledSelectedBackground = UIManager.getColor("TableHeader.background");
        CellHighlighter.disabledSelectedForeground = UIManager.getColor("TableHeader.foreground");

    }
    static Font FONT_FILTER = new Font(
      SwingBasics.FONT_FACE_FAVORITE.getFontName(),
      Font.ITALIC | Font.BOLD,
      SwingBasics.FONT_FACE_FAVORITE.getSize()+1);

    protected void initSortHeader() {
        header.setDefaultRenderer(new HeaderRenderer());
        header.addMouseListener(this);        
        header.addMouseMotionListener(headerMouseOver);
        //SwingBasics.addMouseOver(header, true, false);
        tcm = this.getColumnModel();
        computeHeaderLines(model.getDataColumnIndexesThatAreVisible(this));
        final int sz=this.getFont().getSize();
        final int rSz=(int)(2*sz);
        setRowHeight(rSz);
        addMouseListener(this);
    }

    public void mouseReleased(final MouseEvent event) {
        model.handleMouseEvent(event, true);
        if (model.groupedDataSource != null && model.groupedDataSource.myMouseListener != null){
        	model.groupedDataSource.myMouseListener.handleDraggingIfNecessary();
        } else if (model.columnFreezer.mouseAdapter != null){
        	model.columnFreezer.mouseAdapter.handleDropIfNecessary();
        }
        model.fireListSelection();
    }

    public void mousePressed(final MouseEvent event) {
        model.handleMouseEvent(event, false);
    }

    public void mouseClicked(final MouseEvent event) {}

    public void mouseEntered(final MouseEvent event) {
        /*System.out.println("mouse entered");*/
    }

    public void mouseExited(final MouseEvent event) {
    	headerMouseOver.mouseExited(event);
    }

    public static final Icon NONSORTED = new Arrow(Arrow.NONE);
    final static String filterValueToolTip = "Enter a value to filter on";
    final static String filterOpToolTip = "Select an operator to filter on";
    public final static Color COLOR_B_INCOMPLETE = null; //new Color(0xFF, 0xF0, 0xF5, 0xFF);
    public final static Color COLOR_B_ERROR = null; //new Color(0xFF, 0xE4, 0xE1, 0xFF);
    public final static Color COLOR_F_INCOMPLETE = Color.cyan;
    public final static Color COLOR_F_ERROR = SystemColor.red;
    public final static Border
      BORDER_DEFAULT = UIManager.getBorder("Table.focusCellHighlightBorder"),
    BORDER_YELLOW = new LineBorder(Color.yellow, 2, true),
    BORDER_FOCUS_THIN = new LineBorder(Color.yellow, 1, true),
    BORDER_FOCUS = new LineBorder(Color.yellow, 4, true),
    BORDER_EMPTY = BorderFactory.createEmptyBorder(),
    BORDER_ERROR = new LineBorder(COLOR_F_ERROR, 1, true),
    BORDER_ERROR_FOCUS = new LineBorder(new Color(255, 153, 204), 3, true),
    BORDER_INCOMPLETE = new LineBorder(COLOR_F_INCOMPLETE, 2, true);
    
    static Border BORDER_MAJOR=new BevelBorder(
      BevelBorder.RAISED,
      Color.WHITE,
      Color.BLACK) {
        public Insets getBorderInsets(Component c) {
            return new Insets(3, 3, 6, 6);
        }
    };


    final static int ROW_IDX_FILTER_VALUE = 1, ROW_IDX_FILTER_OP = 0;
    final CellAdvice cellAdvice = new CellAdvice();
    Set<Integer> smallFontDataColumnIndex = new HashSet<Integer> ();
    
    public void addSmallFontDataColumnIndex(final int dataColumnIndex) {
        smallFontDataColumnIndex.add(dataColumnIndex);
    }

    Set<Integer> specialFontDataColumnIndex = new HashSet<Integer> ();
    public void clearSpecialFontDataColumnIndexes() {
    	specialFontDataColumnIndex.clear();
    }
    
    public void addSpecialFontDataColumnIndex(final int dataColumnIndex) {
        specialFontDataColumnIndex.add(dataColumnIndex);
    }
    public void removeSpecialFontDataColumnIndex(final int dataColumnIndex) {
        specialFontDataColumnIndex.remove(dataColumnIndex);
    }

    void decorate(
      final JTable table,
      final JComponent component,
      final int visualRowIndex,
      final int modelColumnIndex,
      final boolean isSelected,
      final boolean hasFocus) {
    	if (ungroupedModel==null){
    		ungroupedModel=getUngroupedModel();
    	}
        final Row row = model.getRowAtVisualIndex(visualRowIndex);
        cellAdvice.clear();
        if (hasFocus) {
        	model.refocus(visualRowIndex, modelColumnIndex);
        }
        boolean borderSet = false;
        if (model.showFilterUI && visualRowIndex == ROW_IDX_FILTER_VALUE) {
            component.setBackground(table instanceof RotateTable ?
                                    rotateFilterBackground : filterBackground);
            component.setForeground(filterForeground);
            component.setFont(FONT_FILTER);
            component.setToolTipText(filterValueToolTip);
        } else if (model.showFilterUI && visualRowIndex == ROW_IDX_FILTER_OP) {
            component.setBackground(table instanceof RotateTable ?
                                    rotateFilterBackground : filterBackground);
            component.setForeground(filterForeground);
            component.setFont(FONT_FILTER);
            component.setToolTipText(filterOpToolTip);
        } else {
            final boolean isEditable = model.isEditable(
              false,
              visualRowIndex,
              modelColumnIndex, false);
            final boolean small =
                smallFontDataColumnIndex.size() > 0 &&
                smallFontDataColumnIndex.contains(model.getDataColumnIndex(
                modelColumnIndex));
            final boolean special =
              specialFontDataColumnIndex.size() > 0 &&
              specialFontDataColumnIndex.contains(model.getDataColumnIndex(
              modelColumnIndex));
            if (isEditable) {
                if (!special) {
					if (small) {
						component.setFont(smallEditableFont);
					} else {
						component.setFont(baseEditableFont);
					}
                } else {
                	component.setForeground(specialForeground);
                    component.setFont(specialEditableFont);
                }
                if (component instanceof AbstractButton){
                	( (AbstractButton)component).setEnabled(true);
                }
            } else {
                if (!special) {
                	if (small){
                    	component.setFont(smallFont);
                    }else{
                    	component.setFont(FONT_BASE);
                    }
                } else {
                	component.setForeground(specialForeground);
                    component.setFont(specialFont);
                }
                if (component instanceof AbstractButton){
                	( (AbstractButton)component).setEnabled(false);
                }
            }
            if (useDisabling==null){
                useDisabling=ungroupedModel.dataSource instanceof PersonalizableDataSource.CanDisable && ungroupedModel.useDisabling;
            }
            if (useDisabling ) {
                if (row != null && (ungroupedModel.hasBeenShownAtSomePoint() 
                		|| 
                		(model.getGroupOption() == PersonalizableTableModel.NO_GROUPING && !model.hasCustomGroupView()))) {
                    final String disabledText = ungroupedModel.getDisabledText(row);
                    if (disabledText != null) {
                        cellHighlighter.setColors(component, row, visualRowIndex,
                                                  modelColumnIndex,
                                                  isSelected, true);
                        component.setToolTipText(disabledText);
                        component.setBorder(BORDER_EMPTY);
                        return;
                    }
                }

            }
            else {
                if (row != null && !row.isActive()) {
                	cellHighlighter.setColors(component, row, visualRowIndex,
                            modelColumnIndex,
                            isSelected, true);
					component.setToolTipText(ANOMALY_INACTIVE);
					component.setBorder(BORDER_EMPTY);
					return;
                }
            }
            boolean haveTip = false;
            if (!maybeColumnsAreMoving) {
                haveTip = model.setCellAdvice(visualRowIndex, modelColumnIndex,
                                              cellAdvice);
                //System.out.print((advices++)+", ");
            }
            cellHighlighter.setColors(component, row, visualRowIndex, modelColumnIndex,
                                      isSelected, ungroupedModel.isShownAsDisabled());
            component.setToolTipText(null);
            borderSet = paintCellAdvice(component, haveTip, cellAdvice,
                                        hasFocus);
        }
        if (!borderSet) {
            if (hasFocus) {
                //System.out.println( "setting focus at " + row + ", "+modelIndex);
                component.setBorder(BORDER_YELLOW);
            } else {
                component.setBorder(BORDER_EMPTY);
            }
        }
        
        if (ungroupedModel.dataSource.isDecorator()) {
        	final TableCellContext context=new TableCellContext(model, model.getDataColumnIndex(modelColumnIndex), row);
        	ungroupedModel.dataSource.decorate(context, component, isSelected, hasFocus);
        }
    }

    public final static String ANOMALY_INACTIVE="This item is inactive/hidden.  Thus you can not pick it for any<br> new situation or circumstance." ;
    int advices = 0;
    JLabel sizeInfo;
    
    final Border foundUnselectedBorder = new LineBorder(UIManager.
  	      getColor("Table.textForeground"), 1, true);
    
    public class StringConverterRenderer extends DefaultTableCellRenderer {
        private final StringConverter sc;
        private int horizontalAlignment=JLabel.LEFT;
        public StringConverterRenderer(final StringConverter sc, final Class<?> cl) {
            this.sc = sc;
            if (cl.equals(Integer.class) || cl.equals(Long.class) || cl.equals(Float.class)) {
            	horizontalAlignment=JLabel.RIGHT;
            } else {
            	horizontalAlignment=sc.getHorizontalAlignment();
            }
        }

        @SuppressWarnings("unchecked")
		public StringConverterRenderer(final Class cl) {
            sc = (StringConverter) DefaultStringConverters.get(cl);
            if (sc == null) {
                throw new UnsupportedOperationException(
                  "No string converter to support " + cl.toString());
            }
        }
        
        String toString(final Row row, final int dataColumnIndex) {
        	final Object p_value=row.get(dataColumnIndex);
			if (p_value == null) {
				return "";
			}
			final Object r_value = row.getRenderOnlyValue(dataColumnIndex, false, false);
			Object value = sc.toString(r_value == null ? p_value : r_value);
			if (value == null) {
				return "";
			}
			return value.toString();
		}
         
       
        public Component getTableCellRendererComponent(final JTable table, final Object p_value,
				final boolean isSelected, final boolean hasFocus, final int row, final int visualColumnIndex) {
			if (visualColumnIndex >= 0 
					&& model.allowsTableShowing
					&& (model.hasBeenShownAtSomePoint()
							|| (model.getGroupOption() == PersonalizableTableModel.NO_GROUPING 
									&& !model.hasCustomGroupView()))) {

				final int modelIndex = tcm.getColumn(visualColumnIndex).getModelIndex();
				final Icon before=getIcon();
				decorate(table, this, row, modelIndex, isSelected, hasFocus);
				final Icon after=getIcon();
				
				String value = p_value==null?null:p_value.toString();
				int _horizontalAlignment=JLabel.LEFT;

				if (!model.showFilterUI || row != ROW_IDX_FILTER_OP) { // don't
																		// render
																		// filter
																		// op
																		// combo
																		// boxes
					//value = (p_value == null) ? "" : sc.toString(model.getRenderOnlyValue(row, modelIndex, p_value));
					//Replaced the above code with the following as we require the rendering format 
					//in some cases though the p_value is null and also it gets the format when the p_value is null!					
					try {
						value = sc.toString(model.getRenderOnlyValue(row, modelIndex, p_value, isSelected, hasFocus));						
					}
					catch(Exception exception) {
						if (p_value != null) {
							exception.printStackTrace();
						}							
						else {
							value = "";
						}
					}
					if (value == null) {
						value = "";
					} else  if ( !(value instanceof String)){
						System.out.println("Yikes not a  string???  HUH?  What?");
						value="";
					}
					final int dataColumnIndex=model.getDataColumnIndex(modelIndex);
					if (model.getUngroupedModel().columnRenderer != null) {
						final Icon icon=model.getUngroupedModel().columnRenderer.getIcon(
		    					model.getUngroupedModel(), dataColumnIndex, false);
		    			if (icon != null){
		    				setIcon(icon);
		    			}	
					}
					
					if (value.toString().contains(CAN_SELECT_SPECIAL_CHARACTER)) {
						value = value.replaceAll(CAN_SELECT_SPECIAL_CHARACTER, "");
						setIcon(MmsIcons.getCanSelectIcon());
						_horizontalAlignment=horizontalAlignment;
						setHorizontalTextPosition(JLabel.LEFT);
					} else if (value.contains(CAN_NOT_SELECT_SPECIAL_CHARACTER)) {
						value = value.replaceAll(CAN_NOT_SELECT_SPECIAL_CHARACTER, "");
						setIcon(MmsIcons.getCanNotSelectIcon());
						_horizontalAlignment=horizontalAlignment;
						setHorizontalTextPosition(JLabel.LEFT);
					} else {
						//final int dataColumnIndex=model.getDataColumnIndex(modelIndex);
						final Integer specialHorizontalAlignment=model.metaRow.getSpecialHorizontalAlignment(dataColumnIndex);
						_horizontalAlignment=specialHorizontalAlignment==null ? horizontalAlignment:specialHorizontalAlignment;
						if (_horizontalAlignment == JLabel.RIGHT) {
							value = value + "   ";
						}
						if (before == null && after == null){
							setIcon(null);
						}
					}
					setHorizontalAlignment(_horizontalAlignment);
					
				}
				final boolean foundRowByTyping = isAutoCompleteItemFound && row == autoCompleteVisualRowIndex;
				final boolean foundThisCellByTyping = foundRowByTyping && autoCompleteVisualColumnIndex == visualColumnIndex;
				if (model.useDittos(row, modelIndex)) {
					setHorizontalAlignment(JLabel.CENTER);
					setText("-");
				} else {
					sb.setLength(0);
					String txt = value == null ? "" : value.toString();
					if (model.seeOnlyTokens != null || foundThisCellByTyping) {
						sb.append("<html>&nbsp;&nbsp;<font color='");
						SwingBasics.toHtmlRGB(getForeground());
						sb.append("'>");
						final Basics.HtmlBody hb = new Basics.HtmlBody(txt);
						sb.append(hb.prefix);
						if (model.seeOnlyTokens != null) {
							// no need to match the row, is is display-able
							// because it
							// has already being filtered as a match
							final Row _row = model.getRowAtVisualIndex(row);
							if (model.seeOnlyTokens.hasReplacementParts(_row,
									txt)) {
								txt = hb.getHtmlEncoded();
								if (isSelected) {
									txt = model.seeOnlyTokens.highlightFgBg(
											_row, SearchAndReplace.YELLOW,
											"black", txt);
								} else {
									txt = model.seeOnlyTokens.highlightBg(_row,
											SearchAndReplace.YELLOW, txt);
								}
							}
							else {
								txt = hb.getHtmlEncoded();
							}

							if (foundThisCellByTyping) {	
								txt = hb.getHtmlEncoded();
								Basics.highlightWithYellow(sb, null,
										true, new Basics.HtmlBody(txt, true),
										autoCompleteSearchEntry);
							} else {
								sb.append(txt);
							}

						} else if (foundThisCellByTyping) {
							Basics.highlightWithYellow(sb, null,
									true, hb, autoCompleteSearchEntry);
						}
						sb.append(hb.suffix);
						sb.append("</font></html>");
						txt = sb.toString();
					} else 
					if (_horizontalAlignment == JLabel.LEFT) {
						if (txt.indexOf("<html>") < 0) {
							sb.append("  ");
							sb.append(txt);
						} else if (txt.startsWith("<html>")){
							sb.append("<html>&nbsp;&nbsp;<font color='");
							SwingBasics.toHtmlRGB(getForeground());
							sb.append("'>");
							sb.append(txt.substring(6,txt.length()-7));
							sb.append("</font></html>");
						}
						txt=sb.toString();
					}
					setText(txt);
				}
				if (foundRowByTyping) {
					if (autoCompleteVisualColumnIndex == visualColumnIndex) {
						setBorder(GroupedDataSource.BORDER_FOUND_UNSELECTED);
					} 
				}
			}
			return this;
		}
	}


    class BooleanRenderer extends JCheckBox implements TableCellRenderer {
        boolean blankNulls;

        BooleanRenderer(boolean blankNulls) {
            this.blankNulls = blankNulls;
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true); // the following is required to allow checkboxes
								// to display a highlighted border
            setBorderPainted(true); // initialize all borders to be blank
        }

        public Component getTableCellRendererComponent(
          final JTable table,
          Object value,
          final boolean isSelected,
          final boolean hasFocus,
          final int row,
          final int visualColumn) {
        	JComponent cmp=this;
            final int modelIndex = tcm.getColumn(visualColumn).getModelIndex();
            if ((PersonalizableTable.this.model.showFilterUI &&
                 row == ROW_IDX_FILTER_OP) || (value == null && blankNulls)) { // don't
																				// render
																				// filter
																				// op
																				// combo
																				// boxes
                decorate(table, baseRenderer, row, modelIndex, isSelected,
                         hasFocus);
                baseRenderer.setText(value == null ? "" : value.toString());
                return baseRenderer;
            }
            final boolean isOn;
            if (value instanceof ComparableBoolean) {
                isOn = ((ComparableBoolean) value).booleanValue();
            } else {
                if (value instanceof Boolean) {
                    isOn = ((Boolean) value).booleanValue();
                } else {
                	ComparableBoolean b=Basics.isYesOrNo(value);
                	if (b!=null){
                		value=b;
                        isOn = ((ComparableBoolean) value).booleanValue();
                	}else{
                		isOn = isSelected;
                		if (unknownValue==null){
                			unknownValue=new JLabel();
                			unknownValue.setHorizontalAlignment(JLabel.CENTER);
                			
                		}
                		String bgcolor=SwingBasics.toHtmlRGB(
                				isSelected?
                						CellHighlighter.disabledSelectedBackground:
                							_background);
                		unknownValue.setText(
                				Basics.concat(
                						"<html><i><small><font color='gray' bgcolor='",
                						bgcolor,
                						"'>", Basics.toString(value), "</font></small></i></html>"));
                		cmp=unknownValue;
                	}
                }
            }
            setSelected(isOn);
            decorate(table, cmp, row, modelIndex, isSelected, hasFocus);
            if (cmp==unknownValue){
            	unknownValue.setForeground(Color.gray);
            	unknownValue.setBackground(isSelected?CellHighlighter.disabledSelectedBackground:_background);
            }
            return cmp;
        }
        
        private JLabel unknownValue=null;
    }




    private class UrlRenderer extends StringConverterRenderer implements TableCellRenderer {
    	
        UrlRenderer (){
            super(String.class);
            super.horizontalAlignment=JLabel.CENTER;
        }
        public Component getTableCellRendererComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final boolean hasFocus,
          final int row,
          final int visualColumn) {
            super.getTableCellRendererComponent(table,null,isSelected,hasFocus,row,visualColumn);
            if (!Basics.isEmpty(value)){
                setIcon(MmsIcons.getWorldSearchIcon());
                if (model.showUrlText && value instanceof URL){
                    setText( value.toString() );
                    setIconTextGap(5);
                    setHorizontalTextPosition(JLabel.RIGHT);
                }
            } else {
                setIcon(null);
            }
            return this;
        }
    }
    
    private class FileRenderer extends StringConverterRenderer implements TableCellRenderer {
        FileRenderer (){
            super(String.class);
        }
        public Component getTableCellRendererComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final boolean hasFocus,
          final int row,
          final int visualColumn) {
            super.getTableCellRendererComponent(table,null,isSelected,hasFocus,row,visualColumn);
            if (!Basics.isEmpty(value)){
                setIcon(MmsIcons.getOpenIcon());
                if (model.showUrlText && value instanceof String){
                    setText( value.toString() );
                    setIconTextGap(5);
                    setHorizontalTextPosition(JLabel.RIGHT);
                }
            } else {
                setIcon(null);
            }
            return this;
        }
    }


    private class ButtonRenderer extends StringConverterRenderer implements TableCellRenderer {
        ButtonRenderer (){
            super(Object.class);
        }

        public Component getTableCellRendererComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final boolean hasFocus,
          final int row,
          final int visualColumnIndex) {
        	if (value instanceof CellButton) {
            	( (CellButton)value).sync(
            			true,
            			model.getDataColumnIndexFromVisualIndex(visualColumnIndex),
            			PersonalizableTable.this, 
            			model.getRowAtVisualIndex(row));
            }
            super.getTableCellRendererComponent(table,null,isSelected,hasFocus,row,visualColumnIndex);
            if (value instanceof JButton){
                final JButton b=(JButton)value;
                //System.out.println("row="+row+", button text="+b.getText());
                setIcon(b.getIcon());
                setText(b.getText());
                setVerticalAlignment(b.getVerticalAlignment());
                setVerticalTextPosition(b.getVerticalTextPosition());
                setHorizontalAlignment(SwingConstants.CENTER);
                setHorizontalTextPosition(b.getHorizontalTextPosition());                
                setIconTextGap(b.getIconTextGap());
                
            } else {
                setIcon(null);
                setText(value==null?"":Basics.toString(value));
            }
            return this;
        }
    }
    public abstract static class CellButton extends SwingBasics.ImageButton{
        private boolean isCurrentlyClicking=false;

    	public CellButton(final ImageIcon icon){
    		super(icon);
    		
    	}   
    	protected boolean clickedByUser=true;
    	private void click() {
    		clickedByUser=false;
    		doClick();
    		clickedByUser=true;
    	}
        public void doClick(int pressTime) {
        	if (!clickedByUser && !isCurrentlyClicking) {
        		isCurrentlyClicking=true;
	        	if (pressable != null && pressable != this) {
	        		pressable.doClick(pressTime);
	        	} else {
	        		super.doClick(pressTime);
	        	}
        		isCurrentlyClicking=false;
        	}
        	
        }

    	private CellButton pressable=null;
    	public CellButton getPressable() {
    		if (pressable == null) {
    			pressable=getSecondOneForPressing();    			
    		}
    		return pressable;
    	}
    	
    	public abstract void sync(final boolean forRenderingPurposes, final int dataColumnIndex, final PersonalizableTable table, final Row theButtonRow) ;
    	protected abstract CellButton getSecondOneForPressing();

    }
    
    private class ButtonEditor extends DefaultCellEditor implements ActionListener {
        private JButton button;
        boolean isPushed;


        private ButtonEditor() {
            super(new JCheckBox());
        }

        void setButton(final JButton button) {
        	if (button instanceof CellButton) {
        		this.button=( (CellButton)button).getPressable();
        	} else {
        		this.button = button;
        	}
            button.removeActionListener(this);
            button.addActionListener(this);
        }

        public void actionPerformed(final ActionEvent e) {
        	model.handleRowButtonClick(button);
        	fireEditingStopped();            
        }


        public Component getTableCellEditorComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final int row,
          final int column) {
    
            if (value instanceof JButton) {
                setButton((JButton) value);
            
            if (button instanceof CellButton) {
            	( (CellButton)button).sync(
            			true,
            			model.getDataColumnIndexFromVisualIndex(column),
            			PersonalizableTable.this, 
            			model.getRowAtVisualIndex(row));
            	( (CellButton)button).click();
   	
            }
            if (button != null){
                    button.setForeground(table.getSelectionForeground());
                    button.setBackground(table.getSelectionBackground());
                
            }
            }
            isPushed = true;
            
            return button;
        }

        public Object getCellEditorValue() {
            isPushed = false;
            return button;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }


    private class UrlEditor extends ButtonEditor {

        private UrlEditor() {
            final JButton button = new JButton();
            button.setOpaque(true);
            button.setIcon(MmsIcons.getSearchIcon());
            setButton(button);
        }

        private URL url;
        public Component getTableCellEditorComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final int row,
          final int column) {
        	if (value instanceof URL){
        		url = (URL) value;
        	}
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public Object getCellEditorValue() {
            if (isPushed && url != null) {
                SwingBasics.showHtml(url.toExternalForm());
                // System.out.println(label + ": Ouch!");
            }
            isPushed = false;
            return url;
        }
    }
    
    private class FileEditor extends ButtonEditor {

        private FileEditor() {
            final JButton button = new JButton();
            button.setOpaque(true);
            button.setIcon(MmsIcons.getSearchIcon());
            setButton(button);
        }

        private File file;
        public Component getTableCellEditorComponent(
          final JTable table,
          final Object value,
          final boolean isSelected,
          final int row,
          final int column) {
            file = new File(value.toString());
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public Object getCellEditorValue() {
            if (isPushed && file != null) {
               SwingBasics.showFolder(file);
            }
            isPushed = false;
            return file;
        }
    }

    class HeaderRenderer extends DefaultTableCellRenderer{
        HeaderRenderer() {
         	setHorizontalTextPosition(RIGHT);
    		setHorizontalAlignment(CENTER);
    		
        }
        
    	int getDataColumnIndex(final int visualColumnIndex){
    		return model.getDataColumnIndexFromVisualIndex(visualColumnIndex);
    	}


    	public Component getTableCellRendererComponent(final JTable table,
    			Object value, final boolean isSelected,
    			final boolean hasFocus, final int row, final int visualColumnIndex) {
    		if (model.allowsTableShowing
					&& (model.hasBeenShownAtSomePoint()
							|| (model.getGroupOption() == PersonalizableTableModel.NO_GROUPING 
									&& !model.hasCustomGroupView()))) {
    		final int dataColumnIndex = getDataColumnIndex(visualColumnIndex);
            final boolean small =
                smallFontDataColumnIndex.size() > 0 &&
                smallFontDataColumnIndex.contains(dataColumnIndex);

    		if (model.headerForeground != null && value instanceof String && model.headerForeground.containsKey(dataColumnIndex)){
    			final String fg= "<html><font color='"+model.headerForeground.get(dataColumnIndex)+"'>";
    			final int start,end;
    			if ( ((String)value).startsWith("<html>")){
    				start=6;
    				end=((String)value).length()-7;
    			} else {
    				start=0;
    				end=((String)value).length();
    			}
    			value=fg+ ((String)value).substring(start, end)+"</font></html>";
    		}
    		super.getTableCellRendererComponent(table, value,
    				isSelected, hasFocus, row, visualColumnIndex);
    		boolean isSorted = false, isAscending = true, isCustom = false;
    		final SortInfo si = model.findSortInfo(dataColumnIndex);
    		if (si != null) {
    			isSorted = true;
    			isAscending = si.ascending;
    			isCustom = model.isUsingNonAlphabeticSortSequence(si.dataColumnIndex);
    		}
    		if (table != null) {
        		if (header != null) {
        			if (model.headerForeground != null ){
            			if (!model.headerForeground.containsKey(dataColumnIndex)){
            				setForeground(CellHighlighter.disabledSelectedForeground);
            			}
            		} else 	{
            			setForeground(header.getForeground());
            		}
    				setBackground(header.getBackground());
    			    final boolean special =
    	                specialFontDataColumnIndex.size() > 0 &&
    	                specialFontDataColumnIndex.contains(dataColumnIndex);
    	              if (!special) {
    	            	  if (small) {
    	            		  setFont(smallFont);
    	            	  } else {
    	            		  setFont(header.getFont());
    	            	  }
    	              } else {
						setForeground(specialForeground);
						setFont(specialEditableFont);
    	              }
    			}
    		}
    		if (isSorted) {
    			final String currentSortOrder = model.getCurrentSort(dataColumnIndex,
    					false);
    			setText((value == null) ? "" : value.toString());
    			setIconTextGap(8);
    			if (model.canSort()) {
    				PersonalizableTable.setIcon(this, isAscending, isCustom);
    			}
    			setToolTipText(currentSortOrder);
    		} else {
    			setText((value == null) ? "" : value.toString());
    			setIcon(null);
    			setToolTipText(TOOL_TIP_HEADER);
    		}
    		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    		int w = getText().length()*7;
    		final int height = getHeaderRowHeight();
    		final Dimension d = new Dimension(w, height);
    		setPreferredSize(d);
    		if (ungroupedModel==null){
    			ungroupedModel=model.getUngroupedModel();
    		}
    		if (ungroupedModel.columnRenderer != null){
    			Color color=ungroupedModel.columnRenderer.getForeground(dataColumnIndex);
    			if (color != null){
    				setForeground(color);
    			}
    			color=ungroupedModel.columnRenderer.getBackground(dataColumnIndex);
    			if (color != null){
    				setBackground(color);
    			} else {
    				setBackground(SwingBasics.isMacLookAndFeel()?Color.lightGray:filterBackground);
    			}
    			final Font font=ungroupedModel.columnRenderer.getFont(dataColumnIndex);
    			if (font != null){
    				setFont(font);
    			}
    			if (!isSorted){
    			final Icon icon=ungroupedModel.columnRenderer.getIcon(
    					model.getUngroupedModel(), dataColumnIndex, false);
    			if (icon != null){
    				setIcon(icon);
    			}
    			}
    			final Component c=ungroupedModel.columnRenderer.get(model.getUngroupedModel(), header, dataColumnIndex, visualColumnIndex, this);
    			if (c!=null){
    	    		if (headerMouseOver.over>=0){
    	    			if (headerMouseOver.over==visualColumnIndex){
    	    				c.setForeground(defaultEnabledHighlight);
    	    			}
    	    		}
    				return c;
    			}
    		}
    		}
    		if (headerMouseOver.over>=0){
    			if (headerMouseOver.over==visualColumnIndex){
    				setForeground(defaultEnabledHighlight);
    			}
    		}
    		return this;
    	}
    		

    }
    static Color defaultEnabledHighlight=new Color(0, 153, 0);

    public int getHeaderRowHeight() {
        return headerLines * 22;
    }

    private void computeHeaderLines(int []d) {
        int max = 1;
        for (int i = 0; i < d.length; i++) {
            final int n = 1 +
                          Basics.count(model.getColumnLabel(d[i]), "<br>");
            if (n > max) {
                max = n;
            }
        }
        headerLines = max;
    }

    public void recomputeHeaderLines() {
        final int prev = headerLines;
        computeHeaderLines(model.getDataColumnIndexesThatAreVisible());
        if (prev != headerLines) {
            getTableHeader().resizeAndRepaint();
        }
    }

    int headerLines = 1;

    static class Arrow implements Icon {
        private static final int NONE = 0;
        private static final int DECENDING = 2;
        private static final int ASCENDING = 1;

        final protected int direction;
        final protected int width = 8;
        final protected int height = 8;

        public Arrow(int direction) {
            this.direction = direction;
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }

        public void paintIcon(
          final Component c,
          final Graphics g,
          final int x,
          final int y) {
            final Color bg = c.getBackground(),
                             light = bg.brighter(),
                                     shade = bg.darker();

            final int w = width;
            final int h = height;
            final int m = w / 2;
            if (direction == DECENDING) {
                g.setColor(shade);
                g.drawLine(x + m, y, x, y + h);
                g.setColor(light);
                g.drawLine(x, y + h, x + w, y + h);
                g.drawLine(x + m, y, x + w, y + h);
            } else if (direction == ASCENDING) {
                g.setColor(shade);
                g.drawLine(x, y, x + w, y);
                g.drawLine(x, y, x + m, y + h);
                g.setColor(light);
                g.drawLine(x + w, y, x + m, y + h);
            }
        }
    }


    public TableCellRenderer getCellRenderer(
      final int visualRowIndex,
      final int p_visualColumn) {
        final int visualColumn = p_visualColumn < 0 ? 0 : p_visualColumn;
        for (final Iterator<NestedTableCellEditor.Factory> it = nestedTableFactories.iterator(); it.hasNext(); ) {
            final NestedTableCellEditor.Factory factory =it.next();
            if (factory.isSuitable(this, visualRowIndex, p_visualColumn)) {
                return factory;
            }
        }
        if (tcr == null) {
        	initTcr();
        }
		final TableCellRenderer value;
		if (model.showFilterUI && visualRowIndex == ROW_IDX_FILTER_OP) {
			value = baseRenderer;
		} else {

			final TableCellRenderer t = tcr[model.getDataColumnIndex(tcm
					.getColumn(visualColumn).getModelIndex())];
			if (t == null) {
				value = super.getCellRenderer(visualRowIndex, visualColumn);
			} else {
				value = t;
			}
		}
        return value;
    }

    TableCellRenderer[] tcr = null;

    public boolean isEditorDisplay = false;
    public TableCellEditor getCellEditor(
      final int visualRowIndex,
      final int visualColumnIndex) {
        if (model.showFilterUI) {
            if (visualRowIndex == ROW_IDX_FILTER_OP) {

                final int modelIndex = tcm.getColumn(visualColumnIndex).
                                       getModelIndex();
                final Class cl = TableBasics.reinterpretClass(
                  model.metaRow,
                  model.getDataColumnIndex(modelIndex),
                  model.getColumnClass(modelIndex));
                final TableCellEditor tmpEditor = ComboCellEditor.New( Arrays.asList(getOps(cl)), false, false, 500);
                return tmpEditor;
            }
            if (visualRowIndex == ROW_IDX_FILTER_VALUE) {
                final int modelIndex = tcm.getColumn(visualColumnIndex).
                                       getModelIndex();
                
                return ReuseColumnValueEditor.New(model.dataSource,
                                                  model.getDataColumnIndex(
                  modelIndex), model.getColumnClass(modelIndex));

            }
        }
        for (final Iterator it = nestedTableFactories.iterator(); it.hasNext(); ) {
            NestedTableCellEditor.Factory factory = (NestedTableCellEditor.
              Factory)
              it.next();
            if (factory.isSuitable(this, visualRowIndex, visualColumnIndex)) {
                return factory.getTableCellEditor(this, visualRowIndex, visualColumnIndex);
            }
        }
        int dataColumnIndex = model.getDataColumnIndex(tcm.getColumn(visualColumnIndex).getModelIndex());        
        final Row row = model.getRowAtVisualIndex(visualRowIndex);
        isEditorDisplay = true;
		if (row != null && !row.isEditable(dataColumnIndex)) {
			isEditorDisplay = false;
			return null;
		}
		isEditorDisplay = false;
        TableCellEditor tce = model.dataSource.getCellEditor(model.
          getRowAtVisualIndex(
            visualRowIndex),dataColumnIndex
          );
        if (tce == null){
            if ( super.getColumnClass(visualColumnIndex).equals(URL.class)){
                final Object o=getValueAt(visualRowIndex, visualColumnIndex);
                if (o==null){
                    return getDefaultEditor(String.class);
                }
            }else if ( super.getColumnClass(visualColumnIndex).equals(File.class)){
                final Object o=getValueAt(visualRowIndex, visualColumnIndex);
                if (o==null){
                    return getDefaultEditor(String.class);
                }
            }
            tce=super.getCellEditor(visualRowIndex, visualColumnIndex);
        }
       return tce;
    }

    ResetableField objectField, integerField, floatField, dateField;

    private void setupObjectEditor() {
        objectField = new ResetableField();
        setupStringConverterEditor(Object.class, objectField);
    }

    private void setupIntegerEditor() {
        integerField = new ResetableField();
        integerField.setHorizontalAlignment(JTextField.RIGHT);
        setupStringConverterEditor(Integer.class, integerField);
    }

    private void setupLongEditor() {
        setupStringConverterEditor(Long.class, integerField);
    }

    private void setupFloatEditor() {
        floatField = new ResetableField();
        floatField.setHorizontalAlignment(JTextField.RIGHT);
        setupStringConverterEditor(Float.class, floatField);
    }

    private void setupDateEditor() {
        dateField = new ResetableField();
        setupStringConverterEditor(Date.class, dateField);
    }

    private void setupDateTimeEditor() {
        dateField = new ResetableField();
        setupStringConverterEditor(DateTime.class, dateField);
    }

    @SuppressWarnings("unchecked")
	private void setupStringConverterEditor(final Class cl,
                                            final ResetableField field) {
        //Set up the editor for the float cells.
        final StringConverter sc = (StringConverter) DefaultStringConverters.
                                   get(cl);

        field.setHorizontalAlignment(sc.getHorizontalAlignment());
        final DefaultCellEditor customEditor = new StringConverterCellEditor(
          field,
          sc);
        setDefaultEditor(cl, customEditor);
    }

    private class ResetableField extends JTextField implements FieldWithPriorValue{
        private Object priorValue = null;
        final VoodooTableCell voodoo=new VoodooTableCell(this);

        protected boolean processKeyBinding(
          final KeyStroke ks,
          final KeyEvent e,
          final int condition,
          final boolean pressed) {
        	ToolTipOnDemand.hideManagerWindow();
            final boolean b = super.processKeyBinding(ks, e, condition, pressed);
            voodoo.handle(ks, e, condition, pressed);
               return b;
        }
        
        public javax.swing.text.JTextComponent getTextComponent(){
        	return this;
        }
        public void setPriorValue(final Object value) {
            priorValue = value;
        }

        public Object getPriorValue() {
            return priorValue;
        }
        
        public VoodooTableCell getVoodoo(){
        	return voodoo;
        }
    }
    public class StringConverterCellEditor extends DefaultCellEditor {
        final ResetableField resetableField;
        final StringConverter sc;
        

        StringConverterCellEditor(final ResetableField resetableField,
                                  final StringConverter sc) {
            super(resetableField);
            this.resetableField = resetableField;
            this.sc = sc;
            super.delegate = new EditorDelegate() {
                public void setValue(final Object value) {
                	VoodooTableCell.startCellEditing(value, resetableField, sc);
                }
            };
        }

        private Toolkit toolkit = Toolkit.getDefaultToolkit();

        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Float, not a String:
        public Object getCellEditorValue() {
            Object retVal;
            String txt = this.resetableField.getText();
            try {
                if (Basics.isEmpty(txt)) {
                    retVal = null;
                } else {
                    retVal = sc.toObject(txt);
                }
            } catch (final Exception e) {
                Pel.log.warn(e);
                toolkit.beep();
                JOptionPane.showMessageDialog(null, sc.getFormatTip(),
                                              "Invalid format!",
                                              JOptionPane.ERROR_MESSAGE);
                retVal = resetableField.getPriorValue();
            }
            return retVal;
        }
    }


    public class ComparableBooleanCellEditor extends DefaultCellEditor {
        final JCheckBox jcb;
        public ComparableBooleanCellEditor(final JCheckBox jcb) {
            super(jcb);
            this.jcb = jcb;
            this.delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    final boolean isOn;
                    if (Basics.isEmpty(value)) {
                        isOn = false;
                    } else {
                    	if (value instanceof String){
                    		value=Basics.isYesOrNo(value);
                    	}
                        isOn = ((ComparableBoolean) value).booleanValue();
                    }
                    if (jcb.isShowing()) {
                    	ComparableBooleanCellEditor.this.jcb.setSelected(isOn);                    	
                    } else {
                    	SwingUtilities.invokeLater(new Runnable(){

							@Override
							public void run() {
								ComparableBooleanCellEditor.this.jcb.setSelected(isOn);
							}
                    		
                    	});
                    }
                }
            };
        }

        public Object getCellEditorValue() {
            return ComparableBoolean.valueOf(this.jcb.isSelected());
        }
    }


    public void scrollToVisible(final int visualRowIndex, final int visualColumnIndex) {
        SwingBasics.scrollToVisible(this, visualRowIndex, visualColumnIndex);
    }

    boolean onFilteringRow() {
        return super.editingRow == ROW_IDX_FILTER_VALUE;
    }

    void moveFocusFromFilteringRowIfNecessary() {
        if (model.showFilterUI && editingRow == ROW_IDX_FILTER_VALUE) {
            editCellAt(0, editingColumn);
        }
    }

    public void setContainer(final JComponent jp) {
    	jp.removeMouseListener(this);
        jp.addMouseListener(this);
    }

    public boolean alterRendererTable() {
        return false;
    }

    public boolean alterRendererIndexes() {
        return true;
    }

    public boolean alterEditorTable() {
        return true;
    }

    public boolean alterEditorIndexes() {
        return true;
    }

    public void repaint(final int visualRowIndex, final int dataColumnIndex) {
        final int visualColumnIndex = model.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
        if (visualColumnIndex > -1) {
            final Rectangle rec = getCellRect(visualRowIndex, visualColumnIndex, true);
            repaint(rec);
        }
    }

    public static class Action {
    	public boolean isEnabled(){
    		return enabled;
    	}
    	public String getToolTipDisabled(){
    		return _toolTipDisabled;
    	}
        private boolean enabled = true;
        private String toolTipEnabled, toolTipDisabled, _toolTipDisabled;
        private String txt;
        private final ActionListener anAction;
        private final KeyStroke acceleratorKeyStroke;
        private final Icon icon;
        private final boolean buttonToo;
        private final PersonalizableTableModel.PopupMenuListener pml;

        public Action(
          final String txt,
          final ActionListener al,
          final KeyStroke acceleratorKeyStroke,
          final String toolTip,
          final PersonalizableTableModel.PopupMenuListener pml) {
            this(txt, al, acceleratorKeyStroke, null, false, toolTip, pml, '\0');
        }

        public Action(
          final String txt,
          final ActionListener al,
          final KeyStroke acceleratorKeyStroke,
          final String toolTip) {
            this(txt, al, acceleratorKeyStroke, null, false, toolTip);
        }

        public Action(
          final String txt,
          final ActionListener anAction,
          final KeyStroke acceleratorKeyStroke,
          final Icon icon,
          final boolean buttonToo,
          final String toolTip) {
            this(txt, anAction, acceleratorKeyStroke, icon, buttonToo, toolTip, null,
                 '\0');

        }

        final char mnemonic;

        public Action(
          final PopupMenuParameters p,
          final ActionListener al,
          final boolean buttonToo) {
            this(p.text, al, p.keyStroke, p.icon, buttonToo, p.toolTip, p.listener,
                 p.mnemonic);
        }

        public Action(
          final String txt,
          final ActionListener al,
          final KeyStroke acceleratorKeyStroke,
          final Icon icon,
          final boolean buttonToo,
          final String toolTip,
          final PersonalizableTableModel.PopupMenuListener pml,
          final char mnemonic) {
            this.txt = txt;
            this.mnemonic = mnemonic;
            this.anAction = al;
            this.acceleratorKeyStroke = acceleratorKeyStroke;
            this.icon = icon;
            this.toolTipEnabled = Basics.toHtmlUncentered(
      Basics.stripSimpleHtml(txt),
      toolTip);

            this.buttonToo = buttonToo;
            this.pml = new PersonalizableTableModel.PopupMenuListener() {
                public void menuPoppedUp(final PersonalizableTableModel.PopupMenuItem
                                         pmi) {
                    update(pmi);
                    if (pml != null){
                        pml.menuPoppedUp(pmi);
                    }
                }
            };
        }

        public void update(final PersonalizableTableModel.PopupMenuItem pmi){
            if (pmi.menuItem != null) {
                pmi.menuItem.setText(txt);
                pmi.menuItem.setToolTipText(enabled ? toolTipEnabled:toolTipDisabled);
                pmi.setEnabled(enabled);
            }
        }


        private DisabledExplainer deButton;
        public void setEnabled(
          final boolean enabled,
          final String _toolTip) {
            this.enabled = enabled;
            final String toolTip;
            if (enabled ) {
                if (_toolTip != null){
                    toolTipEnabled = Basics.toHtmlUncentered(
                      Basics.stripSimpleHtml(txt),
                      _toolTip);
                }
                toolTip=toolTipEnabled;
            } else {
            	_toolTipDisabled=_toolTip;
                toolTipDisabled = Basics.toHtmlErrorUncentered(
                  Basics.stripSimpleHtml(txt),
                  _toolTip);
                toolTip=toolTipDisabled;
            }
            if (button != null) {
                if (deButton==null){
                	deButton=new DisabledExplainer(button);
                }
                deButton.setEnabled(enabled, null, enabled?toolTipEnabled:toolTipDisabled);
            }
            for (final PersonalizableTableModel.PopupMenuItem pmi:pmic) {
                if (pmi != null){
                    pmi.setEnabled(enabled);
                    pmi.setToolTipText(toolTip);
                    if(!enabled){
                    	pmi.disabledToolTipText=_toolTip;
                    }
                }
            }
        }




        public void setText(final String text, final String _toolTip) {
            this.txt = text;
            final String toolTip;
            if (enabled) {
                toolTip=this.toolTipEnabled = Basics.toHtmlUncentered(
                  Basics.stripSimpleHtml(txt),
                  _toolTip);

            } else {
                toolTip=this.toolTipDisabled = Basics.toHtmlUncentered(
                  Basics.stripSimpleHtml(txt),
                  _toolTip);
                _toolTipDisabled=_toolTip;
            }
            if (button != null) {
                button.setText(text);
                button.setToolTipText(toolTip);
            }
            for (final PersonalizableTableModel.PopupMenuItem pmi:pmic) {
                pmi.setText(text);
                pmi.setToolTipText(toolTip);
            }
        }

        public JButton button = null;
        public JButton getButton() {
            if (buttonToo) {
                button = SwingBasics.getButton(txt, icon, mnemonic, anAction,
                                               enabled?toolTipEnabled:toolTipDisabled);
                button.registerKeyboardAction(anAction, acceleratorKeyStroke,
                                              JComponent.WHEN_FOCUSED);
                button.setToolTipText(enabled?toolTipEnabled:toolTipDisabled);
                
                button.setEnabled(enabled);
                return button;
            }
            return null;
        }
        private boolean addToTail=false;
        public void addToTailOfRightClickMenu(){
        	addToTail=true;
        }
        private Collection<PersonalizableTableModel.PopupMenuItem> pmic = new ArrayList<PersonalizableTableModel.PopupMenuItem> ();
        public void newPopup(final PersonalizableTableModel tableModel) {
            if (enabled) {
                pmic.add(tableModel.new PopupMenuItem(
                  Basics.strip(txt, "<br>"),
                  addToTail,
                  anAction,
                  pml,
                  true,
                  icon,
                  acceleratorKeyStroke,
                  enabled?toolTipEnabled:toolTipDisabled,
                  this.mnemonic,"This menu item is currently not allowed"));
            }
        }

        DisabledExplainer menuItemDisabled;
        public JMenuItem newMenuItem(){
            final JMenuItem m = new JMenuItem(
              Basics.stripSimpleHtml(txt),
              icon);
            menuItemDisabled = new DisabledExplainer(m);
            m.setToolTipText(toolTipEnabled);
            m.addActionListener(anAction);
            if(enabled) {
            	menuItemDisabled.setEnabled(true, null, null);
            }
            else {
            	menuItemDisabled.setEnabled(false, Basics.stripSimpleHtml(txt), "This menu is currently not allowed");
            }
            return m;
        }

    }


    // acts as "design-time" decoupler of protege.jar
    public static ContainerBuilder containerBuilder;
    public static interface ContainerBuilder {
        public JComponent configure(String label, JComponent pane, PersonalizableTable table, boolean isTearingAway);
    }

    private JComponent container;

    public JComponent getContainer(){
        return container;
    }

    JComponent getContainer(final String label) {
        if (container != null){
            return container;
        }
        final JComponent c=model.getTearAwayComponent();
        if (containerBuilder != null) {
            container=containerBuilder.configure(label, c, this, false);
            setContainer(container);
            return container;
        }
        return c;
    }
    private static String CAN_SELECT_SPECIAL_CHARACTER="\u2193";
    private static String CAN_NOT_SELECT_SPECIAL_CHARACTER="\u02C6";
    
    private void initTcr() {
        final int n = model.metaRow.size();
        tcr = new TableCellRenderer[n];
        for (int i = 0; i < n; i++) {
            final String f = model.metaRow.getDateFormat(i);
            final Class cl = model.metaRow.getClass(i);
            if (f != null) {
                tcr[i] = new StringConverterRenderer(
                  new DefaultStringConverters._Date(f), cl);
            } else {
                if (!cl.equals(URL.class) &&
                !cl.equals(JButton.class) &&
                    !cl.equals(Boolean.class) &&
                    !cl.equals(ComparableBoolean.class) &&
                    !cl.equals(BooleanConvertor.class) &&
                    !cl.equals(File.class)) {
                    final StringConverter sc = model.metaRow.
                      getStringConverter(i);
                    if (sc != null) {
                        tcr[i] = new StringConverterRenderer(sc, cl);
                    }
                }
            }
        }
    }
    public static boolean isKeyBindingEvent = false;
    private static boolean isAltPressed = false;
    private EventObject eventObject;
    protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,final int condition, final boolean pressed) {
    	final boolean b1=e.isAltDown(), b2=e.isControlDown();
    	final PersonalizableTableModel um =model.getUngroupedModel();
    	final boolean showFilterUI=model.showFilterUI;
    	final int rowIndex=model.focusVisualRowIndex;
    	final int colIndex=SwingBasics.getVisualIndexFromModelIndex(this,model.focusModelColumnIndex);
    	isAltPressed = ((e.getModifiers() & KeyEvent.ALT_MASK) == KeyEvent.ALT_MASK);
    	eventObject = e;    	
        final int kc=e.getKeyCode();
        final int modi=ks.getModifiers();
        if (!b1 && !b2){
        	final char c = e.getKeyChar();
    		if (pressed) {
    			isKeyBindingEvent = true;
    			if (c >= ' ' && c <= '~') {
        	if ( (modi & InputEvent.META_MASK) == 0 && (um.isPickList || !model.getEditInPlace() || !isCellEditable(
					rowIndex, colIndex)) && (!showFilterUI || rowIndex > ROW_IDX_FILTER_VALUE)) {
						System.out.println("c=" + c + ", pressed=" + pressed);
						autoComplete(c);
						return true;
					}
				}
			}
        }
        isProcessingKey=!isInitializingUpCellEditor;
        boolean b=super.processKeyBinding(ks, e, condition, pressed);
      	if (!b1 && !b2 && kc==KeyEvent.VK_ESCAPE){
            final TableCellEditor tce=getCellEditor();
            if (tce instanceof AutoComplete.CellEditor){
            	tce.stopCellEditing();            	
            }
        }
        isProcessingKey=false;
        if (!pressed && (kc==KeyEvent.VK_LEFT || kc==KeyEvent.VK_RIGHT || kc==KeyEvent.VK_UP || kc==KeyEvent.VK_DOWN)){
        	SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					final PersonalizableTable table=PersonalizableTable.this;
					final int rowIdx=table.getSelectionModel().getLeadSelectionIndex();
					final int colIdx=table.getColumnModel().getSelectionModel().getLeadSelectionIndex();
					PersonalizableTable.this.scrollToVisible(rowIdx, colIdx);
				}
			});
        }
        return b;
    }
    private boolean isProcessingKey=false;
    private long lastKeyTime = 0;
    String autoCompleteSearchEntry = "";
    private boolean isAutoCompleteItemFound = false;
    private int autoCompleteVisualRowIndex = -1;
    
    int getAutoCompleteSelectedRow(){
        final int selectedRow;
        if (autoCompleteVisualRowIndex >= 0) {
        	selectedRow = autoCompleteVisualRowIndex;
        }
        else if (getSelectedColumnCount() > 0) {
        	selectedRow =getSelectedRow();
        } else if (model.clickedVisualRowIndex>=0){
        	selectedRow=model.clickedVisualRowIndex;
        } else {
        	selectedRow=0;
        }
        return selectedRow;
    }
    
    private void autoComplete(final char aKey) {
    	if (aKey > 126 || aKey < 32) { //Non printable characters
            return;
        }    	
        final int selectedRow = getAutoCompleteSelectedRow();
        int searchStartRow = -1;
        // Get the current time
        final long curTime = System.currentTimeMillis();
        // If last key was typed less than 500 ms ago, append to current pattern
        if (curTime - lastKeyTime < 500) {        	
        	//Subsequent same key presses move the keyboard focus to the next
 			//object that starts with the same letter.
        	if((autoCompleteSearchEntry.length() == 1) && (aKey == autoCompleteSearchEntry.charAt(0))) {
        		searchStartRow = selectedRow + 1;
        	}
        	else {
        		autoCompleteSearchEntry += ("" + aKey).toLowerCase();
                searchStartRow = selectedRow;	
        	}            
        } else {
        	autoCompleteDataColumnIndex=-1;
        	//tableWideSearch=false;
            autoCompleteSearchEntry = ("" + aKey).toLowerCase();
            if (selectedSinceLastSearch){
            searchStartRow = getSelectedRow() + 1;
            } else {
            	searchStartRow = selectedRow + 1;
            }
            selectedSinceLastSearch=false;
        }
        lastKeyTime = curTime;
        autoComplete(searchStartRow,true);
    }
    
    static boolean tableWideSearch=true;
    boolean hasTipShown=false;
    private int autoCompleteDataColumnIndex=-1;
    boolean autoComplete(final int searchStartRow, final boolean forward){
    	final boolean usingFindWindow=searchStatusComponent!=PersonalizableTable.this;
    	if (Basics.isEmpty(autoCompleteSearchEntry)){
    		return false;
    	}
    	final int rowCount = getRowCount();
    	if (autoCompleteDataColumnIndex==-1){
    		autoCompleteDataColumnIndex=getDataColumnIndexForFocusOrFirstHighlighedOrFirstSortedAndVisibleOrLeftMost();
    	}
    	if (!tableWideSearch){
    		final Class mi=model.getColumnClass(model.getModelColumnIndexFromDataColumnIndex(autoCompleteDataColumnIndex));
    		if (mi== JButton.class){
    			return false;
    		}
    	}
        int visualRowIndex = -1;
        final boolean twasFound;
        if (forward){
        	twasFound=(visualRowIndex = searchForwardForAutoComplete(searchStartRow, rowCount, false)) != -1 
        || (visualRowIndex = searchForwardForAutoComplete(0, searchStartRow+1, true)) != -1;
        } else {
        	twasFound=(visualRowIndex = searchBackwardForAutoComplete(searchStartRow, -1, false)) != -1 
            || (visualRowIndex = searchBackwardForAutoComplete(rowCount-1, searchStartRow-1, true)) != -1;            	
        }
        
        //Search from currently selected row to the end and then from the beginning to selected row        
        if (twasFound)   {
            if (!tableWideSearch){
            	model.highlightColumn(autoCompleteDataColumnIndex);
            } else {
            	autoCompleteVisualColumnIndex=model.getVisualColumnIndexFromDataColumnIndex(autoCompleteDataColumnIndex);
            	model.highlightColumn(autoCompleteDataColumnIndex);
            	setColumnSelectionInterval(autoCompleteVisualColumnIndex, autoCompleteVisualColumnIndex);
            }
            autoCompleteVisualRowIndex = visualRowIndex;        	
        	SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	//Set row to apply border and scroll to Visible
                	isAutoCompleteItemFound = true;
                	SwingBasics.scrollToVisible(PersonalizableTable.this, autoCompleteVisualRowIndex, autoCompleteVisualColumnIndex, 60, 60);
                	final int wOffset,hOffset;
                	if (showAgain.isSelected() || !hasTipShown) {
						final String txt = "\""
								+ Basics.encodeHtml(autoCompleteSearchEntry)
								+ "\"";
						final StringBuilder title = new StringBuilder(100);
						title.append( Basics.concatObjects("Found  ", txt, "\" at row ", (autoCompleteVisualRowIndex+1),
								" in the column ",
								" <b>",
								Basics.stripBodyHtml(getColumnName(autoCompleteVisualColumnIndex)),
								"</b>"));
                		final StringBuilder msg = new StringBuilder(250);
                		final Object value=getValueAt(autoCompleteVisualRowIndex, autoCompleteVisualColumnIndex);
						if (value instanceof java.net.URL) {
							msg.append("<br><small>");
							Basics.highlightWithYellow(msg, null, true,
									new Basics.HtmlBody(Basics
											.toString(value), true),
									autoCompleteSearchEntry);
							msg.append("</small>");
						} else {
							TableCellRenderer renderer = getCellRenderer(
									autoCompleteVisualRowIndex,
									autoCompleteVisualColumnIndex);
							Component component = prepareRenderer(renderer,
									autoCompleteVisualRowIndex,
									autoCompleteVisualColumnIndex);
							if (component instanceof JLabel) {
								String s = ((JLabel) component).getText();
								s = Basics.stripHeaderHtml(s);
								msg
										.append("<br><small>");
								msg.append(s);
								msg.append("</small>");
							}
						}
						if (usingFindWindow){
                    		hOffset=searchComponentHeightOffset;
                    		wOffset=0;
						}else{
							final Rectangle r = getCellRect(
									autoCompleteVisualRowIndex,
									autoCompleteVisualColumnIndex, false);
							wOffset = r.x + (r.width*4/5);
							hOffset = r.y + r.height + 20;
							msg.append("<br><br>Now you can press:");
							msg.append("<ul><li>");
							if (searchStatusComponent == PersonalizableTable.this) {
								msg.append(Basics.concat(
									"<b>",
									SwingBasics.searchSelectKeyText,
												"</b> to make the found row the only selection.<li><b> ",
												SwingBasics.searchAddSelectKeyText,
												"</b> to add found row to previous selections.<li>"));
							}
							msg.append(
								Basics.concat(
									"<b>",SwingBasics.searchDownUp, 
									"</b> for next<font color='red'><b>/</b></font>prev ", 
									txt, 
									" in this column as <i><u>starting</u></i> value<li><b>", 
									SwingBasics.searchRightLeft, "</b> for next<font color='red'><b>/</b></font>prev ", 
									txt, 
									" <i><u>anywhere</u></i> in the table.</ul>"
								)
							);
						}
						setSearchToolTip(Basics
						.toHtmlUncenteredSmall(title.toString(), msg.toString()), wOffset,hOffset+40,usingFindWindow);
					}
                	updateUI();
                }
            });
        }
        else {
            if (!tableWideSearch && autoCompleteDataColumnIndex > -1){
            	model.highlightColumn(autoCompleteDataColumnIndex);
            }
        	//isAutoCompleteItemFound = false;        	
        	//autoCompleteVisualRowIndex = -1;
        	//autoCompleteDataColumnIndex=-1;

        	PopupBasics.beep();
        	updateUI();
        	if (showAgain.isSelected()){
                final int wOffset,hOffset;
            	if (!usingFindWindow){            		
            		final Rectangle r=getVisibleRect();
            		wOffset=r.x;
            		hOffset=r.y+r.height+5;
            	} else {
            		hOffset=searchComponentHeightOffset;
            		wOffset=0;
            	}
            	setSearchToolTip(
           			Basics.toHtmlUncentered(
          				Basics.concat(
           					"Can not find \"", 
           					Basics.encodeHtml(autoCompleteSearchEntry),
           					"\" ",
           					(tableWideSearch?"any where in the table.":Basics.concatObjects("in column ",autoCompleteVisualColumnIndex,
           					" as a starting value.")))), wOffset,hOffset,usingFindWindow);
            	ToolTipOnDemand.getSingleton().show(searchStatusComponent, false, wOffset, hOffset, usingFindWindow ? null:showAgain,null);
        	}
        }
        return twasFound;
    }
    
    static JCheckBox showAgain=new JCheckBox("Show tip again", true);
    private int autoCompleteVisualColumnIndex;
    
    private final String getStringValueAt(final int visualRowIndex, final int dataColumnIndex ) {    	
		String value = "";
		final Row row = model.getRowAtVisualIndex(visualRowIndex);
		if (row != null) {

			if (dataColumnIndex >= 0) {
				value = model.toSequenceableString(row, dataColumnIndex);
				if (Basics.NULL.equals(value)) { // err ... want "" instead
							if ((model.showFilterUI && visualRowIndex<=ROW_IDX_FILTER_VALUE) || row.get(dataColumnIndex)==null){
								return "";
					}
				
				}
			}
		}
		
    	
		return value;
	}        
    
    private int getDataColumnIndexForFocusOrFirstHighlighedOrFirstSortedAndVisibleOrLeftMost() {
		int dataColumnIndex = -1;
		if (model.focusModelColumnIndex >= 0) {
			dataColumnIndex = model.getDataColumnIndex(model.focusModelColumnIndex);
			autoCompleteVisualColumnIndex = SwingBasics.getVisualIndexFromModelIndex(this,
					model.focusModelColumnIndex);
		} else {
			int[] highlightedModelColumnIndexes = cellHighlighter.getSelectedColumns();
			if (highlightedModelColumnIndexes.length >= 1) {
				dataColumnIndex = model.getDataColumnIndex(highlightedModelColumnIndexes[0]);
				autoCompleteVisualColumnIndex = SwingBasics.getVisualIndexFromModelIndex(this,
						highlightedModelColumnIndexes[0]);
			} else {
				final int[] sortedDataColumnIndexes = model.getSort();
				if (sortedDataColumnIndexes.length > 0) {
					final int n = getColumnCount();
					int[] allColumnIndexes = new int[n];
					for (int i = 0; i < n; i++) {
						allColumnIndexes[i] = model.getDataColumnIndexFromVisualIndex(i);
					}

					for (int i = 0; dataColumnIndex < 0 && i < sortedDataColumnIndexes.length; i++) {
						for (int j = 0; dataColumnIndex < 0 && j < allColumnIndexes.length; j++) {
							if (allColumnIndexes[j] == sortedDataColumnIndexes[i]) {
								if (!model.isHidden(sortedDataColumnIndexes[i])) {
									dataColumnIndex = sortedDataColumnIndexes[i];
									autoCompleteVisualColumnIndex = model.getVisualColumnIndexFromDataColumnIndex(j);
								}
							}
						}
					}
				}
				// use left most if no sorted or all sorted are hidden
				if (dataColumnIndex < 0) {
					dataColumnIndex = model.getDataColumnIndexFromVisualIndex(0);
					autoCompleteVisualColumnIndex = 0;
				}
			}
		}
		return dataColumnIndex;
	}
    
    
    private int searchForwardForAutoComplete(final int startRow,
			final int endRow, final boolean hitEnd) {
    	final String se=autoCompleteSearchEntry.toLowerCase();
		if (!tableWideSearch) {
			for (int i = startRow; i < endRow; i++) {
				final String val = getStringValueAt(i, autoCompleteDataColumnIndex);
				if (Basics.HtmlBody.startsWith(val.toLowerCase(), se)) {
					return i;
				}
			}
		} else if (autoCompleteDataColumnIndex>-1){
			int vi = model.getVisualColumnIndexFromDataColumnIndex(autoCompleteDataColumnIndex)+ (hitEnd?0:1);			
			for (int i = startRow; i < endRow; i++) {
				for (; vi < getColumnCount(); vi++) {
					autoCompleteDataColumnIndex = model
							.getDataColumnIndexFromVisualIndex(vi);
					final String val = getStringValueAt(i, autoCompleteDataColumnIndex);
					final Basics.HtmlBody hb=new Basics.HtmlBody(val);
					if (Basics.HtmlBody.contains(val.toLowerCase(), se)) {
						return i;
					}
				}
				vi = 0;
				autoCompleteDataColumnIndex = model.getDataColumnIndexFromVisualIndex(vi);
			}
			autoCompleteDataColumnIndex = model.getDataColumnIndexFromVisualIndex(0);
		}
		return -1;
	}

    private int searchBackwardForAutoComplete(final int startRow,
			final int endRow, final boolean hitEnd) {
    	final String se=autoCompleteSearchEntry.toUpperCase();

		if (!tableWideSearch) {
			for (int i = startRow; i > endRow; i--) {
				if (!tableWideSearch) {
					final String val = getStringValueAt(i, autoCompleteDataColumnIndex);
					final Basics.HtmlBody hb=new Basics.HtmlBody(val);
					if (hb.getBodyOrOriginal().toUpperCase().startsWith(se)) {
						return i;
					}
				}
			}
		} else {
			int vi = model
					.getVisualColumnIndexFromDataColumnIndex(autoCompleteDataColumnIndex) -(hitEnd?0:1);
			for (int i = startRow; i > endRow; i--) {
				for (; vi >= 0; vi--) {
					autoCompleteDataColumnIndex = model
							.getDataColumnIndexFromVisualIndex(vi);
					final String val = getStringValueAt(i, autoCompleteDataColumnIndex);
					final Basics.HtmlBody hb=new Basics.HtmlBody(val);
					if (hb.getBodyOrOriginal().toUpperCase().contains(se)) {
						return i;
					}
				}
				vi = getColumnCount()-1;
				autoCompleteDataColumnIndex = model.getDataColumnIndexFromVisualIndex(vi);
			}
			autoCompleteDataColumnIndex = model.getDataColumnIndexFromVisualIndex(getColumnCount()-1);
		}
		return -1;
	}

	public boolean isAutoCompleteItemFound() {
		return isAutoCompleteItemFound;
	}

	
	boolean useUrlRenderer = true;
	final ActionListener urlAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			useUrlRenderer = !useUrlRenderer;
			if (useUrlRenderer) {
				setDefaultRenderer(URL.class, new UrlRenderer());
				setDefaultEditor(URL.class, new UrlEditor());
			} else {
				setDefaultRenderer(URL.class, baseRenderer);
				setupStringConverterEditor(URL.class, new ResetableField());
			}
			model.refreshShowingTable(false);

		}

	};
	
	boolean supressUpdateUIBecauseItKillsCursorPositionAndOtherStuff=false;
	public static PersonalizableTable creatingWhileCellEditing;

	private int newRowCreatedWhileCellEditing=-1, updateUICalledWhileEditing=0;
	boolean likeDude__WeAreAlreadyStoppingEditingDude=false;
	public void editingStopped(final ChangeEvent e){
		if (!likeDude__WeAreAlreadyStoppingEditingDude){
			likeDude__WeAreAlreadyStoppingEditingDude=true;	
			isKeyBindingEvent = false;
			isEditing=true;
			super.editingStopped(e);
			isEditing=false;
			likeDude__WeAreAlreadyStoppingEditingDude=false;
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			if (postCellEditAction!=null ){
				postCellEditAction.actionPerformed(new ActionEvent(this, 0,"postCellEditAction"));
				postCellEditAction=null;
			} 
			if (model.getUngroupedModel().postCellEditAction!=null) {
				model.getUngroupedModel().postCellEditAction.actionPerformed(new ActionEvent(this, 0,"postCellEditAction"));
			}
			if (newRowCreatedWhileCellEditing>=0 && newRowCreatedWhileCellEditing < getRowCount()){
				removeRowSelectionInterval(newRowCreatedWhileCellEditing, newRowCreatedWhileCellEditing);
				setRowSelectionInterval(newRowCreatedWhileCellEditing, newRowCreatedWhileCellEditing);
				newRowCreatedWhileCellEditing=-1;
			}
			if (updateUICalledWhileEditing>0){
				updateUICalledWhileEditing=0;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateUI();
					}
				});
			}
		}
	}
	
	public ActionListener postCellEditAction=null;
	
	public void updateUI(){
		if (isEditing()){
			updateUICalledWhileEditing++;
		}else{
			super.updateUI();
			if (scrollPane != null){
				scrollPane.updateUI();
			}
			
		}
		
	}
	
/*	public void requestFocus(){
		new Exception("focussing on "+model.key).printStackTrace();
		super.requestFocus();
	}
 */
	
	static void setIcon(final JLabel icon, final boolean isAscending, final boolean isCustom){
		if (isAscending){
			if (isCustom){
				icon.setIcon(MmsIcons.getSortCustomDown16Icon());
			} else{
				icon.setIcon(MmsIcons.getSortAscending16Icon());
			}
		} else {
			if (isCustom){
				icon.setIcon(MmsIcons.getSortCustomUp16Icon());
			} else{
				icon.setIcon(MmsIcons.getSortDescending16Icon());
			}
		}		
	}

	void setStandardColors(final Component component, final int visualRowIndex, final boolean isSelected){
		if (isSelected){
			component.setForeground(CellHighlighter.disabledSelectedForeground);
			component.setBackground(CellHighlighter.disabledSelectedBackground);
		}else{
        component.setForeground(CellHighlighter.disabledForeground);
        component.setBackground(dither(((visualRowIndex % 2) == 0) ? _background :
        	getAlternatingRowColor(), 1));
		}
	}
	
	private Color getAlternatingRowColor() {
		if (model != null && model.instanceAlternatingRowColor != null) {
			return model.instanceAlternatingRowColor;
		}
		return PersonalizableTableModel.globalAlternatingRowColor;
	}
	public static Color dither(final Color color, final int deviationFromStandard) {
		final int d = deviationFromStandard * 6, 
		r = rgbRange(color.getRed(),  d), 
		g = rgbRange(color.getGreen(),  d), 
		b = rgbRange(color.getBlue(), d), 
		alpha = color.getAlpha();
		return new Color(r, g, b, alpha);
	}
	static int rgbRange(final int input, final int d){
		if (input+d<0){
			return input-d;
		} else if (input+d > 255){
			return input-d;
		}
		return input+d;
	}
    
	boolean autoCompletingFromTextField=false;
	JComponent searchStatusComponent=this;
	private int searchComponentHeightOffset=45;

	final ActionListener searchDown=new ActionListener(){
		public void actionPerformed(final ActionEvent e){
			tableWideSearch=false;
			if (isFound() || autoCompletingFromTextField){
				autoComplete(getAutoCompleteSelectedRow()+1, true);
			}else {
				model.showTextAtFocusCell(Basics.toHtmlUncenteredSmall(
						"Search tip", "Every cell in this column will be checked <br>to see if it <u>starts with</u> your search value.<br>Type one now."));
			}
			tableWideSearch=true;
		}
	}, searchUp=new ActionListener(){
		public void actionPerformed(final ActionEvent e){
			tableWideSearch=false;
			autoComplete(getAutoCompleteSelectedRow()-1, false);
			tableWideSearch=true;
		}
	}, searchRight=new ActionListener(){
		public void actionPerformed(final ActionEvent e){
			tableWideSearch=true;
			if (isFound() || autoCompletingFromTextField){
				autoComplete(getAutoCompleteSelectedRow(), true);
			} else {				
				model.showTextAtFocusCell(
						Basics.toHtmlUncenteredSmall(
								"Search tip", "Every cell will be checked <br>to see if it <u>contains</u> your search value.<br> Type one now."));
			}
		}
	}, searchLeft=new ActionListener(){
		public void actionPerformed(final ActionEvent e){
			tableWideSearch=true;
			autoComplete(getAutoCompleteSelectedRow(), false);
		}
	};
	
	private boolean selectedSinceLastSearch=true;
	
	
    void setDragAndDrop(final TransferHandler newHandler) {
    	setDragEnabled(true);
        setTransferHandler(newHandler);
        setParentDragAndDrop();
    }
    
    private void setParentDragAndDrop(){
    	if (getDragEnabled()){
        final Container c=getParent();
        if (c instanceof JViewport){
        	((JViewport)c).setTransferHandler(getTransferHandler());
        }
    	}

    }
		
    public void scrollToVisible(final Row row){
    	final int n=getRowCount();
    	for (int i=0;i<n;i++){
    		final Row r2=model.getRowAtVisualIndex(i);
    		if (r2==row){
    			scrollToVisible(i, 0);
    		}
    	}
    }
    

    private int []refreshColumnIndexesIfOpaque;
    ImageIcon refreshBackgroundImage=null;
    private int startingSizeForBackground=-1;
    public void treatTheBackgroundImageAsModifyingAdvice(){
    	refreshBackgroundImage=backgroundImage;
		final PersonalizableDataSource pd=model.getDataSource();
		final java.util.List<Row> l=pd.getFilteredDataRows();
		final int n=l.size();
    	startingSizeForBackground=n;
    	refreshColumnIndexesIfOpaque=columnsThatMustBeOpaque;
    }
     void refreshBackground(){
    	if (refreshBackgroundImage!=null){
    		final PersonalizableDataSource pd=model.getDataSource();
    		final java.util.List<Row> l=pd.getFilteredDataRows();
    		final int n=l.size();
    		if (startingSizeForBackground>=0){
				if (n != startingSizeForBackground) {
					startingSizeForBackground = -1;
					setBackgroundImage(null, null);
				}
    		} else if (n==0){
    			setBackgroundImage(refreshBackgroundImage, refreshColumnIndexesIfOpaque);    			
    		} else {
    			setBackgroundImage(null, null);
    		}
    	}
    }

     public interface ColumnSelectionListener{
    	 	void selected(PersonalizableTable table, int dataColumnIndex);
    	 	void unselected(PersonalizableTable table, int dataColumnIndex);
     }
     
     private void scrollAllParentViewportsIfSelectingByKeyboard() {
			if (isProcessingKey) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!isInitializingUpCellEditor) {
		    	final int rowIndex=PersonalizableTable.this.getSelectedRow();
		    	final int colIndex=PersonalizableTable.this.getSelectedColumn();
				SwingBasics
						.scrollRectToVisibleForAllParents(
								PersonalizableTable.this, getCellRect(rowIndex, colIndex,true));
					}
					}
				});
			}
     }
     private final StringBuilder sb=new StringBuilder(500);
     
     
     private static final String TOOL_TIP_HEADER="<html><ul><li>Double-click for column <b>specific</b> options<li>Right-click for table options<li>Click & drag to move column<li>Click & drag borders to resize column</ul></html>";

     public static PersonalizableTable lastTableEdited;
     public PersonalizableDataSource getUngroupedDataSource(){
    	 return model.getUngroupedModel().getDataSource();
     }
     public PersonalizableTableModel getUngroupedModel(){
    	 return model.getUngroupedModel();
     }
     
     public void refreshHeader(){
         getTableHeader().resizeAndRepaint();
     }
     
	private class HeaderMouseOver extends MouseAdapter{
		int over=-1;
		int overResize=-1;
	    private boolean canResize(final TableColumn column) { 
	    	return (column != null) && header.getResizingAllowed()
		           && column.getResizable(); 
		}

        private TableColumn getResizingColumn(Point p, int column) {
			if (column == -1) {
				return null;
			}
			final Rectangle r = header.getHeaderRect(column);
			r.grow(-4, 0);
			if (r.contains(p)) {
				return null;
			}
			final int midPoint = r.x + r.width / 2;
			final int columnIndex;
			if (header.getComponentOrientation().isLeftToRight()) {
				columnIndex = (p.x < midPoint) ? column - 1 : column;
			} else {
				columnIndex = (p.x < midPoint) ? column : column - 1;
			}
			if (columnIndex == -1) {
				return null;
			}
			return header.getColumnModel().getColumn(columnIndex);
		}

        private boolean isInCenterColumn(final Point p, int column) {
			if (column == -1) {
				return false;
			}
			final Rectangle r = header.getHeaderRect(column);
			r.grow(-8, 0);
			if (!r.contains(p)) {
				return false;
			}
			
			return true;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}

    	private final Cursor handCursor=new Cursor(Cursor.HAND_CURSOR);
    	private final Cursor defaultCursor=new Cursor(Cursor.DEFAULT_CURSOR);

		private Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		private final ToolTipManager tt=ToolTipManager.sharedInstance();
		final int defaultDelay=tt.getInitialDelay();
		public void mouseMoved(final MouseEvent e) {
			
            if (!header.isEnabled()) {
                return;
            }
            final Point p=e.getPoint();
            final int column=header.columnAtPoint(p);
            if (canResize(getResizingColumn(p, column)) != 
            		(header.getCursor() == resizeCursor)) {
            	unhighlight();
            	if (overResize!=column){            		
            		header.setCursor(resizeCursor);
                	overResize=column;
            	}
            } else if (isInCenterColumn(p, column)){
            	overResize=-1;
            	if (over != column){
            		if (over==-1){
            			header.setCursor(handCursor);
            		}
            		over=column;
            		header.repaint();
            	}
            	tt.setInitialDelay(2);
            } else {
            	unhighlight();
            }            
		}
		
	    public void mouseExited(MouseEvent e) {
	    	unhighlight();
	    }
	    
	    private void unhighlight(){
    		tt.setEnabled(false);
    		tt.setInitialDelay(defaultDelay);
    		SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					tt.setEnabled(true);
				}
			});
    		
        	overResize=-1;
        	over=-1;
        	header.setCursor(defaultCursor);
    		header.repaint();
	    }

	}
	private final HeaderMouseOver headerMouseOver=new HeaderMouseOver();
	public static String SET_COLOR="setColor";
	public static String SET_COLUMN_COLOR="Set column color";

	public void editSelectedCellLater(final int dataColumnIndex) {
		final int selectedRow = getSelectedRow();
		if (selectedRow >= 0) {
			final int vi = model
					.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
			if (lastCellEditEvent != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								AutoComplete.CellEditor.showPopupOnFirstFocus = true;
								_editCellAt(selectedRow, vi,
										null	);
							}
						});
					}
				});
			}
		}
	}

}
