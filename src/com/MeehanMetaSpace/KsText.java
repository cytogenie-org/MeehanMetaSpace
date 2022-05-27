package com.MeehanMetaSpace;

import java.io.*;
import java.util.*;


public class KsText {

	private String ksUrl;
	
	public KsText(String ksUrl) {
		this.ksUrl = ksUrl;
	}
	
	static public KsText getNew(String ksUrl) {
		return ksUrl==null? null : new KsText(ksUrl);
	}
/*
	private KsText(String scope, String relPath) {
		this(scope + ":" + relPath);
	}
*/	
	public String toString() {
		return ksUrl;
	}
	
	// equals and hashCode needed for use in collections
	@Override public boolean equals(Object o) {
		return ksUrl.equals(o);		
	}
	
	@Override public int hashCode() {
		return ksUrl.hashCode();		
	}
	

	/**
	 * Returns the N-th element in a colon (:) separated string
	 */
	private String getNthField(int n) {
		if (ksUrl != null && n >= 0) {
			String[] ss = ksUrl.split(":");
			if (n < ss.length) {
				return ss[n];
			}
		}
		return null;
	}

	/**
	 * Returns the scope string for a KB line
	 * @param ksUrl a line from *Ontologies.txt
	 * @return e.g."FACS researcher specific"
	 */
	public String getScope() {
		return getNthField(0);
	}

	/**
	 * Returns relative path for a KB line
	 * @param ksUrl a line from *Ontologies.txt
	 * @return e.g. "users/bozo@boza.com/FACS/protocols.pprj"
	 */
	public String getRelativePath() {
		return getNthField(1);
	}

	/**
	 * Returns the KB name for a given KB line
	 * @return e.g. "protocols.pprj"
	 */
	public String getKBName() {
		String kbName = getRelativePath();
		if (kbName != null) {
			//get just the name, drop path and .pprj
			kbName = kbName.replaceAll(".*/(.+)\\.pprj", "$1");
		}
		return kbName;
	}

	/**
	 * Returns map (with duplicates) of tuples <KB scope, rel.path>
	 * 
	 * @param c		collection of *Ontologies.txt lines
	 * @param currentRelativePath	rel.path of KB to be EXCLUDED from the map
	 * @return MapOfMany of e.g. ("FACS researcher specific",
	 *         "users/bozo@bo.com/FACS/protocols.pprj") ", "")
	 */
	public static MapOfMany<String, String> createMapOfMany(
			final Collection<KsText> c, 
			final String currentRelativePath) {
		
		final MapOfMany<String, String> mm = new MapOfMany<String, String>(false);
		if (c != null) {
			for (KsText ksURL : c) {
				final String relPath = ksURL.getRelativePath();
				if (relPath != null	&& !relPath.equals(currentRelativePath)) {
					if(mm.keySize() == 0){
						mm.put(ksURL.getScope(), relPath);
					}else{
						Collection<String> mapValues = mm.getAllValues();
						if(!mapValues.contains(relPath)){
							mm.put(ksURL.getScope(), relPath);
						}
					}
				}
			}
		}
		return mm;
	}
	
	/**
	 * Loads and returns a list of ontology locations from a *Ontologies.txt reader
	 */
	public static Set<KsText> getLinesFromReader(Reader reader) {
		Set<KsText> ontoLines = new HashSet<KsText>();
        if (reader != null) {
            final Collection<String> c = IoBasics.readTextLinesAndClose(
              new BufferedReader(reader), false);
            for (String s : c) {
           		ontoLines.add(new KsText(s));
            }
            try {
                reader.close();
            } catch (IOException ioe) {
                Pel.log.print(ioe);
            }
        }
        return ontoLines;
    }

	public static Map<String, String> createNameMap(Set<KsText> c, String currentRelativePath) {
		Map<String, String> nameMap = new HashMap<String, String>();
		if (c != null) {
			for (KsText ksURL : c) {
				final String relPath = ksURL.getRelativePath();
				if (relPath != null	&& !relPath.equals(currentRelativePath)) {
					final String userkbName = ksURL.getUserSpecifiedKBName();
					nameMap.put(relPath, userkbName);
				}
			}
		}
		return nameMap;
	}

	private String getUserSpecifiedKBName() {
		return getNthField(3);
	}
	
}
