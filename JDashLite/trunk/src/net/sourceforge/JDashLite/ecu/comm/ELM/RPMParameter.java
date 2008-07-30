/*********************************************************
 * 
 * @author spowell
 * ELMProtocol.java
 * Jul 26, 2008
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

package net.sourceforge.JDashLite.ecu.comm.ELM;


/*********************************************************
 * 
 *
 *********************************************************/
public class RPMParameter extends ELMParameter
{
	
	/********************************************************
	 * @param name
	 * @param command
	 *******************************************************/
	public RPMParameter()
	{
		super(RPM, 1, 0x0c, 2);
	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		int a = getResponseByte(0);
		int b = getResponseByte(1);
		
		return ((a * 256) + b) / 4; 
	}

}
