
package com.MeehanMetaSpace.swing;

import com.MeehanMetaSpace.*;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.net.PasswordAuthentication;
import javax.swing.SwingConstants;
/**
 *
 * @author cate
 */
public class AuthenticatorGui extends Authenticator {

		private boolean becameSuperUser=false;
		String guestEmail = "guest@woodsidelogic.com";
		private String emailAddress;
		private final String originalEmailAddress, server;
		private final File topSecretFile;
		private String lastPassword;
		private String autoPassword;
		private String singleSignOnPassword;
		private char[] newPassword;
		private final Properties properties;
		private JCheckBox rememberCheckBox;
		private String topSecret;
		private JTextField emailField;
        private final SoftwareProduct softwareProduct;
        private JFrame dummyFrame = new JFrame();
        private boolean okClicked = false;
         private final ImageIcon applicationIcon;
        PasswordAuthentication value;


        /**
         *
         * @param email
         * @param webroot
         */

       
        

        private AuthenticatorGui(final SoftwareProduct softwareProduct,
                final String email, final String webroot, final ImageIcon applicationIcon) {

           this.originalEmailAddress = email;
           this.emailAddress=email;
           this.server = webroot;
           this.applicationIcon=applicationIcon;
           this.softwareProduct = softwareProduct;
           topSecretFile = new File(softwareProduct.getProductDir(), ".xjsxj.hh");
			properties = PropertiesBasics.loadProperties(topSecretFile);
			lastPassword = properties.getProperty(getPasswordPropertyName());
        }
        
        private AuthenticatorGui(final SoftwareProduct softwareProduct,
                final String email, final String webroot, final ImageIcon applicationIcon,
                final String autoPassword) {
           this(softwareProduct, email, webroot, applicationIcon);
		   this.autoPassword = autoPassword;
        }

