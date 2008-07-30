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

import waba.sys.Vm;

/*******************************************************
 * This class is used to log any sort of error messages
 * or class exceptions for the user. Primarily used
 * for developer debugging.  The log will end up on the 
 * SuperWaba debug output.
 ******************************************************/
public class ErrorLog
{
	private static final String INFO = "INFO: ";
	private static final String ERR = "ERR: ";
	
	private static boolean enable_ = false;

	/********************************************************
	 * Initialize the logger.
	 *******************************************************/
	public static void init(boolean enable)
	{
		ErrorLog.enable_ = enable; 
	}
	
	/*******************************************************
	 * @param message
	 *******************************************************/
	public static void info(String message)
	{
		if (ErrorLog.enable_)
		{
			Vm.debug(INFO + message);
		}
	}
	
	
	/********************************************************
	 * @param message
	 ******************************************************/
	public static void error(String message)
	{
		if (ErrorLog.enable_)
		{
			Vm.debug(ERR + message);
		}
	}
	
	/*******************************************************
	 * @param message
	 * @param e
	 *******************************************************/
	public static void error(String message, Throwable e)
	{
		if (ErrorLog.enable_)
		{
			e.printStackTrace();
			Vm.debug(ERR + message);
		}
	}
	
}
