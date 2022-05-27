package com.MeehanMetaSpace.swing;

import javax.swing.JComponent;

public interface VagueWindowComponent{
	JComponent getComponent();
	boolean closeOnClick();
	boolean enableWhenSelected();
  }
