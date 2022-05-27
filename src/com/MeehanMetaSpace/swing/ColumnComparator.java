/*
=====================================================================

  ColumnComparator.java

  Created by Stephen Meehan
  Copyright (c) 2002

=====================================================================
*/
package com.MeehanMetaSpace.swing;
import java.util.*;

public class ColumnComparator
  implements Comparator<Row>
{
	private final SortInfo [] sortInfoArray;

	public ColumnComparator(final MetaRow metaRow, final int []dataColumns){
		this(SortInfo.New(metaRow, dataColumns));
	}
	

	public ColumnComparator(final SortInfo [] sortInfoArray)  {
		this.sortInfoArray=sortInfoArray;
	}

	public int compare(Row one, Row two)  {
		if (one instanceof Row && two instanceof Row){
			int result=0;
			for (int i=0; result==0 && i<sortInfoArray.length;i++){
				final boolean ascending=sortInfoArray[i].ascending;
				final Object leftSideValue=sortInfoArray[i].getSortValue(one);
				final Object rightSideValue=sortInfoArray[i].getSortValue(two);
				if (leftSideValue instanceof Comparable && rightSideValue instanceof Comparable) {
					if (leftSideValue instanceof String && rightSideValue instanceof String){
						result = ascending ?
							   ((String)leftSideValue).compareToIgnoreCase( (String) rightSideValue )  :
							   ((String)rightSideValue).compareToIgnoreCase( (String) leftSideValue );
						continue;
					}
					if (ascending){
						result = ((Comparable)leftSideValue).compareTo((Comparable)rightSideValue);
					} else {
						result = ((Comparable)rightSideValue).compareTo((Comparable)leftSideValue);
					}
				}
				else if (leftSideValue instanceof Comparable){
					result= ascending ? -1 : 1;
				}
				else if (rightSideValue instanceof Comparable){
					result=ascending ? 1 : -1;
				}
				else {
					result=0;
				}
			}
			return result;
		}
		return 1;
  }
}

