/*******************************************************
 * 
 *  @author spowell
 *  TimeParameter
 *  Aug 8, 2006
 *  $Id: TimeParameter.java,v 1.4 2006/12/31 16:59:10 shaneapowell Exp $
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
package net.sourceforge.JDash.ecu.param.special;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;

/******************************************************
 * This special parameter tracks the time in milliseconds
 * for update events.  The monitor class will call its
 * markTime() method, which in turn calls the this time 
 * parameter's markTime() method,  thus updating the
 * the elapsed time.
 *****************************************************/
public class TimeParameter extends Parameter
{
	
    long _init;
    long _mark;

    
    /*******************************************************
     * 
     ******************************************************/
    public TimeParameter()
    {
        _mark = 0;
        _init = 0;
    }

    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.param.Parameter#getName()
     *******************************************************/
    public String getName()
    {
        return ParameterRegistry.TIME_PARAM;
    }

    /********************************************************
     * This method is provided to allow a means to override
     * the timer parameter.  Generally, you won't touch this
     * method. But, in the case of log file playback, it
     * might be desireable to force the time parameter value.
     * @param time
     *******************************************************/
    public void marktime(long time)
    {
    	_mark = time;
    	fireValueChangedEvent();
    }
    
    
    /*******************************************************
     * 
     *******************************************************/
    public void marktime()
    {
        if(_init == 0) _init = System.currentTimeMillis();
        marktime(System.currentTimeMillis() - _init);
    }

    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.param.Parameter#getResult()
     ******************************************************/
    public double getResult()
    {
    	return _mark;
    }
    

	/*******************************************************
	 * Overridden to prevent being disabled.
	 *******************************************************/
	public void setEnabled(boolean enable)
	{
	}
}
