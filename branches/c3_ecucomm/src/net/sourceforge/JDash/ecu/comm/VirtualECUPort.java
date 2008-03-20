/*******************************************************
 * 
 *  @author Gregory Ng
 *  VirtualECUPort.java
 *  February 28, 2008
 *
Copyright (C) 2008  Gregory Ng

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

//import net.sourceforge.JDash.ecu.comm.BaseStream
import java.io.*;
/**
 * A BasePort-derived object to which you can attach an VirtualECU class. 
 * 
 * 
 * 
 * This class implements an input-output pipe to communicate between the JDash
 * and an ECU emulator. Internally, we have:
 * 
 *            pipeComm2ECUInput ==> pipeComm2ECUoutput
 *          /                                          \
 * [JDash protocol handler]  <====>              [VirtualECU]
 *          \                                          /
 *            pipeECU2CommOutput <-- pipeECU2CommInput
 * -->
 * 
 * 
 * Usage
 * 
 * When you connect a JDash ECU protocol handler to the VirtualECUPort, use
 * the getInputStream() and getOutputStream() methods to get stream objects
 * to interface with the port.
 * 
 * If you are connecting an ECU emulator instance to the VirtualECUPort, use 
 * the getECUInputStream() and getECUOutputStream() methods to get stream objects 
 * to interface with the port.
 * 
 * Be sure to call the open() method before attempting communication, and
 * the close() method after you are finished.
 * 
 * See also VirtualECU for documentation on the what is expected of the 
 * VirtualECU class.
 * 
 * @author greg
 */
public class VirtualECUPort extends BasePort {

	private PipedInputStream  pipeComm2ECUInput;
	private PipedOutputStream pipeComm2ECUOutput;
	private PipedInputStream  pipeECU2CommInput;
	private PipedOutputStream pipeECU2CommOutput;

	
	VirtualECUPort() {

	}
	
	/*******************************************************
	 * Close any resources associated with this class
	 * @throws Exception
	 *******************************************************/
	public boolean close() throws IOException {
		PipedInputStream is;
		PipedOutputStream os;
		
		if (pipeComm2ECUInput != null) {
			is  = pipeComm2ECUInput;
			pipeComm2ECUInput  = null; is.close();
		}

		if (pipeComm2ECUOutput != null) {
			os = pipeComm2ECUOutput;
			pipeComm2ECUOutput = null; os.close();
		}

		if (pipeECU2CommInput != null) {
			is  = pipeECU2CommInput;
			pipeECU2CommInput  = null; is.close();
		}

		if (pipeECU2CommOutput != null) {
			os = pipeECU2CommOutput;
			pipeECU2CommOutput = null; os.close();		
		}
		
		return true;
	}
	
	public boolean open() throws IOException {
		close();
		pipeComm2ECUInput  = new PipedInputStream();
		pipeComm2ECUOutput = new PipedOutputStream();
		pipeECU2CommInput  = new PipedInputStream();
		pipeECU2CommOutput = new PipedOutputStream();

		pipeComm2ECUInput.connect(pipeComm2ECUOutput);
		pipeECU2CommInput.connect(pipeECU2CommOutput);

		return true;
	}

	/**
	 * Indicates whether there are stream objects to be returned
	 * @return true if open, false otherwise.
	 */
	public boolean isOpen() {
		return (pipeComm2ECUInput != null &&
				pipeComm2ECUOutput != null &&
				pipeECU2CommInput != null &&
				pipeECU2CommOutput != null);
	}
	/**
	 * Return an InputStream object to write to the stream from the
	 * Communication port side
	 * @return InputStream object if the BaseStream is open.  
	 *   Returns null otherwise.
	 * @throws java.io.IOException
	 */
	public InputStream getInputStream() {
		return pipeComm2ECUInput;
	}
	public OutputStream getOutputStream() {
		return pipeECU2CommOutput;
	}
	
	
	public InputStream getECUInputStream() {
		return pipeECU2CommInput;
	}
	public OutputStream getECUOutputStream() {
		return pipeComm2ECUOutput;
	}

}
