/*********************************************************
 * 
 * @author spowell
 * Const.java
 * Jul 23, 2008
 *
Copyright (C) 2008 Shane Powell

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
 *********************************************************/

package net.sourceforge.JDashLite.config;

import superwaba.ext.xplat.game.Options;
import superwaba.ext.xplat.util.props.Properties;

/*********************************************************
 * Just a simple interface to hold settings constants.s 
 *
 *********************************************************/

public class Preferences 
{
	
	public static final String KEY_GUI_STYLE			= "gui.style";
	public static final String KEY_COM_PORT 			= "com.port";
	public static final String KEY_AUTO_CONNET 			= "auto.connect";
	public static final String KEY_DISPLAYED_SENSORS 	= "displayed.sensors";
	public static final String KEY_ACTIVE_PROFILE		= "active.profile";
	public static final String KEY_TEST_MODE 			= "test.mode";
	public static final String KEY_ENABLE_ERROR_LOG		= "enable.errorlog";

	private Options appOptions_ = null;
	
	
	/********************************************************
	 * 
	 *******************************************************/
	public Preferences(Options appOptions)
	{
		this.appOptions_ = appOptions;
	}
	
	
	/********************************************************
	 * Save the options to the PDB file.
	 * returns true on success, false on fail.
	 ********************************************************/
	public boolean save()
	{
		return true;
//		return this.appOptions_.save();
	}
	
	
//	/*******************************************************
//	 * @return
//	 ********************************************************/
//	public Options getOptions()
//	{
//		return this.appOptions_;
//	}
	
	/******************************************************
	 * Gets the preferences Sring. 
	 * @param key
	 * @param defaultValue
	 * @return
	 ********************************************************/
	public String getString(String key, String defaultValue)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v instanceof Properties.Str)
		{
			return ((Properties.Str)v).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setString(String key, String value)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v == null)
		{
			v = this.appOptions_.declareString(key, value);
		}
		else
		{
			((Properties.Str)v).value = value;
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @return
	 ********************************************************/
	public int getInt(String key, int defaultValue)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v instanceof Properties.Int)
		{
			return ((Properties.Int)v).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setInt(String key, int value)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v == null)
		{
			v = this.appOptions_.declareInteger(key, value);
		}
		else
		{
			((Properties.Int)v).value = value;
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @return
	 ********************************************************/
	public long getLong(String key, long defaultValue)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v instanceof Properties.Long)
		{
			return ((Properties.Long)v).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setLong(String key, long value)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v == null)
		{
			v = this.appOptions_.declareLong(key, value);
		}
		else
		{
			((Properties.Long)v).value = value;
		}
	}
	

	
	
	/*******************************************************
	 * @param key
	 * @return
	 ********************************************************/
	public boolean getBoolean(String key, boolean defaultValue)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v instanceof Properties.Boolean)
		{
			return ((Properties.Boolean)v).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setBoolean(String key, boolean value)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v == null)
		{
			v = this.appOptions_.declareBoolean(key, value);
		}
		else
		{
			((Properties.Boolean)v).value = value;
		}
	}

	
	/*******************************************************
	 * @param key
	 * @return
	 ********************************************************/
	public double getDouble(String key, double defaultValue)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v instanceof Properties.Double)
		{
			return ((Properties.Double)v).value;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setDouble(String key, double value)
	{
		Properties.Value v = this.appOptions_.get(key);
		if (v == null)
		{
			v = this.appOptions_.declareDouble(key, value);
		}
		else
		{
			((Properties.Double)v).value = value;
		}
	}
	
	
}
