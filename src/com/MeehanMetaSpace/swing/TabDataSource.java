package com.MeehanMetaSpace.swing;



import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.Pel;

/**
 * This data source processes a number of tab delimited files. When used for
 * files conforming to the reagent import format this mechanism module allows
 * further filtering (using the ActiveFilter functions) in case the invoking
 * context is the stain set form with specific reagents needed
 * 
 * In the first implementation all column types will be string
 * 
 * @author govarthananm
 * 
 */
public class TabDataSource extends DefaultPersonalizableDataSource {


	private final List<Integer> urlColumnIndices = new ArrayList<Integer>();
	private final List<Integer> integerColumnIndices = new ArrayList<Integer>();
	
	private List<String> readMetaInfo(final List<TabFilter> filters, final List<String> urlColumnNames, final List<String> integerColumnNames) {
		ArrayList<String> columns = new ArrayList<String>();
		String fileNames= "";
		final List<String> noLinesFound=new ArrayList<String>();
		InputStreamReader fr = null;
		try {
			for (final File tabFile: tabFiles) {
				if (!fileNames.equals("")) {
					fileNames += ",";
				}
				fileNames += tabFile.getAbsolutePath();
				fr = new InputStreamReader(new FileInputStream(tabFile), "UTF-8");
				final String s= IoBasics.readFirstLineAndClose(
						new BufferedReader(fr), false);
				if (s!=null) {
					ArrayList<String> thisColumns = (ArrayList<String>) IoBasics.readFieldLabels(s);
					for (String columnName: thisColumns) {
						if (!columns.contains(columnName)) {
							columns.add(columnName);
						}
					}
				}
				fr.close();
				if (TabBrowser.progressShower!=null){
					TabBrowser.progressShower.increment("Opening "+tabFile.getName());
				}
			}
		
			int columnIndex = 0;
			if (!columns.isEmpty()) {
				if (!Basics.isEmpty(urlColumnNames)) {
					for (final String key : columns) {
						if (urlColumnNames.contains(key)) {
							urlColumnIndices.add(columnIndex);
						}
						columnIndex++;
					}
					columnIndex = 0;	
				}
				if (!Basics.isEmpty(integerColumnNames)) {
					for (final String key : columns) {
						if (integerColumnNames.contains(key)) {
							integerColumnIndices.add(columnIndex);
						}
						columnIndex++;
					}
					columnIndex = 0;	
				}
				
				if (!Basics.isEmpty(filters)) {					
					for (TabFilter filter: filters) {
						columnIndex = 0;
						for (final String key : columns) {
							if (key.equalsIgnoreCase(filter.getFilterColumnName())) {
								filter.setFilterColumnId(columnIndex);
								break;
							}
							columnIndex++;						
						}						
					}
				}
				return columns;
			}
			final String fileNotFound=fileNames+"\t"+"not found...";
			
			noLinesFound.add(fileNotFound);
			return noLinesFound;

		} catch (final IOException e) {
			PopupBasics.alert(e.toString(), true);
			Pel.log.print(e);
		} 
		finally {
			IoBasics.closeWithoutThrowingUp(fr);
		}
		return noLinesFound;
	}

	
	// this constructor works with 1 or more tab delimited text files
	// the meta data in the first line of each file is merged to accommodate all
	// files
	private final Collection<File> tabFiles;
	final List<String> columns;
	
	public File getTabDataSourceFile() {
		return IoBasics.getTabDataSourceFile(TabBrowser.progressShower, tabFiles, columns, false,null);
	}

	public static File getFile(
			final Collection<TabListRow> tabRows,
			final List<String> columns) {

		File file = null;
		BufferedWriter writer = null;
		InputStreamReader fr = null;
		try {
			file = File.createTempFile("catalog","temp");
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));

