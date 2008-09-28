/*********************************************************
 * 
 * @author spowell
 * CtoFMetaParam.java
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

package net.sourceforge.JDashLite.ecu.param;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;

/*********************************************************
 * The simplest, and fastes resonding MPG calculating Meta Parameter.
 * This one is dependant on MAF, KPH.
 * The calculation is as follows
 <pre>
 MPG = (14.7 * 6.17 * 4.54 * KPH * 0.621371) / (3600 * MAF / 100)
14.7 grams of air to 1 gram of gasoline - ideal air/fuel ratio
6.17 pounds per gallon - density of gasoline
4.54 grams per pound - conversion
VSS - vehicle speed in kilometers per hour
0.621371 miles per hour/kilometers per hour - conversion
3600 seconds per hour - conversion
MAF - mass air flow rate in 100 grams per second
100 - to correct MAF to give grams per second
 </pre>
 *
 *********************************************************/
public class MPG1MetaParam extends MultiParamMetaParameter
{
	
	private static final String DESC = "This is the simplest and fastest of the MPG calculations.  " +
			"This one depends only on your cars MAF and KPH values.  So, your car will need a MAF sensor. " +
			"If your care does not have a MAF, don't worry.  Your ECU might calculate the MAF value " +
			"and provide it anyway.  My 2000 Subaru does not have a MAF, but the ECU does indeed provide a MAF value.";
	private static final int MAF_NDX = 0;
	private static final int KPH_NDX = 1;
	
	/********************************************************
	 * 
	 *******************************************************/
	public MPG1MetaParam(String name, ECUParameter maf, ECUParameter kph)
	{
		super(name, new ECUParameter[] {maf, kph});
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		if (this.params_[MAF_NDX].getValue() != 0)
		{
			return (411.77346 * this.params_[KPH_NDX].getValue() *  0.621371) / (3600.0 * this.params_[MAF_NDX].getValue() / 100);
		}
		else
		{
			return 0.0;
		}
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getLabel()
	 ********************************************************/
	public String getLabel()
	{
		return "MPG 1";
	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getDescription()
	 ********************************************************/
	public String getDescription()
	{
		return DESC; 
	}
}
