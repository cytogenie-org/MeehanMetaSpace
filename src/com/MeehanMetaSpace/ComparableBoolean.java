package com.MeehanMetaSpace;

/**
 * Title:        Meehan Meta Space Software
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Stephen Meehan
 * @version 1.0
 */

public final class ComparableBoolean implements Comparable{
	final private Boolean b;

	private ComparableBoolean(boolean b){
		this.b= b? Boolean.TRUE: Boolean.FALSE;
	}

	public static ComparableBoolean
		 NO=new ComparableBoolean( false ),
		 YES=new ComparableBoolean( true );

	public static ComparableBoolean valueOf(boolean ok){

	  return ok ? YES : NO;
	}

	public int compareTo(Object o){
		if (o instanceof ComparableBoolean){
			Boolean otherB= ((ComparableBoolean)o).b;
			if (otherB.equals(this.b)){
				return 0;
			}
			if (b.equals(Boolean.FALSE)){
				return -1;
			}
		}
		return 1;
	}

	public boolean equals (Object o){
		if (o == this){
			return true;
		}
		if (o instanceof ComparableBoolean){
			Boolean otherB= ((ComparableBoolean)o).b;
			return otherB.equals(this.b);
		}
		return false;
	}

	public int hashCode(){
		return b.hashCode();
	}

	public Boolean toBoolean(){
		return b;
	}

	public String toString(){
		return b.toString();
	}

	public boolean booleanValue(){
		return b.booleanValue();
	}
}
