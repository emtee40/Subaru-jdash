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
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import net.sourceforge.JDash.ecu.comm.SSMOBD2ProtocolHandler.*;

import net.sourceforge.JDash.ecu.param.*;
/**
 * Emulates an ECU by responding to packets
 * @author greg
 */
public class SSMOBD2VirtualECU extends VirtualECU {
    
    public static final int DEBUGLEVEL          = 0;
    
    public static final int STATE_WAIT_INIT     = 1;
    public static final int STATE_WAIT_ECUQUERY = 2;
    
    protected int state = STATE_WAIT_INIT;
    
    // Maps an ECU address code to a param value
    protected Map<Long,VirtualECUParamState> ecuParamStateList;
    protected Map<Long,ECUCap> ecuCapList;
    
    protected XMLParameterLoader xmlECUParamData;
    
	SSMOBD2ProtocolHandler ssmprotohandler = null;
	public SSMOBD2VirtualECU() {
		super();
        ecuParamStateList = new TreeMap<Long,VirtualECUParamState>();
        
        // Initialize a list of the ecu capabilities and make it searchable
        // by address.
        List<ECUCap> ecuCaps = SSMOBD2ProtocolHandler.getEcuCapabilities();
        ecuCapList = new TreeMap<Long,ECUCap>();
        for (ECUCap ecap : ecuCaps) 
        {
            ecuCapList.put(ecap.getAddressAsLong(), ecap );
        }
        
        
        if (DEBUGLEVEL > 0) System.out.println("Creating SSMOBD2VirtualECU object");
	}
    
    
    
    
    /**
     * 
     * Run assumes that an the BasePort object has already been
     * connected.
     */
	public void run() {
		SSMPacket rxPacket, txPacket;
        if (DEBUGLEVEL > 0)
    		System.out.println("SSMOBD2VirtualECU started.");

        if (emuport == null) throw new NullPointerException("EMUPort is null");
        
        try {
            if (!emuport.open()) {
                throw new RuntimeException("Open returned false!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Open failed: " + e.getMessage());    
        }
        
        InputStream  is = emuport.getECUInputStream();
		OutputStream os = emuport.getECUOutputStream();
        if (is == null) throw new NullPointerException("emuport.is is null");
        if (os == null) throw new NullPointerException("emuport.os is null");
		ssmprotohandler = new SSMOBD2ProtocolHandler(is, os);
		
		int paramsPerPacket = 
				SSMOBD2ProtocolHandler.SSMPacket.MAX_DATA_LENGTH / 
				SSMOBD2ProtocolHandler.SSM_OBD2_ADDRESS_SIZE;
		List<ECUParameter> packetParamList = new ArrayList<ECUParameter>();
		((ArrayList)packetParamList).ensureCapacity(paramsPerPacket);
		
        
        while (getSignal() == 0) {

            try {
        		if (DEBUGLEVEL > 0)
                    System.out.println("SSMOBD2VirtualECU listening for packet.");
            
                int nBytesAvailable=0, nLastBytesAvailable=0;
                nBytesAvailable = is.available();
                while ((nBytesAvailable < 1) && getSignal() == 0)
                {
                    if (DEBUGLEVEL > 0 && 
                            nBytesAvailable != nLastBytesAvailable || 
                            (nBytesAvailable > 0)) {
                        System.out.println("SSMOBD2VirtualECU: now has " +
                                nBytesAvailable + " bytes available");
                    }
                    nLastBytesAvailable = nBytesAvailable;
                    nBytesAvailable     = is.available();
                    
                    try { Thread.sleep(10); } 
                    catch (InterruptedException e) { }

                }
                if (DEBUGLEVEL > 0)
                    System.out.println("SSMOBD2VirtualECU received enough for a packet.");

                if (DEBUGLEVEL > 0)
                    System.out.println("SSMOBD2VirtualECU receiving packet");
                rxPacket = ssmprotohandler.receivePacket(0);
                if (DEBUGLEVEL > 0)
                    System.out.println("SSMOBD2VirtualECU received packet");

                switch (state) {
                    case STATE_WAIT_INIT:
                        if ( !rxPacket.equals(SSMPacket.packetInit()) ) {
                            // Unexpected packet.  Toss an error
                            throw new RuntimeException("SSMOBD2VirtualECU " +
                                    "expected init packet initPacket = " +
                                    SSMPacket.packetInit() + 
                                    "\nreceived " + rxPacket.toString());
                            
                        }
                        System.out.println("SSMOBD2VirtualECU received init packet.");
                        txPacket = new SSMPacket();
                        txPacket.setHeaderDest  (SSMPacket.SSM_DEVICE_APP);
                        txPacket.setHeaderSource(SSMPacket.SSM_DEVICE_ECU);
                        // TODO: figure out some more sensical values to return
                        // here.
                        txPacket.setData(new byte[] {
                            0x00, //I don't know which byte this should be.
                            (byte)0xaa, (byte)0xaa,
                            (byte)0xaa, (byte)0xaa,
                            (byte)0xaa, (byte)0xaa,
                            (byte)0xaa, (byte)0xaa,
                            (byte)0xaa
                        });
                        txPacket.setChecksum();
                        txPacket.write(os);
                        
                        // Set the next state.
                        state = STATE_WAIT_ECUQUERY;
                        break;
                    case STATE_WAIT_ECUQUERY:
                        // Load packetParamList with the list of parameters that we wish
                        // to query.
                        if (DEBUGLEVEL > 1)
                        System.out.println("SSMOBD2VirtualECU: rxPacket: " + rxPacket.toString());
                        SSMOBD2ProtocolHandler.decodeECUParameterQueryPacket(
                                rxPacket, packetParamList);

                        // Make up some values to return.
                        for (ECUParameter p : packetParamList ) {
                            
                            long addr = p.getAddressAsLong();
                            
                            VirtualECUParamState  vecuParamState = 
                                    ecuParamStateList.get(addr);
                            
                            if (vecuParamState == null) {
                                p.setResult(0); // TODO: rand
                                vecuParamState = new VirtualECUParamState(p);
                                ecuParamStateList.put(addr, vecuParamState);
                                
                            } else {
                                //ECUCap ecap = ecuCapList.get(addr);
                                //if (ecap != null) {
                                //    Parameter = paramRegistry_.getParamForName(ecap.name_);
                                //}
                                vecuParamState.updateParam();
                                p.setResult( vecuParamState.param_.getResult());
                            }
                        }
                        txPacket = SSMOBD2ProtocolHandler.encodeECUParameterQueryResponsePacket(packetParamList);
                        /* Send the TX packet */

                        // GN: apparently the ECU is supposed to echo back the request packet?
                        rxPacket.write(os);

                        txPacket.write(os);
                        
                        // Implicit:
                        // state = STATE_WAIT_ECUQUERY;
                        break;
                    default:
                        throw new RuntimeException(
                                "SSMOBD2VirtualECU got into" +
                                "an unexpected state! state=" + state);
                      
                };
                os.flush();
                

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            
        } // end while (getSignal() == 0)
        
        System.out.println("SSMOBD2VirtualECU quitting thread");

        
//		System.out.println("SSMOBD2VirtualECU listening.");
//		do {
//			try 
//			{
//				int nAvail = is.available();
//				if (nAvail > 0) 
//				{
//                    System.out.println("SSMOBD2VirtualECU data available");
//                    System.out.println("SSMOBD2VirtualECU waiting for packet");
//					rxPacket = ssmprotohandler.receivePacket(0);
//					System.out.println("SSMOBD2VirtualECU received a packet");
//
//					// Load packetParamList with the list of parameters that we wish
//					// to query.
//					SSMOBD2ProtocolHandler.decodeECUParameterQueryPacket(
//							rxPacket, packetParamList);
//					
//					// Make up some values to return.
//					for (ECUParameter p : packetParamList ) {
//						p.setResult(0); // TODO: rand
//					}
//					txPacket = SSMOBD2ProtocolHandler.encodeECUParameterQueryResponsePacket(packetParamList);
//					/* Send the TX packet */
//
//	                // GN: apparently the ECU is supposed to echo back the request packet?
//	                rxPacket.write(os);
//					
//										
//					txPacket.write(os);
//					os.flush();
//					
//					
//				}
//			} 
//			catch (IOException e) 
//			{
//				e.printStackTrace();
//			}
//			
//			try 
//			{
//				Thread.sleep(50);
//			} catch (InterruptedException e) {}	
//		} while (getSignal() == 0) ;		
	}
    
    public static class VirtualECUParamState {
        public ECUParameter param_ = null;
        
        // Parameters related to the generation of parameters
        public int t        =   0;
        public double min   =   0;
        public double max   = 255;
        public double delta =   1;
                
        VirtualECUParamState() {
            
        }
        VirtualECUParamState(ECUParameter param) {
            param_ = param.clone();
        }
        
        void updateParam() {
            
            double d = param_.getResult();
            
            d += delta;
            
            if (d > max) {
                d = max;
                delta = -Math.abs(delta);
            }
            
            if (d < min) {
                d = min;
                delta = Math.abs(delta);
            }
            
            param_.setResult(d);

            t++;
            
        }
        
    }
}
