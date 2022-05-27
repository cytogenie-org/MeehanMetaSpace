/*
 * SimpleTableDemo.java is a 1.4 application that requires no other files.
 */

package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class SimpleTableDemo
	 extends JPanel{
	private boolean DEBUG=false;

	final static String[] noNames={
		 null, null};
	final static String[] stepColumnNames={
		 "Step",
		 "Cocktail volume"};

	final static Object[][] stepData={
		 {
		 "1", new Integer(25)}
		 , {
		 "2", new Integer(44)}
		 ,
	};

	final static Object[][] stepData2={
		 {
		 "1", new Integer(15)}
		 , {
		 "2", new Integer(27)}
		 ,
	};

	public SimpleTableDemo(){
		super(new GridLayout(1, 0));

		String[] columnNames={
			 "First Name",
			 "Last Name",
			 "Sport",
			 "# of Years",
			 "Vegetarian"};

		Object[][] data={
			 {
			 "Mary", "Campione",
			 "Snowboarding", new Integer(5), new Boolean(false)}
			 , {
			 "Alison", stepData,
			 "Rowing", new Integer(3), new Boolean(true)}
			 , {
			 "Kathy", "Walrath",
			 "Knitting", new Integer(2), new Boolean(false)}
			 , {
			 "Sharon", stepData2,
			 "Speed reading", new Integer(20), new Boolean(true)}
			 , {
			 "Philip", "Milne",
			 "Pool", new Integer(10), new Boolean(false)}
		};

		final NestedTableCellEditor.Factory factory=new NestedTableCellEditor.Factory( 2, 2);
		final JTable table=new JTable(data, columnNames){
			public TableCellRenderer getCellRenderer(
				 final int row,
				 final int column){
				if (factory.isSuitable(this, row, column)){
					return factory;
				}
				return super.getCellRenderer(row, column);

			}

			public TableCellEditor getCellEditor(int row, int column){
				NestedTableCellEditor nt=factory.getTableCellEditor(this, row, column);
				if (nt!=null){
					return nt;
				}
				return super.getCellEditor(row, column);
			}
		};
		//table.setPreferredScrollableViewportSize(new Dimension(500, 70));

		if (DEBUG){
			table.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e){
					printDebugData(table);
				}
			});
		}

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane=new JScrollPane(table);

		//Add the scroll pane to this panel.
		add(scrollPane);
	}

	private void printDebugData(JTable table){
		int numRows=table.getRowCount();
		int numCols=table.getColumnCount();
		javax.swing.table.TableModel model=table.getModel();

		System.out.println("Value of data: ");
		for (int i=0; i<numRows; i++){
			System.out.print("    row "+i+":");
			for (int j=0; j<numCols; j++){
				System.out.print("  "+model.getValueAt(i, j));
			}
			System.out.println();
		}
		System.out.println("--------------------------");
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI(){
		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame=new JFrame("SimpleTableDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		SimpleTableDemo newContentPane=new SimpleTableDemo();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args){
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		// start program event logging
		try{

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){
			e.printStackTrace();
		}
		/*		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run(){*/
		createAndShowGUI();
		/*			}
		  });*/
	}
}
