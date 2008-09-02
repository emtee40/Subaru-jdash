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

import net.sourceforge.JDashLite.util.Options;
import superwaba.ext.xplat.util.props.Properties;
import waba.io.Catalog;
import waba.sys.Convert;
import waba.ui.MainWindow;
import waba.util.Vector;

/*********************************************************
 * Just a simple interface to hold settings constants.s 
 *
 *********************************************************/

public class Preferences 
{
	
	public static final String KEY_GUI_STYLE					= "gui.style";
	public static final String KEY_COM_PORT 					= "com.port";
	public static final String KEY_AUTO_CONNET 					= "auto.connect";
	public static final String KEY_DISPLAYED_SENSORS 			= "displayed.sensors";
	public static final String KEY_ACTIVE_PROFILE				= "active.profile";
	public static final String KEY_TEST_MODE 					= "test.mode";
	public static final String KEY_LOG_LEVEL					= "log.level";
//	public static final String KEY_DISABLE_AUTO_SCREEN_OFF		= "disable.auto.off";

	public static final String KEY_PROFILE_PREFIX				= "profile.";
	
	private Options appOptions_ = null;
	
	
	
	//private Catalog catalog_ = null;

	
//	/********************************************************
//	 * 
//	 *******************************************************/
//	public Preferences(Options appOptions)
//	{
//		this.appOptions_ = appOptions;
//	}
	
	/********************************************************
	 * 
	 *******************************************************/
	public Preferences(String prefix, String creatorID)
	{
		this.appOptions_ = new Options(prefix, creatorID);
	}
	
	/********************************************************
	 * Save the options to the PDB file.
	 * returns true on success, false on fail.
	 ********************************************************/
	public boolean save()
	{
		return this.appOptions_.save();
	}
	
	
	/********************************************************
	 * returns the number of profiles in the options .  This method
	 * fetches and counts at EACH call,  So, to maximize performance,
	 * call this method, and retain it's value.
	 * @return
	 ********************************************************/
	public int getProfileCount()
	{
		return getProfileKeys().size();
	}
	
	
	/*******************************************************
	 * This method fetches, sorts and returns the list of keys
	 * at each call.  So, to maximize performance, call this method and
	 * retain it's value.
	 * @return
	 ********************************************************/
	public Vector getProfileKeys()
	{
		
		Vector profileKeys = new Vector();
		
		Vector keys = this.appOptions_.getKeys();
		
		for (int index = 0; index < keys.size(); index++)
		{
			String key = (String)keys.items[index];
			if (key.startsWith(KEY_PROFILE_PREFIX))
			{
				profileKeys.addElement(key);
			}
		}
		
		/* Sort the profile keys */
		profileKeys.qsort();
		
		return profileKeys;
	}

	
	/*******************************************************
	 * 
	 * @param index
	 * @return
	 ********************************************************/
	public String getProfile(int index)
	{
		Vector keys = getProfileKeys();
		if (index >= keys.size())
		{
			return null;
		}
		return getString((String)keys.items[index], null);
	}
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	public void deleteAllProfiles()
	{
		Vector keys = getProfileKeys();
		
		/* Delete all existing profiles in the prefs object */
		for (int index = 0; index < keys.size(); index++)
		{
			this.appOptions_.remove((String)keys.items[index]);
		}
	}
	
	
	/********************************************************
	 * Add the profile to the options
	 * @param profile
	 ********************************************************/
	public void addProfile(String profile) throws Exception
	{
		
		Vector keys = getProfileKeys();
		String key = null;
		int highestKey = 0;
		
		/* Create an array of key IDs */
		for (int index = 0; index < keys.size(); index++)
		{
			key = (String)keys.items[index];
			key = key.substring(key.lastIndexOf('.') + 1, key.length());
			highestKey = Math.max(highestKey, Convert.toInt(key));
		}
		
		
		/* Save the profile */
		key = KEY_PROFILE_PREFIX + (highestKey + 1);
		setString(key, profile);
		
	}
	
	
	/*******************************************************
	 * delete the profile at the given index.
	 * @param index
	 ********************************************************/
	public void deleteProfile(int index)
	{
		
		Vector keys = getProfileKeys();
		
		if (index >= keys.size())
		{
			return;
		}
		
		this.appOptions_.remove((String)keys.items[index]);
		
	}
	
	
	/*******************************************************
	 * Given the profile index, update it's contents with this profile
	 * @param index
	 * @param profile
	 ********************************************************/
	public void updateProfile(int index, String profile)
	{
		
		Vector keys = getProfileKeys();
		
		if (index >= keys.size())
		{
			return;
		}
		
		setString((String)keys.items[index], profile);
		
	}
	
	
	/********************************************************
	 * Swap the positions of the provided profile indexes
	 * @param index1
	 * @param index2
	 ********************************************************/
	public void swapProfiles(int index1, int index2)
	{
		String profile1 = getProfile(index1);
		String profile2 = getProfile(index2);
		updateProfile(index1, profile2);
		updateProfile(index2, profile1);
	}
	
	/*******************************************************
	 * @param key
	 ********************************************************/
	public void remove(String key)
	{
		this.appOptions_.remove(key);
	}
	
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
