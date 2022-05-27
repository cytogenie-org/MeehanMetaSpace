package com.MeehanMetaSpace.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.WordUtils;

import com.MeehanMetaSpace.ArrayBasics;
import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.Condition;
import com.MeehanMetaSpace.Credentials;
import com.MeehanMetaSpace.DefaultStringConverters;
import com.MeehanMetaSpace.GenericListModel;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.Pel;
import com.MeehanMetaSpace.ProgressUpdater;
import com.MeehanMetaSpace.PropertiesBasics;
import com.MeehanMetaSpace.StringConverter;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class PopupBasics {
	
	public static boolean capitalizeOk;

  public static Basics.Gui gui = new Basics.Gui() {
    public boolean download(
        final String localFileName,
        final String localFolder,
        final String sourceUrlName,
        final boolean unzipping,
        final boolean junkZipFolder,
        final boolean overWrite) {
      return PopupBasics.download(
          localFileName,
          localFolder,
          sourceUrlName,
          unzipping, junkZipFolder,
                                  overWrite);
    }

    public boolean ask(final String title, final String question,
                       final boolean error) {
      return PopupBasics.ask(title, question, error);
    }

    public String getStringFromUser(
        final Component component,
        final String message,
        final String title,
        final String suggestedValue
        ) {
      return getStringFromUser(component, message, title,
                                           suggestedValue);
    }

    public int choose(
        final Component component,
        final String msg,
        final String title,
        final Object[] choices,
        final int defaultChoice,
        final boolean allowCancel,
        final boolean vertical) {
      return getChosenIndex(component, msg, title, choices, defaultChoice, allowCancel,
                            vertical);
    }

    public void alertAsync(final String msg, final boolean beep) {
      PopupBasics.alertAsync(msg, beep);
    }

    public String getFileName(
        final String regexMatch,
        final String regexAvoid,
        final String extension,
        final String typeDescription,
        final String approveButtonText,
        final String approveToolTip,
        final boolean readOnly,
        final String currentDirectory,
        final String selectedFile,
        final Window containingWnd) {
      return PopupBasics.getFileName(
          regexMatch,
          regexAvoid,
          extension,
          typeDescription,
          approveButtonText,
          approveToolTip,
          readOnly,
          currentDirectory,
          selectedFile,
          containingWnd
          );
    }

    public void alert(final String msg, final boolean beep) {
      PopupBasics.alert(msg, beep);
    }

    public void alert(final String msg) {
      alert(msg, false);
    }

    public String getDirName(
        final String dialogTitle,
        final String dir,
        final String approveButtonText,
        final String approveToolTip,
        final Window containingWnd) {
      return PopupBasics.getDirName(
          dialogTitle,
          dir,
          approveButtonText,
          approveToolTip,
          containingWnd);
    }

	public String showHtml(String temporaryFilePrefix,
			String htmlEncodedContent, boolean useInternalBrowser) {
		return SwingBasics.showHtml(temporaryFilePrefix, htmlEncodedContent, useInternalBrowser);
	}
  };

  public static void assertPopupBasicsGui() {
    if (Basics.gui != PopupBasics.gui) {
      System.err.println("Basics.gui is not set to PopupBasics.gui.");
      System.exit(1);
    }
  }

  public static void alert(final String msg) {
    alert(msg, false);
  }

  public static void alert(final Component cmp, final String msg) {
	    alert(cmp, msg, "Alert", false);
	  }

  public static String applyFormatting(String msg) {
	  if(msg != null && msg.length() > 75 && !msg.contains("<html>")) {
	    	msg = wrap(msg,75,"<br>",true);    
		    msg = "<html><b><i><font face=" + SwingBasics.getTitleFont().getFontName() + 
		    	" size=4>"+ msg +"</font></i></b></html>";
	    }
	  return msg;
  }
  
  public static void alert(String msg, final boolean isError) {

	    assertPopupBasicsGui();
	    	    
	    alert(applyFormatting(msg),"Alert",isError);
	  }
	  
	  public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLastLine) {
		    
		  StringBuffer sb = new StringBuffer();
		  int msgLength = str.length();
		  int offset = 0;
		  int lastBrokenAt = 0;
		  int addWrapLength = wrapLength;
		  while (offset <= msgLength)
		  {
			  while (offset < wrapLength)
			  {
				  if (offset != msgLength)
				  offset++;
			  }		  		
			  if (offset >= wrapLength && offset < msgLength)
			  {
				  while (str.charAt(offset) == '\\' || str.charAt(offset) == ' ')
				  { 
					  if (offset != msgLength)
						  offset++;
					  sb.append(str.substring(lastBrokenAt, offset));
					  sb.append(newLineStr);
					  lastBrokenAt = offset;
					  if ((offset+addWrapLength) < msgLength)
						  wrapLength = offset + addWrapLength;
					  else
					  {
						  wrapLength=offset;
						  wrapLength = wrapLength + (msgLength - offset);
					  }
			  }
				  if (offset != msgLength)
					  offset++;
		      }
			  else {
			  offset = msgLength;
			  sb.append(str.substring(lastBrokenAt, msgLength));
			  if (wrapLastLine)
				  sb.append(newLineStr);
			  break;
			  }
		  }
			 
		  return sb.toString();
	}

  public static void alert(
      final String msg,
      final String title,
      final boolean isError) {
    alert(null, msg, title, isError);
  }

public static void alert2(
		  final String briefMsg,
	      final String detailedMsg,
	      final String title,
	      final boolean isError) {
	    alert2(null, briefMsg,detailedMsg, title, isError);
	  }

  public static void alert2(
	      final Component component,
	      final String briefMsg,
	      final String detailedMsg,
	      final String title,
	      final boolean isError) {

	    if (!Basics.isEmpty(briefMsg)) {

	      showMessageDialog2(component, briefMsg,detailedMsg, title,
	                        isError ? JOptionPane.ERROR_MESSAGE :
	                        JOptionPane.INFORMATION_MESSAGE);
	    }
	  }

  public static void alert(
      final Throwable throwable,
      final String windowTitle,
      final String h3Heading) {
    String msg = Basics.startHtmlErrorUncentered(h3Heading) +
                 "<b>Exception:</b>&nbsp;&nbsp;<code>" +
                 WordUtils.wrap(throwable.getMessage(), 40, "<br>", false) +
                 "</code>";
    final Throwable cause = throwable.getCause();
    if (cause != null) {
      msg += "<p><b>Cause:</b>&nbsp;&nbsp;<code>" +
          WordUtils.wrap(cause.getMessage(), 40, "<br>", false) + "</code>";
    }
    msg += Basics.endHtml();
    alert(msg, windowTitle, true);
  }

  public static void alert(
      final Component component,
      final String msg,
      final String title,
      final boolean isError) {

    if (!Basics.isEmpty(msg)) {

      showMessageDialog(component, msg, title,
                        isError ? JOptionPane.ERROR_MESSAGE :
                        JOptionPane.INFORMATION_MESSAGE);
    }
  }

 public static boolean ask2(final String title, final String txt1,final String txt2,
          final boolean error) {
	  return ask2(null, title, txt1,txt2, error, true);
  }
 public static boolean ask2(final Component component, final String title, String txt1,String txt2,
          final boolean error, final boolean yesIsDefault) {
	   return showMessageDialog2(
			  component,txt1,txt2,"Please confirm...",
			  JOptionPane.QUESTION_MESSAGE,yesIsDefault);
  }

  public static boolean ask(final String title, final String question,
          final boolean error) {
	  return ask(null, title, question, error, true);
  }
  public static boolean ask(final Component component, final String title, final String enquiry,
                            final boolean error, final boolean yesIsDefault) {
    final String txt;
    final String question;
    question=Basics.stripHtmlTagsOnly(enquiry);
    if (error) {
      beep();
      txt = Basics.toHtmlErrorUncentered(title, question);
    } else {
      txt = Basics.toHtmlUncentered(title, question);
    }
    return showMessageDialog(
	        component,
	        txt,
	        "Please confirm...",
	        JOptionPane.QUESTION_MESSAGE,yesIsDefault);
  }
  
  public static boolean ask(final Component component, final String title, final String question,
          final boolean error, final boolean yesIsDefault, final String yesButtonText, final char mnemonic) {
	  final String txt;
	  if (error) {
		  beep();
		  txt = Basics.toHtmlErrorUncentered(title, question);
	  } else {
		  txt = Basics.toHtmlUncentered(title, question);
	  }
	  return showMessageDialog(
			  component,
			  txt,
			  "Please confirm...",
			  JOptionPane.QUESTION_MESSAGE,yesIsDefault, yesButtonText, mnemonic);
  	}
  
  public static JDialog currentMessageDialog, currentOptionDialog;

  private static boolean showMessageDialog(final Component parent, final String msg,
		  final String title, final int optionPaneMessageType, final boolean yesIsDefault,final String yesButtonText,
		  final char mnemonic) {
	    yes = false;
	    final JDialog dlg = getDialog(parent);
	    currentMessageDialog = dlg;
	    final JPanel buttons = SwingBasics.getButtonPanel(2);
	    JButton focus = null;
	    switch (optionPaneMessageType) {
	    case JOptionPane.ERROR_MESSAGE:
	      Toolkit.getDefaultToolkit().beep();
	    default:
	      final JButton ok =
	          SwingBasics.getButton(
	              capitalizeOk?"OK":"Ok",
	              null,
	              'o',
	              new ActionListener() {
	        public void actionPerformed(final ActionEvent ae) {
	          SwingBasics.closeWindow(dlg);
	        }
	      }

	      , "Close window"
	          )
	          ;
	      buttons.add(ok);
	      dlg.getRootPane().setDefaultButton(ok);
	      focus = ok;
	      break;
	    case JOptionPane.QUESTION_MESSAGE:
	    	if(yesButtonText != null){
	    		focus = addYesNo(dlg, buttons, yesButtonText, mnemonic, "No", null, yesIsDefault);
	    	}else{
	    		focus = addYesNo(dlg, buttons, "Yes",'y', "No", null, yesIsDefault);
	    	}
	    }
	    if (showCancelButton) {
	    	JButton cancel = new JButton("Cancel");
	    	cancelClicked = false;
	    	cancel.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			cancelClicked = true;			
	    			SwingBasics.closeWindow(dlg);
	    		}
	    	});
	    	buttons.add(cancel);
	    }
	    show(dlg, "dlg", msg, title, optionPaneMessageType, buttons, focus, false);
	    return yes;
}
public static boolean showCancelButton  = false;
public static boolean cancelClicked = false;
private static boolean showMessageDialog2(Component parent, String briefMsg,String detailedMsg,
			String title, int optionPaneMessageType, boolean yesIsDefault,String yesButtonText,
			char mnemonic) {
		    yes = false;
		    final JDialog dlg = getDialog(parent);
		    final JPanel buttons = SwingBasics.getButtonPanel(2);
		    JButton focus = null;
		    switch (optionPaneMessageType) {
		    case JOptionPane.ERROR_MESSAGE:
		      Toolkit.getDefaultToolkit().beep();
		    default:
		      final JButton ok =
		          SwingBasics.getButton(
		              capitalizeOk?"OK":"Ok",
		              null,
		              'o',
		              new ActionListener() {
		        public void actionPerformed(final ActionEvent ae) {
		          SwingBasics.closeWindow(dlg);
		        }
		      }

		      , "Close window"
		          )
		          ;
		      buttons.add(ok);
		      dlg.getRootPane().setDefaultButton(ok);
		      focus = ok;
		      break;
		    case JOptionPane.QUESTION_MESSAGE:
		    	if(yesButtonText != null){
		    		focus = addYesNo(dlg, buttons, yesButtonText, mnemonic, "No", null, yesIsDefault);
		    	}else{
		    		focus = addYesNo(dlg, buttons, "Yes",'y', "No", null, yesIsDefault);
		    	}
		    }
		    show2(dlg, "dlg", briefMsg,detailedMsg, title, optionPaneMessageType, buttons, focus, false);
		    return yes;
	}

public static boolean ask(final String question) {
    return ask(null, question);
  }

  public static boolean ask(final Component component, final String question) {
	    return showMessageDialog(
	        component,
	        question,
	        "Please confirm...",
	        JOptionPane.QUESTION_MESSAGE);
	  }
