package com.MeehanMetaSpace;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public interface ProgressUpdater {
	boolean isCancelled();
  int getThresholdSize();
  void report(final Condition.Annotated a);
  void report(final String description, final int currentAmount, final int tallySoFar, final int finalAmount);
}
