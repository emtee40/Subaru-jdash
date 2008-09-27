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
import net.sourceforge.JDashLite.util.AffineTransform;
import waba.fx.Coord;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Image;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 *  Zero degrees is straight right.  Rotation is right to left
 *
 *********************************************************/
/*********************************************************
 * 
 *
 *********************************************************/
public class SweepAnalogGauge extends AnalogGauge
{

	
	/********************************************************
	 * 
	 *******************************************************/
	public SweepAnalogGauge()
	{

		this.minimumAngle_ = 90;
		this.maximumAngle_ = 180;

	}

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ColorModel cm, boolean forceRepaint)
	{

			Coord pivot = new Coord(r.width - 10, r.height - 10); 
			super.render(g, r, cm, forceRepaint, pivot);
		
//		/* Generate the static image */
//		if (r.equals(this.currentRect_) == false || cm != this.currentColorModel_)
//		{
//			this.staticContent_ = new Image(r.width, r.height);
//			generateStaticImage(cm, (int)(Math.min(r.width, r.height) * 0.04));
//		}
//		
//		/* Now, the dynamic image */
//		if (this.previousValue_ != getECUParameter().getValue() || forceRepaint || r.equals(this.currentRect_) == false || cm != this.currentColorModel_)
//		{
//			g.drawImage(this.staticContent_, r.x, r.y);
//			renderDynamic(g, r, cm);
//		}
//
//		this.currentRect_ = r;
//		this.currentColorModel_ = cm;

	}
	
	
}
