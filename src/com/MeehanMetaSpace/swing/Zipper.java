package com.MeehanMetaSpace.swing;

/**
 * <p>Title: Zipper</p>
 * <p>Description: Zip and unzip directories maintaining structure.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Ethan Stone
 * @version 1.0
 */

import java.io.*;
import java.util.zip.*;

import com.MeehanMetaSpace.*;

public class Zipper{

  public static final String EMPTY_MANIFEST_DATA=
	  "Manifest-Version: 1.0\nCreated-By: 1.4.1 (Sun Microsystems Inc.)\n";

  public static final int COMPRESSION_LEVEL=9; // eps, Highest compression level.

  public static void main(String[] args){
	if (args.length != 3){
	  printUsage();
	}
	else{
	  String opt=args[0];
	  String path=args[1];
	  String zipFile=args[2];
	  if (opt.equals("-z")){
		PopupBasics.alert("There are " + IoBasics.getFileCount(new File(path)));
		zip(path, null, zipFile, false, null);
		if (PopupBasics.ask("Delete?")){
		  IoBasics.rmdir(new File(path));
		}
	  }
	  else if (opt.equals("-u")){
		unzip(path, zipFile, null);
	  }
	  else{
		printUsage();
	  }
	}
  }

  public static int zip(
	  final String path,
	  final String addRoot,
	  final String zipFile,
	  final boolean addEmptyManifest,
	  final BasicProgressBar bpb){
	File zf=new File(zipFile);
	ZipOutputStream zos=null;
	ZipInputStream zis=null;
	try{
	  if (!zf.exists()){
		zos=new ZipOutputStream(new FileOutputStream(zf));
		zos.setLevel(COMPRESSION_LEVEL);
		if (addEmptyManifest){
		  doZip("META-INF/MANIFEST.MF", zos,
				EMPTY_MANIFEST_DATA, bpb);
		}
		doZip(path, addRoot, path, zos, bpb);
		zos.close();
		zos=null;

	  }
	  else{
		if (bpb != null){
		  bpb.complain("Error: The file " + zf.getAbsolutePath() +
					   " already exists.");
		}
	  }
	  zis=new ZipInputStream(new FileInputStream(zf));
	  int n=0;
	  while (zis.getNextEntry() != null){
		n++;
	  }
	  zis.close();
	  zis=null;
	  return n;
	}
	catch (final IOException ex){
	  if (bpb != null){
		bpb.report(ex);
	  }
	}
	finally{
	  IoBasics.closeWithoutThrowingUp(zos);
	  IoBasics.closeWithoutThrowingUp(zis);
	}
	return 0;
  }

  public static void unzip(
	  final String path,
	  final String zipFile,
	  final BasicProgressBar bpb){
	final File zf=new File(zipFile);
	if (!zf.exists()){
	  bpb.complain("Error: The file " + zf.getName() + " does not exist.");
	  return;
	}
	ZipInputStream zis=null;
	try{
	  zis=new ZipInputStream(new FileInputStream(zf));
	  bpb.brag("\nUnzipping " + zf.getName() + ":\n");
	  doUnzip(path, zis, bpb, true);
	  bpb.conclude("\nFinished unzipping.");
	  zis.close();
	  zis=null;
	}
	catch (final IOException ex){
	  if (bpb != null){
		bpb.report(ex);
	  }
	}
	finally{
	  IoBasics.closeWithoutThrowingUp(zis);
	}
  }

  private static void doZip(
	  final String pathAndFileName,
	  final ZipOutputStream zos,
	  final String data,
	  final BasicProgressBar bpb){
	try{
	  bpb.note(pathAndFileName);
	  final ZipEntry entry=new ZipEntry(pathAndFileName);
	  zos.putNextEntry(entry);
	  zos.write(data.getBytes());
	  zos.closeEntry();
	}
	catch (final IOException ex){
	  if (bpb != null){
		bpb.report(ex);
	  }
	}
	finally{
	}
  }

  public static void createZipUsingParentFolderName(final File zf,
	  final File[] files) throws IOException{
	ZipOutputStream zos=null;
	try{
	  zos=create(zf);
	  for (int i=0; i < files.length; i++){
		final String folder=new File(files[i].getParent()).getName();
		doZip(files[i], IoBasics.concat(folder, files[i].getName()), zos, null);
	  }
	  zos.close();
	  zos=null;
	}
	finally{
	  IoBasics.closeWithoutThrowingUp(zos);
	}

  }

