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
import net.sourceforge.JDashLite.profile.color.ColorModel;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 * 
 *
 *********************************************************/
public class DigitalGauge extends ProfileGauge
{
	
	private double LABEL_HEIGHT = 0.2;

	/* The default number of decimal places to show */
	private int decimalPrecision_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public DigitalGauge()
	{
	}
	
	
	/********************************************************
	 * @return the decimalPlaces
	 ********************************************************/
	public int getDecimalPrecision()
	{
		return this.decimalPrecision_;
	}
	
	
	/********************************************************
	 * @param decimalPlaces the decimalPlaces to set
	 ********************************************************/
	public void setDecimalPrecision(int decimalPrecision)
	{
		this.decimalPrecision_ = decimalPrecision;
	}

	

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ECUParameter p, ColorModel cm)
	{

		/* Blank the gauge first */
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.DEFAULT_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);
		
		g.setForeColor(cm.get(ColorModel.DEFAULT_TEXT));
		
		Font f = null;
		if (getLabel() != null)
		{
			f = ProfileRenderer.findFontBestFitHeight((int)(r.height * LABEL_HEIGHT));
			g.setFont(f);
			g.drawText(getLabel(), r.x + ((r.width - f.fm.getTextWidth(getLabel())) / 2), r.y + r.height - f.fm.height - 1);
		}
		
		/* Draw the current value */
		f = ProfileRenderer.findFontBestFitHeight((int)(r.height - LABEL_HEIGHT));
		g.setFont(f);
		String val = Convert.toString(p.getValue(), getDecimalPrecision());
		g.drawText(val, r.x + ((r.width - f.fm.getTextWidth(val)) / 2), r.y + ((r.height - f.fm.height) / 2));
			
	}
}
