
package com.MeehanMetaSpace.swing;

import java.awt.font.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import com.MeehanMetaSpace.Basics;

public class WaterMarkButton extends JButton {
    private int currentImageIndex = 0;
    private Image[] waterMarkImages;
    final public static boolean defaultDrawValue = false;
    private boolean showWaterMark;

    public WaterMarkButton(final String label, final Image[] image) {
        super(label);
        setWaterMarkImages(image);
        initialize(defaultDrawValue);
    }
    
    private void setWaterMarkImages(final Image[] image) {
    	boolean isNullImage = false;
        for (Image img: image) {
        	if (img == null) {
        		isNullImage = true;
        		break;
        	}
        }
        if (isNullImage) {
        	waterMarkImages = null;        	
        }
        else {
        	waterMarkImages = image;
        }
    }

    public WaterMarkButton(final String label, final Image[] image,
                           final boolean showWaterMarkNow) {
        super(label);
        setWaterMarkImages(image);
        initialize(showWaterMarkNow);
    }

    public WaterMarkButton(final String label, final Image[] image,
                           final Icon icon, final boolean showWaterMarkNow) {
        super(label, icon);
        setWaterMarkImages(image);
        initialize(showWaterMarkNow);
    }


    public WaterMarkButton(Action action, final Image[] image,
                           final boolean showWaterMarkNow) {
        super(action);
        setWaterMarkImages(image);
        initialize(showWaterMarkNow);
    }

    public WaterMarkButton(
      final Action action, final Image[] image) {
        super(action);
        setWaterMarkImages(image);
        initialize(defaultDrawValue);
    }


    private void initialize(final boolean showWaterMarkNow) {
        showWaterMark = showWaterMarkNow;

        if (waterMarkImages != null && isShowingWaterMarks()) {
            int maxW = -1, maxH = -1;
            for (int i = 0; i < waterMarkImages.length; i++) {
                Dimension d = new Dimension(
                  waterMarkImages[i].getWidth(null),
                  waterMarkImages[i].getHeight(null));
                if (d.width < 0 || d.height < 0) {
                    System.err.println("Image size problem index=" + i +
                                       ". text=\"" + getText() + "\" width=" +
                                       d.width + ", height=" + d.height);
                    d = new Dimension(
                      waterMarkImages[i].getWidth(null),
                      waterMarkImages[i].getHeight(null));

                }
                if (d.width > maxW) {
                    maxW = d.width;
                }
                if (d.height > maxH) {
                    maxH = d.height;
                }
            }
            maxW+=10;
            maxH+=6;
            Dimension preferredSizeWithoutImage = getPreferredSize();
            if (preferredSizeWithoutImage.width > maxW) {
                maxW = preferredSizeWithoutImage.width;
            }
            if (preferredSizeWithoutImage.height > maxH) {
                maxH = preferredSizeWithoutImage.height;
            }
            Dimension d=new Dimension(maxW, maxH);
            sizeWithImage=d;
            d=sizeWithImage;
            setPreferredSize(d);
        }
    }

    private Dimension sizeWithImage;
    protected boolean isShowingWaterMarks(){
        return true;
    }

