package com.MeehanMetaSpace.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.MeehanMetaSpace.Basics;
    
public class MultiSelectJComboStatusLabel extends ComboBoxForAbstractButtons implements ActionListener   
{
	JCheckBox items[];
	final JLabel selectedItems;
	final String initialLabel;
	final boolean isTextSmall;
	String promptText;
	
	public static MultiSelectJComboStatusLabel getIntance(final JCheckBox[] items, final String promptText, final String initialLabel, final boolean isTextSmall) {
		final Vector<Object> c=new Vector<Object>();
		c.add(new JLabel(promptText));
		for (JCheckBox item: items) {
			c.add(item);
		}
		MultiSelectJComboStatusLabel box = new MultiSelectJComboStatusLabel(c, items, promptText, initialLabel, isTextSmall, false);
		return box;
	}
	
	public MultiSelectJComboStatusLabel(final Vector v, final JCheckBox[] items, final String promptText, final String initialLabel, final boolean isTextSmall, final boolean useLabel) {
		super(v, useLabel);
		this.items = items;
		this.promptText = promptText;
		this.initialLabel = initialLabel;
		this.isTextSmall = isTextSmall;
		selectedItems = new JLabel("<html>" + initialLabel + "</html>");
		addActionListener(this);
	}
	
	public MultiSelectJComboStatusLabel(final JCheckBox[] items, final String initialLabel, final boolean isTextSmall, final boolean useLabel) {
		super(items, useLabel);
		this.items = items;
		this.initialLabel = initialLabel;
		this.isTextSmall = isTextSmall;
		selectedItems = new JLabel("<html>" + initialLabel + "</html>");
		addActionListener(this);
	}
	public MultiSelectJComboStatusLabel(final JCheckBox[] items, final String initialLabel, final boolean isTextSmall, final boolean useLabel,final String label, /*final ActionListener listener,*/ final ItemListener itemHandler) {
		super(items, useLabel,label);		
		this.items = items;
		this.initialLabel = initialLabel;
		this.isTextSmall = isTextSmall;
		selectedItems = new JLabel("<html>" + initialLabel + "</html>");
		addActionListener(this);
		if (itemHandler != null) {
			for (JCheckBox c: items) {
				c.addItemListener(itemHandler);
			}	
		}		
	}
	
	private boolean isIndexSet = false;
    public void actionPerformed(ActionEvent e)   
    {   
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final StringBuilder sb = new StringBuilder();				
		        
		        int i = 0;
		        boolean toggler = false;
		        for (JCheckBox box: items) {        	
		        	if (box.isSelected()) {
		        		if (i != 0) {
		        			if (isTextSmall && toggler) {
		        				sb.append("<br>");
		        				toggler = false;
		        			}
		        			else {
		        				toggler = true;
		        				sb.append(",&nbsp;");		        					        				
		        			}
		        		}
		        		if (box.getText().indexOf("<b>") > 0 && box.getText().indexOf("</b>") > 0) {
		        			sb.append(box.getText());		        			
		        		}
		        		else {
		        			sb.append("<b>");
		        			sb.append(box.getText());
		        			sb.append("<b>");
		        		}
		            	i++;
		        	}
		        }
		        final String content = Basics.stripSimpleHtml(sb.toString());
		        sb.setLength(0);
		        if (isTextSmall) {
		        	sb.setLength(0);
			        sb.append("<html><p style='font-color:verdana'><small>");
			        sb.append(content);
			        sb.append("</small></p></html>");	
		        }
		        else {
		        	sb.append("<html>" + initialLabel + "&nbsp;&nbsp;<font color=green>");
			        sb.append(content);
			        sb.append("</font></html>");	
		        }
		        selectedItems.setText(sb.toString());		        
		        if (!isIndexSet && promptText != null) {
		        	setSelectedIndex(0);
		        	isIndexSet = true;
		        }		      
		        else {
		        	isIndexSet = false;
		        }
		        repaint();
			}
		});    	      
    }   
    
    public static void main(String[] args)   
    {   
        JFrame f = new JFrame();   
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new FlowLayout());
        JCheckBox box1= new JCheckBox("Hello");
        JCheckBox box2= new JCheckBox("Hi");
        JCheckBox box3= new JCheckBox("Howdy");
        ArrayList<JCheckBox> boxes = new ArrayList<JCheckBox>();
        boxes.add(box1);
        boxes.add(box2);
        boxes.add(box3);
        MultiSelectJComboStatusLabel cc2 = MultiSelectJComboStatusLabel.getIntance(boxes.toArray(new JCheckBox[]{}), "Select/remove one or more","Show reagents:", false);
        f.getContentPane().add(cc2);
        f.getContentPane().add(new JLabel("<html>&nbsp;</html>"));
        f.getContentPane().add(cc2.getSelectedItems());        
        f.setSize(300,160);   
        f.setLocation(200,200);   
        f.setVisible(true);   
    }

	
	public JLabel getSelectedItems() {
		return selectedItems;
	}   
	
	public String getSelectedItemsText() {
		final StringBuilder sb = new StringBuilder();				
        
        for (JCheckBox box: items) {
        	if (box.isSelected()) {
        		if (sb.toString() != null && !sb.toString().equals("")) {
        			sb.append(",");
        		}
        		sb.append(box.getText());
        	}
        }
        return sb.toString();
	}
	
	public List<String> getSelectedItemsList() {
		ArrayList<String> selected = new ArrayList<String>();
        for (JCheckBox box: items) {
        	if (box.isSelected()) {
        		selected.add(box.getText());
        	}
        }
        return selected;
	}
	
	public List<Integer> getSelectedItemsIndices() {
		ArrayList<Integer> selected = new ArrayList<Integer>();
		int index= 0;
        for (JCheckBox box: items) {
        	if (box.isSelected()) {
        		selected.add(index);
        	}
        	index++;
        }
        return selected;
	}
	
	public void setCheckedItemValues(Map<String,Boolean> optionValues) {
		for (JCheckBox box: items) {
        	Object value =optionValues.get(box.getText());
        	if(value!=null){
        	box.setSelected(Boolean.parseBoolean(value.toString()));
        	} 
        	
	  }
		this.repaint();
	}
	public void setCheckedItemValue(String name,boolean value) {
       for (JCheckBox box: items) {
        	if(box.getText().equalsIgnoreCase(name)){
        		box.setSelected(value);
        	}
        }	  
		this.repaint();
	}
}   
    


