package com.MeehanMetaSpace.swing;
import javax.swing.plaf.basic.*;
import javax.swing.JSplitPane;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import com.MeehanMetaSpace.*;

/**
 * <p>Title: FacsXpert</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Herzenberg Lab, Stanford University</p>
 * @author Stephen Meehan
 * @version 1.0
 */

public class PersonalizedView {
	private void removeProperty(final String name){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		properties.remove(theName);
	}

	public void setProperty(final String name, final int value){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		properties.setProperty(theName, Integer.toString(value));
	}

	public void setProperty(final String name, final boolean value){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		properties.setProperty(theName, value?"true":"false");
	}

	public void setProperty(final String name, final String value){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		properties.setProperty(theName, value);
	}

	public String getProperty(final String name, final String theDefault){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		return properties.getProperty(theName, theDefault);
	}

	public int getProperty(final String name, final int theDefault){
		int retVal=theDefault;
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		final String value=properties.getProperty(theName, Integer.toString(theDefault));
		try{
			retVal=Integer.parseInt(value);
		} catch (NumberFormatException nfe){
			Pel.note(nfe);
		}
		return retVal;
	}

	/**
	 *
	 * @return properties that describe the user's personaslizations of the table
	 * since  the last call to updatePropertiesWithPersonalizations().
	 * If this call has not been made with the current instance then the properties
	 * sreflect those made during
	 * the last session with the same table model.
*/
	public Properties getProperties(){
		return properties;
	}

	public boolean getProperty(final String name, final boolean theDefault){
		final String theName= (propertyPrefix!=null?propertyPrefix+"."+name:name);
		final String str=properties.getProperty(theName, theDefault?"true":"false");
		return str==null?theDefault: (str.equalsIgnoreCase("true"));
	}


	final private Properties properties;

	static final String PROPERTY_DIVIDER="divider",
		 PROPERTY_WINDOW_WIDTH="windowWidth",
		 PROPERTY_WINDOW_HEIGHT="windowHeight",
		 PROPERTY_WINDOW_X="windowX",
		 PROPERTY_WINDOW_Y="windowY";
	final private String propertyPrefix;

	public PersonalizedView(final String propertyPrefix, final String fileName){
		this(propertyPrefix, PropertiesBasics.loadProperties(fileName));
	}
   public PersonalizedView(final String propertyPrefix, final Properties properties) {
		this.properties=properties;
		this.propertyPrefix=propertyPrefix;
   }
	private int dividerChanges=0;

	public BasicSplitPaneDivider handleDivider(
			final JSplitPane split,
			final String whichSplitterID){
		final String property=whichSplitterID+"."+PROPERTY_DIVIDER;
		final int dividerLocation=getProperty(property, -1);
		if (dividerLocation != -1){
			split.setDividerLocation(dividerLocation);
		}
		final BasicSplitPaneDivider divider= ( (BasicSplitPaneUI) split.getUI()).getDivider();
		divider.addComponentListener(new ComponentAdapter(){
			public void componentMoved(ComponentEvent e){
				final int dividerLocation=split.getDividerLocation();
				dividerChanges++;
				if (dividerChanges > 1){
					setProperty(property, dividerLocation);
				}
			}
		});
		return divider;
	}

	public void setWindowFromProperties(
		 final Window wnd,
		 final String widthProperty,
		 final String heightProperty,
		 final String xProperty,
		 final String yProperty
		 ){
		boolean squeezedToFitScreen=false;
		final Dimension d=wnd.getSize();
		final int currentWidth= (int) d.getWidth();
		final int currentHeight= (int) d.getHeight();
		final int propertyDefinedWidth=getProperty(widthProperty, currentWidth);
		final int propertyDefinedHeight=getProperty(heightProperty, currentHeight);
		if (propertyDefinedHeight!=currentHeight||propertyDefinedWidth!=currentWidth){
			final Dimension screenSize=wnd.getToolkit().getScreenSize();
			if (propertyDefinedWidth>screenSize.width||propertyDefinedHeight>screenSize.height){
				squeezedToFitScreen=true;
				wnd.setSize(screenSize.width-15, screenSize.height-15);
				wnd.setLocation(0, 0);
			} else{
				wnd.setSize(propertyDefinedWidth, propertyDefinedHeight);
			}
		}
		if (!squeezedToFitScreen){
			final Point p=wnd.getLocation();
			final int currentX=p.x;
			final int currentY=p.y;
			final int propertyDefinedX=getProperty(xProperty, currentX);
			final int propertyDefinedY=getProperty(yProperty, currentY);
			if (propertyDefinedX!=currentX&&propertyDefinedY!=currentY){
				wnd.setLocation(propertyDefinedX, propertyDefinedY);
			}
		}
	}

	public void setPropertiesFromWindow(
			final Window wnd,
			final String widthProperty,
			final String heightProperty,
			final String xProperty,
			final String yProperty
		){
		final Point p=wnd.getLocation();
		setProperty(xProperty, p.x);
		setProperty(yProperty, p.y);
		final Dimension d=wnd.getSize();
		setProperty(widthProperty, d.width);
		setProperty(heightProperty, d.height);
	}

	public void setPropertiesFromWindow(final Window window){
		setPropertiesFromWindow(
			 window,
			 PROPERTY_WINDOW_WIDTH,
			 PROPERTY_WINDOW_HEIGHT,
			 PROPERTY_WINDOW_X,
			 PROPERTY_WINDOW_Y);
	}

	public void setWindow(final Window wnd){
		setWindowFromProperties(
				  wnd,
				  PROPERTY_WINDOW_WIDTH,
				  PROPERTY_WINDOW_HEIGHT,
				  PROPERTY_WINDOW_X,
				  PROPERTY_WINDOW_Y);
		wnd.addWindowListener(new WindowAdapter(){
			public void windowClosing(final WindowEvent e){
				setPropertiesFromWindow(
					wnd,
					PROPERTY_WINDOW_WIDTH,
					PROPERTY_WINDOW_HEIGHT,
					PROPERTY_WINDOW_X,
					PROPERTY_WINDOW_Y);
			}
		});
	}
	public void saveProperties(final String configFileName){
		PropertiesBasics.saveProperties(properties, configFileName, "");
	}

}