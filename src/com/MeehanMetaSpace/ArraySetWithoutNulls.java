package com.MeehanMetaSpace;
import java.lang.reflect.Array;
import java.util.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class ArraySetWithoutNulls<E>
    implements Cloneable, Set<E>{

	private class _iterator<E>
      implements Iterator<E>{
    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext(){
      return idx<array.length;
    }

    /**
     * Returns the next element in the iteration.
     * @return the next element in the iteration.
     */
    @SuppressWarnings("unchecked")
	public E next(){

      if (!hasNext()){
        throw new IllegalStateException("No more elements to iterate");
      }


      return (E)array[idx++];
    }

    /**
     * 
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @exception IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    public void remove(){
    	if (idx==0){
    		throw new IllegalStateException("next() method has not yet been called");
    	}
    	if (idx>=array.length){
    		throw new IllegalStateException("remove() is being called after last call to next()");
    	}

      if (idx>0){
        ArraySetWithoutNulls.this.remove(array[idx-1]);
      }
    }

    private int idx=0;

  }
  public boolean equals(final Object o){
  if (o==this){
    return true;
  }
  if ( !(o instanceof ArraySetWithoutNulls<?>)){
    return false;
  }
  return Basics.equals(array, ( (ArraySetWithoutNulls<?>) o).array);
}

  public int hashCode(){
	  return Arrays.deepHashCode(array);
  }

  private Object[] array;
  private final Basics.ArrayAllocater arrayAllocater;
  public ArraySetWithoutNulls(){
    this(Basics.getObjectArrayAllocater());
  }

  public ArraySetWithoutNulls(final Basics.ArrayAllocater arrayAllocater){
    this.arrayAllocater=arrayAllocater;
    array=arrayAllocater.allocate(0);
  }

  public ArraySetWithoutNulls(final Basics.ArrayAllocater arrayAllocater, final Object object){
    this.arrayAllocater=arrayAllocater;
    if (object == null){
      array=arrayAllocater.allocate(0);
    }
    else{
      array=arrayAllocater.allocate(1);
      array[0]=object;
    }
  }

  public ArraySetWithoutNulls(final Basics.ArrayAllocater arrayAllocater, final Object[] objects){
    this.arrayAllocater=arrayAllocater;
    array=objects == null ?
        arrayAllocater.allocate(0) :
        Basics.removeDuplicatesAndNulls(objects);
  }

  /**
   * @return index of string in container array that equals lookFor
   * @parameter container string array to look up
   * @parameter lookFor string to look for in container
   */
  public static int indexOf(final Object[] container, final Object lookFor){
    if (container != null){
      for (int i=0; i < container.length; i++){
        if (lookFor.equals(container[i])){
          return i;
        }
      }
    }
    return -1;
  }

  private boolean immutable=false;
  public void setImmutable(final boolean immutable){
    this.immutable=immutable;
  }

  public int indexOf(final Object f){
    for (int i=0; i < array.length; i++){
      if (f.equals(array[i])){
        return i;
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
public E get(int i){
    return (E)array[i];
  }

  /**
   * Returns <tt>true</tt> if this set contains the specified element.
   * More formally, returns <tt>true</tt> if and only if this set
   * contains an element <tt>e</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
   *
   * @param o element whose presence in this set is to be tested
   * @return <tt>true</tt> if this set contains the specified element
   */

  public boolean contains( final Object searchArg){
    return Basics.contains(array, searchArg);
  }

  public Object clone(){
    Object o=null;
    try{
      o=super.clone();
    }
    catch (CloneNotSupportedException e){}
    return o;
  }

  public void add(Object[] s){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }
    int n=0;
    s=Basics.removeDuplicatesAndNulls(arrayAllocater, s);
    for (int i=0; i < s.length; i++){
      if (!contains(s[i])){
        n++;
      }
    }
    int l=array.length;
    final Object[] newSet=arrayAllocater.allocate(l + n);
    for (int i=0; i < array.length; i++){
      newSet[i]=array[i];
    }
    for (int i=0, j=0; i < s.length; i++){
      if (!contains(s[i])){
        newSet[l + j]=s[i];
        j++;
      }
    }
    array=newSet;
  }

  public Object[] reduceToSubsetOf(final ArraySetWithoutNulls that){
    int n=array.length;
    for (int i=0; i < array.length; i++){
      if (!that.contains(array[i])){
        n--;
      }
    }
    if (n == array.length){
      return null;
    }
    final Object[] outsideSubSet=arrayAllocater.allocate(array.length - n);
    final Object[] subSet=arrayAllocater.allocate(n);
    for (int i=0, j=0, e=0; i < array.length; i++){
      if (!that.contains(array[i])){
        outsideSubSet[e++]=array[i];
      }
      else{
        subSet[j++]=array[i];
      }
    }
    array=subSet;
    return outsideSubSet;
  }

  /**
   * Removes the specified element from this set if it is present
   * (optional operation).  More formally, removes an element <tt>e</tt>
   * such that
   * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
   * this set contains such an element.  Returns <tt>true</tt> if this set
   * contained the element (or equivalently, if this set changed as a
   * result of the call).  (This set will not contain the element once the
   * call returns.)
   *
   * @param o object to be removed from this set, if present
   * @return <tt>true</tt> if this set contained the specified element
   * @throws ClassCastException if the type of the specified element
   *         is incompatible with this set (optional)
   * @throws NullPointerException if the specified element is null and this
   *         set does not permit null elements (optional)
   * @throws UnsupportedOperationException if the <tt>remove</tt> operation
   *         is not supported by this set
   */
  public boolean remove(final Object f){
    final int idx=indexOf(f);
    if (idx > -1){
      final Object[] newSet=arrayAllocater.allocate(array.length - 1);
      for (int i=0; i < idx; i++){
        newSet[i]=array[i];
      }
      for (int i=idx + 1; i < array.length; i++){
        newSet[i - 1]=array[i];
      }
      array=newSet;
      return true;
    }
    return false;
  }

  /**
   * Adds the specified element to this set if it is not already present
   * (optional operation).  More formally, adds the specified element
   * <tt>e</tt> to this set if the set contains no element <tt>e2</tt>
   * such that
   * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
   * If this set already contains the element, the call leaves the set
   * unchanged and returns <tt>false</tt>.  In combination with the
   * restriction on constructors, this ensures that sets never contain
   * duplicate elements.
   *
   * <p>The stipulation above does not imply that sets must accept all
   * elements; sets may refuse to add any particular element, including
   * <tt>null</tt>, and throw an exception, as described in the
   * specification for {@link Collection#add Collection.add}.
   * Individual set implementations should clearly document any
   * restrictions on the elements that they may contain.
   *
   * @param e element to be added to this set
   * @return <tt>true</tt> if this set did not already contain the specified
   *         element
   * 
   */
  public boolean add(final Object f){
      return add(f, false);
  }

  public boolean add(final Object f, final boolean prepend){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }

    if (f != null){
      final int idx=indexOf(f);
      if (idx < 0){
        final Object[] newSet=arrayAllocater.allocate(array.length + 1);
        if (prepend){
            newSet[0] = f;
            for (int i = 1; i <= array.length; i++) {
                newSet[i] = array[i-1];
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                newSet[i] = array[i];
            }
            newSet[array.length] = f;
        }
        array=newSet;
        return true;
      }
    }
    return false;
  }

  public boolean retainAll(final Object[] sub){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }
    return Basics.retainAll(arrayAllocater, array, sub)!=array;
  }
  /**
   * Removes all of the elements from this set (optional operation).
   * The set will be empty after this call returns.
   *
   */
  public void clear(){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }

    array=arrayAllocater.allocate(0);
  }

  @SuppressWarnings("unchecked")
public static ArraySetWithoutNulls[] toArray(final Basics.ArrayAllocater arrayAllocater, final Object[][] sa){
    ArraySetWithoutNulls[] ssa=new ArraySetWithoutNulls[sa.length];
    for (int i=0; i < sa.length; i++){
      ssa[i]=new ArraySetWithoutNulls(arrayAllocater, sa[i]);
    }
    return ssa;
  }

  public String toString(){
    return Basics.toString(array);
  }


  /**
   * Returns the number of elements in this set (its cardinality).
   *
   * @return the number of elements in this set (its cardinality).
   */
  public int size(){
    return array.length;
  }

  /**
   * Returns <tt>true</tt> if this set contains no elements.
   *
   * @return <tt>true</tt> if this set contains no elements.
   * @todo Implement this java.util.Set method
   */
  public boolean isEmpty(){
    return array.length==0;
  }

  /**
   * Returns an iterator over the elements in this set.
   *
   * @return an iterator over the elements in this set.
   */
  public Iterator<E> iterator(){
    return new _iterator<E>();
  }

  /**
   * Returns an array containing all of the elements in this set.
   *
   * @return an array containing all of the elements in this set.
   */
  public Object[] toArray(){
	final Object[] _a=new Object[array.length];
	System.arraycopy(array, 0, _a, 0, array.length);
    return _a;
  }

  
  public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
      T[] copy = ((Object)newType == (Object)Object[].class)
          ? (T[]) new Object[newLength]
          : (T[]) Array.newInstance(newType.getComponentType(), newLength);
      System.arraycopy(original, 0, copy, 0,
                       Math.min(original.length, newLength));
      return copy;
  }

  /**
   * Returns an array containing all of the elements in this set; the runtime
   * type of the returned array is that of the specified array.
   *
   * @param a the array into which the elements of this set are to be stored, if
   *   it is big enough; otherwise, a new array of the same runtime type is
   *   allocated for this purpose.
   * @return an array containing the elements of this set.
   */
