/*******************************************************
 * 
 *  @author spowell
 *  BaseMonitor
 *  Aug 8, 2006
 *  $Id: BaseMonitor.java,v 1.5 2006/12/31 16:59:08 shaneapowell Exp $
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
import net.sourceforge.JDash.ecu.param.MetaParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.ecu.param.special.TimeParameter;
import net.sourceforge.JDash.skin.SkinEvent;


import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

import java.io.IOException;


/******************************************************
 * All monitors should really extend this class.  Although
 * the ECUMonitor is the primary interface, this method provides
 * a few necessary initialization and setup functions.  And
 * the markTime() method.
 * 
 * Monitor objects encapsulate the functionality of mediating
 * (possibly recurring) requests to the ECU.  The details
 * of how this is done are contained in the ProtocolHandler
 * class.
 *****************************************************/

/***
 * 
 * The communication configurator will need to specify a couple different things:
 * - ECU protocol (e.g., SSM)
 * - Hardware driver (RXTXPort [serial], Cobb Serial, virtual)
 *   * The hardware driver is called a "Port" and is initialized and finalized
 *     by the initPort() and closePort() objects.
 *   * It is the monitor's responsibility to set up the port object correctly.
 *     Typically, each protocol requires certain serial protocol parameters.
 *     These protocol impl. details are typically handled by the port object.
 *     for example, it may need to set the baud rate and flow control parameters.
 
 * 
 * @author greg
 */

public abstract class BaseMonitor implements ECUMonitor
{


	/*************************************************
	 * Constants	
     *************************************************/		
	/** This is the expected name of all DTC codes.  They start with this + an index number.  eg DTC_0 or DTC_4 */
	public static final String DTC_PARAM_NAME_PREFIX = "DTC_";
	
	/** Just like the DTC_PARAM_NAME_PREFIX, this is for DTC History codes */
	public static final String DTC_HISTORY_PARAM_NAME_PREFIX = "DTC_HIST_";
	
	public static final String ACTION_DTC_RESET = "dtc-reset";

	/*************************************************
	 * 
     *************************************************/		
	
	/** This list will hold the entire list of ECU Bound parameters */
    protected List<ECUParameter> params_;
    
    /* This is the special time parameter that the markTime() method will call upon */
    private TimeParameter time_;
    
    /** the list of all parameters */
    private ParameterRegistry paramRegistry_ = null;
    
    /** You'll need this. This flag indicates that the thread is to be run or not. As long as this
     * flag is true, then keep looping in your run() method.  As soon as it's set to false, you can drop
     * out of the loop, and exit the run() method */
    protected Boolean doRun_;


	/*************************************************
	 * Communications port
     *************************************************/		
	
	protected BasePort commPort = null;

	
	/*************************************************
	 * Event Listener members
     *************************************************/		
    /** This is a list of objects that wish to be informed of monitor events */
    List<MonitorEventListener> monitorListeners_ = new ArrayList<MonitorEventListener>();
    
    /******************************************************
     * Create a new base monitor instance.  This is the core monitor for
     * almost all monitors.
     ******************************************************/
    public BaseMonitor()
    {
        this.params_ = Collections.synchronizedList(new ArrayList<ECUParameter>());
        this.doRun_ = new Boolean(true);
    }
    
