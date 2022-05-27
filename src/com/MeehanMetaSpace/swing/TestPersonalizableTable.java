/*
 =====================================================================
  JPersonalizedTableTest.java
  Created by Stephen Meehan
  Copyright (c) 2002
 =====================================================================
 */
package com.MeehanMetaSpace.swing;

import javax.jnlp.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.TreePath;
import com.MeehanMetaSpace.*;

public class TestPersonalizableTable
	extends JPanel{
  static JSpinner spinner=null;
  static void testJnlpTransportedData(){
	final String surl=IoBasics.PROTOCOL_CLASS +
		TestPersonalizableTable.class.getName() + "/stuff.txt";
	final java.io.BufferedReader br=IoBasics.getURLReaderWithoutThrowingUp(surl);

	if (br != null){
	  ArrayList al=IoBasics.readTextLinesAndClose(br, false);
	  System.err.println(al.size() + " lines");
	  for (int i=0; i < al.size(); i++){
		System.err.println(al.get(i));
	  }
	}
	else{
	  System.err.println("Can not read " + surl);
	}

  }

  class DataSource
	  extends DefaultPersonalizableDataSource
	  implements DefaultPersonalizableDataSource.CanPick{
	ArrayList<String> pickedNames=new ArrayList<String>();

	DataSource(java.util.List data, MetaRow metaRow){
	  super(data, metaRow);
	}

	public String getSelectedNodeDisabledText(javax.swing.tree.TreeNode node,
											  Row row){
	  return null;
	}

	public boolean handleDoubleClick(final TableCellContext context){
	  ToolTipOnDemand.getSingleton().show(menu, true);
	  return true;
	}

	public String getNodeEnabledText(
		final javax.swing.tree.TreeNode node,
		final Row row,
		final int[] sortOrder,
		final int columnThatDiffers,
		final int uncondensedDataColumnIndex){
	  return null;
	}

	public Collection getNodeDisabledText(final GroupedDataSource.Node node, Row row, int[] sortOrder,
										  int columnThatDiffers){
	  if (sortOrder != null && sortOrder[columnThatDiffers] == colRate){
		if (((Float) row.get(colRate)).floatValue() > 55){
		  return Basics.toList("Too high");
		}
	  }
	  String name=(String) row.get(colName);
	  if (pickedNames.contains(name)){
		return Basics.toList(name + " already picked ");
	  }
	  return null;
	}

	public Collection getRowDisabledText(Row row){
	  return getNodeDisabledText(null, row, null, 0);
	}

	public TreePath[] reorderPicks(TreePath[] tp){
	  return tp;
	}

	public Object createPick(javax.swing.tree.TreeNode node, Row row,
							 int columnThatDiffers){
	  return row;
	}

	public Object createPick(Row row){
	  return row;
	}

	public void resetPicks(){
	  pickedNames=new ArrayList<String>();
	}

	public void tryPicks(java.util.List<Object> picks){
	  Iterator<Object> it=picks.iterator();
	  while (it.hasNext()){
		Object name=((Row) it.next()).get(colName);
		pickedNames.add((String)name);
	  }
	}

    public void completePicks(final Collection<GroupedDataSource.Node> originalPickNodes, final Collection<Row> orignalRowPicks,
      final Collection<Row> rejectedRows){
	}
  }

  static final int colName=0, colRate=1, colIQ=2, colBirthDate=3,
	  colScore=4, colHappy=5, colRating=6;
  //final PersonalizableTableModel ptm;
  PersonalizableTable table;

  public TestPersonalizableTable(){
	setLayout(new GridLayout());
	setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
//		setPreferredSize(new Dimension(400, 400));
	makeModel();
	//ptm.setReadOnly(true);

	table=new PersonalizableTable(tableModel);
	add(table.makeHorizontalScrollPane());
	tableModel.setAutoFilter(true);

  }

  //	static Date birthDateValues[]={null};

  protected PersonalizableTableModel makeModel(){
	final Random random=new Random();

	final String[] ratings={
		"Friendly", "Mean", "Normal", "Not really nice", "Freaky",
		"Evil"
	};
	final String nameValues[]={
		"Sharon Meehan", "Stephen Meehan", "David Meehan",
		"K-boo Meehan", "Connor Meehan", "Brendan Meehan", "Michelle Meehan",
		"Shelley Meehan", "Gary Meehan", "George Meehan", "Dorothy Meehan", 
		"Moyna Leary", "Gordon Leary", "Alan Leary", "Saravanan R", "Govarthanan M", 
		"Brindha M", "Wayne Moore", "Leonore Herzenberg", "Leonard Herzenberg",
		"Thej K M", "Vijay Hiremath"};
	final Float rateValues[]={
		new Float(1.54), new Float(55.23), new Float(1542.23)};
	final Date birthDateValues[]={
		new Date(), new Date(59, 11, 10), new Date(102, 9, 12),
		new Date(57, 9, 20), new Date(101, 11, 10), null};
	final Vector<Row> data=new Vector<Row>();
	final String[] dataModel=new String[]{
		"Name", "Rate", "IQ", "Birth-date", "Score", "Happy", "Rating"};
	for (int i=0; i < 25; i++){
	  Vector row=new Vector();
	  for (int j=0; j < colRating + 1; j++){
		if (j == colRating){
		  row.add(ratings[(random.nextInt(ratings.length))]);
		}
		else if (j == colName){
		  row.add(nameValues[random.nextInt(nameValues.length)]);
		}
		else if (j == colRate){
		  row.add(rateValues[random.nextInt(rateValues.length)]);
		}
		else if (j == colHappy){
		  row.add(ComparableBoolean.valueOf(random.nextBoolean()));
		}
		else if (j == colBirthDate){
		  row.add(birthDateValues[random.nextInt(birthDateValues.length)]);
		}
		else if (j == colIQ){
		  row.add(new Integer((int) (random.nextInt(256))));
		}
		else if (j == colScore){
		  row.add(new Integer((int) (random.nextInt(1024)) + 4000));
		}
	  }
	  data.add(new ListRow(row){
		public Collection getAllowedValues(int dataColumn){

		  return dataColumn == colRating ? Arrays.asList(ratings) : null;
		}

		public boolean allowNewValue(int dataColumn){
		  return dataColumn != colRating;
		}

	  });
	}
	final ListMetaRow metaRow=new ListMetaRow(Arrays.asList(dataModel),
											  (Row) data.get(0)){
	  public String getDateFormat(int dataColumn){
		return dataColumn == colBirthDate ? "MMM dd yyyy" : null;
	  }

	};
	dataSource=new DataSource(data, metaRow);
	final java.util.List orders=new ArrayList();
	final java.util.List<Integer>dataColumnIndexes=new ArrayList<Integer>();
	dataColumnIndexes.add(colRating);
	Object []o=new Object[]{"Evil", "Freaky", "Friendly", "Mean", "Normal", "Not really nice" };
	orders.add(Basics.toList(o));
	
	dataColumnIndexes.add(colRate);
	o=new Object[]{new Float(55.23), new Float(1.54), new Float(1542.23)};
	orders.add(Basics.toList(o));
	
	dataColumnIndexes.add(colName);
	orders.add(Basics.caseAndNullInsensitive);
	
	
	Collections.sort(data, new RowComparator(orders, dataColumnIndexes));

	// testNonVisualModelProcessing();
	tableModel=PersonalizableTableModel.activate(dataSource,
												 getPropertyFileName(), false);
	tableModel.setKey("test");
	return tableModel;
  }

  DefaultPersonalizableDataSource dataSource;
  String getPropertyFileName(){
	return testingDialog ? "testTableInDialog.properties" :
		"testTableInFrame.properties";
  }

  static boolean testingDialog=false;

  void testTableUtil(){
	try{
	  final SortInfo[] sortInfo;
	  final Map m;
	  final int[] sortColumns;
	  if (PopupBasics.ask("Current sort order?")){
		m=TableBasics.getMapOfSums(tableModel, new int[]{colIQ}
								   , true);
		if (m == null){
		  PopupBasics.alert("No sort order..");
		}
		sortInfo=tableModel.getAllSortInfo();
		sortColumns=SortInfo.convert(sortInfo);
	  }
	  else{
		sortColumns=new int[]{
			colRate};
		m=TableBasics.getMapOfSums(
			(PersonalizableDataSource) dataSource.clone(),
			sortColumns, new int[]{colIQ}
			, true);
		sortInfo=new SortInfo[]{
			new SortInfo(tableModel.getMetaRow(), colRate, 1)};
	  }

	  StringBuilder sb=new StringBuilder("<html><body><table border='1'><tr><td>Key</td><td>count</td><td>sum</td></tr><tr>");
	  for (Iterator it=SortInfo.sort(null, m.keySet(), sortInfo).iterator();
		   it.hasNext(); ){
		final Row row=(Row) it.next();
		final TableBasics.Sum sum=(TableBasics.Sum) m.get(row);
		sb.append("<tr><td>");
		sb.append(TableBasics.encode(row, sortColumns));
		sb.append("</td><td>");
		sb.append(sum.cnt[0]);
		sb.append("</td><td>");
		sb.append(sum.values[0]);
		sb.append("</td></tr>");
	  }
	  sb.append("</tr></table></body></html>");
	  SwingBasics.popUpHtml(tableModel.getTearAwayComponent(), "IQ summary", sb.toString(), false);
	}
	catch (Exception e){
	  System.err.println(e);
	}
  }

  void testNonVisualModelProcessing(){
	// test model processing without visual JTable

	tableModel=PersonalizableTableModel.activate(dataSource, false);
	tableModel.sort(colName, true);
	tableModel.sort(colBirthDate, true);
	tableModel.sort(colRate, true);
	tableModel.setGroupOption(PersonalizableTableModel.GROUP_BY_TREE);
	GroupedDataSource cds=GroupedDataSource.activate(tableModel,
		PersonalizableTableModel.GROUP_BY_TREE, false);
	GroupedDataSource.Node node=cds.getRoot();
	int n=node.getChildCount();
	for (int i=0; i < n; i++){
	  GroupedDataSource.Node level1=(GroupedDataSource.Node) node.getChildAt(
		  i);
	  PersonalizableTableModel.print(level1.groupedRow, System.out);
	}
	//cds.printTree(System.out);
	dataSource.removeFilter();

  }

  PersonalizableTableModel tableModel;
  
  static void testChoiceDialog(final Component cmp, final Point p) {
	  final ChoiceDialog cd;
	  
      if (cmp != null){
    	  cd= new ChoiceDialog("Choose one", "Please choose",  
                                         new String[] {"Child"}
                                         , "Select?", cmp,p.x+cmp.getWidth(), p.y+cmp.getHeight());
      }else{
    	  cd= new ChoiceDialog("Choose one", "Please choose",  
                  new String[] {"Child"}
                  , "Select?");
      }
      final ActionListener answer=new ActionListener() {
  		public void actionPerformed(final ActionEvent e) {
  			if (cd.cancelled){
  				return;
  			}
  			final List<ChoiceDialog.Choice>l=cd.getChoices();
				
  			for (int i = 0; i < l.size(); i++) {
  				final ChoiceDialog.Choice choice=l.get(i);
  				final String name=cd.getLabel(i) ;
  				final boolean b=cd.isChosen(i);
  				assert Basics.equals(choice.label, name);
  				assert Basics.equals(choice.chosen, b);
  				
  				System.out.println(name + "=" + b);
  		      }
  			}
        };
        cd.addChoice("Fergus", false);
      cd.addChoice("Kieran", true);
      cd.addChoice("Connor", false);
      cd.addChoice("Brendan", true);
      cd.addChoice("Aisling", true);
      Basics.debug(Basics.toList(
        Basics.split("this, is / the last time", ",/")
                   )
        );
      cd.show(true, answer, Color.magenta);
  }

  void testPickTreeInModalDialog() {
      testingDialog = true;
      final JDialog dlg = new JDialog(SwingBasics.mainFrame,
                                      "Test pick tree in modal dialog", true);
      dlg.getContentPane().setLayout(new BorderLayout());
      final TestPersonalizableTable pt = new TestPersonalizableTable();
      dlg.getContentPane().add(pt, BorderLayout.CENTER);
      dlg.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
              pt.saveProperties();
              javax.swing.tree.TreePath[] tp = pt.tableModel.
                                               getSelectedTreePaths();
              if (tp != null) {
                  for (int i = 0; i < tp.length; i++) {
                      GroupedDataSource.Node n = (GroupedDataSource.Node) tp[
                        i].
                        getLastPathComponent();
                      System.out.println();
                      System.out.println(n);
                  }
              }
              dlg.dispose();
              testingDialog = false;
          }
      });
      pt.tableModel.setApplicationSpecificTreeSort(new int[] {colBirthDate,
        colRate, colIQ}
        , -1, false, true,  -1, null, false, null, null, null, null);
      PopupBasics.alert(
        "Properties should always revert to pick tree PLUS required sort");
      dlg.pack();
      pt.tableModel.setPersonalizableWindowOwner((Window) dlg);
      com.MeehanMetaSpace.swing.SwingBasics.bottomRight(dlg);
      JButton close = new JButton("close");
      close.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
              com.MeehanMetaSpace.swing.SwingBasics.closeWindow(dlg);
          }
      });
      JPanel jp = new JPanel();
      jp.add(close);
      close.setMnemonic('c');
      dlg.getContentPane().add(jp, BorderLayout.SOUTH);
      dlg.show();

  }

  void saveProperties() {
      if (tableModel.getViewChangeCount() > 0) {
          final Properties prop = tableModel.
                                  updatePropertiesWithPersonalizations(true);

          PropertiesBasics.saveProperties(prop, getPropertyFileName(), "");
      }

  }

  static void download(final String txt, final String part) {
      DownloadService ds;
      final boolean doingPart = !Basics.isEmpty(part);
      try {
          ds = (DownloadService) ServiceManager.lookup(
            "javax.jnlp.DownloadService");
      } catch (final UnavailableServiceException e) {
          ds = null;
      }

      if (ds != null) {
          try {
              // determine if a particular resource is cached
              final URL url = new URL(txt);
              boolean cached = doingPart ?
                               ds.isExtensionPartCached(url, null, part) :
                               ds.isResourceCached(url, null);
              // remove the resource from the cache
              if (cached) {
                  PopupBasics.alert(url.toString() + " is cached");
                  if (doingPart) {
                      ds.removeExtensionPart(url, null, part);
                  } else {
                      ds.removeResource(url, null);
                  }
              }
              // reload the resource into the cache
              final DownloadService ds2 = ds;
              final DownloadServiceListener dsl = ds.getDefaultProgressWindow();
              new Thread(new Runnable() {
                  public void run() {
                      try {
                          if (Basics.isEmpty(part)) {
                              ds2.loadResource(url, null,
                                               new DownloadListenerProxy(dsl));
                          } else {
                              ds2.loadExtensionPart(url, null, part,
                                new DownloadListenerProxy(dsl));
                          }
                          testJnlpTransportedData();
                      } catch (Exception e) {
                          e.printStackTrace();
                      }

                  }
              }).run();

          } catch (Exception e) {
              e.printStackTrace();
          }
      }

  }

  static class DownloadListenerProxy implements DownloadServiceListener {

      final DownloadServiceListener dsl;
      DownloadListenerProxy(final DownloadServiceListener dsl) {
          this.dsl = dsl;
      }

      public void downloadFailed(
        final URL uRL,
        final String string) {
          if (SwingUtilities.isEventDispatchThread()) {
              System.out.println(
                "downloadFailed:  already inside in swing thread:  " +
                string);
              dsl.downloadFailed(uRL, string);
          } else {
              System.out.println(
                "downloadFailed:  Not inside in swing thread:  " +
                string);
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                      System.out.println(
                        "downloadFailed:  Finally inside in swing thread " +
                        string);
                      dsl.downloadFailed(uRL, string);
                  }
              }
              );
          }

      }

      public void progress(
        final URL uRL,
        final String string,
        final long long2,
        final long long3,
        final int int4) {
          if (SwingUtilities.isEventDispatchThread()) {
              System.out.println(
                "progress:  Already inside in swing thread:  " +
                string);
              dsl.progress(uRL, string, long2, long3, int4);
          } else {
              System.out.println("progress:  Not inside in swing thread:  " +
                                 string);
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                      System.out.println(
                        "progress:  Finally inside in swing thread " +
                        string);
                      dsl.progress(uRL, string, long2, long3, int4);
                  }
              }
              );
          }
      }

      public void upgradingArchive(final URL uRL, final String string,
                                   final int int2, final int int3) {
          if (SwingUtilities.isEventDispatchThread()) {
              System.out.println(
                "upgradingArchive:  Already inside in swing thread:  " +
                string);
              dsl.upgradingArchive(uRL, string, int2, int3);
          } else {
              System.out.println(
                "upgradingArchive:  Not inside in swing thread:  " +
                string);
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                      System.out.println(
                        "upgradingArchive:  Finally inside in swing thread " +
                        string);
                      dsl.upgradingArchive(uRL, string, int2, int3);
                  }
              }
              );
          }

      }

      public void validating(
        final URL uRL,
        final String string,
        final long long2,
        final long long3,
        final int int4) {
          if (SwingUtilities.isEventDispatchThread()) {
              System.out.println(
                "validating:  Already inside in swing thread:  " +
                string);
              dsl.validating(uRL, string, long2, long3, int4);
          } else {
              System.out.println("validating:  Not inside in swing thread:  " +
                                 string);
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                      System.out.println(
                        "validating:  Finally inside in swing thread " +
                        string);
                      dsl.validating(uRL, string, long2, long3, int4);
                  }
              }
              );
          }

      }

  }


  public static void main(final String[] args) {
	  Basics.gui=PopupBasics.gui;
	  PersonalizableTableModel.setRootDir(System.getProperty("user.home")+"/test_table");
      SwingBasics.doDefaultLnF();
      SwingBasics.setResizingFactors(1,1,2.5f);
      final ColorPreferences colorProperties = ColorPreferences.instantiate();
		colorProperties.setCurrentPreferences();

      com.MeehanMetaSpace.Pel.init(
        IoBasics.concat(System.getProperty("user.home"), "pel.log"),
        TestPersonalizableTable.class,
        "Table test",
        false);
      SwingBasics.resetDefaultFonts();
      PersonalizableTable.resetDefaultFonts();

      if (args.length == 1 && args[0].equalsIgnoreCase("preferences")) {
          try {
              GlobFilters wc = new GlobFilters(new java.io.File(
                "C:\\\\.cvsignore"));
              final java.io.File dir = new java.io.File("c:\\\\temp");
              String[] ds = dir.list(wc);
              Preferences p = new Preferences();
              p.register(SwingBasics.getButtonPreferences());
              int choice = 0;
              for (; ; ) {

                  p.show("Testing preferences", null);

                  choice = PopupBasics.offerChoiceList(
                    null,
                    "Open Knowledge Base",
                    "<html><body><center>Which knowledge base do<br> you wish to work with?<br>(cancel to return to button preferences)</center></body></html>",
                    new String[] {
                    "herzMouse.pprj",
                    "protocols.pprj",
                  }
                    ,

                    new String[] {
                    "<html><body>This a lab type knowledge base <br>and it is in org/facs.stanford.edu/FACS/</body></html>",
                    "<html><body>This is a researcher type knowledge base<br>users/swmeehan@shaw.ca/FACS/protocols.ppr</body></html>"
                  }
                    ,
                    false,
                    1,
                    null,
                    "fergus.html");
                  if (choice >= 0) {
                      break;
                  }
              }

          } catch (Exception e) {
              e.printStackTrace();
          }
      }
      if (args.length == 0 || !args[0].equalsIgnoreCase("dialog")) {
          runJFrame();
      } else {
          do {
              runJDialog();
          } while (PopupBasics.ask("Reload?"));
          System.exit(0);
      }
  }

  static void noteEntry(final Object o) {
      PopupBasics.alert(
        "<html><body><h1>Result</h1><hr><font color='red'><b>" + o +
        "</b></font>... was entered");
  }

  private static JMenu menu;
  static void runJFrame() {
      final JFrame frame = new JFrame("Meehan family test data (attributes are random)");
      SwingBasics.keepOnSameScreenAs(null, frame);

      /*	 Toolkit.getDefaultToolkit().addAWTEventListener(
         new AWTEventListener() {
         public void eventDispatched(final AWTEvent e) {
           //if (e.getID()==WindowEvent.WINDOW_ACTIVATED    ){
          System.out.println("****"+e );
          //System.out.println("  --->  " + e.getSource());
           //}
          }
        },
        AWTEvent.WINDOW_EVENT_MASK |
         AWTEvent.WINDOW_STATE_EVENT_MASK |
         AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK |
         AWTEvent.INVOCATION_EVENT_MASK
        );*/
      final JMenuBar menuBar = new JMenuBar();
      frame.setJMenuBar(menuBar);
      menu = new JMenu("Edit");
      menu.setMnemonic('e');
      menuBar.add(menu);
      JMenuItem mi = new JMenuItem("test ask/alert");
      menu.setToolTipText("<html><body><center><h3>Well</h3><i>Here is the tool tip</i></center></body></html>");
      mi.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
              if (PopupBasics.ask("See alert")) {
                  PopupBasics.alert("test success");
              }
          }
      });
      menu.add(mi);
      mi = new JMenuItem("test error");
      mi.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
              PopupBasics.alert("Serious flaw", true);
          }
      });
      menu.add(mi);

      JMenuItem mi2 = new JMenuItem("test get int");
      mi2.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
              noteEntry(PopupBasics.getIntegerFromUser("Your IQ",
                new Integer(22)));
          }
      });
      menu.add(mi2);

      mi2 = new JMenuItem("test get float");
      mi2.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
              noteEntry(
                PopupBasics.getFloatFromUser("Enter a float",
                                             new Float(1.3412)));
          }
      });
      menu.add(mi2);

      JMenuItem mi3 = new JMenuItem("test choices");
      mi3.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {

              noteEntry(
                PopupBasics.getChosenString(
                  frame,
                  "From one of the following",
                  "Choose a color...",
                  new String[] {
                  "purple",
                  "red",
                  "blue",
                  "gray",
                  "green",
                  "black"
              }
                , 3, false));
          }
      });
      menu.add(mi3);

      mi3 = new JMenuItem("test auto complete");
      mi3.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {

              noteEntry(
                PopupBasics.getValueFromUser(
                  "Choose a color",
                  "red",
                  new String[] {
                  "purple",
                  "red",
                  "blue",
                  "gray"
              }));
          }
      });
      menu.add(mi3);

      mi3 = new JMenuItem("test get string");
      mi3.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {

              noteEntry(
                PopupBasics.getStringFromUser(
                  "Choose a color",
                  "red"));
          }
      });
      menu.add(mi3);
      ColorPreferences.setColor("Menu.foreground", menu, Color.green);
      menu.setForeground(Color.blue);
      final JMenuItem mi4 = new JMenuItem("test choice dialog");
      mi4.setMnemonic('t');
	  ColorPreferences.setColor("Menu.foreground", mi4, Color.red);
      mi4.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
        	  testChoiceDialog(menu, menu.getLocationOnScreen());
          }
      });
      menu.add(mi4);

      
      mi3 = new JMenuItem("test password");
      mi3.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {

              noteEntry(
                PopupBasics.getPasswordFromUser(
                  frame,
                  "Shhhh",
                  "red"));
          }
      });
      menu.add(mi3);

      mi3 = new JMenuItem("test file chooser");
      mi3.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {

              noteEntry(
                PopupBasics.getFileName("java", "JAVA source files", true));
          }
      });
      menu.add(mi3);

      SwingBasics.addKeepOnSameScreen(menu, frame, null,
                                      PopupBasics.PROPERTY_SAVIOR);

      if (Basics.isMac()) {
/*          MacintoshBasics.handleQuit(new ActionListener() {
              public void actionPerformed(final ActionEvent e) {
                  PopupBasics.alert("Exiting");
                  System.exit(0);
              }
          }
          );*/
      }
      frame.getContentPane().setLayout(new BorderLayout());
      final TestPersonalizableTable pt = new TestPersonalizableTable();
      // Create a number spinner

      frame.getContentPane().add(pt, BorderLayout.CENTER);
      //	Basics.alert(System.getProperty("user.home"));
      frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(final WindowEvent e) {
              pt.saveProperties();
              frame.dispose();
          }
      });

      final JPanel jp = new JPanel();
      final JLabel s = new JLabel();
      jp.add(s);
      pt.tableModel.setSizeInfo(s);
      frame.getContentPane().add(jp, BorderLayout.SOUTH);
      frame.pack();
      pt.tableModel.setPersonalizableWindowOwner((Window) frame);
      frame.show();
  }

  static void runJDialog() {
      final JDialog frame = new JDialog(SwingBasics.mainFrame, true);
      frame.getContentPane().setLayout(new BorderLayout());
      final TestPersonalizableTable pt = new TestPersonalizableTable();
      // Create a number spinner

      frame.getContentPane().add(pt, BorderLayout.CENTER);
      //	Basics.alert(System.getProperty("user.home"));
      frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
              pt.saveProperties();

              frame.dispose();
          }
      });
      pt.tableModel.new PopupMenuItem(
        "Browse apple", false, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
//				pt.tableModel.showColumn(colRate);
              if (PopupBasics.ask("Alert test")) {
                  PopupBasics.alert("waddaya thinkg?", true);
                  PopupBasics.alert("waddaya thinkg?", false);
              } else {
                  RotateTable rt = new RotateTable(pt.table, new int[] {1, 2});
                  final JDialog dlg = SwingBasics.getDialogWith(
                    "Rotating rows", rt, true);
                  dlg.show();
                  pt.tableModel.highlightColumn(colRate);
                  SwingBasics.showHtml("http://developer.apple.com/java");
              }
          }
      }

      , null, true,
        KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK),
        "Go to developer website for Macintosh","This menu item is currently not allowed");

      pt.tableModel.new PopupMenuItem(
        "Launch flow jo", false, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              try {

                  Basics.exec("FlowJo",
                              new java.io.File(
                    "/Users/Admin/Desktop/B2639 B subsets/B2639 11c.jo"), false);
                  if (PopupBasics.ask("Did it work?")) {
                      Runtime.getRuntime().exec(
                        "open -a FlowJo /Users/Admin/Desktop/go.jo");
                  }
              } catch (java.io.IOException ioe) {
              }
          }
      }

      , null, true,
        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
        "Launch flow jo","This menu item is currently not allowed");

      JPanel jp = new JPanel();
      final JButton inspect = new JButton("Inspect jarred stuff.txt");
      inspect.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent ae) {
              TestPersonalizableTable.testJnlpTransportedData();
          }
      });
      jp.add(inspect);

      final JButton jnlpDownload = new JButton("Jnlp download ");
      jnlpDownload.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent ae) {
              final int idx = PopupBasics.getChosenIndex(
                "What kind of resource?",
                new String[] {"resource", "extension"}
                ,
                0);
              if (idx < 0) {
                  return;
              }
              boolean doingPart = idx == 1;
              final String resourceUrl, extensionPart;
              if (doingPart) {
                  resourceUrl = PopupBasics.getStringFromUser(
                    "Enter extension URL",
                    "http://facs.stanford.edu:8080/sciencexperts/domains/FACS/TestTableExt.jnlp");

                  extensionPart = PopupBasics.getStringFromUser(
                    "Enter part",
                    "domains/stuff.jar");
                  if (extensionPart == null) {
                      return;
                  }
              } else {
                  resourceUrl = PopupBasics.getStringFromUser(
                    "Enter extension URL",
                    "http://facs.stanford.edu:8080/sciencexperts/domains/FACS/mms.jar");

                  extensionPart = null;

              }
              if (resourceUrl == null) {
                  return;
              }
              if (PopupBasics.ask("Try thread test/investigation?")) {
                  download(resourceUrl, extensionPart);
                  System.err.println("Exited download() method");
              } else {try{
                      JnlpBasics.download(resourceUrl, extensionPart, null);
                  } catch (java.io.IOException e){

                  } catch (URISyntaxException e) {

                  }
              }
          }
      });
      jp.add(jnlpDownload);
      JButton testModalDialog = new JButton("Test modal pick tree");
      testModalDialog.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
              final GraphicsEnvironment ge = GraphicsEnvironment.
                                             getLocalGraphicsEnvironment();
              GraphicsDevice[] gs = ge.getScreenDevices();

              pt.testPickTreeInModalDialog();
          }
      });
      jp.add(testModalDialog);
      testModalDialog.setMnemonic('t');
      JButton iqDialog = new JButton("IQ summary");
      iqDialog.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
              pt.testTableUtil();
          }
      });
      jp.add(iqDialog);
      iqDialog.setMnemonic('i');

      JButton close = new JButton("close");
      close.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
              com.MeehanMetaSpace.swing.SwingBasics.closeWindow(frame);
          }
      });
      jp.add(close);
      JLabel s = new JLabel();
      jp.add(s);

      JPanel jp2 = new JPanel();
      spinner = new JSpinner();
      String[] items = {
                       "Ant", "Ape", "Bat", "Boa", "Cat", "Cow", "Dog",
                       "elephant",
                       "kitty kat"};
      JComboBox cb = new JComboBox(items);
      // Install the custom key selection manager

      cb.setKeySelectionManager(new MyKeySelectionManager(cb));

      //cb.addKeyListener(new MyKeyListener());

      jp2.add(cb);

