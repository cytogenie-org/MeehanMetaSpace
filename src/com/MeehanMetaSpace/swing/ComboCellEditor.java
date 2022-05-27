package com.MeehanMetaSpace.swing;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;

import com.MeehanMetaSpace.*;

public class ComboCellEditor
	 extends AbstractCellEditor
  implements TableCellEditor{
	private boolean hasFiredStopCellEditing=false;
	protected boolean shouldStopEditing(final JComboBox jcb) {
		return !hasFiredStopCellEditing;
	}
	
    public boolean stopCellEditing() { 
    	hasFiredStopCellEditing=true;
    	//new Exception("test").printStackTrace();
    	return super.stopCellEditing();
    }

    public JComboBox getComboBox(){
    	return comboBox;
    }
    public boolean isCellEditable(final EventObject evt) {
        if (evt instanceof MouseEvent) {
        	if (((MouseEvent) evt).getClickCount() >= 2 || considerSingleClick) {
        		comboBox.setVisible(true);
                return true;
        	}
            return false;
        }
        comboBox.setVisible(true);
        return true;
    }

    public Component getTableCellEditorComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final int row,
      final int column) {
    	table.setSurrendersFocusOnKeystroke(true);
    	hasFiredStopCellEditing=false;
        if (!(value instanceof SelectableCell)) {
        	initializing=true;
        	if ( (table instanceof PersonalizableTable) && 
        			((PersonalizableTable)table).model.allowedValueMap != null &&
        			((PersonalizableTable)table).model.allowedValueMap.containsKey(value)){
        		comboBox.setSelectedIndex(((PersonalizableTable)table).model.allowedValueMap.get(value));	
        			} else {
        				comboBox.setSelectedItem(value);
        			}
            initializing=false;
        }
        return comboBox;
    }

    public interface SelectionManager {
    	String getSelectedText(Object o);
    	void setExistingText(String existingText);
    }
    
    SelectionManager selectionManager;
    
    public void setSelectionManger(SelectionManager selectionManager) {
    	this.selectionManager = selectionManager;
    }
    
    public Object getCellEditorValue() {

        final Object o = comboBox.getSelectedItem();
        if (o == null || o.equals(newItem)) {
            if (comboBox instanceof SelectableCell.ComboBox) {
                return ((SelectableCell.ComboBox) comboBox).nullItem;
            }
            return "";
        }        
        
        if (selectionManager != null) {
        	return selectionManager.getSelectedText(o);
        }
        return o;
    }

    protected final JComboBox comboBox;
    
    private boolean initializing=false;
    public static boolean considerSingleClick=false;

    ComboCellEditor(final JComboBox comboBox) {
        this.comboBox = comboBox;	
        this.comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
            	if (!initializing) {
                final int modifiers = e.getModifiers();
                
                // do not stop editing if searching with keyboard!!!
                if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
                    final Object o = comboBox.getSelectedItem();
                    if (o != newItem && shouldStopEditing(comboBox)) {
                        ComboCellEditor.this.stopCellEditing();
                    }
                }
            	}
            }
        });
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {}

            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
            	final JComboBox j=(JComboBox)e.getSource();
            	final int gsi=j.getSelectedIndex();
            	final boolean ste=shouldStopEditing(j);
            	//System.out.println("gsi="+gsi+", ste="+ste);
            	if (gsi >=0 && ste) {
            		SwingUtilities.invokeLater(new Runnable() {
						
						public void run() {
		                	stopCellEditing();							
						}
					});
                }
            }

            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }


    public static ComboCellEditor New(
      final Collection c,
      final boolean edit,
      final boolean allowNew,
      final int delayKeyEntryByMilliSecs) {
        return new ComboCellEditor(
          newComboBox(c, edit, allowNew, delayKeyEntryByMilliSecs, null, true, -1));
    }

    // This key selection manager will handle selections based on multiple keys.
    static class Manager implements JComboBox.KeySelectionManager {
        private long lastKeyTime = 0;
        private String pattern = "";
        private void updateUI() {
            AutoComplete._updateUI(jcb);
        }

        public int selectionForKey(final char aKey, final ComboBoxModel model) {
            final int idx = _selectionForKey(aKey, model);
            if (idx >= 0) {
                if (!jcb.isPopupVisible()) {
                    jcb.showPopup();
                } else {
                    updateUI();
                }
            }
            return idx;
        }
        private String toString(final Object o){
        	if (o instanceof AbstractButton){
        		return ( (AbstractButton)o).getText().toLowerCase();
        	}
        	return o==null?"":o.toString().toLowerCase();
        }
        private int _selectionForKey(final char aKey, final ComboBoxModel model) {
            // Find index of selected item
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
            if (delayMilliSecs >= 1) {

                // Get the current time
                final long curTime = System.currentTimeMillis();

                // If last key was typed less than 300 ms ago, append to current pattern
                if (curTime - lastKeyTime < delayMilliSecs) {
                    pattern += ("" + aKey).toLowerCase();
                } else {
                    pattern = ("" + aKey).toLowerCase();
                }

                // Save current time
                lastKeyTime = curTime;
            } else { // no delay .. behaves more like a READ-ONLY auto complete
                pattern += ("" + aKey).toLowerCase();
            }

            // Search forward from current selection
            for (int i = selIx; i < 	model.getSize(); i++) {
                final String s = toString(model.getElementAt(i));
                if (s.startsWith(pattern)) {
                    //jcb.setActionCommand("keyboardFind");
                    return i;
                }
            }

            // Search from top to current selection
            for (int i = 0; i < selIx; i++) {
                if (model.getElementAt(i) != null) {
                    final String s = toString(model.getElementAt(i));
                    if (s.startsWith(pattern)) {
                        //jcb.setActionCommand("keyboardFind");
                        if (fl != null) {
                            fl.completionFound(model.getElementAt(i));
                        }
                        return i;
                    }
                }
            }
            final Object o=model.getElementAt(selIx);
            if (o != null && !toString(o).startsWith(pattern)) {
                if (fl != null) {
                    fl.completionFound(null);
                }

                if (addToList) {
                    while (pattern.startsWith(" ")) {
                        pattern = pattern.substring(1);
                    }
                    return addToList(jcb, pattern);
                } else {
                    System.err.println("? " + pattern);
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    pattern = "";
                }
            }
            return selIx;
        }

        private final int delayMilliSecs;
        private JComboBox jcb;
        private final boolean addToList;
        final AutoComplete.FoundListener fl;

        void firstKey(final char c) {
            final int idx = selectionForKey(c, jcb.getModel());
            if (idx >= 0) {
                jcb.setSelectedIndex(idx);
                if (!jcb.isPopupVisible()) {
                    jcb.showPopup();
                } else {
                    updateUI();
                }

            }
        }

        Manager(final int delayMilliSecs, final JComboBox jcb, final boolean addToList,
                final AutoComplete.FoundListener fl) {
            this.delayMilliSecs = delayMilliSecs;
            this.jcb = jcb;
            this.addToList = addToList;
            this.fl = fl;
        }

    }


    private static int addToList(final JComboBox jcb, final String input) {
        final FocusListener[] fl = jcb.getFocusListeners();
        Component[] c = jcb.getComponents();
        for (int i = 0; i < fl.length; i++) {
            jcb.removeFocusListener(fl[i]);
            for (int j = 0; j < c.length; j++) {
                c[j].removeFocusListener(fl[i]);
            }
        }
        java.awt.Toolkit.getDefaultToolkit().beep();

        final String pattern = PopupBasics.getStringFromUser(jcb, "Enter new item", input,
          25);
        for (int i = 0; i < fl.length; i++) {
            jcb.addFocusListener(fl[i]);
            c = jcb.getComponents();
            for (int j = 0; j < c.length; j++) {
                c[j].addFocusListener(fl[i]);
            }
        }

        if (!Basics.isEmpty(pattern)) {
            jcb.addItem(pattern);
            return jcb.getItemCount() - 1;

        }
        return -1;
    }


    final static String newItem = " <new>";

    public static Object[] getItems(final Collection c, final boolean allowNew) {
        final int n = c.size();
        if (n==1 && c.iterator().next() instanceof Object []){
        	assert !allowNew;
        	return (Object[])c.iterator().next() ;
        }
        final Object[] array = new Object[allowNew? n + 2:n];
        if (allowNew) {
            array[n] = "";
            array[n + 1] = newItem;
            int i = 0;
            for (final Iterator it = c.iterator(); it.hasNext(); ) {
                array[i++] = it.next();
            }
        } else {
            c.toArray(array);
        }
        return array;
    }

    public static JComboBox newComboBox(
      final Collection c,
      final boolean edit,
      final boolean allowNew,
      final int delayKeyEntryByMilliSecs) {
        return newComboBox(c, edit, allowNew, delayKeyEntryByMilliSecs, null, false, -1);
    }


    public static JComboBox newReadOnlyComboBox(
      final Collection c,
      final AutoComplete.FoundListener fl) {
        return newComboBox(c, false, false, 500, fl, false, -1);
    }

    public static JComboBox newComboBox(
      final Collection c,
      final boolean edit,
      final boolean allowNew,
      final int delayKeyEntryByMilliSecs,
      final AutoComplete.FoundListener fl,
      final boolean isCellEditor,
      final int maxHeight) {
        return newComboBox(c, edit, allowNew, delayKeyEntryByMilliSecs, fl, isCellEditor,
                           -1, maxHeight);
    }

    static class ResettableComboBox extends JComboBox{
    	
    	ResettableComboBox(final Object items[]){
    		super(items);
    	}
    	Object originalItem=null;
    	boolean setYet=false, escapePressed=false;
    	public void setSelectedItem(final Object item){
    		super.setSelectedItem(item);
    		if (!setYet){
    			originalItem=item;
    		}    		
    	}
        protected boolean processKeyBinding(
                final KeyStroke ks,
                final KeyEvent keyEvent,
                final int condition,
                final boolean pressed) {
            final boolean b = super.processKeyBinding(ks, keyEvent, condition,
                    pressed);
            if (ks.getKeyCode()==KeyEvent.VK_ESCAPE){
            	escapePressed=true;
            }else{
            	escapePressed=false;
            }
            return b;
        }
        
        public Object getSelectedItem(){
        	if (escapePressed && originalItem!=null){
        		return originalItem;
        	}
        	return super.getSelectedItem();
        }
    }
    
    public static JComboBox newComboBox(
      final Collection c,
      final boolean edit,
      final boolean allowNew,
      final int delayKeyEntryByMilliSecs,
      final AutoComplete.FoundListener fl,
      final boolean isCellEditor,
      final int maxWidth,
      final int maxHeight) {
        final JComboBox comboBox;
        if (edit) {
            comboBox = new ResettableComboBox(getItems(c, allowNew)) {
                public Dimension getPreferredSize() {
                    final Dimension d = super.getPreferredSize();
                    if (maxWidth > 0 && maxWidth < d.width) {
                        return new Dimension(maxWidth, d.height);
                    }
                    return d;
                }

            };
            if (!SwingBasics.isMac){
            	new ResizedComboBoxUI(comboBox);	
            }
           
            comboBox.setEditable(true);
        } else {
            final Manager ksm = new Manager(delayKeyEntryByMilliSecs, null, allowNew, fl);

            if (isCellEditor) {
                comboBox = new ResettableComboBox(getItems(c, allowNew)) {
                    public Dimension getPreferredSize() {
                        final Dimension d = super.getPreferredSize();
                        if (maxWidth > 0 && maxWidth < d.width) {
                            return new Dimension(maxWidth, d.height);
                        }
                        return d;
                    }

                    protected boolean processKeyBinding(
                      final KeyStroke ks,
                      final KeyEvent keyEvent,
                      final int condition,
                      final boolean pressed) {
                        final boolean b = super.processKeyBinding(ks, keyEvent, condition,
                          pressed);

                        if (condition == WHEN_FOCUSED && pressed &&
                            !Basics.isMac()) { // the important event
                            final char c = keyEvent.getKeyChar();
                            if (c >= ' ' && c <= '~' && !keyEvent.isAltDown() &&
                                !keyEvent.isControlDown()) {
                                ksm.firstKey(c);
                            }

                        }
                        return b;
                    }

                };
            } else {
                comboBox = new ResettableComboBox(getItems(c, allowNew)) {
                    public Dimension getPreferredSize() {
                        final Dimension d = super.getPreferredSize();
                        if (maxHeight>0){
                        	d.height=maxHeight;
                        }
                        if (maxWidth > 0 && maxWidth < d.width) {
                            return new Dimension(maxWidth, d.height);
                        }
                        return d;
                    }

                };
            }
            ksm.jcb = comboBox;
            if (!SwingBasics.isMac){
            	new ResizedComboBoxUI(comboBox);
            }
            
            /*
            System.out.print("COMBO BOX EDITOR:  "+comboBox.getEditor().getClass().getName());
            System.out.print(", is native Mac L&F?");
            System.out.println(SwingBasics.isNativeMacLookAndFeel());
            */
            comboBox.setKeySelectionManager(ksm);
            comboBox.setRenderer(new AutoComplete.Renderer(
              new AutoComplete.StartingTextProvider() {
                public String getStartingText() {
                    return ksm.pattern;
                }
            }
              , null));
            if (fl != null) {
                comboBox.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final Object item = comboBox.getSelectedItem();
                        if (item != null) {
                            fl.completionFound(item);
                        }
                    }
                });

            }
            comboBox.addKeyListener(new KeyAdapter(){
                public void keyPressed(final KeyEvent keyEvent) {
                    final char c = keyEvent.getKeyChar();
                    if (c >= ' ' && c <= '~' && !keyEvent.isAltDown() &&
                        !keyEvent.isControlDown()) {
                    } else if (!keyEvent.isShiftDown()){
                        ksm.pattern="";
                        ksm.updateUI();
                    }
                    final int kc=keyEvent.getKeyCode();
                    if (kc == KeyEvent.VK_DOWN || kc==KeyEvent.VK_UP || kc==KeyEvent.VK_PAGE_DOWN || kc==KeyEvent.VK_PAGE_UP) {
                        if (!comboBox.isPopupVisible()) {
                            comboBox.showPopup();
                        }
                    }else if (kc == KeyEvent.VK_ESCAPE) {
                        if (comboBox.isPopupVisible()) {
                            comboBox.hidePopup();
                        }
                    }


                }
            });
        }
        final int newItemIndex;
        if (allowNew) {
            comboBox.setToolTipText(Basics.toHtmlUncentered("Press space to create new entry"));
            newItemIndex = c.size() + 1;
        } else {
            newItemIndex = -1;
        }
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (allowNew && comboBox.getSelectedIndex() == newItemIndex) {
                    final int idx = addToList(comboBox, "");
                    if (idx >= 0) {
                        comboBox.setSelectedIndex(idx);
                    }
                }
            }
        });
        //}
        return comboBox;
    }

    public static void addFocusListener(final JComboBox comboBox, final FocusListener fl) {
        comboBox.addFocusListener(fl);
        final Component[] c = comboBox.getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].addFocusListener(fl);
        }

    }
}
