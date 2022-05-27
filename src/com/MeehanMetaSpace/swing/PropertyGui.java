package com.MeehanMetaSpace.swing;

import java.util.*;
import javax.swing.*;
import com.MeehanMetaSpace.*;

/**
 * <p>Title: FacsXpert client</p>
 * <p>Description: Workflow planner for FACS research</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: ScienceXperts Inc.</p>
 * @author not attributable
 * @version beta 3
 */

public class PropertyGui {
    public interface Editor {
        void bind(JPanel jp);

        Object read();
    }


    public interface Factory {
        Editor instantiate(Properties properties, String property, String label,
                           String toolTip, Object defaultValue);

    }


    static {
        InterfaceBroker.registerImplementation(Factory.class, Boolean.class,
                                               _Boolean.class);
    }

    public static class _Boolean implements Factory {

        public _Boolean() {
        }

        public Editor instantiate(
          final Properties properties,
          final String property,
          final String label,
          final String toolTip,
          final Object defaultValue) {
            return new Editor() {
                final JCheckBox cb = new JCheckBox(label);
                public void bind(final JPanel jp) {
                    cb.setToolTipText(Basics.toHtmlUncentered(label, toolTip));
                    final boolean b = PropertiesBasics.getProperty(
                      properties,
                      property,
                      defaultValue == null ? false :
                      ((Boolean) defaultValue).booleanValue());
                    cb.setSelected(b);
                    jp.add(cb);
                }

                public Object read() {
                    final Boolean b = Boolean.valueOf(cb.isSelected());
                    properties.setProperty(property, b.toString());
                    return b;
                }
            };
        }

    }


    static {
        InterfaceBroker.registerImplementation(Factory.class, Integer.class,
                                               _Integer.class);
    }

    public static class _Integer implements Factory {

        public _Integer() {
        }

        public Editor instantiate(
          final Properties properties,
          final String property,
          final String label,
          final String toolTip,
          final Object defaultValue) {
            return new TextEditor(properties, property, label, toolTip,
                                  defaultValue) {
                int getColumns() {
                    return 6;
                }

                String encode() {
                    return Basics.encode(defaultValue);
                }

                public Object read() {
                    final String value = tf.getText();
                    properties.setProperty(property, value);
                    int i = Integer.parseInt(value);
                    return new Integer(i);
                }
            };
        };
    }


    static {
        InterfaceBroker.registerImplementation(Factory.class, Object.class,
                                               _Object.class);
    }

    public static class _Object implements Factory {

        public _Object() {
        }

        public Editor instantiate(
          final Properties properties,
          final String property,
          final String label,
          final String toolTip,
          final Object defaultValue) {
            return new TextEditor(properties, property, label, toolTip,
                                  defaultValue) {
                int getColumns() {
                    if (defaultValue == null) {
                        return 20;
                    }
                    int c = defaultValue.toString().length();
                    if (c < 5) {
                        return 5;
                    }
                    if (c > 40) {
                        return 40;
                    }
                    return c;
                }

                public Object read() {
                    final String value = tf.getText();
                    properties.setProperty(property, value);
                    return value;
                }
            };
        };
    }


    static abstract class TextEditor implements Editor {
        private final Properties properties;
        private final String property, label, toolTip;
        final Object defaultValue;
        abstract int getColumns();

        TextEditor(
          final Properties properties,
          final String property,
          final String label,
          final String toolTip,
          final Object defaultValue) {
            this.properties = properties;
            this.property = property;
            this.label = label;
            this.toolTip = toolTip;
            this.defaultValue = defaultValue;
        }

        String encode() {
            return defaultValue == null ? null : defaultValue.toString();
        }

        final protected JTextField tf = new JTextField();
        public void bind(final JPanel jp) {
            tf.setToolTipText(Basics.toHtmlUncentered(label, toolTip));
            final String value = properties.getProperty(property, encode());
            tf.setText(value);
            tf.setColumns(getColumns());
            jp.add(new JLabel(label));
            jp.add(tf);
        }
    }


    public static Factory getFactory(final Class type) {
        return (Factory) InterfaceBroker.getImplementation(Factory.class, type);
    }
}
