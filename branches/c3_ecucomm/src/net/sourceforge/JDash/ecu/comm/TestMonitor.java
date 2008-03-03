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
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/******************************************************
 * This monitor will get each ECUParameter from the
 * registry, and increment its value every DELAY ms.  The default
 * DELAY value is 50. So, every 50 ms, each raw parameter will
 * get an increment.  This is useful if you just want your
 * screen to go crazy spewing out data.
 ******************************************************/
public class TestMonitor extends BaseMonitor
{
	
	private static final int DELAY = 50; // milliseconds
	
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
    	long timeLastDirectionChange = 0L;
		int signRateChange = 1; // sign (positive or neg) of rate change
		int valueRateChange;
    	Random rnd = new Random(System.currentTimeMillis());
		
		List<ECUParameter> listParamsForUpdate = 
				new LinkedList<ECUParameter>();

        while(doRun_)
        {
        	fireProcessingStartedEvent();
        	
        	/* Random the direction boolean, only every 1 seconds */
        	if (timeLastDirectionChange <= (System.currentTimeMillis() - 1000))
			{
        		signRateChange = rnd.nextBoolean() ? 1 : -1;
        		timeLastDirectionChange = System.currentTimeMillis();
			}

        	/* Randomly set the change rate */
    		valueRateChange = signRateChange * (rnd.nextInt(4) + 1);
        	

			// Get list of parameteres that need updating
			getParamsForUpdate(listParamsForUpdate);
			
			// Update each parameter
            for(ECUParameter p : listParamsForUpdate)
            {
				double result = p.getResult() + valueRateChange;
				
				// saturate results
				if (result > 0xff) result = 0xff;
				if (result < 0x00) result = 0x00;

				p.setResult(result);
				p.setLastFetchTime(System.currentTimeMillis());
				
				fireProcessingParameterEvent(p);
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
