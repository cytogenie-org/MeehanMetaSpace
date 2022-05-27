package com.MeehanMetaSpace.swing;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author stephen
 * @version beta 3
 */

public interface NonVisualUpdater extends ConditionReporter {
    void setFieldFocus(int dataColumnIndex);

    Object getColumnConvertedValue(int dataColumnIndex);
}
