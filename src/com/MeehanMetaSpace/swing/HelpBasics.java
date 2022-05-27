package com.MeehanMetaSpace.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.plaf.ButtonUI;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.HelpListener;
import com.MeehanMetaSpace.IoBasics;


/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class HelpBasics{
  public static final String HELP_URL="Help root folder",
	  HELP_OFFLINE_URL="Help offline root folder";
  public static Properties configurationParameters;

  private static String rootFolder=null;
  private static String helpOfflineRootFolder=null, helpOnlineRootFolder=null;
  public static boolean isHelpInternal = false;
  public static String getHelpOnlineRootFolder(){
	return helpOnlineRootFolder;
  }

  public static void setHelpOnlineRootFolder(final String urlToRootFolder){
	helpOnlineRootFolder=urlToRootFolder;
	rootFolder=null;
  }

  public static void setHelpOfflineRootFolder(final String tryFirstFileFolder,
											  final String trySecondFileFolder){
	if (new File(tryFirstFileFolder).exists()){
	  helpOfflineRootFolder="file://" + tryFirstFileFolder;
	}
	else{
	  helpOfflineRootFolder="file://" + trySecondFileFolder;
	}
  }

  private static boolean consultConfigurationParameters=true;
  public static void consultConfigurationParameters(final boolean ok){
	consultConfigurationParameters=ok;
  }

  public static String getHelpUrl(final String relativeUrl){
	if (relativeUrl.indexOf("://") >= 0){ // not relative
	  return relativeUrl;
	}
	if (rootFolder == null){
	  if (configurationParameters != null){
		if (consultConfigurationParameters){
		  rootFolder=configurationParameters.getProperty(HELP_URL);
		  System.out.println("Using " + rootFolder + " instead of " +
							 helpOfflineRootFolder + "!");
		}
	  }
	  if (Basics.isEmpty(rootFolder)){
		rootFolder=helpOnlineRootFolder;
	  }
	}
	if (rootFolder != null){
	  String url=IoBasics.concat(rootFolder, relativeUrl);
	  if (IoBasics.getURL(url) == null){
		rootFolder=configurationParameters.getProperty(HELP_OFFLINE_URL);
		if (Basics.isEmpty(rootFolder)){
		  rootFolder=helpOfflineRootFolder;
		}
		url=IoBasics.concat(rootFolder, relativeUrl);
	  }
	  return url;
	}
	else{
	  return relativeUrl;
	}
  }

  public static class Button
	  extends JButton
	  implements ActionListener{
	  
	  public boolean isToggleButton = false;
	  public boolean toggleButtonOn = false;
	  public boolean startedByMouse=false;
	  public boolean allowHovering=true;
	  private Map<JMenuItem, Integer> map = new HashMap<JMenuItem, Integer> ();
	private Button(final String relativeUrl, final boolean setAction){
	  super(MmsIcons.getNewHelpIcon());
	  if (setAction) {
		  setAction(getHelpAction());
	  }
	  else {
		  addActionListener(this);			 
	  }
	  SwingBasics.stylizeButton(this);
	  setRelativeUrl(relativeUrl);
	}

	private Button(final String relativeUrl,final Map<JMenuItem, Integer> map, final boolean setAction){
		  super(MmsIcons.getNewHelpIcon());
		  this.map = map;
		  if (setAction) {
			  setAction(getHelpAction());
		  }
		  else {
			  addActionListener(this);			 
		  }
		  SwingBasics.stylizeButton(this);
		  //if (isHelpInternal) {
			  setHelpTopic(relativeUrl);			  
		 // }
		 // else {
		//	  setRelativeUrl(relativeUrl);			  
		 // }
	}
	private Button(final Icon helpIcon, final String relativeUrl,final Map<JMenuItem, Integer> map, final boolean toggleButton, final boolean toggleButtonOn, final boolean setAction, final boolean allowHovering){
		  super(helpIcon);
		  this.map = map;
		  if (setAction) {
			  setAction(getHelpAction());
		  }
		  else {
			  addActionListener(this);			 
		  }
		  this.isToggleButton = toggleButton;
		  this.toggleButtonOn = toggleButtonOn;
		  this.allowHovering = allowHovering;
		  SwingBasics.stylizeButton(this);
		  setHelpTopic(relativeUrl);			  
	}

	public void setHelpTopic(String topic) {
		this.helpTopic = topic; 
	}

	public boolean isWoodSideMenuHelpButton(){
		return isWoodsideMenu;
	}
	public void setUI(ButtonUI ui){
		if(isWoodsideMenu){
			super.setUI(new TaskButton.TaskButtonUI());
		}else{
			super.setUI(ui);
		}
	}
	public boolean isHoveringAllowed(){
		return allowHovering;
	}
	
	private String relativeUrl;
	private String fullUrl;
	private String helpTopic;
	public void setRelativeUrl(final String relativeUrl){
	  this.relativeUrl=relativeUrl;
	  fullUrl=getHelpUrl(relativeUrl);
	  setToolTipText(Basics.toHtmlUncentered("Help", "Click to browse help from web at <br>" + fullUrl+"<br> For Help Contact us, Our Support Contact Number is:  (877) 799-8811 "));
	}

    private boolean ignoreApplicationWideMenu=false;

    public void setIgnoreApplicationWideMenu(final boolean b){
        ignoreApplicationWideMenu=b;
    }
    
    private void performHelpAction(final ActionEvent e) {
    	 if (applicationWideMenuProvider==null || ignoreApplicationWideMenu){
             SwingBasics.showHtml(fullUrl, true);
         } else {
       	  HelpBasics.Button button = (Button)e.getSource();
       	  if(button.isToggleButton) {
      		 if(HelpListener.checkClueTubeIsInvisble()){
        		button.toggleButtonOn = false;
           	 }
      		 else {
      			button.toggleButtonOn = true;	 
      		 }
       	  }
       	  HelpListener listener = new HelpListener(helpTopic+HelpListener.HELP_LABEL);
       	  listener.isHelpInternal = button.isToggleButton;
     		  listener.actionPerformed(null);
         }
    }
    
    public AbstractAction getHelpAction() {
    	return new AbstractAction() { 
        	public void actionPerformed(final ActionEvent e){
        		performHelpAction(e);
      		}
      };
    }
    
	public void actionPerformed(final ActionEvent e){
		performHelpAction(e);
	}
	private boolean isWoodsideMenu=false;
  }
  
  public static void addLabelTextRows(JLabel[] labels, JTextField[] textFields,
          GridBagLayout gridbag, Container container) {
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      int numLabels = labels.length;

      for (int i = 0; i < numLabels; i++) {
          c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
          c.fill = GridBagConstraints.NONE; // reset to default
          c.weightx = 0.0; // reset to default
          container.add(labels[i], c);

          c.gridwidth = GridBagConstraints.REMAINDER; // end row
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          container.add(textFields[i], c);
      }
  } 
  
  
public interface ApplicationWideMenuProvider{
    JPopupMenu get();
}
public static ApplicationWideMenuProvider applicationWideMenuProvider;

public static Button getHelpButton(
		  final RootPaneContainer dialog,
		  final String relativeUrl){
	return getHelpButton(dialog, relativeUrl, false, null);
}



public static Button getHelpButton(
	  final RootPaneContainer dialog,
	  final String relativeUrl,
	  final boolean isWoodsideMenu,
	  final Map<JMenuItem, Integer> map){
	final Button h = new Button(relativeUrl,map, false);
	if (dialog != null){
	  dialog.getRootPane().registerKeyboardAction(
		  h,
		  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
		  JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	h.isWoodsideMenu=isWoodsideMenu;
	return h;
  }

public static Button getHelpButtoWithAction(
		  final RootPaneContainer dialog,
		  final String relativeUrl,
		  final boolean isWoodsideMenu,
		  final Map<JMenuItem, Integer> map){
		final Button h = new Button(relativeUrl,map, true);
		if (dialog != null){
		  dialog.getRootPane().registerKeyboardAction(
			  h,
			  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			  JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
		h.isWoodsideMenu=isWoodsideMenu;
		return h;
	  }
  
public static Button getExternalHelpButton(
	  final RootPaneContainer dialog,
	  final String relativeUrl,
	  final boolean isWoodsideMenu,
	  final Map<JMenuItem, Integer> map,
	  final boolean setAction){
	final Button h = new Button(MmsIcons.getNewHelpIcon(), relativeUrl,map, false, false, setAction, false);
	h.setToolTipText(Basics.toHtmlUncentered("Get help online"));
	if (dialog != null){
	  dialog.getRootPane().registerKeyboardAction(
		  h,
		  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
		  JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	h.isWoodsideMenu=isWoodsideMenu;
	return h;
  }

public static Button getGenieHelpButton(final RootPaneContainer dialog,final String relativeUrl,final boolean isWoodsideMenu, final boolean setAction) {
	final Button h = new Button(SwingBasics.pressedIcon, relativeUrl, null, false, false, setAction, false);
	h.setToolTipText(Basics.toHtmlUncentered("Ask the Genie online"));
	if (dialog != null){
		  dialog.getRootPane().registerKeyboardAction(
			  h,
			  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			  JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
		h.isWoodsideMenu=isWoodsideMenu;
	return h;
  
}

public static Button getClueTubeToggleButton(
		  final RootPaneContainer dialog,
		  final String helpTopic,
		  final boolean isWoodsideMenu, 
		  final boolean buttonStatus, final boolean setAction){
	final Button h = new Button(MmsIcons.getClueTubeIcon(),helpTopic, null, true, buttonStatus, setAction, false);
	h.setToolTipText(Basics.toHtmlUncentered("See help in ClueTube"));
	if (dialog != null){
	  dialog.getRootPane().registerKeyboardAction(
		  h,
		  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
		  JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	h.isWoodsideMenu=isWoodsideMenu;
	return h;
  }

static String screenDir = System.getProperty("user.home");
public static void takeScreenShot(Window owner) {
	String file = PopupBasics.getFileName(
			".jpg", "JPG file", "Save",
		      "Save the ", false, screenDir,
		      null, owner);
	if (file != null) {
		screenDir=file.substring(0,file.lastIndexOf(File.separator));		
	}
	try {
		Thread.sleep(500);		
	}
	catch(InterruptedException e) {}
	if (file != null) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		Rectangle screenRect = new Rectangle(screenSize);

		try {
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(screenRect);
			ImageIO.write(image, "jpg", new File(file));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
 }
}
