package com.MeehanMetaSpace.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.IllegalArgumentException;
import com.MeehanMetaSpace.*;
import com.MeehanMetaSpace.swing.*;

public class MapResolverWidget
	extends AbstractMapResolver
	implements ListSelectionListener, MapResolver{
  ArrayList listeners=new ArrayList();

  public static void register(){
	InterfaceBroker.registerImplementation(MapResolver.class, MapResolverWidget.class);
  }

  private static final int DEFAULT_INDEX=0,
	  DEFAULT_VISIBLE_ROWS=6;

  private static Dimension listDimension=new Dimension(200, 100);
  private static Dimension minListDimension=new Dimension(100, 50);

  private JList keyList, actualList, possibleList;
  private JLabel keyLabel, actualLabel, possibleLabel;

  // These are for convenience so we don't have to go casting from ListModel to
  // GenericListModel all the time when we want the model
  private GenericListModel keyListModel, actualListModel, possibleListModel;

  private Component parent;
  private JDialog myDialog;

  private boolean allowCancel=true;

  protected HashMap map=new HashMap();
  protected Results results=null;
  private JPanel mainPanel;

  public MapResolverWidget(Component parent){
	setParent(parent);
  }

  AbstractAction actualize=new AbstractAction("<<"){
	public void actionPerformed(final ActionEvent e){
	  final Object key=keyList.getSelectedValue(),
		  value=possibleList.getSelectedValue();

	  swapValues(MapResolverEnum.ACTUAL, key, value);
	  notifyActualized(key, value);

	}
  }

  , deactualize=new AbstractAction(">>"){
	public void actionPerformed(ActionEvent e){
	  final Object key=keyList.getSelectedValue(),
		  value=actualList.getSelectedValue();

	  swapValues(MapResolverEnum.POSSIBLE, key, value);
	  notifyDeactualized(key, value);
	}
  };
  
  protected Collection<AbstractButton> getButtons(){
	  return Basics.UNMODIFIABLE_EMPTY_LIST;
  }

  private void init(){
	if (mainPanel == null){
	  initGui();
	  setParent(parent);

	  Box listsPanel=new Box(BoxLayout.LINE_AXIS);

	  // First list panel
	  keyListModel=getListModel(null);
	  keyList=getList(alwaysDisabledToolTips, keyListModel, this);
	  JPanel panel=getPanel(keyLabel, getScrollableList(keyList));
	  listsPanel.add(panel);

	  // Second list panel
	  actualListModel=getListModel(null);
	  actualList=getList(disabledToolTips, actualListModel, this);
	  panel=getPanel(actualLabel, getScrollableList(actualList));
	  listsPanel.add(panel);

	  // Add the add/remove buttons
	  JPanel actionPanel=new JPanel();
	  actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
	  JButton b=getActionButton(actualList, deactualize);
	  b.setMargin(new Insets(1, 2, 1, 2));
	  actionPanel.add(b);
	  possibleListModel=getListModel(null);
	  possibleList=getList(disabledToolTips, possibleListModel, this);

	  b=getActionButton(possibleList, actualize);
	  b.setMargin(new Insets(1, 2, 1, 2));
	  actionPanel.add(b);
	  listsPanel.add(actionPanel);

	  // Third list panel
	  panel=getPanel(possibleLabel, getScrollableList(possibleList));
	  listsPanel.add(panel);

	  // Add the lists panel to the overall panel
	  mainPanel.add(listsPanel, BorderLayout.CENTER);

	  // Add the title panel
	  panel=new JPanel(new BorderLayout());
	  panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
	  mainPanel.add(panel, BorderLayout.NORTH);

	  // Add the button panel
	  final JPanel buttonPanel=new JPanel();
for (final AbstractButton btn:getButtons()) {
	buttonPanel.add(btn);
}
	  final JButton ok=SwingBasics.getDoneButton(myDialog);
	  ok.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		  results=getResults();
		}
	  });

	  SwingBasics.setOkButton(ok);
	  buttonPanel.add(ok);
	  if (allowCancel){
			b=SwingBasics.getCancelButton(myDialog,
										  "Discard any changes and close window", true);
			buttonPanel.add(b);
	  }
	  SwingBasics.layout(false, mainPanel, (Icon)null, (JLabel)null, (JPanel)null,
						 buttonPanel);
	}
  }

  MapResolver.Results getResults(){
	if (map.keySet() == null){
	  throw new IllegalStateException("No keys to resolve");
	}
	return new Result(map);

  }

  // ===========================================================================
  // ######################   GUI methods ######################################
  // ===========================================================================
  private void initGui(){
	mainPanel=new JPanel(new BorderLayout());
	//Create and set up the dialog
	myDialog=new JDialog(SwingBasics.mainFrame);
	myDialog.setModal(true);

	// Initialize the labels
	keyLabel=new JLabel();
	actualLabel=new JLabel();
	possibleLabel=new JLabel();
	mainPanel.setOpaque(true);
  }

  private static JPanel getPanel(final JLabel label,
								 final JComponent component){
	JPanel panel=new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	component.setPreferredSize(listDimension);
	component.setMinimumSize(minListDimension);
	panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
	label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
	panel.add(label);
	panel.add(component);

	return panel;
  }

  public MapResolverWidget(){
  }

  private static JButton getActionButton(
	  final JList targetList,
	  final AbstractAction action){
	final JButton b=new JButton(action);
	if (targetList != null){
	  targetList.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent evt){
		  if (evt.getClickCount() == 2){ // Double-click
			action.actionPerformed(
				new ActionEvent(targetList, evt.getID(), "select"));
		  }
		}
	  });
	}
	return b;
  }

  private JList getList(
	  final Map<Object,String> disabled,
	  final GenericListModel listModel,
	  final ListSelectionListener listener,
	  final int selectedIndex,
	  final int visibleRowCount){

	final JList list=new JList(listModel){
	  // This method is called as the cursor moves within the list.
	  public String getToolTipText(final MouseEvent evt){

		// Get item index
		final int index=locationToIndex(evt.getPoint());
		if (!Basics.isEmpty(disabled) && index >= 0){

		  // Get item
		  final Object item=getModel().getElementAt(index);

		  // Return the tool tip text
		  if (disabled.containsKey(item)) {
			  return disabled.get(item);
		  }
		  return enabledToolTips.get(item);
		}
		return super.getToolTipText(evt);

	  }

	};

	list.setVisibleRowCount(visibleRowCount);
	if (listener != null){
	  list.addListSelectionListener(listener);
	}
	if (!Basics.isEmpty(disabled) ){
	  list.setSelectionModel(new MyListSelectionModel(list, disabled));
	  list.setCellRenderer(new MyCellRenderer(disabled));
	}
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	return list;
  }

  private Map<Object,String> disabledToolTips=new HashMap<Object,String>(), 
  	alwaysDisabledToolTips=new HashMap<Object,String>(),
  enabledToolTips=new HashMap<Object,String>();

  public void purgeExplanations(){
	enabledToolTips.clear();
	disabledToolTips.clear();
  }
  public void disable(
		  final Object item,		  
		  final String explanation){
	  alwaysDisabledToolTips.put(item, explanation);
  }
  public void explain(
	  final Object item,
	  final boolean allowChange,
	  final String explanation){
	if (allowChange){
	  disabledToolTips.remove(item);
	  enabledToolTips.put(item, explanation);
	}
	else{
	  enabledToolTips.remove(item);
	  disabledToolTips.put(item, explanation);
	}

  }

  private final class MyListSelectionModel
	  extends DefaultListSelectionModel{
	private final JList list;
	private final Map<Object,String> disabled;
	private MyListSelectionModel(final JList list, Map<Object,String> disabled){
	  this.list=list;
	  this.disabled=disabled;
	}

	public void addSelectionInterval(final int idx0, final int idx1){
	  if (hereAlready){
		super.addSelectionInterval(idx0, idx1);
	  }
	  else{
		_addSelectionInterval(idx0, idx1);
	  }
	}

	public void setSelectionInterval(final int idx0, final int idx1){
	  if (hereAlready){
		super.setSelectionInterval(idx0, idx1);
	  }
	  else{
		_addSelectionInterval(idx0, idx1);
	  }
	}

	private boolean hereAlready;
	private void _addSelectionInterval(int idx0, int idx1){
	  if (!hereAlready){
		hereAlready=true;
		int h=idx0 > idx1 ? idx0 : idx1, l=idx0 < idx1 ? idx0 : idx1;
		for (int i=l; i <= h; i++){
		  final Object item=list.getModel().getElementAt(i);
		  final String toolTip=disabled.get(item);
		  if (toolTip == null ){
			super.addSelectionInterval(i, i);
		  } else {
			list.setToolTipText(toolTip);
			ToolTipOnDemand.getSingleton().show(list, true, 225, 18);
		  }
		}
		hereAlready=false;
	  }
	}
  }

  private final class MyCellRenderer
	  extends JLabel
	  implements ListCellRenderer{
	  private final Map<Object,String>disabled;
	  MyCellRenderer(final Map<Object,String>disabled){
		  this.disabled=disabled;
	  }
	final Color fg=UIManager.getColor("List.foreground"),
		bg=UIManager.getColor("List.background"),
		selectedFg=UIManager.getColor("List.selectionForeground"),
		selectedBg=UIManager.getColor("List.selectionBackground"),
		disabledFg=UIManager.getColor("Label.disabledForeground");
	Icon icon=MmsIcons.getCancelIcon();
	Icon selIcon=MmsIcons.getYesIcon();

	public Component getListCellRendererComponent(JList list, Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus){
		final String txt=value.toString();
	  final boolean isDisabled=disabled.containsKey(value);
	  setOpaque(true);
	  setText(txt);

	  if (isSelected){
		setForeground(selectedFg);
		setBackground(selectedBg);
		//setIcon(selIcon);
	  }
	  else{
		setForeground(isDisabled ? disabledFg : fg);
		setBackground(bg);
	  }
	  if (cellHasFocus){
		setBorder(new CompoundBorder(new LineBorder(new Color(150, 150, 220)),
									 new EmptyBorder(2, 2, 2, 2)));
	  }
	  else{
		setBorder(new EmptyBorder(2, 2, 2, 2));
	  }

	  return this;
	}
  }

  private JList getList(
	  final Map<Object,String>supportToolTips,
	  final GenericListModel listModel,
	  final ListSelectionListener listener){
	return getList(supportToolTips, listModel, listener, DEFAULT_INDEX,
				   DEFAULT_VISIBLE_ROWS);
  }

  public static GenericListModel getListModel(final Collection c){
	GenericListModel listModel=new GenericListModel();
	if (c != null){
	  listModel.addAll(c);
	}
	return listModel;
  }

  private static JScrollPane getScrollableList(final JList list){
	return new JScrollPane(list);
  }

  //This method is required by ListSelectionListener.
  public void valueChanged(final ListSelectionEvent e){
	if (!e.getValueIsAdjusting()){
	  try{
		final JList theList=(JList) e.getSource();
		if (theList == keyList){
		  Object key=keyList.getSelectedValue();
		  valueSelected(key);
		  actualize.setEnabled(false);
		  deactualize.setEnabled(false);
		}
		else if (theList == actualList){
		  deactualize.setEnabled(theList.getSelectedIndex() >= 0);
		}
		else if (theList == possibleList){
		  actualize.setEnabled(theList.getSelectedIndex() >= 0);
		}
	  }
	  catch (final ClassCastException cce){
		// this shouldn't really ever happen . . . famous last words
		cce.printStackTrace();
	  }
	}

  }

  private String title;
  public Results showDialog(final String aTitle){
	init();

	myDialog.setContentPane(mainPanel);
	if (aTitle != null){
	  myDialog.setTitle(aTitle);
	  //Display the window.
	}
	SwingBasics.packAndPersonalize(
		myDialog,
		null,
		null,
		"mapResolverWidget",
		true,
		true,
		false);
	results=null;
	myDialog.show();
	return results;
  }

  public Results showDialog(){
	return showDialog(title);
  }

  // ===========================================================================
  // #######################  Action Functions #################################
  // ===========================================================================
  private void swapValues(MapResolverEnum typeToAddTo,
						  Object key, Object value){
	if (key != null && value != null){
	  Map valuesMap=getValues(key);
	  if (valuesMap != null){
		Collection actualValues=(Collection) valuesMap.get(MapResolverEnum.
			ACTUAL);
		Collection possibleValues=(Collection) valuesMap.get(MapResolverEnum.
			POSSIBLE);
		if (typeToAddTo == MapResolverEnum.ACTUAL){
		  actualValues.add(value);
		  actualListModel.add(actualListModel.size(), value);
		  possibleValues.remove(value);
		  possibleListModel.removeElement(value);
		}
		else{ // typeToAddTo == MapResolverEnum.POSSIBLE
		  possibleValues.add(value);
		  possibleListModel.add(possibleListModel.size(), value);
		  actualValues.remove(value);
		  actualListModel.removeElement(value);
		}
	  }
	}
  }

  private void setValues(MapResolverEnum type, Object[][] values){
	init();
	for (int i=0; i < keyListModel.size(); i++){
	  setValues(type, i, values[i]);
	}
  }

  private void setValues(MapResolverEnum type, int keyIndex, Object[] values){
	Object key=keyListModel.getElementAt(keyIndex);
	Map mappedValues=getValues(key);
	ArrayList row=new ArrayList(Arrays.asList(values));
	mappedValues.put(type, row);
	map.put(key, mappedValues);
	final GenericListModel m=type.equals(MapResolverEnum.POSSIBLE) ?
		possibleListModel:actualListModel;
	m.clear();
	m.addAll(row);

  }

  private void setValues(MapResolverEnum type, Object key, Object[] values){
	int keyIndex=getKeyIndex(key);

	if (keyIndex == -1){
	  keyIndex=keyListModel.size();
	  keyListModel.addElement(key);
	  actualListModel.ensureCapacity(keyIndex + 1);
	  possibleListModel.ensureCapacity(keyIndex + 1);
	  initKey(key);
	}
	setValues(type, keyIndex, values);
  }

  // ===========================================================================
  // ###################      MapResolver methods ##############################
  // ===========================================================================
  public void setKeys(final Object[] keys){
	init();
	// Clear the old list of keys
	keyListModel.clear();
	// Initialize new key array
	keyListModel.addAll(toCollection(keys));
	for (int i=0; i < keyListModel.size(); i++){
	  initKey(keyListModel.get(i));
	}
  }

  final void initKey(final Object key){
	ArrayList actual=new ArrayList(0);
	ArrayList possible=new ArrayList(0);
	Map values=new HashMap();
	values.put(MapResolverEnum.ACTUAL, actual);
	values.put(MapResolverEnum.POSSIBLE, possible);
	map.put(key, values);

  }

  public void setTitle(final String title){
	this.title=title;
  }

  public void setLabels(
	  final String _keyLabel,
	  final String _possibleValues,
	  final String _actualValues){
	init();
	keyLabel.setText(_keyLabel);
	possibleLabel.setText(_possibleValues);
	actualLabel.setText(_actualValues);
  }

  /**
   *
   * @param possibleValues Object[][]
   * @throws IllegalArgumentException IF
   * possibleValues.length != keys.length
   */
  public void setPossibleValues(final Object[][] possibleValues){

	throwupIfSick(possibleValues);
	// At this point we know that we have a set of keys
	// and a set of possible values 'corresponding' to those keys
	setValues(MapResolverEnum.POSSIBLE, possibleValues);
  }

  public void refresh(){
	keyList.updateUI();
	actualList.updateUI();
	possibleList.updateUI();
  }

  public void setPossibleValues(final Object key, final Object[] possibleValues){
	setValues(MapResolverEnum.POSSIBLE, key, possibleValues);
  }

  public void doThe80PercentUseCase(
	  final Object[] actualValues,
	  final Object[] possibleValues){
	throw new UnsupportedOperationException("This feature not yet implemented");
  }

  public Results resolve(){
	return showDialog();
  }

  public void setParent(final Component parent){
	this.parent=parent;
  }

  /**
   *
   * @param actualValues Object[][]
   * @throws IllegalArgumentException IF
   * possibleValues.length != keys.length
   */
  public void setActualValues(Object[][] actualValues){
	// Check our parameters
	throwupIfSick(actualValues);
	// Set the actual values
	setValues(MapResolverEnum.ACTUAL, actualValues);
  }

  public void setActualValues(Object key, Object[] actualValues){
	setValues(MapResolverEnum.ACTUAL, key, actualValues);
  }

  public void setPoint(final Point point){
	myDialog.setLocation(point);
  }

  public void setAllowCancel(boolean ok){
	allowCancel=ok;
  }

  private class Result
	  implements Results{

	Map map;

	protected Result(Map map){
	  this.map=map;
	}

	public final Set getKeys(){
	  return map.keySet();
	}

	public final Collection possibleValues(Object key){
	  Map values=(Map) map.get(key);
	  return (Collection) values.get(MapResolverEnum.POSSIBLE);
	}

	public final Collection actualValues(Object key){
	  Map values=(Map) map.get(key);
	  return (Collection) values.get(MapResolverEnum.ACTUAL);
	}
  }

  // ===========================================================================


  // ===========================================================================
  // ####################### Utility Methods ###################################
  // ===========================================================================
  private void throwupIfSick(Object[][] values){
	if (keyList == null || values == null ||
		(keyListModel.size() != values.length)){
	  throw new IllegalArgumentException("The number of keys (" +
										 keyListModel.size() + ") is " +
										 "not equal to the number of values (" +
										 values.length + ")");
	}
  }

  // Utility method so we don't have to cast all the time
  private Map getValues(Object key){
	return (Map) map.get(key);
  }

  private int getKeyIndex(Object key){
	if (keyListModel != null){
	  for (int i=0; i < keyListModel.size(); i++){
		Object io=keyListModel.getElementAt(i);
		if (io == key){
		  return i;
		}
	  }
	}
	return -1;
  }

  private static Collection toCollection(final Object[] array){
	Collection c=null;
	if (array != null){
	  c=new ArrayList(array.length);
	  for (int i=0; i < array.length; i++){
		c.add(array[i]);
	  }
	}
	return c;
  }

  private static Collection toCollection(Object[][] array){
	Collection row, everything=null;
	if (array != null){
	  everything=new ArrayList(array.length);
	  for (int i=0; i < array.length; i++){
		row=new ArrayList(array[i].length);
		for (int j=0; i < array[i].length; j++){
		  row.add(array[i][j]);
		}
		everything.addAll(row);
	  }
	}
	return everything;
  }

  private static Object[][] buildPossibleValues(Object[] list){
	Object[][] possibleValues=new Object[list.length][list.length - 1];
	int found;

	for (int i=0; i < list.length; i++){
	  found= -1;
	  for (int j=0; j < list.length - 1; j++){
		if (i == j){
		  found=i;
		  possibleValues[i][j]=list[i + 1];
		}
		else{
		  possibleValues[i][j]=found == -1 ? list[j] : list[j + 1];
		}
	  }
	}
	return possibleValues;
  }

  private static String[] colors={
	  "red", "orange", "blue", "magenta", "papaya whip"};
  private static String[] states={
	  "California", "Arkansas", "Nevada", "Maryland", "New Hampshire",
	  "Florida"};
  private static String[] powerForwards={
	  "Kevin Garnett", "Ben Wallace", "Amare Stoudamire", "Tim Duncan",
	  "Dirk Nowitzki", "Jermaine O'Neal"};

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
  private static void createAndShowGUI(){
	// Make sure we have nice window decorations.
	JFrame.setDefaultLookAndFeelDecorated(true);

	// Create and set up the window.
	JFrame frame=new JFrame("MapResolverWidget");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	JDialog dialog=new JDialog(SwingBasics.mainFrame);
	dialog.setModal(true);

	// Create and set up the content pane.
	final MapResolver mrInterface=new MapResolverWidget();

	mrInterface.setLabels("Favor", "Choices", "Over");
	mrInterface.setKeys(powerForwards);
	mrInterface.setPossibleValues(buildPossibleValues(powerForwards));
	mrInterface.explain(powerForwards[1], false, "Bad dude");

	mrInterface.explain(powerForwards[3], true, "Good dude");
	mrInterface.setTitle("NBA power forward preferences . . .");

	final MapResolver.Results results=mrInterface.resolve();
	if (results != null){
	  for (Iterator iter=results.getKeys().iterator(); iter.hasNext(); ){
		Object key=iter.next();
		System.out.println(key);
		System.out.print("Possible: ");
		for (Iterator pos=results.possibleValues(key).iterator(); pos.hasNext(); ){
		  System.out.print(pos.next() + ",");
		}
		System.out.println();
		System.out.print("Actual: ");
		for (Iterator act=results.actualValues(key).iterator(); act.hasNext(); ){
		  System.out.print(act.next() + ",");
		}
		System.out.println();
	  }
	}
	System.exit(1);
  }

  public static void main(final String[] args){
	SwingBasics.doDefaultLnF();
	//Schedule a job for the event-dispatching thread:
	//creating and showing this application's GUI.
	javax.swing.SwingUtilities.invokeLater(new Runnable(){
	  public void run(){
		createAndShowGUI();
	  }
	});
  }
  
  public void setKey(final Object key) {
	  keyList.setSelectedValue(key, true);
  }
}
