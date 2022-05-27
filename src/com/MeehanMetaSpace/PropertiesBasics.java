package com.MeehanMetaSpace;

import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;

public final class PropertiesBasics {

    public static int getProperty(
      final Properties properties,
      final String propertyName,
      final int defaultPropertyValue) {
        if (properties != null) {
            final String s = properties.getProperty(propertyName);
            if (s != null) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    Pel.note(nfe);
                }
            }
        }
        return defaultPropertyValue;
    }

    public static final String findPathRelativePropertyName(
      final String propertyNameWithoutPath,
      final String longestPossiblePath,
      final Properties properties) {
        String propertyNameWithPath = IoBasics.concat(
          longestPossiblePath,
          propertyNameWithoutPath);
        for (String pn = properties.getProperty(propertyNameWithPath);
                         pn == null;
                         pn = properties.getProperty(propertyNameWithPath)) {
            propertyNameWithPath = IoBasics.removeLastFolder(propertyNameWithPath);
            if (propertyNameWithPath.lastIndexOf('/') < 0) {
                break;
            }
        }
        return propertyNameWithPath;
    }

    public static boolean getProperty(
      final Properties properties,
      final String propertyName,
      final boolean defaultPropertyValue) {
        if (properties != null) {
            final String s = properties.getProperty(propertyName);
            if (s != null) {
                return s.toLowerCase().equals("true");
            }
        }
        return defaultPropertyValue;
    }

    public static String getProperty(
      final Properties properties,
      final String propertyName,
      final String defaultPropertyValue) {
        if (properties != null) {
            final String s = properties.getProperty(propertyName);
            if (!Basics.isEmpty(s)) {
                return s;
            }
        }
        return defaultPropertyValue;
    }

    public static Properties loadProperties(final String fileName) {
        return fileName == null ? new Properties() : loadProperties(new File(fileName));
    }

    public static Properties loadProperties(final File file) {
        return loadProperties(null, file);
    }

    public static Properties loadProperties(final Properties defaults, final File file) {
        final Properties temp;
        if (defaults == null) {
            temp = new Properties();
        } else {
            temp = new Properties(defaults);
        }
        if (file != null && file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                temp.load(fis);
                fis.close();
                fis = null;    
                	
            } catch (final FileNotFoundException fnfe) {
                Pel.note(fnfe);
            } catch (final IOException ioe) {
                Pel.note(ioe);
            } finally {
                IoBasics.closeWithoutThrowingUp(fis);
            }
        }
        return temp;
    }


    public static Properties loadProperties(final Collection<File> files) {
        Properties p = null;
        for (File f : files) {
            p = PropertiesBasics.loadProperties(p, f);
/*            System.out.printf("Property \"testing\" in %s equals \"%s\"\n", f==null?"null":f.getName(),
                              p.getProperty("testing"));*/
        }
        return p;
    }

    public static Properties loadPropertiesFromString(String
      propertiesInStringFormat) {
        Properties properties = new java.util.Properties();
        if (!Basics.isEmpty(propertiesInStringFormat)) {
            try {
                properties.load(new java.io.StringBufferInputStream(
                  propertiesInStringFormat));
            } catch (IOException ioe) {
                Pel.log.print(ioe);
            }
        }
        return properties;
    }

    public interface Savior {
        public void save(final Properties properties);
    }


    public static String saveProperties(final Properties props) {
        final ByteArrayOutputStream fos = new ByteArrayOutputStream();
        props.save(fos, "");
        final String retval = fos.toString();
        try {
            fos.close();
        } catch (IOException ioe) {
            Pel.log.print(ioe);
        }
        return retval;
    }

    public static void saveProperties(
      final Properties properties,
      final String fileName,
      final String fileHeaderDescription) {
        saveProperties(properties, new File(fileName), fileHeaderDescription);
    }

    public static void saveProperties(
      final Properties properties,
      final File wFile,
      final String fileHeaderDescription) {
        FileOutputStream fos = null;
        try {
            final File parentFile = wFile.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(wFile);
            properties.save(fos, fileHeaderDescription);
            fos.close();
            fos = null;
        } catch (final IOException ioe) {
            Pel.log.print(ioe);
        } finally {
            IoBasics.closeWithoutThrowingUp(fos);
        }
    }

    public static void saveIntArray(
      final Properties properties,
      final String key,
      final int[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(Basics.COMMA);
            }
            sb.append(a[i]);
        }
        properties.setProperty(key, sb.toString());
    }

    public static int[] loadIntArray(final Properties properties,
                                     final String key) {
        final String source = properties.getProperty(key);
        return Basics.splitInt(source, "" + Basics.COMMA);
    }

    public static void saveBooleanArray(
      final Properties properties,
      final String key,
      final boolean[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(Basics.COMMA);
            }
            sb.append(a[i]);
        }
        properties.setProperty(key, sb.toString());
    }

    public static boolean[] loadBooleanArray(
      final Properties properties,
      final String key) {
        final String source = properties.getProperty(key);
        return Basics.splitBoolean(source, "" + Basics.COMMA);
    }

    public static String toString(int[] d) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(d[i]);
        }
        return sb.toString();
    }


    public static void copy(final Properties to, final Properties from) {
        for (final Iterator it = from.keySet().iterator(); it.hasNext(); ) {
            final Object key = it.next(), value = from.get(key);
            to.put(key, value);
        }
    }
    
    public static int removeAllEndingWith(final Properties properties, final String ending){
    	int n=0;
    	final Enumeration e=properties.propertyNames();
    	while (e.hasMoreElements()){
    		final String p=(String)e.nextElement();
    		if (p.endsWith(ending)){
    			properties.remove(p);
    			n++;
    		}
    	}
    	return n;
    }
}
