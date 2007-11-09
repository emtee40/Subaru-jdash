/*******************************************************
 * 
 *  @author spowell
 *  GaugeButton.java
 *  Aug 16, 2006
 *  $Id: GaugeButton.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.skin.Skin;


/*******************************************************
 * This is a special button that conforms to the look 
 * and feel of a gauge.  It is also capable of being
 * triggered by a parameter.
 ******************************************************/
public class GaugeButton extends JLabel implements MouseListener //, Observer
{
	
	/* This flag keeps track of the state of this button */
	private boolean isPressed_ = false;
	
	
	
	/** The action string that defines a button as being a high-reset type of button */
	public static final String BUTTON_ACTION_HIGH_RESET = "high-reset";

	/** The action string that defines a button as being a low-reset type of button */
	public static final String BUTTON_ACTION_LOW_RESET = "low-reset";
	
	/** The action string that defines a button as being a logger-toggle type of button */
	public static final String BUTTON_ACTION_LOGGER_TOGGLE = "logger-toggle";
	
	/** The action string that defines a button as being a DTC reset button */
	public static final String BUTTON_ACTION_DTC_RESET = "dtc-reset";
	
	/** A button type code string to represent a pushbutton */
	public static final String BUTTON_TYPE_PUSHBUTTON = "push-button";
	
	/** A button type code string to represent a toggle/checkbox button */
	public static final String BUTTON_TYPE_TOGGLE = "toggle";
	
	public static final long serialVersionUID = 0L;

	private Image upImage_ = null;
	private Image downImage_ = null;
	
	private ButtonShape buttonShape_ = null;
	private ArrayList<ActionListener> actionListeners_ = new ArrayList<ActionListener>();
	
	/*******************************************************
	 * Create a new standard button.  If you wish to setup 
	 * images for this button, use the setImage() methods.
	 * @param shape IN - the button shape to model this gauge button after.
	 * NOte. if the butto shape has a parameter trigger value set, then
	 * this button will NOT respond to the user clicking on the screen.
	 * Instead, this button will ONLY respond to the parameter values
	 * changing according to the values defined within the button shape.
	 * 
	 * @see ButtonShape
	 ******************************************************/
	public GaugeButton(Skin skin, ButtonShape shape)
	{
		super();
		
		try
		{
			
			/* Make sure the type code is a valid type code */
			if ((BUTTON_TYPE_PUSHBUTTON.equalsIgnoreCase(shape.getType()) == false) &&
				(BUTTON_TYPE_TOGGLE.equalsIgnoreCase(shape.getType()) == false))
			{
				throw new Exception("Button Shape with action: " + shape.getAction() + " has an unsupported type code of: " + shape.getType()); 
			}
			
			/* Make sure the action is a valid action code */
			if ((BUTTON_ACTION_HIGH_RESET.equalsIgnoreCase(shape.getAction()) == false) &&
				(BUTTON_ACTION_LOW_RESET.equalsIgnoreCase(shape.getAction()) == false) &&
				(BUTTON_ACTION_LOGGER_TOGGLE.equalsIgnoreCase(shape.getAction()) == false) &&
				(BUTTON_ACTION_DTC_RESET.equalsIgnoreCase(shape.getAction()) == false))
			{
				throw new Exception("Button Shape : " + shape.getType() + " with image: " + shape.getUpImageName() + " has an unupported action type code of: " + shape.getAction());
			}
			
			this.upImage_ = skin.getImage(shape.getUpImageName()).getImage();
			this.downImage_ = skin.getImage(shape.getDownImageName()).getImage();

			super.setBounds(shape.getShape().getBounds());

			setPressed(false);
			
			this.buttonShape_ = shape;
			
			this.setOpaque(false);
			
			/* Add the mouse listener, but ONLY if this button doesn't have a parameter trigger */
			addMouseListener(this);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable setup gauge button [" + shape.getAction() + "]\n" + e.getMessage());
		}
	}

	
	
	/*******************************************************
	 * Returns true of this button is currently in a pressed state.
	 * @return true if pressed, false if not.
	 *******************************************************/
	public boolean isPressed()
	{
		return this.isPressed_;
	}
	

	/********************************************************
	 * Set the pressed status of this button.
	 * @param pressed IN - set to pressed or not.
	 *******************************************************/
	public void setPressed(boolean pressed)
	{
		this.isPressed_ = pressed;
		
		if (isPressed())
		{
			setIcon(new ImageIcon(this.downImage_.getScaledInstance(getBounds().width, getBounds().height, Image.SCALE_SMOOTH)));
		}
		else
		{
			setIcon(new ImageIcon(this.upImage_.getScaledInstance(getBounds().width,getBounds().height,Image.SCALE_SMOOTH)));
		}
		

	}
	
	
	/*******************************************************
	 * Override
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 *******************************************************/
	@Override
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x, y, w, h);
		setPressed(isPressed());
	}
	
	
	/*******************************************************
	 * get the button shape used to create this button.
	 * @return the shape.
	 *******************************************************/
	public ButtonShape getButtonShape()
	{
		return this.buttonShape_;
	}
	
	
	/*******************************************************
	 * Add an action listener just like a regular JButton.
	 * When the button is pressed or triggered, the listeners
	 * will get an actionPerformed event.
	 * 
	 * @param l IN - the listener to add.
	 *******************************************************/
	public void addActionListener(ActionListener l)
	{
		this.actionListeners_.add(l);
	}
	
	/*******************************************************
	 * Fire an action event to all listeners.
	 *******************************************************/
	private void fireActionEvent()
	{
		for (ActionListener l : this.actionListeners_)
		{
			l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		}
	}
	
	
	/*******************************************************
	 * Override does Nothing
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 *******************************************************/
	public void mouseClicked(MouseEvent mev)
	{


		/* If this is a pushbutton, then reset the image */
		if (BUTTON_TYPE_PUSHBUTTON.equalsIgnoreCase(getButtonShape().getType()) == true)
		{
			setPressed(false);
		}
		
	}
	
	
	/*******************************************************
	 * Override does Nothing
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 *******************************************************/
	public void mouseEntered(MouseEvent me)
	{
		
	}
	
	
	/*******************************************************
	 * Override does Nothing
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 *******************************************************/
	public void mouseExited(MouseEvent me)
	{
		
	}
	
	/*******************************************************
	 * Override tracks the user clicking th emouse.
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 *******************************************************/
	public void mousePressed(MouseEvent me)
	{
		

		/* If this is a pushbutton, set it's image to down */
		if (BUTTON_TYPE_PUSHBUTTON.equalsIgnoreCase(getButtonShape().getType()) == true)
		{
			setPressed(true);
		}

		
		/* If this is a toggle button, then togle the state */
		if (BUTTON_TYPE_TOGGLE.equalsIgnoreCase(getButtonShape().getType()) == true)
		{
			
			if (isPressed() == true)
			{
				setPressed(false);
			}
			else
			{
				setPressed(true);
			}
			
		}
		
		/* A press ALWAYS fires an event */
		fireActionEvent();


	}
	
	/*******************************************************
	 * Override
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 *******************************************************/
	public void mouseReleased(MouseEvent me)
	{
		
		/* If it's a push button, then bring the button back up */
		if (BUTTON_TYPE_PUSHBUTTON.equalsIgnoreCase(getButtonShape().getType()) == true)
		{
			setPressed(false);
		}
		
		/* A press ALWAYS fires an event */
		fireActionEvent();

		
	}
	
}
