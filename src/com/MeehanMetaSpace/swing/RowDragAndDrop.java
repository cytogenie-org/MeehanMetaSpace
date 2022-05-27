package com.MeehanMetaSpace.swing;
import com.MeehanMetaSpace.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class RowDragAndDrop {

    private final static String DATA_FLAVOR_TXT =
      "x-com.MeehanMetaSpace.swing.PersonalizableTableModel-";

    public static DataFlavor getDataFlavor(
      final Class rowClass,
      final PersonalizableTableModel model) {
        return new DataFlavor(rowClass, DATA_FLAVOR_TXT + model.key);
    }

    public static DataFlavor getDataFlavor(
      final PersonalizableTableModel model) {
        return new DataFlavor(Row.class, DATA_FLAVOR_TXT + model.key);
    }

    public static class DraggedData {
    	public final GroupedDataSource.Node node;

    	public final Collection<Row> allSelectedRows;
    	public final PersonalizableTableModel tableModel;
    	public final DataFlavor[] df;
        private DraggedData(
          final DataFlavor[] df,
          final GroupedDataSource.Node node,
          final PersonalizableTableModel tableModel,
          final Collection<Row> allSelectedRows) {
        	this.node=node;
            this.df = df;
            this.allSelectedRows = allSelectedRows;
            this.tableModel = tableModel;
        }
    }


    public static class Dragged implements Transferable {
        private final DraggedData d;
        Dragged(
        final GroupedDataSource.Node node,
          final DataFlavor[] df,
          final PersonalizableTableModel tableModel,
          final Collection<Row> rows) {
            d = new DraggedData(df, node, tableModel, rows);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return d.df;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Basics.contains(d.df, flavor);
        }

        public Object getTransferData(final DataFlavor flavor) throws
          UnsupportedFlavorException, IOException {
            if (Basics.contains(d.df, flavor)) {
                return d;
            }
            return null;
        }
    }


    private static class Proxy extends TransferHandler implements Draggable{
        private final Plugin plugin;
        final PersonalizableTableModel tableModel;
        Proxy(final PersonalizableTableModel tableModel, final Plugin dnd) {
            this.plugin = dnd;
            this.tableModel=tableModel;
        }

        Proxy(final String property, final Plugin dnd) {

            this.plugin = dnd;
            this.tableModel=null;
        }

        public Transferable getDraggedRows(final GroupedDataSource.Node node, final JComponent c){
            if (c instanceof PersonalizableTable &&
                plugin.canExportRows()) {
                return new Dragged(
                		node,
                  plugin.getExportDataFlavors(),
                  tableModel,
                  tableModel.getModelShowing().getSelectedRowsInDescendingOrder());
            }
            return null;
        }

        protected Transferable createTransferable(final JComponent c) {
            return getDraggedRows(null, c);
        }

        public int getSourceActions(JComponent c) {
            if (tableModel == null){
                int debug=2;
            }
            return COPY_OR_MOVE;
        }

        public boolean importData(final JComponent c, final Transferable t) {
            final DataFlavor[] df = t.getTransferDataFlavors();
            if (canImport(c, df)) {

                final Row row;

                if ( !(c instanceof PersonalizableTable)){

                    row = null;
                } else {
                    final PersonalizableTable target = (PersonalizableTable) c;
                    final int visualRowIndex = target.getSelectedRow();
                    row = tableModel.
                          getModelShowing().getRowAtVisualIndex(visualRowIndex);
                }
                    try {
                        DraggedData dragged = null;
                        for (int i = 0; i < df.length && dragged == null; i++) {
                            dragged = (DraggedData) t.getTransferData(
                              df[i]);
                        }
                        if (dragged != null) {
                            if (dragged.tableModel != tableModel) {
                                if (plugin.canImportRows(
                                  dragged.node, 
                                  dragged.
                                  tableModel,
                                  dragged.allSelectedRows)) {
                                    plugin.importRows(row, dragged.node, dragged.tableModel,
                                      dragged.allSelectedRows);
                                    tableModel.updateUILater();
                                }
                            } else {
                                // move within table

                            }

                            return true;
                        }
                    } catch (final IOException e) {
                        Pel.println(e);
                    } catch (final UnsupportedFlavorException ufe) {
                        Pel.println(ufe);
                    }

            }

            return false;
        }

        protected void exportDone(final JComponent c, final Transferable t,
                                  final int action) {

        }

        public boolean canImport(
          final JComponent c,
          final DataFlavor[] flavors) {
            if (tableModel == null){
                int debug=2;
            }
            if (tableModel != null && tableModel.isReadOnly() && !tableModel.getCanDragDrop()) {
                return false;
            }
            final DataFlavor[] df = plugin.getImportDataFlavors();
            for (final DataFlavor proposedFlavor : flavors) {
                for (final DataFlavor supportedFlavor : df) {
                    if (proposedFlavor.equals(supportedFlavor)) {
                        if (proposedFlavor.getHumanPresentableName().equals(
                          supportedFlavor.getHumanPresentableName())) {
                            return true;
                        }
                    }
                }

            }
            return false;
        }


    }

    public interface Draggable{
        Transferable getDraggedRows(final GroupedDataSource.Node node, final JComponent c);
    }


    public interface Plugin {
        DataFlavor[] getExportDataFlavors();
        DataFlavor[] getImportDataFlavors();
        boolean canExportRows();
        boolean canImportRows(GroupedDataSource.Node sourcePick, PersonalizableTableModel from,
                                   Collection<Row> rows);
        void importRows(Row to,
        		GroupedDataSource.Node sourcePick,
                             PersonalizableTableModel from,
                             Collection<Row> rows);
    }

    public static void setTransferHandler(final JButton jc, final Plugin plugin){
        jc.setTransferHandler(new Proxy("text", plugin));
    }

    public static void setTransferHandler(final PersonalizableTableModel tableModel, final Plugin plugin){
        final TransferHandler newHandler=new Proxy(tableModel, plugin);
        tableModel.table.setDragAndDrop(newHandler);        
    }





}
