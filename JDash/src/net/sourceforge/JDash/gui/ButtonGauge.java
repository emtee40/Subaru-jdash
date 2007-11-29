/*******************************************************
 * 
 *  @author spowell
 *  ComponentGauge
 *  Aug 24, 2006
 *  $Id: ButtonGauge.java,v 1.4 2006/12/31 16:59:10 shaneapowell Exp $
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
package net.sourceforge.JDash.gui;


import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * This gauge is designed to put a button on the screen.
 * The button can be a user controllable button, or
 * a sensor controllable button.   If the parameter is 
 * a null parameter, then the button will be only user
 * controllable. If the parameter is set, then it will be
 * parameter controllable.
 ******************************************************/
public class ButtonGauge extends AbstractGauge
{
	
	public static final long serialVersionUID = 0L;
	
	private GaugeButton gaugeButton_ = null;
	
	private Double sensorMin_ = null;
	private Double sensorMax_ = null;
	
	/******************************************************
	 * Create a new button gauge from a button shape.
	 ******************************************************/
	public ButtonGauge(Parameter p, GaugeButton gaugeButton)//ButtonShape buttonShape)
	{
		super(p);

		this.gaugeButton_ = gaugeButton;
		
		
//		/* The button MUST be of one of our known types */
//		if ((GaugeButton.BUTTON_ACTION_LOGGER_TOGGLE.equalsIgnoreCase(this.buttonShape_.getAction()) == false) &&
//			(GaugeButton.BUTTON_ACTION_DTC_RESET.equalsIgnoreCase(this.buttonShape_.getAction()) == false))
//		{
//			throw new  RuntimeException("Cannot add button of action [" + this.buttonShape_.getAction() + "].  It's not supported by the component gauge class");
//		}

		/* Create the button */
//		this.gaugeButton_ = new GaugeButton(getParentPanel().getSkin(), this.buttonShape_);
		
		/* Link to the button */
		// TODO Link the button to the parameter
//		this.gaugeButton_.getButtonComponent().addActionListener(this);
		
		/* Add the button */		
//		this.getParentPanel().add(this.gaugeButton_, buttonShape.getShape().getBounds());
		
	}

	
	/*******************************************************
	 * @param min
	 ******************************************************/
	public void setSensorMin(double min)
	{
		this.sensorMin_ = min;
	}
	
	/******************************************************
	 * @return
	 ******************************************************/
	public double getSensorMin()
	{
		return this.sensorMin_;
	}
	
	/*******************************************************
	 * @param max
	 *******************************************************/
	public void setSensorMax(double max)
	{
		this.sensorMax_ = max;
	}
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public double getSensorMax()
	{
		return this.sensorMax_;
	}
	
	
	/******************************************************
	 * Override does nothing.  Just returns.
	 *
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 *******************************************************/
	public void paint(GaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{

		/* This is where this component gets placed onto the parent panel */
		
		/* don't add it, if ti's allreayd been added */
		for (Component comp : panel.getComponents())
		{
			if (comp == this.gaugeButton_.getButtonComponent())
			{
				return;
			}
		}
		
		/* Add it to the gauge panel */
		panel.add(this.gaugeButton_.getButtonComponent(), this.gaugeButton_.getButtonShape().getShape().getBounds());
		
	}
	
	
}