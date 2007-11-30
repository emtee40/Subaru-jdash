/*******************************************************
 * 
 *  @author spowell
 *  XMLSkin.java
 *  Aug 9, 2006
 *  $Id: XMLSkin.java,v 1.7 2006/12/31 16:59:10 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ******************************************************/
package net.sourceforge.JDash.skin.XMLSkin;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MediaTracker;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.AbstractGaugePanel;
import net.sourceforge.JDash.gui.AnalogGauge;
import net.sourceforge.JDash.gui.ButtonGauge;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.gui.DigitalGauge;
import net.sourceforge.JDash.gui.GaugeButton;
import net.sourceforge.JDash.gui.LEDGauge;
import net.sourceforge.JDash.gui.LineGraphGauge;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.gui.shapes.EllipseShape;
import net.sourceforge.JDash.gui.shapes.ImageShape;
import net.sourceforge.JDash.gui.shapes.LineShape;
import net.sourceforge.JDash.gui.shapes.PolygonShape;
import net.sourceforge.JDash.gui.shapes.RectangleShape;
import net.sourceforge.JDash.gui.shapes.RoundRectangleShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinEventListener;
import net.sourceforge.JDash.skin.SkinFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/*******************************************************
 * This extension to the Skin class is the primary
 * skin class used in JDash.  Skins are defined
 * by way of an xml config file. This skin reads
 * the XML file, and returns elements as defined within
 * this file.  This makes this skin easily extendable
 * by users with a simple paint program.  Just chaning
 * the xml file this skin reads will result in a 
 * changed look and feel.  This class looks for skin.xml
 * config files in a very specific place. The System property
 * skin.name is used to find the package of images, and the config
 * file.  The resource URL MUST be ".net.sourceforge.JDash.skin.(skin.name)".
 * Replacing the (skin.name) with the configured skin name.  This
 * class will then attempt to load the resource skin xml file
 * located at the class path l ocation "/net/sourceforge/JDash/skin/(skin.name)/skin.xml".
 * ALL of the resources used by this skin MUST be located in this same resource 
 * directory.  
 ******************************************************/
public class XMLSkin extends Skin
{
	
	/** This is the full resource path to the digital ttf file.  */
//	private static final String PATH_TO_DIGITAL_FONT = "/net/sourceforge/JDash/skin/XMLSkin/DefaultSkin/digital.ttf";
	
	private static final String GAUGE_TYPE_ANALOG 		= "analog";
	private static final String GAUGE_TYPE_DIGITAL 		= "digital";
	private static final String GAUGE_TYPE_DIGITAL_LOW	= "digital-low";
	private static final String GAUGE_TYPE_DIGITAL_HIGH	= "digital-high";
	private static final String GAUGE_TYPE_LED	 		= "led";
	private static final String GAUGE_TYPE_BUTTON		= "button";
	private static final String GAUGE_TYPE_LINE_GRAPH	= "line-graph";
	
	private static final String NODE_SKIN 		= "/skin";
	private static final String NODE_DESC		= "description";
	private static final String NODE_IMAGE 		= "image";
	private static final String NODE_COLOR		= "color";
	private static final String NODE_GAUGE 		= NODE_SKIN + "/gauge";
	private static final String NODE_WINDOW		= "window";
	private static final String NODE_NEEDLE		= "needle";
	private static final String NODE_LED		= "led";
	private static final String NODE_POINT		= "point";
	private static final String NODE_FONT		= "font";
	
	private static final String NODE_BUTTON				= "button";
	private static final String NODE_POLYGON			= "polygon";
	private static final String NODE_ELLIPSE			= "ellipse";
	private static final String NODE_LINE				= "line";
	private static final String NODE_RECTANGLE			= "rectangle";
	private static final String NODE_ROUND_RECTANGLE 	= "round-rectangle";
	private static final String NODE_TEXT				= "text";
	private static final String NODE_RANGE				= "range";
	private static final String NODE_TEXT_HIGH			= NODE_TEXT + "-high";
	private static final String NODE_TEXT_LOW			= NODE_TEXT + "-low";
	
	private static final String ATTRIB_NAME 		= "name";
	private static final String ATTRIB_EXTENDS		= "extends";
	private static final String ATTRIB_DELAY		= "delay";
//	private static final String ATTRIB_RESOURCE_URL	= "resource-url";
	private static final String ATTRIB_SRC 			= "src";
	private static final String ATTRIB_SENSOR 		= "sensor";
	private static final String ATTRIB_TYPE			= "type";
	private static final String ATTRIB_X			= "x";
	private static final String ATTRIB_Y			= "y";
	private static final String ATTRIB_WIDTH		= "width";
	private static final String ATTRIB_HEIGHT		= "height";
	private static final String ATTRIB_ARC_WIDTH	= "arcw";
	private static final String ATTRIB_ARC_HEIGHT	= "arch";
	private static final String ATTRIB_COLOR		= NODE_COLOR;
	private static final String ATTRIB_FILL_COLOR	= "fill-color";
	private static final String ATTRIB_LINE_WIDTH	= "line-width";
	private static final String ATTRIB_FORMAT		= "format";
	private static final String ATTRIB_FONT			= "font";
	private static final String ATTRIB_SIZE			= "size";
	private static final String ATTRIB_SENSOR_MIN	= "sensor-min";
	private static final String ATTRIB_SENSOR_MAX	= "sensor-max";
	private static final String ATTRIB_GAUGE_MIN	= "gauge-min";
	private static final String ATTRIB_GAUGE_MAX	= "gauge-max";
	private static final String ATTRIB_VALUE		= "value";
	private static final String ATTRIB_UP_IMAGE		= "up-image";
	private static final String ATTRIB_DOWN_IMAGE	= "down-image";
	private static final String ATTRIB_UP_ACTION	= "up-action";
	private static final String ATTRIB_DOWN_ACTION	= "down-action";
	private static final String ATTRIB_SECONDS      = "seconds";
	private static final String ATTRIB_LABEL		= "label";
	private static final String ATTRIB_REVERSE		= "reverse";
	
