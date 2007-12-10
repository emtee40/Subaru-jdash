/*******************************************************
 * 
 *  @author spowell
 *  Setup.java
 *  Aug 10, 2006
 *  $Id: Setup.java,v 1.7 2006/12/31 16:59:09 shaneapowell Exp $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;


/*******************************************************
 * This class will allow the user to load, modify
 * and save the setup for this JDash program.  Setup
 * information is saved in a properties file located
 * in the running directory.  At creation, this
 * class will load all offline configuration values, and
 * setup the system environment to run the JDash.  At
 * a minimum, all that is needed is to load this class.
 * Almost all system variable values are retreived from 
 * the instance of this class.  The monitir, logger, etc
 * are all fetched via this class. Call the getSetup() method
 * to get the setup instance.
 ******************************************************/ 
public class Setup implements Version
{	
	
	/** The applicaiton version */
	public static final String VERSION = "v" + MAJOR + "." + MINOR + "." + BUILD;

	/** The application string */
	public static final String APPLICATION = "JDash / " + VERSION + " " + BUILD_DATE;

	
	/** the license string */
	public static final String LICENSE = "Protected under LGPL.  Copyright (C) 2007  Shane Powell";
	
	/** the ecu parameter file extension */
	public static final String PARAMETER_FILE_EXT = ".pml";
	
	/** The default linux serial port */
	public static final String SETUP_DEFAULT_LINUX_SERIAL_PORT = "/dev/tts/USB0";
	
	/** The default windows serial port */
	public static final String SETUP_DEFAULT_WINDOWS_SERIAL_PORT = "com1";
	
	/** The name of the log database directory */
	public static final String SETUP_CONFIG_LOGDB_DIR = "logdb";
	
	/** The name of the skins directory */
	public static final String SETUP_CONFIG_SKINS_DIR = "skins";
	
	/** The name of the ecu parameter directory */
	public static final String SETUP_CONFIG_ECU_PARAMS_DIR = "ecu";
	
	/* These setup keys are the ones that are stored in the config file */
	/** The get() config key to return the skin ID */
	public static final String SETUP_CONFIG_SKIN_ID = "skin.id";
	
	/** The get() config key to return the skin factory class name */
	public static final String SETUP_CONFIG_SKINFACTORY_CLASS = "skinfactory.class";
	
	/** The get() config key to return the monitor serial port */
	public static final String SETUP_CONFIG_MONITOR_PORT = "monitor.port";
	
	/** The get() config key to return the ecu parameter file name */
	public static final String SETUP_CONFIG_PARAMETER_FILE = "parameter.file";
	
	
	/** The flag indicating that the emulations/tester monitor should be started */
	public static final String SETUP_CONFIG_ENABLE_TEST = "enable.test";
	
	/** This flag indicating that the playback monitor should be started */
	public static final String SETUP_CONFIG_ENABLE_LOGGER_PLAYBACK = "log.playback";
	
	
	/** The jar file URL Prefix */
	private static final String JAR_PREFIX = "jar:";
	
	/** The file URL Prefix */
	private static final String FILE_PREFIX = "file:";
	
	/** The name of the license file to */
	public static final String SETUP_LICENSE_FILE = "/license.txt";
	
	/** The name of the config file that values are stored to */
	public static final String SETUP_CONFIG_FILE = "config.ini";
	
	
	
	
	/** The one and only instance of the setup class */
	private static Setup setup_ = null;
	
	/** The stored configureation values */
	private Properties configFileValues_ = new Properties();
	
	/** The runtime configuration values */
	private Properties setupProperties_ = new Properties();
	

	/** This flag indicates if this config file has loaded values or not.  If there is
	 * no config file yet to be loaded, then this would return a false */
	private boolean isNew_ = true;
	
	/*******************************************************
	 * Create a new setup instance.  It's made private, because
	 * the only correct way to access this setup is throuh
	 * the static method getSetup()
	 ******************************************************/
	private Setup()
	{
		
		
		/* Load our config file */
		try
		{
			/* Set the runtime directory */
//			set(SETUP_CONFIG_BASE_DIR, determineRuntimeDir().toString());
			
			/* Make sure that all directories exist */
			File ecuDir = new File(SETUP_CONFIG_ECU_PARAMS_DIR);
			File logDBDir = new File(SETUP_CONFIG_LOGDB_DIR);
			File skinDir = new File(SETUP_CONFIG_SKINS_DIR);
			
			if (!ecuDir.exists() && !logDBDir.exists() && !skinDir.exists())
			{
				throw new Exception("Unable to load config files.  One of the following directories is missing\n" +
						SETUP_CONFIG_ECU_PARAMS_DIR + "\n" + 
						SETUP_CONFIG_LOGDB_DIR + "\n" + 
						SETUP_CONFIG_SKINS_DIR);
			}
			
			
			

			/* Initialize the resource values */
			initializeResources();

			
			/* Check the JVM Version */
			{
				String strJavaVersion = System.getProperties().getProperty("java.version");
				double dJavaVersion = Double.parseDouble(strJavaVersion.substring(0, strJavaVersion.indexOf('.', strJavaVersion.indexOf('.') + 1)));
	
				/* Make sure it's > 1.5x */
				if (dJavaVersion < 1.5)
				{
					throw new Exception("Incompatible JVM Version " + strJavaVersion + " you must use 1.5 or greater");
				}
				
			}
	
			
			/* Load the config file */
			if (loadConfigFile() == false)
			{
				this.isNew_ = true;
				return;
			}
			else
			{
				this.isNew_ = false;
			}
			
			

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable to initialize application: " + e.getMessage());
		}

		
		
	}

	
	/*******************************************************
	 * Return true if this config file is new, and not yet
	 * saved to it's external file.
	 * @return true if this is a new config, false if it's not.
	 *******************************************************/
	public boolean isNew()
	{
		return isNew_;
	}
	

