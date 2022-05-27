package com.MeehanMetaSpace.swing;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.TransferHandler;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

import com.MeehanMetaSpace.Basics;
import com.MeehanMetaSpace.Credentials;

public class Browser extends JPanel implements HyperlinkListener,
    ActionListener {
	String launchTimeURL = null; // This is for fix of Bug Id 8 and 9 : to
									// rememeber the Launchtime URL
									// (http://www.ScienceXperts.net/CytoGenieLoaded.htm)

	protected URLundoManager history = new URLundoManager();// This is for
															// Browser's History
															// which will be
															// used for Back and
															// Forward Buttons

	String m_CurrentURL;
  // for things like passwords
  public interface UrlAdjuster {
    URL adjust(URL url);
  }

  private UrlAdjuster urlAdjuster = new UrlAdjuster() {
    public URL adjust(final URL url) {
      return url;
    }
  };

  public void setUrlAdjuster(final UrlAdjuster urlAdjuster) {
    this.urlAdjuster = urlAdjuster;
  }

  public static void main(final String[] args) {
      Basics.gui=PopupBasics.gui;
	 if (args.length == 0) {
      show("file://\\temp\\seaweed.jpg", "seaweed");
    } else {
      final Browser b=show(args[0], null);
      if (b != null){
          b.setDefaultHyperlinkListener(false);
          b.addHyperlinkListener(new HyperlinkListener() {
              public void hyperlinkUpdate(final HyperlinkEvent e) {
                  final HyperlinkEvent.EventType et=e.getEventType();
                  if (et.equals(HyperlinkEvent.EventType.ACTIVATED)) {
                      URL url = e.getURL();
                      final String p = url.getPath();

                      PopupBasics.alert(Basics.startHtml("Not supported") + "<i>" +
                                        url.getPath() +
                                        " </i><br>is not a supported document ", true);

                  }
              }

          });
      } else {
          PopupBasics.alert("Can not browser "+args[0]);
      }
    }
  }


public void pushHyperLinksToExternalBrowser(){
    addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(final HyperlinkEvent e) {
                    final HyperlinkEvent.EventType et = e.getEventType();
                    if (et.equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        final URL url = e.getURL();
                        SwingBasics.showHtml(url.toString());
                    }
                }
            });
    setDefaultHyperlinkListener(false);
}

  private JButton homeButton,backButton,forwardButton;
  private JTextField urlField;
  private JEditorPane htmlPane;

  public void setTransferHandler(final TransferHandler th){
      urlField.setTransferHandler(th);
      htmlPane.setTransferHandler(th);
  }

  private URL initialURL;

  public static Browser show(final String initialURL, final String iconCue) {
    final JFrame frame = new JFrame("Simple Swing Browser");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.dispose();
      }
    });
    final URL u=getURL(initialURL);
    if (u != null){
        final Browser b = new Browser(u, iconCue);
        frame.getContentPane().add(b);
        frame.pack();
        final Dimension screenSize = frame.getToolkit().getScreenSize();
        final int width = screenSize.width * 8 / 10;
        final int height = screenSize.height * 8 / 10;
        frame.setBounds(width / 8, height / 8, width, height);
        frame.show();
        return b;
    }
    return null;
  }

  private static URL getURL(final String urlText) {
    URL url = null;
    if (urlText != null) {
      try {
        url = new URL(urlText);
      } catch (final MalformedURLException e) {
        e.printStackTrace(System.err);
      }
    }
    return url;
  }

  private final String iconCue;

  public Browser(final URL initialURL, final String iconCue) {
    this(initialURL, iconCue, null, null);
  }

  public Browser(final URL initialURL, final String iconCue,
                 final UrlAdjuster urlAdjuster, final Window toClose) {
    super(new BorderLayout());
    history= new URLundoManager();
    m_CurrentURL=launchTimeURL;
    if (urlAdjuster != null) {
      this.urlAdjuster = urlAdjuster;
    }
    this.iconCue = iconCue;
    this.initialURL = initialURL;
    homeButton = new JButton(MmsIcons.getHomeIcon());
    backButton = new JButton(MmsIcons.getLeftIcon());
    forwardButton = new JButton(MmsIcons.getRightIcon());
    homeButton.addActionListener(this);
    backButton.addActionListener(this);
    forwardButton.addActionListener(this);
    urlField = new JTextField(30);
    if (iconCue != null) {
			final JPanel urlPanel = new JPanel();
			urlPanel.setBackground(Color.lightGray);
			urlField.addActionListener(this);
			urlField.setText(iconCue == null ? initialURL == null ? ""
					: initialURL.toString() : iconCue);
			urlPanel.add(backButton);
			urlPanel.add(forwardButton);
			urlPanel.add(homeButton);
			urlPanel.add(urlField);
			backButton.setPreferredSize(homeButton.getPreferredSize());
			forwardButton.setPreferredSize(homeButton.getPreferredSize());
			backButton.setMinimumSize(homeButton.getMinimumSize());
			forwardButton.setMinimumSize(homeButton.getMinimumSize());
			backButton.setMaximumSize(homeButton.getMaximumSize());
			forwardButton.setMaximumSize(homeButton.getMaximumSize());

			final JPanel jp = new JPanel(new BorderLayout()), jp2 = new JPanel();
			jp.add(urlPanel, BorderLayout.CENTER);
			if (toClose != null) {
				final JButton b = SwingBasics.getDoneButton(toClose,
						"Close browser");
				b.setText("Close");
				jp2.add(b);
				jp.add(jp2, BorderLayout.EAST);
			}
			add(jp, BorderLayout.SOUTH);
			if (initialURL != null) {
				actionPerformed(new ActionEvent(urlField, 0, "go"));
			}
			backButton.setEnabled(false);
			forwardButton.setEnabled(false);
		}
  }



  private JComponent centerComponent;
  private void setMainComponent(final JComponent newComponent,
                                final String borderLayout) {
    if (newComponent != centerComponent) {
      if (centerComponent != null) {
        remove(centerComponent);
      }
      add(newComponent, borderLayout);
      centerComponent = newComponent;
      updateUI();
    }
  }

  private JScrollPane htmlScrollPane;
  private URL _url;

  private void setHtmlViaText(final String text) {
    htmlPane = new JEditorPane("text/html", text);
    _url = null;
    htmlPane.setEditable(false);
    htmlPane.addHyperlinkListener(this);
    htmlScrollPane = new JScrollPane(htmlPane);
    
  }

  private void setHtml(final URL url) {
    try {
      htmlPane = new JEditorPane();
      final URL u = urlAdjuster.adjust(url);
      htmlPane.setEditable(false);
      htmlPane.addHyperlinkListener(this);
      htmlScrollPane = new JScrollPane(htmlPane);
      if (u != null) {
        _url = u;
        htmlPane.setPage(_url);
      }
    } catch (final IOException ioe) {
      warnUser("The site/resource can not be accessed:&nbsp;&nbsp;<b>" + ioe.getMessage() + "</b>");
    }
  }

  public void actionPerformed(final ActionEvent event) {
    if (event.getSource() == urlField) {
      setURL(urlField.getText());
    }
    else
    	if (event.getSource() == backButton) {
            Thread runner = new Thread() {
                public void run() {
                  try {
                    String mDoURL = history.swapURL(m_CurrentURL);
                    history.undo();  //URL now in redo
                    displayPageDirect(mDoURL);
                  }
                  catch (CannotUndoException exc) {}
                  finally {
                    updateMenu_Buttons();
                  }
                }
              };
              runner.start();
            }
    	else
    		if (event.getSource() == forwardButton) {
    	        Thread runner = new Thread() {
    	            public void run() {
    	              try {
    	            	  history.redo();
    	                displayPageDirect(history.swapURL(m_CurrentURL));
    	              }
    	              catch (CannotRedoException exc) {}
    	              finally {
    	                updateMenu_Buttons();
    	              }
    	            }
    	          };
    	          runner.start();
    	        }
    else { // Clicked "home" button instead of entering URL
      setURL(initialURL.toString());
      urlField.setText(launchTimeURL);
    }
  }

  public void setURL(final String _txt) {

    if (_txt.equals(iconCue) ||
        (initialURL != null && !Basics.isEmpty(iconCue) && _txt.equals(initialURL.toString()))) {
    	final JPanel jp=new JPanel();
    	jp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    	jp.add(new JLabel(new ImageIcon(initialURL)));
      setMainComponent(jp,
                       BorderLayout.NORTH);

    } else {
      String txt = _txt;
      if (txt.indexOf(":\\") !=-1) {          //for Fix of Bug Id 8 and 9
      txt =  txt.replace(":\\\\","://");
    }

      if (txt.indexOf(":/") < 1) {
        txt = "http://" + txt;
      }
      try {
        setURL(new URL(txt));
      } catch (final MalformedURLException e) {
        PopupBasics.alert(e.toString());
      }
    }

  }

  public URL getURL() {
    return _url;
  }

  public void setURL(final URL url) {
	  if(launchTimeURL==null)
	  launchTimeURL=url.toString();
    if (url != null) {
      if (htmlPane == null) {
        setHtml(url);
      } else {
        try {
          final URL u = urlAdjuster.adjust(url);
          if (u != null) {
            _url = u;
            htmlPane.setPage(_url);
          }
        } catch (final IOException ioe) {
          warnUser("The site/resource can not be accessed:&nbsp;&nbsp;<b>" + ioe.getMessage() + "</b>");
        }
      }
      if (htmlScrollPane != null) {
        setMainComponent(htmlScrollPane, BorderLayout.CENTER);
      }
      if (_url != null){
        String s = _url.toExternalForm();
        final int idx = s.indexOf("?"+Credentials.HTTP_PARAMETER_PASSWORD+"=");
        // hide password details
        if (idx >= 0) {
          s = s.substring(0, idx);
        }
        urlField.setText(s);
      }
    }

  }

  public void addHyperlinkListener(final HyperlinkListener hll) {
    if (htmlPane != null) {
      htmlPane.addHyperlinkListener(hll);
    }
  }

  private boolean useDefaultHyperlinkListener = true;

  public void setDefaultHyperlinkListener(final boolean on) {
    useDefaultHyperlinkListener = on;
  }

  public void setText(final String text) {
    if (text != null) {
      if (htmlPane == null) {
        setHtmlViaText(text);
      } else {
        htmlPane.setText(text);
        _url = null;
      }
      setMainComponent(htmlScrollPane, BorderLayout.CENTER);
      //urlField.setText(url.toExternalForm());
    }

  }

  public void setUrlField(final boolean enabled, final int columns){
    urlField.setEnabled(enabled);
    urlField.setColumns(columns);
  }

  public void hyperlinkUpdate(final HyperlinkEvent event) {
      if (useDefaultHyperlinkListener) {
          if (event != null &&
              event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              try {
                  final URL url = urlAdjuster.adjust(event.getURL());
                  if (url != null) {
                      htmlPane.setPage(url);
                  }
                  final String dest = event.getURL().toString();
                  Thread runner = new Thread() {
                      public void run() {
                          display_RecordUndo(dest);
                      }
                  };
                  runner.start();
                  urlField.setText(event.getURL().toExternalForm());
              } catch (final IOException ioe) {
                  warnUser("The site/resource can not be accessed:&nbsp;&nbsp;<b>" +
                           ioe.getMessage() + "</b>");
              }
          }
      }
  }


  private void warnUser(final String message) {
    htmlPane.setContentType("text/html");
    final String s=Basics.toHtmlErrorUncentered("Problem reading URL", message);
    htmlPane.setText(s);
  }

  public void displayPageDirect(String strURL) {
		m_CurrentURL = strURL;
		urlField.setText(strURL);

		try {
			htmlPane.setPage(strURL);
		} catch (Exception exc) {
			System.out.println("Problem loading URL...");
		}

	}

	public void display_RecordUndo(String strURL) {
		String mCompareURL = strURL.intern();
		if(m_CurrentURL==null)
			m_CurrentURL=launchTimeURL;

		if (m_CurrentURL!=null && m_CurrentURL.toString() != mCompareURL) {
			history.addURL(m_CurrentURL.toString());
			updateMenu_Buttons();
			displayPageDirect(mCompareURL);
		}
	}

	public void updateMenu_Buttons() {
		boolean mDoState = history.canUndo();

		backButton.setEnabled(mDoState);

		if (mDoState) {
			backButton.setToolTipText(history.getUndoPresentationName());
		} else {
			backButton.setToolTipText(null);
		}

		mDoState = history.canRedo();

		forwardButton.setEnabled(mDoState);

		if (mDoState) {
			forwardButton.setToolTipText(history.getRedoPresentationName());
		} else {
			forwardButton.setToolTipText(null);
		}
	}

	class UndoableURL extends AbstractUndoableEdit {
		private String m_URL;

		public UndoableURL(String m_URL) {
			this.m_URL = m_URL;
		}

		public String getPresentationName() {
			return m_URL;
		}
	}

	class URLundoManager extends CompoundEdit {
		int m_IdxAdd = 0;

		public String getUndoPresentationName() {
			return ((UndoableURL) edits.elementAt(m_IdxAdd - 1))
					.getPresentationName();
		}

		public String getRedoPresentationName() {
			return ((UndoableURL) edits.elementAt(m_IdxAdd))
					.getPresentationName();
		}

		public void addURL(String newURL) {
			if (edits.size() > m_IdxAdd) {
				edits.setElementAt(new UndoableURL(newURL), m_IdxAdd++);
				for (int i = m_IdxAdd; i < edits.size(); i++) {
					edits.removeElementAt(i);
				}
			} else {
				edits.addElement(new UndoableURL(newURL));
				m_IdxAdd++;
			}
		}

		public String swapURL(String newURL) {
			String m_oldURL = getUndoPresentationName();
			edits.setElementAt(new UndoableURL(newURL), m_IdxAdd - 1);
			return m_oldURL;
		}

		public synchronized boolean canUndo() {
			if (m_IdxAdd > 0) {
				UndoableURL edit = (UndoableURL) edits.elementAt(m_IdxAdd - 1);
				return edit != null && edit.canUndo();
			}
			return false;
		}

		public synchronized boolean canRedo() {
			if (edits.size() > m_IdxAdd) {
				UndoableURL edit = (UndoableURL) edits.elementAt(m_IdxAdd);
				return edit != null && edit.canRedo();
			}
			return false;
		}

		public synchronized void undo() throws CannotUndoException {
			((UndoableURL) edits.elementAt(--m_IdxAdd)).undo();
		}

		public synchronized void redo() throws CannotRedoException {
			((UndoableURL) edits.elementAt(m_IdxAdd++)).redo();
		}
	}
}
