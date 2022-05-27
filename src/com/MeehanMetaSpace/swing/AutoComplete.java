

package com.MeehanMetaSpace.swing;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.DefaultStringConverters;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.Pel;
import com.MeehanMetaSpace.StringConverter;
import com.MeehanMetaSpace.TernarySearchTree;



/**
 *         Enables efficient searching through a JComboBox.
 *         Based on code by Ron (rmlchan@yahoo.com)
 *
 *         @author Eric Lindauer (original),
 *          Stephen Meehan (fixed behavior when editing new item, added CellEditor)
 *         @date 2002.9.24
 */

public class AutoComplete extends JComboBox {
	public boolean resetMousePointer=false; // should mouse point to the edit box?
    public interface ItemFactory {
        Object[] getAutoCompleteItems();
        Map<String, java.util.List<String>> getSynonymMap();
    }


    private boolean arrowKeysSelect = !SwingBasics.isMacLookAndFeel();
    
    public interface FoundListener {
        void completionFound(final Object nextSelection);
    }


    private FoundListener fl;
    public void setFoundListener(final FoundListener fl) {
        this.fl = fl;
    }

    JTable specialFocusTable = null; // if different than table being edited (e.g. RotateTable)

    public Component getNextFocusableComponent() {
        if (specialFocusTable != null) {
            specialFocusTable.requestFocus();
        } else if (tableEdited != null) {
            tableEdited.requestFocus();
        }
        return super.getNextFocusableComponent();
    }

    public static boolean wasEscapeLastKeyPressed=false, userExplicitlySelected=false;
   
    private JTable tableEdited=null;
    private JRootPane tableRoot=null;
    private JButton dfltButton=null;
    private boolean tableEditingStarted=false;
    void collapseItems() {
    	itemCurrentlyHighlighted=Basics.stripSimpleHtml(itemCurrentlyHighlighted).trim();        			
		if (!(itemsShowingSubList.size()==0)&& itemsShowingSubList.contains(itemCurrentlyHighlighted)){
				subListStartIndex = indexCurrentlyHighlighted+1;
				itemCurrentlySubListed = itemCurrentlyHighlighted;
				subItems=subListManager.getSubItems(itemCurrentlyHighlighted);
				clearSubList(false);    	    			
		}
    }
    
    void expandItems() {
    	if (itemsShowingSubList.size()==0){
			itemsShowingSubList.add(itemCurrentlyHighlighted);
			showSubList(false);
		} else if(!itemsShowingSubList.contains(itemCurrentlyHighlighted)){
				showSubList(false);
				itemsShowingSubList.add(itemCurrentlyHighlighted);
		}
    }

    private KeyEvent lastKeyEvent;

    protected boolean processKeyBinding(
      final KeyStroke ks,
      final KeyEvent keyEvent,
      final int condition,
      final boolean pressed) {
    	lastKeyEvent=keyEvent;
    	ToolTipOnDemand.hideManagerWindow();
        if (ks.getKeyCode()==KeyEvent.VK_ESCAPE){
        	wasEscapeLastKeyPressed=true;
        } else {
        	wasEscapeLastKeyPressed=false;
        }
    	if (subListManager != null && !Basics.isEmpty(itemCurrentlyHighlighted)) {
    		int selectedIndex=getSelectedIndex();
        	final int keyCode = ks.getKeyCode();
    		switch(keyCode) {
        		case KeyEvent.VK_RIGHT:
        			expandItems();
        			break;
        		case KeyEvent.VK_LEFT:
        			collapseItems();
	        		break;
        		case KeyEvent.VK_UP:
        		case KeyEvent.VK_DOWN:
        			if (isSubListShowing &&(selectedIndex<subListStartIndex-1||(subItems!=null&&selectedIndex>subListStartIndex+subItems.size()-1))) 
        				clearSubList(false);
        			break;        	
    		}
    	}
    	if (subListManager != null && ks.getKeyCode() == KeyEvent.VK_LEFT) {
    		if (isSubListShowing)
				clearSubList(false);
    	}
        if (tableEdited != null) { // if table editor transfer ALL editable keys to text field      
        	if (!pressed){
        		tableEditingStarted=true;
        	}
    		if (ks.getKeyCode()==27){
    			final Window wnd=SwingUtilities.getWindowAncestor(this);
    			if (wnd instanceof RootPaneContainer){
    				tableRoot=((RootPaneContainer)wnd).getRootPane();
    				tableRoot.setDefaultButton(dfltButton);
    			}			
    		} else {
    			final char c = keyEvent.getKeyChar();
    			final Component cmp=editor.getEditorComponent();
    			System.out.println("TRANSFERRing "+c);
    			if (c >= ' ' && c <= '~' && !keyEvent.isAltDown()&& !keyEvent.isControlDown() && !Basics.isPowerPC) {
    				return ( 
    						(com.MeehanMetaSpace.swing.AutoComplete.SearchEditor.BorderlessTextField)cmp).processKeyBinding(ks,keyEvent,condition,pressed);
    			}
    		}
        }
        final boolean b = super	.processKeyBinding(ks, keyEvent, condition, pressed);
        return b;
    }

    public void selectAll() {
        ((javax.swing.text.JTextComponent) editor.getEditorComponent()).
          selectAll();    
    }

    public void addEditorFocusListener(final FocusListener fl) {
        //super.addFocusListener(fl);
        ((javax.swing.text.JTextComponent) editor.getEditorComponent()).
          addFocusListener(fl);
    }

    private final StringConverter sc;
    private boolean allowNew;
    private final boolean  autoSelect;
    
    public interface SubListManager {
    	String getSubItemHeader();
    	void setCurrentItem(String item);
		String getDisplayValue();
    	Set<String> getSubItems(String item);
    	boolean subItemExists(String item);
      	ImageIcon getStatusIcon(String item);
      	String getNormalClickToolTip(boolean expanded);      	
    }
    
    public void setSubListManager(final SubListManager subListManager) {
    	this.subListManager = subListManager;
    	renderer= new SubListRenderer(
      	      new StartingTextProvider(){
    	          public String getStartingText(){
    	              return ((SearchEditor) getEditor()).literallyTypedText;
    	          }
    	        }, this);
    	if (!unselectableItems.isEmpty()) {
    		((SubListRenderer)renderer).unselectableItems=unselectableItems;
        	for (int i=0; i<getItemCount(); i++) {
        		((SubListRenderer)renderer).unselectableItems.add(getItemAt(i)+subListManager.getDisplayValue());
            }	
    	}
    	
        setRenderer( renderer);
        setupComboPopupMouseHandler();
    }

    private SubListManager subListManager; 
    /*private FavoriteItemManager favoriteItemManager; */
    private Set<String> subItems;
    private int subListStartIndex = -1, subListSize=-1, previousMouseX = -1, previousMouseY = -1, mouseX = 0, mouseY = 0;
    private String itemCurrentlyHighlighted="", itemCurrentlySubListed = "";
    private boolean expanded=false;
    private int indexCurrentlyHighlighted = -1;
    private final String SUBLISTTEXT_PREFIX = "<html>&nbsp;&nbsp;<i>";
    private final String SUBSUBLISTTEXT_PREFIX = "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>";
    private final String EXTRA_LEADING_SPACES = "&nbsp;&nbsp;&nbsp;";
    private final String SUBLISTTEXT_SUFFIX = "</i></html>";
    private boolean isSubListShowing = false;
    private List<String> itemsShowingSubList = new ArrayList<String>();
    private void clearSubList(boolean isMouseEvent) {
    	if (subListStartIndex != -1) {
    		if (subItems != null){
    			int subListSize=-1;
    			if(subItems.contains(itemCurrentlySubListed)){
    				subListSize=subItems.size()-1;
    			}
    			else{
    				subListSize=subItems.size();
    			}
    			for (int i = 0; i < subListSize; i++) {
    				String subItem=Basics.stripSimpleHtml((String)getItemAt(subListStartIndex)).trim();
    				if (subItem.contains(subListManager.getDisplayValue()) && itemsShowingSubList.contains(subItem)) {
    					subListStartIndex=subListStartIndex+1;
	    				itemCurrentlySubListed=subItem;
	    				subItems=subListManager.getSubItems(subItem);
	    				itemsShowingSubList.remove(subItem);
	    				clearSubList(isMouseEvent);
    				}
    				subListStartIndex=indexCurrentlyHighlighted+1;
    				itemCurrentlySubListed=itemCurrentlyHighlighted;
    				subItems=subListManager.getSubItems(itemCurrentlyHighlighted);
    				removeItemAt(subListStartIndex);	        				
    			}
    		}
    		_updateUI(this);
    		if ((indexCurrentlyHighlighted >= subListStartIndex)) {
    			if (isMouseEvent) {
    				try {
            			previousMouseY+=10;
    					new Robot().mouseMove(previousMouseX, previousMouseY);
    					_updateUI(this);
    				}catch(Exception e) {
    					e.printStackTrace();
    				}	
    			}
    			else {
    				setSelectedIndex(subListStartIndex-1);
    				setSelectedItem(itemCurrentlySubListed);
    				_updateUI(this);
    			}
			}    		
    		subListStartIndex = -1;
    		subListSize=0;
    		itemsShowingSubList.remove(itemCurrentlySubListed); 
    		itemCurrentlySubListed="";    		   		
    		isSubListShowing = false;
    	}
    }

    private void showSubList(boolean isMouseEvent) {
		previousMouseX = mouseX;
		previousMouseY = mouseY;	
    	subItems = subListManager.getSubItems(itemCurrentlyHighlighted);
    	if (!Basics.isEmpty(subItems)) {
    		List<String> subItemlist= new ArrayList<String>(subItems);
        	Collections.sort(subItemlist);
    		final Iterator<String> it = subItemlist.iterator();
    		while (it.hasNext()) {
    			final String item = it.next();
    			if (Basics.isEmpty(item) || item.trim().equalsIgnoreCase("null") || 
    					item.trim().equalsIgnoreCase(itemCurrentlyHighlighted)) {
        	 		it.remove();
        	 	} 
    		}
			subListStartIndex = indexCurrentlyHighlighted+1;
			subListSize=subItemlist.size();
			for (int i = subListStartIndex, j =0; j < subListSize; i++, j++) {
				String prefix=itemCurrentlyHighlighted.equals(subListManager.getSubItemHeader())?SUBSUBLISTTEXT_PREFIX:SUBLISTTEXT_PREFIX;
				insertItemAt(prefix+EXTRA_LEADING_SPACES+ subItemlist.get(j) + SUBLISTTEXT_SUFFIX, i);
			}
			itemCurrentlySubListed = itemCurrentlyHighlighted;
			isSubListShowing = true;
			if (!mouseClickGesturedSubListExpansion)
				setSelectedIndex(indexCurrentlyHighlighted+1);
		}
    }
    private javax.swing.Timer subListTimer=null;
    private static int HOVER_DELAY_FOR_EXPANDING_SUBLIST=3000;
    int popupFirstVisibleIndex = 0;
    int popupLastVisibleIndex = 0;
    private void setupComboPopupMouseHandler() {
        if (!expandNormallyPlease()){

        	subListTimer=new javax.swing.Timer(HOVER_DELAY_FOR_EXPANDING_SUBLIST, 
			  new ActionListener() {
				public void actionPerformed(ActionEvent e) {
    	        	subListTimer.stop();
    	        	if (!isSubListShowing) {
    	        		showSubList(true);	    	        		
    	        	}
				}
			});
  	  		subListTimer.setRepeats(false);
        }
    	try {
    		final Field popupInBasicComboBoxUI = BasicComboBoxUI.class.getDeclaredField("popup");  
            popupInBasicComboBoxUI.setAccessible(true);  
            final BasicComboPopup popup = (BasicComboPopup) popupInBasicComboBoxUI.get(getUI());  

            final Field scrollerInBasicComboPopup = BasicComboPopup.class.getDeclaredField("scroller");  
            scrollerInBasicComboPopup.setAccessible(true);  
            
            if (!expandNormallyPlease()){
            	final JScrollPane scroller = (JScrollPane) scrollerInBasicComboPopup.get(popup);
            	scroller.getViewport().getView().addMouseMotionListener(new MouseMotionAdapter(){
            	public void mouseMoved(MouseEvent mouseEvent) {
        			mouseX = mouseEvent.getXOnScreen();
                    mouseY = mouseEvent.getYOnScreen();
                    if (subListManager != null && !Basics.isEmpty(itemCurrentlyHighlighted)) {
                    	int selectedIndex=getSelectedIndex();
                    	if (!isSubListShowing) {
                    		if (mouseEvent.isControlDown() ){
                    			showSubList(true);	
                    		} else {
                    			if (subListTimer.isRunning()){
                    				subListTimer.stop();
                    			}
                    			subListTimer.setInitialDelay(HOVER_DELAY_FOR_EXPANDING_SUBLIST);
                    			subListTimer.start();
                    		}
	            		} 
                    	if (isSubListShowing &&(selectedIndex<subListStartIndex-1||(subItems!=null&&selectedIndex>subListStartIndex+subItems.size()-1))) 
	        				clearSubList(true);
                    }
                    
                }
            });
            }
            addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent e1) {
            		BasicComboPopup popup = null;
					try {
						popup = (BasicComboPopup) popupInBasicComboBoxUI.get(getUI());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
            		 JList list = popup.getList(); 
            		Rectangle rect = list.getVisibleRect();                
            		popupFirstVisibleIndex = list.locationToIndex(rect.getLocation());                 
            		popupLastVisibleIndex = popupFirstVisibleIndex + getMaximumRowCount() - 1;
            	}
            });
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public AutoComplete() {
        this(new Object[0]);
    }
    
