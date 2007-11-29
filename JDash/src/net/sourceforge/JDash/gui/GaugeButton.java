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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinEvent;


/*******************************************************
 * This is a special button that conforms to the look 
 * and feel of a gauge.  It is also capable of being
 * triggered by a parameter.
 ******************************************************/
public class GaugeButton
{
	
	
	/** A button type code string to represent a pushbutton */
	public static final String BUTTON_TYPE_PUSHBUTTON = "push-button";
	
	/** A button type code string to represent a toggle/checkbox button */
	public static final String BUTTON_TYPE_TOGGLE = "toggle";
	
	public static final long serialVersionUID = 0L;

	private ButtonShape buttonShape_ = null;
	private Skin skin_ = null;
	
	private AbstractButton theButton_ = null;
	
	private List<SkinEvent> upActions_ = new ArrayList<SkinEvent>();
	private List<SkinEvent> downActions_ = new ArrayList<SkinEvent>();
	
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
		
		this.buttonShape_ = shape;
		this.skin_ = skin;

		
		try
		{
			
			
			/* Create a new instance of the correct button */
			if (BUTTON_TYPE_PUSHBUTTON.equalsIgnoreCase(shape.getType()))
			{
				this.theButton_ = createJButton();
			}
			else if (BUTTON_TYPE_TOGGLE.equalsIgnoreCase(shape.getType()))
			{
				this.theButton_ = createJToggleButton();
			}
			else
			{
				throw new Exception("Button Shape with up-action: " + shape.getUpAction() + " has an unsupported type code of: " + shape.getType()); 
			}
			
			
			
			/* Setup the bounds from the shape.  */
			this.theButton_.setBounds(shape.getShape().getBounds());
			this.theButton_.setOpaque(false);
			
			
			/* Setup the up and down action skin events */
			this.upActions_ = extractActions(getButtonShape().getUpAction());
			this.downActions_ = extractActions(getButtonShape().getDownAction());
			
			
			/* If this button has a paramter trigger, then disable any mouse listeners */
// TODO
			
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable setup gauge button [" + shape.getUpAction() + "][" + shape.getDownAction() + "]\n" + e.getMessage());
		}
	}

	
	
	/*********************************************************
	 * @return
	 ******************************************************/
	private AbstractButton createJButton()
	{
		JButton b = new JButton()
		{
			public static final long serialVersionUID = 0l;
			public void setBounds(int x, int y, int width, int height)
			{
				super.setBounds(x, y, width, height);
				setButtonBounds(this, x, y, width, height);
			}
		};
		
		
		/* Respond to action events on this button by sending skin events */
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				for (SkinEvent se : getDownActions())
				{
					skin_.fireSkinEvent(se);
				}
			}
		});
		
		return b;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	private AbstractButton createJToggleButton()
	{
		JToggleButton b = new JToggleButton()
		{
			public static final long serialVersionUID = 0l;
			public void setBounds(int x, int y, int width, int height)
			{
				super.setBounds(x, y, width, height);
				setButtonBounds(this, x, y, width, height);
			}
		};
		
		
		/* Respond to action events on this button by sending skin events */
		b.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (theButton_.isSelected())
				{
					for (SkinEvent se : getDownActions())
					{
						skin_.fireSkinEvent(se);
					}
				}
				else
				{
					for (SkinEvent se : getUpActions())
					{
						skin_.fireSkinEvent(se);
					}
				}
			}
		});
		
		return b;
	}
	
	/*******************************************************
	 * Given the string parameter, extract the SkinEvent
	 * objects and return them.
	 * @return
	 *******************************************************/
	private List<SkinEvent> extractActions(String action)
	{
		List<SkinEvent> skinEvents = new ArrayList<SkinEvent>();
		
		/* If no action is defined, then just return the empty list */
		if (action == null)
		{
			return skinEvents;
		}
		
		/* Don't bother if no action is defined */
		if (getButtonShape().getDownAction() != null)
		{
			/* Break out each action */
			StringTokenizer st = new StringTokenizer(action, "" + Skin.VALUE_DELIM);
			while(st.hasMoreElements())
			{
				String cmd = st.nextToken();
				String dest = null;
				
				/* Each action can optionally have a specific destination */
				if (cmd.indexOf(Skin.VALUE_DELIM_2) != -1)
				{
					dest = cmd.substring(0, cmd.indexOf(Skin.VALUE_DELIM_2));
					cmd = cmd.substring(dest.length() + 1, cmd.length());
				}
				
				skinEvents.add(new SkinEvent(dest, cmd));
			}
		}
		
		return skinEvents;
	}

	
	/********************************************************
	 * reutrn the list of up action event object.
	 * @return
	 *******************************************************/
	public List<SkinEvent> getUpActions()
	{
		return this.upActions_;
	}
	
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public List<SkinEvent> getDownActions()
	{
		return this.downActions_;
	}
	
	/*******************************************************
	 * get the button shape used to create this button.
	 * @return the shape.
	 *******************************************************/
	public ButtonShape getButtonShape()
	{
		return this.buttonShape_;
	}
	
	
	/******************************************************
	 * @return
	 *******************************************************/
	public AbstractButton getButtonComponent()
	{
		return this.theButton_;
	}
	

	/******************************************************
	 * 
	 *******************************************************/
	private void setButtonBounds(AbstractButton b, int x, int y, int width, int height)
	{
		//super.setBounds(x, y, width, height);
		
		/* Set the icon images */
		ImageIcon upIcon = this.skin_.getImage(getButtonShape().getUpImageName());
		ImageIcon downIcon = this.skin_.getImage(getButtonShape().getDownImageName());

		b.setIcon(new ImageIcon(upIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
		b.setPressedIcon(new ImageIcon(downIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
		b.setSelectedIcon(b.getPressedIcon());
		
		b.setBorderPainted(false);
		b.setContentAreaFilled(false);
		b.setFocusPainted(false);

	}	
	
	
	
}
