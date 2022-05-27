package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import com.MeehanMetaSpace.*;

public class FontPreferences  {
    private SwingBasics.Dialog jd;
    private JPopupMenu pm;
    private JMenu menuFont[];
    private JMenuItem menuSize[][];
    private String fontNames[] = new String[28];
    private final int MENUSIZE = 7;
    private boolean change = false;
    
    private Settings settings;
    private static String propertyFileLocation = System.getProperty("user.home");
    public final static String propertyFileName = "wslFont2.properties";
    
    
    private FontPreferences() {
    	final Collection<String> fc = Basics.getCaseInsensitiveSet();
        // discover additional installed fonts
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final String[] names = ge.getAvailableFontFamilyNames();
        for (int i = 0; i < names.length; i++) {
            final String s = names[i];
            fc.add(s);
        }
        fontNames = fc.toArray(new String[fc.size()]);
        settings=Settings.get();        
    }

    public static void setPropertyFileLocation(String newLocation) {
        propertyFileLocation = newLocation;
    }
    
    private static String getPropertyFilename() {
        return Basics.concat(propertyFileLocation, File.separator, propertyFileName);
    }

    private static Properties loadProperties() {
        final String s = getPropertyFilename();
        if (new File(s).exists()) {
            return PropertiesBasics.loadProperties(s);
        }
        return new Properties();
    }


    public static FontPreferences instantiate() {
        return new FontPreferences();
    }

    void setComplete(final boolean accepted) {
        if (accepted) {
        	if (defaultsProposed) {
        		Settings settings=Settings.getFactoryDefaults();
        		if (settings==null){
        			settings=Settings.lnf;
        		}
            	updateFont(settings.name, settings.size);
            }
        	if (updates>0){
            PopupBasics.alert(
              Basics.toHtmlUncentered("Font changes made...",
                                      "A restart may be necessary before <br>all changes take effect."));
        }
        }
        else {
            updateFont(settings.name, settings.size);
        } 
    }

    private final int[] styles = new int[] {
                                 Font.PLAIN,
                                 Font.BOLD,
                                 Font.ITALIC,
                                 Font.BOLD + Font.ITALIC
    };


    private final String[] styleTxt = new String[] {
                                      "Plain"
    };

