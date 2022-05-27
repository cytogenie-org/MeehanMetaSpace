package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JScrollPane;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.MacintoshBasics;
//import com.MeehanMetaSpace.PropertiesBasics;
import com.MeehanMetaSpace.PropertiesBasics;

public class TabBrowser extends JPanel {
	public PersonalizableTableModel tableModel;
	public PersonalizableTable table;

	private String propertyPrefix;
	private Properties properties;

	/**
	 * This constructor makes the TabBrowser instance ready to run like the
	 * collection baskets that behave identically. A collection basket is a place for the user
	 * to remember picks for seperate purposes.  One purpose may be buying or shopping.  Another may
	 * be importing 
	 * 
	 * @param td
	 * 
	 * @param columnIdentifiersToBeTreatedLikeUrls
	 * @param propertyPrefix
	 * @param collectionBaskets
	 */
	
	private static String TABLE_CLASS_NAME="TabBrowser";
	
	final ArrayList<CollectionBasketHelper> cbs=new ArrayList<CollectionBasketHelper>();	
	private final boolean askToCheckout;
	private TabBrowser(final TabDataSource tabDataSource,
			final Collection<String> columnIdentifiersToBeTreatedLikeUrls,
			final String propertyPrefix,
			final Properties properties,
			final Collection<CollectionBasket> collectionBaskets,
			final JPanel customPanelAtToolBarEast,
			final Window window,
			final boolean autoImport, final ToolBarPlugin toolBarPlugin,
			final List<TabFilter> filters,
			final int collectionBasketHelperStyle,
			final boolean askToCheckout,
			final TabBrowser.PreBrowseListener preBrowseListener, 
			final TabBrowser.PostBrowseListener postBrowseListener) {
		this.filters = filters;	
		this.askToCheckout=askToCheckout;
		this.parent = window;
		this.preBrowseListener = preBrowseListener;
		this.postBrowseListener= postBrowseListener;
		if (window != null) {
			window.addWindowListener(new WindowAdapter() {
				public void windowClosing(final WindowEvent e) {
					saveCollectionBasket();
					dispose();
					if (postBrowseListener != null) {
						postBrowseListener.browseComplete(false, null);
					}
				}
			});
		}
		initialize(tabDataSource, propertyPrefix, properties, window, autoImport);
		if (collectionBaskets != null) {
			for (final CollectionBasket sc : collectionBaskets) {
				objectName=sc.getItemName();
				final CollectionBasketHelper collectionBasketHelper;
				if (collectionBasketHelperStyle == 0) {
					collectionBasketHelper = new LessInputExpandedOutputCollectionBasketHelper(window, sc,
							tableModel, toolBarPlugin, propertyPrefix, askToCheckout);		
				}
				else {				
					collectionBasketHelper = new MoreInputReducedOutputCollectionBasketHelper(sc,
							tableModel, askToCheckout);
				}			 
				cbs.add(collectionBasketHelper);
			}
		}
		tableModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {
				int[] selected = tableModel.getModelShowing().table.getSelectedRows();
				final int n = tableModel.getModelShowing().getSelectedDataRowCount(selected);
				for (final CollectionBasketHelper helper: cbs) {
					final String addToBasketText = helper.getAddToBasketButtonPrefixText();
					final String addToBasketSuffixText = helper.getAddToBasketButtonSuffixText();
					JButton addToBasket = helper.getAddToBasketButton();
					if (n > 0) {		
						addToBasket.setEnabled(true);
						if (addToBasketText != null && !preventLaunch) {
							addToBasket.setText(Basics.concat(addToBasketText, " ", Basics.encode(n),  " " , objectName, (n>1?"s":""), addToBasketSuffixText));
						}
					}
					else {
						addToBasket.setEnabled(false);
						if (addToBasketText != null && !preventLaunch) {
							addToBasket.setText(addToBasketText + addToBasketSuffixText);			
						}
					}
				}					
			}
		});
		
		final JPanel jp=new JPanel(new BorderLayout());
		//jp.add(buttonPanel, BorderLayout.WEST);
		if (customPanelAtToolBarEast != null) {
			if (collectionBasketHelperStyle == -1 && preventLaunch) {
				jp.add(customPanelAtToolBarEast, BorderLayout.SOUTH);	
			}
			else {
				jp.add(customPanelAtToolBarEast, BorderLayout.EAST);
			}
						
		}
		if (toolBarPlugin != null) {
			add(toolBarPlugin.addToolBar("", tableModel, jp));
		} else {
			add(jp, BorderLayout.NORTH);			
		}
	}
	private TabBrowser(final TabDataSource tabDataSource, final String propertyPrefix,
			final Properties properties, final Window window) {
		this.filters = null;
		this.askToCheckout = false;
		this.parent = window;
		this.alreadyImported = true;
		this.readOnly = false;
		initialize(tabDataSource, propertyPrefix, properties, window, false);
	}
	
	private static String objectName;
	private TabBrowser(final TabDataSource tabDataSource,
			final Collection<String> columnIdentifiersToBeTreatedLikeUrls,
			final String propertyPrefix,
			final Properties properties,
			final CollectionBasket.SourceAction sourceAction,
			final JButton customButton,
			final CollectionBasket.UploadAction uploadAction,
			final Window window,
			final boolean autoImport, 
			final String singularNoun,
			final String pluralNoun, 
			final ToolBarPlugin toolBarPlugin,
			final List<TabFilter> filters,
			final JPanel topPanel) {		
		askToCheckout=true;
		this.isTOC = true;
		this.sourceAction = sourceAction;
		this.filters = filters;
		this.parent = window;
		initialize(tabDataSource, propertyPrefix, properties, window, autoImport);
		final JPanel buttonPanel = new JPanel(new BorderLayout());
		final JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		open = SwingBasics.getButton(OPEN_DEFAULT_TEXT, MmsIcons.getOpenIcon(), 'o',new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                	sourceAction.getSource(tableModel.getModelShowing());
                }
            }, Basics.concat("Open selected ", objectName, "(s)"));
		open.setEnabled(false);
		westPanel.add(open);
		if (customButton != null) {
			westPanel.add(customButton);			
			customButton.setVisible(false);
		}
		if (uploadAction != null) {
			upload = SwingBasics.getButton(UPLOAD_CATALOG+" "+singularNoun, MmsIcons
					.getExportIcon(), 'u', new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					uploadAction.putSource();
				}
			}, Basics.concat("UpLoad selected ", objectName, "(s)"));
			westPanel.add(upload);
		}
		buttonPanel.add(westPanel,BorderLayout.WEST);
		
		JLabel lbl = new JLabel("<html><center><b><font size=5>" +
				"Table of contents</font></b><br></center></html>");
		southPanel.add(lbl);
		buttonPanel.add(southPanel,BorderLayout.SOUTH);
		
		if (topPanel != null) {
			buttonPanel.add(topPanel, BorderLayout.CENTER);			
		}

		if (window instanceof RootPaneContainer){
			( (RootPaneContainer)window).getRootPane().setDefaultButton(open);
		} 

		tableModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {
				
				int[] selected = tableModel.getModelShowing().table.getSelectedRows();
				final int n = tableModel.getModelShowing().getSelectedDataRowCount(selected);
				
				final String verb;
				

				if (n > 0) {
					if (n>1){
						verb=pluralNoun;
					}else{
						verb=singularNoun;
					}
					open.setEnabled(true);
					open.setText(OPEN_DEFAULT_TEXT +" "+ n +" "+verb);
				}
				else {
					open.setText(OPEN_DEFAULT_TEXT);
					open.setEnabled(false);
				}
			}
		});
		if (toolBarPlugin != null) {
			add(toolBarPlugin.addToolBar("", tableModel, buttonPanel));
		}
		else {
			final JPanel tabBrowserPanel = new JPanel(new BorderLayout());
			if (!preventLaunch) {
				tabBrowserPanel.add(buttonPanel, BorderLayout.NORTH);
			}
			tabBrowserPanel.add(tableModel.getTable().makeHorizontalScrollPane(), BorderLayout.CENTER);
			add(tabBrowserPanel, BorderLayout.NORTH);			
		}
	}
	
	boolean alreadyImported = false;
	boolean readOnly = true;
	private void initialize(final TabDataSource tabDataSource, final String _propertyPrefix, final Properties properties, 
			final Window window, final boolean autoImport) {
		tabDataSource.tbs.add(this);
		setLayout(new BorderLayout());
		this.propertyPrefix = _propertyPrefix;
		this.properties=properties;
		
		if (_propertyPrefix!=null){
			tabDataSource.setPropertyPrefix(_propertyPrefix);
		}
		tableModel = PersonalizableTableModel.activate(tabDataSource, properties, readOnly, true);
		tableModel.setAutoFilter(true);		
 		if (SwingUtilities.isEventDispatchThread()) {
 			TabImporter.setEventDispatchThread(true); 			
 		}
 		if (progressShower!=null && !alreadyImported){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Runnable r=new Runnable() {
						public void run() {
					 		final Collection<File> files = new ArrayList<File>();
					 		files.add(tabDataSource.getTabDataSourceFile());
							TabImporter.runFiles(files, tableModel.getModelShowing(), window, autoImport, filters);
							progressShower.afterData();
						}
					};
					new Thread(r).start();
				}
			});
			progressShower.beforeData();
		} else if (!alreadyImported){
	 		final Collection<File> files = new ArrayList<File>();
	 		files.add(tabDataSource.getTabDataSourceFile());
			TabImporter.runFiles(files, tableModel.getModelShowing(), window, autoImport, filters);
		}
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		table = new PersonalizableTable(tableModel);
		if (!readOnly) {
			tableModel.setEditInPlace(true);    
			tableModel.canEditCellForMultipleRows = true;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!readOnly) {
					tableModel.getModelShowing().setEditInPlace(true);    
					tableModel.canEditCellForMultipleRows = true;
				}
			}
		});
		add(table.makeHorizontalScrollPane(), BorderLayout.CENTER);			
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton saveBtn = SwingBasics.getButton("Save", MmsIcons.getSaveIcon(), 's', new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i=0; i<tabDataSource.allLines.size(); i++) {
					System.out.println(tabDataSource.allLines.get(i));
				}
			}
		}, "Save changes");
		buttonPanel.add(saveBtn);
		if (!preventLaunch) {
			add(buttonPanel, BorderLayout.SOUTH);
		}
	}
	
	public void saveCollectionBasket() {
		if(!Basics.isEmpty(cbs)){
			for(CollectionBasketHelper cb : cbs){
				if (cb instanceof LessInputExpandedOutputCollectionBasketHelper) {
					LessInputExpandedOutputCollectionBasketHelper less = (LessInputExpandedOutputCollectionBasketHelper) cb;
					less.saveCollectionBasket();
				}
			}
		}
		return;
	}
	
	public void dispose() {
		for (CollectionBasketHelper cb: cbs) {
			cb.dispose();
		}		
	}
	
	public boolean hasAtleastOneItemInBasket() {
		for (CollectionBasketHelper cb: cbs) {
			if (cb.getBasketCount() > 0) {
				return true;				
			}
		}
		return false;
	}
	
	boolean handleDoubleClick(){
		
		if (isSinglePick && !isTOC) {
			for (final CollectionBasketHelper cbh:cbs){
				if (cbh.getCollectionBasket().isDoubleClickObedient()){
					cbh.doubleClick();
					pickPerformed();
				}
			}
			return false;
		}
		
		boolean doubleClickIgnored=true;
		if (!isTOC) {			
			for (final CollectionBasketHelper cbh:cbs){
				if (cbh.isVisible() && cbh.getCollectionBasket().isDoubleClickObedient()){
					cbh.doubleClick();
					doubleClickIgnored=false;
				}
			}
		}
		else {
			open.doClick();
		}
		if (doubleClickIgnored &&!isTOC  && cbs.size()>0){
			for (final CollectionBasketHelper cbh:cbs){
					if (!cbh.isVisible() && cbh.getCollectionBasket().isDoubleClickObedient()){
				final JButton b=cbh.getBasketButton();
						doubleClickIgnored=false;
						b.doClick();
						cbh.doubleClick();
			}
		}
		}
		
		
		return false;
	}

	
	private boolean isTOC = false;
	private JButton open,upload;
	private final String OPEN_DEFAULT_TEXT = "Open";
	private final String UPLOAD_CATALOG="Upload";
	private CollectionBasket.SourceAction sourceAction;
	private ToolBarPlugin toolBarPlugin;
	final List<TabFilter> filters;
	
	public static TabBrowser showTOC(final JFrame frame, final File file,
			final CollectionBasket.SourceAction action,final JButton customButton, final CollectionBasket.UploadAction uploadAction, final String propertyPrefix, 
			final Properties properties, final String singularNoun, final String pluralNoun, 
			final ToolBarPlugin toolBarPlugin, final boolean launch, 
			final List<TabFilter> filters, final JPanel topPanel,
			final List<String> urlColumnNames,
			final List<String> integerColumnNames) {

		final TabDataSource tabDataSource = new TabDataSource(Basics.toList(file), filters, urlColumnNames, integerColumnNames, null);		
		final TabBrowser tabBrowser = new TabBrowser(tabDataSource, null,
				propertyPrefix, properties, action,customButton, uploadAction, frame, true, singularNoun, pluralNoun, toolBarPlugin, filters, topPanel);
		
		if (launch) {
			launchTabBrowser(frame, tabBrowser, "records");						
		}
		
		return tabBrowser;
	}
	
	private static TabDataSource tabDataSource=null; 
	
	private static Properties getProperties(final File tableProperties) {
		Properties props = new Properties();
		FileReader fr = null;
		try {
			fr=new FileReader(tableProperties);
			props.load(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return props;
	}
	
	private static File createTabDataFile(final String tabData) {
		String mmsSwingFolder = System.getProperty("user.home") + File.separator + ".mmsSwing" + File.separator + "tablePrefs";
		IoBasics.mkDirs(mmsSwingFolder);
		String tempFilePath = mmsSwingFolder + File.separator + "tempTab1.txt";
		File tempFile = new File(tempFilePath);
		IoBasics.saveTextFile(tempFile, tabData);
		return tempFile;
	}
	
	private static File createTabPropsFile(final String tabData) {
		String mmsSwingFolder = System.getProperty("user.home") + File.separator + ".mmsSwing" + File.separator + "tablePrefs";
		IoBasics.mkDirs(mmsSwingFolder);
		String tempFilePath = mmsSwingFolder + File.separator + "tempTabProps1.properties";
		File tempFile = new File(tempFilePath);
		IoBasics.saveTextFile(tempFile, tabData);
		return tempFile;
	}
	
	private static File replaceWithRunTimeProperties(File designTimeProps) {
		String mmsSwingFolder = System.getProperty("user.home") + File.separator + ".mmsSwing" + File.separator + "tablePrefs";
		IoBasics.mkDirs(mmsSwingFolder);
		String runTimePropsPath = mmsSwingFolder + File.separator + designTimeProps.getName();
		File runTimeProps = new File(runTimePropsPath);
		if (!runTimeProps.exists()) {
			try {
				final String contents = IoBasics
						.readStringAndClose(new BufferedReader(
								new FileReader(designTimeProps)));
				IoBasics.saveTextFile(runTimeProps, contents);
			} catch (final Exception e3) {
				e3.printStackTrace();
			}
		}
		return runTimeProps;
	}

	public static void testTreeTable() {
	
		String tabData = "NAME\tEMAIL\tAGE\tDOB\tWEIGHT\nJohn\txyz@ab.com\t38\t01/01/1976\t72.5";
		
		ArrayList<Class> dataTypes = new ArrayList<Class>();
		dataTypes.add(String.class);
		dataTypes.add(URL.class);
		dataTypes.add(Integer.class);
		dataTypes.add(Date.class);
		dataTypes.add(Float.class);
		
		ArrayList<Integer> readOnlyColumns = new ArrayList<Integer>();
		readOnlyColumns.add(0);
		
		TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues = new TreeMap<Integer, TreeMap<Integer, List<Object>>>();
		List<Object> emailSet1 = new ArrayList<Object>();
		emailSet1.add("xyz@ab.com");
		emailSet1.add("abc@xy.com");
		TreeMap<Integer, List<Object>> emails = new TreeMap<Integer, List<Object>>();
		emails.put(0, emailSet1);
		allowedValues.put(1, emails);
		
		String tableProps = "tableColumnModel=NAME&EMAIL&AGE&DOB&WEIGHT&";
		
		getInstance(tabData, tableProps, 
				true, dataTypes, readOnlyColumns, allowedValues);//new File(System.getProperty("user.home"), "tabBrowserPrefs.properties")
		
	}
	
	public static TabBrowser getInstance(final String tabData, final String tableProperties, boolean show,
			ArrayList<Class> dataTypes, ArrayList<Integer> readOnlyColumns, 
			TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues) {
		return getInstance(createTabDataFile(tabData), createTabPropsFile(tableProperties), show, dataTypes, readOnlyColumns, allowedValues);
	}
	
	public static TabBrowser getInstance(final File tabData, final String tableProperties, boolean show, 
			ArrayList<Class> dataTypes, ArrayList<Integer> readOnlyColumns, 
			TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues) {
		return getInstance(tabData, createTabPropsFile(tableProperties), show, dataTypes, readOnlyColumns, allowedValues);
	}
	
	public static TabBrowser getInstance(final String tabData, final File tableProperties, boolean show, 
			ArrayList<Class> dataTypes, ArrayList<Integer> readOnlyColumns, 
			TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues) {
		return getInstance(createTabDataFile(tabData), tableProperties, show, dataTypes, readOnlyColumns, allowedValues);
	}
	
	public static TabBrowser getInstance(final File tabData, final File designTimeProps, boolean show, 
			ArrayList<Class> dataTypes, ArrayList<Integer> readOnlyColumns, 
			TreeMap<Integer, TreeMap<Integer, List<Object>>> allowedValues) {
		Collection<File> files = new ArrayList();
		files.add(tabData);
		
		TabDataSource tabDataSource;
		try {
			tabDataSource = new TabDataSource(files, dataTypes, readOnlyColumns, allowedValues);
			final File runTimeProps = replaceWithRunTimeProperties(designTimeProps);
			Properties p =  getProperties(runTimeProps);
			final TabBrowser tabBrowser = new TabBrowser(tabDataSource, null, getProperties(runTimeProps), null);
			
			tabBrowser.setTreeTableProperties(runTimeProps);
			if (show) {
				final JDialog dlg = new JDialog();
				dlg.getContentPane().add(tabBrowser);
				dlg.setModal(true);
				SwingBasics.packAndPersonalize(dlg, "tabBrowser", true);
				SwingBasics.closeOnEscape(dlg);
				tabBrowser.tableModel.setPersonalizableWindowOwner((Window) dlg);
				dlg.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						tabBrowser.saveTreeTablePersonalizations();
					}
				});
				dlg.setVisible(true);
				return tabBrowser;
			}
			return tabBrowser;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	

	public static TabBrowser getInstance(final String tabData, final File tableProperties, boolean show) {
		return getInstance(createTabDataFile(tabData), tableProperties, show);
	}
	
	public static TabBrowser getInstance(final File tabData, final File designTimeProps, boolean show) {
		Collection<File> files = new ArrayList();
		files.add(tabData);
		TabDataSource tabDataSource = new TabDataSource(files, (List<TabFilter>)null, null, null, null);
		final File runTimeProps = replaceWithRunTimeProperties(designTimeProps);
		final TabBrowser tabBrowser = new TabBrowser(tabDataSource, null, null, getProperties(runTimeProps), null, null, 
				null, true, null, null, 0, false, null, null);
		tabBrowser.setTreeTableProperties(runTimeProps);
		if (show) {
			final JDialog dlg = new JDialog();
			dlg.getContentPane().add(tabBrowser);
			dlg.setModal(true);
			SwingBasics.packAndPersonalize(dlg, "tabBrowser", true);
			SwingBasics.closeOnEscape(dlg);
			tabBrowser.tableModel.setPersonalizableWindowOwner((Window) dlg);
			dlg.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					tabBrowser.saveTreeTablePersonalizations();
				}
			});
			dlg.setVisible(true);
		}
		return tabBrowser;
	}
	
	public PersonalizableTable getTreeTable() {
		return table;
	}
	
	File runTimeProps;
	
	private void setTreeTableProperties(File runTimeProps) {
		this.runTimeProps =runTimeProps;
	}
	
	public void saveTreeTablePersonalizations() {
		tableModel.saveProperties(runTimeProps.getAbsolutePath());
	}
	
	public static TabBrowser show(final JFrame frame, final Collection<File> files,
			final Collection<CollectionBasket> collectionBaskets,
			final JPanel customPanelAtToolBarEast,
			final String propertyPrefix,
			final Properties properties, final ToolBarPlugin toolBarPlugin, 
			final List<ComputedColumn>computedColumnsPlugin, 
			final List<TabFilter> filters,
			final List<String> urlColumnNames,
			final List<String> integerColumnNames,
			final Map<String,Object> additionalColumns,
			final int collectionBasketHelperStyle,
			final Map<String,SortValueReinterpreter> sortValueReinterpreterMap,
			final Map<String, Row.IconGetter> iconMap,
			final boolean askToImport,
			final TabBrowser.PreBrowseListener preBrowseListener, 
			final TabBrowser.PostBrowseListener postBrowseListener,
			final String noun) {
		if (progressShower!=null){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final Runnable r=new Runnable() {
						public void run() {
							if (computedColumnsPlugin != null) {
								tabDataSource = new TabDataSource(files, filters, computedColumnsPlugin, urlColumnNames, integerColumnNames, additionalColumns, sortValueReinterpreterMap,iconMap);
							}
							else {
								tabDataSource = new TabDataSource(files, filters, urlColumnNames, integerColumnNames, additionalColumns);
							}
							progressShower.afterMetaData();
						}
					};
					new Thread(r).start();
				}
			});
			progressShower.beforeMetaData();

		} else {
			if (computedColumnsPlugin != null && !computedColumnsPlugin.isEmpty()) {
				tabDataSource = new TabDataSource(files, filters, computedColumnsPlugin, urlColumnNames, integerColumnNames, additionalColumns, sortValueReinterpreterMap,iconMap);
			}
			else {
				tabDataSource = new TabDataSource(files, filters, urlColumnNames, integerColumnNames, additionalColumns);
			}

		}

		

		final TabBrowser tabBrowser = new TabBrowser(tabDataSource, null,
				propertyPrefix, properties, collectionBaskets, customPanelAtToolBarEast, frame, true, 
				toolBarPlugin, filters, collectionBasketHelperStyle, askToImport, preBrowseListener, postBrowseListener);
		
		if (!preventLaunch) {
			launchTabBrowser(frame, tabBrowser, noun);
		}
		
		tabBrowser.tableModel.getTable().setAnticipateHtml(true);
		return tabBrowser;

	}
	
	public static TabBrowser show(
			final JFrame frame, 
			final String windowTag,
			final Collection<File> files,
			final Collection<CollectionBasket> collectionBaskets,
			final JPanel customPanelAtToolBarEast,
			final String propertyPrefix,
			final Properties properties, final ToolBarPlugin toolBarPlugin, 
			final List<ComputedColumn>computedColumnsPlugin, 
			final List<TabFilter> filters,
			final List<String> urlColumnNames,
			final List<String> integerColumnNames,
			final Map<String,Object> additionalColumns,
			final int collectionBasketHelperStyle,
			final Map<String,SortValueReinterpreter> sortValueReinterpreterMap,
			final Map<String, Row.IconGetter> iconMap,
			final boolean askToImport,
			final TabBrowser.PreBrowseListener preBrowseListener, 
			final TabBrowser.PostBrowseListener postBrowseListener,
			final Properties columnTypesByName) {
		if (progressShower!=null){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final Runnable r=new Runnable() {
						public void run() {
							if (computedColumnsPlugin != null) {
								tabDataSource = new TabDataSource(files, filters, computedColumnsPlugin, urlColumnNames, integerColumnNames, additionalColumns, sortValueReinterpreterMap,iconMap);
							}
							else {
								tabDataSource = new TabDataSource(files, filters, urlColumnNames, integerColumnNames, additionalColumns);
							}
							progressShower.afterMetaData();
						}
					};
					new Thread(r).start();
				}
			});
			progressShower.beforeMetaData();

		} else {
			if (computedColumnsPlugin != null) {
				tabDataSource = new TabDataSource(files, filters, computedColumnsPlugin, urlColumnNames, integerColumnNames, additionalColumns, sortValueReinterpreterMap,iconMap);
			}
			else {
				tabDataSource = new TabDataSource(files, filters, urlColumnNames, integerColumnNames, additionalColumns);
			}

		}
		if (columnTypesByName != null && columnTypesByName.size()>0) {
			tabDataSource.dataTypes=PersonalizableTableModel.getDataTypes(
					columnTypesByName, 
					tabDataSource.columns);
		}
		

		final TabBrowser tabBrowser = new TabBrowser(tabDataSource, null,
				propertyPrefix, properties, collectionBaskets, customPanelAtToolBarEast, frame, true, 
				toolBarPlugin, filters, collectionBasketHelperStyle, askToImport, preBrowseListener, postBrowseListener);
		
		launchTabBrowser(frame, tabBrowser, windowTag);
		tabBrowser.tableModel.getTable().setAnticipateHtml(true);
		return tabBrowser;

	}
	
	
	public static boolean preventLaunch = false;
	
	JPanel parentPanel;
	
	public static TabBrowser show(final JFrame frame, final JPanel parent, final Collection<File> files,
			final Collection<CollectionBasket> collectionBaskets,
			final JPanel customPanelAtToolBarEast,
			final String propertyPrefix,
			final Properties properties, final ToolBarPlugin toolBarPlugin, 
			final List<ComputedColumn>computedColumnsPlugin, 
			final List<TabFilter> filters,
			final List<String> urlColumnNames,
			final List<String> integerColumnNames,
			final Map<String,Object> additionalColumns,
			final int collectionBasketHelperStyle,
			final Map<String,SortValueReinterpreter> sortValueReinterpreterMap,
			final Map<String, Row.IconGetter> iconMap,
			final boolean askToImport,
			final TabBrowser.PreBrowseListener preBrowseListener, 
			final TabBrowser.PostBrowseListener postBrowseListener,
			final TabBrowser.PreBasketAddLister preBasketAddLister,
			final String noun) {
		preventLaunch = true;
		TabBrowser tabBrowser = show(frame, files, collectionBaskets, customPanelAtToolBarEast, propertyPrefix, properties, 
				toolBarPlugin, computedColumnsPlugin, filters, urlColumnNames, integerColumnNames,
				additionalColumns, collectionBasketHelperStyle, sortValueReinterpreterMap, iconMap, askToImport,preBrowseListener,postBrowseListener, noun  );
		for (CollectionBasketHelper helper: tabBrowser.cbs) {
			if (helper instanceof LessInputExpandedOutputCollectionBasketHelper) {
				((LessInputExpandedOutputCollectionBasketHelper)helper).setPreBasketAddLister(preBasketAddLister);
			}
			else if (helper instanceof MoreInputReducedOutputCollectionBasketHelper) {
				((MoreInputReducedOutputCollectionBasketHelper)helper).setPreBasketAddLister(preBasketAddLister);
			}
		}
		tabBrowser.parentPanel = parent;
		launchTabBrowser2(frame, parent, tabBrowser, noun);
		preventLaunch = false;
		return tabBrowser;

	}
	
	
	public static boolean isSinglePick = false;
	
	public interface PostBrowseListener {
		void browseComplete(boolean isBrowseSuccessful, Collection<Row> rowsCollected);
	}
	
	public interface PreBrowseListener {
		void browseBegin();
	}
	
	public interface PreBasketAddLister {
		String getParentItemName();		
		List<Row> removeDuplicates(Collection<Row> rows);
	}
	
	private PostBrowseListener postBrowseListener;
	private PreBrowseListener preBrowseListener;
	
	private void pickPerformed() {
		Collection<Row> rowsCollected = cbs.get(0).collect();
		dispose();		
		if (parent != null) {
			SwingBasics.closeWindow(parent);
		}
		if (postBrowseListener != null) {
			postBrowseListener.browseComplete(true, rowsCollected);
		}
		
	}
	
	final private Window parent;
	
	public int getColumnIndex(final String columnName) {		
		return tableModel.getModelShowing().getModelColumnIndexFromDataColumnIndex(tableModel.getDataColumnIndexForLabel(columnName));
	}
	
	private static void launchTabBrowser(final JFrame frame, final TabBrowser tabBrowser, final String windowTag) {
		//frame.getContentPane().add(tabBrowser, BorderLayout.CENTER);				
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tabBrowser, BorderLayout.CENTER);
		final JPanel jp = new JPanel(new BorderLayout());
		final JLabel s = new JLabel();
		jp.add(s, BorderLayout.CENTER);
		if (isSinglePick) {
			final JButton importBtn = SwingBasics.getButton("Pick", MmsIcons.getAcceptIcon(), 'p', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (tabBrowser.tableModel.getModelShowing().getSelectedRowCountMinusLastRowForEditingAndFilterRows() == 0) {
						PopupBasics.alert(tabBrowser,"You have to pick an item first", "Alert", true);
						return;
					}
					tabBrowser.cbs.get(0).doubleClick();
					tabBrowser.pickPerformed();
				}
			}, "Pick the selected item");
			importBtn.setEnabled(false);
			final JButton cancelBtn = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'c', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingBasics.closeWindow(tabBrowser.parent);
				}
			}, "Pick the selected item");
			final JPanel eastPanel = new JPanel(new FlowLayout());
			eastPanel.add(importBtn);
			eastPanel.add(cancelBtn);
			jp.add(eastPanel, BorderLayout.EAST);
			tabBrowser.tableModel.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					int[] selected = tabBrowser.tableModel.getModelShowing().table.getSelectedRows();
					final int n = tabBrowser.tableModel.getModelShowing().getSelectedDataRowCount(selected);
					if (n > 0) {
						importBtn.setEnabled(true);
					}
					else {
						importBtn.setEnabled(false);
					}
				}
			});
		}
		tabBrowser.tableModel.setSizeInfo(s);
		tabBrowser.tableModel.setTreeAutoSelect(true, PersonalizableTableModel.TreeStartupNode.SELECT_ROOT_NODE_AND_SHOW_ALL_ROWS);
		//frame.getContentPane().add(jp, BorderLayout.SOUTH);
		topPanel.add(jp, BorderLayout.SOUTH);
		final JDialog dlg = new JDialog(frame);
		dlg.setTitle(frame.getTitle());
		final JLabel jl;
		if (isSinglePick) {
			jl=null;
			dlg.setModal(true);
			SwingBasics.packAndPersonalize(dlg, windowTag, true);
			SwingBasics.closeOnEscape(dlg);
			tabBrowser.tableModel.setPersonalizableWindowOwner((Window) dlg);
		}
		else {			
			SwingBasics.packAndPersonalize(frame, "tabBrowser", true);
			JLabel l=PersonalizableTableModel.oneMomentLabel;
			int vp=l.getVerticalTextPosition();
			jl=new JLabel(
					Basics.concat(
							"One moment ... loading ", 
							Basics.encode(tabBrowser.tableModel.getDataSource().size()), 
							" ",
							windowTag,
							" ..."), 
					l.getIcon(), 
					l.getHorizontalAlignment());
			jl.setVerticalTextPosition(vp);
			frame.add(jl);	
			tabBrowser.tableModel.setPersonalizableWindowOwner((Window) frame);
			frame.setVisible(true);
        	frame.toFront();    			
        }
		new Timer().schedule(new TimerTask() {
    		public void run() {
				final GradientBasics.Panel gp=new GradientBasics.Panel(new BorderLayout());
    			if (!isSinglePick) {
    				final JSplitPane catalogPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    				gp.add(catalogPane, BorderLayout.CENTER);
    				catalogPane.setTopComponent(topPanel);
        			final JPanel bottomPanel = new JPanel(new BorderLayout());
        			if (tabBrowser.cbs.size() > 0){
        				bottomPanel.add(tabBrowser.cbs.get(0).getBasketPanel(), BorderLayout.CENTER);
        				catalogPane.setBottomComponent(bottomPanel); 
        			}
        			frame.add(gp);
        			SwingBasics.packAndPersonalize(frame, windowTag, true);
        			frame.setVisible(true);
        			frame.toFront();    			
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {				
			        		catalogPane.setDividerLocation(frame.getHeight()/2);
			        		if (jl != null){
			        				jl.setVisible(false);
			        		}
			        	}
					});
    			} else {
    				tabBrowser.tableModel.getModelShowing().getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    				gp.add(topPanel, BorderLayout.CENTER);
    				dlg.add(gp);
    				if (tabBrowser.preBrowseListener != null) {
        				tabBrowser.preBrowseListener.browseBegin();
        			}
    				GradientBasics.setTransparentChildren(gp, true);
    				dlg.show();        		
    			}
  
    		}}, 1000);	
	}


	private static void launchTabBrowser2(final JFrame frame, final JPanel parentPanel, final TabBrowser tabBrowser, final String noun) {
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tabBrowser, BorderLayout.CENTER);
		final JPanel jp = new JPanel(new BorderLayout());
		final JLabel s = new JLabel();
		jp.add(s, BorderLayout.CENTER);
		if (isSinglePick) {
			final JButton importBtn = SwingBasics.getButton("Pick", MmsIcons.getAcceptIcon(), 'p', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (tabBrowser.tableModel.getModelShowing().getSelectedRowCountMinusLastRowForEditingAndFilterRows() == 0) {
						PopupBasics.alert(tabBrowser,"You have to pick an item first", "Alert", true);
						return;
					}
					tabBrowser.cbs.get(0).doubleClick();
					tabBrowser.pickPerformed();
				}
			}, "Pick the selected item");
			importBtn.setEnabled(false);
			final JButton cancelBtn = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'c', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingBasics.closeWindow(tabBrowser.parent);
				}
			}, "Pick the selected item");
			final JPanel eastPanel = new JPanel(new FlowLayout());
			eastPanel.add(importBtn);
			eastPanel.add(cancelBtn);
			jp.add(eastPanel, BorderLayout.EAST);
			tabBrowser.tableModel.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					int[] selected = tabBrowser.tableModel.getModelShowing().table.getSelectedRows();
					final int n = tabBrowser.tableModel.getModelShowing().getSelectedDataRowCount(selected);
					if (n > 0) {
						importBtn.setEnabled(true);
					}
					else {
						importBtn.setEnabled(false);
					}
				}
			});
		}
		tabBrowser.tableModel.setSizeInfo(s);
		tabBrowser.tableModel.setTreeAutoSelect(true, PersonalizableTableModel.TreeStartupNode.SELECT_ROOT_NODE_AND_SHOW_ALL_ROWS);
		//frame.getContentPane().add(jp, BorderLayout.SOUTH);
		topPanel.add(jp, BorderLayout.SOUTH);
		JDialog dlg = null;
		final JLabel jl;
		if (isSinglePick) {
			jl=null;
			SwingBasics.packAndPersonalize(frame, "tabBrowser", true);
			tabBrowser.tableModel.setPersonalizableWindowOwner((Window) frame);
		}
		else {			
			JLabel l=PersonalizableTableModel.oneMomentLabel;
			int vp=l.getVerticalTextPosition();
			jl=new JLabel(
					Basics.concat(
							"One moment ... loading ", 
							Basics.encode(tabBrowser.tableModel.getDataSource().size()), 
							" ",
							noun,
							" ..."), 
					l.getIcon(), 
					l.getHorizontalAlignment());
			jl.setVerticalTextPosition(vp);
			parentPanel.add(jl, BorderLayout.CENTER);	
			//SwingBasics.packAndPersonalize(frame, "tabBrowser", true);
			//tabBrowser.tableModel.setPersonalizableWindowOwner((Window) frame);
			 			
        }
		final JDialog dlg1=dlg;
		final GradientBasics.Panel gp=new GradientBasics.Panel(new BorderLayout());
		if (!isSinglePick) {
			final JSplitPane catalogPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			gp.add(catalogPane, BorderLayout.CENTER);
			catalogPane.setTopComponent(topPanel);
			final JPanel bottomPanel = new JPanel(new BorderLayout());
			if (tabBrowser.cbs.size() > 0){
				bottomPanel.add(tabBrowser.cbs.get(0).getBasketPanel(), BorderLayout.CENTER);
				catalogPane.setBottomComponent(bottomPanel); 
			}
			parentPanel.add(gp);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {				
	        		catalogPane.setDividerLocation(frame.getHeight()/2);
	        		if (jl != null){
	        				jl.setVisible(false);
	        		}
	        	}
			});
		} else {
			tabBrowser.tableModel.getModelShowing().getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			gp.add(topPanel, BorderLayout.CENTER);
			if (tabBrowser.preBrowseListener != null) {
				tabBrowser.preBrowseListener.browseBegin();
			}
			GradientBasics.setTransparentChildren(gp, true);
		}
		parentPanel.add(gp,BorderLayout.CENTER );
	}


	private static class LessInputExpandedOutputCollectionBasketHelper implements CollectionBasketHelper {
		
		private Window window;
		private final AddToBasket addToBasket;
		private final CheckOutBasket checkOutBasket;
		private JLabel basketCollectingHelp;
		private JPanel northPanelOfCollectionBasket;
		private final JPanel mainPanel = new JPanel(new BorderLayout());		
		private final JPanel toolbarPanel = new JPanel(new BorderLayout());
		private DataFlavor dataFlavor;
		private DataFlavor[] noDataFlavors = new DataFlavor[0];
		//private JButton basketButton;
		private final CollectionBasket collectionBasket;
		private final PersonalizableTableModel tableModel;
		private SwingBasics.PinButton pb;
		private int keyColumnIndex = -1;
		private PersonalizableTableModel basketModel;
		private final String REMOVE_TEXT = "Remove";
		private final JButton emptyBasket = SwingBasics.getButton(
				REMOVE_TEXT,
				MmsIcons.getCartRemoveIcon(),
				'r',
				new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						addToBasket.emptyBasket();
					}}, Basics.concat("Remove selected ", objectName, "s"),true);

		
		public JButton getBasketButton() {
			return null;//basketButton;
		}
		
		public Collection<Row> collect() {
			return checkOutBasket.collect();
		}
		
		public JButton getCheckoutBasketButton() {
			return checkOutBasket;
		}

		public JButton getAddToBasketButton() {
			return addToBasket;
		}
		
		public int getBasketCount() {
			return addToBasket.basketItems.size();
		}
		
		public String getAddToBasketButtonPrefixText() {
			return "Add";
		}
		
		public String getAddToBasketButtonSuffixText() {
			return " from catalog";
		}
		
		public boolean isVisible(){
			return window != null && window.isVisible();
		}
		
		public CollectionBasket getCollectionBasket() {
			return collectionBasket;
		}
		
		public void saveCollectionBasket() {
			collectionBasket.saveProperties(basketModel);
		}
		private PreBasketAddLister preBasketAddLister;
		
		public PreBasketAddLister getPreBasketAddLister() {
			return preBasketAddLister;
		}

		public void setPreBasketAddLister(PreBasketAddLister preBasketAddLister) {
			this.preBasketAddLister = preBasketAddLister;
		}

		private LessInputExpandedOutputCollectionBasketHelper(
				final Window window,
				final CollectionBasket collectionBasket,
				final PersonalizableTableModel tableModel,
				final ToolBarPlugin toolBarPlugin,
				final String propertyPrefix,
				final boolean askToCheckout) {
			this.collectionBasket = collectionBasket;
			this.tableModel = tableModel;
			this.window = window;
			addToBasket = new AddToBasket();
			keyColumnIndex = tableModel.getModelShowing().getDataColumnIndexForLabel(
					collectionBasket.getKeyDataColumnName());
			RowDragAndDrop.setTransferHandler(this.tableModel, exportRows);			
			checkOutBasket = new CheckOutBasket(collectionBasket, tableModel, askToCheckout);
			initBasketPanel(toolBarPlugin, propertyPrefix);
		}

		private RowDragAndDrop.Plugin exportRows = new RowDragAndDrop.Plugin() {
			public DataFlavor[] getImportDataFlavors() {
				return noDataFlavors;
			}

			public DataFlavor[] getExportDataFlavors() {
				return new DataFlavor[] { dataFlavor };
			}

			public boolean canExportRows() {
				return true;
			}

			public boolean canImportRows(final GroupedDataSource.Node sourcePick, final PersonalizableTableModel from,
					final Collection<Row> rows) {
				return false;
			}

			public void importRows(final com.MeehanMetaSpace.swing.Row importingTo,
					final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<com.MeehanMetaSpace.swing.Row> rows) {
			}
		};

		private RowDragAndDrop.Plugin importRows = new RowDragAndDrop.Plugin() {
			public DataFlavor[] getImportDataFlavors() {
				return new DataFlavor[] { dataFlavor };
			}

			public DataFlavor[] getExportDataFlavors() {
				return noDataFlavors;
			}

			public boolean canExportRows() {
				return false;
			}

			public boolean canImportRows(final GroupedDataSource.Node sourcePick, final PersonalizableTableModel from,
					final Collection<Row> rows) {
				final PersonalizableTableModel tm = tableModel;
				if (from.equals(tm)) {
					return true;
				}
				return false;
			}

			public void importRows(final com.MeehanMetaSpace.swing.Row importingTo,
					final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<com.MeehanMetaSpace.swing.Row> _rows) {
				final Collection<Row>rows=SwingBasics.handleMultiSelectRows(addToBasket, sourcePick, _rows);
				if (rows != null){
					addToBasket.add(rows);
				}
			}
		};
		
		public void doubleClick(){
			final Collection<Row> rows=tableModel.getModelShowing().getSelectedRowsInDescendingOrder();
			addToBasket.add(rows);			
		}
		
		public void dispose() {
			/*if (window != null) {
				SwingBasics.closeWindow(window);			
			}*/
		}
		
		private void initBasketPanel(final ToolBarPlugin toolBarPlugin, final String propertyPrefix) {
						
			final DefaultPersonalizableDataSource dataSource = new DefaultPersonalizableDataSource(
					(List<Row>) Arrays.asList(addToBasket.basketItems.values().toArray(new Row[0])),
					tableModel.dataSource.getMetaRow());
			if (propertyPrefix != null) {
				dataSource.setPropertyPrefix(propertyPrefix);				
			}
			basketModel = PersonalizableTableModel.activate(dataSource,
					collectionBasket.getProperties(), true);		
			basketModel.setCanDragDrop(true);
			PersonalizableTable table = new PersonalizableTable(basketModel);
			table.setBackgroundImage(backgroundTableImage, null);
			table.treatTheBackgroundImageAsModifyingAdvice();
			tableModel.setAutoFilter(true);
			if (preventLaunch) {
				tableModel.showFilterUI = false;
			}
			basketModel.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					
					int[] selected = basketModel.getModelShowing().table.getSelectedRows();
					final int n = basketModel.getModelShowing().getSelectedDataRowCount(selected);
					if (!preventLaunch) {
						if (n > 0) {		
							emptyBasket.setText( Basics.concat(REMOVE_TEXT, " ", Basics.encode(n), "  ", objectName,  (n>1?"s":"")));
							SwingBasics.setEnabled(emptyBasket, true);
						}
						else {
							emptyBasket.setText(REMOVE_TEXT);
							SwingBasics.setEnabled(emptyBasket, false);
						}
					}
				}
			});
			
			final JPanel jp = new JPanel();
			final JLabel s = new JLabel();
			jp.add(s);
			basketModel.setSizeInfo(s);
			
			dataFlavor = RowDragAndDrop.getDataFlavor(tableModel);
			final JScrollPane sb=basketModel.table.makeHorizontalScrollPane();
			RowDragAndDrop.setTransferHandler(basketModel, importRows);
			
			SwingBasics.setEnabled(emptyBasket, false);
			northPanelOfCollectionBasket = new JPanel();
			basketCollectingHelp = new JLabel(
					"<html><center>Collection basket to "
							+ collectionBasket.getPurpose()
							+ " &nbsp;(drag & drop or click Add button	)</center></html>");
			northPanelOfCollectionBasket.add(basketCollectingHelp);			
			mainPanel.add(northPanelOfCollectionBasket, BorderLayout.NORTH);
			
			final JPanel operationsPanel = new JPanel(new FlowLayout());
			if (preventLaunch) {
				operationsPanel.setLayout(new GridLayout(1,3,10,10));
			}
			operationsPanel.add(addToBasket);
			operationsPanel.add(emptyBasket);
			operationsPanel.add(checkOutBasket);
			
			final JPanel conclusionsPanel = new JPanel();
			/*pb = new SwingBasics.PinButton(conclusionsPanel, true,
					collectionBasket.getPropertyPrefix()+"CollectionBasket");						
			conclusionsPanel.add(pb);
			conclusionsPanel.add(SwingBasics.getDoneButton(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					SwingBasics.closeWindow(window);
				}
			}, "Close window"));*/
					
			toolbarPanel.setBorder(PersonalizableTable.BORDER_MAJOR);						
			if (toolBarPlugin != null) {
				toolbarPanel.add(toolBarPlugin.addToolBar("",basketModel, mainPanel), BorderLayout.CENTER);
			} else {
				final JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new BorderLayout());			
				centerPanel.add(sb, BorderLayout.CENTER);
				centerPanel.add(jp, BorderLayout.SOUTH);
				mainPanel.add(centerPanel, BorderLayout.CENTER);
				toolbarPanel.add(mainPanel, BorderLayout.CENTER);			
			}
			SwingBasics.layout(preventLaunch?true:false,toolbarPanel,null,null,operationsPanel,conclusionsPanel,preventLaunch?true:false);
		}
		
		static int baskets;
		
		
		public JPanel getBasketPanel() {
			return toolbarPanel;
		}

		private final class CheckOutBasket extends JButton implements ActionListener {

			final PersonalizableTableModel tableModel;
			
			private Collection<Row> collect() {
				return collectionBasket.checkOutPerformed(addToBasket.basketItems.values(), tableModel, false );
			}

			private CheckOutBasket(final CollectionBasket collectionBasket, final PersonalizableTableModel tableModel, final boolean askToCheckout) {
				this.tableModel = tableModel;
				this.askToCheckout=askToCheckout;
				update();
				setIcon(MmsIcons.getCartGoIcon());
				setEnabled(false);
				addActionListener(this);
				setMnemonic('i');
			}
			
			String getCheckOutLabel(final int n){
				return Basics.concat(collectionBasket.getCheckOutLabel(), " " , Basics.encode(n), " ", collectionBasket.getItemName(), (n == 1 ? "" : "s"));
			}

			private void update() {
				final int n = addToBasket.basketItems.size();
				final boolean hasPicks = n > 0;
				if (hasPicks) {
					final String label=getCheckOutLabel(n);
					setText(label);
					setEnabled(true);
				} else {
					setText(collectionBasket.getCheckOutLabel());
					setEnabled(false);
				}				
			}

			private final boolean askToCheckout;
			public void actionPerformed(final ActionEvent e) {
				if (!askToCheckout || PopupBasics.ask(window, Basics.concat(collectionBasket.getCheckOutLabel(), "?"))) {
					
					collect();
					if (collectionBasket.closeOnCheckOut()) {
						addToBasket.disposeBasket();
					}	
				}				
			}
		}

		private final class AddToBasket extends JButton implements ActionListener {

			private final Map<String, Row> basketItems = new TreeMap<String, Row>();

			private AddToBasket() {
				super(getAddToBasketButtonPrefixText() + getAddToBasketButtonSuffixText());
				addActionListener(this);
				setEnabled(false);
				setMnemonic('a');
				setIcon(MmsIcons.getCartPutIcon());
				setToolTipText(Basics.toHtmlUncentered("Add to basket", Basics.concat("Add ", objectName, "s to the basket")));
			}

			private void updateControls() {				
				checkOutBasket.update();
			}

			private String getCollectionBasketKeyValue(final Row row) {
				String key = "";			
				if (keyColumnIndex != -1) {
					Object keyValue = row.get(keyColumnIndex);
					if (keyValue != null) {
						key = (String)keyValue;						
					}	
				}
				
				return key;
			}

			private void add() {
				add(tableModel);
			}

			private void add(final PersonalizableTableModel _tableModel) {
				PersonalizableTableModel sourceShowingModel = _tableModel.getModelShowing();							
				final Collection<Row> c = sourceShowingModel.getSelectedRowsInDescendingOrder();								
				add(c);
			}

			private int add(final Collection<Row> rows) {
				final Collection<Row> c=new ArrayList<Row>();
				List<Row> duplicates =  new ArrayList<Row>();
				if (preBasketAddLister != null) {
					duplicates = preBasketAddLister.removeDuplicates(rows);
				}
				for (final Row row : rows) {
					String key = getCollectionBasketKeyValue(row);							
					if (basketItems.get(key) == null) {
						basketItems.put(key, row);
						basketModel.getDataSource().add(row);						
						c.add(row);
					}
				}
				updateControls();
				final int rejectedCount = (rows.size() - c.size()) + duplicates.size();				
				if (rejectedCount > 0) {					
					if (preBasketAddLister != null) {
						PopupBasics.alert(window, Basics.concat("Found ", Basics.encode(rejectedCount),
								" duplicate ", collectionBasket.getItemName(), (rejectedCount>1?"s":""), 
								" already in basket or in ", preBasketAddLister.getParentItemName()), "Duplicate", true);
					}
					else {
						PopupBasics.alert(window, Basics.concat("Found ", Basics.encode(rejectedCount),
								" duplicate ", collectionBasket.getItemName(), (rejectedCount>1?"s":"")), "Duplicate", true);
					}
					
				}
				basketModel.refresh(false);
				new Timer().schedule(new TimerTask() {
		    		public void run() {
		    			tableModel.getModelShowing().table.removeRowSelectionInterval(0, tableModel.getModelShowing().table.getRowCount() - 1);
		    			SwingUtilities.invokeLater(new Runnable() {
		    				public void run() {
		    					basketModel.reselect(c);		
		    					if (preventLaunch) {
		    						SwingUtilities.invokeLater(new Runnable() {
			    						public void run() {
			    							basketModel.reapplyFilter();
			    						}
			    					});
		    					}
		    				}
		    			});
		    		}}, 200);				
				return c.size();				
			}

			public void actionPerformed(final ActionEvent e) {
				addAllSelected();
				basketModel.requestFocusLater();
			}

			private void emptyBasket() {
				for (Row row: basketModel.getModelShowing().getSelectedRowsInDescendingOrder()) {
					String key = getCollectionBasketKeyValue(row);
					basketItems.remove(key);
				}
				basketModel.getModelShowing().deleteSelected();		
				updateControls();
				window.toFront();
			}
			
			private void disposeBasket() {
					basketItems.clear();
					emptyBasket();
					basketModel.getModelShowing().removeAll();
					basketModel.getModelShowing().updateUILater();
//					updateControls();
					//window.setVisible(false);
			}

			private void addAllSelected() {
				add();
			}

		}

		
	}
	
	private static class MoreInputReducedOutputCollectionBasketHelper implements CollectionBasketHelper {
		public boolean isVisible(){
			return window != null && window.isVisible();
		}
		private JFrame window;
		private final AddToBasket addToBasket;
		private final CheckOutBasket checkOutBasket;
		private JLabel basketCollectingHelp;
		private JPanel northPanelOfCollectionBasket;
		private final JPanel mainPanel = new JPanel(new BorderLayout(0, 4));
		private DataFlavor dataFlavor;
		private DataFlavor[] noDataFlavors = new DataFlavor[0];
		private JButton basketButton;
		private CollectionBasket collectionBasket;
		private PersonalizableTableModel tableModel;
		private SwingBasics.PinButton pb;
		private int keyColumnIndex = -1;
		
		public JButton getBasketButton() {
			return basketButton;
		}
		
		public JPanel getBasketPanel() {
			return null;
		}
		
		public JButton getAddToBasketButton() {
			return addToBasket;
		}
		
		public Collection<Row> collect() {
			return checkOutBasket.collect();
		}
		
		public JButton getCheckoutBasketButton() {
			return checkOutBasket;
		}
		
		public int getBasketCount() {
			return addToBasket.basketItems.size();
		}
		
		public String getAddToBasketButtonPrefixText() {
			return null;
		}
		
		public String getAddToBasketButtonSuffixText() {
			return null;
		}

		public CollectionBasket getCollectionBasket() {
			return collectionBasket;
		}
		
		private PreBasketAddLister preBasketAddLister;
		
		
		public PreBasketAddLister getPreBasketAddLister() {
			return preBasketAddLister;
		}

		public void setPreBasketAddLister(PreBasketAddLister preBasketAddLister) {
			this.preBasketAddLister = preBasketAddLister;
		}
		
		private MoreInputReducedOutputCollectionBasketHelper(
				final CollectionBasket collectionBasket,
				final PersonalizableTableModel tableModel,
				final boolean askToCheckout) {
			this.collectionBasket = collectionBasket;
			addToBasket = new AddToBasket();
			this.tableModel = tableModel;
			keyColumnIndex = tableModel.getModelShowing().getDataColumnIndexForLabel(
					collectionBasket.getKeyDataColumnName());
			RowDragAndDrop.setTransferHandler(this.tableModel, exportRows);
			basketButton = SwingBasics.getButton(collectionBasket.getButtonText(), collectionBasket.getButtonMnemonic(),
					new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							if (window != null) {
								if (!window.isVisible()) {
									window.setVisible(true);
								}
								window.setState(JFrame.NORMAL);
								window.toFront();
							} else {
								showCollectionBasketDialog();
							}
						}
					}, Basics.toHtmlUncentered("Open basket",
							Basics.concat("Click to show ", collectionBasket.getPurpose(), "<br>collection buttons")));
			checkOutBasket = new CheckOutBasket(collectionBasket, tableModel, askToCheckout);
			initBasketPanel();
		}

		private RowDragAndDrop.Plugin exportRows = new RowDragAndDrop.Plugin() {
			public DataFlavor[] getImportDataFlavors() {
				return noDataFlavors;
			}

			public DataFlavor[] getExportDataFlavors() {
				return new DataFlavor[] { dataFlavor };
			}

			public boolean canExportRows() {
				return true;
			}

			public boolean canImportRows(final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<Row> rows) {
				return false;
			}

			public void importRows(final com.MeehanMetaSpace.swing.Row importingTo,
					final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<com.MeehanMetaSpace.swing.Row> rows) {
			}
		};

		private RowDragAndDrop.Plugin importRows = new RowDragAndDrop.Plugin() {
			public DataFlavor[] getImportDataFlavors() {
				return new DataFlavor[] { dataFlavor };
			}

			public DataFlavor[] getExportDataFlavors() {
				return noDataFlavors;
			}

			public boolean canExportRows() {
				return false;
			}

			public boolean canImportRows(final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<Row> rows) {
				final PersonalizableTableModel tm = tableModel;
				if (from.equals(tm)) {
					return true;
				}
				return false;
			}

			public void importRows(final com.MeehanMetaSpace.swing.Row importingTo,
					final GroupedDataSource.Node sourcePick, 
					final PersonalizableTableModel from,
					final Collection<com.MeehanMetaSpace.swing.Row> _rows) {
				final Collection<Row>rows=SwingBasics.handleMultiSelectRows(addToBasket, sourcePick, _rows);
				if (rows != null) {
					System.out.println(sourcePick);
					addToBasket.bad.clear();
					for (final Row row : rows) {
						addToBasket.add(row);
					}
					addToBasket.setToolTipText();
				}
			}
		};
		
		public void doubleClick(){
			addToBasket.bad.clear();
			final Collection<Row> rows=tableModel.getModelShowing().getSelectedRowsInDescendingOrder();
			for (final Row row : rows) {
				addToBasket.add(row);
			}
			addToBasket.setToolTipText();
		}
		
		public void dispose() {
			if (window != null) {
				SwingBasics.closeWindow(window);			
			}
		}

		private void initBasketPanel() {		
			dataFlavor = RowDragAndDrop.getDataFlavor(tableModel);
			final JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
			if (preventLaunch) {
				buttonPanel.setLayout(new GridLayout(1, 5));
			}
			northPanelOfCollectionBasket = new JPanel();
			basketCollectingHelp = new JLabel(
					"<html><center>Collection basket to "
							+ collectionBasket.getPurpose()
							+ "<br>Drag & drop or <br>click logo.</center></html>");
			basketCollectingHelp.setIcon(MmsIcons.getDownIcon());
			basketCollectingHelp.setHorizontalTextPosition(JLabel.CENTER);
			basketCollectingHelp.setVerticalTextPosition(JLabel.TOP);
			northPanelOfCollectionBasket.add(basketCollectingHelp);
			mainPanel.add(northPanelOfCollectionBasket, BorderLayout.NORTH);
			buttonPanel.add(addToBasket);
			RowDragAndDrop.setTransferHandler(addToBasket, importRows);
			SwingBasics.setEnabled(emptyBasket, false);
			buttonPanel.add(emptyBasket);
			buttonPanel.add(checkOutBasket);
			if (preventLaunch) {
				buttonPanel.add(new JLabel("<html>&nbsp</html>"));
				buttonPanel.add(new JLabel("<html>&nbsp</html>"));
			}
			final JPanel jp = new JPanel();
			jp.add(buttonPanel);
			mainPanel.add(jp, BorderLayout.CENTER);
			final JPanel conclusionButtonPanel = new JPanel(new BorderLayout());
			pb = new SwingBasics.PinButton(conclusionButtonPanel, true,
					collectionBasket.getPropertyPrefix()+"CollectionBasket");
			final JPanel buttons2 = SwingBasics.getButtonPanel(-1);
			buttons2.add(pb);
			buttons2.add(SwingBasics.getDoneButton(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					SwingBasics.closeWindow(window);
				}
			}, "Close window"));
			conclusionButtonPanel.add(buttons2, BorderLayout.EAST);
			mainPanel.add(conclusionButtonPanel, BorderLayout.SOUTH);
		}
		static int baskets;
		
		private void showCollectionBasketDialog() {
			window = SwingBasics.getFrame();
			window.setTitle(collectionBasket.getBasketWindowTitle());
			window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);			
			final Container contentPane = window.getContentPane();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (window != null) {
						final Point p = basketButton.getParent()
								.getLocationOnScreen();
						window.setLocation(new Point(p.x + 200,
								p.y + 100));
						if (!SwingBasics.packAndPersonalize(window, null,
								null, collectionBasket.getPropertyPrefix()+"CollectionBasketWindow", false, false, false,
								false, null)){
							baskets++;
							int j=baskets%5;
							switch(j){
							case 0:
								SwingBasics.center(window);
								break;
							case 1:
								SwingBasics.topRight(window);
								break;
							case 2:
								SwingBasics.bottomRight(window);
								break;
							case 3:
								window.setLocation(0,0);
								break;
							case 4:
								SwingBasics.bottomLeft(window);
								break;
							}
							SwingBasics.adjustToAvailableScreens(window, false);
						}
						window.setVisible(true);
						window.toFront();
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							pb.activate(window);
						}
					});
				}
			});
			mainPanel.setBorder(PersonalizableTable.BORDER_MAJOR);
			contentPane.add(mainPanel, BorderLayout.CENTER);
			SwingBasics.closeOnEscape(window);
			window.addWindowListener(new WindowAdapter() {
				public void windowClosing(final WindowEvent e) {
					final JFrame f = window;
					f.dispose();
					window = null;
				}
			});
		}

		private final class CheckOutBasket extends JButton implements ActionListener {

			PersonalizableTableModel tableModel;
			
			private Collection<Row> collect() {
				return collectionBasket.checkOutPerformed(addToBasket.basketItems.values(), tableModel, false );
			}

			private CheckOutBasket(final CollectionBasket collectionBasket, final PersonalizableTableModel tableModel, final boolean askToCheckout) {
				this.tableModel = tableModel;
				update("");
				setEnabled(false);
				setIcon(MmsIcons.getCartGoIcon());
				addActionListener(this);
			}
			
			String getCheckOutLabel(final int n){
				return Basics.concat("<html>", collectionBasket.getCheckOutLabel(), "<br><small>", Basics.encode(n), " ",
					collectionBasket.getItemName(), (n == 1 ? "" : "s"),  "</small></html>");
			}

			private void update(final String toolTip) {
				final int n = addToBasket.basketItems.size();
				final boolean hasPicks = n > 0;
				if (hasPicks) {
					final String label=getCheckOutLabel(n);
					setText(label);
					setEnabled(true);
				} else {
					setText(collectionBasket.getCheckOutLabel());
					setEnabled(false);
				}
				setToolTipText(toolTip);
				if (toolTip != null) {
					ToolTipOnDemand.getSingleton().show(this, false, 15, 65, true);
				} else {
					ToolTipOnDemand.getSingleton().hideTipWindow();
				}
			}

			public void actionPerformed(final ActionEvent e) {
				if (PopupBasics.ask(window, collectionBasket.getCheckOutLabel() + "?")) {
					if (northPanelOfCollectionBasket.getComponentZOrder(basketCollectingHelp) >= 0) {
						window.setResizable(true);
						window.pack();
						window.setResizable(false);
					}
					collect();
					if (collectionBasket.closeOnCheckOut()) {
						addToBasket.disposeBasket();
					}	
				}				
			}
		}

		class SimplePickList extends JPanel {

			PersonalizableTableModel model;
			JButton pickDescriber, cancelButton;
			JDialog dialog;
			Collection<Row> picked;

			private void userPickActionPerformed() {
				picked = model.getSelectionsInDescendingOrder(true);
				if (picked.size() > 0) {
					dialog.dispose();
				}
			}

			public Collection<Row> getPicked() {
				return picked;
			}

			SimplePickList(Collection<Row> values, String title, String pickVerb) {
				DefaultPersonalizableDataSource dataSource = new DefaultPersonalizableDataSource(
						(List<Row>) Arrays.asList(values.toArray(new Row[0])),
						tableModel.dataSource.getMetaRow());
				model = PersonalizableTableModel.activate(dataSource,
						"simplePickList.properties", true);
				setLayout(new BorderLayout());
				setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				PersonalizableTable table = new PersonalizableTable(model);
				add(table.makeHorizontalScrollPane(), BorderLayout.CENTER);
				JPanel buttonPanel = new JPanel();
				pickDescriber = SwingBasics.getButton(pickVerb, MmsIcons
						.getYesIcon(), 'p', new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						userPickActionPerformed();
					}
				}, Basics.toHtmlUncentered(pickVerb,
						"Select the current selections"));

				cancelButton = SwingBasics.getButton("Cancel", MmsIcons
						.getCancelIcon(), '\0', new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						picked = null;
						dialog.dispose();
					}
				}, "Close Window");

				Action escAction = new AbstractAction() {
					public void actionPerformed(ActionEvent evt) {
						cancelButton.doClick(150);
					}

					public boolean isEnabled() {
						return (cancelButton != null) && (cancelButton.isEnabled());
					}
				};
				getActionMap().put("esc-action", escAction);
				InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
				im.put(key, "esc-action");
				buttonPanel.add(pickDescriber);
				buttonPanel.add(cancelButton);

				add(buttonPanel, BorderLayout.SOUTH);
				tableModel.setAutoFilter(true);

				dialog = new JDialog();
				dialog.setTitle(title);
				dialog.setModal(true);
				dialog.getContentPane().add(this, BorderLayout.CENTER);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(final WindowEvent e) {
						picked = null;
						dialog.dispose();
					}
				});
				final JPanel jp = new JPanel();				
				dialog.getContentPane().add(jp, BorderLayout.SOUTH);
				dialog.pack();
				this.model.setPersonalizableWindowOwner(dialog);
				this.model.selectAll();
				this.pickDescriber.requestFocus();
				model.setPersonalizableWindowOwner(dialog);
				SwingBasics.windowActivated(dialog);
				dialog.getRootPane().setDefaultButton(pickDescriber);
				
				dialog.setVisible(true);
			}

		}

		private final class AddToBasket extends JButton implements ActionListener {

			private final Map<String, Row> basketItems = new TreeMap<String, Row>(),
					currentBasketItems = new TreeMap<String, Row>();
			private final Map<String, String> basketItemToolTips = new TreeMap<String, String>(),
					currentBasketItemToolTips = new TreeMap<String, String>();
			private final Collection<String> bad = new ArrayList<String>();;

			private AddToBasket() {
				super(null, collectionBasket.getDropIcon());
				super.setHorizontalTextPosition(SwingConstants.RIGHT);
				addActionListener(this);
				setToolTipText(Basics.toHtmlUncentered("Add to basket",Basics.concat(
						"Add ", collectionBasket.getItemName(), "s to the basket")));
			}

			private void setToolTipText() {
				final StringBuilder sb = new StringBuilder(Basics
						.startHtmlUncentered());
				final ArrayList<String> toolTips = new ArrayList<String>();
				for (final String key : basketItemToolTips.keySet()) {
					toolTips.add(basketItemToolTips.get(key));				
				}
				int totalAdded = currentBasketItems.size();
				if (totalAdded > 0) {
					if (basketItems.size() > 0) {
						toolTips.add(0,
								Basics.concat("<tr><td colspan='4'><i><b><center>Previous ", Basics.encode(basketItems.size())," ", collectionBasket.getItemName(),"s</center></b></i></td></tr>"));
					}
					pickCollectionBasketItems(false);
					for (final String key : currentBasketItems.keySet()) {
						final String toolTip = currentBasketItemToolTips.get(key);
						basketItemToolTips.put(key, toolTip);
						// most recent first in tool tip;
						toolTips.add(0, toolTip);
						final Row row = currentBasketItems.get(key);
						basketItems.put(key, row);
					}
					totalAdded = currentBasketItems.size();
					if (totalAdded < 1) {
						return;
					}
					currentBasketItems.clear();
				}
				final String tableStart = "<table border='1'><thead><tr><th>Key</th><th colspan="
						+ collectionBasket.getDataColumnIndexes().size()
						+ ">Details</th>";
				final int totalInBasket = basketItems.size();
				if (totalInBasket > 0) {
					sb.append("<center><b>&nbsp;");
					if (totalAdded > 0) {
						sb.append(totalAdded);
						sb.append(" new ");
						sb.append(collectionBasket.getItemName());
						if (totalAdded >1) {
							sb.append("s");
						}
						sb.append(" added to basket");
						if (totalInBasket > totalAdded) {
							sb.append("<small> (basket now contains ");
							sb.append(Basics.encode(totalInBasket));
							sb.append(" ");
							sb.append(collectionBasket.getItemName());
							sb.append("s)</small>");
						}

					} else {
						sb.append("No change to basket </b>...basket contains ");
						sb.append(Basics.encode(totalInBasket));
						sb.append(" ");
						sb.append(collectionBasket.getItemName());
						if (totalInBasket>1) {
							sb.append("s");
						}
					}

					sb.append("</b></center>");
					sb.append(tableStart);
					sb.append("</tr></thead>");
					int i = 0;
					for (final String toolTip : toolTips) {
						sb.append(toolTip);
						i++;
						if (i >= 10) {
							sb.append("<tr><td  colspan='3'><b>"
									+ (totalInBasket - i)
									+ " more ...</b>.</td></tr>");
							break;
						}
					}
					sb.append("</table>");
					SwingBasics.setEnabled(emptyBasket, true);
				} else {
					SwingBasics.setEnabled(emptyBasket, false);
				}
				int badCnt = bad.size();
				if (badCnt > 0) {
					sb.append("<h3><font color='red'> ");
					sb.append(Basics.encode(badCnt));
					sb.append(" ");
					sb.append(collectionBasket.getItemName());
					if (badCnt > 1) {
						sb.append("s");
					}
					sb.append(" not added to basket</font></h3>");
					sb.append(Basics.lineFeed);
					sb.append(tableStart);
					sb
							.append("<th><font color='red'>Problem</font></th></tr></thead>");
					int i = 0;
					for (final String s : bad) {
						sb.append(Basics.lineFeed);
						sb.append(s);
						i++;
						if (i >= 10) {
							sb.append("<tr><td  colspan='4'><b>" + (badCnt - i)
									+ " more ....</b></td></tr>");
							break;
						}

					}
					sb.append("</table>");
				}
				sb.append(Basics.endHtmlUncentered());
				final String toolTip = sb.toString();
				checkOutBasket.update(totalInBasket + badCnt > 0 ? toolTip : null);
			}

			private boolean pickCollectionBasketItems(final boolean removing) {
				final Map<String, Row> map;
				final String title;
				final int min;
				boolean value = false;
				if (!removing) {
					map = currentBasketItems;
					title = Basics.concat("Select which ", Basics.encode(currentBasketItems.size()),  " ", collectionBasket.getItemName(), "s to add");
					min = 2;
				} else {
					map = basketItems;
					title = Basics.concat("Select one or more ", collectionBasket.getItemName(), "s to remove");
					min = 1;
				}
				if (map.size() >= min) {
					Collection<Row> vals = map.values();
					SimplePickList simplePickList = new SimplePickList(vals, title,
							removing ? "Remove from basket " : "Add to basket");
					final Collection<Row> values = simplePickList.getPicked();
					if (values != null) {
						value = values.size() > 0;
						if (!removing) {
							map.clear();
						}
						for (final Row row : values) {
							String key = getCollectionBasketKeyValue(row);
							if (!removing) {
								map.put(key, row);
							} else {
								basketItems.remove(key);
								basketItemToolTips.remove(key);
							}
						}
					}
					else {
						currentBasketItems.clear();
						currentBasketItemToolTips.clear();
					}
					
				}
				return value;
			}

			private String getCollectionBasketKeyValue(final Row row) {
				String key = "";			
				if (keyColumnIndex != -1) {
					Object keyValue = row.get(keyColumnIndex);
					if (keyValue != null) {
						key = (String)keyValue;						
					}	
				}
				
				return key;
			}

			private void add() {
				add(tableModel);
			}

			private void add(final PersonalizableTableModel _tableModel) {
				final Collection<Row> c = _tableModel.getModelShowing()
						.getSelectedRowsInDescendingOrder();			
				for (final Row row : c) {
					add(row);
				}
			}

			private void add(final Row row) {
				
				String key = getCollectionBasketKeyValue(row);
							
				if (basketItems.get(key) == null) {
					final StringBuilder end = new StringBuilder("<td><small>"), start = new StringBuilder(
							"<tr>");
					end.append(key);
					end.append("</small></td>");
					for (final Integer i : collectionBasket.getDataColumnIndexes()) {
						end.append("<td><small>");
						Object columnValue = row.get(i);
						if (columnValue == null) {
							end.append("&nbsp;");
						}
						else {
							end.append(columnValue.toString());						
						}
						end.append("</small></td>");
					}
					start.append(end);
					start.append("</tr>");
					final String toolTip = start.toString();
					currentBasketItemToolTips.put(key, toolTip);
					currentBasketItems.put(key, row);
				}

			}

			public void actionPerformed(final ActionEvent e) {
				addAllSelected();
			}

			private void emptyBasket() {
				if (pickCollectionBasketItems(true)) {
					bad.clear();
					currentBasketItems.clear();
					currentBasketItemToolTips.clear();
					setToolTipText();
				}
			}
			
			private void disposeBasket() {
					bad.clear();
					currentBasketItems.clear();
					currentBasketItemToolTips.clear();
					basketItems.clear();
					basketItemToolTips.clear();
					setToolTipText();
					window.setVisible(false);
			}

			private void addAllSelected() {
				bad.clear();
				currentBasketItems.clear();
				add();
				setToolTipText();
			}

		}

		private JButton emptyBasket = SwingBasics
				.getButton(
						"<html><center>Remove/<br>review</center></html>",
						null,
						'e',
						new ActionListener() {
							public void actionPerformed(final ActionEvent e) {
								addToBasket.emptyBasket();
							}
						},
						Basics.concat("Remove (and/or review) one or all ", objectName, "<br>that are currently in the basket"),
						true);

	}
	
	static interface CollectionBasketHelper {
		JButton getBasketButton();
		JButton getAddToBasketButton();
		String getAddToBasketButtonPrefixText();
		String getAddToBasketButtonSuffixText();
		void doubleClick();
		boolean isVisible();
		void dispose();
		CollectionBasket getCollectionBasket();
		int getBasketCount();
		JPanel getBasketPanel();
		JButton getCheckoutBasketButton();
		Collection<Row> collect();
	}

	public static TabBrowser NewStrings(final String []args) {
		return New(args, null, null, "measurements", "tabBrowser", "Reagent catalog browser");
	}
	
	public static Properties fcsDataTypesTransformed() {
		final Properties props=new Properties();
		props.setProperty("Event #", "int");
		props.setProperty("Label", "int");
		props.setProperty("default", "float");
		return props;
	}

	public static Properties fcsDataTypes() {
		final Properties props=new Properties();
		props.setProperty("Event #", "int");
		props.setProperty("SSC-A", "int");
		props.setProperty("SSC-H", "int");
		props.setProperty("SSC-W", "int");
		props.setProperty("FSC-A", "int");
		props.setProperty("FSC-H", "int");
		props.setProperty("FSC-W", "int");
		props.setProperty("Time", "int");
		props.setProperty("Label", "int");
		props.setProperty("default", "float");
		return props;
	}

	public static TabBrowser NewFcsData(final String fileName, final String propFile) {
		return New(new String[] {fileName}, propFile, fcsDataTypes(),
				"measurements", "fcs_data", "FCS sample data");
	}

	public static TabBrowser NewFcsData(final String fileName) {
		return NewFcsData(fileName, "/Users/swmeehan/Desktop/AutoGateDemoExperiments/bCellMacrophageDiscoveryDemo.txt.autoGate/ReagentTable.properties");
	}

	public static TabBrowser NewFcsDataTransformed(final String fileName) {
		return New(new String[] {fileName}, "default", fcsDataTypesTransformed(),
				"measurements", "tabBrowser", "FCS sample data, (transformed)");
	}

	public static TabBrowser New(final String []fileNames, final String propsFileName,
			final Properties columnTypes, final String dataTag, 
			final String windowTag, final String windowTitle) {
		final File propsFile;
		if (propsFileName!=null && propsFileName.equalsIgnoreCase("default")) {
			propsFile=IoBasics.switchExtension(new File(fileNames[0]), "properties");
		} else if (propsFileName != null){
			propsFile=new File(propsFileName);
		}else {
			propsFile=null;
		}
		final Properties props;
		if (propsFile!=null && propsFile.exists()) {
			props=PropertiesBasics.loadProperties(propsFile);
		}else {
			props=new Properties();
		}
		
		final List<File> files = new ArrayList<File>();
		for (final String fileName : fileNames) {
			File file = new File(fileName);
			if (!file.exists() || !file.isFile()) {
				PopupBasics.alert("Invalid argument: "
						+ fileNames[0] + " is not a valid file");
			}
			files.add(file);
		}
		JFrame frame=null;
		JLabel jl=null;
		if (windowTag != null) {
			frame = new JFrame(windowTitle);
			SwingBasics.keepOnSameScreenAs(null, frame);
			frame.getContentPane().setLayout(new BorderLayout());
			final JLabel l=PersonalizableTableModel.oneMomentLabel;
			int vp=l.getVerticalTextPosition();
			jl=new JLabel("<html>"+
					Basics.concat(
							"<html><center><h2>One moment ... loading ", 
							dataTag, 
							" from </h2><b>",
							fileNames[0],
							"</b> ...<hr></html>"), 
					l.getIcon(), 
					l.getHorizontalAlignment());
			jl.setVerticalTextPosition(vp);
			frame.getContentPane().add(jl, BorderLayout.CENTER);
			if (SwingBasics.highDef) {
				frame.setPreferredSize(new Dimension(
						(int)(650*SwingBasics.toolBarFactor),
						(int)(400*SwingBasics.toolBarFactor)));
			} else {
				frame.setPreferredSize(new Dimension(650,400));
			}
			SwingBasics.packAndPersonalize(frame, windowTag, true);
			frame.setVisible(true);
		}
		Collection<CollectionBasket> shoppingCarts = new ArrayList<CollectionBasket>();
		final TabBrowser tb=TabBrowser.show(frame, windowTag, files, 
				shoppingCarts, null, dataTag, props, null, 
				null, null, null,null, null, 0,null, 
				null, true, null, null, columnTypes);
		if (frame!=null ) {
			if (propsFile != null) {
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						tb.setTreeTableProperties(propsFile);
						tb.saveTreeTablePersonalizations();
					}
				});
			}
			jl.setText("");
		}
		return tb;
	}

