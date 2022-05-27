package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.MeehanMetaSpace.Basics;

public class GradientBasics {
	public static class Panel extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Panel(){
	        setOpaque(true);
	    	setTransparentChildrenWhenVisible(this);
		}
		
		public Panel(final LayoutManager lm){
			super(lm);
	        setOpaque(true);
	        setTransparentChildrenWhenVisible(this);	
		}
		protected void paintComponent( Graphics g ){
	        if ( !isOpaque( ) )    {
	            super.paintComponent( g );
	            return;
	        }     
	        GradientBasics.paint(this, g);     
	        setOpaque( false );
	        super.paintComponent( g );
	        setOpaque( true );
	    }

	}

    public static class TabbedPaneUI extends BasicTabbedPaneUI{
    	
        public void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                       int x, int y, int w, int h,
                                       boolean isSelected)
        {
        	final Color c1=UIManager.getColor("Panel.background"), c2=c1.brighter();
            final Graphics2D g2 = (Graphics2D)g;
            final GradientPaint
                gradient = new GradientPaint(x, y + h/2, c1,
                                             x + w, y + h/2, c2, true),
                selected = new GradientPaint(x, y + h/2, PersonalizableTable.YELLOW_STICKY_COLOR.brighter(),
                                             x + w, y + h/2, PersonalizableTable.YELLOW_STICKY_COLOR, false);
            g2.setPaint(!isSelected ? gradient : selected);
            switch(tabPlacement) {
              case LEFT:
                  g.fillRect(x+1, y+1, w-1, h-3);
                  break;
              case RIGHT:
                  g.fillRect(x, y+1, w-2, h-3);
                  break;
              case BOTTOM:
                  g.fillRect(x+1, y, w-3, h-1);
                  break;
              case TOP:
              default:
                  g.fillRect(x+1, y+1, w-3, h-1);
            }
        }

    }
    

    static {
    	UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
    }
    private static final double FACTOR = 0.73;

    private static Color darker(final Color col) {
        
    	return new Color(Math.max((int)(col.getRed()  *FACTOR), 0), 
    			 Math.max((int)(col.getGreen()*FACTOR), 0),
    			 Math.max((int)(col.getBlue() *FACTOR), 0));
        }

    
    public static void paint(final Component c, final Graphics g){
    	paint(c,false,g);
    }
   	static void paint(final Component c, boolean ignoreLeeColors, final Graphics g){
    	Color color1 = c.getBackground();
    	final Color color2;
		if (color1.equals(PersonalizableTable.YELLOW_STICKY_COLOR)) {
			color2 = color1.brighter();
		} else {
			color1=UIManager.getColor("Panel.background");
			/*
			 * IF USER'S COLOR  IS   CLOSE   TO  WHAT  LEE  WANTS THEN  
			 *  FORCE IT THAT WAY!
			 *     private static Color top=new Color(255, 254, 254);
					private static Color bottom=new Color(222,225,225);

			 */
			final int r=color1.getRed(), green=color1.getGreen(), b=color1.getBlue();
			if (!ignoreLeeColors && r >= 244 && b >= 244 && green >= 244){
				color1=new Color(255, 254, 254);
				color2=new Color(217,220,220); // WAS 222, 225, 225
			} else {
				color2 = darker(color1);
			}
		}
    	final Graphics2D g2d = (Graphics2D)g;
    	final int w = c.getWidth( );
    	final int h = c.getHeight( );
    	 
    	// Paint a gradient from top to bottom
    	final GradientPaint gp = new GradientPaint(
    	    0, 0, color1,
    	    0, h, color2 );

    	g2d.setPaint( gp );
    	g2d.fillRect( 0, 0, w, h );
    }
	

	public static void setTransparentChildrenWhenVisible(final JComponent cmp) {
		cmp.addAncestorListener(new AncestorListener() {

			public void ancestorRemoved(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {
			}

			public void ancestorAdded(AncestorEvent event) {
				setTransparentChildren(cmp, true);
			}
		});
	}

	private static class GradientListCellRenderer implements ListCellRenderer {
		final ListCellRenderer prior;

		public GradientListCellRenderer(final ListCellRenderer prior) {
			this.prior = prior;
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final Component c = prior.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			((JComponent) c).setOpaque(isSelected);
			return c;
		}
	}

	public static void setTransparentChildren(final Component c,
			final boolean childOnly) {
		if (!childOnly) {
			final String name=c.getClass().getName();
			if (name.equals("edu.stanford.smi.protege.util.LabeledComponent")){
				((JComponent)c).setOpaque(false);
			} else if (c instanceof JList ) {
				((JList) c).setOpaque(false);
				final ListCellRenderer lcr=((JList) c).getCellRenderer();
				if (!(lcr instanceof GradientListCellRenderer)){
					((JList) c).setCellRenderer(new GradientListCellRenderer(lcr));
				}
			} else if (c instanceof JLayeredPane && !Basics.isJava5) {
				((JLayeredPane) c).setOpaque(false);
			} else if (c instanceof JToggleButton) {
				((JToggleButton) c).setOpaque(false);
			} else if (c instanceof JSeparator) {
				((JSeparator) c).setOpaque(false);
			} else if (c instanceof JSplitPane) {
				((JSplitPane) c).setOpaque(false);
			} else if (c instanceof JPanel) {
				((JPanel) c).setOpaque(false);
			} else if (c instanceof JScrollPane) {
				((JScrollPane) c).setOpaque(false);
			} else if (c instanceof JViewport) {
				((JViewport) c).setOpaque(false);
			} else {
				//System.out.println(name+" "+c.isOpaque());
			}
		}
		if (c instanceof Container) {
			final Container cnt = (Container) c;
			final int n = cnt.getComponentCount();
			for (int i = 0; i < n; i++) {
				final Component cmp = cnt.getComponent(i);
				if (cmp instanceof Container) {
					setTransparentChildren(cmp, false);
				}
			}
		}
	}

}
