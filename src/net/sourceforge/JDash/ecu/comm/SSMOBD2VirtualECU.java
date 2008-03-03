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
import net.sourceforge.JDash.ecu.comm.SSMOBD2ProtocolHandler.SSMPacket;
/**
 * Emulates an ECU by responding to packets
 * @author greg
 */
public class SSMOBD2VirtualECU extends VirtualECU {
	SSMOBD2ProtocolHandler ssmph;
	SSMOBD2VirtualECU() {
	}
	public void run() {
		SSMPacket rxPacket, txPacket;
		System.out.println("SSMOBD2ECUEmulator started.");
		InputStream  is = emuport.getECUInputStream();
		OutputStream os = emuport.getECUOutputStream();
		ssmph = new SSMOBD2ProtocolHandler(emuport);
		do {
			try {
				int nAvail = is.available();
				// TODO: instantiate a protocol handler.
				if (nAvail > 0) {
					rxPacket = ssmph.receivePacket(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}			
		} while (getSignal() == 0) ;		
		
	}
}
