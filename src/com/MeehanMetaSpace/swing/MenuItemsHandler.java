package com.MeehanMetaSpace.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.MeehanMetaSpace.swing.PersonalizableTableModel.MenuItemListener;

public class MenuItemsHandler {

	private final JComponent c;
    private final java.awt.event.ActionListener keyboardAction;
    private final KeyStroke keyStroke;
    private final char mnemonic;
    private final DisabledExplainer disabledExplainer;
    private final String disabledToolTipText;
    private final MenuItemListener mil;
    private final String menuText;
    
	public MenuItemsHandler(final JComponent c, final ActionListener anAction, final KeyStroke keyStroke, 
			final char mnemonic, final DisabledExplainer disabledExplainer, final String disabledToolTipText, 
			final MenuItemListener mil, final String menuText) {
		this(c, anAction, anAction, keyStroke, mnemonic, disabledExplainer, disabledToolTipText, mil, menuText);
	}
	
	public MenuItemsHandler(final JComponent c, final ActionListener keyboardAction, final ActionListener menuAction, final KeyStroke keyStroke, 
			final char mnemonic, final DisabledExplainer disabledExplainer, final String disabledToolTipText, 
			final MenuItemListener mil, final String menuText) {
		this.c = c;
		this.keyboardAction = keyboardAction;
		this.keyStroke = keyStroke;
		this.mnemonic = mnemonic;
		disabledExplainer.getMenuItem().setAccelerator(keyStroke);
		disabledExplainer.getMenuItem().setMnemonic(mnemonic);
		disabledExplainer.getMenuItem().addActionListener(menuAction);
		this.disabledExplainer = disabledExplainer;
		this.disabledToolTipText = disabledToolTipText;
		this.mil = mil;
		this.menuText = menuText;
		if(c != null) {
			registerKeyboardAction(c);
		}
	}
	
	
	public void registerKeyboardAction(final JComponent c) {
		if (keyStroke != null) {
			c.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					ToolTipOnDemand.getSingleton().hideTipWindow();
					final String anomaly = mil.computeNewAnomaly();
					if (!disabledExplainer.setEnabled(anomaly == null, null,
							anomaly)) {
						disabledExplainer.showDisabledTextOnTheComponent(c);
					} else {
						mil.keyboardActionStarted(disabledExplainer.getMenuItem());
						if (disabledExplainer.getMenuItem().isEnabled()) {
							keyboardAction.actionPerformed(ae);
						} else {
							if (mil.useOriginalDisabledText()) {
								disabledExplainer.setEnabled(false, menuText,
										disabledToolTipText);
							}
							disabledExplainer.showDisabledTextOnTheComponent(c);
						}
					}
				}
			}, keyStroke, JComponent.WHEN_FOCUSED);
		}
	}
	
	public void register(final JPopupMenu pm, final String text) {
		if (text != null) {
			disabledExplainer.getMenuItem().setText(text);
		}
		pm.add(disabledExplainer.getMenuItem());
	}

	public void setText(final String text) {
		disabledExplainer.getMenuItem().setText(text);
	}
	
	public void setIcon(final Icon icon) {
		disabledExplainer.getMenuItem().setIcon(icon);
	}
	
	public void register(final JMenu pm, final String text) {
		if (text != null) {
			disabledExplainer.getMenuItem().setText(text);
		}
		pm.add(disabledExplainer.getMenuItem());
	}
	
	public DisabledExplainer getDisabledExplainer() {
		return disabledExplainer;
	}
	
	public JMenuItem getMenuItem() {
		return disabledExplainer.getMenuItem();
	}
	
}
