package com.MeehanMetaSpace.swing;
import javax.swing.*;
import java.awt.event.*;

import com.MeehanMetaSpace.*;

import java.awt.*;

public class DisabledExplainer {

	
	private final JComponent ab;
	
	private final Color originalForegroundColor;
	private String originalToolTipText;
	private String disabledToolTipText;
	public String getDisabledToolTipText(){
		return disabledToolTipText;
	}
	private final MouseAdapter ma=new MouseAdapter(){
	    public void mousePressed(final MouseEvent event) {
	        showDisabledText(event);
	    }

	};
	
	public void setAndShowToolTip(final String toolTip){
		ab.setToolTipText(toolTip);
		showDisabledText(ab, toolTip);
	}
	
	static void showDisabledText(JComponent c,String disabledToolTipText) {
		if(c instanceof PersonalizableTable) {
			ToolTipOnDemand.getSingleton().show(c,false, 15, 15,null,disabledToolTipText);
		}
		else {
			final String prior=c.getToolTipText();
			c.setToolTipText(disabledToolTipText);
			//ToolTipOnDemand.getSingleton().showWithCloseButton(jc);
			ToolTipOnDemand.getSingleton().show(c,false, c.getWidth(), c.getHeight());
			c.setToolTipText(prior);
		}
	}
	
	public void showDisabledTextOnTheComponent(JComponent c) {
		if(c instanceof PersonalizableTable) {
			ToolTipOnDemand.getSingleton().show(c,false, 15, 15,null,ab.getToolTipText());
		}
		else {
			c.setToolTipText(ab.getToolTipText());
			//ToolTipOnDemand.getSingleton().showWithCloseButton(jc);
			ToolTipOnDemand.getSingleton().show(c,false, 15, 15);
		}
	}
	
	
	void showDisabledText(){
		showDisabledText(null);
	}
	void showDisabledText(final MouseEvent m){
		int delay=ToolTipOnDemand.getSingleton().getDismissDelay();
		ToolTipOnDemand.getSingleton().setDismissDelay(4250);
		
		if (ab instanceof JMenuItem){
			Component parentOfMenuItem=ab.getParent();
			if (!(parentOfMenuItem instanceof JPopupMenu) ||
					!(parentOfMenuItem instanceof JMenu)){
				ToolTipOnDemand.getSingleton().show(ab,false);
				
			} else {
			for(;;){
				if (parentOfMenuItem instanceof JPopupMenu ){
				parentOfMenuItem=( (JPopupMenu)parentOfMenuItem).getInvoker();
				
				} else if (parentOfMenuItem instanceof JMenu){
					parentOfMenuItem=( (JMenu)parentOfMenuItem).getParent();
				} else {
					break;
				}
			}
			if (parentOfMenuItem instanceof JComponent) {				
				final JComponent jc = (JComponent) parentOfMenuItem;
				final ToolTipOnDemand ttod=ToolTipOnDemand.getSingleton();
				ttod.setAlternateLocation(m.getLocationOnScreen());
				ttod.show(jc,false, 15, 15,null, ab.getToolTipText());
				ttod.setAlternateLocation(null);
				
			} else {
				PopupBasics.alert(ab, ab.getToolTipText(), "Alert", false);
			}
			}
		} else {
			final ToolTipOnDemand ttod=ToolTipOnDemand.getSingleton();
			ttod.setAlternateLocation(m.getLocationOnScreen());
			ttod.show(ab,false);
			ttod.setAlternateLocation(null);
		}
		ToolTipOnDemand.getSingleton().hideOnMouseExitIfComponentIsInvisible = false;
		ToolTipOnDemand.getSingleton().setDismissDelay(delay);
	}

	public DisabledExplainer(final JButton ab) {
		this((JComponent)ab);
	}

	public DisabledExplainer(final JMenuItem ab) {
		this((JComponent)ab);
	}

	public DisabledExplainer(final JComponent ab) {
		this.ab = ab;
		if (!ab.isEnabled()){
			originalForegroundColor=ab instanceof AbstractButton ? UIManager.getColor("Button.foreground"):SystemColor.text;
		} else {
			originalForegroundColor=ab.getForeground();
		}

	}
	
	static String GENERAL_COMPLAINT="This operation is ONLY enabled if you first ";
	static StringBuilder startComplaint(final String op){
		final StringBuilder sb= new StringBuilder(GENERAL_COMPLAINT);
		sb.append(op);
		return sb;
	}
	static String getSelectionComplaint(final String itemType){
		final StringBuilder sb=startComplaint("select");
		sb.append(' ');
		if (!Basics.isEmpty(itemType)){ 
			sb.append(itemType);
			sb.append(' ');		
		} 
		sb.append("items.");
		return sb.toString();
	}
	
	public void setEnabledIfSelected(final boolean enabled, final String nameOfOperation, final String itemType) {
		setEnabled(enabled, nameOfOperation, getSelectionComplaint(itemType));
	}
	
	public boolean setEnabled(final String _anomaly) {
		if (_anomaly != null) {
			String anomaly = _anomaly;
			if (!anomaly.toLowerCase().startsWith("<html>")) {
				anomaly = Basics.toHtmlUncentered(anomaly);
			}
			disable(anomaly);
			return false;
		}
		enable();
		return true;
	}
	
