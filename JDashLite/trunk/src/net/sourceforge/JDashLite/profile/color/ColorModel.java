/*********************************************************
 * 
 * @author spowell
 * ColorModel.java
 * Aug 14, 2008
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

package net.sourceforge.JDashLite.profile.color;

import waba.fx.Color;

/*********************************************************
 * 
 *
 *********************************************************/
public interface ColorModel
{

	
	public static final int STATUS_PAGE_ACTIVE_BG 			= 100;
	public static final int STATUS_PAGE_INACTIVE_BG 		= 101;
	public static final int STATUS_PAGE_ACTIVE_FG 			= 102;
	public static final int STATUS_PAGE_INACTIVE_FG 		= 103;
	public static final int STATUS_RX_COLOR 				= 104;
	public static final int STATUS_TX_COLOR 				= 105;
	public static final int STATUS_RX_IDLE_COLOR 			= 106;
	public static final int STATUS_TX_IDLE_COLOR 			= 107;

	public static final int DEFAULT_BACKGROUND				= 200;
	public static final int DEFAULT_BORDER					= 201;
	public static final int DEFAULT_TEXT					= 202;
	
	public static final int ANALOG_GAUGE_RING				= 300;
	public static final int ANALOG_GAUGE_FACE				= 301;
	public static final int ANALOG_GAUGE_TICK_MARK			= 302;
	public static final int ANALOG_GAUGE_NEEDLE				= 303;


	public static final ColorModel DEFAULT_COLOR_MODEL = new DefaultColorModel();

	
	/*  Get the desired color from the ID */
	public Color get(int colorId);
	
}
