/*******************************************************
 * 
 *  @author s_powell
 *  ErrorLog.java
 *  Aug 2, 2007
 *  $ID:$
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
package net.sourceforge.JDashLite.error;

import sun.nio.cs.ext.ISCII91;
import waba.sys.Vm;

/*******************************************************
 * This class is used to log any sort of error messages
 * or class exceptions for the user. Primarily used
 * for developer debugging.  The log will end up on the 
 * SuperWaba debug output.
 ******************************************************/
public class ErrorLog
{
	public static final String LOG_LEVEL_OFF 		= "Off";
	public static final String LOG_LEVEL_INFO 		= "Info";
	public static final String LOG_LEVEL_WARNING	= "Warning";
	public static final String LOG_LEVEL_ERROR		= "Error";
	public static final String LOG_LEVEL_FATAL		= "Fatal";
	public static final String LOG_LEVEL_DEBUG		= "Debug";
	
	public static final String[] LOG_LEVELS = {LOG_LEVEL_OFF, LOG_LEVEL_INFO, LOG_LEVEL_WARNING, LOG_LEVEL_ERROR, LOG_LEVEL_FATAL, LOG_LEVEL_DEBUG};
	
	private static String level_ = LOG_LEVEL_OFF;

	/********************************************************
	 * Initialize the logger.
	 *******************************************************/
	public static void setLevel(String logLevel)
	{
		ErrorLog.level_ = logLevel; 
	}
	
	/*******************************************************
	 * If the current log level is at least this level, then
	 * retun true, else return false.
	 * @param level
	 * @return
	 ********************************************************/
	private static boolean isCurrentlyAtleastLevel(String level)
	{
		int currentLevel = 0;
		int checkLevel = 0;
		
		for (int index = 0; index < LOG_LEVELS.length; index++)
		{
			if (ErrorLog.level_.equals(LOG_LEVELS[index]))
			{
				currentLevel = index;
			}
			
			if (level.equals(LOG_LEVELS[index]))
			{
				checkLevel = index;
			}
		}
		
		return checkLevel <= currentLevel;
	}
	
	/*******************************************************
	 * @param message
	 *******************************************************/
	public static void info(String message)
	{
		ErrorLog.print(LOG_LEVEL_INFO, message, null);
	}
	
	/********************************************************
	 * @param message
	 ******************************************************/
	public static void warn(String message)
	{
		ErrorLog.print(LOG_LEVEL_WARNING, message, null);
	}
	
	/********************************************************
	 * @param message
	 ******************************************************/
	public static void error(String message)
	{
		ErrorLog.error(message, null);
	}
	
	
	/*******************************************************
	 * @param message
	 * @param e
	 *******************************************************/
	public static void error(String message, Throwable e)
	{
		ErrorLog.print(LOG_LEVEL_ERROR, message, e);
	}

	
	/*******************************************************
	 * @param message
	 ********************************************************/
	public static void fatal(String message)
	{
		ErrorLog.fatal(message, null);
	}
	
	/*******************************************************
	 * @param message
	 * @param e
	 *******************************************************/
	public static void fatal(String message, Throwable e)
	{
		ErrorLog.print(LOG_LEVEL_FATAL, message, e);
	}
	
	
	/*******************************************************
	 * @param message
	 ********************************************************/
	public static void debug(String message)
	{
		ErrorLog.debug(message, null);
	}
	
	/*******************************************************
	 * @param message
	 * @param e
	 *******************************************************/
	public static void debug(String message, Throwable e)
	{
		ErrorLog.print(LOG_LEVEL_DEBUG, message, e);
	}
	
	/*******************************************************
	 * @param message
	 * @param e
	 ********************************************************/
	private static void print(String level, String message, Throwable e)
	{
		
		if (ErrorLog.isCurrentlyAtleastLevel(level))
		{
			if (e != null)
			{
				e.printStackTrace();
			}
			Vm.debug(level + ": " + message);
		}
	}
}
