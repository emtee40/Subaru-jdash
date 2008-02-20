/*******************************************************
 * 
 *  @author spowell
 * KWP2000Monitor
 *  Aug 8, 2006
 *  $Id: KWP2000Monitor.java,v 1.5 2006/12/31 16:59:08 shaneapowell Exp $
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

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterException;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.util.ByteUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/******************************************************
 * This is a KWP2000 Capable monitor. This montior
 * will only talk to an ECU that can handle the KWP Fast Init
 * initialization method.
 * 
 * Usefull Resources
 * http://forums.openecu.org/viewtopic.php?t=115&highlight=tactrix+obdii
 * http://andywhittaker.com/ECU/OBDIISoftware/tabid/69/Default.aspx
 * http://www.etools.org/files/public/generic-protocols-02-17-03.htm
 * http://scoobypedia.co.uk/index.php/Knowledge/ReadingECUCodes
 *****************************************************/
public class KWP2000Monitor extends RS232Monitor
{
		
	private static final int OBD_FAST_INIT_BAUD = 360;
	private static final int OBD_BAUD = 10400;
	
	
	private static final int OBD_HEADER_LENGTH = 3;
	
	private byte TX_LENGTH_BYTE = (byte)0xC0;
//	private byte RX_LENGTH_BYTE = (byte)0x80;
	
	private byte LENGTH_BYTE_MASK = (byte)0x3F;
	
	private byte TX_TARGET = (byte)0x33;
	private byte TX_SOURCE = (byte)0xF1;
	
//	private byte RX_TARGET = TX_SOURCE;
	
    
	/*******************************************************
	 * Create a new SSP Monitor
	 * @throws ParameterException
	 ******************************************************/
	public KWP2000Monitor() throws Exception
	{
	    super(OBD_BAUD, RXTXPort.DATABITS_8, RXTXPort.STOPBITS_1, RXTXPort.PARITY_NONE);
	}


	/*****************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
	 *******************************************************/
	public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
	{
		super.init(reg, initListener);
		
		/* Send the fast init */
		RXTXPort initPort = new gnu.io.RXTXPort(Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT));
		initPort.setFlowControlMode(RXTXPort.FLOWCONTROL_NONE);
		initPort.setSerialPortParams(OBD_FAST_INIT_BAUD,RXTXPort.DATABITS_8, RXTXPort.STOPBITS_1, RXTXPort.PARITY_NONE);
		initPort.enableReceiveTimeout(getTxRxTimeout());
		initPort.getOutputStream().write(new byte[] {0x00});
		initPort.getOutputStream().flush();
		initPort.getOutputStream().close();
		initPort.close();

		
		return null;

	}	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 *******************************************************/
	public String getEcuInfo() throws Exception
	{
		// TODO Auto-generated method stub
		return "KWP2000: ";
	}
	
	
	/******************************************************
	 * Kick off communications with the ECU.
	 * Override
	 * @see java.lang.Runnable#run()
	 ******************************************************/
	public void run()
	{
		
		try
		{
			OutputStream os = getPort().getOutputStream();
			InputStream is = getPort().getInputStream();
			
			
			while(doRun_.booleanValue())
			{
				
				/* create the parameter packet */
				RS232Packet txPacket = createTxPacket(null);
				System.out.println("TX: " + txPacket);
				
				/* Send it */
				RS232Packet rxPacket = sendPacket(txPacket);
				System.out.println("TX: " + rxPacket);
				
				
				System.out.println("RPM: " + (0x00 << rxPacket.getData()[0] & rxPacket.getData()[1]));
				
			}
			    
			os.close();
			is.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
        	{
        		closePort();
        	}
        	catch(Exception e)
        	{
        	}
		}
	}

	
	
	/*******************************************************
	 * This method will perform the heavylifting hard work
	 * required to send and receive a packet with a given
	 * format.  Packets sent and received with this method
	 * MUST conform to a particular format.  RX Packets
	 * can start with any number of header bytes.  But
	 * the next byte MUST be a "numbytes" value. This is the
	 * number of data bytes to expect on the input stream.
	 * After that number of bytes are received, the last byte
	 * will be a checksum byte for the entire returned packet.
	 * 
	 * 
	 * @param txPacket IN - the preformatted tx packet to be sent
	 * out the OutputStream os.
	 * @return the entire RX packet.
	 * @throws Exception if there was an abnormal error.
	 *******************************************************/
	private RS232Packet sendPacket(RS232Packet txPacket) throws Exception
	{
		
		try
		{
			
			/* Get the ports streams */
			OutputStream os = getPort().getOutputStream();
			InputStream is = getPort().getInputStream();
			
			/* Read any stale bytes on the input stream */
			if (is.available() != 0)
			{
				Thread.sleep(100); /* Wait for just a bit longer. giveing the stale bytes time to complete */
				System.out.println("Warning, there were stale bytes on the input stream");
				byte[] staleBytes = readBytes(is, is.available());
				System.out.println(ByteUtil.bytesToString(staleBytes));
			}
			
			
			/* Send the TX packet */
			txPacket.write(os);
			os.flush();
			
			
			/* Create the RX packet */
			RS232Packet rxPacket = new RS232Packet();
			
			/* Read the RX Header. */
			rxPacket.setHeader(readBytes(is, OBD_HEADER_LENGTH));
			
			/* From byte 1 of the header, extract the number of data bytes present */
			int dataByteCount = ByteUtil.unsignedByteToInt(rxPacket.getHeader()[0]) & LENGTH_BYTE_MASK;
System.out.println("RX packet indicates: " + dataByteCount + " data bytes on their way");			

			/* read the data bytes */
			rxPacket.setData(readBytes(is, dataByteCount));
			
			/* Read the checksum byte */
			rxPacket.setCheckSum(readBytes(is, 1)[0]);
			
			
			/* Check the checksum against the packet */
			byte checkSum = rxPacket.calcCheckSum();
			if (checkSum != rxPacket.getCheckSum())
			{
				throw new Exception("The checksum on the RX packet didn't match our calculations.  We calculated [" + checkSum + "]" + 
										" but the packet has [" + rxPacket.getCheckSum() + "]\n" +
										rxPacket);
			}
	
			
			
			/* Return the RX packet */
			return rxPacket;
		}
		catch(Exception e)
		{
			throw new Exception("There was a problem during the send/receive operation to the serial port [" + 
					Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT)+ "]\n", e);
		}
			
	}

	
	
	 /********************************************************
     * @return
     *******************************************************/
    private RS232Packet createTxPacket(ECUParameter p)
    {
    	
    	RS232Packet txPacket = new RS232Packet();
    	
    	/* Header */
    	byte[] header = new byte[OBD_HEADER_LENGTH];
    	header[0] = (byte)(TX_LENGTH_BYTE + ((byte)2));
    	header[1] = TX_SOURCE;
    	header[2] = TX_TARGET;
    	txPacket.setHeader(header);
    	
    	/* Data */
    	byte[] data = new byte[3];
    	data[0] = 01; /* mode */
    	data[1] = 0x0C; /* PID (rpm)*/ 
    	txPacket.setData(data);
    	
    	return txPacket;
    }
    
}

