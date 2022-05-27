package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.MeehanMetaSpace.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

final public class TabImporter
	implements NonVisualUpdater {

  final public class Schema {
	public final String key;
	private final String[] labels;
	public final int size;
	private int dataColumnIndexes[];
	private final StringConverter stringConverters[];

	public void setStringConverter(int idx, StringConverter stringConverter) {
	  stringConverters[idx] = stringConverter;
	}

	public final String getLabel(int idx) {
	  return labels[idx];
	}

	public boolean hasDataColumnsMapped() {
	  return!Basics.isEmpty(this.dataColumnIndexes);
	}

	public int getIndex(String label) {
	  for (int i = 0; i < labels.length; i++) {
		if (labels[i].equals(label)) {
		  return i;
		}
	  }
	  return -1;
	}

	final static String SUFFIX_SECTION = " section";

	private Schema(final String key, final String[] labels) {
	  String s = key.trim();
	  if (s.endsWith(SUFFIX_SECTION)) {
		this.key = s.substring(0, s.length() - SUFFIX_SECTION.length());
	  }
	  else {
		this.key = s;
	  }
	  this.labels = Basics.rTrim(labels, 0);
	  size = this.labels.length;
	  this.stringConverters = new StringConverter[size];
	}

	public synchronized void resolveLabels() {
	  if (dataColumnIndexes != null) {
		throw new UnsupportedOperationException(
			"TabImporter.Schema.dataColumnIndexes can not be set twice");
	  }
	  this.dataColumnIndexes = new int[size];
	  for (int i = 0; i < labels.length; i++) {
		dataColumnIndexes[i] = ptm.getDataColumnIndexForLabel(labels[i]);
		if (dataColumnIndexes[i] >= 0) {
		  final Class cl = ptm.metaRow.getClass(dataColumnIndexes[i]);
		  stringConverters[i] = (StringConverter) DefaultStringConverters.get(
			  cl);
		}
		else {
		  if (!Basics.isEmpty(labels[i])) {
			reportCondition(Condition.WARNING, labels[i],
							"Label can not be resolved");
		  }
		  else {
			reportCondition(Condition.WARNING, labels[i], "Label is null?");
		  }
		}
	  }
	}
  }

  private static class AnomalyMetaRow
	  extends ConditionMetaRow {

	public String getKey() {
	  return "Tab import anomalies";
	}

	private static final Column COLUMN_ROW = new Column("Row (line #)", Integer.class) {
	  public Object get(Object rowObject) {
		if ( (rowObject instanceof Anomaly)){
		  final Anomaly anomaly = (Anomaly) rowObject;
		  return anomaly.line;
		}
		final Condition.Annotated ca=(Condition.Annotated)rowObject;
		return ca.annotation;
	  }
	}

	,COLUMN_COLUMN = new Column("Column (letter/#)", String.class) {
	  public Object get(Object rowObject) {
		if ( (rowObject instanceof Anomaly)){
		  final Anomaly anomaly = (Anomaly) rowObject;
		  if (anomaly.field != null) {
			  int num = anomaly.field.intValue();
			  return num < 0 ? "" :
				  anomaly.label + " (" + Basics.toExcelColumnLabel(num) + "/" +
				  (num + 1) + ")";  
		  }
		  
		}
		return null;
	  }
	}

	,COLUMN_VALUE = new Column("Value", String.class) {
	  public Object get(Object rowObject) {
		if ( (rowObject instanceof Anomaly)){

		  final Anomaly anomaly = (Anomaly) rowObject;
		  return anomaly.value;
		}
		return null;
	  }
	};

	AnomalyMetaRow() {
	  super(new Column[] {COLUMN_ROW, COLUMN_COLUMN, COLUMN_VALUE});
	}

  }

  final private class Anomaly
	  extends Condition.Annotated {
	final Integer line, field;
	final String label, value;

	private Anomaly(Condition condition, int line, int column, String value,
					String annotation) {
	  super(condition, annotation);

	  this.line = new Integer(line);
	  this.field = new Integer(column);
	  this.label = column < 0 ? "null" : schema.labels[column];
	  this.value = value;
	  bpb.report(this);
	}
	
	private Anomaly(Condition condition, String annotation) {
		super(condition, annotation);
		this.line = null;
		this.field = null;
		this.label = null;
		this.value = null;
		bpb.report(this);
	}

  }

  private int getRelevantFieldNumber() {
	if (currentFieldNumber >= 0 && currentFieldNumber < schema.size) {
	  return currentFieldNumber;
	}
	else if (fieldFocus >= 0 && fieldFocus < schema.size) {
	  return fieldFocus;
	}
	return -1;
  }

  public void reportCondition(Condition condition, String value, String msg) {
	anomalies.add(new Anomaly(condition, currentLineNumber,
							  getRelevantFieldNumber(), value, msg));
  }

  public void reportCondition(final Condition condition, final int fieldNum,
							  final String msg) {
	final String value = fieldNum >= 0 && schema.dataColumnIndexes != null ?
		getColumnRawValue(schema.dataColumnIndexes[fieldNum]) : null;
	anomalies.add(new Anomaly(condition, currentLineNumber + 1, fieldNum, value,
							  msg));
  }
  
  public void reportGenericCondition(final Condition condition, final String msg) {	
	anomalies.add(new Anomaly(condition, msg));
  }	

  public void reportCondition(final Condition condition, final String msg) {
	reportCondition(condition, getRelevantFieldNumber(), msg);
  }

  public int getAnomalyCnt() {
	return anomalies.size();
  }

  public int getAnomalyCntWorseThan(final Condition bestPossibleCondition) {
	int cnt = 0;
	for (final Anomaly a:anomalies) {
	  if (bestPossibleCondition.compareTo(a.condition) < 0) {
		cnt++;
	  }
	}
	return cnt;
  }

  private int fieldFocus = -1;

  public void setFieldFocus(int dataColumnIndex) {
	fieldFocus = getInternalIndex(dataColumnIndex);
  }

  public void setFieldFocusIdx(int idx) {
	fieldFocus = idx;
  }

  private final ArrayList lines;
  private final PersonalizableTableModel ptm;
  private final File file;
  private String[] rawValues;
  private Object[] convertedValues;
  private Schema schema;
  public Schema getSchema() {
	return schema;
  }

  private final ArrayList<Anomaly> anomalies = new ArrayList<Anomaly>();

  public int getImportRowSize() {
	  return lines.size() - currentLineNumber;
  }
  private int getInternalIndex(final int dataColumnIndex) {
	for (int i = 0; i < schema.dataColumnIndexes.length; i++) {
	  if (schema.dataColumnIndexes[i] == dataColumnIndex) {
		return i;
	  }
	}
	return -1;
  }

  public static  String nextTitle;
  public final boolean hasPassedQualityControl;
  public static boolean showProgress = true;
  public static boolean treatCloseAsCancel = false;
  private TabImporter(
	  final PersonalizableTableModel ptm,
	  final ArrayList lines,
	  final File file,
	  final Window containingWnd,
	  final boolean hasPassedQualityControl) {
	  this.hasPassedQualityControl=hasPassedQualityControl;
	this.file = file;	
	this.ptm = ptm;
	this.lines = lines;
	this.filters = null;
	final AnomalyMetaRow amr = new AnomalyMetaRow();
	bpb = new BasicProgressBar(lines.size(),
							   nextTitle==null?"Importing " + file.getAbsolutePath():nextTitle,
							   containingWnd, amr);
	if (treatCloseAsCancel) {
		bpb.getDialog().addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				bpb.cancelledByUser = true;
			}
		});	
	}
	setTableModelAndSchema(amr);
  }
  private TabFilter extendedFilterWithMatchMethod=null;
  private final List<TabFilter> filters;
  private TabImporter(
		  final PersonalizableTableModel ptm,
		  final ArrayList<String> lines,
		  final Collection<File> files,
		  final Window containingWnd,
		  final List<TabFilter> filters,
		  final boolean hasPassedQualityControl) {
	  this.hasPassedQualityControl=hasPassedQualityControl;
	  	this.filters = filters;
	  	if(filters!=null){
	  		for (final TabFilter filter:filters){
	  			if (filter.getFilterColumnId()<0){
	  				assert extendedFilterWithMatchMethod==null:"For efficiency, TabImporter only supports one filter supporting match(Map<String, String> namedValues) is allowed";
	  				extendedFilterWithMatchMethod=filter;
	  			}
	  		}
	  	}
	  	this.file = files.iterator().next();
		this.ptm = ptm;
		this.lines = lines;
		final AnomalyMetaRow amr = new AnomalyMetaRow();
		bpb = new BasicProgressBar(lines.size(),
								   "Importing " + files.size() + " file(s)",
								   containingWnd, amr);
		if (treatCloseAsCancel) {
			bpb.getDialog().addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e){
					bpb.cancelledByUser = true;
				}
			});	
		}
		
		setTableModelAndSchema(amr);
  }
  
  private void setTableModelAndSchema(AnomalyMetaRow amr) {
	  final PersonalizableTableModel tm = bpb.getTableModel();
		tm.setDefaultSort(new SortInfo[] {
						  new SortInfo(tm.getMetaRow(), amr.getConditionDataColumnIndex(), 1, false),
						  new SortInfo(tm.getMetaRow(), amr.getMessageDataColumnIndex(), 2, true),
						  new SortInfo(tm.getMetaRow(), amr.getDataColumnIndex(AnomalyMetaRow.
			COLUMN_ROW), 3, true)
		});
		tm.setDefaultColumnWidth(amr.getMessageDataColumnIndex(), 200);

		currentFieldNumber = 0;
		initSchema();
  }

  private BasicProgressBar bpb;
  private JButton browseButton = new JButton();
  private void setButtons(final boolean beforeRun) {
	if (!beforeRun) {
	  bpb.runButton.setEnabled(false);
	}
	if (getAnomalyCnt() > 0) {
	  browseButton.setText("Browse " + getAnomalyCnt() + " issues");
	  browseButton.setEnabled(true);
	  if (beforeRun) {
		bpb.runButton.setEnabled(getAnomalyCntWorseThan(Condition.WARNING) == 0);
	  }
	}
	else {
	  browseButton.setText("Browse");
	  browseButton.setEnabled(false);
	}
  }

  private final void execute(final boolean _hasPassedQualityControl) {
	setButtons(true);
	browseButton.setEnabled(false);
	browseButton.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
		bpb.showAsHtml();
	  }
	});
	final String txt=Basics.concat("Import ", Basics.encode(getImportRowSize()), " ", ptm.getShortenedKey());
	if(showProgress){
	bpb.setRunButton(txt,
					 new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
		importAll();
	  }
	});
	}else{
		bpb.setRunButtonAndExecute(txt,
				 new ActionListener() {
		 public void actionPerformed(ActionEvent ae) {
			importAll();
		 }
		});
	}
	
	bpb.addButtons(new JButton[] {browseButton});
	bpb.allowUserToCancelWhileRunning = true;
	if(showProgress){
	bpb.run(_hasPassedQualityControl);
	}else{
		bpb.runButton.doClick();
	}
  }

  private int insertCnt = 0;
  public int getInsertCnt(){
	  return insertCnt;
  }
  private boolean insertWasOk;
  private boolean insertPending;
  private void backGroundInsert() {
	if (SwingUtilities.isEventDispatchThread()) {
	  
	  synchronized (this) {
		while (insertPending) {
		  if (bpb.wasCancelledByUser()) {
			insertPending=insertWasOk = false;

		  } else {

					  insertWasOk = insert();
					  bpb.setValue(currentLineNumber++);
					  insertPending = insertWasOk && (currentLineNumber % 25) != 0;
					}
		}
		if (!insertWasOk) {
		  bpb.setValue(lines.size());
		  setButtons(false);
		  bpb.runButton.setEnabled(false);
		  ptm.refreshShowingTable(true);
		}
		notifyAll();
	  }
	}
	else {
	  synchronized (this) {
		insertPending = true;
		while (insertPending) {
			 SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					  backGroundInsert();
					}
			 });
	 
		  try {
			this.wait();				  
		  }
		  catch (InterruptedException ie) {
			Pel.log.warn(ie);
		  }
		}

	  }
	}
  }

  boolean isInsertInProcess = false;
  private void doInsert() {
	if (isInsertInProcess) {		  			
		while (insertPending) {
		  if (bpb.wasCancelledByUser()) {
			insertPending=insertWasOk = false;
		  } else {
			  insertWasOk = insert();
			  bpb.setValue(currentLineNumber++);
			  insertPending = insertWasOk && (currentLineNumber % 25) != 0;
		  }
		}
		if (!insertWasOk) {
			bpb.setValue(lines.size());
		  setButtons(false);
		  bpb.runButton.setEnabled(false);
		  if (importComplete != null){
			  importComplete.go(ptm);
		  }
		  ptm.refreshShowingTable(true);
		}
		isInsertInProcess = false;
	}
	else {
		insertPending = true;
		while (insertPending) {
			if (!isInsertInProcess) {
				if (insertPending) {
				  isInsertInProcess = true;
				  doInsert();
				}
			}				 
		}
	}
  }
  
  private Collection<Row> inserted=new ArrayList<Row>();
  public Collection<Row> getInserted(){
	  return inserted;
  }
  private void importAll() {
	insertWasOk = true;
	while (insertWasOk) {
		if (isEventDispatchThread) {
			doInsert();
		}
		else {
			backGroundInsert();			
		}
	}
  }

  private int currentLineNumber, currentFieldNumber;

  public final int getLineNumber() {
	return currentLineNumber;
  }

  public final int getCurrentFieldNumber() {
	return currentFieldNumber;
  }

  private boolean insert() {
	final Condition result = read();
	boolean ok = result.compareTo(Condition.FATAL) < 0;
	if (ok) {
	  ok = !result.equals(Condition.FINISHED);
	}
	if (ok && result.compareTo(Condition.ERROR) < 0 && result.compareTo(Condition.SKIP) < 0) {
	  // only one row expected, if more than one then the current line gets duplicated
	  ptm.dataSource.importTextLine(this);
	}
	return ok;
  }

  private Map<Integer,String> nullSubstitutes;
  private Map<Integer,Integer> nonBooleanColumnSubstitutes;
  
  public void setNullSubstitutes(final Map<Integer,String> ns) {
	  nullSubstitutes = ns;
  }
  
  public void setNonBooleanColumnSubstitutes(final Map<Integer,Integer> cs) {
	  nonBooleanColumnSubstitutes = cs;
  }
  
  private void importCell(final Row row, final int dataColumnIndex){
	  if (dataColumnIndex >= 0 && row.isEditable(dataColumnIndex)) {
		  if (!Basics.isEmpty(convertedValues[currentFieldNumber])) {
			  if (!Basics.isEmpty(nonBooleanColumnSubstitutes)) {
				  final Integer dci= nonBooleanColumnSubstitutes.get(dataColumnIndex);
				  if (!Basics.isEmpty(dci) && !(rawValues[currentFieldNumber].equalsIgnoreCase("TRUE") || rawValues[currentFieldNumber].equals("FALSE"))) {
					   row.set(dci, rawValues[currentFieldNumber]);
				  }
				  else {
					  row.set(dataColumnIndex, convertedValues[currentFieldNumber]);
				  }
			  }
			  else {
				  row.set(dataColumnIndex, convertedValues[currentFieldNumber]);
			  }
		  }
		  else if (!Basics.isEmpty(nullSubstitutes)) {
			   String subs = nullSubstitutes.get(dataColumnIndex);
			   if (!Basics.isEmpty(subs)) {
				   row.set(dataColumnIndex, subs);
			   }		
			   else {
				   row.set(dataColumnIndex, convertedValues[currentFieldNumber]);
			   }	   
		  }
		  else {
			   row.set(dataColumnIndex, convertedValues[currentFieldNumber]);
		  }
	  }
	}
  
  int[]first;
  public void setFirst(final int []first){
	  this.first=first;
  }
  
  public void importRow(final Row row) {
	  if (!Basics.isEmpty(first)){
		  for (final int dataColumnIndex:first){
			  for (currentFieldNumber = 0;
				 currentFieldNumber < convertedValues.length;
				 currentFieldNumber++) {
			  if (dataColumnIndex == schema.dataColumnIndexes[currentFieldNumber]){
			  importCell(row, dataColumnIndex);
			  }
		  }
	  }
	  }
	fieldFocus = -1;
	for (currentFieldNumber = 0;
		 currentFieldNumber < convertedValues.length;
		 currentFieldNumber++) {
	  final int dataColumnIndex = schema.dataColumnIndexes[currentFieldNumber];
	  if (first == null || !Basics.contains(first, dataColumnIndex)){
	  importCell(row, dataColumnIndex);
	  }
	}
	row.endImport();
	inserted.add(row);
	insertCnt++;
  }

  public Row importRow() {
	return importRow(ptm.create());
  }
