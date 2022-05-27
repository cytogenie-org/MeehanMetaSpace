package com.MeehanMetaSpace.swing;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.JnlpBasics;


/**
 * This class runs a servlet communication in the background. 
 * 
 * The servlet need to return a response as a value TRUE indicating success or any error message 
 * The caller of this class need to supply the servlet name to call and optionally the meta information and a text file to upload
 * The status of the servlet communication can be known from isUploadSuccess() method
 * @author govarthananm
 *
 */
public class HTTPFileUploader implements Runnable {
	
	private final File fileToUpload;
	private final String fileMetaInfo;
	private final String PARAMETER_FILE= "file";
	private final String PARAMETER_DIRECTORY= "directory";
	private final String uploadDirectory;
	private final boolean toUpload;
	private final BackgroundRunner backgroundRunner;
	private final String servletName;
	private final String successMessage = "TRUE";
	private boolean isSuccess = false;
	private String errorMessage = "Upload failed";
		
	public HTTPFileUploader(final String servletName, final String metaInfo, final File uploadFile, 
			final String uploadDirectory, final boolean toUpload, final JFrame frame, final String title, final String msg) {
		this.servletName = servletName;
		this.fileMetaInfo = metaInfo;
		this.fileToUpload = uploadFile;
		this.uploadDirectory = uploadDirectory;
		this.toUpload = toUpload;
		backgroundRunner = new BackgroundRunner(frame, title, msg);
		backgroundRunner.go(this, true);
	}
	
	public boolean isUploadSuccess() {
		return isSuccess;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void reset() {
		isSuccess = false;
		errorMessage = "Upload failed";
	}

	private void writeFile(File file, DataOutputStream out) throws Exception {
		final FileInputStream dataStream = new FileInputStream(file);
		final byte[] buf = new byte[1024];
		int len;
		while ((len = dataStream.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		IoBasics.closeWithoutThrowingUp(dataStream);
		IoBasics.closeWithoutThrowingUp(out);
	}
	
	public void run() {
		HttpURLConnection conn = null;
		InputStream stream = null;
		DataOutputStream out = null;
		try {
			final String codebase = JnlpBasics.isOnline() ? JnlpBasics
					.getWebAppRootForClient() : "http://facs.stanford.edu:8080/beta";
					
			URL url = new URL(codebase + "/" + servletName + "?" + PARAMETER_FILE + "=" + 
					fileToUpload.getName() + "&" + PARAMETER_DIRECTORY + "=" + uploadDirectory);				
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setRequestProperty("Content-Type","text/plain");	
			if (toUpload) {
				out = new DataOutputStream(conn.getOutputStream());
				if (fileMetaInfo != null) {
					out.write(fileMetaInfo.getBytes());	
				}
				writeFile(fileToUpload, out);
			}				
			stream = conn.getInputStream();
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			errorMessage = new String(bytes);
			if (errorMessage.trim().equalsIgnoreCase(successMessage)) {
				isSuccess = true;	
			}
			else {
				isSuccess = false;
			}
		} catch (Exception exception) {
			errorMessage = exception.getMessage();
			isSuccess = false;
			exception.printStackTrace();
		}
		finally {
			IoBasics.closeWithoutThrowingUp(out);
			IoBasics.closeWithoutThrowingUp(stream);
			conn = null;
		}
		if (backgroundRunner != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					backgroundRunner.dlg.dispose();
				}
			});
		}
	}
}