    private Map<String, java.util.List<String>> itemsAndSubItems;
    public AutoComplete(final Map<String, java.util.List<String>> itemsAndSubItems, final String entity) {
    	this(itemsAndSubItems.keySet().toArray(new String[0]));
    	this.itemsAndSubItems=itemsAndSubItems;
		resetMousePointer=true;
		setMaximumRowCount(12);
		setSubListManager(new AutoComplete.SubListManager() {
			String highlighted ="";
			public String getSubItemHeader() {
				return highlighted+entity;
			}
			public void setCurrentItem(String item) {
				highlighted=item;
			}
			public String getDisplayValue() {
				return entity;
			}
			public Set<String> getSubItems(final String item) {
				if (item.equals(getSubItemHeader())) {
					final java.util.List<String> o= AutoComplete.this.itemsAndSubItems.get(highlighted);
					Set<String> ss= new HashSet<String>();
					for(final String oo: o) {
						ss.add(oo);
					}
					return ss;
				} else if (!Basics.isEmpty(AutoComplete.this.itemsAndSubItems.get(item))){
					highlighted=item;
					Set<String> s=new TreeSet<String>();
					s.add(getSubItemHeader());
					return s;
				}
				highlighted="";
				return null;
			}
			public boolean subItemExists(final String item) {
				return (AutoComplete.this.itemsAndSubItems.get(item) != null && AutoComplete.this.itemsAndSubItems.get(item).size() > 0) || item.equals(getSubItemHeader());
			}
			
			public ImageIcon getStatusIcon(final String item) {
				if (item.contains(getSubItemHeader())) {
					return null;
				}
				return null;
			}
			@Override
			public String getNormalClickToolTip(boolean expanded) {
				// TODO Auto-generated method stub
				if (!expanded){
					return "Click to see " + entity + " (or press right)";
				} else {
					return "Click to hide " + entity + " (or press left)";
				}
			}
			
		});
    }

    public AutoComplete(final Object[] elements) {
        this(elements, DefaultStringConverters.get(), false, true);
    }
    public AutoComplete(
    	      final Object[] items,
    	      final StringConverter sc,
    	      final boolean allowNew,
    	      final boolean autoSelect) {
    	this(items, null, sc, allowNew, autoSelect);
    }
    private static boolean delayNextTime=false;
    public AutoComplete(
      final Object[] items,
      final Collection unselectableItems,
      final StringConverter sc,
      final boolean allowNew,
      final boolean autoSelect) {
    	setMaximumRowCount(12);
    	lastForbiddenCellValue=null;
    	wasEscapeLastKeyPressed=false;
    	this.unselectableItems=unselectableItems==null?Basics.UNMODIFIABLE_EMPTY_LIST:unselectableItems;
        this.sc = sc != null ? sc :DefaultStringConverters.get();
        this.autoSelect = autoSelect;
        this.allowNew = allowNew;
        for (int i = 0; i < items.length; i++) {
        	if (items[i] != null ) {
            super.addItem(items[i]);
        	}
        }
        init();
        clear();
        setSelectedIndex(-1);
        userExplicitlySelected=false;
        super.addPopupMenuListener(new PopupMenuListener() {
        	private String invisibleToolTip;
        	private JComponent cmp2;
        	final ToolTipOnDemand ttod=ToolTipOnDemand.getSingleton();
        	final JCheckBox delay=new JCheckBox("<html><small>Wait 5 secs to show</small></html>");
        	{
        		delay.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				delayNextTime=delay.isSelected();
        			}
        		});

        	}

