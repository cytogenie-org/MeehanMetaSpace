package com.MeehanMetaSpace.swing;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.JnlpBasics;

public class JavaWebStartFileDownloader extends FileDownloader {

	
	public void setJnlpFile(final File file){
		this._jnlpFile = file;
	}

	private File _jnlpFile;

	protected List<File> downloadInForeground(final File targetFolder, final BackgroundRunner br,
			final Collection<String> fileNames, final String fileNameExtension) {
		final String tf=targetFolder.getAbsolutePath();
		for (final String fileName : fileNames) {
			final File preExistingFile = new File(
					tf,
					fileName + "." + fileNameExtension);
			if (preExistingFile.exists()) {
				preExistingFile.delete();
			}
			URL url = null;
			java.io.InputStream in = null;
			try {
				url = new URL((JnlpBasics.isOnline() ? JnlpBasics
						.getWebAppRootForClient()
						: "http://facs.stanford.edu:8080/beta")
						+ "/"+getSourceFolder()+ fileName + ".jar");
				System.out.println("Fetching catalog url=" + url == null ? url
						: url.toExternalForm());
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				if (connection == null) {
					new Exception("HTTPConnection is NULL").printStackTrace();
					return null;
				}
				in = connection.getInputStream();
				if (in == null) {
					new Exception("InputStream is NULL").printStackTrace();
					return null;
				}
			} catch (final MalformedURLException e) {
				e.printStackTrace();
				br.dlg.dispose();
				return null;
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				// avoid massive memory leak
				com.MeehanMetaSpace.IoBasics.closeWithoutThrowingUp(in);
			}
		}

		try {
			final Runtime runTime = Runtime.getRuntime();
			if (Basics.isMac()) {
				String[] cmd = new String[] { "open", "-n",
						_jnlpFile.getAbsolutePath() };
				runTime.exec(cmd);
			} else {
				runTime.exec("javaws \"" + _jnlpFile.getAbsolutePath() + "\"");
			}

			final List<File> files = new ArrayList<File>();
			for (final String fileName : fileNames) {
				File file = new File(targetFolder, fileName + "." + fileNameExtension);
				while (!file.exists()) {
					if (br.isCancelled) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (final InterruptedException ie) {
						ie.printStackTrace();
						break;
					}

					file = new File(targetFolder, fileName + "." + fileNameExtension);
				}
				files.add(file);
			}
			if (br.isCancelled) {
				return null;
			}
			return files;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<File> downloadInBackground(final File targetFolder,
			final Collection<String> fileNames,
			final String fileNameExtension) {
		//TODO
		return null;
	}
}