// ok to have the variables static because modal dlg uses them
  static boolean yes = true;
  static int idx = -1,idx2 = -1;

  public interface Input {
    JComponent getVisualComponent();

    JComponent getEditorComponent();

    boolean requiresActionEchoing();

    void setInputValue(Object value);

    Object getCellEditorValue();
  }


  private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static DateFormat format = new SimpleDateFormat(
      DATETIME_FORMAT_PATTERN);

  private static final class DateEditor implements Input {
    private final JSpinner field;
    private final DateFormat dateFormat;
    public boolean requiresActionEchoing() {
      return true;
    }

    private DateEditor(final DateFormat df) {
      dateFormat = df == null ? format : df;
      field = Editor.DateSpinner.New(dateFormat);
    }

    public JComponent getVisualComponent() {
      return field;
    }

    public void setInputValue(final Object newValue) {
      if (newValue instanceof Date) {
        field.setValue((Date) newValue);
      } else {
        field.setValue(new Date());
      }
    }

    public JComponent getEditorComponent() {
      return field.getEditor();
    }

    public Object getCellEditorValue() {
      try {
        field.commitEdit();
        return field.getValue();
      } catch (final Exception e) {
        Pel.log.warn(e);
      }
      return null;
    }

  }

  private static final class RangeEditor implements Input {
	    private final JSpinner field;
	    public boolean requiresActionEchoing() {
	      return true;
	    }

	    private RangeEditor(final int initValue, final int step, final int min, final int max) {
	    	SpinnerModel model = new SpinnerNumberModel(initValue, min, max, step);
	        field = new JSpinner(model);
	    }

	    public JComponent getVisualComponent() {
	      return field;
	    }

	    public void setInputValue(final Object newValue) {
	        field.setValue(newValue);
	    }

	    public JComponent getEditorComponent() {
	      return field.getEditor();
	    }

	    public Object getCellEditorValue() {
	      try {
	        field.commitEdit();
	        return field.getValue();
	      } catch (final Exception e) {
	        Pel.log.warn(e);
	      }
	      return null;
	    }

	  }


  private static class StringEditor implements Input {
    private final int fixedColumns;
    final TextFieldWithHint field;
    public boolean requiresActionEchoing() {
      return false;
    }

    public JComponent getEditorComponent() {
      return field;
    }

    private StringEditor() {
      field = new TextFieldWithHint(15);
      fixedColumns = 0;
      handleKeys(field);
    }

    private StringEditor(final int columns) {
      field = new TextFieldWithHint(columns);
      fixedColumns = columns;
      handleKeys(field);
    }
    private StringEditor(final int columns, final String hint) {
        field = new TextFieldWithHint(columns);
        field.setHint(hint);
        fixedColumns = columns;
        handleKeys(field);
      }

    public JComponent getVisualComponent() {
      return field;
    }

    public void setInputValue(final Object newValue) {
      final String s = newValue == null ? "" : newValue.toString();
      if (fixedColumns == 0 || !Basics.isEmpty(s)) {
        final int columns = s.length() > 10 ? s.length() : 10;
        field.setColumns(columns);
      }
      field.setText(s);
      field.selectAll();
    }

    public Object getCellEditorValue() {
      return field.getText();
    }
  }


  private static final class EmailEditor extends StringEditor {
    private boolean ok = true;
    private EmailEditor(final int cols) {
      super(cols);
    }

    public Object getCellEditorValue() {
      final String s = field.getText();
      final int idx = s.indexOf("@");
      if (idx < 0) {
        alert("No @ found in email address !", true);
        ok = false;
      } else if (!Basics.appearsToBeADomainName(s)) {
        alert("Warning, " + s.substring(idx) +
              " does not appear to be a domain name..", true);
        ok = false;
      } else {
        ok = true;
      }
      return s;
    }
  }


  private static final class PasswordEditor implements Input {
    private final JPasswordField field;

    private PasswordEditor() {
      field = new JPasswordField(25);
    }

    public boolean requiresActionEchoing() {
      return false;
    }

    public JComponent getEditorComponent() {
      return field;
    }

    public JComponent getVisualComponent() {
      return field;
    }

    public void setInputValue(final Object newValue) {
      final String s = newValue == null ? "" : newValue.toString();
      final int columns = s.length() > 25 ? s.length() : 25;
      field.setColumns(columns);
      field.setText(s);
    }

    public Object getCellEditorValue() {
      return field.getText();
    }
  }


  private static class AutoCompleteEditor implements Input {
    private final int fixedColumns;
    public boolean requiresActionEchoing() {
      return true;
    }

    private final AutoComplete autoComplete;
    private AutoCompleteEditor(final Object[] values, final StringConverter sc, final int fixedColumns, final boolean readOnly) {
      autoComplete = new AutoComplete(values, sc, !readOnly, true);
      this.fixedColumns = fixedColumns;
      if (readOnly){
    	  autoComplete.setWarnIfChangesCapitalLetters(false);    	  
      }
    }

    public JComponent getEditorComponent() {
      final Object o = autoComplete.getEditor().getEditorComponent();
      if (o instanceof JComponent) {
        return (JComponent) o;
      }
      return autoComplete;
    }

    public JComponent getVisualComponent() {
      return autoComplete;
    }

    public void setInputValue(final Object newValue) {
      final String s = newValue == null ? "" : newValue.toString();
      if (fixedColumns > 0) {
        autoComplete.getTextField().setColumns(fixedColumns);
      } else {
        final int columns = s.length() > 10 ? s.length() : 10;
        autoComplete.getTextField().setColumns(columns);
      }
      autoComplete.setSelectedItem(newValue);
      autoComplete.selectAll();
      
SwingUtilities.invokeLater(new Runnable(){

	public void run() {
		autoComplete.requestFocus();
		
	}
}
);

    }

    public Object getCellEditorValue() {
      return autoComplete.getFinalValue();
    }
  }


  public static String getStringFromUser(
      final Component component,
      final String message,
      final String suggestedValue,
      final int columns) {

    return (String) showInputDialog(
        "stringInput",
        null,
        message,
        "Input required...",
        new StringEditor(columns),
        suggestedValue);
  }

  public static String getStringFromUser(
	      final Component component,	      
	      final String message,
	      final String title,
	      final String suggestedValue,
	      final int columns,
	      final JPanel southCentralAddition,
	      final String hint) {

	    return (String) showInputDialog(
	        "stringInput",
	        component,
	        message,
	        null,
	        title,
	        new StringEditor(columns, hint),
	        suggestedValue,
	        true,
	        false,
	        southCentralAddition);
	  }

  public static Date getDateFromUser(
      final Component component,
      final String windowTitle,
      final String message,
      final Date suggestedValue,
      final DateFormat dateFormat) {
    return (Date) showInputDialog(
        "dateInput",
        null,
        message,
        windowTitle == null ? "Date required" : windowTitle,
        new DateEditor(dateFormat),
        suggestedValue);
  }

  public static String getStringFromUser(
      final Component component,
      final String message,
      final String title,
      final String suggestedValue) {

    assertPopupBasicsGui();

    return (String) showInputDialog(
        "stringInput",
        component,
        message,
        title,
        new StringEditor(),
        suggestedValue);

  }

  public static String getStringFromUser(
      final String message,
      final String suggestedValue) {
    return getStringFromUser(null, message, "Input required", suggestedValue);
  }

  public static String getNonEmptyStringFromUser(
      final String message) {
    return (String) showInputDialog(
        "stringInput",
        null,
        message,
        "Input required",
        new StringEditor(),
        null,
        false,
        false);

  }
  public static String getStringValueOrCancelOperation(
	      final String message,
	      final String suggestedValue,
	      final boolean showCancelButton) {
	  return getStringValueOrCancelOperation(null, message, suggestedValue, showCancelButton);
  }
  
  public static String getStringValueOrCancelOperation(
		  final Component component,
	      final String message,
	      final String suggestedValue,
	      final boolean showCancelButton) {
	    String value = (String) showInputDialog(
	        "stringInput",
	        component,
	        message,
	        "Input required",
	        new StringEditor(),
	        suggestedValue,
	        showCancelButton,
	        false);
	    
	    if (!yes) { //window is closed with x
	    	return null;
	    }
	    return value;

  }

  public static String getPasswordFromUser(
      final Component component,
      final String message,
      final String suggestedValue) {
    return getPasswordFromUser(
        component,
        message,
        "Password required",
        suggestedValue);
  }

  public static String getPasswordFromUser(
      final Component component,
      final String message,
      final String windowTitle,
      final String suggestedValue) {
    return (String) showInputDialog(
        "passwordInput",
        component,
        message,
        windowTitle,
        new PasswordEditor(),
        suggestedValue);
  }

  public static Object getValueFromUser(
      final String message,
      final Object suggestedValue,
      final Object[] suggestedValues
      ) {
    return getValueFromUser(
    		null, 
    		message, 
    		"Input required", 
    		suggestedValue, 
    		suggestedValues, 
    		DefaultStringConverters.get(), 
    		-1, 
    		false,
    		-1);
  }
  public static Object getValueFromUser(
		  final Component owner,
	      final String message,
	      final String title,
	      final Object suggestedValue,
	      final Object[] suggestedValues,
	      final StringConverter sc,
	      final int fixedColumns,
	      final boolean readOnly) {
	  return getValueFromUser(owner,message,title,suggestedValue, suggestedValues, sc,fixedColumns, readOnly, -1);
  }

  public static Object getValueFromUser(
	  final Component owner,
      final String message,
      final String title,
      final Object suggestedValue,
      final Object[] suggestedValues,
      final StringConverter sc,
      final int fixedColumns,
      final boolean readOnly,
      final int maximumLength) {
	  return getValueFromUser(owner, message, title, suggestedValue, suggestedValues, sc, fixedColumns, readOnly, maximumLength, null, null, null, false);
  }
  
  public static Object getValueFromUser(
		  final Component owner,
	      final String message,
	      final String title,
	      final Object suggestedValue,
	      final Object[] suggestedValues,
	      final StringConverter sc,
	      final int fixedColumns,
	      final boolean readOnly,
	      final int maximumLength,
	      final Collection forbidden,
	      final Collection unselected,
	      final JPanel southCentralAddition,
	      final boolean isCaseSensitive) {
	  final AutoCompleteEditor ace=new AutoCompleteEditor(suggestedValues, sc, fixedColumns, readOnly);
	  ace.autoComplete.setIsCaseSensitive(isCaseSensitive);
	  if (forbidden != null){
		  ace.autoComplete.setForbidden(forbidden);
	  }
	  if(unselected != null){
		  ace.autoComplete.setUnselectable(unselected);
	  }
	  if (maximumLength>0) {
		  setMaxLengthForComboBox(ace.getVisualComponent(), maximumLength);
	  }
	  ace.autoComplete.ignoreTheDefaultButton=true;
	  SwingUtilities.invokeLater(new Runnable() {
		
		@Override
		public void run() {
//			ace.autoComplete.isInvokingDefaultButton=false;
			
		}
	});
	  return showInputDialog(
        "autoComplete",
        owner,
        message,
        null,
        title,
        ace,
        suggestedValue, true, maximumLength>0, southCentralAddition);
  }

  public static Object getNewValueFromUser(
		  final Component owner,
	      final String message,
	      final String title,
	      final Object suggestedValue,
	      final Object[] suggestedValues,
	      final StringConverter sc,
	      final int fixedColumns,
	      final int maximumLength) {
	  AutoCompleteEditor ace=new AutoCompleteEditor(suggestedValues, sc, fixedColumns, false);
	  ace.autoComplete.setWriteOnly(true);
	  if (maximumLength>0) {
		  setMaxLengthForComboBox(ace.getVisualComponent(), maximumLength);
	  }

	    return showInputDialog(
	        "autoComplete",
	        owner,
	        message,
	        title,
	        ace,
	        suggestedValue, true, maximumLength>0);
	  }

  private static final class TextFieldLimiter extends PlainDocument
  {
    int maxChar = -1;
    public TextFieldLimiter(int len){maxChar = len;}
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
      if (str != null && maxChar > 0 && this.getLength() + str.length() > maxChar)
      {
        java.awt.Toolkit.getDefaultToolkit().beep();
        return;
      }
      super.insertString(offs, str, a);
    }
  }
  
  private static void setMaxLengthForComboBox(final JComponent jc, final int maximumLength) {
	  if (jc instanceof JComboBox) {
    	  final JTextField tf = (JTextField)(((JComboBox)jc).getEditor().getEditorComponent());
    	  tf.setDocument(new TextFieldLimiter(maximumLength));
      }
  }
  public static int getIntFromUser(final int suggestedValue) {
    final Integer integer = getIntegerFromUser("Enter an integer",
                                               new Integer(suggestedValue));
    return integer == null ? suggestedValue : integer.intValue();
  }

  public static Integer getIntegerFromUser(
      final String message,
      final Integer suggestedValue) {
    return getIntegerFromUser(null, message, "Input required", suggestedValue);
  }

  public static Integer getIntegerFromUser(
	      final Component component,
	      final String message,
	      final String title,
	      final Integer suggestedValue) {
	  return getIntegerFromUser(
			  component, message, title, suggestedValue, 
			  null, null, null);
  }

	public static Integer getIntegerFromUser(final Component component,
			final String message, final String title,
			final Integer suggestedValue, final String helpInformationForSouth,
			final Integer min, final Integer max) {
		final Input input;
/*		if (suggestedValue != null && min != null && max != null) {
			final RangeEditor r=new RangeEditor(suggestedValue, 1, min,max);
			input=r;
			
		} else {*/
			final DecimalCellEditor d = new DecimalCellEditor("###,###",
					Integer.class);
			handleKeys(d.field);
			input = d;
		//}
		boolean ok = false;
		Integer entry=null;
		while (!ok) {
			entry = (Integer) showInputDialog("integer", component,
					message,helpInformationForSouth, title, input, suggestedValue, true, false, null);
			if (entry != null) {
				if (min != null && entry < min && min <= max) {
					alert(component,
							"Your entry must be " + min + " or greater.",
							entry+" is too low", true);
				} else if (max != null && entry > max && max >= min) {
					alert(component,
							"Your entry must be " + max + " or less.",
							entry+" is too high", true);
				} else {
					ok = true;
				}
			} else {
				ok=true;
			}
		}
		return entry;
	}

  public static Float getFloatFromUser(final String message,
                                       final Float suggestedValue) {
    final DecimalCellEditor d = new DecimalCellEditor("###,###,###.####", Float.class);
    handleKeys(d.field);
    return (Float) showInputDialog(
        "float",
        null,
        message, "Input required", d, suggestedValue);
  }

  public static Object showInputDialog(
      final String inputType,
      final Component parent,
      final Object message,
      final String windowTitle,
      final Input input,
      final Object initialValue) {
      return showInputDialog(
      inputType,
      parent,
      message,
      windowTitle,
      input,
      initialValue,
      true,
      false);
  }
  private static Object showInputDialog(
	      final String inputType,
	      final Component parent,
	      final Object message,
	      final String windowTitle,
	      final Input input,
	      final Object initialValue,
	    final boolean showCancelButton,
	    boolean isMaxLengthSet) {
	  return showInputDialog(inputType, parent, message, null, windowTitle, input, initialValue, showCancelButton, isMaxLengthSet, null);
  }

  private static Object showInputDialog(
      final String inputType,
      final Component parent,
      final Object message,
      final String helpInformationForSouth,
      final String windowTitle,
      final Input input,
      final Object initialValue,
    final boolean showCancelButton,
    boolean isMaxLengthSet,
    final JPanel southCentralAddition) {
    yes = false;
    final JDialog dlg=getDialog(parent);
    JPanel buttons = SwingBasics.getButtonPanel(1);
    final String msg=message==null?null:message.toString();
    final boolean haveMessage=msg!=null;
    boolean needScroll=!haveMessage?false:needScrollbar(msg);
    JPanel jp = new JPanel();
    if (isMaxLengthSet) {
    	JPanel tempPanel = new JPanel();
    	tempPanel.setLayout(new GridLayout(2, 2, 5, 5));
    	if (haveMessage){
    		tempPanel.add(new JLabel(msg));
    	}
    	tempPanel.add(initInput(input, initialValue));
    	jp = SwingBasics.newPanel((Icon)null, tempPanel);
    }
    else {
    	if (helpInformationForSouth!=null) {
    		needScroll=false;
    		jp.setLayout(new BorderLayout());
    		final JScrollPane jsp=new JScrollPane(new JLabel(helpInformationForSouth));
			jp.add(jsp, BorderLayout.CENTER);
			final JPanel jp2=new JPanel(new BorderLayout());
			final JPanel jp3=new JPanel();
			if (haveMessage){
				jp3.add(new JLabel(msg));
			}
			jp3.add(initInput(input, initialValue));
			jp2.add(jp3, BorderLayout.WEST);
			jp.add(jp2, BorderLayout.NORTH);
    		
    	} else {
    		if (haveMessage){
				jp.add(new JLabel(msg));
    		}
				jp.add(initInput(input, initialValue));
    	}
    }
        
    addYesNo(dlg, buttons, capitalizeOk?"OK":"Ok", showCancelButton?"Cancel":null, input);
    if (southCentralAddition != null) {
    	final JPanel temp=new JPanel(new BorderLayout());
    	temp.add(southCentralAddition, BorderLayout.SOUTH);
    	temp.add(jp,BorderLayout.CENTER);
    	jp=temp;
    }
    jp.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
    show(needScroll,dlg, inputType, jp, windowTitle, JOptionPane.INFORMATION_MESSAGE,
         buttons, input.getEditorComponent(), false);
    return yes ? input.getCellEditorValue() : null;
  }

  private static JComponent initInput(
      final Input input,
      final Object initialValue) {
    final JComponent jc = input.getVisualComponent();
    input.setInputValue(initialValue);
    if (jc instanceof JFormattedTextField) {
      final JFormattedTextField text = (JFormattedTextField) jc;            
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {

          final EventQueue evtq = Toolkit.getDefaultToolkit().
                                  getSystemEventQueue();
          final Date date = new Date();
        
          /*
           * Post 'End' key event 
           */
          evtq.postEvent(new KeyEvent(text, KeyEvent.KEY_PRESSED, date.getTime(),
                                      0, KeyEvent.VK_END,
                                      KeyEvent.CHAR_UNDEFINED));

          /*
           * Post 'Home' key event with 'Shift' key pressed
           */
          evtq.postEvent(new KeyEvent(text, KeyEvent.KEY_PRESSED, date.getTime(),
        		  						KeyEvent.SHIFT_DOWN_MASK,
                                      KeyEvent.VK_HOME, KeyEvent.CHAR_UNDEFINED));

        
        }
      });
    }
    jc.requestFocus();
    return jc;

  }

  private static JPanel initInput(
      final Input input,
      final String label,
      final String initialValue) {
    final JPanel jp = new JPanel(new BorderLayout(5, 5));
    jp.add(new JLabel(label), BorderLayout.CENTER);
    jp.add(initInput(input, initialValue), BorderLayout.EAST);
    return jp;

  }

  public static Credentials getCredentials(
      final String windowTitle,
      final String msg,
      final String loginLabelText,
      final String passwordLabelText,
      final String loginInitialValue,
      final String passwordInitialValue,
      final boolean treatLoginAsEmail,
      final String notifyUpdatesLabel,
      final Boolean notifyUpdates) {
    yes = false;
    final JDialog dlg = getDialog(null);
    final JPanel buttons = SwingBasics.getButtonPanel(1);
    final JPanel jp = new JPanel(new BorderLayout()),
                      jp2 = new JPanel(new GridLayout(3, 1, 2, 1));
    jp.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 22));
    jp.add(new JLabel(msg), BorderLayout.NORTH);
    jp.add(jp2, BorderLayout.CENTER);
    Input emailInput = treatLoginAsEmail ? new EmailEditor(35) :
                       new StringEditor(35);
    jp2.add(
        initInput(emailInput, loginLabelText, loginInitialValue));
    Input passwordInput = new PasswordEditor();
    jp2.add(
        initInput(passwordInput, passwordLabelText, passwordInitialValue));

    final boolean _notifyUpdates = notifyUpdates == null ?
                                   false :
                                   notifyUpdates.booleanValue();
    final JCheckBox nu;
    if (notifyUpdatesLabel != null) {
      nu = new JCheckBox(notifyUpdatesLabel,
                         _notifyUpdates);
      SwingBasics.addMouseOver(nu);
      nu.setSelected(_notifyUpdates);
      jp2.add(nu);
    } else {
      nu = null;
    }

    addYesNo(dlg, buttons, capitalizeOk?"OK":"Ok", "Cancel", emailInput);
    show(needScrollbar(msg),dlg, "credentialsInput", jp, windowTitle,
         JOptionPane.INFORMATION_MESSAGE, buttons, null, false);
    final Credentials retVal;
    if (yes) {
      final String login = (String) emailInput.getCellEditorValue();
      if (treatLoginAsEmail && !((EmailEditor) emailInput).ok) {
        retVal = null;
      } else {
        retVal = new Credentials(login,
                                 (String) passwordInput.getCellEditorValue(),
                                 nu == null ? null :
                                 Boolean.valueOf(nu.isSelected()));
      }
    } else {
      retVal = null;
    }
    return retVal;
  }

  public static int showRadioButtonOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final Object initialValue,
	      final boolean addCancelButtonAtBottom) {
	  	useRadioButtonOptions=true;
	    final int idx=showOptionDialog(
	        parent, msg, title, options, initialValue, addCancelButtonAtBottom, true, Basics.UNMODIFIABLE_EMPTY_LIST);
	    useRadioButtonOptions=false;
	    return idx;
	  }
  
  public static int showRadioButtonOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final int[] mnemonics,
	      final Object initialValue,
	      final boolean addCancelButtonAtBottom) {
	  	useRadioButtonOptions=true;
	    final int idx=showOptionDialog(
	        parent, msg, title, options, mnemonics, null, initialValue, null, addCancelButtonAtBottom, true, Basics.UNMODIFIABLE_EMPTY_LIST, null, null);
	    useRadioButtonOptions=false;
	    return idx;
	  }
  public static int[] showRadioButtonOptionDialog2(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options1,
	      final Object[] options2,
	      final int[] mnemonics,
	      final Object initialValue1,
	      final Object initialValue2,
	      
	      final boolean addCancelButtonAtBottom) {
	  	useRadioButtonOptions=true;
	  
	  int[]	idx = showOptionDialog2(parent, msg, title, options1, options2, mnemonics,
				 null,initialValue1, initialValue2,null,
				addCancelButtonAtBottom, true,  Basics.UNMODIFIABLE_EMPTY_LIST, null,
				null);
	  	
	  	
	    useRadioButtonOptions=false;
	    return idx;
	  }
  public static int showOptionDialog(
      final Component parent,
      final Object msg,
      final String title,
      final Object[] options,
      final Object initialValue,
      final boolean addCancelButtonAtBottom) {
    return showOptionDialog(
        parent, msg, title, options, initialValue, addCancelButtonAtBottom, true, Basics.UNMODIFIABLE_EMPTY_LIST);
  }
  public static int showOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final Object initialValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons) {
	  return showOptionDialog(parent, msg, title, options, null, initialValue, addCancelButtonAtBottom, vertical, additionalButtons);
  }
  public static int showOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final Object initialValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons,
	      final Collection<JButton> additionalButtonsAtBottom) {
	  return showOptionDialog(parent, msg, title, options, null, initialValue, addCancelButtonAtBottom, vertical, additionalButtons,additionalButtonsAtBottom);
  }
  
  public static boolean useRadioButtonOptions=false;
  public static int showOptionDialog(
      final Component parent,
      final Object msg,
      final String title,
      final Object[] options,
      final Icon [] icons,
      final Object initialValue,
      final boolean addCancelButtonAtBottom,
      final boolean vertical,
      final Collection<JButton> additionalButtons) {    
	  return showOptionDialog(parent, msg,title,options,null,icons,initialValue,null,addCancelButtonAtBottom,vertical,additionalButtons, null, null);
  }
  public static int showOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final Icon [] icons,
	      final Object initialValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons,
	      final Collection<JButton> additionalButtonsAtBottom
	      ) {    
		  return showOptionDialog(parent, msg,title,options,null,icons,initialValue,null,addCancelButtonAtBottom,vertical,additionalButtons, null, null,additionalButtonsAtBottom);
	  }
  public static int showOptionDialog2(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final int[] mnemonics,
	      final Icon [] icons,
	      final Object defaultValue,
	      final Object escapeValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons,
	      final String moreDetails,
	      final JCheckBox cb) {
		ToolTipOnDemand.getSingleton().hideTipWindow();
	    idx = -1; 
	    final JDialog dlg = getDialog(parent);
	    class Closer extends WindowAdapter {
	    	private boolean userClosed=true;
				public void windowClosing(final WindowEvent we) {
				if (userClosed && (myCanceler==null || !myCanceler.canceled)) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					dlg.dispose();
				}
			}
		};
		final Closer wa=new Closer();
	    final JPanel buttons = new JPanel(
	        vertical ? new GridLayout(options.length, 1, 3, 2) :
	        new GridLayout(1, options.length, 3, 2));
	    buttons.setBorder(BorderFactory.createRaisedBevelBorder());

	    AbstractButton focus = null;
	    if (!Basics.isEmpty(additionalButtons)){
	    	for (final AbstractButton b:additionalButtons){
	    		buttons.add(b);
	    	}
	    }
	    final ButtonGroup bg = new ButtonGroup();
		
	    for (int i = 0; i < options.length; i++) {
	      final int currentIndex = i;
	      final ActionListener al=new ActionListener() {
	          public void actionPerformed(final ActionEvent e) {
	            idx = currentIndex;
				wa.userClosed=false;
	            SwingBasics.closeWindow(dlg);	            
	          }
	        };
	        final String s=options[i].toString();
	        final Icon ic= icons != null && icons.length>i?icons[i]:null;
	                
	      final AbstractButton b;
	      if (useRadioButtonOptions) {
	    	  b=new JRadioButton(s, ic);
	    	  wa.userClosed=false;
	    	  SwingBasics.addMouseOver(b);	    	  
	    	  bg.add(b);
	    	      	  
	      } else {
	    	  b= SwingBasics.getButton(s,ic,'\0',al,null);
	      }
	      if (mnemonics != null ) {
    		  b.setMnemonic(mnemonics[i]);
    	  }
    	  if(Basics.equals(options[i], defaultValue)) {
    		  b.setSelected(true);
    		  if (b instanceof JButton){
    			  dlg.getRootPane().setDefaultButton((JButton) b);
    		  }
    	  }
    	  if(Basics.equals(options[i], escapeValue)) {
    		  if (b instanceof JButton){
    			  SwingBasics.registerEscape(dlg, b);
    		  }
    	  }
	      if (options[currentIndex].equals(defaultValue)) {
	        focus = b;
	      }
	      buttons.add(b);
	    }
	    ActionListener ok=null;
	    	if (mnemonics==null) {
	    		SwingBasics.ensureUniqueMnenmonics(bg);
	    	}
		    if (useRadioButtonOptions) {
	    	ok=new ActionListener() {
	    		public void actionPerformed(final ActionEvent e) {
	    			for (int i=0;i<buttons.getComponentCount();i++) {
	    				final Component c=buttons.getComponent(i);
	    				if (c instanceof JRadioButton) {
	    					if ( ((JRadioButton)c).isSelected()){
	    						 idx = i;
	    						wa.userClosed=false;
	    					}
	    				}
	    			}
	    		}
	    	};
	    }
	    myCanceler=null;
	    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(wa);
		final JPanel buttonPanel;
		if (!Basics.isEmpty(moreDetails)){
			final JPanel jp=getMoreLessPanel(dlg, msg.toString(), moreDetails, JOptionPane.INFORMATION_MESSAGE);
			show(false, dlg, "option", jp, title, JOptionPane.INFORMATION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok, cb);
		} else {
			show(dlg, "option", msg.toString(), title, JOptionPane.INFORMATION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok,cb);
		}
	    return idx;
	  }
  public static int showOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final int[] mnemonics,
	      final Icon [] icons,
	      final Object defaultValue,
	      final Object escapeValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons,
	      final String moreDetails,
	      final JCheckBox cb) {
		ToolTipOnDemand.getSingleton().hideTipWindow();
	    idx = -1; 
	    final JDialog dlg =getDialog(parent);
	    class Closer extends WindowAdapter {
	    	private boolean userClosed=true;
				public void windowClosing(final WindowEvent we) {
				if (userClosed && (myCanceler==null || !myCanceler.canceled)) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					dlg.dispose();
				}
			}
		};
		final Closer wa=new Closer();
	    final JPanel buttons = new JPanel(
	        vertical ? new GridLayout(options.length, 1, 3, 2) :
	        new GridLayout(1, options.length, 3, 2));
	    buttons.setBorder(BorderFactory.createRaisedBevelBorder());

	    AbstractButton focus = null;
	    if (!Basics.isEmpty(additionalButtons)){
	    	for (final AbstractButton b:additionalButtons){
	    		buttons.add(b);
	    	}
	    }
	    final ButtonGroup bg = new ButtonGroup();
		
	    for (int i = 0; i < options.length; i++) {
	      final int currentIndex = i;
	      final ActionListener al=new ActionListener() {
	          public void actionPerformed(final ActionEvent e) {
	            idx = currentIndex;
				wa.userClosed=false;
	            SwingBasics.closeWindow(dlg);	            
	          }
	        };
	        final String s=options[i].toString();
	        final Icon ic= icons != null && icons.length>i?icons[i]:null;
	                
	      final AbstractButton b;
	      if (useRadioButtonOptions) {
	    	  b=new JRadioButton(s, ic);
	    	  SwingBasics.addMouseOver(b);	    	  
	    	  bg.add(b);
	    	      	  
	      } else {
	    	  b= SwingBasics.getButton(s,ic,'\0',al,null);
	      }
	      if (mnemonics != null ) {
    		  b.setMnemonic(mnemonics[i]);
    	  }
    	  if(Basics.equals(options[i], defaultValue)) {
    		  b.setSelected(true);
    		  if (b instanceof JButton){
    			  dlg.getRootPane().setDefaultButton((JButton) b);
    		  }
    	  }
    	  if(Basics.equals(options[i], escapeValue)) {
    		  if (b instanceof JButton){
    			  SwingBasics.registerEscape(dlg, b);
    		  }
    	  }
	      if (options[currentIndex].equals(defaultValue)) {
	        focus = b;
	      }
	      buttons.add(b);
	    }
	    ActionListener ok=null;
	    	if (mnemonics==null) {
	    		SwingBasics.ensureUniqueMnenmonics(bg);
	    	}
		    if (useRadioButtonOptions) {
	    	ok=new ActionListener() {
	    		public void actionPerformed(final ActionEvent e) {
	    			for (int i=0;i<buttons.getComponentCount();i++) {
	    				final Component c=buttons.getComponent(i);
	    				if (c instanceof JRadioButton) {
	    					if ( ((JRadioButton)c).isSelected()){
	    						 idx = i;
	    						wa.userClosed=false;
	    					}
	    				}
	    			}
	    		}
	    	};
	    }
	    myCanceler=null;
	    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(wa);
		final JPanel buttonPanel;
		if (!Basics.isEmpty(moreDetails)){
			final JPanel jp=getMoreLessPanel(dlg, msg.toString(), moreDetails, JOptionPane.QUESTION_MESSAGE);
			show(false, dlg, "option", jp, title, JOptionPane.QUESTION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok, cb);
		} else {
			show(dlg, "option", msg.toString(), title, JOptionPane.QUESTION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok,cb);
		}
	    return idx;
	  }
  public static int showOptionDialog(
	      final Component parent,
	      final Object msg,
	      final String title,
	      final Object[] options,
	      final int[] mnemonics,
	      final Icon [] icons,
	      final Object defaultValue,
	      final Object escapeValue,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton> additionalButtons,
	      final String moreDetails,
	      final JCheckBox cb,
	      final Collection<JButton>additionalButtonsAtBottom) {
		ToolTipOnDemand.getSingleton().hideTipWindow();
	    idx = -1; 
	    final JDialog dlg = getDialog(parent);
	    currentOptionDialog = dlg;
	    class Closer extends WindowAdapter {
	    	private boolean userClosed=true;
				public void windowClosing(final WindowEvent we) {
				if (userClosed && (myCanceler==null || !myCanceler.canceled)) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					dlg.dispose();
				}
			}
		};
		final Closer wa=new Closer();
	    final JPanel buttons = new JPanel(
	        vertical ? new GridLayout(options.length, 1, 3, 2) :
	        new GridLayout(1, options.length, 3, 2));
	    buttons.setBorder(BorderFactory.createRaisedBevelBorder());

	    AbstractButton focus = null;
	    if (!Basics.isEmpty(additionalButtons)){
	    	for (final AbstractButton b:additionalButtons){
	    		buttons.add(b);
	    	}
	    }
	    final ButtonGroup bg = new ButtonGroup();
		
	    for (int i = 0; i < options.length; i++) {
	      final int currentIndex = i;
	      final ActionListener al=new ActionListener() {
	          public void actionPerformed(final ActionEvent e) {
	            idx = currentIndex;
				wa.userClosed=false;
	            SwingBasics.closeWindow(dlg);	            
	          }
	        };
	        final String s=options[i].toString();
	        final Icon ic= icons != null && icons.length>i?icons[i]:null;
	                
	      final AbstractButton b;
	      if (useRadioButtonOptions) {
	    	  b=new JRadioButton(s, ic);
	    	  SwingBasics.addMouseOver(b);	    	  
	    	  bg.add(b);
	    	      	  
	      } else {
	    	  b= SwingBasics.getButton(s,ic,'\0',al,null);
	      }
	      if (mnemonics != null ) {
    		  b.setMnemonic(mnemonics[i]);
    	  }
    	  if(Basics.equals(options[i], defaultValue)) {
    		  b.setSelected(true);
    		  if (b instanceof JButton){
    			  dlg.getRootPane().setDefaultButton((JButton) b);
    		  }
    	  }
    	  if(Basics.equals(options[i], escapeValue)) {
    		  if (b instanceof JButton){
    			  SwingBasics.registerEscape(dlg, b);
    		  }
    	  }
	      if (options[currentIndex].equals(defaultValue)) {
	        focus = b;
	      }
	      buttons.add(b);
	    }
	    ActionListener ok=null;
	    	if (mnemonics==null) {
	    		SwingBasics.ensureUniqueMnenmonics(bg);
	    	}
		    if (useRadioButtonOptions) {
	    	ok=new ActionListener() {
	    		public void actionPerformed(final ActionEvent e) {
	    			for (int i=0;i<buttons.getComponentCount();i++) {
	    				final Component c=buttons.getComponent(i);
	    				if (c instanceof JRadioButton) {
	    					if ( ((JRadioButton)c).isSelected()){
	    						 idx = i;
	    						wa.userClosed=false;
	    					}
	    				}
	    			}
	    		}
	    	};
	    }
	    myCanceler=null;
	    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(wa);
		final JPanel buttonPanel;
		if (!Basics.isEmpty(moreDetails)){
			final JPanel jp=getMoreLessPanel(dlg, msg.toString(), moreDetails, JOptionPane.QUESTION_MESSAGE);
			show(false, dlg, "option", jp, title, JOptionPane.QUESTION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok, cb,additionalButtonsAtBottom);
		} else {
			show(dlg, "option", msg.toString(), title, JOptionPane.QUESTION_MESSAGE,
					buttons,
					focus, 
					addCancelButtonAtBottom, ok,cb,additionalButtonsAtBottom);
		}
	    return idx;
	  }

  public static int[] showOptionDialog2(final Component parent,
			final Object message, final String title, final Object[] options1,
			final Object[] options2, final int[] mnemonics, final Icon[] icons,
			final Object defaultValue1, final Object defaultValue2,
			final Object escapeValue, final boolean addCancelButtonAtBottom,
			final boolean vertical,
			final Collection<JButton> additionalButtons,
			final String moreDetails, final JCheckBox cb) {
		String[] msgs = (String[]) message;
		String msg1 = msgs[0];
		String msg2 = msgs[1];
		int[] idxs = new int[2];
		idx = -1;
		idx2=-1;
		final JDialog dlg =getDialog(parent);
		class Closer extends WindowAdapter {
			private boolean userClosed = true;

			public void windowClosing(final WindowEvent we) {
				if (userClosed && (myCanceler == null || !myCanceler.canceled)) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					dlg.dispose();
				}
			}
		}
		;
		final JPanel basePanel= new JPanel(new BorderLayout());
		final Closer wa = new Closer();
		final JPanel buttons = new JPanel(vertical ? new GridLayout(
				options1.length, 1, 3, 2) : new GridLayout(1, options1.length,
				3, 2));
		buttons.setBorder(BorderFactory.createRaisedBevelBorder());
		final JPanel buttons2 = new JPanel(vertical ? new GridLayout(
				options2.length, 1, 3, 2) : new GridLayout(1, options2.length,
				3, 2));
		buttons2.setBorder(BorderFactory.createRaisedBevelBorder());
		AbstractButton focus = null;
		if (!Basics.isEmpty(additionalButtons)) {
			for (final JButton b : additionalButtons) {
				buttons.add(b);
			}
		}
		final ButtonGroup bg = new ButtonGroup();
		final ButtonGroup bg2 = new ButtonGroup();
		for (int i = 0; i < options1.length; i++) {
			final int currentIndex = i;
			final ActionListener al = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					idx = currentIndex;
					wa.userClosed = false;
					SwingBasics.closeWindow(dlg);
				}
			};
			final String s = options1[i].toString();
			final Icon ic = icons != null && icons.length > i ? icons[i] : null;

			final AbstractButton b;
			if (useRadioButtonOptions) {
				b = new JRadioButton(s, ic);
				SwingBasics.addMouseOver(b);
				bg.add(b);

			} else {
				b = SwingBasics.getButton(s, ic, '\0', al, null);
			}
			if (mnemonics != null) {
				b.setMnemonic(mnemonics[i]);
			}
			if (Basics.equals(options1[i], defaultValue1)) {
				b.setSelected(true);
				if (b instanceof JButton) {
					dlg.getRootPane().setDefaultButton((JButton) b);
				}
			}
			if (Basics.equals(options1[i], escapeValue)) {
				if (b instanceof JButton) {
					SwingBasics.registerEscape(dlg, b);
				}
			}
			if (options1[currentIndex].equals(defaultValue1)) {
				focus = b;
			}
			buttons.add(b);
		}
		
		for (int i = 0; i < options2.length; i++) {
			final int currentIndex = i;
			final ActionListener al = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					idx2 = currentIndex;
					wa.userClosed = false;
					SwingBasics.closeWindow(dlg);
				}
			};
			final String s = options2[i].toString();
			final Icon ic = icons != null && icons.length > i ? icons[i] : null;

			final AbstractButton b;
			if (useRadioButtonOptions) {
				b = new JRadioButton(s, ic);
				SwingBasics.addMouseOver(b);
				bg2.add(b);

			} else {
				b = SwingBasics.getButton(s, ic, '\0', al, null);
			}
			if (mnemonics != null) {
				b.setMnemonic(mnemonics[i]);
			}
			if (Basics.equals(i, defaultValue2)) {
				b.setSelected(true);
				if (b instanceof JButton) {
					dlg.getRootPane().setDefaultButton((JButton) b);
				}
			}
			if (Basics.equals(options2[i], escapeValue)) {
				if (b instanceof JButton) {
					SwingBasics.registerEscape(dlg, b);
				}
			}
			
			if (currentIndex==(Integer)(defaultValue2)) {
				focus = b;
			}
			buttons2.add(b);
		}
		basePanel.add(buttons,BorderLayout.NORTH);
		JPanel spacePanel = new JPanel();
		spacePanel.add(new JLabel());
		spacePanel.add(new JLabel());
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(new JLabel(msg2));
		JPanel mainCenterPanel= new JPanel(new BorderLayout());
		mainCenterPanel.add(spacePanel,BorderLayout.NORTH);
		mainCenterPanel.add(spacePanel,BorderLayout.CENTER);
		mainCenterPanel.add(centerPanel,BorderLayout.SOUTH);
		basePanel.add(mainCenterPanel,BorderLayout.CENTER);
		basePanel.add(buttons2,BorderLayout.SOUTH);
		
		
		ActionListener ok1 = null;
		if (mnemonics == null) {
			SwingBasics.ensureUniqueMnenmonics(bg);
		}
		if (useRadioButtonOptions) {
			ok1= new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					for (int i = 0; i < buttons.getComponentCount(); i++) {
						final Component c = buttons.getComponent(i);
						if (c instanceof JRadioButton) {
							if (((JRadioButton) c).isSelected()) {
								idx = i;
								wa.userClosed = false;
							}
						}
					}
					for (int i = 0; i < buttons2.getComponentCount(); i++) {
						final Component c = buttons2.getComponent(i);
						if (c instanceof JRadioButton) {
							if (((JRadioButton) c).isSelected()) {
								idx2 = i;
								wa.userClosed = false;
							}
						}
					}
				}
			};
			
		}
		
		myCanceler = null;
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.addWindowListener(wa);
		
		if (!Basics.isEmpty(moreDetails)) {
			final JPanel jp = getMoreLessPanel(dlg, msg1.toString(),
					moreDetails, JOptionPane.QUESTION_MESSAGE);
			show(false, dlg, "option", jp, title, JOptionPane.QUESTION_MESSAGE,
					basePanel, focus, addCancelButtonAtBottom, ok1, cb);
		} else {
			show(dlg, "option", msg1.toString(), title,
					JOptionPane.QUESTION_MESSAGE, basePanel, focus,
					addCancelButtonAtBottom, ok1, cb);
		}
		idxs[0] = idx;
		idxs[1]=idx2; 
		
		return idxs;
	}
  private static JPanel getMoreLessPanel(
		  final JDialog dlg,
		  final String briefMsg,
		  final String detailedMsg,
          final int optionPaneMessageType){
	    final JPanel msgPanel = new JPanel();
	    final JLabel msgLabel = new JLabel(briefMsg);
	    msgPanel.add(msgLabel);
	    if (infoIcon != null && optionPaneMessageType==JOptionPane.INFORMATION_MESSAGE){
	        msgLabel.setIcon(infoIcon);
	        msgLabel.setHorizontalTextPosition(JLabel.LEFT);
	    }
	    msgPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		final JButton details = SwingBasics.getButton("<html><small>More...</small></html>",
				MmsIcons.getGreatIcon(), 'd', null,
				"See explanation for marker expression on target");
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (msgLabel.getText().equals(briefMsg)) {
					msgLabel.setText(detailedMsg);
					details.setText("<html><small>Less</small></html>");
					details.setIcon(MmsIcons.getLessIcon());
				} else {
					msgLabel.setText(briefMsg);
					details.setText("<html><small>More...</small></html>");
					details.setIcon(MmsIcons.getGreatIcon());
				}
				SwingBasics.stylizeAsHyperLink(details);
				dlg.pack();
			}
		});
		msgPanel.add(new JLabel("   "));
		msgPanel.add(details);
		SwingBasics.stylizeAsHyperLink(details);
		return msgPanel;
  
  }
  public static boolean getBoolean(final String message,
                                   final boolean defaultValue) {
    return getBoolean(null, message, "Input required", defaultValue);
  }

  public static boolean getBoolean(
      final Component component,
      final String message,
      final String title,
      final boolean defaultValue) {
    final int idx = getChosenIndex(
        component,
        message,
        title,
        new String[] {"True", "False"}
        ,
        defaultValue ? 0 : 1, false);
    return idx < 0 ? defaultValue : idx == 0;
  }

  public static int getRadioButtonOption(
	      final String message,
	      final Object[] choices,
	      final int defaultChoice) {
	  useRadioButtonOptions=true;
	    final int idx=getChosenIndex(null,
	                          message,
	                          "Please make a choice...",
	                          choices,
	                          defaultChoice,
	                          false);
	    useRadioButtonOptions=false;
	    return idx;
  }
  public static int getChosenIndex(
      final String message,
      final Object[] choices,
      final int defaultChoice) {
    return getChosenIndex(null,
                          message,
                          "Please make a choice...",
                          choices,
                          defaultChoice,
                          false);
  }

  public static int getRadioButtonOption(
	      final String message,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom) {
	  final boolean prior=useRadioButtonOptions;
	  useRadioButtonOptions=true;
	  final int idx=getChosenIndex(null,
	                          message,
	                          "Please make a choice...",
	                          choices,
	                          defaultChoice,
	                          addCancelButtonAtBottom);
	  useRadioButtonOptions=prior;
	  
	  return idx;
	  }

  public static int getChosenIndex(
      final String message,
      final Object[] choices,
      final int defaultChoice,
      final boolean addCancelButtonAtBottom) {
    return getChosenIndex(null,
                          message,
                          "Please make a choice...",
                          choices,
                          defaultChoice,
                          addCancelButtonAtBottom);
  }

  public static String getChosenString(
      final Component component,
      final String msg,
      final String title,
      final String[] choices,
      final int defaultChoice,
      final boolean addCancelButtonAtBottom) {
    final int idx = getChosenIndex(
        component,
        msg == null ? "Please make a choice..." : msg,
        title,
        choices,
        defaultChoice,
        addCancelButtonAtBottom);
    return idx >= 0 ? choices[idx] : null;
  }

  public static int getRadioButtonOption(
	      final Component component,
	      final String msg,
	      final String title,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom) {
	  useRadioButtonOptions=true;
	    final int idx=getChosenIndex(component, msg, title, choices, defaultChoice,
	                          addCancelButtonAtBottom, true);
	    useRadioButtonOptions=false;
	     return idx;
	  }
  public static int getRadioButtonOption(
	      final Component component,
	      final String msg,
	      final String title,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom,
	      final Collection<JButton>additionalButtonsAtBottom) {
	  useRadioButtonOptions=true;
	    final int idx=getChosenIndex(component, msg, title, choices, defaultChoice,
	                          addCancelButtonAtBottom, true,additionalButtonsAtBottom);
	    useRadioButtonOptions=false;
	     return idx;
	  }

  public static int getChosenIndex(
      final Component component,
      final String msg,
      final String title,
      final Object[] choices,
      final int defaultChoice,
      final boolean addCancelButtonAtBottom) {
    return getChosenIndex(component, msg, title, choices, defaultChoice,
                          addCancelButtonAtBottom, true);
  }

  public static int getRadioButtonOption(
	      final Component component,
	      final String msg,
	      final String title,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical) {

	    assertPopupBasicsGui();
	    useRadioButtonOptions=true;
	    final int idx=showOptionDialog(component, msg, title,
	                            choices,
	                            defaultChoice >= 0 &&
	                            defaultChoice < choices.length ?
	                            choices[defaultChoice] : choices[0],
	                            addCancelButtonAtBottom,
	                            vertical,
	      Basics.UNMODIFIABLE_EMPTY_LIST);
	    useRadioButtonOptions=false;
	    return idx;
	  }

  public static int getChosenIndex(
	      final Component component,
	      final String msg,
	      final String title,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final JCheckBox cb) {

	    assertPopupBasicsGui();
	    bottomRightComponentIsLeft=true;
	    final int result=showOptionDialog(component, msg, title,
	                            choices,
	                            null, null,
	                            defaultChoice >= 0 &&
	                            defaultChoice < choices.length ?
	                            choices[defaultChoice] : choices[0],
	                            null,
	                            addCancelButtonAtBottom,
	                            vertical,
	                            null, null, cb);
	    bottomRightComponentIsLeft=false;
	    return result;
	  }

  public static boolean bottomRightComponentIsLeft=false;
  
  public static int getRadioButtonOption(
	      final String message,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom,
	      final JCheckBox cb) {
	  final boolean prior=useRadioButtonOptions;
	  useRadioButtonOptions=true;
	  final int idx=getChosenIndex(null,
	                          message,
	                          "Please make a choice...",
	                          choices,
	                          defaultChoice,
	                          addCancelButtonAtBottom,true,cb);
	  useRadioButtonOptions=prior;	  
	  return idx;
	  }
  public static int[] getRadioButtonOption2(final String message[],
			final Object[] choices1, final Object[] choices2,
			final int defaultChoice1, final int defaultChoice2,
			final boolean addCancelButtonAtBottom) {
		final boolean prior = useRadioButtonOptions;
		useRadioButtonOptions = true;
		assertPopupBasicsGui();
		int[] idx = new int[2];
		idx = showOptionDialog2(null, message, "Please make a choice...", choices1, choices2, null,
				 null,defaultChoice1 >= 0 && defaultChoice1 < choices1.length ? choices1[defaultChoice1]: choices1[0], defaultChoice2,null,
				addCancelButtonAtBottom, true,  Basics.UNMODIFIABLE_EMPTY_LIST, null,
				null);
		useRadioButtonOptions = prior;
		return idx;
	}

  public static int getChosenIndex(
      final Component component,
      final String msg,
      final String title,
      final Object[] choices,
      final int defaultChoice,
      final boolean addCancelButtonAtBottom,
      final boolean vertical) {

    assertPopupBasicsGui();

    return showOptionDialog(component, msg, title,
                            choices,
                            defaultChoice >= 0 &&
                            defaultChoice < choices.length ?
                            choices[defaultChoice] : choices[0],
                            addCancelButtonAtBottom,
                            vertical,
      Basics.UNMODIFIABLE_EMPTY_LIST);
  }
  public static int getChosenIndex(
	      final Component component,
	      final String msg,
	      final String title,
	      final Object[] choices,
	      final int defaultChoice,
	      final boolean addCancelButtonAtBottom,
	      final boolean vertical,
	      final Collection<JButton>additionalButtonsAtBottom) {

	    assertPopupBasicsGui();

	    return showOptionDialog(component, msg, title,
	                            choices,
	                            defaultChoice >= 0 &&
	                            defaultChoice < choices.length ?
	                            choices[defaultChoice] : choices[0],
	                            addCancelButtonAtBottom,
	                            vertical,
	                            Basics.UNMODIFIABLE_EMPTY_LIST,
	                            additionalButtonsAtBottom);
	  }
  private static JButton addYesNo(
	      final JDialog dlg,
	      final JPanel buttons,
	      final String yesText,
	      final String noText,
	      final Input echoActions) {
	  return addYesNo(dlg, buttons, yesText, 'y',noText, echoActions, true);
  }

  private static JButton addYesNo(
      final JDialog dlg,
      final JPanel buttons,
      final String yesText,
      final char mnemonic,
      final String noText,
      final Input echoActions,
      final boolean yesIsDefault) {

    final JButton yesButton = SwingBasics.getButton(
        yesText,
        MmsIcons.getYesIcon(),
        yesText == null?'y':mnemonic,
        new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        yes = true;
        SwingBasics.closeWindow(dlg);
      }
    }

    , "");
    
    JButton focus=yesButton;
    buttons.add(yesButton);
    final JRootPane rp = dlg.getRootPane();
    final ActionListener closeAction =
        new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        yes = false;
        SwingBasics.closeWindow(dlg);
      }
    };
    if (noText != null){
    	
        final JButton noButton = SwingBasics.getButton(
          noText,
          MmsIcons.getCancelIcon(),
          'n',
          closeAction,
          "");
        if (!yesIsDefault){
        	focus=noButton;
        }
        buttons.add(noButton);
        rp.registerKeyboardAction(
          new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                noButton.doClick(132);
            }
        }

        ,

        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    if (echoActions != null && echoActions.requiresActionEchoing()) {
      SwingBasics.reuseDefaultButtonAction(
          echoActions.getEditorComponent(),
          dlg,
          JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,echoActions);
    }
    rp.setDefaultButton(focus);
    return focus;
  }

  public static boolean showMessageDialog(
	      final Component parent,
	      final String msg,
	      final String title,
	      final int optionPaneMessageType) {
	  return showMessageDialog(parent,msg,title,optionPaneMessageType, true);
  }

 public static boolean showMessageDialog2(
	      final Component parent,
	      final String briefMsg,
	      final String detailedMsg,
	      final String title,
	      final int optionPaneMessageType) {
	  return showMessageDialog2(parent, briefMsg,detailedMsg, title,optionPaneMessageType, true);
  }
 public static boolean showMessageDialog2(
	      final Component parent,
	      final String briefMsg,
	      final String detailedMsg,
	      final String title,
	      final int optionPaneMessageType,
	      final boolean yesIsDefault) {
		  return showMessageDialog2(parent, briefMsg,detailedMsg, title, optionPaneMessageType, yesIsDefault, null, 'y');
	  }


  public static boolean showMessageDialog(
      final Component parent,
      final String msg,
      final String title,
      final int optionPaneMessageType,
      final boolean yesIsDefault) {
	  return showMessageDialog(parent, msg, title, optionPaneMessageType, yesIsDefault, null, 'y');
  }

  private static void show(
      final JDialog dlg,
      final String id,
      final String msg,
      final String title,
      final int optionPaneMessageType,
      final JPanel buttons,
      final JComponent focus,
      final boolean addCancelButtonAtBottom) {
	  show(needScrollbar(msg), dlg, id, getMsgPanel(msg, optionPaneMessageType), title,
		         optionPaneMessageType, buttons, focus, addCancelButtonAtBottom);
  }
  private static void show(
      final JDialog dlg,
      final String id,
      final String msg,
      final String title,
      final int optionPaneMessageType,
      final JPanel buttons,
      final JComponent focus,
      final boolean addCancelButtonAtBottom,
      final ActionListener ok,
      final JComponent otherBottomRightComponent) {

      show(needScrollbar(msg), dlg, id, getMsgPanel(msg, optionPaneMessageType), title,
         optionPaneMessageType, buttons, focus, addCancelButtonAtBottom, ok, otherBottomRightComponent);
  }
  private static void show(
	      final JDialog dlg,
	      final String id,
	      final String msg,
	      final String title,
	      final int optionPaneMessageType,
	      final JPanel buttons,
	      final JComponent focus,
	      final boolean addCancelButtonAtBottom,
	      final ActionListener ok,
	      final JComponent otherBottomRightComponent,
	      final Collection<JButton>additionalButtonsAtBottom) {

	      show(needScrollbar(msg), dlg, id, getMsgPanel(msg, optionPaneMessageType), title,
	         optionPaneMessageType, buttons, focus, addCancelButtonAtBottom, ok, otherBottomRightComponent,additionalButtonsAtBottom);
	  }

 private static void show2(
	      final JDialog dlg,
	      final String id,
	      final String briefMsg,
	      final String detailedMsg,
	      final String title,
	      final int optionPaneMessageType,
	      final JPanel buttons,
	      final JComponent focus,
	      final boolean addCancelButtonAtBottom) {
	 if(detailedMsg==null){
		 show(false, dlg, id, getMsgPanel(briefMsg, optionPaneMessageType), title,
		         optionPaneMessageType, buttons, focus, addCancelButtonAtBottom);
	 }
	 else{
		  show(false, dlg, id, getMoreLessPanel(dlg, briefMsg, detailedMsg, optionPaneMessageType), title,
			         optionPaneMessageType, buttons, focus, addCancelButtonAtBottom);
	 }
		  
	  }
  private static boolean needScrollbar(final String msg){
      final int n = msg.length();     
      final boolean isHtml = msg.indexOf("<html>") >= 0, hasTable = msg.indexOf("<table") >= 0,
                             needScrollbar = n > ((isHtml && !hasTable) ? 1500 : 1100);
      return needScrollbar;
  }
  private static void show(final boolean needScrollBar, final JDialog dlg,
			final String id, final JPanel topPanel, final String title,
			final int optionPaneMessageType, final JPanel buttons,
			final JComponent focus, final boolean addCancelButtonAtBottom) {
	  show(
			  needScrollBar, 
			  dlg, 
			  id, 
			  topPanel, 
			  title, 
			  optionPaneMessageType, 
			  buttons, 
			  focus, 
			  addCancelButtonAtBottom, 
			  null,
			  null);
  }
  
  private static void show(final boolean needScrollBar, final JDialog dlg,
			final String id, final JPanel topPanel, final String title,
			final int optionPaneMessageType, final JPanel buttons,
			final JComponent focus, 
			final boolean addBottomRightPanel, 
			final ActionListener okAction,
			final JComponent otherBottomRightComponent) {
	  	ToolTipOnDemand.hideManagerWindow();
		final JPanel cp = new GradientBasics.Panel();
		dlg.getContentPane().add(cp);
		JLabel iconLabel = null;
		cp.setLayout(new BorderLayout());
		if (optionPaneMessageType != JOptionPane.PLAIN_MESSAGE
				&& !addBottomRightPanel && okAction==null && otherBottomRightComponent==null) {
			 iconLabel = new JLabel(
					getIcon(optionPaneMessageType));
			cp.add(iconLabel, BorderLayout.WEST);
		}
		final JPanel middlePanel = SwingBasics.newPanel((Icon) null,
				buttons);
		middlePanel
				.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 10));
		if (!needScrollBar) {
			cp.add(topPanel, BorderLayout.NORTH);
			cp.add(middlePanel, BorderLayout.CENTER);
			if (addBottomRightPanel || okAction != null || otherBottomRightComponent != null) {
				cp.add(getOkCancelPanel(dlg, optionPaneMessageType, addBottomRightPanel, okAction, otherBottomRightComponent), BorderLayout.SOUTH);
			}
		} else {
			if (needScrollBar) {
				cp.add(new JScrollPane(topPanel), BorderLayout.CENTER);
				dlg.getContentPane().setPreferredSize(new Dimension(500, 600));
			} else {
				cp.add(topPanel, BorderLayout.NORTH);

			}
			if (addBottomRightPanel || okAction != null || otherBottomRightComponent!=null) {
				final JPanel jp4 = new JPanel(new BorderLayout());
				jp4.add(middlePanel, BorderLayout.CENTER);
				jp4.add(getOkCancelPanel(dlg, optionPaneMessageType, addBottomRightPanel, okAction, otherBottomRightComponent), BorderLayout.SOUTH);
				cp.add(jp4, BorderLayout.SOUTH);
			} else {
				cp.add(middlePanel, BorderLayout.SOUTH);

			}
		}
		dlg.setTitle(title);
		dlg.setModal(true);
		if (relativeToParent){
			dlg.pack();		
		}else if (forceBottomRight){
			dlg.pack();
		}else{
			final boolean disposeOnClose=dlg.getDefaultCloseOperation()!=JDialog.DO_NOTHING_ON_CLOSE;
			SwingBasics.packAndPersonalize(
					dlg, 
					null,
					PopupBasics.PROPERTY_SAVIOR,
					PopupBasics.class.getName() + "." + id + optionPaneMessageType,
					true,
					false,
					false,
					disposeOnClose,
					null);
		}
		if(iconLabel != null && topPanel != null) {
			if(buttons.getComponentCount() < 2) {
				dlg.setSize(new Dimension(
						topPanel.getWidth() + iconLabel.getWidth(), dlg.getHeight()));
		
			}
		}
		if (location != null){
			dlg.setLocation(location);
			location=null;
		} else if (forceBottomRight){
			SwingBasics.bottomRight(dlg);
		}
		GradientBasics.setTransparentChildren(cp, true);
		SwingBasics.showUpFront(dlg, focus, setAlwaysOnTop);
	}
  private static void show(final boolean needScrollBar, final JDialog dlg,
			final String id, final JPanel topPanel, final String title,
			final int optionPaneMessageType, final JPanel buttons,
			final JComponent focus, 
			final boolean addBottomRightPanel, 
			final ActionListener okAction,
			final JComponent otherBottomRightComponent,
			final Collection<JButton>additionalButtonsAtBottom) {
	  	ToolTipOnDemand.hideManagerWindow();
		final JPanel cp = new GradientBasics.Panel();
		dlg.getContentPane().add(cp);
		JLabel iconLabel = null;
		cp.setLayout(new BorderLayout());
		if (optionPaneMessageType != JOptionPane.PLAIN_MESSAGE
				&& !addBottomRightPanel && okAction==null && otherBottomRightComponent==null) {
			 iconLabel = new JLabel(
					getIcon(optionPaneMessageType));
			cp.add(iconLabel, BorderLayout.WEST);
		}
		final JPanel middlePanel = SwingBasics.newPanel((Icon) null,
				buttons);
		middlePanel
				.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 10));
		if (!needScrollBar) {
			cp.add(topPanel, BorderLayout.NORTH);
			cp.add(middlePanel, BorderLayout.CENTER);
			if (addBottomRightPanel || okAction != null || otherBottomRightComponent != null) {
				cp.add(getOkCancelPanel(dlg, optionPaneMessageType, addBottomRightPanel, okAction, otherBottomRightComponent,additionalButtonsAtBottom), BorderLayout.SOUTH);
			}
		} else {
			if (needScrollBar) {
				cp.add(new JScrollPane(topPanel), BorderLayout.CENTER);
				dlg.getContentPane().setPreferredSize(new Dimension(500, 600));
			} else {
				cp.add(topPanel, BorderLayout.NORTH);

			}
			if (addBottomRightPanel || okAction != null || otherBottomRightComponent!=null) {
				final JPanel jp4 = new JPanel(new BorderLayout());
				jp4.add(middlePanel, BorderLayout.CENTER);
				jp4.add(getOkCancelPanel(dlg, optionPaneMessageType, addBottomRightPanel, okAction, otherBottomRightComponent,additionalButtonsAtBottom), BorderLayout.SOUTH);
				cp.add(jp4, BorderLayout.SOUTH);
			} else {
				cp.add(middlePanel, BorderLayout.SOUTH);

			}
		}
		dlg.setTitle(title);
		dlg.setModal(true);if (relativeToParent){
			dlg.pack();		
		}else if (forceBottomRight){
			dlg.pack();
		}else{
			final boolean disposeOnClose=dlg.getDefaultCloseOperation()!=JDialog.DO_NOTHING_ON_CLOSE;
			SwingBasics.packAndPersonalize(
					dlg, 
					null,
					PopupBasics.PROPERTY_SAVIOR,
					PopupBasics.class.getName() + "." + id + optionPaneMessageType,
					true,
					false,
					false,
					disposeOnClose,
					null);
		}
		if(iconLabel != null && topPanel != null) {
			if(buttons.getComponentCount() < 2) {
				dlg.setSize(new Dimension(
						topPanel.getWidth() + iconLabel.getWidth(), dlg.getHeight()));
		
			}
		}
		if (location != null){
			dlg.setLocation(location);
			location=null;
		} else if (forceBottomRight){
			SwingBasics.bottomRight(dlg);
		}
		GradientBasics.setTransparentChildren(cp, true);
		SwingBasics.showUpFront(dlg, focus, setAlwaysOnTop);
	}

  public static boolean useEditorPaneNextTimeOnly=false;
  private static JPanel getMsgPanel(final String msg,
                                    final int optionPaneMessageType) {

    final JPanel msgPanel = new JPanel(new BorderLayout(45, 0));
    if (useEditorPaneNextTimeOnly){
    	useEditorPaneNextTimeOnly=false;
    	final JEditorPane ep=new JEditorPane("text/html", msg);
    	ep.setOpaque(false);
    	ep.setEditable(false);
    	ep.setCaretPosition(0);
        msgPanel.add(ep, BorderLayout.CENTER);
    } else {
    final JLabel msgLabel = new JLabel(msg);
    msgPanel.add(msgLabel, BorderLayout.CENTER);
    if (infoIcon != null && optionPaneMessageType==JOptionPane.INFORMATION_MESSAGE){
        msgLabel.setIcon(infoIcon);
        msgLabel.setHorizontalTextPosition(JLabel.LEFT);
    }
    }
    msgPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    
    return msgPanel;
  }

  static Properties properties;
  static String propertyFileName = null;

  public static void setProperties(
      final Properties properties,
      final String propertyFileName) {

    PopupBasics.properties = properties;
    PopupBasics.propertyFileName = propertyFileName;
  }

  public static Properties getProperties(final Properties _properties) {
    final Properties properties;
    if (_properties == null) {
      if (PopupBasics.properties == null) {
        properties = PersonalizableTableModel.getGlobalProperties();
      } else {
        properties = PopupBasics.properties;
      }
    } else {
      properties = _properties;
    }
    return properties;
  }

  public final static PropertiesBasics.Savior PROPERTY_SAVIOR = new
      PropertiesBasics.Savior() {
    public void save(final Properties properties) {
      final String fileName = getPropertyFileName(properties);
      System.out.println("Saving "+fileName);
      if (fileName != null) {
        PropertiesBasics.saveProperties(properties, fileName, null);
        System.out.println("Saved "+fileName);
      }
    }
  };
  public static String getPropertyFileName(final Properties properties) {

    final String propertyFileName;
    if (properties == PersonalizableTableModel.globalProperties) {
      propertyFileName = PersonalizableTableModel.getGlobalPropertyFileName();
    } else if (properties == PopupBasics.properties) {
      propertyFileName = PopupBasics.propertyFileName;
    } else {
      propertyFileName = null;
    }
    return propertyFileName;
  }

  public static Icon infoIcon;
  public static Icon opionPaneIcon;

  public static Icon getIcon(final int optionPaneMessageType) {
	if (opionPaneIcon != null) {
		return opionPaneIcon;
	}
    final String s;
    switch (optionPaneMessageType) {
    default:
      s = "OptionPane.informationIcon";
      break;
    case JOptionPane.ERROR_MESSAGE:
      s = "OptionPane.errorIcon";
      break;
    case JOptionPane.QUESTION_MESSAGE:
      s = "OptionPane.questionIcon";
      break;
    case JOptionPane.WARNING_MESSAGE:
      s = "OptionPane.warningIcon";
    }
    return (Icon) UIManager.get(s);
  }

  public static String getFileName(
      final String dialogTitle,
      final String extension,
      final String typeDescription,
      final boolean readOnly,
      final String dir,
      final Window containingWnd) {
    return getFileName(dialogTitle, extension, typeDescription, null, null,
                       readOnly, dir, null,
                       containingWnd);
  }

  public static String getFileName(
		  final Window window,
	      final String extension,
	      final String typeDescription,
	      final boolean readOnly,
	      final String selectedFile) {
	  return getFileName(null, extension, typeDescription, null, null,
              readOnly, selectedFile==null?null:new File(selectedFile).getParent(), selectedFile,window);
  
  }

  public static String getFileName(
      final String extension,
      final String typeDescription,
      final boolean readOnly,
      final String dir,
      final Window containingWnd) {
    return getFileName(null, extension, typeDescription, readOnly, dir,
                       containingWnd);
  }

  public static String getFileName(
      final String dialogTitle,
      final String extension,
      final String typeDescription,
      final boolean readOnly) {
    return getFileName(dialogTitle, extension, typeDescription, null, null,
                       readOnly, null, null, null);
  }

  public static String getFileName(
      final String extension,
      final String typeDescription,
      final boolean readOnly) {
    return getFileName(null, extension, typeDescription, readOnly);
  }

  public static String getFileName(
      final String regexMatch,
      final String regexAvoid,
      final String extension,
      final String typeDescription,
      final String approveButtonText,
      final String approveToolTip,
      final boolean readOnly,
      final String currentDirectory,
      final String selectedFile,
      final Window containingWnd) {

    assertPopupBasicsGui();

    return getFileName(
        null,
        regexMatch,
        regexAvoid,
        extension,
        typeDescription,
        approveButtonText,
        approveToolTip,
        readOnly,
        currentDirectory,
        selectedFile,
        containingWnd);

  }

  public static String getFileName(
      final String dialogTitle,
      final String regexMatch,
      final String regexAvoid,
      final String extension,
      final String typeDescription,
      final String approveButtonText,
      final String approveToolTip,
      final boolean readOnly,
      final String currentDirectory,
      final String selectedFile,
      final Window containingWnd) {

    if (Basics.isEmpty(regexMatch) && Basics.isEmpty(regexAvoid)) {
      return getFileName(
          extension,
          typeDescription,
          approveButtonText,
          approveToolTip,
          readOnly,
          currentDirectory,
          selectedFile,
          containingWnd);
    }
    final boolean cs = Basics.isMac();
    final String p_regexMatch = cs || Basics.isEmpty(regexMatch) ? regexMatch :
                                regexMatch.toLowerCase();
    final String p_regexAvoid = cs || Basics.isEmpty(regexAvoid) ? regexAvoid :
                                regexAvoid.toLowerCase();
    final Pattern match = Basics.isEmpty(p_regexMatch) ? null :
                          Pattern.compile(p_regexMatch),
                          avoid = Basics.isEmpty(p_regexAvoid) ? null :
                                  Pattern.compile(p_regexAvoid);
    final FileFilter fileFilter = new FileFilter() {
      public boolean accept(final File f) {
        boolean ok = f.isDirectory();
        if (!ok && ((readOnly && f.canRead()) || f.canWrite())) {
          final String n = cs ? f.getName() : f.getName().toLowerCase();
          if (match != null) {
            final Matcher matcher = match.matcher(n);
            ok = matcher.matches();
          } else {
            ok = true;
          }
          if (ok && avoid != null) {
            final Matcher shunner = avoid.matcher(n);
            ok = !shunner.matches();
          }
        }
        return ok;
      }

      public String getDescription() {
        return typeDescription;
      }
    };
    return getFileName(
        dialogTitle,
        fileFilter,
        typeDescription,
        extension.startsWith(".") ? extension : '.' + extension,
        approveButtonText,
        approveToolTip,
        readOnly,
        currentDirectory,
        selectedFile,
        containingWnd);

  }

  public static String getFileName(
      final String extension,
      final String typeDescription,
      final String approveButtonText,
      final String approveToolTip,
      final boolean readOnly,
      final String dir,
      final String selectedFile,
      final Window containingWnd) {
    return getFileName(
        null,
        extension,
        typeDescription,
        approveButtonText,
        approveToolTip,
        readOnly,
        dir,
        selectedFile,
        containingWnd);
  }

  public static String getFileName(
      final String dialogTitle,
      final String extension,
      final String typeDescription,
      final String approveButtonText,
      final String approveToolTip,
      final boolean readOnly,
      final String dir,
      final String selectedFile,
      final Window containingWnd) {

    final String ext;
    if (extension == null) {
      ext = null;
    } else {
      ext = extension.startsWith(".") ? extension : '.' + extension;
    }
    final boolean cs = Basics.isMac();
    final String extToMatch;
    if (ext == null) {
      extToMatch = null;
    } else {
      extToMatch = cs ? ext : ext.toLowerCase();
    }
    final FileFilter fileFilter = new FileFilter() {
      public boolean accept(final File f) {
        boolean ok = f.isDirectory();
        if (extToMatch != null && !ok &&
            ((readOnly && f.canRead()) || f.canWrite())) {
          final String n = cs ? f.getName() : f.getName().toLowerCase();
          ok = n.endsWith(extToMatch);
        }
        return ok;
      }

      public String getDescription() {
        return typeDescription;
      }
    };
    return getFileName(
        dialogTitle,
        fileFilter,
        typeDescription,
        ext,
        approveButtonText,
        approveToolTip,
        readOnly,
        dir,
        selectedFile,
        containingWnd);

  }

  private static String getFileName(
      final String dialogTitle,
      final FileFilter fileFilter,
      final String typeDescription,
      final String ext,
      final String _approveButtonText,
      final String _approveToolTip,
      final boolean readOnly,
      final String dir,
      final String selectedFile,
      final Window _containingWnd) {
      final Window containingWnd = _containingWnd == null ? SwingBasics.mainFrame :
                                   _containingWnd;
      personalizeNextWindowActivated("filechooser", true, null, null, JDialog.class);
      final String approveButtonText;
      if (Basics.isEmpty(_approveButtonText)) {
          approveButtonText = readOnly ? "Open" : "Save";
      } else {
          approveButtonText = _approveButtonText;
      }
      final String approveToolTip;
      if (Basics.isEmpty(_approveToolTip)) {
          approveToolTip = readOnly ? "Open the file" : "Save the file";
      } else {
          approveToolTip = _approveToolTip;
      }
      final JFileChooser c = new JFileChooser();
      c.setDialogTitle(
        dialogTitle != null ?
        dialogTitle :
        Basics.capitalize(approveButtonText) + " " +
        Basics.uncapitalize(typeDescription));
      c.setAcceptAllFileFilterUsed(false);
      c.setFileFilter(fileFilter);
      String directory = null;
      if (!Basics.isEmpty(dir)) {
          directory = dir;
      } else {
          directory = System.getProperty("user.home");
      }
      final File dirFile = new File(directory);
      c.setDialogType(
        readOnly ?
        JFileChooser.OPEN_DIALOG :
        JFileChooser.SAVE_DIALOG);
      if (dirFile.isDirectory()) {
          c.setCurrentDirectory(dirFile);
          if (!Basics.isEmpty(selectedFile)) {
              final File file = new File(IoBasics.concat(directory, selectedFile));
              c.setSelectedFile(file);
          }
      } else {
          final File parentFile = dirFile.getParentFile();
          if (parentFile != null) {
              c.setCurrentDirectory(parentFile);
          }
          c.setSelectedFile(dirFile);
      }
      c.setApproveButtonText(approveButtonText);
      c.setApproveButtonToolTipText(approveToolTip);
      int rVal = 0;
      for (; ; ) {
          if (readOnly) {
              rVal = c.showOpenDialog(containingWnd);
          } else {
              rVal = c.showSaveDialog(containingWnd);
          }
          if (rVal == JFileChooser.APPROVE_OPTION) {
              File file = c.getSelectedFile();
              if (file != null) {
                  String s = file.getAbsolutePath();
                  final int idx = s.lastIndexOf(File.separatorChar);

                  final String fileName = s.substring(idx + 1);
                  final String folderName = s.substring(0, idx);
                  if (ext != null && !s.toLowerCase().endsWith(ext.toLowerCase()) &&
                      !s.endsWith(".")) {
                      s += ext;
                      file = new File(s);
                  }
                  if (!readOnly) {

                      if (file.exists() && !ask(containingWnd, Basics.startHtmlUncentered(
                        "Overwrite file?") +
                                                fileName +
                                                " already exists, in the folder<br>" +
                                                folderName + Basics.endHtml())) {
                          s = null;
                      } else {
                          if (containingWnd != null) {
                              containingWnd.toFront();
                          }
                          return s;
                      }
                  } else {
                      if (!file.exists()) {
                          alert("Can not find " + file.getAbsolutePath(), true);
                          s = null;
                      } else {
                          if (containingWnd != null) {
                              containingWnd.toFront();
                          }

                          return s;
                      }
                  }
              }
          } else {
              break;
          }
      }
      if (containingWnd != null) {
          containingWnd.toFront();
      }

      return null;
  }

  public static String getDirName(
      final String dialogTitle,
      final String dir,
      final String _approveButtonText,
      final String approveToolTip,
      final Window _containingWnd) {
    final Window containingWnd  = _containingWnd==null?SwingBasics.mainFrame:_containingWnd;

    assertPopupBasicsGui();

    personalizeNextWindowActivated("filechooser", true, null, null, JDialog.class);

    final String approveButtonText = Basics.isEmpty(_approveButtonText) ?
                                     "Choose" : _approveButtonText;

    final JFileChooser c = new JFileChooser();
    c.setDialogTitle(dialogTitle == null ? "Choose a directory" : dialogTitle);
    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    final String directory = !Basics.isEmpty(dir) ? dir :
                             System.getProperty("user.home");
    c.setDialogType(JFileChooser.OPEN_DIALOG);
    final File dirFile = new File(directory);
    final File parentDirFile = dirFile.getParentFile();
//    if (parentDirFile == null) {
    c.setCurrentDirectory(dirFile);
    /*    }
        else {
          c.setCurrentDirectory(parentDirFile);
          c.setSelectedFile(dirFile);
        }*/
    c.setApproveButtonText(approveButtonText);
    c.setApproveButtonToolTipText(approveToolTip);
    c.setAcceptAllFileFilterUsed(false);
    final FileFilter fileFilter = new FileFilter() {
      public boolean accept(File f) {
        return true;
      }

      public String getDescription() {
        return "Choose directory";
      }
    };
    c.setFileFilter(fileFilter);

    int rVal = c.showOpenDialog(containingWnd);
    if (rVal == JFileChooser.APPROVE_OPTION) {
      String s = c.getSelectedFile().getAbsolutePath();
      return s;
    }
    return null;
  }

  public static boolean alertIfNeeded(final Collection messages) {

    final boolean needsAlert = messages.size() > 0;
    if (needsAlert) {
      final StringBuilder sb = new StringBuilder();
      for (final Iterator it = messages.iterator(); it.hasNext(); ) {
        sb.append(it.next());
        sb.append('\n');
      }
      alert(sb.toString(), false);
    }
    return needsAlert;
  }

  public static void alertAsync(final String message, final boolean beep) {

    assertPopupBasicsGui();

    if (beep) {
      beep();
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        alert(message);
      }
    });
  }

  public static void beep() {
    Toolkit.getDefaultToolkit().beep();
  }

  public static final KeyStroke
      ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
  ESCAPE_RELEASED = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
  ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
  ENTER_RELEASED = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);

  private static void handleKeys(final JTextField tf) {

    tf.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final JRootPane rootPane = tf.getRootPane();
        if (rootPane != null) {
          final JButton b = rootPane.getDefaultButton();
          if (b != null) {
             b.doClick();
          }
        }
      }
    });
    tf.addKeyListener(new KeyAdapter() {
      public void keyReleased(final KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
          final JRootPane rootPane = tf.getRootPane();
          if (rootPane != null) {
            final ActionListener al = rootPane.getActionForKeyStroke(ESCAPE);
            if (al != null) {
              al.actionPerformed(new ActionEvent(tf, 1, "escape"));
            }
          }
        }
      }

    });

  }

  public static void personalizeNextWindowActivated(
      final String id,
      final boolean sizeMatters,
      final Properties properties,
      final PropertiesBasics.Savior propertySavior,
      final Class cl) {

    final AWTEventListener al = new AWTEventListener() {
      public void eventDispatched(final AWTEvent e) {
        if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
          if (cl != null) {
            final Object o = e.getSource();
            if (o == null || !cl.equals(o.getClass())) {
              return;
            }
          }
          Toolkit.getDefaultToolkit().removeAWTEventListener(this);
          SwingBasics.needToRefocusDlg=true;
          SwingBasics.packAndPersonalize(
              (Window) e.getSource(),
              properties,
              propertySavior == null ? PopupBasics.PROPERTY_SAVIOR :
              propertySavior,
              id,
              false,
              sizeMatters,
              true);
          SwingBasics.needToRefocusDlg=false;
        }
      }
    };
    Toolkit.getDefaultToolkit().addAWTEventListener(
        al,
        AWTEvent.WINDOW_EVENT_MASK);

  }

  public static int offerChoiceList (final Window owner,
      final String title,
      final String msg,
      final Object[] choices,
      final String[] toolTips,
      final boolean putTipInPane,
      final int defaultChoice,
      final JButton otherButton,
      final String helpUrl){

      return PopupBasics.offerChoiceList (owner, title, msg, choices, toolTips,
              putTipInPane, defaultChoice, otherButton, helpUrl,
              ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

  }
  
  public static int offerChoiceListAndRestrictSingleSelection (final Window owner,
	      final String title,
	      final String msg,
	      final Object[] choices,
	      final String[] toolTips,
	      final boolean putTipInPane,
	      final int defaultChoice,
	      final JButton otherButton,
	      final String helpUrl){

	      return PopupBasics.offerChoiceList (owner, title, msg, choices, toolTips,
	              putTipInPane, defaultChoice, otherButton, helpUrl,
	              ListSelectionModel.SINGLE_SELECTION);

	  }

  public static int offerChoiceList(
      final Window owner,
      final String title,
      final String msg,
      final Object[] choices,
      final String[] toolTips,
      final boolean putTipInPane,
      final int defaultChoice,
      final JButton otherButton,
      final String helpUrl,
      final int selectionMode) {

    final SwingBasics.Dialog dialog =
        owner instanceof JFrame ? new SwingBasics.Dialog(  (JFrame)owner, title):
        owner instanceof JDialog? new SwingBasics.Dialog((JDialog)owner, title) :
        new SwingBasics.Dialog(title);


    final JPanel mainPanel = new GradientBasics.Panel(new BorderLayout(0, 1));
    dialog.getRootPane().setBorder(ColorPreferences.getGenieBorder());

  
    final JList jl = new JList(choices) {
      public String getToolTipText(final MouseEvent event) {
        if (toolTips == null || putTipInPane) {
          return super.getToolTipText(event);
        }
        final Point p = event.getPoint();
        final int location = locationToIndex(p);
        final String tip = toolTips[location];
        return tip;
      }
    };
    if (putTipInPane) {
      final JTextPane jtp = new JTextPane();
      jtp.setContentType("text/html");
      jtp.setMargin(new java.awt.Insets(3, 5, 3, 3));
      jl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
          final int idx = jl.getSelectedIndex();
          if (idx >= 0) {
            jtp.setText(toolTips[idx]);
            jtp.setCaretPosition(0);
          }
        }
      });
      final JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                                            new JScrollPane(jl),
                                            new JScrollPane(jtp));
      jsp.setResizeWeight(0.33);
      mainPanel.add(jsp, BorderLayout.CENTER);
    } else {
      mainPanel.add(new JScrollPane(jl), BorderLayout.CENTER);
		jl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()){
					jl.repaint();
				}
			}
		});  
    }
    if (defaultChoice>=0 && defaultChoice < choices.length){
    	jl.scrollRectToVisible(jl.getCellBounds(defaultChoice, defaultChoice));
    }
    jl.setSelectionMode (selectionMode);
    jl.setSelectionInterval(defaultChoice, defaultChoice);
    jl.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent evt) {
        if (evt.getClickCount() == 2) {
          dialog.accepted = true;
          SwingBasics.closeWindow(dialog);
        }
      }
    });
    dialog.layout("offerChoices", msg, mainPanel, otherButton, helpUrl, false);
    GradientBasics.setTransparentChildren(mainPanel, true);
    SwingBasics.showUpFront(dialog, jl, setAlwaysOnTop);
    return dialog.accepted ? jl.getSelectedIndex() : -1;
  }

  public static boolean setAlwaysOnTop=false;

  public static boolean download(
      final String localFileName,
      final String localFolder,
      final String sourceUrlName) {
    return download(localFileName, localFolder, sourceUrlName, false, false, true);
  }

  public static boolean downloadAndUnzip(
      final String localFileName,
      final String localZipFolder,
      final String sourceUrlName) {
    return download(localFileName, localZipFolder, sourceUrlName, true, false, true);
  }

  public static boolean download(
      final String localFileName,
      final String localFolder,
      final String sourceUrlName,
      final boolean unzipping,
      final boolean junkZipFolders,
      final boolean overwrite) {
    IoBasics.mkDirs(localFolder);
    final String localPath = IoBasics.concat(localFolder, localFileName);
    final File localFile = new File(localPath);
    if (localFile.exists() && !PopupBasics.ask(
        "File already exists!",
        "Overwrite " + IoBasics.dirHtml(localFile) + "?", true)) {
      return false;
    }
    final String wndTitle = "Downloading " +
                            (unzipping ? " & unzipping..." : "");
    final SwingBasics.Dialog dialog = new SwingBasics.Dialog(wndTitle);
    final JPanel mainPanel = new JPanel(new BorderLayout(0, 1));

    final DefaultListModel dlm;
    final JProgressBar progressBar;
    final JList progressComments = new JList();
    dlm = new DefaultListModel();
    progressComments.setModel(dlm);
    final JScrollPane jsp = new JScrollPane(progressComments);
    progressBar = new JProgressBar(0, 1);
    final JPanel jp = new JPanel(new BorderLayout());
    jp.add(jsp, BorderLayout.CENTER);
    jp.add(progressBar, BorderLayout.SOUTH);
    mainPanel.add(jp, BorderLayout.CENTER);
    dialog.layout("downloadAndUnzip", sourceUrlName,
                  mainPanel, null, false);

    final ProgressUpdater pu = new ProgressUpdater() {
    	public boolean isCancelled() {
    		return false;
    	}
      public int getThresholdSize() {
        return IoBasics.CHUNK * 64;
      }


      public void report(final Condition.Annotated a) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dlm.addElement(a.annotation);
            if (a.condition == Condition.FINISHED) {
              dlm.addElement("Downloaded " + localFileName + " to " +
                             localFolder);
              dialog.ok.setEnabled(true);
            }
            if (a.condition == Condition.FATAL) {
              dlm.addElement("FAILED to download " + localFileName + " to " +
                             localFolder);
              SwingUtilities.invokeLater(
                  new Runnable() {
                public void run() {
                  dialog.cancel.doClick(4000);
                }
              });
            }
            int idx = dlm.getSize();
            progressComments.setSelectedIndex(idx - 1);
            progressComments.ensureIndexIsVisible(idx - 1);
          }
        }
        );
      }

      public void report(final String description, final int currentAmount,
                         final int tallySoFar, final int finalAmount) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (progressBar.getMaximum() < finalAmount) {
              progressBar.setMaximum(finalAmount);
            }
            if (tallySoFar >= 0) {
              final String s = (description == null ? "" : description + ":  ") +
                               Basics.encode(tallySoFar / 1000) +
                               " of " +
                               Basics.encode(finalAmount / 1000) +
                               "KB\n";
              dlm.addElement(s);
              int idx = dlm.getSize();
              progressComments.setSelectedIndex(idx - 1);
              progressComments.ensureIndexIsVisible(idx - 1);
              if (progressBar != null) {
                progressBar.setValue(tallySoFar);
              }
            }
          }
        }
        );
      }
    };

    if (unzipping) {
      // to pass-thru this method's parameters junkZipFolder, overWRite
      IoBasics.copyAndUnzipRecursivelyInTheBackground(
          localPath,
          localFolder,
          sourceUrlName,
          junkZipFolders,
          overwrite,
          pu);
    } else {
      IoBasics.copyInTheBackground(
          localPath,
          sourceUrlName,
          true,
          pu);

    }

    dialog.ok.setEnabled(false);
    SwingBasics.showUpFront(dialog, null, setAlwaysOnTop);
    return dialog.accepted;
  }

  public static void main(final String[] args) {
	  SwingBasics.resetDefaultFonts();
	  PersonalizableTableModel.initGlobalProperties();
	  Pel.init(null, PopupBasics.class, null, false);
	    Basics.gui = PopupBasics.gui;
	final String txt=Basics.encodeHtml("<html><img src='dog'>You are > than me</html>", true);
    final String msg=Basics.toUlHtml("ol", Basics.toList(new String[]{Basics.convertToCharNumbering(0)+". "+txt,Basics.convertToCharNumbering(1)+".  I am < than you"}), true);
    SwingBasics.showHtml("tmp", Basics.toHtmlErrorUncentered("Here is the result", msg), false);
    if (ask("Proceed with tests?")) {
    	int idx2=getRadioButtonOption("Radio ", new String[] {"one", "two"},1,true);
    	final StringBuilder sb=new StringBuilder("<html>Which?<table>");
        for (int i=0;i<42;i++){
      	  sb.append("<tr><td>This is ine # "+i+"</td></tr>");
        }
        sb.append("</table></html>");
        System.out.println(getChosenIndex(sb.toString(), new String[] {"one", "two",
                                          "three"}
                                          , 2));

        System.out.println(getChosenIndex(sb.toString(), new String[] {"one", "two",
        "three"}
        , 2, true));

      System.out.println(getStringFromUser("Test string", "stephen"));
      System.out.println(getDateFromUser(null, "Date", "What date", null, null));
      System.out.println(getChosenIndex("Which?", new String[] {"one", "two",
                                        "three"}
                                        , 2));
      System.out.println(getChosenIndex("Which?", new String[] {"one", "two",
      "three"}
      , 2, true));
System.out.println(getFloatFromUser("How much?", new Float(44)));
      System.out.println(getValueFromUser(null, "Dog", "Input required", "Fergus",
                                          new String[] {"German shepard",
                                          "Golden retrieve", "Pooch",
                                          "Weiner dog"}
                                          , 
                                          DefaultStringConverters.get(),
                                          1, false, -1));
    }
    System.exit(0);
  }
  

  public static Object[] reorganizeList(final Component c, final String title, final String msg,
			final JButton otherClosingButton, final int defaultChoice, final Object[] choices, final String[] toolTips,
			final boolean putTipInPane, final String helpUrl) {
	  final Window owner = SwingUtilities.getWindowAncestor(c);
		final SwingBasics.Dialog dialog = owner instanceof JFrame ? new SwingBasics.Dialog((JFrame) owner, title)
				: owner instanceof JDialog ? new SwingBasics.Dialog((JDialog) owner, title) : new SwingBasics.Dialog(
						title);

		final JPanel mainPanel = new JPanel(new BorderLayout(0, 1));
		final JList jl = new JList(choices) {
			public String getToolTipText(final MouseEvent event) {
				if (toolTips == null || putTipInPane) {
					return super.getToolTipText(event);
				}
				final Point p = event.getPoint();
				final int location = locationToIndex(p);
				final String tip = toolTips[location];
				return tip;
			}
		};
		
		final JButton top = new SwingBasics.ImageButton(MmsIcons.getTopIcon());
		top.setToolTipText(Basics.toHtmlUncentered("Move selected item(s) to top of display order"));
		ActionListener anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(jl, ArrayBasics.Direction.TOP);
			}
		};
		SwingBasics.echoAction(jl, top, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_MASK), 't');

		final JButton up = new SwingBasics.ImageButton(MmsIcons.getUpIcon());
		up.setToolTipText(Basics.toHtmlUncentered("Move selected item(s) towards start of display order"));
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(jl, ArrayBasics.Direction.UP);
			}
		};
		SwingBasics.echoAction(jl, up, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK), 'u');

		final JButton down = new SwingBasics.ImageButton(MmsIcons.getDownIcon());
		down.setToolTipText(Basics.toHtmlUncentered("Move selected item towards start of display order"));
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(jl, ArrayBasics.Direction.DOWN);
			}
		};
		SwingBasics.echoAction(jl, down, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK), 'd');

		final JButton bottom = new SwingBasics.ImageButton(MmsIcons.getBottomIcon());
		bottom.setToolTipText(Basics.toHtmlUncentered("Move selected item(s) to bottom of display order"));
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(jl, ArrayBasics.Direction.BOTTOM);
			}
		};
		SwingBasics
				.echoAction(jl, bottom, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.ALT_MASK), 'b');

		if (putTipInPane) {
			final JTextPane jtp = new JTextPane();
			jtp.setContentType("text/html");
			jtp.setMargin(new java.awt.Insets(3, 5, 3, 3));
			jl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					final int idx = jl.getSelectedIndex();
					if (idx >= 0) {
						jtp.setText(toolTips[idx]);
						jtp.setCaretPosition(0);
					}
				}
			});
			final JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, new JScrollPane(jl),
					new JScrollPane(jtp));
			jsp.setResizeWeight(0.33);
			mainPanel.add(jsp, BorderLayout.CENTER);
		} else {
			mainPanel.add(new JScrollPane(jl), BorderLayout.CENTER);
		}
		
		jl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					int[] indices=jl.getSelectedIndices();
					for(int i=0;i<indices.length;i++){
						if(indices[i]==0){
							up.setEnabled(false);
							top.setEnabled(false);
							break;
						}else{
							up.setEnabled(true);
							top.setEnabled(true);
						}
						
						
					}
					for(int i=0;i<indices.length;i++){
						if(indices[i]==jl.getModel().getSize()-1){
							down.setEnabled(false);
							bottom.setEnabled(false);
							break;
						}else{
							down.setEnabled(true);
							bottom.setEnabled(true);
						}
						
						
					}
				}
			});
		final JPanel east = new JPanel(new BorderLayout());
		final JPanel eastNorth = new JPanel(new GridLayout(2, 1));
		eastNorth.add(top);
		eastNorth.add(up);
		east.add(eastNorth, BorderLayout.NORTH);

		final JPanel eastSouth = new JPanel(new GridLayout(2, 1));
		eastSouth.add(down);
		eastSouth.add(bottom);
		east.add(eastSouth, BorderLayout.SOUTH);

		mainPanel.add(east, BorderLayout.EAST);
		if (defaultChoice >= 0) {
			jl.setSelectionInterval(defaultChoice, defaultChoice);
		}
		jl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					dialog.accepted = true;
					SwingBasics.closeWindow(dialog);
				}
			}
		});
		if (otherClosingButton != null) {
			otherClosingButton.addActionListener(SwingBasics.getCloseAction(dialog));
		}
		dialog.layout("reorganizeList", msg, mainPanel, otherClosingButton, helpUrl, false);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				jl.requestFocus();
			}
		});
		SwingBasics.showUpFront(dialog, dialog.ok, setAlwaysOnTop);
		final Object[] values;
		if (dialog.accepted) {
			values = SwingBasics.getDataList(jl);
		} else {
			values = null;
		}
		return values;
	}
  
  private static JPanel getOkCancelPanel(
		  final JDialog dlg, 
		  final int optionPaneMessageType, 
		  final boolean addCancel, 
		  final ActionListener okAction,
		  final JComponent otherBottomRightComponent){
		final JPanel jp = SwingBasics.getButtonPanel(2);
		JPanel okCancelPanel=null;
		if (otherBottomRightComponent != null && bottomRightComponentIsLeft){
			jp.add(otherBottomRightComponent);
			okCancelPanel=new JPanel(new GridLayout(1, 2));
			jp.add(okCancelPanel);
		}
		else {
			okCancelPanel=jp;
		}
		if (okAction != null) {
			final JButton b=SwingBasics.getDoneButton(dlg);
			b.setMnemonic('\0');
			b.addActionListener(okAction);
			b.setText(capitalizeOk?"OK":"Ok");
			okCancelPanel.add(b);
		}
		if (addCancel) {
			final JButton b=SwingBasics.getCancelButton(dlg, null, true);
			myCanceler=new Canceler();
			myCanceler.canceled=false;
			b.addActionListener(myCanceler);
			okCancelPanel.add(b);
		}
		if (otherBottomRightComponent != null && !bottomRightComponentIsLeft){
			jp.add(otherBottomRightComponent);
		}
		final JPanel jp3 = new JPanel();
		jp3.add(jp);
		final JPanel jp2 = new JPanel(new BorderLayout());
		jp2.add(jp3, BorderLayout.EAST);
		final JLabel iconLabel = new JLabel(
				getIcon(optionPaneMessageType));
		jp2.add(iconLabel, BorderLayout.WEST);
		return jp2;
  }
  private static JPanel getOkCancelPanel(
		  final JDialog dlg, 
		  final int optionPaneMessageType, 
		  final boolean addCancel, 
		  final ActionListener okAction,
		  final JComponent otherBottomRightComponent,
		  final Collection<JButton>additionalButtonsAtBottom){
		final JPanel jp = SwingBasics.getButtonPanel(2);
		JPanel okCancelPanel=jp;
		if (otherBottomRightComponent != null && bottomRightComponentIsLeft){
			jp.add(otherBottomRightComponent);
			okCancelPanel=new JPanel(new GridLayout(1, 2));
			jp.add(okCancelPanel);
		}
		if (okAction != null) {
			final JButton b=SwingBasics.getDoneButton(dlg);
			b.setMnemonic('\0');
			b.addActionListener(okAction);
			b.setText(capitalizeOk?"OK":"Ok");
			okCancelPanel.add(b);
		}
		if (addCancel) {
			final JButton b=SwingBasics.getCancelButton(dlg, null, true);
			myCanceler=new Canceler();
			myCanceler.canceled=false;
			b.addActionListener(myCanceler);
			okCancelPanel.add(b);
		}
		if(additionalButtonsAtBottom.size()>0){
			for (JButton button : additionalButtonsAtBottom) {
				okCancelPanel.add(button);
			}
		}
		if (otherBottomRightComponent != null && !bottomRightComponentIsLeft){
			jp.add(otherBottomRightComponent);
		}
		final JPanel jp3 = new JPanel();
		jp3.add(jp);
		final JPanel jp2 = new JPanel(new BorderLayout());
		jp2.add(jp3, BorderLayout.EAST);
		final JLabel iconLabel = new JLabel(
				getIcon(optionPaneMessageType));
		jp2.add(iconLabel, BorderLayout.WEST);
		return jp2;
  }
  
  public static interface DoubleClickResponder{
	  void respond(final Object clickedOn, final Component parent);
  }

  public static <T> Set<T> allowUserToSelectAndOrder(
		  final Component owner,
		  final String title,
		  final Collection<T> previouslySelectedAndOrderedObjects,
		  final Collection<T> unselectedAndDisOrderedObjects){
	  return allowUserToSelectAndOrder(owner, title, previouslySelectedAndOrderedObjects, unselectedAndDisOrderedObjects, 
			  null, null, null, null, null, null);
  }

  public static <T> Set<T> allowUserToSelectAndOrder(
		  final Component owner,
		  final String title,
		  final Collection<T> previouslySelectedAndOrderedObjects,
		  final Collection<T> unselectedAndDisOrderedObjects,
		  final JComponent atBottom,
		  final JComponent stuffToShowAboveSelected,
		  final JComponent stuffToShowAboveUnselected,
		  final ListCellRenderer lcrForSelected,
		  final ListCellRenderer lcrForUnselected,
		  final DoubleClickResponder doubleClickResponderForSelectList
		  ) {
	  final JPanel stuffToShowTheUserAtBottom=new JPanel();
		final Canceler canceler=new Canceler();	  	

		class ListModelDataWrapper {
			private Set set=new TreeSet();
			public Object stringToObject(String str){
				Iterator it=set.iterator();
				while(it.hasNext()){
					Object obj=it.next();
					if(obj.toString().equals(str)){
						return obj;
					}
				}
				
				return null;
			}
			public void addAll(Collection data){
				set.addAll(data);
			}
		}
		final Set<T> results = new LinkedHashSet<T> (previouslySelectedAndOrderedObjects);
		final Set<T> unorderedItems = new LinkedHashSet<T> (unselectedAndDisOrderedObjects);
		int n1=results.size(),n2=unorderedItems.size();
		int n=n1+n2;
		if (n<5){
			n=5;
		} else if (n>15){
			n=15;
		}
		DefaultListModel unOrderedModel=new DefaultListModel();
		
		final JList selectedAndOrderedList = new JList(unOrderedModel){
			public void setListData(final Object[] listData) {
				DefaultListModel model=new DefaultListModel();
				for(int index=0;index<listData.length;index++){
					model.addElement(listData[index]);
				}
		        setModel (model);
		     }
		};
		if (lcrForSelected != null){
			selectedAndOrderedList.setCellRenderer(lcrForSelected);
		}
		final ListModelDataWrapper dataWrapper=new ListModelDataWrapper();
		dataWrapper.addAll(results);
		dataWrapper.addAll(unorderedItems);
		DefaultListModel selectedAndOrderedModel=new DefaultListModel();
		Iterator it=results.iterator();
		while(it.hasNext()){
			unOrderedModel.addElement(it.next());
		}
		it=unorderedItems.iterator();
		while(it.hasNext()){
			selectedAndOrderedModel.addElement(it.next());
		}
		final JList unselectedList = new JList(selectedAndOrderedModel){
			public void setListData(final Object[] listData) {
				DefaultListModel model=new DefaultListModel();
				for(int index=0;index<listData.length;index++){
					model.addElement(listData[index]);
				}
		        setModel (model);
		     }
		};
		if (lcrForUnselected != null){
			unselectedList.setCellRenderer(lcrForUnselected);
		}
		unselectedList.setVisibleRowCount(n);
		unselectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		unselectedList.setDragEnabled(true);
		final JDialog myDialog;
		GradientBasics.Panel mainPanel = null;
        final DragSource ds=new DragSource();

				
		class ListDragGesture implements DragGestureListener,DragSourceListener{

			JComponent m_cmp=null;
			public ListDragGesture(JComponent cmp){
				m_cmp=cmp;
				DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(cmp,
				DnDConstants.ACTION_COPY, this);
			}
			public void dragDropEnd(DragSourceDropEvent dsde) {
			
			}

			public void dragEnter(DragSourceDragEvent dsde) {
				// TODO Auto-generated method stub
				
			}

			public void dragExit(DragSourceEvent dse) {
				// TODO Auto-generated method stub
				
			}

			public void dragOver(DragSourceDragEvent dsde) {
				// TODO Auto-generated method stub
				
			}

			public void dropActionChanged(DragSourceDragEvent dsde) {
				// TODO Auto-generated method stub
				
			}

			public void dragGestureRecognized(DragGestureEvent dge) {
				try{
					ds.startDrag(dge, DragSource.DefaultMoveDrop,				
						((StringTransferHandler) ((JList)m_cmp).getTransferHandler())
								.createTransferable(m_cmp), this);
					
				}
				catch(Exception e){
					
				}
				
			}
			
		}
		
		class ListTransferHandler extends StringTransferHandler {
			private int[] indices = null;
			private int addIndex = -1; // Location where items were added
			private int addCount = 0; // Number of items added.
		    
			public ListTransferHandler(){
				
			}
			// Bundle up the selected items in the list
			// as a single string, for export.
			protected String exportString(JComponent c) {
				JList source = (JList) c;
				DefaultListModel model=(DefaultListModel)source.getModel();
				indices = source.getSelectedIndices();
				 Object[] values = source.getSelectedValues();
			        
			        StringBuffer buff = new StringBuffer();

			        for (int i = 0; i < values.length; i++) {
			            Object val = values[i];
			            buff.append(val == null ? "" : val.toString());
			            if (i != values.length - 1) {
			                buff.append("\n");
			            }
			        }
			        
			        return buff.toString();
			}

			// Take the incoming string and wherever there is a
			// newline, break it into a separate item in the list.
			protected void importString(JComponent c, String str) {
				if(!(c instanceof JList) || str.trim().length()==0){
					return;
				}
								
				JList target = (JList) c;
				DefaultListModel listModel=(DefaultListModel)target.getModel();
				
				int index = target.getSelectedIndex()==-1? 0 : target.getSelectedIndex();

			        addIndex = index;
			        String[] values = str.split("\n");
			        addCount = values.length;
			        for (int i = 0; i < values.length; i++) {
			        	Object obj=dataWrapper.stringToObject(values[i]);
			            if(unselectedList.equals(target)){
							((DefaultListModel)selectedAndOrderedList.getModel()).removeElement(obj);
						}
						if(selectedAndOrderedList.equals(target)){
							((DefaultListModel)unselectedList.getModel()).removeElement(obj);
						}
						listModel.removeElement(obj);
						
						try{
							listModel.add(index++,obj);
						}catch(Exception e){
							listModel.add(listModel.getSize()-1,obj);
						}
			        }
				}

	
			protected void cleanup(JComponent c, boolean remove) {
		        if (remove && indices != null) {
		            JList source = (JList)c;
		            DefaultListModel model  = (DefaultListModel)source.getModel();
		            //If we are moving items around in the same list, we
		            //need to adjust the indices accordingly, since those
		            //after the insertion point have moved.
		            if (addCount > 0) {
		                for (int i = 0; i < indices.length; i++) {
		                    if (indices[i] > addIndex) {
		                        indices[i] += addCount;
		                    }
		                }
		            }
		           
		        }
		        indices = null;
		        addCount = 0;
		        addIndex = -1;
		    }
		}

		
		selectedAndOrderedList.setTransferHandler(new ListTransferHandler());
		ds.createDefaultDragGestureRecognizer(selectedAndOrderedList,
				DnDConstants.ACTION_MOVE, new ListDragGesture(selectedAndOrderedList));
		
		
		unselectedList.setTransferHandler(new ListTransferHandler());
		ds.createDefaultDragGestureRecognizer(unselectedList,
				DnDConstants.ACTION_MOVE, new ListDragGesture(unselectedList));
		
		JPanel moveButtonPanel = new JPanel(new BorderLayout());
		final JButton moveUp = new JButton(MmsIcons.getUpIcon());
		final JButton moveDown = new JButton(MmsIcons.getDownIcon());
		SwingBasics.addMouseOver(moveUp);
		SwingBasics.addMouseOver(moveDown);
		final AbstractAction unselectAction = new AbstractAction() {
			public void actionPerformed(final ActionEvent e) {
				final Object[] value = selectedAndOrderedList.getSelectedValues();
				for(int i=0;i<value.length;i++){
				((DefaultListModel)selectedAndOrderedList.getModel()).removeElement(value[i]);
				((DefaultListModel)unselectedList.getModel()).addElement(value[i]);
				}
				int[] indices=selectedAndOrderedList.getSelectedIndices();
				
				for(int i=0;i<indices.length;i++){
					if(indices[i]==0){
						moveUp.setEnabled(false);
					}
					if(indices[i]==selectedAndOrderedList.getModel().getSize()-1){
						moveDown.setEnabled(false);
					}
				}
			}
		};
		
		final JButton b2 = new JButton(unselectAction);
		SwingBasics.addMouseOver(b2);
		final ListSelectionListener selectAndOrderListSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (selectedAndOrderedList.getSelectedIndex() > -1) {
					b2.setEnabled(true);
				} else {
					b2.setEnabled(false);
				}
				moveUp.setEnabled(true);
				moveDown.setEnabled(true);
				int[] indices=selectedAndOrderedList.getSelectedIndices();
				for(int i=0;i<indices.length;i++){
					if(indices[i]==0){
						moveUp.setEnabled(false);
						//moveDown.setEnabled(true);
					}
					else if(indices[i]==selectedAndOrderedList.getModel().getSize()-1){
						moveDown.setEnabled(false);
						//moveUp.setEnabled(true);
					}
				}
				if(selectedAndOrderedList.getModel().getSize() <=1 || indices.length <= 0) {
					moveUp.setEnabled(false);
					moveDown.setEnabled(false);
				}
				else if(selectedAndOrderedList.getSelectedIndices().length == selectedAndOrderedList.getModel().getSize()) {
					moveUp.setEnabled(false);
					moveDown.setEnabled(false);
				}
			}
		};
		
		
		final AbstractAction selectAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				final Object[] value = unselectedList.getSelectedValues();
				for(int i=0;i<value.length;i++){
					((DefaultListModel)selectedAndOrderedList.getModel()).addElement(value[i]);
					((DefaultListModel)unselectedList.getModel()).removeElement(value[i]);
					selectAndOrderListSelectionListener.valueChanged(new ListSelectionEvent(selectedAndOrderedList,0,0,false));
				}
				int[] indices=selectedAndOrderedList.getSelectedIndices();
				
				for(int i=0;i<indices.length;i++){
					if(indices[i]==0){
						moveUp.setEnabled(false);
					}
					if(indices[i]==selectedAndOrderedList.getModel().getSize()-1){
						moveDown.setEnabled(false);
					}
				}
			}
		};

		
		final JButton top = new SwingBasics.ImageButton(MmsIcons.getTopIcon());
		ActionListener anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(selectedAndOrderedList, ArrayBasics.Direction.TOP);
				selectedAndOrderedList.requestFocus();
			}
		};
		SwingBasics.echoAction(selectedAndOrderedList, top, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_MASK), 't');

		final JButton bottom = new SwingBasics.ImageButton(MmsIcons.getBottomIcon());
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(selectedAndOrderedList, ArrayBasics.Direction.BOTTOM);
				selectedAndOrderedList.requestFocus();
			}
		};
		SwingBasics.echoAction(selectedAndOrderedList, bottom, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.ALT_MASK), 'b');

		
		moveUp.setMargin(new Insets(1, 2, 1, 2));
		moveDown.setMargin(new Insets(1, 2, 1, 2));
		
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(selectedAndOrderedList, ArrayBasics.Direction.UP);
				selectedAndOrderedList.requestFocus();
			}
		};
		SwingBasics.echoAction(selectedAndOrderedList, moveUp, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_MASK), 'u');

		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				SwingBasics.move(selectedAndOrderedList, ArrayBasics.Direction.DOWN);
				selectedAndOrderedList.requestFocus();
			}
		};
		SwingBasics.echoAction(selectedAndOrderedList, moveDown, anAction, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_MASK), 'd');

		moveButtonPanel.add(moveUp, "North");
		moveButtonPanel.add(moveDown, "South");
		if (mainPanel == null) {
			mainPanel = new GradientBasics.Panel(new BorderLayout());
			myDialog = getDialog(owner);
			myDialog.setModal(true);
			mainPanel.setOpaque(true);
			Box listsPanel = new Box(BoxLayout.LINE_AXIS);
			JScrollPane pane = new JScrollPane(unselectedList);

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(pane);
			final JPanel east=new JPanel(new BorderLayout());
			east.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
			if (stuffToShowAboveUnselected != null){
				east.add(stuffToShowAboveUnselected, BorderLayout.NORTH);
			} else {
				east.add(new Label("Unselected"), BorderLayout.NORTH);
			}
			east.add(panel, BorderLayout.CENTER);
			
			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));

			final JButton b1 = new JButton(selectAction);
			SwingBasics.addMouseOver(b1);
			if (unselectedList != null) {
				
				unselectedList.addMouseListener(new MouseAdapter() {
					
					
					public void mouseClicked(final MouseEvent evt) {
						if (evt.getClickCount() == 2) { // Double-click
							selectAction.actionPerformed(new ActionEvent(unselectedList,
									evt.getID(), "select"));
							
						}
						
						
					}
				});				
			}

			b1.setIcon(MmsIcons.getLeftIcon());
			if (unselectedList != null) {
				unselectedList
						.addListSelectionListener(new ListSelectionListener() {
							public void valueChanged(ListSelectionEvent evt) {
								if (unselectedList.getSelectedIndex() > -1) {
									b1.setEnabled(true);
								} else {
									b1.setEnabled(false);
								}
							}
						});
			}
			b1.setMargin(new Insets(1, 2, 1, 2));
			actionPanel.add(b1);
			selectedAndOrderedList.setVisibleRowCount(n);
			selectedAndOrderedList.setDragEnabled(true);
			selectedAndOrderedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			b1.setEnabled(false);
			
			if (selectedAndOrderedList != null) {
				if (doubleClickResponderForSelectList != null){
					selectedAndOrderedList.setToolTipText(Basics.toHtmlUncentered("More options exist ...",
							"Double click on any item in the <i>"+
							(stuffToShowAboveSelected instanceof JLabel ? ( (JLabel)stuffToShowAboveSelected).getText() : " left list " )+
							"</i><br>in order to access more options..."));
				}
				selectedAndOrderedList.addMouseListener(new MouseAdapter() {
					public void mouseEntered(final MouseEvent evt){
						if (doubleClickResponderForSelectList != null){
							ToolTipManager.sharedInstance().setEnabled(false);
							ToolTipOnDemand.getSingleton().showWithoutCancelButton(selectedAndOrderedList, false, evt.getX()+5, evt.getY()+5);							
						}
					}

					public void mouseExited(final MouseEvent evt){
						if (doubleClickResponderForSelectList != null){
							ToolTipManager.sharedInstance().setEnabled(true);
							ToolTipOnDemand.getSingleton().hideTipWindow();							
						}
					}
					
					public void mouseClicked(MouseEvent evt) {
						if (doubleClickResponderForSelectList != null){
							ToolTipManager.sharedInstance().setEnabled(true);
							ToolTipOnDemand.getSingleton().hideTipWindow();							
						}
						if (evt.getClickCount() == 2) { // Double-click
							if (doubleClickResponderForSelectList ==null){
							unselectAction.actionPerformed(new ActionEvent(selectedAndOrderedList,
									evt.getID(), "select"));
							} else {
								final int index=selectedAndOrderedList.locationToIndex(evt.getPoint());
								if (index>=0){
									doubleClickResponderForSelectList.respond(selectedAndOrderedList.getModel().getElementAt(index), selectedAndOrderedList);
									selectAndOrderListSelectionListener.valueChanged(new ListSelectionEvent(selectedAndOrderedList,0,0,false));
								}
							}
						}						
					}
				});				
			}		
			b2.setIcon(MmsIcons.getRightIcon());
			if (selectedAndOrderedList != null) {
				selectedAndOrderedList
						.addListSelectionListener(selectAndOrderListSelectionListener);
			}
			b2.setEnabled(false);
			b2.setMargin(new Insets(1, 2, 1, 2));
			actionPanel.add(b2);
			JScrollPane pane1 = new JScrollPane(selectedAndOrderedList);

			JPanel panel1 = new JPanel();
			panel1.setLayout(new BorderLayout());
			panel1.add(pane1);
			panel1.add(moveButtonPanel,BorderLayout.EAST);
			final JPanel west=new JPanel(new BorderLayout());
			west.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
			if (stuffToShowAboveSelected != null){
				west.add(stuffToShowAboveSelected, BorderLayout.NORTH);
			} else {
				west.add(new JLabel("Selected"), BorderLayout.NORTH);
			}
			west.add(panel1, BorderLayout.CENTER);
			listsPanel.add(west);
			listsPanel.add(actionPanel);
			listsPanel.add(east);
			
			// Add the lists panel to the overall panel
			mainPanel.add(listsPanel, BorderLayout.CENTER);
			final JPanel south=new JPanel(new BorderLayout());
			if (atBottom != null){
				stuffToShowTheUserAtBottom.add(atBottom);
			}
			south.add(stuffToShowTheUserAtBottom, BorderLayout.CENTER);
			JScrollPane southPane = new JScrollPane();
			southPane.setViewportView(south);
			
			final JButton cancel=SwingBasics.getCancelButton(myDialog, "Cancel all changes", true, canceler);
			final JButton done = SwingBasics.getDoneButton(myDialog, null, false);
			
			done.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				  canceler.canceled = false;	
				}
			});
			stuffToShowTheUserAtBottom.add(done);
			stuffToShowTheUserAtBottom.add(cancel);
			SwingBasics.registerEscape(myDialog, cancel);
			//south.add(b, BorderLayout.EAST);
			mainPanel.add(southPane, BorderLayout.SOUTH);

			// Add the button panel
			final JPanel buttonPanel = new JPanel();
			for (final AbstractButton btn : (Collection<AbstractButton>) Basics.UNMODIFIABLE_EMPTY_LIST) {
				buttonPanel.add(btn);
			}
			myDialog.setContentPane(mainPanel);
			myDialog.setTitle(title);
			SwingBasics.packAndPersonalize(myDialog, null, null,
					"subListResolver2", true, true, false);
			if (selectedAndOrderedList != null && selectedAndOrderedList.getSelectedIndex() < 0) {
				moveUp.setEnabled(false);
				moveDown.setEnabled(false);
			}		
			GradientBasics.setTransparentChildren(mainPanel, true);
			myDialog.setVisible(true);
			ToolTipManager.sharedInstance().setEnabled(true);

		}
		results.clear();
		for(int i=0;i<selectedAndOrderedList.getModel().getSize();i++) {
			results.add((T)selectedAndOrderedList.getModel().getElementAt(i));
		}
		
		
		return canceler.canceled ? null:results;
	
	}
  public static boolean forceBottomRight=false, relativeToParent=false;
  static JDialog getDialog(final Component locateRelativeTo) {
  	final JDialog dlg = SwingBasics.getDialog(locateRelativeTo);
  	if (relativeToParent && locateRelativeTo != null){
  		dlg.setLocationRelativeTo(locateRelativeTo);
  	}
  	return dlg;
  }

  public static Point location=null;
  private static class Canceler implements ActionListener{
		private boolean canceled=true;
		public void actionPerformed(final ActionEvent e){
			canceled=true;
		}
	};
