/*********************************************************
 * 
 * @author spowell
 * MenuUtil.java
 * Jul 21, 2008
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

package net.sourceforge.JDashLite.util;

import waba.ui.Event;
import waba.ui.MenuItem;

/*********************************************************
 *  This class contains some static help methods for use with
 *  SuperWaba menus.
 *
 *********************************************************/
public class MenuUtil
{

	/********************************************************
	 * Given the 2x array of MenuItems, find the label for the menu item
	 * that corrisponds to the wabaMenuId value.  eg.. 205 is the 3rd menu, 5th item down.
	 * 3 is the first menu, 3rd item down.
	 * 
	 * @param menuItems IN - the 2x array of menu items used to create the menu.
	 * @param wabaMenuId IN - the waba based menu id that came from a Control event.
	 * @return
	 * @see Event
	 ********************************************************/
	public static String getLabel(MenuItem[][] menuItems, int wabaMenuId)
	{
		String label = null;
		int col = 0;
		int row = 1;
		
		if (wabaMenuId > 99)
		{
			col = (wabaMenuId / 100);
		}
		
		row = wabaMenuId - (col * 100);
		
		label = menuItems[col][row].caption;
		
		return label;
	}
	
	
	/********************************************************
	 * When an menu event with a menu ID is received by the Window
	 * class, this method can be used to dispatch the MenuAction
	 * listeners within menu item arrays.
	 * @param menuItems
	 * @param wabaMenuId
	 ********************************************************/
	public static void dispatchMenuAction(ListeningMenuItem[][] menuItems, int wabaMenuId)
	{
		if (wabaMenuId == -1)
		{
			return;
		}
		
		int col = 0;
		int row = 1;
		
		if (wabaMenuId > 99)
		{
			col = (wabaMenuId / 100);
		}
		
		row = wabaMenuId - (col * 100);
		
		menuItems[col][row].fireAction();
		
	}
}
