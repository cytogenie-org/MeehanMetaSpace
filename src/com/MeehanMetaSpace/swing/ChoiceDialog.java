/*
=====================================================================

  ChoiceDialog.java

  Created by Stephen Meehan
  Copyright (c) 2002

 =====================================================================
*/
package com.MeehanMetaSpace.swing;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import com.MeehanMetaSpace.*;


public class ChoiceDialog {
	private int length, charsWide;
	private static final int choiceColumnWidth=22,
		 baseWidth=30, baseHeight=110,
		 interColumnChars=2;
	private final Vector<ChoiceRow> rows = new Vector<ChoiceRow>();
	private java.util.List<String> headerLabels;
	private final JDialog dlg;
	private final JPanel cp;
	private static Popup popup;
	public static void hide(){
		if(popup!=null){
			if (cancel != null){
				cancel.doClick();
				cancel=null;
			}else{
				popup.hide();
			}
			popup=null;
		}
	}
	
	private final Component componentForPopup; 
	private final Point locationForPopup;
	public ChoiceDialog(
			final String title, 
			final String msg, 
			final String []labelHeaders, 
			final String choiceHeader){
		this(title,msg,labelHeaders,choiceHeader,null,0,0);
	}
	
	public ChoiceDialog(
			final String title, 
			final String msg, 
			final String []labelHeaders, 
			final String choiceHeader,
			final Component componentForPopup
			){
		this(title,msg,labelHeaders,choiceHeader,componentForPopup,-1,-1);
	}
	public interface Listener{
		void notifySelected(int selections, JButton done);
		void notifySetting(int rowIndex, int dataColumnIndex, Object before, Object after);
	}
	private final Listener listener;
	public ChoiceDialog(
			final String title, 
			final String msg, 
			final String []labelHeaders, 
			final String choiceHeader, 
			final Component componentForPopup, 
			int xOffset,
			int yOffset){
		this(title, msg, labelHeaders, choiceHeader, componentForPopup, xOffset, yOffset, null);
	}
	
	public ChoiceDialog(
			final String title, 
			final String msg, 
			final String []labelHeaders, 
			final String choiceHeader, 
			final Component componentForPopup, 
			int xOffset,
			int yOffset,
			Listener listener){
		this.listener=listener;
		hide();
		cancel=SwingBasics.getCancelButton(null, "Close window and do not accept selections", true, null);

		if (componentForPopup!=null){
			final Point p=componentForPopup.isShowing()?componentForPopup.getLocationOnScreen():new Point(150,150);
			if (xOffset>=0 && yOffset>=0 ){
				this.locationForPopup=new Point(p.x+xOffset, p.y+yOffset);
			} else {
				xOffset=p.x+componentForPopup.getWidth();
				yOffset=p.y+componentForPopup.getHeight();
				this.locationForPopup=new Point(xOffset,yOffset);
			}
		} else {
			this.locationForPopup=null;
		}
		this.componentForPopup=componentForPopup;
		headerLabels=new ArrayList<String>();
		cp=new GradientBasics.Panel();
		cp.setOpaque(true);
		final Border border=BorderFactory.createEmptyBorder(15, 15, 15, 15);
		if (componentForPopup==null){
			dlg=SwingBasics.getDialog(null);
			dlg.setTitle(title);	
			dlg.setModal(true);
			dlg.getContentPane().add(cp);
			popup=null;
			cp.setBorder(border);
		} else {
			dlg=null;
			final Border outer=BorderFactory.createLineBorder(Color.blue, 1);
			cp.setBorder(BorderFactory.createCompoundBorder(outer, border));
		}
		cp.setLayout(new BorderLayout(5,5));
		cp.add(new JLabel(msg), BorderLayout.NORTH);
		charsWide=title.length();
		if ( msg.length() > charsWide){
			charsWide=msg.length();
		}
		int w=0;
		for (int i=0;i<labelHeaders.length;i++){
			w+=labelHeaders[i].length() + interColumnChars;
			headerLabels.add(labelHeaders[i]);
		}
		headerLabels.add(choiceHeader);
		if ( w + choiceHeader.length() + interColumnChars > charsWide){
			charsWide=w  + choiceHeader.length() + interColumnChars ;
		}
	}

	public void addChoice(String label, boolean chosen) {
		addChoice(new String [] {label}, chosen);
	}

