/*******************************************************
 * 
 *  @author spowell
 *  SkinEvents.java
 *  Aug 27, 2007
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
package net.sourceforge.JDash.skin;

import java.util.List;

/*******************************************************
 * Skin events are messages passed to the engine from the skin usually
 * when the user performs some action, like clicking a button.
 * A SkinEvent object is mutable, like a string object.  Values
 * cannot, and must not be modified.
 ******************************************************/
public class SkinEvent
{

	private String destination_ = null;
	private String action_ = null;
	
	
	/******************************************************
	 * @param destination
	 * @param action
	 *****************************************************/
	public SkinEvent(String destination, String action)
	{
		this.destination_ = destination;
		this.action_ = action;
	}
	
	/********************************************************
	 * @return
	 ******************************************************/
	public String getDestination()
	{
		return this.destination_;
	}
	
	/*******************************************************
	 * Override the destination value.
	 * @param id
	 *******************************************************/
	public void setDestination(String id)
	{
		this.destination_ = id;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public String getAction()
	{
		return this.action_;
	}

	
}

