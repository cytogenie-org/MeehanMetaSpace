package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MenuPanel {
	private Color foreground = UIManager.getColor("Menu.foreground"),
	background = UIManager.getColor("Menu.background"),
	selectionBackground = UIManager
			.getColor("MenuItem.selectionBackground"),
	selectionForeground = UIManager
			.getColor("MenuItem.selectionForeground");
	final Component []ca;
	public final MenuElement[] mes;
	public final JPanel menuPanel;
	public MenuPanel(final JPopupMenu jp){
		final int n=jp.getComponentCount();
		ca=new Component[n];
		for (int i=0;i<n;i++) {
			ca[i]=jp.getComponent(i);
		}
		mes= jp.getSubElements();
		menuPanel = new JPanel(new GridLayout(n+1, 1, 0, 0)) {
			private static final long serialVersionUID = 1L;
			int cur = -1, found = -1;

			protected boolean processKeyBinding(final KeyStroke ks,
					final KeyEvent e, final int condition,
					final boolean pressed) {
				final boolean b1 = e.isAltDown(), b2 = e.isControlDown();
				char c = e.getKeyChar();
				if (!b1 && !b2 && pressed) {
					final int kc = ks.getKeyCode();
					if (c == KeyEvent.VK_ENTER || c == ' ') {
						if (found >= 0) {
							final JMenuItem mi = (JMenuItem) mes[found];
							if (mi.isEnabled()) {
								mi.doClick();
								e.consume();
								return true;

							}
						}
					} else if (kc == KeyEvent.VK_UP ||
							kc == KeyEvent.VK_DOWN ||
							(c > ' ' && c <= '~') ){
						final boolean wasFound=found>=0;
						found = -1;							
						if (kc == KeyEvent.VK_DOWN) {
							for (cur = cur + 1; cur < mes.length; cur++) {
								final JMenuItem mi = (JMenuItem) mes[cur];
								if (mi.isEnabled()) {
									found = cur;
									break;
								}
							}
							if (found < 0) {
								final int end=cur==mes.length?mes.length:cur+1;
								for (cur = 0; cur < end; cur++) {		
									final JMenuItem mi = (JMenuItem) mes[cur];
									if (mi.isEnabled()) {
										found = cur;
										break;
									}
								}
							}
						} else if (kc == KeyEvent.VK_UP) {
							for (cur = cur - 1; cur >=0; cur--) {
								final JMenuItem mi = (JMenuItem) mes[cur];
								if (mi.isEnabled()) {
									found = cur;
									break;
								}
							}
							if (found < 0) {
								final int end=cur == -1? -1:cur-1;
								for (cur = mes.length-1; cur > end; cur--) {		
									final JMenuItem mi = (JMenuItem) mes[cur];
									if (mi.isEnabled()) {
										found = cur;
										break;
									}
								}
							}
						} else if (c > ' ' && c <= '~') {
							c = Character.toLowerCase(c);
							for (cur = cur + 1; cur < mes.length; cur++) {
								final JMenuItem mi = (JMenuItem) mes[cur];
								if (mi.isEnabled()) {
									final char c2 = Character
											.toLowerCase((char) mi
													.getMnemonic());
									if (c2 == c) {
										found = cur;
										break;
									}
								}
							}
							if (found < 0) {
								final int end=cur==mes.length?mes.length:cur+1;
								for (cur = 0; cur < end; cur++) {
									final JMenuItem mi = (JMenuItem) mes[cur];
									if (mi.isEnabled()) {
										final char c2 = Character
												.toLowerCase((char) mi
														.getMnemonic());
										if (c2 == c) {
											found = cur;
											break;
										}
									}
								}
							}
						}
						if (found >= 0) {
							for (int i = 0; i < mes.length; i++) {
								final JMenuItem mi = (JMenuItem) mes[i];
								if (i == found) {
									mi.setOpaque(true);
									mi.setBackground(selectionBackground);
									mi.setForeground(selectionForeground);
								} else {
									mi.setForeground(foreground);
									mi.setBackground(background);
									mi.setOpaque(false);
								}
							}
							int cnt = 0;
							for (final MenuElement me : mes) {
								final JMenuItem mi = (JMenuItem) me;
								if (mi.isEnabled()){
								final char c2 = Character
										.toLowerCase((char) mi
												.getMnemonic());
								if (c2 == c) {
									cnt++;
								}
								}
							}
							if(cnt==1){									
								final JMenuItem mi = (JMenuItem) mes[found];
								mi.doClick();
							}
						} else if (!wasFound){
							cur=-1;
						}
						e.consume();
						return true;
					}
				}
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		};
		menuPanel.setOpaque(false);
	}
	
	public void addMenuItems(){
		for (final Component o:ca) {
			if (o instanceof JMenuItem) {
				final JMenuItem mi = (JMenuItem) o;
				SwingBasics.addMouseOver(mi);
				if (!mi.getBackground().equals(SwingBasics.getAlreadyDoingBackground())){
					mi.setOpaque(false);
				} 				
				menuPanel.add(mi);
			} else {
				if (o instanceof JSeparator){
		    		((JSeparator)o).setOpaque(false);
		    		menuPanel.add(o);
				}
			}
		}
    }
}
