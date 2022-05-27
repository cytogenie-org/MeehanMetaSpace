package com.MeehanMetaSpace.swing;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import com.MeehanMetaSpace.*;
public final class EndUserLicenseAgreement
	extends JDialog{
  public static boolean show( final String htmlEncodedContent) {

  if (htmlEncodedContent != null) {
	final File f=IoBasics.saveTempTextFile("mms", ".html",
								  htmlEncodedContent);
	final String s=f.getAbsolutePath();

	final String url;
   if (s.charAt(1)==':'){
	 url="file://" + s.substring(2);
   } else {
	 url="file://"+s;
   }
	final EndUserLicenseAgreement eula= new EndUserLicenseAgreement(url, true);
	return eula.accepted;
  }
  return false;
}

  public static void main(final String[] args){
	if (args.length == 0){
	  new EndUserLicenseAgreement(
		  "http://sciencexchange-sff.stanford.edu/protege/fergus.html", true);
	}
	else{
	  //new EndUserLicense(args[0],true);
	  new EndUserLicenseAgreement( IoBasics.readStringAndCloseWithoutThrowingUp(new File(args[0])), false);
	}
  }

  private JEditorPane htmlPane;
  private String initialURL;
  private JScrollPane scrollPane;
  private JRadioButton accept;
  private boolean accepted=false;
  private JPanel getButtonGroup(){
	final JPanel jp=new JPanel();
	// Create an action for each radio button
	final Action acceptAction=new AbstractAction("Accept"){
	  // This method is called whenever the radio button is pressed,
	  // even if it is already selected; this method is not called
	  // if the radio button was selected programmatically
	  public void actionPerformed(ActionEvent evt){
		accepted=true;
		SwingBasics.closeWindow(EndUserLicenseAgreement.this);
	  }
	};
	final Action declineAction=new AbstractAction("Decline"){
	  // See above
	  public void actionPerformed(final ActionEvent evt){
		PopupBasics.alert("Can not continue unless you accept the license!");

		SwingBasics.closeWindow(EndUserLicenseAgreement.this);
	  }
	};

	// Create the radio buttons using the actions
	accept=new JRadioButton(acceptAction);
	accept.setEnabled(true);
	final JScrollBar vsb= scrollPane.getVerticalScrollBar();
	vsb.addAdjustmentListener(new java.awt.event.
		AdjustmentListener(){
	  public void adjustmentValueChanged(final AdjustmentEvent evt){
		  final Adjustable source=evt.getAdjustable();
		  // Determine which scrollbar fired the event
		  int orient=source.getOrientation();
		  if (orient == Adjustable.HORIZONTAL){
			// Event from horizontal scrollbar
		  }
		  else{
			// Event from vertical scrollbar
			final int v=evt.getValue(), m=vsb.getMaximum(),
				va=vsb.getVisibleAmount();
			if (v + va == m ){
			  if (times++>0){
				accept.setEnabled(true);
			  }
			}
			//System.out.println("v="+v+", m="+m+", va="+va+",  "+evt.getAdjustmentType());
		  }


	  }

	});
	// Associate the two buttons with a button group
	final ButtonGroup group=new ButtonGroup();
	group.add(accept);
	final JRadioButton decline=new JRadioButton(declineAction);
	group.add(decline);
	jp.add(new JLabel("<html><body>Read <b>entire</b> agreement&nbsp; (Scroll down)&nbsp;&nbsp;&nbsp;&nbsp;</body><html>"));
	jp.add(accept);
	jp.add(decline);
	addWindowListener(new WindowAdapter(){
	  public void windowClosing(final WindowEvent we){
		if (!accepted){
		  System.exit(0);
		}
	  }
	});
	return jp;
  }
  int times=0;

  public EndUserLicenseAgreement(final String initialURL, final boolean isUrl){
	super();
	setTitle("End user license agreement");
	setModal(true);
	this.initialURL=initialURL;
	addWindowListener(new WindowAdapter(){
	  public void windowClosing(WindowEvent e){
		dispose();
	  }
	});


	try{
	  if (isUrl){
		htmlPane=new JEditorPane(initialURL);
	  } else {
		htmlPane=new JEditorPane("text/html", initialURL);
	  }
	  htmlPane.setEditable(false);
	  scrollPane=new JScrollPane(htmlPane);
	  getContentPane().add(scrollPane, BorderLayout.CENTER);
	  htmlPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 14, 10));
	  pack();
	  getContentPane().add(getButtonGroup(), BorderLayout.SOUTH);
	  final Dimension screenSize=getToolkit().getScreenSize();
	  final int width=screenSize.width * 8 / 10;
	  final int height=screenSize.height * 8 / 10;
	  setBounds(width / 8, height / 8, width, height);
	  setVisible(true);

	}
	catch (IOException ioe){
	  warnUser("Can't build HTML pane for " + initialURL
			   + ": " + ioe);
	}

  }

  private void warnUser(final String message){
	JOptionPane.showMessageDialog(this, message, "Error",
								  JOptionPane.ERROR_MESSAGE);
  }
}
