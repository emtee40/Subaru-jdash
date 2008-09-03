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


import net.sourceforge.JDashLite.config.Preferences;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandlerThread;
import net.sourceforge.JDashLite.ecu.comm.TestProtocol;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.Profile;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.profile.StatusBar;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import net.sourceforge.JDashLite.util.ListeningMenuItem;
import net.sourceforge.JDashLite.util.MenuUtil;
import waba.fx.Color;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.sys.Convert;
import waba.sys.Settings;
import waba.sys.Vm;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.IKeys;
import waba.ui.KeyEvent;
import waba.ui.MainWindow;
import waba.ui.MenuBar;
import waba.ui.PenEvent;


/*********************************************************
 * 
 *
 ********************************************************X*/
public class JDashLiteMainWindow extends MainWindow/*GameEngine*/ implements ProtocolEventListener
{
	
	
	public static final int PROFILE_MENU_INDEX = 1;
	public static final byte DEFAULT_WINDOW_BORDER = NO_BORDER;

	public static final String JDASH_TITLE = "JDashLite";
	public static final String JDASH_CREATOR_ID = waba.sys.Settings.onDevice ? waba.sys.Settings.appCreatorId:JDASH_TITLE.substring(0,4);
	public static final int JDASH_VERSION_MAJOR = 0;
	public static final int JDASH_VERSION_MINOR = 2;



	private ListeningMenuItem[][] menuItems_ = null;
	private MenuBar menuBar_ = null;
	
	/* Hang onto the reference to the options object */
	private Preferences jdashOptions_ = null;

	/* The currently active profile */
	private Profile activeProfile_ = null;
	
	/* The active profile renderer */
	private ProfileRenderer profileRenderer_ = null;
	
	/* The current protocol handler */
	private ProtocolHandler protocolHandler_ = null;
	
	/* The handler thread */
	private ProtocolHandlerThread protocolHandlerThread_ = null;

	/* Remember the previous system off time */
	private int originalSystemOffTime_ = 0;
	
	/* force a redraw of the gauges dynamic data regardless of if a new EcU value has been detected.  It's an override */
	private boolean forceRedraw_ = false;
	
	/* Force a re-generate and draw of the static content of the gague also */
	private boolean forceStaticRedraw_ = false;
	
	/* Pause any redraws. Usually due to a menu request */
	private boolean pausePainting_ = false;
	
