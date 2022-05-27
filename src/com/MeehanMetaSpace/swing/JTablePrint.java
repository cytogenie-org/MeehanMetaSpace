package com.MeehanMetaSpace.swing;

/**
 * Title:        Herzenberg Protocol Editor
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      Herzenberg Lab, Stanford University
 * @author Stephen Meehan
 * @version 1.0
 */

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Vector;
import com.MeehanMetaSpace.Pel;

public class JTablePrint {
	public static void print(JTable table){
		new JTablePrint().printJTable(table);
	}

	Paper paper = null;
	PageFormat pageFormat = null;

	public JTablePrint(){
		paper = new Paper();
		// paper.setSize(8.5*72.0, 11.0*72.0);
		// paper.setImageableArea(.25, .25, (8.5*72.0)-.5, (11.0*72.0)-.5);
        paper.setImageableArea(0, 0, (8.5*72.0)-.5, (11.0*72.0)-.5);
		pageFormat = new PageFormat();
		pageFormat.setPaper(paper);
	}

	public void printJTable(JTable table){
		PrinterJob pj=PrinterJob.getPrinterJob();
		pj.setPrintable(new JTablePrintable(table));
		if ( pj.printDialog() ) {
			try{
				pj.print();
			}catch (Exception e)
			{
				Pel.log.print(e);
			}
		}
	}

	private int getPageCount(JTable t, PageFormat pf){
		return (int) Math.ceil( ((double) t.getRowHeight() * (double) t.getRowCount()) / (double)pf.getImageableHeight()  );
	}

	public static PageFormat getPageFormat(){
		Paper spaper = new Paper();
		spaper.setSize(8.5*72.0, 11.0*72.0);
		spaper.setImageableArea(.25, .25, (8.5*72.0)-.5, (11.0*72.0)-.5);
		PageFormat spageFormat = new PageFormat();
		spageFormat.setPaper(spaper);
		return spageFormat;
	}

}
