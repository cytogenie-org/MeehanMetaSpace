package com.MeehanMetaSpace.swing;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;

import org.apache.commons.codec.binary.Base64;

import com.MeehanMetaSpace.Basics;
//import com.MeehanMetaSpace.HelpListener;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.Pel;
//import com.MeehanMetaSpace.JnlpBasics;
import com.MeehanMetaSpace.PropertiesBasics;

public class SimpleServerManager extends Authenticator {

	private boolean becameSuperUser=false;
	private String guestEmail = "guest@woodsidelogic.com";
	public String emailAddress;
	//private String originalEmailAddress;
	private String server;
	private final File topSecretFile;
	private String lastPassword;
	//private String autoPassword
	//private String singleSignOnPassword;
	private char[] newPassword;
	private final Properties properties;
	private JCheckBox rememberCheckBox;
	private String topSecret;
	private JTextField emailField = new JTextField(25);;
    private JFrame dummyFrame = null;//new JFrame();
    private boolean okClicked = false;
    private ImageIcon applicationIcon;
    private PasswordAuthentication value;
    private JPasswordField passwordField;
    private String baseServletURL =  "http://cgworkspace.cytogenie.org/AGS";
    public static boolean PROHIBIT_EXIT_ON_CANCEL=false;
    public boolean registered=false, needToTest=false, authenticated = false, switched=false; 
    
    String productName, productVersion, productDir;
    //boolean isSuperUser, isAdvancedUser;
    
    public boolean isRegistered() {
    	return registered;
    }
    
    public boolean isAuthenticated() {
    	return authenticated;
    }
    
    public boolean isSwitched() {
    	return switched;
    }
    
    public String getEmailAddress() {
    	return emailAddress;
    }
    
    public static List<String> getHiddenFolderStartsWith(String dir, final String filterName) {
    	File mainFolder = new File(dir);
    	List<String> files= new ArrayList<String>();
		FilenameFilter fileNameFilter = new FilenameFilter() {
			   
            @Override
            public boolean accept(File dir, String name) {
               if(name.lastIndexOf('.')>0) {
                  // get last index for '.' char
                  int lastIndex = name.lastIndexOf('.');
                  
                  // get extension
                  String str = name.substring(0,lastIndex);
                  
                  // match path name extension
                  if((name.equals(filterName) || str.equals(filterName)) && new File(dir, name).isDirectory())
                  {
                     return true;
                  }
               }
	           else if(name.equals(filterName) && new File(dir, name).isDirectory()) {
	        	   return true;
	           }
               return false;
            }
		};
		if (mainFolder.exists() && mainFolder.isDirectory()) {
			File listFiles[]=mainFolder.listFiles(fileNameFilter);
			for (File s: listFiles) {
				files.add(s.getAbsolutePath());	
			}
		}
		return files;
    }
    
	public static void main(String[] args) {
		String productDir = System.getProperty("user.home") + File.separator + ".autoGate";
		String webRoot = "http://cgworkspace.cytogenie.org/ROOT";
		//SimpleServerManager authenticator = new SimpleServerManager("AutoResume", "5.2", productDir, "free@vada.com");
		initSwing2();
		SimpleServerManager.doFirstTimeCheck("AutoResume", productDir, "brissy@cytogenie.org");
		//System.out.println("Authentication process completed" + SimpleServerManager.getServerDetails(".autoGate", "free@vada.com"));
		//Authenticator.setDefault(authenticator);
		//authenticator.getPasswordAuthentication();
	}
	
	private boolean isOnline() {
		final String host="cgworkspace.cytogenie.org";
		final int port=22;
		int timeout=5500; 
		Socket socket = new Socket();
  		boolean online = false;
  		if (timeout==0){
  			timeout=9000;
  		}
  		try {
  			final SocketAddress sockaddr = new InetSocketAddress(host, port);  	  		
  			socket.connect(sockaddr, timeout);
  			online = true;
  		} catch (Exception ex) {
  			//ex.printStackTrace();
  			System.out.println(ex.getMessage());
  			online = false;
  		} finally {
  			try {
  				socket.close();
  			} catch (IOException ex) {
  				//ex.printStackTrace();
  				System.out.println(ex.getMessage());
  			}

  		}
  		return online;
	}
	
	private static boolean isDefaultServer(final Properties properties, final String emailAddress) {
		if (properties.get(emailAddress+".serveruser").equals("root")
		 && properties.get(emailAddress+".serverhost").equals("cgworkspace.cytogenie.org")
		 && properties.get(emailAddress+".serverdir").equals("/usr/local/tomcat7/webapps/ROOT/GetDown2/AutoGate/ExperimentFiles/")) {
			 return true;
		 }
		return false;
	}
	
	
	private static boolean isDefaultServer(final String productName, String productDir, final String emailAddress) {
		boolean ret = false;
		productDir = System.getProperty("user.home") + File.separator + productDir;
		File topSecretFile = new File(productDir, ".xjsxj.hh");
		Properties properties = PropertiesBasics.loadProperties(topSecretFile);
		System.out.println("Checking entries for " + emailAddress);
		SimpleServerManager sm = null;
		if (Basics.isEmpty(properties.get(emailAddress+".serverhost")) && Basics.isEmpty(properties.get(emailAddress+".servercheck"))) {
			SimpleServerManager manager = new SimpleServerManager(productName, productDir, emailAddress);
			System.out.println("No entries, checking in server");
			if (!manager.setFromServer()) {
				return false;
			}
		}
		return true;
	}
	
	
	public static SimpleServerManager doFirstTimeCheck(final String productName, String productDir, final String emailAddress) {
		productDir = System.getProperty("user.home") + File.separator + productDir;
		File topSecretFile = new File(productDir, ".xjsxj.hh");
		Properties properties = PropertiesBasics.loadProperties(topSecretFile);
		System.out.println("Checking entries for " + emailAddress);
		SimpleServerManager sm = null;
		if (Basics.isEmpty(properties.get(emailAddress+".serverhost")) && Basics.isEmpty(properties.get(emailAddress+".servercheck"))) {
			SimpleServerManager manager = new SimpleServerManager(productName, productDir, emailAddress);
			System.out.println("No entries, checking in server");
			if (!manager.setFromServer()) {
				if (!PopupBasics.ask("<html>Do you want to save your experiment metadata in "+ productName +" server?, <br/></br/>Click No if you would like to configure your server</html>")) {
					sm = new SimpleServerManager(productName, "1.0", productDir, emailAddress);
				}
			}
		}
		System.out.println("Setting server check to true");
		properties = PropertiesBasics.loadProperties(topSecretFile);
		properties.put(emailAddress+".servercheck", "true");
		PropertiesBasics.saveProperties(properties, topSecretFile, "");
		return sm;
	}
	
