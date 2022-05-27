package com.MeehanMetaSpace;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.MeehanMetaSpace.MapOfMaps.InnerMapConstructor;

public class MapOfMapOfMany <OK, IK, V> {
	public interface InnerMapConstructor <IK,V>{
		MapOfMany<IK,V> instantiate();
	}
	
	private final Map<OK, MapOfMany<IK,V>> outerMap;
	
	private final InnerMapConstructor<IK,V> imc;
	private final boolean sortInnerKey, allowDuplicateValues;
	public MapOfMapOfMany() {
		this(false,false, false);
	}
	public MapOfMapOfMany(final boolean sortOuterKey, final boolean sortInnerKey, final boolean allowValueDuplicates) {
		this.sortInnerKey=sortInnerKey;
		this.allowDuplicateValues=allowValueDuplicates;
		this.outerMap=sortOuterKey?new TreeMap<OK, MapOfMany<IK,V>> ():new HashMap<OK, MapOfMany<IK,V>> ();
		this.imc=null;
	}
	
	public MapOfMapOfMany( final Map<OK, MapOfMany<IK,V>> outerMap, final InnerMapConstructor<IK,V> imc) {
		this.outerMap=outerMap;
		this.imc=imc;
		this.sortInnerKey=false;
		this.allowDuplicateValues=false;
	}

	
	protected MapOfMany<IK, V> newMapOfMany(final boolean sortInnerKey, final boolean allowDuplicateValues){
		return new MapOfMany<IK,V>(sortInnerKey, allowDuplicateValues);
	}
	
	public MapOfMany<IK,V> instantiateIfNecessary(final OK outerKey){
		MapOfMany<IK,V> m=outerMap.get(outerKey);
		if (m==null) {
			if (imc != null) {
				m=imc.instantiate();
			}else {
				m=newMapOfMany(sortInnerKey, allowDuplicateValues);
			}
			outerMap.put(outerKey, m);
		}
		return m;
	}
	public void put( final OK outerKey, final IK innerKey, final V value) {
		MapOfMany<IK,V> m=instantiateIfNecessary(outerKey);
		m.put(innerKey, value);
	}
	
	public MapOfMany<IK,V> remove( final OK outerKey) {
		return outerMap.remove(outerKey);
	}
	
	public Collection<V> remove( final OK outerKey, final IK innerKey) {
		MapOfMany<IK,V> m=outerMap.get(outerKey);
		Collection<V> v=null;
		if (m !=null) {
			v=m.removeAll(innerKey);
		}
		return v;
	}

	public boolean remove( final OK outerKey, final IK innerKey, V innerValue) {
		MapOfMany<IK,V> m=outerMap.get(outerKey);
		boolean ok=false;
		if (m !=null) {
			ok=m.remove(innerKey, innerValue);
		}
		return ok;
	}

	public int size(){
		int size=0;
		for (final OK key:keySet()){
			final MapOfMany<IK,V> mom=get(key);					
			size+=mom==null?0:mom.size();			
		}
		return size;
	}
	public Set<OK> keySet(){
		return outerMap.keySet();
	}
	public void clear() {
		outerMap.clear();
	}
	public void clear(final OK outerKey) {
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			m.clear();
		}
	}

	public Collection<IK>getCollection(final OK outerKey) {
		
		MapOfMany<IK,V> innerMap=outerMap.get(outerKey);
		return innerMap == null ? Basics.UNMODIFIABLE_EMPTY_LIST:innerMap.keySet();
	}
	
	public MapOfMany<IK,V>get(final OK outerKey) {
		return outerMap.get(outerKey);
	}
	public Collection<V> get(final OK outerKey, final IK innerKey) {
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.getCollection(innerKey);
		}
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}
	
	public Collection<MapOfMany<IK,V>> values(){
		return outerMap.values();
	}
	
	public Collection<V> getAllValues(final Collection<V>c){
		for (final OK outerKey:keySet()){
			for (final IK innerKey:keySet(outerKey)){
			c.addAll(get(outerKey, innerKey));
			}
		}
		return c;
	}
	
	public Collection<V> values(final OK outerKey){
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.getAllValues();
		}
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}
	
	public Set<IK> keySet(final OK outerKey){
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.keySet();
		}
		return Collections.unmodifiableSet(new HashSet<IK>());
	}
	
	public boolean contains(final OK outerKey) {
		return outerMap.containsKey(outerKey);
	}
	
	public boolean contains(final OK outerKey, final IK innerKey) {
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.containsKey(innerKey);
		}
		return false;
	}
	public boolean contains(final OK outerKey, final IK innerKey, final V value) {
		final MapOfMany<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.contains(innerKey, value);
		}
		return false;
	}

}
