package com.MeehanMetaSpace.swing;


import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;


import com.MeehanMetaSpace.swing.WoodsideMenu.Style;



public class TaskButton extends WaterMarkButton {
    private Icon priorIcon;
    private final Image doingImage, doneImage;
    private Color foreGround;
    private Border border;
    private Dimension sizeWithImage;
    public static TaskButtonUI UI=new TaskButtonUI();
  
    public boolean isOpaque(){
    	return false;
    }
   	 
    public TaskButton(final String label, final Image doingImage, final Image doneImage) {
        super(label, new Image[]{doingImage, doneImage}, false);
        this.doingImage=doingImage;
        this.doneImage=doneImage;
        this.border=getBorder();
        setPressedIcon(SwingBasics.pressedIcon);
    }
    
    public void setUI(ButtonUI ui) {
        super.setUI(new TaskButtonUI());
    }
    
    public static class Reference{
        public TaskButton referenced;
    }

    public TaskButton (
      final TaskButton other,
      final Reference pointToCopyIfOtherIsOnInWorkflow,
      final Workflow workFlow){
        this(other.getRealText(), other.doingImage,other.doneImage, other.priorIcon);
        if (workFlow != null && other.equals(workFlow.getOnButton())){
            pointToCopyIfOtherIsOnInWorkflow.referenced=this;
        }
        setText(other);
        SwingBasics.removeActionListeners(other);
        setFont(other.getFont());
        setMargin(other.getMargin());
        setMnemonic(other.getMnemonic());
        setToolTipText(other.getToolTipText());
        if (other.state == State.DONE){
            setDoneState(other.isEnabled(), other.getIcon());
        } else if (other.state == State.DOING){
            setDoingState(!other.isEnabled());
        } else /* other.state == State.NOT_DONE */{
            setNotDoneState();
        }
    }

    public TaskButton(
      final String label,
      final Image doingImage,
      final Image doneImage,
      final Icon icon) {
        super(label, new Image[]{doingImage, doneImage}, icon, false);
        priorIcon = icon;
        this.doingImage=doingImage;
        this.doneImage=doneImage;
        this.border=getBorder();
    }


    public TaskButton(
      final Action action,
      final Image doingImage,
      final Image doneImage) {
        super(action, new Image[]{doingImage, doneImage});
        this.doingImage=doingImage;
        this.doneImage=doneImage;
        this.border=getBorder();
    }

    public void setIcon(final Icon icon) {
        priorIcon = icon;
        super.setIcon(icon);
    }


    public static enum State {
        NOT_DONE,
        DOING,
        DONE
    };

  private State state;
  public State getState(){
      return state;
  }
  
  private Color notDoingForeground, notDoingBackground;

    private void setDoingState(final boolean disable) {
        state=State.DOING;
        if (disable) {
            setEnabled(false);
        }
        if (!isShowingWaterMarks()) {
        	notDoingForeground=getForeground();
        	notDoingBackground=getBackground();
        	setForeground(Color.blue);
        	setBackground(PersonalizableTable.YELLOW_STICKY_COLOR);
        	if(getParent()!= null){
        	getParent().setBackground(PersonalizableTable.YELLOW_STICKY_COLOR);
        	}
        } else {
            setWaterMarkImage(0);
            SwingUtilities.invokeLater(
                    new Runnable() {
                      public void run() {
                          updateUI();
                      }
                  });
        }

    }

    private void setDoneState(final boolean enable, final Icon doneIcon) {
        state=State.DONE;
        if (enable) {
            setEnabled(true);
        }
        if (!isShowingWaterMarks()) {
        	if (notDoingForeground!=null){
        		setForeground(notDoingForeground);
        		if(getParent() != null){
        			getParent().setBackground(null);
        		}
        		setBackground(null);
        	}
        } else {
            setWaterMarkImage(1);
            SwingUtilities.invokeLater(
                    new Runnable() {
                      public void run() {
                          updateUI();
                      }
                  });
        }
    }


