package com.MeehanMetaSpace;

import java.util.*;

public class Abbreviation implements Comparable{
	public int compareTo(final Object other) {
		if (other instanceof Abbreviation) {
		return value.compareTo(((Abbreviation)other).value);
		}
		return -1;
	}
	public static int MINIMUM_LENGTH = 2;
	private int minimumLength = MINIMUM_LENGTH;
	private final String[] words;
	private int ii = 0;
	private final String value;
	private final String[] abs;
	private int[] abi;

	public void setMinimumLength(final int minimumLength) {
		this.minimumLength = minimumLength;
	}

	public Abbreviation(final String value) {
		this.value = value;
		if (value.equals(Basics.NULL)){
			words=new String[]{"--"};
		}else {
			words = value.split("[,\\s]+");
		}
		abs = new String[words.length];
		abbreviate();
	}
	private String abbreviation, overridenAbbreviation;
	public boolean canOverrideAbbreviation(final String overriddenAbbreviation, final Collection<Abbreviation>nameSpace) {
		for (final Abbreviation ab:nameSpace) {
			if ( !Basics.equals(value, ab.value) && Basics.equals(overriddenAbbreviation, ab.abbreviation)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean overrideAbbreviation(final String overriddenAbbreviation, final Collection<Abbreviation>nameSpace) {
		if (overriddenAbbreviation == null) {
			this.overridenAbbreviation=null;
			reset();
			abbreviate();		
			return true;
		}
		if (nameSpace==null||canOverrideAbbreviation(overriddenAbbreviation, nameSpace)) {
			abbreviation=overriddenAbbreviation;
			return true;
		}
		return false;
	}
	
	private void abbreviate() {
		while (hasAbbreviationOptions()) {
			_abbreviate();
			final String s = computeAbbreviation();
			abbreviation=s;
			if (s.length() >= minimumLength) {				
				break;
			}
			
		}
	}
	private void reset() {
		abi=null;
	}
	private void _abbreviate() {
		if (abi == null) {
			abi = new int[words.length];
			for (int i = 0; i < abi.length; i++) {
				abi[i] = 1;
			}
		} else {
			abi[ii]++;
			ii++;
			if (ii == words.length) {
				ii = 0;
			}
		}
		for (int i = 0; i < words.length; i++) {
			if (abi[i] < words[i].length()) {
				abs[i] = words[i].substring(0, abi[i]);
			} else {
				abs[i] = words[i];
			}
		}
	}
	public String getAbbreviation() {
		return abbreviation;
	}
	private String computeAbbreviation() {
		final StringBuilder sb = new StringBuilder();
		for (final String ab : abs) {
			sb.append(ab);
		}
		return sb.toString().toLowerCase();
	}

	private boolean hasAbbreviationOptions() {
		if (overridenAbbreviation!=null) {
			return false;
		}
		for (int i = 0; i < words.length; i++) {
			if (!Basics.equalsIgnoreCase(abs[i], words[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean enforceUniqueness(final Collection<Abbreviation> all) {
		// first make sure all words are unique
		for (final Abbreviation ab1 : all) {
			for (final Abbreviation ab2 : all) {
				if (ab1 != ab2 && ab1.equals(ab2)) {
					return false;
				}
			}
		}

		// now make sure abbreviations are unique
		boolean done = false;
		for (; !done;) {
			done = true;
			for (final Abbreviation ab1 : all) {
				for (final Abbreviation ab2 : all) {
					if (ab1 != ab2 && ab1.isEquallyAbbreviated(ab2)) {
						done = false;
						boolean done2 = false;
						for (; !done2;) {
							if (!ab1.hasAbbreviationOptions() && ab2.hasAbbreviationOptions()) {
								ab2.abbreviate();
							} else if (ab1.hasAbbreviationOptions()){
								ab1.abbreviate();
							} else {
								boolean ok1=ab1.hasAbbreviationOptions(), ok2=ab2.hasAbbreviationOptions();
								return false;								
							}
							done2 = !ab1.isEquallyAbbreviated(ab2);
						}
					}
				}
			}
		}
		return true;
	}

	public int hashCode() {
		return value.hashCode();
	}

	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Abbreviation)) {
			return false;
		}
		return Basics.equals(value, ((Abbreviation) o).value);
	}

	public boolean isEquallyAbbreviated(final Abbreviation o) {
		if (abbreviation != null && o.abbreviation != null) {
			return o.abbreviation.equals(abbreviation);			
		}
		return false;
	}

	public String toString() {
		return value;
	}
	static MapOfMaps<String, String, Abbreviation> m=new MapOfMaps<String, String, Abbreviation>();
	static int count=0;
	public static void test(final String key, final Abbreviation[] a) {
		for (final Abbreviation _a : a) {
			System.out.print(_a);
			System.out.print("    ");
			System.out.println(_a.abbreviation);
			m.put(key, _a.value, _a);
		}
		System.out.println();
		final Collection<Abbreviation> c=m.values(key);
		final boolean ok = enforceUniqueness(c);
		Abbreviation found=null;
		if (ok) {
			for (final Abbreviation _a : a) {
				found=m.get(key, _a.value);
				System.out.print(_a);
				System.out.print("    ");
				System.out.println(_a.abbreviation);
			}
		} else {
			System.out.println("Uniqueness enforced? ok=" + ok);
		}
		System.out.println();

	}
	

	public static void main(final String[] args) {

		test("Low", new Abbreviation[] { new Abbreviation("how are youc"), new Abbreviation("how are you"),
				new Abbreviation("howareyou"), new Abbreviation("I am no nice"), new Abbreviation("how are youch"), });

		test("high", new Abbreviation[] { new Abbreviation("how are youc"), new Abbreviation("how are you"),
				new Abbreviation("howareyou"), new Abbreviation("I am no nice"), new Abbreviation("how are you"), });

	}
}
