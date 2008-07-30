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

package net.sourceforge.JDashLite.ecu.comm;

import waba.sys.Convert;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;


/*********************************************************
 * 
 *
 *********************************************************/
public class RateSpecialParameter extends ECUParameter
{
	
	private double rate_ = 0.0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public RateSpecialParameter()
	{
		super(RATE);
	}

	/********************************************************
	 * @param rate
	 ********************************************************/
	public void setRate(double rate)
	{
		this.rate_ = rate;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		return this.rate_;
	}

}
