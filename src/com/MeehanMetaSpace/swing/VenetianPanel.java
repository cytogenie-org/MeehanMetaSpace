package com.MeehanMetaSpace.swing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.LayoutManager;

public class VenetianPanel extends TransitionPanel {

	public VenetianPanel(final LayoutManager lm){
		super(lm);
	}
	@Override
	public void paintTransition(Graphics2D g2, int step, Rectangle size,
			Image prev) {
		VenetianTabbedPane._paintTransition(g2, step, size, prev, getAnimationLength());

	}

}
