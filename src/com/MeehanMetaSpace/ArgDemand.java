package com.MeehanMetaSpace;

import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class ArgDemand extends com.MeehanMetaSpace.Args.Expect{

  public static class Boolean extends ArgDemand{
    public Boolean(final String key, boolean defaultValue, final String help){
      super(key, defaultValue ? "true" : "false", new String[]{"true", "false"}
            , help);
    }
  }

  public boolean isNeeded(final Args args){
    return true;
  }

  public boolean isWorthCarryingOn(final Args args, final String key){
    return true;
  }

  private final String[] onlyAcceptableValues;
  protected String extension, regexMatch, regexAvoid;
  final String rootDirectory, subDirectory;
  public ArgDemand(
      final String key,
      final String defaultValue,
      final String help){
    super(key, defaultValue, help);
    this.onlyAcceptableValues=null;
    this.rootDirectory=null;
    this.extension=null;
    this.subDirectory=null;
  }

  public ArgDemand(
      final String key,
      final String help){
    this(key, null, help);
  }

  public ArgDemand(
      final String key,
      final String defaultValue,
      final String[] acceptableValues,
      final String help){
    super(key, defaultValue, help);
    this.onlyAcceptableValues=acceptableValues;
    this.rootDirectory=null;
    this.extension=null;
    this.subDirectory=null;
  }

  public ArgDemand(
      final String key,
      final String defaultValue,
      final String help,
      final String rootDirectory,
      final String extension,
      final String subDirectory){
    super(key, defaultValue, help);
    this.onlyAcceptableValues=null;
    this.rootDirectory=rootDirectory;
    this.extension=extension;
    this.subDirectory=subDirectory;
  }

  private final boolean getArgFromUserIfNeedBe(final Args args){

    if (Basics.isEmpty(onlyAcceptableValues) && Basics.isEmpty(rootDirectory)){
      return getArgFromUserIfNeedBe(args, help, key, defaultValue);
    } else if (Basics.isEmpty(rootDirectory)){
      return getArgFromUserIfNeedBe(args, key, help, onlyAcceptableValues,
                                    Basics.indexOf(onlyAcceptableValues,
          defaultValue));
    }
    return getDirFromUserIfNeedBe(
        args,
        key,
        help,
        subDirectory,
        regexMatch,
        regexAvoid,
        extension,
        rootDirectory,
        defaultValue);

  }

  public static boolean getArgFromUserIfNeedBe(
      final Args args,
      final String help,
      final String key,
      final String defaultValue){
    final String value=args.get(key);
    if (Basics.isEmpty(value)){
      args.remove(key);

      final Object o=Basics.gui.getStringFromUser(
          null,
          help,
          " \"" + key + "\" argument missing.",
          defaultValue);
      if (o == null){
        return false;
      }
      args.put(key, o.toString());
      hadToPause=true;
    }
    return true;
  }

  public static boolean getArgFromUserIfNeedBe(
      final Args args,
      final String key,
      final String help,
      final String[] onlyPossibleValues,
      int defaultIdx){
    final String[] values=args.getStrings(key);
    if (Basics.isEmpty(values) || (values.length==1 && Basics.isEmpty(values[0]))){
      final String userValue;
      final int idx=Basics.gui.choose(
          null,
          help,
          "\"" + key + "\" argument missing.  ",
          onlyPossibleValues,
          defaultIdx,
          true,
          onlyPossibleValues.length > 3);
      userValue=idx >= 0 ? onlyPossibleValues[idx] : null;
      if (userValue != null){
        args.remove(key);
        args.put(
            key,
            userValue
            );
        hadToPause=true;
        return true;
      }
    }

    for (int i=0; i < values.length; i++){
      if (!Basics.contains(onlyPossibleValues, values[i])){
        return false;
      }
    }
    return true;
  }

  public static boolean getDirFromUserIfNeedBe(
      final Args args,
      final String key,
      final String help,
      final String subDirectory,
      final String regExMatch,
      final String regExAvoid,
      final String extension,
      final String rootDirectory,
      final String lastChoice){
    final String dir;
    if (Basics.isEmpty(lastChoice)){
      dir=rootDirectory + "/" + subDirectory;
    } else{
      final String s=rootDirectory + "/" + lastChoice;
      dir=new File(s).getParent();
    }
    if (Basics.isEmpty(args.get(key))){
      final String s=Basics.gui.getFileName(
          regExMatch,
          regExAvoid,
          extension,
          help,
          "Select",
          "Select the file",
          true,
          dir,
          lastChoice,
          null);
      if (s == null){
        return false;
      }
      args.remove(key);
      final String p=IoBasics.subtractRootDirectory(
          IoBasics.standardizeFileName(s),
          rootDirectory);
      args.put(key, p);
      hadToPause=true;
    }
    return true;
  }

  public static boolean hadToPause=false;
  public static String checkUsage(
      final Args args,
      final ArgDemand[] demands,
      final java.io.File propertyFile){
    final Properties p=PropertiesBasics.loadProperties(propertyFile);
    for (int i=0; i < demands.length; i++){
      if (p.containsKey(demands[i].key)){
        demands[i].defaultValue=p.getProperty(demands[i].key);
      }
    }
    final String retVal=checkUsage(args, demands);
    for (int i=0; i < demands.length; i++){
      if (args.hasArg(demands[i].key)){
        final String v=args.get(demands[i].key);
        if (v == null){
          p.remove(demands[i].key);
        } else{
          p.setProperty(demands[i].key, v);
        }
      }
    }
    PropertiesBasics.saveProperties(p, propertyFile.getAbsolutePath(), null);
    return retVal;
  }

  public static String checkUsage(
      final Args args,
      final ArgDemand[] demands){
    hadToPause=false;
    for (int i=0; i < demands.length; i++){
      final boolean ok;
      if (demands[i].isNeeded(args)){
        ok=demands[i].getArgFromUserIfNeedBe(args);
      } else{
        ok=true;
      }
      if (!ok){
        final String suffix=Basics.isEmpty(demands[i].onlyAcceptableValues) ?
            "" :
            "<code>(Value must be:  " +
            Basics.toString(demands[i].onlyAcceptableValues) + ")</code>";
        Basics.gui.alert(
            Basics.toHtmlErrorUncentered(
            "Exiting because argument is missing",
            "The value supplied for argument <i>" +
            demands[i].key +
            "</i> was <b>" +
            args.get(demands[i].key) +
            "</b>!<br>" + suffix));
        System.exit(2);
      }
      if (!demands[i].isWorthCarryingOn(args, args.get(demands[i].key))){
        System.exit(1);
      }
    }
    return args.checkUsage(demands);
  }
}
