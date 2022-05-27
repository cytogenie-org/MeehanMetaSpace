package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.*;

import javax.swing.ToolTipManager;
import java.awt.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class InteractiveSoftwareProduct implements SoftwareProduct.Gui{

public InteractiveSoftwareProduct(){
  Basics.gui=PopupBasics.gui;
}


  public void alertJnlpUrlNotFound(final SoftwareProduct softwareProduct){
  PopupBasics.alert(softwareProduct. sourceJnlpUrl + "  not found ");
}

	public boolean acquirePassPhrase() {
		for (;;) {
			final Object o = PopupBasics.getPasswordFromUser(null,
					"Enter pass phrase for super user..", null);
			if (o == null) {
				return false;
			}
			if (o.toString().equals("ybabr8r")) {
				return true;
			}
			PopupBasics.beep();
		}
	}

  public void alertMissingLicenseArguments(){
	PopupBasics.alert("Link licensing arguments are incomplete!");
  }

  public void alertUnauthorizedJnlpUrl(final SoftwareProduct softwareProduct){
	PopupBasics.alert(
			   Basics.encodeHtml(
						"Unauthorized JNLP source URL<br>" +
				 softwareProduct.sourceJnlpUrl,
						"  valid websites:  " ,
						"ol",
						SuperUser.sites,
						true));

}

  public void alertJnlpUrlMismatch(final SoftwareProduct softwareProduct){

	SwingBasics.alertHtml(
	true,
	false,
	true,
	"Source JNLP URL mis-match!",
	"The &lt;argument&gt; in the JNLP source file at <b>" +
	softwareProduct. sourceJnlpUrl +
	"</b> does not match " +
	softwareProduct.sourceJnlpUrl
	);
  }


  public void setToolTipTiming(final int initialDelayMilliSecs) {
	setToolTipTiming( -1, initialDelayMilliSecs);
  }

  public void setToolTipTiming(
	  final int dismissDelayMilliSecs,
	  final int initialDelayMilliSecs) {
	try {
	  if (dismissDelayMilliSecs >= 0) {
		ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMilliSecs);
	  }
	  if (initialDelayMilliSecs >= 0) {
		ToolTipManager.sharedInstance().setInitialDelay(initialDelayMilliSecs);
	  }
	}
	catch (Exception e) {
	  Pel.log.warn(e);
	}
  }



