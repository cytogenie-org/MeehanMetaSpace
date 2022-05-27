package com.MeehanMetaSpace.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXCollapsiblePane;

import com.MeehanMetaSpace.swing.Accordion.Button;
import com.MeehanMetaSpace.swing.Accordion.POSITION;
import com.MeehanMetaSpace.swing.Accordion.Tab;

public class CascadingMenu {
	
	String rootName;
	Icon logo;
	Accordion accordion;
	JButton root;
	
	public CascadingMenu() {
		Accordion.initWindow();
		accordion = new Accordion(null);
		rootName="Menu";
		root = accordion.addTab(rootName, 'm', new JXCollapsiblePane(), null);
	}
	
	public CascadingMenu(String name) {
		Accordion.initWindow();
		this.rootName = name;
		accordion = new Accordion(null);
		root = accordion.addTab(name, (name.length()>0)?name.charAt(0):'z', new JXCollapsiblePane(), null);
	}

	public CascadingMenu(Icon logo) {
		Accordion.initWindow();
		this.logo = logo;
		rootName="Menu";
		accordion = new Accordion(logo);
		root = accordion.addTab(rootName, 'm', new JXCollapsiblePane(), null);
	}

	public CascadingMenu(String name, Icon logo) {
		Accordion.initWindow();
		this.rootName = name;
		this.logo = logo;
		accordion = new Accordion(logo);
		root = accordion.addTab(name, (name.length()>0)?name.charAt(0):'z', new JXCollapsiblePane(), null);
	}
	//ArrayList<JButton> tabList =new ArrayList<JButton>();
	public Object createTab(String name) {
		JButton btn = accordion.addTab(name, -1, new JXCollapsiblePane(), null);//(name.length()>0)?name.charAt(0):'z'
		//tabList.add(btn);
		//accordion.addTabToTab(header, entry.getKey(), -1, null, manager, pos++)
		//accordion.addTabToTab(rootName, btn.getText(), -1, null, null, count++);//(btn.getText().length()>0)?btn.getText().charAt(0):'z'
		return btn;
	}
	
	public Object createTabItem(final String name, Icon icon) {
		final JButton btn = (new JButton(new AbstractAction(name, icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(name + " clicked");
			}
		}));
		btn.setIcon(icon);
		return Accordion.getDecoratedButton(btn, Accordion.COLOR_SUB_SUBLIST, POSITION.LEFT, null, 1);
	}
	
	public Object createTabItemLabel(final String name) {
		return Accordion.getDecoratedLabel(name, 11, 11);
	}
	
	public Object createTabItemSeparator() {
		return Accordion.getSeparator();
	}
	
	public Object createTabItem(final String name) {
		final JButton btn = (new JButton(new AbstractAction(name, MmsIcons.getBlank13x13Icon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(name + " clicked");
			}
		}));
		return Accordion.getDecoratedButton(btn, Accordion.COLOR_SUB_SUBLIST, POSITION.LEFT, null, 1);
	}
	
	public void add(Object tab, Object tabItem) {
		if (tab instanceof JButton && tabItem instanceof Button) {
			String name = ((JButton) tab).getText();
			Tab parent = accordion.getTab(name);
			if (parent != null) {
				parent.addComponentItem((Button)tabItem);
			}
			//accordion.addButton((JButton)tabItem);
		}
		else if (tab instanceof JButton && tabItem instanceof Component) {
			String name = ((JButton) tab).getText();
			Tab parent = accordion.getTab(name);
			if (parent != null) {
				parent.addComponentItem((Component)tabItem);
			}	
		}
		
	}
	
	public void add(Object tab) {
		accordion.addTabToTab(rootName, ((JButton)tab).getText(), -1, null, null, count++);//(btn.getText().length()>0)?btn.getText().charAt(0):'z'
		/*if (expandFirstSubMenu && firstButton) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {										
					btn.doClick(500);					
				}
			});
			firstButton = false;
		}*/
	}
	
	int count=0;
	public Accordion getCascadingMenu() {
		/*int count=0;
		for (JButton btn: tabList) {
			accordion.addTabToTab(rootName, btn.getText(), (btn.getText().length()>0)?btn.getText().charAt(0):'z', null, null, count++);
		}*/
		root.doClick(300);
		accordion.render(true);
		return accordion;
	}
	
	public static void main(String[] args) {
		Accordion.initWindow();
		JFrame frame = new JFrame("Cascading Menus demo");
		
		CascadingMenu cascadingMenu = new CascadingMenu(MmsIcons.getGenieIcon());
		
		Object tab1 = cascadingMenu.createTab("File");
		Object tabItem1 = cascadingMenu.createTabItem("New", MmsIcons.getNewIcon());
		Object tabItem2 = cascadingMenu.createTabItem("Open", MmsIcons.getOpenIcon());
		//Object tab1Item3 = cascadingMenu.createTabItemSeparator();
		
		Object tab2 = cascadingMenu.createTab("View");
		Object tab2Item1 = cascadingMenu.createTabItem("Help", MmsIcons.getHelpIcon());
		Object tab2Item2 = cascadingMenu.createTabItem("About", MmsIcons.getTickIcon());
		//Object tab2Item3 = cascadingMenu.createTabItemLabel("Thank you");
		
		cascadingMenu.add(tab1, tabItem1);
		cascadingMenu.add(tab1, tabItem2);
		//cascadingMenu.add(tab1, tab1Item3);
		
		cascadingMenu.add(tab2, tab2Item1);
		cascadingMenu.add(tab2, tab2Item2);
		//cascadingMenu.add(tab2, tab2Item3);
		
		cascadingMenu.add(tab1);
		cascadingMenu.add(tab2);
		
		frame.add(cascadingMenu.getCascadingMenu());
		frame.setSize(200,400);
		frame.setVisible(true);
		
		/* Example below for Matlab
		todoList = CascadingMenu;
		m=todoList.NewMenu('File', ...
		                @(h,e)openAnyExperiment(GatingTree.OPEN_NEW));
		mi=todoList.NewMenuItem('Click to create a new experiment', ...
		                @(h,e)openAnyExperiment(GatingTree.OPEN_NEW), ...
		                fullfile(pp, 'bullseye.gif'));
		            
		todoList.addMenuItem(m, mi);
		todoList.addMenu(m);
		accordion=todoList.getCascadingMenu();
		 */
	}

}
