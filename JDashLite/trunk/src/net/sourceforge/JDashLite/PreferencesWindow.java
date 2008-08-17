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
import net.sourceforge.JDashLite.config.Preferences;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import waba.io.SerialPort;
import waba.ui.ComboBox;
import waba.ui.Container;
import waba.ui.Label;
import waba.ui.MainWindow;
import waba.ui.TabPanel;


/*********************************************************
 * 
 *
 ********************************************************X*/
public class PreferencesWindow extends AbstractWindow
{

	private static final ListItem[] GUI_LIST = new ListItem[]
	{
		new ListItem(waba.sys.Settings.PalmOS,	"PalmOS"),
		new ListItem(waba.sys.Settings.WinCE,	"WinCE"),
		new ListItem(waba.sys.Settings.Vista,	"Vista"),
		new ListItem(waba.sys.Settings.Flat,	"Flat"),
	};
	
	private static final ListItem[] PORT_LIST = new ListItem[] 
	{
		new ListItem(SerialPort.DEFAULT, 	"Cable"),
		new ListItem(SerialPort.USB, 		"USB"),
		new ListItem(SerialPort.BLUETOOTH, 	"Bluetooth"),
		new ListItem(SerialPort.IRCOMM,		"IR"),
	};
	
	private static final ListItem[] YESNO_LIST = new ListItem[]
	{
		new ListItem(1, "Yes"),
		new ListItem(0, "No")
	};
	
	
	private ComboBox guiStyleComboBox_ = null;
	private ComboBox portComboBox_ = null;
	private ComboBox autoConnectComboBox_ = null;
	private ComboBox disableAutoOffComboBox_ = null;
	private ComboBox scanDisplayedOnlyComboBox_ = null;
	private ComboBox testModeComboBox_ = null;
	private ComboBox logLevel_ = null;
	

	private ProfilesContainer profilesContainer_ = null;
	
