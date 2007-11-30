/*******************************************************
 * 
 *  @author spowell
 *  Startup.java
 *  Aug 10, 2006
 *  $Id: Startup.java,v 1.6 2006/12/31 16:59:09 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

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
 ******************************************************/
package net.sourceforge.JDash;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;



import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.comm.ECUMonitor;
import net.sourceforge.JDash.ecu.comm.InitListener;
import net.sourceforge.JDash.ecu.comm.TestMonitor;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.ecu.param.XMLParameterLoader;
import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.ConfigureFrame;
import net.sourceforge.JDash.gui.DashboardFrame;import net.sourceforge.JDash.gui.Splash;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.logger.DatabaseLogger;
import net.sourceforge.JDash.logger.LoggerPlaybackMonitor;
import net.sourceforge.JDash.skin.SkinFactory;


/*******************************************************
 * This is a simple bootstrap class to kick off the
 * JDash frame.  Or to kick off the configuration
 * editor.
 ******************************************************/
public class Startup
{
	
	/** This is the command line argument to start the configuration window */
	public static final String CONFIG_FLAG = "-config";
	
	
	/**  The splash frame */
	private static Splash splashFrame_ = null; 
	
	/** The frame that started it all */
	private static JFrame mainFrame_ = null;
	
	/** This is the running monitor */
	private static BaseMonitor monitor_ = null;
	
	/** This is THE monitor thread */
	private static Thread monitorThread_ = null;
	

	/******************************************************
	 * Create a new startup instance.  It's private because only
	 * the main() can create a new startup class
	 ******************************************************/
	private Startup()
	{
	}
	
	
	
