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
 * fires off events to it's added listeners.  
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
	
	
	/*******************************************************
	 * Called when the protocol starts it's init phase.
	 ********************************************************/
	public void initStarted();
	
	/********************************************************
	 * Called when the protocl is finished it's init phase.
	 ********************************************************/
	public void initFinished();
	
	/*******************************************************
	 * When a protocol is going through it's init phase, it will
	 * fire off a sequence of message through this method.
	 * Any message sent through here will be rendered to the
	 * user in the middle of the screen.  The message will
	 * remain until this method is called wit a null value.
	 * This method can also be used any time during the normal
	 * procesisng to simply display a message.  Just remember
	 * to turn it off when your done!!
	 * 
	 * @param statusMessage
	 ********************************************************/
	public void initStatus(String statusMessage);
	
	/********************************************************
	 * Called when the a set of parameters to be fetched has begun.
	 * Some protocols patch a series of parameters at once, some
	 * do them one at a time.
	 * @param count IN - the number of parameters about to be fetched.
	 ********************************************************/
	public void beginParameterBatch(int count);
	
	
	/*******************************************************
	 * Called after each individual parameter is successfully fetched
	 ********************************************************/
	public void parameterFetched(ECUParameter p);
	

	/********************************************************
	 * Called after the batch of parameters has been fetched.
	 *******************************************************/
	public void endParameterBatch();
	

	/*******************************************************
	 * Called as the protocol enters a comms TX phase.
	 ********************************************************/
	public void commTX();
	
	
	/*******************************************************
	 * Called as the protocol enters a comms RX phase.
	 ********************************************************/
	public void commRX();
	
	
	/*******************************************************
	 * Called as the protocll enters a comms ready, or non TX/RX phase.
	 ********************************************************/
	public void commReady();
	

	/*********************************************************
	 * 
	 *
	 *********************************************************/
	public static class ProtocolEventAdapter implements ProtocolEventListener
	{
		
		public void protocolStarted() {};
		public void protocolStopped() {};
		public void initStarted() {};
		public void initFinished() {};
		public void initStatus(String statusMessage) {};
		public void beginParameterBatch(int count) {};
		public void parameterFetched(ECUParameter p) {};
		public void endParameterBatch() {};
		public void commTX() {};
		public void commRX() {};
		public void commReady() {};
	}
	
	
}
