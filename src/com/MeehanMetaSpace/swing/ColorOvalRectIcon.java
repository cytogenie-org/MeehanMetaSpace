package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ColorOvalRectIcon implements Icon {
	
	private final Color ovalColor;
	private final Color rectColor;

	public ColorOvalRectIcon(final Color ovalColor, final Color rectColor) {
		this.ovalColor = ovalColor;
		this.rectColor = rectColor;
	}

	public int getIconHeight() {
		return 16;
	}

	public int getIconWidth() {
		return 16;
	}

	public void paintIcon(
			final Component c, 
			final Graphics g, 
			int x,
			int y) {
		int width=4;
		int height=4;		
		if (!Color.white.equals(ovalColor)){
			g.setColor(ovalColor);
			x = 8;
			y=8;
			g.fillOval(x, y, width, height);
			g.drawOval(x, y, width, height);
		}
		if (!Color.white.equals(rectColor)){
			g.setColor(rectColor);
			x += width + 3;
		//	width=8;
			g.fillRect(x, y, width, height);
			g.drawRect(x, y, width, height);
		}
	}
}