	private Preferences prefs_ = null;
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public PreferencesWindow(Preferences prefs)
	{
		super("Preferences", MainWindow.ROUND_BORDER, TYPE_OK_CANCEL);
		
		this.prefs_ = prefs;
		
	}
		

	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Window#onPopup()
	 ********************************************************/
	protected void onPopup()
	{
		this.highResPrepared = true;
//
		/* Add the main buttons now */
 		int buttonTop = this.addMainButtons();

		/* The settings tabs */
		TabPanel tabPanel = new TabPanel(new String[] {"Settings", "Profiles"});
		add(tabPanel);
		tabPanel.setGaps(CONTROL_SPACE, CONTROL_SPACE, CONTROL_SPACE, CONTROL_SPACE);
		tabPanel.setRect(LEFT, TOP, FILL, FIT - CONTROL_SPACE);
		
		
		/* Setup the Settings Container */
		Container settingsContainer = new Container();
		settingsContainer.setRect(tabPanel.getClientRect());
		tabPanel.setPanel(0, settingsContainer);
		

		/* GUI */
		this.guiStyleComboBox_ = new ComboBox(GUI_LIST);
		this.guiStyleComboBox_.fullHeight = true;
		settingsContainer.add(new Label(" "), CENTER, TOP);
		settingsContainer.add(this.guiStyleComboBox_, AFTER, TOP);
		settingsContainer.add(new Label("Look&Feel:"), BEFORE - CONTROL_SPACE, SAME);
		
		
		/* Port */
		this.portComboBox_ = new ComboBox(PORT_LIST);
		this.portComboBox_.fullHeight = true;
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.portComboBox_, AFTER, SAME);
		settingsContainer.add(new Label("Com Port:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Connect at start */
		this.autoConnectComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.autoConnectComboBox_, AFTER, SAME);
		settingsContainer.add(new Label("Auto Connect:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Scan displayed only */
		this.scanDisplayedOnlyComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.scanDisplayedOnlyComboBox_, AFTER, SAME);
		settingsContainer.add(new Label("Scan Only Visible:"), BEFORE - CONTROL_SPACE, SAME);
		
		
		/* disable auto off */
		this.disableAutoOffComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.disableAutoOffComboBox_, AFTER, SAME);
		settingsContainer.add(new Label("Disable Auto Off:"), BEFORE - CONTROL_SPACE, SAME);
		
		
		/* Test Mode */
		this.testModeComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.testModeComboBox_, AFTER, SAME);
		settingsContainer.add(new Label("Test Mode:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Log Level */
		this.logLevel_ = new ComboBox(ErrorLog.LOG_LEVELS);
		settingsContainer.add(new Label(" "), CENTER, AFTER + CONTROL_SPACE);
		settingsContainer.add(this.logLevel_, AFTER, SAME);
		settingsContainer.add(new Label("Log Level:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Add the profiles container */
		this.profilesContainer_ = new ProfilesContainer(this.prefs_, tabPanel.getClientRect());
		tabPanel.setPanel(1, this.profilesContainer_);


		/* Select the current prefs */
		this.guiStyleComboBox_.select(ListItem.findItem(GUI_LIST, this.prefs_.getInt(Preferences.KEY_GUI_STYLE, GUI_LIST[0].getId())));
		this.portComboBox_.select(ListItem.findItem(PORT_LIST, this.prefs_.getInt(Preferences.KEY_COM_PORT, PORT_LIST[0].getId())));
		this.autoConnectComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_AUTO_CONNET, YESNO_LIST[0].getId())));
		this.scanDisplayedOnlyComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_DISPLAYED_SENSORS, YESNO_LIST[0].getId())));
		this.disableAutoOffComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_DISABLE_AUTO_SCREEN_OFF, YESNO_LIST[1].getId())));
		this.testModeComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_TEST_MODE, YESNO_LIST[0].getId())));
		this.logLevel_.select(this.prefs_.getString(Preferences.KEY_LOG_LEVEL, ErrorLog.LOG_LEVELS[0]));
		
	}

	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.AbstractWindow#okPressed()
	 ********************************************************/
	public void okPressed()
	{
		try
		{
			boolean requireRestart = false;
	
			/* Check if a restart is needed to affect a settings chagne */
			if ((this.prefs_.getInt(Preferences.KEY_GUI_STYLE, -1) != ((ListItem)this.guiStyleComboBox_.getSelectedItem()).getId()) ||
				(this.prefs_.getInt(Preferences.KEY_TEST_MODE, -1) != ((ListItem)this.testModeComboBox_.getSelectedItem()).getId()))
			{
				requireRestart = true;
			}
				
			/* Pull in all the editable settings */
			this.prefs_.setInt(Preferences.KEY_GUI_STYLE, ((ListItem)this.guiStyleComboBox_.getSelectedItem()).getId());
			this.prefs_.setInt(Preferences.KEY_COM_PORT, ((ListItem)this.portComboBox_.getSelectedItem()).getId());
			this.prefs_.setInt(Preferences.KEY_AUTO_CONNET, ((ListItem)this.autoConnectComboBox_.getSelectedItem()).getId());
			this.prefs_.setInt(Preferences.KEY_DISPLAYED_SENSORS, ((ListItem)this.scanDisplayedOnlyComboBox_.getSelectedItem()).getId());
			this.prefs_.setInt(Preferences.KEY_DISABLE_AUTO_SCREEN_OFF, ((ListItem)this.disableAutoOffComboBox_.getSelectedItem()).getId());
			this.prefs_.setInt(Preferences.KEY_TEST_MODE, ((ListItem)this.testModeComboBox_.getSelectedItem()).getId());
			this.prefs_.setString(Preferences.KEY_LOG_LEVEL, this.logLevel_.getSelectedItem().toString());
			
			/* Tell the profiles container to save it's changes */
			this.profilesContainer_.save();
			
			/* Save the preferences to the PDB */
			this.prefs_.save();
			
			/* Notify if a restart is needed */
			if (requireRestart)
			{
				ErrorDialog.showInfo("Restart required|for changes to take affect");
			}
			
			unpop();
		}
		catch(Exception e)
		{
			ErrorLog.error("Preferences Save Error", e);
			ErrorDialog.showError("Unable to save preferences", e);
		}
		
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