private static Canceler myCanceler=null;



public static class GroupSelector {

	private JList actualList, possibleList;
	private JLabel actualLabel, possibleLabel;
	private Component parent;
	private JDialog myDialog;
	private Dimension listDimension = new Dimension(200, 100);
	private Dimension minListDimension = new Dimension(100, 50);

	private boolean onlyGroupSelectorComponent = false;
	public boolean cancelButtonPressed = false;
	public List<String> results = null;
	private GradientBasics.Panel mainPanel;
	private GenericListModel actualListModel, possibleListModel;

	public GroupSelector(Collection<String> actual,
			Collection<String> possible, String title, String actualTitle, String possibleTite, Component parent) {
		this.parent = parent;
		actualLabel = new JLabel(actualTitle);
		possibleLabel = new JLabel(possibleTite);
		init(actual, possible);
		results = (List) possible;
		myDialog.setContentPane(mainPanel);
		if (title != null) {
			myDialog.setTitle(title);
		}
		SwingBasics.packAndPersonalize(myDialog, null, null,
				"mapResolverWidget", true, true, false);

		myDialog.show();

	}
	
	public GroupSelector(Collection<String> actual,
			Collection<String> possible, String title, String actualTitle, String possibleTite, boolean onlyGroupSelectorComponent) {
		this.onlyGroupSelectorComponent = onlyGroupSelectorComponent;
		actualLabel = new JLabel(actualTitle);
		possibleLabel = new JLabel(possibleTite);
		init(actual, possible);
		results = (List) possible;
	}
	
