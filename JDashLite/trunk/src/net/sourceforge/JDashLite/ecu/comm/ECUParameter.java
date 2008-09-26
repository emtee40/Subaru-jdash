/*********************************************************
 * 
 * @author spowell
 * Parameter.java
 * Jul 27, 2008
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

package net.sourceforge.JDashLite.ecu.comm;

import net.sourceforge.JDashLite.Cleanable;
import waba.sys.Vm;
import waba.util.Vector;

/*********************************************************
 * The base class of all parameters.  By default
 * Parameters handled by JDash deal in double values.
 *
 *********************************************************/
public abstract class ECUParameter implements Cleanable
{
	private boolean isEnabled_ = true;
	
	private String name_ = null;
	
	private int timeStamp_ = 0;
	
	private Vector valueChangedListeners_ = new Vector(1);
	
	/** 
	 * The default is that all parameters are user selectable.  This variable
	 * can be used to change that behavior.
	 */
	protected boolean isSelectable_ = true;
	
	/********************************************************
	 *  A parameter is identified by it's name.  The name of
	 *  each parameter must be unique.
	 *******************************************************/
	public ECUParameter(String name)
	{
		this.name_ = name;
	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.Cleanable#clean()
	 ********************************************************/
	public void clean()
	{
		this.valueChangedListeners_.removeAllElements();
		
	}	
	
	/********************************************************
	 * An enabled parameter is one that is ready and willing
	 * to be used to fetch and display parameters.  A disabled
	 * parameter might be one that the profile has determined
	 * is not visible, and therefor is not needed to be fetched.
	 * So, it will be disabled.
	 * @return the isEnabled
	 ********************************************************/
	public boolean isEnabled()
	{
		return this.isEnabled_;
	}
	
	
	/********************************************************
	 * @param isEnabled the isEnabled to set
	 ********************************************************/
	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled_ = isEnabled;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public String getName()
	{
		return this.name_;
	}

	/********************************************************
	 * Return a brief human readable description of this parameters.
	 * This description will be shown to the user as they select it
	 * for display on the screen.
	 * 
	 * @return
	 ********************************************************/
	public abstract String getDescription();
	
	/*******************************************************
	 * Slightly different from the parameters Name, it's Label is
	 * a display string to show the user when they are selecting 
	 * from a list.  the getName() returns the unique string key for
	 * this parameter, but the Label is for the users.
	 * @return
	 ********************************************************/
	public abstract String getLabel();
	
	
	/*******************************************************
	 * By default, all ECU parameters are user selectable for display.
	 * If however a parameter should NOT be user selectable, trip
	 * the isSelectable_ member variable that is returned by this
	 * method.
	 * @return
	 ********************************************************/
	public boolean isUserSelectable()
	{
		return this.isSelectable_;
	}
	
	/********************************************************
	 * @return
	 ********************************************************/
	public int getTimeStamp()
	{
		return this.timeStamp_;
	}
	
	
	/*******************************************************
	 * Set the current timestamp. 
	 ********************************************************/
	public void notifyValueChanged()
	{
		this.timeStamp_ = Vm.getTimeStamp();
		for (int index = 0; index < this.valueChangedListeners_.size(); index++)
		{
			((ValueChangedListener)this.valueChangedListeners_.items[index]).onValueChanged();
		}
	}
	
	/*******************************************************
	 * @param l
	 ********************************************************/
	public void addValueChangedListener(ValueChangedListener l)
	{
		this.valueChangedListeners_.addElement(l);
	}
	
	/*******************************************************
	 * @param l
	 ********************************************************/
	public void removeValueChangedListener(ValueChangedListener l)
	{
		this.valueChangedListeners_.removeElement(l);
	}
	
	/********************************************************
	 * Return the adjusted, calculated and formatted value
	 * this parameter represents
	 * @return
	 ********************************************************/
	public abstract double getValue();
	
	
	/*******************************************************
	 * This method is called by the Test/Demo mode protocol
	 * handler.  It's the job of the parameter to know how to 
	 * put itself into a demo mode, and adjust it's value to 
	 * simulate ECU values.
	 ********************************************************/
	public abstract void setDemoValue();
	
}
