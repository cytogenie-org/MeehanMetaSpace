package com.MeehanMetaSpace.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.MeehanMetaSpace.Basics;

public abstract class FileDownloader implements Runnable {
	private List<File> _downloaded = null;
	private Collection<String> _fileNames;
	private BackgroundRunner _br;
	private File jnlpFile;
	private String _fileNameExtension;
	private boolean isBackGround = false;
	abstract protected List<File> downloadInForeground(final File targetFolder, final BackgroundRunner br, final Collection<String> fileNames, final String fileNameExtension);
	abstract protected List<File> downloadInBackground(final File targetFolder, final Collection<String> fileNames, final String fileNameExtension);

	private File _targetFolder;
	
	public List<File> getInForeground(final JFrame parent,
			final File targetFolder,
			final Collection<String> fileNames, final String title,
			final String msg, final String fileNameExtension, 
			final boolean showProgressbar) {
		this._targetFolder=targetFolder;
		this._fileNames = fileNames;
		this._fileNameExtension = fileNameExtension;
		this.isBackGround = false;
		_downloaded = null;
		_br = new BackgroundRunner(parent, title, msg);
		_br.go(this, showProgressbar);	
		return _downloaded;
	}
	
	public List<File> getInBackground(
			final File targetFolder,
			final Collection<String> fileNames, final String fileNameExtension) {
		_br=null;
		this._targetFolder=targetFolder;
		this._fileNames = fileNames;
		this._fileNameExtension = fileNameExtension;
		this.isBackGround = true;
		_downloaded = null;
		run();	
		return _downloaded;
	}

	//TODO Need to change the following reagent catalog specific variables and usage to a generic way
	private String sourceFolder="reagentcatalog";
	protected String sourceTOCFileName="toc";
	protected String sourceTOCFileNameExtension="cgtoc";
	public String getSourceFolder(){
		return Basics.isEmpty(sourceFolder)?"":sourceFolder+"/";
	}
	public void setSourceFolder(final String sourceFolder){
		this.sourceFolder=sourceFolder;
	}
	public void run() {
		if (isBackGround) {
			_downloaded = downloadInBackground(_targetFolder, _fileNames, _fileNameExtension);
		}
		else {
			_downloaded = downloadInForeground(_targetFolder, _br, _fileNames, _fileNameExtension);
			if (_br != null){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					_br.dlg.dispose();					
				}
			});
		}
		}
		
	}
	
	
}
