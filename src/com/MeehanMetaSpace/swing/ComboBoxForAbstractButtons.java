package com.MeehanMetaSpace.swing;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.swing.ComboCellEditor.Manager;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class ComboBoxForAbstractButtons extends JComboBox
{
	private boolean isPopupVisible=false;
	
	private static final long serialVersionUID = 4203828308464744914L;
	public ComboBoxForAbstractButtons(final Vector<AbstractButton> items, final String label, final Icon icon) {
		super(items);
		this.useLabel=true;
		init();
		setHint(label, icon);
		super.setMaximumRowCount(15);
	}
	
	public void addHint(){
		if (useLabel){
			addItem(new JSeparator());
			addItem(hint);
			setSelectedItem(hint);
		}
	}
   public ComboBoxForAbstractButtons(final AbstractButton[] items, final boolean useLabel) {
      super(items);
      this.useLabel = useLabel;
      init();
   }
   JCheckBox[] items = null;
   public ComboBoxForAbstractButtons(final JCheckBox[] items,final boolean useLabel, final String label) {
      super(items);
      this.items=items;
      for (final JCheckBox item: items) {
    	  item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setHint(hint.getText() + item.getText());
			}
		});
      }
      this.useLabel = useLabel;
      this.label=label;
      init();
   }
   ComboCellEditor editor;
   public void setComboCellEditor(ComboCellEditor editor) {
	   this.editor = editor;
   }

   public ComboBoxForAbstractButtons(Vector<AbstractButton> items, final boolean useLabel) {
      super(items);
      this.useLabel = useLabel;
      init();
   }

   public void addItem(Object anObject) {
	   if (anObject instanceof JComponent){
		   SwingBasics.addMouseOver((JComponent)anObject);
	   }
	   super.addItem(anObject);
   }

   public ComboBoxForAbstractButtons(ComboBoxModel aModel, final boolean useLabel) {
      super(aModel);
      this.useLabel = useLabel;
      init();
   }

   public boolean ignoreAction=false;
   private boolean useLabel=false;
   private String label=null;
   private final JLabel hint = new JLabel(), hintWhenPoppedUp=new JLabel();
   public void setHint(final String txt, final Icon icon){
	   
		   hint.setIcon(icon);
		   hintWhenPoppedUp.setIcon(icon);
		   hint.setIconTextGap(4);
		   hintWhenPoppedUp.setIconTextGap(4);
		   hint.setHorizontalTextPosition(SwingConstants.RIGHT);                
		   hintWhenPoppedUp.setHorizontalTextPosition(SwingConstants.RIGHT);                
		   hint.setHorizontalAlignment(SwingConstants.CENTER);                
		   hintWhenPoppedUp.setHorizontalAlignment(SwingConstants.CENTER);      
	  
	   setHint(txt);
	   hintWhenPoppedUp.setHorizontalAlignment(JLabel.CENTER);
   }

   public void setHint(final String txt){
	   if (txt.startsWith("<html>")){
		   hintWhenPoppedUp.setText(Basics.concat("<html><small>", Basics.stripHtmlHeaderTagsOnly(txt), "</small></html>"));
		   if (SwingBasics.isWindowTooSmall && SwingBasics.isMac){
			   hint.setText(Basics.concat("<html><small>", Basics.stripHtmlHeaderTagsOnly(txt), "</small></html>"));
		   } else {
			   hint.setText(txt);	
		   }
	   } else {
		   hintWhenPoppedUp.setText(
				   Basics.concat("<html><small>", txt, "</small></html>"));
		   if (SwingBasics.isWindowTooSmall && SwingBasics.isMac){
			   hint.setText(
					   Basics.concat("<html><small>", txt, "</small></html>"));
			      
		   } else {
				hint.setText(txt);
		   }
	   }	   
   }
   
   public void setMnemonic(final JComponent cmp, final int c){
	  // hint.setMnemonic(c);
	   cmp.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				requestFocus();
				showPopup();
			}
		}, KeyStroke.getKeyStroke(c, InputEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
   }
   private final boolean isMac=SwingBasics.isQuaQuaLookAndFeel();
   public Dimension getPreferredSize(){
	   final Dimension d=super.getPreferredSize();
	   final Dimension d2=hint.getPreferredSize();
	   d.width=d2.width+(isMac?(SwingBasics.isWindowTooSmall?45:25):40);
	   return d;
   }
   
   public Collection pendingItems;
   private void init() {
	   addPopupMenuListener(new PopupMenuListener() {
		
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			isPopupVisible=true;
		}
		
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			isPopupVisible=false;			
		}
		
		public void popupMenuCanceled(PopupMenuEvent e) {			
			
		}
	});
	  if (useLabel) {
		  if(label==null){
			  setHint("Options",MmsIcons.getPreferencesIcon());
			  }
		  else{
			  setHint(label,MmsIcons.getPreferencesIcon());
		  }
		  addHint();
	  }
      setRenderer(new ComboBoxRenderer());
      addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
             if (!ignoreAction){
                 itemSelected();
             }
             if (useLabel) {
           	  setSelectedIndex(getItemCount()-1);   
             }
         }
      });
      if (!SwingBasics.isMac){
    	  new ResizedComboBoxUI(this);
      }
      final Manager ksm = new Manager(400, this, false, null);
      setKeySelectionManager(ksm);
      final int n=getItemCount();
      for (int i=0;i<n;i++){
    	  Object o=getItemAt(i);
    	  if (o instanceof JComponent){
    		  SwingBasics.addMouseOver((JComponent)o);
    	  }
      }
   }

   private void itemSelected() {
      if (getSelectedItem() instanceof AbstractButton) {
         final AbstractButton jcb = (AbstractButton)getSelectedItem();
         jcb.doClick();
      }
   }

   private class ComboBoxRenderer implements ListCellRenderer {
      private JLabel label;
      private Color _bg=UIManager.getColor("List.background"), _bgSel=UIManager.getColor("List.selectionBackground"), _bgFor=UIManager.getColor("List.selectionForeground");
      
      public ComboBoxRenderer() {
         setOpaque(true);
      }

      public Component getListCellRendererComponent(
    		  final JList list, 
    		  final Object value, 
    		  final int index,
    		  final boolean isSelected, 
    		  final boolean cellHasFocus) {
         if (value == hint && ComboBoxForAbstractButtons.this.isVisible() && isPopupVisible){
        	 hintWhenPoppedUp.setForeground(SystemColor.textInactiveText);
        	 hintWhenPoppedUp.setBackground(_bg);
        	 return hintWhenPoppedUp;
         }
    	  if (value instanceof Component) {
            Component c = (Component)value;
            if (list != null){
            	Color fg=null;
            	if ( c instanceof PersonalizableTableModel.GroupMenu){
                	fg=((PersonalizableTableModel.GroupMenu)c).getGroupForeground();
            	}
                if (isSelected) {
                	c.setBackground(_bgSel);
                	c.setForeground(_bgFor);
                	
                } else {
               		c.setBackground(_bg);
                	if (fg==null){
                		c.setForeground(list.getForeground());
                	} else {
                		c.setForeground(fg);
                	}
                	
                }
            }
            return c;
         } else if (value != null) {
             if (label ==null) {
                 label = new JLabel(value.toString());
              }
              else {
                 label.setText(value.toString());
              }

           }
           else {
          	 if (label ==null) {
                   label = new JLabel("");
                }
                else {
                   label.setText("");
                }
           }
           return label;
      }
   }

   public String getToolTipText() {
       final Object o=getSelectedItem();
       if (o instanceof JComponent){
           return ((JComponent)o).getToolTipText();
       }
       return null;
   }
   
   public static void main(final String []args){
       try {
           UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel().getClass().getName());
       } catch (Exception ex) {
           System.err.println("Could not load the look & feel");
       }

