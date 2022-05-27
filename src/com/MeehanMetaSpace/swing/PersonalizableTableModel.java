/*=====================================================================
 PersonalizableTableModel.java
 Created by Stephen Meehan
 Copyright (c) 2002
 =====================================================================*/
package com.MeehanMetaSpace.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.MeehanMetaSpace.ArrayBasics;
import com.MeehanMetaSpace.ArraySetWithoutNulls;
import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.ComparableBoolean;
import com.MeehanMetaSpace.Counter;
import com.MeehanMetaSpace.DefaultStringConverters;
import com.MeehanMetaSpace.IoBasics;
import com.MeehanMetaSpace.MapOfMany;
import com.MeehanMetaSpace.Pel;
import com.MeehanMetaSpace.PropertiesBasics;
import com.MeehanMetaSpace.StringConverter;
import com.MeehanMetaSpace.swing.DefaultFilterable.Filter;
import com.MeehanMetaSpace.swing.GroupedDataSource.Node;
import com.MeehanMetaSpace.swing.MultiQueryFilter.QuerySet;






public final class PersonalizableTableModel extends DefaultTableModel implements
		TearAway.Item {
	private static PickChecker pickChecker;
	public static DeleteListener deleteListenerToIgnoreKBHierarchyBehavior = null;
	public boolean isMultiColumnEnabled = false;
	public int multiColumnSelectionCount = 0;
	private int exploreCellIndex=0;
	private boolean isExploreItemAddded=false;
	private JEditorPane filterMessagePane=null;
	public boolean showLimitedMenu=false;
	public static boolean hideSelectedMenu=true;
	public final static String MSG_NOTHING_IS_SELECTED = "No rows with data are selected";
	public static int deleteCount = 0;
	
	void updateMultiQueryText() {
		if (filterMessagePane != null) {
			String text = multiQuerySet.getText();
			filterMessagePane.setText(text);
			filterMessagePane.setToolTipText(text);
			if (filterMessagePane.isVisible()) {
				filterMessagePane.updateUI();
			}
		}
	}
	
	boolean isToolTipListenerSet=false;
	boolean isAddQueryReset=false;
	private String queryToolTip="Multiple filters were specified to form the query:<br><br> ";
	static class RowOptions {
		Color alternatingColor = null;
		Boolean useDittos = null;
	}

	void newTree() { // unsorting creates ZERO columns to sort which signals
		// the need for the tree builder GUI to
		// GroupedDataSource
		unsort();
		final FocusFreeze ff = new FocusFreeze();
		group();
		ff.restorePrevValue();
		notifyViewChanged();
	}

	private void newTree(final SortInfo[] si) {
		finishTreeBuilding();
		ungroupIfNecessary();
		showOneMomentDisplay();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sort(si);
				sort();
				group();
				hideOneMomentDisplay();
				notifyViewChanged();
			}
		});
	}

	class TreeShapes {
		private class TreeShape implements ActionListener {
			final String[] sortIdentifiers;
			final int[] sortOrder;
			final boolean[] ascending;

			private TreeShape() {
				final SortInfo[] si = getAllSortInfo();
				sortIdentifiers = new String[si.length];
				sortOrder = new int[si.length];
				ascending = new boolean[si.length];
				for (int i = 0; i < si.length; i++) {
					sortOrder[i] = si[i].dataColumnIndex;
					sortIdentifiers[i] = getDataColumnIdentifier(sortOrder[i]);
					ascending[i] = si[i].ascending;
				}
			}

			private TreeShape(final Properties properties, final int id) {
				final String prefix = getPropertyPrefix();
				String name = getPropertyName(prefix, propertyColumnIdentifiers
						+ id);
				final String s = properties.getProperty(name);
				final List<String> l = Basics.urlDecode(s);
				name = getPropertyName(prefix, propertyColumnIndexes + id);
				int[] a = PropertiesBasics.loadIntArray(properties, name);
				final ArrayList<Integer> li = new ArrayList<Integer>();
				li.add(a[0]);
				for (int i = 1; i < a.length; i++) {
					if (a[i] < metaRow.size()) {
						li.add(a[i]);
					} else {
						System.out.println("odd");
					}

				}
				a = new int[li.size()];
				int j = 0;
				for (final int ii : li) {
					a[j++] = ii;
				}

				sortOrder = new int[a.length - 1];
				sortIdentifiers = new String[a.length - 1];
				for (int i = 1; i < a.length; i++) {
					sortOrder[i - 1] = a[i];
					if (i >= 0 && i < l.size()) {
						sortIdentifiers[i - 1] = l.get(i - 1);
					} else {
						sortIdentifiers[i - 1] = getDataColumnIdentifier(sortOrder[i - 1]);

					}
				}
				name = getPropertyName(prefix, propertyAscending + id);
				ascending = PropertiesBasics.loadBooleanArray(properties, name);
			}

			private void saveProperties(final String prefix, final int id) {
				if (!Basics.isEmpty(sortOrder)) {
					PropertiesBasics.saveBooleanArray(properties,
							getPropertyName(prefix, propertyAscending + id),
							ascending);
					int[] a = new int[sortOrder.length + 1];
					a[0] = groupOption;
					for (int i = 0; i < sortOrder.length; i++) {
						a[i + 1] = sortOrder[i];
					}
					PropertiesBasics.saveIntArray(properties, getPropertyName(
							prefix, propertyColumnIndexes + id), a);
					properties.setProperty(getPropertyName(prefix,
							propertyColumnIdentifiers + id), Basics
							.urlEncode(sortIdentifiers));
				} else {
					properties.remove(getPropertyName(prefix, propertyAscending
							+ id));
					properties.remove(getPropertyName(prefix,
							propertyColumnIndexes + id));
				}
			}

			private SortInfo[] getSortInfo() {
				final SortInfo[] si = new SortInfo[sortOrder.length];
				for (int i = 0; i < sortOrder.length; i++) {
					si[i] = new SortInfo(metaRow, sortOrder[i], i + 1,
							ascending[i]);
				}
				return si;
			}

			public void actionPerformed(final ActionEvent event) {
				getUngroupedModel().newTree(getSortInfo());
			}

			public boolean equals(Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof TreeShape)) {
					return false;
				}
				final String s1 = toString(), s2 = o.toString();
				return s1.equals(s2);
			}

			public int hashCode() {
				return toString().hashCode();
			}

			private boolean isStillValid() {
				final int max = metaRow.size();
				final SortInfo[] si = getSortInfo();
				if (si.length > 0) {
					for (int i = 0; i < si.length; i++) {
						if (si[i].dataColumnIndex < 0
								|| si[i].dataColumnIndex >= max) {
							return false;
						}
						final String id1 = getDataColumnIdentifier(si[i].dataColumnIndex), id2 = sortIdentifiers[i];
						final String debug = singularKey;
						if (!Basics.equals(id1, id2)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}

			public String toString() {
				if (sortOrder.length > 0) {
					final StringBuilder sb = new StringBuilder("<html>");
					final SortInfo[] si = getSortInfo();
					for (int i = 0; i < si.length; i++) {
						if (i > 0) {
							sb.append(", ");
						}
						encode(sb, si[i], false);
					}
					sb.append("</html>");
					return sb.toString();
				}
				return "";
			}

			private JCheckBoxMenuItem getMenuItem() {
				final JCheckBoxMenuItem returnValue;
				if (!Basics.isEmpty(sortOrder)) {
					final String s = toString();
					returnValue = new JCheckBoxMenuItem(s);
				} else {
					returnValue = null;
				}
				return returnValue;
			}

			private KeyStroke k;

			void unregister(final JComponent component) {
				component.unregisterKeyboardAction(k);
			}

			private JMenuItem register(final JComponent c, final int id) {
				final JCheckBoxMenuItem menuItem = getMenuItem();
				if (menuItem != null) {
					k = KeyStroke.getKeyStroke(KeyEvent.VK_1
							+ (id == 9 ? -1 : id), keyMask);
					SwingBasics.echoAction(c, menuItem, this, k, '\0');

				}
				return menuItem;
			}
		}

		private final int keyMask;
		private final LinkedList<TreeShape> list = new LinkedList();
		private final String propertyColumnIndexes, propertyAscending,
				propertyColumnIdentifiers;
		private final boolean addAddRemoveMenuItems;

		private TreeShapes(final boolean addAddRemoveMenuItems,
				final int keyMask, final String propertyColumnIndexes,
				final String propertyColumnIdentifiers,
				final String propertyAscending) {
			this.addAddRemoveMenuItems = addAddRemoveMenuItems;
			this.keyMask = keyMask;
			this.propertyColumnIndexes = propertyColumnIndexes;
			this.propertyColumnIdentifiers = propertyColumnIdentifiers;
			this.propertyAscending = propertyAscending;
			final String prefix = getPropertyPrefix();
			for (int id = 0, i = 0; i < 10; i++) {
				final String name = getPropertyName(prefix,
						propertyColumnIndexes + i);
				final String propertyValue = properties.getProperty(name);
				if (!Basics.isEmpty(propertyValue)) {
					final TreeShape treeShape = new TreeShape(properties, id);
					if (treeShape.isStillValid()) {
						final String debug = treeShape.toString();
						final Object[] debug2 = list.toArray();
						if (!list.contains(treeShape)) {
							list.add(treeShape);
							id++;
						}
					} else {
						System.out.println(treeShape + " is invalid!");
					}
				} else {
					break;
				}
			}
		}

		private void remove(final int dataColumnIndex) {
			boolean changes = false;
			ArrayList<Integer> al = new ArrayList();
			final int n = list.size();
			for (int i = 0; i < n; i++) {
				final TreeShape treeShape = list.get(i);
				for (int j = 0; j < treeShape.sortOrder.length; j++) {
					if (treeShape.sortOrder[j] == dataColumnIndex) {
						al.add(i);
						changes = true;
					} else if (treeShape.sortOrder[j] > dataColumnIndex) {
						treeShape.sortOrder[j]--;
						changes = true;
					}
				}
			}
			if (changes) {
				for (int i = al.size() - 1; i >= 0; i--) {
					int ii = al.get(i);
					list.remove(ii);
				}
				save();
				notifyViewChanged();
			}
		}

		private void removeCurrentTree() {
			final TreeShape treeShape = new TreeShape();
			if (treeShape.sortOrder.length > 0) {
				final int idx = list.indexOf(treeShape);
				if (idx >= 0) {
					unregisterActions();
					list.remove(idx);
					for (int i = 0; i < menus.size(); i++) {
						final JMenu menu = menus.get(i);
						final JComponent component = components.get(i);
						registerActions(component, menu);
					}
					save();
					notifyViewChanged();
				}
			}
		}

		private void clear() {
			unregisterActions();
			list.clear();
			for (int i = 0; i < menus.size(); i++) {
				final JMenu menu = menus.get(i);
				final JComponent component = components.get(i);
				registerActions(component, menu);
			}
			save();
			notifyViewChanged();
		}

		private void save() {
			final int n = list.size() > 10 ? 10 : list.size();
			int i = 0;
			final String prefix = getPropertyPrefix();
			for (; i < n; i++) {
				final TreeShape treeShape = list.get(i);
				treeShape.saveProperties(prefix, i);
			}
			for (; i < 10; i++) {
				properties
						.remove(getPropertyName(prefix, propertyAscending + i));
				properties.remove(getPropertyName(prefix, propertyColumnIndexes
						+ i));
				properties.remove(getPropertyName(prefix,
						propertyColumnIdentifiers + i));
			}

		}

		private void unregisterActions() {
			for (int i = 0; i < list.size(); i++) {
				final TreeShape treeShape = list.get(i);
				for (final JComponent component : components) {
					treeShape.unregister(component);
				}
			}
		}

		void unregisterActions(final JComponent component, final JMenu menu) {
			for (int i = 0; i < list.size(); i++) {
				final TreeShape treeShape = list.get(i);
				treeShape.unregister(component);
			}
			menus.remove(menu);
			components.remove(component);
		}

		private final ArrayList<JMenu> menus = new ArrayList<JMenu>();
		private final ArrayList<JComponent> components = new ArrayList<JComponent>();

		void addCurrentTree(final boolean first) {
			if (!isInTreeBuildingMode) {
				final TreeShape treeShape = new TreeShape();
				final String debug = treeShape.toString();
				final Object[] debug2 = list.toArray();
				if (treeShape.sortOrder.length > 0) {
					if (!list.contains(treeShape)) {
						unregisterActions();
						if (first) {
							list.addFirst(treeShape);
						} else {
							list.add(treeShape);
						}
						while (list.size() > 10) {
							list.remove(list.size() - 1);
						}
						for (int i = 0; i < menus.size(); i++) {
							final JMenu menu = menus.get(i);
							final JComponent component = components.get(i);
							registerActions(component, menu);
						}
						save();
						notifyViewChanged();
					}
				}
			}
		}

		DisabledExplainer addDisabled,removeDisabled,clearDisabled;
		void registerActions(final JComponent component, final JMenu menu) {
			menu.removeAll();
			String debug = null;
			;
			for (int i = 0; i < list.size(); i++) {
				final TreeShape treeShape = list.get(i);
				final JMenuItem menuItem = treeShape.register(component, i);
				if (menuItem != null) {
					debug = menuItem.getText();
					menu.add(menuItem);
				}
			}
			if (addAddRemoveMenuItems) {
				final JMenuItem add, remove, clear;
				add = new JMenuItem(ADD_CURRENT_TO_FAVORITES, MmsIcons
						.getAddIcon());
				addDisabled = new DisabledExplainer(add);
				remove = new JMenuItem(REMOVE_CURRENT_FROM_FAVORITES, MmsIcons
						.getRemoveIcon());
				removeDisabled = new DisabledExplainer(remove);
				clear = new JMenuItem(REMOVE_ALL_FAVORITES, MmsIcons
						.getDeleteIcon());
				clearDisabled = new DisabledExplainer(clear);
				menu.addSeparator();
				menu.add(add);
				add.setMnemonic('a');
				menu.add(remove);
				remove.setMnemonic('r');
				menu.add(clear);

				add.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addCurrentTree(true);
					}
				});
				remove.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeCurrentTree();
					}
				});
				clear.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						clear();
					}
				});

			}
			if (!menus.contains(menu)) {
				menus.add(menu);
				components.add(component);
			}
		}

		private void qualifyActions() {
			final boolean isTreeActive = groupedDataSource != null
					&& !isCompanionTable;
			final TreeShape ts = new TreeShape();
			final String cur;
			if (!isTreeActive) {
				cur = "";
			} else {
				final JMenuItem mi = ts.getMenuItem();
				if (mi == null) {
					cur = "";
				} else {
					cur = mi.getText();
				}
			}
			final boolean preExists = !isTreeActive ? false : list.contains(ts);
			DisabledExplainer miDisabled;
			String disabledMenuItemMsg = null;
			for (final JMenu menu : menus) {
				JMenuItem add = null, remove = null, clear = null;
				final Component[] c = menu.getMenuComponents();
				for (int i = 0; i < c.length; i++) {
					if (c[i] instanceof JMenuItem) {
						final JMenuItem mi = (JMenuItem) c[i];
						miDisabled = new DisabledExplainer(mi);
						final String txt = mi.getText();
						if (txt.equals(ADD_CURRENT_TO_FAVORITES)) {
							add = mi;
							disabledMenuItemMsg = "This operation is ONLY enabled if you are in tree view OR if it is not already added to favorites";
						} else if (txt.equals(REMOVE_CURRENT_FROM_FAVORITES)) {
							remove = mi;
							disabledMenuItemMsg = "This operation is ONLY enabled if you already have favorites";
						} else if (txt.equals(REMOVE_ALL_FAVORITES)) {
							clear = mi;
							disabledMenuItemMsg = "This operation is ONLY enabled if you already have favorites";
						} else {
							final boolean theSame = cur.equals(txt);
							final boolean enabled = !theSame || !isTreeActive;
							boolean invalid = false;
							if (enabled) {
								boolean found = false;
								for (final TreeShape _ts : list) {
									final String s = _ts.toString();
									if (Basics.equals(s, txt)) {
										found = true;
										if (!_ts.isStillValid()) {
											invalid = true;
										}
										break;
									}
								}
								if (!found) {
									invalid = true;
								}
							}
							if (invalid) {
								menu.remove(mi);
							} else {
								if(enabled) {
									mi.setToolTipText(null);
									miDisabled.setEnabled(true,null,null);
								}
								else {
									miDisabled.setEnabled(false, txt, disabledMenuItemMsg);
								}
								mi.setFont(SwingBasics.getLabelFont(enabled));
								((JCheckBoxMenuItem) mi).setState(!enabled);
							}
							// System.out.println( "enabled=" + enabled+",
							// for \""+txt+"\"");
						}
					}
				}
				if (add != null) {
					if(isTreeActive && !preExists) {
						addDisabled.setEnabled(true, null, null);
					}
					else {
						addDisabled.setEnabled(false, ADD_CURRENT_TO_FAVORITES,
								"This operation is ONLY enabled if you are in tree view OR if it is not already added to favorites");
					}
				}
				if (remove != null) {
					if(isTreeActive && preExists) {
						removeDisabled.setEnabled(true, null, null);
					}
					else {
						removeDisabled.setEnabled(false, REMOVE_CURRENT_FROM_FAVORITES,
								"This operation is ONLY enabled if you already have favorites");
					}
				}
				if (clear != null) {
					if(list.size() > 0) {
						clearDisabled.setEnabled(true, null, null);
					}
					else {
						clearDisabled.setEnabled(false, REMOVE_ALL_FAVORITES,
								"This operation is ONLY enabled if you already have favorites");
					}
				}
			}
		}
	}

	TreeShapes recentTrees, favoriteTrees;

	public class MultiRowEditRule {
		private final MultiRowEditRuleType ruleType;
		final int[] editTheseColumns, ifAnyOfTheseColumns;
		public final JMenuItem menuItem;
		private final String ruleViolationToolTip;
		final boolean applyEditsToAllUncondensedChildRows;

		public MultiRowEditRule(final MultiRowEditRuleType ruleType,
				final String menuDescription,
				final String ruleViolationToolTip,
				final int[] editTheseColumns, final int[] ifAnyOfTheseColumns,
				boolean applyEditsToAllUncondensedChildRows) {
			this.ruleType = ruleType;
			menuItem = new JMenuItem(
					menuDescription == null ? getColumnAbbreviations(
							editTheseColumns, "") : menuDescription);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					dataSource.editFromTreeContext(
							PersonalizableTableModel.this,
							MultiRowEditRule.this, -1);

				}
			});
			this.editTheseColumns = editTheseColumns;
			this.ifAnyOfTheseColumns = ifAnyOfTheseColumns;
			this.applyEditsToAllUncondensedChildRows = applyEditsToAllUncondensedChildRows;
			this.ruleViolationToolTip = ruleViolationToolTip;
			treeEditRules.add(this);
			setEnabledToolTip();
		}

		public MultiRowEditRule(final String menuDescription,
				final String ruleViolationToolTip, final int[] editTheseColumns) {
			this(MultiRowEditRuleType.ANYTHING_GOES, menuDescription,
					ruleViolationToolTip, editTheseColumns, new int[0], true);

		}

		public MultiRowEditRule(final MultiRowEditRuleType ruleType,
				final String menuDescription,
				final String ruleViolationToolTip,
				final int[] editTheseColumns, // e.g.
				// sample
				// repeats,
				// cell
				// volume
				final int[] ifAnyOfTheseColumns) {
			this(ruleType, menuDescription, ruleViolationToolTip,
					editTheseColumns, ifAnyOfTheseColumns, true);

		}

		void resolve(final int[] dataColumnSortOrder,
				final GroupedDataSource.Node[] selectedNodes,
				final int[] dataColumnsOfSelected) {

			if (!isApplicable(dataColumnSortOrder, selectedNodes,
					dataColumnsOfSelected)) {
				menuItem.setEnabled(false);
				menuItem.setToolTipText(ruleViolationToolTip);
			} else {
				menuItem.setEnabled(true);
				setEnabledToolTip();
			}
		}

		boolean isApplicable(final int[] dataColumnSortOrder,
				final GroupedDataSource.Node[] selectedNodes,
				final int[] selectedDataColumns) {
			return ruleType.isApplicableToTreeSelection(this,
					dataColumnSortOrder, selectedNodes, selectedDataColumns);
		}

		void setEnabledToolTip() {
			menuItem.setToolTipText(getColumnAbbreviations(editTheseColumns,
					"Edit columns:  "));
		}

		int[] getEditableVisualIndexes(final PersonalizableTableModel model) {
			return model.getVisualIndexes(editTheseColumns);
		}
	}

	String getColumnAbbreviations(final int[] dataColumns, final String prefix) {
		final StringBuilder sb = new StringBuilder(prefix);
		for (int i = 0; i < dataColumns.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(getColumnAbbreviation(dataColumns[i]));
		}
		return sb.toString();
	}

	String getColumnAbbreviations(final List<Integer>dataColumns) {
		final StringBuilder sb = new StringBuilder("<ul>");
		for (int i = 0; i < dataColumns.size(); i++) {
			sb.append("<li>");
			sb.append(getColumnAbbreviation(dataColumns.get(i)));
		}
		sb.append("</ul>");
		return sb.toString();
	}

	public boolean hasCustomSortSequence(final int dataColumnIndex) {
		if (dataColumnIndex < 0 || dataColumnIndex >= metaRow.size()) {
			return false;
		}
		final String id = metaRow.getDataColumnIdentifier(dataColumnIndex);
		List<String> values;
		final boolean wasAlphabetic;
		values = getSortSequence(metaRow, id);
		wasAlphabetic = values == null;
		return !wasAlphabetic;
	}

	public boolean isSorted(final int dataColumnIndex) {
		final SortInfo[] si = getAllSortInfo();
		for (int i = 0; i < si.length; i++) {
			if (si[i].dataColumnIndex == dataColumnIndex) {
				return true;
			}
		}
		return false;
	}

	public Boolean getSorted(final int dataColumn) {
		final SortInfo[] si = getAllSortInfo();
		for (int i = 0; i < si.length; i++) {
			if (si[i].dataColumnIndex == dataColumn) {
				return si[i].ascending;
			}
		}
		return null;
	}

	private boolean allowEditAllInClickedColumn = true;

	public void setAllowEditAllInClickedColumn(final boolean ok) {
		allowEditAllInClickedColumn = ok;
	}

	public boolean canEditCellForMultipleRows = true;

	public void setPopupLocationToHeader(final int dataColumnIndex, final boolean toTheRight){
		final Point p = table.header.getLocationOnScreen();
		final int visualColumnIndex=getVisualColumnIndexFromDataColumnIndex(dataColumnIndex <0?getFocusDataColumnIndex():dataColumnIndex);
		final Rectangle rec = table.header.getHeaderRect(visualColumnIndex);
		if (toTheRight){
			p.x += rec.x+rec.width;
		}else{
			p.x += rec.x;
		}
		p.y+=rec.y;
		p.y += rec.height;
		PopupBasics.location = p;
	}
	
	public boolean editingAll=false;
	private void editAllValues(final int dataColumnIndex, final int updatedVisualRowIndex) {
		if (canEditAll(dataColumnIndex)) {
			ToolTipOnDemand.getSingleton().hideTipWindow();
			editingAll=true;
			setMultiRowChangeOperationFlag(2, true);		
			delayReSyncResolution();
			final boolean canEdit = metaRow.isEditable(dataColumnIndex);
			final int max = showFilterUI ? 3 : 1;
			if (canEdit && getRowCount() > max && getUngroupedModel().canEditCellForMultipleRows) {
				setPopupLocationToHeader(dataColumnIndex,true);
				if (getModelType() == TYPE_GROUP_SEIVED
						&& groupedDataSource.tree != null
						&& groupedDataSource.tree.getSelectionCount() > 0
						&& !isSorted(dataColumnIndex)) {
					final PersonalizableTableModel utm = getUngroupedModel();
					utm.resolveMultiRowEditRules();
					MultiRowEditRule rule = null;
					for (final Iterator it = utm.treeEditRules.iterator(); it
							.hasNext();) {
						final MultiRowEditRule prospectiveRule = (MultiRowEditRule) it
								.next();
						if (Basics.equalsAny(prospectiveRule.editTheseColumns,
								dataColumnIndex)) {
							if (prospectiveRule.menuItem.isEnabled()) {
								rule = prospectiveRule;
							} else {
								PopupBasics.alert(getTearAwayComponent(),
										prospectiveRule.ruleViolationToolTip,
										"Note", false);
								undelayReSyncResolution(true);
								return;
							}
						}
					}
					if (rule == null) {
						rule = new MultiRowEditRule(null, "",
								new int[] { dataColumnIndex });
					}
					dataSource.editFromTreeContext(utm, rule, updatedVisualRowIndex);
				} else {					
					dataSource.editAll(this, new MultiRowEditRule(null, "",
							new int[] { dataColumnIndex }), updatedVisualRowIndex);
				}
				PopupBasics.location=null;
			}
			undelayReSyncResolution(true);
			editingAll=false;
			setMultiRowChangeOperationFlag(2, false);		
		}
	}

	public static int[] getSort(final SortInfo[] sortInfo) {
		final int[] r = new int[sortInfo.length];
		for (int i = 0; i < r.length; i++) {
			r[i] = sortInfo[i].dataColumnIndex;
		}
		return r;
	}

	void resolveMultiRowEditRules() {
		final GroupedDataSource.Node[] selectedNodes = getSelectedNodes();
		if (treeEditRules.size() > 0 && selectedNodes.length > 0) {
			int[] selections = new int[selectedNodes.length];
			final GroupedDataSource gds = selectedNodes[0].getDataSource();
			int[] sortOrder = gds.getSortUngroupedDataColumnIndexes();
			for (int i = 0; i < selectedNodes.length; i++) {
				if (selectedNodes[i].sortIndexThatDiffers >= 0) {
					selections[i] = selectedNodes[i]
							.getUngroupedColumnThatDiffers();
				} else {
					selections[i] = MultiRowEditRuleType.ROOT_IS_SELECTED; // is
					// selected
				}
			}
			for (final Iterator it = treeEditRules.iterator(); it.hasNext();) {
				final MultiRowEditRule prospectiveRule = (MultiRowEditRule) it
						.next();
				prospectiveRule.resolve(sortOrder, selectedNodes, selections);
			}
		}
	}

	public int[] getVisualIndexes(final int[] dataColumns) {
		final ArrayList al = new ArrayList();
		for (int i = 0; i < dataColumns.length; i++) {
			final int modelColumnIndex = getModelColumnIndexFromDataColumnIndex(dataColumns[i]);
			if (modelColumnIndex >= 0) {
				int vi = SwingBasics.getVisualIndexFromModelIndex(table,
						modelColumnIndex);
				if (vi >= 0) {
					al.add(new Integer(vi));
				}
			}
		}
		int[] r = new int[al.size()];
		for (int i = 0; i < r.length; i++) {
			r[i] = ((Integer) al.get(i)).intValue();
		}
		return r;
	}

	final ArrayList treeEditRules = new ArrayList();

	private WeakHashMap<Row, String> disabledTextByRow = null;
	private WeakHashMap<GroupedDataSource.Node, String> enabledTextByNode = null,
			disabledTextByNode = null;

	public String getDisabledText(final Row row) {
		final PersonalizableTableModel utm=getUngroupedModel();
		if (utm.disabledTextByRow == null) {
			utm.disabledTextByRow = new WeakHashMap<Row, String>();
		}
		final String txt;
		if (utm.disabledTextByRow.containsKey(row)) {
			txt = utm.disabledTextByRow.get(row);
		} else {
			txt = encodeAnomalyHtml(utm.disabledTextByRow, null, row,
					getUncachedAnomalies(row));
		}
		return txt;
	}

	String encodeNode(final GroupedDataSource.Node node) {
		return nodeEncoder == null ? "<font color='red'>" + node.toString()
				+ "</font>" : nodeEncoder.encode(node);

	}

	private Collection getUncachedAnomalies(final GroupedDataSource.Node node) {
		return ((PersonalizableDataSource.CanDisable) dataSource)
				.getNodeDisabledText(node, node.groupedRow.firstUngroupedRow,
						applicationSpecificTreeSort, node.sortIndexThatDiffers);
	}

	private Collection getUncachedAnomalies(final Row row) {		
		if (dataSource instanceof PersonalizableDataSource.CanDisable) {
			return ((PersonalizableDataSource.CanDisable) dataSource)
					.getRowDisabledText(row);
		}
		return Basics.UNMODIFIABLE_EMPTY_LIST;
	}

	public String encodeUncachedAnomalyHtml(final GroupedDataSource.Node node) {
		return encodeAnomalyHtml(null, encodeNode(node), node,
				getUncachedAnomalies(node));
	}

	public static int om_treeAnomalyComputed;

	private String encodeAnomalyHtml(final GroupedDataSource.Node node) {
		om_treeAnomalyComputed++;
		return encodeAnomalyHtml(disabledTextByNode, encodeNode(node), node,
				getUncachedAnomalies(node));
	}

	String getDisabledText(final GroupedDataSource.Node node,
			final boolean useCache) {
		if (disabledTextByNode == null) {
			disabledTextByNode = new WeakHashMap<GroupedDataSource.Node, String>();
		}
		final String txt;
		if (useCache && disabledTextByNode.containsKey(node)) {
			txt = (String) disabledTextByNode.get(node);
		} else {
			
			txt = encodeAnomalyHtml(node);
		}
		return txt;
	}

	boolean supressAnomalyPopupIfPicked = false;

	public boolean setSupressAnomalyPopupIfPicked(final boolean ok) {
		final boolean was = supressAnomalyPopupIfPicked;
		supressAnomalyPopupIfPicked = ok;
		return was;
	}

	private String encodeAnomalyHtml(final WeakHashMap whm, final String title,
			final Object key, final Collection flaws) {
		final String txt;
		if (!Basics.isEmpty(flaws)) {
			StringBuilder sb = new StringBuilder("Can not select ");
			if (title != null) {
				sb.append(title);
			} else {
				sb.append("item");
			}
			if (flaws.size() > 1) {
				int i = 2;
			}
			sb.append(" because");
			final String heading = sb.toString();
			txt = Basics.toHtmlUncentered(heading, Basics.toUlHtml(flaws, null,
					false, true));
		} else {
			txt = null;
		}
		if (whm != null) {
			whm.put(key, txt);
		}
		return txt;
	}

	public String getNodeEnabledText(final GroupedDataSource.Node node,
			final int uncondensedDataColumnIndex) {
		if (enabledTextByNode == null) {
			enabledTextByNode = new WeakHashMap();
		}
		String txt = null;
		if (enabledTextByNode.containsKey(node)) {
			txt = (String) enabledTextByNode.get(node);
		} else if (dataSource instanceof PersonalizableDataSource.CanDisable) {
			txt = ((PersonalizableDataSource.CanDisable) dataSource)
					.getNodeEnabledText(node,
							node.groupedRow.firstUngroupedRow,
							applicationSpecificTreeSort,
							node.sortIndexThatDiffers,
							uncondensedDataColumnIndex);
			if (txt == null && !node.isLeaf()) {
				GroupedDataSource.MetaRow metaRow = node.groupedRow
						.getGroupedMetaRow();
				final int n = node.getChildCount();
				txt = "<html><body>"
						+ getEnabledText(false, node, metaRow.getLabel(
								node.sortIndexThatDiffers + 1).toLowerCase())
						+ "</body></html>";

			}
			if (txt != null){
				enabledTextByNode.put(node, txt.intern());
			}
		}
		return txt;
	}

	public static String getEnabledText(final boolean supressKey,
			final javax.swing.tree.TreeNode node, final String itemType) {
		final int n = node.getChildCount();

		final StringBuilder sb = new StringBuilder();
		sb.append(supressKey ? "" : node.toString());
		sb.append(n);
		sb.append(" ");
		sb.append(itemType);
		sb.append("<ul> ");
		for (int i = 0; i < n; i++) {
			sb.append("<li>");
			javax.swing.tree.TreeNode tn = node.getChildAt(i);
			sb.append(tn.toString());
		}
		sb.append("</ul>");

		return sb.toString();
	}

	public void resetDisabledCache() {
		getUngroupedModel().disabledTextByRow = null;
		disabledTextByNode = null;
	}

	boolean useDisabling = true;

	class PickHandler {
		ArrayList<TreePath[]> treePickHistory = new ArrayList<TreePath[]>();
		private final PersonalizableDataSource.CanPick pickableDataSource;
		private final JTree tree;
		private Collection<Row> originalPickRows = Basics.UNMODIFIABLE_EMPTY_LIST;
		private Collection<Row> rejectedRows = Basics.UNMODIFIABLE_EMPTY_LIST;
		private Collection<GroupedDataSource.Node> originalPickNodes = Basics.UNMODIFIABLE_EMPTY_LIST;
		private List<Object> resolvedPickObjects = Basics.UNMODIFIABLE_EMPTY_LIST;

		PickHandler(final JTree tree) {
			pickableDataSource = dataSource instanceof PersonalizableDataSource.CanPick ? (PersonalizableDataSource.CanPick) dataSource
					: null;
			this.tree = tree;
		}

		int undone = -1;

		/**
		 *
		 * @return true if a subsequent undo() will work
		 */
		void undoTree(final AbstractButton undo, final AbstractButton redo) {
			undone = (undone == -1) ? treePickHistory.size() - 2 : undone - 1;
			selectPreviousPicks();
			setDo(undo, redo);
		}

		private void setUndo(final AbstractButton undo) {
			PersonalizableTableModel.setUndo(undo, undone, treePickHistory
					.size(), true, "Undo", null);
		}

		/**
		 *
		 * @return if a subsequent redo will work
		 */
		void redoTree(final AbstractButton undo, final AbstractButton redo) {
			if (undone >= 0) {
				undone++;
				selectPreviousPicks();
				setDo(undo, redo);
			}

		}

		private void setRedo(final AbstractButton redo) {
			PersonalizableTableModel.setRedo(redo, undone, treePickHistory
					.size(), true, "Redo", null);
		}

		void setDo(final AbstractButton undo, final AbstractButton redo) {
			if (rejectedRows.size() == 0) {
				setRedo(redo);
				setUndo(undo);
			}
		}

		private void selectPreviousPicks() {
			if (undone >= 0 && undone < treePickHistory.size()) {
				alreadyHere = true;
				TreePath[] tp = treePickHistory.get(undone);				
				tree.clearSelection();
				if (tp != null && tp.length > 0) {
					int n = tp.length - 1;
					for (int i = 0; i < n; i++) {
						tree.addSelectionPath(tp[i]);
					}
					alreadyHere = false;
					doneByUser = false;
					tree.addSelectionPath(tp[n]);
					doneByUser = true;
				}
			}
		}

		private boolean doneByUser = true;

		private String resolveTreePicks(
				final GroupedDataSource.Node getAnomalyTxtForThisNode) {
			String theReturnValue = null;
			final TreePath []tp2=getSelectedTreePaths();
			final TreePath[] tp = pickableDataSource
					.reorderPicks(tp2);
			for (final TreePath path:tp2){
				if (!Basics.contains(tp, path)){
					removeTreeSelectionsSilently(path);
				}
			}
			for (int i = 0; i < tp.length; i++) {
				boolean remove = true;
				final GroupedDataSource.Node node = (GroupedDataSource.Node) tp[i]
						.getLastPathComponent();
				if (node.sortIndexThatDiffers >= allowTreeSelectionsAtThisLevelOrGreater) {
					if (!useDisabling || canPickDisabled) {
						remove = false;
					} else {
						if (!Basics.equals(getAnomalyTxtForThisNode, node)) {
							final Collection c = getUncachedAnomalies(node);
							if (Basics.isEmpty(c) || (c.size() == 1 && 
									c.contains(PersonalizableTable.ANOMALY_INACTIVE))) {
								remove = false;
							}
						} else {
							theReturnValue = getAnomalyTxtForThisNode
									.getAnomaly(false);
							if (Basics.isEmpty(theReturnValue)) {
								remove = false;
							}
						}
					}
				}
				if (!remove) {
					final Object o = pickableDataSource.createPick(node,
							node.groupedRow.firstUngroupedRow,
							node.sortIndexThatDiffers);
					if (o != null) {
						resolvedPickObjects.add(o);
						originalPickRows.add(node.groupedRow.firstUngroupedRow);
						originalPickNodes.add(node);
						pickableDataSource.tryPicks(resolvedPickObjects);
						remove=!resolvedPickObjects.contains(o);
					} else {
						pickableDataSource.tryPicks(resolvedPickObjects);
					}
				} 
				if (remove){
					if (node.groupedRow != null) {
						rejectedRows.add(node.groupedRow.firstUngroupedRow);
					}
					removeTreeSelectionsSilently(tp[i]);
				}
			}
			tree.repaint();
			if (rejectedRows.size()==0){
				recordUserSelections(getSelectedTreePaths());
			}
			return theReturnValue;
		}

		void recordUserSelections(final TreePath[] tp) {
			if (doneByUser && treeSupportsUndo) {
				if (undone < (treePickHistory.size() - 1)) {
					int sz = undone + 1;
					while (treePickHistory.size() > sz) {
						treePickHistory.remove(treePickHistory.size() - 1);
					}
				}
				treePickHistory.add(tp);
				undone = treePickHistory.size() - 1;
			}
		}

		private void resolveTablePicks() {
			final PersonalizableTableModel model = getModelShowing();
			final PersonalizableTable _table = model.table;			
			final int[] di = _table.getSelectedRows();
			final java.util.List<Row> l = model.dataSource.getFilteredDataRows();
			for (int i = 0; i < di.length; i++) {

				int r = model.getFilteredDataRowIndex(di[i]);
				if (r > -1 && r < l.size()) {
					final Row row = (Row) l.get(r);
					boolean remove = true;
					if (!useDisabling || canPickDisabled) {
						remove = false;
					} else {
						final Collection anomalies = getUngroupedModel()
								.getUncachedAnomalies(row);
						if (Basics.isEmpty(anomalies)) {
							remove = false;
						}
					}
					if (!remove) {
						final Object o = pickableDataSource.createPick(row);
						if (o != null) {
							resolvedPickObjects.add(o);
							originalPickRows.add(row);
						}
						pickableDataSource.tryPicks(resolvedPickObjects);
					} else {
						rejectedRows.add(row);
						_table.removeRowSelectionInterval(di[i], di[i]);
					}
				}
			}
			_table.repaint();
		}

		private boolean alreadyHere = false;

		String notifySelection(
				final GroupedDataSource.Node choiceToProblemInspect) {
			String problemOfChoice = null;
			if (pickableDataSource != null) {
				if (alreadyHere) {
					return problemOfChoice;
				}
				alreadyHere = true;
				rejectedRows = new ArrayList<Row>();
				originalPickRows = new ArrayList<Row>();
				originalPickNodes = new ArrayList<GroupedDataSource.Node>();
				resolvedPickObjects = new ArrayList<Object>();
				pickableDataSource.resetPicks();
				if (tree != null) {
					final String e = resolveTreePicks(choiceToProblemInspect);
					if (e != null) {
						problemOfChoice = e;
					}

				} else {
					resolveTablePicks();
				}
				pickableDataSource.completePicks(originalPickNodes,
						originalPickRows, rejectedRows);
				alreadyHere = false;
			}
			return problemOfChoice;
		}
	}

	PickHandler tablePickHandler, treePickHandler;

	PickHandler getTablePickHandler() {
		PersonalizableTableModel uncondensedTableModel = getUngroupedModel();
		if (uncondensedTableModel.tablePickHandler == null) {
			uncondensedTableModel.tablePickHandler = uncondensedTableModel.new PickHandler(
					null);
		}
		return uncondensedTableModel.tablePickHandler;
	}

	public List getPicks() {
		final PersonalizableTableModel uncondensedTableModel = getUngroupedModel();
		PickHandler ph;
		if (getGroupOption() == GROUP_BY_TREE
				&& uncondensedTableModel.treePickHandler != null) {
			ph = uncondensedTableModel.treePickHandler;
		} else {
			ph = uncondensedTableModel.tablePickHandler;
		}
		if (ph != null) {
			ph.notifySelection(null);
			return ph.resolvedPickObjects;
		}
		return null;
	}

	private final ArrayList<ViewChangeListener> layoutChangeListeners = new ArrayList<ViewChangeListener>();

	public void addLayoutChangeListener(
			final ViewChangeListener layoutChangeListener) {
		layoutChangeListeners.add(layoutChangeListener);
	}

	public void removeLayoutChangeListener(
			final ViewChangeListener layoutChangeListener) {
		layoutChangeListeners.remove(layoutChangeListener);
	}

	public interface ViewChangeListener {
		void layoutChanged(PersonalizableTableModel tableModel);
	}

	private int layoutChangeCnt = 0;

	public void notifyViewChanged() {
		final PersonalizableTableModel utm = getUngroupedModel();
		utm.layoutChangeCnt++;	
		for (final ViewChangeListener vcl:utm.layoutChangeListeners) {
			vcl.layoutChanged(utm);
		}
	}

	final static int CONFIG_COLUMN_IDENTIFIER_IDX = 1,
			CONFIG_COLUMN_LABEL_IDX = 0, CONFIG_COLUMN_HIDDEN_IDX = 2,
			CONFIG_COLUMN_SORT_IDX = 3, CONFIG_COLUMN_SORT_ORDER_IDX = 4,
			CONFIG_COLUMN_SORT_ASCENDING_IDX = 5, CONFIG_COLUMN_CNT = 6;

	public static final Collection<String> columnsWithFixedHiddenSettings = new HashSet<String>();
	public static final Collection<String> columnsAlwaysAllowEditInPlace = new HashSet<String>();	
	public static Map<String,String>globalLabels=new HashMap<String, String>();
	
	public static Set<String>forbidEditAllColumns=new HashSet<String>();
	private boolean isForbidEditAllColumn(final int dataColumnIndex){
		final String columnIdentifier=getDataColumnIdentifier(dataColumnIndex);
		if (forbidEditAllColumns.contains(columnIdentifier)){
			return true;
		}
		final Collection<String> c=getUngroupedModel()._forbidEditAllColumns;
		return  c != null && c.contains(columnIdentifier);
	}

	private Collection<String>_forbidEditAllColumns;
	public void forbidEditAll(final Collection<String>forbidEditAllColumns){
		this._forbidEditAllColumns=Basics.isEmpty(forbidEditAllColumns)?null:forbidEditAllColumns;
	}

	

	public static Set<String>invisibleColumns=new HashSet<String>();
	private Collection<String>_invisibleColumns;
	private boolean isInvisibleColumn(final String columnIdentifier){
		if (invisibleColumns.contains(columnIdentifier)){
			return true;
		}
		final Collection<String> c=getUngroupedModel()._invisibleColumns;
		return  c != null && c.contains(columnIdentifier);
	}
	
	public void hide(final Collection<String>invisibleColumns){
		this._invisibleColumns=Basics.isEmpty(invisibleColumns)?null:invisibleColumns;
	}
	
	class ColumnConfigModel extends DefaultTableModel {
		boolean changed = false;

		int getMaxLabelLen() {
			int maxLabelLen = 0;
			for (int i = 0; i < outerTableColumns.length; i++) {
				final Object o = outerTableColumns[i].data[CONFIG_COLUMN_LABEL_IDX];
				final int len = o == null ? 12 : o.toString().length();
				if (len > maxLabelLen) {
					maxLabelLen = len;
				}
			}
			return maxLabelLen;
		}

		int getLabelColumnWidth(final int maxWidth, final int charSize) {
			int w = getMaxLabelLen();
			return w * charSize > maxWidth ? maxWidth : w * charSize;
		}

		int getWindowHeight(final int maxHeight, final int rowHeight) {
			int height = dataSource.getMetaRow().size() * rowHeight;
			return height + 95 > maxHeight ? maxHeight : height + 95;
		}

		class ColumnConfig {
			private final Object data[];
			private final int dataColumnIndex;
			private String identifier;
			private final boolean isLabelSetByColumnPlugin; 
			ColumnConfig(final int dataColumnIndex, final boolean showing) {
				this.dataColumnIndex = dataColumnIndex;
				data = new Object[CONFIG_COLUMN_CNT];
				identifier = (String) metaRow
						.getDataColumnIdentifier(dataColumnIndex);
				final Boolean hidden = new Boolean(!showing);
				final String label;
				if (columnRenderer!=null){
					final String pluginLabel= columnRenderer.getColumnLabel(PersonalizableTableModel.this, dataColumnIndex);
					if (pluginLabel==null){
						label=getColumnLabel(dataColumnIndex);
						isLabelSetByColumnPlugin=false;
					} else {
						label=pluginLabel;
						isLabelSetByColumnPlugin=true;
					}
				} else {
					label=getColumnLabel(dataColumnIndex);
					this.isLabelSetByColumnPlugin=false;
				}
				data[CONFIG_COLUMN_LABEL_IDX] =label;
				data[CONFIG_COLUMN_IDENTIFIER_IDX] = identifier;
				data[CONFIG_COLUMN_HIDDEN_IDX] = hidden;
				setSortInfo();
			}

			void setSortInfo() {
				final SortInfo si = findSortInfo(dataColumnIndex);
				if (si != null) {
					data[CONFIG_COLUMN_SORT_ORDER_IDX] = new Integer(
							si.sortOrder);
					data[CONFIG_COLUMN_SORT_IDX] = new Boolean(true);
					data[CONFIG_COLUMN_SORT_ASCENDING_IDX] = new Boolean(
							!si.ascending);
				} else {
					data[CONFIG_COLUMN_SORT_ORDER_IDX] = null;
					data[CONFIG_COLUMN_SORT_IDX] = new Boolean(false);
					data[CONFIG_COLUMN_SORT_ASCENDING_IDX] = new Boolean(false);
				}
			}
		}

		void setSortInfo() {
			for (int i = 0; i < outerTableColumns.length; i++) {
				outerTableColumns[i].setSortInfo();
			}
		}

		private ColumnConfig[] outerTableColumns; // outer table's columns
		// are inner table's
		// rows
		private final String[] innerTableColumnNames = new String[] { "Name displayed",
				"System name", "Hidden?", "Sort?", "Sort order",
				"Descending?" };

		public String getColumnName(final int i) {
			return innerTableColumnNames[i];
		}

		final JDialog dlg;
		final int numberOfVisibleOuterColumns;
		ColumnConfigModel(final int rowSize, final JDialog dlg) {
			super(rowSize, CONFIG_COLUMN_CNT);
			this.numberOfVisibleOuterColumns=rowSize;
			this.dlg = dlg;
			doneButton = SwingBasics.getDoneButton(dlg,
					"Save changes to columns", false);
			dlg.getRootPane().setDefaultButton(doneButton);
			doneButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (configTable.isEditing()) {
						configTable.getCellEditor().stopCellEditing();
					}
					applyConfiguration();
				}
			});

			configTable = new JTable(this) {
				public boolean editCellAt(final int row, final int col) {
					doneButton.setEnabled(true);
					changed = true;
					SwingBasics.scrollTo(this, row, col);
					return super.editCellAt(row, col);
				}

				public boolean editCellAt(final int row, final int col,
						final EventObject eo) {
					doneButton.setEnabled(true);
					changed = true;
					// Select the cell contents
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							final Component editor = getEditorComponent();
							if (editor != null
									&& editor instanceof JTextComponent) {
								editor.requestFocusInWindow();
								((JTextComponent) editor).selectAll();
							}
						}
					});
					return super.editCellAt(row, col, eo);
				}
			};
			class DnDStarter extends MouseInputAdapter {
				public void mouseReleased(MouseEvent e) {
					TransferHandler th = configTable.getTransferHandler();
					th.exportAsDrag(configTable, e, TransferHandler.MOVE);
				}
			}
			DnDStarter starter = new DnDStarter();
			configTable.setDragEnabled(true);
			configTable.setTransferHandler(new ArrangeColumnsTransferHandler());
			configTable.addMouseMotionListener(starter);
			configTable.setIntercellSpacing(new Dimension(6,6));
			configTable.setRowHeight(configTable.getRowHeight()+6);
			SwingBasics.ignoreKeyEvent(configTable, KeyEvent.VK_ENTER);
			outerTableColumns = new ColumnConfig[rowSize];
			int showing = 0;
			for (int i = 0; i < table.tcm.getColumnCount(); i++) {
				outerTableColumns[showing++] = new ColumnConfig(
						getDataColumnIndex(table.tcm.getColumn(i)
								.getModelIndex()), true);
			}
			final MapOfMany<String, ColumnConfig>mom=new MapOfMany<String, ColumnConfigModel.ColumnConfig>(true);
			for (int dataColumnIndex = 0; dataColumnIndex < metaRow.size(); dataColumnIndex++) {
				final String columnIdentifier = metaRow.getDataColumnIdentifier(dataColumnIndex);
				if (!modelColumnIdentifiers.contains(columnIdentifier)) {
					if (isSorted(dataColumnIndex) || !isInvisibleColumn(columnIdentifier)){
						final String label = _getColumnLabel(dataColumnIndex, true);
						mom.put(label, new ColumnConfig(dataColumnIndex,false));
					}
				}
			}
			for (final String label:mom.keySet()) {
				final Collection<ColumnConfig>rows=mom.getCollection(label);
				for (ColumnConfig cc:rows){
					outerTableColumns[showing++] = cc;
				}
			}
		}

		final JTable configTable;

		public Class getColumnClass(final int i) {
			if (i == CONFIG_COLUMN_LABEL_IDX) {
				return String.class;
			} else if (i == CONFIG_COLUMN_HIDDEN_IDX) {
				return Boolean.class;
			} else if (i == CONFIG_COLUMN_SORT_ORDER_IDX) {
				return Integer.class;
			} else if (i == CONFIG_COLUMN_SORT_ASCENDING_IDX) {
				return Boolean.class;
			} else if (i == CONFIG_COLUMN_SORT_IDX) {
				return Boolean.class;
			} else if (i == CONFIG_COLUMN_IDENTIFIER_IDX) {
				return String.class;
			}
			return Object.class;
		}

		public Object getValueAt(final int row, final int col) {
			return outerTableColumns[row].data[col];
		}

		public boolean isCellEditable(final int visualRowIndex,
				final int modelColumnIndex) {
			if (modelColumnIndex==CONFIG_COLUMN_LABEL_IDX){
				if (outerTableColumns[visualRowIndex].isLabelSetByColumnPlugin){
					final String s=outerTableColumns[visualRowIndex].identifier;
					final int dataColumnIndex=metaRow.indexOf(s);
					configTable.setToolTipText(Basics.toHtmlErrorUncentered("Can't change this column label since it is controlled internally."));
					ToolTipOnDemand.getSingleton().showLater(configTable);
					new Timer().schedule(new TimerTask() {
	    	    		public void run() {
	    	    			configTable.setToolTipText(null);
	    	    		}}, 500);
					return false;
				}
				return true;
			} else if (modelColumnIndex==CONFIG_COLUMN_HIDDEN_IDX){
				final ArrayList<Integer>al=columnFreezer.l;
				final String s=outerTableColumns[visualRowIndex].identifier;
				final int dataColumnIndex=metaRow.indexOf(s);
				if (al.contains(dataColumnIndex)){
					configTable.setToolTipText(Basics.toHtmlErrorUncentered("Can't change", getColumnAbbreviation(dataColumnIndex)+" is frozen in row header"));
					ToolTipOnDemand.getSingleton().showLater(configTable);
					new Timer().schedule(new TimerTask() {
	    	    		public void run() {
	    	    			configTable.setToolTipText(null);
	    	    		}}, 500);
					return false;
				}
				else if(columnsWithFixedHiddenSettings.contains(s)) {
					configTable.setToolTipText(Basics.toHtmlErrorUncentered("Can't change", getColumnAbbreviation(dataColumnIndex)+" is controlled by the system"));
					ToolTipOnDemand.getSingleton().showLater(configTable);
					new Timer().schedule(new TimerTask() {
	    	    		public void run() {
	    	    			configTable.setToolTipText(null);
	    	    		}}, 500);
					return false;
				}
				return true;
				
			}
			return ( canSort() || modelColumnIndex != CONFIG_COLUMN_SORT_ASCENDING_IDX) && 
			( canSort() || modelColumnIndex != CONFIG_COLUMN_SORT_IDX) && 
			modelColumnIndex != CONFIG_COLUMN_SORT_ORDER_IDX && modelColumnIndex != CONFIG_COLUMN_IDENTIFIER_IDX;
			
		}

		public void setValueAt(final Object value, final int row,
				final int modelColumn) {
			if (modelColumn == CONFIG_COLUMN_SORT_IDX) {
				if (((Boolean) value).booleanValue()) {
					sort(
							outerTableColumns[row].dataColumnIndex,
							!((Boolean) outerTableColumns[row].data[CONFIG_COLUMN_SORT_ASCENDING_IDX])
									.booleanValue());
				} else {
					unsort(outerTableColumns[row].dataColumnIndex);
				}
				setSortInfo();
				configTable.repaint();
			} else if (modelColumn == CONFIG_COLUMN_SORT_ASCENDING_IDX) {
				sort(outerTableColumns[row].dataColumnIndex, !((Boolean) value)
						.booleanValue());
				outerTableColumns[row].setSortInfo();
			} else {
				outerTableColumns[row].data[modelColumn] = value;
			}
			doneButton.setEnabled(true);
			changed = true;
		}

		class Renderer extends DefaultTableCellRenderer {
			private final Font canNotConfigFont, baseFont;
			private final java.awt.Color canNotConfigForeground, foreground, selectionForeground, background, selectionBackground;
			private final String rgbSelectionForeground, rgbForeground;

			Renderer() {
				background=configTable.getBackground();
				selectionBackground=configTable.getSelectionBackground();
				foreground = configTable.getForeground();
				selectionForeground=configTable.getSelectionForeground();
				canNotConfigForeground = java.awt.Color.red;
				baseFont = configTable.getFont();
				canNotConfigFont = new Font(baseFont.getName(), Font.ITALIC,
						baseFont.getSize());
				rgbSelectionForeground = SwingBasics.toHtmlRGB(selectionForeground);
				rgbForeground = SwingBasics.toHtmlRGB(foreground);
			}

			public Component getTableCellRendererComponent(final JTable table,
					Object value, final boolean isSelected,
					final boolean hasFocus, final int row, final int visualIndex) {
				if (isSelected) {
					setBackground(selectionBackground);
				} else { 
					setBackground(background);
				}
				if (hasFocus) {
					Border border = PersonalizableTable.BORDER_FOCUS_THIN;
					setBorder(border);
				} else {
					setBorder(new EmptyBorder(1, 1, 1, 1));
				}
				final ColumnConfig column = outerTableColumns[row];
				final boolean canConfig = true; // canConfigColumn(column.dataColumnIndex);
				if (!canConfig) {
					setFont(canNotConfigFont);
					setToolTipText(Basics.toHtmlUncentered("This column is under control of the grouped view"));
					setForeground(canNotConfigForeground);
				} else {
					setFont(baseFont);
					if (column.identifier != column.data[CONFIG_COLUMN_LABEL_IDX]
							&& visualIndex != 0) {
						setToolTipText(Basics.concat("Also known as \"", column.identifier, "\""));
					} else {
						value=Basics.stripBodyHtml((String)value);
						setToolTipText("");
					}
					setForeground(isSelected?selectionForeground:foreground);
				}
				if (value != null) {
					String s = value.toString();
					if (s.startsWith(html)) {
						final int idx = s.lastIndexOf("</html>");
						if (idx > html.length()) {
							final String textColor = isSelected ? rgbSelectionForeground
									: rgbForeground;
							s = s.substring(html.length(), idx);
							setText(Basics.concat(html, "<font color='",
									textColor, "'>", s, "</font></html>"));
							return this;
						}

					}
					setText(s);
				}
				return this;
			}
		}

		private final String html="<html>";
		void moveTop(){
			final int []selected=configTable.getSelectedRows();
	    	final Object []v2=ArrayBasics.move(outerTableColumns, selected, ArrayBasics.Direction.TOP);
	    	final int []n=ArrayBasics.move(selected, ArrayBasics.Direction.TOP, outerTableColumns.length-1);
	    	configTable.clearSelection();
	    	for(int i=0;i<v2.length;i++){
				outerTableColumns[i]=(ColumnConfig)v2[i];
			}
	    	for (final int i:n) {
	    		configTable.getSelectionModel().addSelectionInterval(i, i);
	    	}
	    	SwingBasics.scrollTo(configTable, 0,0);
	    	configTable.requestFocus();	    	
		}
		
		void moveBottom(){
			final int []selected=configTable.getSelectedRows();
	    	final Object []v2=ArrayBasics.move(outerTableColumns, selected, ArrayBasics.Direction.BOTTOM);
	    	final int []n=ArrayBasics.move(selected, ArrayBasics.Direction.BOTTOM, outerTableColumns.length-1);
	    	configTable.clearSelection();
	    	for(int i=0;i<v2.length;i++){
				outerTableColumns[i]=(ColumnConfig)v2[i];
			}
	    	if (n.length>0){
	    		SwingBasics.scrollTo(configTable, n[0], 0);
	    	}
	    	for (final int i:n) {
	    		configTable.getSelectionModel().addSelectionInterval(i, i);    		
	    	}
	    	configTable.requestFocus();
		}
		void up(final int idx) {
			if (idx > 0) {
				final ColumnConfig cc = outerTableColumns[idx - 1];
				outerTableColumns[idx - 1] = outerTableColumns[idx];
				outerTableColumns[idx] = cc;
			}
		}

		void down(final int idx) {
			if (idx < numberOfVisibleOuterColumns - 1) {
				ColumnConfig cc = outerTableColumns[idx + 1];
				outerTableColumns[idx + 1] = outerTableColumns[idx];
				outerTableColumns[idx] = cc;
			}

		}

		boolean applied = false;
		final RowOptions rowOptions = new RowOptions();

		void applyConfiguration() {
			// System.out.print("Apply configuration?");
			if (changed && !applied) {
				applied = true;
				if (rowOptions.alternatingColor != null) {
					setGlobalAlternatingRowColor(rowOptions.alternatingColor);
				}
				if (rowOptions.useDittos != null) {
					useDittos = rowOptions.useDittos.booleanValue();
				}
				final ArrayList al = new ArrayList();
				final HashMap<String, String> rlbn = (ungroupedModel == null) ? renamedLabelsByIdentifier
						: ungroupedModel.renamedLabelsByIdentifier;
				for (int i = 0; i < outerTableColumns.length; i++) {
					if (!((Boolean) outerTableColumns[i].data[CONFIG_COLUMN_HIDDEN_IDX])
							.booleanValue()) {
						al.add(outerTableColumns[i].identifier);
					}
					if (!outerTableColumns[i].data[CONFIG_COLUMN_LABEL_IDX]
							.equals(outerTableColumns[i].identifier)) {
						rlbn
								.put(
										outerTableColumns[i].identifier,
										(String) outerTableColumns[i].data[CONFIG_COLUMN_LABEL_IDX]);
					} else {
						removeRenamedProperty(outerTableColumns[i].identifier);
						rlbn.remove(outerTableColumns[i].identifier);
					}
				}
				if (al.size() == 0) {
					PopupBasics.alert(getTearAwayComponent(),
							"Must have at least one column showing", "Alert",
							true);
					al.add(outerTableColumns[0].identifier);
					rlbn
							.put(
									outerTableColumns[0].identifier,
									(String) outerTableColumns[0].data[CONFIG_COLUMN_LABEL_IDX]);
				}
				initModelColumns(al);
				table.cellHighlighter.reset();
				sortAndRepaint();
				notifyViewChanged();

			} else {
				// System.out.println(" ... no");
			}
		}

		final JButton doneButton;

		class ArrangeColumnsTransferHandler extends TransferHandler {

			public DataFlavor FLAVOR = null;

			public ArrangeColumnsTransferHandler() {
				try {
					FLAVOR = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class=java.lang.Integer");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public boolean importData(JComponent c, Transferable t) {
				if (!(c instanceof JTable)) {
					return false;
				}

				try {
					Object obj = t.getTransferData(FLAVOR);
					if (!(obj instanceof ArrayTransfer)) {
						return false;
					}
					ArrayTransfer at = (ArrayTransfer) obj;
					if (!c.equals(at.getSource())) {
						return false;
					}
					Object[] arr = at.getData();
					Arrays.sort(arr);
					if (configTable.isEditing()) {
						configTable.getCellEditor().stopCellEditing();
					}
					doneButton.setEnabled(true);
					changed = true;

					int dropRow = ((JTable) c).getSelectedRow();
					List<Integer> rowsUp = new ArrayList<Integer>();
					List<Integer> rowsDown = new ArrayList<Integer>();
					for (int i = 0; i < arr.length; i++) {
						if (arr[i] instanceof Integer) {
							if ((((Integer) arr[i]).intValue()) >= dropRow) {
								rowsUp.add((Integer) arr[i]);
							} else {
								rowsDown.add((Integer) arr[i]);
							}
						}
					}
					for (int k = rowsDown.size() - 1; k >= 0; k--) {
						dropDown(((Integer) rowsDown.get(k)).intValue(),
								dropRow, (rowsDown.size() - 1 - k));
					}
					for (int k = 0; k < rowsUp.size(); k++) {
						dropUp(((Integer) rowsUp.get(k)).intValue(), dropRow, k);
					}
					configTable.clearSelection();
					configTable.repaint();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}

			public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
				if (!(c instanceof JTable)) {
					return false;
				}
				for (int k = 0; k < transferFlavors.length; k++) {
					if (transferFlavors[k].equals(FLAVOR)) {
						return true;
					}
				}
				return false;
			}

			public int getSourceActions(JComponent c) {
				if (!(c instanceof JTable)) {
					return NONE;
				}
				return MOVE;
			}

			protected Transferable createTransferable(JComponent c) {
				if (!(c instanceof JTable)) {
					return null;
				}
				int rows[] = ((JTable) c).getSelectedRows();
				Integer rowsInt[] = new Integer[rows.length];
				for (int i = 0; i < rows.length; i++) {
					rowsInt[i] = new Integer(rows[i]);
				}
				return new ArrayTransfer(c, rowsInt);
			}

			public void exportDone(JComponent source, Transferable t, int action) {
			}

			final void dropUp(int from, int to, int justplaced) {
				while (from > (to + justplaced)) {
					up(from);
					from--;
				}
			}

			final void dropDown(int from, int to, int justplaced) {
				while (from < (to - justplaced) - 1) {
					down(from);
					from++;
				}
			}

			class ArrayTransfer implements Transferable {

				protected JComponent m_source;
				protected Object[] m_arr;

				public ArrayTransfer(JComponent source, Object[] arr) {

					m_source = source;
					m_arr = arr;
				}

				public Object getTransferData(DataFlavor flavour)
						throws UnsupportedFlavorException, IOException {

					if (!isDataFlavorSupported(flavour)) {
						throw new UnsupportedFlavorException(flavour);
					}
					return this;
				}

				public boolean isDataFlavorSupported(DataFlavor flavour) {
					return FLAVOR.equals(flavour);
				}

				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[] { FLAVOR };
				}

				public JComponent getSource() {
					return m_source;
				}

				public Object[] getData() {
					return m_arr;
				}

			}
		}

	}

	private int getShowSize(){
		int n=0;
		for (int i = 0; i < metaRow.size(); i++) {
			final String columnIdentifier = metaRow.getDataColumnIdentifier(i);
			if (!modelColumnIdentifiers.contains(columnIdentifier)) {
				if (!isSorted(getDataColumnIndexForIdentifier(columnIdentifier)) && isInvisibleColumn(columnIdentifier)){
					continue;
				} 
			}
			n++;
		}
		return n;
	}
	void showColumnConfig() {
		final JDialog dlg = SwingBasics.getModalDialog(table, MmsIcons
				.getPreferencesImage(), "Sort/arrange columns");
		final JPanel mainPanel = new GradientBasics.Panel(new BorderLayout());
		
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,7,5,7));
		dlg.getContentPane().add(mainPanel);
		final ColumnConfigModel columnConfigModel = new ColumnConfigModel(getShowSize(), dlg);
		final JTable configTable = columnConfigModel.configTable;
		configTable.setDefaultRenderer(Object.class,
				columnConfigModel.new Renderer());
		configTable.getTableHeader().setReorderingAllowed(false);
		configTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		configTable.getInputMap(configTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.getParent().remove(
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		configTable.setColumnSelectionAllowed(false);
		configTable.setRowSelectionAllowed(true);
		final JPanel moveButtons = new JPanel(new BorderLayout());
		final ClassLoader cl = this.getClass().getClassLoader();
		final JButton up = new JButton(MmsIcons.getUpIcon());
		up.setToolTipText(Basics.toHtmlUncentered("Move selected item towards start of display order"));
		up.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				final int[] rows = configTable.getSelectedRows();
				if (configTable.isEditing()) {
					configTable.getCellEditor().stopCellEditing();
				}
				columnConfigModel.doneButton.setEnabled(true);
				columnConfigModel.changed = true;
				Arrays.sort(rows);
				for (int i = 0; i < rows.length; i++) {
					columnConfigModel.up(rows[i]);
				}
				configTable.clearSelection();
				// int lowest=Integer.MAX_VALUE;
				for (int i = 0; i < rows.length; i++) {

					rows[i]--;

					if (rows[i] >= 0) {
						if (i == 0) {
							configTable.setRowSelectionInterval(rows[i],
									rows[i]);
						} else {
							configTable.addRowSelectionInterval(rows[i],
									rows[i]);
						}
					}
				}
				if (rows.length > 0) {
					final Rectangle rect = configTable.getCellRect(rows[0], 0,
							true);
					configTable.scrollRectToVisible(rect);
				}
				configTable.repaint();
				configTable.requestFocus();
			}
		});
		up.setMnemonic(KeyEvent.VK_UP);
		moveButtons.add(up, BorderLayout.NORTH);
		up.setEnabled(false);
		final JButton down = new JButton(MmsIcons.getDownIcon());
		down.setMnemonic(KeyEvent.VK_DOWN);
		down.setToolTipText(Basics.toHtmlUncentered("Move selected item towards end of display order"));
		configTable.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_UP || e.getKeyCode()==KeyEvent.VK_DOWN || e.getKeyCode()==KeyEvent.VK_CONTROL || e.getKeyCode()==KeyEvent.VK_HOME || e.getKeyCode()==KeyEvent.VK_END){
					if(configTable.isEditing())
						configTable.getCellEditor().stopCellEditing();
					
					if(e.getKeyCode()==KeyEvent.VK_HOME && !e.isControlDown()){
						configTable.clearSelection();
						configTable.getSelectionModel().setSelectionInterval(0,0);
						configTable.requestFocus();
					}
					if(e.getKeyCode()==KeyEvent.VK_END && !e.isControlDown()){
						configTable.clearSelection();
						configTable.getSelectionModel().setSelectionInterval(configTable.getRowCount()-1,configTable.getRowCount()-1);
						configTable.requestFocus();
					}
				}
			}
			
		});
		
		
		down.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				final int[] rows = configTable.getSelectedRows();
				if (configTable.isEditing()) {
					configTable.getCellEditor().stopCellEditing();
				}

				columnConfigModel.doneButton.setEnabled(true);
				columnConfigModel.changed = true;
				Arrays.sort(rows);
				for (int i = rows.length - 1; i >= 0; i--) {
					columnConfigModel.down(rows[i]);
				}
				configTable.clearSelection();
				for (int i = 0; i < rows.length; i++) {
					rows[i]++;
					if (rows[i] < metaRow.size()) {
						if (i == 0) {
							configTable.setRowSelectionInterval(rows[i],
									rows[i]);
						} else {
							configTable.addRowSelectionInterval(rows[i],
									rows[i]);
						}
					}
				}
				if (rows.length > 0) {
					final Rectangle rect = configTable.getCellRect(
							rows[rows.length - 1], 0, true);
					configTable.scrollRectToVisible(rect);
				}
				configTable.repaint();
				configTable.requestFocus();
			}
		});
		
		final JButton top = new SwingBasics.ImageButton(MmsIcons.getTopIcon());
		ActionListener anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				columnConfigModel.moveTop();
			}
		};
		SwingBasics.echoAction(configTable, top, anAction, 
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_HOME, InputEvent.ALT_MASK, 
						KeyEvent.VK_I, InputEvent.ALT_MASK),
						
				't');
		
		final JButton bottom = new SwingBasics.ImageButton(MmsIcons.getBottomIcon());
		anAction = new ActionListener() {
			public void actionPerformed(final ActionEvent a) {
				columnConfigModel.moveBottom();
			}
		};
		SwingBasics.echoAction(configTable, bottom, anAction, 
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_END, InputEvent.ALT_MASK, 
						KeyEvent.VK_M, InputEvent.ALT_MASK),
						 'b');

		
		moveButtons.add(down, BorderLayout.SOUTH);
		down.setEnabled(false);
		final JPanel buttons = SwingBasics.getButtonPanel(2);
		buttons.add(columnConfigModel.doneButton);
		columnConfigModel.doneButton.setEnabled(false);
		final JButton cancel = SwingBasics.getCancelButton(dlg,
				"Do not save changes", true);
		buttons.add(cancel);
		SwingBasics.ignoreKeyEvent(columnConfigModel.configTable,
				KeyEvent.VK_ESCAPE);
		final JLabel iconLabel = new JLabel("Column options");
		iconLabel.setIcon(MmsIcons.getPreferencesIcon());
		SwingBasics.layout(false, mainPanel, iconLabel, null, null, buttons);
		mainPanel.add(new JScrollPane(configTable), BorderLayout.CENTER);
		mainPanel.add(moveButtons, BorderLayout.EAST);
		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				dlg.dispose();
				toFront();
			}
		});
		// measuremments
		final int height = PersonalizableTableModel.this.table
				.getHeaderRowHeight();
		configTable.setRowHeight(height < 24 ? 24 : height);

		// table.setIntercellSpacing(new Dimension(5, 2));
		final int labelColumnWidth = columnConfigModel.getLabelColumnWidth(225,
				7);
		configTable.getTableHeader().getColumnModel().getColumn(
				CONFIG_COLUMN_LABEL_IDX).setPreferredWidth(labelColumnWidth);

		configTable.getTableHeader().getColumnModel().getColumn(
				CONFIG_COLUMN_IDENTIFIER_IDX).setPreferredWidth(
				labelColumnWidth);

		dlg.setSize(labelColumnWidth * 2 + 375, columnConfigModel
				.getWindowHeight(700, 34));

		configTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(final ListSelectionEvent e) {
						int[] idx = configTable.getSelectedRows();
						if (idx.length > 0) {
							up.setEnabled(idx[0] > 0);
							for (int i = 0; i < idx.length; i++) {
								if (idx[i] == configTable.getRowCount() - 1) {
									down.setEnabled(false);
									break;
								} else {
									down.setEnabled(true);
								}
							}
						} else {
							up.setEnabled(false);
							down.setEnabled(false);
						}

					}
				});
		SwingBasics.packAndPersonalize(dlg, null, PopupBasics.PROPERTY_SAVIOR,
				"configColumns", true, true, false);
		if (clickedVisualColumnIndex >= 0) {
			configTable.setRowSelectionInterval(clickedVisualColumnIndex,
					clickedVisualColumnIndex);
			configTable.editCellAt(clickedVisualColumnIndex, 0);
		}
		final Rectangle prevR = dlg.getBounds();
		GradientBasics.setTransparentChildren(mainPanel, true);
		SwingBasics.showUpFront(dlg, configTable);
		if (!prevR.equals(dlg.getBounds())) {
			setPropertiesFromWindow(dlg, "c");
			notifyViewChanged();
		}
		PersonalizableTableModel.this.table.recomputeHeaderLines();
		// No check needed of FocusFreeze.isFrozen
		PersonalizableTableModel.this.table.requestFocus();
	}

	boolean canConfigColumn(final int dataColumnIndex) {
		boolean youBet = true;
		if (getModelType() == TYPE_GROUP_SEIVED) {
			youBet = getUngroupedModel().findSortInfo(dataColumnIndex) == null;
		}
		return youBet;
	}

	public Boolean[] getFalseByModelIndex(final int[] dataColumnIndexes) {
		int max = -1;
		final int[] visualColumnIndexes = SwingBasics.getVisualIndexes(table);
		for (int i = 0; i < visualColumnIndexes.length; i++) {
			final int modelColumnIndex = SwingBasics
					.getVisualIndexFromModelIndex(table, visualColumnIndexes[i]);
			if (modelColumnIndex > max) {
				max = modelColumnIndex;
			}
		}
		if (max < 0) {
			return null;
		}
		final Boolean[] b = new Boolean[max + 1];
		for (int i = 0; i < dataColumnIndexes.length; i++) {
			final int modelColumnIndex = getModelColumnIndexFromDataColumnIndex(dataColumnIndexes[i]);
			if (modelColumnIndex >= 0 && modelColumnIndex < b.length) {
				b[modelColumnIndex] = Boolean.FALSE;
			}
		}
		return b;
	}

	public static String[] getTreeArgs(
			final GroupedDataSource.Node nodeContext, final Row searchArg) {
		final int[] a = nodeContext.getSortUngroupedIndexes();
		final String[] args = new String[a.length];
		for (int i = 0; i < a.length; i++) {
			args[i] = Basics.toString(nodeContext.groupedRow.firstUngroupedRow
					.get(a[i]));
		}
		return args;
	}

	private boolean selectInTree(final String[] searchArgForEachTreeLevel,
			final boolean additive, final boolean alert) {
		final boolean ok;
		if (searchArgForEachTreeLevel != null && groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			final TreePath[] tp = groupedDataSource.findInTree(
					searchArgForEachTreeLevel, alert);
			if (!Basics.isEmpty(tp)) {
				if (additive) {
					groupedDataSource.tree.addSelectionPath(tp[0]);
				} else {
					groupedDataSource.tree.setSelectionPath(tp[0]);
				}
				ok = true;
			} else {
				ok = false;
			}
		} else {
			ok = false;
		}
		return ok;
	}

	public boolean findInTree(final GroupedDataSource.Node node) {
		final int n = node.sortIndexThatDiffers + 1;
		final String[] args = new String[n];
		for (int i = 0; i < n; i++) {
			args[i] = Basics.toString(node.groupedRow.get(i));
		}
		return findInTree(args, true);
	}

	private boolean selectInTree(final Row searchArg, final boolean additive) {
		final String[] args;

		final int[] a = groupedDataSource.getSortUngroupedDataColumnIndexes();
		args = new String[a.length];
		for (int i = 0; i < a.length; i++) {
			args[i] = Basics.toString(toSequenceableString(searchArg, a[i]));
		}
		return selectInTree(args, additive, false);
	}

	public boolean findInTree(final String[] searchArgForEachTreeLevel,
			final boolean alertIfNotFound) {
		return findInTree(searchArgForEachTreeLevel, alertIfNotFound, false);
	}
	public boolean scrollTreeIfFound=true;
	public boolean findInTree(final String[] searchArgForEachTreeLevel,
			final boolean alertIfNotFound, final boolean expandIfFound) {
		if (searchArgForEachTreeLevel != null && groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			final TreePath [] tps=groupedDataSource.findInTree(
					searchArgForEachTreeLevel, alertIfNotFound);
			if (!Basics.isEmpty(tps)){
				if (expandIfFound){
					SwingBasics.expandFirstLeaf(groupedDataSource.tree, tps[0]);
				}
				return true;
			}
		}
		return false;
	}

	public boolean isInTree(final String[] searchArgForEachTreeLevel) {
		if (searchArgForEachTreeLevel != null && groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			final TreePath [] tps=SwingBasics.findInSubtree(groupedDataSource.tree, searchArgForEachTreeLevel);
			return !Basics.isEmpty(tps);
		}
		return false;
	}

	public TreePath[] getTreePaths(final String[] searchArgForEachTreeLevel) {
		if (searchArgForEachTreeLevel != null && groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource
					.findInTree(searchArgForEachTreeLevel, true);
		}
		return new TreePath[0];
	}

	public final static String PROPERTY_SORT_ORDER = "sortOrder",PROPERTY_GROUP = "condensation"; 
	private final static String PROPERTY_ASCENDING = "ascending",
			PROPERTY_SHOW_FILTER_UI = "showFilter",
			PROPERTY_USE_DITTOS = "useDittos", 

			PROPERTY_FILTER_VALUE = "filterValue",
			PROPERTY_FILTER_OP = "filterOp",

			PROPERTY_DISABLE = "disable",
			PROPERTY_CUSTOM_GROUP = "customCondensation",
			PROPERTY_TREE_MULTI_SELECT_WITH_KEY = "treeMultiSelect",
			PROPERTY_TREE_SHOW_COUNTS = "showCounts",
			PROPERTY_ROW_HEADER = "rowHeader",
			PROPERTY_TABLE_COLUMN_MODEL = "tableColumnModel",
			PROPERTY_RECENT_TREE_ASCENDING = "recentTreeAscending",
			PROPERTY_RECENT_TREE_IDX = "recentTree",
			PROPERTY_RECENT_TREE_IDS = "recentTreeIds",
			PROPERTY_FAVORITE_TREE_ASCENDING = "favoriteTreeAscending",
			PROPERTY_FAVORITE_TREE_IDX = "favoriteTree",
			PROPERTY_FAVORITE_TREE_IDS = "favoriteTreeIds";

	final static String PROPERTY_AUTO_RESIZE = "autoResize",
			PROPERTY_DIVIDER = "channelDivide",
			PROPERTY_HEADER_WIDTH = "headerWidth",
			PROPERTY_SUFFIX_ALTERNATING_COLOR = "alternatingRowColor",
			PROPERTY_ALTERNATING_COLOR = "Table."
					+ PROPERTY_SUFFIX_ALTERNATING_COLOR,
			PROPERTY_WINDOW_WIDTH = "windowWidth",
			PROPERTY_WINDOW_HEIGHT = "windowHeight",
			PROPERTY_WINDOW_X = "windowX", PROPERTY_WINDOW_Y = "windowY",
			PROPERTY_WINDOW_M = "windowM",
			PROPERTY_SHOW_SEARCH_NODE_PANEL = "showSearchNodePanel",
			PROPERTY_EXPAND_ALL_AT_STARTUP = "expandAllAtStartup",
			PROPERTY_TABS = "tabIdx";

	public final static String PROPERTY_HEADER_LABEL = "headerLabel",
			PROPERTY_DIVIDER_LOCATION = "divider",
			PROPERTY_SELECTED_ROW = "selectedRow",
			PROPERTY_SEIVE_SORT_FREELY = "seiveSortFreely",
			PROPERTY_SAME_TREE_SHAPE = "wasCondensed",PROPERTY_QUERY_FAVORITE="queryFavorite";

	private static boolean PROPERTY_DEFAULT_TREE_SHOW_COUNTS = false;

	protected List<SortInfo> columnsToSort = new ArrayList<SortInfo>();

	public void sort(final String key, final boolean ascending) {
		final int dc=metaRow.indexOf(key);
		if (dc>=0){
			sort(dc, ascending);
		} else {
			System.out.println(key+" is no longer a column?");
		}
	}
	
	public void sort(final int dataColumnIndex, final boolean ascending) {
		SortInfo sid = findSortInfo(dataColumnIndex);
		if (sid == null) {
			sid = new SortInfo(metaRow, dataColumnIndex,
					columnsToSort.size() + 1);
			columnsToSort.add(sid);
		}
		sid.ascending = ascending;
		sortNeeded = true;
		latestSortColumn = getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);

	}

	private int latestSortColumn;

	public void unsort(final int dataColumnIndex) {
		final SortInfo sid = findSortInfo(dataColumnIndex);
		int idx = -1;
		if (sid != null) {
			idx = sid.sortOrder - 1;
			columnsToSort.remove(sid.sortOrder - 1);
			for (int i = idx; i < columnsToSort.size(); i++) {
				((SortInfo) columnsToSort.get(i)).sortOrder = i + 1;
			}
		}
		sortNeeded = columnsToSort.size() > 0;
	}

	public String getCurrentSort() {
		return getCurrentSort(", ");
	}

	public String getCurrentSort(final String delimiter) {
		return getCurrentSortTxt(-1, delimiter);
	}

	private String getCurrentSortTxt(final int highlightColumn,
			final String delimiter) {
		final StringBuilder sb = new StringBuilder();
		final int n = columnsToSort.size();
		for (int i = 0; i < n; i++) {
			final SortInfo si = columnsToSort.get(i);
			if (i > 0) {
				sb.append(delimiter);
			}
			encode(sb, si, highlightColumn == si.dataColumnIndex);
		}
		return sb.toString();
	}

	private String getCurrentTreeHtml() {
		final StringBuilder sb = new StringBuilder();
		final int n = columnsToSort.size();

		for (int i = 0; i < n; i++) {
			sb.append("<ul><li>");
			final SortInfo si = (SortInfo) columnsToSort.get(i);
			encode(sb, si, true);
		}
		for (int i = 0; i < n; i++) {
			sb.append("</ul>");
		}
		return sb.toString();
	}

	private boolean hasDescending() {
		for (final SortInfo si : columnsToSort) {
			if (!si.ascending) {
				return true;
			}
		}
		return false;
	}

	public String getCurrentSort(final int highlightColumn,
			final boolean highlightColumnIsCustomSequence) {
		if (highlightColumn >= 0) {
			final int n = columnsToSort.size();
			final StringBuilder sb = new StringBuilder();
			if (n > 1) {
				final String s = getCurrentSortTxt(highlightColumn, ",&nbsp;&nbsp;");
				sb.append(s);
			} else {
				sb.append(getCurrentSortTxt(-1, ""));
			}
			if (highlightColumnIsCustomSequence) {
				sb.append("<br><br>");
				sb.append(Basics.NOTE_BOLDLY);
				sb.append("The newly sorted column <i>");
				sb.append(getColumnLabel(highlightColumn));
				sb.append("</i> uses a custom sort <br>sequence.");
			}
			return Basics.toHtmlUncentered("Current sort order", sb.toString());
		}
		return "";
	}

	boolean hasNonAlphabeticSortSequence(final int dataColumnIndex){
		return hasCustomSortSequence(dataColumnIndex) ||
			getUngroupedModel().getMetaRow().hasNonAlphabeticSort(dataColumnIndex);
	}
	
	boolean isUsingNonAlphabeticSortSequence(final int dataColumnIndex){
		if (hasCustomSortSequence(dataColumnIndex)){
			return true;
		}
		final MetaRow mr=getUngroupedModel().getMetaRow();
		if (mr.hasNonAlphabeticSort(dataColumnIndex)){
			if (mr.getSortValueReinterpreter(dataColumnIndex).isOn()){
				return true;
			}
		}
		return false;
	}
	
	private void encode(final StringBuilder sb, final SortInfo si,
			final boolean italicize) {
		final boolean isNonAlphabetic=isUsingNonAlphabeticSortSequence(si.dataColumnIndex);
		final URL url;
		if (si.ascending){
			if (isNonAlphabetic){
				url=MmsIcons.getURL("sortCustomDown16.gif");
			} else {
				url=MmsIcons.getURL("sortDown16.gif");
			}
		} else {
			if (isNonAlphabetic){
				url=MmsIcons.getURL("sortCustomUp16.gif");
			} else {
				url=MmsIcons.getURL("sortUp16.gif");
			}
		}
		final String text = getColumnAbbreviation(si.dataColumnIndex);
		if (!italicize) {
			sb.append(text);
			sb.append("&nbsp;<img src='");
			sb.append(url);
			sb.append("'>");

		} else {
			sb.append("<i><b>");
			sb.append(text);
			sb.append("&nbsp;<img src='");
			sb.append(url);
			sb.append("'>&nbsp;");
			sb.append("</b></i>");
		}
	}

	public SortInfo findSortInfo(final int dataColumnIndex) {
		final int n = columnsToSort.size();
		for (int i = 0; i < n; i++) {
			final SortInfo si = (SortInfo) columnsToSort.get(i);
			if (si.dataColumnIndex == dataColumnIndex) {
				return si;
			}
		}
		return null;
	}

	public void removeSort(final int dataColumnIndex) {
		final List<SortInfo> c = new ArrayList<SortInfo>();
		final int n = columnsToSort.size();
		for (int i = 0; i < n; i++) {
			final SortInfo si = (SortInfo) columnsToSort.get(i);
			if (si.dataColumnIndex != dataColumnIndex) {
				c.add(si);
			}
		}
		columnsToSort = c;
	}

	boolean sortNeeded = false;

	public boolean isSortNeeded() {
		return sortNeeded;
	}

	public ColumnRenderer getColumnRenderer(){
		return columnRenderer;
	}
	public SortInfo[] getAllSortInfo() {
		return columnsToSort.toArray(new SortInfo[columnsToSort.size()]);
	}

	public void setTable(final PersonalizableTable table) {
		this.table = table;
		configColumns();
		dataSource.configure(table);
		if (globalAlternatingRowColor == null) {
			initGlobalProperties();
		}
	}

	PersonalizableTable table;
	private Properties properties;

	List<String> modelColumnIdentifiers;

	public boolean showFilterUI = false;
	private boolean firstSettingOfFilterUI = true;

	public boolean setFilterRows(final boolean show) {
		if (showFilterUI != show) {
			if (!show || dataSource.isFilterable()) {
				showFilterUI = show;
				refreshShowingTable(false);
				return true;
			}
		}
		return false;
	}

	private Set valueSettings = null;

	void watchValueSettings() {
		valueSettings = new HashSet();
	}

	int[] getValueSettings() {
		return Basics.toIntArray(valueSettings);
	}

	boolean settingMultipleCells = false;
	public void setValueAt(final Object value, final int visualRowIndex,
			final int modelColumnIndex) {
		final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
		final boolean isSettingFilteringCellValues;
		isDoubleClickEvent = false;
		if (!showFilterUI
				|| (visualRowIndex != table.ROW_IDX_FILTER_VALUE && visualRowIndex != table.ROW_IDX_FILTER_OP)) {
			final SortInfo si = findSortInfo(dataColumnIndex);
			isSettingFilteringCellValues = false;
			final boolean wasSortNeeded = sortNeeded;
			sortNeeded = si != null;
			if (sortNeeded && !wasSortNeeded) {
				table.getTableHeader().repaint();
			}
		} else {
			isSettingFilteringCellValues = true;
		}
		final Row row = getRowAtVisualIndex(visualRowIndex);
		if (row != null) {
			final Object prior=row.getFilterableValue(dataColumnIndex);
			if (!isSettingFilteringCellValues) {
				vr.rememberPriorTreeValue(row, dataColumnIndex);
			}
			if (value instanceof SelectableCell.Item) {	
				((SelectableCell.Item) value).executeCmd();
			} 
			else {
				Object previousValue = getValueAt(visualRowIndex, modelColumnIndex);
				row.set(dataColumnIndex, value);
				if (getSelectedRowCountMinusLastRowForEditingAndFilterRows() > 1 
						&& !settingMultipleCells ) {
					boolean editAll=!Basics.equals(previousValue,value);
					if (!editAll){
						final TableCellEditor tce=table.getCellEditor();
						if (tce instanceof PersonalizableCellEditor){
							final KeyEvent ke=((PersonalizableCellEditor)tce).lastKeyEvent();
							if (ke!=null){
								final boolean tableEdited=((PersonalizableCellEditor)tce).didTableEditingStart();
								final int kc=ke.getKeyCode();
								if (!tableEdited && (kc==9 || (kc<=40 && kc>=37))){
								}else{
									editAll=true;
								}
							}
						}
					}
					if (editAll){
						settingMultipleCells = true;
						editAllValues(dataColumnIndex, visualRowIndex);
						settingMultipleCells = false;
					}
				}
				if (showFilterUI) {
					final Object opValue;
					String opCond = "";
					if (visualRowIndex == PersonalizableTable.ROW_IDX_FILTER_VALUE) {
						if (value instanceof String){
							opCond=(String)getValueAt(PersonalizableTable.ROW_IDX_FILTER_OP, modelColumnIndex);
							if (Filterable.opStartsWith.equals(opCond) && ((String) value).endsWith("*")){
								final int n=( (String)value).length()-1;
								opValue=((String)value).substring(0,n);
								row.set(dataColumnIndex, opValue);

							} else {
								opValue = value;
							}
						} else {
							opValue = value;							
						}
					} else {
						opValue = getValueAt(PersonalizableTable.ROW_IDX_FILTER_VALUE, modelColumnIndex);
					}
					
					if (visualRowIndex == PersonalizableTable.ROW_IDX_FILTER_OP) {
						opCond = (String) value;
					}  else{
						opCond=(String)getValueAt(PersonalizableTable.ROW_IDX_FILTER_OP, modelColumnIndex);
					}
					final QuerySet set = multiQuerySet.getCurrentQuerySet();
					final Filter filter = new Filter(dataColumnIndex, 
							translateOp(opCond, opValue == null ? null
									: opValue.getClass()), opValue);
					if (!Basics.isEmpty(opCond) && !Basics.isEmpty(opValue)) {
						set.updateFilter(dataColumnIndex, filter);
					} else {
						set.removeFilter(dataColumnIndex);
					}
					multiQuerySet.updateCurrentQuerySet(set);
					updateMultiQueryText();
					
					if (visualRowIndex == table.ROW_IDX_FILTER_OP) {
						final int op = translateOp((String) value);
						filterable.setColumnOp(op, dataColumnIndex);
					}
					if (value != null
							&& value != filteringRow.get(dataColumnIndex)) {
						if (!Basics.isEmpty(filteringRow.get(dataColumnIndex))) {
							if (Basics.isEmpty(filterOperatorRow
									.get(dataColumnIndex))) {
								filteringRow.set(dataColumnIndex, null);
							}
						}
					}
					Object arrayOperators[] = ListRow
							.toObjectArray(filterOperatorRow);
					boolean operatorPresent = false;
					for (int i = 0; i < arrayOperators.length; i++) {
						if (arrayOperators[i] != null
								&& !Basics.isEmpty(arrayOperators[i])) {
							operatorPresent = true;
							break;
						}
					}
					if (operatorPresent || !Basics.isEmpty(value)) {
						doAutoFilter(visualRowIndex, modelColumnIndex);
					} else {
						boolean queryActive=getUngroupedModel().getDataSource().getFilteringContext()==PersonalizableDataSource.FILTER_CONTEXT_QUERY_PROPOSING;

						if (queryActive && dlg != null && dlg.isVisible() && !multiQuerySet.isFilterable()) {
							popupTableDoneButton.setEnabled(false);
							seeOnly.setEnabled(false);
							seeAll.setEnabled(false);
							dataSource.removeFilter();
							refreshShowingTable(true);
						} else {
							doAutoFilter(visualRowIndex, modelColumnIndex);
						}

					}
				}
				if (valueSettings != null) {
					valueSettings.add(new Integer(dataColumnIndex));
				}
			}
			if (currentlyActiveTabImporter == null) {
				final Component c = table.getEditorComponent();
				if (c instanceof JComponent) {
					final CellAdvice ca = new CellAdvice();
					setCellAdvice(visualRowIndex, modelColumnIndex, ca);
					if (ca.TYPE_ERROR == ca.type) {
						((JComponent) c).setToolTipText(ca.toolTip);
						ToolTipOnDemand.getSingleton().show((JComponent) c,
								false);
					}
				}
				if (!isSettingFilteringCellValues) {
					final Object now=row.getFilterableValue(dataColumnIndex);
					if (!Basics.equals(now, prior)){
						reSyncViewsToChanges(row, dataColumnIndex);
					}
				}
			}
		}
	}

	public static int translateOp(final String op) {
		if (Basics.isEmpty(op)) {
			return Filterable.none;
		}
		return Basics.indexOf(Filterable.allFilterOp, op);
	}

	public static int translateOp(final String operator, final Class cl) {
		final int op=translateOp(operator);
		if (op>Filterable.none || cl == null){
			return op;
		}
		return DefaultFilterable.getDefaultFilterOp(cl);
	}

	
	int focusVisualRowIndex=0;
	int focusModelColumnIndex=0;
	int focusChanges=0;
	
	public int getFocusVisualRowIndex(){
		return focusVisualRowIndex;
	}
	
	void refocus(final int visualRowIndex, final int modelColumnIndex){
		if (visualRowIndex!=focusVisualRowIndex || modelColumnIndex != focusModelColumnIndex){
			focusVisualRowIndex = visualRowIndex;
			focusModelColumnIndex = modelColumnIndex;
			focusChanges++;
		}	
	}
	
	public boolean isCellEditable(final int visualRowIndex,
			final int modelColumnIndex) {
		if (modelColumnIndex < 0 || visualRowIndex < 0) {
			return false;
		}
		final int dc = getDataColumnIndex(modelColumnIndex);
		final String name= metaRow.getDataColumnIdentifier(dc);
		final Row row = getRowAtVisualIndex(visualRowIndex);
		if (!columnsAlwaysAllowEditInPlace.contains(name) && row != null && !row.isActive()) {
			 ToolTipOnDemand.getSingleton().hideTipWindow();
			 if (!columnsAlwaysAllowEditInPlace.contains(name)) {
				 showDisabledText(Basics.toHtmlUncentered("Internal item",
						 "This item is used for internal purposes, parts of this item may not editable"), false, false);
			 }
			 else {
				 showDisabledText(Basics.toHtmlUncentered("Inactive Item",
						 PersonalizableTable.ANOMALY_INACTIVE), false, false);	 
			 }
			 
			return false;
		}
		refocus(visualRowIndex, modelColumnIndex);
		// System.out.println("focus=" + visualRowIndex + ", " +
		// modelColumnIndex);

		if (columnsAlwaysAllowEditInPlace.contains(name)) {
			final Object value = row == null ? null : (Object)row.get(dc);
			if(getSelectedRowsInTable().contains(row)){
				ComboCellEditor.considerSingleClick = true;
			}
			return isColumnsAlwaysAllowEditInPlaceEditable(false, row, visualRowIndex, dc, modelColumnIndex, value);
		}
		isUserPicking = true;
		boolean isEditable = isEditable(true, visualRowIndex, modelColumnIndex, false);
		if (showFilterUI && (visualRowIndex == 0 || visualRowIndex == 1)
				&& multiQuerySet != null && multiQuerySet.isMultiFilterSet()) {
			isEditable = false;
		}
		return isEditable;
	}
	
	private boolean isColumnsAlwaysAllowEditInPlaceEditable(final boolean forRenderingOnly,final Row row, 
			final int visualRowIndex, final int dc, final int modelColumnIndex, final Object value){
		if (row == null) {
			if (visualRowIndex == getRowCountMinusEmptyLastRowForCreating()) {
				return showingEmptyLastRowForCreating();
			}
			return false;
		}
		final boolean edit = row.isEditable(dc);
		if (edit && !forRenderingOnly) {
			/*if (isPickList) { //This is not true when executed on a pick list
				isUserPicking = false;*/ 
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fireColumnsAlwaysAllowEditInPlaceChanged(value);
					}
				});
//			}					
		}
		return edit;
	}

	boolean isEditable(final boolean alreadyTestedForIsActive, final int visualRowIndex,
			final int modelColumnIndex, final boolean isDoubleClick) {
		final int dc = getDataColumnIndex(modelColumnIndex);
		final Row row = getRowAtVisualIndex(visualRowIndex);
		if (!alreadyTestedForIsActive){
			final String name = metaRow.getDataColumnIdentifier(dc);
			if (columnsAlwaysAllowEditInPlace.contains(name)) {
				final Object value = row == null ? null : (Object)row.get(dc);
				return isColumnsAlwaysAllowEditInPlaceEditable(true, row, visualRowIndex, dc,
						modelColumnIndex, row);
			}
		}

		if (showFilterUI
				&& (visualRowIndex == table.ROW_IDX_FILTER_VALUE || visualRowIndex == table.ROW_IDX_FILTER_OP)) {
			final Class cl = metaRow.getClass(getDataColumnIndex(modelColumnIndex));
			return !(cl.equals(Object[][].class));
		}
		final Class cl = metaRow.getClass(dc);
		boolean isUrl = URL.class.equals(cl);
		if (isUrl) {
			return true;
		}
		if (JButton.class.equals(cl) && !isDoubleClick) {
			return row==null || (row != null && row.isEditable(dc));
		}
		if(File.class.equals(cl)) {
			return true;
		}
		if ((!readOnly && !isPickList && editInPlace) || isUrl) {
			if (row == null) {
				if (visualRowIndex == getRowCountMinusEmptyLastRowForCreating()) {
					return showingEmptyLastRowForCreating();
				}
				return false;
			}
			return row.isEditable(dc);
		}
		return false;
	}

	public static class Remedy {
		private final String txt;
		private final JButton remedy;

		public Remedy(final String txt, final JButton remedy) {
			this.txt = txt;
			this.remedy = remedy;
		}
	};

	boolean isPickList = false;

	public void setIsPickList(final boolean ok) {
		isPickList = ok;
	}

	Collection<String> getReadOnlyExplanation(final int visualRowIndex,
			final int visualColumnIndex) {
		if (showFilterUI
				&& (visualRowIndex == table.ROW_IDX_FILTER_VALUE || visualRowIndex == table.ROW_IDX_FILTER_OP)) {
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}
		if(visualColumnIndex < 0) {
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}
		final int dc = getDataColumnIndexFromVisualIndex(visualColumnIndex);
		final Class cl = metaRow.getClass(dc);
		boolean isUrl = URL.class.equals(cl);
		if (isUrl) {
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}
		final Collection<String> c = new ArrayList<String>();
		if (readOnly) {
			c.add(entireTableIsReadOnly);
		}
		if (!editInPlace) {
			final String txt = "The table's <b>'Enter/Edit directly in table</b> feature is off.";
			if (allowEditInPlaceControl) {
				c
						.add(txt
								+ "  Click right-click button (double down arrow) to turn this on.");
			} else {
				c.add(txt + "You can not turn it on.");
			}
		}
		final Row row = getRowAtVisualIndex(visualRowIndex);
		if (row == null) {
			if (visualRowIndex == getRowCountMinusEmptyLastRowForCreating()) {
				if (!showingEmptyLastRowForCreating()) {
					c.add("Last row is not for editing");
				} else {
					c.add("New row is uneditable");
				}
			}
		} else if (editInPlace && row instanceof Row.SelfExplanatory){
			c.addAll( ((Row.SelfExplanatory)row).getEditableAnomalies(dc));
		}
		return c;
	}

	private boolean forbidShowingEmptyLastRow = false;

	public void setShowingEmptyLastRowForCreating(final boolean ok) {
		forbidShowingEmptyLastRow = !ok;
	}

	public boolean showingEmptyLastRowForCreating() {
		return !forbidShowingEmptyLastRow
				&& (getUngroupedModel().dataSource.isCreatable() || getUngroupedModel().dataSource.isAddable())
				&& dataSource.getMaximumCardinality() != 1 && getEditInPlace();
	}

	private static final String PROPERTY_EDIT_IN_PLACE = "editInPlace";
	boolean editInPlace = false, allowEditInPlaceControl = true;

	public void setAllowEditInPlaceControl(final boolean ok) {
		allowEditInPlaceControl = ok;
	}
	
	public boolean getAllowEditInPlaceControl() {
		return allowEditInPlaceControl;
	}
	

	public boolean getEditInPlace() {
		return editInPlace;
	}

	public void setEditInPlace(final boolean ok) {
		editInPlace = ok;
	}

	private JCheckBoxMenuItem editInPlaceItem = new JCheckBoxMenuItem(
			"'Enter/Edit directly in table", MmsIcons.getTableEditIcon());
	
	static List clone(final Collection in) {
		final ArrayList out = new ArrayList(in.size());
		out.addAll(in);
		return out;
	}

	private boolean allowMultipleColumnModels = true;

	public void setAllowMultipleColumnModels(final boolean ok) {
		allowMultipleColumnModels = ok;
	}

	private boolean hideNewColumnsAfterPersonalizing=true;
			
	public List<String> getModelColumnIdentifiers() {
		final List<String> v = getStrings(PROPERTY_TABLE_COLUMN_MODEL,
				ungroupedModel!=null&&ungroupedModel.allowMultipleColumnModels);
		if (Basics.isEmpty(v)) {
			return metaRow.cloneDataColumnIdentifiers();
		} else {
			if (metaRow.containsAllIdentifiers(v) && v.size() == metaRow.size()) {
				return v;
			}
			final ArrayList<String> al = new ArrayList<String>();
			for (final String ss : v) {
				if (metaRow.indexOf(ss) >= 0) {
					al.add(ss);
				}
			}
			if (!hideNewColumnsAfterPersonalizing) {
				List<String> columns = metaRow.cloneDataColumnIdentifiers();
				for (String column: columns) {
					if (!al.contains(column)) {
						al.add(column);
					}
				}
			}
			return al.size() > 0 ? al : metaRow.cloneDataColumnIdentifiers();
		}
	}

	final PersonalizableDataSource dataSource;

	private Row filteringRow = null, filterOperatorRow = null; // for visual
	// presentation
	// purposes only
	// - not used by
	// filtering
	// logic

	private final DefaultFilterable filterable = new DefaultFilterable();
	final MultiQueryFilter multiQuerySet=new MultiQueryFilter(this);	
	private String logicOperator=null;
	
	SeeOnlyTokens seeOnlyTokens;
	
	public boolean clearSeeOnlySettings() {
		boolean ok=false;
		if (groupedDataSource != null){
			groupedDataSource.seeOnlyTokens=null;
		}
		
		if (multiQuerySet!=null && multiQuerySet.isFilterable()) {
			seeOnlyTokens=null;
			dataSource.removeFilter();
			refreshShowingTable(true);
			if (filterMessagePane != null){
				filterMessagePane.setText("");			
			}
			ok=true;
		} else if (seeOnlyTokens != null){
			seeOnlyTokens=null;
			dataSource.removeFilter();
			refreshShowingTable(true);
			ok=true;
		} 
		if (hasFilterValues()){
			removeFilter();
			ok=true;
		}
		return ok;
	}

	public void removeFilter() {
		for (int dataColumnIndex = 0; dataColumnIndex < metaRow.size(); dataColumnIndex++) {
			filteringRow.set(dataColumnIndex, null);
			filterable.setColumnOp(Filterable.none, dataColumnIndex);
			filterOperatorRow.set(dataColumnIndex, Filterable.opNone);
		}
		if(multiQuerySet!=null){
			multiQuerySet.clearFilters();
		}
	}

	public void filter(final int dataColumnIndex, final Object value,
			final String operation) {
		filteringRow.set(dataColumnIndex, value);
		final int op = translateOp(operation);
		filterable.setColumnOp(op, dataColumnIndex);
		filterOperatorRow.set(dataColumnIndex, operation);
	}

	public boolean autoFilter = false;

	public void setAutoFilter(boolean autoFilter) {
		this.autoFilter = autoFilter;
		applyFilterItem.setVisible(!autoFilter);
	}
	
	public String[] getColumnValues(String columnName) {
		List<Row> dataRows = dataSource.getFilteredDataRows();
		Set<String> columnValues = new HashSet<String>();
		for (Row r: dataRows) {
			columnValues.add((String)r.get(getDataColumnIndexForIdentifier(columnName)));
		}
		return columnValues.toArray(new String[0]);
	}

	private void doAutoFilter(final int visualRowIndex,
			final int modelColumnIndex) {
		if (autoFilter) {
			if (visualRowIndex == table.ROW_IDX_FILTER_VALUE
					|| visualRowIndex == table.ROW_IDX_FILTER_OP) {
				final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
				final Object filtering = filteringRow.get(dataColumnIndex);

				Object filterOp = filterOperatorRow.get(dataColumnIndex);
				if (Basics.isEmpty(filterOp)
						&& visualRowIndex == table.ROW_IDX_FILTER_VALUE) {
					final Class cl = TableBasics.reinterpretClass(metaRow,
							getDataColumnIndex(modelColumnIndex),
							getColumnClass(modelColumnIndex));
					final String[] op = table.getOps(cl);
					if (Basics.indexOf(op, Filterable.opStartsWith) > -1) {
						filterOp = Filterable.opStartsWith;
					} else if(isAddQueryReset){
						filterOp = Filterable.opNone;
					}else {
						filterOp = Filterable.opEquals;
					}
					setValueAt(filterOp, table.ROW_IDX_FILTER_OP,
							modelColumnIndex);
				} else {
					syncFilter(true);
					SwingBasics.getVisualIndexFromModelIndex(table,
							modelColumnIndex);
					table.scrollToVisible(visualRowIndex, modelColumnIndex);
				}
			}
		}
	}

	private boolean hasFilterValues() {
		boolean retVal = false;
		if (dataSource.isFilterable()) {
			for (int dataColumnIndex = 0; !retVal
					&& dataColumnIndex < metaRow.size(); dataColumnIndex++) {
				Object filtering = filteringRow.get(dataColumnIndex);
				if (!Basics.isEmpty(filtering)) {
					retVal = true;
				}
			}
		}
		return retVal;
	}

	public boolean isFilterable(){
		return dataSource.isFilterable();
	}
	private boolean hasFilterOps() {
		boolean retVal = false;
		if (dataSource.isFilterable()) {
			for (int dataColumnIndex = 0; !retVal
					&& dataColumnIndex < metaRow.size(); dataColumnIndex++) {
				Object filterOp = filterOperatorRow.get(dataColumnIndex);
				if (!Basics.isEmpty(filterOp)) {
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	private void filterFurtherBySeeOnlyTokens(final SeeOnlyTokens seeOnlyTokens){
		if (seeOnlyTokens!=null){
			seeOnlyTokens.initDataColumnIndexes();
			final List<Row>old=dataSource.getFilteredDataRows(), nw=new ArrayList<Row>();
			for (final Row row:old){
				if (seeOnlyTokens.matches(row)){
					nw.add(row);
				}
			}
			dataSource.setFilter(nw);
		}
	}
	
	private boolean refilterOrRemove(final boolean remove){
		final boolean ok;
		if (hasSingleQueryViaCurrentFilterRows() && !hasMultiQuerySet()) {
			dataSource.filter(filteringRow, filterable);
			filterFurtherBySeeOnlyTokens(seeOnlyTokens);
			if (groupedDataSource != null && this.treeHasSeeOnlyAbility){
				filterFurtherBySeeOnlyTokens(groupedDataSource.seeOnlyTokens);
			}
			ok=true;
		} else if (hasMultiQuerySet()) {
			dataSource.filter(filterable,multiQuerySet);
			filterFurtherBySeeOnlyTokens(seeOnlyTokens);
			if (groupedDataSource != null && this.treeHasSeeOnlyAbility){
				filterFurtherBySeeOnlyTokens(groupedDataSource.seeOnlyTokens);
			}
			ok=true;
		} else if (seeOnlyTokens != null){
			dataSource.filter(seeOnlyTokens);
			if (groupedDataSource != null){
				filterFurtherBySeeOnlyTokens(groupedDataSource.seeOnlyTokens);
			}
			ok=false;
		} else if (groupedDataSource != null && this.treeHasSeeOnlyAbility && groupedDataSource.seeOnlyTokens != null){
			dataSource.setFilter(dataSource.getDataRows());
			filterFurtherBySeeOnlyTokens(groupedDataSource.seeOnlyTokens);
			dataSource.applyExternalFilters();
			ok=false;
		}else {		
		
			if (remove){
				seeOnlyTokens=null;
				dataSource.removeFilter();				
			}
			ok=false;
		}
		if (table!=null){
			table.resetAsNotFound();
		}
		return ok;
	}
	
	boolean hasSeeOnlySettings() {
		return hasMultiQuerySet() || hasSingleQueryViaCurrentFilterRows();
	}
	
	public boolean hasSingleQueryViaCurrentFilterRows(){
		return hasFilterOps() && hasFilterValues();
	}
	
	private boolean hasMultiQuerySet() {
		if(multiQuerySet!=null){
			return multiQuerySet.isFilterable();
		}
		return false;
	}

	public void handleRemovedColumn(final int dataColumnIndex) {
		hideColumn(dataColumnIndex);
		unsort(dataColumnIndex);
		final SortInfo sis[] = getAllSortInfo();
		for (final SortInfo si : sis) {
			if (si.dataColumnIndex > dataColumnIndex) {
				si.dataColumnIndex--;
			}
		}
		for (int i = 0; i < modelToDataColumnIndexes.length; i++) {
			if (modelToDataColumnIndexes[i] > dataColumnIndex) {
				modelToDataColumnIndexes[i]--;
			}
		}
		if (!isCompanionTable) {
			recentTrees.remove(dataColumnIndex);
		}
	}

	public void handleDataColumnChange() {
		if (dataSource.isFilterable()) {
			final int option = getGroupOption();
			if (option != NO_GROUPING && groupSeivedModel != null) {
				groupSeivedModel.initFilterRows();
				groupSeivedModel.table.tcr = null;
			}
			initFilterRows();
			table.tcr = null;
		}

	}

	private void initFilterRows() {
		filteringRow = dataSource.getBlankFilter();
		final ArrayList<String> al = new ArrayList<String>();
		filterOperatorRow = null;
		for (int dataColumnIndex = 0; dataColumnIndex < metaRow.size(); dataColumnIndex++) {
			final String identifier = metaRow
					.getDataColumnIdentifier(dataColumnIndex);
			final String stringOp = getProperty(identifier + "."
					+ PROPERTY_FILTER_OP, Filterable.opNone);
			al.add(stringOp);
			final String stringValue = getProperty(identifier + "."
					+ PROPERTY_FILTER_VALUE, null);
			setFilter(dataColumnIndex, stringOp, stringValue);
		}
		filterOperatorRow = new ListRow(al);
	}

	public boolean reapplyFilter() {
		if (dataSource.isFilterable()) {
			return refilterOrRemove(false);
		}
		return false;

	}

	private void initRowFiltering() {
		if (dataSource.isFilterable()) {
			showFilterUI = dataSource.getMaximumCardinality() > 1;
			initFilterRows();
			refilterOrRemove(true);
		}else {
			dataSource.removeFilter();
		}
	}

	String sizeInfo(final int subset, final int superSet) {
		final StringBuilder sb = new StringBuilder(sizeInfoPrefix);
		sb.append(subset);
		sb.append(" of ");
		sb.append(superSet);
		sb.append(" ");
		sb.append(sizeInfoSuffix == null ? key : sizeInfoSuffix);
		return sb.toString();
	}

	private String sizeInfo() {
		return sizeInfo(dataSource.getFilteredDataRows().size(), dataSource
				.size());
	}

	private final void refreshSizeInfo() {
		if (table != null ) {
			// DREPC
			if (table.sizeInfo != null) {
				final String s = sizeInfo();
				table.sizeInfo.setText(s);
			}
			table.refreshBackground();
		}
	}

	public void setEmptyView() {
		dataSource.filterOutAllRows();
		refreshShowingTable(false);
	}

	boolean suppressRefresh = false;

	public void refresh() {
		refresh(true);
	}


	public void refresh(final boolean refreshSelect) {
		if (!suppressRefresh) {
			if (groupedDataSource != null){
				GroupedDataSource.remember=groupedDataSource.seeOnlyTokens;
			}
			final FocusFreeze ff = new FocusFreeze();

			reTabIfNecessary();
			final int option = getGroupOption();
			if (option == NO_GROUPING || getSort().length==0) {
				if (option != NO_GROUPING && groupedDataSource != null){
					SwingBasics.switchContaineesWithinContainer(groupedDataSource .splitPane,
                            table.scrollPane);
					groupOption=NO_GROUPING;
					group(null, null);
				}
				refreshTable(true, refreshSelect);
			} else {
				final TreePath[] tp;
				if (reselectPriorSelectedTreeNodesWhenRegrouping) {
					tp = getSelectedTreePaths();
				} else {
					tp = null;
				}
				final PersonalizableTableModel utm = getUngroupedModel();
				if (utm.columnsToSort.size() > 0) {
					utm.sort(false);
				}
				if (utm.specialSortOrder == null) {
					group();
				} else {
					group(utm.specialSortOrder, utm.columnsForGroupSeive,
							utm.distinctRowsForGroupSeive);
				}

				if (refreshSelect && tp != null && tp.length > 0) {
					reselectTreeLater(tp);
				}
			}
			ff.restorePrevValue();
			resetPending = false;
			GroupedDataSource.remember=null;
		}
	}
	
	private int findAndSelectAndExpandIfLevel(final Collection<TreePath> toDo,
			final int level, final Collection<TreePath> done, final int limit) {
		int cnt = 0;
		final Collection<TreePath> _done = new ArrayList<TreePath>();
		final int n1 = toDo.size(), n2 = done.size();
		if (n1 > 0) {
			for (final TreePath tp : toDo) {
				if (tp.getPathCount() == level) {
					boolean isChildOfDone = false;
					for (final TreePath _tp : done) {
						if (_tp.isDescendant(tp)) {
							isChildOfDone = true;
							_done.add(tp);
							break;
						}
					}
					if (!isChildOfDone) {
						final Object[] os = SwingBasics.getPath(tp);
						final String[] s = new String[os.length];
						for (int j = 0; j < os.length; j++) {
							s[j] = os[j].toString();
						}
						final ArrayList<TreePath> al = SwingBasics.find(
								groupedDataSource.tree, s);
						if (al.size() > 0) {
							final TreePath tp2 = al.iterator()
									.next();
							groupedDataSource.tree.addSelectionPath(tp2);
							groupedDataSource.tree.expandPath(tp2);
							_done.add(tp);

							cnt++;
							if (limit > 0 && _done.size() >= limit) {
								break;
							}
						}
					}
				}
			}
			for (final TreePath tp : _done) {
				toDo.remove(tp);
				done.add(tp);
			}
		} else {
			cnt = 0;
		}
		return cnt;
	}
	
	private boolean supressUpdateUI = false;
	private boolean hasUpdatePending = false;

	public void updateUILater() {
		if (!hasUpdatePending) {
			if (!supressUpdateUI) {
				hasUpdatePending = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hasUpdatePending = false;
						updateUI();
					}
				});
			}
		} else {
			int debug = 2;
		}
	}

	private void updateUI() {
		final int option = getGroupOption();
		if (option == NO_GROUPING) {
			table.updateUI();
			table.getTableHeader().updateUI();
			columnFreezer.repaint();
		} else {
			final JTree tree = getTree();
			if (tree != null) {
				updateTreeUI();
				groupSeivedModel.table.updateUI();
				groupSeivedModel.table.getTableHeader().updateUI();
				groupSeivedModel.columnFreezer.repaint();
			}
		}
	}

	public void repaint() {
		final int option = getGroupOption();
		if (option == NO_GROUPING) {
			refreshShowingTable(true);
		} else {
			final JTree tree = getTree();
			if (tree != null) {
				// freaking repaint voodoo
				final Dimension dimension = tree.getSize();
				tree.setSize(dimension.width + 1, dimension.height);
				tree.invalidate();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tree.setSize(dimension);
						tree.repaint();
					}
				});
			}
			if (groupSeivedModel != null) {
				groupSeivedModel.refreshShowingTable(true);				
			}
		}
	}

	public void refreshShowingTable(final boolean resort) {
		refreshTable(resort, true);
	}

	private boolean hasTableUpdateUIPending = false;

	private int refreshTable(final boolean resort,
			final boolean refreshSelect) {
		refreshSizeInfo();
		setRowCount(getRowCountMinusEmptyLastRowForCreating());
		if (resort && columnsToSort.size() > 0) {
			sort(refreshSelect);
		}
		if (table != null
				&& !table.supressUpdateUIBecauseItKillsCursorPositionAndOtherStuff) {
			if (!hasTableUpdateUIPending) {
				if (!supressUpdateUI) {
					hasTableUpdateUIPending = true;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							table.updateUI();
							columnFreezer.updateUI();
							hasTableUpdateUIPending = false;
						}
					});
				}
			}
		}
		final List<Row> filteredDataList = dataSource.getFilteredDataRows();
		return filteredDataList == null ? 0 : filteredDataList.size();
	}

	public int getDataRowCount() {
		return dataSource.getFilteredDataRows().size();
	}

	int getRowCountMinusEmptyLastRowForCreating() {
		final List l = dataSource.getFilteredDataRows();
		final int n = l == null ? 0 : l.size();
		return showFilterUI ? n + 2 : n;
	}

	public int getSize() {
		return dataSource.getDataRows().size();
	}

	public boolean isEmpty() {
		return dataSource.getFilteredDataRows().size() == 0;
	}

	String _propertyPrefix;

	public void setPropertyPrefix(final String prefix) {
		final boolean showOneMomentDisplay = false;
		final PersonalizableTableModel um = getUngroupedModel();

		// FIRST we must remember everything that the user liked using the
		// current property prefix
		um.updatePropertiesWithPersonalizations(true);
		um.dataSource.setPropertyPrefix(prefix);
		um.ungroupIfNecessary(false);
		if (showOneMomentDisplay) {
			showOneMomentDisplay();
		}
		um.resetFromProperties();
		um.groupIfNecessary();
		um.refreshShowingTable(true);
		if (showOneMomentDisplay) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					hideOneMomentDisplay();
				}
			});
		}
	}

	private String defaultPropertyPrefix;

	public void setDefaultPropertyPrefix(final String defaultPropertyPrefix) {
		this.defaultPropertyPrefix = defaultPropertyPrefix;
	}

	private static final String getPropertyPrefix(
			final PersonalizableTableModel ungrouped) {
		return ungrouped.dataSource.getPropertyPrefix();
	}

	private String getPropertyPrefix(final String ungroupedPrefix) {
		if (ungroupedPrefix == null) {
			return _propertyPrefix;
		}
		return ungroupedPrefix + "." + _propertyPrefix;
	}

	PersonalizableTableModel ungroupedModel = null;

	private String getPropertyName(final String prefix, final String name) {
		if (prefix == null) {
			return name;
		}
		return prefix + "." + name;

	}

	public String getUngroupedDataSourcePropertyPrefix() {
		final PersonalizableTableModel ptm = getUngroupedModel();
		return ptm.dataSource.getPropertyPrefix();
	}

	public final String getPropertyPrefix() {
		final PersonalizableTableModel ptm = getUngroupedModel();
		final String ungroupedPrefix = ptm.dataSource.getPropertyPrefix();
		if (ptm == this) {
			return ungroupedPrefix;
		}
		return getPropertyPrefix(ungroupedPrefix);
	}

	String getPropertyName(final String name) {
		if (ungroupedModel == null) {
			return getPropertyName(getPropertyPrefix(), name);
		}
		return getPropertyName(
				getPropertyPrefix(getPropertyPrefix(ungroupedModel)), name);
	}

	boolean forbidDeletions = false;

	public void setDeletionsAllowed(final boolean allowDeletions) {
		forbidDeletions = !allowDeletions;
	}

	boolean readOnly = false;

	public void setReadOnly(final boolean isReadOnly) {
		forbidDeletions = isReadOnly;
		readOnly = isReadOnly;
		popup=null;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	boolean favoritesSelection = false;

	public void setFavoritesSelection(final boolean isFavoritesSelection) {
		favoritesSelection = isFavoritesSelection;
	}

	public boolean isFavoritesSelection() {
		return favoritesSelection;
	}

	private int dataColumnIndexToExplore;
	private Row dataRowToExplore;

	private boolean canExplore() {
		final boolean retVal;
		if (isRightClickDoingThePoppingUp){
			dataRowToExplore = getMouseFocusRow();
		} else {
			dataRowToExplore = getDataRowAtVisualIndex(focusVisualRowIndex);
		}
		if (dataRowToExplore != null) {
			if (isRightClickDoingThePoppingUp){
				dataColumnIndexToExplore = clickedDataColumnIndex;
			} else {
				dataColumnIndexToExplore=getDataColumnIndex(focusModelColumnIndex);
			}
			if (dataColumnIndexToExplore<0){
				dataColumnIndexToExplore=getFocusDataColumnIndex();
			}
			if (dataColumnIndexToExplore >= 0) {
				retVal = dataRowToExplore
						.isExplorable(dataColumnIndexToExplore);
			} else {
				retVal = false;
			}
		} else {
			retVal = false;
		}
		return retVal;
	}

	private void explore() {
		if (dataRowToExplore == null) {
			dataRowToExplore = getRowAtVisualIndex(focusVisualRowIndex);
			dataColumnIndexToExplore = getDataColumnIndex(focusModelColumnIndex);
		}
		if (dataRowToExplore != null) {
			dataRowToExplore.explore(dataColumnIndexToExplore);
			dataRowToExplore = null;
		}
	}

	final MetaRow metaRow;
	private boolean isCompanionTable = false;
	public boolean isAutoTreeResizeNeeded = false;
	public static boolean ignorePreviousGrouping = false;

	public PersonalizableTableModel(final PersonalizableDataSource dataSource,
			final Properties properties, final boolean readOnly) {
		this(dataSource, properties, readOnly, false);
	}

	public PersonalizableTableModel(final PersonalizableDataSource dataSource,
			final Properties properties, final boolean readOnly,
			final boolean isCompanionTable,final boolean hideNewColumnsAfterPersonalizing) {
		this.isCompanionTable = isCompanionTable;
		this.readOnly = readOnly;
		this.hideNewColumnsAfterPersonalizing=hideNewColumnsAfterPersonalizing;
		forbidDeletions = readOnly;
		this.dataSource = dataSource;
		metaRow = dataSource.getMetaRow();
		initialize(dataSource, properties);
	}

	private void initialize(final PersonalizableDataSource dataSource,
			final Properties properties) {
		
		setProperties(properties);
		setAutoFilter(true); // by default
		viewRefresh.setHowToUndoMsg(
				Basics.concat(
						"select ", 
						AutoComplete.encodeImg("find16.gif"), 
						" on the table's tool bar, and then select the <b>last</b> menu item."));
	}
	
	public PersonalizableTableModel(final PersonalizableDataSource dataSource,
			final Properties properties, final boolean readOnly,
			final boolean isCompanionTable) {
		this.isCompanionTable = isCompanionTable;
		this.readOnly = readOnly;
		forbidDeletions = readOnly;
		this.dataSource = dataSource;
		metaRow = dataSource.getMetaRow();
		initialize(dataSource, properties);
	}

	public void setProperties(final String fileName) {
		setProperties(PropertiesBasics.loadProperties(fileName));
	}

	public void setProperties(final Properties properties) {
		this.properties = properties;
		resetFromProperties();
	}

	public String getDataColumnIdentifier(final int dataColumnIndex) {
		return metaRow.getDataColumnIdentifier(dataColumnIndex);
	}

	private void resetFromProperties() {
		initRowFiltering();
		final PersonalizableTableModel ptm = getUngroupedModel();
		ptm.renamedLabelsByIdentifier.clear();
		String ungroupedPrefix = ptm.dataSource.getPropertyPrefix(), defaultPropertyPrefix = ptm.defaultPropertyPrefix;
		final boolean isUngroupedPrefix = Basics.equals(ungroupedPrefix,
				defaultPropertyPrefix);
		for (int i = 0; i < metaRow.size(); i++) {
			final String name = metaRow.getDataColumnIdentifier(i);
			final String name1 = name + "." + PROPERTY_HEADER_LABEL;
			String name2 = getPropertyName(ungroupedPrefix, name1);
			String label = properties.getProperty(name2, null);
			if (label != null) {
				ptm.renamedLabelsByIdentifier.put(name, label);
			} else if (!isUngroupedPrefix) {
				name2 = getPropertyName(defaultPropertyPrefix, name + "."
						+ PROPERTY_HEADER_LABEL);
				label = properties.getProperty(name2, null);
				if (label != null) {
					ptm.renamedLabelsByIdentifier.put(name, label);
				}
			}
		}
		initSortInfo();
		final List<String>l=getModelColumnIdentifiers();
		initModelColumns(l);
		useDittos = getProperty(PROPERTY_USE_DITTOS, false);
		if (ignorePreviousGrouping) {
			groupOption = NO_GROUPING;
		}
		else {
			groupOption = getProperty(PROPERTY_GROUP, NO_GROUPING);
		}
		if (dataSource.isFilterable()) {
			showFilterUI = dataSource.getMaximumCardinality() < 2 ? false
					: getProperty(PROPERTY_SHOW_FILTER_UI, true);
			if (showFilterUI && firstSettingOfFilterUI) {
				focusVisualRowIndex = 2;
				firstSettingOfFilterUI = false;
			}
		}
		useDisabling = getProperty(PROPERTY_DISABLE, true);
		final int type = getModelType();
		if (!isCompanionTable) {
			recentTrees = new TreeShapes(false, InputEvent.ALT_MASK,
					PROPERTY_RECENT_TREE_IDX, PROPERTY_RECENT_TREE_IDS,
					PROPERTY_RECENT_TREE_ASCENDING);
			favoriteTrees = new TreeShapes(true, InputEvent.CTRL_MASK,
					PROPERTY_FAVORITE_TREE_IDX, PROPERTY_FAVORITE_TREE_IDS,
					PROPERTY_FAVORITE_TREE_ASCENDING);
			if (table != null && isTreeOn) {
				recentTrees.registerActions(table, recentTreeMenu);
				favoriteTrees.registerActions(table, favoriteTreeMenu);
			}

		}
		if (allowEditInPlaceControl) {
			if (readOnly) {
				editInPlace = false;
			} else {
				editInPlace = getProperty(PROPERTY_EDIT_IN_PLACE, editInPlace);
			}
		}
		freeze(getStrings(PROPERTY_ROW_HEADER, true));
		
	}
	
	public void freeze(final List<String> fr) {
		if (fr.size() > 0 || columnFreezer.l.size() > 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									columnFreezer.setView(fr);
								}
							});
						}
					});
				}
			});
		}

	}

	public void setCanPickDisabled(final boolean ok) {
		canPickDisabled = ok;
	}

	boolean canPickDisabled = false;

	public int getRowCount() {
		if (dataSource == null) {
			return super.getRowCount();
		}
		final int n = getRowCountMinusEmptyLastRowForCreating();
		if (showingEmptyLastRowForCreating()) {
			return n + 1;
		}
		return n;
	}

	public int getColumnCount() {
		return modelColumnIdentifiers == null ? super.getColumnCount()
				: modelColumnIdentifiers.size();
	}

	public boolean isSortable(final int modelColumnIndex) {
		final int dc = getDataColumnIndex(modelColumnIndex);
		if (dc >= 0) {
			Class cl = metaRow.getClass(dc);
			final SortValueReinterpreter svr = metaRow
					.getSortValueReinterpreter(dc);
			if (svr != null) {
				cl = svr.reinterpret(cl);
			}
			return Comparable.class.isAssignableFrom(cl);
		}
		return false;
	}

	public int getFilteredDataRowIndex(final int visualRowIndex) {
		final int dataRowIndex;
		if (showFilterUI) {
			dataRowIndex = visualRowIndex - 2;
		} else {
			dataRowIndex = visualRowIndex;
		}
		if (dataRowIndex >= dataSource.getFilteredDataRows().size()) {
			return -1;
		}
		return dataRowIndex;
	}
	
	private int[] getFilteredDataRowsIndex(
			int[] selected) {
		int[] indexes = null;  
		if(selected != null){
			indexes = new int[selected.length];
			for(int i=0; i<selected.length; i++){
				int visualRowIndex = selected[i];
				indexes[i] = getFilteredDataRowIndex(visualRowIndex);
			}
		}
		return indexes;
	}

	public int getVisualRowIndex(final int filteredDataRowIndex) {
		if (showFilterUI) {
			return filteredDataRowIndex + 2;
		}
		return filteredDataRowIndex;
	}

	/**
	 *
	 * @param visualRowIndex
	 *            int
	 * @return Row, could be true row from data source or the filtering or
	 *         filtered row
	 */
	public Row getRowAtVisualIndex(final int visualRowIndex) {
		Row row = null;
		if (visualRowIndex >= 0) {
			final List<Row> filteredDataList = dataSource.getFilteredDataRows();

			if (showFilterUI) {
				if (visualRowIndex == table.ROW_IDX_FILTER_VALUE) {
					row = filteringRow;
				} else if (visualRowIndex == table.ROW_IDX_FILTER_OP) {
					row = filterOperatorRow;
				} else if (visualRowIndex - 2 < filteredDataList.size()) {
					row = ((Row) filteredDataList.get(visualRowIndex - 2));
				}
			} else if (visualRowIndex < filteredDataList.size()) {
				row = ((Row) filteredDataList.get(visualRowIndex));
			}
		}
		return row;
	}

	int getVisualIndexOfRow(final Row row) {
		final int n = getRowCount();
		for (int i = 0; i < n; i++) {
			final Row _row = getRowAtVisualIndex(i);
			if (Basics.equals(row, _row)) {
				return i;
			}
		}
		return -1;
	}

	private final Object getDataValue(final int visualRowIndex,
			final int dataColumnIndex) {
		final Row row = getRowAtVisualIndex(visualRowIndex);
		if (row != null) {
			Object value = row.get(dataColumnIndex);
			if (value instanceof SelectableCell) {
				value = value.toString();
			}
			return value;
		}
		return null;
	}

	private final boolean isPriorValueTheSame(final int visualRowIndex,
			final int dataColumnIndex) {
		final Object prevValue = getDataValue(visualRowIndex - 1,
				dataColumnIndex);
		final Object curValue = getDataValue(visualRowIndex, dataColumnIndex);
		return Basics.equals(prevValue, curValue);
	}

	public boolean useDittos(final int visualRowIndex,
			final int modelColumnIndex) {
		final boolean value;
		if (useDittos) {
			final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
			if (visualRowIndex > 0) {
				final SortInfo[] si = getAllSortInfo();
				if (si.length == 0) {
					value = isPriorValueTheSame(visualRowIndex, dataColumnIndex);
				} else {
					int i = 0;
					for (; i < si.length; i++) {
						if (si[i].dataColumnIndex == dataColumnIndex) {
							break;
						}
					}
					if (i==si.length){
						value = isPriorValueTheSame(visualRowIndex, dataColumnIndex);
					} else {
						boolean allTheSame = true;
						for (int j = 0; allTheSame && j < i; j++) {
							allTheSame = isPriorValueTheSame(visualRowIndex,
									si[j].dataColumnIndex);
						}
						if (allTheSame) {
							value = isPriorValueTheSame(visualRowIndex,
									dataColumnIndex);
						} else {
							value = false;
						}
					}
				}
			} else {
				value = false;
			}
		} else {
			value = false;
		}
		return value;
	}

	public Object getRenderOnlyValue(final int visualRowIndex,
			final int modelColumnIndex, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
		final Row row = getRowAtVisualIndex(visualRowIndex);
		final Object v;
		if (row==null){
			v=dataSource.getRenderOnlyValueForLastRow(dataColumnIndex, isSelected, hasFocus);
		}else{
			v= row.getRenderOnlyValue(dataColumnIndex, isSelected, hasFocus);
		}
		return v == null ? value : v;
	}

	public int getDataColumnIndex(final int modelColumnIndex) {
		if (modelColumnIndex < 0
				|| modelColumnIndex >= modelColumnIdentifiers.size()) {
			return -1;
		} else if (modelToDataColumnIndexes != null) {
			return modelToDataColumnIndexes[modelColumnIndex];
		}
		return metaRow.indexOf(modelColumnIdentifiers.get(modelColumnIndex));
	}
	public int rowId = -1;
	public int colId = -1;
	public Object getValueAt(final int visualRowIndex,	final int modelColumnIndex) {
		rowId = visualRowIndex;
		colId = modelColumnIndex;
		return _getValueAt(visualRowIndex, getDataColumnIndex(modelColumnIndex));
	}

	public String getColumnName(final int modelColumnIndex) {
		final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
		return getColumnLabel(dataColumnIndex);
	}

	private class PopupRow {
		private final JDialog dlg;
		private PopupRow(final Row row) {
			dlg=SwingBasics.getDialog(getTearAwayComponent());
			dlg.setModal(true);
			final JPanel mainPanel = new JPanel(new BorderLayout());
			dlg.getContentPane().add(mainPanel);
			JPanel namePanel = new JPanel(), valuePanel = new JPanel();
			int n = metaRow.size();
			Font headFont = table.getTableHeader().getFont(), labelFont;
			labelFont = new Font(headFont.getFontName(), Font.ITALIC, PersonalizableTable.FONT_FILTER.getSize());
			headFont = new Font(headFont.getFontName(), Font.ITALIC | Font.BOLD
					| Font.HANGING_BASELINE, 14);
			namePanel.setLayout(new GridLayout(n + 2, 1));
			valuePanel.setLayout(new GridLayout(n + 2, 1));
			JLabel jl = new JLabel("Showing");
			jl.setFont(headFont);
			namePanel.add(jl);
			jl = new JLabel("");
			jl.setFont(headFont);
			valuePanel.add(jl);
			int numShowing = modelColumnIdentifiers.size();
			for (int modelColumnIndex = 0; modelColumnIndex < numShowing; modelColumnIndex++) {
				final int dataColumnIndex = getDataColumnIndex(modelColumnIndex);
				if (dataColumnIndex >= 0) {
					jl = new JLabel(getColumnLabel(dataColumnIndex));
					jl.setFont(labelFont);
					namePanel.add(jl);
					jl = new JLabel("" + row.get(dataColumnIndex));
					valuePanel.add(jl);
				}
			}
			jl = new JLabel(numShowing == n ? "(nothing hidden)" : "Hiding");
			jl.setFont(headFont);
			namePanel.add(jl);
			jl = new JLabel("");
			jl.setFont(headFont);
			valuePanel.add(jl);
			for (int dataColumnIndex = 0; dataColumnIndex < n; dataColumnIndex++) {
				final String name = (String) metaRow
						.getDataColumnIdentifier(dataColumnIndex);
				if (!modelColumnIdentifiers.contains(name)) {
					jl = new JLabel(getColumnLabel(dataColumnIndex));
					jl.setFont(labelFont);
					namePanel.add(jl);
					jl = new JLabel("" + row.get(dataColumnIndex));
					valuePanel.add(jl);
				}
			}

			JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					namePanel, valuePanel);
			mainPanel.add(new JScrollPane(sp), BorderLayout.CENTER);
			final JPanel buttons = SwingBasics.getButtonPanel(1);
			final JButton done = SwingBasics.getDoneButton(dlg);
			buttons.add(done);
			SwingBasics.layout(false, mainPanel, (JLabel) null, null, buttons,
					null);
			SwingBasics.packAndPersonalize(dlg, null,
					PopupBasics.PROPERTY_SAVIOR, "doubleClick", true, false,
					false);
			// show(); deprecated, use instead:
			dlg.setVisible(true);
		}
	};

	private final HashMap<String, String> renamedLabelsByIdentifier = new HashMap<String, String>();

	private String _getColumnLabel(final int dataColumnIndex, final boolean stripHtml) {
		if (dataColumnIndex < 0 || dataColumnIndex >= metaRow.size()) {
			return null;
		}
		if (columnRenderer!=null){
			final String pluginLabel= columnRenderer.getColumnLabel(PersonalizableTableModel.this, dataColumnIndex);
			if (pluginLabel!=null){
				return stripHtml?Basics.stripSimpleHtml(pluginLabel):pluginLabel;
			}
		} 
		final String identifier = metaRow.getDataColumnIdentifier(dataColumnIndex);
		final HashMap<String, String> rlbn = (ungroupedModel == null) ? renamedLabelsByIdentifier
				: ungroupedModel.renamedLabelsByIdentifier;

		final String label = rlbn.containsKey(identifier) ? rlbn.get(identifier) : metaRow
				.getLabel(dataColumnIndex);
		if (!Basics.isEmpty(label)){
			if (Basics.equals(identifier, label) && globalLabels.containsKey(identifier)){
				return globalLabels.get(identifier);
			}
			if (stripHtml){
				return Basics.stripSimpleHtml(label);
			} else {
				return label;
			}
		} else {
			if (globalLabels.containsKey(identifier)){
				return globalLabels.get(identifier);
			}
			return identifier;
		}
	}

	public String getColumnAbbreviation(final int dataColumnIndex) {
		return _getColumnLabel(dataColumnIndex, true);
	}

	public int getDataColumnIndexFromVisualIndex(final int visualColumnIndex) {
		final int modelColumnIndex = SwingBasics.getModelIndexFromVisualIndex(
				table, visualColumnIndex);
		return getDataColumnIndex(modelColumnIndex);
	}

	public String getFirstColumnLabel() {
		return getColumnAbbreviation(getDataColumnIndexFromVisualIndex(0));
	}

	public String getColumnLabel(final int dataColumnIndex) {
		return _getColumnLabel(dataColumnIndex, false);
	}
	
	public int getDataColumnIndexForIdentifier(final String dataColumnIdentifier){
		return metaRow.indexOf(dataColumnIdentifier);
	}
	
	public int getDataColumnIndexForLabel(final String label) {
		int idx = metaRow.indexOf(label);
		if (idx < 0) {
			final HashMap<String, String> rlbn = (ungroupedModel == null) ? renamedLabelsByIdentifier
					: ungroupedModel.renamedLabelsByIdentifier;

			for (final Iterator it = rlbn.keySet().iterator(); idx < 0
					&& it.hasNext();) {
				final String name = (String) it.next();
				final String label2 = rlbn.get(name);
				if (label2.equals(label)) {
					idx = metaRow.indexOf(name);
				}
			}
			if (idx < 0) {
				for (int i = 0; idx < 0 && i < metaRow.size(); i++) {
					final String s = metaRow.getLabel(i);
					if (Basics.equals(s, label)) {
						idx = i;
					}

				}
			}
		}
		return idx;
	}

	private String sizeInfoPrefix = "Viewing ", sizeInfoSuffix = null;

	// JLabel sizeInfo;
	public void setSizeInfo(final JLabel label) {
		setSizeInfo(label, "Viewing ", null);
	}

	public void setSizeInfo(final JLabel label, final String prefix,
			final String suffix) {
		sizeInfoPrefix = prefix;
		sizeInfoSuffix = suffix;
		if (label != null) {
			label.setText(sizeInfo());
		}
		if (table != null) {
			table.sizeInfo = label;
		}
	}

	public void setDefaultColumnWidth(final int dataColumnIndex, final int width) {
		final String identifier = metaRow.getDataColumnIdentifier(dataColumnIndex);
		final String personalizedWidth = getProperty(Basics.concat(identifier, ".",
				PROPERTY_HEADER_WIDTH), null);
		if (personalizedWidth == null) {
			final int visualColumnIndex = getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
			if (visualColumnIndex >= 0) {
				final TableColumn tc = table.tcm.getColumn(visualColumnIndex);
				tc.setPreferredWidth(width);
				tc.setResizable(true);
			}
		}
	}

	
	public void setColumnWidth(final int dataColumnIndex, final int width) {
		final int visualColumnIndex = getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
		if (visualColumnIndex >= 0) {
			final TableColumn tc = table.tcm.getColumn(visualColumnIndex);
			tc.setPreferredWidth(width);
			tc.setResizable(true);
			setColumnWidthProperty(dataColumnIndex, width);
		}
	}

	public void setColumnWidthProperty(final int dataColumnIndex, final int width) {
			final String identifier = metaRow.getDataColumnIdentifier(dataColumnIndex);

			final String property=Basics.concat(identifier, ".",PROPERTY_HEADER_WIDTH);
			setProperty(property, width);
			if (getUngroupedModel()==this){
				final String value=Integer.toString(width);
				for (final Object key:properties.keySet()){
					if ( ((String)key).endsWith(property) ){
						properties.setProperty((String)key, value);		
					}
				}
			}
		
	}

	private void setPropertyIfRootTableModelDiffers(final String name,
			final String _value) {
		if (ungroupedModel != null) {
			final String ungroupedPrefix = ungroupedModel.dataSource
					.getPropertyPrefix();
			final String value = ungroupedModel.properties.getProperty(
					getPropertyName(ungroupedPrefix, name), null);
			if (value.equals(_value)) {
				return;
			}
		}
		setProperty(name, _value);
	}

	private String getPropertyUsingRootTableModelAsDefault(final String name) {
		String value = null;
		if (ungroupedModel != null) {
			final String ungroupedPrefix = ungroupedModel.dataSource
					.getPropertyPrefix();
			value = ungroupedModel.properties.getProperty(getPropertyName(
					ungroupedPrefix, name), null);
		}
		value = getProperty(name, value);
		if (value == null) {
			final PersonalizableTableModel ptm = getUngroupedModel();
			final String ungroupedPrefix = ptm.defaultPropertyPrefix;
			value = ptm.properties.getProperty(getPropertyName(ungroupedPrefix,
					name), null);
		}
		return value;
	}

	int getColumnWidth(final int dataColumnIndex) {
		return getColumnWidth(metaRow.getDataColumnIdentifier(dataColumnIndex));
	}

	private int getColumnWidth(final String identifier) {
		final String headerWidthIdentifier = Basics.concat(identifier, ".",
				PROPERTY_HEADER_WIDTH);
		final String width = getPropertyUsingRootTableModelAsDefault(headerWidthIdentifier);
		if (width != null) {
			try {
				return Integer.parseInt(width);
			} catch (NumberFormatException nfe) {
				Pel.note(nfe);
			}
		}
		return -1;
	}

	private void configColumn(final TableColumn tc) {
		final int modelColumnIndex = tc.getModelIndex();
		final String identifier = (String) modelColumnIdentifiers
				.get(modelColumnIndex);
		if (this.rememberUserPreferredColumnWidth) {
		final int width = getColumnWidth(identifier);
		if (width >= 1) {
			tc.setPreferredWidth(width);
			tc.setResizable(true);
		}
		}
		tc.setHeaderValue(getColumnName(modelColumnIndex));
		tc.setIdentifier(identifier);
	}

	void initModelColumns(final int[] dataColumnIndexes) {
		final ArrayList<String> identifierList = new ArrayList<String>(dataColumnIndexes.length);
		for (int i = 0; i < dataColumnIndexes.length; i++) {
			identifierList.add(metaRow
					.getDataColumnIdentifier(dataColumnIndexes[i]));
		}
		initModelColumns(identifierList);
	}

	private int[] modelToDataColumnIndexes;

	private void configColumnIndexes() {
		final int n = modelColumnIdentifiers.size();
		modelToDataColumnIndexes = new int[n];
		for (int i = 0; i < n; i++) {
			modelToDataColumnIndexes[i] = metaRow
					.indexOf(modelColumnIdentifiers.get(i));
		}

	}

	void initModelColumns(final List<String> identifierList) {
		modelColumnIdentifiers = identifierList;
		configColumnIndexes();
		if (table != null) {
			table.header.setDraggedColumn(null); // addresses issue with Mac
			// JVM 1.4.1
			table.tcm = new DefaultTableColumnModel();
			for (int modelColumnIndex = 0; modelColumnIndex < modelColumnIdentifiers
					.size(); modelColumnIndex++) {
				final TableColumn tc = new TableColumn(modelColumnIndex);
				dataSource.handleNewColumn(tc,
						getDataColumnIndex(modelColumnIndex));
				table.tcm.addColumn(tc);
			}
			configColumns();
			table.setColumnModel(table.tcm);
		}
	}
	
	private Collection<String> getIdentifiersForHiddenSortedColumn() {
		ArrayList<String> c=columnFreezer.getIdentifiers();
		final Collection<String> value=new ArrayList<String>();
		final int[] sortedDataColumnIndices = getSort();
		for (int sortedDataColumnIndex : sortedDataColumnIndices) {
			final String id=getDataColumnIdentifier(sortedDataColumnIndex);
			if(isHidden(id) && !c.contains(id)){
				value.add(getColumnAbbreviation(sortedDataColumnIndex));
			}
		}
		return value;
	}

	private void configColumns() {
		final TableColumnModel tcm = table.tcm;
		final int n = tcm.getColumnCount();
		for (int i = 0; i < n; i++) {
			configColumn(tcm.getColumn(i));
		}
	}

	public void unsort() {
		sortNeeded = false;
		columnsToSort = new ArrayList<SortInfo>();
	}

	public void setDefaultSort(final SortInfo[] sortInfo) {
		final String s = getProperty(PROPERTY_SORT_ORDER, null);
		if (s == null) {
			sort(sortInfo);
		}
	}

	public void sort(final SortInfo[] sortInfo) {
		sortNeeded = false;
		columnsToSort = new ArrayList();
		for (int i = 0; i < sortInfo.length; i++) {
			columnsToSort.add(sortInfo[i]);
		}
	}

	int[] getSelectedRowIndexesInAscendingOrder() {
		if (table != null) {
			final int[] selected = table.getSelectedRows();
			Arrays.sort(selected);
			return selected;
		}
		return null;
	}
	
	public int getSelectedRowCountMinusLastRowForEditingAndFilterRows() {
		final int []a=table.getSelectedRows();
		int n=a.length;
		if (this.showingEmptyLastRowForCreating()) {
			int rn=table.getRowCount();
			for (int i:a) {
				
				if (i==rn-1) {
					n--;
				}
			}
		}
		if (showFilterUI) {
			for (int i :a){
				if (i == table.ROW_IDX_FILTER_VALUE
						|| i == table.ROW_IDX_FILTER_OP) {
					n--;
				}
			}
		}

		return n;
	}

	public int[] getSelectedDataRowIndexesInDescendingOrder(){
		final int[] selected = getSelectedRowIndexesInAscendingOrder();
		final int n = getSelectedDataRowCount(selected);
		if (n < selected.length) { // remove 2 top filter rows if on
			int[] p = new int[n];
			final int start = selected.length - n;
			for (int j = n - 1, i = selected.length - 1; i >= start; i--, j--) {
				p[j] = selected[i];
			}
			return p;
		}
		int[] v=new int[selected.length];
		for (int i = 0; i < selected.length; i++) {
			v[i]=selected[(selected.length-i)-1];
		}
		
		return v;
		
	}
	public int[] getSelectedDataRowIndexesInAscendingOrder() {
		final int[] selected = getSelectedRowIndexesInAscendingOrder();
		final int n = getSelectedDataRowCount(selected);
		if (n < selected.length) { // remove 2 top filter rows if on
			int[] p = new int[n];
			final int start = selected.length - n;
			for (int j = n - 1, i = selected.length - 1; i >= start; i--, j--) {
				p[j] = selected[i];
			}
			return p;
		}
		return selected;
	}

	public static Object[] debugCells(final Row row) {
		final Collection c = new ArrayList();
		for (int i = 0; i < row.getColumnCount(); i++) {
			final String s = Basics.toString(row.get(i));
			c.add(s);
			System.out.print(s);
			System.out.print(", ");
		}
		System.out.println();
		return c.toArray();
	}

	public ArrayList<Row> getSelectedRowsInDescendingOrder() {
		final ArrayList<Row> al = new ArrayList<Row>();
		if (table != null) {
			final int[] selected = getSelectedRowIndexesInAscendingOrder();
			if (selected != null) {
				// skip past 2 filtering rows
				final int n = getSelectedDataRowCount(selected);
				final int start = selected.length - n;
				for (int i = selected.length - 1; i >= start; i--) {
					final Row r = getRowAtVisualIndex(selected[i]);
					if (r != null) {
						al.add(r);
					}
				}
			}
		}
		return al;
	}

	public ArrayList<Row> getSelectedRowsInAscendingOrder() {
		final ArrayList<Row> value = new ArrayList<Row>(), l = getSelectedRowsInDescendingOrder();
		for (int i = l.size() - 1; i >= 0; i--) {
			value.add(l.get(i));
		}
		return value;
	}

	public void sort() {		
		sort(true);
	}

	private String debug(final SortInfo[] si) {
		StringBuilder sb = new StringBuilder();
		for (final SortInfo s : si) {
			sb.append(getColumnAbbreviation(s.dataColumnIndex));
			sb.append(", ");
		}
		return sb.toString();
	}

	public static boolean resorting = false;

	public void sort(final boolean refreshSelected) {
		if (table == null || !table.isInitializingUpCellEditor) {
			int[] debugged = getSelectedRowIndexesInAscendingOrder();
			final List<Row> selected = getSelectedRowsInDescendingOrder();
			sortNeeded = false;
			final List<Row> l = dataSource.getFilteredDataRows();
			if (l != null && l.size() > 1) {
//				final TimeKeeper tk=new TimeKeeper();
				final SortInfo[] si = getAllSortInfo();
				if (OPTIMIZE_SORTING
						&& l.size() >= OPTIMIZE_SORT_DATA_SIZE_THRESHOLD) {
//					tk.reset("Start sort ... optimizing needed");
					Collections.sort(l,
							new ColumnComparatorFast(dataSource, si));
				} else {
//					tk.reset("Start sort ... optimizing not needed");
					Collections.sort(l, new ColumnComparator(si));
				}
//				tk.announce("   .... done sort "
//						+ l.size() + " rows by " + si.length + " columns "+ debug(si)); 
			}
			resorting = true;
			dataSource.fireSortOrderChanged();
			resorting = false;
			if (refreshSelected && !Basics.isEmpty(selected)) {
				final List<Row> filteredDataList = dataSource
						.getFilteredDataRows(), filteredSelections=new ArrayList<Row>();
				
				for (int i = 0; i < selected.size(); i++) {
					final Row row = (Row) selected.get(i);

					final int filteredDataRowIndex = filteredDataList.indexOf(row);
					if (filteredDataRowIndex>=0){
						Row that = (Row) filteredDataList
								.get(filteredDataRowIndex);
						assert that.equals(row);
						filteredSelections.add(that);
					}
				}
				// debugCells(row);
				// debugCells(that);
				initializingPicks=true;
				final int n=filteredSelections.size();
				boolean first = true;
				for (int i = 0; i < n; i++) {
					int filteredDataRowIndex = filteredDataList
							.indexOf(filteredSelections.get(i));
					if (i == n - 1) {
						initializingPicks = false;
					}
					int visualRowIndex = getVisualRowIndex(filteredDataRowIndex);
					if (first) {
						first = false;
						table.setRowSelectionInterval(visualRowIndex,
								visualRowIndex);
						if (latestSortColumn >= 0) {
							table.scrollToVisible(visualRowIndex,
									latestSortColumn);
							latestSortColumn = -1;
						}
					} else {
						table.addRowSelectionInterval(visualRowIndex,
								visualRowIndex);
					}
				}
				initializingPicks = false;
			}
		}
	}

	public Window containingWnd;

	public void setWindowFromProperties(final Window containingWnd,
			final String prefix) {
		setWindowFromProperties(containingWnd, prefix + "W", prefix + "H",
				prefix + "X", prefix + "Y", prefix + "M");

	}

	private ComponentListener frameResizeMoveListener;
	private Window resizeMoveListeningFrame;

	public void setWindowFromProperties(final Window containingWnd,
			final String widthProperty, final String heightProperty,
			final String xProperty, final String yProperty,
			final String maximizedProperty) {
		setWindowFromProperties(containingWnd, widthProperty, heightProperty,
				xProperty, yProperty, maximizedProperty, true);
	}

	public void setWindowFromProperties(final Window containingWnd,
			final String widthProperty, final String heightProperty,
			final String xProperty, final String yProperty,
			final String maximizedProperty, final boolean adjustNow) {
		if (adjustNow) {
			final Point p = containingWnd.getLocation();
			final int x = getProperty(xProperty, p.x);
			final int y = getProperty(yProperty, p.y);
			containingWnd.setLocation(x, y);
			final Dimension d = containingWnd.getSize();
			final int w = getProperty(widthProperty, d.width);
			final int h = getProperty(heightProperty, d.height);
			containingWnd.setSize(w, h);
		}
		if (containingWnd instanceof Frame) {	
			removeResizeMoveListener();
			resizeMoveListeningFrame = containingWnd;
			frameResizeMoveListener = new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					setPropertiesFromWindow(containingWnd, widthProperty,
							heightProperty, xProperty, yProperty,
							maximizedProperty);
				}

				public void componentMoved(ComponentEvent e) {
					setPropertiesFromWindow(containingWnd, widthProperty,
							heightProperty, xProperty, yProperty,
							maximizedProperty);
				}
			};
			((Frame) containingWnd)
					.addComponentListener(frameResizeMoveListener);
		}
		if (adjustNow) {
			SwingBasics.adjustToAvailableScreens(containingWnd, getProperty(
					maximizedProperty, false));
		}
	}

	public boolean hasResizeMoveListener() {
		return frameResizeMoveListener != null;
	}

	public final void removeResizeMoveListener() {
		if (hasResizeMoveListener()) {
			resizeMoveListeningFrame
					.removeComponentListener(frameResizeMoveListener);
		}
	}

	public void setPropertiesFromWindow(final Window containingWnd,
			final String prefix) {
		setPropertiesFromWindow(containingWnd, prefix + "W", prefix + "H",
				prefix + "X", prefix + "Y", prefix + "M");
	}

	public void setPropertiesFromWindow(final Window containingWnd,
			final String widthProperty, final String heightProperty,
			final String xProperty, final String yProperty,
			final String maximizedProperty) {
		if (containingWnd instanceof Frame) {
			if (((Frame) containingWnd).getExtendedState() == Frame.MAXIMIZED_BOTH) {
				// no need to set dimensions
				setProperty(maximizedProperty, true);
				notifyViewChanged();
				return;
			}
		}
		setProperty(maximizedProperty, false);
		final Rectangle oldR = new Rectangle(getProperty(xProperty, 0),
				getProperty(yProperty, 0), getProperty(widthProperty, 0),
				getProperty(heightProperty, 0)), newR = containingWnd
				.getBounds();

		if (!newR.equals(oldR)) {
			final Rectangle r = SwingBasics.getScreen(containingWnd);
			if (newR.x >= r.x && newR.y >= r.y) {
				setProperty(xProperty, newR.x);
				setProperty(yProperty, newR.y);
			}
			setProperty(widthProperty, newR.width);
			setProperty(heightProperty, newR.height);
			notifyViewChanged();
		}
	}

	private ComponentListener containingWndListener;

	public void setPropertiesFromWindow() {
		setPropertiesFromWindow(containingWnd, PROPERTY_WINDOW_WIDTH,
				PROPERTY_WINDOW_HEIGHT, PROPERTY_WINDOW_X, PROPERTY_WINDOW_Y,
				PROPERTY_WINDOW_M);
	}

	public void setPersonalizableWindowOwner(final Window containingWnd) {
		if (containingWnd != null) {
			this.containingWnd = containingWnd;
			setWindowFromProperties(containingWnd, PROPERTY_WINDOW_WIDTH,
					PROPERTY_WINDOW_HEIGHT, PROPERTY_WINDOW_X,
					PROPERTY_WINDOW_Y, PROPERTY_WINDOW_M);
			containingWnd.addMouseListener(table);
			containingWndListener = new ComponentListener() {
				public void componentShown(ComponentEvent e) {
				}

				public void componentHidden(ComponentEvent e) {
				}

				public void componentMoved(ComponentEvent e) {
					if (containingWnd.isVisible() && containingWnd.isActive()) {
						notifyViewChanged();
					}
				}

				public void componentResized(ComponentEvent e) {
					if (containingWnd.isVisible() && containingWnd.isActive()) {
						notifyViewChanged();
					}
				}
			};
			containingWnd.addComponentListener(containingWndListener);
		} else {
			if (containingWndListener != null) {
				this.containingWnd
						.removeComponentListener(containingWndListener);
				this.containingWndListener = null;
			}
			this.containingWnd = null;
		}
	}

	public void saveProperties(final String configFileName) {
		final Properties properties = updatePropertiesWithPersonalizations(true);
		PropertiesBasics.saveProperties(properties, configFileName, "");
	}

	/**
	 *
	 * @param saveFilter
	 * @return
	 */
	public Properties updatePropertiesWithPersonalizations(
			final boolean saveFilter) {
		final int type = getModelType();
		if (table != null && table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		if (layoutChangeCnt > 0) {
			int i = 3;
		}
		if (type == TYPE_UNGROUPED && hiddenByGrouping()) {

			getViewChangeCount();
			groupSeivedModel.updatePropertiesWithPersonalizations(true);
			recentTrees.save();
		}
		final HashMap<String, String> rlbn = (ungroupedModel == null) ? renamedLabelsByIdentifier
				: ungroupedModel.renamedLabelsByIdentifier;
		final String ungroupedPrefix = getUngroupedModel().dataSource
				.getPropertyPrefix();

		for (final Iterator<String> it = rlbn.keySet().iterator(); it.hasNext();) {
			final String name = it.next();
			final String label = rlbn.get(name);
			final String _name = getPropertyName(ungroupedPrefix, name + "."
					+ PROPERTY_HEADER_LABEL);

			properties.setProperty(_name, label);
		}

		setProperty(PROPERTY_SHOW_FILTER_UI, showFilterUI);
		if (restartWithGroupOption == null) {
			setProperty(PROPERTY_GROUP, columnsToSort.size()==0?NO_GROUPING : groupOption);
		} else {
			setProperty(PROPERTY_GROUP, restartWithGroupOption.intValue());
		}
		if (table != null) {
			final TableColumnModel tcm = table.tcm;
			final int n = tcm.getColumnCount();
			final ArrayList<String> al = new ArrayList<String>();
			for (int i = 0; i < n; i++) {
				final TableColumn tc = tcm.getColumn(i);
				final String identifier = modelColumnIdentifiers.get(tc
						.getModelIndex());
				if (!identifier.equals("null")) {
					al.add(identifier);
					final int w = tc.getPreferredWidth();
					setProperty(Basics.concat(identifier, ".",PROPERTY_HEADER_WIDTH), w);
				}
			}
			setProperty(PROPERTY_TABLE_COLUMN_MODEL,
					Basics.urlEncode(al));
			final boolean autoResizeOn = table.getAutoResizeMode() != JTable.AUTO_RESIZE_OFF;
			setProperty(PROPERTY_AUTO_RESIZE, autoResizeOn);
			final int idx = getFirstSelectedRowIdx();
			setProperty(PROPERTY_SELECTED_ROW, idx);
		}
		{ // calculate sort order 
			final ArrayList al = new ArrayList();
			for (int i = 0; i < columnsToSort.size(); i++) {
				final SortInfo si = (SortInfo) columnsToSort.get(i);
				if (si.dataColumnIndex >= 0) {
					final String name = (String) metaRow
							.getDataColumnIdentifier(si.dataColumnIndex);
					al.add(name);
					setProperty(name + "." + PROPERTY_ASCENDING, si.ascending);
				}
			}
			setProperty(PROPERTY_SORT_ORDER, Basics.urlEncode(al));
		}
		setProperty(PROPERTY_USE_DITTOS, useDittos);
		setProperty(PROPERTY_DISABLE, useDisabling);
		for (int dataColumnIndex = 0; dataColumnIndex < metaRow.size(); dataColumnIndex++) {
			final String name = (String) metaRow
					.getDataColumnIdentifier(dataColumnIndex);
			Object value = null;
			if (dataSource.isFilterable()) {
				value = filteringRow.get(dataColumnIndex);
				if (value != null && saveFilter) {
					final String strValue = DefaultStringConverters.toString(
							metaRow.getClass(dataColumnIndex), value);
					setProperty(name + "." + PROPERTY_FILTER_VALUE, strValue);
				} else {
					removeProperty(name + "." + PROPERTY_FILTER_VALUE);
				}
				if (saveFilter) {
					value = filterOperatorRow.get(dataColumnIndex).toString();
					setProperty(name + "." + PROPERTY_FILTER_OP, value
							.toString());
				} else {
					setProperty(name + "." + PROPERTY_FILTER_OP,
							Filterable.opNone);
				}
			}
		}

		if (containingWnd != null) {
			setPropertiesFromWindow(containingWnd, PROPERTY_WINDOW_WIDTH,
					PROPERTY_WINDOW_HEIGHT, PROPERTY_WINDOW_X,
					PROPERTY_WINDOW_Y, PROPERTY_WINDOW_M);

		}
		return properties;
	}

	private void removeRenamedProperty(final String name) {
		final String ungroupedPrefix = getUngroupedModel().dataSource
				.getPropertyPrefix();
		final String _name = getPropertyName(ungroupedPrefix, name + "."
				+ PROPERTY_HEADER_LABEL);

		removeProperty(_name);
	}

	private void removeProperty(final String name) {
		final String theName = getPropertyName(name);
		properties.remove(theName);
	}

	public void setProperty(final String name, final int value) {
		final String theName = getPropertyName(name);
		properties.setProperty(theName, Integer.toString(value));
	}

	public static void setProperty(final Properties properties,
			final String fullName, final boolean value) {
		properties.setProperty(fullName, value ? "true" : "false");
	}

	public void setProperty(final String name, final boolean value) {
		final String theName = getPropertyName(name);
		;
		properties.setProperty(theName, value ? "true" : "false");
	}

	public void setProperty(final String name, final String value) {
		final String theName = getPropertyName(name);
		;
		properties.setProperty(theName, value);
	}

	public String getProperty(final String name, final String theDefault) {
		final String theName = getPropertyName(name);
		return properties.getProperty(theName, theDefault);
	}

	public int getProperty(final String name, final int theDefault) {
		int retVal = theDefault;
		final String theName = getPropertyName(name);
		final String value = properties.getProperty(theName, Integer
				.toString(theDefault));
		try {
			retVal = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			Pel.note(nfe);
		}

		return retVal;
	}

	/**
	 *
	 * @return properties that describe the user's personaslizations of the
	 *         table since the last call to
	 *         updatePropertiesWithPersonalizations(). If this call has not been
	 *         made with the current instance then the properties sreflect those
	 *         made during the last session with the same table model.
	 */
	public Properties getProperties() {
		return properties;
	}

	public static boolean getProperty(final Properties properties,
			final String fullName, final boolean theDefault) {
		final String str = properties.getProperty(fullName, theDefault ? "true"
				: "false");
		return str == null ? theDefault : (str.equalsIgnoreCase("true"));
	}

	public boolean getProperty(final String name, final boolean theDefault) {
		final String theName = getPropertyName(name);
		String str = properties.getProperty(theName);
		if (str == null) {
			return theDefault;
		}
		return str.equalsIgnoreCase("true");
	}

	private void prependColumn(final int dataColumnIndex) {
		final int vc=getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
		if (vc<0) {
			showColumn(metaRow.getDataColumnIdentifier(dataColumnIndex), true);
		} else {
			table.tcm.moveColumn(vc, 0);
		}
	}

	public void showColumn(final int dataColumnIndex, final boolean prepend) {
		showColumn(metaRow.getDataColumnIdentifier(dataColumnIndex), prepend);
	}

	public void showAfterColumn(final int dataColumnIndexToMove, final String postionAfterColumnName) {
		int postionAfterColumnIndex = modelColumnIdentifiers.indexOf(postionAfterColumnName);
		showColumn(++postionAfterColumnIndex, metaRow.getDataColumnIdentifier(dataColumnIndexToMove));
	}
	
	public boolean showColumn(final int dataColumnIndex) {
		return showColumn(metaRow.getDataColumnIdentifier(dataColumnIndex));
	}

	public boolean showColumn(final String name) {
		return showColumn(-1, name);
	}

	public void showColumn(final String name, final boolean prepend) {
		showColumn(prepend ? 0 : -1, name);
	}

	public boolean showColumn(final int moveToThisVisualIndex, final String name) {
		if (!modelColumnIdentifiers.contains(name)) {
			modelColumnIdentifiers.add(name);
			configColumnIndexes();
			final int modelColumnIndex = modelColumnIdentifiers.size() - 1;
			final TableColumn tc = new TableColumn(modelColumnIndex);
			configColumn(tc);
			table.tcm.addColumn(tc);
			if (moveToThisVisualIndex >= 0) {
				table.tcm.moveColumn(table.tcm.getColumnCount() - 1,
						moveToThisVisualIndex);
			}
			return true;
		} 		
		else if (moveToThisVisualIndex >= 0) {
			int from = getVisualColumnIndexFromDataColumnIndex(getDataColumnIndexForIdentifier(name));
			int total = table.tcm.getColumnCount() ;
			table.tcm.moveColumn(from,
					moveToThisVisualIndex);
			return true;
		}
		return false;
	}
	
	public boolean moveToThisVisualIndex(final String name, final int moveToThisVisualIndex) {
		if (moveToThisVisualIndex >= 0) {
			table.tcm.moveColumn(getModelColumnIndexFromDataColumnIndex(getDataColumnIndexForIdentifier(name)),
					moveToThisVisualIndex);
			return true;
		}
		return false;
	}
	
	public void showDuplicateColumns(final String name) {
		if (!showColumn(-1,name)) {	
			for (int i = 0; i < table.tcm.getColumnCount(); i++) {
				final TableColumn tc = table.tcm.getColumn(i);
				if (tc.getIdentifier().equals(name)) {
					return;
				}
			}
			final TableColumn tc = new TableColumn(modelColumnIdentifiers.indexOf(name));
			configColumn(tc);
			table.tcm.addColumn(tc);
		}
	}

	int squeezeCondensedMax = 0;
	double squeezeCondensedRatio = 0;

	public void setCondensedRatioOfScreen(final double ratio,
			final int maxPixels) {
		this.squeezeCondensedRatio = ratio;
		this.squeezeCondensedMax = maxPixels;
	}

	public void showAllColumns() {
		final ArrayList<String> al = new ArrayList<String>();

		for (int i = 0; i < table.tcm.getColumnCount(); i++) {
			al.add((String)table.tcm.getColumn(i).getIdentifier());
		}
		final Collection<String> c = metaRow.cloneDataColumnIdentifiers();
		for (final String dataColumnIdentifier:c) {
			if (!al.contains(dataColumnIdentifier) && !isInvisibleColumn(dataColumnIdentifier)) {
				al.add(dataColumnIdentifier);
			}
		}

		initModelColumns(al);
		table.cellHighlighter.reset();
		sortAndRepaint();
		notifyViewChanged();
	}
	
	public void showActiveColumn() {
		final ArrayList al = new ArrayList();
		final Collection c = metaRow.cloneDataColumnIdentifiers();
		for (final Iterator it = c.iterator(); it.hasNext();) {
			final String name = (String) it.next();
			if (name.contains("active")) {
				al.add(name);				
			}
		}
		final int tableColumnCount = table.tcm.getColumnCount(); 
		for (int i = 0; i < tableColumnCount; i++) {
			final String columnIdentifier = (String)table.tcm.getColumn(i).getIdentifier();
			if (!columnIdentifier.contains("active")) { 				
				al.add(columnIdentifier);
			}
				
		}
		initModelColumns(al);
		TableColumn tc = table.tcm.getColumn(0);
		if (tc.getIdentifier().toString().contains("active")) {
			tc.setPreferredWidth(75);
			tc.setHeaderValue("Active");			
		}
		table.cellHighlighter.reset();
		sortAndRepaint();
		notifyViewChanged();
	}
	
	public void hideActiveColumn() {
		final ArrayList al = new ArrayList();
		final int tableColumnCount = table.tcm.getColumnCount();
		for (int i = 0; i < tableColumnCount; i++) {
			final String name = table.tcm.getColumn(i).getIdentifier().toString();
			if (!name.contains("active")) {
				al.add(name);				
			}
		}
		initModelColumns(al);
		table.cellHighlighter.reset();
		sortAndRepaint();
	}

	public void viewAllColumns(final boolean value) {
		final int n = metaRow.size();
		for (int i = 0; i < n; i++) {
			if (value)
				showColumn(-1, metaRow.getDataColumnIdentifier(i));
			else
				hideColumn(i);
		}
	}

	void initSortInfo() {
		columnsToSort = new ArrayList();
		final PersonalizableTableModel ptm = getUngroupedModel();
		String ungroupedPrefix = ptm.dataSource.getPropertyPrefix(), defaultPropertyPrefix = ptm.defaultPropertyPrefix;
		boolean useDefaultPrefix = false;
		String s = getProperty(PROPERTY_SORT_ORDER, null);
		if (Basics.isEmpty(s)) {
			if (ptm == this) {
				final boolean isUngroupedPrefix = Basics.equals(
						ungroupedPrefix, defaultPropertyPrefix);
				if (!isUngroupedPrefix) {
					useDefaultPrefix = true;
					final String name = getPropertyName(defaultPropertyPrefix,
							PROPERTY_SORT_ORDER);
					s = properties.getProperty(name, null);
				}
			}
		}
		if (!Basics.isEmpty(s)) {
			final List<String> v = Basics.urlDecode(s);
			if (metaRow.containsAllIdentifiers(v)) {
				for (int i = 0; i < v.size(); i++) {
					final String name = v.get(i);
					// int modelIndex=tableColumnModel.indexOf(name);
					final boolean ascending;
					if (!useDefaultPrefix) {
						ascending = getProperty(
								name + '.' + PROPERTY_ASCENDING, true);
					} else {
						final String name2 = getPropertyName(
								defaultPropertyPrefix, name + '.'
										+ PROPERTY_ASCENDING);
						final String str = properties.getProperty(key);
						if (str == null) {
							ascending = true;
						} else {
							ascending = str.equalsIgnoreCase("true");
						}
					}
					final int dataColumnIndex = metaRow.indexOf(name);
					if (dataColumnIndex >= 0) {
						if (dataColumnIndex >= metaRow.size()) {
							System.err.println("sort column out of range"
									+ dataColumnIndex);
						} else {
							final SortInfo si = new SortInfo(metaRow,
									dataColumnIndex, i + 1);
							si.ascending = ascending;
							columnsToSort.add(si);
						}
					}
				}
				sort();
			}
		}
	}

	public static PersonalizableTableModel activate(final List data,
			final String[] dataModel, final String fileName,
			final boolean readOnly) {
		final List l = Basics.toList(dataModel);
		final ListMetaRow metaRow = new ListMetaRow(l, (Row) data.get(0));
		final DefaultPersonalizableDataSource ds = new DefaultPersonalizableDataSource(
				data, metaRow);
		return activate(ds, PropertiesBasics.loadProperties(fileName), readOnly);
	}

	public static PersonalizableTableModel activate(
			final PersonalizableDataSource ds,
			final Properties personalizationProperties, final boolean readOnly) {
		return new PersonalizableTableModel(ds, personalizationProperties,
				readOnly);
	}
	
	public static PersonalizableTableModel activate(
			final PersonalizableDataSource ds,
			final Properties personalizationProperties, final boolean readOnly, final boolean isTreeResizeNeeded) {		
		PersonalizableTableModel ptm = new PersonalizableTableModel(ds, personalizationProperties,
				readOnly);
		ptm.isAutoTreeResizeNeeded = isTreeResizeNeeded;
		return ptm;
	}

	public static PersonalizableTableModel activate(
			final PersonalizableDataSource dataSource, final String fileName,
			final boolean readOnly) {
		return activate(dataSource, PropertiesBasics.loadProperties(fileName),
				readOnly);
	}
	
	public static PersonalizableTableModel activateWithDynamicColumns(
			final PersonalizableDataSource dataSource, final String fileName,
			final boolean readOnly) {
		return new PersonalizableTableModel(dataSource, PropertiesBasics.loadProperties(fileName),
				readOnly, false, false);
	}
	
	public static PersonalizableTableModel activateWithDynamicColumns(
			final PersonalizableDataSource dataSource, final Properties personalizationProperties,
			final boolean readOnly) {
		return new PersonalizableTableModel(dataSource, personalizationProperties,
				readOnly, false, false);
	}

	public static PersonalizableTableModel activate(
			final PersonalizableDataSource dataSource, final boolean readOnly) {
		return new PersonalizableTableModel(dataSource, new Properties(),
				readOnly);
	}

	static PersonalizableTableModel activateCompanionTable(
			final PersonalizableDataSource dataSource, final boolean readOnly) {
		return new PersonalizableTableModel(dataSource, new Properties(),
				readOnly, true);
	}

	String key = "";
	private String singularKey = "";

	public JPopupMenu popup;
	static final String MENU_TEXT_SEQUENCE = "Alter sort sequence",
			MENU_TEXT_SORT_ASCENDING = "Sort ascending",
			MENU_TEXT_SORT_DESCENDING = "Sort descending",
			MENU_TEXT_TAB = "Create tabs", 
			MENU_TEXT_SHIFT_LEFT="Shift left",
			MENU_TEXT_SHIFT_RIGHT="Shift right",
			MENU_TEXT_HIDE_COLUMNS="Hide columns",
			MENU_TEXT_FREEZE_AS_ROW_HEADER="Freeze as row header",
			MENU_TEXT_UNFREEZE="Unfreeze row header";
	private JMenuItem saveItem = null, changeEntireColumn = new JMenuItem(),
			moveUpItem = new JMenuItem(), moveDownItem = new JMenuItem(),
			moveTopItem = new JMenuItem(), moveBottomItem = new JMenuItem();
	private DisabledExplainer newDisabledButton, addDisabledButton;
	private final String QUERY_HIGHLIGHTED_COLUMNS="Query selected columns", QUERY_FAVORITE_COLUMNS="Query favorite columns";
	String getQueryFavoriteText(){
		return Basics.concat("<html>", QUERY_FAVORITE_COLUMNS,
		":  <i>",
		Basics.trimIfTooBig(getColumnAbbreviationsForDataColumnIndexes(
				getColumnDisplayOrder(false, PROPERTY_QUERY_FAVORITE, true))),
				"</i></html>");
	}
	private Operation delete=null, remove=null;
	private final JMenuItem newItem = new JMenuItem(),
			chooseItemFromList = new JMenuItem(),
			refreshSortItem = new JMenuItem("Refresh sort", MmsIcons
					.getRefreshIcon()), sortSequenceItem = new JMenuItem(
					MENU_TEXT_SEQUENCE, MmsIcons.getSortCustomDown16Icon()), sortAscendingItem = new JMenuItem(
					MENU_TEXT_SORT_ASCENDING, MmsIcons.getSortAscending16Icon()),
			sortDescendingItem = new JMenuItem(MENU_TEXT_SORT_DESCENDING,
					MmsIcons.getSortDescending16Icon()), tabItem = new JMenuItem(
					MENU_TEXT_TAB, MmsIcons.getWideBlankIcon()), unsortItem = new JMenuItem("Unsort", MmsIcons.getTableDeleteIcon()),
			unsortAllItem = new JMenuItem("Unsort all columns", MmsIcons.getTableRefreshIcon()),
			hideColumnItem = new JMenuItem("Hide", MmsIcons.getHideColumnIcon()),
			sortArrangeColumnsItem = new JMenuItem("Sort/arrange columns",
					MmsIcons.getPreferencesIcon()),
			freezeColumnsItem = new JMenuItem(MmsIcons.getTableFreezeIcon()), 
			unfreezeColumnsItem = new JMenuItem(MENU_TEXT_UNFREEZE, MmsIcons.getTableUnfreezeIcon()),
					
			showAllColumnsItem = new JMenuItem("Show all", MmsIcons
					.getShowAllColumnsIcon()), 
			shiftLeftItem = new JMenuItem(
					"Shift left", MmsIcons.getLeftIcon()),
			shiftRightItem = new JMenuItem("Shift right", MmsIcons
					.getRightIcon()), printItem = new JMenuItem("Print",
					MmsIcons.getPrintIcon()),
					hideItem = new JMenuItem("Hide item"), unhideItem = new JMenuItem("Unhide item"),
					showFilterItem = new JMenuItem(
					"Show filter", MmsIcons.getRowsIcon()), applyFilterItem = new JMenuItem(
					"Apply filter", MmsIcons.getFindIcon()),
			findItem = new JMenuItem("Find", MmsIcons.getFindIcon()),
			queryAllColumnsItem = new JMenuItem("Query all columns", MmsIcons
					.getWorldSearchIcon()), 
			querySelectedColumnsItem = new JMenuItem(QUERY_HIGHLIGHTED_COLUMNS, MmsIcons
							.getSearchIcon()), 
			queryFavoriteColumnsItem = new JMenuItem(QUERY_FAVORITE_COLUMNS, MmsIcons
									.getHeart16Icon()), 			
			seeAllItem = new JMenuItem(
					"See all", MmsIcons.getEyeIcon()), 
			saveViewItem = new JMenuItem(),
			pasteViewItem = new JMenuItem(),
			openViewItem = new JMenuItem(), copyViewItem = new JMenuItem(),
			useDisablingItem = new JMenuItem(), fitWindowItem = new JMenuItem(
					"Fit window", MmsIcons.getPageWhiteCompressedIcon()), exploreItem = new JMenuItem("Explore",
					MmsIcons.getMagnifyIcon()), 
			selectAllItem = new JMenuItem(
					"Select all", MmsIcons.getWideBlankIcon()),
					tearAwayItem = new JMenuItem();;
					
	final ActionListener editInPlaceAction=new ActionListener() {
		public void actionPerformed(final ActionEvent ae) {
			editInPlace = !editInPlace;
			saveEditInPlaceSetting();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						table.resizeAndRepaint();
					}
				});
		}
	};
	int deleteItemPopupIndex = 0;
	private DisabledExplainer 
	disabledTab=new DisabledExplainer(tabItem), printDisabled = new DisabledExplainer(printItem), 
	hideDisabled = new DisabledExplainer(hideItem), unhideDisabled = new DisabledExplainer(unhideItem),
	sortAscendingDisabled = new DisabledExplainer(sortAscendingItem),
	sortDescendingDisabled = new DisabledExplainer(sortDescendingItem), refreshDisabled = new DisabledExplainer(refreshSortItem),
	freezeDisabler = new DisabledExplainer(freezeColumnsItem),
	hideDisabler=new DisabledExplainer(hideColumnItem),
	unfreezeDisabler=new DisabledExplainer(unfreezeColumnsItem),
	shiftLeftDisabled = new DisabledExplainer(shiftLeftItem), shiftRightDisabled = new DisabledExplainer(shiftRightItem),
	unsortDisabled = new DisabledExplainer(unsortItem), unsortAllDisabled = new DisabledExplainer(unsortAllItem),
	 sortSequenceDisabled  = new DisabledExplainer(sortSequenceItem), showAllColumnsDisabled;
	public DisabledExplainer moveUpDisabled,moveDownDisabled,moveTopDisabled,moveBottomDisabled;
	private final JMenuItem removeTreeItem = new JMenuItem(MmsIcons
			.getRestoreTableIcon());
	DisabledExplainer removeTreeDisabled = new DisabledExplainer(removeTreeItem);
	private final JCheckBoxMenuItem newTreeItem = new JCheckBoxMenuItem(
			MmsIcons.getHandshakeIcon());

	private final JMenuItem sortBasedTreeItem = new JMenuItem(
			MmsIcons.getSortAscending16Icon());

	public Row[] create() {
		return getUngroupedModel().dataSource.create(this);
	}

	boolean suppressTreeNewPopup = false;

	public void setSuppressTreeNewPopup(final boolean suppressTreeNewPopup) {
		this.suppressTreeNewPopup = suppressTreeNewPopup;
	}

	boolean invokingNewProgrammatically = false;

	public boolean isInvokingNewProgramatically() {
		return invokingNewProgrammatically;
	}

	private void showTableIfHiddenByUnselectedTree() {
		if (isGrouped()
				&& getModelShowing().table.scrollPane.getParent() == null) {
			if (groupedDataSource.tree != null && 
					Basics.isEmpty(groupedDataSource.tree.getSelectionPaths())) {
				groupedDataSource.pickRoot();				
			}
			table.requestFocus();
		}
	}

	public void newItem() {
		invokingNewProgrammatically = true;
		final PersonalizableTableModel utm = getUngroupedModel();
		if ((!suppressTreeNewPopup || !isCompanionModelWithCreatableTree())
				&& utm.dataSource.isCreatable()) {
			if (showingEmptyLastRowForCreating()) {
				showTableIfHiddenByUnselectedTree();
				final int n = getRowCountMinusEmptyLastRowForCreating();
				SwingBasics
						.scrollRectToVisibleForAllParents(
								table, table.getCellRect(n, 0,true));
				
				table.scrollToVisible(n, 0);
				table.clearSelection();
				table.editCellAt(n, 0);
			} else {
				create();
				showTableIfHiddenByUnselectedTree();
			}
		}
		invokingNewProgrammatically = false;
		fireListSelection(new ListSelectionEvent(table, -1, -1, false));
	}

	boolean createInPlace() {
		assert getEditInPlace() == true;
		final PersonalizableTableModel utm = getUngroupedModel();
		final Row[] creations = utm.dataSource.create(this);
		if (!Basics.isEmpty(creations) && !isTabImportOccuring()) {
			if (utm.isGrouped()) {
				for (final Row row : creations) {
					createdInPlace.add(row);
				}
			} else {
				final boolean resort = !table.isInitializingUpCellEditor
						&& utm.dataSource.resortAfterCreate();
				refreshShowingTable(resort);
				final boolean wasSortNeeded = sortNeeded;
				sortNeeded = !resort && columnsToSort.size() > 0;
				if (sortNeeded && !wasSortNeeded) {
					table.getTableHeader().repaint();
				}
			}
			return true;
		}
		return false;
	}

	public void setSortIndication() {
		final boolean wasSortNeeded = sortNeeded;
		sortNeeded = columnsToSort.size() > 0;
		if (sortNeeded && !wasSortNeeded) {
			table.getTableHeader().repaint();
		}
	}

	public void removeAll() {
		dataSource.removeAll();
		getModelShowing().table.clearSelection();
	}

	public void select(final int filteredDataRowIndex,
			final boolean scrollToVisible) {
		final int visualRowIndex = getVisualRowIndex(filteredDataRowIndex);
		table.addRowSelectionInterval(visualRowIndex, visualRowIndex);
		if (scrollToVisible) {
			table.scrollToVisible(visualRowIndex, 0);
		}
		
	}

	public int select(final Row row, final boolean deepEquals,
			final boolean edit, final boolean additive) {

		if (row != null) {
			final List<Row> filteredDataList = dataSource.getFilteredDataRows();
			for (int j = 0; j < filteredDataList.size(); j++) {
				final Row that = (Row) filteredDataList.get(j);
				final boolean found;
				if (deepEquals) {
					found = that.equals(row);
				} else {
					found = that == row;
				}
				if (found) {
					if (showFilterUI) {
						j += 2;
					}
					if (!additive || edit) {
						table.clearSelection();
						table.setRowSelectionInterval(j, j);
						setProperty(PROPERTY_SELECTED_ROW, j);
					} else {
						table.addRowSelectionInterval(j, j);
					}
					final int idx2 = j;
					if (scrollOnce == -1 || scrollOnce == 0) {
						if (scrollOnce == 0) {
							scrollOnce = 1;
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								table.scrollToVisible(idx2, 0);
							}
						});
					}

					table.setColumnSelectionInterval(0, 0);
					if (edit) {
						table.editFirstEditable(j, 0, null, false, false);
					}
					return idx2;
				}
			}
		}
		return -1;
	}

	public void setVisibleCols(int[] visibleCols) {
		if (visibleCols != null) {
			Arrays.sort(visibleCols);
			for (int i = 0; i < getColumnCount(); i++) {
				if (Arrays.binarySearch(visibleCols, i) < 0)
					hideColumn(i);
			}
		}
	}

	public void hideColumn(int dataColumnIndex) {
		hideColumn(metaRow.getDataColumnIdentifier(dataColumnIndex));
	}

	public void hideColumn(final String columnName) {
		if (modelColumnIdentifiers.contains(columnName)) {
			final int visualColumnIndex = table.tcm.getColumnIndex(columnName);
			if (visualColumnIndex >= 0) {
				hideColumn(table.tcm.getColumn(visualColumnIndex));
			}
		}
	}

	final Object []debugColumns(){
		final Collection c=new ArrayList<String>();
		final Enumeration<TableColumn> en=table.getColumnModel().getColumns();
		while (en.hasMoreElements()){
			final TableColumn tc=en.nextElement();
			final Object s=tc.getIdentifier();
			c.add(s);
		}
		return c.toArray();
	}
	private void hideColumn(final TableColumn column) {
		Object []before=debugColumns();
		if (modelColumnIdentifiers.size() > 1) {
			final int cmi = column.getModelIndex();
			table.removeColumn(column);
			Enumeration enumeration = table.getColumnModel().getColumns();
			for (; enumeration.hasMoreElements();) {
				final TableColumn c = (TableColumn) enumeration.nextElement();
				if (c.getModelIndex() >= cmi) {
					c.setModelIndex(c.getModelIndex() - 1);
				}
			}
			modelColumnIdentifiers.remove(cmi);
			configColumnIndexes();
			final Collection<Row>c=getSelectedRowsInTable();
			final boolean was=initializingPicks;
			initializingPicks=true;
			super.fireTableStructureChanged();
			selectAndFocus(c);
			initializingPicks=was;
		}
		Object []after =debugColumns();
		if (after.length!=table.getColumnCount()){
			System.out.println("Zoiks ... count=" + table.getColumnCount()+" after="+Basics.toString(after));	
			table.getColumnCount();
			after=debugColumns();
		}
	}

	boolean usePriorSelectedRow = true;

	public void removePriorSelectedRow() {
		usePriorSelectedRow = false;
	}

	private boolean filterExternallySet = false;

	public void indicateFilterExternallySet(final boolean on) {
		filterExternallySet = on;
	}
	
	private static boolean suspendControlPanelOnQueryWindow=true;
	
	private JCheckBox cb=null;
	private void suspendControlPanel(){
		if (cb == null) {
			cb = new JCheckBox("Suspend control panel hiding",
					suspendControlPanelOnQueryWindow);
			cb.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					suspendControlPanelOnQueryWindow = cb.isSelected();
					SwingUtilities.invokeLater( new Runnable(){
						public void run(){
							syncFilter(true);
							repaint();
						}
					});
				}
			});
		}
		dataSource.setControlPanelSuspension(cb);

		
	}
	public void syncFilter(final boolean resort) {
		if (dataSource.isFilterable()) {
			final JButton foundButton = creatingFilter
					&& popupTableDoneButton != null ? popupTableDoneButton
					: null;
			final boolean hasFilter=refilterOrRemove(!filterExternallySet || GroupedDataSource.removingSeeOnlyTokens);
			refreshShowingTable(resort);
			if (foundButton != null) {
				dataSource.showQueryResults(foundButton,
						getFindMessage());
				SwingBasics.setEnabled(seeOnly, hasFilter);
				SwingBasics.setEnabled(seeAll, hasFilter);
			}
		}
	}

	public void setFilter(final int dataColumnIndex, final int op,
			final String stringValue) {
		setFilter(dataColumnIndex, op, DefaultStringConverters.toObject(metaRow
				.getClass(dataColumnIndex), stringValue));
	}

	public void setFilter(final int dataColumnIndex, final int op,
			final Object objectValue) {
		filterable.setColumnOp(op, dataColumnIndex);
		filteringRow.set(dataColumnIndex, objectValue);
		if (filterOperatorRow != null) {
			filterOperatorRow.set(dataColumnIndex, Filterable.allFilterOp[op]);
		}
	}

	public JSplitPane getGroupedSplitPane() {
		if (groupedDataSource != null) {
			return groupedDataSource.splitPane;
		}
		return null;
	}

	public void setFilter(final int dataColumnIndex, final String stringOp,
			final String stringValue) {
		setFilter(dataColumnIndex, translateOp(stringOp), stringValue);
	}

	GroupedDataSource groupedDataSource;
	PersonalizableTableModel groupSeivedModel;

	public boolean isGrouped() {
		final int t = getUngroupedModel().getGroupOption();
		final boolean b = t != NO_GROUPING;
		return b;
	}

	final static int TYPE_UNGROUPED = 0 /* default */, TYPE_GROUPED = 1,
			TYPE_GROUP_SEIVED = 2, TYPE_INVALID = 3;

	boolean isCompanionModelWithCreatableTree() {
		return getModelType() == TYPE_GROUP_SEIVED
				&& groupedDataSource.canCreate;
	}

	private void addToCompanionTable(final Row row) {
		if (isCompanionModelWithCreatableTree()) {
			dataSource.add(row);
		}
	}

	int getModelType() {
		if (dataSource instanceof GroupedDataSource) {
			return TYPE_GROUPED;
		} else if (groupedDataSource != null && groupSeivedModel == this) {
			return TYPE_GROUP_SEIVED;
		} else if ((groupedDataSource == null && groupSeivedModel == null)
				|| (groupedDataSource != null && groupSeivedModel != null)) {
			return TYPE_UNGROUPED;
		}
		return TYPE_INVALID;
	}

	public PersonalizableTableModel getUngroupedModel() {
		final int type = getModelType();
		if (type == TYPE_GROUP_SEIVED) {
			return groupedDataSource.ungroupedModel;
		} else if (type == TYPE_GROUPED) {
			return ((GroupedDataSource) dataSource).ungroupedModel;
		}
		return this; // default type == TYPE_UNCONDENSED
	}

	boolean hiddenByGrouping() {
		return getModelType() == TYPE_UNGROUPED && groupedDataSource != null;
	}

	/**
	 * This data source is the filtered uncondensed one.
	 */
	void group(final GroupedDataSource groupedDataSource,
			final PersonalizableTableModel groupSeivedModel) {
		this.groupedDataSource = groupedDataSource;
		if (groupSeivedModel == null && this.groupSeivedModel != null
				&& tearAwayHandler != null) {
			tearAwayHandler.removeContext(this.groupSeivedModel.table);
		}
		this.groupSeivedModel = groupSeivedModel;
	}

	public void findFirstEnabledNode() {
		final GroupedDataSource.Node retVal;
		final GroupedDataSource cds = getTreeDataSource();
		if (cds != null && cds.nextFeasibleSelection != null) {
			cds.nextFeasibleSelection.doClick();
		}
	}

	public Collection<Row>getSelectedRowsOrMouseOverRows(){
		final Collection<Row>c;
		final GroupedDataSource.Node node=getUngroupedModel().getMouseOverNode();
		if (node==null){
			c= getModelShowing().getSelectedRowsInDescendingOrder();
		} else {
			c=node.getChildRows();
		}
		return c;
	}
	public GroupedDataSource.Node getMouseOverNode() {
		final GroupedDataSource.Node retVal;
		final GroupedDataSource cds = getTreeDataSource();
		if(cds != null){
		final boolean isTreeEvent=cds.treeMouseEvent!=null && cds.treeMouseEvent.getComponent()==cds.tree;
		if (cds != null && cds.treeMouseEvent != null && isTreeEvent) {
			final Point point = cds.treeMouseEvent.getPoint();
			final TreePath path = cds.tree.getPathForLocation(point.x, point.y);
			if (path != null) {
				retVal = (GroupedDataSource.Node) path.getLastPathComponent();
			} else {
				retVal = null;
			}
		}else {
			retVal = null;
		} 
		}else {
			retVal = null;
		}
		return retVal;
	}

	public GroupedDataSource getTreeDataSource() {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource;
		}
		return null;
	}

	public JTree getTree() {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource.tree;
		}
		return null;
	}
	
	public boolean suppressTreeSelections(final boolean suppress) {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			groupedDataSource.supressSelections(suppress);
		}
		return false;

	}

	public int[] getSelectedDataIndexes() {
		int[] dataRowIndex = null;
		int[] selected = table.getSelectedRows();
		int n = getSelectedDataRowCount(selected);
		if (n > 0) {
			dataRowIndex = new int[n];
			for (int i = n - 1; i >= 0; i--) {
				int r = getFilteredDataRowIndex(selected[i]);
				if (r > -1) {
					dataRowIndex[i] = r;
				}
			}
		}
		return dataRowIndex;
	}

	public void removeOrDeleteSelected() {
		int deletable = 0, removable = 0;
		final boolean bd = getUngroupedModel().dataSource.isDeletable(), br = getUngroupedModel().dataSource
				.isRemovable();
		table.stopCellEditing();
		if (bd || br) {
			final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
			deletable = !bd ? 0 : getDeletableSelectionCount(selected);
			removable = !br ? 0 : getRemovableSelectionCount(selected, null); 
		}
 		if (deletable > 0 && removable > 0) {
			final int answer = PopupBasics.getRadioButtonOption(
					getTearAwayComponent(), "<html>Remove or delete "
							+ (removable) + (removable>1?" items?</html>":" item?</html>"), "Please confirm",
					new String[] { "Remove from list", "Delete from system" }, 0, true, true);
			if (answer == 0) {
				removeSelected(removable, false);
			} else if (answer == 1) {
				deleteSelected(deletable, false);
			}		
		} else if (removable > 0) {
			removeSelected(removable, true);
		} else if (deletable > 0) {
			deleteSelected(deletable, true);
		}
	}

	public void deleteSelected(){
		deleteSelected(getDeletableSelectionCount(), false);
	}

	public void deleteSelectedIfUserAgrees(){
		deleteSelected(getDeletableSelectionCount(), true);
	}

	private void deleteSelected(final int cnt, final boolean ask) {
		if (cnt > 0) {
			setMultiRowChangeOperationFlag(-2, true);
			final PersonalizableTableModel m = PersonalizableTableModel.this;
			final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
			table.stopCellEditing();
			// stopCellEditing may create pending deletes for primary key count
			final int _cnt=getDeletableSelectionCount(selected);
 			if (_cnt > 0) {
 				saveFocus(false);
				if (!ask
						|| dataSource.proceedWithDeletions(this, getFilteredDataRowsIndex(selected),
								_cnt)) {
					for (int i = selected.length - 1; i >= 0; i--) {
						selected[i] = m.getFilteredDataRowIndex(selected[i]);
					}
					final PersonalizableTableModel utm = getUngroupedModel();
					table.clearSelection();
					AtomicOpsWithProgressBar.singleton.startOps(
							selected.length, new ActionListener() {
								public void actionPerformed(final ActionEvent ae) {
									deleteSelected(selected, m, utm);
								}
							}, "deletions");

					m.dataSource.finishedDeleting(selected.length);
					if (m == utm) {
						refreshShowingTable(false);
						final int idx;
						if (selected.length > 0) {
							idx = selected[0] - 1;

						} else {
							idx = -1;
						}
						if (idx >= 0) {
							final int colIdx;
							if (focusModelColumnIndex >= 0) {
								colIdx = SwingBasics
										.getVisualIndexFromModelIndex(m.table,
												focusModelColumnIndex);
							} else {
								colIdx = 0;
							}
							// m.table.setRowSelectionInterval(idx, idx);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									SwingBasics.scrollToVisible(m.table, idx,
											colIdx);
								}
							});
						}
					} else {
						utm.refresh();
					}
				}
			}
			setMultiRowChangeOperationFlag(-2, false);
		}
	}

	private void removeSelected(final int cnt, final boolean ask) {
		if (cnt > 0) {
			setMultiRowChangeOperationFlag(-1, true);
			final PersonalizableTableModel m = PersonalizableTableModel.this;
			final int[] selected = getSelectedRowIndexesInAscendingOrder();
			table.stopCellEditing(); 			
			final int n = getRemovableSelectionCount();
			if (n > 0) {
				if (!ask
						|| PopupBasics.ask(table, "<html>Remove " + n
								+ (n>1?" rows":" row")+" from the list?</html>")) {
					final PersonalizableTableModel utm = getUngroupedModel();
					table.clearSelection();
					for (int i = selected.length - 1; i >= 0; i--) {
						final Row visualRow = getRowAtVisualIndex(selected[i]);
						if (visualRow != null && !getUngroupedModel().getDataSource().
								isTemporaryForPrimaryKeySelecting(visualRow)) {
							final int filteredDataRowIndex = m.getFilteredDataRowIndex(selected[i]);
							if (filteredDataRowIndex > -1) { // don't delete
								// filtering row
								if (utm != m) {
									final Row row = m.getFilteredDataRowsShowing()
											.get(filteredDataRowIndex);
									final int idx = utm.dataSource
											.getFilteredDataRows().indexOf(row);
									utm.dataSource.remove(idx);
								}
								m.dataSource.remove(filteredDataRowIndex);
							}
						}
						
					}
					if (m == utm) {
						refreshShowingTable(false);
					} else {
						utm.refresh();
					}

					restoreFocus(-1);
				}
			}
			setMultiRowChangeOperationFlag(-1, false);
			fireListSelection();
		}
	}
	
	public void removeSelected() {
		setMultiRowChangeOperationFlag(-1, true);
		final PersonalizableTableModel m = PersonalizableTableModel.this;
		final int[] selected = getSelectedRowIndexesInAscendingOrder();
		table.stopCellEditing();
		final int n = getRemovableSelectionCount();
		if (selected.length > 0) {
			final PersonalizableTableModel utm = getUngroupedModel();
			table.clearSelection();
			for (int i = selected.length - 1; i >= 0; i--) {
				final Row visualRow = getRowAtVisualIndex(selected[i]);
				if (visualRow != null
						&& !getUngroupedModel().getDataSource()
								.isTemporaryForPrimaryKeySelecting(visualRow)) {
					final int filteredDataRowIndex = m
							.getFilteredDataRowIndex(selected[i]);
					if (filteredDataRowIndex > -1) { // don't delete
						// filtering row
						if (utm != m) {
							final Row row = m.getFilteredDataRowsShowing().get(
									filteredDataRowIndex);
							final int idx = utm.dataSource
									.getFilteredDataRows().indexOf(row);
							utm.dataSource.remove(idx);
						}
						m.dataSource.remove(filteredDataRowIndex);
					}
				}

			}
			if (m == utm) {
				refreshShowingTable(false);
			} else {
				utm.refresh();
			}

			restoreFocus(-1);

		}
		setMultiRowChangeOperationFlag(-1, false);
		fireListSelection();
	}
	
	private void hideJob() {
		final List<Row> selectedRows = getSelectedRowsInAscendingOrder();
		
		for (Row row: selectedRows) {
			
			final String key = row.getRowType();			
			final String value = row.getRowId();
			
			String currentFilters = properties.getProperty(key, null);
			if (currentFilters == null) {
				properties.setProperty(key, value);			
			}
			else if (currentFilters.indexOf(value) == -1){
				currentFilters+="," + value;
				properties.setProperty(key, currentFilters);
			}
			notifyViewChanged();
			final PersonalizableTableModel utm = getUngroupedModel();
			final PersonalizableTableModel m = PersonalizableTableModel.this;
			utm.refreshLater(false);			
			utm.table.clearSelection();
			if (m == utm) {
				refreshShowingTable(false);
			} else {
				utm.refresh();				
			}
			utm.restoreFocus(-1);
			utm.setMultiRowChangeOperationFlag(0, false);
			utm.fireListSelection();
		}
	}
	
	private void unhideJob() {
		final ArrayList<Row> selectedRows = getSelectedRowsInAscendingOrder();
		final List<Row> hiddenRows = dataSource.getHiddenRows();
		final SimplePickList simplePickList = new SimplePickList(hiddenRows, "Hidden items", "Unhide", properties);
		final Collection<Row> values = simplePickList.getPicked();
		if (values == null || values.isEmpty()) {
			return;
		}
		final String key = values.iterator().next().getRowType();
		String currentFilters = properties.getProperty(key, null);
		if (currentFilters == null) {
			return;			
		}
		for (Row row: values) {
			final String value = row.getRowId();
			int startIndex = currentFilters.indexOf(value);
			if (startIndex != -1) {
				String remainingString = currentFilters.substring(startIndex);
				int endIndex = remainingString.indexOf(",");
				if (endIndex != -1) {
					final String firstHalf = currentFilters.substring(0, startIndex);
					final String secondHalf = remainingString.substring(endIndex + 1);
					currentFilters = firstHalf + secondHalf;						
				}
				else {
					currentFilters = currentFilters.substring(0, startIndex);
				}
			}				
		}
		properties.setProperty(key, currentFilters);
		refreshLater(false);
		getUngroupedModel().refreshLater(false);
		notifyViewChanged();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				final List<Row> filteredDataList = dataSource.getFilteredDataRows();
				table.clearSelection();
				for(Row row: selectedRows){
					final int filteredDataRowIndex = filteredDataList.indexOf(row);
					table.addRowSelectionInterval(filteredDataRowIndex, filteredDataRowIndex);
				}
			}
		});
	}
	
	private final class SimplePickList extends JPanel {

		final private PersonalizableTableModel model;
		final private JButton pickDescriber, cancelButton;
		final private JDialog dialog;
		private Collection<Row> picked;

		private void userPickActionPerformed() {
			picked = model.getSelectionsInDescendingOrder(true);
			if (picked.size() > 0) {
				dialog.dispose();
			}
		}

		Collection<Row> getPicked() {
			return picked;
		}

		SimplePickList(Collection<Row> values, final String title, final String pickVerb, final Properties properties) {
			DefaultPersonalizableDataSource defaultDataSource = new DefaultPersonalizableDataSource(
					(List<Row>) Arrays.asList(values.toArray(new Row[0])),dataSource.getMetaRow());
			model = PersonalizableTableModel.activate(defaultDataSource,
					properties, true);
			model.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent e) {
					
					int[] selected = model.getModelShowing().table.getSelectedRows();
					final int n = model.getModelShowing().getSelectedDataRowCount(selected);
					if (n > 0) {		
						final String singularOrPlural = (n == 1)? " item":" items"; 
						pickDescriber.setText(pickVerb + " " + n + singularOrPlural);
						SwingBasics.setEnabled(pickDescriber, true);
					}
					else {
						pickDescriber.setText(pickVerb);
						SwingBasics.setEnabled(pickDescriber, false);
					}
				}
			});
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			PersonalizableTable table = new PersonalizableTable(model);
			add(table.makeHorizontalScrollPane(), BorderLayout.CENTER);
			JPanel buttonPanel = new JPanel();
			pickDescriber = SwingBasics.getButton(pickVerb, MmsIcons
					.getYesIcon(), 'p', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					userPickActionPerformed();
				}
			}, Basics.toHtmlUncentered(pickVerb,
					"Select the current selections"));

			cancelButton = SwingBasics.getButton("Cancel", MmsIcons
					.getCancelIcon(), '\0', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					picked = null;
					dialog.dispose();
				}
			}, "Close Window");

			Action escAction = new AbstractAction() {
				public void actionPerformed(ActionEvent evt) {
					cancelButton.doClick(150);
				}

				public boolean isEnabled() {
					return (cancelButton != null) && (cancelButton.isEnabled());
				}
			};
			getActionMap().put("esc-action", escAction);
			InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
			im.put(key, "esc-action");
			buttonPanel.add(pickDescriber);
			buttonPanel.add(cancelButton);

			add(buttonPanel, BorderLayout.SOUTH);
			setAutoFilter(true);

			dialog = SwingBasics.getDialog(null);
			dialog.setTitle(title);
			dialog.setModal(true);
			dialog.getContentPane().add(this, BorderLayout.CENTER);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(final WindowEvent e) {
					picked = null;
					dialog.dispose();
				}
			});
			final JPanel jp = new JPanel();
			final JLabel s = new JLabel();
			jp.add(s);
			this.model.setSizeInfo(s);
			dialog.getContentPane().add(jp, BorderLayout.SOUTH);
			dialog.pack();
			this.model.setPersonalizableWindowOwner(dialog);
			SwingBasics.setEnabled(pickDescriber, false);
			this.pickDescriber.requestFocus();
			model.setPersonalizableWindowOwner(dialog);
			SwingBasics.windowActivated(dialog);
			dialog.getRootPane().setDefaultButton(pickDescriber);
			dialog.setVisible(true);
		}

	}

	private String toHtml(final boolean askIfSelected) {
		List<Row> l = getSelectedRowsInAscendingOrder();
		final int n=getDataRowCount();
		if (askIfSelected && l!=null && l.size()>0 && l.size()<n){
			final String []choices=new String[]{"Yes", Basics.concatObjects("No .. browse all ",n," rows")};
			final int choice=PopupBasics.getRadioButtonOption(
					getTearAwayComponent(), 
					Basics.concatObjects("<html>Only browse your ", l.size(), " selected rows?</html>"), 
					"Browse options", choices, 0, true, true);
			if (choice==1){
				l.clear();
			} else if (choice==-1){//cancel
				return null;
			}
		}
		final int indexes[];
		if (Basics.isEmpty(l)) {
			l = dataSource.getFilteredDataRows();
			indexes = null;
		} else {
			indexes = getSelectedDataIndexes();
		}
		final String page;
		if (l.size() == 1) {
			page = toHtmlVertical(l.get(0), indexes == null ? 0 : indexes[0]);
		} else {
			final TableColumnModel tcm = table.tcm;
			final int columns = tcm.getColumnCount();
			final StringBuilder sb = new StringBuilder();
			sb.append("<html><body><table><tr bgcolor='#DCDCDC'>");
			for (int i = 0; i < columns; i++) {
				final TableColumn tc = tcm.getColumn(i);
				final int modelColumnIndex = tc.getModelIndex();
				final String name = (String) modelColumnIdentifiers
						.get(modelColumnIndex);
				sb.append("<td>");
				final SortInfo si = findSortInfo(getDataColumnIndex(modelColumnIndex));
				if (si != null) {
					sb.append(si.ascending ? "&#8593;" : "&#8595;");
					sb.append(si.sortOrder);
					sb.append(". ");
				}
				final int dc = getDataColumnIndex(modelColumnIndex);
				final String str=getColumnLabel(dc);
				if (str.toLowerCase().contains("<html>")){
					sb.append(Basics.stripHeaderHtml(str));
				} else{
					sb.append(Basics.encodeHtml(str));
				}
				sb.append("</td>");
			}
			final CellAdvice cellAdvice = new CellAdvice();
			sb.append("</tr>\n");
			for (int rowIndex = 0; rowIndex < l.size(); rowIndex++) {
				sb.append("<tr bgcolor='");
				if (rowIndex < 2 && showFilterUI) {
					sb.append("#C0C0C0");
				} else if ((rowIndex % 2) == 0) {
					sb.append("#f0f8ff");
				} else {
					// sb.append("#e6e6fa");
					sb.append("#c6deff");
				}
				sb.append("'>");
				for (int i = 0; i < columns; i++) {
					final TableColumn tc = tcm.getColumn(i);
					final int mc = tc.getModelIndex();
					final int dc = getDataColumnIndex(mc);
					final Class c = getColumnClass(mc);
					final Row row=l.get(rowIndex);
					final Object value = row.get(dc);
					String str=this.toSequenceableString(row, dc);
					if (str.toLowerCase().contains("<html>")){
						sb.append("<td>");
						str=Basics.stripHeaderHtml(str);
						sb.append(str);
					} else if (value == null) {
						sb.append("<td>");
					} else {
						sb.append("<td ");
						if (c.equals(Float.class) || c.equals(Integer.class)
								|| c.equals(Date.class)) {
							sb.append("align='right'");
						}
						boolean errorFont = false;
						final int rowIdx = indexes == null ? rowIndex : indexes[rowIndex];
						if (setCellAdvice(rowIdx, i, cellAdvice)) {
							sb.append(" title='");
							final String toolTip = Basics
									.stripBodyHtml(cellAdvice.toolTip);
							sb.append(Basics.encodeHtml(toolTip));
							sb.append("'>");
							if (cellAdvice.type == CellAdvice.TYPE_ERROR) {
								sb.append("<font color='red'><b>");
								errorFont = true;
							}
						} else {
							sb.append(">");
						}
						if ((!showFilterUI || rowIdx != PersonalizableTable.ROW_IDX_FILTER_OP)
								&& (c.equals(Boolean.class) || c
										.equals(ComparableBoolean.class))) {
							if (value.toString().equalsIgnoreCase("true")) {
								sb.append("&#8730;");
							} else {
								sb.append("&#8709;");
							}
						} else {
								sb.append(Basics.encodeHtml(str));
							
						}
						if (errorFont) {
							sb.append("</b></font>");
						}
					}
					sb.append("</td>");
				}
				sb.append("</tr>");
				Basics.eol(sb);
			}
			sb.append("</table></body></html>");
			page = sb.toString();
		}
		// IoBasics.saveTextFile("C:\\temp.html", page);

		return page;
	}

	private String toHtmlVertical(final Row row, final int rowIdx) {
		final StringBuilder sb = new StringBuilder("<html><table>");
		final TableColumnModel tcm = table.tcm;
		final int columns = tcm.getColumnCount();
		final CellAdvice cellAdvice = new CellAdvice();
		for (int i = 0; i < columns; i++) {
			final TableColumn tc = tcm.getColumn(i);
			sb.append("<tr>");
			final int mc = tc.getModelIndex();
			final int dc = getDataColumnIndex(mc);
			final Class c = getColumnClass(mc);
			final Object object = row.get(dc);
			String value=this.toSequenceableString(row, dc);
			if (value.toLowerCase().contains("<html>")){
				value=Basics.stripHeaderHtml(value);
			}else {
				value=Basics.encodeHtml(value);
			}
			sb.append("<td bgcolor='#C0C0C0'>");

			final String str=getColumnLabel(dc);
			if (str.toLowerCase().contains("<html>")){
				sb.append(Basics.stripHeaderHtml(str));
			} else{
				sb.append(Basics.encodeHtml(str));
			}

			sb.append("</td>");
			sb.append("<td bgcolor='");
			if ((i % 2) == 0) {
				sb.append("#f0f8ff");
			} else {
				sb.append("#c6deff");
			}
			sb.append("'");

			if (value == null) {
				sb.append(">");
			} else {
				sb.append(" ");
				if (c.equals(Float.class) || c.equals(Integer.class)
						|| c.equals(Date.class)) {
					sb.append("align='right'");
				}
				boolean errorFont = false;

				if (setCellAdvice(rowIdx, i, cellAdvice)) {
					sb.append(" title='");
					final String toolTip = Basics
							.stripBodyHtml(cellAdvice.toolTip);
					sb.append(Basics.encodeHtml(toolTip));
					sb.append("'>");
					if (cellAdvice.type == CellAdvice.TYPE_ERROR) {
						sb.append("<font color='red'><b>");
						errorFont = true;
					}
				} else {
					sb.append(">");
				}
				if ((!showFilterUI || rowIdx != PersonalizableTable.ROW_IDX_FILTER_OP)
						&& (c.equals(Boolean.class) || c
								.equals(ComparableBoolean.class))) {
					if (value.toString().equalsIgnoreCase("true")) {
						sb.append("&#8730;");
					} else {
						sb.append("&#8709;");
					}
				} else {
					sb.append(value);
				}
				if (errorFont) {
					sb.append("</b></font>");
				}
			}
			sb.append("</td>");
			sb.append("</tr>");
			Basics.eol(sb);
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	void sortAndRepaint() {
		sort();
		table.getTableHeader().repaint();
		table.repaint();
		columnFreezer.repaint();
	}

	public PersonalizableTable getTable() {
		return table;
	}

	public void saveAsHtml() {
		final String html=toHtml(true);
		if (!Basics.isEmpty(html)){
			final String fileName = PopupBasics.getFileName("html",
				"Hyper-text markup language", false, null, containingWnd);
			if (!Basics.isEmpty(fileName)) {
				IoBasics.saveTextFile(fileName, html);
			}
		}
	}

	public void showAsHtml() {
		final String html=toHtml(true);
		if (!Basics.isEmpty(html)){
			SwingBasics.showHtml("XpertGrid", html, false);
		}
	}

	boolean useDittos = false;

	public void setUseDittos(boolean useDittos) {
		this.useDittos = useDittos;
	}

	public void printJob() {
		// JPrintableEditorPane.printHtml("Converting table to html document",
		// toHtml());
		JTablePrint.print(table);
	}
	
	Integer restartWithGroupOption = null;

	public void setGroupOptionRestart(int ci) {
		restartWithGroupOption = new Integer(ci);
	}

	public int[] getSort() {
		return getSort(getAllSortInfo());
	}

	private SortInfo[] getSortInfo(final int[] sortOrder,
			final boolean[] ascending) {
		final SortInfo[] si = new SortInfo[sortOrder.length];
		for (int i = 0; i < sortOrder.length; i++) {
			si[i] = new SortInfo(metaRow, sortOrder[i], i + 1,
					ascending == null ? true : ascending[i]);
		}
		return si;
	}

	String getSeivedPrefix() {
		if (columnsForGroupSeive == null) {
			return "s["
					+ (columnsToSort.size() == 0 ? "*" : getCurrentSort("."))
					+ "]"; // Basics.toString(getSort()):
		}
		final StringBuilder sb = new StringBuilder("d[");
		for (int i = 0; i < columnsForGroupSeive.length; i++) {
			sb.append(metaRow.getDataColumnIdentifier(columnsForGroupSeive[i]));
		}
		sb.append("]");
		return sb.toString();
	}

	int[] columnsForGroupSeive;
	boolean distinctRowsForGroupSeive;

	final void setDistinctGroupSeive(final int[] distinctGroupSeive) {
		this.columnsForGroupSeive = distinctGroupSeive;
	}

	public GroupedDataSource group(final int[] sortOrder,
			final int[] columnsForGroupSeive,
			final boolean distinctRowsForGroupSeive) {
		// get root table model
		final PersonalizableTableModel utm = getUngroupedModel();
		utm.specialSortOrder = sortOrder;
		utm.columnsForGroupSeive = columnsForGroupSeive;
		utm.distinctRowsForGroupSeive = distinctRowsForGroupSeive;
		group(sortOrder);
		return utm.groupedDataSource;
	}

	int[] specialSortOrder;

	private void group(final int[] sortOrder) {
		ungroupIfNecessary();
		// get root table model
		final PersonalizableTableModel utm = getUngroupedModel();
		utm.sort(getSortInfo(sortOrder, null));
		utm.sort();
		utm._group();
	}

	public void group() {
		ungroupIfNecessary();

		// get root table model
		final PersonalizableTableModel utm = getUngroupedModel();
		utm.mostRecentlyClickedNode=null;
		utm.specialSortOrder = null;
		utm.columnsForGroupSeive = null;
		utm.distinctRowsForGroupSeive = false;
		utm._group();
	}

	private void _group() {
		setGroupOption(GROUP_BY_TREE);
	}

	public void clearGroupOptionRestart() {
		restartWithGroupOption = null;
	}

	public static final int NO_GROUPING = -1, GROUP_BY_TREE = 0;
	int groupOption = NO_GROUPING;

	public int getGroupOption() {
		return groupOption;
	}

	public boolean groupIfNecessary() {
		if (doCustomGroupView()) {
			return true;
		}
		int c = groupOption;
		setGroupOption(groupOption);
		return c == GROUP_BY_TREE;
	}

	boolean firstTimeAncestorWasAdded = true;
	boolean allowsTableShowing=true;

	public void setAllowTableShowing(final boolean ok) {
		allowsTableShowing=ok;
	}

	public boolean hasBeenShownAtSomePoint() {
		return !firstTimeAncestorWasAdded;
	}

	public boolean ungroupIfNecessary() {
		return ungroupIfNecessary(true);
	}

	private boolean ungroupIfNecessary(boolean saveProperties) {
		if (table != null && table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		int modelType = getModelType();
		if ((modelType == TYPE_GROUPED)) {
			((GroupedDataSource) dataSource).seeOnlyTokens=null;
			// this data souce is the condensing data source
			((GroupedDataSource) dataSource).ungroup(saveProperties);
			return true;
		} else if (modelType == TYPE_GROUP_SEIVED) {
			// this data source is seived by the condensed data source
			groupedDataSource.seeOnlyTokens=null;
			groupedDataSource.ungroup(saveProperties);
			return true;
		} else if (groupedDataSource != null) {
			groupedDataSource.ungroup(saveProperties);
			
		}
		return false;
	}

	boolean initializingPicks = false;
	boolean verticalSplit = false;

	public void setVerticalSplit(boolean on) {
		verticalSplit = on;
	}

	private void showInitialPicks() {
		if (groupOption == NO_GROUPING) {
			if (!Basics.isEmpty(applicationSpecificTreeSort)){
				return;
			}
			PersonalizableDataSource.CanEditPriorPicks cepp = null;
			if (dataSource instanceof PersonalizableDataSource.CanEditPriorPicks) {
				cepp = (PersonalizableDataSource.CanEditPriorPicks) dataSource;
				cepp.initPicks();
				initializingPicks = true;
				final int[] di = cepp.getSelectedRows();
				if (di != null) {
					PersonalizableTableModel tm = getUngroupedModel();
					tm.table.clearSelection();
					for (int i = 0; i < di.length; i++) {
						final int dataColumnIndex = getVisualRowIndex(di[i]);
						if (i == 0) {
							tm.table.setRowSelectionInterval(dataColumnIndex,
									dataColumnIndex);
						} else {
							tm.table.addRowSelectionInterval(dataColumnIndex,
									dataColumnIndex);
						}
					}
				}
				initializingPicks = false;
			}
		} else if (groupOption == GROUP_BY_TREE) {
			getUngroupedModel().ungroupIfNecessary();
			getUngroupedModel().setGroupOption(
					PersonalizableTableModel.GROUP_BY_TREE);
		}
	}

	public boolean showToolBarPanelInTree= false;
	
	public void setGroupOption(final int groupOption) {
		this.groupOption = groupOption;

		closeFindWindow();
		if (groupOption != NO_GROUPING) {
			PersonalizableDataSource.CanEditPriorPicks cepp = null;
			if (dataSource instanceof PersonalizableDataSource.CanEditPriorPicks) {
				cepp = (PersonalizableDataSource.CanEditPriorPicks) dataSource;
				cepp.initPicks();
			}
			GroupedDataSource.activate(this, groupOption);
		} else {
			showInitialPicks();
		}
	}

	private static String rootFolder = null, rootSequenceFolder = null,
			SORT_SEQUENCE_SUB_FOLDER = "sortSequences";

	static String getPropertyRootFolder() {
		return rootFolder;
	}

	public static void setPreferencesFolders(String productDir) {
		try {
			rootFolder = productDir + File.separatorChar + 
				"My Table Views" + File.separatorChar;
			IoBasics.mkDirs(rootFolder);
			rootSequenceFolder = IoBasics.concat(rootFolder,
					SORT_SEQUENCE_SUB_FOLDER);
			IoBasics.mkDirs(rootSequenceFolder);
			initGlobalProperties();
		} catch (final SecurityException e) {
			rootFolder = "";
			rootSequenceFolder = "";
		}
	}

	public static String getPropertyFolder(final String classKey,
			final String key) {
		final String s = IoBasics.replaceFilenameAllergicChars(classKey + " "
				+ (key == null ? "miscellaneous" : key));
		return rootFolder + s + " views" + File.separatorChar;
	}

	public static void setRootDir(final String dir) {
		rootFolder = dir;
		if (!rootFolder.endsWith(File.separator)) {
			rootFolder += File.separator;
		}
		IoBasics.mkDirs(rootFolder);
		rootSequenceFolder = IoBasics.concat(rootFolder,
				SORT_SEQUENCE_SUB_FOLDER);
		IoBasics.mkDirs(rootSequenceFolder);

		initGlobalProperties();
	}

	static Properties globalProperties;

	public static String getGlobalPropertyFileName() {
		return rootFolder + "com_MeehanMetaSpace_Swing.properties";
	}

	public static Properties getGlobalProperties() {
		return globalProperties;
	}

	public static Color getGlobalAlternatingRowColor() {
		if (globalAlternatingRowColor == null) {
			initGlobalProperties();
		}
		return globalAlternatingRowColor;
	}
	
	public static void setGlobalAlternatingColor(Color color) {
		globalAlternatingRowColor = color;
	}

	public static Color DEFAULT_ALTERNATING_COLOR= new Color(228, 254, 225);
	static void initGlobalProperties() {
		globalAlternatingRowColor = DEFAULT_ALTERNATING_COLOR;
		final String fileName = getGlobalPropertyFileName();

		globalProperties = PropertiesBasics.loadProperties(fileName);
		globalAlternatingRowColor = new Color(PropertiesBasics.getProperty(
				globalProperties, PROPERTY_ALTERNATING_COLOR,
				globalAlternatingRowColor.getRGB()));
		globalShowTreeCounts = PropertiesBasics.getProperty(globalProperties,
				PROPERTY_TREE_SHOW_COUNTS, PROPERTY_DEFAULT_TREE_SHOW_COUNTS);
		TREE_MULTI_SELECT_WITH_KEY = PropertiesBasics.getProperty(
				globalProperties, PROPERTY_TREE_MULTI_SELECT_WITH_KEY, true);

	}

	private static boolean globalShowTreeCounts;

	static Color globalAlternatingRowColor;
	Color instanceAlternatingRowColor;
	
	public void setInstanceAlternatingColor(Color color) {
		instanceAlternatingRowColor = color;
	}
	
	boolean allowOneClickMultiSelect = false;

	public void allowOneClickMultiSelect() {
		allowOneClickMultiSelect = true;
	}

	static boolean TREE_MULTI_SELECT_WITH_KEY;

	static boolean setTreeMultiSelectWithKey() {
		TREE_MULTI_SELECT_WITH_KEY = !TREE_MULTI_SELECT_WITH_KEY;
		if (globalProperties == null) {
			initGlobalProperties();
		}
		globalProperties.setProperty(PROPERTY_TREE_MULTI_SELECT_WITH_KEY, ""
				+ TREE_MULTI_SELECT_WITH_KEY);
		PropertiesBasics.saveProperties(globalProperties,
				getGlobalPropertyFileName(), "");
		return TREE_MULTI_SELECT_WITH_KEY;
	}

	static boolean setTreeShowCounts() {
		globalShowTreeCounts = !globalShowTreeCounts;
		if (globalProperties == null) {
			initGlobalProperties();
		}
		globalProperties.setProperty(PROPERTY_TREE_SHOW_COUNTS,
				globalShowTreeCounts ? "true" : "false");
		PropertiesBasics.saveProperties(globalProperties,
				getGlobalPropertyFileName(), "");
		return globalShowTreeCounts;
	}

	public static boolean getTreeShowCounts() {
		return globalShowTreeCounts;
	}

	static void setGlobalAlternatingRowColor(final Color newGlobalColor) {
		globalAlternatingRowColor = newGlobalColor;
		if (globalProperties == null) {
			initGlobalProperties();
		}
		globalProperties.setProperty(PROPERTY_ALTERNATING_COLOR, ""
				+ newGlobalColor.getRGB());
		PropertiesBasics.saveProperties(globalProperties,
				getGlobalPropertyFileName(), "");
	}

	String classKey = "C";

	public void setClassKey(final String classKey) {
		this.classKey = classKey;
	}

	private boolean firstAccessPropertyDir = false;

	public String getPropertyFolder() {
		final String dir = getPropertyFolder(classKey, key);
		IoBasics.mkDirs(dir);
		firstAccessPropertyDir = true;
		return dir;
	}

	private String getViewFileName(final boolean readOnly) {
		String toolTip, button;
		if (readOnly) {
			toolTip = "Load view/layout from this file";
			button = "Load view/layout";
		} else {
			toolTip = "Save view/layout to this file";
			button = "Save view/layout";
		}
		return PopupBasics.getFileName("ptv", "Table view/layout", button,
				toolTip, readOnly, getPropertyFolder(), null, getWindow());
	}

	private Window getWindow() {
		final Window value;
		if (containingWnd != null) {
			value = containingWnd;
		} else {
			value = SwingUtilities.getWindowAncestor(getUngroupedModel()
					.getTearAwayComponent());
		}
		return value;
	}

	private void resetCurrentView(final Properties p) {
		if (p != null) {
			ungroupIfNecessary();
			final PersonalizableTableModel uncondensedModel = getUngroupedModel();
			final Map<String, String> rlbn = new HashMap<String, String>();

			if (uncondensedModel.everyWhereColumns != null){
				for (final String columnIdentifer:uncondensedModel.everyWhereColumns){
					uncondensedModel.addColumnEveryWhere(p, columnIdentifer);
					rlbn.put(columnIdentifer, uncondensedModel.renamedLabelsByIdentifier.get(columnIdentifer));
				}
			}
			uncondensedModel.setProperties(p);
			for (final String columnIdentifier:rlbn.keySet()){
				setColumnLabel(getDataColumnIndexForIdentifier(columnIdentifier), rlbn.get(columnIdentifier), true);
			}
			uncondensedModel.groupIfNecessary();
			refreshShowingTable(true);
		}
	}

	private void openView() {
		final String fileName = getViewFileName(true);
		if (!Basics.isEmpty(fileName)) {
			final Properties p = PropertiesBasics.loadProperties(fileName);
			resetCurrentView(p);
			notifyViewChanged();
		}
	}

	private void saveView() {
		final String fileName = getViewFileName(false);
		if (!Basics.isEmpty(fileName)) {
			PropertiesBasics.saveProperties(getUngroupedModel()
					.updatePropertiesWithPersonalizations(true), fileName, "");
		}
	}

	void popup() {
		final Component c = table.isEditing() ? table.getEditorComponent()
				: table.getTableHeader();
		final Point p = c.getLocation();
		popupMenu(c, p.x, p.y);
	}

	public interface DefaultViewRestorer {
		Properties getDefaultView();
	}

	DefaultViewRestorer defaultViewRestorer = null;

	public void setDefaultViewRestorer(DefaultViewRestorer defaultViewRestorer) {
		this.defaultViewRestorer = defaultViewRestorer;
		if (defaultViewRestorer != null){
			SwingBasics.echoAction(table, restoreDefaultViewItem,
					restoreDefaultViewAction, restoreDefaultViewKeyStroke, 'r');

		}
	}

	public JMenu getViewMenu(JComponent component) {
		return getViewMenu(component, null, null, null);
	}

	public JComponent getContainer() {
		final PersonalizableTableModel u = getUngroupedModel();
		return u.table.getContainer();
	}

	public JComponent getContainer(final String label) {
		final PersonalizableTableModel u = getUngroupedModel();
		return u.table.getContainer(label);
	}

	private JComponent specialTearAway;
	public void setTearAwayComponent(final JComponent c){
		specialTearAway=c;
	}
	
	public JComponent getTearAwayComponent() {
		final PersonalizableTableModel u = getUngroupedModel();
		if (specialTearAway != null){
			return specialTearAway;
		}
		if (tabs == null) {
			if (u.groupedDataSource != null) {
				return u.groupedDataSource.splitPane;
			}
			return table.scrollPane;
		}
		return tabs;
	}

	public JComponent getSeperateWindowPanel(final JComponent tornAway) {
		final JComponent jp;
		if (PersonalizableTable.containerBuilder != null) {
			jp = PersonalizableTable.containerBuilder.configure("", tornAway,
					this.table, true);
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					if (!reTabIfNecessary(true)){
					if (isGrouped()){
						ungroupIfNecessary();
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								group();
							}
							});
					}
					}
				}

			});
		} else {
			jp = new JPanel();
		}
		return jp;
	}

	static JLabel oneMomentLabel = new JLabel(
			"One moment, building display...", JLabel.CENTER);

	public static void setOneMomentDisplay(final JLabel l) {
		oneMomentLabel = l;
	}

	private Component oldOneMomentDisplay;
	private JScrollPane oldScrollPane;
	private int oneMomentDisplaying = 0;

	public int getOneMomentDisplaying() {
		return oneMomentDisplaying;
	}

	private ArrayList<String[]>treeViewInPriorMoment;
	public void showOneMomentDisplay() {
		if (oneMomentDisplaying == 0	) {
			treeViewInPriorMoment=null;
			final JScrollPane scrollPane = getScrollPane();
			if (scrollPane != null) {
				oldOneMomentDisplay = scrollPane.getViewport().getView();
				oldScrollPane = scrollPane;
				treeViewInPriorMoment=saveTreeView();
				scrollPane.setViewportView(oneMomentLabel);
			}
			GradientBasics.setTransparentChildren(scrollPane, false);
		}
		oneMomentDisplaying++;
	}

	public void hideOneMomentDisplay() {
		if (oneMomentDisplaying > 0) {
			oneMomentDisplaying--;
		}
		if (oneMomentDisplaying == 0 && oldOneMomentDisplay != null) {
			oldScrollPane.setViewportView(oldOneMomentDisplay);
			if (treeViewInPriorMoment != null){
				restoreTreeView(treeViewInPriorMoment);
			}
		}

	}

	private JScrollPane getScrollPane() {
		final PersonalizableTableModel u = getUngroupedModel();
		if (u.groupedDataSource != null) {
			return u.groupedDataSource.scrollPane;
		}
		return table.scrollPane;
	}

	public Properties getPropertiesForTearAway() {
		return getUngroupedModel().properties;
	}

	public PropertiesBasics.Savior getPropertiesSaviorForTearAway() {
		return new PropertiesBasics.Savior() {
			public void save(final Properties properties) {
				getUngroupedModel().notifyViewChanged();
			}
		};

	}

	public String getPropertiesPrefixForTearAway() {
		return "tearAway";
	}

	private boolean canTearAway = true;

	public void setCanTearAway(final boolean ok) {
		final PersonalizableTableModel u = getUngroupedModel();
		u.canTearAway = ok;
	}

	public boolean supportsTearAway() {
		return canTearAway && dataSource.getMaximumCardinality() > 1 && !SwingBasics.isModalActive;
	}

	TearAway.Handler tearAwayHandler = new TearAway.Handler(this);

	public void disposeTreeAndTimer() { // cannot inherit from protege interface 'cos 2
		if (groupedDataSource!=null){
			groupedDataSource.dispose();
			groupedDataSource=null;
		}

	}
	public void dispose() { // cannot inherit from protege interface 'cos 2
		// packages are decoupled
		final PersonalizableTableModel u = getUngroupedModel();
		if (u.tearAwayHandler != null) {
			u.tearAwayHandler.dispose();
		}
		disposeTreeAndTimer();
		u.closeBuildingTreeWindow();
	}

	public TearAway.Handler getTearAwayHandler() {
		return getUngroupedModel().tearAwayHandler;
	}

	public void closeTearAway(){
		final PersonalizableTableModel u = getUngroupedModel();
		if (u.tearAwayHandler != null) {
			u.tearAwayHandler.closeTearAway();
		}
	}
	public void toggleTearAway() {
		final PersonalizableTableModel u = getUngroupedModel();
		if (u.tearAwayHandler != null) {
			u.tearAwayHandler.setTableModel(u);
			u.tearAwayHandler.toggleTearAway();
		}
	}

	private JMenu getViewMenu(final JComponent component,
			final JMenuItem p_openViewItem, final JMenuItem p_copyViewItem,
			final JMenuItem p_pasteViewItem) {
		final JMenu menu = new JMenu("Manage view");
		menu.setIcon(MmsIcons.getWeatherCloudyIcon());
		menu.setMnemonic('v');
		final JMenuItem saveView = new JMenuItem("Save this view", MmsIcons
				.getTableSaveIcon());
		saveView.setMnemonic('v');
		saveView.setAccelerator(saveViewKeyStroke);	
		menu.add(saveView);
		saveView.addActionListener(saveViewAction);
		final JMenuItem openViewItem;
		if (p_openViewItem == null) {
			openViewItem = new JMenuItem();
			SwingBasics.echoAction(component, openViewItem, openViewAction,
					openViewKeyStroke, 'o');
		} else {
			openViewItem = p_openViewItem;
			openViewItem.setMnemonic('o');
		}
		openViewItem.setText("Open different view");
		openViewItem.setIcon(MmsIcons.getOpenIcon());
		menu.add(openViewItem);

		final PersonalizableTableModel utm = getUngroupedModel();
		if (utm.defaultViewRestorer != null) {
			menu.add(restoreDefaultViewItem);
			restoreDefaultViewItem.setMnemonic('r');
			SwingBasics.echoAction(component, restoreDefaultViewItem,
					restoreDefaultViewAction, restoreDefaultViewKeyStroke, 'r');
		}

		final JMenuItem copyViewItem;
		if (p_copyViewItem == null) {
			copyViewItem = new JMenuItem();
			SwingBasics.echoAction(component, copyViewItem, copyViewAction,
					copyViewKeyStroke, 'c');
		} else {
			copyViewItem = p_copyViewItem;
			copyViewItem.setMnemonic('c');
		}
		copyViewItem.setText("Copy");
		copyViewItem.setIcon(MmsIcons.getCopyIcon());
		menu.add(copyViewItem);

		final JMenuItem pasteViewItem;
		if (p_pasteViewItem == null) {
			pasteViewItem = new JMenuItem();
			SwingBasics.echoAction(component, pasteViewItem, pasteViewAction,
					pasteViewKeyStroke, 'p');
		} else {
			pasteViewItem = p_pasteViewItem;
			pasteViewItem.setMnemonic('p');
		}
		pasteViewItem.setText("Paste view");
		pasteViewItem.setIcon(MmsIcons.getPasteIcon());
		pasteViewItem.setEnabled(false);
		menu.add(pasteViewItem);
		if (p_openViewItem != null && saveAsEditMoreView != null) {
			menu.addSeparator();
			menu.add(saveAsEditMoreView);
			menu.add(restoreEditMoreView);
			menu.add(saveAsEditLessView);
			menu.add(restoreEditLessView);
		}
		return menu;
	}

	private static Properties clipBoardProperties;
	private final static String PROPERTY_PASTE_PREFIX = "pastePrefix";
	private static String clipBoardKey;
	private JButton popupTableDoneButton;

	private JDialog dlg = null;

	private JPanel getMultiQueryPanel(final JComboBox logialOperatorComboBox, final JButton prevQuery, final JButton addQuery){
		final JPanel multiQueryPanel=new JPanel(new BorderLayout());
		final JPanel scrollQueryPanel=new JPanel(new BorderLayout());
		final JPanel addQueryPanel=new JPanel();
		final JLabel label=new JLabel("Operator:");
		logicOperator=QuerySet.LOGICAL_OR;
		logialOperatorComboBox.addItem(QuerySet.LOGICAL_OR);
		logialOperatorComboBox.addItem(QuerySet.LOGICAL_AND);
		logialOperatorComboBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				logicOperator=(String) logialOperatorComboBox.getSelectedItem();
			}
		});
		scrollQueryPanel.add(prevQuery, BorderLayout.WEST);
		addQueryPanel.add(label);
		addQueryPanel.add(logialOperatorComboBox);
		addQueryPanel.add(addQuery);		
		//addQueryPanel.setPreferredSize(new Dimension(addQueryPanel.getWidth(),30));
		if (filterMessagePane == null) {
			filterMessagePane = new JEditorPane();
			filterMessagePane.setEditable(false);
			filterMessagePane.setContentType("text/html");
		}
		scrollQueryPanel.add(addQueryPanel, BorderLayout.EAST);
		multiQueryPanel.add(scrollQueryPanel,BorderLayout.NORTH);		
		multiQueryPanel.setPreferredSize(new Dimension(520,30));
		return multiQueryPanel;
	}
	
	
	boolean popupTable(final JComponent mostBottomEastComponent, final JComponent bottomCenter, final Image image,
			final String wprop, final String title,
			final RotateTable rotateTable, final JComponent[] extraComponents,
			final int startEditingRowIndex, final int startEditingModelIndex,
			final JButton doneButton, final String doneToolTip,
			final String doneText, final boolean supportsCancel,
			final String cancelToolTip, final String cancelText,
			final boolean sizeMatters) {
		
		final ArrayList status = new ArrayList();
		status.add("ok");
		dlg = SwingBasics.getModalDialog(getModelShowing().table, image, title);
		final JPanel mainPanel = new JPanel(new BorderLayout());
		dlg.getContentPane().add(mainPanel);
		final JScrollPane jsp = rotateTable.getScrollPane();
		jsp.setBorder(BorderFactory.createEmptyBorder(9, 12, 19, 10));
		mainPanel.add(jsp, BorderLayout.CENTER);
		rotateTable.setColumnSelectionAllowed(true);
		rotateTable.setRowSelectionAllowed(true);
		rotateTable.setRowSelectionInterval(0, 0);
		rotateTable.setColumnSelectionInterval(rotateTable.getColumnCount()-1, rotateTable.getColumnCount()-1);
		rotateTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				updateMultiQueryText();
			}
		
		});
		if (startEditingRowIndex > -1 && startEditingModelIndex > -1) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					rotateTable.setRowSelectionInterval(startEditingRowIndex,
							startEditingRowIndex);
					rotateTable.setColumnSelectionInterval(
							startEditingModelIndex-1, startEditingModelIndex-1);
					SwingBasics.scrollToVisible(rotateTable,
							startEditingRowIndex, 2);
				}
			});

		}
		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent event) {
				final TableCellEditor tce = rotateTable.getCellEditor();
				if (tce != null) {
					tce.stopCellEditing();
				}
			}
		});
		
		final JPanel bottomPanel=new JPanel(new BorderLayout());
		final JPanel extraComponentButtonPanel = SwingBasics
				.getButtonPanel(2 + (extraComponents == null ? 0
						: extraComponents.length));
		final JPanel mostSouthernPanel=new JPanel(new BorderLayout(2,2));
		mostSouthernPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 1, 10));
		mostSouthernPanel.add(mostBottomEastComponent, BorderLayout.EAST);
		final JPanel jp=new JPanel();
		jp.add(extraComponentButtonPanel);
		mostSouthernPanel.add(jp, BorderLayout.NORTH);
		int bottomHeight=15;
		if (bottomCenter != null) {
			bottomPanel.add(bottomCenter, BorderLayout.CENTER);
			final Dimension d=bottomCenter.getPreferredSize();
			//bottomPanel.setPreferredSize(new Dimension(d.width,d.height+ 30));
			bottomHeight += d.height;
		}
		bottomPanel.add(mostSouthernPanel,BorderLayout.SOUTH);
		if (extraComponents != null) {
			for (int i = 0; i < extraComponents.length; i++) {	
				extraComponentButtonPanel.add(extraComponents[i]);
			}
		}
		popupTableDoneButton = SwingBasics.getDoneButton(dlg, doneToolTip,
				false);
		if (doneText != null) {
			popupTableDoneButton.setText(doneText);
			//popupTableDoneButton.setMnemonic(doneText.charAt(0));
		} else if (supportsCancel) {
			SwingBasics.setOkButton(popupTableDoneButton);
		}
		SwingBasics.ignoreKeyEvent(rotateTable, KeyEvent.VK_ENTER);

		rotateTable.getInputMap(rotateTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.getParent().remove(
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		if (doneButton == null) {
			extraComponentButtonPanel.add(popupTableDoneButton);
		} else {
			dlg.getRootPane().setDefaultButton(doneButton);
		}
		if (supportsCancel) {
			final JButton cancelButton = SwingBasics.getCancelButton(dlg,
					cancelToolTip, true, new ActionListener() {
						public void actionPerformed(final ActionEvent ae) {
							status.clear();
						}
					});
			if (cancelText != null) {
				cancelButton.setText(cancelText);
				cancelButton.setMnemonic(cancelText.charAt(0));
			}
			extraComponentButtonPanel.add(cancelButton);
			
		}
		
		mainPanel.add(bottomPanel,BorderLayout.SOUTH);
		final int rows = rotateTable.getRowCount();
		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				rotateTable.dispose();
			}
		});
		final boolean hadBeenPersonalized=SwingBasics.packAndPersonalize(dlg, null, null, key + "." + wprop,true, sizeMatters, true);
		dlg.setResizable(true);
		final Dimension screenSize = dlg.getToolkit().getScreenSize();
		// No check needed of FocusFreeze.isFrozen
		rotateTable.requestFocus();
		Rectangle prior = null;
		//supressUpdateUI = true;
		initFindFilterButtons();
		// dlg.show(); - deprecated
		if (PopupBasics.location!=null){
			RotateTable.Editor tce=null;
			if (rotateTable.getRowCount()==1){
				tce=(RotateTable.Editor )rotateTable.getCellEditor(0, 0);
				if (tce.source instanceof AutoComplete.CellEditor){
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							AutoComplete.CellEditor.showPopupOnFirstFocus=true;
							rotateTable.editCellAt(0, 0);
						}	
					});
					
				}
			}
			dlg.setLocation(PopupBasics.location);
		}
		dlg.setVisible(true);
		//supressUpdateUI = false;
		return status.size() > 0;
	}

	private void initFindFilterButtons() {
		if (creatingFilter) {
			SwingBasics.setEnabled(popupTableDoneButton, false);
			SwingBasics.setEnabled(seeOnly, hasSeeOnlySettings());
			SwingBasics.setEnabled(seeAll, hasSeeOnlySettings());
		}
	}

	public final int getFocusDataColumnIndex() {
		int[] selectedModelIndexes = table.cellHighlighter.getSelectedColumns();
		final int mc;
		if (selectedModelIndexes.length == 1) {
			mc = selectedModelIndexes[0];
		} else if (focusModelColumnIndex >= 0) {
			mc = focusModelColumnIndex;
		} else {
			mc = clickedModelColumnIndex;
		}
		return getDataColumnIndex(mc);

	}

	private List<String>limitEditAllColumns;
	public void addLimitEditAllTo(final int dataColumnIndex){ 
		if (limitEditAllColumns==null){
			limitEditAllColumns=new ArrayList<String>();
		}
		limitEditAllColumns.add(getDataColumnIdentifier(dataColumnIndex));
	}
	
	public void limitEditAll(final Collection<String>columns){
		if (!Basics.isEmpty(columns)){
			if (limitEditAllColumns==null){
				limitEditAllColumns=new ArrayList<String>();
			}
			limitEditAllColumns.addAll(columns);
		}
	}

	String getEditAllAnomaly() {

		String problem = null;
		if (readOnly) {
			return entireTableIsReadOnly;
		} else if (editInPlace) {
			int dc = getFocusDataColumnIndex();
			if (dc < 0) {
				problem="You must click on a particular table cell first";
			} else if ((!allowEditAllInClickedColumn || !getUngroupedModel().canEditCellForMultipleRows) && 
					(limitEditAllColumns==null || !limitEditAllColumns.contains(getDataColumnIdentifier(dc)))) {
				problem=Basics.isEmpty(limitEditAllColumns)?
						"This paticular table does not support changing multpile row values at once."
						:
							"This paticular table only supports changing multiple rows at once for some columns.";
			} else if (dataSource.getMaximumCardinality() < 2){
				problem="This table does not support more than one row.";
			} else {
			final String editableColumnName = getNameOfEditAllColumn();
			if (editableColumnName == null) {
				problem = entireTableIsReadOnly;
				if (editableColumnName == null) {
					problem = Basics
							.concat("The column/cell <b>",
									getColumnAbbreviation(dc),
									"</b> can not be edited");
				}
			}
			}
		} else {
			if (allowEditInPlaceControl){
				problem = "You must first turn 'Enter/Edit directly in table' on (shortcut=F3).";
			} else {
				return "This table does not allow editing in place.";
			}
		}
		return problem;
	}

	private boolean hasAnyEditAllColumn(){
		if (editInPlace
				&& allowEditAllInClickedColumn
				&& !readOnly 
				&& dataSource.getMaximumCardinality() > 1
				&& getUngroupedModel().canEditCellForMultipleRows) {
			if (!Basics.isEmpty(limitEditAllColumns)){
				return true;
			}
			for (int dc=0;dc<metaRow.size();dc++){
				if (metaRow.isEditable(dc) && !dataSource.isPartOfPrimaryKey(dc)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean canEditAll(final int dataColumnIndex){
		if (	editInPlace
				&& allowEditAllInClickedColumn
				&& !readOnly 
				&& dataSource.getMaximumCardinality() > 1
				&& getUngroupedModel().canEditCellForMultipleRows) {
			if (limitEditAllColumns==null){
				if (metaRow.isEditable(dataColumnIndex) && !dataSource.isPartOfPrimaryKey(dataColumnIndex)) {
					if (!isForbidEditAllColumn(dataColumnIndex)){
						return true;
					}
				}
			}else if (limitEditAllColumns.contains(getDataColumnIdentifier(dataColumnIndex))){
				return true;
			} 
		}
		return false;
	}
	

	private String getNameOfEditAllColumn() {
		String value = null;

			final int dc = getFocusDataColumnIndex();

			if (dc >= 0 && metaRow.isEditable(dc) && !dataSource.isPartOfPrimaryKey(dc)) {
				value = getColumnAbbreviation(dc);
			}
		return value;
	}
	public ActionListener clearSeeOnlyExternalAction=null;

	void initActions(final GroupedDataSource gds) {
		
		final boolean worksWithMultipleRows = dataSource
				.getMaximumCardinality() > 1;
		final DisabledExplainer editInPlaceDisabled = new DisabledExplainer(
				editInPlaceItem);

		final PersonalizableTableModel.MenuItemListener editInPlaceItemMenuListener = new Operation(
				editInPlaceDisabled) {
			String computeNewAnomaly(final int[] selected,
					final int rowsWithData, final int selectedDataRowCount) {
				if (readOnly) {
					return entireTableIsReadOnly;
				} else if (!allowEditInPlaceControl) {
					return editInPlaceIsFixed;
				}
				return null;
			}

		};

		new MenuItemsHandler((JComponent) table, editInPlaceAction, KeyStroke
				.getKeyStroke(KeyEvent.VK_F3, 0), 'e', editInPlaceDisabled,
				!allowEditInPlaceControl ? editInPlaceIsFixed
						: entireTableIsReadOnly, editInPlaceItemMenuListener,
				EDIT_IN_PLACE_TEXT);
		SwingBasics.echoAction(table, saveAsHtmlItem, saveAsHtmlAction,
				showInBrowserKeyStroke, 's');
		
		if (worksWithMultipleRows) {
			table.echoAction(printItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					printJob();
				}
			}, KeyEvent.VK_P, InputEvent.CTRL_MASK, 'p');

			if (isPickList) {
				table.echoAction(hideItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						hideJob();
					}
				}, KeyEvent.VK_H, InputEvent.CTRL_MASK, 'h');
				
				table.echoAction(unhideItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						unhideJob();
					}
				}, KeyEvent.VK_U, InputEvent.CTRL_MASK, 'u');
			}
			if (dataSource.isFilterable()) {
				final ActionListener findAction = new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						find();
					}
				};
				table.echoAction(findItem, findAction,
						KeyEvent.VK_F, InputEvent.CTRL_MASK, 'f');
				queryAllColumnsItem.setMnemonic('q');
				queryAllColumnsItem.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						query(false, QUERY_TYPE.ALL);
					}
				});
				querySelectedColumnsItem.setMnemonic('q');
				querySelectedColumnsItem.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						query(false, QUERY_TYPE.HIGHLIGHTED);
					}
				});

				table.echoAction(queryFavoriteColumnsItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						query(false, QUERY_TYPE.FAVORITE);
					}
				}, KeyEvent.VK_Q, InputEvent.CTRL_MASK, 'q');

				table.registerKeyboardAction(table.urlAction, KeyStroke
						.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						JComponent.WHEN_FOCUSED);
				final ActionListener seeAllAction = new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						if (dataSource.isFiltering()) {
							final Collection<Row> c = getSelectedRowsInAscendingOrder();
							if (clearSeeOnlySettings()){
								selectAndFocus(c);
								notifyViewChanged();
							}
							if (getUngroupedModel().clearSeeOnlyExternalAction != null) {
								getUngroupedModel().clearSeeOnlyExternalAction.actionPerformed(null);
							}
						}
					}
				};
				
				
				final Operation seeAllItemMenuListener = new
			      Operation(new DisabledExplainer(seeAllItem)) {
			        
					String computeNewAnomaly(final int[] selected, final int rowsWithData,
							final int selectedDataRowCount) {
						if(dataSource.isFilterable() && !dataSource.isFiltering()) {
							return "This operation is ONLY enabled if you have already filtered the table";
						}
						return null;
					}
			    };

			    
				new MenuItemsHandler((JComponent)table,seeAllAction,
						SwingBasics.getKeyStrokeWithMetaIfMac(KeyEvent.VK_F, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK),'r',
						seeAllItemMenuListener.da,
						"This operation is ONLY enabled if you have already filtered the table",
						seeAllItemMenuListener,"Show all");
							
				table.echoAction(showFilterItem, new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						setFilterRows(!showFilterUI);
						notifyViewChanged();
					}
				}

				, KeyEvent.VK_F3, InputEvent.CTRL_MASK, 's');
			}
			if (!readOnly && getUngroupedModel().canEditCellForMultipleRows) {
				changeEntireColumn.setIcon(MmsIcons.getEditIcon());
				changeEntireColumn.setText("Change all values...");
				ActionListener changeEntireColumnAct = new ActionListener() {
					public void actionPerformed(final ActionEvent event) {
						editAllValues(getFocusDataColumnIndex(), -1);
					}
				};
				
				final Operation changeEntireColumnItemMenuListener = new
			      Operation(new DisabledExplainer(changeEntireColumn)) {
			        
					String computeNewAnomaly(final int[] selected, final int rowsWithData,
							final int selectedDataRowCount) {
						final int dc = getFocusDataColumnIndex();
						if (dc >= 0) {
							final String name= getColumnAbbreviation(dc);
							final String s=getChangeColumnCount(dc);
							if (s!=null){
								changeEntireColumn.setText(Basics.concat("<html>Change column value for <b>", s, "</b> rows</html>"));
							} else {
								return "No editable rows";
							}
			    		} else {
			    			changeEntireColumn.setText("Change all values...");
			    		}
						return getEditAllAnomaly();
					}
			    };
				
				
				new MenuItemsHandler((JComponent)table,changeEntireColumnAct,
						hasAnyEditAllColumn()?KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK):null,'c',
				changeEntireColumnItemMenuListener.da," This operation is ONLY enabled if you select editable item(s)",
				changeEntireColumnItemMenuListener,"Change all values...");
			}
			final PersonalizableTableModel.MenuItemListener sortMenuListener = new MenuItemListener() {
		        public void keyboardActionStarted(final JMenuItem menuItem) {
		        	qualifySorting();
		        }
				
		        public boolean useOriginalDisabledText(){
		        	return false;
		        }

				public String computeNewAnomaly() {
					return null;
				}

		        
		    };

			if (canSort()){
			ActionListener sortAscendingAct = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (dataSource.getMaximumCardinality() > 1) {
						final int[] sortable = getComparableSelectedModelColumns(true);
						if (sortable.length > 1) {
							unsort();
						}
						int dc = 0;
						for (int i = 0; i < sortable.length; i++) {
							dc = getDataColumnIndex(sortable[i]);
							sort(dc, true);
						}
						sortAndRepaint();
						notifyViewChanged();
						showCurrentSort(dc);
					}
				}
			};
			
			
			
			new MenuItemsHandler((JComponent)table,sortAscendingAct,KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),'a',
			sortAscendingDisabled,"This column(s) is currently sorted in Ascending order",
			sortMenuListener,"Sort Ascending");
			
			table.echoAction(sortSequenceItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final int[] sortable = getComparableSelectedModelColumns(true);
					alterSortSequence(getDataColumnIndex(sortable[0]), table);
				}
			}

			, KeyEvent.VK_C, InputEvent.CTRL_MASK, 'c');
			
			
			ActionListener refreshSortAct = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (sortNeeded) {
						sortAndRepaint();
					}
				}
			};
			
						
			new MenuItemsHandler((JComponent)table,refreshSortAct,KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK),'r',
					refreshDisabled,"This operation is ONLY enabled when all values of a column which is currently sorted, are changed",
					sortMenuListener,"Refresh sort");
			
						
			ActionListener sortDescendingAct = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (dataSource.getMaximumCardinality() > 1) {
						final int[] sortable = getComparableSelectedModelColumns(true);

						int dc = 0;
						if (sortable.length > 1) {
							unsort();
						}
						for (int i = 0; i < sortable.length; i++) {
							dc = getDataColumnIndex(sortable[i]);
							sort(dc, false);
						}
						notifyViewChanged();
						sortAndRepaint();
						showCurrentSort(dc);
					}
				}
			};
		
			
			new MenuItemsHandler((JComponent)table,sortDescendingAct
					,KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.SHIFT_MASK),'d',
			sortDescendingDisabled,"This column(s) is currently sorted in Descending order"
			,sortMenuListener,"Sort Descending");
			}
			if(isCreateTabsOn) {
				table.echoAction(tabItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						qualifyActions();
						final PersonalizableTableModel utm=getUngroupedModel();
						if (tabItem.isEnabled()) {
							if (utm.tabs == null) {
								if (utm.isGrouped()){
									utm.ungroupIfNecessary();
								}
								int dataColumnIndex=-1;
								final int[] _selectedModelIndexes = getSelectedModelColumns(true);
								if(_selectedModelIndexes.length > 0 ){	
									dataColumnIndex=getDataColumnIndex(_selectedModelIndexes[0]);
								}
								if(dataColumnIndex>=0){
									utm.activateTabs(dataColumnIndex, true);
								}
							} else {
								utm.tabs.dispose();
							}
						} else {
							disabledTab.showDisabledTextOnTheComponent(table);
						}
					}
				}
	
				, KeyEvent.VK_B, InputEvent.CTRL_MASK, 'c'); 
			}
			if (canSort()){

		    
		    ActionListener unsortAct = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (dataSource.getMaximumCardinality() > 1) {

						final int[] sortable = getComparableSelectedModelColumns(true);
						int dc = 0;
						for (int i = 0; i < sortable.length; i++) {
							dc = getDataColumnIndex(sortable[i]);
							unsort(dc);
						}
						notifyViewChanged();
						sortAndRepaint();
						showCurrentSort(dc);
					}
				}
			};
			
			new MenuItemsHandler((JComponent)table,unsortAct,
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),'u',
					unsortDisabled,
					"This operation is ONLY available if the list is already sorted",
					sortMenuListener,"Unsort");
			
			
		    
		    ActionListener unsortAllAct = new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (unsortAllItem.isEnabled()) {
						unsortAndRepaint();
					}
				}
			};
			
			new MenuItemsHandler((JComponent)table,unsortAllAct,
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_MASK),'u',
					unsortAllDisabled,
					"This operation is ONLY available if the list is already sorted",
					sortMenuListener,"Unsort all");
			
		}
		}
		final PersonalizableTableModel utm=gds==null?getUngroupedModel():gds.ungroupedModel;
		if (utm != this && utm.editMenuItem!=null){
			editMenuItem = new PopupMenuItem(utm.editMenuItem);
			editMenuItem.registerKeyboardAction(table);
			
		}
		
		ActionListener hideAction = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				qualifyActions();
				if (hideColumnItem.isEnabled()) {
					final int[]a=getSelectedModelColumns(true);
					if (!Basics.isEmpty(a)) {
						final ArrayList<String> names = new ArrayList();
						for (int i = 0; i < a.length; i++) {
							names.add(modelColumnIdentifiers
									.get(a[i]));
						}
						for (int i = 0; i < names.size(); i++) {
							final int idx = table.tcm.getColumnIndex(names
									.get(i));
							final TableColumn tc = table.tcm.getColumn(idx);
							hideColumn(tc);
						}
					}
					table.cellHighlighter.reset();
					notifyViewChanged();
				} else {
					hideDisabler.showDisabledTextOnTheComponent(table);
				}
			}};
	
			if (allowColumnHidingByKeyboardShortcut) {
				table.echoAction(hideColumnItem, hideAction, KeyEvent.VK_F6, 0, 'h');
			}
			else {
				hideColumnItem.addActionListener(hideAction);
				hideColumnItem.setMnemonic('h');
			}
		

		table.echoAction(fitWindowItem, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final int newMode = table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF ? JTable.AUTO_RESIZE_ALL_COLUMNS
						: JTable.AUTO_RESIZE_OFF;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (allowAutoResizeControl) {
							table.setAutoResizeMode(newMode);
							table.updateUI();
						}
					}
				});
				notifyViewChanged();
			}
		}

		, KeyEvent.VK_F7, 0, 'f');

		if (isTreeOn) {
			table.echoAction(newTreeItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (dataSource.getMaximumCardinality() > 1) {

						qualifyActions();   

						if (newTreeItem.isEnabled() && isTreeOn) {
							final PersonalizableTableModel um = getUngroupedModel();
							final int[] sortable = getComparableSelectedModelColumns(true);
							
							for(int di: sortable) {
								if (columnsNotGroupable.contains(di)) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											showTextInPopupWindow("Can't group as one or more of the sorted columns are not groupable", false, false, false, null,
													null, true);
										} 
									});
									return;
								}
							}
							
							ungroupIfNecessary();
							if (um.dataSource.getDataRows().size() > um.treeOneMomentThreshold) {
								um.showOneMomentDisplay();
							}
							if (sortable.length > 0) {
								um.unsort();
								for (int i = 0; i < sortable.length; i++) {
									um.sort(getDataColumnIndex(sortable[i]), true);
								}
								um.sort();
							} else if (um != PersonalizableTableModel.this
									&& columnsToSort.size() > 0) {
								um.unsort();
								for (final SortInfo si : columnsToSort) {
									um.sort(si.dataColumnIndex, si.ascending);
								}
								um.sort();
							} else {
								um.sort();
							}
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									final FocusFreeze ff = new FocusFreeze();
									final Collection<Row> c = um
											.getSelectedRowsInTable();
									um.finishTreeBuilding();
									um.group();
									um.notifyViewChanged();
									if (um.dataSource.getDataRows().size() > um.treeOneMomentThreshold) {
										um.hideOneMomentDisplay();
									}
									reselect(c, -1);
									ff.thawLater(um.groupedDataSource.tree);
									// No check needed of FocusFreeze.isFrozen
								}
							});
						}
					}
				}
			}

			, KeyEvent.VK_F8, 0, 'u');
			table.echoAction(sortBasedTreeItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (dataSource.getMaximumCardinality() > 1) {
						final SortInfo[]si=getModelType()== TYPE_GROUP_SEIVED?getAllSortInfo():null;
						qualifyActions();   
						
						for(SortInfo csi: columnsToSort) {
							if (columnsNotGroupable.contains(csi.dataColumnIndex)) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										showTextInPopupWindow("Can't group as one or more of the sorted columns are not groupable", false, false, false, null,
												null, true);
									} 
								});
								return;
							}
						}
						if (newTreeItem.isEnabled() && isTreeOn && columnsToSort.size()>0) {
							final PersonalizableTableModel um = getUngroupedModel();
							ungroupIfNecessary();							
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									final FocusFreeze ff = new FocusFreeze();
									final Collection<Row> c = um
											.getSelectedRowsInTable();
									um.finishTreeBuilding();
									if (si != null) {
										um.sort(si);
										um.sort();
									}
									um.group();
									um.notifyViewChanged();
									if (um.dataSource.getDataRows().size() > um.treeOneMomentThreshold) {
										um.hideOneMomentDisplay();
									}
									reselect(c, -1);
									ff.thawLater(um.groupedDataSource.tree);
									// No check needed of FocusFreeze.isFrozen
								}
							});
						}
					}
				}
			}

			, KeyEvent.VK_F8, InputEvent.ALT_MASK, 'c');

		}
	
		if (isTreeOn) {
			table.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (isTreeOn) {
						getUngroupedModel().buildTreeByDraggingAndDroppingColumns();
					}
				}
			}, SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_T, false),
					JComponent.WHEN_FOCUSED);		
			table.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if(isTreeOn) {
						getUngroupedModel().buildTreeFromDropDownListOfColumns=true;
						getUngroupedModel().newTree();
					}
				}
			}, SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_G, false),
			JComponent.WHEN_FOCUSED);

		}
		
		if (gds != null && isTreeOn) {
			table.echoAction(removeTreeItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (removeTreeItem.isEnabled() && isTreeOn) {
						final Collection<Row> reselect = getSelectedRowsInTable();
						ungroupIfNecessary();
						finishTreeBuilding();
						if (reselect.size() > 0) {
							getUngroupedModel().reselect(reselect, -1);
						} else {
							fireListSelection();
						}

						notifyViewChanged();
						// no FocusFreeze.isFrozen check necessary
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								table.requestFocus();
							}

						});

					}
				}
			}

			, KeyEvent.VK_T, InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK, 't');
		}
		table.echoAction(copyViewItem, copyViewAction, copyViewKeyStroke, 'c');
		table.echoAction(pasteViewItem, pasteViewAction, pasteViewKeyStroke,
				'p');
		table.echoAction(openViewItem, openViewAction, openViewKeyStroke, 'o');
		table.echoAction(saveViewItem, saveViewAction, saveViewKeyStroke, 's');

		if (canSortArrangeColumns ) {
			table.echoAction(sortArrangeColumnsItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					showColumnConfig();
				}
			}

			, KeyEvent.VK_F10, 0, 's');
			
		}
		
		showAllColumnsItem.setMnemonic('s');
		showAllColumnsItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				showAllColumns();
			}
		});
		
		final Operation exploreItemMenuListener = new
	      Operation(new DisabledExplainer(exploreItem)) {
			String computeNewAnomaly(final int[] selected, final int rowsWithData,
					final int selectedDataRowCount) {
	        	exploreItem.setText(VERB_EDIT_CELL);
	        	if (canExplore() && getRowCount() > 0) {
	    			final StringConverter sc = metaRow
	    					.getStringConverter(clickedDataColumnIndex);
	    			String s = sc
	    					.toString(dataRowToExplore.get(clickedDataColumnIndex));
	    			if (!Basics.isEmpty(s)) {
	    				if (table.anticipateHtml) {
	    					s = Basics.stripSimpleHtml(s);
	    				}
	    				if (s.length() > 12) {
	    					s = s.substring(0, 11) + "...";
	    				}
	    				exploreItem.setText(Basics.concat(VERB_EDIT_CELL, " \"", s, "\""));
	    			} else {
	    				return selectRowsAnomaly("explorable cell");
	    			}
	    		} else {
	    			return selectRowsAnomaly("explorable cell");
	    		}
	        	return null;
	        }
	    };

	    
	    ActionListener exploreCellAct = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				explore();
			}
		};
		
		if(!(showLimitedMenu)){		
			new MenuItemsHandler((JComponent)table,exploreCellAct,
		
					SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_X, false),'x',
				exploreItemMenuListener.da,
				"This menu item is currently not allowed",
				exploreItemMenuListener,"Explore Cell");
		}
		table.echoAction(
		freezeColumnsItem, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				qualifyActions();
				if (freezeColumnsItem.isEnabled()) {
					columnFreezer.setView();
				} else {
					freezeDisabler.showDisabledText();
				}
			}
		}, KeyEvent.VK_Z, InputEvent.ALT_MASK, 'f' );
		unfreezeColumnsItem.setMnemonic('u');
		
		table.echoAction(unfreezeColumnsItem, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				qualifyActions();
				if (unfreezeColumnsItem.isEnabled()) {
					columnFreezer.setView(true);
					setRowHeaderProperties();
					notifyViewChanged();
				} else {
					unfreezeDisabler.showDisabledText();
				}
			}
		}, KeyEvent.VK_Z, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, 'u');

		if(table.getColumnCount() > 2) {
			table.echoAction(shiftLeftItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (shiftLeftItem.isEnabled()) {
						table.cellHighlighter.shiftSelectedLeft();
						notifyViewChanged();
					}
				}
			}
	
			, KeyEvent.VK_LEFT, InputEvent.CTRL_MASK, 'l');
			
			table.echoAction(shiftRightItem, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					qualifyActions();
					if (shiftRightItem.isEnabled()) {
						table.cellHighlighter.shiftSelectedRight();
						notifyViewChanged();
					}
				}
			}
	
			, KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK, 'r');
		}
		if (!readOnly) {
			if ((dataSource.isCreatable() || (gds != null && gds.canCreate)) && !utm.useCustomNewItemOnly) {
				table.echoAction(newItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						newItem();
						requestFocusLater();
					}
				}

				, KeyEvent.VK_N, InputEvent.CTRL_MASK, 'n');
			}

			if ((gds == null && dataSource.isAddable())
					|| (gds != null && gds.ungroupedModel.dataSource
							.isAddable())) {
				table.echoAction(chooseItemFromList, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						getUngroupedModel().add();
					}
				}

				, KeyEvent.VK_L, InputEvent.CTRL_MASK, 'c');
			}
			
			
			if (dataSource.isSaveable()) {
				saveItem = new JMenuItem();
				table.echoAction(saveItem, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						dataSource.save();
					}
				}

				, KeyEvent.VK_S, InputEvent.CTRL_MASK, 's');
			}

				

		}
		
		moveUpItem = new JMenuItem("Up", MmsIcons.getUpIcon());
		moveUpDisabled = new DisabledExplainer(moveUpItem);
		
		final PersonalizableTableModel.MenuItemListener moveItemMenuListener = new MenuItemListener() {
	        public void keyboardActionStarted(final JMenuItem menuItem) {
	        	qualifyMoveAction.actionPerformed(new ActionEvent(menuItem, -1, "keyboard"));
	        }	
	        public boolean useOriginalDisabledText(){
	        	return false;
	        }

			public String computeNewAnomaly() {
				return null;
			}

	    };

		ActionListener moveUpAct = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				moveUpAction.actionPerformed(e);
			}
		};
		new MenuItemsHandler((JComponent)table,moveUpAct,
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_UP, InputEvent.CTRL_MASK, 
						KeyEvent.VK_K, InputEvent.ALT_MASK),
						'u',
				moveUpDisabled,
				selectRowsAnomaly(),
				moveItemMenuListener,"Move Up");
		


		moveDownItem = new JMenuItem("Down", MmsIcons.getDownIcon());
		moveDownDisabled = new DisabledExplainer(moveDownItem);
	    
		ActionListener moveDownAct = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				moveDownAction.actionPerformed(e);
			}
		};
		new MenuItemsHandler((JComponent)table,moveDownAct,
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_DOWN, InputEvent.CTRL_MASK, 
						KeyEvent.VK_J, InputEvent.ALT_MASK),
						'd',
				moveDownDisabled,
				selectRowsAnomaly(),
				moveItemMenuListener,"Move Down");
		

		moveTopItem = new JMenuItem("Top");
		moveTopItem.setIcon(MmsIcons.getHomeIcon());				
		moveTopDisabled = new DisabledExplainer(moveTopItem);
		
		ActionListener moveTopAct = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				moveTopAction.actionPerformed(e);
			}
		};
		new MenuItemsHandler((JComponent)table,moveTopAct,
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_HOME, InputEvent.CTRL_MASK, 
						KeyEvent.VK_I, InputEvent.ALT_MASK),
						't',
				moveTopDisabled,
				selectRowsAnomaly(),
				moveItemMenuListener,"Move to Top");
		
		moveBottomItem = new JMenuItem("Bottom", MmsIcons.getBottomIcon());
		moveBottomDisabled = new DisabledExplainer(moveBottomItem);

		ActionListener moveBottomAct = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				moveBottomAction.actionPerformed(e);
			}
		};
		new MenuItemsHandler((JComponent)table,moveBottomAct,
				SwingBasics.getKeyStrokeWithMetaIfMac(
						KeyEvent.VK_END, InputEvent.CTRL_MASK, 
						KeyEvent.VK_M, InputEvent.ALT_MASK),
						'b',
				moveBottomDisabled,
				selectRowsAnomaly(),
				moveItemMenuListener,"Move to Bottom");

		remove=createRemoveOperation(table, true);		
		delete=createDeleteOperation(table, true);
		table.echoAction(selectAllItem, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final int n1 = showFilterUI ? 2 : 0, n2 = getRowCount() - 1;
				if (n2 > n1) {
					table.setRowSelectionInterval(n1, n2);
				}
			}
		}, KeyEvent.VK_A, InputEvent.CTRL_MASK, 'a');
		if (!isCompanionTable && isTreeOn) {
			recentTrees.registerActions(table, recentTreeMenu);
			favoriteTrees.registerActions(table, favoriteTreeMenu);
		}
		if (utm.supportsTearAway()) {
			utm.tearAwayHandler.setTableModel(utm);
			utm.tearAwayHandler.echoAction(table, tearAwayItem);
		}
		if (utm.defaultViewRestorer != null) {
			SwingBasics.echoAction(table, restoreDefaultViewItem,
					restoreDefaultViewAction, restoreDefaultViewKeyStroke, 'r');
		}		
	}
	
	final JMenuItem restoreDefaultViewItem = new JMenuItem(
			"Restore default view", MmsIcons.getRefreshIcon());

	public final ActionListener restoreDefaultViewAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			Collection<Row>reselect=getUngroupedModel().getSelectionsInDescendingOrder(true);
			resetCurrentView(getUngroupedModel().defaultViewRestorer.getDefaultView());
			favoriteTrees = new TreeShapes(true, InputEvent.CTRL_MASK,
					PROPERTY_FAVORITE_TREE_IDX,
					PROPERTY_FAVORITE_TREE_IDS,
					PROPERTY_FAVORITE_TREE_ASCENDING);
			if (isTreeOn) {
				getUngroupedModel().favoriteTrees.registerActions(table,
						favoriteTreeMenu);						
				notifyViewChanged();
                if (Basics.isEmpty(reselect)){
                	final GroupedDataSource gds=getUngroupedModel().getTreeDataSource();
        			if (gds != null){
                		gds.pickRootLater();
                	}
                }else{
                	reselect(reselect);
                }

			}

		}
	};

	final KeyStroke	
			restoreDefaultViewKeyStroke = SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_D, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK), 
			saveViewKeyStroke=SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_S, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK),
			openViewKeyStroke = SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_O, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK), 
			copyViewKeyStroke = SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_C, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK),
			pasteViewKeyStroke = SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_V, InputEvent.SHIFT_MASK|InputEvent.CTRL_MASK, InputEvent.SHIFT_MASK),
			showInBrowserKeyStroke = SwingBasics.getKeyStrokeWithMetaIfMac(
					KeyEvent.VK_B, InputEvent.ALT_MASK, InputEvent.ALT_MASK);
	final ActionListener saveAsHtmlAction=new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			showAsHtml();
		}
	};
	
	final ActionListener 
	saveViewAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			saveView();

		}
	},
	openViewAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			openView();

		}
	}, copyViewAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			PersonalizableTableModel ptm = getUngroupedModel();
			int debug = getProperty(PROPERTY_GROUP, -1);
			final Properties p = ptm.updatePropertiesWithPersonalizations(true);
			debug = getProperty(PROPERTY_GROUP, -1);
			final String prfx = getUngroupedDataSourcePropertyPrefix();
			if (prfx == null) {
				p.remove(PROPERTY_PASTE_PREFIX);
			} else {
				p.setProperty(PROPERTY_PASTE_PREFIX, prfx);
			}
			clipBoardProperties = new Properties();
			final Enumeration<String> en = (Enumeration<String>) p
					.propertyNames();
			while (en.hasMoreElements()) {
				final String key = en.nextElement();
				final String value = p.getProperty(key);
				clipBoardProperties.setProperty(key, value);
			}
			String debug2 = clipBoardProperties.getProperty("iv.condensation");

			clipBoardKey = key;
		}
	}

	, pasteViewAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			if (clipBoardProperties != null
					&& (key.equals(clipBoardKey) || PopupBasics.ask(table,
							"Pasting view copied for table of " + clipBoardKey
									+ "?"))) {
				final String prfx = clipBoardProperties
						.getProperty(PROPERTY_PASTE_PREFIX);
				final String cur = getUngroupedDataSourcePropertyPrefix();
				if (!Basics.equals(prfx, cur)) {
					String debug = clipBoardProperties
							.getProperty("iv.condensation");
					final String prefixP = prfx == null ? "" : prfx + ".";
					final int n = prefixP.length();
					final String curP = cur == null ? "" : cur + ".";
					final Enumeration<String> en = (Enumeration<String>) clipBoardProperties
							.propertyNames();
					while (en.hasMoreElements()) {
						final String k = en.nextElement();
						if (k.endsWith("condensation")) {
							int debug2 = 3;
						}
						if (k != null && k.startsWith(prefixP)) {
							final String value = clipBoardProperties
									.getProperty(k);
							final String name = curP + k.substring(n);
							clipBoardProperties.setProperty(name, value);
						}
						debug = clipBoardProperties
								.getProperty("iv.condensation");
					}
				}
				resetCurrentView(clipBoardProperties);
				notifyViewChanged();
			}
		}
	};

	final JMenu recentTreeMenu = new JMenu();
	private final JMenu favoriteTreeMenu = new JMenu();
	private JComboBox columnFinder;
	private AutoComplete.FoundListener columnFinderFoundListener;
	private JMenu treeMenu;
	private JPopupMenu treePopup;
	boolean isInTreeBuildingMode = false;

	private Window buildingTreeWindow;

	public boolean isInTreeBuildingMode() {
		return getUngroupedModel().isInTreeBuildingMode;
	}

	public void closeBuildingTreeWindow() {
		closeFindWindow();
		if (buildingTreeWindow != null) {
			final Window w = buildingTreeWindow;
			buildingTreeWindow = null;
			columnFinder=null;
			SwingBasics.closeWindow(w);
			toFront();
		}
	}

	private void toFront() {
		if (table != null && SwingUtilities.getWindowAncestor(table) != null) {
			SwingUtilities.getWindowAncestor(table).toFront();
		}

	}

	public void finishTreeBuilding() {
		getUngroupedModel().isInTreeBuildingMode = false;
		getUngroupedModel().closeBuildingTreeWindow();
	}

	public void showBuildingTreeWindow() {
		getUngroupedModel().isInTreeBuildingMode = true;
		getUngroupedModel().showBuildingTreeWindowIfNecessary();
	}

	public void showBuildingTreeWindowIfNecessary() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showBuildingTreeWindow(getModelShowing(), getUngroupedModel());
			}
		});
	}

	private void initColumnFinder(
			final PersonalizableTableModel rootTableModel, final JButton select) {
		final String[] labels = new String[rootTableModel.metaRow.size()];
		final Map<String, Integer> dataColumnIndexes = new HashMap<String, Integer>();
		for (int i = 0; i < labels.length; i++) {
			String s = rootTableModel.getColumnLabel(i);
			labels[i] = Basics.stripSimpleHtml(s);
			dataColumnIndexes.put(labels[i], i);
		}
		Arrays.sort(labels);
		columnFinderFoundListener = new AutoComplete.FoundListener() {
			public void completionFound(final Object nextSelection) {
				final PersonalizableTableModel companionTableModel = rootTableModel.groupSeivedModel == null ? rootTableModel
						: rootTableModel.groupSeivedModel;
				if (nextSelection == null) {
					if (select != null) {
						select.setEnabled(false);
					}

				} else {
					final String label = nextSelection.toString();
					final int i = Arrays.binarySearch(labels, label);
					if (i >= 0) {
						final int dataColumnIndex = dataColumnIndexes
								.get(label);
						final int v = companionTableModel
								.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
						if (v >= 0) {
							companionTableModel.table.scrollToVisible(0, v);
							final int m = companionTableModel
									.getModelColumnIndexFromDataColumnIndex(dataColumnIndex);
							companionTableModel.table.cellHighlighter
									.selectColumn(m, v, null);
						}
						if (select != null) {
							select.setEnabled(true);
							final SortInfo si = rootTableModel
									.findSortInfo(dataColumnIndex);
							if (si != null) {
								select.setText("Remove");
								select.setToolTipText(Basics.toHtmlUncentered(
										select.getText(),
										"Click here to remove <b>" + label
												+ "</b><br>from the tree"));
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										rootTableModel.buildingTreeWindow.pack();
									}
								});
							} else {
								select.setText("Add");
								select.setToolTipText(Basics.toHtmlUncentered(
										select.getText(),
										"Click here to add <b>" + label
												+ "</b><br>to the tree"));
							}
							ToolTipOnDemand.getSingleton().showLater(select);
						}
					}
				}
			}
		};

		columnFinder = getReadOnlyComboBox(Basics.toList(labels), null,
				columnFinderFoundListener, -1);

		select.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (select != null) {
					final String label = (String) columnFinder
							.getSelectedItem();
					if (label != null) {
						final String[] debug = labels;
						final int i = Arrays.binarySearch(labels, label);
						if (i >= 0) {
							final int dataColumnIndex = dataColumnIndexes
									.get(label);
							rootTableModel.adjustTree(dataColumnIndex, false);
						}
					}
				}
			}
		});
	}

	public static Icon treeBuildIcon = null;

	private static void showBuildingTreeWindow(
			final PersonalizableTableModel companionTableModel,
			final PersonalizableTableModel rootTableModel) {
		if (rootTableModel.isInTreeBuildingMode) {
			if (rootTableModel.buildingTreeWindow == null
					&& companionTableModel.table.isShowing()) {
				final String txt;
				final Container cp;
				final String title = "View organizer for \""
						+ rootTableModel.singularKey + "\" data";
				final RootPaneContainer root;
				final JComponent c=rootTableModel.getTearAwayComponent();
				final Window wa = SwingUtilities
						.getWindowAncestor(c);
				if (!(wa instanceof Dialog)) {
					final JFrame fr = SwingBasics.getFrame();
					SwingBasics.setTitle(fr, title);
					cp = fr.getContentPane();
					fr.setAlwaysOnTop(true);
					rootTableModel.buildingTreeWindow = fr;
					txt = "<html><b>Build from drop down list of columns</b><br>Select column and click to add a level to the tree.&nbsp;&nbsp;<br>"
							+ "Re-select to remove from the tree.</html>";
					fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					root = fr;
				} else {
					final JDialog dlg = SwingBasics.getDialog(c);
					dlg.setTitle(title);
					dlg.setModal(true);
					cp = dlg.getContentPane();
					rootTableModel.buildingTreeWindow = dlg;
					txt = "<html>To add/remove columns to the tree select from the <b>\"Column list\"</b><br>and then click Add/Remove<hr></html>";
					dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					root = dlg;
				}
				final JLabel jl = new JLabel(txt);
				jl.setIcon(rootTableModel.treeBuildIcon);
				jl.setHorizontalTextPosition(JLabel.LEFT);
				final JPanel center = new JPanel();
				center.add(jl);
				center.setBorder(BorderFactory.createEmptyBorder(5, 10, 2, 10));
				cp.setLayout(new BorderLayout());
				cp.add(center, BorderLayout.CENTER);

				final JButton finished = SwingBasics
						.getButton(
								"Done",
								null,
								'd',
								null,
								Basics
										.toHtmlUncentered("Stop tree building",
												"Click this button to turn<br>tree-building mode <i>OFF</i>"));
				SwingBasics.registerEscape(root, finished);
				final JPanel south = new JPanel();
				south.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
				final JButton select = SwingBasics.getButton("Add", 's', null,
						null);
				root.getRootPane().setDefaultButton(select);
				rootTableModel.initColumnFinder(rootTableModel, select);
				south.add(new JLabel("<html><b>Column list:  </b></html>"));
				south.add(rootTableModel.columnFinder);
				final JPanel buttons = SwingBasics.getButtonPanel(2);
				buttons.add(select);
				buttons.add(finished);
				buttons.add(HelpBasics.getHelpButton(root, "buildTree.htm"));
				south.add(buttons);
				cp.add(south, BorderLayout.SOUTH);
				final Window w = rootTableModel.buildingTreeWindow;
				rootTableModel.buildingTreeWindow
						.addWindowListener(new WindowAdapter() {
							public void windowClosing(final WindowEvent e) {
								if (rootTableModel.buildingTreeWindow != null) { // user
									// clicked
									// x at
									// top
									// right
									// of
									// window
									if (root instanceof JFrame) {
										((JFrame) root).setAlwaysOnTop(false);
									}
									finished.doClick(150);
								} else {
									w.dispose();
								}
							}
						});
				rootTableModel.buildingTreeWindow.pack();
				final Point screenLocation = companionTableModel.table
						.getLocationOnScreen();
				final int x = screenLocation.x + 15, y = screenLocation.y + 15;
				rootTableModel.buildingTreeWindow.setLocation(x, y);
				SwingBasics.packAndPersonalize(
						rootTableModel.buildingTreeWindow,
						rootTableModel.properties, rootTableModel
								.getPropertiesSaviorForTearAway(), "treeBuild",
						false, false, true, false, null);

				finished.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final SortInfo[] sis = rootTableModel.getAllSortInfo();
						if (sis.length == 0) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (PopupBasics
											.ask(rootTableModel
													.getTearAwayComponent(),
													"The tree has no levels/columns, restore full table view?")) {
										rootTableModel.ungroupIfNecessary();
										rootTableModel.finishTreeBuilding();
										rootTableModel.notifyViewChanged();
										// NO check of FocusFreeze is necessary
										SwingUtilities
												.invokeLater(new Runnable() {
													public void run() {
														rootTableModel.table
																.requestFocus();
													}
												});
									} else {
										if (root instanceof JFrame) {
											((JFrame) root)
													.setAlwaysOnTop(true);
										}
									}

								}
							});
						} else {
							rootTableModel.finishTreeBuilding();
							rootTableModel.recentTrees.addCurrentTree(true);
							// NO check of FocusFreeze is necessary
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									rootTableModel.groupedDataSource.tree
											.requestFocus();
								}
							});
						}
					};
				});
				// rootTableModel.buildingTreeWindow.show(); - deprecated, use
				// instead
				rootTableModel.buildingTreeWindow.setVisible(true);
			}
		}
	}

	static void setTreeBuildingMode(
			final PersonalizableTableModel companionTableModel,
			final PersonalizableTableModel rootTableModel, final boolean ok) {
		// this assignment merely communicates that this is the
		// companionTableModel
		if (ok && !rootTableModel.isInTreeBuildingMode) {
			rootTableModel.isInTreeBuildingMode = ok;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (companionTableModel != null
									&& companionTableModel.table != null) {
								showBuildingTreeWindow(companionTableModel,
										rootTableModel);
							}
						}
					});
				}
			});
		} else {
			rootTableModel.isInTreeBuildingMode = ok;
		}
	}

	public void extractTreeMenuFromPopup(final JPopupMenu popup) {
		qualifyActions();
		treePopup = popup;
		if (treeMenu != null) {
			final Component[] a = treeMenu.getMenuComponents();
			for (int i = 0; i < a.length; i++) {
				treeMenu.remove(0);
				popup.add(a[i]);
			}
		}
	}

	private void restoreTreeMenu() {
		if (treePopup != null) {
			final int n = treePopup.getComponentCount();
			for (int i = 0; i < n; i++) {
				final Component c = treePopup.getComponent(0);
				treePopup.remove(0);
				treeMenu.add((JMenuItem) c);
			}
		}
	}

	public void resetPopupMenu() {
		popup = null;
	}


	public final static String VERB_ADD = "Choose", VERB_NEW = "Enter new",
			VERB_REMOVE = "Remove", VERB_DELETE = "Delete", VERB_EDIT = "Edit",
			VERB_VIEW = "View", EDIT_MENU_TEXT = VERB_EDIT + " row",
			VIEW_MENU_TEXT = VERB_VIEW + " row",
			VERB_EDIT_CELL = "Explore cell",EDIT_IN_PLACE_TEXT = "'Enter/Edit directly in table"; 

	public int select(final Collection c, final boolean deepEquals,
			final boolean additive) {
		int idx = focusVisualRowIndex;
		if (!Basics.isEmpty(c)) {
			if (!additive) {
				table.clearSelection();
			}
			scrollOnce = 0;
			for (final Iterator it = c.iterator(); it.hasNext();) {
				final Row row = (Row) it.next();
				idx = select(row, deepEquals, false, true);
			}
			scrollOnce = -1;
			if (isGrouped()) {
				final PersonalizableTableModel um = getUngroupedModel();
				boolean was = um.autoSelectTableFromTree;
				um.autoSelectTableFromTree = true;
				um.groupedDataSource.tree.clearSelection();
				for (final Object row : c) {
					um.selectInTree((Row)row, true);
				}
				um.autoSelectTableFromTree = was;
			}
		}
		return idx;
	}

	boolean clickedApplyFilter = false;
	boolean creatingFilter = false;
	private JButton seeOnly, seeAll;

	private static String SEE_SELECTED_TEXT="See selected";
	private String getFindMessage(){
		return "Click <b>"+SEE_SELECTED_TEXT+"</b> to:<ul><li>Close this window<li>Select the rows that match the filter setting<li>Show all other rows as unselected</ul>";
	}
	private String getFindToolTipText() {
		return Basics
				.toHtmlUncentered(
						SEE_SELECTED_TEXT,
						getFindMessage());
	}

	private int savedVisualColumnIndex, savedVisualRowIndex;

	public void saveFocus(final boolean useHighlightedColumnInsteadOfFocus) {
		int modelColumnIndex = -1;
		if (useHighlightedColumnInsteadOfFocus) {
			final int[] hc = table.cellHighlighter.getSelectedColumns();
			if (!Basics.isEmpty(hc) && hc.length == 1) {
				modelColumnIndex = hc[0];
			}
		}
		if (modelColumnIndex == -1) {
			if (focusModelColumnIndex >= 0) {
				modelColumnIndex = focusModelColumnIndex;
			} else {
				modelColumnIndex = 0;
			}
		}
		savedVisualColumnIndex = SwingBasics.getVisualIndexFromModelIndex(
				table, modelColumnIndex);
	}

	private int ensureRowIdx(final int idx) {
		if (idx < 0) {
			return 0;
		}
		if (idx >= getRowCount()) {
			return getRowCount() - 1;
		}
		return idx;
	}

	public void restoreColumnSelectionInterval() {
		if (savedVisualColumnIndex >= 0) {
			table.addColumnSelectionInterval(savedVisualColumnIndex,
					savedVisualColumnIndex);
		}
	}

	public void restoreFocus(final int rowIdx) {
		if (savedVisualColumnIndex >= 0) {
			table.addColumnSelectionInterval(savedVisualColumnIndex,
					savedVisualColumnIndex);
		}
		final int _r;
		if (rowIdx > -1) {
			_r = rowIdx;
		} else {
			_r = savedVisualRowIndex;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.scrollToVisible(ensureRowIdx(_r), savedVisualColumnIndex);
				final Window w = SwingUtilities.getWindowAncestor(table);
				if (w != null) {
					w.toFront();
				}
			}
		});
		if (!FocusFreeze.isFrozen()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					table.requestFocus();
				}
			});
		}

	}

	private int scrollOnce = -1;

	void releaseSeeOnlySettings(){
		clearSeeOnlySettings();
		if (clearSeeOnlyExternalAction == null) {
			showOneMomentDisplay();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (clearSeeOnlyExternalAction != null) {
					clearSeeOnlyExternalAction.actionPerformed(null);
				} else {
					refresh();
					hideOneMomentDisplay();
				}
			}
		});
	}
	
	public void limitTreeToContain(final QUERY_TYPE type) {
		if (query(true, type)) {
			showOneMomentDisplay();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					refresh();
					hideOneMomentDisplay();
				}
			});
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
	                groupedDataSource.tree.requestFocus();
					
				}});
		}
	}

	enum QUERY_TYPE {
		ALL, HIGHLIGHTED, FAVORITE
	};
	
	
	int removeActiveFiltered(final boolean alert){
		int hidden=0;
		final ArrayList<Row>c=new ArrayList<Row>();
    	for (final Row row:dataSource.getFilteredDataRows()){
        	if (dataSource.meetsCriteria(row)) {
        		c.add(row);
			} else {
				hidden++;
			}
        }
    	if (hidden>0 ){
    		if(alert){
    			PopupBasics.alert(getTearAwayComponent(), dataSource.describeHiddenQueryResults(), "Note...",false);
    		}
        	dataSource.setFilter(c);
    	}
    	return hidden;
	}
	
	private boolean query(final boolean limitTreeToContain, QUERY_TYPE queryType) {
		final boolean hadFilter = hasSeeOnlySettings();
		final Collection previousSelections = getSelectedRowsInDescendingOrder();
		final boolean filterRowsWereShowing = showFilterUI;
		int rowIdx = -1;	
		if (!isToolTipListenerSet) {
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(final MouseEvent e) {
					if (showFilterUI
							&& (table.getSelectedRow() == table.ROW_IDX_FILTER_OP || table
									.getSelectedRow() == table.ROW_IDX_FILTER_VALUE)) {
						if (multiQuerySet != null && multiQuerySet.isMultiFilterSet()) {
							String message=multiQuerySet.getText();
							message="<font face='courier'>"+message.substring(message.indexOf("<html>"),message.length())+"</font>";
							final JButton query=SwingBasics.getButton("See query",MmsIcons.getMagnifyIcon(),'\0', new ActionListener(){

								public void actionPerformed(ActionEvent e) {
									ToolTipOnDemand.getSingleton().hideTipWindow();
									query(false, QUERY_TYPE.ALL);
								}
								
							}, null);
							ToolTipOnDemand.getSingleton()
									.show(table.getTableHeader(), false, e.getX(), e.getY(),
											query,Basics.toHtmlUncenteredSmall("Can not adjust a single filter because ...", queryToolTip+message));
						}
					}
				}
			});
			isToolTipListenerSet=true;
		}
		if (!showFilterUI) {
			setFilterRows(true);
			rowIdx = table.getEditingRow();
			if (rowIdx < 0) {
				rowIdx = table.getSelectedRow();
			}
		}
		final ArrayList<String> highlightedColumns=new ArrayList<String>();
		int []vis=SwingBasics.getVisualIndexes(table);
		if (queryType == QUERY_TYPE.HIGHLIGHTED){
			final int[] a= getComparableSelectedModelColumns(true, true);
			if (a.length >= 1) {
				for (int i=0;i<a.length;i++){
					highlightedColumns.add(modelColumnIdentifiers.get(a[i]));
					a[i]= SwingBasics.getVisualIndexFromModelIndex(table, a[i]);
				}
				vis=a;
			}
		} else if (queryType == QUERY_TYPE.FAVORITE){
			final int[]dcs=getColumnDisplayOrder(false, PROPERTY_QUERY_FAVORITE, true);
			if (dcs.length >0){
				
				final ArrayList<Integer> showing = new ArrayList<Integer>(), hidden= new ArrayList<Integer>();
				for (final int dc : dcs) {
					final int vi = getVisualColumnIndexFromDataColumnIndex(dc);
					if (Basics.contains(vis, vi)) {
						showing.add(vi);
					} else {
						hidden.add(dc);
					}
				}
				if (showing.size() == 0) {
					if (!PopupBasics
							.ask(table, "No favorite columns are showing ... show all visible?")) {
						return false;
					}
				} else {
					int choice=0;
					if (showing.size() < dcs.length) {
						choice=limitTreeToContain && groupedDataSource.hasApplicationSpecificTreeSort?
						1:PopupBasics.getRadioButtonOption(
								table,
								"Only "
								+ showing.size()
								+ " of "
								+ dcs.length
								+ " favorite columns are showing ... ?", 
								"Please make a choice...",
								new String[]{"Only query "+showing.size(), "Show & query all "+dcs.length}, 0, true,true);
						if (choice<0) {
							return false;
						}
						if (choice==1){
							for (final int dc:hidden){
								showColumn(dc);
							}
							showing.clear();
							for (final int dc:dcs){
								final int vi = getVisualColumnIndexFromDataColumnIndex(dc);
								showing.add(vi);
							}
						}
					}
					vis = new int[showing.size()];
					for (int i = 0; i < vis.length; i++) {
						vis[i] = showing.get(i);
					}
					
				}
			}
		}
		final int []visualIndexes=vis;
		table.scrollRowAndColToVisible(0, 0);
		final RotateTable rt = new RotateTable(table, visualIndexes, new int[] { 0, 1 }, new String[] {
				"Condition", "Search value" }, new Boolean[]{Boolean.FALSE,Boolean.TRUE,Boolean.TRUE}, null, new int[] { 125, 75,
				225});
		clickedApplyFilter = false;
		seeOnly = SwingBasics
				.getButton(
						"See only",
						MmsIcons.getMagnifyIcon(),
						'l',
						null,
						"Click this button to:<ul><li>Close this window <li>Continue <b>hiding</b> all rows that do not match <br>the filter settings</ul>",
						true);
		

		seeAll = SwingBasics.getButton("See all", MmsIcons.getEyeIcon(),
				'a', null,
				"Clear all filter settings<br>without closing the window.",
				true);
		
		seeAll.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				clearSeeOnlySettings();
				notifyViewChanged();
				initFindFilterButtons();
				ToolTipOnDemand.getSingleton().hideTipWindow();
				rt.repaint();
			}
		});
		final JComboBox logicalOperatorComboBox=new JComboBox();
		final JButton prevQuery = SwingBasics.getButton("Previous query", MmsIcons.getLeftIcon(),
				KeyEvent.VK_LEFT, null,null);
		final JButton addQuery = SwingBasics.getButton("", MmsIcons.getRightIcon(),
				KeyEvent.VK_RIGHT, null,null);
		final JPanel multiQueryPanel=getMultiQueryPanel(logicalOperatorComboBox, prevQuery, addQuery);
		multiQueryPanel.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
		final JScrollPane scroller=new JScrollPane(filterMessagePane);
		scroller.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
		prevQuery.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						rt.stopCellEditing();
						scrollQuery(false, rt, logicalOperatorComboBox, visualIndexes);
						setQueryButtons(prevQuery, addQuery);
					}
				});
		addQuery.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				rt.stopCellEditing();
				scrollQuery(true, rt, logicalOperatorComboBox, visualIndexes);
				setQueryButtons(prevQuery, addQuery);
				if (scroller.getParent() == null && multiQuerySet.isFilterable()) {
					multiQueryPanel.add(scroller, BorderLayout.CENTER);
					multiQueryPanel.setPreferredSize(new Dimension(520, 130));
					dlg.pack();
				}
				
			}
		});
		seeOnly.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				
				clickedApplyFilter = true;
				final PersonalizableTableModel utm = getUngroupedModel();
				utm.getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_QUERY_CONCLUSION);				
				final Window w = SwingUtilities.getWindowAncestor(seeOnly);
				if (w != null) {
					SwingBasics.closeWindow(w);
					toFront();
				}				
				QuerySet set=multiQuerySet.getCurrent();
				if(!set.isValidQuerySet()){
					multiQuerySet.previous(true);
					rt.clear(multiQuerySet.getCurrent().getFilters());
				}
				if (removeActiveFiltered(true)>0){
					refreshShowingTable(true);
				}
				if (limitTreeToContain) {					
					utm.refresh();
				} else {
					utm.getDataSource().notifyFilterAppiedListeners("query");
				}
				utm.getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_NORMAL);
				notifyViewChanged();
			}
		});
		if (multiQuerySet.isMultiFilterSet()){
			multiQueryPanel.add(scroller,BorderLayout.CENTER);
			multiQueryPanel.setPreferredSize(new Dimension(520,130));
			updateMultiQueryText();
		}

		final JComponent[] extraComponents;
		final JLabel prior;
		if (table.sizeInfo == null) {
			prior = null;
		} else {
			prior = table.sizeInfo;
		}
		table.sizeInfo = new JLabel();
		refreshSizeInfo();
		extraComponents = limitTreeToContain ? new JComponent[] {seeOnly }
				: new JComponent[] { seeAll, seeOnly};
		saveFocus(true);
		SwingBasics.ignoreKeyEvent(rt, KeyEvent.VK_ENTER);
		creatingFilter = true;
		setQueryButtons(prevQuery, addQuery);
		if(multiQuerySet.isMultiFilterSet()){
			setFilterRows(true);
			notifyViewChanged();
		}
		suspendControlPanel();
		getUngroupedModel().getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_QUERY_PROPOSING);
		
		final boolean ok=popupTable(table.sizeInfo, multiQueryPanel, MmsIcons.getFindImage(), "changeAll" +(queryType != QUERY_TYPE.ALL?queryType:""),
				limitTreeToContain ? "Limit tree to contain ..."
						: "Query \"" + key + "\"", rt, extraComponents,
				Basics.indexOf(visualIndexes, savedVisualColumnIndex), 2, limitTreeToContain ? seeOnly : null,
				getFindToolTipText(), SEE_SELECTED_TEXT, true, Basics
						.toHtmlUncentered(limitTreeToContain ? "No limits"
								: "Cancel", "Restore table to prior state"),
				limitTreeToContain ? "No limits" : null, queryType != QUERY_TYPE.HIGHLIGHTED);
		getUngroupedModel().getDataSource().setFilteringContext(PersonalizableDataSource.FILTER_CONTEXT_NORMAL);
		if (!ok) { // cancel
			// pressed
			clearSeeOnlySettings();
		}
		dataSource.setControlPanelSuspension(null);
		if(multiQuerySet.isMultiFilterSet()){
			setFilterRows(false);
			notifyViewChanged();
		}
		
		creatingFilter = false;
		if (clickedApplyFilter) {
			if (!hasSeeOnlySettings()) {
				setFilterRows(false);
				if (!filterRowsWereShowing && rowIdx > -1) {
					table.scrollRowAndColToVisible(rowIdx, 0);
				}
				select(previousSelections, false, false);
				rowIdx = showFilterUI ? 2 : 0;
			}
		} else { // remove filter or find
			if (hasSeeOnlySettings()) {
				removeActiveFiltered(true);
				table.clearSelection();
				final int mode = table.getSelectionModel().getSelectionMode();
				final boolean additive = mode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
				final Row[] select;
				final List<Row> filteredDataList = dataSource
						.getFilteredDataRows();

				if (filteredDataList.size() > 0) {
					if (!additive) {
						select = new Row[] { (Row) filteredDataList.get(0) };
					} else {
						int n = filteredDataList.size();
						select = new Row[n];
						for (int i = 0; i < n; i++) {
							select[i] = (Row) filteredDataList.get(i);
						}
					}
				} else {
					select = null;
				}
				clearSeeOnlySettings();
				if (!filterRowsWereShowing) {
					setFilterRows(false);
				}
				if (select != null) {
					scrollOnce = 0;
					for (int i = 0; i < select.length; i++) {
						final int rowIdx2 = PersonalizableTableModel.this
								.select(select[i], false, false, additive);
						if (i == 0) { // focus on first row
							rowIdx = rowIdx2;
						}
					}
					scrollOnce = -1;
				}
			} else {
				if (!filterRowsWereShowing) {
					setFilterRows(false);
				}
				select(previousSelections, false, false);

			}
		}
		if (prior != null) {
			table.sizeInfo = prior;
			refreshSizeInfo();
		}
		if (queryType == QUERY_TYPE.HIGHLIGHTED && PopupBasics.ask(table, "<html>Are these now your <u>favorite</u><br>query columns for this table?</html>")){
			properties.setProperty(getPropertyName(PROPERTY_QUERY_FAVORITE), Basics
					.urlEncode(highlightedColumns));
			notifyViewChanged();
		}
		restoreFocus(rowIdx);
		return hasSeeOnlySettings() != hadFilter;
	}
	
	private void setQueryButtons(final JButton prevQuery, final JButton addQuery){
		prevQuery.setEnabled(multiQuerySet.hasPrevious());
		addQuery.setText(multiQuerySet.hasNext() ? "Next query"
				: "Add query");		
	}
	
	private void scrollQuery(final boolean next, final RotateTable rt, final JComboBox logicalOperatorComboBox, final int []visualIndexes){
		Map<Integer, Filter> filters = getFilters(rt, visualIndexes);
		if (next && filters.size() == 0 && !multiQuerySet.hasNext()) {
			PopupBasics
					.alert(
							dlg,
							"<html>You can not add a query until you <br>have a condition and search value for <br> at least one column.</html>",
							"Can not add query...", true);
			return;
		}
		boolean logicalOperatorChanged=!Basics.equals(logicOperator, multiQuerySet.getCurrent().getLogicalOperator());
		if (filters.size() != 0) {
			final QuerySet set = new QuerySet(logicOperator);
			set.setFilter(filters);
			multiQuerySet.updateCurrentQuerySet(set);
		}
		if (next && !multiQuerySet.next(filters.size()==0)) {
			multiQuerySet.addQuerySet(
					new LinkedHashMap<Integer, Filter>(), "");
			isAddQueryReset = true;
		} else {
			if (!next){
				multiQuerySet.previous(filters.size()==0);
			}
			logicalOperatorComboBox.setSelectedItem(multiQuerySet
					.getCurrent().getLogicalOperator());
		}		
		boolean old = autoFilter;
		autoFilter = false;
		rt.clear(multiQuerySet.getCurrent().getFilters());
		rt.updateUI();
		autoFilter = old;
		updateMultiQueryText();
		if (next && !isAddQueryReset && logicalOperatorChanged){
			syncFilter(false);
		}
		isAddQueryReset = false;
	}

	Map<Integer, Filter> getFilters(final RotateTable rt, final int []visualIndexes){
		Map<Integer, Filter> filters = new LinkedHashMap<Integer, Filter>();
		for (int i = 0; i < rt.getRowCount(); i++) {
			final int dataColumnIndex=getDataColumnIndexFromVisualIndex(visualIndexes[i]);
			final String dataColumnIndentifier=getDataColumnIdentifier(dataColumnIndex);
			String condtion = (String) rt.getValueAt(i, 0);
			Object conditionValue = rt.getValueAt(i, 1);
			if (!Basics.isEmpty(conditionValue)) {
				final Filter filter = new Filter(dataColumnIndex, translateOp(condtion),
						conditionValue);
				filters.put(dataColumnIndex, filter);
			}
		}
		return filters;
		}


	private JMenu findFilterMenu, manageColumnsMenu;
	private DisabledExplainer findDisabled,manageColumnsDisabled;

	public boolean canEnterNewItem() {
		return (dataSource.isCreatable() || isCompanionModelWithCreatableTree())
				&& !isFavoritesSelection();
	}

	private boolean moreTableOptionsSubMenu=false;
	public void useMoreTableOptionsSubMenu(){
		moreTableOptionsSubMenu=true;
	}
	
	private boolean avoidChangeAllColumnsMenuItem=false;
	public void avoidChangeAllColumnsMenuItem(){
		avoidChangeAllColumnsMenuItem=true;
	}
	
	private void initPopup() {
		if (popup == null) {
			JMenu moreTableOptions=null;
			final boolean worksWithMultipleRows = dataSource
					.getMaximumCardinality() > 1;
			popup = new JPopupMenu();
            cloneMenuItems();
			addMenuItems(false);
			if (!readOnly && worksWithMultipleRows
					&& getUngroupedModel().canEditCellForMultipleRows) {
				changeEntireColumn.setText("Change all values...");
				changeEntireColumn
						.setToolTipText(Basics
								.toHtmlUncentered("Change all values...",
										"Set a single value for all cells<br> in the selected column"));

				if(hasAnyEditAllColumn() && !avoidChangeAllColumnsMenuItem){					
					popup.add(changeEntireColumn);
				}
				
			}
			if (invokerDefinedHeaderMenuItems > 0) {
				popup.addSeparator();
			}
			if (invokerDefinedBodyMenuItems > 0) {
				popup.addSeparator();
			}
			if (dataSource.isMovable()) {
				final JMenu move = new JMenu("Move");
				move.setIcon(MmsIcons.getCogIcon());
				move.setMnemonic('m');
				move.add(moveTopItem);
				move.add(moveUpItem);
				move.add(moveDownItem);
				move.add(moveBottomItem);
				popup.add(move);
			}
			if (!readOnly) {
				int bodyItemsSoFar = 0;

				if (getUngroupedModel().dataSource.isAddable() && canHavePickMenu) {
					popup.add(chooseItemFromList);

					chooseItemFromList.setText(getAddMenuText());
					chooseItemFromList.setIcon(getAddIcon());
					addDisabledButton=new DisabledExplainer(chooseItemFromList);
					bodyItemsSoFar++;
				}
				if (canEnterNewItem() && !getUngroupedModel().useCustomNewItemOnly) {
					popup.add(newItem);
					newItem.setText(getNewText());
					newItem.setIcon(MmsIcons.getNewIcon());
					newDisabledButton=new DisabledExplainer(newItem);
					bodyItemsSoFar++;
				}

				if (dataSource.isSaveable()) {
					popup.add(saveItem);
					saveItem.setText("Save");
					saveItem.setIcon(MmsIcons.getSaveIcon());
					bodyItemsSoFar++;
				}
				deleteItemPopupIndex = popup.getComponentCount();
				if (getUngroupedModel().dataSource.isDeletable()
						&& !isFavoritesSelection()) {
					popup.add(delete.da.getMenuItem());
					bodyItemsSoFar++;
				}
				if (getUngroupedModel().dataSource.isRemovable()) {
					popup.add(remove.da.getMenuItem());
					bodyItemsSoFar++;
				}

				if (bodyItemsSoFar > 0) {
					popup.addSeparator();
				}
				
				if (!isPickList &&!(showLimitedMenu)) {
					if (!getUngroupedModel().moreTableOptionsSubMenu){
						popup.add(editInPlaceItem);
					}
					editInPlaceItem.setMnemonic('e');
				}
			}
			if (editMenuItem != null) {
				editMenuItem.create(null, popup, editMenuItem.addToTail);
			}
			exploreCellIndex=popup.getSubElements().length;
			if(!(showLimitedMenu)&& !hideSelectedMenu)
				popup.add(exploreItem);
			
			if (isPickList) {
				hideItem.setIcon(MmsIcons.getWideBlankIcon());
				unhideItem.setIcon(MmsIcons.getWideBlankIcon());
				popup.add(hideItem);
				popup.add(unhideItem);
				popup.addSeparator();
			}
			if (worksWithMultipleRows) {
				if (moreTableOptions!=null){
					moreTableOptions.add(printItem);
				}else{
				popup.add(printItem);
				}
				findFilterMenu = new JMenu("Find");
				findDisabled = new DisabledExplainer(findFilterMenu);
				findFilterMenu.setMnemonic('f');
				boolean hasFilterItems = false;
				if (dataSource.isFilterable()) {
					findFilterMenu.setIcon(MmsIcons.getFindIcon());
					hasFilterItems = true;
					// table.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
					// InputEvent.CTRL_MASK) );
					findFilterMenu.add(findItem);
					findFilterMenu.add(seeAllItem);
					//popup.add(findSeeOnlyItem);
					findFilterMenu.add(queryFavoriteColumnsItem);
					findFilterMenu.add(queryAllColumnsItem);
					findFilterMenu.add(querySelectedColumnsItem);
					popup.add(applyFilterItem);
					applyFilterItem.addActionListener(new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							table.moveFocusFromFilteringRowIfNecessary();
							syncFilter(true);
							notifyViewChanged();
						}
					});
					findFilterMenu.add(applyFilterItem);
					findFilterMenu.add(showFilterItem);
					if (!readOnly|| dataSource.isCreatable()){
						findFilterMenu.add(viewRefresh.getMenuItem(getTable()));
					}
				}
				if (supportIgnoreInvalid
						&& getModelType() == TYPE_UNGROUPED
						&& dataSource instanceof PersonalizableDataSource.CanPick) {
					final ActionListener useDisablingAction = new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							useDisabling = !useDisabling;
							table.resizeAndRepaint();
							notifyViewChanged();
							useDisablingItem
									.setText(!useDisabling ? "Disable invalid"
											: "Ignore invalid");
						}
					};
					findFilterMenu.add(useDisablingItem);
					useDisablingItem.setText(!useDisabling ? "Disable invalid"
							: "Ignore invalid");
					useDisablingItem.addActionListener(useDisablingAction);
					hasFilterItems = true;
				}
				if (hasFilterItems) {
					popup.add(findFilterMenu);
				}
			}
			
			manageColumnsMenu = new JMenu("Manage columns");
			manageColumnsMenu.setIcon(MmsIcons.getColumnsIcon());
			manageColumnsDisabled = new DisabledExplainer(manageColumnsMenu);
			manageColumnsMenu.setMnemonic('c');
			if (moreTableOptions!=null){
				moreTableOptions.add(manageColumnsMenu);
			}else{
				popup.add(manageColumnsMenu);
			}
			if (canSortArrangeColumns) {
				if (moreTableOptions!=null){
					moreTableOptions.add(sortArrangeColumnsItem);
				}else{
					manageColumnsMenu.add(sortArrangeColumnsItem);
				}
			}
			if (canSort()) {
				manageColumnsMenu.add(sortAscendingItem);
				manageColumnsMenu.add(sortDescendingItem);
				manageColumnsMenu.add(refreshSortItem);

				manageColumnsMenu.add(sortSequenceItem);
				manageColumnsMenu.addSeparator();
				manageColumnsMenu.add(unsortItem);
				manageColumnsMenu.add(unsortAllItem);
				if (isCreateTabsOn) {
					manageColumnsMenu.addSeparator();
					manageColumnsMenu.add(tabItem);					
				}

			}
			manageColumnsMenu.addSeparator();
			if(table.getColumnCount() > 2) {
				manageColumnsMenu.add(shiftLeftItem);
				manageColumnsMenu.add(shiftRightItem);
			}
			manageColumnsMenu.add(hideColumnItem);
			manageColumnsMenu.add(fitWindowItem);
			manageColumnsMenu.addSeparator();
			manageColumnsMenu.add(showAllColumnsItem);
			manageColumnsMenu.add(freezeColumnsItem);
			manageColumnsMenu.add(unfreezeColumnsItem);
			if (worksWithMultipleRows) {
				final JMenu manageRowsMenu = new JMenu("Manage rows");
				manageRowsMenu.setMnemonic('r');
				manageRowsMenu.setIcon(MmsIcons.getRowsIcon());
				if(!hideSelectedMenu){
				if (moreTableOptions!=null){
					moreTableOptions.add(manageRowsMenu);
				}else{
					popup.add(manageRowsMenu);
				}
				}
				saveAsHtmlItem = new JMenuItem("Show in browser", MmsIcons
						.getWorldSearchIcon());
				saveAsHtmlItem.setMnemonic('s');
				saveAsHtmlItem.addActionListener(saveAsHtmlAction);
				manageRowsMenu.add(saveAsHtmlItem);
				
				manageRowsMenu.addMenuListener(new MenuListener() {
					public void menuSelected(final MenuEvent event) {
						loadRowsMenu(manageRowsMenu);
					}

					public void menuCanceled(final MenuEvent event) {
						manageRowsMenu.removeAll();
						manageRowsMenu.add(saveAsHtmlItem);
					}

					public void menuDeselected(final MenuEvent event) {
						manageRowsMenu.removeAll();
						manageRowsMenu.add(saveAsHtmlItem);
					}
				});

			}
			if(!(showLimitedMenu)&&!hideSelectedMenu){
				final JMenuItem manageViewItem=getViewMenu(table, openViewItem, copyViewItem,
						pasteViewItem);
				if (moreTableOptions!=null){
					moreTableOptions.add(manageViewItem);
				}else{
					popup.add(manageViewItem);
				}
			}
			if (supportsTearAway()&&!hideSelectedMenu) {
				final PersonalizableTableModel u = getUngroupedModel();
				if (!u.isPickList) {
					if (moreTableOptions!=null){
						moreTableOptions.add(tearAwayItem);
					}else{
						popup.add(tearAwayItem);
					}
				}
				u.tearAwayHandler.addContext(table, tearAwayItem);
			}
			if (worksWithMultipleRows) {
				if (dataSource.isFilterable()) {
					if (getModelType() == TYPE_UNGROUPED
							|| Basics
									.isEmpty(getUngroupedModel().applicationSpecificTreeSort)) {
						if (isCompanionTable && isTreeOn) {
							ungroupedModel.recentTrees.registerActions(table,
									recentTreeMenu);
							ungroupedModel.favoriteTrees.registerActions(table,
									favoriteTreeMenu);
						}
					}
				}
				if (isTreeOn) {
					treePopup = null;
					treeMenu = createTreeMenu(newTreeItem, sortBasedTreeItem,
							removeTreeItem,
							recentTreeMenu, favoriteTreeMenu);
					popup.add(treeMenu);
				}

			}
			if (moreTableOptions!=null){
				popup.add(moreTableOptions);
			}
			if (hasCustomTail()) {
				popup.addSeparator();
				addMenuItems(true);
			}
			if (canImportExport&&needImportExportItem) {
			popup.addSeparator();
			JMenu menu = new JMenu("Import/Export");
			if (!readOnly && dataSource.isCreatable()) {
				SwingBasics.setMenuItem("Import", new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						TabImporter.run(PersonalizableTableModel.this,
								containingWnd);
					}

				}
				, menu, 'i', MmsIcons.getImportIcon());
			}
			SwingBasics.setMenuItem("Export", new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (pickChecker != null) {
						ArrayList al = getSelectedRowsInDescendingOrder();
						if (al.size() > 0)
							pickChecker.checkPick(getModelShowing().table,
									getSelectedRowsInDescendingOrder(), true,
									false);
					}
					TabExporter.execute(PersonalizableTableModel.this,
							containingWnd);
				}
			}

			, menu, 'e', MmsIcons.getExportIcon());
		
			popup.add(menu);
			}
		}else{
			establishFindFilterMenu();
			MenuElement[] elements=popup.getSubElements();
			for(int i=0;i<elements.length;i++){
				MenuElement ele=elements[i];
				if(ele.equals(exploreItem)){
					isExploreItemAddded=true;
				}
			}
			if(!isExploreItemAddded&&!(showLimitedMenu)&&!hideSelectedMenu){
				popup.add(exploreItem, exploreCellIndex);
				isExploreItemAddded=true;
			}
		}
		
		// SwingBasics.setFontAllMenuElements(popup,
		// PersonalizableTable.FONT_POPUP_MENU);
	}

	public void setTreeMenu() {
		if(treeMenu != null) {
			if(!isTreeOn) {
				treeMenu.setVisible(false);
			}
			else {
				treeMenu.setVisible(true);
			}
		}
	}
	
	JMenuItem saveAsHtmlItem;

	private boolean cancelledIAS;
	private boolean canImportExport = true;
	private boolean canDragDrop = false;

	public void setCanImportExport(final boolean ok) {
		canImportExport = ok;
	}

	public void setCanDragDrop(final boolean ok) {
		canDragDrop = ok;
	}
	
	public boolean getCanDragDrop() {
		return canDragDrop;
	}
	
	private void loadRowsMenu(final JMenu menu) {
		menu.removeAll();
		if(!(showLimitedMenu)){	
		menu.add(saveAsHtmlItem);}
		configureRowOptions(menu, null);
		if (canImportExport) {
			menu.addSeparator();
			if (!readOnly && dataSource.isCreatable()) {
				SwingBasics.setMenuItem("Import", new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						TabImporter.run(PersonalizableTableModel.this,
								containingWnd);
					}

				}

				, menu, 'i', MmsIcons.getImportIcon());
			}
			SwingBasics.setMenuItem("Export", new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (pickChecker != null) {
						ArrayList al = getSelectedRowsInDescendingOrder();
						if (al.size() > 0)
							pickChecker.checkPick(getModelShowing().table,
									getSelectedRowsInDescendingOrder(), true,
									false);
					}
					TabExporter.execute(PersonalizableTableModel.this,
							containingWnd);
				}
			}

			, menu, 'e', MmsIcons.getExportIcon());
		}
		// SwingBasics.setFontAllMenuElements(menu,PersonalizableTable.FONT_POPUP_MENU);

	}

	private static String getKBName(String absPath) {
		String facilityName = null;
		if (absPath.indexOf('/') != -1 && absPath.indexOf(".pprj") != -1)
			facilityName = absPath.substring(absPath.lastIndexOf('/') + 1,
					absPath.lastIndexOf(".pprj"));
		return facilityName;
	}

	private int[] getComparableSelectedModelColumns(
			final boolean useFocusIfNoColumnHighted) {
		return getComparableSelectedModelColumns(false, useFocusIfNoColumnHighted);
	}
	private int[] getComparableSelectedModelColumns(
			final boolean allowButton, final boolean useFocusIfNoColumnHighted) {
		final int[] returnValue;
		if (selectedModelIndexes.length > 0) {
			int n = 0;
			for (int i = 0; i < selectedModelIndexes.length; i++) {
				if (isSortable(selectedModelIndexes[i])) {
					n++;
				} else if (allowButton){
					final int dc = getDataColumnIndex(selectedModelIndexes[i]);
					if (dc >= 0) {
						Class cl = metaRow.getClass(dc);
						if (JButton.class.equals(cl)) {
							n++;
						}
					}				
				}
			}
			returnValue = new int[n];
			for (int j = 0, i = 0; i < selectedModelIndexes.length; i++) {
				if (isSortable(selectedModelIndexes[i])) {
					returnValue[j] = selectedModelIndexes[i];
					j++;
				} else if (allowButton){
					final int dc = getDataColumnIndex(selectedModelIndexes[i]);
					if (dc >= 0) {
						Class cl = metaRow.getClass(dc);
						if (JButton.class.equals(cl)) {
							returnValue[j] = selectedModelIndexes[i];
							j++;
						}
					}				
				}

			}
		} else if (useFocusIfNoColumnHighted && focusModelColumnIndex >= 0 && isSortable(focusModelColumnIndex)) {
			returnValue = new int[] { focusModelColumnIndex };
		} else {
			returnValue = new int[] {};
		}
		return returnValue;
	}
	
	public Collection<String>getSelectedColumnIdentifiers(){
		final PersonalizableTableModel ms=getModelShowing();
		final int[] modelIndexes = ms.table.cellHighlighter.getSelectedColumns();
		final Collection<String>c=new ArrayList<String>();
		for (int i = 0; i < modelIndexes.length; i++) {
			c.add(getDataColumnIdentifier(getDataColumnIndex(modelIndexes[i])));
		}
		return c;
	}
	
	public Collection<String>getLabels(final Collection<String>identifiers){
		final Collection<String>c=new ArrayList<String>();
		for (final String identifier:identifiers) {
			final int dataColumnIndex=getDataColumnIndexForIdentifier(identifier);
			c.add(getColumnAbbreviation(dataColumnIndex));
		}
		return c;
	}
	
	private String getColumnAbbreviationsForModelColumnIndexes(final int []modelIndexes){
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < modelIndexes.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb
					.append(getColumnAbbreviation(getDataColumnIndex(modelIndexes[i])));
		}
		return sb.toString();
	}

	private String getUnFrozenColumnsList(final int []modelIndexes){
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < modelIndexes.length; i++) {			
			if (columnsThatCanNotBeFrozen.contains(getDataColumnIdentifier(getDataColumnIndex(modelIndexes[i])))) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(getColumnAbbreviation(getDataColumnIndex(modelIndexes[i])));
			}
		}
		return sb.toString();
	}

	private String getColumnAbbreviationsForDataColumnIndexes(final int []dataColumnIndexes){
		final StringBuilder sb = new StringBuilder(100);
		for (int i = 0; i < dataColumnIndexes.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb
					.append(getColumnAbbreviation(dataColumnIndexes[i]));
		}
		return sb.toString();
	}

	int[] getSelectedModelColumns(
			final boolean useFocusIfNoColumnHighted) {
		final int[] returnValue;
		if (selectedModelIndexes.length > 0) {
			returnValue=selectedModelIndexes;
		} else if (useFocusIfNoColumnHighted && focusModelColumnIndex >= 0 ) {
			returnValue = new int[] { focusModelColumnIndex };
		} else {
			returnValue = new int[] {};
		}
		return returnValue;
	}


	private void configureRowOptions(final JMenu m, final ColumnConfigModel ccm) {
		final JMenuItem useDittosItem = new JMenuItem(!useDittos ? "Use dittos"
				: "No ditto use", MmsIcons.getWideBlankIcon());
		if(!(showLimitedMenu)){	m.add(useDittosItem);}
		useDittosItem.setMnemonic('u');
		useDittosItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (ccm == null) { // making changes globally
					useDittos = !useDittos;
					table.resizeAndRepaint();
					useDittosItem.setText(!useDittos ? "Use dittos"
							: "No ditto use");
					notifyViewChanged();
				} else {
					final boolean b;
					if (ccm.rowOptions.useDittos == null) {
						b = useDittos;
					} else {
						b = ccm.rowOptions.useDittos.booleanValue();
					}
					ccm.rowOptions.useDittos = b ? Boolean.FALSE : Boolean.TRUE;
					useDittosItem.setText(b ? "Use dittos" : "No ditto use");
					ccm.doneButton.setEnabled(true);
					ccm.changed = true;
				}
			}
		});
		// m.add(getComboBoxMenu(ccm));

		m.add(selectAllItem);

		SwingBasics.setMenuItem("Change color (globally)",
				new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final Color color = JColorChooser
								.showDialog(
										table,
										"Color for alternating row (affects all tables)",
										(ccm == null || ccm.rowOptions.alternatingColor == null) ? globalAlternatingRowColor
												: ccm.rowOptions.alternatingColor);
						if (color != null) {
							if (ccm == null) {
								setGlobalAlternatingRowColor(color);
								refreshShowingTable(false);
							} else {
								ccm.rowOptions.alternatingColor = color;
								ccm.doneButton.setEnabled(true);
								ccm.changed = true;
							}
						}
					}
				}

				, m, 'c', MmsIcons.getColorSwatch16Icon());

	}

	final JMenu createTreeMenu(final JMenuItem treeItem,
			final JMenuItem sortBasedTreeItem,
			final JMenuItem removeTreeItem, final JMenu recentTreeMenu,
			final JMenu favoriteTreeMenu) {
		final JMenu treeMenu = new JMenu("Tree");
		treeMenu.setIcon(MmsIcons.getChartOrg16Icon());
		treeMenu.setMnemonic('t');
		final JMenuItem nt= new JMenuItem("New tree",
				MmsIcons.getNewIcon());
		treeMenu.add(nt);	
		
		table.echoAction(nt, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				getUngroupedModel().buildTreeByDraggingAndDroppingColumns();
			}
		}, KeyEvent.VK_T, InputEvent.CTRL_MASK, 'n');			

		if (removeTreeItem != null) {
			treeMenu.add(removeTreeItem);
			removeTreeItem.setText("Table ");
			removeTreeItem.setMnemonic('t');
		}
		final JMenu newMenu = new JMenu("<html><i>New</i> tree using..</html>");
		newMenu.setIcon(MmsIcons.getWrench16Icon());
		treeMenu.add(newMenu);
		newMenu.setMnemonic('n');
		final JMenuItem newGenieAssistedItem = new JMenuItem("Drop down list of columns",
				MmsIcons.getNewIcon());
		newMenu.add(newGenieAssistedItem);
		table.echoAction(newGenieAssistedItem, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				getUngroupedModel().buildTreeFromDropDownListOfColumns=true;
				getUngroupedModel().newTree();
			}
		}, KeyEvent.VK_G, InputEvent.CTRL_MASK, 'd');
		
		
		if (treeItem != null) {
			newMenu.add(treeItem);
			treeItem.setText("");
			treeItem.setMnemonic('u');
		}
		if (sortBasedTreeItem != null) {
			newMenu.add(sortBasedTreeItem);
			sortBasedTreeItem.setText("Current sort order");
			sortBasedTreeItem.setMnemonic('c');
		}
		final JMenuItem newTreeFreshStartItem = new JMenuItem("Dragging and dropping of columns",
				MmsIcons.getTableRefreshIcon());
		newMenu.add(newTreeFreshStartItem);
		newTreeFreshStartItem.setMnemonic('d');
		newTreeFreshStartItem.addActionListener( new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				getUngroupedModel().buildTreeByDraggingAndDroppingColumns();
			}
		});

		favoriteTreeMenu.setIcon(MmsIcons.getWideBlankIcon());
		favoriteTreeMenu.setText("Favorite trees");
		favoriteTreeMenu.setIcon(MmsIcons.getHeart16Icon());
		favoriteTreeMenu.setMnemonic('f');
		treeMenu.add(favoriteTreeMenu);
		recentTreeMenu.setText("Recent trees");
		recentTreeMenu.setIcon(MmsIcons.getClock16Icon());
		
		recentTreeMenu.setMnemonic('r');
		treeMenu.add(recentTreeMenu);
		return treeMenu;
	}

	private int getDeletableSelectionCount(final int[] selected) {
		int deletable = 0;
		if (getUngroupedModel().dataSource.isDeletable() && selected != null) {
			for (int i = 0; i < selected.length; i++) {
				final Row row = getRowAtVisualIndex(selected[i]);
				if (row != null && row.isDeletable() && !getUngroupedModel().deletionHasBeenResolved.contains(row)) {
					deletable++;
				}
			}
		}
		return deletable;
	}



	public int getSelectedAndEditableDataRowCount(final int[] selected) {
		int n = 0;
		if (selected != null) {
			n = selected.length;

			for (int i = 0; i < selected.length; i++) {
				if (showFilterUI
						&& (selected[i] == table.ROW_IDX_FILTER_VALUE || selected[i] == table.ROW_IDX_FILTER_OP)) {

					n--;
				} else {
					final Row row = getRowAtVisualIndex(selected[i]);
					if (!areAnyDisplayedColumnsEditable(row)) {
						n--;
					}
				}
			}

		}
		return n;
	}
	public int getDataRowCount(final int[] selected) {
		int n=0;
		for (final int i:selected) {
			if (null != this.getRowAtVisualIndex(i)) {
				n++;
			}
		}
		return n;
	}

	public int getSelectedDataRowCount(final int[] selected) {
		int n = 0;
		if (selected != null) {
			n = selected.length;
			if (showFilterUI) {
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] != table.ROW_IDX_FILTER_VALUE
							&& selected[i] != table.ROW_IDX_FILTER_OP) {
						break;
					} else {
						n--;
					}
				}
			}
		}
		return n;
	}

	public interface PopupMenuListener {
		void menuPoppedUp(PopupMenuItem popupMenuItem);
	}
	
	public interface MenuItemListener {
		void keyboardActionStarted(JMenuItem menuItem);
		String computeNewAnomaly();
		boolean useOriginalDisabledText();
		
	}

	Collection<Operation>menuItemListeners=new ArrayList<Operation>();

	public abstract class Operation implements MenuItemListener{
		
		public void keyboardActionStarted(final JMenuItem menuItem){
			assert(menuItem==da.getMenuItem());
			operationStarted(true);
		}
		
		final void operationStarted(final boolean isKeyboard){
			final int[] selected = table.getSelectedRows();
			final int rowsWithData=getDataRowCount(selected);
			final int selectedDataRowCount = getSelectedDataRowCount(selected);
			prepareStartedOperation(isKeyboard, selected, rowsWithData, selectedDataRowCount);	
		}
		
		void prepareStartedOperation(final boolean isKeyboard, int []selected, int rowsWithData, int selectedDataRowCount){			
		}
		
		public String computeNewAnomaly(){
			final int[] selected = table.getSelectedRows();
			final int rowsWithData=getDataRowCount(selected);
			final int selectedDataRowCount = getSelectedDataRowCount(selected);
			return computeNewAnomaly(selected, rowsWithData, selectedDataRowCount);	
		}
		
		abstract String computeNewAnomaly(final int []selected, final int rowsWithData, final int selectedDataRowCount);
		
		void recompute(final int []selected, final int rowsWithData, final int selectedDataRowCount){
			final String anomaly=computeNewAnomaly(selected,rowsWithData,selectedDataRowCount);
			da.setEnabled(anomaly==null, null, anomaly);
			prepareStartedOperation(false, selected, rowsWithData, selectedDataRowCount);
		}
		
		public 	boolean useOriginalDisabledText(){
			return false;
		}
		
		final DisabledExplainer da;
		public Operation(final DisabledExplainer da){
			this(da, true);
		}
		
		public Operation(final DisabledExplainer da, final boolean addToList){
			this.da=da;
			if (addToList && !menuItemListeners.contains(this)){
				menuItemListeners.add(this);
			}
		}
	}
	static public PopupMenuListener actOnSelectedNonLastRow=new PersonalizableTableModel.PopupMenuListener() {
		public void menuPoppedUp(
				final PersonalizableTableModel.PopupMenuItem pmi) {
			if (pmi.menuItem != null){
				pmi.setEnabled(pmi.areSelectedRowsOk(Integer.MAX_VALUE, false,
					false),"",pmi.disabledMsg);
			}
		}
	};

	static public PopupMenuListener actOnSelectedRow = new PopupMenuListener() {
		public void menuPoppedUp(
				final PersonalizableTableModel.PopupMenuItem pmi) {
			if(pmi.menuItem != null) {
			DisabledExplainer menuItemDisabled = new DisabledExplainer(pmi.menuItem);
			if (pmi.menuItem != null && menuItemDisabled != null){
				if(pmi.getTableModel().selectedDataRowCount > 0 && !pmi.getTableModel().isReadOnly()) {
					menuItemDisabled.setEnabled(true, null, null);
				}
				else {
					menuItemDisabled.setEnabled(false, pmi.menuText, selectRowsAnomaly());
				}
			}
			}
		}
	};

	public static PopupMenuListener actOnNoSelectedRows = new PopupMenuListener() {
		public void menuPoppedUp(
				final PersonalizableTableModel.PopupMenuItem pmi) {
			pmi.menuItem
					.setEnabled(pmi.getTableModel().selectedDataRowCount == 0);
		}
	};

	public static PopupMenuListener actOnSelectedRowIfEditable = new PopupMenuListener() {
		public void menuPoppedUp(
				final PersonalizableTableModel.PopupMenuItem pmi) {
			pmi.setEnabled(pmi.isFirstSelectedRowEditable(),"edit", pmi.disabledMsg);
		}
	};

	public static PopupMenuListener actOnSelectedRowsIfEditable = new PopupMenuListener() {
		public void menuPoppedUp(
				final PersonalizableTableModel.PopupMenuItem pmi) {
			pmi.setEnabled(pmi.areSelectedRowsEditable(),"edit", pmi.disabledMsg);
		}
	};

	private void notifyPopupMenuListeners() {
		for (final Iterator it = popupMenuItems.iterator(); it.hasNext();) {
			final PopupMenuItem pmi = ((PopupMenuItem) (it.next()));
			if (pmi.pml != null) {
				pmi.pml.menuPoppedUp(pmi);
			}
		}
		if (editMenuItem != null) {
			editMenuItem.pml.menuPoppedUp(editMenuItem);
		}
	}

	void notifyTreePopupMenuListeners(final Collection customMenuItems) {
		for (final Iterator it = customMenuItems.iterator(); it.hasNext();) {
			final PopupMenuItem pmi = ((PopupMenuItem) (it.next()));
			if (pmi.appliesToTree && pmi.pml != null) {
				pmi.pml.menuPoppedUp(pmi);
			}
		}
		resolveMultiRowEditRules();
	}

	private final ArrayList<PopupMenuItem> popupMenuItems = new ArrayList<PopupMenuItem>();
	private PopupMenuItem editMenuItem = null;

	public ActionListener getEditAction() {
		return editMenuItem != null ? editMenuItem.anAction : null;
	}

	public void setEditMenuItem(final ActionListener al, final String toolTip,
			final PopupMenuListener pml) {
		editMenuItem = new PopupMenuItem(EDIT_MENU_TEXT, false, al,
				pml == null ? actOnSelectedRowIfEditable : pml, false, MmsIcons
						.getEditIcon(), SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_E), toolTip, 'e',selectRowsAnomaly("editable item(s)"));

	}

	public class PopupMenuItem {
		private Boolean isCheckBoxMenuItem = false;;
		public String menuText;
		private final boolean addToTail;
		private final ActionListener anAction;
		private final PopupMenuListener pml;
		private final boolean appliesToTree;
		JMenuItem menuItem = null;
		private DisabledExplainer disabledExplainer;
		private final Icon icon;
		private final KeyStroke acceleratorKeyStroke;
		private String toolTipText;
		String disabledToolTipText;
		private final char mnemonic;
		private boolean createAMenu = false;
		
		ActionListener getActionListener(){
			return anAction;
		}
		
		KeyStroke getKeyStroke(){
			return acceleratorKeyStroke;
		}

		public void initMenuItemIfNecessary() {
			if (menuItem == null) {
				
				qualifyActions();
			}
		}
		
		public JMenuItem getMenuItem() {
			return menuItem;
		}

		public void setText(final String text) {
			menuText = Basics.strip(text, "<br>");
			if (menuItem != null) {
				menuItem.setText(menuText);
			}
		}

		public void setIcon(final Icon icon) {
			if (menuItem != null) {
				menuItem.setIcon(icon);
			}
		}
		
		private boolean enabled = true;

		void registerKeyboardAction(final PersonalizableTable table) {
			if (acceleratorKeyStroke != null) {
				table.registerKeyboardAction(new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						qualifyActions();
						if (pml != null) {
							pml.menuPoppedUp(PopupMenuItem.this);
						}
						
						if (enabled && menuItem != null && anAction != null) {
							anAction.actionPerformed(ae);
						}
						else {
							final String d=disabledExplainer.getDisabledToolTipText();
							setEnabled(false);
							disabledExplainer.showDisabledText(table, d);
						}
					}
				}, acceleratorKeyStroke, JComponent.WHEN_FOCUSED);
			}
		}
		
		void registerKeyboardAction(final JComponent cc) {
			if (acceleratorKeyStroke != null) {
				final ActionListener al=new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						qualifyActions();
						if (pml != null) {
							pml.menuPoppedUp(PopupMenuItem.this);
						}
						
						if (enabled && menuItem != null && anAction != null) {
							anAction.actionPerformed(ae);
						}
						else {
							final String d=disabledExplainer.getDisabledToolTipText();
							setEnabled(false);
							disabledExplainer.showDisabledText(cc, d);
						}
					}
				};
				SwingBasics.echoAction(cc, null, al, acceleratorKeyStroke, '\0');
			}
		}


		public void setEnabled(final boolean enabled) {
			this.enabled = enabled;
			setEnabled(enabled, "", Basics.isEmpty(disabledToolTipText)?"This menu item is currently not allowed.":disabledToolTipText);
		}

		public void setEnabledIfSelected(final boolean enabled, final String nameOfOperation, final String itemType){
			if (menuItem != null) {
				disabledExplainer.setEnabledIfSelected(enabled, nameOfOperation, itemType);
			}

		}
		public void setEnabledIfEditingInPlace(final String nameOfOperation){
			setEnabled(
					getModelShowing().getEditInPlace(),
					"\"'Enter/Edit directly in table\" is OFF",
					"To activate this menu option, you select <br>the \"'Enter/Edit directly in table\" item on the right<br>click menu.");
		}

		public boolean isEnabled(){
			return enabled;
		}
		public void setEnabled(final boolean enabled,
				final String nameOfOperation,
				final String msgExplainingWhyDisabled) {
			this.enabled = enabled;
			if (menuItem != null) {
				disabledExplainer.setEnabled(enabled, nameOfOperation, msgExplainingWhyDisabled);
			}
		}

		public void setToolTipText(final String toolTipText) {
			this.toolTipText = toolTipText;
			if (menuItem != null) {
				menuItem.setToolTipText(toolTipText);
			}
		}

		private PopupMenuItem(final PopupMenuItem other) {
			this.menuText = other.menuText;
			this.addToTail = other.addToTail;
			this.anAction = other.anAction;
			this.pml = other.pml;
			this.appliesToTree = other.appliesToTree;
			this.icon = other.icon;
			this.acceleratorKeyStroke = other.acceleratorKeyStroke;
			this.toolTipText = other.toolTipText;
			this.mnemonic = other.mnemonic;
			this.createAMenu = other.createAMenu;
			this.disabledToolTipText=other.disabledToolTipText;
		}

		public PersonalizableTableModel getTableModel() {
			return PersonalizableTableModel.this;
		}

		public PopupMenuItem(final boolean addSeparatorToTail) {
			this(null, addSeparatorToTail, null, null, null, null,null);
		}

		public PopupMenuItem(final String menuText, final boolean addToTail,
				final ActionListener actionListener,
				final PopupMenuListener pml,
				final KeyStroke acceleratorKeyStroke, final String toolTipText,final String disabledToolTipText) {
			this(menuText, addToTail, actionListener, pml, false,
					acceleratorKeyStroke, toolTipText,disabledToolTipText);
		}

		public PopupMenuItem(final String menuText, final boolean addToTail,
				final ActionListener actionListener,
				final PopupMenuListener pml, final boolean appliesToTree,
				final Icon icon, final KeyStroke acceleratorKeyStroke,
				final String toolTipText, final char mnemonic,final String disabledToolTipText) {
			this.appliesToTree = appliesToTree;
			this.menuText = menuText;
			this.addToTail = addToTail;
			this.anAction = actionListener;
			this.pml = pml;
			this.icon = icon == null ? MmsIcons.getWideBlankIcon():icon;
			this.acceleratorKeyStroke = acceleratorKeyStroke;
			this.toolTipText = toolTipText;
			this.mnemonic = mnemonic;
			this.disabledToolTipText = disabledToolTipText;
			registerKeyboardAction(table);
			if (!Basics.equals(menuText, EDIT_MENU_TEXT)) {
				popupMenuItems.add(this);
			}
			resetPopupMenu();
		}

		public PopupMenuItem(final String menuText, final boolean addToTail,
				final ActionListener actionListener,
				final PopupMenuListener pml, final boolean appliesToTree,
				final Icon icon, final KeyStroke acceleratorKeyStroke,
				final String toolTipText,final String disabledToolTipText) {
			this(menuText, addToTail, actionListener, pml, appliesToTree, icon,
					acceleratorKeyStroke, toolTipText, menuText != null && menuText.length()>0? menuText.charAt(0):'\0',disabledToolTipText);
		}

		public PopupMenuItem(final String menuText, final boolean addToTail,
				final ActionListener actionListener,
				final PopupMenuListener pml, final boolean appliesToTree,
				final KeyStroke acceleratorKeyStroke, final String toolTipText,final String disabledToolTipText) {
			this(menuText, addToTail, actionListener, pml, appliesToTree, null,
					acceleratorKeyStroke, toolTipText,disabledToolTipText);
		}

		public void setCreateMenu(final boolean ok) {
			createAMenu = ok;
		}

		public void setCheckBoxMenuItem(final Boolean ok) {
			isCheckBoxMenuItem = ok;
		}

		
		private boolean create(final JTree tree, final JPopupMenu jpm,
				final boolean atTail) {
			if (atTail == this.addToTail) {
				if (menuText != null) {
					if (createAMenu) {
						menuItem = new JMenu(menuText);
						menuItem.setIcon(icon);
					} else if (isCheckBoxMenuItem != null && isCheckBoxMenuItem) {
						menuItem = new JCheckBoxMenuItem(menuText, icon,
								false);

					} else {

						menuItem = new JMenuItem(menuText);
						menuItem.setIcon(icon);
					}
					disabledExplainer=new DisabledExplainer(menuItem);
					if (!(menuItem instanceof JMenu)) {
						if (tree == null) {
							menuItem.addActionListener(anAction);
						} else {
							SwingBasics.echoAction(tree, menuItem, anAction,
									acceleratorKeyStroke, mnemonic);
						}
						if (acceleratorKeyStroke != null) {
							menuItem.setAccelerator(acceleratorKeyStroke);
						}
					}
					menuItem.setToolTipText(toolTipText);
					setEnabled(enabled);
					if (mnemonic != '\0') {
						menuItem.setMnemonic(mnemonic);
					}
					jpm.add(menuItem);
					jpm.insert(menuItem, 0);
				} else {
					jpm.addSeparator();
				}
				return true;
			}
			return false;
		}

		public void setSelectedText(final String selectCountPrefix,
				final String selectCountSuffix) {
			final int n = getTableModel().selectedDataRowCount;
			if (n > 0) {
				menuItem.setText(selectCountPrefix
						+ " "
						+ n
						+ " "
						+ (selectCountSuffix == null ? (n > 1 ? key
								: singularKey) : selectCountSuffix));
			}
		}

		public void set(final boolean disabled,
				final int maxRequiredSelections,
				final boolean childMustBeEditable,
				final String selectCountPrefix, final String selectCountSuffix,
				final boolean allowEmptyLastRowSelection) {
			if (maxRequiredSelections < 0) {
				System.err.println("-1 for " + menuText);
			}
			if (menuItem != null) {
				if (disabled) {
					setEnabled(!disabled);
				} else {
					setEnabled(areSelectedRowsOk(maxRequiredSelections,
							childMustBeEditable, allowEmptyLastRowSelection), "", disabledMsg);
				}

				if (!Basics.isEmpty(selectCountPrefix)
						|| !Basics.isEmpty(selectCountSuffix)) {
					setSelectedText(selectCountPrefix, selectCountSuffix);
				}
			}
		}

		public boolean isFirstSelectedRowEditable() {
			return areSelectedRowsOk(1, true, true);
		}
		public boolean areSelectedRowsEditable() {
			return areSelectedRowsOk(Integer.MAX_VALUE, true, true);
		}

		String disabledMsg;
		private boolean areSelectedRowsOk(final int max,
				final boolean mustBeEditable,
				final boolean allowEmptyLastRowSelection) {
			disabledMsg=null;
			final PersonalizableTableModel tm = getTableModel().getModelShowing();
			String msg;
			if (max == 0) {
				msg=DisabledExplainer.startComplaint("unselect all items.").toString();
			} else {
				msg=DisabledExplainer.getSelectionComplaint(mustBeEditable?"editable":null);
			}
			if (!mustBeEditable) {
				int selected = 0;
				if (allowEmptyLastRowSelection
						|| !showingEmptyLastRowForCreating()) {
					selected = tm.table.getSelectedRowCount();
				} else {
					final int[] sdi = tm.getSelectedDataIndexes();
					if (sdi != null) {
						int n = tm.getRowCountMinusEmptyLastRowForCreating();
						for (int i : sdi) {
							if (i != n) {
								selected++;
							}
						}
					}
				}
				if (max == 0) {
					if (selected ==0){
						return true;
					} else {
						disabledMsg=msg;
						return false;
					}
				}
				if (max == -1) {
					return true;
				}
				if (selected < 1) {
					disabledMsg=msg;
					return false;
				}
				if (selected <= max) {
					return true;
				}
				if (max > 1){
					disabledMsg=msg;
				}
				return false;
			}
			final ArrayList<Row> rows = tm.getSelectedRowsInDescendingOrder();
			int editableRows = 0;
			if (!Basics.isEmpty(rows)) {
				for (final Row row : rows) {
					if (tm.isEditable(row)) {
						editableRows++;
					}
				}
			}
			// must be editable *AND*
			final boolean ok=max == -1 ? editableRows > 0 : editableRows > 0
					&& editableRows <= max;

			if (!ok) {
				disabledMsg=msg;
			}
			return ok;
		}
	}

	public void addMenuSeparator(final boolean addToTail) {
		new PopupMenuItem(addToTail);
	}

	int invokerDefinedBodyMenuItems = 0, invokerDefinedHeaderMenuItems = 0;

	private void addMenuItem(final boolean atTail, final PopupMenuItem pmi) {
		if (pmi.create(null, popup, atTail)) {
			invokerDefinedBodyMenuItems++;
		}
	}

	void addActions() {
		final ArrayList<PopupMenuItem> list = new ArrayList<PopupMenuItem>(
				popupMenuItems);
		final int type = getModelType();
		if (type == TYPE_GROUPED) {
			list
					.addAll(((GroupedDataSource) dataSource).ungroupedModel.popupMenuItems);
		} else if (type == TYPE_GROUP_SEIVED) {
			list.addAll(groupedDataSource.ungroupedModel.popupMenuItems);
		}
		if (list.size() > 0) {
			for (final Iterator it = list.iterator(); it.hasNext();) {
				final PopupMenuItem pmi = (PopupMenuItem) it.next();
				pmi.registerKeyboardAction(table);
			}
		}
	}

	private void cloneMenuItems() {
		final int type = getModelType();
		if (type != TYPE_UNGROUPED) {
			final Iterator<PopupMenuItem> it;
			if (type == TYPE_GROUPED) {
				it = ((GroupedDataSource) dataSource).ungroupedModel.popupMenuItems
						.iterator();
			} else {
				it = groupedDataSource.ungroupedModel.popupMenuItems.iterator();
			}

			while (it.hasNext()) {
				final PopupMenuItem pmi = it.next();
				popupMenuItems.add(new PopupMenuItem(pmi));
			}
		}
	}

	private boolean hasCustomTail() {
		for (final PopupMenuItem pmi : popupMenuItems) {
			if (pmi.addToTail) {
				return true;
			}
		}
		return false;
	}

	private void addMenuItems(final boolean atTail) {
		for (final PopupMenuItem pmi : popupMenuItems) {
			addMenuItem(atTail, pmi);	
		}
	}

	public void setCustomGrouping(final String menuText) {
		if (menuText != null) {
			setProperty(PROPERTY_CUSTOM_GROUP, menuText);
		} else {
			removeProperty(PROPERTY_CUSTOM_GROUP);
		}
	}

	boolean hasCustomGroupView() {
		final String menuText = (String) getProperty(PROPERTY_CUSTOM_GROUP,
				null);
		if (!Basics.isEmpty(menuText)) {
			for (final Iterator it = popupMenuItems.iterator(); it.hasNext();) {
				final PopupMenuItem pmi = (PopupMenuItem) it.next();
				if (pmi.menuText.equals(menuText)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean doCustomGroupView() {
		final String menuText = (String) getProperty(PROPERTY_CUSTOM_GROUP,
				null);
		if (!Basics.isEmpty(menuText)) {
			for (final Iterator it = popupMenuItems.iterator(); it.hasNext();) {
				final PopupMenuItem pmi = (PopupMenuItem) it.next();
				if (pmi.menuText.equals(menuText)) {
					pmi.anAction.actionPerformed(null);
					return true;
				}
			}
		}
		return false;
	}

	Collection<PopupMenuItem> addMenuItemsToTree(final JTree tree, final boolean atTail,
			final JPopupMenu jpm) {
		final Collection<PopupMenuItem> c = new ArrayList<PopupMenuItem>();

		for (final Iterator it = popupMenuItems.iterator(); it.hasNext();) {
			final PopupMenuItem pmi = (PopupMenuItem) it.next();
			if (pmi.appliesToTree) {
				final PopupMenuItem newPmi = new PopupMenuItem(pmi);
				if (newPmi.create(tree, jpm, atTail)) {
					c.add(newPmi);
				}
			}

		}
		return c;
	}

	static boolean supportIgnoreInvalid = true;

	public static void setSupportIgnoreInvalid(boolean supported) {
		supportIgnoreInvalid = supported;
	}

	TabImporter currentlyActiveTabImporter;

	public TabImporter getCurrentlyActiveTabImporter() {
		return currentlyActiveTabImporter;
	}

	public boolean isTabImportOccuring() {
		return currentlyActiveTabImporter != null;
	}

	public Row getFirstSelectedRow(final boolean respectGrouping) {
		final Row row;
		if (respectGrouping && getModelType() == TYPE_UNGROUPED
				&& hiddenByGrouping()) {
			row = groupSeivedModel.getFirstSelectedRow(false);
		} else {
			final int idx = getFirstSelectedRowIdx();
			if (idx >= 0) {
				row = getRowAtVisualIndex(idx);
			} else {
				row = null;
			}
		}
		return row;
	}

	public int getFirstSelectedRowIdx() {
		if (table != null && table.getRowSelectionAllowed()) {
			final int[] n = table.getSelectedRows();
			if (n != null) {
				for (int i = 0; i < n.length; i++) {
					if (!showFilterUI
							|| (n[i] != table.ROW_IDX_FILTER_VALUE && n[i] != table.ROW_IDX_FILTER_OP)) {
						return n[i];
					}
				}
			}
		}
		return -1;
	}

	public Class getColumnClass(final int modelColumnIndex) {
		return metaRow.getClass(getDataColumnIndex(modelColumnIndex));
	}

	int clickedVisualColumnIndex, clickedModelColumnIndex,
			clickedDataColumnIndex, clickedVisualRowIndex;
	TableColumn clickedColumn;
	Point clicked;

	Rectangle getClickedRect() {
		Rectangle r = null;
		if (clickedVisualRowIndex >= 0 && clickedVisualColumnIndex >= 0) {
			r = table.getCellRect(clickedVisualRowIndex,
					clickedVisualColumnIndex, true);
		}
		return r;
	}

	private Rectangle getKeyboardRect() {
		Rectangle r = null;
		final int vc = SwingBasics.getVisualIndexFromModelIndex(table,
				focusModelColumnIndex);
		if (focusVisualRowIndex >= 0 && vc >= 0) {
			r = table.getCellRect(focusVisualRowIndex, vc, true);
		}
		return r;
	}

	public boolean showDisabledText(final Row row, final boolean beep) {
		return showDisabledText(getDisabledText(row), beep);
	}

	public void showDisabledText(final GroupedDataSource.Node node,
			final String txt, final boolean beep) {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			groupedDataSource.showDisabledText(node, txt, beep);
		}
	}

	public void showText(final GroupedDataSource.Node node,
			final String txt, final boolean beep) {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			groupedDataSource.showText(node, txt, beep);
		}
	}

	public boolean showDisabledText(final String txt, final boolean beep) {
		return showDisabledText(txt, beep, true);
	}

	public boolean showDisabledText(final String txt, final boolean beep,
			final boolean showCloseButton) {
		return showTextInPopupWindow(txt, beep, showCloseButton, false, null,
				null, true);
	}

	public void showTextAtFocusCell(final String text) {
		Rectangle r = getKeyboardRect();
		if (r == null) {
			r = getClickedRect();
		}
		if (r != null) {
			SwingBasics.adjustToNorthWestIfNotVisible(table,r);
			final int wOffset = r.x, hOffset = r.y + r.height;
			final String priorText=table.getToolTipText();
			table.setToolTipText(text);
			ToolTipOnDemand.getSingleton().show(table, false, wOffset, hOffset);
			table.setToolTipText(priorText);
		}
	}

	public void showAdvice(final Row row, final int dataColumnIndex){
		final int vi=getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
		final Rectangle r=table.getCellRect(getVisualIndexOfRow(row), vi, true);
		SwingBasics.adjustToNorthWestIfNotVisible(table,r);
		final int wOffset = r.x+r.width, hOffset = r.y - (2*r.height);
		final String priorText=table.getToolTipText();
		final CellAdvice ca = new CellAdvice();
		row.setAdvice(dataColumnIndex, ca);
		table.setToolTipText(ca.toolTip);
		ToolTipOnDemand.getSingleton().show(table, false, wOffset, hOffset);
		table.setToolTipText(priorText);

	}
	public boolean showTextForErrorAdvice() {
		final CellAdvice cellAdvice=new CellAdvice();
		for (int visualRowIndex=getRowCount()-1;visualRowIndex>=0;visualRowIndex--){
			for (int visualColumnIndex=0;visualColumnIndex<getColumnCount();visualColumnIndex++){
				final Row row = getRowAtVisualIndex(visualRowIndex);
				final int dataColumnIndex=getDataColumnIndexFromVisualIndex(visualColumnIndex);
				if (row.setAdvice(
						dataColumnIndex, cellAdvice)){
					if (cellAdvice.type==CellAdvice.TYPE_ERROR){
						final String priorText=table.getToolTipText();
						final int vi=visualColumnIndex;
						SwingUtilities.invokeLater(new Runnable(){

							
							public void run() {
								final Rectangle r=table.getCellRect(getVisualIndexOfRow(row), vi, true);
								SwingBasics.adjustToNorthWestIfNotVisible(table,r);
								final int wOffset = r.x+r.width, hOffset = r.y + r.height;
										table.setToolTipText(cellAdvice.toolTip);
								ToolTipOnDemand.getSingleton().show(table, false, wOffset, hOffset);
								table.setToolTipText(priorText);
							}
						}); 
						return true;
					}
				}
			}
		}
		return false;
	}

	private void showCurrentSort(final int dataColumnIndex) {
		final boolean wasAlphabetic = !hasCustomSortSequence(dataColumnIndex);
		if (wasAlphabetic || !isSorted(dataColumnIndex)) {
			showTextInPopupWindow(getCurrentSort(dataColumnIndex, false),
					false, false, true, null, null, false);
		} else {
			final JPanel jp = new JPanel(new GridLayout(2, 1, 0, 3));
			final String h = "See custom sequence";
			jp.add(SwingBasics.getButton(h, 's', new ActionListener() {

				public void actionPerformed(final ActionEvent e) {
					alterSortSequence(dataColumnIndex, table);
					ToolTipOnDemand.getSingleton().hideTipWindow();

				}
			}, Basics.toHtmlUncentered(h, "<b>"
					+ getColumnLabel(dataColumnIndex)
					+ "</b> has a custom sort sequence.")));
			showTextInPopupWindow(getCurrentSort(dataColumnIndex, true), false,
					true, true, 0, 25, false, jp);
		}
	}

	private boolean showTextInPopupWindow(final String txt, final boolean beep,
			final boolean showCloseButton, final boolean header,
			final Integer wOffset, final Integer hOffset,
			boolean restoreToolTip, final JPanel extra) {
		return showTextInPopupWindow(txt, beep, showCloseButton, header,
				wOffset, hOffset, true, restoreToolTip, extra);
	}

	private boolean showTextInPopupWindow(final String txt, final boolean beep,
			final boolean showCloseButton, final boolean header,
			final Integer wOffset, final Integer hOffset, boolean restoreToolTip) {
		return showTextInPopupWindow(txt, beep, showCloseButton, header,
				wOffset, hOffset, true, restoreToolTip, null);
	}
	public boolean left2Right=false;
	boolean showTextInPopupWindow(final String txt, final boolean beep,
			final boolean showCloseButton, final boolean header,
			Integer wOffset, Integer hOffset,
			final boolean tryMouseRectFirst,
			final boolean restoreToolTipAfterShowing, final JPanel extraStuff) {
		final PersonalizableTableModel ptm;
		if (groupSeivedModel != null) {
			ptm = groupSeivedModel;
		} else {
			ptm = this;
		}
		final boolean shown;
		if (isUserPicking && !Basics.isEmpty(txt)) {
			shown = true;
			final Rectangle rec;
			final JComponent jc;
			if (header) {
				if (ptm.table == null){
					rec=null;
					jc=null;
				} else{
					final JTableHeader h = ptm.table.getTableHeader();
					rec = h.getHeaderRect(clickedVisualColumnIndex);
					jc = h;	
					if (wOffset==null){
						wOffset=rec.width;
					}
					if (hOffset==null){
						hOffset=0-rec.height;
					}
				}
			} else {
				if (!tryMouseRectFirst) {
					final Rectangle rec2 = ptm.getKeyboardRect();
					if (rec2 == null) {
						rec = ptm.getClickedRect();
					} else {
						rec = rec2;
					}

				} else {
					final Rectangle rec2 = ptm.getClickedRect();
					if (rec2 == null) {
						rec = ptm.getKeyboardRect();
					} else {
						rec = rec2;
					}
				}
				jc = ptm.table;
			}
			if (rec != null) {
				final String oldToolTipText = restoreToolTipAfterShowing ? jc
						.getToolTipText() : null;
				jc.setToolTipText(txt);
				final Integer _wOffset=wOffset, _hOffset=hOffset;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						final ToolTipOnDemand ttod = ToolTipOnDemand
								.getSingleton();
						if (left2Right) {
							ttod.left2Right=true;
						}
						final int widthOffset = rec.x
								+ (_wOffset != null ? _wOffset : rec.width), heightOffset = rec.y
								+ (_hOffset != null ? _hOffset : rec.height);
						if (extraStuff == null) {
							ttod.show(jc, !header, widthOffset, heightOffset,
									showCloseButton);
						} else {
							ttod.show(jc, !header, widthOffset, heightOffset,
									extraStuff,txt);
						}

						if (restoreToolTipAfterShowing) {
							jc.setToolTipText(oldToolTipText);
						}
						if (beep) {
							Toolkit.getDefaultToolkit().beep();
						}
					}
				});
			}
		} else {
			shown = false;
		}
		return shown;
	}

	private void resolveRowColumnIndexes(final MouseEvent event) {
		clicked = event.getPoint();
		clickedVisualColumnIndex = table.tcm.getColumnIndexAtX(event.getX());
		if (clickedVisualColumnIndex < 0) {
			return;
		}
		clickedColumn = table.tcm.getColumn(clickedVisualColumnIndex);
		clickedModelColumnIndex = clickedColumn.getModelIndex();
		if (clickedModelColumnIndex < 0
				|| clickedModelColumnIndex >= modelColumnIdentifiers.size()) {
			clickedColumn = table.tcm.getColumn(0);
			clickedVisualColumnIndex = 0;
			clickedModelColumnIndex = clickedColumn.getModelIndex();
		}
		clickedDataColumnIndex = getDataColumnIndex(clickedModelColumnIndex);
		clickedVisualRowIndex = table.rowAtPoint(clicked);
	}

	public int getMouseDataColumnIndex() {
		return clickedDataColumnIndex;
	}

	PopupRow popupRow = null;
	int selectedDataRowCount = 0;

	public static Window currentWindowDuringDoubleClick = null;
	public void doubleClick(final Row row) {
		currentWindowDuringDoubleClick = SwingUtilities
				.getWindowAncestor(getTearAwayComponent());
		handleDoubleClick(row);
		currentWindowDuringDoubleClick = null;
	}

	public static boolean isDoubleClickEvent = false;
	public boolean isTreeOn = true, isFindOn=false;;
	public boolean tableHadTreePreviously = false;
	public boolean isCreateTabsOn = true;
	public boolean canSortArrangeColumns = true;
	public boolean canHavePickMenu = true;
	public boolean isDependentColumnNotEditable = false;
	public static boolean allowColumnHidingByKeyboardShortcut = false;
	
	private boolean isRightClickDoingThePoppingUp=false;
	void manageBody(final boolean released) { // int selectedStart, int
		// selectedEnd)
		if (popupRow != null) {
			popupRow.dlg.dispose();
			popupRow = null;
		}
		table.cellHighlighter.reset();
		if (event.isPopupTrigger()) {
			isRightClickDoingThePoppingUp=true;
			popupMenu(event.getComponent(), event.getX(), event.getY());
			isRightClickDoingThePoppingUp=false;
		}
		// The check !isDoubleClickEvent is added to show the instrument alert only once in MAC
		if (event.getClickCount() == 2
				&& !SwingUtilities.isRightMouseButton(event) && !isDoubleClickEvent) {
			isDoubleClickEvent = true;
			if (getUngroupedModel().isInTreeBuildingMode) {
				getUngroupedModel().adjustTree(clickedDataColumnIndex, true);
			} else {
				if (!editInPlace
						|| !isEditable(false, focusVisualRowIndex,
								focusModelColumnIndex, true)) {
					final Row row = getRowAtVisualIndex(focusVisualRowIndex);
					if (editInPlace && editMenuItem != null || 
							(getUngroupedModel().doubleClickDataColumnIndexes!=null && 
									Basics.contains(getUngroupedModel().doubleClickDataColumnIndexes, clickedDataColumnIndex))) {
						final Collection<String> c = getUngroupedModel().isPickList ? null
								: getReadOnlyExplanation(
										clickedVisualRowIndex,
										clickedVisualColumnIndex);
						if (!Basics.isEmpty(c)) {
							final String msg = Basics.toHtmlUncentered(
									Basics.concat("<font color='red'>You can't enter information here because...</font>", Basics.toUlHtml(c)));
							JPanel jp = null;
							if (editMenuItem != null){
							jp=new JPanel(new GridLayout(2, 1,
									0, 3));
							final boolean b = areAnyDisplayedColumnsEditable(row);
							
								final String h = b ? EDIT_MENU_TEXT
									: VIEW_MENU_TEXT;
							jp
									.add(SwingBasics
											.getButton(
													h,
													h.charAt(0),
													new ActionListener() {

														public void actionPerformed(
																final ActionEvent e) {
															doubleClick(row);
														}
													},
													Basics
															.toHtmlUncentered(
																	h,
																	"Pop up a window for the current row")));
							}
							showTextInPopupWindow(msg, true, false, false, -15,
									null, true, jp);
						} else {
							doubleClick(row);
						}
					} else {
						doubleClick(row);
					}
				}
			}
		}else if(event.getClickCount() < 2
				&& !SwingUtilities.isRightMouseButton(event)){
			isDoubleClickEvent = false;
		}
	}

	ArrayList<Integer> columnsNotGroupable = new ArrayList<Integer>();
	public ArrayList<Integer> getColumnsNotGroupable() {
		return columnsNotGroupable;
	}

	public void setColumnsNotGroupable(ArrayList<Integer> columnsNotGroupable) {
		this.columnsNotGroupable = columnsNotGroupable;
	}

	void adjustTree(final int dataColumnIndex, final boolean warnIfRemoving) {
		if (columnsNotGroupable.contains(dataColumnIndex)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (columnFinder != null) {
						columnFinder.setToolTipText("This column cannot be used for grouping");
						ToolTipOnDemand.getSingleton().show(columnFinder, true,
							170, 25, true);
					}
					else if (groupedDataSource != null && groupedDataSource.tree != null) {
						groupedDataSource.tree.setToolTipText("This column cannot be used for grouping");
						groupedDataSource.showToolTipLater(groupedDataSource.getRoot(), false, false, true);
					}
				} 
			});
			return;
		}
		
		final TreePath [] selectedTreePaths=getSelectedTreePaths();
		final SortInfo si = findSortInfo(dataColumnIndex);
		final String label;
		if (si != null) {
			label = null;
			if (!warnIfRemoving
					|| PopupBasics.ask(getWindow(), "<html>Remove the column <u><i>"
							+ getColumnAbbreviation(dataColumnIndex)
							+ "</i></u> from the tree?</html>")) {
				removeSort(dataColumnIndex);
				ungroupIfNecessary();
				showOneMomentDisplay();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						final FocusFreeze ff = new FocusFreeze();
						sort();
						group();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								SwingBasics.expandFistLeaf(getTree());
								hideOneMomentDisplay();
								ff.restorePrevValue();
							}
						});

					}
				});
			}
		} else {
			label = getColumnAbbreviation(dataColumnIndex);
			sort(dataColumnIndex, true);			
			ungroupIfNecessary();
			showOneMomentDisplay();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					sort();
					group();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SwingBasics.expandFistLeaf(getTree());
							hideOneMomentDisplay();
						}
					});

				}
			});
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final String s = getCurrentTreeHtml();

				final String txt;
				if (Basics.isEmpty(s)) {
					txt = "The tree has no levels yet";
				} else {
					txt = Basics.toHtmlUncentered(
							"Columns for current tree levels", s);
				}
				if (columnFinder == null) {
					reselectTreeLater(selectedTreePaths);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
					groupedDataSource.tree.setToolTipText(txt);
					groupedDataSource.showToolTipLater(groupedDataSource.getRoot(), false, false, true);
						}
					});
					return;
				}

				columnFinder.setToolTipText(txt);
				if (label != null) {
					columnFinder.setSelectedItem(label);
				} else {
					AutoComplete.notifyCompletionFoundIfNecessary(columnFinder,
							columnFinderFoundListener);
				}
				if (!FocusFreeze.isFrozen()) {
					columnFinder.requestFocus();
				}
				if (columnFinder instanceof AutoComplete) {
					((AutoComplete) columnFinder).selectAll();
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SwingBasics.repack(buildingTreeWindow);
						ToolTipOnDemand.getSingleton().show(columnFinder, true,
								170, 25, true);
					}
				});
			}
		});
	}

	boolean treeDoubleClickUsesUngroupedTable = false;

	public void setTreeDoubleClickUsesUngroupedTable(final boolean ok) {
		treeDoubleClickUsesUngroupedTable = ok;
	}

	void handleDoubleClick(final Row row) {
		handleDoubleClick(row, false);
	}
	void handleDoubleClick(final Row row, final boolean wasTreeDoubleClicked) {
		if (getUngroupedModel().doubleClickAction!=null){
			getUngroupedModel().doubleClickAction.doClick();
		} else if (row != null) {
			if (!row.isActive()) {
				return;
			}			
			final TableCellContext context = new TableCellContext(this,
					clickedDataColumnIndex, row, wasTreeDoubleClicked);
			rememberPriorTreeValues(row);

			if (dataSource.handleDoubleClick(context)) {
				if (pickChecker != null) {
					final Collection<Row>c=getSelectedRowsInDescendingOrder();
					if (c.size()>0){
						pickChecker.checkPick(getModelShowing().table,
							c, true, false);
					}
				}
				popupRow = new PopupRow(row);
			}
			reSyncViewsToChanges(context.row, -1);
		}
	}
	
	public JButton doubleClickAction=null;

	boolean _handleDoubleClick(final Row row) {
		if (row != null) {
			final TableCellContext context = new TableCellContext(this,
					clickedDataColumnIndex, row);
			return dataSource.handleDoubleClick(context);

		}
		return false;
	}

	public Row getMouseFocusRow() {
		return getDataRowAtVisualIndex(clickedVisualRowIndex);
	}

	public Row getDataRowAtVisualIndex(final int visualRowIndex) {
		final int r = getFilteredDataRowIndex(visualRowIndex);
		if (r > -1) {
			return getRowAtVisualIndex(visualRowIndex);
		}
		return null;
	}

	public int getViewChangeCount() {
		if (getModelType() == TYPE_UNGROUPED && hiddenByGrouping()) {
			layoutChangeCnt += groupSeivedModel.layoutChangeCnt;

		}
		// System.out.println("Table " + hashCode() + " ("+ getModelType()
		// +")has " + layoutChanges + " layout changes ");
		return layoutChangeCnt;
	}

	boolean pressedPopup;

	public String setKey(final String key) {
		final String prev = this.key;
		this.key = key;
		if (key.endsWith("(s)")) {
			singularKey = key.substring(0, key.length() - 3);
		} else {
			singularKey = key;
		}
		return prev;
	}

	public String toString() {
		return key +" "+super.toString();
	}

	public void resetLayoutChangeCount() {
		if (getModelType() == TYPE_UNGROUPED && hiddenByGrouping()) {
			groupSeivedModel.layoutChangeCnt = 0;

		}
		// System.out.println(getKey()+" reset layout changes");
		layoutChangeCnt = 0;
	}

	private int[] selectedModelIndexes = new int[0];
	boolean isMouseEventWithEmptyLabel;

	public MouseEvent event;

	void handleMouseEvent(final MouseEvent event, final boolean released) {
		this.event = event;
		resolveRowColumnIndexes(event);
		if (event.getSource().equals(table.header)) {
			manageHeader(released);
		} else {
			if (!showFilterUI || clickedVisualRowIndex > 1) {
				manageBody(released);
			} else {
				if (event.isPopupTrigger()) {
					popupMenu(event.getComponent(), event.getX(), event.getY());
					if (!released) { // appears popup menu was on press not
						// release
						pressedPopup = true;
					}
				}

			}
		}
	}


	private void handleMovedColumn() {
		table.maybeColumnsAreMoving = false;
		if (table.columnListener.moving) {
            if (getUngroupedModel().columnMovedListener!=null) {
            	getUngroupedModel().columnMovedListener.actionPerformed(table,0,true);
            }
		}
	}
	
	private Collection<Integer>doubleClickDataColumnIndexes;
	public void addDoubleClickAllowedValueColumn(final int dataColumnIndex){
		if (doubleClickDataColumnIndexes == null){
			doubleClickDataColumnIndexes=new HashSet<Integer>();
		}
		
		doubleClickDataColumnIndexes.add(dataColumnIndex);
	}
	
	
	private void manageHeader(final boolean released) {
		if (clickedVisualColumnIndex < 0) {
			if (released) {
				handleMovedColumn();
			}
			return;
		}
		if (released) {
			if (!pressedPopup) {
				// No check needed of FocusFreeze.isFrozen
				table.requestFocus();
				table.cellHighlighter.selectColumn(clickedModelColumnIndex,
						clickedVisualColumnIndex, event);
				
			} else {
				pressedPopup = false;
			}
		}
		final PersonalizableTableModel utm=getUngroupedModel();
		if (utm.columnRenderer != null && utm.columnRenderer.handleMouseEvent(getUngroupedModel(),table.header, clickedDataColumnIndex, clickedVisualColumnIndex, event, released)){
			if (released){
				handleMovedColumn();
			}
			return;
		}
		if (event.getClickCount() == 2 && released) {
			if (utm.isInTreeBuildingMode) {
				utm.adjustTree(clickedDataColumnIndex, true);
			} else {
				final boolean sort=canSort(), changeEntire=getEditAllAnomaly()==null ;
				if (sort ) {
					showColumnOptions(getColumnAbbreviation(clickedDataColumnIndex), clickedDataColumnIndex, changeEntire);
				} else if (changeEntire){
					changeEntireColumn.doClick();
					if (released){
						handleMovedColumn();
					}
					return;
				}
			}
		}
		if (!released) {
			table.columnListener.reset();
		} else {
			handleMovedColumn();
			if (table.columnListener.hasChanged()) {
				table.cellHighlighter.adjustSelectedColumnsAfterMove();
				notifyViewChanged();
				return;
			}
		}
		if (event.isPopupTrigger()) {
			popupMenu(event.getComponent(), event.getX(), event.getY());
			if (!released) { // appears popup menu was on press not release
				pressedPopup = true;
			}
		} 

	}


	public void popupMenu(final Component component) {
		
		qualifyActions();
		if (!getUngroupedModel().suppressRightClick && (popupMenuButton==null || popupMenuButton.getButton().isEnabled())){
			notifyPopupMenuListeners();
			SwingBasics.show(popup,component);
		}
	}
	
	public void popupMenu(final Component component,int x ,int y) {
		
		qualifyActions();
		if (!getUngroupedModel().suppressRightClick && (popupMenuButton==null || popupMenuButton.getButton().isEnabled())){	
			notifyPopupMenuListeners();
			ToolTipOnDemand.getSingleton().hideTipWindow();
			popup.show(component,x,y);
		}
	}
	

	public int getSelectedRowCount() {
		return table.getSelectedRowCount();
	}

	private String getDeletableAnomalies(final int[] selected) {
		int unDeletable = 0;
		String value=null;
		final Collection<String> c = new HashSet<String>();
		if (Basics.isEmpty(selected)){
			value=MSG_NOTHING_IS_SELECTED;
		}
		if (selected.length==1 && selected[0]==getRowCountMinusEmptyLastRowForCreating()){
			value="Can not delete the empty last row";
		}else if (getUngroupedModel().dataSource.isDeletable()) {
			for (int i = 0; i < selected.length; i++) {
				final Row row = getRowAtVisualIndex(selected[i]);
				if (row != null
						&& (!row.isDeletable() || getUngroupedModel().deletionHasBeenResolved
								.contains(row))) {
					if (row instanceof Row.SelfExplanatory) {
						if (((Row.SelfExplanatory) row)
								.getDeletableAnomalies(c)) {
							unDeletable++;
						}
						else if (!row.isDeletable()) {
							c.add("not deletable");
							unDeletable++;
						}
					} else {
						if (!row.isDeletable()) {
							c.add("not deletable");
							unDeletable++;
						}
					}
				}
			}
			if (unDeletable > 0){
				final StringBuilder preamble=new StringBuilder();
				if (unDeletable==1){
					preamble.append("This selection is:");
				} else {
					preamble.append(unDeletable);
					preamble.append(" selections are ");
				}
				preamble.append(Basics.toUlHtml(c));
				value=preamble.toString();
			}
		}
		return value;
	}

	
	private String editInPlaceIsFixed = "The edit-in-place state is fixed for this particular table.";
	private String entireTableIsReadOnly = "All of this particular table is read-only.  You can not modify it.";
	
	
	private void qualifyActions() {
		final int[] selected = table.getSelectedRows();
		final int rowsWithData=this.getDataRowCount(selected);
		
		final int rowCount = getRowCount();
		editInPlaceItem.setState(editInPlace);
		if (rowsWithData > 0) {
			hideDisabled.setEnabled(true,null,null);
			hideItem.setToolTipText(null);
		}
		else {
			hideDisabled.setEnabled(false,"Hide item",selectRowsAnomaly());
		}
		if (Basics.isEmpty(dataSource.getHiddenRows())) {
			unhideDisabled.setEnabled(false,"Unhide item",selectRowsAnomaly("hidden items"));
		}
		else {
			unhideDisabled.setEnabled(true,null,null);
			unhideItem.setToolTipText(null);
		}
		selectedDataRowCount = getSelectedDataRowCount(selected);
		
		
		if(moveDownDisabled != null) {//At lease one move menu item should be applicable at this context
			qualifyMoveAction.actionPerformed(new ActionEvent(table, -1, "popup"));
		}
		
		final boolean hasSortColumns = columnsToSort.size() > 0;
		initPopup();
		for (final Operation dmil:menuItemListeners){
			dmil.recompute(selected, rowsWithData, selectedDataRowCount);
		}
		getUngroupedModel().favoriteTrees.qualifyActions();
		getUngroupedModel().recentTrees.qualifyActions();
		restoreTreeMenu();
		if(sortNeeded) {
			refreshDisabled.setEnabled(true,null,null);
		}
		else {
			refreshDisabled.setEnabled(false,"Refresh sort",
					"This operation is ONLY enabled when all values of a column which is currently sorted, are changed");
		}
		if (dataSource.isFilterable()) {
			applyFilterItem.setEnabled(hasSeeOnlySettings()
					|| (hasFilterOps() && table.onFilteringRow()));
			if (showFilterUI) {
				showFilterItem.setText("Hide filter UI");
				showFilterItem.setMnemonic('h');
			} else {
				showFilterItem.setText("Show filter rows");
				showFilterItem.setMnemonic('s');
			}
		}
		fitWindowItem
				.setText(table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF ? "Fit window"
						: "Do not fit window");
		final String sortableColumnDescription=qualifySorting();
		qualifyQuerying(sortableColumnDescription);
		qualifyColumnOperationsOtherThanSorting();
		if (isTreeOn) {
			final boolean canGroup;
			final String groupColumnText;
			sortBasedTreeItem.setEnabled(hasSortColumns);
			sortBasedTreeItem.setToolTipText(hasSortColumns?"<html><b>Current sort order</b>:  "+getCurrentSort():"No current sort order!");
			if (!Basics.isEmpty(sortableColumnDescription)) {
				canGroup = true;
				groupColumnText = sortableColumnDescription;
			} else {
				canGroup = hasSortColumns;
				if (hasSortColumns) {
					groupColumnText = getCurrentSort();
				} else {
					groupColumnText = null;
				}
			}
			if (canGroup) {
				final String txt = getUngroupedModel().getCurrentSort();
				final boolean checked = txt.equals(groupColumnText);
				newTreeItem.setState(checked);
				final boolean isTreeActive = groupedDataSource != null
						|| isCompanionTable;
				newTreeItem.setEnabled(!checked || !isTreeActive);
				newTreeItem.setText(checked ? "<html>"+groupColumnText+"</html>"
						: "<html><b>" + groupColumnText + "</b></html>");

			} else {
				newTreeItem.setText("selected column(s)");
				newTreeItem.setEnabled(false);
				newTreeItem.setState(false);
			}
			if ((getModelType() == TYPE_UNGROUPED)) {
				removeTreeDisabled.setEnabled(false,"Restore table","This operation is ONLY enabled if you have a tree view");
			} else {
				removeTreeDisabled.setEnabled(true,null,null);
			}			
		}
		pasteViewItem.setEnabled(clipBoardProperties != null);
		final boolean addable = getUngroupedModel().dataSource.isAddable(), creatable = dataSource
				.isCreatable()
				|| isCompanionModelWithCreatableTree();
		if (!readOnly) {

			if (addable || creatable) {
				final int n = dataSource.getMaximumCardinality();

				final boolean canChooseOrCreate = n == 1 || rowCount < n;
				String text = null;
				if (!canChooseOrCreate) {
					text = "A maximum of " + n + " \"" + key + "\" is allowed!";
				}
				if (creatable) {
					if (canEnterNewItem() && dataSource.checkCardinality(newDisabledButton, 1) && !getUngroupedModel().useCustomNewItemOnly) {
						// newItem.setEnabled(canChooseOrCreate);
						if (canChooseOrCreate) {
							text = VERB_NEW + " instance of \"" + key + "\" ";
						}
						newItem.setToolTipText(text);
					}
				}
				if (addable && canHavePickMenu) {
					if (dataSource.checkCardinality(addDisabledButton, 1)) {
						// chooseItemFromList.setEnabled(canChooseOrCreate);
						if (canChooseOrCreate) {
							text = getAddMenuText();
						}
						chooseItemFromList.setToolTipText(text);
					}
				}
			}
		}

		if (clipBoardKey != null) {
			pasteViewItem.setText("Paste view of " + clipBoardKey);
		}
		if(isMouseEventWithEmptyLabel) {
			printDisabled.setEnabled(false,"Print",selectRowsAnomaly());
		}else {
			printDisabled.setEnabled(true,null,null);
		}
		if (findFilterMenu != null) {
			if(!isMouseEventWithEmptyLabel) {
				findDisabled.setEnabled(true,null,null);
			}
			else {
				findDisabled.setEnabled(false,"Find","This operation is ONLY enabled if you first select item(s) in the tree");
			}
		}
		if(!isMouseEventWithEmptyLabel) {
			manageColumnsDisabled.setEnabled(true,null,null);
		}
		else {
			manageColumnsDisabled.setEnabled(false,"Manage Columns","This operation is ONLY enabled if you first select item(s) in the tree");
		}
		manageColumnsMenu.setEnabled(!isMouseEventWithEmptyLabel);
		if (editMenuItem != null && selectedDataRowCount > 0) {
			final int n = getSelectedAndEditableDataRowCount(selected);
			editMenuItem.setText(n > 0 ? EDIT_MENU_TEXT : VIEW_MENU_TEXT);
		}
		if (showAllColumnsDisabled==null){
			showAllColumnsDisabled=new DisabledExplainer(showAllColumnsItem);
		}
		if (getColumnCount()==metaRow.size()){
			
			showAllColumnsDisabled.setEnabled(false, null, "All columns are already showing.");
		} else {
			showAllColumnsDisabled.enable();
		}
	}
	
	public void qualifyMove() {
		int row[] = getTable().getSelectedRows();
		int rowCount = getTable().getRowCount();
		String downAnomaly=null, upAnomaly=null;
		if (ungroupedModel != null){
			downAnomaly=upAnomaly="You can not move items in the companion table that the tree navigates.";
		} else  if(readOnly){
			downAnomaly=upAnomaly=entireTableIsReadOnly;
		} else if(!dataSource.isMovable()){
			downAnomaly=upAnomaly="This particular table does not allow the moving of items";
		} else if (isInTreeBuildingMode()) {
			downAnomaly=upAnomaly="You must first complete building the navigation tree";
		}else if (rowCount == 0 || row==null||row.length == 0){
			downAnomaly=upAnomaly=selectRowsAnomaly();
		}else  {
			boolean isLastRowEditing = false;
			for (int i = 0; i < row.length; i++) {
				if (row[i] == rowCount - 1) {
					isLastRowEditing = true;
					break;
				}
			}
			int[] indexes = new int[row.length - 1];
			if (showingEmptyLastRowForCreating() && isLastRowEditing) {
				for (int i = 0; i < indexes.length; i++) {
					indexes[i] = row[i];
				}
			} else {
				indexes = row;
			}
			int[] selected = null;
			if (showFilterUI) {
				int n = getSelectedDataRowCount(indexes);
				selected = new int[n];
				int index = 0;
				for (int i = 0; i < indexes.length; i++) {
					if (indexes[i] > 1) {
						selected[index] = indexes[i] - 2;
						index++;
					}
				}
			} else {
				selected = indexes;
			}
			rowCount = getDataRowCount();

			if (selected == null || selected.length == 0) {
				downAnomaly=upAnomaly=selectRowsAnomaly();
			}

			for (int i = 0; i < selected.length; i++) {
				if (selected[i] == 0) {
					upAnomaly="You can move no further <b>up</b> in the table.";
				}
				if (selected[i] == rowCount - 1) {
					downAnomaly="You can move no further <b>down</b> in the table.";
				}
			}

		}
		moveDownDisabled.setEnabled(downAnomaly==null, null, downAnomaly);
		moveBottomDisabled.setEnabled(downAnomaly==null, null, downAnomaly);
		moveUpDisabled.setEnabled(upAnomaly==null, null, upAnomaly);
		moveTopDisabled.setEnabled(upAnomaly==null, null, upAnomaly);
	}
	
	
	
	static final String COLUMN_SELECTION_COMPLAINT = "This operation is ONLY enabled if <br>you first select a column";
	
	private String qualifySorting() {
		final String retVal;
		final boolean hasSortColumns = columnsToSort.size() > 0;
		selectedModelIndexes = table.cellHighlighter.getSelectedColumns();
		final int[] _selectedModelIndexes = getComparableSelectedModelColumns(true);
		final boolean userHasSelectedComparableColumns = _selectedModelIndexes.length > 0;
		int ascendingOptions = 0, descendingOptions = 0;
		if (userHasSelectedComparableColumns) {
			retVal = getColumnAbbreviationsForModelColumnIndexes(_selectedModelIndexes);
			final String txt=":  <i>"+Basics.trimIfTooBig(retVal)+ "</i></html>";
			boolean isSortable = false;
			int selectedAscending = 0, selectedDescending = 0;
			final StringBuilder selectedUnsortTxt = new StringBuilder();
			for (int i = 0; i < _selectedModelIndexes.length; i++) {
				if (isSortable(_selectedModelIndexes[i])) {
					final int dataColumnIndex = getDataColumnIndex(_selectedModelIndexes[i]);
					final String label = "\""
							+ getColumnAbbreviation(dataColumnIndex) + "\"";
					isSortable = true;
					boolean canAscend = true, canDescend = true, canUnsort = true;
					SortInfo si = findSortInfo(dataColumnIndex);
					if (si != null) {
						if (si.ascending) {
							canAscend = false;
							selectedAscending++;
						} else {
							canDescend = false;
							selectedDescending++;
						}
					} else {
						canUnsort = false;
					}
					if (canAscend) {
						ascendingOptions++;
					}
					if (canDescend) {
						descendingOptions++;
					}
					if (canUnsort) {
						if (selectedUnsortTxt.length() > 0) {
							selectedUnsortTxt.append(", ");
						}
						selectedUnsortTxt.append(label);
					}
				}
			}
			setSortSequenceMenuText(
					sortSequenceDisabled,
					_selectedModelIndexes.length == 1 ? getDataColumnIndex(_selectedModelIndexes[0])
							: -1);
			if (_selectedModelIndexes.length == 1) {
				final String t = getColumnAbbreviation(getDataColumnIndex(_selectedModelIndexes[0]));
				sortSequenceItem.setText(Basics.concat("<html>",
						MENU_TEXT_SEQUENCE, ":  <i>", t, "</i></html>"));
				sortSequenceDisabled.setEnabled(true, null, null);
			} else {
				sortSequenceItem.setText(MENU_TEXT_SEQUENCE);
				sortSequenceDisabled
						.setEnabled(
								false,
								MENU_TEXT_SEQUENCE,
								"You cannot customize this sort sequence OR you have not selected an item(s) to sort");
			}
			sortSequenceItem.setMnemonic('a');
			if (ascendingOptions > 0) {
				if (_selectedModelIndexes.length == 1 && hasSortColumns) {
					if (selectedUnsortTxt.length() > 0) {
						sortAscendingItem.setText("<html>"
								+ MENU_TEXT_SORT_ASCENDING + txt );
					} else {
						sortAscendingItem
								.setText("<html>Add to sort ascending"
										+ txt);
					}
				} else {
					sortAscendingItem.setText("<html>"
							+ MENU_TEXT_SORT_ASCENDING +txt);
				}
				sortAscendingDisabled.setEnabled(true, null, null);
			} else {
				sortAscendingDisabled
						.setEnabled(false, MENU_TEXT_SORT_ASCENDING,
								"This column(s) is currently sorted in Ascending order");
			}
			if (descendingOptions > 0) {
				if (_selectedModelIndexes.length == 1 && hasSortColumns) {
					if (selectedUnsortTxt.length() > 0) {
						sortDescendingItem.setText("<html>"
								+ MENU_TEXT_SORT_DESCENDING + txt);
					} else {
						sortDescendingItem
								.setText("<html>Add to sort descending"+txt);
					}
				} else {
					sortDescendingItem.setText("<html>"
							+ MENU_TEXT_SORT_DESCENDING + txt);
				}
				sortDescendingDisabled.setEnabled(true, null, null);
			} else {
				sortDescendingDisabled.setEnabled(false, MENU_TEXT_SORT_ASCENDING,
								"This column(s) is currently sorted in Descending order");
			}
			if (selectedUnsortTxt.length() > 0) {
				unsortItem.setText("<html>Unsort:  <i>"+Basics.trimIfTooBig(selectedUnsortTxt.toString())+ "</i></html>");
				unsortDisabled.setEnabled(true, null, null);
			} else {
				unsortDisabled
						.setEnabled(false, "Unsort",
								"This operation is ONLY available if the list is already sorted");
			}
		} else {
			retVal = "";
			sortAscendingDisabled.setEnabled(false, MENU_TEXT_SORT_ASCENDING, COLUMN_SELECTION_COMPLAINT);
			sortDescendingDisabled.setEnabled(false, MENU_TEXT_SORT_DESCENDING, COLUMN_SELECTION_COMPLAINT);			
			unsortDisabled.setEnabled(false, "Unsort", COLUMN_SELECTION_COMPLAINT);			
		}
		if (hasSortColumns) {
			unsortAllDisabled.setEnabled(true, null, null);
		} else {
			unsortAllDisabled
					.setEnabled(false, "Unsort All",
							"This operation is only available ONLY if you have sorted columns");
		}
		return retVal;
	}

	public static final Collection<String> columnsThatCanNotBeFrozen = new HashSet<String>();
	
	private void qualifyColumnOperationsOtherThanSorting() {
		final int[] _selectedModelIndexes = getSelectedModelColumns(true);
		if (_selectedModelIndexes.length > 0) {
			final String txt = ":  <i>"+Basics.trimIfTooBig(getColumnAbbreviationsForModelColumnIndexes(_selectedModelIndexes)) + "</i></html>";
			if (_selectedModelIndexes.length < table.tcm.getColumnCount()) {
				hideDisabler.setEnabled(true, null, null);
				hideColumnItem.setText("<html>"+MENU_TEXT_HIDE_COLUMNS + txt);
			} else {
				hideDisabler.setEnabled(false, MENU_TEXT_HIDE_COLUMNS,
						"At least one column must be showing.  You<br>have selected"+txt);
			}
			if (table.tcm.getColumnCount()==1){
				freezeDisabler.setEnabled(false, MENU_TEXT_FREEZE_AS_ROW_HEADER, "There is now only one \"unfrozen\" column ...<br>at least one column must not be a row header!");
			}else{
				final String unFrozenColumns = getUnFrozenColumnsList(_selectedModelIndexes); 
				if (unFrozenColumns.length() > 0) {
					freezeDisabler.setEnabled(false, MENU_TEXT_FREEZE_AS_ROW_HEADER, "Can not \"freeze\" column(s): " + unFrozenColumns);					
				}
				else {
				freezeDisabler.setEnabled(true, null, null);
			}
			}
			freezeColumnsItem.setText("<html>" +MENU_TEXT_FREEZE_AS_ROW_HEADER+ txt);
			shiftLeftDisabled.setEnabled(true, null, null);
			shiftLeftItem.setText("<html>"+MENU_TEXT_SHIFT_LEFT + txt);
			shiftRightDisabled.setEnabled(true, null, null);
			shiftRightItem.setText("<html>"+MENU_TEXT_SHIFT_RIGHT + txt);
			final Tabs _tabs = getUngroupedModel().tabs;
			if (_tabs == null) {
				if (_selectedModelIndexes.length == 1) {					
					tabItem.setEnabled(true);
					tabItem.setText("<html>" + MENU_TEXT_TAB + " for"+txt);
							
					tabItem.setMnemonic('c');
					tabItem.setIcon(MmsIcons.getTabAddIcon());
				} else {
					disabledTab.setEnabled(false, MENU_TEXT_TAB, "You have selected more than one column.<br>Tabs operate only on one column");					
				}
			} else {
				disabledTab.setEnabled(true, null, null);
				tabItem.setIcon(MmsIcons.getTabDeleteIcon());
				tabItem.setMnemonic('r');
				tabItem.setText("<html>Remove tabs for \"<i>"
						+ getColumnAbbreviation(_tabs.dataColumnIndex)
						+ "\"</i></html>");
			} 
		} else {
			hideDisabler.setEnabled(false, MENU_TEXT_HIDE_COLUMNS,
					COLUMN_SELECTION_COMPLAINT);
			shiftLeftDisabled.setEnabled(false, MENU_TEXT_SHIFT_LEFT,
					COLUMN_SELECTION_COMPLAINT);
			shiftRightDisabled.setEnabled(false, MENU_TEXT_SHIFT_RIGHT,
					COLUMN_SELECTION_COMPLAINT);
			freezeDisabler.setEnabled(false, MENU_TEXT_FREEZE_AS_ROW_HEADER,
					COLUMN_SELECTION_COMPLAINT);
		}
		if (columnFreezer.l.size() > 0) {
			unfreezeDisabler.setEnabled(true, null, null);
		} else {
			unfreezeDisabler.setEnabled(false, MENU_TEXT_UNFREEZE,
					"No columns are currently frozen<br>as row headers.");
		}

	}

	public String getEditOrViewText() {
		final int[] selected = table.getSelectedRows();
		int selectedDataRowCount = getSelectedDataRowCount(selected);
		if (editMenuItem != null && selectedDataRowCount > 0) {
			final int n = getSelectedAndEditableDataRowCount(selected);
			if (n > 0) {
				return EDIT_MENU_TEXT;
			}
		}
		return VIEW_MENU_TEXT;
	}

	public boolean setCellAdvice(final int visualRowIndex,
			final int modelColumnIndex, final CellAdvice cellAdvice) {
		final Row row = getRowAtVisualIndex(visualRowIndex);
		return row == null ? false : row.setAdvice(
				getDataColumnIndex(modelColumnIndex), cellAdvice);
	}

	public static void print(final Row row, final java.io.PrintStream out) {
		for (int i = 0; i < row.getColumnCount(); i++) {
			if (i > 0) {
				out.print(", ");
			}
			out.print(row.get(i));
		}
		out.println();
	}

	GroupedDataSource.Node[] getSelectedNodes() {
		final TreePath[] tp = getSelectedTreePaths();
		final GroupedDataSource.Node[] n = new GroupedDataSource.Node[tp.length];
		for (int i = 0; i < tp.length; i++) {
			n[i] = (GroupedDataSource.Node) tp[i].getLastPathComponent();
		}
		return n;
	}

	GroupedDataSource.Node getHighestCommonNodeOtherThanRoot() {
		final TreePath[] tp = getSelectedTreePaths();
		final int n = tp.length;
		if (n < 2) {
			return n == 1 ? (GroupedDataSource.Node) tp[0]
					.getLastPathComponent() : null;
		}
		final List<TreeNode>[] l = new List[n];
		for (int i = 0; i < n; i++) {
			l[i] = new ArrayList<TreeNode>();
			final GroupedDataSource.Node node = (GroupedDataSource.Node) tp[i]
					.getLastPathComponent();
			l[i].add(node);
			for (TreeNode tn = node.getParent(); tn != null; tn = tn
					.getParent()) {
				l[i].add(tn);
			}
		}
		for (int i = 0; i < n; i++) {
			Collections.reverse(l[i]);
		}
		int shortestPath = Integer.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			final int nn = l[i].size();
			if (nn < shortestPath) {
				shortestPath = nn;
			}
		}
		// start after root and find lowest node that all selections share
		TreeNode highestCommonNode = null;
		TreeNode[] a = new TreeNode[n];
		for (int level = 1; level < shortestPath; level++) {
			int i = 0;
			for (i = 0; i < n; i++) {
				a[i] = l[i].get(level);
			}
			for (i = 1; i < n; i++) {
				if (!a[i].equals(a[i - 1])) {
					break;
				}
			}
			if (i == n) {
				highestCommonNode = a[0];
			} else {
				break;
			}
		}
		return (GroupedDataSource.Node) highestCommonNode;
	}

	int[] getDataColumnIndexesThatAreVisibleInVisibleOrder() {
		final int n=table.getColumnCount();
		final int []dc=new int[n];
		for (int i=0;i<n;i++) {
			dc[i]=getDataColumnIndexFromVisualIndex(i);
		}
		return dc;
	}
	
	public int[] getDataColumnIndexesThatAreVisible() {
		final int []dc=new int[modelColumnIdentifiers.size()];
		int i=0;
		for (final String s:modelColumnIdentifiers){
			dc[i++]=metaRow.indexOf(s);			
		}
		return dc;
	}
	int[] getDataColumnIndexesThatAreVisible(final PersonalizableTable tbl) {
		final int n = tbl.getColumnCount();
		int[] dataColumnIndexes = new int[n];
		for (int i = 0; i < n; i++) {
			dataColumnIndexes[i] = getDataColumnIndex(SwingBasics
					.getModelIndexFromVisualIndex(tbl, i));
		}
		return dataColumnIndexes;
	}

	int[] getSortedAndVisibleDataColumns() {
		int[] s = getSort();
		final ArrayList al = new ArrayList();
		for (int i = 0; i < s.length; i++) {
			al.add(new Integer(s[i]));
		}
		final int n = table.getColumnCount();
		for (int i = 0; i < n; i++) {
			Integer dataColumn = new Integer(getDataColumnIndex(SwingBasics
					.getModelIndexFromVisualIndex(table, i)));
			if (!al.contains(dataColumn)) {
				al.add(dataColumn);
			}
		}
		return Basics.toIntArray(al);
	}

	TreePath[] getSelectedTreePaths() {
		final TreePath[] tp;
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			final TreePath[] p = groupedDataSource.tree.getSelectionPaths();
			tp = p == null ? new TreePath[0] : p;
		} else {
			tp = new TreePath[0];
		}
		return tp;
	}

	Iterator getSelected() {
		return getSelectionsInDescendingOrder().iterator();
	}

	boolean selectionsPropagateToLeaf = false;

	public void setSelectionsPropagateToLeaf(
			final boolean selectionsPropagateToLeaf) {
		this.selectionsPropagateToLeaf = selectionsPropagateToLeaf;
	}

	boolean isUserPicking = true;
	boolean autoSelectTableFromTree = false;
	public boolean allowArrowKeySelection = true;
	
	public void setAllowArrowKeysForSelection(boolean flag) {
		allowArrowKeySelection = flag;
	}

	public enum TreeStartupNode {
		SELECT_ROOT_NODE_AND_SHOW_ALL_ROWS, SELECT_NO_NODE_AND_SHOW_NO_ROWS, SELECT_NO_NODE_BUT_SHOW_ALL_ROWS
	};

	TreeStartupNode autoSelectTreeRoot = TreeStartupNode.SELECT_NO_NODE_AND_SHOW_NO_ROWS;

	public void setTreeAutoSelect(final boolean autoSelectTableFromTree,
			final TreeStartupNode autoSelectTreeRoot) {
		this.autoSelectTableFromTree = autoSelectTableFromTree;
		this.autoSelectTreeRoot = autoSelectTreeRoot;
	}

	public Collection<Row> getSelectionsInDescendingOrder(
			final boolean useSeivedTableIfActive) {
		final int option = getGroupOption();
		if (useSeivedTableIfActive && groupedDataSource != null
				&& option != NO_GROUPING) {
			return groupSeivedModel.getSelectedRowsInDescendingOrder();
		}
		return getSelectionsInDescendingOrder();
	}

	public Collection<Row> getSelectionsInDescendingOrder() {
		final Collection<Row> al;
		if (groupedDataSource != null) {
			if (groupedDataSource.tree != null
					&& getGroupOption() == GROUP_BY_TREE) {
				final TreePath[] tp = groupedDataSource.tree
						.getSelectionPaths();
				if (tp != null) {
					al = tp.length > 1 ? new HashSet<Row>()
							: new ArrayList<Row>();
					for (int i = 0; i < tp.length; i++) {
						final GroupedDataSource.Node node = (GroupedDataSource.Node) tp[i]
								.getLastPathComponent();
						if (!selectionsPropagateToLeaf || node.isLeaf()) {
							for (final Iterator<Row> it = node
									.ungroupedChildRowIterator(); it.hasNext();) {
								final Row r = it.next();
								if (r != null) {
									al.add(r);
								}
							}
						}
					}
				} else {
					al = Basics.UNMODIFIABLE_EMPTY_LIST;
				}
			} else {
				al = Basics.UNMODIFIABLE_EMPTY_LIST;
			}
		} else {
			al = getSelectedRowsInDescendingOrder();
		}
		return al;
	}

	public int resetTreePicks() {
		int n = 0;
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE
				&& groupedDataSource.isSortedBy(applicationSpecificTreeSort)) {
			n = groupedDataSource.resetPicks();
		}
		return n;
	}

	public void reallyRepaintTree() {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			resetDisabledCache();
			groupedDataSource.reallyRepaintTree();
		}
	}

	public void updateTreeUI() {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			disabledTextByNode = null;
			groupedDataSource.updateUI();
		}
	}

	public boolean areAllRowsShowing() {
		if (dataSource.isFiltering()) {
			return (dataSource.getFilteredDataRows().size() +  dataSource.getHiddenRows().size()) == dataSource
					.getDataRows().size();
		}
		return true;
	}

	public int getVisualColumnIndexFromDataColumnIndex(final int dataColumnIndex) {
		final String name = metaRow.getDataColumnIdentifier(dataColumnIndex);
		if (!isHidden(name) && table != null && table.tcm != null) {
			return table.tcm.getColumnIndex(name);
		}
		return -1;
	}

	public int getModelColumnIndexFromDataColumnIndex(final int dataColumnIndex) {
		final int visualColumnIndex = getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
		if (visualColumnIndex >= 0) {
			return table.tcm.getColumn(visualColumnIndex).getModelIndex();
		}
		return -1;
	}

	public boolean isHidden(final int dataColumnIndex) {
		return isHidden(metaRow.getDataColumnIdentifier(dataColumnIndex));
	}

	public boolean isHidden(final String identifier) {
		return !modelColumnIdentifiers.contains(identifier);
	}

	public void highlightColumn(final int dataColumnIndex) {
		highlightColumn(dataColumnIndex, true);
	}

	public void highlightColumn(final int dataColumnIndex, final boolean reset) {
		final String name = metaRow.getDataColumnIdentifier(dataColumnIndex);
		if (isHidden(name)) {
			showColumn(name);
		}
		final int visualColumnIndex = table.tcm.getColumnIndex(name);
		final int modelColumnIndex = table.tcm.getColumn(visualColumnIndex)
				.getModelIndex();
		if (reset) {
			table.cellHighlighter.reset();
		}
		table.cellHighlighter.selectColumn(modelColumnIndex, visualColumnIndex,
				null, !reset);
	}

	int[] applicationSpecificTreeSort;
	int singleSelectForDescendentOfColumn = -1;
	int expandToLevel = -1;

	boolean treeSupportsUndo = false;
	int minimumPickLevel = -1;

	public void setMinimumPickLevel(final int minimumPickLevel) {
		this.minimumPickLevel = minimumPickLevel;
	}

	public interface NodeEncoder {
		String encode(GroupedDataSource.Node node);
	}

	private NodeEncoder nodeEncoder;
	private boolean reselectPriorSelectedTreeNodesWhenRegrouping = true;;
	
	int []applicationSpecificTreeSearch;
	String textAreaSearchHint="Enter search values";
	String textFieldSearchHint="Enter search value";
	public void setApplicationSpecificTreeSort(
			final int[] applicationSpecificTreeSort,
			final int singleSelectForDescendentOfColumn,
			final boolean ascending, final boolean supportUndo,
			final int expandToLevel, final NodeEncoder nodeEncoder,
			final boolean reselectPriorSelectedTreeNodesWhenRegrouping,
			final String textAreaSearchHint,
			final int []yellowStickySearch, 
			final JButton additionalTreeSearch,
			final String additionalTreeSearchToolTip) {
		this.nodeEncoder = nodeEncoder;
		this.textAreaSearchHint=textAreaSearchHint;
		this.applicationSpecificTreeSearch=yellowStickySearch;
		this.reselectPriorSelectedTreeNodesWhenRegrouping = reselectPriorSelectedTreeNodesWhenRegrouping;
		this.treeSupportsUndo = supportUndo;
		this.applicationSpecificTreeSort = applicationSpecificTreeSort;
		this.singleSelectForDescendentOfColumn = singleSelectForDescendentOfColumn;
		this.expandToLevel = expandToLevel;
		setSortOrder(applicationSpecificTreeSort, ascending);
		setTreeOn();
		ignoreSelections=new ArrayList<TreePath>();
		this.additionalTreeSearch=additionalTreeSearch;
		this.additionalTreeSearchToolTip=additionalTreeSearchToolTip;
	}

	public boolean hasTreeYellowStickyEntries() {
		if (groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource.hasSeeOnlyText();
		}
		return false;
	}

	public String getTreeYellowStickyEntries() {
		if (groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource.getSeeOnlyText();
		}
		return "";
	}

	public String getTreeYellowStickyHint() {
		if (groupedDataSource != null
				&& groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return textAreaSearchHint;
		}
		return "";
	}

	boolean allowEditingOfCompanionTable = false;

	public void setAllowEditingOfCompanionTable(final boolean ok) {
		allowEditingOfCompanionTable = ok;
	}

	public void setTreeSort(final int[] applicationSpecificTreeSort,
			final boolean ascending, final int expandToLevel,
			final NodeEncoder nodeEncoder,
			final boolean allowEditingOfCompantionTable) {
		this.nodeEncoder = nodeEncoder;
		this.treeSupportsUndo = false;
		this.expandToLevel = expandToLevel;
		this.allowEditingOfCompanionTable = allowEditingOfCompantionTable;
		setSortOrder(applicationSpecificTreeSort, ascending);
		setTreeOn();
	}

	public void setTreeOn() {
		setProperty(PROPERTY_GROUP, GROUP_BY_TREE);
		groupOption = GROUP_BY_TREE;
	}

	public void setSortOrder(final int[] sortOrder, final boolean ascending) {
		final ArrayList al = new ArrayList();
		for (int sortO : sortOrder) {
			final String name = (String) metaRow.getDataColumnIdentifier(sortO);
			al.add(name);
			setProperty(name + '.' + PROPERTY_ASCENDING, ascending);
		}
		setProperty(PROPERTY_SORT_ORDER, Basics.urlEncode(al));
		initSortInfo();
	}

	public PersonalizableDataSource getDataSource() {
		return dataSource;
	}

	private int dividerChanges = 0;

	public void handleDivider(final JSplitPane split, final int minimumSplit) {
		handleDivider("0", split, minimumSplit);
	}

	public int getDividerLocation(final String dividerPrefix) {
		final String property = dividerPrefix + "." + PROPERTY_DIVIDER;
		return getProperty(property, -1);
	}

	public boolean labelGroupedView = true;

	public void handleDivider(final String dividerPrefix,
			final JSplitPane splitPane,final int minimumSplit) {
		final String property = dividerPrefix + "." + PROPERTY_DIVIDER;
		final int dividerLocation = getDividerLocation(dividerPrefix);
		if (dividerLocation != -1) {
			splitPane.setDividerLocation(dividerLocation < minimumSplit? minimumSplit:dividerLocation);
		}
		final BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane
				.getUI()).getDivider();
		divider.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				final int dividerLocation = splitPane.getDividerLocation();
				dividerChanges++;
				if (dividerChanges > 1) {
					getUngroupedModel().layoutChangeCnt++;
					setProperty(property, dividerLocation);
				}
			}
		});
	}

	ImageIcon treeBackgroundImage = null;

	public void setTreeBackgroundImage(final ImageIcon image) {
		treeBackgroundImage = image;
		final GroupedDataSource gds = getUngroupedModel().groupedDataSource;
		if (gds != null ) {
			//gds.tree.setOpaque(image==null);
			gds.treeImage=image;
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if (gds.tree != null) {
						final Container c = gds.tree.getParent();
						if (c != null) {
							c.invalidate();
							c.repaint();
						}
					}
				}
			});
		}

	}

	static TableCellEditor getComboBoxEditor(final Collection values,
			final Collection unselectableValues, final Collection forbiddenValues, final boolean edit,
			final boolean allowNew, final boolean isCaseSensitive,
			final StringConverter sc, final Object currentValue) {
				final Object oa=Basics.getFirst(values);
		if (oa instanceof Object[]){
			return AutoComplete.CellEditor.New(
					(Object [])oa,
					unselectableValues, 
					forbiddenValues,
					true, true, sc, allowNew, isCaseSensitive,
					true, currentValue);
			
		}
		if (currentValue instanceof JButton) {
			return ComboCellEditor.New(values, false, allowNew,
					1000);
		}

		return AutoComplete.CellEditor.New(
				values.toArray(),
				unselectableValues, 
				forbiddenValues,
				true, true, sc, allowNew, isCaseSensitive,
				true, currentValue);
	}

	public void unsortAndRepaint() {
		unsort();
		sortAndRepaint();
		notifyViewChanged();

	}

	public static boolean undoText2Lines = false;

	static void setUndo(final AbstractButton undo, final int undone,
			final int history,final boolean setText, final String op,
			final String noUndoMsg) {
		if (undo.getIcon() == null) {
			undo.setIcon(MmsIcons.getUndoIcon());			
		}
		undo.setHorizontalTextPosition(SwingConstants.RIGHT);
		DisabledExplainer undoDisabled = new DisabledExplainer(undo);
		if ((undone - 1) >= 0) {
			final String txt=Basics.concatObjects(
					Basics.startHtml2(),
					op,
					"</font></b><small>",
					 (undoText2Lines ? "<br> " : " "),
					 (undone),
					 " of ",
					 history,
					 "</small>",
					 Basics.endHtml2());
			if(setText){
				undo.setText(txt);
			}
			undo.setToolTipText(txt);
			undoDisabled.setEnabled(true,null,null);
		} else {
			undoDisabled.setEnabled(false, op, noUndoMsg==null?
					"This is ONLY enabled if you have something to undo.":
						noUndoMsg);
			if(setText){
				undo.setText(op);
			}
		}
	}

	public final static String REDO_NO_CAN_DO="There are no operations to redo.";
	static void setRedo(final AbstractButton redo, final int undone,
			final int history, final boolean setText, final String op,
			final String noRedoMsg) {
		if (redo.getIcon() == null) {			
			redo.setIcon(MmsIcons.getRedoIcon());
		}
		redo.setHorizontalTextPosition(SwingConstants.RIGHT);
		DisabledExplainer redoDisabled = new DisabledExplainer(redo);
		if ((undone + 1) < history) {
			final String txt=Basics.concatObjects(
					Basics.startHtml2(), 
					op,
					"</font></b><small>",
					 (undoText2Lines ? "<br> " : " "),
					 undone + 2,
					 " of ",
					 history,
					"</small>",
					Basics.endHtml2());
			if (setText){
				redo.setText(txt);
			}
			redo.setToolTipText(txt);
			redoDisabled.setEnabled(true,null,null);
		} else {
			if (setText){
				redo.setText(op);
			}
			redoDisabled.setEnabled(false, op, noRedoMsg==null?REDO_NO_CAN_DO:noRedoMsg);
			
		}
	}

	public PersonalizableTableModel getModelShowing() {
		final int option = getGroupOption();
		if (option != NO_GROUPING && groupSeivedModel != null) {
			return groupSeivedModel;
		}
		return this;
	}

	private Collection<ListSelectionListener> listSelectionListeners;
	
	private Collection<ColumnsAlwaysAllowEditInPlaceListener> columnsAlwaysAllowEditInPlaceListeners;
	
	public void addListSelectionListener(final ListSelectionListener lsl) {
		PersonalizableTableModel u = getUngroupedModel();
		if (u.listSelectionListeners == null) {
			u.listSelectionListeners = new ArrayList();
		}
		u.listSelectionListeners.add(lsl);
	}	
	public abstract interface ColumnsAlwaysAllowEditInPlaceListener {
		
		public abstract void valueChanged(Object value);
	}
	
	public void addColumnsAlwaysAllowEditInPlaceListener(final ColumnsAlwaysAllowEditInPlaceListener lsl) {
		PersonalizableTableModel u = getUngroupedModel();
		if (u.columnsAlwaysAllowEditInPlaceListeners == null) {
			u.columnsAlwaysAllowEditInPlaceListeners = new ArrayList();
		}
		u.columnsAlwaysAllowEditInPlaceListeners.add(lsl);
	}

	void fireIfNoneSelected() {
		final int n = getSelectedRowCount();
		if (n == 0) {
			fireListSelection(new ListSelectionEvent(table, -1, -1, false));
		}
	}

	public void add(){
		final PersonalizableTableModel tableModel=getModelShowing(), rm=getUngroupedModel();
		if (tableModel.table.getCellEditor() != null) {
			tableModel.table.getCellEditor().stopCellEditing();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					rm._add();
					rm.requestFocusLater();									
				}
			});	
		}
		else {
			rm._add();
			rm.requestFocusLater();
		}
	}
	
	private void _add() {
		setMultiRowChangeOperationFlag(1, true);		
		dataSource.add();
		fireListSelection(new ListSelectionEvent(table, -1, -1, false));
		setMultiRowChangeOperationFlag(1, false);
	}
	
	
	void fireColumnsAlwaysAllowEditInPlaceChanged(Object value) {
		final PersonalizableTableModel u = getUngroupedModel();
		final Collection<ColumnsAlwaysAllowEditInPlaceListener> c = u.columnsAlwaysAllowEditInPlaceListeners;
		if (c != null) {
			for (final ColumnsAlwaysAllowEditInPlaceListener lsl : u.columnsAlwaysAllowEditInPlaceListeners) {
				lsl.valueChanged(value);
			}
		}
	}

	public void fireListSelection() {
		fireListSelection(new ListSelectionEvent(table, 0, 0, false));
	}

	public static class ActiveFilterSelectionitem {
		public final String text, toolTip;
		private final ActiveFilter activeFilter;

		public ActiveFilterSelectionitem(final String text,
				final String toolTip, final ActiveFilter activeFilter) {
			this.text = text;
			this.toolTip = toolTip;
			this.activeFilter = activeFilter;
		}

		private JMenuItem instantiateMenuItem(
				final PersonalizableTableModel tableModel) {
			final JMenuItem jm = new JMenuItem(text);
			jm.setToolTipText(toolTip);
			jm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setActiveFilter(tableModel);
				}
			});
			return jm;
		}

		public void setActiveFilter(final PersonalizableTableModel tableModel) {
			if (activeFilter == null) {
				tableModel.dataSource.removeFilter();
			} else {
				tableModel.dataSource.setActiveFilter(text, activeFilter);
			}
			notifyActiveFilterApplied(tableModel);
			if (tableModel.hasSingleQueryViaCurrentFilterRows()) {
				tableModel.syncFilter(true);
			}
		}

		public void notifyActiveFilterApplied(
				final PersonalizableTableModel tableModel) {
			tableModel.refresh();
		}

		public String toString() {
			return text;
		}
	}

	public void selectAll() {
		final int option = getGroupOption();
		if (option == NO_GROUPING) {
			table.selectAll();
		} else {
			final JTree tree = getTree();
			if (tree != null) {
				tree.setSelectionPath(new TreePath(this.groupedDataSource
						.getRoot()));
			}
		}
	}

	public void clearSelectionsShowing(final boolean suppressRequestFocus) {
		final int option = getGroupOption();

		final FocusFreeze ff = new FocusFreeze(suppressRequestFocus);
		if (option == NO_GROUPING) {
			table.clearSelection();

		} else {
			final JTree tree = getTree();
			tree
					.setSelectionPath(new TreePath(this.groupedDataSource
							.getRoot()));
		}
		clearColumnSelections();
		ff.restorePrevValue();
	}

	public void clearSelection() {
		final int option = getGroupOption();
		if (option == NO_GROUPING) {
			table.clearSelection();
		} else {
			final JTree tree = getTree();
			if (tree != null) {
				tree.clearSelection();				
			}
		}
	}

	public java.util.List<Row> getFilteredDataRowsShowing() {
		final int option = getGroupOption();
		if (option == NO_GROUPING) {
			return dataSource.getFilteredDataRows();
		} else {
			return getUngroupedModel().groupSeivedModel.dataSource
					.getFilteredDataRows();
		}
	}

	/**
	 * @todo Must refactor this to be instance method (non static)
	 * @param pickChecker
	 *            PickChecker
	 */
	public static void setGlobalPickChecker(final PickChecker pickChecker) {
		PersonalizableTableModel.pickChecker = pickChecker;

	}

	public String createToolTip(final int dataColumnIndex, final String msg,
			final boolean error) {
		final String h = Basics.stripSimpleHtml(metaRow
				.getLabel(dataColumnIndex));
		return error ? Basics.toHtmlErrorUncentered(h, msg) : Basics
				.toHtmlUncentered(h, msg);
	}

	public void resetActiveFilter() {
		dataSource.applyActiveFilters();
	}

	public void setActiveFilter(final String key, final ActiveFilter af) {
		dataSource.setActiveFilter(key, af);
	}

	private boolean resetPending = false;

	public void refreshLater(final boolean showOneMomentDisplay) {
		refreshLater(showOneMomentDisplay, true);
	}

	public void refreshLater(final boolean showOneMomentDisplay, final boolean refreshSelect) {
		if (!resetPending) {
			if (showOneMomentDisplay) {
				showOneMomentDisplay();
			}
			resetPending = true;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					resetAndRefresh(refreshSelect);
					if (showOneMomentDisplay) {
						hideOneMomentDisplay();
					}
				}
			});
		}
	}

	private void resetAndRefresh(final boolean refreshSelect) {
		resetActiveFilter();
		resetDisabledCache();
		refresh(refreshSelect);
	}

	public boolean showUrlText = false;
	public int treeOneMomentThreshold = 20;

	public interface EmptyTreeLabel {
		JLabel get();
	}

	public static EmptyTreeLabel emptyTreeLabel = new EmptyTreeLabel() {

		public JLabel get() {

			JLabel emptyTreeLabel = new JLabel(
					"<html>Click on a <b>node</b> in the <i>tree</i><br>to see a table of rows here...</html>",
					JLabel.CENTER);
			emptyTreeLabel.setVerticalTextPosition(JLabel.TOP);
			return emptyTreeLabel;

		}
	};

	private int allowTreeSelectionsAtThisLevelOrGreater = 0;

	public void setAllowTreeSelectionsAtThisLevelOrGreater(final int l) {
		allowTreeSelectionsAtThisLevelOrGreater = l;
	}

	public String getSingularKey() {
		return singularKey;
	}

	static JComboBox getReadOnlyComboBox(final Collection values,
			final StringConverter sc,
			final AutoComplete.FoundListener foundListener, final int maxWidth) {
		return ComboCellEditor.newComboBox(values, false, false,
				500, foundListener, false, maxWidth, -1);

	}

	public boolean isWithinSubTreeOf(final int treeLevel,
			final int dataColumnIndexThatIsSorted) {
		if (groupedDataSource != null) {
			for (int i = treeLevel; i < groupedDataSource.sortInfo.length; i++) {
				if (groupedDataSource.sortInfo[i].dataColumnIndex == dataColumnIndexThatIsSorted) {
					return true;
				}

			}
		}
		return false;
	}

	private static final String ADD_CURRENT_TO_FAVORITES = "Add current to Favorites",
			REMOVE_CURRENT_FROM_FAVORITES = "Remove current from Favorites",
			REMOVE_ALL_FAVORITES = "Remove all Favorites";
	
	private boolean areAnyDisplayedColumnsEditable(final Row row) {
		boolean value = false;
		if (row == null) {
			value = true;// presumably some Row class extender allows editing
			// null for creating?
		} else if (table != null) {
			final int n = table.getColumnCount();
			for (int i = 0; !value && i < n; i++) {				
				final int dataColumnIndex = getDataColumnIndexFromVisualIndex(i);
				value = row.isEditable(dataColumnIndex);
			}
		}
		return value;
	}

	private boolean isEditable(final Row row) {
		boolean value = true;
		if (!isPickList && !readOnly) {
			if (row == null) {
				value = true;// presumably some Row class extender allows
				// editing
				// null for creating?
			} else {
				final int dataColumns = metaRow.size();
				value=false;
				for (int dataColumnIndex = 0; !value
						&& dataColumnIndex < dataColumns; dataColumnIndex++) {
					value = row.isEditable(dataColumnIndex);
				}
			}
		}
		return value;
	}
	
	public void move(ArrayBasics.Direction d){
		final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
		if (selected.length > 0) {
			table.clearSelection();
			final int row = (showFilterUI ? 2 : 0);
			final int idx = selected[0] - row;
			dataSource.move(idx, -2);
			table.addRowSelectionInterval(row, row);
			table
					.scrollToVisible(row, SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));

		}
		unsortAndRepaint();
	}

	public void moveUp() {
		final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
		if (selected.length > 0) {
			table.clearSelection();
			for (int i = 0; i < selected.length; i++) {
				final int idx = selected[i] - (showFilterUI ? 2 : 0);
				if (idx > 0) {
					dataSource.move(idx, -1);
					table.addRowSelectionInterval(selected[i] - 1,
							selected[i] - 1);

				} else {
					table.addRowSelectionInterval(0,0);

				}
			}
			table
					.scrollToVisible(selected[0] - 1, SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));
		}
		unsortAndRepaint();
		getTable().requestFocus();
		fireListSelection();

	}
	
	public void moveUp(int[] selected) {
		if (selected!=null && selected.length > 0) {
			table.clearSelection();
			for (int i = 0; i < selected.length; i++) {
				final int idx = selected[i] - (showFilterUI ? 2 : 0);
				if (idx > 0) {
					dataSource.move(idx, -1);
					table.addRowSelectionInterval(selected[i] - 1,
							selected[i] - 1);

				}
			}
			table
					.scrollToVisible(selected[0] - 1, SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));
		}
		unsortAndRepaint();
	}

	public void moveBottom() {
		final int[] selected = getSelectedDataRowIndexesInAscendingOrder();

		if (selected.length > 0) {
			table.clearSelection();
			for (int i = 0; i < selected.length; i++) {
				final int idx = selected[i]-i - (showFilterUI ? 2 : 0);
				dataSource.move(idx, 2);
				table.addRowSelectionInterval(dataSource.size()-1-i, dataSource.size()-1-i);
			}
			table
					.scrollToVisible(dataSource.size(), SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));
		}
		unsortAndRepaint();
	}

	public void moveTop() {
		final int[] selected = getSelectedDataRowIndexesInDescendingOrder();
		if (selected.length > 0) {
			table.clearSelection();
			for (int i = 0; i < selected.length; i++) {
				//final int row =(showFilterUI ? 2 : 0);
				final int idx = selected[i]+i - (showFilterUI ? 2 : 0);
				dataSource.move(idx, -2);
				table.addRowSelectionInterval(i, i);
			}
			table.scrollToVisible(0, SwingBasics
					.getVisualIndexFromModelIndex(table,
							focusModelColumnIndex));
		}
		unsortAndRepaint();

	}

	public void moveDown() {
		final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
		if (selected.length > 0) {
			table.clearSelection();
			final int n = getRowCountMinusEmptyLastRowForCreating();

			for (int i = selected.length - 1; i >= 0; i--) {
				final int idx = selected[i] - (showFilterUI ? 2 : 0);
				dataSource.move(idx, 1);
				if (idx < n-1) {
					table.addRowSelectionInterval(idx+1,idx+1);
				} else {
					table.addRowSelectionInterval(idx,idx);
				}
			}
			table
					.scrollToVisible(selected[0] + 1, SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));
		}
		unsortAndRepaint();
	}
	
	public void moveDown(int[] selected) {
		if (selected!=null && selected.length > 0) {
			table.clearSelection();
			final int n = dataSource.getFilteredDataRows().size();

			for (int i = selected.length - 1; i >= 0; i--) {
				final int idx = selected[i] - (showFilterUI ? 2 : 0);
				if (idx < n - 1) {
					dataSource.move(idx, 1);
					table.addRowSelectionInterval(selected[i] + 1,
							selected[i] + 1);
				}
			}
			table
					.scrollToVisible(selected[0] + 1, SwingBasics
							.getVisualIndexFromModelIndex(table,
									focusModelColumnIndex));
		}
		unsortAndRepaint();
	}

	public static String toSequenceableString(final Row row,
			final int dataColumnIndex) {
		if (row==null){
			return "";
		}
		return toSequenceableString(row.get(dataColumnIndex), row
				.getRenderOnlyValue(dataColumnIndex, false, false));
	}

	static String toSequenceableString(final Object o, final Object r_value) {
		String value = null;
		if (o instanceof AbstractButton) {
			value = ((AbstractButton) o).getText();
			if(Basics.isEmpty(value)){
				value = ((AbstractButton) o).getName();
			}
		} else if (o != null) {
			final StringConverter sc = (StringConverter) DefaultStringConverters
					.get(r_value == null ? o.getClass() : r_value.getClass());
			if (sc != null) {
				value = sc.toString(r_value == null ? o : r_value);
			} else if (o != null) {
				value = o.toString();
			}
		}

		return Basics.isEmpty(value) ? Basics.NULL : value;
	}


	public MetaRow getMetaRow() {
		return metaRow;
	}

	void alterSortSequence(final int dataColumnIndex, final Component component) {
		int choice=1;
		final PersonalizableTableModel ungroupedModel=getUngroupedModel();
		final String label = ungroupedModel.getColumnAbbreviation(dataColumnIndex);
		final PersonalizableTableModel ms = getModelShowing();
		final boolean needRefresh = !Basics.equals(ungroupedModel, ms);
		final SortValueReinterpreter svr=ungroupedModel.getMetaRow().getSortValueReinterpreter(dataColumnIndex);
		final String id=getDataColumnIdentifier(dataColumnIndex);
		final boolean hasCustomDefined=hasCustomDefinedSortSequence(ungroupedModel.metaRow, id);
		int defaultChoice=-1;
		if (svr!=null){
			if(svr.canTurnOff()){
				final boolean isOn=svr.isOn();
				final String [] choices=new String[]{"Alpha numeric sort order", svr.getName(), "Custom defined sort order"};
				if (hasCustomDefined){
					defaultChoice=2;
				} else if (isOn){
					defaultChoice=1;
				} else {
					defaultChoice=0;
				}
				choice=PopupBasics.getRadioButtonOption(
						component, 
						Basics.concat("<html>Sort sequence options for <b>", label, "</b>: </html>"), 
						"Make a choice", 
						choices, 
						defaultChoice, 
						true);
			}else{
				defaultChoice=-1;
			}
		} else {
			choice=2;
		}
		if (choice==0 ){
			if (defaultChoice !=0) {
				svr.activate(!svr.isOn());
				removeSortSequence(id);
				ungroupedModel.refresh();
			}
		} else if (choice==1){
			if (defaultChoice !=1) {
				svr.activate(!svr.isOn());
				removeSortSequence(id);
				ungroupedModel.refresh();
			}
		} else if (choice==2){
			PopupBasics
				.alert(
						component,
						Basics.concatHtmlUncentered(
							Basics.NOTE_BOLDLY, 
							" a sort sequence applies to <u>all possible</u> values of the underlying <br>table column <b>",
							label,
							"</b> which produces this tree display. <br>A sort sequence does <u>not</u> apply to just the current displayed values."),
						Basics.concat("\"", label, "\" sort sequence"),
						false);
			ungroupedModel.alterSortSequence(getTearAwayComponent(), dataColumnIndex, true, needRefresh);
		}
	}

	private boolean removeCustomSequence = false;

	private void alterSortSequence(
			final Component parent,
			final int dataColumnIndex,
			final boolean doSortNow,
			final boolean needRefresh) {
		qualifyActions();
		final String id = metaRow.getDataColumnIdentifier(dataColumnIndex);
		List<String> values;
		final Set<String> v = new HashSet<String>();
		final boolean wasAlphabetic;
		values = getSortSequence(metaRow, id);
		if (values==null){
			final File f=getSequenceFile(metaRow, id);
			final File bak=IoBasics.switchExtension(f, CUSTOM_SEQUENCE_BAK_FILE_EXTENSION);
			if (bak.exists()){
				bak.renameTo(f);
				final String key = metaRow.getKey() + "." + id;
				sequenceFiles.remove(key);
				values=getSortSequence(metaRow, id);
			}
			
		}
		wasAlphabetic = values == null;
		if (!wasAlphabetic) {
			v.addAll(values);
		} else {
			values = new ArrayList<String>();
		}
		for (final Row row : dataSource.getDataRows()) {
			final String value = toSequenceableString(row, dataColumnIndex);
			if (!v.contains(value)) {
				values.add(value);
				v.add(value);
			}
		}
		if (wasAlphabetic) {
			Collections.sort(values);
		}
		removeCustomSequence = false;
		final String resetButtonLabel = "Reset";
		final String resetText = Basics
				.toHtmlUncentered(
						resetButtonLabel,
						"Remove the custom sort sequence and <br><b>reset</b> to the standard alpha/numeric sequence?");
		final Object[] a = PopupBasics
				.reorganizeList(
						parent,
						MENU_TEXT_SEQUENCE,
						Basics.concatHtmlUncentered("Shuffle values for the column <b>",
								getColumnAbbreviation(dataColumnIndex),
								"</b> using <i>Alt</i> plus arrow keys.<br><br>(", Basics.NOTE_BOLDLY, "sort sequences apply to <u>all possible</u> values of a table column)</i>"),
						wasAlphabetic ? null : SwingBasics.getButton(
								resetButtonLabel, 'r', new ActionListener() {

									public void actionPerformed(ActionEvent e) {
										removeCustomSequence = true;
									}
								}, resetText), 0, values.toArray(), null,
						false, null);
		if (removeCustomSequence) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (PopupBasics.ask(parent, resetText)) {
						removeSortSequence(id);
						if (!doSortNow){
							return;
						}
						if (!isSorted(dataColumnIndex)) {
							sort(dataColumnIndex, true);
						} else {
							for (final SortInfo si : columnsToSort) {
								si.initSequence();
							}
						}
						if (!needRefresh) {
							sortAndRepaint();
						} else {
							refresh();
						}
						notifyViewChanged();
					}
				}
			});
			return;

		} else if (a == null) { // Cancel pressed
			return;
		}
		values.clear();
		Basics.addAll((List)values, a);
		putSortSequence(id, values);
		if (!doSortNow){
			return;
		}
		if (!isSorted(dataColumnIndex)) {
			sort(dataColumnIndex, true);
		} else {
			for (final SortInfo si : columnsToSort) {
				si.initSequence();
			}
		}
		if (!needRefresh) {
			sortAndRepaint();
		} else {
			refresh();
		}
		notifyViewChanged();
	}

	void setSortSequenceMenuText(final DisabledExplainer sortSequenceDisabled,
			final int dataColumnIndex) {
		if (dataColumnIndex >= 0) {
			final String t = getColumnAbbreviation(dataColumnIndex);
			sortSequenceItem.setText(Basics.concat("<html>", MENU_TEXT_SEQUENCE, ":  <i>", t, "</i></html>"));
			sortSequenceDisabled.setEnabled(true,null,null);
		} else {
			sortSequenceItem.setText(MENU_TEXT_SEQUENCE);
			sortSequenceDisabled.setEnabled(false,MENU_TEXT_SEQUENCE,"You cannot customize this sort sequence OR you have not selected an item(s) to sort");
		}
	}

	static final String CUSTOM_SEQUENCE_FILE_EXTENSION="sequence", CUSTOM_SEQUENCE_BAK_FILE_EXTENSION="bak";
	private static File getSequenceFile(final MetaRow metaRow,
			final String columnIdentifier) {
		final String key = metaRow.getKey() + "." + columnIdentifier;
		if (sequenceFiles.containsKey(key)) {
			return sequenceFiles.get(key);
		}
		String fileName = IoBasics.replaceFilenameAllergicChars(key
				+ "."+CUSTOM_SEQUENCE_FILE_EXTENSION);
		fileName = IoBasics.concat(rootSequenceFolder, fileName);
		final File value = new File(fileName);
		sequenceFiles.put(key, value);
		return value;
	}

	private static Map<String, List<String>> _sortSequence = new HashMap<String, List<String>>();
	private static final Map<String, File> sequenceFiles = new HashMap<String, File>();

	final static List<String> loadSortSequence(final MetaRow metaRow,
			final String columnIdentifier) {
		List<String> value = null;
		File f = getSequenceFile(metaRow, columnIdentifier);
		if (!f.exists()){
			f.renameTo(IoBasics.switchExtension(f, CUSTOM_SEQUENCE_FILE_EXTENSION));
		}
		if (f.exists()) {
			try {
				value = IoBasics.readTextFileLines(f);
				final String key = metaRow.getKey() + "." + columnIdentifier;

				_sortSequence.put(key, value);
			} catch (final Exception e) {
				Pel.log.warn(e);
			}
		}
		return value;
	}

	final boolean hasCustomDefinedSortSequence(final MetaRow metaRow, final String columnIdentifier){
			final String key = metaRow.getKey() + "." + columnIdentifier;
			List<String> value = null;
			return _sortSequence.containsKey(key);
	}
	final static List<String> getSortSequence(final MetaRow metaRow,
			final String columnIdentifier) {
		final String key = metaRow.getKey() + "." + columnIdentifier;

		List<String> value = null;
		if (_sortSequence.containsKey(key)) {
			value = _sortSequence.get(key);
		} else {
			if (sequenceFiles.containsKey(key)) {
				value = null;
			} else {
				value = loadSortSequence(metaRow, columnIdentifier);
			}
		}
		return value;
	}

	public void putSortSequence(final String columnIdentifier,
			final List<String> sequence) {
		final File f = getSequenceFile(metaRow, columnIdentifier);
		IoBasics.saveTextFile(f.getAbsolutePath(), sequence);
		final String key = metaRow.getKey() + "." + columnIdentifier;

		_sortSequence.put(key, sequence);
	}

	private void removeSortSequence(final String columnIdentifier) {
		final File f = getSequenceFile(metaRow, columnIdentifier);
		final File bak=IoBasics.switchExtension(f, CUSTOM_SEQUENCE_BAK_FILE_EXTENSION);
		f.renameTo(bak);
		final String key = metaRow.getKey() + "." + columnIdentifier;
		_sortSequence.remove(key);
		
	}

	public String getNewText() {
		return VERB_NEW + " \"" + getUngroupedModel().singularKey + "\"";
	}

	private Set tabValues;
	private Tabs tabs;
	
	public int getSelectedTabIndex(){
		int r=-1;
		if (tabs != null){
			r=tabs.getSelectedIndex();
		}
		return r;
	}
	
	public void setSelectedTabIndex(final int r){
		if (tabs != null){
			if (r>=0&&r<tabs.getTabCount()){
				tabs.setSelectedIndex(r);
			}
		}
	}

	private class Tabs extends JTabbedPane implements ActiveFilter {

		public SelectableActiveFilterGroup getTopGroup(){
	    	return null;
	    }   

	    public void setSelectedIndex(final int idx) {
			final PersonalizableTable tm = getModelShowing().table;
			tm.stopCellEditing();
			super.setSelectedIndex(idx);
		}

		private class Tab extends JPanel {
			private final Object value;

			private Tab(final Object value) {
				this.value = value;
				setLayout(new BorderLayout());
				setOpaque(false);
			}

			private String getPropertyPrefix() {
				if (Basics.isEmpty(oldPrefix) || oldPrefix.startsWith("tab.")) {
					return "tab." + dataColumnIndex + "." + value;
				}
				return oldPrefix+".tab." + dataColumnIndex + "." + value;
			}

			private void activate() {
				final String s = getPropertyPrefix();
				setPropertyPrefix(s);
				SwingBasics.switchContainers(
						isGrouped() ? groupedDataSource.splitPane
								: table.scrollPane, this);
				requestFocusLater();
				if (tabActivationListeners.size() > 0) {
					final ActionEvent e = new ActionEvent(this,
							dataColumnIndex, s);
					for (final ActionListener al : tabActivationListeners) {
						al.actionPerformed(e);
					}
				}
			}
		}

		private final int dataColumnIndex;

		private Tabs(final int dataColumnIndex, final String[] titles,
				final Object[] values) {
			setOpaque(false);
			this.dataColumnIndex = dataColumnIndex;
			if (titles.length > 0) {
				tabs = new Tab[titles.length];
				for (int i = 0; i < titles.length; i++) {
					final String title = titles[i];
					final Object value = values[i];
					final Tab tab = new Tab(value);
					tabs[i] = tab;
					super.addTab(
							"<html><b>" + toSequenceableString(title, null)
									+ "</b></html>", tab);
				}
			} else {
				tabs = new Tab[1];
				final Tab tab = new Tab(".");
				tabs[0] = tab;
				super.addTab("(empty)", tab);
			}
			addChangeListener(new ChangeListener() {
				public void stateChanged(final ChangeEvent e) {
					final int i = getSelectedIndex();
					if (i >= 0) {
						tabs[i].activate();
					}
				}
			});
			SwingBasics.switchContaineesWithinContainer(getTearAwayComponent(),
					this);
			oldPrefix = dataSource.getPropertyPrefix();
			oldDefaultPrefix = defaultPropertyPrefix;
			activeFilterKey = "Tab for "
					+ getColumnAbbreviation(dataColumnIndex);
			dataSource.setActiveFilter(activeFilterKey, this);
			final TabbedPaneUI tpu=getUI();
			if (tpu instanceof BasicTabbedPaneUI){
				setUI(new GradientBasics.TabbedPaneUI());
			}
			tabs[0].activate();
		}


		private final String activeFilterKey;

		public boolean meetsCriteria(final Row row) {
			final int idx = getSelectedIndex();
			final Object value = row.getFilterableValue(dataColumnIndex);
			return Basics.equals(tabs[idx].value, value);
		}

		public ActiveFilter.FilterType getType() {
	    	return ActiveFilter.FilterType.TABS;
	    }
		
		public boolean describeCriteriaFailures(final Row row, final Collection<String> criteriaFailureDescriptions){
	    	if (!meetsCriteria(row) ){
	            criteriaFailureDescriptions.add("tab setting");
	            return false;
	        }
	        return true;
	    }

		private final String oldPrefix, oldDefaultPrefix;

		private void close() {
			if (isGrouped()) {
				final int idx = getSelectedIndex();
				if (idx >= 0) {
					tabs[idx].remove(groupedDataSource.splitPane);
				}
			}
			SwingBasics.switchContaineesWithinContainer(this, table.scrollPane);
			PersonalizableTableModel.this.tabs = null;
			PersonalizableTableModel.this.tabValues = null;
			dataSource.removeActiveFilter(activeFilterKey);
		}

		private void dispose() {
			close();
			setPropertyPrefix(oldPrefix); // @todo reapplyFilter()?
			setDefaultPropertyPrefix(oldDefaultPrefix);
			requestFocusLater();
			String name = getPropertyPrefix();
			name = getPropertyName(name, PROPERTY_TABS);
			properties.remove(name);
			notifyViewChanged();
		}

		private final Tab[] tabs;
	}

	private final int MAX_TABS = 15;

	void activateTabs(final int dataColumnIndex,
			final boolean alertIfMaxExceeded) {
		if (hasSeeOnlySettings()) {
			PopupBasics.alert(getTearAwayComponent(),
					"<html>The tab view reveals rows hidden by<br>your \"see only\" settings.</html>",
					"Alert", false);
		}
		setTabProperty(dataColumnIndex);
		final String columnIdentifier = metaRow
				.getDataColumnIdentifier(dataColumnIndex);
		final List<String> sequence = PersonalizableTableModel.getSortSequence(
				metaRow, columnIdentifier);

		final Map vMap = new TreeMap();
		Map tMap = null;
		tabValues = new HashSet();
		final Collection<Row> rows = dataSource.getDataRows();
		if (sequence == null) {
			for (final Row row : rows) {
				final Object value = row.getFilterableValue(dataColumnIndex);
				tabValues.add(value);
				final String title = toSequenceableString(row, dataColumnIndex);

				vMap.put(title, value);
			}
		} else {
			tMap = new TreeMap();
			for (final Row row : rows) {
				final Object value = row.getFilterableValue(dataColumnIndex);
				final String title = toSequenceableString(row, dataColumnIndex);
				final Integer key = sequence.indexOf(title);
				tabValues.add(value);
				vMap.put(key, value);
				tMap.put(key, title);
			}
		}
		final int n = vMap.size();
		if (n > MAX_TABS) {
			if (alertIfMaxExceeded) {
				PopupBasics.alert("There are " + n + " distinct values for \""
						+ getColumnAbbreviation(dataColumnIndex) + "\"",
						"Tab limit of " + MAX_TABS + " exceeded!", true);
			}
		} else {
			if (sequence == null) {
				tabs = new Tabs(dataColumnIndex, (String[]) vMap.keySet()
						.toArray(new String[n]), vMap.values().toArray());
			} else {
				tabs = new Tabs(dataColumnIndex, (String[]) tMap.values()
						.toArray(new String[n]), vMap.values().toArray());

			}
		}
	}

	public void setTabProperty(final int dataColumnIndex) {
		String propertyPrefix = getPropertyPrefix();
		final String name = getPropertyName(propertyPrefix, PROPERTY_TABS);
		properties.setProperty(name, "" + getDataColumnIdentifier(dataColumnIndex));
		notifyViewChanged();
	}

	private void setFromTabContext(final Row row) {
		if (tabs != null && row != null) {
			final int idx = tabs.getSelectedIndex();
			final Object columnValue = tabs.tabs[idx].value;
			final int dataColumnIndex = tabs.dataColumnIndex;
			if (!dataSource.isPartOfPrimaryKey(dataColumnIndex)){
				row.set(dataColumnIndex, columnValue);
			}
		}
	}

	public void addNewRowToFilterListForEditing(final Row row) {
		final List<Row> filteredDataList = dataSource.getFilteredDataRows();
		if (!filteredDataList.contains(row)) {
			filteredDataList.add(row);
		}
		final List<Row> dataList = dataSource.getDataRows();
		if (!dataList.contains(row)) {
			dataList.add(row);
		}
		
	}

	public int getTabbedDataColumnIndex() {
		int value = -1;
		if (tabs != null) {
			value = tabs.dataColumnIndex;
		}
		return value;
	}

	public Object getTabbedValue() {
		Object value = null;
		if (tabs != null) {
			final int idx = tabs.getSelectedIndex();
			value = tabs.tabs[idx].value;
		}
		return value;
	}

	public Collection<Object> getTabValues() {
		final Collection<Object> c=new ArrayList<Object>();
		if (tabs != null) {
			for (final PersonalizableTableModel.Tabs.Tab tab : tabs.tabs) {
				c.add(tab.value);
			}
		}
		return c;
	}
	
	void handleTabsWhenFirstDisplaying() {
		if (tabs == null) {
			String name = getPropertyPrefix();
			name = getPropertyName(name, PROPERTY_TABS);
			final String dataColumnIdentifier= properties.getProperty(name);
			closeTabsIfNecessary();
			final int requiredDataColumnIndex=metaRow.indexOf(dataColumnIdentifier);
			if (requiredDataColumnIndex >= 0 && requiredDataColumnIndex < metaRow.size()/*handle wierd case of properties for Inventory being in other class/slot of reagent product */) {
				activateTabs(requiredDataColumnIndex, false);
			}
		}
	}

	private boolean isReTabPending = false;

	private boolean alwaysRetabWhenRefreshing = false;

	public void setAlwaysRetabWhenRefreshing(final boolean ok) {
		alwaysRetabWhenRefreshing = ok;
	}

	private void reTabIfNecessary(final int dataColumnIndex) {
		isReTabPending = false;
		if (!alwaysRetabWhenRefreshing) {
			tabs.dispose();
			activateTabs(dataColumnIndex, true);

			final Set _tabValues = new HashSet();
			final Collection<Row> rows = dataSource.getDataRows();
			for (final Row row : rows) {
				final Object value = row.getFilterableValue(dataColumnIndex);
				_tabValues.add(value);
			}
			if (_tabValues.size() == tabValues.size()
					&& Basics.containsAll(_tabValues, tabValues)
					&& Basics.containsAll(tabValues, _tabValues)) {
				return;
			}

		}
		final int idx = tabs.getSelectedIndex();
		final Object value = tabs.tabs[idx].value;
		tabs.dispose();
		activateTabs(dataColumnIndex, true);
		for (int i = 0; i < tabs.tabs.length; i++) {
			if (Basics.equals(value, tabs.tabs[i].value)) {
				tabs.setSelectedIndex(i);
				break;
			}
		}
	}

	private boolean reTabWhenNEcessary = true;

	public void setReTabOnRefresh(final boolean ok) {
		reTabWhenNEcessary = ok;
	}
	private void reTabIfNecessary() {
		reTabIfNecessary(reTabWhenNEcessary);
	}

	private boolean reTabIfNecessary(final boolean reTabWhenNecessary) {
		if (reTabWhenNecessary) {
			final int dataColumnIndex = getTabbedDataColumnIndex();
			if (dataColumnIndex >= 0 && !isReTabPending) {
				isReTabPending = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reTabIfNecessary(dataColumnIndex);
					}
				});
				return true;
			}
		}
		return false;
	}

	public void closeTabsIfNecessary() {
		final int currentDataColumnIndex = getTabbedDataColumnIndex();
		if (currentDataColumnIndex >= 0) {
			tabs.close();
		}
	}

	public void setTabs(final int requiredDataColumnIndex) {
		final int currentDataColumnIndex = getTabbedDataColumnIndex();
		if (currentDataColumnIndex != requiredDataColumnIndex) {
			if (currentDataColumnIndex >= 0) {
				tabs.close();
			}
			activateTabs(requiredDataColumnIndex, false);
		}
	}

	public void requestFocusLater() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				requestFocus();
			}
		});
	}

	public void requestFocus() {
		if (!FocusFreeze.isFrozen()) {
			int type = getModelType();
			if (type == TYPE_GROUP_SEIVED) {
				table.requestFocus();
			} else if (type == TYPE_GROUPED) {
				((GroupedDataSource) dataSource).tree.requestFocus();
			} else {
				table.requestFocus(); // default type == TYPE_UNCONDENSED
			}
		}
	}

	public void setFocusOnFirstCellLater() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!FocusFreeze.isFrozen()) {
					int type = getModelType();
					if (type == TYPE_GROUPED) {
						((GroupedDataSource) dataSource).tree.requestFocus();
					} else {
						if (table.getRowCount() > 0) {
							table.removeRowSelectionInterval(0, table.getRowCount() - 1);
							if (showFilterUI && table.getRowCount() > 2) {
								table.addRowSelectionInterval(2, 2);					
								table.scrollToVisible(2, 2);
							}
							else {
								table.addRowSelectionInterval(0, 0);
								table.scrollToVisible(0, 0);
							}	
						}
						else {
							table.requestFocus();
						}
					}
				}		
			}
		});		
	}
	
	private void setColumnDisplayOrder(final String property) {
		if (table != null) {
			final TableColumnModel tcm = table.tcm;
			final int n = tcm.getColumnCount();
			final ArrayList al = new ArrayList();
			for (int i = 0; i < n; i++) {
				final TableColumn tc = tcm.getColumn(i);
				final String name = modelColumnIdentifiers.get(tc
						.getModelIndex());
				if (!name.equals("null")) {
					al.add(name);
				}
			}
			properties.setProperty(getPropertyName(property), Basics
					.urlEncode(al));
			notifyViewChanged();
		}
	}

	static int[] merge(final int[] a, final int[] b) {
		if (Basics.isEmpty(a)) {
			return b;
		}
		final ArraySetWithoutNulls<Integer> c = new ArraySetWithoutNulls<Integer>();
		for (final int i : a) {
			c.add(i);
		}
		for (final int i : b) {
			c.add(i);
		}
		final int[] value = new int[c.size()];
		for (int i = 0; i < c.size(); i++) {
			value[i] = c.get(i);
		}
		return value;
	}

	public int[] getEditMoreView(final int[] preferred) {
		return merge(preferred, getColumnDisplayOrder(PROPERTY_EDIT_MORE_VIEW));
	}

	public int[] getEditLessView(final int[] preferred) {
		return getColumnDisplayOrder(PROPERTY_EDIT_LESS_VIEW);
	}

	public int[] getEditMoreView(final int[] preferred, final String prefix) {
		return merge(preferred, getColumnDisplayOrder(true, PROPERTY_EDIT_MORE_VIEW, false, prefix));
	}

	public int[] getEditLessView(final int[] preferred, final String prefix) {
		return getColumnDisplayOrder(true, PROPERTY_EDIT_LESS_VIEW, false, prefix);
	}
	
	private int[] getColumnDisplayOrder(final String property) {
		return getColumnDisplayOrder(true, property, false);
	}
	
	private int[] getColumnDisplayOrder(final boolean getAllByDefault, final String property, final boolean useRootAsDefault) {
		return getColumnDisplayOrder(getAllByDefault, property, useRootAsDefault, null);
	}
	
	private int[] getColumnDisplayOrder(final boolean getAllByDefault, final String property, final boolean useRootAsDefault, final String prefix) {
		List<String> v = null;
		final String s;
		if (useRootAsDefault) {
			s=getPropertyUsingRootTableModelAsDefault(property);
			
		} else if (prefix==null) {
			s=properties.getProperty(getPropertyName(property));
		} else {
			s=properties.getProperty(prefix+property);
		}
		if (Basics.isEmpty(s)) {
			if (getAllByDefault){
				v = metaRow.cloneDataColumnIdentifiers();
			} else {
				return new int[]{};
			}
		} else {
			v = Basics.urlDecode(s);
			if (!metaRow.containsAllIdentifiers(v)) {
				final ArrayList<String> al = new ArrayList();
				for (final String ss : v) {
					if (metaRow.indexOf(ss) >= 0) {
						al.add(ss);
					}
				}
				v = al.size() > 0 ? al : metaRow.cloneDataColumnIdentifiers();
			}
		}
		final int[] a = new int[v.size()];
		for (int i = 0; i < v.size(); i++) {
			final String name = v.get(i);
			a[i] = metaRow.indexOf(name);
		}
		return a;
	}

	private JMenuItem saveAsEditMoreView, saveAsEditLessView,
			restoreEditLessView, restoreEditMoreView;

	private final static String PROPERTY_EDIT_MORE_VIEW = "editMoreView",
			PROPERTY_EDIT_LESS_VIEW = "editLessView";

	public void supportEditViewOrders() {
		saveAsEditMoreView = new JMenuItem("Save as edit more view", MmsIcons.getWideBlankIcon());
		saveAsEditMoreView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setColumnDisplayOrder(PROPERTY_EDIT_MORE_VIEW);
				notifyViewChanged();
			}
		});
		saveAsEditMoreView.setMnemonic('m');
		saveAsEditMoreView.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F10, InputEvent.ALT_MASK));
		restoreEditMoreView = new JMenuItem("Restore edit more view", MmsIcons.getWideBlankIcon());
		restoreEditMoreView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				initModelColumns(getColumnDisplayOrder(PROPERTY_EDIT_MORE_VIEW));
				table.cellHighlighter.reset();
				sortAndRepaint();
				notifyViewChanged();
			}
		});
		restoreEditMoreView.setMnemonic('m');

		saveAsEditLessView = new JMenuItem("Save as edit less view", MmsIcons.getWideBlankIcon());
		saveAsEditLessView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setColumnDisplayOrder(PROPERTY_EDIT_LESS_VIEW);
				notifyViewChanged();
			}
		});
		saveAsEditLessView.setMnemonic('l');
		saveAsEditLessView.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F10, InputEvent.SHIFT_MASK));
		restoreEditLessView = new JMenuItem("Restore edit less view", MmsIcons.getWideBlankIcon());
		restoreEditLessView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				initModelColumns(getColumnDisplayOrder(PROPERTY_EDIT_LESS_VIEW));
				table.cellHighlighter.reset();
				sortAndRepaint();
				notifyViewChanged();
			}
		});
		restoreEditLessView.setMnemonic('l');

	}

	private Collection<ActionListener> tabActivationListeners = new ArrayList<ActionListener>();

	public void addTabActivationListener(final ActionListener al) {
		tabActivationListeners.add(al);
	}

	public DisabledExplainer popupMenuButton;

	public ActionListener actionAfterMultiRowChangeOperation = null;
	public static PersonalizableTableModel multiRowChangeOperation = null;

	private void setMultiRowChangeOperationFlag(final int id, final boolean ok) {
		multiRowChangeOperation = ok ? this : null;
		if (!ok && actionAfterMultiRowChangeOperation != null) {
			actionAfterMultiRowChangeOperation.actionPerformed(new ActionEvent(
					this, id, "postMultiRowChangeOperation"));
			actionAfterMultiRowChangeOperation = null;
		}
		getUngroupedModel().dataSource.setMultiRowChangeOperationFlag(ok);
	}

	private int getRemovableSelectionCount(final int[] selected,
			int removableCount, final Set<String> problems) {

		final int n = getRowCountMinusEmptyLastRowForCreating();

		for (final int r : selected) {
			if (r == n) {
				if (problems != null){
					problems.add("Row selected is for new items");
				}
				removableCount--;
				break;
			}
			final Row row = getRowAtVisualIndex(r);
			if (row != null && getUngroupedModel().getDataSource().isTemporaryForPrimaryKeySelecting(row)) {
				if (problems != null){
					problems.add("The record is brand new and only has identifier information so far.");
				}
				removableCount--;
			}
		}
		return removableCount;

	}

	private int getRemovableSelectionCount(final int[] selected, final Set<String> problems) {
		return getRemovableSelectionCount(selected,
				getSelectedDataRowCount(selected), problems);
	}

	public String getDisabledAnomalyIfNeitherDeleteNorRemoveCanWork() {
		String retVal = null;
		if (!isReadOnly()) {
			final PersonalizableTableModel utm = getUngroupedModel();
			final boolean bd = utm.dataSource.isDeletable(), br = utm.dataSource
					.isRemovable();
			if (bd || br) {
				final PersonalizableTableModel ms = getModelShowing();
				final int[] selected = ms.table.getSelectedRows();
				final int rowsWithData=this.getDataRowCount(selected);
				if (rowsWithData==0) {
					retVal=MSG_NOTHING_IS_SELECTED;
				} else if (selected.length>0){
				if (bd){
						retVal=ms.getDeletableAnomalies(selected);
						if (retVal==null){ // delete works allow button to be enabled even if remove does not
							return null;
						}
				}
				if (br) {
					final String s= ms.getRemovableAnomaly(selected, getSelectedDataRowCount(selected));
					
					if (s != null){
						if (retVal==null){
							retVal=s;
						} else {
							retVal+="<br>"+s;
						}
					} else {
						retVal=null;
					}
				}
			} else {
				retVal=MSG_NOTHING_IS_SELECTED;
			}
			}
		} else {
			retVal=entireTableIsReadOnly;
		}
		return retVal;
	}
	
	String getRemovableAnomaly(final int []selected, final int selectedDataRowCount){
		final Set<String> problems=new HashSet<String>();
		final int removableCount = getRemovableSelectionCount(selected, selectedDataRowCount, problems);
		final String value;
		if (removableCount ==0){
			value=selected.length+" selected "+ (removableCount==1?"row is":"row(s) are")+" not removable because"+Basics.toUlHtml(problems);			
		} else {
			value=null;
		}
		return value;
	}
	
	public int getDeletableSelectionCount() {
		return getDeletableSelectionCount(table.getSelectedRows());
	}

	private int getRemovableSelectionCount() {
		return getRemovableSelectionCount(table.getSelectedRows(), null);
	}

	private String getSortTxt(final SortInfo[] sortOrder,
			final int highlightColumn, final String delimiter) {
		final StringBuilder sb = new StringBuilder();
		final int n = sortOrder.length;
		for (int i = 0; i < n; i++) {
			final SortInfo si = sortOrder[i];
			if (i > 0) {
				sb.append(delimiter);
			}
			encode(sb, si, highlightColumn == si.dataColumnIndex);
			if (hasCustomSortSequence(si.dataColumnIndex)) {
				sb.append("<small> (custom sort sequence)</small>");
			}
		}
		return sb.toString();
	}

	private class ViewReSyncer {
		int tabHides, treeHides, controlPanelHides, filterGroupHides;
		int findSeeOnlyCompanionTableHides, findSeeOnlyRootTableHides;

		private boolean delay = false;

		private void clear() {
			if (!delay) {
				tabHides = 0;
				treeHides = 0;
				controlPanelHides = 0;
				filterGroupHides = 0;
				findSeeOnlyRootTableHides = 0;
				findSeeOnlyCompanionTableHides = 0;
				hides.clear();
				controlPanelHidden.clear();
				criteriaFailureDescriptions.clear();
			}
		}

		final Collection<Row> hides = new ArrayList<Row>(),
				controlPanelHidden = new ArrayList<Row>();
		private final Collection<String> criteriaFailureDescriptions=new ArrayList<String>();

		private Object getMemorableValue(PersonalizableTableModel utm, final Row row, final int dataColumnIndex){
			Object value=row.getFilterableValue(dataColumnIndex);
			if (Basics.isEmpty(value) && utm.dataSource.isPartOfPrimaryKey(dataColumnIndex)){
				final GroupedDataSource.Node node=utm.getHighestCommonNodeOtherThanRoot();
				if (node != null){
					value=node.get(dataColumnIndex);
				}
			}
			return value;
		}
		Object rememberedPriorTreeValue = null;

		void rememberPriorTreeValue(final Row row, final int dataColumnIndex) {
			if (row != null) {
				final boolean isInTreeMode = isGrouped();
				if (isInTreeMode) {
					final PersonalizableTableModel utm = getUngroupedModel();
					if (Basics.contains(utm.groupedDataSource.tableColumns,
							dataColumnIndex)) {
						rememberedPriorTreeValue = getMemorableValue(utm, row, dataColumnIndex);
						return;
					}
				}
			}
			rememberedPriorTreeValues = null;
		}

		Object[] rememberedPriorTreeValues = new Object[0];

		void rememberPriorTreeValues(final Row row) {
			final boolean isInTreeMode = isGrouped();
			if (isInTreeMode && row != null) {
				final PersonalizableTableModel utm = getUngroupedModel();
				if (utm.groupedDataSource != null) {
					final int[] treeColumns = getSort(utm.groupedDataSource.sortInfo);
					rememberedPriorTreeValues = new Object[treeColumns.length];
					for (int i = 0; i < treeColumns.length; i++) {
						rememberedPriorTreeValues[i] = getMemorableValue(utm, row, treeColumns[i]);
					}
				}
			} else {
				rememberedPriorTreeValues = new Object[0];
			}
		}

		private Row lastTolerated;

		private void detectNeedToResync(final Row row,
				final int dataColumnIndex, final boolean creatingFromPopup) {
			if (dataSource.doFilterRemovalWhenRefiltering() && !row.equals(ignoreResync)) {
				if (!row.equals(lastTolerated)) {
					lastTolerated = null;
					boolean hidden = false;
					final boolean isInTreeMode = isGrouped();
					final PersonalizableTableModel utm = getUngroupedModel();
					if (isInTreeMode && utm.groupedDataSource != null) {
						final int[] treeColumns = utm.groupedDataSource.tableColumns;
						if (dataColumnIndex >= 0) {
							if (Basics.contains(treeColumns, dataColumnIndex)) {
								final Object value=row.getFilterableValue(dataColumnIndex);
								if (!Basics.equals(rememberedPriorTreeValue,value)) {
									if (!Basics.isEmpty(rememberedPriorTreeValue)|| !Basics.isEmpty(value)){
										treeHides++;
										hidden = true;
									}
								}
							}
						} else if (rememberedPriorTreeValues.length == treeColumns.length) {
							for (int i = 0; i < rememberedPriorTreeValues.length; i++) {
								Object o1 = rememberedPriorTreeValues[i];
								if (Basics.isEmpty(o1) && creatingFromPopup) { // did
																				// user
																				// pick
																				// high
																				// in
																				// the
																				// tree
																				// when
																				// creating?
									for (i++; i < rememberedPriorTreeValues.length; i++) {
										o1 = rememberedPriorTreeValues[i];
										if (!Basics.isEmpty(o1)) {
											break;
										}
									}
									if (i >= rememberedPriorTreeValues.length) {
										break;
									}
								}
								final Object o2 = row
										.getFilterableValue(treeColumns[i]);
								if (!Basics.equals(o1, o2)) {
									treeHides++;
									hidden = true;
									break;
								}
							}
						} else {
							treeHides++;
							hidden = true;
						}
					}
					boolean tabHidden = utm.isHiddenByTab(row);
					if (tabHidden) {
						tabHides++;
						hidden = true;
					}
					if (!tabHidden
							&& utm.dataSource.describeSelectedCriteriaFailures(row, true, criteriaFailureDescriptions)) {
						filterGroupHides++;
						//utm.dataSource.isHiddenByActiveFilters(row);
						controlPanelHidden.add(row);
						hidden = true;
					}
					else if (!tabHidden
							&& utm.dataSource.describeSelectedCriteriaFailures(row, false, criteriaFailureDescriptions)) {
						controlPanelHides++;
						//utm.dataSource.isHiddenByActiveFilters(row);
						controlPanelHidden.add(row);
						hidden = true;
					}
					if (utm.isHiddenBySeeOnlySettings(row)) {
						findSeeOnlyRootTableHides++;
						hidden = true;
					}
					if (isInTreeMode && isHiddenBySeeOnlySettings(row)) {
						findSeeOnlyCompanionTableHides++;
						hidden = true;
					}
					if (hidden) {
						hides.add(row);
					}
				}
			}
			ignoreResync=null;
		}

		private boolean lastAnswer = true;

		private boolean reSyncIfNeededAndPermitted(
				final boolean doingBigRefreshSoon, final Row created) {
			final boolean creatingFromPopup = created != null;
			boolean previousTreeIsGone = false;
			if (!delay) {
				final int n = treeHides + tabHides
						+ findSeeOnlyCompanionTableHides
						+ findSeeOnlyRootTableHides + controlPanelHides + filterGroupHides;
				if (n > 0) {
					final PersonalizableTableModel utm = getUngroupedModel();
					final boolean isInTreeMode = isGrouped();
					final StringBuilder sb = new StringBuilder("<html>");
					final int nn = hides.size();
					if (nn > 1) {
						sb.append(nn);
						sb.append(" items are");
					} else {
						sb.append("This item is");
					}
					sb.append(" inconsistent with this table's current:<ul>");
					final StringBuilder settings = new StringBuilder();
					// establish remedy question
					final StringBuilder problem = new StringBuilder();
					if (!doingBigRefreshSoon && treeHides > 0) {
						problem.append("<li>navigation tree <ol><li>");
						problem.append(getSortTxt(
								utm.groupedDataSource.sortInfo, -1, "<li>"));
						problem.append("</ol>");
					}
					if (tabHides > 0) {
						problem.append("<li>tab selection <i>");
						final int dc = utm.getTabbedDataColumnIndex();
						problem.append(utm.getColumnAbbreviation(dc));
						problem.append("=<b>\"");
						final int idx = utm.tabs.getSelectedIndex();
						problem.append(Basics.stripSimpleHtml(utm.tabs
								.getTitleAt(idx)));
						problem.append("\"</b></i>");
					}
					if (findSeeOnlyCompanionTableHides > 0) {
						problem
								.append("<li>query settings in the \"companion\" table");
					}
					if (findSeeOnlyRootTableHides > 0) {
						problem
								.append("<li>query settings in the \"root\" table");
					}
					if (filterGroupHides > 0) {
						if (Basics.isEmpty(criteriaFailureDescriptions)) {
							return false;
						}
						problem.append("<li>visibility settings<br>(");
						problem.append(Basics.toString(criteriaFailureDescriptions.toArray(), 4));
						problem.append(")");
					}
					else if (controlPanelHides > 0) {
						if (Basics.isEmpty(criteriaFailureDescriptions)) {
							return false;
						}
						problem.append("<li>control panel settings<br>(");
						problem.append(Basics.toString(criteriaFailureDescriptions.toArray(), 4));
						problem.append(")");
					}
					sb.append(problem.toString());
					sb
							.append("</ul><br>Do you want to restore a view that keeps this item <br>both consistent and visible?</html>");
					lastAnswer=viewRefresh.ask(getTable(), sb.toString());
					if (lastAnswer) {
						if (findSeeOnlyCompanionTableHides > 0
								&& findSeeOnlyRootTableHides < 1
								&& treeHides < 1 && tabHides < 0
								&& controlPanelHides < 0 && filterGroupHides < 0) {
							clearSeeOnlySettings();
							if (!doingBigRefreshSoon && !FocusFreeze.isFrozen()) {
								table.requestFocus();
							}
							if (!doingBigRefreshSoon && creatingFromPopup) {
								resetAndSelectAfterCreatingLater(hides);
							}
						} else {
							if (controlPanelHides > 0 || filterGroupHides > 0) {
								for (final Row row : controlPanelHidden) {
									utm.dataSource.adjustActiveFilters(row);
								}
							}
							final int tabDataColumnIndex;
							if (!doingBigRefreshSoon && tabHides > 0) {
								tabDataColumnIndex = utm
										.getTabbedDataColumnIndex();
								utm.tabs.dispose();
								previousTreeIsGone = true;
							} else {
								tabDataColumnIndex = -1;
							}
							if (isInTreeMode && tabHides == 0) {
								if (findSeeOnlyCompanionTableHides > 0) {
									clearSeeOnlySettings();
								}
								if (!doingBigRefreshSoon) {
									previousTreeIsGone = true;
									ungroupIfNecessary();
								}
							}
							if (findSeeOnlyRootTableHides > 0) {
								utm.clearSeeOnlySettings();
							} else if (!doingBigRefreshSoon) {
								utm.refreshShowingTable(true);
							}
							if (!doingBigRefreshSoon && isInTreeMode
									&& tabHides == 0) {
								utm.group();
							}
							if (!doingBigRefreshSoon) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										utm.reselect(hides, tabDataColumnIndex);
									}
								});
							}
						}
						utm.notifyViewChanged();
					} else {
						if (hides.size() == 1) {
							lastTolerated = hides.iterator().next();
						}
						if (!FocusFreeze.isFrozen()) {
							table.requestFocus();
						}
						final Component c = table.getEditorComponent();
						if (c instanceof JComponent) {
							final JComponent jc = (JComponent) c;
							jc
									.setToolTipText(Basics.concatHtmlUncentered(Basics.NOTE_BOLDLY, "the current item will remain hidden."));
							ToolTipOnDemand.getSingleton().showLater(jc);
						}
						if (!doingBigRefreshSoon && creatingFromPopup) {
							resetAndSelectAfterCreatingLater(hides);
						}
					}
				} else if (!doingBigRefreshSoon && creatingFromPopup) {
					final Collection<Row> creations = new ArrayList<Row>();
					creations.add(created);
					resetAndSelectAfterCreatingLater(creations);
				}
			}
			return previousTreeIsGone;
		}
	}

	private ViewReSyncer vr = new ViewReSyncer();
	private boolean resyncingViewsAlready = false;

	public void rememberPriorTreeValues(final Row row) {
		vr.rememberPriorTreeValues(row);
	}

	public boolean reSyncViewsToChanges(final Row row, final int dataColumnIndex) {
		return reSyncViewsToChanges(row, dataColumnIndex, false, false);
	}

	private boolean allowResync = true;

	public void setResyncWithChanges(final boolean ok) {
		allowResync = ok;
	}

	public boolean reSyncViewsToCreation(final Row row) {
		return reSyncViewsToChanges(row, -1, false, true);
	}

	private Row ignoreResync=null;
	public void setIgnoreResync(final Row r){
		ignoreResync=r;
	}
	boolean reSyncViewsToChanges(final Row row, final int dataColumnIndex,
			final boolean bigRefreshComingSoon, final boolean creatingFromPopup) {
		boolean ok = false;
		if (getUngroupedModel().allowResync && !resyncingViewsAlready) {
			resyncingViewsAlready = true;
			vr.clear();
			vr.detectNeedToResync(row, dataColumnIndex, creatingFromPopup);
			ok = vr.reSyncIfNeededAndPermitted(bigRefreshComingSoon,
					creatingFromPopup ? row : null);
			resyncingViewsAlready = false;
		}
		return ok;
	}

	boolean isHiddenByTab(final Row row) {
		boolean retVal = false;
		if (tabs != null) {
			final int dc = getTabbedDataColumnIndex();
			final int idx = tabs.getSelectedIndex();
			final Object value = row.getFilterableValue(dc);
			retVal = !Basics.equals(tabs.tabs[idx].value, value);
		}
		return retVal;
	}

	boolean isHiddenBySeeOnlySettings(final Row row) {
		boolean retVal = false;
		if (dataSource.isFilterable()) {
			if (hasSeeOnlySettings()) {
				retVal = !DefaultFilterable.isFiltered(row, filteringRow,
						filterable);
			}
		}
		return retVal;
	}

	public void delayReSyncResolution() {
		vr.clear();
		vr.delay = true;
	}

	boolean undelayReSyncResolution(final boolean resolve) {
		return undelayReSyncResolution(resolve, false);
	}

	public boolean undelayReSyncResolution(final boolean resolve,
			final boolean doingBigRefreshSoon) {
		boolean ok = false;
		vr.delay = false;
		if (resolve) {
			ok = vr.reSyncIfNeededAndPermitted(doingBigRefreshSoon, null);
		} else {
			vr.clear();
		}
		return ok;
	}

	void selectAndFocus(final Collection<Row> hides) {
		selectAndFocus(hides, false);
	}
	void selectAndFocus(final Collection<Row> hides, final boolean _additive) {
		scrollOnce = 0;
		final int mode = table.getSelectionModel().getSelectionMode();
		final boolean additive = mode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		initializingPicks=true;
		if (!_additive){
			table.clearSelection();
		}
		int i=0, n=hides.size();
		for (final Row row : hides) {
			if (i++==n-1){
				initializingPicks=false;
			}
			select(row, true, false, additive);
		}
		initializingPicks=false;
		if (!FocusFreeze.isFrozen()) {
			table.requestFocus();
		}

	}

	public void reselect(final Collection<Row> changedRows) {
		reselect(changedRows, getTabbedDataColumnIndex());
	}

	public void reselect(final Collection<Row> changedRows, final boolean additive) {
		reselect(changedRows, getTabbedDataColumnIndex(), additive);
	}

	void reselect(final Collection<Row> changedRows,
			final int tabDataColumnIndex) {
		reselect(changedRows, tabDataColumnIndex, false);
	}
	void reselect(final Collection<Row> changedRows,
			final int tabDataColumnIndex, final boolean additive) {
		if (!Basics.isEmpty(changedRows)) {
			if (changedRows != null && tabDataColumnIndex >= 0) {
				final Object value = changedRows.iterator().next()
						.getFilterableValue(tabDataColumnIndex);
				if (tabs == null) {
					activateTabs(tabDataColumnIndex, false);
				}
				for (int i = 0; i < tabs.tabs.length; i++) {
					final PersonalizableTableModel.Tabs.Tab tab = tabs.tabs[i];
					if (Basics.equals(value, tab.value)) {
						tabs.setSelectedIndex(i);
						break;
					}
				}
			}
			if (isGrouped()) {
				final PersonalizableTableModel um = getUngroupedModel();
				if (um != null && um.groupedDataSource != null
						&& um.groupedDataSource.tree != null) {
					boolean was = um.autoSelectTableFromTree;
					um.autoSelectTableFromTree = false;
					if (!additive){
						um.groupedDataSource.tree.clearSelection();
					}
					for (final Row row : changedRows) {
						um.selectInTree(row, true);
					}
					um.autoSelectTableFromTree = was;
				}
			}
			getModelShowing().selectAndFocus(changedRows, additive);
		}
	}

	public final Collection<Row> getSelectedRowsInTable() {
		final Collection<Row> c = new ArrayList<Row>();
		int[] selected = table.getSelectedRows();
		int n = getSelectedDataRowCount(selected);
		if (n > 0) {
			final java.util.List<Row> all = getFilteredDataRowsShowing();
			for (int i = n - 1; i >= 0; i--) {
				final int r = getFilteredDataRowIndex(selected[i]);
				if (r >= 0 && r<all.size()) {
					c.add(all.get(r));
				}
			}
		}
		return c;

	}

	private Collection<String>everyWhereColumns;
	private boolean columnEveryWhereReordering=false;
	public void setColumnEveryWhereReordering(){
		this.columnEveryWhereReordering=true;
	}

	public void addColumnEveryWhere(final String columnIdentifier) {
		final Properties p=getUngroupedModel().properties;
		addColumnEveryWhere(p, columnIdentifier);
	}

	public void setColumnModelEveryWhere(final String columnModel){
		final Properties properties=getProperties();
		for (final Object o : properties.keySet()) {
			final String key = (String) o;
			if (key.endsWith(PROPERTY_TABLE_COLUMN_MODEL)) {
				properties.setProperty(key, columnModel);
			}
		}
		final List<String>l=getModelColumnIdentifiers();
		initModelColumns(l);
	}
	
	public String getColumnModel(){
		final ArrayList<String> al = new ArrayList<String>();
		final TableColumnModel tcm = table.tcm;
		final int n = tcm.getColumnCount();
		
		for (int i = 0; i < n; i++) {
			final TableColumn tc = tcm.getColumn(i);
			final String identifier = modelColumnIdentifiers.get(tc
					.getModelIndex());
			if (!identifier.equals("null")) {
				al.add(identifier);
			}
		}
		return Basics.urlEncode(al);
	}
	private void addColumnEveryWhere(final Properties p, final String columnIdentifier) {
		if (everyWhereColumns==null){
			everyWhereColumns=new LinkedHashSet<String>();
		}
		everyWhereColumns.add(columnIdentifier);		
		for (final Object o : p.keySet()) {
			final String key = (String) o;
			if (key.endsWith(PROPERTY_TABLE_COLUMN_MODEL)) {
				String value = p.getProperty(key);
				final java.util.List<String> c = Basics.urlDecode(value);
				if (columnEveryWhereReordering){
					c.remove(columnIdentifier);
				}
				if (!c.contains(columnIdentifier)) {
					c.add(columnIdentifier);
					value = Basics.urlEncode(c);
					p.setProperty(key, value);
				}
			}
		}
	}
	public void setFirstColumnEveryWhere(final String columnIdentifier) {
		setFirstColumnEveryWhere(getUngroupedModel().properties, columnIdentifier);
	}

	private void setFirstColumnEveryWhere(final Properties p, final String columnIdentifier) {
		if (everyWhereColumns==null){
			everyWhereColumns=new LinkedHashSet<String>();
		}
		everyWhereColumns.add(columnIdentifier);		
		for (final Object o : p.keySet()) {
			final String key = (String) o;
			if (key.endsWith(PROPERTY_TABLE_COLUMN_MODEL)) {
				final java.util.List<String> c = new ArrayList<String>();
				c.add(columnIdentifier);
				final String value = Basics.urlEncode(c);
				p.setProperty(key, value);
			}
		}
	}

	public boolean isTornAway() {
		return tearAwayHandler.isTornAway();
	}

	private Set<Integer>forbiddenForPrepopulating;
	public void addForbbiddenForPrepopulating(final Integer dataColumnIndex){
		if (forbiddenForPrepopulating == null){
			forbiddenForPrepopulating=new HashSet<Integer>();
		}
		forbiddenForPrepopulating.add(dataColumnIndex);
	}
	public void setFromTreeContext(final Row newRow) {
		final GroupedDataSource.Node creationContext = getUngroupedModel()
				.getHighestCommonNodeOtherThanRoot();
		if (creationContext != null ) {
			creationContext.setFromContext(newRow, forbiddenForPrepopulating);
		}
	}

	private void resetAndSelectAfterCreatingLater(
			final Collection<Row> creations) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resetAndSelectAfterCreating(creations);
			}
		});
	}

	private void resetAndSelectAfterCreating(final Collection<Row> creations) {
		if (!isTabImportOccuring()) {
			if (isGrouped()) {
				refreshAndReselect(creations);
			} else {
				final boolean resort = dataSource.resortAfterCreate();
				refreshShowingTable(false);
				final boolean wasSortNeeded = sortNeeded;
				sortNeeded = !resort && columnsToSort.size() > 0;
				if (sortNeeded && !wasSortNeeded) {
					table.getTableHeader().repaint();
				}
				selectAndFocus(creations);
			}
		}
	}

	public void setFromContext(final Row creation) {
		if (!isTabImportOccuring()) {
			final PersonalizableTableModel utm = getUngroupedModel();
			utm.setFromTabContext(creation);
			utm.setFromTreeContext(creation);

		}
	}

	public void addCreationToViewedTable(final Row creation) {
		addToCompanionTable(creation);
		addNewRowToFilterListForEditing(creation);
	}

	Collection<Row> createdInPlace = new ArrayList<Row>();

	boolean refreshIfCreatedInPlace() {
		boolean ok = false;
		if (createdInPlace.size() > 0 && isGrouped()) {
			ok = true;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getUngroupedModel().refresh(true);
					createdInPlace.clear();
				}
			});
		}
		return ok;
	}

	private void refreshAndReselect(final Collection<Row> c) {
		final PersonalizableTableModel um = getUngroupedModel();
		um.refresh(false);
		if (c.size() > 0){
			if(isGrouped()){ 
			boolean was = um.autoSelectTableFromTree;
				um.autoSelectTableFromTree = false;
				um.selectInTree(c.iterator().next(), false);
				um.getModelShowing().selectAndFocus(c);
				um.autoSelectTableFromTree = was;
			}else{
				getModelShowing().selectAndFocus(c);
			}
		}
	}

	public void deleteSelected(final int[] selected,
			final PersonalizableTableModel m, final PersonalizableTableModel utm) {
		deleteCount = selected.length;
		for (int i = selected.length - 1; i >= 0; i--) {
			final int filteredDataRowIndex = selected[i];
			if (filteredDataRowIndex > -1) { // don't delete
				// filtering row
				AtomicOpsWithProgressBar.singleton.doOp(new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						final Row row = m.getFilteredDataRowsShowing().get(
								filteredDataRowIndex);
						if (row.isDeletable()) {
							if (utm != m) {							
								final int idx = utm.dataSource
								.getFilteredDataRows().indexOf(row);
									utm.dataSource.delete(idx);		
							}	
							m.dataSource.delete(filteredDataRowIndex);						
						}
					}
				});
			}
		}
		deleteCount = 0;
        fireListSelection();

	}

	void saveEditInPlaceSetting() {
		setProperty(PROPERTY_EDIT_IN_PLACE, editInPlace);
		notifyViewChanged();
	}

	public boolean useRenderOnlyValueForTree=true;
	
	public Collection<Row>deletionHasBeenResolved=new ArrayList<Row>();
	
	public String getAddVerb(){
		final int n=getUngroupedModel().getDataSource().getMaximumCardinality();
		final int size = getUngroupedModel().getRowCount();
		return n==1 ? size == 0?"Pick":"Change":"Add";
	}
	public Icon getAddIcon(){
		final int n=getUngroupedModel().getDataSource().getMaximumCardinality();
		final int size = getUngroupedModel().getRowCount();
		return n==1 ? MmsIcons.getPageFindIcon():MmsIcons.getAddIcon();
	}
	
	public String getAddMenuText(){
		final String txt=Basics.concat(" pre-existing \"", getSingularKey(), "\" record");
		final String s;
			final int n=getUngroupedModel().getDataSource().getMaximumCardinality();
			if (n==1) {
				final int size = getUngroupedModel().getRowCount();
				if (size == 0) {
					s= Basics.concat("Pick ", txt);	
				}
				else {
					return Basics.concat("Change \"", getSingularKey(), "\"");
				}
			} else {
				s=Basics.concat("Add", txt, "s");
			}
			return Basics.concat(s," from list/tree");
	}
	
	private void reselectTreeLater(final TreePath []tp){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				System.out.println("REFRESHING  SELECT...");
				if (groupedDataSource != null) {
					int n = 1;
					for (final TreePath _tp : tp) {
						int nn = _tp.getPathCount();
						if (nn > n) {
							n = nn;
						}
					}
					final Collection<TreePath> done = new ArrayList<TreePath>(), toDo = new ArrayList<TreePath>();
					Basics.add(toDo, tp);
					findAndSelectAndExpandIfLevel(toDo, 1, done, 1);
					if (done.size() == 0 && n > 1) {
						final int nn = groupedDataSource.getRoot()
								.getChildCount();
						findAndSelectAndExpandIfLevel(toDo, 2, done, nn);
						if (done.size() < nn) {
							for (int i = 3; i <= n; i++) {
								findAndSelectAndExpandIfLevel(toDo,
										i, done, -1);
							}
						}
					}

				}
			}
		});
	}
	
	boolean buildTreeFromDropDownListOfColumns=false;
	
	void buildTreeByDraggingAndDroppingColumns(){
		getUngroupedModel().buildTreeFromDropDownListOfColumns=false;
		getUngroupedModel().newTree();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				groupedDataSource.pickRoot();
			}
		});
	}
	
	
	public static Color 
	gold=new Color(212,224,53),cyan=Color.cyan,blue=new Color(0,0,202), lightGreen=new Color(228, 254, 204),
	defaultBlue=new Color(49, 106, 197), midBlue=new Color(0,102,204), lightBlue=new Color(22,144,210);
	private static Color [][] colors=new Color[][]{
		{Color.white, 	blue},
		{Color.white, midBlue},
		{Color.white, lightBlue},
		
		
		{Color.yellow, blue},
		{Color.yellow, midBlue},
		{Color.yellow, lightBlue},
		{Color.yellow, defaultBlue},

		{gold, blue},
		{gold, midBlue},
		{gold, lightBlue},
		{gold, defaultBlue},

		{Color.black, lightBlue},
		
		{cyan, blue},
		{cyan, midBlue},
		{cyan, lightBlue},
		{cyan, defaultBlue},
		
		{lightGreen, blue},
		{lightGreen, midBlue},
		{lightGreen, lightBlue},
		{lightGreen, defaultBlue},
		
		
	};

	private Counter<Color[]> colorCount=new Counter<Color[]>();
	{
		clearStaticColors();
	}
	
	private void clearStaticColors()	{
		for (final Color[] _colors : colors) {
			colorCount.put(_colors, 0);
		}
	}
    
	private final Map<GroupedDataSource.Node, Color[]> nodeByColor=new HashMap<GroupedDataSource.Node, Color[]>();
	private Color []selectionColor;
	Color[] getSelectionColorsBasedOnSelectionsInActiveTree(final GroupedDataSource.Node node, final Row row, final Color foreground, final Color background, final boolean dither){
    	int deviationFromStandard=0;
    	if (selectionColor==null){
    		selectionColor=new Color[]{foreground, background};
    	} else {
    		selectionColor[0]=foreground;
    		selectionColor[1]=background;
    	}
    	GroupedDataSource.Node[] selectedNodes=null; 
    	GroupedDataSource.Node picked=null;
    	if (groupedDataSource!=null && !groupedDataSource.hasApplicationSpecificTreeSort ){
    		selectedNodes=getUngroupedModel().getSelectedNodes();
    		if (selectedNodes.length > 1) {    			
				for (int i = 0; i < selectedNodes.length; i++) {
					final GroupedDataSource.Node n = selectedNodes[i];
					if (node != null ){
						if(!node.equals(n)){
							continue;
						}
						deviationFromStandard = 1 + i;
						picked=n;
					}
					if (n != null && n.groupedRow  != null && n.groupedRow.isFiltered(row, n.sortIndexThatDiffers)) {
						if (deviationFromStandard>0 ){
							if(!selectedNodes[deviationFromStandard-1].isNodeChild(n)){
								continue;
							}
						}
						deviationFromStandard = 1 + i;
						picked=n;
					}
				}
    		}
    	}
    	if (deviationFromStandard > 0) {
    		if (dither) {
				final Color c1 = background;
				final int d = deviationFromStandard * 16, 
				r = rgbRange(c1 .getRed() + d), 
				g = rgbRange(c1.getGreen() + d), 
				b = rgbRange(c1.getBlue() + d), 
				alpha = rgbRange(c1.getAlpha() - d);
				final Color bg = new Color(r, g, b, alpha);
	    		if (row instanceof GroupedDataSource.MetaRow.Row){
	    			return  new Color[]{Color.yellow, bg};
	    		}
	    		return new Color[]{Color.yellow, bg};
			} else {
				final Collection<GroupedDataSource.Node>set=new ArrayList(nodeByColor.keySet());
				for (final GroupedDataSource.Node n:set){
					if (!Basics.contains(selectedNodes,n)){
						final Color[]col=nodeByColor.remove(n);
						colorCount.decrement(col);
					}
				}				
				Color []chosen=nodeByColor.get(picked);
				if (chosen==null){
						final TreeMap<Integer,Color[]> tm=colorCount.getLowToHighCount();
						final int ii=tm.keySet().iterator().next();
						chosen=tm.get(ii);
					
					nodeByColor.put(picked, chosen);
					colorCount.count(chosen);					
				} 
				return chosen;
    		}
		} else if (nodeByColor!=null && selectedNodes != null && selectedNodes.length==1){
			nodeByColor.clear();
			nodeByColor.put(selectedNodes[0],selectionColor);
			colorCount.put(selectionColor, 1);
			clearStaticColors();
		}
    	return selectionColor;
	}
	
	static int rgbRange(final int input){
		if (input<0){
			return 255-((0-input)%255);
		} else if (input > 255){
			return 0+(input%255);
		}
		return input;		
	}
	
	
	public int getDataColumnCount(){
		return metaRow.size();
	}
	
	public class Column implements Comparable{
		final int dataColumnIndex;
		
		public Column(final int dataColumnIndex){
			this.dataColumnIndex=dataColumnIndex;
			si = findSortInfo(dataColumnIndex);
			isCustom = hasCustomSortSequence(dataColumnIndex);
			if(si!=null)
				isAscending=si.ascending;
		}
		
		public String getLabel(){
			return getColumnAbbreviation(dataColumnIndex);
		}
		
		public String getIdentifier(){
			return getDataColumnIdentifier(dataColumnIndex);
		}
		
		public String toString(){
			return getLabel();
		}
		final SortInfo si;
		public boolean isAscending=true;
		private boolean isCustom ;
		
		public void set(final JLabel l) {
			
				if (isAscending) {
					if (isCustom) {
						l.setIcon(MmsIcons.getSortCustomAscendingIcon());
					} else {
						l.setIcon(MmsIcons.getSortAscendingIcon());
					}
				} else {
					if (isCustom) {
						l.setIcon(MmsIcons.getSortCustomDescendingIcon());
					} else {
						l.setIcon(MmsIcons.getSortDescendingIcon());
					}
				}
			
			l.setText(getLabel());
		}
		
	    public int compareTo(final Object that){
	    	return that instanceof Column?
	    			getLabel().compareTo(((Column)that).getLabel()):
	    				-1;
	    }
	    
	    public int hashCode(){
	    	return dataColumnIndex;
	    }
	    
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Column)) {
				return false;
			}
			return dataColumnIndex == o.hashCode();
		}
		private JDialog dlg;
		private JCheckBox cbAscending,cbCustom;
		private JLabel icon;
		public void alterSort(final Component parent){
			
			dlg=SwingBasics.getDialog(parent);
			icon=new JLabel();
			cbAscending=new JCheckBox("Ascending order?");
			cbCustom=new JCheckBox("Custom sequence?");
			cbAscending.addActionListener(new ActionListener(){
				public void actionPerformed(final ActionEvent e) {
					final boolean b1=cbAscending.isSelected(), b2=cbCustom.isSelected();
					PersonalizableTable.setIcon(icon, b1, b2);
					isAscending=b1;
				}				
			});
			cbAscending.setSelected(isAscending);
			cbCustom.setSelected(isCustom);
			PersonalizableTable.setIcon(icon, isAscending,isCustom);
			action=new ActionListener(){

				public void actionPerformed(final ActionEvent e) {
					if (cbCustom.isSelected()){
					alterSortSequence(parent, dataColumnIndex, false, false);
					isCustom=hasCustomSortSequence(dataColumnIndex);
					if (!isCustom){
						cbCustom.removeActionListener(action);
						cbCustom.setSelected(false);
						cbCustom.addActionListener(action);
					}
					} else {
						removeSortSequence(getDataColumnIdentifier(dataColumnIndex));
						isCustom=false;
					}
				}
				
			};
			cbCustom.addActionListener(action);
			final JPanel jp=new JPanel(new BorderLayout(1,4));
			jp.setBorder(BorderFactory.createEmptyBorder(4,2,2,4));
			dlg.setTitle("Sort options..");
			final JLabel jl=new JLabel("<html><b>Specify sort options for \"<i>"+toString()+"</i>\"");
			jl.setHorizontalAlignment(JLabel.CENTER);
			jp.add(jl,BorderLayout.NORTH);
			final JPanel jp2=new JPanel();
			jp2.add(icon);
			jp2.add(cbAscending);
			jp2.add(cbCustom);
			jp2.add(SwingBasics.getDoneButton(dlg));
			jp.add(jp2, BorderLayout.SOUTH);
			dlg.add(jp);
			dlg.setModal(true);
			SwingBasics.packAndPersonalize(dlg, "tableColumnSort");
			dlg.setVisible(true);
		}
		private ActionListener action;
	
	}
	
	public Column getColumn(final int dataColumnIndex){
		return new Column(dataColumnIndex);
	}
	
	public Column getColumn(final String dataColumnIdentifier){
		return new Column(metaRow.indexOf(dataColumnIdentifier));
	}
	
	public Collection<Column> getShowColumns(final Collection<String>exclusions){
		final PersonalizableTableModel m=getModelShowing();
		final int n=m.getColumnCount();
		final Collection<Column>c=new ArrayList<Column>();
		for (int visualColumnIndex=0;visualColumnIndex<n;visualColumnIndex++){
			final int dataColumnIndex=getDataColumnIndexFromVisualIndex(visualColumnIndex);
			final String id=getDataColumnIdentifier(dataColumnIndex);
			if (!exclusions.contains(id)){
				c.add(getColumn(dataColumnIndex));
			}
		}
		return c;
	}
	
	private Object _getValueAt(final int visualRowIndex, final int dataColumnIndex){
		if (visualRowIndex < 0 || dataColumnIndex < 0) {
			return null;
		}
		final Row row = getRowAtVisualIndex(visualRowIndex);
		final Object value = row == null ? null : row.get(dataColumnIndex);
		return value;
	}

	class ColumnFreezer {
		
		class MyMouseAdapter extends MouseAdapter{		
				private Cursor cu=rowHeaderTable.getCursor();
		        private int draggedDataColumnIndex=-1,draggedVisualColumnIndex=-1;
		        public void mouseEntered(final MouseEvent e) {
		        	if (table.maybeColumnsAreMoving){
		        		( (JComponent)e.getSource()).setCursor(DragSource.DefaultMoveDrop);
		        		draggedDataColumnIndex=clickedDataColumnIndex;
		        		draggedVisualColumnIndex=clickedVisualColumnIndex;
		        	}
		        }

		        public void mouseExited(final MouseEvent e) {
		        	( (JComponent)e.getSource()).setCursor(cu);
					draggedVisualColumnIndex = draggedDataColumnIndex = -1;
		        }


				
				void handleDropIfNecessary(){
					if (draggedDataColumnIndex >= 0) {
						rowHeaderTable.setCursor(cu);
						if (PopupBasics.ask(getWindow(), "<html>Add \"<i>"+getColumnAbbreviation(draggedDataColumnIndex)+"</i>\" to row header?</html>")){								
							setView(draggedDataColumnIndex);
							inner.notifyViewChanged();
						}
						table.maybeColumnsAreMoving=false;
						draggedVisualColumnIndex = draggedDataColumnIndex = -1;
					}
				}
		}
		
		MyMouseAdapter mouseAdapter;
		private final PersonalizableTableModel inner = PersonalizableTableModel.this;
		JTable rowHeaderTable;
		private AbstractTableModel outer;
		private ArrayList<Integer>l=new ArrayList<Integer>(), w=new ArrayList<Integer>();
		
		private void setView(final int dataColumnIndex){
			add(dataColumnIndex);
			setView(false);
			setRowHeaderProperties();
			notifyViewChanged();
		}
		
		private boolean add(final int dataColumnIndex){
			if (dataColumnIndex >= 0 && !l.contains(dataColumnIndex)) {
				l.add(dataColumnIndex);
				int width = inner.getWidth(dataColumnIndex);
				if (width < 1) {
					width = inner.getColumnWidth(dataColumnIndex);
				}
				if (width > 0) {
					inner.setProperty(Basics.concat(inner.metaRow
							.getDataColumnIdentifier(dataColumnIndex),
							 ".",  PROPERTY_HEADER_WIDTH), width);

				}
				w.add(width);
				return true;
			}
			return false;
		}
		
		private void setView(final List<String> identifiers){
			l.clear();
			w.clear();
			int i=0;
			while (i<identifiers.size()){
				add(inner.getDataColumnIndexForLabel(identifiers.get(i)));
				i++;
			}
			setView(identifiers.size()==0);
		}
		private Color separatorColor=new Color(.81f, .81f, .9f);
		private void setView(){
			final int[] a = inner.getSelectedModelColumns(true);
			if (a.length > 0) {
				for (final int i:a){
					add(inner.getDataColumnIndex(i));					
				}
				setView(false);
				setRowHeaderProperties();
				inner.notifyViewChanged();
			}			
		}
		
		final boolean resizePane=true;
		
		private void setView(final boolean thaw) {
			JViewport jv = null;
			if (!thaw)  {
				if (l.size()<1){
					return;
				}
				outer = new AbstractTableModel() {
					public String getColumnName(final int viewCol) {
						final int dataCol=l.get(viewCol);
						return inner.getColumnLabel(dataCol);
					}

					public int getColumnCount() {
						final int n = l.size();
						return n;
					}

					public int getRowCount() {
						final int n = inner.getRowCount();
						return n;
					}

					public Object getValueAt(int visualRowIndex, int modelCol) {
						final int dataCol=l.get(modelCol);
						final Object o=inner._getValueAt(visualRowIndex, dataCol);
						return o;
					}
				};
				final TableColumnModel columnModel = new DefaultTableColumnModel() {
					int n = 0;

					public void addColumn(final TableColumn tc) {
						//tc.setMaxWidth(w.get(n)+1);
						tc.setPreferredWidth(w.get(n));
						
						super.addColumn(tc);
						final int dataColumnIndex=l.get(n);
						if (inner !=null && inner.table!=null){
							final int viewColumn=inner.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
							//inner.table.getColumnModel().getColumn(viewColumn).setMaxWidth(5);
							inner.table.getColumnModel().getColumn(viewColumn).setMinWidth(5);
							inner.table.getColumnModel().getColumn(viewColumn).setPreferredWidth(5);							
						}
						n++;
					}
				};
				rowHeaderTable = new JTable(outer, columnModel){
					public int getRowHeight() {
						if (inner != null && inner.table != null){
							return inner.table.getRowHeight();
						} 
						return super.getRowHeight();
						
					}

					public int getRowMargin() {
						if (inner != null && inner.table != null){
							return inner.table.getRowMargin();
						}
						return super.getRowMargin();
					}
				};
				if (resizePane){
					rowHeaderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				}
				rowHeaderTable.setColumnSelectionAllowed(false);
				rowHeaderTable.createDefaultColumnsFromModel();
				class _Renderer  implements TableCellRenderer {					
					
					public Component getTableCellRendererComponent(
							final JTable table, final Object value,
							boolean isSelected, final boolean hasFocus,
							final int viewRow, final int rowHeaderTableViewCol) {
						isSelected=inner.table.isRowSelected(viewRow);
						final int rowHeaderTableModelCol=rowHeaderTable.convertColumnIndexToModel(rowHeaderTableViewCol);
						final int dataCol=l.get(rowHeaderTableModelCol);
						final int vCol=inner.getVisualColumnIndexFromDataColumnIndex(dataCol);
						return inner.table.getCellRenderer(viewRow, vCol).getTableCellRendererComponent(
								inner.table, value, isSelected, hasFocus, viewRow, vCol);
					}
				}
				rowHeaderTable.setDefaultRenderer(Object.class, new _Renderer());				
				jv = new JViewport();
				jv.setBackground(separatorColor);
				jv.setView(rowHeaderTable);
				final Dimension d=rowHeaderTable.getPreferredSize();
				d.width=d.width+5;
				jv.setPreferredSize(d);
				rowHeaderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					
					public void valueChanged(ListSelectionEvent e) {
						ListSelectionModel lsm=inner.table.getSelectionModel();
						final ListSelectionListener[]lsl;
						if (lsm instanceof DefaultListSelectionModel){
							lsl=((DefaultListSelectionModel) lsm).getListSelectionListeners();
						} else {
							lsl=null;
						}
						if (e.getValueIsAdjusting()){
							return;
						}
						TableBasics.fireListSelection(rowHeaderTable, lsl, e, inner.table);
					}
				});
				
				rowHeaderTable.addMouseListener(table);
				mouseAdapter=new MyMouseAdapter();
				rowHeaderTable.addMouseListener(mouseAdapter);
				rowHeaderTable.getTableHeader().addMouseListener(mouseAdapter);
				if(table != null){
					old=table.getSelectionModel();

				}
			}else{
				final TableColumnModel tcm=inner.table.getColumnModel();
				int N=l.size();
				assert(N==w.size());
				for (int i=0;i<N;i++){
					final int width=w.get(i);
					final int dataCol=l.get(i);
					final int vCol=inner.getVisualColumnIndexFromDataColumnIndex(dataCol);
					//tcm.getColumn(vCol).setMaxWidth(width*2);
					tcm.getColumn(vCol).setPreferredWidth(width);
					
				}
				l.clear();
				w.clear();
				mouseAdapter=null;
				if (table!= null && old != null){
					table.setSelectionModel(old);
				}
			}
			if (table != null && table.scrollPane != null){
				table.scrollPane.setRowHeader(jv);
				final JTableHeader h=rowHeaderTable.getTableHeader();
				h.setBackground(separatorColor);
				table.scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, h);
				final TableColumnModel tcm=rowHeaderTable.getColumnModel();
				h.setDefaultRenderer(
						table.new HeaderRenderer() {
							protected int getDataColumnIndex(
									final int v) {
								final int m=tcm.getColumn(v).getModelIndex();
								return ColumnFreezer.this.l.get(m);
							}
						});
				h.addMouseListener(new MouseAdapter(){
					public void mouseReleased(final MouseEvent e){
						if (e.getClickCount()==2){
							if (!getUngroupedModel().isInTreeBuildingMode && dataSource.getMaximumCardinality() > 1) {
								final int v=tcm.getColumnIndexAtX(e.getX()), m=tcm.getColumn(v).getModelIndex();
								final int dataColumnIndex=l.get(m);
								toggleSort(dataColumnIndex, true);
								return;
							}
						} else {
							if (resizePane){
								if ((e.getModifiersEx()&KeyEvent.SHIFT_DOWN_MASK)!=0){
									return;
								}
								ArrayList<Integer>l2=new ArrayList<Integer>(), w2=new ArrayList<Integer>();
								boolean changed=false;
								final int n=rowHeaderTable.getColumnCount();
								final TableColumnModel tcm=rowHeaderTable.getColumnModel();
								for (int i=0;i<n;i++){
									final TableColumn tc=tcm.getColumn(i);
									final int pw=tc.getPreferredWidth();
									final int mi=tc.getModelIndex();
									final int old=w.get(mi);
									l2.add(l.get(mi));
									if (old!=pw){
										w.set(tc.getModelIndex(), pw);
										changed=true;
									}
									w2.add(pw);
								}
								if (changed){
									l=l2;
									w=w2;
									setView(false);
								}
							}
						}
					}
				});
			}
		}
		
		private ListSelectionModel old;
		
		private ArrayList<String> getIdentifiers(){
			final ArrayList<String>retVal=new ArrayList<String>();
			for (final int dataColumnIndex:l){
				retVal.add(inner.getDataColumnIdentifier(dataColumnIndex));
			}
			return retVal;
		}
		
		void repaint(){
			if (rowHeaderTable!=null){
				rowHeaderTable.repaint();
				rowHeaderTable.getTableHeader().repaint();
			}
		}
		
		void updateUI(){
			if (rowHeaderTable!=null){
				rowHeaderTable.updateUI();
				rowHeaderTable.getTableHeader().updateUI();
			}
		}
	}
	
	List<String> getStrings(final String propertyName, final boolean useRootAsDefault){
		final String s;
		if (ungroupedModel == null || useRootAsDefault) {
			s = getPropertyUsingRootTableModelAsDefault(propertyName);
		} else {
			String ungroupedPrefix = ungroupedModel.dataSource
					.getPropertyPrefix();
			final String _s = ungroupedModel.properties.getProperty(
					getPropertyName(ungroupedPrefix,
							propertyName), null);
			if (_s == null) {
				ungroupedPrefix = ungroupedModel.defaultPropertyPrefix;
				s = ungroupedModel.properties.getProperty(getPropertyName(
						ungroupedPrefix, propertyName), null);
			} else {
				s = _s;
			}
		}
		if (Basics.isEmpty(s)) {
			return new ArrayList<String>();
		} else {
			return Basics.urlDecode(s);
		}
	}

	
	final ColumnFreezer columnFreezer=new ColumnFreezer();
	

	public int getWidth(final int dataColumnIndex) {
		final String name = metaRow.getDataColumnIdentifier(dataColumnIndex);
		final int visualColumnIndex = getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
		if (visualColumnIndex >= 0) {
			final TableColumn tc = table.tcm.getColumn(visualColumnIndex);
			return tc.getPreferredWidth();			
		}
		return -1;
	}
	
	public static void setRowHeaderProperties(final Properties properties,final String prefix, final java.util.List<String> c){
		properties.setProperty(Basics.concat(prefix, ".", PROPERTY_ROW_HEADER), Basics.urlEncode(c));
	}

	public static void setColumnModelProperties(final Properties properties,final String prefix, final java.util.List<String> c){
		properties.setProperty(Basics.concat(prefix, ".", PROPERTY_TABLE_COLUMN_MODEL), Basics.urlEncode(c));
	}

	private void setRowHeaderProperties(){
		final ArrayList<String>al=columnFreezer.getIdentifiers();
		if (al.size()>0){
			setProperty(PROPERTY_ROW_HEADER, Basics.urlEncode(al));
			for (int i=0;i<al.size();i++){
				final String identifier=al.get(i);
				final int w=columnFreezer.w.get(i);
				if (w>0){
					setProperty(Basics.concat(identifier, ".",PROPERTY_HEADER_WIDTH), w);
				}
			}
			} else {
				removeProperty(PROPERTY_ROW_HEADER);
			}

	}
    public boolean isNonGroupSelectionEvent(){
    	if (event==null){
    		return true;
    	}
    	return (!event.isShiftDown()
                && !((SwingBasics.usesMacMetaKey()
                      && event.isMetaDown()) || event.isControlDown()
                ));
    }
	private boolean selectionListening=true;
	public boolean setSelectionListening(final boolean ok){
		final boolean prior=selectionListening;
		selectionListening=ok;
		return prior;
	}
	void fireListSelection(final ListSelectionEvent e) {
		final PersonalizableTableModel u = getUngroupedModel();
		if (u.selectionListening) {
			Collection<ListSelectionListener> c = u.listSelectionListeners;
			if (c != null) {
				c=new ArrayList<ListSelectionListener>(u.listSelectionListeners);
				for (final ListSelectionListener lsl : c) {
					lsl.valueChanged(e);
				}
			}
		}
	}
	
	public void removeListSelectionListeners(){
		PersonalizableTableModel u = getUngroupedModel();
		if (u.listSelectionListeners == null) {
			u.listSelectionListeners = new ArrayList();
		} else {
			u.listSelectionListeners.clear();
		}
	}

	public boolean columnsAreDynamic=true, showReadOnlyAsDisabled=false;
	boolean isShownAsDisabled(){
		return readOnly&&showReadOnlyAsDisabled;
	}
	
	boolean useCustomNewItemOnly=false;

	public void setUseCustomNewItemOnly(){
		useCustomNewItemOnly=true;
	}
	
	private boolean suppressRightClick=false;
	public void hideRightClickMenu(){
		suppressRightClick=true;
	}
	
	public void pickRoot() {
		if (isGrouped()) {
			if (groupedDataSource.tree != null && 
					Basics.isEmpty(groupedDataSource.tree.getSelectionPaths())) {
				groupedDataSource.pickRoot();				
			}
		}
	}
	
	private Icon getSortIcon(final int dataColumnIndex){
		final Boolean b = getSorted(dataColumnIndex);
		if (b == null) {
			return MmsIcons.getSortAscending16Icon();
		} else if (b) {
			return MmsIcons.getSortDescending16Icon();
		} 
		return MmsIcons.getTableDeleteIcon();
	}

	private String getSortText(final int dataColumnIndex){
		final Boolean b = getSorted(dataColumnIndex);
		if (b == null) {
			return SORT_CMD_ASCEND;
		} else if (b) {
			return SORT_CMD_DESCEND;
		} 
		return SORT_CMD_UN;
	}
	
	
	private static String 
		SORT_CMD_ASCEND="sort ascending", 
		SORT_CMD_DESCEND="sort descending", 
		SORT_CMD_UN="unsort";

	private void toggleSort(final int dataColumnIndex, final boolean showSortAfter){
		final Boolean b = getSorted(dataColumnIndex);
		if (b == null) {
			toggleSort(dataColumnIndex, SORT_CMD_ASCEND, showSortAfter);
		} else if (b) {
			toggleSort(dataColumnIndex, SORT_CMD_DESCEND, showSortAfter);
		} else {
			toggleSort(dataColumnIndex, SORT_CMD_UN, showSortAfter);
		}
	}
	
	private void toggleSort(final int dataColumnIndex, final String cmd, final boolean showSortAfter){
		final Collection<String>c=getIdentifiersForHiddenSortedColumn();
		if (c.size()>0) {
			PopupBasics.alert(getTearAwayComponent(),
					"<html>The sort appearance may seem confusing since hidden columns are<br>already sorted<br>"+Basics.toUlHtml(c)+"Use <b>Sort/arrange columns</b>(F10) to resolve.</html>", "Note...",false);
		}
		final Boolean b = getSorted(dataColumnIndex);
		if (cmd.equals(SORT_CMD_ASCEND)) {
			sort(dataColumnIndex, true);
		} else if (cmd.equals(SORT_CMD_DESCEND)) {
			sort(dataColumnIndex, false);
		} else {
			unsort(dataColumnIndex);
		}
		table.cellHighlighter.reset();
		if (columnsToSort.size() > 0
				&& dataSource.getMaximumCardinality() > 1 && showSortAfter) {
			showCurrentSort(dataColumnIndex);
		}
		notifyViewChanged();
		sortAndRepaint();
		
	}
	
	public void packColumns(final int margin){
		if (columnFreezer.rowHeaderTable != null){
			SwingBasics.packColumns(columnFreezer.rowHeaderTable, margin);
		}
		SwingBasics.packColumns(table, margin);
	}
	
	private class SearchPanel extends JPanel{
		private final JDialog dlg;
		private final JPanel searchPanel=new JPanel(), mainPanel, southWest, buttonPanel;
		private final JButton 
		less=SwingBasics.getButton("<html><small>L<u>e</u>ss</small></html>", MmsIcons.getLessIcon(), 'e',
				new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				buttonPanel.removeAll();
				buttonPanel.add(next);
				buttonPanel.add(prev);
				buttonPanel.add(up);
				buttonPanel.add(down);
				buttonPanel.add(query);
				mainPanel.remove(jspFind);
				mainPanel.add(tfFind, BorderLayout.WEST);
				southWest.remove(less);
				southWest.add(more);
				dlg.pack();
				tfFind.requestFocus();
			}
			
		}, null),
		more=SwingBasics.getButton("<html><small>Mor<u>e</u></small></html>", MmsIcons.getGreatIcon(), 'e', 
				new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				buttonPanel.removeAll();
				buttonPanel.add(seeOnly);
				buttonPanel.add(seeAll);
				buttonPanel.add(query);
				southWest.remove(more);
				southWest.add(less);
				taFind=new TextAreaWithHint(4, 30);
				if (seeOnlyTokens!=null){
					taFind.setText(seeOnlyTokens.searchEntry);
				}
				mainPanel.remove(tfFind);
				jspFind=initFindYellowSticky(taFind, null);
				mainPanel.add(jspFind, BorderLayout.WEST);
					
				taFind.setHint(textAreaSearchHint);
				taFind.setDocumentChangeListener(
						new TextAreaWithHint.DocumentChangeListener() {
							public void onChange(final TextAreaWithHint hta) {							
								enableSeeOnly(taFind.getText());
							}
					}
				);
				dlg.pack();
				if (Basics.isEmpty(taFind.getText())){
					ToolTipOnDemand.getSingleton().showLater(taFind, false, null);
					taFind.requestFocus();
				}
		}},null),
		next=SwingBasics.getButton(null, MmsIcons.getRightIcon(), 
				KeyEvent.VK_RIGHT, new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				table.autoCompleteSearchEntry=tfFind.getText();
				PersonalizableTable.tableWideSearch=true;
				table.searchStatusComponent=sp.tfFind;
				table.autoComplete(table.getAutoCompleteSelectedRow(), true);				
			}
		}, "Find next occurrence any where in the table."),
		prev=SwingBasics.getButton(null, MmsIcons.getLeftIcon(), KeyEvent.VK_LEFT, 
				new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				table.autoCompleteSearchEntry=tfFind.getText();
				table.searchStatusComponent=sp.tfFind;
				table.searchLeft.actionPerformed(ae);
			}
		}, "Find previous occurrence any where in the table."), 
		down=SwingBasics.getButton(null, MmsIcons.getDownIcon(), 
				KeyEvent.VK_DOWN, new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				table.autoCompleteSearchEntry=tfFind.getText();
				PersonalizableTable.tableWideSearch=false;
				table.searchStatusComponent=sp.tfFind;
				table.autoComplete(table.getAutoCompleteSelectedRow(), true);
			}
		}, "Find next occurrence in the same column as a starting value."),
		up =SwingBasics.getButton(null, MmsIcons.getUpIcon(), KeyEvent.VK_UP, 
				new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				table.autoCompleteSearchEntry=tfFind.getText();
				table.searchUp.actionPerformed(ae);
			}
		}, "Find previous occurrence in the same column as a starting value."), 
		seeAll=SwingBasics.getButton("See all",MmsIcons.getEyeIcon(), 'a',new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
								if (!Basics.isEmpty(taFind.getText())) {
									taFind.setText("");
									if (seeOnlyTokens != null) {
										seeOnlyTokens = null;
										refilterOrRemove(true);
										refreshShowingTable(true);
									}
									taFind.requestFocus();
								} else {
									SwingBasics.closeWindow(dlg);
								}
			}
		}, seeAllToolTip),
		seeOnly =SwingBasics.getButton("See only", MmsIcons.getMagnifyIcon(), 'l', new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				seeOnlyTokens=new SeeOnlyTokens(taFind.getText(), PersonalizableTableModel.this, false);
				refilterOrRemove(true);					
				refreshShowingTable(true);
			}
		}, seeOnlyToolTip),
		query =SwingBasics.getButton(queryText, null, 'q', new ActionListener(){
			public void actionPerformed(final ActionEvent ae){
				SwingBasics.closeWindow(dlg);
				query(false, QUERY_TYPE.ALL);
			}
		}, queryToolTip2);
	
		
		private final TextFieldWithHint tfFind=new TextFieldWithHint(25);
		private TextAreaWithHint taFind=null;
		private JScrollPane jspFind;
		private void enableArrows(final boolean b) {
			next.setEnabled(b);
			prev.setEnabled(b);
			up.setEnabled(b);
			down.setEnabled(b);

		}
		private void enableSeeOnly(final String text) {
			final boolean b=!Basics.isEmpty(text);
			PersonalizableTableModel.enableSeeOnly(b, seeOnly, seeAll);
		}

		private void enableArrows(final String text) {
			final boolean b=!Basics.isEmpty(text);
			enableArrows(b);
		}
		
		private SearchPanel(final JDialog dlg) {
			this.dlg=dlg;
			
				tfFind.setText(table.autoCompleteSearchEntry);
			tfFind.setHint(textFieldSearchHint);
			enableArrows(tfFind.getText());
			
			mainPanel=new JPanel(new BorderLayout(1,5));
			
			tfFind.setDocumentChangeListener(
					new TextFieldWithHint.DocumentChangeListener() {
						public void onChange(TextFieldWithHint hta) {							
							enableArrows(tfFind.getText());
						}
				}
			);
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					tfFind.requestFocus();
				}
			});
			
//			mainPanel.add(initFindYellowSticky(tfFind, null), BorderLayout.WEST);
			mainPanel.add(tfFind, BorderLayout.WEST);
			buttonPanel=new JPanel();
			SwingBasics.setToolBarStyle(next);
			buttonPanel.add(next);
			SwingBasics.setToolBarStyle(prev);
			buttonPanel.add(prev);
			SwingBasics.setToolBarStyle(down);
			buttonPanel.add(down);
			SwingBasics.setToolBarStyle(up);
			buttonPanel.add(up);
			buttonPanel.add(query);
			southWest=new JPanel();
			final JPanel south=new JPanel(new BorderLayout());
			southWest.add(more);
			south.add(southWest, BorderLayout.WEST);
			south.add(buttonPanel, BorderLayout.EAST);
			mainPanel.add(south,BorderLayout.SOUTH);
			add(mainPanel);
			if (seeOnlyTokens!=null){
				more.doClick();
			}
			SwingBasics.stylizeAsHyperLink(less);
			SwingBasics.stylizeAsHyperLink(more);
			
		}
	}
	
	static JScrollPane initFindYellowSticky(final JTextComponent tfFind, final String additional) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><ol><li>Enter 1 or more search values...<ul><li>words: e.g. \"science\" finds \"Life science\"<li>word fragments:  e.g. \"sc*\" find \"scar tissue\"</ul> <li>Type \",\" or &lt;Enter&gt; for \"or logic\" between searches.<li>Type \"+\" for \"and logic\" between searches.<li>Click <img src='");
		sb.append(MmsIcons.getURL("magnify16.gif"));
		sb.append("'> or <b>alt-L</b> to <b>look</b> <u>only</u> at items that match the search.<li>Click <img src='");
		sb.append(MmsIcons.getURL("eye.gif"));
		sb.append("'> or <b>alt-A</b> to see all items.");
		if (additional != null) {
			sb.append("<li>");
			sb.append(additional);
		}
		sb.append("</ol></html>");
		tfFind.setToolTipText(sb.toString());
		//tfFind.setBorder(PersonalizableTable.BORDER_EMPTY);
		tfFind.setBackground(PersonalizableTable.YELLOW_STICKY_COLOR);
		final JScrollPane jspFind=new JScrollPane(tfFind);
		
		jspFind.setBorder(null);
		jspFind.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jspFind.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		return jspFind;
	}
	
	private boolean hereAlready=false;
	private SearchPanel sp;
	public void find(){
		find(false);
	}
	public void find(final boolean isModal){
		closeFindWindow();
		findWindow=SwingBasics.getDialog(table);
		findWindow.getRootPane().registerKeyboardAction(
	              SwingBasics.getCloseAction(findWindow),
	              KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	              JComponent.WHEN_IN_FOCUSED_WINDOW);
		sp=new SearchPanel(findWindow);
		sp.tfFind.addFocusListener(new FocusAdapter(){
			public void focusGained(final FocusEvent fe){
				Window w = SwingUtilities.getWindowAncestor(table);
				if (!hereAlready && w != null) {
					w.toFront();
					hereAlready = true;
					sp.tfFind.requestFocus();
				} else {
					hereAlready = false;
				}
			}
		});
		
		findWindow.setTitle("Find");
		findWindow.getRootPane().setDefaultButton(sp.next);
		findWindow.getContentPane().add(sp);
		findWindow.setAlwaysOnTop(true);
		findWindow.pack();
		if (table.scrollPane.isShowing()) {
			SwingBasics.locateNextTo(table.scrollPane, findWindow);			
		}
		final JComponent prev=table.searchStatusComponent;
		table.searchStatusComponent=sp.tfFind;
		table.hasTipShown=false;
		table.autoCompletingFromTextField=true;
		findWindow.setModal(isModal);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								sp.tfFind.requestFocus();
							}
						});
					}
				});}
		});
		findWindow.addWindowListener(new WindowAdapter(){

			public void windowClosing(final WindowEvent e) {
				table.autoCompletingFromTextField=false;
				table.searchStatusComponent=prev;

			}

		});
		findWindow.setVisible(true);

	}
	
	public void setColumnFocus(final int dc, final String helpText){
		setColumnFocus(getModelShowing().getVisualRowIndex(0), dc, helpText);
	}
	
	public void setColumnFocus(final int row, final int dc, final String helpText){
	  SwingUtilities.invokeLater(new Runnable(){
		  public void run(){
			  SwingUtilities.invokeLater(new Runnable(){
				  public void run(){
					  SwingUtilities.invokeLater(new Runnable(){
						  public void run(){
											  final PersonalizableTableModel m2=getModelShowing();
			  final PersonalizableTable pt=m2.getTable();
			  pt.requestFocus();
			  if (dc>=0){
				  final PersonalizableDataSource dataSource=m2.getDataSource();
				  if (dataSource.getFilteredDataRows().size() > 0) {
						//int[] vc = m2.getVisualIndexes(new int[] { dc });
						final int col = m2.getVisualColumnIndexFromDataColumnIndex(dc);
						
						if (col  >= 0) {
							  
							if (helpText != null){
								final Rectangle r=pt.getCellRect(row, col, false);
	                    		final Integer wOffset=r.x, hOffset=r.y+r.height;
								showTextInPopupWindow(helpText, false, true, false, wOffset,
										hOffset, true);
							}
							pt.setRowSelectionInterval(row, row);
							pt.setColumnSelectionInterval(col, col);

							}
						}
				   }
			  }
		  
	  });}});}});
	}

	public enum NodeSelectStyle{
		ON_MOUSE_PRESS__THE_STANDARD,
		ON_DOUBLE_CLICK,
		ON_MOUSE_RELEASE,
		ONLY_BY_DRAGGING
	}
	public NodeSelectStyle nodeSelectStyle=NodeSelectStyle.ON_MOUSE_PRESS__THE_STANDARD;
	public  boolean isTreeDoubleClickForSelecting(){
		return nodeSelectStyle==NodeSelectStyle.ON_DOUBLE_CLICK;
	}
	
	public interface TreeDragListener {
		void stopDraggingPriorSelectedNode(GroupedDataSource.Node node, boolean success);
		GroupedDataSource.Node getDragStartingNode(GroupedDataSource.Node proposedStartingNode, boolean isPriorSelection);
		String getDragGestureAnomaly(GroupedDataSource.Node node, boolean isPriorSelection);
		void dragDropEnd();
		boolean canDropOnTree();
		void dropOnTree(final Transferable  t);
	}
	
	public TreeDragListener treeDragListener;
	public Node mostRecentlyClickedNode;
	private boolean userDefinedSorting = true;

	public boolean setUserDefinedSorting(final boolean ok) {
		final boolean was = userDefinedSorting;
		userDefinedSorting = ok;
		return was;
	}

	public boolean isWorkingWithMultipleRows() {
		return dataSource.getMaximumCardinality() > 1;
	}

	boolean canSort() {
		return isWorkingWithMultipleRows() && userDefinedSorting;
	}
	
    Map<Object, Integer>  allowedValueMap;
    public void putAllowedValueMap(final Object value, final int index){
    	if (allowedValueMap==null){
    		allowedValueMap=new HashMap<Object, Integer>();
    	}
    	allowedValueMap.put(value, index);
    }
    
    public Collection<TreePath> ignoreSelections;
    public String ignoreSelectionAnomaly="Can not deselect this item.";
	private static final boolean OPTIMIZE_SORTING=true;
	private static int OPTIMIZE_SORT_DATA_SIZE_THRESHOLD=50;
	
	Map<Integer, String> headerForeground;
	public void setHeaderForeGround(final int dataColumnIndex, final String color){
		if (this.headerForeground==null){
			this.headerForeground=new HashMap<Integer, String>();
			
		}
		this.headerForeground.put(dataColumnIndex, color);
	}
	
	static String queryText="<html><small><u>Q</u>uery</small></html>",
	queryToolTip2=Basics.toHtmlUncentered("Query", "Popup entry form for a structured query");
	public static String seeOnlyText="<html><font face='verdana'><small><u>S</u>ee only</small></font></html>",
	seeOnlyToolTip=Basics.startHtmlUncentered()+"When done, press <img src='"+MmsIcons.getURL("magnify16.gif")+"'> or <b>alt-L</b> to <b>look</b> <u>only</u> at items which <br>match your search.  "+Basics.NOTE_BOLDLY+"with multiple searches. <br>Type \",\" or &lt;Enter&gt; for \"or\" logic.  Type \"+\" for \"and logic\"."+Basics.endHtml();
	public static String seeAllText="<html><small>See <u>a</u>ll</small></html>",
	seeAllToolTip=Basics.startHtmlUncentered()+"Press <img src='"+MmsIcons.getURL("eye.gif")+"'> or <b>alt-A</b> to <i>see all</i> items <br>and clear any searches."+Basics.endHtml();

	boolean treeHasSeeOnlyAbility=true;
	public void setTreeSeeOnlyAbility(final boolean ok){
		treeHasSeeOnlyAbility=ok;
	}
	static void enableSeeOnly(final boolean haveSearch, final JButton seeOnly, final JButton seeAll){
		seeOnly.setEnabled(haveSearch);
		if (haveSearch) {
			seeAll.setIcon(MmsIcons.getEyeIcon());
		} else {
			seeAll.setIcon(MmsIcons.getCancelIcon());
		}
	}

	public String getColumnLabel(final int dataColumnIndex, final String prefix) {
		final String name = (String) metaRow
				.getDataColumnIdentifier(dataColumnIndex);
		final String name1 = name + "." + PROPERTY_HEADER_LABEL;
		String name2 = prefix+name1;
		final String label = properties.getProperty(name2, null);
		return label == null ? name : label;
	}

	public boolean enforceView(
			final List<Integer>requiredToShow, 
			final int []requiredToSort, 
			final List<Integer>requiredToHide) {
		boolean ok=false;		
		ok=Basics.equals(requiredToSort, getSort());
		if (!ok) {
			unsort();	
			for (final int i:requiredToSort) {
				sort(i, true);
			}
			sort(false);
		}
		final int []showing=getDataColumnIndexesThatAreVisibleInVisibleOrder();
		if (!Basics.isEmpty(requiredToHide)) {
			for (int i=requiredToHide.size()-1;i>=0;i--) {
				if (Basics.contains(showing, requiredToHide.get(i))){
					hideColumn( requiredToHide.get(i) );
				}
			}			
		}
		ok=Basics.equals(requiredToShow, showing);
		if (!ok) {
			for (int i=requiredToShow.size()-1;i>=0;i--) {
				hideColumn( requiredToShow.get(i) );
			}
			for (int i=requiredToShow.size()-1;i>=0;i--) {
				prependColumn(requiredToShow.get(i));
			}
		}
		return ok;
	}
	
	public ActionListener postCellEditAction=null;
	public boolean allowAutoResizeControl=true;
	public boolean resizePanelIfSingleColumnResized=true;
	public interface ColumnResizedListener{
		void actionPerformed(PersonalizableTable table);
	}
	public ColumnResizedListener columnResizedListener;
	public interface ColumnMovedListener{
		void actionPerformed(PersonalizableTable table, int distance, boolean released);
	}
	public ColumnMovedListener columnMovedListener;
	public boolean rememberUserPreferredColumnWidth=true;

	public void handleRowButtonClick(JButton button) {
		final Row row = getRowAtVisualIndex(focusVisualRowIndex);
		if (row != null) {
			if (!row.isActive()) {
				return;
			}			
			final TableCellContext context = new TableCellContext(this,
					clickedDataColumnIndex, row);
			dataSource.handleRowButtonClick(button, context);
			
		}
	}
	
	public void setColumnLabel(final int dataColumnIndex, final String label, final boolean remember) {
		if (label != null) {
			final HashMap<String, String> rlbn = (ungroupedModel == null) ? renamedLabelsByIdentifier
					: ungroupedModel.renamedLabelsByIdentifier;
			final String identifier = getDataColumnIdentifier(dataColumnIndex);
			rlbn.put(identifier, label);
			if (remember) {
				final PersonalizableTableModel utm = getUngroupedModel();
				final String ungroupedPrefix = utm.dataSource
						.getPropertyPrefix();
				final String _name = getPropertyName(ungroupedPrefix,
						identifier + "." + PROPERTY_HEADER_LABEL);
				utm.properties.setProperty(_name, label);
				utm.notifyViewChanged();
			}
			SwingBasics.repaintHeader(table);
		}
	}
	
	public PersonalizableTable.ColumnSelectionListener columnSelectionListener=null;
	
	JButton additionalTreeSearch;
	String additionalTreeSearchToolTip=null;
	public void setTree(final int []tree, final int []columns, final String seeOnlyText, final String textAreaSearchHint, final int expandToLevel) {
		final PersonalizableTableModel um = this.getUngroupedModel();
		this.applicationSpecificTreeSearch=columns;
		this.expandToLevel=expandToLevel;
		this.textAreaSearchHint=textAreaSearchHint;
		um.unsort();		
		for (final int i : tree) {
			um.sort(i, true);
		}
		um.sort();
		final boolean hasText=!Basics.isEmpty(seeOnlyText);
		if (!Basics.isEmpty(seeOnlyText)) {
			seeOnlyTokens = new SeeOnlyTokens(seeOnlyText, this, true);
			GroupedDataSource.remember = this.seeOnlyTokens;
			syncFilter(true);
		}
		um.setGroupOption(GROUP_BY_TREE);
		if (hasText) {
			final GroupedDataSource gds = um.groupedDataSource;
			if (!gds.isFindPanelShowing()) {
				gds.setFindPanel(false);
			}
			seeOnlyTokens = null;
		}
	}
	
	public void setFocusOnTreeYellowSticky() {
		final PersonalizableTableModel um = this.getUngroupedModel();
		if (um.groupedDataSource !=null) {
			um.groupedDataSource.focusOnYellowSticky();
		}
	}
	
	String treeSeeOnlyText=null;
	int []treeSeeOnlyTextSort=null;
	private static JDialog findWindow;
	private static void closeFindWindow(){
		if (findWindow!=null){
			SwingBasics.closeWindow(findWindow);
			findWindow=null;
		}
	}
	
	public int setExpandToLevel(final int expandToLevel){
		final int prior=expandToLevel;
		this.expandToLevel=expandToLevel;
		return prior;
	}
	
	final Collection<MouseListener> treeMouseListeners=new ArrayList<MouseListener>();
	public void addTreeMouseListener(final MouseListener treeMouseListener){
		this.treeMouseListeners.add(treeMouseListener);
		final JTree tree=getTree();
		if (tree != null){
			tree.addMouseListener(treeMouseListener);
		}
		
	}
	
	public boolean removeTreeMouseListener(final MouseListener treeMouseListener){
		return treeMouseListeners.remove(treeMouseListener);
	}
	

	public ArrayList<String[]>saveTreeView(){
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return SwingBasics.saveView(groupedDataSource.tree);
		}
		return null;
	}
	public void restoreTreeView(ArrayList<String[]> saved){
		if (!Basics.isEmpty(saved) && groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			SwingBasics.restoreView(groupedDataSource.tree, saved);
		}
	}

	
	public SeeOnlyTokens getTreeSeeOnlyTokens(){
			if (groupedDataSource != null && groupedDataSource.tree != null
					&& getGroupOption() == GROUP_BY_TREE) {
				return groupedDataSource.seeOnlyTokens;
			}
			return null;
		}
	public void setTreeSeeOnlyTokens(final SeeOnlyTokens seeOnlyTokens){
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			groupedDataSource.seeOnlyTokens=seeOnlyTokens;
		}
	}

	private Collection<ActionListener>treeSearchListeners;
	public void addTreeSearchListeners(final ActionListener al){
		if (treeSearchListeners==null){
			treeSearchListeners=new ArrayList<ActionListener>();
		}
		treeSearchListeners.add(al);
	}
	
	void fireTreeSearchListeners() {
		if (treeSearchListeners != null) {
			final ActionEvent event = new ActionEvent(this, 0, "change");
			for (final ActionListener al : treeSearchListeners) {
				al.actionPerformed(event);
			}
		}
	}
	
	public void hideTreeYellowSticky(){
		final GroupedDataSource gds = getUngroupedModel().groupedDataSource;
		if (gds != null && gds.isFindPanelShowing()) {
			gds.treePanel.remove(gds.findPanel);
		}
	}
	
	public boolean removeTreeSelectionsSilently(final TreePath tp) {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			groupedDataSource.removeSelectionSilently(tp);
		}
		return false;

	}
	
	public void resetLabels(){
		final PersonalizableTableModel ptm = getUngroupedModel();
		ptm.renamedLabelsByIdentifier.clear();
		String ungroupedPrefix = ptm.dataSource.getPropertyPrefix(), defaultPropertyPrefix = ptm.defaultPropertyPrefix;
		final boolean isUngroupedPrefix = Basics.equals(ungroupedPrefix,
				defaultPropertyPrefix);
		for (int i = 0; i < metaRow.size(); i++) {
			final String name = metaRow.getDataColumnIdentifier(i);
			final String name1 = name + "." + PROPERTY_HEADER_LABEL;
			String name2 = getPropertyName(ungroupedPrefix, name1);
			String label = properties.getProperty(name2, null);
			if (label != null) {
				ptm.renamedLabelsByIdentifier.put(name, label);
			} else if (!isUngroupedPrefix) {
				name2 = getPropertyName(defaultPropertyPrefix, name + "."
						+ PROPERTY_HEADER_LABEL);
				label = properties.getProperty(name2, null);
				if (label != null) {
					ptm.renamedLabelsByIdentifier.put(name, label);
				}
			}
		}
		
	}
	
	public static boolean ignoreNextTreeTimerToolTip=false;
	ColumnRenderer columnRenderer;
	public void setColumnRenderer(final ColumnRenderer columnRenderer){
		this.columnRenderer=columnRenderer;
	}
	
	static String selectRowsAnomaly(){
		return selectRowsAnomaly(null);
	}
	
	static String selectRowsAnomaly(final String type){
		if (type==null)
			return "This operation is ONLY enabled if you first select item(s)";;
		return Basics.concat("This operation is ONLY enabled if you first select ", type,"(s)");
	}
	
	public ActionListener moveDownAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			moveDown();
			getTable().requestFocus();
			fireListSelection();
		}
	};

	public ActionListener moveBottomAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			moveBottom();
			getTable().requestFocus();
			fireListSelection();
		}
	};

	public ActionListener moveUpAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			moveUp();
			getTable().requestFocus();
			fireListSelection();
		}
	};

	public ActionListener moveTopAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			moveTop();
			getTable().requestFocus();
			fireListSelection();
		}
	};


	public ActionListener qualifyMoveAction = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			qualifyMove();
		}
	};

	private String getChangeColumnCount(final int dc) {
		final int[] selected = getSelectedDataRowIndexesInAscendingOrder();
		int cnt=0;
		if (selected.length>0){
			for (int i=0;i<selected.length;i++){
				final Row row=getDataRowAtVisualIndex(selected[i]);
				if (row != null && row.isEditable(dc)){
					cnt++;
				}
			}
		} 
		if (cnt==0){
			for (final Row row:getDataSource().getFilteredDataRows()){
				if (row != null && row.isEditable(dc)){
					cnt++;
				}
			}
		}
		final String all;
		if (cnt>1){
			all= Integer.toString(cnt);
		}else if (cnt==1){
			all="all";
		} else{
			all=null;
		}
		return all;
	}
	
	Operation createDeleteOperation(final JComponent component, final boolean addToList) {
		final JMenuItem menuItem = new JMenuItem();
		menuItem.setMnemonic('d');
		menuItem.setIcon(MmsIcons.getDeleteIcon());

		final Operation operation = 
			new Operation(new DisabledExplainer(menuItem), addToList) {

			public void prepareStartedOperation(final boolean isKeyboard,
					final int[] selected,
					final int rowsWithData, final int selectedDataRowCount) {
				final int deletableCount = rowsWithData == 0 ? 0
						: getDeletableSelectionCount(selected);
				if (isKeyboard){
					final Object value=getDeletableValue(deletableCount);
					if (value != null){
						da.setEnabled(true, "Delete", null);
						return;
					}
				}
				boolean canDelete = !forbidDeletions && deletableCount > 0;
				String dt = null;
				if (isMultiColumnEnabled) {
					if (multiColumnSelectionCount > 0) {
						canDelete = true;
					}
				} else {
					if (!canDelete) {
						if (forbidDeletions) {
							dt = "Deletions forbidden/unallowed for <br>the entire list/table.";
						} else if (rowsWithData == 0) {
							dt = MSG_NOTHING_IS_SELECTED;
						} else {
							dt = getDeletableAnomalies(selected);
						}
					}
				}
				if (dt == null) {
					menuItem.setToolTipText(null);
				}
				da.setEnabled(canDelete, "Delete", dt);
				if (deletableCount > 0 || isMultiColumnEnabled) {
					final boolean worksWithMultipleRows = getUngroupedModel().dataSource
							.getMaximumCardinality() > 1;
					final String singularKey = getUngroupedModel().singularKey, nonSingularKey = (worksWithMultipleRows ? key
 							: singularKey);
					if (isMultiColumnEnabled) {
						menuItem.setText(
								Basics.concatObjects(VERB_DELETE, " ", multiColumnSelectionCount, " \"", nonSingularKey, "\""));
					} else {
						menuItem.setText(
								Basics.concatObjects(VERB_DELETE, " ", deletableCount, " \"",nonSingularKey,  "\""));
					}
				} else {
					menuItem.setText(VERB_DELETE);
				}

			}

			String computeNewAnomaly(final int[] selected,
					final int rowsWithData, final int selectedDataRowCount) {
				if (!getUngroupedModel().dataSource.isDeletable()){
					return "All of this particular table does not support deletion of rows"; 
				}
				if (readOnly) {
					return entireTableIsReadOnly;
				}
				return selectedDataRowCount == 0 ? MSG_NOTHING_IS_SELECTED
						: getDeletableAnomalies(selected);
			}

		};

		final ActionListener keyboardAction = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final int count=getDeletableSelectionCount();
				final Object value=getDeletableValue(count);
				if (value != null){
					clearValue(focusVisualRowIndex, focusModelColumnIndex);
					deleteFocusChanges=focusChanges;
					return;
				}
				if (isMultiColumnEnabled) {
					dataSource.triggerDeletion();
				} else {
					deleteSelected(count, true);
				}
			}
		};
		final ActionListener menuAction = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (isMultiColumnEnabled) {
					dataSource.triggerDeletion();
				} else {
					deleteSelected(getDeletableSelectionCount(), true);
				}
			}
		};
		new MenuItemsHandler(component, keyboardAction, menuAction, SwingBasics.getKeyStrokeWithMetaIfMac(
				KeyEvent.VK_DELETE, 0, KeyEvent.VK_R, InputEvent.SHIFT_MASK), 'd',
				operation.da,
				"You need to select a deletable item", operation,
				"Delete");

		return operation;
	}
	
	Operation createRemoveOperation(final JComponent component, final boolean addToList){
		final JMenuItem item=new JMenuItem();
		item.setIcon(MmsIcons.getRemoveIcon());
		
		final Operation operation = new Operation(new DisabledExplainer(item), addToList) {	        
			public void prepareStartedOperation(final boolean isKeyboard,
					final int[] selected,
					final int rowsWithData, final int selectedDataRowCount) {
				final boolean worksWithMultipleRows = getUngroupedModel().dataSource.getMaximumCardinality() > 1;

				final String singularKey = getUngroupedModel().singularKey, nonSingularKey = (worksWithMultipleRows ? key
						: singularKey);
				item.setText(Basics.concat(VERB_REMOVE, " selected \"",nonSingularKey, "\""));
				
			}
			
				
			String computeNewAnomaly(final int[] selected, final int rowsWithData,
					final int selectedDataRowCount) {
				if (!getUngroupedModel().dataSource.isRemovable()){
					return "All of this particular table does not support removal of rows"; 
				}
				if (readOnly){
					return entireTableIsReadOnly;
				}
				return selectedDataRowCount==0?MSG_NOTHING_IS_SELECTED:getRemovableAnomaly(selected, selectedDataRowCount);
			}

	    };

		ActionListener action = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				removeSelected(getRemovableSelectionCount(), true);
			}
		};

		new MenuItemsHandler(component,action,
				SwingBasics.addCtrlOrMetaIfMac(KeyEvent.VK_R),'r',
				operation.da,
				"You need to select a removable item",
				operation,"Remove");

		return operation;
	}
	
	public String getShortenedKey(){
		final String s=" item(s)";
		if (key.endsWith(s)){
			return Basics.concat(key.substring(0, key.length()-s.length()), "s").toLowerCase();
		}
		return key;
	}
	
	private Collection<Color> allowedBackgroundColorsIfImg;
	boolean isBackgroundOkWithImg(final Color bg){
		if (globalAlternatingRowColor.equals(bg)){
			return true;
		}
		if (allowedBackgroundColorsIfImg != null && allowedBackgroundColorsIfImg.contains(bg)){
			return true;
		}
		return false;
	}
	
	public void allowBackgroundColorIfImg(final Color color){
		if (allowedBackgroundColorsIfImg==null){
			allowedBackgroundColorsIfImg=new ArrayList<Color>();
		}
		if (!allowedBackgroundColorsIfImg.contains(color)){
			allowedBackgroundColorsIfImg.add(color);
		}
	}
	final static String PROPERTY_REFRESH_VIEW="refreshView";
	
	public DoNotAskAgainIdiom viewRefresh=new DoNotAskAgainIdiom(
			"Changes affect view...", "Refresh view to reflect new changes?", PROPERTY_REFRESH_VIEW, true);

	private JPopupMenu findFilterPopupMenu;
	private void qualifyQuerying(final String sortableColumnDescription){
		querySelectedColumnsItem.setText(QUERY_HIGHLIGHTED_COLUMNS);
		querySelectedColumnsItem.setToolTipText(Basics.concatHtmlUncentered(
				QUERY_HIGHLIGHTED_COLUMNS,
				":  <i>",
				Basics.trimIfTooBig(sortableColumnDescription),
				"</i>"));
		queryFavoriteColumnsItem.setText(QUERY_FAVORITE_COLUMNS);
		queryFavoriteColumnsItem.setToolTipText(getQueryFavoriteText());
	}
	
	public JPopupMenu getFindFilterPopupMenu(){
		if (findFilterPopupMenu==null){
			findFilterPopupMenu=new JPopupMenu("Find options..");
			if (findFilterMenu==null){
				qualifyActions();
			}
			final ArrayList<JMenuItem>c=new ArrayList<JMenuItem>();
			final int n=findFilterMenu.getMenuComponentCount();
			for (int i=0;i<n;i++){
				c.add((JMenuItem)findFilterMenu.getMenuComponent(i));
			}
			for (final JMenuItem mi:c){
				findFilterPopupMenu.add(mi);
			}
		}
		qualifyQuerying(qualifySorting());
		return findFilterPopupMenu;
	}
	
	private JMenu establishFindFilterMenu(){
		if (findFilterPopupMenu!=null){
			final ArrayList<JMenuItem>c=new ArrayList<JMenuItem>();
			final int n=findFilterPopupMenu.getComponentCount();
			for (int i=0;i<n;i++){
				c.add((JMenuItem)findFilterPopupMenu.getComponent(i));
			}
			for (final JMenuItem mi:c){
				findFilterMenu.add(mi);
			}
			findFilterPopupMenu=null;
		}
		return findFilterMenu;
	}
	
    public interface ColumnRenderer{
    	boolean useDefaultShowHideMenuItem(PersonalizableTableModel tableModel, int dataColumnIndex);    	
    	GroupCheckBoxItem getShowHideMenuItem(PersonalizableTableModel tableModel, int dataColumnIndex);
    	GroupMenuItem getAddFromListMenuItemForGroup(PersonalizableTableModel tableModel,int dataColumnIndex);
    	GroupMenuItem getCreateMenuItem(PersonalizableTableModel tableModel, int dataColumnIndex);
    	Collection<GroupMenuItem>getAddableColumns(PersonalizableTableModel tableModel, int dataColumnIndex);
    	void showAddableColumns(PersonalizableTableModel tableModel, int dataColumnIndex);
   	 String getGroupName(int dataColumnIndex);
   	 Color getForeground(int dataColumnIndex);
   	 Color getBackground(int dataColumnIndex);
   	 Icon getIcon(PersonalizableTableModel tableModel, int dataColumnIndex, boolean forMenuItem);
   	 Font getFont(int dataColumnIndex);
   	 Component get(PersonalizableTableModel tableModel, JTableHeader header, int dataColumnIndex, int visualColumnIndex, DefaultTableCellRenderer dtcr);
   	 boolean handleMouseEvent(PersonalizableTableModel model, JTableHeader header, int dataColumnIndex, int visualColumnIndex, MouseEvent e, boolean released);
   	 boolean isPreferredShown(int dataColumnIndex);
   	 boolean isGroupShowingOn(int dataColumnIndex);
   	 void notifyShowingChange(int dataColumnIndex, boolean isShowing);
   	 boolean isShowable(final int dataColumnIndex);
   	boolean isNothidableColumn(PersonalizableTableModel tableModel, int dataColumnIndex);
   	 boolean alwaysShowSameColumnOrder(final int dataColumnIndex);
   	Collection<Integer> sortColumns(final PersonalizableTableModel model, final Collection<Integer>dataColumnIndexes);
   	String getColumnLabel(PersonalizableTableModel tableModel, int dataColumnIndex);
   	Collection getAllowedValues(PersonalizableTableModel tableModel, int dataColumnIndex);
    }

	private class RowFreezer {

		private final PersonalizableTableModel inner = PersonalizableTableModel.this;
		JTable columnHeaderTable;
		private AbstractTableModel outer;

		private void freeze(final List<Row> rows) {
			JViewport jv = null;
			if (!Basics.isEmpty(rows)) {
				outer = new AbstractTableModel() {
					public String getColumnName(final int visualColumnIndex) {
						return inner.getColumnName(visualColumnIndex);
					}

					public int getColumnCount() {
						final int n = inner.getColumnCount();
						return n;
					}

					public int getRowCount() {
						final int n = rows.size();
						return n;
					}

					public Object getValueAt(int visualRowIndex,
							int visualColumnIndex) {
						final int dataColumnIndex = inner
								.getDataColumnIndexFromVisualIndex(visualColumnIndex);
						final Row row = rows.get(visualRowIndex);
						final Object o = row.get(dataColumnIndex);
						return o;
					}
				};
				final TableColumnModel columnModel = new DefaultTableColumnModel() {
					int col = 0;

					public void addColumn(final TableColumn tc) {
						final int dataColumnIndex = inner
								.getDataColumnIndexFromVisualIndex(col++);
						int width = inner.getWidth(dataColumnIndex);
						if (width < 1) {
							width = inner.getColumnWidth(dataColumnIndex);
						}

						tc.setMaxWidth(width);
						super.addColumn(tc);

					}
				};
				columnHeaderTable = new JTable(outer, columnModel) {
					public int getRowHeight() {
						return inner.table.getRowHeight();
					}

					public int getRowMargin() {
						return inner.table.getRowMargin();
					}
				};
				columnHeaderTable.setColumnSelectionAllowed(false);
				columnHeaderTable.setCellSelectionEnabled(false);

				columnHeaderTable.createDefaultColumnsFromModel();
				class _Renderer extends JTextPane implements TableCellRenderer {

					_Renderer() {
						setContentType("text/html");
					}

					public Component getTableCellRendererComponent(
							final JTable table, final Object value,
							final boolean isSelected, final boolean hasFocus,
							final int rowIdx, final int colIdx) {
						if (value != null) {
							final Class cl = value.getClass();
							final StringConverter sc = (StringConverter) DefaultStringConverters
									.get(cl);
							final boolean alignRight = cl == Long.class
									|| cl == Integer.class
									|| cl == Double.class || cl == Float.class
									|| cl == Date.class;
							final String htmlStart, htmlEnd;
							if (alignRight) {
								htmlStart = "<html><div align='right'>";
								htmlEnd = "</div></html>";
							} else {
								htmlStart = "<html><center>";
								htmlEnd = "</center></html>";
							}

							setText((value == null) ? "" : htmlStart
									+ (sc == null ? value.toString() : sc
											.toString(value)) + htmlEnd);

						} else {
							setText(null);
						}
						PersonalizableTableModel.this.table.setStandardColors(
								this, rowIdx, isSelected);
						return this;
					}
				}
				columnHeaderTable.setDefaultRenderer(Object.class,
						new _Renderer());
				jv = new JViewport();
				jv.setView(columnHeaderTable);
				jv.setPreferredSize(columnHeaderTable.getPreferredSize());
				columnHeaderTable.addMouseListener(table);
			} else {
			}
			if (table != null && table.scrollPane != null) {
				table.scrollPane.setColumnHeader(jv);
			}
		}
	}
	
	public void redisplayColumns(){
		initModelColumns(getModelColumnIdentifiers());
	}
	
	public void debug(){
		String name="tableColumnModel";
		String value=properties.getProperty(name);
		if (value != null && value.contains("spectra+demerits&antibody+demerits")){
			System.out.println(" ===========>>> herp ");
		}
		name="s[Rank].tableColumnModel";
		value=properties.getProperty(name);
		if (value != null && value.contains("spectra+demerits&antibody+demerits")){
			System.out.println(" ===========>>>  AND derp ");
		}
		
		
	}
	
	private List<ColumnShower>showHide;
	public interface GroupMenu{
		Color getGroupBackground();
		Color getGroupForeground();
	}
	
	
	/* IF we wish to toggle column group menu item appearance between JCheckBoxMenuItem
	 * and JCheckBox then we change the super class extends below (duh).
	*/
	public static class GroupMenuItem extends JCheckBox implements GroupMenu{
		public GroupMenuItem(){
		}
		private Color bg,fg;
		public Color getGroupBackground() {
			return SwingBasics.isQuaQuaLookAndFeel() ? null:bg;
		}		
		public void setGroupBackground(final Color bg){
			this.bg=bg;
			setOpaque(true);
			setBackground(bg);
		}
		
		public Color getGroupForeground() {
			return SwingBasics.isQuaQuaLookAndFeel() ? fg : null;
		}		
		public void setGroupForeground(final Color fg){
			this.fg=fg;
			setOpaque(true);
			setBackground(bg);
		}
		public void set(final Color bg, final Color fg){
			if (bg != null) {
				setGroupBackground(bg);
			}
			if (fg != null){
				setGroupForeground(fg);
			}
		}
	}
	

	public static class GroupCheckBoxItem extends GroupMenuItem{
	}

	public class ColumnShower extends JCheckBoxMenuItem implements ActionListener, GroupMenu{
		private boolean mustEnsureVisible=false;
		private int[]columns;
		public void setSelected(final boolean ok){
			super.setSelected(ok);
		}
		private final String groupName;
		private ColumnShower(final boolean showOrHide, final String name, final int[]columns, final Icon icon){
			super(name);
			if (!SwingBasics.isPlasticLookAndFeel()){
				setIcon(icon);
			}
			this.groupName=name;
			this.columns=columns;
			final Color b=getGroupBackground();
			if (b!=null){
				setBackground(b);
			}
			setOpaque(true);
			if (showOrHide && columnRenderer.isGroupShowingOn(columns[0])){
				if (columnRenderer.isPreferredShown(columns[0])){
					setSelected(true);
					showColumns();
				} else{
					setSelected(false);
					hideColumns();
				}
			}
			addActionListener(this);
		}
		
		public Color getGroupBackground(){
			return SwingBasics.isQuaQuaLookAndFeel()?null:columnRenderer.getBackground(columns[0]);
		}

		public Color getGroupForeground(){
			return !SwingBasics.isQuaQuaLookAndFeel()?null:columnRenderer.getBackground(columns[0]);
		}

		
		private void reshow(final int []prior) {
			int lastVisualColumnIndex = -1;
			final PersonalizableTableModel ms=getModelShowing();			
			for (final int dataColumnIndex : prior) {
				final int vc = ms.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
				if (vc > lastVisualColumnIndex) {
					lastVisualColumnIndex = vc;
				}
			}
			setSelected(true);
			if (lastVisualColumnIndex < 0) {
				showColumns();
			} else {
				for (final int dataColumnIndex : columns) {
					if (columnRenderer.isShowable(dataColumnIndex)) {						
						if (!Basics.contains(prior, dataColumnIndex)) {
							ms.hideColumn(dataColumnIndex);
							ms.showColumn(lastVisualColumnIndex+1,
									getDataColumnIdentifier(dataColumnIndex));
							
						}
					}
				}
			}
			notifyShowingChange();
			ensureVisible();
		}
		
		private void ensureVisible(){
			if (!Basics.isEmpty(columns)){
				final PersonalizableTableModel ms=getModelShowing();
				final int dataColumnIndex=columns[columns.length-1];
				final int vc = ms.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
				SwingUtilities.invokeLater(new Runnable() {					
					public void run() {
				SwingUtilities.invokeLater(new Runnable() {					
					public void run() {
						ms.table.ensureColumnVisible(vc);
	
					}
				});
				}
			});
			}
		}
		
		private void showColumns(){
			int shown=0;
			final PersonalizableTableModel ms=getModelShowing();
			if (columnRenderer.alwaysShowSameColumnOrder(columns[0])){
				boolean ok=true;
				int prior=-1;
				for (final int dataColumnIndex:columns){
					int n=ms.getVisualColumnIndexFromDataColumnIndex(dataColumnIndex);
					if (prior>-1 && n != prior+1){
						ok=false;
						break;
					}
					prior=n;
				}
				if (!ok){
					for (final int dataColumnIndex:columns){
						ms.hideColumn(dataColumnIndex);
					}
				}
				
			}
			for (final int dataColumnIndex:columns){
				if (columnRenderer.isShowable(dataColumnIndex)){
					if (ms.showColumn(dataColumnIndex)){
						shown++;
					}
				}
			}
			if (shown>0 && mustEnsureVisible){
				ensureVisible();
			}
		}
		
		private void hideColumns(){
			final PersonalizableTableModel ms=getModelShowing();
			for (final int dataColumnIndex:columns){
				if (columnRenderer.isShowable(dataColumnIndex)){
					ms.hideColumn(dataColumnIndex);
				}
			}
		}

		public void actionPerformed(final ActionEvent e) {
			if (isSelected()){
				mustEnsureVisible=true;
				showColumns();
			} else {
				hideColumns();
			}
			notifyShowingChange();
		}

		public String toString() {
			return groupName+" "+Basics.toString(columns);
		}

		private void notifyShowingChange(){
			columnRenderer.notifyShowingChange(columns[0], isSelected());
		}

		public boolean hasColumns(){
			for (final int dataColumnIndex:columns){
				if (columnRenderer.isShowable(dataColumnIndex)){
					return true;
				}
			}
			return false;
		}
	}
	
	public void showColumnGroup(final String name){
		for (final ColumnShower mi:showHide){
			if (name.equalsIgnoreCase( mi.getText()) && !mi.isSelected()){
				mi.showColumns();
				mi.setSelected(true);
				break;
			}
		}
	}
	
	public int getLastVisualIndexInColumnGroup(final String groupName){
		return getUngroupedModel()._getLastVisualIndexInColumnGroup(groupName, true);
	}
	private int _getLastVisualIndexInColumnGroup(final String groupName, final boolean firstCall){
		refreshColumnGroup(groupName);
		final PersonalizableTableModel ms=getModelShowing();
		int n=ms.table.getColumnModel().getColumnCount();
		if (showHide!=null){
			for (final ColumnShower mi:showHide){
				if (groupName.equalsIgnoreCase( mi.getText())){
					for (int vi=n-1;vi>-1;vi--){
						final int dataColumnIndex=ms.getDataColumnIndexFromVisualIndex(vi);
						final int idx=Basics.indexOf(mi.columns, dataColumnIndex);
						if (idx>=0){
							return vi;
						}
					}
					break;
				}
			}
			if (firstCall){
				return _getLastVisualIndexInColumnGroup(groupName, false);
			}
		}
		return n-1;
	}
	
	public void hideColumnGroup(final String groupName){
		for (final ColumnShower mi:showHide){
			if (groupName.equalsIgnoreCase( mi.getText())){
				mi.hideColumns();
				mi.setSelected(false);
				mi.notifyShowingChange();
				break;
			}
		}
		
	}
	
	public Collection getShowHideColumnGroupMenuList(){
		return getShowHideColumnGroupMenuList(true);
	}
	
	public Collection getShowHideColumnGroupMenuList(final boolean showOrHide){
		if (columnRenderer==null){
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}
		showHide=new ArrayList<ColumnShower>();
		final MapOfMany<String, Integer> mom=new MapOfMany<String, Integer>(Basics.caseAndNullInsensitive, false);
		for (int dataColumnIndex=0;dataColumnIndex<getDataColumnCount();dataColumnIndex++){
			final String name=columnRenderer.getGroupName(dataColumnIndex);
			if (name != null){
				mom.put(name, dataColumnIndex);
			}
		}
		for (final String name:mom.keySet()){
			final Collection<Integer>c=columnRenderer.sortColumns(this, mom.getCollection(name));
			final Icon icon=columnRenderer.getIcon(getUngroupedModel(), Basics.getFirst(c), true);
			final ColumnShower mi=new ColumnShower(showOrHide, name, Basics.toIntArray(c), icon);
			showHide.add(mi);
			if (!mi.hasColumns()){
				new DisabledExplainer(mi).setEnabled(false, mi.getText(), "There are no columns yet");
				if (mi.isSelected()){
					mi.setSelected(false);
					//mi.notifyShowingChange();
				}
			}
		}
		return showHide;
	}

	public Collection<AbstractButton> getShowHideMenuList(final String groupName, final int max){
		final Collection<AbstractButton>menuItems=new ArrayList<AbstractButton>();
		final PersonalizableTableModel um=getUngroupedModel();
		final ColumnRenderer cr=um.columnRenderer;
		final MapOfMany<String, Integer> mom=new MapOfMany<String, Integer>(Basics.caseAndNullInsensitive, false);
		int showing=0, hidden=0, cnt=0;
		Collection<GroupMenuItem>addable=null;
		GroupMenuItem addFromList=null,createNew=null;
		int ai=-1;
		Color bg=null,fg=null;
		final Map<String,GroupMenuItem>map=new TreeMap<String, GroupMenuItem>(Basics.caseAndNullInsensitive);
		for (int dataColumnIndex=0;dataColumnIndex<getDataColumnCount();dataColumnIndex++){
			final String name=cr.getGroupName(dataColumnIndex);
			if (map.size()==max){
				break;
			}
			if (cr.isShowable(dataColumnIndex) && groupName.equals(name)){
				cnt++;
				bg=cr.getBackground(dataColumnIndex);
				fg=cr.getForeground(dataColumnIndex);
				if (addFromList==null){
					addFromList=cr.getAddFromListMenuItemForGroup(this, dataColumnIndex);
				}
				if (createNew==null){
					createNew=cr.getCreateMenuItem(this, dataColumnIndex);
				}
				if (addable==null){
					ai=dataColumnIndex;
					addable=cr.getAddableColumns(this, dataColumnIndex);
				}
				final GroupCheckBoxItem mi;
				final String label=Basics.stripSimpleHtml(getColumnLabel(dataColumnIndex));
				
				final String identifier=metaRow.getDataColumnIdentifier(dataColumnIndex);
				final int dt=dataColumnIndex;
				if (cr.useDefaultShowHideMenuItem(this, dataColumnIndex)){
					mi=new GroupCheckBoxItem();
					mi.setText(label);
					mi.setMnemonic(label.charAt(0));
					if (PersonalizableTableModel.isGroupMenuAMenuItem()){
						mi.setIcon(MmsIcons.getBlank13x13Icon());
					}
					mi.addActionListener(new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							if (!mi.isSelected() && !cr.isNothidableColumn(getModelShowing(), dt)){
								getModelShowing().hideColumn(identifier);
							} else{
								final int idx=getLastVisualIndexInColumnGroup(groupName);
								getModelShowing().showColumn(idx+1, identifier);
								getModelShowing().table.scrollToVisible(focusVisualRowIndex, idx+1);
							}
						}
					});
				}else{
					mi=cr.getShowHideMenuItem(this, dataColumnIndex);
				}
				if (mi != null){
					mi.set(bg, fg);
					if (!isHidden(identifier)){
						showing++;
						mi.setSelected(true);
						mi.setToolTipText(Basics.toHtmlUncentered("Click to hide this column"));
					}else{
						hidden++;
						mi.setSelected(false);
						mi.setToolTipText(Basics.toHtmlUncentered("Click to show this column"));
					}
					map.put(label, mi);
				}
			}
		}
		if (addable!=null){
			for (final GroupMenuItem mi:addable){
				if (!map.containsKey(mi.getText())){
					if (map.size()==max){
						break;
					}
					mi.set(bg, fg);
					map.put(mi.getText(),mi);
				}
			}
		}
		for (final String label:map.keySet()){
			menuItems.add(map.get(label));
		}
		JMenuItem mi=new JMenuItem();
		mi.setText("Show all");
		mi.setMnemonic('s');
		mi.setBackground(bg);
		mi.setForeground(fg);
		final int addableDataColumnIndex=ai;
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					um.refreshColumnGroup(groupName); 	
					um.reshowColumnGroup(groupName);
					cr.showAddableColumns(
						PersonalizableTableModel.this, 
						addableDataColumnIndex);
			}
		});
		
		menuItems.add(mi);
		
		mi=new JMenuItem();
		mi.setText("Hide all");
		mi.setMnemonic('h');
		mi.setBackground(bg);
		mi.setForeground(fg);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				um.refreshColumnGroup(groupName); 	
				um.hideColumnGroup(groupName);
			}
		});
		
		menuItems.add(mi);
		if (createNew!=null){
			createNew.set(bg, fg);
			createNew.setIcon(MmsIcons.getNewIcon());
			menuItems.add(createNew);
		}		
		if (addFromList!=null){
			addFromList.set(bg, fg);
			addFromList.setIcon(MmsIcons.getAddIcon());
			menuItems.add(addFromList);
		}
		return menuItems;
	}
	
	public void reshowColumnGroup(final String groupName) {
		for (final ColumnShower cs : showHide) {
			if (groupName.equals(cs.groupName)) {
				final Collection<Integer>c=new ArrayList<Integer>();
				for (int dataColumnIndex = 0; dataColumnIndex < getDataColumnCount(); dataColumnIndex++) {
					final String name = columnRenderer
							.getGroupName(dataColumnIndex);
					if (cs.groupName.equals(name)) {
						c.add(dataColumnIndex);
					}
				}
				final int []a=Basics.toIntArray(c);
				if (!Basics.equals(a, cs.columns)){
					final int []prior=cs.columns;
					cs.columns=a;
					cs.reshow(prior);
				} else {
					cs.mustEnsureVisible=true;
					cs.showColumns();
					cs.setSelected(true);
					cs.notifyShowingChange();
				}

				return;
			}
		}
	}

	private void refreshColumnGroup(final String groupName) {
		if(showHide==null){
			return;
		}
		for (final ColumnShower cs : showHide) {
			if (groupName.equals(cs.groupName)) {
				final Collection<Integer>c=new ArrayList<Integer>();
				for (int dataColumnIndex = 0; dataColumnIndex < getDataColumnCount(); dataColumnIndex++) {
					final String name = columnRenderer
							.getGroupName(dataColumnIndex);
					if (cs.groupName.equals(name)) {
						c.add(dataColumnIndex);
					}
				}
				final int []a=Basics.toIntArray(c);
				if (!Basics.equals(a, cs.columns)){
					final int []prior=cs.columns;
					cs.columns=a;
					
				} 
				return;
			}
		}
	}
	
	public interface DeleteListener{
		public boolean checkIsDeletable(Row row);
	}
	
	public static void setDeleteListenerToIgnoreKBHierarchyBehavior(final DeleteListener deleteListener) {
		PersonalizableTableModel.deleteListenerToIgnoreKBHierarchyBehavior = deleteListener;

	}
	
	public static boolean isGroupMenuAMenuItem(){
		return JMenuItem.class.isAssignableFrom(PersonalizableTableModel.GroupMenuItem.class);
	}
	
	private Icon []getSortIcons(final int dataColumnIndex){
		final Boolean b = getSorted(dataColumnIndex);
		if (b == null) {
			return new Icon[]{MmsIcons.getSortAscending16Icon(), MmsIcons.getSortDescending16Icon()};
		} else if (b) {
			return new Icon[]{MmsIcons.getSortDescending16Icon(), MmsIcons.getTableDeleteIcon()};
		} 
		return new Icon[]{MmsIcons.getSortAscending16Icon(),MmsIcons.getTableDeleteIcon()};
	}

	private String[] getSortTexts(final int dataColumnIndex){
		final Boolean b = getSorted(dataColumnIndex);
		if (b == null) {
			return new String[]{SORT_CMD_ASCEND, SORT_CMD_DESCEND};
		} else if (b) {
			return new String[]{SORT_CMD_DESCEND, SORT_CMD_UN};
		} 
		return new String[]{SORT_CMD_ASCEND, SORT_CMD_UN};
	}

	private Collection<JButton> getSortButtons(final int dataColumnIndex){
		if (!canSort()){
			return Basics.UNMODIFIABLE_EMPTY_LIST;
		}
		final boolean custom=hasNonAlphabeticSortSequence(dataColumnIndex);
		final Collection<JButton>b=new ArrayList<JButton>();
		final Icon []sis=getSortIcons(dataColumnIndex);
		final String []sts=getSortTexts(dataColumnIndex);
		for (int i=0;i<2;i++){
			final Icon si=sis[i];
			final String st=sts[i];
			b.add(SwingBasics.getButton(Basics.capitalize(st), si, '\0', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ToolTipOnDemand.getSingleton().hideTipWindow();
					toggleSort(dataColumnIndex, st, true);
				}
			}, ""));
		}
		if (columnsToSort.size()>0){
		b.add(SwingBasics.getButton(unsortAllItem.getText(), 'u', new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				qualifyActions();
				if (unsortAllItem.isEnabled()) {
					unsortAndRepaint();
				}
				
			}
		}, "This column has the option of a non alpha-numeric sort sequence!"));
		}
		if (custom){
			b.add(SwingBasics.getButton(MENU_TEXT_SEQUENCE, 'a', new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					sortSequenceItem.doClick();
				}
			}, "This column has the option of a non alpha-numeric sort sequence!"));
		}
		
		return b;
	}

	public JButton getChangeAllButton(final int dataColumnIndex){
		final String scope=getChangeColumnCount(dataColumnIndex);
		if (scope != null){
			final JButton b=SwingBasics.getButton(Basics.concat("Change ", scope," rows"), MmsIcons.getEditIcon(), '\0', new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editAllValues(dataColumnIndex, -1);
				}
			}, "");
			return b;
		}
		return null;
	}
	
	public void showColumnOptions(
			final String name,
			final int dataColumnIndex,
			final boolean changeAll,
			final JButton ... buttons){
		_showColumnOptions(name, dataColumnIndex, changeAll, buttons);
	}
	public void showColumnOptions(
			final String name,
			final int dataColumnIndex,
			final boolean changeAll,
			final JComponent ... components){
		_showColumnOptions(name, dataColumnIndex, changeAll, components);
	}
	
	HashMap<Integer, ArrayList<JButton>> columnOptions = new HashMap<Integer, ArrayList<JButton>>();
	public void addColumnOption(int dataColumnIndex, JButton button) {
		ArrayList<JButton> options = columnOptions.get(dataColumnIndex);
		if (options == null) {
			options = new ArrayList<JButton>();
		}
		options.add(button);
		columnOptions.put(dataColumnIndex, options);
	}
	
	private void _showColumnOptions(
			final String name,
			final int dataColumnIndex,
			final boolean changeAll,
			final JButton []buttons){
		final Collection<JButton>bSort=getSortButtons(dataColumnIndex);
		final Collection<JButton>jp=new ArrayList<JButton>();
		final Collection<JButton>jpTemp=new ArrayList<JButton>();
		
		int cnt=0;
		JButton setColorButton=null;
		for (final JButton button:buttons){
			if (button != null){
				cnt++;
				final String cmd=button.getText();
				if (PersonalizableTable.SET_COLOR.equals(cmd)||PersonalizableTable.SET_COLUMN_COLOR.equals(cmd)){
					setColorButton=button;
				} else {
					jpTemp.add(button);
				}
			}
		}
		
		for (final JButton b:bSort){			
			jpTemp.add(b);
			cnt++;
		}
		if (setColorButton==null){
			final ActionListener al=ColorPreferences.lastOneInstantiated.getColorChooser(getTearAwayComponent(), "TableHeader", "background");
			setColorButton= ColorPreferences.getColorButton(al);			
			cnt++;
		}
		jp.add(setColorButton);
		jp.addAll(jpTemp);
		if(changeAll && canEditAll(dataColumnIndex) && getUngroupedModel().canEditCellForMultipleRows){
			final JButton bChange=getChangeAllButton(dataColumnIndex);
			if (bChange != null){
				jp.add(bChange);
				cnt++;
			}
		}
		if (columnOptions.get(dataColumnIndex) != null) {
			for (JButton button: columnOptions.get(dataColumnIndex)) {
				jp.add(button);
				cnt++;
			}
		}
		highlightColumn(dataColumnIndex);	
		showHeaderButtons(
				jp, 
				Basics.toHtmlUncentered(Basics.concat("<b>", name, "</b> column options:")));

	}
	
	private void _showColumnOptions(
			final String name,
			final int dataColumnIndex,
			final boolean changeAll,
			final JComponent []components){
		final Collection<JButton>bSort=getSortButtons(dataColumnIndex);
		final Collection<JComponent>jp=new ArrayList<JComponent>();
		final Collection<JComponent>jpTemp=new ArrayList<JComponent>();
		int cnt=0;
		JButton setColorButton=null;
		for (final JComponent component:components){
			if (component != null){
				cnt++;
				if(component instanceof JButton){
					final String cmd=((JButton)component).getText();
					if (PersonalizableTable.SET_COLOR.equals(cmd)||PersonalizableTable.SET_COLUMN_COLOR.equals(cmd)){
					setColorButton=(JButton)component;
				} else {
					jpTemp.add(component);
				}
			}
				else{
					jpTemp.add(component);
				}	
		}
		}
		for (final JButton b:bSort){			
			jpTemp.add(b);
			cnt++;
		}
		if (setColorButton==null){
			final ActionListener al=ColorPreferences.lastOneInstantiated.getColorChooser(getTearAwayComponent(), "TableHeader", "background");
			setColorButton= ColorPreferences.getColorButton(al);			
			cnt++;
		}
		jp.add(setColorButton);
		jp.addAll(jpTemp);
		if(changeAll && canEditAll(dataColumnIndex) && getUngroupedModel().canEditCellForMultipleRows){
			final JButton bChange=getChangeAllButton(dataColumnIndex);
			if (bChange != null){
				jp.add(bChange);
				cnt++;
			}
		}
		if (highlightColumnWhenOptionsShown){
			highlightColumn(dataColumnIndex);	
		}
		if (columnOptions.get(dataColumnIndex) != null) {
			for (JButton button: columnOptions.get(dataColumnIndex)) {
				jp.add(button);
				cnt++;
			}
		}
		showHeaderComponents(
				jp, 
				Basics.toHtmlUncentered(Basics.concat("<b>", name, "</b> column options:")));

	}
	public static boolean highlightColumnWhenOptionsShown=true;
	public void showHeaderButtons(final Collection<JButton>buttons, final String txt){
		
		
		final JPanel jp=new JPanel();
		class Silence implements ActionListener {
			Font f=null;	
			public void actionPerformed(ActionEvent e) {
				ToolTipOnDemand.getSingleton().hideTipWindow();				
			}
			void add(JButton b){
				if (f==null){
					f=b.getFont();
					f=new Font(f.getName(), f.getStyle(), PersonalizableTable.FONT_BASE.getSize()-3);
				}
				b.setFont(f);
				jp.add(b);
				b.addActionListener(this);
			}
		};
		Silence silence=new Silence();

		final int cnt=buttons.size();
		for (final JButton button:buttons){
			silence.add(button);
		}
		final int bCnt=Basics.count(txt, "<br>");
		int hoffset=-90;
		final int ROWS=4;
		if (cnt+bCnt>ROWS){
			final int rows=(cnt%ROWS)==0?cnt/ROWS:cnt/ROWS+1;
			jp.setLayout(new GridLayout(rows, ROWS, ROWS-1,ROWS-1));
			if (bCnt>0){
				hoffset-=(rows-1+(bCnt-2))*18;
			} else {
				hoffset-=(rows-1)*18;
			}
		}	
		showTextInPopupWindow(
				txt, false, false, true, 15, hoffset, true, jp);

	}
public void showHeaderComponents(final Collection<JComponent>components, final String txt){
		final JPanel jp=new JPanel();
		class Silence implements ActionListener {
			Font f=null;	
			public void actionPerformed(ActionEvent e) {
				ToolTipOnDemand.getSingleton().hideTipWindow();				
			}
			void add(JComponent b){
				if (f==null){
					f=b.getFont();
					f=new Font(f.getName(), f.getStyle(), PersonalizableTable.FONT_BASE.getSize()-3);
				}
				b.setFont(f);
				jp.add(b);
				if(b instanceof JButton)
				((JButton)b).addActionListener(this);
			}
		};
		Silence silence=new Silence();
		final int cnt=components.size();
		for (final JComponent component:components){
			silence.add(component);
		}
		final int bCnt=Basics.count(txt, "<br>");
		int hoffset=-90;
		final int ROWS=4;
		if (cnt+bCnt>ROWS){
			final int rows=(cnt%ROWS)==0?cnt/ROWS:cnt/ROWS+1;
			jp.setLayout(new GridLayout(rows, ROWS, ROWS-1,ROWS-1));
			if (bCnt>0){
				hoffset-=(rows-1+(bCnt-2))*18;
			} else {
				hoffset-=(rows-1)*18;
			}
		}	
		showTextInPopupWindow(
				txt, false, false, true, 15, hoffset, true, jp);
	}
	
	public boolean isDraggingFromTree() {
		if (groupedDataSource != null && groupedDataSource.tree != null
				&& getGroupOption() == GROUP_BY_TREE) {
			return groupedDataSource.isDragging();
		}
		return false;

	}

	private int deleteFocusChanges = -1;
	public boolean needImportExportItem=false;

	private Object getDeletableValue(final int count) {
		Object value = null;
		if (deleteFocusChanges != focusChanges) {
			if (count == 1) {
				if (focusVisualRowIndex >= 0 && focusModelColumnIndex >= 0) {
					if (editInPlace) {
						if (isCellEditable(focusVisualRowIndex,
								focusModelColumnIndex)) {
							final int colIdx = table.getColumnModel()
									.getSelectionModel()
									.getLeadSelectionIndex();
							value = table.getValueAt(focusVisualRowIndex,
									colIdx);
						}
					}
				}
			}
		}
		return value;
	}
	
	private void clearValue(final int visualRowIndex, final int modelColumnIndex){
		Object value=null;
		final Class cl=metaRow.getClass(getDataColumnIndex(modelColumnIndex));
		if (ComparableBoolean.class.equals(cl)){
			value=ComparableBoolean.NO;
		} else if (Boolean.class.equals(cl)){
			value=Boolean.FALSE;
		}else if (Integer.class.equals(cl)){
			value=Integer.valueOf(0);
		}else if (Double.class.equals(cl)){
			value=Double.valueOf(0);
		}else if (Float.class.equals(cl)){
			value=Float.valueOf(0);
		}
		setValueAt(value, focusVisualRowIndex, focusModelColumnIndex);

	}
	
	public boolean isFocus(final int visualRowIndex, final int visualColumnIndex){
		if (visualRowIndex==focusVisualRowIndex){
			final int dc1=getDataColumnIndexFromVisualIndex(visualColumnIndex),
					dc2=getDataColumnIndex(focusModelColumnIndex);
			if (dc1==dc2){
				return true;
			}
		}
		return false;
	}
	
	public void clearColumnSelections(){
		getModelShowing().table.cellHighlighter.reset();
	}
	
	public static ArrayList<Class> getDataTypes(final Properties columnTypesByName, final List<String>columns){
		final ArrayList<Class> dataTypes=new ArrayList<Class>();
		final String dflt=columnTypesByName.getProperty("default");
		final int N=columns.size();
		for (int i=0;i<N;i++) {
			final String name=columns.get(i);
			String s=columnTypesByName.getProperty(name);
			if (s==null) {
				s=columnTypesByName.getProperty(name.toLowerCase());
				if (s==null) {
					s=columnTypesByName.getProperty(name.toUpperCase());
					if (s==null) {
						s=dflt;
					}
				}
			}
			final Class cl;
			if (s==null) 
				cl=String.class; 
			else if (s.equalsIgnoreCase("double") || s.equalsIgnoreCase("float")) 
				cl=Float.class;
			else if (s.equalsIgnoreCase("date"))
				cl=Date.class;
			else if (s.equalsIgnoreCase("int") || s.equalsIgnoreCase("integer") || s.equalsIgnoreCase("long"))
				cl=Integer.class;
			else 
				cl=String.class;
			dataTypes.add(cl);
		}
		return dataTypes;
	}
}