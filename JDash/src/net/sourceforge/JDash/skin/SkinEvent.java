/*******************************************************
 * 
 *  @author spowell
 *  SkinEvents.java
 *  Aug 27, 2007
 *  $Id:$
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
package net.sourceforge.JDash.skin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/*******************************************************
 * Skin events are messages passed to the engine from the skin usually
 * when the user performs some action, like clicking a button.
 * A SkinEvent object is mutable, like a string object.  Values
 * cannot, and must not be modified.
 ******************************************************/
public class SkinEvent
{
	
	/** this object represents the special reserved destination "all".  This means that
	 * any object that wishes to respond to this event, need only check for a destination
	 * matching this string */
	public static final String DESTINATION_ALL = "all";
	
	/** This string represents the reserved destination code for the logger.  All loggers
	 * must respond to SkinEvent messages with this destination code */
	public static final String DESTINATION_LOGGER = "logger";
	
	
	/** This string represents the reserved destination code for sounds playback.
	 * It is up to the skin to watch for sound events for sound playback */
	public static final String DESTINATION_SOUND = "sound";

	
	/** This string represents the reserved destination code for stdout console output.
	 * When an event triggers one of these, the action will be printed to the stdout console.
	 * This is really just usefull for debugging */
	public static final String DESTINATION_STDOUT = "stdout";
	
	

	private String destination_ = null;
	private String action_ = null;
	
	
	/******************************************************
	 * @param destination
	 * @param action
	 *****************************************************/
	public SkinEvent(String destination, String action)
	{
		this.destination_ = destination;
		this.action_ = action;
	}
	
	/********************************************************
	 * @return
	 ******************************************************/
	public String getDestination()
	{
		return this.destination_;
	}
	
	/*******************************************************
	 * Override the destination value.
	 * @param id
	 *******************************************************/
	public void setDestination(String id)
	{
		this.destination_ = id;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public String getAction()
	{
		return this.action_;
	}

	
	/*******************************************************
	 * Override
	 * @see java.lang.Object#toString()
	 *******************************************************/
	public String toString()
	{
		return this.getDestination() + Skin.VALUE_DELIM_2 + getAction();
	}
	
	
	/*******************************************************
	 * This utility method will extract the list of actions
	 * defined by the action string.  An action string
	 * is made up of a list of destination/action pairs,
	 * separated by a ':' character. An example of an action list
	 * is
	 *  <pre>
	 *    all/high-reset:logger/enable
	 *  </pre>  
	 * 
	 * @see XMLSkin
	 * @return
	 *******************************************************/
	public static List<SkinEvent> extractActions(String action)
	{
		List<SkinEvent> skinEvents = new ArrayList<SkinEvent>();
		
		/* If no action is defined, then just return the empty list */
		if (action == null)
		{
			return skinEvents;
		}
		
		/* Break out each action */
		StringTokenizer st = new StringTokenizer(action, "" + Skin.VALUE_DELIM);
		while(st.hasMoreElements())
		{
			String cmd = st.nextToken();
			String dest = null;
			
			/* Each action can optionally have a specific destination */
			if (cmd.indexOf(Skin.VALUE_DELIM_2) != -1)
			{
				dest = cmd.substring(0, cmd.indexOf(Skin.VALUE_DELIM_2));
				cmd = cmd.substring(dest.length() + 1, cmd.length());
			}
			
			skinEvents.add(new SkinEvent(dest, cmd));
		}
		
		return skinEvents;
	}

	
}

