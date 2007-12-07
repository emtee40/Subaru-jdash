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
 * Usefull Links
 * http://obddiagnostics.com/obdinfo/msg_struct.html
 *****************************************************/
public abstract class RS232Monitor extends BaseMonitor
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
	
	/** The seial port */
	private RXTXPort port_ = null;
	
	
	
	/*******************************************************
	 *  Create a new instance of an RS232Monitor.
	 *  @param rxEchosTx IN - this indicates that the ecu at the
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
	public RS232Monitor(int serialBaud, int data, int parity, int stop) throws Exception
	{
		super();
		
		this.serialBaud_ = serialBaud;
		this.dataBits_ = data;
		this.parity_ = parity;
		this.stopBit_ = stop;
		
		setTxRxTimeout(DEFAULT_TXRX_TIMEOUT);
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
	 * @return
	 ******************************************************/
	public int getTxRxTimeout()
	{
		return this.txrxTimeout_;
	}
	
	
	/*******************************************************
	 * @param timeout
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
	
	/*******************************************************
	 * This function will create and return the RXTX Serial Port.
	 * The first call to this method will create the port, all
	 * following calls will return the originally created one. 
	 * The BAUD rate of the port is set with the value passed into the
	 * constructor. If you need to do anything out of the ordinary
	 * for serial port generation, then override this method, and
	 * go to town.  Note, other than the data/parity/stop bit settings,
	 * the only thing done to this port is to set the flowcontrol to NONE>
	 * 
	 * @return the serial port.
	 ******************************************************/
	protected RXTXPort getPort() throws Exception
	{
		
		if (this.port_ != null)
		{
			return this.port_;
		}
	  
	  
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Exception("Unable to open monitor communications port [" + Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT) + "]\n" + e.getMessage());
		}
		
		return this.port_;
  }
  
  
	
	/*******************************************************
	 * Calculate the checksum value for the given byte array
	 * @param b
	 * @return
	 *******************************************************/
	protected static byte calcCheckSum(byte[] byteArray)
	{
		byte sum = 0;
		
		for (byte b : byteArray)
		{
			sum += b;
		}
		
		return sum;
	}
	
	
	
	
	/*******************************************************
	 * The sendPacket method sets up a monitor timer to watch
	 * for the IO port timeint out on the TX/RX sequence.  If the
	 * timer fires, it will call this method.  Here, we'll
	 * not only break the connection, but also close the port.
	 *******************************************************/
	protected void breakConnection()
	{
		try
		{
			closePort();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/********************************************************
	 * Read the EXACT number of bytes from the input stream as
	 * identified in the numBytes value. This method will
	 * wait on the InputSTream until atleast the numBytes
	 * of byte are available. Then EXACTLY that number of bytes
	 * will be read. No more, no less.  The return byte[] will
	 * be the bytes you requested.
	 * 
	 * @param is IN - the input stream to read from.
	 * @param bytes IN - the number of bytes to read.
	 * @return the read bytes.
	 * @throws Exception If there was a problem reading the byes. Like
	 * the read timed out.
	 ******************************************************/
	protected byte[] readBytes(InputStream is, int numBytes) throws Exception
	{
		
		/* Setup our read buffer */
		byte[] rxPacket = new byte[numBytes];
		
		
		/* Byte by byte, read the data */
		for (int index = 0; index < rxPacket.length; index++)
		{
			is.read(rxPacket, index, 1);
		}
		
		
		/* Return the receive packet */
		return rxPacket;
		
	}

	
	
}

