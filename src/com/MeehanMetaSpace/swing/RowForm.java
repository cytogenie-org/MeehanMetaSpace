package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.MeehanMetaSpace.ArraySetWithoutNulls;
import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.BooleanConvertor;
import com.MeehanMetaSpace.ComparableBoolean;
import com.MeehanMetaSpace.MapOfMany;

public class RowForm {
	private static final int MINIMUM_LENGTH_STRING=15, MINIMUM_LENGTH_INTEGER=5, MINIMUM_LENGTH_FLOAT=7, MINIMUM_LENGTH_URL=25;
	private boolean isWorkingWithMore;
	private final boolean supportDynamicDisabling;
	private final PersonalizableTableModel model;
	private final int[] moreDataColumnIndexes, lessDataColumnIndexes;
	private final List<Integer> renderOnlyColumnIndexes;
	private final MapOfMany<String, Integer>moreSection;
	
	public RowForm(final PersonalizableTableModel model, final Row row, final boolean supportDynamicDisabling,
			final boolean startWithMore, final int[] moreDataColumnIndexes, final int[] lessDataColumnIndexes, final List<Integer> renderOnlyColumnIndexes, final MapOfMany<String, Integer>moreSections, 
			final String title,
			final PersonalizableTableModel modelForLabelProperties,
			final String prefix, 
			final Collection<Integer> needsRedStar,
			final boolean showMandatoryFieldsOnly) {
		this.metaRow = model.getMetaRow();
		this.modelForLabelProperties=modelForLabelProperties;
		this.prefixForLabelProperty=prefix;
		this.row = row;
		this.model = model;
		this.moreDataColumnIndexes = moreDataColumnIndexes;
		this.lessDataColumnIndexes = lessDataColumnIndexes;
		this.renderOnlyColumnIndexes = renderOnlyColumnIndexes;
		this.supportDynamicDisabling = supportDynamicDisabling;
		this.moreSection=moreSections;
		this.needsRedStar=needsRedStar;
		this.showMandatoryFieldsOnly=showMandatoryFieldsOnly;
		setView(showMandatoryFieldsOnly?startWithMore:model.getProperty(PROPERTY_EDIT_MORE, startWithMore), true, title);
	}
	
	public Row getRow(){
		return row;
	}
	public final static String PROPERTY_EDIT_MORE="editMore"; 

	private JButton defaultButton=null;
	public void setDefaultButton(final JButton b) {
		defaultButton=b;
	}
	private void setView(final boolean more, final boolean initializing, final String title) {
		final int idx=getFocus();
		final JRootPane root = SwingUtilities.getRootPane(panel);
		panel.setPreferredSize(null);
		if (more) {
			if (lessButtonPanel != null) {
				panel.remove(lessButtonPanel);
				panel.remove(lessPanel);
				// dispose(lessFields);
			}
			if (moreButtonPanel == null) {
				moreButtonPanel = new JPanel(new BorderLayout());
				moreButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
				moreButtonPanel.add(SwingBasics.getButton("Minimal entry", MmsIcons.getLessIcon(), 'a', new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						setView(false, false, title);
						model.setProperty(PROPERTY_EDIT_MORE, false);
						model.notifyViewChanged();
						syncEnabling(moreFields, lessFields);
					}
				}, "Click to see the minimum set of fields"), BorderLayout.WEST);
				moreFields = newFields(moreDataColumnIndexes, lessFields);
			} else {
				setFields(moreFields);
			}
			if (moreSection==null) {
				morePanel = buildPanel(title, moreFields);
			} else {
				morePanel = buildPanel(moreFields, moreSection);
			}
			panel.add(morePanel, BorderLayout.CENTER);

