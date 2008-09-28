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
	public static final int ANALOG_GAUGE_HIGH_NEEDLE		= 304;
	public static final int ANALOG_GAUGE_LOW_NEEDLE			= 305;
	
	public static final int LED_GAUGE_RING					= 400;
	public static final int LEG_GAUGE_FACE					= 401;
	public static final int LED_GAUGE_LED_OFF				= 402;
	public static final int LED_GAUGE_LED_ON				= 403;
	
	public static final int LINE_GRAPH_LINE					= 500;
	public static final int LINE_GRAPH_BACKGROUND			= 501;
	public static final int LINE_GRAPH_HIGH_LINE			= 502;
	public static final int LINE_GRAPH_LOW_LINE				= 503;
	
	
	public static final Color LT_GRAY 	= Color.getColor(0xcc, 0xcc, 0xcc);
	public static final Color GRAY 		= Color.getColor(0x88, 0x88, 0x88);
	public static final Color DK_GRAY 	= Color.getColor(0x55, 0x55, 0x55);
	
	

	
	/* http://www.w3schools.com/html/html_colorsfull.asp */
	public static final Color GRAY_1		= Color.getColor(0xf0, 0xf0, 0xf0);
	public static final Color GRAY_2		= Color.getColor(0xe0, 0xe0, 0xe0);
	public static final Color GRAY_3		= Color.getColor(0xd0, 0xd0, 0xd0);
	public static final Color GRAY_4		= Color.getColor(0xc0, 0xc0, 0xc0);
	public static final Color GRAY_5		= Color.getColor(0xb0, 0xb0, 0xb0);
	public static final Color GRAY_6		= Color.getColor(0xa0, 0xa0, 0xa0);
	public static final Color GRAY_7		= Color.getColor(0x90, 0x90, 0x90);
	public static final Color GRAY_8		= Color.getColor(0x80, 0x80, 0x80);
	public static final Color GRAY_9		= Color.getColor(0x70, 0x70, 0x70);
	public static final Color GRAY_10		= Color.getColor(0x60, 0x60, 0x60);
	public static final Color GRAY_11		= Color.getColor(0x50, 0x50, 0x50);
	public static final Color GRAY_12		= Color.getColor(0x40, 0x40, 0x40);
	public static final Color GRAY_13		= Color.getColor(0x30, 0x30, 0x30);
	public static final Color GRAY_14		= Color.getColor(0x20, 0x20, 0x20);
	public static final Color GRAY_15		= Color.getColor(0x10, 0x10, 0x10);


	public static final Color DK_RED		= Color.getColor(0x98, 0x00, 0x00);
	public static final Color DK_GREEN		= Color.getColor(0x00, 0x98, 0x00);
	public static final Color DK_BLUE		= Color.getColor(0x00, 0x00, 0x98);
	

	//public static final ColorModel DEFAULT_COLOR_MODEL = new DefaultColorModel();
	public static final ColorModel DEFAULT_MODEL 	= new DefaultColorModel();
	public static final ColorModel NIGHT_MODEL 		= new NightColorModel();
	public static final ColorModel BLUE_MODEL		= new BlueColorModel();


	public static final ColorModel[] ALL_MODELS = {DEFAULT_MODEL, NIGHT_MODEL, BLUE_MODEL};
	
	/*******************************************************
	 * Get the desired color from the ID 
	 * @param colorId
	 * @return
	 ********************************************************/
	public Color get(int colorId);
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public String getName();
	
	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	public static final class IntWrapper
	{
		public int id_ = -1;
		public IntWrapper(int id) 	{ this.id_ = id; }
		public int hashCode() 		{ return this.id_; }
		public String toString()	{ return this.id_ + ""; }
		public boolean equals(Object obj) { return obj.hashCode() == this.id_; }
		
	}
	
}
