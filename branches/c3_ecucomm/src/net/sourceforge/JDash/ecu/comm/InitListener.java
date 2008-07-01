/*******************************************************
 * 
 *  @author spowell
 *  InitListener.java
 *  Dec 14, 2006
 *  $ID:$
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
package net.sourceforge.JDash.ecu.comm;

/*******************************************************
 * When the init method on a monitor is called, an instance
 * of this class can be passed to it. This listener will
 * receive init event messages as the init runs.  This
 * is really just useful for the splash screen.
 ******************************************************/
public abstract class InitListener
{
	
	public String prefix_ = null;
	
	
	/*******************************************************
	 * @param prefix
	 ******************************************************/
	public InitListener(String prefix)
	{
		this.prefix_ = prefix;
	}
	
	/*******************************************************
	 * @return 
	 *******************************************************/
	public String getPrefix()
	{
		return this.prefix_;
	}
	

	/********************************************************
	 * Inform the listener of an update in the init status.
	 * Think of the parameters as this, the message is displayed
	 * to the user, and the step is #n of max.
	 * 
	 * @param message IN - a displayable string message.
	 * @param step IN - the step index between 0 and max
	 * @param max IN - the maximum number of steps that will
	 * ever be send to the update string.
	 *******************************************************/
	public abstract void update(String message, int step, int max);
	
}
