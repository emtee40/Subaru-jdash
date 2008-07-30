/*********************************************************
 * 
 * @author spowell
 * ProtocolHandlerThread.java
 * Jul 29, 2008
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
 * This is a simple thread wrapper around our ProtocolHandler
 * interface
 *
 *********************************************************/

public class ProtocolHandlerThread implements waba.sys.Thread
{
	
	private boolean isEnabled_ = false;
	private ProtocolHandler pHandler_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProtocolHandlerThread()
	{
	}
	
	/********************************************************
	 * @param h
	 ********************************************************/
	public void setProtocolHandler(ProtocolHandler h)
	{
		this.pHandler_ = h;
	}

	/********************************************************
	 * @param enable
	 ********************************************************/
	public void setEnabled(boolean enable)
	{
		this.isEnabled_ = enable;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 ********************************************************/
	public void run()
	{
		if ((pHandler_ != null) && (this.isEnabled_))
		{
			this.pHandler_.doTask();
		}
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.sys.Thread#started()
	 ********************************************************/
	public void started()
	{
		// TODO Auto-generated method stub
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.sys.Thread#stopped()
	 ********************************************************/
	public void stopped()
	{
		// TODO Auto-generated method stub
		
	}
	
}
