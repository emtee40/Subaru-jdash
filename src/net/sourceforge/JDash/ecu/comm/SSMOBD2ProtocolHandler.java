/*******************************************************
 *
 *  @author gng
 *  SSMOBD2ProtocolHandler.java
 *  Created 2008 02 28
 *
Copyright (C) 2008  Shane Powell

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


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.Arrays;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.util.ByteUtil;

//import net.sourceforge.JDash.
/***************************************************************************************************
 * This extension to the SSMMonitor is setup to use the OBD-II version of SSM
 * http://scoobypedia.co.uk/index.php/Knowledge/ReadingAndReflashingECUs
 * http://www.scoobypedia.co.uk/index.php/Knowledge/ECUVersionCompatibilityList#toc3
 * http://www.enginuity.org/search.php?search_id=1844875639&start=105
 * http://www.vwrx.com/index.php?pg=selectmonitor
 *************************************************************************************************
 * A ProtocolHandler implements protocols for communicating with the ECU.
 * This includes any packet formats, initialization, handshaking protocols.
 *
 * Any routine which requires knowledge of protocol state (e.g., init, close, etc.)
 * should probably be a member of the ProtocolHandler.  Anything that is just
 * packet formatting could probably be made part of the SSMPacket class.
 *
 *

 */
public class SSMOBD2ProtocolHandler
// extends ProtocolHandler
{
    public static final int DEBUGLEVEL = 0;
	// GN: I think these will probably end up being in a generic ProtocolHandler class

	public static final int STATE_NOT_READY = 1;
	public static final int STATE_READY     = 2;

	int protocolState;
	public int getProtocolState() {
		return protocolState;
	}
	// base protocol_init();
	// base protocol_close();
	// getecuid?
	// end ProtocolHandler functionality


	/** This is the default BAUD rate for the SSM protocol */
	public static final int	DEFAULT_SSM_BAUD		= 4800;


	/** This is the number of bytes in a single SSM address request for the OBD-II protocol */
	public static final int	SSM_OBD2_ADDRESS_SIZE	= 3;

	/** This is the number of bytes in the SSM packet header */
	public static final int	SSM_HEADER_LEN			= 3;

	//GN: removable?
    // Make sure that only one entity is using the protocol at a time.
	private Integer			semaphore_				= new Integer(0);
	private String			ecuId_					= null;

    /*
    // TODO: I think this should be a member of SSMPacket?
	public static final byte SSM_READ_COMMAND   = (byte) 0xA8;
	public static final byte SSM_WRITE_COMMAND 	= (byte) 0xB8;
    public static final byte SSM_INIT_COMMAND   = (byte) 0xBF;
	public static final byte SSM_DEVICE_ECU		= (byte) 0x10;
	public static final byte SSM_DEVICE_APP		= (byte) 0xF0;
    */


	private static final byte[] ADDRESS_RESET_ECU				= {(byte)0x00, (byte)0x00, (byte)0x60}; //0x00000060;
	private static final byte[] ADDRESS_IGNITION_RETARD			= {(byte)0x00, (byte)0x00, (byte)0x6f}; //0x0000006F;
	private static final byte[] ADDRESS_IDLE_SPEED_NORMAL		= {(byte)0x00, (byte)0x00, (byte)0x70}; //0x00000070;
	private static final byte[] ADDRESS_IDLE_SPEED_AIRCON		= {(byte)0x00, (byte)0x00, (byte)0x71}; //0x00000071;

	private static final ECUParameter PARAM_GET_IDLE_SPEED_NORMAL = new ECUParameter(ADDRESS_IDLE_SPEED_NORMAL, "E_IDLE_SPEED_NORMAL", "Get Normal Idle Speed", -1);
	private static final ECUParameter PARAM_GET_IDLE_SPEED_AIRCON = new ECUParameter(ADDRESS_IDLE_SPEED_AIRCON, "E_IDLE_SPEED_AIRCON", "Get With AirCon Idle Speed", -1);

	private static final int MAX_PACKET_FAILURES = 5;

    // TODO: actually, I think we want to change this to use
    // the inputstream and outputstreams.
	//BasePort comm_serial;
    public InputStream  is_;
    public OutputStream os_;


	public ArrayList<Parameter> paramList;

	public String getECUID() {
		return ecuId_;
	}

	/***********************************************************************************************
	 * Create a new SSM OBD-II capable monitor.
	 **********************************************************************************************/
    SSMOBD2ProtocolHandler()
    {
        is_ = null;
        os_ = null;
    }
	SSMOBD2ProtocolHandler(InputStream is, OutputStream os)
	{
		//comm_serial = bp;
        is_ = is;
        os_ = os;
	}


	/***********************************************************************************************
	 * Override
	 *
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
	 **********************************************************************************************/

	void protocolInit() throws Exception { //public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception {

        //super.init(reg, initListener);

		/* Setup the init packet. DST=Subaru ECU(0x10), SRC=App(0xF0),
           Command=0xBF */
        SSMPacket txPacket = SSMPacket.packetInit();
        SSMPacket rxPacket;

		/*
		 * Send the init packet. The datalength index value for the RX packet is the length of the
		 * txPacket, plus the length of the SSM protocol header . This is because the init command
		 * echos our init packet, along with the regular data
		 */
		//initListener.update("Initialize Interface", 1, 1);

        System.out.println("SSMOBD2PH::Sending init packet.");
        sendPacket(txPacket, true);
        System.out.println("SSMOBD2PH::Sent init packet.");
        System.out.println("SSMOBD2PH::waiting for init packet echo.");

        
        // GN: I have to chomp this packet.  My ECU echos the packet that
        // I just sent to it.  Maybe
        // I need to conditionally consume it (if it's seen to be an echo)?

        rxPacket = receivePacket(0);
        System.out.println(rxPacket.toString());
        
        // GN: The corresponding read hasn't yet been added to the
        // virtualecu.
        //rxPacket = receivePacket(0);

        //TODO: verify the echo'd init packet before verifying the 
        // ECU's response packet?

        /* New: The received packet header should have DST=APP(0xF0) and
         * SRC=ECU(0x10), and a data payload, of which we take bytes 2-9 as
         * the ECU ID.
		/*
		 * The rxPacket header should contain the init packet, plus the last 3 bytes should be the
		 * response header of 0x80 0xf0 0x10
		 */
        if (!rxPacket.isHeaderSourceDest(SSMPacket.SSM_DEVICE_ECU, SSMPacket.SSM_DEVICE_APP))
		{
			throw new RuntimeException("Init RX packet's source and dest incorrect"
                    + "\nReceived: " + rxPacket.toString());
		}
        if (rxPacket.getDataLength() < 9)
		{
            //
			throw new RuntimeException("Init RX packet must have at least " +
                    "9 bytes of data for ecuid. received " + rxPacket.getDataLength() +
                    " bytes. Received: " + rxPacket.toString());
		}


		/* Ecu ID */
		this.ecuId_ = String.format("0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x",
				rxPacket.getData()[1], rxPacket.getData()[2], rxPacket.getData()[3], rxPacket
						.getData()[4], rxPacket.getData()[5], rxPacket.getData()[6], rxPacket
						.getData()[7], rxPacket.getData()[8]);
        System.out.println("SSMOBD2PH verified init packet. ecuID = " + ecuId_);

		/*
		 * Map the init results bitmask to our known values. For each defined ecu param
		 */
		paramList = new ArrayList<Parameter>();

		for (ECUCap cap : getEcuCapabilities())
		{

			/* ( Skip caps that are not defined */
			if (cap.name_ == null)
			{
				continue;
			}

			/* Generate the mask */
			byte mask = (byte) (0x01 << cap.bitIndex_);

			/* Make sure the ECU has returned a bitmask byte for the capability we're testing */
			if (rxPacket.getData().length <= cap.byteIndex_)
			{
				break;
			}

			/* For each bit definition */
			if ((rxPacket.getData()[cap.byteIndex_] & mask) != 0)
			{
				ECUParameter newParam = new ECUParameter(
                        cap.address_, cap.name_,
						cap.description_, 1);
				paramList.add(newParam);
			}
		}

		// paramList is saved as a member variable!
		// return paramList;
	}

	void protocolClose() {
	}


	private int flushInputStream(InputStream is) throws IOException {
		/* Read any stale bytes on the input stream */
        if (DEBUGLEVEL >= 2) 
            System.out.println("Flushing inputstream");
		if (is.available() != 0)
		{
			// Wait for just a little bit longer.  Giving the stale
			// bytes time to complete.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			int nAvailable = is.available();
			is.skip(nAvailable);
            if (DEBUGLEVEL >= 2) 
                System.out.println("Flushed "+nAvailable+" bytes");
			return nAvailable;
		}
        if (DEBUGLEVEL >= 2) 
            System.out.println("Flushed stream.");
		return 0;
	}

	/***********************************************************************
	 * This method performs the heavylifting hard work required to send and
         * receive a SSMPacket over the provided port. RX Packets can start
         * with any number of header bytes. But the next byte MUST be a
	 * "numbytes" value. This is the number of data bytes to expect on the
         * input stream. After that number of bytes are received, the last byte
         * will be a checksum byte for the entire returned packet.
	 *
	 * This method is synchronized so that two entities won't attempt to
	 * perform two SSM requests at the same time.
	 *
	 *******************************************************
	 * From RS232Monitor... :
	 * The sendPacket method sets up a monitor timer to watch
	 * for the IO port timeint out on the TX/RX sequence.  If the
	 * timer fires, it will call this method.  Here, we'll
	 * not only break the connection, but also close the port.
	 *******************************************************
	 * sendAndReceivePacket is intended to send and receive a packet
	 * to a port in one atomic operation.
	 *
	 *
	 * @param txPacket
	 *            IN - the preformatted tx packet to be sent out the OutputStream os.
	 * @return the entire received packet.
	 * @throws Exception
	 *             if there was an abnormal error.
	 **********************************************************************************************/
	public SSMPacket sendAndReceivePacket(SSMPacket txPacket) throws Exception
	{
        return sendAndReceivePacket(txPacket, 0);
    }

    /**
     *
     * @param txPacket - packet to transmit
     * @param timeout - Timeout value in milliseconds.
     * @return SSM packet received from input stream
     * @throws java.lang.Exception
     */
    public SSMPacket sendAndReceivePacket(SSMPacket txPacket, int timeout) throws Exception
	{

       long start = (new Date()).getTime();
       long now   = start;

		synchronized (this.semaphore_)
		{

			try
			{
				/* Get the ports streams */
				OutputStream os = os_;
				InputStream  is = is_;

                if (os == null || is == null)
                    throw new NullPointerException("SSMOBD2PH: comm_serial is null");

                flushInputStream(is);

				/* Send the TX packet */
                if (DEBUGLEVEL > 1) 
                    System.out.println("Writing packet.");
				txPacket.write(os);
				os.flush();
                if (DEBUGLEVEL > 1) 
                    System.out.println("os.flush finished. is.available=()" + is.available());
				/*
				 * Read the bytes on the port until we get to the start of
				 * the return packet. That means we need to first skip the
				 * tx packet
				 */

                int nBytesAvailable=0, nLastBytesAvailable=0;
				while ((is.available() < txPacket.length()) &&
                        (now - start) < timeout
                        //&& comm_serial.isOpen()
                        )
				{
                    nBytesAvailable = is.available();
                    if ((DEBUGLEVEL > 1) && nBytesAvailable != nLastBytesAvailable) {
                        System.out.println("SSMPH: SendAndReceivePacket now has " +
                                nBytesAvailable + " bytes available");
                    }
                    nLastBytesAvailable = nBytesAvailable;

                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { }
                    start = (new Date()).getTime();
				}

                if (DEBUGLEVEL > 1)
                    System.out.println("received enough for a packet.");

				// If for some reason the port closed, then don't try to read
				// the rest of the packet.
				// if (! comm_serial.isOpen()) return null;

                // GN: apparently the ECU is supposed to echo back the request packet?
				//comm_serial.readBytes(is, txPacket.length());
				is.skip(txPacket.length());

				/* Create the RX packet */
				SSMPacket rxPacket = new SSMPacket();
				rxPacket.read(is);

				/* Check the checksum against the packet */
				if (! rxPacket.verifyChecksum())
				{
					throw new RuntimeException(
							"The checksum on the RX packet didn't match our calculations.  We calculated ["
									+ rxPacket.calcChecksum() + "]" + " but the packet has ["
									+ rxPacket.getChecksum() + "]\n" + rxPacket);
				}

				/* Return the RX packet */
				return rxPacket;
			}
			catch (Exception e)
			{

				throw new Exception(e.getClass().getName() + ": " +
						"There was a problem during the send/receive operation to SSMOBD2VirtualECU port"
                        + e.getMessage(), e);
			}

		} /* end semaphore */
	}

    /**
     * Sends txPacket on the output stream.
     * @param txPacket Packet to be sent.
     * @param bFlushInputStreamFirst if true, flushes the associated
     *        InputStream of any already-present data before sending
     *        the packet on the Outputstream.
     * @throws java.io.IOException
     */
	public void sendPacket(SSMPacket txPacket,
	                            boolean bFlushInputStreamFirst)
	                            throws IOException
	{

		synchronized (this.semaphore_)
		{
			/* Get the ports streams */
			//OutputStream os = comm_serial.getOutputStream();
			//InputStream  is = comm_serial.getInputStream();
            OutputStream os = os_;
            InputStream  is = is_;
            if (os == null || is == null)
                throw new NullPointerException("Stream is null");

			if (bFlushInputStreamFirst) flushInputStream(is);

			/* Send the TX packet */
			txPacket.write(os);
			os.flush();
		} /* end semaphore */
	}


	/**
     * Receive an SSM packet from the InputStream.  Wait for up to
     * timeout milliseconds for the packet. If timeout is 0 or negative,
     * wait indefinitely.
     *
     * @param timeout Timeout value in milliseconds
     * @return Received SSM packet
     * @throws java.io.IOException
     */
	public SSMPacket receivePacket(int timeout) throws IOException
	{
		synchronized (this.semaphore_)
		{
            InputStream is = is_;
            if (is == null) throw new NullPointerException("InputStream is null");

            // Create the RX packet
            SSMPacket rxPacket = new SSMPacket();
            rxPacket.read(is, timeout);

            /* Check the checksum against the packet */
            if (! rxPacket.verifyChecksum())
            {
                throw new IOException(
                        "Received SSM packet has bad checksum.  checksum=" + rxPacket.getChecksum() +
                        " but should be " + rxPacket.calcChecksum() + "\n" + rxPacket);
            }

            // Return the RX packet
            return rxPacket;
		} /* end semaphore */
	}

    // TODO: I think this should be a member of SSMPacket.
	/***************************************************************************
	 * @param params IN - the list of ECUParameters to be queried
	 * @param typeRead IN - Is this a READ or WRITE packet?
	 * @return a formatted SSMPacket for this query request
	 **************************************************************************/
	//private SSMPacket createTxPacket(List<ECUParameter> params, boolean read)
	public static SSMPacket encodeECUParameterQueryPacket(List<ECUParameter> params, boolean typeRead)
	{
		SSMPacket packet = new SSMPacket();

		/* Set the header */
        packet.setHeader(new byte[] { (byte) 0x80, /* padding */
                                    SSMPacket.SSM_DEVICE_ECU, /* destination ecu */
                                    SSMPacket.SSM_DEVICE_APP /* source diag app */
									});

		/* This array list will hold our list of address bytes */
		List<Byte> data = new LinkedList<Byte>();

		/* Add the command byte and required padding */
		data.add( typeRead ? SSMPacket.SSM_CMD_READ_ADDR : SSMPacket.SSM_CMD_WRITE_ADDR );
		data.add((byte) 0x00); /* Data Padding */

 		/* For each parameter */
		for (int index = 0; index < params.size(); index++)
		{
			ECUParameter p = params.get(index);

			/* the SSM protocol allows for ONLY 3 address bytes */
			if (p.getAddress().length != SSM_OBD2_ADDRESS_SIZE)
			{
				throw new RuntimeException("This SSM Protocol allows for ONLY "
						+ SSM_OBD2_ADDRESS_SIZE + " address bytes.  Parameter "
						+ p.getName() + " has " + data.size());
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
		packet.setChecksum();

		/* Return it */
		return packet;

	}


    // TODO: I think this should be a member of SSMPacket.
	/***********************************************************************************************
	 * @param packet   IN  - the packet to be decoded
	 * @param params   OUT - the list of ECUParameters to be queried
	 * @return typeRead - Is this a READ or WRITE packet?  true = read; false = write
	 **********************************************************************************************/
	public static boolean decodeECUParameterQueryPacket(SSMPacket packet, List<ECUParameter> params)
	{
		int index;
		/* Check the header */
		if (! packet.isHeaderSourceDest(SSMPacket.SSM_DEVICE_APP,SSMPacket.SSM_DEVICE_ECU))
		{
			// This isn't a properly formatted header
			throw new RuntimeException("SSMPacket: Unexpected source and dest: " + packet.toString());
		}
		if (!packet.verifyChecksum()) {
			// This isn't a properly formatted header
			throw new RuntimeException("SSMPacket: invalid data checksum: " + packet.toString());
		}

		byte[] data = packet.getData();
		// TODO: assert that the length of the data is ok

		/* Read the command byte */
		boolean typeRead;
		switch (data[0]) {
			case SSMPacket.SSM_CMD_READ_ADDR:  typeRead = true;  break;
			case SSMPacket.SSM_CMD_WRITE_ADDR: typeRead = false; break;
			default: throw new RuntimeException(
					"Invalid packet error: Command specifier (read/write) is invalid (" + data[0] + ")");
		}

		// TODO: assert data[1] == 00?
		// data.add((byte) 0x00); /* Data Padding */

		// assert that the number of address bytes is a multiple of SSM_OBD2_ADDRESS_SIZE
		int nAddressBytes = data.length - 2;
		if (nAddressBytes % SSM_OBD2_ADDRESS_SIZE != 0) {
			throw new RuntimeException("Invalid packet error: Unexpected number of address bytes.");
		}

 		/* For each address parameter, add it to the list*/
		params.clear();
 		for (index = 2; index < data.length; index += 3)
		{

	 		// read the 3-byte address
	 		byte[] address = new byte[] {data[index],data[index+1],data[index+2]};

	 		// GN: The ECUParameter class requires that you provide a name.
	 		// the name, desc, and rate aren't really meaningful here.
	 		ECUParameter p = new ECUParameter(address, "UNKNOWN_depq", "UNKNOWN_depq", -1);
	 		params.add(p);
 		}

		return typeRead;
	}



    // TODO: I think this should be a member of SSMPacket.
	/**
	 * Encode the response sent to be sent by the ECU when the ECU is responding
	 * to a parameter query event.  For use by the SSMOBD2VirtualECU.  Written by GN.
	 * TODO: compare this implementation with a specification.
	 */
	public static SSMPacket encodeECUParameterQueryResponsePacket(List<ECUParameter> params)
	{
		int index=0;
		/* The result data is preceded by a padding byte, thats why the +1 */
		byte[] data = new byte[ params.size() + 1];

		data[index++] = 0;

		for (ECUParameter p : params )
		{
			// TODO: we need to figure out how to format the result
			// as a byte?
			data[index++] = (byte)p.getResult();
		}

		SSMPacket packet = new SSMPacket();
        packet.setHeaderSource(SSMPacket.SSM_DEVICE_ECU);
        packet.setHeaderDest  (SSMPacket.SSM_DEVICE_APP);
		packet.setData(data);
        packet.setChecksum();
		return packet;
	}

	/**
	 * Decodes the response sent by the ECU when the ECU is responding
	 * to a parameter query event.
	 */

	public static void decodeECUParameterQueryResponsePacket(List<ECUParameter> params, SSMPacket rxPacket) {

		for (int index = 0; index < params.size(); index++)
		{
			ECUParameter p = params.get(index);

			/* The result data is preceded by a padding byte, thats why the +1 */
			p.setResult(rxPacket.getData()[index + 1]);
		}
	}



	/***************************************************************************
	 * @param withAirCon true to get the idle speed with the air conditioning
     *        on, false to get the idle speed with air conditioning off
	 * @return idle speed in revolutions per minute (RPM)
	 **************************************************************************/
	public long getIdleSpeed(boolean withAirCon) throws Exception
	{
		long idleSpeed = -1;
		ArrayList<ECUParameter> pList = new ArrayList<ECUParameter>();

		pList.add( withAirCon ? PARAM_GET_IDLE_SPEED_AIRCON : PARAM_GET_IDLE_SPEED_NORMAL );

		/* Create an empty packet */
		SSMPacket txPacket = encodeECUParameterQueryPacket(pList, true);


		/* Send the packet */
		System.out.println("\nTX:" + txPacket.toString());
		SSMPacket rxPacket = sendAndReceivePacket(txPacket);
		System.out.println("\nRX:" + rxPacket.toString());

		/* Get the idle speed */
		idleSpeed = ByteUtil.unsignedByteToInt(rxPacket.getData()[1]);
		idleSpeed = (idleSpeed - 128) * 25L;

		return idleSpeed;
	}

	/***************************************************************************
	 * Currently unimplemented!
     * @param withAirCon sets the idle speed
	 **************************************************************************/
	public static void setIdleSpeed(boolean withAirCon) throws Exception
	{
        throw new Exception("setIdleSpeed() is unimplemented!");
	}


	/***************************************************************************
	 * Generate, and return a list with all of the ecu capabilities. why not a 
     * static array list? Well, we only use this list once, then we're done 
     * with it, making it a returnable variable will also make it garbage 
     * collectable.
	 *
	 * @return ArrayList of the ECU capabilities
	 **************************************************************************/
	public static ArrayList<ECUCap> getEcuCapabilities()
	{
		/*
		 * The byte index values are setup expecting the 5 byte ecu ID to be included in the byte
		 * array. The index starts at 0
		 */
		ArrayList<ECUCap> ecuCaps = new ArrayList<ECUCap>();

		ecuCaps.add(new ECUCap(9, 7, new byte[] { 0x00, 0x00, 0x07 }, "LOAD",
				"Engine Load - Multiply value by 100.0 and divide by 255 to get percent"));
		ecuCaps.add(new ECUCap(9, 6, new byte[] { 0x00, 0x00, 0x08 }, "COOLANT_TEMP_C",
				"Coolant Temp in C - Multiply by (9/5) and add 32to get Fahrenheit"));
		ecuCaps
				.add(new ECUCap(9, 5, new byte[] { 0x00, 0x00, 0x09 }, "AF_COR_1",
						"Air/Fuel Correction #1 - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps
				.add(new ECUCap(9, 4, new byte[] { 0x00, 0x00, 0x0A }, "AF_LEARN_1",
						"Air/Fuel Learning #1 - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps
				.add(new ECUCap(9, 3, new byte[] { 0x00, 0x00, 0x0B }, "AF_COR_2",
						"Air/Fuel Correction #2 - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps
				.add(new ECUCap(9, 2, new byte[] { 0x00, 0x00, 0x0C }, "AF_LEARN_2",
						"Air/Fuel Learning #2  - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps
				.add(new ECUCap(9, 1, new byte[] { 0x00, 0x00, 0x0D }, "MAP",
						"Manifold Absolute Pressure - Multiply value by 37.0 and divide by 255 to get psig"));
		ecuCaps.add(new ECUCap(9, 0, new byte[] { 0x00, 0x00, 0x0E }, "RPM_H",
				"Engine Speed High Byte - Divide value by 4 to get RPM"));
		ecuCaps.add(new ECUCap(9, 0, new byte[] { 0x00, 0x00, 0x0F }, "RPM_L",
				"Engine Speed Low Byte - Divide value by 4 to get RPM"));

		ecuCaps.add(new ECUCap(10, 7, new byte[] { 0x00, 0x00, 0x10 }, "KPH",
				"Vehicle Speed in KPH"));
		ecuCaps.add(new ECUCap(10, 6, new byte[] { 0x00, 0x00, 0x11 }, "IG_TIMING",
				"Ignition Timing - Subtract 128 from value and divide by 2 to get degrees"));
		ecuCaps.add(new ECUCap(10, 5, new byte[] { 0x00, 0x00, 0x12 }, "INTAKE_AIR_TEMP",
				"Intake Air Temperature in C"));
		ecuCaps.add(new ECUCap(10, 4, new byte[] { 0x00, 0x00, 0x13 }, "MAF_H",
				"Mass Air Flow High Byte - Divide value by 100.0 to get grams/s"));
		ecuCaps.add(new ECUCap(10, 4, new byte[] { 0x00, 0x00, 0x14 }, "MAF_L",
				"Mass Air Flow Low Byte - Divide value by 100.0 to get grams/s"));
		ecuCaps
				.add(new ECUCap(10, 3, new byte[] { 0x00, 0x00, 0x15 }, "TPS",
						"Throttle Opening Angle - Multiply value by 100.0 and divide by 255 to get percent"));
		ecuCaps.add(new ECUCap(10, 2, new byte[] { 0x00, 0x00, 0x16 }, "FRONT_O2V_1_H",
				"Front O2 Sensor #1 High Byte - Multiply value by 0.005 to get voltage"));
		ecuCaps.add(new ECUCap(10, 2, new byte[] { 0x00, 0x00, 0x17 }, "FRONT_O2V_1_L",
				"Front O2 Sensor #1 Low Byte - Multiply value by 0.005 to get voltage"));
		ecuCaps.add(new ECUCap(10, 1, new byte[] { 0x00, 0x00, 0x18 }, "REAR_O2V_H",
				"Rear O2 Sensor High Byte - Multiply value by 0.005 to get voltage"));
		ecuCaps.add(new ECUCap(10, 1, new byte[] { 0x00, 0x00, 0x19 }, "REAR_O2V_L",
				"Rear O2 Sensor Low Byte - Multiply value by 0.005 to get voltage"));
		ecuCaps.add(new ECUCap(10, 0, new byte[] { 0x00, 0x00, 0x1A }, "FRONT_02V_2_H",
				"Front O2 Sensor #2 High Byte - Multiply value by 0.005 to get voltage"));
		ecuCaps.add(new ECUCap(10, 0, new byte[] { 0x00, 0x00, 0x1B }, "FRONT_02V_2_L",
				"Front O2 Sensor #2 Low Byte - Multiply value by 0.005 to get voltage"));

		ecuCaps.add(new ECUCap(11, 7, new byte[] { 0x00, 0x00, 0x0C }, "BATTERY_VOLTS",
				"Battery Voltage - Multiply value by 0.08 to get volts"));
		ecuCaps.add(new ECUCap(11, 6, new byte[] { 0x00, 0x00, 0x0D }, "AF_VOLTS",
				"Air Flow Sensor Voltage - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(11, 5, new byte[] { 0x00, 0x00, 0x0E }, "TPS_VOLTS",
				"Throttle Sensor Voltage - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(11, 4, new byte[] { 0x00, 0x00, 0x0F }, "DPS_VOLTS",
				"Differential Pressure Sensor Voltage - Multiply value by 0.02 to get Volts"));
		ecuCaps.add(new ECUCap(11, 3, new byte[] { 0x00, 0x00, 0x20 }, "INJ_1_PULSE",
				"Fuel Injection #1 Pulse Width - Multiply value by 0.256 to get ms"));
		ecuCaps.add(new ECUCap(11, 2, new byte[] { 0x00, 0x00, 0x21 }, "INJ_2_PULSE",
				"Fuel Injection #2 Pulse Width - Multiply value by 0.256 to get ms"));
		ecuCaps.add(new ECUCap(11, 1, new byte[] { 0x00, 0x00, 0x22 }, "KNOCK_COR",
				"Knock Correction - Subtract 128 from value and divide by 2 to get degrees"));
		ecuCaps.add(new ECUCap(11, 0, new byte[] { 0x00, 0x00, 0x23 }, "ATMO",
				"Atmospheric Pressure - Multiply value by 37.0 and divide by 255 to get psig"));

		ecuCaps
				.add(new ECUCap(
						12,
						7,
						new byte[] { 0x00, 0x00, 0x24 },
						"VAC_BOOST",
						"Manifold Relative Pressure - Subtract 128 from value, multiply by 37.0 and divide by 255 to get psig"));
		ecuCaps
				.add(new ECUCap(
						12,
						6,
						new byte[] { 0x00, 0x00, 0x25 },
						"DPS",
						"Pressure Differential Sensor - Subtract 128 from value, multiply by 37.0 and divide by 255 to get psig"));
		ecuCaps.add(new ECUCap(12, 5, new byte[] { 0x00, 0x00, 0x26 }, "FUEL_PRESSURE",
				"Fuel Tank Pressure - Subtract 128 from value and multiply by 0.0035 to get psig"));
		ecuCaps.add(new ECUCap(12, 4, new byte[] { 0x00, 0x00, 0x27 }, "CO_VOLTS",
				"CO Adjustment - Multiply value by 0.02 to get volts"));
		ecuCaps
				.add(new ECUCap(12, 3, new byte[] { 0x00, 0x00, 0x28 }, "LEARNED_IG_TIMING",
						"Learned Ignition Timing - Subtract 128 from value and divide by 2 to get degrees"));
		ecuCaps.add(new ECUCap(12, 2, new byte[] { 0x00, 0x00, 0x29 }, "ACCEL",
				"Accelerator Opening Angle - Divide value by 2.56 to get percent"));
		ecuCaps.add(new ECUCap(12, 1, new byte[] { 0x00, 0x00, 0x2A }, "FUEL_TEMP",
				"Fuel Temperature - Subtract 40 from value to get Degrees C"));
		ecuCaps.add(new ECUCap(12, 0, new byte[] { 0x00, 0x00, 0x2B }, "FRONT_02A_1_HEATER",
				"Front O2 Heater #1 - Multiply value by 10.04 and divide by 256 to get Amps"));

		ecuCaps.add(new ECUCap(13, 7, new byte[] { 0x00, 0x00, 0x2C }, "REAR_O2A_HEATER",
				"Rear O2 Heater Current -Multiply value by 10.04 and divide by 256 to get Amps"));
		ecuCaps.add(new ECUCap(13, 6, new byte[] { 0x00, 0x00, 0x2D }, "FRONT_O2A_2_HEATER",
				"Front O2 Heater #2 - Multiply value by 10.04 and divide by 256 to get Amps"));
		ecuCaps.add(new ECUCap(13, 5, new byte[] { 0x00, 0x00, 0x2E }, "FUEL_LEVEL",
				"Fuel Level - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(13, 4, new byte[] { 0x00, 0x00, 0x2F }, null, null));
		ecuCaps
				.add(new ECUCap(13, 3, new byte[] { 0x00, 0x00, 0x30 }, "PRIM_WG_DUTY",
						"Primary Wastegate Duty Cycle - Multiply value by 100.0 and divide by 255 to get percent"));
		ecuCaps
				.add(new ECUCap(13, 2, new byte[] { 0x00, 0x00, 0x31 }, "SEC_WG_DUTY",
						"Secondary Wastegate Duty Cycle - Multiply value by 100.0 and divide by 255 to get percent"));
		ecuCaps.add(new ECUCap(13, 1, new byte[] { 0x00, 0x00, 0x32 }, "CPC_DUTY",
				"CPC Valve Duty Ratio - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(13, 0, new byte[] { 0x00, 0x00, 0x33 }, "TUMB_VALVE_POS_R",
				"Tumble Valve Position Sensor Right - Multiply value by 0.02 to get volts"));

		ecuCaps.add(new ECUCap(14, 7, new byte[] { 0x00, 0x00, 0x34 }, "TUMB_VALVE_POS_L",
				"Tumble Valve Position Sensor Left - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(14, 6, new byte[] { 0x00, 0x00, 0x35 }, "IDLE_SPEED_DUTY",
				"Idle Speed Control Valve Duty Ratio - Divide value by 2 to get percent"));
		ecuCaps.add(new ECUCap(14, 5, new byte[] { 0x00, 0x00, 0x36 }, "AF_LEAN_COR",
				"Air/Fuel Lean Correction - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(14, 4, new byte[] { 0x00, 0x00, 0x37 }, "AF_HEATER_DUTY",
				"Air/Fuel Heater Duty - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(14, 3, new byte[] { 0x00, 0x00, 0x38 }, "IDLE_STEP",
				"Idle Speed Control Valve Step - Value is in steps"));
		ecuCaps.add(new ECUCap(14, 2, new byte[] { 0x00, 0x00, 0x39 }, "EX_GAS_REC_STEP",
				"Number of Ex. Gas Recirc Steps - Value is in steps"));
		ecuCaps.add(new ECUCap(14, 1, new byte[] { 0x00, 0x00, 0x3A }, "ALT_DUTY",
				"Alternator Duty - Value is in percent"));
		ecuCaps.add(new ECUCap(14, 0, new byte[] { 0x00, 0x00, 0x3B }, "FUEL_PUMP_DUTY",
				"Fuel Pump Duty - Divide value by 2.55 to get percent"));

		ecuCaps.add(new ECUCap(15, 7, new byte[] { 0x00, 0x00, 0x3C }, "VVT_ADVANCE_R",
				"VVT Advance Angle Right - Subtract 50 from value to get degrees"));
		ecuCaps.add(new ECUCap(15, 6, new byte[] { 0x00, 0x00, 0x3D }, "VVT_ADVANCE_L",
				"VVT Advance Angle Left- Subtract 50 from value to get degrees"));
		ecuCaps.add(new ECUCap(15, 5, new byte[] { 0x00, 0x00, 0x3E }, "OVC_DUTY_R",
				"OCV Duty Right - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(15, 4, new byte[] { 0x00, 0x00, 0x3F }, "OVC_DUCY_L",
				"OCV Duty Left - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(15, 3, new byte[] { 0x00, 0x00, 0x40 }, "OVC_CUR_R",
				"OCV Current Right - Multiply value by 32 to get mA"));
		ecuCaps.add(new ECUCap(15, 2, new byte[] { 0x00, 0x00, 0x41 }, "OVC_CUR_L",
				"OCV Current Left - Multiply value by 32 to get mA"));
		ecuCaps
				.add(new ECUCap(15, 1, new byte[] { 0x00, 0x00, 0x42 }, "AF_1A",
						"Air/Fuel Sensor #1 Current -Subtract 128 from value and multiply by .125 to get mA"));
		ecuCaps
				.add(new ECUCap(15, 0, new byte[] { 0x00, 0x00, 0x43 }, "AF_2A",
						"Air/Fuel Sensor #2 Current -Subtract 128 from value and multiply by .125 to get mA"));

		ecuCaps.add(new ECUCap(16, 7, new byte[] { 0x00, 0x00, 0x44 }, "AF_1R",
				"Air/Fuel Sensor #1 Resistance - Value is in ohms"));
		ecuCaps.add(new ECUCap(16, 6, new byte[] { 0x00, 0x00, 0x45 }, "AF_2R",
				"Air/Fuel Sensor #2 Resistance - Value is in ohms"));
		ecuCaps.add(new ECUCap(16, 5, new byte[] { 0x00, 0x00, 0x46 }, "AF_1",
				"Air/Fuel Sensor #1 - Divide value by 128.0 to get Lambda"));
		ecuCaps.add(new ECUCap(16, 4, new byte[] { 0x00, 0x00, 0x47 }, "AF_2",
				"Air/Fuel Sensor #2 - Divide value by 128.0 to get Lambda"));
		ecuCaps
				.add(new ECUCap(16, 3, new byte[] { 0x00, 0x00, (byte) 0xD0 }, "AF_COR_3",
						"Air/Fuel Correction #3 - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps
				.add(new ECUCap(16, 2, new byte[] { 0x00, 0x00, (byte) 0xD1 }, "AF_LEARN_3",
						"Air/Fuel Learning #3 - Subtract 128 from value and divide by 1.28 to get percent"));
		ecuCaps.add(new ECUCap(16, 1, new byte[] { 0x00, 0x00, (byte) 0xD2 }, "REAR_AF_V_HEATER",
				"Rear O2 Heater Voltage - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(16, 0, new byte[] { 0x00, 0x00, (byte) 0xD3 }, "AF_ADJ_V",
				"Air/Fuel Adjustment Voltage - Multiply value by 0.02 to get voltage"));

		ecuCaps.add(new ECUCap(17, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 5, new byte[] { 0x00, 0x00, 0x4A }, "GEAR_POS",
				"Gear Position - Add 1 to value to get gear"));
		ecuCaps.add(new ECUCap(17, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(17, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(18, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(18, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(18, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(18, 4, new byte[] { 0x00, 0x00, 0x53 }, "AF_1C_HEATER",
				"Air/Fuel Sensor #1 Heater Current - Divide value by 10 to get Amps"));
		ecuCaps.add(new ECUCap(18, 3, new byte[] { 0x00, 0x00, 0x54 }, "AF_2C_HEATER",
				"Air/Fuel Sensor #2 Heater Current - Divide value by 10 to get Amps"));
		ecuCaps.add(new ECUCap(18, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(18, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(18, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(19, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(19, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(20, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(20, 6, new byte[] { 0x00, 0x00, 0x61 }, "AT_ID_SW",
				"AT Vehicle ID - bit 6 (01000000)"));
		ecuCaps.add(new ECUCap(20, 5, new byte[] { 0x00, 0x00, 0x61 }, "TEST_MODE_CON_SW",
				"Test Mode Connector - bit 5 (00100000)"));
		ecuCaps.add(new ECUCap(20, 4, new byte[] { 0x00, 0x00, 0x61 }, "READ_MEM_CON_SW",
				"Read Memory Connector - bit 4 (00010000)"));
		ecuCaps.add(new ECUCap(20, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(20, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(20, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(20, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(21, 7, new byte[] { 0x00, 0x00, 0x62 }, "NEUTRAL_SW",
				"Neutral Position Switch  - bit 7"));
		ecuCaps.add(new ECUCap(21, 6, new byte[] { 0x00, 0x00, 0x62 }, "IDLE_SW",
				"Idle Switch - bit 6"));
		ecuCaps.add(new ECUCap(21, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(21, 4, new byte[] { 0x00, 0x00, 0x62 }, "IC_WASH_SW",
				"Intercooler AutoWash Switch - bit 4"));
		ecuCaps.add(new ECUCap(21, 3, new byte[] { 0x00, 0x00, 0x62 }, "IG_SW",
				"Ignition Switch - bit 3"));
		ecuCaps.add(new ECUCap(21, 2, new byte[] { 0x00, 0x00, 0x62 }, "PS_SW",
				"Power Steering Switch - bit 2"));
		ecuCaps.add(new ECUCap(21, 1, new byte[] { 0x00, 0x00, 0x62 }, "AC_SW",
				"Air Conditioning Switch - bit 1"));
		ecuCaps.add(new ECUCap(21, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(22, 7, new byte[] { 0x00, 0x00, 0x63 }, "HANDLE_SW",
				"Handle Switch - bit 7"));
		ecuCaps.add(new ECUCap(22, 6, new byte[] { 0x00, 0x00, 0x63 }, "STARTER_SW",
				"Starter Switch - bit 6"));
		ecuCaps.add(new ECUCap(22, 5, new byte[] { 0x00, 0x00, 0x63 }, "FRONT_O2_RICH_SW",
				"Front O2 Rich Signal - bit 5"));
		ecuCaps.add(new ECUCap(22, 4, new byte[] { 0x00, 0x00, 0x63 }, "REAR_O2_RICH_SW",
				"Rear O2 Rich Signal - bit 4"));
		ecuCaps.add(new ECUCap(22, 3, new byte[] { 0x00, 0x00, 0x63 }, "FRONT_02_RICH_2_SW",
				"Front O2 #2 Rich Signal - bit 3"));
		ecuCaps.add(new ECUCap(22, 2, new byte[] { 0x00, 0x00, 0x63 }, "KNOCK_1_SW",
				"Knock Signal 1 - bit 2"));
		ecuCaps.add(new ECUCap(22, 1, new byte[] { 0x00, 0x00, 0x63 }, "KNOCK_2_SW",
				"Knock Signal 2 - bit 1"));
		ecuCaps.add(new ECUCap(22, 0, new byte[] { 0x00, 0x00, 0x63 }, "ELEC_LOAD_SW",
				"Electrical Load Signal - bit 0"));

		ecuCaps.add(new ECUCap(23, 7, new byte[] { 0x00, 0x00, 0x64 }, "CRANK_POS_SW",
				"Crank Position Sensor - bit 7"));
		ecuCaps.add(new ECUCap(23, 6, new byte[] { 0x00, 0x00, 0x64 }, "CAM_POS_SW",
				"Cam Position Sensor - bit 6"));
		ecuCaps.add(new ECUCap(23, 5, new byte[] { 0x00, 0x00, 0x64 }, "DEFOG_SW",
				"Defogger Switch - bit 5"));
		ecuCaps.add(new ECUCap(23, 4, new byte[] { 0x00, 0x00, 0x64 }, "BLOWER_SW",
				"Blower Switch - bit 4"));
		ecuCaps.add(new ECUCap(23, 3, new byte[] { 0x00, 0x00, 0x64 }, "INT_LIGHT_SW",
				"Interior Light Switch - bit 3"));
		ecuCaps.add(new ECUCap(23, 2, new byte[] { 0x00, 0x00, 0x64 }, "WIPER_SW",
				"Wiper Switch - bit 2"));
		ecuCaps.add(new ECUCap(23, 1, new byte[] { 0x00, 0x00, 0x64 }, "AC_LOCK_SW",
				"Air-Con Lock Signal - bit 1"));
		ecuCaps.add(new ECUCap(23, 0, new byte[] { 0x00, 0x00, 0x64 }, "AC_MID_PRES_SW",
				"Air-Con Mid Pressure Switch - bit 0"));

		ecuCaps.add(new ECUCap(24, 7, new byte[] { 0x00, 0x00, 0x65 }, "AC_COMP_SW",
				"Air-Con Compressor Signal - bit 7"));
		ecuCaps.add(new ECUCap(24, 6, new byte[] { 0x00, 0x00, 0x65 }, "RAD_FAN_3_SW",
				"Radiator Fan Relay #3 - bit 6"));
		ecuCaps.add(new ECUCap(24, 5, new byte[] { 0x00, 0x00, 0x65 }, "RAD_FAN_1 SW",
				"Radiator Fan Relay #1 - bit 5"));
		ecuCaps.add(new ECUCap(24, 4, new byte[] { 0x00, 0x00, 0x65 }, "RAD_FAN_2_SW",
				"Radiator Fan Relay #2 - bit 4"));
		ecuCaps.add(new ECUCap(24, 3, new byte[] { 0x00, 0x00, 0x65 }, "FUEL_PUMP_SW",
				"Fuel Pump Relay - bit 3"));
		ecuCaps.add(new ECUCap(24, 2, new byte[] { 0x00, 0x00, 0x65 }, "IC_WASH_SW",
				"Intercooler Auto-Wash Relay - bit 2"));
		ecuCaps.add(new ECUCap(24, 1, new byte[] { 0x00, 0x00, 0x65 }, "CPC_VALVE_SW",
				"CPC Solenoid Valve - bit 1"));
		ecuCaps.add(new ECUCap(24, 0, new byte[] { 0x00, 0x00, 0x65 }, "BLOW_BY_SW",
				"Blow-By Leak Connector - bit 0"));

		ecuCaps.add(new ECUCap(25, 7, new byte[] { 0x00, 0x00, 0x66 }, "PVC_SW",
				"PCV Solenoid Valve - bit 7"));
		ecuCaps.add(new ECUCap(25, 6, new byte[] { 0x00, 0x00, 0x66 }, "TGV_OUT_SW",
				"TGV Output - bit 6"));
		ecuCaps.add(new ECUCap(25, 5, new byte[] { 0x00, 0x00, 0x66 }, "TGV_DRIVE_SW",
				"TGV Drive - bit 5"));
		ecuCaps.add(new ECUCap(25, 4, new byte[] { 0x00, 0x00, 0x66 }, "VAR_IA_SW",
				"Variable Intake Air Solenoid - bit 4"));
		ecuCaps.add(new ECUCap(25, 3, new byte[] { 0x00, 0x00, 0x66 }, "PRESURE_CH_SW",
				"Pressure Sources Change - bit 3"));
		ecuCaps.add(new ECUCap(25, 2, new byte[] { 0x00, 0x00, 0x66 }, "VENT_SOL_SW",
				"Vent Solenoid Valve - bit 2"));
		ecuCaps.add(new ECUCap(25, 1, new byte[] { 0x00, 0x00, 0x66 }, "PS_SW",
				"P/S Solenoid Valve - bit 1"));
		ecuCaps.add(new ECUCap(25, 0, new byte[] { 0x00, 0x00, 0x66 }, "ASSIST_AIR_SW",
				"Assist Air Solenoid Valve - bit 0"));

		ecuCaps.add(new ECUCap(26, 7, new byte[] { 0x00, 0x00, 0x67 }, "TANK_CTRL_SW",
				"Tank Sensor Control Valve - bit 7"));
		ecuCaps.add(new ECUCap(26, 6, new byte[] { 0x00, 0x00, 0x67 }, "RELIEF_1_SW",
				"Relief Valve Solenoid 1 - bit 6"));
		ecuCaps.add(new ECUCap(26, 5, new byte[] { 0x00, 0x00, 0x67 }, "RELIEF_2_SW",
				"Relief Valve Solenoid 2 - bit 5"));
		ecuCaps.add(new ECUCap(26, 4, new byte[] { 0x00, 0x00, 0x67 }, "TCS_RELIEF_SW",
				"TCS Relief Valve Solenoid - bit 4"));
		ecuCaps.add(new ECUCap(26, 3, new byte[] { 0x00, 0x00, 0x67 }, "EX_GAS_POS_SW",
				"Ex. Gas Positive Pressure - bit 3"));
		ecuCaps.add(new ECUCap(26, 2, new byte[] { 0x00, 0x00, 0x67 }, "EX_GAS_NEG_SW",
				"Ex. Gas Negative Pressure - bit 2"));
		ecuCaps.add(new ECUCap(26, 1, new byte[] { 0x00, 0x00, 0x67 }, "IA_SW",
				"Intake Air Solenoid - bit 1"));
		ecuCaps.add(new ECUCap(26, 0, new byte[] { 0x00, 0x00, 0x67 }, "MUFFLER_CTL_SW",
				"Muffler Control - bit 0"));

		ecuCaps.add(new ECUCap(27, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(27, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(27, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(27, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(27, 3, new byte[] { 0x00, 0x00, 0x68 }, "AT_RETARD_SW",
				"Retard Signal from AT - bit 3"));
		ecuCaps.add(new ECUCap(27, 2, new byte[] { 0x00, 0x00, 0x68 }, "AT_FUEL_CUT_SW",
				"Fuel Cut Signal from AT - bit 2"));
		ecuCaps.add(new ECUCap(27, 1, new byte[] { 0x00, 0x00, 0x68 }, "AT_BAN_TORQUE_SW",
				"Ban of Torque Down - bit 1"));
		ecuCaps.add(new ECUCap(27, 0, new byte[] { 0x00, 0x00, 0x68 }, "AT_REQ_TORQUE_SW",
				"Request Torque Down VDC - bit 0"));

		ecuCaps.add(new ECUCap(28, 7, new byte[] { 0x00, 0x00, 0x69 }, "TORQUE_CTRL_1_SW",
				"Torque Control Signal #1 - bit 7"));
		ecuCaps.add(new ECUCap(28, 6, new byte[] { 0x00, 0x00, 0x69 }, "TORQUE_CTRL_2_SW",
				"Torque Control Signal #2 - bit 6"));
		ecuCaps.add(new ECUCap(28, 5, new byte[] { 0x00, 0x00, 0x69 }, "TORQUE_PERM_SW",
				"Torque Permission Signal - bit 5"));
		ecuCaps.add(new ECUCap(28, 4, new byte[] { 0x00, 0x00, 0x69 }, "EAM_SW",
				"EAM signal - bit 4"));
		ecuCaps.add(new ECUCap(28, 3, new byte[] { 0x00, 0x00, 0x69 }, "AT_COOP_LOCK_SW",
				"AT coop. lock up signal - bit 3"));
		ecuCaps.add(new ECUCap(28, 2, new byte[] { 0x00, 0x00, 0x69 }, "AT_COOP_LEAN_SW",
				"AT coop. lean burn signal - bit 2"));
		ecuCaps.add(new ECUCap(28, 1, new byte[] { 0x00, 0x00, 0x69 }, "AT_COOP_RICH_SW",
				"AT coop. rich spike signal - bit 1"));
		ecuCaps.add(new ECUCap(28, 0, new byte[] { 0x00, 0x00, 0x69 }, "AET_SW",
				"AET Signal - bit 0"));

		ecuCaps.add(new ECUCap(29, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(29, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(30, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(30, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(31, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(31, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(32, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(32, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(33, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(33, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(34, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(34, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(35, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(35, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(36, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(36, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(37, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(37, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(38, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(38, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(39, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(39, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(40, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(40, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(41, 7, new byte[] { 0x00, 0x01, 0x00 }, "SUB_THROT",
				"Sub Throttle Sensor - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(41, 6, new byte[] { 0x00, 0x01, 0x01 }, "MAIN_THROT",
				"Main Throttle Sensor - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(41, 5, new byte[] { 0x00, 0x01, 0x02 }, "SUB_ACCEL",
				"Sub Accelerator Sensor - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(41, 4, new byte[] { 0x00, 0x01, 0x03 }, "MAIN_ACCEL",
				"Main Accelerator Sensor - Multiply value by 0.02 to get volts"));
		ecuCaps.add(new ECUCap(41, 3, new byte[] { 0x00, 0x01, 0x04 }, "BRAKE_BOOST",
				"Brake Booster Pressure  - Multiply value by 37.0 and divide by 255 to get psig"));
		ecuCaps.add(new ECUCap(41, 2, new byte[] { 0x00, 0x01, 0x05 }, "FUEL_PRESSUSRE_HIGHT",
				"Fuel Pressure (High) - Multiply value by 0.04 to get MPa"));
		ecuCaps.add(new ECUCap(41, 1, new byte[] { 0x00, 0x01, 0x06 }, "EGT_C",
				"Exhaust Gas Temperature - Add 40 to value and multiply by 5 to get Degrees C"));
		ecuCaps.add(new ECUCap(41, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(42, 7, new byte[] { 0x00, 0x01, 0x08 }, "COLD_START_INJ",
				"Cold Start Injector - Multiply value by .256 to get ms"));
		ecuCaps.add(new ECUCap(42, 6, new byte[] { 0x00, 0x01, 0x09 }, "SVC_STEP",
				"SCV Step - Value is in Steps"));
		ecuCaps.add(new ECUCap(42, 5, new byte[] { 0x00, 0x01, 0x0A }, "CRUISE_KPH",
				"Memorized Cruise Speed - Value is in km/h"));
		ecuCaps.add(new ECUCap(42, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(42, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(42, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(42, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(42, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(43, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(43, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(43, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(44, 7, new byte[] { 0x00, 0x01, 0x18 }, "EX_VVT_ADV_R",
				"Exhaust VVT Advance Angle Right - Subtract 50 from value to get degrees"));
		ecuCaps.add(new ECUCap(44, 6, new byte[] { 0x00, 0x01, 0x19 }, "EX_VVT_ADV_L",
				"Exhaust VVT Advance Angle Left - Subtract 50 from value to get degrees"));
		ecuCaps.add(new ECUCap(44, 5, new byte[] { 0x00, 0x01, 0x1A }, "EX_OCV_DUTY_R",
				"Exhaust OCV Duty Right - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(44, 4, new byte[] { 0x00, 0x01, 0x1B }, "EX_OCV_DUTY_L",
				"Exhaust OCV Duty Left - Divide value by 2.55 to get percent"));
		ecuCaps.add(new ECUCap(44, 3, new byte[] { 0x00, 0x01, 0x1C }, "EX_OCV_CUR_R",
				"Exhaust OCV Current Right - Multiply value by 32 to get mA"));
		ecuCaps.add(new ECUCap(44, 2, new byte[] { 0x00, 0x01, 0x1D }, "EX_OCV_CUR_L",
				"Exhaust OCV Current Left - Multiply value by 32 to get mA"));
		ecuCaps.add(new ECUCap(44, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(44, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(45, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 6, new byte[] { 0x00, 0x01, 0x20 }, "ETC_SW",
				"ETC Motor Relay - bit 6"));
		ecuCaps.add(new ECUCap(45, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(45, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(46, 7, new byte[] { 0x00, 0x01, 0x21 }, "CLUTCH_SW",
				"Clutch Switch - bit 7"));
		ecuCaps.add(new ECUCap(46, 6, new byte[] { 0x00, 0x01, 0x21 }, "STOP_SW",
				"Stop Light Switch - bit 6"));
		ecuCaps.add(new ECUCap(46, 5, new byte[] { 0x00, 0x01, 0x21 }, "CRUISE_SET_SW",
				"Set/Coast Switch - bit 5"));
		ecuCaps.add(new ECUCap(46, 4, new byte[] { 0x00, 0x01, 0x21 }, "CRUISE_RES_SW",
				"Resume/Accelerate Switch - bit 4"));
		ecuCaps.add(new ECUCap(46, 3, new byte[] { 0x00, 0x01, 0x21 }, "BRAKE_SW",
				"Brake Switch - bit 3"));
		ecuCaps.add(new ECUCap(46, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(46, 1, new byte[] { 0x00, 0x01, 0x21 }, "ACCEL_SW",
				"Accelerator Switch - bit 1"));
		ecuCaps.add(new ECUCap(46, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(47, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(47, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(48, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(48, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(49, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(49, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(51, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(51, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(52, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(52, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(53, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(53, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(54, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(54, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(55, 7, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 6, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 5, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 4, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(55, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		ecuCaps.add(new ECUCap(56, 7, new byte[] { 0x00, 0x00, 0x00 }, "PARM",
				"Roughness Monitor Cylinder #1"));
		ecuCaps.add(new ECUCap(56, 6, new byte[] { 0x00, 0x00, 0x00 }, "PARM",
				"Roughness Monitor Cylinder #2"));
		ecuCaps.add(new ECUCap(56, 5, new byte[] { 0x00, 0x00, 0x00 }, "PARM",
				"Roughness Monitor Cylinder #3"));
		ecuCaps.add(new ECUCap(56, 4, new byte[] { 0x00, 0x00, 0x00 }, "PARM",
				"Roughness Monitor Cylinder #4"));
		ecuCaps.add(new ECUCap(56, 3, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(56, 2, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(56, 1, new byte[] { 0x00, 0x00, 0x00 }, null, null));
		ecuCaps.add(new ECUCap(56, 0, new byte[] { 0x00, 0x00, 0x00 }, null, null));

		return ecuCaps;

	}

	/***********************************************************************************************
	 * CLASS
	 **********************************************************************************************/
	/***********************************************************************************************
	 * This internal class defines a single definition of an SSM capability.
	 **********************************************************************************************/
	public static class ECUCap
	{
		public static final long	SerialVersionUID	= 0L;
		public String				name_				= null;
		public String				description_		= null;
		public int					byteIndex_			= -1;
		public int					bitIndex_			= -1;
		byte[]						address_			= null;

		/*******************************************************************************************
		 * @param name
		 * @param description
		 * @param byteIndex
		 * @param bitIndex
		 * @param address
		 ******************************************************************************************/
		ECUCap(int byteIndex, int bitIndex, byte[] address, String name, String description)
		{
			this.name_ = name;
			this.description_ = description;
			this.byteIndex_ = byteIndex;
			this.bitIndex_ = bitIndex;
			this.address_ = address;

		}

        public long getAddressAsLong()
        {
            return ByteUtil.byteArrayToLongBE(address_);
        }
	}




    // TODO: give the SSMPacket its own class file
	public static class SSMPacket implements ByteStreamable, Cloneable
	{

        ////////////////////////////////////////////////
        // HEADER Source and destination fields:
        public static final byte SSM_DEVICE_ECU		= (byte) 0x10;
        public static final byte SSM_DEVICE_APP		= (byte) 0xF0;

        ////////////////////////////////////////////////
        // Commands

        // SSM_CMD_READ_MEM: Read a block of memory
        public static final byte SSM_CMD_READ_MEM   = (byte) 0xA0;
        public static final byte SSM_CMD_READ_ADDR  = (byte) 0xA8;
        public static final byte SSM_CMD_WRITE_MEM  = (byte) 0xB0;
        public static final byte SSM_CMD_WRITE_ADDR = (byte) 0xB8;
        public static final byte SSM_CMD_INIT       = (byte) 0xBF;

		public static int SSM_HEADER_LEN = 3;
		// This is the theoretical minimum.  You don't need to block on a read
		// until you have this many bits available.
		public static int SSM_PACKET_MIN_LENGTH = SSM_HEADER_LEN + 1 + 1;

		/** This is the maximum number of bytes allowed in the data array */
		public static final int MAX_DATA_LENGTH = 128;


		private byte[] fieldHeader;
		private int    fieldDataLen  = 0;
		private byte[] fieldData     = null;
		private byte   fieldChecksum = 0;


		/******************************************************
		 * Create a new empty packet
		 ******************************************************/
		public SSMPacket()
		{
			fieldHeader = new byte[SSM_HEADER_LEN];
            fieldHeader[0] = (byte)0x80;
		}


        /******************************************************
         * @return header field of SSM packet
         ******************************************************/
        public byte[] getHeader()
        {
            return this.fieldHeader;
        }

        /*******************************************************
         * @param header byte array representing the header value.
         *        It will only use the first SSM_HEADER_LEN bytes
         *        to set the header.
         ******************************************************/
        public void setHeader(byte[] header)
        {
            for (int i=0; i < SSM_HEADER_LEN; i++)
                this.fieldHeader[i] = header[i];
        }

        public void setHeaderDest(byte dest)
        {
            this.fieldHeader[1] = dest;
        }

        public void setHeaderSource(byte src)
        {
            this.fieldHeader[2] = src;
        }

        public byte getHeaderDest()
        {
            return this.fieldHeader[1];
        }

        public byte getHeaderSource()
        {
            return this.fieldHeader[2];
        }

        public boolean isHeaderSourceDest(byte src, byte dest)
        {
            return ((this.fieldHeader[1] == dest) &&
                    (this.fieldHeader[2] == src));
        }

        /*******************************************************
         * @return data length field of SSM packet
         ******************************************************/
        public int getDataLength()
        {
            return this.fieldDataLen;
        }

        /*******************************************************
         * @param b byte value for SSM packet
         ******************************************************/
        public void setDataLength(byte b)
        {
            setDataLength(ByteUtil.unsignedByteToInt(b));
        }

        public void setDataLength(int len)
        {
            if (len > MAX_DATA_LENGTH)
            {
                throw new RuntimeException(
                        "Maximum of [" + MAX_DATA_LENGTH +
                        "] data bytes exceeded in SSMPacket. ["
                        + len + "]");
            }

            this.fieldDataLen = len;
        }

		/*******************************************************
		 * @return data field of the SSMPacket
		 ******************************************************/
		public byte[] getData()
		{
			return this.fieldData;
		}

		/********************************************************
		 * Set the data array to the given data array value.
		 * The setDataLength() method will be called automatically.
		 * @param data
		 ******************************************************/
		public void setData(byte[] data)
		{
			fieldData = data;
			setDataLength(data.length);
		}

		/*******************************************************
		 * @param data
		 ******************************************************/
		public void setData(List<Byte> data)
		{
			byte[] dataArray = new byte[data.size()];

			for (int index = 0; index < data.size(); index++)
			{
				dataArray[index] = data.get(index);
			}

			setData(dataArray);
		}

		/*******************************************************
		 * @return Read the checksum field
		 ******************************************************/
		public byte getChecksum()
		{
			return this.fieldChecksum;
		}

		/*******************************************************
		 * @param checksum 
		 ******************************************************/
		public void setChecksum(byte checksum)
		{
			this.fieldChecksum = checksum;
		}

		/******************************************************
		 * Given the current state of this packet, calcualte and set
		 * it's checksum.
		 ******************************************************/
		public void setChecksum()
		{
			this.fieldChecksum = calcChecksum();
		}


		/*******************************************************
		 * Given the current state of the header, datalength, and data values,
		 * this method will calculate the checksum and return it.
		 *******************************************************/
		public byte calcChecksum()
		{
			/* Check the checksum against the packet */
			byte checksum = 0;
			int i;
			for (i=0; i < fieldHeader.length; i++)
				checksum += fieldHeader[i];

			checksum += (byte)fieldDataLen;

			for (i=0; i < fieldData.length; i++)
				checksum += fieldData[i];

			return checksum;
		}

		public boolean verifyChecksum() {
			return (fieldChecksum == calcChecksum());
		}

		/*******************************************************
		 * Get the entire length of this packet. This
		 * is the length of the header array, plus 1 for the
		 * data length byte, plus the length of the data array
		 * plus 1 for the checksum byte.
		 * @return length of packet
		 *******************************************************/
		public int length()
		{
			return fieldHeader.length + 1 + fieldData.length + 1;
		}


		/*******************************************************
		 * This is a simple convience method to write the contents
		 * of this packet to the given output steam.
		 * @param os
		 *******************************************************/
		public void write(OutputStream os) throws IOException
		{
			/* Send the TX packet */
            byte[] pdata = new byte[length()];
            byte[] temp;
            int i=0,j=0;
            
            temp = getHeader();
            for (i=0; i < temp.length; i++) pdata[j++] = temp[i];
            
            pdata[j++] = (byte) getDataLength();
            
            temp = getData();
            for (i=0; i < temp.length; i++) pdata[j++] = temp[i];

            pdata[j++] = getChecksum();
            if (DEBUGLEVEL >= 1) System.out.println("Writing packet: " + pdata.length + " bytes");
            os.write(pdata);
/*			os.write(getHeader());
			os.write((byte)getDataLength());
			os.write(getData());
			os.write(getChecksum());
 */
		}

        /**
         * Read a packet from the InputStream with no timeout.
         * @param is InputStream to read from
         * @throws java.io.IOException
         */
		public void read(InputStream is) throws IOException
		{
            read(is, 0);
		}
        /**
         * Read a packet from the InputStream, waiting for up to timeout milliseconds
         * @param is InputStream to read from
         * @param timeout timeout value in milliseconds. A non-positive value
         * indicates an unlimited timeout.
         * @throws java.io.IOException
         */
		public void read(InputStream is, int timeout) throws IOException
		{
            long tlimit = (timeout > 0) ?
                ((new Date()).getTime() + timeout)
                :
                0;
            if (DEBUGLEVEL >= 2) System.out.println("Waiting for input for " + timeout + " ms.");
            
            waitForInput(is, (SSM_HEADER_LEN+1), tlimit);

			is.read(fieldHeader, 0, SSM_HEADER_LEN); // header
			setDataLength( is.read() );              // data length

			// Reallocate the data field if necessary
			if (fieldData == null || fieldData.length != fieldDataLen)
				fieldData = new byte[ fieldDataLen ];

            waitForInput(is, fieldDataLen+1, tlimit);

            is.read( fieldData, 0, fieldDataLen );   // data field
			fieldChecksum = (byte)(is.read() & 0xff);// checksum
		}

        /**
         * Waits until the Inputstream is either has nAvail bytes available,
         * or (new Date).getTime() passes tLimit.
         * @param is an inputstream object
         * @param nAvail Number of bytes to wait for
         * @param tlimit Timestamp to wait until. tlimit = 0 means no timeout.
         * @throws java.io.IOException
         */
        public static void waitForInput(InputStream is, int nAvail, long tlimit)
                throws IOException
        {
            while (is.available() < nAvail) {
                if (tlimit != 0 && (new Date()).getTime() > tlimit)
                    throw new IOException("SSMPacket read timed out.");
                try { Thread.sleep(10); }
                catch (InterruptedException e) {}
            }

        }
		/******************************************************
		 * Override
		 * @see java.lang.Object#toString()
		 ******************************************************/
		@Override
		public String toString()
		{
			String ret = "";

			ret = "\nSSMPacket";
			ret += "\nH: " + ByteUtil.bytesToString(fieldHeader);
			ret += "\nL: " + getDataLength();
			ret += "\nD: " + ByteUtil.bytesToString(fieldData);
			ret += "\nC: " + ByteUtil.unsignedByteToInt(fieldChecksum);

			return ret;
		}


        @Override
        public SSMPacket clone()
        {
            SSMPacket packet = new SSMPacket();

            packet.fieldHeader   = new byte[this.fieldHeader.length];
            packet.fieldDataLen  = this.fieldDataLen;
            packet.fieldData     = new byte[this.fieldData.length];
            packet.fieldChecksum = this.fieldChecksum;
            int i;

            for (i=0; i < this.fieldHeader.length; i++)
                packet.fieldHeader[i] = this.fieldHeader[i];
            for (i=0; i < this.fieldData.length; i++)
                packet.fieldData[i] = this.fieldData[i];

            return packet;
        }
        
        public boolean equals(SSMPacket packet)
        {
            if (fieldDataLen  != packet.fieldDataLen)
                return false;

            return (
                    fieldChecksum == packet.fieldChecksum &&
                    Arrays.equals(fieldHeader, packet.fieldHeader) &&
                    Arrays.equals(fieldData  , packet.fieldData)
                    );
        }

        ////////////////////////////////////////////////////////
        /// Packet construction methods

        /**
         * 
         * @return an initialization packet from the APP to the ECU
         */
        public static SSMPacket packetInit()
        {
            SSMPacket packetInit = new SSMPacket(); //RS232Packet txPacket = new RS232Packet();
            packetInit.setHeader(new byte[] { (byte) 0x80, SSM_DEVICE_ECU, SSM_DEVICE_APP });
            packetInit.setData(new byte[] { SSM_CMD_INIT });
            packetInit.setChecksum();
            return packetInit;
        }

	}

    // Not sure if we'll need this class.
    //public static class SSMQuery implements Cloneable {
    //    // Source and Destination Devices
    //    public byte fieldSourceDevice;
    //    public byte fieldDestDevice;
    //
    //
    //    // GENERIC FIELDS
    //    // Command
    //    public byte fieldCommand;
    //
    //    // Init Response commands
    //    public byte fieldInitResponse;
    //
    //
    //    // Address Response commands
    //}


}
