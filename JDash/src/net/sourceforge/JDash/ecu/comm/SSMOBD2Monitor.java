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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.util.UTIL;

/*******************************************************
 * This extension to the SSMMonitor is setup to
 * use the OBD-II version of SSM
 * 
 *  * http://scoobypedia.co.uk/index.php/Knowledge/ReadingAndReflashingECUs
 * http://www.scoobypedia.co.uk/index.php/Knowledge/ECUVersionCompatibilityList#toc3
 * http://www.enginuity.org/search.php?search_id=1844875639&start=105
 * http://www.vwrx.com/index.php?pg=selectmonitor
 ******************************************************/
public class SSMOBD2Monitor extends RS232Monitor
{
	
	
	/** This is the default BAUD rate for the SSM protocol */
	public static final int DEFAULT_SSM_BAUD = 4800;
	
	/** This is the number of bytes in a single SSM address request for the OBD-II protocol */
    public static final int SSM_OBD2_ADDRESS_SIZE = 3;
    
    /** This is the number of bytes in the SSM header */
    public static final int SSM_HEADER_LEN = 3;
    
	private Integer semaphore_ = new Integer(0);

    
    private String ecuId_ = null;
    
	/*******************************************************
	 * Create a new SSM OBD-II capable monitor.
	 ******************************************************/
	public SSMOBD2Monitor() throws Exception
	{
		super(DEFAULT_SSM_BAUD, RXTXPort.DATABITS_8, RXTXPort.PARITY_NONE, RXTXPort.STOPBITS_1);
	}
	
	
	   
