package com.MeehanMetaSpace.swing;

import javax.swing.Icon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class MouseHoverButton extends JButton{
	
	public boolean startedByMouse = false;
	
	public MouseHoverButton(String description) {
		super(description);
		SwingBasics.setToolBarStyle(this);
	}
		
	public MouseHoverButton(Icon icon) {
		super(icon);
		SwingBasics.setToolBarStyle(this);

	}
	
	public MouseHoverButton(String description, Icon icon) {
		super(description,icon);
	}	    	
}
