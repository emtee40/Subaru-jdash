/*******************************************************
 * 
 *  @author spowell
 *  LoggerExporter.java
 *  Sep 7, 2006
 *  $Id: LoggerExporter.java,v 1.3 2006/12/31 16:59:10 shaneapowell Exp $
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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterEventListener;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;

/*******************************************************
 * This class gets linked to a monitor in order to 
 * export to a CSV, all the ecu values.  Note, this
 * class is designed to be linked to a LoggerPlaybackMonitor,
 * but it really doesn't matter.  This exporter will
 * output the values for any monitor.  This dialog will
 * link itself to the monitors time parameter. When ever a
 * time even is fired, all of the values from the monitors
 * list of parameters will be
 ******************************************************/
public class LoggerExporter extends JDialog implements ParameterEventListener
{

	public static final long serialVersionUID = 0L;

	private JLabel status_ = null;
	
	private LoggerPlaybackMonitor monitor_ = null;
	
	private ArrayList<Parameter> params_ = null;
	
	private int rowCount_ = 0;
	
	private FileOutputStream fos_ = null;
	
	/* A resuable file chooser */
	private static JFileChooser fileChooser_ = new JFileChooser();
	
	/*******************************************************
	 * @param owner
	 * @param monitor
	 * @throws Exception
	 *******************************************************/
	public synchronized static void export(PlaybackControlDialog owner, LoggerPlaybackMonitor monitor) throws Exception
	{
		/* Find out where to save the file */
		LoggerExporter.fileChooser_.showSaveDialog(owner);
		File file = LoggerExporter.fileChooser_.getSelectedFile();
		
		if (file == null)
		{
			return;
		}
		
		/* Create an output stream */
		FileOutputStream fos = new FileOutputStream(file);
		
		/* create a new exporter dialog. Note, this exporter is Modal, so it will block on the setVisible() */
		LoggerExporter exp = new LoggerExporter(owner, monitor, fos);
		exp.setVisible(true);
		
		/* Flush and close the output stream */
		fos.flush();
		fos.close();
		
		monitor.fireProcessingFinishedEvent();
	}

	
	/******************************************************
	 * Open a new modal exporter dialog.
	 * @param owner
	 ******************************************************/
	private LoggerExporter(PlaybackControlDialog owner, LoggerPlaybackMonitor monitor, FileOutputStream fos) throws Exception
	{
		super(owner, "Export");
		
		setModal(true);
		
		this.monitor_ = monitor;
		this.fos_ = fos;
		
		this.params_ = new ArrayList<Parameter>();
		
		
		/* Get the parameters */
		this.params_ = new ArrayList<Parameter>(this.monitor_.getTime().getOwnerRegistry().getAll().values());
		
		
		/* Respond to update events on the TIME parameter */
		this.monitor_.getTime().addEventListener(this);
		
		/* Sort for readability */
		Collections.sort(this.params_, new Comparator<Parameter>()
		{
			public int compare(Parameter p1, Parameter p2)
			{
				/* Force the time parameter to the front */
				if (p1.getName().equals(ParameterRegistry.TIME_PARAM))
				{
					return -1;
				}
				
				if (p2.getName().equals(ParameterRegistry.TIME_PARAM))
				{
					return 1;
				}
				
				/* for all others */
				return p1.getName().compareTo(p2.getName());
			}
		});		
		
		
		
		
		/* Setup the dialog */
		JPanel mainPanel = new JPanel();
		setContentPane(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		
		
		/* The status label area */
		this.status_ = new JLabel(" . . . . . . . . . . . . . . .  ");
		mainPanel.add(this.status_, BorderLayout.CENTER);
		
		/* Add the close button */
		JButton closeButton = new JButton("Close");
		mainPanel.add(closeButton, BorderLayout.SOUTH);
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
			}
		});
		
		
		/* Pack it all up */
		setSize(200,100);
		
		
		/* For each parameter in the time parameters owner registry, Add a heade value */
		StringBuffer buffer = new StringBuffer();
		for (Parameter p : this.params_)
		{
			buffer.append(p.getName() + ",");
		}
		
		buffer.append("\n");
		
		/* Write the header */
		this.fos_.write(buffer.toString().getBytes());
		
		
		/* Center on the owner */
		setLocationRelativeTo(owner);
	}
	
	
	
	/*******************************************************
	 * Override
	 * @see java.awt.Component#setVisible(boolean)
	 *******************************************************/
	@Override
	public void setVisible(boolean v)
	{
		if (v == true)
		{
			/* Start the export */
			this.monitor_.setPaused(false);

		}
		else
		{
			this.monitor_ = null;
		}
		
		super.setVisible(v);
	}
	
	/*******************************************************
	 * Respond to the TIME parameter event.
	 * Override
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *******************************************************/
	public void valueChanged(Parameter param)
	{
		
		StringBuffer buffer = new StringBuffer();
	
		/* For each parameter in the registry, output a line to the dialog window */
		for (Parameter p : this.params_)
		{
			buffer.append(p + ",");
		}
		
		buffer.append("\n");
		
		/** Add the row of data */
		try
		{
			this.fos_.write(buffer.toString().getBytes());
			this.status_.setText("Row Count: " + ++this.rowCount_);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Unexpected Error\n" + e.getMessage());
			setVisible(false);
		}
		
	}
	
	
	
}
