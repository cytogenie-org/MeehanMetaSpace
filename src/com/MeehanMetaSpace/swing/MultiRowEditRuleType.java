package com.MeehanMetaSpace.swing;

import java.util.*;

import com.MeehanMetaSpace.*;

//Ordinal base typesafe enum
public abstract class MultiRowEditRuleType
	implements Comparable{

  static final int ROOT_IS_SELECTED= -1;
  private final String name;
  private static int nextOrdinal=0;

  public static final MultiRowEditRuleType SELECTED_AND_TOP_OF_SORT=
	  new MultiRowEditRuleType("selected and top of sort"){

	boolean isApplicable(
		final PersonalizableTableModel.MultiRowEditRule rule,
		final Collection rows){
	  return true;
	}

	boolean isApplicableToTreeSelection(final PersonalizableTableModel.
										MultiRowEditRule rule,
										final int[] dataColumnSortOrder,
										final GroupedDataSource.Node[]
										selectedNodes,
										final int[] selectedDataColumns){
	  if (!isTopOfSortOrNotSelected(dataColumnSortOrder,
									rule.ifAnyOfTheseColumns,
									selectedDataColumns)){
		return false;
	  }
	  for (int i=0; i < selectedDataColumns.length; i++){
		if (selectedDataColumns[i] != ROOT_IS_SELECTED
			&&
			!Basics.equalsAny(rule.ifAnyOfTheseColumns, selectedDataColumns[i])){
		  return false;
		}
	  }
	  return true;
	}
  };

  public static final MultiRowEditRuleType SELECTED_OR_COMMON_PARENT=
	  new MultiRowEditRuleType("selected OR common parent"){

	boolean isApplicable(
		final PersonalizableTableModel.MultiRowEditRule rule,
		final Collection rows){
	  return true;
	}

	boolean isApplicableToTreeSelection(
		final PersonalizableTableModel.MultiRowEditRule rule,
		final int[] dataColumnSortOrder,
		final GroupedDataSource.Node[] selectedNodes,
		final int[] selectedDataColumns){
	  final Object[] commonParent=new Object[rule.ifAnyOfTheseColumns.length];
	  if (!MultiRowEditRuleType.isHigherOrEqualInSortOrder(
		  dataColumnSortOrder,
		  rule.ifAnyOfTheseColumns,
		  selectedDataColumns)){
		return false;
	  }
	  for (int i=0; i < selectedNodes.length; i++){
		final Row row=selectedNodes[i].getFirstUngroupedRow();
		if (row != null){
		for (int j=0; j < rule.ifAnyOfTheseColumns.length; j++){
		  final Object parent=row.get(rule.ifAnyOfTheseColumns[j]);
		  if (commonParent[j] == null){
			commonParent[j]=parent;
		  }
		  if (!commonParent[j].equals(parent)){
			return false;
		  }
		}
		}
	  }
	  return true;
	}
  };

  public static final MultiRowEditRuleType ANYTHING_GOES=new
	  MultiRowEditRuleType("any thing goes"){
	boolean isApplicable(
		final PersonalizableTableModel.MultiRowEditRule rule,
		final Collection rows){
	  return true;
	}

	boolean isApplicableToTreeSelection(
		final PersonalizableTableModel.MultiRowEditRule rule,
		final int[] dataColumnSortOrder,
		final GroupedDataSource.Node[] selectedNodes,
		final int[] selectedDataColumns){
	  return true; // any thing goes !!
	}
  };

  private final int ordinal=nextOrdinal++;
  private MultiRowEditRuleType(String name){
	this.name=name;
  }

  public String toString(){
	return name;
  }

  public int compareTo(Object o){
	return ordinal - ((MultiRowEditRuleType) o).ordinal;
  }

  private static final MultiRowEditRuleType[] privateValues={
	  ANYTHING_GOES,
	  SELECTED_OR_COMMON_PARENT,
	  SELECTED_AND_TOP_OF_SORT
  };

  public static final List values=
	  Collections.unmodifiableList(Arrays.asList(privateValues));

  public static MultiRowEditRuleType asRecommendation(String name){
	for (int i=0; i < privateValues.length; i++){
	  if (privateValues[i].name.equals(name)){
		return privateValues[i];
	  }
	}
	return null;
  }

  public static MultiRowEditRuleType[] asRecommendation(Collection c){
	MultiRowEditRuleType[] array=new MultiRowEditRuleType[c.size()];
	int i=0;
	for (final Iterator it=c.iterator(); it.hasNext(); i++){
	  array[i]=asRecommendation((String) it.next());
	}
	return array;
  }

  static boolean isTopOfSortOrNotSelected(
	  final int[] sortOrder, // data columns
	  final int[] args,
	  final int[] selected
	  ){
	for (int i=0; i < args.length; i++){
	  final int arg=args[i];
	  int foundPosition=Integer.MAX_VALUE;
	  for (int j=0; foundPosition == Integer.MAX_VALUE && j < sortOrder.length;
		   j++){
		if (sortOrder[j] == arg){
		  foundPosition=j;
		}
	  }
	  if (foundPosition >= args.length // not top of sort order
		  && Basics.equalsAny(selected, arg) // and IS selected
		  ){
		return false;
	  }
	}
	return true;
  }

  static boolean isHigherOrEqualInSortOrder(
	  final int[] sortOrder,
	  final int[] dataColumnsThatMustBeHigher,
	  final int[] selectedDataColumns){

	int[] mustBeHigher=new int[dataColumnsThatMustBeHigher.length];
	for (int i=0; i < dataColumnsThatMustBeHigher.length; i++){
	  mustBeHigher[i]=Basics.indexOf(sortOrder, dataColumnsThatMustBeHigher[i]);
	  if (mustBeHigher[i] < 0){ // is mustBeHigher EVEN in sort order ?
		return false;
	  }
	}
	for (int i=0; i < selectedDataColumns.length; i++){
	  if (selectedDataColumns[i] != -1 /* root*/){
		int idx=Basics.indexOf(sortOrder, selectedDataColumns[i]);
		// must be lower than all mustBeHigher
		for (int j=0; j < mustBeHigher.length; j++){
		  if (idx < mustBeHigher[j]){ // selected data column is NOT lower OR equal?
			return false;
		  }
		}
	  }
	}
	return true;

  }

  abstract boolean isApplicable(
	  final PersonalizableTableModel.MultiRowEditRule rule,
	  final Collection rows);

  abstract boolean isApplicableToTreeSelection(
	  final PersonalizableTableModel.MultiRowEditRule rule,
	  final int[] dataColumnSortOrder,
	  final GroupedDataSource.Node[] selectedNodes,
	  final int[] selectedDataColumns);

}
