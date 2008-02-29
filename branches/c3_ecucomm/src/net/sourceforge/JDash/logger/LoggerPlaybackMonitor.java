/*******************************************************
 * 
 *  @author spowell
 *  MainPanel.java
 *  Aug 9, 2006
 *  $Id: LoggerPlaybackMonitor.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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



import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterException;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.gui.DashboardFrame;


/******************************************************
 * This monitor is a completely different animal.  It's
 * designed to read it's values from an HSQLDB Database,
 * re-submit them to the display subsystem that is listening.
 * It's basically a facility to record, and playback
 * ecu events.  One more major difference between this
 * monitor, and others is that this monitor is capable of
 * both reading and writing to the database.  This is 
 * basically a complete DAO class.  Infact, this monitor
 * is used for the data logging capabilities of this 
 * application.
 *****************************************************/
public class LoggerPlaybackMonitor extends BaseMonitor
{
	
	private DashboardFrame parentFrame_ = null;
	private DataLogger logger_ = null;
	private PlaybackControlDialog controlDialog_ = null;
		
	private long previousParameterTime_ = -1;
	private long previousSendTime_ = -1;
	
	private boolean paused_ = true;
	
	private boolean playbackForward_ = true;
	
	/* This flag will cause playback to run as fast as possible */
	private boolean fastPlayback_ = false;
		
	/*******************************************************
	 * Create a new database monitor
	 * @throws ParameterException
	 ******************************************************/
	public LoggerPlaybackMonitor() throws Exception
	{
		super();
	}


	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 *******************************************************/
	public String getEcuInfo() throws Exception
	{
		return "Logger Playback";
	}
	
	
	/*******************************************************
	 * 
	 *******************************************************/
	public void reset()
	{
		this.previousParameterTime_ = -1l;
		this.previousSendTime_ = -1l;
	}
	
	/*******************************************************
	 * @param puuse
	 ******************************************************/
	public void setPaused(boolean pause)
	{
		this.paused_ = pause;

	}

	
	/******************************************************
	 * @param fast
	 ******************************************************/
	public void setFastPlayback(boolean fast)
	{
		this.fastPlayback_ = fast;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public boolean isFastPlayback()
	{
		return this.fastPlayback_;
	}

	/*******************************************************
	 * @return
	 *******************************************************/
	public boolean isPaused()
	{
		return this.paused_;
	}
	
	
	/*******************************************************
	 * Set the playback direction. True for forward, false for 
	 * backward.
	 * @param forward
	 *******************************************************/
	public void setDirection(boolean forward)
	{
		this.playbackForward_ = forward;
	}
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public boolean getDirection()
	{
		return this.playbackForward_;
	}
	
	/*******************************************************
	 * The parent frame
	 * @param parentFrame
	 *******************************************************/
	public void init(DashboardFrame parentFrame, DataLogger logger)
	{
		
		this.parentFrame_ = parentFrame;
		this.logger_ = logger;
		
		this.parentFrame_.addComponentListener(new ComponentAdapter()
		{
			public void componentShown(ComponentEvent arg0)
			{
				init();
			}
		});
		
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init()
	 *******************************************************/
	private String init()
	{

		try
		{
			/* Setup the playback dialog and display it */
			this.controlDialog_ = new PlaybackControlDialog(this.parentFrame_, this, this.logger_);
			this.controlDialog_.setVisible(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			this.parentFrame_.showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, "Playback Control Error", e.getMessage());
		}
		
		return "";
		
	}
	
	
	/******************************************************
	 * Kick off communications with the ECU.
	 * Override
	 * @see java.lang.Runnable#run()
	 ******************************************************/
	public void run()
	{
		
		try
		{

			
			while (doRun_)
			{
				/* If paused, then sleep for a bit, and continue the loop, The
				 * reason for the sleep is to play fair with other threads */
				if (this.paused_)
				{
					Thread.sleep(100);
					continue;
				}
				
				
				/* Get the next playback value */
				LogParameter p = null;
				if (getDirection())
				{
					p = this.logger_.getNext();
				}
				else
				{
					p = this.logger_.getPrevious();
				}


				/* No more events, then pause playback */
				if (p == null)
				{
					setPaused(true);
					continue;
				}

				
				/* Initialize the previous values if this is the first in this sequence */
				if (this.previousParameterTime_ == -1)
				{
					this.previousParameterTime_ = p.getEventTime();
					this.previousSendTime_ = System.currentTimeMillis();
				}
				
				
				/* As long as the time gap between the previous parameter and this paramter is less than
				 * the time gap between the last paramter update send, and now, then we wait */
				if (this.fastPlayback_ == false)
				{
					while ((p.getEventTime() - this.previousParameterTime_) > (System.currentTimeMillis() - this.previousSendTime_))
					{
						Thread.sleep(10);
					}
				}

				
				this.previousParameterTime_ = p.getEventTime();
				this.previousSendTime_ = System.currentTimeMillis();

				
				/* If the parameter read was the TIME parameter, then we dont' send it's value, instead 
				 * we treat it as a batch indicator */
				if (p.getName().equals(ParameterRegistry.TIME_PARAM))
				{
					fireProcessingFinishedEvent();
					fireProcessingStartedEvent();
				}
				else
				{
					/* Otherwise, send the result */
					sendResult(p.getName(), p.getResult());
				}
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}


	  /*******************************************************
     * @param rxPacket
     *******************************************************/
    private void sendResult(String name, double value)
    {
    	
    	/* If this is the time parameter, then rather than set it's value, trip of a mark time event */
		if (name.equals(ParameterRegistry.TIME_PARAM) == true)
		{
			getTime().marktime((long)value);
    		return;
		}

    	
    	try
    	{
    		Parameter param = null;
    		
    		/* It's possible that the parameter in the database, doesn't exist in the current config. So, we'll just skip it */
    		try
    		{
    			for (Parameter p : getParams())
    			{
    				if (name.equals(p.getName()) == true)
    				{
    					param = p;
    					break;
    				}
    			}
    			
    		}
    		catch(Exception e)
    		{
    			return;
    		}
    		
    		
    		/* if the parameter was not found in our list, then no need to continue */
    		if (param == null)
    		{
    			return;
    		}
	    	
	    	/* Normal ecu parameters, set their values */
	    	if (param instanceof ECUParameter)
	    	{
	    		if (((ECUParameter)param).isEnabled())
	    		{
	    			((ECUParameter)param).setResult(value);
	    			fireProcessingParameterEvent(param);
	    		}
	    	}
	    	else
	    	{
	    		throw new Exception("The database monitor returned a value for a parameter that is NOT a raw ECU parameter [" + param.getName()  + "]");
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new RuntimeException("The database monitor failed to send the result for a value update", e);
    	}
    	
    }

  
}
