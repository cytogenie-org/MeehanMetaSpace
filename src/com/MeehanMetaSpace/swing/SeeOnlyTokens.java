package com.MeehanMetaSpace.swing;

import java.util.regex.Pattern;

import javax.swing.JTextField;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.SearchAndReplace;
import com.MeehanMetaSpace.StringConverter;

public class SeeOnlyTokens extends SearchAndReplace{
	
	private final boolean forTree;
	public final PersonalizableTableModel tableModel;
	private int []dataColumnIndexes;
	void initDataColumnIndexes(){
		if (!forTree) {
			dataColumnIndexes=tableModel.getDataColumnIndexesThatAreVisible();
		} else {
			if (tableModel.applicationSpecificTreeSearch==null || !Basics.containsAll(tableModel.getSort(), tableModel.applicationSpecificTreeSearch)) {
				dataColumnIndexes=tableModel.getSort();
			}else {
				dataColumnIndexes=tableModel.applicationSpecificTreeSearch;
			}
			if (Basics.isEmpty(dataColumnIndexes)) {
				dataColumnIndexes=tableModel.getDataColumnIndexesThatAreVisible();
			}
		}
	}
	public boolean isDataColumnIndexSeen(final int dataColumnIndex) {
		return Basics.indexOf(this.dataColumnIndexes, dataColumnIndex)>=0;
	}
	private final _StringConverter sc;
	
	public SeeOnlyTokens(final String searchEntry, final PersonalizableTableModel tableModel, final boolean forTree) {
		super(searchEntry, true);
		this.tableModel=tableModel;
		this.forTree=forTree;
		sc=new _StringConverter();
	}

	public boolean matches(final Row row){
		if (super.isUsingPlusAsAnd() ) {
			return _matches(row);
		}
		for (final int dataColumnIndex:dataColumnIndexes){
			final Basics.HtmlBody hb=new Basics.HtmlBody(tableModel.toSequenceableString(row, dataColumnIndex));
			if (super.matches(hb.getHtmlEncoded())){
				return true;
			}
		}
		return false;
	}
	
	String highlightFgBg(final Row row, final String bgColor, final String color, final String cellValue) {
		return  this.replaceAll(row, cellValue, super.getFgBgColorReplacementSpec(bgColor, color));
	}
	
	String highlightBg(final Row row, final String bgColor, final String cellValue) {
		return  this.replaceAll(row, cellValue, super.getBgColorReplacementSpec(bgColor));
	}

	String highlightFg(final Row row, final String color, final String cellValue) {
		return  this.replaceAll(row, cellValue, super.getFgColorReplacementSpec(color));
	}

	private boolean _matches(final Row row) {
		final String data=sc.toString(row);
		return super.matches(data);
	}

	
	private String replaceAll(final Row row, final String cellValue, final String replacementSpecification) {
		if (row==null) {
			return cellValue;
		}
		final Pattern pat=super.getReplaceAllPattern(row, sc);
		return pat==null?cellValue:SearchAndReplace.replaceAll(pat, cellValue, replacementSpecification);
	}
	
	private class _StringConverter implements StringConverter{

		public int getHorizontalAlignment(){
			return JTextField.LEFT;
		}

		public String getFormatTip()
		{
			return "??";
		}

		public String toString(Object input) {
			final Row row=(Row)input;
			final StringBuffer sb=new StringBuffer(dataColumnIndexes.length*15);
			for (final int dataColumnIndex:dataColumnIndexes){
				final String data=tableModel.toSequenceableString(row, dataColumnIndex);
				sb.append('\t');
				sb.append(data);
			}
			return sb.toString();
		}

		public Object toObject(String input) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	public boolean hasReplacementParts(final Row row, final String data) {
		final Pattern pat = super.getReplaceAllPattern(row, sc);
		if (pat != null) {
			return pat.matcher(data).find();
		}
		return false;
	}
}
