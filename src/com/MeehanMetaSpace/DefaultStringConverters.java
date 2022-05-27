package com.MeehanMetaSpace;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.util.*;
import java.text.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class DefaultStringConverters {
	// global policy
	private final static Map<Class, StringConverter> registry=new HashMap<Class, StringConverter> ();

	public static Object toObject(final Class cl, final String value)
	{
		Object returnValue=value;
		if (value != null){
			final StringConverter sc=registry.get(cl);
			if (sc!=null) {
				try{
					returnValue=sc.toObject(value);
				}catch (Exception e){
					Pel.log.warn(e);
				}
			}
		}
		return returnValue;
	}

	public static String toString(final Object value)
	{
		return toString(value.getClass(), value);
	}

	public static String toString(final Class cl, final Object value)
	{
		String returnValue="";
		if (value != null){
			final StringConverter sc=registry.get(cl);
			if (sc!=null) {
				try{
					returnValue=sc.toString(value);
				}catch (Exception e){
					Pel.log.warn(e);
				}
			}
			else {
				returnValue=value.toString();
			}
		}
		return returnValue;
	}

	public static class _Integer implements StringConverter{
		public String toString(final Object input){
			return input == null ? "":Basics.encode(input);
		}

		public Object toObject(final String input)	throws Exception {
			if (Basics.isEmpty(input)){
				return null;
			}
			return Basics.decode(input, Integer.class);
		}
		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Enter a whole number";
		}
	}

	public static class _Long extends _Integer{

		public Object toObject(final String input) throws Exception {
			if (Basics.isEmpty(input)){
				return null;
			}
			return Basics.decode(input, Long.class);
		}
	}

	public static class _Condition implements StringConverter{
		public String toString(Object input){
			return input == null ? "":input.toString();
		}

		public Object toObject(String input)	throws Exception {
			Condition retVal;
			if (Basics.isEmpty(input)){
				retVal=null;
			}else{
				retVal = Condition.find(input);
			}
			return retVal;
		}
		public int getHorizontalAlignment(){
			return JTextField.LEFT;
		}

		public String getFormatTip()
		{
			return "Enter a condition";
		}
	}

	public static void register(final Class cl, final StringConverter tc)
	{
		registry.put(cl, tc);
	}

	public static StringConverter get(final Class cl)	{
		StringConverter sc= registry.get(cl);
		if (sc == null) {
			sc=scDefault;
			Class prior=null;
			for (final Class key:registry.keySet()) {
				if (key.isAssignableFrom(cl)) {
					if (prior == null || prior.isAssignableFrom(key)) {
						prior=key;
					sc=registry.get(key);
					}
					
				}
			}
		}
		return sc;
	}

	public static class _Float implements StringConverter{
		public String toString(Object input){
			return input == null ? "": Basics.encode(input);
		}

		public Object toObject(final String input) throws Exception {
			if (Basics.isEmpty(input)){
				return null;
			}
			return Basics.decode(input, Float.class);
		}

		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Enter a floating point number";
		}
	}

	public static class _Date implements StringConverter{
		boolean stringToDate=false;
		final DateFormat df;
		final String format;
		public _Date(String format){
			this.format=format;
			this.df=new SimpleDateFormat(format);
		}

		public String toString(Object input){
			String txt;
			try{
				if (input instanceof String){
					stringToDate=true;
					try{
						txt = GmtFormat.formatToAlternate(df, (String) input);
					} catch (Exception e) {
						txt=df.format(new Date(Long.parseLong( (String) input)));
					}
				} else {
					if (input==null){
						txt="";
					}else{
						txt = df.format( (Date) input);
					}
				}
			}catch (Exception e){
				txt="";
				Pel.log.warn(e);
			}
			return txt;
		}

		public Object toObject(String input)	throws Exception {
			Date retVal;
			if (Basics.isEmpty(input)){
				retVal=null;
			}else{
				retVal = df.parse(input);
			}
			return retVal;
		}

		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Enter date using format:  ";
		}
	}

	public static class _JButton implements StringConverter{
		public static Map<String,JButton>buttons=new HashMap<String, JButton>();
		public String toString(Object input){
			if (input instanceof JButton){
				final String txt=( (JButton) input).getName();
				if (txt!=null){
					buttons.put(txt.toLowerCase(), (JButton)input);
					return txt;
				}
			}
			return "";
		}

		public Object toObject(String input)	throws Exception {
			if (input != null){
				return buttons.get(input.toLowerCase());
			}
			return null;
		}
		
		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Enter date using format:  mm/dd/yyyy";
		}
	}

	public static class _DateTime implements StringConverter{

		public String toString(Object input){
			String txt;
			try{
				txt=DateTime.df.format( ( (DateTime)input).date);
			}catch (Exception e){
				txt="";
				Pel.log.warn(e);
			}
			return txt;
		}

		public Object toObject(String input)	throws Exception {
			Date retVal;
			if (Basics.isEmpty(input)){
				retVal=null;
			}else{
				retVal = DateTime.df.parse( input);
			}
			return retVal;
		}
		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Enter date using format:  mm/dd/yyyy";
		}
	}

	public static class _String implements StringConverter{

		public String toString(final Object input){
			if (input != null) {
				return input.toString() ;				
			}
			return null;
		}

		public Object toObject(String input)	throws Exception {
			if (input instanceof String){
				return input;
			}
			Pel.log.print(Condition.WARNING, "Can not convert to object");
			return input;
		}

		public int getHorizontalAlignment(){
			return JTextField.LEFT;
		}

		public String getFormatTip()
		{
			return "";
		}
	}

	public static class Dflt implements StringConverter{

		public String toString(final Object input){
			return input == null ? null : input.toString() ;
		}

		public Object toObject(String input)	throws Exception {
			if (input instanceof String){
				return input;
			}
			Pel.log.print(Condition.WARNING, "Can not convert to object");
			return input;
		}

		public int getHorizontalAlignment(){
			return JTextField.LEFT;
		}

		public String getFormatTip()
		{
			return "";
		}
	}

	private final static Dflt scDefault=new Dflt();
	public static StringConverter get(){
		return scDefault;
	}

	static {
		register(String.class, new _String());
	}


	static {
		register(Integer.class, new _Integer());
	}

	static {
		register(Long.class, new _Long());
	}

	static {
		register(Condition.class, new _Condition());
	}

	static {
		register(Float.class, new _Float());
	}

	static {
		register(Date.class, new _Date("M/d/yy"));
	}

	static {
		register(JButton.class, new _JButton());
	}
	static {
		register(DateTime.class, new _DateTime());
	}

	public static class _Boolean implements StringConverter{

		public String toString(Object input){
			return input == null ? "":input.toString();
		}

		public Object toObject(String input)	throws Exception {
			Boolean retVal;
			if (Basics.isEmpty(input)){
				retVal=null;
			}else{
				retVal = Boolean.valueOf(input);
			}
			return retVal;
		}
		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Click true/false";
		}
	}

	static {
		register(Boolean.class, new _Boolean());
	}

	public static class _ComparableBoolean implements StringConverter{

		public String toString(Object input){
			return input == null ? "": input.toString();
		}

		public Object toObject(String input)	throws Exception {
			Boolean retVal;
			if (Basics.isEmpty(input)){
				retVal=null;
			}else{
				retVal = Boolean.valueOf(input);				
				return ComparableBoolean.valueOf( retVal.booleanValue());
			}
			return null;
		}

		public int getHorizontalAlignment(){
			return JTextField.CENTER;
		}

		public String getFormatTip()
		{
			return "Click true/false";
		}
	}

	static {
		register(ComparableBoolean.class, new _ComparableBoolean());
	}
	public static class _Object implements StringConverter{

		public String toString(Object input){
			return input == null ? "":input.toString();
		}

		public Object toObject(String input) throws Exception {
			return input;
		}

		public int getHorizontalAlignment(){
			return JTextField.LEFT;
		}

		public String getFormatTip()
		{
			return "Enter a value.";
		}
	}


	static {
		register(Object.class, new _Object());
	}
}

