package com.MeehanMetaSpace;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

/*
 -skgPrimaryRootDirectoryURL c:\FacsXpertBeta3\KB
 */
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;


public class SoftwareProduct {
    public static void register(final Class versionClass) {

        InterfaceBroker.registerImplementation(
          SoftwareProduct.Version.class,
          versionClass);
    }

    public interface Version {

        /**
         *
         * @return String
         *MUST* match one of the ReleasceCycle static enum objects
         this object can not be referenced here because of the protege dependencies
           which are not part of the servlet environment

          public static final ReleaseCycle ALPHA= new ReleaseCycle("alpha");
         public static final ReleaseCycle BETA= new ReleaseCycle("beta");
         public static final ReleaseCycle CANDIDATE= new ReleaseCycle("candidate");
         public static final ReleaseCycle PRODUCTION= new ReleaseCycle("production");


         */


        String getReleaseCycle();

        int getMinorVersion();

        float getMajorVersion();

        String[] getCoreApplicationJars();

        String[] getSupplementalJars();

        String getProducerName();
        
        String getExternalProducerName();

        String getProductName();

        String getPreviousProductName();

        void setLicense(String license);

        String getLicense();

        String getLoadingUrl();

        String getPasswordManagementUrl(final String email, final String sourceJnlp);
        String getDesktopFileName();
        String getDesktopIcon();
    }


    public interface Gui {

        void goodToGo(SoftwareProduct softwareProduct);

        void alertJnlpUrlNotFound(SoftwareProduct softwareProduct);

        void alertUnauthorizedJnlpUrl(SoftwareProduct softwareProduct);

        void alertMissingLicenseArguments();

        void alertJnlpUrlMismatch(SoftwareProduct softwareProduct);

        void alertRenewalSiteUnreachable(SoftwareProduct softwareProduct,
                                         long diffDays, String rationale);

        void alertLinkRenewalMismatch(SoftwareProduct softwareProduct);

        void alertExpiryDateMismatch(SoftwareProduct softwareProduct);

        boolean acquirePassPhrase();

        void alertLinkExpired(SoftwareProduct softwareProduct,
                              final String rationale);

    }


    public final static String
      ARG_IS_DEMO = "isDemo",
    ARG_ADVANCED_USER = "advancedUser",
    ARG_LICENSE_EXPIRY = "licenseExpiry",
    ARG_LICENSE_RENEWAL_URL = "licenseRenewalUrl",
    ARG_SOURCE_JNLP_URL = "sourceJnlpUrl",
    ARG_LICENSEE = "licensee",
    ARG_LICENSE = "license",
    ARG_PASSWD = "passwd",
    ARG_SKIP_LICENSING = "skipLicensing",
    ARG_USER_PASS = "skgUserPass",
    ARG_USER_KB = "skgUserKB",
    ARG_USER_PROTOCOL = "skgUserProtocol",
    ARG_CLUETUBE = "ClueTube";
    
    public static boolean isAdvancedUser = false;
    public static String emailID = null;

    public static SoftwareProduct New(
      final Class mainClass,
      final String[] args,
      final String subSystemName,
      final String buildTimeText,
      final long buildTime,
      final String propertyName_for_productDocumentDir,
      final String[] authors,
      final Gui gui) {
        return New(
          mainClass,
          new Args(args),
          subSystemName,
          buildTimeText,
          buildTime,
          propertyName_for_productDocumentDir,
          authors,
          gui);
    }

    public static SoftwareProduct New(
      final Class mainClass,
      final Args args,
      final String subSystemName,
      final String buildTimeText,
      final long buildTime,
      final String propertyName_for_productDocumentDir,
      final String[] authors,
      final Gui gui) {
        return New(
          mainClass,
          args,
          subSystemName,
          buildTimeText,
          buildTime,
          propertyName_for_productDocumentDir,
          null,
          authors,
          gui);

    }

    public static void setLicensing(final Args args, final boolean on) {
        args.put(SoftwareProduct.ARG_SKIP_LICENSING, on ? "false" : "true");
    }

