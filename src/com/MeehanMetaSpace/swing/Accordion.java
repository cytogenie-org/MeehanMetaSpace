package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXCollapsiblePane;

import com.MeehanMetaSpace.Basics;

public class Accordion extends JPanel implements ActionListener {
	
	public static final Color COLOR_SUB_SUBLIST = Color.WHITE;
	private static final String HIDE_THE_TASK_PANEL = "Hide the task bar";
	private static final String SHOW_THE_TASK_PANEL = "Show the task bar";
	private static final String REFRESH_THE_TASK_PANEL = "Refresh the task bar";
	public static Color COLOR_SUBLIST = Color.WHITE;
	public static final Font FONT_SUBLIST;
	public static final Font FONT_SUB_SUBLIST;
	static {
		final int size = UIManager.getFont("Label.font").getSize();
		System.out.println("Label.font size=="+size);
		FONT_SUBLIST = new Font("Calibri", Font.PLAIN, size-1);
		FONT_SUB_SUBLIST = new Font("Calibri", Font.PLAIN, size-1);
			
	}
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
	private static Color FONT_COLOR = new Color(2, 2, 20);
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
				return FONT_COLOR;
			}

			public Dimension getSize() {
				return new Dimension(super.getSize().width,
						(super.getSize().height + height));
			}

			public Font getFont() {
				return new Font(fontName, Font.PLAIN, fontsize);
			}

			public void paintComponent(Graphics g) {
				g.setColor(bgColor);
				g.fillRect(0, 0, getSize().width, getSize().height);
				super.paintComponent(g);
			}
		};
	}
	
	public static JLabel getSeparator() {
		return new JLabel("-----------") {
			public Color getForeground() {
				return FONT_COLOR;
			}

			public Dimension getSize() {
				return new Dimension(super.getSize().width,
						(super.getSize().height + 5));
			}

			public Font getFont() {
				return new Font(fontName, Font.PLAIN, 11);
			}

			public void paintComponent(Graphics g) {
				g.setColor(bgColor);
				g.fillRect(0, 0, getSize().width, getSize().height);
				super.paintComponent(g);
			}
		};
	}
	
	public static JLabel getDecoratedLabelWhiteBg(final String name, final int height, final int fontsize) {
		return new JLabel(name) {
			public Color getForeground() {
				return FONT_COLOR;
			}

			public Dimension getSize() {
				return new Dimension(super.getSize().width,
						(super.getSize().height + height));
			}

			public Font getFont() {
				return new Font(fontName, Font.PLAIN, fontsize);
			}

			public void paintComponent(Graphics g) {
				g.setColor(Color.WHITE);
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
			hideButton.setIcon(MmsIcons.getTableFreezeIcon());
			add(mainPanel, BorderLayout.CENTER);
			add(southPanel, BorderLayout.SOUTH);
			accordion_split_location=position == POSITION.LEFT?260:1050;
		} else {
			hideButton.setText("");
			hideButton.setToolTipText(SHOW_THE_TASK_PANEL);
			hideButton.setIcon(MmsIcons.getTableUnfreezeIcon());
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
	private JComponent additionalControls;
	
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
	
	public void addHideMouseListener(MouseAdapter ma) {
		hideButton.addMouseListener(ma);
	}

	private void setHideButton() {
		hideButton = new JLabel("");
		hideButton.setToolTipText(HIDE_THE_TASK_PANEL);
		hideButton.setIcon(MmsIcons.getTableFreezeIcon());
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
	
	private void setBottomControls(List<AbstractButton> additionalControls) {		
		southPanel.setLayout(new GridLayout(additionalControls.size(), 1));
		for (AbstractButton b: additionalControls) {
			Accordion.Button button=getDecoratedButton(b, COLOR_SUB_SUBLIST, POSITION.LEFT, null, 1);
			button.setFocusable(false);
			southPanel.add(button);
		}
		this.add(southPanel, BorderLayout.SOUTH);
	}
	
	private void setBottomControls(JMenuItem menuBottomControl) {		
		southPanel.setLayout(new BorderLayout());
		if (menuBottomControl != null) {
			Accordion.Button button=getDecoratedButton(menuBottomControl, Accordion.COLOR_SUBLIST, Accordion.POSITION.LEFT, menuBottomControl, 1);
			button.setFocusable(false);
			southPanel.add(button);
		}
		else {
			JMenuItem item = new JMenuItem("<html><body><center>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Click for screenshot</body></center></html>"); 
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent t) {
					HelpBasics.takeScreenShot(null);
				}
			});	
			Accordion.Button button=getDecoratedButton(item, Accordion.COLOR_SUBLIST, Accordion.POSITION.LEFT, item, 1);
			button.setFocusable(false);
			southPanel.add(button);
		}
		
		
		this.add(southPanel, BorderLayout.SOUTH);
	}
	final Color bgColorLabel;
	boolean isToDo=true;
	
	public void setIsToDo(boolean isToDo) {
		this.isToDo = isToDo;
	}
	public Accordion(Icon logo) {
		this(Accordion.COLOR_SUBLIST, Accordion.COLOR_SUBLIST, PersonalizableTable.YELLOW_STICKY_COLOR,
				"Calibri", Accordion.POSITION.LEFT, null, null, false, false, false, null, null, logo);
	}
	
	public Accordion(final Color fgColor, final Color bgColor, final Color bgColorLabel,
			final String fontName, final POSITION position, final JButton toolBarButton1, final JButton toolBarButton2,
			boolean needNavigationControls, boolean needBottomControls, boolean needMotionControls, 
			JComponent additionalControls, List<AbstractButton> additionalBottomControls, Icon logo) {
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
		this.additionalControls = additionalControls;
		if (logo != null) {
			this.logo = logo;
		}
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
			setBottomControls(additionalBottomControls);
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
	
	public Tab getTab(final String tabName) {
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
		 SubListManager manager, final int position, final Object context) {
		Tab info = getTab(tabName);
		if (info != null) {
			if (manager == null) {
				final Tab info2 = getTab(name);
				manager = new SubListManager() {
					
					@Override
					public JPanel getSubList(Object context) {
						final ArrayList<Component> buttons = new ArrayList<Component>();
						JXCollapsiblePane jxPane = (JXCollapsiblePane)info2.component;	
						Container pane = jxPane.getContentPane(); 
						if (pane.getComponentCount()>0 ) {
							Component comp[] = pane.getComponents();
							for (Component c: comp) {
								if (c instanceof JButton) {
									//JButton b = (JButton)c;
									//b.setText(setMargin(b.getText()));
								   //buttons.add(getDecoratedButton(b, COLOR_SUB_SUBLIST, POSITION.LEFT, null, 1));
									buttons.add(c);//Its already decorated
								}
								else if (c instanceof Component) {
									buttons.add(c);
								}
							}
							addButtons(buttons);
						}
						JXCollapsiblePane panel = new JXCollapsiblePane();
						panel.setLayout(new GridBagLayout());
						GridBagConstraints c = new GridBagConstraints();
						c.anchor = GridBagConstraints.NORTH;
						for (Component but : buttons) {
							c.gridwidth = GridBagConstraints.REMAINDER;
							c.fill = GridBagConstraints.HORIZONTAL;
							c.weightx = 1.0;
							c.insets= new Insets(5,5,5,5);
							panel.add(but, c);
							if (but instanceof JButton) {
								JButton button = (JButton) but;
								button.setCursor(new Cursor(Cursor.HAND_CURSOR));
								button.setBackground(Accordion.COLOR_SUB_SUBLIST);
								button.setBorder(BorderFactory.createEmptyBorder());
								//button.setText(setMargin(button.getText()));
								but.setFont(Accordion.FONT_SUBLIST);
								/*if (button.getText().length() > 30) {
									String txt = PopupBasics.wrap(button.getText(), 30,
											"<br>", true);
									button.setText("<html>" + txt + "</html>");
								}*/
								panel.registerKeyboardAction(
										button.getActionListeners()[0], KeyStroke
												.getKeyStroke(Character
														.toLowerCase((char) button
																.getMnemonic())),
										JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
							}
						}
						return panel;
					}
				};
				
			}
			
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
			return tabInfo.button;
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

	public void changeButtonText(String txtSource, String txtTarget) {
		for (Component cmp: buttons) {
			if (cmp instanceof JButton) {
				JButton btn = (JButton)cmp;
				if (btn.getText().contains(txtSource)) {
					btn.setText(btn.getText().replaceAll(txtSource, txtTarget));
				}
			}
		}
	}
	Icon logo = MmsIcons.getSpacer();
	public void setLogoIcon(Icon logo) {
		this.logo=logo;
		noImages = false;
		render(true);
	}
	boolean noImages = false;
	public void removeImages() {
		this.logo=MmsIcons.getSpacer();
		noImages = true;
		render(true);
	}
	
	public void render(boolean subTab) {
		this.topPanel.removeAll();
		this.topPanel.setBackground( Accordion.COLOR_SUBLIST);
		JLabel lbl1 = getDecoratedLabelWhiteBg("<html><body>&nbsp;&nbsp;&nbsp;</body></html> ", 500, 15);
		JPanel panel = new JPanel(new BorderLayout());
		if(!noImages) {
			JLabel lbl = getDecoratedLabelWhiteBg("", 500, 15);
			lbl.setIcon(logo);
			JLabel lbl2 = getDecoratedLabelWhiteBg("", 500, 15);
			if (isToDo) {
				lbl2.setIcon(MmsIcons.getTodoIcon());
			}
			else {
				lbl2.setIcon(MmsIcons.getMenuIcon());
			}
			lbl2.setVerticalAlignment(SwingConstants.BOTTOM);
			JPanel iconPanel = new JPanel(new FlowLayout()) {
				public Color getBackground() {
					return Color.WHITE;
				}
				public void setOpaque(final boolean b) {
					super.setOpaque(true);
				}
			};
			iconPanel.add(lbl);
			JPanel todoPanel = new JPanel(new GridLayout(2,1)) {
				public Color getBackground() {
					return Color.WHITE;
				}
				public void setOpaque(final boolean b) {
					super.setOpaque(true);
				}
			};
			todoPanel.add(getDecoratedLabelWhiteBg("<html><body>&nbsp;&nbsp;&nbsp;</body></html>", 600, 20));
			todoPanel.add(lbl2);
			iconPanel.add(todoPanel);
			panel.add(iconPanel, BorderLayout.WEST);
		}
		panel.add(lbl1, BorderLayout.CENTER);
		this.topPanel.add(panel, c);
		
		Tab tabInfo = null;
		int totalTabs = this.tabs.size();
		Iterator itr = this.tabs.keySet().iterator();
		for (int i = 0; i < totalTabs; i++) {
			String tabName = (String) itr.next();
			tabInfo = (Tab) this.tabs.get(Basics.stripSimpleHtml(tabName));
			/*if (tabInfo.getButton().getText() != null && !tabInfo.getButton().getText().trim().equals("")) {
				this.topPanel.add(tabInfo.getButton(), c);
			}*/
			if (tabInfo.component != null) {
				if (tabInfo.component instanceof JXCollapsiblePane) {
					JXCollapsiblePane jxPane = (JXCollapsiblePane)tabInfo.component;	
					jxPane.setBackground(Accordion.COLOR_SUBLIST);
					Container pane = jxPane.getContentPane(); 
					pane.setBackground(Accordion.COLOR_SUBLIST);
					if (pane.getComponentCount()>0 ) {
						Component comp[] = pane.getComponents();
						int pos = 0;
						for (int z=0; z<comp.length; z++) {
							final Component cp = comp[z];
							if (cp instanceof JButton) {
								String subTsk = ((JButton)cp).getText();
								((JButton)cp).setText(Basics.stripSimpleHtml(subTsk).trim());
								if (subTabs.containsKey(Basics.stripSimpleHtml(subTsk))) {
									Tab tab = (Tab)subTabs.get(Basics.stripSimpleHtml(subTsk));
									pane.add(tab.getButton(), c, pos++);
									tab.getButton().setBackground(Accordion.COLOR_SUB_SUBLIST);
									pane.add(tab.getComponent(), c, pos++);	
									((JPanel)tab.getComponent()).setBackground(Accordion.COLOR_SUB_SUBLIST);
									jxPane.setCollapsed(true);
									if (tab.getComponent() instanceof JXCollapsiblePane) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(true);
										((JXCollapsiblePane)tab.getComponent()).getContentPane().setBackground(Accordion.COLOR_SUB_SUBLIST);
									}
									if (lastOpenSubTabName.trim().equalsIgnoreCase(subTsk.trim())) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(false);
									} else if (lastOpenSubTabName.trim() != "" && subTsk.trim().endsWith(lastOpenSubTabName.trim())) {
										((JXCollapsiblePane)tab.getComponent()).setCollapsed(false);
									}
								} else {
									pos++;
									((JPanel)tabInfo.component).setBackground(Accordion.COLOR_SUB_SUBLIST);
									this.topPanel.add(tabInfo.component, c);
								}
							}else {
								((JPanel)tabInfo.component).setBackground(Accordion.COLOR_SUB_SUBLIST);
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
		if (additionalControls != null) {
			this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
			this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
			this.topPanel.add(additionalControls, c);
		}
		this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
		this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
		this.topPanel.add(getDecoratedLabel("<html>&nbsp;</html>", 100, 10), c);
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

	int buttonMargin = Basics.isMac()?-15:0;
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
					setFocusable(false);
				}
				public Color getForeground() {
					if (isSubTab) {
						return FONT_COLOR;
					} else if (isLabel) {
						return FONT_COLOR;
					}
					return FONT_COLOR;
				}

				public Font getFont() {
					/*if (isSubTab) {
						return FONT_SUB_SUBLIST;
					}*/
					return FONT_SUBLIST;
					//return new Font(fontName,Font.PLAIN, 12);
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
						color = COLOR_SUBLIST;
					} else {
						color = COLOR_SUBLIST;
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
						if (component instanceof JXCollapsiblePane && !((JXCollapsiblePane)component).isCollapsed()) {
							icon2 = MmsIcons.getArrowUpDoubleIcon();
						}
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
			int iconMargin = icon == null || icon == MmsIcons.getBlank16Icon()?0:15;
	    	if (isSubTab) {
	    		this.button.setMargin(new Insets(0, buttonMargin, this.button.getWidth(), this.button.getHeight()));
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
						if (tabInfo.component instanceof JXCollapsiblePane) {
							if (tabInfo.getButton() != arg0.getSource() && !tabInfo.haveThisChild((JButton)arg0.getSource())) {
								((JXCollapsiblePane)tabInfo.component).setCollapsed(true);								
							} else {
								lastOpenTabName = tabName;
								lastOpenSubTabName = "";
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {										
										((JXCollapsiblePane)tabInfo.component).transferFocus();						
									}
								});
							}
						}
					}
				}
				
				itr = subTabs.keySet().iterator();
				for (int i = 0; i < subTabs.size(); i++) {
					String tabName = (String) itr.next();
					Tab tabInfo = (Tab) subTabs.get(Basics.stripSimpleHtml(tabName));
					if (tabInfo.component != null) {
						if (tabInfo.component instanceof JXCollapsiblePane) {
							if (tabInfo.getButton() != arg0.getSource()) {
								((JXCollapsiblePane)tabInfo.component).setCollapsed(true);								
							} else {
								if (tabInfo.getButton() instanceof StateAssociateAble) {
									if (((StateAssociateAble)tabInfo.getButton()).getAssociatedState() != null) {
										((JXCollapsiblePane)tabInfo.component).setCollapsed(!((JXCollapsiblePane)tabInfo.component).isCollapsed());																			
									}
								}
								lastOpenSubTabName=tabName;
							}
						}
					}
				}
				new Timer().schedule(new TimerTask() {
    	    		public void run() {
    	    			topPanel.revalidate();
						mainPanel.revalidate();
    	    			toolbarScrollablePanel.update();
    	    		}}, 200);
				
			}
		 };
		
		 
		 Tab(String name, int mnemonic, final Icon icon, final JComponent component,
				final boolean isSubTab, final boolean isLabel, final SubListManager manager, final ActionListener[] action) {
			 if (name == null) {
				 this.name = ""; 
			 }
			 else {
				 this.name = name;
			 }
			this.mnemonic = mnemonic;
			this.component = component;
			this.icon = icon;
			this.manager = manager;
			this.isSubTab = isSubTab;
			this.isLabel = isLabel;
			decorateButton(action);
			if (component != null && component instanceof JXCollapsiblePane) {
				Action toggleAction = component.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
				if (toggleAction != null) {
					toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
							MmsIcons.getArrowDownDoubleIcon());
					toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
							MmsIcons.getArrowUpDoubleIcon());
					this.button.addActionListener(collpapseListener);
					((JXCollapsiblePane)component).setCollapsed(false);
					this.button.addActionListener(toggleAction);
				}
			}
		}
		 
		 public void addComponentItem(Button b) {
			 if (component != null && component instanceof JXCollapsiblePane) {
				 ((JXCollapsiblePane)component).add(b);
			 }
		 }
		 
		 public void addComponentItem(Component b) {
			 if (component != null && component instanceof JXCollapsiblePane) {
				 ((JXCollapsiblePane)component).add(b);
			 }
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
				((Accordion)movingPanel).position = Accordion.POSITION.RIGHT;
			}
			else if (e.getXOnScreen() < 300) {
				((Accordion)movingPanel).position = Accordion.POSITION.LEFT;
			}
			((Accordion)movingPanel).refresh();
		}

	}
	
	
	public static class Button extends JButton implements StateAssociateAble{
		boolean intendMe = false;
		int buttonMargin = Basics.isMac()?-15:0;
		public Color getForeground() {
			if (!isEnabled()) {
				return Color.GRAY;
			}
			else {
				if (super.getForeground() != Color.BLACK) {
					return super.getForeground();
				}
				return Color.BLACK;
			}										
		}
		
		public void setIndentMe(boolean intendMe) {
			this.intendMe = intendMe;
		}

		public void setOpaque(final boolean b) {
			super.setOpaque(true);
		}
		public Font getFont() {
			if (level == 0) {
				return FONT_SUBLIST;
			}
			return FONT_SUB_SUBLIST;
		}
		
		public void setIcon(Icon i) {
			if (icon == null) {//FIXME Why shouldnt we overwrite?
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
				g.drawImage(img, 0, getSize().height/2-height/2, width, height, isEnabled()?bgColor:COLOR_SUB_SUBLIST, null);
			}
			if (level == 0) {
				setMargin(new Insets(0, buttonMargin, 0, 0));
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

	public static Button getDecoratedButton(final AbstractButton item, final Color bgColor, 
			final POSITION position, 
			final Object associatedState,
			final int level) {
		Button button1 = new Button(associatedState, item.getIcon(), bgColor, Basics.stripSimpleHtml(item.getText().trim()), level) ;
    	if (SwingBasics.isMacLookAndFeel()){
            button1.setUI(new com.jgoodies.plaf.plastic.PlasticButtonUI());
    	}

		button1.setToolTipText(item.getToolTipText());
		button1.setEnabled(item.isEnabled());
		button1.setContentAreaFilled(false);
		button1.setFocusable(false);
		button1.setHorizontalAlignment(SwingConstants.LEFT);
		for (int i=0; i<item.getActionListeners().length; i++) {			
			button1.addActionListener(item.getActionListeners()[i]);
		}
		button1.setMnemonic(item.getMnemonic());
		button1.setBackground(item.getBackground());
		button1.setForeground(item.getForeground());
		button1.setHorizontalAlignment(item.getHorizontalAlignment());
		return button1;
	}
	
	static void initWindow(){
		SwingBasics.resetDefaultFonts();
    	Basics.gui=PopupBasics.gui;
    	PersonalizableTableModel.setRootDir(System.getProperty("user.home")+"/.mmsSwing");
    	final ColorPreferences colorProperties = ColorPreferences.instantiate();
    	colorProperties.setCurrentPreferences();
	}
	
	private static JPanel getAccordionSubList(ArrayList<Component> buttons,
			int level) {
		JXCollapsiblePane panel = new JXCollapsiblePane();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		for (Component but : buttons) {
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.insets= new Insets(5,5,5,5);
			panel.add(but, c);
			if (but instanceof JButton) {
				JButton button = (JButton) but;
				button.setCursor(new Cursor(Cursor.HAND_CURSOR));
				button.setBackground(Accordion.COLOR_SUB_SUBLIST);
				button.setBorder(BorderFactory.createEmptyBorder());
				but.setFont(Accordion.FONT_SUBLIST);
				/*if (button.getText().length() > 30) {
					String txt = PopupBasics.wrap(button.getText(), 30,
							"<br>", true);
					button.setText("<html>" + txt + "</html>");
				}*/
				panel.registerKeyboardAction(
						button.getActionListeners()[0], KeyStroke
								.getKeyStroke(Character
										.toLowerCase((char) button
												.getMnemonic())),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			}
		}
		return panel;
	}

	ArrayList<Component> buttons = new ArrayList<Component>();
	public void addButtons(final ArrayList<Component> buttons) {
		this.buttons.addAll(buttons);
	}
	public void addButton(JButton button) {
		this.buttons.add(button);
	}
	
	public static boolean expandFirstSubMenu= false;
	public static Accordion createAccordion(String header, Icon logo, LinkedHashMap<String, List<JButton>> data) {
		
		initWindow();
		Accordion accordion = new Accordion(logo);
		JButton root= accordion.addTab(header, (header != null && header.length()>0)?header.charAt(0):'z', new JXCollapsiblePane(), null);
		int pos = 0;
		boolean firstButton = true;
		for (Map.Entry<String, List<JButton>> entry: data.entrySet()) {
			final ArrayList<Component> buttons = new ArrayList<Component>();
			for (final JButton b: entry.getValue()) {
				if (data.get(entry.getKey()).size() > 1) {
					b.setText(setMargin(b.getText()));
				}
				buttons.add(getDecoratedButton(b, COLOR_SUB_SUBLIST, POSITION.LEFT, null, 1));
			}
			if (buttons.size() == 1) {
				Button bb = getDecoratedButton((JButton)buttons.get(0), COLOR_SUB_SUBLIST, POSITION.LEFT, null, 0);
				accordion.addActionToTab(header,bb);
				accordion.addButton(bb);
				pos++;
			}
			else{
				Accordion.SubListManager  manager = new Accordion.SubListManager () {
					public JPanel getSubList(final Object context) {
						return getAccordionSubList(buttons, 1);
					}
				};
				final JButton btn = accordion.addTabToTab(header, entry.getKey(), -1, null, manager, pos++);
				if (expandFirstSubMenu && firstButton) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {										
							btn.doClick(500);					
						}
					});
					firstButton = false;
				}
				
			}
			accordion.addButtons(buttons);
		}
		root.doClick(300);
		return accordion;
	}
	
	private static String setMargin(String text) {
		//if (text.indexOf("nbsp") == -1 ) {FIXME
			return "<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + text + "</html>";
		//}
		//return text;
	}
	
	private static LinkedHashMap<String, List<JButton>> getData() {
		LinkedHashMap<String, List<JButton>> data = new LinkedHashMap<String, List<JButton>>();
		List<JButton> buttons1 = new ArrayList<JButton>();
		buttons1.add(new JButton(new AbstractAction("Open", MmsIcons.getOpenIcon()) {//
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));
		buttons1.add(new JButton(new AbstractAction("Save", MmsIcons.getSaveIcon()) {//
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));
		
		/*List<JButton> buttons2 = new ArrayList<JButton>();
		buttons2.add(new JButton(new AbstractAction("Zoom in", MmsIcons.getMagnifyIcon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));
		buttons2.add(new JButton(new AbstractAction("Zoom out", MmsIcons.getBookOpenIcon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}));*/
		List<JButton> buttons3 = new ArrayList<JButton>();
		JButton btb1= new JButton(new AbstractAction("About", MmsIcons.getAcceptIcon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("About");
			}
		});
		JButton btb= new JButton(new AbstractAction("Exit", MmsIcons.getTickIcon()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		buttons3.add(btb1);
		buttons3.add(btb);
		
		data.put("File",buttons1);
		//data.put("View",buttons2);
		data.put("Exit",buttons3);
		return data;
	}
	
	public static void main(String args[]) {
		initWindow();
		JFrame frame = new JFrame("Accordion demo");
		
		
		Accordion ac = Accordion.createAccordion("Menus", MmsIcons.getGenie(), getData());
		ac.render(true);
		frame.add(ac);
		frame.setSize(400,400);
		frame.setVisible(true);
	}
}