			panel.add(moreButtonPanel, BorderLayout.SOUTH);
		} else {
			if (moreButtonPanel != null) {
				panel.remove(moreButtonPanel);
				panel.remove(morePanel);
				// dispose(moreFields);
			}
			if (lessButtonPanel == null) {
				lessButtonPanel = new JPanel(new BorderLayout());
				lessButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
				lessButtonPanel.add(SwingBasics.getButton("Add details", MmsIcons.getGreatIcon(), 'a', new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						setView(true, false, title);
						model.setProperty(PROPERTY_EDIT_MORE, true);
						model.notifyViewChanged();
						syncEnabling(lessFields, moreFields);
					}
				}, "Click to see all fields"), BorderLayout.WEST);
				lessFields = newFields(lessDataColumnIndexes, moreFields);
			} else {
				setFields(lessFields);
			}
			lessPanel = buildPanel(title, lessFields);
			panel.add(lessPanel, BorderLayout.CENTER);
			if (!showMandatoryFieldsOnly) {
				panel.add(lessButtonPanel, BorderLayout.SOUTH);				
			}
		}

		if (!initializing) {
			if (repack==null) {
				panel.updateUI();
			} else {
				panel.updateUI();
				repack.pack();
				SwingBasics.resizeIfBiggerThanScreen(repack);
			}
		}
		if (root != null && defaultButton != null) {
			root.setDefaultButton(defaultButton);
		}
			
		isWorkingWithMore = more;
		if (idx>=0) {
			int idx2=find(idx);
			if (idx2>=0) {
				getEditors()[idx2].requestFocus();
			} else {
				getEditors()[0].requestFocus();

			}
		}
		GradientBasics.setTransparentChildren(panel, true);
	}

	public javax.swing.JDialog repack;
	public boolean isWorkingWithMore() {
		return isWorkingWithMore;
	}

	private ColumnField[] moreFields, lessFields;

	public ColumnField[] getEditors() {
		return isWorkingWithMore ? moreFields : lessFields;
	}

	private final MetaRow metaRow;
	private final Row row;
	private final JPanel panel = new GradientBasics.Panel(new BorderLayout());

	public JPanel getPanel() {
		return panel;
	}

	private JPanel moreButtonPanel, lessButtonPanel;

	private JScrollPane morePanel, lessPanel;

	private ColumnField[] newFields(final int[] dataColumnIndexes, final ColumnField[] reuse) {
		currentlySettingOrCreating=this;
		ArrayList<Integer> finalDataColumnIndexes = new ArrayList<Integer>(); 
		for (int i = 0; i < dataColumnIndexes.length; i++) {
			if (!showMandatoryFieldsOnly || needsRedStar.contains(dataColumnIndexes[i])) {
				finalDataColumnIndexes.add(dataColumnIndexes[i]);
			}
		}
		final RowForm.ColumnField[] fields = new ColumnField[finalDataColumnIndexes.size()];
		int i=0;
		for (int dataColumnIndex: finalDataColumnIndexes) {			
			final int vi = getVisualIndex(reuse, dataColumnIndex);
			if (vi < 0) {
				final int n1 = model.getTable().getFont().getSize();
				final int n2 = model.getColumnWidth(dataColumnIndex);
				final int n3 = n2 / n1;
				int length = n3 < 1 ? 2 : n3 > 25 ? 25 : n3;
				
				new RowForm.ColumnField(fields, dataColumnIndex, i, length, supportDynamicDisabling);
			} else {
				reuse[vi].setFields(fields, i);
			}
			i++;
			
		}
		currentlySettingOrCreating=null;
		return fields;
	}

	private int toBeDispatched=0;
	public final class ColumnField {
		private JLabel label;
		private final int dataColumnIndex;
		private Collection allowedValues, unselectableValues;
		private Editor editor;
		private boolean allowDynamicDisabling = true;

		private void setEnabled(final boolean enabled) {
			if (allowDynamicDisabling) {
				editor.setEnabled(enabled);
			}
		}

		private void setAllowDynamicDisabling(final boolean allowDynamicDisabling) {
			this.allowDynamicDisabling = allowDynamicDisabling;

		}

		private void setValue(final Object value) {
			editor.setValue(value);
			paintCellAdvice(false);
		}

		public void requestFocus() {
			System.out.println(" -->  REQUESTING FOCUS FOR "+label.getText());
			editor.requestFocus();
		}

		private boolean isEnabled() {
			return editor.isEnabled();
		}

		private boolean supportDynamicDisabling;
		private JPanel fieldPanel;
		private int containerIdx;
		private ColumnField[] fields;
		private int visualIndex;
		

		private void setFields(final ColumnField[] _fields, final int _visualIndex) {
			this.fields = _fields;
			this.visualIndex = _visualIndex;
			fields[visualIndex] = this;
		}

		private ColumnField(final ColumnField[] _fields, final int dataColumnIndex, final int _visualIndex,
				final int fieldLength, final boolean supportDynamicDisabling) {
			setFields(_fields, _visualIndex);
			this.supportDynamicDisabling = supportDynamicDisabling;
			this.dataColumnIndex = dataColumnIndex;
			final String dateFormat = metaRow.getDateFormat(dataColumnIndex);
			final Object value = get(dataColumnIndex);
			if (value instanceof SelectableCell) {
				System.out.println("selectable cell");
				editor = new Editor.SelectableCellEditor((SelectableCell) value);
			} else if (dateFormat != null) {
				editor = new Editor.DateSpinner(new java.text.SimpleDateFormat(dateFormat), dateFormat);
			} else {
				final String decimalFormat = metaRow.getDecimalFormat(dataColumnIndex);
				if (decimalFormat != null) {
					editor = new Editor.Decimal(decimalFormat, metaRow.getDecimalClass(dataColumnIndex));
				} else {
					final String editMask = metaRow.getEditMask(dataColumnIndex);
					if (editMask != null) {
						editor = new Editor.Mask(editMask);
					} else {
						final Class cl = metaRow.getClass(dataColumnIndex);
						if (Boolean.class.isAssignableFrom(cl) || ComparableBoolean.class.isAssignableFrom(cl)
								|| BooleanConvertor.class.isAssignableFrom(cl)) {
							editor = new Editor.CheckBox();
							editor.addChangeListener(new ChangeListener() {
								public void stateChanged(ChangeEvent e) {
									currentlySettingOrCreating=RowForm.this;
									row.set(dataColumnIndex, getCurrentValue());
									currentlySettingOrCreating=null;
									resetFieldIfRowDiffers();
									savePriorEditorValue();
								}
							});

						} else {
							allowedValues = TableBasics.getAllowedValues(row, dataColumnIndex);
							if (!Basics.isEmpty(allowedValues)) {
								int l;
								if (fieldLength < MINIMUM_LENGTH_STRING) {
									l = MINIMUM_LENGTH_STRING;
								} else {
									l = fieldLength;
								}
								unselectableValues = row.getUnselectableValues(dataColumnIndex);
								final Collection forbidden=row.getForbiddenValues(dataColumnIndex);

								if (unselectableValues.size() > 0) {
									int debug = 2;
								}
								editor = new Editor.ComboBox(allowedValues, unselectableValues, forbidden, 
										metaRow.getStringConverter(dataColumnIndex), row.allowNewValue(dataColumnIndex),
										isCaseSensitive(dataColumnIndex), getFoundListener(dataColumnIndex),
										RowForm.this);
								editor.setPreferredWidth(l);
							} else {
								final int l;
								
								if (java.net.URL.class.equals(cl) ){
									l=MINIMUM_LENGTH_URL;
								} else  if (Integer.class.equals(cl)) {
										if (fieldLength<MINIMUM_LENGTH_INTEGER) {
											l=MINIMUM_LENGTH_INTEGER;
										} else {
										l=fieldLength;
										}
									} else  if (Float.class.equals(cl)) {
										if (fieldLength<MINIMUM_LENGTH_FLOAT) {
											l=MINIMUM_LENGTH_FLOAT;
										} else {
										l=fieldLength;
										}
									}else {
										if (fieldLength<MINIMUM_LENGTH_STRING) {
											l=MINIMUM_LENGTH_STRING;
										} else {
										l=fieldLength;
										}
									}								
								editor = new Editor.TextField(new JTextField(l), metaRow
										.getClass(dataColumnIndex));
							}
						}
					}
				}
			}

			if (!row.isEditable(dataColumnIndex) || !row.isActive()) {
				this.setEnabled(false);
			}
			paintCellAdvice(false);
			editor.setValue(value);
			editor.addFocusListener(new FocusAdapter() {
				public void focusGained(final FocusEvent e) {
					System.out.println("> Gaining focus for "+label.getText());
					if (nextFocusAfterChange>=0 && visualIndex!=nextFocusAfterChange){
						System.out.println("   ****  WHAT ?? visualIndex="+ visualIndex+", nextFocusAfterChange="+ nextFocusAfterChange);
					}
					currentFocus=visualIndex; 
					final boolean reset=resetAllowedValues();
					editor.setFocusBorder();
					label.setIcon(MmsIcons.getRightIcon());
					SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						currentlyGettingSelectedItem=RowForm.this;
						final Object o=row.get(dataColumnIndex);
						currentlyGettingSelectedItem=null;
						editor.setSelectedItem(o);
						scroll();
						editor.selectAll();
					
					}});
				}
				public void scroll() {
					final int nudge=23;
					final java.awt.Rectangle r=editor.getComponent().getParent().getBounds(null);
					if (r.y>nudge) {
						r.y-=nudge;
						r.height+=nudge;
					} else {
						r.height+=r.y;
						r.y=0;
					}
					fieldPanel.scrollRectToVisible(r);
				}
				public void focusLost(final FocusEvent e) {
					ColumnField.this.focusLost(e);
				}
			});
			// position forced tool tips to the right of the field.
			toolTipWidthOffset = editor.getComponent().getWidth() + 10;
			toolTipHeightOffset = 10;
		}

		private void focusLost(final FocusEvent e) {
			if (alreadyInFocusLost){
				return;
			}
			alreadyInFocusLost=true;
			final Component next;
			int nextVisualIdx=-1;
			if (e != null){
				next=e.getOppositeComponent();
				if (next != null){
					final Component parent=next.getParent();
					for (int i = 0; i < fields.length; i++) {
						if (fields[i] != null) {
							final Component c=fields[i].editor.getComponent();
							if (c==next || c==parent){
								nextVisualIdx=fields[i].visualIndex;
								System.out.println("OPPOSITE component="+fields[i].label.getText());
								break;
							}
						}
					}
				}
			} else {
				next=null;
			}
			editor.setNormalBorder();
			label.setIcon(MmsIcons.getBlankIcon());
			lastLost=visualIndex;
			for (int i = 0; i < fields.length; i++) {
				if (fields[i] != null) {
					fields[i].savePriorRowValue();
				}
			}
			Object currentValue = getCurrentValue();
			final boolean hasRowValueChanged = !Basics.equals(currentValue, priorRowValue);
			if (hasRowValueChanged) {
				if (!Basics.isEmpty(currentValue) || !Basics.isEmpty(priorRowValue)){
					currentlySettingOrCreating=RowForm.this;					
					row.set(dataColumnIndex, currentValue);
					resetFocus(currentFocus, nextVisualIdx);
					currentlySettingOrCreating=null;
				}else if (next != null && !next.isEnabled()){
					resetFocus(currentFocus, nextVisualIdx);
				}
			} else if (next != null && !next.isEnabled()){
				resetFocus(currentFocus, nextVisualIdx);
			}
			final boolean hasEditorValueChanged = !Basics.equals(currentValue, priorEditorValue);
			boolean changedFocus=false;
			for (int i = 0; i < fields.length; i++) {
				if (fields[i] != null) {
					boolean becameEditableWasReadOnly=fields[i].resetFieldIfRowDiffers();
					if (!changedFocus && becameEditableWasReadOnly && nextVisualIdx>=0){
						final ColumnField cf=fields[i];
						final Component c=fields[i].editor.getComponent();
						final int vi=fields[i].visualIndex;
						if ((vi<currentFocus && vi> nextVisualIdx)||(vi>currentFocus && vi < nextVisualIdx)){
							next.setFocusable(false);
							SwingUtilities.invokeLater(new Runnable() {public void run() {	
								cf.requestFocus();
								next.setFocusable(true);
							}});
							changedFocus=true;
						}
					}
					fields[i].savePriorEditorValue();							
				}
			}
			if (hasEditorValueChanged) {
				paintCellAdvice(true);
				final Object o = get(dataColumnIndex);
				if (!Basics.equals(o, currentValue)) {
					currentValue = o;
					editor.setValue(o);
				}
			} else { 
				// may have reset a prior problem?
				paintCellAdvice(false);				
			}
			alreadyInFocusLost=false;

		}
		
		private int toolTipWidthOffset, toolTipHeightOffset;

		private Object priorRowValue, priorEditorValue;
		private final CellAdvice cellAdvice = new CellAdvice();
		private Object get(final int dataColumnIndex){
			return RowForm.get(RowForm.this,row,dataColumnIndex);
		}
		private void savePriorRowValue() {
			priorRowValue = get(dataColumnIndex);
		}

		private void savePriorEditorValue() {
			priorEditorValue = getCurrentValue();
		}
		
		private boolean resetAllowedValues(){
			if (!Basics.isEmpty(allowedValues)) {
				final Collection values = TableBasics.getAllowedValues(row, dataColumnIndex);
				final Collection us = row.getUnselectableValues(dataColumnIndex);
				final Collection forbidden=row.getForbiddenValues(dataColumnIndex);
				if (!Basics.equals(allowedValues, values) || !Basics.equals(us, unselectableValues)) {
					System.out.print("   NEW   VALUES ... ");
					System.out.println(label.getText());
					allowedValues = values;
					unselectableValues = us;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (fieldPanel == null || editor instanceof Editor.ComboBox) {
								editor.setAllowedValues(values, unselectableValues, forbidden, get(dataColumnIndex),isCaseSensitive(dataColumnIndex));
							} else {
								fieldPanel.remove(containerIdx);
								editor = new Editor.ComboBox(
										allowedValues, 
										unselectableValues, 
										forbidden, 
										metaRow.getStringConverter(dataColumnIndex), 
										row.allowNewValue(dataColumnIndex),
										isCaseSensitive(dataColumnIndex),
										getFoundListener(dataColumnIndex),
										RowForm.this);
								final JPanel jp = new JPanel(new BorderLayout());
								jp.add(editor.getComponent(), BorderLayout.WEST);
								fieldPanel.add(jp, containerIdx);
							}
							//editor.requestFocus();
						}
					});
					return true;
				}	
			}
			return false;
		}

		private Object getCurrentValue() {
			return editor.getValue();
		}

		private boolean resetFieldIfRowDiffers() {
			final Object value = get(dataColumnIndex);
			if (!Basics.equals(priorRowValue, value)) {
				editor.setValue(value);
				paintCellAdvice(true);
			}
			/* Comment OUT the else if {} block below if you want the editor to SHOW "Not entered yet" when
			 * switching from virtual to non virtual mode.  If you uncomment this line then the user sees the
			 * unique generated value whose pattern is "virtual - {protege system id}"
			 * 
			 */
			/*else if ( (!Basics.isEmpty(editorValue) || !Basics.isEmpty(value)) && !Basics.equals(value, editorValue)){
				editor.setValue(value);
				paintCellAdvice(true);
			}*/
			if (supportDynamicDisabling) {
				final boolean was=isEnabled(), is=row.isEditable(dataColumnIndex) && row.isActive();
				setEnabled(is);
				if (is && !was){
					return true;
				}
			}
			return false;
		}

		private boolean doCellAdvice(final JComponent c){
			final boolean haveTip = row.setAdvice(dataColumnIndex, cellAdvice);
			final boolean wasOk=!PersonalizableTable.paintCellAdvice(c, haveTip, cellAdvice, false);
			if (wasOk) {
				c.setBorder(editor.border);
			} 
			editor.setToolTip(cellAdvice.toolTip);
			return wasOk;
		}

		
		private void paintCellAdvice(final boolean dispatchToolTip) {
			final JComponent c = editor.getComponent();
			if (!doCellAdvice(c) && dispatchToolTip && toBeDispatched==0){
				toBeDispatched++;
				final String toolTipText=c.getToolTipText();
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						c.setToolTipText(toolTipText);
						ToolTipOnDemand.getSingleton().show(c, false);
						toBeDispatched=0;
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
//								doCellAdvice(c);
							}
						});
					}
				});
			}
			
		}
	}

	public void setEnabled(final int dataColumnIndex, final boolean ok) {
		setEnabled(isWorkingWithMore ? moreFields : lessFields, dataColumnIndex, ok);
	}

	private static void setEnabled(final ColumnField[] ctf, final int dataColumnIndex, final boolean ok) {
		final int vi = getVisualIndex(ctf, dataColumnIndex);
		if (vi >= 0) {
			ctf[vi].setEnabled(ok);
			ctf[vi].setAllowDynamicDisabling(false);
		}
	}

	public void setDynamicEnabling(final int dataColumnIndex, final boolean ok) {
		setDynamicEnabling(isWorkingWithMore ? moreFields : lessFields, dataColumnIndex, ok);
	}

	private static void setDynamicEnabling(final ColumnField[] ctf, final int dataColumnIndex, final boolean ok) {
		final int vi = getVisualIndex(ctf, dataColumnIndex);
		if (vi >= 0) {
			ctf[vi].supportDynamicDisabling=ok;
		}
	}

	public void setValue(final int dataColumnIndex) {
		setValue(isWorkingWithMore ? moreFields : lessFields, dataColumnIndex, get(this, row, dataColumnIndex));
	}
	
	public void setValue(final int dataColumnIndex, final Object value) {
		setValue(isWorkingWithMore ? moreFields : lessFields, dataColumnIndex, value);
	}

	private static void setValue(final ColumnField[] ctf, final int dataColumnIndex, final Object value) {
		final int vi = getVisualIndex(ctf, dataColumnIndex);
		if (vi >= 0) {
			ctf[vi].setValue(value);
		}
	}
	
	public Object getValue(final int dataColumnIndex) {
		final ColumnField[] ctf = isWorkingWithMore ? moreFields : lessFields;
		final int vi = getVisualIndex(ctf, dataColumnIndex);
		if (vi >= 0) {
			return ctf[vi].getCurrentValue();
		}
		return null;
	}

	private void syncEnabling(final ColumnField[] from, final ColumnField[] to) {
		for (int i = 0; i < from.length; i++) {
			final int j = getVisualIndex(to, from[i].dataColumnIndex);
			if (j >= 0) {
				to[j].setEnabled(from[i].isEnabled());
			}
		}
	}

	public static int getVisualIndex(final ColumnField[] ctf, final int dataColumnIndex) {
		if (ctf != null) {
			for (int i = 0; i < ctf.length; i++) {
				if (ctf[i] != null && dataColumnIndex == ctf[i].dataColumnIndex) {
					return i;
				}
			}
		}
		return -1;
	}

	public JComponent getFirstAccessible() {
		return getFirstAccessible(isWorkingWithMore ? moreFields : lessFields);
	}

	private static JComponent getFirstAccessible(final ColumnField[] fields) {
		int i = 0;
		for (; i < fields.length; i++) {
			if (fields[i] != null && fields[i].isEnabled()) {
				return fields[i].editor._getComponent();
			}
		}
		return null;
	}

	public void setToolTipOffset(final int widthOffset, final int heightOffset) {
		setToolTipOffset(isWorkingWithMore ? moreFields : lessFields, widthOffset, heightOffset);
	}

	private static void setToolTipOffset(final ColumnField[] fields, final int widthOffset, final int heightOffset) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] != null) {
				fields[i].toolTipHeightOffset = heightOffset;
				fields[i].toolTipWidthOffset = widthOffset;
			}
		}
	}

	private final PersonalizableTableModel modelForLabelProperties;
	private final String prefixForLabelProperty;
	
	String getColumnLabel(final ColumnField field){
		final String s;
		if (modelForLabelProperties != null && prefixForLabelProperty != null) {;
			s=modelForLabelProperties.getColumnLabel(field.dataColumnIndex, prefixForLabelProperty);
		} else {
			s=model.getColumnLabel(field.dataColumnIndex);
		}
		final String suffix=":  ";
		return Basics.stripSimpleHtml(s)+suffix;
	}
	
	private final Collection<Integer> needsRedStar;
	private boolean showMandatoryFieldsOnly;
	private String getLabelPrefix(final ColumnField field) {
		return needsRedStar.contains(field.dataColumnIndex)?
				"<font color='red'>*&nbsp;</font>":
					"&nbsp;&nbsp;";
	}
	
	private JScrollPane buildPanel(final ColumnField[] fields, final MapOfMany<String, Integer> sections) {
		Map<Integer, String> map = sections.getKeyByValue();
		final ArraySetWithoutNulls<String> as = new ArraySetWithoutNulls<String>();
		int max=0;
		for (int i = 0; i < fields.length; i++) {
			final String s=getColumnLabel(fields[i]);
			if (s.length()>max) {
				max=s.length();
			}
			as.add(map.get(fields[i].dataColumnIndex));
		}
		int maxLabelWidth=0, maxLabelHeight=0, maxEditorHeight=0;
		final JLabel[]labels=new JLabel[fields.length];
		for (int i = 0; i < fields.length; i++) {
			final String s=getColumnLabel(fields[i]);
			labels[i]=new JLabel( Basics.concat("<html>", getLabelPrefix(fields[i]), s, "&nbsp;&nbsp;</html>"));
			labels[i].setHorizontalAlignment(JLabel.RIGHT);
			labels[i].setIcon(MmsIcons.getBlankIcon());
			labels[i].setHorizontalTextPosition(JLabel.RIGHT);
			fields[i].label=labels[i];
			Dimension d=labels[i].getPreferredSize();
			if (d.width>maxLabelWidth) {
				maxLabelWidth=d.width;
			}
			if (d.height>maxLabelHeight) {
				maxLabelHeight=d.height;
			}
			final JComponent jc = fields[i].editor.getComponent();
			d=jc.getPreferredSize();
			if (d.height>maxEditorHeight) {
				maxEditorHeight=d.height;
			}
		}
		// GridLayoutPlus is documented and free at
		// http://www.jhlabs.com/java/layout/
		final BasicGridLayout l = new BasicGridLayout(as.size(), 1);
		final JPanel jp = new JPanel(l);
		jp.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 4));

		int vi=0;
		for (final String sectionLabel : as) {
			final ArrayList<Integer> dcs = new ArrayList<Integer>();
			for (int i = 0; i < fields.length; i++) {
				final String s2 = map.get(fields[i].dataColumnIndex);
				if (Basics.equals(sectionLabel, s2)) {
					dcs.add(i);
				}
			}
			final GridLayoutPlus l2 = new GridLayoutPlus(dcs.size(), 2, 1, 3);
			//l2.setColWeight(1, 1);
			//l2.setFill(1);
			final JPanel sectionPanel=new JPanel(l2);
			if (!Basics.isEmpty(sectionLabel)) {				
				sectionPanel.setBorder(createTitledBorder(sectionLabel));			
			}
			for (final int i:dcs) {
				final JPanel jp3 = new JPanel(new BorderLayout());
				jp3.add(labels[i], BorderLayout.EAST);
				sectionPanel.add(jp3);
				Dimension d=labels[i].getPreferredSize();
				d.width=maxLabelWidth;
				d.height=maxLabelHeight;
				jp3.setPreferredSize(d);
				fields[i].fieldPanel = sectionPanel;
				final JComponent jc = fields[i].editor.getComponent();
				fields[i].visualIndex=vi++;
				d=jc.getPreferredSize();
				d.height=maxEditorHeight;
				jc.setPreferredSize(d);
				final JPanel jp2 = new JPanel(new BorderLayout());
				jp2.add(jc, BorderLayout.WEST);
				jp2.add(new JLabel("  "), BorderLayout.EAST);
				sectionPanel.add(jp2);
				fields[i].containerIdx = SwingBasics.indexOf(sectionPanel, jp2);;
			}
			jp.add(sectionPanel);
		}
		final JScrollPane jsp=new JScrollPane(jp);
		return jsp;
	}

	private JScrollPane buildPanel(final String title, final ColumnField[] fields) {
		// BasicGridLayout is documented and free at
		// http://www.jhlabs.com/java/layout/
		int max=0, maxEditorHeight=0;;
		int maxLabelWidth=0, maxLabelHeight=0;
		final JLabel[]labels=new JLabel[fields.length];

		for (int i = 0; i < fields.length; i++) {
			final String s=getColumnLabel(fields[i]);				
			labels[i]=new JLabel( Basics.concat("<html>", getLabelPrefix(fields[i]), s, "&nbsp;&nbsp;</html>"));
			labels[i].setHorizontalAlignment(JLabel.RIGHT);
			labels[i].setIcon(MmsIcons.getBlankIcon());
			labels[i].setHorizontalTextPosition(JLabel.RIGHT);
			fields[i].label=labels[i];

			Dimension d=labels[i].getPreferredSize();
			if (d.width>maxLabelWidth) {
				maxLabelWidth=d.width;
			}
			if (d.height>maxLabelHeight) {
				maxLabelHeight=d.height;
			}

			if (s.length()>max) {
				max=s.length();
			}
			final JComponent jc = fields[i].editor.getComponent();
			d=jc.getPreferredSize();
			if (d.height>maxEditorHeight) {
				maxEditorHeight=d.height;
			}
		}
		
		final GridLayoutPlus l = new GridLayoutPlus(fields.length, 2, 1, 3);
//		l.setColWeight(1, 1);
//		l.setFill(1);
		final JPanel jp = new JPanel(l);
		jp.setBorder(createTitledBorder(title));
		
		int idx;
		for (int i = 0; i < fields.length; i++) {
			final JPanel jp3 = new JPanel(new BorderLayout());
			jp3.add(labels[i], BorderLayout.EAST);
			jp.add(jp3);
			Dimension d=labels[i].getPreferredSize();
			d.width=maxLabelWidth;
			d.height=maxLabelHeight;
			jp3.setPreferredSize(d);

			fields[i].fieldPanel = jp;
			final JComponent jc = fields[i].editor.getComponent();
			d=jc.getPreferredSize();
			d.height=maxEditorHeight;
			jc.setPreferredSize(d);
			final JPanel jp2 = new JPanel(new BorderLayout());
			jp2.add(jc, BorderLayout.CENTER);
			jp2.add(new JLabel(" "), BorderLayout.WEST);
			d.width+=4;
			jp2.setPreferredSize(d);
			jp.add(jp2);
			idx = SwingBasics.indexOf(jp, jp2);
			fields[i].containerIdx = idx;
		}
		return new JScrollPane(jp);
	}

	private static void dispose(final ColumnField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			final JComponent c = fields[i].editor.getComponent();
			final java.awt.Container co = c.getParent();
			if (co != null) {
				co.remove(c);
			}

		}
	}

	private static void setFields(final ColumnField[] fields) {
		for (int i = 0; i < fields.length; i++) {
			fields[i].setFields(fields, i);
		}
	}
	
	
	public static TitledBorder createTitledBorder(final String title) {
		final Font f=UIManager.getFont("Label.font");
		final Font titleFont=new Font(f.getFontName(), Font.BOLD, f.getSize());
		return BorderFactory.createTitledBorder(
				BorderFactory.createBevelBorder(BevelBorder.LOWERED, SystemColor.activeCaptionBorder,SystemColor.window),  
				title, 
				TitledBorder.LEFT, 
				TitledBorder.DEFAULT_POSITION, 
				titleFont,
				SystemColor.controlText);
	}

	public int find(final int dataColumnIndex) {
		final ColumnField[]fields=getEditors();
		for (int i=0;i<fields.length;i++) {
			if (fields[i].dataColumnIndex==dataColumnIndex) {
				return i;
			}
		}
		return -1;
	}

	private int lastLost=-1, currentFocus=-1;
	public int getFocus() {
		final ColumnField[]fields=getEditors();
		if (fields != null && fields.length>lastLost && lastLost>=0) {
			return fields[lastLost].dataColumnIndex;
		}
		return -1;
	}
	
	public void looseFocus() {
		final ColumnField[]fields=getEditors();
		if (fields != null && fields.length>currentFocus && currentFocus>=0) {
			fields[getInternalIndex(currentFocus)].focusLost(null);
		}
	}
	int getInternalIndex(final int visualIndex){
		final ColumnField[]fields=getEditors();
		int next=0;
		for (int i=0;i<fields.length;i++){
			if (fields[i].visualIndex==visualIndex){
				return i;
			}
		}
		return -1;
	}
	private boolean alreadyInFocusLost=false;

	public void nextFocus() {
		final ColumnField[]fields=getEditors();
		if (fields != null && fields.length>currentFocus && currentFocus>=0 && !alreadyInFocusLost) {
			int idx=getInternalIndex(currentFocus);
			if (idx != -1) {
				currentFocus++;
				if (currentFocus==fields.length){
					currentFocus=0;
				}
				idx=getInternalIndex(currentFocus);
				if(idx<0){
					return;
				}
				if (supportDynamicDisabling) {
					fields[idx].setEnabled(
							row.isEditable(
									fields[idx].dataColumnIndex) && row.isActive());
				}
				fields[idx].requestFocus();	
			}
		}
	}
	
	private AutoComplete.FoundListener getFoundListener(final int dataColumnIndex) {
		final PersonalizableDataSource ds=model.getDataSource();
		return ds.getFoundListener(row, dataColumnIndex);
	}

	private boolean isCaseSensitive(final int dataColumnIndex) {
		final PersonalizableDataSource ds=model.getDataSource();
		return ds instanceof DefaultPersonalizableDataSource?
				((DefaultPersonalizableDataSource)ds).isCaseSensitive(row, dataColumnIndex):false;
	}
	private static Object get(final RowForm form, final Row row, final int dataColumnIndex){
		Object value=null;
		
		value=row.get(dataColumnIndex);
			
		if (!row.isEditable(dataColumnIndex) || form.renderOnlyColumnIndexes.contains(dataColumnIndex)){
			final Object r_value=row.getRenderOnlyValue(dataColumnIndex, false, false);
			if (r_value!=null){
				value=r_value;
			}
		}
		return value;
	}
	
	public static RowForm currentlySettingOrCreating, currentlyGettingSelectedItem;
	private void resetAllowed(final int dataColumnIndex){
		final int idx=find(dataColumnIndex);
		if (idx>=0) {
			getEditors()[idx].resetAllowedValues();
		}
	}
	
	public static void resetAllowedValues(final int dataColumnIndex){
		if (currentlySettingOrCreating != null){
			final int idx=currentlySettingOrCreating.find(dataColumnIndex);
			if (idx>=0) {
				currentlySettingOrCreating.getEditors()[idx].resetAllowedValues();
			}
		}
	}

	public void refresh() {
		final ColumnField[]fields=getEditors();
		if (fields != null && fields.length>currentFocus && currentFocus>=0) {
			final ColumnField cf=fields[getInternalIndex(currentFocus)];
			Object currentValue = cf.getCurrentValue();
			currentlySettingOrCreating=this;
			row.set(cf.dataColumnIndex, currentValue);
			cf.resetAllowedValues();
			currentlySettingOrCreating=null;
			cf.requestFocus();
		}
	}

	public void repaintCellAdvice() {
		final ColumnField[]fields=getEditors();
		if (fields != null && fields.length>currentFocus && currentFocus>=0) {
			for (ColumnField cf:fields){
				final JComponent c = cf.editor.getComponent();				
				cf.doCellAdvice(c);
				if (cf.visualIndex == currentFocus){
					ToolTipOnDemand.getSingleton().showLater(c);
				}
			}
		}
	}
	
	private int nextFocusAfterChange;
	private void resetFocus(final int priorVisualIdx, final int nextVisualIdx){
		nextFocusAfterChange=nextVisualIdx;
		if (nextVisualIdx<0){
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {public void run() {	
			SwingUtilities.invokeLater(new Runnable() {public void run() {	
				SwingUtilities.invokeLater(new Runnable() {public void run() {	
					final ColumnField []fields=getEditors();
					boolean forward=true;
					if (nextVisualIdx!=0 && fields.length > 2 && priorVisualIdx>nextVisualIdx){
						forward=false;
					}
					System.out.print("Reset focus, forward="+forward);
					for (int i = 0; i < fields.length; i++) {
						if (fields[i].visualIndex==nextFocusAfterChange){										
							while (!fields[i].isEnabled()){
								if (forward){
									nextFocusAfterChange++;
									if (nextFocusAfterChange==fields.length){
										nextFocusAfterChange=0;
									}
								} else {
									nextFocusAfterChange--;
									if (nextFocusAfterChange==0){
										nextFocusAfterChange=fields.length-1;
									}	
								}
								if (nextFocusAfterChange== priorVisualIdx){
									System.out.println("..YIKES...looping?");
									return;
								}
								i=getInternalIndex(nextFocusAfterChange);
							}
							fields[i].requestFocus();
							nextFocusAfterChange=-1;
							break;
						}
					}
			}});
				}});
		}});

	}
}
