/*******************************************************
 * 
 *  @author spowell
 *  SwingComponentGauge
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

import java.awt.Component;
import java.util.List;

/******************************************************
 * A gauge that implements this interface is treated as
 * a SWING/AWT componet, and placed on the gauge panel
 * as such.  The PaintableGauge is the other type
 * of gauge.
 ******************************************************/
public interface SwingComponentGauge
{

	/********************************************************
	 * Get the instance of the gauge components that is to
	 * be placed into the panel
	 * @return
	 *******************************************************/
	public List<Component> getGaugeComponents();
	
	
	/********************************************************
	 * This method will be called when the components display should
	 * be updated.
	 *******************************************************/
	public void updateDisplay();
	
}
