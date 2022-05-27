package com.MeehanMetaSpace;
import java.io.*;
import java.util.Enumeration;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class FolderWithEmail {
  public FolderWithEmail(final String cueFolder, final String folder) {
    this.cueFolder=cueFolder;
    this.originalPath=folder;
    unixCueFolder=this.cueFolder+"/";
    windowsCueFolder=this.cueFolder+"\\";
    int startIdx=folder.indexOf(unixCueFolder);
    if (startIdx<0){
      startIdx=folder.indexOf(windowsCueFolder);
    }
    if (startIdx==0 || (startIdx >=0 && folder.charAt(startIdx-1)==File.separatorChar)){
      rootFolder=folder.substring(0, startIdx);
      subFolderWithEmail=folder.substring(startIdx + unixCueFolder.length());
      int idx=subFolderWithEmail.indexOf("/");
      if (idx < 0){
        idx=subFolderWithEmail.indexOf("\\");
      }
      email=idx >= 0 ? subFolderWithEmail.substring(0, idx) : subFolderWithEmail;
      subFolderWithoutEmail=subFolderWithEmail.substring(email.length());
      rootFolderWithCue=folder.substring(0, startIdx + unixCueFolder.length());
    } else {
      email=null;
      rootFolder=null;
      subFolderWithEmail=null;
      subFolderWithoutEmail=null;
      rootFolderWithCue=null;
    }
  }

  public boolean hasEmail(){
    return email!=null;
  }

  private final String originalPath,
      cueFolder,
      unixCueFolder,
      windowsCueFolder,
      rootFolder,
      rootFolderWithCue,
      subFolderWithEmail,
      subFolderWithoutEmail,
      email;
  public static File getFolder(final String rootFolder, final String cueFolder){
    return new File(rootFolder + File.separator + cueFolder);
  }



  public  String useFileSystemSpelling(
    final String fileSystemRoot // not likely the same as rootFolder if originalPath was relative path
    ){
    if (hasEmail()){
      final String finalEmailAddress=getFileSystemSpelling(fileSystemRoot, email, cueFolder);
      if (!email.equals(finalEmailAddress)){
        System.out.println("Converted " + email+ " to " + finalEmailAddress);
      }
      return rootFolderWithCue + finalEmailAddress + subFolderWithoutEmail;
    }
    return originalPath;
  }

  public static String getFileSystemSpelling(
    final String rootFolder,
    final String email,
    final String cueFolder){
    final File[] files=getFolder(rootFolder, cueFolder).listFiles();
    if (files != null){
      for (int i=0; i < files.length; ++i){
        final String subFolder=new String(files[i].getName());
        if ((files[i].isDirectory()) &&
            (subFolder.compareToIgnoreCase(email) == 0)){
          return subFolder;
        }
      }
    }
    return email;
  }

  public static void setFileSystemSpelling(
    final String rootFolder,
    final String email,
    final String cueFolder){
    final File[] files=getFolder(rootFolder, cueFolder).listFiles();
    if (files != null){
      for (int i=0; i < files.length; ++i){
        final String subFolder=new String(files[i].getName());
        if ((files[i].isDirectory()) &&
            (subFolder.compareToIgnoreCase(email) == 0)){
          //isUnmatchedCaseEmail=true;
          if (!subFolder.equals(email)){
            files[i].renameTo(new File(IoBasics.concat(files[i].getParent(),email)));
            Basics.gui.alert("<html><body><h3>Email spelling harmonization occurred!</h3>We altered the email address <i>" + subFolder+"</i> to <b>" + email + "</b> <br>in " +
                             IoBasics.dirHtml(files[i].getParentFile()) + "</body></html>");
          }
        }
      }
    }
  }

  public String getEmail(){
    return email;
  }


}
