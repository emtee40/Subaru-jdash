/*******************************************************
 * 
 *  @author spowell
 *  LogParameter.java
 *  Aug 23, 2006
 *  $Id: LogParameter.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.logger;

import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * This extension to a Parameter used by the logging packing
 * contains the extra needed methods for logging use.
 ******************************************************/
public class LogParameter extends Parameter
{

	private String name_ = null;
	private double result_ = 0l;
	private long eventTime_ = 0l;
	
	
	/******************************************************
	 * @param name
	 * @param result
	 * @param time
	 *****************************************************/
	public LogParameter(String name, double result, long time)
	{
		this.name_ = name;
		this.result_ = result;
		this.eventTime_ = time;
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
	@Override
	public double getResult()
	{
		return this.result_;
	}
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public long getEventTime()
	{
		return this.eventTime_;
	}
	
}
