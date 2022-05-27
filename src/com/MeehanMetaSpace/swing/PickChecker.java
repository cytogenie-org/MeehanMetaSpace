package com.MeehanMetaSpace.swing;
import javax.swing.*;
import java.util.Collection;
public interface PickChecker{
    public boolean checkPick (JComponent toolTipSource, Collection<Row> c, boolean enabled, boolean isTryingToPick);
    public boolean removeSelectionsForPicksThatDoNotCheck();
}
