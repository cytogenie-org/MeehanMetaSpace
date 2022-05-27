package com.MeehanMetaSpace;

/**
 *
 * <p>Title: MapResolverEnum</p>
 * <p>Description: A typesafe enumerated class representing the two
 *  types in the MapResolver class (Bloch, Effective Java Programming, 104) </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Herzenberg Lab</p>
 * @author Noah Zimmerman
 * @version 1.0
 */
public final class MapResolverEnum {

  private final String type;

  private MapResolverEnum( String type ) { this.type = type; }
  public String toString() { return type; }

  public static final MapResolverEnum ACTUAL = new MapResolverEnum( "Actual" );
  public static final MapResolverEnum POSSIBLE = new MapResolverEnum( "Possible" );

}
