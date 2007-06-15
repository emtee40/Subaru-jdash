/*******************************************************
 * 
 *  @author spowell
 *  SSMOBD2Monitor.java
 *  Sep 7, 2006
 *  $Id: SSMOBD1Monitor.java,v 1.4 2006/12/31 16:59:08 shaneapowell Exp $
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

import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.util.UTIL;
import gnu.io.RXTXPort;


/*******************************************************
 * This extension to the SSMMonitor is setup to
 * use the OBD-II version of SSM
 ******************************************************/
public class SSMOBD1Monitor extends RS232Monitor
{
	private static final int SSM_OBD1_BAUD_RATE = 1953;
	
	private static final int TX_PACKET_SIZE = 4;
	
	private static final int SSM_OBD1_ADDRESS_SIZE = 2;
	
	private static final byte SSM_STOP_CODE = (byte)0x12;
	private static final byte SSM_READ_CODE = (byte)0x78; 
	
	/*******************************************************
	 * Create a new SSM OBD-II capable monitor.
	 ******************************************************/
	public SSMOBD1Monitor() throws Exception
	{
		super(SSM_OBD1_BAUD_RATE, RXTXPort.DATABITS_8, RXTXPort.PARITY_EVEN, RXTXPort.STOPBITS_1);
	}


	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 *******************************************************/
	public String getEcuInfo() throws Exception
	{
		return "SSM/OBD-I";
	}
	
	
	 
    /******************************************************
     * Kick off communications with the ECU.
     * Override
     * @see java.lang.Runnable#run()
     ******************************************************/
    public void run()
    {
        
    	int packetFailureCount = 0;

        try
        {
        	/* Set the RTS to false */
            getPort().setRTS(false);
            InputStream is = getPort().getInputStream();
            OutputStream os = getPort().getOutputStream();

            
        	/* The main TX/RX loop */
            while(this.doRun_.booleanValue())
            {
            	
            	try
            	{
                
	                /* define the list of parameters that will be fetched.  This is based on if the
	                 * packets rate has it ready for an update or not. */
	                for (ECUParameter param : this.params_)
	                {
	                	/* If it's due for an update, then run it */
	                	if (param.getLastFetchTime() + param.getPreferedRate() < System.currentTimeMillis())
	                	{
	                		/* reset the fetch time */
	                		param.setLastFetchTime(System.currentTimeMillis());
	                		
	                		/* Check the address size of the parameter */
	                		if (param.getAddress().length != SSM_OBD1_ADDRESS_SIZE)
	                		{
	                			throw new Exception("Invalid address for parameter: " + param.getName() + ". It has " + param.getAddress().length + " bytes, but we can only work with " + SSM_OBD1_ADDRESS_SIZE); 
	                		}
	                		
	                		/* Send the stop command */
	                		os.write(new byte[] {SSM_STOP_CODE});
	                		
	                		/* Setup the read packet */
	                		byte[] txPacket = new byte[TX_PACKET_SIZE];
	                		txPacket[0] = SSM_READ_CODE;
	                		txPacket[1] = param.getAddress()[0];
	                		txPacket[2] = param.getAddress()[1];
	                		txPacket[3] = 0;
	                System.out.println("TX: " + UTIL.bytesToString(txPacket));
	                		
	                		/* Send the read packet */
	                		os.write(txPacket, 0, txPacket.length);
	                		
	                		/* Read the result */
	                		byte[] rxPacket = readBytes(is, SSM_OBD1_ADDRESS_SIZE + 1);
	                		
	                System.out.println("RX: " + UTIL.bytesToString(rxPacket));
	                		
	                		/* Send the stop command */
	                		os.write(new byte[] {SSM_STOP_CODE});

	                		/* The result MUST start with the 2 byte address */
	                		if ((rxPacket[0] != param.getAddress()[0]) && (rxPacket[1] != param.getAddress()[1]))
	                		{
	                			throw new Exception("The packet received from the ECU did not start with the memory address as expected\n" +
	                								"TX:" + UTIL.bytesToString(txPacket) + "\n" + 
	                								"RX:" + UTIL.bytesToString(rxPacket));
	                		}
	                		
	                		/* Set the result to the parameter */
	                		param.setResult(rxPacket[2]);
	                		
	                	} /* end of if statement */
	                	
	                	/* Check the doRun flag */
	                	if (this.doRun_ == false)
	                	{
	                		break;
	                	}
	                	
	                } /* end of for loop */
	                
	                
	                /* We've gone through the parameters once, so now we'll mark the time parameter */
	                markTime();
	                

                }
                catch(Exception e)
                {
                	packetFailureCount++;  

                	if (packetFailureCount >= MAX_PACKET_FAILURES)
                	{
                		stop();
                		throw new RuntimeException("There was a problem with the TX/RX packet.  Too many failures in a row: " + packetFailureCount);
                	}
                	
                	
                	/* Log any failed packets */
                	e.printStackTrace();
                }
                
            } /* end while loop */
            
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

    

}
