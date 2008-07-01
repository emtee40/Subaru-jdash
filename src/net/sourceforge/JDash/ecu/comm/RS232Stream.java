/*******************************************************
 * 
 *  @author spowell
 *  RS232Monitor
 *  Aug 8, 2006
 *  $Id: RS232Monitor.java,v 1.6 2007/06/15 15:09:28 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

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

import gnu.io.RXTXPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import net.sourceforge.JDash.Setup;



/******************************************************
 * If your concrete monitor uses some sort of serial rs232 protocol,
 * then you'll likely want to extend this monitor.  Only
 * because it provides a number of methods that are usefull
 * for reading and writing serial bytes to a serial port.
 * Although the ELMScan module uses an RS232 serial interface, 
 * it's not the same thing as this monitor.  This class
 * will send and receive specially formatted packets. The 
 * ELMScan module responds to standard modem AT commands, and a
 * simple sending and receive to text strings.  Use the ELMScanMonitor
 * instead.
 * 
 * 
 * the getRXTXPort method was removed as I don't see a reason for
 * any higher level routine to directly access the rxtx port. -GN
 * 
 * 
 * Usefull Links
 * http://obddiagnostics.com/obdinfo/msg_struct.html
 *****************************************************/
public class RS232Stream extends BasePort
{
	/** This is the maximum number of failed packets in a row that can occur before this monitor will stop processing */
	public static final int MAX_PACKET_FAILURES = 5;
	
	public static final int DEFAULT_TXRX_TIMEOUT = 4000;
	
	/** The default timeout in ms on the TXRX line.  */
	private int txrxTimeout_ = DEFAULT_TXRX_TIMEOUT;
	
	/** The serial line baud rate */
	private int serialBaud_ = 0;
	
	/** The data bit size */
	private int dataBits_ = 0;
	
	/** The parity code */
	private int parity_ = 0;
	
	/** The stop bit */
	private int stopBit_ = 0;
	
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
	 *  @param serialBaud IN - the default BAUD rate to set the serial port to.
	 *  @param data IN - The data bits, use the constants from RXTXPort
	 *  @param parity IN - the parity state, use the constants from RXTXPort
	 *  @param stop IN - the stop bit, use the constatns from RXTXPOrt
	 ******************************************************/
	public RS232Stream(int serialBaud, int data, int parity, int stop) throws Exception
	{
		super();
		
		setTxRxTimeout(DEFAULT_TXRX_TIMEOUT);
	}
		
	/**
	 * Set the serial communications parameters.  Parameters may only be set
	 * while a communications port is not open.
	 * @param baud
	 * @param data
	 * @param parity
	 * @param stop
	 * @return true if setting of parameters succeeds, false otherwise.
	 */
	public boolean setSerialParams(int baud, int data, int parity, int stop) {
		if (port_ == null) {
			this.serialBaud_ = baud;
			this.dataBits_   = data;
			this.parity_     = parity;
			this.stopBit_    = stop;
			return true;
		} else {
			return false;
		}
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
		closePort();
		super.finalize();
	}
	
	/*******************************************************
	 * @return rxTxTimeout value in milliseconds
	 ******************************************************/
	public int getTxRxTimeout()
	{
		return this.txrxTimeout_;
	}
	
	
	/*******************************************************
	 * @param timeout timeout value in milliseconds
	 ******************************************************/
	public void setTxRxTimeout(int timeout)
	{
		this.txrxTimeout_ = timeout;
	}

	/*******************************************************
	 * Close the io streams and the port, and set the internal
	 * port variable to null.  The next call to getPort()
	 * will result in the re-opening of the port.
	 * @throws Exception
	 *******************************************************/
	protected void closePort() throws Exception
	{
		this.port_.getInputStream().close();
		this.port_.getOutputStream().close();
		this.port_.close();
		this.port_ = null;

	}
	
	
	
	/**
	 * Open the communications port using the parameters set in the constructor
	 * or setParams.
	 * @return true if method succeeds, false otherwise.
	 */
	public boolean open() throws IOException {
	  
		try
		{
			this.port_ = new gnu.io.RXTXPort(Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT));
			this.port_.setFlowControlMode(RXTXPort.FLOWCONTROL_NONE);
			this.port_.setSerialPortParams(this.serialBaud_, this.dataBits_, this.stopBit_, this.parity_);
			this.port_.setDTR(true);
			this.port_.setRTS(false);
			this.port_.notifyOnDSR(false);
			this.port_.notifyOnCTS(false);
			this.port_.enableReceiveTimeout(getTxRxTimeout());
			this.port_.notifyOnOutputEmpty(true);
			this.port_.setEndOfInputChar((byte)0x0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
            String errMsg = "Unable to open RS232 communications port";
            errMsg += " [" + Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT) + "]\n";
            errMsg += e.getClass().getName() + ": " + e.getMessage();
            
			throw new IOException(errMsg);
		}
		return true;
	}
    public boolean open(int timeout) throws IOException
    {
        return open(0);
    }
	
	public boolean close() 
	{
		try
		{
			closePort();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	
	/********************************************************
	 * Read the EXACT number of bytes from the input stream as
	 * identified in the numBytes value. This method will
	 * wait on the InputStream until atleast the numBytes
	 * of byte are available. Then EXACTLY that number of bytes
	 * will be read. No more, no less.  The return byte[] will
	 * be the bytes you requested.
	 * 
	 * @param numBytes IN - the number of bytes to read.
	 * @return the bytes that were read.
	 * @throws Exception If there was a problem reading the bytes. Like
	 * the read timed out.
	 ******************************************************/
	public byte[] readBytes(int numBytes) throws Exception
	{
		/* Setup our read buffer */
		byte[] rxData = new byte[numBytes];
		readBytes(rxData);
		return rxData;
	}
	
	public boolean readBytes(byte[] buffer) throws Exception
	{
		if (! isOpen()) {
			throw new Exception("Stream is not open!");
		}
		InputStream is = port_.getInputStream();
		is.read(buffer,0, buffer.length);
		return true;
	}
}

