/*******************************************************
 * 
 *  @author spowell
 *  JSStringMetaParam.java
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
package net.sourceforge.JDash.ecu.param;



/*******************************************************
 * This meta param will take a single parameter named "script"
 * and run the script against the values using BeanShell. 
 * The variables substitutions are done before hand though.
 * Variables are named as 
 ******************************************************/
public class JSStringMetaParam extends JSMetaParam implements StringParameter
{

	
	/* The value object is an Object instead of the literal double, because beanshell parameters can be quite
	 * crafty.  The usual is to return a double value, but once in a while, a metaparameter is setup that
	 * returns a string.  */
	private String value_ = null;
	
	
	
	/*****************************************************
	 * Create a new bean shell parameter.
	 ******************************************************/
	public JSStringMetaParam()
	{
	}
	
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.Parameter#getResult()
	 *******************************************************/
	public double getResult()
	{
		throw new RuntimeException("getResult() not implemented by this class.  Use toStrin() instead.");
	}
	
	
	/*******************************************************
	 * @param value
	 *******************************************************/
	public void setValue(String value)
	{
		this.value_ = value;
	}
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.Parameter#toString()
	 *******************************************************/
	public String toString()
	{
		return this.value_;
	}

		
	
}
