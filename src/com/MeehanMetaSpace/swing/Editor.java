

package com.MeehanMetaSpace.swing;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import com.MeehanMetaSpace.*;

import java.text.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;


class DocumentChangedListener implements DocumentListener, ChangeListener {
    ChangeListener cl;
    DocumentChangedListener(ChangeListener cl) {
        this.cl = cl;
    }

    public void stateChanged(ChangeEvent ce) {
        cl.stateChanged(ce);
    }

    public void changedUpdate(DocumentEvent event) {
        stateChanged(new ChangeEvent(event.getDocument()));
    }

    public void insertUpdate(DocumentEvent event) {
        stateChanged(new ChangeEvent(event.getDocument()));
    }

    public void removeUpdate(DocumentEvent event) {
        stateChanged(new ChangeEvent(event.getDocument()));
    }
}


public abstract class Editor {
	static Border focusBorder=PersonalizableTable.BORDER_YELLOW	;
    Border border;
	public void setNormalBorder(){
		_getComponent().setBorder(border);
	}
	public void setFocusBorder(){
		border = _getComponent().getBorder();        
		_getComponent().setBorder(focusBorder);
	}
	
    boolean isDirty = false;
    void setAllowedValues(final Collection allowedValues, final Collection unselectableValues, final Collection forbidden, final Object current, final boolean isCaseSensitive) {
        setValue(current);
    }

    void init() {
    	addChangeListener(new ChangeListener() {
            public void stateChanged(final ChangeEvent event) {
                isDirty = true;
            }
        });
    }

    abstract void selectAll();

    public boolean isDirty() {
        return this.isDirty;
    }

    boolean firstGet = true;
    public JComponent getComponent() {
        final JComponent c = _getComponent();
        if (firstGet) {
            setToolTip();
            firstGet = false;
        }
        return c;
    }

    void setPreferredWidth(final int n){
        final JComponent c = _getComponent();
        final Dimension d=c.getPreferredSize();
        final int _n=c.getFont().getSize();
        c.setPreferredSize(new Dimension(_n*n, d.height));
    }

    public abstract void addChangeListener(ChangeListener al);

    public void setToolTip() {
        _getComponent().setToolTipText(getToolTip());
    }

    public void addFocusListener(FocusListener fl) {
        _getComponent().addFocusListener(fl);
    }

    protected abstract JComponent _getComponent();

    public void setEnabled(boolean b) {
        _getComponent().setEnabled(b);
    }

    public boolean isEnabled() {
        return _getComponent().isEnabled();
    }

    public void requestFocus() {
        _getComponent().requestFocus();
    }

    abstract public void setValue(Object value);
    public void setSelectedItem(Object value){
    	
    }

    abstract public Object getValue();

    abstract public String defaultToolTip();

    private String help = "";

    public void setToolTip(String help) {
        this.help = help;
        setToolTip();
    }

	static class DilutionTextField extends FormattedTextField {
		private boolean firstKey = true;

		private DilutionTextField(final MaskFormatter maskf) {
			super(maskf);
		}

		public void setValue(final Object value) {
			super.setValue(DilutionBasics.reformat((String) value));
		}

		public Object getValue() {
			return DilutionBasics.reformat((String) super.getValue());
		}

