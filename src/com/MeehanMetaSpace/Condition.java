package com.MeehanMetaSpace;
import java.util.*;

public class Condition implements Comparable{
	public static class Annotated {
		public final Condition condition;
		public final String annotation;
		protected Annotated(final Condition condition, final String annotation){
			this.condition=condition;
			this.annotation=annotation;
		}

	public String toString(){
	  return condition.equals(Condition.NORMAL )?
		  annotation:
		  "Condition="+condition.toString()+":  "+annotation;
	}
	}

	public Annotated annotate(final String annotation){
		return new Annotated(this, annotation);
	}

	private static final ArrayList all= new ArrayList();

	final String name;
	final int severity;

	private Condition(String name, int severity){
		this.name=name;
		this.severity=severity;
		all.add(this);
	}

	public final String toString(){
		return name;
	}

	public final static Condition
		 FATAL=new Condition("Fatal", 1000000),
		 CRITICAL=new Condition("Critical", 600000),
		 ERROR=new Condition("Error", 200000),
		 WARNING=new Condition("Warning", 100000),
		 SKIP=new Condition("Skip", 50000),
		 NORMAL=new Condition("Normal", 0),
                 STARTED=new Condition("Started", -50000),
		 OPTIMAL=new Condition("Optimal",   -100000),
		 FINISHED=new Condition("Finished", -200000);

		 ;

	public boolean isWorseThan(Condition condition){
		return compareTo(condition)>0;
	}

	public int compareTo(Object o){
		if (o instanceof Condition){
			Condition that=(Condition)o;
			if (severity < that.severity ) {
				return -1;
			} else if (severity > that.severity){
				return 1;
			}
			return 0;
		}
		return -1;
	}

	public final boolean equals(Object that){
		return super.equals(that);
	}
	public final int hashCode(){
		return super.hashCode();
	}

	public static Condition find(String name){
		Iterator it=all.iterator();
		while (it.hasNext()){
			Condition c=(Condition)it.next();
			if (c.name.equals(name)){
				return c;
			}
		}
		return null;
	}
}
