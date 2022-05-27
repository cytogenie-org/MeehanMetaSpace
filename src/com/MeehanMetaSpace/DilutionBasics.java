package com.MeehanMetaSpace;

import java.io.*;
import java.util.*;
import com.MeehanMetaSpace.*;
/**
 *
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 *
 * I hate this file so much, it is the consumate example of working
 * without knowing the requirements but thinking you know when *IT* has
 * to be done by.  Ahhhhhhhhhh
 *
 */
public class DilutionBasics {
    private static boolean valid=false;
public static String encodeFraction(final float fraction) {
	return encode(1 / fraction);
  }

  static String toString(final float f) {
	String retVal = "" + f;
	if (retVal.endsWith(".0") || retVal.endsWith(".00") ||
		retVal.endsWith(".000")) {
	  int idx = retVal.indexOf('.');
	  if (idx >= 0) {
		retVal = retVal.substring(0, idx);
	  }
	}
	return retVal;
  }

  public static String encode(final float numerator, final float denominator) {
	return recodeDilutionToNumeratorOfOne(toString(numerator) + SEPERATOR +
										  toString(denominator));
  }

  public static String encode(final String numerator, final String denominator) {
	return recodeDilutionToNumeratorOfOne(numerator + SEPERATOR + denominator);
  }

  /**
   *
   * @param dilutionDenominator the denominator where the numerator is one.
   * @return the string version of the dilution
   */
  private static String encode(final float denominator) {
		if (denominator > 1.0) {
			  return Basics.concat("   1", Basics.toString(SEPERATOR), toString( Basics.round(denominator,0)));
		}
		return Basics.concat("   1", Character.toString(SEPERATOR), toString(Basics.round(1 / denominator,0)));
  }

  public static String encode(final Float denominator) {
	return denominator == null ? null : encode(denominator.floatValue());
  }

  
  /**
  *
  * @param dilutionDenominator the denominator where the numerator is one.
  * @return the string version of the dilution
  */
 private static String encodeInteger(final float denominator) {
	if (denominator > 1.0) {
	  return Basics.concat("   1", Basics.toString(SEPERATOR), toString( Basics.round(denominator,0)));
	}
	return Basics.concat("   1", Character.toString(SEPERATOR), toString(Basics.round(1 / denominator,0)));
 }

 public static String encodeInteger(final Float denominator) {
	return denominator == null ? null : encodeInteger(denominator.floatValue());
 }

  public static boolean isValid(final String dilution) {
	final Float dilutionDenominator = decodeDilutionDenominator(
		dilution);
	if (dilutionDenominator == null) {
	  return false;
	}
	return true;
  }

  public static boolean validate(final String dilution) {
	if (!isValid(dilution)) {
	  DilutionBasics.advise();
	  return false;
	}
	return true;
  }

  private static boolean isInt(final String s) {
	final char[] c = s.toCharArray();
	for (int i = 0; i < c.length; i++) {
	  if (!Character.isDigit(c[i]) && c[i] != ' ' && c[i] != '.' && c[i] !=',') {
		return false;
	  }
	}
	return true;
  }

  public final static char[] DEPRECATED_SEPERATORS = new char[] {
	  ':'
  };
  public final static char SEPERATOR = '/';
  public final static String numeratorMask="****", denominatorMask="*****";
  public final static int SEPERATOR_IDX=numeratorMask.length();
  public final static String editMask = numeratorMask + SEPERATOR + denominatorMask;

  private static String padNumerator(final String numerator){
	if (numerator.length()<numeratorMask.length()){
	  final StringBuilder sb=new StringBuilder(numeratorMask.length());
	  final int n=numeratorMask.length()-numerator.length();
	  for (int i=0;i<n;i++){
		sb.append(' ');
	  }
	  sb.append(numerator);
	  return sb.toString();
	}
	return numerator;

  }

public static String reformat(final String dilutionRatioText){
  final float []f=decodeDilution(dilutionRatioText);
  if (f != null){
	final String numerator=padNumerator( Basics.encode(f[0]));
	return numerator + SEPERATOR + toString(f[1]);
  }
  return getNullRatioText();
}

