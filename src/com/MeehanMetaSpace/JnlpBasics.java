package com.MeehanMetaSpace;

import javax.jnlp.*;
import javax.swing.SwingUtilities;
import java.net.*;
import java.io.IOException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class JnlpBasics {
    static public DownloadServiceListener silentListener = new
      DownloadServiceListener() {
        /**
         * downloadFailed
         *
         * @param uRL URL
         * @param string String
         * @todo Implement this javax.jnlp.DownloadServiceListener method
         */
        public void downloadFailed(URL uRL, String string) {
        }

        /**
         * progress
         *
         * @param uRL URL
         * @param string String
         * @param long2 long
         * @param long3 long
         * @param int4 int
         * @todo Implement this javax.jnlp.DownloadServiceListener method
         */
        public void progress(URL uRL, String string, long long2, long long3,
                             int int4) {
        }

        /**
         * upgradingArchive
         *
         * @param uRL URL
         * @param string String
         * @param int2 int
         * @param int3 int
         * @todo Implement this javax.jnlp.DownloadServiceListener method
         */
        public void upgradingArchive(URL uRL, String string, int int2, int int3) {
        }

        /**
         * validating
         *
         * @param uRL URL
         * @param string String
         * @param long2 long
         * @param long3 long
         * @param int4 int
         * @todo Implement this javax.jnlp.DownloadServiceListener method
         */
        public void validating(URL uRL, String string, long long2, long long3,
                               int int4) {
        }

    };
    private static int pingCount=0;
    private static Boolean ping;
    public static String pingOnlineUrl;
    public static boolean isOnline() {
    	if (System.getProperty("getdown") != null) {
    		
    		try {
    			URL url = new URL(System.getProperty("getdownpath"));
    			URLConnection uconn = url.openConnection();
    			uconn.setUseCaches(false);
        		uconn.getInputStream();
        		return true;
    		}
    		catch(Exception e) {
    			return false;
    		}
    	}
        boolean ok = false;
        try {
            final BasicService basicService = (BasicService) ServiceManager.lookup(
              "javax.jnlp.BasicService");
            ok = !basicService.isOffline();
            if (pingOnlineUrl != null){
                if ((pingCount % 5)==0){
                    ok = IoBasics.ping(pingOnlineUrl);
                    ping = Boolean.valueOf(ok);
                    System.out.println("Pinging " + (ok ? "succeeded" : "failed") +
                                       " for " + pingOnlineUrl);
                } else {
                    ok=ping.booleanValue();
                }
            }
            pingCount++;
        } catch (final javax.jnlp.UnavailableServiceException use) {
            //use.printStackTrace();
        }
        return ok;
    }

    public static String getWebAppRootForClient() {
    	String webAppRoot = null;
    	URL codeBase = JnlpBasics.getCodeBase(); 
    	if (codeBase != null) {
    		webAppRoot = codeBase.toExternalForm();
    		if (webAppRoot != null) {
    			final int idx = webAppRoot.indexOf("/users");
    			if (idx >= 0) {
    				webAppRoot = webAppRoot.substring(0, idx);
    			}
    			//else return code base as-is
    		}
    	}		
		return webAppRoot;
	}
    
	public static String getDNSFolder(){
		String codeBase = null;
		if(JnlpBasics.isOnline()){
			codeBase = JnlpBasics.getCodeBase().toString();
		}else{
			if(pingOnlineUrl != null)
				codeBase = pingOnlineUrl;
			else
				return "localhost";
		}
    	codeBase = codeBase.replaceAll("http://", "");
    	String dnsName = codeBase.substring(0,codeBase.indexOf('/'));
    	dnsName = dnsName.replace('/', '_').replace(':', '_').replace('?', '_');
        return dnsName;
	}

    public static URL getCodeBase() {
    	if (System.getProperty("getdown") != null) {
    		try {
        		return new URL(System.getProperty("getdownpath"));
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
    	}
        URL codeBase = null;
        try {
            final BasicService bs = (BasicService) ServiceManager.lookup(
              "javax.jnlp.BasicService");
            codeBase = bs.getCodeBase();
        } catch (final javax.jnlp.UnavailableServiceException use) {
            // Do nothing as in isOnline() above.
        }
        return codeBase;
    }

    public static boolean isActive() {
        return ServiceManager.getServiceNames().length > 0;
    }

    /**
     * Augment basic JNLP service with thread safety, pel loggin and simplicity
     *
     * @param resourceUrl String
     * @param extensionPart String
     * @return boolean
     * @throws URISyntaxException 
     */
    public static void download(final String resourceUrl,
                                final String extensionPart,
                                final DownloadServiceListener progressWindow) throws IOException, URISyntaxException{
        if (resourceUrl != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                //boolean ok = false;
                DownloadService ds;
                final boolean doingPart = !Basics.isEmpty(extensionPart);
                try {
                    ds = (DownloadService) ServiceManager.lookup(
                      "javax.jnlp.DownloadService");
                } catch (UnavailableServiceException e) {
                    ds = null;
                }
                if (ds != null) {
                    try {
                        Pel.log.println("Starting JNLP download of " + resourceUrl + " (" +
                                        extensionPart + ")");
                        // determine if a particular resource is cached
                        URL url = new URL(resourceUrl);
            			URI uri = new URI(url.getProtocol(), url.getUserInfo(),
            					url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
            					null);
            			url = uri.toURL();
                        final boolean cached = doingPart ?
                                               ds.isExtensionPartCached(url, null,
                          extensionPart) :
                                               ds.isResourceCached(url, null);
                        // remove the resource from the cache
                        if (cached) {
                            Pel.log.println(url.toString() + " is cached");
                            if (doingPart) {
                                ds.removeExtensionPart(url, null, extensionPart);
                            } else {
                                ds.removeResource(url, null);
                            }
                        }
                        // reload the resource into the cache
                        final DownloadService ds2 = ds;
                        final DownloadServiceListener dsl = progressWindow != null ?
                          progressWindow : ds.getDefaultProgressWindow();
                        if (Basics.isEmpty(extensionPart)) {
                            ds2.loadResource(url, null, dsl);
                        } else {
                            ds2.loadExtensionPart(url, null, extensionPart, dsl);
                        }
                        //ok = true;
                        Pel.log.println("Completed JNLP download of " + resourceUrl +
                                        " (" +
                                        extensionPart + ")");
                    } catch (final IOException e) {
                        e.printStackTrace();
                        throw e;
                    } catch (URISyntaxException e) {
                    	e.printStackTrace();
                        throw e;
					}

                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try{
                            download(resourceUrl, extensionPart, progressWindow);
                        } catch (final IOException e){

                        } catch (URISyntaxException e) {
                        	
						}
                    }
                });
            }
        }
    }
}
