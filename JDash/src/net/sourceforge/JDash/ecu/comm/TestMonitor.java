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
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.ecu.param.special.InternalParam;

import java.util.List;
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
     * @see net.sourceforge.JDash.ecu.comm.BaseMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry, net.sourceforge.JDash.ecu.comm.InitListener)
     *******************************************************/
    public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
    {
		/* Add the DTC meta parameters to the registry */
		for (int index = 0; index < 9; index++)
		{
			InternalParam dtcCode = new InternalParam(DTC_PARAM_NAME_PREFIX + index);
			InternalParam dtcHistCode = new InternalParam(DTC_HISTORY_PARAM_NAME_PREFIX + index);
			reg.add(dtcCode);
			reg.add(dtcHistCode);
		}
		
    	return super.init(reg, initListener);
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
    	long lastDirectionChange = 0L;
    	Random rnd = new Random(System.currentTimeMillis());
    	boolean increase = true;
    	int increaseRate = 1;

        while(doRun_)
        {
        	fireProcessingStartedEvent();
        	
        	/* Random the direction boolean, only every 1 seconds */
        	if (lastDirectionChange <= (System.currentTimeMillis() - 1000))
			{
        		increase = rnd.nextBoolean();
        		lastDirectionChange = System.currentTimeMillis();
			}

        	/* Randomly set the change rate */
    		increaseRate = rnd.nextInt(4) + 1;
        	
        	/* Setup the test list */
            for(ECUParameter p : getParams())
            {
            	if (p.isEnabled() == false)
            	{
            		continue;
            	}
            	
            	if (p.getLastFetchTime() + p.getPreferedRate() < System.currentTimeMillis())
            	{
            		p.setLastFetchTime(System.currentTimeMillis());
            		
            		if (increase)
            		{
            			p.setResult(p.getResult() + increaseRate);
            		}
            		else
            		{
            			p.setResult(p.getResult() - increaseRate);
            		}
            		
            		if ((p.getResult() > 0xff) || (p.getResult() < 0))
            		{
            			p.setResult(0);
            			fireProcessingParameterEvent(p);
            		}
            	}
            }
            
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
