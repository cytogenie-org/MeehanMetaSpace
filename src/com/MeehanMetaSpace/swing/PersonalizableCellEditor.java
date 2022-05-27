package com.MeehanMetaSpace.swing;

import java.awt.event.KeyEvent;

public interface PersonalizableCellEditor {
	KeyEvent lastKeyEvent();
	boolean didTableEditingStart();
}
