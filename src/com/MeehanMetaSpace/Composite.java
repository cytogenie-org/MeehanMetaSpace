package com.MeehanMetaSpace;
import java.util.*;

public class Composite implements Comparable{
    public final ArrayList list = new ArrayList();
    public Composite(final java.util.List list) {
    	this.list.addAll(list);
    }
        
    public Composite(final Object ...objects) {
        for (final Object o:objects){
            if (o instanceof Collection){
                list.addAll((Collection)o);
            }else {
                list.add(o);
            }
        }
        String debug=toString();
        int debug2=2;
    }

    public void add(final Object o) {
        list.add(o);
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Composite)) {
            return false;
        }
        return Basics.equals(list, ((Composite) o).list);
    }

    public int hashCode() {
        int result = 17;
        for (final Object o : list) {
            result = 37 * result + (o==null?0:o.hashCode());
        }
        return result;
    }

    private int compareTo(final Object o1, final Object o2) {
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
        }
        if (o1 instanceof Comparable) {
            return 1;
        }
        if (o2 instanceof Comparable) {
            return -1;
        }
        return 0;
    }

    private int compareTo(final List l) {
        int value = 0;

        final int n1 = list.size(),
                       n2 = l.size(),
                            n = n2 > n1 ? n2 : n1;
        for (int i = 0; i < n; i++) {
            final Object o1 = list.get(i), o2 = l.get(i);
            value = compareTo(o1, o2);
            if (value != 0) {
                return value;
            }
        }
        assert value == 0;
        if (n1 > n2) {
            return 1;
        } else if (n2 > n1) {
            return -1;
        }
        return 0;
    }

    public int compareTo(final Object o) {
        if (o instanceof Composite) {
            return compareTo(((Composite) o).list);
        } else if (o instanceof Comparable) {

        } else if (o instanceof List) {
            return compareTo((List) o);
        }
        return 0;
    }


    public String toString(){
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final Object o : list) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(o);
            i++;
        }
        return sb.toString();
    }

}