	public JComponent getGroupSelector(){
		return mainPanel;
	}
	
	public List<String> getSelectionList(){
		Enumeration enums = possibleListModel.elements();
		results = null;
		while (enums.hasMoreElements()) {
			String group = (String) enums.nextElement();
			if (group != null) {
				if (results == null) {
					results = new ArrayList<String>();
				}
				results.add(group);
			}
		}
		return results;
	}

	AbstractAction actualize = new AbstractAction(null, MmsIcons
			.getRightIcon()) {
		public void actionPerformed(final ActionEvent e) {
			final Object value = possibleList.getSelectedValue();
			swapValues("actual", value);
			// setEnabled(false);
		}
	}, deactualize = new AbstractAction(null, MmsIcons.getLeftIcon()) {
		public void actionPerformed(ActionEvent e) {
			final Object value = actualList.getSelectedValue();

			swapValues("possible", value);
			// setEnabled(false);
		}
	};

	private void swapValues(String typeToAddTo, Object value) {
		if (value != null && !value.equals("")) {
			if (typeToAddTo == "actual") {
				actualListModel.add(actualListModel.size(), value);
				possibleListModel.removeElement(value);
				actualize.setEnabled(false);
			} else {
				possibleListModel.add(possibleListModel.size(), value);
				actualListModel.removeElement(value);
				deactualize.setEnabled(false);
			}
		}
	}

