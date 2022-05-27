package com.MeehanMetaSpace;

import java.util.regex.*;
import java.util.*;

public class SearchAndReplace {
	private final Pattern matchPattern, replacePattern;

	public static String escapeMetaCharacters(final String literal) {
		final String metaCharacters = "|?+^$\\()[].";
		final StringBuilder sb = new StringBuilder();
		final char[] cha = literal.toCharArray();
		for (final char ch : cha) {
			// ignore glob
			if (ch != '*' && metaCharacters.indexOf(ch) >= 0) {
				sb.append('\\');
			}
			sb.append(ch);
		}
		return sb.toString();
	}
	
	public SearchAndReplace(final String searchEntry, boolean encodeXmlOrHtmlReservedCharacters){
		this(searchEntry, COMMA_PATTERN, true, encodeXmlOrHtmlReservedCharacters);
	}
	
	public final String searchEntry;
	private MapOfMany<Integer, Integer> plusGroups = null;
	private boolean hasPlusAsAnd = false;

	public boolean isUsingPlusAsAnd() {
		return hasPlusAsAnd;
	}

	final List<String> tokens = new ArrayList<String>();

	public SearchAndReplace(final String searchEntry, final Pattern delimiter, final boolean supportPlusAsAnd, final boolean encodeHtmlOrXmlCharacters){
		this.searchEntry = searchEntry;
		final String[] rawTokens = delimiter.split(searchEntry);
		final int sz = searchEntry.length() + rawTokens.length;
		final StringBuilder rep=new StringBuilder(sz), mat=new StringBuilder(sz);
		rep.append("(");
		mat.append("(");
		boolean appendOr = false;
		for (String token : rawTokens) {
			if (appendOr) {
				mat.append('|');
				rep.append('|');
			}
			if (supportPlusAsAnd) {
				final ArrayList<String> c = splitPlus(token);
				if (c.size() > 1) {
					if (!hasPlusAsAnd) {
						hasPlusAsAnd = true;
						plusGroups = new MapOfMany<Integer, Integer>();
					}
					StringBuilder sb = new StringBuilder(), sb2 = new StringBuilder();
					sb.append('(');
					sb2.append('(');
					final int firstIdx = tokens.size();
					for (int i2 = 0; i2 < c.size(); i2++) {
						if (i2 > 0) {
							sb2.append("|");
						}
						final String l = escapeMetaCharacters(c.get(i2));
						sb.append("(?=.*");
						sb.append(handleGlobs(l));
						sb.append(")");
						sb2.append(handleGlobs(l));
						tokens.add(removeGlobs(l));
					}
					
					final int lastIdx = tokens.size() - 1;
					for (int j = firstIdx; j <= lastIdx; j++) {
						for (int k = firstIdx; k <= lastIdx; k++) {
							if (j != k) {
								plusGroups.put(j, k);
							}
						}
					}
					sb.append(')');
					String s = sb.toString();
					mat.append(s);
					
					sb2.append(')');
					s = sb2.toString();
					rep.append(s);
					appendOr = true;
					continue;
				} else if (c.size() == 1) {
					token = c.get(0);
				}
			}
			if (!"".equals(token) && !"*".equals(token)) {
				String s = escapeMetaCharacters(token);
				s = handleGlobs(s);
				tokens.add(s);
				rep.append(s);
				mat.append(s);
				appendOr = true;
			} else {
				appendOr = false;
			}
		}
		rep.append(")");
		mat.append(")");	
		final String regEx1, regEx2;
		if (encodeHtmlOrXmlCharacters) {
			regEx1 = Basics.encodeXmlOrHtml(rep.toString());
			regEx2 = Basics.encodeXmlOrHtml(mat.toString());
				
		} else {
			regEx1 = rep.toString();
			regEx2 = mat.toString();
		}
		replacePattern = Pattern.compile(regEx1, Pattern.CASE_INSENSITIVE);
		matchPattern=Pattern.compile(".*"+regEx2+".*", Pattern.CASE_INSENSITIVE);
	}
	
	protected List<String> getMatches(final String data) {
		final ArrayList<String> c = new ArrayList<String>();
		final List<Integer> l = getTokenIndexes(data);
		for (final int i : l) {
			c.add(tokens.get(i));
		}
		return c;
	}

