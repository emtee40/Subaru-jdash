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
import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
import net.sourceforge.JDashLite.ecu.comm.SSM.SSMProtocol;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.profile.Profile;
import waba.ui.Button;
import waba.ui.ComboBox;
import waba.ui.Edit;
import waba.ui.Label;
import waba.ui.ListBox;
import waba.ui.MainWindow;


/*********************************************************
 * 
 *
 ********************************************************X*/
public class ProfileEditWindow extends AbstractWindow
{

	
	private static final ListItem[] PROTOCOL_LIST = new ListItem[]
	{
		new ListItem(ELMProtocol.class.getName(),	"ELM OBD2"),
		new ListItem(SSMProtocol.class.getName(),	"Subaru SSM")
	};
	
	private Profile profile_ = null;
	
	private Edit profileNameEditBox_ = null;
	private ComboBox protocolHandlerComboBox_ = null;
	private ListBox pagesListBox_ = null;
	
	private Button newButton_ = null;
	private Button editButton_ = null;
	private Button deleteButton_ = null;
	private Button upButton_ = null;
	private Button downButton_ = null;
	
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
		this.protocolHandlerComboBox_ = new ComboBox(PROTOCOL_LIST);
		this.protocolHandlerComboBox_.fullHeight = true;
		add(new Label("Protocol:"), LEFT + CONTROL_SPACE, AFTER + CONTROL_SPACE);
		add(this.protocolHandlerComboBox_, AFTER + CONTROL_SPACE, SAME);
		
		
		/* Add the pages list box */
		add(new Label("Pages:"), LEFT + CONTROL_SPACE, AFTER + CONTROL_SPACE);
		this.pagesListBox_ = new ListBox();
		add(this.pagesListBox_);
		this.pagesListBox_.setRect(AFTER + CONTROL_SPACE, 
								SAME, 
								(getClientRect().width / 4) * 2, 40);
		this.pagesListBox_.setRect(this.pagesListBox_.getRect().x,
								this.pagesListBox_.getRect().y,
								this.pagesListBox_.getRect().width,
								buttonTop - CONTROL_SPACE - this.pagesListBox_.getRect().y);		

		/* Create the buttons */
		this.newButton_ = new Button("New");
		this.editButton_ = new Button("Edit");
		this.deleteButton_ = new Button("Delete");
		this.upButton_ = new Button("Up");
		this.downButton_ = new Button("Down");
		
		/* Add them to the right */
		add(this.newButton_, AFTER + CONTROL_SPACE, SAME + CONTROL_SPACE);
		add(this.editButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.deleteButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.upButton_, SAME, AFTER + CONTROL_SPACE);
		add(this.downButton_, SAME, AFTER + CONTROL_SPACE);
		
		
		
		
		/* Set the current profile values */
		this.profileNameEditBox_.setText(this.profile_.getName() == null?"New Profile":this.profile_.getName());
		this.protocolHandlerComboBox_.select(ListItem.findItem(PROTOCOL_LIST, this.profile_.getProtocolClass()));
		
		for (int index = 0; index < this.profile_.getPageCount(); index++)
		{
			this.pagesListBox_.add(new ListItem(index, "Page " + (index + 1)));
		}
		
		
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

