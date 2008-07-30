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
import waba.fx.Rect;
import waba.io.SerialPort;
import waba.ui.ComboBox;
import waba.ui.Container;
import waba.ui.ControlEvent;
import waba.ui.Event;
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
	private ComboBox scanDisplayedOnlyComboBox_ = null;
	private ComboBox testModeComboBox_ = null;
	private ComboBox enableErrorLog_ = null;
	

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
		waba.sys.Settings.keyboardFocusTraversable = true;
		
		/* The settings tabs */
		TabPanel tabPanel = new TabPanel(new String[] {"Settings", "Profiles"});
		add(tabPanel);
		tabPanel.setGaps(CONTROL_SPACE, CONTROL_SPACE, CONTROL_SPACE, CONTROL_SPACE);
//		tabPanel.setRect(getClientRect());
		tabPanel.setRect(LEFT, TOP, FILL, FILL);
		
		/* Add the main buttons now */
 		this.addMainButtons();
		
		/* Setup the Settings Container */
		Container settingsContainer = new Container();
		settingsContainer.setRect(tabPanel.getClientRect());
		tabPanel.setPanel(0, settingsContainer);
		

		/* GUI */
		this.guiStyleComboBox_ = new ComboBox(GUI_LIST);
		this.guiStyleComboBox_.fullHeight = true;
		settingsContainer.add(this.guiStyleComboBox_, RIGHT, TOP);
		settingsContainer.add(new Label("Look&Feel:"), BEFORE - CONTROL_SPACE, SAME);
		
		
		/* Port */
		this.portComboBox_ = new ComboBox(PORT_LIST);
		this.portComboBox_.fullHeight = true;
		settingsContainer.add(this.portComboBox_, RIGHT, AFTER + CONTROL_SPACE);
		settingsContainer.add(new Label("Com Port:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Connect at start */
		this.autoConnectComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(this.autoConnectComboBox_, RIGHT, AFTER + CONTROL_SPACE);
		settingsContainer.add(new Label("Auto Connect:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Scan displayed only */
		this.scanDisplayedOnlyComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(this.scanDisplayedOnlyComboBox_, RIGHT, AFTER + CONTROL_SPACE);
		settingsContainer.add(new Label("Scan Only Displayed:"), BEFORE - CONTROL_SPACE, SAME);
		
		/* Test Mode */
		this.testModeComboBox_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(this.testModeComboBox_, RIGHT, AFTER + CONTROL_SPACE);
		settingsContainer.add(new Label("Test Mode:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* ErrorLog */
		this.enableErrorLog_ = new ComboBox(YESNO_LIST);
		settingsContainer.add(this.enableErrorLog_, RIGHT, AFTER + CONTROL_SPACE);
		settingsContainer.add(new Label("Enable Error Log:"), BEFORE - CONTROL_SPACE, SAME);

		
		/* Add the profiles container */
		this.profilesContainer_ = new ProfilesContainer(this.prefs_, tabPanel.getClientRect());
		tabPanel.setPanel(1, this.profilesContainer_);


		/* Select the current prefs */
		this.guiStyleComboBox_.select(ListItem.findItem(GUI_LIST, this.prefs_.getInt(Preferences.KEY_GUI_STYLE, GUI_LIST[0].getId())));
		this.portComboBox_.select(ListItem.findItem(PORT_LIST, this.prefs_.getInt(Preferences.KEY_COM_PORT, PORT_LIST[0].getId())));
		this.autoConnectComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_AUTO_CONNET, YESNO_LIST[0].getId())));
		this.scanDisplayedOnlyComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_DISPLAYED_SENSORS, YESNO_LIST[0].getId())));
		this.testModeComboBox_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_TEST_MODE, YESNO_LIST[0].getId())));
		this.enableErrorLog_.select(ListItem.findItem(YESNO_LIST, this.prefs_.getInt(Preferences.KEY_ENABLE_ERROR_LOG, YESNO_LIST[0].getId())));
	}

	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.AbstractWindow#okPressed()
	 ********************************************************/
	public void okPressed()
	{
		boolean requireRestart = false;

		/* Check if a restart is needed to affect a settings chagne */
		if (this.prefs_.getInt(Preferences.KEY_GUI_STYLE, -1) != ((ListItem)this.guiStyleComboBox_.getSelectedItem()).getId())
		{
			requireRestart = true;
		}
			
		this.prefs_.setInt(Preferences.KEY_GUI_STYLE, ((ListItem)this.guiStyleComboBox_.getSelectedItem()).getId());
		this.prefs_.setInt(Preferences.KEY_COM_PORT, ((ListItem)this.portComboBox_.getSelectedItem()).getId());
		this.prefs_.setInt(Preferences.KEY_AUTO_CONNET, ((ListItem)this.autoConnectComboBox_.getSelectedItem()).getId());
		this.prefs_.setInt(Preferences.KEY_DISPLAYED_SENSORS, ((ListItem)this.scanDisplayedOnlyComboBox_.getSelectedItem()).getId());
		this.prefs_.setInt(Preferences.KEY_TEST_MODE, ((ListItem)this.testModeComboBox_.getSelectedItem()).getId());
		this.prefs_.setInt(Preferences.KEY_ENABLE_ERROR_LOG, ((ListItem)this.enableErrorLog_.getSelectedItem()).getId());

		this.prefs_.save();
		
		if (requireRestart)
		{
			ErrorDialog.showInfo("Restart required|for changes to take affect");
		}
		
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

