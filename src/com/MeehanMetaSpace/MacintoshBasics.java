package com.MeehanMetaSpace;

import java.awt.Frame;
import java.awt.FileDialog;

import java.awt.event.*;
import com.apple.eawt.*;
import com.apple.eio.*;
import com.apple.mrj.MRJOSType;
import com.apple.mrj.MRJFileUtils;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJQuitHandler;
import com.apple.mrj.MRJPrefsHandler;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import java.io.*;
//import edu.stanford.herzenberg.notebook.ProjectMenuBar;


public class MacintoshBasics extends ApplicationAdapter{

    private static MacintoshBasics theAdapter;
    private static com.apple.eawt.Application theApplication;

    private Application mainApp;

    private MacintoshBasics (Application inApp){
        mainApp = inApp;
    }


	public static void main(String[] args) {
		go(args.length > 0 ? args[0] : "http://www.meehanmetaspace.com");
		System.exit(0);
	}

	public static File findApp(final File file) {
        Pel.log.println (">>>>>>>>>> MacintoshBasics.findApp "+ file.getName());
		try {
			final MRJOSType mrjo = MRJFileUtils.getFileType(file);
            final int mrjoInt = FileManager.getFileType (file.getName());
			System.err.print("The MRJOSType for ");
			System.err.print(file.getAbsolutePath());
			System.err.print(" is \"");
			System.err.print(mrjoInt);
			System.err.print("\"");
			File f = MRJFileUtils.findApplication(mrjo);

           // File f = Application.
			System.err.print(" and the app is ");
			System.err.println(f);
			System.err.print("ttxt=");
			System.err.println(MRJFileUtils.findApplication(mrjo));
			System.err.print("FWks=");
			System.err.println(MRJFileUtils.findApplication(new MRJOSType("FWks")));
			return f;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}

  // The main entry-point for this functionality.  This is the only method
     // that needs to be called at runtime, and it can easily be done using      // reflection (see MyApp.java)
      public static void registerMacOSXApplication(Application inApp) {
          if (theApplication == null) {
              theApplication = new com.apple.eawt.Application();
          }

          if (theAdapter == null) {
              theAdapter = new MacintoshBasics(inApp);
          }
         // theApplication.addApplicationListener(theAdapter);
      }


	public static void handleQuit(final ActionListener al) {
		MRJApplicationUtils.registerQuitHandler(new MRJQuitHandler() {
			public void handleQuit() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						al.actionPerformed(null);
					}
				});
			}
		});

	}

	public static void handlePrefs(final ActionListener al) {
		MRJApplicationUtils.registerPrefsHandler(new MRJPrefsHandler() {
			public void handlePrefs() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						al.actionPerformed(null);
					}
				});
			}
		});
	}


	public static void openWithSafari(final String url) throws IOException {
		final String exe = "Safari";
		final String[] cmd;
		cmd = new String[] { "open", "-a", exe, url  };
		Runtime.getRuntime().exec(cmd);

	}

	public static void go(final String url) {
		try {
			System.out.println("Attempting to let MRJ launch " + url);
			FileManager.openURL(url);
			//If this was successful, then we need not go on.
			return;
		} catch (IOException exc) {
            Pel.log.println ("IOException in MacintoshBasics.java");
			//This can occur if problems arise while attempting
			// to open the URL.
		} catch (NoSuchMethodError err) {
            Pel.log.println ("NoSuchMethodException in MacintoshBasics.java");

			// This can occur when earlier versions of MRJ are used which
			// do not support the openURL method.
		} catch (NoClassDefFoundError err) {

             Pel.log.println ("NoClassDef foundException in MacintoshBasics.java");

			//This can occur under runtime environments other than MRJ.
		}
		//If we make it here, MRJ was unsuccessful in opening the URL, and
		//we need to do it the hard way, using Runtime.exec.

		String browserName;

		//Set up a FileDialog for the user to locate the browser to use.
		FileDialog fileDialog = new java.awt.FileDialog(new Frame());
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setTitle("Choose the browser to use:");
		fileDialog.setVisible(true);
		//Retrieve the path information from the dialog and verify it.
		String resultPath = fileDialog.getDirectory();
		String resultFile = fileDialog.getFile();
		if (resultPath != null && resultPath.length() != 0 && resultFile != null && resultFile.length() != 0) {
			File file = new File(resultPath + resultFile);
			if (file != null) {
				browserName = file.getPath();
				try {
					//Launch the browser and pass it the desired URL
					Runtime.getRuntime().exec(new String[] { browserName, url });
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		}
	}
   /*	public static void handleQuit(final ActionListener al) {

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						al.actionPerformed(null);
					}
				});


	}*/

    @Override
    public void handleQuit (ApplicationEvent ae){
        Pel.log.println ("MacintoshBasics handleQuit");
    }


}
