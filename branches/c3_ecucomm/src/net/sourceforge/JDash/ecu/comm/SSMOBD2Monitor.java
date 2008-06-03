/*******************************************************
 * 
 *  @author spowell
 *  SSMOBD2Monitor.java
 *  Sep 7, 2006
 *  $Id: SSMOBD2Monitor.java,v 1.4 2006/12/31 16:59:08 shaneapowell Exp $
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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
//import net.sourceforge.JDash.util.JDEvent;
//import net.sourceforge.JDash.util.JDEventListener;

//import net.sourceforge.JDash.ecu.comm.SSMOBD2ProtocolHandler;
import net.sourceforge.JDash.ecu.comm.SSMOBD2ProtocolHandler.SSMPacket;
import java.io.IOException;

/***************************************************************************************************
 * This extension to the SSMMonitor is setup to use the OBD-II version of SSM
 *  * http://scoobypedia.co.uk/index.php/Knowledge/ReadingAndReflashingECUs
 * http://www.scoobypedia.co.uk/index.php/Knowledge/ECUVersionCompatibilityList#toc3
 * http://www.enginuity.org/search.php?search_id=1844875639&start=105
 * http://www.vwrx.com/index.php?pg=selectmonitor
 **************************************************************************************************/
public class SSMOBD2Monitor extends BaseMonitor
{

	private static final int MAX_PACKET_FAILURES = 5;
	
	private SSMOBD2ProtocolHandler ssmprotohandler;

	/***************************************************************************
	 * Create a new SSM OBD-II capable monitor.
	 **************************************************************************/
	public SSMOBD2Monitor() throws Exception
	{
        //initDefaultPortObject();

		ssmprotohandler = new SSMOBD2ProtocolHandler();
	}

    /* The SSM protocol requires that communication be initialized to 4800 Baud,
     * N81.
     * 
     * 
     * */
    
    
    /**
     * 
     * This method initializes a BasePort object to be ready to use the SSM
     * protocol.  If the BasePort-derived class is not supported, the routnie
     * returns false.
     * 
     * TODO: determine if strPortName needs to be made into a more generic parameter.
     * 
     * @param port A reference to an initialized BasePort object.  
     * @param strPortName Name of the port resource to connect to.
     * @return port object if initialization succeeds, null if it fails (e.g., if the
     * baseport class is not supported.)
     * @throws java.lang.Exception
     */
    
    
	public BasePort initPort(BasePort port, String strPortName) throws IOException
    {
        
        if (commPort != null) {
            System.out.println("Warning: SSMOBD2Monitor.commPort object is already initialized!");
            return commPort;
        }

        // Can't initialize a null port [this case implicitly handled by super]
        if (port == null) return null;
        
        if (port instanceof RXTXSerialPort) 
        {
            // Set the port to baud 4800, N81.
            ((RXTXSerialPort)port).setSerialParams(strPortName,
                SSMOBD2ProtocolHandler.DEFAULT_SSM_BAUD,
				RXTXPort.DATABITS_8, 
				RXTXPort.PARITY_NONE, 
				RXTXPort.STOPBITS_1);
            commPort = port;
        } 
        else 
        {
            if (super.initPort(port, strPortName) == null) 
                throw new RuntimeException(
                        "This BasePort derived class is not supported.");
        }
        commPort.open();

        
        //ssmprotohandler.comm_serial = commPort;
        ssmprotohandler.is_ = commPort.getInputStream();
        ssmprotohandler.os_ = commPort.getOutputStream();
        
        return commPort;
    }
    
    public boolean closePort() {
        return super.closePort();
    }
	
	/***********************************************************************************************
	 * Override
	 * 
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
	 **********************************************************************************************/
	public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
	{
		super.init(reg, initListener);
		
		ssmprotohandler.protocolInit();

		/* Return the list of parameters this monitor claims to support */
		return ssmprotohandler.paramList;
	}

	/***********************************************************************************************
	 * Override
	 * 
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 **********************************************************************************************/
	public String getEcuInfo() throws Exception
	{
		// TODO Auto-generated method stub
		return "SSP Monitor:\nECUID: " + ssmprotohandler.getECUID();
	}

