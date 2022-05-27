package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;



public class WoodsideMenu {
    public enum Style {
    	NONE,
    	CONTEXT,
    	TASK    	
    };
    public final static Color 
    
    CONTEXT_BACKGROUND_DEFAULT=new Color(0,148,214),
    CONTEXT_FOREGROUND_DEFAULT=Color.white, 
    CONTEXT_DISABLED_FOREGROUND_DEFAULT=Color.gray,
    
    TASK_BACKGROUND_DEFAULT=new Color(15, 59, 91),
    TASK_FOREGROUND_DEFAULT=Color.white, 
    TASK_DISABLED_FOREGROUND_DEFAULT=Color.blue;
    

    public final static String TASK="Woodside task menu";
    public final static String TASK_DISABLED_FOREGROUND=TASK+"."+"disabledForeground";
    public final static String TASK_FOREGROUND=TASK+"."+"foreground";
    public final static String TASK_BACKGROUND=TASK+"."+"background";
    
    
    public final static String CONTEXT="Woodside context menu";
    public final static String CONTEXT_DISABLED_FOREGROUND=CONTEXT+"."+"disabledForeground";
    public final static String CONTEXT_FOREGROUND=CONTEXT+"."+"foreground";
    public final static String CONTEXT_BACKROUND=CONTEXT+"."+"background";

	public static Color getTaskDisabledForeground(){
		final Color color=UIManager.getColor(TASK_DISABLED_FOREGROUND);
		return color;
	}

	public static Color getContextDisabledForeground(){
		final Color color=UIManager.getColor(CONTEXT_FOREGROUND);
		return color;
	}

	public static Color getTaskForeground(){
		final Color color=UIManager.getColor(TASK_FOREGROUND);
		return color;
	}


	public static Color getTaskBackground(){
		final Color color=UIManager.getColor(TASK_BACKGROUND);
		return color;
	}


	public static Color getContextForeground(){
		final Color color=UIManager.getColor(CONTEXT_FOREGROUND);
		return color;
	}
	

	public static Color getContextBackground(){
		final Color color=UIManager.getColor(CONTEXT_BACKROUND);
		return color;
	}
	
	public static Color getDisabledForeground(final Style style){
		return style == Style.TASK ? getTaskDisabledForeground():getContextDisabledForeground();
	}
	
	public static Color getForeground(final Style style){
		return style == Style.TASK ? getTaskForeground():getContextForeground();
	}

	public static Color getBackground(final Style style){
		return style == Style.TASK ? getTaskBackground():getContextBackground();
	}

	public static void setDisabledForeground(final JComponent b, final Style style){
		b.setForeground(getDisabledForeground(style));
	}

	public static void setForeground(final JComponent b, final Style style){
		b.setForeground(getForeground(style));
	}
	
	public static JPanel newTaskPanel(){
		return newTaskPanel(new FlowLayout(FlowLayout.LEFT));
	}
	
	public static JPanel newTaskPanel(final LayoutManager lm){
		return new JPanel(lm==null?new FlowLayout():lm){
			public Color getBackground() {
		        final Color background = WoodsideMenu.getBackground(WoodsideMenu.Style.TASK);
		        if (background != null) {
		            return background;
		        }
		       return super.getBackground();
		    }
		  
		   	 public void setOpaque(final boolean b){
		   		 super.setOpaque(true);
		   	 }

		};
		
	}

	public static void setBackground(final JComponent b, final Style style){
		b.setOpaque(true);
		b.setBackground(getBackground(style));
	}

	public static void setColors(final JComponent b, final Style style){
		
		setForeground(b, style);
		if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle()) && b.getParent()!=null){
			b.getParent().setBackground(getBackground(Style.TASK));
		}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()&& b.getParent()!=null){
			b.getParent().setBackground(getBackground(Style.TASK));
		}
	}
	
	private static JFrame getMainFrame(final Component cmp){
		if(cmp==null){
			return null;
		}
		Component parent=cmp.getParent();
		if(parent instanceof JFrame){
			return (JFrame) parent;
		}
		JFrame frame=getMainFrame(parent);
		return frame;
	}
	
	public static boolean isHoveringOver=false;
	public static void set(final JButton b,final Style style){
		b.setUI(new TaskButton.TaskButtonUI());
		setColors(b, style);
		Properties properties = PopupBasics.getProperties(null);
    	if (properties != null && SwingBasics.ButtonPreferences_DropDownMenuStyleOption_ON.equals(
    			properties.getProperty(SwingBasics.ButtonPreferences_DropDownMenuStyle))) {
    		b.addMouseListener(new MouseAdapter(){
    		    public void mouseEntered(final MouseEvent e) {
    		    	JFrame frame=getMainFrame(b);
    		    	if (frame!=null && frame.isActive()) {
    					if ((b instanceof TaskButton)) {
    						((TaskButton) b).startedByMouse = true;
    					}
    					if (b instanceof MouseHoverButton) {
    						((MouseHoverButton) b).startedByMouse = true;
    					}
    					if (b instanceof HelpBasics.Button) {
    						((HelpBasics.Button) b).startedByMouse = true;
    					}
    					if(!(b instanceof HelpBasics.Button) || ((HelpBasics.Button)b).isHoveringAllowed()){
    					isHoveringOver=true;
    					b.doClick();
    					isHoveringOver=false;
    					}
    					if (b instanceof TaskButton) {
    						((TaskButton) b).startedByMouse = false;
    					}
    					if (b instanceof MouseHoverButton) {
    						((MouseHoverButton) b).startedByMouse = false;
    					}
    					if (b instanceof HelpBasics.Button) {
    						((HelpBasics.Button) b).startedByMouse = false;
    					}
    				}
    			}
    		});	
    	}
		b.setBorderPainted(false);	
//			b.setBorder(null);
	}
	
	public static void setFinish(final JButton b, final Style style) {
		setColors(b, style);
		b.addMouseListener(new MouseAdapter() {
			public void mouseEntered(final MouseEvent e) {
				b.setBackground(java.awt.Color.gray);
				if(b instanceof TaskButton && WoodsideMenu.Style.TASK.equals(((TaskButton)b).getStyle()) && b.getParent()!=null){
					b.getParent().setBackground(Color.gray);
				}else if(b instanceof HelpBasics.Button && ((HelpBasics.Button)b).isWoodSideMenuHelpButton()&& b.getParent()!=null){
					b.getParent().setBackground(Color.gray);
				}
			}
			public void mouseExited(final MouseEvent e) {
				setColors(b, style);				
			}
		});
		b.setBorderPainted(false);
	}

}
