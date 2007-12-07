/*******************************************************
 * 
 *  @author spowell
 *  SwingComponent
 *  Dec 4, 2007
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
package net.sourceforge.JDash.gui;

import java.awt.Component;


/*******************************************************
 * A class implementing this interface will provide a method
 * to return the SwingComponent it represents.
 ******************************************************/
public interface SwingComponent
{
	/********************************************************
	 * Get the swing component this object represents.
	 * @return
	 *******************************************************/
	public Component getComponent();
}
