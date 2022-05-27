package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooser {
	public interface Refresher {
		void refresh(Color color, boolean andSaveToo);
	}

	private static class ColorChooserDialog extends JDialog {
		private Color initialColor;
		
		private JButton cancelButton;

		private ColorChooserDialog(final Dialog owner, final String title, final boolean modal,
				final Component c, final JColorChooser chooserPane,
				final ActionListener okListener, final Color defaultColor)
				throws HeadlessException {
			super(owner, title, modal);
			initColorChooserDialog(c, chooserPane, okListener, defaultColor);
		}	

		private ColorChooserDialog(
				final Frame owner, 
				final String title, 
				final boolean modal,
				final Component c, 
				final JColorChooser chooserPane,
				final ActionListener okListener, 
				final Color defaultColor)
				throws HeadlessException {
			super(owner, title, modal);
			initColorChooserDialog(c, chooserPane, okListener, defaultColor);
		}

		private void initColorChooserDialog(final Component c,
				final JColorChooser chooserPane,
				final ActionListener okListener, 
				final Color defaultColor) {
			// setResizable(false);

			

			final String resetString = UIManager.getString("ColorChooser.resetText");

			final Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(chooserPane, BorderLayout.CENTER);

			/*
			 * Create Lower button panel
			 */
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));

			final JButton resetButton = SwingBasics.getButton(resetString, 'r',
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							chooserPane.setColor(initialColor);
						}
					}, "Reset ");
			buttonPane.add(resetButton);
			if (defaultColor!=null){
				final JButton defaultButton = SwingBasics.getButton("Default", 'r',
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							chooserPane.setColor(defaultColor);
						}
					}, "Default ");
				buttonPane.add(defaultButton);
			}
			buttonPane.add(new JLabel("    "));
			final JButton okButton = SwingBasics.getDoneButton(this);
			if (okListener != null) {
				okButton.addActionListener(okListener);
			}
			buttonPane.add(okButton);

			cancelButton = SwingBasics.getCancelButton(this, "Cancel choices");

			cancelButton.setActionCommand("cancel");
			buttonPane.add(cancelButton);

			contentPane.add(buttonPane, BorderLayout.SOUTH);

			if (JDialog.isDefaultLookAndFeelDecorated()) {
				boolean supportsWindowDecorations = UIManager.getLookAndFeel()
						.getSupportsWindowDecorations();
				if (supportsWindowDecorations) {
					getRootPane().setWindowDecorationStyle(
							JRootPane.COLOR_CHOOSER_DIALOG);
				}
			}
			applyComponentOrientation(((c == null) ? getRootPane() : c)
					.getComponentOrientation());

			if (!SwingBasics.packAndPersonalize(this, "ColorChooser")){
				SwingBasics.bottomRight(this);
			}
			

		}

	}

	private final static class ColorTracker implements ActionListener{
		JColorChooser chooser;
		Color color;

		private ColorTracker(JColorChooser c) {
			chooser = c;
		}

		public void actionPerformed(ActionEvent e) {
			color = chooser.getColor();
		}

		private Color getColor() {
			return color;
		}
	}

	private static JDialog createDialog(final Component c, final String title,
			final boolean modal, final JColorChooser chooserPane,
			final ActionListener ok, final Color defaultColor,
			final Refresher refresher) throws HeadlessException {
		final Window window = c==null?SwingBasics.mainFrame:SwingUtilities.getWindowAncestor(c);
		ColorChooserDialog dialog;
		chooserPane.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				refresher.refresh(chooserPane.getColor(), false);
			}
		});
		if (window instanceof Frame) {
			dialog = new ColorChooserDialog((Frame) window, title, modal, c,
					chooserPane, ok, defaultColor);
		} else {
			dialog = new ColorChooserDialog((Dialog) window, title, modal, c,
					chooserPane, ok, defaultColor);
		}
		return dialog;
	}
	public static Color showDialog(
			final Component component, 
			final String title,
			final Color initialColor, 
			final Refresher refresher) throws HeadlessException {
		final JColorChooser pane = new JColorChooser(
				initialColor != null ? initialColor : Color.white);
		final ColorTracker ok = new ColorTracker(pane);
		final JDialog dialog = createDialog(component, title, true, pane, ok,
				null, refresher);
		dialog.setVisible(true); // blocks until user brings dialog down...
		return ok.getColor();
	}

	public static Color showDialog(
			final Component component, 
			final String title,
			final Color initialColor, 
			final Color defaultColor,
			final Refresher refresher) throws HeadlessException {
		final JColorChooser pane = new JColorChooser(
				initialColor != null ? initialColor : Color.white);
		final ColorTracker ok = new ColorTracker(pane);
		final JDialog dialog = createDialog(component, title, true, pane, ok,
				defaultColor, refresher);
		dialog.setVisible(true); // blocks until user brings dialog down...
		return ok.getColor();
	}

}
