package com.MeehanMetaSpace;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CombinationCounter {


	private static Object[][] reduceObjectsThatEqualtoSameObject(
			final Object[][] input) {
		final Map<Object, Object> set = new HashMap<Object, Object>();
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				set.put(input[i][j], input[i][j]);
			}
		}
		final Object[][] in = new Object[input.length][];
		for (int i = 0; i < in.length; i++) {
			in[i] = new Object[input[i].length];
			for (int j = 0; j < in[i].length; j++) {
				Object o = set.get(input[i][j]);
				if (VERBOSE) {
					if (o != input[i][j]) {
						System.out.print(o + " != " + input[i][j]
								+ " but equals() ");
					}
				}
				in[i][j] = o;
			}
		}
		return in;

	}

    public static BigInteger max(final Object[][] in) {
    	final int k = in.length;
    	
    	final Set<Object> objects = new HashSet<Object>();
    	for (int i = 0; i < k; i++) {
    		for (int j = 0; j < in[i].length; j++)
    			objects.add(in[i][j]);
    	}
    	final int n = objects.size();
    	final BigInteger numerator=factorialB(n);
    	final BigInteger divisor=factorialB(n - k);
    	final long _numerator=numerator.longValue(), _divisor=divisor.longValue();
    	return numerator.divide(divisor);
    }
    
    public static BigInteger factorialB(int n) {
    	BigInteger m = BigInteger.ONE;
    	for (int i = 1; i <= n; i++) 
    		m = m.multiply(BigInteger.valueOf(i));
    	
    	return m;
    }


	private static BigInteger LONG_MAX=BigInteger.valueOf(Long.MAX_VALUE);
	
	public static long estimateMax(final Object[][] input) {
			final BigInteger bi=max(input);
			final int cmp=bi.compareTo(LONG_MAX);
			final long l=bi.longValue();
			if (cmp>0) {
				return Long.MAX_VALUE;
			}else{
				return l;
			}
	}

	public static int count(final Object[][] input) {
		if (input==null||input.length==0){
			return 0;
		}
		final Object[][] in = reduceObjectsThatEqualtoSameObject(input);
		int count = 0;
		final int[] cur = new int[in.length];
		for (int i = 0; i < cur.length; i++) {
			cur[i] = -1;
		}
		final Object[] combination = new Object[in.length];
		int i = 0;
		for (;;) {
			boolean alreadyInUse = true;
			if (i == cur.length) {
				count++;
				if (VERBOSE){
					for (int i2=0;i2<in.length;i2++){
						if (i2>0){
							System.out.print(", ");
						}
						int ii2=cur[i2];
						System.out.print(in[i2][ii2]);
						
					}
					System.out.println();
				}
				boolean haveVisitedAll = true;
				for (int j = 0; j < cur.length; j++) {
					if (cur[j] < in[j].length - 1) {
						haveVisitedAll = false;
						break;
					}
				}
				if (haveVisitedAll) {
					break;
				}
				i--;
			} else {
				cur[i] = cur[i] + 1;
				for (; cur[i] < in[i].length;) {
					alreadyInUse = false;
					Object o = in[i][cur[i]];
					for (int j = i - 1; j >= 0; j--) {
						if (o == combination[j]) {
							alreadyInUse = true;
							break;
						}
					}
					if (!alreadyInUse) {
						combination[i] = o;
						break;
					}
					cur[i] = cur[i] + 1;
				}
				if (!alreadyInUse) {
					i++;
				} else {
					if (i == 0) {
						break;
					}
					cur[i] = -1;
					i--;
				}
			}
		}
		return count;
	}
	public static boolean PLEASE_STOP=false;
	public static long count(
			final Object[][] input,
			final Object[][][] exclusions,
			final Object okToExclude) {
		if (input==null||input.length==0){
			return 0;
		}
		
		final Object[][] in = reduceObjectsThatEqualtoSameObject(input);
		long count = 0;
		final int[][] curEx = new int[in.length][];
		final int[] cur = new int[in.length];
		for (int i = 0; i < cur.length; i++) {
			cur[i] = -1;
			curEx[i] = new int[in[i].length];
			for (int ii = 0; ii < in[i].length; ii++) {
				if (exclusions[i][ii] != null) {
					curEx[i][ii] = -1;
				} else {
					curEx[i][ii] = -2;
				}
			}
		}
		final Object[] combination = new Object[in.length];
		int i = 0;
		final Set<Object> alreadyExcluded = new HashSet<Object>();
		for (;;) {
			if (PLEASE_STOP){
				break;
			}
			boolean alreadyInUse = true;
			if (i == cur.length) {
				count++;
				if (VERBOSE){
					for (int i2=0;i2<in.length;i2++){
						if (i2>0){
							System.out.print(", ");
						}
						int ii2=cur[i2];
						if (ii2 < in[i2].length){
							System.out.print(in[i2][ii2]);
							if (exclusions[i2][ii2]!=null){
								int iii2=curEx[i2][ii2];
								System.out.print("*");
								System.out.print(exclusions[i2][ii2][iii2].toString().trim());
							}
						}
					}
					System.out.println();
				}
				boolean haveVisitedAll = true;
				for (int j = 0; j < cur.length; j++) {
					if (cur[j] < in[j].length - 1) {
						haveVisitedAll = false;
						break;
					}
				}
				if (haveVisitedAll) {
					break;
				}
				i--;
			} else {
				int ii = cur[i];
				if (ii>=0 && ii < exclusions[i].length && exclusions[i][ii] != null) {
					if (exclusionsAreOkay(false, alreadyExcluded, exclusions[i][ii], curEx[i], ii, okToExclude)) {
						i++;
						continue;
					}
				}				
				cur[i] = cur[i] + 1;
				for (; cur[i] < in[i].length;) {
					ii = cur[i];
					alreadyInUse = false;
					Object o = in[i][ii];
					for (int j = i - 1; j >= 0; j--) {
						if (o == combination[j]) {
							alreadyInUse = true;
							break;
						}
					}
					if (!alreadyInUse) {
						combination[i] = o;
						if (exclusions[i][ii] != null) {
							if (exclusionsAreOkay(true, alreadyExcluded, exclusions[i][ii], curEx[i], 
									ii, okToExclude)) {
								break;
							} else {
								alreadyInUse=true;
							}
						} else {
							break;
						}
					}
					cur[i] = cur[i] + 1;
				}
				if (!alreadyInUse) {
					i++;
				} else {
					if (i == 0) {
						break;
					}
					cur[i] = -1;
					i--;
				}
			}
		}
		return count;
	}

	private static boolean exclusionsAreOkay(final boolean starting,
			final Set<Object> okToExclude, final Object[] exclusions,
			final int[]curEx, final int ii, final Object okExclusion) {
		boolean exclusionsOk = false;
		int iii = curEx[ii];
		if (starting || iii >= 0) {
			assert !starting || iii == -1;
			if (VERBOSE && !starting){
				System.out.print(" ");
			}
			Object excluded=null;
			if (iii>=0) {
				excluded = exclusions[iii];
				okToExclude.remove(excluded);
			}
			curEx[ii] = curEx[ii] + 1;
			final int n = exclusions.length;
			for (; curEx[ii] < n;) {
				iii = curEx[ii];
				excluded = exclusions[iii];
				if (okExclusion.equals(excluded)){
					exclusionsOk = true;
					break;
				}
				if (!okToExclude.contains(excluded)) {
					okToExclude.add(excluded);
					exclusionsOk = true;
					break;
				}
				curEx[ii] = curEx[ii] + 1;
			}
			if (!exclusionsOk) {
				curEx[ii] = -1;
			}
		}
		return exclusionsOk;
	}

	public static class Tester {
		private static void test(String[][] s) {
			int n3 = count(s);
			final String NO_HANDLE = "\tNO_HANDLE\t";
			String biotin[] = { "Biotin" };
			String biotinDnp[] = { NO_HANDLE, "Biotin", "DNP" };
			Object[][][] exclusions = new Object[s.length][][];
			for (int i = 0; i < s.length; i++) {
				exclusions[i] = new Object[s[i].length][];
				if (exclusions[i].length > 0) {
					if (i == 0) {
						int ii = 0;// s[i].length>2?1:0;
						exclusions[i][ii] = biotin;
					} else if (i == s.length - 1) {
						int ii = s[i].length - 1;
						exclusions[i][ii] = biotinDnp;
					}
				}
			}
			long n4 = count(s, exclusions, NO_HANDLE);
			for (int i = 0; i < s.length; i++) {
				exclusions[i] = new Object[s[i].length][];
				if (exclusions[i].length > 0) {
					if (i < 2) {
						for (int ii = 0; // s[i].length>2?1:0;
						ii < exclusions[i].length; ii++) {
							exclusions[i][ii] = biotin;
						}
					}
				}
			}
			long n5 = count(s, exclusions, NO_HANDLE);
			System.out.println("max possible without exclusions="
					+ estimateMax(s) + ", count without exclusions=" + n3
					+ ", with exclusions1=" + n4 + ", with exclusions2=" + n5);
			System.out.println();

		}

		public static void main(String[] args) {
			test(new String[][] { { "a", "b", "c" }, { "a", "b", "c" },
					{ "a", "b", "c" } });
			test(new String[][] {});
			test(new String[][] { {} });
			test(new String[][] { { "a", "b", "c" } });

			StringBuilder sb = new StringBuilder();
			sb.append('a');
			test(new String[][] { { "a", "b", "c" },
					{ sb.toString(), "b", "d" }, { "a", "d", "f" } });
			test(new String[][] { { "a", "b", "c" }, { "a", "b", "d" },
					{ "a", "d" } });
		}

	}

	private static final boolean VERBOSE=false;
}