	private void initGui() {
		mainPanel = new GradientBasics.Panel(new BorderLayout());
		myDialog = getDialog(parent);
		myDialog.setModal(true);
		
	}

	public GenericListModel getListModel(final Collection c) {
		GenericListModel listModel = new GenericListModel();
		if (c != null) {
			listModel.addAll(c);
		}
		return listModel;
	}

	private JList getList(final GenericListModel listModel,
			final int visibleRowCount) {
		final JList list = new JList(listModel);
		list.setVisibleRowCount(visibleRowCount);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return list;
	}

	private JScrollPane getScrollableList(final JList list) {
		return new JScrollPane(list);
	}

	private JPanel getPanel(final JLabel label, final JComponent component) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 6));
		component.setPreferredSize(listDimension);
		component.setMinimumSize(minListDimension);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		// label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.add(label, BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	private JButton getActionButton(final JList targetList,
			final AbstractAction action) {
		final JButton b = new JButton(action);
		SwingBasics.addMouseOver(b);
		if (targetList != null) {
			targetList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					if (evt.getClickCount() == 2) { // Double-click
						action.actionPerformed(new ActionEvent(targetList,
								evt.getID(), "select"));
					}

				}
			});
			targetList
					.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent evt) {
							if (targetList.getSelectedIndex() > -1) {
								b.setEnabled(true);
							} else {
								b.setEnabled(false);
							}
						}
					});
		}
		return b;
	}

	protected Collection<AbstractButton> getButtons() {
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	private void init(Collection actual, Collection possible) {
		if (mainPanel == null) {
			initGui();
			Box listsPanel = new Box(BoxLayout.LINE_AXIS);
			actualListModel = getListModel(actual);
			int n1=possible.size(),n2=actual.size();
			int n=n1>n2?n1:n2;
			if (n<5){
				n=5;
			} else if (n>15){
				n=15;
			}
			actualList = getList(actualListModel, n);

			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new BoxLayout(actionPanel,
					BoxLayout.Y_AXIS));
			JButton b = getActionButton(actualList, deactualize);
			b.setMargin(new Insets(1, 2, 1, 2));
			actionPanel.add(b);
			possibleListModel = getListModel(possible);
			possibleList = getList(possibleListModel, n);
			b.setEnabled(false);
			b = getActionButton(possibleList, actualize);
			b.setEnabled(false);
			b.setMargin(new Insets(1, 2, 1, 2));
			actionPanel.add(b);
			JPanel panel = null;

			panel = getPanel(possibleLabel, getScrollableList(possibleList));
			listsPanel.add(panel);
			listsPanel.add(actionPanel);
			panel = getPanel(actualLabel, getScrollableList(actualList));
			listsPanel.add(panel);

			// Add the lists panel to the overall panel
			mainPanel.add(listsPanel, BorderLayout.CENTER);

			// Add the title panel
			panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			mainPanel.add(panel, BorderLayout.NORTH);

			if(!onlyGroupSelectorComponent){
			// Add the button panel
			final JPanel buttonPanel = new JPanel();
			for (final AbstractButton btn : getButtons()) {
				buttonPanel.add(btn);
			}
			final JButton ok = SwingBasics.getDoneButton(myDialog);
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Enumeration enums = possibleListModel.elements();
					results = null;
					while (enums.hasMoreElements()) {
						String group = (String) enums.nextElement();
						if (group != null) {
							if (results == null) {
								results = new ArrayList<String>();
							}
							results.add(group);
						}
					}
				}
			});
			ok.setText("Save");
			buttonPanel.add(ok);
			buttonPanel.add(SwingBasics.getCancelButton(myDialog,
					"Press to not change what is showing.", true,
					new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							cancelButtonPressed = true;
						}
					}));

			SwingBasics.layout(false, mainPanel, (Icon) null,
					(JLabel) null, (JPanel) null, buttonPanel);
			}else{
				SwingBasics.layout(false, mainPanel, (Icon) null,
						(JLabel) null, (JPanel) null, null);
			}
		}
	}
}

