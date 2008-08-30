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
	

	public static final String PROP_I_PRECISION			= "precision";

	/********************************************************
	 * 
	 *******************************************************/
	public DigitalGauge()
	{
	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ColorModel cm, boolean redrawAll)
	{

		/* Blank the gauge first */
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.DEFAULT_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);
		
		g.setForeColor(cm.get(ColorModel.DEFAULT_TEXT));
		
		String label = getProperty(PROP_STR_LABEL);
		Font f = null;
		if (label != null)
		{
			f = ProfileRenderer.findFontBestFitWidth(r.width - 6, label, false);
			g.setFont(f);
			g.drawText(label, r.x + ((r.width - f.fm.getTextWidth(label)) / 2), r.y + r.height - f.fm.height - 1);
		}
		
		/* Draw the current value */
		//f = ProfileRenderer.findFontBestFitHeight((int)(r.height - LABEL_HEIGHT), true);
		String val = Convert.toString(getECUParameter().getValue(), getIntProperty(PROP_I_PRECISION, 0));
		f = ProfileRenderer.findFontBestFitWidth(r.width - 10, val, true);
		g.setFont(f);
		g.drawText(val, r.x + ((r.width - f.fm.getTextWidth(val)) / 2), r.y + ((r.height - f.fm.height) / 2));
			
	}
}
