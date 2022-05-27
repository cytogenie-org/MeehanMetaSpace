
package com.MeehanMetaSpace.swing;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

	public interface SortValueReinterpreter{
		Comparable reinterpret(Object other);
		Class reinterpret(Class clas);
		boolean useInDropDownList();
		boolean isOn();
		boolean canTurnOff();
		void activate(boolean on);
		String getName();
	}

