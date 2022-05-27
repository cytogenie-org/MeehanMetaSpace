
package com.MeehanMetaSpace;

/**
 * Title:\
 * Description:  Basic non visual JAVA utilities
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
import java.awt.Component;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;



public class Basics {

	public static boolean IsInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}
    public static TreeSet<String> getCaseInsensitiveSet(){
        return new TreeSet<String>(new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                if (o1 == null) {
                    return o2 == null ? 0 : -1;
                }
                if (o2 == null) {
                    return o1 == null ? 0 : 1;
                }
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }

        });
    }
    public static String duplicate(final char c, final int n) {
        char[] charArr = new char[n];
        Arrays.fill(charArr, c);
        return new String(charArr);
    }

    public static String duplicate(final String str, final int n) {
    	final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
  		final StringBuilder sb=tssb.lock();
  		for (int i=0;i<n;i++){
    		sb.append(str);
    	}
  		return tssb.unlockString();
    }

    public static Properties props =null;
       static String[] validDomainExtensions = {
                                            ".com", ".org", ".net", ".ca",
                                            ".jp", ".edu", ".aero", ".biz",
                                            ".coop", ".info", ".museum",
                                            ".name", ".pro", ".travel", ".gov",
                                            ".mil",
                                            ".int", ".ac", ".ad",
                                            ".ae", ".af", ".ag", ".ai", ".al",
                                            ".am", ".an", ".ao", ".aq", ".ar",
                                            ".as", ".at", ".au", ".aw", ".ax",
                                            ".az",
                                            ".ba", ".bb", ".bd", ".be", ".bf",
                                            ".bg", ".bh", ".bi", ".bj", ".bm",
                                            ".bn", ".bo", ".br", ".bs", ".bt",
                                            ".bv",
                                            ".bw", ".by", ".bz", ".ca", ".cc",
                                            ".cd", ".cf", ".cg", ".ch", ".ci",
                                            ".ck", ".cl", ".cm", ".cn", ".co",
                                            ".cr",
                                            ".cs", ".cu", ".cv", ".cx", ".cy",
                                            ".cz", ".de", ".dj", ".dk", ".dm",
                                            ".do", ".dz", ".ec", ".ee", ".eg",
                                            ".eh",
                                            ".er", ".es", ".et", ".eu", ".fi",
                                            ".fj", ".fk", ".fm", ".fo", ".fr",
                                            ".ga", ".gb", ".gd", ".ge", ".gf",
                                            ".gg",
                                            ".gh", ".gi", ".gl", ".gm", ".gn",
                                            ".gp", ".gq", ".gr", ".gs", ".gt",
                                            ".gu", ".gw", ".gy", ".hk", ".hm",
                                            ".hn",
                                            ".hr", ".ht", ".hu", ".id", ".ie",
                                            ".il", ".im", ".in", ".io", ".iq",
                                            ".ir", ".is", ".it", ".je", ".jm",
                                            ".jo",
                                            ".jp", ".ke", ".kg", ".kh", ".ki",
                                            ".km", ".kn", ".kp", ".kr", ".kw",
                                            ".ky", ".kz", ".la", ".lb", ".lc",
                                            ".li",
                                            ".lk", ".lr", ".ls", ".lt", ".lu",
                                            ".lv", ".ly", ".ma", ".mc", ".md",
                                            ".mg", ".mh", ".mk", ".ml", ".mm",
                                            ".mn",
                                            ".mo", ".mp", ".mq", ".mr", ".ms",
                                            ".mt", ".mu", ".mv", ".mw", ".mx",
                                            ".my", ".mz", ".na", ".nc", ".ne",
                                            ".nf",
                                            ".ng", ".ni", ".nl", ".no", ".np",
                                            ".nr", ".nu", ".nz", ".om", ".pa",
                                            ".pe", ".pf", ".pg", ".ph", ".pk",
                                            ".pl",
                                            ".pm", ".pn", ".pr", ".ps", ".pt",
                                            ".pw", ".py", ".qa", ".re", ".ro",
                                            ".ru", ".rw", ".sa", ".sb", ".sc",
                                            ".sd",
                                            ".se", ".sg", ".sh", ".si", ".sj",
                                            ".sk", ".sl", ".sm", ".sn", ".so",
                                            ".sr", ".st", ".sv", ".sy", ".sz",
                                            ".tc",
                                            ".td", ".tf", ".tg", ".th", ".tj",
                                            ".tk", ".tl", ".tm", ".tn", ".to",
                                            ".tp", ".tr", ".tt", ".tv", ".tw",
                                            ".tz",
                                            ".ua", ".ug", ".uk", ".um", ".us",
                                            ".uy", ".uz", ".va", ".vc", ".ve",
                                            ".vg", ".vi", ".vn", ".vu", ".wf",
                                            ".ws",
                                            ".ye", ".yt", ".yu", ".za", ".zm",
                                            ".zw"};

    public interface Gui {
    	public String showHtml(
    			final String temporaryFilePrefix,
    		      final String htmlEncodedContent,
    		      final boolean useInternalBrowser);
        public boolean download(
          String localFileName,
          String localFolder,
          String sourceUrlName,
          boolean unzipping,
          final boolean junkZipFolder,
          final boolean overWrite);

        boolean ask(String title, String question, boolean error);

        String getStringFromUser(
          Component component,
          String message,
          String title,
          String suggestedValue);

        String getFileName(
          String regexMatch,
          String regexAvoid,
          String extension,
          String dsc,
          String approveButtonText,
          String approveToolTip,
          boolean readOnly,
          String currentDirectory,
          String selectedFile,
          Window containingWnd);

        String getDirName(
          String dialogTitle,
          String dir,
          String p_approveButtonText,
          String approveToolTip,
          Window containingWnd);

        void alert(String msg, boolean beep);

        void alert(String msg);

        void alertAsync(String msg, boolean beep);

        int choose(Component component, String msg, String title,
                   Object[] choices,
                   int defaultChoice,
                   boolean allowCancel,
                   boolean vertical);
    }


    public static Gui gui = new Basics.Gui() {
        public boolean download(
          final String localFileName,
          final String localFolder,
          final String sourceUrlName,
          final boolean unzipping,
          final boolean junkZipFolder,
          final boolean overWrite) {
            boolean retVal = false;
            final String localPath = IoBasics.concat(localFolder, localFileName);
            try {
                if (unzipping) {
                    IoBasics.copy(localPath, sourceUrlName, null);
                    IoBasics.unzipRecursively(localPath, localFolder,
                                              junkZipFolder,
                                              overWrite, null);
                } else {
                    IoBasics.copy(
                      localPath,
                      sourceUrlName,
                      null);

                }
                retVal = true;
            } catch (final IOException e) {
                Pel.log.warn(e);
            }
            return retVal;

        }

        public boolean ask(final String title, final String question,
                           final boolean error) {
            Pel.log.println("Asking title=" + title + " question=" + question);
            return!error; // risky will return yes if not an error, otherwise returns no
        }

        public String getStringFromUser(
          Component component,
          String message,
          String title,
          String suggestedValue) {
            return suggestedValue;
        }

        public int choose(
          final Component component,
          final String msg,
          final String title,
          final Object[] choices,
          final int defaultChoice,
          final boolean allowCancel,
          final boolean vertical) {
            return defaultChoice;
        }

        public String getFileName(
          String regexMatch,
          String regexAvoid,
          String extension,
          String dsc,
          String approveButtonText,
          String approveToolTip,
          boolean readOnly,
          String dir,
          String file,
          Window containingWnd) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void alert(final String msg) {
            alert(msg, false);
        }

        public void alert(final String msg, final boolean beep) {
            if (beep) {
                if (Pel.log == null) {
                    System.out.println(msg);
                } else {
                    Pel.log.println(msg);
                }
            } else {
                if (Pel.log == null) {
                    System.err.println(msg);
                } else {
                    Pel.log.printlnErr(msg);
                }
            }
        }

        public void alertAsync(final String msg, final boolean beep) {
            alert(msg, beep);
        }

        public String getDirName(
          final String dialogTitle,
          final String dir,
          final String p_approveButtonText,
          final String approveToolTip,
          final Window containingWnd) {
            throw new UnsupportedOperationException("Not implemented");
        }

		public String showHtml(String temporaryFilePrefix,
				String htmlEncodedContent, boolean useInternalBrowser) {
			return null;
		}
    };

    public static String doubleSlashes(final String input) {
        final char[] c = input.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\\') {
                sb.append("\\\\");
            } else {
                sb.append(c[i]);
            }
        }
        return sb.toString();
    }

    public static int count(final String input, final String search) {
        int count = 0;
        int len = search.length();
        String source = input;
        int idx = source.indexOf(search);
        while (idx >= 0) {
            source = source.substring(idx + len);
            idx = source.indexOf(search);
            count++;
        }
        return count;
    }

    public static String strip(final String input, final String toStrip) {
        String s = input;
        final StringBuilder sb = new StringBuilder();
        for (int idx = s.indexOf(toStrip); idx >= 0; idx = s.indexOf(toStrip)) {
            sb.append(s.substring(0, idx));
            s = s.substring(idx + toStrip.length());
        }
        sb.append(s);
        return sb.toString();
    }

    public static String stripBodyHtml(final String s) {
    	if (s == null ) return null;
        final char[] c = s.toCharArray();
        final StringBuilder sb = new StringBuilder();

        //int j = 0;
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '&') {
                //final int start = i;
                for (; i < c.length; i++) {
                    if (c[i] == ';') {
                        sb.append(' ');
                        break;
                    }
                }
            } else if (c[i] == '<') {
                final int start = i;
                for (; i < c.length; i++) {
                    if (c[i] == '>') {
                        final String snip = s.substring(start, i + 1);
                        if (snip.equalsIgnoreCase("<br>")) {
                            sb.append(' ');
                        }
                        break;
                    }
                }
            } else {
                sb.append(c[i]);
            }
        }
        final String retVal = sb.toString();
        return retVal;

    }
    public static String stripHtmlHeaderTagsOnly(final String input){
    	return snip(input, "<html>", "</html>");
    }
    
    public static String stripHtmlTagsOnly(final String input){
    	final String question;
    	if (input.startsWith("<html>")){ // forgive usage of html by programmer
        	if (input.endsWith("</html>")){ // EVEN forgive BAD usage of html by programmer 
        		question=input.substring("<html>".length(), input.length()-"</html>".length());
        	} else {
        		question=input.substring("<html>".length());
        	}
        } else {
        	question=input;
        }
    	return input;
    }
    public static String stripSimpleHtml(final String input) {
        final String value;
        if (input != null) {
            String v = snip(input, "<body>", "</body>");
            if (v == null) {
                v = snip(input, "<html>", "</html>");
                if (v != null) {
                    value=stripBodyHtml(v);
                } else {
                    value=input;
                }
            } else {
                value=stripBodyHtml(v);
            }
        } else {
            value = input;
        }
        return value;
    }

    public static String snip(final String input, final String start, final String end){
        if (input != null) {
            final int idx = input.indexOf(start);
            if (idx >= 0) {
                final String s;
                final int idx2 = input.lastIndexOf(end);
                if (idx2 >= 0) {
                    s = input.substring(idx + start.length(), idx2);
                } else {
                    s = input.substring(idx + start.length());
                }
                return s;
            }
        }
        return null;
    }

    public static String stripHeaderHtml(final String input) {
        String value = snip(input, "<body>", "</body>");
        if (value == null) {
            value = snip(input, "<html>", "</html>");
            if (value != null) {
                return value;
            }
        } else {
            return value;
        }
        return input;
    }

    public static void main(String[] args) {
    	String []test=new String[]{"me","we","us","them"};
    	String []order=null;
    	boolean ok=orderIsConsistent(test, order);
    	order=new String[]{"we"};
    	ok=orderIsConsistent(test, order);
    	order=new String[]{"we", "us"};
    	ok=orderIsConsistent(test, order);
    	order=new String[]{"us", "me"};
    	ok=orderIsConsistent(test, order);
    	order=new String[]{"we", "them"};
    	ok=orderIsConsistent(test, order);
    	order=new String[]{"us"};
    	ok=orderIsConsistent(test, order);
    	order=new String[]{"we", "me"};
    	ok=orderIsConsistent(test, order);
		if (args.length == 0) {
			Basics.encode(" 125.33");
			Basics.encode(" poo");
			Basics.encode("1");
			Basics.encode("1, 2");
			Basics.encode(".1,2");
			Basics.encode(",1,2");
			args = new String[] { "this", "that" };
			System.out.println();
			Object o1 = null, o2 = null;
			Basics.equals(o1, o2);
			Basics.equals(args, null);
			Basics.equals(null, args);

			Basics.equals(new Integer[] { Integer.MAX_VALUE }, new Integer[] { Integer.MAX_VALUE });
			Basics.equals(args, new Integer[] { Integer.MAX_VALUE });
			Object[] a1 = new Integer[] { Integer.MIN_VALUE }, a2 = new Integer[] { Integer.MAX_VALUE };
			Basics.equals(a1, a2);
			Basics.equals(args, args);
			a1 = new Object[] { args };
			a2 = new Object[] { args };
			Basics.equals(a1, a2);
			final Collection c1 = Basics.toList(args), c2 = Basics.toList(args);
			Basics.equals(c1, c2);
			c1.add(a1);
			c2.add(a2);
			Basics.equals(c1, c2);
		} else {

			// System.out.println("Last build time was " + BuildTime.text );
			java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
			Date now = new Date();
			now.setHours(now.getHours() + 1);
			String rightNow = df.format(now);
			String javaCode = ("package " + args[0] + ";");
			javaCode += lineFeed;
			javaCode += "public class BuildTime {" + lineFeed;
			javaCode += "\tpublic static String text=\"" + rightNow + "\";" + lineFeed;
			javaCode += "\tpublic static long value=" + now.getTime() + "l;" + lineFeed;
			javaCode += "}" + lineFeed;
				IoBasics.saveTextFile("BuildTime.java", javaCode);
			System.out.println("New build time is " + rightNow);
			System.out.println();
			System.out.println();
		}
	}

    public static boolean endsWith(final Collection strings,
                                   final String string) {
        for (final Iterator it = strings.iterator(); it.hasNext(); ) {
            final Object o = it.next();
            if (string.endsWith(o.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean endsWithIgnoreCase(final Collection strings,
                                             final String string) {
        final String s = string.toLowerCase();
        for (final Iterator it = strings.iterator(); it.hasNext(); ) {
            final Object o = it.next();
            if (s.endsWith(o.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(final String[] strings,
                                     final String string) {
        for (int i = 0; i < strings.length; i++) {

            if (string.startsWith(strings[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(final Collection strings,
                                     final String string) {
        if (string != null){
            for (final Iterator it = strings.iterator(); it.hasNext(); ) {
                final Object next = it.next();
                final String nextString = next == null ? "" : next.toString();
                if (string.startsWith(nextString)) {
                    if (Basics.isEmpty(next)) {
                        if (Basics.equals(string, next)) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static boolean startsWithIgnoreCase(final Collection strings,
                                               final String string) {
        final String s = string.toLowerCase();
        for (final Iterator it = strings.iterator(); it.hasNext(); ) {
            final Object o = it.next();
            if (s.startsWith(o.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    public final static java.util.Map
    	UNMODIFABLE_EMPTY_MAP=Collections.unmodifiableMap(Collections.EMPTY_MAP);
    public static final MapOfMany EMPTY_MAP_OF_MANY=new MapOfMany(UNMODIFABLE_EMPTY_MAP, false);
    public final static java.util.List
      UNMODIFIABLE_EMPTY_LIST = Collections.unmodifiableList(Collections.EMPTY_LIST);
    
    public final static java.util.Set
    	UNMODIFIABLE_EMPTY_SET = Collections.unmodifiableSet(Collections.EMPTY_SET);

    public static StringEncoder DEFAULT_SE=new StringEncoder(){
        public String toString(final Object input){
            return input==null?"":input.toString();
        }
    };

    public static String toOlHtml(final Collection c) {
        return toOlHtml(c,DEFAULT_SE);
    }

    public static String toOlHtml(final Collection c, final StringEncoder se) {
        return toHtmlList("ol",c,se,false,false);
    }

    public static String toUlHtml(final Collection c) {
        return toUlHtml(c,DEFAULT_SE, false, false);
    }

    public static String toUlHtml(
      final Collection c,
      final StringEncoder se,
      final boolean noListIfOne,
      final boolean eliminateDuplicates) {
        return toHtmlList("ul",c,se, noListIfOne,eliminateDuplicates);
    }

    public static String toHtmlList(
      final String htmlListType,
      final Collection _c,
      final StringEncoder _se,
      final boolean noListIfOne,
      final boolean eliminateDuplicates) {
        final StringEncoder se=_se==null?DEFAULT_SE:_se;
        final Collection c;
        if (eliminateDuplicates){
            c=new HashSet(_c);
        } else {
            c=_c;
        }
        if (c.size()==1 && noListIfOne){
            return se.toString(c.iterator().next());
        }
        final StringBuilder sb = new StringBuilder("<");
        sb.append(htmlListType);
        sb.append(">");
        for (final Iterator it = c.iterator(); it.hasNext(); ) {
            sb.append("<li>");
            sb.append(se.toString(it.next()));
        }
        sb.append("</");
        sb.append(htmlListType);
        sb.append(">");
        return sb.toString();
    }

    public static String toUlHtml(final Object[] items) {
        final StringBuilder sb = new StringBuilder("<ul>");
        for (int i = 0; i < items.length; i++) {
            sb.append("<li>");
            sb.append(items[i]);
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public static Collection add(final Collection to, final Object[] from) {
        for (int i = 0; i < from.length; i++) {
            to.add(from[i]);
        }
        return to;
    }

    public static boolean hasNulls(final Object[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNulls(final Collection c) {
        for (final Iterator it = c.iterator(); it.hasNext(); ) {
            if (it.next() == null) {
                return true;
            }
        }
        return false;
    }

    public final static String[] EMPTY_STRINGS = new String[0];
    public final static Integer[] EMPTY_INTEGERS = new Integer[0];
    public static String[] toStrings(final Collection c) {
        return (String[]) c.toArray(new String[c.size()]);
    }

    public static String[] toStringsCoerced(final Collection c) {
        final String[] s = new String[c.size()];
        final Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            s[i] = it.next().toString();
        }
        return s;
    }

    public static String[][] toStrings2d(final Collection c) {
        final String[][] s = new String[c.size()][];
        final Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            s[i] = (String[]) it.next();
        }
        return s;
    }

    public static String[] sortByPreferenceOrder(String[] items,
                                                 Collection preferenceOrder) {
        return sortByPreferenceOrder(items,
                                     (String[]) preferenceOrder.toArray(new
          String[
          preferenceOrder.size()]));
    }

    public static class ObjectCounter {

        protected Object[] getEmptyArray(final int n) {
            return new Object[n];
        }

        private final HashMap hm = new HashMap();

        public Object[] getThoseHavingCountOf(final int exactCount) {
            final Integer criteria = new Integer(exactCount);
            final ArrayList al = new ArrayList();
            for (final Iterator it = hm.keySet().iterator(); it.hasNext(); ) {
                final Object key = it.next();
                final Integer integer = (Integer) hm.get(key);
                if (integer.equals(criteria)) {
                    al.add(key);
                }
            }
            return al.toArray(getEmptyArray(al.size()));
        }

        public int count(final Object obj) {
            final Integer count;
            if (!hm.containsKey(obj)) {
                count = new Integer(1);
                hm.put(obj, count);
            } else {
                count = new Integer(((Integer) hm.get(obj)).intValue() + 1);
                hm.put(obj, count);
            }
            return count.intValue();
        }
    }


    public static int getMinimum(final int[] a) {
        int minimum = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < minimum) {
                minimum = a[i];
            }
        }
        return minimum;
    }

    public static int getMaximum(final int[] a) {
        int maximum = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > maximum) {
                maximum = a[i];
            }
        }
        return maximum;
    }
    
    public static Collection sort(final Collection data, final Collection preferredOrder){
    	final Object[]a=data.toArray();
    	sort(a, preferredOrder.toArray());
    	return toList(a);
    }

    public static boolean sort(
      final Object[] items,
      final Object[] preferenceOrder) {
    	if (items.length <2){
    		return false;
    	}
    	boolean changedOrder=false;
        if (preferenceOrder != null && preferenceOrder.length>0) {
        	final HashSet<Object> done=new HashSet<Object>();
            final Object[] retVal = new Object[items.length];
            int j = 0;
            for (int i = 0; i < preferenceOrder.length; i++) {
                final Object lookFor = preferenceOrder[i];
				if (!done.contains(lookFor)) {
					for (int k = 0; k < items.length; k++) {
						if (Basics.equals(items[k], lookFor)) {
							if (k!=j){
								changedOrder=true;
							}
							retVal[j++] = items[k];
						}
					}
					done.add(lookFor);
				}
            }
            for (int i = 0; i < items.length; i++) {
                if (!equalsAny(preferenceOrder, items[i])) {
                    retVal[j++] = items[i];
                }
            }
            //assert j == items.length:j + " does not equal " + items.length;
            for (int i = 0; i < items.length; i++) {
                items[i] = retVal[i];
            }
        }
        return changedOrder;
    }

	public static boolean orderIsConsistent(final Object[] test, Object[] order) {
		if (test.length > 1 && order != null && order.length > 0) {
			int last = -1;
			for (int i=0;i<test.length;i++) {
				final Object o=test[i];
				final int idx = indexOf(order, o);
				if (idx >= 0) {
					if (idx < last) {
						return false;
					}
					last = idx;
				} 
			}
		}
		return true;
	}
    public static String[] sortByPreferenceOrder(
      final String[] items,
      final String[] preferenceOrder) {
    	Basics.sort(items, preferenceOrder);
    	return items;
    }

    public static boolean equalsAny(int[] list, int searchArg) {
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                if (list[i] == searchArg) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean equalsAny(
      final Object[] list,
      final Object searchArg) {
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                if (equals(searchArg, list[i])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean contains(final int[]list, final int searchArg){
    	return indexOf(list, searchArg)>=0;
    }
    public static boolean contains(final Object[] list, final Object searchArg) {
        if (list != null) {
            for (Object item : list) {
                if (equals(searchArg, item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean hasNew(final Collection<T>oldStuff, final Collection<T>possiblyNewStuff){
    	for (final T t:possiblyNewStuff){
    		if (!oldStuff.contains(t)){
    			return true;
    		}
    	}
    	return false; 
    }
    public static <T> boolean containsAny(final Collection<T>_this, final Collection<T>_that){
    	Collection<T>c=_this.size()>_that.size()?_that:_this;
    	Collection<T>o=_this.size()>_that.size()?_this:_that;
    	for (final T t:c){
    		if (o.contains(t)){
    			return true;
    		}
    	}
    	return false; 
    }
    
    public static boolean containsAny(final String[] list,
                                      final String searchArg) {
        if (list != null && searchArg != null) {
            for (int i = 0; i < list.length; i++) {
                if (searchArg.indexOf(list[i]) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int[] toIntArray(final Collection c) {
        final int[] ia = new int[c.size()];
        final Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            ia[i] = ((Integer) it.next()).intValue();
        }
        return ia;
    }

    public static String[] toStringArray(final List l) {
        final String[] sa = new String[l.size()];
        for (int i = 0; i < sa.length; i++) {
            sa[i] = l.get(i).toString();
        }
        return sa;
    }

    public static String[] toStringArray(final Object []l) {
        final String[] sa = new String[l.length];
        for (int i = 0; i < sa.length; i++) {
            sa[i] = l[i].toString();
        }
        return sa;
    }

    public static String toString(final Map map) {
        final StringBuilder sb = new StringBuilder();
        for (final Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            final Object key = it.next();
            sb.append(key);
            sb.append("=");
            Object value = map.get(key);
            sb.append(value);
            eol(sb);

        }
        return sb.toString();
    }

    public static String toString(final Iterator it) {
        return toString(it, false);
    }

    public static String toString(final Collection c, final StringEncoder e) {
        if (c == null) {
            return "null";
        }
        return toString(c.iterator(), e, false);
    }

    public static String toString(final Collection c) {
        if (c == null) {
            return "null";
        }
        return toString(c.iterator(), false);
    }

    public static String encodeHtml(
      final String title,
      final String itemDescription,
      final String htmlListNode,
      final Collection msgs,
      final boolean encodeHtmlForEachListItem) {
        final int n = msgs == null ? 0 : msgs.size();
        final StringBuilder sb = new StringBuilder(startHtmlUncentered());
        if (title != null) {
            sb.append(HTML_START_HEAD4);
            sb.append(title);
            sb.append(HTML_END_HEAD4);
        }
        if (n == 1) {
            sb.append(msgs.iterator().next());
        } else if (n > 1) {
            sb.append("<i>There are ");
            sb.append(msgs.size());
            sb.append(' ');
            sb.append(itemDescription);
            sb.append("</i>");
            sb.append(toUlHtml(htmlListNode, msgs, encodeHtmlForEachListItem));
        }
        sb.append(endHtml());
        return sb.toString();
    }


    public static String toUlHtml(
      final String listType,
      final Collection items,
      final boolean encodeHtmlForEachListItem) {
        final StringBuilder sb = new StringBuilder("<");
        sb.append(listType);
        sb.append(">");
        for (final Iterator it = items.iterator(); it.hasNext(); ) {
            final Object o = it.next();
            sb.append("<li>");
            sb.append(encodeHtmlForEachListItem ? encodeHtml(o.toString()) :
                      o.toString());
            //eol(sb);
        }
        sb.append("</");
        sb.append(listType);
        sb.append(">");
        return sb.toString();
    }

    public static String toString(final Iterator it, final boolean quoted) {
        final StringBuilder sb = new StringBuilder();
        if (it == null) {
            sb.append("null");
        } else {
            int i;
            for (i = 0; it.hasNext(); i++) {
				if (!sb.toString().endsWith(lineFeed)) {
					if (i > 0) {
						sb.append(quoted ? "\", \"" : ", ");
					} else if (quoted) {
						sb.append('\"');
					}
				} else {
					eol(sb);
				}
                sb.append(it.next());
            }
            if (i > 0 && quoted) {
                sb.append('\"');
            }
        }
        return sb.toString();
    }

    public static String toString(
      final Iterator it,
      final StringEncoder encoder,
      final boolean quoted) {
            final StringBuilder sb = new StringBuilder();
            if (it == null) {
                sb.append("null");
            } else {
                int i;
                for (i = 0; it.hasNext(); i++) {
                    if (i > 0) {
                        sb.append(quoted ? "\", \"" : ", ");
                    } else if (quoted) {
                        sb.append('\"');
                    }
                    sb.append(encoder.toString(it.next()));
                }
                if (i > 0 && quoted) {
                    sb.append('\"');
                }
            }
            return sb.toString();
        }

    public static String toString(
      final Object[] a,
      final StringEncoder sc,
      final boolean quoted) {
        final StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(quoted ? "\", \"" : ", ");
            } else if (quoted) {
                sb.append('\"');
            }
            sb.append(sc.toString(a[i]));
        }
        if (i > 0 && quoted) {
            sb.append('\"');
        }
        return sb.toString();
    }

    public static String toString(
      final Collection c,
      final StringEncoder sc,
      final boolean quoted) {
        final StringBuilder sb = new StringBuilder();
        final Iterator it = c.iterator();
        int i;
        for (i = 0; it.hasNext(); i++) {
            if (i > 0) {
                sb.append(quoted ? "\", \"" : ", ");
            } else if (quoted) {
                sb.append('\"');
            }
            sb.append(sc.toString(it.next()));
        }
        if (i > 0 && quoted) {
            sb.append('\"');
        }

        return sb.toString();
    }

    public static <T> java.util.List<T> toList(final Collection<T> c) {
        return c == null ? new ArrayList<T>() : new ArrayList<T>(c);
    }

    
    
    public static MapOfMany<String, Integer> toMap(final String []o) {
    	final MapOfMany<String, Integer> map=new MapOfMany<String, Integer>(true, false);
    	for(int i=0;i<o.length;i++) {
    		if (o[i]==null) {
    			map.put("NULL "+i, i);
    		} else {
    			map.put(o[i], i);
    		}
    	}
    	return map;
    }

    public static ComparableBoolean isYesOrNo(final Object o){
    	if (o instanceof String){
    		if ("yes".equalsIgnoreCase((String)o)){
    			return ComparableBoolean.YES;
    		} else if ("no".equalsIgnoreCase((String)o)){
    			return ComparableBoolean.NO;
    		}
    	}
    	return null;
    }
    public static<T> ArrayList<T> toList(final T o) {
        final ArrayList<T> al = new ArrayList<T>();
        al.add(o);
        return al;
    }

    public static String printToString(
      final String title,
      final Collection c) {
        final StringWriter sw = new StringWriter();
        print(title, new PrintWriter(sw), c);
        return sw.toString();
    }

    public static void print(
      final String title,
      final PrintStream ps,
      final Collection c) {
        print(title, new PrintWriter(ps), c);
    }

    public static void print(
      final String title,
      final PrintWriter ps,
      final Collection c) {
        ps.println();
        ps.print(c.size());
        ps.print(' ');
        ps.println(title);
        ps.println();
        ps.println();
        final ArrayList al = new ArrayList();
        final Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            ps.println(i + ". " + it.next());
        }
    }

    public static <T> T getFirst(final Collection<T> c) {
        T retVal = null;
        if (c != null && c.size() > 0) {
            retVal = c.iterator().next();
        }
        return retVal;
    }

    public static void count(final Object[] toCount, final Object[] match,
                             final int[] counts) {
        for (int i = 0; i < toCount.length; i++) {
            for (int j = 0; j < match.length; j++) {
                if (toCount[i].equals(match[j])) {
                    counts[j]++;
                }
            }
        }
    }

    public static <T> Collection<T> getLowestCountItems(final T[] items,
                                                 final int[] counts) {
        final int minimum = Basics.getMinimum(counts);
        final Collection<T> c = new ArrayList<T>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == minimum) {
                c.add(items[i]);
            }
        }
        return c;
    }

    public static <T>Collection<T> getHighestCountItems(final T[] items,
                                                  int[] counts) {
        final int maximum = Basics.getMaximum(counts);
        final Collection<T> c = new ArrayList<T>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == maximum) {
                c.add(items[i]);
            }
        }
        return c;
    }

    public static <T> Collection<T> addAll(final Collection<T> to, final T[] from) {
        if (!Basics.isEmpty(from)){
            for (int i = 0; i < from.length; i++) {
                to.add(from[i]);
            }
        }
        return to;
    }

    public static <T> Collection<T> addAllNonNull(final Collection<T> to, final T[] from) {
        if (from!=null){
            for (int i = 0; i < from.length; i++) {
            	if (from[i]!=null){
            		to.add(from[i]);
            	}
            }
        }
        return to;
    }

    public static void encodeAnchorHtml(
      final boolean href,
      final StringBuilder sb,
      final String internalAnchorName,
      final String externalAnchorName,
      final String tocHeader,
      final String bodyHeader,
      final String backToTopHref
      ) {
        if (!href && backToTopHref != null) {
            sb.append("<a href=\"#");
            sb.append(backToTopHref);
            sb.append("\">(back to top)</a><br>");
        }
        sb.append(href ? "<li>" : "<hr>");
        sb.append("<a ");
        sb.append(href ? "href='#" : "name='");
        sb.append(internalAnchorName);
        sb.append("'><");
        sb.append(href ? tocHeader : bodyHeader);
        sb.append(">");
        sb.append(externalAnchorName);
        sb.append("</");
        sb.append(href ? tocHeader : bodyHeader);
        sb.append("></a>");
    }

    public static void Assert(final boolean fundamentallyNecessaryReality,
                              final String message) {
        if (!fundamentallyNecessaryReality) {
            Basics.gui.alert(concat("<html>Exiting because <br>", message, "</html>"), true);
            System.exit(0);
        }
    }

    public static boolean isEmpty(final int[] input) {
        return input == null || input.length == 0;
    }

    public static boolean isEmpty(final Object[] argument) {
        return argument == null || argument.length == 0;
    }

    public static Collection getSubCollection(final Collection c,
                                              final Object startAt) {
        boolean rootFound = false;
        final ArrayList al = new ArrayList();
        for (final Iterator it2 = c.iterator(); it2.hasNext(); ) {
            final Object o = it2.next();
            if (!rootFound) {
                rootFound = o.equals(startAt);
            }
            if (rootFound) {
                al.add(o);
            }
        }
        return al;

    }

    public static boolean isEmpty(final MapOfMapOfMany input) {
        return input == null ||
          input.size()==0;
    }

    public static boolean isEmpty(final MapOfMany input) {
        return input == null ||
          input.size()==0;
    }

    public static boolean isEmpty(final Object input) {
        if (input instanceof Number) {
            return ((Number) input).doubleValue() == 0.0;
        }
        return input == null ||
          input.toString().trim().length() == 0;
    }

    public static String removeLineFeed(String s){
    	if (s != null ){
    		s=s.replace('\r', ' ');
    		s=s.replace('\n', ' ');
    	}
    	return s;
    }

    public static String[] pad(final String[] labels, final int leastAmount) {
        final String[] retVal = new String[leastAmount];
        int i = leastAmount - 1;
        for (; i >= labels.length; i--) {
            retVal[i] = "";
        }
        for (; i >= 0; i--) {
            retVal[i] = labels[i];
        }
        return retVal;
    }

    public static String[] rTrim(final String[] labels, final int leastAmount) {
        int i = labels.length - 1;
        for (; i >= 0; i--) {
            if (!Basics.isEmpty(labels[i]) || (i + 1) <= leastAmount) {
                break;
            }
        }
        final String[] retVal;
        if (i < labels.length - 1) {
            retVal = new String[i + 1];
            for (int j = 0; j < retVal.length; j++) {
                retVal[j] = labels[j];
            }
        } else {
            retVal = labels;
        }
        return retVal;
    }

    public static boolean isEmpty(final String inputString) {

        return inputString == null ||
          inputString.trim().length() == 0;
    }

    public static <T> ArrayList<T> toList(final T[] ao) {
        if (ao == null) {
            return null;
        }
        final ArrayList<T> al = new ArrayList<T>();
        for (int i = 0; i < ao.length; i++) {
            al.add(ao[i]);
        }
        return al;
    }

    public static java.util.List toUniqueNonNullList(final Object[] ao) {
        return (java.util.List) addIfUniqueNonNull(new ArrayList(), ao);
    }

    public static Collection addIfUniqueNonNull(final Collection al,
                                                final Object[] ao) {
        for (int i = 0; i < ao.length; i++) {
            if (ao[i] != null && !al.contains(ao[i])) {
                al.add(ao[i]);
            }
        }
        return al;
    }

    public static HashSet toSet(final Object[] ao) {
        final HashSet hs = new HashSet();
        for (int i = 0; i < ao.length; i++) {
            hs.add(ao[i]);
        }
        return hs;
    }
    public static boolean contains(
      final Collection c,
      final Object lookFor) {
        return indexOf(c,lookFor)>=0;
    }

    public static int indexOf(
      final Collection c,
      final Object lookFor) {
        if (c != null) {
            final Iterator it = c.iterator();
            if (lookFor instanceof Object[]) {
                for (int i = 0; it.hasNext(); i++) {
                    final Object o = it.next();
                    if (o instanceof Object[]) {
                        if (equals( (Object []) o, (Object [])lookFor)){
                            return i;
                        }
                    }
                }
            } else {
                for (int i = 0; it.hasNext(); i++) {
                    final Object o = it.next();
                    if (equals(o, lookFor)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static int count(final Object[] objects, final Object lookFor) {
        int n = 0;
        for (int i = 0; i < objects.length; i++) {
            if (Basics.equals(lookFor, objects[i])) {
                n++;
            }
        }
        return n;
    }


    /**
     * @return index of string in container array that equals lookFor
     * @parameter container string array to look up
     * @parameter lookFor string to look for in container
     */
    public static int indexOf(
      final Object[] container,
      final Object lookFor) {
        if (container != null) {
            for (int i = 0; i < container.length; i++) {
                if (equals(lookFor, container[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String makeString(final char c, final int repeat){
        if (repeat>0){
            char []s=new  char[repeat];
            for (int i=0;i<s.length;i++){
                s[i]=c;
            }
            return new String(s);
        }
        return null;
    }

    /**
     * @return index of string in container array that equals lookFor
     * @parameter container string array to look up
     * @parameter lookFor string to look for in container
     */
    public static int indexOf(int[] container, int lookFor) {
        if (container != null) {
            for (int i = 0; i < container.length; i++) {
                if (lookFor == container[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String[] prepend(final String firstItem, final String[] a) {
        String[] r = new String[a.length + 1];
        r[0] = firstItem;
        for (int i = 1; i < r.length; i++) {
            r[i] = a[i - 1];
        }
        return r;
    }
    
    public static boolean isEmptyExcludingHeader(ArrayList<String> lines){
		int count=0;
		for(String line:lines){
			if(line.trim().length()>0){
				count++;
				if(count>1){
					return false;
				}
			}
		}
		return true;
	}
    

    public static Collection<String> toStrings(
      final String source,
      final String delimiters) {
        final char[] s = source.toCharArray();
        int l = 0, i = 0;
        final ArrayList<String> al = new ArrayList();

        for (; i < s.length; i++) {
            if (delimiters.indexOf(s[i]) >= 0) {
                al.add(i > l ? source.substring(l, i) : "");
                if (i == s.length) {
                    break;
                }
                l = i + 1;
            }
        }
        al.add(i > l ? source.substring(l, i) : "");
        return al;
    }
    
    	    
    public static Collection<String> toTrimmedStrings(
      final String source,
      final String delimiters) {
        final ArrayList<String> al = new ArrayList();
        if (source != null){
            final char[] s = source.toCharArray();
            int l = 0, i = 0;

            for (; i < s.length; i++) {
                if (delimiters.indexOf(s[i]) >= 0) {
                    al.add(i > l ? source.substring(l, i).trim() : "");
                    if (i == s.length) {
                        break;
                    }
                    l = i + 1;
                }
            }
            al.add(i > l ? source.substring(l, i).trim() : "");
        }
        return al;
    }

    /**
     *
     * @param c
     * @return list copy of c if c.size()>1
     */
    public static Collection sortIntoCopyIfNecessary(Collection c) {
        if (c.size() > 1) {
            final ArrayList al = new ArrayList(c);
            Collections.sort(al);
            return al;
        }
        return c;
    }
    
    public static String []trim(final String []input){
    	final Collection<String>c=new ArrayList<String>();
    	for (final String s:input){
    		final String _s=s.trim();
   			c.add(_s);
    	}
    	return c.toArray(new String[c.size()]);
    }

    public static String[] split(
      final String source,
      final String delimiters) {
        if (source == null) {
            return new String[0];
        }
        final Collection<String> c = toStrings(source, delimiters);
        return c.toArray(new String[c.size()]);
    }
    
    public static String[] splitnTrim(
      final String source,
      final String delimiters) {
        if (source == null) {
            return new String[0];
        }
        final Collection<String> c = toTrimmedStrings(source, delimiters);
        return c.toArray(new String[c.size()]);
    }

    public static Collection split(
      final String source,
      final String regex,
      final Collection c,
      final boolean trim,
      final boolean toLowerCase,
      final boolean ignoreEmpty) {
        if (source != null) {
            final String[] s = source.split(regex);
            if (!trim && !toLowerCase) {
                addAll(c, s);
            } else {
                for (int i = 0; i < s.length; i++) {
                    String p = null;
                    if (trim) {
                        p = s[i].trim();
                    }
                    if (toLowerCase) {
                        p = s[i].toLowerCase();
                    }
                    if (!ignoreEmpty || !Basics.isEmpty(p)) {
                        c.add(p);
                    }
                }
            }
        }
        return c;
    }

    public static int[] splitInt(final String source, final String delimiter) {
        if (source != null) {
            String[] values = split(source, delimiter);
            int[] intValues = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                intValues[i] = Integer.parseInt(values[i]);
            }
            return intValues;
        }
        return null;
    }

    public static boolean[] splitBoolean(final String source,
                                         final String delimiter) {
        if (source != null) {
            String[] values = split(source, delimiter);
            boolean[] booleanValues = new boolean[values.length];
            for (int i = 0; i < values.length; i++) {
                booleanValues[i] = Boolean.valueOf(values[i]);
            }
            return booleanValues;
        }
        return null;
    }

    public static String join(final Collection c, String newDelimiter) {
        final StringBuilder sb = new StringBuilder(c.size() * 5);
        int i = 0;
        for (final Iterator it = c.iterator(); it.hasNext(); i++) {
            if (i > 0) {
                sb.append(newDelimiter);
            }
            sb.append(it.next());
        }
        return sb.toString();
    }

    public static String join(final Object[] a, String newDelimiter) {
        final StringBuilder sb = new StringBuilder(a.length * 5);
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(newDelimiter);
            }
            sb.append(a[i]);
        }
        return sb.toString();
    }

	public static int indexOfIgnoreCase(
			final List<String> c, final String lookFor) {
		for (int i=0;i<c.size();i++){
			final String arg=c.get(i);
			if (lookFor.equalsIgnoreCase(arg)){
				return i;
			}
		}
		return -1;
	}
	public static boolean containsIgnoreCase(
			final Collection<String> c, final String lookFor) {
		for (final String s : c) {
			if (lookFor.equalsIgnoreCase(s)) {
				return true;
			}
		}

		return false;
	}
    
    public static boolean contains(String source, ArrayList<String> searchStr) {
        for (String str: searchStr) {
        	if (!Basics.isEmpty(str) && source.indexOf(str) >= 0) {
        		return true;
        	}
        }
        return false;
    }

    public static boolean isEmpty(final Collection c) {
        return c == null || c.size() == 0;
    }

    public static float toFloat(Float f) {
        return f == null ? 0 : f.floatValue();
    }

    public static List getHead(SortedMap m, int n, List retVal) {
        if (retVal == null) {
            retVal = new ArrayList();
        }

        if (!isEmpty(m) && n > 0) {
            if (m.size() < n) {
                n = m.size();
            }
            final Object[] oa = new Object[n];
            for (int i = 0; i < n && m.size() > 0; i++) {
                Object o = m.lastKey();
                oa[i] = o;
                m = m.headMap(o);
            }
            for (int i = oa.length - 1; i >= 0; i--) {
                retVal.add(oa[i]);
            }
        }
        return retVal;
    }

    public static List getTail(Map m, int n, List retVal) {
        if (retVal == null) {
            retVal = new ArrayList();
        }

        if (!isEmpty(m)) {
            final Iterator it = m.keySet().iterator();
            for (int i = 0; i < n && it.hasNext(); i++) {
                retVal.add(it.next());
            }
        }
        return retVal;
    }

    public static void append(final Collection c, final StringBuilder sb,
                              final String delimiter) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(it.next());
        }
    }

    public static boolean isAnyEmpty(final Collection c) {
        if (c != null && c.size() > 0) {
            for (final Iterator it = c.iterator(); it.hasNext(); ) {
                final Object o = it.next();
                if (Basics.isEmpty(o)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isLeftGreater(final Comparable left,
                                        final Comparable right) {
        return compareTo(left, right) > 0;
    }

    public static int compareTo(final Comparable thisObject,
                                final Comparable thatObject) {
        return thisObject == null ? (thatObject == null ? 0 : -1) :
          (thatObject == null ? 1 : thisObject.compareTo(thatObject));
    }
    public static int removeNull(final Collection c){
        int removed = 0;
        for (final Iterator it = c.iterator(); it.hasNext(); ) {
            Object item = (Object) it.next();
            if (item == null) {
                removed++;
                it.remove();

            }

        }
        return removed;
    }
    
    public static String[] replaceAll(final String[] collection, final String find, final String replaceWith){
        String[] returnValues = new String[collection.length];
        for (int i = 0; i <collection.length; i++) {
            if (collection[i].equals(find)) {
            	returnValues[i] = replaceWith;
            }
            else {
            	returnValues[i] = collection[i];
            }
        }
        return returnValues;
    }

    public static int removeMissing(
      final MapOfMany removeFromHere,
      final Collection ifNotInHere) {
        final Collection c2 = new ArrayList(removeFromHere.keySet());
        int removed = 0;
        for (final Object ir : c2) {
            if (!ifNotInHere.contains(ir)) {
                removeFromHere.removeAll(ir);
                removed++;
            }
        }
        return removed;
    }

    public static int removeMissing(
      final Collection removeFromHere,
      final Collection ifNotInHere) {
        final Collection c2 = new ArrayList(removeFromHere);
        int removed = 0;
        for (final Object ir : c2) {
            if (!ifNotInHere.contains(ir)) {
                removeFromHere.remove(ir);
                removed++;
            }
        }
        return removed;
    }
    public static boolean equalsObjectOrCollectionOrMapOrArray(final Object thisObject, final Object thatObject) {
		if (thisObject instanceof Collection && thatObject instanceof Collection) {
			return equals((Collection) thisObject, (Collection) thatObject);
		}
		if (thisObject instanceof Map && thatObject instanceof Map) {
			return equals((Map) thisObject, (Map) thatObject);
		}
		if (thisObject instanceof Object[] && thatObject instanceof Object[]) {
			return equals((Object[]) thisObject, (Object[]) thatObject);
		}
		return equals(thisObject, thatObject);
	}

    public static boolean equals(final Object thisObject,
                                 final Object thatObject) {
    	if (thisObject==thatObject) {
    		return true;
    	}
        if (thatObject != null) { // one is non NULL
            return thatObject.equals(thisObject);
        }
        return thisObject.equals(thatObject);
    }

    public static boolean equalsIgnoreCase(final String thisObject,
                                 final String thatObject) {
    	if (thisObject==thatObject) {
    		return true;
    	}
        if (thatObject != null) { // one is non NULL
            return thatObject.equalsIgnoreCase(thisObject);
        }
        return  thisObject.equalsIgnoreCase(thatObject);
    }

    private static boolean equalsOrderDoesNotMatter(final Collection<?> s1, final Collection<?> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }

        for (Object s1Item : s1) {
            boolean found = false;
            for (Object s2Item : s2) {
                if (equalsObjectOrCollectionOrMapOrArray(s2Item, s1Item)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static <T> boolean equalsOrderMatters(
      final Collection<T> thisCollection,
      final Collection<T> thatCollection) {
        for (final Iterator thisIterator = thisCollection.iterator(),thatIterator = thatCollection.iterator();
          thisIterator.hasNext();
          /*equals sizes means thatIt.hasNext() is guaranteed to end at the same time as thisIt.hasNext() */
          ) {
            if (!equalsObjectOrCollectionOrMapOrArray(thisIterator.next(), thatIterator.next())) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> boolean equals(
    	      final Collection<T> thisCollection,
    	      final Collection<T> thatCollection) {
    	if (thisCollection == thatCollection) { // catches mutual NULL too
            return true;
        }
        if (thisCollection == null || thatCollection == null) { // one is non NULL
            return false;
        }
        if (thisCollection.size() != thatCollection.size()) {
            return false;
        }
        if(thisCollection instanceof List && thatCollection instanceof List) {
    		return equalsOrderMatters(thisCollection, thatCollection);
    	}
        return equalsOrderDoesNotMatter(thisCollection, thatCollection);
    }


    public static boolean equals(final Object[] thisArray,
                                 final Object[] thatArray) {
        if (thisArray == thatArray) { // catches mutual NULL too
            return true;
        }
        if (thisArray == null || thatArray == null) { // one is non NULL
            return false;
        }
        if (thisArray.length != thatArray.length) {
            return false;
        }
        for (int i = 0; i < thatArray.length; i++) {
            if (!equalsObjectOrCollectionOrMapOrArray(thisArray[i], thatArray[i])) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean equals(final Map m1, final Map m2) {
    	if (m1==m2) {
    		return true;
    	}
    	if (m1==null || m2==null) {
    		return false;
    	}
    	if(m1.size()!=m2.size()) {
    		return false;
    	}
    	for (final Object key:m1.keySet()) {
    		if (!m2.containsKey(key)) {
    			return false;
    		}
    		if (!Basics.equals(m1.get(key), m2.get(key))) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static boolean equals(final MapOfMany m1, final MapOfMany m2) {
    	if (m1==m2) {
    		return true;
    	}
    	if (m1==null || m2==null) {
    		return false;
    	}
    	if(m1.size()!=m2.size()) {
    		return false;
    	}
    	for (final Object key:m1.keySet()) {
    		if (!m2.containsKey(key)) {
    			return false;
    		}
    		if (!Basics.equals(m1.getCollection(key), m2.getCollection(key))) {
    			return false;
    		}
    	}
    	return true;
    }

    public static boolean equals(final MapOfMapOfMany m1, final MapOfMapOfMany m2) {
    	if (m1==m2) {
    		return true;
    	}
    	if (m1==null || m2==null) {
    		return false;
    	}
    	if(m1.size()!=m2.size()) {
    		return false;
    	}
    	if (!equals(m1.keySet(),m2.keySet())){
    		return false;
    	}

    	for (final Object key:m1.keySet()) {
    		if (!Basics.equals(m1.get(key), m2.get(key))) {
    			return false;
    		}
    	}
    	return true;
    }

    public static boolean containsAll(
          final Collection superSet, final Object[] subSet) {
            if (subSet == null || superSet == null) { // one is NON null
                return false;
            }
            if (subSet.length > superSet.size()) {
                return false;
            }
            for (int i = 0; i < subSet.length; i++) {
                if (!superSet.contains( subSet[i])) {
                    return false;
                }
            }

            return true;
        }

    public static boolean containsAll(
            final int []superSet, final int []subSet) {
              if (subSet == null || superSet == null) { // one is NON null
                  return false;
              }
              if (subSet.length > superSet.length) {
                  return false;
              }
              for (int i = 0; i < subSet.length; i++) {
                  if (!contains( superSet, subSet[i])) {
                      return false;
                  }
              }

              return true;
          }

    public static boolean containsAll(
      final Object[] superSet, final Object[] subSet) {
        if (subSet == superSet) { // catches mutual NULL too
            return true;
        }
        if (subSet == null || superSet == null) { // one is NON null
            return false;
        }
        if (subSet.length > superSet.length) {
            return false;
        }
        for (int i = 0; i < subSet.length; i++) {
            if (!equalsAny(superSet, subSet[i])) {
                return false;
            }
        }

        return true;
    }

    public static boolean containsAll(
      final Collection superSet, final Collection subSet) {
        if (subSet == superSet) { // catches mutual NULL too
            return true;
        }
        if (subSet == null || superSet == null) { // one is NON null
            return false;
        }
        if (subSet.size() > superSet.size()) {
            return false;
        }
        for (final Iterator it=subSet.iterator(); it.hasNext();) {
            if (!superSet.contains(it.next())) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean startsWith(final T [] superSet,
                                     final T[] subSet) {
        if (superSet != null && subSet != null) {
            if (superSet.length >= subSet.length) {
                for (int i = 0; i < subSet.length; i++) {
                    if (!superSet[i].equals(subSet[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean startsWith(final int[] superSet,
			final int[] subSet) {
		if (superSet != null && subSet != null) {
			if (superSet.length >= subSet.length) {
				for (int i = 0; i < subSet.length; i++) {
					if (superSet[i]!= subSet[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

    public static boolean needsHtmlEncoding(final char[] c) {
        final int n = c.length;
        for (int i = 0; i < n; i++) {
            switch (c[i]) {
            case '>':
                return true;
            case '<':
                return true;
            case '&':
                return true;
            case '"':
                return true;
            }
        }
        return false;
    }

    public static Object[] append(Object[] newArray, Object[] oldArray, int idx) {
        for (int i = 0; i < oldArray.length; i++) {
            if (idx >= newArray.length) {
                break;
            }
            newArray[idx++] = oldArray[i];
        }
        return newArray;
    }

    public static Object[] toArray(
      final Object[] newArray,
      final Object[] a1,
      final Object[] a2) {
        append(newArray, a1, 0);
        return append(newArray, a2, a1.length);
    }

    public static String encodeHtml(final String input) {
    	return encodeHtml(input, false);
    }
    
    public static String encodeHtml(final String input, final boolean lookForImgPrefix) {
        final String value;
        if (Basics.isEmpty(input)) {
            value = "";
        } else {
            String v = snip(input, "<body>", "</body>");
            if (v == null) {
                v = snip(input, "<html>", "</html>");
                String prefix="";
                if (lookForImgPrefix && v!=null && v.startsWith("<img src='")){
                	final int idx=v.indexOf("'>", 1);
                	if (idx>=0) {
                		prefix=v.substring(0, idx+2);
                		v=v.substring(idx+2);
                	}
                }
                if (v != null) {
                    value = concat(prefix, encodeXmlOrHtml(v));
                } else {
                    value = concat(prefix, encodeXmlOrHtml(input));
                }
            } else {
                value = encodeXmlOrHtml(v);
            }
        }
        return value;
    }

    public static String encodeXmlOrHtml(final Object input) {
        if (input != null){
            String out=input.toString();
            char[] c = out.toCharArray();
            if (needsHtmlEncoding(c)) {
                String convert = null;
                int start = 0;
                StringBuilder sb = new StringBuilder();
                int n = c.length;
                for (int i = 0; i < n; i++) {
                    switch (c[i]) {
                    case '>':
                        convert = "&gt;";
                        break;
                    case '<':
                        convert = "&lt;";
                        break;
                    case '&':
                        convert = "&amp;";
                        break;
                    case '"':
                        convert = "&quot;";
                        break;
                    default:
                        continue;
                    }
                    if (i > start) {
                        sb.append(c, start, i - start);
                    }
                    start = i + 1;
                    sb.append(convert);
                }
                sb.append(c, start, n - start);
                out=sb.toString();
            }
            return out;
        }
        return "";
    }

    public static Object[] add(final Object[] in1, final Object[] in2,
                               final Object[] out) {
        Collection c = new ArrayList(Arrays.asList(in1));
        c.addAll(Arrays.asList(in2));
        return c.toArray(out);
    }

    public static <T> Collection getCommon(
      final T [] one,
      final T [] another) {
        final Collection c1 = Arrays.asList(one), c2 = Arrays.asList(another);
        return getCommon(c1, c2);
    }

    public static <T> Collection<T> getCommon(
      final Collection<T> one,
      final Collection<T> another) {
        final ArrayList<T> common = new ArrayList();
        for (final Iterator<T> it = one.iterator(); it.hasNext(); ) {
            final T object = it.next();
            if (another.contains(object)) {
                common.add(object);
            }
        }
        return common;
    }

    public static boolean hasCommon(
      final Collection one,
      final Collection another) {
        final ArrayList common = new ArrayList();
        for (final Iterator it = one.iterator(); it.hasNext(); ) {
            final Object object = it.next();
            if (another.contains(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Generic collection subtraction
     *  
     * @param <T>  DUH ... any type you want silly goose
     * @param superset the super set of things to be subtracted FROM
     * @param subset the subset of things to be subtracted from the super set 
     * @return the difference (resultant collection of super set minus sub set)
     */
    public static <T> ArrayList<T> subtract(
      final Collection<T> superset,
      final Collection <T>subset) {
        final ArrayList<T> objects = new ArrayList<T>();
        for (final T o:superset ) {
            if (!subset.contains(o)) {
                objects.add(o);
            }
        }
        return objects;
    }

    public static Collection subtract(
      final Object[] superset,
      final Object[] subset) {
        final ArrayList returnValue = new ArrayList();
        for (int i = 0; i < superset.length; i++) {
            final Object object = superset[i];
            if (!equalsAny(subset, object)) {
                returnValue.add(object);
            }
        }
        return returnValue;
    }

    public static <T> void getUniqueAndCommon(
      final Collection<T> This,
      final Collection<T> That,
      final Collection<T> uniqueToThis,
      final Collection<T> uniqueToThat,
      final Collection<T> commonToBoth){
        uniqueToThis.addAll(subtract(This, That));
        uniqueToThat.addAll(subtract(That, This));
        commonToBoth.addAll(getCommon(This,That));
    }

    public static void debug(final Collection c) {
        Iterator it = c.iterator();
        for (int i = 0; it.hasNext(); i++) {
            if (i > 0) {
                System.out.println(", ");
            }
            System.out.print(i);
            System.out.print(".  ");
            System.out.print(it.next());
        }
        System.out.println();
    }
    public static final boolean isPowerPC=isPowerPC();
    private static boolean isPowerPC(){
    	return Basics.equals("ppc", getSystemProperty("os.arch"));
    }

    public static String getSystemProperty(final String property) {
        String value;
        try {
            value = System.getProperty(property);
        } catch (SecurityException e) {
            value = null;
            Pel.log.print(e);
        }
        return value;
    }

    public static void setUpMac(com.apple.eawt.Application aapp) {
        MacintoshBasics.registerMacOSXApplication (aapp);
    }

    public static boolean isJava5;
    static {
    	final String javaVersion = System.getProperty("java.version");
		isJava5=javaVersion.startsWith("1.5");		
    }
    public static boolean isJava6;
    static {
    	final String javaVersion = System.getProperty("java.version");
		isJava6=javaVersion.startsWith("1.6");
		
    }
    public static boolean isMac() {
        return getSystemProperty("os.name").indexOf("Mac OS") >= 0;
    }

    public static boolean isWin2000() {
        return getSystemProperty("os.name").indexOf("Windows 2000") != -1;
    }

    public static boolean isWinXP() {
        return getSystemProperty("os.name").indexOf("Windows XP") != -1;
    }

    public static boolean isWin95() {
        return getSystemProperty("os.name").indexOf("Windows 95") != -1;
    }

    public static boolean isWin98() {
        return getSystemProperty("os.name").indexOf("Windows 98") != -1;
    }

    public static boolean isWin9X() {
        return isWin95() || isWin98();
    }
    
    public static boolean isWin7() {
        return getSystemProperty("os.name").indexOf("Windows 7") != -1;
    }

    public static boolean isEvilEmpireOperatingSystem() {
        return getSystemProperty("os.name").indexOf("Windows") != -1;
    }

    public static boolean isWinNT() {
        return getSystemProperty("os.name").indexOf("Windows NT") != -1;
    }
    
    public static boolean isWinVista() {
    	final String system=getSystemProperty("os.name");
        return system.indexOf("Windows Vista") != -1 ||
        //Known issue with Sun. Proper OS name is returned only with JDK 1.6
        	getSystemProperty("os.name").indexOf("Windows NT (unknown)") != -1;
    }

    public final static String lineFeed = System.getProperty("line.separator");
    public final static String htmlLineFeed = "<br>" + lineFeed;

    public static void eol(final StringBuilder sb) {
        sb.append(lineFeed);
    }


    public final static int intValueOfCharA = (int) 'A';

    public static String convertToCharNumbering(final int value) {
        return Character.toString((char)(intValueOfCharA + value));
    }

    public static String convertToCharNumbering(final Integer value) {
        if (value != null) {
            return convertToCharNumbering(value.intValue());
        }
        return "A";
    }

    public static int convertFromCharNumbering(final String value) {
        int row = 0;
        if (value != null) {
            char[] c = value.toString().toUpperCase().toCharArray();
            row = ((int) c[0]) - intValueOfCharA;
        }
        return row;
    }

    /**
     * Round a double value to a specified number of decimal
     * places.
     *
     * @param arg the value to be rounded.
     * @param places the number of decimal places to round to.
     * @return val rounded to places decimal places.
     */
    public static double round(final double _arg, final int places) {
        long factor = (long) Math.pow(10, places);

        // Shift the decimal the correct number of places
        // to the right.
        final double arg = _arg * factor;

        // Round to the nearest integer.
        long tmp = Math.round(arg);

        // Shift the decimal the correct number of places
        // back to the left.
        return (double) tmp / factor;
    }

    public static float round(final float arg, final int places) {
        return (float) round((double) arg, places);
    }
    

    public static float round(final float arg) {
        return (float) round((double) arg, 0);
    }
    
    public static float roundUp(final int arg, final int number) {
    	final int i=arg/number;
    	final int ii=arg%number;
    	final int value=arg*number+(number-ii);
    	return value;
    }

    public static float roundDown(final int arg, final int number) {
    	final int i=arg/number;
    	final int ii=arg%number;
    	final int value=arg*number -(ii);
    	return value;
    }

    /**
     *
     * Useful for separating file name from extension
     */
    public static String getBeforeLastDelimiter(
      final String s,
      final char delimiter) {
        String ext = null;
        int i = s.lastIndexOf(delimiter);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(0, i);
        }
        return ext;
    }

    public static String getAfterLastDelimiter(final String s,
                                               final char delimiter) {
        String ext = null;
        int i = s.lastIndexOf(delimiter);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1);
        }
        return ext;
    }

    /**
     *
     * Useful for extracting a file extension.
     */
    public static String getLastField(String s, char delimiter) {
        String ext = null;
        int i = s.lastIndexOf(delimiter);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1);
        }
        return ext;
    }

    public static String getFirstField(String s, char delimiter) {
        String ext = null;
        int i = s.indexOf(delimiter);
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(0, i);
        }
        return ext;
    }

    public static String capitalize(final String s) {
        return s.length() == 0 ? "" :
          concat(s.substring(0, 1).toUpperCase(),  s.substring(1));
    }

    public static String uncapitalize(String s) {
    	if (s==null){
    		return null;
    	}
        return s.length() == 0 ? "" :
          concat(s.substring(0, 1).toLowerCase(), s.substring(1));
    }

    public static int[] geometric(final int base, final int digitSize) {
        int num = base, times = 0;
        for (num = num / digitSize; num > 0; num = num / digitSize) {
            times++;
        }
        int[] a = new int[times];
        for (int i = times; i > 0; i--) {
            int div = 1;
            for (int j = 0; j < i; j++) {
                div *= digitSize;
            }
            a[i - 1] = div;
        }
        int[] retVal = new int[times + 1];
        num = base;
        for (int i = times; i > 0; i--) {
            retVal[i] = num / a[i - 1];
            num -= (retVal[i] * a[i - 1]);
        }
        retVal[0] = base % digitSize;
        return retVal;
    }

    public static String toExcelColumnLabel(final int number) {

        int[] a = geometric(number, 26);
        final StringBuilder sb = new StringBuilder();
        for (int i = a.length - 1; i >= 0; i--) {
            sb.append((char) ((int) 'A' + a[i]));
        }
        return sb.toString();
    }

    public final static java.text.DecimalFormat num =
      new java.text.DecimalFormat("#,###,###.###");


    public static Object decode(final Object o, final Class cls) {
        if (o.getClass().equals(cls)) {
            return o;
        }
        return decode(o.toString(), cls);
    }
    
    public static String decode(final Integer i){
    	return i==null?"":i.toString();
    }
    public static boolean isValidDecimalNumber(final String sn) {
		if (sn.matches("[\\d]*.[\\d]*") && sn.matches("[^a-zA-Z-+!@#$%&()*^]*")) {
			return true;
		}
		return false;
	}

    public static String encodeRightJustify(final Integer l, final int size){
    	return l==null?"":encodeRightJustify(l.longValue(), size);
    }
    public static String encodeRightJustify(final long l, final int size){
    	return prepend(Long.toString(l),3,"&nbsp;");
    }
    
    public static String align(final String s) {
        int i=0;
        while (s.charAt(i)=='0') i++;
        return s.substring(0,i+1).replace('0', ' ')  +  s.substring(i+1);
    }

	public static boolean isValidWholeNumber(final String sn) {
		if (sn.matches("[^a-zA-Z-+!@#$%&()*^]*")) {
			return true;
		}
		return false;
	}

    public static Object decode(final String s, final Class cls) {
        try {
			final String sn = removePlusSign(stripDoubleQuotes(s)).trim();
			if (cls == Integer.class || cls == Long.class || cls == Short.class) {
				final String test;
				if (sn.startsWith("-")) {
					test=sn.substring(1);
				} else {
					test=sn;
				}
				
				if (isValidWholeNumber(test)) {
					final Number number = num.parse(sn);
					if (cls == Integer.class) {
						return new Integer(number.intValue());
					} else if (cls == Long.class) {
						return new Long(number.longValue());
					} else if (cls == Short.class) {
						return new Short(number.shortValue());
					}
				}
			}
			if (cls == Float.class || cls == Double.class) {
				final String test;
				if (sn.startsWith("-")) {
					test=sn.substring(1);
				} else {
					test=sn;
				}
				
				if (isValidDecimalNumber(test)) {
					final Number decnumber = num.parse(sn);
					if (cls == Float.class) {
						return new Float(decnumber.floatValue());
					} else if (cls == Double.class) {
						return new Double(decnumber.doubleValue());
					}
				}

			}
        } catch (final java.text.ParseException pe) {
            Pel.log.warn(pe);
        }
        return null;
    }

    public static String encode(final double f) {
        return num.format(f);
    }

    public static String encode(final long n) {
        return num.format(n);
    }

    public static String encode(final Integer n) {
        return encode(n.longValue());
    }

    public static String encode(final Long n) {
        return encode(n.longValue());
    }

    public static String encode(final Float f) {
        return f == null ? "" : encode(f.doubleValue());
    }

    public static String encode(final Double f) {
        return encode(f.doubleValue());
    }

    public static String encode(final Object o) {
        if (o instanceof Double) {
            return encode((Double) o);
        } else if (o instanceof Float) {
            return encode((Float) o);
        } else if (o instanceof Integer) {
            return encode((Integer) o);
        } else if (o instanceof Long) {
            return encode((Long) o);
        } else if (o instanceof String){
        	// string must be either a number or a comma separated list of numbers
        	if ( ( (String)o).matches(" *\\d+[\\.\\,\\d ]*")){
        		return (String)o;
        	}

        }
        return null;
    }

    public static boolean appearsToBeADomainName(final String
                                                 stringEndingWithDomainName) {
        final String lowerCaseStringEndingWithDomainName =
          stringEndingWithDomainName.
          toLowerCase();
        for (int i = 0; i < validDomainExtensions.length; i++) {
            if (lowerCaseStringEndingWithDomainName.endsWith(
              validDomainExtensions[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean appearsToBeAnEmailAddress(final String str) {
        if (appearsToBeADomainName(str)) {
            int idx = str.indexOf('@');
            if (idx >= 0) {
                return str.substring(idx + 1).indexOf('@') < 0;
            }
        }
        return false;
    }

    public static String translate(String arg, char from,	char to) {
		return arg == null ? null : arg.replace(from, to);
	}

    public static String toHtmlTable(final StringBuilder p_sb,
                                     final Collection c) {
        final StringBuilder sb = p_sb == null ? new StringBuilder() : p_sb;
        if (c.size() > 0) {
            sb.append("<table border='1'>");
            if (c != null) {
                for (final Iterator it = c.iterator(); it.hasNext(); ) {
                    sb.append("<tr><td>");
                    sb.append(it.next());
                    sb.append("</td></tr>");
                }
            }
            sb.append("</table>");
        }
        return sb.toString();
    }

    public static String toHtmlTableBody(final StringBuilder p_sb,
                                         final Collection[] c) {
        final StringBuilder sb = p_sb == null ? new StringBuilder() : p_sb;
        sb.append("<table>");
        for (int i = 0; i < c.length; i++) {
            sb.append("<tr>");
            for (final Iterator it = c[i].iterator(); it.hasNext(); ) {
                sb.append("<td>");
                sb.append(it.next());
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static String toHtmlError(final String heading, final String msg) {
        return  concat(startHtmlError(heading), msg,  endHtml());
    }

    public static String toHtmlErrorUncentered(final String heading,
                                               final String msg) {
        return  concat(startHtmlErrorUncentered(heading),  msg,  endHtml());
    }

    public static String toHtmlCentered(final String heading, final String msg) {
        return  concat(startHtml(heading), msg, endHtml());
    }

    public static String toHtmlUncenteredBigTitle(final String heading,
                                          final String msg) {
        return  concat(startHtmlUncenteredBigTitle(heading), msg, endHtmlUncentered());
    }

    public static String toHtmlUncentered(final String heading,
                                          final String msg) {
        return  concat(startHtmlUncentered(heading), msg, endHtmlUncentered());
    }

    public static String toHtmlUncenteredSmall(final String heading,
            final String msg) {
    	return concat(startHtmlUncenteredSmall(heading), msg, endHtmlUncentered());
    }

    public static String toHtmlErrorUncentered(final String msg) {
        return concat(startHtmlUncentered(), "<font color='red'>", msg, "</font>", endHtmlUncentered());
    }
    
    public static String toHtmlUncentered(final String msg) {
        return concat(startHtmlUncentered(), msg, endHtmlUncentered());
    }
    
    public static String toHtmlUncenteredNarrow(final String msg) {
        return concat(HTML_NARROW_START, msg, endHtmlUncentered());
    }

    public static String toHtmlCentered(final String msg) {
        return concat(startHtml(),  msg, endHtml());
    }

    public static String startHtmlErrorUncentered(final String heading) {
        if (heading != null) {
            return concat(
              HTML_START_UNCENTERED_WITH_ERROR,
              heading, 
              HTML_END_HEAD4);
        }
        return startHtmlUncentered();
    }

    public static String startHtmlUncenteredBigTitle() {
        return HTML_START_UNCENTERED18;
    }

    public static String startHtmlUncentered() {
        return HTML_START_UNCENTERED11;
    }

    public static String startHtmlError(final String heading) {
        if (heading != null) {
            return concat(
              HTML_START_CENTERED_WITH_ERROR, 
              heading, 
              HTML_END_HEAD4);
        }
        return startHtml();
    }

    public static String startHtml() {
        return HTML_START_CENTERED11;
    }

    public static String encodePixelDescription(
      final int width,
      final int height) {
        return encodePixelDescription(null, width, height);
    }

    public static String encodePixelDescription(
      final StringBuilder p_sb,
      final int width,
      final int height) {
        final StringBuilder sb = (p_sb == null) ? new StringBuilder() : p_sb;
        sb.append(width);
        sb.append(" pixels X ");
        sb.append(height);
        sb.append(" pixels ");
        return sb.toString();
    }

    public final static String HTML_NARROW_START="<html><body><table cellpadding='5'><tr><td>";
    private final static String
      HTML_START18 = "<html><body><table cellpadding='18'><tr>",
    HTML_START11 = "<html><body><table cellpadding='11'><tr>",
    HTML_END = "</td></tr></table></body></html>",
    HTML_BASIC_START = "<html><body>",
    HTML_BASIC_END = "</center></body></html>",
    HTML_START_HEAD2 = "<h2><font color='blue'>",
    HTML_START_HEAD3 = "<h3><font color='blue'>",
    HTML_START_HEAD4 = "<h4><font color='blue'>",
    HTML_START_HEAD5 = "<font color='blue'>",
    HTML_START_ERROR = "<h4><font color='red'>",
    HTML_END_HEAD2 = "</font></h2><hr>",
    HTML_END_HEAD3 = "</font></h3><hr>",
    HTML_END_HEAD4 = "</font></h4><hr>",
    HTML_END_HEAD5 = "</font><hr>",
    HTML_END_HEAD5_NOHR = "</font>",
    HTML_BASIC_START_CENTERED=HTML_BASIC_START+"<center>",
    HTML_START_CENTERED11 = HTML_START11 + "<td align='center'>",
    HTML_START_CENTERED_WITH_HEAD = HTML_START_CENTERED11 + HTML_START_HEAD4,
    HTML_START_CENTERED_WITH_ERROR = HTML_START_CENTERED11 + HTML_START_ERROR,
    HTML_START_UNCENTERED18 = HTML_START18 + "<td>",
    HTML_START_UNCENTERED11 = HTML_START11 + "<td>",
    HTML_START_UNCENTERED_WITH_HEAD_HUGE_TITLE = HTML_START_UNCENTERED18 + HTML_START_HEAD2,
    HTML_START_UNCENTERED_WITH_HEAD_BIG_TITLE = HTML_START_UNCENTERED18 + HTML_START_HEAD3,
    HTML_START_UNCENTERED_WITH_HEAD = HTML_START_UNCENTERED11 + HTML_START_HEAD4,
    HTML_START_UNCENTERED_WITH_SMALL_HEAD = HTML_START_UNCENTERED11 + HTML_START_HEAD5,
    HTML_START_UNCENTERED_WITH_ERROR = HTML_START_UNCENTERED11 + HTML_START_ERROR;

    public static String startHtmlUncenteredHugeTitle(final String heading) {
        if (heading != null) {
            return HTML_START_UNCENTERED_WITH_HEAD_HUGE_TITLE + heading + HTML_END_HEAD2;
        }
        return startHtmlUncenteredBigTitle();
    }

    public static String startHtmlUncenteredBigTitle(final String heading) {
        if (heading != null) {
            return HTML_START_UNCENTERED_WITH_HEAD_BIG_TITLE + heading + HTML_END_HEAD3;
        }
        return startHtmlUncenteredBigTitle();
    }

    public static String startHtmlUncentered(final String heading) {
            if (heading != null) {
                return HTML_START_UNCENTERED_WITH_HEAD + heading +HTML_END_HEAD4 ;
            }
            return startHtmlUncentered();
      }

    public static String startHtmlUncenteredSmallNoLine(final String heading) {
        if (heading != null) {
            return HTML_START_UNCENTERED_WITH_SMALL_HEAD + heading +HTML_END_HEAD5_NOHR ;
        }
        return startHtmlUncentered();
    }
    
    public static String startHtmlUncenteredSmall(final String heading) {
        if (heading != null) {
            return HTML_START_UNCENTERED_WITH_SMALL_HEAD + heading +HTML_END_HEAD5 ;
        }
        return startHtmlUncentered();
    }

    public static String startHtmlUncenteredWithImage(final String heading,ImageIcon icon, String msg) {
            if (heading != null) {
                return "<html><body><table cellpadding='11'><tr>" + "<font size = 6 color='blue'>" + heading 
                +"&nbsp&nbsp&nbsp"+
                "<img src=\""+icon+"\">" +"</font>" + msg + "</td></tr></table></body></html>";    
            }
            return startHtmlUncentered();
        }
    
    
    public static String startHtml(final String heading) {
        if (heading != null) {
            return HTML_START_CENTERED_WITH_HEAD + heading + HTML_END_HEAD4;
        }
        return startHtml();
    }

    public static String endHtml() {
        return HTML_END;
    }

    public static String endHtmlUncentered() {
        return HTML_END;
    }

    public static String toString(final Class[] values) {
        final StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(values[i].getName());
            }
        }
        return sb.toString();
    }

    public static boolean isSuperSet(final Object[] superSet,
                                     final Object[] subSet) {
        for (int i = 0; i < subSet.length; i++) {
            if (!equalsAny(superSet, subSet[i])) {
                return false;
            }
        }
        return true;
    }

    public static String toStringStartingAt(
      final Object[] values,
      final int startingAt) {
        final StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (int i = startingAt; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(values[i]);
            }
        }
        return sb.toString();

    }

    public static String toString(final Object[] values) {
        final StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(values[i]);
            }
        }
        return sb.toString();
    }

    public static String toString(
      final Object[] values,
      final StringEncoder encoder) {
            final StringBuilder sb = new StringBuilder();
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(encoder.toString(values[i]));
                }
            }
            return sb.toString();
    }
    public static String toString(final Object[] values, final int breakEvery) {
        final StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(",&nbsp;");
                    if (((i) % breakEvery) == 0) {
                        sb.append("<br>");
                    }
                }

                sb.append(values[i]);
            }
        }
        return sb.toString();
    }

    public static String toString(final Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Collection) {
            return toString((Collection) o);
        }
        if (o instanceof Object[]) {
        	return toString( (Object [])o);
        }
        return o.toString();
    }

    public static String toStringOrNull(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Collection) {
            return toString((Collection) o);
        }
        return o.toString();
    }

    public final static char COMMA = ',';

    public static String toString(final int[] d) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(d[i]);
        }
        return sb.toString();
    }


    public static String toString(final double[] d) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(d[i]);
        }
        return sb.toString();
    }

    public static String toString(final float[] f) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < f.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(f[i]);
        }
        return sb.toString();
    }

    public static Date getDateFromStringEncodedLong(final String value) {
        if (value != null) {
            final long l = Long.parseLong(value);
            return new Date(l);
        }
        return null;
    }

    public static void exec(
      final String executable,
      final File fileToExecute,
      final boolean readStdOutUntilExits) throws IOException {
        String exe;
        if (Basics.isEmpty(executable)) {
            if (isMac()) {
                final File f = com.MeehanMetaSpace.MacintoshBasics.findApp(
                  fileToExecute);

                if (f == null) {
                    exe = null;
                } else {
                    exe = f.getAbsolutePath();
                }
            } else {
                exe = null;
            }
        } else {
            exe = executable;
        }
        if (exe != null) {
            final String[] cmd;
            if (isMac()) {
                cmd = new String[] {
                      "open", "-a", exe, fileToExecute.getAbsolutePath()};
            } else {
                cmd = new String[] {
                      exe, fileToExecute.getAbsolutePath()};
            }
            final Process p = Runtime.getRuntime().exec(cmd);
            if (readStdOutUntilExits) {
                final BufferedReader br =
                  new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }

        }
    }

    public static void debug(final Properties properties) {
        final Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            final Object o = e.nextElement();
            final Object value = properties.get(o);
            System.out.println("name=" + o + ", value=" + value);
        }
    }

    public static void move(
      final java.util.List l, final int from, final int to) {
        final int n = l.size();
        if (from >= 0 && from < n && to >= 0 && to < n && from != to) {
            final Object moved = l.remove(from);
            l.add(to, moved);
        }
    }

    public static String STYLE_GUIDE = "";

    public static String getSystemStory() {
        final StringBuilder sb = new StringBuilder();
        eol(sb);
        sb.append("computer name=");
        sb.append(IoBasics.getComputerNameOfThisMachine());
        eol(sb);
        sb.append("user=");
        sb.append(getSystemProperty("user.name"));
        sb.append(", home=");
        sb.append(getSystemProperty("user.home"));
        eol(sb);
        sb.append("os=");
        sb.append(getSystemProperty("os.name"));
        sb.append(", version=");
        sb.append(getSystemProperty("os.version"));
        sb.append(", architecture=");
        sb.append(getSystemProperty("os.arch"));
        sb.append(isPowerPC()?" (Power PC)":"");
        eol(sb);
        sb.append("java=");
        sb.append(getSystemProperty("java.vendor"));
        sb.append(", ");
        sb.append(getSystemProperty("java.version"));
        sb.append(", home=");
        sb.append(getSystemProperty("java.home"));
        eol(sb);
        return sb.toString();
    }

    public static Object[] removeDuplicatesAndNulls(final ArrayAllocater
      arrayAllocater,
      final Object[] objects) {
        int newSize = objects.length;
        final boolean[] weed = new boolean[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (weed[i]) {
                continue;
            }
            final Object f = objects[i];
            if (f == null) {
                weed[i] = true;
                newSize--;
            } else {
                for (int j = i + 1; j < objects.length; j++) {
                    if (f.equals(objects[j])) {
                        weed[j] = true;
                        newSize--;
                    }
                }
            }
        }
        if (newSize == objects.length) {
            return objects;
        } else {
            final Object[] newSet = arrayAllocater.allocate(newSize);
            for (int i = 0, j = 0; i < objects.length; i++) {
                if (!weed[i]) {
                    newSet[j++] = objects[i];
                }
            }
            return newSet;
        }
    }

    public static String stripDoubleQuotes(final String s) {
        final int n = s.length();
        if (n > 1) {
            if (s.charAt(0) == '"' && s.charAt(n - 1) == '"') {
                return s.substring(1, n - 1);
            }
        }
        return s;
    }

    public static String removePlusSign(final String sn) {
		final int n = sn.length();
		if (n > 1) {
			if (sn.charAt(0) == '+') {
				return sn.substring(1, n);
			}
		}
		return sn;
	}
    public static ArrayAllocater getObjectArrayAllocater() {
        return new Basics.ArrayAllocater() {
            public Object[] allocate(final int size) {
                return new Object[size];
            }
        };
    }

    public static Object[] removeDuplicatesAndNulls(final Object[] objects) {
        return removeDuplicatesAndNulls(getObjectArrayAllocater(), objects);
    }

    public interface ArrayAllocater {
        Object[] allocate(int size);
    }


    public static ArrayAllocater getStringArrayAllocater() {
        return new Basics.ArrayAllocater() {
            public Object[] allocate(final int size) {
                return new String[size];
            }
        };
    }

    public static String[] removeDuplicatesAndNulls(final String[] strings) {
        return (String[]) removeDuplicatesAndNulls(
          Basics.getStringArrayAllocater(), strings);
    }

    public static Object[] retainAll(final ArrayAllocater arrayAllocater,
                                     final Object[] superSet,
                                     final Object[] subSet) {
        int n = 0;
        for (int i = 0; i < subSet.length; i++) {
            if (contains(superSet, subSet[i])) {
                n++;
            }
        }
        if (n > 0) {
            final Object[] newSet = arrayAllocater.allocate(superSet.length - n);
            for (int i = 0, j = 0; i < superSet.length; i++) {
                if (Basics.contains(subSet, superSet[i])) {
                    newSet[j++] = superSet[i];
                }
            }
            return newSet;
        }
        return superSet;
    }


    public static String urlEncode(final Collection l){
        final StringBuilder sb = new StringBuilder();
        for (final Object o : l) {
            sb.append(URLEncoder.encode(o==null?"":o.toString()));
            sb.append('&');
        }
        return sb.toString();
    }

    public static String urlEncode(final String []l){
        final StringBuilder sb = new StringBuilder();
        for (Object o : l) {
            sb.append(URLEncoder.encode(o==null?"":o.toString()));
            sb.append('&');
        }
        return sb.toString();
    }

    public static List<String> urlDecode(final String s){
        if (s!=null){
            final ArrayList names = new ArrayList();
            int start = 0;
            for (int idx = s.indexOf('&', start); idx >= 0;
              idx = s.indexOf('&', start)) {
                String name = s.substring(start, idx);
                final String n = URLDecoder.decode(name);
                if (!names.contains(n)) {
                    names.add(n);
                }
                start = idx + 1;
            }
            return names;
        }
        return Collections.EMPTY_LIST;
  }
    public static String handleSpecialCharacters(String testSpecialChars) {
		if (props == null) {
			props = new Properties();
			InputStream fin = null;
			try {
				fin = Basics.class
						.getResourceAsStream("swing/images/specialCharacters.properties");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				props.load(fin);
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Check if special chars exists.
		boolean specialChar = false;
		if (testSpecialChars != null)
			for (Enumeration e = props.keys(); e.hasMoreElements();) {
				specialChar = testSpecialChars.indexOf(Integer
						.parseInt((String) e.nextElement())) != -1;
				if (specialChar)
					break;
			}
		if (specialChar) {
			StringBuilder repRowLabel = new StringBuilder();
			if (testSpecialChars != null) {
				for (int i = 0; i < testSpecialChars.length(); i++) {
					repRowLabel.append(props.getProperty(""
							+ ((int) testSpecialChars.charAt(i)), ""
							+ testSpecialChars.charAt(i)));
				}
			}
			return repRowLabel.toString();
		}
		return testSpecialChars;
	}
  public static void addToMapOfLists(final Map<String, java.util.List<String>> map, final String key, final Object value){
      addToMapOfLists(map, key, value, true, true);
  }

  public static void addToMapOfLists(final Map<String, java.util.List<String>> map, final String key, final Object value, final boolean enforceNonNullKey, final boolean enforceUniqueValue){
      if (key != null || !enforceNonNullKey) {
          List list = (List) map.get(key);
          if (list == null) {
              list = new ArrayList();
              map.put(key, list);
          }
          if (!enforceUniqueValue || !list.contains(value)) {
              list.add(value);
          }
      }
  }

  public static boolean isWellFormedURL(final String s) {

      boolean value = false;

      if (!Basics.isEmpty(s)) {
          try {
              new URL(s);
              value = true;
          } catch (java.net.MalformedURLException e) {

          }
      }
      return value;
  }
  
  public static boolean equals(final int[] thisArray, final int[] thatArray) {
		if (thisArray == thatArray) { // catches mutual NULL too
			return true;
		}
		if (thisArray == null || thatArray == null) { // one is non NULL
			return false;
		}
		if (thisArray.length != thatArray.length) {
			return false;
		}
		for (int i = 0; i < thatArray.length; i++) {
			if (thisArray[i] != thatArray[i]) {
				return false;
			}
		}
		return true;
	}
	public static void highlightWithYellow(final StringBuilder sb, final String leftPadding, final boolean isEnabled, final HtmlBody searchTarget, final String searchFor) {
		highlight(sb, leftPadding, isEnabled?"black":"gray","yellow", searchTarget, searchFor);
	}
	
  	private static void highlight(final StringBuilder sb, final String leftPadding, final String bgcolor, final String color, final HtmlBody hb, final String searchFor) {
  		final String txt = hb.getHtmlEncoded();
		final String ltxt = txt.toLowerCase();
		final String pattern=encodeXmlOrHtml(searchFor.toLowerCase());
		final int idx = ltxt.indexOf(pattern);
		if (idx >= 0) {
			final int endOfPattern=idx + pattern.length();
			final int end=txt.length();
			if (leftPadding!=null) {
				sb.append(leftPadding);
			}
			sb.append(txt.substring(0, idx));  
			sb.append("<b><font bgcolor='");
			sb.append(bgcolor);
			sb.append("' color='");
			sb.append( color );
			sb.append("'>");
			
			sb.append(txt.substring(idx, endOfPattern));
			sb.append( "</font></b>");
			sb.append(txt.substring(endOfPattern, end>=endOfPattern?end:txt.length()));
			
			} else {
			sb.append(txt);
			}
	}
  	
  	/**
  	 * Sigh... duplicate of the one in protege.jar but needed for modules that don't ship/distribute with protege
  	 * 
  	 * @param priorCount
  	 * @param proposedCount
  	 * @param min
  	 * @param max
  	 * @param allowAddingIfMaxIsOne
  	 * @return
  	 */
  	public static String getCardinalityAnomalyStatement(final int priorCount, final int proposedCount, final int min, final int max,
			final boolean allowAddingIfMaxIsOne) {
		if (proposedCount < min) {
			return "You need at least<br>" + min + " of these items.";
		} else if (proposedCount > max && (!allowAddingIfMaxIsOne || max > 1)) {
			if (proposedCount >= priorCount) {
				return "You can <b>not</b> have <i>more</i> than<br>" + max + " of these items.";
			}
		}
		return null;
	}

  	public static String append(final String s, int max, final char c ) {
  		int len=s.length();
  		int todo=max-len;
  		final StringBuilder sb=new StringBuilder(s);
  		for (int i=0;i<todo;i++) {
  			sb.append(c);
  		}
  		return sb.toString();
  	}
  	
  	public static String append(final String s, int max, final String c ) {
  		final StringBuilder sb=new StringBuilder(s);
  		for (int i=0;i<max;i++) {
  			sb.append(c);
  		}
  		return sb.toString();
  	}
  	
  	public static String prepend(final String s, int max, final char c ) {
  		int len=s.length();
  		int todo=max-len;
  		final StringBuilder sb=new StringBuilder();
  		for (int i=0;i<todo;i++) {  			
  			sb.append(c);
  		}
  		sb.append(s);
  		return sb.toString();
  	}

  	public static String prepend(final String s, int max, final String c ) {
  		int len=s.length();
  		int todo=max-len;
  		final StringBuilder sb=new StringBuilder();
  		for (int i=0;i<todo;i++) {  			
  			sb.append(c);
  		}
  		sb.append(s);
  		return sb.toString();
  	}

  	
  	

  	
  	public static final String NULL="<null>";
  	
  	public static final String NOT_APPLICABLE="<html><font color='red' face='verdana'><small>N/A</small></font></html>";

  	public static Comparator<Object> htmlAndCaseInsensitive=new Comparator<Object>(){
	    public int compare(Object o1, Object o2){
	    	if ((o1 instanceof String) && (o2 instanceof String)){
	    		o1=stripSimpleHtml((String)o1);
	    		o2=stripSimpleHtml((String)o2);
	    		return ((String)o1).compareToIgnoreCase((String)o2);
	    	}
	    	else if (o2 instanceof Comparable){
	    		return 0-( (Comparable)o2).compareTo(o1);
	    	} else if (o1 != null && o2 != null){
	    		return ( (Comparable)o1).compareTo(o2);
	    	}
	    	return 0;
	    }

  };
  	
  	public static Comparator<Object> caseInsensitive=new Comparator<Object>(){
	    public int compare(final Object o1, final Object o2){
	    	if ((o1 instanceof String) && (o2 instanceof String)){
	    		return ((String)o1).compareToIgnoreCase((String)o2);
	    	}
	    	else if (o2 instanceof Comparable){
	    		return 0-( (Comparable)o2).compareTo(o1);
	    	} else if (o1 != null && o2 != null){
	    		return ( (Comparable)o1).compareTo(o2);
	    	}
	    	return 0;
	    }

  };

	public static Comparator<Object> caseAndNullInsensitive=new Comparator<Object>(){
	    public int compare(final Object o1, final Object o2){
	    	if ((o1 instanceof String) && (o2 instanceof String)){
	    		return ((String)o1).compareToIgnoreCase((String)o2);
	    	}
	    	else if (o2 instanceof Comparable){
	    		if (o1==null){
	    			return 1;
	    		}
	    		return 0-( (Comparable)o2).compareTo(o1);
	    	} else if (o1 != null){
	    		if (o2==null){
	    			return -1;
	    		}
	    		
	    		return ( (Comparable)o1).compareTo(o2);
	    	}
	    	return 0;
	    }

  };

  public static String trimIfTooBig(
		  final String s) {
	  return trimIfTooBig(s,",", 60, "<small>... etc.</small>");
  }
  
  public static String trimIfTooBig(
		  final String s,
		  final String delimiter, 
		  final int maxLength,
		  final String replaceWith) {
		if (s.length() > maxLength) {
			int p = 0, i = 0;
			for (i = s.indexOf(delimiter, p);i>0 && i < maxLength;i = s.indexOf(delimiter, p+1)) {
				p=i;
			}
			return s.substring(0, p > 0 ? p : maxLength) + replaceWith;
		}
		return s;
	}

  	public static class HtmlBody{
  		
  		public String getHtml() {
  			return body==null?null:prefix+body+suffix;
  		}
  		
  		public String getBodyOrOriginal() {
  			return body==null?original:body;
  		}

  		private String encodedOriginal;
  		
  		public boolean isHtml() {
  			return body!=null;
  		}
  		
  		/**
  		 * used when *FIRST* doing an autocomplete search for either a row or a node 
  		 * @param txt
  		 * @return
  		 */
  		public boolean contains(final String txt) {
  			if (body==null) {
  				return this.original.contains(txt);
  			}
  			return body.contains(encodeXmlOrHtml(txt));
  		}

  		public static boolean contains(final String searchTarget, final String searchArg) {
  			final HtmlBody hb=new HtmlBody(searchTarget);
  			return hb.contains(searchArg);
  		}

  		/**
  		 * used when *FIRST* doing an autocomplete search for either a row or a node 
  		 * @param txt
  		 * @return
  		 */
  		public boolean startsWith(final String txt) {
  			if (body==null) {
  				return this.original.startsWith(txt);
  			}
  			return body.startsWith(encodeXmlOrHtml(txt));
  		}

  		public static boolean startsWith(final String searchTarget, final String searchArg) {
  			final HtmlBody hb=new HtmlBody(searchTarget);
  			return hb.startsWith(searchArg);
  		}
  		


  		public static String getHtmlEncoded(final String input) {
  			final HtmlBody hb=new HtmlBody(input);
  			return hb.getHtmlEncoded();
  		}
  		public String getHtmlEncoded() {
  			if (body==null) {
  				if (encodedOriginal==null) {
  					encodedOriginal=encodeXmlOrHtml(original);
  				}
  				return encodedOriginal;
  			}
  			return body;
  		}

  		String body=null, original;
  		public String prefix="",suffix="";
	    
        public HtmlBody(final String input) {
        	this(input, false);
        }
        public HtmlBody(final String input, final boolean isHtmlWithoutHtmlOrBodyTag) {
        	original=input;
	        if (!Basics.isEmpty(input)) {
	        
	            String v = snip(input, "<body>", "</body>");
	            if (v == null) {
	                v = snip(input, "<html>", "</html>");
	            }
	            if (v!=null ){
	            	while (v.startsWith("&nbsp;")) {
						v = v.substring("&nbsp;".length());
						
					}
	                while (v.startsWith("<")) {
						final int idx2 = v.indexOf(">");
						if (idx2 >= 0) {
							prefix = prefix + v.substring(0, idx2 + 1);
							v = v.substring(idx2 + 1);
                	} else {
                		break;
                	}
                	}
                	while (v.endsWith(">")) {
                    	final int idx2=v.lastIndexOf("<");
                    	if (idx2>=0) {
                    		suffix=v.substring(idx2)+suffix;
                    		v=v.substring(0, idx2);
                    	} else {
                    		break;
                    	}
                    	}
                    body = v;
                } else if (isHtmlWithoutHtmlOrBodyTag) {
                	body=v;
                }
	        }
	    }
  	}

  	
  
  	 /**
  	  * By reusing a pre-allocated string builder *hopefully* that 
  	  * is reserved *EXCLUSIVELY* for use on the dispatch thread ...
  	  * ... well hopefully this might slow down heap memory growth/expansion and the cost 
  	  * of garbage collection more than is the case with excessive use of 
  	  * built in String concating via the plus operator with "this" + " and " + "that".
  	  * 
  	  */
  	
  	public static String NOTE="<u><font color='blue'>Note:</font></u>&nbsp;&nbsp;";
  	public static String NOTE_BOLDLY="<b>"+NOTE+"</b>";
  	public static String concatHtmlUncentered( final String arg1, final String arg2, final String ... args){
  		return toHtmlUncentered(concat(arg1, arg2, args));
  	}
  	
  	public static String concatHtmlCentered( final String arg1, final String arg2, final String ... args){
  		return toHtmlCentered(concat(arg1, arg2, args));
  	}
  	
  	/**
  	 * This attempts to improve on the compiler's built in string concatenating by reducing the cost
  	 * of memory reallocation with one static StringBuilder instance.  Must be called on the SWING dispatch thread 
  	 * @param args
  	 * @return
  	 */
  	public static String concat( final String arg1, final String arg2, final String ... args){  		
  		final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
  		final StringBuilder sb=tssb.lock();
  		sb.append(arg1);
  		sb.append(arg2);
  		for (final String arg:args){
  			sb.append(arg);
  		}
  		return tssb.unlockString();
  	}

  	public static String concatObjects( final Object arg1, final Object arg2, final Object ... args){
  		final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
  		final StringBuilder sb=tssb.lock();
  		sb.append(arg1);
  		sb.append(arg2);
  		for (final Object arg:args){
  			sb.append(arg);
  		}
  		return tssb.unlockString();
  	}
  	
  	public static void clearStringBuildingMemory(){
  		ThreadSafeStringBuilder.clear();  		 
  	}
  	
  	public static boolean isSameLineage(final Object [] a1, final Object []a2){
		final Object[]shorterOfTheTwo=a1.length>a2.length?a2:a1;
		final int comparisonLength= a1.length==a2.length?a1.length-1:shorterOfTheTwo.length;
		for (int i=0;i<comparisonLength;i++){
			if (!Basics.equals(a1[i], a2[i])){
				return false;
			}
		}
		return true;
	}

  	public static <K,V> void copy(Map<K,V>dst, Map<K,V>src){
  		for (final K key:src.keySet()){
  			final V value = src.get(key);
  			dst.put(key, value);
  		}
  	}
  	public static String delimitWithXmlNodes(final String text, final String... xmlNodes){
  		final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
  		final StringBuilder sb=tssb.lock();
  		final String txt;
  		if (text.startsWith("<html>")){
        	final int idx=text.lastIndexOf("</html>");
        	txt=text.substring("<html>".length(), idx);       	
  		} else {
  			txt=text;
  		}
  		for (final String xmlNode:xmlNodes){
  			sb.append('<');
  			sb.append(xmlNode);
  			sb.append('>');
  		}
  		sb.append(txt);
  		for (int i=xmlNodes.length-1;i>=0;i--){
  			sb.append("</");
  			sb.append(xmlNodes[i]);
  			sb.append('>');
  		}
  		return tssb.unlockString();
  	}
  	
 	public static void println(final Object... objs){
  		for (final Object o:objs){
  			System.out.print(o);
  		}
  		System.out.println();
  	}
 	 public static String endHtml2() {
         return HTML_BASIC_END;
     }
 	  public static String startHtml2() {
 	        return HTML_BASIC_START_CENTERED;
 	    }
 	  
 	  public static <K,V> Map<V,K> flipKeyToValue(Map<K,V>in, Map<V,K>out){
 		  for (final K key : in.keySet()){
 			  out.put(in.get(key), key);
 		  }
 		  return out;
 	  }
 	  
 	  public static String anglacizePropertyName(final String propertyName){
 		  final StringBuilder sb=new StringBuilder();
 		  final char []a=propertyName.toCharArray();
 		  for (int i=0;i<a.length;i++){
 			  if (a[i]=='.'){
 				  sb.append(' ');
 			  } else if (Character.isUpperCase(a[i])){
 				  if (i>0){
 					  sb.append(' ');
 				  }
 				  sb.append(Character.toLowerCase(a[i]));
 			  } else {
 				  sb.append(a[i]);
 			  }
 		  }
 		  return sb.toString();
 	  }

	public static int nextMultipleOf10(final int n) {
		final double m2 = (int) Math.log10(n);
		return (int) Math.pow(10, Math.ceil(m2 + 1.0));
	}

	public static String leadingZeros(final int n, final int i) {
		int m = Basics.nextMultipleOf10(n);
		if (m < 100) {
			m = 100;
		}
		String s2 = Integer.toString(m + i).substring(1);
		return s2;

	}
}

