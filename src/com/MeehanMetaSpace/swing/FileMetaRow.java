

/*
=====================================================================

  FileMetaRow.java

  Created by Stephen Meehan
  Copyright (c) 2002

 =====================================================================
*/
package com.MeehanMetaSpace.swing;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.DateFormat;
import javax.swing.*;
import javax.swing.table.*;
import com.MeehanMetaSpace.*;

final class JLabelCell extends JLabel implements TableCellRenderer {
	JLabelCell(){
		// must  do this or background will not be colored
		setOpaque(true);
	}

	public java.awt.Component getTableCellRendererComponent(
			  final JTable table,
			  final Object value,
			  final boolean isSelected,
			  final boolean hasFocus,
			  final int rowIdx,
			  final int visualIdx)
	{
	  if (! (table instanceof PersonalizableTable)){
				return table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIdx, visualIdx);

			}

		setText(value.toString());
		if (visualIdx >=0){
			final int modelIndex=SwingBasics.getModelIndexFromVisualIndex(table, visualIdx);
			( (PersonalizableTable)table).decorate(
						 table,
						 this,
						 rowIdx,
						 modelIndex,
						 isSelected,
						 hasFocus);
		}
		return this;
	}
}


public class FileMetaRow extends DefaultMetaRow {

	public SortValueReinterpreter getSortValueReinterpreter(int dataColumnIndex){
		return null;
	}

	public String getKey(){
		return "File";
	}

	protected FileMetaRow(final Column [] columns){
		super ( columns );
	}

	protected FileMetaRow(final boolean b){

	}

	protected void initColumns(final Column []additional){
		super.setColumns(concatenate(FileMetaRow.columns, additional));
	}

	public FileMetaRow(){

	}

	protected static final Column PATH=new Column("Path", String.class) {
		protected Object get(final Object rowObject) {
			File file=( (FileHolder) rowObject).file;
			try{
				final String s = file.getCanonicalPath();
				final String n=file.getName();
				if (n.equals("..")){
					return " ";
				}
				return Basics.getBeforeLastDelimiter(s, File.separatorChar);
			} catch (Exception e){
				Pel.log.print(e);
			}
			return file.getParent();
		}
	};

	protected static final Column NAME=new Column("Name", FileName.class) {
		protected Object get(Object rowObject) {
			final File file =((FileHolder) rowObject).file;
			if (file.isDirectory()){
				return Basics.startHtml()+"<i><b>&lt;"+file.getName()+"</b></i>&gt;"+Basics.endHtml();
			}
			return new FileName(((FileHolder) rowObject).file.getName());
		}
	};

	protected static final Column EXTENSION=new Column("Extension", String.class) {
		protected Object get(Object rowObject) {
			final File file=( (FileHolder) rowObject).file;
			if (file.isDirectory()){
				return " <dir>";
			}
			return Basics.getLastField( file.getName(), '.');
		}
	};

	protected static final Column TYPE=new Column("Type", String.class) {
		protected Object get(Object rowObject) {
			final File file=( ( FileHolder) rowObject).file;
			if (file.isDirectory()){
				return "Directory";
			} else if (file.isHidden()){
				return "Hidden";
			}
			return "Normal";
		}
	};

	static DateFormat tf=DateFormat.getTimeInstance(DateFormat.SHORT , new Locale("en", "US"));

	protected static final Column LAST_MODIFIED_DATE=new Column("Date", DateTime.class) {
		protected Object get(Object rowObject) {
			final long l=( (FileHolder) rowObject).file.lastModified();
			return new DateTime(l);
		}
	};


	protected static final Column SIZE=new Column("Size", Long.class) {
		protected Object get(Object rowObject) {
			final File file=( (FileHolder) rowObject).file;
			if (file.isDirectory()){
				return null;
			}

			final long l = ( (FileHolder) rowObject).file.length();
			return new Long(l);
	}};


	static Column [] columns={
		 TYPE,
		 PATH,
		 NAME,
		 EXTENSION,
		 LAST_MODIFIED_DATE,
		 SIZE
	};


	interface FileHolderFactory {
		FileHolder New(File file);
	}
	FileHolderFactory defaultFileHolderFactory=new FileHolderFactory(){
		public FileHolder New(File file){
			return new FileHolder(file);
		}
	};

	public static class FileHolder {
		public final File file;

		public FileHolder(final File file){
			this.file=file;
		}

		public boolean equals(final Object that){
			if (this==that){
				return true;
			}
			if (that instanceof FileHolder){
				return Basics.equals( ( (FileHolder) that).file, file);
			}
			return false;
		}

		public int hashCode(){
			return file != null ? file.hashCode() : 0;
		}

	}


	protected boolean changeUp=true,changeDown=true;

	protected class DataSource  extends DefaultMetaRow.DataSource {

		File rootDir=null;

		protected DataSource (
				  final File dir,
				  final String extension,
				  final FileHolderFactory fileHolderFactory) {
			this();
			rootDir=dir;
			instantiateFiles(dir, extension, fileHolderFactory);
		}

