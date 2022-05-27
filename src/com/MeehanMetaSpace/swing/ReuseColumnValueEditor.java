package com.MeehanMetaSpace.swing;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import com.MeehanMetaSpace.*;

public class ReuseColumnValueEditor
	implements StringConverter {

  public String toString(final Object input) {
	if (input instanceof Proxy) {
	  return ( (Proxy) input).toString();
	}
	return sc.toString(input);
  }

  public Object toObject(final String input) throws Exception {
	return sc.toObject(input);
  }

  public int getHorizontalAlignment() {
	return sc.getHorizontalAlignment();
  }

  public String getFormatTip() {
	return sc.getFormatTip();
  }

  private final Class<? extends Object> cl;
  private final Set<Proxy> prevValuesForColumn = new TreeSet<Proxy>();
  private StringConverter sc;
  private ReuseColumnValueEditor(
	  final PersonalizableDataSource ds,
	  final int dataColumnIndex) {
	Class cl = ds.getMetaRow().getClass(dataColumnIndex);
	sc = DefaultStringConverters.get(cl);
	final java.util.List<Row> l = ds.getDataRows();
	for (final Row row:l) {
	  final Object rawValue = row.get(dataColumnIndex);		
	  final Object cookedValue=TableBasics.getSortableValueInDropDownList(row, dataColumnIndex, rawValue);
	  if (rawValue instanceof AbstractButton) {
		  cl=cookedValue.getClass();
		sc = DefaultStringConverters.get(cl);

	  }
	  if (cookedValue != null) {
		  final Proxy proxy=new Proxy(cookedValue);
		  prevValuesForColumn.add(proxy);		
	  }
	}
	this.cl=cl;
  }

  class Proxy
	  implements Comparable {
	final Object o;
	Comparable cmp;

	Proxy(final Object o) {
	  this.o = o;
	  if (o instanceof Comparable){
		  cmp=(Comparable) o;
	  } else {
		  cmp=sc.toString(o);
		  if (cmp==null){
			  cmp="";
		  }
	  }
	}

	public String toString() {
	  return sc.toString(o);
	}

	public int compareTo(Object obj) {
	  if (obj instanceof Proxy) {
		return cmp.compareTo( ( (Proxy) obj).cmp);
	  }
	  return cmp.compareTo(obj);
	}

	public boolean equals(final Object obj) {
	  if (obj instanceof Proxy) {
		return Basics.equals(o, ( (Proxy) obj).o);
	  }
	  return Basics.equals(o, obj);
	}

	public int hashCode() {
	  return o.hashCode();
	}
  }

  private Object coerceValue(final Object o) {
	if (o == null || o.equals(ComboCellEditor.newItem)) {
	  return null;
	}

	if (o instanceof Proxy) {
	  return ( (Proxy) o).o;
	}
	if (o.getClass().equals(cl)) {
	  return o;
	}
	if (o instanceof String) {
	  try {

		return sc.toObject( (String) o);
	  }
	  catch (Exception e) {
		Pel.log.warn(e);
	  }
	}
	return null;

  }

  public static TableCellEditor New(
	  final PersonalizableDataSource ds,
	  final int dataColumnIndex,
	  final Class cl) {
	return New(ds, dataColumnIndex, 1000, cl);
  }

  public static TableCellEditor New(
	  final PersonalizableDataSource ds,
	  final int dataColumnIndex,
	  final int delayKeyEntryByMilliSecs,
	  final Class cl) {
	final ReuseColumnValueEditor rcve = new ReuseColumnValueEditor(ds,
		dataColumnIndex);
	  final AutoComplete ac =
		  new AutoComplete( cl == Boolean.class ? new Object[]{Boolean.TRUE, Boolean.FALSE} : rcve.prevValuesForColumn.toArray(), rcve, true, true);
	  return new AutoComplete.CellEditor(ac, true, true) {
		public Object getCellEditorValue() {
		  return rcve.coerceValue(ac.getFinalValue());
		}

	  };
  }

}
