package com.MeehanMetaSpace.swing;
import java.util.Collection;

public interface ActiveFilter {
	enum FilterType {TABS, FILTERGROUP, SCOPE, ACTIVE, VISIBLE, SOURCE};
    boolean meetsCriteria(Row row);
    boolean describeCriteriaFailures(Row row, Collection<String> criteriaFailureDescriptions);
    FilterType getType();
    SelectableActiveFilterGroup getTopGroup();
}