// Set its value
      spinner.setValue(new Integer(100));
      jp2.add(spinner);
// Create a list spinner
      SpinnerListModel listModel = new SpinnerListModel(
        new String[] {"really red", "gray", "gruesome blue", "purple",
        "black",
        "blood red", "red", "green", "blue"});
      spinner = new JSpinner(listModel);

// Set its value
      spinner.setValue("blue");
      spinner.setPreferredSize(new Dimension(150, 50));

      jp2.add(spinner);

// Create a date spinner
      SpinnerDateModel dateModel = new SpinnerDateModel();
      spinner = new JSpinner(dateModel);

// Set its value to jan 1 2000
      Calendar calendar = new GregorianCalendar(2000, Calendar.JANUARY, 1);
      spinner.setValue(calendar.getTime());

      jp2.add(spinner);
// Create a calendar object and initialize to a particular hour if desired
      calendar = new GregorianCalendar();
      calendar.set(Calendar.HOUR_OF_DAY, 13); // 1pm

      // Create a date spinner that controls the hours
      dateModel = new SpinnerDateModel(
        calendar.getTime(), null, null, Calendar.HOUR_OF_DAY);
      spinner = new JSpinner(dateModel);

      // Get the date formatter
      JFormattedTextField tf =
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
      javax.swing.text.DefaultFormatterFactory factory =
        (javax.swing.text.DefaultFormatterFactory) tf.getFormatterFactory();
      javax.swing.text.DateFormatter formatter = (javax.swing.text.
        DateFormatter)
        factory.getDefaultFormatter();

      /*
        // Change the date format to only show the hours
        formatter.setFormat(new java.text.SimpleDateFormat("hh:00 a"));
       */
      // Or use 24 hour mode
      formatter.setFormat(new java.text.SimpleDateFormat(
        "yyyy-M-d hh:mm:ss.S z"));
      jp2.add(spinner);
      spinner.setToolTipText("So how is this?");
      MaskFormatter fmt = null;

      // A phone number
      try {
          fmt = new MaskFormatter("###-###-####");
      } catch (java.text.ParseException e) {
      }
      JFormattedTextField tft1 = new JFormattedTextField(fmt);

      // A social security number
      try {
          fmt = new MaskFormatter("###-##-####");
      } catch (java.text.ParseException e) {
      }
      JFormattedTextField tft2 = new JFormattedTextField(fmt);
      jp2.add(tft2);
      // Support a date in the MEDIUM format in the current locale;
      // see e322 Formatting and Parsing a Date Using Default Formats.
      // For Locale.ENGLISH, the format would be Feb 8, 2002.
      tft1 = new JFormattedTextField(new Date());

      // Support a date in the SHORT format using the current locale.
      // For Locale.ENGLISH, the format would be 2/8/02.
      tft2 = new JFormattedTextField(DateFormat.getDateInstance(DateFormat.
        SHORT));
      tft2.setValue(new Date());

      // Support a date with the custom format: 2002-8-2
      JFormattedTextField tft3 = new JFormattedTextField(new SimpleDateFormat(
        "yyyy-M-d"));
      tft3.setValue(new Date());
      tft3.setEditable(true);
      // See also e320 Formatting a Date Using a Custom Format
      jp2.add(tft3);

      pt.tableModel.setSizeInfo(s);
      close.setMnemonic('c');
      frame.getContentPane().add(jp2, BorderLayout.NORTH);
      frame.getContentPane().add(jp, BorderLayout.SOUTH);
      frame.pack();
      pt.tableModel.setPersonalizableWindowOwner((Window) frame);
      frame.show();
      /*		if (Basics.ask("Look at ui defaults")){
       UIManager.LookAndFeelInfo []ui=UIManager.getInstalledLookAndFeels();
       for (int i=0;i<ui.length;i++){
        System.out.println("class name="+ui[i].getClassName() + ", name="+ui[i].getName() );
       }
        }*/
      try {
          tft3.commitEdit();
          System.out.println("tft3=" + tft3.getValue());
      } catch (Exception e) {

      }

  }
}


