/*********************************************************
 * 
 * @author spowell
 * ProfileRow.java
 * Aug 7, 2008
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

package net.sourceforge.JDashLite.profile;

import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import waba.util.Vector;

/*********************************************************
 * 
 *
 *********************************************************/
public class ProfileRow
{

	private double heightPercent_ = -1;
	private Vector gauges_ = new Vector(2);
	
	
	/********************************************************
	 * @param index
	 * @return
	 ********************************************************/
	public ProfileGauge getGauge(int index)
	{
		return (ProfileGauge)this.gauges_.items[index];
	}
	
	/********************************************************
	 * @return the heightPercent
	 ********************************************************/
	public double getHeightPercent()
	{
		return this.heightPercent_;
	}
	
	
	/********************************************************
	 * @param heightPercent the heightPercent to set
	 ********************************************************/
	public void setHeightPercent(double heightPercent)
	{
		this.heightPercent_ = heightPercent;
	}
	
	/********************************************************
	 * @param gauge
	 ********************************************************/
	public void addGauge(ProfileGauge gauge)
	{
		this.gauges_.addElement(gauge);
	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int getGaugeCount()
	{
		return this.gauges_.size();
	}
	
	/********************************************************
	 * @param index
	 ********************************************************/
	public void removeGauge(int index)
	{
		this.gauges_.removeElementAt(index);
	}
}
