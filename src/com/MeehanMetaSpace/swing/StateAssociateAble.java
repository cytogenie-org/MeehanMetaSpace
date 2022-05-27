package com.MeehanMetaSpace.swing;

public interface StateAssociateAble {
	Object getAssociatedState();
	void setAssociatedState(Object state);
	boolean hasAssociatedState(Object state);
}
