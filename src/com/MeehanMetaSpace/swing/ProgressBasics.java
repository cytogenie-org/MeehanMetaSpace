package com.MeehanMetaSpace.swing;

import javax.swing.*;
import java.awt.Window;
/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class ProgressBasics {
  public static void indicateProgressStarting(
	  final JProgressBar[] progressBars,
	  final Window windowNeedingRepacking) {
	if (SwingUtilities.isEventDispatchThread()) {
	  for (int i = 0; i < progressBars.length; i++) {
		progressBars[i].setValue(0);
		progressBars[i].setVisible(true);
	  }
	  if (windowNeedingRepacking != null) {
		windowNeedingRepacking.pack();
	  }

	}
	else {
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  indicateProgressStarting(progressBars, windowNeedingRepacking);
		}
	  });
	}

  }

  public static void indicateProgressStarting(
	  final JProgressBar progressBar,
	  final Window windowNeedingRepacking) {
	if (SwingUtilities.isEventDispatchThread()) {
	  progressBar.setValue(0);
	  progressBar.setVisible(true);
	  if (windowNeedingRepacking != null) {
		windowNeedingRepacking.pack();
	  }
	}
	else {
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  indicateProgressStarting(progressBar, windowNeedingRepacking);
		}
	  });
	}

  }

  public static void indicateProgressCompletion(final JProgressBar progressBar) {
	if (SwingUtilities.isEventDispatchThread()) {
	  progressBar.setValue(progressBar.getMaximum());
	  progressBar.setString("100% complete!");
	}
	else {
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  indicateProgressCompletion(progressBar);
		}
	  });

	}
  }

  public static void indicateProgress(final JProgressBar progressBar,
									   final int value, final String msg) {
	if (SwingUtilities.isEventDispatchThread()) {
	  if (msg != null) {
		progressBar.setString(msg);
	  }
	  if (value > 0) {
		progressBar.setValue(value);
	  }
	}
	else {
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  indicateProgress(progressBar, value, msg);
		}
	  }
	  );
	}
  }

}
