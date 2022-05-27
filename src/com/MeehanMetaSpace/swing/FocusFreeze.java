package com.MeehanMetaSpace.swing;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class FocusFreeze {
	private static boolean isFrozen=false;
	private final Boolean prev;

	public static boolean isFrozen(){
		return isFrozen;
	}

	public FocusFreeze() {
		this(true);
	}
	
	public FocusFreeze(final boolean freeze) {
		prev = setFrozen(freeze);
	}
	

	private static Boolean setFrozen(final Boolean value) {
		if (value != null && SwingUtilities.isEventDispatchThread() ) {
			Boolean prev = null;
			prev = isFrozen;
			isFrozen = value;
			return prev;
		}
		return null;
	}

	public void restorePrevValue() {
		setFrozen(prev);
	}

	public void restorePrevValueLater() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				restorePrevValue();
			}
		});
	}
	
	public void thawLater(final JComponent newFocus) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setFrozen(false);
				if (newFocus!=null){
					newFocus.requestFocus();
				}
			}
		});
	}
	public void thawLater() {
		thawLater(null);
	}

}
