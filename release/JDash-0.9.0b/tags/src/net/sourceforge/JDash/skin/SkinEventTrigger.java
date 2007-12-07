/*******************************************************
 * 
 *  @author spowell
 *  SkinEventTrigger.java
 *  Dec 5, 2007
 *  $Id:$
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
package net.sourceforge.JDash.skin;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterEventListener;

/******************************************************
 * Some skins support the creation of SkinEventTriggers.
 * These are objects that will trigger a SkinEvent message
 * fired to the skin when the trigger criteria are met. 
 * The most common trigger is when an ECU Sensors value
 * is between a defined set of values.
 ******************************************************/
public class SkinEventTrigger 
{
	
	/** An event type with this value will be fired when the triggers ecu parameter is
	 * inside the range */
	public static final String EVENT_TYPE_ENTER_RANGE = "enter-range";
	
	/** An event tuye with this value will be fired when the triggers ecu parameter is 
	 * outside the range */
	public static final String EVENT_TYPE_EXIT_RANGE = "exit-range";
	

	private Skin skin_ = null;
	private String parameterName_ = null;
	private Double rangeMin_ = -1.0;
	private Double rangeMax_ = -1.0;
	
	private List<SkinEvent> enterActions_ = new ArrayList<SkinEvent>();
	private List<SkinEvent> exitActions_ = new ArrayList<SkinEvent>();

	/* The parameter that gets attached to */
	private Parameter param_ = null;
	
	/* This flag tracks if the enter Actions have been fired.
	 * this prevents this trigger from firing an action over
	 * and over again, just becaues it's within range.  This
	 * flag also is used for exit action tracking.  */
	private Boolean hasFiredEnterActions_ = null;

	
	private ParameterEventListener paramEventListener_ = null;
	
	/******************************************************
	 * Creates a new event trigger.  
	 * @param skin
	 * @param rangeMin
	 * @param rangeMax
	 * @param enterActions
	 * @param exitActions
	 ******************************************************/
	public SkinEventTrigger(Skin skin, String parameterName, Double rangeMin, Double rangeMax)
	{
		this.skin_ = skin;
		this.parameterName_ = parameterName;
		this.rangeMin_ = rangeMin;
		this.rangeMax_ = rangeMax;
		
		/* Create our parameter listener */
		this.paramEventListener_ = new ParameterEventListener()
		{
			public void valueChanged(Parameter p)
			{
				paramChanged();
			}
		};
	}

	/********************************************************
	 * Add the event to this event trigger.
	 * @param e
	 *******************************************************/
	public void addSkinEvent(SkinEvent e) throws Exception
	{
		if (EVENT_TYPE_ENTER_RANGE.equals(e.getType()))
		{
			this.enterActions_.add(e);
		}
		else if (EVENT_TYPE_EXIT_RANGE.equals(e.getType()))
		{
			this.exitActions_.add(e);
		}
		else
		{
			throw new Exception("Unable to add the SkinEvent to this trigger.  The Event has an unsupported type of " + e.getType());
		}
			
	}
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public String getParameterName()
	{
		return this.parameterName_;
	}
	
	/*******************************************************
	 * Set the parameter to trigger off of.  If the parameter
	 * has already been set, then this trigger will remove
	 * it's self from the existing parameter, and attach to
	 * this new one.
	 * @param p
	 *******************************************************/
	public void attachToParameter(Parameter p)
	{
		if (this.param_ != null)
		{
			this.param_.removeEventListener(this.paramEventListener_);
		}
		
		this.param_ = p;
		this.param_.addEventListener(this.paramEventListener_);
	}
	
	
	/*******************************************************
	 * Gets called when the parameters value changes.
	 *******************************************************/
	private void paramChanged()
	{
		double result = this.param_.getResult();

		/* If the action flag is not yet set, then we'll just set the
		 * action status flag, and not fire any events.  this
		 * is really only for the first parameter value */
		if (this.hasFiredEnterActions_ == null)
		{
			this.hasFiredEnterActions_ = new Boolean((result >= this.rangeMin_) && (result <= this.rangeMax_));
		}
	
		if ((result >= this.rangeMin_) &&
			(result <= this.rangeMax_))
		{
			fireEnterActions();
		}
		else
		{
			fireExitActions();
		}
		
	}
	
	
	/********************************************************
	 * 
	 *******************************************************/
	private void fireEnterActions()
	{
		if (Boolean.FALSE.equals(this.hasFiredEnterActions_))
		{
			this.hasFiredEnterActions_ = Boolean.TRUE;
			
			/* Only fire enter actions, if this trigger is transitioning into the range */
			for (SkinEvent se : this.enterActions_)
			{
				this.skin_.fireSkinEvent(se);
			}
		}
	}
	
	
	/********************************************************
	 * 
	 *******************************************************/
	private void fireExitActions()
	{
		if (Boolean.TRUE.equals(this.hasFiredEnterActions_))
		{
			this.hasFiredEnterActions_ = Boolean.FALSE;
			
			/* Only fire exit actions, if this trigger is transitioning out of the range */
			for (SkinEvent se : this.exitActions_)
			{
				this.skin_.fireSkinEvent(se);
			}		
		}
	}


}