public <T>T[] toArray(final T[] a){
      if (a.length < array.length){          // Make a new array of a's runtime type, but my contents:
          return (T[]) copyOf(array, array.length, a.getClass());
      }
	System.arraycopy(array, 0, a, 0, array.length);
      if (a.length > array.length){
          a[array.length] = null;
    }
    return a;

  }

/**
 * Returns <tt>true</tt> if this set contains all of the elements of the
 * specified collection.  If the specified collection is also a set, this
 * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
 *
 * @param  c collection to be checked for containment in this set
 * @return <tt>true</tt> if this set contains all of the elements of the
 * 	       specified collection
 * @throws ClassCastException if the types of one or more elements
 *         in the specified collection are incompatible with this
 *         set (optional)
 * @throws NullPointerException if the specified collection contains one
 *         or more null elements and this set does not permit null
 *         elements (optional), or if the specified collection is null
 * @see    #contains(Object)
 */
/**
   * Returns <tt>true</tt> if this set contains all of the elements of the
   * specified collection.
   *
   * @param c collection to be checked for containment in this set.
   * @return <tt>true</tt> if this set contains all of the elements of the
   *   specified collection.
   */
  public boolean containsAll(final Collection<?> c){
    for (final Iterator<?> it = c.iterator(); it.hasNext(); ) {
      if (!Basics.contains(array, it.next())){
        return false;
      }
    }
    return true;
  }

  /**
   * Adds all of the elements in the specified collection to this set if they're
   * not already present (optional operation).
   *
   * @param c collection whose elements are to be added to this set.
   * @return <tt>true</tt> if this set changed as a result of the call.
   */
  public boolean addAll(final Collection<? extends E> c){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }

    boolean changed=false;
    for (final Iterator<? extends E> it=c.iterator(); it.hasNext(); ){
      if (add(it.next())){
        changed=true;
      }
    }
    return changed;
  }

  /**
   * Retains only the elements in this set that are contained in the specified
   * collection (optional operation).
   *
   * @param c collection that defines which elements this set will retain.
   * @return <tt>true</tt> if this collection changed as a result of the call.
   */
  public boolean retainAll(final Collection<?> c){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }
    return retainAll(c.toArray());
  }

  /**
   * Removes from this set all of its elements that are contained in the
   * specified collection (optional operation).
   *
   * @param c collection that defines which elements will be removed from this
   *   set.
   * @return <tt>true</tt> if this set changed as a result of the call.

   */
  public boolean removeAll(final Collection<?> c){
    if (immutable){
      throw new IllegalStateException("Can not alter this ArraySetWithoutNulls");
    }


    boolean changed=false;
    for (final Iterator<?> it=c.iterator(); it.hasNext(); ){
      if (remove(it.next())){
        changed=true;
      }
    }
    return changed;
  }

  private static Collection cc;
  public static void main(final String[] args){
	  ArraySetWithoutNulls<String>as=new ArraySetWithoutNulls<String>();
	  as.add("foo");
	  as.add("foobar");
	  as.add("foo");
	  Object []d=as.toArray();
	  String []ass=as.toArray(new String[1]);
	  ass=as.toArray(new String[2]);
	  ass=as.toArray(new String[3]);
	  
    ArraySetWithoutNulls a=new ArraySetWithoutNulls(
      Basics.getStringArrayAllocater(),
      new String[]{"hi","there",null,"Lee","how","are","there","you"
    });

    System.out.println("ArraySetWithoutNulls.toString() "+a);
    System.out.println("Basics.toString(Collection) "+Basics.toString(a));
    a=new ArraySetWithoutNulls(
          Basics.getStringArrayAllocater(),
          new String[]{"hi","there","Lee","how","are","you"
        });
    System.out.println("ArraySetWithoutNulls.toString() "+a);
    System.out.println("Basics.toString(Collection) "+Basics.toString(a));
    System.out.println("ArraySetWithoutNulls.toString() "+new ArraySetWithoutNulls(Basics.getObjectArrayAllocater(), null));
    System.out.println("ArraySetWithoutNulls.toString() "+new ArraySetWithoutNulls(Basics.getObjectArrayAllocater(), System.getProperties()));
    a.removeAll(Arrays.asList(new String[]{"are"}));
    System.out.println("Removed are "+a);
    a.removeAll(Arrays.asList(new Object[]{"there"}));
    System.out.println("Removed there "+a);
    a.retainAll(Arrays.asList(new Object[]{"hi", "you"}));
    System.out.println("Retained hi you "+a);

  }

}
