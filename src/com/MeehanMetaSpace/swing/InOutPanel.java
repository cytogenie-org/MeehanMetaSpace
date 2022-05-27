package com.MeehanMetaSpace.swing;

import java.awt.*;

public class InOutPanel extends TransitionPanel {
	
	public InOutPanel (final LayoutManager lm){
		super(lm);
	}
	
	public void paintTransition(Graphics2D g2, int step, Rectangle size,
			Image prev) {
		InOutTabbedPane._paintTransition(g2, step, size, prev, getAnimationLength());
	}

}
