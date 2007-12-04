/*******************************************************
 * 
 *  @author spowell
 *  LoggerControlDialog.java
 *  Aug 26, 2006
 *  $Id: PlaybackControlDialog.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.gui.DashboardFrame;


/*******************************************************
 * This dialog class is a modless dialog box that will pop
 * giving the user control over the playback logger.
 ******************************************************/
public class PlaybackControlDialog extends JDialog
{
	
	public static final long serialVersionUID =0L;
	
	private JList logList_ = null;
	
	private LoggerPlaybackMonitor playbackMonitor_ = null;
	private DataLogger logger_ = null;
	
	private String[] logNames_ = null;

	/*******************************************************
	 * Create a new playback control instance 
	 ******************************************************/
	public PlaybackControlDialog(DashboardFrame ownerFrame, LoggerPlaybackMonitor playbackMonitor, DataLogger logger) throws Exception
	{
		super(ownerFrame);
		
		this.playbackMonitor_ = playbackMonitor;
		this.logger_ = logger;
		
		setModal(false);
		
		init();
		populateList();

		pack();
		
		setLocationRelativeTo(ownerFrame);
	}
	
	
	/*******************************************************
	 * Setup the display 
	 *******************************************************/
	private void init()
	{
		this.setLayout(new BorderLayout());
		
		JLabel label = null;
		
		JPanel mainPanel = new JPanel();
		this.add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		mainPanel.setLayout(new BorderLayout());
		
		/* Setup the log list */
		//this.logList_ = new JComboBox(this.logNames_);
		this.logList_ = new JList();
		JScrollPane sp = new JScrollPane(this.logList_);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mainPanel.add(sp);
		label = new JLabel("Logs");
		label.setLabelFor(this.logList_);
		
		
		/* Add the button panel */
		JPanel buttonPanel = new JPanel();
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		
		buttonPanel.add(Box.createHorizontalGlue());
				
		/* Play Button */
		buttonPanel.add(Box.createHorizontalStrut(10));
		JButton button = new JButton("Play");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doPlay();
			}
		});
		
		/* Pause Button */
		buttonPanel.add(Box.createHorizontalStrut(10));
		button = new JButton("Pause");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doPause();
			}
		});
		
		/* Stop Button */
		buttonPanel.add(Box.createHorizontalStrut(10));
		button = new JButton("Stop");
		buttonPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doStop();
			}
		});
		
		/* Rew Button */
		buttonPanel.add(Box.createHorizontalStrut(10));
		button = new JButton("Rewind");
		buttonPanel.add(button);
		button.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent arg0)
			{
				doRewind(true);
			}
			
			public void mouseReleased(MouseEvent arg0)
			{
				doRewind(false);
			}
		});
		
		
		/* Fwd Button */
		buttonPanel.add(Box.createHorizontalStrut(10));
		button = new JButton("Fworward");
		buttonPanel.add(button);
		button.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent arg0)
			{
				doForward(true);
			}
			
			public void mouseReleased(MouseEvent arg0)
			{
				doForward(false);
			}
		});
		
		


		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(Box.createHorizontalGlue());
		
		
		/* Setup the menu */
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu logMenu = new JMenu("Log");
		menuBar.add(logMenu);
		
		/* The Delete Menu */
		JMenuItem menuItem = new JMenuItem("Delete");
		logMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doDelete();
			}
		});

		
		
		/* The Export Menu */
		menuItem = new JMenuItem("Export");
		logMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doExport();
			}
		});
		
		
		
		/* The Rename Menu */
		menuItem = new JMenuItem("Rename");
		logMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doRename();
			}
		});
		
		
		/* The Exit Menu */
		menuItem = new JMenuItem("Exit");
		logMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});
		
	}

	
	/*******************************************************
	 *  Populate the log list
	 *******************************************************/
	private void populateList() throws Exception
	{
		/* Get the logger for convience */
		
		/* Create a selectable list for playback */
		this.logNames_ = new String[this.logger_.getLogCount()];
		for (int index = 0; index < this.logNames_.length; index++)
		{
			this.logNames_[index] = this.logger_.getLogName(index);
		}
		
		/* Throw an error if the logger has nothing to display */
		if ((this.logNames_ == null) || (this.logNames_.length == 0))
		{
			JOptionPane.showMessageDialog(this, "The Logger did not have any logs for us to display. Please log some data first", "Warning", JOptionPane.INFORMATION_MESSAGE);
		}

		/* Re-Pop the list */
		this.logList_.setListData(this.logNames_);
		this.logList_.setSelectedIndex(0);

	}
	
	/*******************************************************
	 * Override
	 * @see java.awt.Component#setVisible(boolean)
	 *******************************************************/
	@Override
	public void setVisible(boolean v)
	{
		super.setVisible(true);
	}
	
	
	/********************************************************
	 *  Start playback
	 *******************************************************/
	private void doPlay()
	{

		try
		{
			/* Setup the playback */
			this.logger_.prepareForPlayback(logList_.getSelectedIndex());
			this.playbackMonitor_.setFastPlayback(false);
			((DashboardFrame)getOwner()).suspendGaugeDisplayUpdates(false);
			
			/* Start playback */
			this.playbackMonitor_.setPaused(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			((DashboardFrame)getOwner()).showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, "Playback Error", e.getMessage());
		}
	}
	
	/*******************************************************
	 *  Pause playback
	 *******************************************************/
	private void doPause()
	{
		playbackMonitor_.setPaused(!playbackMonitor_.isPaused());
	}
	
	/*******************************************************
	 * Stop playback
	 ******************************************************/
	private void doStop()
	{
		/* Reset the playback, but pause */
		doPlay();
		this.playbackMonitor_.reset();
		playbackMonitor_.setPaused(true);
		
	}
	
	
	/*******************************************************
	 *  rewind the playback
	 *******************************************************/
	private void doRewind(boolean rew)
	{
		this.playbackMonitor_.setDirection(!rew);
	}
	
	
	/*******************************************************
	 *  Fast Forward the playback
	 *******************************************************/
	private void doForward(boolean fwd)
	{
		this.playbackMonitor_.setFastPlayback(fwd);
	}
	
	/*******************************************************
	 *  delete the current entry
	 *******************************************************/
	private void doDelete()
	{

		try
		{
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected log entry?", "Delete?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				this.logger_.deleteLog(this.logList_.getSelectedIndex());

				populateList();
				
			}
		}
		catch(Exception e)
		{
			Startup.showException(e, false);
		}
	}
	
	
	/*******************************************************
	 * Export the selected log to a file.
	 *******************************************************/
	private void doExport()
	{
		
		((DashboardFrame)getOwner()).showMessage(DashboardFrame.MESSAGE_TYPE.INFO, "Export", 
					"In order to export to a file, we must fast forward through the log one event at at time.\n" +
					"While fast forwarding, a window will display the export status.\n" +
					"If you notice that the row count is no loger going up, then the export is finished, and you can close the export window\n" +
					"Once done, you can open the csv file in Excel.\n" +
					"This could take a while if it's a large log.");
		
		try
		{
			/* Setup the export */
			this.logger_.prepareForPlayback(logList_.getSelectedIndex());
			this.playbackMonitor_.setFastPlayback(true);
			((DashboardFrame)getOwner()).suspendGaugeDisplayUpdates(true);
			
			/* Export */
			LoggerExporter.export(this, this.playbackMonitor_);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			((DashboardFrame)getOwner()).showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, "Export Error", e.getMessage());
		}
	}
	
	/*******************************************************
	 * Rename the log entry.
	 *******************************************************/
	private void doRename()
	{
		try
		{
			int logIndex = this.logList_.getSelectedIndex();
			String logName = this.logger_.getLogName(logIndex);
			
			String newName = JOptionPane.showInputDialog(this, "Rename Log", logName);
			
			if (newName == null)
			{
				return;
			}
			
			/* Rename the log entry */
			this.logger_.setLogName(logIndex, newName);
			
			/* Refresh our list */
			populateList();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			((DashboardFrame)getOwner()).showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, "Rename Error", e.getMessage());
		}
	}
}
