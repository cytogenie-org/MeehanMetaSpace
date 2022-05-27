
/*
=====================================================================

  ConditionTableModel.java

  Created by Stephen Meehan
  Copyright (c) 2002

 =====================================================================
*/
package com.MeehanMetaSpace.swing;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import com.MeehanMetaSpace.*;


public class ConditionMetaRow extends DefaultMetaRow {

public String getKey(){
		return "Annotated condition";
	}


	protected ConditionMetaRow(Column [] columns){
		super( concatenate(ConditionMetaRow.columns, columns) );
	}

	public ConditionMetaRow(){
		super ( columns );
	}

	protected static final Column CONDITION=new Column("Condition", Condition.class) {
		protected Object get(Object rowObject) {
			return ( (Condition.Annotated) rowObject).condition;
		}

		public boolean setAdvice(Object rowObject, CellAdvice cellAdvice) {
			Condition condition=( (Condition.Annotated ) rowObject).condition;
			if ( condition.compareTo(Condition.WARNING)>0){
				cellAdvice.set(CellAdvice.TYPE_ERROR, "Condition="+condition.toString());
				return true;
			}
			return false;

		}

	};

	protected static final Column MESSAGE=new Column("Message", String.class) {
		protected Object get(Object rowObject) {
			return ( (Condition.Annotated) rowObject).annotation;
		}
	};

  public int getConditionDataColumnIndex(){
	return getDataColumnIndex(CONDITION);
  }

  public int getMessageDataColumnIndex(){
	return getDataColumnIndex(MESSAGE);
  }

	static Column [] columns={
		 CONDITION,
		 MESSAGE
	};


	class DataSource  extends DefaultMetaRow.DataSource {
		protected DataSource () {
			super();
		}
		public DefaultMetaRow.Row [] create(Condition.Annotated anomaly) {
			return super.create(anomaly);
		}
	}
	/**
	 * Override if you need a new subclass to ConditionMetaRow.DataSource
	 *
	 * @return data source for rows of meta row
	 * */
	 protected DefaultMetaRow.DataSource newDataSource() {
		 return new DataSource();
	 }

}
