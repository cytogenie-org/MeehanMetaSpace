package com.MeehanMetaSpace.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.MeehanMetaSpace.Basics;

public abstract class FilterCheckBox extends JCheckBox implements VagueWindowComponent, SelectableActiveFilter {

    public SelectableActiveFilterGroup getTopGroup(){
    	return topGroup;
    }

	protected abstract boolean matches(final Row row);

	public boolean describeCriteriaFailures(final Row row, final Collection<String> criteriaFailureDescriptions){
    	if (!meetsCriteria(row) ){
            criteriaFailureDescriptions.add(getText());
            return false;
        }
        return true;
    }

	public boolean meetsCriteria(final Row row) {
		final boolean value=matches(row);
		if (value) {
			cnt++;			
		}
		return value;		
	}
	
	protected PersonalizableTableModel tableModel;
	protected JButton pickButton;
	private SelectableActiveFilterGroup topGroup;
	private final boolean defaultState;
	private final String property;
	private boolean hiddenFilter = false;

	public boolean isHiddenFilterSelectedInThisGroup() {
		if (topGroup != null && topGroup.getHiddenFilter() != null && topGroup.getHiddenFilter().isSelected()) {
			return true;
		}
		return topGroup.getHiddenFilter() == null ;

	}
	public void associateTopGroup(final SelectableActiveFilterGroup topGroup) {
		this.topGroup = topGroup;
	}

	public boolean isHiddenFilter() {
		return hiddenFilter;
	}
	
	protected FilterCheckBox(final String label, final String toolTip, final boolean defaultState,
			final String property, final SelectableActiveFilterGroup group, final boolean isHidden) {
		addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) { // optional to
																// override
				refresh(true);
				if (tableModel != null) {
					if (!hiddenFilter) {
						tableModel.setProperty(property, isSelected());
					}						
					else {
						tableModel.setProperty(property, false);
					}
					tableModel.notifyViewChanged();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tableModel.clearSelection();
							if (pickButton != null) {
								pickButton.setEnabled(false);
							}
						}
					});
				}
				if (hiddenFilter) {
					if (isSelected()) {
						tableModel.getModelShowing().showActiveColumn();
					}
					else {
						tableModel.getModelShowing().hideActiveColumn();
						tableModel.getModelShowing().notifyViewChanged();
					}
				}
			}
		});
		this.topGroup = group;
		this.property = property;
		this.defaultState = defaultState;
		if (group != null) {
			group.add(this);
		}
		if (isHidden) {
			group.setHiddenFilter(this);
			hiddenFilter = true;
		}
		setSelected(defaultState);
		setText(label);
		setToolTipText(Basics.toHtmlUncentered(label, toolTip));
	}
		
	private int cnt=0;
	
	public void resetCount() {
		cnt=0;
	}
	public void incrementCount() {
		cnt++;
	}
	
	public void updateCount() {
		updateCount(cnt);
	}
	
	public void updateCount(final int n) {
		final String label = getText();
		if (PersonalizableTableModel.getTreeShowCounts()) {
			final String end = "</sup></html>";
			if (!label.endsWith(end)) {
				setText("<html><b>" + label + "</b><sup>" + n + end);
			}
		} else {
			setText("<html><b>" + label + "</b></html>");
		}
	}

	public void setTableModel(final PersonalizableTableModel tableModel) {
		this.tableModel = tableModel;
		final boolean b = tableModel.getProperty(property, defaultState);
		setSelected(b);
	}
	
	// AKWARD single-tainer logic ... sigh -deadline pinch for june 2007
	// the usual case if that these filter actions are created before the
	// pick list and then passed to the pick list constructor
	public void setTableModel(final PersonalizableTableModel tableModel, final boolean fullRefresh) {
		for (final SelectableActiveFilter fcb : topGroup) {
			fcb.setTableModel(tableModel);
		}
		refresh(fullRefresh);
	}
	
	public void setActionButton(final JButton button) {
		pickButton = button;
	}

	protected boolean meetsAllCriteria(final Row row) {
		if (topGroup != null) {
			return topGroup.meetsCriteria(row);
		} else { // singleton
			return meetsCriteria(row);
		}

	}

	public void refreshGroupFromProperties() {
		for (final SelectableActiveFilter fcb : topGroup) {
			if (fcb instanceof FilterCheckBox) {
				((FilterCheckBox) fcb).refreshFromProperties();
			}
		}
	}

	private void refreshFromProperties() {
		final boolean b = tableModel.getProperty(property, defaultState);
		setSelected(b);
	}

	public void refresh(final boolean full) {
		if (tableModel != null) {
			// basic reset
			if (!tableModel.getDataSource().hasActiveFilter(getText())) {
				tableModel.setActiveFilter(getText(), new ActiveFilter() {
					public boolean meetsCriteria(final Row row) {
						return meetsAllCriteria(row);
					}
					public boolean describeCriteriaFailures(final Row row, final Collection<String> criteriaFailureDescriptions){
						if (topGroup != null) {
							return topGroup.describeCriteriaFailures(row,criteriaFailureDescriptions);
						} else { // singleton
							return describeCriteriaFailures(row,criteriaFailureDescriptions);
						}
				    }
					public ActiveFilter.FilterType getType() {
				    	return ActiveFilter.FilterType.FILTERGROUP;
				    }
					
				    public SelectableActiveFilterGroup getTopGroup(){
				    	return topGroup;
				    }   


				});
			}
			if (full) {
				tableModel.clearSeeOnlySettings();
				tableModel.refreshLater(false);
			} else {// probably because starting pick list now
				tableModel.resetDisabledCache();				
			}
		}
	}

	public boolean closeOnClick() {
		return false;
	}

	public boolean enableWhenSelected() {
		return false;
	}

	public JComponent getComponent() {
		return this;
	}

	public static void setTableModel(final VagueWindowComponent[] components,
			final PersonalizableTableModel tableModel, final boolean fullRefresh) {
		DefaultPersonalizableDataSource.delayActiveFilterApplying=true;
		for (final VagueWindowComponent o : components) {
			if (o instanceof FilterCheckBox) {
				((FilterCheckBox) o).setTableModel(tableModel, fullRefresh);
			}
		}
		DefaultPersonalizableDataSource.delayActiveFilterApplying=false;
		tableModel.getDataSource().applyActiveFilters();
	}
	
	public static void setActionButton(final VagueWindowComponent[] components,
			final JButton button) {
		for (final VagueWindowComponent o : components) {
			if (o instanceof FilterCheckBox) {
				((FilterCheckBox) o).setActionButton(button);
			}
		}
	}

}
