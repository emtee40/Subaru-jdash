/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * A wrapper class for use with the RXTXSerialPort so that it conforms to the
 * BasePort class.
 * 
 * 
 */

package net.sourceforge.JDash.ecu.comm;
import gnu.io.RXTXPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
/**
 *
 * @author greg
 */
public class RXTXSerialPort extends BasePort {
	
	public static final int DEFAULT_RXTX_TIMEOUT = 4000;
	
	/** The default timeout in ms on the RXTX line.  */
	private int rxtxTimeout_ = DEFAULT_RXTX_TIMEOUT;
	
	/** The serial line baud rate */
	private int serialBaud_ = 0;
	
	/** The data bit size */
	private int dataBits_ = 0;
	
	/** The parity code */
	private int parity_ = 0;
	
	/** The stop bit */
	private int stopBit_ = 0;
	
	/** Identifier of the serial port */
	private String strPortName_ = null;
	
	/** The serial port */
	private RXTXPort port_ = null;

		
	/*******************************************************
	 *  Create a new instance of an RS232Monitor.
	 *  param rxEchosTx IN - this indicates that the ecu at the
	 *  other end of the serial port will echo our TX packet
	 *  back on the RX packet.  This TX echo will be stripped
	 *  from the RX packet before it's returned in the
	 *  sendPacket() method.  The default RX Timeout will be set to a
	 *  value of 1000ms.
	 *  
     *  @param strPortName String value to represent this portname for debugging.
	 *  @param serialBaud IN - the default BAUD rate to set the serial port to.
	 *  @param data IN - The data bits, use the constants from RXTXPort
	 *  @param parity IN - the parity state, use the constants from RXTXPort
	 *  @param stop IN - the stop bit, use the constatns from RXTXPOrt
	 ******************************************************/
	public RXTXSerialPort(String strPortName, int serialBaud, int data, int parity, int stop) throws Exception
	{
		super();
		setSerialParams(strPortName, serialBaud, data, parity, stop);
		rxtxTimeout_ = DEFAULT_RXTX_TIMEOUT;
	}
		
	/**
	 * Set the serial communications parameters.  Parameters may only be set
	 * while a communications port is not open.
	 * @param strPortName  the name of the port, e.g., COM1, or /dev/tty/USB0
	 * @param baud         baud for the comm port
	 * @param data         data for the comm port
	 * @param parity       parity for the comm port
	 * @param stop         stop value for the comm port
	 * @return true if setting of parameters succeeds, false otherwise.
	 */
	public boolean setSerialParams(String strPortName, int baud, int data, int parity, int stop) {
		if (port_ != null) return false;
		this.strPortName_ = strPortName;
		this.serialBaud_ = baud;
		this.dataBits_   = data;
		this.parity_     = parity;
		this.stopBit_    = stop;
		return true;
	}
	
	
	public InputStream getInputStream() {
		if (port_ == null) return null;
		return port_.getInputStream();
	}
	
	public OutputStream getOutputStream() {
		if (port_ == null) return null;
		return port_.getOutputStream();
	}
	
	public boolean isOpen() {
		return (port_ == null);
	}

	/*******************************************************
	 * Override
	 * @see java.lang.Object#finalize()
	 *******************************************************/
	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	

	public boolean close() throws IOException
	{
		if (this.port_ == null) return false;
		// GN: is close necessary?
		this.port_.getInputStream().close();
		this.port_.getOutputStream().close();
		this.port_.close();
		this.port_ = null;
		return true;

	}
	
	
	
	/**
	 * Open the communications port using the parameters set in the constructor
	 * or setParams.  Throws an IOException if we are unable to create the 
     * underlying port object.
	 * @return true if method succeeds, false otherwise. 
	 */
	public boolean open(int timeout) throws IOException {
	  
		try
		{
			// Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT)
			port_ = new gnu.io.RXTXPort(strPortName_);
			port_.setFlowControlMode(RXTXPort.FLOWCONTROL_NONE);
			port_.setSerialPortParams(this.serialBaud_, this.dataBits_, this.stopBit_, this.parity_);
			port_.setDTR(true);
			port_.setRTS(false);
			port_.notifyOnDSR(false);
			port_.notifyOnCTS(false);
			port_.enableReceiveTimeout(rxtxTimeout_);
			port_.notifyOnOutputEmpty(true);
			port_.setEndOfInputChar((byte)0x0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
            String errMsg = "Unable to open RS232 communications port";
            errMsg += " [" + strPortName_ + "]\n";
            errMsg += e.getClass().getName() + ": " + e.getMessage();
            
			throw new IOException(errMsg);
		}
		return true;
	}
    
    public boolean open() throws IOException {
        return open(0);
    }

}
