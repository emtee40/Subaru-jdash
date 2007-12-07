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

/*******************************************************
 * Skin events are messages passed to the engine from the skin usually
 * when the user performs some action, like clicking a button
 ******************************************************/
public interface SkinEventListener
{

	
	/********************************************************
	 * When an aciton is performed, each listener will
	 * get a chance to respond to the action.  It is the
	 * responsibility of the listener to know what SkinEvent
	 * messages to respond to, and what ones to ignore.
	 * 
	 * @param e
	 *******************************************************/
	public void actionPerformed(SkinEvent e);
	
}

