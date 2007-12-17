/*******************************************************
 * 
 *  @author spowell
 *  InternalParam.java
 *  Dec 17, 2007
 *  $Id:$
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
package net.sourceforge.JDash.ecu.param.special;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.StringParameter;




/*******************************************************
 * An Internal param is one that was not defined by an ECU file, 
 * or that is used to extract info from the ECU. An InternalParameter
 * is one that is created, and used internally by a Monitor.
 ******************************************************/
public class InternalParam extends Parameter implements StringParameter
{
	

	private String name_ = null;
	private String value_ = null;
	
	
	/*****************************************************
	 * Create a DTC parameter
	 ******************************************************/
	public InternalParam(String name)
	{
		this.name_ = name;
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
		throw new RuntimeException("Method not supported, use toString() instead.");
	}

	
	/********************************************************
	 * Sets the value of this internal parameter.
	 * @param value
	 *******************************************************/
	public void setValue(String value)
	{
		this.value_ = value;
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
		return (this.value_==null?"":this.value_);
	}


	
	
}
