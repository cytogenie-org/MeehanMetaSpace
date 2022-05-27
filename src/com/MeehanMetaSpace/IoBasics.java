package com.MeehanMetaSpace;

import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import sun.security.action.GetPropertyAction;

public class IoBasics {

    // it is not that we really have a choice about the encoding - 
	// different platforms have different defaults 
	// (typical may be cp-1252 for windows, iso-8859 for linux server, utf-8 or MacRoman on Mac)
	// some of those single-byte encodings don't support special characters (like greek letters)
	// UTF-8 on the other hand is the most common, compact and compatible with ASCII encoding
	//
	static final String UTF8 = "UTF-8";

    public static boolean loadFilefromFileSystem(
      final String fileNameWithoutFolder,
      final String fileSystemFolder,
      final Class<? extends Object> jarClassIfNotFoundInFileSystem,
      final String jarFolderIfNotFoundInFileSystem) {

        final File f = new File(fileSystemFolder, fileNameWithoutFolder);
        String fileString = IoBasics.
                            readStringAndCloseWithoutThrowingUp(
                              jarClassIfNotFoundInFileSystem,
                              jarFolderIfNotFoundInFileSystem,
                              fileNameWithoutFolder);
        // create a file at the desired location

        // copy the contents
        try {
            // Old way: Depending on System.Default encoding
            // BufferedWriter outBuff = new BufferedWriter(new FileWriter(f));
            // New Way: Specifying our mandatory UTF-8 encoding
            BufferedWriter outBuff = new BufferedWriter(new OutputStreamWriter(new
              FileOutputStream(f), UTF8));
            try {
                outBuff.write(fileString);
                outBuff.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return true;

    }

    public static boolean ensureFileIsInFileSystem(

      final String fileNameWithoutFolder,
      final String fileSystemFolder,
      final Class<? extends Object> jarClassIfNotFoundInFileSystem,
      final String jarFolderIfNotFoundInFileSystem) {

        //final String s = concat(fileSystemFolder, fileNameWithoutFolder);
        final File f = new File(fileSystemFolder, fileNameWithoutFolder);
        boolean retValue = false;
        // if the file does not exisits load it
        if (!(f.exists())) {
            retValue = loadFilefromFileSystem(fileNameWithoutFolder, fileSystemFolder,
                                              jarClassIfNotFoundInFileSystem,
                                              jarFolderIfNotFoundInFileSystem);
        }
        return retValue;
    }

    public static SimpleDateFormat getFileModifiedFormat() {
        return new SimpleDateFormat(
          "yyyy/MM/dd   HH:mm:ss zzz");
    }


    public static void pause(final String pauseMessage) {
        try {
            System.out.flush();
            System.err.flush();
            System.out.println();
            System.out.println();
            System.out.print(pauseMessage == null ? "Press <Enter> to continue" :
                             pauseMessage);
            System.out.println();
            System.in.read();
            while (System.in.available() != 0) {
                System.in.read();
            }
        } catch (Exception e) {
        }
    }

    public static ProgressUpdater getSystemOut() {
        return new IoBasics.PrintStreamProgressUpdater(System.out);
    }

    public static class PrintStreamProgressUpdater implements ProgressUpdater {
        final PrintStream verbose;
        public boolean isCancelled() {
    		return false;
    	}
        public int getThresholdSize() {
            return CHUNK * 64;
        }

        public PrintStreamProgressUpdater(final PrintStream ps) {
            this.verbose = ps;
        }

        public void report(final Condition.Annotated a) {
            verbose.println(a);
        }

        public void report(final String description, final int currentAmount,
                           final int tallySoFar, final int finalAmount) {
            verbose.print(Basics.encode(tallySoFar));
            if (finalAmount >= 0) {
                verbose.print(" of ");
                verbose.print(finalAmount);
            }
            verbose.println();
        }
    }


    public static void copy(
      final OutputStream outputStream,
      final File file,
      final ProgressUpdater verbose) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            copy(outputStream, is, file.length(), verbose);
            is.close();
            is = null;
        } finally {
            closeWithoutThrowingUp(is);
        }
    }

    public final static int CHUNK = 1024 * 3;

    public static void copy(
      final OutputStream outputStream,
      final InputStream inputStream,
      final long finalAmountForDisplayPurposes,
      final ProgressUpdater pu) throws IOException {
        InputStream is = inputStream;
        OutputStream os = outputStream;
        if (os == null || is == null) {
            return;
        }
        int lastReported = 0, sumBytes, total = 0;
        final byte[] bt = new byte[CHUNK];
        do {
            sumBytes = 0;
            {
                int bytesRead = 0;

                while ((bytesRead = is.read(bt, sumBytes, CHUNK - sumBytes)) != -1 &&
                                    (sumBytes < CHUNK)) {
                    sumBytes += bytesRead;
                }
                if (bytesRead == -1 && sumBytes == 0) {
                    sumBytes = -1;
                }

            }
            if (sumBytes > -1) {
                os.write(bt, 0, sumBytes);
                os.flush();

                total += sumBytes;
                if (pu != null) {
                    lastReported += sumBytes;
                    if ((total % pu.getThresholdSize() == 0)) {
                        pu.report(null, pu.getThresholdSize(), total,
                                  (int) finalAmountForDisplayPurposes);
                        lastReported = 0;
                    }
                }
            }
        } while (sumBytes > -1 && (pu == null || !pu.isCancelled()));
        if (pu != null && lastReported > 0) {
            pu.report(null, (int) lastReported, (int) total,
                      (int) finalAmountForDisplayPurposes);
        }
    }

    /**
     * Create a new file, making sure to cure any illegal chars in the filename
     * Note that this does not check the length of the final path.
     * Therefore it is possible that the resulting file may not be readable by
     * the operating system (> 256 on windows).
     * @param parent File - the base path
     * @param fileName String
     * @throws IOException
     * @return File
     */
    public static File createFile(final File parent, final String fileName) throws
      IOException {
        File file = null;
        String modFileName = replaceFilenameAllergicChars(fileName);

        if (modFileName != null) {
            file = new File(parent, modFileName);
            file.createNewFile();
        }
        return file;
    }

