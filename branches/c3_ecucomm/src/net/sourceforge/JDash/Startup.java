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
import net.sourceforge.JDash.ecu.comm.BasePort;
import net.sourceforge.JDash.ecu.comm.ECUMonitor;
import net.sourceforge.JDash.ecu.comm.InitListener;
import net.sourceforge.JDash.ecu.comm.TestMonitor;
import net.sourceforge.JDash.ecu.comm.VirtualECU;
import net.sourceforge.JDash.ecu.comm.VirtualECUPort;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.ecu.param.XMLParameterLoader;
import net.sourceforge.JDash.gui.ConfigureFrame;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.gui.Splash;
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
    
    // TODO: make this an option accessible in the configuration interface
    public static final String TESTECU_OPT = "-testecu";
    public static final String TESTECU_OPT_TEST = "test";
    public static final String TESTECU_OPT_VIRTUAL = "virtual";

    private static final String CLASSNAME_LOGGERPLAYBACKMONITOR = 
            "net.sourceforge.JDash.ecu.logger.LoggerPlaybackMonitor";
    private static final String CLASSNAME_TESTMONITOR = 
            "net.sourceforge.JDash.ecu.comm.TestMonitor";


    
	// GN: I'm sort of confused as to everything is forced to be static.
	// Is it so that you have global variables?  If you only want to allow one
	// instance I would think that's not too hard to enforce.
	
	// I could see a reason why you might want to allow more than one monitor.
	
	
	/**  The splash frame */
	private static Splash splashFrame_ = null; 
	
	/** The frame that started it all */
	private static JFrame mainFrame_ = null;
	
	/** This is the running monitor */
	private static BaseMonitor monitor_ = null;
    
    private static BasePort    port_    = null;
	
	/** This is THE monitor thread */
	private static Thread      monitorThread_ = null;

	/** These are only instantiated if you are in VirtualECU mode **/
	private static VirtualECU  vecu_       = null;
	private static Thread      vecuThread_ = null;
	//private static VirtualECUPort vecuport;

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
		boolean loggerEnableable = true;
		Startup.splashFrame_ = new Splash(Setup.APPLICATION);
		JFrame displayFrame = null;
        
        final int MODE_MAIN   = 1;
        final int MODE_CONFIG = 2;
        
        boolean bUseVirtualECU = false;
        String strOptTestECU   = "";
        int mode               = MODE_MAIN;
        int i;
                
			
		try
		{
			/* Show the splash frame */
			Startup.splashFrame_.setVisible(true);
			Startup.splashFrame_.setStatus(0, "Initializing Settings");
		
			/* Output the version string */
			System.out.println(Setup.APPLICATION);
			System.out.println(Setup.getSetup().getLicense());
			
			
            /////////////////////////////////////////////////////////
            // Processing args
            for (i=0; i < args.length; i++) 
            {
                if (args[i].equalsIgnoreCase(CONFIG_FLAG))
                {
                    mode = MODE_CONFIG;
                } 
                else if (args[i].equalsIgnoreCase(TESTECU_OPT))
                {
                    if (i == args.length-1)
                        throw new RuntimeException("Expected argument to " + TESTECU_OPT);
                    strOptTestECU = args[++i];
                }
            }

            /* A new config file ALWAYS shows the config dialog */
			if (Setup.getSetup().isNew() == true)
			{
                mode = MODE_CONFIG;
			}

            
            if (bUseVirtualECU) System.out.println("Using Virtual ECU");
            
            // End processing args
            /////////////////////////////////////////////////////////


            /* What startup mode is being requested? */
            switch (mode) {
                
			/* The default frame, main mode. the dashboard itself */
            case MODE_MAIN:
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
				
				
                // Process ECU monitor overrides
                                
				/* Instantiate any special ECU modes */
                String ecuCommMode = Setup.getSetup().get(Setup.SETUP_CONFIG_COMM_MODE);
                String strPortName = Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT);
                if (ecuCommMode.equals(Setup.SETUP_VALUE_COMM_MODE_TEST))
				{
					Startup.splashFrame_.setStatus(60, "Initializing Test Monitor");
                    // GN: Shouldn't the logger be enableable here?
					loggerEnableable = false;
					Startup.monitor_ = new TestMonitor();
                    // baseport doesn't need to be initialized.
				}
                else if (ecuCommMode.equals(Setup.SETUP_VALUE_COMM_MODE_LOGPLAY))
                {
                    Startup.splashFrame_.setStatus(60, "Initializing Log Playback Monitor");
                    loggerEnableable = false;
                    Startup.monitor_ = new LoggerPlaybackMonitor();
                    // TODO: port object for this?
                }
                else if (ecuCommMode.equals(Setup.SETUP_VALUE_COMM_MODE_VECU)) 
                {
                    Startup.splashFrame_.setStatus(60, "Initializing Virtual ECU");


                    
                    // Initialize from the bottom up.
                    // Create the virtualEcu then connect it to a virtualecu port,
                    // and connect the port to the monitor.
                    
                    // Create objects first
                    Startup.vecu_    = createVirtualECU(loader);
                    Startup.port_    = new VirtualECUPort();
                    Startup.monitor_ = createMonitor(loader);
                    

                    // Connect the VirtualECU to the VirtualECUPort
                    Startup.vecu_.connect((VirtualECUPort)Startup.port_);
                    Startup.vecu_.paramRegistry_ = paramRegistry;
                    
                    /* Startup the VirtualECU thread.  Do this after connecting the
                     * VirtualECU to the VirtualECUPort, but before connecting the
                     * protocol monitor 
                       TODO: Document in the VirtualECU class. Also make both sides
                       wait better. */
                    Startup.vecuThread_ = new Thread(Startup.vecu_, "VirtualECUThread");
                    Startup.vecuThread_.start();
                    // Give the thread some time to start.
        			try   { Thread.sleep(100);} 
                    catch ( InterruptedException e) { }
                                        
                    
                    // Connect the VirtualECUPort to the monitor
                    Startup.monitor_.initPort(Startup.port_, strPortName);
                }
                else // assume that this is the normal mode.
                {
                    Startup.splashFrame_.setStatus(60, "Initializing " + loader.getName());
                    // Initialize from the bottom up.
                    Startup.port_    = createPort(loader);
                    Startup.monitor_ = createMonitor(loader);
                    Startup.monitor_.initPort(Startup.port_, strPortName);
                    // Initialize a port object.
                }
				
				System.out.println("Monitor: " + monitor_.getClass().getName());
				if (this.port_ != null)
				{
					System.out.println("Port:    " + port_.getClass().getName());
				}
				if (this.vecu_ != null)
				{
					System.out.println("VECU:    " + this.vecu_.getClass().getName());
				}

				/* Initialize the monitor */
                // GN: TODO: report which parameter set we're loading.
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
				logger.setEnableable(loggerEnableable);

	
				
				/* Add the logger to the skin as a SkinEventListener */
				Startup.splashFrame_.setStatus(82, "Building Skin...");
				skinFactory.getDefaultSkin().addSkinEventListener(logger);
			
				/* Add the monitor to the skin as a SkinEventListener */
				Startup.splashFrame_.setStatus(84, "Connecting Monitor...");
				skinFactory.getDefaultSkin().addSkinEventListener(monitor_);
				
				/* create the main dashboard frame */
				Startup.splashFrame_.setStatus(86, "Initializing Dashboard");
				displayFrame = new DashboardFrame(skinFactory.getDefaultSkin(), Startup.monitor_, logger);
				if (Startup.monitor_ instanceof LoggerPlaybackMonitor)
				{
					((LoggerPlaybackMonitor)Startup.monitor_).init((DashboardFrame)displayFrame, logger);
				}
				Startup.splashFrame_.setStatus(100 ,"Done");
				
				
				/* Show the main dashboard frame */
				displayFrame.setVisible(true);

				/* Startup the monitor thread */
				Startup.monitorThread_ = new Thread(Startup.monitor_, "MonitorThread");
				Startup.monitorThread_.start();

                
                
                
                break;
                
            ////////////////////////////////////////////////
            // Configuration Window
                
            case MODE_CONFIG:
                Startup.splashFrame_.setStatus(50, "Setting Up Configuration Window");
					
                /* Show the config frame */
                displayFrame = new ConfigureFrame();
                displayFrame.setVisible(true);
					
                Startup.splashFrame_.setStatus(100, "Done");
                break;
                default: break; // Invalid mode. oh well.
            };
            
            
            
			
			/* Hide the splash screen */
			Startup.splashFrame_.setVisible(false);
			Startup.splashFrame_.dispose();



			/* Trap the displayFrame window closing to stop the jvm */
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
		// TODO: assert that the monitor class is of type net.sourceforge. ...?
		
		if ((monitorClass == null) || (monitorClass.length() == 0))
		{
			throw new Exception("There was a problem getting the monitor. The XMLParameterLoader object returned a [" + monitorClass + "] as the monitors class string");
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
	 * Create a port object
	 * @return
	 * @throws Exception
	 ******************************************************/
	private BasePort createPort(XMLParameterLoader loader) throws Exception
	{
		String portClass = loader.getPortClass();
		
		
		if ((portClass == null) || (portClass.length() == 0))
		{
			throw new RuntimeException(
                    "There was a problem getting the monitor. The " + 
                    "XMLParameterLoader object returned [" + 
                    portClass + "] as the monitor's class string");
		}

		/* Create an instance of the monitor class */
		Object port = Class.forName(portClass).newInstance();
		if (port == null)
		{
			throw new RuntimeException(
                    "Unable to create instance of monitor class [" + portClass + "]");
		}
		
		if (port instanceof BasePort == false)
		{
			throw new RuntimeException(
                    "The specified monitor class [" + portClass + 
                    "] is not an implementation of the BasePort class");
		}
		
		/* Cast the monitor */
		return (BasePort)port;
	}

	/*******************************************************
	 * Create a VirtualECU object
	 * @return
	 * @throws Exception
	 ******************************************************/
	private VirtualECU createVirtualECU(XMLParameterLoader loader) throws Exception
	{
		String vecuClass = loader.getVirtualECUClass();
		
		
		if ((vecuClass == null) || (vecuClass.length() == 0))
		{
			throw new RuntimeException(
                    "There was a problem getting the VirtualECU. The " + 
                    "XMLParameterLoader object returned [" + 
                    vecuClass + "] as the VirtualECU's class string");
		}

		/* Create an instance of the monitor class */
		Object vecu = Class.forName(vecuClass).newInstance();
		if (vecu == null)
		{
			throw new RuntimeException(
                    "Unable to create instance of vecuClass class [" + vecuClass + "]");
		}
		
		if (vecu instanceof VirtualECU == false)
		{
			throw new RuntimeException(
                    "The specified vecuClass class [" + vecuClass + 
                    "] is not an implementation of the VirtualECU class");
		}
		
		/* Cast the monitor */
		return (VirtualECU)vecu;
	}    
    
    
    /*
	private Object tryCreateObject(Object baseClass, 
            String className, String classDesc) throws Exception
	{
		if ((className == null) || (className.length() == 0))
		{
			throw new RuntimeException(
                    "There was a problem creating " + classDesc + " object. [" + 
                    className + "] is not a valid class name.");
		}

		// Create an instance of the monitor class 
		Object port = Class.forName(className).newInstance();
		if (port == null)
		{
			throw new RuntimeException(
                    "Unable to create instance of monitor class [" + className + "]");
		}
		
     //* e.getClass()
		if (port instanceof baseClass == false)
		{
			throw new RuntimeException(
                    "The specified monitor class [" + className + 
                    "] is not an implementation of the BasePort class");
		}
		
		// Cast the monitor 
		return (BasePort)port;
	}
    */
    
	/*******************************************************
	 * This method will return the skin factory instance that was
	 * created as a result of the confgiuration.
	 * @return the one and only skin factory
	 *******************************************************/
	public SkinFactory createSkinFactory() throws Exception
	{
		/* Create an instance of the monitor class */
        String skinfactoryClassName = Setup.getSetup().get(Setup.SETUP_CONFIG_SKINFACTORY_CLASS);
		Object factory = Class.forName(skinfactoryClassName).newInstance();
		if (factory == null)
		{
			throw new Exception(
                    "Unable to create instance of skin factory class [" + 
                    skinfactoryClassName + "]");
		}
		
		if (factory instanceof SkinFactory == false)
		{
			throw new Exception(
                    "The specified skin factory class [" + skinfactoryClassName + 
                    "] is not an implementation of the SkinFactory class");
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
	
	
	/********************************************************
	 * @param message
	 *******************************************************/
	public synchronized static void showWarning(String message)
	{
		if ((Startup.splashFrame_ != null) && (Startup.splashFrame_.isVisible()))
		{
			JOptionPane.showMessageDialog(Startup.splashFrame_, message, "Warning", JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(Startup.mainFrame_, message, "Warning", JOptionPane.WARNING_MESSAGE);
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
	public synchronized static void showException(Exception e, boolean fatal)
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
