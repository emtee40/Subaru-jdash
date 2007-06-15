/*******************************************************
 * 
 *  @author spowell
 *  Logger.java
 *  Aug 23, 2006
 *  $Id: DataLogger.java,v 1.5 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.logger;

import java.util.List;
import java.util.Observer;

import net.sourceforge.JDash.ecu.param.Parameter;


/*******************************************************
 * This is the interface needed to implement in order for
 * a class to be a logger.
 ******************************************************/
public abstract class DataLogger implements Observer
{
	
		/********************************************************
		 * The enableability of a logger can be overidden with this
		 * method.  If this is set to false, then logging cannt not be
		 * enabled. If true, then loggging can be enabled.
		 * @param enableOverride
		 * @throws Exception
		 *******************************************************/
		public abstract void disableOverride(boolean enableOverride) throws Exception;
	
		/********************************************************
		 * Once an instance of a logger is created, this method will
		 * be called to set the parameters list that this logger
		 * needs to keep track of, and log values for.
		 * @param parameters
		 *******************************************************/
		public abstract void setParameters(List<Parameter> parameters) throws Exception;
		
		/********************************************************
		 * Add a single parameter to this logger.
		 * 
		 * @param param IN - the param to add.
		 * @throws Exception if a problem occured adding the param.
		 ******************************************************/
		public abstract void addParameter(Parameter param) throws Exception;
		
		/********************************************************
		 * Return true if this logger is currently enabled.
		 * False if it is not.
		 * @return
		 * @throws Exception
		 *******************************************************/
		public abstract boolean isEnabled() throws Exception;
		
		/*******************************************************
		 * Enable or disable logging.  This kinda the same thing
		 * as start and stop.  If this logger is designed
		 * to hold unique logs, it's up to you to generate
		 * the seperate logs as needed.
		 *
		 * @param enable
		 *******************************************************/
		public abstract void enable(boolean enable) throws Exception;
		
		
		/*******************************************************
		 * Return a count of the number of logs this logger has.
		 * @return
		 * @throws Exception
		 *******************************************************/
		public abstract int getLogCount() throws Exception;
		
		
		/*******************************************************
		 * Get the name of the log identified by the logIndex value.
		 * @param logIndex
		 * @return
		 * @throws Exception
		 *******************************************************/
		public abstract String getLogName(int logIndex) throws Exception;
		
		
		/*******************************************************
		 * Modify the name of an existing log. By default, a log is
		 * auto named by the logger. But, you can modify the log
		 * name with this method.
		 * 
		 * @param logIndex IN - the log index to rename.
		 * @param name IN - the new name.
		 * @throws Exception
		 *******************************************************/
		public abstract void setLogName(int logIndex, String name) throws Exception;
		
		
		/*******************************************************
		 * Delete the log at the given index.
		 * 
		 * @param logIndex
		 * @return
		 * @throws Exception
		 *******************************************************/
		public abstract void deleteLog(int logIndex) throws Exception;

		
		/********************************************************
		 * @throws Exception
		 *******************************************************/
		public abstract void deleteAll() throws Exception;
		
		/*******************************************************
		 * If your intent is to use this logger for reading parameter
		 * values back to you, then call this method to prepare the
		 * list of parameters for playback.  Now, use the getNext() and
		 * getPrevious() methods to get the values from the log.
		 * 
		 * @param logIndex IN - the log index to prepare.
		 *******************************************************/
		public abstract void prepareForPlayback(int logIndex) throws Exception;
		
		
		
		/*******************************************************
		 * Get the next log parameter in order.  This will return the
		 * next parameter, or a null of there are no more to return.
		 * @return
		 *******************************************************/
		public abstract LogParameter getNext() throws Exception;

		
		
		/********************************************************
		 * Get the previous log parameter value. This will return
		 * a null of there are no more to return;
		 * @return
		 * @throws Exception
		 *******************************************************/
		public abstract LogParameter getPrevious() throws Exception;

}
