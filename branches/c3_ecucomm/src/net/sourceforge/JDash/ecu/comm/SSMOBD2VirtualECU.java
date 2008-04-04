/*******************************************************
 * 
 *  @author Gregory Ng
 *  SSMOBD2ECUEmulator.java
 *  February 28, 2008
 *
Copyright (C) 2006  Gregory Ng

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
import java.util.List;
import java.util.ArrayList;
import net.sourceforge.JDash.ecu.comm.SSMOBD2ProtocolHandler.*;
import net.sourceforge.JDash.ecu.param.ECUParameter;
/**
 * Emulates an ECU by responding to packets
 * @author greg
 */
public class SSMOBD2VirtualECU extends VirtualECU {
	SSMOBD2ProtocolHandler ssmprotohandler = null;
	SSMOBD2VirtualECU() {
		super();
	}
	public void run() {
		SSMPacket rxPacket, txPacket;
		System.out.println("SSMOBD2ECUEmulator started.");
		InputStream  is = emuport.getECUInputStream();
		OutputStream os = emuport.getECUOutputStream();
		ssmprotohandler = new SSMOBD2ProtocolHandler(emuport);
		
		int paramsPerPacket = 
				SSMOBD2ProtocolHandler.SSMPacket.MAX_DATA_LENGTH / 
				SSMOBD2ProtocolHandler.SSM_OBD2_ADDRESS_SIZE;
		List<ECUParameter> packetParamList = new ArrayList<ECUParameter>();
		((ArrayList)packetParamList).ensureCapacity(paramsPerPacket);
		
		
		
		do {
			try 
			{
				int nAvail = is.available();
				if (nAvail > 0) 
				{
					rxPacket = ssmprotohandler.receivePacket(0);
					System.out.println("SSMOBD2VirtualECU received a packet");

					// Load packetParamList with the list of parameters that we wish
					// to query.
					SSMOBD2ProtocolHandler.decodeECUParameterQueryPacket(
							rxPacket, packetParamList);
					
					// Make up some values to return.
					for (ECUParameter p : packetParamList ) {
						p.setResult(0); // TODO: rand
					}
					txPacket = SSMOBD2ProtocolHandler.encodeECUParameterQueryResponsePacket(packetParamList);
					/* Send the TX packet */

	                // GN: apparently the ECU is supposed to echo back the request packet?
	                rxPacket.write(os);
					
										
					txPacket.write(os);
					os.flush();
					
					
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			try 
			{
				Thread.sleep(50);
			} catch (InterruptedException e) {}	
		} while (getSignal() == 0) ;
		
	}
}
