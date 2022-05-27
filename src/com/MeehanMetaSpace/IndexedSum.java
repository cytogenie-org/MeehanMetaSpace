package com.MeehanMetaSpace;
import java.util.*;

public final class IndexedSum {
	private final Map hm=new TreeMap();

  public String toString(){
	return Basics.toString(hm);
}

	public void add(
			final Object [] o,
			final boolean []keyScopeIsRelative,
			final Number sum){
		for (int i=0;i<o.length;i++){
			add(key(o, keyScopeIsRelative, i+1),sum.doubleValue());
		}
	}

	public void withdraw(
			final Object [] o,
			final boolean[]keyScopeIsRelative,
			final Number sum){
			for (int i=0;i<o.length;i++){
				withDraw(key(o, keyScopeIsRelative, i+1), sum.doubleValue());
			}
		}

	public double getDouble(
			final Object []sortOrder,
			final boolean []keyScopeIsRelative){
		Double whammy=(Double)hm.get(key(sortOrder, keyScopeIsRelative));
		return whammy==null?0:whammy.doubleValue();
	}

	public long getLong(
			final Object []sortOrder,
			final boolean []keyScopeIsRelative){
		final Double whammy=(Double)hm.get(key(sortOrder, keyScopeIsRelative));
		return whammy==null?0:whammy.longValue();
	}


	public static String key(
			final Object []sortOrder,
			final boolean []keyScopeIsRelative){
		return key(sortOrder, keyScopeIsRelative, sortOrder.length);
	}

	public static String key(
			final Object []sortOrder,
			final boolean [] keyScopeIsRelative,
			final int length){
		final boolean isRelative=keyScopeIsRelative[length-1];
		final StringBuilder sb=new StringBuilder(sortOrder.length*10);
		for (int i=0;i<length;i++){
			if (i>0){
				sb.append('.');
			}
			sb.append(!isRelative && i<(length-1) ? null : sortOrder[i]);
		}
		return sb.toString();
	}

	private void add(final String key, final double sum){
		Double whammy=(Double)hm.get(key);
		if (whammy == null){
			whammy=new Double(sum);
		} else {
			whammy=new Double(whammy.doubleValue() + sum);
		}
		hm.put(key, whammy);
	}

	private void withDraw(final String key, final double sum){
		Double whammy=(Double)hm.get(key);
		if (whammy == null){
			return;
		} else {
			whammy=new Double(whammy.doubleValue() - sum);
		}
		hm.put(key, whammy);
	}

}
