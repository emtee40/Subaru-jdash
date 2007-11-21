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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.skin.Skin;


/*******************************************************
 * This is a special button that conforms to the look 
 * and feel of a gauge.  It is also capable of being
 * triggered by a parameter.
 ******************************************************/
public class GaugeButton extends JToggleButton
{
	
	
	
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

	private ButtonShape buttonShape_ = null;

	
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
		super(shape.getAction());
		
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
			
//			this.upImage_ = skin.getImage(shape.getUpImageName()).getImage();
//			this.downImage_ = skin.getImage(shape.getDownImageName()).getImage();
			
			/* Set the icon images */
			setIcon(new ImageIcon(skin.getImage(shape.getUpImageName()).getImage()));
			setPressedIcon(new ImageIcon(skin.getImage(shape.getDownImageName()).getImage()));
			setSelectedIcon(getPressedIcon());

			super.setBounds(shape.getShape().getBounds());

			this.buttonShape_ = shape;
			
			this.setOpaque(false);
			
			/* If this button has a paramter trigger, then disable any mouse listeners */
// TODO			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable setup gauge button [" + shape.getAction() + "]\n" + e.getMessage());
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
	}
	
	
	/*******************************************************
	 * get the button shape used to create this button.
	 * @return the shape.
	 *******************************************************/
	public ButtonShape getButtonShape()
	{
		return this.buttonShape_;
	}
	
	
	
}
