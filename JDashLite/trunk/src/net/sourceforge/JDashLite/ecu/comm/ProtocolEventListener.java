/*********************************************************
 * 
 * @author spowell
 * ProtocolEventListener.java
 * Jul 26, 2008
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

package net.sourceforge.JDashLite.ecu.comm;

/*********************************************************
 * A protocol handler, during it's cycle of tasks, 
 * fires of events to it's added listeners.
 *
 *********************************************************/
public interface ProtocolEventListener
{

	/********************************************************
	 * Called when the protocol handler is started.
	 ********************************************************/
	public void protocolStarted();
	
	/********************************************************
	 * Called when the protocol handler is stopped.
	 ********************************************************/
	public void protocolStopped();
	
	/********************************************************
	 * Called when the a set of parameters to be fetched has begun.
	 * @param count IN - the number of parameters about to be fetched.
	 ********************************************************/
	public void beginParameterBatch(int count);
	
	/*******************************************************
	 * Called after each individual parameter is fetched
	 ********************************************************/
	public void parameterFetched();
	
	/********************************************************
	 * Called after the batch of parameters has been fetched.
	 *******************************************************/
	public void endParameterBatch();
	

	public static class ProtocolEventAdapter implements ProtocolEventListener
	{
		public void protocolStarted()  {}
		public void protocolStopped()  {}
		public void beginParameterBatch(int count)  {}
		public void parameterFetched()  {}
		public void endParameterBatch() {}
			
	}
	
}
