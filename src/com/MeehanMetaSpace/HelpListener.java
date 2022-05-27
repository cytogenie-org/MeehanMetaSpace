/* $Id: HelpListener.java,v 1.1 2015/11/26 03:28:42 gautham.woodsidelogic Exp $
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.MeehanMetaSpace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.MeehanMetaSpace.monitor.FileLocking;




/**
 *
 * @author cate
 */
public class HelpListener extends AbstractAction implements ActionListener,
                                                            ChangeListener  {

    /* The TO DO List menu items. */
    public final static String ADJUST_SAMPLE_VOLUME = "ADJUST_SAMPLE_VOLUME";
    public final static String ADJUST_STAINSET_VOLUME = "ADJUST_STAINSET_VOLUME";    //No refe
    public final static String ASSOCIATE_SUBJECTS_SAMPLES = "ASSOCIATE_SUBJECTS_SAMPLES";
    public final static String CATALOG_STAIN_SET="CATALOG_STAIN_SET";
    public final static String CATALOGS="CATALOGS";
    public final static String CG_HOME="CG_HOME";
    private final static String CG_HELP_HOME="CG_HELP_HOME";
    private final static String CG_HELP_HOME_MAC="CG_HELP_HOME_MAC";
    public static String getCGHelpHome() {
    	if (Basics.isMac()) {
    		return CG_HELP_HOME_MAC;
    	}
    	return CG_HELP_HOME;
    }
    public final static String CHANGE_PASSWORD="CHANGE_PASSWORD";
    public final static String CHANNELS="CHANNELS";

    public final static String CONTROL_PANEL = "CONTROL_PANEL";
    public final static String CONFIGURE_TUBE_WELL_LAYOUT_AND_NAMES = "CONFIGURE_TUBE_WELL_LAYOUT_AND_NAMES";
    public final static String CURRENT_TOPIC_FILE="CURRENT_TOPIC_FILE";
    public final static String DETERMINANTS="DETERMINANTS";
    public final static String DILUTION_STEPS = "DILUTION_STEPS";
    public final static String EDIT_NAMES_COMMENTS = "EDIT_CURRENT_PROTOCOL";
    public final static String ENTER_CONTROLS="ENTER_CONTROLS";
    public final static String EXPORT = "EXPORT";
    public final static String FILE_MENU_KB_ACTIONS="FILE_MENU_KB_ACTIONS";
    public final static String FILE_MENU_PROTOCOL_ACTIONS="FILE_MENU_PROTOCOL_ACTIONS";
    public final static String FMO = "FMO";
    public final static String HELP_HOME="CG_HELP_HOME";
    public final static String IMPORT_REAGENTS = "IMPORT_REAGENTS";
    public final static String INSTRUMENT = "INSTRUMENT";
    public final static String INVENTORY = "INVENTORY";
    public final static String INVENTORY_IMPORT = "INVENTORY_IMPORT";    
    public final static String INVENTORY_EDIT = "INVENTORY_EDIT";    
    public final static String KB="KB";
    public final static String KB_LOCKS="KB_LOCKS";
    public final static String MY_PROTOCOLS="MY_PROTOCOLS";
    public final static String MY_REAGENTS="MY_REAGENTS";
    public final static String NAMING_POLICY = "NAMING_POLICY";
    public final static String NEW_PROTOCOL = "NEW_PROTOCOL";
    public final static String PIPETTING="PIPETTING";
    public final static String PLAN_PIPETTING="PLAN_PIPETTING";
    public final static String PLATES="PLATES";
    public final static String PREFERENCES = "PREFERENCES";
    public final static String PRINT = "PRINT";
    public final static String PROTOCOL_OVERVIEW = "PROTOCOL_OVERVIEW";
    public final static String REVIEW="REVIEW";
    public final static String ROUTINE_PROTOCOLS="ROUTINE_PROTOCOLS";
    public final static String SHOW_SPECTRA="SHOW_SPECTRA";
    public final static String SHOW_TUBE_WELLS_LAYOUT="SHOW_TUBE_WELLS_LAYOUT";
    public final static String SPECIFY_DILUTIONS_STEPS="SPECIFY_DILUTIONS_STEPS";
    public final static String SPECTRA_DASHBOARD="SPECTRA_DASHBOARD";
    public final static String SPECTRA_OVERLAP="SPECTRA_OVERLAP";
    public final static String STAIN_SETS = "STAIN_SETS";
    public final static String START_UP="START_UP";
    public final static String SUBJECTS_AND_SAMPLES = "SUBJECTS_AND_SAMPLES";
    public final static String SYSTEM_EXIT = "EXIT";
    public final static String TEMPLATES="TEMPLATES";
	public final static String TODO = "TODO";
    public final static String UNKNOWN="UNKNOWN";
    public final static String VOLUMES = "VOLUMES";
    public final static String WELL_TUBES = "WELL_TUBES";
	public static final String FLUOROCHROMES = "FLUOROCHROMES";
	public static final String STAIN_SET_CONDITION_RULES = "STAIN_SET_CONDITION_RULES";
	public static final String USER_GUIDE = "USER_GUIDE";
	public static final String RELEASE_NOTES = "RELEASE_NOTES";
	public static final String FAQ = "FAQ";
	public static final String HELP_LABEL = "SHOW";
	public static final String ASK_GENIE ="ASK_GENIE";
	
	public static final String CELL_SOURCES = "CELL_SOURCES";
	public static final String TARGET_SPECIES = "TARGET_SPECIES";
	


	public static boolean isHelpInternal= false;
    public static String CLUETUBE_FOLDER, CLUETUBE_BASE_HTTP_URL;

    private Properties helpProperties, helpProperties2;
   /**CurrentScreen enumerated values from FacsXpert
    *   NONE,
		START_UP,
		EDIT_CURRENT_PROTOCOL,
		ENTER_CONTROLS_ETC,
		PLAN_PIPETTING,
		SHOW_TUBE_WELLS_LAYOUT,
		CONFIGURE_TUBE_WELL_LAYOUT_AND_NAMES,
		REVIEW,
		STAIN_SETS,
		SUBJECTS_AND_SAMPLES **/

    private String helpTopic;
    private static FileLocking fileLocking = new FileLocking();
//    private String CurrentTopicFile = help_HOME + CURRENT_TOPIC_FILE; from get properties


    public HelpListener(String topic) {
        super();
        helpTopic = topic;
       

    }
    public HelpListener(){

    }
    
    public interface HelpActionListener {
		public void actionPerformed(String actionString);
	}
	
	private static HelpActionListener helpActionListener;

	public static void registerHelpActionListener(HelpActionListener helpActionListener2) {
		helpActionListener = helpActionListener2;
	}

    public void actionPerformed (ActionEvent e){
        if (helpTopic != null){
        	if (isHelpInternal) {
        		if (helpActionListener != null) {     
                	helpActionListener.actionPerformed(helpTopic);            	
                }
                if(helpTopic.endsWith(HelpListener.HELP_LABEL)){
                	Random r = new Random();
                	int num = r.nextInt(9);
                	fileLocking.writeMessage (helpTopic+num);
                }else{
                	fileLocking.writeMessage (helpTopic);
                }
        	}
        	else {
        		if (!isPropertiesBuilt) {
        			buildProperties2();
        		}
        		if (CLUETUBE_BASE_HTTP_URL == null && helpProperties2 != null) {

        			CLUETUBE_BASE_HTTP_URL = helpProperties2.getProperty(CG_HOME);
        		}
				if (helpTopic.endsWith("SHOW")){
					final String loadString = helpTopic.substring(0,helpTopic.length()-4);
					final String topicSection = CLUETUBE_BASE_HTTP_URL + helpProperties2.getProperty(loadString);
					showHtml(topicSection, false);
				}
				/*else {
					final String topicSection = "file:///" + CLUETUBE_FOLDER + File.separator + helpProperties.getProperty(helpTopic);
					showHtml(topicSection, false);
				}*/
        	}
            
        }
    	
    }
    
    public static boolean checkClueTubeIsInvisble(){
    	return (new File(CLUETUBE_FOLDER+File.separator+"ctOff.txt").exists());
    }

    public void showHtml(final String _url, final boolean useSafariIfMac) {
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
    
    	
    private void buildProperties() {
    	if (CLUETUBE_FOLDER == null){
                System.out.println (" Clue tube folder has not been initialized");
                return;
        }
	    final String helpfile = CLUETUBE_FOLDER + File.separator + "help.properties";
	    FileInputStream fis = null; 
	    try {
	        helpProperties = new Properties();
	        fis = new FileInputStream (helpfile);
	        helpProperties.load (fis);
	        
	    } catch (Exception e){
	        e.printStackTrace();
	        return; 
	    }
	    finally {
	    	if (fis != null) {
	    		try {
	    			fis.close();	    			
	    		}
	    		catch(Exception e) {}
	    	}
	    }
	    isPropertiesBuilt = true;	    
	}
    
    private void buildProperties2() {
    	

    	
	    final String helpfile = CLUETUBE_FOLDER + File.separator + "help2.properties";

	    FileInputStream fis = null; 
	    try {
	        helpProperties2 = new Properties();
	        fis = new FileInputStream (helpfile);
	        helpProperties2.load (fis);
	        
	    } catch (Exception e){
//	        e.printStackTrace();
	        return; 
	    }
	    finally {
	    	if (fis != null) {
	    		try {
	    			fis.close();	    			
	    		}
	    		catch(Exception e) {}
	    	}
	    }
	    isPropertiesBuilt = true;	    
	}

    private boolean isPropertiesBuilt = false;
    public static void actionSystemExit() {
        fileLocking.writeMessage (SYSTEM_EXIT);
    }



    public void setTopic (String topic){

    }

    public void stateChanged(ChangeEvent cs) {
      
    }


}