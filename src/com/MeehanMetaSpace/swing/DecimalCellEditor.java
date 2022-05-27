

package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.*;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.math.*;
import java.util.EventObject;

public class DecimalCellEditor
	extends DefaultCellEditor
	implements TableCellEditor, PopupBasics.Input {
  public JComponent getVisualComponent(){

	return field;
  }
  public boolean requiresActionEchoing(){
  return false;
}

  public void setInputValue(final Object newValue){
        field.setValue(newValue);
        field.setFocusLostBehavior(JFormattedTextField.COMMIT);

  }
  public JComponent getEditorComponent(){
    return field;
  }
	final FormattedTextField field;
	final DecimalFormat decimalFormat;
	final String formatHelp;
	final Class cls;
	// Initializes the spinner.
	public DecimalCellEditor(final DecimalFormat decimalFormat,
							 final String formatHelp, final Class cls){
	  super(new FormattedTextField(new BigDecimal("0.0")));
	  this.field=(FormattedTextField)editorComponent;
	this.decimalFormat=decimalFormat;
	
	  this.formatHelp=formatHelp;
	  
	  final DefaultFormatter fmt=new NumberFormatter(decimalFormat);
	  fmt.setValueClass(field.getValue().getClass());
	  final DefaultFormatterFactory fmtFactory=new DefaultFormatterFactory(fmt,
		  fmt, fmt);
	  field.setFormatterFactory(fmtFactory);
	  field.setHorizontalAlignment(JTextField.RIGHT);
	  field.setColumns( decimalFormat.toPattern().length() );
	  this.cls=cls;
      super.delegate = new EditorDelegate() {
          public void setValue(final Object value) {
          	VoodooTableCell.startCellEditing(value, field, null);
          }
      };

	}

	public DecimalCellEditor(final String format, final Class cls) {
		this(new DecimalFormat(format), format, cls);
	}



	// Enables the editor only for double-clicks.
	public boolean isCellEditable(final EventObject evt) {
		if (evt instanceof MouseEvent) {
			return ( (MouseEvent) evt).getClickCount() >= 2;
		}
		return true;
	}

	public static Object coerce(final Class cls, final Object value){
		if (cls.equals(Integer.class)){
			return new Integer(( (Number ) value).intValue());
		} else if (cls.equals(Float.class)){
			return new Float( ( (Number) value).floatValue());
		} else if (cls.equals(Double.class)){
			return new Double(( ( Number ) value ).doubleValue());
		} else if (cls.equals(Long.class)){
			return new Long(( ( Number ) value ).longValue());
		}else if (cls.equals(Short.class)){
			return new Short(( ( Number ) value ).shortValue());
		}
		return value;
	}
	// Returns the current value.
	public Object getCellEditorValue() {
		try{
    		return getValue(field,cls);

		} catch (final Exception e){
			Pel.log.warn(e);
			PopupBasics.alert(toolTip(), true);
			return field.getPriorValue();
		}
	}

	String toolTip(){
		return "Enter a number with format " +formatHelp;
	}

    static Object getValue(final JFormattedTextField field, final Class cls) throws java.text.ParseException{
        final String s = field.getText().trim();
        if (s.startsWith("+")) {
            field.setText(s.substring(1).trim());
        } else if (Basics.isEmpty(s)){
            field.setText("0");
        } else {
        	field.setText(s.trim());
        }
        field.commitEdit();
        Object value = field.getValue();
        value = coerce(cls, value);
        return value;
    }

}




