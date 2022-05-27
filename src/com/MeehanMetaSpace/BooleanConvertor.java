package com.MeehanMetaSpace;

import javax.swing.JTextField;

import com.MeehanMetaSpace.DefaultStringConverters._ComparableBoolean;

public class BooleanConvertor extends  _ComparableBoolean{
	public String toString(Object input){
		return input == null ? "": input.toString();
	}
}
