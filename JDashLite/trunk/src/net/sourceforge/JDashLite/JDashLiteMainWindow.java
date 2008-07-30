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
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandlerThread;
import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.Profile;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.util.ListeningMenuItem;
import net.sourceforge.JDashLite.util.MenuUtil;
import waba.fx.Graphics;
import waba.sys.Vm;
import waba.ui.ControlEvent;
import waba.ui.Event;
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
	
	/** This listener refreshes the drawing area as needed */
	private ProtocolRefreshListener protocolRefreshListener_ = null;
	
	/* This flag gets tripped when new data is read and a re-render is due. 
	 * We start with true to force the first render */
	private boolean dataUpdated_ = true;
	

	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public JDashLiteMainWindow()
	{
		super();  /* No Title and No Border */
		this.highResPrepared = true;
		waba.sys.Settings.keyboardFocusTraversable = true;
		
		/* Set the refresh listener */
		this.protocolRefreshListener_ = new ProtocolRefreshListener(this);
		
		/* Setup the game options */
		this.gameName = JDASH_TITLE;
		this.gameCreatorID = JDASH_CREATOR_ID;
		this.gameVersion = JDASH_VERSION_MAJOR;
		//this.gameRefreshPeriod = NO_AUTO_REFRESH;
		this.gameRefreshPeriod = 75;  /* 75 = 13 fps */
		this.gameIsDoubleBuffered = true;
		this.gameDoClearScreen = true;
		this.gameHasUI = false;
		
		/* For debugging, lets set a default active profile now */
		this.activeProfile_ = new Profile();
		this.activeProfile_.setName("TEST PROFILE");
		this.activeProfile_.setProtocolClass(ELMProtocol.class.getName());
		getPreferences().setString(Preferences.KEY_ACTIVE_PROFILE, this.activeProfile_.getName());
		
	}

	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public void onGameInit()
	{
		try
		{
			/* Setup the look and feel */
			waba.sys.Settings.setUIStyle((byte)getPreferences().getInt(Preferences.KEY_GUI_STYLE, waba.sys.Settings.WinCE));
			
			/* Initialize the Logger instance */
			ErrorLog.init(getPreferences().getInt(Preferences.KEY_ENABLE_ERROR_LOG, 0) == 1);
			
			/* Setup the protocol thread */
			this.protocolHandlerThread_ = new ProtocolHandlerThread();
			addThread(this.protocolHandlerThread_, false);
			
			try
			{
				
				/* Setup and Keep the menu bar reference for convinence */
				this.menuItems_ = createMenuItems();
				this.menuBar_ = new MenuBar(this.menuItems_);
				setMenuBar(this.menuBar_);
				
				/* Set the active profile.. to active!! */
				doSetActiveProfile(this.activeProfile_);

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
		stop();
		doDisconnect();
		killThreads();
		removeThread(this.protocolHandlerThread_);
		super.onGameExit();
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

		ListeningMenuItem[] mainMenu = new ListeningMenuItem[5];
		ListeningMenuItem[] profileMenu = new ListeningMenuItem[1];
		ListeningMenuItem[] helpMenu = new ListeningMenuItem[2];
		

		int menuIndex = -1;
		
		{
			mainMenu[++menuIndex] = new ListeningMenuItem("JDash");
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Connect");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed()
				{
					doConnect();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Disconnect");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed()
				{
					doDisconnect();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Preferences");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed()
				{
					doPreferences();
				}
			});
			
			mainMenu[++menuIndex] = new ListeningMenuItem("Exit");
			mainMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed()
				{
					doExit();
				}
			});
			

		}
		
		
		/* Profile Menu */
		{
			menuIndex = -1;
			profileMenu[++menuIndex] = new ListeningMenuItem("Profile");
		}
		

		/* Help Menu */
		{
			menuIndex = -1;
			helpMenu[++menuIndex] = new ListeningMenuItem("Help");
			
			helpMenu[++menuIndex] = new ListeningMenuItem("About");
			helpMenu[menuIndex].setActionListener(new ListeningMenuItem.MenuActionListener()
			{
				public void actionPerformed()
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
			
			/* create the protocol handler  */
			this.protocolHandler_ = ((ProtocolHandler)Class.forName(this.activeProfile_.getProtocolClass()).newInstance());
			
			/* Add the refresh listener.  This forces refreshes when the protocol handler fetches a batch */
			this.protocolHandler_.addProtocolEventListener(this.protocolRefreshListener_);
			
			/* Give the protocol handler access to the active profile for access to the desired params */
			this.protocolHandler_.setProfile(this.activeProfile_);
			
			/* Give the list of parameters to the renderer */
			this.profileRenderer_.setParameters(this.protocolHandler_.getSupportedParameters());
			
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
			this.protocolHandler_ = null;
		}
		
	}

	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doPreferences()
	{
		PreferencesWindow prefs = new PreferencesWindow(getPreferences());
		prefs.popupBlockingModal();
		
		/* Re-Initialize the Logger instance */
		ErrorLog.init(getPreferences().getInt(Preferences.KEY_ENABLE_ERROR_LOG, 0) == 1);
		

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
	private void doSetActiveProfile(Profile p)
	{
		this.activeProfile_ = p;
		getPreferences().setString(Preferences.KEY_ACTIVE_PROFILE, this.activeProfile_.getName());
		
		/* Stop and clean up the current profile */
		
		/* Release any resources */
		
		/* Create a new profile display container, But, you'll notice we dont' add it. Because
		 * we are not acutally using any GUI components.  This is display ONLY */
		this.profileRenderer_ = new ProfileRenderer(this.activeProfile_);
		
	
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.game.GameEngine#onPaint(waba.fx.Graphics)
	 ********************************************************/
	public void onPaint(Graphics g)
	{
		//super.onPaint(g);
		
//		try
//		{
//			throw new Exception("on paint");
//		}
//		catch(Exception te)
//		{
//			te.printStackTrace();
//		}
//		
		try
		{
			/* The renderer is setup, so let it draw */
			if (this.profileRenderer_ != null && this.dataUpdated_)
			{
				this.dataUpdated_ = false;
				this.profileRenderer_.render(g);
			}
			
//			//if (this.protocolHandler_ != null && this.protocolHandler_.isRunning())
//			System.out.println((this.protocolHandler_ != null) + "  " + (this.profileRenderer_ != null));
//			if (this.protocolHandler_ != null)
//			{
//				if (this.profileRenderer_ != null)
//				{
//					this.profileRenderer_.render(g);
//				}
//				
//			}
		}
		catch(Exception e)
		{
			ErrorLog.error("onPaint", e);
			ErrorDialog.showError("Fatal Error", e);
			doExit();
		}
		

	}

	
	
	/*********************************************************
	 * 
	 *
	 *********************************************************/
	private static class ProtocolRefreshListener extends ProtocolEventAdapter
	{
		private JDashLiteMainWindow wnd_ = null;
		
		private int startTime_ = 0;
		
		/********************************************************
		 * 
		 *******************************************************/
		public ProtocolRefreshListener(JDashLiteMainWindow wnd)
		{
			this.wnd_ = wnd;
		}
		
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#beginParameterBatch(int)
		 ********************************************************/
		public void beginParameterBatch(int count)
		{
			this.startTime_ = Vm.getTimeStamp();
		}
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener#endParameterBatch()
		 ********************************************************/
		public void endParameterBatch()
		{
			ECUParameter.SPECIAL_PARAM_RATE.setRate(Vm.getTimeStamp() - this.startTime_);
			this.wnd_.dataUpdated_ = true;
		}
		
	}


}

