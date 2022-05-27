/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 Copyright (c) John D. Mitchell, 1997 -- All Rights Reserved

 MODULE:		Tips & Tricks
 FILE:		Redirect.java

 AUTHOR:		Stephen Meehan

 DESCRIPTION:
 This class is a Program Event Log that tracks all program events including
 exceptions printed to standard out/error by the JAVA system library and
 any other modules unaware of Pel.


 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

package com.MeehanMetaSpace;

import java.io.*;
import java.util.*;
import java.text.*;

public final class Pel {

  public static final PrintStream defaultOut = System.out,
      defaultErr = System.err;
  public final PrintStream out, err;
  public final File file;

  public synchronized static void note(Throwable e) {
    if (log == null) {
      System.err.println(e.getMessage());
    }
    else {
      log.warn(e);
    }
  }
public static void println(final Throwable e){
  if (log == null){
    e.printStackTrace(System.err);
  } else {
    log.print(e);
  }
}
  public static Pel log = null;
  public synchronized static void init(final String pathLocation,
				       final Class cls,
				       final String appFullName,
				       final boolean announce) {
    if (log == null) {
      log = new Pel(pathLocation, cls, appFullName, false, announce);
    }
  }

  private FileOutputStream fos;
  private final EchoOutputStream echoErr, echoOut;
  private Pel(
      final String pathLocation,
      final Class mainClass,
      final String productInfo,
      final boolean redirectOut,
      final boolean announce) {
    PrintStream o = null, e = null;
    EchoOutputStream ee = null, eo = null;
    File pelFile = null;

    try {
      String location;
      if (Basics.isEmpty(pathLocation)) {
	location = System.getProperty("user.home") + File.separator + "pel" +
	    File.separator;
      }
      else if (!pathLocation.endsWith(File.separator)) {
	location = pathLocation + File.separator;
      }
      else {
	location = pathLocation;
      }
      IoBasics.mkDirs(location);
      pelFile = new File(location + "pel.log");
      fos = new FileOutputStream(pelFile, true);
      ee = new EchoOutputStream(new OutputStream[] {fos, defaultErr});
      e = new PrintStream(ee);
      eo = new EchoOutputStream(new OutputStream[] {fos, defaultOut});
      o = new PrintStream(eo);
    }
    catch (IOException err) {
      // Sigh.  Couldn't start peling
      System.out.println("Unable to append to file " + pelFile.getAbsolutePath());
      err.printStackTrace(System.err);
    }
    finally {
      this.file = pelFile;
      err = e == null ? defaultErr : e;
      out = o == null ? defaultOut : o;
      echoErr = ee;
      echoOut = eo;
      if (err != defaultErr) {
	System.setErr(err);
      }
      if (out != defaultOut && redirectOut) {
	System.setOut(out);
      }
      mainClassName = " (main=" + mainClass.getName() + ")";

      if (announce) {
	println(productInfo+"\nLogging program events to "+(e == null ? "standard out/err!" :
			   file.getAbsolutePath()));

      }
    }
  }

  public synchronized void close() {
    try {
      if (echoOut != null) {
	echoOut.setStreams(new OutputStream[] {defaultOut});
      }
      if (echoErr != null) {
	echoErr.setStreams(new OutputStream[] {defaultErr});
      }
      fos.close();

    }
    catch (IOException ioe) {
      ioe.printStackTrace(defaultErr);
    }
  }

  public synchronized void reopen() {
    try {
      fos = new FileOutputStream(file.getAbsolutePath(), true);
      if (echoOut != null) {
	echoOut.setStreams(new OutputStream[] {fos, defaultOut});
      }
      if (echoErr != null) {
	echoErr.setStreams(new OutputStream[] {fos, defaultErr});
      }
    }
    catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace(defaultErr);
    }
  }

  private final String mainClassName;
  private final static String pattern = "yyyy.MM.dd H:mm:ss:SSS";
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);

  private String tag(Condition condition) {
    return condition.toString() + ":  " + dateFormatter.format(new Date()) +
	mainClassName;
  }

  public synchronized void print(final String output) {
    print(Condition.NORMAL, output);
  }

  public synchronized void print(Condition condition, final String output) {
    final PrintStream printStream = condition.compareTo(Condition.WARNING) > 0 ?
	err : out;
    printStream.println();
    printStream.print(tag(condition));
    printStream.println();
    printStream.println(output);

  }

  public synchronized void println(final String output) {
    print(output);
    out.println();
  }

  public synchronized void printErr(final String output) {
    print(Condition.ERROR, output);
  }

  public synchronized void printlnErr(final String output) {
    printErr(output);
    err.println();
  }

  public synchronized void print(Throwable exception, boolean stackTrace) {
    final Condition condition =
	exception instanceof RuntimeException ?
	Condition.FATAL : (stackTrace ? Condition.ERROR : Condition.WARNING);

    if (stackTrace) {
      err.println();
      exception.printStackTrace(err);
      err.println();
    }
    else {
      print(condition, exception.getMessage());
      if (condition.isWorseThan(Condition.WARNING)) {
	err.println();
      }
      else {
	out.println();
      }
    }
  }

  public void print(final Throwable exception) {
    print(exception, true);
  }

  public void warn(Throwable exception) {
    print(exception, false);
  }

  public static void main(String args[]) {
    init(null, Pel.class, "test", true);
    System.out.println("Test exception without stack trace");
    try {
      FileInputStream fin = new FileInputStream(":://dog");
    }
    catch (IOException ioe) {
      System.out.println("Now with stack trace...");
      try {
	new FileInputStream(":://dog again");
      }
      catch (IOException ioe2) {
	Pel.log.print(ioe2);
      }

    }
    System.err.println(
	"Now PEL will capture a system emitted NullPointerException");
    File f = null;
    f.getAbsolutePath();
    System.exit(0);
  }
}
