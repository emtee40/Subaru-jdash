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
public class DefaultColorModel implements ColorModel
{
	
	protected static final Color LT_GRAY 	= Color.getColor(0xcc, 0xcc, 0xcc);
	protected static final Color GRAY 		= Color.getColor(0x88, 0x88, 0x88);
	protected static final Color DK_GRAY 	= Color.getColor(0x55, 0x55, 0x55);

	/* The hash of colors for this model */
	private Hashtable colorHash_ = new Hashtable(50);
	
	
	/********************************************************
	 * 
	 *******************************************************/
	protected DefaultColorModel()
	{
		add(DEFAULT_BACKGROUND,				Color.WHITE);
		add(DEFAULT_BORDER,					Color.BLACK);
		add(DEFAULT_TEXT,					Color.BLACK);
		
		add(STATUS_PAGE_ACTIVE_BG, 			DK_GRAY);
		add(STATUS_PAGE_INACTIVE_BG,		LT_GRAY);
		add(STATUS_PAGE_ACTIVE_FG,			Color.WHITE);
		add(STATUS_PAGE_INACTIVE_FG,		Color.BLACK);
		add(STATUS_RX_COLOR,				Color.RED);
		add(STATUS_TX_COLOR,				Color.GREEN);
		add(STATUS_RX_IDLE_COLOR,			Color.getColor(99,00,22)); /* dark red */
		add(STATUS_TX_IDLE_COLOR,			Color.getColor(33,66,33)); /* dark green */
		
		add(ANALOG_GAUGE_RING,				DK_GRAY);
		add(ANALOG_GAUGE_FACE,				Color.BLACK);
		add(ANALOG_GAUGE_TICK_MARK,			Color.WHITE);
		add(ANALOG_GAUGE_NEEDLE,			Color.getColor(0xFF,00,00));

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