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
package net.sourceforge.JDash.skin.TableSkin;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterComparator;
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
public class TableSkin extends Skin
{
	
	/* Standard 4:3 ratio monitor */
	private static int STARTUP_SIZE = 40;
	private static int PANEL_WIDTH = 100 * 3;
	private static int PANEL_HEIGHT = 100 * 6;

	
	private String name_ = "Table of Parameters";
	private String description_ = "This Skin will put ALL of the parameters on the screen in simple Table Format." +
			" You will be able to enable and disable the parameters you want updated.";

	
	private TableGauge theTableGauge_ = null;
	
	/******************************************************
	 * Create a new xml skin class.
	 ******************************************************/
	public TableSkin(SkinFactory ownerFactory) throws Exception
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
		return Color.LIGHT_GRAY;
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
		//return getOwnerFactory().getParameterRegistry().getAll().size();
		return 1;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getGauge(int)
	 *******************************************************/
	@Override
	public AbstractGauge createGauge(int index) throws Exception
	{
		if (this.theTableGauge_ == null)
		{
			setupTableGauge();
		}
		
		return this.theTableGauge_;
	}

	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getReferencedParameters()
	 *******************************************************/
	@Override
	public List<Parameter> getReferencedParameters() throws Exception
	{
		return new ArrayList<Parameter>(getOwnerFactory().getParameterRegistry().getAll().values());
	}
	
	/*******************************************************
	 * @throws Exception
	 *******************************************************/
	private void setupTableGauge() throws Exception
	{
		ArrayList<Parameter> parameterList = new ArrayList<Parameter>(getOwnerFactory().getParameterRegistry().getAll().values());
		Collections.sort(parameterList, new ParameterComparator());
		this.theTableGauge_ = new TableGauge(parameterList);
	}

	
	
}