	public static String getServerDetails(String productDir, final String emailAddress) {
		StringBuffer sb = new StringBuffer();
		productDir = System.getProperty("user.home") + File.separator + productDir;
		File topSecretFile = new File(productDir, ".xjsxj.hh");
		Properties properties = PropertiesBasics.loadProperties(topSecretFile);
		if (Basics.isEmpty(properties.get(emailAddress+".serverhost"))) {
			properties.put(emailAddress+".serveruser","root");
			properties.put(emailAddress+".serverpassword", new String(Base64.encodeBase64("s6,SciCyto0729".getBytes())));
			properties.put(emailAddress+".serversalt", "xxxxx");
			properties.put(emailAddress+".serverhost", "cgworkspace.cytogenie.org");
			properties.put(emailAddress+".serverdir","/usr/local/tomcat7/webapps/ROOT/GetDown2/AutoGate/ExperimentFiles/");
			properties.put(emailAddress+".servercomments","");
			//PropertiesBasics.saveProperties(properties, topSecretFile, "");
		}
		//properties = PropertiesBasics.loadProperties(topSecretFile);
		if (!Basics.isEmpty(properties.get(emailAddress+".serverhost"))) {
			sb.append((String)properties.get(emailAddress+".serverhost"));
			sb.append("~");
			//sb.append((String)properties.get(emailAddress+".serverport"));
			//sb.append("~");
			sb.append((String)properties.get(emailAddress+".serverdir"));
			sb.append("~");
			sb.append((String)properties.get(emailAddress+".serveruser"));
			sb.append("~");
			//sb.append(new String(Base64.decodeBase64((String)properties.get(emailAddress+".serverpassword"))));
			sb.append(new String(Base64.decodeBase64(((String)properties.get(emailAddress+".serverpassword")).getBytes())));
		}
		return sb.toString();
	}
	
	private boolean setFromServer() {
		HashMap<String, String> userDetails= getUserPass(emailAddress);
		System.out.println("Gotcha2: "+ userDetails.size());
		if (!userDetails.isEmpty()) {
			properties.put(emailAddress+".serveruser",userDetails.get(PROPERTY_EMAIL));
			properties.put(emailAddress+".serverpassword", userDetails.get(PROPERTY_PASS));
			properties.put(emailAddress+".serversalt", userDetails.get(PROPERTY_SALT));
			properties.put(emailAddress+".serverhost", userDetails.get(PROPERTY_HOST));
			//properties.put(emailAddress+".serverport", portTxt.getText());
			properties.put(emailAddress+".serverdir", userDetails.get(PROPERTY_DIR));
			properties.put(emailAddress+".servercomments", userDetails.get(PROPERTY_COMMENTS));
			System.out.println("Saving sever details for " + emailAddress + " at " + 
					topSecretFile.getAbsolutePath() + " with values " + properties);
			if (!isDefaultServer(properties, emailAddress)) {
				PropertiesBasics.saveProperties(properties, topSecretFile, "");
			}
			return true;
		}
		return false;
	}
	
	public SimpleServerManager(final String email) {
		this("","",System.getProperty("user.home"),email);
     }
	
	public SimpleServerManager(final String productName, String productDir,
			final String email) {
		//this.originalEmailAddress = email;
		this.emailAddress = email;
		// this.applicationIcon=applicationIcon;
		this.productName = productName;
		this.productVersion = "1.0";
		this.productDir = productDir;
		initSwing();
		Pel.init(productDir, SimpleServerManager.class, productName, false);
		topSecretFile = new File(productDir, ".xjsxj.hh");
		properties = PropertiesBasics.loadProperties(topSecretFile);
     }
	
	public SimpleServerManager(final String productName, final String productVersion, String productDir,
			final String email) {
		//this.originalEmailAddress = email;
		this.emailAddress = email;
		// this.applicationIcon=applicationIcon;
		this.productName = productName;
		this.productVersion = productVersion;
		this.productDir = productDir;
		initSwing();
		Pel.init(productDir, SimpleServerManager.class, productName, false);
		topSecretFile = new File(productDir, ".xjsxj.hh");
		properties = PropertiesBasics.loadProperties(topSecretFile);
		//register(email);
	/*	String autoLogin = properties.getProperty(email + ".remember");
		if (autoLogin != null && autoLogin.equalsIgnoreCase("true")) {
			registered=true;
			authenticated=true;
			return;
		}*/
		/*String propPassword = properties.getProperty(email + ".password");
		String propSalt = properties.getProperty(email + ".salt");
		lastPassword = getEncryptedPassword(Base64.decode(propPassword.getBytes()), Base64.decode(propSalt.getBytes()));*/
		if (properties.getProperty(email + ".serverhost") == null) {	
			if (setFromServer()) {
				registered=true;
			}
		}
		else {
			registered=true;
		}
		
		register(email);
		/*if (registered) {
			getPasswordAuthentication();	
		}*/
		//forgotPassword();
	}
            
	public static boolean isRemember(String productDir, String email) {
		productDir = System.getProperty("user.home") + File.separator + productDir;
		File topSecretFile = new File(productDir, ".xjsxj.hh");
		if (topSecretFile.exists()) {
			Properties props = PropertiesBasics.loadProperties(topSecretFile);	
			String val = props.getProperty(email + ".remember");
			if (val != null) {
				return Boolean.valueOf(val);	
			}
		}
		return false;
	}
	
	public static void setRemember(String productDir, String email, boolean val) {
		productDir = System.getProperty("user.home") + File.separator + productDir;
		File topSecretFile = new File(productDir, ".xjsxj.hh");
		if (topSecretFile.exists()) {
			Properties props = PropertiesBasics.loadProperties(topSecretFile);	
			props.setProperty(email + ".remember", String.valueOf(val));
			PropertiesBasics.saveProperties(props, topSecretFile, "");
		}
	}
	
	/*private SimpleServerManager(final String productName, final String productVersion, String productDir,
			final String email, final String webroot, final ImageIcon applicationIcon, boolean isSuperUser,
			final String autoPassword) {

		this.originalEmailAddress = email;
		this.emailAddress = email;
		this.server = webroot;
		this.applicationIcon = applicationIcon;
		this.productName = productName;
		this.productVersion = productVersion;
		this.productDir = productDir;
		this.isSuperUser = isSuperUser;
		this.autoPassword = autoPassword;
		topSecretFile = new File(productDir, ".xjsxj.hh");
		properties = PropertiesBasics.loadProperties(topSecretFile);
		lastPassword = properties.getProperty(email + ".password");
		initSwing();
		if (lastPassword == null) {
			register();
		}
		getPasswordAuthentication();
		
	}*/
	
