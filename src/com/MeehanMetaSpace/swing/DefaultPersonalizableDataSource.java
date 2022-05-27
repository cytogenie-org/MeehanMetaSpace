package com.MeehanMetaSpace.swing;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.Counter;
import com.MeehanMetaSpace.StringConverter;
import com.MeehanMetaSpace.swing.AutoComplete.FoundListener;
import com.MeehanMetaSpace.swing.DefaultFilterable.Filter;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class DefaultPersonalizableDataSource
	 implements PersonalizableDataSource, Cloneable, ActiveFilter{

		protected String getDeleteMsg(final int selectedCount, final int count) {
	           return (count == 1 ?
                  (selectedCount>1?"<b>Delete the only deletable row?</b>":"<b>Delete the currently selected row?</b>" ):
                  "<b>Delete " + count + " rows?</b>") +
               getDeleteMessageSuffix();

		}
       public boolean proceedWithDeletions(final PersonalizableTableModel model, final int []selected, final int count){
           final String msg = Basics.startHtml()
                              + getDeleteMsg(selected.length, count)
                              + Basics.endHtml();
           return PopupBasics.ask(SwingUtilities.getWindowAncestor(model.getTearAwayComponent()), msg);

       }


   public String getDeleteMessageSuffix(){
	 return "";
   }

   public void triggerDeletion() {
	   
   }
   
   public boolean isMovable(){
	 return false;
   }

   static void move(final java.util.List<Row> l, final int current, final int direction){
	 final int to;
	 if (direction == -2){
	   to=0;
	 }else if (direction == 2){
	   to=l.size()-1;
	 } else {
	   to=current+direction;
	 }
	 Basics.move(l, current, to);
   }

  public void fireSortOrderChanged(){

  }
   public void move(final int filteredRowIndex, final int direction){
	 final java.util.List<Row> d=getDataRows();
	 final java.util.List<Row> f=getFilteredDataRows();
	 final int dc=getDataRowIndex(filteredRowIndex);

	 move(d, dc, direction);
	 if (d != f){
	   move(f, filteredRowIndex, direction);
	 }

   }

	public int getMinimumCardinality(){
		return 0;
	}

	public int getMaximumCardinality(){
		return Integer.MAX_VALUE;
	}

	public void configure(final PersonalizableTable table){

	}

	/**
	 * Overide if any business prior to import is required.
	 *
	 * @param ti
	 * @return
	 */
	public boolean startImport(final TabImporter ti){
		return true;
	}

	/**
	 * Overide if any business prior to import is required.
	 *
	 * @param ti
	 * @return
	 */
	public void endImport(final TabImporter ti){

	}

	public void importTextLine(final TabImporter ti) {
		ti.importRow();
	}

	public boolean defineSchemaForImportedRelatives(final TabImporter ti) {
		return false;
	}

	public void startExport(final TabExporter te){
	}
	public void endExport(final TabExporter te){
	}

	public void export(final TabExporter te, final Row row){
	  te.export(row);
	}

	public void removeAll(){
		setDataRows(new ArrayList() );
	}


	public boolean editAll(
		final PersonalizableTableModel model,
		final PersonalizableTableModel.MultiRowEditRule rule, final int updatedVisualRowIndex){
	  final boolean ok;
		final int []selectedRowIndexes=model.getSelectedDataRowIndexesInAscendingOrder();

	final String all;
		final int visualRowIndex;
		if (updatedVisualRowIndex != -1) {
			visualRowIndex=updatedVisualRowIndex;
			all=Integer.toString(selectedRowIndexes.length);
		}
		else if (selectedRowIndexes==null||selectedRowIndexes.length<2){
			all="ALL";
			visualRowIndex=model.showFilterUI ? 2 : 0;
		} else {
			all=Integer.toString(selectedRowIndexes.length);
			visualRowIndex=selectedRowIndexes[0];
		}

	  final int[] di=rule == null ?
		  model.getDataColumnIndexesThatAreVisible(model.table) :
		  rule.editTheseColumns;

    final String []identifiers=new String[di.length];
    for (int i=0;i<di.length;i++){
        identifiers[i]=metaRow.getDataColumnIdentifier(di[i]);
    }
    for (int i = 0; i < di.length; i++) {
        if (!model.isHidden(identifiers[i])) {
            model.highlightColumn(di[i]);
        }
    }

    final int[] vi=rule == null ?
		  SwingBasics.getVisualIndexes(model.table) :
		  rule.getEditableVisualIndexes(model);
	  final String applyHeader=
		  rule != null
		  &&
		  (!rule.applyEditsToAllUncondensedChildRows
		   ||
		   rule.editTheseColumns.length == 1) ?
		  null : "Apply to all ?";
	int max=0;
	for (int i=0;i<vi.length;i++){
	  final int dci=model.getDataColumnIndex( SwingBasics.getModelIndexFromVisualIndex(model.table, vi[i]));
	  final int n=model.getWidth(dci);
	  if (n>max){
		max=n;
	  }
	}
	if (max<127){ // ensure header "Change .." gets seen
		max=127;
	}
	final int []colWidth=new int[]{125, max};

	  final RotateTable rt=new RotateTable(
		  model.table,
		  vi,
		  new int[]{visualRowIndex}
		  ,
		  new String[]{Basics.concat("Change ", all, " rows to:")}
		  ,
		  null,
		  applyHeader,
		  colWidth);
	  final Row fromRow=model.getRowAtVisualIndex(visualRowIndex);
	  // find out what changed
	final ArrayList<Integer> al=new ArrayList<Integer>();
	for (int i=0; i < vi.length; i++){
	  if (applyHeader == null || rt.getChecked(i)){
		al.add(new Integer(di[i]));
	  }
	}
	final int []modelIndexes=new int[al.size()];
	final int []dataColumnIndexes=new int[modelIndexes.length];
	int j=0;
	for (int dataColumnIndex:al){
		dataColumnIndexes[j]=dataColumnIndex;
		modelIndexes[j++]=model.getModelColumnIndexFromDataColumnIndex(dataColumnIndex);
	}

	  final Object[] prev=new Object[vi.length];
	  for (int i=0; i < vi.length; i++){
		prev[i]=fromRow.getFilterableValue(dataColumnIndexes[i]);
	  }
	  if (updatedVisualRowIndex == -1) {
		  ok=editFromTreeContext(model,
					 rule == null ? "Editing " : rule.menuItem.getText(),
					 rt, visualRowIndex);
	  }
	  else {
		  ok = true;
	  }
	 
	  if (!ok){
		boolean refresh=false;
		for (int i=0; i < vi.length; i++){
		  final Object o=fromRow.getFilterableValue(dataColumnIndexes[i]);
		  if (!Basics.equals(o, prev[i])){
			fromRow.set(dataColumnIndexes[i], prev[i]);
			refresh=true;
		  }
		}

		if (refresh){
		  model.table.repaint();
		}


	  }
	  else{
		  
		if (!Basics.isEmpty(modelIndexes)){
		  if (rule == null || rule.applyEditsToAllUncondensedChildRows){
			
			if (selectedRowIndexes==null||selectedRowIndexes.length<2){
			int rows=model.getRowCount();
			for (int i=0; i < rows; i++){
			  TableBasics.copy(
				dataColumnIndexes,
				  modelIndexes,
				  model,
				  i,
				  fromRow);
			}
			} else {
				for (final int i:selectedRowIndexes){
					TableBasics.copy(
							dataColumnIndexes,
							  modelIndexes,
							  model,
							  i,
							  fromRow);
				}
			}
		  }
		  model.refreshShowingTable(false);
		}
	  }
	  return ok;
	}
	
	public boolean isPartOfPrimaryKey(final int dc){
		return false;
	}
	
	
	public boolean editFromTreeContext(
			final PersonalizableTableModel ungroupedModel,
			final PersonalizableTableModel.MultiRowEditRule rule, final int updatedVisualRowIndex){
		final boolean ok;
		final GroupedDataSource.Node []selectedNodes=ungroupedModel.getSelectedNodes();
		if (selectedNodes.length>0){
			ok=editAll(ungroupedModel.groupedDataSource.groupSeivedModel, rule, updatedVisualRowIndex);
			ungroupedModel.groupedDataSource.tree.repaint();
		} else {
			ok=false;
		}
		return ok;
	}



	public boolean editFromTreeContext(final PersonalizableTableModel model, final String title, final RotateTable viewOfEditedRow, final int updatedVisualRowIndex){
		return model.popupTable(
                new JLabel(MmsIcons.getEditIcon()),
                null,
                MmsIcons.getEditImage(),
				"editTree",
				title,
				viewOfEditedRow,
				null,
				-1,
				-1,
				null,
			   "Apply value to all underlying rows",
			   null,
               true,
			  "Do not apply this value to all underlying rows",
			 null, false );
	}


	public boolean canEditFromTreeContext(){
		return true;
	}

	public static class Picker extends DefaultPersonalizableDataSource{
		public Picker(List<Row> data) {
			super(data);
		}


		public Picker(final List<Row> data, final MetaRow metaRow) {
			super(data, metaRow);
		}

		public Picker(final MetaRow metaRow) {
			super(metaRow);
		}

		public boolean isFilterable(){
			return false;
		}

		public boolean handleDoubleClick(final TableCellContext context) {
			return false;
		}

		public boolean isRemovable() {
			return false;
		}

		public boolean isAddable() {
			return false;
		}

		public boolean isCreatable() {
			return false;
		}

		public boolean isDeletable() {
			return false;
		}

		public boolean isSaveable() {
			return false;
		}


	}
	/**
	 * Overide if any business after deleting is required.
	 *
	 * @param ti
	 * @return
	 */
	public void finishedDeleting(final int count){
	}

	public void setMultiRowChangeOperationFlag(final boolean ok) {
		
	}

	public void nodeSelectionChanged(){}

	public TableCellEditor getCellEditor(
			final Row row,
			final int dataColumnIndex) {
		if (usedForGroupSeivedTable()){
			return seivedDataSource.getCellEditor(row, dataColumnIndex);
		}
		String s=metaRow.getDateFormat(dataColumnIndex);
		if (s != null){
			return new DateCellEditor(s);
		}
		s=metaRow.getDecimalFormat(dataColumnIndex);
		if (s != null ){
			return new DecimalCellEditor(s, metaRow.getDecimalClass(dataColumnIndex));
		}
		s=metaRow.getEditMask(dataColumnIndex);
		if (s != null ){
			  final FormattedTextField f=Editor.getField(s);
			  return f==null?null:new EditMaskCellEditor(f, s);
		}
		final Collection values=getAllowed(row, dataColumnIndex);
		if ( !Basics.isEmpty(values  )){
		  final boolean allowNew=row.allowNewValue(dataColumnIndex);
		  final boolean isCaseSensitive=isCaseSensitive(row, dataColumnIndex) ;
		  final Collection unselectable=row.getUnselectableValues(dataColumnIndex);
		  final Collection forbidden=row.getForbiddenValues(dataColumnIndex);
		  final StringConverter sc=metaRow.getStringConverter(dataColumnIndex);
		  final Object currentValue=row.get(dataColumnIndex);
		  if (metaRow.getClass(dataColumnIndex) == ComboBoxForAbstractButtons.class) {
			  return ComboBoxForAbstractButtons.getTableCellEditor(values, currentValue);
		  }
		  return PersonalizableTableModel.getComboBoxEditor(values, unselectable, forbidden, true, allowNew, isCaseSensitive, sc, currentValue);
		}
		return null;
	}
	
	public Collection getAllowed(final Row row, final int dataColumnIndex){
        return TableBasics.getAllowedValues(row, dataColumnIndex);
    }


    public boolean isCaseSensitive(final Row row, final int dataColumnIndex){
        return true;
    }
    
	public void handleNewColumn(final TableColumn tc, final int dataColumnIndex){
	}
	public List<Row> getDataRows(){
		return dataRows;
	}
	public boolean isRemovable(){
	  return true ;	
	}

	public boolean isTemporaryForPrimaryKeySelecting(final Row row){
	  return false;	
	}

	public Row remove(final int filteredDataRowIndex) throws UnsupportedOperationException{
		return _delete(filteredDataRowIndex);
	}
	boolean usedForGroupSeivedTable(){
		return seivedDataSource != null;
	}

	public boolean isAddable(){
		return seivedDataSource == null ? true : false;
	}

	public void add() throws UnsupportedOperationException{
			if (!isAddable()){
				throw new UnsupportedOperationException("isAddable() returns FALSE");
			}

	}

	public boolean isSaveable(){
	  return seivedDataSource == null ? true : seivedDataSource.isSaveable();
	}

	public boolean save() throws UnsupportedOperationException{
		if (!isSaveable()){
			throw new UnsupportedOperationException("isSaveable() returns FALSE");
		}

		return false;
	}

	private String propertyPrefix=null;
	public void setPropertyPrefix(final String propertyPrefix){
		this.propertyPrefix=propertyPrefix;
	}

	public final String getPropertyPrefix(){
		return propertyPrefix;
	}

	public void filter(final Filterable filterable,
			final MultiQueryFilter mfilter) {
		final Filter[][] filters = mfilter.compile();
		if (filters != null) {
			final DefaultFilterableEx.Op op = new DefaultFilterableEx.Op(this,
					filters);
			filteredDataRows = op.or(dataRows);			
			applyExternalFilters();
			
		}
	}
	
	
	

	private List<Row> filter(
			final List<Row> data,
			final Row filteringRow,
			final Filterable filterable){
		final List<Row> al= new ArrayList<Row>();
		for (int i=0;i<data.size();i++){
			final Row filteredRow=(Row)data.get(i);
            if (DefaultFilterable.isFiltered(filteredRow, filteringRow, filterable)){
                if (meetsCriteria(filteredRow)) {
                    al.add(filteredRow);
                } 
			}
		}
		return al;
	}

    public final void setFilter(final java.util.List<Row> rows){
      filteredDataRows=rows;	
    }
    	
	private final Row createBlankListRow()	{
		final List<String> al=new ArrayList<String>();
		for (int i=0;i<metaRow.size();i++){
			al.add(null);
		}
		return new ListRow( al );
	}

	protected MetaRow metaRow;

	protected void setMetaRow(final MetaRow metaRow){
		this.metaRow=metaRow;
	}

	protected List<Row> dataRows=new ArrayList<Row>(), filteredDataRows, hiddenDataRows=new ArrayList<Row>();

	public void filterOutAllRows(){
		filteredDataRows=new ArrayList();
	}

	public DefaultPersonalizableDataSource(){
	}

	public DefaultPersonalizableDataSource(final List<Row> data) {
		setDataRows(data);
	}

	PersonalizableDataSource seivedDataSource=null;
	DefaultPersonalizableDataSource(
         final PersonalizableDataSource ds,
         final boolean prePopulate){
		this( new ArrayList( prePopulate ? ds.getFilteredDataRows():Basics.UNMODIFIABLE_EMPTY_LIST), ds.getMetaRow());
		seivedDataSource=ds;
        setActiveFilters(ds.getActiveFilters());
	}

	public DefaultPersonalizableDataSource(final List<Row> data, final MetaRow metaRow) {
		this.metaRow=metaRow;
		setDataRows(data);
	}

	public DefaultPersonalizableDataSource(final MetaRow metaRow) {
		this.metaRow=metaRow;
	}

	public final void copyRowReferences( final Collection<Row> dataRows){
		final ArrayList<Row> newDataRows=new ArrayList();
		for (final Row row:dataRows){
			newDataRows.add( row );
		}
		setDataRows(newDataRows);
	}

	protected final void setDataRows(final List<Row> dataRows)	{
		this.dataRows=dataRows;
		filteredDataRows=dataRows;
	}

	public final MetaRow getMetaRow()
	{
		return this.metaRow;
	}

	/**
	 * Override to create rows in  data source.
	 *
	 * @param modelShowing
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public Row [] create(final PersonalizableTableModel modelShowing) throws UnsupportedOperationException	{
		if ( ! isCreatable()){
			throw new UnsupportedOperationException("isCreatable() returns FALSE (like Darwin did)");
		}
		final Row[] created = new Row[1];
		created[0] = createBlankListRow();
		modelShowing.setFromContext(created[0]);
		add(created[0]);
		modelShowing.addCreationToViewedTable(created[0]);
		return created;
	}

    public boolean canCreateInPlaceWithKeyStroke() {
        return true;
    }

	public boolean resortAfterCreate(){
		return false;
	}

	/**
	 *
	 * @param row
	 * @return filtered data row index
	 */
    public final int add(final Row row) {
        if (dataRows == null || dataRows.isEmpty()) {
            setDataRows(new ArrayList());
        }
        dataRows.add(row);
        if (dataRows != filteredDataRows && filteredDataRows != null) {
            if (activeFilters.size()==0) {
                filteredDataRows.add(row);
            } else if (!filteredDataRows.contains(row) && meetsCriteria(row)) {
                filteredDataRows.add(row);
            }
        }
        return filteredDataRows == null ? dataRows.size() :
          filteredDataRows.size();
    }

    public final boolean refilter(final Row row){
    	boolean ok=false;
    	if (activeFilters.size() > 0 &&
            dataRows != filteredDataRows &&
            filteredDataRows != null &&
          dataRows.contains(row)){
            final boolean alreadyThere=filteredDataRows.contains(row);
            final boolean meetsCriteria=meetsCriteria(row);
            if (alreadyThere && !meetsCriteria) {
                filteredDataRows.remove(row);
                ok=true;
            } else if (!alreadyThere && meetsCriteria){
                filteredDataRows.add(row);
                ok=true;
            }
        }
        return ok;
    }

	public boolean describeCriteriaFailures(final Row row, final Collection<String> criteriaFailureDescriptions){
    	boolean ok=false;
    	if (activeFilters.size() > 0 ){
            ok =!_describeCriteriaFailures(row, criteriaFailureDescriptions);
        }
        return ok;
    }
	
	public boolean describeSelectedCriteriaFailures(final Row row, final boolean isCriteriaFilterGroup, final Collection<String> criteriaFailureDescriptions) {
		boolean ok=false;
    	if (activeFilters.size() > 0 ){
            ok =!_describeSelectedCriteriaFailures(row, isCriteriaFilterGroup, criteriaFailureDescriptions);
        }
        return ok;
	}
	
	private boolean _describeSelectedCriteriaFailures(final Row row, final boolean isCriteriaFilterGroup, final Collection<String> criteriaFailureDescriptions) {
    	boolean value=true;
    	for (final String key:activeFilters.keySet()) {
    		final ActiveFilter af=activeFilters.get(key);
    		if (isCriteriaFilterGroup) {
				if (af.getType() == ActiveFilter.FilterType.FILTERGROUP) {
					if (!af.describeCriteriaFailures(row, criteriaFailureDescriptions)) {
						value= false;    					    						
					}
				}
    		}
    		else if (!af.describeCriteriaFailures(row, criteriaFailureDescriptions)) {
    			value=false;
    		}
    	}
    	return value;    	
    }
	
    private boolean _describeCriteriaFailures(final Row row, final Collection<String> criteriaFailureDescriptions) {
    	boolean value=true;
    	for (final String key:activeFilters.keySet()) {
    		final ActiveFilter af=activeFilters.get(key);
    		if (!af.describeCriteriaFailures(row, criteriaFailureDescriptions)) {
    			value=false;    			
    		}
    	}
    	return value;    	
    }


	/**
	 * Override if delete semantics change
	 *
	 * @param filteredDataRowIndex the index to the filtered collection of rows
	 *
	 * @throws UnsupportedOperationException
	 */
	public void delete(final int filteredDataRowIndex) throws UnsupportedOperationException	{
		if ( ! isDeletable()){
			throw new UnsupportedOperationException("isDeletable() returns FALSE");
		}
		_delete(filteredDataRowIndex);
	}

	protected final int getDataRowIndex(final int filteredDataRowIndex) {
		if (filteredDataRows == dataRows || filteredDataRows == null) {
			return filteredDataRowIndex;
		}
		final Object row = filteredDataRows.get(filteredDataRowIndex);
		final int n = dataRows.size();
		for (int dataRowIndex = 0; dataRowIndex < n; dataRowIndex++) {
			if (row.equals(dataRows.get(dataRowIndex)))	{
				return dataRowIndex;
			}
		}
		return -1;
	}

	protected final int getFilteredDataRowIndex(final Row row,
												final int suspectedIndex){
	  final int n=filteredDataRows.size();
	  if (n > 0){
		if (row.equals(filteredDataRows.get(suspectedIndex))){
		  return suspectedIndex;
		}
		for (int filteredDataRowIndex=0; filteredDataRowIndex < n;
			 filteredDataRowIndex++){
		  if (row.equals(filteredDataRows.get(filteredDataRowIndex))){
			return filteredDataRowIndex;
		  }
		}
	  }
	  return -1;
	}

	private Row _delete(final int filteredDataRowIndex) {
        final Row r;
		if (filteredDataRows== dataRows){
			r=dataRows.remove(filteredDataRowIndex);
		} else {
			int dataRowIndex=getDataRowIndex(filteredDataRowIndex);
			if (dataRowIndex < 0) {
				System.err.println("Detected inconsistency between view and data when removing " + filteredDataRowIndex + "!");
                r=null;
			} else  {
				final Row debug1=filteredDataRows.get(filteredDataRowIndex),debug2=dataRows.get(dataRowIndex);
				dataRows.remove(dataRowIndex);
				r=filteredDataRows.remove(filteredDataRowIndex);
			}
		}
        return r;
	}
	private boolean removingFilter = false;
	public final void removeFilter()	{
		removingFilter = true;
		filteredDataRows=dataRows;
        if (activeFilters.size()>0){
            applyActiveFilters();
        }
        removingFilter = false;
	}

	public final boolean isFiltering()	{
		return dataRows!=filteredDataRows;
	}


	public Row getBlankFilter()	{
		return createBlankListRow();
	}

	public int size()	{
		return dataRows.size();
	}

	public final List<Row> getFilteredDataRows()	{
		return filteredDataRows == null ? dataRows : filteredDataRows;
	}

	private JCheckBox suspendControlPanelHiding=null;
	
    public void setControlPanelSuspension(final JCheckBox suspendControlPanel){
    	suspendControlPanelHiding=suspendControlPanel;
    }

    
    public String describeHiddenQueryResults() {
		if (dataRows != filteredDataRows) {
			int hidden = 0;
			if (activeFilters.size() > 0) {
				Counter<String> criteriaShortFall = new Counter<String>();
				for (final Row row : filteredDataRows) {
					final Collection<String> c2 = new LinkedHashSet<String>();
					if (describeCriteriaFailures(row, c2)) {
						hidden++;
						for (final String key : c2) {
							criteriaShortFall.count(key);
						}
					}
				}
				if (hidden > 0) {
					final StringBuilder sb = new StringBuilder("Your query finds ");
					sb.append(filteredDataRows.size());
					if (filteredDataRows.size()>1){
						sb.append( " items, ");
					} else {
						sb.append( " item, ");
							
					}
					sb.append(hidden);
					sb.append(" of which will be <br> hidden by ");
					sb.append("<table border='1'><thead><tr><th>Control panel item</th><th># rows affected</th></tr></thead>");
					for (final String key : criteriaShortFall.keySet()) {
						sb.append("<tr><td>");
						sb.append(key);
						sb.append("</td><td>");
						sb.append(criteriaShortFall.getCount(key));
						sb.append("</td></tr>");
					}
					sb.append("</table>");
					String title = filteredDataRows.size()+ " found, " + hidden + " hidden ";
					return Basics.toHtmlUncentered(title, sb.toString());
				}
			}
		}
		return null;
	}
    public void showQueryResults(final AbstractButton b, final String defaultToolTipText){
    	final int n=filteredDataRows.size();
		final boolean rowsFound =  n > 0;
		String toolTip = describeHiddenQueryResults();
		if (toolTip == null) {
			if (rowsFound) {
				toolTip = Basics.toHtmlUncentered(n +(n>1?" items":" item")+ " found",
						defaultToolTipText);
			} else {
				toolTip = "No items found!";
			}
		}
		b.setToolTipText(toolTip);
		ToolTipOnDemand.getSingleton().show(b, false, b.getWidth() + 5,
				b.getHeight() + 5, suspendControlPanelHiding, null);
		SwingBasics.setEnabled(b, rowsFound);
	}

    private String activeFilterName="active filters";
    public void setActiveFilterName(final String name){
        activeFilterName=name;
    }

	public void filter(final SeeOnlyTokens sot) {
		filteredDataRows = new ArrayList<Row>();
		sot.initDataColumnIndexes();
		for (final Row row : getDataRows()) {
			if (sot.matches(row)) {
				filteredDataRows.add(row);
			}
		}
		applyExternalFilters();
	}

	public void filter(
       final Row filteringRow,
       final Filterable filterable) throws UnsupportedOperationException{
		if (!isFilterable()){
			throw new UnsupportedOperationException("isFilterable() returns FALSE");
		}

		this.filteredDataRows=filter(this.dataRows, filteringRow, filterable);
		
		applyExternalFilters();
		
	}

	public boolean isFilterable(){
		return !usedForGroupSeivedTable() ? true : seivedDataSource.isFilterable();
	}

	public boolean isCreatable(){
		return usedForGroupSeivedTable() ? false : true;
	}

	public boolean isEditable(){
		return usedForGroupSeivedTable() ? false : true;
	}


	public boolean isDeletable(){
		return true;
	}
	
    public boolean isDecorator() {
    	return false;
    }
    public void decorate(TableCellContext context, JComponent component, boolean isSelected, boolean hasFocus) {}


	public boolean handleDoubleClick(final TableCellContext context){
		return !usedForGroupSeivedTable()?true:seivedDataSource.handleDoubleClick(context);
	}

	public final List<Row> conjugateByOR(final DefaultFilterable.Filter [][] filters){
		final DefaultFilterable.Op op=new DefaultFilterable.Op( this, filters );
		return op.or(dataRows);
	}


	public Object clone() throws CloneNotSupportedException {
		if (usedForGroupSeivedTable()){
			throw new CloneNotSupportedException("Can not use for group seived table");
		}
		final DefaultPersonalizableDataSource d=(DefaultPersonalizableDataSource)super.clone();
		d.dataRows=Basics.toList(dataRows);
		d.filteredDataRows=d.dataRows;
		return d;
	}
	
	public void applyExternalFilters() {
		if (suspendControlPanelHiding == null
				|| !suspendControlPanelHiding.isSelected()) {
			if (activeFilters.size() > 0) {
				final ArrayList<Row> l = new ArrayList<Row>();
				for (final Row row : filteredDataRows) {
					if (meetsExternalFilterCriteria(row)) {
						l.add(row);
					}
				}
				setFilter(l);
			}
		}
		notifyFilterAppiedListeners("applyNonActiveFilters");		
	}
	
    public List<Row> applyActiveFilters(){
    	if (activeFilters.size() > 0) {
			final ArrayList<Row> l = new ArrayList<Row>();
			final int n=dataRows.size();
			for (int i=0;i<n;i++) {
				final Row row=dataRows.get(i);
				if (meetsCriteria(row)) {
					l.add(row);
				}
			}
			setFilter(l);
		}
    	
    	if (hiddenFilter != null & !Basics.isEmpty(dataRows)) {
    		final ArrayList<Row> hiddenRows = new ArrayList<Row>();
    		for (Row row: dataRows) {
    			if (!hiddenFilter.meetsCriteria(row)) {
    				hiddenRows.add(row);
    				filteredDataRows.remove(row);
    			}
    		}
    		if (!removingFilter) {
    			setHiddenRows(hiddenRows);    			
    		}
    	}
    	notifyFilterAppiedListeners("applyActiveFilters");
		return filteredDataRows;
    }
    private int filteringContext=FILTER_CONTEXT_NORMAL;
    public void setFilteringContext(final int id){
    	filteringContext=id;
    }
    
    public int getFilteringContext(){
    	return filteringContext;
    }
    
    private ActionEvent ae=null;
	
	public void notifyFilterAppiedListeners(final String cmd) {
		if (filterAppliedListeners != null) {
			ae = new ActionEvent(this, filteringContext, cmd);
			if (!filterNotified) {

				filterNotified = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						filterNotified = false;
						for (final ActionListener al : filterAppliedListeners) {
							al.actionPerformed(ae);
						}
					}
				});
				filterNotified = false;
			}
		}
	}
    private boolean filterNotified=false;
    private Collection<ActionListener>filterAppliedListeners;
	public void addFilterAppliedListener(final ActionListener al){
		if (filterAppliedListeners==null){
			filterAppliedListeners=new ArrayList<ActionListener>();
		}
		filterAppliedListeners.add(al);
	}

    public final void setHiddenRows(final java.util.List<Row> rows){
        hiddenDataRows=rows;
    }
    
    public  List<Row> getHiddenRows() {
    	return hiddenDataRows;
    }
    
    // new methods to support table TABS for distinct column values
    private static Map<String,ActiveFilter>EMPTY_FILTERS=Collections.unmodifiableMap(new HashMap<String,ActiveFilter>());
    private final Map<String,ActiveFilter>activeFilters=new TreeMap<String, ActiveFilter>();
    private final Map<String,ActiveFilter>uniqueActiveFilters=new TreeMap<String, ActiveFilter>();
    
    public Map<String, ActiveFilter>  getActiveFilters(){
        return this.activeFilters;
    }

    public List<Row> setActiveFilters(final Map<String,ActiveFilter>activeFiltersByKey){
    	this.activeFilters.clear();
    	for (final String key:activeFiltersByKey.keySet()) {
    		this.activeFilters.put(key, activeFiltersByKey.get(key));
    	}
    	setUniqueActiveFilters();
    	return applyActiveFilters();
    }
    
    public boolean hasActiveFilter(final String key) {
    	return activeFilters.containsKey(key);
    }
    
    private void setUniqueActiveFilters(){
    	final Collection<SelectableActiveFilterGroup>c=new ArrayList<SelectableActiveFilterGroup>();
    	uniqueActiveFilters.clear();
    	
    	for (final String key:activeFilters.keySet()){
    		final ActiveFilter af=activeFilters.get(key);
    		SelectableActiveFilterGroup g=af.getTopGroup();
    		if (g==null){
    			uniqueActiveFilters.put(key, af);
    		} else {
    			final String k=g.getKey();
    			if (!c.contains(g)){
    				uniqueActiveFilters.put(k, af);
    				c.add(g);
    			}
    		}
    	}
    }
    public List<Row> setActiveFilter(final String key, final ActiveFilter activeFilter){
    	activeFilters.put(key, activeFilter);
    	setUniqueActiveFilters();
    	if (!delayActiveFilterApplying){
    		return applyActiveFilters();
    	}
    	return null;
    }
    
    public static boolean delayActiveFilterApplying=false;
    private ActiveFilter hiddenFilter;
    
    public void setHiddenFilter(ActiveFilter activeFilter) {
    	hiddenFilter = activeFilter;
    }
    public ActiveFilter getHiddenFilter() {
    	return hiddenFilter;
    }
    public ActiveFilter.FilterType getType() {
    	return ActiveFilter.FilterType.SOURCE;
    }
    
    
    
    public boolean meetsCriteria(final Row row) {
    	boolean value=true;
    	for (final String key:uniqueActiveFilters.keySet()) {
    		final ActiveFilter af=uniqueActiveFilters.get(key);
    		if (!af.meetsCriteria(row)) {
    			value=false;
    			break;
    		}
    	}
    	return value;    	
    }
    
    private boolean meetsExternalFilterCriteria(final Row row) {
    	boolean value=true;
    	for (final String key:uniqueActiveFilters.keySet()) {
    		final ActiveFilter af=uniqueActiveFilters.get(key);    		
    		if ( !af.meetsCriteria(row)) {
    			value=false;
				break;
    		}
    	}
    	return value;    	
    }
    
    public boolean removeActiveFilter(final String key) {
    	removingFilter = true;
		final boolean value = activeFilters.containsKey(key);
		if (value) {
			activeFilters.remove(key);
	    	setUniqueActiveFilters();
			applyActiveFilters();
		}
		removingFilter = false;
		return value;
	}
    
    public boolean checkCardinality(final DisabledExplainer b, final int changeInCount) {    	
    	final String complaint=Basics.getCardinalityAnomalyStatement(size(), size()+changeInCount, getMinimumCardinality(), getMaximumCardinality(), true);
    	return b==null?complaint==null:b.setEnabled(complaint);    	
    }
    
    public boolean doFilterRemovalWhenRefiltering(){
    	return true;
    }
       
    public void adjustActiveFilters(final Row row){    	
    }
    public boolean hasActiveFilter() {
    	return activeFilters.size()>0;
    }
    
    public SelectableActiveFilterGroup getTopGroup(){
    	return null;
    }

	public boolean handleRowButtonClick(JButton button, TableCellContext context) {
		return usedForGroupSeivedTable()?seivedDataSource.handleRowButtonClick(button,context):false;
	}

	public Collection<Integer> getMustHideColumns() {
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	public FoundListener getFoundListener(Row row, int dataColumnIndex) {
		return null;
	}

	private Map<Integer,String>useHeaderLabelsForLastRow;
	public void useHeaderLabelAsRenderOnlyValueForLastRow(final int dataColumnIndex, final String value){
		if (useHeaderLabelsForLastRow==null){
			useHeaderLabelsForLastRow=new HashMap<Integer, String>();
		}
		useHeaderLabelsForLastRow.put(dataColumnIndex, value);
	}
	
	public Object getRenderOnlyValueForLastRow(final int dataColumnIndex,
			final boolean isSelected, final boolean hasFocus) {
		if (useHeaderLabelsForLastRow!=null && useHeaderLabelsForLastRow.containsKey(dataColumnIndex)){
			final String htmlDisabledColor=SwingBasics.toHtmlRGB(SystemColor.textInactiveText);
			return Basics.concat(
					"<html><font color='", htmlDisabledColor,
					"'><small><i>", 
					Basics.stripSimpleHtml(useHeaderLabelsForLastRow.get(dataColumnIndex)),
					"</i></small></font></html>");

		}
		return null;
	}
	@Override
	public void prepareRenderer(JComponent component, int row, int column) {
		// TODO Auto-generated method stub
		
	}
    
}
