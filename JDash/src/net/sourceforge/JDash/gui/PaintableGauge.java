/*******************************************************
 * 
 *  @author spowell
 *  PaintableGauge
 *  Aug 30, 2007
 *  $Id:$
 *
Copyright (C) 2006  Shane Powell

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
 ******************************************************/
package net.sourceforge.JDash.gui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/*******************************************************
 * A gauge that implements this interface will have it's
 * updateDisplay method called.  This is because
 * these type of gauges are not placed on a panel like
 * Swing component, but rather are painted to the
 * gaugePanel directly.
 ******************************************************/
public interface PaintableGauge
{


	/*******************************************************
	 * Once the gauge panel is ready to draw itself, each gauge will
	 * get a change to draw itself also.   This method will be
	 * called by the owner panel when ever a display update is
	 * required.  For exmple, the XMLGaugePanel class
	 * will call this method on all of it's gauges for each update.
	 * 
	 * <br>
	 * NOTE: You MUST make this method thread safe.  
	 *  is gets called from different threads.
	 *   The easiest way should be to just
	 * make the methods synchronized. This should work for most cases. But, don't assume
	 * it's a perfect fix.
	 * 
	 * @param panel IN = the gauge panel that holds this gauge.
	 * @param g2 IN - the optional graphics context to draw with.  Not all panels
	 * 		provide a g2 to use. So, be sure you have the correct combination of
	 * 		gauge type to go with the panel type.
	 * @param scalingTransform IN - the tranform to apply that will
	 * adjust to the size of the parent panel. This is the same
	 * transform that would be returned if calling getParent().getScalingTransform()
	 *******************************************************/
	public abstract void paint(AbstractGaugePanel panel, Graphics2D g2, AffineTransform scalingTransform);


	
}
