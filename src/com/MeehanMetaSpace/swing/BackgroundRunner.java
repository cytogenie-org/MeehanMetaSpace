package com.MeehanMetaSpace.swing;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BackgroundRunner implements Runnable {

	public boolean isCancelled = false;
	private final String msg, title;
	private final JFrame parent;
	private boolean isShowProgressPanel=false;
	public BackgroundRunner(final JFrame frame, final String title,
			final String msg) {
		this.title = title;
		this.msg = msg;
		this.parent = frame;
	}

	public JDialog dlg;
	private Runnable target;

	public void go(final Runnable target) {
		this.target = target;
		this.isShowProgressPanel=false;
		run();
	}
	
	public void go(final Runnable target,final boolean showProgressPanel) {
		this.target = target;
		this.isShowProgressPanel=showProgressPanel;
		run();
	}

	public static void run(final Runnable target, final JFrame parent,
			final String title) {
		final InfiniteProgressPanel glassPane;
		final Component jc = parent.getGlassPane();
		if (jc instanceof InfiniteProgressPanel) {
			glassPane = (InfiniteProgressPanel) jc;
		} else {
			glassPane = new InfiniteProgressPanel();
		}
		parent.setGlassPane(glassPane);

		glassPane.run(target, 1, title);
	}

	public void run() {
		dlg = SwingBasics.getDialog(parent);
		dlg.setTitle(title);
		final JPanel jp=new JPanel(new BorderLayout(3,10));
		dlg.getContentPane().add(jp);
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		jp.add(new JLabel(msg), BorderLayout.CENTER);
		jp.setBorder(BorderFactory.createEmptyBorder(15,20,5,20));
		final JButton cancelButton = SwingBasics.getCancelButton(dlg,
				"Click to stop");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isCancelled = true;
				dlg.dispose();
			}
		});
		final JPanel cancelPanel = new JPanel();
		cancelPanel.setLayout(new BorderLayout());
		cancelPanel.add(cancelButton, BorderLayout.EAST);
		jp.add(cancelPanel, BorderLayout.SOUTH);
		dlg.setModal(true);
		dlg.pack();
		SwingBasics.bottomRight(dlg);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (isShowProgressPanel) {
					BackgroundRunner.run(target,parent,msg);
				} else {
					new Thread(target).start();
				}
			}
		});
		SwingBasics.showUpFront(dlg, cancelButton, true);
	}

}