	public void addChoice(String label1, String label2, boolean chosen) {
		addChoice(new String [] {label1, label2}, chosen);
	}

	public void addChoice(String []labels, boolean chosen) {
		final Vector row=new Vector();
		int w=0;
		int i=0;
		for ( ;i<labels.length;i++){
			row.add(labels[i]);
			w+=(labels[i]==null?0:labels[i].length())+interColumnChars ;
		}
		row.add(ComparableBoolean.valueOf(chosen));
		rows.add( new ChoiceRow( row ));
		if (w + choiceColumnWidth  + interColumnChars > charsWide){
			charsWide=w + choiceColumnWidth  + interColumnChars ;
		}
	}

	public int size(){
		return rows.size();
	}

	Properties properties=null;
	public void setPersonalizations(Properties properties){
		this.properties=properties;
	}

	public Properties getPersonalizations(){
		return tableModel == null ? null :tableModel.updatePropertiesWithPersonalizations(false);
	}
	public static class Choice{
		final public String label;
		final public boolean chosen;
		public Choice (final String label, final boolean chosen){
			this.label=label;
			this.chosen=chosen;
		}
	}
	
	public List<Choice>getChoices(){
		final List<Choice>r=new ArrayList<ChoiceDialog.Choice>();
		for (int i = 0; i < size(); i++) {
			final String label=getLabel(i) ;
			final boolean b=isChosen(i);
			r.add(new Choice(label, b));
			System.out.println(label + "=" + b);
		}
		
		return r;
	}
	
	public boolean isChosen(final int i){
		final ChoiceRow cr=rows.get(i);
		final int col=cr.getColumnCount()-1;
		return ( (ComparableBoolean) cr.get(col)).booleanValue() ;
	}

	public String getLabel(final int i){
		return ( (String ) (  rows.get(i).get(0))) ;
	}
	private static JButton cancel;
	private JButton done=null;
	private DisabledExplainer de;
	private int selections=0;
	
	private void initSelections(){
		selections=0;
		final List<ChoiceDialog.Choice>l=getChoices();
		boolean removed=false;
		for (int i = 0; i < l.size(); i++) {
			final ChoiceDialog.Choice choice=l.get(i);
			if (isChosen(i)){
				selections++;
			}
		}
		handleDoneButton();
	}
	
	public void addCancelAction(final ActionListener al){
		cancel.addActionListener(al);
	}
	private void handleDoneButton(){
		final String doneText=headerLabels.get(headerLabels.size()-1);
		if (selections>0){
			de.setEnabled(null);
			done.setText(Basics.concatObjects(doneText, " (", selections, ")"));
		} else {
			done.setText(doneText);
			de.setEnabled("Nothing is selected yet");
		}
		
		if (listener!=null){
			listener.notifySelected(selections, done);
		}
	}
	private final class ChoiceRow extends ListRow{
		private ChoiceRow( final List l) {
			super(l);
		}

		public boolean isEditable(final int dataColumnIndex){
			return editable || dataColumnIndex>0;
		}
		
		public void set(final int dataColumnIndex, final Object element){
			if (element instanceof ComparableBoolean){
				if ( ((ComparableBoolean)element).booleanValue()){
					selections++;
				} else {
					selections--;
				}
				handleDoneButton();
			} else {
				final int choiceColumn=getColumnCount()-1;
				set(choiceColumn, ComparableBoolean.YES);
			}
			Object before=null;
			if (listener != null){
				before=super.get(dataColumnIndex);
			}
			super.set(dataColumnIndex, element);
			if (listener !=null){
				
				listener.notifySetting(rows.indexOf(this), dataColumnIndex, before, element);
			}
		}
	}

	private PersonalizableTableModel tableModel;

	public boolean cancelled=false;
	private boolean editable=false;