	private List<Integer> getTokenIndexes(final String data) {
		final Collection<Integer> incompleteAndTokens = new ArrayList<Integer>();
		List<Integer> c;
		Pattern pat = this.replacePattern;
		String sPat = this.replacePattern.pattern();
		int n = 0;
		for(c=getTokes(pat, data,incompleteAndTokens);n<incompleteAndTokens.size(); c=getTokes(pat, data,incompleteAndTokens)) {
			n = incompleteAndTokens.size();
			boolean appendOr = false;
			final StringBuilder sb = new StringBuilder("(");
			for (int i = 0; i < tokens.size(); i++) {
				if (appendOr) {
					sb.append('|');
				}
				if (!incompleteAndTokens.contains(i)) {
					appendOr = true;
					sb.append(tokens.get(i));
				} else {
					appendOr = false;
				}
			}
			sb.append(')');
			sPat = sb.toString();
			pat = Pattern.compile(sPat);
		}
		return c;
	}
	
	private List<Integer>getTokes(final Pattern pattern, final String data, final Collection<Integer> incompleteAndTokens){
		final ArrayList<Integer> c = new ArrayList<Integer>();
		final Matcher m = pattern.matcher(data);
		while (m.find()) {
			final String s = escapeMetaCharacters(m.group());
			int i = 0;
			for (; i < tokens.size(); i++) {
				final String s2 = tokens.get(i);
				if (s.equalsIgnoreCase(s2)) {
					break;
				}
			}
			if (i < tokens.size()) {
				c.add(i);
			}
		}		
		for (final int i : c) {
			if (!wouldNeedReplacing(c, i)) {
				incompleteAndTokens.add(i);
			}
		}
		return c;
	}

	public boolean matches(final String data) {
		return matchPattern.matcher(data).matches();
	}
	
	public String highlightFg(boolean isEnabled, final String fgColor, final String data) {
		return highlightFgBg(isEnabled ? "black" : "gray", fgColor, data);
	}

