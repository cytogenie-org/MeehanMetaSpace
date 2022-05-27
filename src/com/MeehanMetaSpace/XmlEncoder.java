package com.MeehanMetaSpace;

import java.io.PrintWriter;

public class XmlEncoder {
    private final String namespace;
    private int tabIndents = 0;
    private final PrintWriter pw;

    public XmlEncoder(final PrintWriter pw, final String nameSpace) {
        this.namespace = Basics.encodeXmlOrHtml(nameSpace);
        this.pw = pw;
    }

    public void setTabIndents(final int tabIndents) {
        this.tabIndents = tabIndents;
    }

    public void println(final Object whatEver) {
        pw.println(whatEver);
    }

    private void _open(final String node) {
        pw.print("<");
        pw.print(namespace);
        pw.print(":");
        pw.print(Basics.encodeXmlOrHtml(node));

    }

    public void node(final String node) {
        printTabIndents();
        _open(node);
        pw.println(">");
        tabIndents++;
    }

    public void node(
      final String nodeName,
      final String attributeName,
      final Object attributeValue) {
        printTabIndents();
        _open(nodeName);
        pw.print(' ');
        pw.print(Basics.encodeXmlOrHtml(attributeName));
        pw.print('=');
        pw.print('"');
        pw.print(Basics.encodeXmlOrHtml(attributeValue));
        pw.print('"');
        pw.println(">");
        tabIndents++;
    }

    public void nodeComplete(
      final String node,
      final String attributeName,
      final Object attributeValue) {
        printTabIndents();
        _open(node);
        pw.print(' ');
        pw.print(Basics.encodeXmlOrHtml(attributeName));
        pw.print('=');
        pw.print('"');
        pw.print(Basics.encodeXmlOrHtml(attributeValue));
        pw.print('"');
        pw.println("/>");
    }

    private void _close(final String node) {
        pw.print("</");
        pw.print(namespace);
        pw.print(":");
        pw.print(Basics.encodeXmlOrHtml(node));
        pw.print(">");

    }

    public void nodeEnd(final String nodeName) {
        tabIndents--;
        printTabIndents();
        _close(nodeName);
        pw.println();
    }

    public void printTabIndents() {
        if (tabIndents > 0) {
            for (int tab = 0; tab < tabIndents; tab++) {
                pw.print('\t');
            }
            pw.print("    ");
        }

    }


    public void node(
      final String node,
      final Object nodeText) {

        if (node != null) {
            printTabIndents();
            if (nodeText != null) {
                _open(node);
                pw.print(">");
                pw.print(Basics.encodeXmlOrHtml(nodeText));
                _close(node);
            } else {
                _open(node);
                pw.print("/>");
            }
        }
        pw.println();
    }

}
