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
public class LineGraphGauge extends ProfileGauge
{

	/********************************************************
	 * 
	 *******************************************************/
	public LineGraphGauge()
	{
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ECUParameter p, ColorModel cm)
	{
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.DEFAULT_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);

		g.setForeColor(cm.get(ColorModel.DEFAULT_TEXT));
		g.drawText(p.getName(), r.x + 2, r.y + 2);
	
	}
	
}