public static JPopupMenu popup(final JMenu menu){
	JPopupMenu popupMenu;
		popupMenu=new JPopupMenu(){
		    protected void paintComponent( Graphics g ){
		        if ( !isOpaque( ) )
		        {
		            super.paintComponent( g );
		            return;
		        }
		     
		        GradientBasics.paint(this, true, g);
		     
		        setOpaque( false );
		        super.paintComponent( g );
		        setOpaque( true );
		    }};
		final boolean doMouseOver=!SwingBasics.isPlasticLookAndFeel();
		final Component []components=menu.getMenuComponents();
		for (final Component component:components){
			if (component instanceof JMenuItem){
				SwingBasics.addMouseOver((JMenuItem)component);
				popupMenu.add((JMenuItem)component);
				popupMenu.add((JMenuItem)component);
				((JMenuItem)component).addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						((JMenuItem)component).setOpaque(false);
						
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						((JMenuItem)component).setOpaque(true);
						
					}
				});
				
			} else if (component instanceof JComponent){
				((JComponent)component).setOpaque(false);
			}else{
				popupMenu.add(component);
			}				
		}
	
	final Window w=SwingUtilities.getWindowAncestor(menu);
	if (w != null){
		final Point p1=w.getLocationOnScreen(), p2=menu.getLocationOnScreen();
		final Point p=new Point(p2.x-p1.x, p2.y-p1.y);
		popupMenu.show(w, p.x+menu.getWidth(), p.y);
	}
	return popupMenu;
}

