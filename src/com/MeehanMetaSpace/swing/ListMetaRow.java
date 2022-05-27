package com.MeehanMetaSpace.swing;
import java.util.ArrayList;
import java.util.List;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class ListMetaRow extends AbstractMetaRow {
		com.MeehanMetaSpace.swing.Row row;
		public ListMetaRow(List names, com.MeehanMetaSpace.swing.Row row)
		{
			super(names);
			this.row=row;
		}
		public Class getClass(int index)
		{
			Object o=this.row.get(index);
			if (o==null)return String.class;
			return o.getClass();
		}
}