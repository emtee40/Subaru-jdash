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
import waba.fx.Rect;
import waba.ui.Button;
import waba.ui.Container;
import waba.ui.ListBox;

/*********************************************************
 * 
 *
 *********************************************************/
public class ProfilesContainer extends Container
{

	private static final int CONTROl_SPACE = AbstractWindow.CONTROL_SPACE;
	
	private Preferences prefs_ = null;
	
	private ListBox profilesListBox_ = null;
	private Button newButton_ = null;
	private Button editButton_ = null;
	private Button deleteButton_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public  ProfilesContainer(Preferences prefs, Rect rect)
	{
		this.prefs_ = prefs;
		this.setRect(rect);
		init();
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void init()
	{
		
		/* Add the list box */
		this.profilesListBox_ = new ListBox();
		this.profilesListBox_.add("test 1");
		this.profilesListBox_.add(" test 2");
		this.profilesListBox_.setRect(LEFT, TOP, (getRect().width / 4) * 3, getRect().height);
		add(this.profilesListBox_);
		
		
		/* Create the buttons */
		this.newButton_ = new Button("New");
		this.editButton_ = new Button("Edit");
		this.deleteButton_ = new Button("Delete");
		
		/* Add them to the right */
		add(this.editButton_, this.profilesListBox_.getRect().width + CONTROl_SPACE, CENTER);
		add(this.newButton_, SAME, BEFORE - CONTROl_SPACE);
		add(this.deleteButton_, SAME, this.editButton_.getRect().y + this.editButton_.getRect().height + CONTROl_SPACE);

	}
	
	
	/********************************************************
	 * If the ok button is pressed, this method gets called.  It
	 * results in the current set of profiles being saved to the
	 * preferences object.
	 ********************************************************/
	protected void save()
	{
		
	}
}
