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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JOptionPane;

import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * This gauge is designed to put a button on the screen.
 * The button can be a user controllable button, or
 * a sensor controllable button.   If the parameter is 
 * a null parameter, then the button will be only user
 * controllable. If the parameter is set, then it will be
 * parameter controllable.
 ******************************************************/
public class ButtonGauge extends AbstractGauge implements ActionListener
{
	
	public static final long serialVersionUID = 0L;
	
	private GaugeButton gaugeButton_ = null;
	
	private GaugePanel gaugePanel_ = null;
	
	private Double sensorMin_ = null;
	private Double sensorMax_ = null;
	
	/******************************************************
	 * Create a new button gauge from a button shape.
	 ******************************************************/
	public ButtonGauge(Parameter p, GaugeButton gaugeButton)//ButtonShape buttonShape)
	{
		super(p);

//		this.buttonShape_ = buttonShape;
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
		this.gaugeButton_.addActionListener(this);
		
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
	
//	/*******************************************************
//	 * We need to trap the update message, to watch for value changes so we can
//	 * trip the button.
//	 * Override
//	 * @see net.sourceforge.JDash.gui.AbstractGauge#update(java.util.Observable, java.lang.Object)
//	 *******************************************************/
//	@Override
//	public void update(Observable obs, Object obj)
//	{
//		
//		/* If a parameter is linked to this gauge, then we'll see if the button respnods to it */
//		if (getParameter() != null)
//		{
//			
//			Parameter param = getParameter();
//			
//			/* If it's in the range */
//			if ((param.getResult() >= getSensorMin()) && 
//				(param.getResult() <= getSensorMax()))
//			{
//
//				/* turn the botton on only if it's currently off */
//				if (this.gaugeButton_.isPressed() == false)
//				{
//					this.gaugeButton_.mousePressed(null);
//				}
//			}
//			else
//			{
//				/* Turn the button off only if it's currently on */
//				if (this.gaugeButton_.isPressed() == true)
//				{
//					this.gaugeButton_.mousePressed(null);
//				}
//			}
//
//		}
//	}
	
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
			if (comp == this.gaugeButton_)
			{
				return;
			}
		}
		
		/* Add it to the gauge panel */
		panel.add(this.gaugeButton_, this.gaugeButton_.getButtonShape().getShape().getBounds());
		
		/* We'll need the paranet gauge panel for action reference */
		this.gaugePanel_ = panel;
		
	}
	
	
	/*******************************************************
	 * Respond to component actions.  At the moment only the logger
	 * toggle button is supported. 
	 * Override
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 *******************************************************/
	public void actionPerformed(ActionEvent ae)
	{
		
		System.out.println("Button Triggered: "+ getParameter() + "  " + this.gaugeButton_.isSelected());
		
		try
		{
				
			/* Logger Toggle Button */
			if (GaugeButton.BUTTON_ACTION_LOGGER_TOGGLE.equalsIgnoreCase(this.gaugeButton_.getButtonShape().getAction()))
			{
				
				this.gaugePanel_.getLogger().enable(this.gaugeButton_.isSelected());
				return;
				
			} /* end if Logger Button */
				
			
			/* Logger Toggle Button */
			if (GaugeButton.BUTTON_ACTION_DTC_RESET.equalsIgnoreCase(this.gaugeButton_.getButtonShape().getAction()))
			{
				/* Ask the user before doing a reset */
				if (JOptionPane.YES_OPTION ==
						JOptionPane.showConfirmDialog(this.gaugePanel_, "Are you sure you want to reset the ECUs trouble codes?\n" +
						"This will result in the loss of any stored codes, and cause a reset of all learned values.", 
						"DTC Reset. Are you sure?", JOptionPane.YES_NO_OPTION))
				{
	// TODO. Relink to the monitor some how
//					getParentPanel().getMonitor().resetDTCs();
				}
				return;
					
			} /* end if Logger Button */
			
			
			/* If we got here, then the button action was not supported */
			throw new Exception("The Component pressed does not appear to be propertly supported");
			
		}
		catch(Exception e)
		{
			Startup.showException(e, false);
		}
		
	}
}