  public static ZipOutputStream create(
	  final File zf
	  ) throws IOException{
	ZipOutputStream zos=null;
	System.out.println("\nZipping files into " + zf.getAbsolutePath() + ":\n");
	if (zf.exists()){
	  zf.delete();
	}
	try{
	  zos=new ZipOutputStream(new FileOutputStream(zf));
	}
	catch (final IOException ex){
	  if (zos != null){
		try{
		  zos.close();
		}
		catch (IOException ex1){
		}
	  }
	  throw ex;
	}
	return zos;
  }

  public static boolean doZip(
	  final String zipDirectory,
	  final File source,
	  final ZipOutputStream zos) throws IOException{
	final String zipEntryname;
	if (!zipDirectory.endsWith("/") &&
		!zipDirectory.endsWith("\\")){
	  zipEntryname=IoBasics.convertBackSlashOfEvilEmpire(
		  zipDirectory +
		  "/" +
		  source.getName());
	}
	else{
	  zipEntryname=IoBasics.convertBackSlashOfEvilEmpire(
		  zipDirectory + source.getName());
	}
	return doZip(source, zipEntryname, zos, null);
  }

  public static boolean doZip(
	  final File source,
	  final String zipEntryname,
	  final ZipOutputStream zos,
	  final BasicProgressBar bpb) throws IOException{
		boolean ok=false;
		if (source.exists()){
		  FileInputStream fis=null;
		  try{
			if (bpb != null){

			  bpb.note(
				  "zipping " +
				  source.getName() +
				  " " +
				  source.length() +
				  " bytes");
			}
			fis=new FileInputStream(source);
		final ZipEntry entry=new ZipEntry(zipEntryname);
		zos.putNextEntry(entry);
			byte[] b=new byte[10 * 1024]; // eps, Read 10KB at a time.
			int n=0;
			while ((n=fis.read(b)) != -1){
			  zos.write(b, 0, n);
			}
			zos.closeEntry();
			fis.close();
			fis=null;
			if (bpb != null){
			  bpb.addValue(1);
			}
			ok=true;
		  }
		  catch (IOException ex){
			if (bpb != null){
			  bpb.report(ex);
			}
			throw ex;
		  }
		  finally{
			IoBasics.closeWithoutThrowingUp(fis);
		  }

		}
		return ok;
	  }

  public static String fixPath(
	  final String p_subtractRoot,
	  final String p_addRoot,
	  final String p_path){
	final String p, retVal;
	final String path=IoBasics.convertBackSlashOfEvilEmpire(p_path),
		subtractRoot=IoBasics.convertBackSlashOfEvilEmpire(p_subtractRoot),
		addRoot=IoBasics.convertBackSlashOfEvilEmpire(p_addRoot);
	if (!Basics.isEmpty(subtractRoot) && path.startsWith(subtractRoot)){
	  p=IoBasics.ltrimSlash(path.substring(subtractRoot.length()));
	}
	else{
	  p=IoBasics.ltrimSlash(path);
	}
	if (Basics.isEmpty(addRoot)){
	  retVal=p;
	}
	else{
	  retVal=IoBasics.ltrimSlash(IoBasics.rtrimSlash(addRoot) + "/" + p);
	}
	return IoBasics.convertBackSlashOfEvilEmpire(retVal);
  }

  public static void doZip(
	  final String subtractRoot,
	  final String addRoot,
	  final String path,
	  final ZipOutputStream zos,
	  final BasicProgressBar bpb) throws IOException{
	final File f=new File(path);
	if (f.isDirectory()){
	  final String directory=path + File.separator;
	  final String[] files=f.list();
	  for (int i=0; i < files.length; i++){
		doZip(subtractRoot, addRoot, (directory + files[i]), zos, bpb);
	  }
	}
    else if (f.isFile()){
      String zipEntry=fixPath(subtractRoot, addRoot, path);
      if ( Basics.isEmpty( zipEntry )){
        zipEntry=new File(path).getName();
      }
	  doZip(f, zipEntry, zos, bpb);
	}
  }

