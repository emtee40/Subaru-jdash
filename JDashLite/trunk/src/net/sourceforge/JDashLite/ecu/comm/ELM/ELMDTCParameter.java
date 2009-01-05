/*********************************************************
 * 
 * @author spowell
 * ELMDTCParameter.java
 * Jan 4, 2009
 *
Copyright (C) 2009 Shane Powell

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

import waba.sys.Convert;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.param.OneParamMetaParameter;
import net.sourceforge.JDashLite.error.ErrorLog;

/*********************************************************
 * 
 *
 *********************************************************/

public class ELMDTCParameter extends OneParamMetaParameter
{

	private int offset_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ELMDTCParameter(String name, ECUParameter p1, int offset)
	{
		super(name, p1);
		this.offset_ = offset;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getDescription()
	 ********************************************************/
	public String getDescription()
	{
		return "Diagnostic Trouble Code n";
	}

	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getLabel()
	 ********************************************************/
	public String getLabel()
	{
		return getName();
	}

	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		int c1 = ((ELMParameter)p1_).getResponseByte(this.offset_);
		int c2 = ((ELMParameter)p1_).getResponseByte(this.offset_+1);
		int c3 = ((ELMParameter)p1_).getResponseByte(this.offset_+2);
		int c4 = ((ELMParameter)p1_).getResponseByte(this.offset_+3);
		
		//ErrorLog.info(getName() + " Gauge Requested " + c1 + " " + c2);
		
		String dtc = c1 + "" + c2 + "" + c3 + "" + c4;
		return Convert.toDouble(dtc);
		
	}

}
