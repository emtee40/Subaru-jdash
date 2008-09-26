/*********************************************************
 * 
 * @author spowell
 * MetaParameter.java
 * Sep 20, 2008
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
 * A meta parameter is a special type of parameter.  It does not
 * request it's value directly from the ECU, but rather calculates
 * it's value from one or more actual ECU values.  For example, 
 * in the OBD2 protocol, the engine temperature is retreived in 
 * degrees C.  A meta paraemter can be used to translate the temp
 * into degrees F.  But, why have such as simple and bare class that does 
 * almost nothing more than the ECUParameter parent class?  
 * Mostly for identification.  Parameter Handlers can distinguish between
 * values that fetch from the ECU and ones based on a MataParameter
 * more easily this way.
 *
 *********************************************************/
public abstract class MetaParameter extends ECUParameter
{

	/********************************************************
	 * 
	 *******************************************************/
	public MetaParameter(String name)
	{
		super(name);
	}
	
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#setDemoValue()
	 ********************************************************/
	public void setDemoValue()
	{
		throw new RuntimeException("Meta Parameters do NOT have demo values set.  They rely on the parent parameters");
		
	}

}
