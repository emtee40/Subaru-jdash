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
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Rect;

/*********************************************************
 * 
 *
 *********************************************************/
public class DigitalGauge extends AbstractGauge
{

	/* The name of the ECU Parameter whos value wil be displayed */
	private String parameterName_ = null;
	
	/* The default number of decimal places to show */
	private int decimalPlaces_ = 2;
	
	
	/********************************************************
	 * 
	 *******************************************************/
	public DigitalGauge()
	{
	}
	
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
		if (parameterName == null)
		{
			throw new RuntimeException("Can't create a digital gauge with a null parameter name");
		}
		this.parameterName_ = parameterName;
	}
	
	
	/********************************************************
	 * @return the decimalPlaces
	 ********************************************************/
	public int getDecimalPlaces()
	{
		return this.decimalPlaces_;
	}
	
	
	/********************************************************
	 * @param decimalPlaces the decimalPlaces to set
	 ********************************************************/
	public void setDecimalPlaces(int decimalPlaces)
	{
		this.decimalPlaces_ = decimalPlaces;
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r) throws Exception
	{
		Font f = ProfileRenderer.findFontBestFitHeight(r.height);
		g.setFont(f);
		g.drawText(this.getParameterName(), r.x, r.y);
	}
}
