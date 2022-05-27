package com.MeehanMetaSpace;

import java.io.*;
import java.net.*;

public class InternetCache {
    final File rootFolder;

    public void clear(){
        IoBasics.rmdir(rootFolder);
    }

    public InternetCache(final File rootFolder) {
        this.rootFolder=rootFolder;
        this.rootFolder.mkdirs();
    }

    private final File composeFileName(final URL url){
        final StringBuilder sb=new StringBuilder();
        sb.append(url.getProtocol());
        sb.append(File.separator);
        sb.append(url.getHost());
        sb.append(File.separator);
        sb.append(url.getPort());
        sb.append(File.separator);
        sb.append(url.getPath());
        return new File(rootFolder, sb.toString());
    }

    public final File getCachedFile(final URL url){
        final File file=composeFileName(url);
        if (!file.exists()){
            file.getParentFile().mkdirs();
            try{
                if (IoBasics.copy(file.getAbsolutePath(), url.toString(), null)){
                    return file;
                } else {
                    return null;
                }
            } catch(final IOException e){
                System.err.println(e.getMessage());
            }
        }
        return file;
    }

    public final java.awt.Image get(final URL url){
        final File f=getCachedFile(url);
        if (f!=null){
            return java.awt.Toolkit.getDefaultToolkit().getImage(f.getAbsolutePath());
        }
        return null;
    }


}
