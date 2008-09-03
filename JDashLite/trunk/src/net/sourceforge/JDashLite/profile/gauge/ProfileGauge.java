/*********************************************************
 * 
 * @author spowell
 * ProfileGauge.java
 * Jul 30, 2008
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

package net.sourceforge.JDashLite.profile.gauge;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.sys.Convert;
import waba.util.Hashtable;
import waba.util.Vector;


/*********************************************************
 * A profile gauge is an abstract class with methods used to 
 * get and fetch properties that will describe how to 
 * actually render a concrete version of this abstract class.
 * It is VERY important to note that all properties are stored
 * as a String.  They are converted to and from any native
 * types (int, long, double) on the fly.  This is important because
 * converstion can take up a lot more time than expected if used
 * in tight loops.  Your wondering why?  Lazy!  Lazy in that a 
 * ProfileGauge can now be easily inspected by the XML content handler
 * for it's properties.  These properties can be now saved to and read
 * from an XML file quiete easily.  Therefor, if you have a 
 * member variable that you do NOT need saved, do NOT use the properties
 * methods here.
 *
 *********************************************************/
public abstract class ProfileGauge //implements RenderableProfileComponent
{
	
	protected static final String BOOLEAN_TRUE				= "true";
	protected static final String BOOLEAN_FALSE				= "false";

	public static final String PROP_STR_LABEL 				= "label";
	public static final String PROP_STR_PARAMETER_NAME 		= "param";
	public static final String PROP_D_WIDTH 				= "width";
	
	
	private Hashtable props_ = new Hashtable(3);

	
	private ECUParameter param_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileGauge()
	{
	}
	
	
	/********************************************************
	 * @param param
	 ********************************************************/
	public void setECUParameter(ECUParameter param)
	{
		this.param_ = param;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public ECUParameter getECUParameter()
	{
		return this.param_;
	}
	
	/*******************************************************
	 * @param key
	 * @return
	 ********************************************************/
	public String getProperty(String key)
	{
		return (String)this.props_.get(key);
	}
	
	/******************************************************
	 * @param key
	 * @param ifNull
	 * @return
	 ********************************************************/
	public String getProperty(String key, String ifNull)
	{
		String str = getProperty(key);
		if (str == null)
		{
			return ifNull;
		}
		else
		{
			return str;
		}
	}
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setProperty(String key, String value)
	{
		this.props_.put(key,value);
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public Vector getPropertyKeys()
	{
		return this.props_.getKeys();
	}
	
	/*******************************************************
	 * @param key IN - the key to return the value of.
	 * @param ifNull IN - if the property is not found, return this value
	 * @return
	 ********************************************************/
	public int getIntProperty(String key, int ifNull)
	{
		String str = getProperty(key);
		if (str == null)
		{
			return ifNull;
		}
		else
		{
			return Convert.toInt(str);
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setIntProperty(String key, int value)
	{
		setProperty(key, Convert.toString(value));
	}
	
	/*******************************************************
	 * @param key IN - the key to return the value of.
	 * @param ifNull IN - if the property is not found, return this value
	 * @return
	 ********************************************************/
	public double getDoubleProperty(String key, double ifNull)
	{
		String str = getProperty(key);
		if (str == null)
		{
			return ifNull;
		}
		else
		{
			return Convert.toDouble(str);
		}
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setDoubleProperty(String key, double value)
	{
		setProperty(key, Convert.toString(value));
	}

	
	/*******************************************************
	 * Unlike the other native getters, a missing boolean is the 
	 * same as a FALSE.
	 * @param key IN - the key to return the value of.
	 * @return
	 ********************************************************/
	public boolean getBooleanProperty(String key)
	{
		String str = getProperty(key);
		return BOOLEAN_TRUE.equals(str);
	}
	
	
	/*******************************************************
	 * @param key
	 * @param value
	 ********************************************************/
	public void setBooleanProperty(String key, boolean value)
	{
		if (value)
		{
			setProperty(key, BOOLEAN_TRUE);
		}
		else
		{
			setProperty(key, BOOLEAN_FALSE);
		}
	}


	/********************************************************
	 * @param g
	 * @param r
	 * @param p
	 * @param cm
	 * @param forceRedrawAll IN - force a redraw and re-calc of ALL elements.
	 * @param staticContent
	 ********************************************************/
	public abstract void render(Graphics g, Rect r, ColorModel cm, boolean forceRedraw, boolean includingStaticContent);
}
