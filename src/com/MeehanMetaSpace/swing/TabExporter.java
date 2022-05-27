package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.MeehanMetaSpace.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class TabExporter{

  private BasicProgressBar bpb;
  private int rowCnt;
  private PersonalizableTableModel ptm;
  private java.util.List rows;
  private int columnCount;
  private File file;
  private StringConverter[] stringConverters;
  private BufferedWriter bw=null;
  private boolean exportHidden=false;
  private boolean retainColumnNamesAndOrder=false;
  private MetaRow metaRow;
  private HashMap schemasForRelatives=new HashMap(),
      valuesForRelatives=new HashMap();

  void execute(final PersonalizableTableModel tm, final File file){
    rowCnt  = tm.getSelectedRowCount();
    metaRow = tm.dataSource.getMetaRow();
    ptm=tm;

    // We need to export only the rows that have been selected, and give
    // the option of only exporting the columns that are showing
    if (rowCnt != 0){
      rows=tm.getSelectedRowsInDescendingOrder();
    }else {
      rows=tm.getDataSource().getFilteredDataRows();
      rowCnt=tm.dataSource.getDataRows().size();
    }
    columnCount=ptm.getColumnCount();

    if (columnCount != metaRow.size()){
      exportHidden=PopupBasics.ask("Only " + columnCount + " of " +
                                   metaRow.size() +
                                   " columns are showing, export hidden columns too?");
      if (exportHidden){
        columnCount=metaRow.size();        
      }
    }
    retainColumnNamesAndOrder=PopupBasics.ask(Basics.toHtmlUncentered("Do you wish to export with prefered column labels <br> " +
    		"and in the same column order as it appear? <br><br><b> Note: </b>For a successful re-import it is recommended<br>not to export with prefered column lables."));
    this.file=file;
    bpb=new BasicProgressBar(rowCnt, "Exporting to " + file.getAbsolutePath(), null, null);
    bpb.setRunButton("Export " + rowCnt + " rows, " + columnCount + " columns",
                     new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        bpb.runButton.setEnabled(false);
        exportAll(true);
        bpb.report(Condition.OPTIMAL.annotate(rowCnt + " rows exported"));
      }
    });
    bpb.run();
  }

  void execute(final PersonalizableTableModel tm, final File exportFile, ArrayList<Row> exportRows){
	  	rows = exportRows;
	  	file = exportFile;
	  	ptm = tm;
	    rowCnt = rows.size();
	    metaRow = tm.dataSource.getMetaRow();
	    columnCount = ptm.getColumnCount();
        exportHidden = true;
        retainColumnNamesAndOrder = false;
        columnCount = metaRow.size();
	    exportAll(false);
  }

  public void defineSchemaForExportedRelatives(final Object relativeKey,
                                               final Object[] labels){
    schemasForRelatives.put(relativeKey, labels);
  }

  public void exportRelative(final Object relativeKey, final Object[] values){
    ArrayList al=(ArrayList) valuesForRelatives.get(relativeKey);
    if (al == null){
      al=new ArrayList();
      valuesForRelatives.put(relativeKey, al);
    }
    al.add(values);
  }

  private ArrayList<Integer> getColumnIndices() {
	ArrayList<Integer> columns = new ArrayList<Integer>();
    for (int i=0; i < columnCount; i++){
    	columns.add(i);
    }
    return columns;
  }
  
  private void exportAll(boolean updateProgess){
    stringConverters=new StringConverter[columnCount];
    ArrayList<Integer> columns = getColumnIndices();
    
    for (int i=0; i < columnCount; i++){
      final Class cl;      
	  if (retainColumnNamesAndOrder) {
		  if (i < ptm.getColumnCount()) {
	    	  cl=ptm.getColumnClass(i);	
	    	  columns.remove((Integer)ptm.getDataColumnIndex(i));
	    	}
	    	else {
	    	  cl=metaRow.getClass(columns.get(0));
	    	  columns.remove(0);
	    	}	  
	  }
	  else if (exportHidden){
		  cl=metaRow.getClass(i);
	  }
      else{
        cl=ptm.getColumnClass(i);
      }
      stringConverters[i]=(StringConverter) DefaultStringConverters.get(cl);
    }
    bw=null;
    try{
      bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
      ptm.dataSource.startExport(this);
      columns = getColumnIndices();
      for (int i=0; i < columnCount; i++){
    	  int z = i;
    	  if (retainColumnNamesAndOrder) {
    		  if (i < ptm.getColumnCount()) {
    	    	  z=ptm.getDataColumnIndexFromVisualIndex(i);	
    	    	  columns.remove((Integer)ptm.getDataColumnIndex(i));
    	    	}
    	    	else {
    	    	  z=columns.get(0);
    	    	  columns.remove(0);
    	    	}	  
    	  }
    	  if (retainColumnNamesAndOrder) {
    		 String label= Basics.stripBodyHtml(ptm.getColumnLabel(exportHidden?z:ptm.getDataColumnIndexFromVisualIndex(i)));
    		 String[] splitLabel = label.split(" ");
    		 label="";
    		 for (String labelPart : splitLabel) {
    			 if(labelPart.length()>0)
    				 label=label+labelPart.trim()+" ";
			}
    		 label=label.trim();
    		 bw.write(label);    		  
    	  }else {
    		 String label= ptm.getDataColumnIdentifier(exportHidden?i:ptm.getDataColumnIndexFromVisualIndex(i));
     		 String[] splitLabel = label.split(" ");
     		label="";
     		 for (String labelPart : splitLabel) {
     			 if(labelPart.length()>0)
     				 label=label+labelPart.trim()+" ";
 			}
     		 label=label.trim();
    		 bw.write(label);
    	}
        bw.write('\t');
      }
      bw.write(Basics.lineFeed);
      int rowIndex=0;
      for (final Iterator it=rows.iterator();it.hasNext();){
        ptm.dataSource.export(this, (Row)it.next());
        if (updateProgess) {
        	bpb.setValue(rowIndex + 1);        	
        }
        rowIndex++;
      }
      int relatives=0;
      for (final Iterator it=schemasForRelatives.keySet().iterator();
           it.hasNext(); ){
        final String key=(String) it.next();
        ArrayList rows=(ArrayList) valuesForRelatives.get(key);
        relatives+=rows == null ? 0 : rows.size();
      }
      if (relatives > 0){
        int relative=0;
        if (updateProgess) {
        	bpb.resetMax(relatives, "Exporting " + relatives + " relatives");        	
        }
        for (final Iterator it=schemasForRelatives.keySet().iterator();
             it.hasNext(); ){
          bw.write(Basics.lineFeed);
          final Object key=it.next();
          bw.write(SCHEMA_KEY + key.toString());
          bw.write(Basics.lineFeed);
          export((Object[]) schemasForRelatives.get(key));
          ArrayList al=(ArrayList) valuesForRelatives.get(key);
          if (al != null){
            for (final Iterator it2=al.iterator(); it2.hasNext(); ){
              export((Object[]) it2.next());
              if (updateProgess) {
            	  bpb.setValue(++relative);            	  
              }
            }
          }
        }
      }
    }
    catch (IOException e){
      System.err.println(e);
    }
    finally{
      try{
    	  ptm.dataSource.endExport(this);  
        if (bw != null){
          bw.close();
        }
      }
      catch (IOException ioe){
        Pel.log.print(ioe);
      }
    }

  }

  void export(final Object[] objects){
    try{
      for (int i=0; i < objects.length; i++){
        if (objects[i] != null){
          bw.write(objects[i].toString());
        }
        bw.write('\t');
      }
      bw.write(Basics.lineFeed);
    }
    catch (IOException ioe){
      Pel.log.print(ioe);
    }
  }

  public void export(final Row row){
    try{
      ArrayList<Integer> columns = getColumnIndices();
      for (int column=0; column < columnCount; column++){
    	  
    	int z = column;
  	    if (retainColumnNamesAndOrder) {
  		  if (column < ptm.getColumnCount()) {
  	    	  z=ptm.getDataColumnIndex(column);	
  	    	  columns.remove((Integer)ptm.getDataColumnIndex(column));
  	    	}
  	    	else {
  	    	  z=columns.get(0);
  	    	  columns.remove(0);
  	    	}	  
  	    }
  	    else {
  		  z=column;
  	    }
        final int dataColumnIndex=exportHidden ? retainColumnNamesAndOrder? z: column:
            ptm.getDataColumnIndex(column);
        final Object o=row.get(dataColumnIndex);
        if (o != null){
          final String s;
          if (stringConverters[column] == null){
            s=o.toString();
          }
          else{
            s=stringConverters[column].toString(o);
          }
          bw.write(s);
        }
        bw.write('\t');
      }
      bw.write(Basics.lineFeed);
    }
    catch (IOException ioe){
      Pel.log.print(ioe);
    }
  }

  public static String execute(final PersonalizableTableModel tm,
                             final Window containingWnd){
    final String fileName=TabImporter.getFileName(false, containingWnd);
    return execute( tm, fileName );
  }
  
  public static String execute( final PersonalizableTableModel tm,
                              final Window containingWnd,
                              final String extension,
                              final String typeDescription ){
    final String fileName = TabImporter.getFileName( extension, typeDescription, false, containingWnd );
    return execute( tm, fileName );
  }

  private static String execute( final PersonalizableTableModel tm,
                               final String fileName ){
    if (!Basics.isEmpty(fileName)){
      final TabExporter te=new TabExporter();
      te.execute(tm, new File(fileName));
    }
    return fileName;
  }
  
  public static String execute( final PersonalizableTableModel tm,
          final String fileName, ArrayList<Row> rows){
		if (!Basics.isEmpty(fileName)){
		final TabExporter te=new TabExporter();
		te.execute(tm, new File(fileName), rows);
		}
		return fileName;
}

  static final String SCHEMA_KEY="//";
}
