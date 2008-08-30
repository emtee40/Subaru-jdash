/*********************************************************
 * 
 * @author spowell
 * ProfilesContainer.java
 * Jul 26, 2008
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

import net.sourceforge.JDashLite.config.Preferences;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.Profile;
import waba.fx.Rect;
import waba.ui.Button;
import waba.ui.Container;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.ListBox;

/*********************************************************
 * 
 *
 *********************************************************/
public class ProfilesContainer extends Container
{

	private static final int CONTROL_SPACE = AbstractWindow.CONTROL_SPACE;
	
	private Preferences prefs_ = null;
	
	private ListBox profilesListBox_ = null;
	private Button newButton_ = null;
	private Button editButton_ = null;
	private Button deleteButton_ = null;
	private Button upButton_ = null;
	private Button downButton_ = null;
	
	
//	private Vector profiles_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public  ProfilesContainer(Preferences prefs, Rect rect)
	{
		this.prefs_ = prefs;
		this.setRect(rect);
		
//		/* Load the profiles */
//		int profileCount = this.prefs_.getProfileCount();
////		this.profiles_ = new Vector(profileCount);
//
//		/* Load each profile into the vector */
//		for (int index = 0; index < profileCount; index++)
//		{
//			Profile profile = new Profile();
//			
//			try
//			{
//				profile.loadFromXml(this.prefs_.getProfile(index));
//			}
//			catch(Exception e)
//			{
//				ErrorLog.error("Error loading profile ", e);
//				ErrorDialog.showError("Error loading profile " + index);
//			}
//
//			this.profiles_.addElement(profile);
//		}

		init();

	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void init()
	{
		

		/* Add the list box */
		this.profilesListBox_ = new ListBox();
		this.profilesListBox_.setRect(LEFT, TOP, (getRect().width / 4) * 3, getRect().height);
		add(this.profilesListBox_);

		/* Load the profiles */
		int profileCount = this.prefs_.getProfileCount();

		/* Load each profile into the vector */
		for (int index = 0; index < profileCount; index++)
		{
			Profile profile = new Profile();
			
			try
			{
				profile.loadFromXml(this.prefs_.getProfile(index));
			}
			catch(Exception e)
			{
				ErrorLog.error("Error loading profile ", e);
				ErrorDialog.showError("Error loading profile " + index);
			}

			//this.profiles_.addElement(profile);
			this.profilesListBox_.add(profile);
		}

		
		/* Create the buttons */
		this.newButton_ = new Button("New");
		this.editButton_ = new Button("Edit");
		this.deleteButton_ = new Button("Delete");
		this.upButton_ = new Button("Up");
		this.downButton_ = new Button("Down");
		
		/* Add them to the right */
		add(this.newButton_, this.profilesListBox_.getRect().width + CONTROL_SPACE, SAME + CONTROL_SPACE);
		add(this.editButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.deleteButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.upButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.downButton_, SAME, AFTER + CONTROL_SPACE);


//		/* Add the profiles to the list box */
//		int profileCount = this.prefs_.getProfileCount();
//		this.profiles_ = new Vector(profileCount);
//		
//		for (int index = 0; index < profileCount; index++)
//		{
//			Profile profile = new Profile();
//			
//			try
//			{
//				profile.loadFromXml(this.prefs_.getProfile(index));
//			}
//			catch(Exception e)
//			{
//				ErrorLog.error("Error loading profile ", e);
//				ErrorDialog.showError("Error loading profile " + index);
//			}
//
//			this.profiles_.addElement(profile);
//			this.profilesListBox_.add(new ListItem(index, profile.getName()));
//		}
		
		
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
				if (event.target == this.newButton_)
				{
					doNewProfile();
				}
				
				if (event.target == this.editButton_)
				{
					doEditProfile();
				}
				
				if (event.target == this.deleteButton_)
				{
					doDeleteProfile();
				}
				
				if (event.target == this.upButton_)
				{
					doMoveProfileUp();
				}
				
				if (event.target == this.downButton_)
				{
					doMoveProfileDown();
				}
				
			break;
				
		}
	}
	
	/********************************************************
	 * If the ok button is pressed, this method gets called.  It
	 * results in the current set of profiles being saved to the
	 * preferences object.
	 ********************************************************/
	protected void save() throws Exception
	{
		
		/* Clear them all */
		this.prefs_.deleteAllProfiles();
		
		/* Save the current list one by one in order */
		for (int index = 0; index < this.profilesListBox_.size(); index++)
		{
			Profile profile = (Profile)this.profilesListBox_.getItemAt(index);
			this.prefs_.addProfile(profile.toXml());
		}
		
	}
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doNewProfile()
	{
		
		/* Create a new profile */
		Profile newProfile = new Profile();
		
		/* Pop up the edit profile dialog */
		ProfileEditWindow editWindow = new ProfileEditWindow(newProfile);
		editWindow.popupBlockingModal();
		
		if (editWindow.getButtonPressedCode() == ProfileEditWindow.BUTTON_OK)
		{
			this.profilesListBox_.add(newProfile);
			this.profilesListBox_.repaint();
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doEditProfile()
	{
		int selectedIndex = this.profilesListBox_.getSelectedIndex();
		if (selectedIndex < 0)
		{
			return;
		}
		
		Profile profile = (Profile)this.profilesListBox_.getItemAt(selectedIndex);

		/* Pop up the edit profile dialog */
		ProfileEditWindow editWindow = new ProfileEditWindow(profile);
		editWindow.popupBlockingModal();

		if (editWindow.getButtonPressedCode() == ProfileEditWindow.BUTTON_OK)
		{
			this.profilesListBox_.setItemAt(selectedIndex, profile);
		}
		else
		{
			// TODO
//			System.out.println("Discard and re-load profile");
		}
		
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doDeleteProfile()
	{
		
		int selectedIndex = this.profilesListBox_.getSelectedIndex();
		if (selectedIndex >= 0)
		{
			this.profilesListBox_.remove(selectedIndex);
		}
		
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doMoveProfileUp()
	{
		int selectedIndex = this.profilesListBox_.getSelectedIndex();
		
		if (selectedIndex <= 0)
		{
			return;
		}
		
		if (this.profilesListBox_.size() < 2)
		{
			return;
		}
		
		Object selectedObject = this.profilesListBox_.getItemAt(selectedIndex);
		this.profilesListBox_.remove(selectedIndex);
		this.profilesListBox_.insert(selectedObject, selectedIndex-1);

	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doMoveProfileDown()
	{
		int selectedIndex = this.profilesListBox_.getSelectedIndex();
		
		if (selectedIndex == -1)
		{
			return;
		}
		
		if (this.profilesListBox_.size() < 2)
		{
			return;
		}
		
		if (selectedIndex >= this.profilesListBox_.size())
		{
			return;
		}
		
		Object selectedObject = this.profilesListBox_.getItemAt(selectedIndex);
		this.profilesListBox_.remove(selectedIndex);
		this.profilesListBox_.insert(selectedObject, selectedIndex+1);
	}
	
}
