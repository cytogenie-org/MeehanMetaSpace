package com.MeehanMetaSpace.swing;
import java.util.Collection;

public interface SelectableActiveFilterGroup extends SelectableActiveFilter, Collection<SelectableActiveFilter>{
	int getSelectedCount();
	boolean isVisible();
	void setVisible(final boolean visible);
	boolean describeCriteriaFailures(Row row, Collection<String> criteriaFailureDescriptions);
	boolean isAndLogic();
	String getKey();
	void setHiddenFilter(SelectableActiveFilter filter);
	SelectableActiveFilter getHiddenFilter();
}
