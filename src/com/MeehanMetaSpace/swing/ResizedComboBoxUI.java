package com.MeehanMetaSpace.swing;
import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.Rectangle;
import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.Dimension;
//import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import ch.randelshofer.quaqua.QuaquaComboBoxUI;
import ch.randelshofer.quaqua.QuaquaComboPopup;
import com.pagosoft.plaf.PgsComboBoxUI;
public class ResizedComboBoxUI {
	interface ListHighlightShower{
		int getHighlighted();
	}
    private static class CmdPopup extends BasicComboPopup {
        CmdPopup(final JComboBox jcb) {
            super(jcb);
        }

        int getHighlighted() {
            return super.list.getSelectedIndex();
        }

        void setHighlighted(final int idx) {
            super.list.setSelectedIndex(idx);
            super.list.ensureIndexIsVisible(idx);
        }

        public void show() {
        	comboBox.firePopupMenuWillBecomeVisible();
            if (comboBox.getSelectedIndex() == -1) {
                super.list.clearSelection();
            } else {
                super.list.setSelectedIndex(comboBox.getSelectedIndex());
                int n=comboBox.getSelectedIndex()+comboBox.getMaximumRowCount()-1;
                int m=comboBox.getModel().getSize()-comboBox.getMaximumRowCount();
                final int first=n>m?comboBox.getSelectedIndex():n;
                SwingUtilities.invokeLater(new Runnable(){
                	public void run(){
                        list.ensureIndexIsVisible(first);                		
                	}
                });
            }

            Dimension popSize = super.comboBox.getSize();
            Dimension listSize = list.getPreferredSize();

            int newWidth = listSize.width;
            newWidth += 20; // for the size of the scrollbar !
            if (popSize.width > newWidth) {
                newWidth = popSize.width;
            }
            popSize.setSize(newWidth,
                            getPopupHeightForRowCount(super.comboBox.
              getMaximumRowCount()));

            Rectangle popupBounds = this.computePopupBounds(0,
              comboBox.getBounds().height, popSize.width, popSize.height);

            setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

            super.scroller.setPreferredSize(popupBounds.getSize());

            super.list.invalidate();
            super.comboBox.validate();
            if (super.comboBox.isShowing()){
            	show(super.comboBox, popupBounds.x, popupBounds.y);
            }
        }
    }

    private static class CmdPopupQuaQua extends QuaquaComboPopup{
        CmdPopupQuaQua(final JComboBox jcb, final QuaquaComboBoxUI qqui) {
        	super(jcb, qqui);
        }

        int getHighlighted() {
            return super.list.getSelectedIndex();
        }

        void setHighlighted(final int idx) {
            super.list.setSelectedIndex(idx);
            super.list.ensureIndexIsVisible(idx);
        }

        public void show() {
        	comboBox.firePopupMenuWillBecomeVisible();
            if (comboBox.getSelectedIndex() == -1) {
                super.list.clearSelection();
            } else {
                super.list.setSelectedIndex(comboBox.getSelectedIndex());
                int n=comboBox.getSelectedIndex()+comboBox.getMaximumRowCount()-1;
                int m=comboBox.getModel().getSize()-comboBox.getMaximumRowCount();
                final int first=n>m?comboBox.getSelectedIndex():n;
                SwingUtilities.invokeLater(new Runnable(){
                	public void run(){
                        list.ensureIndexIsVisible(first);                		
                	}
                });
            }

            Dimension popSize = super.comboBox.getSize();
            Dimension listSize = list.getPreferredSize();

            int newWidth = listSize.width;
            newWidth += 20; // for the size of the scrollbar !
            if (popSize.width > newWidth) {
                newWidth = popSize.width;
            }
            popSize.setSize(newWidth,
                            getPopupHeightForRowCount(super.comboBox.
              getMaximumRowCount()));

            Rectangle popupBounds = this.computePopupBounds(0,
              comboBox.getBounds().height, popSize.width, popSize.height);

            setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

            super.scroller.setPreferredSize(popupBounds.getSize());

            super.list.invalidate();
            super.comboBox.validate();
            if (super.comboBox.isShowing()){
            	show(super.comboBox, popupBounds.x, popupBounds.y);
            }
        }
    }

    boolean ignoreAction;

    private final class ResizedBasicComboBoxUI extends BasicComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }


        protected ComboPopup createPopup() {
        	final CmdPopup popup = new CmdPopup(super.comboBox);
            popup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return popup;
        }
    }


    /*private final class ResizedWindowsComboBoxUI extends WindowsComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
    	
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }

        protected ComboPopup createPopup() {
        	final CmdPopup myPopup = new CmdPopup(super.comboBox);
            myPopup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return myPopup;
        }
    }*/

    private final class ResizedPgsComboBoxUI extends PgsComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
    	
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }

        protected ComboPopup createPopup() {
        	final CmdPopup myPopup = new CmdPopup(super.comboBox);
            myPopup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return myPopup;
        }
    }

    private final class ResizedQuaquaComboBoxUI extends QuaquaComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
    	
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }

        protected ComboPopup createPopup() {
        	final CmdPopupQuaQua myPopup = new CmdPopupQuaQua(super.comboBox, this);
            myPopup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return myPopup;
        }
    }

    private final class ResizedMetalComboBoxUI extends MetalComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }


        
        protected ComboPopup createPopup() {
            final CmdPopup popup = new CmdPopup(super.comboBox);
            popup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return popup;
        }
    }


    private final class ResizedMotifComboBoxUI extends MotifComboBoxUI implements ListHighlightShower{
    	public int getHighlighted(){
    		return popup.getList().getSelectedIndex();
    	}
        protected void selectNextPossibleValue() {
            ignoreAction = true;
            super.selectNextPossibleValue();
            ignoreAction = false;
        }

        protected void selectPreviousPossibleValue() {
            ignoreAction = true;
            super.selectPreviousPossibleValue();
            ignoreAction = false;
        }


        protected ComboPopup createPopup() {
        	final CmdPopup popup = new CmdPopup(super.comboBox);
            popup.getAccessibleContext().setAccessibleParent(super.comboBox);
            return popup;
        }
    }


    public ResizedComboBoxUI(final JComboBox comboBox) {
        if (!SwingBasics.isNativeMacLookAndFeel()) {
            final ComboBoxUI bcbu = comboBox.getUI();
            //System.out.println(bcbu.getClass().getName());
            if (bcbu instanceof com.pagosoft.plaf.PgsComboBoxUI){
            	comboBox.setUI(new ResizedPgsComboBoxUI());
            }else if (bcbu instanceof MotifComboBoxUI) {
                comboBox.setUI(new ResizedMotifComboBoxUI());
            } else if (bcbu instanceof MetalComboBoxUI) {
                comboBox.setUI(new ResizedMetalComboBoxUI());
            } /*else if (bcbu instanceof WindowsComboBoxUI) {
                comboBox.setUI(new ResizedWindowsComboBoxUI());
            }*/ else if (bcbu instanceof QuaquaComboBoxUI){
            	comboBox.setUI(new ResizedQuaquaComboBoxUI());
            }else {
                comboBox.setUI(new ResizedBasicComboBoxUI());
            }
        }
    }
    

}