	/********************************************************
	 * Get the setup instance.  This is the method you want to
	 * use to get the one and only Setup instance.
	 * @return the Setup instance
	 *******************************************************/
	public static Setup getSetup()
	{
		if (Setup.setup_ == null)
		{
			Setup.setup_ = new Setup();
		}
		
		return Setup.setup_;
	}
	
	
	/*******************************************************
	 * Return the version string.
	 * @return the version string.
	 ******************************************************/
	public final String getVersion()
	{
		return VERSION;
	}
	
	
	/*******************************************************
	 * Return the license stirng.
	 * 
	 * @return the license string.
	 *******************************************************/
	public final String getLicense()
	{
		return LICENSE;
	}
	
	/*******************************************************
	 * Call this to get a setup configuration value.
	 * @param setupConfigName IN - the config parameter to get.
	 * @return the parameter value, if it exists.
	 ******************************************************/
	public String get(String setupConfigName)
	{
		return this.setupProperties_.getProperty(setupConfigName, this.configFileValues_.getProperty(setupConfigName));
	}
	
	/*******************************************************
	 * Call this value to set a configuration value.
	 * @param setupConfigName IN - the config parameter to set.
	 * @param value IN - the value to set.
	 ******************************************************/
	public void set(String setupConfigName, String value)
	{
		this.setupProperties_.setProperty(setupConfigName, value);
		
		/* Also, set this value in the config file, if it exists in the first place */
		if (this.configFileValues_.containsKey(setupConfigName))
		{
			this.configFileValues_.setProperty(setupConfigName, value);
		}
	}

	
	/********************************************************
	 * Determine the current runtime library dir.  This is
	 * the home directory of the program files.
	 * @return the runtime directory.
	 *******************************************************/
	private File determineRuntimeDir() throws Exception
	{
		/* Determin the location of the program files. This is where all
		 * configuration parameters are kept.  Get the license file from within the JDash.jar file, and work from there. */
		URL licenseFile = this.getClass().getResource(SETUP_LICENSE_FILE);
		if (licenseFile == null)
		{
			throw new RuntimeException("License file resource: " + SETUP_LICENSE_FILE + " not found. Cannot startup JDash witout it");
		}
		
		/* If the URL stars with a jar:, then were running from within a jar file.   If it
		 * starts with file:, then we have an extracted set of classes */
		String baseDir = licenseFile.toString();
		if (baseDir.startsWith(JAR_PREFIX) == true)
		{
			/* Strip the leading jar:file: */
			baseDir = baseDir.substring(baseDir.indexOf(FILE_PREFIX) + FILE_PREFIX.length());
			
			/* Strip back to the 2nd / character. This will remove the path to the license file, and the jar file */
			baseDir = baseDir.substring(0, baseDir.lastIndexOf("/"));
			baseDir = baseDir.substring(0, baseDir.lastIndexOf("/") + 1);
		}
		else if (baseDir.startsWith(FILE_PREFIX) == true)
		{
			/* Strip the leading file: */
			baseDir = baseDir.substring(FILE_PREFIX.length());
			
			/* Strip back license file */
			baseDir = baseDir.substring(0, baseDir.lastIndexOf("/") + 1);
		}
		else
		{
			throw new RuntimeException("We were unable to determine the location of the application runtime.\n" + baseDir);
		}
	
		/* Convert to a URL, then to a URL, then create a file from it */
		File baseDirFile = new File(new URL("file://" + baseDir).toURI());
		
		if (baseDirFile.exists() == false)
		{
			throw new Exception("Runtime Directory doesn't seem to exist.\n" + baseDirFile.getAbsolutePath() + "\nThis is not really possible, so I think you've found a bug");
		}

		
		if (baseDirFile.isDirectory() == false)
		{
			throw new Exception("Runtime Directory doesn't appear to be a directory at all.\n" + baseDirFile.getAbsoluteFile() + "\nThis is not really possible, so I think you've found a big");
		}
		
		return baseDirFile;
	}
	
