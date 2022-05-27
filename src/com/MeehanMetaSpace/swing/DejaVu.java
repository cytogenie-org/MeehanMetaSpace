package com.MeehanMetaSpace.swing;
import javax.swing.*;

import com.MeehanMetaSpace.Basics;

import java.util.*;

/**
 * <p>Title: Herzenberg Protocol Editor</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Herzenberg Lab, Stanford University</p>
 * @author Stephen Meehan
 * @version 1.0
 */

public abstract class DejaVu {
    protected DejaVu(){
        state=new State();
    }
    public static class State{
        private final ArrayList history = new ArrayList();
        private int undone = -1;
    }

    private State state;

    private final java.awt.event.ActionListener undoAction=
		 new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						undo();
					}
	};

	private final java.awt.event.ActionListener redoAction=
		 new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							redo();
						}
		};

    private ArrayList<AbstractButton> undoButtons=new ArrayList<AbstractButton>(), 
    	redoButtons=new ArrayList<AbstractButton>();


	private void registerUndo(final AbstractButton undo, final boolean addAction){
		if (undo != null) {
			undoButtons.add(undo);
			if (addAction){
				undo.addActionListener(undoAction);
			}
			setUndo(undo);
		}
	}

	private void registerRedo(final AbstractButton redo, final boolean addAction){
		if (redo != null){
			redoButtons.add(redo);
			if (addAction){
				redo.addActionListener(redoAction);
			}
			setRedo(redo);
		}
	}

	public void register(final AbstractButton undo, final AbstractButton redo){
		registerUndo(undo, true);
		registerRedo(redo, true);
	}

	/**
	 *
	 * @return true if a subsequent undo() will work
	 */
	 public void undo(){
		state.undone= (state.undone==-1)?state.history.size()-2:state.undone-1;
		replay();
		paintButtons();
	}
	
	 void removeLastStep(){
		 state.history.remove(state.history.size() - 1);
			paintButtons();
	 }
	public Object getPriorStatePeek(final int eonsInThePast) {
		final int h=state.history.size();
		final int undone, sundone=state.undone;
		
		if (sundone==-1) {
			undone=h-2;
		} else {
			undone=sundone-eonsInThePast;
		}
		if (undone >= 0 && undone < h) {
			final Object historicalState = state.history.get(undone);
			return historicalState;
		}
		return null;
	}


	/**
	 *
	 * @return if a subsequent redo will work
	 */
	private void redo(){
		if (state.undone>=0){
			state.undone++;
			replay();
			paintButtons();
		}

	}
	private String undoOp="Undo", redoOp="Redo";
	private boolean setText=true;
	
	protected void configure(final boolean setText, final String undoOp, final String redoOp){
		this.undoOp=undoOp;
		this.redoOp=redoOp;
		this.setText=setText;
	}
	protected String getNoUndoMsg(){
		return null;
	}
	
	private void setUndo(final AbstractButton undo){
      PersonalizableTableModel.setUndo(undo, state.undone, state.history.size(), setText, undoOp, getNoUndoMsg());
	}

	protected String getNoRedoMsg(){
		return null;
	}
	
	private void setRedo(final AbstractButton redo) {
      PersonalizableTableModel.setRedo(redo, state.undone, state.history.size(), setText, redoOp, getNoRedoMsg());
	}


    public void setEnabled(final boolean ok){
        for (final AbstractButton b:undoButtons){
            b.setEnabled(ok);
        }
        for (final AbstractButton b:redoButtons){
            b.setEnabled(ok);
        }


    }

	private void paintButtons(){
        for (final AbstractButton b:undoButtons){
			setUndo( b );
		}
        for (final AbstractButton b:redoButtons){
			setRedo(b);
		}
	}

	public abstract void replay(Object historicalState);

	private boolean replaying=false;

	private void replay(){
		if (state.undone >= 0 && state.undone < state.history.size()) {
			final Object historicalState = state.history.get(state.undone);
			if (historicalState != null) {
				replaying = true;
				replay(historicalState);
				replaying=false;
			}
		}
		//paintButtons();
	}

	protected abstract void handleRemoval(Object removed);

	public void record(final Object historicalState){
		record(historicalState,false);
	}
	public void record(final Object historicalState, final boolean checkForDuplicate){
		if (!replaying){
			boolean ok=true;
			if (checkForDuplicate){
				final int n=state.history.size();
				if (n>0){
					final Object o=state.history.get( n-1);
					if (Basics.equals(o, historicalState)){
						ok=false;
					}
				}
			}
			if (ok){
				if (state.undone < (state.history.size() - 1)) {
					int sz = state.undone + 1;
					while (state.history.size() > sz) {
						handleRemoval(state.history.get(state.history.size()-1));
						state.history.remove(state.history.size() - 1);
					}
				}
				state.history.add(historicalState);
				state.undone = state.history.size() - 1;
				paintButtons();
			}
		}
	}

    public void setState(final State state ){
        this.state=state;
        paintButtons();
    }

    protected ArrayList getHistory(){
        return state.history;
    }
    public State getState(){
        return state;
    }

    public boolean canUndo(){
        return state.undone>0;
    }
    
    public int getUndone(final int eonsInThePast){
    	final int h=state.history.size();
    	final int undone, sundone=state.undone;
    		
    		if (sundone==-1) {
    			undone=h-2;
    		} else {
    			undone=sundone-eonsInThePast;
    		}
    		if (undone >= 0 && undone < h) {
    			return undone;
    		}
    		return -1;
    }
    	
    public boolean hasHistory(){
    	return state.history.size()>0;
    }
}
