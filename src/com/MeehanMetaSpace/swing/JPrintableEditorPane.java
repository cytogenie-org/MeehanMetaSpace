package com.MeehanMetaSpace.swing;

/**
 * Title:        Meehan Meta Space Software
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Stephen Meehan
 * @version 1.0
 */

 //START JPrintableEditPane

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.Serializable;
import com.MeehanMetaSpace.Pel;
import javax.swing.*;
import javax.swing.event.*;

public class JPrintableEditorPane extends JEditorPane
	   implements Printable, Serializable
{
	static void printHtml(String title, String page){
		final JPrintableEditorPane jep=new JPrintableEditorPane("text/html", page );
		final JDialog jd=new JDialog(SwingBasics.mainFrame, title, true);
		jep.setEditable(false);
		jep.setContentType("text/html");
		Container cp=jd.getContentPane();
		cp.setLayout(new BorderLayout());
		JButton print = new JButton("Print");
		print.setMnemonic('P');
		print.setMargin(new Insets(5, 5, 5, 5));
		print.addActionListener(new ActionListener(){
			public void actionPerformed(final ActionEvent e)		{
				final PrinterJob job = PrinterJob.getPrinterJob();
				job.setPrintable(jep);
				if (job.printDialog())	{
					try {
					   job.print();
					   jd.dispose();
					}
					catch (final Exception ex) {
						   Pel.log.print(ex);
					}
				 }
			}
		});
		final JPanel jp=new JPanel();
		jp.add(print);
		JScrollPane jsp=new JScrollPane(jep);
		cp.add(jsp, BorderLayout.CENTER);
		jep.setCaretPosition(0); // jep is the JEditorPane
		JViewport jvp = jsp.getViewport(); // jsp is the JScrollPane
		jvp.setViewPosition(new Point(0,0));
		jsp.setViewport(jvp);
		cp.add(jp, BorderLayout.SOUTH);

		jd.pack();
		jd.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				jd.dispose();
			}
		});
		jd.show();

	}
	public JPrintableEditorPane(String type, String text)
	{
		super(type, text);
	}
	/**
	 * * The method @print@ must be implemented for @Printable@ interface.
	 * * Parameters are supplied by system.
	 * */
	public int print(Graphics g, PageFormat pf, int pageIndex)
			throws PrinterException
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.black);    //set default foreground color to black
		RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
		double pageHeight = pf.getImageableHeight();   //height of printer page
		double pageWidth  = pf.getImageableWidth();    //width of printer page

		Dimension d = this.getSize();    //get size of document
		double panelWidth  = d.width;    //width in pixels
		double panelHeight = d.height;   //height in pixels
		double scale = 0.65;
		if (panelWidth>0){
			scale=pageWidth/panelWidth;
			int totalNumPages = (int)Math.ceil(scale * panelHeight / pageHeight);
			// Make sure not print empty pages
			if(pageIndex >= totalNumPages)
			{
				return Printable.NO_SUCH_PAGE;
			}
		}

		// Shift Graphic to line up with beginning of print-imageable region
		g2.translate(pf.getImageableX(), pf.getImageableY());
		// Shift Graphic to line up with beginning of next page to print

		g2.translate(0f, -pageIndex*pageHeight);

		// Scale the page so the width fits...
		g2.scale(scale, scale);
		this.paint(g2);   //repaint the page for printing

		return Printable.PAGE_EXISTS;
	}
}

//END JPRIPrintableEditPane
