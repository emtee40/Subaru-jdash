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
 *
 * Actually this is a COBB *USB* serial port.
 */
package net.sourceforge.JDash.ecu.comm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import net.sourceforge.JDash.logger.StreamTraceLog;

public class CobbSerialPort extends BasePort {
    public static final int DEBUGLEVEL = 0;

    // interfaces to the native Cobb driver methods.
    private native static int  nativeStart(int timeout);

    private native static void nativeStop(int nSessionID);

    private native static int  nativeRead(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativeWrite(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativePurge(int nSessionID);
    
	protected int nSessionID;
	protected CobbSerialInputStream istream = null;
	protected CobbSerialOutputStream ostream = null;

	public static final String MSG_EXCP_NULLPTR =
		"Pointer to CobbSerialPort cport is null.";
	public static final String MSG_EXCP_STREAM_NOT_OPEN =
		"CobbSerialPort is not open.";	
	
    public CobbSerialPort() 
	{
        super();
        // TODO: wire in something that lets you enable or
        // disable this trace.
		nSessionID = -1;
    }

    /**
     * Opens and allocates resources to access the Cobb OBDII serial dongle.  
     * After calling this method, you may call getInputStream or getOutputStream
     * 
     * @return true on success, false on failure
     */
    public boolean open(int timeout) throws IOException {
	    String errmsg = "";
		nSessionID = nativeStart(timeout);

		// Interpret error codes
		switch (nSessionID) {
		case -1: errmsg = "Could not open COBBdriver.dll"; break;
		case -2: errmsg = "Could not locate all necessary routines in COBBdriver.dll.\n" + 
		                  "COBBdriver.dll and cobbjni.dll may be out of sync."; 
		         break;
		case -3: errmsg = "Port driver couldn't open a session. \n" +
				          "Is the hardware dongle connected?";
		         break;
		default: if (nSessionID < 0)
			     errmsg = "Unknown error " + nSessionID + ".";
		         break;	
		}
		
        if (nSessionID < 0) 
            throw new IOException("CobbSerialPort error:\n" + errmsg );

        
		istream = new CobbSerialInputStream(this);
		ostream = new CobbSerialOutputStream(this);
        
        strace.open("cobbserialport.log");
        ((CobbSerialInputStream)istream).strace = strace;
        ((CobbSerialOutputStream)ostream).strace = strace;
        
        
        
		if (DEBUGLEVEL >= 2) 
            System.out.println("CobbSerialPort::open() returning code " + nSessionID);
		return (nSessionID < 0);
    }

	@Override
    public boolean open() throws IOException
    {
        return open(0);
    }
	@Override
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
		if (DEBUGLEVEL >= 2) 
            System.out.println("CobbSerialPort::close()");
		nativePurge(nSessionID);
		if (DEBUGLEVEL >= 3) 
            System.out.println("CobbSerialPort::close() finished purge");
		nativeStop(nSessionID);
		if (DEBUGLEVEL >= 3) 
            System.out.println("CobbSerialPort::close() finished stop");
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
		CobbSerialPort cport = null;
        StreamTraceLog strace = null;
        
        private int _avail;

		CobbSerialInputStream(CobbSerialPort cstream) 
		{
			this.cport = cstream;
            _avail = 0;
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
       		if (DEBUGLEVEL >= 3) 
                System.out.println("CobbSerialInputStream::read(off=" + off + "; len=" + len + ")");
			checkPortIsOpen(cport);
			int n = nativeRead(cport.nSessionID, b, off, len);
       		if (DEBUGLEVEL >= 2) 
                System.out.println("CobbSerialInputStream::read(off=" + off + "; len=" + len + ") returned "+n+ " bytes");
            
            
            if (strace != null)
            {
                strace.logDataEvent("ECU2PC", b, off, len);
            }
            
            _avail -= n;

            return n;
		} // end read()
        
        @Override
        public synchronized int available()
        {
            // Hmm... how do we figure out how many bytes are
            // available on the input stream?
            int n = _avail++ - 1;
            return (n < 0) ? 0 : n;
        }
        
    } // end CobbSerialInputStream

    static public class CobbSerialOutputStream extends OutputStream {

		CobbSerialPort cport = null;
        StreamTraceLog strace = null;


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
    		if (DEBUGLEVEL >= 2) 
            System.out.println("CobbSerialOutputStream::write(" + len + " bytes)");

			int n = nativeWrite(cport.nSessionID, b, off, len);
			if (n != len) 
			{
				throw new IOException(
						"Not all data was written to CobbSerialPort (only " 
						+ n + " bytes)");
			}

            if (strace != null)
            {
                strace.logDataEvent("PC2ECU", b, off, len);
            }
            
            if (DEBUGLEVEL >= 2) 
            System.out.println("CobbSerialOutputStream::write() wrote " + n + " bytes");
		}

		@Override
		public synchronized void flush() throws IOException 
		{
    		if (DEBUGLEVEL >= 2) 
            System.out.println("CobbSerialOutputStream::flush()");
			checkPortIsOpen(cport);
            // Sort of assuming that "purge" means the same thing as "flush"
			nativePurge(cport.nSessionID);
		}
    } // end CobbSerialOutputStream
	
    /**
     * A static method to see whether the given serialport is open.  If not,
     * throw an exception.  This method is static because it also checks to
     * see if the serial port is null.
     * @param cport
     * @throws java.io.IOException
     */
	public static void checkPortIsOpen(CobbSerialPort cport) 
			throws IOException 
	{
	    if (cport == null)
			throw new NullPointerException(MSG_EXCP_NULLPTR);
	    if (! cport.isOpen()) 
			throw new IOException(MSG_EXCP_STREAM_NOT_OPEN);
	}
	
}
