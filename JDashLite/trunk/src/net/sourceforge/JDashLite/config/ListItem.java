/*********************************************************
 * 
 * @author spowell
 * ListItem.java
 * Jul 24, 2008
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

package net.sourceforge.JDashLite.config;

/*********************************************************
 * A list item is a simple id and string value pair for use
 * in things like combo boxes.
 *
 *********************************************************/
public class ListItem
{
	private int id_ = 0;
	private String label_ = null;
	
	/*******************************************************
	 * @param id
	 * @param label
	 *******************************************************/
	public ListItem(int id, String label)
	{
		this.id_ = id;
		this.label_ = label;
	}

	/********************************************************
	 * @return the id
	 ********************************************************/
	public int getId()
	{
		return this.id_;
	}
	
	/********************************************************
	 * @return the label
	 ********************************************************/
	public String getLabel()
	{
		return this.label_;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 ********************************************************/
	public String toString()
	{
		return getLabel();
	}
	
	
	/********************************************************
	 * @param items
	 * @param id
	 * @return
	 ********************************************************/
	public static ListItem findItem(ListItem[] items, int id)
	{
		int index = findItemIndex(items, id);
		
		if (index > -1)
		{
			return items[index];
		}
		
		return null;
	}
	
	/********************************************************
	 * find the item with the given ID and return it's index in the array.
	 * @return  the item index, -1 if not found.
	 ********************************************************/
	public static int findItemIndex(ListItem[] items, int id)
	{
		for (int index = 0; index < items.length; index++)
		{
			if (items[index].getId() == id)
			{
				return index;
			}
		}
		return -1;
	}
	
}
