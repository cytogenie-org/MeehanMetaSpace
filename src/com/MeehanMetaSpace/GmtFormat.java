package com.MeehanMetaSpace;
import java.io.*;
import java.util.*;
import java.text.*;

public class GmtFormat  {
	private static final String FORMAT_PATTERN = "yyyy.MM.dd HH:mm:ss.SSS zzz";

	private static DateFormat gmtFormat=new SimpleDateFormat(FORMAT_PATTERN);

	public static String format(final Date date){
		return date != null ? gmtFormat.format(date):null;
	}

	public static void setGmtFormat(final DateFormat gmt){
		GmtFormat.gmtFormat=gmt;
	}

	public static Date parse(final String dateInGmtFormat){
		if (dateInGmtFormat!=null){
			try {
				return gmtFormat.parse(dateInGmtFormat);
			}
			catch (final Exception pe) {
				com.MeehanMetaSpace.Pel.log.warn(pe);
			}
		}
	  return null;
  }

	public static String formatToAlternate(
			final DateFormat alternateFormat,
			final String dateInGmtFormat){
		if (dateInGmtFormat!=null){
			try {
				return alternateFormat.format(gmtFormat.parse(dateInGmtFormat));
			}
			catch (ParseException pe) {
				com.MeehanMetaSpace.Pel.log.warn(pe);
			}
		}
	  return null;
  }

  public static String formatGmt(
		 final DateFormat alternateFormat,
		 final String dateInAlternateFormat){
	  if (dateInAlternateFormat!=null){
		  try {
			  return gmtFormat.format(alternateFormat.parse(dateInAlternateFormat));
		  }
		  catch (ParseException pe) {
			  com.MeehanMetaSpace.Pel.log.warn(pe);
		  }
	  }
	 return null;
 }


}