    private void setNotDoneState() {
        state=State.NOT_DONE;
        if (!isShowingWaterMarks()) {
            setForeground(foreGround);
            setBorder(SwingBasics.BUTTON_BORDER);  
            
            if(style==Style.TASK){
            	notDoingBackground = WoodsideMenu.getTaskBackground();
            }else if(style==Style.CONTEXT){
            	notDoingBackground = WoodsideMenu.getContextBackground();
            }
            setBackground(notDoingBackground);
            if(getParent() != null){
    			getParent().setBackground(notDoingBackground);
    		}
            //setFont(SwingBasics.BUTTON_FONT);
            super.setIcon(priorIcon);
        } else {
            removeImage();
        }
        SwingUtilities.invokeLater(
          new Runnable() {
            public void run() {
                updateUI();
            }
        });
    }

    public static class Workflow {
        private TaskButton[] g;
        
        public TaskButton[] getTasks() {
			return g;
		}

		private TaskButton doingButton = null;
        private TaskButton specialTaskButton = null;
        private ActionListener globalAction = null;
        public TaskButton getOnButton() {
            return doingButton;
        }
        
        public int getOnButtonIndex() {
        	for (int i=0; i<g.length; i++) {
        		if (g[i] == doingButton)
        			return i;
        	}
            return -1;
        }

        public Workflow(final TaskButton[] g) {
            this(g, MmsIcons.getYesIcon());
        }
        
        public Workflow(final TaskButton[] g, ActionListener e) {
            this(g, MmsIcons.getYesIcon());
            this.globalAction = e ;
        }

        public void setSpecialTaskButton(TaskButton specialTaskButton) {
        	this.specialTaskButton = specialTaskButton;
        }
        
        public void setSpecialTaskDoingState() {
        	if (specialTaskButton != null) {
        		setNotDoneState();        		
            	setDoingState(specialTaskButton, false);
        	}
        }
        
        public Workflow(
          final TaskButton[] g,
          final Icon icon) {
            this.icon = icon;
            this.g = g;
        }

        public void setNotDoneState() {
            for (int i = 0; i < g.length; i++) {
                g[i].setNotDoneState();
            }
            doingButton = null;
        }

        final private Icon icon;

        public void toggleOffIfCurrent(final TaskButton ifThisButton,
                                       final boolean enable) {
            if (doingButton != null && doingButton.equals(ifThisButton)) {
                doingButton.setDoneState(enable, icon);
            }
        }

        /**
         *
         * @param nextOnButton WaterMarkButton
         * @param disable boolean
         * @return WaterMarkButton previously "on" button
         */
        public TaskButton setDoingState(
          final TaskButton doingButton,
          final boolean disable) {
        	if (globalAction != null) {
        		globalAction.actionPerformed(new ActionEvent(this,0,"Click"));
        	}        		
            final TaskButton prev = this.doingButton;
            if (doingButton != null) {
                doingButton.setDoingState(disable);
                if (this.doingButton != null &&
                    !this.doingButton.equals(doingButton)) {
                    this.doingButton.setDoneState(
                      disable ? true /* re-enable prev*/ : false,
                      icon);
                }
                if (specialTaskButton != null && !doingButton.equals(specialTaskButton)) {
                	specialTaskButton.setNotDoneState();
                }
                this.doingButton = doingButton;
            }
            return prev;	
        }

        public boolean isOn(final TaskButton b) {
            return b.equals(doingButton);
        }
    }


    protected boolean isShowingWaterMarks() {
        return SwingBasics.taskButtonWaterMarks;
    }

    static TaskButton.Workflow group;

