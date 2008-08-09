/*********************************************************
 * 
 * @author spowell
 * ELMProtocol.java
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

import net.sourceforge.JDashLite.profile.Profile;
import waba.io.SerialPort;
import waba.util.Vector;

/*********************************************************
 * 
 *
 *********************************************************/

public abstract class AbstractProtocol implements ProtocolHandler
{
	
//	private boolean isRunning_ = false;
	private int serialPortId_ = SerialPort.DEFAULT;
	private SerialPort serialPort_ = null;
	private Profile profile_  = null;
	private Vector eventListeners_ = new Vector(1);
	
	/********************************************************
	 * 
	 *******************************************************/
	public AbstractProtocol()
	{
		
	}
	
	/*******************************************************
	 * Given the list of parameters supported, find the one
	 * by it's name.  This is just a simple utility method 
	 * to search the array of params from getSupportedParameters()
	 * 
	 * @param name
	 * @return
	 ********************************************************/
	public ECUParameter getParameter(String name)
	{
		for (int index = 0; index < getSupportedParameters().length; index++)
		{
			if (name.equals(getSupportedParameters()[index].getName()))
			{
				return getSupportedParameters()[index];
			}
		}
		return null;
	}

	
	/*********************************************************
	 * This method will automatcially close the serial port, if it
	 * has been set, and remove all event listeners.
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#disconnect()
	 ********************************************************/
	public boolean disconnect()
	{
			
		if (getSerialPort() != null)
		{
			getSerialPort().close();
		}
		
		this.eventListeners_.removeAllElements();
		
		return true;
	}

	
	
	/********************************************************
	 * @return the serialPort
	 ********************************************************/
	public SerialPort getSerialPort()
	{
		return this.serialPort_;
	}
	
	/********************************************************
	 * @return the serialPortId
	 ********************************************************/
	public int getSerialPortId()
	{
		return this.serialPortId_;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#setSerialPortId(int)
	 ********************************************************/
	public void setSerialPortId(int port)
	{
		this.serialPortId_ = port;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#setSerialPort(waba.io.SerialPort)
	 ********************************************************/
	public void setSerialPort(SerialPort serialPort)
	{
		this.serialPort_ = serialPort;
	}
	
	/********************************************************
	 * @return the profile
	 ********************************************************/
	public Profile getProfile()
	{
		return this.profile_;
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#setProfile(net.sourceforge.JDashLite.profile.Profile)
	 ********************************************************/
	public void setProfile(Profile profile)
	{
		this.profile_ = profile;
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#addProtocolEventListener(net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener)
	 ********************************************************/
	public void addProtocolEventListener(ProtocolEventListener l)
	{
		this.eventListeners_.addElement(l);
	}

	/*******************************************************
	 * @return
	 ********************************************************/
	public int getEventListenerCount()
	{
		return this.eventListeners_.size();
	}
	
	/*******************************************************
	 * @param index
	 * @return
	 ********************************************************/
	public ProtocolEventListener getEventListener(int index)
	{
		return (ProtocolEventListener)this.eventListeners_.items[index];
	}
	

	/********************************************************
	 * 
	 ********************************************************/
	public void fireStartedEvent()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).protocolStarted();
		}
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	public void fireStoppedEvent()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).protocolStopped();
		}	
	}
	
	/*******************************************************
	 * @param count
	 ********************************************************/
	public void fireBeginParameterBatchEvent(int count)
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).beginParameterBatch(count);
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	public void fireEndParameterBatchEvent()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).endParameterBatch();
		}
	}

	/*******************************************************
	 * 
	 ********************************************************/
	public void fireParemeterFetched()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).parameterFetched();
		}
	}

	
	
}

