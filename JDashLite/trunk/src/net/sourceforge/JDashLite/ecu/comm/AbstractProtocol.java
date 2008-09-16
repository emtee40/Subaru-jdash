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

import waba.io.SerialPort;
import waba.sys.Vm;
import waba.util.Vector;

/*********************************************************
 * 
 *
 *********************************************************/

public abstract class AbstractProtocol implements ProtocolHandler
{
	
	private int serialPortId_ = SerialPort.DEFAULT;
	private SerialPort serialPort_ = null;
	private Vector eventListeners_ = new Vector(1);
	
	
	/* The current ELM stage */
	private int currentStage_ = -1;
	
	/* The current TX/RX mode */
	private int currentMode_ = -1;
	

	/* The operation timer */
	private int opTimeLimit_ = 0;
	private int opTimeStart_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public AbstractProtocol()
	{
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.Cleanable#clean()
	 ********************************************************/
	public void clean()
	{
		for (int index = 0; index < getSupportedParameters().length; index++)
		{
			ECUParameter p = getSupportedParameters()[index];
			p.clean();
		}
		
	}

	
	/********************************************************
	 * @param stage
	 * @param mode
	 ********************************************************/
	protected void setStageAndMode(int stage, int mode)
	{
		setStage(stage);
		setMode(mode);
	}
	
	/*******************************************************
	 * @param stage
	 ********************************************************/
	protected void setStage(int stage)
	{
		this.currentStage_ = stage;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	protected int getStage()
	{
		return this.currentStage_;
	}
	
	/*******************************************************
	 * @param mode
	 ********************************************************/
	protected void setMode(int mode)
	{
		this.currentMode_ = mode;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	protected int getMode()
	{
		return this.currentMode_;
	}
	
	/*******************************************************
	 * This method will reset the current time holder, and 
	 * keep track of the t value passed in.  Calls to
	 * isOperationTimerExpired() will compare if the time
	 * elapsed is over the t value.
	 * @param t
	 ********************************************************/
	protected void setOperationTimer(int timeLimit)
	{
		this.opTimeStart_ = Vm.getTimeStamp();
		this.opTimeLimit_ = timeLimit;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	protected boolean isOperationTimerExpired()
	{
		return (this.opTimeStart_ + this.opTimeLimit_) < Vm.getTimeStamp();
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
	
	
	/*******************************************************
	 * A utility method to get the index of the next enabled
	 * parameter.
	 * @return
	 ********************************************************/
	public int getNextEnabledParamIndex(int currentIndex)
	{
		int nextIndex = currentIndex;
		
		do
		{
			nextIndex++;
			
			/* Back to 0 */
			if (nextIndex >= getSupportedParameters().length)
			{
				nextIndex = 0;
			}
			
			/* Full circle witout a match? */
			if (nextIndex == currentIndex)
			{
				return currentIndex;
			}
			
		}
		while(getSupportedParameters()[nextIndex].isEnabled() == false);
	
		return nextIndex;
		
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
	 * 
	 ********************************************************/
	public void fireInitStartedEvent()
	{
		
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).initStarted();
		}
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	public void fireInitFinishedEvent()
	{
		
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).initFinished();
		}
	}
	
	/*******************************************************
	 * @param msg
	 ********************************************************/
	public void fireInitStatusEvent(String msg)
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).initStatus(msg);
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
	public void fireParemeterFetchedEvent(ECUParameter p)
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).parameterFetched(p);
		}
	}

	
	/*******************************************************
	 * 
	 ********************************************************/
	public void fireCommTXEvent()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).commTX();
		}
	}
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	public void fireCommRXEvent()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).commRX();
		}
	}
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	public void fireCommReady()
	{
		for (int index = 0; index < getEventListenerCount(); index++)
		{
			getEventListener(index).commReady();
		}
	}
	
	
}

