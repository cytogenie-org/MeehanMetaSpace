package com.MeehanMetaSpace;
import java.util.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class InterfaceBroker {

  private static final Map<Class<?>, Map<Class<?>,Class<?>>> all=new HashMap<Class<?>, Map<Class<?>,Class<?>>>();
  public static void registerImplementation(final Class<?> _interface, final Class<?> implementation){
	registerImplementation(_interface, Object.class, implementation);
  }

  public static void registerImplementation(final Class<?> _interface, final Class<?> cue, final Class<?> implementation){
	if (!_interface.isInterface()){
	  throw new IllegalArgumentException(_interface.getName()+" must be a JAVA interface");
	}
	if (implementation.isInterface()){
	  throw new IllegalArgumentException(implementation.getName()+" must be a JAVA class");
	}
	if (!_interface.isAssignableFrom(implementation)){
	  throw new IllegalArgumentException(
			 implementation.getName() +
			 " MUST implement " +
			 _interface.getName());
	}
	  Map<Class<?>,Class<?>> map=all.get(_interface);
	  if (map == null){
		map=new HashMap<Class<?>,Class<?>>();
		all.put(_interface, map);
	  }
	  map.put(cue, implementation);

  }

  public static boolean hasSpecificImplementation(final Class<?> _interface, final Class<?> cue){
	final Map<Class<?>, Class<?>> map=all.get(_interface);
	if (map!=null){
	  return map.containsKey(cue);
	}
	return false;
  }

  public static Class<?> getImplementationClass(final Class<?> _interface, final Class<?> cue){
	final Map<Class<?>, Class<?>> map=all.get(_interface);
	if (map!=null){
	  Class<?> best=null;
	  if (map.containsKey(cue)){
		best=cue;
	  } else {
		for (final Iterator<Class<?>> it=map.keySet().iterator(); it.hasNext(); ){
		  final Class<?> cl=it.next();
		  if (cue == null || cl.isAssignableFrom(cue)){
			if (best == null || best.isAssignableFrom(cl)){
			  best=cl;
			}
		  }
		}
	  }
	  if (best != null){
		return map.get(best);
	  }
	}
	return null;
  }

  public static Object getImplementation(final Class<?> _interface) {
	return getImplementation(_interface,null);
  }

  public static Object getImplementation(final Class<?> _interface, final Class<?> cue) {
	try{
	  final Class<?> cl=getImplementationClass(_interface, cue);
	  return cl == null ? null : cl.newInstance();
	} catch(Exception e){
	  System.err.println(e);
	}
	return null;
  }

interface Itf{
}
  static class Impl implements Itf{
  }

  static class RootClass {
  }
  static class SubClass extends RootClass{
  }
  static class SubSubClass extends SubClass {
  }
  static class LeafClass extends SubSubClass implements Itf{
  }

public static void main(String []args){
  try{
	registerImplementation(Itf.class, RootClass.class, SubClass.class);
  } catch (RuntimeException re){
	System.out.println(re);
  }
  try{
	registerImplementation(Impl.class, RootClass.class, SubClass.class);
  } catch (RuntimeException re){
	System.out.println(re);
  }
  try{
	registerImplementation(Itf.class, Itf.class);
  } catch (RuntimeException re){
	System.out.println(re);
  }
  registerImplementation(Itf.class, Impl.class);
  test(Itf.class, LeafClass.class);
  registerImplementation(Itf.class, LeafClass.class, LeafClass.class);
  test(Itf.class, SubClass.class);
  test(Itf.class, LeafClass.class);
}
  static void test(Class<?> _itf, Class<?> cue){
	final Itf impl=(Itf)getImplementation(_itf,cue);
	if (impl ==null){
	  System.err.println("No implementation of interface "+_itf.getName() + " found using cue " + cue.getName());
	} else {
	  System.out.println(impl.getClass().getName() + " implementation of interface "+_itf.getName() + " found using cue " + cue.getName());
	}
  }
}