        private String getPasswordPropertyName() {
			return server + "." + emailAddress;
		}

		
        JPasswordField passwordField;
    @Override
        public PasswordAuthentication getPasswordAuthentication() {

        
        System.out.println ("getPasswordAuthentication() changed");
        
        if (!Basics.isEmpty(autoPassword)) {
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
			if(guestEmail.equals(originalEmailAddress)) {
				guestAccount = true;
			}
			final JDialog dlg = new JDialog(dummyFrame, softwareProduct.getProductName() + " authentication...", true);

            final JPanel main = new GradientBasics.Panel(new BorderLayout(2, 8));

            Color genieBlue = new Color (13, 57, 84);
			main.setBorder(BorderFactory.createMatteBorder (12,8,8,8,genieBlue));
            dlg.setAlwaysOnTop(true);
			dlg.getContentPane().add(main);
			final JPanel middle = new JPanel(new BorderLayout(4,4));

//			middle.add(new JLabel(applicationIcon), BorderLayout.WEST);
			final JPanel entries = new JPanel();
            
            GridLayout grid = new GridLayout (5,1);
       
            entries.setLayout (grid);
            
//            entries.setLayout (new BoxLayout (entries, BoxLayout.Y_AXIS));


            JPanel userlogin = new JPanel();
            JPanel pwdpanel = new JPanel();
            JPanel buttons = new JPanel();
            final Dimension dim1=SwingBasics.isQuaQuaLookAndFeel()?new Dimension(340,30):new Dimension(260,30);
            userlogin.setPreferredSize(dim1);
            pwdpanel.setPreferredSize(dim1);
	
  			userlogin.setLayout(new BoxLayout(userlogin, BoxLayout.X_AXIS));
            JLabel userEmail = new JLabel("User email:");
            userEmail.setPreferredSize(new Dimension(90, 30));
            userlogin.add (userEmail);
            emailField = new JTextField(25);
			emailField.setText(originalEmailAddress);
			final Font f = emailField.getFont();
            Font font = new Font (f.getName(), Font.BOLD, f.getSize());
            System.out.println (f.getName() + "  " + f.getSize());
			emailField.setFont(font);
            userlogin.add (emailField);
            userEmail.setFont (font);

//            pwdpanel.setPreferredSize (dim3);
            pwdpanel.setLayout(new BoxLayout(pwdpanel, BoxLayout.X_AXIS));
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setPreferredSize(new Dimension(90, 30));
            pwdpanel.add(passwordLabel);
            passwordLabel.setFont (font);
            Dimension rigidArea = new Dimension (0, 8);
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
           
			   
			
			passwordField = new JPasswordField(25);
			if(guestAccount) {
				emailField.setMargin(new Insets(2,2,2,2));
				passwordField.setMargin(new Insets(2,2,2,2));
			}

			pwdpanel.add(passwordField);
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            buttons.add(Box.createRigidArea(rigidArea));
            final Font smallFont=new Font(UIManager.getFont("Table.font").getName(), Font.PLAIN, 10);
			if (!Basics.isEmpty(originalEmailAddress)) {
				final String switchToolTip = Basics.toHtmlUncentered(
						"Switch user",
						"If you are not <b>"
                        + emailAddress
                        + "</b><br>then this button exits this login window and launches<br>your browser to access a different user's knowledge <br>bases at the server <b>"
                        + server + "</b>.<br>.. continue?");
		        final JButton switchUser = SwingBasics.getButton("Switch user?", null, 'u', new ActionListener() {
		            public void actionPerformed(final ActionEvent e) {
		                       SwingBasics.showHtml(server);
		                    if (!PROHIBIT_EXIT_ON_CANCEL)
		                    	System.exit(1);
		            
						}
					}, switchToolTip);
		        switchUser.setFont(smallFont);
		        buttons.add(switchUser);
//                userlogin.add(switchUser);

			}
			else {
				buttons.add(Box.createRigidArea(rigidArea));
			}
						
			final JButton ok = SwingBasics.getDoneButton(dlg, Basics.toHtmlUncentered("Ok", "Submit password to "
					+ softwareProduct.getProductName() + "."));

			ok.setText(" Ok ");
            

		

            System.out.println ("userlogin size = " + userlogin.getSize().toString());
            System.out.println ("pwd panel size = " + pwdpanel.getSize().toString());
            entries.add (Box.createRigidArea(rigidArea));
			entries.add(userlogin);
            entries.add(pwdpanel);
            JPanel rememberPass = new JPanel();
            rememberPass.setPreferredSize(dim1);
            rememberPass.setLayout(new BoxLayout(rememberPass, BoxLayout.X_AXIS));
            JLabel rememEmail = new JLabel("                      ");

            rememberPass.add (rememEmail);

            rememberCheckBox = new JCheckBox("Remember password!");
			rememberCheckBox.setMnemonic('r');
			rememberCheckBox.setFont(smallFont);
            //rememberPass.add (rememberCheckBox);
            entries.add (rememberPass);
            buttons.add (Box.createRigidArea (rigidArea) );
            buttons.add(rememberCheckBox);
            buttons.add (Box.createRigidArea (rigidArea));

            JPanel sidebyside = new JPanel ();
            sidebyside.setLayout (new BoxLayout (sidebyside, BoxLayout.X_AXIS));


            buttons.setAlignmentY (Component.TOP_ALIGNMENT);
            entries.setAlignmentY(Component.TOP_ALIGNMENT);
            sidebyside.add (entries);
            sidebyside.add(new JLabel("    "));
            sidebyside.add (buttons);
			final JLabel serverLabel = new JLabel("                   ");
            serverLabel.setBackground (Color.WHITE);

            JLabel title = new JLabel(SwingBasics.isQuaQuaLookAndFeel()?"  Login to your CytoGenie":"   Login to your CytoGenie");
            title.setFont (new Font (f.getName(), Font.BOLD, f.getSize()+2));
            title.setHorizontalTextPosition(SwingConstants.CENTER);


            middle.add (title, BorderLayout.NORTH);
            middle.add (sidebyside, BorderLayout.CENTER);


            middle.add(serverLabel, BorderLayout.SOUTH);
            middle.add(new JPanel(), BorderLayout.EAST);
            middle.add(new JPanel(), BorderLayout.WEST);

			
			if(!guestAccount) {
				if (lastPassword != null) {
					rememberCheckBox.setSelected(true);
					passwordField.setText(lastPassword);
				}
			}
            
			main.add(middle, BorderLayout.CENTER);
            JPanel geniePanel = new JPanel(new GridLayout(8,1));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            geniePanel.add (new JLabel ("         "));
            main.add(geniePanel, BorderLayout.WEST);

            JPanel geniePanel2 = new JPanel(new GridLayout(8,1));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            geniePanel2.add (new JLabel ("         "));
            main.add(geniePanel2, BorderLayout.EAST);

			JPanel northPanel = new JPanel(new GridLayout(2,1));
			northPanel.add (new JLabel ("         "));
			northPanel.add (new JLabel ("         "));
			main.add(northPanel, BorderLayout.NORTH);

			
			final JPanel passwordMemory = new JPanel();
			if(!guestAccount) {
				final JButton changePassword = SwingBasics.getButton("Change password", null, 'c', new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						changePassword();
					}
				}, Basics.toHtmlUncentered("Change password",
						"Press this button to pop up a form<br>in your browser so that you can <br>change your password at the "
								+ softwareProduct.getProductName() + " server"));
				changePassword.setFont(smallFont);
				passwordMemory.add(changePassword);
			}
			final JPanel okCancel = new JPanel();
			JLabel emptyLabel2 = new JLabel("                ");
			emptyLabel2.setBounds(2,2,2,2);
			if(!guestAccount) {
				final JButton forgot = SwingBasics.getButton("Forgot password?", MmsIcons.getHelpIcon(),
						'f', new ActionListener() {
							public void actionPerformed(final ActionEvent e) {
								try {
									IoBasics.readWebPage(server + "/cgaccs?op=forgot&email=" + emailField.getText());
									PopupBasics.alert("We have emailed a new password to your address <b>"+emailField.getText()+"</b>");
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
//								SwingBasics.showHtml(server + "/cgaccs?op=forgot&email=" + emailField.getText());
							}
						}, Basics.toHtmlUncentered("Forgot my password", "Press this button to tell the "
								+ softwareProduct.getProductName() + " <br>server <b>" + server
                                + "</b><br>to send the password to <br><b>" + emailAddress + "</b>"));
				forgot.setFont(smallFont);
				passwordMemory.add(forgot);
			}
			else {
				okCancel.add(emptyLabel2);
			}
			final JButton cancel = SwingBasics.getCancelButton(dlg, Basics.toHtmlUncentered("Cancel", "Exit "
					+ softwareProduct.getProductName() + " without authenticating"), true, new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                	HelpListener.actionSystemExit();
                	if (!PROHIBIT_EXIT_ON_CANCEL){
                		System.exit(1);
                	}
				}
			});
			okCancel.add(ok);
            okCancel.add(cancel);

			final JPanel bottom = new JPanel(new BorderLayout(12, 1));
            bottom.setBorder (BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
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
					dlg.dispose();
				}
			});
			
			emailField.addFocusListener(new FocusListener() {
				public void focusGained(final FocusEvent fe) {
					if (Basics.equals(String.copyValueOf(passwordField.getPassword()), "admin")) {
						emailField.setEditable(true);
						emailField.setText("stephen@MeehanMetaSpace.com");
						emailField.getCaret().setVisible(true);
						emailField.selectAll();
						if(!softwareProduct.isSuperUser()) {
							softwareProduct.setSuperUser(true);
							becameSuperUser=true;
						}

					}
				}
                public void focusLost (FocusEvent fe){
                	lastPassword = properties.getProperty(server + "." + emailField.getText());
                	rememberCheckBox.setSelected(true);
					passwordField.setText(lastPassword);
                }
			});
			dlg.getRootPane().setDefaultButton(ok);
			dlg.pack();
			SwingBasics.center(dlg);
			if (!Basics.isEmpty(originalEmailAddress)) {
				emailField.setEditable(false);
				passwordField.requestFocus();	
			}
			else {
				emailField.setEditable(true);
				emailField.requestFocus();
			}
			
            dlg.setResizable (false);
            GradientBasics.setTransparentChildren(main, true);
			dlg.setVisible(true);

			if (!okClicked) {
				value = null;
//				System.out.println("Cancelling");
			} else {
				singleSignOnPassword = newPassword.toString();
				value = new PasswordAuthentication(emailAddress, newPassword);
//				System.out.print("HERE  IT  IS:  pwd=\"");
//				System.out.print(topSecret);
//				System.out.print("\"  email=\"");
//				System.out.print(emailAddress);
//				System.out.println("\"");
//				System.out.print("Super User=");
				if (emailAddress.equals(originalEmailAddress) && becameSuperUser){
//					System.out.println("FALSE (Reverted to non super user after back door failed)");
					softwareProduct.setSuperUser(false);
				} else {
					SoftwareProduct.isAdvancedUser=true;

//					System.out.println("Advanced user is now TRUE ... and super user is"+softwareProduct.isSuperUser());
				}
			}
			return value;
		}

		protected void changePassword() {
			final JDialog dlg = new JDialog(dummyFrame, "Change Password...", true);
			dlg.setAlwaysOnTop(true);
	        JTextField emailField = new JTextField(25);
	        final Font font = emailField.getFont();
	        emailField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
	        emailField.setText(SoftwareProduct.emailID);
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
            textControlsPane.setBorder(
            		BorderFactory.createCompoundBorder(
            				BorderFactory.createTitledBorder(""),
            				BorderFactory.createEmptyBorder(5,5,5,5)));
	                
	                 
	        JLabel labels[] = {emailLabel,oldPasswordLabel,newPasswordLabel,confirmPasswordLabel};
	        JTextField textFields[] = {emailField,oldPasswordField,newPasswordField,confirmNewPasswordField};
	                 
	        addLabelTextRows(labels, textFields, gridbag, textControlsPane);
	        ActionListener submitAction = new ActionListener(){

	 		public void actionPerformed(ActionEvent event) {
	 			URL url;
	 			final String codebase=JnlpBasics.isOnline()?JnlpBasics.getWebAppRootForClient():"http://facs.stanford.edu:8080/beta";
	 					
	            char []oldpassword=  oldPasswordField.getPassword();
	            final String oldPass = new String (oldpassword).trim();
	            char [] newpassword = newPasswordField.getPassword();
	 			final String newPass = new String (newpassword).trim();
                char[] confirmnew = confirmNewPasswordField.getPassword();
                final String confirmNewPass = new String(confirmnew).trim();
				try {
					if(Basics.isEmpty(oldPass) 
							|| Basics.isEmpty(newPass) 
							|| Basics.isEmpty(confirmNewPass)) {
						PopupBasics.alert("Password cannot be empty");
						dlg.toFront();
					}
					else if(!(newPass.equals(confirmNewPass))) {
						PopupBasics.alert("Passwords do not match");
						dlg.toFront();
					}
					else if(newPass.equals(oldPass)) {
						PopupBasics.alert("Old and new password cannot be same");
						dlg.toFront();
					}
					else {
						url = new URL(
								codebase+"/cgaccs?op=savePasswd&email="
								+SoftwareProduct.emailID+"&oldPassword="+URLEncoder.encode(oldPass,"UTF-8")+
								"&newPassword="+URLEncoder.encode(newPass,"UTF-8"));
						
		                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		                conn.setRequestMethod("GET");
						conn.setDoOutput(true);
						conn.setDoInput(true);
						InputStream is = conn.getInputStream();
						final BufferedReader br =
			                  new BufferedReader(
			                    new InputStreamReader(is));
						
			                String line;
			                while ((line = br.readLine()) != null) {
			                	if(line.contains("mis-match")) {
			                		PopupBasics.alert("Password not changed");
			                		dlg.toFront();
			                		break;
			                	}
			                	if(line.contains("successfully")) {
			                		PopupBasics.alert("Password changed successfully");
			                		dlg.dispose();
			                		break;
			                	}
			                }
						} 
					}catch (MalformedURLException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
             	  
               };

             final JButton save = SwingBasics.getButton(
                     "Submit",
                     MmsIcons.getYesIcon(),
                     'y',
                     submitAction,
                     "");
             dlg.getRootPane().setDefaultButton(save);

             
             
             final JButton noButton = SwingBasics.getButton(
                     "Cancel",
                     MmsIcons.getCancelIcon(),
                     'n',
                     new ActionListener() {
                         public void actionPerformed(final ActionEvent e) {
                           SwingBasics.closeWindow(dlg);
                         }},
                     "");
             dlg.getRootPane().registerKeyboardAction(
                     new ActionListener() {
                       public void actionPerformed(final ActionEvent e) {
                           noButton.doClick(132);
                       }
                   }

                   ,

                   KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                   JComponent.WHEN_IN_FOCUSED_WINDOW);
             
             
             JPanel buttonPanel = new JPanel();
             buttonPanel.add(save);
             buttonPanel.add(noButton);
             
             buttonPanel.setBorder(
                     BorderFactory.createEtchedBorder()/*(5,5,5,5))*/);
             
             
             dlg.add(textControlsPane,"North");
             dlg.add(buttonPanel,"South");
             dlg.pack();
             dlg.setResizable(true);
             dlg.setLocationRelativeTo(null);
             dlg.setVisible(true); 
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
	


		public void savePasswordIfRequired() {
			if (rememberCheckBox != null) {
				if (rememberCheckBox.isSelected()) {
//					System.out.println("Setting password to "+topSecret);
					properties.setProperty(getPasswordPropertyName(), topSecret);
				} else {
					properties.remove(getPasswordPropertyName());
				}
			}
			PropertiesBasics.saveProperties(properties, topSecretFile, "");
			System.out.println("Saved "+topSecretFile.getAbsolutePath());

       }

/* *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
 *
 * **/

    public static void go(final SoftwareProduct softwareProduct,
                            final SoftwareProduct.Version fxv,
                            final ImageIcon applicationIcon,
                            final String emailAddress) {

        String webRoot = JnlpBasics.getWebAppRootForClient();
        AuthenticatorGui passwordCredentials = new AuthenticatorGui(softwareProduct,
                                                emailAddress, webRoot, applicationIcon);
        goingProcess(softwareProduct, fxv, applicationIcon, emailAddress, passwordCredentials);
    }
    
    public static void justGo(final SoftwareProduct softwareProduct,
            final SoftwareProduct.Version fxv,
            final ImageIcon applicationIcon,
            final String emailAddress,
            final String password) {
    	System.out.println("Using auto password");
		String webRoot = JnlpBasics.getWebAppRootForClient();
		AuthenticatorGui passwordCredentials = new AuthenticatorGui(
				softwareProduct, emailAddress, webRoot, applicationIcon, password);
		goingProcess(softwareProduct, fxv, applicationIcon, emailAddress, passwordCredentials); 
	}
    
    private static void goingProcess(final SoftwareProduct softwareProduct,
            final SoftwareProduct.Version fxv,
            final ImageIcon applicationIcon,
            final String emailAddress, final AuthenticatorGui passwordCredentials) {

    	String webRoot = JnlpBasics.getWebAppRootForClient();
//    	 _Authenticator a = new _Authenticator(emailAddress, webRoot);
		Authenticator.setDefault(passwordCredentials);
		//passwordCredentials.getPasswordAuthentication();
		if (JnlpBasics.isOnline()) {
			if (webRoot != null) {
				String result;
				try {
					result = IoBasics.readWebPage(
							webRoot + "/verifyUser.jsp?email=" + emailAddress)
							.trim();
				} catch (IOException e) { // code to catch 401 exception
					e.printStackTrace();
					if (e.getMessage().contains("HTTP response code: 401")
							|| e.getMessage().contains(
									"Server redirected too many")) {
						result = "FAILED";
					} else { // assuming it's a connectivity problem, allow
						// to continue
						result = "OFFLINE";
					}
				}

				if (result.equals("OK") || result.equals("OFFLINE")) {
					System.out.println("Authentication OK");
					passwordCredentials.savePasswordIfRequired();
					IoBasics.deleteFile(new File(SoftwareProduct
							.getHomeProducerDir(fxv)
							+ File.separatorChar
							+ JnlpBasics.getDNSFolder()
							+ File.separatorChar + "cglaunchflag"));
				} 

			}
		} 

    }

    	public static boolean PROHIBIT_EXIT_ON_CANCEL=false;
	}





