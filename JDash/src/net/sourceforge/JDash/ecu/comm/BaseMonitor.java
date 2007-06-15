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


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/******************************************************
 * All monitors should really extend this class.  Although
 * the ECUMonitor is the primary interface, this method provides
 * a few necessary initialization and setup functions.  And
 * the markTime() method.
 *****************************************************/
public abstract class BaseMonitor implements ECUMonitor
{
	
	/** This list will hold the entire list of ECU Bound parameters */
    protected List<ECUParameter> params_;
    
    /* This is the special time parameter that the markTime() method will call upon */
    private TimeParameter time_;
    
    /** You'll need this. This flag indicates that the thread is to be run or not. As long as this
     * flag is true, then keep looping in your run() method.  As soon as it's set to false, you can drop
     * out of the loop, and exit the run() method */
    protected Boolean doRun_;

    private ParameterRegistry paramRegistry_ = null;
    
    /******************************************************
     * Create a new base monitor instance.  This is the core monitor for
     * almost all monitors.
     ******************************************************/
    public BaseMonitor()
    {
        this.params_ = Collections.synchronizedList(new ArrayList<ECUParameter>());
        this.doRun_ = new Boolean(true);
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
    
    /********************************************************
     * @return
     *******************************************************/
    public ParameterRegistry getParameterRegistry()
    {
    	return this.paramRegistry_;
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
     * mark the time in the time parameter.
     ******************************************************/
    protected final void markTime()
    {
        time_.marktime();
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
    	for (int index = 0; index < params.size(); index++)
    	{
    		if (params.get(index) == null)
    		{
    			throw new RuntimeException("Cannot add a null parameter to this monitor.  Index [" + index + "]");
    		}
    		
    	}
    	
        List<Parameter> l = new ArrayList<Parameter>(params.size());
        l.addAll(params);
        recurseParam(l);
    }

    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getParams()
     *******************************************************/
    public List<ECUParameter> getParams()
    {
    	return this.params_;
    }
    

    
    /*******************************************************
     * @param params
     *******************************************************/
    private void recurseParam(List<Parameter> params)
    {
            if(params.size() == 0) return;

            if(params.get(0) instanceof MetaParameter)
            {
                params.addAll( ((MetaParameter) params.get(0)).getDependants());
            }
            else if (params.get(0) instanceof TimeParameter)
            {
                /*do nothing*/
            }
            else
            {
                if(!this.params_.contains(params.get(0)))
                {
                	this.params_.add((ECUParameter)params.get(0));
                }
            }
            
            params.remove(0);
            recurseParam(params);
    }
    
    
    /*******************************************************
     * The default is that the DTC Reset is NOT supported. If your
     * monitor class uspportes it, then you MUST override this method.
     * 
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#resetDTCs()
     *******************************************************/
    public void resetDTCs() throws Exception
    {
    	throw new Exception("DTC reset not supported by this monitor");
    	
    }
    
 
}
