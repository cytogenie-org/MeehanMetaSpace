package com.MeehanMetaSpace;

import java.util.*;
import java.util.Properties;


public class Args{

  public static void apply(final Args fromSource, final Properties toTarget){
	for (final Iterator it=fromSource.hm.keySet().iterator(); it.hasNext(); ){
	  final Object key=it.next();
	  toTarget.setProperty(key.toString(),
			   Basics.toString(fromSource.hm.get(key)));
	}
  }

  public static void applyDefaultsIfNull(
	  final Properties fromSource,
	  final Expect[] toTarget
	  ){
	for (int i=0; i < toTarget.length; i++){
	  if (toTarget[i].defaultValue == null){
	final String value=fromSource.getProperty(toTarget[i].key);
	toTarget[i].defaultValue=value;
	  }
	}
  }

  public static class Expect{
		public final String key;
		public String defaultValue;
		public String help;
		public Expect(
			  final String key,
			  final String defaultValue,
			  final String help){
			this.key=key;
			this.defaultValue=defaultValue;
			this.help=help;
		}
	};

	final HashMap<String, ArrayList<String>> hm=new HashMap<String, ArrayList<String>>();
	boolean usageAskedFor=false;
public boolean getUsageAskedFor(){
  return usageAskedFor;
}
	public String toHtml(){
	  final StringBuilder sb=new StringBuilder("<ol>");
	  for (final Iterator it=hm.keySet().iterator();it.hasNext();){
		final Object key=it.next();
		sb.append("<li><b>");
		sb.append(key);
		sb.append("</b>=<i>");
		sb.append(Basics.toString(hm.get( key) ));
	sb.append("</i>");
	  }
	  sb.append("</ol>");
	  return sb.toString();
	}

	public Args(	final String []args ){
		String key=null, value="";
		if (args != null){
		  for (int i = 0; i < args.length; i++) {

			if (args[i].startsWith("-")) {
			  put(key, value);
			  key = args[i].substring(1);
			  if (key.equals("?")) {
				usageAskedFor = true;
			  }
			  value = "";
			}
	else {
			  if (value != "") {
				value += " ";
			  }
			  value += args[i];
			}
		  }

		  if (key != null) {
			put(key, value);
		  }
		}
	}

  public final Set<String> keySet(){
	return hm.keySet();
}

  public boolean hasArg(final String key){
	if (key != null){
	  return hm.containsKey(key);
	}
	return false;
  }

	public void put(final String key, final String value){
		if (key != null){
			ArrayList<String> al=hm.get(key);
			if (al==null){
				al=new ArrayList<String>();
				hm.put(key, al);
			}
			al.add( value );
		}
	}

	public String checkUsage( final Expect [] expect ){
		final StringBuilder sb=new StringBuilder();
		int errors=0;
		if (!usageAskedFor){
			for (int i = 0; i < expect.length; i++) {
				if (!hm.containsKey(expect[i].key)) {
					if (expect[i].defaultValue == null) {

                        if (! (expect[i] instanceof ArgDemand) || ( (ArgDemand)expect[i]).isNeeded(this)){
                            if (errors > 0) {
                                sb.append(", ");
                            }
                            errors++;
                            sb.append(expect[i].key);
                        }
					}
					else {
						put(expect[i].key, expect[i].defaultValue);
					}
				}
			}
		}
		if (errors>0 || usageAskedFor){
			final StringBuilder answer=new StringBuilder();
			if (errors>0){
				answer.append(errors);
				answer.append(" errors!");
				answer.append("\nMissing argument(s):\n\t");
				answer.append(sb.toString());
			}
			return getUsage(answer, expect);
		}
		return null;
	}

	public String getUsage(final Expect [] expect ){
	  return getUsage(new StringBuilder(), expect);
	}

	private String getUsage(final StringBuilder answer, final Expect[] expect){
	  answer.append("\n\nUsage: invoke with the following argument flags\n");
	  for (int i=0; i < expect.length; i++){
		answer.append("\t -");
		answer.append(expect[i].key);
		if (expect[i].defaultValue != null){
		  answer.append(" (default ");
		  answer.append(expect[i].defaultValue);
		  answer.append(")");
		}
		answer.append(":  ");
		answer.append(expect[i].help);
		answer.append("\n");
	  }
	  return answer.toString();
	}

	public static String toString(final Iterator it){
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; it.hasNext(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			final Object o = it.next();
			sb.append(o);
		}
		return sb.toString();
	}

  public ArrayList<String> remove(final String key){
	return hm.remove(key);
  }
	public String get(final String key){
		if (hm.containsKey(key)){
			return toString(hm.get(key).iterator());
		}
		return null;
	}

	public String getTrimmed(final String key){
		if (hm.containsKey(key)){
			return toString(hm.get(key).iterator()).trim();
		}
		return null;
	}

	public String []getStrings(final String key){
		if (hm.containsKey(key)){
			final Collection<String> c=hm.get(key);
			return c.toArray(new String[c.size() ]);
		}
		return new String[0];
	}

	public boolean getBoolean(final String key){
		if (hm.containsKey(key)){
		  final Collection<String> c=hm.get(key);
		  final String s=c.iterator().next();
		return Boolean.valueOf(s ).booleanValue();
		}
		return false;
	}

	public static String []split(String source, String delimiters){
			char [] s=source.toCharArray();
			int l=0,i=0;
			ArrayList al=new ArrayList();
			for(;i<s.length;i++){
				if ( delimiters.indexOf(s[i])>=0) {
					al.add(i>l?source.substring(l, i):"");
					if (i==s.length){
						break;
					}
					l=i+1;
				}
			}
			al.add(i>l?source.substring(l, i):"");
			return (String []) al.toArray( new String[al.size()]);
		}

}
