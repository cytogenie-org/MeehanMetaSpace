package com.MeehanMetaSpace.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.JnlpBasics;
import com.MeehanMetaSpace.ThreadSafeStringBuilder;

public class HttpFileDownloader extends FileDownloader {

	public static boolean isSupportedOperation = true;
	
	private String alertMessage = "Unable to download";
	
	private String downloadMessage = "Reagent catalogs download is slow. Do you want to continue?";
	
	public HttpFileDownloader(String downloadFailureAlertMessage) {
		alertMessage = downloadFailureAlertMessage;
	}
	private String urlString(final String fileName){
		final ThreadSafeStringBuilder tssb=ThreadSafeStringBuilder.get();
		final StringBuilder sb=tssb.lock();
		final String root=JnlpBasics.isOnline() ? JnlpBasics.getWebAppRootForClient()
				: "http://cgworkspace.woodsidelogic.com:8080/beta";
		sb.append(root);		
		sb.append( "/");
		sb.append(getSourceFolder());
		sb.append(fileName);
		sb.append(".jar");
		return tssb.unlockString();
	}
	
	private boolean getCatalogs = true, catalogsConnectionAlertShown = false;  
	private boolean needDownload = false, showCatalogsConnectionAlert = true;
	private Exception exception = null;
	private Thread dt = null;

	private boolean needDownload(final String tf, final String fileName, 
			final String fileNameExtension, boolean foregroundTask) throws Exception {
		final File preExistingFile = new File(tf, fileName + ".jar"); 
		String localTimeStamp = "";
		exception=null;
		if (preExistingFile.exists()) {
			File localTimeStampFile =  new File(tf, fileName + "_servertimestamp.txt");
			if (localTimeStampFile.exists()) {					
				localTimeStamp = IoBasics.readStringAndCloseWithoutThrowingUp(localTimeStampFile);
			}
		}
		if((foregroundTask && catalogsConnectionAlertShown && getCatalogs)){
			connectAndCheckNeedDownload(fileName, localTimeStamp);
		}else{
			final Thread ct = Thread.currentThread();
			dt = new Thread(new NeedDownloadThread(fileName, localTimeStamp));
			Thread tt = new Thread(new TimerThread());
			tt.start();
			dt.start();
			dt.join();
			while(!dt.getState().toString().equalsIgnoreCase("TERMINATED")){
				ct.sleep(3000);
			}
			if(dt.getState().toString().equalsIgnoreCase("TERMINATED") && showCatalogsConnectionAlert){
				showCatalogsConnectionAlert = false;
				catalogsConnectionAlertShown = true;
				final boolean prior = PopupBasics.forceBottomRight;
				PopupBasics.forceBottomRight = false;
				if(foregroundTask && PopupBasics.ask(downloadMessage)){
					getCatalogs = true;
					connectAndCheckNeedDownload(fileName, localTimeStamp);
					
				}else{
					getCatalogs = false;
				}
				PopupBasics.forceBottomRight = prior;
			}
		}
		if(exception!=null){
			throw new IOException("Exception in need download");
		}
		return needDownload;
	}
	
	private void doDownload(final String tf, final String fileName, 
			final String fileNameExtension, boolean foregroundTask) throws Exception {	
		if(!catalogsConnectionAlertShown){
			final Thread ct = Thread.currentThread();
			dt = new Thread (new DoDownloadThread(tf,fileName, fileNameExtension));
			Thread tt = new Thread(new TimerThread());
			tt.start();
			dt.start();
			dt.join();
			while(!dt.getState().toString().equalsIgnoreCase("TERMINATED")){
				ct.sleep(1000);
			}
			if(dt.getState().toString().equalsIgnoreCase("TERMINATED") && showCatalogsConnectionAlert){
				catalogsConnectionAlertShown = true;
				final boolean prior = PopupBasics.forceBottomRight;
				PopupBasics.forceBottomRight = false;
				if(foregroundTask && PopupBasics.ask(downloadMessage)){
					PopupBasics.forceBottomRight = prior;
					getCatalogs = true;
					connectAndDoDownload(tf, fileName, fileNameExtension);
				}else{
					getCatalogs = false;
				}
				PopupBasics.forceBottomRight = prior;
			}
			if(exception != null){
				throw exception;
			}
		}else{
			connectAndDoDownload(tf, fileName, fileNameExtension);
		}
	}
	
	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
	
	protected List<File> downloadInBackground(final File targetFolder,
			final Collection<String> fileNames,
			final String fileNameExtension) {
		final String tf = targetFolder.getAbsolutePath();
		final Set<File> files = new HashSet<File>();
		hasUpdates = false;
		getCatalogs = true; 
		catalogsConnectionAlertShown = false;
		for (final String fileName : fileNames) {
			if(getCatalogs){
			try {
				if (needDownload(tf, fileName, fileNameExtension, false)) {
					hasUpdates = true;
					doDownload(tf, fileName, fileNameExtension, false);
				}
			}
			catch(Exception exception) {
				exception.printStackTrace();
				return null;
			}
			files.add(new File(tf, fileName + "." + fileNameExtension));
			}
		}
		
		if (!hasUpdates) {
			return null;
		}
		
		final ArrayList<File>c=new ArrayList<File>();
		c.addAll(files);
		System.out.println("Return to sender: "+Basics.toString(c));
		return c;
	}
	