		protected DataSource (final File dir, final String extension) {
			this(dir, extension, defaultFileHolderFactory);
		}

		protected DataSource(){
			super();
			setDataSource(this);
		}

		public DefaultMetaRow.Row [] create(final FileHolder fileHolder) {
			return super.create(fileHolder);
		}

		public final void createFiles(final File dir){
			final PersonalizableTableModel ptm=getTableModel();
			ptm.removeAll();
			instantiateFiles(dir, currentExtension, currentFileHolderFactory);
			ptm.refreshShowingTable(true);
		}

		public boolean handleDoubleClick(final TableCellContext context){
			File file=((FileHolder) ( (DefaultMetaRow.Row)context.row).rowObject).file;
			if (file.isDirectory()){
				/* no NEED to consult changeUp or changeDown here.  instantiateFiles()
				 ONLY allows a directory into the table if these flags
				 indicate that the user can switch to them
				*/
			  if (file.getName().equals("..")){
				file=file.getParentFile();
				if (file==null){
					file=currentDir.getParentFile();
				}
			}

				if (file != null){
					final PersonalizableTableModel ptm=getTableModel();
					ptm.removeAll();
					instantiateFiles(file, currentExtension, currentFileHolderFactory);
					ptm.refreshShowingTable(true);
					return false;
				}
			}
			return true;
		}

		private String currentExtension;
		private File currentDir;
		private FileHolderFactory currentFileHolderFactory=null;

		protected File getCurrentDir(){
			return new File(currentDir.getAbsolutePath());
		}
		public final void instantiateFiles(
				  final File dir,
				  final String extension,
				  final FileHolderFactory fileHolderFactory){
			this.currentFileHolderFactory=fileHolderFactory;
			this.currentDir=dir;
			final String ext = Basics.isEmpty(extension) ? "" :  extension.startsWith(".") ? extension : '.'+extension;
			currentExtension=ext;
			final File[] files = dir.listFiles(new FileFilter() {
				public boolean accept(final File name) {
					if (name.isDirectory()){
						final String s=name.getName();
						boolean isParent=s.equals("..");
						if (!changeUp){
							if (rootDir != null && isParent){
								if (rootDir.getParentFile().equals(name)){
									return false;
								}
							}
						}
						if (!changeDown && !isParent){
							return false;
						}
						return true;
					}
					return name.getName().endsWith(ext);
				}
			});
			final File parentDirectory=new File("..");
			if (changeUp){
				create(fileHolderFactory.New(parentDirectory));
			} else {
				if (!rootDir.equals(dir)){
					create(fileHolderFactory.New(parentDirectory));
				}
			}
			if (files != null){
			  for (int i=0; i < files.length; i++){
				create(fileHolderFactory.New(files[i]));
			  }
			}
		}
	}
	public synchronized PersonalizableTableModel getTableModel(
			final java.awt.Window window){
		if (super.tableModel==null){
			super.getTableModel(window);
			final PersonalizableTable table=tableModel.getTable();
			table.setAnticipateHtml(true);
			table.setDefaultRenderer(FileName.class, new JLabelCell());
			return tableModel;
		} else {
			return super.getTableModel(window);
		}
	}



	/**
	 * Override if you need a new subclass to FileMetaRow.DataSource
	 *
	 * @return data source for rows of meta row
	 * */
	 protected DefaultMetaRow.DataSource newDataSource() {
		 throw new UnsupportedOperationException("FileMetaRow.dataSource association is immutable!");
	 }

	public final static void runDialog(final String startingDirectory){
		final FileMetaRow fmr=new FileMetaRow(columns);
		
		final String dir=PopupBasics.getDirName(
				  null,
				  startingDirectory,
				  "Select",
				  "Pick directory please",
				  null);
		if (!Basics.isEmpty(dir)){
			fmr.new DataSource(new File(dir), null);
			final JDialog dialog=new JDialog(SwingBasics.mainFrame);
			dialog.setModal(true);
			dialog.setTitle("File table demo");
			Container cp=dialog.getContentPane();
			fmr.getTableModel(dialog);
			PersonalizableTable pt=fmr.getTableModel().getTable();
			cp.add(pt.makeHorizontalScrollPane());
			dialog.pack();
			fmr.getTableModel().setPersonalizableWindowOwner(dialog);
			dialog.show();
		}
	}

	public static void main(final String [] args){
		SwingBasics.doDefaultLnF();
	      com.MeehanMetaSpace.Pel.init(
	        IoBasics.concat(System.getProperty("user.home"), "pel.log"),
	        TestPersonalizableTable.class,
	        "FileMetaRow test",
	        false);
	      SwingBasics.resetDefaultFonts();
	      PersonalizableTable.resetDefaultFonts();

		final String dir=args.length>0?args[0] : "..";
		Basics.gui=PopupBasics.gui;
		runDialog(dir);
		System.exit(0);
	}

}