    /**
     * If no BasePort object has been initialized for this BaseMonitor object,
     * then this method checks whether the BasePort object is of the supported
     * type, then initializes it and opens the port.  
     * 
     * In general, overrides should check whether the port object is of the
     * supported type, and perform any necessary initialization to use this 
     * BasePort object.
     * 
     * initPort should assign the internal commPort class member so that the 
     * Monitor can use this commPort for communication.
     * 
     * The BasePort implementation of the initPort() method supports the
     * VirtualECUPort class.  See the implementation of this function to understand
     * how you should structure initPort() methods for overrides.
     * 
     * @param port A BasePort class or derived class. If null, the method will 
     *             not succeed.
     * @param strPortName name of the port resource (may be used in overrides, 
     *        but is not used in the BasePort implementation of this method)
     * @return the initialized BasePort object (should be equal to input parameter
     * port) if success, otherwise null.
     */
    public BasePort initPort(BasePort port, String strPortName) throws IOException
    {
        if (commPort != null) 
        {
            System.out.println("Warning: BasePort.commPort is already initialized!");
            return commPort;
        }

        // Can't initialize a null port
        if (port == null) return null;
        
        if (port instanceof VirtualECUPort) 
        {
            // Nothing to do. Just accept the port.
            commPort = port;
        } 
        else 
        {
            // This is not a supported port type.  Return null.
            // TODO: make this warning message more informative.
            System.out.println("Warning: BaseMonitor doesn't support this type of BasePort");
        }
        
        // TODO: In all cases, wait for the comm port to say that it's ready.

        return commPort;
    }
    
