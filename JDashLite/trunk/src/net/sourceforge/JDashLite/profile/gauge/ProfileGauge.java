/*********************************************************
 * 
 * @author spowell
 * ProfileGauge.java
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

import net.sourceforge.JDashLite.profile.RenderableProfileComponent;
import waba.fx.Graphics;
import waba.fx.Rect;

/*********************************************************
 * 
 *
 *********************************************************/
public abstract class ProfileGauge implements RenderableProfileComponent
{

	private String parameterName_ = null;
	private double widthPercent_ = -1;
	
	/********************************************************
	 * @return the parameterName
	 ********************************************************/
	public String getParameterName()
	{
		return this.parameterName_;
	}
	
	
	/********************************************************
	 * @param parameterName the parameterName to set
	 ********************************************************/
	public void setParameterName(String parameterName)
	{
		this.parameterName_ = parameterName;
	}
	
	
	/********************************************************
	 * @return the widthPercent
	 ********************************************************/
	public double getWidthPercent()
	{
		return this.widthPercent_;
	}
	
	
	/********************************************************
	 * @param widthPercent the widthPercent to set
	 ********************************************************/
	public void setWidthPercent(double widthPercent)
	{
		this.widthPercent_ = widthPercent;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public abstract void render(Graphics g, Rect r) throws Exception;
}
