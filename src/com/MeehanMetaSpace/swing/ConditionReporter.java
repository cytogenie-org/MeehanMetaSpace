package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.Condition;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public interface ConditionReporter {
	void reportCondition(Condition anomaly, String value, String msg);
	void reportCondition(Condition anomaly, String msg);
}
