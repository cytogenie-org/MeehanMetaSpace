package com.MeehanMetaSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MACAddress {

	private static String macAddress = null;
	
	/**
     * The current clock and node value.
     */
    private static long clockSeqAndNode = 0x8000000000000000L;
	
	 public static String getMACAddress() {
	        if (macAddress == null) {

	            Process p = null;
	            BufferedReader in = null;

	            try {
	                String osname = System.getProperty("os.name");

	                if (osname.startsWith("Windows")) {
	                    p = Runtime.getRuntime().exec(
	                            new String[] { "ipconfig", "/all" }, null);
	                }
	                // Solaris code must appear before the generic code 
	                else if (osname.startsWith("Solaris") || osname.startsWith("SunOS")) {
	                    String hostName = UUIDGen.getFirstLineOfCommand(new String[] { "uname",
	                            "-n" });
	                    if (hostName != null) {
	                        p = Runtime.getRuntime().exec(
	                                new String[] { "/usr/sbin/arp", hostName }, null);
	                    }
	                }
	                else if (new File("/usr/sbin/lanscan").exists()) {
	                    p = Runtime.getRuntime().exec(
	                            new String[] { "/usr/sbin/lanscan" }, null);
	                }
	                else if (new File("/sbin/ifconfig").exists()) {
	                    p = Runtime.getRuntime().exec(
	                            new String[] { "/sbin/ifconfig", "-a" }, null);
	                }

	                if (p != null) {
	                    in = new BufferedReader(new InputStreamReader(
	                            p.getInputStream()), 128);
	                    String l = null;
	                    while ((l = in.readLine()) != null) {
	                        macAddress = MACAddressParser.parse(l);
	                        if (macAddress != null
	                                && Hex.parseShort(macAddress) != 0xff) break;
	                    }
	                }

	            }
	            catch (SecurityException ex) {}
	            catch (IOException ex) {}
	            finally {
	                if (p != null) {
	                    if (in != null) {
	                        try {
	                            in.close();
	                        }
	                        catch (IOException ex) {}
	                    }
	                    try {
	                        p.getErrorStream().close();
	                    }
	                    catch (IOException ex) {}
	                    try {
	                        p.getOutputStream().close();
	                    }
	                    catch (IOException ex) {}
	                    p.destroy();
	                }
	            }

	            if (macAddress != null) {
	                if (macAddress.indexOf(':') != -1) {
	                    clockSeqAndNode |= Hex.parseLong(macAddress);
	                }
	                else if (macAddress.startsWith("0x")) {
	                    clockSeqAndNode |= Hex.parseLong(macAddress.substring(2));
	                }
	            }
	            else {
	                try {
	                    byte[] local = InetAddress.getLocalHost().getAddress();
	                    clockSeqAndNode |= (local[0] << 24) & 0xFF000000L;
	                    clockSeqAndNode |= (local[1] << 16) & 0xFF0000;
	                    clockSeqAndNode |= (local[2] << 8) & 0xFF00;
	                    clockSeqAndNode |= local[3] & 0xFF;
	                }
	                catch (UnknownHostException ex) {
	                    clockSeqAndNode |= (long) (Math.random() * 0x7FFFFFFF);
	                }
	            }

	            // Skip the clock sequence generation process and use random instead.

	            clockSeqAndNode |= (long) (Math.random() * 0x3FFF) << 48;

	        
	        }

			return macAddress;
	 }
}
