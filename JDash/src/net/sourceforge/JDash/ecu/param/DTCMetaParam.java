/*******************************************************
 * 
 *  @author spowell
 *  BeanShellMetaParam.java
 *  Aug 16, 2006
 *  $Id: DTCMetaParam.java,v 1.1 2006/12/31 16:59:09 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

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
 ******************************************************/
package net.sourceforge.JDash.ecu.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;




/*******************************************************
 * DTCs are a different thing all together.  JDash was 
 * created to be a real-time display of sensor values, and 
 * DTCs don't exactly fit into this definition.  Well.. mostly.
 * So.. A special type of Meta parameter was created for DTCs.
 * It's up to each implementation of a monitor to detect any and all
 * configured DTCMetaParam objects within the parameter registry and
 * deal with them accordingly. 
 ******************************************************/
public class DTCMetaParam extends MetaParameter implements StringParameter
{
	
	private List<Parameter> dependants_ = new ArrayList<Parameter>();
	private String name_ = null;
	
	private String dtcValue_ = null;
	
	
	/*****************************************************
	 * Create a DTC parameter
	 ******************************************************/
	public DTCMetaParam()
	{
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.MetaParameter#getDependants()
	 *******************************************************/
	@Override
	public List<Parameter> getDependants()
	{
		return this.dependants_;
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.MetaParameter#setName(java.lang.String)
	 *******************************************************/
	@Override
	public void setName(String name)
	{
		this.name_ = name;

	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.MetaParameter#addArg(java.lang.String, java.lang.String)
	 *******************************************************/
	@Override
	public void addArg(String name, String value) throws ParameterException
	{
		/* Do Nothing */
		
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.Parameter#getName()
	 *******************************************************/
	@Override
	public String getName()
	{
		return this.name_;
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.Parameter#getResult()
	 *******************************************************/
	public double getResult()
	{
		throw new RuntimeException("Method not supported, use toString() or getDTCCode() instead.");
	}

	
	/*******************************************************
	 * @param dtc
	 *******************************************************/
	public void setDTCCode(String dtc)
	{
		this.dtcValue_ = dtc;
		fireValueChangedEvent();
	}
	
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public String getDTCCode()
	{
		if (this.dtcValue_ == null)
		{
			return "";
		}
		else
		{
			return this.dtcValue_;
		}
	}
	
	/******************************************************
	 * A DTC Parameter does NOT return a double result.  It
	 * kinda breaks the parameter rule a bit. But.. just a bit.
	 * Instead of using getResult(), use this toString() or getDTCCode();
	 * 
	 * Override
	 * @see java.lang.Object#toString()
	 *******************************************************/
	public String toString()
	{
		return this.getDTCCode();
	}


	
	
}
