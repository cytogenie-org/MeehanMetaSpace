package com.MeehanMetaSpace.swing;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.TooManyListenersException;
import javax.swing.plaf.UIResource;
import javax.swing.event.*;
import javax.swing.*;

/**
 * This class is used to handle the transfer of a <code>Transferable</code>
 * to and from Swing components.  The <code>Transferable</code> is used to
 * represent data that is exchanged via a cut, copy, or paste
 * to/from a clipboard.  It is also used in drag-and-drop operations
 * to represent a drag from a component, and a drop to a component.
 * Swing provides functionality that automatically supports cut, copy,
 * and paste keyboard bindings that use the functionality provided by
 * an implementation of this class.  Swing also provides functionality
 * that automatically supports drag and drop that uses the functionality
 * provided by an implementation of this class.  The Swing developer can
 * concentrate on specifying the semantics of a transfer primarily by setting
 * the <code>transferHandler</code> property on a Swing component.
 * <p>
 * This class is implemented to provide a default behavior of transferring
 * a component property simply by specifying the name of the property in
 * the constructor.  For example, to transfer the foreground color from
 * one component to another either via the clipboard or a drag and drop operation
 * a <code>Dnd</code> can be constructed with the string "foreground".  The
 * built in support will use the color returned by <code>getForeground</code> as the source
 * of the transfer, and <code>setForeground</code> for the target of a transfer.
 *
 *
 * @author  Timothy Prinzing
 * @version 1.24 01/23/03
 */
public class DnD extends TransferHandler  {



	/**
	 * Constructs a transfer handler that can transfer a Java Bean property
	 * from one component to another via the clipboard or a drag and drop
	 * operation.
	 *
	 * @param property  the name of the property to transfer; this can
	 *  be <code>null</code> if there is no property associated with the transfer
	 *  handler (a subclass that performs some other kind of transfer, for example)
	 */
	public DnD(final String property) {
	super(property);
	}

	protected DnD() {
	}

	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
	  int srcActions=getSourceActions(comp);
	  int dragAction=srcActions & action;
	  if (!(e instanceof MouseEvent)){
		// only mouse events supported for drag operations
		dragAction=NONE;
	  }
	  if (dragAction != NONE && !GraphicsEnvironment.isHeadless()){
		if (recognizer == null){
		  recognizer=new SwingDragGestureRecognizer(new DragHandler());
		}
		recognizer.gestured(comp, (MouseEvent) e, srcActions, dragAction);
	  }
	  else{
		exportDone(comp, null, NONE);
	  }
	}


	protected Transferable createTransferable(JComponent c) {
	  return super.createTransferable(c);
	}

	protected void exportDone(JComponent source, Transferable data, int action) {
	  super.exportDone(source, data, action);
	}



	private static SwingDragGestureRecognizer recognizer = null;


	/**
	 * This is the default drag handler for drag and drop operations that
	 * use the <code>TransferHandler</code>.
	 */
	private static class DragHandler
		implements DragGestureListener, DragSourceListener{

	  private boolean scrolls;

	  // --- DragGestureListener methods -----------------------------------

	  /**
	   * a Drag gesture has been recognized
	   */
	  public void dragGestureRecognized(final DragGestureEvent dge){
		final JComponent c=(JComponent) dge.getComponent();
		final DnD th=(DnD) c.getTransferHandler();
		final Transferable t=th.createTransferable(c);
		if (t != null){
		  scrolls=c.getAutoscrolls();
		  c.setAutoscrolls(false);
		  try{
			Image img=null;
			Icon icn=th.getVisualRepresentation(t);
			System.out.println("Found icon!" + icn);
			if (icn != null){
			  if (icn instanceof ImageIcon){
				img=((ImageIcon) icn).getImage();
			  }
			  else{
				img=new BufferedImage(
					icn.getIconWidth(),
					icn.getIconWidth(),
					BufferedImage.TYPE_4BYTE_ABGR);
				Graphics g=img.getGraphics();
				icn.paintIcon(c, g, 0, 0);
			  }
			}
			if (img == null){
			  dge.startDrag(null, t, this);
			}
			else{
			  final Cursor cur=Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(5,5), "custom");
			  dge.startDrag(cur, img, new Point(5,5), t, this);
			}
			return;
		  }
		  catch (RuntimeException re){
			c.setAutoscrolls(scrolls);
		  }
		}

		th.exportDone(c, null, NONE);
	  }

	  // --- DragSourceListener methods -----------------------------------

	  /**
	   * as the hotspot enters a platform dependent drop site
	   */
	  public void dragEnter(DragSourceDragEvent dsde){
	  }

	  /**
	   * as the hotspot moves over a platform dependent drop site
	   */
	  public void dragOver(DragSourceDragEvent dsde){
	  }

	  /**
	   * as the hotspot exits a platform dependent drop site
	   */
	  public void dragExit(DragSourceEvent dsde){
	  }

	  /**
	   * as the operation completes
	   */
	  public void dragDropEnd(DragSourceDropEvent dsde){
		DragSourceContext dsc=dsde.getDragSourceContext();
		JComponent c=(JComponent) dsc.getComponent();
		if (dsde.getDropSuccess()){
		  ((DnD) c.getTransferHandler()).exportDone(c, dsc.getTransferable(),
													dsde.getDropAction());
		}
		else{
		  ((DnD) c.getTransferHandler()).exportDone(c, null, NONE);
		}
		c.setAutoscrolls(scrolls);
	  }

	  public void dropActionChanged(DragSourceDragEvent dsde){
	  }
	}

	private static class SwingDragGestureRecognizer extends DragGestureRecognizer {

	SwingDragGestureRecognizer(DragGestureListener dgl) {
		super(DragSource.getDefaultDragSource(), null, NONE, dgl);
	}

	void gestured(JComponent c, MouseEvent e, int srcActions, int action) {
		setComponent(c);
			setSourceActions(srcActions);
		appendEvent(e);
		fireDragGestureRecognized(action, e.getPoint());
	}

	/**
	 * register this DragGestureRecognizer's Listeners with the Component
	 */
		protected void registerListeners() {
	}

	/**
	 * unregister this DragGestureRecognizer's Listeners with the Component
	 *
	 * subclasses must override this method
	 */
		protected void unregisterListeners() {
	}

	}



}