/**
 * 
 * @param created
 * @return last row created
 */
  public Row importRow(final Row[] created) {
	  Row row=null;
	if (!Basics.isEmpty(created)) {
	  for (int rowIdx = 0; rowIdx < created.length; rowIdx++) {
		  row=created[rowIdx];
		importRow(row);
	  }
	}
	return row;
  }

  public final Object getConvertedValue(int idx) {
	return convertedValues[idx];
  }

  public final String getRawValue(int idx) {
	return rawValues[idx];
  }

  public final Object getColumnConvertedValue(int dataColumnIndex) {
	for (int i = 0; i < schema.dataColumnIndexes.length; i++) {
	  if (schema.dataColumnIndexes[i] == dataColumnIndex) {
		return convertedValues[i];
	  }
	}
	return null;
  }

  public final String getColumnRawValue(int dataColumnIndex) {
	for (int i = 0; i < schema.dataColumnIndexes.length; i++) {
	  if (schema.dataColumnIndexes[i] == dataColumnIndex) {
		return rawValues == null ? "" : rawValues[i];
	  }
	}
	return null;
  }

  
  private Condition read() {
	Condition result = Condition.ERROR;
	currentFieldNumber = -1;
	if (currentLineNumber >= lines.size()) {
	  result = Condition.FINISHED;
	}
	else {
	  String s = (String) lines.get(currentLineNumber);
	  while (Basics.isEmpty(s)) {
		if (!initSchema()) {
		  return result = Condition.FINISHED;
		}
		s = (String) lines.get(currentLineNumber);
	  }
	  if (!Basics.isEmpty(s)) {		
		rawValues = Basics.split(s, "\t");
		if (rawValues.length > schema.size) {
		  rawValues = Basics.rTrim(rawValues, schema.size);
		}
		if (rawValues.length < schema.size) {
		  rawValues = Basics.pad(rawValues, schema.size);
		}
		if (rawValues.length == schema.size) {
		  result = Condition.NORMAL;
		  final HashMap<String,String>nv=extendedFilterWithMatchMethod==null?null:new HashMap<String, String>();
		  for (currentFieldNumber = 0; currentFieldNumber < schema.size;
			   currentFieldNumber++) {
			convertedValues[currentFieldNumber] = null;
			if (rawValues[currentFieldNumber].length() > 0) {
                // Microsoft excel tab bug
                String value=rawValues[currentFieldNumber].trim();
                if (extendedFilterWithMatchMethod != null){
                	nv.put(schema.labels[currentFieldNumber], value);
                }else if (filters!=null){
                	for (TabFilter filter: filters) {
                		final int id=filter.getFilterColumnId() ;
                			if (currentFieldNumber == id && !filter.contains(value)) {
                        		return result = Condition.SKIP;
                        	}
                		
                	}
                }
                
                if (value.endsWith("\"") && value.startsWith("\"")){
                    value=value.substring(1, value.length()-1);
                } else {
                    value=rawValues[currentFieldNumber];
                }
			  if (schema.stringConverters[currentFieldNumber] == null) {
				convertedValues[currentFieldNumber] = value;
			  }
			  else {
				try {
				  convertedValues[currentFieldNumber] = schema.stringConverters[
					  currentFieldNumber].toObject(value);
				}
				catch (Exception e) {
				  reportCondition(Condition.ERROR, e.getMessage());
				  result = Condition.ERROR;
				}
			  }
			}
		  }
		  if (extendedFilterWithMatchMethod!=null){
		  	if (!extendedFilterWithMatchMethod.matches(nv)){
      				return Condition.SKIP;
      		}
		  	if (extendedFilterWithMatchMethod.isFinished()){
		  		return Condition.FINISHED;
		  	}
		  	
		 }
		}
		else {
		  reportCondition(Condition.ERROR,
						  schema.size + " fields expected, " + rawValues.length +
						  " found.");
		}
	  }
	}
	return result;
  }

  private boolean initSchema() {
	if (lines.size() > 0) {
	  String text = null, key = "";
	  for (; currentLineNumber < lines.size() &&
		   (Basics.isEmpty(text) || text.startsWith(TabExporter.SCHEMA_KEY));
		   currentLineNumber++) {
		text = (String) lines.get(currentLineNumber);
		if (text.startsWith(TabExporter.SCHEMA_KEY)) {
		  key = text.substring(2);
		}
	  }
	  if (currentLineNumber < lines.size()) {
		//currentLineNumber++;
		if (!Basics.isEmpty(text)) {
			int c=0;
			while (!Character.isLetter(text.charAt(c))){
				c++;
			}
			text=text.substring(c);
		  final String[] untrimmed= Basics.split(text, "\t");
		  final String[] labels =Basics.trim(untrimmed);
		  if (!Basics.isEmpty(labels)) {
			schema = new Schema(key, labels);
			if (Basics.isEmpty(key) ||
				!ptm.dataSource.defineSchemaForImportedRelatives(this)) {
			  schema.resolveLabels();
			}
			convertedValues = new Object[schema.size];
		  }
		}
		return true;
	  }
	}
	return false;
  }

  public static com.MeehanMetaSpace.SoftwareProduct softwareProduct = null;

  public static String getFileName( final boolean importing,
                                    final Window containingWnd) {
    return getFileName( "txt", "Tab delimited text file", importing, containingWnd );
  }

  public static String getFileName( String extension, final String typeDescription,
                                final boolean importing, final Window containingWnd) {
    final String dir = softwareProduct == null ? "" :
        softwareProduct.
        getDocumentsFolder()
		+ softwareProduct.removeTradeMarkAndRightsReserved(softwareProduct.getProductName()) + File.separatorChar
		+ "Data";
    IoBasics.mkDirs(dir);
    if (importing) {
      return PopupBasics.getFileName(
             extension, typeDescription, "Import",
             "Import the " + typeDescription, true, dir,
             null, containingWnd);
    }
    return PopupBasics.getFileName(
      extension, typeDescription, "Export",
      "Export the " + typeDescription, false, dir,
      null, containingWnd);
  }

  public static TabImporter run(final PersonalizableTableModel ptm,
						final Window containingWnd) {
	final String fileName = getFileName(true, containingWnd);
	if (!Basics.isEmpty(fileName)) {
	  final File file = new File(fileName);
	  return run(file, ptm, containingWnd, false);
	}
	return null;
  }

  public static TabImporter run(final String fileName, final PersonalizableTableModel ptm,
				 final Window containingWnd, final boolean _hasPassedQualityControl) {
	if (!Basics.isEmpty(fileName)) {
	  File file = new File(fileName);
	  return run(file, ptm, containingWnd, _hasPassedQualityControl);
	}
	return null;
  }
  
  public static TabImporter run(final String fileName, final PersonalizableTableModel ptm,
			 final Window containingWnd, final boolean _hasPassedQualityControl, 
			 final Map<Integer,String> nullSubstitutes,final Map<Integer,Integer> columnSubstitutes) {
	  	if (!Basics.isEmpty(fileName)) {
	  		File file = new File(fileName);
	  		return run(file, ptm, containingWnd, _hasPassedQualityControl, nullSubstitutes, columnSubstitutes);
	  	}
	  	return null;
  }
    
  private static boolean isEventDispatchThread = false;
  public static void setEventDispatchThread(boolean newValue) {
	  isEventDispatchThread = newValue;
  }
  public static int runFiles(
		  final Collection<File> files,
		  final PersonalizableTableModel ptm,
		  final Window containingWnd,
		  final boolean autoImport,
		  final List<TabFilter> filters) {
		Reader fr = null;
		boolean metaRead = false;
		try {
			ArrayList<String> allLines = new ArrayList<String>();			
			for (final File file: files) {
				if (TabBrowser.progressShower!=null){
					TabBrowser.progressShower.increment("Finishing download..");
				}
				fr = new InputStreamReader(new FileInputStream(file), "UTF-8");
				List<String> lines = IoBasics.readTextLinesAndClose(new BufferedReader(fr), false);
				if (metaRead) {
					lines = lines.subList(1, lines.size());
				}
				else {
					metaRead = true;				
				}
				allLines.addAll(lines);
			}
		  final TabImporter ti = new TabImporter(ptm, allLines, files, containingWnd, filters, false);
		  if (ptm.dataSource.startImport(ti)) {
			ptm.currentlyActiveTabImporter = ti;
			if (autoImport){
				ti.importAll();
			} else{
				ti.execute(false);
			}
			ptm.currentlyActiveTabImporter = null;
			if (SwingUtilities.isEventDispatchThread()){
		        SwingUtilities.invokeLater( new Runnable(){
		          public void run(){
		            ptm.repaint();
		          }
		        });
			} else {
				ptm.repaint();
			}
		  }
		  else {
			PopupBasics.alert("Tab import was not run", true);
		  }
		  ptm.dataSource.endImport(ti);
		  return ti.insertCnt;
		}
		catch (final IOException e) {
		  PopupBasics.alert(e.toString(), true);
		  Pel.log.print(e);
		}
		finally {
		 IoBasics.closeWithoutThrowingUp(fr);
		}
		return 0;
  }

  public static TabImporter run(
	  final File file,
	  final PersonalizableTableModel ptm,
	  final Window containingWnd,
	  final boolean _hasPassedQualityControl) {
	Reader fr = null;
	try {
	  fr = new InputStreamReader(new FileInputStream(file), "UTF-8");
	  final ArrayList lines = IoBasics.readTextLinesAndClose(new BufferedReader(fr), false);
	  final TabImporter ti = new TabImporter(ptm, lines, file, containingWnd, _hasPassedQualityControl);	  
	  if (ptm.dataSource.startImport(ti)) {
		ptm.currentlyActiveTabImporter = ti;
		ti.execute(_hasPassedQualityControl);			
		ptm.currentlyActiveTabImporter = null;
        SwingUtilities.invokeLater( new Runnable(){
          public void run(){
            ptm.repaint();
          }
        });
	  }
	  else {
		PopupBasics.alert("Tab import was not run", true);
		ti.insertCnt = -1;
	  }
	  ptm.dataSource.endImport(ti);
	  return ti;
	}
	catch (final IOException e) {
	  PopupBasics.alert(e.toString(), true);
	  Pel.log.print(e);
	}
	finally {
	 IoBasics.closeWithoutThrowingUp(fr);
	}
	return null;
  }
  
  public static TabImporter run(
		  final File file,
		  final PersonalizableTableModel ptm,
		  final Window containingWnd,
		  final boolean _hasPassedQualityControl,
		  final Map<Integer,String> nullSubstitutes,
		  final Map<Integer,Integer> columnSubstitutes) {
		Reader fr = null;
		try {
		  fr = new InputStreamReader(new FileInputStream(file), "UTF-8");
		  final ArrayList lines = IoBasics.readTextLinesAndClose(new BufferedReader(fr), false);
		  final TabImporter ti = new TabImporter(ptm, lines, file, containingWnd, _hasPassedQualityControl);
		  ti.setNullSubstitutes(nullSubstitutes);
		  ti.setNonBooleanColumnSubstitutes(columnSubstitutes);
		  if (ptm.dataSource.startImport(ti)) {
			ptm.currentlyActiveTabImporter = ti;
			ti.execute(_hasPassedQualityControl);			
			ptm.currentlyActiveTabImporter = null;
	        SwingUtilities.invokeLater( new Runnable(){
	          public void run(){
	            ptm.repaint();
	          }
	        });
		  }
		  else {
			PopupBasics.alert("Tab import was not run", true);
			ti.insertCnt = -1;
		  }
		  ptm.dataSource.endImport(ti);
		  return ti;
		}
		catch (final IOException e) {
		  PopupBasics.alert(e.toString(), true);
		  Pel.log.print(e);
		}
		finally {
		 IoBasics.closeWithoutThrowingUp(fr);
		}
		return null;
	  }
 
  public interface ImportComplete{
	  void go(PersonalizableTableModel ptm);
  }

  public static ImportComplete importComplete;
}