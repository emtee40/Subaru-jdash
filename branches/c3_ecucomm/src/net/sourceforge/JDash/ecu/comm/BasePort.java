/*******************************************************
 * 
 *  @author Gregory Ng
 *  BaseStream.java
 *  February 28, 2008
 *
Copyright (C) 2006  Gregory Ng

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 *
 * @author greg
 */
public abstract class BasePort {
    protected boolean bIsOpen = false;
	/*******************************************************
	 * Override
	 * @see java.lang.Object#finalize()
	 *******************************************************/
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	
	/*******************************************************
	 * Close any resources associated with this class
	 * @throws IOException
	 *******************************************************/
	abstract public boolean close() throws IOException;
	
	/*******************************************************
	 * Wait for resources to be ready.
	 * @throws IOException
	 *******************************************************/
    abstract public boolean open(int timeout) throws IOException;
    abstract public boolean open()            throws IOException;

	/**
	 * Indicates whether the stream object is open
	 * @return true if open, false otherwise.
	 */
	public boolean isOpen() 
    {
        return bIsOpen;
    }


    /**
	 * Return an InputStream object for the client to to write to the stream.
	 * @return InputStream object if the BaseStream is open.  
	 *   Returns null otherwise.
	 * @throws java.io.IOException
	 */
	abstract public InputStream  getInputStream();
	abstract public OutputStream getOutputStream();
    
    
    
    
	
}
