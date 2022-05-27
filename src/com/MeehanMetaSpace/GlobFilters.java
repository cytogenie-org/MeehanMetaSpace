package com.MeehanMetaSpace;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;
import java.util.StringTokenizer;



public class GlobFilters implements FilenameFilter {
  private final GlobFilter []globFilters;

  public GlobFilters(final File file) {
	this((String[])IoBasics.readTextFileLineArray(file));
  }

  public GlobFilters(final String []globPatterns) {
	int n=0;
	for (int i=0;i<globPatterns.length;i++){
	  if (!Basics.isEmpty(globPatterns[i])){
		n++;
	  }
	}
	globFilters=new GlobFilter [n];
	int j=0;
	for (int i=0;i<globPatterns.length;i++){
	  if (!Basics.isEmpty(globPatterns[i])){
		globFilters[j++]=new GlobFilter(globPatterns[i]);
	  }
	}
  }

  public boolean accept(final File dir, final String name){
	for (int i=0;i<globFilters.length;i++){
	  if (globFilters[i].accept(dir, name)){
		return true;
	  }
	}
	return false;
  }


}
