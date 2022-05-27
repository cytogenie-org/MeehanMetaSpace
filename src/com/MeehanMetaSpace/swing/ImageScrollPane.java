package com.MeehanMetaSpace.swing;

// File: ImageScrollPane.java
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import com.sun.java.swing.plaf.motif.*;
//import com.sun.java.swing.plaf.windows.*;


public class ImageScrollPane extends JScrollPane
{
	public ImageScrollPane(Component view, int vsbPolicy, int hsbPolicy)
	{
		super( view, vsbPolicy, hsbPolicy );
		// Set the component to transparent
		if( view instanceof JComponent )
			((JComponent)view).setOpaque(false);
	}

	public ImageScrollPane(Component view)
	{
		 this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public ImageScrollPane(int vsbPolicy, int hsbPolicy)
	{
		this(null, vsbPolicy, hsbPolicy);
	}

	public ImageScrollPane()
	{
		this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public void paint(Graphics g)
	{
		if( image != null )
		{
			// Draw the background image
		   	Rectangle d = getViewport().getViewRect();
			for( int x = 0; x < d.width; x += image.getIconWidth() )
				for( int y = 0; y < d.height; y += image.getIconHeight() )
					g.drawImage( image.getImage(), x, y, null, null );
			// Do not use cached image for scrolling
			getViewport().setBackingStoreEnabled(false);
		}
		super.paint( g );
	}

	public void setBackgroundImage( ImageIcon image )
	{
		this.image = image;
	}

	ImageIcon image = null;

}


