package com.MeehanMetaSpace.swing;


/**
 * Title:
 * Description:  Basic non visual JAVA utilities
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Container;
import java.awt.GridLayout;
import com.MeehanMetaSpace.*;

public class ZipBasics{
	public static ZipOutputStream createZip(final String pathName) throws FileNotFoundException{
		final FileOutputStream f=new FileOutputStream(
			 pathName.endsWith(".zip")?
			 pathName:
			 pathName+".zip");
		final CheckedOutputStream csum=new CheckedOutputStream(f, new CRC32());
		return new ZipOutputStream(new BufferedOutputStream(csum));
	}

	public final static int BUFSIZE=2048;

	public static void add(
			final ZipOutputStream out,
			final File file,
			final BasicProgressBar basicProgressBar)
		 throws FileNotFoundException, IOException{
		FileInputStream f=null;
		BufferedInputStream in=null;
		final byte[] buf=new byte[BUFSIZE];
		try{
			f=new FileInputStream(file);
			in=new BufferedInputStream(f);
			final ZipEntry zipEntry=new ZipEntry(
				 Basics.getAfterLastDelimiter(file.getPath(), File.separatorChar));
			if (basicProgressBar!=null){
				final StringBuilder sb=new StringBuilder();
				sb.append("Zipping ");
				sb.append(zipEntry.getName());
				sb.append(" size=");
				sb.append(Basics.encode(zipEntry.getSize()));
				basicProgressBar.report(Condition.NORMAL.annotate(sb.toString()));
			}

			out.putNextEntry(zipEntry);
			int len;
			while ( (len=in.read(buf))>=0){
				out.write(buf, 0, len);
				if (basicProgressBar!=null){
					basicProgressBar.addValue(len);
				}
			}
			out.closeEntry();
			if (basicProgressBar!=null){
				final StringBuilder sb=new StringBuilder();
				sb.append("   done: ");
				sb.append(" compressed size=");
				sb.append(Basics.encode(zipEntry.getCompressedSize()));
				basicProgressBar.report(Condition.NORMAL.annotate(sb.toString()));
			}

		} catch (FileNotFoundException fnfe){
			out.closeEntry();
			throw fnfe;
		} catch (IOException ioe){
			out.closeEntry();
			throw ioe;
		} finally{
			in.close();
		}
	}

	public static String zip(
			final File[] files,
			final BasicProgressBar basicProgressBar)
		 throws FileNotFoundException, IOException{
		String zipFileName=null;
		if (!Basics.isEmpty(files)){
			ZipOutputStream out=null;
			try{
				final String projectFilePath=files[0].getAbsolutePath();
				final String path=Basics.getBeforeLastDelimiter(
					 projectFilePath,
					 File.separatorChar);
				if (path!=null){
					final String name=Basics.getAfterLastDelimiter(
						 projectFilePath,
						 File.separatorChar);
					final String s=
						 path
						 +File.separatorChar
						 +Basics.getBeforeLastDelimiter(name, '.')
						 +".zip";
					out=ZipBasics.createZip(s);
					for (int i=0; i<files.length; i++){
						add(out, files[i], basicProgressBar);
					}
					zipFileName=s;
				}
			} finally{
			  if (out != null){
				try {
				  out.close();
				}
				catch (IOException ioe) {
				  Pel.log.print(ioe);
				}
			  }
			}
		}
		return zipFileName;
	}

}
