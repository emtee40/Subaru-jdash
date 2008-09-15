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
import net.sourceforge.JDash.logger.StreamTraceLog;

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
 *            pipeComm2ECUOutput ==> pipeComm2ECUInput
 *          /                                          \
 * [JDash protocol handler]  <====>              [VirtualECU]
 *          \                                          /
 *            pipeECU2CommInput <-- pipeECU2CommOutput
 * -->
 *
 *
 * Usage
 *
 * Use the open() method to initialize the VirtualECUPort
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

	protected PipedInputStream  pipeComm2ECUInput  = null;
	protected PipedOutputStream pipeComm2ECUOutput = null;
	protected PipedInputStream  pipeECU2CommInput  = null;
	protected PipedOutputStream pipeECU2CommOutput = null;

    protected LoggedOutputStream streamFromECU = new LoggedOutputStream("ECU2PC");
    protected LoggedOutputStream streamFromPC  = new LoggedOutputStream("PC2ECU");

    // Denotes whether each party has connected to the port.
    protected boolean bIsECUConnected  = false;
    protected boolean bIsCommConnected = false;

    // Waiting semaphores
    public static final boolean bLogStream = false;

	public VirtualECUPort()
    {

    }

    synchronized void createResources() throws IOException {
        // Create the pipe from communication to ECU
        if (pipeComm2ECUInput == null){
    		pipeComm2ECUInput  = new PipedInputStream();
    		pipeComm2ECUOutput = new PipedOutputStream();

            // Create the pipe from the ECU to the comm unit
    		pipeECU2CommInput  = new PipedInputStream();
        	pipeECU2CommOutput = new PipedOutputStream();

    		pipeComm2ECUInput.connect(pipeComm2ECUOutput);
    		pipeECU2CommInput.connect(pipeECU2CommOutput);

            streamFromECU.os = pipeECU2CommOutput;
            streamFromPC.os  = pipeComm2ECUOutput;
            
            if (bLogStream) 
            {
                strace.open("virtualecu.log");
                streamFromECU.strace = strace;
                streamFromPC.strace  = strace;
            }
        }
    }
    synchronized void destroyResources() throws IOException {
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
        if (bLogStream) strace.close();
        bIsECUConnected  = false;
        bIsCommConnected = false;
    }


	/*******************************************************
	 * Close any resources associated with this class
	 * @throws Exception
	 *******************************************************/
	public boolean close() throws IOException {
        destroyResources();

		return true;
	}

    // Open connection resources from the port to the monitor
    @Override
	public boolean open() throws IOException {
        return open(0);
	}
    public boolean open(int timeout) throws IOException {
		//close();
        createResources();

        bIsCommConnected = true;
        // Wait for the ECU to connect


        return true;

    }

    // Open connection resources from the ECU to the port
    public boolean ecuOpen(int timeout) throws IOException {

        createResources();
        bIsECUConnected = true;
        // Wait for the monitor to connect.

        return true;
    }
    public boolean ecuOpen() throws IOException {
        return ecuOpen(0);
    }



	/**
	 * Indicates whether there are stream objects to be returned
	 * @return true if open, false otherwise.
	 */
    @Override
	public boolean isOpen() {
		return (pipeComm2ECUInput != null &&
				pipeComm2ECUOutput != null &&
				pipeECU2CommInput != null &&
				pipeECU2CommOutput != null);
	}
	/**
	 * Return an InputStream object to write to the stream from the
	 * Communication port side.
     *
	 * @return InputStream object if the BaseStream is open.
	 *   Returns null otherwise.
	 * @throws java.io.IOException
	 */
	synchronized public InputStream getInputStream() {
		return pipeECU2CommInput;
	}
	synchronized public OutputStream getOutputStream() {
		//return pipeComm2ECUOutput;
        return streamFromPC;
	}


	synchronized public InputStream getECUInputStream() {
		return pipeComm2ECUInput;
	}
	synchronized public OutputStream getECUOutputStream() {
		//return pipeECU2CommOutput;
        return streamFromECU;
	}

    //
    public static class LoggedOutputStream extends OutputStream
    {
        public String         name   = null;
        public StreamTraceLog strace = null;
        public OutputStream   os     = null;

        LoggedOutputStream(String name)
        {
            this.name = name;
        }

		public void write(int b) throws IOException
		{
			byte[] b1 = new byte[1];
			b1[0] = (byte) (b & 0xff);
			write(b1, 0, 1);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len)
            throws IOException
        {
            os.write(b,off,len);
            if (strace != null)
            {
                // TODO: honor the offset and length
                strace.logDataEvent(name, b, off, len);
            }
        }

    }

}