	private void setSubMenuItems(final Font f, final int i) {
		pm.add(menuFont[i]);
		for (int l = 0; l < MENUSIZE; l++) {
			final Font styleAndSizeFont = new Font(f.getName(), Font.PLAIN,
					8 + l);
			menuSize[0][l] = new JMenuItem("" + (8 + l) + " point size");
			menuSize[0][l].setFont(styleAndSizeFont);
			menuFont[i].add(menuSize[0][l]);
			menuSize[0][l].addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					updateFont(styleAndSizeFont);
				}
			});
		}
	}

    private int start = 0, end = -1, menuItems = 0;
    private final int MAX_ITEMS_PER_MENU = 20;
    private void setMenuItems() {
        menuItems = fontNames.length - start > MAX_ITEMS_PER_MENU ? MAX_ITEMS_PER_MENU :
                    fontNames.length - start;
    }

    private int computeNextMenuSize() {
        final int i = start + menuItems;
        if (i < fontNames.length) {
            return fontNames.length - i > MAX_ITEMS_PER_MENU ? MAX_ITEMS_PER_MENU :
              fontNames.length - i;
        }
        return 0;
    }

    private void setNextMenuStart() {
        final int n = computeNextMenuSize();
        if (n > 0) {
            start += menuItems;
        }
        setMenuItems();
    }

    private void setStartOfMenu() {
        if (start != 0) {
            start -= MAX_ITEMS_PER_MENU;
            if (start < 0) {
                start = 0;
            }
        }
        setMenuItems();

    }

    private int computePrevMenuSize() {
        if (start - MAX_ITEMS_PER_MENU < 0) {
            return start;
        }
        return MAX_ITEMS_PER_MENU;
    }

    final ActionListener prevMenu = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            setStartOfMenu();
            setPopupMenu();
        }
    };
    final ActionListener nextMenu = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
            setNextMenuStart();
            setPopupMenu();
        }
    };
    private JComponent component;
    JPopupMenu showPopupMenu(final JComponent component) {
        this.component=component;
        pm = new JPopupMenu("Font settings");
        setStartOfMenu();
        setPopupMenu();
        return pm;
    }

    private String getFontStyleName(final int fontStyleNumber) {
    	String fontStyle = null;
        if (fontStyleNumber == Font.BOLD) {
        	fontStyle = "Bold";
        }
        else if (fontStyleNumber == Font.PLAIN) {
        	fontStyle = "Plain";
        }
        else if (fontStyleNumber == Font.ITALIC) {
        	fontStyle = "Italic";
        }
        else {
        	fontStyle = "Bold & Italic";
        }
        return fontStyle;
    }
    
    private void setPopupMenu() {
        pm.removeAll();
        int n = computePrevMenuSize();
        if (n > 0) {
            final JMenuItem prev = new JMenuItem("Previous " + n + " fonts");
            prev.setIcon(MmsIcons.getUpIcon());
            prev.addActionListener(prevMenu);
            pm.add(prev);
            pm.addSeparator();
        }
        menuFont = new JMenu[menuItems];
        menuSize = new JMenuItem[styles.length][MENUSIZE];
        int iFont = start;
        final Settings factoryDefaults=Settings.getFactoryDefaults();
        for (int i = 0; i < menuItems; i++, iFont++) {
            menuFont[i] = new JMenu();
            menuFont[i].setText(fontNames[iFont]);
            final Font f = menuFont[i].getFont();
            final Font nf = new Font(fontNames[iFont], f.getStyle(), f.getSize());
            menuFont[i].setFont(nf);
            menuFont[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            String fontName = nf.getName();
            if (fontName.indexOf('.') != -1)
                fontName = fontName.substring(0, fontName.indexOf('.'));
            if (factoryDefaults !=null && fontName.equals(factoryDefaults.name))
                menuFont[i].setIcon(MmsIcons.getFontSelectedIcon());
            else
                menuFont[i].setIcon(MmsIcons.getBlankIcon());
            setSubMenuItems(nf, i);
        }
        pm.addSeparator();
        JMenuItem mi = null;
        if (factoryDefaults!=null){ 
        	mi=new JMenuItem(Basics.concatObjects("Wsl recommendation:  ", factoryDefaults.name, ", plain, ", factoryDefaults.size));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont(factoryDefaults.name, factoryDefaults.size);
            }
        });
        pm.add(mi);
        }
       mi = new JMenuItem(Basics.concatObjects(LNF_NAME, ":  ", LNF_FONT_NAME, ", plain, ", LNF_FONT_SIZE));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont(LNF_FONT_NAME, LNF_FONT_SIZE);
            }
        });
        pm.add(mi);
        final Font f=new JLabel().getFont();
        mi = new JMenuItem(Basics.concatObjects("Current:  ", f.getName(), ",  " + getFontStyleName(f.getStyle()) + " , ", f.getSize()));
        pm.add(mi);
        pm.addSeparator();
        JMenuItem mi2 = new JMenuItem("Restore defaults");
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defaultsProposed=true;;
            }
        });
        pm.add(mi2);
        n = computeNextMenuSize();
        if (n > 0) {
            final JMenuItem next = new JMenuItem("Next " + n + " fonts");
            next.setIcon(MmsIcons.getDownIcon());
            next.addActionListener(nextMenu);
            pm.add(next);
        }
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {										
				 pm.show(component,15,15);				
			}
		});
       
    }
    private final static class Settings{
    	final String name;
    	final int size;
    	public String toString(){
    		return name+", "+size;
    	}
     	private Settings(final String name, final int size){
    		this.name=name;
    		this.size=size;
    		this.properties=null;
    	}
    	private final Properties properties;
    	private Settings(final String name, final String size, final Properties properties){
    		this.name=name;
    		this.size=Basics.isEmpty(size)?12:Integer.parseInt(size);
    		this.properties=properties;
    	}
    	private Settings(final String name, final String size){
    		this(name,size,null);
    	}
    	
    	static Settings getFactoryDefaults(){
    		final Class cl=UIManager.getLookAndFeel().getClass();
    		return defaultsByLnF.get(cl);
    	}
    	
    	private static String getNameProperty(){
    		final Class cl=UIManager.getLookAndFeel().getClass();
    		return Basics.concat(cl.getName(),".",_PROPERTY_FONT_NAME);
    	}
    	
    	private static String getSizeProperty(){
    		final Class cl=UIManager.getLookAndFeel().getClass();
    		return Basics.concat(cl.getName(),".",_PROPERTY_FONT_SIZE);
    	}
    	
    	private static Settings getUserDefaults(){
    		final Properties properties=loadProperties();
    		final String name=properties.getProperty(getNameProperty());
    		final Settings dflts;
    		if (Basics.isEmpty(name)){
    			dflts=null;
    		} else {
    			final String size=properties.getProperty(getSizeProperty(),""+lnf.size);
    			dflts=new Settings(name, size, properties);
    		}
    		return dflts;
    	}
    	
    	static Settings lnf;
    	static Settings get(){    		
        	lnf=new Settings(LNF_FONT_NAME, LNF_FONT_SIZE);
    		Settings dflts=getUserDefaults();
    		if (dflts==null){
    			dflts=getFactoryDefaults();
    			if (dflts != null){
    				return dflts;
    			}
    		}
    		if (dflts==null){
    			dflts=lnf;
        		
    		}
    		return dflts;
    	}
        private static final Map<Class, Settings> defaultsByLnF=new HashMap<Class, Settings>();
        
        
        static {
        	defaultsByLnF.put(com.jgoodies.plaf.plastic.Plastic3DLookAndFeel.class, new Settings("Sans serif", 13));
        }
        
    };
    
    
    
    private static String LNF_NAME=null, LNF_FONT_NAME=null;
    private static int LNF_FONT_SIZE;
    private boolean defaultsProposed=false;
    private static LookAndFeel LNF;
    public static void updateLnf(){
    	LNF=UIManager.getLookAndFeel();
    	LNF_NAME=LNF.getName();
    	final Font label=new JLabel().getFont();    	
    	LNF_FONT_NAME=label.getName();
    	LNF_FONT_SIZE=label.getSize();
		Settings settings=Settings.get();
		if (!settings.name.equals(LNF_FONT_NAME)) {
			updateFont(settings.name, settings.size, null, settings.properties);			
		} else {
			SwingBasics.resetDefaultFonts();
			PersonalizableTable.resetDefaultFonts();
		}
    }
    private final static String _PROPERTY_FONT_NAME = "FONT_NAME",
    	_PROPERTY_FONT_SIZE = "FONT_SIZE";

    
    private void updateFont(final Font newFont) {
        updateFont(newFont.getName(), newFont.getSize());
    }
    private void updateFont(
      final String name,
      final int size) {
    	updates++;
        change=updateFont(name,size,jd,loadProperties());
    }

    private static boolean updateFont(final String name,
			final int size, final SwingBasics.Dialog dialog,
			final Properties properties) {
    	if (Basics.isEmpty(name) || properties==null){
    		return false;
    	}
		Font newFont = null;
		final GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		final String[] names = ge.getAvailableFontFamilyNames();
		
		
		
		try {

		

				properties.setProperty(Settings.getNameProperty(), name);
				properties.setProperty(Settings.getSizeProperty(), "" + size);
				newFont = new Font(name, Font.PLAIN, size);				
				PropertiesBasics.saveProperties(properties, getPropertyFilename(),
				"fontProperties");

			UIDefaults table = UIManager.getDefaults();
			Enumeration eKeys = table.keys();
			while (eKeys.hasMoreElements()) {
				Object obj = eKeys.nextElement();
				String key = obj.toString();
				if (key.indexOf("font") != -1) {
					UIManager.put(key, newFont);
				}
			}
			if (dialog != null) {
				dialog.repaint();
				dialog.validate();
			}
			SwingBasics.resetDefaultFonts();
			PersonalizableTable.resetDefaultFonts();
			return true;
		} catch (final Exception e) {
			e.printStackTrace(System.err);
		}
		return false;
	}
    private int updates;
    void setDialog(final SwingBasics.Dialog jd) {
        this.jd = jd;
        updates=0;
    }
	public static void setCurrentPreferences() {
		setCurrentPreferences(null,null);
	}

    
    /**
	 * Set current property preferences.
	 * 
	 * @param resetPropertyValue
	 * @return TRUE if font preferences were reset because of a specific build.
	 * 
	 */
	public static boolean setCurrentPreferences(final String resetPropertyName, final String resetPropertyValue) {
		final Properties properties = loadProperties();
		if (properties != null) {
			if (!Basics.isEmpty(resetPropertyName) && !Basics.isEmpty(resetPropertyValue)) {
				final String b = properties.getProperty(resetPropertyName);
				if (!Basics.equals(b, resetPropertyValue)) {
					properties.setProperty(resetPropertyName, resetPropertyValue);
					SwingBasics.resetDefaultFonts();

					return true;
				}
			}
			final Settings settings=Settings.get();
			updateFont(
					settings.name, 
					settings.size, 
					null, 
					properties);
		}
		return false;
	}
	
}
