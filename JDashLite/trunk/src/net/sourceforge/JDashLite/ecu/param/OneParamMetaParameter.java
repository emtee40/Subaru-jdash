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

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.MetaParameter;
import net.sourceforge.JDashLite.ecu.comm.ValueChangedListener;

/*********************************************************
 * Most, not all, but mote meta parameters are a calculation conversion
 * of one ECU value.  Like, KPH to MPG or Degrees C to Degrees F.
 * The concrete class that does the conversion should extend this 
 * abstract class for simplicity.  The p1 parameter is made available
 * as the local member variable p1_
 *
 *********************************************************/
public abstract class OneParamMetaParameter extends MetaParameter
{

	/** The local member variable that holds the p1 value passed to the constructor */
	protected ECUParameter p1_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public OneParamMetaParameter(String name, ECUParameter p1)
	{
		super(name);
		this.p1_ = p1;
		this.p1_.addValueChangedListener(new ValueChangedListener()
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
		this.p1_.setEnabled(isEnabled);
	}
	

}
