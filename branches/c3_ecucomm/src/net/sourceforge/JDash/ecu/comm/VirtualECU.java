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

// for VirtualECUConsole
import javax.swing.*;
import java.awt.*;
import net.sourceforge.JDash.ecu.param.*;


public class VirtualECU implements Runnable {
	protected VirtualECUPort emuport    = null;
	protected boolean        bIsRunning = false;
    
    public boolean           bShowDebugConsole = true;
    public VECUConsoleFrame  vecuConsoleFrame  = null;
	
    public ParameterRegistry paramRegistry_ = null;
    
	Thread t      = null;
	int    signal = 0;;
	
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
	
	public boolean isRunning() {
		return bIsRunning;
	}
	
	///////////////////////////////////////////////////
	// Thread control routines
	
	/** Call this, not run, to start a thread.
	 * 
	 * @return thread of execution.
	 */
	public Thread start() {
        System.out.println( this.getClass().getName() + ".start();");
        if (bShowDebugConsole) 
        {
            System.out.println("Creating VirtualECU Console Frame");
            vecuConsoleFrame = new VECUConsoleFrame();
        }
		t = new Thread(this);
		t.setName("VirtualECU");
		t.start();
		return t;
	}
	
	/**
	 * Override of Runnable.run().  This routine handles
	 * processing of stream data.  Currently it is simply
	 * a in a loopback mode.
	 * 
	 * You should override this routine in a similar way.
	 */
	public void run() {
		System.out.println("VirtualECU started.");
        if (emuport == null) {
            System.out.println("VirtualECU waiting for connection to VirtualECUPort");
            while (emuport == null && getSignal() == 0) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
            }
            if (getSignal() != 0) return;
        }
        
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
	/**
	* Connect this ECU to a VirtualPort object.  You will need to call this method
	* before calling the start() method.  This method cannot be called while the 
	* VirtualECU is running.
	*/
	public void connect(VirtualECUPort emuport) throws IOException {
		if (bIsRunning) 
			throw new RuntimeException("Cannot change VirtualECUPort while VirtualECU is running.");
		this.emuport = emuport;
        if (!emuport.ecuOpen())
            throw new RuntimeException("VirtualECUPort.ecuOpen returned false");
	}
	
	//abstract void init();
	//abstract void stop();


	/**
	 * Allows you to test out this VirtualECU by sending it commands.
	 * @param emu
	 */
	public static void emulatorConsoleTest(VirtualECU emu) throws IOException {
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
			System.out.println ( "IOException on Buffered Read" ) ; 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	} // end emulatorConsoleTest

    /// A GUI class for showing what's going on inside the VirtualECU
    public static class VECUConsoleFrame extends JFrame {
        
        protected JTextField textConsole;
        
        public VECUConsoleFrame() {
            super();
            /* Set the frame title */
            setTitle("VirtualECU Console");
            //setIconImage(new ImageIcon(ICON).getImage());
            initComponents();
        }
        void initComponents() {
            textConsole = new JTextField();
            
            ////////////////////////////////
            // Init Layout
            /* Init the content pane */
            JPanel contentPanel = new JPanel();
            setContentPane(contentPanel);
            contentPanel.setLayout(new BorderLayout());

               
            /* Setup the main content panel */
            JPanel mainPanel = new JPanel(new GridBagLayout());
            JScrollPane mainScrollPane = new JScrollPane(mainPanel);
            getContentPane().add(mainScrollPane);
            mainScrollPane.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                    );
            
            mainPanel.add(textConsole);
            
            
    		pack();
        } // end initComponents()
                   
        
    } // end VECUConsoleFrame
    
    

	// Test the VirtualECU using 
	public static void main(String args[]) {
		VirtualECU emu = new VirtualECU();
        try {
    		emulatorConsoleTest(emu);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
