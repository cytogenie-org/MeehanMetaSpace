package com.MeehanMetaSpace.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.Pel;

public class DoNotAlertAgainIdiom {
	private String answer;
	private final String title, question, property;

	public DoNotAlertAgainIdiom(final String title, final String question, final String property, final boolean defaultAnswerIsYes){
		this.property=property;
		answer=PopupBasics.getProperties(null).getProperty(property, "0");
		this.question=question;
		this.title=title;
	}
	
	
	private static String getAnswer(final int answerIndex){
		final String behavior;
		switch (answerIndex){
		default:
			behavior="Yes";
			break;
		case 1:
			behavior="Always";
			break;
		case 2:
			behavior="No";
			break;
		case 3:
			behavior="Never";
			break;
		}
		return behavior;
	}

	private int getAnswerIndex(){
		for (int i=1;i<4;i++){
			if (getAnswer(i).equals(answer)){
				return i;
			}
		}
		return 0;
	}
	
	private void saveAnswer(final int answerIndex){
		answer=getAnswer(answerIndex);
		final Properties properties=PopupBasics.getProperties(null);
		properties.setProperty(property, answer);
		PopupBasics.PROPERTY_SAVIOR.save(properties);
	}

	private void askWhatToDoIfNotAsking(final Component parent){
		int answerIndex=getAnswerIndex();
		final String []options=new String[]{
				getAnswer(1),
				getAnswer(3),
			};
		answerIndex=PopupBasics.showOptionDialog(
			parent, 
			question, 
			title, 
			options, 
			new int[]{'a','v'}, 
			new Icon[]{
					MmsIcons.getAcceptIcon(),
					MmsIcons.getCloseIcon()
			}, 
			null, 
			null,
			true, 
			false, 
			null, 
			null,
			null);
		if (answerIndex>=0){
			saveAnswer(answerIndex==0?1:3);
		}
		
	}
	
	void setMenuItemText(final JMenuItem mi){
		if (isAlerting()){
			final String negativeQuestion=Basics.concat("Do not ask \"", question, "\"");
			mi.setText(negativeQuestion);
		} else {
			final String positiveQuestion=Basics.concat("Ask \"", question, "\"");
			mi.setText(positiveQuestion);
		}
	}
	
	private String undoMsg;
	public void setHowToUndoMsg(final String undoMsg){
		this.undoMsg=undoMsg;
	}
	
	private void alertHowToUndo(final Component parent){
		PopupBasics.alert(parent, Basics.concat("<html>To re-activate the question \"", question, "\"<br>...", undoMsg, "</html>"));	
	}
	
	public JMenuItem getMenuItem(final Component parent){
		final JMenuItem mi=new JMenuItem();
		setMenuItemText(mi);
		mi.setIcon(MmsIcons.getHelpIcon());
		mi.addActionListener(new ActionListener() {			
			public void actionPerformed(final ActionEvent e) {
				if (!isAlerting()){
					saveAnswer(0);
				} else {
					askWhatToDoIfNotAsking(parent);
					alertHowToUndo(parent);
				}
				setMenuItemText(mi);
			}
		});
		return mi;
	}
	
	public boolean alert(final Component parent, final String detailedMsg){
		int answerIndex=getAnswerIndex();
		if (isAlerting()){
			final JCheckBox cb=new JCheckBox("Remember my answer");
			final String []options=new String[]{"Ok"};
			answerIndex=PopupBasics.showOptionDialog2(
				parent, 
				question, 
				title, 
				options, 
				new int[]{'y','n'}, 
				new Icon[]{
						MmsIcons.getYesIcon()
				}, 
				options[0], 
				null,
				false, 
				false, 
				null, 
				detailedMsg, 
				cb);
			if (cb.isSelected()){
				saveAnswer(1);			
				
			} else {
				saveAnswer(2);			
			}
			return answerIndex==0;
		}
		return answerIndex==1;
	}
	
	public boolean isAlerting(){
		final int answerIndex=getAnswerIndex();
		return answerIndex==0 || answerIndex==2;
	}
	
	public boolean isAways(){
		return getAnswerIndex()==1;
	}
	
	
}
