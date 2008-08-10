/*********************************************************
 * 
 * @author spowell
 * Parameter.java
 * Jul 27, 2008
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

package net.sourceforge.JDashLite.ecu.comm;

/*********************************************************
 * The base class of all parameters.  By default
 * Parameters handled by JDash deal in double values.
 *
 *********************************************************/
public abstract class ECUParameter
{
	public static final String RATE = "RATE";
	
	public static final RateSpecialParameter SPECIAL_PARAM_RATE = new RateSpecialParameter();
	
	private boolean isEnabled_ = true;
	
	private String name_ = null;
	
	/********************************************************
	 *  A parameter is identified by it's name.  The name of
	 *  each parameter must be unique.
	 *******************************************************/
	public ECUParameter(String name)
	{
		this.name_ = name;
	}

	
	/********************************************************
	 * An enabled parameter is one that is ready and willing
	 * to be used to fetch and display parameters.  A disabled
	 * parameter might be one that the profile has determined
	 * is not visible, and therefor is not needed to be fetched.
	 * So, it will be disabled.
	 * @return the isEnabled
	 ********************************************************/
	public boolean isEnabled()
	{
		return this.isEnabled_;
	}
	
	
	/********************************************************
	 * @param isEnabled the isEnabled to set
	 ********************************************************/
	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled_ = isEnabled;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public String getName()
	{
		return this.name_;
	}

	/********************************************************
	 * Return the adjusted, calculated and formatted value
	 * this parameter represents
	 * @return
	 ********************************************************/
	public abstract double getValue();
	
}