    public static SoftwareProduct New(
      final Class mainClass,
      final Args args,
      final String subSystemName,
      final String buildTimeText,
      final long buildTime,
      final String propertyName_for_productDocumentDir,
      final String authorLabel,
      final String[] authors,
      final Gui gui
      ) {
        final String
          argDefaultValueAdvancedUser = "false",
                                        argDefaultValueLicensee = "*";

        final String usageFlaws = args.checkUsage(
          new Args.Expect[] {
          new Args.Expect(
            ARG_LICENSEE,
            argDefaultValueLicensee,
            "Name of licensee"),
          new Args.Expect(ARG_ADVANCED_USER, argDefaultValueAdvancedUser,
                          "Name of protege workbook ")
        });
        if (usageFlaws != null) {
            return null;
        }
        emailID = args.get(ARG_LICENSEE);
        isAdvancedUser = "aaronbkantor@aol.com".equalsIgnoreCase(emailID)?true:args.getBoolean(ARG_ADVANCED_USER);
        return new SoftwareProduct(

          args.getBoolean(ARG_SKIP_LICENSING),
          mainClass,
          subSystemName,
          args.get(ARG_LICENSE),
          emailID,
          args.get(ARG_LICENSE_EXPIRY),
          args.get(ARG_LICENSE_RENEWAL_URL),
          args.get(ARG_SOURCE_JNLP_URL),
          isAdvancedUser,
          buildTimeText,
          buildTime,
          propertyName_for_productDocumentDir,
          authorLabel,
          authors,
          gui
          );
    }

    private static Properties _properties;

    public final void setUserProperties(final Properties properties, final File f) {
        _properties = properties;
        userPropertiesFile = f;
    }

    public final Properties getUserProperties() {
        return _properties != null ? _properties :
          PropertiesBasics.loadProperties(userPropertiesFile);
    }

    public final void setUserProperty(final String key, final String value) {
        final Properties p = getUserProperties();
        p.setProperty(key, value);
        PropertiesBasics.saveProperties(p, userPropertiesFile.getAbsolutePath(), null);
    }

    public final String getDirOnInternationalMachine(
      final String title,
      final String toolTip,
      final String lookingFor) {
        String dir = lookingFor;
        if (!new File(dir).exists()) {
            final Properties p = getUserProperties();
            dir = p.getProperty(lookingFor);
            if (Basics.isEmpty(dir) || !new File(dir).exists()) {
                try {
                    final LookAndFeel lnf = UIManager.getLookAndFeel();
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    dir = Basics.gui.getDirName(
                      title,
                      lookingFor,
                      "Select",
                      toolTip,
                      null);
                    UIManager.setLookAndFeel(lnf);
                    if (dir != null) {
                        p.setProperty(lookingFor, dir);
                        System.out.println("S E T T I N G   property " + lookingFor +
                                           " to " +
                                           dir + " in " +
                                           userPropertiesFile.getAbsolutePath());
                        PropertiesBasics.saveProperties(p,
                          userPropertiesFile.getAbsolutePath(), null);
                        System.out.println("====>" + p.getProperty(lookingFor));
                    }
                } catch (Exception e) {

                }
            }
        }
        return dir;
    }

    /*  deprecated code - to be removed
    public static String[] priorPrefixesToPurgeLauncher;
    private final Collection<String> prefixes = new ArrayList();
    public String saveJnlpOnDesktop(final Collection jnlpLinesOfText) {
        final String fileNamePrefix = IoBasics.replaceFilenameAllergicChars(
          makeTradeMarkAndRightsReservedPrintable(
            getProducerAndProductName()) + ", ");
        prefixes.add(fileNamePrefix);
        Basics.addAll(prefixes, priorPrefixesToPurgeLauncher);
        final String fileName = fileNamePrefix +
                                IoBasics.replaceFilenameAllergicChars(
                                  makeTradeMarkAndRightsReservedPrintable(
                                    getReleaseCycle() +
                                    " " +
                                    getVersionText()));
        final String jnlpPath;
        final String jnlpFileName = fileName + ".jnlp",
                                    pd = getProductDir();
        purgeOldFiles(pd);
        jnlpPath = IoBasics.translateToNativeSlash(IoBasics.concat(pd,
          jnlpFileName));

        IoBasics.saveTextFile(jnlpPath, jnlpLinesOfText);
        return jnlpPath;
    }

    public void purgeOldFiles(String pd) {
        if(!prefixes.isEmpty()) {

            // Purge old Jnlps from productDir
            final File pdf = new File(pd);
            for (final String prefix : prefixes) {
                IoBasics.purgeFiles(pdf, prefix, ".jnlp", false);
            }

            // Purge old Shortcut file on desktop
            final File dtf = new File(getDesktopDir());
            for (final String prefix : prefixes) {
                IoBasics.purgeFiles(dtf, prefix, " Launcher.html", false);
                IoBasics.purgeFiles(dtf, prefix, "", false);
            }
        }
    }

    public File saveLaunchHtml(
      final String renewalUrl,
      final String jnlpPath,
      final String htmlDir,
      final String htmlFileName,
      final String emailAddress) {
        // Purge old Shortcut file on desktop only if we are online
        // and we were able to get the latest index.jsp from the server
        final String suffix = " Launcher.html";
        final File dtf = new File(getDesktopDir());
        if(!prefixes.isEmpty()) {
            for (final String prefix : prefixes) {
                IoBasics.purgeFiles(dtf, prefix, suffix, false);
            }
        }

        return new File(jnlpPath);
    }
*/

