package com.MeehanMetaSpace;

import java.io.*;

/**
 * <p>Title: FacsXpert client</p>
 *
 * <p>Description: Workflow planner for FACS research</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: ScienceXperts Inc.</p>
 *
 * @author not attributable
 * @version beta 3
 */
public class WildCardFilter implements FilenameFilter {

    private String pattern;
    private boolean checkPath = false; //	true if pattern contains directory (e.g. c:\test\sample*.*)
    private boolean matchCase;


    public WildCardFilter() {
        this(null, !Basics.isEvilEmpireOperatingSystem()); // null filter accepts everything
    }

    public WildCardFilter(String pattern) {
        this(pattern, !Basics.isEvilEmpireOperatingSystem());
    }

    public WildCardFilter(String pattern, boolean matchCase) {
        setPattern(pattern);
        this.matchCase = matchCase;
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     * @param name the name of the file.
     * @return <code>true</code> if and only if the name should be included in
     *   the file list; <code>false</code> otherwise.
     * @todo Implement this java.io.FilenameFilter method
     */
    public boolean accept(File dir, String name) {
        if (pattern == null || pattern.length() == 0)
            return true;

        if (checkPath && dir != null)
            name = new File(dir, name).getAbsolutePath();

        name = name.replace('\\', '/');

        if (matchCase) {
            System.out.println("Attempting to match " + name + " and " + pattern);
            return name.matches(pattern);
        } else {
            System.out.println("Attempting to match " + name.toLowerCase() + " and " +
                               pattern.toLowerCase());
            // do the match as lowercase
            return name.toLowerCase().matches(pattern.toLowerCase());
        }
    }

    public boolean avoid(File dir, String name) {
        if (pattern == null || pattern.length() == 0)
            return false;

        if (checkPath && dir != null)
            name = new File(dir, name).getAbsolutePath();

        name = name.replace('\\', '/');

        if (matchCase) {
            System.out.println("Attempting to match " + name + " and " + pattern);
            return name.matches(pattern);
        } else {
            System.out.println("Attempting to match " + name.toLowerCase() + " and " +
                               pattern.toLowerCase());
            // do the match as lowercase
            return name.toLowerCase().matches(pattern.toLowerCase());
        }
    }


    public void setPattern(String pattern) {
        this.pattern = wildcardToRegex(pattern); // convert CL wildcard to Java regex
        checkPath = pattern != null &&
                    (pattern.indexOf('/') > -1 || pattern.indexOf('\\') > -1);
        if (checkPath) {
            this.pattern.replace('\\', '/');
        }
    }

    public String getPattern() {
        return pattern;
    }

    public static void main(String[] args) {
        String pattern = args[0];
        boolean matchCase = false;
        if (args.length > 1) {
            matchCase = args[1].equals("-c");
        }
        WildCardFilter wcf = new WildCardFilter(pattern, matchCase);
        File testFile = new File("c:\\somedir",
                                 "protocols_for_www.sciencexperts.com_facs_demomouse.jar");
        if (wcf.avoid(testFile.getParentFile(), testFile.getName())) {
            System.out.println(testFile.getAbsolutePath() + " matches the pattern " +
                               pattern);
        } else {
            System.out.println(testFile.getAbsolutePath() + " doesn't match the pattern " +
                               pattern);
        }
    }

    /**
     * Converts a windows/unix wildcard pattern to a regex pattern
     *
     * @param wildcard - Wildcard pattern containing * and ?
     *
     * @return - a regex pattern that is equivalent to the windows/unix wildcard pattern
     */
    private static String wildcardToRegex(String wildcard) {
        if (wildcard == null)
            return null;

        // make sure that orientation of file separator chars doesn't affect the matching
        wildcard = wildcard.replace('\\', '/');

        StringBuilder buffer = new StringBuilder();

        char[] chars = wildcard.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '*')
                buffer.append(".*");
            else if (chars[i] == '?')
                buffer.append(".");
            else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
                buffer.append('\\').append(chars[i]); // prefix all metacharacters with backslash
            else
                buffer.append(chars[i]);
        }

        return buffer.toString();

    }
}