	boolean newUser = false;
	@Override
	public PasswordAuthentication getPasswordAuthentication() {

		System.out.println("getPasswordAuthentication() changed");

		/*if (!Basics.isEmpty(autoPassword)) {
			newPassword = autoPassword.toCharArray();
			topSecret = autoPassword;
			return new PasswordAuthentication(emailAddress, autoPassword.toCharArray());
		}

		if (!Basics.isEmpty(singleSignOnPassword)) {
			newPassword = singleSignOnPassword.toCharArray();
			topSecret = singleSignOnPassword;
			return new PasswordAuthentication(emailAddress, singleSignOnPassword.toCharArray());
		}

		boolean guestAccount = false;
		if (guestEmail.equals(originalEmailAddress)) {
			guestAccount = true;
		}*/
		final JDialog dlgMain = new JDialog(dummyFrame, productName + " authentication...", true);

		final JPanel main = new GradientBasics.Panel(new BorderLayout(2, 8));

		Color genieBlue = new Color(13, 57, 84);
		main.setBorder(BorderFactory.createMatteBorder(12, 8, 8, 8, genieBlue));
		//dlg.setAlwaysOnTop(true);
		dlgMain.getContentPane().add(main);
		final JPanel middle = new JPanel(new BorderLayout(4, 4));

		// middle.add(new JLabel(applicationIcon), BorderLayout.WEST);
		final JPanel entries = new JPanel();

		GridLayout grid = new GridLayout(5, 1);

		entries.setLayout(grid);

		// entries.setLayout (new BoxLayout (entries, BoxLayout.Y_AXIS));

		JPanel userlogin = new JPanel();
		JPanel pwdpanel = new JPanel();
		JPanel buttons = new JPanel();
		final Dimension dim1 = SwingBasics.isQuaQuaLookAndFeel() ? new Dimension(340, 30) : new Dimension(260, 30);
		userlogin.setPreferredSize(dim1);
		pwdpanel.setPreferredSize(dim1);

		userlogin.setLayout(new BoxLayout(userlogin, BoxLayout.X_AXIS));
		JLabel userEmail = new JLabel("User email:");
		userEmail.setPreferredSize(new Dimension(90, 30));
		userlogin.add(userEmail);
		
		emailField.setText(emailAddress);
		final Font f = emailField.getFont();
		Font font = new Font(f.getName(), Font.BOLD, f.getSize());
		System.out.println(f.getName() + "  " + f.getSize());
		emailField.setFont(font);
		userlogin.add(emailField);
		userEmail.setFont(font);

		// pwdpanel.setPreferredSize (dim3);
		pwdpanel.setLayout(new BoxLayout(pwdpanel, BoxLayout.X_AXIS));
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setPreferredSize(new Dimension(90, 30));
		pwdpanel.add(passwordLabel);
		passwordLabel.setFont(font);
		Dimension rigidArea = new Dimension(0, 8);
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

		passwordField = new JPasswordField(25);
		/*if (guestAccount) {
			emailField.setMargin(new Insets(2, 2, 2, 2));
			passwordField.setMargin(new Insets(2, 2, 2, 2));
		}*/

		pwdpanel.add(passwordField);
		buttons.add(Box.createRigidArea(rigidArea));
		buttons.add(Box.createRigidArea(rigidArea));
		buttons.add(Box.createRigidArea(rigidArea));
		buttons.add(Box.createRigidArea(rigidArea));
		final Font smallFont = new Font(UIManager.getFont("Table.font").getName(), Font.PLAIN, 12);
		if (!Basics.isEmpty(emailAddress)) {
			final String switchToolTip = Basics.toHtmlUncentered("Switch user",
					"If you are not <b>" + emailAddress
							+ "</b><br>then press this button to login as a different user");
			final JButton switchUser = SwingBasics.getButton("Switch user?", null, 'u', new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					newUser = true;
					//originalEmailAddress="";
					emailAddress="";
					emailField.setText("");
					emailField.requestFocus();
					emailField.setEditable(true);
					switched=true;
					/*SwingBasics.showHtml(server);
					if (!PROHIBIT_EXIT_ON_CANCEL)
						System.exit(1);*/

				}
			}, switchToolTip);
			switchUser.setFont(smallFont);
			buttons.add(switchUser);
			// userlogin.add(switchUser);

		} else {
			buttons.add(Box.createRigidArea(rigidArea));
		}

		/*final JButton ok = SwingBasics.getDoneButton(dlgMain,
				Basics.toHtmlUncentered("Ok", "Submit password"));
		ok.setText(" Ok ");*/
		final JButton ok = SwingBasics.getButton("Ok", MmsIcons.getHelpIcon(), 'o',
					null,
					Basics.toHtmlUncentered("Ok",
							"Press this button to submit the password"));
									 
		System.out.println("userlogin size = " + userlogin.getSize().toString());
		System.out.println("pwd panel size = " + pwdpanel.getSize().toString());
		entries.add(Box.createRigidArea(rigidArea));
		entries.add(userlogin);
		entries.add(pwdpanel);
		JPanel rememberPass = new JPanel();
		rememberPass.setPreferredSize(dim1);
		rememberPass.setLayout(new BoxLayout(rememberPass, BoxLayout.X_AXIS));
		JLabel rememEmail = new JLabel("                      ");

		rememberPass.add(rememEmail);

		rememberCheckBox = new JCheckBox("Login automatically");
		rememberCheckBox.setToolTipText("<html>You would not be prompted for password from next time. <br/>You can enable login again from main screen menu options</html>");
		rememberCheckBox.setMnemonic('r');
		rememberCheckBox.setFont(smallFont);
		rememberCheckBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				properties.setProperty(emailAddress + ".remember", String.valueOf(rememberCheckBox.isSelected()));
			}
		});
		// rememberPass.add (rememberCheckBox);
		entries.add(rememberPass);
		buttons.add(Box.createRigidArea(rigidArea));
		buttons.add(rememberCheckBox);
		buttons.add(Box.createRigidArea(rigidArea));

		JPanel sidebyside = new JPanel();
		sidebyside.setLayout(new BoxLayout(sidebyside, BoxLayout.X_AXIS));

		buttons.setAlignmentY(Component.TOP_ALIGNMENT);
		entries.setAlignmentY(Component.TOP_ALIGNMENT);
		sidebyside.add(entries);
		sidebyside.add(new JLabel("    "));
		sidebyside.add(buttons);
		sidebyside.add(new JLabel("    "));
		
		final JLabel serverLabel = new JLabel("                   ");
		serverLabel.setBackground(Color.WHITE);

		JLabel title = new JLabel(
				SwingBasics.isQuaQuaLookAndFeel() ? "  Login to your " + productName: "   Login to your " + productName);
		title.setFont(new Font(f.getName(), Font.BOLD, f.getSize() + 2));
		title.setHorizontalTextPosition(SwingConstants.CENTER);

		middle.add(title, BorderLayout.NORTH);
		middle.add(sidebyside, BorderLayout.CENTER);

		middle.add(serverLabel, BorderLayout.SOUTH);
		middle.add(new JPanel(), BorderLayout.EAST);
		middle.add(new JPanel(), BorderLayout.WEST);

		//if (!guestAccount) {
			/*if (lastPassword != null) {
				rememberCheckBox.setSelected(true);
				passwordField.setText(lastPassword);
			}*/
		//}

		main.add(middle, BorderLayout.CENTER);
		JPanel geniePanel = new JPanel(new GridLayout(8, 1));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		geniePanel.add(new JLabel("         "));
		main.add(geniePanel, BorderLayout.WEST);

		JPanel geniePanel2 = new JPanel(new GridLayout(8, 1));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		geniePanel2.add(new JLabel("         "));
		main.add(geniePanel2, BorderLayout.EAST);

		JPanel northPanel = new JPanel(new GridLayout(2, 1));
		northPanel.add(new JLabel("         "));
		northPanel.add(new JLabel("         "));
		main.add(northPanel, BorderLayout.NORTH);

		final JPanel passwordMemory = new JPanel();
		//if (!guestAccount) {
			final JButton changePassword = SwingBasics.getButton("Change password", null, 'c', new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					changePassword(false);
				}
			}, Basics.toHtmlUncentered("Change password",
					"Press this button to change your current password "));
			changePassword.setFont(smallFont);
			passwordMemory.add(changePassword);
		//}
		final JPanel okCancel = new JPanel();
		JLabel emptyLabel2 = new JLabel("                ");
		emptyLabel2.setBounds(2, 2, 2, 2);
		//if (!guestAccount) {
			final JButton forgot = SwingBasics.getButton("Forgot password?", MmsIcons.getHelpIcon(), 'f',
					new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							forgotPassword();
						}
					},
					Basics.toHtmlUncentered("Forgot my password",
							"Press this button to answer challenge questions and <b>" +
									 "</b><br>then reset the password"));
			forgot.setFont(smallFont);
			passwordMemory.add(forgot);
		//} else {
		//	okCancel.add(emptyLabel2);	
		//}
			final JButton signup = SwingBasics.getButton("New user?, sign up here!", null, 's', new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					register(null);
				}
			}, Basics.toHtmlUncentered("New user",
					"Press this button to create a new AutoGate user account"));
			
			passwordMemory.add(signup);
		final JButton cancel = SwingBasics.getCancelButton(dlgMain,
				Basics.toHtmlUncentered("Cancel",
						"Close this window without authenticating"),
				true, new ActionListener() {

					public void actionPerformed(final ActionEvent e) {
						dlgMain.dispose();	
						/*HelpListener.actionSystemExit();
						if (!PROHIBIT_EXIT_ON_CANCEL) {
							System.exit(1);
						}*/
					}
				});
		okCancel.add(ok);
		okCancel.add(cancel);

		final JPanel bottom = new JPanel(new BorderLayout(12, 1));
		bottom.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		bottom.add(passwordMemory, BorderLayout.WEST);
		bottom.add(new JLabel("           "), BorderLayout.CENTER);
		bottom.add(okCancel, BorderLayout.EAST);

		main.add(bottom, BorderLayout.SOUTH);
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				newPassword = passwordField.getPassword();
				emailAddress = emailField.getText();
				topSecret = String.copyValueOf(passwordField.getPassword());
				newRegistration = false;
				if (validatePass(emailAddress, String.valueOf(newPassword))) {
					authenticated=true;
					//PopupBasics.alert("Login successful");
					PropertiesBasics.saveProperties(properties, topSecretFile, "");
					dlgMain.dispose();	
				}
				else {
					if (!newRegistration) {
						PopupBasics.alert("Invalid user/password, please try again");
					}
					passwordField.requestFocus();
				}
				
			}
		});

		emailField.addFocusListener(new FocusListener() {
			public void focusGained(final FocusEvent fe) {
				
				/*if (Basics.equals(String.copyValueOf(passwordField.getPassword()), "admin")) {
					emailField.setEditable(true);
					emailField.setText("stephen@MeehanMetaSpace.com");
					emailField.getCaret().setVisible(true);
					emailField.selectAll();
					if (!isSuperUser) {
						becameSuperUser = true;
					}

				}*/
			}

			public void focusLost(FocusEvent fe) {
				//lastPassword = properties.getProperty(emailField.getText()+".password");
				emailAddress= emailField.getText();
				//rememberCheckBox.setSelected(true);
				//passwordField.setText(lastPassword);
			}
		});
		dlgMain.getRootPane().setDefaultButton(ok);
		dlgMain.pack();
		SwingBasics.center(dlgMain);
		if (!Basics.isEmpty(emailAddress)) {
			emailField.setEditable(false);
			passwordField.requestFocus();
		} else {
			emailField.setEditable(true);
			emailField.requestFocus();
		}

		dlgMain.setResizable(false);
		GradientBasics.setTransparentChildren(main, true);
		dlgMain.setVisible(true);

		if (!okClicked) {
			value = null;
			// System.out.println("Cancelling");
		} else {
			//singleSignOnPassword = newPassword.toString();
			value = new PasswordAuthentication(emailAddress, newPassword);
			// System.out.print("HERE IT IS: pwd=\"");
			// System.out.print(topSecret);
			// System.out.print("\" email=\"");
			// System.out.print(emailAddress);
			// System.out.println("\"");
			// System.out.print("Super User=");
			/*if (emailAddress.equals(originalEmailAddress) && becameSuperUser) {
				 System.out.println("FALSE (Reverted to non super user after
				 back door failed)");
				isSuperUser= false;
			} else {
				isAdvancedUser = true;

				// System.out.println("Advanced user is now TRUE ... and super
				// user is"+softwareProduct.isSuperUser());
			}*/
		}
		return value;
	}

	protected void changePassword(final boolean noCheckOldPass) {
		final JDialog dlg = new JDialog(dummyFrame, "Change Server details...", true);
		//dlg.setAlwaysOnTop(true);
		JTextField emailField = new JTextField(25);
		final Font font = emailField.getFont();
		emailField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		emailField.setText(emailAddress);
		emailField.setEditable(false);
		final JPasswordField oldPasswordField = new JPasswordField(20);
		final JPasswordField newPasswordField = new JPasswordField(20);
		final JPasswordField confirmNewPasswordField = new JPasswordField(20);

		final JLabel emailLabel = new JLabel("User email : ");
		final JLabel oldPasswordLabel = new JLabel("Old Password : ");
		final JLabel newPasswordLabel = new JLabel("New Password : ");
		final JLabel confirmPasswordLabel = new JLabel("Confirm New Password : ");

		emailLabel.setLabelFor(emailField);
		oldPasswordLabel.setLabelFor(oldPasswordField);
		newPasswordLabel.setLabelFor(newPasswordLabel);
		confirmPasswordLabel.setLabelFor(confirmNewPasswordField);

		JPanel textControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		textControlsPane.setLayout(gridbag);
		textControlsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		if(noCheckOldPass) {
			JLabel labels[] = { emailLabel, newPasswordLabel, confirmPasswordLabel };
			JTextField textFields[] = { emailField, newPasswordField, confirmNewPasswordField };

			addLabelTextRows(labels, textFields, gridbag, textControlsPane);	
		}
		else {
			JLabel labels[] = { emailLabel, oldPasswordLabel, newPasswordLabel, confirmPasswordLabel };
			JTextField textFields[] = { emailField, oldPasswordField, newPasswordField, confirmNewPasswordField };

			addLabelTextRows(labels, textFields, gridbag, textControlsPane);
		}
		
		ActionListener submitAction = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				URL url;
				if (!noCheckOldPass) {
					char[] oldpassword = oldPasswordField.getPassword();
					final String oldPass = new String(oldpassword).trim();
					if (!validatePass(emailAddress, oldPass)) {
						PopupBasics.alert("Old Password is wrong");
						dlg.toFront();
						return;
					}
				}
				
				char[] newpassword = newPasswordField.getPassword();
				final String newPass = new String(newpassword).trim();
				char[] confirmnew = confirmNewPasswordField.getPassword();
				final String confirmNewPass = new String(confirmnew).trim();
				try {
					if (Basics.isEmpty(newPass) || Basics.isEmpty(confirmNewPass)) {
						PopupBasics.alert("Password cannot be empty");
						dlg.toFront();
					} else if (!(newPass.equals(confirmNewPass))) {
						PopupBasics.alert("Passwords do not match");
						dlg.toFront();
					}  else {
						byte[]salt = generateSalt();
						String saltEncoded=new String(Base64.encodeBase64(salt));
						String passEncoded= new String(Base64.encodeBase64(getEncryptedPassword(newPass, salt)));
						if (isOnline()) {
							url = new URL(baseServletURL + "/updatepass?email=" + emailAddress
									+ "&pass="
									+ URLEncoder.encode(passEncoded, "UTF-8")
									+ "&salt="
									+ URLEncoder.encode(saltEncoded, "UTF-8"));

							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
							conn.setDoOutput(true);
							conn.setDoInput(true);
							InputStream is = conn.getInputStream();
							final BufferedReader br = new BufferedReader(new InputStreamReader(is));

							String line;
							while ((line = br.readLine()) != null) {
								if (line.contains("success")) {
									PopupBasics.alert("Your password has been changed successfully");
									dlg.toFront();
									properties.put(emailAddress+".password", passEncoded);
									properties.put(emailAddress+".salt", saltEncoded);
									PropertiesBasics.saveProperties(properties, topSecretFile, "");
									break;
								}
								else {
									PopupBasics.alert("Error registering, please contact support");
									//dlg.dispose();
									break;
								}
							}
						}
						SwingBasics.closeWindow(dlg);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				} 

			}

		};

		final JButton save = SwingBasics.getButton("Submit", MmsIcons.getYesIcon(), 'y', submitAction, "");
		dlg.getRootPane().setDefaultButton(save);

		final JButton noButton = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'n', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingBasics.closeWindow(dlg);
			}
		}, "");
		dlg.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				noButton.doClick(132);
			}
		}

		,

		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(save);
		buttonPanel.add(noButton);

		buttonPanel.setBorder(BorderFactory.createEtchedBorder()/* (5,5,5,5)) */);

		dlg.add(textControlsPane, "North");
		dlg.add(buttonPanel, "South");
		dlg.pack();
		dlg.setResizable(true);
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);
	}
	
	private boolean newRegistration = false;
	private boolean validatePass(String email, String pass) {
		String pass1 = (String)properties.get(email +".password");
		String salt = (String)properties.get(email +".salt");
		if (pass != null && salt != null) {
			try {
				return authenticate(pass, Base64.decodeBase64(pass1.getBytes()), Base64.decodeBase64(salt.getBytes()));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		else {
			HashMap<String, String> userDetails= getUserPass(email);
			System.out.println("Gotcha2: "+ userDetails.size());
			if (!userDetails.isEmpty()) {
				String pass2=userDetails.get(PROPERTY_PASS);
				String salt2=userDetails.get(PROPERTY_SALT);
				properties.setProperty(email + ".password",pass2);
				properties.setProperty(email + ".salt",salt2);
				PropertiesBasics.saveProperties(properties, topSecretFile, "");
				registered=true;
				try {
					return authenticate(pass, Base64.decodeBase64(pass2.getBytes()), Base64.decodeBase64(salt2.getBytes()));
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}	
			else {
				newRegistration = true;
				register(email);
			}
		}
		return false;
	}
	boolean isUpdate = false;
	private void register(String email) {
		
		
		final JDialog dlg = new JDialog(dummyFrame, "Register your " + productName +" server...", true);
		//dlg.setAlwaysOnTop(true);
		final JTextField serverUser = new JTextField(25);
		/*final Font font = serverUser.getFont();
		serverUser.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		if (email != null && !email.equals("")) {
			serverUser.setText(email);
			serverUser.setEditable(false);	
		}
		else {
			switched=true;
			serverUser.setText("");
			serverUser.setEditable(true);
		}
				*/
		final JPasswordField newPasswordField = new JPasswordField(20);
		final JPasswordField confirmNewPasswordField = new JPasswordField(20);
		//final JTextField newPasswordField = new JTextField(20);
		//final JTextField confirmNewPasswordField = new JTextField(20);
		//final JTextField ques1Field = new JTextField(30);
		final JTextField hostTxt = new JTextField(30);
		//final JTextField ques2Field = new JTextField(30);
		//final JTextField portTxt = new JTextField(30);
		final JTextField dirTxt = new JTextField(30);
		final JTextArea cmtsTxt = new JTextArea(2,2);
		
		if (!Basics.isEmpty(properties.get(emailAddress+".serverhost")) && !isDefaultServer(properties, emailAddress)) {
			isUpdate=true;
			hostTxt.setText((String)properties.get(emailAddress+".serverhost"));
			//portTxt.setText((String)properties.get(emailAddress+".serverport"));
			dirTxt.setText((String)properties.get(emailAddress+".serverdir"));
			cmtsTxt.setText((String)properties.get(emailAddress+".servercomments"));
			serverUser.setText((String)properties.get(emailAddress+".serveruser"));
			newPasswordField.setText(new String(Base64.decodeBase64(((String)properties.get(emailAddress+".serverpassword")).getBytes())));
			confirmNewPasswordField.setText(newPasswordField.getText());
		}

		final JLabel usrLbl = new JLabel("Server login user* : ");
		final JLabel hostLbl = new JLabel("Server host(with port)*: ");
		//final JLabel portLbl = new JLabel("Server port* : ");
		final JLabel dirLbl = new JLabel("Storage Directory*: ");
		final JLabel newPasswordLabel = new JLabel("Server login Password* : ");
		final JLabel confirmPasswordLabel = new JLabel("Confirm Password* : ");
		final JLabel cmtsLbl = new JLabel("Comments: ");

		usrLbl.setLabelFor(serverUser);
		newPasswordLabel.setLabelFor(newPasswordLabel);
		confirmPasswordLabel.setLabelFor(confirmNewPasswordField);
		hostLbl.setLabelFor(hostTxt);
		//portLbl.setLabelFor(portTxt);
		dirLbl.setLabelFor(dirTxt);
		cmtsLbl.setLabelFor(cmtsTxt);
	
		JPanel textControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		textControlsPane.setLayout(gridbag);
		textControlsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JLabel labels[] = {  hostLbl, /*portLbl,*/ dirLbl, usrLbl, newPasswordLabel, confirmPasswordLabel, cmtsLbl  };
		JComponent textFields[] = { hostTxt, /*portTxt,*/ dirTxt, serverUser, newPasswordField, confirmNewPasswordField, cmtsTxt };

		addLabelTextRows(labels, textFields, gridbag, textControlsPane);
		ActionListener submitAction = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				URL url;
				/*char[] newpassword = newPasswordField.getPassword();
				final String newPass = new String(newpassword).trim();
				char[] confirmnew = confirmNewPasswordField.getPassword();
				final String confirmNewPass = new String(confirmnew).trim();*/
				final String newPass = newPasswordField.getText();
				final String confirmNewPass =confirmNewPasswordField.getText();
				try {
					if ((!Basics.isEmpty(newPass) || !Basics.isEmpty(confirmNewPass))
							&& (!(newPass.equals(confirmNewPass)))) {
							PopupBasics.alert("Passwords do not match");
							dlg.toFront();
					} else if (Basics.isEmpty(serverUser.getText()) ) {
						PopupBasics.alert("Server user cannot be empty");
						dlg.toFront();
					} else if (Basics.isEmpty(hostTxt.getText()) ) {
						PopupBasics.alert("Server host cannot be empty");
						dlg.toFront();
					} /*else if (Basics.isEmpty(portTxt.getText()) ) {
						PopupBasics.alert("Server port cannot be empty");
						dlg.toFront();
					}*/ else if (Basics.isEmpty(dirTxt.getText()) ) {
						PopupBasics.alert("Server directory cannot be empty");
						dlg.toFront();
					} 
					else {
						String user = serverUser.getText();
						System.out.println("Registering server: " + user);
						/*if (properties.getProperty(newEmail +".serverpassword") != null || !getUserPass(newEmail).isEmpty()) {
							PopupBasics.alert("User already exists!");
							dlg.toFront();
							return;
						}*/
						String saltEncoded=null;
						String passEncoded= null;
						if (isUpdate && !Basics.isEmpty(newPass)) {
							saltEncoded=(String)properties.get(emailAddress+".serversalt");
							//passEncoded= new String(Base64.encodeBase64(getEncryptedPassword(newPass, Base64.decodeBase64(saltEncoded))));
							passEncoded= new String(Base64.encodeBase64(newPass.getBytes()));
						}
						else {
							byte[]salt = generateSalt();
							System.out.println("Jar location: "+Base64.class.getProtectionDomain().getCodeSource().getLocation());
							saltEncoded=new String(Base64.encodeBase64(salt));
							//passEncoded= new String(Base64.encodeBase64(getEncryptedPassword(newPass, salt)));
							passEncoded= new String(Base64.encodeBase64(newPass.getBytes()));
						}
						
						if (isOnline()) {
							try {
								url = new URL(baseServletURL + "/registerserver?email=" + emailAddress
										+ "&user="
										+ URLEncoder.encode(user, "UTF-8")
										+ "&pass="
										+ URLEncoder.encode(passEncoded, "UTF-8")
										+ "&salt="
										+ URLEncoder.encode(saltEncoded, "UTF-8")
										+ "&host="
										+ URLEncoder.encode((String)hostTxt.getText(), "UTF-8")
										//+ "&port="
										//+ URLEncoder.encode(portTxt.getText(), "UTF-8")
										+ "&dir="
										+ URLEncoder.encode((String)dirTxt.getText(), "UTF-8")
										+ "&comments="
										+ URLEncoder.encode(cmtsTxt.getText(), "UTF-8"));

								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								conn.setRequestMethod("POST");
								conn.setDoOutput(true);
								conn.setDoInput(true);
								InputStream is = conn.getInputStream();
								final BufferedReader br = new BufferedReader(new InputStreamReader(is));

								String line;
								while ((line = br.readLine()) != null) {
									if (line.contains("success")) {
										registered=true;
										//PopupBasics.alert("You server has been registered successfully");
										dlg.toFront();
										break;
									}
									else {
										PopupBasics.alert("Error registering server, please contact support");
										//dlg.dispose();
										break;
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						properties.put(emailAddress+".serveruser",serverUser.getText());
						properties.put(emailAddress+".serverpassword", passEncoded);
						properties.put(emailAddress+".serversalt", saltEncoded);
						properties.put(emailAddress+".serverhost", hostTxt.getText());
						//properties.put(emailAddress+".serverport", portTxt.getText());
						properties.put(emailAddress+".serverdir", dirTxt.getText());
						properties.put(emailAddress+".servercomments", cmtsTxt.getText());
						PropertiesBasics.saveProperties(properties, topSecretFile, "");
						
						emailField.setText(emailAddress);
						//originalEmailAddress=emailAddress;
						registered=true;
						needToTest=true;
						SwingBasics.closeWindow(dlg);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				} 

			}

		};

		final JButton save = SwingBasics.getButton("Submit", MmsIcons.getYesIcon(), 'y', submitAction, "");
		dlg.getRootPane().setDefaultButton(save);

		final JButton noButton = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'n', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				registered=false;
				switched=false;
				SwingBasics.closeWindow(dlg);
			}
		}, "");
		dlg.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				noButton.doClick(132);
			}
		}

		,

		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		final JButton resetButton = SwingBasics.getButton("Reset / Use AutoGate server", MmsIcons.getDeleteIcon(), 'r', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				properties.remove(emailAddress+".serveruser");
				properties.remove(emailAddress+".serverpassword");
				properties.remove(emailAddress+".serversalt");
				properties.remove(emailAddress+".serverhost");
				//properties.remove(emailAddress+".serverport");
				properties.remove(emailAddress+".serverdir");
				properties.remove(emailAddress+".servercomments");
				PropertiesBasics.saveProperties(properties, topSecretFile, "");
				registered=true;
				
				if (isOnline()) {
					try {
						URL url = new URL(baseServletURL + "/registerserver?email=" + emailAddress);

						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("POST");
						conn.setDoOutput(true);
						conn.setDoInput(true);
						InputStream is = conn.getInputStream();
						final BufferedReader br = new BufferedReader(new InputStreamReader(is));

						String line;
						while ((line = br.readLine()) != null) {
							if (line.contains("success")) {
								registered=true;
								//PopupBasics.alert("You server has been registered successfully");
								dlg.toFront();
								break;
							}
							else {
								PopupBasics.alert("Error registering server, please contact support");
								//dlg.dispose();
								break;
							}
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				SwingBasics.closeWindow(dlg);
			}
		}, "Remove my server credentials and use AutoGate Server");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(save);
		buttonPanel.add(resetButton);
		
		/*final JButton testButton = SwingBasics.getButton("Test", MmsIcons.getTickIcon(), 't', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				needToTest=true;
				SwingBasics.closeWindow(dlg);
			}
		}, "Test the server connection by placing a dummy file");		
		buttonPanel.add(testButton);*/
		buttonPanel.add(resetButton);
		
		buttonPanel.add(noButton);

		buttonPanel.setBorder(BorderFactory.createEtchedBorder()/* (5,5,5,5)) */);

		dlg.add(textControlsPane, "North");
		dlg.add(buttonPanel, "South");
		dlg.pack();
		dlg.setResizable(true);
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);
	}
	
	private void forgotPassword() {
		final JDialog dlg = new JDialog(dummyFrame, "Forgot password...", true);
		//dlg.setAlwaysOnTop(true);
		JTextField emailField = new JTextField(25);
		final Font font = emailField.getFont();
		emailField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		emailField.setText(emailAddress);
		emailField.setEditable(false);
		
		final JTextField ques1Field = new JTextField(30);
		final JTextField ans1Field = new JTextField(30);
		final JTextField ques2Field = new JTextField(30);
		final JTextField ans2Field = new JTextField(30);

		final JLabel emailLabel = new JLabel("User email* : ");
		final JLabel ques1 = new JLabel("Challenge Question #1: ");
		final JLabel ans1 = new JLabel("Answer #1 : ");
		final JLabel ques2 = new JLabel("Challenge question #2 : ");
		final JLabel ans2 = new JLabel("Answer #2: ");

		emailLabel.setLabelFor(emailField);
		ques1.setLabelFor(ques1Field);
		ans1.setLabelFor(ans1Field);
		ques1.setLabelFor(ques2Field);
		ans2.setLabelFor(ans2Field);
		

		JPanel textControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		textControlsPane.setLayout(gridbag);
		textControlsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JLabel labels[] = { emailLabel, ques1, ans1, ques2, ans2 };
		JTextField textFields[] = { emailField, ques1Field, ans1Field, ques2Field, ans2Field };

		addLabelTextRows(labels, textFields, gridbag, textControlsPane);
		
		final String quesAns[]= new String[4];
		if (isOnline()) {
			
			String result;
			try {
				result = IoBasics.readWebPage(baseServletURL + "/getChallengeQues?email=" + emailAddress).trim();
				if (result != null && result.length()>15) {
					String res[]= result.split(",");
					if (res.length==4) {
						quesAns[0] = res[0];
						quesAns[1] = res[1];
						quesAns[2] = res[2];
						quesAns[3] = res[3];
					}
					ques1Field.setText(quesAns[0]);
					ques2Field.setText(quesAns[2]);
					//ans1Field.setText(quesAns[1]);
					//ans2Field.setText(quesAns[3]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ActionListener submitAction = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				URL url;
				try {
						if (ans1Field.getText().equals(quesAns[1]) && 
								ans2Field.getText().equals(quesAns[3])) {
							PopupBasics.alert("Correct..");
							changePassword(true);
						}
						else {
							PopupBasics.alert("Incorrect answers. Please try again or contact support.");
						}
						SwingBasics.closeWindow(dlg);
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
			}

		};

		final JButton save = SwingBasics.getButton("Submit", MmsIcons.getYesIcon(), 'y', submitAction, "");
		dlg.getRootPane().setDefaultButton(save);

		final JButton noButton = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'n', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingBasics.closeWindow(dlg);
			}
		}, "");
		dlg.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				noButton.doClick(132);
			}
		}

		,

		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(save);
		buttonPanel.add(noButton);

		buttonPanel.setBorder(BorderFactory.createEtchedBorder()/* (5,5,5,5)) */);

		dlg.add(textControlsPane, "North");
		dlg.add(buttonPanel, "South");
		dlg.pack();
		dlg.setResizable(true);
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);
	}
	
	private String PROPERTY_EMAIL = "email";
	private String PROPERTY_PASS = "serverpass";
	private String PROPERTY_SALT = "serversalt";
	private String PROPERTY_HOST = "serverhost";
	//private String PROPERTY_ANS1 = "serverport";
	private String PROPERTY_DIR = "serverdir";
	private String PROPERTY_COMMENTS = "servercomments";
	
	private HashMap<String, String> getUserPass(String email) {
		HashMap<String, String> userpass= new HashMap<String, String>();
		System.out.println("getting server details for " + email);
		if (isOnline()) {
			
			String result;
			try {
				result = IoBasics.readWebPage(baseServletURL + "/getserver?email=" + email).trim();
				System.out.println("getserver pass results2: "+ result);
				if (result != null) {//&& result.indexOf("salt") != -1
					String up[]=result.split(",");
					if (up.length>=5) {
						System.out.println("Popuplated DAO");
						userpass.put(PROPERTY_EMAIL, up[0]);
						userpass.put(PROPERTY_PASS, up[1]);
						userpass.put(PROPERTY_SALT, up[2]);
						userpass.put(PROPERTY_HOST, up[3]);
						//userpass.put(PROPERTY_ANS1, up[3]);
						userpass.put(PROPERTY_DIR, up[4]);
						if (up.length>5) {
							userpass.put(PROPERTY_COMMENTS, up[5]);
						}
						else {
							userpass.put(PROPERTY_COMMENTS, "");
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return userpass;
	}
	
	protected void challenge() {
		final JDialog dlg = new JDialog(dummyFrame, "Challenge...", true);
		//dlg.setAlwaysOnTop(true);
		JTextField emailField = new JTextField(25);
		final Font font = emailField.getFont();
		emailField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		emailField.setText(emailAddress);
		emailField.setEditable(false);
		final JPasswordField oldPasswordField = new JPasswordField(20);
		final JPasswordField newPasswordField = new JPasswordField(20);
		final JPasswordField confirmNewPasswordField = new JPasswordField(20);

		final JLabel emailLabel = new JLabel("Question #1: ");
		final JLabel oldPasswordLabel = new JLabel("Answer #1 : ");
		final JLabel newPasswordLabel = new JLabel("Question #2 : ");
		final JLabel confirmPasswordLabel = new JLabel("Answer #2: ");

		emailLabel.setLabelFor(emailField);
		oldPasswordLabel.setLabelFor(oldPasswordField);
		newPasswordLabel.setLabelFor(newPasswordLabel);
		confirmPasswordLabel.setLabelFor(confirmNewPasswordField);

		JPanel textControlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		textControlsPane.setLayout(gridbag);
		textControlsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JLabel labels[] = { emailLabel, oldPasswordLabel, newPasswordLabel, confirmPasswordLabel };
		JTextField textFields[] = { emailField, oldPasswordField, newPasswordField, confirmNewPasswordField };

		addLabelTextRows(labels, textFields, gridbag, textControlsPane);
		ActionListener submitAction = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				URL url;
				/*final String codebase = JnlpBasics.isOnline() ? JnlpBasics.getWebAppRootForClient()
						: "http://facs.stanford.edu:8080/beta";*/
				final String codebase ="http://facs.stanford.edu:8080/beta";
				char[] oldpassword = oldPasswordField.getPassword();
				final String oldPass = new String(oldpassword).trim();
				char[] newpassword = newPasswordField.getPassword();
				final String newPass = new String(newpassword).trim();
				char[] confirmnew = confirmNewPasswordField.getPassword();
				final String confirmNewPass = new String(confirmnew).trim();
				try {
					if (Basics.isEmpty(oldPass) || Basics.isEmpty(newPass) || Basics.isEmpty(confirmNewPass)) {
						PopupBasics.alert("Password cannot be empty");
						dlg.toFront();
					} else if (!(newPass.equals(confirmNewPass))) {
						PopupBasics.alert("Passwords do not match");
						dlg.toFront();
					} else if (newPass.equals(oldPass)) {
						PopupBasics.alert("Old and new password cannot be same");
						dlg.toFront();
					} else {
						url = new URL(codebase + "/cgaccs?op=savePasswd&email=" + emailAddress
								+ "&oldPassword=" + URLEncoder.encode(oldPass, "UTF-8") + "&newPassword="
								+ URLEncoder.encode(newPass, "UTF-8"));

						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setDoOutput(true);
						conn.setDoInput(true);
						InputStream is = conn.getInputStream();
						final BufferedReader br = new BufferedReader(new InputStreamReader(is));

						String line;
						while ((line = br.readLine()) != null) {
							if (line.contains("mis-match")) {
								PopupBasics.alert("Password not changed");
								dlg.toFront();
								break;
							}
							if (line.contains("successfully")) {
								PopupBasics.alert("Password changed successfully");
								//dlg.dispose();
								break;
							}
						}
					}
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		};

		final JButton save = SwingBasics.getButton("Submit", MmsIcons.getYesIcon(), 'y', submitAction, "");
		dlg.getRootPane().setDefaultButton(save);

		final JButton noButton = SwingBasics.getButton("Cancel", MmsIcons.getCancelIcon(), 'n', new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				SwingBasics.closeWindow(dlg);
			}
		}, "");
		dlg.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				noButton.doClick(132);
			}
		}

		,

		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(save);
		buttonPanel.add(noButton);

		buttonPanel.setBorder(BorderFactory.createEtchedBorder()/* (5,5,5,5)) */);

		dlg.add(textControlsPane, "North");
		dlg.add(buttonPanel, "South");
		dlg.pack();
		dlg.setResizable(true);
		dlg.setLocationRelativeTo(null);
		dlg.setVisible(true);
	}
	public static void addLabelTextRows(JLabel[] labels, JComponent[] textFields, GridBagLayout gridbag,
			Container container) {
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
			
			c.gridwidth = GridBagConstraints.REMAINDER; // end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(new JPanel(), c);
		}
	}

	/*public void savePasswordIfRequired() {
		if (rememberCheckBox != null) {
			if (rememberCheckBox.isSelected()) {
				// System.out.println("Setting password to "+topSecret);
				properties.setProperty(getPasswordPropertyName(), topSecret);
			} else {
				properties.remove(getPasswordPropertyName());
			}
		}
		PropertiesBasics.saveProperties(properties, topSecretFile, "");
		System.out.println("Saved " + topSecretFile.getAbsolutePath());

	}
	*/
	//http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html
	public boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
	}

	public byte[] getEncryptedPassword(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		// specifically names SHA-1 as an acceptable hashing algorithm for
		// PBKDF2
		String algorithm = "PBKDF2WithHmacSHA1";
		// SHA-1 generates 160 bit hashes, so that's what makes sense here
		int derivedKeyLength = 160;
		// Pick an iteration count that works for you. The NIST recommends at
		// least 1,000 iterations:
		// http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		// iOS 4.x reportedly uses 10,000:
		// http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
		int iterations = 20000;
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

		return f.generateSecret(spec).getEncoded();
	}

	public byte[] generateSalt() throws NoSuchAlgorithmException {
		// VERY important to use SecureRandom instead of just Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		// Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		byte[] salt = new byte[8];
		random.nextBytes(salt);

		return salt;
	}

	private void initSwing() {
		//edu.stanford.facs.swing.Basics.setFontFace("Arial");
		Basics.gui=PopupBasics.gui;
		//dialog=setupDialog();
		PersonalizableTableModel.setRootDir(System.getProperty("user.home")+"/test_table");
//		SwingBasics.doDefaultLnF();
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}
		final ColorPreferences colorProperties = ColorPreferences.instantiate();
		colorProperties.setCurrentPreferences();
		SwingBasics.resetDefaultFonts();
		PersonalizableTable.resetDefaultFonts();
	}
	
	private static void initSwing2() {
		//edu.stanford.facs.swing.Basics.setFontFace("Arial");
		Basics.gui=PopupBasics.gui;
		//dialog=setupDialog();
		PersonalizableTableModel.setRootDir(System.getProperty("user.home")+"/test_table");
//		SwingBasics.doDefaultLnF();
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}
		final ColorPreferences colorProperties = ColorPreferences.instantiate();
		colorProperties.setCurrentPreferences();
		SwingBasics.resetDefaultFonts();
		PersonalizableTable.resetDefaultFonts();
	}
	
	/*public static void go(final String productName, String productVersion,
			String productDir, String webRoot, final ImageIcon applicationIcon, final String emailAddress) {

		SimpleServerManager passwordCredentials = new SimpleServerManager(productName, productVersion, productDir, emailAddress, webRoot,
				applicationIcon, false, null);
		
		try {*/
			/*byte sa[]=passwordCredentials.generateSalt();
			byte pa[]=passwordCredentials.getEncryptedPassword("MyPass123", sa);
			String salt=Base64.encode(sa);
			String pass=Base64.encode(pa);
			//String salt=new String(sa);
			//String pass=new String(pa);
			System.out.println(salt);
			System.out.println(pass);*/
