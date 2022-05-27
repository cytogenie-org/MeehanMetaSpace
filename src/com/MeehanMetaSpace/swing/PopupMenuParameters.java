package com.MeehanMetaSpace.swing;
import javax.swing.*;
import com.MeehanMetaSpace.*;

public class PopupMenuParameters {
    public PersonalizableTableModel.PopupMenuListener listener;
    public final String text, toolTip;
    public final char mnemonic;
    public final KeyStroke keyStroke;
    public final Icon icon;

    public PopupMenuParameters(PopupMenuParameters other){
        this.listener=other.listener;
        this.text=other.text;
        this.toolTip=other.toolTip;
        this.mnemonic=other.mnemonic;
        this.keyStroke=other.keyStroke;
        this.icon=other.icon;
    }

    public PopupMenuParameters(
      final PersonalizableTableModel.PopupMenuListener listener,
      final String text,
      final String toolTip,
      final char mnemonic,
      final KeyStroke keyStroke,
      final Icon icon) {
        this.listener=listener;
        this.text=text;
        this.toolTip=Basics.toHtmlUncentered(text,toolTip);
        this.mnemonic=mnemonic;
        this.keyStroke=keyStroke;
        this.icon=icon;
    }
}