    public void paintComponent(final Graphics g) {
        // if a foreground image exists, paint it
        if (waterMarkImages != null && showWaterMark && isShowingWaterMarks()) {
            super.paintComponent(g);

            final Dimension d = getSize();
            final int imageW = waterMarkImages[currentImageIndex].getWidth(null);
            final int imageH = waterMarkImages[currentImageIndex].getHeight(null);

            // we need to cast to Graphics2D for this operation
            final Graphics2D g2d = (Graphics2D) g;

            // create the composite to use for the translucency
            final AlphaComposite comp = AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER, 0.85f);

            // save the old composite
            final Composite oldComp = g2d.getComposite();

            // set the translucent composite
            g2d.setComposite(comp);

            // calculate the x and y positions to paint at
            final int xloc = (d.width - imageW) / 2;
            final int yloc = (d.height - imageH) / 2;

            // paint the image using the new composite
            g2d.drawImage(waterMarkImages[currentImageIndex], xloc, yloc, this);

            if (text1 != null){
                redraw2Lines(g);
            } else
            // restore the original composite
            if (redrawTxt != null) {
                final FontMetrics fm = this.getFontMetrics(getFont());
                final int fontHeight = fm.getAscent();

                g2d.setComposite(oldComp);
                final int fontWidth = fm.charsWidth(redrawTxt.toCharArray(), 0,
                  redrawTxt.length());
                final int xTxt = d.width / 2 - (fontWidth / 2);
                final int yTxt = d.height / 2 + (fontHeight / 2);
                g2d.drawString(redrawTxt, xTxt, yTxt);
            }
        } else {
            super.paintComponent(g);
            if (text1 != null){
                redraw2Lines(g);
            }

        }
    }
    private String text1, text2;
    void setText(final WaterMarkButton other){
        if (other.text1 != null){
            setText(other.text1, other.text2);
        }
    }

    public void setIcon(Icon i){
        super.setIcon(i);
        if (i != null){
            super.setIconTextGap(25);
            setDummyText();
        }
    }

    private void setDummyText(){
        if (text1 != null){
            final int max = text1.length() > text2.length() ? text1.length() :
                            text2.length();
            final String s = Basics.makeString(' ', max);
            setText(s);
        }
    }
    public void setText(final String text1, final String text2){
        this.text1=text1;
        this.text2=text2;
        if (getIcon() != null){
            setDummyText();
        }
    }

    private final void redraw2Lines(final Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        final Composite oldComp = g2.getComposite();

     int w = getWidth();
     int h = getHeight();

     FontMetrics fm = g.getFontMetrics();
     int textw1 = fm.stringWidth(text1);
     int textw2 = fm.stringWidth(text2);

     FontRenderContext context = g2.getFontRenderContext();
     LineMetrics lm = getFont().getLineMetrics(text1, context);
     int texth = (int)lm.getHeight();

     int x1 = (w - textw1) / 2;
     int x2 = (w - textw2) / 2;
     int th = (texth * 2);
     int dh = ((h - th) / 2);
     int y1 = dh + texth - 3;
     int y2 = y1 + texth;
     g2.setComposite(oldComp);

     // Draw texts
     g.setColor(getForeground());

     g.drawString(text1, x1, y1);
     g.drawString(text2, x2, y2);

    }

    private String redrawTxt = null;

    public int getImageIndex(){
        return currentImageIndex;
    }

    public void removeImage() {
        showWaterMark = false;
        if (redrawTxt != null) {
            setText(redrawTxt);
            redrawTxt = null;
        }
    }

    public String getRealText(){
        return redrawTxt==null? getText():redrawTxt;
    }

    public void setWaterMarkImage(final int imageIndex) {
        if (redrawTxt==null){
            final String txt = getText();
            final boolean okToRedrawTxt = txt != null &&
                                          txt.toLowerCase().indexOf("<html>") <
                                          0;
            if (okToRedrawTxt) {
                setText(Basics.duplicate(' ', txt.length() + 5));
                this.redrawTxt = txt;
            }
        }
        showWaterMark = true;
        currentImageIndex = imageIndex;
        final Dimension d=sizeWithImage;
        setPreferredSize(d);
    }

    public static void main(final String[] args) {

        class Button extends WaterMarkButton implements ActionListener {
            Button(final String txt, final Image doingImage, final Image doneImage, final boolean showImage) {
                super(txt, new Image[]{ doingImage, doneImage},showImage);
                addActionListener(this);
            }

            Button(
              final String txt,
              final Icon icon,
              final Image doingImage,
              final Image doneImage,
              final boolean showImage) {
                super(txt, new Image[]{ doingImage, doneImage}, icon, showImage);
                addActionListener(this);
            }

            public void actionPerformed(final ActionEvent e) {
                if (getImageIndex() == 0) {
                    setWaterMarkImage(1);
                } else {
                    setWaterMarkImage(0);
                }
            }
        };

        final Toolkit kit = Toolkit.getDefaultToolkit();
        final Image doneImage=kit.getImage("C:\\FacsXpert_beta_4_19\\src\\edu\\stanford\\herzenberg\\notebook\\images\\finishedButtonArrow.gif"),
                    doingImage=kit.getImage("C:\\FacsXpert_beta_4_19\\src\\edu\\stanford\\herzenberg\\notebook\\images\\downArrow.gif");
        final ImageIcon plusIcon = new ImageIcon(TaskButton.class.getClassLoader().
                                       getResource(
                                         "com/MeehanMetaSpace/swing/images/plus.gif")),
                                   refreshIcon = new ImageIcon(TaskButton.class.getClassLoader().
                                       getResource(
                                         "com/MeehanMetaSpace/swing/images/refresh16.gif"));
        Button bugWhereFirstButtonInstantiatedDisappears =
          new Button("wierd bug", doingImage, doneImage, true);

        final Button niko = new Button(
          "Niko", plusIcon, doingImage, doneImage, true);
        final Button sam = new Button(
          "Sam", refreshIcon, doingImage, doneImage, false);
        final Button rooFish = new Button(
          "Roo-fish", doingImage, doneImage, false);
        final WaterMarkButton pepper = new Button(
          "<HTML><BODY>Pepper<BR>golden retriever</BODY></HTML>", doingImage, doneImage, true);

        // Create a frame in which to show the button.
        JFrame frame = new JFrame();
        SwingBasics.buttonEqualSizes=PopupBasics.ask("Equal size?");
        JPanel jp = SwingBasics.getButtonPanel(4);
        jp.add(pepper);
        jp.add(sam);
        jp.add(niko);
        jp.add(rooFish);
        frame.getContentPane().add(jp);
        frame.pack();
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}