// Create a read-only combobox

// This key selection manager will handle selections based on multiple keys.
class MyKeySelectionManager implements JComboBox.KeySelectionManager,
FocusListener,
java.awt.event.ActionListener {
  private final JComboBox cb;
  long lastKeyTime = 0;
  String pattern = "";

  public int selectionForKey(char aKey, ComboBoxModel model) {
      // Find index of selected item
      if (!Character.isJavaIdentifierPart(aKey)) {
          System.out.println(aKey + " is not printable");
          return -1;
      }

      int selIx = 01;
      Object sel = model.getSelectedItem();
      if (sel != null) {
          for (int i = 0; i < model.getSize(); i++) {
              if (sel.equals(model.getElementAt(i))) {
                  selIx = i;
                  break;
              }
          }
      }

      // Get the current time
      long curTime = System.currentTimeMillis();

      // If last key was typed less than 300 ms ago, append to current pattern
      if (curTime - lastKeyTime < 700) {
          pattern += ("" + aKey).toLowerCase();
      } else {
          pattern = ("" + aKey).toLowerCase();
      }

      // Save current time
      lastKeyTime = curTime;
      // Search forward from current selection
      for (int i = selIx + 1; i < model.getSize(); i++) {
          String s = model.getElementAt(i).toString().toLowerCase();
          if (s.startsWith(pattern)) {
              return i;
          }
      }

      // Search from top to current selection
      for (int i = 0; i < selIx; i++) {
          if (model.getElementAt(i) != null) {
              String s = model.getElementAt(i).toString().toLowerCase();
              if (s.startsWith(pattern)) {
                  return i;
              }
          }
      }
      if (!model.getElementAt(selIx).toString().toLowerCase().startsWith(
        pattern)) {
          //FocusListener []a=cb.getFocusListeners();
          //cb.setEditable(true);
          System.out.println("creating item =" + pattern);
          java.awt.Toolkit.getDefaultToolkit().beep();
          JOptionPane.showInputDialog(cb, "Entering new item", pattern);
      }
      return -1;
  }

  MyKeySelectionManager(JComboBox cb) {
      this.cb = cb;
      cb.addFocusListener(this);
      //cb.setEditable(true);
      cb.addActionListener(this);
  }

  public void actionPerformed(java.awt.event.ActionEvent e) {
      //System.out.println("action performed");
      //Basics.ask("Create new?");
  }

  public void focusGained(FocusEvent e) {
      /**@todo Implement this java.awt.event.FocusListener method*/
      //cb.setEditable(false);
      //System.out.println("Focus gained, editing is false");
  }

  public void focusLost(FocusEvent e) {
      /**@todo Implement this java.awt.event.FocusListener method*/
      //System.out.println("Focus lost");
      //cb.setEditable(false);
  }

}


// This key listener displays the menu only if the pressed key
// does not select a new item or if the selected item is not unique.
class MyKeyListener extends KeyAdapter {
  public void keyPressed(KeyEvent evt) {
      JComboBox cb = (JComboBox) evt.getSource();

      // At this point, the selection in the combobox has already been
      // changed; get the index of the new selection
      int curIx = cb.getSelectedIndex();

      // Get pressed character
      char ch = evt.getKeyChar();

      // Get installed key selection manager
      MyKeySelectionManager ksm = (MyKeySelectionManager) cb.
                                  getKeySelectionManager();
      if (ksm != null) {
          // Determine if another item has the same prefix
          System.out.println("Searching.." + ksm.pattern);
          int ix = ksm.selectionForKey(ch, cb.getModel());
          boolean noMatch = ix < 0;
          boolean uniqueItem = ix == curIx;

          // Display menu if no matching items or the if the selection is not unique
          if (noMatch || !uniqueItem) {
              cb.showPopup();
          }
      }
  }

}