//	   SwingBasics.doNativeLnF();
	   SwingBasics.resetDefaultFonts();
	   JCheckBox item=new JCheckBox("Becton Dickenson Laboratories");
	   final Vector<AbstractButton>c=new Vector<AbstractButton>();
	   c.add(item);
		c.add(new JMenuItem("Define rules", MmsIcons.getEditIcon()));

	   item=new JCheckBox("Invitrogen Laboratories");
	   c.add(item);
	   item=new JCheckBox("eBioScience Ltd.");
	   c.add(item);
	   final ComboBoxForAbstractButtons cb=new ComboBoxForAbstractButtons(c, true);
	   final JPanel jp=new JPanel();
	   cb.setHint("All sorts of ..", MmsIcons.getWeatherCloudyIcon());
	   final JDialog dlg=new JDialog();
	   dlg.getContentPane().add(jp);
	   cb.pendingItems=new ArrayList();
	   cb.addFocusListener(new FocusAdapter() {
		    public void focusGained(FocusEvent e) {
		    	
		    	System.out.println("yikes");
		    }

	});
	   cb.pendingItems.add(new JCheckBox("Tony"));
	   cb.setMnemonic(jp, KeyEvent.VK_O);
	   final JTextField jt=new JTextField(15);
	   jt.addFocusListener(new FocusListener() {
		
		public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void focusGained(FocusEvent e) {
			System.out.println("Ah hah again");
		}
	});
	   jp.add(jt);
	   jp.add(cb);
	   dlg.pack();
	   dlg.setVisible(true);
   }
   
   public void setFocusable(boolean focusable) {
	   super.setFocusable(true);
   }
   public synchronized void addFocusListener(FocusListener l) {
	   super.addFocusListener(l);
   }
   
   protected void processFocusEvent(FocusEvent e) {
	   super.processFocusEvent(e);
   }

   public void setRequestFocusEnabled(boolean requestFocusEnabled) {
	   super.setRequestFocusEnabled(true);
   }
   
   public static TableCellEditor getTableCellEditor(final Collection values, final Object currentValue) {
       class _ComboCellEditor extends ComboCellEditor {
       	private boolean isAdjustingAddable=false;
       	protected boolean shouldStopEditing(final JComboBox jcb) {
       		return true;
       	}
           _ComboCellEditor(final JComboBox jcb) {
               super(jcb);                

           }

           boolean init = false;

           public boolean isCellEditable(final EventObject evt) {

               final boolean ok = super.isCellEditable(evt);
               return ok;
           }
       };

       final JCheckBox[]c=new JCheckBox[values.size()];
       final StringTokenizer st = new StringTokenizer((String)currentValue, "+");
       final ArrayList<String> allvalues = new ArrayList<String>();
       while (st.hasMoreTokens()) {
    	   allvalues.add(st.nextToken());
       }
       int i=0;
       String existing = "";
       for (Object v: values) {
    	   String itemText = v.toString();
    	   final StringTokenizer st2 = new StringTokenizer((String)itemText, "*");
    	   JCheckBox item=new JCheckBox(st2.nextToken());
    	   if (st2.hasMoreTokens()) {
    		   item.setToolTipText(st2.nextToken());
    	   }
    	   if (allvalues.contains(itemText)) {
    		   if (!existing.isEmpty()) {
    			   existing+="+";
    		   }
    		   existing+=itemText;
    		   item.setSelected(true);
    	   }
    	   c[i++]=item;
       }
	   final ComboBoxForAbstractButtons cb=new ComboBoxForAbstractButtons(c, true, null);
	   
	   _ComboCellEditor _cmb = new _ComboCellEditor(cb);
	   
	   ComboCellEditor.SelectionManager manager = new ComboCellEditor.SelectionManager() {
		   private String existingText;
		   public void setExistingText(String existingText) {
			   this.existingText = existingText;
		   }
		   
		   public String getSelectedText(final Object o) {
			   if (o instanceof JCheckBox) {
			       	JCheckBox chkBox = (JCheckBox)o;
			       	String s = chkBox.getText();
			       	if (!chkBox.isSelected()) {
			       		if (!Basics.isEmpty(chkBox.getToolTipText())) {
			       			if (!PopupBasics.ask(chkBox.getToolTipText() +". Change?")) {
			       				return existingText;
			       			}
			       		}
			       		existingText+="+";
			       		existingText+=s+"*"+((JCheckBox)o).getToolTipText();
			       	} 
			       	else if (existingText.indexOf(s) != -1){  
			       		if (existingText.indexOf("+") == -1) {
			       			existingText="";
			       		}
			       		else {
			       			existingText = existingText.substring(0, existingText.indexOf(s)) + existingText.substring(existingText.indexOf(s)+s.length());        			
			       		}
			       	}
			       	if (!Basics.isEmpty(existingText)) {
			       		if (existingText.indexOf("+")==0) {
			           		existingText = existingText.substring(1);
			           	} else if (existingText.indexOf("+")==existingText.length()-1) {
			           		existingText = existingText.substring(0,existingText.length()-1);
			           	}	
			       	}
			       	return existingText;
		       }
		       else if (o instanceof JLabel && !Basics.isEmpty(existingText)) {
		       		return existingText;
		       }
			   return o.toString();
		   }   
	   };
	   _cmb.setSelectionManger(manager);
	   manager.setExistingText(existing);
	   cb.setComboCellEditor(_cmb);
       return _cmb; 
       
   }
}