	/** If there is a status messgae to be displayed, put it here */
	private String statusMessage_ = null;

	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public JDashLiteMainWindow()
	{
		super();  /* No Title and No Border */
		
		this.highResPrepared = true;
		this.flicker = false;
		
		/* I don't yet know why, but setting this to true prevents the left and right keys from working to change pages */
		waba.sys.Settings.keyboardFocusTraversable = false;
		
		/* Setup the game options */
//		this.gameName = JDASH_TITLE;
//		this.gameCreatorID = JDASH_CREATOR_ID;
//		this.gameVersion = JDASH_VERSION_MAJOR;
//		this.gameRefreshPeriod = NO_AUTO_REFRESH;  /* Auto refresh is WAY hard on CPU resources!! */
		//this.gameRefreshPeriod = 50;  /* 75 = 13fps, 50 = 20fps, 40=25fps */
//		this.gameIsDoubleBuffered = false;
//		this.gameDoClearScreen = false;
//		this.gameHasUI = false;
		
		/* For debugging, lets set a default active profile now */
		this.activeProfile_ = null;
		
		
	}

	
	
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public void onStart()
	{
		
		try
		{
//			/* Turn off the auto off? */
//			if (getPreferences().getInt(Preferences.KEY_DISABLE_AUTO_SCREEN_OFF, 0) == 1)
//			{
//				this.originalSystemOffTime_ = Vm.setDeviceAutoOff(0);
//			}
			
			/* Setup the look and feel */
			waba.sys.Settings.setUIStyle((byte)getPreferences().getInt(Preferences.KEY_GUI_STYLE, waba.sys.Settings.WinCE));
			
			/* Initialize the Logger instance */
			ErrorLog.setLevel(getPreferences().getString(Preferences.KEY_LOG_LEVEL, ErrorLog.LOG_LEVEL_DEBUG));

			/* Make sure there is at least SOMETHING in the profile list */
			if (getPreferences().getProfileCount() == 0)
			{
				getPreferences().addProfile(Profile.createSampleProfile().toXml());
			}

			/* Setup the protocol thread */
			this.protocolHandlerThread_ = new ProtocolHandlerThread();
			addThread(this.protocolHandlerThread_, false);
			
			try
			{
				
				/* Set the last active profile */
				doSetActiveProfile(getPreferences().getInt(Preferences.KEY_ACTIVE_PROFILE, 0));
				
				/* Setup and Keep the menu bar reference for convinence */
				this.menuItems_ = createMenuItems();
				this.menuBar_ = new MenuBar(this.menuItems_);
				setMenuBar(this.menuBar_);
				
// TODO
//				/* Set the active profile.. to active!! */
//				doSetActiveProfile(this.activeProfile_);

				/* Auto Connect? */
				if (getPreferences().getBoolean(Preferences.KEY_AUTO_CONNET, false))
				{
					doConnect();
				}
				
				
				/* Start the game engine */
//				start();
//				refresh();
				repaint(true, true);

			}
			catch(Exception e)
			{
				ErrorDialog.showError("Error", e);
				ErrorLog.error("Error at Startup", e);
			}
			
		}
		catch(Exception e)
		{
			ErrorLog.error("Init Error", e);
			ErrorDialog.showError(e.getMessage());
			exit(1);
		}
	}
	
	
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onExit()
	 *******************************************************/
	public void onExit()
	{
		/* restore the system off time */
		if (this.originalSystemOffTime_ > 0)
		{
			Vm.setDeviceAutoOff(this.originalSystemOffTime_);
		}
		
//		stop();
		doDisconnect();
		killThreads();
		removeThread(this.protocolHandlerThread_);
//		super.onGameExit();
	}
	
	
	/*********************************************************
	 * Overidden to ensure the same options catalog is returned
	 * every time.
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#getOptions()
	 ********************************************************/
	public Preferences getPreferences()
	{
		
		if (this.jdashOptions_ == null)
		{
			this.jdashOptions_ = new Preferences("JDashLite", Settings.appCreatorId);
			//this.jdashOptions_ = new Preferences(super.getOptions());
//			this.jdashOptions_ = new Preferences();
		}
		
		return this.jdashOptions_;
	}

