
package com.MeehanMetaSpace.swing;

//import com.ScienceXperts.cluetube.ClueTube;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import com.MeehanMetaSpace.*;
import com.MeehanMetaSpace.swing.PopupBasics.Input;




	

public final class SwingBasics {
    public static ImageIcon Resize(final ImageIcon icon, float factor){
    	if (icon==null)return null;
    	final int width=(int)(icon.getIconWidth()*factor);
    	final int height=(int)(icon.getIconHeight()*factor);
    	return Resize(icon, width, height, Image.SCALE_SMOOTH);
    }

    public static ImageIcon Resize(final ImageIcon icon, final int width, final int height){
    	return Resize(icon, width, height, Image.SCALE_SMOOTH);
    }
    public static ImageIcon Resize(final ImageIcon icon, final int width, final int height, final int hints){
    	return Resize(icon.getImage(), width, height, hints);
    }

    public static ImageIcon Resize(final Image img, final int width, final int height, final int hints){
    	final Image out=img.getScaledInstance(width, height, hints);
    	return new ImageIcon(out);
    }

    public static boolean highDef=false;
    public static double highDefIconFactor=2;
	
	public void setHighDef(final float factor) {
		toolBarFactor=factor;
		highDef=true;
	}
	
    public static Float widthFactor, heightFactor, toolBarFactor;
    public static void setResizingFactors(final float widthFactor, 
    		final float heightFactor, final float toolBarFactor){
    	SwingBasics.widthFactor=widthFactor;
    	SwingBasics.heightFactor=heightFactor;
    	SwingBasics.toolBarFactor=toolBarFactor;
    }
    
	static boolean initialized=false;
	public static void initialize(String appFolder, String appName, 
			final boolean highDef, final float widthFactor,  
			final float heightFactor, final float toolBarFactor) {
		if (initialized) 
			return;
		SwingBasics.highDef=highDef;
		setResizingFactors(widthFactor, heightFactor, toolBarFactor);
		initialized=true;
		if (appFolder==null)
			appFolder=System.getProperty("user.home") + "/test_table";
		if (appName==null)
			appName="Table test";
		PersonalizableTableModel.setRootDir(appFolder);
		if (ColorPreferences.lastOneInstantiated==null) {
			final ColorPreferences colorProperties = ColorPreferences.instantiate();
			colorProperties.setCurrentPreferences();
		}
		com.MeehanMetaSpace.Pel.init(IoBasics.concat(appFolder, "pel.log"), TabBrowser.class,
				appName, false);
		SwingBasics.resetDefaultFonts();
		PersonalizableTable.resetDefaultFonts();
	}

    public static ImageIcon ResizeIfNeeded(final ImageIcon in){
    	if (toolBarFactor==null){
    		setResizing(2000, 2000, 12);
    	}
    	if (toolBarFactor!=1){
    		return SwingBasics.Resize(in, toolBarFactor);
    	}
    	return in;
    }
    public static void setResizing(
    		final int maxHeight, 
    		final int maxWidth, 
    		final int normalFontSize) {
    	
    	final GraphicsEnvironment ge = GraphicsEnvironment.
    			getLocalGraphicsEnvironment();
    	widthFactor=1f;
    	heightFactor=1f;
    	toolBarFactor=1f;
    	final GraphicsDevice[] physicalScreens = ge.getScreenDevices();
    	for (int i = 0; i < physicalScreens.length; i++) {
    		final GraphicsConfiguration gc = physicalScreens[i].
    				getDefaultConfiguration();
    		final Rectangle physicalScreen = gc.getBounds();
    		if (physicalScreen != null &&
    				(physicalScreen.height>maxHeight || physicalScreen.width>maxWidth)){
    			toolBarFactor=(float)(UIManager.getFont("Label.font").getSize())
                        /normalFontSize;
    			heightFactor=(float)physicalScreen.height/(float)maxHeight;
    			widthFactor=(float)physicalScreen.width/(float)maxWidth;
    		}
    	}
    }
	
	public static String toHtmlRGB(final Color color){
        //final String s=color.toString();
        final StringBuilder sb=new StringBuilder("rgb(");
        sb.append(color.getRed());
        sb.append(", ");
        sb.append(color.getGreen());
        sb.append(", ");
        sb.append(color.getBlue());
        sb.append(")");
        return sb.toString();
    }

    private static final String
      PROPERTY_WINDOW_BUTTON_LAYOUT = "windowButtonLayout",
    PROPERTY_TAB_BUTTON_LAYOUT = "tabButtonLayout",
    PROPERTY_BUTTON_EQUAL_SIZES = "buttonEqualSizes",
    PROPERTY_TASK_BUTTON_WATER_MARKS = "taskButtonWaterMarks",
    PROPERTY_WIZARD_ICONS = "wizardIcons",
    PROPERTY_POPUP_MENU_BUTTON = "popupMenuButtons",
    PROPERTY_TEAR_AWAY_BUTTON = "tearAwayButton",
    PROPERTY_BUTTON_MARGIN_SIZE = "buttonMarginSize"; 
    
    public static final String RESTART_MSG="A restart may be necessary before <br>all changes take effect.";
    
    public static void showUpFront(final Window w, final JComponent focus) {
        showUpFront(w,focus,false);
    }


    private static boolean isMouseonSubMenu(MenuElement[] items){
    	boolean isMouseOnSubMenu=false;
    	for(int i=0;i<items.length;i++){
    		MenuElement item=items[i];
    		Point point=item.getComponent().getMousePosition();
    		if(point!=null){
    			return true;
    		}
    		isMouseOnSubMenu=isMouseonSubMenu(item.getSubElements());
    		if(isMouseOnSubMenu){
    			return true;
    		}
    	}
    	
    	return isMouseOnSubMenu;
    }
    
    public static void show(final JPopupMenu popup,final Component b, final boolean isWoodsideMenu){
    	final Color bg2 = b.getBackground();
    	final Color bg;
    	if (!bg2.equals(Color.gray)) {
			bg = b.getBackground();
			b.setBackground(Color.gray);
			if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
				b.getParent().setBackground(Color.gray);
			}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
				b.getParent().setBackground(Color.gray);
			}
		} else {
			bg = null;
			return;
		}
    	popup.addPopupMenuListener(new PopupMenuListener() {
    		
    		public void popupMenuCanceled(PopupMenuEvent e) {    			
    		}
    		
    		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    			if (bg!=null){
    				b.setBackground(bg);
    				if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
    					b.getParent().setBackground(bg);
    				}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
    					b.getParent().setBackground(bg);
    				}
    				popup.removePopupMenuListener(this);
    		    	ToolTipOnDemand.doNotShowOnEntry(popup.getSubElements());
    			}
				ToolTipManager.sharedInstance().setEnabled(true);
    		}
    		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

    		}
    	});
    	ToolTipManager.sharedInstance().setEnabled(false);
    	Properties properties = PopupBasics.getProperties(null);
		if (properties != null && SwingBasics.ButtonPreferences_DropDownMenuStyleOption_ON.equals(
				properties.getProperty(SwingBasics.ButtonPreferences_DropDownMenuStyle))) {
			if ((b instanceof HelpBasics.Button) || (b instanceof MouseHoverButton && ((MouseHoverButton)b).startedByMouse) || (b instanceof TaskButton && ( (TaskButton)b).startedByMouse) ){
	    		final Timer timer = new Timer();
	    		timer.scheduleAtFixedRate(new TimerTask() {
	    		public void run() {
	    			try {
	    				SwingUtilities.invokeAndWait(new Runnable() {
	    					public void run() {
	    						Point point = popup.getMousePosition(true);
	    						if (point == null && !isMouseonSubMenu(popup.getSubElements())) {
	    							try {
	    								point = b.getMousePosition();
	    								if (point == null) {
												if ((b instanceof TaskButton && WoodsideMenu.Style.TASK
														.equals(((TaskButton) b)
																.getStyle()))
														|| (b instanceof HelpBasics.Button && ((HelpBasics.Button) b)
																.isWoodSideMenuHelpButton())) {
													if (b.getParent()
															.getMousePosition() == null) {
														popup.setVisible(false);
														timer.cancel();
													}
												} else {
													popup.setVisible(false);
													timer.cancel();
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							});
	    			} catch (final Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	    	}, 100, 100);
	    	}
		}
    	ToolTipOnDemand.doNotShowOnEntry(popup.getSubElements());
    	ToolTipOnDemand.getSingleton().showOnEntry(popup.getSubElements());
    	if(isWoodsideMenu && b instanceof TaskButton && WoodsideMenu.Style.CONTEXT.equals(((TaskButton)b).getStyle())){
        		int subWidth=popup.getWidth();
    			if(subWidth==0){
    				subWidth=139;
    			}
    			popup.show(b, ((b.getWidth()/2))-(subWidth/2), b.getHeight());
    	}else if(isWoodsideMenu && b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
    		popup.show(b.getParent(), 0, b.getParent().getHeight());
    	}else if(isWoodsideMenu && b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
    		popup.show(b.getParent(), b.getParent().getWidth(), b.getParent().getHeight());
    	}else{
    		popup.show(b, 0, b.getHeight());
    	}
    }

    public static void show(final JPopupMenu popup,final Component b){
    	show(false, popup,b);
    }

    public static void show(final boolean isWoodsideMenu, final JPopupMenu popup,final Component b){
    	final Color bg2 = b.getBackground();
    	final Color bg;
    	if (!bg2.equals(Color.gray)) {
			bg = b.getBackground();
			b.setBackground(Color.gray);
			if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
				b.getParent().setBackground(Color.gray);
			}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
				b.getParent().setBackground(Color.gray);
			}
		} else {
			bg = null;
		}
    	popup.addPopupMenuListener(new PopupMenuListener() {
    		
    		public void popupMenuCanceled(PopupMenuEvent e) {    			
    		}
    		
    		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    			if (bg!=null){
    				b.setBackground(bg);
    				if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
    					b.getParent().setBackground(bg);
    				}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
    					b.getParent().setBackground(bg);
    				}
    				popup.removePopupMenuListener(this);
    		    	ToolTipOnDemand.doNotShowOnEntry(popup.getSubElements());
    			}
				ToolTipManager.sharedInstance().setEnabled(true);
    		}
    		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

    		}
    	});
    	ToolTipManager.sharedInstance().setEnabled(false);
    	Properties properties = PopupBasics.getProperties(null);
    	if (properties != null && SwingBasics.ButtonPreferences_DropDownMenuStyleOption_ON.equals(
    			properties.getProperty(SwingBasics.ButtonPreferences_DropDownMenuStyle))) {
    		if ((b instanceof HelpBasics.Button && (( HelpBasics.Button)b).startedByMouse) || (b instanceof MouseHoverButton && ((MouseHoverButton)b).startedByMouse) || (b instanceof TaskButton && ( (TaskButton)b).startedByMouse) ){
        		final Timer timer = new Timer();
        		timer.scheduleAtFixedRate(new TimerTask() {
        		public void run() {
        			try {
        				SwingUtilities.invokeAndWait(new Runnable() {
        					public void run() {
        						Point point = popup.getMousePosition(true);
        						if (point == null && !isMouseonSubMenu(popup.getSubElements())) {
        							try {
        								point = b.getMousePosition();
        								if (point == null) {
    										if ((b instanceof TaskButton && WoodsideMenu.Style.TASK
    												.equals(((TaskButton) b)
    														.getStyle()))
    												|| (b instanceof HelpBasics.Button && ((HelpBasics.Button) b)
    														.isWoodSideMenuHelpButton())) {
    											if (b.getParent()
    													.getMousePosition() == null) {
    												popup.setVisible(false);
    												timer.cancel();
    											}
    										} else {
    											popup.setVisible(false);
    											timer.cancel();
    										}
    									}
        							} catch (Exception e) {
        								e.printStackTrace();
        							}
        						}
        					}
        				});
        			} catch (final Exception e) {
        				e.printStackTrace();
        			}
        		}
        	}, 100, 100);
        	}
    	}
    	ToolTipOnDemand.doNotShowOnEntry(popup.getSubElements());
    	ToolTipOnDemand.getSingleton().showOnEntry(popup.getSubElements());
    	if(isWoodsideMenu && b instanceof TaskButton && WoodsideMenu.Style.CONTEXT.equals(((TaskButton)b).getStyle())){
    		int subWidth=popup.getWidth();
    			if(subWidth==0){
    				subWidth=139;
    			}
    			popup.show(b, ((b.getWidth()/2))-(subWidth/2), b.getHeight());
    	}else if(isWoodsideMenu && b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle())){
    		popup.show(b.getParent(), 0, b.getParent().getHeight());
    	}else if(isWoodsideMenu && b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()){
    		popup.show(b.getParent(), b.getParent().getWidth(), b.getParent().getHeight());
    	}else{
    		popup.show(b, 0, b.getHeight());
    	}

    }
    
    public static Point nextShowUpFrontLocation=null;
    public static void showUpFront(final Window w, final JComponent focus, final boolean alwaysOnTop) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!alwaysOnTop){
                    w.toFront();
                }
                if (focus != null) {
                	if (isMacLookAndFeel()) {
                		SwingUtilities.invokeLater(new Runnable() {public void run() {
                			SwingUtilities.invokeLater(new Runnable() {public void run() {
                				SwingUtilities.invokeLater(new Runnable() {public void run() {
                            				focus.requestFocus();
                		}});
                			}});
                		}});
                	} else {
                		focus.requestFocus();
                	}
                } else {
                    w.requestFocus();
                }
                if (alwaysOnTop){
                    w.setAlwaysOnTop(true);
                }
                if (nextShowUpFrontLocation!=null){
	                w.setLocation(nextShowUpFrontLocation);
                }
            }
        });
        java.awt.Dialog priorActiveModal=activeModal;
        if ( w instanceof java.awt.Dialog){
        	isModalActive= ( (java.awt.Dialog)w).isModal();
        	activeModal=(java.awt.Dialog)w;
        }
        w.setVisible(true);
        if (isModalActive ){
        	if (runAfterModalConcludes != null){
        		runAfterModalConcludes.run();
        		runAfterModalConcludes=null;
        	}
            isModalActive=false;
        }
        activeModal=priorActiveModal;
    }
    public static java.awt.Dialog activeModal;
    public static boolean isModalActive=false;
    public static Runnable runAfterModalConcludes;

	public static void refocusAndRestoreMainFrame() {
		refocusAndRestore(mainFrame);
	}
	public static void refocusAndRestore(final JFrame frame) {
		if (!isModalActive) {
			if (frame.getExtendedState() == Frame.ICONIFIED) {
				frame.setExtendedState(Frame.NORMAL);
			}
			frame.requestFocus();
		} else {
			SwingBasics.runAfterModalConcludes = new Runnable() {

				public void run() {
					refocusAndRestore(frame);
				}
			};
		}
	};

    public final static String ButtonPreferences_DropDownMenuStyle ="Dropdown Menu Style";
    public final static String ButtonPreferences_DropDownMenuStyleOption_ON ="On";
    public final static String ButtonPreferences_DropDownMenuStyleOption_OFF ="Off";
    
    public final static String ButtonPreferences_VenetianStyle ="Venetian Style";
    public final static String ButtonPreferences_VenetianStyleOption_ON ="On";
    public final static String ButtonPreferences_VenetianStyleOption_OFF ="Off";
    public static Preferences.TabContributor getButtonPreferences() {
        return new Preferences.TabContributor() {
            private PropertyGui.Editor windowsLayout, tabsLayout,
            equalSizesEditor, /*waterMarksEditor,*/
            wizardIconsEditor, marginSizeEditor, popupMenuButtonEditor,
            tearAwayButtonEditor;
            String mainMenuOption="Tab";
            String mainTaskOption="Accordion";
            String dropDownMenuOption="Off";
            String venetianBlindOption="Off";
            private String menuStyle = "Menu Style";
            private String taskStyle = "Task Style";
            private Properties properties;
            public void contribute(final JTabbedPane tabs) {
                properties = PopupBasics.getProperties(null);
                final JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                PropertyGui.Factory booleanPropertyEditorFactory = PropertyGui.
                  getFactory(
                    Boolean.class);

                
                final JPanel appearancePanel = new JPanel();
                appearancePanel.setBorder(BorderFactory.createTitledBorder(
                  "Appearance"));
                PropertyGui.Factory integerPropertyEditorFactory = PropertyGui.
                  getFactory(
                    Integer.class);
                equalSizesEditor = booleanPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_BUTTON_EQUAL_SIZES,
                  "Equal sizes   ",
                  "Check to give the same size to <br>all buttons in the same group",
                  Boolean.valueOf(buttonEqualSizes));
                wizardIconsEditor = booleanPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_WIZARD_ICONS,
                  "Wizard icons   ",
                  "Check to show pictures on wizard <br>buttons.  These are the workflow buttons<br>with arrows.",
                  Boolean.valueOf(wizardIcons));

                marginSizeEditor = integerPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_BUTTON_MARGIN_SIZE,
                  "Margin size",
                  "Enter the margin size<br>of the button interior<br>(pixels)",
                  new Integer(buttonMargin));

                equalSizesEditor.bind(appearancePanel);
                /*waterMarksEditor.bind(appearancePanel);*/
                wizardIconsEditor.bind(appearancePanel);
                marginSizeEditor.bind(appearancePanel);
                mainPanel.add(appearancePanel);
               
                final JPanel mainMenuPanel=new JPanel();
                mainMenuPanel.setBorder(BorderFactory.createTitledBorder(
                "Main menu"));
                JRadioButton suckerFishButton = new JRadioButton(WoodsideMenu.CONTEXT);
                suckerFishButton.setBackground(mainPanel.getBackground());
        		JRadioButton tabButton = new JRadioButton("Tab");
        		
        		tabButton.setBackground(mainPanel.getBackground());
        		
        		ButtonGroup myButtonGroup = new ButtonGroup();
        		myButtonGroup.add(suckerFishButton);
        		myButtonGroup.add(tabButton);

        		suckerFishButton.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				mainMenuOption = WoodsideMenu.CONTEXT;
        			}
        			
        		});
        		
        		tabButton.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				mainMenuOption = "Tab";
        			}
        			
        		});
        		
        		if (properties.getProperty(menuStyle) != null) {
					if (properties.getProperty(menuStyle).equals("Tab")) {
						mainMenuOption="Tab";
						tabButton.setSelected(true);
					} else {
						mainMenuOption=WoodsideMenu.CONTEXT;
						suckerFishButton.setSelected(true);
					}
				} else {
					mainMenuOption="Tab";
					tabButton.setSelected(true);
				}
        		
        		mainMenuPanel.add(suckerFishButton);
        		mainMenuPanel.add(tabButton);
                mainPanel.add(mainMenuPanel);
                
                
        		ButtonGroup myChetanGroup = new ButtonGroup();
        		JRadioButton accordion = new JRadioButton("Accordion");
        		accordion.setBackground(mainPanel.getBackground());
        		JRadioButton tskMenu = new JRadioButton("Menu");
        		tskMenu.setBackground(mainPanel.getBackground());
        		
        		final JPanel mainTaskPanel=new JPanel();
        		mainTaskPanel.setBorder(BorderFactory.createTitledBorder("Task display"));
                
        		myChetanGroup.add(tskMenu);
        		myChetanGroup.add(accordion);
        		
        		mainTaskPanel.add(accordion);
        		mainTaskPanel.add(tskMenu);
        		mainPanel.add(mainTaskPanel);
        		
                accordion.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent arg0) {
        				mainTaskOption = "Accordion";
        			}
        			
        		});
                
                tskMenu.addActionListener(new ActionListener() {
        			public void actionPerformed(ActionEvent arg0) {
        				mainTaskOption = "Menu";
        			}
        			
        		});
                
                if (properties.getProperty(taskStyle) != null) {
					if (properties.getProperty(taskStyle).equals("Menu")) {
						mainTaskOption="Menu";
						tskMenu.setSelected(true);
					} else {
						mainTaskOption="Accordion";
						accordion.setSelected(true);
					}
				} else {
					mainTaskOption="Accordion";
					accordion.setSelected(true);
				}
                
                //Mouse hover drop down feature
                final JPanel dropDownMenuPanel=new JPanel();
                dropDownMenuPanel.setBorder(BorderFactory.createTitledBorder(
                "Mouse hover drop down menu"));
                JRadioButton dropDownMenuOn = new JRadioButton("On");
                dropDownMenuOn.setBackground(mainPanel.getBackground());
        		JRadioButton dropDownMenuOff  = new JRadioButton("Off");
        		dropDownMenuOff.setBackground(mainPanel.getBackground());
        		
        		ButtonGroup dropDownMenuButtonGroup = new ButtonGroup();
        		dropDownMenuButtonGroup.add(dropDownMenuOn);
        		dropDownMenuButtonGroup.add(dropDownMenuOff);

        		dropDownMenuOn.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				dropDownMenuOption = "On";
        			}
        			
        		});
        		
        		dropDownMenuOff.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				dropDownMenuOption = "Off";
        			}
        			
        		});
        		
        		if (properties.getProperty(ButtonPreferences_DropDownMenuStyle) != null) {
					if (properties.getProperty(ButtonPreferences_DropDownMenuStyle).equals("On")) {
						dropDownMenuOption="On";
						dropDownMenuOn.setSelected(true);
					} else {
						dropDownMenuOption="Off";
						dropDownMenuOff.setSelected(true);
					}
				} else {
					dropDownMenuOption="Off";
					dropDownMenuOff.setSelected(true);
				}
        		
        		dropDownMenuPanel.add(dropDownMenuOn);
        		dropDownMenuPanel.add(dropDownMenuOff);
                mainPanel.add(dropDownMenuPanel);
                
                //Venetian blind feature
                final JPanel venetianMenuPanel=new JPanel();
                venetianMenuPanel.setBorder(BorderFactory.createTitledBorder(
                	"Venetian blind effect"));
                JRadioButton venetianMenuOn = new JRadioButton("On");
                venetianMenuOn.setBackground(mainPanel.getBackground());
        		JRadioButton venetianMenuOff  = new JRadioButton("Off");
        		venetianMenuOff.setBackground(mainPanel.getBackground());
        		
        		ButtonGroup venetianMenuButtonGroup = new ButtonGroup();
        		venetianMenuButtonGroup.add(venetianMenuOn);
        		venetianMenuButtonGroup.add(venetianMenuOff);

        		venetianMenuOn.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				venetianBlindOption = "On";
        			}
        			
        		});
        		
        		venetianMenuOff.addActionListener(new ActionListener() {

        			public void actionPerformed(ActionEvent arg0) {
        				venetianBlindOption = "Off";
        			}
        			
        		});
        		
        		if (properties.getProperty(ButtonPreferences_VenetianStyle) != null) {
					if (properties.getProperty(ButtonPreferences_VenetianStyle).equals("On")) {
						venetianBlindOption="On";
						venetianMenuOn.setSelected(true);
					} else {
						venetianBlindOption="Off";
						venetianMenuOff.setSelected(true);
					}
				} else {
					venetianBlindOption="Off";
					venetianMenuOff.setSelected(true);
				}
        		
        		venetianMenuPanel.add(venetianMenuOn);
        		venetianMenuPanel.add(venetianMenuOff);
                mainPanel.add(venetianMenuPanel);
                
                //
                final JPanel tablePanel = new JPanel();
                tablePanel.setBorder(BorderFactory.createTitledBorder(
                  "Table top right buttons"));
                popupMenuButtonEditor = booleanPropertyEditorFactory.
                                        instantiate(
                                          properties,
                                          PROPERTY_POPUP_MENU_BUTTON,
                                          "Right-click popup menu   ",
                                          "Check if you wish a button on the top of tables<br> for activating the right-click popup menu.",
                                          Boolean.valueOf(popupMenuButton));
                popupMenuButtonEditor.bind(tablePanel);
                tearAwayButtonEditor = booleanPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_TEAR_AWAY_BUTTON,
                  "Seperate window",
                  "Check if you wish a button on the top of tables<br>for tearing away that table into<br>a separate window.",
                  Boolean.valueOf(tearAwayButton));
                tearAwayButtonEditor.bind(tablePanel);
                mainPanel.add(tablePanel);

                final JPanel layoutPanel = new JPanel();
                layoutPanel.setBorder(BorderFactory.createTitledBorder(
                  "Location at bottom"));
                final String layoutToolTip =
                  "Check to put buttons on the bottom<br>otherwise buttons are placed at the top";
                windowsLayout = booleanPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_WINDOW_BUTTON_LAYOUT,
                  "For popup windows   ",
                  layoutToolTip,
                  Boolean.valueOf(normalButtonLayoutForWindows));
                tabsLayout = booleanPropertyEditorFactory.instantiate(
                  properties,
                  PROPERTY_TAB_BUTTON_LAYOUT,
                  "For main window ",
                  layoutToolTip,
                  Boolean.valueOf(normalButtonLayoutForTabs));
                windowsLayout.bind(layoutPanel);
                tabsLayout.bind(layoutPanel);
                mainPanel.add(layoutPanel);

                mainPanel.add(
                  new JLabel(
                    "Some changes may only take effect after a restart",
                    JLabel.CENTER));
                tabs.add("Buttons", mainPanel);
            }

            public void reset() {
            	mainMenuOption="Tab";
            	mainTaskOption="Accordion";
				properties.setProperty(menuStyle, mainMenuOption);
				properties.setProperty(taskStyle, mainTaskOption);
				dropDownMenuOption="Off";
				venetianBlindOption="Off";
				properties.setProperty(ButtonPreferences_DropDownMenuStyle, dropDownMenuOption);
				properties.setProperty(ButtonPreferences_VenetianStyle, venetianBlindOption);
            }
            
            public void conclude(final boolean save) {
                if (save) {
                    normalButtonLayoutForTabs =
                      ((Boolean) tabsLayout.read()).booleanValue();
                    normalButtonLayoutForWindows =
                      ((Boolean) windowsLayout.read()).booleanValue();
                    buttonEqualSizes = ((Boolean) equalSizesEditor.read()).
                                       booleanValue();
                    taskButtonWaterMarks=false;/*((Boolean) waterMarksEditor.read()).booleanValue();*/
                    wizardIcons = ((Boolean) wizardIconsEditor.read()).
                                  booleanValue();
                    buttonMargin = ((Integer) marginSizeEditor.read()).intValue();

                    BUTTON_INSETS = new Insets(buttonMargin, buttonMargin,
                                               buttonMargin,
                                               buttonMargin);

                    popupMenuButton = ((Boolean) popupMenuButtonEditor.read()).
                                      booleanValue();
                    tearAwayButton = ((Boolean) tearAwayButtonEditor.read()).
                                     booleanValue();
                    if(!mainMenuOption.equals(properties.getProperty(menuStyle)) ||
                    		!mainTaskOption.equals(properties.getProperty(taskStyle)) 
                    		|| !dropDownMenuOption.equals(properties.getProperty(
                    				ButtonPreferences_DropDownMenuStyle)))
                        PopupBasics.alert(
                                Basics.toHtmlUncentered("Menu changes made...",
                                		RESTART_MSG));                         
                    properties.setProperty(menuStyle, mainMenuOption );
                    properties.setProperty(taskStyle, mainTaskOption );
                    properties.setProperty(ButtonPreferences_DropDownMenuStyle, dropDownMenuOption );
                    properties.setProperty(ButtonPreferences_VenetianStyle, venetianBlindOption );
                    final String fileName = PopupBasics.getPropertyFileName(
                      properties);
                    if (fileName != null) {
                        PropertiesBasics.saveProperties(properties, fileName, null);
                    }
                             
                }
            }
        };
    }

	
    public static void resetDefaultFonts(){
    	FONT_FACE_FAVORITE = UIManager.getFont("Label.font");
    }

    {
        resetDefaultFonts();
    }

    public static Font FONT_FACE_FAVORITE=null;//UIManager.getFont("Label.font");

	private static String BAD_LOOK_AND_FEEL_FOR_SUCKER_FISH="com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

	public static String getDefaultLookAndFeelClassName() {
		String lookAndFeelClassName = null;
		if (isMac) {
			if (isQuaQuaCapable()) {
				lookAndFeelClassName = ch.randelshofer.quaqua.QuaquaManager
						.getLookAndFeel().getClass().getName();
			}
		} else {
			lookAndFeelClassName = com.pagosoft.plaf.PgsLookAndFeel.class
					.getName();
		}
		return lookAndFeelClassName;
	}
	
    public static void doDefaultLnF() {
        final String defaultLnF = getDefaultLookAndFeelClassName();
// Load the native look and feel, if possible
        try {
            UIManager.setLookAndFeel(defaultLnF);
            resetDefaultFonts(FONT_FACE_FAVORITE);
            favoriteFontSet = true;

        } catch (Exception ex) {
            System.err.println("Could not load the native look & feel");
        }
    }

    public static void setWindowFromProperties(
      final Window containingWnd,
      final Properties properties,
      final String prefix) {
        setWindowFromProperties(containingWnd, properties, prefix, true);
    }

    public static boolean setWindowFromProperties(
      final Window containingWnd,
      final Properties properties,
      final String prefix,
      final boolean sizeMatters
      ) {
        return setWindowFromProperties(
          containingWnd,
          properties,
          sizeMatters ? prefix + "W" : null,
          sizeMatters ? prefix + "H" : null,
          prefix + "X",
          prefix + "Y",
          prefix + "M");
    }

    public static boolean setWindowFromProperties(
      final Window containingWnd,
      final Properties properties,
      final String widthProperty,
      final String heightProperty,
      final String xProperty,
      final String yProperty,
      final String maximizedProperty
      ) {
        final Dimension startSize = containingWnd.getSize();
        final Point startLocation = containingWnd.getLocation();
        final int x = PropertiesBasics.getProperty(properties, xProperty,
                                                   startLocation.x);
        final int y = PropertiesBasics.getProperty(properties, yProperty,
                                                   startLocation.y);
        containingWnd.setLocation(x, y);
        if (widthProperty != null && heightProperty != null) {
            final int w = PropertiesBasics.getProperty(properties,
              widthProperty,
              startSize.width);
            final int h = PropertiesBasics.getProperty(properties,
              heightProperty,
              startSize.height);
            containingWnd.setSize(w, h);
        }
        if (containingWnd instanceof Frame) {
            containingWnd.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    setPropertiesFromWindow(
                      containingWnd,
                      properties,
                      widthProperty,
                      heightProperty,
                      xProperty,
                      yProperty,
                      maximizedProperty);
                }

                public void componentMoved(ComponentEvent e) {
                    setPropertiesFromWindow(
                      containingWnd,
                      properties,
                      widthProperty,
                      heightProperty,
                      xProperty,
                      yProperty,
                      maximizedProperty);
                }
            });
        }
        final Dimension endSize = containingWnd.getSize();
        final Point endLocation = containingWnd.getLocation();
        adjustToAvailableScreens(
          containingWnd,
          PropertiesBasics.getProperty(properties, maximizedProperty, false));
        return!startSize.equals(endSize) ||
          !startLocation.equals(endLocation);
    }

    public static void setPropertiesFromWindow(
      final Window containingWnd,
      final Properties properties,
      final String prefix
      ) {
        setPropertiesFromWindow(
          containingWnd,
          properties,
          prefix + "W",
          prefix + "H",
          prefix + "X",
          prefix + "Y",
          prefix+"M");
    }

    public static void setPropertiesFromWindow(
      final Window containingWnd,
      final Properties properties,
      final String widthProperty,
      final String heightProperty,
      final String xProperty,
      final String yProperty,
      final String maximizedProperty
      ) {
        if (containingWnd instanceof Frame) {
            if (((Frame) containingWnd).getExtendedState() == Frame.MAXIMIZED_BOTH) {
                properties.setProperty(maximizedProperty, "true");
                // no need to set dimensions
                return;
            }
        }
        properties.setProperty(maximizedProperty, "false");

        final Point p = containingWnd.getLocation();
        final Rectangle r=getScreen(containingWnd);
        if (p.x>=r.x){
            properties.setProperty(xProperty, "" + p.x);
        }
        if (p.y >= r.y){
            properties.setProperty(yProperty, "" + p.y);
        }

        final Dimension d = containingWnd.getSize();
        if (widthProperty != null && heightProperty != null) {
            properties.setProperty(widthProperty, "" + d.width);
            properties.setProperty(heightProperty, "" + d.height);
        }
    }