	public void show(final boolean editable, final ActionListener conclude,
			final Color mainBg){
		this.editable=editable;
		final DefaultPersonalizableDataSource dataSource = new DefaultPersonalizableDataSource.Picker (
				  (Vector)rows, new ListMetaRow(headerLabels, rows.get(0))){
			public boolean isAddable() {
				return editable;
			}
			public Object getRenderOnlyValueForLastRow(int dataColumnIndex,
					boolean isSelected, boolean hasFocus) {
				if (dataColumnIndex==0){
					final String htmlDisabledColor=SwingBasics.toHtmlRGB(SystemColor.textInactiveText);
					return Basics.concat(
							"<html><font color='", htmlDisabledColor,
							"'><small><i>Enter NEW <u>", 
							Basics.stripSimpleHtml(headerLabels.get(0)),
							"</u></i></small></font></html>");
					
				}
				return null;
			}
			public boolean isCreatable() {
				return editable;
			}

	        public Row[] create(final PersonalizableTableModel modelShowing) {
	        	final Row[]created=new Row[1];
	        	final List al=new ArrayList();
	    		for (int i=0;i<headerLabels.size()-1;i++){
	    			al.add(null);
	    		}
	    		al.add(ComparableBoolean.NO);
	        	created[0]=new ChoiceRow(al);
	    		modelShowing.setFromContext(created[0]);
	    		add(created[0]);
	    		modelShowing.addCreationToViewedTable(created[0]);
	        	return created;
	        }
		};
		if (properties==null){
			tableModel = PersonalizableTableModel.activate(dataSource, false);
		}else {
			tableModel = PersonalizableTableModel.activate(dataSource, properties, false);
		}
		TableBasics.setListTable(tableModel);
		tableModel.rememberUserPreferredColumnWidth=true;
		tableModel.setColumnWidthProperty(0, 250);
		tableModel.setColumnWidthProperty(1, 78);
		TableBasics.focusOnFirstCellLater(tableModel, true);
		final PersonalizableTable table=new PersonalizableTable(  tableModel );
        final JScrollPane jsp=table.makeHorizontalScrollPane(  );
		
		cp.add( tableModel.getContainer("") , BorderLayout.CENTER  );
		final JPanel jp=new JPanel();
		final ActionListener doneAction=new ActionListener() {
			public void actionPerformed(final ActionEvent e){
				cancelled=false;
				if (dlg!=null){
					SwingBasics.closeWindow(dlg);
				} else {
					if(popup != null){
						popup.hide();
					}
					popup=null;
				}
			}
		};
		done=SwingBasics.getDoneButton(doneAction, "Close this window and accept selections");
		jp.add(done);
		final String doneText=headerLabels.get(headerLabels.size()-1);
		done.setText(doneText);
		done.setMnemonic(doneText.charAt(0));
		de=new DisabledExplainer(done);
		if (conclude !=null){
			done.addActionListener(conclude);
		}
		final ActionListener cancelAction=new ActionListener() {
			public void actionPerformed(ActionEvent e){
				cancelled=true;
				if (dlg != null){
					SwingBasics.closeWindow(dlg);
				} else{
					popup.hide();
				}
			}
		};
		cancel.addActionListener(cancelAction);
		cancel.setMnemonic('c');
		jp.add(cancel);
		cp.add( jp, BorderLayout.SOUTH ) ;
		final Dimension screenSize = table.getToolkit().getScreenSize();
		int totalHeight=(table.getRowHeight()+2)*(dataSource.getDataRows().size()+3);
		totalHeight+=baseHeight;
		if (totalHeight>screenSize.getHeight()){
			totalHeight=screenSize.height-10;
		}
		int totalWidth=charsWide*(table.FONT_BASE.getSize()/2);
		if (totalWidth > screenSize.getWidth()){
			totalWidth=screenSize.width-10;
		} if (totalWidth<300){
			totalWidth=300;
		}
		initSelections();
		tableModel.requestFocusLater();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				table.registerKeyboardAction(
						new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								cancel.doClick();
								
							}
						}, 
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
						JComponent.WHEN_FOCUSED);
				GradientBasics.setTransparentChildren(cp, true);

			}
		});
		if (dlg!=null){
			dlg.getRootPane().setDefaultButton(done);
			dlg.getRootPane(). registerKeyboardAction(
					cancelAction,
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			SwingBasics.packAndPersonalize(dlg, "choiceDialog");
			dlg.setPreferredSize(new Dimension(totalWidth, dlg.getHeight()));
			dlg.setVisible(true);
		} else {
			final PopupFactory popupFactory = PopupFactory.getSharedInstance();
			final JPanel main=new JPanel();
			main.setBackground(mainBg==null?cp.getBackground():mainBg);
			
			main.add(cp);
			cp.setPreferredSize(new Dimension(totalWidth, totalHeight));
			popup = popupFactory.getPopup(
					componentForPopup, 
					main,
	                locationForPopup.x,
	                locationForPopup.y);
			popup.show();
		}
	}
}
