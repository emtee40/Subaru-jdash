/*********************************************************
 * 
 * @author spowell
 * ProfileEditControl.java
 * Aug 31, 2008
 *
Copyright (C) 2008 Shane Powell

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
 *********************************************************/

package net.sourceforge.JDashLite;

import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.Profile;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import waba.fx.Color;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.ui.Control;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.PenEvent;

/*********************************************************
 * 
 *
 *********************************************************/
public class ProfileEditControl extends Control
{

	private Profile profile_ = null;
	private ProtocolHandler protocol_ = null;
	private ProfileRenderer profileRenderer_ = null;

	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileEditControl(Profile profile)
	{
		this.profile_ = profile;
	}
	
	/*******************************************************
	 * @param ph
	 ********************************************************/
	public void setProtocolHandler(ProtocolHandler ph)
	{
		this.protocol_ = ph;
		this.profileRenderer_ = new ProfileRenderer(this.profile_, this.protocol_);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Control#onEvent(waba.ui.Event)
	 ********************************************************/
	public void onEvent(Event event)
	{
		switch(event.type)
		{
			case PenEvent.PEN_UP:
				System.out.println("Pen Up");
			break;
			
			case PenEvent.PEN_DOWN:
				System.out.println("Pen Down");
			break;
			
			case PenEvent.PEN_DRAG:
				System.out.println("Pen Drag");
			break;
				
			
		}
		
		super.onEvent(event);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Control#onPaint(waba.fx.Graphics)
	 ********************************************************/
	public void onPaint(Graphics g)
	{
		super.onPaint(g);

		int width = getSize().x;
		int height = getSize().y;
		
		
		
		Rect drawRect = new Rect(0, 0, width, height);
		
		try
		{
			this.profileRenderer_.render(g, drawRect);
		}
		catch(Exception e)
		{
			ErrorLog.error("oop", e);
		}
		
		
		
		
		
		
		
		
		
		/* Finally, draw a border around it all */
		g.setColors(Color.BLACK);
		g.drawRect(0, 0, width, height);
		
	}
	
}
