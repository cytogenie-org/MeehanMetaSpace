
package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.table.*;
//import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import javax.swing.plaf.metal.*;

import com.MeehanMetaSpace.*;

import javax.swing.event.*;


public abstract class SelectableCell implements Comparable {
    public boolean hasOptions() {
        return isViewable() || isDeletable() || isRemovable() || isCreatable() ||
          isAddable();
    }

    public boolean equals(final Object thatValue) {
        // special case (wrong?:gotta deadline to hit!)
        if (thatValue instanceof String) {
            return toString().equalsIgnoreCase((String) thatValue);
        }
        if (thatValue == this) {
            return true;
        }
        if (!(thatValue instanceof SelectableCell)) {
            return false;
        }
        return toString().equals(((SelectableCell) thatValue).toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(final Object thatValue) {
        // special case (wrong?:gotta deadline to hit!)
        if (thatValue instanceof String) {
            return toString().compareToIgnoreCase((String) thatValue);
        }
        if (!(thatValue instanceof SelectableCell) || thatValue == this) {
            return 0;
        }
        final String thisString = toString(),
                                  thatString = ((SelectableCell) thatValue).
                                               toString();
        return thisString.compareToIgnoreCase(thatString);
    }

    private final SortValueReinterpreter sortValueReinterpreter;
    private final StringConverter sc;


    protected SelectableCell(final MetaRow metaRow, int dataColumnIndex) {
        this(metaRow.getSortValueReinterpreter(dataColumnIndex),
             TableBasics.getBestStringConverter(metaRow, dataColumnIndex));
    }

    protected SelectableCell(
      final SortValueReinterpreter sortValueReinterpreter,
      final StringConverter sc) {
        this.sortValueReinterpreter = sortValueReinterpreter;
        this.sc = sc == null ? DefaultStringConverters.get() : sc;
    }

    // all the following protected methods experience self use by the root class
    protected abstract void select(Object value);

    protected abstract void choose();

    protected abstract void remove(Object value);

    protected abstract void add(Object value);

    protected abstract void createNew();

    protected abstract void delete(Object value);

    protected abstract void view(Object value);

    protected abstract int getMaximumCardinality();

    protected abstract int getMinimumCardinality();

    protected abstract boolean allowsDuplicates();

    public static class Default extends SelectableCell {
        final Row row;
        final int dataColumnIndex;
        final char newDelimiter;
        protected Default(
          final MetaRow metaRow,
          final Row row,
          final int dataColumnIndex,
          final char newDelimiter,
          final String[] feasibleCellValues) {
            this(metaRow.getSortValueReinterpreter(dataColumnIndex),
                 metaRow.getStringConverter(dataColumnIndex),
                 row,
                 dataColumnIndex,
                 newDelimiter,
                 feasibleCellValues);
        }

        protected Default(
          final SortValueReinterpreter sortValueReinterpreter,
          final StringConverter sc,
          final Row row,
          final int dataColumnIndex,
          final char newDelimiter,
          final String[] feasibleCellValues) {
            super(sortValueReinterpreter, sc);
            this.row = row;
            this.dataColumnIndex = dataColumnIndex;
            this.newDelimiter = newDelimiter;
            final Object o = row.get(dataColumnIndex);
            if (o != null) {
                setCurrentValues(Basics.toStrings(o.toString(),
                                                  "" + newDelimiter));
            }
            setFeasibleCellValues(Basics.toList(feasibleCellValues));
        }

        protected Default(
          final StringConverter sc,
          final Row row,
          final int dataColumnIndex,
          final char newDelimiter,
          final String[] feasibleCellValues) {
            this(null, sc, row, dataColumnIndex, newDelimiter,
                 feasibleCellValues);
        }

        protected String getAddAnomaly(final Object value) {
            return null;
        }

        protected String getRemoveAnomaly(final Object value) {
            return null;
        }

        protected String getDeleteAnomaly(final Object value) {
            return null;
        }

        protected void delete(Object value) {
        }

        protected void choose() {
        }

        protected void createNew() {
        }

        protected void view(Object value) {
        }

        protected void select(Object value) {
            row.set(dataColumnIndex, value);
        }

        public String toString() {
            return TableBasics.toString(super.sc, getCurrentValues(),
                                        newDelimiter + " ", true,
                                        super.sortValueReinterpreter);
        }

        protected void remove(final Object value) {
            getCurrentValues().remove(value);
            setRow();
        }

        protected void add(final Object value) {
            final Collection c = getCurrentValues();
            c.add(value);
            setRow();
        }

        private void setRow() {
            row.set(dataColumnIndex, toString());
        }

        protected int getMaximumCardinality() {
            return Integer.MAX_VALUE;
        }

        protected int getMinimumCardinality() {
            return 0;
        }

        protected boolean allowsDuplicates() {
            return true;
        }

    }


    /**
     * Subclasses can override this behavior if it varies from the default
     * The public super class method getTableCellEditor() uses this protected
     * method.
     *
     * @return collection of Item instances
     */
    protected Collection getRemovableValues() {
        return getCurrentValues();
    }

    protected abstract String getRemoveAnomaly(final Object value);

    private Collection<Item> getRemovableItems() {
        final Collection<Item> returnValue;
        if (isRemovable()) {
            returnValue = getSubtractableItems(CMD_REMOVE);
            for (final Iterator it = returnValue.iterator(); it.hasNext(); ) {
                Item item = (Item) it.next();
                item.setAnomaly(getRemoveAnomaly(item.value));
            }
        } else {
            returnValue = Collections.EMPTY_LIST;
        }
        return returnValue;
    }

    protected Collection getDeletableValues() {
        return getCurrentValues();
    }

    protected abstract String getDeleteAnomaly(final Object value);


    private Collection<Item> getDeletableItems() {
        final Collection<Item> returnValue;
        if (isDeletable()) {
            returnValue = getSubtractableItems(CMD_DELETE);
            for (final Iterator it = returnValue.iterator(); it.hasNext(); ) {
                Item item = (Item) it.next();
                item.setAnomaly(getDeleteAnomaly(item.value));
            }
        } else {
            returnValue = Collections.EMPTY_LIST;
        }

        return returnValue;
    }


    private Collection getSubtractableItems(String cmd) {
        final Collection returnValue = new Vector();
        final int min = getMinimumCardinality();
        if (min < getCurrentValues().size()) {
            for (final Iterator it = getCurrentValues().iterator(); it.hasNext(); ) {
                returnValue.add(new Item(it.next(), cmd));
            }
        }
        return returnValue;
    }
    
    private boolean useAddAnomalies=false;
    
    private Collection<Item> getAddableItems() {
    	hideUnaddable= viewUnaddable=null;
        final Collection<Item> returnValue = new Vector<Item>();
        if (isAddable()) {
            final int max = getMaximumCardinality();
            if (max == 1 || max > getCurrentValues().size()) {
                final Collection c = getAddableValues(),
                                     current = getCurrentValues();
                final String cmd = max == 1 ? CMD_SELECT : CMD_ADD;
                int unaddableCnt=0;
                for (final Iterator it = c.iterator(); it.hasNext(); ) {
                    final Object value = it.next();
                    final Item item = new Item(value, cmd, max > 1, true);
                    final String anomaly=getAddAnomaly(value);
                    final boolean emptyAnomaly=Basics.isEmpty(anomaly);
                    if (useAddAnomalies || emptyAnomaly) {
						item.setAnomaly(anomaly);
						returnValue.add(item);
					} 
                    if (!emptyAnomaly) {
						unaddableCnt++;
					}
                    
                }
                if (unaddableCnt>0) {
                	if (!useAddAnomalies ) {
                		viewUnaddable=new Item(CMD_VIEW_UNADDABLE + unaddableCnt + CMD_UNADDABLE);
                	} else {
                		hideUnaddable=new Item(CMD_HIDE_UNADDABLE + unaddableCnt + CMD_UNADDABLE);
                	}                	
                }
            }
        }
        return returnValue;
    }
    
    private Item viewUnaddable, hideUnaddable;
    
    private void addShowUnaddable(final Collection<Item> items) {
		if (viewUnaddable != null) {
			items.add(viewUnaddable);
		}
	}

	private void addHideUnaddable(final Vector<Item> items) {
		if (hideUnaddable != null) {
			items.add(hideUnaddable);
		}
	}

    protected abstract String getAddAnomaly(final Object value);

    protected Collection getCurrentValues() {
        return currentCellValues;
    }

    protected Collection getAddableValues() {
        final Collection returnValue = new Vector();
        final boolean dupsOk = allowsDuplicates();
        for (final Iterator it = feasibleCellValues.iterator();
                                 it.hasNext(); ) {
            final Object value = it.next();
            if (dupsOk || !getCurrentValues().contains(value)) {
                returnValue.add(value);
            }
        }

        return returnValue;
    }

    public boolean isAddable() {
        return true;
    }

    public boolean isRemovable() {
        return true;
    }

    public boolean isCopyable() {
        return true;
    }

    public boolean isDeletable() {
        return false;
    }

    public boolean isCreatable() {
        return false;
    }

    public boolean isChoosable() {
        return false;
    }

    public boolean isViewable() {
        return false;
    }

    protected String getViewText(final Object value){
    	return null;
    }
    public boolean isViewable(final Object value) {
        return isViewable();
    }

    // sort based on sortvaluereinterpreter
    private final Collection feasibleCellValues = new Vector(),currentCellValues = new Vector();

    protected void setFeasibleCellValues(final Collection feasibleCellValues) {
        this.feasibleCellValues.clear();
        this.feasibleCellValues.addAll(feasibleCellValues);
    }

    protected void setCurrentValues(final Collection currentCellValues) {
        this.currentCellValues.clear();
        this.currentCellValues.addAll(currentCellValues);
    }

    protected void clearCurrentCellValues() {
        currentCellValues.clear();
    }


    public String getCreateAnomaly() {
    	return null;
    }
    
    protected boolean putNewItemFirstInsteadOfLast(){
    	return true;
    }
    
    protected Vector<Item> getOptions() {
        final Vector<Item> items = new Vector<Item>();
        final Collection<Item>a=getAddableItems();
        addHideUnaddable(items);
        items.addAll(a);
        if (isViewable()) {
            for (final Object item : getCurrentValues()) {
                if (isViewable(item)) {
                	final Item item2=new Item(item, CMD_VIEW);
                    items.add(item2);
                    final String txt=getViewText(item);
                    if (txt != null){
                    	final String txt2=item2.toString;
                    	item2.toString=txt;
                    	item2.customString=txt2;
                    }
                }
            }
        }
        items.addAll(getRemovableItems());
        items.addAll(getDeletableItems());
        if (isChoosable()) {
            items.add(new Item(getChooseText()));
        }
        if (isCreatable()) {
            final int max=getMaximumCardinality(), cnt=getCurrentValues().size();
            if (cnt < max || max==1 ) {	
            	final boolean first=putNewItemFirstInsteadOfLast();
            	final Item item=new Item(getNewText());
            	String s=getCreateAnomaly();
            	if (s!= null && !s.startsWith("<html>")) {
            		s=Basics.toHtmlUncentered("Can not create!", s);
            	}
            	item.anomaly=s;
            	if (first){
            		items.add(0, item);
            	} else {
            		items.add(item);
            	}
			}
        }
        for (final String other:getOtherItems()){
        	items.add(new Item(other));
        }
        addShowUnaddable(items);
        return items;
    }

    protected Collection<String>getOtherItems(){
    	return Basics.UNMODIFIABLE_EMPTY_LIST;
    }
    
    protected void doOtherItems(final String cmd){    	
    }

    protected boolean sortByCommand() {
        return false;
    }


    public TableCellEditor getTableCellEditor() {
    	return getTableCellEditor(false);
    }
    
    protected TableCellEditor getTableCellEditor(final boolean parseUnderscore) {
        final JComboBox comboBox = getComboBox(-1, null, parseUnderscore);
        class _ComboCellEditor extends ComboCellEditor {
        	private boolean isAdjustingAddable=false;
        	protected boolean shouldStopEditing(final JComboBox jcb) {
        		if (isAdjustingAddable || !super.shouldStopEditing(jcb)) {
        			return false;
        		}
        		final Object item=jcb.getSelectedItem();
        		if (item instanceof Item) {
        			final Item it=(Item)item;
        			if (it.cmd.endsWith(CMD_UNADDABLE) && (it.cmd.startsWith(CMD_VIEW_UNADDABLE) || it.cmd.startsWith(CMD_HIDE_UNADDABLE)) ){
                    	useAddAnomalies=!useAddAnomalies;
                    	isAdjustingAddable=true;
                        refresh(lastComboBoxGotten, new RefreshListener() {
							public void actionPerformed(final JComboBox comboBox) {

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										if (comboBox.getItemAt(0) != SelectableCell.this) {
											comboBox.insertItemAt(SelectableCell.this, 0);
										}
										comboBox.setSelectedIndex(0);
										SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												comboBox.showPopup();
											}
										});
										isAdjustingAddable = false;
									}
								});

							}
						});
                        return false;
        			} else if (it.anomaly!= null) {
        				comboBox.setToolTipText(it.anomaly);
                        Toolkit.getDefaultToolkit().beep();
                        ToolTipOnDemand.getSingleton().show(comboBox, true, comboBox.getWidth(), 0-comboBox.getHeight(), false);
        				return false;
        			}
        		}
        		return true;
        	}
            _ComboCellEditor(final JComboBox jcb) {
                super(jcb);                

            }

            boolean init = false;

            public boolean isCellEditable(final EventObject evt) {

                final boolean ok = super.isCellEditable(evt);
                if (!init && ok) {
                    init = true;
                    refresh(comboBox, showAllAfterRefresh);
                    if (comboBox.getItemCount()==0) {
                    	return false;
                    }
                    comboBox.addActionListener(new MyActionListener(comboBox));
                }
                return ok;
            }


        };
        return new _ComboCellEditor(comboBox);
    }


    // This key selection manager will handle selections based on multiple keys.
    private final static class KeySelectionManager implements JComboBox.
      KeySelectionManager {
        private void updateUI() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (comboBox.getRenderer() instanceof Renderer) {
                        final Renderer r = (Renderer) comboBox.getRenderer();
                        if (r.list != null) {
                            r.list.invalidate();
                            r.list.repaint();
                        }
                    }
                }
            });
        }

        private long lastKeyTime = 0;
        private String pattern = "";

        public int selectionForKey(final char aKey, final ComboBoxModel model) {
            final int idx = _selectionForKey(aKey, model);
            if (idx >= 0) {
                if (!comboBox.isPopupVisible()) {
                    comboBox.showPopup();
                } else {
                    updateUI();
                }
            }
            return idx;
        }

        private int _selectionForKey(final char aKey, final ComboBoxModel model) {
            // Find index of selected item
            if (aKey > 126 || aKey < 32) {
                return -1;
            }
            int selIx = 01;
            final Object sel = model.getSelectedItem();
            if (sel != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (sel.equals(model.getElementAt(i))) {
                        selIx = i;
                        break;
                    }
                }
            }

            // Get the current time
            final long curTime = System.currentTimeMillis();

            // If last key was typed less than delayMilliSecs ms ago, append to current pattern
            if (curTime - lastKeyTime < delayMilliSecs) {
                pattern += ("" + aKey).toLowerCase();
                lastKeyTime = curTime;
                System.out.println("Looking for " + pattern);
                if (sel instanceof Item) {
                    final Item item = (Item) sel;
                    final String s = item.getBrowserString().toLowerCase();
                    if (s.startsWith(pattern)) {
                        System.out.println("  *  -> found at " + selIx);
                        return selIx;
                    }
                }
            } else {
                lastKeyTime = curTime;
                pattern = ("" + aKey).toLowerCase();
                // Save current time
                System.out.println("Looking for " + pattern);

            }

            // Search forward from current selection
            for (int i = selIx + 1; i < model.getSize(); i++) {
                final Object o = model.getElementAt(i);
                if (o instanceof Item) {
                    final Item item = (Item) o;
                    final String s = item.getBrowserString().toLowerCase();
                    if (s.startsWith(pattern)) {
                        System.out.println("  cur -> found at " + i);

                        return i;
                    }
                }
            }

            // Search from top to current selection
            for (int i = 0; i <= selIx; i++) {
                if (model.getElementAt(i) != null) {
                    final Object o = model.getElementAt(i);
                    if (o instanceof Item) {
                        final Item item = (Item) o;
                        final String s = item.getBrowserString().toLowerCase();
                        if (s.startsWith(pattern)) {
                            System.out.println("  top  -> found at " + i);

                            return i;
                        }
                    }
                }
            }
            System.out.println("  Not found! ");

            return -1;
        }

        final int delayMilliSecs;
        final JComboBox comboBox;

        KeySelectionManager(final int delayMilliSecs, final JComboBox comboBox) {
            this.delayMilliSecs = delayMilliSecs;
            this.comboBox = comboBox;
        }
    }


    private KeySelectionManager ksm;

    private class MyActionListener implements ActionListener {
        final JComboBox combo;
        Item currentItem;

        MyActionListener(final JComboBox comboBox) {
            this.combo = comboBox;
            /*            if (comboBox.getItemCount() > 0) {
                            comboBox.setSelectedIndex(0);
                        }*/
            final Object tempItem = comboBox.getSelectedItem();
            if (tempItem instanceof SelectableCell &&
                !(tempItem instanceof Item)) {
                currentItem = null;

            } else {
                currentItem = (Item) tempItem;
            }
        }

        public void actionPerformed(final ActionEvent e) {
            if (!resizedUI.ignoreAction) {
                final int idx = combo.getSelectedIndex();
                if (idx >= 0) {
                    final Object tempItem = combo.getModel().getElementAt(idx);
                    if (tempItem instanceof SelectableCell &&
                        !(tempItem instanceof Item)) {
                        currentItem = null;
                        return;
                    } else
                    if (
                      (tempItem instanceof Item && ((Item) tempItem).anomaly != null)) {
                        ToolTipOnDemand.getSingleton().showWithCloseButton(combo);
                        return;
                    }
                    currentItem = (Item) tempItem;
                }
            }
        }
    }


    
    private final class Renderer extends JLabel implements ListCellRenderer {

    	boolean showDropDownValues(){
//    		return Basics.isMac() ? comboBox.hasFocus():comboBox.isPopupVisible();
    		return comboBox.hasFocus();
    	}
        final JSeparator separator;
        private final JComboBox comboBox;
        final boolean parseUnderscore;
        Renderer(final JComboBox comboBox, final boolean parseUnderscore) {
            setOpaque(true);
            this.parseUnderscore=parseUnderscore;
            this.comboBox=comboBox;
            setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
            separator = new JSeparator(JSeparator.HORIZONTAL);
        }

        private JList list;
        public Component getListCellRendererComponent(
          final JList list,
          final Object value,
          final int index,
          final boolean isSelected,
          boolean cellHasFocus) {
            this.list = list;
            String str;
            if (value == null) {
                str = "";
            } else {
                if (index == list.getSelectedIndex() && value instanceof Item &&
                    !Basics.isEmpty(ksm.pattern)) {
                    str = ((Item) value).makeToString(ksm.pattern);
                } else {
                    str = value.toString();
                }
                if (value instanceof Item ){
					if (useIconPrefixes()) {
						URL url=null;
						if (CMD_VIEW.equals(((Item) value).cmd)) {
							url = getIconURLForViewing(((Item) value).value);	
						} else 	if (getNewText().equals(((Item) value).cmd)) {
							url=MmsIcons.getURL("new.gif");
						} else if (CMD_DELETE.equals(((Item) value).cmd)) {
							url=MmsIcons.getURL("delete16.gif");
						}else if (CMD_ADD.equals(((Item) value).cmd)) {
							url=MmsIcons.getURL("plus.gif");
						}else if (getChooseText().equals(((Item) value).cmd)) {
							url=MmsIcons.getURL("find16.gif");
						}
						if (url != null) {
							ThreadSafeStringBuilder tssb = ThreadSafeStringBuilder
									.get();
							final StringBuilder sb = tssb.lock();
							sb.append("<html>");
							final String urlStr = AutoComplete
									.encodeImg(url);
							sb.append(urlStr);
							final boolean hasHtml = str
									.startsWith("<html>")
									&& str.endsWith("</html>");
							if (CMD_VIEW.equals(((Item) value).cmd) && suppressViewCmd) {
								sb.append(getSuppressedViewHtmlAfterUrl());
							} 
							if (hasHtml) {
								sb.append(str.substring(6));
								
							} else {
								final String s = Basics.encodeHtml(str);
								sb.append(s);
								sb.append("</html>");
							}
							str = tssb.unlockString();
						}
					}
                }
            }
            if (SEPARATOR.equals(str)) {
                return separator;
            }
            if (value instanceof Item && ((Item) value).customString!= null && !showDropDownValues()) {
            	str=((Item) value).customString;
            }
            if (value instanceof Item && ((Item) value).anomaly != null) {
            	if (isSelected) {
					setBackground(Color.red);
					setForeground(list.getBackground());
				} else {
					setBackground(list.getBackground());
					setForeground(Color.red);
				}            	
                list.setToolTipText(((Item) value).anomaly);                
            } else {
                list.setToolTipText("");
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
            }
            setFont(list.getFont());
            if (parseUnderscore){
            	setText(str.replace('_', ' '));
            } else {
            	setText(str);
            }
            return this;
        }
    }


    private static final String
    CMD_VIEW_UNADDABLE="&lt;View ",
    CMD_HIDE_UNADDABLE="&lt;Hide ",
    CMD_UNADDABLE=" non addable&gt;",
      CMD_ADD = "Add",
    CMD_SELECT = "Select",
    CMD_DELETE = "Delete",
    CMD_REMOVE = "Remove",
    CMD_NEW = "&lt;New&gt;",
    CMD_VIEW = "View",
    CMD_NOTHING_TO_EDIT = "Nothing to edit",
    CMD_CHOOSE = "&lt;Add&gt;";

    protected String getChooseText(){
    	return CMD_CHOOSE;
    }
    
    protected String getNewText(){
    	return CMD_NEW;
    }
    
    protected String nothingToEdit = CMD_NOTHING_TO_EDIT;
    static final String
      SEPARATOR = "SEPARATOR";

    class Item implements Comparable {
        private final Object value;
        private final String cmd;
        private String toString;
        private String customString;
        private String anomaly;


        void setAnomaly(final String anomaly) {
            this.anomaly = anomaly;
        }

        private final boolean displayCmd, displayValue;

        public Item(
          final Object obj,
          final String cmd,
          final boolean displayCmd,
          final boolean displayValue) {
            this.value = obj;
            this.cmd = cmd;
            this.displayCmd = displayCmd;
            this.displayValue = displayValue;
            toString = makeToString(null);
            reinterpretedValue = sortValueReinterpreter == null ? null :
                                 sortValueReinterpreter.reinterpret(value);
        }

        public Item(
          final Object obj,
          final String cmd) {
            this.value = obj;
            this.cmd = cmd;
            this.displayCmd = true;
            this.displayValue = true;
            toString = makeToString(null);
            reinterpretedValue = sortValueReinterpreter == null ? null :
                                 sortValueReinterpreter.reinterpret(value);
        }

        public Item(final String cmd) {
            this.cmd = cmd;
            value = null;

            this.displayCmd = true;
            this.displayValue = true;
            toString = makeToString(null);
            reinterpretedValue = null;
        }

        public Item() {
            cmd = SEPARATOR;
            value = null;
            this.displayCmd = false;
            this.displayValue = false;
            toString = makeToString(null);
            reinterpretedValue = null;
        }

        final Comparable reinterpretedValue;

        public int compareTo(final Object thatValue) {
            if (!(thatValue instanceof Item) || thatValue == this) {
                return 0;
            }
            final Item that = (Item) thatValue;
            if (reinterpretedValue != null) {
                final Comparable other = sortValueReinterpreter.reinterpret(
                  that.value);
                return reinterpretedValue.compareTo(other);
            } else {
                if (this.value instanceof String &&
                    that.value instanceof String) {
                    return ((String)this.value).compareToIgnoreCase((String)
                      that.value);
                }
                if (this.value instanceof Comparable &&
                    that.value instanceof Comparable) {
                    return ((Comparable)this.value).compareTo(that.value);
                }
            }
            return this.value == null || that.value == null ?
              0 : this.value.toString().compareToIgnoreCase(that.value.toString());
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + (cmd == null ? 0 : cmd.hashCode());
            result = 37 * result + (value == null ? 0 : value.hashCode());
            return result;
        }

        public boolean equals(final Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Item)) {
                return false;
            }
            return Basics.equals(this.cmd, ((Item) other).cmd) &&
              Basics.equals(this.value, ((Item) other).value);
        }

        final String getBrowserString() {
            return value == null ? "" : sc.toString(value);
        }


        private String makeToString(final String userTyped) {
            if (editable && CMD_VIEW.equals(cmd)){
            	return getBrowserString();
           	}
            if (cmd.startsWith("<html>")){
            	return cmd;
            }
            final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
            final StringBuilder sb = tssb.lock();
            sb.append("<html>");
            if (displayCmd && cmd != null) {
                if (!suppressViewCmd || !CMD_VIEW.equals(cmd)) {
                    if (!cmd.equals(CMD_DELETE) && !cmd.equals(CMD_REMOVE)) {
                        sb.append("<b><i>");
                        sb.append(cmd);
                        if (value != null) {
                            sb.append(":");
                        }
                        sb.append("</i></b>&nbsp;&nbsp;");
                    } else {
                        sb.append("<b><i>");
                        sb.append(cmd);
                        if (value != null && (!suppressViewCmd || !CMD_VIEW.equals(cmd))) {
                            sb.append(":");
                        }
                        sb.append("</i></b>&nbsp;&nbsp;");
                    }
                }
            }
            if (displayValue) {
            	final String str=getBrowserString();
            	if (str.startsWith("<html>") && str.endsWith("</html>")){
            		tssb.unlock();
            		return str;
            	}
                final String s = Basics.encodeHtml(str);
                if (userTyped != null && s.toLowerCase().startsWith(userTyped)) {
                    sb.append("<b><font color='yellow'>");
                    sb.append(s.substring(0, userTyped.length()));
                    sb.append("</font></b>");
                    sb.append(s.substring(userTyped.length()));
                } else {
                    sb.append(s);
                }
            }
            sb.append("</html>");
            final String retVal = tssb.unlockString();
            return retVal;
        }

        public String toString() {
            return toString;
        }

        private boolean isExecutingCmd = false;
        public boolean isExecutingCmd() {
            return isExecutingCmd;
        }

        void executeCmd() {
        	executeCmd(null, null);
        }
        void executeCmd(final Object selected, final JComboBox  cb) {
            if (!resizedUI.ignoreAction && anomaly==null) {
                isExecutingCmd = true;
                if (cmd.equals(CMD_ADD)) {
                    SelectableCell.this.add(value);
                } else if (cmd.equals(getChooseText())) {
                    SelectableCell.this.choose();
                } else if (cmd.equals(CMD_REMOVE)) {
                    SelectableCell.this.remove(value);
                } else if (cmd.equals(CMD_VIEW)) {
                	if (editable && selected instanceof String){
                		if (!setToViewItem(cb, selected)){
                			SelectableCell.this.view(selected);
                		}
                	} else {
                		SelectableCell.this.view(value);
                	}
                } else if (cmd.equals(CMD_DELETE)) {
                    SelectableCell.this.delete(value);
                } else if (cmd.equals(getNewText())) {
                    SelectableCell.this.createNew();
                } else if (cmd.equals(CMD_SELECT)) {
                    SelectableCell.this.select(value);
                } else if (getOtherItems().contains(cmd)){
                	doOtherItems(cmd);
                }
                isExecutingCmd = false;
            }
        }
    }


    public String toString() {
        return TableBasics.toString(sc, getCurrentValues(), ", ", true,
                                    sortValueReinterpreter);
    }

    public interface RefreshListener {
        void actionPerformed(final JComboBox comboBox);
    }


    public RefreshListener showAllAfterRefresh = new RefreshListener() {
        public void actionPerformed(final JComboBox comboBox) {
            if (comboBox.getItemAt(0) != SelectableCell.this) {
                comboBox.insertItemAt(SelectableCell.this, 0);
            }
            comboBox.setSelectedIndex(0);
        }
    };

    public static final boolean setSelectedItem(final JComboBox comboBox,
                                                final Object arg) {
        boolean ok = false;
        if (arg != null) {
            final int n = comboBox.getItemCount();
            for (int i = 0; i < n; i++) {
                final Object o = comboBox.getItemAt(i);
                if (o instanceof Item) {
                    if (arg.equals(((Item) o).value)) {
                        comboBox.setSelectedIndex(i);
                        ok = true;
                        break;
                    }
                } else if (arg.equals(o)) {
                    comboBox.setSelectedIndex(i);
                    ok = true;
                    break;
                }
            }

        }
        return ok;
    }

    class ComboBox extends JComboBox {
        

        public boolean selectWithKeyChar(char keyChar) {
            resizedUI.ignoreAction = true;
            final boolean ok = super.selectWithKeyChar(keyChar);
            resizedUI.ignoreAction = false;
            return ok;
        }

        public void setSelectedIndex(final int idx) {
            if (idx == 0) {
                System.out.println();
            }
            super.setSelectedIndex(idx);
        }
        protected boolean processKeyBinding(
        	      final KeyStroke ks,
        	      final KeyEvent keyEvent,
        	      final int condition,
        	      final boolean pressed) {
            final boolean b = super.processKeyBinding(ks, keyEvent, condition, pressed);
            if (condition == WHEN_FOCUSED && pressed ) { // the important event
                    final char c = keyEvent.getKeyChar();
                    if (c >= ' ' && c <= '~' && !keyEvent.isAltDown()&& !keyEvent.isControlDown()) {
                    	selectWithKeyChar(c);
                    }
                    
            }
            return b;
        }

        public void setSelectedItem(final Object anObject) { // breakpoint for debugging only
            super.setSelectedItem(anObject);
        }
        
        public void updateUI(){
        	super.updateUI();
        	setKeySelectionManager(ksm);
        }

        Item nullItem = new Item("null");
    }
    
    private JComboBox lastComboBoxGotten;
    protected JComboBox getLastComboBox() {
    	return lastComboBoxGotten;
    }
    
    private boolean closedByEscapeKey=false;
    public boolean closedByEscapeKey(){
    	return closedByEscapeKey;
    }
    
    private JComboBox getComboBox(final int maxWidth, final JComponent heightReference, final boolean parseUnderscore) {
        final JComboBox comboBox = new ComboBox(){
            public Dimension getPreferredSize(){
                final Dimension d=super.getPreferredSize();
                if (maxWidth>0){
                    d.width=maxWidth;
                }
                if (heightReference != null){
                	d.height=heightReference.getHeight();
                }
                return d;
            }

        };
        this.lastComboBoxGotten=comboBox;
        if (!SwingBasics.isMac){
        	resizedUI = new ResizedComboBoxUI(comboBox);
        }
        ksm = new KeySelectionManager(1000, comboBox);
        
        comboBox.setKeySelectionManager(ksm);
        comboBox.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
            	final int kc=e.getKeyCode();
                if (kc == KeyEvent.VK_DOWN || kc==KeyEvent.VK_UP || kc==KeyEvent.VK_PAGE_DOWN || kc==KeyEvent.VK_PAGE_UP) {                	
                    if (!comboBox.isPopupVisible()) {
                        comboBox.showPopup();
                    }
                } else if (kc == KeyEvent.VK_ESCAPE) {
                	closedByEscapeKey=true;
                    comboBox.setSelectedItem(null);
                    closedByEscapeKey=false;
                    
                	if (comboBox.isPopupVisible()) {
                        comboBox.hidePopup();
                    }
                }

            }
        });
        comboBox.setRenderer(new Renderer(comboBox, parseUnderscore));
        return comboBox;
    }

    public final JComboBox getComboBox(
      final RefreshListener refreshListener) {
        return getComboBox(refreshListener,-1, null, false);
    }
    private boolean editable=false;

    public final JComboBox getComboBox(
      final RefreshListener refreshListener, int maxWidth, final JComponent heightReference, final boolean editable) {
        final JComboBox comboBox = getComboBox(maxWidth, heightReference, false);
        comboBox.setEditable(editable);
        this.editable=editable;
        class Listener extends MyActionListener {
            final JComboBox comboBox;
            Listener(final JComboBox comboBox) {
                super(comboBox);
                this.comboBox = comboBox;
                comboBox.addActionListener(this);
                update();
            }

            public void actionPerformed(final ActionEvent e) {
                super.actionPerformed(e);
                final Object o=comboBox.getSelectedItem();
                if (currentItem != null) {
                    currentItem.executeCmd(o, comboBox);
                } else if (comboBox.isEditable()){
                	view(o);
                }
                update();
            }

            void update() {
                if (!resizedUI.ignoreAction) {
                    SelectableCell.this.refresh(comboBox, refreshListener);
                }
            }

        };
        new Listener(comboBox);
        if (!SwingBasics.isNativeMacLookAndFeel()) { // This is commented to fix BugID:203
            comboBox.addFocusListener(new FocusAdapter() {
                public void focusLost(final FocusEvent fe) {
                    final Object value = comboBox.getSelectedItem();
                    //System.out.println("focus lost while selected=" + value);
                    if (value instanceof SelectableCell.Item) {
                        if (!((SelectableCell.Item) value).isExecutingCmd) {
                            ((SelectableCell.Item) value).executeCmd();
                            //System.out.println("executing cmd=" + value);
                        }
                    }
                }
            });
        }
        return comboBox;
    }

    private ResizedComboBoxUI resizedUI = null;
    public void refresh(final JComboBox comboBox,
                        final RefreshListener refreshListener) {
        if (!resizedUI.ignoreAction) {
            resizedUI.ignoreAction = true;
            comboBox.removeAllItems();
            final Vector<Item> c = getOptions();
            if (c.size() == 1 && comboBox.isPopupVisible()) {
                comboBox.setToolTipText(nothingToEdit);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ToolTipOnDemand.getSingleton().show(comboBox, false, 0, -40, false);
                    }
                });
            }
            for (final Item item : c) {
                comboBox.addItem(item);
            }
            if (c.size()>0&&refreshListener != null) {
                refreshListener.actionPerformed(comboBox);
            }
            resizedUI.ignoreAction = false;
        }
    }

    private boolean suppressViewCmd = false;
    public void suppressViewCmd(final boolean ok) {
        suppressViewCmd = ok;
    }

	public boolean setToViewItem(final JComboBox cb, final Object selected) {
		for (final Item item : getOptions()) {
			if (CMD_VIEW.equals(item.cmd)) {
				final String s = item.getBrowserString();
				if (selected.equals(s)) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							cb.setSelectedItem(item);
						}
					});
					return true;
				}
			}
		}
		return false;
	}
	protected String getSuppressedViewHtmlAfterUrl(){
		return "";
	}
	
	protected boolean useIconPrefixes(){
		return false;
	}
	protected URL getIconURLForViewing(Object value){
		return MmsIcons.getURL("edit.gif");
	}
	
	public Object getSelectedItem(final JComboBox cb){
		final Item item=(Item)cb.getSelectedItem();
		return item==null?null:item.value;
	}
}