	/*******************************************************
	 * This method will load the setup config file into memory.
	 * Overwritting any currently set values.
	 * 
	 * @return true if the file was loaded, false if it was not
	 *******************************************************/
	public boolean loadConfigFile() throws Exception
	{
		/* Create a new properties object */
		this.configFileValues_ = new Properties();
		
		/* Create the config file object */
		//File configFile = new File(get(SETUP_CONFIG_BASE_DIR), SETUP_CONFIG_FILE);
		File configFile = new File(SETUP_CONFIG_FILE);
		
		/* If the config file doesn't exist, then set the defaults */
		if (configFile.exists() == false)
		{
			throw new Exception("Unable to find config file in runtime path.");
		
//			/* Set the config values with blanks / Defaults */
//			this.configFileValues_.setProperty(SETUP_CONFIG_PARAMETER_FILE, "");
//			
//			/* If this is a windows system, then the default monitor port is different */
//			if (System.getProperty("os.name").indexOf("Windows") >= 0)
//			{
//				this.configFileValues_.setProperty(SETUP_CONFIG_MONITOR_PORT, SETUP_DEFAULT_WINDOWS_SERIAL_PORT);
//			}
//			else
//			{
//				this.configFileValues_.setProperty(SETUP_CONFIG_MONITOR_PORT, SETUP_DEFAULT_LINUX_SERIAL_PORT);
//			}
//			
//			this.configFileValues_.setProperty(SETUP_CONFIG_SKINFACTORY_CLASS, "");
//			this.configFileValues_.setProperty(SETUP_CONFIG_SKIN_ID, "");
//			this.configFileValues_.setProperty(SETUP_CONFIG_ENABLE_TEST, Boolean.FALSE.toString());
//			this.configFileValues_.setProperty(SETUP_CONFIG_ENABLE_LOGGER_PLAYBACK, Boolean.FALSE.toString());
//			
//			this.setupProperties_.putAll(this.configFileValues_);
//			
//			return false;

		}
		
		/* Load the config file */
		this.configFileValues_.load(new FileInputStream(configFile));
		return true;
		
	}
	
	/*******************************************************
	 * Save the current config values to the config file.
	 * @throws Exception
	 *******************************************************/
	public void saveConfigFile() throws Exception
	{
		/* Create the config file object */
//		File configFile = new File(get(SETUP_CONFIG_BASE_DIR), SETUP_CONFIG_FILE);
		File configFile = new File(SETUP_CONFIG_FILE);
		
		this.configFileValues_.store(new FileOutputStream(configFile), "JDash Config");
	}
	
	
	
	
	/********************************************************
	 * Given the current state of the config, this method
	 * will setup the system properties that are needed
	 * in order to run the application.
	 *******************************************************/
	private void initializeResources() throws Exception
	{
//		File baseDir = new File(get(SETUP_CONFIG_BASE_DIR));
		
		/* Setup the minimum system parameters.  Copy in our config values, and set a few manualy */
//		set(SETUP_CONFIG_BASE_DIR, baseDir.toString()); 
		
		
//		/* Modify the class path to include the jar files found here */
//		File[] allFiles = baseDir.listFiles();
//		for (File f : allFiles)
//		{
//			/* If the file IS a file, and it ends with .jar, then append it to the class path */
//			if (f.isFile() == true)
//			{
//				if (f.getName().endsWith(".jar") || f.getName().endsWith(".JAR"))	
//				{
//					addClassPathURL(f.toURL());
//					//System.setProperty("java.class.path", System.getProperty("java.class.path") + libPathSeparator + f.getAbsolutePath());
//						/* Also, if the .jar file is named XXXX.skin.jar, then this is a skin jar file */
//				}
//			}
//		}
//		
//		/* Set the native library path to include our base directory */
//		addLibDir(baseDir);
	}
	
	
	/********************************************************
     * Add the URL specified to the classpath.
     *
     * @param u
     * @throws IOException
     *******************************************************/
    private void addClassPathURL(URL u) throws Exception
    {
    	Class[] parameters = new Class[]{URL.class};


        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        Method method = sysclass.getDeclaredMethod("addURL",parameters);
        method.setAccessible(true);
        method.invoke(sysloader,new Object[]{ u });

    }


	/*******************************************************
	 * Modify the library path at runtime by adding the directory
	 ******************************************************/
	private static void addLibDir(File dir) throws IOException
	{
		try
		{
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			for (int i = 0; i < paths.length; i++)
			{
				if (dir.toString().equals(paths[i]))
				{
					return;
				}
			}
			
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths,0,tmp,0,paths.length);
			tmp[paths.length] = dir.toString();
			field.set(null,tmp);
		}
		catch (IllegalAccessException e)
		{
			throw new IOException("Failed to get permissions to set library path");
		}
		catch (NoSuchFieldException e)
		{
			throw new IOException("Failed to get field handle to set library path");
		}
		
	}
	

}

