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


import superwaba.ext.xplat.game.GameEngine;
import net.sourceforge.JDashLite.config.Preferences;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandlerThread;
import net.sourceforge.JDashLite.ecu.comm.TestProtocol;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.Profile;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.util.ListeningMenuItem;
import net.sourceforge.JDashLite.util.MenuUtil;
import waba.fx.Graphics;
import waba.sys.Convert;
import waba.sys.Vm;
import waba.ui.ControlEvent;
import waba.ui.Event;
import waba.ui.IKeys;
import waba.ui.KeyEvent;
import waba.ui.MenuBar;


/*********************************************************
 * 
 *
 ********************************************************X*/
public class JDashLiteMainWindow extends GameEngine
{
	
	
	public static final int PROFILE_MENU_INDEX = 1;
	public static final byte DEFAULT_WINDOW_BORDER = NO_BORDER;

	public static final String JDASH_TITLE = "JDashLite";
	public static final String JDASH_CREATOR_ID = waba.sys.Settings.onDevice ? waba.sys.Settings.appCreatorId:JDASH_TITLE.substring(0,4);
	public static final int JDASH_VERSION_MAJOR = 0;
	public static final int JDASH_VERSION_MINOR = 1;



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
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public JDashLiteMainWindow()
	{
		super();  /* No Title and No Border */
		
		this.highResPrepared = true;
		waba.sys.Settings.keyboardFocusTraversable = true;
		
		/* Setup the game options */
		this.gameName = JDASH_TITLE;
		this.gameCreatorID = JDASH_CREATOR_ID;
		this.gameVersion = JDASH_VERSION_MAJOR;
		//this.gameRefreshPeriod = NO_AUTO_REFRESH;
		this.gameRefreshPeriod = 50;  /* 75 = 13fps, 50 = 20fps, 40=25fps */
		this.gameIsDoubleBuffered = false;
		this.gameDoClearScreen = false;
		this.gameHasUI = false;
		
		/* For debugging, lets set a default active profile now */
		this.activeProfile_ = null;
		
		
	}

	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public void onGameInit()
	{
		
		try
		{
			/* Turn off the auto off? */
			if (getPreferences().getInt(Preferences.KEY_DISABLE_AUTO_SCREEN_OFF, 0) == 1)
			{
				this.originalSystemOffTime_ = Vm.setDeviceAutoOff(0);
			}
			
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
				if (getPreferences().getInt(Preferences.KEY_AUTO_CONNET, 0) == 1)
				{
					doConnect();
				}
				
				
				/* Start the game engine */
				start();

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
	public void onGameExit()
	{
		/* restore the system off time */
		if (this.originalSystemOffTime_ > 0)
		{
			Vm.setDeviceAutoOff(this.originalSystemOffTime_);
		}
		
		stop();
		doDisconnect();
		killThreads();
		removeThread(this.protocolHandlerThread_);
		super.onGameExit();
	}
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void populateProfileMenu()
	{
		
		int profileCount = getPreferences().getProfileCount();
		
		for (int index = 0; index < profileCount; index++)
		{
			Profile p = new Profile();
			try
			{
				p.loadFromXml(getPreferences().getProfile(index));
			}
			catch(Exception e)
			{
				ErrorLog.error("Can't add profile to menu", e);
			}
		}
		
		
//		helpMenu[++menuIndex] = new ListeningMenuItem("About");
//		helpMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
//		{
//			public void actionPerformed()
//			{
//				doAbout();
//			}
//		});
		
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
			this.jdashOptions_ = new Preferences(super.getOptions());
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
		stop();
		
		if (this.protocolHandler_ != null)
		{
			this.protocolHandlerThread_.setEnabled(false);
		}
		
		super.popupMenuBar();
	}
	
	
	
	
	/*******************************************************
	 * Respond to window events.
	 *******************************************************/
	public void onOtherEvent(Event event)
	{
		switch(event.type)
		{
			/* Window Close, like the menu bar */
			case ControlEvent.WINDOW_CLOSED:
				if (event.target == this.menubar)
				{
					MenuUtil.dispatchMenuAction(this.menuItems_, this.menuBar_.getSelectedMenuItem());
					start();
					
					if (this.protocolHandler_ != null)
					{
						this.protocolHandlerThread_.setEnabled(true);
					}
					
				}
			break;
			
		}
//		super.onEvent(event);
	
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
		prefs.popupBlockingModal();
		
		/* Rebuild the preferences menu */
		if (prefs.getButtonPressedCode() == PreferencesWindow.BUTTON_OK)
		{
			this.menuItems_ = createMenuItems();
			this.menuBar_ = new MenuBar(this.menuItems_);
			setMenuBar(this.menuBar_);
		}
		
		/* Re-Initialize the Logger instance */
		ErrorLog.setLevel(getPreferences().getString(Preferences.KEY_LOG_LEVEL,  ErrorLog.LOG_LEVEL_OFF));

		/* Set the auto off */
		if (getPreferences().getInt(Preferences.KEY_DISABLE_AUTO_SCREEN_OFF, 0) == 1)
		{
			this.originalSystemOffTime_ = Vm.setDeviceAutoOff(0);
		}

		
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
			if (getPreferences().getInt(Preferences.KEY_TEST_MODE, 0) == 1)
			{
				this.protocolHandler_ = new TestProtocol(this.protocolHandler_);
			}
	
			/* Create a new profile display container, But, you'll notice we dont' add it. Because
			 * we are not acutally using any GUI components.  This is display ONLY */
			this.profileRenderer_ = new ProfileRenderer(this.activeProfile_, this.protocolHandler_);
			this.profileRenderer_.enableDisableParameters(getPreferences().getBoolean(Preferences.KEY_DISPLAYED_SENSORS, false));
			
			/* Add the profile render event listener.  This forces refreshes when the protocol handler fetches a batch */
			this.protocolHandler_.addProtocolEventListener(this.profileRenderer_.getEventAdapter());
			
			
		}
		catch(Exception e)
		{
			ErrorLog.error("Error setting profle: " + profileIndex, e);
			ErrorDialog.showError("Unexpected Error", e);
		}
	
	}
	
//	double theta = 0.0;
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#onPaint(waba.fx.Graphics)
	 ********************************************************/
	public void onPaint(Graphics g)
	{

//		AffineTransform t0 = AffineTransform.translateInstance(-15, -60);
//		t0.addRotate(Math.toRadians(theta));
//		t0.addScale(4,4);
//		t0.addTranslate(100, 200);
//		Coord c1 = new Coord(10,50);
//		Coord c2 = new Coord(20, 80);
//		t0.apply(c1);
//		t0.apply(c2);
//		
//		g.setForeColor(Color.BLACK);
//		g.drawLine(c1.x, c1.y, c2.x, c2.y);
////		
//		theta += 5;
//		if (theta >= 360) theta = 0;
//		Matrix t0 = AffineTransform.createTranslateMatrix(-50, -80);
//		Matrix t1 = AffineTransform.createRotateMatrix(Math.toRadians(theta));
//		Matrix t2 = AffineTransform.createTranslateMatrix(50, 80);
//		Matrix t3 = AffineTransform.createScaleMatrix(10, 2);
//		t0 = AffineTransform.multiply(t1, t0);
//		t0 = AffineTransform.multiply(t2, t0);
//
//
//		net.sourceforge.JDashLite.util.AffineTransform.Vector v1 = AffineTransform.createVector(50, 50);
//		net.sourceforge.JDashLite.util.AffineTransform.Vector v2 = AffineTransform.createVector(50, 80);
//		
//		g.setForeColor(Color.BLACK);
//		g.drawLine(v1.m_[0], v1.m_[1], v2.m_[0], v2.m_[1]);
//		
//		v1 = AffineTransform.multiply(v1, t0);
//		v2 = AffineTransform.multiply(v2, t0);
////		System.out.println(v1);
//		g.setForeColor(Color.RED);
//		g.drawLine(v1.m_[0], v1.m_[1], v2.m_[0], v2.m_[1]);
//		
//
//
//		g.clearScreen();
//		
//		int yOffset = -10;
//		Font f = null;

//		f = Font.getFont("SW", true, Font.BIG_SIZE); /* height = 30 */
//		g.setFont(f);
//		g.drawText("abcABC123 SW B+B " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("SW", false, Font.BIG_SIZE); /* height = 28 */
//		g.setFont(f);
//		g.drawText("abcABC123 SW Big " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("SW", false, Font.NORMAL_SIZE); /* height = 22 */
//		g.setFont(f);
//		g.drawText("abcABC123 SW Normal " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("Tahoma", false, Font.BIG_SIZE);  /* Height = 17 */
//		g.setFont(f);
//		g.drawText("abcABC123 Tahoma Big " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("Verdana", false, Font.BIG_SIZE); /* Height = 16 */
//		g.setFont(f);
//		g.drawText("abcABC123 Verdana " + f.fm.height, 10, yOffset += 20);
//		
//		f = Font.getFont("Tahoma", false, Font.NORMAL_SIZE);  /* Height = 15 Same size as TinyLarge NORMAl but this has tails!! */
//		g.setFont(f);
//		g.drawText("abcABC123 Tahoma " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("TinyLarge", false, Font.NORMAL_SIZE); /* height = 14 */
//		g.setFont(f);
//		g.drawText("abcABC123 TinyLarge " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("Verdana", false, Font.NORMAL_SIZE); /* Height <= 14   Verdana also has tails */
//		g.setFont(f);
//		g.drawText("abcABC123 Verdana " + f.fm.height, 10, yOffset += 20);
//
//		f = Font.getFont("Arial", false, Font.NORMAL_SIZE);  /* Height = 13 */
//		g.setFont(f);
//		g.drawText("abcABC123 Arial " + f.fm.height, 10, yOffset += 20);
//		
//		f = Font.getFont("TinySmall", false, Font.NORMAL_SIZE);  /* Height = 6 (ugly) */
//		g.setFont(f);
//		g.drawText("abcABC123 TinySmall " + f.fm.height, 10, yOffset += 20);
//		
		
//		if (1==1) return;

		
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
				this.profileRenderer_.render(g, getClientRect());
			}
			
		}
		catch(Exception e)
		{
			this.profileRenderer_ = null;
			stop();
			ErrorLog.fatal("onPaint", e);
			ErrorDialog.showError("Fatal Error", e);
			//doExit();
		}
		

	}

	
}