  private static float []decodeDilution(
	  final String dilutionRatioText) {
	float []retval = null;
	if (dilutionRatioText != null) {
	  int seperatorIdx = dilutionRatioText.indexOf(SEPERATOR);
	  if (seperatorIdx < 0) {
		for (int i = 0; i < DEPRECATED_SEPERATORS.length; i++) {
		  seperatorIdx = dilutionRatioText.indexOf(DEPRECATED_SEPERATORS[i]);
		  if (seperatorIdx >= 0) {
			break;
		  }
		}

	  }
	  String n1 = "";
	  String n2 = "";
	  if (seperatorIdx >= 0 && seperatorIdx != dilutionRatioText.length() - 1) {
		n1 = dilutionRatioText.substring(0, seperatorIdx);
		n2 = dilutionRatioText.substring(seperatorIdx + 1);
		if (isInt(n1) && isInt(n2)) {
		  try {
			retval =new float[2];
			retval[0] = (Float)Basics.decode(n1.trim(), Float.class);
			retval[1] = (Float)Basics.decode(n2.trim(), Float.class);
            if (retval[0] <= 0.0)
                valid = false;
            else
                valid = true;
		  }
		  catch (final Exception e) {
			Pel.log.warn(e);
		  }
		}
	  }
	}
	return retval;
  }

  private static final String UNDILUTED_RATIO_TEXT=
	  "   1" +
	SEPERATOR +
	"1    ";
  public static String getUndilutedRatioText() {
	return UNDILUTED_RATIO_TEXT;
  }

  public static float getUndilutedRatio() {
	return 1;
  }

  private final static String NULL_RATIO_TEXT="   1" +
	SEPERATOR +
	"0    ";
  
  public static String getNullRatioText() {
	return NULL_RATIO_TEXT;
  }

  public static float getNullRatio() {
	return 0;
  }

  public static final String advice =Basics.toHtmlUncentered("Dilution format",
	  "Enter a fraction (e.g. 1" +
	  SEPERATOR +
	  "5 means 1 volume of reagent *IN* 5 volumes total) <br>1" +
	  SEPERATOR +
	  "1 means UNDILUTED");
  public static void advise() {
	Basics.gui.alertAsync(advice, true);
  }

  /**
   * Converts the dilution fraction so that the numerator is one, and then returns
   * the denominator.
   *
   * @param dilutionFractionText
   * @return determines the dilutio denominator
   */
  public static Float decodeDilutionDenominator(final String dilutionRatioText) {
	final float []f=decodeDilution(dilutionRatioText);
	if (f != null){
	  return new Float(f[1] / f[0]);
	}
	return null;
  }

  public static Float decodeDilutionRatio(final String dilutionRatioText, final boolean zeroIfNull) {
	final Float retVal;
	final Float n = decodeDilutionDenominator(dilutionRatioText);
	if (n != null) {
	  retVal = new Float(1.0 / n.floatValue());
	}
	else {
	  if (!zeroIfNull){
		retVal = null;
	  } else {
		return new Float(0);
	  }
	}
	return retVal;
  }

  public static String recodeDilutionToNumeratorOfOne(
	  final String dilutionRatioText) {
	return encode(decodeDilutionDenominator(dilutionRatioText));
  }

  public static String incrementDenominator(
	  final String dilutionRatioText,
	  final int step){
	Float f=decodeDilutionDenominator(dilutionRatioText);
	if (f == null){
	  f=new Float(1/25);
	}
	if(f == 0.0){
		return encode(step);
	}else{
	  return encode(f.floatValue() * step);
	}
  }

  public static boolean isValid() {
      return valid;
  }
  
  private static boolean isBad(final float f){
	  return f==0 || Float.NaN==f;
  }
  
  public static boolean isBadDenominator(final String txt){
	  final float []f=decodeDilution(txt);
	  if (f != null && f.length==2){
		  if (isBad(f[1])){
			  return true;
		  }
		  return false;
	  }
	  return false; // don't know
  }
  
  public static boolean isBadNumerator(final String txt){
	  final float []f=decodeDilution(txt);
	  if (f != null && f.length==2){
		  if (isBad(f[0])){
			  return true;
		  }
		  return false;
	  }
	  return false; // don't know
  }
  
  public static void main(final String[]args){
	  String txt=encode(0.1f);
	  System.out.println(txt);
  }
}