/*			String salt="UmtwMrW3fvw=";
			String pass="Hzs1l0TO4fp7IhpOtGX8dvMYGOk=";
			boolean success=false;
			try {
				success = passwordCredentials.authenticate("MyPass123", Base64.decode(pass.getBytes()), Base64.decode(salt.getBytes()));
			} catch (Base64DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Success" + success);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		goingProcess(productName, productVersion, webRoot, applicationIcon, emailAddress, passwordCredentials);
	}
*/
/*	public static void justGo(final String productName, String productVersion,
			String productDir, String webRoot, final ImageIcon applicationIcon, final String emailAddress, String password) {

		System.out.println("Using auto password");
		SimpleServerManager passwordCredentials = new SimpleServerManager(productName, productVersion, productDir, emailAddress, webRoot,
				applicationIcon, false, password);
		goingProcess(productName, productVersion, webRoot, applicationIcon, emailAddress, passwordCredentials);
	}

	private static void goingProcess(final String productName, String productVersion,  String webRoot,
			final ImageIcon applicationIcon, final String emailAddress, final SimpleServerManager passwordCredentials) {

		Authenticator.setDefault(passwordCredentials);
		 passwordCredentials.getPasswordAuthentication();
		String result;
		try {
			result = IoBasics.readWebPage(webRoot + "/verifyUser.jsp?email=" + emailAddress).trim();
		} catch (IOException e) { // code to catch 401 exception
			e.printStackTrace();
			if (e.getMessage().contains("HTTP response code: 401")
					|| e.getMessage().contains("Server redirected too many")) {
				result = "FAILED";
			} else { // assuming it's a connectivity problem, allow
				// to continue
				result = "OFFLINE";
			}
		}

		if (result.equals("OK") || result.equals("OFFLINE")) {
			System.out.println("Authentication OK");
			passwordCredentials.savePasswordIfRequired();
		}
	}*/
}
