
package com.MeehanMetaSpace;
import java.util.*;
import java.text.DateFormat;

/**
 * Title:        Meehan Meta Space Software
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Stephen Meehan
 * @version 1.0
 */

public final class DateTime implements Comparable{
  final static Locale locale=new Locale("en", "US");

	static DateFormat df=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
	public static DateFormat getDateFormat()	{
		return df;
	}

	public static void setDateFormat(final DateFormat df) {
		DateTime.df = df;
	}

	final Date date;

	public DateTime(final long secs){
		this.date= new Date(secs);
	}

	public int compareTo(final Object o){
		if (o instanceof DateTime){
			Date otherDate= ((DateTime)o).date;
			return date.compareTo(otherDate);
		}
		return -1;
	}
	public boolean equals (final Object o){
		if (o == this){
			return true;
		}
		if (o instanceof DateTime){
			Date otherDate= ((DateTime)o).date;
			return date.equals(otherDate);
		}
		return false;
	}

	public int hashCode(){
		return date.hashCode();
	}

	public String toString(){
		return date.toString();
	}

	public Date dateValue(){
		return new Date(date.getTime());
	}
}
