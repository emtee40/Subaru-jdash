/*******************************************************
 * 
 *  @author s_powell
 *  Setup.java
 *  Aug 2, 2007
 *  $ID:$
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
package net.sourceforge.JDash.waba;


import superwaba.ext.xplat.io.DataStream;
import superwaba.ext.xplat.io.ObjectCatalog;
import superwaba.ext.xplat.io.Storable;
import superwaba.ext.xplat.util.props.Properties;
import waba.io.Catalog;
import waba.util.Vector;

/*******************************************************
 * This singleton class loads and holds all the setup and config
 * settings for this dashboard.  When the dashboard main window is
 * started, it will init this class. This class will then
 * initialize itself, and install itself at the lone Setup singleton.
 ******************************************************/
public class Setup
{
	
	/** The application version string */
	public static final String VERSION = "v2.0.0b 08-02-2007";
	
	/** The applicaiton name */
	public static final String APPLICATION = "JDash / " + VERSION;

	
	/** the license string */
	public static final String LICENSE = "Protected under LGPL.  Copyright (C) 2006  Shane Powell";

	/** The preferred JDash catalog prefix */
	public static final String CATALOG_PREFIX = "jdash-";
	
	/** Use this creator ID */
	public static final String CATALOG_CREATOR = "JDSH";
	
	/** Use this for all data catalog types */
	public static final String CATALOG_DATA_TYPE = "DATA";
	
	/** PDB Suffix. It's simply a concatted string of . + CREATOR + . + TYPE */
	public static final String CATALOG_SUFFIX = "." + CATALOG_CREATOR + "." + CATALOG_DATA_TYPE;

	/** The name of the settings catalog */
	private static final String SETTINGS_CATALOG = CATALOG_PREFIX + "conf" + CATALOG_SUFFIX;
	
	
	/** The get() config key to return the ecu parameter file name */
	public static final String PROP_PARAMETER_FILE = "parameter.file";
	
	/** The get() config key to return the monitor serial port */
	public static final String PROP_MONITOR_PORT = "monitor.port";
	
	
	/** The one and only setup object */
	private static Setup setup_ = null;
	
	/** The properties object used by this setup class */
	private Properties props_ = new Properties();
	
	/*******************************************************
	 * ONLY gets created by the static initializer.
	 ******************************************************/
	private Setup()
	{
		
		/* Read the settings catalog */
		ObjectCatalog settings = new ObjectCatalog(SETTINGS_CATALOG, Catalog.READ_WRITE);
		
		/* read each key/value into the props_ object */
		DBProp p = new DBProp();
		while (settings.nextObject(p))
		{
			props_.put(p.key, new Properties.Str(p.value));
		}
		
		settings.close();
	}

	
	/********************************************************
	 * If it has not yet been done so, initialize a new 
	 * singleton copy of this class.
	 *******************************************************/
	protected static void init()
	{
		if (Setup.setup_ == null)
		{
			Setup.setup_ = new Setup();
		}
	}
	
	
	/*******************************************************
	 * Save the current state of the settings back to the PDB
	 *******************************************************/
	public void save()
	{
		/* Delete the catalog */
		ObjectCatalog settings = new ObjectCatalog(SETTINGS_CATALOG, Catalog.WRITE_ONLY);
		settings.delete();
		
		/* Re-create the catalog */
		settings = new ObjectCatalog(SETTINGS_CATALOG, Catalog.CREATE);
	
		Vector keys = this.props_.getKeys();
		for (int index = 0; index < keys.size(); index++)
		{
			/* Get the key/value pair  */
			DBProp p = new DBProp();
			p.key = (String)keys.items[index];
			p.value = ((Properties.Str)this.props_.get(p.key)).value;
			
			/* Write them to the catalog */
			System.out.println("index: " + index + " " + settings.addObject(p));
		}
		
		settings.close();
	}
	
	/********************************************************
	 * return the singleton instnace of the Setup object.  If
	 * it has not been initialized, you'll get a runtime exception
	 * @return
	 *******************************************************/
	public static Setup getInstance() throws RuntimeException
	{
		if (Setup.setup_ == null)
		{
			throw new RuntimeException("Setup is not initialized");
		}
		
		return Setup.setup_;
	}

	
	
	/*******************************************************
	 * @param key
	 * @return
	 *******************************************************/
	public String get(String key)
	{
		Properties.Value v = this.props_.get(key);
		
		if (v == null)
		{
			return null;
		}
		
		return ((Properties.Str)v).value;
	}
	
	
	/********************************************************
	 * @param key
	 * @param value
	 ******************************************************/
	public void set(String key, String value)
	{
		this.props_.put(key, new Properties.Str(value));
	}
	
	
	
	/*******************************************************
	 * Return the version string.
	 * @return the version string.
	 ******************************************************/
	public final String getVersion()
	{
		return VERSION;
	}
	
	
	/*******************************************************
	 * Return the license stirng.
	 * 
	 * @return the license string.
	 *******************************************************/
	public final String getLicense()
	{
		return LICENSE;
	}
	
	
	/********************************************************
	 * Static inline Class 
	 *******************************************************/
	/******************************************************
	 * A simple class implementing the Storable interface to
	 * read and write properties
	 ******************************************************/
	private static class DBProp implements Storable
	{
		public static final byte ID = 1;
		public String key = null;
		public String value = null;
		
		/*******************************************************
		 * Override
		 * @see superwaba.ext.xplat.io.Storable#getID()
		 *******************************************************/
		public byte getID()
		{
			return ID;
		}
		
		/*******************************************************
		 * Override
		 * @see superwaba.ext.xplat.io.Storable#getInstance()
		 *******************************************************/
		public Storable getInstance()
		{
			return new DBProp();
		}
		
		/*******************************************************
		 * Override
		 * @see superwaba.ext.xplat.io.Storable#loadState(superwaba.ext.xplat.io.DataStream)
		 *******************************************************/
		public void loadState(DataStream ds)
		{
			key = ds.readString();
			value = ds.readString();
		}
		
		/*******************************************************
		 * Override
		 * @see superwaba.ext.xplat.io.Storable#saveState(superwaba.ext.xplat.io.DataStream)
		 *******************************************************/
		public void saveState(DataStream ds)
		{
			ds.writeString(key);
			ds.writeString(value);
		}
	}
	
	
}
