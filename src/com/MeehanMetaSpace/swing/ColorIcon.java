package com.MeehanMetaSpace.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import com.MeehanMetaSpace.Basics;

public class ColorIcon implements Icon {

	private final Color color;
	public Color getColor(){
		return color;
	}
	private final boolean rect;
	public ColorIcon(final Color color, final boolean rect) {
		this(color,rect,3,5,8,8);
	}

	private final int x,y,width,height,iconHeight,iconWidth;
	public ColorIcon(final Color color, final boolean rect, final int x, final int y, final int height, final int width) {
		this.color = color;
		this.rect=rect;
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
		this.iconHeight=y+height+(y*2);
		this.iconWidth=x+width+1;
	}

	public int getIconHeight() {
		return iconHeight;
	}

	public int getIconWidth() {
		return this.iconWidth;
	}

	
	public void paintIcon(final Component c, final Graphics g, int x, int y) {
		if (!Color.white.equals(color)){
			g.setColor(color);
			if (rect){
				g.fillRect(x+this.x, y+this.y, width, height);			
			} else {
				g.fillOval(x+this.x, y+this.y, width,height);			
			}
		}
	}
	
	public URL saveIfNew(final String path, final String fileNamePrefix, final String type){
		final String fileName=Basics.concatObjects(fileNamePrefix, 
				"_", 
				(rect?"rect":"oval"),
				"_", 
				height, "_", width, "__", color.getRed(), "_", color.getGreen(), "_", color.getBlue(), ".", type);
	    final File file = new File(path, fileName);
	    if (file.exists()){
	    	try {
				return file.toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null;
	    }
		// Create an image to save
		final RenderedImage rendImage = myCreateImage();

		// Write generated image to a file
		try {
		    // Save as PNG
		    ImageIO.write(rendImage, type, file);
			final URL url=file.toURL();
			return url;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}
		// Returns a generated image.
	private RenderedImage myCreateImage() {
		    // Create a buffered image in which to draw
		    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		    // Create a graphics contents on the buffered image
		    final Graphics2D g = bufferedImage.createGraphics();
		    g.setBackground(color);
		    g.clearRect(0, 0, width, height);
			if (rect){
				g.fillRect(x+this.x, y+this.y, width, height);			
			} else {
				g.fillOval(x+this.x, y+this.y, width,height);			
			}	
		    // Graphics context no longer needed so dispose it
		    g.dispose();
		    return bufferedImage;
		}
	
}
