/*******************************************************
 * 
 *  @author spowell
 *  DashboardFrame.java
 *  Aug 8, 2006
 *  $Id: DashboardFrame.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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
package net.sourceforge.JDash.gui;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.skin.Skin;



/*******************************************************
 *  This class will display the main dashboard frame to the
 *  user.   It will get the skin from the Setup class, and
 *  use the skin to populate the look and feel of this dashboard.
 ******************************************************/
public class DashboardFrame extends JFrame
{
	
	public static final long serialVersionUID = 0L;
	
	public enum MESSAGE_TYPE {INFO, WARNING, ERROR, DEBUG};
	
	/* The skin used */
	private Skin skin_ = null;
	
	/* the one and only gauge panel */
	private AbstractGaugePanel gaugePanel_ = null;
	
	public static final URL ICON = Setup.class.getResource("icon.png");
	
	/*******************************************************
	 *  Create a new frame instance.
	 *  
	 *  @param skin IN - the skin to render this frame with.
	 ******************************************************/
	public DashboardFrame(Skin skin, BaseMonitor monitor, DataLogger logger)
	{
		super();
		
		this.skin_ = skin;
		
		try
		{
			
			/* Set the frame title */
			setTitle(this.skin_.getName());
			setIconImage(new ImageIcon(ICON).getImage());
			
			/* Setup the screenlayout */
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//Dimension frameSize = new Dimension(screenSize);
			Dimension frameSize = skin.getWindowSize();
			String windowState = this.skin_.getWindowStartupState();
			
			/* Windowed */
			if (windowState.startsWith(Skin.STATE_WINDOW) == true)
			{
				
				/* Set the size? */
				if (windowState.indexOf(":") != -1)
				{
					double ratio = Double.parseDouble(windowState.substring(windowState.indexOf(":") + 1, windowState.length()));
					frameSize = new Dimension((int)((double)screenSize.width * (ratio / 100f)), (int)((double)screenSize.height * (ratio / 100f)));
				}
				else
				{
					/* Default to the full size of the window, unless it's bigger than the screen */
					frameSize.width = Math.min(frameSize.width, screenSize.width);
					frameSize.height = Math.min(frameSize.height, screenSize.height);
					
				}
			}
			else if (windowState.startsWith(Skin.STATE_FULLSCREEN) == true)
			{
				/* Turn off some of the window controls */
				throw new RuntimeException("Window state " + Skin.STATE_FULLSCREEN + " not Yet Supported");
				
			}
			else
			{
				throw new Exception("Invalid initial Window State [" + windowState + "]");
			}
			

			/* Set the screen size */
			setSize(frameSize);
			
			/* And the location in the center of the screen */
			setLocation(((screenSize.width - frameSize.width) / 2), ((screenSize.height - frameSize.height) / 2));
			
			/* Setup the main panel */
			this.gaugePanel_ = this.skin_.createGaugePanel(this, monitor, logger);
			setContentPane(this.gaugePanel_);
						
			
			/* Respond to the f1 key for the about box */
			InputMap i = this.gaugePanel_.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			i.put(KeyStroke.getKeyStroke("F1"), "ABOUT");
			this.gaugePanel_.getActionMap().put("ABOUT", new AbstractAction()
			{
				public static final long serialVersionUID =0L;
				public void actionPerformed(ActionEvent arg0)
				{
					doShowAbout();
				}
			}); 

			
		}
		catch(Exception e)
		{
			Startup.showException(e, true);
		}


		
	}
	
	

	
	/*******************************************************
	 * call this method to suspend gauge updates.  This is 
	 * used primarily by the logg playback capability.  In order
	 * to increase performance, this method is called to
	 * prevent screen re-draws.  Once done, this method will
	 * re-enable the updates.
	 * @param suspend
	 *******************************************************/
	public void suspendGaugeDisplayUpdates(boolean suspend)
	{
		this.gaugePanel_.suspendGaugeDisplayUpdates(suspend);
	}

	
	/*******************************************************
	 * Show the about dialog box
	 ******************************************************/
	// TODO
	private void doShowAbout()
	{
		showMessage(MESSAGE_TYPE.INFO, "About", Setup.getSetup().APPLICATION + "\n" +
												Setup.getSetup().getLicense());
	}
	
	/********************************************************
	 * Display to the user a basic message.  This is a convience
	 * method for child classes.
	 * 
	 * @param type IN - the type of message.
	 * @param title IN - the Title of the message box
	 * @param message IN - the message itself
	 *******************************************************/
	public void showMessage(MESSAGE_TYPE type, String title, String message)
	{
		
		if (title == null)
		{
			title = type.toString();
		}
		
		int messageType = JOptionPane.ERROR_MESSAGE;
		
		if (type == MESSAGE_TYPE.INFO)
		{
			messageType = JOptionPane.INFORMATION_MESSAGE;
		}
		
		if (type == MESSAGE_TYPE.WARNING)
		{
			messageType = JOptionPane.WARNING_MESSAGE;
		}

		if (type == MESSAGE_TYPE.ERROR)
		{
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		if (type == MESSAGE_TYPE.DEBUG)
		{
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}
	


}
