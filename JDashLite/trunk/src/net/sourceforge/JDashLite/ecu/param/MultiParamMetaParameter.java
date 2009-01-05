/*********************************************************
 * 
 * @author spowell
 * OneParamMetaParameter.java
 * Sep 20, 2008
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

package net.sourceforge.JDashLite.ecu.param;

import waba.util.Vector;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.MetaParameter;
import net.sourceforge.JDashLite.ecu.comm.ValueChangedListener;

/*********************************************************
 * A multi param meta parameter is a parameter that bases
 * it's value on 1-n other parameters.  By default, when the 
 * first parameter in the array has it's new value notify method
 * fired, it will trigger the notify method of this meta paraemter.
 * The new value notification of the remaining ECUParameters will
 * have no effect. 
 *
 *********************************************************/
public abstract class MultiParamMetaParameter extends MetaParameter
{

	/** The local member variable that holds the p1 value passed to the constructor */
	protected ECUParameter[] params_ = null;
	
	/********************************************************
	 *  
	 *******************************************************/
	public MultiParamMetaParameter(String name, ECUParameter[] params)
	{
		super(name);
		this.params_ = params;

		/* If no parameters were provided, then no point in listening for any changes */
		if ((this.params_ == null) || (this.params_.length < 1))
		{
			return;
		}

		if (this.params_[0] == null)
		{
			return;
		}
		
		this.params_[0].addValueChangedListener(new ValueChangedListener()
		{
			public void onValueChanged()
			{
				notifyValueChanged();
			}
		});
	}
	
	

	/*********************************************************
	 * Enable/disable this parameter, and ALSO the dependant this.p1_ parameter
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#setEnabled(boolean)
	 ********************************************************/
	public void setEnabled(boolean isEnabled)
	{
		super.setEnabled(isEnabled);
		for (int index = 0; index < this.params_.length; index++)
		{
			this.params_[index].setEnabled(isEnabled);
		}
	}
	

}
