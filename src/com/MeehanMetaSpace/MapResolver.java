package com.MeehanMetaSpace;
import java.awt.*;
import java.util.Collection;
import java.util.Set;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public interface MapResolver {
  public interface Listener{
	void notifyKeySelected(MapResolver mr, Object key);
	void notifyActualized(MapResolver mr, Object key, Object value);
	void notifyDeactualized(MapResolver mr, Object key, Object value);
  }

  void addKeySelectionListener(Listener k);
  void purgeExplanations();
  void disable(Object item, String toolTip);
  void explain(Object item, boolean allowChange, String toolTip);
  void setKeys(Object []keys);
  /**
   *
   * @param possibleValues Object[][]
  * @throws IllegalArgumentException IF
  * possibleValues.length != keys.length
   */
  void setPossibleValues(Object [][]possibleValues);

  /**
   *
   * @param actualValues Object[][]
  * @throws IllegalArgumentException IF
  * possibleValues.length != keys.length
   */
  void setActualValues(Object [][]actualValues);
  void setTitle(String title);
  void setLabels(String keyLabel, String possibleValues, String actualValues);

  // Methods added to interface by NHZ
  abstract void valueSelected( Object key );
  void setPossibleValues( Object key, Object []possibleValues );
  void setActualValues( Object key, Object []actualValues );
  void refresh();

  /**
  *
  * @return MapResolverResults where each key indexes a collection of actual values
  * and possible values resolved by the implementation of this interface
  *
  * @throws IllegalStateException IF
  * no keys or no possible values
  *
  */
  Results resolve();

  public interface Results{
	Set      getKeys();
	Collection possibleValues( Object key );
	Collection actualValues( Object key );
  }
  
  void setKey(Object key);

}
