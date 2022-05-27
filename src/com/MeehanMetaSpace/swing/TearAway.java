package com.MeehanMetaSpace.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.*;
import java.awt.event.WindowEvent;
import com.MeehanMetaSpace.PropertiesBasics;

import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;

public class TearAway {
    public interface Item {
        void setTearAwayComponent(JComponent c);
        JComponent getTearAwayComponent();

        Properties getPropertiesForTearAway();

        PropertiesBasics.Savior getPropertiesSaviorForTearAway();

        String getPropertiesPrefixForTearAway();

        boolean supportsTearAway();
        JComponent getSeperateWindowPanel(JComponent tornAway);
    }

    public interface Listener{
        void actionPerformed(boolean tornAway, Container container);
    }

    public static class Handler{
        private final Collection<Listener> listeners=new ArrayList<Listener>();

        public void addListener(Listener l){
            listeners.add(l);
        }

        public void removeListener(final Listener l){
            listeners.remove(l);
        }

        private final String UNTORN_TEXT = "Put in separate window", TORN_TEXT =
          "Put in original window";

        final Map<JComponent, JMenuItem> menuItemByContext=new HashMap();

        public Handler(final Item item) {
            this.item = item;
        }

        public void removeContext(final JComponent context){
            menuItemByContext.remove(context);

        }
        public void addContext( final JComponent context, final JMenuItem tearAwayItem){
            menuItemByContext.put(context, tearAwayItem);
            if (item.supportsTearAway()) {
                if (divorcedParent == null) {
                    tearAwayItem.setText(UNTORN_TEXT);
                } else {
                    tearAwayItem.setText(TORN_TEXT);
                }
                tearAwayItem.setIcon(MmsIcons.getRestoreIcon());
                tearAwayItem.setMnemonic('w');                
            }
        }
        
        
        public PersonalizableTableModel tableModel = null;
        
        public void setTableModel(PersonalizableTableModel model) {
        	tableModel = model;
        }
        
        
        public void echoAction(final JComponent context, final JMenuItem tearAwayItem){
        	SwingBasics.echoAction(
                    context, tearAwayItem, new ActionListener() {
                    
                      public void actionPerformed(final ActionEvent e) {
                    	  if (divorcedParent == null) {
                    		  //tableModel.isTreeOn = false;
                    		  tableModel.setTreeMenu();
                    	  }
                    	  else {
                    		  tableModel.isTreeOn = tableModel.tableHadTreePreviously;
                    		  tableModel.setTreeMenu();
                    	  }
                          toggleTearAway();
                          
                      }
                  }, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK), 'w');
        }

        private Container divorcedParent;
        private JFrame separateWindow;
        private JPanel separateWindowPanel;
        private void untearAway() {
            if (divorcedParent != null) {
                final JComponent jc = item.getTearAwayComponent();
                separateWindow.getContentPane().remove(jc);
                SwingBasics.switchContaineesWithinContainer(divorcedParent, separateWindowPanel,
                                       jc);
                for (JMenuItem tearAwayItem:menuItemByContext.values()){
                    if (tearAwayItem != null) {
                        tearAwayItem.setText("Put in separate window");
                    }
                }
                for (Listener listener:listeners){
                    listener.actionPerformed(false, divorcedParent);
                }
                divorcedParent = null;
            }
        }

        private final Item item;
        public void closeTearAway() {
            if (separateWindow != null) {
                untearAway();
                SwingBasics.closeWindow(separateWindow);
                separateWindow = null;
            }
        }

        public void tearAway() {
            final JComponent jc = item.getTearAwayComponent();
            divorcedParent = jc.getParent();
            separateWindowPanel = new JPanel();
            TransferHandler th=jc.getTransferHandler();
            for (Container p=divorcedParent.getParent();th==null&&p!=null;p=p.getParent()){
                if (p instanceof JComponent){
                    th= ( (JComponent) p).getTransferHandler();
                    if (th != null){
                    	break;
                    }
                }
            }
            separateWindowPanel.add(SwingBasics.getButton("See separate window", null, 't', new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    separateWindow.toFront();
                }
            },"Click to access window"));
            SwingBasics.switchContaineesWithinContainer(divorcedParent, jc,
                                   separateWindowPanel);
            separateWindow = SwingBasics.getFrame();
            jc.setVisible(true);
            final JPanel mainPanel =new GradientBasics.Panel(new BorderLayout());
            
            final JComponent main=item.getSeperateWindowPanel(jc);
            mainPanel.add(main, BorderLayout.CENTER);
            
            if (th != null){
                main.setTransferHandler(th);
            }
            final JPanel bottomPanel=new JPanel(new BorderLayout());
            final SwingBasics.PinButton pin=new SwingBasics.PinButton(mainPanel, false,"tableTearAway");
            bottomPanel.add(pin, BorderLayout.EAST);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            separateWindow.getContentPane().add(mainPanel);
            SwingBasics.packAndPersonalize(
              separateWindow,
              item.getPropertiesForTearAway(),
              item.getPropertiesSaviorForTearAway(),
              item.getPropertiesPrefixForTearAway(), true, true, false);
            if (!pin.activate(separateWindow)){
            	separateWindow.show();
            }
            
            separateWindow.addWindowListener(new WindowAdapter() {
                public void windowClosing(final WindowEvent we) {
                    untearAway();
                    separateWindow = null;
                    if(tableModel != null) {
                    	tableModel.isTreeOn = tableModel.tableHadTreePreviously;
                    	tableModel.setTreeMenu();
                    }
                }
            });
            for (Listener listener:listeners){
                listener.actionPerformed(true, separateWindow.getContentPane());
            }
            for (JMenuItem tearAwayItem:menuItemByContext.values()){
                if (tearAwayItem != null) {
                    tearAwayItem.setText("Put in original window");
                }
            }
            GradientBasics.setTransparentChildren(mainPanel, true);
        }

        public void toggleTearAway() {
            if (item.supportsTearAway()) {
                if (divorcedParent == null) {
                    tearAway();
                } else {
                    closeTearAway();
                }
            }
        }

        public boolean toggleTearAwayIfUserPermits() {
            boolean toggle = false;
            if (item.supportsTearAway()) {
                if (divorcedParent == null) {
                    toggle = PopupBasics.ask("Put table in separate window?");
                } else {
                    toggle = PopupBasics.ask("Put table back in original window?");
                }
                if (toggle) {
                    toggleTearAway();
                }
            }
            return false;
        }
        public void dispose(){ // cannot inherit from protege interface 'cos 2 packages are decoupled
            closeTearAway();
            listeners.clear();
        }
        boolean isTornAway(){
        	return divorcedParent!=null;
        }
    }
    

}
