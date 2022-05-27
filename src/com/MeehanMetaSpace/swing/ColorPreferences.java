package com.MeehanMetaSpace.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;


import com.MeehanMetaSpace.*;
import com.MeehanMetaSpace.swing.ColorChooser.Refresher;


public class ColorPreferences {
	
    private static Color genieBlue = new Color (13, 57, 84);
    private static Border genieBorder = BorderFactory.createMatteBorder (12,8,8,8,genieBlue);

	
	//this is added to keep defaults in UIManager as UIManager will not have Have SukerFishMenu Colors
	static{
		UIManager.put(WoodsideMenu.TASK_DISABLED_FOREGROUND, WoodsideMenu.TASK_DISABLED_FOREGROUND_DEFAULT);
		UIManager.put(WoodsideMenu.TASK_FOREGROUND, WoodsideMenu.TASK_FOREGROUND_DEFAULT);
		UIManager.put(WoodsideMenu.TASK_BACKGROUND,  WoodsideMenu.TASK_BACKGROUND_DEFAULT);
		
		UIManager.put(WoodsideMenu.CONTEXT_DISABLED_FOREGROUND, WoodsideMenu.CONTEXT_DISABLED_FOREGROUND_DEFAULT);
		UIManager.put(WoodsideMenu.CONTEXT_FOREGROUND, WoodsideMenu.CONTEXT_FOREGROUND_DEFAULT);
		UIManager.put(WoodsideMenu.CONTEXT_BACKROUND,  WoodsideMenu.CONTEXT_BACKGROUND_DEFAULT);
	}
	
    private static class NamedColor {
        SystemColor color;
        String name;
        NamedColor(final SystemColor color, final String name) {
            this.name = name;
            this.color = color;
        }
    }


    private String getPropertyFilename() {
        return PersonalizableTableModel.getPropertyRootFolder() +
          "color.properties";
    }

    private void loadProperties() {
        final String s = getPropertyFilename();
        properties = PropertiesBasics.loadProperties(s);
    }

    public void saveProperties() {
        final String s = getPropertyFilename();
        PropertiesBasics.saveProperties(properties, s, "color preferences");
    }

    private Map<String, Color> changes = new HashMap<String,Color>(),
    		beforeChanges = new HashMap<String,Color>();
    public void setComplete(final boolean acceptChanges) {
        if (changes.size() > 0 || importFile != null || defaultsProposed) {
            if (acceptChanges) {
                if (defaultsProposed){
                    proposeDefaults();
                    refreshDefaults(false);
                    setCurrentPreferences();
                }
                if (importFile != null) {
                    final Properties p = PropertiesBasics.loadProperties(
                      importFile);
                    PropertiesBasics.copy(properties, p);
                    setCurrentPreferences();
                }
                for (final String k : changes.keySet()) {
                    setColor(k, changes.get(k));
                }
                saveProperties();

                PopupBasics.alert(
                  Basics.toHtmlUncentered("Color changes made...",
                                          "A restart may be necessary before <br>all changes take effect."));
            } else {
                for (final String k : beforeChanges.keySet()) {
                    setColor(k, beforeChanges.get(k));
                }
            }
            beforeChanges.clear();
            changes.clear();
            refresh();
        }
    }

    private void proposeChange(final String fullPropertyName, final Color color, final Color initialColor) {
    	if (!beforeChanges.containsKey(fullPropertyName)){
    		beforeChanges.put(fullPropertyName,initialColor);
    	}
        changes.put(fullPropertyName, color);
    }

    private void refresh(final String fullPropertyName, final Color color) {
    	setColor(fullPropertyName, color);
    	refresh();
    }
    
    private void refresh(){
    	PersonalizableTable.initColors();
    	if (parentWindow != null){
    		parentWindow.invalidate();
    		parentWindow.repaint();
    	}
    	if (parentWindow != SwingBasics.mainFrame && 
    			SwingBasics.mainFrame != null){
    		SwingBasics.mainFrame.invalidate();
    		SwingBasics.mainFrame.repaint();
    	}
    	if (mainColorMenu!=null){
    		mainColorMenu.repaint();
    	}
    }
    
    public void setColor(final String fullPropertyName, final Color color) {
    	if (color != null) {
			if (properties != null) {
				properties.setProperty(fullPropertyName, "" + color.getRGB());
			}
			UIManager.put(fullPropertyName, color);
			handleSimilarities(fullPropertyName, color);
		}
    }

