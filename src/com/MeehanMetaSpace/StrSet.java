package com.MeehanMetaSpace;

import java.util.*;

/* 4 reasons for this:
   1. need set behavior without excess memory isage of HashSet
     and to a lesser extent Treeset
   2. avoid converting String[] to Object[]
   3. use less memory re-allocating for collections that change less
   4. handle synonyms (not clear how to handle this with interface Comparable.compareTo())
   5. support JDK 1.1 on Mac OS.9
 */


public class StrSet implements Cloneable {
  public String[] set;

  public StrSet() {
    set = new String[0];
  }

  public StrSet(final String s) {
    if (s == null) {
      set = new String[0];
    } else {
      this.set = new String[1];
      this.set[0] = s;
    }
  }

  public StrSet(final String[] s) {
    this.set = s == null ?
               new String[0] :
               Basics.removeDuplicatesAndNulls(s);
  }

  /**
   * @return index of string in container array that equals lookFor
   * @parameter container string array to look up
   * @parameter lookFor string to look for in container
   */
  public static int indexOf(final String[] container, final String lookFor) {
    if (container != null) {
      for (int i = 0; i < container.length; i++) {
        if (lookFor.equals(container[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  private boolean immutable = false;
  public void setImmutable(final boolean immutable) {
    this.immutable = immutable;
  }


  public int indexOf(final String f) {
    for (int i = 0; i < this.set.length; i++) {
      if (f.equals(this.set[i])) {
        return i;
      }
    }
    return -1;
  }

  public String get(final int i) {
    return set[i];
  }

  public boolean contains(final String searchArg) {
    if (searchArg != null) {
      for (int i = 0; i < this.set.length; i++) {
        if (searchArg.equals(this.set[i])) {
          return true;
        }
      }
    }
    return false;
  }

  public Object clone() {
    Object o = null;
    try {
      o = super.clone();
    } catch (CloneNotSupportedException e) {}
    return o;
  }

  public void add(final String[] _s) {
    if (immutable) {
      throw new IllegalStateException("Can not alter this StrSet");
    }
    ArrayList c;
    int n = 0;
    final String[] s = Basics.removeDuplicatesAndNulls(_s);
    for (int i = 0; i < s.length; i++) {
      if (!contains(s[i])) {
        n++;
      }
    }
    int l = set.length;
    String[] newSet = new String[l + n];
    for (int i = 0; i < this.set.length; i++) {
      newSet[i] = this.set[i];
    }
    for (int i = 0, j = 0; i < s.length; i++) {
      if (!contains(s[i])) {
        newSet[l + j] = s[i];
        j++;
      }
    }
    set = newSet;
  }

  public String[] reduceToSubsetOf(final StrSet that) {
    int n = this.set.length;
    for (int i = 0; i < set.length; i++) {
      if (!that.contains(set[i])) {
        n--;
      }
    }
    if (n == set.length) {
      return null;
    }
    String[] outsideSubSet = new String[set.length - n];
    String[] subSet = new String[n];
    for (int i = 0, j = 0, e = 0; i < set.length; i++) {
      if (!that.contains(set[i])) {
        outsideSubSet[e++] = set[i];
      } else {
        subSet[j++] = set[i];
      }
    }
    set = subSet;
    return outsideSubSet;
  }

  public String[] getMissingFrom(final StrSet that) {
      int n = this.set.length;
      for (int i = 0; i < set.length; i++) {
        if (!that.contains(set[i])) {
          n--;
        }
      }
      if (n == set.length) {
        return new String[0];
      }
      String[] outsideSubSet = new String[set.length - n];
      for (int i = 0, j = 0, e = 0; i < set.length; i++) {
        if (!that.contains(set[i])) {
          outsideSubSet[e++] = set[i];
        }
      }
      return outsideSubSet;
    }

  public boolean remove(final String f) {
    int idx = this.indexOf(f);
    if (idx > -1) {
      String[] newSet = new String[this.set.length - 1];
      for (int i = 0; i < idx; i++) {
        newSet[i] = this.set[i];
      }
      for (int i = idx + 1; i < this.set.length; i++) {
        newSet[i - 1] = this.set[i];
      }
      this.set = newSet;
      return true;
    }
    return false;
  }

  public void add(final String f) {
    if (immutable) {
      throw new IllegalStateException("Can not alter this StrSet");
    }

    if (f != null) {
      int idx = this.indexOf(f);
      if (idx < 0) {
        String[] newSet = new String[this.set.length + 1];
        for (int i = 0; i < this.set.length; i++) {
          newSet[i] = this.set[i];
        }
        newSet[this.set.length] = f;
        this.set = newSet;
      }
    }
  }


  public void clear() {
    if (immutable) {
      throw new IllegalStateException("Can not alter this StrSet");
    }

    this.set = new String[0];
  }

  public static StrSet[] toArray(final String[][] sa) {
    StrSet[] ssa = new StrSet[sa.length];
    for (int i = 0; i < sa.length; i++) {
      ssa[i] = new StrSet(sa[i]);
    }
    return ssa;
  }

  public String toString() {
    return Basics.toString(set);
  }

  public static void main(final String[] args) {
    System.out.println(new StrSet(new String[] {
                                  "hi",
                                  "there",
                                  null,
                                  "Lee",
                                  "how",
                                  "are",
                                  "there",
                                  "you"
    }));

    System.out.println(new StrSet(new String[] {
                                  "hi",
                                  "there",
                                  "Lee",
                                  "how",
                                  "are",
                                  "you"
    }));

  }
}