    /**
     * In your override, perform any monitor or protocol-specific 
     * cleanup before calling this routine.
     * 
     * The details of the cleanup should be hidden in a ProtocolHandler
     * class, to keep this method simple.
     * 
     * BasePort.closePort() calls the close() method of the commPort object,
     * then invalidates the commPort object attached to this monitor.
     * 
     * @return
     */
    public boolean closePort() 
    {
        try {
            commPort.close();
            commPort = null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    /*******************************************************
     * If you extend this class, you MUST call this super.init() method.
     * Otherwise this base monitor will not init correctly. The return value
     * is always null from this method, so you'll need to create your own
     * parameter list, and return it, if you wish.
     * 
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
     *******************************************************/
    public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
    {
    	this.time_ = (TimeParameter)reg.getParamForName(ParameterRegistry.TIME_PARAM);
    	
    	this.paramRegistry_ =  reg;
    	
    	return new ArrayList<Parameter>();
    }
    
    
    /*******************************************************
     * watch for known skin events.
     * Override
     * @see net.sourceforge.JDash.skin.SkinEventListener#actionPerformed(net.sourceforge.JDash.skin.SkinEvent)
     *******************************************************/
    public void actionPerformed(SkinEvent e)
    {
    	if (SkinEvent.DESTINATION_MONITOR.equals(e.getDestination()))
    	{
    		if (ACTION_DTC_RESET.equals(e.getAction()))
    		{
    			resetDTCs();
    		}
    		else
    		{
    			
    		}
    	}
    	
    }
    
    /********************************************************
     * @return
     *******************************************************/
    public ParameterRegistry getParameterRegistry()
    {
    	return this.paramRegistry_;
    }
    
    /********************************************************
     * @param mel
     *******************************************************/
    public void addMonitorListener(MonitorEventListener mel)
    {
    	this.monitorListeners_.add(mel);
    }
    
    /********************************************************
     * @param mel
     *******************************************************/
    public void removeMonitorListener(MonitorEventListener mel)
    {
    	this.monitorListeners_.remove(mel);
    }
    
    
    /********************************************************
     * 
     *******************************************************/
    public void fireProcessingStartedEvent()
    {
    	for (MonitorEventListener mel : this.monitorListeners_)
    	{
    		mel.processingStarted();
    	}
    }
    
    
    /********************************************************
     * 
     *******************************************************/
    public void fireProcessingFinishedEvent()
    {
        time_.marktime();
        fireProcessingParameterEvent(time_);

    	
    	for (MonitorEventListener l : this.monitorListeners_)
    	{
    		l.processingFinished();
    	}
    }
    
    
    /*********************************************************
     * @param p
     *******************************************************/
    public void fireProcessingParameterEvent(Parameter p)
    {
    	for (MonitorEventListener l : this.monitorListeners_)
    	{
    		l.processedParameter(p);
    	}
    }
    
    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#stop()
     *******************************************************/
    public void stop()
    {
        synchronized(doRun_)
        {
            doRun_ = new Boolean(false);
        }
    }

    /*******************************************************
     * Get the time parameter.
     * @return
     *******************************************************/
    public final TimeParameter getTime()
    {
    	return this.time_;
    }
    
    /*******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#addParameter(net.sourceforge.JDash.ecu.param.Parameter)
     *******************************************************/
    public void addParam(Parameter param)
    {
    	addAllParams(Collections.singletonList(param));
    }
    
    

    /*******************************************************
     * @param params
     *******************************************************/
    private void addAllParams(List<Parameter> params)
    {
    	/* Make sure a null parameter is not beeing added */
    	for (int index = 0; index < params.size(); index++)
    	{
    		if (params.get(index) == null)
    		{
    			throw new RuntimeException(
						"Cannot add a null parameter to this monitor.  Index [" + index + "]");
    		}
    	}

    	/* Recursively add the parameters.  We do this with a copy of
    	 * the original list, because the recursion method modifies the
    	 * list as it processes its entries */
        ArrayList<Parameter> l = new ArrayList<Parameter>(params.size());
        l.addAll(params);
        recursivelyAddParams(l);
    }

    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getParams()
     *******************************************************/
    public List<ECUParameter> getParams()
    {
    	return this.params_;
    }
    
	/**
	 * Get a list of parameters that need to be updated, based on their preferred
	 * update rate, and when it was last updated.
	 * @return 
	 * List of ECUParameters that need to be updated.  If you update an
	 * ECUParameter, call that parameter's setLastFetchTime method.
	 * 
	 */
	public List<ECUParameter> getParamsForUpdate() {
		List<ECUParameter> list = new LinkedList<ECUParameter>();
		getParamsForUpdate(list);
		return list;
	}
	
	/**
	 * Get a list of parameters that need to be updated, based on their
	 * preferred update rate, and when it was last updated.
	 * @param list 
	 * A reference to a list where values will be returned.  If you update an
	 * ECUParameter in this list, call that parameter's setLastFetchTime method.
	 */
	public void getParamsForUpdate(List<ECUParameter> list) {
		list.clear();
		long now = System.currentTimeMillis();
		for (ECUParameter p : params_) {
			if (!p.isEnabled()) continue;
			
			if (p.getLastFetchTime() + p.getPreferredRate() < now)
			{
				list.add(p);
				// Don't set the fetch time because it might not
				// actually get fetched.  We'll set this later when
                // it actually gets updatd.
            	// p.setLastFetchTime(System.currentTimeMillis());
			}
		}
	}

    
    /*******************************************************
     * @param params
     *******************************************************/
    private void recursivelyAddParams(ArrayList<Parameter> params)
    {
    	/* If this list is not empty, then stop here */
        if(params.size() == 0)
        {
        	return;
        }

        /* Meta parameters are not added themselves, rather their dependants are added.
         * Note, that we use the addAll method so that we can correctly resurse through 
         * the dependants */
        if(params.get(0) instanceof MetaParameter)
        {
        	if(((MetaParameter) params.get(0)).getDependants().size() == 0)
        	{
        		System.out.println("Warning: Metaparameter " + params.get(0).getName() + " does not have any dependents identified");
        	}
        	this.addAllParams(((MetaParameter) params.get(0)).getDependants());
        }
        else if(params.get(0) instanceof ECUParameter)
        {
        	/* If this param has already been added, then no need to add it again */
            if(!this.params_.contains(params.get(0)))
            {
            	this.params_.add((ECUParameter)params.get(0));
            }
        }
        else
        {
        	/* Do nothing, we only care about monitoring MetaParams and actual ECU Parameters */
        }
        
        /* Remove this now added parameter from the list, and recurse to the next one */
        params.remove(0);
        recursivelyAddParams(params);
    }
    
	
    /*******************************************************
     * The default is that the DTC Reset is NOT supported. If your
     * monitor class uspportes it, then you MUST override this method.
     * 
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#resetDTCs()
     *******************************************************/
    public void resetDTCs() throws RuntimeException
    {
    	throw new RuntimeException("DTC reset not supported by this ECU monitor");
    } 
}