    public static void main(final String[] args) {
        class Button extends TaskButton implements ActionListener {
            Button(final String txt, final Image doingImage, final Image doneImage) {
                super(txt, doingImage, doneImage);
                addActionListener(this);
            }

            Button(final String txt, final Icon icon, final Image doingImage, final Image doneImage) {
                super(txt, doingImage, doneImage, icon);
                addActionListener(this);
            }

            public void actionPerformed(final ActionEvent e) {
                group.setDoingState(this, false);
            }

        };
        final Toolkit kit = Toolkit.getDefaultToolkit();

        Image doneImage=kit.getImage("C:\\FacsXpert_beta_4_19\\src\\edu\\stanford\\herzenberg\\notebook\\images\\finishedButtonArrow.gif"),
        doingImage=kit.getImage("C:\\FacsXpert_beta_4_19\\src\\edu\\stanford\\herzenberg\\notebook\\images\\downArrow.gif");
        ImageIcon icon = new ImageIcon(TaskButton.class.getClassLoader().
                                       getResource(
                                         "com/MeehanMetaSpace/swing/images/plus.gif"));
        Button bugWhereFirstButtonInstantiatedDisappears =
          new Button("wierd bug", doingImage, doneImage);

        final Button niko = new Button(
          "Niko", icon, doingImage, doneImage);
        final Button rooFish = new Button(
          "Roo-fish", doingImage, doneImage);

        final TaskButton reset = new TaskButton(
          "<HTML><BODY>Reset</BODY></HTML>", doingImage, doneImage);
        final TaskButton pepper = new Button(
          "<HTML><BODY>Jackpot<BR>Pepper</BODY></HTML>", doingImage, doneImage);

        reset.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (PopupBasics.ask("Reset?")) {
                    group.setNotDoneState();
                }
            }
        });

        group = new Workflow(new TaskButton[] {
                          niko,
                          pepper,
                          rooFish,
                          reset

        });

        // Create a frame in which to show the button.
        JFrame frame = new JFrame();
        SwingBasics.buttonEqualSizes=PopupBasics.ask("Equal size?");
        JPanel jp = SwingBasics.getButtonPanel(4);
        jp.add(pepper);
        jp.add(niko);
        jp.add(rooFish);
        jp.add(reset);
        group.setNotDoneState();
        frame.getContentPane().add(jp);
        frame.pack();
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    private WoodsideMenu.Style  style=WoodsideMenu.Style.NONE;
    
    public void setState(State state){
    	this.state=state;
    }
    
    public WoodsideMenu.Style getStyle(){
    	return style;
    }
    public void setWoodsideMenu(final WoodsideMenu.Style woodsideStyle){
    	style=woodsideStyle;
    	WoodsideMenu.set(this, style); 
    }
    
    public void setWoodsideMenuForFinishButton(final WoodsideMenu.Style woodsideStyle){
    	style=woodsideStyle;
    	WoodsideMenu.setFinish(this, style); 
    }
    
    public Color getForeground(){
    	if(state==State.DOING){
    		return Color.blue;
    	}else{
    		if(style==Style.TASK && !isEnabled()){
    		 return UIManager.getColor(WoodsideMenu.TASK_DISABLED_FOREGROUND);
    		}else if(style==Style.CONTEXT && !isEnabled()){
    			return UIManager.getColor(WoodsideMenu.CONTEXT_DISABLED_FOREGROUND);
    		}
    	}
    	return super.getForeground();
    }
    
    public void setForeground(final Color color){
    	
    	if (style != WoodsideMenu.Style.NONE && state != State.DOING){
    		if (isEnabled()){
    			super.setForeground(WoodsideMenu.getForeground(style));
    		} else {
    			super.setForeground(WoodsideMenu.getDisabledForeground(style));
    		}
    	} else {
    		super.setForeground(color);
    	}
    }
    
    public void setEnabled(final boolean b){
    	super.setEnabled(b);
    	if (style!= WoodsideMenu.Style.NONE ){
    		WoodsideMenu.setColors(this, style);
    		if (!b){
    			WoodsideMenu.setDisabledForeground(this, style);
    		} 
    	}
    }
    
    
   static class TaskButtonUI extends BasicButtonUI {
		protected void installDefaults(AbstractButton b) {
			super.installDefaults(b);
			LookAndFeel.uninstallBorder(b);
		}
		
	    protected void paintText(final Graphics g, final AbstractButton c, final Rectangle textRect, final String text) {
	    	AbstractButton b = (AbstractButton) c;
	    	FontMetrics fm = g.getFontMetrics();
			int mnemonicIndex = b.getDisplayedMnemonicIndex();
			g.setColor(b.getForeground());			
			BasicGraphicsUtils.drawStringUnderlineCharAt (g, text,mnemonicIndex,textRect.x + getTextShiftOffset(),
					textRect.y + fm.getAscent() + getTextShiftOffset());
		}
	    
	    

	}
    public boolean startedByMouse=false;
}


