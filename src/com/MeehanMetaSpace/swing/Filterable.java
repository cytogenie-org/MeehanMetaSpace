package com.MeehanMetaSpace.swing;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public interface Filterable {
	boolean filters(Object This, Object That, int filteringIdx);
	void setColumnOp(int op, int dataIndex);

	String opNone=" ", opEquals="is", opNotEquals="is not", opContains="contains",
		  opNotContains="not contains", opEndsWith="ends with", opStartsWith="starts with",
		  opNotEndsWith="not ends with", opNotStartsWith="not starts with",
		  opLesser="<", opLesserEquals="<=", opGreater=">", opGreaterEquals=">=";
	String[] comparableBooleanFilterOp = { opNone, opEquals, opNotEquals};
	String[] canNotFilterOp = { opNone};

	String[] stringFilterOp = { opNone, opEquals, opNotEquals, opContains, opNotContains,
		opEndsWith, opNotEndsWith, opStartsWith, opNotStartsWith,
		opGreater, opGreaterEquals, opLesser, opLesserEquals};
	String[] buttonFilterOp={ opNone, opEquals, opNotEquals};
	String[] numericFilterOp={ opNone, opEquals, opNotEquals, opGreater, opGreaterEquals, opLesser, opLesserEquals};
	int none=0,
		equals=1,
		notEquals=2,
		greater=3,
		greaterEquals=4,
		lesser=5,
		lesserEquals=6,
		contains=7,
		notContains=8,
		endsWith=9,
		notEndsWith=10,
		startsWith=11,
		notStartsWith=12;

	String[] allFilterOp = {
		opNone,
		opEquals,                   //0
		opNotEquals,                //1
		opGreater,                //2
		opGreaterEquals,           //3
		opLesser,                 //4
		opLesserEquals,            //5
		opContains,             //6
		opNotContains,          //7
		opEndsWith,             //8
		opNotEndsWith,          //9
		opStartsWith,           //10
		opNotStartsWith         //11
	};


}