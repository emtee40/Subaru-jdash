/*******************************************************
 * 
 *  @author spowell
 *  MonitorEventListener
 *  Aug 8, 2006
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
package net.sourceforge.JDash.ecu.comm;

import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * This interface is defines events fired by a monitor during
 * it's processing life.  Remember.. A monitor is running
 * in it's own thread, so you had better make sure this listener
 * is thread safe.
 *****************************************************/
public interface MonitorEventListener
{

	/********************************************************
	 * This method is fired when a monitor is about to start it's
	 * run on the list of parameters.
	 *******************************************************/
	public void processingStarted();
	
	/*******************************************************
	 * this method is called as soon as a parameter is processed.
	 * @param p
	 *******************************************************/
	public void processedParameter(Parameter p);
	
	/*******************************************************
	 * This method is fired when a monitor is finished processing
	 * it's list of parameters.
	 *******************************************************/
	public void processingFinished();
	
	
	/*******************************************************
	 * don't need all themethod of the event listener.. Then
	 * override this adapter, and use what you want.
	 ******************************************************/
	public static class MonitorEventAdapter implements MonitorEventListener
	{
	
		public void processedParameter(Parameter p)
		{
		}
		
		public void processingFinished()
		{
		}
		
		public void processingStarted()
		{
		}
		
	}
	
}
