package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MenuInWindow extends JMenu {
	static Collection<MenuInWindow>allUnclosed=new ArrayList<MenuInWindow>();
	public MenuInWindow(){
		allUnclosed.add(this);
		addMouseListener(new MouseAdapter() {
			private int hoveringOm=0;
			private Timer t;
			public void mouseEntered(MouseEvent e) {
				hoveringOm++;
				purgeTimer();
				final int state=hoveringOm;
				if (!isPopupMenuVisible()){
					t=new Timer();
					t.schedule(new TimerTask() {
						public void run() {
							if (state==hoveringOm){
								popup();
							}
						}
					}, 350);
				}
			}
			private void purgeTimer(){
				if (t!=null){
					t.cancel();
					t.purge();
				}
			}
			public void mouseExited(MouseEvent e) {
				hoveringOm++;
				purgeTimer();
			}
			public void mouseReleased(MouseEvent e) {
				popup();
			}
		});

	}

	public void close(){
		if (popupMenu!=null){
    		popupMenu.setVisible(false);
    		System.out.print("Closed menus in windows"+allUnclosed.size());
    		allUnclosed.remove(this);
    		System.out.println(" "+allUnclosed.size());
    	}
    	
	}
	
	public static void closeAll(){
		final Collection<MenuInWindow>c=new ArrayList<MenuInWindow>(allUnclosed);
		for (final MenuInWindow m:c){
			m.close();
		}
	}
    public void removeAll() {
    	close();
    	popupMenu=null;
    	super.removeAll();
    }
	private JPopupMenu popupMenu;
	public JPopupMenu popup(){
		fireMenuSelected();
		if (popupMenu==null){
			popupMenu=PopupBasics.popup(this);
		} else{
		final Window w=SwingUtilities.getWindowAncestor(this);
		if (w != null){
			final Point p1=w.getLocationOnScreen(), p2=getLocationOnScreen();
			final Point p=new Point(p2.x-p1.x, p2.y-p1.y);
			popupMenu.show(w, p.x+getWidth(), p.y);
		}
		}
    	return popupMenu;
	}
	
    public void setPopupMenuVisible(boolean b) {
    	
    }

    public void setBackground(final Color bg){
    	if (!bg.equals(SwingBasics.getAlreadyDoingBackground())){
    		super.setBackground(bg);
    	} else {
    		System.out.print(' ');
    	}
    }
	public void doClick(){
		popup();
	}
}
