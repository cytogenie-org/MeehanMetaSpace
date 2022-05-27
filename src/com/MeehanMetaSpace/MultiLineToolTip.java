package com.MeehanMetaSpace;

import java.awt.*;
import javax.swing.*;
import org.apache.commons.lang.*;
import java.util.Arrays;

public class MultiLineToolTip {

  private static final String HTML_START =
	  "<HTML>";
  private static final String HTML_END = "</HTML>";
  private static final String SPACE = " ",
	  OPEN_P = "<p>",
	  CLOSE_P = "</p>",
	  HR = "<hr>",
	  HEADER = "</h",
	  BR = "<br>";

  // RegExp to pick out all the HTML tags from the String - this is not failsafe
  // It simply removes everything between an open and close bracket so will also remove
  // z < x && x > y  --> z y  . . . . . but I don't think this is an issue much in tooltips!
  private static final String TAG_REGEX = "\\<.*?\\>";

  static private String addStartEndTags(String text) {
	if (text.toUpperCase().indexOf(HTML_START) == -1) {
	  text = HTML_START + text;
	}
	if (text.toUpperCase().indexOf(HTML_END) == -1) {
	  text += HTML_END;
	}
	return text;
  }

  public static String multiLineTT(final String text, final int cols) {
	return multiLineTT(text,cols,true);
  }

  public static String multiLineTT(
		final String text,
		final int cols,
		final boolean addHtmlTags) {
	String wrappedText = wrapHtml(text, cols, "<BR>", false);
	return addHtmlTags ? addStartEndTags(wrappedText) : wrappedText;
  }

  public static String multiLineTT(
	  final String text,
	  final int cols,
	  final String linebreak,
	  final boolean wrapLongWords ) {
	final String wrappedText = wrapHtml(text, cols, linebreak, wrapLongWords );
	return addStartEndTags(wrappedText);
  }

  /**
   * Adaped from the Jakarta commons WordUtils.wrap( String, int, String, boolean )
   * by NHZ for wrapping text with embedded HTML tags
   *
   * <p>Wraps a single line of text, identifying words by <code>' '</code>.</p>
   *
   * <p>Leading spaces on a new line are stripped.
   * Trailing spaces are not stripped.</p>
   *
   * <pre>
   * WordUtils.wrap(null, *, *, *) = null
   * WordUtils.wrap("", *, *, *) = ""
   * </pre>
   *
   * @param str  the String to be word wrapped, may be null
   * @param wrapLength  the column to wrap the words at, less than 1 is treated as 1
   * @param newLineStr  the string to insert for a new line,
   *  <code>null</code> uses the system property line separator
   * @param wrapLongWords  true if long words (such as URLs) should be wrapped
   * @return a line with newlines inserted, <code>null</code> if null input
   */
  private static String wrapHtml(String str, int wrapLength, String newLineStr,
								 boolean wrapLongWords) {
	if (str == null) {
	  return null;
	}
	if (newLineStr == null) {
	  newLineStr = SystemUtils.LINE_SEPARATOR;
	}
	if (wrapLength < 1) {
	  wrapLength = 1;
	}
	int inputLineLength = str.length();
	int offset = 0;
	int index = -1;
	int positionInString = 0;
	boolean finishedWrapping = false;
	StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);
	// Split up the html tags, if any
	String[] contents = str.split(TAG_REGEX);
	String tail;

