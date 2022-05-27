package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.*;
import java.util.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class ColumnValueGroup{
  public static class Schema{

    private final int[] dataColumnIndexes;

    public Schema(final int[] dataColumnIndexes){
      this.dataColumnIndexes=dataColumnIndexes;
    }

  }

  public static class Item{
    private final int dataColumnIndex;
    private final Object value;
    public Item(final int dataColumnIndex, final Object value){
      this.dataColumnIndex=dataColumnIndex;
      this.value=value;
    }

    private int hc=0;
    public int hashCode(){
      if (hc == 0){
        hc=17;
        hc=37 * hc + dataColumnIndex;
        hc=37 * hc + (value == null ? 0 : value.hashCode());
      }
      return hc;
    }

    public boolean equals(final Object o){
      if (this == o){
        return true;
      }
      if (!(o instanceof Item)){
        return false;
      }
      final Item item=(Item) o;
      return item.dataColumnIndex == dataColumnIndex &&
          Basics.equals(item.value, value);
    }
  }

  private final Item[] items;
  private final Schema schema;

  public ColumnValueGroup(final Schema schema, final Row row){
    this.items=new Item[schema.dataColumnIndexes.length];
    this.schema=schema;
    for (int i=0; i < items.length; i++){
      items[i]=new Item(schema.dataColumnIndexes[i],
                        row.get(schema.dataColumnIndexes[i]));
    }
  }
  public ColumnValueGroup(final Schema schema, final Object value){
	    this.items=new Item[schema.dataColumnIndexes.length];
	    this.schema=schema;
	    for (int i=0; i < items.length; i++){
	      items[i]=new Item(schema.dataColumnIndexes[i],
	                        value);
	    }
	  }

  private int result=0;
  public int hashCode(){
    if (result ==0){
      result=17;
      for (int i=0; i < items.length; i++){
        result=37 * result + items[i].hashCode();
      }
    }
    return result;
  }

  public boolean equals(final Object o){
    if (this == o){
      return true;
    }
    if ( !(o instanceof ColumnValueGroup )){
      return false;
    }
    final ColumnValueGroup cvg=(ColumnValueGroup)o;
    return Basics.equals(items,cvg.items );
  }

  private boolean hasEqual(final Row row){
    for (int i=0;i<schema.dataColumnIndexes.length;i++){
      if (! Basics.equals(items[i].value, row.get(items[i].dataColumnIndex))){
        return false;
      }
    }
    return true;
  }

  private ColumnValueGroup clone(final Row row){
    return new ColumnValueGroup(schema, row);
  }

  public static class WeakMap{
    private final WeakHashMap<ColumnValueGroup, java.util.List<Row>> whm=new WeakHashMap<ColumnValueGroup, java.util.List<Row>>();
    private final PersonalizableTableModel tableModel;

public void clear(){
  whm.clear();
}
    public WeakMap(
    final PersonalizableTableModel tableModel){
      this.tableModel=tableModel;
    }


      public java.util.List<Row> getRows(
      final ColumnValueGroup key,
      final boolean filtered){
        if (!whm.containsKey(key)){
          final java.util.List<Row> values=new ArrayList<Row>();
          final List l=filtered ?
              tableModel.getDataSource().getFilteredDataRows():
              tableModel.getDataSource().getDataRows();
          for (final Iterator it=l.iterator();it.hasNext();){
            final Row row=(Row)it.next();
            if (key.hasEqual(row)){
              values.add(row);
            }
          }
          whm.put(key, values);
        }
        return whm.get(key);
      }

  }
}
