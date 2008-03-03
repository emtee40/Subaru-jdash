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

public class CobbSerialStream extends BaseStream {

    // interfaces to the native Cobb methods.
    private native static int nativeStart();

    private native static void nativeStop(int nSessionID);

    private native static int  nativeRead(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativeWrite(int nSessionID, byte[] buff, int off, int nLength);

    private native static int  nativePurge(int nSessionID);
    int nSessionID;

    public CobbSerialStream() {
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

    public CobbSerialInputStream getInputStream() {
	if (nSessionID < 0) {
	    return null;
	}
	return new CobbSerialInputStream(this);
    }

    public CobbSerialOutputStream getOutputStream() {
	if (nSessionID < 0) {
	    return null;
	}
	return new CobbSerialOutputStream(this);
    }

    static public class CobbSerialInputStream extends InputStream {

	public static final String MSG_EXCP_NULLPTR =
		"Pointer to CobbSerialStream cstream is null.";
	public static final String MSG_EXCP_STREAM_NOT_OPEN =
		"CobbSerialStream is not open.";
	// Pointer to the source port
	CobbSerialStream cstream;

	CobbSerialInputStream(CobbSerialStream cstream) {
	    this.cstream = cstream;
	}

	public int read() throws IOException {
	    byte[] buff = new byte[1];
	    read(buff, 0, 1);
	    return buff[0];
	}

	@Override
	public int read(byte[] b) throws IOException {
	    return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
	    if (cstream == null) {
		throw new NullPointerException(MSG_EXCP_NULLPTR);
	    }
	    int nSessionID = cstream.nSessionID;
	    if (nSessionID <= 0) {
		throw new IOException(MSG_EXCP_STREAM_NOT_OPEN);
	    }
	    int n = nativeRead(nSessionID, b, off, len);
	    return n;
	}
    } // end CobbSerialInputStream

    static public class CobbSerialOutputStream extends OutputStream {

	CobbSerialStream cstream;
	public static final String MSG_EXCP_NULLPTR =
		"Pointer to CobbSerialStream cstream is null.";
	public static final String MSG_EXCP_STREAM_NOT_OPEN =
		"CobbSerialStream is not open.";

	CobbSerialOutputStream(CobbSerialStream cstream) {
	    this.cstream = cstream;
	}

	@Override
	protected void finalize() throws Throwable {
	    close();
	    super.finalize();
	}

	@Override
	public void close() throws IOException {
	    flush();
	}

	public void write(int b) throws IOException {
	    byte[] b1 = new byte[1];
	    b1[0] = (byte) (b & 0xff);
	    write(b1, 0, 1);
	}

	@Override
	public void write(byte[] b) throws IOException {
	    write(b, 0, b.length);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
	    if (cstream == null) {
		throw new NullPointerException(MSG_EXCP_NULLPTR);
	    }
	    int nSessionID = cstream.nSessionID;
	    if (nSessionID <= 0) {
		throw new IOException(MSG_EXCP_STREAM_NOT_OPEN);
	    }

	    int n = nativeWrite(nSessionID, b, off, len);
	    if (n != len) {
		throw new IOException("Not all data was written to CobbSerialStream " + n);
	    }
	}

	@Override
	public synchronized void flush() throws IOException {
	    if (cstream == null) {
		throw new NullPointerException(MSG_EXCP_NULLPTR);
	    }
	    int nSessionID = cstream.nSessionID;
	    if (nSessionID <= 0) {
		throw new IOException(MSG_EXCP_STREAM_NOT_OPEN);
	    }
	    nativePurge(nSessionID);

	}
    } // end CobbSerialOutputStream
}