	private static final String VALUE_MAIN			= "main";
	private static final String VALUE_LOW			= "low";
	private static final String VALUE_HIGH			= "high";
	
	/** This is the fontfile name to use in a text component is you wish to use the built in digital font */
//	public static final String VALUE_JDASH_DIGITAL	= "jdash-digital";

	
	
	/** The url to the image files.  This is used to get the relative path to the images */
	private String resourceUrl_ = null;
	
	/** The loaded xml config file. */
	private Document xmlSkinDoc_ = null;

	/** The cache of fonts */
	private HashMap<String, Font> fontCache_ = new HashMap<String,Font>();

	/* The cache of images */
	private HashMap<String, ImageIcon> imageCache_ = new HashMap<String, ImageIcon>();
	
	/* the cache of all created gauges */
	private HashMap<Integer, AbstractGauge> gaugeCache_= new HashMap<Integer, AbstractGauge>();

	
	private String id_ = null;
	private String name_ = null;
	private String description_ = null;
	private Integer gaugeCount_ = null;
	private Dimension windowSize_ = null;
	
	/******************************************************
	 * Create a new xml skin class.
	 ******************************************************/
	public XMLSkin(SkinFactory ownerFactory, URL skinXmlFile) throws Exception
	{
		this(ownerFactory, skinXmlFile, null);
		
//		{
//    	/* Write the xml to the return string */
//        OutputStream os = new ByteArrayOutputStream();
//        OutputFormat of = new OutputFormat();
//        of.setOmitDocumentType(true);
//        of.setOmitXMLDeclaration(true);
//        of.setIndent(1);
//        of.setIndenting(true);
//        XMLSerializer serializer = new XMLSerializer(os,of);
//        serializer.asDOMSerializer();
//        serializer.serialize( this.xmlSkinDoc_.getDocumentElement() );
//        os.close();
//        System.out.println("------------ " + this.id_ + " -----------\n" + os.toString() + "----------- " + this.id_ + " ----------\n");
//        
//	}

		
	}
	
	
	/*******************************************************
	 * This private constructor is for our extends skin ability.
	 * Create a skin with this method, and it not only create an instance of itself, but
	 * it will add all child elements of the <skin> node to the docToAddTo parameter
	 * passed in.
	 * 
	 * @param ownerFactory IN - the owner factory.
	 * @param skinXmlFile IN - the skin file URL.
	 * @param docToAddTo IN - the xml doc, if any to add nodes to.
	 * @throws Exception
	 ******************************************************/
	private XMLSkin(SkinFactory ownerFactory, URL skinXmlFile, Document docToAddTo) throws Exception
	{
		super(ownerFactory);
		
		if (ownerFactory == null)
		{
			throw new Exception("Unable to create skin. No ownerFactory object provided.");
		}
		
		if (skinXmlFile == null)
		{
			throw new RuntimeException("Unable to create skin, No skin file provided." 
					+
					(docToAddTo==null?"":"This occured during an attempt to extend a skin"));
		}
		
		loadSkin(skinXmlFile);
		
		/* if the add to is not null, then add the child nodes */
		if (docToAddTo != null)
		{
			
			/* Get a list of all child nodes */
			XPath xp =   XPathFactory.newInstance().newXPath();
			NodeList childNodes = (NodeList)xp.evaluate(NODE_SKIN + "/*", this.xmlSkinDoc_, XPathConstants.NODESET);
			
			try
			{
				
				/* For each of our child nodes, append them to the docToAdd doc */
				for (int index = 0; index < childNodes.getLength(); index++)
				{
					Node n = childNodes.item(index);
					
					/* We only need to copy the element nodes under <skin> */
					if (n.getNodeType() == Node.ELEMENT_NODE)
					{
						docToAddTo.getDocumentElement().appendChild(docToAddTo.importNode(n, true));
					}
				}
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#createGaugePanel(net.sourceforge.JDash.gui.DashboardFrame, net.sourceforge.JDash.ecu.comm.BaseMonitor, net.sourceforge.JDash.logger.DataLogger)
	 *******************************************************/
	@Override
	public AbstractGaugePanel createGaugePanel(DashboardFrame dashFrame, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		return new XMLGaugePanel(dashFrame, this, monitor, logger);
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getId()
	 *******************************************************/
	@Override
	public String getId()
	{
		return id_;
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getName()
	 *******************************************************/
	@Override
	public String getName()
	{
		return this.name_;
	}
	
	
	/*****************************************************
	 * @return Returns the description.
	 *****************************************************/
	public String getDescription()
	{
		return this.description_;
	}

	
	/********************************************************
	 * Given the skin URL, load that xml skin into this skin class.
	 * @param skinUrl
	 * @throws Exception
	 *******************************************************/
	private void loadSkin(URL skinUrl) throws Exception
	{
		
		/* Load the xml skin file */
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.xmlSkinDoc_ = builder.parse(skinUrl.openStream());
		
		/* Set the name of this skin */
		this.name_ = extractString(NODE_SKIN + "/@" + ATTRIB_NAME);
		
		/* Setup the unique id */
		File skinFile = new File(skinUrl.getFile());
		this.id_ = skinFile.getName() + "-" + getName();

		/* Set the description */
		this.description_ = extractString(NODE_SKIN + "/" + NODE_DESC);

		
		/* Get the extension, if any */
		String extendedSkinName = null;
		try
		{
			extendedSkinName = extractString(NODE_SKIN + "/@" + ATTRIB_EXTENDS);
		}
		catch(Exception e) {}
		if (extendedSkinName != null)
		{
			File extSkinFile = new File(skinFile.getParent(), extendedSkinName);
			new XMLSkin(getOwnerFactory(), extSkinFile.toURL(), this.xmlSkinDoc_);
		}
		
		/* Set the resource url for this skin */
//		this.resourceUrl_ = extractString(NODE_SKIN + "/@" + ATTRIB_RESOURCE_URL);
		this.resourceUrl_ = skinFile.getParent();
		

	}
	
	
	/*******************************************************
	 * Extract a string at the given path.
	 * @param path
	 * @return the string.
	 * @throws Exception If there is no string at the requested path.
	 *******************************************************/
	private String extractString(String path) throws Exception
	{
		try
		{
			XPath xp =   XPathFactory.newInstance().newXPath();
			String value = xp.evaluate(path, this.xmlSkinDoc_);
			if ((value == null) || (value.length() == 0))
			{
				throw new Exception("String not Found");
			}
			
			return value;
		}
		catch(Exception e)
		{
			throw new Exception("Unable to extract String at path [" + path + "] " + this.id_, e);
		}

	}
	
	
	/******************************************************
	 * Extract an Integer at the given path.
	 * @param path
	 * @return
	 * @throws Exception if there is no Int at the given path.
	 *******************************************************/
	private Integer extractInt(String path) throws Exception
	{
		
		try
		{
			XPath xp =   XPathFactory.newInstance().newXPath();
			return Integer.parseInt(xp.evaluate(path, this.xmlSkinDoc_));
		}
		catch(Exception e)
		{
			throw new Exception("Unable to extract int at path [" + path + "] " + this.id_, e);
		}


	}
	
	
	/********************************************************
	 * Extract a double at the given path.
	 * @param path
	 * @return
	 * @throws Exception if there is no double at the given path.
	 *******************************************************/
	private Double extractDouble(String path) throws Exception
	{
		try
		{
			XPath xp =   XPathFactory.newInstance().newXPath();
			return Double.parseDouble(xp.evaluate(path, this.xmlSkinDoc_));
		}
		catch(Exception e)
		{
			throw new Exception("Unable to extract double at path [" + path + "] " + this.id_, e);
		}
	}
	

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowStartupState()
	 *******************************************************/
	@Override
	public String getWindowStartupState() throws Exception
	{
		return extractString(NODE_SKIN + "/" + NODE_WINDOW + "/@" + ATTRIB_TYPE);
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowSize()
	 *******************************************************/
	public Dimension getWindowSize() throws Exception
	{
		if (this.windowSize_ == null)
		{
			int w = extractInt(NODE_SKIN + "/" + NODE_WINDOW + "/@" + ATTRIB_WIDTH);
			int h = extractInt(NODE_SKIN + "/" + NODE_WINDOW + "/@" + ATTRIB_HEIGHT);
			this.windowSize_ = new Dimension(w,h);
		}
		
		return this.windowSize_;
	}
	
	/********************************************************
	 * the getImage() method is final to enforce caching of images.
	 * Once an image is loaded, it's cached into a hashmap, and returned
	 * from it for future calls.
	 * 
	 * @param imageName IN - the name of the image to be fetched.
	 * @return
	 *******************************************************/
	private ImageIcon getImage(String imageName)
	{
		
		try
		{
			
			/* Look first in the cache */
			if (this.imageCache_.containsKey(imageName) == true)
			{
				return this.imageCache_.get(imageName);
			}
			
			
			/* Load the image */
			URL imageUrl = getImageUrl(imageName);
			if (imageUrl == null)
			{
				throw new Exception("image [" + imageName + "] does not appear to exist");
			}
			ImageIcon image = new ImageIcon(getImageUrl(imageName));
			
			/* Wait for the image to load */
			while (image.getImageLoadStatus() == MediaTracker.LOADING);

			/* We gotta have something!! */
			if (image.getImageLoadStatus() != MediaTracker.COMPLETE)
			{
				throw new RuntimeException("Unable to load image: " + getImageUrl(imageName));
			}
			
			/* Cache the image and return it */
			this.imageCache_.put(imageName, image);
			return image;

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowShapes()
	 *******************************************************/
	public List<AbstractShape> getWindowShapes() throws Exception
	{
		ArrayList<AbstractShape> backgroundShapes = new ArrayList<AbstractShape>();
		
		int shapeCount = extractInt("count(" + NODE_SKIN + "/" + NODE_WINDOW + "/*" + ")");

		for (int shapeIndex = 1; shapeIndex <= shapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(NODE_SKIN + "/" + NODE_WINDOW + "/*[" + shapeIndex + "]");
			backgroundShapes.add(shape);
		}
		
		return backgroundShapes;
	}
	
	/*******************************************************
	 * get the defined fill color for this skin.
	 * 
	 * @return
	 *******************************************************/
	public java.awt.Color getBackgroundColor() throws Exception
	{
		return Color.decode(getColor(extractString(NODE_SKIN + "/" + NODE_WINDOW + "/@" + ATTRIB_FILL_COLOR)));
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getImageUrl(net.sourceforge.JDash.skin.Skin.IMAGE_RESOURCE)
	 *******************************************************/
	public URL getImageUrl(String imageName) throws Exception
	{
		String imageSource = extractString(NODE_SKIN + "/" + NODE_IMAGE + "[@" + ATTRIB_NAME + "='" + imageName + "']/@" + ATTRIB_SRC);
		imageSource = this.resourceUrl_ + "/" + imageSource;
		
		/* This image source could be a link to a file, or a link to an image inside a jar
		 * 	file.  this is goverened by what the resourceUrl_ value it.  So.. The easy way.
		 * Just try to load it as a file first.  If that fails, try it as a resource URL. 
		 * if that failes.. well.. we give up. */
		File imageFile = new File(imageSource);
		if (imageFile.exists() && imageFile.isFile())
		{
			return imageFile.toURL();
		}
		

		/* Now, try it as a resource file */
       URL url = this.getClass().getResource(this.resourceUrl_ + "\\" + imageSource);
       
       /* If the url is still null, we couldn't find the image */
       if (url == null)
       {
    	   throw new Exception("The image resource [" + this.resourceUrl_ + "/" + imageSource + "] does not seem to exist");
       }
       
       return url;
	}
	
	
	/*******************************************************
	 * A color value can be either an encoded #aabbcc rgb value, 
	 * or a color name.  If the colorName starts with a #, then
	 * it will just be returned right back.  If the colorName
	 * does NOT start with a #, then it is assumed to be a 
	 * color constant name, and the #d value will be returned
	 * from the color tag.
	 * 
	 * @param colorName
	 * @return
	 * @throws Exception
	 *******************************************************/
	private String getColor(String colorName) throws Exception
	{
		if (colorName.startsWith("#"))
		{
			return colorName;
		}
		
		String value = extractString(NODE_SKIN + "/" + NODE_COLOR + "[@" + ATTRIB_NAME + "='" + colorName + "']/@" + ATTRIB_VALUE );
		return value;
	}
	
	
	/********************************************************
	 * given the font resource name, find the font definition, and
	 * return an instance of this font.  When a font is loaded
	 * it's cached for future reuse.
	 * 
	 * @param fontName
	 * @return
	 * @throws Exception
	 *******************************************************/
	private Font getFont(String fontName) throws Exception
	{
		Font f = null;
		
		if (this.fontCache_.containsKey(fontName))
		{
			f = this.fontCache_.get(fontName);
		}
		else
		{
			
			String fontSource = extractString(NODE_SKIN + "/" + NODE_FONT + "[@" + ATTRIB_NAME + "='" + fontName + "']/@" + ATTRIB_SRC );
			f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File(this.resourceUrl_ + File.separatorChar + fontSource)));
		}
		
		if (f == null)
		{
			throw new Exception("Unable to load font: " + fontName + " the font for some reason is null");
		}
			
		return f;
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	public int getGaugeCount() throws Exception
	{
		if (this.gaugeCount_ == null)
		{
			this.gaugeCount_ = new Integer(extractInt("count(" + NODE_GAUGE + ")"));
		}
		
		return this.gaugeCount_.intValue();
	}
	
	/********************************************************
	 * get the guage at the given index. Throw an exception if
	 * a problem occurs.  do NOT return null.
	 * @param index IN - the index of which gauge to create.
	 * @param parentPanel IN - the parent panel this guage will belong to
	 * @return
	 *******************************************************/
	public AbstractGauge getGauge(int index) throws Exception
	{
		/* If the gauge has not yet been created, then create and cache it first */
		if (this.gaugeCache_.containsKey(new Integer(index)) == false)
		{
			this.gaugeCache_.put(new Integer(index), this.createGauge(index));
		}
			
		return this.gaugeCache_.get(new Integer(index));
		
	}
	
	/*******************************************************
	 * 
	 *******************************************************/
	private AbstractGauge createGauge(int index) throws Exception
	{
		/* XPath is 1 relative, so increment index */
		index++;
		
		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		AbstractGauge gauge = null;
		
		/* Get the gauge type */
		String type = extractString(gaugePath + "/@" + ATTRIB_TYPE);
		if (GAUGE_TYPE_ANALOG.equalsIgnoreCase(type))
		{
			gauge = createAnalogGauge(index);
		}
		else if (GAUGE_TYPE_DIGITAL.equalsIgnoreCase(type))
		{
			gauge = createDigitalGauge(index);
		}
		else if (GAUGE_TYPE_DIGITAL_HIGH.equalsIgnoreCase(type))
		{
			gauge = createDigitalHighGauge(index);
		}
		else if (GAUGE_TYPE_DIGITAL_LOW.equalsIgnoreCase(type))
		{
			gauge = createDigitalLowGauge(index);
		}
		else if (GAUGE_TYPE_LED.equalsIgnoreCase(type))
		{
			gauge = createLedGauge(index);
		}
		else if (GAUGE_TYPE_BUTTON.equalsIgnoreCase(type))
		{
			gauge = createButtonGauge(index);
		}
		else if (GAUGE_TYPE_LINE_GRAPH.equalsIgnoreCase(type))
		{
			gauge = createLineGraphGauge(index);
		}
		else
		{
			throw new Exception("Invalud Gauge Type of [" + type + "] at index: " + index);
		}
		
		
		/* If this gauge is a skin event listener, then we can go ahead and automatically
		 * add it to our listener list */
		if (gauge instanceof SkinEventListener)
		{
			addSkinEventListener((SkinEventListener)gauge);
		}
			
		
		return gauge;
	}
	
	/********************************************************
	 * Create an analog gague from the given gauge index
	 * @return
	 ******************************************************/
	private AbstractGauge createAnalogGauge(int index) throws Exception
	{

		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		/* Get the sensor type */
		String sensor = extractString(gaugePath + "/@" + ATTRIB_SENSOR);
		if ((sensor == null) || (sensor.length() == 0))
		{
			throw new Exception("Invalid Sensor of [" + sensor + "] at gauge index: " + index);
		}
		
		
		/* Get the parameter for this sensor */
		Parameter param = getOwnerFactory().getParameterRegistry().getParamForName(sensor);
		if (param == null)
		{
			throw new Exception("Parameter Registry could not find a parameter with the name: " + sensor);
		}
		
		
		/* Create the Analog Gauge */
		AnalogGauge analogGauge = new AnalogGauge(param);
		
		
		/* Get the needle pivot point values */
		int pivotX = extractInt(gaugePath + "/@" + ATTRIB_X);
		int pivotY = extractInt(gaugePath + "/@" + ATTRIB_Y);
		analogGauge.setPivot(new Point(pivotX, pivotY));

		/* There can be as many as 3 needles, each one of 3 types */
		
		/* Add the main needle */
		int mainNeedleShapeCount = extractInt("count(" + gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_MAIN + "']/*" + ")");
		for (int shapeIndex = 1; shapeIndex <= mainNeedleShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_MAIN + "']/*[" + shapeIndex + "]");
			analogGauge.addMainNeedleShape(shape);
		}
		

		/* Add the high needle */
		int highNeedleShapeCount = extractInt("count(" + gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/*" + ")");
		for (int shapeIndex = 1; shapeIndex <= highNeedleShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/*[" + shapeIndex + "]");
			analogGauge.addHighNeedleShape(shape);
		}
		
		/* Add the low needle */
		int lowNeedleShapeCount = extractInt("count(" + gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/*" + ")");
		for (int shapeIndex = 1; shapeIndex <= lowNeedleShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/*[" + shapeIndex + "]");
			analogGauge.addLowNeedleShape(shape);
		}

		
		/* Set the high / low needle delay */
		int lowNeedleDelay = 0;
		int highNeedleDelay = 0;
		if (lowNeedleShapeCount > 0)
		{
			lowNeedleDelay = extractInt(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/@" + ATTRIB_DELAY);
		}
		if (highNeedleShapeCount > 0)
		{
			highNeedleDelay = extractInt(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/@" + ATTRIB_DELAY);
		}
		analogGauge.setNeedleResetDelays(lowNeedleDelay, highNeedleDelay);
		
		/* Add the buttons */
		int buttonShapeCount = extractInt("count(" + gaugePath + "//" + NODE_BUTTON + ")");
		for (int shapeIndex = 1; shapeIndex <= buttonShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_BUTTON + "[" + shapeIndex + "]");
			GaugeButton gaugeButton = new GaugeButton(this, (ButtonShape)shape);
			analogGauge.addButton(gaugeButton);
		}
				
		/* Get the min degree */
		int needleDegreeMin = extractInt(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_GAUGE_MIN);
		analogGauge.setDegreeMin(needleDegreeMin);
		
		/* Get the max degree */
		int needleDegreeMax = extractInt(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_GAUGE_MAX);
		analogGauge.setDegreeMax(needleDegreeMax);
		
		/* Get the min value */
		int needleValueMin = extractInt(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_SENSOR_MIN);
		analogGauge.setValueMin(needleValueMin);
		
		/* Get the max value */
		int needleValueMax = extractInt(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_SENSOR_MAX);
		analogGauge.setValueMax(needleValueMax);
		
		/* Clockwise? It's optional */
		try
		{
			Boolean clockwise = new Boolean(extractString(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_REVERSE));
			analogGauge.setClockwise(!clockwise);
		}
		catch(Exception e) {}
		
		/* Return our new gauge */
		return analogGauge;
		
	}
	
	
	/********************************************************
	 * @param index
	 * @param parentPanel
	 * @return
	 * @throws Exception
	 *******************************************************/
	private AbstractGauge createDigitalLowGauge(int index) throws Exception
	{
		DigitalGauge gauge = (DigitalGauge)createDigitalGauge(index);
		gauge.enableLowHighHold(false);
		return gauge;
	}
	
	
	/********************************************************
	 * @param index
	 * @param parentPanel
	 * @return
	 * @throws Exception
	 *******************************************************/
	private AbstractGauge createDigitalHighGauge(int index) throws Exception
	{
		DigitalGauge gauge = (DigitalGauge)createDigitalGauge(index);
		gauge.enableLowHighHold(true);
		return gauge;
	}
	
	
	/********************************************************
	 * Create a digital gauge.
	 * @param index
	 * @return
	 *******************************************************/
	private AbstractGauge createDigitalGauge(int index) throws Exception
	{
		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		/* Get the sensor type */
		String sensor = extractString(gaugePath + "/@" + ATTRIB_SENSOR);
		
		
		/* Get the parameter for this sensor */
		Parameter param = getOwnerFactory().
							getParameterRegistry().
									getParamForName(sensor);
		if (param == null)
		{
			throw new Exception("Parameter Registry could not find a parameter with the name: " + sensor);
		}
		
		
		/* Get Text Shape */
		TextShape textShape = createTextShape(gaugePath + "/" + NODE_TEXT);

		/* Create the gague */
		DigitalGauge digitalGauge = new DigitalGauge(param, textShape);
		
		/* Return it */
		return digitalGauge;
	}
	
	/********************************************************
	 * Create an LED gauge.
	 * @param index
	 * @return
	 *******************************************************/
	private AbstractGauge createLedGauge(int index) throws Exception
	{
		
		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		/* Get the sensor type */
		String sensor = extractString(gaugePath + "/@" + ATTRIB_SENSOR);
		
		/* Get the parameter for this sensor */
		Parameter param = getOwnerFactory().getParameterRegistry().getParamForName(sensor);
		if (param == null)
		{
			throw new Exception("Parameter Registry could not find a parameter with the name: " + sensor);
		}
		
		/* Create the Analog Gauge */
		LEDGauge ledGauge = new LEDGauge(param);
		
		/* An LED Gauge is made up of n LED comonents.  Each component is made up */
		int ledCount = extractInt("count(" + gaugePath + "/" + NODE_LED + "[*])");
		for (int ledIndex = 1; ledIndex <= ledCount; ledIndex++)
		{
			/* Add this LED configuration to the LED Gauge */
			double min = extractDouble(gaugePath + "/" + NODE_LED + "[" + ledIndex + "]/@" + ATTRIB_SENSOR_MIN);
			double max = extractDouble(gaugePath + "/" + NODE_LED + "[" + ledIndex + "]/@" + ATTRIB_SENSOR_MAX);
			LEDGauge.LED led = new LEDGauge.LED(min, max);
			
			/* Each LED is made up of 1 - n shapes */
			int shapeCount = extractInt("count(" + gaugePath + "/" + NODE_LED + "[" + ledIndex + "]/*)");
			for (int shapeIndex = 1; shapeIndex <= shapeCount; shapeIndex++)
			{
				AbstractShape shape = createShape(gaugePath + "/" + NODE_LED + "[" + ledIndex + "]/*[" + shapeIndex + "]");
				led.addShape(shape);

			}
			
			/* Add this LED */
			ledGauge.addLed(led);
			
		}
		
		/* An LED Gauge migh also have a high and low indicator defined */
		
		/* Low Indicator Shapes */
		int lowShapeCount = extractInt("count(" + gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/*)");
		for (int shapeIndex = 1; shapeIndex <= lowShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/*[" + shapeIndex + "]");
			ledGauge.addLowNeedleShape(shape);
		}
		
		
		/* High Indicator Shapes */
		int highShapeCount = extractInt("count(" + gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/*)");
		for (int shapeIndex = 1; shapeIndex <= highShapeCount; shapeIndex++)
		{
			AbstractShape shape = createShape(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/*[" + shapeIndex + "]");
			ledGauge.addHighNeedleShape(shape);
		}
		
		
		/* Set the high / low needle delay */
		int lowNeedleDelay = 0;
		int highNeedleDelay = 0;
		if (lowShapeCount > 0)
		{
			lowNeedleDelay = extractInt(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_LOW + "']/@" + ATTRIB_DELAY);
		}
		if (highShapeCount > 0)
		{
			highNeedleDelay = extractInt(gaugePath + "/" + NODE_NEEDLE + "[@" + ATTRIB_TYPE + "='" + VALUE_HIGH + "']/@" + ATTRIB_DELAY);
		}
		ledGauge.setNeedleResetDelays(lowNeedleDelay, highNeedleDelay);
		
		/* Return our new gauge */
		return ledGauge;

	}

	
	/********************************************************
	 * Create an analog gague from the given gauge index
	 * @return
	 ******************************************************/
	private AbstractGauge createButtonGauge(int index) throws Exception
	{

		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		
		/* Get the parameter for this sensor.  It could be null, indicating that this is not a sensor triggered button. */
		Parameter param = null;
		try
		{
			/* Get the sensor type */
			String sensor = extractString(gaugePath + "/@" + ATTRIB_SENSOR);
			param = getOwnerFactory().getParameterRegistry().getParamForName(sensor);
		}
		catch(Exception e) {}
				
		/* Add the button */
		AbstractShape shape = createShape(gaugePath + "/" + NODE_BUTTON + "[1]");
	// TODO
		GaugeButton gaugeButton = new GaugeButton(this, (ButtonShape)shape);
		ButtonGauge buttonGauge = new ButtonGauge(param, gaugeButton);
		//ButtonGauge buttonGauge = new ButtonGauge(param, (ButtonShape)shape);
		

		/* Get the range values.  This is only needed if the parameter has been defined. */
		if (param != null)
		{
			double sensorMin = extractDouble(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_SENSOR_MIN);
			double sensorMax = extractDouble(gaugePath + "/" + NODE_RANGE + "/@" + ATTRIB_SENSOR_MAX);
			buttonGauge.setSensorMin(sensorMin);
			buttonGauge.setSensorMax(sensorMax);
		}
		
		
		/* Return our new gauge */
		return buttonGauge;
		
	}
	

	
	/********************************************************
	 * Create a new line graph gauge
	 * @return
	 ******************************************************/
	private AbstractGauge createLineGraphGauge(int index) throws Exception
	{

		/* Get the gauge node */
		String gaugePath = NODE_GAUGE + "[" + index + "]";
		
		/* Get the sensor type */
		String sensor = extractString(gaugePath + "/@" + ATTRIB_SENSOR);
		
		/* Get the parameter for this sensor */
		Parameter param = getOwnerFactory().getParameterRegistry().getParamForName(sensor);
		if (param == null)
		{
			throw new Exception("Parameter Registry could not find a parameter with the name: " + sensor);
		}
		

		/* Get the values */
		double x = extractDouble(gaugePath + "/@" + ATTRIB_X);
		double y = extractDouble(gaugePath + "/@" + ATTRIB_Y);
		double width = extractDouble(gaugePath + "/@" + ATTRIB_WIDTH);
		double height = extractDouble(gaugePath + "/@" + ATTRIB_HEIGHT);
		double seconds = extractDouble(gaugePath + "/@" + ATTRIB_SECONDS);
		double min = extractDouble(gaugePath + "/@" + ATTRIB_SENSOR_MIN);
		double max = extractDouble(gaugePath + "/@" + ATTRIB_SENSOR_MAX);
		String format = extractString(gaugePath + "/@" + ATTRIB_FORMAT);
		String label = extractString(gaugePath + "/@" + ATTRIB_LABEL);

		
		TextShape valueText = null;
		TextShape lowText = null;
		TextShape highText = null;
		
		/* value Text */
		if (extractInt("count(" + gaugePath + "/" + NODE_TEXT + ")") > 0)
		{
			valueText = createTextShape(gaugePath + "/" + NODE_TEXT);
		}
		
		
		/* High Text */
		if (extractInt("count(" + gaugePath + "/" + NODE_TEXT_HIGH + ")") > 0)
		{
			highText = createTextShape(gaugePath + "/" + NODE_TEXT_HIGH);
		}
		
		
		/* Low Text */
		if (extractInt("count(" + gaugePath + "/" + NODE_TEXT_LOW + ")") > 0)
		{
			lowText = createTextShape(gaugePath + "/" + NODE_TEXT_LOW);
		}
		
		
		/* Create and return the line graph */
		LineGraphGauge lineGraphGauge = new LineGraphGauge(param, x, y, width, height, seconds, min, max, format, label, valueText, lowText, highText);
		return lineGraphGauge;
		
	}
	
	
	/********************************************************
	 * create a shape from the given shape path.
	 * @param shapePath
	 * @return
	 *******************************************************/
	private AbstractShape createShape(String shapePath) throws Exception
	{
		/* get the name of this shape */
		String shapeType = extractString("name(" + shapePath + ")");
		
		/* Given the shape type, create the property shape */
		if (NODE_POLYGON.equals(shapeType) == true)
		{
			return createPolygonShape(shapePath);
		}
		else if (NODE_ELLIPSE.equals(shapeType) == true)
		{
			return createElipseShape(shapePath);
		}
		else if (NODE_LINE.equals(shapeType) == true)
		{
			return createLineShape(shapePath);
		}
		else if (NODE_RECTANGLE.equals(shapeType) == true)
		{
			return createRectangleShape(shapePath);
		}
		else if (NODE_ROUND_RECTANGLE.equals(shapeType) == true)
		{
			return createRoundRectangleShape(shapePath);
		}
		else if (NODE_TEXT.equals(shapeType) == true)
		{
			return createTextShape(shapePath);
		}
		else if (NODE_IMAGE.equals(shapeType) == true)
		{
			return createImageShape(shapePath);
		}
		else if (NODE_BUTTON.equals(shapeType) == true)
		{
			return createButtonShape(shapePath);
		}
		else
		{
			throw new Exception("Unable to create shape at path: [" + shapePath + "] Unknown shape type");
		}
		
	}
	
	
	/*******************************************************
	 * Since most shapes have a standard set of attribures, like
	 * color and fill.  This method will read and set them.
	 * It will also check the value for some of them
	 * to ensure they are correct.
	 * 
	 * @param shapePath
	 * @param shape
	 *******************************************************/
	private void addShapeAttributes(String shapePath, AbstractShape shape) throws Exception
	{
		
		/* Decode the color */
		String color = getColor(extractString(shapePath + "/@" + ATTRIB_COLOR));
		
		/* Decode the fill color */
		String fillColor = extractString(shapePath + "/@" + ATTRIB_FILL_COLOR);
		if (fillColor.equals(AbstractShape.KEYWORD_NONE) == false)
		{
			fillColor = getColor(fillColor);
		}
		
		/* Check the color value */
		if ((color.startsWith("#") == false) || (color.length() != 7))
		{
			throw new Exception("The color value at [" + shapePath + "] is not a valid HTML Color Value.  It must be [#aabbcc], but is [" + color + "]");
		}
		
		/* Check the fill color value */
		if ((fillColor.equals(AbstractShape.KEYWORD_NONE) == false) && 
			((fillColor.startsWith("#") == false) || (fillColor.length() != 7)))
		{
			throw new Exception("The fill color value at [" + shapePath + "] is not a valid HTML Color Value.  It must be [#aabbcc], but is [" + fillColor + "]");
		}

			
		/* Add the attributes */
		shape.addAttribute(AbstractShape.PROPS.COLOR, color);
		shape.addAttribute(AbstractShape.PROPS.FILL_COLOR, fillColor);
		shape.addAttribute(AbstractShape.PROPS.LINE_WIDTH, extractInt(shapePath + "/@" + ATTRIB_LINE_WIDTH).toString());
		

	}
	
	/********************************************************
	 * @param shapePath
	 * @return
	 *******************************************************/
	private PolygonShape createPolygonShape(String shapePath) throws Exception
	{
		PolygonShape newPolygon = new PolygonShape();
		
		/* count the points */
		int pointCount = extractInt("count(" + shapePath + "/*)");
		
		/* Add each point */
		for (int pointIndex = 1; pointIndex <= pointCount; pointIndex++)
		{
			int x = extractInt(shapePath + "/" + NODE_POINT + "[" + pointIndex + "]/@" + ATTRIB_X);
			int y = extractInt(shapePath + "/" + NODE_POINT + "[" + pointIndex + "]/@" + ATTRIB_Y);
			newPolygon.addPoint(x, y);
		}

		/* Add the standard attribues */
		addShapeAttributes(shapePath, newPolygon);

		/* Return the polygon */
		return newPolygon;
	}
	
	/********************************************************
	 * @param shapePath
	 * @return
	 * @throws Exception
	 *******************************************************/
	private EllipseShape createElipseShape(String shapePath) throws Exception
	{
		
		
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		double w = extractDouble(shapePath + "/@" + ATTRIB_WIDTH);
		double h = extractDouble(shapePath + "/@" + ATTRIB_HEIGHT);
		
		EllipseShape ellipse = new EllipseShape(x, y, w, h); 
		
		/* Add the standard attribues */
		addShapeAttributes(shapePath, ellipse);
	
		return ellipse;
	}

	/*******************************************************
	 * @param shapePath
	 * @return
	 * @throws Exception
	 ******************************************************/
	private LineShape createLineShape(String shapePath) throws Exception
	{
		
		
		/* Add each point. There should be 2, and only 2 */
		int x1 = extractInt(shapePath + "/" + NODE_POINT + "[1]/@" + ATTRIB_X);
		int y1 = extractInt(shapePath + "/" + NODE_POINT + "[1]/@" + ATTRIB_Y);
		int x2 = extractInt(shapePath + "/" + NODE_POINT + "[2]/@" + ATTRIB_X);
		int y2 = extractInt(shapePath + "/" + NODE_POINT + "[2]/@" + ATTRIB_Y);

		/* Create the line */
		LineShape lineShape = new LineShape(x1,y1, x2, y2);
		
		/* Add the standard attribues */
		addShapeAttributes(shapePath, lineShape);

		/* Return the polygon */
		return lineShape;
	}
	
	
	/*******************************************************
	 * @param shapePath
	 * @return
	 * @throws Exception
	 ******************************************************/
	private RectangleShape createRectangleShape(String shapePath) throws Exception
	{
		
		
		/* Add each point. There should be 2, and only 2 */
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		double w = extractDouble(shapePath + "/@" + ATTRIB_WIDTH);
		double h = extractDouble(shapePath + "/@" + ATTRIB_HEIGHT);

		/* Create the line */
		RectangleShape rectShape = new RectangleShape(x,y, w, h);
		
		/* Add the standard attribues */
		addShapeAttributes(shapePath, rectShape);

		/* Return the polygon */
		return rectShape;
	}

	
	/*******************************************************
	 * @param shapePath
	 * @return
	 * @throws Exception
	 ******************************************************/
	private RoundRectangleShape createRoundRectangleShape(String shapePath) throws Exception
	{
		
		
		/* Add each point. There should be 2, and only 2 */
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		double w = extractDouble(shapePath + "/@" + ATTRIB_WIDTH);
		double h = extractDouble(shapePath + "/@" + ATTRIB_HEIGHT);
		double aw = extractDouble(shapePath + "/@" + ATTRIB_ARC_WIDTH);
		double ah = extractDouble(shapePath + "/@" + ATTRIB_ARC_HEIGHT);

		/* Create the line */
		RoundRectangleShape rectShape = new RoundRectangleShape(x,y, w, h, aw, ah);
		
		/* Add the standard attribues */
		addShapeAttributes(shapePath, rectShape);

		/* Return the polygon */
		return rectShape;
	}

	
	/********************************************************
	 * @param shapePath
	 * @return
	 * @throws Exception
	 ******************************************************/
	private TextShape createTextShape(String shapePath) throws Exception
	{
		
		/* Add each point. There should be 2, and only 2 */
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		String format = extractString(shapePath + "/@" + ATTRIB_FORMAT);
		String fontName = extractString(shapePath + "/@" + ATTRIB_FONT);
		Font font = null;
		
		if (fontName == null)
		{
			throw new Exception("text shape must have a font attribute");
		}
		
		/* Get the font */
		font = getFont(fontName);
		
//		/* If the font named is our digital meta fond, then we'll setup the path and load it manualy */
//		if (fontFile.equals(VALUE_JDASH_DIGITAL))
//		{
//			font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(PATH_TO_DIGITAL_FONT));
//		}
//		else
//		{
//			
//			/* Load the font */
//			if (fontFile.endsWith(".ttf"))
//			{
//				font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(this.resourceUrl_ + "/" + fontFile));
//			}
//			else
//			{
//				font = new Font(fontFile, Font.BOLD, 1);
//			}
//		}
			
		TextShape textShape = new TextShape(x, y, format, font);
		
		/* Add the attributes */
		textShape.addAttribute(AbstractShape.PROPS.COLOR, getColor(extractString(shapePath + "/@" + ATTRIB_COLOR)));
		textShape.addAttribute(AbstractShape.PROPS.FILL_COLOR, getColor(extractString(shapePath + "/@" + ATTRIB_COLOR)));
		textShape.addAttribute(AbstractShape.PROPS.SIZE, extractInt(shapePath + "/@" + ATTRIB_SIZE).toString());
		textShape.addAttribute(AbstractShape.PROPS.LINE_WIDTH, "1");
		
		
		/* Return the polygon */
		return textShape;

	}
	
	
	
	/********************************************************
	 * Create an image shape from the given xml path.
	 * @param shapePath
	 * @return
	 * @throws Exception
	 *******************************************************/
	private ImageShape createImageShape(String shapePath) throws Exception
	{
		/* Add each point. There should be 2, and only 2 */
		String name = extractString(shapePath + "/@" + ATTRIB_NAME);
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		double w = extractDouble(shapePath + "/@" + ATTRIB_WIDTH);
		double h = extractDouble(shapePath + "/@" + ATTRIB_HEIGHT);

		/* Create the shape */
		ImageShape imageShape = new ImageShape(getImage(name), x, y, w, h);
		

		/* Return the shape */
		return imageShape;
	}
	
	
	/*******************************************************
	 * create a button shape from the xml path.
	 * @param shapePath
	 * @return
	 * @throws Exception
	 *******************************************************/
	private ButtonShape createButtonShape(String shapePath) throws Exception
	{
		String type = extractString(shapePath + "/@" + ATTRIB_TYPE);
		double x = extractDouble(shapePath + "/@" + ATTRIB_X);
		double y = extractDouble(shapePath + "/@" + ATTRIB_Y);
		double w = extractDouble(shapePath + "/@" + ATTRIB_WIDTH);
		double h = extractDouble(shapePath + "/@" + ATTRIB_HEIGHT);
		
		String upAction = null;
		String downAction = null;
		
		try {upAction = extractString(shapePath + "/@" + ATTRIB_UP_ACTION);} catch(Exception e) {}
		try {downAction = extractString(shapePath + "/@" + ATTRIB_DOWN_ACTION);} catch(Exception e) {}
		
		
		String upImageName = extractString(shapePath + "/@" + ATTRIB_UP_IMAGE);
		String downImageName = extractString(shapePath + "/@" + ATTRIB_DOWN_IMAGE);
		
		/* Create the shape */
		ButtonShape buttonShape = new ButtonShape(type, x, y, w, h, upAction, downAction, getImage(upImageName), getImage(downImageName));

		/* Return the shape */
		return buttonShape;
	}
	
}


