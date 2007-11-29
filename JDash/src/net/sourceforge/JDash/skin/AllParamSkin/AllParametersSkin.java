/*******************************************************
 * 
 *  @author spowell
 *  XMLSkin.java
 *  Aug 9, 2006
 *  $Id: AllParametersSkin.java,v 1.5 2006/12/31 16:59:10 shaneapowell Exp $
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
package net.sourceforge.JDash.skin.AllParamSkin;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.StringParameter;
import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.ButtonGauge;
import net.sourceforge.JDash.gui.DigitalGauge;
import net.sourceforge.JDash.gui.GaugeButton;
import net.sourceforge.JDash.gui.GaugePanel;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinFactory;

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
public class AllParametersSkin extends Skin
{
	
	private static final int IMAGE_HEIGHT = 15;
	private static final int IMAGE_WIDTH = 38;
	private static final String LOGGER_IMAGE_UP = "logger-up.jpg";
	private static final String LOGGER_IMAGE_DOWN = "logger-down.jpg";
	
	private static final int MAX_FONT_SIZE = 14;

	
	/* The columns */
	private static int COLUMN_COUNT = 3;
	                 
	/*  This array MUST have the same number of (or more) elements than the number of columns.  Each column will
	 * be set to it's matching color by this array
	 *                                         blue        green     yellow      red       cyan*/
	private static final String[] COLORS = {"#00CCFF", "#00FF33", "#FFFF00", "#ff0033", "#66FFFF"};
	
	/* Standard 4:3 ratio monitor */
	private static int STARTUP_SIZE = 40;
	private static int PANEL_WIDTH = 100 * 6;
	private static int PANEL_HEIGHT = 100 * 3;

	
	private String name_ = "All Parameters";
	private String description_ = "This Skin will put ALL of the parameters on the screen in a grid format. Warning, this skin can be quite" +
			" CPU hungry, and can respond very slow on some interface monitors like the ELM monitor.  Simply because it must render a large " +
			"number of parameters as fast as possible.  If you locate the RATE value, it will" +
			" tell you the number of updates per second that are occuring. The TIME parameter indicates the Total run time since starting the monitor.";

	private ArrayList<AbstractGauge> allGauges_ = null;
	
	/******************************************************
	 * Create a new xml skin class.
	 ******************************************************/
	public AllParametersSkin(SkinFactory ownerFactory) throws Exception
	{
		super(ownerFactory);
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getId()
	 *******************************************************/
	@Override
	public String getId()
	{
		return this.getClass().getName();
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

	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowStartupState()
	 *******************************************************/
	@Override
	public String getWindowStartupState() throws Exception
	{
		return Skin.STATE_WINDOW + ":" + STARTUP_SIZE;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowSize()
	 *******************************************************/
	public Dimension getWindowSize() throws Exception
	{
		return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowShapes()
	 *******************************************************/
	public List<AbstractShape> getWindowShapes() throws Exception
	{
		/* Return an empty list of shapes */
		return new ArrayList<AbstractShape>();
		
	}
	
	/*******************************************************
	 * get the defined fill color for this skin.
	 * 
	 * @return
	 *******************************************************/
	public java.awt.Color getBackgroundColor() throws Exception
	{
		return Color.BLACK;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getImageUrl(net.sourceforge.JDash.skin.Skin.IMAGE_RESOURCE)
	 *******************************************************/
	public URL getImageUrl(String imageName) throws Exception
	{
       return this.getClass().getResource(imageName);
	}
	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getGaugeCount()
	 *******************************************************/
	@Override
	public int getGaugeCount() throws Exception
	{
		return getOwnerFactory().getParameterRegistry().getAll().size();
		
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getGauge(int)
	 *******************************************************/
	@Override
	public AbstractGauge createGauge(int index) throws Exception
	{
		if (this.allGauges_ == null)
		{
			setupGauges();
		}
		
		return this.allGauges_.get(index);
	}


	/********************************************************
	 * @param parentPanel
	 *******************************************************/
	private void setupGauges() throws Exception
	{

		this.allGauges_ = new ArrayList<AbstractGauge>();

		/* Get all the parameters */
		ArrayList<Parameter> parameterList = new ArrayList<Parameter>(getOwnerFactory().getParameterRegistry().getAll().values());
		Collections.sort(parameterList, new ParameterComparator());
		
		
		/* Calculate the font size */
		int fontSize = Math.min(MAX_FONT_SIZE, PANEL_HEIGHT / Math.max((parameterList.size() / COLUMN_COUNT), 1) - 2);
		
		
		/* Add a parameter to each column, left to right until there are no more parameters */
		int rowIndex = 0;
		int columnIndex = 1; /* Start at column 1 so we can put the log button into cell 0,0 */
		
		for (int parameterIndex = 0; parameterIndex < parameterList.size(); parameterIndex++)
		{
			/* Setup the gauges x and y */
			int x = 1 + (columnIndex * (PANEL_WIDTH / COLUMN_COUNT));
			int y = -2 + fontSize + (rowIndex * ((PANEL_HEIGHT - fontSize) / Math.max((parameterList.size() / COLUMN_COUNT), 1)));
			
			Parameter p = parameterList.get(parameterIndex);
			
			TextShape textShape = null;
			if (p instanceof StringParameter)
			{
				textShape = new TextShape(x, y , p.getName() + ": [%s]", Font.decode("Arial"));
			}
			else
			{
				textShape = new TextShape(x, y , p.getName() + ": [%03.02f]", Font.decode("Arial"));
			}
			textShape.addAttribute(AbstractShape.PROPS.COLOR, COLORS[columnIndex]);
			textShape.addAttribute(AbstractShape.PROPS.FILL_COLOR, COLORS[columnIndex]);
			textShape.addAttribute(AbstractShape.PROPS.SIZE, "" + fontSize);
		
			DigitalGauge digitalGauge = new DigitalGauge(p, textShape);
			
			this.allGauges_.add(digitalGauge);

			/* Increment the column index */
			columnIndex++;
			
			/* Wrap to a new row if this is past the last column */
			if (columnIndex >= COLUMN_COUNT)
			{
				columnIndex = 0;
				rowIndex++;
			}
			
		}
		
		
		// TODO
//		/* And, finally, add the logger button */
//		ButtonShape buttonShape = new ButtonShape(GaugeButton.BUTTON_TYPE_TOGGLE, 
//													2, 
//													2, 
//													IMAGE_WIDTH, 
//													Math.min(IMAGE_HEIGHT, fontSize), 
//													GaugeButton.BUTTON_ACTION_LOGGER_TOGGLE, 
//													LOGGER_IMAGE_UP, 
//													LOGGER_IMAGE_DOWN);
//		GaugeButton gaugeButton = new GaugeButton(this, buttonShape);
//		ButtonGauge compGauge = new ButtonGauge(null, gaugeButton);
//		this.allGauges_.add(compGauge);

	}
	
	
	/*******************************************************
	 * The parameter comparator
	 *******************************************************/
	private static class ParameterComparator implements Comparator<Parameter>
	{
		/*******************************************************
		 * Override
		 * @see java.util.Comparator#compare(T, T)
		 *******************************************************/
		public int compare(Parameter p0, Parameter p1)
		{
			
			String name0 = p0.getName();
			String name1 = p1.getName();
			
			return name0.compareToIgnoreCase(name1);
			
		}
	}
}


