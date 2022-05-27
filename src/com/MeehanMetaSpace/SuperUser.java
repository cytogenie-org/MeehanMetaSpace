package com.MeehanMetaSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public final class SuperUser {
  public static boolean isLoggedIn() {
    return getEmailForCurrentLoginAndMachine() != null;
  }

  public static String getEmailForCurrentLoginAndMachine() {
    //DREPC
    final String retVal =
	SuperUser.getEmailForCurrentLoginAndMachine(
	MACAddress.getMACAddress(), System.getProperty("user.name"));
    return retVal;
  }

  public static String getEmailForCurrentLoginAndMachine(
      final String macAddress,
      final String loginName) {
    for (final Iterator it = SuperUser.all.iterator(); it.hasNext(); ) {
      final SuperUser superUser = (SuperUser) it.next();
      if ( (superUser.hasSameMacAddress(macAddress)) &&
	  superUser.hasSameLoginName(loginName)) {
	return superUser.getEmail();
      }
    }
    return null;
  }

  public static boolean isTheSuperUser(final String licensee) {
    final SuperUser su = new SuperUser(
	licensee,
	System.getProperty("user.name"),
	MACAddress.getMACAddress());
    boolean b=all.contains(su);
    if (!b){
    	for (final SuperUser su2:all){
    		if (Basics.equals(su2.loginName, su.loginName) && Basics.equals(su2.macAddress, su.macAddress)){
    			return true;
    		}
    	}
    }
    return b;
  }

  public static boolean hasEmail(final String email) {
    final String macAddress = null;
    final String loginName = null;
    for (final Iterator it = SuperUser.all.iterator(); it.hasNext(); ) {
      final SuperUser superUser = (SuperUser) it.next();
      if (superUser.email.equalsIgnoreCase(email)) {
	return true;
      }
    }
    return false;
  }

  public static ArrayList sites = new ArrayList();

  public static void addSourceSite(final String site) {
    sites.add(site);
  }

  public final static Collection<SuperUser> all = new ArrayList<SuperUser>();
  public static void addSuperUser(
      final String email,
      final String loginName,
      final String allowedMacAddress) {
    all.add(new SuperUser(email, loginName, allowedMacAddress));
  }

  private final String email, loginName, macAddress;

  public String getEmail() {
    return email;
  }

  public boolean hasSameLoginName(final String loginName) {
    return this.loginName == null || this.loginName.equals(loginName);
  }

  public boolean hasSameMacAddress(final String macAddress) {
    return this.macAddress == null || this.macAddress.equals(macAddress);
  }

  public SuperUser(
      final String email,
      final String loginName,
      final String macAddress) {
    this.email = email;
    this.loginName = loginName;
    this.macAddress = macAddress;
  }

  public int hashCode() {
    int result = 17;
    result += 37 * result + email.toLowerCase(). hashCode();
    result += 37 * result + (macAddress == null ? 0 : macAddress.hashCode());
    result += 37 * result + (loginName==null ? 0 : loginName.hashCode());
    return result;
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (! (o instanceof SuperUser)) {
      return false;
    }
    final SuperUser that = (SuperUser) o;
    if (!this.email.equalsIgnoreCase(that.email)) {
      return false;
    }
    if (!hasSameMacAddress(that.macAddress)) {
      return false;
    }
    if (!hasSameLoginName(that.loginName)) {
      return false;
    }

    return true;
  }
}
