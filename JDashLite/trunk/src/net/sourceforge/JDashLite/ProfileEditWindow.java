/*********************************************************
 * 
 * @author spowell
 * JDashLiteMainWindow.java
 * Jul 21, 2008
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


import net.sourceforge.JDashLite.config.ListItem;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
import net.sourceforge.JDashLite.ecu.comm.SSM.SSMProtocol;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.profile.Profile;
import waba.fx.Rect;
import waba.ui.ComboBox;
import waba.ui.Edit;
import waba.ui.Event;
import waba.ui.Label;
import waba.ui.MainWindow;


/*********************************************************
 * 
 *
 ********************************************************X*/
public class ProfileEditWindow extends AbstractWindow
{

	
	private Profile profile_ = null;
	
	private Edit profileNameEditBox_ = null;
	private ComboBox protocolHandlerComboBox_ = null;

	private ProfileEditControl profileEdit_ = null;
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public ProfileEditWindow(Profile profile)
	{
		super("Profile", MainWindow.ROUND_BORDER, TYPE_OK_CANCEL);
		this.profile_ = profile;
	}
		

	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Window#onPopup()
	 ********************************************************/
	protected void onPopup()
	{
		this.highResPrepared = true;

		/* Add the main buttons now */
 		int buttonTop = this.addMainButtons();

 		/* Add the name edit window */
 		this.profileNameEditBox_ = new Edit();
 		add(new Label("Name:"), LEFT + CONTROL_SPACE, TOP + CONTROL_SPACE);
 		add(this.profileNameEditBox_, AFTER + CONTROL_SPACE, SAME);
 		this.profileNameEditBox_.setRect(this.profileNameEditBox_.getRect().x, 
 										this.profileNameEditBox_.getRect().y, 
 										getClientRect().width - this.profileNameEditBox_.getRect().x - CONTROL_SPACE, 
 										PREFERRED);
 		this.profileNameEditBox_.getRect().width = getClientRect().width - this.profileNameEditBox_.getRect().x - CONTROL_SPACE;
 		
		/* Protocol Handler */
		this.protocolHandlerComboBox_ = new ComboBox(ProtocolHandler.PROTOCOL_LIST);
		this.protocolHandlerComboBox_.fullHeight = true;
		add(new Label("Protocol:"), LEFT + CONTROL_SPACE, AFTER + CONTROL_SPACE);
		add(this.protocolHandlerComboBox_, AFTER + CONTROL_SPACE, SAME);
		
		

		/* Set the current profile values */
		this.profileNameEditBox_.setText(this.profile_.getName() == null?"New Profile":this.profile_.getName());
		this.protocolHandlerComboBox_.select(ListItem.findItem(ProtocolHandler.PROTOCOL_LIST, this.profile_.getProtocolClass()));

		
		
		/* Add Profile Edit Control.  We'll manually calculate it's rect, and make it square and 2/3 the width */
		Rect profileEditRect = new Rect(getClientRect().x + CONTROL_SPACE, 
										this.protocolHandlerComboBox_.getRect().y + this.protocolHandlerComboBox_.getRect().height + CONTROL_SPACE, 
										(getClientRect().width - (CONTROL_SPACE * 2)) / 3 * 2, 
										(getClientRect().width - (CONTROL_SPACE * 2)) / 3 * 2);
		//profileEditRect.width = Math.min(profileEditRect.width, profileEditRect.height);
		//profileEditRect.height = profileEditRect.width;
		profileEditRect.height = Math.min(profileEditRect.height, (buttonTop - CONTROL_SPACE) - (this.protocolHandlerComboBox_.getRect().x + this.protocolHandlerComboBox_.getRect().height));
		this.profileEdit_ = new ProfileEditControl(this.profile_); 
		this.profileEdit_.setRect(profileEditRect);
		add(this.profileEdit_);

		
		ProtocolHandler ph = null;
		try
		{
			ph = (ProtocolHandler)Class.forName(this.profile_.getProtocolClass()).newInstance();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
		
		this.profileEdit_.setProtocolHandler(ph);
		
	}

	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Control#onEvent(waba.ui.Event)
	 ********************************************************/
	public void onEvent(Event event)
	{
		super.onEvent(event);
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.AbstractWindow#okPressed()
	 ********************************************************/
	public void okPressed()
	{
		if (this.protocolHandlerComboBox_.getSelectedIndex() < 0)
		{
			ErrorDialog.showError("No Protocol Selected");
			return;
		}

		/* Save the name */
		this.profile_.setName(this.profileNameEditBox_.getText());
		
		/* Save the protocol */
		if (this.protocolHandlerComboBox_.getSelectedIndex() >= 0)
		{
			this.profile_.setProtocolClass(((ListItem)this.protocolHandlerComboBox_.getSelectedItem()).getStringId());
		}
		
		/* The pages are saved as they are edited */
		
		unpop();
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.AbstractWindow#cancelPressed()
	 ********************************************************/
	public void cancelPressed()
	{
		unpop();
	}
	
	
	
}