/*
public static boolean downloadMany(
	      final String localFolder,
	      final String []urls,
	      final String []localFileNames) {
	    IoBasics.mkDirs(localFolder);
	    for (int i=0;i<urls.length;i++){
	    final String localPath = IoBasics.concat(localFolder, localFileNames[i]);
	    final File localFile = new File(localPath);
	    if (localFile.exists() && !PopupBasics.ask(
	        "File already exists!",
	        "Overwrite " + IoBasics.dirHtml(localFile) + "?", true)) {
	      return false;
	    }
}
	    final String wndTitle = "Downloading ";
	    final SwingBasics.Dialog dialog = new SwingBasics.Dialog(wndTitle);
	    final JPanel mainPanel = new JPanel(new BorderLayout(0, 1));

	    final DefaultListModel dlm;
	    final JProgressBar progressBar;
	    final JList progressComments = new JList();
	    dlm = new DefaultListModel();
	    progressComments.setModel(dlm);
	    final JScrollPane jsp = new JScrollPane(progressComments);
	    progressBar = new JProgressBar(0, 1);
	    final JPanel jp = new JPanel(new BorderLayout());
	    jp.add(jsp, BorderLayout.CENTER);
	    jp.add(progressBar, BorderLayout.SOUTH);
	    mainPanel.add(jp, BorderLayout.CENTER);
	    dialog.layout("downloadMany", String.format("%4d url(s)", urls.length),
	                  mainPanel, null, false);

	    class Updater implements ProgressUpdater {
	    	String localFileName;
	    	public boolean isCancelled() {
	    		return false;
	    	}
	      public int getThresholdSize() {
	        return IoBasics.CHUNK * 64;
	      }


	      public void report(final Condition.Annotated a) {
	        SwingUtilities.invokeLater(new Runnable() {
	          public void run() {
	            dlm.addElement(a.annotation);
	            if (a.condition == Condition.FINISHED) {
	              dlm.addElement("Downloaded " + Updater.this.localFileName + " to " +
	                             localFolder);
	              dialog.ok.setEnabled(true);
	            }
	            if (a.condition == Condition.FATAL) {
	              dlm.addElement("FAILED to download " + Updater.this.localFileName + " to " +
	                             localFolder);
	              SwingUtilities.invokeLater(
	                  new Runnable() {
	                public void run() {
	                  dialog.cancel.doClick(4000);
	                }
	              });
	            }
	            int idx = dlm.getSize();
	            progressComments.setSelectedIndex(idx - 1);
	            progressComments.ensureIndexIsVisible(idx - 1);
	          }
	        }
	        );
	      }

	      public void report(final String description, final int currentAmount,
	                         final int tallySoFar, final int finalAmount) {
	        SwingUtilities.invokeLater(new Runnable() {
	          public void run() {
	            if (progressBar.getMaximum() < finalAmount) {
	              progressBar.setMaximum(finalAmount);
	            }
	            if (tallySoFar >= 0) {
	              final String s = (description == null ? "" : description + ":  ") +
	                               Basics.encode(tallySoFar / 1000) +
	                               " of " +
	                               Basics.encode(finalAmount / 1000) +
	                               "KB\n";
	              dlm.addElement(s);
	              int idx = dlm.getSize();
	              progressComments.setSelectedIndex(idx - 1);
	              progressComments.ensureIndexIsVisible(idx - 1);
	              if (progressBar != null) {
	                progressBar.setValue(tallySoFar);
	              }
	            }
	          }
	        }
	        );
	      }
	    };
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    copy(
                      targetLocalFileName, sourceUrl, pu);
                    if (indicateFinishedWhenDone && pu != null) {
                        pu.report(Condition.FINISHED.annotate("done"));
                    }

                } catch (final IOException e) {
                    Pel.log.warn(e);
                }
            }
        });
        thread.start();
	      IoBasics.copyInTheBackground(
	          localPath,
	          sourceUrlName,
	          true,
	          pu);


	    dialog.ok.setEnabled(false);
	    SwingBasics.showUpFront(dialog, null, setAlwaysOnTop);
	    return dialog.accepted;
	  }*/

}