	private String getOperationTitle(final String _nameOfOperation){
		final String nameOfOperation;
		if (Basics.isEmpty(_nameOfOperation)){
			final String s=ab instanceof AbstractButton ? null:((AbstractButton)ab).getText();
			if (Basics.isEmpty(s)){
				nameOfOperation="This operation";
			}else {
				nameOfOperation=Basics.concat("\"", Basics.stripSimpleHtml(s).trim(),"\"");
			}
		} else {
			nameOfOperation=Basics.concat((Basics.isEmpty(_nameOfOperation)?"This":Basics.concat("\"",_nameOfOperation.trim(),"\"")), " ");
		}
		return Basics.concat(nameOfOperation, " is disabled."); 
	}
	
	public boolean setEnabled(final boolean ok,
			final String msgExplainingWhyDisabled) {
		return setEnabled(ok,null,msgExplainingWhyDisabled);
	}
	
	public boolean setAlreadyDoing(
			final String _nameOfOperation){
		return setEnabled(false, _nameOfOperation, ALREADY_PEFORMING_THIS_OPERATION);
	}
	int debug;
	public boolean setEnabled(final boolean ok,
			final String _nameOfOperation,
			final String msgExplainingWhyDisabled) {
		if (ok) {
			if (msgExplainingWhyDisabled!=null){
				disabledToolTipText=msgExplainingWhyDisabled;
			}
			enable();
			return true;
		} else {
			String nameOfOperation=_nameOfOperation != null && _nameOfOperation.startsWith("<html>")?Basics.stripSimpleHtml(_nameOfOperation):_nameOfOperation;
			if (ab instanceof AbstractButton){
				if (Basics.isEmpty(nameOfOperation)){
					nameOfOperation=( (AbstractButton)ab).getText();
					if (nameOfOperation != null){
						nameOfOperation=Basics.stripSimpleHtml(nameOfOperation.trim());
					}
				}
			}
			if (ALREADY_PEFORMING_THIS_OPERATION.equals(msgExplainingWhyDisabled)){
				disable(Basics.toHtmlUncenteredSmall(getOperationTitle(nameOfOperation), msgExplainingWhyDisabled), SwingBasics.getAlreadyDoingForeground(), SwingBasics.getAlreadyDoingBackground());
				if (ab instanceof JMenuItem){
					( (JMenuItem)ab).setIcon(MmsIcons.getNextIcon());
					ab.setOpaque(true);
				}
			} else {
				if (msgExplainingWhyDisabled != null && msgExplainingWhyDisabled.startsWith("<html>") && (msgExplainingWhyDisabled.contains("<h4>") || msgExplainingWhyDisabled.contains("<h3"))){
					disable(msgExplainingWhyDisabled);
				} else {
					disable(Basics.toHtmlErrorUncentered(
					getOperationTitle(nameOfOperation), msgExplainingWhyDisabled));
				}
			}
			return false;
		}
	}
	

	public void resetOriginalToolTipMessage(){
		ab.setToolTipText(originalToolTipText);
	}

	public void setOriginalToolTipMessage(final String toolTip){
		this.originalToolTipText=toolTip;
		resetOriginalToolTipMessage();
	}

	public void enable() {
		if (!ab.isEnabled()) {
			ab.removeMouseListener(ma);
			ab.setEnabled(true);
			ab.setForeground(originalForegroundColor);
		}
		if (originalToolTipText != null) {
			if (ab instanceof AbstractButton){
				final String txt=((AbstractButton) ab)
				.getText();
				if (!Basics.isEmpty(txt)){
					if(!originalToolTipText.contains("<html>")){
						ab.setToolTipText(Basics.toHtmlUncentered(txt, Basics.stripSimpleHtml(originalToolTipText)));
					}else {
						ab.setToolTipText(originalToolTipText);
						
					}
				} else {
					ab.setToolTipText(originalToolTipText);
					
				}
			} else {
				ab.setToolTipText(originalToolTipText);
					
			}
		}
		
	}

	private void disable(final String toolTipText) {
		disable(toolTipText, SystemColor.textInactiveText, null);
	}
	
	private void disable(final String toolTipText, final Color foreground, final Color background) {
		if (ab.isEnabled()) {			
			originalToolTipText = ab.getToolTipText();
			if (foreground != null){
				ab.setForeground(foreground);
			}
			if (background != null){
				ab.setBackground(background);								
			}
			ab.addMouseListener(ma);			
			ab.setEnabled(false);
		}
		disabledToolTipText = toolTipText;
		ab.setToolTipText(disabledToolTipText);
		
	}

	public JMenuItem getMenuItem() {
		return (JMenuItem) ab;
	}

	public JButton getButton() {
		return (JButton) ab;
	}
	
	public boolean isEnabled(){
		return ab.isEnabled();
	}
	public void echo(final DisabledExplainer other){
		if (ab.isEnabled()){
			other.setEnabled(true, null, null);
		} else {
			other.disable(disabledToolTipText);
		}
	}
	public static String ALREADY_PEFORMING_THIS_OPERATION="This is already your current selection.";
	
}