	private boolean wouldNeedReplacing(final Collection<Integer> l, final Integer idx) {
		if (plusGroups != null && plusGroups.containsKey(idx)) {
			for (final Integer idx2:this.plusGroups.getCollection(idx)) {
				if (!l.contains(idx2)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Pattern getReplaceAllPattern(final String data) {
		return getReplaceAllPattern(data, null);
	}
	
	private WeakHashMap<Object, Pattern> whm = new WeakHashMap<Object, Pattern>();
	
	protected Pattern getReplaceAllPattern(final Object object, final StringConverter sc ) {
			if (!this.hasPlusAsAnd) {
				return replacePattern;				
			} else {
				if (whm.containsKey(object)) {
					return  whm.get(object);
				}
				final String data;
			if (sc == null) {
					data=object instanceof String ? (String)object:object.toString();
				} else {
				data = sc.toString(object);
				}
			final List<Integer> l = getTokenIndexes(data);
			if (l.size() == 0) {
					return null;
				}
			final StringBuilder sb = new StringBuilder("(");
			for (int i = 0; i < l.size(); i++) {
				final Integer idx = l.get(i);
				if (i > 0) {
							sb.append('|');
						}
						final String s = tokens.get(idx);
						sb.append(s);
				}
				sb.append(')');
			final String regEx = sb.toString();
				final Pattern pat=Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
				whm.put(object, pat);
				return pat;
			}
	}
	
	protected static String replaceAll(final Pattern replacePattern, final String data, final String replacementSpecification) {
		if (replacePattern == null) {
			return data;
		}
		final Matcher matcher = replacePattern.matcher(data);
		return matcher.replaceAll(replacementSpecification);
	}
	
	private String replaceAll(final String data, final String replacementSpecification) {
		return replaceAll(getReplaceAllPattern(data), data, replacementSpecification);
	}
	protected static String getFgColorReplacementSpec(final String bgColor, final String color) {
		return "<font color='" + color + "'>$1</font>";
	}
	
	protected static String getFgBgColorReplacementSpec(final String bgColor, final String color) {
		return "<font color='"+color+"' bgcolor='"+bgColor+"'>$1</font>";
	}
	
	public String highlightFgBg(final String bgColor, final String color, final String data) {
		return replaceAll(data, getFgBgColorReplacementSpec(bgColor, color));
	}

	protected static String getBgColorReplacementSpec(final String bgColor) {
		return "<font bgcolor='" + bgColor + "'>$1</font>";
	}
	
	public String highlightBg(final String bgColor, final String data) {
		return replaceAll(data, getBgColorReplacementSpec(bgColor));
	}

	protected static String getFgColorReplacementSpec(final String color) {
		return "<font color='" + color + "'>$1</font>";
	}
	
	public String highlightFg(final String color, final String data) {
		return replaceAll(data, getFgColorReplacementSpec(color));
	}
	
	private static final String[] finds=new String[] {
		"lov+uc, quite, are",		
		"CD5 , FITC, texas-red	, CD, texas	, Fluor",
		"as, is, ex"};

	private static final String[] data = new String[] {
		"I love everything good",
		"I love everything quite good",
		"I LOVE lucy",
		"I love her .. sort of ",
		"I love lucy quite a bit ... are you aware?",
		"Sexy Ricky was quite cd in his texas",
		"CD5 and CD3 are my favorite"
	};
	
	static ArrayList<String> splitPlus(final String input) {
		final ArrayList<String> c = new ArrayList<String>();
		String[] as = PLUS_PATTERN.split(input);
		String prior = "";
		for (final String s : as) {
			if (s.length() == 0) {
			} else if (s.endsWith("\\")) {
				prior = s.substring(0, s.length() - 1) + "+";
			} else {
				c.add(prior + s);
				prior = "";
			}
		}
		if (!Basics.isEmpty(prior)) {
			c.add(prior);
		}
		return c;
	}
	
	private static String removeGlobs(String s) {
		if (!s.equals("*")) {
			if (s.endsWith("*")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.startsWith("*")) {				
				s = s.substring(1);
			}
		} else {
			s="";
		}
		return s;
	}

	private static String handleGlobs(String s) {
		if (!s.equals("*")) {
			if (!s.endsWith("*")) {
				s += "\\b";
			} else {
				s = s.substring(0, s.length() - 1);
			}
			if (!s.startsWith("*")) {
				s = "\\b" + s;
			} else {
				s = s.substring(1);
			}
		}
		return s;
	}

	private static Pattern 
	PLUS_PATTERN=Pattern.compile("\\+"), 
			COMMA_PATTERN = Pattern.compile("[,\\n] ?");

	private static void test(String patternStr, String inputStr) {
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inputStr);
		boolean b = matcher.matches();
		if (b) {
			System.out.println("MATCHES !");
		} else {
			System.out.println("--->> no MATCHES !");
		}
		matcher = pattern.matcher(inputStr);
		boolean matchFound = matcher.find();

		if (matchFound) {
			// Get all groups for this match
			for (int i = 0; i <= matcher.groupCount(); i++) {
				System.out.print(i);
				System.out.print("  ");
				System.out.println(matcher.group(i));
			}
		}
	}
		
	private static void print(final String find, final String arg) {
		print(find, arg, -1);
	}
	private static void print(final String find, final String arg, final int trial) {
		SearchAndReplace sar = new SearchAndReplace(find, false);
		System.out.println();
		System.out.print("Trial #");
		System.out.println(trial);
		System.out.print("Input:  ");
		System.out.println(arg);
		System.out.print("...matching for:  ");
		System.out.println(sar.searchEntry);
		System.out.print("\tOUTPUT:  ");
		if (sar.matches(arg)) {
			System.out.println(sar.highlightFg(false, "yellow", arg));
			final String s = Basics.toString(sar.getMatches(arg));
			System.out.println("Matched " + s);
		} else {
			System.out.println(" ... NO  MATCH  DUDE!!!");
		}

	}

	public static void main(final String[] _args) {
		test("(a(?:b*))+(c*)", "abbabcd");
	    test("(IgM)", "You know ? ... IgM is pretty cool");
	    test("IgM", "You know ? ... IgM is pretty cool");
		String patternStr = ".*(((?=.*Ig)(?=.*26)(?=.*Her))|IgM).*";
		test(patternStr, "IgD (Herz: 1126)");
		test(patternStr, "IgD (Herz: 112)");
		test(patternStr, "1126 *IgD (Herz: )");
		test(patternStr, "112 *IgD (Herz: )");
		test(patternStr, "You know ? IgM is pretty cool");
		patternStr = "(((?:.*Ig)(?:.*26))|(IgM))";
		test(patternStr, "IgD (Herz: 1126)");
		test(patternStr, "IgM is pretty cool");
		Collection<String> as = splitPlus("Hello+you+ me");
		as = splitPlus("+Hello+you+me");
		as = splitPlus("+Hello++you+me");
		as = splitPlus("Hello+you\\+me");
		System.out.println(as);
		patternStr = ",,Ig+6\nIgM";
		print(patternStr, "IgG2 (Herz: 1216)");
		print(patternStr, "IgM");
		print(patternStr, "IgD (Herz: 1126)");
		print(patternStr, "IgD (Herz: 112)");
		print(patternStr, "1126 *IgD (Herz: )");
		print(patternStr, "112 *IgD (Herz: )");
		for (final String find : finds) {
			int trial = 0;
			for (final String arg : data) {
				print(find, arg, trial++);
			}
		}
	}
	
	public static String YELLOW="rgb(255,255,100)", YELLOW_DISABLED="rgb(255,255,120)";
}