		void keyPressedAndDocUpdated(final char c) {
			if ("/0123456789.".indexOf(c) >= 0) {
				final int idx = getCaretPosition();
				final String s = getText();
				if (idx <= DilutionBasics.SEPERATOR_IDX) {
					if (c == DilutionBasics.SEPERATOR) {
						final String denominator = s.substring(
								DilutionBasics.SEPERATOR_IDX + 1, s.length());
						final String numerator = s.substring(0, idx);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setText(DilutionBasics.reformat(numerator
										+ DilutionBasics.SEPERATOR
										+ denominator));
								setCaretPosition(DilutionBasics.SEPERATOR_IDX + 1);
								select(
										DilutionBasics.SEPERATOR_IDX + 1,
										DilutionBasics.SEPERATOR_IDX
												+ 1
												+ DilutionBasics.denominatorMask
														.length());
							}
						});
					} else {
						if (firstKey) {
							final StringBuilder sb = new StringBuilder();
							// System.out.println("FIRST KEY  Handled");
							sb.append(c);
							for (int i = 1; i < DilutionBasics.SEPERATOR_IDX; i++) {
								sb.append(' ');
							}
							sb.append(DilutionBasics.SEPERATOR);
							final String txt;
							if (s.length() > DilutionBasics.SEPERATOR_IDX) {
								final String denominator = s.substring(
										DilutionBasics.SEPERATOR_IDX + 1, s
												.length());
								sb.append(denominator);
								txt = sb.toString();
							} else {
								txt = DilutionBasics.reformat(sb.toString());
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									setText(txt);

									final int n = getDocument().getLength();
									if (n < 2) {
										System.err.println("Document corrupt?");
									} else {
										setCaretPosition(1);
									}
								}
							});
						}
					}
					firstKey = false;
				}
			}
		}

	};

    public static FormattedTextField getField(final String format) {
        FormattedTextField mf = null;
        try {
            final MaskFormatter maskf = new MaskFormatter(format);
            if (format.equals(DilutionBasics.editMask)) {
                maskf.setValidCharacters("/1234567890. ,");
                // turn off tips
                ToolTipOnDemand.getSingleton().hideTipWindow();
                mf = new DilutionTextField(maskf);
            } else {
                mf = new FormattedTextField(maskf);
            }
        } catch (final ParseException pe) {
            Pel.log.warn(pe);
        }
        return mf;

    }

    public String getToolTip() {
        return Basics.isEmpty(help) ? defaultToolTip() : help;
    }

    public static class DateSpinner extends Editor {

        public String toString() {
            Object o = getValue();
            if (o instanceof String) {
                o = GmtFormat.parse((String) o);
            }
            return dateFormat.format((Date) o);
        }

        public final JSpinner spinner;

        public void addChangeListener(final ChangeListener al) {
            spinner.addChangeListener(al);
        }

        public void setToolTip() {
            getFocus().setToolTipText(getToolTip());
        }

        final DateFormat dateFormat;
        final String format;
        
        public DateSpinner(final String format, final int maxHeight) {
            this(new SimpleDateFormat(DateCellEditor.dflt(format)),
                 DateCellEditor.dflt(format), maxHeight);
        }

        // Initializes the spinner.
        public DateSpinner(final DateFormat df, final String formatHelp) {
        	this(df,formatHelp,-1);
        }
        
        public DateSpinner(final DateFormat df, final String formatHelp, final int maxHeight) {
            dateFormat = df;
            spinner = New(df, maxHeight);
            format = formatHelp;
            spinner.setToolTipText(getToolTip());
            final JFormattedTextField tf =
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            tf.setMargin(STANDARD_INSETS);  
            init();
        }

        public static JSpinner New(final DateFormat df) {
        	return New(df, -1);
        }
        public static JSpinner New(final DateFormat df, final int maxHeight) {
            final JSpinner spinner = new JSpinner(new SpinnerDateModel()){
            	public Dimension getPreferredSize() {
                    final Dimension d = super.getPreferredSize();
                    if (maxHeight>0){
                    	d.height=maxHeight;
                    }
                    return d;
                }
            };
            // Get the date formatter
            final JFormattedTextField tf =
              ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            final DefaultFormatterFactory factory =
              (DefaultFormatterFactory) tf.getFormatterFactory();
            final DateFormatter formatter = (DateFormatter) factory.
                                            getDefaultFormatter();

            formatter.setFormat(df);
            getFocus(spinner).addMouseListener(new CalPopup(spinner, df));
            return spinner;

        }

        public void addFocusListener(final FocusListener fl) {
            getFocus().addFocusListener(fl);
        }

        JComponent getFocus() {
            return getFocus(spinner);
        }

        public static JComponent getFocus(JSpinner spinner) {
            return ((JTextComponent) spinner.getEditor().getComponent(0));
        }

        boolean convertingStringToDate;
        boolean isEmpty = false;
        public void setValue(final Object value) {
            if (value == null) {
                isEmpty = true;
                spinner.setValue(new Date());
                final Color c = UIManager.getColor("TextField.background");
                final JComponent jc = spinner.getEditor();
                jc.setForeground(c);

            } else {
                if (isEmpty) {
                    final Color c = UIManager.getColor("TextField.foreground");
                    spinner.getEditor().setForeground(c);
                    isEmpty = false;
                }

                if (!(value instanceof java.util.Date)) {
                    convertingStringToDate = true;
                    if (value != null) {
                        try {
                            spinner.setValue(GmtFormat.parse(value.toString()));
                        } catch (java.lang.NumberFormatException nfe) {
                            Pel.log.warn(nfe);
                        }
                        isDirty = false;
                    }
                } else {
                    convertingStringToDate = false;
                    spinner.setValue(value);
                    isDirty = false;

                }
            }

        }

        public Object getValue() {
            final Object prev = spinner.getValue();
            try {
                spinner.commitEdit();
                Object value = spinner.getValue();
                if (convertingStringToDate) {
                    value = GmtFormat.format((Date) value);
                }
                return value;
            } catch (final ParseException pe) {
                //Pel.log.print("Parse exception in date spinner");
                Pel.log.warn(pe);
                PopupBasics.alertAsync(getToolTip(), true);
            }
            return prev;
        }

        public String defaultToolTip() {
            return "Enter a date with format " + format + "!  ";
        }

        protected JComponent _getComponent() {
            return spinner;
        }

        void selectAll() {
        }
    }


    public static class Decimal extends Editor {

        void selectAll() {
            field.selectAll();
        }

        final JFormattedTextField field;

        final DecimalFormat decimalFormat;
        final String format;
        final Class cls;

        public Decimal(final String format, final Class cls) {
            this(new DecimalFormat(format == null ? "##" : format),
                 format == null ? "##" : format, cls);
        }

        public void addChangeListener(final ChangeListener al) {
            field.getDocument().addDocumentListener(new DocumentChangedListener(
              al));
        }

        // Initializes the spinner.
        public Decimal(
          final DecimalFormat decimalFormat,
          final String formatHelp,
          final Class cls) {
            this.decimalFormat = decimalFormat;
            format = formatHelp;
            field = new JFormattedTextField(new java.math.BigDecimal("0.0"));
            final DefaultFormatter fmt = new NumberFormatter(decimalFormat);
            fmt.setValueClass(field.getValue().getClass());
            DefaultFormatterFactory fmtFactory = new DefaultFormatterFactory(
              fmt, 
              fmt,
              fmt);
            field.setFormatterFactory(fmtFactory);
            field.setToolTipText(getToolTip());
            field.setHorizontalAlignment(JTextField.RIGHT);
            this.cls = cls == null ? Integer.class : cls;
            init();
        }

        public void setValue(final Object value) {
            if (value != null) {
                field.setValue(value);
                isDirty = false;
            }
        }

        public Object getValue() {
        	final Object prev = field.getValue();
			try {
				return DecimalCellEditor.getValue(field, cls);
			} catch (final ParseException pe) {
				Pel.log.warn(pe);
				PopupBasics.alertAsync(getToolTip(), true);
			} catch (final java.lang.ClassCastException cce) {
				Pel.log.warn(cce);
				PopupBasics.alertAsync(getToolTip(), true);
			}
			return prev;
		}

        public String defaultToolTip() {
            return "Enter a number with format " + format + "!  ";
        }

        protected JComponent _getComponent() {
            return field;
        }
    }


    public static class Mask extends Editor {

        void selectAll() {
            field.selectAll();
        }

        public void addChangeListener(final ChangeListener al) {
            field.getDocument().addDocumentListener(new DocumentChangedListener(
              al));
        }

        final FormattedTextField field;
        final String format;

        
        public Mask(final String format) {
            this.format = format;
            field = getField(format);
            init();
        }

        // Initializes the spinner.
        public Mask(
        		final JFormattedTextField.AbstractFormatter f,
                final String format) {
            field = new FormattedTextField(f);
            this.format = format;
        }

        public void setValue(final Object value) {
            field.setValue(value);
            isDirty = false;
        }

        public Object getValue() {
            return getValue(format, field, getToolTip());
        }

        public static Object getValue(
          final String format,
          final FormattedTextField field,
          final String toolTip) {
            try {
                field.commitEdit();
                final Object value = field.getValue();
                if (format.equals(com.MeehanMetaSpace.DilutionBasics.editMask) &&
                    !DilutionBasics.isValid((String) value)) {
                    field.setBorder(PersonalizableTable.BORDER_ERROR_FOCUS);
                    field.setToolTipText(DilutionBasics.advice);
                }
                return value;
            } catch (Exception e) {
                Pel.log.warn(e);
                return field.getPriorValue();
                //Basics.alertAsync(toolTip,true);
            }
        }

        public String defaultToolTip() {
            return "Enter a string with format " + format;
        }

        protected JComponent _getComponent() {
            return this.field;
        }

    }


    public static class ComboBox extends Editor {
    	static Border defaultBorder=BorderFactory.createCompoundBorder(new LineBorder(Color.gray),BorderFactory.createEmptyBorder(1,2,1,2));
    	public void setFocusBorder(){    	    
    		_getComponent().setBorder(focusBorder);
    	}

        public void addChangeListener(final ChangeListener al) {
            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    al.stateChanged(new ChangeEvent(ComboBox.this));
                }
            });
        }

        void selectAll() {
            if (comboBox instanceof AutoComplete) {
                ((AutoComplete) comboBox).selectAll();
            }
        }

        private final JComboBox comboBox;
        void setAllowedValues(final Collection allowedValues, final Collection unselectableValues, final Collection forbidden, final Object current, final boolean isCaseSensitive) {
            comboBox.removeAllItems();
            if (comboBox instanceof AutoComplete) {
                ((AutoComplete) comboBox).setUnselectable(unselectableValues);
                ((AutoComplete) comboBox).setForbidden(forbidden);
            }
            for (final  Object o:allowedValues) {
            	comboBox.addItem(o);
            }
            if (comboBox instanceof AutoComplete) {
                //((AutoComplete) comboBox).reinit(autoCompleteListeners);
                ((AutoComplete) comboBox).setIsCaseSensitive(isCaseSensitive);
            }
            
            setValue(current);
        }

        ComboBox(
          final Collection c,
          final Collection unselectable,
          final Collection forbidden,
          final StringConverter sc,
          final boolean allowNew, 
          final boolean isCaseSensitive,
          final AutoComplete.FoundListener fl,
          final RowForm rowForm) {
        	super.border=defaultBorder;
            	final Object oa=Basics.getFirst(c);
                final AutoComplete ac=new AutoComplete(
                oa instanceof Object[] ? (Object[])oa:c.toArray(),
                  unselectable,
                  sc,
                  allowNew,
                  true);
                ac.ignoreDefaultButtonWhenFocussed();
                ac.setRowForm(rowForm);
                ac.setFoundListener(fl);
                ac.setForbidden(forbidden);
                comboBox = ac;
                ac.setIsCaseSensitive(isCaseSensitive);
                if (rowForm != null){
                	ac.addActionListener(new ActionListener() {
						
						public void actionPerformed(final ActionEvent e) {
							ac.showPopupAgainIfForbiddenOnRowForm(e);
							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									ac.showPopupAgainIfForbiddenOnRowForm(e);
								}
							});
						}
					});
                }
            init();
        }

        public void addChangeListener(final ActionListener al) {
            comboBox.addActionListener(al);
        }

        ArrayList<FocusListener> autoCompleteListeners = null;

        public void addFocusListener(final FocusListener _fl) {
            FocusListener fl = new FocusListener() {
                public void focusGained(FocusEvent fe) {
                    _fl.focusGained(fe);
                }

                public void focusLost(FocusEvent fe) {
                    _fl.focusLost(fe);
                    comboBox.setPopupVisible(false);
                }
            };
            ComboCellEditor.addFocusListener(comboBox, fl);
            if (comboBox instanceof AutoComplete) {
                ((AutoComplete) comboBox).addEditorFocusListener(fl);

                if (autoCompleteListeners == null) {
                    autoCompleteListeners = new ArrayList<FocusListener>();
                }
                autoCompleteListeners.add(fl);
            }
        }

        public void setSelectedItem(final Object item){
        	comboBox.setSelectedItem(item);
        }
        public void setValue(final Object value) {
			if (SwingBasics.indexOf(comboBox, value) >= 0) {
				comboBox.setSelectedItem(value);
			} else if (!Basics.isEmpty(value)) {
				comboBox.addItem(value);
				comboBox.setSelectedItem(value);
	        	if (SwingBasics.indexOf(comboBox, value)<0) {
	        		comboBox.setSelectedIndex(-1);
	        		if (comboBox instanceof AutoComplete) {
	                    ((AutoComplete) comboBox).clearText();
	        		}
	        	}
			} else {
				comboBox.setSelectedIndex(-1);
        		if (comboBox instanceof AutoComplete) {
                    ((AutoComplete) comboBox).clearText();
        		}
			}
			if (comboBox instanceof AutoComplete) {
                ((AutoComplete) comboBox).setDefaultItem(value);
    		}
			isDirty = false;
		}

        public Object getValue() {
            final Object item;
            if (comboBox instanceof AutoComplete) {
                item = ((AutoComplete) comboBox).getFinalValue();
                if (Basics.equals(item, ComboCellEditor.newItem)) {
                    return "";
                }
            } else {
                item = comboBox.getSelectedItem();
            }
            return item;
        }

        public String defaultToolTip() {
            return "Select an item for the drop down, or enter a new one";
        }

        protected JComponent _getComponent() {
            return comboBox;
        }
    }


    public static class TextField extends Editor {
        void selectAll() {
            field.selectAll();
        }

        final StringConverter sc;
        public void addChangeListener(final ChangeListener al) {
            field.getDocument().addDocumentListener(new DocumentChangedListener(
              al));
        }

        public final JTextField field;

        TextField(final JTextField txt, Class cl) {
            field = txt;
            sc = (StringConverter) DefaultStringConverters.get(cl);
            init();
        }

        public void addChangeListener(final ActionListener al) {
            field.addActionListener(al);
        }

        public void setValue(final Object value) {
            if (sc != null) {
                field.setText(sc.toString(value));
            } else {
                field.setText(value.toString());
            }
            field.setCaretPosition(0);
            isDirty = false;
        }

        public Object getValue() {
            String currentValue = field.getText();
            if (sc != null) {
                try {
                    return sc.toObject((String) currentValue);
                } catch (Exception ex) {
                    Pel.log.warn(ex);
                }
            }
            return currentValue;
        }

        public String defaultToolTip() {
            return "Enter a value ";
        }

        protected JComponent _getComponent() {
            return field;
        }

    }


    public static class CalPopup extends MouseAdapter {
        private final JSpinner spinner;
        private final DateFormat df;
        public CalPopup(final JSpinner spinner, final DateFormat df) {
            this.spinner = spinner;
            this.df = df;
        }

        public void mouseReleased(final MouseEvent e) {
            if (e.isPopupTrigger() || e.getClickCount() == 2) {
                popup();
            }
        }

        public void mousePressed(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup();
            }
        }

        public void popup() {
            final Object value = spinner.getValue();
            if (value instanceof Date) {
                final ShowCalendar d = new ShowCalendar(
                  SwingBasics.getFrame(spinner),
                  df.getCalendar(),
                  (Date) value);
                SwingBasics.locateNextTo(spinner, d);
                d.setVisible(true);
                try {
                    if (spinner.isEnabled()) {
                        spinner.setValue(d.rvalue);
                    }
                } catch (final Exception olde) {
                    Pel.log.warn(olde);
                }
            }

        }

    }

    final static class SelectableCellEditor extends Editor {
        public void addChangeListener(final ChangeListener al) {
            comboBox.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    al.stateChanged(new ChangeEvent(SelectableCellEditor.this));
                }
            });
        }

        void selectAll() {

        }

        private final JComboBox comboBox;
        private final SelectableCell selectableCell;
        SelectableCellEditor(
          final SelectableCell selectableCell) {
            this.selectableCell=selectableCell;
          this.comboBox=selectableCell.getComboBox(selectableCell.showAllAfterRefresh);
          init();
        }

        public void addChangeListener(final ActionListener al) {
            comboBox.addActionListener(al);
        }

        ArrayList<FocusListener> autoCompleteListeners = null;

        public void addFocusListener(final FocusListener _fl) {
            FocusListener fl = new FocusListener() {
                public void focusGained(final FocusEvent fe) {
                    _fl.focusGained(fe);

                }

                public void focusLost(final FocusEvent fe) {
                    _fl.focusLost(fe);
                    comboBox.setPopupVisible(false);
                    comboBox.setSelectedItem(selectableCell);


                }
            };
            ComboCellEditor.addFocusListener(comboBox, fl);
            if (comboBox instanceof AutoComplete) {
                ((AutoComplete) comboBox).addEditorFocusListener(fl);

                if (autoCompleteListeners == null) {
                    autoCompleteListeners = new ArrayList<FocusListener>();
                }
                autoCompleteListeners.add(fl);
            }
        }

        public void setValue(final Object value) {
            if (value instanceof SelectableCell.Item) {
                //System.out.println("debug:  executing " + value);
                ((SelectableCell.Item) value).executeCmd();
            }
            selectableCell.showAllAfterRefresh.actionPerformed(comboBox);
            isDirty = false;
        }

        public Object getValue() {
            return comboBox.getSelectedItem();
        }

        public String defaultToolTip() {
            return "Select an item for the drop down, or enter a new one";
        }

        protected JComponent _getComponent() {
            return comboBox;
        }
    }

    public static class CheckBox extends Editor {
        
        private final JCheckBox field=new JCheckBox();
        void selectAll() {        	
        }

        CheckBox() {
            init();
        }

        public void addChangeListener(final ChangeListener al) {
            field.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					al.stateChanged(null);
					
				}
            	
            });
        }

        public void setValue(final Object value) {
            if (value instanceof ComparableBoolean) {                
                field.setSelected(( (ComparableBoolean)value).booleanValue());
            }
            isDirty = false;
        }

        public Object getValue() {
        	ComparableBoolean currentValue = ComparableBoolean.valueOf(field.isSelected());            
            return currentValue;
        }

        public String defaultToolTip() {
            return "Click if true";
        }

        protected JComponent _getComponent() {
            return field;
        }

    }
    public final static Insets STANDARD_INSETS=new Insets(2,4,2,3);
}
