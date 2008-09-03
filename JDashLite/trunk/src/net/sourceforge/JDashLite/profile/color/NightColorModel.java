/*********************************************************
 * 
 * @author spowell
 * DefaultColorModel.java
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
import waba.util.Hashtable;


/*********************************************************
 * 
 *
 *********************************************************/
public class NightColorModel implements ColorModel
{
	
	/* The hash of colors for this model */
	private Hashtable colorHash_ = new Hashtable(50);
	
	
	/********************************************************
	 * 
	 *******************************************************/
	protected NightColorModel()
	{
		add(DEFAULT_BACKGROUND,				Color.BLACK);
		add(DEFAULT_BORDER,					GRAY_8);
		add(DEFAULT_TEXT,					GRAY_2);
		
		add(STATUS_PAGE_ACTIVE_BG, 			DK_GRAY);
		add(STATUS_PAGE_INACTIVE_BG,		LT_GRAY);
		add(STATUS_PAGE_ACTIVE_FG,			GRAY_2);
		add(STATUS_PAGE_INACTIVE_FG,		Color.BLACK);
		add(STATUS_RX_COLOR,				Color.RED);
		add(STATUS_TX_COLOR,				Color.GREEN);
		add(STATUS_RX_IDLE_COLOR,			Color.getColor(99,00,22)); /* dark red */
		add(STATUS_TX_IDLE_COLOR,			Color.getColor(33,66,33)); /* dark green */
		
		add(ANALOG_GAUGE_RING,				GRAY_8);
		add(ANALOG_GAUGE_FACE,				Color.BLACK);
		add(ANALOG_GAUGE_TICK_MARK,			GRAY_2);
		add(ANALOG_GAUGE_NEEDLE,			Color.getColor(0xdd,00,00));

		add(LED_GAUGE_RING,					get(ANALOG_GAUGE_RING));
		add(LEG_GAUGE_FACE,					get(ANALOG_GAUGE_FACE));
		add(LED_GAUGE_LED_OFF,				GRAY_7);
		add(LED_GAUGE_LED_ON,				Color.BLUE);
		
		add(LINE_GRAPH_BACKGROUND,			Color.BLACK);
		add(LINE_GRAPH_LINE,				DK_GREEN);
		add(LINE_GRAPH_HIGH_LINE,			DK_RED);
		add(LINE_GRAPH_LOW_LINE,			DK_BLUE);

	}
	
	/*******************************************************
	 * Shorthand convinet method to add colors to the hash.
	 * @param id
	 * @param c
	 ********************************************************/
	private void add(int id, Color c)
	{
		this.colorHash_.put(new IntWrapper(id), c);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.color.ColorModel#get(int)
	 ********************************************************/
	public Color get(int colorId)
	{
		Color c = (Color)this.colorHash_.get(new IntWrapper(colorId));
		if (c == null)
		{
			throw new RuntimeException("Invalid Color ID " + colorId);
		}
		return c;
	}

	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static final class IntWrapper
	{
		public int id_ = -1;
		public IntWrapper(int id) 	{ this.id_ = id; }
		public int hashCode() 		{ return this.id_; }
		public String toString()	{ return this.id_ + ""; }
		public boolean equals(Object obj) { return obj.hashCode() == this.id_; }
		
	}
}
