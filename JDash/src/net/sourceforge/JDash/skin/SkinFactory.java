/*******************************************************
 * 
 *  @author spowell
 *  SkinFactory.java
 *  Aug 8, 2006
 *  $Id: SkinFactory.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.JDash.ecu.param.ParameterRegistry;


/*******************************************************
 * This is a static factory class that will create
 * the currently configured skinn class.  The skin
 * class is configurable by way of the system parameter
 * jdash.skin_name and jdash.skin_class.
 ******************************************************/
public abstract class SkinFactory
{
	
	public static final String SKIN_LIST_PROPS_FILE = "skinfactory-list.properties";
	public static final String SKIN_LIST_PREFIX = "skinfactory.";
	
	
	private ParameterRegistry parameterRegistry_ = null;

	
	private String defaultSkinId_ = null;

	/*******************************************************
	 * The constructor is private because this is NOT
	 * an instanceable class.
	 *****************************************************/
	public SkinFactory()
	{
	}
	
	
	/********************************************************
	 * Get the ID of the current default skin.
	 * @return
	 *******************************************************/
	public String getDefaultSkinId()
	{
		return this.defaultSkinId_;
	}
	
	/********************************************************
	 * Set the ID of the default skin to return.
	 * @param id
	 *******************************************************/
	public void setDefaultSkinId(String id)
	{
		this.defaultSkinId_ = id;
	}
	
	/*******************************************************
	 * @param registry
	 *******************************************************/
	public void setParameterRegistry(ParameterRegistry registry)
	{
		this.parameterRegistry_ = registry;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public ParameterRegistry getParameterRegistry()
	{
		return this.parameterRegistry_;
	}

	
	
	/********************************************************
	 * Get the current default skin
	 * @return
	 *******************************************************/
	public abstract Skin getDefaultSkin() throws Exception;
	
	
	/*******************************************************
	 * Return a list of all skins this factory contains.
	 * 
	 * @return
	 * @throws Exception
	 ******************************************************/
	public abstract List<Skin> getAllSkins() throws Exception;
	
	
	
	/********************************************************
	 * Gets a list of the available skin factories.
	 * @return
	 *******************************************************/
	public static List<SkinFactory> getAllFactories() throws Exception
	{
		ArrayList<SkinFactory> skinFactories = new ArrayList<SkinFactory>();
		
		int missedSkinIndexCount = 0;
		int skinIndex = 0;
		Properties skinClasses = new Properties();
		skinClasses.load(SkinFactory.class.getResource(SKIN_LIST_PROPS_FILE).openStream());
		
		/* we'll look until we get at least 5 missed skins. So..keep the file tight  */
		while(missedSkinIndexCount < 5)
		{
			String skinClassName = skinClasses.getProperty(SKIN_LIST_PREFIX + skinIndex);

			if ((skinClassName == null) || (skinClassName.length() == 0))
			{
				missedSkinIndexCount++;
			}
			else
			{
				skinFactories.add((SkinFactory)Class.forName(skinClassName).newInstance());
			}
			
			/* Next Skin */
			skinIndex++;
		}

		return skinFactories;
		
	}

}
