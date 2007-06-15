/*******************************************************
 * 
 *  @author spowell
 *  BeanShellMetaParam.java
 *  Aug 16, 2006
 *  $Id: BeanShellMetaParam.java,v 1.3 2006/09/14 02:03:42 shaneapowell Exp $
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
package net.sourceforge.JDash.ecu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;


import net.sourceforge.JDash.ecu.param.MetaParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterException;

import java.lang.Math;

import bsh.Interpreter;

/*******************************************************
 * This meta param will take a single parameter named "script"
 * and run the script against the values using BeanShell. 
 * The variables substitutions are done before hand though.
 * Variables are named as 
 ******************************************************/
public class BeanShellMetaParam extends MetaParameter
{

	
	private static final String SCRIPT_PARAM = "script";
	
	private static final String PARAM_OPEN_BRACE = "~";
	private static final String PARAM_CLOSE_BRACE = "~";
	
	private static final String SHELL_FUNCTION = " calc() ";
	private static final String SHELL_THIS_REFERENCE = "BSH";
	private static final String SHELL_VAR = "var";
	
	private String script_ = null;
	private List<Parameter> dependants_ = new ArrayList<Parameter>();
	private String name_ = null;
	
	
	private HashSet<Parameter> updatedDependants_ = new HashSet<Parameter>();
	
	
	private double value_ = 0l;
	
	/* The beah shell */
	private Interpreter bshInterpreter_ = new Interpreter();

	
	static
	{
		/* A lame cheat to prevent the not-used warning in Eclupse */
		Math.max(1,2);
	}
	
	
	/*****************************************************
	 * Create a new bean shell parameter.
	 ******************************************************/
	public BeanShellMetaParam()
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
	 * @see net.sourceforge.JDash.ecu.param.MetaParameter#setArgs(java.util.Map)
	 *******************************************************/
	@Override
	public void setArgs(Map<String, String> args) throws ParameterException
	{
		try
		{
			
			/* Get the script */
			this.script_ = args.get(SCRIPT_PARAM);
			if ((this.script_ == null) || (this.script_.length() == 0))
			{
				throw new ParameterException(getClass().getName() + "  "  + getName() + " is missing it's " + SCRIPT_PARAM + " argument");
			}
			
	
			/* Find all the param instances */
			int braceIndex = -1;
			while ((braceIndex = this.script_.indexOf(PARAM_OPEN_BRACE)) != -1)
			{
				
				/* Extract the parameter */
				String param = this.script_.substring(braceIndex + PARAM_OPEN_BRACE.length(), 
														this.script_.indexOf(PARAM_CLOSE_BRACE, braceIndex + PARAM_OPEN_BRACE.length()));
				this.script_ = this.script_.replaceAll(PARAM_OPEN_BRACE + param + PARAM_CLOSE_BRACE, " " + SHELL_THIS_REFERENCE +".getParamValue(\"" + param + "\") ");
				
				/* Add Parameter */
				Parameter p = getOwnerRegistry().getParamForName(param);
				dependants_.add(p);
				p.addObserver(this);
			}
			
			/* Setup the script method */
			this.script_ = " double " + SHELL_VAR + " = " + this.script_ + ";";
			this.script_ += "\n" + SHELL_THIS_REFERENCE + ".setCalculatedValue(" + SHELL_VAR + "); ";
			this.script_ = SHELL_FUNCTION + "{\n " + this.script_ + " \n}";
			
			/* DEBUG */
			//System.out.println(this.script_);
			
			/* If no params were added from the script, then warn the user */
			if (this.dependants_.size() == 0)
			{
				throw new ParameterException(getClass().getName() + "  " + getName() + " did not include any " + PARAM_OPEN_BRACE + "PARM" + PARAM_CLOSE_BRACE + " parts"); 
			}
	
			/* Setup the bean shell, and call the script once */

			this.bshInterpreter_.set(SHELL_THIS_REFERENCE, this);
			this.bshInterpreter_.eval(this.script_);
		}
		catch(Exception e)
		{
			throw new ParameterException("Formula Error.\nOriginal:\n" + args.get(SCRIPT_PARAM) + "\nConverted:\n" + this.script_ + "\n", e);
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
			/* Call our shell script */
			this.bshInterpreter_.eval(SHELL_FUNCTION);
			return value_;
		}
		catch(Exception e)
		{
			throw new RuntimeException("Unable to evaluate script\n" + this.script_ + "\n", e);
		}

		
	}

	/*******************************************************
	 * Override
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *******************************************************/
	public void update(Observable parameter, Object arg1)
	{
		/* We want to watch for updates from the params we're watching.  But,
		 * only notify the observers once ALL params have checked in */
		
		this.updatedDependants_.add((Parameter)parameter);
		
		if (this.updatedDependants_.size() == this.dependants_.size())
		{
			setChanged();
			notifyObservers();
			this.updatedDependants_ = new HashSet<Parameter>();
		}

	}
	
	
	/********************************************************
	 * This is a simple method used by the shell script to get
	 * the parameter values for the given name.  The generated
	 * script will have this method embedded to simplify
	 * value fetching.
	 * 
	 * @param paramName IN - the parameter name to get the value for.
	 * @return IN - the parameter value.
	 ******************************************************/
	public int getParamValue(String paramName) throws Exception
	{
		Parameter p = getOwnerRegistry().getParamForName(paramName);
		
		if (p == null)
		{
			throw new Exception("Meta Parameter attempted to retrive value for a non-existant parameter: " + paramName);
		}
		
		return (int)p.getResult();
	}
	

	/********************************************************
	 * This method is used by the beanshell script to set the value
	 * variable.  This is just to simplify the setting of this meta-parameters
	 * return value. 
	 * 
	 * @param val IN - the value to set.
	 *******************************************************/
	public void setCalculatedValue(double val)
	{
		this.value_ = val;
	}
	
	/********************************************************
	 * This method is used by the beanshell script to set the value
	 * variable.  This is just to simplify the setting of this meta-parameters
	 * return value.
	 * @param val IN - the value to set
	 *******************************************************/
	public void setCalculatedValue(String val)
	{
		setCalculatedValue(Double.parseDouble(val));
	}
	
	/********************************************************
	 * This method is used by the beanshell script to set the value
	 * variable.  This is just to simplify the setting of this meta-parameters
	 * return value.
	 * @param val IN - the value to set
	 *******************************************************/
	public void setCalculatedValue(int val)
	{
		setCalculatedValue((double)val);
	}
	

	
	/******************************************************/
	/******************************************************/
	/******************************************************
	 * Below this point, these are designed to be utility functions
	 * available to the shell script
	 *******************************************************/
	
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
}