public void goodToGo(final SoftwareProduct softwareProduct){
	if (TabImporter.softwareProduct == null) {
  TabImporter.softwareProduct = softwareProduct;
}
setToolTipTiming(Integer.MAX_VALUE, 1300);

  }


  public void alertRenewalSiteUnreachable(final SoftwareProduct softwareProduct, final long diffDays, final String rationale){
	  SwingBasics.alertHtml(
				  false,
				  false,
				  true,
				  softwareProduct.getProductName() + " link renewal site is unreachable",
				  rationale+
				  "<br>System will try to access this server later:  " +
				  softwareProduct.linkRenewalUrl +
				  " not found. <br> Oh ... by the way .. you have " +
				  ( (SoftwareProduct.PERIOD_OFFLINE_DAYS - diffDays) > 0 ?
				   (SoftwareProduct.PERIOD_OFFLINE_DAYS - diffDays) : 0) +
				  " days to renew !!");

	}


	public void alertLinkExpired(final SoftwareProduct softwareProduct, final String rationale){
	  SwingBasics.alertHtml(
	  false,
	  false,
	  true,
	  softwareProduct.getProductName() + " link expired.",
	  rationale+"<br>A fresh link has been emailed to you <br><i>(via " + softwareProduct.linkRenewalUrl+")</i> !!");
	}

	public void alertLinkRenewalMismatch(final SoftwareProduct softwareProduct){
	  SwingBasics.alertHtml(
	true,
	false,
	true,
	"Link renewal URL mis-match!",
	"The license renewal URL &lt;argument&gt; in the JNLP source file at <b>" +
	softwareProduct.sourceJnlpUrl +
	"</b> does not match " +
	softwareProduct.linkRenewalUrl
	);

	}

	public void alertExpiryDateMismatch(
	  final SoftwareProduct softwareProduct){
	  SwingBasics.alertHtml(
					  true,
					  false,
					  true,
					  "Expiry date mis-match!",
					  "The link expiry date &lt;argument&gt; in the JNLP source file at <br><b>" +
					  softwareProduct.sourceJnlpUrl +
					  "</b> does not match <b>" +
					  softwareProduct.licenseExpiryDatetime+"</b>"

					  );

	}

	public static boolean determineAdequateMemory(
	  final SoftwareProduct softwareProduct,
	  final long minimumRequiredTotal,
		final long minimumRequiredFree,
		final boolean emitAlert,
		final String minimumInstalledRAM){
	  final boolean ok;
	  long freeMemory=Runtime.getRuntime().freeMemory();
	  if (freeMemory < minimumRequiredFree){
		Runtime.getRuntime().gc();
		freeMemory=Runtime.getRuntime().freeMemory();
	  }
	  final long totalMemory=Runtime.getRuntime().totalMemory();
	  if (freeMemory < minimumRequiredFree || totalMemory < minimumRequiredTotal){
		if (emitAlert){
		  final StringBuilder sb=new StringBuilder(Basics.startHtmlError(
			  "Low memory"));
		  if (minimumInstalledRAM != null){
			sb.append(softwareProduct.getProductName());
			sb.append(" requires <b><i>");
			sb.append(minimumInstalledRAM);
			sb.append("</i></b> of intalled RAM.<br>");
		  }
		  sb.append("The java virtual machine for ");
		  sb.append(softwareProduct.getProductName());
		  sb.append(" needs ");
		  sb.append(Basics.encode(minimumRequiredTotal));
		  sb.append(" total bytes of memory, and ");
		  sb.append(Basics.encode(minimumRequiredFree));
		  sb.append(" free!!<br>Instead we have <font color='red'>");
		  sb.append(Basics.encode(totalMemory));
		  sb.append("</font> total, and <font color='red'>");
		  sb.append(Basics.encode(freeMemory));
		  sb.append(
			  "</font> free!! <br> You may proceed, but save your work often.");
		  sb.append(Basics.endHtml());
		  PopupBasics.alert(sb.toString());
		}
		ok=false;
	  }
	  else{
		ok=true;
	  }
	  return ok;
	}

	public static boolean determineAdequateScreenResolution(
	  final SoftwareProduct softwareProduct,
	  final int minimumRequiredWidthInPixels,
		final int minimumRequiredHeightInPixels,
		final boolean emitAlert){
	  final boolean ok;
	  final int MIN=50;
	  final Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
	  int h=minimumRequiredHeightInPixels-dim.height,w=minimumRequiredWidthInPixels-dim.width;
	  if (h>MIN || w>MIN){
		if (emitAlert){
		  final StringBuilder sb=new StringBuilder(Basics.startHtmlError(
			  "Inadequate Screen Resolution"));
		  sb.append(softwareProduct.getProductName());
		  sb.append(" requires a screen resolution of <b><i>");
		  Basics.encodePixelDescription(sb, minimumRequiredWidthInPixels,
										minimumRequiredHeightInPixels);
		  sb.append(
			  "</i></b>.<br>Yet your current screen resolution is <font color='red'>");
		  Basics.encodePixelDescription(sb, dim.width, dim.height);
		  sb.append("</font><br><br>Continue?");
		  sb.append(Basics.endHtml());
		  ok=PopupBasics.ask(sb.toString());
		}
		else{
		  ok=false;
		}
	  }
	  else{
		ok=true;
	  }
	  return ok;
	}
	
	public static void handleJava6(final String []args){
		final String JRE_5_URL = "http://www.sciencexperts.com/support/java-install-2.html";
		if( Basics.isJava6) {
			if (!SoftwareProduct.isBeingUsedBySeniorDeveloper(args)){
				PopupBasics
						.alert(
								"<html><center><b><i>Java 5 needs to be installed on the computer for proper functioning of CytoGenie. <br>"
										+ "The program will exit now and redirect you to a web page with instructions how to install Java 5."
										+ "</i></b></center></html>",
								"Alert", false);
				SwingBasics.showHtml(JRE_5_URL);
				System.exit(1);
			} else {
				if (!PopupBasics.ask("<html>You are using JAVA 6 in test mode using a senior developer account.<br>Odd things may happen, ....proceed?</html>")){
					SwingBasics.showHtml(JRE_5_URL);
					System.exit(1);
				}
			}
			
		}

	}

}