    /*******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
     *******************************************************/
    public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
    {
    	super.init(reg, initListener);
    	
    	
		/* Setup the init packet */
    	RS232Packet txPacket = new RS232Packet();
    	txPacket.setHeader(new byte[] {(byte)0x80, (byte)0x10, (byte)0xF0});
    	txPacket.setData(new byte[] {(byte)0xBF});
    	txPacket.setCheckSum();
    	

		
		/* Send the init packet.  The datalength index value for the RX packet is the length of the txPacket, 
		 * plus the length of the SSM protocol header . This is because the init command echos our init
		 * packet, along with the regular data */
    	initListener.update("Initialize Interface", 1, 1);
		RS232Packet rxPacket = sendPacket(txPacket, SSM_HEADER_LEN);
		

		/* The rxPacket header should container the init packet, plus the last 3 bytes
		 * should be the respons header of 0x80 0xf0 0x10 */
		if (rxPacket.getHeader().length != SSM_HEADER_LEN)
		{
			throw new Exception("Init RX packet did not return the expected header\n" +
							"Expected: " + UTIL.bytesToString(txPacket.getHeader()) + 
								txPacket.getDataLength() + " " +  
								UTIL.bytesToString(txPacket.getData()) + " " +
								txPacket.getCheckSum() + " 0x80 0xf0 0x10\n" +
							"Received: " + UTIL.bytesToString(rxPacket.getHeader()));
		}
		
		
		
		/* Ecu ID */
		this.ecuId_ = String.format("0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x",
				rxPacket.getData()[1],
				rxPacket.getData()[2],
				rxPacket.getData()[3],
				rxPacket.getData()[4], 
				rxPacket.getData()[5], 
				rxPacket.getData()[6], 
				rxPacket.getData()[7], 
				rxPacket.getData()[8]);
		
		
		/* Map the init results bitmask to our known values.  For each
		 * defined ecu param */
		ArrayList<Parameter> paramList = new ArrayList<Parameter>();

		for (ECUCap cap : getEcuCapabilities())
		{
			
			/*( Skip caps that are not defined */
			if (cap.name_ == null)
			{
				continue;
			}
			
			
			/* Generate the mask */
			byte mask = (byte)(0x01 << cap.bitIndex_);
			
			/* Make sure the ECU has returned a bitmask byte for the capability we're testing */
			if (rxPacket.getData().length <= (int)cap.byteIndex_)
			{
				break;
			}
			
			/* For each bit definition */
			if ((rxPacket.getData()[cap.byteIndex_] & mask) != 0)
			{
				ECUParameter newParam = new ECUParameter((byte[])cap.address_, cap.name_, cap.description_, 1); 
				paramList.add(newParam);
			}
		}
		

		/* Return the list of parameters this monitor claims to support */
    	return paramList;
    }
    
    
    /*******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
     *******************************************************/
    public String getEcuInfo() throws Exception
    {
    	// TODO Auto-generated method stub
    	return "SSP Monitor:\nECUID: " + this.ecuId_;
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
        	
            getPort().setRTS(false);

            
            /* The main TX/RX loop */
            while(doRun_.booleanValue())
            {
                
                /* define the list of parameters that will be fetched.  This is based on if the
                 * packets rate has it ready for an update or not. */
                List<ECUParameter> thisFetchList = new ArrayList<ECUParameter>();
                for (ECUParameter param : this.params_)
                {
                	if (param.isEnabled())
                	{
	                	if (param.getLastFetchTime() + param.getPreferedRate() < System.currentTimeMillis())
	                	{
	                		param.setLastFetchTime(System.currentTimeMillis());
	                		thisFetchList.add(param);
	                	}
                	}
                }
                
                
                /* Send the TX packet, and wait for the RX packet */
                try
                {
                	
                	fireProcessingStartedEvent();
                	
                    /* Now, we need to check that we are not trying to process TOO many parameters in one go.
                     * There is a maximum number of bytes that can be sent in one packet.  If we are about to
                     * exceed that, then we'll need to chop up our parameters into multiple packets.  Since
                     * the max data size of an RS232 packet appears to be 128, then we'll send 128/3 parameters 
                     * at a time. */
                	int paramsPerPacket = (RS232Packet.MAX_DATA_LENGTH / SSM_OBD2_ADDRESS_SIZE);
                    List<ECUParameter> packetList = new ArrayList<ECUParameter>();
                    
                    for (int index = 0; index < thisFetchList.size(); index++)
                    {
                    	
                    	/* Add the current parameter to the current param list */
                    	packetList.add(thisFetchList.get(index));
                    	
                    	
                    	/* If we've hit the max, then send this packet. Or the last one too */
                    	if ((index % paramsPerPacket == 0) || (index == (thisFetchList.size() - 1)))
                    	{
                    		
                            /* Create the new TX Packet */
                            RS232Packet txPacket = createTxPacket(packetList);
                            
                            /* Send and distribute it's results */
                        	RS232Packet rxPacket = sendPacket(txPacket, SSM_HEADER_LEN);
                        	distributeResult(packetList, rxPacket);
                    		
                    		/* Reset the packet list */
                    		packetList = new ArrayList<ECUParameter>();
                    	}
                    	
                    }
                    

                    /* Once all packets in this run are sent and received, mark the time */
                    fireProcessingFinishedEvent();
                    
                    /* Reset the packet failure count */
                    packetFailureCount = 0;

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
	 * @param rxDataLenIndex IN - the datalength byte index within the RX packet that this TX packet will generate.
	 * @return the entire RX packet.
	 * @throws Exception if there was an abnormal error.
	 *******************************************************/
	private RS232Packet sendPacket(RS232Packet txPacket, int rxDataLenIndex) throws Exception
	{
		
		synchronized(this.semaphore_)
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
				
				
				/* Send the TX packet */
				txPacket.write(os);
				os.flush();
				
				/* Read the bytes on the port until we get to the start of the return packet. That means we
				 * need to first skip the tx packet */
				while ((is.available() < txPacket.length()) && (getPort() != null))
				{
					Thread.sleep(10);
				}
				readBytes(is, txPacket.length());
		
				
				/* Create the RX packet */
				RS232Packet rxPacket = new RS232Packet();
				
				/* Read the RX Header. */
				rxPacket.setHeader(readBytes(is, rxDataLenIndex));
				
				/* Read the data length byte */
				rxPacket.setDataLength(readBytes(is, 1)[0]);
				
				/* Read the data bytes */
				rxPacket.setData(readBytes(is, rxPacket.getDataLength()));
				
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
			
		} /* end semaphore */
		
			
	}
	
    
    /*******************************************************
     * @param rxPacket
     *******************************************************/
    private void distributeResult(List<ECUParameter> params, RS232Packet rxPacket)
    {
    	
    	for (int index = 0; index < params.size(); index++)
    	{
    		ECUParameter p = params.get(index);
    		
    		/* The result data is preceeded by a padding byte, thats why the +1 */
    		p.setResult(rxPacket.getData()[index + 1]);
    		
    		fireProcessingParameterEvent(p);
    	}
    }
    

    

    /********************************************************
     * @return
     *******************************************************/
    private RS232Packet createTxPacket(List<ECUParameter> params)
    {
    	RS232Packet packet = new RS232Packet();
    	
    	/* Set the header */
    	packet.setHeader(new byte[] {
    									(byte)0x80, /* padding */
    									(byte)0x10, /* destination ecu */
    									(byte)0xF0  /* source diag app */
    								});


    	/* This array list will hold our list of address bytes */
    	ArrayList<Byte> data = new ArrayList<Byte>();
    	
    	/* Add the command byte and required padding */
    	data.add((byte)0xA8);  /* Read Address Command */
    	data.add((byte)0x00);  /* Data Padding */
    	
    	/* For each parameter */
    	for (int index = 0; index < params.size(); index++)
        {
        	ECUParameter p = params.get(index);

    		/* the SSM protocol allows for ONLY 3 address bytes */
        	if (p.getAddress().length != SSM_OBD2_ADDRESS_SIZE)
        	{
        		throw new RuntimeException("This SSM Protocol allows for ONLY " + SSM_OBD2_ADDRESS_SIZE   + " address bytes.  Parameter " + p.getName() + " has " + data.size());
        	}
        
    		/* for each address byte in each parameter */
    		for (byte pAddress : p.getAddress())
    		{
    			data.add(pAddress);
    		}
    		
        }

    	/* Set the data array */
    	packet.setData(data);
    	
    	/* Set the checksum */
    	packet.setCheckSum();
    	
    	/* Return it */
    	return packet;
    	
    }
    

    
    /*******************************************************
     * Generate, and return a list with all of the ecu capabilities.
     * why not a static array list? Well, we only use this
     * list once, then we're done with it, making it a
     * returnable variable will also make it garbage collectable.
     * @return
     ******************************************************/
    private ArrayList<ECUCap> getEcuCapabilities()
    {
    	/* The byte index values are setup expecting the 5 byte ecu ID to be included in the byte array. The index starts at 0 */
    	ArrayList<ECUCap> ecuCaps = new ArrayList<ECUCap>();
    	
    	ecuCaps.add(new ECUCap(9, 7, new byte[] {0x00, 0x00, 0x07}, "LOAD", 					"Engine Load - Multiply value by 100.0 and divide by 255 to get percent"));
    	ecuCaps.add(new ECUCap(9, 6, new byte[] {0x00, 0x00, 0x08}, "COOLANT_TEMP_C",			"Coolant Temp in C - Multiply by (9/5) and add 32to get Fahrenheit"));
    	ecuCaps.add(new ECUCap(9, 5, new byte[] {0x00, 0x00, 0x09}, "AF_COR_1",				"Air/Fuel Correction #1 - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(9, 4, new byte[] {0x00, 0x00, 0x0A}, "AF_LEARN_1", 				"Air/Fuel Learning #1 - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(9, 3, new byte[] {0x00, 0x00, 0x0B}, "AF_COR_2", 				"Air/Fuel Correction #2 - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(9, 2, new byte[] {0x00, 0x00, 0x0C}, "AF_LEARN_2", 				"Air/Fuel Learning #2  - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(9, 1, new byte[] {0x00, 0x00, 0x0D}, "MAP", 					"Manifold Absolute Pressure - Multiply value by 37.0 and divide by 255 to get psig"));
    	ecuCaps.add(new ECUCap(9, 0, new byte[] {0x00, 0x00, 0x0E}, "RPM_H", 					"Engine Speed High Byte - Divide value by 4 to get RPM"));
    	ecuCaps.add(new ECUCap(9, 0, new byte[] {0x00, 0x00, 0x0F}, "RPM_L", 					"Engine Speed Low Byte - Divide value by 4 to get RPM"));
    	
    	ecuCaps.add(new ECUCap(10, 7, new byte[] {0x00, 0x00, 0x10}, "KPH", 					"Vehicle Speed in KPH"));
    	ecuCaps.add(new ECUCap(10, 6, new byte[] {0x00, 0x00, 0x11}, "IG_TIMING",				"Ignition Timing - Subtract 128 from value and divide by 2 to get degrees"));
    	ecuCaps.add(new ECUCap(10, 5, new byte[] {0x00, 0x00, 0x12}, "INTAKE_AIR_TEMP",		"Intake Air Temperature in C"));
    	ecuCaps.add(new ECUCap(10, 4, new byte[] {0x00, 0x00, 0x13}, "MAF_H", 					"Mass Air Flow High Byte - Divide value by 100.0 to get grams/s"));
    	ecuCaps.add(new ECUCap(10, 4, new byte[] {0x00, 0x00, 0x14}, "MAF_L", 					"Mass Air Flow Low Byte - Divide value by 100.0 to get grams/s"));
    	ecuCaps.add(new ECUCap(10, 3, new byte[] {0x00, 0x00, 0x15}, "TPS", 					"Throttle Opening Angle - Multiply value by 100.0 and divide by 255 to get percent"));
    	ecuCaps.add(new ECUCap(10, 2, new byte[] {0x00, 0x00, 0x16}, "FRONT_O2V_1_H",		 	"Front O2 Sensor #1 High Byte - Multiply value by 0.005 to get voltage"));
    	ecuCaps.add(new ECUCap(10, 2, new byte[] {0x00, 0x00, 0x17}, "FRONT_O2V_1_L",		 	"Front O2 Sensor #1 Low Byte - Multiply value by 0.005 to get voltage"));
    	ecuCaps.add(new ECUCap(10, 1, new byte[] {0x00, 0x00, 0x18}, "REAR_O2V_H",		 		"Rear O2 Sensor High Byte - Multiply value by 0.005 to get voltage"));
    	ecuCaps.add(new ECUCap(10, 1, new byte[] {0x00, 0x00, 0x19}, "REAR_O2V_L", 			"Rear O2 Sensor Low Byte - Multiply value by 0.005 to get voltage"));
    	ecuCaps.add(new ECUCap(10, 0, new byte[] {0x00, 0x00, 0x1A}, "FRONT_02V_2_H",		 	"Front O2 Sensor #2 High Byte - Multiply value by 0.005 to get voltage"));
    	ecuCaps.add(new ECUCap(10, 0, new byte[] {0x00, 0x00, 0x1B}, "FRONT_02V_2_L",		 	"Front O2 Sensor #2 Low Byte - Multiply value by 0.005 to get voltage"));
    	
    	ecuCaps.add(new ECUCap(11, 7, new byte[] {0x00, 0x00, 0x0C}, "BATTERY_VOLTS",			"Battery Voltage - Multiply value by 0.08 to get volts"));
    	ecuCaps.add(new ECUCap(11, 6, new byte[] {0x00, 0x00, 0x0D}, "AF_VOLTS", 				"Air Flow Sensor Voltage - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(11, 5, new byte[] {0x00, 0x00, 0x0E}, "TPS_VOLTS",				"Throttle Sensor Voltage - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(11, 4, new byte[] {0x00, 0x00, 0x0F}, "DPS_VOLTS", 				"Differential Pressure Sensor Voltage - Multiply value by 0.02 to get Volts"));
    	ecuCaps.add(new ECUCap(11, 3, new byte[] {0x00, 0x00, 0x20}, "INJ_1_PULSE",			"Fuel Injection #1 Pulse Width - Multiply value by 0.256 to get ms"));
    	ecuCaps.add(new ECUCap(11, 2, new byte[] {0x00, 0x00, 0x21}, "INJ_2_PULSE", 			"Fuel Injection #2 Pulse Width - Multiply value by 0.256 to get ms"));
    	ecuCaps.add(new ECUCap(11, 1, new byte[] {0x00, 0x00, 0x22}, "KNOCK_COR", 				"Knock Correction - Subtract 128 from value and divide by 2 to get degrees"));
    	ecuCaps.add(new ECUCap(11, 0, new byte[] {0x00, 0x00, 0x23}, "ATMO",		 			"Atmospheric Pressure - Multiply value by 37.0 and divide by 255 to get psig"));
    	
    	ecuCaps.add(new ECUCap(12, 7, new byte[] {0x00, 0x00, 0x24}, "VAC_BOOST", 				"Manifold Relative Pressure - Subtract 128 from value, multiply by 37.0 and divide by 255 to get psig"));
    	ecuCaps.add(new ECUCap(12, 6, new byte[] {0x00, 0x00, 0x25}, "DPS", 					"Pressure Differential Sensor - Subtract 128 from value, multiply by 37.0 and divide by 255 to get psig"));
    	ecuCaps.add(new ECUCap(12, 5, new byte[] {0x00, 0x00, 0x26}, "FUEL_PRESSURE",			"Fuel Tank Pressure - Subtract 128 from value and multiply by 0.0035 to get psig"));
    	ecuCaps.add(new ECUCap(12, 4, new byte[] {0x00, 0x00, 0x27}, "CO_VOLTS", 				"CO Adjustment - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(12, 3, new byte[] {0x00, 0x00, 0x28}, "LEARNED_IG_TIMING", 		"Learned Ignition Timing - Subtract 128 from value and divide by 2 to get degrees"));
    	ecuCaps.add(new ECUCap(12, 2, new byte[] {0x00, 0x00, 0x29}, "ACCEL",	 				"Accelerator Opening Angle - Divide value by 2.56 to get percent"));
    	ecuCaps.add(new ECUCap(12, 1, new byte[] {0x00, 0x00, 0x2A}, "FUEL_TEMP",				"Fuel Temperature - Subtract 40 from value to get Degrees C"));
    	ecuCaps.add(new ECUCap(12, 0, new byte[] {0x00, 0x00, 0x2B}, "FRONT_02A_1_HEATER",		"Front O2 Heater #1 - Multiply value by 10.04 and divide by 256 to get Amps"));
    	
    	ecuCaps.add(new ECUCap(13, 7, new byte[] {0x00, 0x00, 0x2C}, "REAR_O2A_HEATER",		"Rear O2 Heater Current -Multiply value by 10.04 and divide by 256 to get Amps"));
    	ecuCaps.add(new ECUCap(13, 6, new byte[] {0x00, 0x00, 0x2D}, "FRONT_O2A_2_HEATER",		"Front O2 Heater #2 - Multiply value by 10.04 and divide by 256 to get Amps"));
    	ecuCaps.add(new ECUCap(13, 5, new byte[] {0x00, 0x00, 0x2E}, "FUEL_LEVEL", 			"Fuel Level - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(13, 4, new byte[] {0x00, 0x00, 0x2F}, null, 					null));
    	ecuCaps.add(new ECUCap(13, 3, new byte[] {0x00, 0x00, 0x30}, "PRIM_WG_DUTY", 			"Primary Wastegate Duty Cycle - Multiply value by 100.0 and divide by 255 to get percent"));
    	ecuCaps.add(new ECUCap(13, 2, new byte[] {0x00, 0x00, 0x31}, "SEC_WG_DUTY",			"Secondary Wastegate Duty Cycle - Multiply value by 100.0 and divide by 255 to get percent"));
    	ecuCaps.add(new ECUCap(13, 1, new byte[] {0x00, 0x00, 0x32}, "CPC_DUTY", 				"CPC Valve Duty Ratio - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(13, 0, new byte[] {0x00, 0x00, 0x33}, "TUMB_VALVE_POS_R",		"Tumble Valve Position Sensor Right - Multiply value by 0.02 to get volts"));
    	
    	ecuCaps.add(new ECUCap(14, 7, new byte[] {0x00, 0x00, 0x34}, "TUMB_VALVE_POS_L", 		"Tumble Valve Position Sensor Left - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(14, 6, new byte[] {0x00, 0x00, 0x35}, "IDLE_SPEED_DUTY", 		"Idle Speed Control Valve Duty Ratio - Divide value by 2 to get percent"));
    	ecuCaps.add(new ECUCap(14, 5, new byte[] {0x00, 0x00, 0x36}, "AF_LEAN_COR", 			"Air/Fuel Lean Correction - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(14, 4, new byte[] {0x00, 0x00, 0x37}, "AF_HEATER_DUTY",			"Air/Fuel Heater Duty - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(14, 3, new byte[] {0x00, 0x00, 0x38}, "IDLE_STEP", 				"Idle Speed Control Valve Step - Value is in steps"));
    	ecuCaps.add(new ECUCap(14, 2, new byte[] {0x00, 0x00, 0x39}, "EX_GAS_REC_STEP", 		"Number of Ex. Gas Recirc Steps - Value is in steps"));
    	ecuCaps.add(new ECUCap(14, 1, new byte[] {0x00, 0x00, 0x3A}, "ALT_DUTY", 				"Alternator Duty - Value is in percent"));
    	ecuCaps.add(new ECUCap(14, 0, new byte[] {0x00, 0x00, 0x3B}, "FUEL_PUMP_DUTY", 		"Fuel Pump Duty - Divide value by 2.55 to get percent"));
    	
    	ecuCaps.add(new ECUCap(15, 7, new byte[] {0x00, 0x00, 0x3C}, "VVT_ADVANCE_R",			"VVT Advance Angle Right - Subtract 50 from value to get degrees"));
    	ecuCaps.add(new ECUCap(15, 6, new byte[] {0x00, 0x00, 0x3D}, "VVT_ADVANCE_L", 			"VVT Advance Angle Left- Subtract 50 from value to get degrees"));
    	ecuCaps.add(new ECUCap(15, 5, new byte[] {0x00, 0x00, 0x3E}, "OVC_DUTY_R", 			"OCV Duty Right - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(15, 4, new byte[] {0x00, 0x00, 0x3F}, "OVC_DUCY_L", 			"OCV Duty Left - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(15, 3, new byte[] {0x00, 0x00, 0x40}, "OVC_CUR_R", 				"OCV Current Right - Multiply value by 32 to get mA"));
    	ecuCaps.add(new ECUCap(15, 2, new byte[] {0x00, 0x00, 0x41}, "OVC_CUR_L", 				"OCV Current Left - Multiply value by 32 to get mA"));
    	ecuCaps.add(new ECUCap(15, 1, new byte[] {0x00, 0x00, 0x42}, "AF_1A",	 				"Air/Fuel Sensor #1 Current -Subtract 128 from value and multiply by .125 to get mA"));
    	ecuCaps.add(new ECUCap(15, 0, new byte[] {0x00, 0x00, 0x43}, "AF_2A", 					"Air/Fuel Sensor #2 Current -Subtract 128 from value and multiply by .125 to get mA"));
    	
    	ecuCaps.add(new ECUCap(16, 7, new byte[] {0x00, 0x00, 0x44}, "AF_1R", 					"Air/Fuel Sensor #1 Resistance - Value is in ohms"));
    	ecuCaps.add(new ECUCap(16, 6, new byte[] {0x00, 0x00, 0x45}, "AF_2R", 					"Air/Fuel Sensor #2 Resistance - Value is in ohms"));
    	ecuCaps.add(new ECUCap(16, 5, new byte[] {0x00, 0x00, 0x46}, "AF_1", 					"Air/Fuel Sensor #1 - Divide value by 128.0 to get Lambda"));
    	ecuCaps.add(new ECUCap(16, 4, new byte[] {0x00, 0x00, 0x47}, "AF_2", 					"Air/Fuel Sensor #2 - Divide value by 128.0 to get Lambda"));
    	ecuCaps.add(new ECUCap(16, 3, new byte[] {0x00, 0x00, (byte)0xD0}, "AF_COR_3", 		"Air/Fuel Correction #3 - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(16, 2, new byte[] {0x00, 0x00, (byte)0xD1}, "AF_LEARN_3", 		"Air/Fuel Learning #3 - Subtract 128 from value and divide by 1.28 to get percent"));
    	ecuCaps.add(new ECUCap(16, 1, new byte[] {0x00, 0x00, (byte)0xD2}, "REAR_AF_V_HEATER",	"Rear O2 Heater Voltage - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(16, 0, new byte[] {0x00, 0x00, (byte)0xD3}, "AF_ADJ_V", 		"Air/Fuel Adjustment Voltage - Multiply value by 0.02 to get voltage"));
    	
    	ecuCaps.add(new ECUCap(17, 7, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 6, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 5, new byte[] {0x00, 0x00, 0x4A}, "GEAR_POS",		 		"Gear Position - Add 1 to value to get gear"));
    	ecuCaps.add(new ECUCap(17, 4, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 3, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 2, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 1, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(17, 0, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	
    	ecuCaps.add(new ECUCap(18, 7, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(18, 6, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(18, 5, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(18, 4, new byte[] {0x00, 0x00, 0x53}, "AF_1C_HEATER", 			"Air/Fuel Sensor #1 Heater Current - Divide value by 10 to get Amps"));
    	ecuCaps.add(new ECUCap(18, 3, new byte[] {0x00, 0x00, 0x54}, "AF_2C_HEATER", 			"Air/Fuel Sensor #2 Heater Current - Divide value by 10 to get Amps"));
    	ecuCaps.add(new ECUCap(18, 2, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(18, 1, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(18, 0, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	
    	ecuCaps.add(new ECUCap(19, 7, new byte[] {0x00, 0x00, 0x00}, null,			 			null));
    	ecuCaps.add(new ECUCap(19, 6, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 5, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 4, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 3, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 2, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 1, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(19, 0, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	
    	ecuCaps.add(new ECUCap(20, 7, new byte[] {0x00, 0x00, 0x00}, null, 				null));
    	ecuCaps.add(new ECUCap(20, 6, new byte[] {0x00, 0x00, 0x61}, "AT_ID_SW", 			"AT Vehicle ID - bit 6 (01000000)"));
    	ecuCaps.add(new ECUCap(20, 5, new byte[] {0x00, 0x00, 0x61}, "TEST_MODE_CON_SW",	"Test Mode Connector - bit 5 (00100000)"));
    	ecuCaps.add(new ECUCap(20, 4, new byte[] {0x00, 0x00, 0x61}, "READ_MEM_CON_SW", 	"Read Memory Connector - bit 4 (00010000)"));
    	ecuCaps.add(new ECUCap(20, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(20, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(20, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(20, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(21, 7, new byte[] {0x00, 0x00, 0x62}, "NEUTRAL_SW", 		"Neutral Position Switch  - bit 7"));
    	ecuCaps.add(new ECUCap(21, 6, new byte[] {0x00, 0x00, 0x62}, "IDLE_SW", 			"Idle Switch - bit 6"));
    	ecuCaps.add(new ECUCap(21, 5, new byte[] {0x00, 0x00, 0x00}, null, 				null));
    	ecuCaps.add(new ECUCap(21, 4, new byte[] {0x00, 0x00, 0x62}, "IC_WASH_SW", 		"Intercooler AutoWash Switch - bit 4"));
    	ecuCaps.add(new ECUCap(21, 3, new byte[] {0x00, 0x00, 0x62}, "IG_SW", 				"Ignition Switch - bit 3"));
    	ecuCaps.add(new ECUCap(21, 2, new byte[] {0x00, 0x00, 0x62}, "PS_SW", 				"Power Steering Switch - bit 2"));
    	ecuCaps.add(new ECUCap(21, 1, new byte[] {0x00, 0x00, 0x62}, "AC_SW", 				"Air Conditioning Switch - bit 1"));
    	ecuCaps.add(new ECUCap(21, 0, new byte[] {0x00, 0x00, 0x00}, null, 				null));
    	
    	ecuCaps.add(new ECUCap(22, 7, new byte[] {0x00, 0x00, 0x63}, "HANDLE_SW", 				"Handle Switch - bit 7"));
    	ecuCaps.add(new ECUCap(22, 6, new byte[] {0x00, 0x00, 0x63}, "STARTER_SW", 			"Starter Switch - bit 6"));
    	ecuCaps.add(new ECUCap(22, 5, new byte[] {0x00, 0x00, 0x63}, "FRONT_O2_RICH_SW", 		"Front O2 Rich Signal - bit 5"));
    	ecuCaps.add(new ECUCap(22, 4, new byte[] {0x00, 0x00, 0x63}, "REAR_O2_RICH_SW", 		"Rear O2 Rich Signal - bit 4"));
    	ecuCaps.add(new ECUCap(22, 3, new byte[] {0x00, 0x00, 0x63}, "FRONT_02_RICH_2_SW", 	"Front O2 #2 Rich Signal - bit 3"));
    	ecuCaps.add(new ECUCap(22, 2, new byte[] {0x00, 0x00, 0x63}, "KNOCK_1_SW", 			"Knock Signal 1 - bit 2"));
    	ecuCaps.add(new ECUCap(22, 1, new byte[] {0x00, 0x00, 0x63}, "KNOCK_2_SW", 			"Knock Signal 2 - bit 1"));
    	ecuCaps.add(new ECUCap(22, 0, new byte[] {0x00, 0x00, 0x63}, "ELEC_LOAD_SW", 			"Electrical Load Signal - bit 0"));
    	
    	ecuCaps.add(new ECUCap(23, 7, new byte[] {0x00, 0x00, 0x64}, "CRANK_POS_SW", 		"Crank Position Sensor - bit 7"));
    	ecuCaps.add(new ECUCap(23, 6, new byte[] {0x00, 0x00, 0x64}, "CAM_POS_SW", 		"Cam Position Sensor - bit 6"));
    	ecuCaps.add(new ECUCap(23, 5, new byte[] {0x00, 0x00, 0x64}, "DEFOG_SW", 			"Defogger Switch - bit 5"));
    	ecuCaps.add(new ECUCap(23, 4, new byte[] {0x00, 0x00, 0x64}, "BLOWER_SW", 			"Blower Switch - bit 4"));
    	ecuCaps.add(new ECUCap(23, 3, new byte[] {0x00, 0x00, 0x64}, "INT_LIGHT_SW", 		"Interior Light Switch - bit 3"));
    	ecuCaps.add(new ECUCap(23, 2, new byte[] {0x00, 0x00, 0x64}, "WIPER_SW", 			"Wiper Switch - bit 2"));
    	ecuCaps.add(new ECUCap(23, 1, new byte[] {0x00, 0x00, 0x64}, "AC_LOCK_SW", 		"Air-Con Lock Signal - bit 1"));
    	ecuCaps.add(new ECUCap(23, 0, new byte[] {0x00, 0x00, 0x64}, "AC_MID_PRES_SW", 	"Air-Con Mid Pressure Switch - bit 0"));
    	
    	ecuCaps.add(new ECUCap(24, 7, new byte[] {0x00, 0x00, 0x65}, "AC_COMP_SW", 			"Air-Con Compressor Signal - bit 7"));
    	ecuCaps.add(new ECUCap(24, 6, new byte[] {0x00, 0x00, 0x65}, "RAD_FAN_3_SW", 			"Radiator Fan Relay #3 - bit 6"));
    	ecuCaps.add(new ECUCap(24, 5, new byte[] {0x00, 0x00, 0x65}, "RAD_FAN_1 SW", 			"Radiator Fan Relay #1 - bit 5"));
    	ecuCaps.add(new ECUCap(24, 4, new byte[] {0x00, 0x00, 0x65}, "RAD_FAN_2_SW", 			"Radiator Fan Relay #2 - bit 4"));
    	ecuCaps.add(new ECUCap(24, 3, new byte[] {0x00, 0x00, 0x65}, "FUEL_PUMP_SW", 			"Fuel Pump Relay - bit 3"));
    	ecuCaps.add(new ECUCap(24, 2, new byte[] {0x00, 0x00, 0x65}, "IC_WASH_SW", 			"Intercooler Auto-Wash Relay - bit 2"));
    	ecuCaps.add(new ECUCap(24, 1, new byte[] {0x00, 0x00, 0x65}, "CPC_VALVE_SW", 			"CPC Solenoid Valve - bit 1"));
    	ecuCaps.add(new ECUCap(24, 0, new byte[] {0x00, 0x00, 0x65}, "BLOW_BY_SW", 			"Blow-By Leak Connector - bit 0"));
    	
    	ecuCaps.add(new ECUCap(25, 7, new byte[] {0x00, 0x00, 0x66}, "PVC_SW", 				"PCV Solenoid Valve - bit 7"));
    	ecuCaps.add(new ECUCap(25, 6, new byte[] {0x00, 0x00, 0x66}, "TGV_OUT_SW", 			"TGV Output - bit 6"));
    	ecuCaps.add(new ECUCap(25, 5, new byte[] {0x00, 0x00, 0x66}, "TGV_DRIVE_SW", 			"TGV Drive - bit 5"));
    	ecuCaps.add(new ECUCap(25, 4, new byte[] {0x00, 0x00, 0x66}, "VAR_IA_SW", 				"Variable Intake Air Solenoid - bit 4"));
    	ecuCaps.add(new ECUCap(25, 3, new byte[] {0x00, 0x00, 0x66}, "PRESURE_CH_SW", 			"Pressure Sources Change - bit 3"));
    	ecuCaps.add(new ECUCap(25, 2, new byte[] {0x00, 0x00, 0x66}, "VENT_SOL_SW", 			"Vent Solenoid Valve - bit 2"));
    	ecuCaps.add(new ECUCap(25, 1, new byte[] {0x00, 0x00, 0x66}, "PS_SW", 					"P/S Solenoid Valve - bit 1"));
    	ecuCaps.add(new ECUCap(25, 0, new byte[] {0x00, 0x00, 0x66}, "ASSIST_AIR_SW", 			"Assist Air Solenoid Valve - bit 0"));
    	
    	ecuCaps.add(new ECUCap(26, 7, new byte[] {0x00, 0x00, 0x67}, "TANK_CTRL_SW", 			"Tank Sensor Control Valve - bit 7"));
    	ecuCaps.add(new ECUCap(26, 6, new byte[] {0x00, 0x00, 0x67}, "RELIEF_1_SW", 			"Relief Valve Solenoid 1 - bit 6"));
    	ecuCaps.add(new ECUCap(26, 5, new byte[] {0x00, 0x00, 0x67}, "RELIEF_2_SW", 			"Relief Valve Solenoid 2 - bit 5"));
    	ecuCaps.add(new ECUCap(26, 4, new byte[] {0x00, 0x00, 0x67}, "TCS_RELIEF_SW", 			"TCS Relief Valve Solenoid - bit 4"));
    	ecuCaps.add(new ECUCap(26, 3, new byte[] {0x00, 0x00, 0x67}, "EX_GAS_POS_SW", 			"Ex. Gas Positive Pressure - bit 3"));
    	ecuCaps.add(new ECUCap(26, 2, new byte[] {0x00, 0x00, 0x67}, "EX_GAS_NEG_SW", 			"Ex. Gas Negative Pressure - bit 2"));
    	ecuCaps.add(new ECUCap(26, 1, new byte[] {0x00, 0x00, 0x67}, "IA_SW", 					"Intake Air Solenoid - bit 1"));
    	ecuCaps.add(new ECUCap(26, 0, new byte[] {0x00, 0x00, 0x67}, "MUFFLER_CTL_SW", 		"Muffler Control - bit 0"));
    	
    	ecuCaps.add(new ECUCap(27, 7, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(27, 6, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(27, 5, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(27, 4, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	ecuCaps.add(new ECUCap(27, 3, new byte[] {0x00, 0x00, 0x68}, "AT_RETARD_SW", 			"Retard Signal from AT - bit 3"));
    	ecuCaps.add(new ECUCap(27, 2, new byte[] {0x00, 0x00, 0x68}, "AT_FUEL_CUT_SW", 		"Fuel Cut Signal from AT - bit 2"));
    	ecuCaps.add(new ECUCap(27, 1, new byte[] {0x00, 0x00, 0x68}, "AT_BAN_TORQUE_SW", 		"Ban of Torque Down - bit 1"));
    	ecuCaps.add(new ECUCap(27, 0, new byte[] {0x00, 0x00, 0x68}, "AT_REQ_TORQUE_SW", 		"Request Torque Down VDC - bit 0"));
    	
    	ecuCaps.add(new ECUCap(28, 7, new byte[] {0x00, 0x00, 0x69}, "TORQUE_CTRL_1_SW", 			"Torque Control Signal #1 - bit 7"));
    	ecuCaps.add(new ECUCap(28, 6, new byte[] {0x00, 0x00, 0x69}, "TORQUE_CTRL_2_SW", 			"Torque Control Signal #2 - bit 6"));
    	ecuCaps.add(new ECUCap(28, 5, new byte[] {0x00, 0x00, 0x69}, "TORQUE_PERM_SW", 			"Torque Permission Signal - bit 5"));
    	ecuCaps.add(new ECUCap(28, 4, new byte[] {0x00, 0x00, 0x69}, "EAM_SW", 					"EAM signal - bit 4"));
    	ecuCaps.add(new ECUCap(28, 3, new byte[] {0x00, 0x00, 0x69}, "AT_COOP_LOCK_SW", 			"AT coop. lock up signal - bit 3"));
    	ecuCaps.add(new ECUCap(28, 2, new byte[] {0x00, 0x00, 0x69}, "AT_COOP_LEAN_SW", 			"AT coop. lean burn signal - bit 2"));
    	ecuCaps.add(new ECUCap(28, 1, new byte[] {0x00, 0x00, 0x69}, "AT_COOP_RICH_SW", 			"AT coop. rich spike signal - bit 1"));
    	ecuCaps.add(new ECUCap(28, 0, new byte[] {0x00, 0x00, 0x69}, "AET_SW", 					"AET Signal - bit 0"));
    	
    	ecuCaps.add(new ECUCap(29, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(29, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(30, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(30, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(31, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(31, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(32, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(32, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(33, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(33, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(34, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(34, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(35, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(35, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(36, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(36, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(37, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(37, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(38, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(38, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(39, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(39, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(40, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(40, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(41, 7, new byte[] {0x00, 0x01, 0x00}, "SUB_THROT", 				"Sub Throttle Sensor - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(41, 6, new byte[] {0x00, 0x01, 0x01}, "MAIN_THROT", 			"Main Throttle Sensor - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(41, 5, new byte[] {0x00, 0x01, 0x02}, "SUB_ACCEL", 				"Sub Accelerator Sensor - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(41, 4, new byte[] {0x00, 0x01, 0x03}, "MAIN_ACCEL", 			"Main Accelerator Sensor - Multiply value by 0.02 to get volts"));
    	ecuCaps.add(new ECUCap(41, 3, new byte[] {0x00, 0x01, 0x04}, "BRAKE_BOOST", 			"Brake Booster Pressure  - Multiply value by 37.0 and divide by 255 to get psig"));
    	ecuCaps.add(new ECUCap(41, 2, new byte[] {0x00, 0x01, 0x05}, "FUEL_PRESSUSRE_HIGHT",	"Fuel Pressure (High) - Multiply value by 0.04 to get MPa"));
    	ecuCaps.add(new ECUCap(41, 1, new byte[] {0x00, 0x01, 0x06}, "EGT_C", 					"Exhaust Gas Temperature - Add 40 to value and multiply by 5 to get Degrees C"));
    	ecuCaps.add(new ECUCap(41, 0, new byte[] {0x00, 0x00, 0x00}, null, 					null));
    	
    	ecuCaps.add(new ECUCap(42, 7, new byte[] {0x00, 0x01, 0x08}, "COLD_START_INJ", 	"Cold Start Injector - Multiply value by .256 to get ms"));
    	ecuCaps.add(new ECUCap(42, 6, new byte[] {0x00, 0x01, 0x09}, "SVC_STEP", 			"SCV Step - Value is in Steps"));
    	ecuCaps.add(new ECUCap(42, 5, new byte[] {0x00, 0x01, 0x0A}, "CRUISE_KPH", 		"Memorized Cruise Speed - Value is in km/h"));
    	ecuCaps.add(new ECUCap(42, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(42, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(42, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(42, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(42, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(43, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(43, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(43, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(44, 7, new byte[] {0x00, 0x01, 0x18}, "EX_VVT_ADV_R", 			"Exhaust VVT Advance Angle Right - Subtract 50 from value to get degrees"));
    	ecuCaps.add(new ECUCap(44, 6, new byte[] {0x00, 0x01, 0x19}, "EX_VVT_ADV_L", 			"Exhaust VVT Advance Angle Left - Subtract 50 from value to get degrees"));
    	ecuCaps.add(new ECUCap(44, 5, new byte[] {0x00, 0x01, 0x1A}, "EX_OCV_DUTY_R", 			"Exhaust OCV Duty Right - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(44, 4, new byte[] {0x00, 0x01, 0x1B}, "EX_OCV_DUTY_L", 			"Exhaust OCV Duty Left - Divide value by 2.55 to get percent"));
    	ecuCaps.add(new ECUCap(44, 3, new byte[] {0x00, 0x01, 0x1C}, "EX_OCV_CUR_R", 			"Exhaust OCV Current Right - Multiply value by 32 to get mA"));
    	ecuCaps.add(new ECUCap(44, 2, new byte[] {0x00, 0x01, 0x1D}, "EX_OCV_CUR_L", 			"Exhaust OCV Current Left - Multiply value by 32 to get mA"));
    	ecuCaps.add(new ECUCap(44, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(44, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(45, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 6, new byte[] {0x00, 0x01, 0x20}, "ETC_SW", 		"ETC Motor Relay - bit 6"));
    	ecuCaps.add(new ECUCap(45, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(45, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(46, 7, new byte[] {0x00, 0x01, 0x21}, "CLUTCH_SW", 			"Clutch Switch - bit 7"));
    	ecuCaps.add(new ECUCap(46, 6, new byte[] {0x00, 0x01, 0x21}, "STOP_SW", 			"Stop Light Switch - bit 6"));
    	ecuCaps.add(new ECUCap(46, 5, new byte[] {0x00, 0x01, 0x21}, "CRUISE_SET_SW", 		"Set/Coast Switch - bit 5"));
    	ecuCaps.add(new ECUCap(46, 4, new byte[] {0x00, 0x01, 0x21}, "CRUISE_RES_SW", 		"Resume/Accelerate Switch - bit 4"));
    	ecuCaps.add(new ECUCap(46, 3, new byte[] {0x00, 0x01, 0x21}, "BRAKE_SW", 			"Brake Switch - bit 3"));
    	ecuCaps.add(new ECUCap(46, 2, new byte[] {0x00, 0x00, 0x00}, null, 				null));
    	ecuCaps.add(new ECUCap(46, 1, new byte[] {0x00, 0x01, 0x21}, "ACCEL_SW", 			"Accelerator Switch - bit 1"));
    	ecuCaps.add(new ECUCap(46, 0, new byte[] {0x00, 0x00, 0x00}, null, 				null));
    
    	ecuCaps.add(new ECUCap(47, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(47, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(48, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(48, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(49, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(49, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(51, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(51, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));

    	ecuCaps.add(new ECUCap(52, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(52, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(53, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(53, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(54, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(54, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(55, 7, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 6, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 5, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 4, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(55, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	ecuCaps.add(new ECUCap(56, 7, new byte[] {0x00, 0x00, 0x00}, "PARM", 			"Roughness Monitor Cylinder #1"));
    	ecuCaps.add(new ECUCap(56, 6, new byte[] {0x00, 0x00, 0x00}, "PARM", 			"Roughness Monitor Cylinder #2"));
    	ecuCaps.add(new ECUCap(56, 5, new byte[] {0x00, 0x00, 0x00}, "PARM", 			"Roughness Monitor Cylinder #3"));
    	ecuCaps.add(new ECUCap(56, 4, new byte[] {0x00, 0x00, 0x00}, "PARM", 			"Roughness Monitor Cylinder #4"));
    	ecuCaps.add(new ECUCap(56, 3, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(56, 2, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(56, 1, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	ecuCaps.add(new ECUCap(56, 0, new byte[] {0x00, 0x00, 0x00}, null, 			null));
    	
    	return ecuCaps;
    	
    }

    
    

    /******************************************************
     * CLASS 
     ******************************************************/
    /******************************************************
     * This internal class defines a single definition of an
     * SSM capability.
     ******************************************************/
    private static class ECUCap
    {
    	public static final long SerialVersionUID = 0L;
    	public String name_ = null;
    	public String description_ = null;
    	public int byteIndex_ = -1;
    	public int bitIndex_ = -1;
    	byte[] address_ = null;
    	
    	/******************************************************
    	 * @param name
    	 * @param description
    	 * @param byteIndex
    	 * @param bitIndex
    	 * @param address
    	 *****************************************************/
    	ECUCap(int byteIndex, int bitIndex, byte[] address, String name, String description)
    	{
    		this.name_ = name;
    		this.description_ = description;
    		this.byteIndex_ = byteIndex;
    		this.bitIndex_ = bitIndex;
    		this.address_ = address;
    		
    	}
    }

}
