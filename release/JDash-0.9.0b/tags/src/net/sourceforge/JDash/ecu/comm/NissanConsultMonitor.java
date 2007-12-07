/*******************************************************
 * 
 *  @author spowell
 * KWP2000Monitor
 *  June 21, 2007
 *  $Id:$
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/******************************************************
 * This is a Nissan Consult capable monitor. At present, this
 * module is pretty much stubbed out.  I'm hoping someone with
 * a Nissan can step up and write the gutts of this one.
 *****************************************************/
public class NissanConsultMonitor extends RS232Monitor
{
		
	private static final int CONSULT_BAUD = 9600;
	
	private static final byte READ_BYTE_COMMAND_START = (byte)0xc9;
	private static final byte READ_BYTE_COMMAND_END = (byte)0xf0;
	
    
	/*******************************************************
	 * Create a new Consult Monitor
	 * @throws ParameterException
	 ******************************************************/
	public NissanConsultMonitor() throws Exception
	{
	    super(CONSULT_BAUD, RXTXPort.DATABITS_8, RXTXPort.PARITY_NONE, RXTXPort.STOPBITS_1);
	}


	/*****************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
	 *******************************************************/
	public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
	{
		super.init(reg, initListener);
		
    	/* Setup and send the init packet */
    	RS232Packet txPacket = new RS232Packet();
    	txPacket.setHeader(new byte[] {(byte)0xff, (byte)0xff, (byte)0xef});
    	initListener.update("Initialize Interface", 1, 1);
		RS232Packet rxPacket = sendPacket(txPacket);

		
		return null;

	}	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 *******************************************************/
	public String getEcuInfo() throws Exception
	{
		// TODO Auto-generated method stub
		return "Nissan Consult: ";
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
				
				/* Refer to one of the other montiors on what to do here.  The Subaru SSM is the most complete */
				
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
				byte[] staleBytes = readBytes(is, is.available());
			}
			
			
//			/* Send the TX packet */
//			txPacket.write(os);
//			os.flush();
//			
//			
			/* Create the RX packet */
			RS232Packet rxPacket = new RS232Packet();
//			
//			/* Read the RX Header. */
//			rxPacket.setHeader(readBytes(is, OBD_HEADER_LENGTH));
//			
//			/* From byte 1 of the header, extract the number of data bytes present */
//			int dataByteCount = UTIL.unsignedByteToInt(rxPacket.getHeader()[0]) & LENGTH_BYTE_MASK;
//
//			/* read the data bytes */
//			rxPacket.setData(readBytes(is, dataByteCount));
//			
//			/* Read the checksum byte */
//			rxPacket.setCheckSum(readBytes(is, 1)[0]);
//			
//			
//			/* Check the checksum against the packet */
//			byte checkSum = rxPacket.calcCheckSum();
//			if (checkSum != rxPacket.getCheckSum())
//			{
//				throw new Exception("The checksum on the RX packet didn't match our calculations.  We calculated [" + checkSum + "]" + 
//										" but the packet has [" + rxPacket.getCheckSum() + "]\n" +
//										rxPacket);
//			}
//	
			
			
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
    	byte[] header = new byte[1];
    	header[0] = READ_BYTE_COMMAND_START;
    	txPacket.setHeader(header);
    	
    	/* Data */
    	byte[] data = new byte[2];
    	data[0] = p.getAddress()[0];
    	data[1] = READ_BYTE_COMMAND_END; 
    	txPacket.setData(data);
    	
    	return txPacket;
    }
    
}

