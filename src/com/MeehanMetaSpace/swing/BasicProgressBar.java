package com.MeehanMetaSpace.swing;

import com.MeehanMetaSpace.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public final class BasicProgressBar {
  public static class Updater
      implements ProgressUpdater {
    private final JProgressBar bar;
    public boolean isCancelled() {
    	return false;
    }
    public Updater(final JProgressBar bar, final int chunk) {
      this.bar = bar;
      bar.setMinimum(0);
      bar.setStringPainted(true);
      this.chunk=chunk >=1 ? chunk : IoBasics.CHUNK*4;
    }

    public void report(final Condition.Annotated a) {
      if (a.condition == Condition.STARTED){
        if (!SwingUtilities.isEventDispatchThread()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              bar.setString(a.toString());
            }
          });
        } else {
          bar.setString(a.toString());
        }
      }
    }

    private final int chunk;
    public int getThresholdSize(){
      return chunk;
    }
    boolean maxSet = false;
    private void report(final String description,
                        final int tallySoFar, final int finalAmount) {
      final StringBuilder s = new StringBuilder(Basics.encode(tallySoFar));
      if (finalAmount >= 0) {
        s.append(" of ");
        s.append(Basics.encode(finalAmount));
        s.append(" copied ");
      }
      bar.setString(s.toString());
      if (!maxSet) {
        maxSet = true;
        bar.setMaximum(finalAmount);
      }
      bar.setValue(tallySoFar);
    }

    public void report(final String description, final int currentAmount,
                       final int tallySoFar, final int finalAmount) {

      if (!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            report(description, tallySoFar, finalAmount);
          }
        });
      } else {
        report(description, tallySoFar, finalAmount);
      }
    }
  }


  private final JProgressBar progressBar;
 private JProgressBar subProgressBar;
  public JProgressBar getSubProgressBar(){
    return subProgressBar;
  }

  private boolean hideConditionTableIfNoErrors;
  private final ConditionMetaRow conditionMetaRow;
  public final JButton closeButton;
  private final JDialog dialog;

  boolean cancelledByUser = false;
  private int runningActions = 0;

  public synchronized boolean wasCancelledByUser() {
    return cancelledByUser;
  }

  public boolean allowUserToCancelWhileRunning = false;
  private boolean disableCloseAction = false;

  public interface Copier {
    void go(ProgressUpdater pu, Map<String,File>files);
  }


  public static void copy(
      final Copier copier,
      final Map<String,File>files,
      final String dstDsc,
      final String heading,
      final Window wnd,
      final boolean pauseWhenStarting,
      final boolean pauseWhenEnding,
      final boolean allowCancel
      ) {
    long len = 0;
    for (final File file:files.values()) {
      len += file.length();
    }
    final BasicProgressBar bpb = new BasicProgressBar(
        (int) len,
        "Copy " + (files.size() + 1) + " file(s) to " + dstDsc, heading,
        wnd, null); //size + 1 for the meta file export.xml

    bpb.setRunButton("Start", new ActionListener() {
      public void actionPerformed(final ActionEvent ae) {
        copier.go(bpb.getProgressUpdater(), files);
        if (!pauseWhenEnding) {
          bpb.close();
        }
      }
    });
    bpb.closeButton.setText("Cancel");	
    if (allowCancel) {
    	bpb.allowUserToCancelWhileRunning=true;
    }
    if (!pauseWhenStarting) {
      bpb.runButton.doClick();
    }
    bpb.run();
  }

  public ProgressUpdater getProgressUpdater() {

    return new ProgressUpdater() {
    	public boolean isCancelled() {
    		return cancelledByUser;
    	}
public int getThresholdSize(){
  return IoBasics.CHUNK*64;
}
      public void report(final Condition.Annotated a) {
        BasicProgressBar.this.report(a);
      }

      public void report(final String description, final int currentAmount,
                         final int tallySoFar, final int finalAmount) {
        BasicProgressBar.this.addValue(currentAmount);
      }
    };
  }
  public BasicProgressBar(
	      final int max,
	      final String title,
	      final String heading,
	      final Window containingWnd,
	      final ConditionMetaRow conditionMetaRow) {
	    this(max, title, heading, containingWnd, 5, 50, conditionMetaRow, false, true, false);
   }
  public BasicProgressBar(
      final int max,
      final String title,
      final Window containingWnd,
      final ConditionMetaRow conditionMetaRow) {
    this(max, title, null, containingWnd, 5, 50, conditionMetaRow, false, true, false);
  }
  public BasicProgressBar(
        final int max,
        final String title,
        final Window containingWnd,
        final ConditionMetaRow conditionMetaRow,
        final boolean showSubProgressBar,
        final boolean disableCloseAction) {
      this(max, title, null, containingWnd, 5, 50, conditionMetaRow, showSubProgressBar, false, disableCloseAction);
    }

  public JDialog getDialog() {
    return dialog;
  }

  private String heading;
  private BasicProgressBar(
      final int max,
      final String title,
      final String heading,
      final Window containingWnd,
      final int msgHeight,
      final int msgWidth,
      final ConditionMetaRow conditionMetaRow,
      final boolean showSubProgressBar,
      final boolean hideConditionTableIfNoErrors,
      final boolean disableCloseAction) {
	  this.disableCloseAction = disableCloseAction;
	  this.hideConditionTableIfNoErrors=hideConditionTableIfNoErrors;
    this.conditionMetaRow = conditionMetaRow == null ? new ConditionMetaRow() :
                            conditionMetaRow;
    if (containingWnd instanceof Frame) {
      dialog = new JDialog((Frame) containingWnd, true);
    } else if (containingWnd instanceof Dialog) {
      dialog = new JDialog((Dialog) containingWnd, true);
    } else {
      dialog = new JDialog(SwingBasics.mainFrame);
      dialog.setModal(true);
    }
    runButton = SwingBasics.getButton("Run", MmsIcons.getRedoIcon(),
                                      'r', null, null);
    dialog.getRootPane().setDefaultButton(runButton);

    closeButton = SwingBasics.getButton(
        "Done",
        MmsIcons.getCancelIcon(),
        'c', new ActionListener() {
      public void actionPerformed(final ActionEvent ae) {
        synchronized (BasicProgressBar.this) {
          cancelledByUser = true;
          if (runningActions <= 0) {
        	  close();
          }
        }
      }
    }

    , null);
    if(!disableCloseAction)
    	SwingBasics.registerEscape(dialog, closeButton);

    dialog.setTitle(title);
    progressBar = new JProgressBar(0, max);
    if (showSubProgressBar){
      subProgressBar = new JProgressBar();
    }
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    if(disableCloseAction){
    	tableModel = this.conditionMetaRow.getTableModel((Window)null);
    } else{
    	tableModel = this.conditionMetaRow.getTableModel(dialog);
    }
    tableModel.unsort();
    if (tableModel.getTable() != null) {
    	tableModel.getTable().makeHorizontalScrollPane();    	
    }
    else {
    	new PersonalizableTable(tableModel).makeHorizontalScrollPane();	
    }    
    if(disableCloseAction){
    	dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }else{
    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dialog.dispose();
      }
    });
    }

    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    dialog.setContentPane(contentPane);
    if (heading != null) {
    	this.heading = heading;    	
    }
    if (subProgressBar == null){
      contentPane.add(progressBar, BorderLayout.SOUTH);
    } else {
      final JPanel jp=new JPanel(new GridLayout(2,1));
jp.add(progressBar);
jp.add(subProgressBar);
contentPane.add(jp, BorderLayout.SOUTH);

    }

    if (!hideConditionTableIfNoErrors){
    contentPane.add(tableModel.getContainer(""), BorderLayout.CENTER);
    }
    if (conditionMetaRow == null) {
      tableModel.setDefaultColumnWidth(this.conditionMetaRow.
                                 getConditionDataColumnIndex(),
                                 80);
      tableModel.setDefaultColumnWidth(this.conditionMetaRow.
                                 getMessageDataColumnIndex(),
                                 400);      
    }
  }

  private final PersonalizableTableModel tableModel;
  public PersonalizableTableModel getTableModel() {
    return tableModel;
  }

  public void showAsHtml() {
    tableModel.showAsHtml();
  }

  private JPanel contentPane;

  public final JButton runButton;

  public void setRunButton(final String text,
                           final ActionListener backgroundAction) {
    runButton.setText(text);
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent ae) {
        runButton.setEnabled(false);
        synchronized (BasicProgressBar.this) {
          if (cancelledByUser) {
            return;
          }
          runningActions++;
          closeButton.setText(SwingBasics.TEXT_CANCEL);
        }
        if (!allowUserToCancelWhileRunning) {
          closeButton.setEnabled(false);
        } else {
          closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
              synchronized (BasicProgressBar.this) {
                report(Condition.FATAL.annotate("Cancelled by user"));
                cancelledByUser = true;
                
              }
            }
          });
        }
        final Thread t = new Thread() {
          public void run() {
            backgroundAction.actionPerformed(ae);
            synchronized (BasicProgressBar.this) {
              runningActions--;
            }
            if (dialog.isVisible()) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  closeButton.setText("Done");
                  closeButton.setEnabled(true);
                  if (hideConditionTableIfNoErrors){
                	  closeButton.doClick();
                  } else {
                	  closeButton.requestFocus();
                  }
                  dialog.getRootPane().setDefaultButton(closeButton);
                  tableModel.refreshShowingTable(true);
                }
              });
            }

          }
        };
        t.start();
      }
    });
  }

  public void setRunButtonAndExecute(final String text,
		  final ActionListener backgroundAction) {
	  System.out.println("set run button 1");
	  runButton.setText(text);
	  runButton.addActionListener(new ActionListener() {
		  public void actionPerformed(final ActionEvent ae) {
			  runButton.setEnabled(false);
			  synchronized (BasicProgressBar.this) {
				  if (cancelledByUser) {
					  return;
				  }
				  runningActions++;
				  closeButton.setText(SwingBasics.TEXT_CANCEL);
			  }
			  if (!allowUserToCancelWhileRunning) {
				  closeButton.setEnabled(false);
			  } else {
				  closeButton.addActionListener(new ActionListener() {
					  public void actionPerformed(final ActionEvent ae) {
						  synchronized (BasicProgressBar.this) {
							  report(Condition.FATAL.annotate("Cancelled by user"));
							  cancelledByUser = true;

						  }
					  }
				  });
			  }
			  backgroundAction.actionPerformed(ae);
			  synchronized (BasicProgressBar.this) {
				  runningActions--;
			  }
			  if (dialog.isVisible()) {
				  SwingUtilities.invokeLater(new Runnable() {
					  public void run() {
						  System.out.println("close button...");
						  closeButton.setText("Done");
						  closeButton.setEnabled(true);
						  if (hideConditionTableIfNoErrors){
							  closeButton.doClick();
						  } else {
							  closeButton.requestFocus();
						  }
						  dialog.getRootPane().setDefaultButton(closeButton);
						  tableModel.refreshShowingTable(true);
					  }
				  });
			  }
		  }
	  });
  }
  
  private JButton[] btns;

  public void addButtons(JButton[] btns) {
    this.btns = btns;
  }

  public void run() {
    run(false);
  }

  private boolean showCloseButton = true;
  public void setShowCloseButton(final boolean ok) {
    showCloseButton = ok;
  }

  public void run(final boolean runRightAway) {
    final JPanel buttons = SwingBasics.getButtonPanel((showCloseButton ? 2 : 1) +
        (btns == null ? 0 : btns.length));
    buttons.add(runButton);
    if (showCloseButton) {
      buttons.add(closeButton);
    }
    if (!Basics.isEmpty(btns)) {
      for (int i = 0; i < btns.length; i++) {
        buttons.add(btns[i]);
      }
    }
    final JPanel jp = new JPanel();
    jp.add(buttons);
    if (heading != null) {
    	contentPane.add(new JLabel(heading), BorderLayout.NORTH);
    	contentPane.add(jp, BorderLayout.CENTER);
    }
    else {
    	contentPane.add(jp, BorderLayout.NORTH);
    }    
    contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    dialog.setContentPane(contentPane);

    // tableModel properties can only reshape containing window after pack()
    tableModel.setPersonalizableWindowOwner(dialog);
    if (PopupBasics.forceBottomRight){
    	dialog.setPreferredSize(new Dimension(375,450));
    	dialog.pack();
    	SwingBasics.bottomRight(dialog);
    }else if(runRightAway){
    	dialog.pack();
    	SwingBasics.center(dialog);
    	SwingBasics.adjustToAvailableScreens(dialog,false);
    }else{
    	SwingBasics.packAndPersonalize(dialog, "basicProgressBar", !hideConditionTableIfNoErrors);
    }
    if (runRightAway) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          runButton.doClick();
        }
      });
    }
    SwingBasics.showUpFront(dialog, runButton);
  }

  public void report(Exception e) {
    Pel.log.warn(e);
    report(Condition.ERROR.annotate(e.getClass().getName() + ": " +
                                    e.getMessage()));
  }

  public void note(final String msg) {
    report(Condition.NORMAL.annotate(msg));
  }

  public void brag(final String msg) {
    report(Condition.OPTIMAL.annotate(msg));
  }

  public void conclude(final String msg) {
    report(Condition.FINISHED.annotate(msg));
  }

  public void complain(final String msg) {
    report(Condition.ERROR.annotate(msg));
  }

  public void warn(final String msg) {
    report(Condition.WARNING.annotate(msg));
  }

  public void report(final RuntimeException e) {
    Pel.log.print(e);
    report(Condition.FATAL.annotate(e.getMessage()));
  }

  public void report(final Condition.Annotated annotatedCondition) {
		if (!cancelledByUser && annotatedCondition.annotation != null 
				&& !annotatedCondition.annotation.equals("")) {
			if (SwingUtilities.isEventDispatchThread()) {
				final ConditionMetaRow.DataSource ds = (ConditionMetaRow.DataSource) conditionMetaRow.getDataSource();
				ds.create(annotatedCondition);
				if (annotatedCondition.condition.equals(Condition.ERROR) && hideConditionTableIfNoErrors){
					hideConditionTableIfNoErrors=false;
				    contentPane.add(tableModel.getContainer(""), BorderLayout.CENTER);
				    SwingUtilities.getWindowAncestor(contentPane).pack();
				}
				if (!hideConditionTableIfNoErrors){
					progressBar.setString(annotatedCondition.annotation);
				}
				tableModel.refreshShowingTable(false);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						report(annotatedCondition);
					}
				});
			}
		}

	}

  private int accumulated = 0;

  public void addValue(final int v) {
    if (SwingUtilities.isEventDispatchThread()) {
      accumulated += v;
      progressBar.setValue(accumulated);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          addValue(v);
        }
      });
    }
  }

  public void setValue(final int v) {
    if (SwingUtilities.isEventDispatchThread()) {
      progressBar.setValue(v);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          setValue(v);
        }
      });
    }
  }

  public void resetMax(
      final int max,
      final String why) {
    if (SwingUtilities.isEventDispatchThread()) {
      progressBar.setMaximum(max);
      progressBar.setValue(0);
      progressBar.setString(why);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          resetMax(max, why);
        }
      });
    }

  }

  public void close() {
	  if(disableCloseAction){
		  dialog.dispose();
	  }else{
		  SwingBasics.closeWindow(dialog);
	  }
  }
}
