package com.MeehanMetaSpace;
import java.util.*;

public class Accumulator extends HashMap<Object, Double> {


    public void clear(final Object key){
        if (containsKey(key)) {
            put(key, 0.0);
        }
    }

    public void accumulate(final Object key, final double amount){
        final Double i=get(key);
        if (i==null){
            put(key, amount);
        } else {
            put(key,i+amount);
        }
    }

    public Double getAmount(final Object key){
        final Double i=get(key);
        if (i==null){
            return null;
        }
        return i;
    }

}
