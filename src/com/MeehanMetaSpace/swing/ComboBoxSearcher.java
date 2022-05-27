package com.MeehanMetaSpace.swing;

import java.util.Collection;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.DefaultStringConverters;
import com.MeehanMetaSpace.StringConverter;

public class ComboBoxSearcher {
	final JComboBox jc;
	final StringConverter sc;
	private boolean caseSensitive=true;

	private final boolean beepWhenNotFound;
	ComboBoxSearcher(final JComboBox jc, final StringConverter sc, final boolean beepWhenNotFound){
		this.jc=jc;
		assert sc != null;
		this.sc=sc;
		this.beepWhenNotFound=beepWhenNotFound;
	}
	
	
	ComboBoxSearcher(final JComboBox jc){
		this(jc,DefaultStringConverters.get(), true);
	}
	
	private interface Matcher{
		boolean match(String comboBoxItem, String searchArgument);
	}
	
	private Matcher startsWither=new Matcher(){
		public boolean match(final String comboBoxItem, final String searchArgInLowerCaseIfNecessary) {
			if (caseSensitive){
				return comboBoxItem.startsWith(searchArgInLowerCaseIfNecessary);
			}
			return comboBoxItem.toLowerCase().startsWith(searchArgInLowerCaseIfNecessary);
		}		
	};

	
	private Matcher equalizer=new Matcher(){

		public boolean match(final String comboBoxItem, final String searchArgInLowerCaseIfNecessary) {
			boolean ok=false;
				if (caseSensitive){
					ok=comboBoxItem.equals(searchArgInLowerCaseIfNecessary);
				} else {
					ok=comboBoxItem.equalsIgnoreCase(searchArgInLowerCaseIfNecessary);
				}
				if (!ok){
					if (comboBoxItem.contains(AutoComplete.IMG_CUE) && 
							!comboBoxItem.startsWith("&") && 
							!comboBoxItem.startsWith("<html>")){
						final String s=Basics.stripBodyHtml(comboBoxItem).trim();
						if (caseSensitive){
							ok=s.equals(searchArgInLowerCaseIfNecessary);
						} else {
							ok=s.equalsIgnoreCase(searchArgInLowerCaseIfNecessary);
						}
						if (ok){
							int debug=23;
						}
					}
				}
				return ok;
			}
		
	};

	private boolean equals(final String s1, final String s2){
		if (caseSensitive){
			return s1.equals(s2);
		}
		return s1.equalsIgnoreCase(s2);
	}

	
	private int index = -1;
    private Boolean stillOnStartingSelection=null;
	public Object find(final String _arg, final boolean startsWith, final boolean isCaseSensitive) {
		final Collection unselectable;
		if (jc instanceof AutoComplete){
			unselectable=(  (AutoComplete) jc).unselectableItems;
		} else {
			unselectable=Basics.UNMODIFIABLE_EMPTY_LIST;
		}

		String arg=isCaseSensitive?_arg:_arg.toLowerCase();
		if (arg.startsWith("<html>")){
			arg=Basics.stripSimpleHtml(arg).trim();
		}
		this.caseSensitive=isCaseSensitive;
    	final Matcher matcher=startsWith?startsWither:equalizer;
    	final ComboBoxModel model=jc.getModel();
    	
    	// Find index of selected item
		if (index == -1) {
			final Object sel = model.getSelectedItem();
			if (sel != null && !unselectable.contains(sel)) {
				for (int i = 0; i < model.getSize(); i++) {
					if (sel.equals(model.getElementAt(i))) {
						index = i;
						if (stillOnStartingSelection==null){
							stillOnStartingSelection=true;
						}
						break;
					}
				}
				if (index<0){
					index=0;
				}
			} else {
				index = 0;
			}
		}
		
		String s=null;
		Object o=null;
		
		final int startingIndex=index;
        // Search forward from current selection
		for (; index < model.getSize(); index++) {
			o = model.getElementAt(index);
			if (!unselectable.contains(o)) {
				s = sc.toString(o);
				if (s==null){
					sc.toString(o);
					continue;
				}
				if (s.startsWith("<html>")){
					s=Basics.stripSimpleHtml(s).trim();
				}
				if (matcher.match(s, arg)) {
					if (index != startingIndex) {
						stillOnStartingSelection = false;
					}
					return o;
				} else if (stillOnStartingSelection != null
						&& stillOnStartingSelection) {
					index = -1;// for loop will change to 0
				}
			}
			stillOnStartingSelection = false;
		}

        // Search from top to current selection
        for (index=0; index < startingIndex; index++) {
        	o=model.getElementAt(index);
            if (o != null && !unselectable.contains(o)) {
                s = sc.toString(o);
                if (s.startsWith("<html>")){
					s=Basics.stripSimpleHtml(s).trim();
				}				
                if (matcher.match(s,arg)) {
                    return o;
                }
            }
        }
        index=-1;
        if (beepWhenNotFound){
        	java.awt.Toolkit.getDefaultToolkit().beep();
        }
        return null;
    }
	
	public int getIndexOfLastSearch(){
		return index;
	}
	
	public void reset(){
		index=0;
	}
}
