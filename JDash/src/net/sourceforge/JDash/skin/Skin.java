/*******************************************************
 * 
 *  @author spowell
 *  Skin.java
 *  Aug 8, 2006
 *  $Id: Skin.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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

import java.awt.Dimension;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javazoom.jl.player.Player;


import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.gui.AbstractGaugePanel;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.logger.DataLogger;


/*******************************************************
 * This is the abstract skin class.  From this, we
 * can get all the skinnable attributes used
 * to define the user interface.
 ******************************************************/
public abstract class Skin implements SkinEventListener
{
	
	/** A value used by the skins to identify a windows mode */
	public static final String STATE_WINDOW 		= "window";
	
	/** The value used by the skins to identify a fullscreen mode */
	public static final String STATE_FULLSCREEN 	= "fullscreen";
	
	/** Common to ALL skins, is this deliminator character ':' */
	public static final char VALUE_DELIM			= ':';
	
	/** A less commonly used deliminator character '/' */
	public static final char VALUE_DELIM_2			= '/';

	/* A human readable string name for this Skin */
	private SkinFactory ownerFactory_ = null;
	
	
	/* The list of event listeners that respond to things like button presses */
	private ArrayList<SkinEventListener> skinEventListeners_ = new ArrayList<SkinEventListener>();
	

	/******************************************************
	 * create a new skin class.  Don't do anything special
	 * in the constructor. Because you can't be sure that
	 * the necessary values are yet ready in the sEtup class.
	 * do you init in the init() method.s
	 * @param ownerFactory IN - the factory that created this skin
	 * @param id IN - a unique ID for this skin.
	 ******************************************************/
	public Skin(SkinFactory ownerFactory)
	{
		this.ownerFactory_ = ownerFactory;
		
		/* Add this skin to the event listeners, because there are a few
		 * events that we watch for */
		addSkinEventListener(this);
	}
	
	
	/*******************************************************
	 * respond to specific skin events.  At present, the Skin
	 * class responds only to stdout events. 
	 * 
	 * @see SkinEvent
	 * 
	 * Override
	 * @see net.sourceforge.JDash.skin.SkinEventListener#actionPerformed(net.sourceforge.JDash.skin.SkinEvent)
	 *******************************************************/
	public void actionPerformed(SkinEvent se)
	{
		try
		{
			/* STDOUT events */
			if (SkinEvent.DESTINATION_STDOUT.equals(se.getDestination()))
			{
				System.out.println(se.getAction());
			}
			
			if (SkinEvent.DESTINATION_SOUND.equals(se.getDestination()))
			{
				playSound(se.getAction());
			}
		}
		catch(Exception e)
		{
			Startup.showException(e, true);
		}
	}
	
	/********************************************************
	 * Get the factory that created this skin.
	 * @return
	 *******************************************************/
	public SkinFactory getOwnerFactory()
	{
		return this.ownerFactory_;
	}
	
	/*******************************************************
	 * get the unique ID of this skin. You need to try very hard
	 * to make this unique among all possible skins.  I suggest
	 * you include the full package and class name in this if you
	 * can.
	 * @return
	 *******************************************************/
	public abstract String getId();
	
	
	/********************************************************
	 * @return
	 *******************************************************/
	public abstract String getName();
	
	/*******************************************************
	 * @param desc
	 *******************************************************/
	public abstract String getDescription();
	
	
	/*******************************************************
	 * Override
	 * @see java.lang.Object#toString()
	 *******************************************************/
	public String toString()
	{
		return getName();
	}
	
	/*******************************************************
	 * Add a skin event listener to our list of listeners.
	 * @param l
	 *******************************************************/
	public void addSkinEventListener(SkinEventListener l)
	{
		if (this.skinEventListeners_.contains(l) == false)
		{
			this.skinEventListeners_.add(l);
		}
	}
	
	
	/*******************************************************
	 * remove a skin event listener from the list.
	 * @param l
	 *******************************************************/
	public void removeSkinEventListener(SkinEventListener l)
	{
		this.skinEventListeners_.remove(l);
	}
	
	/********************************************************
	 * Inform each listener of the skin event.
	 * @param e
	 *******************************************************/
	public void fireSkinEvent(SkinEvent e)
	{
		for (SkinEventListener l : this.skinEventListeners_)
		{
			l.actionPerformed(e);
		}
	}
	
	/********************************************************
	 * Get the initial window startup state.  This will be one 
	 * of 2 words. "fullscreen" or "window". But, the "window"
	 * value can be suffixed with a scale value.  eg. "window:26"
	 * will start in window mode and scale the initial size to 26% 
	 * of the screen size.
	 * 
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract String getWindowStartupState() throws Exception;
	
	/*******************************************************
	 * return the original dimensions of the desired window. 
	 * This is the width and height that the gauge panel
	 * will prefer to be sized to. All auto-resizing will
	 * be scaled to maintain the aspect ratio that this
	 * window size represents.  When window shapes are drawn
	 * to the background window, this dimenstion defines the
	 * coords that they are drawn into.
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract Dimension getWindowSize() throws Exception;
	
	
	/********************************************************
	 * Return this skins gauge panel.  It is up to each individual skin
	 * to create an instnace of, and return the GaugePanel it want's placed 
	 * in the Dashboard.  The reason for this, is because most skins have
	 * some sort of relationship between the components the skin defines, and
	 * the Panel they will be shown on.  This gives the skin the ability
	 * to define just what is shown to the user, beyond just the gauges
	 * and buttons.   This also gives the skin the chance to initialize anything
	 * special about the panel before it gets added to the frame.
	 * This method should only be called once, therefor, it
	 * is OK to create a new instance of a gaugPanel on each call.
	 * 
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract AbstractGaugePanel createGaugePanel(DashboardFrame dashFrame, BaseMonitor monitor, DataLogger logger) throws Exception;

	
	/*******************************************************
	 * Plays the given sound name.  The concrete skin is
	 * asked for the sound object by it's soundName reference
	 * with the method getSound(). then the sound is played.
	 * The sound is played in it's own thread, so it will not
	 * block the gauges.  
	 * @param soundName
	 *******************************************************/
	public void playSound(String soundName) throws Exception
	{
		SoundRunnable sr = new SoundRunnable(getSound(soundName));
		new Thread(sr).start();
		
	}
	
	/********************************************************
	 * Returns a sound object so the skin can play it back through the sound interface.
	 * @param name
	 * @throws Exception
	 * @return an input stream of the sound.
	 ******************************************************/
	public abstract InputStream getSound(String name) throws Exception;
	
	
	private static class SoundRunnable implements Runnable
	{
		private InputStream is_ = null;
		public SoundRunnable(InputStream is)
		{
			this.is_ = is;
		}
		public void run()
		{
			try
			{
				Player player = new Player(this.is_);
				player.play();
				this.is_.close();
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
