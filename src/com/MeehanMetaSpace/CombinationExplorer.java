package com.MeehanMetaSpace;

import java.util.*;

public abstract class CombinationExplorer <K,V>{
	public CombinationExplorer(final MapOfMany<K,V>mom){
		this.mom=mom;
	}
	public void set(final MapOfMany<K,V>mom){
		this.mom=mom;
	}
	
	private MapOfMany<K,V>mom;	
	private Object[] k;
	private Map<K,V>map;
	public void explore(){
		this.k=mom.keySet().toArray();
		map=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
		explore(0);
	}
	
	private void explore(final int combinationIndex){
		if (combinationIndex==k.length){
			Map <K,V>clone=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
			for (final K k:map.keySet()){
				clone.put(k, map.get(k));
			}
			explore(clone);
		} else {
			final K key=(K)k[combinationIndex];
			for (final V value:mom.getCollection(key)){
				map.put(key, value);
				explore(combinationIndex+1);
			}
		}
	}
	
	private HashSet<V>v;
	public void exploreUnique(){
		this.k=mom.keySet().toArray();
		map=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
		v=new HashSet<V>();
		exploreUnique(0);
	}
	
	private void exploreUnique(final int combinationIndex){
		if (combinationIndex==k.length){
			final Map <K,V>clone=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
			for (final K k:map.keySet()){
				clone.put(k, map.get(k));
			}
			explore(clone);
		} else {
			final K key=(K)k[combinationIndex];
			for (final V value:mom.getCollection(key)){
				if (!v.contains(value)) {
				map.put(key, value);
				v.add(value);
					exploreUnique(combinationIndex + 1);
				v.remove(value);
				}
			}
		}
	}

	public Map <K,V>findFirstUnique(){
		this.k=mom.keySet().toArray();
		map=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
		v=new HashSet<V>();
		return findFirstUnique(0);
	}
	
	private Map <K,V> findFirstUnique(final int combinationIndex){
		Map <K,V>rc=null;
		if (combinationIndex==k.length){
			final Map <K,V>clone=mom.getMap() instanceof TreeMap? new TreeMap<K,V>():new HashMap<K,V>();
			for (final K key:map.keySet()){
				clone.put(key, map.get(key));
			}
			explore(clone);
			rc=clone;
		} else {
			final K key=(K)k[combinationIndex];
			for (final V value : mom.getCollection(key)) {
				if (!v.contains(value)) {
					map.put(key, value);
					v.add(value);
					rc=findFirstUnique(combinationIndex + 1);
					if (rc != null){
						break;
					}
					v.remove(value);
				}
			}
		}
		return rc;
	}

	public static class Counter<K,V> extends CombinationExplorer<K,V>{
		
		private int count=0;
		public int getCount(){
			return count;
		}
		public boolean verbose;
		public Counter(){
			super(null);
		}
		public Counter(final MapOfMany<K,V>mom){
			super(mom);
		}
		public void explore(){
			count=0;
			lastOne=null;
			
			super.explore();
		}
		public void explore(final Map<K,V>m){
			count++;
			lastOne=m;
			if (verbose) {
				for (final K key : m.keySet()) {
					System.out.print(key);
					System.out.print("=");
					System.out.print(m.get(key));
					System.out.print(";");
				}
				System.out.println();
			}

		}
		
		public void exploreUnique(){
			lastOne=null;
			count=0;
			super.exploreUnique();
		}
		
		public Map<K,V>getLastOne(){
			return lastOne;
		}
		
		private Map<K,V>lastOne;
	}

	public abstract void explore(final Map<K,V> combination);
	
	private final static String[][]testHaptens=new String[][]{
		{"FITC", "Biotin","DNP"},
		{"Texas-red", "Biotin","DNP"},
		{"APC", "Biotin"}
	};
	private final static String[][]testHaptens2=new String[][]{
		{"FITC", "Biotin","DNP"},
		{"Texas-red", "Biotin","DNP"}
	};
	
	private final static String[][]testDye=new String[][]{
			{"CD43", "420 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
			{"CD4", "420 514/25","520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
			{"CD2", "420 514/25","520 614/25","520 625/25","520 645/25", "620 745/25"},
			{"CD90", "420 514/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25"},
			{"IgG1", "520 625/25","520 645/25", "620 745/25"},
			{"Ly-6", "420 514/25","420 525/25", "620 714/25","620 725/25"},
			{"CD143", "420 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
			{"CD25", "410 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
			
			
	};
	
	private final static String[][]testDye2=new String[][]{
		{"CD43", "420 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
		{"CD4", "420 514/25","520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
		{"CD2", "420 514/25","520 614/25","520 625/25","520 645/25", "620 745/25"},
		{"CD90", "420 514/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25"},
		{"IgG1", "520 625/25","520 645/25", "620 745/25"},
		{"Ly-6", "420 514/25","420 525/25", "620 714/25","620 725/25"},
		{"CD143", "420 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
		{"CD25", "410 514/25","420 525/25","420 545/25", "520 614/25","520 625/25","520 645/25", "620 714/25","620 725/25","620 745/25"},
		{"IgG2", "520 625/25"},
		{"CD43a", "520 625/25"}
		
		
};
	
	private static final MapOfMany<String,String> convert(final String [][]input){
		final MapOfMany<String,String>output=new MapOfMany<String,String>(true, true);
		for (int i=0;i<input.length;i++){
			final String key=input[i][0];
			for (int j=1;j<input[i].length;j++){
				output.put(key, input[i][j]);
			}
		}
		return output;
	}

	static class Tester {
		public static void main(final String[] args) {
			/*
			 * final Args a=new Args(args); final
			 * MapOfMany<String,String>mom=new MapOfMany<String,String>(true,
			 * true); for (final String key:a.keySet()){ for (final String
			 * value:a.getStrings(key)){ mom.put(key, value);
			 * 
			 * } }
			 */
			Counter<String, String> counter = new Counter<String, String>(
					convert(testHaptens2));
			counter.verbose = true;
			counter.explore();
			System.out.println(counter.count);
			counter.exploreUnique();
			System.out.println(counter.count);
			counter.set(convert(testHaptens));
			counter.explore();
			System.out.println(counter.count);
			counter.exploreUnique();
			System.out.println(counter.count);

			TimeKeeper tk = new TimeKeeper();
			tk.reset("all visists");
			counter.set(convert(testDye));
			counter.verbose = testDye.length < 4;
			counter.explore();
			System.out.print(counter.count);
			System.out.println(" TOTAL visits");
			tk.stop();
			tk.reset("first unique visist");
			Map<String, String> m1 = counter.findFirstUnique();
			tk.stop();
			counter = new Counter<String, String>(convert(testDye2));
			tk.reset("first unique visist");
			Map<String, String> m2 = counter.findFirstUnique();
			tk.stop();
			tk.reset("unique visits");
			counter.exploreUnique();
			System.out.print(counter.count);
			System.out.println(" 	UNIQUE visits");
			tk.stop();
		}
	}
}