			// Write the header
			final StringBuilder sb = new StringBuilder("");
			for (final String value : columns) {
				sb.append(value);
				sb.append("\t");
			}
			sb.append("\n");
			writer.write(sb.toString());
			sb.setLength(0);
			for (final TabListRow row: tabRows) {
				final List<String>l=row.l;
				final int n=row.l.size();
				for (int i=0;i<n;i++){
					final String value=l.get(i);
					sb.append(value);
					sb.append("\t");
				}
				sb.append("\n");
				writer.write(sb.toString());
				sb.setLength(0);
			}
		} catch (final Exception e) {
			Pel.log.print(e);
		} finally {
			IoBasics.closeWithoutThrowingUp(fr);
			IoBasics.closeWithoutThrowingUp(writer);
		}
		return file;
	}

	public TabDataSource(final Collection<File> tabFiles, final List<TabFilter> filters, final List<String> urlColumnNames, 
			final List<String> integerColumnNames, final Map<String,Object> additionalColumns) {
		this.tabFiles=tabFiles;
		this.columns = readMetaInfo(filters, urlColumnNames, integerColumnNames);
		this.tabColumnSize=this.columns.size();
		this.additionalColumns = additionalColumns;
		if (additionalColumns != null) {
			Iterator<String> addionalColKey = additionalColumns.keySet().iterator();
			while(addionalColKey.hasNext()) {
				String colName = addionalColKey.next();
				this.columns.add(colName);
			}	
		}
		actualColumnsSize = this.columns.size();
		setMetaRow(new TabMetaRow(this.columns));
		
	}
	
	ArrayList<Class> dataTypes;
	ArrayList<Integer> readOnlyColumns;
	ArrayList<String> allLines = new ArrayList();
	public ArrayList<String> getTableData() {
		return allLines;
	}

	public void setTableData(ArrayList<String> tableData) {
		this.allLines = tableData;
	}

	public TabDataSource(final Collection<File> tabFiles,final ArrayList<Class> dataTypes, 
			final ArrayList<Integer> readOnlyColumns, final TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues) throws Exception {
		this.tabFiles=tabFiles;
		this.dataTypes = dataTypes;
		this.readOnlyColumns = readOnlyColumns;
		this.additionalColumns = null;
		this.columns = readMetaInfo(null, null, null);
		
		for (File tabFile: tabFiles) {
			allLines.addAll(IoBasics.readTextFileLinesExcludingFirst(tabFile));
		}
		final Vector<Row> data=new Vector<Row>();
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		int rowIndex = 0;
		for (String line: allLines) {
			String[] cols = line.split("\t");
			if (cols.length == columns.size()) {
				Vector row=new Vector();
				for (int i=0; i < cols.length; i++) {
					if (dataTypes.get(i) == Date.class) {
						row.add(dateFormat.parse(cols[i]));
					}
					else {
						row.add(cols[i]);
					}
				}
				final int rowIndexFinal = rowIndex;
				final Row r=new ListRow(row){
					public Collection getAllowedValues(int dataColumnIndex){
						if (allowedValues.containsKey(dataColumnIndex)) {
							 TreeMap<Integer, List<Object>> val = allowedValues.get(dataColumnIndex);
							 if (val.containsKey(rowIndexFinal)) {
								 return val.get(rowIndexFinal);
							 }
						}
						return null;
					}
					public boolean isEditable(int dataColumnIndex) {
						if (readOnlyColumns.contains(dataColumnIndex)) {
							return false;
						}
						return true;
					}
					public boolean allowNewValue(int dataColumnIndex){
						if (readOnlyColumns.contains(dataColumnIndex)) {
							return false;
						}
						return true;

					}
					public Object getRenderOnlyValue(final int dataColumnIndex,
							final boolean isSelected, final boolean hasFocus) {
						/*if (dataTypes.get(dataColumnIndex) == Date.class) {
							return dateFormat.format(get(dataColumnIndex));
						}*/
						return super.getRenderOnlyValue(dataColumnIndex, isSelected,hasFocus );
					}
					public void set(int index, Object element)
					{
						String line = allLines.get(rowIndexFinal);
						String a[] = line.split("\t");
						if (dataTypes.get(index) == Date.class) {
							a[index] = dateFormat.format(element);
						}
						else {
							a[index] = element.toString();
						}
						
						StringBuilder builder = new StringBuilder();
						for (int i=0;i<a.length;i++) {
							builder.append(a[i]);
							if (i != a.length-1) {
								builder.append("\t");
							}
						}
						line=builder.toString();
						allLines.remove(rowIndexFinal);
						allLines.add(rowIndexFinal, line);
						super.set(index, element);
					}
					
				};
				data.add(r);
				rowIndex++;
			}
		}
		setMetaRow(new TabMetaRow(this.columns));
		setDataRows(data);
	}
	
	int actualColumnsSize = 0;
	int tabColumnSize = 0;
	final ArrayList<String> computedColumnsReplacementPattern = new ArrayList<String>();
	private Map<String, SortValueReinterpreter > sortValueReinterpreterMap;
	private final Map<String,Object> additionalColumns;
	private Map<String, Row.IconGetter> iconMap;
	private List<ComputedColumn> computedColumns=null;
	public TabDataSource(final Collection<File> tabFiles, final List<TabFilter> filters, 
			final List<ComputedColumn> computedColumns, final List<String> urlColumnNames,final List<String> integerColumnNames,
			final Map<String,Object> additionalColumns,
			final Map<String,SortValueReinterpreter> sortValueReinterpreterMap,
			final Map<String, Row.IconGetter> iconMap) {
		this.tabFiles=tabFiles;
		this.sortValueReinterpreterMap=sortValueReinterpreterMap;
		this.iconMap=iconMap;
		this.columns = readMetaInfo(filters, urlColumnNames, integerColumnNames);
		this.tabColumnSize=this.columns.size();
		this.additionalColumns = additionalColumns;
		if (additionalColumns != null) {
			Iterator<String> addionalColKey = additionalColumns.keySet().iterator();
			while(addionalColKey.hasNext()) {
				String colName = addionalColKey.next();
				this.columns.add(colName);
			}	
		}		
		actualColumnsSize = this.columns.size();
		this.computedColumns=computedColumns;
		for (final ComputedColumn cc:computedColumns) {
			cc.init(columns);
			columns.add(cc.getColumnIdentifier());			
		}
		cache=new Map[computedColumns.size()];
		for (int i=0;i<cache.length;i++){
			cache[i]=new HashMap<TabListRow, Object>();
		}
		setMetaRow(new TabMetaRow(this.columns));
	}
	
	
	ArrayList<String> pickedNames = new ArrayList<String>();

	public String getSelectedNodeDisabledText(javax.swing.tree.TreeNode node,
			Row row) {
		return null;
	}

	Collection<TabBrowser>tbs=new ArrayList<TabBrowser>();
	
	public boolean handleDoubleClick(final TableCellContext context) {
		boolean ok=true;
		if (tbs.size()>0){
			for (final TabBrowser tb:tbs){
				if (!tb.handleDoubleClick()){
					ok=false;
				}
			}
		}
		return ok;
	}

	public String getNodeEnabledText(final javax.swing.tree.TreeNode node,
			final Row row, final int[] sortOrder, final int columnThatDiffers,
			final int uncondensedDataColumnIndex) {
		return null;
	}

	public Collection<String> getNodeDisabledText(final GroupedDataSource.Node node, final Row row, final int[] sortOrder,
			final int columnThatDiffers) {
		return null;
	}

	public Collection<String> getRowDisabledText(final Row row) {
		return getNodeDisabledText(null, row, null, 0);
	}

	public TreePath[] reorderPicks(final TreePath[] tp) {
		return tp;
	}

	public Object createPick(javax.swing.tree.TreeNode node, Row row,
			int columnThatDiffers) {
		return row;
	}

	public Object createPick(Row row) {
		return row;
	}

	public void resetPicks() {
		pickedNames = new ArrayList<String>();
	}

	public void tryPicks(java.util.List<Object> picks) {
	}

	public void completePicks(
			final Collection<GroupedDataSource.Node> originalPickNodes,
			final Collection<Row> orignalRowPicks,
			final Collection<Row> rejectedRows) {
	}

	NonVisualUpdater importer;

	public boolean startImport(final TabImporter tabImporter) {
		this.importer = tabImporter;
		return true;
	}

	public void endImport(final TabImporter tabImporter) {
		importer = null;
	}

	void focusImport(int dataColumnIndex) {
		if (importer != null) {
			importer.setFieldFocus(dataColumnIndex);
		}
	}

	void importLot(final TabImporter ti) {
		//TODO 0 the key column?
		focusImport(0);
		ti.importRow();

	}

	public void importTextLine(final TabImporter ti) {
		importLot(ti);
	}

	private final Row createBlankListRow() {
		final ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; i < metaRow.size(); i++) {
			al.add(null);
		}
		return new TabListRow(al);
	}

	public boolean isCreatable(){
		return false;
	}

	public boolean isEditable(){
		return false;
	}

	public boolean isRemovable(){
		  return false;	
	}

	public boolean isDeletable(){
		return false;
	}
	
	public boolean isSaveable(){
		return false;
	}

	public Row[] create(final PersonalizableTableModel modelShowing) {
		Row[] created = null;
		if (importer != null) {
			created = new Row[1];
			created[0] = createBlankListRow();
			add(created[0]);
		}
		return created;
	}

	public void adjustActiveFilters(final Row row) {
		// TODO
	}
	
	class TabMetaRow extends AbstractMetaRow {
	    public Icon getIcon(
	    	      final Iterator selectedRows,
	    	      final int dataColumnIndex,
	    	      final boolean isExpanded,
	    	      final boolean isLeaf) {

	    	final String columnIdentifier=dataColumnIdentifiers.get(dataColumnIndex);
	    	if (iconMap != null && iconMap.containsKey(columnIdentifier)){
	    		return iconMap.get(columnIdentifier).get(selectedRows, dataColumnIndex);
	    	}
	        return null;
	       }

	    public Icon getIcon(final int dataColumnIndex) {
	    	return null;
	    }

	    public SortValueReinterpreter getSortValueReinterpreter(final int dataColumnIndex) {
	    	final String key=dataColumnIdentifiers.get(dataColumnIndex);
	    	if (sortValueReinterpreterMap != null && sortValueReinterpreterMap.containsKey(key)){
	    		return sortValueReinterpreterMap.get(key);
	    	}
	        return null;
	    }

		TabMetaRow(final Collection <String> columnHeaders) {
			super(null);
			for (final String columnKey : columnHeaders) {
				dataColumnIdentifiers.add(columnKey.trim());
			}
		}

		public Class getClass(final int index) {
			if (Basics.isEmpty(dataTypes)) {
				if (urlColumnIndices.contains(index)) {
					return URL.class;	
				}

				if (integerColumnIndices.contains(index)) {
					return Integer.class;	
				}
				return String.class;
			}
			else {
				return dataTypes.get(index);
			}
		}

	}

	class TabListRow extends ListRow {
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof TabListRow)) {
				return false;
			}
			return Basics.equals(l, ((TabListRow) o).l);
		}

		public int hashCode() {
			int result = 17;
			for (final Object obj : l) {
				result = 37 * result + (obj==null ? 14:obj.hashCode());
			}
			return result;
		}

		public TabListRow(java.util.List l)
		{
			super(l);			
		}
		
		public MetaRow getMetaRow(){
			return metaRow;
		}
		public Object get(final int dataColumnIndex)
		{
			if (dataColumnIndex < actualColumnsSize) {
				Object value = super.get(dataColumnIndex);
				if (value != null && !(value instanceof URL) 
						&& (urlColumnIndices.contains(dataColumnIndex))) {
					try {
						return new URL((String)value);						
					}
					catch(Exception e) {e.printStackTrace();}
				}
				if (value != null && !(value instanceof Integer) 
						&& (integerColumnIndices.contains(dataColumnIndex))) {
					try {
						return Integer.parseInt((String)value);						
					}
					catch(final Exception e) {e.printStackTrace();}
				}
				if (dataColumnIndex >= tabColumnSize && dataColumnIndex < actualColumnsSize) {
					if (!additionalColumns.isEmpty()) {
						return additionalColumns.values().iterator().next();					
					}
				}
				return value;				
			}
			else {
				final int computedColumnsIndex = dataColumnIndex
						- actualColumnsSize;
				Object displayValue = null;
				if (cache != null && cache[computedColumnsIndex] != null) {
					displayValue = cache[computedColumnsIndex].get(this);					
				}
				if (displayValue == null && computedColumns != null) {

					displayValue = TabDataSource.this.computedColumns.get(
							computedColumnsIndex).getValue(this);
					cache[computedColumnsIndex].put(this, displayValue);
				}
				return displayValue;
				
			}
		}
		
		public String toString(){
			return Basics.isEmpty(l.size())?super.toString():Basics.toString(l.get(0));
		}
	}
	
	Map<TabListRow, Object>[]cache;
	   

}
