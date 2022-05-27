package com.MeehanMetaSpace.swing;



public interface SelectableActiveFilter extends ActiveFilter{
	  boolean isSelected();
	  boolean isHiddenFilter();
	  void incrementCount();
	  void resetCount();
	  void updateCount();
	  void associateTopGroup(SelectableActiveFilterGroup topGroup);
	  void setTableModel(PersonalizableTableModel tableModel);
	  
}