	/*******************************************************
	 * @param args IN - the args array from the main() method.
	 ******************************************************/
	private void startup(String args[])
	{
		boolean disableLoggingOverride = false;
		Startup.splashFrame_ = new Splash(Setup.APPLICATION);
		JFrame displayFrame = null;
			
		try
		{
			/* Show the splash frame */
			Startup.splashFrame_.setVisible(true);
			Startup.splashFrame_.setStatus(0, "Initializing Settings");
		
			/* Output the version string */
			System.out.println(Setup.APPLICATION);
			System.out.println(Setup.getSetup().getLicense());
			
			/* A new config file ALWAYS shows the config dialog */
			if (Setup.getSetup().isNew() == true)
			{
				args = new String[] {CONFIG_FLAG};
			}
			

			/* What startup mode is being requested */
			if (args.length > 0)
			{
				if (CONFIG_FLAG.equalsIgnoreCase(args[0]) == true)
				{
					
					Startup.splashFrame_.setStatus(50, "Setting Up Configuration Window");
					
					/* Show the config frame */
					displayFrame = new ConfigureFrame();
					displayFrame.setVisible(true);
					
					Startup.splashFrame_.setStatus(100, "Done");
				}
			}
			
			
			
			/* The default frame, the dashboard itself */
			if (displayFrame == null)
			{
				
				Startup.splashFrame_.setStatus(20, "Loading ECU Parameters");
				
				/* Load the XML Parameter loader */
				String ecuParamFile = Setup.getSetup().get(Setup.SETUP_CONFIG_PARAMETER_FILE);
				final XMLParameterLoader loader = new XMLParameterLoader(new File(Setup.SETUP_CONFIG_ECU_PARAMS_DIR + File.separatorChar + ecuParamFile));
				
				/* Create a parameter registry */
				ParameterRegistry paramRegistry = new ParameterRegistry();
				paramRegistry.init(loader);

				Startup.splashFrame_.setStatus(40, "Loading Skin");
				
				/* Load the skin factory and give it the parameter registry */
				SkinFactory skinFactory = createSkinFactory();
				skinFactory.setParameterRegistry(paramRegistry);
				
				
				/* Create the desired ECU monitor */
				/* If the test monitor has been checked, then override our default monitor */
				if (new Boolean(Setup.getSetup().get(Setup.SETUP_CONFIG_ENABLE_TEST)) == true)
				{
					Startup.splashFrame_.setStatus(60, "Initializing Test Monitor");
					
// TODO: Reenable this
//					disableLoggingOverride = true;
					Startup.monitor_ = new TestMonitor();
				}
				else
				{
					/* If the logger playback monitor has been checked, then override our default monitor */
					if (new Boolean(Setup.getSetup().get(Setup.SETUP_CONFIG_ENABLE_LOGGER_PLAYBACK)) == true)
					{
						Startup.splashFrame_.setStatus(60, "Initializing Log Playback Monitor");
						disableLoggingOverride = true;
						Startup.monitor_ = new LoggerPlaybackMonitor();
					}
					else
					{
						Startup.splashFrame_.setStatus(60, "Intializing " + loader.getName());
						Startup.monitor_ = createMonitor(loader);
					}
					
				}
				
				
				System.out.println("Monitor: " + Startup.monitor_.getClass().getName());

				List<Parameter> supportedParams = Startup.monitor_.init(paramRegistry, new InitListener(loader.getName())
				{
					public void update(String message, int step, int max)
					{
						int p = (int)(60.0 + (20.0 * ((double)(step / max))));
						Startup.splashFrame_.setStatus(p, getPrefix() + " - " + message);
						
					}
				});
				
				System.out.println("Supported Parameters\n-----------------------\n");
				for (Parameter p : supportedParams)
				{
					System.out.println(p.getName());
				}
				
				
				
				/* Show the monitor ecu info */
				System.out.println("\n-----------------------\n");
				System.out.println(Startup.monitor_.getEcuInfo());
				System.out.println("\n-----------------------\n");
				

				/* Create a logger instance */
				Startup.splashFrame_.setStatus(80, "Initializing Logger");
				DataLogger logger = new DatabaseLogger(loader.getName());
				logger.addParameter(paramRegistry.getParamForName(ParameterRegistry.TIME_PARAM));
				logger.disableOverride(disableLoggingOverride);

			
				
				/* create the main dashboard frame */
				displayFrame = new DashboardFrame(skinFactory.getDefaultSkin(), Startup.monitor_, logger);
				if (Startup.monitor_ instanceof LoggerPlaybackMonitor)
				{
					((LoggerPlaybackMonitor)Startup.monitor_).init((DashboardFrame)displayFrame, logger);
				}
				Startup.splashFrame_.setStatus(100 ,"Done");
				
				
				/* Show the main dashboard frame */
				displayFrame.setVisible(true);

				/* Startup the monitor thread */
				Startup.monitorThread_ = new Thread(Startup.monitor_);
				Startup.monitorThread_.start();

				
				
			}

			
			/* Hide the splash screen */
			Startup.splashFrame_.setVisible(false);
			Startup.splashFrame_.dispose();



			/* Trap the window closing to stop the jvm */
			displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			displayFrame.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent we)
				{
					shutdown();
				}
			});

			
			/* Remember the frame for error output */
			Startup.mainFrame_ = displayFrame;
			
			
						
		}
		catch(Exception e)
		{
			showException(e, true);
		}

	}
	

	/*******************************************************
	 * create a new instance of the monitor, given the setup instance.
	 * @return
	 * @throws Exception
	 ******************************************************/
	private BaseMonitor createMonitor(XMLParameterLoader loader) throws Exception
	{
		String monitorClass = loader.getMonitorClass();
		
		if ((monitorClass == null) || (monitorClass.length() == 0))
		{
			throw new Exception("There was a problem getting the monitor. The Loader object returned a [" + monitorClass + "] as the monitors class string");
		}

		/* Create an instance of the monitor class */
		Object monitor = Class.forName(monitorClass).newInstance();
		if (monitor == null)
		{
			throw new Exception("Unable to create instance of monitor class [" + monitorClass + "]");
		}
		
		if (monitor instanceof ECUMonitor == false)
		{
			throw new Exception("The specified monitor class [" + monitorClass + "] is not an implementation of the ECUMonitor class");
		}
		
		/* Cast the monitor */
		return (BaseMonitor)monitor;
					

	}

	/*******************************************************
	 * This method will return the skin factory instance that was
	 * created as a result of the confgiuration.
	 * @return the one and only skin factory
	 *******************************************************/
	public SkinFactory createSkinFactory() throws Exception
	{
		/* Create an instance of the monitor class */
		Object factory = Class.forName(Setup.getSetup().get(Setup.SETUP_CONFIG_SKINFACTORY_CLASS)).newInstance();
		if (factory == null)
		{
			throw new Exception("Unable to create instance of skin factory class [" + Setup.getSetup().get(Setup.SETUP_CONFIG_SKINFACTORY_CLASS) + "]");
		}
		
		if (factory instanceof SkinFactory == false)
		{
			throw new Exception("The specified skin factory class [" + Setup.getSetup().get(Setup.SETUP_CONFIG_SKINFACTORY_CLASS) + "] is not an implementation of the SkinFactory class");
		}
		
		/* Cast the monitor */
		SkinFactory skinFactory = (SkinFactory)factory;
		skinFactory.setDefaultSkinId(Setup.getSetup().get(Setup.SETUP_CONFIG_SKIN_ID));
			
		return skinFactory;

	}
	
	/*******************************************************
	 * Perform a clean shutdown 
	 ******************************************************/
	private static void shutdown()
	{
		try
		{
			if (Startup.monitor_ == null)
			{
				return;
			}
			
			Startup.monitor_.stop();
			
			if (Startup.monitorThread_ != null)
			{
				while (Startup.monitorThread_.isAlive())
				{
					Thread.sleep(100);
				}
			}

		}
		catch(Exception e)
		{
			showException(e, false);
		}
		finally
		{
			System.exit(1);
		}

	}
	
	
	/*******************************************************
	 *  A static show exception method.  If a frame exists, then
	 *  it will be set as the owner of the message box, if not,
	 *  then the dialog will be poped by itself.
	 *  
	 *  @param e IN - the exception to show the error for.
	 *  @param fatal IN - if true, then the system will exit after
	 *  the message is displayed.
	 *******************************************************/
	public static void showException(Exception e, boolean fatal)
	{
		
		e.printStackTrace();
		
		String msg = Setup.APPLICATION + "\n" + e.getMessage();
		
		if (e.getCause() != null)
		{
			msg += "\n" + e.getCause().getMessage();
		}
		
		if (e instanceof NullPointerException)
		{
			msg += "\n" + NullPointerException.class.getName();
			
			for (int index = 0; index < Math.min(4, e.getStackTrace().length); index++)
			{
				msg += "\n" + e.getStackTrace()[index];
			}
		}
		
		if ((Startup.splashFrame_ != null) && (Startup.splashFrame_.isVisible()))
		{
			JOptionPane.showMessageDialog(Startup.splashFrame_, msg, "Error", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(Startup.mainFrame_, msg, "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if (fatal)
		{
			Startup.shutdown();
		}
		
	}


	
	/********************************************************
	 * Startup a new instance of JDash.
	 * @param args IN - the startup parameters. At present, the only 
	 * support argument is "-config"
	 *******************************************************/
	public static void main(String args[])
	{
			new Startup().startup(args);
	}
	
}
