package com.MeehanMetaSpace.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.MeehanMetaSpace.Basics;

public class AtomicOpsWithProgressBar {

	public void flush(final boolean finished) {
		try {
			if (actions != null && actions.size() > 0) {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						for (final ActionListener action : actions) {
							action.actionPerformed(ae);
						}
						final int increment=actions.size();
						if (!finished) {
							progressBar.setValue(progressBar.getValue()
									+ increment);
						} else {
							progressBar.setValue(progressBar.getMaximum());
							dlg.setVisible(false);
							actions = null;
						}
						actions.clear();
					}
				});
			} else if (finished) {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						dlg.setVisible(false);
						actions = null;
					}
				});
			}
		} catch (final Exception e) {

		}

	}

	private int divisor;

	public void doOp(final ActionListener action) {
		if (actions != null) {
			actions.add(action);
			final int i = actions.size();
			if (((i) % divisor) == 0) {
				flush(false);
			}
		} else {
			action.actionPerformed(ae);
		}
	}

	public void startOps(final int maximumOps,
			final ActionListener overallMultiOpAction, final String item) {
		startOps(maximumOps, 50, 10, overallMultiOpAction, item);
	}

	public void startOps(final int maximumOps,
			final int minimumOpsToJustifyShowingProgressBar,
			final int updateProgressBarEveryNthOp,
			final ActionListener overallMultiOpAction, final String item) {
		assert SwingUtilities.isEventDispatchThread();
		if (maximumOps >= minimumOpsToJustifyShowingProgressBar) {
			actions = new ArrayList<ActionListener>();
			dlg.setTitle("Processing " + Basics.encode(maximumOps) +" "+ item);
			progressBar.setMaximum(maximumOps);
			progressBar.setValue(0);
			divisor = progressBar.getMaximum() / updateProgressBarEveryNthOp;
			SwingBasics.bottomRight(dlg);
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					final Thread th = new Thread() {
						public void run() {
							overallMultiOpAction.actionPerformed(ae);
							endOps();
						}
					};
					th.start();
				}
			});
			dlg.setVisible(true);					
		} else {
			overallMultiOpAction.actionPerformed(ae);
		}
	}

	private void endOps() {
		if (actions != null) {
			flush(true);
			
		}
	}

	private final JDialog dlg = new SwingBasics.Dialog("");
	private final JProgressBar progressBar = new JProgressBar();
	private final ActionEvent ae;
	private Collection<ActionListener> actions = null;

	private AtomicOpsWithProgressBar() {
		final JPanel jp = new JPanel();
		progressBar.setPreferredSize(new Dimension(400, 25));
		ae = new ActionEvent(progressBar, 1, "multi row op");
		jp.add(progressBar);
		jp.setBorder(BorderFactory.createEmptyBorder(5, 15, 25, 15));
		dlg.getContentPane().add(jp);
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent we) {
					Toolkit.getDefaultToolkit().beep();
					progressBar.setToolTipText(Basics.toHtmlErrorUncentered("Can not close",
							"Take your fingers off the input devices<br>or the genie will break them off and<br>stuff them in your ears."));
					ToolTipOnDemand.getSingleton().showLater(progressBar);
			}
		});

		dlg.pack();
	}

	public static AtomicOpsWithProgressBar singleton = new AtomicOpsWithProgressBar();
}
