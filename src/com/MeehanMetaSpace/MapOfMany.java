package com.MeehanMetaSpace;

import java.lang.reflect.Array;
import java.util.*;

public class MapOfMany <K, V> {
  private final Map<K, Collection<V>> map;
  public String toHtmlTable(
          final String keyLabel,
          final String valueLabel) {
	  return toHtmlTable(keyLabel, "count",valueLabel, null, null);
  }
  public String toHtmlTable() {
	  return toHtmlTable("key", "count","value", null, null);
  }

  public String toHtmlTable(
          final String keyLabel,
          final String cntLabel,
          final String valueLabel,
          final StringConverter scKey,
          final StringConverter scValue) {
        final StringBuilder sb = new StringBuilder(
                "<table border='1'><thead><tr><th>");
        sb.append(keyLabel);
        sb.append("</th><th>");
        sb.append(cntLabel == null ? "#" : cntLabel);
        sb.append("</th><th>");
        sb.append(valueLabel);
        sb.append("</th></tr></thead>");
        for (final Iterator<K> it = map.keySet().iterator(); it.hasNext(); ) {
          final K key = it.next();
          sb.append("<tr><td>");
          sb.append(scKey==null?Basics.toString(key):scKey.toString(key ));
          sb.append("</td><td>");
          final Collection<V> c = getCollection(key);
          sb.append(c.size());
          sb.append("</td><td>");
          sb.append(scValue==null?Basics.toString(c):Basics.toString(c, scValue,false));
          sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
  }

  public MapOfMany() {
    this(false);
  }

  public MapOfMany(final boolean sorted) {
        this(sorted, true);
  }

  private final boolean allowDuplicateValues;
  public MapOfMany(final boolean sorted, final boolean allowDuplicateValues) {
        this.allowDuplicateValues = allowDuplicateValues;
        if (sorted) {
          map = new TreeMap<K, Collection<V>>(Basics.caseInsensitive);
        }
        else {
          map = new HashMap<K, Collection<V>>();
        }
  }

  public MapOfMany(final Comparator  keyCmp, final boolean allowDuplicateValues) {
      this.allowDuplicateValues = allowDuplicateValues;
      map = new TreeMap<K, Collection<V>>(keyCmp);
}

  public MapOfMany(final Map<K, Collection<V>> map, final boolean allowDuplicateValues) {
      this.allowDuplicateValues = allowDuplicateValues;
      this.map = map;
}

  private Class<K> keyType;
  public void setKeyType(Class<K>keyType){
	  this.keyType=keyType;
  }
protected Object[]newKeysArray(){
	final int n=keySize();
	if (keyType != null){
		return (K[]) Array.newInstance(keyType, n);
	}
  return new Object[n];
}
  protected Collection<V> newValuesCollection() {
        return new ArrayList<V>();
  }

  public void putAll(final K key, final Collection<V> values){
    for (final Iterator<V> it=values.iterator(); it.hasNext(); ){
      final V value=it.next();
      put(key, value);
    }
  }
  
  
  public void putAll(final MapOfMany <K, V> mapOfManyObject){
		 
	  for (final K key:mapOfManyObject.map.keySet()){
    	  for (final V value:mapOfManyObject.map.get(key)) {
    		  put(key, value);
    	  }
      }
  }

  public void put(final K key, final V value) {
        Collection<V> c = map.get(key);
        if (c == null) {
          c = newValuesCollection();
          map.put(key, c);
        }
        if (allowDuplicateValues || !c.contains(value)){
          c.add(value);
        }
        if(keyType==null && key != null){
        	keyType=(Class<K>) key.getClass();
        }
  }

  public Collection<V> getAllValues(){
      final Collection<V> c=newValuesCollection();
      for (final Collection<V> c2:map.values()){
         c.addAll(c2);
      }
      return c;
  }

  public Collection<V> getAllValuesInKeyOrder(){
      final Collection<V> c=newValuesCollection();
      for (final K k:map.keySet()){
    	  final Collection<V> c2=getCollection(k);
         c.addAll(c2);
      }
      return c;
  }

  public Collection<V> getAllValuesInReverseKeyOrder(){
      final Collection<V> c=newValuesCollection();
      final ArrayList<K> l=new ArrayList<K>(map.keySet());
      for (int i=l.size()-1;i>=0;i--){
    	  final K k=l.get(i);
    	  final Collection<V> c2=getCollection(k);
         c.addAll(c2);
      }
      return c;
  }

  public Set<K> keySet() {
        return map.keySet();
  }

  public K[] keys() {
        return (K[])keySet().toArray(newKeysArray());
  }

  public Map<K, Collection<V>> getMap(){
	  return map;
  }
  public void clear() {
        map.clear();
  }

  public Collection<V>[] values() {
        final Collection c = map.values();
        return (Collection<V>[]) c.toArray(new Collection[c.size()]);
  }

  private Collection<V>empty=newValuesCollection(); 
  public Collection<V> getCollection(final K key) {
        final Collection<V> c= map.get(key);
        return c==null ? empty : c;
  }

  public Iterator<V> get(final K key) {
        //DREPC
        final Collection<V> c = getCollection(key);
        return c.iterator();
  }

  public boolean containsKey(final K key) {
      return map.containsKey(key);
  }
  
  public int size() {
	  if (map != null) {
		  return map.size();		  
	  }
	  return 0;
  }
  
  public int valuesSize(final K key) {
	  if (containsKey(key)) {
		  return map.get(key).size();		  
	  }
	  return 0;
  }

  public V getValue(final K key) {
          final Iterator<V> c = get(key);
          return c.hasNext()? c.next():null;
  }
  public boolean contains(final K key, final V value) {
        final Collection<V> c = map.get(key);
        return c != null && c.contains(value);
  }

  public Collection<V> removeAll(final K key) {
        return map.remove(key);
  }

  public boolean remove(final K key, final V value) {
      final  boolean o;
      final Collection<V> c = map.get(key);
      if (c != null && c.contains(value)) {
          o=c.remove(value);
          if (c.size() == 0) {
              map.remove(key);
          }
      } else {
          o=false;
      }
      return o;
  }

  public int keySize(){
    return map.size();
  }

  public int valueSize() {
        int size=0;
        final Collection []c=values();
        for (int i=0;i<c.length;i++){
          size+=c[i].size();
        }
        return size;
  }
  
  public int getCombinatoricSize() {
	  int size=keySize()>0 ? 1:0;
	  for (final K k:keys()) {
		  size *= getCollection(k).size();
	  }
	  return size;
  }

  public final int getMaxKeySize(){
      final Collection<K> ks=new ArrayList<K>();
      int max=0;
      for (final K _k:map.keySet()){
          final int _n=getCollection(_k).size();
          if (_n>max){
              max=_n;
          } 
      }
      return max;
  }
  public final MapOfMany<Integer, K> getKeysInSizeOrder(){
      return getKeysInSizeOrder(this.keySet());
  }
  

  public final MapOfMany<Integer, K> getKeysInSizeOrder(final Set<K> keys){
      final MapOfMany<Integer, K> ts=new MapOfMany<Integer, K>(true, false);
      for (final K _k:keys){
          final int _n = getCollection(_k).size();
          ts.put(_n,_k);
      }
      return ts;
  }

  public final MapOfMany<V, K> invertValuesAndKeys(final Collection<K> ks){
      MapOfMany<V, K> value=new MapOfMany<V,K>(map instanceof TreeMap, allowDuplicateValues);
      for (final K _k:ks){
          final Collection<V> c=getCollection(_k);
          for (final V v:c){
              value.put(v, _k);
          }
      }
      return value;
  }

  public void putAll(final K key, V []values){
      for (final V value:values){
          put(key,value);
      }
  }

  public Map<V,K> getKeyByValue() {
	  final Map<V,K> hm=new HashMap<V,K>();
      for (final K key:map.keySet()){
    	  for (final V value:map.get(key)) {
    	  hm.put(value,key);
    	  }
      }
      return hm;
  }

  public MapOfMany<V,K> getKeysByValue(final boolean sorted) {
	  final MapOfMany<V,K> m=new MapOfMany<V,K>(sorted, false);
      for (final K key:map.keySet()){
    	  for (final V value:map.get(key)) {
    		  m.put(value,key);
    	  }
      }
      return m;
  }

  public MapOfMany(final Object[][] keyThenValues, final boolean sorted, final boolean allowDuplicateValues) {
		this(sorted, allowDuplicateValues);
		for (final Object[] ok : keyThenValues) {

			for (int i = 1; i < ok.length; i++) {

				put((K) ok[0], (V) ok[i]);
			}
		}
	}
  
  public static void main(final String []args){
	  final MapOfMany<String, Integer>m=new MapOfMany<String, Integer>();
	  m.put("foo", 2);
	  m.put("foo", 3);
	  m.put("foobar", 1);
	  String string=m.toHtmlTable("string", "integer");
	  String[]ss=m.keys();
	  string=Basics.toString(ss);
  }
  
  private Map<K,V> firstValues;
  public void freezeCurrentFirstValues(){
	  firstValues=new HashMap<K, V>();
	  for (K key:keySet()){
		  firstValues.put(key, get(key).next());
	  }
  }
  
  public V getFirstValue(K key){
	  return firstValues.get(key);
  }
  
  public String toString(){
	final StringBuilder sb=new StringBuilder();
	for (final K key:keySet()){
		sb.append(key);
		sb.append("=");
		sb.append(Basics.toString(getCollection(key).size()));
		sb.append("; ");
	  }
	sb.append(Basics.lineFeed);
	for (final K key:keySet()){
		sb.append(key);
		sb.append("=");
		sb.append(Basics.toString(getCollection(key)));
		sb.append(Basics.lineFeed);
	  }
	return sb.toString();
  }
}
