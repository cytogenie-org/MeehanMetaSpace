package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXCollapsiblePane;

import com.MeehanMetaSpace.Basics;

public class AccordionLite extends JPanel implements ActionListener {
	
	public static final Color COLOR_SUB_SUBLIST = new Color(162, 210, 245);
	private static final String HIDE_THE_TASK_PANEL = "Hide the task bar";
	private static final String SHOW_THE_TASK_PANEL = "Show the task bar";
	private static final String REFRESH_THE_TASK_PANEL = "Refresh the task bar";
	public static Color COLOR_SUBLIST = new Color(118, 192, 245);
	public static Font FONT_SUBLIST = new Font("Calibri", Font.PLAIN,12);
	private JPanel topPanel = new JPanel(new GridBagLayout()) {
		public Color getBackground() {
			return bgColor;
		}
		public void setOpaque(final boolean b) {
			super.setOpaque(true);
		}
	};
	
	public static enum  POSITION {
        LEFT,
        RIGHT        
	};
	
	public static POSITION position = POSITION.LEFT; 
	private Map tabs = new LinkedHashMap();
	private int visibleTab = -1;
	private JButton visibleButton, visibleSubButton;
	private JComponent visibleComponent = null;
	private static Color fgColor, bgColor;
	private static String fontName;
	private boolean toggle = false, subToggle = false;

	private ArrayList<Component> subTabVisibleComponent = new ArrayList<Component>();
	private String expandedSubTab = "";
	private Map subTabs = new LinkedHashMap();
	private String visibleSubTab = "";
	private GridBagConstraints c = new GridBagConstraints();

	public interface Refresher {
		public void refresh();
	}
	private Refresher refresher;
	public void registerRefresher(Refresher refresher) {
		this.refresher = refresher;
		setRefreshButton();
		refreshPanel.add(new JLabel("<html>&nbsp;&nbsp;</html>"), BorderLayout.WEST);
		refreshPanel.add(refreshButton, BorderLayout.CENTER);
	}
	public void refresh() {
		if (refresher != null) {
			lastOpenTab=visibleTab;
			refresher.refresh();
		}
	}

	public static JLabel getDecoratedLabel(final String name, final int height, final int fontsize) {
		return new JLabel(name) {
			public Color getForeground() {
				return fgColor;
			}

			public Dimension getSize() {
				return new Dimension(super.getSize().width,
						(super.getSize().height + height));
			}

			public Font getFont() {
				return new Font(fontName, Font.BOLD, fontsize);
			}

			public void paintComponent(Graphics g) {
				g.setColor(bgColor);
				g.fillRect(0, 0, getSize().width, getSize().height);
				super.paintComponent(g);
			}
		};
	}

	public static boolean isMainPanelVisible = true;
	JSplitPane splitPane;

	public void setSplitPane(final JSplitPane sp) {
		splitPane = sp;
	}

	public static int accordion_split_location = 260;
	
	public void setSplitPane() {
		removeAll();
		if (isMainPanelVisible) {
			northPanel.add(closerPanel, BorderLayout.EAST);
			add(northPanel, BorderLayout.NORTH);
			hideButton.setToolTipText(HIDE_THE_TASK_PANEL);
			hideButton.setIcon(MmsIcons.getMinimize3());
			add(mainPanel, BorderLayout.CENTER);
			add(southPanel, BorderLayout.SOUTH);
			accordion_split_location=position == POSITION.LEFT?260:1050;
		} else {
			hideButton.setText("");
			hideButton.setToolTipText(SHOW_THE_TASK_PANEL);
			hideButton.setIcon(MmsIcons.getMaximize3());
			add(closerPanel, BorderLayout.NORTH);
			accordion_split_location=position == POSITION.LEFT?25:1255;
			
		}
		splitPane.setDividerLocation(accordion_split_location);
		splitPane.revalidate();
		validate();
	}
	
