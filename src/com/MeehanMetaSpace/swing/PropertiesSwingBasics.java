package com.MeehanMetaSpace.swing;

import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Container;
import java.awt.GridLayout;

import com.MeehanMetaSpace.*;

public final class PropertiesSwingBasics {


  public static String[] edit(final Properties properties, final String[] names) {
	final JDialog dialog = new JDialog(SwingBasics.mainFrame);
	dialog.setModal(true);
	final Container cp = dialog.getContentPane();
	cp.setLayout(new BorderLayout());
	final JPanel buttonPanel = new JPanel(),
		parameterPanel = new JPanel(),
		buttons = SwingBasics.getButtonPanel(1);
	buttons.add(SwingBasics.getDoneButton(dialog, "", true));
	buttonPanel.add(buttons);
	final GridLayout gridLayout = new GridLayout(names.length, 2);
	parameterPanel.setLayout(gridLayout);
	final JTextField[] flds = new JTextField[names.length];
	cp.add(BorderLayout.SOUTH, buttonPanel);
	for (int i = 0; i < names.length; i++) {
	  parameterPanel.add(new JLabel(names[i]));
	  final String name = names[i];
	  flds[i].setText(properties.getProperty(name));
	  parameterPanel.add(flds[i]);
	}
	dialog.pack();
	dialog.show();
	final String[] values = new String[names.length];
	for (int i = 0; i < names.length; i++) {
	  values[i] = flds[i].getText();
	  properties.setProperty(names[i], values[i]);
	}
	return values;
  }

}
