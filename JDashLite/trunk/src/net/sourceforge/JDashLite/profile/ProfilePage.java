/*********************************************************
 * 
 * @author spowell
 * ProfilePage.java
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

package net.sourceforge.JDashLite.profile;

import waba.util.Vector;

/*********************************************************
 * 
 *
 *********************************************************/
public class ProfilePage
{
	
	private waba.util.Vector rows_ = new Vector(3);
	
	
	/********************************************************
	 * @param index
	 * @return
	 ********************************************************/
	public ProfileRow getRow(int index)
	{
		return (ProfileRow)this.rows_.items[index];
	}
	
	
	/*******************************************************
	 * @param row
	 ********************************************************/
	public void addRow(ProfileRow row)
	{
		this.rows_.addElement(row);
	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int getRowCount()
	{
		return this.rows_.size();
	}
	
	
	/*******************************************************
	 * @param index
	 ********************************************************/
	public void removeRow(int index)
	{
		this.rows_.removeElementAt(index);
	}
	
}