    private final String authorLabel, productName,
    
    releaseCycle,
    userHomeDir,
    buildTimeText,
    productDir;
    private final String[] authors;
    private File userPropertiesFile;
    private final Version version;

    public Version getVersion() {
        return version;
    }

    private final String license;
    private String licensee;

    private final long buildTime;

    public String getReleaseCycle() {
        return releaseCycle;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public float getMajorVersion() {
        return majorVersion;
    }

    private final boolean licenseeIsEmailAddress;
    private boolean isSuperUser;

    public boolean isSuperUser() {
        return isSuperUser;
    }

    private static SoftwareProduct _current;

    public static SoftwareProduct getCurrent() {
        return _current;
    }
       
    private final float majorVersion;
    private final int minorVersion;
    private final Gui gui;

    private static Collection<String> avoidPriorHomeDirectories, avoidPriorDocDirectories;
    public static void copyPrior(
      final String[] avoidPriorHomeDirectories,
      final String[] avoidPriorDocDirectories) {
        SoftwareProduct.avoidPriorHomeDirectories = Basics.toList(
          avoidPriorHomeDirectories);
        SoftwareProduct.avoidPriorDocDirectories = Basics.toList(
          avoidPriorDocDirectories);
    }

    private VersionFolder priorVersionFolder;
    public boolean isMajorVersionChange() {
        if (priorVersionFolder != null) {
            final float m = majorVersion - priorVersionFolder.getMajorVersion();
            return m >= 0.1;
        }
        return true; // no version change ... first version .. yes this is major!!
    }
    
    public void setSuperUser(final boolean b){
    	this.isSuperUser=b;
    }


    private SoftwareProduct(
      final boolean skipLicensing,
      final Class mainClass,
      final String productName,
      final String license,
      final String licensee,
      final String licenseExpiryDatetime,
      final String linkRenewalUrl,
      final String sourceJnlpUrl,
      final boolean needSuperUserPrivileges,
      final String buildTimeText,
      final long buildTime,
      final String propertyName_for_productDocumentDir,
      final String authorLabel,
      final String[] authors,
      final Gui _gui) {
        _current = this;
        gui = _gui;
        if (Basics.isEmpty(buildTimeText)) {
            throw new IllegalArgumentException("Must provide a buildTime argument");
        }
        if (Basics.isEmpty(licensee)) {
            throw new IllegalArgumentException("Must provide a licensee argument");
        }
        if (needSuperUserPrivileges) {
            if (SuperUser.isTheSuperUser(licensee)) {
                this.isSuperUser = true;
            } else {
                this.isSuperUser = gui == null ? false : gui.acquirePassPhrase();
            }
        } else {
            this.isSuperUser = false;
        }

        final Version _version = (Version) InterfaceBroker.getImplementation(Version.class,
          mainClass);
        version = _version == null ?
                  new DefaultVersionImplementation() :
                  _version;
        version.setLicense(license);
        if (Basics.isEmpty(productName)) {
            this.productName = version.getProductName();
        } else {
            this.productName = productName;
        }
        if (Basics.isEmpty(this.productName)) {
            throw new IllegalArgumentException(
              "The productName argument is missing.");
        }
        this.releaseCycle = version.getReleaseCycle();
        this.majorVersion = version.getMajorVersion();
        this.minorVersion = version.getMinorVersion();
        if (Basics.isEmpty(version.getProducerName())) {
            throw new IllegalArgumentException("Must provide a producerName argument");
        }

        if (Basics.isEmpty(releaseCycle)) {
            throw new IllegalArgumentException("Must provide a releaseCycle argument");
        }

        this.authors = authors;
        this.authorLabel = authorLabel;
        this.license = license;
        if (Basics.isEmpty(licensee) ||
            licensee.indexOf('@') < 1 ||
            licensee.indexOf('.') < 1) {
            this.licensee = System.getProperty("user.name");
            System.err.println(licensee +
                               " does not appear to be a valid email address");
            this.licenseeIsEmailAddress = false;
        } else {
            this.licensee = licensee;
            this.licenseeIsEmailAddress = true;
        }
        this.buildTimeText = buildTimeText;
        this.buildTime = buildTime;
        String value;
        try {
            value = System.getProperty("user.home");
        } catch (SecurityException e) {
            value = null;
        }
        userHomeDir = value;
        userPropertiesFile = new File(IoBasics.concat(userHomeDir,
          "user.properties"));
        String producerDir = userHomeDir + File.separatorChar +
                             getProducerFolderName(version),
                             productDir = producerDir + File.separatorChar +
                                          getProductSubDirName();
        priorVersionFolder = VersionFolder.upgradeDir(
          getProductSubDirName(),
          removeTradeMarkAndRightsReserved(this.productName),
          removeTradeMarkAndRightsReserved(version.getPreviousProductName()),
          producerDir,
          avoidPriorHomeDirectories);

        VersionFolder.upgradeDir(
          getProductSubDirName(),
          removeTradeMarkAndRightsReserved(this.productName),
          removeTradeMarkAndRightsReserved(version.getPreviousProductName()),
          getDocumentProducerDir(version),
          avoidPriorDocDirectories);
        IoBasics.mkDirs(productDir);
        productDir += File.separatorChar;
        this.productDir = productDir;
        if (!Basics.isEmpty(propertyName_for_productDocumentDir)) {
            System.setProperty(propertyName_for_productDocumentDir, // communicate to applications like Protege
                               establishDocumentDir());
        }
        Pel.init(
          establishProductSubDir(SUBDIR_LOGS),
          mainClass,
          getFullTextInfo() + Basics.getSystemStory(), true);
        this.sourceJnlpUrl = sourceJnlpUrl;
        purgeDownloads();
        this.licenseExpiryDatetime = licenseExpiryDatetime;
        this.linkRenewalUrl = linkRenewalUrl;
        //sourceJnlpUrl will be null if called by AccountManager
        if(sourceJnlpUrl != null) {
	        checkLicensing(
	          skipLicensing,
	          sourceJnlpUrl
	          );
        }
        if (gui != null) {
            gui.goodToGo(this);
        }
    }

    public void purgeDownloads() {
        if (Basics.isMac()) {
            if (sourceJnlpUrl != null) {
				int idx = sourceJnlpUrl.lastIndexOf("/"), idx2 = sourceJnlpUrl
						.lastIndexOf(".jnlp");
				if (idx >= 0 && idx2 >= 0) {
					final String desktopDir = getDesktopDir();
					final String fn = sourceJnlpUrl.substring(idx + 1, idx2);
					IoBasics
							.purgeFiles(new File(desktopDir), fn, ".jnlp", true);
				}
			}
        }
    }

    public final String sourceJnlpUrl, licenseExpiryDatetime, linkRenewalUrl;

    public String getLicenceExpiryDatetime() {
        return licenseExpiryDatetime;
    }

    public String getSourceJnlpUrl() {
        return sourceJnlpUrl;
    }

    public File getArgsPropertyFile() {
        return new File(IoBasics.concat(SoftwareProduct.getCurrent().getProductDir(),
                                        "args.properties"));
    }

    public static final int PERIOD_OFFLINE_DAYS = 21;
    public static boolean skipLicensingUnconditionally = false;

    private void checkLicensing(
      final boolean _skipLicensing,
      final String sourceJnlpUrl) {
        final boolean skipLicensing = _skipLicensing ? true : false;
        if (JnlpBasics.isOnline() && !isSuperUser && !skipLicensingUnconditionally &&
            (!skipLicensing || !SuperUser.isLoggedIn())) {
            if (Basics.isEmpty(licenseExpiryDatetime) ||
                Basics.isEmpty(sourceJnlpUrl)) {
                if (gui != null) {
                    gui.alertMissingLicenseArguments();
                }
                Pel.log.printlnErr("Unauthorized usage:  get thee to a nunnery!!");
                System.exit(1);
            }
            try {
            	System.out.println("Checking license");
                final Date today = new Date();
                Date expiresOn = GmtFormat.parse(licenseExpiryDatetime);
                if (expiresOn == null) {
                    System.out.println("Server must be using yyyy-MM-dd HH:mm format (BEFORE GMT time upgrade on May 2, 2005) ... " +
                                       licenseExpiryDatetime);
                    try {
                        expiresOn = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(
                          licenseExpiryDatetime);
                    } catch (ParseException e) {
                        System.out.println("Server must be using yyyy-MM-dd format for " +
                                           licenseExpiryDatetime);
                        expiresOn = new SimpleDateFormat("yyyy-MM-dd").parse(
                          licenseExpiryDatetime);
                    }
                }
                //final String s = expiresOn.toString(); // for debugger viewer

                if (today.after(expiresOn)) {
                    regenerateLink(today.getTime() - expiresOn.getTime(),
                                   "Time to renew!");
                } else {
                    final boolean good = Basics.startsWithIgnoreCase(SuperUser.sites,
                      sourceJnlpUrl);
                    if (!good) {
                        if (gui != null) {
                            gui.alertUnauthorizedJnlpUrl(this);
                        }
                        System.exit(1);
                    }
                    //else if (SuperUser.getEmailForCurrentLoginAndMachine()==null){
                    else {
                        final BufferedReader br = IoBasics.getURLReaderWithoutThrowingUp(
                          sourceJnlpUrl);
                        if (br != null) {
                            final ArrayList<String> al = IoBasics.readTextLinesAndClose(br, true);
                            if (al.size() > 0) { // strange problem if  no lines
                                if (!findArg(al, ARG_LICENSE_EXPIRY,
                                             licenseExpiryDatetime)) {
                                    if (gui != null) {
                                        gui.alertExpiryDateMismatch(this);
                                    }
                                 //   System.exit(1);
                                }
                                if (!findArg(al, ARG_LICENSE_RENEWAL_URL, linkRenewalUrl)) {
                                    if (gui != null) {
                                        gui.alertLinkRenewalMismatch(this);
                                    }
                                    System.exit(1);
                                }
                                if (!findArg(al, ARG_SOURCE_JNLP_URL, sourceJnlpUrl)) {
                                    if (gui != null) {
                                        gui.alertJnlpUrlMismatch(this);
                                    }
                                    System.exit(1);
                                }
                                final String txt = describeMissingJars(al);
                                if (txt != null) {
                                    regenerateLink(0, "Missing resources:  " + txt);
                                }

                            }
                            IoBasics.closeWithoutThrowingUp(br);
                        } else {
                            final BufferedReader br2 = IoBasics.
                              getURLReaderWithoutThrowingUp(
                                linkRenewalUrl);
                            if (br2 != null) {
                                if (gui != null) {
                                    gui.alertJnlpUrlNotFound(this);
                                }
                                System.exit(1);
                            }
                            IoBasics.closeWithoutThrowingUp(br2);
                        }
                    }
                }
            } catch (final java.text.ParseException e) {
                Pel.log.print(e);
                Basics.gui.alert("Expiry date is incorrect " + licenseExpiryDatetime);
                System.exit(1);
            }
        }
    }

    public String[] getJars() {
        return version.getCoreApplicationJars();
    }

    private static void removeContained(final Collection<String> c,
                                        final String searchArg) {
        if (c != null && searchArg != null) {
            for (final Iterator<String> it = c.iterator(); it.hasNext(); ) {
                final String arg = it.next();
                if (searchArg.indexOf(arg) >= 0) {
                    it.remove();
                    //Basics.gui.alert("Removed "+ arg + "("+searchArg+")");
                    return;
                }
            }
        }
    }

    private String describeMissingJars(final ArrayList<String> al) {
        final Collection<String> c = Basics.toList(version.getCoreApplicationJars());
        for (final Iterator<String> it = al.iterator(); it.hasNext(); ) {
            removeContained(c, it.next());
        }
        return c.size() > 0 ? Basics.toString(c) : null;
    }

    private void regenerateLink(final long diffMillis, final String rationale) {
        if (!IoBasics.exists(linkRenewalUrl)) {
            // Get difference in days
            final long diffDays = diffMillis / (24 * 60 * 60 * 1000); // 7
            if (gui != null) {
                gui.alertRenewalSiteUnreachable(this, diffDays, rationale);
            }

            if (diffDays > PERIOD_OFFLINE_DAYS) {
                System.exit(1);
            }
        } else {
            if (gui != null) {
                gui.alertLinkExpired(this, rationale);
            }
            System.exit(1);
        }
    }

    private static boolean findArg(
      final ArrayList<String> al,
      final String parameterName,
      final String parameterValue) {
        final int n = al.size();
        final String lookingFor = "<argument>-" + parameterName + "</argument>";
        for (int i = 0; i < n; i++) {
            final String line = al.get(i).toString().trim();
            if (line.startsWith("<argument>")) {
                match(line, lookingFor);
                if (line.equals(lookingFor)) {
                    final String s2 = al.get(i + 1).toString().trim();
                    final String shouldMatch = "<argument>" + parameterValue +
                                               "</argument>";

                    if (s2.equals(shouldMatch)) {
                        return true;
                    }

                    return false;
                }
            }
        }
        return false;

    }

    static void match(final String s1, final String s2) {
        char[] c1 = s1.toCharArray(), c2 = s2.toCharArray();
        if (c1.length != c2.length) {
            return;
        }
        for (int i = 0; i < c1.length; i++) {

            if (c1[i] != c2[i]) {
                return;
            }
        }
    }

    public String getLicensee() {
        return licensee;
    }

    public String getLicense() {
        return license;
    }

    public void setLicensee(final String licensee) {
        this.licensee = licensee;
    }

    public boolean isLicenseeValidEmailAddress() {
        return licenseeIsEmailAddress;
    }

    public String subtractDocumentDir(final String filePath) {
        final String s = getDocumentsFolder();
        if (filePath.startsWith(s)) {
            return filePath.substring(s.length());
        }
        return filePath;
    }

    public static String getDocumentsFolder() {
  	  if (Basics.isMac()) {
		  return System.getProperty("user.home") +
		  	File.separator + "Documents" +
		  	File.separator;
	  }
	  else {
		  return FileSystemView.getFileSystemView().
			getDefaultDirectory().getAbsolutePath() +
			File.separator;
	  }
    }

    public static String makeTradeMarkAndRightsReservedPrintable(final String argName) {
        return argName==null ? null
          : argName.replaceAll("\u00ae","(R)").replaceAll("\u2122","(TM)");
    }

    public static String removeTradeMarkAndRightsReserved(final String argName) {
        return argName==null ? null
          : argName.replaceAll("\u00ae|\u2122", "");
    }

    public String getDocumentDir() {
        return getDocumentProducerDir(version) + File.separator +
          getProductSubDirName();
    }

    public static String getProducerFolderName(final Version version){
    	final String p=removeTradeMarkAndRightsReserved(version.getProducerName());
    	if (Basics.isMac()){
    		return "."+p;
    	} else {
    		return p;
    	}
    }
    
    public static String getHomeProducerDir(final Version version) {
    	return System.getProperty("user.home") + 
    	File.separatorChar + 
    	getProducerFolderName(version);
    }
    
    public static String getDocumentProducerDir(final Version version){
    	return getDocumentsFolder() + getProducerFolderName(version);
    }
    
    public static void establishProducerDirs(final Version version){
    	final String p=getHomeProducerDir(version), d=getDocumentProducerDir(version);
    	if (Basics.isMac()){
    		final String oldProducerName=removeTradeMarkAndRightsReserved(version.getProducerName());
    		final String _p=System.getProperty("user.home") + File.separatorChar+oldProducerName;
        	final String _d=getDocumentsFolder()+oldProducerName;
        	IoBasics.renameToIfPossible(_p, p, true);
        	IoBasics.renameToIfPossible(_d, d, true);
    	} else if (Basics.isEvilEmpireOperatingSystem()){ // product is running on the evil virus known as MicroSquish Windows 
    		try{
    			IoBasics.mkDirs(p);
    			IoBasics.mkDirs(d);
    			IoBasics.exec("attrib +h \""+p+"\"");
    			IoBasics.exec("attrib +h \""+d+"\"");
    		} catch (final IOException e){
    			e.printStackTrace();
    		}
    	} 
    }

 
    private static String getProductSubDir(final Version version){
    	final String releaseCycle=version.getReleaseCycle();
    	return VersionFolder.encode(
        	removeTradeMarkAndRightsReserved(version.getProductName()),
        	version.getMajorVersion(),
        	releaseCycle,
        	releaseCycle.equals("production") ? -1 : version.getMinorVersion());    	
    }
    
    public static String getDocumentDir(final Version version){
        return getDocumentProducerDir(version)+
        	File.separator+
        	getProductSubDir(version);
    }

    public static String getProductDir(final Version version) {
        return getHomeProducerDir(version) + 
        File.separatorChar + 
        getProductSubDir(version);
    }

    public String getDesktopDir() {
        final String dd =
          userHomeDir +
          File.separator +
          "Desktop";
        return getDirOnInternationalMachine(
          "Indicate desktop directory",
          "Select the desktop directory that contains your shortcuts", dd);
    }

    public String getDocumentSubDir(final String subDirName) {
        return getDocumentDir() +
          File.separator +
          subDirName +
          File.separatorChar;
    }

    public String establishDocumentSubDir(final String subDirName) {
        final String retVal = getDocumentSubDir(subDirName);
        IoBasics.mkDirs(retVal);
        return retVal;
    }

    private String establishDocumentDir() {
        final String retVal = getDocumentDir();
        IoBasics.mkDirs(retVal);
        return retVal;
    }

    public String getVersionText() {
        return majorVersion +
          (releaseCycle.equals("production") ? "" : "-" + minorVersion);
    }

    private String getProductSubDirName() {
    	return getProductSubDir(version);
    }

    public String getProductSubDir(final String dirName) {
        return productDir + dirName + File.separatorChar;
    }

    public String getProductDir() {
        return productDir;
    }

    public final static String SUBDIR_LOGS = "logs";
    public String establishProductSubDir(final String dirName) {
        final String retVal = getProductSubDir(dirName);
        IoBasics.mkDirs(retVal);
        return retVal;
    }

    public String getProductName() {
        return productName;
    }

    
    public String getExternalProducerName() {
        return version.getExternalProducerName();
    }

    public String getProducerAndProductName() {
        return version.getProducerName() + " " + productName;
    }

    public String getFullTextInfo() {
        return
          getExternalProducerName() +
          " " +
          productName +
          ", licensee: " +
          licensee +
          (license == null ? "" : " (" + license + ")") +
          "; " +
          releaseCycle +
          " " +
          getVersionText() +
          ", built on " +
          BuildTime.text;

    }

    public String getBuildTimeText() {
        return buildTimeText;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public String getFullHtmlInfo() {
        return
        Basics.concat(
          "<html><font color='blue'><center><h2>",
          productName,
          "</h2><font color='black'>Produced by:&nbsp;&nbsp;</font>",
          getExternalProducerName(),          
          ".&nbsp;&nbsp;&nbsp;<font color='black'>Licensed to:&nbsp;&nbsp;</font>",
          licensee,
          (license == null ? "" : " (" + license + ")"),
          ".<br><font color='black'>Version:&nbsp;&nbsp;</font>",
          releaseCycle,
          " ",
          getVersionText(),
          ".&nbsp;&nbsp;&nbsp;<font color='black'>Built on:&nbsp;&nbsp;</font>",
          buildTimeText,
          "</b>.<br><font color='black'>Running on:&nbsp;&nbsp;</font>",
          System.getProperty("os.name"),
          ".&nbsp;&nbsp;&nbsp;<font color='black'>Java version:&nbsp;&nbsp;</font>",
          System.getProperty("java.version"),
          ".&nbsp;&nbsp;&nbsp; <font color='black'>jvm version:&nbsp;&nbsp;</font>",
          System.getProperty("java.vm.version"),
          "</center></font></html>");
    }

    
    
    public String getShortTextInfo() {
        return productName +
          " " +
          majorVersion +
          " " +
          (releaseCycle.equals("production") ? "" :
           releaseCycle + " " + "" + minorVersion);
    }

    public static String getVersionTitle(
      final float majorVersion,
      final String releaseCycle,
      final int minorVersion) {
        return "ver. " +
          majorVersion +
          (releaseCycle.equals("production") ? "" :
           ", " + releaseCycle +
           " " +
           minorVersion);

    }

    public static String getVersionTitle( final Version version) {
        return getVersionTitle(
          version.getMajorVersion(),
          version.getReleaseCycle(),
          version.getMinorVersion());
    }


    public String getVersionTitle() {
        return getVersionTitle(
          getMajorVersion(),
          getReleaseCycle(),
          getMinorVersion());
    }

    public String getAuthorsHtml(final int lineBreakCount) {
        final StringBuilder sb = new StringBuilder("<html><body><center>");
        sb.append(getAuthorsHtmlBody(lineBreakCount));
        sb.append("</center></body></html> ");
        return sb.toString();
    }

    public String getAuthorsHtmlBody(final int lineBreakCount) {
        final StringBuilder sb = new StringBuilder("<small>");
        sb.append(authorLabel == null ? "<b><i>Authors:<i/></b>:&nbsp;&nbsp; " :
                  authorLabel);
        if (!Basics.isEmpty(authors)) {
            for (int i = 0; i < authors.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                    if ((i % lineBreakCount) == 0) {
                        sb.append("<br>");
                    }
                }
                sb.append(authors[i]);
            }
        }
        sb.append("</small>");
        return sb.toString();
    }

    public static class DefaultVersionImplementation implements Version {
        public String getDesktopFileName(){
            return "MOM";
        }
        
        public String getExternalProducerName(){
        	return getProducerName();
        }
        public String getDesktopIcon() {
            return null;
        }

        public String getProducerName() {
            return "ScienceXperts";
        }
        
        public String getPreviousProductName() {
            return null;
        }

        public String getProductName() {
            return "MOM";
        }

        public DefaultVersionImplementation() {

        }

        public float getMajorVersion() {
            return 1;
        }

        public int getMinorVersion() {
            return 1;
        }

        public String getReleaseCycle() {
            return "production";
        }

        public String[] getSupplementalJars() {
            return new String[] {};
        }

        public String[] getCoreApplicationJars() {
            return new String[] {
              "mmsProtege.jar"};
        }

        private String license;
        public void setLicense(final String license) {
            this.license = license;
        }

        public String getLicense() {
            return license;
        }

        public static String URL_FOR_LOADING_CYTOGENIE_FULL =
          "http://cytogenie.sciencexperts.com/CytoGenieLoading.htm",
        URL_FOR_LOADING_CYTOGENIE_BASIC =
          "http://cytogenie.sciencexperts.com/CytoGenieLoading.htm";

        public String getLoadingUrl() {
            return null;
        }

        public String getPasswordManagementUrl(final String email,
                                               final String sourceJnlp) {
            return null;
        }
    }


    public String getServerRootFolder() {
		String value = getSourceJnlpUrl();
		if (value != null) {
			int idx = value.indexOf("/?");
			if (idx < 0) { // try the old formats
				idx = value.indexOf("/index.jsp?");
				if (idx < 0) { 
					idx = value.indexOf("/users/");
				}
			}

			if (idx >= 0) {
				value = value.substring(0, idx + 1); // include the slash
			}
		}
		return value;
	}

    public static boolean isBeingUsedBySeniorDeveloper(final String []args){
		for (final String s :args){
			final String l=s.toLowerCase();
			if (l.endsWith("meehanmetaspace.com") || l.endsWith("mindtree.com")){
				return true;
			}
		}
		return false;
	}
    
    public boolean isBeingUsedBySeniorDeveloper(){
		final String l = getLicensee().toLowerCase();
		return l.endsWith("meehanmetaspace.com") || l.endsWith("mindtree.com") || l.endsWith("bmeehan@woodsidelogic.com");
	}
    
    public boolean isBeingUsedBySeniorDeveloperAndSupport(){
		final String l = getLicensee().toLowerCase();
		return l.endsWith("meehanmetaspace.com") || l.endsWith("mindtree.com") || l.endsWith("bmeehan@woodsidelogic.com")||l.endsWith("support@woodsidelogic.com");
	}
    
    public static String Name="Auto Compensation";
	public static String Version="v1";
	public static File UserFolder;
	public static File Folder;
	

}