  public static void doUnzip(final String path, final ZipInputStream zis,
			final BasicProgressBar bpb, boolean confirmOverwrite) {
		doUnzip(path, zis, bpb, confirmOverwrite, false);
	}

	public static void doUnzip(final String path, final ZipInputStream zis,
			final BasicProgressBar bpb, boolean confirmOverwrite,
			boolean discardSourcePaths) {
		FileOutputStream fos = null;
		ZipEntry entry = null;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				final String sourceName;
				if (discardSourcePaths) {
					sourceName = new File(entry.getName()).getName();
				} else {
					sourceName = entry.getName();
				}
				final String destinationName = path.endsWith(File.separator) ? path
						+ sourceName
						: path + File.separator + entry.getName();
				final File f = new File(destinationName);
				if (f.exists()) {

					if (confirmOverwrite && !getUserPermissionToOverwrite(f)) {
						zis.closeEntry();
						continue;
					}
				} else {
					if (!createNecessaryParentDirectories(f)) {
						zis.closeEntry();
						continue;
					}
				}
				if (entry.isDirectory()) {
					if (!discardSourcePaths) {
						new File(destinationName).mkdirs();
					}
				} else {
					fos = new FileOutputStream(destinationName);
					byte[] b = new byte[10 * 1024]; // eps, Read 10KB at a time.
					int n = 0;
					while ((n = zis.read(b)) != -1) {
						fos.write(b, 0, n);
					}
					zis.closeEntry();
					fos.close();
					fos = null;
				}
			}
		} catch (final IOException ex) {
			if (bpb != null) {
				bpb.report(ex);
			} else {
				ex.printStackTrace();
			}
		} finally {
			IoBasics.closeWithoutThrowingUp(fos);
		}
	}

  public static boolean doPreferencesUnzip(
		  final String path, 
		  final String tableViewPath, 
		  final ZipInputStream zis,
		  final BasicProgressBar bpb, 
		  boolean confirmOverwrite){
		FileOutputStream fos=null;
		ZipEntry entry=null;
		String name= null;
		boolean isUpgradebeta5to5 = false;
		try{
		  while ((entry=zis.getNextEntry()) != null){
			String entryName = entry.getName();
			if (entryName.contains("CytoGenie")) {
				int indexTableViews = entryName.indexOf("Table Views");					
				name=tableViewPath.endsWith(File.separator) ?
						tableViewPath + entryName.substring(indexTableViews) :
							tableViewPath + File.separator + entryName.substring(indexTableViews);
				isUpgradebeta5to5 = true;
			}
			else {
				name=path.endsWith(File.separator) ?
						path + entry.getName() :
						path + File.separator + entry.getName();
			}
				
			final File f=new File(name);
			if (f.exists()){

			  if (confirmOverwrite && !getUserPermissionToOverwrite(f)){
				zis.closeEntry();
				continue;
			  }
			}
			else{
			  if (!createNecessaryParentDirectories(f)){
				zis.closeEntry();
				continue;
			  }
			}
			fos=new FileOutputStream(name);
			byte[] b=new byte[10 * 1024]; // eps, Read 10KB at a time.
			int n=0;
			while ((n=zis.read(b)) != -1){
			  fos.write(b, 0, n);
			}
			zis.closeEntry();
			fos.close();
			fos=null;
		  }
		}
		catch (final IOException ex){
		  if (bpb != null){
			bpb.report(ex);
		  }
		}
		finally{
		  IoBasics.closeWithoutThrowingUp(fos);
		}
		return isUpgradebeta5to5;
	}

  private static void printUsage(){
	System.out.println("Usage: java Zipper option directory newFile.zip");
	System.out.println("\toption: -z for zip, -u for unzip");
  }

  private static boolean getUserPermissionToOverwrite(final File f){
	return PopupBasics.ask(f.getName() + " already exists.  Overwrite? [y/n]: ");
  }

  private static boolean createNecessaryParentDirectories(File f) throws
	  IOException{
	final File parentDir=f.getParentFile();
	if (!parentDir.exists()){
	  if (!parentDir.mkdirs()){
		System.out.println("Error: Could not create the directory " +
						   parentDir.getCanonicalPath());
		return false;
	  }
	}
	return true;
  }

} // End Class Zipper
