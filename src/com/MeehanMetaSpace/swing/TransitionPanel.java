package com.MeehanMetaSpace.swing;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;

public abstract class TransitionPanel extends JPanel
    implements Runnable {
    
    private int step;
    private BufferedImage buf = null;
    private Component previousComponent = null;
    private int animation_length = 16;

    public TransitionPanel(final LayoutManager lm) {
        super(lm);
    }
    
    public void paintChildren(Graphics g) {
        super.paintChildren(g);
        
        if(step != -1 && nextComponent!=null) {
            Rectangle size = nextComponent.getBounds();
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
    
    protected Component nextComponent;
    private Runnable endTask=null;
    // threading code
    public void animate(final Component comp) {
    	animate(comp,null);
    }
    public void animate(final Component comp, final Runnable endTask) {
    	this.nextComponent=comp;
    	this.endTask=endTask;
        new Thread(this).start();
    }
    

    public void run() {
        step = 0;
        
        // save the previous tab
        if(previousComponent != null && previousComponent.getWidth() > 0 && previousComponent.getHeight() > 0) {
            buf = new BufferedImage(previousComponent.getWidth(),
            		previousComponent.getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
            try{
            previousComponent.paint(buf.getGraphics());
            } catch(RuntimeException e){
            	System.err.println(e.getMessage());
            }
        }
        
        for(int i=0; i<animation_length; i++) {
            step = i;
            repaint();
            try {
                Thread.currentThread().sleep(44);
            } catch (final Exception ex) {
                System.out.println("ex: " + ex);
            }
        }
        
        step = -1;
        repaint();
        previousComponent=nextComponent;
        if (endTask != null){
        	SwingUtilities.invokeLater(endTask);
        	endTask=null;
        }
    }
}