        	private int subtractHeight=0;
        	private Component cmp;
        	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
        		((SearchEditor) editor).handlePendingItems();
        		invisibleToolTip=getToolTipText();
        		if (callBack != null){
        			subtractHeight=0;
        			String txt2=callBack.getPopupVisibleToolTipText();
        			cmp=callBack.getPopupVisibleComponent(AutoComplete.this);
        			if (cmp != null || !Basics.isEmpty(txt2)){
        				SwingUtilities.invokeLater(new Runnable(){
        					public void run(){
        						delay.setSelected(delayNextTime);
        						cmp2=AutoComplete.this;//showingList;
        						insideTimer=new javax.swing.Timer(delayNextTime?6000:3200, 
        								new ActionListener() {
        							public void actionPerformed(ActionEvent e) {
        								show();
        							}
        						});
        						insideTimer.setRepeats(false);
        						priorKeyCnt=keycnt;
        						insideTimer.start();
        					}
        				});

        			} 
        		}
        		//System.out.println("popupMenuWillBecomeVisible");
        	}
        	private void show(){
        		insideTimer.stop();
        		if (callBack != null && cmp2.isShowing() && isPopupVisible()){
        			if (priorKeyCnt==keycnt){
        				haveNextKeyHideToolTip=true;
        				//ttod.setNorthCentralComponent(delay);
        				int h=callBack.getPopupHeightOffset();
        				final Point p=AutoComplete.this.getLocationOnScreen();
        				if (p.y > lastPopupLocation.y){
        					subtractHeight=p.y-lastPopupLocation.y;
        				}
        				if (subtractHeight>0){
        					h -= subtractHeight;
        				}
        				final JButton prior=ttod.setCancel(null);
        				ttod.show(
        						cmp2, 
        						false, 
        						callBack.getPopupWidthOffset(), 
        						h, 
        						cmp, 
        						callBack.getPopupVisibleToolTipText());
        				ttod.setCancel(prior);
        				AutoComplete.this.requestFocus();
        				//ttod.setNorthCentralComponent(null);
        			} else {
        				insideTimer.setInitialDelay(5000);
        				priorKeyCnt=keycnt;
        				insideTimer.start();
        			}
        		}
        	}
        	javax.swing.Timer insideTimer=null;

        	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        		setToolTipText(invisibleToolTip);
        	}

        	public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        renderer=new Renderer(
        	      new StartingTextProvider(){
        	          public String getStartingText(){
        	              return ((SearchEditor) getEditor()).literallyTypedText;
        	          }
        	        }, this);
        ((Renderer)renderer).unselectableItems=unselectableItems;
        setRenderer( renderer);
        sequentialMatcher=SUPPORT_SEQUENTIAL_NON_JUMPY_MATCHING?new ComboBoxSearcher(this, this.sc, false):null;
    }
    private ListCellRenderer renderer;

    private String toString(final Object item) {
        return sc.toString(item).toLowerCase();
    }

    public void removeItem(final Object item) {
        super.removeItem(item);
        if (!Basics.isEmpty(item) && sequentialMatcher==null) {
            ((SearchEditor) editor)._data.remove(toString(item));
        }
    }

    public void addItem(final Object item) {
        super.addItem(item);
        ((SearchEditor) editor).add(item);
    }


    public void addItems(final Object[] items) {
        for (int i = 0; i < items.length; i++) {
            addItem(items[i]);
        }
    }

    public void addOnNextKeyEvent(final ItemFactory itemFactory) {
        ((SearchEditor) editor).addOnNextKeyEvent(itemFactory);
        
    }


    public void addOnNextKeyEvent(final Object[] items) {
        ((SearchEditor) editor).addOnNextKeyEvent(items);
        if (SwingBasics.isNativeMacLookAndFeel()) {
            System.out.println(
              "SwingBasics.isNativeMacLookAndFeel addOnNextKeyEvent(final Object[] items){!!");
            ((SearchEditor) editor).handlePendingItems();
            ((SearchEditor) editor).clear();
        }
    }


    private Object originalItem = null, priorItem = null;

    public void setPriorItem(final Object o) {
        priorItem = o;
    }

    public Object getPriorItem() {
        return priorItem;
    }
    
    public void setDefaultItem(final Object item) {
    	originalItem = item;
    	if (editor instanceof SearchEditor){
    		final SearchEditor e=(SearchEditor)editor;
			e.returnPressed=false;
    	}
    }

    public void setSelectedItem(final Object item) {
    	boolean isForbidden=isForbiddenFruit(item);
		super.setSelectedItem(item);
		clearProblem();
		if (item == null) {
			final String txt=((JTextField) editor.getEditorComponent()).getText();
			if (unselectableItems.contains(txt)) {
				((JTextField) editor.getEditorComponent()).setText("");
			}			
							
		} 
		if (isForbidden) {
			((JTextField) editor.getEditorComponent()).setText("");
		}
	}	
    
    public void clearText() {
    	((JTextField) editor.getEditorComponent()).setText("");
    }

    private void clearProblem() {
        if (problematic) {
            setBorder(originalBorder);
            setToolTipText(originalToolTip);
            problematic = false;
        }

    }

    private void conveyProblem(final Border b, final String problemToolTip) {
        if (b != null) {
            if (originalBorder == null) {
                originalBorder = getBorder();
            }
            setBorder(b);
            if (originalToolTip == null) {
                originalToolTip = getToolTipText();
            }
            setToolTipText(problemToolTip);
            problematic = true;
        }
    }

    private boolean problematic = false;
    private String originalToolTip = null;
    private Border originalBorder;

    public void clear() {
        ((SearchEditor) editor).clear();
    }

    public String getKeyboardValue() {
        return ((JTextField) editor.getEditorComponent()).getText();
    }

    public boolean hasFoundValue() {
        final String s=getKeyboardValue(), t=sc.toString(_getFinalValue());
        if (s != null && t!=null){
            return t.toLowerCase().startsWith(s.toLowerCase());
        }
        return false;
    }
    /**
     * method to check whether the cell type entered from the keyboard
     * exists in the
     * @return boolean
     */
    public boolean isValidCellType(){
    	final String s=getKeyboardValue(), t=sc.toString(_getFinalValue());
    	if(s!=null && t!=null){
    		return s.equals(t);
    	}
    	return false;

    }
    
    private boolean isCaseSensitive=true;
    public void setIsCaseSensitive(final boolean ok) {
    	isCaseSensitive=ok;
    }

    public static boolean hideToolTipWhenGettingFinalValue=true;
    public Object getFinalValue() {
        Object o=_getFinalValue();
        if (isForbiddenFruit(o)){
    		lastForbiddenCellValue=o;
    		final SearchEditor e=(SearchEditor)editor;
			e.nextSelection=null;
    		System.out.println(Basics.concatObjects("LAST FORBIDDEN  ", o));
    		o="";
    	}
        callBack=null;
        return o;
    }
    private Object _getFinalValue() {
    	if (wasEscapeLastKeyPressed) {
			if (hideToolTipWhenGettingFinalValue)ToolTipOnDemand.getSingleton().hideTipWindow();
        	if (originalItem!=null){
        		return originalItem;
        	}
        	return "";
        }
		try {
			final SearchEditor e=(SearchEditor)editor;
			
			Object item=super.getSelectedItem();
			//System.out.println("Getting FINAL value object=" + item);
			if ( e.returnPressed  ){
				if (e.nextSelection != null) {
					return e.nextSelection;
				}
				if (item==null && SwingBasics.isNativeMacLookAndFeel()){
					if (isForbiddenFruit(e.itemSelectedOnReturn) ){
						System.out.println( Basics.concatObjects("Reversing native MAC  L&F queer nullifying of forbidden selections .. using ", item));
						item=e.itemSelectedOnReturn;
						e.itemSelectedOnReturn=null;
					}
				}
			} 
			
			if (!unselectableItems.contains(item)) {
				String text = ((JTextField) editor.getEditorComponent()).getText();
				if (hideToolTipWhenGettingFinalValue)ToolTipOnDemand.getSingleton().hideTipWindow();
				//System.out.println("Getting FINAL value text=" + text );
				if (unselectableItems.contains(sc.toString(text))) {
					((JTextField) editor.getEditorComponent()).setText("");
				}else if ( item == null || !Basics.equals(sc.toString(item), text) ) {
					if (allowNew) {
						if (sc != null) {
							if (item!=null && !isForbiddenFruit(item) && callBack!=null){
								((JTextField) editor.getEditorComponent()).setText(sc.toString(item));
								text = ((JTextField) editor.getEditorComponent()).getText();								
							}
							if (isForbiddenFruit(item)){
								return item instanceof String ? sc.toObject((String) item) : item;
							}
							if (!isCaseSensitive && text.equalsIgnoreCase(sc.toString(item))) {
								return item;
							}
							return sc.toObject(text);
						}
					} else if (item == null) {
						 Object o2=originalItem instanceof String ? sc.toObject((String) originalItem) : originalItem;
						if (o2==null){
							o2="";
						}
						((JTextField) editor.getEditorComponent()).setText(sc.toString(o2));
						return o2;
					}
					if (!isForbiddenFruit(item)){
					((JTextField) editor.getEditorComponent()).setText(sc.toString(item));
					}
				}
				((SearchEditor) getEditor()).literallyTypedText = "";
				if (hideToolTipWhenGettingFinalValue)ToolTipOnDemand.getSingleton().hideTipWindow();
				((SearchEditor) getEditor()).lastCapitalWarning = null;
				return item instanceof String ? sc.toObject((String) item) : item;
			}
		} catch (Exception e) {
			Pel.log.print(e);
		}
		return null;
	}

    public void setModel(final ComboBoxModel model) {
        super.setModel(model);
        // init();
    }

    int debug() {
        final ComboBoxModel model = getModel();
        for (int i = 0; i < model.getSize(); i++) {
            final Object data = model.getElementAt(i);
            if (!Basics.isEmpty(data)) {
                System.out.println(i + ":  " + data);
            }
        }
        return 0;
    }

    private void init() {
    	new ResizedComboBoxUI(this);
        setEditable(true);
        setEditor(new SearchEditor(this));
    }

    private boolean handlingPendingItems=false;
    public boolean isHandlingPendingItems(){
        return handlingPendingItems;
    }


    public void requestFocus() {
    	final Component c=editor.getEditorComponent();
    	final boolean b=c.isVisible();
        c.requestFocus();
    }


    public JTextField getTextField() {
        return ((SearchEditor) editor).getEditor();
    }

    private static class SearchEditor implements ComboBoxEditor{
    	final JTextField editor;
        private JTextField getEditor() {
            return editor;
        }

        boolean lostSelection = false;
        public void setItem(final Object item) {
    		boolean isForbidden=ac.isForbiddenFruit(item);
    		//System.out.println("setItem item="+item+", isForbidden="+isForbidden);
    		if (!lostSelection) {
                if (item != null && !ac.unselectableItems.contains(item) && !isForbidden) {
                    priorDefaultEditor.setItem(item);                    
                } else {
                	editor.setText("");
                }
            }
        }

        void clear() {
            priorDefaultEditor.setItem(null);
        }

        private TernarySearchTree _data ;
        private Map<String,Object>caseSensitive=null;
        public Object getItem() {
        	final Object item = priorDefaultEditor.getItem();
            String text=ac.sc.toString(item);
            if (text==null && item instanceof String) {
            	text=editor.getText();
            }
            if (text != null) {
            	Object o=get(text);
            	if (o== null && returnPressed){
            		final ComboBoxUI ci=ac.getUI();
            		if (movingArrowAround && ci instanceof ResizedComboBoxUI.ListHighlightShower){
            			final int idx=( (ResizedComboBoxUI.ListHighlightShower)ci).getHighlighted();
            			if (idx >=0 ){
            				o=ac.getItemAt(idx);
            			}
            		} else {
                        o=getIgnoreCase(text);
            		}
            	}
            	return o;
            }
            return null;
        }
        
        Object getIgnoreCase(final String text) {
        	if (ac.sequentialMatcher != null){
        		return ac.sequentialMatcher.find(text, false, false);
        	}
			final String lwr = text.toLowerCase();
			return _data.get(lwr);
		}
        
        
        
        Object get(final String text) {
        	if (ac.sequentialMatcher != null){
        		return ac.sequentialMatcher.find(text, false, ac.isCaseSensitive);
        	}
			if (caseSensitive == null) {
				final String lwr = text.toLowerCase();
				return _data.get(lwr);
			} 
			return caseSensitive.get(text);			
		}
        
        String matchPrefix(final String text){
        	if (ac.sequentialMatcher != null){
        		final Object o=ac.sequentialMatcher.find(text, true, ac.isCaseSensitive);
        		return o==null?"":ac.sc.toString(o);
        	}
        	if (caseSensitive == null) {
				return _data.matchPrefixString(text,1);
				
			} 
			for (final String key:caseSensitive.keySet()){
				if (key.startsWith(text)){
					return key;
				}
			}
			return "";
        }

        String matchPrefixIgnoreCase(final String text){
        	if (ac.sequentialMatcher != null){
        		final Object o=ac.sequentialMatcher.find(text, true, false);
        		String value=o==null?"":ac.sc.toString(o);
        		if (value.startsWith("<html>")){
        			value=Basics.stripSimpleHtml(value).trim();
        		}
        		return value;
        	}
			return _data.matchPrefixString(text,1);
		}

        final AutoComplete ac;

        private String literallyTypedText; // case insensitive
        private void determineLiterallyTypedText(final char currentChar, final String startText, final int caretPosition, final boolean textFieldHasProcessedFutureKeyEvents) {
            if (textFieldHasProcessedFutureKeyEvents) { // yes user is typing fast
            	System.out.print(" typing fast ");
                literallyTypedText=startText;
            } else {
                final StringBuilder sb = new StringBuilder();
                if (literallyTypedText == null && currentChar != '\0') {
                    if (caretPosition > 1) {
                        sb.append(startText.substring(0, caretPosition - 1));
                    }

                    sb.append(currentChar);
                    if (caretPosition < startText.length()) {
                        sb.append(startText.substring(caretPosition));

                    }
                } else if (currentChar != '\0') { // merge startText with literallyTyped and c
                    final char[] l = literallyTypedText.toCharArray(),
                                     s = startText.toCharArray();
                    for (int i = 0; i < s.length; i++) {
                        if (i == caretPosition - 1 && currentChar != '\0') {
                            sb.append(currentChar);
                        } else if (i >= l.length) {
                            sb.append(s[i]);
                        } else {
                            if (l[i] != s[i]) {
                                if (Character.toLowerCase(l[i]) ==
                                    Character.toLowerCase(s[i])) {
                                    sb.append(l[i]);
                                } else {
                                    sb.append(s[i]);
                                }
                            } else {
                                sb.append(s[i]);
                            }
                        }
                    }
                }
                literallyTypedText = sb.toString();
            }
            System.out.println(Basics.concat("literal done=\"",  literallyTypedText, "\""));
        }


        private ItemFactory itemFactory;
        private void addOnNextKeyEvent(final ItemFactory itemFactory) {
            this.itemFactory = itemFactory;
        }

        private Object[] toAdd;
        private void addOnNextKeyEvent(final Object[] items) {
            toAdd = items;
        }

        private boolean handlePendingItems() {
            boolean done=false;
            ac.handlingPendingItems=true;
            if (itemFactory != null) {
                done=true;
                ac.addItems(itemFactory.getAutoCompleteItems());
                ac.itemsAndSubItems=itemFactory.getSynonymMap();
                itemFactory = null;
            }
            if (toAdd != null) {
                done=true;
                ac.addItems(toAdd);
                toAdd = null;
            }
            ac.handlingPendingItems=false;
            return done;
        }

        private class BorderlessTextField extends JTextField {
            public BorderlessTextField(String value, int n) {
                super(value, n);
            }

            // workaround for 4530952
            public void setText(String s) {
            	Object o=null;
            	try {
					o=ac.sc.toObject(s);
				} catch (Exception e) {
					e.printStackTrace();					
				}
				if (ac.isForbiddenFruit(o) || ac.unselectableItems.contains(o)){
					return;
				}
                if (getText().equals(s)) {
                    return;
                }
                if (ac.subListManager != null) {
                	if (s.contains(ac.subListManager.getDisplayValue())) {
                		s=ac.subListManager.getDisplayValue();
                	}
                }
                if (callBack != null || _callBack !=null){
                	if (_callBack==null){
                		_callBack=callBack;
                	}
                	s=_callBack.getText(s);                	
                } else  if (s != null && s.contains("<html>")){
        			s=Basics.stripSimpleHtml(s);
        		}
                if (!Basics.isEmpty(s)) {
                	s=s.trim();
                }
        		super.setText(s);
            }
            
            private CallBack _callBack;
            

            public void setBorder(Border b) {}
            
            
			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
					int condition, boolean pressed) {
				final int kc = e.getKeyCode();
				if (kc == KeyEvent.VK_ENTER || kc == KeyEvent.VK_TAB) {
					returnPressed = true;
					
					userExplicitlySelected=true;
					if (ac.renderer instanceof SubListRenderer){
						final int idx=((SubListRenderer)ac.renderer).selectedIndex;
						if (idx>=0){
							final Object item=ac.getItemAt(idx);
							if (item !=null){
								final String strippedItem = Basics.stripSimpleHtml(((String)item)).trim(); 
								if (ac.unselectableItems.contains(strippedItem)){
									ac.setSelectedIndex(-1);
								}
							}
						}
					}
					if (SwingBasics.isMacLookAndFeel() && ac.isPopupVisible()){
						if (ac.renderer instanceof Renderer){
							final int idx=((Renderer)ac.renderer).selectedIndex;
							if (idx>=0){
								final Object item=ac.getItemAt(idx);
								if (!ac.unselectableItems.contains(item)){
									ac.setSelectedIndex(idx);
								}
							}
						}
					}
					itemSelectedOnReturn=ac.getSelectedItem();
					
					if (!pressed && condition == 0) {
						if (ac.getSelectedItem()==null) {
							if (!ac.ignoreTheDefaultButton) {
								ac.ignoreTheDefaultButton = true;
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										ac.respectDefaultButton();
										ac.ignoreTheDefaultButton = false;
									}
								});
							}
						}
						
					}
				}
				final boolean ok; 
				if ( (kc == KeyEvent.VK_DOWN || kc == KeyEvent.VK_UP
						|| kc == KeyEvent.VK_PAGE_DOWN
						|| kc == KeyEvent.VK_PAGE_UP) && SwingBasics.isMacLookAndFeel()) {
						if (!pressed) {
							nextSelection = null;// arrow movements?
							movingArrowAround = true;
						}
						System.out.print(Basics.concatObjects("Pressed MOVEMENT on MAC pressed=", pressed, ", condition=", condition));
						nextSelection = null;// arrow movements?
						movingArrowAround = true;
						if (!pressed && condition == 0) {
							handleKeyEvent(e);
							return true;
						} else {
							ok=ac.processKeyBinding(ks, e, condition, pressed);// respectDownKey();)
							System.out.print(Basics.concatObjects(", ok=", ok));
						}
						System.out.println();
				} else {
					ok = super.processKeyBinding(ks, e, condition,
						pressed);
				}
				if (ok) {
					if (!pressed && condition == 0) {
						keycnt++;
						if (haveNextKeyHideToolTip){
							ToolTipOnDemand.getSingleton().hideTipWindow();
							haveNextKeyHideToolTip=false;
						}
						handleKeyEvent(e);
						movingArrowAround = false;
					}
				} else if (!pressed
						&& (kc == KeyEvent.VK_DOWN || kc == KeyEvent.VK_UP
								|| kc == KeyEvent.VK_PAGE_DOWN || kc == KeyEvent.VK_PAGE_UP)) {
					nextSelection = null;// arrow movements?
					movingArrowAround = true;
				}
				if (pressed && kc == KeyEvent.VK_ENTER  && ac.tableEdited!=null && !ac.isPopupVisible()){
					final TableCellEditor tce=ac.tableEdited.getCellEditor();
					if (tce instanceof CellEditor){
						tce.stopCellEditing();
					}
				}
				return ok;
			}
		}

        private boolean movingArrowAround=false;
        
        private boolean returnPressed=false;
        private Object itemSelectedOnReturn=null;

        private void add(final Object data){
        	if (ac.sequentialMatcher==null && !Basics.isEmpty(data) && !ac.unselectableItems.contains(data)) {
        		if (_data==null){
        			_data=new TernarySearchTree();
        		}
        		final String s=ac.toString(data);
                _data.put(s, data);
                if (ac.isCaseSensitive){
                	if (caseSensitive==null){
                		caseSensitive=new LinkedHashMap<String, Object>();
                	}
                	caseSensitive.put(ac.sc.toString(data), data);
                }
            }
        }
        
        final ComboBoxEditor priorDefaultEditor;
        public SearchEditor(final AutoComplete ac) {
        	priorDefaultEditor=ac.getEditor();
            this.ac = ac;
            editor = new BorderlessTextField("",9);
            //editor.setBorder(null);
            // populate the search tree with the items in the list
            final ComboBoxModel model = ac.getModel();

            for (int i = 0; i < model.getSize(); i++) {
                add(model.getElementAt(i));
            }
            //editor.addKeyListener(listener);
            final TransferHandler defaultTransferHandler = editor.
              getTransferHandler();
            editor.setTransferHandler(new TransferHandler() {
                public boolean importData(final JComponent c,
                                          final Transferable t) {
                    final boolean b = defaultTransferHandler.importData(c, t);
                    if (b) {
                        System.out.println(
                        		Basics.concat("Pasted or dropped:  ",
                                           editor.getText()));
                        findItemWithWhichToAutoComplete(editor.getText());
                    }
                    return b;
                }

            });

            // register an action listener to keep the text area always up-to-date
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                	if (!ac.isHandlingPendingItems()){
                        final Object item = ac.getSelectedItem();
                		final boolean isForbidden=ac.isForbiddenFruit(item);
                        if (item != null && !ac.unselectableItems.contains(item) && !isForbidden) {
                        	final String text = editor.getText();
                            final String itemText = ac.sc.toString(item);
                            if (ac.fl != null) {
                                ac.fl.completionFound(item);
                            }
                            if (!text.equals(itemText)) {
                                editor.setText(itemText);
                            }                            
                            

                        }
                    }
                    if (returnPressed || isFinishedWithComboBox(e)){
                    	userExplicitlySelected=true;
                    }

                }
            };

            ac.addActionListener(actionListener);
        }

        private void handleKeyEvent(final KeyEvent e){
            final char c=e.getKeyChar();
            final int kc=e.getKeyCode();
            final boolean b1=e.isAltDown(), b2=e.isControlDown();
            handleKey(c, kc, b1, b2);
        }

        private void handleKey(char currentChar, final int keyCode, final boolean isAltDown, final boolean isControlDown){
            System.out.println("!  ");
            final int ascii = currentChar;
            if (currentChar >= ' ' && currentChar <= '~' && !isAltDown&& !isControlDown) {
            	if(ac.resetMousePointer){
            		try {
            			new Robot().mouseMove((int)editor.getLocationOnScreen().getX()+75,(int)editor.getLocationOnScreen().getY());
            		} catch (AWTException e) {
    				e.printStackTrace();
            		}	
    			}
            	if (ascii == 8) {
            		currentChar = editor.getText().charAt(editor.getCaretPosition() - 1);//apply the previous char back
            	}
            	
                String startText = editor.getText();
                final int cp = editor.getCaretPosition();

                System.out.print(Basics.concatObjects("char='", currentChar, "', startText=\"", startText,
                                 "\", cp=", cp, ", "));
                if (handlePendingItems()) {
                    editor.setText(startText);
                }
                int caretPosition = editor.getCaretPosition();
                
                int currentCharPosition = startText.lastIndexOf(currentChar);
                if (startText.length()==caretPosition && currentCharPosition == -1){
                    startText += currentChar;
                    System.out.print(Basics.concat("  change 3 startText=\"", startText, "\"  "));
                    //editor.setText(startText);
                    caretPosition++;
                }
                final boolean textFieldHasProcessedFutureKeyEvents = /* is use typing fast?*/
                  startText.length() == editor.getCaretPosition() && currentCharPosition>=0 && currentCharPosition < editor.getCaretPosition() - 1;
                System.out.print( Basics.concatObjects("-->", textFieldHasProcessedFutureKeyEvents, ", caret=" + caretPosition + ", startText=\"", startText, 
                                       "\", literal start='", literallyTypedText, "', currentCharPosition=", currentCharPosition, ", "));
                if (textFieldHasProcessedFutureKeyEvents){
                    if (!Basics.isEmpty(nextSelectionText)){
                        String s=startText.substring(0, currentCharPosition+1);
                        if (nextSelectionText.equalsIgnoreCase(startText)){
                            System.out.print(Basics.concatObjects("  change 4 startText=\"", startText, "\"  "));
                            startText=s;
                        }

                    }
                }
                determineLiterallyTypedText(currentChar, startText, caretPosition, textFieldHasProcessedFutureKeyEvents);
                final String s=startText.startsWith(literallyTypedText)?startText:literallyTypedText;
                findItemWithWhichToAutoComplete(s);
                if (startText.length() == 1) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                        	if (editor.getText().length()==1) { // avoid conflict with fast typing and typeahead 
                        		editor.setSelectionStart(1);
                           		editor.setSelectionEnd(editor.getText().length());
                        	}
                        }
                   });                	
                }
                warnUserIfChangesInUseOfCapitalLetters();
            } else {
                boolean usefulKey = false; // not interested in shift key, ctrl key, alt key
                if (keyCode == KeyEvent.VK_UP) {
                    usefulKey = true;
                    handlePendingItems();
                    if (!ac.arrowKeysSelect) {
                        final int idx = ac.getSelectedIndex() - 1;
                        ac.setSelectedIndex(idx < 0 ? 0 : idx);
                    }
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    usefulKey = true;
                    handlePendingItems();
                    if (!ac.arrowKeysSelect) {
                        final int idx = ac.getSelectedIndex() + 1;
                        ac.setSelectedIndex(idx == ac.getItemCount() ?
                                            ac.getItemCount() - 1 : idx);
                    }
                } else if (keyCode == KeyEvent.VK_DELETE ||
                    keyCode == KeyEvent.VK_BACK_SPACE || 
                    ascii == 127 || ascii == 8) {
                    usefulKey = true;
                    String et = editor.getText();
                    if (et != null) {
                    	boolean specialHandling=false;
                        final int pos = editor.getCaretPosition();
                        if (literallyTypedText != null) {
                            if (pos == et.length()) {
                                //manageLiterallyTypedText('\0');
                                if (keyCode ==
                                    KeyEvent.VK_BACK_SPACE || ascii == 8) {
                                    ac.setSelectedItem(null);
                                    nextSelection = null;
                                    nextSelectionText = null;
                                    ac.hidePopup();
                                    specialHandling=true;
                                    editor.setText(et);
                                } else {
                                    if (et.length() == literallyTypedText.length()) {
                                        ac.setSelectedItem(null);
                                        nextSelection = null;
                                        nextSelectionText = null;
                                        ac.hidePopup();
                                        specialHandling=true;
                                        editor.setText(literallyTypedText);
                                    }
                                }
                            }
                            et = editor.getText();
							final Object o = get(et);
							if (o != null) {
								ac.setSelectedItem(o);
								if (specialHandling) {
									ac.showPopup();
								}
							} else {
                                ac.hidePopup();
								indicateNotFound();
							}
						  literallyTypedText = et;
                        }
                    }
                }else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                    usefulKey = true;
                }
                if (usefulKey) {
                    lastCapitalWarning=null;
                    if (callBack == null || !ac.isPopupVisible() || callBack.getPopupVisibleToolTipText()==null){
                    	ToolTipOnDemand.getSingleton().hideTipWindow();
                    }
                }
            }
            //keyEvent.consume();

        }

        private void indicateNotFound(){
            if (ac.fl != null) {
                ac.fl.completionFound(null);
            }
            if (!ac.allowNew) {
                Toolkit.getDefaultToolkit().beep();
                final String tt=Basics.toHtmlErrorUncentered(
                        "Not found", 
                        callBack==null?
                        		Basics.concat("The value \"<b>",
                        		editor.getText(),
                        		"</b>\" is not in list!"):
                        			callBack.getNotFoundText(editor.getText()));
                        
                ac.conveyProblem(PersonalizableTable.
                                 BORDER_ERROR_FOCUS,
                                 tt);
                ToolTipOnDemand.getSingleton().showLater(ac, false, callBack==null?null:callBack.getNotFoundComponent());
                
                	SwingUtilities.invokeLater(new Runnable(){
                		public void run(){
                			ac.requestFocus();
                		}
                	});
                
            } else {
                ac.conveyProblem(PersonalizableTable.
                                 BORDER_INCOMPLETE, ac.originalToolTip);
            }

        }

        private String nextSelectionText, lastCapitalWarning;
        Object nextSelection; 
        private boolean findItemWithWhichToAutoComplete(final String text) {
        	final String startText=text.toLowerCase();
            final String editorText=editor.getText();
            String finalText = matchPrefix(editorText);
            if (finalText.equals("")) {
				finalText = matchPrefixIgnoreCase(startText);
				if (finalText.equals("")) {
					finalText = editorText;
				}
				nextSelection=getIgnoreCase(finalText);
			} else {
				nextSelection = get(finalText);
			}
            nextSelectionText=ac.sc.toString(nextSelection);
            final Object currentSelection = ac.getSelectedItem();
            final int idx=editor.getCaretPosition();
            ac.setSelectedItem(nextSelection);
            int selectionStart=-1, selectionEnd=-1;
            if (nextSelection!=null && (( ac.isCaseSensitive && !finalText.equals(text)) || 
            		(!ac.isCaseSensitive && !finalText.equalsIgnoreCase(text)) || 
            		!nextSelectionText.equals(editorText))) {

                final String n2;
                if (startText.equalsIgnoreCase(editorText)){
                	String suffix=nextSelectionText.substring(editorText.length());
                	if (ac.isForbiddenFruit(nextSelection) ||(nextSelection instanceof String && ((String)nextSelection).startsWith("<html>"))){
                    	suffix="";
                    }
                    n2 = Basics.concat(editorText,suffix);
                } else {
                	String suffix=nextSelectionText.substring(startText.length());
                	if (ac.isForbiddenFruit(nextSelection)||(nextSelection instanceof String && ((String)nextSelection).startsWith("<html>"))){
                    	suffix="";
                    }
                	n2 = Basics.concat(startText, suffix);
                }
                editor.setText(n2);
                selectionStart=startText.length();
                selectionEnd=finalText.length();
                editor.setSelectionStart(selectionStart);
                editor.setSelectionEnd(selectionEnd);
                System.out.println(Basics.concatObjects("start text=\"", startText,
                                   "\", final text=\"", finalText, "\", n2=\"",
                                   n2, "\", selectionStart=", selectionStart, ", selectionEnd=",selectionEnd));
            }
            lostSelection = currentSelection != null &&
                            nextSelection == null;
            System.out.print(Basics.concatObjects(
            		"lost selection=", lostSelection, ", cp=", editor.getCaretPosition(), ", next selection= \"", nextSelection, "\" selected item=\"", 
                               ac.getSelectedItem(),  "\",  selected index=",
                               ac.getSelectedIndex()));
            if (lostSelection) {
                editor.setText(literallyTypedText);
        	    if (idx > literallyTypedText.length() || idx < 0) {
        	    	System.out.println();
        	    	System.out.print( Basics.concatObjects("--- >  idx=", idx, " but literallyTypedText=", literallyTypedText, "??"));
        	    } else {
        	        final Document doc = editor.getDocument();
        	        if (doc != null) {
        		    if (idx <=doc.getLength() && idx >= 0) {

        		    	editor.setCaretPosition(idx);
        		    }
        	        }
        	    }
                ac.hidePopup();
            }
            if (nextSelection == null) {
                if (!ac.allowNew) {
                    editor.setText(finalText);
                } 
                indicateNotFound();
            } else {

            	 if (ac.autoSelect) {
            		 	 final String txt=editor.getText();
                         ac.showPopup();
                         if (!Basics.equals(txt, editor.getText())){
                         	editor.setText(txt);
                         }
                         if (selectionStart >= 0) {
 							editor.setSelectionStart(selectionStart);
 						}
                     	if (selectionEnd >= 0) {
 							editor.setSelectionEnd(selectionEnd);
 						}
                     }                 
                else {
                    _updateUI(ac);
                }
             /*   if (literallyTypedText.equalsIgnoreCase(nextSelectionText)){
                    editor.setCaretPosition(literallyTypedText.length());
                }*/
                if (ac.writeOnly) {
                	ac.conveyProblem(PersonalizableTable.
                            BORDER_ERROR_FOCUS,
                            Basics.toHtmlErrorUncentered(
                              "Already exists", Basics.concat("The value \"<b>", nextSelectionText, "</b>\" already exists!")));
                	ToolTipOnDemand.getSingleton().showLater(ac);
                }
            }
            System.out.println(Basics.concat(", start text=\"", editor.getText(), "\""));
        	if (ac.forbidden!=null){
        		try{
        		if (ac.forbidden.contains(ac.sc.toObject(text))){
                    final String tt=Basics.toHtmlUncentered(
                            Basics.concat("The value \"<b>", text, "</b>\" is not allowed!"));
                    ac.conveyProblem(PersonalizableTable.BORDER_ERROR_FOCUS,tt);
                    ToolTipOnDemand.getSingleton().showLater(ac);
        		}
        		} catch(final Exception e){
        			e.printStackTrace();
        		}
        	}

            return nextSelection != null;
        }

        private void warnUserIfChangesInUseOfCapitalLetters(){
            if (ac.warnIfChangesCapitalLetters && ac.isCaseSensitive && ac.allowNew){
                if (nextSelectionText != null &&
                    nextSelectionText.toLowerCase().
                    startsWith(literallyTypedText.toLowerCase()) &&
                    !nextSelectionText.startsWith(literallyTypedText)) {
                    if (!literallyTypedText.equals(lastCapitalWarning)){
                        final String txt = editor.getToolTipText();
                        final char[] previous = nextSelectionText.toCharArray(),
                                                current = literallyTypedText.toCharArray();
                        lastCapitalWarning = literallyTypedText;
                        final StringBuilder sb = new StringBuilder(Basics.
                          startHtmlUncentered());
                        sb.append("<b>Use different capital letters?</b><br><br><table border='1'><tr><td>.</td><td><small><font color='blue'>Old </font></small></td><td><small><font color='blue'>New </font></small></td></tr><tr><td><small><font color='blue'>Spelling</font></small></td><td>");
                        sb.append(nextSelectionText);
                        sb.append("</td><td>");
                        for (int i = 0; i < previous.length; i++) {
                            if (i >= current.length || previous[i] == current[i]) {
                                sb.append(previous[i]);
                            } else {
                                sb.append("<font color='red'><b>");
                                sb.append(current[i]);
                                sb.append("</b></font>");
                            }
                        }
                        sb.append("</td></tr><tr><td><small><font color='blue'>To accept press:</font></small></td><td><b>&lt;Enter&gt;</td></b><td><b>&lt;Tab&gt;</b></td></tr></table>");
                        sb.append(Basics.endHtmlUncentered());
                        final String html = sb.toString();
                        editor.setToolTipText(html);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                final int c = editor.getColumns() * 13;
                                ToolTipOnDemand.getSingleton().show(editor, true, c, 85, true);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        editor.setToolTipText(txt);
                                    }
                                });
                            }
                        });
                    }
                } else {
                    lastCapitalWarning=null;
                    ToolTipOnDemand.getSingleton().hideTipWindow();
                }
            }
        }

		public Component getEditorComponent() {
			return editor;
		}

		public void selectAll() {
			priorDefaultEditor.selectAll();			
		}
		public void addActionListener(ActionListener l) {
			priorDefaultEditor.addActionListener(l);			
		}

		public void removeActionListener(ActionListener l) {
			priorDefaultEditor.removeActionListener(l);
		}
    }

    public static class CellEditor extends AbstractCellEditor 
    	implements TableCellEditor, PersonalizableCellEditor {
    	static boolean showPopupOnFirstFocus=false;

    	private boolean trimWhenEditing=false;
    	public void setTrimWhenEditing(final boolean ok) {
    		trimWhenEditing=ok;
    	}
        public boolean isCellEditable(final java.util.EventObject evt) {
            if (evt instanceof MouseEvent) {
                return ((MouseEvent) evt).getClickCount() >= 2;
            }
            return true;
        }
        private Object untrimmed=null;
        public Component getTableCellEditorComponent(
          final JTable table,
          final Object _value,
          final boolean isSelected,
          final int row,
          final int column) {
            Object value=_value;
            if (trimWhenEditing && (value instanceof String )&& Basics.isEmpty(value)) {
            	untrimmed=value;
            	value=((String)value).trim();
            }
            autoComplete.tableEdited = table;
            autoComplete.lastKeyEvent=null;
            autoComplete.tableEditingStarted=false;
            final Window wnd=SwingUtilities.getWindowAncestor(table);
    		if (wnd instanceof RootPaneContainer){
    			autoComplete.tableRoot=((RootPaneContainer)wnd).getRootPane();
    			autoComplete.dfltButton=autoComplete.tableRoot.getDefaultButton();
    			autoComplete.tableRoot.setDefaultButton(null);
    		}
            if (!(value instanceof SelectableCell)) {
                if (!autoComplete.allowNew && value != null){
                	if (SwingBasics.indexOf(autoComplete,value)>=0){
                		suppressActionPerformed=true;
                		autoComplete.setSelectedItem(value);
                		suppressActionPerformed=false;
                	} else {
                		final int idx=autoComplete.getSelectedIndex();
                		autoComplete.selectAll();
                		SwingUtilities.invokeLater(new Runnable() {
							
							public void run() {
                		SwingUtilities.invokeLater(new Runnable() {
							
							public void run() {
								int idx1=idx,idx2=autoComplete.getSelectedIndex();
								if (idx1==idx2){
									autoComplete.setPopupVisible(true);
								}
							}
						});
							}
						});
                	}
                } else {
                	suppressActionPerformed=true;
           
                	autoComplete.setSelectedItem(value);
                	suppressActionPerformed=false;
                    
                }
            }
            if (selectAllWhenFocusGained && !Basics.isEmpty(_value)) {
                autoComplete.selectAll();
            }
            if (autoComplete.tableEdited != null &&
                surrenderFocusOnKeystroke) {
                // start editing toute suite
                autoComplete.tableEdited.setSurrendersFocusOnKeystroke(true);
            }
            if (showPopupOnFirstFocus){
            	SwingUtilities.invokeLater(new Runnable(){
            		public void run(){
            			SwingUtilities.invokeLater(new Runnable(){
                    		public void run(){
                            	autoComplete.requestFocus();            	                    			
                    		}
                    	});		
            		}
            	});
            }
            return autoComplete;
        }

        public Object getCellEditorValue() {            
        	Object o = autoComplete.getFinalValue();
            if (autoComplete.dfltButton!=null){
    			autoComplete.tableRoot.setDefaultButton(autoComplete.dfltButton);	
    		}
        	if (trimWhenEditing && (o instanceof String) && Basics.isEmpty(o)) {
            	o=untrimmed;
            }
            if (o == null && autoComplete.originalItem!=null) {
                return autoComplete.originalItem;
            }
            return o;
        }

        public AutoComplete autoComplete;
        private final boolean selectAllWhenFocusGained,
        surrenderFocusOnKeystroke;
        private ActionEvent lastAction=null;
    	
        CellEditor(
          final AutoComplete autoComplete,
          final boolean selectAllWhenFocusGained,
          final boolean surrenderFocusOnKeystroke) {
            this.surrenderFocusOnKeystroke = surrenderFocusOnKeystroke;
            this.autoComplete = autoComplete;
            this.selectAllWhenFocusGained = selectAllWhenFocusGained;
            if (!SwingBasics.isMac){
            	autoComplete.putClientProperty("JComboBox.isTableCellEditor",Boolean.TRUE);
            }
            autoComplete.arrowKeysSelect = false;
            
            this.autoComplete.addEditorFocusListener(new FocusAdapter() {
            	
                public void focusGained(FocusEvent e) {
	          		SwingUtilities.invokeLater(new Runnable() {
		        		public void run() {
		        			doNotDisplayImgTags(null);
		        			if (showPopupOnFirstFocus && firstFocus){
		        				SwingUtilities.invokeLater(new Runnable() {
									public void run() {
				        				autoComplete.setPopupVisible(true);
				        				autoComplete.selectAll();
				        				showPopupOnFirstFocus=false;
									}
								});
		        			}
		        			firstFocus=false;
		        		}
	        		});                    	
                }
            });

            
            this.autoComplete.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                	autoComplete.performedActionFor=null;
                	lastAction=e;
                	final boolean done=autoComplete.explicitlySelected(e);
                	if (!done) {
                		if (doNotDisplayImgTags(null)) {
                			return;                			
                		}
                	}
                	if (autoComplete.mouseClickGesturedSubListExpansion){
                		System.out.println();
                	} else if(!suppressActionPerformed && done ){
                		final Object o=autoComplete.getSelectedItem();
                		autoComplete.performedActionFor=o;	
                		userExplicitlySelected=true;
                		if (!autoComplete.unselectableItems.contains(o)) {
                			if (CellEditor.this.autoComplete.isPopupVisible()){
                				autoComplete.setPopupVisible(false);
                			} else {
                				CellEditor.this.stopCellEditing();
                			}
                        	SwingUtilities.invokeLater(
                        			new Runnable() {
                        				public void run() {
                        					SwingUtilities.invokeLater(
                                    			new Runnable() {
                                    				public void run() {
                    					doNotDisplayImgTags(null);                        					
                    				}
                    			});                   					
                        				}
                        			});
                        } else {
                        	SwingUtilities.invokeLater(
                        			new Runnable() {
                        				public void run() {
                                        	autoComplete.showPopup();
                                        	autoComplete.setSelectedItem(null);
                        				}
                        			});
                        }
                    }

                }
            });
            this.autoComplete.addPopupMenuListener(new PopupMenuListener() {
            	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {            		
            		final int idx=autoComplete.getSelectedIndex();
                    if (idx <0 || (idx == 0 && autoComplete.getItemAt(0).equals(""))) {
                    	suppressActionPerformed=true;
                         if (autoComplete.getItemCount()>0 && !autoComplete.getItemAt(0).equals("")) {
                                  autoComplete.setSelectedIndex(0);
                         }
                         else if (autoComplete.getItemCount()>1) { //At times the first item is set to ""
                                  autoComplete.setSelectedIndex(1);
                         }
                         suppressActionPerformed=false;
                    } else {
                    	final Object item=autoComplete.getSelectedItem();
                    	final String text = ((JTextField) autoComplete.editor.getEditorComponent()).getText();
                    	if (!doNotDisplayImgTags(autoComplete.sc.toString(item)) && !Basics.equals(autoComplete.sc.toString(item), text)) {
                    		System.out.println("User altered field from selected value...");
                    		suppressActionPerformed=true;
                			if (!autoComplete.isForbiddenFruit(item)) {
                        		((JTextField) autoComplete.editor.getEditorComponent()).setText(autoComplete.sc.toString(item));
                			}
                    		suppressActionPerformed=false;
                    	}
                    }
 
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                	autoComplete.fireActionEventIfQuaQuaAndTheSameObject();
                	if (lastAction!=null && lastAction.getModifiers()==16){
                		((SearchEditor)autoComplete.getEditor()).returnPressed=true;
                		((SearchEditor)autoComplete.getEditor()).editor.setText(Basics.toString(autoComplete.getSelectedItem()));
                		stopCellEditing();
                	}
                }

                public void popupMenuCanceled(PopupMenuEvent e) {}
            });
        }
        
        private boolean firstFocus=true;;

        private boolean doNotDisplayImgTags(String text) {        	
        	
        	final Component comp = autoComplete.editor.getEditorComponent();
        	if (comp == null  || !(comp instanceof JTextField)) {
        		return false;
        	}
        	if (text == null) {
        		text = ((JTextField)comp).getText();
        	}
        	if (text.contains("<img") ) {
        		final Object o=autoComplete.getSelectedItem();
        		if (autoComplete.isForbiddenFruit(o)){
        		suppressActionPerformed=true;
        		((JTextField)comp).setText(null);
        		suppressActionPerformed=false;
        		return true;
        		}
        	}
        	return false;
        }
        
        private boolean suppressActionPerformed=false;
        public static CellEditor New(
          final Object[] allowedValues,
          final Collection unselectableValues,
          final Collection forbiddenValues,
          final boolean surrenderFocusOnKeystroke,
          final boolean selectAllWhenFocusGained,
          final StringConverter sc,
          final boolean allowNew,
          final boolean isCaseSensitive,
          final boolean autoSelect,
          Object defaultItem) {
        	final AutoComplete ac = new AutoComplete(allowedValues, unselectableValues, sc, allowNew,
              autoSelect);
            ac.setIsCaseSensitive(isCaseSensitive);
            if (!Basics.isEmpty(forbiddenValues)){
            	ac.setForbidden(forbiddenValues);
            }
            if (defaultItem == null 
            		&& !Basics.contains(allowedValues, defaultItem)){
            	if(!Basics.isEmpty(allowedValues.length)){
            		for (int i=0;i<allowedValues.length;i++){
            			if ((Basics.isEmpty(forbiddenValues)||
            					!forbiddenValues.contains(allowedValues[i]))
            					&&
            					(Basics.isEmpty(unselectableValues)||
            					!unselectableValues.contains(allowedValues[i]))){
            				defaultItem=allowedValues[i];
            				break;
            			}
            		}
            	}
            }
            ac.setDefaultItem(defaultItem);
            CellEditor editor=new CellEditor(ac, selectAllWhenFocusGained,
              surrenderFocusOnKeystroke);
            return editor;
        }

        public static CellEditor New(
        		final Map<String, java.util.List<String>> synonymMap,
                final boolean surrenderFocusOnKeystroke,
                final boolean selectAllWhenFocusGained,
                final StringConverter sc,
                final boolean allowNew,
                final boolean isCaseSensitive,
                final boolean autoSelect,
                Object defaultItem) {
        	final AutoComplete ac = Synonyms(synonymMap);
        	ac.setIsCaseSensitive(isCaseSensitive);
        	if (defaultItem != null){
        		ac.setDefaultItem(defaultItem);
        	}
        	CellEditor editor=new CellEditor( ac,
        			selectAllWhenFocusGained,
        			surrenderFocusOnKeystroke);
        	return editor;
        }

        public KeyEvent lastKeyEvent() {
			return autoComplete.lastKeyEvent;
		}
        
    	public boolean didTableEditingStart() {
    		return autoComplete.tableEditingStarted;
    	}

    }
    
    public static final Map<String, java.util.List<String>> SynonymMap(final String synonymsFile) {    	
    	HashMap<String, java.util.List<String>> items = new HashMap<String, java.util.List<String>>();
    	try {    		
    		ArrayList<String> al = IoBasics.readTextFileLines(synonymsFile);
    		for (String synonyms: al) {
    			final StringTokenizer st = new StringTokenizer(synonyms, "\t");
    			final String synonym = st.nextToken();
    			ArrayList<String> synlist = new ArrayList<String>(); 
    			while (st.hasMoreTokens()) {
    				synlist.add(st.nextToken());    				
    			}
    			items.put(synonym, synlist);    			
    		}
    	}
    	catch(final Exception e) {
    		ArrayList<String> list = new ArrayList<String>();
    		list.add("no file");
    		list.add("file missing");
    		list.add("mis-filed file");
    		list.add("file vanished");
    		list.add("file not filed");
    		items.put("File not found", list);
    		list = new ArrayList<String>();
    		final File f=new File(synonymsFile);
    		list.add(f.getName());
    		list.add(f.getParent());
    		items.put(synonymsFile, list);
    		e.printStackTrace(System.err);
    	}    	
    	return items;
    }
    private final static String SYNONYMS="Synonyms";
    public static AutoComplete Synonyms(final Map<String, java.util.List<String>>items){
    	final AutoComplete ac = new AutoComplete(items, SYNONYMS);    		
		ac.resetMousePointer=true;
		ac.setMaximumRowCount(12);
		ac.setPreferredSize(new Dimension(100,30));
		ac.setForbidden(Basics.toList(SYNONYMS));
		ac.allowNew=true;
		return ac;
    }

    public static AutoComplete Synonyms(final ItemFactory ifa){
    	final AutoComplete ac = Synonyms(new HashMap<String, java.util.List<String>>());
    	ac.addOnNextKeyEvent(ifa);
    	return ac;
    }
    public static void main(final String[] args) {
    	List<String[]>l1=new ArrayList<String[]>(),l2=new ArrayList<String[]>();
		l1.add(new String[]{"hi", "there"});
		l2.add(new String[]{"hi", "there"});
		l1.add(new String[]{"hit", "theres"});
		l2.add(new String[]{"hit", "theres"});
		boolean ok=Basics.equals(l1, l2);
		l1.add(new String[]{"hit", "top", "theres"});
		l2.add(new String[]{"hit", "theres2"});
		boolean ok2=Basics.equals(l1, l2);
		l1.set(2, new String[]{"hit", "theres2"});
		boolean ok3=Basics.equals(l1, l2);
		//l1.set(0, l2.get(1));
		l1.add(new String[]{});
		boolean ok4=Basics.equals(l1, l2);
    	SwingBasics.init();
    	java.awt.Image appImage=MmsIcons.getLeonard().getImage();
        final javax.swing.JFrame f = new javax.swing.JFrame();
        SwingBasics.setScreenRec(1);

        String[] stuff = new String[] {
                         "first", "second", "second and some",
                         "third", "third time lucky", "Next section", "third time again",
                         "three", "three+",
                         "thrice", "four", "four-plus"};
        AutoComplete autoComplete = new AutoComplete(stuff, Basics.toList("third"), DefaultStringConverters.get(), false, true);
        autoComplete.setUnselectable(Basics.toList("Next section"));
        f.setLayout(new BorderLayout(25, 25));
        f.setTitle("Test auto complete:");
        JButton c = new JButton("Close");
        c.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//PopupBasics.relativeToParent=true;
            	PopupBasics.alert(f, "Ok");
                SwingBasics.closeWindow(f);
            }
        }
        );
        GridLayout grid = new GridLayout(1, 3);
        JPanel jp = new JPanel(grid);
        jp.add(autoComplete);
        final String[] colors1 = new String[] {"blackyd", "cd4", "cd8a", "cd2",
                                 "black", "green", "gray", "blonde", "yellow",
                                 "purple", "pink", "red", "BROWN", "blue",
                                 "magenta", "turquoise", "orange", "cyan",
                                 "crimson"};
        final String[] colors = new String[] {"blacky", "cd4", "cd8a", "cd2",
                                "purple", "pink", "red", "BROWN", "blue",
                                "black", "green", "gray", "blonde", "yellow",
                                "magenta", "turquoise", "orange", "cyan",
                                "crimson"};
        final Map<String, java.util.List<String>> synonymMap=SynonymMap("/Users/swmeehan/Documents/workspace/CytoGate/matlabsrc/Markers.txt");
        final Object[]synonymKeys=synonymMap.keySet().toArray(new String[0]);
        final boolean TESTING_FAST_SYNONYMS=true;
        final AutoComplete ac;
        if (! TESTING_FAST_SYNONYMS){
        	ac = Synonyms(synonymMap);
        }else{
        	ItemFactory ifa=new ItemFactory(){
        		public Object[] getAutoCompleteItems(){
        			return synonymKeys;
        		}
        		public Map<String, java.util.List<String>> getSynonymMap(){
        			return synonymMap;
        		}

        	};

        	ac = Synonyms(ifa);
        	        	
        }
        ac.setSelectedItem("CD5");
        jp.add(ac);
        final JComboBox cb2 = new JComboBox(new String[] {"normal JComboBox",
                                            "Should not", "change"});
        jp.add(cb2);
        f.add(jp, BorderLayout.NORTH);
        final JPanel south=new JPanel();
        south.add(c);
        f.add(south, BorderLayout.SOUTH);

        final String[] names = new String[] {
                               "default textfield",
                               "Auto complete",
                               "non editable mms",
                               "editable mms",
                               "non editable default combo box",
                               "editable combo box",
                               "marker synonyms"
        };
        Object[][] data = new Object[][] {
                          new Object[] {
                          "green", "black", "pink", "white", "black", "blue", "CD8"}
                          ,
                          new Object[] {
                          "green", "gray", "cyan", "crimson", "yellow", "red", "CD4"}
        };
        JTable table = new JTable(data, names);
        table.setRowHeight(25);
        table.setIntercellSpacing(new Dimension(17, 0));

        final TableColumnModel tcm = table.getColumnModel();

        TableColumn col = tcm.getColumn(0);
        JTextField tf = new JTextField();
        col.setCellEditor(new DefaultCellEditor(tf));
        col = tcm.getColumn(1);
        Arrays.sort(colors);
        col.setCellEditor(
          CellEditor.New(
            colors,
            Basics.toList(new String[] {"pink"}),
            Basics.toList(new String[] {"green"}),
            true,
            true,
            DefaultStringConverters.get(String.class),
            true, true, false, null)
          );
        col = table.getColumnModel().getColumn(2);
        col.setCellEditor(ComboCellEditor.New(java.util.Arrays.asList(colors), false, true,
                                              500));
        col = table.getColumnModel().getColumn(3);
        col.setCellEditor(ComboCellEditor.New(java.util.Arrays.asList(colors), true, true,
                                              500));
        
        col = table.getColumnModel().getColumn(4);
        JComboBox jcb = new JComboBox(colors);
        
        col.setCellEditor(new DefaultCellEditor(jcb));
        col = table.getColumnModel().getColumn(5);
        jcb = new JComboBox(colors);
        
        jcb.setEditable(true);
        col.setCellEditor(new DefaultCellEditor(jcb));

        col = table.getColumnModel().getColumn(6);
        col.setCellEditor(
                CellEditor.New(
                  synonymMap,
                  true,
                  true,
                  DefaultStringConverters.get(String.class),
                  true, true, false, null)
                );

        f.add(new JScrollPane(table), BorderLayout.CENTER);
        f.pack();
        f.setSize(1200, 600);
        SwingBasics.personalize(f, "testAuto", true);
        final WindowListener[]a=f.getWindowListeners();
        ac.requestFocus();
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent ev) {
                SwingUtilities.invokeLater(new Runnable(){
                	public void run(){
                		System.exit(0);
                	}
                });
            }
        }
        );
        f.show();
    }

    public void setWarnIfChangesCapitalLetters(final boolean ok){
        warnIfChangesCapitalLetters=ok;
    }

    private boolean warnIfChangesCapitalLetters=true;
    
    static void notifyCompletionFoundIfNecessary(final JComboBox jcb, final FoundListener fl){
        if (fl != null){
            final Object o = jcb.getSelectedItem();
            if (o != null) {
                fl.completionFound(o);
            }
        }

    }
    interface StartingTextProvider{
            String getStartingText();
        }

    private static Point lastPopupLocation;

    private final static Border STANDARD_BORDER_INSETS =BorderFactory.createEmptyBorder(0, 4, 1, 3);
    static final class Renderer implements ListCellRenderer {
        private StartingTextProvider ksm;
        private AutoComplete ac;
        private JLabel label= new JLabel();
        private  JList list;
        private  Font normal=null,title=null;
        private  Color bg,fg,sbg,sfg;
        private  Collection unselectableItems, forbidden;
	    private Object selected;
	    int selectedIndex;
        Renderer(final StartingTextProvider ksm, final AutoComplete ac) {
			this.ksm=ksm;
			this.ac= ac;
			label.setHorizontalTextPosition(JLabel.LEFT);
			label.setOpaque(true);
			label.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
			//label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),BorderFactory.createEmptyBorder(20,20,0,0)));
       

		}
        private boolean mouseInstalled=false;
    final StringBuilder sb=new StringBuilder();
    public Component getListCellRendererComponent(
      final JList list,
      final Object value,
      final int index,
      final boolean isSelected,
      boolean cellHasFocus) {
    	
    	if (list.isShowing()){
    		lastPopupLocation=list.getParent().getLocationOnScreen();
    	} 
        this.list = list;
        if (normal==null){
        	normal=list.getFont();
        	title=new Font(normal.getName(),Font.PLAIN,normal.getSize()+2);
        	fg=list.getForeground();
        	bg=list.getBackground();
        	sfg=list.getSelectionForeground();
        	sbg=list.getSelectionBackground();
        }
    	boolean hasHighlighting=false;
        String str;
        final boolean hasUnselectable=!Basics.isEmpty(unselectableItems);
        final boolean isUnselectable=unselectableItems==null? false:unselectableItems.contains(value);
        final boolean needsMargin=hasUnselectable && !isUnselectable;
        final boolean isForbidden=forbidden==null? false:forbidden.contains(value);
      			label.setBorder(STANDARD_BORDER_INSETS);
				final boolean hasImage;
				if (value == null) {
					str = "";
					hasImage = false;
				} else {
					str = value.toString();
					hasImage = str.contains("<img src='");
					if (!isUnselectable && index == list.getSelectedIndex()) {
						final String userTyped = ksm.getStartingText();
						if (!Basics.isEmpty(ksm.getStartingText())) {
							if (!str.startsWith("<html>")) {
								final String searchable = str;
								if (userTyped != null
										&& searchable.toLowerCase().startsWith(
												userTyped.toLowerCase())) {
									sb.setLength(0);
									sb.append("<html><b><font color='yellow'>");
									if (needsMargin) {
										sb.append("&nbsp;&nbsp;&nbsp;");
									}
									sb.append(searchable.substring(0,
											userTyped.length()));
									sb.append("</font></b>");
									sb.append(searchable.substring(userTyped
											.length()));
									sb.append("</html>");
									str = sb.toString();
									hasHighlighting = true;
								}
							} else {
								final String searchable = Basics
										.stripSimpleHtml(str).trim();
								if (userTyped != null
										&& searchable.toLowerCase().startsWith(
												userTyped.toLowerCase())) {
									sb.setLength(0);
									sb.append("<html><b><font color='yellow'>");
									sb.append(searchable.substring(0,
											userTyped.length()));
									sb.append("</font></b>");
									sb.append(searchable.substring(userTyped
											.length()));
									sb.append("</html>");
									str = sb.toString();
									hasHighlighting = true;
								}
							}
						}
					}
					if (ac != null
							&& list.isShowing()
							&& (index >= ac.popupFirstVisibleIndex && index <= ac.popupLastVisibleIndex)
							&& str.indexOf("[") != -1
							&& !str.startsWith("<html>")) {
						sb.setLength(0);
						sb.append("<html>");
						sb.append("&nbsp;&nbsp;&nbsp;");
						sb.append(str);
						sb.append("</html>");
						str = sb.toString();
					}

					if (needsMargin && !hasHighlighting
							&& !str.startsWith("<html>")) {
						sb.setLength(0);
						sb.append("   ");
						sb.append(str);
						str = sb.toString();
					}
				}
				boolean isHtml = str.startsWith("<html>");
				label.setFont(normal);
				if (isSelected) {
					selected=value;
					selectedIndex=index;
					if (ac != null) {
						_updateUI(ac);
					}
					if (isForbidden) {
						if (hasImage && isHtml) {
							label.setBackground(sbg);
							label.setForeground(com.MeehanMetaSpace.swing.PersonalizableTable
									.dither(sbg, 10));
						} else {
							label.setBackground(Color.LIGHT_GRAY);
							label.setForeground(Color.red);
						}
					} else if (!isUnselectable) {
						label.setBackground(sbg);
						label.setForeground(sfg);
					} else {
						label.setFont(title);
						if (!isHtml) {
							isHtml = true;
							str = Basics.concat(
									"<html><b><font color='green'>", str,
									"</b></html>");
						}
						label.setForeground(Color.green);
						label.setBackground(bg);
					}					
				} else {
					if (isForbidden) {
						label.setBackground(bg);
						label.setForeground(hasImage && isHtml ? SystemColor.black
								: SystemColor.textInactiveText);
					} else if (!isUnselectable) {
						label.setBackground(bg);
						label.setForeground(fg);
					} else {
						label.setFont(title);
						if (!isHtml) {
							isHtml = true;
							str = Basics.concat(
									"<html><i><font color='green'>", str,
									"</i></html>");
						}
						label.setForeground(SystemColor.green);
						label.setBackground(bg);
					}
				}
				if (!isHtml && str.contains(IMG_CUE)) {
					StringBuilder leadingSpaces = new StringBuilder();
					final char[] l = str.toCharArray();
					for (int i = 0; i < l.length; i++) {
						if (l[i] == ' ') {
							leadingSpaces.append("&nbsp;");
						} else {
							break;
						}
					}
					label.setText(Basics.concat("<html>",
							leadingSpaces.toString(), str, "</html>"));
				} else {
					label.setText(str);
				}
				if (callBack != null) {
					callBack.decorate(label,
							hasHighlighting ? "   " + value.toString() : str,
							str.indexOf(IMG_CUE));
				}
				//label.setText(Basics
					//	.stripSimpleHtml(label.getText()).trim());//TODO FIXME - Check the need of this
				return label;
			
		}
    
    }
    
    
    private static final class SubListRenderer implements ListCellRenderer {
        private StartingTextProvider ksm;
        Object selected;
        int selectedIndex;
        private AutoComplete ac;
        private JLabel rightLabel= new JLabel(" ");
       /* private JLabel middleLabel= new JLabel(" ");*/
        private JLabel leftLabel= new JLabel(" ");
        private JPanel panel= new JPanel();
        private  JList list;
        private  Font normal=null,title=null;
        private  Color bg,fg,sbg,sfg;
        private  Collection unselectableItems, forbidden;
        SubListRenderer(final StartingTextProvider ksm, AutoComplete ac) {
			this.ksm = ksm;
			this.ac = ac;
			panel.setOpaque(true);
			if (!expandNormallyPlease()){
				panel.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
			}
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel.add(leftLabel);
			panel.add(rightLabel);
			if (expandNormallyPlease()){
				rightLabel.setHorizontalTextPosition(JLabel.LEFT);
			} else {
				rightLabel.setHorizontalTextPosition(JLabel.LEFT);
			}
			leftLabel.setHorizontalTextPosition(JLabel.LEFT);
			//leftLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),BorderFactory.createEmptyBorder(0,11,0,0)));//TODO FIXME Check the need of this
			//rightLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),BorderFactory.createEmptyBorder(0,11,0,0)));
        }
        final StringBuilder sb=new StringBuilder();
  
    private class MouseHandler extends MouseAdapter {
    	private final JList list;
    	MouseHandler(final JList list){
    		this.list=list;
    		this.prior=list.getMouseListeners();
    		for (final MouseListener l:prior){
    			list.removeMouseListener(l);
    		}
    		list.addMouseListener(this);
    		list.addMouseMotionListener(this);
    	}
    	private final MouseListener []prior;
        public void mouseReleased(final MouseEvent e) {
        	final Point p1=e.getPoint();    	    	
	    	if (p1.x<MARGIN_FOR_EXPANSION_ICON){
		    	ac.mouseClickGesturedSubListExpansion=true;
	    		if (ac.itemsShowingSubList.size()==0){
	    			ac.itemsShowingSubList.add(ac.itemCurrentlyHighlighted);
	    			ac.showSubList(false);
	    		} else {
	    			if(!ac.itemsShowingSubList.contains(ac.itemCurrentlyHighlighted)){
	    				ac.showSubList(false);
	    				ac.itemsShowingSubList.add(ac.itemCurrentlyHighlighted);
	    			}
	    			else{
	    				ac.subListStartIndex = ac.indexCurrentlyHighlighted+1;
	    				ac.itemCurrentlySubListed = ac.itemCurrentlyHighlighted;
	    				ac.subItems=ac.subListManager.getSubItems(ac.itemCurrentlyHighlighted);
	    				ac.clearSubList(false);	    			
	    			}
	    		}		    	
		    	ac.expanded=!ac.expanded;
	    	}
	    	else if (ac.itemCurrentlyHighlighted.indexOf(ac.subListManager.getDisplayValue()) != -1) {
	    		return;
	    	}
	    	else {
	    		ac.mouseClickGesturedSubListExpansion=false;
	    		delegateMouseReleaseToOriginalListeners(e);
	    	}
        }

        private void delegateMouseReleaseToOriginalListeners(final MouseEvent e){
        	for (final MouseListener l:prior){
        		l.mouseReleased(e);
        	}
        }
	    public void mouseMoved(MouseEvent e){
	    	final Point p1=e.getPoint();
    		final int idx=list.locationToIndex(p1);
	    	if (p1.x<MARGIN_FOR_EXPANSION_ICON){
	    		if (!ac.mouseClickGesturedSubListExpansion && 
	    			(ac.subListSize==0 || idx<ac.subListStartIndex || idx>=ac.subListStartIndex+ac.subListSize)){
	    			ac.mouseClickGesturedSubListExpansion=true;
	    			list.setToolTipText(ac.subListManager.getNormalClickToolTip(idx==ac.subListStartIndex-1));
	    			ToolTipOnDemand.getSingleton().show(list, false, MARGIN_FOR_EXPANSION_ICON, p1.y, false, false);
	    		}
	    	}else if (ac.mouseClickGesturedSubListExpansion){
	    		ac.mouseClickGesturedSubListExpansion=false; 
	    		list.setToolTipText(null);
	    		ToolTipOnDemand.getSingleton().hideTipWindow();
	    	}
	    }

	};
    private static int MARGIN_FOR_EXPANSION_ICON=40;
    private final Icon 
    		iconActiveSelected, iconActive,
    		iconSelected, icon;
    {
    	if (SwingBasics.isPageSoftLookAndFeel()){
    		iconActiveSelected=MmsIcons.getMenuItemArrowActiveSelectedPageSoftIcon();
    		iconActive=MmsIcons.getMenuItemArrowActivePageSoftIcon();
        	iconSelected=MmsIcons.getMenuItemArrowSelectedPageSoftIcon();
        	icon=MmsIcons.getMenuItemArrowPageSoftIcon();
    	} else {
    		iconActiveSelected=MmsIcons.getMenuItemArrowActiveSelectedIcon();
    		iconActive=MmsIcons.getMenuItemArrowActiveIcon();
        	iconSelected=MmsIcons.getMenuItemArrowSelectedIcon();
        	icon=MmsIcons.getMenuItemArrowIcon();
    	}
    }
	private boolean mouseInstalled=false;
    public Component getListCellRendererComponent(
      final JList list,
      final Object value,
      final int index,
      final boolean isSelected,
      boolean cellHasFocus) {
    	if (expandNormallyPlease()){
    		if (!mouseInstalled){
    			MouseHandler ml=new MouseHandler(list);        	        	
    			mouseInstalled=true;
    		}
    	}

    	if (list.isShowing()){
    		lastPopupLocation=list.getParent().getLocationOnScreen();
    	} 
        this.list = list;
        if (normal==null){
        	normal=list.getFont();
        	title=new Font(normal.getName(),Font.PLAIN,normal.getSize()+(expandNormallyPlease()?0:2));
        	fg=Color.WHITE;
        	bg=new Color(162, 210, 245);
        	sfg=list.getSelectionForeground();
        	sbg=list.getSelectionBackground();
        }
    	boolean hasHighlighting=false;
        String str;
        final boolean hasUnselectable=!Basics.isEmpty(unselectableItems);
        final boolean isUnselectable=unselectableItems==null? false:unselectableItems.contains(value);
        final boolean needsMargin=hasUnselectable && !isUnselectable;
        final boolean isForbidden=forbidden==null? false:forbidden.contains(value);
        
				if (!expandNormallyPlease()){
					panel.setBorder(STANDARD_BORDER_INSETS);
				}
				final boolean hasImage;
				String string = value == null?"":value.toString();
				if (value == null) {
					str = "";
					hasImage = false;
				} else {
					str = value.toString();
					hasImage = str.contains("<img src='");
					if (!isUnselectable && index == list.getSelectedIndex()) {
						final String userTyped = ksm.getStartingText();
						if (!Basics.isEmpty(ksm.getStartingText())) {
							if (!str.startsWith("<html>")) {
								final String searchable = str;
								if (userTyped != null
										&& searchable.toLowerCase().startsWith(
												userTyped.toLowerCase())) {
									sb.setLength(0);
									sb.append("<html><b><font color='yellow'>");
									if (needsMargin) {
										sb.append("&nbsp;&nbsp;&nbsp;");
									}
									sb.append(searchable.substring(0,
											userTyped.length()));
									sb.append("</font></b>");
									sb.append(searchable.substring(userTyped
											.length()));
									sb.append("</html>");
									str = sb.toString();
									hasHighlighting = true;
								}
							} else {
								if(!str.startsWith(ac.SUBLISTTEXT_PREFIX)){
								final String searchable = Basics
										.stripSimpleHtml(str).trim();
								if (userTyped != null
										&& searchable.toLowerCase().startsWith(
												userTyped.toLowerCase())) {
									sb.setLength(0);
									sb.append("<html><b><font color='yellow'>");
									if(str.contains(ac.SUBSUBLISTTEXT_PREFIX)){
										sb.append(ac.SUBSUBLISTTEXT_PREFIX).append(ac.EXTRA_LEADING_SPACES).append("&nbsp;");
									}
									sb.append(searchable.substring(0,
											userTyped.length()));
									sb.append("</font></b>");
									sb.append(searchable.substring(userTyped
											.length()));
									sb.append("</html>");
									str = sb.toString();
									hasHighlighting = true;
								}
							}
						}
					}
					}
					if (ac != null
							&& list.isShowing()
							&& (index >= ac.popupFirstVisibleIndex && index <= ac.popupLastVisibleIndex)
							&& str.indexOf("[") != -1
							&& !str.startsWith("<html>")) {
						sb.setLength(0);
						sb.append("<html>");
						sb.append("&nbsp;&nbsp;&nbsp;");
						sb.append(str);
						sb.append("</html>");
						str = sb.toString();
						str = str.replaceAll(Pattern.quote("["),
						"<sup>&nbsp;").replaceAll(Pattern.quote("]"),
						"&nbsp;</sup>");
					}
					if (needsMargin && !hasHighlighting
							&& !str.startsWith("<html>")) {
						sb.setLength(0);
						sb.append("   ");
						sb.append(str);
						str = sb.toString();
					}
				}				
				boolean isHtml = str.startsWith("<html>");
				boolean bigger=isHtml?str.length()>90:str.length()>30;
				boolean yellow=str.indexOf("yellow")!=-1?true:false;
				if (bigger) {					
					if (isHtml) {
						str=PopupBasics.wrap(str, yellow?85:60, "<br>&nbsp;&nbsp;&nbsp;",false);
					} else {
						str="<html>&nbsp;&nbsp;" + PopupBasics.wrap(str, yellow?60:30, "<br>&nbsp;&nbsp;&nbsp;",false) + "</html>";
					}
				}
				panel.setFont(normal);
				String strippedText = (String) value;
				if (strippedText != null && strippedText.contains(ac.subListManager.getSubItemHeader())) {
					strippedText=Basics.stripSimpleHtml(strippedText).trim();
				}
				if (isSelected && strippedText.contains(ac.subListManager.getDisplayValue())) {
					strippedText=Basics.stripSimpleHtml(strippedText).trim();
					ac.subListManager.setCurrentItem(strippedText.substring(0,strippedText.indexOf(ac.subListManager.getDisplayValue())));
				}
				
					if (strippedText != null && ((!strippedText.contains(ac.SUBLISTTEXT_PREFIX) && ac.subListManager.subItemExists(Basics.stripSimpleHtml(strippedText))) || strippedText.contains(ac.subListManager.getSubItemHeader()))) {
						rightLabel.setIconTextGap(1);
						if (!expandNormallyPlease()){
							final int was = ToolTipManager.sharedInstance()
								.getInitialDelay();
							ToolTipManager.sharedInstance().setInitialDelay(0);
							panel.setToolTipText("To see sub list press ctrl key or hover with the mouse.");
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									ToolTipManager.sharedInstance()
										.setInitialDelay(was);
								}
							});
						}
						int lindex = str.lastIndexOf("</html>");
						int lbraceindex = str.lastIndexOf("[");
						if (lindex != -1) {
							str = Basics.concat(str.substring(0, lindex),
									"&nbsp;", str.substring(lindex));
							if (lbraceindex != -1) {
								str = str.replaceAll(Pattern.quote("["),
										"<sup>&nbsp;").replaceAll(Pattern.quote("]"),
										"&nbsp;</sup>");
							}
						} else {
							str += " ";
						}
					} else {
						rightLabel.setToolTipText(null);
						rightLabel.setIconTextGap(0);
						rightLabel.setIcon(null);
					}
					if (isSelected) {
						ac.itemCurrentlyHighlighted = strippedText;
						ac.indexCurrentlyHighlighted = list.getSelectedIndex();
					}
				if (isSelected) {
					selected=value;
					selectedIndex=index;
					if (ac != null) {
						_updateUI(ac);
					}					
					if (isForbidden) {
						if (hasImage && isHtml) {
							panel.setBackground(sbg);
							panel.setForeground(com.MeehanMetaSpace.swing.PersonalizableTable
									.dither(sbg, 10));
						} else {
							panel.setBackground(Color.LIGHT_GRAY);
							panel.setForeground(Color.red);
						}
					} else if (!isUnselectable) {
						panel.setBackground(sbg);
						panel.setForeground(sfg);
					} else {
						panel.setFont(title);
						if (!isHtml) {
							isHtml = true;
							str = Basics.concat(
									"<html><b><font color='green'>", str,
									"</b></html>");
						}
						panel.setForeground(Color.green);
						panel.setBackground(bg);
					}					
				} else {
					if (isForbidden) {
						panel.setBackground(bg);
						panel.setForeground(hasImage && isHtml ? SystemColor.black
								: SystemColor.textInactiveText);
					} else if (!isUnselectable) {
						panel.setBackground(bg);
						panel.setForeground(fg);
					} else {
						panel.setFont(title);
						if (!isHtml) {
							isHtml = true;
							str = Basics.concat(
									"<html><i><font color='green'>", str,
									"</i></html>");
						}
						panel.setForeground(SystemColor.green);
						panel.setBackground(bg);
					}
				}
				if (!isHtml && str.contains(IMG_CUE)) {
					StringBuilder leadingSpaces = new StringBuilder();
					final char[] l = str.toCharArray();
					for (int i = 0; i < l.length; i++) {
						if (l[i] == ' ') {
							leadingSpaces.append("&nbsp;");
						} else {
							break;
						}
					}
					rightLabel.setText(Basics.concat("<html>",
							leadingSpaces.toString(), str, "</html>"));
				} else {
					String str1=str;
					if (str.contains(ac.subListManager.getDisplayValue())) {
						str1=str1.substring(0,str1.indexOf(ac.subListManager.getDisplayValue()));
						final int idx=str1.indexOf(Basics.stripSimpleHtml(strippedText).trim().substring(0,2));
						if (idx>-1){
							str1=str1.substring(idx);
							str1=str.replaceAll(str1,"");
						}
					}
					//str1="<html>&nbsp;&nbsp;" + str1+"</html>";
					rightLabel.setText(str1);
				}
				if (callBack != null) {
					callBack.decorate(panel,
							hasHighlighting ? "   " + value.toString() : str,
							str.indexOf(IMG_CUE));
				}
				if(expandNormallyPlease()){
					/*if(ac.favoriteItemManager!=null){
						middleLabel.setIcon(ac.favoriteItemManager.getFavoriteIcon(Basics.stripSimpleHtml(strippedText)));		
					}else{
						middleLabel.setIcon(null);
						}		*/			
					if(strippedText == null || !ac.subListManager.subItemExists(Basics.stripSimpleHtml(strippedText))){
						leftLabel.setIcon(null);
						leftLabel.setText(ac.SUBLISTTEXT_PREFIX+"&nbsp;&nbsp;"+ac.SUBLISTTEXT_SUFFIX);
					}else{
						leftLabel.setText("");
						if (ac.itemsShowingSubList.contains(strippedText)){
							if (isSelected){
								leftLabel.setIcon( iconActiveSelected);
								ac.expanded=true;
							} else {
								leftLabel.setIcon( iconActive);									
							}
						}else{
							if (isSelected){
								leftLabel.setIcon( iconSelected);
								ac.expanded=false;
							}else{
								leftLabel.setIcon( icon);
							}
						}					
					}
				}
				
				if (rightLabel.getText().startsWith(ac.SUBSUBLISTTEXT_PREFIX)) {
					leftLabel.setText(ac.SUBSUBLISTTEXT_PREFIX+ac.SUBLISTTEXT_SUFFIX);
					leftLabel.setIcon(null); // remove this line if want the status icon to be displayed for sub list
				} else if (rightLabel.getText().startsWith(ac.SUBLISTTEXT_PREFIX)) {
					leftLabel.setText(ac.SUBLISTTEXT_PREFIX+ac.SUBLISTTEXT_SUFFIX);
					leftLabel.setIcon(null);
				} else {
					if (expandNormallyPlease()){
						rightLabel.setIcon(ac.subListManager.getStatusIcon(string)); 
					} else {
						leftLabel.setIcon(ac.subListManager.getStatusIcon(string));
					}
				}
				if (strippedText != null && 
						strippedText.contains(ac.subListManager.getDisplayValue())) {
					if (ac.itemsShowingSubList.contains(strippedText)){
						if (isSelected){
							leftLabel.setIcon( iconActiveSelected);
							ac.expanded=true;
						} else {
							leftLabel.setIcon( iconActive);									
						}
					}else{
						if (isSelected){
							leftLabel.setIcon( iconSelected);
							ac.expanded=false;
						}else{
							leftLabel.setIcon( icon);
						}
					}
				}
				return panel;
		}
    }
    
    static void _updateUI(final JComboBox jcb) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (jcb.getRenderer() instanceof AutoComplete.Renderer) {
                        final AutoComplete.Renderer r = (AutoComplete.Renderer) jcb.getRenderer();
                        if (r.list != null) {
                            r.list.invalidate();
                            r.list.repaint();
                        }
                    }
                }
            });
        }
    
    private boolean writeOnly;
    
    public void setWriteOnly(final boolean writeOnly) {
    	this.writeOnly=writeOnly;
    }
    
    static boolean isFinishedWithComboBox(final ActionEvent e) {
    	final int modifiers = e.getModifiers();
		final String cmd=e.getActionCommand();
        // do not stop editing if searching with keyboard!!!
		//System.out.println("return mod="+(modifiers & InputEvent.BUTTON1_MASK)+ " AND cmd="+cmd);
		return (modifiers & InputEvent.BUTTON1_MASK) != 0 || cmd.equals("comboBoxEdited");	
	    		
    }
    
    Collection unselectableItems;
    
    public void setUnselectable(final Collection unselectableItems) {
    	this.unselectableItems=unselectableItems==null?Basics.UNMODIFIABLE_EMPTY_LIST:unselectableItems;
    	if (this.renderer instanceof SubListRenderer) {
    		((SubListRenderer)renderer).unselectableItems=unselectableItems;
    	}
    	else if (this.renderer instanceof Renderer) {
    		((Renderer)renderer).unselectableItems=unselectableItems;
    	}
    }

    private JButton defaultButton;
    private JRootPane rootPane;
    
	public void ignoreDefaultButtonWhenFocussed() {
		addEditorFocusListener(new FocusAdapter() {
			public void focusGained(final FocusEvent fe) {
				if (rootPane == null) {
					final JRootPane rp = SwingUtilities.getRootPane(AutoComplete.this);
					if (rp != null) {
						rootPane=rp;
						defaultButton = rp.getDefaultButton();
						rp.setDefaultButton(null);
					}
				} else {
					rootPane.setDefaultButton(null);
				}
			}

			public void focusLost(final FocusEvent fe) {
				if (rootPane != null) {
					rootPane.setDefaultButton(defaultButton);					
					
				}
			}

		});
	}
	
	private void respectDefaultButton() {
		if (isPopupVisible()) {
			int i=0;
            //setPopupVisible(false);
        } else {
            // Call the default button binding.
            // This is a pretty messy way of passing an event through
            // to the root pane.
            JRootPane root = SwingUtilities.getRootPane(this);
            if (root != null) {
            	final JButton b=root.getDefaultButton();
            	if (b != null) {
            		b.doClick();
            	}
            }
        }
	}
	
	
	private Collection forbidden;
	public void setForbidden(final Collection forbidden){
		this.forbidden=forbidden;
		if (this.renderer instanceof SubListRenderer) {
    		((SubListRenderer)renderer).forbidden=forbidden;
    	}
    	else if (this.renderer instanceof Renderer) {
    		((Renderer)renderer).forbidden=forbidden;
    	}
	}

	private boolean isForbiddenFruit(final Object item){
		boolean isForbidden=false;
		if (item != null && !Basics.isEmpty(forbidden)){
			isForbidden=forbidden.contains(item);
		}
		return isForbidden;
	}
	public boolean contains(final String o){
		if (editor != null){
			((SearchEditor) editor).handlePendingItems();
			final Object txt=((SearchEditor)editor).get(o);
			return !Basics.isEmpty(txt);
		}
		return false;
	}

	private final static boolean SUPPORT_SEQUENTIAL_NON_JUMPY_MATCHING=true;
	private final ComboBoxSearcher sequentialMatcher;
	public static Object lastForbiddenCellValue;
	public interface CallBack{
		Component getNotFoundComponent();
		String getNotFoundText(String editorText);
		String getPopupVisibleToolTipText();
		Component getPopupVisibleComponent(AutoComplete ac);
		String getText(String text);
		int getPopupHeightOffset();
		int getPopupWidthOffset();
		void decorate(Component component, String value, int imgCueIndex);
	}
	
	public static CallBack callBack;

	public static String encodeImgsWithOneSpaceBetween(final Object ... args){
		final StringBuilder sb=new StringBuilder("&nbsp;");
		for (final Object url:args){
			sb.append("&nbsp;<img src='");
			if (url instanceof String){
				sb.append(MmsIcons.getURLText((String)url));
			} else {
				sb.append(url);
			}
			sb.append("'>&nbsp;");
		}
		return sb.toString();
	}

	public static String IMG_CUE="&nbsp;&nbsp;<img src='";
	
	public static String encodeImg(final URL url){
		return Basics.concatObjects(IMG_CUE,url,"'>&nbsp;&nbsp;");
	}

	public static String encodeImg(final String file){
		return encodeImg(MmsIcons.getURL(file));
	}

	private RowForm rowForm;
	void setRowForm(final RowForm rowForm){
		this.rowForm=rowForm;
	}

	void showPopupAgainIfForbiddenOnRowForm(final ActionEvent e) {
		if (rowForm != null && AutoComplete.isFinishedWithComboBox(e)) {
			final Object o = getFinalValue();
			if (AutoComplete.lastForbiddenCellValue == null) {
				rowForm.nextFocus();
			} else {
				rowForm.refresh();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFocus();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								showPopup();
							}
						});
					}
				});
			}
		}
	}

	public void showPopup_IF_onRowForm() {
		if (rowForm != null) {
			final Object o = getFinalValue();
			rowForm.refresh();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					requestFocus();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showPopup();
						}
					});
				}
			});

		}
}
	private static boolean haveNextKeyHideToolTip=false;
	private static int priorKeyCnt,keycnt=0;

	private boolean explicitlySelected(final ActionEvent e){
		if (editor instanceof SearchEditor){
    		final SearchEditor _editor=(SearchEditor)editor;
			if ( _editor.returnPressed ){
				return true;
			}
    	}
		return isFinishedWithComboBox(e); // don't know
	}
	public boolean selectedByPressingReturn(){
		if (!isPopupVisible()){
	    	if (editor instanceof SearchEditor){
	    		final SearchEditor e=(SearchEditor)editor;
				return e.returnPressed;
	    	}
		}
		return false;
	}

	private Object performedActionFor=null;
	private void fireActionEventIfQuaQuaAndTheSameObject(){
		if (SwingBasics.isQuaQuaLookAndFeel()){
			//if (performedActionFor == null){ 
				if (!Basics.equals(performedActionFor, getSelectedItem())){
					fireActionEvent();
				}
			//}
		}
	}
	public enum SubListExpandStyle{
		CUSTOM_HOVERING_AND_CTRL_KEY_IDIOM,
		NORMAL_TREE_CLICK_IDIOM
	}		
	
	public static SubListExpandStyle subListExpandPreference=SubListExpandStyle.NORMAL_TREE_CLICK_IDIOM;

	
	private static boolean expandNormallyPlease(){
		return subListExpandPreference==SubListExpandStyle.NORMAL_TREE_CLICK_IDIOM;
	}

	private boolean mouseClickGesturedSubListExpansion=false;
    boolean ignoreTheDefaultButton=false;

}


