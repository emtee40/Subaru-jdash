/*******************************************************
 * 
 *  @author spowell
 *  MetaParameter
 *  Aug 8, 2006
 *  $Id: MetaParameter.java,v 1.3 2006/12/31 16:59:09 shaneapowell Exp $
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


import java.util.List;

/******************************************************
 * A MetaParameter is an abstract class that represents a parameter value that is
 * somewhat dynamic.  A meta parameter is not a value retreived diretly from the 
 * ECU.  Instead, it's a parameter that can have special operations done on it's
 * value before returned to observer.  The best example is the MetaParameter.
 * 
 * @see MetaParameter
 ******************************************************/
public abstract class MetaParameter extends Parameter // implements Observer
{
	
	/*******************************************************
	 * Return the list of dependant parameters. This is the parameter
	 * objects that this meta parameter uses for it's data before 
	 * calculating it's final result.
	 * 
	 * @return
	 *******************************************************/
	public abstract List<Parameter> getDependants();
	
	/********************************************************
	 * Set the name of this meta parameter.
	 * 
	 * @param name
	 *******************************************************/
	public abstract void setName(String name);
	
	/*******************************************************
	 * When loaded from the xml parameter file each arg will
	 * be loaded, and passed to this method one at a time, and in order
	 * 
	 * @param args
	 * @throws ParameterException
	 *******************************************************/
	public abstract void addArg(String name, String value) throws ParameterException;
	
	
	/******************************************************
	 * When a MetaParameter is enabled/disabled, all it's dependants
	 * are also enabled/disabled.
	 * Override
	 * @see net.sourceforge.JDash.ecu.param.Parameter#setEnabled(boolean)
	 *******************************************************/
	public void setEnabled(boolean enable)
	{
		for (Parameter dep : getDependants())
		{
			dep.setEnabled(enable);
		}
		super.setEnabled(enable);
	}
	
}
