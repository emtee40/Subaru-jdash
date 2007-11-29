/*******************************************************
 * 
 *  @author spowell
 *  RateParameter
 *  Aug 8, 2006
 *  $Id: RateParameter.java,v 1.2 2006/09/14 02:03:43 shaneapowell Exp $
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

import net.sourceforge.JDash.ecu.param.MetaParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterException;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;

import java.util.*;

/******************************************************
 * This parameter tracks the number of updates per
 * second that are being performed. It is the job of the
 * running monitor to call it's marktime() method
 * at the correct time to indicate it's finished one complete
 * update.  Every time the Time parameter is updated, this
 * parameter will update it's rate value.
 * 
 * @see net.sourceforge.JDash.ecu.param.MetaParameter
 *****************************************************/
public class RateParameter extends MetaParameter {
    private Parameter timeParam_;
    private double lastTime_ = 0;
    private double rate_ = 0;

    public RateParameter(TimeParameter time) throws RuntimeException {
    	try
    	{
	        this.timeParam_ = time;
//	        this.timeParam_.addObserver(this);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new RuntimeException("There was a problem adding the Rate Parameter to the registry.  Most Likely cause, the TIME parameter is not yet added");
    	}
    }
    public List<Parameter> getDependants() {
        return Collections.singletonList(this.timeParam_);
    }

    public void setName(String name) {
        //Do nothing
    }

    @Override
    public void addArg(String name, String value) throws ParameterException {
    	// TODO Auto-generated method stub
    	
    }

    public String getName() {
        return ParameterRegistry.RATE_PARAM;
    }

    public double getResult()
    {
      double interval = this.timeParam_.getResult() - this.lastTime_;
      this.lastTime_ = this.timeParam_.getResult();        
      this.rate_ = 1000D / interval;
      return this.rate_;
    	
    }

//    /******************************************************
//     * When an update on this parameter gets called, it's because this
//     * parameter is "Observing" the TIME parameter.  When ever a time
//     * event is hit, this method will re-calculate the current 
//     * rate.
//     * 
//     * Override
//     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
//     *******************************************************/
//    public void update(Observable o, Object arg) {
//
//        double interval = this.timeParam_.getResult() - this.lastTime_;
//        this.lastTime_ = this.timeParam_.getResult();        
//        this.rate_ = 1000D / interval;
//
//        this.setChanged();
//        this.notifyObservers();
//    }
}