	/***********************************************************************************************
	 * Kick off communications with the ECU. Override
	 * 
	 * @see java.lang.Runnable#run()
	 **********************************************************************************************/
	public void run()
	{

		int packetFailureCount = 0;
		/*
		 * Now, we need to check that we are not trying to process TOO many parameters
		 * in one go. There is a maximum number of bytes that can be sent in one packet.
		 * If we are about to exceed that, then we'll need to chop up our parameters
		 * into multiple packets. Since the max data size of an packet appears to
		 * be 128, then we'll send 128/3 parameters at a time.
		 */
		int paramsPerPacket = 
				SSMOBD2ProtocolHandler.SSMPacket.MAX_DATA_LENGTH / 
				SSMOBD2ProtocolHandler.SSM_OBD2_ADDRESS_SIZE;

		List<ECUParameter> updateParamList = new ArrayList<ECUParameter>();
		List<ECUParameter> packetParamList = new ArrayList<ECUParameter>();
		((ArrayList)packetParamList).ensureCapacity(paramsPerPacket);

		try
		{

			// GN: this should already be handled in the open() function
			// for the port implementation.
			//comm_serial.getPort().setRTS(false);

			/* The main TX/RX loop */
			while (doRun_.booleanValue())
			{

				/* Choose which parameters to update, based on the preferred
				 * update rate and the last time this parameter was updated.
				 */
				updateParamList.clear();
				super.getParamsForUpdate(updateParamList);

				/* Send the TX packet, and wait for the RX packet */
				try
				{

					fireProcessingStartedEvent();

					int index = 0;
					
					while (index < updateParamList.size()) {
						packetParamList.clear();
						
						// Add the parameters to the current param list until
						// we reach the max parameters per packet or we run
						// out of parameters
						long t = System.currentTimeMillis();
						while (packetParamList.size() < paramsPerPacket && 
								index < updateParamList.size()) 
						{	
							ECUParameter p = updateParamList.get(index++);
							packetParamList.add(p);
							p.setLastFetchTime(t);
						}
						
						// GN: the next three operations should probably be
						// folded into a helper method in SSMOBD2ProtocolHandler.
						
						// Create the new TX Packet 
						SSMPacket txPacket = 
								SSMOBD2ProtocolHandler.encodeECUParameterQueryPacket(
								packetParamList, true);

						// Send a packet and receive a response.
						SSMPacket rxPacket = ssmprotohandler.sendAndReceivePacket(txPacket);
						
						// Update the parameter list with the data returned in
						// rxPacket.
						SSMOBD2ProtocolHandler.decodeECUParameterQueryResponsePacket(
								packetParamList, rxPacket);
						
						// distribute update results to listeners
						for (ECUParameter p : packetParamList )
							fireProcessingParameterEvent(p);
					}

					/* Once all packets in this run are sent and received, mark the time */
					fireProcessingFinishedEvent();

					/* Reset the packet failure count */
					packetFailureCount = 0;

				}
				catch (Exception e)
				{
					packetFailureCount++;

					if (packetFailureCount >= MAX_PACKET_FAILURES)
					{
						stop();
						throw new RuntimeException(
								"There was a problem with the TX/RX packet.  Too many failures in a row: "
										+ packetFailureCount);
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
				ssmprotohandler.protocolClose();
				commPort.close();
			}
			catch (Exception e)
			{
			}
		}
	}


	
	/***********************************************************************************************
	 * This main is provided for the purpose of running a few tests, or utility methods specific to
	 * the SSM Protocol.
	 * 
	 * @param args
	 **********************************************************************************************/
/*
	public static void main(String args[])
	{

		try
		{
			
			Setup.getSetup().set(Setup.SETUP_CONFIG_MONITOR_PORT, "/dev/ttyUSB0");
			ParameterRegistry reg = new ParameterRegistry();
			
			SSMOBD2Monitor mon = new SSMOBD2Monitor();
			mon.init(reg, new InitListener("SSM Util")
			{
				public void update(String message, int step, int max)
				{
					System.out.println(message);
				}
			});
			
			System.out.println("Normal Idle Speed: " + mon.ssmph.getIdleSpeed(false));
			System.out.println("AirCon Idle Speed: " + mon.ssmph.getIdleSpeed(true));
			
			mon.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
 * */

}
