package com.MeehanMetaSpace;

import java.io.*;
import java.util.*;


/**
 * <p>Title: FolderMerger</p>
 *
 * <p>Description: Selective Folder Merging Utility to support version roll forward.</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: ScienceXperts Inc.</p>
 *
 * @author Ethan Stone
 * @version beta 3
 *
 * Oct. 18, 2005 : Submitted to CVS
 *
 */
public class FolderMerger{

  public final static String
      ARG_SOURCE = "source",
      ARG_DEST = "dest",
      ARG_FILES_TO_AVOID_COPYING = "filesToAvoidCopying",
      ARG_FILES_TO_AVOID_OVERWRITING = "filesToAvoidOverwriting",
      ARG_DIRS_TO_AVOID_COPYING = "dirsToAvoidCopying",
      ARG_DIRS_TO_AVOID_OVERWRITING = "dirsToAvoidOverwriting",
      ARG_HELP = "help";

  private final String source;
  private final String dest;
  private final String[] filesToAvoidCopying;
  private final String[] filesToAvoidOverwriting;
  private final String[] dirsToAvoidCopying;
  private final String[] dirsToAvoidOverwriting;

  private final ArrayList noMergeFiles;
  private final ArrayList noMergeDirs;


  public static void main(final String[] args) {
    main(new Args(args));
  }

  public static void main(final Args args) {

    if (args.get(ARG_HELP) != null) {
     printUsage();
     System.exit(0);
   }

    final String delimiters = " ";
    final FolderMerger fm =
        new FolderMerger(args.get(ARG_SOURCE),
                         args.get(ARG_DEST),
                         getAndSplitArg(args, ARG_FILES_TO_AVOID_COPYING, delimiters),
                         getAndSplitArg(args, ARG_DIRS_TO_AVOID_COPYING, delimiters),
                         getAndSplitArg(args, ARG_FILES_TO_AVOID_OVERWRITING, delimiters),
                         getAndSplitArg(args, ARG_DIRS_TO_AVOID_OVERWRITING, delimiters));

    fm.printParameters();
    fm.merge(true);
  }
  public static void go (
      final String source,
      final String dest,
      final String[] filesToAvoidCopying,
      final String[] dirsToAvoidCopying,
      final String[] filesToAvoidOverwriting,
      final String[] dirsToAvoidOverwriting) {
    final FolderMerger fm=new FolderMerger(
      source,
      dest,
      filesToAvoidCopying,
      dirsToAvoidCopying,
      filesToAvoidOverwriting,
      dirsToAvoidOverwriting);
    fm.merge(true);
  }

  public FolderMerger(final String source,
                      final String dest,
                      final String[] filesToAvoidCopying,
                      final String[] dirsToAvoidCopying,
                      final String[] filesToAvoidOverwriting,
                      final String[] dirsToAvoidOverwriting) {
    if (Basics.isEmpty(source) || Basics.isEmpty(dest)) {
      throw new IllegalArgumentException("Values must be provided for both source and dest fields.");
    }
    this.source = source;
    this.dest = dest;
    this.filesToAvoidCopying = filesToAvoidCopying == null ? new String[0] : filesToAvoidCopying;
    this.filesToAvoidOverwriting = filesToAvoidOverwriting == null ? new String[0] : filesToAvoidOverwriting;
    this.dirsToAvoidCopying = dirsToAvoidCopying == null ? new String[0] : dirsToAvoidCopying;
    this.dirsToAvoidOverwriting = dirsToAvoidOverwriting == null ? new String[0] : dirsToAvoidOverwriting;

    noMergeDirs = getNoMergeDirs();
    noMergeFiles = getNoMergeFiles();
  }

  public String getSource() {
    return source;
  }
  public String getDest() {
    return dest;
  }
  public String[] getFilesToAvoidCopying() {
    return filesToAvoidCopying;
  }
  public String[] getFilesToAvoidOverwriting() {
    return filesToAvoidOverwriting;
  }
  public String[] getDirsToAvoidCopying() {
    return dirsToAvoidCopying;
  }
  public String[] getDirsToAvoidOverwriting() {
    return dirsToAvoidOverwriting;
  }

  public void merge() {
    merge(false);
  }

  public void merge(final boolean recurse) {
    doMerge(recurse);
  }

  private void doMerge(final boolean recurse) {
    doMerge("", "", recurse);
  }