	while ( (inputLineLength - offset) > wrapLength && !finishedWrapping ) {
	  if (str.charAt(offset) == ' ') {
		offset++;
		continue;
	  }

	  index = getFirstNonEmptyString(contents, index + 1);
	  if (index >= 0) {
		positionInString = str.indexOf(contents[index]);
	  }
	  else {
		// No more tags except the closing tags
		positionInString = offset;
		// Cut off the ending tags, if they exist
		String[] ending = str.substring(positionInString).split(TAG_REGEX);
		// Check if we can just add the rest of the tail without breaks
		if (ending[0] != null && ending[0].length() <= wrapLength)
		  finishedWrapping = true;
	  }
	  tail = str.substring(positionInString);

	  if (!finishedWrapping) {
		// Check if there is an HTML line break
		int htmlBreakingSpace = lastHtmlLineBreak(tail, wrapLength);
		// if there is an html line break, we should respect it and break here, otherwise look for a space
		int spaceToWrapAt = htmlBreakingSpace == -1 ?
			tail.lastIndexOf(' ', wrapLength) : htmlBreakingSpace;

		spaceToWrapAt = spaceToWrapAt + positionInString;

		if (spaceToWrapAt >= offset) {
		  // normal case
		  wrappedLine.append(str.substring(offset, spaceToWrapAt));
		  if (htmlBreakingSpace == -1) {
			wrappedLine.append(newLineStr);
		  }
		  offset = spaceToWrapAt;
		}
		else {
		  // really long word or URL
		  if (wrapLongWords) {
			// wrap really long word one line at a time
			wrappedLine.append(str.substring(offset, wrapLength + offset));
			wrappedLine.append(newLineStr);
			offset += wrapLength;
		  }
		  else {
			// do not wrap really long word, just extend beyond limit
			spaceToWrapAt = tail.indexOf(' ', wrapLength + offset);
			if (spaceToWrapAt >= 0) {
			  wrappedLine.append(str.substring(offset, spaceToWrapAt));
			  wrappedLine.append(newLineStr);
			  offset = spaceToWrapAt;
			}
			else {
			  wrappedLine.append(str.substring(offset));
			  offset = inputLineLength;
			}
		  }
		}
	  }
	}
	// Whatever is left in line is short enough to just pass through
	wrappedLine.append(str.substring(offset));
	return wrappedLine.toString();
  }

  private static int lastHtmlLineBreak(String in, int fromIndex) {
	int[] breaks = new int[5];
	if (in != null && (fromIndex >= 0 && fromIndex < in.length())) {
	  in = in.toLowerCase();
	  int pos = in.lastIndexOf(BR, fromIndex);
	  breaks[0] = pos == -1 ? -1 : pos + BR.length();
	  pos = in.lastIndexOf(HEADER, fromIndex);
	  breaks[1] = pos == -1 ? -1 : pos + HEADER.length() + 2; // for the 2>
	  pos = in.lastIndexOf(OPEN_P, fromIndex);
	  breaks[2] = pos == -1 ? -1 : pos + OPEN_P.length();
	  pos = in.lastIndexOf(CLOSE_P, fromIndex);
	  breaks[3] = pos == -1 ? -1 : pos + CLOSE_P.length();
	  pos = in.lastIndexOf(HR, fromIndex);
	  breaks[4] = pos == -1 ? -1 : pos + HR.length();
	  Arrays.sort(breaks);
	  // Find the closest breaking point (but not -1!)
	  for (int i = 0; i < breaks.length; i++) {
		if (breaks[i] != -1) {
		  return breaks[i];
		}
	  }
	}
	return -1;
  }

  private static int getFirstNonEmptyString(String[] array, int startIndex) {
	if (array != null && (startIndex >= 0 && startIndex <= array.length)) {
	  for (int i = startIndex; i < array.length; i++) {
		if (array[i] != null && !array[i].equals("")) {
		  return i;
		}
	  }
	}
	return -2;
  }

  public static void main(String[] args) throws Exception {
	final String myToolTip = "Lights go down it's dark The jungle is your head - can't rule your heart I'm feeling so much stronger than before";
	String htmlToolTip = "<H1>Four score and seven years ago</H2> our forefathers founded this great nation and hello to the world my name is noah ";

	JFrame myFrame = new JFrame("Hello World");
	myFrame.getContentPane().setLayout(new BorderLayout(2, 2));
	myFrame.setSize(new Dimension(200, 100));
	JPanel p = new JPanel(new GridLayout(2, 2));
	JButton myButton = new JButton("Multi-line tool tip");
	JButton otherButton = new JButton("Vanilla tool tip");
	myButton.setToolTipText( multiLineTT( myToolTip, 35 ) );
	otherButton.setToolTipText( myToolTip );

	p.add(myButton);
	p.add(otherButton);
	myFrame.getContentPane().add(p, BorderLayout.CENTER);
	myFrame.setDefaultCloseOperation(myFrame.EXIT_ON_CLOSE);
	myFrame.pack();
	myFrame.setVisible(true);
  }
}
