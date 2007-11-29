/*******************************************************
 * 
 *  @author spowell
 *  TestMonitor
 *  Aug 8, 2006
 *  $Id: TestMonitor.java,v 1.4 2006/09/14 02:03:42 shaneapowell Exp $
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

import java.util.Random;
import java.util.concurrent.TimeUnit;


/******************************************************
 * This monitor will get each ECUParameter from the
 * registry, and increment it's value every DELAY ms.  The default
 * DELAY value is 50. So, every 50 ms, each raw parameter will
 * get an increment.  This is usefull if you just want your
 * screen to go crazy spewing out data.
 ******************************************************/
public class TestMonitor extends BaseMonitor
{
	
	private static final int DELAY = 50;

	/*****************************************************
	 * @param params IN - the list of parameters to monitor.
	 * @throws ParameterException
	 ******************************************************/
    public TestMonitor() throws Exception
    {
        super();
    }
    
    
    
    /*******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
     *******************************************************/
    public String getEcuInfo() throws Exception
    {
    	return "Test Monitor";
    }
    
    
    /*******************************************************
     * Override
     * @see java.lang.Runnable#run()
     *******************************************************/
    public void run()
    {
    	Random r = new Random(System.currentTimeMillis());
    	/* Randomize the parameters */
    	for (ECUParameter p : getParams())
    	{
    		p.setResult(r.nextDouble() * 0xff);
    	}

        while(doRun_)
        {
        	fireProcessingStartedEvent();
        	
        	/* Setup the test list */
            for(ECUParameter p : getParams())
            {
            	if (p.getLastFetchTime() + p.getPreferedRate() < System.currentTimeMillis())
            	{
            		p.setLastFetchTime(System.currentTimeMillis());
            		p.setResult(p.getResult()+1);
            		if (p.getResult() >= 0xff)
            		{
            			p.setResult(0);
            			fireProcessingParameterEvent(p);
            		}
            	}
            }
            
            markTime();
            fireProcessingFinishedEvent();
            
            
            try
            {
                TimeUnit.MILLISECONDS.sleep(DELAY);
            }
            catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
            
            
        }
    }
}