    public static FileOutputStream createFileOutputStream(final String filePath) throws
      IOException {
        final int l = filePath.lastIndexOf(File.separator);
        if (l >= 0) {
            final String dir = filePath.substring(0, l);
            mkDirs(dir);
        }
        final File file = new File(filePath);
        file.createNewFile();
        return new FileOutputStream(file);
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param is InputStream
     */
    public static void closeWithoutThrowingUp(final ZipOutputStream zos) {
        if (zos != null) {
            try {
                zos.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param is InputStream
     */
    public static void closeWithoutThrowingUp(final InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param os OutputStream
     */
    public static void closeWithoutThrowingUp(final OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param is Reader
     */
    public static void closeWithoutThrowingUp(final Reader is) {
        if (is != null) {
            try {
                is.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param is Reader
     */
    public static void closeWithoutThrowingUp(final RandomAccessFile is) {
        if (is != null) {
            try {
                is.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    /**
     * Muffles exceptions writing them to PEL log (program event log)
     * @param os Writer
     */
    public static void closeWithoutThrowingUp(final Writer os) {
        if (os != null) {
            try {
                os.close();
            } catch (final IOException ie) {
                Pel.log.warn(ie);
            }
        }
    }

    public interface Storer {
        boolean store(String relativeFile, File file, ProgressUpdater pu) throws
          Exception;

        String getEngineerableUrl(String relativeFolder);

        String getDisplayableUrl(String relativeFolder);
    }


    public static Storer newStorer(final String className) {
        try {
            final Class<?> cl = Class.forName(className);
            if (cl != null) {
                final Object storer = cl.newInstance();
                if (storer instanceof Storer) {
                    return (Storer) storer;
                }
            }
        } catch (final Throwable e) {
            Pel.log.warn(e);
        }
        return null;
    }

    public static String rtrimSlash(final String arg) {
        int i = lastNonSlash(arg);
        return i >= 0 ? arg.substring(0, i + 1) : "";
    }

    public static int lastNonSlash(final String arg) {
        char[] c = arg.toCharArray();
        for (int i = c.length - 1; i >= 0; i--) {
            if (c[i] != '/' && c[i] != '\\') {
                return i;
            }
        }
        return -1;
    }

    public static String appendSlash(final String arg) {
        if (arg == null) {
            return "/";
        } else {
            if (arg.endsWith("/") || arg.endsWith("\\")) {
                return arg;
            } else {
                return arg + "/";
            }
        }
    }

    public static String ltrimSlash(final String arg) {
        int i = firstNonSlash(arg);
        return i >= 0 ? arg.substring(i) : "";
    }

    public static int firstNonSlash(final String arg) {
        if (arg != null) {
            char[] c = arg.toCharArray();
            for (int i = 0; i < c.length; i++) {
                if (c[i] != '/' && c[i] != '\\') {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String subtractRootDirectory(
      final String absolutePath,
      final String rootPath) {
        if (absolutePath.startsWith(rtrimSlash(rootPath))) {
            return ltrimSlash(absolutePath.substring(rtrimSlash(rootPath).length()));
        }
        return null;
    }

    public static String translateToNativeSlash(final String path) {
        if (File.separatorChar == '/') {
            return Basics.translate(path, '\\', '/');
        }
        return Basics.translate(path, '/', '\\');
    }

    public static String standardizeFileName(final String arg) {
        if (!Basics.isEmpty(arg)) {
            if (arg.length() > 1 && arg.charAt(1) == ':') {
                // if there is a drive letter make it upper case
                return Basics.translate(arg.substring(0, 2).toUpperCase() +
                                        arg.substring(2), '\\', '/');
            }
            return Basics.translate(
              arg,
              '\\',
              '/');
        }
        return arg;
    }

    public static void purgeFiles(final File dir, final String startsWith,
                                  final String endsWith, boolean verbose) {
        final String[] files = dir.list();
        final String desktopDir = dir.getAbsolutePath();

        for (int i = 0; i < files.length; i++) {
            if (files[i].startsWith(startsWith) && files[i].endsWith(endsWith)) {
                final File file = new File(desktopDir + "/" + files[i]);
                if (verbose) {
                    Pel.log.print("Removing file " + file.getAbsolutePath());
                }
                file.delete();
            }
        }

    }

    public static File getFirstFile(final File dir, final String startsWith,
                                    final String endsWith) {
        final String[] files = dir.list();
        final String desktopDir = dir.getAbsolutePath();

        for (int i = 0; i < files.length; i++) {
            if (files[i].startsWith(startsWith) && files[i].endsWith(endsWith)) {
                return new File(desktopDir + "/" + files[i]);
            }
        }
        return null;

    }

    public static boolean scpDownload(
      final String pw,
      final String account,
      final String host,
      final String path,
      final File dir,
      final String sshPort) {
        final String dstPath = IoBasics.rtrimSlash(dir.getAbsolutePath());
        boolean ok = false;
        String command;
        String os = System.getProperty("os.name");

        boolean isWindows = os != null && os.startsWith("Windows");
        if (isWindows) {
            command = "pscp -P " + sshPort + " -unsafe -r -pw \"" + pw + "\" " + account +
                      "@" + host +
                      ":" + path + " \"" + dstPath + "\"";
        } else {
            throw new UnsupportedOperationException(
              "ONLY scp for windows is supported");
//command = "scp -a";
        }
        System.out.println("Executing:  ");
        System.out.println(command);
        System.out.println();
        BufferedReader br = null;
        try {
            final Process p = Runtime.getRuntime().exec(command);
            br =
              new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String output = null;
            while ((output = br.readLine()) != null) {
                if (isWindows) {
                    if (output.indexOf("100%") >= 0) {
                        br.close();
                        br = null;
                        return true;
                    }
                } else {
                }
            }
            br.close();
            br = null;
            br =
              new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
            while ((output = br.readLine()) != null) {
                if (isWindows) {
                    if (output.indexOf("100%") >= 0) {
                        br.close();
                        br = null;
                        return true;
                    }
                } else {
                }
            }
            br.close();
            br = null;

        } catch (final IOException e) {
            Pel.log.print(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    Pel.log.print(e);
                }

            }
        }
        return ok;

    }

    public static String extractFolder(final char[] c, final int idx) {
        final StringBuilder sb = new StringBuilder(50);
        for (int j = idx; j < c.length; j++) {
            if (c[j] == '/' || c[j] == '\\') {
                break;
            }
            sb.append(c[j]);
        }
        return sb.toString();
    }

    public static String extractFolder(final String path, int pathNumberFromStart) {
        final char[] c = path.toCharArray();
        if (pathNumberFromStart == 0) {
            return extractFolder(c, 0);
        }

        int cnt = 0;
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '/' || c[i] == '\\') {
                while (i < c.length && (c[i] == '/' || c[i] == '\\')) {
                    i++;
                }
                cnt++;
                if (cnt == pathNumberFromStart) {
                    return extractFolder(c, i);
                }
            }
        }
        return null;
    }

    public static boolean move(
      final File from,
      final File to) {
        boolean ok; // debugger friendly choice
        ok = to.delete();
        mkDirs(to.getParent());
        ok = from.renameTo(to);
        return ok;
    }

    public static boolean copy(
      final File from,
      final File to,
      final ProgressUpdater pu) throws IOException {
        boolean rc = false;
        if (from.exists()) {
            mkDirs(to.getParent());
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(from);
                out = new FileOutputStream(to);
                copy(out, in, from.length(), pu);
                out.close();
                out = null;
                in.close();
                in = null;
                rc = true;
            } finally {
                closeWithoutThrowingUp(in);
                closeWithoutThrowingUp(out);
            }
        }
        return rc;
    }

    // EPS,
    // Unzip all entries in sourceZipFile to destinationDir without
    // maintaining entry structure.
    //
    public static final void flatUnzip(final String sourceZipFile,
                                       final String destinationDir,
                                       final ProgressUpdater pu) {
        final Enumeration<? extends ZipEntry> entries;
        final ZipFile zipFile;
        final String dstDir = !destinationDir.endsWith(File.separator) ?
                              destinationDir + File.separator : destinationDir;
        InputStream is = null;
        OutputStream os = null;

        try {
            zipFile = new ZipFile(sourceZipFile);
            entries = zipFile.entries();

            // EPS,
            // Assure destination dir exists
            File dir = new File(dstDir);
            if (!dir.exists()) {
                IoBasics.mkDirs(dir.getCanonicalPath()); // throws IOException if canonical path cannot be constructed
            }

            while (entries.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory()) {
                    // Don't worry about directories since this is flat unzip
                    continue;
                }

                if (pu != null) {
                    pu.report(Condition.NORMAL.annotate("Extracting file: " +
                      entry.getName()));
                }
                is = zipFile.getInputStream(entry);
                final String entryNameWithoutPath = new File(entry.getName()).getName();
                os = new BufferedOutputStream(new FileOutputStream(dstDir +
                  entryNameWithoutPath));
                copy(os, is, entry.getSize(), pu);
                is.close();
                is = null;
                os.close();
                os = null;
            }
            zipFile.close();
        } catch (final IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        } finally {
            closeWithoutThrowingUp(is);
            closeWithoutThrowingUp(os);
        }
    }

    public static String[] getZipEntryNames(final File f) throws IOException {
        final ZipFile zipFile = new ZipFile(f.getAbsolutePath());
        final Collection<String> c = new ArrayList<String>();
        final Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            c.add(ze.getName());
        }
        zipFile.close();
        return c.toArray(new String[c.size()]);
    }

    public static void unzipRecursively(
      final String sourceZipPathName,
      final String destinationDir,
      final ProgressUpdater pu) {
        unzipRecursively(sourceZipPathName, destinationDir, false, true, pu);
    }


    public static final void unzipRecursively(
      final String sourceZipPathName,
      final String destinationDir,
      final boolean junkZipFolders,
      final boolean overWrite,
      final ProgressUpdater pu) {
        int lastChoice = 2;
        final Enumeration<? extends ZipEntry> entries;
        final ZipFile zipFile;
        final String dstDir = !destinationDir.endsWith(File.separator) ?
                              destinationDir + File.separator : destinationDir;
        InputStream is = null;
        OutputStream os = null;

        try {
            zipFile = new ZipFile(sourceZipPathName);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                if (entry.isDirectory() && !junkZipFolders) {
                    // Assume directories are stored parents first then children.
                    if (pu != null) {
                        pu.report(Condition.NORMAL.annotate("Extracting directory: " +
                          entryName));
                    }
                    // This is not robust, just for demonstration purposes.
                    IoBasics.mkDirs(dstDir + entryName);
                    continue;
                }

                // EPS,
                // Assure parent dir exists in case the above assumption is false.
                File dir = null;
                if (!junkZipFolders) {
                    dir = new File(dstDir, entryName).getParentFile();
                } else {
                    final int idx = entryName.lastIndexOf('/');
                    if (idx >= 0) {
                        entryName = entryName.substring(idx + 1);
                    }
                    dir = new File(dstDir);
                }
                if (!dir.exists()) {
                    IoBasics.mkDirs(dir.getCanonicalPath()); // throws IOException if canonical path cannot be constructed
                }

                final File outputFile = new File(dstDir, entryName);
                boolean canExtract = overWrite || lastChoice == 3;
                if (outputFile.exists()) {
                    if (!canExtract) {
                        if (lastChoice != 1) {
                            lastChoice = Basics.gui.choose(
                              null,
                              "Overwrite " +
                              outputFile.getAbsolutePath() +
                              "?", "Alert", new String[] {"No",
                              "No to all", "Yes",
                              "Yes to all"}
                              , lastChoice, true, false);
                            if (lastChoice > 1) {
                                canExtract = true;
                            }
                        }
                    }
                } else {
                    canExtract = true;
                }
                if (canExtract) {
                    if (pu != null) {
                        pu.report(Condition.NORMAL.annotate("Extracting file: " +
                          entry.getName()));
                    }
                    is = zipFile.getInputStream(entry);
                    os = new BufferedOutputStream(new FileOutputStream(
                      outputFile));
                    copy(os, is, entry.getSize(), pu);
                    is.close();
                    is = null;
                    os.close();
                    os = null;
                } else {
                    if (pu != null) {
                        pu.report(Condition.NORMAL.annotate("Ignoring file: " +
                          entry.getName()));
                    }
                }
            }
            zipFile.close();
        } catch (final IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        } finally {
            closeWithoutThrowingUp(is);
            closeWithoutThrowingUp(os);
        }
    }

    public static String getSystemProperty(final String property) {
        String value;
        try {
            value = System.getProperty(property);
        } catch (SecurityException e) {
            value = null;
        }
        return value;
    }

	public static void main(final String[] args) {
        Pel.init(null, IoBasics.class, IoBasics.class.getName(), false);
        try {
            execJnlp(args[0], new String[] {args[1]});
        } catch (final IOException e) {
            System.out.println(e);
        }
        final BufferedReader br =
          IoBasics.getURLReaderWithoutThrowingUp(
            "http://facs.stanford.edu:8080/sciencexperts/users/tung@darwin.stanford.edu/FACS/user.jnlp");
        if (br != null) {
            final ArrayList<String> al = readTextLinesAndClose(br, false);
            Basics.gui.alert("Found " + al.size() + " lines ", false);
        }

    }

    public static String removeLastFolder(final String path) {
        final int li = path.lastIndexOf('/');
        if (li < 0) {
            return path;
        }
        String ss = path.substring(0, li);
        final int li2 = ss.lastIndexOf('/');
        if (li2 < 0) {
            return path.substring(li + 1);
        }
        return path.substring(0, li2 + 1) +
          path.substring(li + 1);

    }

    public static void copyAndUnzipRecursivelyInTheBackground(
      final String targetPathName,
      final String targetUnzipDir,
      final String sourceUrl,
      final ProgressUpdater pu) {
        copyAndUnzipRecursively(targetPathName, targetUnzipDir, sourceUrl, false, true,
                                pu);
    }

    public static void copyAndUnzipRecursivelyInTheBackground(
      final String targetPathName,
      final String targetUnzipDir,
      final String sourceUrl,
      final boolean junkZipFolder,
      final boolean overWrite,
      final ProgressUpdater pu) {
        if (isURL(sourceUrl)) {
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    copyAndUnzipRecursively(
                      targetPathName, targetUnzipDir, sourceUrl, junkZipFolder, overWrite,
                      pu);
                }
            });
            thread.start();
        }
    }

    public static void copyInTheBackground(
      final String toPath,
      final String fromUrl,
      final boolean indicateFinishedWhenDone,
      final ProgressUpdater pu) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
            	copyWithoutThrowingUp(toPath, fromUrl, pu, indicateFinishedWhenDone);
            }
        });
        thread.start();
    }

    public static void copyAndUnzipRecursively(
      final String targetPathName,
      final String targetUnzipDir,
      final String sourceUrl,
      final ProgressUpdater pu) {
        copyAndUnzipRecursively(targetPathName, targetUnzipDir, sourceUrl, false, true, pu);
    }

    public static void copyAndUnzipRecursively(
      final String targetPathName,
      final String targetUnzipDir,
      final String sourceUrl,
      final boolean junkZipFolders,
      final boolean overWrite,
      final ProgressUpdater pu) {
        try {
            copy(targetPathName, sourceUrl, pu);
            unzipRecursively(targetPathName, targetUnzipDir, junkZipFolders, overWrite,
                             pu);
            if (pu != null) {
                pu.report(new Condition.Annotated(Condition.FINISHED, "done"));
            }
        } catch (final IOException ioe) {
            Pel.log.print(ioe);
            if (pu != null) {
                pu.report(new Condition.Annotated(Condition.FATAL, ioe.getMessage()));
            }

        }
    }

    public static void rmdir(final File f) {
        if (f.isDirectory()) {
            final String d = f.getAbsolutePath() + File.separator;
            final String[] fs = f.list();
            for (int i = 0; i < fs.length; i++) {
                rmdir(new File(d + fs[i]));
            }
        }
        f.delete();
    }

    public static int getFileCount(final File f, final Collection<String> doNotStartWith,
                                   final Collection<String> endsWith) {
        int cnt = 0;
        if (f.isDirectory()) {
            final String d = f.getAbsolutePath() + File.separator;
            final String[] fs = f.list();
            for (int i = 0; i < fs.length; i++) {
                cnt += getFileCount(new File(d + fs[i]), doNotStartWith, endsWith);
            }
        } else {
            final String n = f.getName();
            if (Basics.isEvilEmpireOperatingSystem()) {
                if (!Basics.startsWithIgnoreCase(doNotStartWith, n) &&
                    Basics.endsWithIgnoreCase(endsWith, n)) {
                    cnt = 1;
                }
            } else {
                if (!Basics.startsWith(doNotStartWith, n) &&
                    Basics.endsWith(endsWith, n)) {
                    cnt = 1;
                }
            }
        }
        return cnt;
    }

    public static int getFileCount(final File f) {
        int cnt = 0;
        if (f.isDirectory()) {
            final String d = f.getAbsolutePath() + File.separator;
            final String[] fs = f.list();
            for (int i = 0; i < fs.length; i++) {
                cnt += getFileCount(new File(d + fs[i]));
            }
        } else {
            cnt = 1;
        }
        return cnt;
    }

    public static File getFile(
      final String destinationDirectory,
      final String relativeURLLocation) {
        String dir = destinationDirectory, s = relativeURLLocation;
        for (int idx = s.indexOf('/'); idx >= 0; ) {
            dir += s.substring(0, idx);
            dir += File.separator;
            s = s.substring(idx + 1);
            idx = s.indexOf('/');
        }
        final File directory = new File(dir);
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
        return new File(dir + s);
    }

    // "have you no sense of decency, sir?"
    //private final static int BUFFER_SIZE = 10000000; // 10 Meg

    public final static String PROTOCOL_CLASS = "class://";

    public static boolean isJarProtocol(final String url) {
        return url != null &&
          (url.startsWith(PROTOCOL_CLASS) || url.startsWith("jar://"));
    }

    public static URL getURL(final String url) {
        try {
            return getUrl(url);
        } catch (final IOException ioe) {
// Okay cause user wants a null
        }
        return null;
    }


    private static URL getUrl(final String url) throws IOException {
        if (url == null) {
            throw new MalformedURLException("Null exception unallowed");
        } else if (url.startsWith(PROTOCOL_CLASS)) {
            final String s = url.substring(PROTOCOL_CLASS.length());
            final int idx = s.indexOf('/');
            if (idx >= 0) {
                final String className = s.substring(0, idx);
                try {
                    //DREPC
                    final Class<?> cl = Class.forName(className);
                    final String relativeFileName = s.substring(idx + 1);
                    final URL _url = cl.getResource(relativeFileName);
                    return _url;
                } catch (final ClassNotFoundException e) {
                    Pel.note(e);
                    throw new MalformedURLException("Malformed Class URL error: \n" + url +
                      "\n" + e.getMessage());
                }
            } else {
                throw new MalformedURLException("Malformed Class URL error: \n" + url +
                                                "\n");
            }
        } else {
            return new URL(url);
        }
    }


    public static File mkDirs(final File folder) {
    	return mkDirs(folder.getAbsolutePath());
    }
    public static File mkDirs(final String folder) {
        final File f = new File(folder);
        if (!f.exists() || !f.isDirectory()) {
            f.mkdirs();
        }
        return f;
    }

    public static boolean atleastOneExists(
    		final File folder, final String endsWith) {
    	boolean yes = false;
    	if (folder.exists() && folder.isDirectory()) {
    		for (final String file :folder.list()) {
    			if (file.endsWith(endsWith)) {
    				yes = true;
    				break;
    			}
    		}
    	}
    	return yes;
    }

    public static String replaceFilenameAllergicChars(final String fileName) {
        return replaceFilenameAllergicChars(fileName, '_');
    }

    public static boolean isAbsoluteFilePath(String path) {
        if (path != null) {
            if (path.toLowerCase().startsWith("file://") ||
                /*drive letter*/
                (path.indexOf(':') == 1 && !path.substring(1).equals("://")) ||
                /* windows slash  */
                path.startsWith("\\") ||
                /* unix slash */
                path.startsWith("/")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isValidDirPath(final String dirPath){
    	if(!Basics.isEmpty(dirPath)){
    		if (dirPath.indexOf("\"") < 0 && 
				(dirPath.indexOf(":") < 0 || dirPath.indexOf(":", 2) < 0) && dirPath.indexOf("|") < 0 &&
				dirPath.indexOf("?") < 0 && dirPath.indexOf("<") < 0 && dirPath.indexOf(">") < 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public  static boolean isValidFileName(final String fileName){
    	if(!Basics.isEmpty(fileName) &&
    		!fileName.contains("/") && !fileName.contains("\\") && !fileName.contains("|") && !fileName.contains("\"") &&
    		!fileName.contains(":") && !fileName.contains("?") && !fileName.contains("*") && !fileName.contains("<") &&
    		!fileName.contains(">")) {
    			return true;
    		}
    	
    	return false;
    }

    public static boolean isFile(final String path) {
        if (path != null) {
            if (!isAbsoluteFilePath(path)) {
                return path.indexOf("://") < 0;
            }
            return true;
        }
        return false;
    }

    public static boolean isAbsolutePath(final String path) {
        if (isAbsoluteFilePath(path)) {
            return true;
        }
        return!isFile(path);
    }

    public static boolean exists(final String fileOrUrl) {
        return exists(null, null, fileOrUrl);
    }

    public static boolean exists(
      final String checkThisRootFolderFirst, // could be URL or file
      final String checkThisRootFolderSecond, // could be URL or file
      final String fileOrUrl) {
        final boolean ok;
        if (Basics.isEmpty(fileOrUrl)) {
            ok = false;
        } else if (!isFile(fileOrUrl)) {
            final Reader r = getURLReaderWithoutThrowingUp(fileOrUrl);
            ok = r != null;
            if (ok) {
                closeWithoutThrowingUp(r);
            }
        } else if (isAbsoluteFilePath(fileOrUrl)) {
            ok = new File(fileOrUrl).exists();
        } else {
            if (checkThisRootFolderFirst != null &&
                exists(null, null, concat(checkThisRootFolderFirst, fileOrUrl))) {
                ok = true;
            } else if (checkThisRootFolderSecond != null &&
                       exists(null, null, concat(checkThisRootFolderSecond, fileOrUrl))) {
                ok = true;
            } else {
                ok = false;
            }
        }
        return ok;
    }

    public static String getExistentURL(
      final String checkThisRootFolderFirst, // could be URL or file
      final String checkThisRootFolderSecond, // could be URL or file
      final String path) {
        final String ok;
        if (Basics.isEmpty(path)) {
            ok = null;
        } else if (!isFile(path)) {
            final Reader r = getURLReaderWithoutThrowingUp(path);
            if (r != null) {
                try {
                    r.close();
                } catch (final IOException ioe) {
                    Pel.log.print(ioe);
                }
                ok = path;
            } else {
                ok = null;
            }

        } else if (isAbsoluteFilePath(path)) {
            final File f = new File(path);
            if (f.exists()) {
                ok = path;
            } else {
                ok = null;
            }
        } else {
            if (checkThisRootFolderFirst != null &&
                exists(null, null, concat(checkThisRootFolderFirst, path))) {
                ok = concat(checkThisRootFolderFirst, path);
            } else if (checkThisRootFolderSecond != null &&
                       exists(null, null, concat(checkThisRootFolderSecond, path))) {
                ok = concat(checkThisRootFolderSecond, path);
            } else {
                ok = null;
            }
        }
        return ok;
    }

    public static Reader getReader(
      final String checkThisRootFolderFirst, // could be URL or file
      final String checkThisRootFolderSecond, // could be URL or file
      final String path) {
        Reader r;
        if (!isFile(path)) {
            r = getURLReaderWithoutThrowingUp(path);
        } else if (isAbsoluteFilePath(path)) {
            r = getFileReader(path);
        } else {
            r = getReader(concat(checkThisRootFolderFirst, path));
            if (r == null) {
                r = getReader(concat(checkThisRootFolderSecond, path));
            }
        }
        return r;
    }

    public static BufferedReader getReader(final Class<? extends Object> clas,
			final String directory, final String name) {
		final InputStream is = getResourceStream(clas, directory, name);
		//File file = new File(directory);
		return is == null ? null : new BufferedReader(
				getInputStreamReaderWithEncoding(is, UTF8));
	}

    public static InputStream getInputStreamWithoutThrowingUp(
      final String checkThisRootFolderFirst, // could be URL or file
      final String checkThisRootFolderSecond, // could be URL or file
      final String path) {
        InputStream is = null;
        if (!isFile(path)) {
            is = getURLInputStreamWithoutThrowingUp(path);
        } else {
            try {
                if (isAbsoluteFilePath(path)) {
                    is = getFileInputStream(path);
                } else {
                    try {
                        final InputStreamAndSize sas = getURLInputStreamAndSize(concat(
                          checkThisRootFolderFirst, path));
                        is = sas.is;
                    } catch (final IOException ioe) {
                        Pel.log.warn(ioe);
                    }
                    if (is == null) {
                        final InputStreamAndSize sas = getInputStreamAndSize(concat(
                          checkThisRootFolderSecond, path));
                        is = sas.is;
                    }
                }
            } catch (final IOException ioe) {
                Pel.log.warn(ioe);
            }
        }
        return is;
    }

    public static Reader getReader(final String fileOrUrl) {
        if (!isFile(fileOrUrl)) {
            return getURLReaderWithoutThrowingUp(fileOrUrl);
        }
        return getFileReader(fileOrUrl);
    }

    public static boolean usingSameServer(final URL left, final URL right) {
        final String lp = left.getProtocol(), rp = right.getProtocol();
        final String lh = left.getHost(), rh = right.getHost();
        final int li = left.getPort(), ri = right.getPort();
        return lp.equalsIgnoreCase(rp) && lh.equalsIgnoreCase(rh) && li == ri;

    }


    public static boolean usingSameServer(final String left, final String right) {
        try {
            return usingSameServer(new URL(left), new URL(right));
        } catch (final MalformedURLException e) {
        }
        return false;
    }

    public static String concat(final String rootPath, final String relativePath) {
        return standardizeFileName(
          rtrimSlash(rootPath) +
          "/" +
          Basics.translate(ltrimSlash(relativePath), ':', '_'));
    }

    public static String replaceFilenameAllergicChars(final String fileName,
      final char with) {
        final String illegalFileName = "~`'\"\\/\n\r\t:*?|";
        final String s = fileName == null ? "Unnamed" : fileName;
        final char[] f = s.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < f.length; i++) {
            final char c = f[i];
            int idx = illegalFileName.indexOf(c);
            if (idx >= 0 || c > 126 || c < 32) {
                sb.append(with);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static File switchExtension(final File f, final String newExtension) {
        final String s = switchExtension(f.getAbsolutePath(), newExtension);
        if (Basics.isEmpty(s)) {
            return null;
        }
        return new File(s);
    }

    public static String switchExtension(
      final String fileName,
      final String newExtension) {
        if (fileName != null) {
            final String newPath;
            final String _newExtension =
              newExtension.startsWith(".") ?
              newExtension :
              (Basics.isEmpty(newExtension) ? "" : "." + newExtension);
            final int idx = fileName.lastIndexOf('.');
            if (idx < 0) {
                newPath = fileName + _newExtension;
            } else {
                newPath = fileName.substring(0, idx) + _newExtension;
            }

            return newPath;
        }
        return newExtension;
    }

    public static File switchExtension(
      final File file,
      final String oldExtension,
      final String newExtension) {
        final String newPath;
        final String _newExtension =
          newExtension.startsWith(".") ?
          newExtension :
          (Basics.isEmpty(newExtension) ? "" : "." + newExtension);
        final String fileName = file.getAbsolutePath();
        final int idx = fileName.lastIndexOf(oldExtension);
        if (idx < 0) {
            newPath = fileName + _newExtension;
        } else {
            newPath = fileName.substring(0, idx) + _newExtension;
        }
        return new File(newPath);
    }

    public static String getExtension(final File f) {
        final String n = f.getName();
        if (n != null) {
            final int idx = n.lastIndexOf('.');
            if (idx > 0) {
                return n.substring(idx + 1);

            }
        }
        return "";
    }

    public static void execJnlp(
      final String url,
      final String[] arguments
      ) throws IOException {
        if (Basics.isEvilEmpireOperatingSystem()) {
            mkDirs("C:\\tmp_execJnlp");
            execJnlp(url, arguments, new File("C:\\tmp_execJnlp"), "tmp_");
        } else {
            execJnlp(url, arguments, new File(System.getProperty("user.home")),
                     "tmp_");
        }
    }

    public static void execJnlp(
      final String url,
      final String[] arguments,
      final File localFolder,
      final String fileNamePrefixForThisLaunch
      ) throws IOException {
        if (url != null && url.endsWith(".jnlp")) {
            final int idx = url.lastIndexOf('/');
            if (idx > 0) {
                final String fileNameForPrototype = IoBasics.replaceFilenameAllergicChars(
                  url.substring(idx + 1));

                final BufferedReader br = getURLReader(url);
                final File cache = new File(localFolder, fileNameForPrototype);
                ArrayList<String> lines = null;
                if (br != null) {
                    //final StringBuilder sb = new StringBuilder();
                    lines = readTextLinesAndClose(br, false);
                    if (!cache.exists()) {
                        saveTextFile(cache.getAbsolutePath(), lines);
                    }
                } else if (cache.exists()) {
                    lines = readTextFileLines(cache);
                }
                if (!Basics.isEmpty(lines)) {
                    final Collection<String> newFile = new ArrayList<String>();
                    for (String line : lines) {
                        if (line.equals("</application-desc>")) {
                            for (int i = 0; i < arguments.length; i++) {
                                newFile.add("<argument>" + arguments[i] + "</argument>");
                            }
                        }
                        newFile.add(line);
                    }
                    final File thisLaunch = new File(localFolder,
                      fileNamePrefixForThisLaunch +
                      fileNameForPrototype);
                    saveTextFile(thisLaunch.getAbsolutePath(), newFile);
                    execReader(thisLaunch.getAbsolutePath());
                } else {
                    throw new IOException("No lines found in url \"" + url + "\"");
                }
            } else {
                throw new IOException("No / found in url argument \"" + url + "\"");
            }

        } else {
            throw new IOException(" \".jnlp\" not found at end of url argument \"" + url +
                                  "\"");
        }
    }

    public static String escapeWindowsCommandLineSpecialCharacters(final String cmd) {
        final char[] ca = cmd.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (final char c : ca) {
            switch (c) {
            case '&':
            case '(':
            case ')':
            case '|':
                sb.append('^');
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String[] getExecCmdArray(final String _documentOrURL) {
        final String[] launchString;
        final String documentOrURL=_documentOrURL.trim();
        if (Basics.isWin9X()) {
            launchString = new String[] {
                           "start", documentOrURL};
        } else if (Basics.isEvilEmpireOperatingSystem()) {
            if (documentOrURL.indexOf(' ') >= 0) {
                final String cmd = "C:\\execJava.cmd";
                saveTextFile(cmd,
                             "\"" + documentOrURL + "\"" + Basics.lineFeed + "exit" +
                             Basics.lineFeed);
                launchString = new String[] {
                               "cmd", "/c", "start", cmd
                };

            } else {
                launchString = new String[] {
                               "cmd", "/c", "start",
                               escapeWindowsCommandLineSpecialCharacters(documentOrURL)};
            }
        } else { // must be unix berkley flavor like Red Hat or MAC OS X
            launchString = new String[] {
                           "open", documentOrURL};
        }
        return launchString;
    }

    public static void execReader(final String documentOrURL) throws IOException {
        Runtime.getRuntime().exec(getExecCmdArray(documentOrURL));
    }

    public static void execReaderWithoutThrowingUp(final String documentOrURL) {
        try {
            execReader(documentOrURL);
        } catch (final IOException e) {
            Pel.println(e);
        }
    }

    public static BufferedReader getURLReaderWithoutThrowingUp(final String url) {
        BufferedReader reader = null;
        if (!Basics.isEmpty(url)) {
            final InputStream is = getURLInputStreamWithoutThrowingUp(url);
            if (is != null) {
                reader = new BufferedReader(getInputStreamReaderWithEncoding(is, UTF8)/*, BUFFER_SIZE*/);
                // This was the old way to read file in non-UTF-8 encoding
                // which we found was cauding come problems with special characters.
                // Hence replacing the creation of BufferedReader as UTF-8 above
                // reader=new BufferedReader(new InputStreamReader(is), BUFFER_SIZE);
            }
        }
        return reader;
    }

    public static boolean ping(final String ping){
        final boolean value;
        final BufferedReader br = getURLReaderWithoutThrowingUp(ping);
        if (br == null) {
            value = false;
        } else {
            value = true;
            IoBasics.closeWithoutThrowingUp(br);
        }
        return value;
    }

    public static BufferedReader getURLReader(final String url) throws IOException {
        BufferedReader reader = null;
        if (!Basics.isEmpty(url)) {
            final InputStreamAndSize sas = getURLInputStreamAndSize(url);
            final InputStream is = sas.is;
            if (is != null) {
                reader = new BufferedReader(getInputStreamReaderWithEncoding(is, UTF8)/*, BUFFER_SIZE*/);
            }
        }
        return reader;
    }

    public static boolean isURL(final String path) {
        return path != null && path.indexOf("://") >= 0;
    }

    //-------------------------------
    // eps, 29Aug2003
    // Ammended method to accept https urls by adding
    // ' || name.startsWith("https:") '.
    // Protect against null pointer exception by adding name != null test.
//
    public static boolean isHttpURL(final String name) {
        return ((name != null) &&
                (name.startsWith("http:") || name.startsWith("https:")));
    }

    //-----------------------------------------------

    public static boolean isHttpSecuredURL(final String name) {
        return (name != null && name.startsWith("https:"));
    }
    
    public static InputStream getURLInputStreamWithoutThrowingUp(final String urlName) {
        try {
            final InputStreamAndSize sas = getURLInputStreamAndSize(urlName);
            return sas.is;
        } catch (final IOException e) {
            // not needed, invoker wants null if problems occur
        }

        return null;
    }

    public static String readWebPage(final String url) throws IOException {
        final InputStreamAndSize sas = getURLInputStreamAndSize(url);
        final InputStream is = sas.is;
        final BufferedReader br = new BufferedReader(getInputStreamReaderWithEncoding(is, UTF8)/*, BUFFER_SIZE*/);
        return readStringAndClose(br);
    }

    public static BufferedReader readWebPageAsLines(final String url) throws IOException {
        final InputStream is = getInputStream(url);
        return new BufferedReader(getInputStreamReaderWithEncoding(is, UTF8)/*, BUFFER_SIZE*/);
    }

    public static boolean lastIoNeededHttpAuthentication = false;
    public static String httpAccessType = null;

    private static final String HIDDEN_ACESSS_TYPE =
      "<input type=\"hidden\" name=\"accessType\" value=\"";

    public static void encodeHiddenAccessType(final PrintWriter out,
                                              final String accessType) {
        out.print(IoBasics.HIDDEN_ACESSS_TYPE);
        out.print(accessType);
        out.println("\">");
    }

    public static String decodeHiddenAccessType(final String s) {
        int idx = s.indexOf(HIDDEN_ACESSS_TYPE);
        if (idx >= 0) {
            int idx2 = s.lastIndexOf("\">");
            if (idx2 >= 0) {
                return s.substring(idx + HIDDEN_ACESSS_TYPE.length(), idx2);
            }
        }
        return null;
    }

    public static class InputStreamAndSize {
        public final InputStream is;
        public final long size;
        InputStreamAndSize(final InputStream is, final long size) {
            this.is = is;
            this.size = size;
        }
    }


    private static InputStreamAndSize getURLInputStreamAndSize(final String urlName) throws
      IOException {
        lastIoNeededHttpAuthentication = false;
        httpAccessType = null;
        InputStream is = null;
        final URL url = getUrl(urlName);
        int len = -1;
        if (url != null) {
            if (isHttpURL(urlName)) {
                final HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
                ucon.setUseCaches(false);
                ucon.setDoInput(true);
                ucon.setRequestMethod("GET");
                len = ucon.getContentLength();
                try {
                    is = ucon.getInputStream();
                } finally {
                    lastIoNeededHttpAuthentication =
                      ucon.getResponseCode() == HttpURLConnection.HTTP_PROXY_AUTH;
                    if (lastIoNeededHttpAuthentication) {
                        final BufferedReader br = new BufferedReader(new
                          InputStreamReader(ucon.getErrorStream()));
                        String feedBackFromServer = null;
                        while ((feedBackFromServer = br.readLine()) != null) {
                            String hat = IoBasics.decodeHiddenAccessType(
                              feedBackFromServer);
                            if (hat != null) {
                                httpAccessType = hat;
                                break;
                            }

                        }
                    }
                }
            } else {
                is = url.openStream();
            }
        }
        return new InputStreamAndSize(is, len);
    }

    
   
    /**
     * Outputs string to a URL, then reads the reply. This causes Java to use HTTP POST
     * 
     * @param url - of a web service
     * @param data - string to be send in HTTP request body, presumably x-www-form-urlencoded
     * @return - the response body text  
     * @throws IOException
     */
    public static String postToUrl(String url, String data) 
    	throws IOException 
    {
    	// HttpURLConnection ucon = (HttpURLConnection) url1.openConnection();
		URLConnection conn = new URL(url).openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		// ucon.setRequestMethod("POST"); - this is assumed if doing output first
		// ucon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// ucon.setRequestProperty( "Content-Length", encodedData.length() );
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		// wr.flush();
		wr.close();

		// int rc = ucon.getResponseCode();
		// int len = ucon.getContentLength();
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), UTF8));
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String ln;
		while ((ln = rd.readLine()) != null) {
			pw.println(ln);
		}
		rd.close();

		return sw.toString();
	}
    
    private static InputStream getFileInputStream(final String name) throws
      IOException {
        lastIoNeededHttpAuthentication = false;
        return new FileInputStream(name);
    }

    public static InputStream getInputStream(final String nameOrUrl) throws
      IOException {
        return getInputStreamAndSize(nameOrUrl).is;
    }

    public static InputStreamAndSize getInputStreamAndSize(final String nameOrUrl) throws
      IOException {
        final InputStreamAndSize is;
        if (nameOrUrl == null) {
            is = new InputStreamAndSize(null, -1);
        } else {
            if (isURL(nameOrUrl)) {
                is = getURLInputStreamAndSize(nameOrUrl);
            } else {
                InputStream _is = getFileInputStream(nameOrUrl);
                File f = new File(nameOrUrl);
                is = new InputStreamAndSize(_is, f.length());
            }
        }
        return is;
    }

    public static InputStream getResourceStream(final Class<? extends Object> clas,
                                                final String path) {
        return clas.getResourceAsStream(path);
    }

    public static InputStream getResourceStream(final Class<? extends Object> clas,
                                                final String directory,
                                                final String name) {
        return getResourceStream(clas, directory + "/" + name);
    }

    public static String encodeClassMimeType(final Class<? extends Object> clas,
                                             final String relativeLocation) {
        return PROTOCOL_CLASS + clas.getName() + "/" + relativeLocation;
    }

    public static boolean copyResource(
      final String fileSystemFolder,
      final String fileSystemFileName,
      final Class<? extends Object> jarClass,
      final String jarFolder) {
        InputStream in = null;
        OutputStream out = null;
        try {
            final String s = encodeClassMimeType(jarClass, jarFolder);
            final InputStreamAndSize sas = getInputStreamAndSize(s);
            in = sas.is;
            if (in != null) {
                final File file = getFile(fileSystemFolder, fileSystemFileName);
                //final byte[] buf = new byte[5 * 2048];
                file.createNewFile();
                out = new FileOutputStream(file);
                copy(out, in, 0, null);
                in.close();
                in = null;
                out.close();
                out = null;
                return true;
            } else {
                Pel.log.printErr("Can not find " + s);
            }
        } catch (final IOException ioe) {
            Pel.log.print(ioe);
        } finally {
            closeWithoutThrowingUp(in);
            closeWithoutThrowingUp(out);
        }
        return false;
    }

    public static String standardizeUrl(final String input) {
		String[] folders = Basics.split(convertBackSlashOfEvilEmpire(input), "/");
		for (int i = 0; i < folders.length; i++) {
			try {
				folders[i] = URLEncoder.encode(folders[i], UTF8);
			} catch (final UnsupportedEncodingException ioe) {
				Pel.log.print(ioe);
			}
		}
		return Basics.join(folders, "/");
	}
    public static boolean copyWithoutThrowingUp(
    		final String toPath,
    		final String fromUrl,
    		final ProgressUpdater pu,
    		boolean indicateFinishedWhenDone) {
    	boolean ok=true;
    	try {
    		copy(
    				toPath, fromUrl, pu);
    		if (indicateFinishedWhenDone && pu != null) {
    			pu.report(Condition.FINISHED.annotate("done"));
    		}

    	} catch (final IOException e) {
    		ok=false;
    		Pel.log.warn(e);
    		pu.report(Condition.ERROR.annotate(e.getMessage()));
    	}
    	return ok;
    }
    public static boolean copy(
      final String toPath,
      final String fromUrl,
      final ProgressUpdater pu) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            final InputStreamAndSize sas = getInputStreamAndSize(fromUrl);
            in = sas.is;
            out = createFileOutputStream(toPath);
            copy(out, in, sas.size, pu);
            in.close();
            in = null;
            out.close();
            out = null;
            return true;
        } catch (final IOException ioe) {
            if (pu != null) {
                pu.report(new Condition.Annotated(Condition.FATAL, ioe.getMessage()));
            }
            return false;
        } finally {
            closeWithoutThrowingUp(in);
            closeWithoutThrowingUp(out);
        }
    }

    public static void copy(
      final File fileToCreate,
      final InputStream inputStream,
      final ProgressUpdater pu) throws IOException {
        OutputStream out = null;
        InputStream in = inputStream;
        try {
            out = new FileOutputStream(fileToCreate);
            copy(out, in, -1, pu);
            in.close();
            in = null;
            out.close();
            out = null;
        } finally {
            closeWithoutThrowingUp(in);
            closeWithoutThrowingUp(out);
        }
    }

    public static BufferedReader getFileReader(final String name) {
        BufferedReader reader = null;
        final InputStream is = getFileInputStreamWithoutThrowingUp(name);
        if (is != null) {
            reader = new BufferedReader(getInputStreamReaderWithEncoding(is, UTF8));
        }
        return reader;
    }

    private static InputStream getFileInputStreamWithoutThrowingUp(final String
      name) {
        InputStream is = null;
        try {
            final String fullName = name;
            if (fullName.toLowerCase().startsWith("file://")) {
				is = new FileInputStream(fullName.substring(7));
			} else {
				is = new FileInputStream(fullName);
			}
        } catch (FileNotFoundException e) {
            Pel.log.print(e);
        }
        return is;
    }

    public static String readStringAndCloseWithoutThrowingUp(
      final Class<? extends Object> clas,
      final String directory,
      final String name) {
        final InputStream is = getResourceStream(clas, directory, name);
        return is == null ? null :
          readStringAndCloseWithoutThrowingUp(new BufferedReader(
            getInputStreamReaderWithEncoding(is, UTF8)));
    }

    public static ArrayList<String> readTextLines(
      final Class<? extends Object> clas,
      final String directory,
      final String name) {
        final InputStream is = getResourceStream(clas, directory, name);
        return is == null ? new ArrayList<String>() :
          readTextLinesAndClose(new BufferedReader(getInputStreamReaderWithEncoding(is,
          UTF8)), false);
    }

    public static ArrayList<String> readTextLines(
      final String fileNameOrURL
      ) throws IOException {
        final ArrayList<String> al;
        if (isURL(fileNameOrURL)) {
            final BufferedReader br = getURLReaderWithoutThrowingUp(fileNameOrURL);
            al = readTextLinesAndClose(br, false);
        } else {
            al = readTextFileLines(fileNameOrURL);
        }
        return al;
    }

    public static ArrayList<String> readTextFileLines(final String fileName) throws
      FileNotFoundException {
        return readTextFileLines(new File(fileName));
    }

    public static String[] readTextFileLineArray(final File file) {
        try {
            final Collection<String> c = readTextFileLines(file);
            return c.toArray(new String[c.size()]);
        } catch (final Exception e) {
            return new String[0];
        }
    }
    
    public static ArrayList<String> readTextFileLinesExcludingFirst(final File file) throws FileNotFoundException {
    	ArrayList<String> value = readTextFileLines(file);
    	value.remove(0);
    	return value;
    }

    public static ArrayList<String> readTextFileLines(final File file) throws
      FileNotFoundException {
        ArrayList<String> value = null;
        //FileReader fr=null;
        try {
            // fr=new FileReader(file);
            // returnValue=readTextLinesAndClose(new BufferedReader(fr), false);
            value = readTextLinesAndClose(new BufferedReader(new InputStreamReader(new
            FileInputStream(file), UTF8)), false);
        } catch (final FileNotFoundException fnfe) {
            Pel.log.print(fnfe);
            throw fnfe;
        } catch (final UnsupportedEncodingException ex) {
            Pel.println(ex);
        }
        return value;
    }

    public static String stripQuery(final URL url) {
        String savePath = url.getPath();
        final String protocol = url.getProtocol();
        final String host = url.getHost();
        String port = Integer.toString(url.getPort());
        if (port.equals("80") || port.equals("-1")) {
            port = "";
        } else {
            port = ":" + port;
        }
        if (!savePath.endsWith("/")) {
            savePath += "/";
        }
        final String s = protocol + "://" + host + port + savePath;
        return s;
    }

    /**
     * Read text file into memory as a String object.
     * @param fileName name of file
     * @return memory image of text file
     */
    public static synchronized ArrayList<String> readTextLinesAndClose(
      final BufferedReader br,
      final boolean echoOut) {
        final ArrayList<String> value = new ArrayList<String>();
        if (br != null) {
            try {
                String s;
                while ((s = br.readLine()) != null) {
                    if (echoOut) {
                        System.out.println("***" + s);
                    }
                    value.add(s);
                }
            } catch (final IOException ioe) {
                Pel.log.print(ioe);
            } finally {
                closeWithoutThrowingUp(br);
            }
        }
        return value;
    }

    public static synchronized String readFirstLineAndClose(
    	      final BufferedReader br,
    	      final boolean echoOut) {
    	        if (br != null) {
    	            try {
    	                String s;
    	                while ((s = br.readLine()) != null) {
    	                    if (echoOut) {
    	                        System.out.println("***" + s);
    	                    }
    	                    return s;
    	                }
    	            } catch (final IOException ioe) {
    	                Pel.log.print(ioe);
    	            } finally {
    	                closeWithoutThrowingUp(br);
    	            }
    	        }
    	        return null;
    	    }

    public static String urlDecodePath(final String urlPath) {
        final String[] s = urlPath.split("/");
        final StringBuilder sb = new StringBuilder();
        if (urlPath.startsWith("/")) {
            sb.append('/');
        }
        for (int i = 0; i < s.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
			try {
	            sb.append(URLDecoder.decode(s[i], UTF8));
			} catch (final UnsupportedEncodingException ioe) {
				Pel.log.print(ioe);
			}
        }
        if (urlPath.endsWith("/")) {
            sb.append('/');
        }
        return sb.toString();
    }

    public static String urlEncodePath(final String urlPathText) {
        final String[] s = urlPathText.split("/");
        final StringBuilder sb = new StringBuilder();
        if (urlPathText.startsWith("/")) {
            sb.append('/');
        }
        for (int i = 0; i < s.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            if (s[i].indexOf(' ') >= 0) {
    			try {
                    sb.append(URLEncoder.encode(s[i], UTF8));
    			} catch (final UnsupportedEncodingException ioe) {
    				Pel.log.print(ioe);
    			}
            } else {
                sb.append(s[i]);
            }
        }
        if (urlPathText.endsWith("/")) {
            sb.append('/');
        }
        return sb.toString();
    }

    public static String urlEncodeIfPathHasSpaces(final String urlText) {
        final int idxOfLastSlash = urlText.lastIndexOf('/');
        if (idxOfLastSlash > 0) {
            final int idxOfProtocol = urlText.indexOf("://");
            if (idxOfProtocol > 0) {
                final int idxOfPathStart = urlText.indexOf('/', idxOfProtocol + 3);
                if (idxOfPathStart > 0) {
                    final int idxOfSpaceEmbeddedInPath = urlText.indexOf(' ');
                    if (idxOfSpaceEmbeddedInPath > idxOfPathStart) {
                        final int idxOfQuery = urlText.indexOf('?');
                        if (idxOfQuery < 0 || idxOfSpaceEmbeddedInPath < idxOfQuery) {
                            return concat(urlText.substring(0, idxOfPathStart),
                                          urlEncodePath(urlText.substring(idxOfPathStart)));
                        }
                    }
                }
            }
        }
        return urlText;
    }

    public static URL urlEncodeIfPathHasSpaces(final URL url) {
        final String urlText = url.getPath();
        final int idx = urlText.lastIndexOf('/');
        if (idx > 0) {
            final int idx2 = urlText.indexOf(' ');
            if (idx2 < idx) {
                return changePath(url, urlEncodePath(urlText));
            }
        }
        return url;
    }

    public static URL changePath(final URL url, final String path) {
        String query = url.getQuery();
        final String protocol = url.getProtocol();
        final String host = url.getHost();
        final int port = url.getPort();
        final String portText;
        if (port == 80 || port == -1) {
            portText = "";
        } else {
            portText = ":" + port;
        }
        if (!query.startsWith("?")) {
            query = "?" + query;
        }
        final String urlText = protocol +
                               "://" +
                               host +
                               portText +
                               (path.startsWith("/") ? path : "/" + path) +
                               query;
        try {
            return new URL(urlText);
        } catch (final MalformedURLException me) {
            Pel.println(me);
        }
        return url;
    }

    /**
     * Read text file into memory as a String object.
     * @param fileName name of file
     * @return memory image of text file
     */
    public static synchronized String readStringAndCloseWithoutThrowingUp(final File file) {
        try {
            return readStringAndCloseWithoutThrowingUp(new BufferedReader(new
              InputStreamReader(new FileInputStream(file), UTF8)));
        } catch (final FileNotFoundException fnfe) /** File not found **/ {
            Pel.log.print(fnfe);
        } catch (final UnsupportedEncodingException useex) {
            Pel.log.print(useex);
        }
        return null;
    }

    /**
     * Read text file into memory as a String object.
     * @param fileName name of file
     * @return memory image of text file
     */
    public static synchronized String readStringAndCloseWithoutThrowingUp(final
      BufferedReader br) {
        try {
            return readStringAndClose(br);
        } catch (final IOException ioe) {
            Pel.log.print(ioe);
        }
        return "";
    }
    
    public static synchronized String readStringAndClose(final BufferedReader br) throws
      IOException {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        try {
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(line);
            }
        } finally {
            closeWithoutThrowingUp(br);
        }
        return sw.toString();
    }


    public static synchronized boolean saveTextFile(final File file,
      final String data) {
        return saveTextFile(file.getAbsolutePath(), data);
    }

    public static synchronized boolean saveEncodedTextFile(
      final String fileName,
      final String data) {
        PrintWriter out = null;
        boolean good = false;

        try {
            out = getPrintWriterWithAppropriateEncoding(fileName, UTF8);
            out.println(data);
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }


    public static synchronized boolean saveTextFile(
      final String fileName,
      final String data) {
        PrintWriter out = null;
        boolean good = false;

        try {
            out = getPrintWriterWithAppropriateEncoding(fileName, UTF8);
            out.println(data);
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }

    public static synchronized boolean saveTextFile(
      final String fileName,
      final String data, final boolean append) {
        PrintWriter out = null;
        boolean good = false;

        try {
            // out = new PrintWriter(new FileWriter(fileName, append));
            out = getPrintWriterWithEncodingInAppendMode(fileName, append, UTF8);
            out.println(data);
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }


    public static String convertBackSlashOfEvilEmpire(final String arg) {
		return Basics.translate(arg, '\\', '/');
	}

    public static synchronized boolean saveTextFile(
      final String fileName,
      final Collection<? extends Object> data) {
        PrintWriter out = null;
        boolean good = false;

        try {
            // Old way of writing file in System.Default encoding
            // out = new PrintWriter(new FileWriter(fileName));
            // New way of mandatory forcing files to be written in UTF-8 encoding
            out = getPrintWriterWithAppropriateEncoding(fileName, UTF8);
            for (final Object ln : data) {
                //DREPA
                out.println(ln);
            }
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }

    public static synchronized boolean saveTextFile(
      final File f,
      final Object[] lines) {
        PrintWriter out = null;
        boolean good = false;

        try {
            // out = new PrintWriter(new FileWriter(f));
            out = getPrintWriterWithAppropriateEncoding(f, UTF8);
            for (int i = 0; i < lines.length; i++) {
                out.println(lines[i]);
            }
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }

    public static synchronized boolean saveTextFile(
      final File f,
      final String[] lines) {
        PrintWriter out = null;
        boolean good = false;

        try {
            // out = new PrintWriter(new FileWriter(f));
            out = getPrintWriterWithAppropriateEncoding(f, UTF8);
            for (int i = 0; i < lines.length; i++) {
                out.println(lines[i]);
            }
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }

    private static String tmpdir = null;

    public static String getTempDir() {
        if (tmpdir == null) {
            GetPropertyAction a = new GetPropertyAction("java.io.tmpdir");
            tmpdir = ((String) AccessController.doPrivileged(a));
        }
        return tmpdir;
    }

    public static final synchronized File saveTempTextFile(
      final String prefix,
      final String suffix,
      final String data) {
        File temp = null;
        BufferedWriter out = null;
        try {
            // Create temp file.
            temp = File.createTempFile(prefix, suffix);

            // Delete temp file when program exits.
            temp.deleteOnExit();

            // Write to temp file
            out = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(temp), "UTF-8"));
            out.write(data);
        } catch (final IOException ioe) {
            Pel.log.print(ioe);
        } finally {
            closeWithoutThrowingUp(out);
        }
        return temp;
    }

    public static synchronized boolean saveTextFile(
      final String fileName,
      final Collection<String> linesOfText,
      final boolean ignoreEmpty) {
        PrintWriter out = null;
        boolean good = false;

        try {
            // out = new PrintWriter(new FileWriter(fileName));
            out = getPrintWriterWithAppropriateEncoding(fileName, UTF8);
            for (String txt : linesOfText ) {
                if (ignoreEmpty) {
                    if (txt.trim().length() == 0) {
                        continue;
                    }
                }
                out.println(txt);
            }
            good = true;
        } catch (final Exception e) {
            Pel.log.print(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return good;
    }

    public static String getMACAddress() {
        if (macAddress == null) {
            String address = "";
            String os = System.getProperty("os.name");
            if (os != null) {
                String command;
                boolean isWindows = os.startsWith("Windows");
                if (isWindows) {
                    command = "cmd.exe /c ipconfig /all";
                } else {
                    command = "ifconfig -a";
                }
                BufferedReader br = null;
                try {

                    Process p;
                    try {
                        p = Runtime.getRuntime().exec(command);
                    } catch (final IOException ioe) {
                        if (!isWindows) {
                            command = "/sbin/ifconfig -a";
                            p = Runtime.getRuntime().exec(command); // 2nd try
                        } else {
                            throw ioe;
                        }
                    }

                    br = new BufferedReader(
                      new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (isWindows) {
                            if (line.indexOf("Physical Address") > 0) {
                                int index = line.indexOf(":");
                                index += 2;
                                address = line.substring(index);
                                break;
                            }
                        } else {
                            int idx = line.indexOf("ether");
                            if (idx >= 0) {
                                idx += 5;
                                address = line.substring(idx);
                            } else {
                                idx = line.indexOf("HWaddr");
                                if (idx >= 0) {
                                    idx += 6;
                                    address = line.substring(idx);
                                }
                            }
                        }
                    }
                    br.close();
                    br = null;
                } catch (final IOException e) {
                    Pel.log.warn(e);
                } finally {
                    IoBasics.closeWithoutThrowingUp(br);
                }
            }
            macAddress = address.trim();
        }
        return macAddress;
    }

    private static String hostName = null;
    public static String getComputerNameOfThisMachine() {
        if (hostName == null) {
            String line = "";
            BufferedReader br = null;
            try {
                final Process p = Runtime.getRuntime().exec("hostname");
                br =
                  new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
                while ((line = br.readLine()) != null) {
                    if (!Basics.isEmpty(line)) {
                        break;
                    }
                }
                br.close();
                br = null;

            } catch (final IOException e) {
                Pel.note(e);
            } finally {
                IoBasics.closeWithoutThrowingUp(br);
            }
            hostName = line.trim();
        }
        return hostName;
    }

    public static String[] exec(final String command) throws IOException {
        final ArrayList<String> al = new ArrayList<String>();
        if (command != null) {
            BufferedReader br = null;
            try {
                final Process p = Runtime.getRuntime().exec(command);
                br = new BufferedReader(getInputStreamReaderWithEncoding(p.getInputStream(),
                  UTF8));
                String line = "";
                while ((line = br.readLine()) != null) {
                    al.add(line);
                }
                br.close();
                br = null;
            } finally {
                IoBasics.closeWithoutThrowingUp(br);
            }
        }
        return (String[]) al.toArray(new String[al.size()]);
    }

    public static String[] getJnlpCommand(String url) {
        String[] launchString = null;
        if (System.getProperty("os.name").indexOf("Mac OS") >= 0) {
            launchString = new String[] {
                           "javaws", url};
        } else {
            launchString = new String[] {
                           "cmd", "/c", "javaws", "\"" + url + "\""};
        } 
        return launchString;
    }
    
    private static String macAddress;

    // Recursively delete directory
    //
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
            	for (int i = 0; i < children.length; i++) {
            		deleteDir(children[i]);
            	}            	
            }
        }
        deleteFile(dir);
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                System.out.print("Failed deleting: " + file.toString());
            }
        }
    }

    private static void encodeDir(final StringBuilder sb, final String[] fields,
                                  int cur) {
        if (cur < fields.length) {
            sb.append("<ul>");
            sb.append("<li>");
            sb.append(Basics.encodeHtml(fields[cur]));
            encodeDir(sb, fields, cur + 1);
            sb.append("</ul>");
        }
    }

    public static String dirHtml(final File file) {
        final String fileName = file.getName();
        String s = file.getParent();
        if (s.startsWith(SoftwareProduct.getDocumentsFolder())) {
        	s="<My Documents>"+File.separator+s.substring(SoftwareProduct.getDocumentsFolder().length());
        }
        final String[] fields = s == null ? new String[0] : s.split("[\\\\/]", -1);
        final StringBuilder sb = new StringBuilder("<b>");
        sb.append(fileName);
        sb.append("</b> in the file folder:");
        if (fields.length > 0) {
            encodeDir(sb, fields, 0);
        }
        return sb.toString();
    }

    // The three methods below centralizes the saving of file in the preferred encoding through out the application
    private static PrintWriter getPrintWriterWithAppropriateEncoding(String fileName,
      String encoding) throws Exception{
        PrintWriter out = null;
            if (encoding != null) {
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                  fileName), encoding));
            } else {
                out = new PrintWriter(new FileWriter(fileName));
            }
        
        return out;
    }

    private static PrintWriter getPrintWriterWithAppropriateEncoding(File fileName,
      String encoding) {
        PrintWriter out = null;
        try {
            if (encoding != null) {
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                  fileName), encoding));
            } else {
                out = new PrintWriter(new FileWriter(fileName));
            }
        } catch (FileNotFoundException ex) {
            Pel.log.print(ex);
        } catch (UnsupportedEncodingException ex) {
            Pel.log.print(ex);
        } catch (IOException ex) {
            Pel.log.print(ex);
        }
        return out;
    }

    private static PrintWriter getPrintWriterWithEncodingInAppendMode(String fileName,
      boolean append, String encoding) {
        PrintWriter out = null;
        try {
            if (encoding != null) {
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                  fileName, true), encoding));
            } else {
                out = new PrintWriter(new FileWriter(fileName), true);
            }
        } catch (FileNotFoundException ex) {
            Pel.log.print(ex);
        } catch (UnsupportedEncodingException ex) {
            Pel.log.print(ex);
        } catch (IOException ex) {
            Pel.log.print(ex);
        }
        return out;
    }

    // Return an InputStreamReader with specified encoding
    private static InputStreamReader getInputStreamReaderWithEncoding(InputStream is,
      String encoding) {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(is, encoding);
        } catch (UnsupportedEncodingException ex) {
            Pel.println(ex);
        }
        return reader;
    }

    public static boolean isImage(final String _s) {
        final String s = _s.toLowerCase();
        if (s.endsWith(".jpg") || s.endsWith(".gif") || s.endsWith(".png") || s.endsWith(".bmp")) {
            return true;
        }
        return false;
    }

    public static File[]toArray(final Map<String,File> files){
    	return files.values().toArray(new File[files.size()]);
    }

	// Annoying fuss due to history of code pre-dating JAVA 5 (no generics ... sigh)
    public static File[]toArray(final Collection<File> files) {
    	final File[] value;
    	if (!Basics.isEmpty(files)) {
          value = (File[]) files.toArray(new File[files.size()]);
        } else {
        	value=new File[0];
        }
    	return value;
    }


    public static List<File> toList(final File []files) {
    	return (List<File>) Basics.toList(files);
    }

    public static void createFileFromStream(final InputStream is, final File outputFile) throws IOException {
    	if (is != null) {			
			byte []buf = new byte[2048];
			OutputStream out = new FileOutputStream(outputFile);
			int len;
			while ( (len = is.read(buf)) >= 0) {						
				out.write(buf, 0, len);
			}
			out.close();
			is.close();
    	}
    	
    }
    
    public static void copy(final BufferedReader br, final String fileName){
    	FileOutputStream fo=null;
    	PrintWriter pw=null;
    	BufferedReader _br=br;
    	try{
    		pw = new PrintWriter(fileName, "UTF-8");    		
            String line = "";
            while ((line = _br.readLine()) != null) {
                pw.println(line);
            }
            pw.close();
            pw=null;
            _br.close();
            _br = null;
    	}
    	catch(final Exception e){
    		e.printStackTrace();
    	}
    	finally {
            IoBasics.closeWithoutThrowingUp(_br);
            IoBasics.closeWithoutThrowingUp(pw);
        }
    }

	public static void getExecCmdArray(File directoryPath) throws IOException {
		final String[] command;
		if (Basics.isWin9X()) {
			command = new String[] { "start" };
		} else if (Basics.isEvilEmpireOperatingSystem()) {
			command = new String[] { "cmd", "/c", "start ." };
		} else { // must be unix berkley flavor like Red Hat or MAC OS X
			command = new String[] { "open",
					directoryPath.getAbsolutePath() };
			Runtime.getRuntime().exec(command);
			return;
		}
		Runtime.getRuntime().exec(command, null, directoryPath);
	}
	
	public static void deleteUnZippedFiles(String dirPath,String[] filesList){
		int childIndex = 0;
		for (childIndex = 0; childIndex < filesList.length; childIndex++) {
			if (!filesList[childIndex].endsWith(".jar")) {
				File children = new File(dirPath+ "//"
						+ filesList[childIndex]);
				children.delete();
			}
		}		
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @return NULL if no rename needed, false if attempted and failed, true if all is well.
	 */
	public static Boolean renameToIfPossible(final String from, final String to, final boolean exitIfFalse){
		final File fileFrom=new File(from), fileTo=new File(to);
		final boolean bFrom=fileFrom.exists(), bTo=fileTo.exists();
		if (bFrom && !bTo){
			final boolean ok=fileFrom.renameTo(fileTo);
			if (!ok && exitIfFalse){
        		Basics.gui.alert(Basics.toHtmlUncenteredSmall("Must exit", "Can not rename <i>"+from+"</i><br>to <i>"+to+"</i><br><br>Please ensure the first folder is not in use!</html>"));
			}
			return ok;
		}
		return null;// not necessary
	}
	
	public static PrintStream getPrintStream(final String filePath){
		PrintStream po=null;
		try{
			FileOutputStream fos=createFileOutputStream(filePath);
			po=new PrintStream(fos);
		} catch (final IOException e){
			e.printStackTrace();
		}
		return po;
	}

  	public static ArrayList<String> readFieldLabels(final String source) {
		final String delimiters = "\t";
		final char[] s = source.toCharArray();
		int i = 0;
		final ArrayList<String> al = new ArrayList<String>();
		// avoid *ODD* problem where the first chracter is some high one like
		// 0xFFFFF
		while (!Character.isLetter(s[i])) {
			i++;
		}
		int l = i;
		for (; i < s.length; i++) {
			if (delimiters.indexOf(s[i]) >= 0) {
				if (i > l) {
					String subString = source.substring(l, i);
					if (!subString.trim().equals("")) {
						al.add(subString.trim());
					}
				}
				if (i == s.length) {
					break;
				}
				l = i + 1;
			}
		}
		if (i > l) {
			String subString = source.substring(l, i);
			if (!subString.trim().equals("")) {
				al.add(subString);
			}
		}
		return al;
	}
  	public interface FileProgress{
  		void increment(final String s);
  	}
  	
	public static File getTabDataSourceFile(final Collection<File> tabFiles,
			final List<String> columns,final boolean updateDuplicates,StringBuffer updateCount) {
		return getTabDataSourceFile(null, tabFiles, columns, updateDuplicates, updateCount);
	}

	public static File getTabDataSourceFile(final FileProgress fp, final Collection<File> tabFiles,
			final List<String> columns,final boolean updateDuplicates,StringBuffer updateCount) {

		if (tabFiles.size() == 1) {
			return tabFiles.iterator().next();
		}
		
		LinkedHashMap<String,StringBuilder> map=new LinkedHashMap<String,StringBuilder>();
		File file = null;
		BufferedWriter writer = null;
		InputStreamReader fr = null;
		try {
			file = File.createTempFile("catalog","temp");
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));

			// Write the header
			StringBuffer columnHeader = new StringBuffer("");
			for (String column : columns) {
				if (!columnHeader.toString().trim().equals("")) {
					columnHeader.append("\t");
				}
				columnHeader.append(column);
			}
			columnHeader.append("\n");
			writer.write(columnHeader.toString());
			
			int linecount = 0;
			for (final File tabFile : tabFiles) {
				if (!tabFile.exists()){
					continue;
				}
				fr = new InputStreamReader(new FileInputStream(tabFile),
						"UTF-8");
				if (fp!=null){
					fp.increment(Basics.concat("Reading ", tabFile.getName()));
				}
				ArrayList<String> thisLines = IoBasics.readTextLinesAndClose(
						new BufferedReader(fr), false);
				if (thisLines.size() > 0) {
					String columnHeaderLine = thisLines.get(0);
					ArrayList<String> thisColumns = (ArrayList<String>) readFieldLabels(columnHeaderLine);
					boolean metaRead = false;
					for (final String line : thisLines) {
						StringBuilder newLine = new StringBuilder("");
						String lotID = "";
						if (metaRead) {
							final String[] rawValues = Basics.split(line, "\t");
							int i = 0;
							for (final String column : columns) {
								if (i++ > 0) {
									newLine.append("\t");
								}
								int index = thisColumns.indexOf(column);
								if (index != -1) {
									if (rawValues.length > index) {
										newLine.append(rawValues[index]);
										try {
											if (column.trim().equalsIgnoreCase(
													"Lot ID")) {
												lotID = rawValues[index];
											}
										} catch (Exception e) {
											System.out.println("Exception :: "
													+ e.getMessage());
											e.printStackTrace();
										}
									}
								}
								 
								
							}
							newLine.append("\n");
						} else {
							metaRead = true;
						}
						linecount++;
						if(updateDuplicates){
							map.put(lotID,newLine);
						}
						else{
							writer.write(newLine.toString());
						}
					}
				}
				
			}
			if(updateDuplicates){
				
				for(StringBuilder sb:map.values()){
					writer.write(sb.toString());
				}
				updateCount.append(String.valueOf(map.size()));
			}
			
			
			
		} catch (final Exception e) {
			Pel.log.print(e);
		} finally {
			IoBasics.closeWithoutThrowingUp(fr);
			IoBasics.closeWithoutThrowingUp(writer);
		}
		return file;
	}

}

