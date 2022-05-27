package com.MeehanMetaSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class DeepEquals {

   /**
    * @param args
    */
   public static void main(String args[]) {

       ArrayList<Object> stuff1 = new ArrayList<Object>();
       stuff1.add(null);
       stuff1.add("A");
       stuff1.add(new Object[] { "B", "C", null });

       ArrayList<Object> stuff2 = new ArrayList<Object>();
       stuff2.add(null);
       stuff2.add("A");
       stuff2.add(new Object[] { "B", "C", null });

       TreeSet<Integer> ts1 = new TreeSet<Integer>();
       ts1.add(1);
       ts1.add(3);
       ts1.add(2);

       TreeSet<Integer> ts2 = new TreeSet<Integer>();
       ts2.add(1);
       ts2.add(2);
       ts2.add(3);
       // ts2.add(4);
     
       HashMap<String, Object> map1 = new HashMap<String, Object>();
       HashMap<String, Object> map2 = new HashMap<String, Object>();

       map1.put("key1", new Object[] { ts1, "hi", 123, 456 });
       map2.put("key1", new Object[] { ts2, "hi", 123, 456 });

       stuff1.add(map1);
       stuff2.add(map2);

       System.out.println("Result = " + deepEquals(stuff1, stuff2));
   }

   /**
    * An implementation of "deep equals" for collection classes, built in the
    * Arrays.deepEquals() style. It attempts to compare equality based on the
    * contents of the collection.
    *
    * @param t1 -
    *            first object, most likely a collection of some sort.
    * @param t2 -
    *            second object, most likely a collection of some sort.
    * @return - true if the content of the collections are equal.
    */
   public static <T> boolean deepEquals(T t1, T t2) {

       if (t1 == t2) {
           return true;
       }

       if (t1 == null || t2 == null) {
           return false;
       }
       if (t1 instanceof MapOfMany && t2 instanceof MapOfMany) {
           return mapOfManyDeepEquals((MapOfMany<?, ?>) t1, (MapOfMany<?, ?>) t2);
       } else   if (t1 instanceof MapOfMaps && t2 instanceof MapOfMaps) {
           return mapOfMapsDeepEquals((MapOfMaps<?, ?, ?>) t1, (MapOfMaps<?, ?, ?>) t2);
       } else  if (t1 instanceof Map && t2 instanceof Map) {
           return mapDeepEquals((Map<?, ?>) t1, (Map<?, ?>) t2);
       } else if (t1 instanceof List && t2 instanceof List) {
           return linearDeepEquals((List<?>) t1, (List<?>) t2);

       } else if (t1 instanceof Set && t2 instanceof Set) {
           return linearDeepEquals((Set<?>) t1, (Set<?>) t2);

       } else if (t1 instanceof Object[] && t2 instanceof Object[]) {
           return linearDeepEquals((Object[]) t1, (Object[]) t2);

       } else {
           return t1.equals(t2);
       }
   }

   /**
    * Compares two maps for equality. This is based around the idea that if the
    * keys are deep equal and the values the keys return are deep equal then
    * the maps are equal.
    *
    * @param m1 -
    *            first map
    * @param m2 -
    *            second map
    * @return - weather the maps are deep equal
    */
   private static boolean mapDeepEquals(Map<?, ?> m1, Map<?, ?> m2) {
       if (m1.size() != m1.size()) {
           return false;
       }

       Set<?> allKeys = m1.keySet();
       if (!linearDeepEquals(allKeys, m2.keySet())) {
           return false;
       }

       for (Object key : allKeys) {
           if (!deepEquals(m1.get(key), m2.get(key))) {
               return false;
           }
       }
       return true;
   }

   /**
    * Compares two Collections for deep equality.
    *
    * @param s1
    * @param s2
    * @return
    */
   private static boolean linearDeepEquals(Collection<?> s1, Collection<?> s2) {
       if (s1.size() != s2.size()) {
           return false;
       }

       for (Object s1Item : s1) {
           boolean found = false;
           for (Object s2Item : s2) {
               if (deepEquals(s2Item, s1Item)) {
                   found = true;
                   break;
               }
           }
           if (!found) {
               return false;
           }
       }
       return true;
   }

   /**
    * Compares two Object[] for deep equality
    *
    * @param s1
    * @param s2
    * @return
    */
   private static boolean linearDeepEquals(Object[] s1, Object[] s2) {

       if (s1.length != s2.length) {
           return false;
       }

       for (Object s1Item : s1) {
           boolean found = false;
           for (Object s2Item : s2) {
               if (deepEquals(s2Item, s1Item)) {
                   found = true;
                   break;
               }
           }
           if (!found) {
               return false;
           }
       }
       return true;
   }
   
   public static boolean mapOfMapsDeepEquals(final MapOfMaps<?,?,?> m1, final MapOfMaps<?,?,?> m2)  {
	   final Set s1=m1.keySet(), s2=m2.keySet();
       if (!linearDeepEquals(s1, s2)) {
           return false;
       }

       if (!linearDeepEquals(m1.values(), m2.values())) {
           return false;
       }

       return true;
   }

   public static boolean mapOfManyDeepEquals(final MapOfMany<?,?> m1, final MapOfMany<?,?> m2)  {
	   final Set s1=m1.keySet(), s2=m2.keySet();
       if (!linearDeepEquals(s1, s2)) {
           return false;
       }

       if (!linearDeepEquals(m1.values(), m2.values())) {
           return false;
       }

       return true;
   }

}
