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


import java.awt.Dimension;
import java.io.InputStream;

import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.gui.AbstractGaugePanel;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.logger.DataLogger;
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
	
	private static int PANEL_WIDTH = 600;
	private static int PANEL_HEIGHT = 800;

	
	private String name_ = "Table of Parameters";
	private String description_ = "This Skin will put ALL of the parameters on the screen in simple Table Format." +
			" You will be able to enable and disable the parameters you want updated.";

	
	/******************************************************
	 * Create a new xml skin class.
	 ******************************************************/
	public TableSkin(SkinFactory ownerFactory) throws Exception
	{
		super(ownerFactory);
	}
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#createGaugePanel(net.sourceforge.JDash.gui.DashboardFrame, net.sourceforge.JDash.ecu.comm.BaseMonitor, net.sourceforge.JDash.logger.DataLogger)
	 *******************************************************/
	public AbstractGaugePanel createGaugePanel(DashboardFrame dashFrame, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		TableGaugePanel gp = new TableGaugePanel(dashFrame, this, monitor, logger);
		return gp;
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
		return Skin.STATE_WINDOW;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getWindowSize()
	 *******************************************************/
	@Override
	public Dimension getWindowSize() throws Exception
	{
		return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
	}
	
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.Skin#getSound(java.lang.String)
	 *******************************************************/
	public InputStream getSound(String name) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
}