  private void doMerge(final String relSrc, final String relDest, final boolean recurse) {

    final File src = Basics.isEmpty(relSrc) ? new File(this.source) : new File(this.source, relSrc);
    final File dest = Basics.isEmpty(relDest) ? new File(this.dest) : new File(this.dest, relDest);

    // recurse through subdirectories
    if (src.isDirectory()) {
      File[] children = src.listFiles();
      for (int i = 0; i < children.length; i++) {
        File child = children[i];
        if (!child.isDirectory() || (recurse && avoidNoMergeDirs(child))) {
          doMerge(IoBasics.concat(relSrc, child.getName()), IoBasics.concat(relDest, child.getName()), recurse);
        }
      }
    }
    else {
      // if it's a file, and it passes the filters, copy it to destination
      if (avoidNoMergeFiles(src)){
        //System.out.println("Copying " + src + " to " + dest);
        try{
          IoBasics.mkDirs(dest.getParent());
          IoBasics.copy(src, dest, null);
          System.out.println("Copying " + src + " to " + dest);
        }
        catch (IOException ioe){
          System.out.println("Couldn't copy " + source + " to " + dest);
        }
      }
    }
  }

  private static void printUsage() {
    Args args=new Args(new String[]{});
    System.out.println(args.getUsage(
        new Args.Expect[]{
        new Args.Expect(ARG_SOURCE, null,
                        "This flag should be followed by the source directory"),
        new Args.Expect(ARG_DEST, null,
                        "This flag should be followed by the destination directory."),
        new Args.Expect(ARG_FILES_TO_AVOID_COPYING, null,
                        "Optional: This flag should be followed by a \n\t\tlist of files to avoid copying separated by spaces."),
        new Args.Expect(ARG_FILES_TO_AVOID_OVERWRITING, null,
                        "Optional: This flag should be followed by a \n\t\tlist of files to avoid overwriting separated by spaces."),
        new Args.Expect(ARG_DIRS_TO_AVOID_COPYING, null,
                        "Optional: This flag should be followed by a \n\t\tlist of directories to avoid copying separated by spaces."),
        new Args.Expect(ARG_DIRS_TO_AVOID_OVERWRITING, null,
                        "Optional: This flag should be followed by a \n\t\tlist of directories to avoid overwriting separated by spaces."),
        new Args.Expect(ARG_HELP, null,
                        "This flag indicates that this help menu should be displayed.") }
    ));
  }

  private static String[] getAndSplitArg(final Args args, final String argKey, final String delimiters) {
    final String arg = args.get(argKey);
    final String[] argArr;
    if (!Basics.isEmpty(arg)) {
      argArr = args.split(arg, delimiters);
    }
    else {
      argArr = new String[0];
    }

    return argArr;
  }

  // EPS,
  // Should only be called once by the constructor since noMergeDirs is final.
  //
  private ArrayList getNoMergeDirs() {
    ArrayList al = new ArrayList();
    for (int i = 0; i < dirsToAvoidCopying.length; i++) {
      al.add(dirsToAvoidCopying[i]);
    }
    for (int i = 0; i < dirsToAvoidOverwriting.length; i++) {
      al.add(dirsToAvoidOverwriting[i]);
    }

    return al;
  }

  // EPS,
  // Should only be called once by the constructor since noMergeDirs is final.
  //
  private ArrayList getNoMergeFiles() {
    ArrayList al = new ArrayList();
    for (int i = 0; i < filesToAvoidCopying.length; i++) {
      al.add(filesToAvoidCopying[i]);
    }
    for (int i = 0; i < filesToAvoidOverwriting.length; i++) {
      al.add(filesToAvoidOverwriting[i]);
    }

    return al;

  }

  private boolean avoidNoMergeDirs(final File directory) {
   WildCardFilter wcf = new WildCardFilter();
   final Iterator iter = noMergeDirs.iterator();
   while (iter.hasNext()) {
     wcf.setPattern((String)iter.next());
     if (wcf.avoid(directory.getParentFile(), directory.getName())) {
       System.out.println("Avoiding " + directory);
       return false;
     }
   }
   return true;
 }

 private boolean avoidNoMergeFiles(final File file){
   WildCardFilter wcf = new WildCardFilter();
   final Iterator iter = noMergeFiles.iterator();
   while (iter.hasNext()) {
     wcf.setPattern((String)iter.next());
     if (wcf.avoid(file.getParentFile(), file.getName())) {
       System.out.println("Avoiding " + file);
       return false;
     }
   }
   return true;
 }


  private void printParameters() {
    System.out.println("");
    System.out.println("source: " + source);
    System.out.println("dest: " + dest);
    System.out.println("files to avoid copying: ");
    listArrayElements(filesToAvoidCopying);
    System.out.println("dirs to avoid copying: ");
    listArrayElements(dirsToAvoidCopying);
    System.out.println("files to avoid overwriting: ");
    listArrayElements(filesToAvoidOverwriting);
    System.out.println("dirs to avoid overwriting: ");
    listArrayElements(dirsToAvoidOverwriting);
    System.out.println("");
}

private void listArrayElements(String[] arr) {
  for (int i = 0; i < arr.length; i++) {
    System.out.println("\t" + (i+1) + ": " + arr[i]);
  }
}



}