    private void handleSimilarities(final String fullPropertyName,
                                    final Color color) {
        if (fullPropertyName.equals(PersonalizableTableModel.
                                           PROPERTY_ALTERNATING_COLOR)) {
            PersonalizableTableModel.setGlobalAlternatingRowColor(color);
        } else {
        	final Set<String>done=new HashSet<String>();
        	final Collection<String>c=similarColor.getCollection(fullPropertyName);
        	for (final String key:c){
        		UIManager.put(key, color);
        		final String n=toObjectName(key);
        		done.add(n);
        	}
        	final String objectName=toObjectName(fullPropertyName);
        	final String propertyName=toPropertySuffix(fullPropertyName);
        	final Collection<Class>c2=similarObjects.getCollection(objectName);
        	for(final Class cl:c2){
        		final String objectName2=nameMap.get(cl);
        		if (!done.contains(objectName2)){
        			final String fullPropertyName2=Basics.concat(objectName2, ".", propertyName);
        			UIManager.put(fullPropertyName2, color);
        		}
        	}
        }
    }

    private NamedColor[] systemColors = null;
    private Properties properties;

    private JMenuItem getColorMenuItem(
      final String colorName,
      final Color color,
      final String fullPropertyName) {
        final JMenuItem mi = new JMenuItem(colorName);
        mi.setMnemonic(colorName.charAt(0));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                proposeChange(fullPropertyName, color,(Color)UIManager.get(fullPropertyName));
            }
        });
        setColor(mi, fullPropertyName, color);
        return mi;
    }


    private JMenu getSystemMenu(final String fullPropertyName) {
        if (systemColors == null) {
            systemColors = new NamedColor[] {
                           new NamedColor(SystemColor.activeCaption,
                                          "activeCaption"),
                           new NamedColor(SystemColor.activeCaptionBorder,
                                          "activeCaptionBorder"),
                           new NamedColor(SystemColor.activeCaptionText,
                                          "activeCaptionText"),
                           new NamedColor(SystemColor.controlDkShadow,
                                          "controlDkShadow"),
                           new NamedColor(SystemColor.controlHighlight,
                                          "controlHighlight"),
                           new NamedColor(SystemColor.controlLtHighlight,
                                          "controlLtHighlight"),
                           new NamedColor(SystemColor.controlShadow,
                                          "controlShadow"),
                           new NamedColor(SystemColor.controlText,
                                          "controlText"),
                           new NamedColor(SystemColor.desktop, "desktop"),
                           new NamedColor(SystemColor.inactiveCaption,
                                          "inactiveCaption"),
                           new NamedColor(SystemColor.inactiveCaptionBorder,
                                          "inactiveCaptionBorder"),
                           new NamedColor(SystemColor.inactiveCaptionText,
                                          "inactiveCaptionText"),
                           new NamedColor(SystemColor.info, "info"),
                           new NamedColor(SystemColor.infoText, "infoText"),
                           new NamedColor(SystemColor.menu, "menu"),
                           new NamedColor(SystemColor.menuText, "menuText"),
                           new NamedColor(SystemColor.scrollbar, "scrollbar"),
                           new NamedColor(SystemColor.text, "text"),
                           new NamedColor(SystemColor.textHighlight,
                                          "textHighlight"),
                           new NamedColor(SystemColor.textHighlightText,
                                          "textHighlightText"),
                           new NamedColor(SystemColor.textInactiveText,
                                          "textInactiveText"),
                           new NamedColor(SystemColor.window, "window"),
                           new NamedColor(SystemColor.windowBorder,
                                          "windowBorder"),
            };
        }
        final JMenu menu = new JMenu("System color");
        for (int i = 0; i < systemColors.length; i++) {
            final JMenuItem mi = getColorMenuItem(systemColors[i].name,
                                                  systemColors[i].color,

                                                  fullPropertyName);
            menu.add(mi);
        }
        return menu;
    }

    private Collection<ColorProperty> colors = new ArrayList<ColorProperty>();
    private ColorPreferences() {
        loadProperties();
    }

    public static Color getGenieBlue () {
        return genieBlue;
    }

    public static Border getGenieBorder() {
        return genieBorder;
    }

    private static String toObjectName(final String fullPropertyName){
    	final int idx=fullPropertyName.indexOf(".");
    	return fullPropertyName.substring(0, idx);
    }

    private static String toPropertySuffix(final String fullPropertyName){
    	final int idx=fullPropertyName.indexOf(".");
    	return fullPropertyName.substring(idx+1);
    }

    private static Map<String, Class>objectMap=new HashMap<String, Class>();
    private static Map<Class, String>nameMap=new HashMap<Class, String>();
    private static MapOfMany<String, Class>similarObjects=new MapOfMany<String, Class>();
    private static MapOfMany<String, String>similarColor=new MapOfMany<String, String>();
    private static MapOfMany<String, String>objectNameMapForSimilarColor=new MapOfMany<String, String>(false, false);
    
    static {
    	nameMap.put(JPanel.class, "Panel");
    	nameMap.put(JTable.class,"Table");
    	nameMap.put(JTableHeader.class, "TableHeader");
    	nameMap.put(JButton.class, "Button");
    	nameMap.put(JRadioButton.class, "RadioButton");
    	nameMap.put(JToggleButton.class, "ToggleButton");
    	nameMap.put(JMenu.class,"Menu");
    	nameMap.put(JMenuItem.class,"MenuItem");
    	nameMap.put(JRadioButtonMenuItem.class, "RadioButtonMenuItem");
    	nameMap.put(JCheckBoxMenuItem.class, "CheckBoxMenuItem");
    	nameMap.put(JViewport.class, "Viewport");
    	nameMap.put(JSplitPane.class, "Splitpane");
    	nameMap.put(JTabbedPane.class, "TabbedPane");
    	nameMap.put(JMenuBar.class, "TabbedPane");
    	nameMap.put(JLabel.class, "Label");
    	nameMap.put(JTextField.class, "TextField");
    	nameMap.put(JTextArea.class, "TextArea");
    	nameMap.put(JTextPane.class, "TextPane");
    	nameMap.put(JTree.class, "Tree");
    	nameMap.put(JToolBar.class, "ToolBar");
    	nameMap.put(JToolTip.class, "ToolTip");
    	
    	objectMap=Basics.flipKeyToValue(nameMap, new HashMap<String,Class>());
    			similarObjects.putAll("TetxField", new Class[]{
    			JTextPane.class,
    			JFormattedTextField.class
    	});
    	similarObjects.putAll("Menu", new Class[]{
    			JMenu.class,
    			JMenuItem.class,
    			JCheckBoxMenuItem.class,
    			JRadioButtonMenuItem.class});
    	similarObjects.putAll("Button", new Class[]{
    			JButton.class,
    			JToggleButton.class});
    	similarColor.putAll("Panel.background", new String[]{
    			"Viewport.background", 
    			"SplitPane.background", 
    			"Label.background", 
    			"CheckBox.background", 
    			"MenuBar.background", 
    			"TabbedPane.background",
    			"ToolBar.background"});
    	for (final String key:similarColor.keySet()){
    		final Collection<String> values=similarColor.getCollection(key);
    		for (final String value:values){
    			objectNameMapForSimilarColor.put(toObjectName(key), toObjectName(value));
    		}
    	}
    }

    private static boolean isSimilarObject(final String primaryObjectName, final Class cl){
    	final Collection<Class>clses=similarObjects.getCollection(primaryObjectName);
    	Class found=null;
		for (final Class cls:clses){
			if (cls.isAssignableFrom(cl)){
				if (found==null || 
						/*find LEAST super class */!cls.isAssignableFrom(found)){
					found=cls;
				} 
			}
		}
		return found != null;
    }

    private static String getComponentName(final Class cl){
    	String result=null;
    	final Collection<Class>clses=nameMap.keySet();
    	Class found=null;
		for (final Class cls:clses){
			if (cls.isAssignableFrom(cl)){
				if (found==null || 
						/*find LEAST super class */ !cls.isAssignableFrom(found)){
					found=cls;
					result=nameMap.get(cls);
				}
			}
		}
		return result;
    }
    
    private static boolean isComponentRelevant(final String primaryObjectName, final Component component){
    	final Class componentClass=component.getClass();
    	final String componentObjectName=getComponentName(componentClass);
    	final Class primaryClass=objectMap.get(primaryObjectName);
		if (primaryClass != null) {
	    	boolean ok=false;
			if (primaryClass.isAssignableFrom(componentClass)) {
				ok = true;
			} else if (componentObjectName != null
					&& objectNameMapForSimilarColor.contains(primaryObjectName,
							componentObjectName)) {
				ok = true;
			} else {
				ok = isSimilarObject(primaryObjectName, componentClass);
			}
			return ok;
		} else {
			return false;
		}
    }
    
    static boolean setColor(final String fullPropertyName, final Component component, final Color color){
    	final String primaryObjectName=toObjectName(fullPropertyName);
    	boolean ok=false;
		final Class primaryClass = objectMap.get(primaryObjectName);
		if (primaryClass != null) {
			boolean good=isComponentRelevant(primaryObjectName, component);
	    	final String propertySuffix=toPropertySuffix(fullPropertyName);
	    	final Class componentClass=component.getClass();
	    	final String componentObjectName=getComponentName(componentClass);
	    	if (primaryClass.isAssignableFrom(componentClass)) {
				ok = true;
			} else if (componentObjectName != null
					&& similarColor.contains(fullPropertyName, Basics.concat(
							componentObjectName, ".", propertySuffix))) {
				ok = true;
			} else if (isSimilarObject(primaryObjectName, componentClass)) {
				ok = true;
			} else {
				System.out.println(fullPropertyName + " has no relation to "
						+ componentObjectName + "." + propertySuffix);
			}
			if (ok) {
				setColor(component, propertySuffix, color);
			}
		}
    	return ok;
    }
    
    
    private static void setColor(final Component component, final String propertySuffix,
			final Color color) {
    
    		final String methodName=Basics.concat("set", Basics.capitalize(propertySuffix));
    		java.lang.reflect.Method method=null; 
    		try { 
    		  method = component.getClass().getMethod(methodName, Color.class); 
    		} catch (SecurityException e) { 
				e.printStackTrace();
    		} catch (NoSuchMethodException e) { 
				//e.printStackTrace();
    		} 
    		if (method != null){
    			try { 
    			  method.invoke(component, color); 
    			} catch (IllegalArgumentException e) {
    				e.printStackTrace();
    			} catch (IllegalAccessException e) { 
    				e.printStackTrace();
    			} catch (InvocationTargetException e) { 
    				e.printStackTrace();
    				
    			}
    		}
    	
		
	}

    static ColorPreferences lastOneInstantiated;
    public static JButton getColorButton(final ActionListener al){
    	return getColorButton(al,PersonalizableTable.SET_COLUMN_COLOR);
    }
    public static JButton getColorButton(final ActionListener al, final String text){
		final JButton bColor = SwingBasics.getButton(text, 'c',
				al, "Visually change this color");
		bColor.setActionCommand(PersonalizableTable.SET_COLOR);
		bColor.setIcon(MmsIcons.getColorWheelIcon());
		return bColor;

    }
	public static ColorPreferences instantiate() {
        final String[] std = new String[] {
                             "background", "foreground"};

        final ColorPreferences colorPreferences = new ColorPreferences();
        lastOneInstantiated=colorPreferences;
        colorPreferences.add("Panel", new String[] {"background"});
        colorPreferences.add("Button", new String[] {
                "foreground"});
/* MUST hard code selectionBackground because of html spacing issues on to do*/

        colorPreferences.add("Menu", new String[] {
        		"background", 
        		"foreground",
                "selectionBackground",
                "selectionForeground",
                "disabledBackground",
                "disabledForeground"});
        colorPreferences.add("Label", new String[] {
        		"foreground"});
        colorPreferences.add(WoodsideMenu.CONTEXT, new String[] {"background", "foreground", "disabledForeground"});
        colorPreferences.add(WoodsideMenu.TASK, new String[] {"background", "foreground", "disabledForeground"});
        colorPreferences.add("TableHeader", std);
        colorPreferences.add(
          "Table",
          new String[] {
          PersonalizableTableModel.PROPERTY_SUFFIX_ALTERNATING_COLOR,
          "background",
          "focusCellBackground",
          "focusCellForeground",
          "foreground",
          "gridColor",
          "selectionBackground",
          "selectionForeground"});
        /*        colorProperties.add("ProgressBar",
                                    new String[] {
                          "background",
                          "foreground",
                          "selectionBackground",
                          "selectionForeground"});*/
        colorPreferences.add("TextField",
                             new String[] {
                             "background",
                             "caretForeground",
                             "foreground",
                             "inactiveBackground",
                             "inactiveForeground",
                             "light",
                             "selectionBackground",
                             "selectionForeground",
                             "shadow"
        });
        if (!SwingBasics.isPageSoftLookAndFeel()){
        	colorPreferences.add("ToolTip", std);
        } else {
        	colorPreferences.add("ToolTip", new String[] {
                    "foreground"});
        }
        colorPreferences.add(
          "Tree",
          new String[] {
          "background",
          "foreground",
          "hash",
          
          "selectionBorderColor",
          
          "textBackground",
          "textForeground"

        });
        

        colorPreferences.overrideJavaDefalt("Table", PersonalizableTableModel.PROPERTY_SUFFIX_ALTERNATING_COLOR, PersonalizableTableModel.DEFAULT_ALTERNATING_COLOR);
        colorPreferences.overrideJavaDefalt(WoodsideMenu.TASK, "background", WoodsideMenu.TASK_BACKGROUND_DEFAULT);
        colorPreferences.overrideJavaDefalt(WoodsideMenu.TASK, "foreground", WoodsideMenu.TASK_FOREGROUND_DEFAULT);
        colorPreferences.overrideJavaDefalt(WoodsideMenu.TASK, "disabledForeground", WoodsideMenu.TASK_DISABLED_FOREGROUND_DEFAULT);
        
        colorPreferences.overrideJavaDefalt(WoodsideMenu.CONTEXT, "background", WoodsideMenu.CONTEXT_BACKGROUND_DEFAULT);
        colorPreferences.overrideJavaDefalt(WoodsideMenu.CONTEXT, "foreground", WoodsideMenu.CONTEXT_FOREGROUND_DEFAULT);
        colorPreferences.overrideJavaDefalt(WoodsideMenu.CONTEXT, "disabledForeground", WoodsideMenu.CONTEXT_DISABLED_FOREGROUND_DEFAULT);
        
        colorPreferences.overrideJavaDefalt("Menu", "selectionBackground", new Color(65,90,225));
        colorPreferences.overrideJavaDefalt("ToolTip", "background", new Color(
          228,
          254,
          204
                                            ));
        colorPreferences.overrideJavaDefalt("Panel", "background", new Color(
        	244,244,244
                                            ));
        colorPreferences.overrideJavaDefalt("TableHeader", "background", new Color(191,205,219));

        return colorPreferences;
    }

    private static void setColor(
      final JComponent component,
      final String propertyName,
      final Color color) {
        if (component != null) {
            if (propertyName.endsWith("ackground")) {
                component.setBackground(color);
            } else {
                component.setForeground(color);
            }
        }
    }


    public void add(final String objectName, final String[] propertyNames) {
        colors.add(new ColorProperty(objectName, propertyNames));
    }

    public void overrideJavaDefalt(final String objectName,
                                   final String propertyName,
                                   final Color newDefault) {
        for (final ColorProperty c : colors) {
            if (c.objectName.equals(objectName)) {
                for (int i = 0; i < c.propertyNames.length; i++) {
                    if (c.propertyNames[i].equals(propertyName)) {
                        c.defaultColors[i] = newDefault;
                    }
                }
            }
        }
    }
    
    public ActionListener getColorChooser(
    		final Component parent, 
    		final String objectName,
    		final String property){
    	for (final ColorProperty cp : colors) {
            if (cp.objectName.equals(objectName)){
            	for (int i=0;i<cp.propertyNames.length;i++){
            		if (property.equals(cp.propertyNames[i])){
            			this.parentWindow=parent==null?SwingBasics.mainFrame:SwingUtilities.getWindowAncestor(parent);
            			cp.relevantByOpaque=getRelevantByOpaque(objectName);
            			return cp.getColorChooser(parent, i);
            		}
            	}
            }
        }
    	return null;        
    }
    
    private Component parent;
    private Window parentWindow;
    private JPopupMenu mainColorMenu;
    public JPopupMenu getPopupMenu(final Component parent) {
    	this.parent=parent;
    	this.parentWindow=parent==null?SwingBasics.mainFrame:SwingUtilities.getWindowAncestor(parent);
        mainColorMenu = new JPopupMenu("Color settings");
        for (final ColorProperty cp : colors) {
            mainColorMenu.add(cp.getObjectMenu());
        }
        mainColorMenu.addSeparator();
        getNonDefaults();
        final JMenu defaultMenu=new JMenu("Defaults");
        defaultMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				refreshDefaults(false);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
				refreshDefaults(true);				
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
				refreshDefaults(true);
				
			}
		});
        final JMenuItem mi = new JMenuItem("Restore all");
        defaultMenu.add(mi);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defaultsProposed=true;
                changes.clear();
                beforeChanges.clear();
            }
        });
        mainColorMenu.add(defaultMenu);
        mainColorMenu.add(getExportMenuItem());
        mainColorMenu.add(getImportMenuItem());
        return mainColorMenu;
    }

    boolean defaultsProposed=false;


    public void setCurrentPreferences() {
        for (ColorProperty cp : colors) {
            cp.setCurrentPreferences();
        }
    }

    public void proposeDefaults() {
        for (final ColorProperty cp : colors) {
            cp.proposeDefault();
        }
    }

    void refreshDefaults(final boolean showNewColor) {
        for (final ColorProperty cp : colors) {
            cp.refreshDefault(showNewColor);
        }
    }

    void getNonDefaults() {
        for (final ColorProperty cp : colors) {
            cp.getNonDefault();
        }
    }

    private static Map<String, Color>originalMap=new HashMap<String,Color>();
    private Color getJavaDefault(final String p){
		if (originalMap.containsKey(p)){
            return originalMap.get(p);
        } else {
            final Color c=(Color) UIManager.get(p);
            originalMap.put(p,c);
            return c;
        }	
    }

    private final class ColorProperty {
        private final String objectName;
        private final String[] propertyNames;
        private final Color[] javaDefaultColors;
        private final Color[] defaultColors;

        private ColorProperty(
          final String objectName,
          final String[] propertyNames) {
            this.objectName = objectName;
            final Map<String,Color>m=extensions.get(objectName);
            final int extensionsSize=m==null?0:m.size();
            final int n=propertyNames.length+extensionsSize;
            this.javaDefaultColors = new Color[propertyNames.length+n];
            this.defaultColors = new Color[propertyNames.length+n];
            if (extensionsSize>0){
                this.propertyNames = new String[n];
                int i=0;
                for (;i<propertyNames.length;i++){
                	this.propertyNames[i]=propertyNames[i];
                }
                final Iterator<String>it=m.keySet().iterator();
            	for (;i<n;i++){
            		final String k=it.next();
            		this.propertyNames[i]=k;
            		this.defaultColors[i]=m.get(k);
            	}
            } else {
                this.propertyNames = propertyNames;                
            }
            for (int i = 0; i < propertyNames.length; i++) {
                this.javaDefaultColors[i] = getJavaDefault(getFullPropertyName(i));
            }
        }

        private String getFullPropertyName(final int i) {
            return Basics.concat(objectName, ".", propertyNames[i]);
        }

        private void setCurrentPreferences() {
            for (int i = 0; i < propertyNames.length; i++) {
                setCurrentPreference(i);
            }
        }

        private void proposeDefault() {
            for (int i = 0; i < propertyNames.length; i++) {
                proposeDefault(i);
            }
        }
        
        private void refreshDefault(final boolean showNewColor) {
            for (int i = 0; i < propertyNames.length; i++) {
                refreshDefault(i, showNewColor);
            }
        }
        private void getNonDefault() {
            for (int i = 0; i < propertyNames.length; i++) {
                getNonDefault(i);
            }
        }
        
        int indexOf(final String propertyName){
        	return Basics.indexOf(propertyNames, propertyName);
        }
        
        private Color getDefaultColor(final int i){            
        	Color defaultRgb = Color.white;
            if (defaultColors[i] != null) {
                defaultRgb = defaultColors[i];
            } else {
                if (javaDefaultColors[i] == null) {
                    
                } else {
                    defaultRgb = javaDefaultColors[i];
                }
            }
            return defaultRgb;
        }
        
        private Color getCurrent(final int i){
            return ColorPreferences.this.getCurrent(getFullPropertyName(i), getDefaultColor(i));
        }
        
        private void setCurrentPreference(final int i) {
            if (properties != null) {
                final Color defaultColor = getDefaultColor(i);
                final int defaultRgb=defaultColor.getRGB();
                final String fullPropertyName = getFullPropertyName(i);
                final int rgb = PropertiesBasics.getProperty(properties,
                  fullPropertyName, defaultRgb);
                if (rgb != 0) {
                    if (rgb != defaultRgb){
                        final Color color = new Color(rgb);
                        UIManager.put(fullPropertyName, color);
                        handleSimilarities(fullPropertyName, color);
                    } else {
                        final Color c=(Color)UIManager.get(fullPropertyName);
                        if (c != null && c.getRGB()!= defaultRgb){
                            final Color color = defaultColor;
                            UIManager.put(fullPropertyName, color);
                            handleSimilarities(fullPropertyName, color);
                        }
                    }
                } else {
                    System.out.println("RGB == 0?");
                }

            }
        }

        private void proposeDefault(final int i) {
            final String fullPropertyName = getFullPropertyName(i);
            properties.remove(fullPropertyName);
        }
        
        private Color nonDefaultColor;
        private void getNonDefault(final int i){
        	final String fullPropertyName = getFullPropertyName(i);
            final Color defaultColor=getDefaultColor(i);
            final Color newColor=new Color(PropertiesBasics.getProperty(properties, fullPropertyName, defaultColor.getRGB()));
            if (!Basics.equals(newColor,defaultColor)){
            	nonDefaultColor=newColor;
            }else{
            	nonDefaultColor=null;
            }
        }
        private void refreshDefault(final int i, final boolean showNewColor){
        	final Color defaultColor=getDefaultColor(i);
            if (nonDefaultColor!=null){
            	refresh(getFullPropertyName(i), showNewColor?nonDefaultColor:defaultColor);
            }
        }

        private ActionListener getColorChooser(final Component parent, final int i){
        	final String fullPropertyName = getFullPropertyName(i);
            return new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                	final Color initialColor=getCurrent(i);
                    final Color color = ColorChooser.showDialog(
                      parent,
                      Basics.concat("Color for ", Basics.anglacizePropertyName(fullPropertyName)),
                      initialColor, 
                      getDefaultColor(i),
                      new ColorChooser.Refresher() {
						
						@Override
						public void refresh(final Color color, final boolean andSaveToo) {
							ColorProperty.this.refresh(fullPropertyName, color);
						}
					});
                    if (color==null){
                    	refresh(fullPropertyName, initialColor);
                    }
                    proposeChange(fullPropertyName, color, initialColor);
                }
            };
        }
        final Collection<Component>getRelevant(final String propertyName){
        	final boolean isBackground=propertyName.toLowerCase().endsWith("background");
        	final Collection<Component>all=relevantByOpaque.getCollection(isBackground?"yes":"all");
        	return all;
        }
        
        private void refresh(final String fullPropertyName, final Color color) {
        	final Collection<Component>all=getRelevant(fullPropertyName);
        	for (final Component cmp:all){
        		if (cmp != null){
        			setColor(fullPropertyName, cmp, color);
        		} else {
        			System.out.println();
        		}
        	}
        	ColorPreferences.this.refresh(fullPropertyName, color);
        }

        private void setPropertyMenuItems(final JMenu menu, final int i) {
            menu.removeAll();
            final String fullPropertyName = getFullPropertyName(i);
            final JMenuItem mi = new JMenuItem("Choose");
            mi.setMnemonic('c');
            mi.addActionListener(getColorChooser(parent, i));
            menu.add(mi);
            menu.add(getSystemMenu(fullPropertyName));
            if (defaultColors[i] != null) {
                menu.add(getColorMenuItem("default (factory)", defaultColors[i],
                                          fullPropertyName));
            } else
            if (javaDefaultColors[i] != null) {
                menu.add(getColorMenuItem("default (JAVA)", javaDefaultColors[i],
                                          fullPropertyName));
            }
        }

        private JMenuItem getPropertyMenu(final int i) {
			if (!advanced) {
				final JMenuItem mi = new JMenuItem( Basics.anglacizePropertyName(propertyNames[i]));
				mi.setMnemonic(propertyNames[i].charAt(0));
				mi.addActionListener(getColorChooser(parent, i));
				return mi;
			} else {
				final JMenu menu = new JMenu(propertyNames[i]);
				menu.setMnemonic(propertyNames[i].charAt(0));
				menu.addMenuListener(new MenuListener() {
					final String fullPropertyName = getFullPropertyName(i);
					final Color initialColor = getCurrent(i);

					public void menuSelected(final MenuEvent event) {
						refresh(fullPropertyName, highlightColor);
						setPropertyMenuItems(menu, i);
					}

					public void menuCanceled(MenuEvent event) {
						refresh(fullPropertyName, initialColor);
					}

					public void menuDeselected(MenuEvent event) {
						refresh(fullPropertyName, initialColor);
					}
				});
				return menu;
			}
        }
        private MapOfMany<String, Component>relevantByOpaque=Basics.EMPTY_MAP_OF_MANY;
        private JMenu getObjectMenu() {
        	relevantByOpaque=getRelevantByOpaque(objectName);
            final JMenu menu = new JMenu(Basics.capitalize(Basics.anglacizePropertyName(objectName)));
            menu.setMnemonic(objectName.charAt(0));
            menu.addMenuListener(new MenuListener() {
                final String fullPropertyName;
                final int bg=indexOf("background");
                final int fg=indexOf("foreground");
                
                final Color initialColor, highlightColor;
                {
                	if (bg>=0){
                		initialColor=getCurrent(bg);
                		fullPropertyName= Basics.concat(objectName, ".background");
                		highlightColor=ColorPreferences.this.highlightColor;
                	} else {
                		highlightColor=Color.MAGENTA;
                		initialColor=getCurrent(fg);
                		fullPropertyName= Basics.concat(objectName, ".foreground");
                	}
                }

                public void menuSelected(final MenuEvent event) {
                	
                    menu.removeAll();
                    for (int i = 0; i < propertyNames.length; i++) {
                    	final Collection<Component>c=getRelevant(propertyNames[i]);
                    	if (c.size()==0 && relevantByOpaque.size()>0){
                    		continue;
                    	}
                        menu.add(getPropertyMenu(i));
                    }
                    refresh(fullPropertyName, highlightColor);
                }

                public void menuCanceled(MenuEvent event) {
                	refresh(fullPropertyName,initialColor);
                }

                public void menuDeselected(MenuEvent event) {
                	refresh(fullPropertyName,initialColor);
                }
            }
            );
            return menu;

        }
        public String toString(){
        	return objectName+" "+Basics.toString(this.propertyNames);
        }
    }

    private static Color highlightColor=new Color(244,200,118);
    final JMenuItem getExportMenuItem() {
        final JMenuItem mi = new JMenuItem("Export");
        mi.setIcon(MmsIcons.getExportIcon());
        mi.setMnemonic('e');
        mi.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String f = PopupBasics.getFileName(".properties",
                  "Preference file", false, SoftwareProduct.getDocumentsFolder(), null);
                if (f != null) {
                    PropertiesBasics.saveProperties(
                      properties,
                      f, "Color preferences");
                }
            }
        });
        mi.setToolTipText(Basics.toHtmlUncentered("Export all color preferences to a file"));

        return mi;
    }

    private String importFile;

    final JMenuItem getImportMenuItem() {
        final JMenuItem mi = new JMenuItem("Import");
        mi.setIcon(MmsIcons.getImportIcon());
        mi.setMnemonic('i');
        mi.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String s = PopupBasics.getFileName(".properties",
                  "Preference file", true, SoftwareProduct.getDocumentsFolder(), null);
                if (s != null) {
                    importFile = s;
                }
            }
        });
        mi.setToolTipText(Basics.toHtmlUncentered("Import all color preferences from a file"));
        return mi;
    }

    public static boolean advanced=false;
    public static MapOfMaps<String, String, Color>extensions=new MapOfMaps<String, String, Color>();

    private Color getCurrent(final String fullPropertyName, final Color defaultColor){
    	final int rgb = PropertiesBasics.getProperty(properties,
                fullPropertyName, defaultColor.getRGB());
        return new Color(rgb);
    }
    
    private MapOfMany<String, Component>getRelevantByOpaque(final String objectName){
    	final MapOfMany<String, Component>map=new MapOfMany<String, Component>();
    	final SwingBasics.UnaryFunction uf=new SwingBasics.UnaryFunction() {
			@Override
			public boolean apply(Object o) {
				if (o instanceof Component){
					final Component cmp=(Component)o;
					
					// TODO Auto-generated method stub
					if (isComponentRelevant(objectName, cmp)){
						map.put("all", cmp);
						if (cmp.isOpaque()){
							map.put("yes", cmp);
						}else{
							map.put("no", cmp);
						}
					}
				}
				return false;
			}
		};
		if (parentWindow != null){
			SwingBasics.apply(parentWindow, uf, false);
		}
    	if (parentWindow != SwingBasics.mainFrame && 
    			SwingBasics.mainFrame != null){
    		SwingBasics.apply(SwingBasics.mainFrame, uf, false);
    	}
    	if (mainColorMenu!=null){
    		SwingBasics.apply(mainColorMenu, uf, false);
    	}
    	return map;
    }
}