	public boolean isAccordionHidden() {
		if (!isMainPanelVisible) {
			return true;
		}
		return false;
	}
	
	public void showOrHideNavigationPanel() {
		isMainPanelVisible=!isMainPanelVisible;
		setSplitPane();
	}

	final private JPanel closerPanel, mainPanel, refreshPanel, northPanel, southPanel;
	private ToolbarScrollablePanel toolbarScrollablePanel; 
	private JComponent bottomControls;
	
	public static  JPanel getDecoratedPanel(final Color color) {
		return new JPanel(new BorderLayout()) {
			public Color getBackground() {
				return color;
			}

			public void setOpaque(final boolean b) {
				super.setOpaque(true);
			}
		};
	}
	JLabel hideButton, refreshButton;

	private void setHideButton() {
		hideButton = new JLabel("");
		hideButton.setToolTipText(HIDE_THE_TASK_PANEL);
		hideButton.setIcon(MmsIcons.getMinimize3());
		hideButton.setVerticalAlignment(SwingConstants.BOTTOM);
		hideButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showOrHideNavigationPanel();
			}
		});
	}
	
	private JLabel button2Label(final JButton button) {
		JLabel label = new JLabel("");
		label.setToolTipText(button.getToolTipText());
		label.setIcon(button.getIcon());
		label.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				button.doClick();
			}
		});
		return label;
	}
	
	private void setRefreshButton() {
		refreshButton = new JLabel("");
		refreshButton.setToolTipText(REFRESH_THE_TASK_PANEL);
		refreshButton.setIcon(MmsIcons.getSync());
		refreshButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				refresher.refresh();
			}
		});
	}
	
	private void setBottomControls() {		
		southPanel.setLayout(new BorderLayout());
		JMenuItem item = new JMenuItem("<html><body><center>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Click for screenshot</body></center></html>"); 
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent t) {
				HelpBasics.takeScreenShot(null);
			}
		});
		AccordionLite.Button button=getDecoratedButton(item, AccordionLite.COLOR_SUBLIST, AccordionLite.POSITION.LEFT, item, 1);
		southPanel.add(button);
		this.add(southPanel, BorderLayout.SOUTH);
	}
	final Color bgColorLabel;
	public AccordionLite(final Color fgColor, final Color bgColor, final Color bgColorLabel,
			final String fontName, final POSITION position, final JButton toolBarButton1, final JButton toolBarButton2,
			boolean needNavigationControls, boolean needBottomControls, boolean needMotionControls, JComponent bottomControls) {
		this.fgColor = fgColor;
		this.bgColor = bgColor;
		this.bgColorLabel=bgColorLabel;
		this.fontName = fontName;
		this.position = position;
		this.closerPanel = getDecoratedPanel(bgColor);
		this.mainPanel= getDecoratedPanel(bgColor);
		this.refreshPanel= getDecoratedPanel(bgColor);
		this.northPanel= getDecoratedPanel(bgColor);
		this.southPanel = getDecoratedPanel(bgColor);
		this.bottomControls = bottomControls;
		setHideButton();
		closerPanel.add(hideButton, BorderLayout.EAST);
		
		northPanel.setLayout(new BorderLayout());
		if (needNavigationControls) {
			northPanel.add(closerPanel, BorderLayout.EAST);
		}
		toolbarScrollablePanel = new ToolbarScrollablePanel(topPanel, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainPanel.validate();
			}
		}, MmsIcons.getGoUp2(), MmsIcons.getGoDown2(), false, bgColor);
		if (toolBarButton1 != null && toolBarButton2 != null) {			
			JPanel pan = getDecoratedPanel(bgColor);
			pan.setLayout(new FlowLayout());
			pan.add(button2Label(toolBarButton1));
			pan.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));
			pan.add(button2Label(toolBarButton2));
			northPanel.add(pan,BorderLayout.CENTER);			
		}

		setLayout(new BorderLayout());
		if (position == POSITION.LEFT || position == POSITION.RIGHT) {
			//mainPanel.add(northPanel, BorderLayout.NORTH);
			
			mainPanel.add(toolbarScrollablePanel,BorderLayout.CENTER);
			c.anchor = GridBagConstraints.NORTH;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			this.add(northPanel, BorderLayout.NORTH);
			this.add(mainPanel, BorderLayout.CENTER);
		}
		else {
			mainPanel.add(northPanel, BorderLayout.EAST);
			mainPanel.add(topPanel,BorderLayout.CENTER);
			c.anchor = GridBagConstraints.NORTH;
			c.gridheight = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.VERTICAL;
			c.weighty = 1.0;
			this.add(mainPanel, BorderLayout.WEST);
		}
		if (needBottomControls) {
			setBottomControls();
		}
		if (needMotionControls) {
			AccordionMotionListener listener = new AccordionMotionListener(this);
			this.addMouseListener(listener);
			this.addMouseMotionListener(listener);
		}
		
	}

	private void registerListener(JComponent component) {
		for (int i = 0; i < component.getComponentCount(); i++) {
			Component comp = component.getComponent(i);
			if (comp instanceof JButton) {
				((JButton) comp).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showOrHideNavigationPanel();
					}
				});
			}
		}
	}

	public JButton addTab(String name, int mnemonic, JComponent component, ActionListener[] action) {
		Tab tabInfo = new Tab(name, mnemonic, component, false, false, null, action);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}

	public int getTabSize() {
		return tabs.size();
	}

	public JButton addTab(String name, int mnemonic, Icon icon, JComponent component, ActionListener[] action) {
		Tab tabInfo = new Tab(name, mnemonic, icon, component, false, false, null, action);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}
	
	public JButton addTab(String name, int mnemonic, Icon icon, final SubListManager manager, ActionListener[] action) {
		Tab tabInfo = new Tab(name, mnemonic, icon, null, false, false, manager, action);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}
	
	public JButton addActionTab(String name, int mnemonic, Icon icon, ActionListener[] action) {
		Tab tabInfo = new Tab(name, mnemonic, icon, null, false, false, null, action);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}
	
	public JButton addIconTab(String name, int mnemonic, Icon icon, ActionListener[] action) {
		Tab tabInfo = new Tab(name, mnemonic, icon, null, false, false, null, action);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}
	
	public JButton addLabel(String name, int mnemonic, Icon icon, JComponent component) {
		Tab tabInfo = new Tab(name, mnemonic, icon, component, false, true, null, null);
		this.tabs.put(Basics.stripSimpleHtml(name), tabInfo);
		return tabInfo.getButton();
	}

	public interface SubListManager {
		public JPanel getSubList(Object context);
	}
	public JButton addTabToTab(final String tabName, final String name, int mnemonic, final Icon icon,
			final SubListManager manager, final int position) {
		return addTabToTab(tabName, name, mnemonic, icon, manager, position, null);
	}
	
	private Tab getTab(final String tabName) {
		Tab info = (Tab) tabs.get(Basics.stripSimpleHtml(tabName));
		if(info==null){
			Collection<Tab> values = tabs.values();
			for (Tab tabInfo : values) {
				if(tabInfo.getName().contains(tabName)){
					info=tabInfo;
					break;
				}
			}
		}
		return info;
	}
	
	public JButton addTabToTab(final String tabName, final String name, int mnemonic, final Icon icon,
			final SubListManager manager, final int position, final Object context) {
		Tab info = getTab(tabName);
		if (info != null) {
			JPanel panel2 = manager.getSubList(context);
			final Tab tabInfo = new Tab(name, mnemonic, icon, panel2, true, false, manager, null);
			info.addChild(tabInfo.getButton());
			info.component.add(tabInfo.getButton(), c, position);
			info.component.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tabInfo.getButton().getActionListeners()[0].actionPerformed(new ActionEvent(tabInfo.getButton(), 1,tabName+name));
				}
			},KeyStroke.getKeyStroke(Character.toLowerCase((char)tabInfo.getButton().getMnemonic())), JComponent.WHEN_IN_FOCUSED_WINDOW);			
			this.subTabs.put(Basics.stripSimpleHtml(name), tabInfo);
			return info.button;
		}
		return null;
	}
	
	public void addActionToTab(String tabName, final JButton button) {
		Tab info = (Tab) tabs.get(Basics.stripSimpleHtml(tabName));
		if (info != null) {
			JPanel panel = (JPanel) info.component;
			panel.add(button, c);
			info.component.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button.doClick(60);
				}
			},KeyStroke.getKeyStroke(Character.toLowerCase((char)button.getMnemonic())), JComponent.WHEN_IN_FOCUSED_WINDOW);	
		}		
	}
	
	public JButton addActionToSubTab(String tabName, ActionListener[] actionListeners) {
		Tab info = (Tab) subTabs.get(tabName);
		if(info==null){
			for (Tab tabInfo : (Collection<Tab>)subTabs.values()) {
				if(tabInfo.getName().contains(tabName)){
					info=tabInfo;
					break;
				}
			}
		}
		if (info != null) {
			for (ActionListener actionListener : actionListeners) {
				info.getButton().addActionListener(actionListener);
			}
			return info.getButton();
		}
		return null;
	}
	
	public void removeTab(String name) {
		this.tabs.remove(name);
	}

	public int getVisibleTab() {
		return this.visibleTab;
	}

	public void setVisibleTab(int visibleTab) {
		if (visibleTab > 0 && visibleTab < this.tabs.size() - 1) {
			this.visibleTab = visibleTab;
		}
	}

	
	public void render(boolean subTab) {
		this.topPanel.removeAll();

		JLabel lbl1 = getDecoratedLabel("<html><body>&nbsp;&nbsp;&nbsp;</body></html> ", 500, 15);
		JLabel lbl = getDecoratedLabel("", 500, 15);
		lbl.setIcon(MmsIcons.getGenie());
		JLabel lbl2 = getDecoratedLabel("", 500, 15);
		lbl2.setIcon(MmsIcons.getTodoIcon());
		lbl2.setVerticalAlignment(SwingConstants.BOTTOM);
		JPanel iconPanel = new JPanel(new FlowLayout()) {
			public Color getBackground() {
				return bgColor;
			}
			public void setOpaque(final boolean b) {
				super.setOpaque(true);
			}
		};
		iconPanel.add(lbl);
		JPanel todoPanel = new JPanel(new GridLayout(2,1)) {
			public Color getBackground() {
				return bgColor;
			}
			public void setOpaque(final boolean b) {
				super.setOpaque(true);
			}
		};
		todoPanel.add(getDecoratedLabel("<html><body>&nbsp;&nbsp;&nbsp;</body></html>", 600, 20));
		todoPanel.add(lbl2);
		iconPanel.add(todoPanel);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(iconPanel, BorderLayout.WEST);
		panel.add(lbl1, BorderLayout.CENTER);
		this.topPanel.add(panel, c);
		this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
		
		Tab tabInfo = null;
		int totalTabs = this.tabs.size();
		Iterator itr = this.tabs.keySet().iterator();
		for (int i = 0; i < totalTabs; i++) {
			String tabName = (String) itr.next();
			tabInfo = (Tab) this.tabs.get(Basics.stripSimpleHtml(tabName));
			this.topPanel.add(tabInfo.getButton(), c);
			if (tabInfo.component != null) {
				if (tabInfo.component instanceof JXCollapsiblePane) {
					JXCollapsiblePane jxPane = (JXCollapsiblePane)tabInfo.component;					
					Container pane = jxPane.getContentPane(); 
					if (pane.getComponentCount()>0 ) {
						Component comp[] = pane.getComponents();
						int pos = 0;
						for (int z=0; z<comp.length; z++) {
							final Component cp = comp[z];
							if (cp instanceof JButton) {
								String subTsk = ((JButton)cp).getText();
								if (subTabs.containsKey(Basics.stripSimpleHtml(subTsk))) {
									Tab tab = (Tab)subTabs.get(Basics.stripSimpleHtml(subTsk));
									pane.add(tab.getButton(), c, pos++);
									pane.add(tab.getComponent(), c, pos++);	
									jxPane.setCollapsed(true);
									if (tab.getComponent() instanceof JXCollapsiblePane) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(true);
									}
									if (lastOpenSubTabName.trim().equalsIgnoreCase(subTsk.trim())) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(false);
									} else if (lastOpenSubTabName.trim() != "" && subTsk.trim().endsWith(lastOpenSubTabName.trim())) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(false);
									}
								} else {
									pos++;
									this.topPanel.add(tabInfo.component, c);
								}
							}else {
								this.topPanel.add(tabInfo.component, c);
							}
						}	
					}
					
					String name1=removeBraces(tabName);
					String name2=removeBraces(lastOpenTabName);
					if (name2.trim().equalsIgnoreCase(name1.trim())) {
						jxPane.setCollapsed(false);
					}
					else {
						jxPane.setCollapsed(true);						
					}
				}
				this.topPanel.add(tabInfo.component, c);
			}
		}
		if (bottomControls != null) {
			this.topPanel.add(bottomControls, c);
		}
		new Timer().schedule(new TimerTask() {
    		public void run() {
    			topPanel.validate();
    			toolbarScrollablePanel.update();
    			mainPanel.validate();
    			validate();	
    			cancel();
    		}}, 1000);	
		
	}
	
	private String removeBraces(String tabName) {
		int symbol=tabName.indexOf("(");
		String tname= tabName;
		if (symbol != -1) {
			tname=tabName.substring(0,symbol);
		}
		return tname;
	}
	

	private ActionEvent lastActionEvent;
	public static int lastOpenTab = -1;
	public static String lastOpenTabName = "", lastOpenSubTabName = "";
	private boolean refresh;
	public void actionPerformed(ActionEvent e) {
		int currentTab = 0;
		lastActionEvent = e;
		for (final Iterator i = this.tabs.keySet().iterator(); i.hasNext();) {
			String tabName = (String) i.next();
			Tab tabInfo = (Tab) this.tabs.get(Basics.stripSimpleHtml(tabName));
			if (tabInfo.getButton() == e.getSource()) {
				this.visibleButton = tabInfo.getButton();
				if (!refresh) {
					if (visibleTab == currentTab && !toggle) {
						toggle = true;
					} else {
						toggle = false;
					}	
				}
				this.visibleTab = currentTab;
				render(false);
				lastOpenTab = currentTab;
				return;
			}
			currentTab++;
		}
		currentTab = 0;
		for (Iterator i = this.subTabs.keySet().iterator(); i.hasNext();) {
			final String tabName = (String) i.next();
			Tab tabInfo = (Tab) this.subTabs.get(Basics.stripSimpleHtml(tabName));
			if (tabInfo.getButton() == e.getSource()) {
				this.visibleSubButton = tabInfo.getButton();
				tabInfo.component = tabInfo.manager.getSubList(this.visibleSubButton);
				if (!refresh) {
					if (visibleSubTab == tabName && !subToggle) {
						subToggle = true;
					} else {
						subToggle = false;
					}	
				}
				this.visibleSubTab = tabName;
				render(true);
				return;
			}
			currentTab++;
		}
	}
	
	public void refreshCurrentSelection() {
		if (lastActionEvent != null) {
			refresh = true;
			actionPerformed(lastActionEvent);
			refresh = false;	
		}
		else {			
			refresh();
		}
	}
	
	public void refreshTab(String tabName) {
		Tab info=getTab(tabName);
		if (info != null) {
			this.visibleComponent=info.getComponent();
			if (this.visibleComponent == null) {
				info.setComponent(info.manager.getSubList(null));
			}
			lastActionEvent=new ActionEvent(info.getButton(),0,"");
			refreshCurrentSelection();
		}
	}

	class Tab {
		private String name;
		private int mnemonic;
		private JButton button;
		private Icon icon;
		private JComponent component;
		private SubListManager manager;
		private boolean isSubTab;
		private boolean isLabel;
		private ArrayList<JButton> children = new ArrayList<JButton>();
		
		public void addChild(JButton button) {
			children.add(button);
		}
		
		public boolean haveThisChild(JButton button) {
			return children.contains(button);
		}
		
		private void decorateButton(final ActionListener[] action) {
	
			class Button extends JButton implements StateAssociateAble{
				Button(String text){
					super(text);
				}
				public Color getForeground() {
					if (isSubTab) {
						return Color.BLACK;
					} else if (isLabel) {
						return bgColor;
					}
					return fgColor;
				}

				public Font getFont() {
					return new Font(fontName,
							isSubTab ? Font.PLAIN : Font.BOLD, isSubTab ? 12
									: 15);
				}
				
				public void setIcon(Icon i) {
					if (icon == null) {
						super.setIcon(i);
					}
					else {
						icon=i;
						repaint();	
					}
				}

				public void paintComponent(Graphics g) {
					Color color = COLOR_SUBLIST;
					if (isSubTab) {
						color = COLOR_SUBLIST;
					} else if (isLabel) {
						color = bgColorLabel;
					} else {
						color = bgColor;
					}
					g.setColor(color);
					g.fillRect(0, 0, getSize().width, getSize().height);
					if (icon != null) {
						int width = icon.getIconWidth();
						int height = icon.getIconHeight();
						BufferedImage img = new BufferedImage(width, height,
								BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2d = (Graphics2D) img.getGraphics();
						icon.paintIcon(null, g2d, 0, 0);
						g2d.dispose();
						if (position == POSITION.LEFT ||position == POSITION.RIGHT) {
							g.drawImage(img, isSubTab?20:5, getSize().height / 2 - height / 2,
									width, height, color, null);	
						}
						else {
							g.drawImage(img, isSubTab?20:5, getSize().height  - (height+5),
									width, height, color, null);
						}
					}
					if (isSubTab && component.getComponentCount() > 0) {
						Icon icon2 = MmsIcons.getArrowDownDoubleIcon();
						int width = icon2.getIconWidth();
						int height = icon2.getIconHeight();
						BufferedImage img = new BufferedImage(width, height,
								BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2d = (Graphics2D) img.getGraphics();
						icon2.paintIcon(null, g2d, 0, 0);
						g2d.dispose();
						g.drawImage(img, getSize().width - 20,
								getSize().height / 2 - height / 2, width,
								height, color, null);	
					}
					super.paintComponent(g);
				}
				private Object associatedState=null;
				public Object getAssociatedState(){
					return this.associatedState;
				}
				public void setAssociatedState(final Object o){
					this.associatedState=o;
				}
				public boolean hasAssociatedState(final Object state) {
					return Basics.equals(associatedState, state);
				}

			};
			
			this.button = new Button(Basics.stripSimpleHtml(name));
			this.button.setBorder(BorderFactory.createEmptyBorder());
			int iconMargin = icon == null || icon == MmsIcons.getBlank16Icon()?0:15;
	    	if (isSubTab) {
	    		this.button.setMargin(new Insets(0, 20+iconMargin,this.button.getWidth(), this.button.getHeight()));
			}
	    	if (SwingBasics.isMacLookAndFeel()){
	            this.button.setUI(new com.jgoodies.plaf.plastic.PlasticButtonUI());
	    	}

			this.button.setContentAreaFilled(false);
			this.button.setHorizontalAlignment(SwingConstants.LEFT);
			this.button.setVerticalTextPosition(0);
			if (action != null) {
				for (ActionListener al: action)
				this.button.addActionListener(al);				
			}
			this.button.setMnemonic(this.mnemonic);
		}
		
		 ActionListener collpapseListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Iterator itr = tabs.keySet().iterator();
				for (int i = 0; i < tabs.size(); i++) {
					String tabName = (String) itr.next();
					final Tab tabInfo = (Tab) tabs.get(Basics.stripSimpleHtml(tabName));
					if (tabInfo.component != null) {
					}
				}
				
				itr = subTabs.keySet().iterator();
				for (int i = 0; i < subTabs.size(); i++) {
					String tabName = (String) itr.next();
					Tab tabInfo = (Tab) subTabs.get(Basics.stripSimpleHtml(tabName));
					if (tabInfo.component != null) {
					}
				}
				new Timer().schedule(new TimerTask() {
    	    		public void run() {
    	    			topPanel.revalidate();
						mainPanel.revalidate();
    	    			toolbarScrollablePanel.update();
    	    		}}, 1000);
				
			}
		 };
		
		 
		 Tab(String name, int mnemonic, final Icon icon, final JComponent component,
				final boolean isSubTab, final boolean isLabel, final SubListManager manager, final ActionListener[] action) {
			this.name = name;
			this.mnemonic = mnemonic;
			this.component = component;
			this.icon = icon;
			this.manager = manager;
			this.isSubTab = isSubTab;
			this.isLabel = isLabel;
			decorateButton(action);
		}

		 Tab(String name, int mnemonic, final JComponent component, final boolean isSubTab,
				final boolean isLabel, final SubListManager manager, final ActionListener[] action) {
			 this(name, mnemonic, null, component, isSubTab, isLabel, manager, action);
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public JButton getButton() {
			return this.button;
		}

		public JComponent getComponent() {
			return this.component;
		}
		
		public void setComponent(JComponent component) {
			this.component=component;
		}
	}
	
	class AccordionMotionListener extends MouseAdapter {
		private JPanel movingPanel;
		private Point pt;

		public AccordionMotionListener(JPanel movingPanel) {
			this.movingPanel = movingPanel;
		}

		public void mouseDragged(MouseEvent e) {
			pt = SwingUtilities.convertPoint(movingPanel, e.getX(), e.getY(),
					movingPanel.getParent());
			movingPanel.setBounds(pt.x, pt.y, movingPanel.getWidth(),
					movingPanel.getHeight());
		}
		
		public void mouseReleased(MouseEvent e) {
			pt = SwingUtilities.convertPoint(movingPanel, e.getX(), e.getY(),
					movingPanel.getParent());
			movingPanel.setBounds(pt.x, pt.y, movingPanel.getWidth(),
					movingPanel.getHeight());
			Dimension d = SwingBasics.getScreenSizeMinusDockingBar(movingPanel);
			if ((e.getXOnScreen()+300) > d.getWidth()) {
				((AccordionLite)movingPanel).position = AccordionLite.POSITION.RIGHT;
			}
			else if (e.getXOnScreen() < 300) {
				((AccordionLite)movingPanel).position = AccordionLite.POSITION.LEFT;
			}
			((AccordionLite)movingPanel).refresh();
		}

	}
	
	
	public static class Button extends JButton implements StateAssociateAble{
		public Color getForeground() {
			if (!isEnabled()) {
				return Color.GRAY;
			}
			else {
				return Color.BLACK;
			}										
		}

		public void setOpaque(final boolean b) {
			super.setOpaque(true);
		}
		public Font getFont() {
			return FONT_SUBLIST;
		}
		
		public void setIcon(Icon i) {
			if (icon == null) {
				super.setIcon(i);
			}
			else {
				icon=i;
				repaint();	
			}
		}
		
		public void paintComponent(Graphics g) {
			if (isEnabled()) {				
				g.setColor(bgColor);
			} else {
				g.setColor(disabledBgColor!=null?disabledBgColor:COLOR_SUB_SUBLIST);
			}
			g.fillRect(0, 0, getSize().width, getSize().height);
			if (icon != null) {
				int width = icon.getIconWidth();
				int height = icon.getIconHeight();
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D)img.getGraphics();
				icon.paintIcon(null, g2d, 0, 0);
				g2d.dispose();
				g.drawImage(img, 15+level*5, getSize().height/2-height/2, width, height, isEnabled()?bgColor:COLOR_SUB_SUBLIST, null);
			}
			final Icon ic=getIcon();
			if (ic != null){
				if (getText().contains("Importance")){
					System.out.println("Importance button ("+this.hashCode()+") has Icon "+(ic==MmsIcons.getTickIcon()?"COMPLETE":"INCOMPLETE")+" for "+getAssociatedState());
				}
			} else {
				if (getText().contains("Importance")){
					System.out.println(this.hashCode()+" has NO Icon for "+getAssociatedState());
				}
			}
			super.paintComponent(g);
		}

		private final Color bgColor;
		private Color disabledBgColor;
		private Icon icon;
		private Object associatedState;
		private int level;
		public Button(final Object associatedState, final Icon icon, final Color bgColor, final String text, final int level){
			super(text);
			this.level=level;
			this.icon=icon;
			this.associatedState=associatedState;
			this.bgColor=bgColor;
		}
		public void setDisabledBgColor(Color disabledBgColor) {
			this.disabledBgColor = disabledBgColor;
		}
	

		public Object getAssociatedState(){
			return this.associatedState;
		}
		public void setAssociatedState(final Object o){
			this.associatedState=o;
		}
		public boolean hasAssociatedState(final Object state) {
			return Basics.equals(associatedState, state);
		}
	}
	
	public static JButton getAccordionButton() {
		return new JButton(){
		  	public Color getBackground() {
		        final Color background = WoodsideMenu.getBackground(WoodsideMenu.Style.TASK);
		        if (background != null) {
		            return background;
		        }
		        return super.getBackground();
		    }
		  	public Color getForeground() {
		  		return Color.WHITE;
		  	}
		   	public void setOpaque(final boolean b){
		   		super.setOpaque(false);
		   	}
		   	public Dimension getSize() {
				return new Dimension(getIcon().getIconWidth()-50,
						getIcon().getIconHeight()-50);
			}
		   	public void paintComponent(Graphics g) {
				g.fillRect(0, 0, getSize().width, getSize().height);
				final Icon icon = getIcon();
				if (icon != null) {
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage img = new BufferedImage(width, height,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = (Graphics2D) img.getGraphics();
					icon.paintIcon(null, g2d, 0, 0);
					g2d.dispose();
					g.drawImage(img, 5, getSize().height / 2 - height / 2,
							width, height, getBackground(), null);	
				}
				super.paintComponent(g);
			}
		};
	}

	public static Button getDecoratedButton(final JMenuItem item, final Color bgColor, 
			final POSITION position, 
			final Object associatedState,
			final int level) {
		Button button1 = new Button(associatedState, item.getIcon(), bgColor, Basics.stripSimpleHtml(item.getText().trim()), level) ;
		int iconMargin = item.getIcon() == null || item.getIcon() == MmsIcons.getBlank16Icon()?0:15;
    	if (level != 0) {
    		button1.setMargin(new Insets(0, 15+(level*5)+iconMargin,button1.getWidth(), button1.getHeight()));
		}
    	if (SwingBasics.isMacLookAndFeel()){
            button1.setUI(new com.jgoodies.plaf.plastic.PlasticButtonUI());
    	}

		button1.setToolTipText(item.getToolTipText());
		button1.setEnabled(item.isEnabled());
		button1.setContentAreaFilled(false);
		button1.setHorizontalAlignment(SwingConstants.LEFT);
		for (int i=0; i<item.getActionListeners().length; i++) {			
			button1.addActionListener(item.getActionListeners()[i]);
		}
		button1.setMnemonic(item.getMnemonic());
		return button1;
	}

}


