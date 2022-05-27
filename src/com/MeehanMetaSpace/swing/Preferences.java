
package com.MeehanMetaSpace.swing;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import com.MeehanMetaSpace.*;
import java.awt.event.*;
import java.io.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class Preferences {
    public interface TabContributor {
        void contribute(JTabbedPane tabs);

        void conclude(boolean cancelled);
        
        void reset();
    }


    private final ArrayList<TabContributor> tabContributors = new ArrayList();

    public void register(final TabContributor tab) {
        tabContributors.add(tab);
    }

    private ColorPreferences colorProperties = ColorPreferences.instantiate();
    private FontPreferences fontProperties = FontPreferences.instantiate();

    public JButton getColorPropertiesButton() {
        final JButton b = SwingBasics.getButton(
          "Color settings",
          null,
          'l',
          null,
          Basics.toHtmlUncentered("Color settings",
                                  "Determine color settings<br>for<i>all</i> various GUI elements"));

        b.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final JPopupMenu pm = colorProperties.getPopupMenu(b);
                pm.show(b, 15, 15);
            }
        });
        return b;
    }
    public JButton getFontPropertiesButton(String name) {
    	fontProperties.updateLnf();
        final JButton b = SwingBasics.getButton(
          name,
          null,
          'z',
          null,
          Basics.toHtmlUncentered("Font settings",
                                  "Determine Font settings<br>for<i>all</i> various GUI elements"));

        b.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
            	if (e.getSource() instanceof JComponent) {
            		fontProperties.showPopupMenu((JComponent)e.getSource());
            	}
            	else {
            		fontProperties.showPopupMenu(b);
            	}
            }
        });
        return b;
    }

    private SwingBasics.Dialog jd =null;
    
    public void show(final String msg, final String defaultTablePropertyFolder) {
    	show(null,msg,defaultTablePropertyFolder);
    }
    
    public void show(final JFrame owner, final String msg, final String defaultTablePropertyFolder) {
        jd = new SwingBasics.Dialog(owner, "Preferences...");
        fontProperties.setDialog(jd);
        jd.setModal(true);
        final JPanel mainPanel = new GradientBasics.Panel(new BorderLayout());
        final JTabbedPane tp = new JTabbedPane();
        for (TabContributor item : tabContributors) {
            item.contribute(tp);
        }
        mainPanel.add(tp, BorderLayout.CENTER);
        final Collection<JButton> c = new ArrayList();
        if (defaultTablePropertyFolder != null) {
            final String txt = "Factory settings";
            final JButton b = SwingBasics.getButton(
              txt,
              null,
              'f',
              new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if (PopupBasics.ask(txt,
                                        "Restore factory settings for <b>all</b> Xpert grids/tables?", false)) {
                    	 for (TabContributor item : tabContributors) {
                             item.reset();
                         }
                    	IoBasics.rmdir(new File(defaultTablePropertyFolder));
                        PopupBasics.alert(
                          "Some settings only take effect after exiting and restarting...",
                          txt, false);
                    }
                }
            },
              Basics.toHtmlUncentered(txt,
                                      "Reset all of the settings<br>for <i>all</i> Xpert grids/tables"));
            c.add(b);
            final JButton cb = getColorPropertiesButton();
            c.add(cb);
            final JButton fb = getFontPropertiesButton("Font settings");
            c.add(fb);
            jd.layout(c, "preferences", msg, mainPanel, true, null, true);
        } else {
            final JButton cb = getColorPropertiesButton();
            c.add(cb);
            jd.layout(c, "preferences", msg, mainPanel, true, null, true);
        }
        GradientBasics.setTransparentChildren(mainPanel, true);
        jd.show();
        for (TabContributor item : tabContributors) {
            item.conclude(jd.accepted);
        }
        colorProperties.setComplete(jd.accepted);
        fontProperties.setComplete(jd.accepted);

    }


}
