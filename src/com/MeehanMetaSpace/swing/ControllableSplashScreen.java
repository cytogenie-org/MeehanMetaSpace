package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ControllableSplashScreen extends Thread {
	  private boolean stop = false;
	  JDialog frame;
	  
	  public ControllableSplashScreen(JDialog owner, String title) {
		  frame = new JDialog(owner);
		    JPanel content = (JPanel)frame.getContentPane();
		    content.setBackground(Color.white);

		    int width = MmsIcons.getSplashIcon().getIconWidth() + 50;
		    int height =MmsIcons.getSplashIcon().getIconHeight() + 50;
		    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		    int x = (screen.width-width)/2;
		    int y = (screen.height-height)/2;
		    frame.setBounds(x,y,width,height);

		    JLabel label = new JLabel(MmsIcons.getSplashIcon());
		    JLabel copyrt = new JLabel
		      ("Loading " +title + " .....", JLabel.CENTER);
		    copyrt.setFont(new Font("Sans-Serif", Font.BOLD, 20));
		    content.add(label, BorderLayout.CENTER);
		    content.add(copyrt, BorderLayout.SOUTH);
		    content.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
		    frame.setTitle(title);
		    frame.setResizable(false);
		    frame.setVisible(true);
	  }
	  
	  public void setStop(boolean s) {
		  stop = s;
	  }
	  
	  public void run() {		  
		  while (!stop) {			    
		  }
		  frame.setVisible(false);		
	  }
	  
}