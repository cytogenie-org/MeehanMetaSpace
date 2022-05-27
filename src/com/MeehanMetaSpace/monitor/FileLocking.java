/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.MeehanMetaSpace.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.Properties;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.HelpListener;
import com.MeehanMetaSpace.JnlpBasics;




/**
 *
 * @author cate
 */

public class FileLocking {

    class FileMonitor extends AbstractMonitor {
        private String filename;
        private FileOutputStream fos;
        private FileLock fl;
        private FileLockAssertion assertion;

        class FileLockAssertion extends Assertion {
             @Override
            public boolean isTrue() {
//     System.out.println ("FileLockAssertion ");
                boolean flag = true;
                if (fl != null && fl.isValid()){
                    flag=true;
                }
                
                    
//     System.out.println ("My Assertion isTrue "+ flag);

                return flag;
            }

        }
        FileMonitor (String filename){
            this.filename = filename;
            setup ();
            assertion = new FileLockAssertion();

        }
        private void setup () {
//            System.out.println ("setup file locking");
            try {
                fos = new FileOutputStream (filename, false);
                fl = fos.getChannel().tryLock();
//                if (fl.isValid())
//                    fl.release();
            } catch (final Exception e2){
//                System.out.println (" Error in set up "+ e2.getMessage());
            }
        }
        @Override
        public boolean invariant() {
//            System.out.println (" invariant == "+ assertion.isTrue());
            return assertion.isTrue();
        }

       /*
          if the file lock is not valid, then what
        * the lock becomes not valid after it has been released, after the channel
        * has been closed or upon termination of the JVM.
        */
        private FileLock getLock() {
            try {
                if (fos == null){
                    //System.out.println ("fos is null");
                    fos = new FileOutputStream (filename, false);
                }
//       System.out.println (" is the lock valid now?  "+ fl.isValid() + "  "+ fos.toString());
                if (fl != null && !fl.isValid() ){

                    if (fos.getChannel() == null)
                        System.out.println ("The channel is null");
                    else
                        fl  = fos.getChannel().tryLock();
                }
                if (fl != null && fl.isValid()){
                    fl.release();
                    fl = fos.getChannel().lock();
                }
//
//            return fl;
            } catch (Exception e1) {
                //System.out.println ("Exception in get Lock "+ e1.getMessage());
            }
            return fl;
        }


        public void enter (String message) {
          super.enter();
//   System.out.println (" Enter the monitor");
          fl = getLock();
          /* Lock is null when it cannot be gotten */
          if (fl == null){
//              System.out.println (" Lock is null");
           
          }
          /* isValid means that the file is locked  */
          else if (fl.isValid()){
//              System.out.println (" file lock is valid");
              writeMessage (message);

          }
          else {
//              System.out.println ("In the monitor, write the message "+ message);

             writeMessage(message);
             
          }
          leave();
        }

        @Override
        public void leave(){
            super.leave();

        }
        private void writeMessage ( String message){
        	if (message==null||fos==null)return;
            //String endofline="\n";
        try {
//System.out.println ("private Write Message "+ message + " "+ fos.toString());
            fos.write (message.getBytes(), 0, message.length());
            //fos.write (endofline.getBytes());
            fl.release();
            fos.close();
            fos = null;
        } catch (IOException ioe){
            System.out.println ("Exception in write message " + ioe.getMessage());
        }
    }

    }


    
    private String filename;
    private FileLock fl;
    private FileOutputStream fos;
    private FileMonitor monitor;
    private Properties helpProperties;
    private File currentTopicFile;
    private static String PRODUCT_FOLDER_NAME = Basics.isMac() ? ".ScienceXperts" : "ScienceXperts";
    private static String USER_HOME_FOLDER = System.getProperty("user.home");
    private static String CLUETUBE_FOLDER = USER_HOME_FOLDER + File.separator + PRODUCT_FOLDER_NAME + File.separatorChar +JnlpBasics.getDNSFolder()+File.separatorChar +"ClueTube";
    
    public FileLocking() {
       buildProperties();
       this.filename = CLUETUBE_FOLDER + File.separator + 
       			helpProperties.getProperty("CURRENT_TOPIC_FILE");      
       monitor = new FileMonitor (this.filename);
       setUpCurrentTopicFile(this.filename);
    }

    /* what I really want to do is to empty the file of its contents,
     * and close it.  Alternatively during start up of clue tube, clue
     * tube reads the help.properties file, get the url for CG_HELP_HOME,
     * opens the file, empties it.  Clue Tube can initialize itself with
     * CG_HeLP_HOME.
     * */
    public void closeAndCleanUp () {
        if (fos != null){
            try {
                if (fl != null )
                    fl.release();
                fos.close();

            } catch (Exception e2) {
                System.out.println ("Error trying to close the fos");

            }

        }
    }

    /**getLock is  entering the monitor
     *
     * @param filename
     * @return
     */
    private FileLock getLock(String filename) {
        try {
            fos = new FileOutputStream (filename);
//            System.out.println ("get lock ");
            fl = fos.getChannel().lock();
        } catch (Exception e){
            System.out.println ("get lock exception "+ e.getMessage() + e.toString());
            //return fl;
        }
        return fl;
    }

    public void writeMessage ( String message){
//        System.out.println (" public Write Message:  "+ message);
        monitor.enter (message);

    }

    private void buildProperties() {

    String helpfile =  CLUETUBE_FOLDER + File.separator + "help.properties";
    try {
    	//System.out.println("helpFile : " + helpfile);
        helpProperties = new Properties();
        FileInputStream fis = new FileInputStream (helpfile);        
        helpProperties.load (fis);
        Enumeration keys = helpProperties.propertyNames();
        for ( keys=helpProperties.propertyNames(); keys.hasMoreElements();){
            String key = (String) keys.nextElement();
            String value = helpProperties.getProperty (key);            
        }

    } catch (Exception e){
        //System.out.println ("build properties exception "+ e.getMessage());
        //e.printStackTrace();
    }

}

    private void setUpCurrentTopicFile(String filename){

        currentTopicFile = new File (filename);
        //if (currentTopicFile.exists()){
          //  boolean flag = currentTopicFile.delete();
//            System.out.println (flag + " file deleted " + filename );
        //}
        //currentTopicFile = new File (filename);
        this.writeMessage (HelpListener.getCGHelpHome());


    }

    
    

  public static void main(String[] args) throws Exception {
      FileLocking filelocking = new FileLocking ();
      String []msgs = {"Message 1", "Message 2", "Message 3", "Message 4", "Message 5",
                       "Message 6", "Message 7", "Message 8", "Message 9", "Message 10"};
      for (int i=0; i < 20; i++){
//          System.out.println ("main message = "+ msgs[i%10] + " i = "+ i);
          filelocking.writeMessage (msgs[i%10] + i);
          Thread.sleep (2000);
      }
      filelocking.closeAndCleanUp();

     }
}

