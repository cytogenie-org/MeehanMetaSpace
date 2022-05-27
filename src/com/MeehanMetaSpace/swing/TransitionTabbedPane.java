package com.MeehanMetaSpace.swing;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;

public abstract class TransitionTabbedPane extends JTabbedPane 
    implements ChangeListener, Runnable{
    
    private int step;
    private BufferedImage buf = null;
    private int previous_tab = -1;
    private int animation_length = 15;

    public TransitionTabbedPane() {
        super();
        this.addChangeListener(this);
    }
    
    public void paintChildren(Graphics g) {
        super.paintChildren(g);
        
        if(step != -1) {
            Rectangle size = this.getComponentAt(0).getBounds();
            Graphics2D g2 = (Graphics2D)g;
            paintTransition(g2, step, size, buf);
        }
    }
    
    public int getAnimationLength() {
        return this.animation_length;
    }
    
    public void setAnimationLength(int length) {
        this.animation_length = length;
    }
    
    public abstract void paintTransition(Graphics2D g2, int step,
            Rectangle size, Image prev);
    
    private boolean animate=true;
    public boolean setAnimation(final boolean on){
    	final boolean was=animate;
    	animate=on;
    	return was;
    }
    
    // threading code
    public void stateChanged(ChangeEvent evt) {
    	if (animate){
        new Thread(this).start();
    	}
    }

    public void run() {
        step = 0;
        
        // save the previous tab
        if(previous_tab != -1 && previous_tab < getTabCount()) {
            Component comp = getComponentAt(previous_tab);
			if (comp != null) {
				final int h = comp.getHeight(), w = comp.getWidth();
				if (h > 0 && w > 0) {
					buf = new BufferedImage(comp.getWidth(), comp.getHeight(),
							BufferedImage.TYPE_4BYTE_ABGR);
					if (SwingUtilities.getWindowAncestor(comp)==null) {
						System.out.println("No window for "+comp);
					} else {
						comp.paint(buf.getGraphics());
					}
				}
			}
        }
        
        for(int i=0; i<animation_length; i++) {
            step = i;
            repaint();
            try {
                Thread.currentThread().sleep(40);
            } catch (Exception ex) {
            	System.out.println("ex: " + ex);
            }
        }
        
        step = -1;
        previous_tab = this.getSelectedIndex();
        repaint();
    }    
}