	/********************************************************
	 * 
	 ********************************************************/
	private ListeningMenuItem[][] createMenuItems()
	{

		int profileCount = getPreferences().getProfileCount();
		ListeningMenuItem[] mainMenu = new ListeningMenuItem[5];
		ListeningMenuItem[] profileMenu = new ListeningMenuItem[profileCount + 1];
		ListeningMenuItem[] helpMenu = new ListeningMenuItem[2];
		

		int menuIndex = -1;
		
		{
			mainMenu[++menuIndex] = new ListeningMenuItem("JDash");
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Connect");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed(Object ref)
				{
					doConnect();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Disconnect");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed(Object ref)
				{
					doDisconnect();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Preferences");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed(Object ref)
				{
					doPreferences();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Exit");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed(Object ref)
				{
					doExit();
				}
			});
			

		}
		
		
		/* Profile Menu */
		{
			menuIndex = -1;
			profileMenu[++menuIndex] = new ListeningMenuItem("Profile");
			
			/* Add each profile */
			for (int index = 0; index < profileCount; index++)
			{
				Profile p = new Profile();
				try
				{
					p.loadFromXml(getPreferences().getProfile(index));
					profileMenu[++menuIndex] = new ListeningMenuItem(p.getName(), getPreferences().getInt(Preferences.KEY_ACTIVE_PROFILE, -1) == index);
					profileMenu[menuIndex].isChecked = false;
					profileMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener(""+index)
					{
						public void actionPerformed(Object profileId)
						{
							doSetActiveProfile(Convert.toInt(profileId.toString()));
						}
					});
				}
				catch(Exception e)
				{
					ErrorLog.error("Unable to add profile to menu", e);
				}
			}
			
		}
		

		/* Help Menu */
		{
			menuIndex = -1;
			helpMenu[++menuIndex] = new ListeningMenuItem("Help");
			
			helpMenu[++menuIndex] = new ListeningMenuItem("About");
			helpMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed(Object ref)
				{
					doAbout();
				}
			});
		}

		
		/* Return the new menu itmes array */
		return 	new ListeningMenuItem[][] {mainMenu, profileMenu, helpMenu};

	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see waba.ui.Window#popupMenuBar()
	 ********************************************************/
	protected void popupMenuBar()
	{
//		stop();
		setPausePaint(true);
		
		if (this.protocolHandler_ != null)
		{
			this.protocolHandlerThread_.setEnabled(false);
		}
		
		waba.sys.Settings.keyboardFocusTraversable = true;
		super.popupMenuBar();
		repaint(true);
	}
	
	

	
	/*******************************************************
	 * Respond to window events.
	 *******************************************************/
	public void onEvent(Event event)
	{

//		System.out.println("EVENT");
		
		switch(event.type)
		{
			/* Window Close, like the menu bar */
			case ControlEvent.WINDOW_CLOSED:
				if (event.target == this.menubar)
				{
					waba.sys.Settings.keyboardFocusTraversable = false;
					MenuUtil.dispatchMenuAction(this.menuItems_, this.menuBar_.getSelectedMenuItem());
					setPausePaint(false);
//					start();
					
					if (this.protocolHandler_ != null)
					{
						this.protocolHandlerThread_.setEnabled(true);
					}
					
				}
			break;
			
			
			case PenEvent.PEN_DOWN:
				onPenDown((PenEvent)event);
			break;
			
			case KeyEvent.KEY_PRESS:
				onKey((KeyEvent)event);
			break;
			
		}

//		refresh();
//		System.out.println("Other Event");
		repaint(true);
		
	
	}
	
	

	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#onPenDown(waba.ui.PenEvent)
	 ********************************************************/
	public void onPenDown(PenEvent evt)
	{
		this.profileRenderer_.onPenDown(evt);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#onKey(waba.ui.KeyEvent)
	 ********************************************************/
	public void onKey(KeyEvent ke)
	{
		
		/* Respond to the nav keys */
		if (ke.key == IKeys.LEFT)
		{
			this.profileRenderer_.setActivePage(this.profileRenderer_.getActivePage()-1);
		}
		
		if (ke.key == IKeys.RIGHT)
		{
			this.profileRenderer_.setActivePage(this.profileRenderer_.getActivePage()+1);
		}
		
		repaint(true);
		
	}
	
	
	
	/******************************************************
	 * 
	 ********************************************************/
	private void doConnect()
	{
		
		if (this.activeProfile_ == null)
		{
			ErrorDialog.showError("No Active|Profile Selected");
			return;
		}
		
		try
		{
			/* Do a disconnect first */
			doDisconnect();
			
			/* start the protocol handler */
			int serialPort = getPreferences().getInt(Preferences.KEY_COM_PORT, -3);
			if (serialPort == -1)
			{
				throw new Exception("Serial Port Not Set");
			}
			this.protocolHandler_.setSerialPortId(serialPort);
			
			/* Connect */
			if (this.protocolHandler_.connect() == false)
			{
				throw new Exception("Connect Failure");
			}

			/* Add the handler to the thread engine */
			if (this.protocolHandler_ != null)
			{
				this.protocolHandlerThread_.setProtocolHandler(this.protocolHandler_);
				this.protocolHandlerThread_.setEnabled(true);
			}

		}
		catch(Exception e)
		{
			ErrorLog.error("Connect Error", e);
			doDisconnect();
			ErrorDialog.showError("Unexpected Error", e);
		}
	}
	

	/*******************************************************
	 * 
	 ********************************************************/
	private void doDisconnect()
	{
		/* Stop the current protocol handler */
		if (this.protocolHandler_ != null)
		{
			this.protocolHandlerThread_.setEnabled(false);
			this.protocolHandlerThread_.setProtocolHandler(null);
			this.protocolHandler_.disconnect();
		}
		
	}

	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doPreferences()
	{
		PreferencesWindow prefs = new PreferencesWindow(getPreferences());
		waba.sys.Settings.keyboardFocusTraversable = true;
		prefs.popupBlockingModal();
		waba.sys.Settings.keyboardFocusTraversable = false;
		
		/* Rebuild the preferences menu */
		if (prefs.getButtonPressedCode() == PreferencesWindow.BUTTON_OK)
		{
			this.menuItems_ = createMenuItems();
			this.menuBar_ = new MenuBar(this.menuItems_);
			setMenuBar(this.menuBar_);
		}
		
		/* Re-Initialize the Logger instance */
		ErrorLog.setLevel(getPreferences().getString(Preferences.KEY_LOG_LEVEL,  ErrorLog.LOG_LEVEL_OFF));

		
		/* re-enable/disable active params */
		this.profileRenderer_.enableDisableParameters(getPreferences().getBoolean(Preferences.KEY_DISPLAYED_SENSORS, false));
		

	}
	
	/********************************************************
	 * 
	 ********************************************************/
	private void doExit()
	{
		doDisconnect();
		this.exit(0);
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	private void doAbout()
	{
		ErrorDialog.showHelp(JDASH_TITLE + " " + JDASH_VERSION_MAJOR + "." + JDASH_VERSION_MINOR + "|Copyright Shane Powell|2008");
	}
	
	
	/*******************************************************
	 * Take the provided profile object, and set it 
	 * @param p
	 ********************************************************/
	private void doSetActiveProfile(int profileIndex)
	{
		
		if ((profileIndex < 0) || (profileIndex >= getPreferences().getProfileCount()))
		{
			return;
		}
		
		try
		{
			/* remember the active profile in our preferences */
			getPreferences().setInt(Preferences.KEY_ACTIVE_PROFILE, profileIndex);
			
			Profile p = new Profile();
			p.loadFromXml(getPreferences().getProfile(profileIndex));
			
			/* Disconnect any active connection and protocol handler */
			doDisconnect();

			/* Remember the now active profile */
			this.activeProfile_ = p;
			
			/* create the protocol handler  */
			this.protocolHandler_ = ((ProtocolHandler)Class.forName(this.activeProfile_.getProtocolClass()).newInstance());

			/* But.. is this TEST mode? */
			if (getPreferences().getBoolean(Preferences.KEY_TEST_MODE, false))
			{
				this.protocolHandler_ = new TestProtocol(this.protocolHandler_);
			}
	
			/* Create a new profile display container, But, you'll notice we dont' add it. Because
			 * we are not acutally using any GUI components.  This is display ONLY */
			this.profileRenderer_ = new ProfileRenderer(this.activeProfile_, this.protocolHandler_);
			this.profileRenderer_.enableDisableParameters(getPreferences().getBoolean(Preferences.KEY_DISPLAYED_SENSORS, false));
			
			/* Add the profile render event listener.  This forces refreshes when the protocol handler fetches a batch */
//				this.protocolHandler_.addProtocolEventListener(this.profileRenderer_.getEventAdapter());
			this.protocolHandler_.addProtocolEventListener(this);
			
			
		}
		catch(Exception e)
		{
			ErrorLog.error("Error setting profle: " + profileIndex, e);
			ErrorDialog.showError("Unexpected Error", e);
		}
	
	}
	
	
//	
//	/*******************************************************
//	 * Draw the status message, if there is one, on top of
//	 * and in the middle of everything.
//	 * @param msg
//	 ********************************************************/
//	private void renderStatusMessage(Graphics g, ColorModel cm, int width, int yCenter)
//	{
//		if (this.statusMessage_ != null)
//		{
//			
//			/* Calc positions */
//			//Font f = findFontBestFitWidth(width, this.statusMessage_, true);
//			Font f = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);
//			int textHeight = f.fm.height - f.fm.descent;
//			int textWidth = f.fm.getTextWidth(this.statusMessage_);
//			
//			/* Draw a window box */
//			g.setBackColor(Color.CYAN);
//			g.fillRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
//			g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
//			g.drawRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
//			
//			/* Draw the text */
//			g.drawText(this.statusMessage_, (width / 2) - (textWidth / 2), yCenter - (textHeight / 2));
//			
//		}
//	}
	
	/********************************************************
	 * @param pause
	 ********************************************************/
	protected void setPausePaint(boolean pause)
	{
		this.pausePainting_ = pause;
	}
	
	
	/*******************************************************
	 * @param force
	 ********************************************************/
	private void repaint(boolean force)
	{
		repaint(force, false);
	}
	
	/*******************************************************
	 * @param force
	 * @param includeStatic
	 ********************************************************/
	private void repaint(boolean force, boolean includeStatic)
	{
		/* Pause?  Don't redraw!! */
		if (this.pausePainting_)
		{
			return;
		}
		
		this.forceRedraw_ = force;
		this.forceStaticRedraw_ = includeStatic;
		repaintNow();
	}
	

	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#onPaint(waba.fx.Graphics)
	 ********************************************************/
	public void onPaint(Graphics g)
	{

//		this.forceRedraw_ = true;
//	ErrorLog.info("on Paint");	
		
		/* If no profile is yet set, then warn the user, and pop the preferences dialog */
		if (this.activeProfile_ == null)
		{
			return;
		}
		
		try
		{
			/* The renderer is setup, so let it draw */
			if (this.profileRenderer_ != null)
			{
				this.profileRenderer_.render(g, getClientRect(), ColorModel.DEFAULT_COLOR_MODEL, this.forceRedraw_, this.forceStaticRedraw_);
				this.forceRedraw_ = false;
				this.forceStaticRedraw_ = false;
			}
			
			
			/* If there is a status message pending, then render it */
			if (this.statusMessage_ != null)
			{
				
				/* Calc positions */
				//Font f = findFontBestFitWidth(width, this.statusMessage_, true);
				Font f = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);
				int textHeight = f.fm.height - f.fm.descent;
				int textWidth = f.fm.getTextWidth(this.statusMessage_);
				int yCenter = getClientRect().height / 2;
				
				/* Draw a window box */
				g.setBackColor(Color.CYAN);
				g.fillRect(10, yCenter - (textHeight / 2) - 10, getClientRect().width - 20, textHeight + 20);
				g.setForeColor(ColorModel.DEFAULT_COLOR_MODEL.get(ColorModel.DEFAULT_BORDER));
				g.drawRect(10, yCenter - (textHeight / 2) - 10, getClientRect().width - 20, textHeight + 20);
				
				/* Draw the text */
				g.drawText(this.statusMessage_, (width / 2) - (textWidth / 2), yCenter - (textHeight / 2));
				
			}
			
		}
		catch(Exception e)
		{
			this.profileRenderer_ = null;
//			stop();
			setPausePaint(true);
			ErrorLog.fatal("onPaint", e);
			ErrorDialog.showError("Fatal Error", e);
			//doExit();
		}
		

	}

	
	
		public void protocolStarted() {repaint(false);};
		public void protocolStopped() {repaint(false);};
		public void initStarted() {repaint(false);};
		public void initFinished() {this.statusMessage_ = null; repaint(false);};
		public void initStatus(String statusMessage) {this.statusMessage_ = statusMessage; repaint(false);};
		public void beginParameterBatch(int count) {repaint(false);};
		public void parameterFetched(ECUParameter p) {repaint(false);};
		public void endParameterBatch() {repaint(false);};
		public void commTX() {this.profileRenderer_.getStatusBar().setRXTXMode(StatusBar.RXTX_SEND); repaint(false);};
		public void commRX() {this.profileRenderer_.getStatusBar().setRXTXMode(StatusBar.RXTX_RECEIVE); repaint(false);};
		public void commReady() {this.profileRenderer_.getStatusBar().setRXTXMode(StatusBar.RXTX_READY); repaint(false);};
	

}