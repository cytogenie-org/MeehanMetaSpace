package com.MeehanMetaSpace;
import java.util.*;

public class MapOfMaps <OK, IK, V> {
	public interface InnerMapConstructor <IK,V>{
		Map<IK,V> instantiate();
	}
	
	private final Map<OK, Map<IK,V>> outerMap;
	
	private final InnerMapConstructor<IK,V> imc;
	private final boolean sortInnerKey;
	public MapOfMaps() {
		this(false,false);
	}
	public MapOfMaps(final boolean sortOuterKey, final boolean sortInnerKey) {
		this.sortInnerKey=sortInnerKey;
		this.outerMap=sortOuterKey?new TreeMap<OK, Map<IK,V>> ():new HashMap<OK, Map<IK,V>> ();
		this.imc=null;
	}
	
	public MapOfMaps( final Map<OK, Map<IK,V>> outerMap, final InnerMapConstructor<IK,V> imc) {
		this.outerMap=outerMap;
		this.imc=imc;
		this.sortInnerKey=false;
	}
	
	public void put( final OK outerKey, final IK innerKey, final V value) {
		Map<IK,V> m=outerMap.get(outerKey);
		if (m==null) {
			if (imc != null) {
			m=imc.instantiate();
			}else {
				m=sortInnerKey?new TreeMap<IK,V>():new HashMap<IK,V>();
			}
			outerMap.put(outerKey, m);
		}
		m.put(innerKey, value);
	}
	public void put( final OK outerKey, Map<IK,V> map) {
		outerMap.put(outerKey, map);
	}
	
	public Map<IK,V> remove( final OK outerKey) {
		return outerMap.remove(outerKey);
	}
	public V remove( final OK outerKey, final IK innerKey) {
		Map<IK,V> m=outerMap.get(outerKey);
		V v=null;
		if (m !=null) {
			v=m.remove(innerKey);
		}
		return v;
	}
	
	public Set<OK> keySet(){
		return outerMap.keySet();
	}
	public void clear() {
		outerMap.clear();
	}
	public void clear(final OK outerKey) {
		final Map<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			m.clear();
		}
	}

	public Map<IK,V>get(final OK outerKey) {
		return outerMap.get(outerKey);
	}
	public V get(final OK outerKey, final IK innerKey) {
		final Map<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.get(innerKey);
		}
		return null;
	}
	
	public Collection<Map<IK,V>> values(){
		return outerMap.values();
	}
	
	public Collection<V> values(final OK outerKey){
		final Map<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.values();
		}
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}
	
	public Set<IK> keySet(final OK outerKey){
		final Map<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.keySet();
		}
		return Collections.unmodifiableSet(new HashSet());
	}
	
	public boolean contains(final OK outerKey) {
		return outerMap.containsKey(outerKey);
	}
	
	public boolean contains(final OK outerKey, final IK innerKey) {
		final Map<IK,V>m=outerMap.get(outerKey);
		if (m!=null) {
			return m.containsKey(innerKey);
		}
		return false;
	}

	public int size(){
		int n=0;
		for (final OK outerKey:keySet()){
			final Map<IK,V>map=get(outerKey);
			n+=map.size();
		}
		return n;
	}
}
