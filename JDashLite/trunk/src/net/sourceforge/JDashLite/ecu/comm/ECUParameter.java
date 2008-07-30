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
	public static final String RPM = "RPM";
	public static final String MPG = "MPG";
	public static final String KPH = "KPH";
	
	public static final RateSpecialParameter SPECIAL_PARAM_RATE = new RateSpecialParameter();
	
	private String name_ = null;
	
	/********************************************************
	 *  A parameter is identified by it's name.  The name of
	 *  each parameter must be unique.
	 *******************************************************/
	public ECUParameter(String name)
	{
		this.name_ = name;
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
