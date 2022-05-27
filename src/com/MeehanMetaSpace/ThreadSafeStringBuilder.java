package com.MeehanMetaSpace;

public class ThreadSafeStringBuilder {
	private boolean inUse = false;
	private final StringBuilder sb = new StringBuilder(250);
	public StringBuilder lock(){
		sb.setLength(0);
		inUse = true;
		return sb;		
	}
	
	public String unlockString() {
		final String value=sb.toString();
		unlock();
		return value;
	}

	public void unlock() {
		if (sb.capacity()>=2500){
  			threadLocal.remove();
  		}  		
		inUse = false;
	}

	private static ThreadLocal<ThreadSafeStringBuilder> threadLocal = new ThreadLocal<ThreadSafeStringBuilder>() {
		protected ThreadSafeStringBuilder initialValue() {
			return new ThreadSafeStringBuilder();
		}
	};

	public static ThreadSafeStringBuilder get() {
		ThreadSafeStringBuilder sb = threadLocal
				.get();
		if (sb.inUse) {
			sb = new ThreadSafeStringBuilder();
		}
		return sb;
	}
	
  	public static void clear(){
  		threadLocal.remove();  		 
  	}

}