public static void main(final String []args){

	if (args.length<2){
		System.out.println();
		System.out.println();

		System.out.println("Usage:  "+ TabBrowser.class.getName());
		System.out.println("\t\t{property prefix} {file name} {file name} etc.");
		System.exit(1);
	}
	//main2(args);
	SwingBasics.initialize(null, null, false, 1,1,1.5f);
	NewFcsData(args[1]);
	SwingBasics.initialize(null, null, false, 1,1,1.5f);
}

public static void main2(final String []args){
	
	if (args.length<2){
		System.out.println();
		System.out.println();
		
		System.out.println("Usage:  "+ TabBrowser.class.getName());
		System.out.println("\t\t{property prefix} {file name} {file name} etc.");
		System.exit(1);
	}
	PersonalizableTableModel.setRootDir(System.getProperty("user.home")
			+ "/test_table");
	SwingBasics.doDefaultLnF();
	com.MeehanMetaSpace.Pel.init(IoBasics.concat(System
			.getProperty("user.home"), "pel.log"), TabBrowser.class,
			"Table test", false);
	SwingBasics.resetDefaultFonts();
	PersonalizableTable.resetDefaultFonts();
	final JFrame frame = new JFrame("Reagent catalog browser");
	SwingBasics.keepOnSameScreenAs(null, frame);
	if (Basics.isMac()) {
		MacintoshBasics.handleQuit(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});
	}
	final String propertyPrefix=args[0];
	frame.getContentPane().setLayout(new BorderLayout());

	
	List<File> files = new ArrayList<File>();
	int i=0;
	for (final String fileName : args) {
		if (i > 0) {
			File file = new File(fileName);
			if (!file.exists() || !file.isFile()) {
				PopupBasics.alert("Invalid command line argument : "
						+ args[0] + " is not a valid file");
				System.exit(1);
			}
			files.add(file);
		}
		i++;
	}
	Collection<CollectionBasket> shoppingCarts = new ArrayList<CollectionBasket>();
	TabBrowser.show(frame, files, shoppingCarts, null, propertyPrefix, null, null, null, null, null,null, null, 0,null, null, true, null, null, "records");
}


	public interface ToolBarPlugin {
		public JComponent addToolBar(final String label, final PersonalizableTableModel tableModel, final JPanel headerComponent);
	}
	
	public interface ComputedColumnsPlugin {
		/**
		 * 
		 * @return Map containing the display pattern (containing column indexes wrapped in calibraces {0})as key 
		 * and respective replacement column names as values
		 */
		public HashMap<String,List<String>> getComputedColumns();
	}
	
	public static ImageIcon backgroundTableImage;
	public static void setProgressBarWindow(final int numberOfFiles, final Component component){
		if (numberOfFiles>1){
			progressShower=new ProgressShower(numberOfFiles, component);
		}
	}
	
	static class ProgressShower implements IoBasics.FileProgress{
		private final JProgressBar progressBar=new JProgressBar();
		private final JDialog dlg;
		private ProgressShower(final int numberOfFiles, final Component component){
			dlg=SwingBasics.getDialog(component);
			dlg.setTitle("One moment ...");
			final JLabel label=new JLabel(Basics.concatObjects("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Downloading ", numberOfFiles, " reagent catalogs&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));			
			final JPanel mainPanel=new JPanel(new BorderLayout());
			mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
			mainPanel.add(label, BorderLayout.NORTH);
			mainPanel.add(progressBar, BorderLayout.CENTER);
			dlg.setModal(false);
			dlg.getContentPane().add(mainPanel);
			progressBar.setStringPainted(true);
			progressBar.setMaximum(numberOfFiles*2);
			final boolean personalized=SwingBasics.packAndPersonalize(dlg, "tabBrowserOpener");
			if (!personalized){
				SwingBasics.bottomRight(dlg);
			}
		}
		
		
		public void increment(final String status) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					public void run() {
						final int value=progressBar.getValue();
						progressBar.setValue(value+1);
						progressBar.setString(status);
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		void beforeMetaData(){
			dlg.setModal(true);
			dlg.setVisible(true);
		}
		void afterMetaData(){
			dlg.setModal(false);
			dlg.setVisible(false);
		}
		private void beforeData(){
			dlg.setModal(true);	
			dlg.setVisible(true);
		}
		
		private void afterData(){
			SwingBasics.closeWindow(dlg);
			progressShower=null;
		}
	}
	
	static ProgressShower progressShower;
	
}
