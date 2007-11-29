/*******************************************************
 * 
 *  @author spowell
 *  BeanShellMetaParam.java
 *  Aug 16, 2006
 *  $Id: BeanShellMetaParam.java,v 1.1 2006/12/31 16:59:09 shaneapowell Exp $
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
import java.lang.Math;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/*******************************************************
 * This meta param will take a single parameter named "script"
 * and run the script against the values using BeanShell. 
 * The variables substitutions are done before hand though.
 * Variables are named as 
 ******************************************************/
public class JSMetaParam extends MetaParameter
{

	
	private static final String ARG_NAME_SCRIPT = "script";
	private static final String ARG_NAME_DEPENDANT = "dependant"; 
	
	private static final String JS_THIS_REFERENCE = "meta";

	
	private String script_ = null;
	private List<Parameter> dependants_ = new ArrayList<Parameter>();
	private String name_ = null;
	

	private Context rhinoContext_ = null;
	private Scriptable rhinoScope_ = null;
	private Script rhinoScript_ = null;

	
	
	/* The value object is an Object instead of the literal double, because beanshell parameters can be quite
	 * crafty.  The usual is to return a double value, but once in a while, a metaparameter is setup that
	 * returns a string.  */
	private Double value_ = new Double(0);
	
	
	static
	{
		/* A lame cheat to prevent the not-used warning in Eclupse, because the Math pack is used in the script. */
		Math.max(1,2);
	}
	
	
	/*****************************************************
	 * Create a new bean shell parameter.
	 ******************************************************/
	public JSMetaParam()
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


	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.MetaParameter#addArg(java.lang.String, java.lang.String)
	 *******************************************************/
	@Override
	public void addArg(String name, String value) throws ParameterException
	{
		try
		{
			/* If it's a script arg */
			if (ARG_NAME_SCRIPT.equals(name))
			{
				this.script_ = 	"function _js() \n" +
								"{ \n /********/\n" + value + "\n/********/\n}\n  meta.setValue(_js())\n";
				
				if ((this.script_ == null) || (this.script_.length() == 0))
				{
					throw new ParameterException(getClass().getName() + "  "  + getName() + " is missing it's " + ARG_NAME_SCRIPT + " argument");
				}
				
			}
			
			
			/* The only other thing we care about are dependant arguments */
			if (ARG_NAME_DEPENDANT.equals(name))
			{
				Parameter p = getOwnerRegistry().getParamForName(value);
				if (p == null)
				{
					throw new ParameterException(getClass().getName() + " " + getName() +  " references an unknown dependant " + value);
				}
				this.dependants_.add(p);
			}
			
			
		}
		finally
		{
			this.rhinoContext_ = null;
		}
		

		
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
		
		try
		{

			if (this.rhinoContext_ == null)
			{
				
					this.rhinoContext_ = Context.enter();
					this.rhinoScope_ = this.rhinoContext_.initStandardObjects();
		
		
					this.rhinoScript_ = this.rhinoContext_.compileString(this.script_, "JSMetaParam", 1, null);
					ScriptableObject.putProperty(this.rhinoScope_, JS_THIS_REFERENCE, Context.javaToJS(this, this.rhinoScope_));
		
					this.rhinoScript_.exec(this.rhinoContext_, this.rhinoScope_);
					
			}
		
			/* Call our rhino script */
			this.rhinoScript_.exec(this.rhinoContext_, this.rhinoScope_);
			return this.value_;
		}
		catch(Exception e)
		{
			throw new RuntimeException("JSMetaParam error in " + this.getName() + "\n", e);
		}

		
	}

	
	
	/******************************************************/
	/******************************************************/
	/******************************************************
	 * Below this point, these are designed to be utility functions
	 * available to the shell script
	 *******************************************************/

	
	/********************************************************
	 * This is a simple method used by the shell script to get
	 * the parameter values for the given name.  The generated
	 * script will have this method embedded to simplify
	 * value fetching.
	 * 
	 * @param paramName IN - the parameter name to get the value for.
	 * @return IN - the parameter value.
	 ******************************************************/
	public double getParamValue(String paramName) throws Exception
	{
		
		Parameter p = getOwnerRegistry().getParamForName(paramName);
		
		/* For help in problems, and debugging, lets make sure that this parameter
		 * was identified as a dependant */
		if (this.dependants_.contains(p) == false)
		{
			throw new Exception("a call to getParamValue(\"" + paramName + "\") in the script for [" + getName() + "] was made.\n" +
					" But this parameter was never identified in the dependants list.");
		}
		

		
		if (p == null)
		{
			throw new Exception("Meta Parameter attempted to retrive value for a non-existant parameter: " + paramName);
		}
		
		return p.getResult();
	}
	
	
	
	/********************************************************
	 * @return
	 *******************************************************/
	public double getValue()
	{
		return this.value_;
	}

	/********************************************************
	 * This method is used by the beanshell script to set the value
	 * variable.  This is just to simplify the setting of this meta-parameters
	 * return value. 
	 * 
	 * @param val IN - the value to set.
	 *******************************************************/
	public void setValue(double val)
	{
		this.value_ = val;
		fireValueChangedEvent();
	}
	

	
	/*******************************************************
	 * This utility function is made available to the shell script
	 * for the purpose of taking a highbyte and a low byte, and
	 * makeing them into a single int value.
	 * @param highByte IN - the high byte of the int.
	 * @param lowByte IN - the low byte of the int.
	 * @return the resulting int, but in double format.  We return
	 * the value as a double, to help enforse double int point precision
	 * from within the shell script.
	 ******************************************************/
	public double makeint(int highByte, int lowByte)
	{
		return ((highByte << 8) + lowByte);
	}
	
	/*******************************************************
	 * This utility funciton is available to the bean shell for
	 * generating a 1 or 0 from a bitmask.
	 * @param paramValue IN - the parameter value to bitcheck.
	 * @param bit IN - the bit to check.  the bit order for a byte is "76543210"
	 * @return a 1 if the bit is set, a 0 if the bit is not set.
	 *******************************************************/
	public double bitcheck(int paramValue, int bit)
	{
		int mask = 0x01 << bit;
		
		if ((paramValue & mask) > 0)
		{
			return 1;
		}
		else
		{
			return 0;
		}
		
	}
	
	
	/********************************************************
	 * This utility method simply converts the double to a long, then
	 * shifts the bits right "bits" number of bits.
	 * @param d
	 * @param bits
	 * @return
	 *******************************************************/
	public long bitShiftRight(double d, int bits)
	{
		return ((long)d) >> bits;
	}
	
	/********************************************************
	 * This utility method simply converts the double to a long, then
	 * shifts the bits right "bits" number of bits.
	 * @param d
	 * @param bits
	 * @return
	 *******************************************************/
	public long bitShiftLeft(double d, int bits)
	{
		return ((long)d) << bits;
	}
	
}
