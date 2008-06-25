/*******************************************************
 * 
 *  @author spowell
 *  ECUMonitor
 *  Aug 8, 2006
 *  $Id: ECUMonitor.java,v 1.4 2006/12/31 16:59:09 shaneapowell Exp $
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


import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.skin.SkinEventListener;

import java.util.List;


/******************************************************
 * An ECU Monitor represents the methods required by the main
 * application to communicate with the ECU.  The steps 
 * the app will follow on the concrete monitor are
 *  1. init()
 *  2. addAllParam()
 *  3. start the monitor in it's own thread.
 * 
 * The ECUMonitor acts as a gasket between the protocol
 * handler that knows *how* to make and process requests,
 * and primarily handles the timing of such requests.
 * 
 ******************************************************/
public interface ECUMonitor extends Runnable, SkinEventListener
{
	
	
	
	/*******************************************************
	 * This method will get called to initialize the monitor.
	 * The return list is a list of parameters that this monitor
	 * claims to support.  It is not actually the list of
	 * parameters used.  Infact, the return list is at present, optional.
	 * 
	 * Ror example, the SSM monitor will return the available
	 * sensors for the ECU it's connected to.
	 *  And OBD-II monitor might do the same.  The main 
	 *  DashboardFrame is passed into the monitor on init, giving
	 *  the monitor a chance to do some GUI work before
	 *  starting. The reason for passing the frame is go provide an 
	 *  owner frame to center dialog boxes around.  You should
	 *  not really directly affect the frame itself.
	 * 
	 * @return a list of parameters that this monitor claims to support.  This
	 * is a list for reference only, it is NOT used to initialize anything.
	 * 
	 * @throws Exception if there is a fatal init problem.
	 *******************************************************/
	public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception;
	
	
	/********************************************************
     * @return a string with monitor specific ecu info.  Can contain
     * info such as the ECU ID, verions code, date, comm type, etc etc.
     * 
     * @throws Exception
     *******************************************************/
    public String getEcuInfo() throws Exception;
    
	/********************************************************
	 * This method will be called to add a single parameter to this
	 * monitor for monitoring.  
	 *********************************************************/
	public void addParam(Parameter param);
    
	/*******************************************************
	 * Get the list of parameters this moniotor is tracking.
	 * @return the list of parametres.
	 *******************************************************/
	public List<ECUParameter> getParams();
	
	
	/********************************************************
	 * Since DTCs are usualy a bit different, and this is a rare
	 * to-ecu command, we define this here.  This forces any monitor
	 * to include the ability to reset the DTCs. But.. it is NOT
	 * necessary for the monitor to actually do it.  If the
	 * monitor can't reset DTCs, then throw an exception.  
	 * The GaugePanel will display the message to the user.
	 *******************************************************/
	public void resetDTCs() throws Exception;
	
    /********************************************************
     * Tell this monitor to stop its monitoring, and close
     * its connection. Note that this does NOT mean that the
     * thread is stopped right away.  This will flip a flag
     * indicating to the thread that it should finish what it's
     * currently doing, and then exit normally.  It is the
     * job of the thread owner to watch for the thread to finish
     * before moving on.
     *******************************************************/
    public void stop();
    
    
}
