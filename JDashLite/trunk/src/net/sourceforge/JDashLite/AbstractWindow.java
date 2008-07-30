/*********************************************************
 * 
 * @author spowell
 * AbstractWindow.java
 * Jul 23, 2008
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

import waba.fx.Rect;
import waba.ui.Button;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.Window;

/*********************************************************
 * A simple palce to keep constants and common methods
 *
 *********************************************************/
public class AbstractWindow extends Window
{
	
	public static final int TYPE_OK_CANCEL = 0;
	public static final int TYPE_OK = 1;
	
	public static final int CONTROL_SPACE = 6;

	private int type_ = -1;
	
	private Button okButton_ = null;
	private Button cancelButton_ = null;

	
	/********************************************************
	 * 
	 *******************************************************/
	public AbstractWindow(String title, byte style, int type)
	{
		super(title, style);
	
		this.type_ = type;
		
	}
	
	
	/********************************************************
	 * 
	 ********************************************************/
	public void addMainButtons()
	{

		switch (this.type_)
		{

			case TYPE_OK:
				this.okButton_ = new Button("OK");
				add(this.okButton_, LEFT + CONTROL_SPACE, BOTTOM - CONTROL_SPACE);
			break;
			
			case TYPE_OK_CANCEL:
				this.okButton_ = new Button("OK");
				this.cancelButton_ = new Button("Cancel");
				add(this.okButton_, LEFT + CONTROL_SPACE, BOTTOM - CONTROL_SPACE);
				add(this.cancelButton_, AFTER + CONTROL_SPACE, SAME, this.okButton_);
			break;
		}

	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Control#onEvent(waba.ui.Event)
	 ********************************************************/
	public void onEvent(Event event)
	{
		switch (event.type)
		{
			case ControlEvent.PRESSED:
				if (event.target == this.okButton_)
				{
					//new MessageBox("Ok", "Pressed").popupModal();
					this.okPressed();
				}
				
				if (event.target == this.cancelButton_)
				{
					this.cancelPressed();
				}
			break;
				
		}
	}

	
//	/*********************************************************
//	 * (non-Javadoc)
//	 * @see waba.ui.Window#getClientRect()
//	 ********************************************************/
//	public Rect getClientRect()
//	{
//		Rect r = super.getClientRect();
//		r.height = getButtonRect().y - r.y - CONTROL_SPACE;
//		return r;
//	}
//	
//	/********************************************************
//	 * This method will return the rect of the buttons added. 
//	 * This is used to calculate screen placements for components.
//	 * @return
//	 ********************************************************/
//	private Rect getButtonRect()
//	{
//		Rect r = null;
//		switch(this.type_)
//		{
//			case TYPE_OK_CANCEL:
//				r = this.okButton_.getRect();
//				r.width = this.cancelButton_.getRect().x + this.cancelButton_.getRect().width;
//				return r;
//			
//			case TYPE_OK:
//				return okButton_.getRect();
//			
//			default:
//				return null;
//		}
//	}
	
	
	/*******************************************************
	 * This method does nothing, but you will need to override it to
	 * respond to the ok button.
	 ********************************************************/
	public void okPressed() {}
	
	
	/*******************************************************
	 * This method does nothing, but you will need to override it to
	 * respond to the cancel button.
	 ********************************************************/
	public void cancelPressed() {}
	
	

}
