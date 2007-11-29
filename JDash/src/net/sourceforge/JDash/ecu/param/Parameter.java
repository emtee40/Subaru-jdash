/*******************************************************
 * 
 *  @author spowell
 *  Parameter
 *  Aug 8, 2006
 *  $Id: Parameter.java,v 1.3 2006/12/31 16:59:09 shaneapowell Exp $
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
import java.util.Observable;


/******************************************************
 * This basic parameter class is simple class whos
 * sole purpose is to provide data to the renderers/Observers.
 *****************************************************/
public abstract class Parameter implements DoubleParameter
{
	
	
	
	private ParameterRegistry ownerRegistry_ = null;
	
	
	/* The linked listeners */
	private ArrayList<ParameterEventListener> eventListeners_ = new ArrayList<ParameterEventListener>();
	
	
	/*******************************************************
	 * This method will return the owner registry value that
	 * was set with the setOnwerRegistry metod.
	 * 
	 * @return the owner registry.
	 *******************************************************/
	public ParameterRegistry getOwnerRegistry()
	{
		return this.ownerRegistry_;
	}
	
	

	
	
	/********************************************************
	 * @param l
	 *******************************************************/
	public void addEventListener(ParameterEventListener l)
	{
		if (this.eventListeners_.contains(l) == false)
		{
			this.eventListeners_.add(l);
		}
	}
	
	/*******************************************************
	 * @param l
	 *******************************************************/
	public void removeEventListener(ParameterEventListener l)
	{
		this.eventListeners_.remove(l);
	}
	
	/*******************************************************
	 * Call this method to send the valueChanged() event
	 * message to all the event listeners.
	 *******************************************************/
	public void fireValueChangedEvent()
	{
		for (ParameterEventListener l : this.eventListeners_)
		{
			l.valueChanged(this);
		}
	}
	
	/********************************************************
	 * Set the owner registry of this parameter.
	 * @param ownerRegistry
	 *******************************************************/
	public void setOwnerRegistry(ParameterRegistry ownerRegistry)
	{
		this.ownerRegistry_ = ownerRegistry;
	}
	
	/*******************************************************
	 * Get the string Name/ID of this parameter.
	 * @return
	 *******************************************************/
    public abstract String getName();
    
    
    /*******************************************************
     * Override
     * @see java.lang.Object#toString()
     *******************************************************/
    @Override
    public String toString()
    {
    	return "" + getResult();
    }
    
    /*******************************************************
     * Get the currently set ecu result value for this parameter.
     * The value is returned as a double to preserve any precision when
     * math is done on this value.  For raw ECU parameter values, this
     * will always be an integer, but the MetaParameter class
     * can easily return a decimal value.
     * @return
     *******************************************************/
    public abstract double getResult();
    
    
    /*******************************************************
     * the equals() method will simply compare parameter names.
     * If they match, you'll get a true, if not, false.
     * Override
     * @see java.lang.Object#equals(java.lang.Object)
     *******************************************************/
    @Override
    public boolean equals(Object obj)
    {
    	if (obj instanceof Parameter == false)
    	{
    		return false;
    	}
    	
    	return getName().equals(((Parameter)obj).getName());
    }
}
