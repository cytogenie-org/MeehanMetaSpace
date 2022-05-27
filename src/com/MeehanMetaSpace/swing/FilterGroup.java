package com.MeehanMetaSpace.swing;

import java.util.ArrayList;
import java.util.Collection;

public final class FilterGroup extends ArrayList<SelectableActiveFilter> implements SelectableActiveFilterGroup {
	private final boolean and;
	private SelectableActiveFilter hiddenFilter;
	private boolean visible=true;
	public void setVisible(final boolean ok){
		visible=ok;
	}
	public boolean isVisible(){
		return visible;
	}
	public boolean isAndLogic() {
		return and;
	}

	public void setHiddenFilter(SelectableActiveFilter filter) {
		hiddenFilter = filter;
	}
	
	public SelectableActiveFilter getHiddenFilter() {
		return hiddenFilter;
	}
	
	public boolean isHiddenFilter() {
		return false;
	}
	
	private final String key;
	public String getKey(){
		return key;
	}
	
	public FilterGroup(final String key, final boolean andBetweenGroupMembers) {
		this.and = andBetweenGroupMembers;
		this.key=key;
	}

	public void associateTopGroup(final SelectableActiveFilterGroup topGroup) {
		for (final SelectableActiveFilter a : this) {
			a.associateTopGroup(topGroup);
		}
	}

	public boolean isSelected() {
		return true;
	}

	public boolean describeCriteriaFailures(final Row row,
			final Collection<String> criteriaFailureDescriptions) {
		boolean ok = true;
		final Collection<String> all = new ArrayList<String>();
		if (and) {
			for (final SelectableActiveFilter fcb : this) {
				if (fcb.isSelected() && !fcb.meetsCriteria(row)) {
					ok = false;
					criteriaFailureDescriptions.add(((FilterCheckBox) fcb)
							.getText());
				}
			}
		} else {
			ok = false;
			for (final SelectableActiveFilter fcb : this) {
				if (!fcb.isHiddenFilter() && fcb.isSelected()
						&& fcb.meetsCriteria(row)) {
					ok = true;
				} else {
					if (!fcb.isSelected() &&  fcb.meetsCriteria(row)) {
						all.add(((FilterCheckBox) fcb).getText());
					}
				}
			}
		}
		if (!ok) {
			for (final String s : all) {
				if (!criteriaFailureDescriptions.contains(s)) {
					criteriaFailureDescriptions.add(s);
				}
			}
		}
		return ok;
	}

	
	public boolean meetsCriteria(final Row row) {
		if (and) {
			for (final SelectableActiveFilter fcb : this) {
				if (fcb.isSelected() && !fcb.meetsCriteria(row)) {
					return false; // AND short circuit logic
				}
			}
			return true;
		}
		for (final SelectableActiveFilter fcb : this) {
			if (!fcb.isHiddenFilter() && fcb.isSelected() && fcb.meetsCriteria(row)) {
				return true;// OR short circuit logic
			}

		}
		return false;

	}

	public ActiveFilter.FilterType getType() {
    	return ActiveFilter.FilterType.FILTERGROUP;
    }

	public void resetCount() {		
		for (final SelectableActiveFilter o : this) {
			o.resetCount();
		}
	}

	public int getSelectedCount() {
		int n = 0;
		for (final SelectableActiveFilter a : this) {
			if (a instanceof SelectableActiveFilterGroup) {
				n += ((SelectableActiveFilterGroup) a).getSelectedCount();
			} else {
				if (a.isSelected()) {
					n++;
				}
			}
		}
		return n;
	}

	public void setTableModel(final PersonalizableTableModel tableModel) {
		for (final SelectableActiveFilter fcb : this) {
			fcb.setTableModel(tableModel);
		}
	}
	
	public void incrementCount() {
		for (final SelectableActiveFilter o : this) {
			o.incrementCount();
		}
	}
	public void updateCount() {
		for (final SelectableActiveFilter o : this) {
			o.updateCount();
		}
	}
    public SelectableActiveFilterGroup getTopGroup(){
    	return null;
    }   

}