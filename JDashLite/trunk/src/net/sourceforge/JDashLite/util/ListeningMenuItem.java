/*********************************************************
 * 
 * @author spowell
 * ListeningMenuItem.java
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

import waba.ui.MenuItem;
import waba.util.Vector;

/*********************************************************
 * This class, in conjunction with the MenuUtil class
 * provides a similar listener type mode to Swing. No, it's
 * not the same, but it's similar. 
 *
 *********************************************************/
public class ListeningMenuItem extends MenuItem
{
	
	private MenuActionListener actionListener_ = null;

	/*******************************************************
	 * 
	 *******************************************************/
	public ListeningMenuItem(String caption)
	{
		super(caption);
	}
	
	
	/*******************************************************
	 * 
	 *******************************************************/
	public ListeningMenuItem(String caption, boolean checked)
	{
		super(caption, checked);
	}
	
	/*******************************************************
	 * @param l
	 ********************************************************/
	public void setActionListener(MenuActionListener l)
	{
		this.actionListener_ = l;
	}

	/********************************************************
	 * 
	 ********************************************************/
	public void fireAction()
	{
		if (this.actionListener_ != null)
		{
			this.actionListener_.actionPerformed(this.actionListener_.listenerReferenceObject_);
		}
	}
	
	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	public abstract static class MenuActionListener
	{
		public Object listenerReferenceObject_ = null;
		
		public MenuActionListener(Object ref)
		{
			this.listenerReferenceObject_ = ref;
		}
		
		public MenuActionListener()
		{
			this(null);
		}
		
		public abstract void actionPerformed(Object o);
	}
}
