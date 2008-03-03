/*******************************************************
 * 
 *  @author Gregory Ng
 *  VirtualECU.java
 *  February 28, 2008
 *
Copyright (C) 2008  Gregory Ng

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


/**
 * A base class to emulate an ECU.  Provides basic functionality to establish
 * an input and output stream to the ECU.  You should override the run() 
 * method to be a free-running daemon to process stream data.
 * 
 * JDash ECU communication modules send data to the ECU Emulator via the
 * VirtualECUInputStream is object, and receive data from the 
 * VirtualECUOutputStream os object, which are retrieved via a VirtualECUPort
 * object.
 * 
 * 
 * 
 * @author greg
 */

import java.io.*;
import java.lang.RuntimeException;


public class VirtualECU implements Runnable {
	public VirtualECUPort emuport;
	boolean bIsRunning;
	
	Thread t;
	int signal;
	
	VirtualECU() {
		signal = 0;
		bIsRunning = false;
	}
	
	public synchronized int getSignal() {
		int retVal = signal;
		signal = 0;
		return retVal;
	}
	public synchronized boolean setSignal(int sig) {
		if (signal == 0) {
			signal = sig;
			return true;
		} else {
			return false;
		}
	}
	///////////////////////////////////////////////////
	// Thread control routines
	
	/** Call this, not run, to start a thread.
	 * 
	 * @return thread of execution.
	 */
	public Thread start() {
		t = new Thread(this);
		t.setName("VirtualECU");
		t.start();
		return t;
	}
	
	/**
	 * Override of Runnable.run().  This routine handles
	 * processing of stream data.  Currently it is simply
	 * a loopback device.
	 */
	public void run() {
		System.out.println("VirtualECU started.");
		InputStream  is = emuport.getECUInputStream();
		OutputStream os = emuport.getECUOutputStream();
		bIsRunning = true;
		do {
			try {
				int nAvail = is.available();
				if (nAvail > 0) {
					System.out.println("VirtualECU proc'd " + nAvail + " bytes" );
					byte [] b = new byte[nAvail];
					is.read(b);
					os.write(b);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {e.getMessage();}			
		} while (getSignal() == 0) ;
		bIsRunning = false;
	}
	
	public Thread getThread() {
		return t;
	}
	
	void connect(VirtualECUPort emuport) throws RuntimeException{
		if (bIsRunning) 
			throw new RuntimeException("Cannot change VirtualECUPort while VirtualECU is running.");
		this.emuport = emuport;
	}
	
	//abstract void init();
	//abstract void stop();


	/**
	 * Allows you to test out this VirtualECU by sending it commands.
	 * @param emu
	 */
	public static void emulatorConsoleTest(VirtualECU emu) {
		VirtualECUPort emuport = new VirtualECUPort();
		emu.connect(emuport);
		String instr;
		System.out.println("VirtualECU Console Test\n");
		// TODO: print out the class name of the VirtualECU object under test
		
		BufferedReader brin = new BufferedReader(new InputStreamReader(System.in));
		//Writer out          = new BufferedWriter(new OutputStreamWriter(System.out));
		BufferedReader brecuin ;

		try {
			emu.start();
			emuport.open();
			brecuin = new BufferedReader(new InputStreamReader(emuport.getInputStream()));
			
			do {
				System.out.print("Input: ");
				instr = brin.readLine().trim();
				System.out.println("You wrote: " + instr);
				
				if (( instr.equalsIgnoreCase("!exit") ||
		                instr.equalsIgnoreCase("!quit")  )) {
					break;
				}
				
				emuport.getOutputStream().write((instr + "\n").getBytes());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.getMessage();
				}
				
				System.out.println("Reading ECU\n");
				//byte[] b = new byte[ instr.length() ];
				String strecu = brecuin.readLine();
				System.out.println("ECU Output: " + strecu);
				
				
			} while (1==1);
			emu.setSignal(1); // kill the emulator
		}  catch  ( IOException e )   {  
			System.out.println ( "IO Exception on Buffered Read" ) ; 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	} // end emulatorConsoleTest
	

	// Test the VirtualECU using 
	public static void main(String args[]) {
		VirtualECU emu = new VirtualECU();
		emulatorConsoleTest(emu);
	}
}
