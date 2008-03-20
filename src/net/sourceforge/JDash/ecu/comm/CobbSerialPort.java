/**
 *
 * @author greg
 * 
 * This class instantiates the drivers for the Cobb OBD-II USB dongle, based
 * on DLLs and source code provided from the Cobb Forums. 
 * 
 * 
 * You should instantiate a CobbSerialStream, then initialize it with the open()
 * method.  If that succeeds, then you can get a handle to a 
 * CobbSerialInputStream and/or a CobbSerialOutputStream.
 * 
 * The CobbSerialInputStream and CobbSerialOutputStreams make some attempt
 * at thread-safeness by specifying any methods that call the native* methods 
 * as 'synchronized'.
 */
package net.sourceforge.JDash.ecu.comm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class CobbSerialPort extends BasePort {

    // interfaces to the native Cobb driver methods.
    private native static int nativeStart();

    private native static void nativeStop(int nSessionID);

    private native static int  nativeRead(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativeWrite(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativePurge(int nSessionID);
    
	protected int nSessionID;
	protected CobbSerialInputStream istream;
	protected CobbSerialOutputStream ostream;

	public static final String MSG_EXCP_NULLPTR =
		"Pointer to CobbSerialPort cport is null.";
	public static final String MSG_EXCP_STREAM_NOT_OPEN =
		"CobbSerialPort is not open.";	
	
    public CobbSerialPort() 
	{
		nSessionID = -1;
    }

    /**
     * Opens and allocates resources to access the Cobb OBDII serial dongle.  
     * After calling this method, you may call getInputStream or getOutputStream
     * 
     * @return true on success, false on failure
     */
    public boolean open() {
		nSessionID = nativeStart();
		istream = new CobbSerialInputStream(this);
		ostream = new CobbSerialOutputStream(this);
		
		return (nSessionID < 0);
    }
	
	public boolean isOpen() {
		return nSessionID >= 0;
	}

    /**
     * Purges (flushes) the output stream and releases resources associated
     * with the Cobb OBDII serial dongle.  If, after you call close(), you
     * attempt to read or write from a stream created with getInputStream() or 
     * getOutputStream(), those calls will throw an IOException.
     * @return true
     */
    public boolean close() {
		nativePurge(nSessionID);
		nativeStop(nSessionID);
		nSessionID = -1;
		return true;
    }

    public InputStream getInputStream() {
		if (nSessionID < 0) return null;
		return istream;
    }

    public OutputStream getOutputStream() {
		if (nSessionID < 0) return null;
		return ostream;
    }

	
	
	/**
	 * Stream class definitions
	 */
    static public class CobbSerialInputStream extends InputStream {

		// Pointer to the source port
		CobbSerialPort cport;

		CobbSerialInputStream(CobbSerialPort cstream) 
		{
			this.cport = cport;
		}

		public int read() throws IOException 
		{
			byte[] buff = new byte[1];
			read(buff, 0, 1);
			return buff[0];
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) throws IOException 
		{
			checkPortIsOpen(cport);
			int n = nativeRead(cport.nSessionID, b, off, len);
			return n;
		} // end read()
    } // end CobbSerialInputStream

    static public class CobbSerialOutputStream extends OutputStream {

		CobbSerialPort cport;


		CobbSerialOutputStream(CobbSerialPort cport) 
		{
			this.cport = cport;
		}

		@Override
		protected void finalize() throws Throwable 
		{
			close();
			super.finalize();
		}

		@Override
		public void close() throws IOException 
		{
			flush();
		}

		public void write(int b) throws IOException 
		{
			byte[] b1 = new byte[1];
			b1[0] = (byte) (b & 0xff);
			write(b1, 0, 1);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException 
		{
			checkPortIsOpen(cport);

			int n = nativeWrite(cport.nSessionID, b, off, len);
			if (n != len) 
			{
				throw new IOException(
						"Not all data was written to CobbSerialPort (only" 
						+ n + " bytes");
			}
		}

		@Override
		public synchronized void flush() throws IOException 
		{
			checkPortIsOpen(cport);
			nativePurge(cport.nSessionID);
		}
    } // end CobbSerialOutputStream
	
	public static void checkPortIsOpen(CobbSerialPort cport) 
			throws IOException 
	{
	    if (cport == null)
			throw new NullPointerException(MSG_EXCP_NULLPTR);
	    if ( cport.nSessionID <= 0) 
			throw new IOException(MSG_EXCP_STREAM_NOT_OPEN);
	}
	
}
