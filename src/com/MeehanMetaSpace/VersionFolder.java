package com.MeehanMetaSpace;

import java.util.regex.*;
import java.util.*;
import java.io.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public final class VersionFolder implements Comparable {

    private final static String DELIMITER = "_";

    // The /**/ comment cures the curse of module entropy where
    // both this module and this package became dependent on SWING
    /*
    private static boolean alertShown=false;
*/

    public static String encode(
      final String productName,
      final float major,
      final String releaseCycle,
      final int minor) {
        return productName +
          DELIMITER +
          major +
          DELIMITER +
          releaseCycle +
          ( minor < 0 ? "" : DELIMITER + minor );
    }

    public static VersionFolder upgradeDir(
      final String newVersionFolder,
      final String currentProductName,
      final String previousProductName,
      final String rootFolder,
      final Collection avoidPriorDirectories) {
        VersionFolder value = null;
        if (new File(rootFolder).exists()) {
            final File newVersionFile = new File(IoBasics.concat(rootFolder,
              newVersionFolder));
            boolean exists = newVersionFile.exists();
            if ((!exists || showUpgradeAlert) && avoidPriorDirectories != null) {
                final File f = new File(rootFolder);
                final File[] ff = f.listFiles();
                System.out.println("Checking " + rootFolder);
                if (ff != null) {
                    final TreeSet ts = new TreeSet();
                    String productName = null;
                    for (int i = 0; productName == null && i < ff.length; i++) {
                        final VersionFolder vd = decode(currentProductName, ff[i].getName());
                        if (vd != null) {
                            productName = currentProductName;
                        }
                    }
                    for (int i = 0; productName == null && i < ff.length; i++) {
                        final VersionFolder vd = decode(previousProductName, ff[i].getName());
                        if (vd != null) {
                            productName = previousProductName;
                        }
                    }
                    for (int i = 0; i < ff.length; i++) {
                        final VersionFolder vd = decode(productName, ff[i].getName());
                        if (vd != null) {
                            ts.add(vd);
                        }
                    }
                    if(showUpgradeAlert && ts.size() > 1){
	                   final VersionFolder[] vfs = (VersionFolder[]) ts.toArray(new
	                      VersionFolder[ts.size()]);
                    	value = vfs[vfs.length - 2];
	                    notifyStartMerge(value);
	                    final ArrayList al = new ArrayList();
	                    final String src = IoBasics.concat(rootFolder, value.dirName),
	                                       dst = newVersionFile.getAbsolutePath();
	                    for (final Iterator it = avoidPriorDirectories.iterator();
	                                             it.hasNext(); ) {
	                        al.add(IoBasics.concat(src, (String) it.next()));
	                    }
	                    FolderMerger.go(
	                      src,
	                      dst,
	                      null,
	                      (String[]) al.toArray(new String[al.size()]),
	                      null,
	                      null
	                      );
	                    notifyEndMerge();
                                        
                    }
                    else if (!exists && ts.size() > 0) {
                        final VersionFolder[] vfs = (VersionFolder[]) ts.toArray(new
                          VersionFolder[ts.size()]);
                          value = vfs[vfs.length - 1];
                        notifyStartMerge(value);
                        final ArrayList al = new ArrayList();
                        final String src = IoBasics.concat(rootFolder, value.dirName),
                                           dst = newVersionFile.getAbsolutePath();
                        for (final Iterator it = avoidPriorDirectories.iterator();
                                                 it.hasNext(); ) {
                            al.add(IoBasics.concat(src, (String) it.next()));
                        }
                        FolderMerger.go(
                          src,
                          dst,
                          null,
                          (String[]) al.toArray(new String[al.size()]),
                          null,
                          null
                          );
                        notifyEndMerge();
                    }
                }
            }
            showUpgradeAlert = false;
        }
        return value;
    }

    private static VersionFolder decode(
      final String productName, final String dirName) {
        String patternStr = productName + ".*";
        final Pattern p = Pattern.compile(patternStr);
        final Matcher m = p.matcher(dirName);
        if (m.matches()) {
            patternStr = DELIMITER;
            final String[] fields = dirName.split(patternStr, -1);
            if (fields.length >= 3) {
          	    String minorVersion = "0";
            	if(fields.length == 4)
            		minorVersion = fields[3];
                final int ii = Basics.indexOf(releaseCyclesInOrder, fields[2]);
                if (ii >= 0) {
                    final Float f1 = (Float) Basics.decode(fields[1], Float.class),
                                     f2 = (Float) Basics.decode(minorVersion, Float.class);
                    if (f2 != null && f1 != null) {
                        return new VersionFolder(dirName,
                                                 f1.floatValue(), fields[2],
                                                 f2.floatValue());
                    }
                }
            }
        }
        return null;
    }

    private static String[] releaseCyclesInOrder;
    public static void setReleaseCycles(final String[] releaseCyclesInOrder) {
        VersionFolder.releaseCyclesInOrder = releaseCyclesInOrder;
    }

    private static boolean showUpgradeAlert;
    public static void setShowUpgradeAlert(final boolean alertVal) {
        VersionFolder.showUpgradeAlert = alertVal;
    }
    
    private final String dirName;
    private final float majorVersion, minorVersion;
    private final String releaseCycle;

    private VersionFolder(
      final String dirName,
      final float majorVersion,
      final String releaseCycle,
      final float minorVersion) {
        this.dirName = dirName;
        this.majorVersion = majorVersion;
        this.releaseCycle = releaseCycle;
        this.minorVersion = minorVersion;
    }

    public String getDirName(){
        return dirName;
    }

    public float getMajorVersion(){
        return majorVersion;
    }

    public String getReleaseCycle(){
        return releaseCycle;
    }

    public float getMinorVersion(){
        return minorVersion;
    }

    public final boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VersionFolder))
            return false;
        final VersionFolder vf = (VersionFolder) o;
        return vf.majorVersion == majorVersion && vf.minorVersion == minorVersion &&
          vf.releaseCycle.equals(releaseCycle);
    }

    public final int hashCode() {
        int result = 17;
        result = 37 * result + (int) majorVersion;
        result = 37 * result + releaseCycle.hashCode();
        result = 37 * result + (int) minorVersion;
        return result;
    }

    public final int compareTo(final Object o) {
        if (o instanceof VersionFolder) {
            final VersionFolder vf = (VersionFolder) o;
            if (majorVersion == vf.majorVersion) {
                if (releaseCycle.equals(vf.releaseCycle)) {
                    if (minorVersion == vf.minorVersion) {
                        return 0;
                    } else {
                        if (minorVersion < vf.minorVersion) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                } else {
                    int l = Basics.indexOf(releaseCyclesInOrder, releaseCycle),
                            r = Basics.indexOf(releaseCyclesInOrder, vf.releaseCycle);
                    if (l < r) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            } else if (majorVersion < vf.majorVersion) {
                return -1;
            } else {
                return 1;
            }
        }
        return -1;
    }

// The /**/ comment cures the curse of module entropy:
// this module and this package must not depend on SWING
/*

    public static boolean isAlertShown() {
		return alertShown;
	}
*/
   public interface MergeListener {
		 void notifyStartMerge(VersionFolder priorFolder);
		 void notifyEndMerge();
	}

    private static Collection<MergeListener> mls=new ArrayList<MergeListener>();
    // Minor expansion to the exterior of VersionFolder by adding the JAVA Listener idiom .
    // This simplifies deeper complications that previously expanded the module
    //  for notifying users of a module expansion
    public static void add(final MergeListener ml){
        mls.add(ml);
    }

    /**
     * Prevent memory leaks
    */
    public static void dispose(){
        mls.clear();
    }

    private static void notifyStartMerge(final VersionFolder priorVersionFolder){
        for (final MergeListener ml:mls){
            ml.notifyStartMerge(priorVersionFolder);
        }

    }

    private static void notifyEndMerge(){
        for (final MergeListener ml : mls) {
            ml.notifyEndMerge();
        }
    }
}

