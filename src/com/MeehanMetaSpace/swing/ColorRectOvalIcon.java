package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ColorRectOvalIcon implements Icon{
	private final Color ovalColor;
	private final Color rectColor;

	public ColorRectOvalIcon(final Color rectColor, final Color ovalColor) {
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
		if (!Color.white.equals(rectColor)){
			g.setColor(rectColor);
			x = 4;
			y=4;
			g.fillRect(x, y, width, height);
			g.drawRect(x, y, width, height);
		}
		if (!Color.white.equals(ovalColor)){
			g.setColor(ovalColor);
			x += width + 3;
			g.fillOval(x, y, width, height);
			g.drawOval(x, y, width, height);
		}
	}
}
