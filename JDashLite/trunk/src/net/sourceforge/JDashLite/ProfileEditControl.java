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
import net.sourceforge.JDashLite.profile.color.ColorModel;
import waba.fx.Color;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.ui.Control;
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

	private Rect activeElementRect_ = null;
	
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
//				System.out.println("Pen Up");
			break;
			
			case PenEvent.PEN_DOWN:
//				System.out.println("Pen Down");
				doPenDown((PenEvent)event);
			break;
			
			case PenEvent.PEN_DRAG:
//				System.out.println("Pen Drag");
			break;
				
			
		}
		
		//super.onEvent(event);
		
		repaint();
	}
	
	
	/*******************************************************
	 * @param pe
	 ********************************************************/
	private void doPenDown(PenEvent pe)
	{
		/* Unlike the main window, we are not going to forward the event to teh 
		 * profile render.  Instead, we're going to deal with the events here */
		
		
		/* Look for a page change event.*/
		int pageButtonIndex = this.profileRenderer_.getPageButtonAt(pe.x, pe.y);
		if (pageButtonIndex > -1)
		{
			this.profileRenderer_.setActivePage(pageButtonIndex);
			this.activeElementRect_ = null;
			return;
		}
		
		
		/* Failing that, look for a gauge selection event */
		int pageIndex = this.profileRenderer_.getActivePage();
		int rowIndex =  this.profileRenderer_.getRowAt(pageIndex, pe.x, pe.y);
		int gaugeIndex = this.profileRenderer_.getGaugeAt(pageIndex, pe.x, pe.y);
		
		/* If no gauge is found, then nothing to do */
		if (gaugeIndex == -1)
		{
			return;
		}

		Rect gaugeRect = this.profileRenderer_.getGaugeRect(pageIndex, rowIndex, gaugeIndex);
		this.activeElementRect_ = gaugeRect;
		
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
			/* We need to re-set the active page each time to force a redraw of the elements */
			this.profileRenderer_.setActivePage(this.profileRenderer_.getActivePage());
			this.profileRenderer_.render(g, drawRect, ColorModel.DEFAULT_MODEL, true);
		}
		catch(Exception e)
		{
			ErrorLog.error("oop", e);
		}
		
		

		/* draw a border around it all */
		g.setForeColor(Color.BLACK);
		g.drawRect(0, 0, width, height);

		

		/* And, the active element identifying rect */
		if (this.activeElementRect_ != null)
		{
			g.setForeColor(Color.RED);
			g.drawRect(this.activeElementRect_.x, this.activeElementRect_.y, this.activeElementRect_.width, this.activeElementRect_.height);
			g.drawRect(this.activeElementRect_.x+1, this.activeElementRect_.y+1, this.activeElementRect_.width-2, this.activeElementRect_.height-2);
			g.drawRect(this.activeElementRect_.x+2, this.activeElementRect_.y+2, this.activeElementRect_.width-4, this.activeElementRect_.height-4);
//			g.drawCircle(this.activeElementRect_.x + (this.activeElementRect_.width / 2), 
//						this.activeElementRect_.y + (this.activeElementRect_.height / 2), 
//						Math.min(this.activeElementRect_.width, this.activeElementRect_.height) / 2);
		}
		
		
		
	}
	
}