/**
 * 
 * @param toBePackedAndPersonalized
 * @param propertyPrefix
 * @param sizeMatters
 * @return TRUE if the user HAS personalized this window, 
 * 		FALSE if the user has NOT personalized
 */
    public static boolean packAndPersonalize(
      final Window toBePackedAndPersonalized,
      final String propertyPrefix,
      final boolean sizeMatters) {
        return packAndPersonalize(
          toBePackedAndPersonalized,
          null,
          PopupBasics.PROPERTY_SAVIOR,
          propertyPrefix,
          true,
          sizeMatters,
          false);

    }

    public static boolean packAndPersonalize(
      final Window toBePackedAndPersonalized,
      final String propertyPrefix) {
        return packAndPersonalize(
          toBePackedAndPersonalized,
          null,
          PopupBasics.PROPERTY_SAVIOR,
          propertyPrefix,
          true,
          false,
          false);
    }

    public static void setDefaultPropertiesFromWindow(final Window w,
      final String propertyPrefix) {
        final Properties properties = PopupBasics.getProperties(null);
        setPropertiesFromWindow(w, properties, propertyPrefix);
        PopupBasics.PROPERTY_SAVIOR.save(properties);
    }

    public static boolean packAndPersonalize(
      final Window toBePackedAndPersonalized,
      final Properties _properties,
      final PropertiesBasics.Savior propertySavior,
      final String propertyPrefix,
      final boolean centerFirst,
      final boolean sizeMatters,
      final boolean personalizeWhenDeactivating) {
        return packAndPersonalize(
          toBePackedAndPersonalized,
          _properties,
          propertySavior,
          propertyPrefix,
          centerFirst,
          sizeMatters,
          personalizeWhenDeactivating,
          true,
          null);
    }

    public interface Personalizer {
        public void personalize();

        public void detachFromWindow();
    }


    private static Collection<JFrame>frames=
      new HashSet<JFrame>();
    private static MapOfMany offTheTop=new MapOfMany ();
    private static boolean isSomethingOnTop(){
        for (final JFrame frame:frames){
            if (frame.isAlwaysOnTop()){
                return true;
            }
        }
        return false;
    }

    public static void windowActivated(final Window w){
        if (w instanceof java.awt.Dialog) {
            final java.awt.Dialog dlg = (java.awt.Dialog) w;
            if (dlg.isModal()) {
                if (isSomethingOnTop()) {
                    takeOffTop(dlg);
                }
            }
        }
    }
    static boolean needToRefocusDlg=false;
    private  static void takeOffTop(final java.awt.Dialog dlg){
        for (final JFrame frame:frames){
             if (frame.isAlwaysOnTop()){
                offTheTop.put(dlg,frame);
                frame.setAlwaysOnTop(false);
                frame.toBack();
            }
        }
        if (needToRefocusDlg) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dlg.requestFocus();
					dlg.toFront();
				}
			});
		}
    }

    private static void returnToTop(final java.awt.Dialog dlg){
        for (final JFrame frame : (Collection<JFrame>)offTheTop.getCollection(dlg)) {
            frame.setAlwaysOnTop(true);
        }
        offTheTop.removeAll(dlg);
    }


    public static void windowDeactivated(final Window w){
        if (w instanceof java.awt.Dialog) {
            final java.awt.Dialog dlg = (java.awt.Dialog) w;
            if (dlg.isModal()) {
                returnToTop(dlg);
            }
        }
    }

    public static boolean packAndPersonalize(
          final Window toBePackedAndPersonalized,
          final Properties _properties,
          final PropertiesBasics.Savior _propertySavior,
          final String propertyPrefix,
          final boolean centerFirst,
          final boolean sizeMatters,
          final boolean personalizeWhenDeactivating,
          final boolean disposeOnClose,
          final Collection listeners
      ){
         toBePackedAndPersonalized.pack();
        return personalize(
      toBePackedAndPersonalized,
      _properties,
      _propertySavior,
      propertyPrefix,
      centerFirst,
      sizeMatters,
      personalizeWhenDeactivating,
      disposeOnClose,
      listeners);
    }

    public static boolean personalize(final Window wnd, final String prefix, 
    		final boolean centerFirst){
    	final boolean ok=personalize(wnd, prefix, centerFirst, true);
    	System.out.println(prefix+" personalized ?  "+ok+"!!!");
    	return ok;
    }
    public static boolean personalize(final Window wnd, final String prefix, 
    		final boolean centerFirst, final boolean sizeMatters){
    	final boolean disposeOnClose;
    	if (wnd instanceof Dialog){
    		disposeOnClose=( (Dialog)wnd).getDefaultCloseOperation()!=JDialog.DO_NOTHING_ON_CLOSE;
    	}else if (wnd instanceof JFrame){
    		disposeOnClose=( (JFrame)wnd).getDefaultCloseOperation()!=JFrame.DO_NOTHING_ON_CLOSE;
    	} else {
    		disposeOnClose=false;
    	}
    	final boolean ok= personalize(
    			wnd, 
    			null,
    			PopupBasics.PROPERTY_SAVIOR,
    			prefix,
    			centerFirst,
    			sizeMatters,
    			true,
    			disposeOnClose,
    			null);
    	return ok;
    }
    
    public static boolean personalize(
      final Window toBePackedAndPersonalized,
      final Properties _properties,
      final PropertiesBasics.Savior _propertySavior,
      final String propertyPrefix,
      final boolean centerFirst,
      final boolean sizeMatters,
      final boolean personalizeWhenDeactivating,
      final boolean disposeOnClose,
      final Collection listeners) {
        boolean personalized = false;
        final PropertiesBasics.Savior propertySavior =
          _propertySavior == null ? PopupBasics.PROPERTY_SAVIOR :
          _propertySavior;
        final Properties properties = PopupBasics.getProperties(_properties);
        if (centerFirst) {
            center(toBePackedAndPersonalized);
        }
        if (properties != null) {
            personalized = setWindowFromProperties(
              toBePackedAndPersonalized,
              properties,
              propertyPrefix,
              sizeMatters);
            resizeIfBiggerThanScreen(toBePackedAndPersonalized);
            if (!sizeMatters) {
                if (toBePackedAndPersonalized instanceof Frame) {
                    ((Frame) toBePackedAndPersonalized).setResizable(false);
                    
                } else
                if (toBePackedAndPersonalized instanceof java.awt.Dialog) {
                    ((java.awt.Dialog) toBePackedAndPersonalized).setResizable(false);
                    
                }
            }
            if (!personalized && centerFirst ){
            	final Rectangle sc=getScreen(toBePackedAndPersonalized);
            	final Dimension screenSize =  new Dimension(sc.width, sc.height);
            	screenSize.width -= BORDER_SIZE;
            	screenSize.height -= BORDER_SIZE;
            	center(toBePackedAndPersonalized, sc.x, sc.y, screenSize);    		
        	}
            final Rectangle r = toBePackedAndPersonalized.getBounds();
            class WW extends WindowAdapter implements Personalizer {
                public void detachFromWindow() {
                    toBePackedAndPersonalized.removeWindowListener(this);
                }

                public void personalize() {
                	final Rectangle b=toBePackedAndPersonalized.getBounds();
                	final boolean save=!r.equals(b);
                	System.out.println("save="+save+", r="+r.toString()+", b="+b.toString()+"..."+PopupBasics.getPropertyFileName(properties) +", ps="+propertySavior);
                    if (save) {
                        setPropertiesFromWindow(toBePackedAndPersonalized,
                                                properties,
                                                propertyPrefix);
                        if (propertySavior != null) {
                        	propertySavior.save(properties);
                        }
                    }

                }

                public void windowDeactivated(final WindowEvent we) {
                	System.out.println("windowDeactivated "+personalizeWhenDeactivating+", "+disposeOnClose);
                    if (personalizeWhenDeactivating) {
                        personalize();
                        SwingBasics.windowDeactivated(toBePackedAndPersonalized);
                    }

                }

                public void windowClosing(final WindowEvent we) {
                	System.out.println("windowClosing "+personalizeWhenDeactivating+", "+disposeOnClose);
                    if (!personalizeWhenDeactivating) {
                        personalize();
                        SwingBasics.windowDeactivated(toBePackedAndPersonalized);
                    }
                    if (disposeOnClose) {
                        toBePackedAndPersonalized.dispose();
                    }
                    final Window w=we.getWindow();
                    if ( w instanceof JFrame) {
                        frames.remove(w);
                    }
                }
            }


            final WW ww = new WW();
            toBePackedAndPersonalized.addWindowListener(ww);
            if (listeners != null) {
                listeners.add(ww);
            }
            SwingBasics.windowActivated(toBePackedAndPersonalized);
            if (toBePackedAndPersonalized instanceof JFrame) {
                frames.add((JFrame) toBePackedAndPersonalized);
            }
        } else if (centerFirst){
        	adjustToAvailableScreens(toBePackedAndPersonalized, false);
        	final Rectangle sc=getScreen(toBePackedAndPersonalized);
        	final Dimension screenSize =  new Dimension(sc.width, sc.height);
        	screenSize.width -= BORDER_SIZE;
        	screenSize.height -= BORDER_SIZE;
        	center(toBePackedAndPersonalized, sc.x, sc.y, screenSize); 
        }
        return personalized;
    }

    public static void throwIllegalStateException(final String msg) {
        PopupBasics.alert(msg);
        throw new IllegalStateException(msg);
    }

    public static void addAllChildIfNoneInBucket(
      final DefaultTreeModel model,
      final Collection parentTreePaths,
      final Collection bucket) {
        for (final Iterator it = parentTreePaths.iterator(); it.hasNext(); ) {
            final TreeNode node = (TreeNode) ((TreePath) it.next()).
                                  getLastPathComponent();
            final String debug = node.toString();
            addAllChildIfNoneInBucket(model, node, bucket);
        }
    }

    public static void addAllChildIfNoneInBucket(
      final DefaultTreeModel model,
      final TreeNode parent,
      final Collection bucket) {
        final int n = parent.getChildCount();
        boolean hasChildrenInBucket = false;
        for (int i = 0; !hasChildrenInBucket && i < n; i++) {
            final TreeNode child = (TreeNode) parent.getChildAt(i);
            final TreePath childPath = new TreePath(model.getPathToRoot(child));
            hasChildrenInBucket = bucket.contains(childPath);
        }
        if (!hasChildrenInBucket) {
            for (int i = 0; i < n; i++) {
                final TreeNode child = (TreeNode) parent.getChildAt(i);
                final String debug = child.toString();
                addAllChildIfNoneInBucket(model, child, bucket);
                final TreePath p = new TreePath(model.getPathToRoot(child));
                bucket.add(p);
            }
        }
    }

    public static void removeIfAnyChildNotInBucket(
      final DefaultTreeModel model,
      final Collection removalCandidates,
      final Collection bucket) {
        for (final Iterator it = removalCandidates.iterator(); it.hasNext(); ) { // hit the root?
            final TreePath parentPath = (TreePath) it.next();
            final TreeNode parentNode = (TreeNode) parentPath.
                                        getLastPathComponent();
            final int n = parentNode.getChildCount();
            for (int i = 0; i < n; i++) {
                final TreeNode childNode = (TreeNode) parentNode.getChildAt(i);
                final TreePath childPath = new TreePath(model.getPathToRoot(
                  childNode));
                if (!bucket.contains(childPath)) {
                    removeParent(childPath, bucket);
                    break;
                }
            }
        }
    }

    public static Object []getPath(final TreePath tp){
        final Object []o=tp.getPath();
        return o;
    }

    public static String []getNodeNamesAfterRoot(final TreePath tp){
        final Object []o=tp.getPath();
        final String []a=new String[o.length-1];
        for (int i=1;i<o.length;i++){
        	a[i-1]=o[i].toString();
        }
        return a;
    }

    public static void addParentIfAllChildrenInBucket(
      final DefaultTreeModel model,
      final Collection candidates,
      final Collection bucket) {
        for (final Iterator it = candidates.iterator(); it.hasNext(); ) { // hit the root?
            final TreePath parentPath = ((TreePath) it.next()).getParentPath();
            if (parentPath != null) {
                final TreeNode parentNode = (TreeNode) parentPath.
                                            getLastPathComponent();
                final int n = parentNode.getChildCount();
                boolean allInBucket = true;
                for (int i = 0; i < n; i++) {
                    final TreeNode childNode = (TreeNode) parentNode.getChildAt(
                      i);
                    final TreePath childPath = new TreePath(model.getPathToRoot(
                      childNode));
                    if (!bucket.contains(childPath)) {
                        allInBucket = false;
                        break;
                    }
                }
                if (allInBucket) {
                    bucket.add(parentPath);
                }
            }

        }
    }

    public static Collection getNodesWithSomeKidsInBucket(
      final DefaultTreeModel model,
      final Collection selectedTreePaths,
      final Collection bucket) {
        final Collection returnValue = new ArrayList(), done = new HashSet();
        for (final Iterator it = selectedTreePaths.iterator(); it.hasNext(); ) { // hit the root?
            final TreePath parentPath = ((TreePath) it.next()).getParentPath();
            if (parentPath == null) {
                continue;
            }
            if (!done.contains(parentPath)) {
                done.add(parentPath);
                final TreeNode parentNode = (TreeNode) parentPath.
                                            getLastPathComponent();
                final int n = parentNode.getChildCount();
                int inBucket = 0;
                for (int i = 0; i < n; i++) {
                    final TreeNode childNode = (TreeNode) parentNode.getChildAt(
                      i);
                    final TreePath childPath = new TreePath(model.getPathToRoot(
                      childNode));
                    if (bucket.contains(childPath)) {
                        inBucket++;
                    }
                }
                if (inBucket > 0 && inBucket < n) {
                    returnValue.add(parentNode);
                }
            }
        }
        return returnValue;
    }

    public static void removeAllChildren(
      final DefaultTreeModel model,
      final Collection parentTreePaths,
      final Collection bucket) {
        for (final Iterator it = parentTreePaths.iterator(); it.hasNext(); ) {
            removeAllChildren(model,
                              (TreeNode) ((TreePath) it.next()).
                              getLastPathComponent(),
                              bucket);
        }
    }

    public static void removeAllChildren(
      final DefaultTreeModel model,
      final TreeNode parent,
      final Collection bucket) {
        final int n = parent.getChildCount();
        for (int i = 0; i < n; i++) {
            final TreeNode child = (TreeNode) parent.getChildAt(i);
            removeAllChildren(model, child, bucket);
            final TreePath p = new TreePath(model.getPathToRoot(child));
            bucket.remove(p);
        }
    }

    public static void removeParent(
      final Collection toBeOrphaned,
      final Collection bucket) {
        for (final Iterator it = toBeOrphaned.iterator(); it.hasNext(); ) {
            removeParent((TreePath) it.next(), bucket);
        }
    }

    public static void removeParent(
      final TreePath toBeOrphaned,
      final Collection container) {
        for (TreePath path = toBeOrphaned.getParentPath(); path != null;
                             path = path.getParentPath()) {
            container.remove(path);
        }
    }

    public static void removeOrphans(
      final Collection parentTreePaths,
      final Collection childTreePaths) {
        for (final Iterator childIt = childTreePaths.iterator();
                                      childIt.hasNext(); ) {
            final TreePath childPath = (TreePath) childIt.next();
            for (final Iterator parentIt = parentTreePaths.iterator();
                                           parentIt.hasNext(); ) {
                final TreePath parentPath = (TreePath) parentIt.next();
                if (parentPath.isDescendant(childPath)) {
                    childIt.remove();
                }
            }
        }
    }

    /**
     * Returning true signifies a mouse event on the node should select
     * from the anchor point.
     */
    public static boolean isMultiSelectionEvent(final MouseEvent event) {
        return (SwingUtilities.isLeftMouseButton(event)
                && event.isShiftDown());
    }

    public static boolean isToggleSelectionEvent(final MouseEvent event) {
        return (SwingUtilities.isLeftMouseButton(event)
                && event.isControlDown());
    }

    public static JPanel getPanelWith(final String heading, final JTable table) {
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new JLabel(heading), BorderLayout.NORTH);
        jp.add(table, BorderLayout.CENTER);
        return jp;
    }

    public static JDialog getDialogWith(
      final String heading,
      final JTable table,
      final boolean modal) {
        final JDialog dlg = getDialog(table);
        dlg.setTitle(heading);
        dlg.setModal(modal);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        dlg.pack();
        return dlg;
    }

    public static JDialog getDialog(
      final String heading,
      final JTable table,
      final boolean modal) {
        final JDialog dlg = getDialog(table);
        dlg.setTitle(heading);
        dlg.setModal(modal);
        return dlg;
    }

    public static int getModelIndexFromVisualIndex(final JTable table,
      final int visualIndex) {
        return table.getColumnModel().getColumn(visualIndex).getModelIndex();
    }

    public static int[] getVisualIndexes(final JTable table) {
        int[] c = new int[table.getColumnCount()];
        for (int i = 0; i < c.length; i++) {
            c[i] = i;
        }
        return c;
    }

    public static int getVisualIndexFromModelIndex(final JTable table,
      final int modelIndex) {
        final TableColumnModel tcm = table.getColumnModel();
        final int n = tcm.getColumnCount();
        for (int i = 0; i < n; i++) {
            final TableColumn tc = tcm.getColumn(i);
            if (modelIndex == tc.getModelIndex()) {
                return i;
            }
        }
        return -1;
    }

    private static final int BORDER_SIZE = 50;

    public static JComponent getReadOnlyHtml(final String htmlText) {
        JTextPane tp = new JTextPane();
        tp.setContentType("text/html");
        tp.setText(htmlText);
        JScrollPane js = new JScrollPane();
        JViewport jvp = js.getViewport();
        jvp.add(tp);
        return js;
    }

    public static void echoAction(
      final JComponent []cc,
      final JMenuItem menuItem,
      final java.awt.event.ActionListener anAction,
      final KeyStroke keyStroke,
      final char mnemonic) {
        if (menuItem != null) {
            menuItem.addActionListener(anAction);
        }
        if (keyStroke != null) {
            if (menuItem != null) {
                menuItem.setAccelerator(keyStroke);
            }
            for (final JComponent c : cc) {
                c.registerKeyboardAction(anAction, keyStroke,
                                         JComponent.WHEN_FOCUSED);
            }
        }
        if (menuItem != null){
            menuItem.setMnemonic(mnemonic);
        }
    }

    public static void echoAction(
      final JComponent c,
      final AbstractButton menuItem,
      final java.awt.event.ActionListener anAction,
      final KeyStroke keyStroke,
      final char mnemonic) {
    	Action action=null;
    	if (menuItem != null ){
    		menuItem.addActionListener(anAction);
    		menuItem.setMnemonic(mnemonic);
    	}
    	if (keyStroke != null) {
        	if (menuItem instanceof JMenuItem) {
        		( (JMenuItem)menuItem).setAccelerator(keyStroke);
        	}
        	if (c != null){
        		c.registerKeyboardAction(anAction, keyStroke,
                                     JComponent.WHEN_FOCUSED);
        	}
        }        
    }

    public static Icon getIcon(
      final boolean isExpanded,
      final boolean isLeaf,
      final MetaRow metaRow,
      final Iterator selectedRows,
      final int dataColumnIndex) {
        Icon returnValue = null;
        if (metaRow != null) {
            returnValue = metaRow.getIcon(dataColumnIndex);
            if (returnValue == null) {
                returnValue = metaRow.getIcon(selectedRows, dataColumnIndex,
                                              isExpanded,
                                              isLeaf);
            }
        }
        return returnValue;
    }

    public static void setMenuItem(
      final String txt,
      final java.awt.event.ActionListener al,
      final JPopupMenu jpm) {
        final JMenuItem menuItem = new JMenuItem(txt);
        menuItem.addActionListener(al);
        jpm.add(menuItem);
    }

    public static void setMenuItem(
      final String txt,
      final java.awt.event.ActionListener al,
      final JMenu jpm,
      final ImageIcon icon) {
        final JMenuItem menuItem = new JMenuItem(txt);
        menuItem.addActionListener(al);
        jpm.add(menuItem);
    }

    public static void setMenuItem(
      final String txt,
      final java.awt.event.ActionListener al,
      final JMenu jpm,
      final char mnemonic,
      final ImageIcon icon) {
        final JMenuItem menuItem = new JMenuItem(txt, icon);
        menuItem.addActionListener(al);
        menuItem.setMnemonic(mnemonic);
        jpm.add(menuItem);
    }

    public static void setMenuItem(
      final String txt,
      final java.awt.event.ActionListener al,
      final JPopupMenu jpm,
      final Icon icon,
      final char mnemonic) {
        JMenuItem menuItem = new JMenuItem(txt, icon);
        menuItem.addActionListener(al);
        menuItem.setMnemonic(mnemonic);
        jpm.add(menuItem);
    }

    public static Frame getFrame(final Component c) {
        Frame frame = null;
        if (c instanceof Frame) {
            frame = (Frame) c;
        } else {
            final Window w = SwingUtilities.windowForComponent(c);
            if (w instanceof Frame) {
                frame = (Frame) w;
            } else if (w == null){
                frame=mainFrame;
            }
        }
        return frame;
    }

    public static void locateNextTo(final Component neighbor,
                                    final Window toBeLocated) {    	
        final Rectangle screen = getScreen(neighbor);
        screen.width -= BORDER_SIZE;
        screen.height -= BORDER_SIZE;
        final Point point = neighbor.getLocationOnScreen();
        final Dimension nSize = neighbor.getSize(), size = toBeLocated.getSize();
        int x = point.x + nSize.width, y = point.y + nSize.height;

        if (x + size.width > screen.x+screen.width) {
            x = point.x - size.width;
            if (x<screen.x){
            	x=screen.x;
            }
        }
        if (y + size.height > screen.y+screen.height) {
            y = point.y - size.height;
            if (y< screen.y){
            	y=screen.y;
            }
        }
        toBeLocated.setLocation(new Point(x, y));
    }

    public static void bottomLeft(final Component toLocate,
                                       final Component context) {
            bottomLeft(toLocate, context.getY(), context.getSize());
        }

    	public static Dimension getScreenSizeMinusDockingBar(final Component c){
    		final Dimension d = c.getToolkit().getScreenSize();
    		d.height -= (Basics.isEvilEmpireOperatingSystem() ? 48 : 30);
            return d;	
    	}
        public static void bottomLeft(final Component c) {
        	bottomLeft(c, 0, getScreenSizeMinusDockingBar(c));
        }

        public static void bottomLeft(
          final Component c,
          final int y,
          final Dimension size) {
            size.height -= 5;
            final Dimension componentSize = c.getSize();
            int yPos = y + (size.height - componentSize.height);
            yPos = Math.max(yPos, 0);
            c.setLocation(new Point(0, yPos));
        }
        public static void bottomRight(final Component toLocate,
                                       final Component context) {
            bottomRight(toLocate, context.getX(), context.getY(), context.getSize());
        }

        public static void bottomRight(final Component c) {
    		bottomRight(c, 0, 0, getScreenSizeMinusDockingBar(c));
    		if (c instanceof Window){
    			boolean maximized=false;
    			if (c instanceof Frame){
    				maximized=( (Frame)c).getExtendedState()==Frame.MAXIMIZED_BOTH;
    			}
    			adjustToAvailableScreens((Window)c, maximized);

    		}
        }

        public static void bottomRight(final Component c, final int y) {
		bottomRight(c, 0, y, getScreenSizeMinusDockingBar(c));
	}

	public static void bottomRight(final Component c, final int x, final int y, final Dimension size) {
		size.width -= 5;
		size.height -= 5;
		final Dimension componentSize = c.getSize();
		int xPos = x + (size.width - componentSize.width);
		xPos = Math.max(xPos, 0);
		int yPos = y + (size.height - componentSize.height);
		yPos = Math.max(yPos, 0);
		c.setLocation(new Point(xPos, yPos));
	}

    public static void topRight(final Component toLocate,
                                   final Component context) {
        topRight(toLocate, context.getX(), context.getSize());
    }

    public static void topRight(final Component c) {
        topRight(c, 0, getScreenSizeMinusDockingBar(c));
        if (c instanceof Window){
			boolean maximized=false;
			if (c instanceof Frame){
				maximized=( (Frame)c).getExtendedState()==Frame.MAXIMIZED_BOTH;
			}
			adjustToAvailableScreens((Window)c, maximized);

		}

    }

    public static void topRight(
      final Component c,
      final int x,
      final Dimension size) {
        size.width -= 5;
        size.height -= 5;
        final Dimension componentSize = c.getSize();
        int xPos = x + (size.width - componentSize.width);
        xPos = Math.max(xPos, 0);
        c.setLocation(new Point(xPos, 0));
    }

    public static void center(Component needsCentering, Component context) {
        center(needsCentering, context.getX(), context.getY(), context.getSize());
    }

    public static void center(Component needsCentering, JFrame context) {
        center(needsCentering, context.getX(), context.getY(), context.getSize());
    }

    public static void center(Component needsCentering, JDialog context) {
        center(needsCentering, context.getX(), context.getY(), context.getSize());
    }

    public static void center(
      final Component needsCentering,
      final int x,
      final int y,
      final Dimension size) {
        size.width -= BORDER_SIZE;
        size.height -= BORDER_SIZE;
        Dimension componentSize = needsCentering.getSize();
        int xPos = x + (size.width - componentSize.width) / 2;
        xPos = Math.max(xPos, 0);
        int yPos = y + (size.height - componentSize.height) / 2;
        yPos = Math.max(yPos, 0);
        needsCentering.setLocation(new Point(xPos, yPos));
    }

    public static void center(final Component c) {
        final Dimension screenSize = c.getToolkit().getScreenSize();
        screenSize.width -= BORDER_SIZE;
        screenSize.height -= BORDER_SIZE;
        center(c, 0, 0, screenSize);
    }

    public static void switchContaineesWithinContainer(
      final JComponent showingContainee,
      final JComponent hiddenContainee) {
        Container showingContainer = showingContainee.getParent();
        if (showingContainer instanceof JScrollPane) {
            showingContainer = showingContainer.getParent();
        }
        switchContaineesWithinContainer(showingContainer, showingContainee, hiddenContainee);
    }


    /**
     * If expand is true, expands all nodes in the tree.
     * Otherwise, collapses all nodes in the tree
     * @param tree JTree
     * @param expand boolean
     * @param ifMatches String node is treated if toString() equals this or if NULL
     */

    public static void expandOrCollapse(
      final JTree tree,
      final boolean expand,
      final String ifMatches) {
        if (tree != null) {
            final TreeNode root = (TreeNode) tree.getModel().getRoot();

            // Traverse tree from root
            expandOrCollapse(tree, new TreePath(root), expand, ifMatches);
        }
    }

    private static void expandOrCollapse(
      final JTree tree,
      final TreePath parent,
      final boolean expand,
      final String ifMatches) {
        // Traverse children
        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (@SuppressWarnings("unchecked")
			Enumeration<TreeNode> e = (Enumeration<TreeNode>)node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandOrCollapse(tree, path, expand, ifMatches);
            }
        }
        // Expansion or collapse must be done bottom-up
        if ( ifMatches==null){
            if (expand) {
                tree.expandPath(parent);
            } else {
                tree.collapsePath(parent);
            }
        } else if (Basics.equals(ifMatches,node.toString())){
            final TreePath grandParent=parent.getParentPath();
            if (grandParent !=  null){
                if (expand) {
                    tree.expandPath(grandParent);
                } else {
                    tree.collapsePath(grandParent);
                }
            }
        }
    }

    public static void expandToLevel( final JTree tree, final int level) {
        if (tree != null) {
            final TreeNode root = (TreeNode) tree.getModel().getRoot();

            // Traverse tree from root which is level 1
            expandToLevel(tree, new TreePath(root), 0, level);
        }
    }

    public static void expandToLevel( final JTree tree, final TreePath path, final int cur, final int level) {
        if (cur==level){
        	tree.expandPath(path);
        } else {
            final TreeNode node = (TreeNode) path.getLastPathComponent();
            final int n=node.getChildCount();
            for (int i=0;i<n;i++){
            	final TreeNode childNode=node.getChildAt(i);
            	expandToLevel(tree, path.pathByAddingChild(childNode), cur + 1, level);
            }
        }
    }

    public static void expandFistLeaf( final JTree tree) {
            if (tree != null) {
                final TreeNode root = (TreeNode) tree.getModel().getRoot();

                // Traverse tree from root
                expandFirstLeaf(tree, new TreePath(root));
            }
        }

        public static void expandFirstLeaf(
          final JTree tree,
          final TreePath parent) {
            // Traverse children
            final TreeNode node = (TreeNode) parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                final Enumeration e = node.children();
                if (e.hasMoreElements()){
                    final TreeNode n = (TreeNode) e.nextElement();
                    final TreePath path = parent.pathByAddingChild(n);
                    expandFirstLeaf(tree, path);
                }
            }
            tree.expandPath(parent);
        }

    public static DefaultMutableTreeNode getNextSiblingOrAncestralSibling(
      DefaultMutableTreeNode node) {
        // find next sibling node or ancestor
        DefaultMutableTreeNode nextSibling = node.getNextSibling();
        while (nextSibling == null) {
            node = (DefaultMutableTreeNode) node.getParent();
            if (node == null) {
                break;
            }
            nextSibling = node.getNextSibling();
        }
        return nextSibling;
    }

    public static boolean isMac = Basics.isMac();

    public static void switchContaineesWithinContainer(
      final Container container,
      final JComponent showingContainee,
      final JComponent hiddenContainee) {
        if (container != null) {
            container.remove(showingContainee);
            container.add(hiddenContainee);
            showingContainee.setVisible(false);
            hiddenContainee.setVisible(true);
            if (container instanceof JComponent) {
                ((JComponent) container).updateUI();
            } else {
                hiddenContainee.updateUI();
            }
        }
    }

    // only way I could figure how to repaint a header
    public static void repaintHeader(final JTable table) {
        TableModel tableModel = table.getModel();
        JTableHeader head = table.getTableHeader();
        TableColumnModel tcm = head.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            TableColumn tc = tcm.getColumn(i);
            int modelIndex = tc.getModelIndex();
            String label = tableModel.getColumnName(modelIndex);
            tc.setHeaderValue(label);
        }
        head.resizeAndRepaint();
    }

    public static void closeWindow(final Window window) {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
    
    public static void closeWindow(final Window window, final int delay) {
		final Timer timer = new Timer();

		timer.schedule(new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SwingBasics.closeWindow(window);
					}
				});
				timer.cancel();
				// Task here ...
			}
		}, delay);
	}


    // Finds the path in tree as specified by the array of names.
    // The names array is a sequence of names where names[0]
    // is the root and names[i] is a child of names[i-1].
    // Comparison is done using String.equals().
    // Returns null if not found.
    public static ArrayList<TreePath> find(final JTree tree, final String[] names) {
        final TreeNode root = (TreeNode) tree.getModel().getRoot();
        final ArrayList<TreePath> pathList = new ArrayList<TreePath>();
        find2(tree, new TreePath(root), names, 0, true, pathList);
        return pathList;
    }

    private static void find2(
      final JTree tree,
      final TreePath parent,
      final String[] nodeNames,
      final int depth,
      final boolean byName,
      final ArrayList<TreePath> pathList) {
        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        final String o = node.toString().toLowerCase();

        boolean equals = true;
        String arg;
        int n;
        if (nodeNames[depth] != null) {
            arg = nodeNames[depth].toLowerCase();
            n = arg.length();
        } else {
            arg = "";
            n = 0;
            equals = true;
        }
        if (n > 0) {
            if (arg.charAt(n - 1) == '*') {
                if (n < 2) {
                    equals = true;
                } else {
                    arg = arg.substring(0, n - 1);
                    equals = o.startsWith(arg);
                }
            } else {
                equals = o.equals(arg);
            }
        }
        // If equal, go down the branch
        if (equals) {
            // If at end, return match
            if (depth == nodeNames.length - 1) {
                pathList.add(parent);
                return;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (@SuppressWarnings("unchecked")
				final Enumeration<TreeNode> e = (Enumeration<TreeNode>) node.children(); e.hasMoreElements(); ) {
                    final TreeNode nextNode = (TreeNode) e.nextElement();
                    final TreePath path = parent.pathByAddingChild(nextNode);
                    find2(tree, path, nodeNames, depth + 1, byName, pathList);
                }
            }
        }
    }

    public static TreePath[] find(final JTree tree, final String srch) {
        ArrayList<TreePath> found = null;
        if (!Basics.isEmpty(srch)) {
            String[] args = Basics.split(srch, "/");
            found = find(tree, Basics.prepend("*", args));
        }
        return found == null ? new TreePath[0] :
          (TreePath[]) found.toArray(new TreePath[found.size()]);
    }

    public static TreePath[] findInSubtree(final JTree tree,
                                           final String[]
                                           searchArgForEachTreeLevel) {
        final ArrayList<TreePath> found = find(tree,
                                     Basics.prepend("*",
          searchArgForEachTreeLevel));
        return found == null ? new TreePath[0] :
          (TreePath[]) found.toArray(new TreePath[found.size()]);

    }

    public static void setMinimumSize(
      final JComponent component,
      final int minWidth,
      final int minHeight,
      final boolean recenter) {
        Dimension s = component.getPreferredSize();
        boolean resize = false;
        if (s.height < minHeight) {
            s.height = minHeight;
            resize = true;
        }
        if (s.width < minWidth) {
            s.width = minWidth;
            resize = true;
        }
        if (resize) {
            component.setPreferredSize(s);
            final Window window = getWindowOrWindowAncestor(component);
            window.pack();
            if (recenter) {
                center(window);
            }
        }
    }

    public static JDialog getDialog(final Component c) {
		final JDialog value;
		final Window w = getWindowOrWindowAncestor(c);
		if (w instanceof Frame) {
			value = new JDialog((Frame) w);
		} else if (w instanceof java.awt.Dialog) {
			final boolean modal = ((java.awt.Dialog) w).isModal();
			value = new JDialog((java.awt.Dialog) w, modal);
		} else if (w instanceof JFrame) {
			value = new JDialog((JFrame) w);
		} else if (w instanceof JDialog) {
			final boolean modal = ((JDialog) w).isModal();
			value = new JDialog((JDialog) w, modal);
		} else {
			value = new JDialog(mainFrame);
		}
		return value;
	}

    static boolean buttonEqualSizes = true, taskButtonWaterMarks=false, wizardIcons = false,
    popupMenuButton = true, tearAwayButton = true,suckerFishButton=true;
    public static boolean isButtonEqualSizes() {
        return buttonEqualSizes;
    }

    public static boolean isWizardIcons() {
        return wizardIcons;
    }

    public static boolean isPopupMenuButton() {
        return popupMenuButton;
    }

    public static boolean isTearAwayButton() {
        return tearAwayButton;
    }

    public static void setButtonPanelLayout(final JPanel panel) {
        if (buttonEqualSizes) {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setHgap(3);
            gridLayout.setVgap(2);
            panel.setLayout(gridLayout);

        } else {
            panel.setLayout(new FlowLayout());
        }

    }

    // convenience procedures for promoting consistent button L&F
    public static JPanel getButtonPanel(final int cnt) {
        final JPanel buttons = new JPanel();
        if (buttonEqualSizes) {
            final GridLayout gridLayout;
            if (cnt >= 0) {
                gridLayout = new GridLayout(1, cnt, 3, 2);
            } else {
                gridLayout = new GridLayout();
                gridLayout.setHgap(3);
                gridLayout.setVgap(2);
            }
            buttons.setLayout(gridLayout);
        } else {
            //buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        }
        buttons.setBorder(BUTTONS_BORDER);
        return buttons;
    }

    public final static Border BUTTONS_BORDER=BorderFactory.createRaisedBevelBorder();
    static Border BUTTON_BORDER=null;
    public static JPanel newPanel(final Icon icon, final JPanel buttons) {
        return newPanel(icon, buttons, null);
    }

    public static JPanel newPanel(final Icon icon,
                                  final JPanel centerButtons,
                                  final JPanel eastButtons) {
        return newPanel(icon, centerButtons, null, eastButtons);
    }

    public static JPanel newPanel(final Icon icon,
                                  final JPanel centerButtons,
                                  final String centerButtonsLayoutPos,
                                  final JPanel eastButtons) {
        final JLabel iconLabel;
        if (icon != null) {
            iconLabel = new JLabel();
            iconLabel.setIcon(icon);
        } else {
            iconLabel = null;
        }

        return newPanel(iconLabel, centerButtons, centerButtonsLayoutPos,
                        eastButtons, true);
    }

    public static JPanel newPanel(
      final JLabel iconLabel,
      final JPanel westButtons) {
        return newPanel(iconLabel, westButtons, null);
    }

    public static JPanel newPanel(
      final JLabel westLabel,
      final JPanel centerButtons,
      final JPanel eastButtons) {
        //final JPanel topPanel = new JPanel(new BorderLayout());
        return newPanel(westLabel, centerButtons, null, eastButtons, true);
    }

    private static JPanel newPanel(
      final JComponent westLabel,
      final JPanel centerButtons,
      final String centerButtonsLayoutPos,
      final JPanel eastButtons,
      final boolean scrollPaneForButtons) {
        final JPanel centerPanel = new JPanel(new BorderLayout());
//        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 16, 1));
        if (westLabel != null) {
            centerPanel.add(westLabel, BorderLayout.WEST);
        }
        if (centerButtons != null) {
            final JPanel buttonPanel = new JPanel();
            buttonPanel.add(centerButtons);
            if (scrollPaneForButtons) {
                centerPanel.add(new JScrollPane(buttonPanel,
                                                JScrollPane.
                                                VERTICAL_SCROLLBAR_NEVER,
                                                JScrollPane.
                                                HORIZONTAL_SCROLLBAR_AS_NEEDED),
                                centerButtonsLayoutPos == null ?
                                BorderLayout.CENTER :
                                centerButtonsLayoutPos);
            } else {
                centerPanel.add(buttonPanel,
                                centerButtonsLayoutPos == null ?
                                BorderLayout.CENTER :
                                centerButtonsLayoutPos);

            }
        }
        if (eastButtons != null) {
            final JPanel buttonPanel = new JPanel();
            buttonPanel.add(eastButtons);
            centerPanel.add(buttonPanel, BorderLayout.EAST);
        }
        return centerPanel;
    }

    private static boolean normalButtonLayoutForWindows = false;
    public static boolean isNormalButtonLayoutForWindows() {
        return normalButtonLayoutForWindows;
    }

    public static void setNormalButtonLayoutForWindows(final boolean b) {
        normalButtonLayoutForTabs = b;
    }

    private static boolean normalButtonLayoutForTabs = false;
    static {
        initProperties();

    }

    private static boolean isKanjiComputer() {

        String value;
        try {
            value = System.getProperty("user.home") + File.separator +
                    "Desktop";
            return!new File(value).exists();
        } catch (final SecurityException e) {
            value = null;
        }
        return true;
    }

    public static void resetDefaultFonts(final Font fontFace) {
        if (!isKanjiComputer() && fontFace != null) {
            final LookAndFeel laf = UIManager.getLookAndFeel();
            final UIDefaults def = laf.getDefaults();
            final Collection c = new ArrayList(def.keySet());
            for (final Iterator it = c.iterator(); it.hasNext(); ) {
                final Object key = it.next();
                final Font f = def.getFont(key);

                if (f != null) {
                    UIManager.put(key,fontFace);
                } //if
            } //while
        }
    } //method

    private static boolean favoriteFontSet = false;
    public static void initProperties() {
        if (!favoriteFontSet) {
        	favoriteFontSet = true;
            try {
                resetDefaultFonts(FONT_FACE_FAVORITE);
            } catch (java.lang.ArrayIndexOutOfBoundsException ae){
            	System.err.println(ae.getMessage());
            }
            favoriteFontSet = true;

        }
        setProperties(PopupBasics.getProperties(null));
    }

    public static void setProperties(final Properties pr) {
        normalButtonLayoutForTabs = PropertiesBasics.getProperty(pr,
          PROPERTY_TAB_BUTTON_LAYOUT, false);
        normalButtonLayoutForWindows = PropertiesBasics.getProperty(pr,
          PROPERTY_WINDOW_BUTTON_LAYOUT, true);
        buttonEqualSizes = PropertiesBasics.getProperty(pr,
          PROPERTY_BUTTON_EQUAL_SIZES, true);
        taskButtonWaterMarks=false;/*PropertiesBasics.getProperty(pr,
          PROPERTY_TASK_BUTTON_WATER_MARKS, false);*/
        wizardIcons = PropertiesBasics.getProperty(pr, PROPERTY_WIZARD_ICONS, false);
        buttonMargin = PropertiesBasics.getProperty(pr,
          PROPERTY_BUTTON_MARGIN_SIZE,
          2);
        popupMenuButton = PropertiesBasics.getProperty(pr,
          PROPERTY_POPUP_MENU_BUTTON, true);
        tearAwayButton = PropertiesBasics.getProperty(pr,
          PROPERTY_TEAR_AWAY_BUTTON, true);
        BUTTON_INSETS = new Insets(buttonMargin, buttonMargin, buttonMargin,
                                   buttonMargin);

    }

    public static boolean isNormalButtonLayoutForTabs() {
        return normalButtonLayoutForTabs;
    }

    public static void setNormalButtonLayoutForTabs(final boolean b) {
        normalButtonLayoutForTabs = b;
    }

    private static void layoutNorthAndSouth(
      final boolean isolateIconIfNoMsg,
      final JPanel mainPanel,
      final JComponent iconLabel,
      final JLabel msgLabel,
      final JPanel operationButtons,
      final JPanel conclusionButtons,
      final boolean scrollPane) {
    	if (mainPanel != null && iconLabel != null && msgLabel==null && operationButtons==null && conclusionButtons==null){
    		mainPanel.add(iconLabel, BorderLayout.SOUTH);
    		return;
    	}

        final JPanel operatorPanel = newPanel(
          iconLabel,
          operationButtons,
          conclusionButtons == null ? BorderLayout.EAST :
          BorderLayout.CENTER,
          conclusionButtons, scrollPane);
        mainPanel.add(operatorPanel, BorderLayout.SOUTH);
        if (msgLabel != null) {
            mainPanel.add(msgLabel, BorderLayout.NORTH);
        }
    }

    private static void layoutNorthOrSouth(
      final String borderLayoutConstraint,
      final JPanel mainPanel,
      final JComponent iconLabel,
      final JLabel msgLabel,
      final JPanel operationButtons,
      final JPanel conclusionButtons,
      final boolean scrollPane
      ) {
    	if (mainPanel != null && iconLabel != null && msgLabel==null && operationButtons==null && conclusionButtons==null){
    		mainPanel.add(iconLabel, borderLayoutConstraint);
    		return;
    	}
        final JPanel operatorPanel =
          newPanel(
            iconLabel,
            operationButtons,
            iconLabel == null ? BorderLayout.WEST :
            BorderLayout.CENTER,
            conclusionButtons, scrollPane);
        if (msgLabel != null) {
            final JPanel jp = new JPanel(new BorderLayout());
            JPanel anotherJPanel = new JPanel(new BorderLayout());
            jp.add(operatorPanel, BorderLayout.NORTH);
            anotherJPanel.add(new JLabel("                "),BorderLayout.CENTER);
            anotherJPanel.add(msgLabel,BorderLayout.SOUTH);
            jp.add(anotherJPanel, BorderLayout.SOUTH);
            mainPanel.add(jp, borderLayoutConstraint);
        } else {
            mainPanel.add(operatorPanel, borderLayoutConstraint);
        }
    }

    public static JComponent layout(
      final boolean isTab,
      final JPanel mainPanel,
      final Icon icon,
      final JLabel msgLabel,
      final JPanel operationButtons,
      final JPanel conclusionButtons) {
        return layout(isTab, mainPanel, new JLabel(icon), msgLabel,
                      operationButtons, conclusionButtons);
    }

    public static JComponent layout(
      final boolean isTab,
      final JPanel mainPanelWithBorderLayout,
      final JComponent iconLabel,
      final JLabel msgLabel,
      final JPanel operationButtons,
      final JPanel conclusionButtons) {
        return layout(isTab, mainPanelWithBorderLayout, iconLabel, msgLabel,
                      operationButtons, conclusionButtons, true);

    }

    public static JComponent layout(
      final boolean isTab,
      final JPanel mainPanelWithBorderLayout,
      final JComponent iconLabel,
      final JLabel msgLabel,
      final JPanel operationButtons,
      final JPanel conclusionButtons,
      final boolean scrollPane) {
        if (!(mainPanelWithBorderLayout.getLayout() instanceof BorderLayout)) {
            throw new IllegalArgumentException(
              "mainPanelWithBorderLayout does not have a border layout");
        }
        if (!isTab) {
            if (!normalButtonLayoutForWindows) {
                layoutNorthOrSouth(BorderLayout.NORTH,
                                   mainPanelWithBorderLayout,
                                   iconLabel, msgLabel,
                                   operationButtons, conclusionButtons,
                                   scrollPane);
            } else {
                layoutNorthAndSouth(true, mainPanelWithBorderLayout, iconLabel,
                                    msgLabel,
                                    operationButtons, conclusionButtons,
                                    scrollPane);
            }
        } else {
            if (!normalButtonLayoutForTabs) {
                layoutNorthOrSouth(BorderLayout.NORTH,
                                   mainPanelWithBorderLayout,
                                   iconLabel, msgLabel,
                                   operationButtons, conclusionButtons,
                                   scrollPane);
            } else {
                layoutNorthAndSouth(false, mainPanelWithBorderLayout, iconLabel,
                                    msgLabel,
                                    operationButtons, conclusionButtons,
                                    scrollPane);
            }
        }
        return iconLabel;
    }

    public static int taskButtonDeafultSize=30;
    public static int buttonMargin = 2;
    static Insets BUTTON_INSETS = new Insets(
      buttonMargin,
      buttonMargin,
      buttonMargin,
      buttonMargin);

    public final static JButton getButtonCopy(final AbstractButton original){
    	final ActionListener[]als=original.getActionListeners();
    	final JButton copy=getButton(original.getText(), original.getIcon(), original.getMnemonic(), null, original.getToolTipText());
    	for(final ActionListener al:als){
    		copy.addActionListener(al);
    	}
    	return copy;
    }
    public final static JButton getNextButton(
      final String toolTip, final ActionListener action) {
        return getButton("Next", MmsIcons.getRightIcon(),
                         KeyEvent.VK_RIGHT, action, toolTip);
    }

    public final static JButton getPrevButton(
      final String toolTip, final ActionListener action) {
        return getButton("Prev", MmsIcons.getLeftIcon(),
                         KeyEvent.VK_LEFT, action, toolTip);
    }

    public static JButton getDoneButton(
      final JFrame frame,
      final String toolTip,
      final PersonalizableTable table) {
        return getDoneButton(frame, toolTip, true);
    }

    public static JButton getDoneButton(
      final JFrame frame,
      final String toolTip,
      final boolean registerEscapeKey) {
    	final JButton button;
        if (registerEscapeKey && frame != null) {
        	 button = getDoneButton(frame, toolTip);
        	 SwingBasics.registerEscapeKeyAction(
        			 frame, frame.getRootPane(), button);
             return button;
        }
        return null;
    }

    public static JButton getDoneButton(final JDialog dialog) {
        return getDoneButton(dialog, null, true);
    }

    public static JButton getDoneButton(final JFrame frame) {
        return getDoneButton(frame, null, true);
    }

    public static JButton getDoneButton(
      final JDialog dialog,
      final String toolTip,
      final boolean registerEscapeKey) {
        if (registerEscapeKey && dialog != null) {
            dialog.getRootPane().registerKeyboardAction(
              getCloseAction(dialog),
              KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
              JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        final JButton b = getDoneButton(dialog, toolTip);       
        if (dialog != null) {
            dialog.getRootPane().setDefaultButton(b);
        }
        return b;
    }

    static final Border blue = new LineBorder(Color.blue, 1, true);

    public final static JButton getDoneButton(
      final Window window,
      final String toolTip) {
        return getDoneButton(getCloseAction(window), toolTip);
    }

    public final static JButton getDoneButton(
      final ActionListener al,
      final String toolTip) {
        final JButton b = getButton(
          TEXT_DONE,
          MmsIcons.getAcceptIcon(),
          'd',
          al,
          toolTip);
        if (isMacLookAndFeel()){
        	b.registerKeyboardAction(al, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        return b;
    }

    /**
     * getWaterMarkButton
     *
     * @param txt String - the text to appear on the button
     * @param icon Icon - the icon that will appear on the button
     * @param mnemonic char - the key which, when combined with the look and
     *   feel's mouseless modifier (usually Alt) will activiate this button
     * @param action ActionListener - what to do when this button is pressed
     * @param toolTip String - the tooltip that will appear when hovering over
     *   this button
     * @param image Image - the image that will appear in place of the standard
     *   button
     * @param drawImage boolean - false if the button should appear initially as a regular
     *   button, true otherwise
     * @return WaterMarkButton
     */
    public final static TaskButton getTaskButton(
      final String txt,
      final Icon icon,
      final char mnemonic,
      final ActionListener action,
      final String toolTip,
      final Image doingImage,
      final Image doneImage) {
        final TaskButton button = new TaskButton(txt, doingImage, doneImage, icon);
        button.setMnemonic(mnemonic);
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }
        stylizeButton(button);
        if (action != null) {
            button.addActionListener(action);
        }
        return button;
    }

    public final static TaskButton getTaskButton(
      final Action action,
      final Image doingImage,
      final Image doneImage) {
        final TaskButton button = new TaskButton(action, doingImage, doneImage);
        stylizeButton(button);
        return button;
    }
    public final static JButton getButton(
          final String txt,
          final Icon icon,
          final char mnemonic,
          final ActionListener action,
          final String toolTip,
          final boolean txtIsToolTipTitle) {
        return getButton(
          txt,
          icon,
          mnemonic,
          action,
          txtIsToolTipTitle?Basics.toHtmlUncentered(Basics.stripBodyHtml(txt),toolTip):toolTip);
    }

    public final static JButton getButton(
      final String txt,
      final Icon icon,
      final char mnemonic,
      final ActionListener action,
      final String toolTip) {
    	final JButton button = new JButton(txt, icon); 
        button.setMnemonic(mnemonic);
        if (toolTip != null && !(Basics.isEmpty(toolTip))) {
            button.setToolTipText(toolTip);
        }
        if (action != null) {
            button.addActionListener(action);
        }
        stylizeButton(button);
        return button;
    }

    public final static JButton getButton(
    		final JButton button,
    	      final String txt,
    	      final Icon icon,
    	      final char mnemonic,
    	      final ActionListener action,
    	      final String toolTip) {
    	        button.setText(txt);
    	        button.setIcon(icon);
    	        button.setMnemonic(mnemonic);
    	        if (toolTip != null) {
    	            button.setToolTipText(toolTip);
    	        }
    	        if (action != null) {
    	            button.addActionListener(action);
    	        }
    	        stylizeButton(button);
    	        return button;
    	    }

    public final static JButton getButton(
      final String txt,
      final Icon icon,
      final int mnemonic,
      final ActionListener action,
      final String toolTip) {
        final JButton button;
        if (icon == null) {
            button = new JButton(txt);
        } else {
            button = new JButton(txt, icon);
        }
        button.setMnemonic(mnemonic);
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }
        button.addActionListener(action);
        stylizeButton(button);
        return button;
    }

    public final static JButton getButton(
    	      final String txt,
    	      final int mnemonic,
    	      final ActionListener action,
    	      final String toolTip) {
        final JButton button = new JButton(txt);
        button.setMnemonic(mnemonic);
        if (toolTip != null) {
            button.setToolTipText(toolTip);
        }
        button.addActionListener(action);        
        stylizeButton(button);
        return button;
    }
    
    
    public final static MouseHoverButton getMouseHoverButton(
  	      final String txt,
  	      final int mnemonic,
  	      final ActionListener action,
  	      final String toolTip) {
      final MouseHoverButton button = new MouseHoverButton(txt);
      button.setMnemonic(mnemonic);
      if (toolTip != null) {
          button.setToolTipText(toolTip);
      }
      button.addActionListener(action);
      stylizeButton(button);
      return button;
  }
  
    
    public final static MouseHoverButton getMouseHoverButton(
    	      final String txt,
    	      final Icon icon,
    	      final char mnemonic,
    	      final ActionListener action,
    	      final String toolTip) {
    	    	final MouseHoverButton button = new MouseHoverButton(txt, icon);
    	        button.setMnemonic(mnemonic);
    	        if (toolTip != null && !(Basics.isEmpty(toolTip))) {
    	            button.setToolTipText(toolTip);
    	        }
    	        if (action != null) {
    	            button.addActionListener(action);
    	        }
    	        stylizeButton(button);
    	        return button;
    	    }
    
    
    public final static JButton getButton(
  	      final ActionListener action) {
      final JButton button = new JButton();
      
      button.addActionListener(action);
      stylizeButton(button);
      return button;
  }

    public final static AbstractButton getActionButton(Action action) {
        return stylizeButton(new JButton(action));
    }

    public static AbstractButton stylizeButton(final AbstractButton button) {
        if (BUTTON_BORDER == null){
            //indicate standard border for those who like toggling it
            BUTTON_BORDER=button.getBorder();/*BorderFactory.createCompoundBorder(
            		button.getBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2));*/
        }
        addMouseOver(button);
        button.setPressedIcon(SwingBasics.pressedIcon);
        return button;
    }

    public final static ActionListener getCloseAction(final Window window) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeWindow(window);
            }
        };
    }

    public static String TEXT_CANCEL =
      "<html><body><center><i>Cancel</i></center></body></html>",
    TEXT_DONE = "Done";

    public static JButton getCancelButton(
      final JFrame dialog,
      final String toolTip,
      final PersonalizableTable table) {

        return getCancelButton(dialog, toolTip, true);
    }

    public static JButton getCancelButton(
      final JFrame frame,
      final String toolTip,
      final boolean registerEscapeKey) {
        final JButton b = getCancelButton(frame, toolTip);
        if (registerEscapeKey && frame != null) {
            registerEscape(frame, b);
        }
        return b;
    }

    public static JButton getCancelButton(
      final JDialog dialog,
      final String toolTip,
      final PersonalizableTable table) {
        return getCancelButton(dialog, toolTip, true);
    }

    public static void registerEscape(final RootPaneContainer rpc, final AbstractButton b){
        rpc.getRootPane().registerKeyboardAction(
          new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                b.doClick(150);
            }
        }

        ,
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
          JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static JButton getCancelButton(
      final JDialog dialog,
      final String toolTip,
      final boolean registerEscapeKey) {

        final JButton b = getCancelButton(dialog, toolTip);
        if (registerEscapeKey && dialog != null) {
            registerEscape(dialog, b);
        }
        return b;
    }

    public final static JButton getCancelButton(
      final JDialog dialog,
      final String toolTip,
      final boolean registerEscapeKey,
      final ActionListener action) {
        final JButton returnValue = getCancelButton(dialog, toolTip,
          registerEscapeKey);
        if (action != null){
        	returnValue.addActionListener(action);
        }
        return returnValue;
    }

    public final static JButton getCancelButton(
      final Window w,
      final String toolTip,
      final boolean registerEscapeKey,
      final ActionListener action) {
        final JButton returnValue;
        if (w instanceof Dialog){
        returnValue= getCancelButton((Dialog)w, toolTip,
          registerEscapeKey);
        } else if (w instanceof JFrame){
        	returnValue= getCancelButton((JFrame)w, toolTip,
        	          registerEscapeKey);
        } else {
        	returnValue= getCancelButton((JDialog)null,toolTip, registerEscapeKey);

        }
        returnValue.addActionListener(action);
        return returnValue;
    }

    public final static JButton getCancelButton(
      final Window window,
      final String toolTip) {
        return getButton(TEXT_CANCEL, MmsIcons.getCancelIcon(), '\0',
                         window == null ? null : getCloseAction(window),
                         toolTip);
    }


    public static void setFontAllMenuElements(
      final MenuElement popup,
      final Font font) {
        final MenuElement[] a = popup.getSubElements();
        for (int i = 0; i < a.length; i++) {
            setFontAllMenuElements(a[i], font);
        }
        popup.getComponent().setFont(font);
    }

    public static class Dialog extends JDialog {

        public Dialog(final JDialog owner, final String title) {
            super(owner, title, true);
        }

        public Dialog(final JFrame owner, final String title) {
            super(owner == null ? mainFrame:owner, title, true);
        }

        public Dialog(final String title) {
            super(mainFrame);
            this.setTitle(title);
            this.setModal(true);
        }

        public boolean accepted = false;
        public JButton ok, cancel;


        public void layout(
          final String prefixForPersonalizationProperty,
          final String msg,
          final JPanel mainPanel,
          final JButton otherButton,
          final String helpUrl,
          final boolean bigFont) {
            final Collection<JButton>c=new ArrayList<JButton>();
            if (otherButton != null){
                c.add(otherButton);
            }
            layout(c, prefixForPersonalizationProperty, msg, mainPanel, true, helpUrl,bigFont);
        }

        public void layout(
          final String prefixForPersonalizationProperty,
          final String msg,
          final JPanel mainPanel,
          final String helpUrl,
          final boolean bigFont) {
            layout(null, prefixForPersonalizationProperty, msg, mainPanel,true, helpUrl,bigFont);
        }

        public void layout(
          final Collection<JButton> otherButton,
          final String prefixForPersonalizationProperty,
          final String msg,
          final JPanel mainPanel,
          final boolean canCancel,
          final String helpUrl,
          final boolean bigFont) {
            accepted = false;
            ok = SwingBasics.getDoneButton(this,null,false);
            SwingBasics.registerEscapeKeyAction(this, mainPanel,ok);
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    accepted = true;
                }
            });
            SwingBasics.setOkButton(ok);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(final WindowEvent e) {
                    Dialog.this.dispose();
                }
            });
            final JPanel buttons = SwingBasics.getButtonPanel(otherButton == null ?
              2 :
              otherButton.size()+2);
            if (otherButton != null) {
                for(JButton b:otherButton){
                    buttons.add(b);
                }
            }
            buttons.add(ok);
            if (canCancel){
                cancel = SwingBasics.getCancelButton(this, null);
                buttons.add(cancel);
            }
            if (helpUrl != null) {
                buttons.add(HelpBasics.getHelpButton(this, helpUrl));
            }
            final JLabel msgLabel = new JLabel(msg, JLabel.CENTER);
            if (bigFont) {
                Font FONT_LABEL = UIManager.getFont("Label.font"),
                                  FONT_MSG = new Font(FONT_LABEL.getName(), Font.BOLD,
                    FONT_LABEL.getSize() + 5);

                msgLabel.setFont(FONT_MSG);
            }
            SwingBasics.layout(
              false,
              mainPanel,
              MmsIcons.getPreferencesIcon(),
              msgLabel,
              (JPanel)null,
              buttons);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            getContentPane().add(mainPanel);
            packAndPersonalize(this, null, PopupBasics.PROPERTY_SAVIOR,
                               prefixForPersonalizationProperty, true, true, false);
        }

    }


    public static void setOkButton(final JButton jb) {
        jb.setText("Ok");
        jb.setMnemonic('o');
        jb.setIcon(MmsIcons.getYesIcon());
        jb.setMargin(BUTTON_INSETS);
    }


    public static void popUpHtml(final Component component, final String title, final String html,
                                 boolean modal) {
        final JTextPane jtp = new JTextPane();
        jtp.setContentType("text/html");
        jtp.setMargin(new java.awt.Insets(3, 5, 3, 3));
        jtp.setText(html);
        jtp.setEditable(false);
        final JDialog dialog = getDialog(component);
        dialog.setTitle(title);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setModal(modal);
        dialog.setTitle(title);
        final JPanel cp = new JPanel(new BorderLayout());
        cp.add(new JScrollPane(jtp), BorderLayout.CENTER);
        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(SwingBasics.getDoneButton(dialog, null, true));
        cp.add(buttonPanel, BorderLayout.SOUTH);
        layout(false, cp, (JLabel)null, (JLabel)null, null, buttonPanel);
        dialog.getContentPane().add(cp);
        packAndPersonalize(dialog, "popupHtml");
        dialog.setVisible(true);
    }

    public static String showHtml(
      final String temporaryFilePrefix,
      final String htmlEncodedContent,
      final boolean useInternalBrowser) {
        String url = null;
        if (htmlEncodedContent != null) {
            url = "file://" +
                  IoBasics.saveTempTextFile(temporaryFilePrefix == null ? "mms" :
                                            temporaryFilePrefix, ".html",
                                            htmlEncodedContent).
                  getAbsolutePath();
            if (!useInternalBrowser) {
                showHtml(url);
            } else {
                Browser.show(url, null);
            }
        }
        return url;
    }

    public static void showHtml(final String _url) {
    	showHtml(_url, false);
    }
    public static void showHtml(final String _url, final boolean useSafariIfMac) {
    	final String url = _url.replaceAll(" ", "%20");
		if (Basics.isMac()) {
			if (useSafariIfMac) {
				try {
                    //ClueTube.showHelpHtml (url);
					MacintoshBasics.openWithSafari(url);
				} catch (final IOException ioe) {
					System.out.println(ioe);
				}
			} else {
				// System.gc();
				MacintoshBasics.go(url);
			}
		} else {
			try {
				final String[] command = IoBasics.getExecCmdArray(url);
				Runtime.getRuntime().exec(command);
			} catch (final IOException e) {
				Pel.log.print(e);
			}
		}
    }
    
    public static void showFolder(final File directoryPath) {
		try {
			IoBasics.mkDirs(directoryPath.getAbsolutePath());
			IoBasics.getExecCmdArray(directoryPath);
		} catch (final IOException e) {
			Pel.log.print(e);
		}
	}
    
    public static void showPDF(final File pdfFile) {
    	if(Basics.isMac()) {
			try {
				final String[] cmd = {"open",pdfFile.getAbsolutePath()};
				System.out.print("Invoking ");
				System.out.println(Basics.toString(cmd));
				Process p = Runtime.getRuntime().exec(cmd);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else {
			try {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + pdfFile);
			} catch (final IOException e) {
				Pel.log.print(e);
			}
		}
    }
    
    public static String alertHtml(
      final boolean error,
      final boolean showBodyInBrowser,
      final boolean center,
      final String heading,
      final String body) {
        final String wholeMessage;
        if (!center) {
            wholeMessage = (error ?
                            Basics.startHtmlErrorUncentered(heading) :
                            Basics.startHtmlUncentered(heading))
                           + body
                           + Basics.endHtmlUncentered();
        } else {
            wholeMessage = (error ?
                            Basics.startHtmlError(heading) :
                            Basics.startHtml(heading))
                           + body
                           + Basics.endHtml();

        }

        final String msg;
        if (!showBodyInBrowser) {
            msg = wholeMessage.replace('\n', ' ');
        } else {
            msg = (error ? Basics.startHtmlError(heading) :
                   Basics.startHtml(heading))
                  + "<br>Click OK to browse details"
                  + Basics.endHtml();
        }
        PopupBasics.alert(msg, error);
        if (showBodyInBrowser) {
            showHtml(null, wholeMessage, false);
        }

        return wholeMessage;
    }

    public static void alert(final Throwable t) {
        Pel.log.warn(t);
        alertHtml(true,
                  false,
                  true,
                  "Unexpected exception:  " + t.getClass().getName(),
                  t.getMessage());
    }

    public static String alertHtml(final Throwable t) {
        final String heading = "Exception occurred:";
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        final String body = "<b><i>" +
                            t.getMessage() + "</i></b>"; //"<code>"+encodeHtml( sw.toString())+"</code>";
        return alertHtml(true, false, true, heading, body);
    }

    public static String alertHtml(
      final boolean error,
      final String heading,
      final boolean useNumberedList,
      final boolean encodeHtmlForEachListItem,
      final Collection c) {

        return alertHtml(
          error,
          c.size() > 10,
          false,
          heading,
          Basics.toUlHtml(useNumberedList ? "OL" : "UL", c,
                          encodeHtmlForEachListItem));
    }

    /**
     * Some components have keys mapped to actions.
     * For example, a JTable consumes the ENTER key press and moves the cell selection down.
     * The key events never reach the default button of a dialog.
     * Use this class to override the mapped action and forward the event to the specified component's parent.
     */
    public static void ignoreKeyEvent(
      final JComponent component,
      final int keyCode) {
    	
	        final InputMap inputMap = component.getInputMap();
	        final ActionMap actionMap = component.getActionMap();
	        final Object actionKey = new Object();
	        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionKey);
	        actionMap.put(actionKey, new IgnoreKeyAction(component, keyCode));
    }

    public interface UnaryFunction {

        boolean apply(Object o);
    }


    public static boolean apply(
      final Component component,
      final UnaryFunction f,
      final boolean stopWhenDone) {
        boolean done = f.apply(component);
        if (!done || !stopWhenDone) {
            done = applyToDescendents(component, f, stopWhenDone);
        }
        return done;
    }

    public static boolean applyToDescendents(
      final Component component,
      final UnaryFunction f,
      final boolean stopWhenDone) {
        if (component instanceof Container) {
            Container container = (Container) component;
            int count = container.getComponentCount();
            for (int i = 0; i < count; ++i) {
                Component subComponent = container.getComponent(i);
                final boolean done = apply(subComponent, f, stopWhenDone);
                if (done && stopWhenDone) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class IgnoreKeyAction extends AbstractAction {
        final private JComponent component_;
        final private int keyCode_;

        public IgnoreKeyAction(final JComponent component, final int keyCode) {
            component_ = component;
            keyCode_ = keyCode;
        }

        public void actionPerformed(final ActionEvent e) {
                final JRootPane rp = component_.getRootPane();
                if (rp != null) {
                    if (keyCode_ == KeyEvent.VK_ENTER) {
                        final JButton b = rp.getDefaultButton();
                        if (b != null) {
                            b.doClick();
                        }

                        return;
                    }
                    if (keyCode_ == KeyEvent.VK_ESCAPE) {
                        UnaryFunction click = new UnaryFunction() {
                            public boolean apply(Object o) {
                                if (o instanceof JButton) {
                                    final String s = ((JButton) o).getText();
                                    if (Basics.equals(s, TEXT_CANCEL)) {
                                        ((JButton) o).doClick();
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };
                        apply(rp, click, true);
                        return;
                    }
                }
            
            component_.transferFocus();
            component_.getParent().dispatchEvent(new KeyEvent(component_,
              KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
              keyCode_));

        }
    }

    /**
     * Ignore the given key stroke
     * For e.g., Use this to avoid triggering the default button's action for a dialog,  
     * when the enter key is pressed on any button 
     */
    public static void voidKeyEvent(
    		final JComponent component, final int condition, int keyCode) {    	
    	InputMap im  = component.getInputMap(condition);
		KeyStroke enter = KeyStroke.getKeyStroke(keyCode, 0);
		im.put(enter, "none");
    }

    public static int getScreenCount() {
        final GraphicsDevice[] gs = GraphicsEnvironment.
                                    getLocalGraphicsEnvironment().
                                    getScreenDevices();
        int count = 0;
        for (int i = 0; i < gs.length; i++) {
            final GraphicsConfiguration gc = gs[i].getDefaultConfiguration();
            final Rectangle b = gc.getBounds();
            if (b != null) {
                count++;
            }

        }
        return count;
    }

    public static void setScreenRec(final int screenNumber) {
    	if (screenNumber<0){
    		mainRec=null;
    	}else{
    		final GraphicsDevice[] gs = GraphicsEnvironment.
    				getLocalGraphicsEnvironment().
    				getScreenDevices();
    		Rectangle b=null;
    		System.out.println(gs.length+" screen devices");
			for (int i = 0; i < gs.length; i++) {
				GraphicsConfiguration gc = gs[i].getDefaultConfiguration();
				b = gc.getBounds();
				System.out.println(i+":  x="+b.x+", y="+b.y+", width="+b.width+", height="+b.height);
			}
    		if (screenNumber < gs.length){
    			GraphicsConfiguration gc = gs[screenNumber].getDefaultConfiguration();
    			b = gc.getBounds();
    		}
    		if (b == null) {
    			for (int i = 0; i < gs.length; i++) {
    				GraphicsConfiguration gc = gs[i].getDefaultConfiguration();
    				b = gc.getBounds();
    				if (b != null) {
    					break;
    				}
    			}
    		}
    		mainRec=b;
    	}
    }


    private static int getPortion(final Rectangle w, final Rectangle s){
    	final int width=getPortion(w.x, w.width, s.x, s.width);
    	final int height=getPortion(w.y, w.height, s.y, s.height);
    	return width*height;
    }
    
    private static int getPortion(final int leftStart, final int leftSize, final int rightStart, final int rightSize){
    	int portion=0;
    	int leftSpan=leftStart+leftSize, rightSpan=rightStart+rightSize;
		
    	if (leftStart<rightStart){
    		if (leftSpan>rightStart){
    			if (rightSpan>leftSpan){ // window's right side is in screen
    				portion=leftSpan-rightStart;
    			} else {
    				portion=rightSize;
    			}
    		}
    	} else {
    		if (leftStart < rightSpan){
    			if (leftSpan<rightSpan){
    				portion=leftSize;
    			} else {
    				portion=rightSpan-leftStart;
    			}
    		}
    	}
    	return portion;
    }

    
    private static boolean pointIsContained(
      final Point point,
      final Rectangle container) {
        return
          point.x >= container.x - 8 && // maximized subtracts 4?
          point.x <= (container.x - 8) + container.width &&
          point.y >= container.y - 8 && // maximized subtracts 4?
          point.y <= (container.y - 8) + container.height;
    }

    public static Rectangle getScreen(final Component c) {
    	final Window w=SwingUtilities.getWindowAncestor(c);
    	final Rectangle value;
    	if (w == null){
    		final Dimension d=c.getToolkit().getScreenSize();
    		value=new Rectangle(0,0,d.width,d.height);
    	} else {
    		value=getScreen(w);
    	}
    	return value;
    }
    
    public static Rectangle getScreen(final Window window) {
        final GraphicsEnvironment ge = GraphicsEnvironment.
                                       getLocalGraphicsEnvironment();
        final GraphicsDevice[] physicalScreens = ge.getScreenDevices();
        final TreeMap<Integer, Rectangle>m=new TreeMap<Integer, Rectangle>();
        if (physicalScreens.length > 1) {
            final Point topLeftCorner = window.getLocation();
            final Rectangle b=window.getBounds();
            
            for (int i = 0; i < physicalScreens.length; i++) {
                final GraphicsConfiguration gc = physicalScreens[i].
                                                 getDefaultConfiguration();
                final Rectangle physicalScreen = gc.getBounds();
                if (physicalScreen != null){
                	final int portion=getPortion(b, physicalScreen);
                    m.put(portion, physicalScreen);                    
                }
            }
        }
        if (m.size()>0){
        	final int key=m.lastKey();
        	final Rectangle ret=m.get(key);
        	return ret;
        }
        final Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle(0, 0, d.width, d.height);
    }

    public static void addKeepOnSameScreen(
      final JMenu m,
      final Window mainWindow,
      final Properties _properties,
      final PropertiesBasics.Savior propertySavior) {
        final Properties properties = PopupBasics.getProperties(_properties);
        final String property = SwingBasics.class.getName() +
                                ".keepOnSameScreen";
        final JCheckBoxMenuItem jcb = new JCheckBoxMenuItem(
          "Keep on *SAME* screen", MmsIcons.getWideBlankIcon(), true);
        jcb.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                final boolean b = jcb.getState();
                SwingBasics.keepOnSameScreenAs(b ? mainWindow : null);
                properties.setProperty(property, b ? "true" : "false");
                if (propertySavior != null) {
                    propertySavior.save(properties);
                }
            }
        });
        final boolean b =
          PropertiesBasics.getProperty(properties, property,
                                       SwingBasics.mainWindow != null);
        jcb.setState(b);
        keepOnSameScreenAs(b ? mainWindow : null);
        jcb.setMnemonic('k');
        m.add(jcb);
        m.addMenuListener(
          new MenuListener() {
            public void menuSelected(MenuEvent event) {
                jcb.setVisible(getScreenCount() > 1);
            }

            public void menuCanceled(MenuEvent event) {
            }

            public void menuDeselected(MenuEvent event) {
            }
        }
        );

    }

    public static void keepOnSameScreenAs(
      final Properties _properties,
      final Window mainWindow) {
        final Properties properties = PopupBasics.getProperties(_properties);
        final String property = SwingBasics.class.getName() +
                                ".keepOnSameScreen";

        final boolean b =
          PropertiesBasics.getProperty(properties, property,
                                       SwingBasics.mainWindow != null);
        keepOnSameScreenAs(b ? mainWindow : null);
    }

    public static void keepOnSameScreenAs(final Window mainWindow) {
        SwingBasics.mainWindow = mainWindow;
    }

    public static Rectangle mainRec=null;  
    private static Rectangle keepOnSameScreen(
      final Window wnd,
      final Rectangle screen) {
    	if (mainWindow != null || mainRec != null) {
    		Rectangle screenOfMainWindow=null;
    		if (mainRec != null){
    			screenOfMainWindow=mainRec;    			
        	}else{
                screenOfMainWindow = getScreen(mainWindow);                
    		}
            if (!screenOfMainWindow.equals(screen)) {
                final Point p = wnd.getLocation();
                p.x = screenOfMainWindow.x + (p.x - screen.x);
                p.y = screenOfMainWindow.y + (p.y - screen.y);
                wnd.setLocation(p);
                return screenOfMainWindow;
            }
        }
        return screen;
    }

    public static boolean adjustToAvailableScreens(
      final Window wnd,
      final boolean isMaximized) {
        boolean adjusted = false;
        final Rectangle screen = keepOnSameScreen(wnd, getScreen(wnd));
        Rectangle window = wnd.getBounds();
        if (screen != null) {
            // if starts to the left of the screen
            if (window.x < screen.x) {
                wnd.setLocation(screen.x, window.y);
                adjusted = true;
            }
            // if starts above the top of the screen
            if (window.y < screen.y) {
                adjusted = true;
                wnd.setLocation(wnd.getX(), screen.y);
            }
            if (adjusted) {
                window = wnd.getBounds();
            }
            // if off the screen
            if (window.x + 10 > screen.x + screen.width) {
                adjusted = true;
                wnd.setLocation(screen.x + screen.width - window.width,
                                window.y);

                // if too wide, make thinner
                if (window.getWidth() + 15 > screen.width) {
                    adjusted = true;
                    wnd.setSize(screen.width - 15, window.height);
                    wnd.setLocation(0, window.y);
                }
            }

            if (window.y + 10 > screen.y + screen.height) {
                adjusted = true;
                wnd.setLocation(window.x,
                                screen.y + screen.height - window.height);

                // if too tall, make shorter
                if (window.height + 15 > screen.height) {
                    adjusted = true;
                    wnd.setSize(wnd.getWidth(), screen.width - 15);
                    wnd.setLocation(wnd.getX(), 0);
                }
            }
            final int h=Math.abs(wnd.getHeight()-screen.height),
                        w=Math.abs(wnd.getWidth()-screen.width);
            if ( wnd instanceof Frame && (isMaximized || (h <11 && w < 11))){
                ( (Frame) wnd).setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        }

        return adjusted;

    }

    public static boolean toNorth(final Window w, final Component c) {
        Point pp = c.getLocationOnScreen();
        Dimension d = w.getSize();
        Point p = new Point(pp.x, pp.y - d.height);
        w.setLocation(p);
        if (adjustToAvailableScreens(w, false)) {
            // less than half?
            Dimension dd = c.getSize();
            p = w.getLocation();
            d = w.getSize();
            if (p.y + d.height > pp.x + (dd.height / 2)) {
                return false;
            }
        }
        return true;
    }

    public static JDialog getModalDialog(
      final Component c,
      final Image image,
      final String title) {
        final Window w = getWindowOrWindowAncestor(c);
        if (w instanceof Frame) {
            final Image prev = ((Frame) w).getIconImage();
            if (image != null) {
                ((Frame) w).setIconImage(image);
                final JDialog dlg = new JDialog((Frame) w, title, true);
                dlg.addWindowListener(new WindowAdapter() {
                    public void windowClosing(final WindowEvent e) {
                        ((Frame) w).setIconImage(prev);
                    }
                });
                return dlg;
            }
            return new JDialog((Frame) w, title, true);
        } else if (w instanceof java.awt.Dialog && image != null) {

            return new JDialog((java.awt.Dialog) w, title, true);
        }
        final JFrame dummyFrame;
        if (image != null) {
            dummyFrame = new JFrame();
            dummyFrame.setIconImage(image);
        } else {
            dummyFrame = mainFrame;
        }
        final JDialog dlg = new JDialog(dummyFrame);
        dlg.setModal(true);
        dlg.setTitle(title);
        return dlg;
    }

    public static void closeOnEscape(final RootPaneContainer frame) {
        frame.getRootPane().registerKeyboardAction(
          getCloseAction((Window) frame),
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
          JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    public static void reuseDefaultButtonAction(
      final JComponent jc,
      final Window window,
      final int condition,
      final Input input) {
        final Window w = window != null ? window : getWindowOrWindowAncestor(jc);
        if (w instanceof RootPaneContainer) {
            final JRootPane rp = ((RootPaneContainer) w).getRootPane();
            if (rp != null) {
                final JButton b = rp.getDefaultButton();
                if (b != null) {
                    jc.registerKeyboardAction(
                      new ActionListener() {
                        public void actionPerformed(final ActionEvent e) {
                        	if (input.getCellEditorValue() != null) {
                    				b.doClick(150);                        		
        					}
                        }
                    }
                    , PopupBasics.ENTER_RELEASED,
                      condition);

                }
            }
        }
    }

    public static boolean isPlasticLookAndFeel(){
    	final String cl = UIManager.getLookAndFeel().getClass().getName();
        if (cl.startsWith("com.jgoodies.plaf.plastic.")){
        	return true;
        }
        return false;
    }
    
    public static boolean isPageSoftLookAndFeel() {
        final String cl = UIManager.getLookAndFeel().getClass().getName();
        return cl.equals("com.pagosoft.plaf.PgsLookAndFeel");
    }


	public static boolean isQuaQuaLookAndFeel(final String cl) {
		return cl.contains(quaQuaStr);
	}

	private static String quaQuaStr = "ch.randelshofer.quaqua.";

	public static boolean isQuaQuaLookAndFeel() {
		final String cl = UIManager.getLookAndFeel().getClass().getName();
		return isQuaQuaLookAndFeel(cl);
	}

    public static boolean usesMacMetaKey(){
    	return (isMac && isQuaQuaLookAndFeel()) || isNativeMacLookAndFeel();
    }
	
    private static final String osVersion = System.getProperty("os.version");
	private static int majorVersion;
	private static float minorVersion;
	static {
		try {
			final int idx = osVersion.indexOf('.');

			majorVersion = Integer.valueOf(osVersion.substring(0, idx));
			minorVersion = Float.valueOf(osVersion.substring(idx + 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isQuaQuaCapable() {
		if (isMac) {

			if (majorVersion >= 10 && minorVersion >= 7.0) {
				return false;
			}
		}
		return true;
	}
	
    public static boolean isMacLookAndFeel(){
    	return isQuaQuaLookAndFeel() || isNativeMacLookAndFeel();
    }
    public static boolean isNativeMacLookAndFeel() {
        final String cl = UIManager.getLookAndFeel().getClass().getName();
        return isMac &&
          cl.contains("apple.laf.AquaLookAndFeel");
    }

    public static boolean isNativeWindowsLookAndFeel() {
        final String cl = UIManager.getLookAndFeel().getClass().getName();
        return !isMac &&
          UIManager.getLookAndFeel().getClass().getName().endsWith(
            ".WindowsLookAndFeel");
    }

    public static void report(final JProgressBar bar, final String str,
                              final int value) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    report(bar, str, value);
                }
            });
        } else {
            if (str != null) {
                bar.setString(str);
            }
            if (value >= 0) {
                bar.setValue(value);
            }
        }
    }

    public static Window getWindowOrWindowAncestor(final Component c){
        final Window w;
        if (c instanceof Window){
            w=(Window)c;
        } else {
            if (c==null){
                w=mainFrame;
            } else {
                final Window w2=SwingUtilities.getWindowAncestor(c);
                if (w2 == null) {
                    w = mainFrame;
                } else {
                    w=w2;
                }
            }
        }
        return w;
    }

    public static Image mainWindowIconImage;
    private static Window mainWindow;
    public static JFrame mainFrame;

    public static void initMainWindow(final JFrame mainFrame, final Image mainWindowIconImage){
        SwingBasics.mainFrame=mainFrame;
        SwingBasics.mainWindowIconImage=mainWindowIconImage;
    }


    public static JFrame getFrame(){
        final JFrame frame=new JFrame();
        setParentWindowProperties(frame);
        return frame;
    }
    
    public static void setParentWindowProperties(final JFrame frame) {
    	if (mainWindowIconImage != null){
            frame.setIconImage(mainWindowIconImage);
        }
    }

    public static class ImageButton extends JButton {

        public ImageButton(final String img) {
            this(new ImageIcon(img));
        }

        public ImageButton(final ImageIcon icon) {
            this(icon,null);
        }

        public ImageButton(final ImageIcon icon, final ActionListener al) {
        	setIcon(icon);
           	setSize(icon.getImage().getWidth(null), icon.getImage().getHeight(null));
            if (al!=null){
                addActionListener(al);
            }
        	setToolBarStyle(this);
        }
        
    }
    
    public static class VerticalButtonUI extends BasicButtonUI {
   	 
        protected int angle;
     
        public VerticalButtonUI(int angle) {
            super();
            this.angle = angle;
        }
     
        public Dimension getPreferredSize(JComponent c) {
            Dimension dim = super.getPreferredSize(c);
            return new Dimension( dim.height, dim.width );
        }
     
        private static Rectangle paintIconR = new Rectangle();
        private static Rectangle paintTextR = new Rectangle();
        private static Rectangle paintViewR = new Rectangle();
        private static Insets paintViewInsets = new Insets(0, 0, 0, 0);
     
        public void paint(Graphics g, JComponent c) {
        	AbstractButton button = (AbstractButton)c;
            String text = Basics.stripSimpleHtml(button.getText());
            Icon icon = (button.isEnabled()) ? button.getIcon() : button.getDisabledIcon();
     
            if ((icon == null) && (text == null)) {
                return;
            }
     
            FontMetrics fm = g.getFontMetrics();
            paintViewInsets = c.getInsets(paintViewInsets);
     
            paintViewR.x = paintViewInsets.left;
            paintViewR.y = paintViewInsets.top;
     
            paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
            paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);
     
            paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
            paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
     
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tr = g2.getTransform();
     
            if (angle == 90) {
                g2.rotate( Math.PI / 2 );
                g2.translate( 0, - c.getWidth() );
                paintViewR.y = c.getWidth()/2 - (int)fm.getStringBounds(text, g).getHeight()/2;
            }
            else if (angle == 270) {
                g2.rotate( - Math.PI / 2 );
                g2.translate( - c.getHeight(), 0 );
                paintViewR.y = c.getWidth()/2 - (int)fm.getStringBounds(text, g).getHeight()/2;
            }
     
            if (icon != null) {
                icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
            }
     
            if (text != null) {
                int textX = paintTextR.x;
                int textY = paintTextR.y + fm.getAscent();
     
                if (button.isEnabled()) {
                    paintText(g,c,new Rectangle(paintViewR.x,paintViewR.y,textX,textY),text);
                } else {
                    paintText(g,c,new Rectangle(paintViewR.x,paintViewR.y,textX,textY),text);
                }
            }
     
            g2.setTransform( tr );
        }
    }

    public static AbstractButton setToolBarStyle(final AbstractButton b) {
    	b.setPressedIcon(pressedIcon);
    	b.setMargin(new Insets(0,0,0,0));
    	b.setIconTextGap(0);
    	b.setBorderPainted(false);
    	b.setBorder(null);
    	b.setText(null);
    	b.setOpaque(false);
        b.setBackground(UIManager.getColor("Panel.background"));
        b.setFocusPainted(false);
    	if (Basics.isEvilEmpireOperatingSystem()){
            final boolean isNative=Basics.equals(UIManager.getLookAndFeel().getClass().getName(), UIManager.getSystemLookAndFeelClassName());
            if (isNative){
            	b.setUI(new com.jgoodies.plaf.plastic.PlasticButtonUI());
            }
    	}
        return b;
    }
    public static class PinButton extends JButton implements ActionListener {
        
    	private final JPanel panel;
    	private final Properties properties;
		private final PropertiesBasics.Savior savior;
		private final boolean initialAlwaysOnTop;
    	private boolean initializing;
        private final String propertyName;
        
        public PinButton(
        		final JPanel panel, 
        		final boolean alwaysOnTop,
        		final String propertyName) {
        	this(panel, alwaysOnTop,null,null, propertyName);
        }
        
        public PinButton(
        		final JPanel panel, 
        		final boolean alwaysOnTop, 
        		final Properties properties,
        		final PropertiesBasics.Savior savior, 
        		final String propertyPrefix) {
            super(MmsIcons.getPinIcon());
            setText("<html><font face='verdana'><small>Pin</small></font></html>");
            this.panel = panel;
            addActionListener(this);
            stylizeButton(this);
            this.savior=savior == null?PopupBasics.PROPERTY_SAVIOR:savior;
            this.properties = PopupBasics.getProperties(properties);
            this.propertyName=propertyPrefix+".pinned";
            this.initialAlwaysOnTop=alwaysOnTop;
        }
        private Window window;
        public boolean activate(final Window w) {
        	window=w;
        	boolean onTop=initialAlwaysOnTop;
            if (savior != null && properties!=null) {
				onTop = PropertiesBasics.getProperty(properties, propertyName, initialAlwaysOnTop);				
			} 
            initializing = true;
			setAlwaysOnTopLater(onTop);
			initializing = false;
            if (w != null && onTop) {
            	w.setVisible(true);
            }
            decorate(w);
            return onTop;
        }
        
        private boolean wasOnTop;

        public void unpinIfNecessary(){
            wasOnTop=setAlwaysOnTop(false);
        }

        public void repinIfNecessary(){
            if (wasOnTop){
                setAlwaysOnTop(true);
            }
        }

        public boolean setAlwaysOnTopLater(final boolean yes){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setAlwaysOnTop(yes);
                }
            });

            final Window w = window != null ? window:SwingUtilities.
                             getWindowAncestor(panel);
            if (w==null){
                return false;
            }
            final boolean wasOnTop = w.isAlwaysOnTop();

            return yes != wasOnTop;
        }

        public boolean setAlwaysOnTop(final boolean yes){
            final Window w = window != null ? window:SwingUtilities.
                             getWindowAncestor(panel);
            if (w != null){
                final boolean wasOnTop = w.isAlwaysOnTop();
                if (yes) {
                    if (!wasOnTop) {
                        doClick();
                        return true;
                    }
                } else {
                    if (wasOnTop) {
                        doClick();
                        return true;
                    }
                }
            }
            return false;
        }



        public void actionPerformed(final ActionEvent e) {
            final Window w = window != null ? window:SwingUtilities.
                             getWindowAncestor(panel);
            w.setAlwaysOnTop(!w.isAlwaysOnTop());
            final boolean alwaysOnTop=w.isAlwaysOnTop();
            decorate(w);
            if (!initializing && properties != null && savior != null) {
            	properties.setProperty(propertyName, ""+alwaysOnTop);
            	savior.save(properties);
            }
        }
        
        private void decorate(final Window w){
        	final boolean hasText=!Basics.isEmpty(getText());
            if (w.isAlwaysOnTop()) {
                setIcon(MmsIcons.getUnpinIcon());
                if (hasText){
                	setText("<html><font face='verdana'><small>Unpin</small></font></html>");
                }
                setToolTipText(Basics.toHtmlUncentered("Un-pin", "Click this so that this window<br>does not stay on top"));                
            } else {
                setIcon(MmsIcons.getPinIcon());
                if (hasText){
                	setText("<html><font face='verdana'><small>Pin</small></font></html>");
                }
                setToolTipText(Basics.toHtmlUncentered("Pin", "Click this so that this window<br>stays on top"));                
            }
        }
    }


    public static Collection getItems(final JComboBox cb){
        final int n=cb.getItemCount();
        final Collection c=new ArrayList();
        for (int i=0;i<n;i++){
            c.add(cb.getItemAt(i));
        }
        return c;
    }

    public static void setItems(final JComboBox cb, final Collection<Object> c){
        for (final Object o:c){
            cb.addItem(o);
        }
    }

    public static void addItems(final JComboBox cb, final Object []c){
        for (final Object o:c){
            cb.addItem(o);
        }
    }

    public static void replaceItems(final JComboBox cb, final Vector v){
    	cb.removeAllItems();
    	final ActionListener[]a=cb.getActionListeners();
		for (final ActionListener al:a){
			cb.removeActionListener(al);
		}
        for (final Object o:v){
            cb.addItem(o);
        }
		for (final ActionListener al:a){
			cb.addActionListener(al);
		}
    }

    public static void setEnabled(final AbstractButton b, final boolean enabled){
        if (b != null){
            b.setEnabled(enabled);
            Color color = UIManager.getColor("Button.foreground");            
            b.setForeground(enabled ? (color != null ? color : SystemColor.textText) : SystemColor.textInactiveText);            	
        }
    }

    public static void setHeight(final JComponent heightStandard, final JComponent toResize){
        final Dimension d1=heightStandard.getPreferredSize(),
                           d2=toResize.getPreferredSize();
        toResize.setPreferredSize(new Dimension(d2.width, d1.height));
    }

    public static void scrollToVisible(final JTable table, final int visualRowIndex, final int vColIndex) {
    	scrollToVisible(table, visualRowIndex, vColIndex,0,0);
    }
    
    public static void scrollToVisible(final JTable table, final int visualRowIndex, final int vColIndex, final int extraHeight, final int extraWidth) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        final Rectangle rect = table.getCellRect(visualRowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        final Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);
        rect.height+=extraHeight;
        rect.width+=extraWidth;

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    public static void adjustToNorthWestIfNotVisible(final JTable table, final Rectangle rect) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        final JViewport viewport = (JViewport) table.getParent();


        // The location of the viewport relative to the table
        final Point pt = viewport.getViewPosition();
        if (rect.x < pt.x){
        	rect.x=pt.x;
        }
        if (rect.y<pt.y){
        	rect.y=pt.y;
        }

    }

    public static void setTitle(final Frame frame, final String title){
        frame.setTitle(Basics.stripSimpleHtml(title));
    }

    public static void setTitle(final java.awt.Dialog frame, final String title){
        frame.setTitle(Basics.stripSimpleHtml(title));
    }

    public static void repack(final Window w){
        if (w instanceof java.awt.Dialog){
            final java.awt.Dialog d=(java.awt.Dialog)w;
            if (!d.isResizable()){
                d.setResizable(true);
                d.pack();
                d.setResizable(false);
                return;
            }
        } else if (w instanceof Frame){
            final Frame f=(Frame)w;
            if (!f.isResizable()){
                f.setResizable(true);
                f.pack();
                f.setResizable(false);
                return;
            }
        }
        w.pack();
    }

    public static Font getLabelFont(final boolean bold){
        final Font font = FONT_FACE_FAVORITE;
        return new Font(font.getName(), bold ? Font.BOLD : 0,
                        font.getSize());
    }

    public static Font getTitleFont(){
        Font font = UIManager.getFont("Label.font");
        return new Font(font.getName(), Font.ITALIC | Font.BOLD,
                        font.getSize() + 2);
    }

    private static void getComponents(final Collection<VagueWindowComponent> c, final SelectableActiveFilterGroup group){
  	  int i=0;
  	  final boolean verbose=PersonalizableTableModel.getTreeShowCounts();
  	  final String and=verbose?"and":"|";
  	  final String or=verbose?"or":"";
  	  for (final SelectableActiveFilter o : group) {
			if (o instanceof SelectableActiveFilterGroup) {
				SelectableActiveFilterGroup g = (SelectableActiveFilterGroup) o;
				if (g.size() > 0 && g.isVisible()) {
					if (i > 0) {
						c.add(new VagueLabel((group.isAndLogic() ? and : or)));
					}
					if (verbose) {
						c.add(new VagueLabel("("));
					}
					getComponents(c, g);
					if (verbose) {
						c.add(new VagueLabel(")"));
					}
				}
			} else {
				if (i > 0) {
					c.add(new VagueLabel((group.isAndLogic() ? and : or)));
				}
				if ((o instanceof VagueWindowComponent)) {
					c.add((VagueWindowComponent) o);
				}
			}
			i++;
		}
	}
    
    public static Collection<VagueWindowComponent>getComponents(final SelectableActiveFilterGroup group){
  	  final Collection<VagueWindowComponent> c=new ArrayList<VagueWindowComponent>();
  	  c.add(new SeeLabel());
  	  getComponents(c, group);
  	  return c;
    }
    
    private final static class SeeLabel extends VagueLabel{
      public SeeLabel() {
          super("<html><b>See:&nbsp;&nbsp;</b></html>");
      }

      public boolean closeOnClick() {
          return false;
      }

      public boolean enableWhenSelected() {
          return false;
      }

      public JComponent getComponent() {
          return this;
      }
  }
    

    private static class VagueLabel extends JLabel implements VagueWindowComponent {
  	  private static final String PREFIX="<html><b><font color='blue'>", SUFFIX="</font></b></html>";

  	    public VagueLabel(final String text) {
  	        super(PREFIX+text+SUFFIX);
  	    }

  	    public boolean closeOnClick() {
  	        return false;
  	    }

  	    public boolean enableWhenSelected() {
  	        return false;
  	    }

  	    public JComponent getComponent() {
  	        return this;
  	    }
  	}
    
    public static MenuContainer newMenu(final JMenu jm) {
    	return new MenuContainer() {
    		public void add(final JMenuItem jmi) {
    			jm.add(jmi);
    		}
    		public void addSeparator() {
    			jm.addSeparator();
    		}
    	};
    }

    
    public static MenuContainer newMenu(final JPopupMenu jm) {
    	return new MenuContainer() {
    		public void add(final JMenuItem jmi) {
    			jm.add(jmi);
    		}
    		public void addSeparator() {
    			jm.addSeparator();
    		}
    	};
    }

    public static void move(final JList jl, ArrayBasics.Direction d) {
    	final Object []v1=getDataList(jl);
    	final int []selected=jl.getSelectedIndices();
    	final Object []v2=ArrayBasics.move(v1, selected, d);
    	final int []n=ArrayBasics.move(selected, d, v1.length-1);
    	jl.clearSelection();
    	jl.setListData(v2);
    	for (final int i:n) {
    		jl.addSelectionInterval(i, i);    		
    	}
    	if (d == ArrayBasics.Direction.UP || d== ArrayBasics.Direction.TOP) {
    		jl.ensureIndexIsVisible(n[0]);
    	}else {
    		jl.ensureIndexIsVisible(n[n.length-1]);        		
    	}
    }
    
    public static Object []getDataList(final JList jl) {
	   final ListModel lm=jl.getModel();
	   final Collection c=new ArrayList();
	   for (int i=0;i<lm.getSize();i++) {
		   c.add(lm.getElementAt(i));
	   }   
	   return c.toArray();
   }
    
    public static ImageIcon loadImageIcon(Class cls, String name) {
		ImageIcon icon = null;
		URL url = cls.getResource(name);
		if (url != null) {
			icon = new ImageIcon(url);
		}
		return icon;
	}
    
    public static void registerEscapeKeyAction(final Window windowToClose, 
    		final JComponent componentToRegister, 
    		final JComponent componentToIdentifyContext)
    {
    	Action escAction = new AbstractAction() {
	        public void actionPerformed(ActionEvent evt) {
	        	SwingBasics.closeWindow(windowToClose);                	
	        }
        
	       public boolean isEnabled() {
	            return (componentToIdentifyContext != null) 
	            && (componentToIdentifyContext.isEnabled());
	       }
    	};
	    componentToRegister.getActionMap().put("esc-action", escAction);
	    InputMap im = componentToRegister.getInputMap(
	    		JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	    im.put(key, "esc-action");
    	
    }    
    
    public static void resizeIfBiggerThanScreen(final Window w) {
    	final Rectangle screenSize = getScreen(w);
        final Dimension windowSize = w.getSize();
        final boolean tooBig=windowSize.height > screenSize.height || windowSize.width>screenSize.width;
        if (tooBig) {        	
        	if (windowSize.height < screenSize.height) {
        		w.setLocation(screenSize.x+4,w.getY());
            	w.setSize(new Dimension(screenSize.width-10, windowSize.height));
        	}else if (windowSize.width<screenSize.width){
        		w.setLocation(w.getX(), screenSize.y+4);
            	w.setSize(new Dimension(windowSize.width, screenSize.height-20));
        	} else {
        		w.setLocation(screenSize.x+4,screenSize.y+4);
            	w.setSize(new Dimension(screenSize.width-10, screenSize.height-20));
        	}
        }
    }
    
    public static int indexOf(final JComboBox comboBox, final Object value) {
    	if (value != null) {
    	   final ListModel lm=comboBox.getModel();
    	   for (int i=0;i<lm.getSize();i++) {
    		   if (Basics.equals(value, lm.getElementAt(i))) {
    			   return i;
    		   }
    	   }   
    	}
    	   return -1;
    }
    public static void switchContainers(
    		final JComponent containee,
    		final Container newContainer) {
		final Container oldContainer=containee.getParent();
		if (oldContainer != null) {
		oldContainer.remove(containee);
		}
		newContainer.add(containee);
		containee.setVisible(true);
		if (newContainer instanceof JComponent) {
            ((JComponent) newContainer).updateUI();
        } else {
            containee.updateUI();
        }

    }
    
    public static boolean setEnabledIfNoAnomaly(final AbstractButton b, final String _anomaly) {
		if (_anomaly != null) {
			String anomaly=_anomaly;
    		if (!anomaly.toLowerCase().startsWith("<html>")) {
    			anomaly=Basics.startHtml()+anomaly+Basics.endHtml();
    		}

			b.setToolTipText(anomaly);
			b.setEnabled(false);
			return false;
		}
		b.setToolTipText(null);
		b.setEnabled(true);
		return true;
    }

	public static int indexOf(final Container jp, final Component c) {
		final Component []cs=jp.getComponents();
		for (int i=0;i<cs.length;i++) {
			if (cs[i]==c) {	
				return i;
			}
		}
		return -1;		
	}
	
	public static JMenuItem getMenuItem(final String txt, final Icon icon, final char mnemnoic, final ActionListener al, final String toolTip) {
		final JMenuItem jm=new JMenuItem(txt,icon);
		jm.setIcon(icon);
		jm.addActionListener(al);
		jm.setToolTipText(Basics.toHtmlUncentered(txt,toolTip));
		return jm;
	}
	static interface AutoCompleteActionListener extends ActionListener {
		public boolean isFound();
		public void resetAsUnfound();
	}
	
	static void registerAutoCompleteKeyboardAction(final JComponent jc, final AutoCompleteActionListener selectAutoComplete, final AutoCompleteActionListener addAutoComplete, final ActionListener searchDown, final ActionListener searchUp, final ActionListener searchRight, final ActionListener searchLeft, final boolean doDownRightOnlyIfFound){
        
        final ActionListener priorAddKeyAction=jc.getActionForKeyStroke(searchAddSelectKey);
        
		jc.registerKeyboardAction(new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				if (addAutoComplete.isFound()){
					addAutoComplete.actionPerformed(ae);
					addAutoComplete.resetAsUnfound();
				} else if (priorAddKeyAction!=null){
					priorAddKeyAction.actionPerformed(ae);
				} 
			}
		}, searchAddSelectKey, JComponent.WHEN_FOCUSED);		
		
	
        final KeyStroke enterKey=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);        
        final ActionListener priorSelectKeyAction=jc.getActionForKeyStroke(searchSelectKey);

		jc.registerKeyboardAction(new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				if (selectAutoComplete.isFound()){
					selectAutoComplete.actionPerformed(ae);
					selectAutoComplete.resetAsUnfound();
				} else if (searchSelectKey.equals(enterKey) && jc.getRootPane()!=null && jc.getRootPane().getDefaultButton()!=null){
					if (!jc.getRootPane().getDefaultButton().isEnabled()){
						if ( !Basics.isEmpty(jc.getRootPane().getDefaultButton().getToolTipText())){
							ToolTipOnDemand.getSingleton().show(jc.getRootPane().getDefaultButton(), false);
						}
					} else {
						jc.getRootPane().getDefaultButton().doClick(50);
					}					
				}else if (priorSelectKeyAction != null){
					priorSelectKeyAction.actionPerformed(ae);
				} 
			}
		}, searchSelectKey, JComponent.WHEN_FOCUSED);
		{
        final ActionListener priorNextKeyAction=jc.getActionForKeyStroke(searchDownKey);
        
		jc.registerKeyboardAction(new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				if (!doDownRightOnlyIfFound || selectAutoComplete.isFound()){
					searchDown.actionPerformed(ae);					
				} else if (priorNextKeyAction!=null){
					priorNextKeyAction.actionPerformed(ae);
				} 
			}
		}, searchDownKey, JComponent.WHEN_FOCUSED);
		}
		if (searchUp != null){			
	        final ActionListener priorUpKeyAction=jc.getActionForKeyStroke(searchUpKey);	        
			jc.registerKeyboardAction(new ActionListener(){
				public void actionPerformed(final ActionEvent ae){
					if (selectAutoComplete.isFound()){
						searchUp.actionPerformed(ae);					
					} else if (priorUpKeyAction!=null){
						priorUpKeyAction.actionPerformed(ae);
					} 
				}
			}, searchUpKey, JComponent.WHEN_FOCUSED);

		}
		if (searchRight != null){			
	        final ActionListener priorRightKeyAction=jc.getActionForKeyStroke(searchRightKey);	        
			jc.registerKeyboardAction(new ActionListener(){
				public void actionPerformed(final ActionEvent ae){
					if (!doDownRightOnlyIfFound || selectAutoComplete.isFound()){
						searchRight.actionPerformed(ae);					
					} else if (priorRightKeyAction!=null){
						priorRightKeyAction.actionPerformed(ae);
					} 
				}
			}, searchRightKey, JComponent.WHEN_FOCUSED);

		}
		if (searchLeft != null){
		    final ActionListener priorLeftKeyAction=jc.getActionForKeyStroke(searchLeftKey);	        
			jc.registerKeyboardAction(new ActionListener(){
				public void actionPerformed(final ActionEvent ae){
					if (selectAutoComplete.isFound()){
						searchLeft.actionPerformed(ae);					
					} else if (priorLeftKeyAction!=null){
						priorLeftKeyAction.actionPerformed(ae);
					} 
				}
			}, searchLeftKey, JComponent.WHEN_FOCUSED);

		}

	}
	
	private static String getText(final KeyStroke ks){
		final StringBuilder sb=new StringBuilder();
		final int kc=ks.getKeyCode(), m=ks.getModifiers();
		if (m>0){
			sb.append(InputEvent.getModifiersExText(m));
		} 
		sb.append(KeyEvent.getKeyText(kc));
		return Basics.encodeHtml(sb.toString());
	}
	private static final int searchKeyModifiers=InputEvent.ALT_MASK;
	private static final KeyStroke
	searchSelectKey=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
	searchAddSelectKey=getKeyStrokeWithMetaIfMac(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK),
	searchLeftKey=altOrMeta(KeyEvent.VK_LEFT, true),
	searchDownKey=altOrMeta(KeyEvent.VK_DOWN, true),
	searchUpKey=altOrMeta(KeyEvent.VK_UP, true),
	searchRightKey=altOrMeta(KeyEvent.VK_RIGHT, true);
    
	static String
	searchSelectKeyText=getText(searchSelectKey),
	searchAddSelectKeyText=getText(searchAddSelectKey),
	searchLeftKeyText=getText(searchLeftKey),
	searchDownKeyText=getText(searchDownKey),
	searchUpKeyText=getText(searchUpKey),
	searchRightKeyText=getText(searchRightKey),
	searchRightLeft=SwingBasics.searchLeftKeyText+"<font color='red'><b>/</b></font>"+SwingBasics.searchRightKeyText, 
	searchDownUp=SwingBasics.searchDownKeyText+"<font color='red'><b>/</b></font>"+SwingBasics.searchUpKeyText;
	
	public static Icon pressedIcon;
	
	
	public static void removeActionListeners(final AbstractButton ab){
		for (final ActionListener al:ab.getActionListeners()){
			ab.removeActionListener(al);
		}
	}
	
	public static void addSeparator(final JComponent jp){
		if (jp instanceof JPopupMenu) {
			((JPopupMenu) jp).addSeparator();
		} else if (jp instanceof JMenu) {
			((JMenu) jp).addSeparator();
		}

	}
	
	public static void packColumns(final JTable table, final int margin) {
		packColumns(table, margin, false);
	}
	public static void packColumns(final JTable table, final int margin, final boolean max) {
        for (int c=0; c<table.getColumnCount(); c++) {
            packColumn(table, c, margin,max);
        }
    }
    
    // Sets the preferred width of the visible column specified by vColIndex. The column
    // will be just wide enough to show the column head and the widest cell in the column.
    // margin pixels are added to the left and right
    // (resulting in an additional width of 2*margin pixels).
    public static void packColumn(final JTable table, final int vColIndex, final int margin, final boolean max) {
        TableModel model = table.getModel();
        DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);
        int width = 0;
    
        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
            table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;
    
        // Get maximum width of column data
        for (int r=0; r<table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }
    
        // Add margin
        width += 2*margin;
    
        // Set the width
        if (max){
        	col.setMaxWidth(width);
        }else{
        col.setPreferredWidth(width);
        }
    }
    
    public static JMenuItem addLabel(final JPopupMenu jp, final String label){
		return addLabel(jp, null, label);
    }
    
    public static void addCloseButton(final JPopupMenu jp){
    	final JMenuItem mi=new JMenuItem(Basics.concat("<html>", Basics.duplicate("&nbsp;", 75), "<img src='", MmsIcons.getURLText("close.gif"), "'></html>"));
		mi.setEnabled(true);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent t) {
				jp.setVisible(false);
			}
		});
		jp.add(mi);
    }
    
    public static JMenuItem addLabel(final JPopupMenu jp, Icon icon, final String label){
    	final JMenuItem mi=new JMenuItem(label);
		mi.setEnabled(false);
		mi.setForeground(SystemColor.activeCaptionText);
		if (icon != null){
			mi.setIcon(icon);
		}
		jp.add(mi);
		return mi;
    }
    
    public static void scrollTo(final JTable table, final int row, final int col){
    	final Rectangle r = table.getCellRect(row, col, false);
		table.scrollRectToVisible(r);
    }

    public static void repaint(final JTree tree, final TreePath treePath){
    	if (treePath != null) {
			final Rectangle r = tree.getPathBounds(treePath);
			if (r != null) {
				tree.repaint(r);
			}
		}

    }
    
    public static Collection<Row> handleMultiSelectRows(
    		final JComponent jc,
    		final GroupedDataSource.Node node, 
    		final Collection<Row> _rows){
		Collection<com.MeehanMetaSpace.swing.Row> rows;		
		if (node != null){					
			rows=node.getChildRows();
		} else {
			rows=_rows;
		}
		if (rows.size()<_rows.size()){
			final int choice=PopupBasics.getChosenIndex(
					jc,
					"<html>You dragged <b>"+node.toString()+"</b> which relates to "+rows.size() + " rows of the table, <br>But "+_rows.size()+ " rows are selected.  What do you want to drop?",
					"Drop all nodes?",
					new String[]{rows.size()+" rows", _rows.size()+" rows"}, 0, true, false);
			if (choice==-1){
				return null;
			}
			if (choice==1){
				rows=_rows;
			}			
		}
		return rows;

    }
    
    public static boolean endsWith(final TreeNode node, final String[] args) {
    	if (args==null){
    		return false;
    	}
		boolean ok = true;
		TreeNode _node = node;
		for (int i = args.length - 1; _node != null && ok && i >= 0; i--, _node = _node.getParent()) {
			if (!Basics.equals(_node.toString(), args[i])) {
				ok = false;
			}
		}

		return ok;
	}
    
	public static TreePath getChild(final TreePath path, final String childNodeName) {
		final GroupedDataSource.Node node = (GroupedDataSource.Node) path.getLastPathComponent();
		final int n = node.getChildCount();
		for (int i = 0; i < n; i++) {
			final GroupedDataSource.Node child = (GroupedDataSource.Node) node.getChildAt(i);
			final String _childNodeName = child.toString();
			if (_childNodeName.equals(childNodeName)) {
				return child.getPathFromRoot();
			}

		}
		return null;
	}
	
	public static TreePath getLastTreePathFound(final JTree tree, final String []args){
		final TreePath[] tps = findInSubtree(tree, args);
		if (!Basics.isEmpty(tps)) {
			return tps[tps.length - 1];
		}
		return null;
	}

	static void draw(final ImageIcon img,
			final Graphics g,
			final JScrollPane jsp) {

		if (img != null) {
			// Draw the background image
			final Rectangle d = jsp.getViewport().getViewRect();
			final int w1 = d.width, w2 = img.getIconWidth();
			final int h1 = d.height, h2 = img.getIconHeight();

			g.drawImage(img.getImage(), w1 > w2 ? (w1 - w2) / 2 : 0,
					h1 > h2 ? (h1 - h2) / 2 : 0, w2, h2, jsp);
			// Do not use cached image for scrolling
			jsp.getViewport().setBackingStoreEnabled(true);
		}
	}
 
	public static void setProperty(final String name, final String value) {
		final Properties p = PersonalizableTableModel.getGlobalProperties();
		p.setProperty(name, value);
		PropertiesBasics.saveProperties(p, PopupBasics.getPropertyFileName(p),
				"");
	}

	public static String getProperty(final String name) {
		final Properties p = PersonalizableTableModel.getGlobalProperties();
		return p.getProperty(name,null);
	}


	public static void scrollFirstSelected(final JTable table, final int selectedRow, final int selectedColumn) {
		int r=selectedRow, c=selectedColumn;
		if (r <0) {
			r=table.getSelectedRow();
		}
		if (c<0) {
			c=table.getSelectedColumn();
		}
		final Rectangle rect = table.getCellRect(r, c, true);
		scrollRectToVisibleForAllParents(table, rect, null);
	}

	/**
	 * This overcomes the limitation of JComponent.scrollRectToVisible which does not 
	 * guarantee that the childRect is visible on the window.   JComponent.scrollRectToVisible only
	 * guarantees that the childRect is visible to the parent component which itself may be hidden
	 * by another ancestor's JViewport
	 * 
	 * @param parent
	 * @param childRect
	 */
	public static void scrollRectToVisibleForAllParents(
			final JComponent parent, Rectangle childRect) {
		scrollRectToVisibleForAllParents(parent, childRect, null);
	}
	
	private static void scrollRectToVisibleForAllParents(
			final JComponent parent, Rectangle childRect,
			final Rectangle grandChildRectVisibleToChild) {
		if (grandChildRectVisibleToChild != null) {
			childRect.x += grandChildRectVisibleToChild.x;
			childRect.width = grandChildRectVisibleToChild.width
					+ (grandChildRectVisibleToChild.width / 2);
			childRect.y += grandChildRectVisibleToChild.y;
			childRect.height = grandChildRectVisibleToChild.height
					+ (grandChildRectVisibleToChild.height);
		}
		parent.scrollRectToVisible(childRect);
		final JViewport parentViewport = getNextViewPort(parent);
		if (parentViewport != null) {
			Container grandParent = parentViewport.getParent();
			if (grandParent instanceof JComponent) {
				final JViewport nextVp = getNextViewPort(grandParent);
				if (nextVp != null) {
					Rectangle nextGrandChildVisibleRect=null, parentRect=null;
					final Point point = parentViewport.getViewPosition();
					int x = childRect.x - point.x, y = childRect.y
							- point.y;
					boolean first=true;
					parentRect = parent.getBounds();
					nextGrandChildVisibleRect = new Rectangle(x, y,
							childRect.width , childRect.height);
					for (;;) {
						Container greatGrandParent = grandParent.getParent();
						if (greatGrandParent == nextVp) {
							break;
						} else {
							if (!first) {
								x+=parentRect.x;
								y+=parentRect.y;								
							}
							first=false;
							childRect = parentRect;
							parentRect = grandParent.getBounds();
							nextGrandChildVisibleRect = new Rectangle(x, y,
									nextGrandChildVisibleRect.width, nextGrandChildVisibleRect.height);
							grandParent = greatGrandParent;
						}

					}
					scrollRectToVisibleForAllParents(
							(JComponent) grandParent, parentRect,
							nextGrandChildVisibleRect);
				}else if(grandChildRectVisibleToChild != null && parent != null){
					Window window = SwingUtilities.getWindowAncestor(parent);
					if (window != null) {
						window.repaint();												
					} else {
						System.out.print(" um er ");
					}
				}
			}
		} 
	}
	
	public static JViewport getNextViewPort(final Component c) {
		for (Container ct=c.getParent();ct!=null;ct=ct.getParent()) {
			if (ct instanceof JViewport) {
				return (JViewport)ct;
			}
		}
		return null;
	}
	
	public static void ensureUniqueMnenmonics(final ButtonGroup bg) {
		final Enumeration<AbstractButton>en=bg.getElements();
		final Collection<Character>cMnemonics=new ArrayList<Character>();
	    while(en.hasMoreElements()) {
			final AbstractButton b=en.nextElement();
			int j=0;
			final String s=b.getText();
			char ch = s.charAt(j);
			while (cMnemonics.contains(ch) && j < s.length()) {
				ch = s.charAt(j++);
			}
			if (j == s.length()) {
				ch = s.charAt(0);
			}
			cMnemonics.add(ch);
			b.setMnemonic(ch);
		}
	}
	
	
	public static Collection<GroupedDataSource.Node> getChildNodes(final PersonalizableTableModel tm, final String []args){
		final JTree tree=tm.getTree();
		final Collection<GroupedDataSource.Node> c=new ArrayList<GroupedDataSource.Node>();
		if (tree != null){
			tm.useRenderOnlyValueForTree=false;
			final TreePath tp=getLastTreePathFound(tree, args);
			tm.useRenderOnlyValueForTree=true;
			if (tp != null){
				final GroupedDataSource.Node parentNode=(GroupedDataSource.Node)tp.getLastPathComponent();
				final int n=parentNode.getChildCount();
				for (int i=0;i<n;i++){
					final GroupedDataSource.Node child=(GroupedDataSource.Node)parentNode.getChildAt(i);
					c.add(child);
				}
			}
		}
		return c;
	}
	
	public static boolean hasChild(final TreeNode parentNode, final String arg){
		final int n=parentNode.getChildCount();
		for (int i=0;i<n;i++){
			final String s=Basics.toString(parentNode.getChildAt(i));
			if (arg.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isInView(final JTree tree, final TreePath tp) {
		final Object jsp = tree.getParent();
		if (jsp instanceof JViewport) {
			final JViewport jvp = (JViewport) jsp;
			final Rectangle r = jvp.getViewRect();
			final TreeModel model=tree.getModel();
			if (model instanceof DefaultTreeModel){
				return isInView(tree, tp, r);
			}
		}
		return false;
	}
	
	public static boolean isInView(final JTree tree, final TreePath tp, final Rectangle r){
		final Rectangle tpR=tree.getPathBounds(tp);
		if (tpR.x>=r.x && tpR.x < (r.x+r.width) && tpR.y>=r.y && tpR.y<(r.y+r.height)){
			return true;
		}else{
		return false;
		}
	}

	
	
	public static boolean saveView(final JTree tree,
			final TreePath startingPath, final Rectangle r,
			final ArrayList<String[]> saved) {
		boolean reachedEndOfView = false;
		if (tree.isVisible(startingPath)) {
			boolean startingPathIsInView = isInView(tree, startingPath, r);
			final boolean priorPathWasHidden = saved.size() == 0;
			if (startingPathIsInView) {
				if (priorPathWasHidden) {
					final String[] next = getNodeNamesAfterRoot(startingPath);
					saved.add(next);
				}
			} else if (!priorPathWasHidden) {
				reachedEndOfView = true;// reached end of view
			}
			if (!reachedEndOfView) {
				final int priorSize = saved.size();
				if (tree.isExpanded(startingPath)) {
					final TreeNode parent = (TreeNode) startingPath
							.getLastPathComponent();
					final int n = parent.getChildCount();
					for (int i = 0; i < n; i++) {
						final TreeNode child = parent.getChildAt(i);
						final TreePath childPath = startingPath
								.pathByAddingChild(child);
						reachedEndOfView = saveView(tree, childPath, r, saved);
						if (reachedEndOfView) {
							break;
						}
					}
				}
				final int endSize = saved.size();
				if (priorSize == endSize && startingPathIsInView
						&& !priorPathWasHidden) {
					final String[] last = saved.get(saved.size() - 1);
					final int next = startingPath.getPathCount();
					if (next!= last.length) {
						saved.add(getNodeNamesAfterRoot(startingPath));
					}
				}
			}
		}

		return reachedEndOfView;
	}

	public static final ArrayList<String[]> saveView(final JTree tree){
		final ArrayList<String[]> saved=new ArrayList<String[]>();
		final Object jsp=tree.getParent();
		if (jsp instanceof JViewport){
			final JViewport jvp=(JViewport)jsp;
			final Rectangle r=jvp.getViewRect();
			final TreePath rootPath=tree.getPathForLocation(0, 0);
			saveView(tree, rootPath, r,saved);
		}
		return saved;
	}


	public static final void restoreView(final JTree tree,
			final List<String[]> saved) {
		if (!Basics.isEmpty(saved)) {
			String[]prior=saved.get(0);
			TreePath found = findFirst(tree, prior);
			if (found != null) {
				tree.scrollPathToVisible(found);
				for (int i=0;i<saved.size();i++){
					final String []arg=saved.get(i);
					if (arg.length>prior.length || (!Basics.isSameLineage(prior,arg))){
						found = findFirst(tree, arg);
						if (found != null) {
							tree.makeVisible(found);
						} 
					}
					prior=arg;
				}
			}
		}
	}
	
    // Finds the path in tree as specified by the array of names.
    // The names array is a sequence of names where names[0]
    // is the root and names[i] is a child of names[i-1].
    // Comparison is done using String.equals().
    public static TreePath findFirst(final JTree tree, final String[] names) {
        final TreeNode root = (TreeNode) tree.getModel().getRoot();
        return findFirst(tree, new TreePath(root), Basics.prepend("*",names), 0);
    }

    private static TreePath findFirst(
      final JTree tree,
      final TreePath parent,
      final String[] nodeNames,
      final int depth) {
        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        final String o = node.toString().toLowerCase();

        boolean equals = true;
        String arg;
        int n;
        if (nodeNames[depth] != null) {
            arg = nodeNames[depth].toLowerCase();
            n = arg.length();
        } else {
            arg = "";
            n = 0;
            equals = true;
        }
        if (n > 0) {
            if (arg.charAt(n - 1) == '*') {
                if (n < 2) {
                    equals = true;
                } else {
                    arg = arg.substring(0, n - 1);
                    equals = o.startsWith(arg);
                }
            } else {
                equals = o.equals(arg);
            }
        }
        // If equal, go down the branch
        if (equals) {
            // If at end, return match
            if (depth == nodeNames.length - 1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (@SuppressWarnings("unchecked")
				final Enumeration<TreeNode> e = (Enumeration<TreeNode>)node.children(); e.hasMoreElements(); ) {
                    final TreeNode nextNode = e.nextElement();
                    final TreePath path = parent.pathByAddingChild(nextNode);
                    final TreePath found=findFirst(tree, path, nodeNames, depth + 1);
                    if (found != null){
                    	return found;
                    }
                }
            }
        }
        return null;
    }
    
    public static String toPathText(GroupedDataSource.Node node, final int treePickLevel){
    	final ArrayList<String>c=new ArrayList<String>();
    	while (node != null && node.sortIndexThatDiffers>=treePickLevel){
    		c.add(node.toString());
    		node=(GroupedDataSource.Node)node.getParent();
    	}
    	final StringBuilder sb=new StringBuilder();
    	int i=c.size()-1;
    	for (;i>=0;i--){
    		if (i<c.size()-1){
    			sb.append(" / ");
    		}
    		sb.append(Basics.encodeHtml(c.get(i)));
    	}
    	return sb.toString();
    }

    public static void addMouseOver(final JComponent cmp, final Color highlightColor, final Color doingColor){
    	new MouseOver(cmp, highlightColor, doingColor);
    }
    
    private final static Color defaultEnabledHighlight=new Color(0, 153, 0), windowsForeground=new Color(142,142, 254);
    public static Color getAlreadyDoingBackground(){
    	return PersonalizableTable.YELLOW_STICKY_COLOR;
    }
    
    public static Color getAlreadyDoingForeground(){
    	return windowsForeground;
    }
    
    public static void addMouseOver(final JComponent cmp){
    	final MouseListener[]ms=cmp.getMouseListeners();
    	for (final MouseListener ml:ms){
    		if (ml instanceof MouseOver){
    			return;
    		}
    	}
    	new MouseOver(cmp, defaultEnabledHighlight, PersonalizableTable.YELLOW_STICKY_COLOR);
    }

    private static Color highlightQuaQua=new Color(12,207,8);
    private static class MouseOver extends MouseAdapter{
    	private final JComponent cmp;
    	private final Color highlightColor, doingColor;
    	private MouseOver(final JComponent cmp, final Color highlightColor, final Color doingColor){
    		this.cmp=cmp;
    		this.doingColor=doingColor;
    		this.highlightColor=highlightColor;
    		cmp.addMouseListener(this);
    	}
    	private Cursor priorCursor;
    	private final Cursor cursor=new Cursor(Cursor.HAND_CURSOR);
    	private Color priorFg, priorBg;

		public void mouseEntered(final MouseEvent e) {
			priorFg = cmp.getForeground();
			priorBg = cmp.getBackground();
			final boolean notDoing = priorBg == null || priorBg != doingColor;
			if (priorFg != null && !priorFg.equals(highlightColor)) {
				priorCursor = cmp.getCursor();
				if (notDoing) {
					if (cmp.isEnabled()) {
						if (!isQuaQuaLookAndFeel() ){
							cmp.setForeground(highlightColor);
						} else {
							cmp.setForeground(highlightQuaQua);
						}
						cmp.setCursor(cursor);
					} else {
						cmp.setForeground(Color.red);
					}
/*					if (cmp instanceof JMenuItem) {
				        final Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
						final Window w=SwingUtilities.getWindowAncestor(cmp);
						final Point p=w.getLocationOnScreen();
						final int wOffset=p.x>screen.width/2?-275:(cmp.getWidth()/4)*3;
						if (cmp instanceof MenuInWindow){
							ToolTipOnDemand.getSingleton().show(cmp, false,
									wOffset, -55, false, false);
							
						} else {
							ToolTipOnDemand.getSingleton().show(cmp, false,
								wOffset, cmp.getHeight()+4, false, false);
						}
					}*/
				}
			}
		}

		public void mouseExited(final MouseEvent e) {
			if (priorFg != null ){
				cmp.setForeground(priorFg);
				cmp.setCursor(priorCursor);
			}
		}

    }
	
	public static void inspectHierarchy(final Component c) {
		System.out.println("=========== Inspecting ");
		inspectHierarchy(c, 1);
		System.out.println("===========  done");
	}
	
	private static void inspectHierarchy(final Component c, final int level) {
		for (int i=0;i<level;i++){
			System.out.print(" ");
		}
		System.out.println(Basics.concatObjects(level, "-->", c.getClass().getName(), " ", c.isOpaque()));
		if (c instanceof Container) {
			final Container cnt = (Container) c;
			final int n = cnt.getComponentCount();
			for (int i = 0; i < n; i++) {
				final Component cmp = cnt.getComponent(i);
				if (cmp instanceof Container) {
					inspectHierarchy(cmp, level+1);
				}
			}
		}
	}

	private static Map<String, ImageIcon> icons = 
    	new HashMap<String, ImageIcon>();
	

    public static ImageIcon getImageIcon(final Class _class, final String subFolder, final String fileName, String fileExtension) {
    	if (!fileExtension.startsWith(".")){
    		fileExtension=Basics.concat(".", fileExtension.toLowerCase());
    	} else {
    		fileExtension=fileExtension.toLowerCase();
    	}
    	final String key=Basics.concat(_class.getName(), ":", subFolder, "/", fileName, fileExtension);
        ImageIcon icon = icons.get(key);
        if (icon == null){
        	final String key2=Basics.concat(_class.getName(), ":", subFolder, "/", fileName, fileExtension.toUpperCase());
        	icon = icons.get(key2);
        	if (icon != null){
        		return icon;
        	}
        }
        if (icon == null || icon.getIconWidth() == -1) {
            icon = loadImageIcon(
            		_class, Basics.concat(subFolder, "/", fileName, fileExtension.toLowerCase()));
            if (icon==null){
            	icon = loadImageIcon(
            			_class, Basics.concat(subFolder, "/", fileName, fileExtension.toUpperCase()));         	
            }
            if (icon == null && !fileName.equals("spacer")) {
                icon = getImageIcon(MmsIcons.class, subFolder, "spacer", ".gif");
            }
            icons.put(key, SwingBasics.ResizeIfNeeded(icon));
        }
        return icon;
    }

    private static void encode(final StringBuilder sb, final String s) {
    	if (s != null){
            sb.append("%20%22");
            sb.append(java.net.URLEncoder.encode(s));
            sb.append("%22");
    	}
    }
    
    public static void searchGoogle(final String arg1, final String ... args) {
        final StringBuilder url = new StringBuilder("http://www.google.com/search?q=");
        
        encode(url, arg1);
        for (final String arg:args){
        	encode(url, arg);
        }
        SwingBasics.showHtml(url.toString());
    }

    public static JMenuItem createMenuItem(
			final String menuTxt,
			final ActionListener al,
			final boolean isSelected,
			final String toolTip){
		final JMenuItem mi = new JMenuItem(menuTxt);
		mi.addActionListener(al);
		final String txt=Basics.stripSimpleHtml(menuTxt);
		mi.setMnemonic(txt.charAt(0));
		if (isSelected){
			mi.setEnabled(false);
			mi.setForeground(SwingBasics.getAlreadyDoingForeground());
			mi.setBackground(SwingBasics.getAlreadyDoingBackground());
			mi.setOpaque(true);
			mi.setIcon(MmsIcons.getYesIcon());
			new DisabledExplainer(mi).setEnabled(
					Basics.toHtmlUncentered(
							txt,
							"This item is already selected"));
			
		} else {
			mi.setIcon(MmsIcons.getBlankIcon());
			mi.setEnabled(true);
			mi.setToolTipText(
				Basics.toHtmlUncentered(
					txt,
					toolTip));
		}
		return mi;
	}

    public static void stylizeAsHyperLink(final AbstractButton b){
    	final String text=b.getText();
    	setToolBarStyle(b);
    	b.setText(text);
    	b.setForeground(Color.blue);
    }
 
    public static boolean isWindowTooSmall=true;
    public static KeyStroke addAltOrMetaIfMac(final int key){
    	return altOrMeta(key, false);
    }

    private static boolean isStandardAppleKey(final int key){
		return key == KeyEvent.VK_Q 
				|| key == KeyEvent.VK_COMMA
				|| key == KeyEvent.VK_H 
				|| key == KeyEvent.VK_C 
				|| key == KeyEvent.VK_X 
				|| key == KeyEvent.VK_W 
				|| key == KeyEvent.VK_V;

    }

    public static  KeyStroke getKeyStrokeWithMetaIfMac(final int key, final int modifiers){
    	return getKeyStrokeWithMetaIfMac(key, modifiers,key, 0);
    }    

    public static  KeyStroke getKeyStrokeWithMetaIfMac(final int key, final int modifiers, final int extraMacModifiers){
    	return getKeyStrokeWithMetaIfMac(key, modifiers,key, extraMacModifiers);
    }    
    
    public static  KeyStroke getKeyStrokeWithMetaIfMac(
    		final int key, final int modifiers, final int macKey, final int extraMacModifiers){
    	if (isMacLookAndFeel()){
    		if (  (extraMacModifiers & InputEvent.SHIFT_MASK) != 0
    					|| (extraMacModifiers & InputEvent.ALT_MASK) != 0 
    					|| !isStandardAppleKey(macKey)
    				
    				){
        		final KeyStroke ks=KeyStroke.getKeyStroke(macKey, InputEvent.META_MASK|extraMacModifiers);
        		return ks;
    		}
    	}
    	return KeyStroke.getKeyStroke(key, modifiers);
    }
    
    
    public static  KeyStroke altOrMeta(final int key, final boolean shiftToo){
    	int shift=shiftToo?InputEvent.SHIFT_MASK:0;
    	return getKeyStrokeWithMetaIfMac(key, InputEvent.ALT_MASK|shift, key, shift);
    }
    
       
    public static KeyStroke addCtrlOrMetaIfMac(final int key){
    	return addCtrlOrMetaIfMac(key, false);
    }
    
    public static KeyStroke addCtrlOrMetaIfMac(final int key, final boolean shiftToo){
    	int shift=shiftToo?InputEvent.SHIFT_MASK:0;
    	return getKeyStrokeWithMetaIfMac(key, InputEvent.CTRL_MASK|shift, key, shift);
    }
    

    public static Action getAction(final JComponent cmp, final int condition, final KeyStroke ks){
    	InputMap map = cmp.getInputMap(condition);
    	ActionMap am = cmp.getActionMap();

    	if(map != null && am != null) {
    		Object binding = map.get(ks);
    		if (binding == null) { // really ? no really?
    			final KeyStroke[]aa=map.keys();
    			for (final KeyStroke a:aa){
    				if (a.equals(ks)){
    					System.out.println("Yikes!");
    				}
    			}
    		}
    		Action action = (binding == null) ? null : am.get(binding);
    		if (action != null) {
    			return action;
    		}
    	}
    	return null;
    }

    public static void init(){
		resetDefaultFonts();
    	Basics.gui=PopupBasics.gui;
    	PersonalizableTableModel.setRootDir(System.getProperty("user.home")+"/.mmsSwing");
    	doDefaultLnF();
    	final ColorPreferences colorProperties = ColorPreferences.instantiate();
    	colorProperties.setCurrentPreferences();
    }
    

}