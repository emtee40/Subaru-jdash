/*********************************************************
 * 
 * @author spowell
 * DigitalGauge.java
 * Jul 30, 2008
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

package net.sourceforge.JDashLite.profile.gauge;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ValueChangedListener;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 * Based on a digital gauge, this type of gauge maintaines a
 * running cummulative running average of the values that
 * are recorded.  Because it's a cummulative calulation, it does
 * not store all of the previous values, and re-cal each time. 
 * Instead, the calculation applies the new value in a cummulative
 * manner.  This results in a very fast, and low memory calculation.
 * This was the source of the algorithm
 * http://en.wikipedia.org/wiki/Moving_average#Cummulative_moving_average
 *
 *********************************************************/
public class DigitalCummulativeAverageGauge extends DigitalGauge implements ValueChangedListener
{
	private double valueCount_ = 0;
	private double runningAverage_ = 0.0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public DigitalCummulativeAverageGauge()
	{
	}

	/*********************************************************
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 ********************************************************/
	protected void finalize() throws Throwable
	{
		getECUParameter().removeValueChangedListener(this);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.DigitalGauge#getCurrentDisplayValue()
	 ********************************************************/
	protected double getCurrentDisplayValue()
	{
		return runningAverage_;;
	}
	
	/*********************************************************
	 * We trap the set parameter method so we can add the value change listener
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#setECUParameter(net.sourceforge.JDashLite.ecu.comm.ECUParameter)
	 ********************************************************/
	public void setECUParameter(ECUParameter param)
	{
		super.setECUParameter(param);
		param.addValueChangedListener(this);
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ValueChangedListener#onValueChanged()
	 ********************************************************/
	public void onValueChanged()
	{
		double newValue = getECUParameter().getValue();
		double currentAverage = this.runningAverage_;
		this.valueCount_ += 1;

		this.runningAverage_ = currentAverage + ((newValue - currentAverage) / (this.valueCount_));
		
		
	}
	
}