	protected List<File> downloadInForeground(final File targetFolder,
			final BackgroundRunner br,
			final Collection<String> fileNames,
			final String fileNameExtension) {
		final String tf = targetFolder.getAbsolutePath();
		final Set<File> files = new HashSet<File>();
		getCatalogs = true; 
		catalogsConnectionAlertShown = false;
		for (final String fileName : fileNames) {
			if(getCatalogs){
			try {
				if (needDownload(tf, fileName, fileNameExtension, true)) {
					if ((br != null && br.isCancelled) || !getCatalogs) {
						break;
					}
					doDownload(tf, fileName, fileNameExtension, true);
				}
			}catch(Exception exception) {
				showDownloadFailureAlertMessage();
				return null;
			}		
			files.add(new File(tf, fileName + "." + fileNameExtension));
			}
		}
		if ((br != null && br.isCancelled) || !getCatalogs) {
			return null;
		}
		final ArrayList<File>c=new ArrayList<File>();
		c.addAll(files);
		System.out.println("Return to sender: "+Basics.toString(c));
		return c;
	}
	
	private void showDownloadFailureAlertMessage() {
		final boolean previousPos = PopupBasics.forceBottomRight;
		PopupBasics.forceBottomRight = false;
		PopupBasics.alert(alertMessage);
		PopupBasics.forceBottomRight = previousPos;	
	}
	private boolean hasUpdates = false;
	
	class TimerThread implements Runnable {
		private int time_elapsed;

		TimerThread(){
			this.time_elapsed = 0;
		}

		public void run(){
			while(showCatalogsConnectionAlert) {
				try { 
					Thread.sleep(30000);
				}
				catch (InterruptedException ioe) {
					continue;
				}
				time_elapsed += 30000;
				synchronized (this) {
					if(time_elapsed >= 30000){
						if(showCatalogsConnectionAlert){
								dt.interrupt();
								break;
						}
					}
				}
			}
		}
	}
	
	class NeedDownloadThread implements Runnable{
		String fileName;
		String localTimeStamp;

		public NeedDownloadThread(String fileName, String localTimeStamp) {
			this.fileName = fileName;
			this.localTimeStamp = localTimeStamp;
		}
		
		public void run() {
			connectAndCheckNeedDownload(fileName, localTimeStamp);
		}
	}

	private void connectAndCheckNeedDownload(String fileName, String localTimeStamp) {
		showCatalogsConnectionAlert = true;
		needDownload = false;
		HttpURLConnection connection = null;
		URL url = null;
		java.io.InputStream in = null;
		try {
			if(dt != null){
				dt.sleep(10);
			}
			url = new URL(urlString(fileName));
			connection = (HttpURLConnection) url
			.openConnection();
				
		
			if (connection == null) {
				throw new IOException("HTTPConnection is NULL");
			}
			Date lastModified = new Date(connection.getLastModified());
			showCatalogsConnectionAlert = false;
			if (localTimeStamp.trim().equals(dateFormat.format(lastModified).trim())) {
				System.out.println(Basics.concat(
						"Unchanged ", fileName, " in server ", url.toString(), ".  Using local cache"));
				needDownload = false;
			}
			else {
				System.out.println(Basics.concat("Found updates for ", fileName, " in server.", url.toString(), ". "));
				needDownload = true;
			}
		}catch (final MalformedURLException e) {
			exception = e;
		} catch (final IOException e) {
			exception = e;
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception in need download....."+e);
		}  finally {
			// avoid massive memory leak
			com.MeehanMetaSpace.IoBasics.closeWithoutThrowingUp(in);
		}
	}
	
	class DoDownloadThread implements Runnable{
		String tf;
		String fileName;
		String fileNameExtension;

		public DoDownloadThread(String tf, String fileName, String fileNameExtension) {
			this.tf = tf;
			this.fileName = fileName;
			this.fileNameExtension = fileNameExtension;
		}
		
		public void run() {
			connectAndDoDownload(tf, fileName, fileNameExtension);
		}
	}
	
	private void connectAndDoDownload(String tf, String fileName, String fileNameExtension) {
		showCatalogsConnectionAlert = true;
		URL url = null;
		java.io.InputStream in = null;
		ZipInputStream zis =null;
		try {
			if(dt != null){
				dt.sleep(10);
			}
			url = new URL(urlString(fileName));
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			if (connection == null) {
				throw new IOException("HTTPConnection is NULL");
			}
			Date lastModified = new Date(connection.getLastModified());
			in = connection.getInputStream();
			if (in == null) {
				throw new IOException("InputStream is NULL");					
			} 
			else {		
				//Download
				File outputJar = new File(tf, Basics.concat(fileName, "_temp.jar"));
				IoBasics.createFileFromStream(in, outputJar);
				
				//Delete pre existing files
				File preExistingFile = new File(tf, Basics.concat(fileName, ".jar"));
				preExistingFile.delete();				
				preExistingFile = new File(tf, Basics.concat(fileName, ".cgrit"));
				preExistingFile.delete();
				preExistingFile = new File(tf, Basics.concat(fileName, "_servertimestamp.txt"));
				preExistingFile.delete();
				showCatalogsConnectionAlert = false;
				//Copy temp to real file
				File realJar = new File(tf, Basics.concat(fileName, ".jar"));
				IoBasics.move(outputJar, realJar);
				//Delete temp file
				outputJar.delete();
				//Unzip
				zis = new ZipInputStream(new FileInputStream(realJar));
				Zipper.doUnzip(Basics.concat(tf, File.separator), zis, null, false, true);
				
				//Create updated timestamp
				final File timeStampFile =  new File(tf, Basics.concat(fileName, "_servertimestamp.txt"));
		 		IoBasics.saveTextFile(timeStampFile, dateFormat.format(lastModified));
			}
		} catch (final MalformedURLException e) {
			exception = e;
		} catch (final IOException e) {
			exception = e;
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception in dodownload.."+e);
		}  finally {
			// avoid massive memory leak
			IoBasics.closeWithoutThrowingUp(in);
			IoBasics.closeWithoutThrowingUp(zis);
		}
	}

}
