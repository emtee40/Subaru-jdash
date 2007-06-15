/*******************************************************
 * 
 *  @author spowell
 *  Splash.java
 *  Dec 14, 2006
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
package net.sourceforge.JDash.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;


/*******************************************************
 * This is the main splash screen frame for the JDash program.
 ******************************************************/
public class Splash extends JFrame
{
	
	public static final long serialVersionUID = 0l;
	
	public static final int MIN = 0;
	public static final int MAX = 100;
	
	private JProgressBar progressBar_ = null;
	
	public static final String SPLASH_IMAGE_NAME = "splash.jpg";

	/******************************************************
	 * Create a new splash screen. 
	 ******************************************************/
	public Splash(String title)
	{
		
		super(title);
		
		/* Configure our splash frame */
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(DashboardFrame.ICON).getImage());
		setAlwaysOnTop(true);
		setResizable(false);
		
		
		/* Load the image */
		ImageIcon splashImage = new ImageIcon(this.getClass().getResource(SPLASH_IMAGE_NAME));
		while (splashImage.getImageLoadStatus() == MediaTracker.LOADING)
		{
			try
			{
				Thread.sleep(10);
			}
			catch(Exception e)
			{
				break;
			}
		}
		
		int width = splashImage.getIconWidth();
		int height = splashImage.getIconHeight();
		
		/* Setup the main panel */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		this.setContentPane(mainPanel);
		
		
		/* Setup the  main image */
		JLabel mainImage = new JLabel(new ImageIcon(this.getClass().getResource(SPLASH_IMAGE_NAME)));
		mainPanel.add(mainImage);
		
		/* Setup the progress bar */
		UIManager.put("ProgressBar.selectionBackground", Color.BLUE);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		this.progressBar_ = new JProgressBar(MIN, MAX);
		this.progressBar_.setStringPainted(true);
		this.progressBar_.setBackground(Color.WHITE);
		this.progressBar_.setForeground(Color.BLUE);
		mainPanel.add(progressBar_, BorderLayout.SOUTH);
		
		setStatus(MIN, "");
	
		/* And the location in the center of the screen */
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(((screenSize.width - width) / 2), ((screenSize.height - height) / 2));
		
		/* Size it */
		pack();
		
		 
	}
	
	
	/*******************************************************
	 * Sets the status to 0-100% of the total.  The value of
	 * progress can ONLY be between 0 and 100.
	 * 
	 * @param progress a progress value between 0 and 100
	 * @param message the display string
	 *******************************************************/
	public void setStatus(int progress, String message)
	{
		progress = (int)((double)MAX * ((double)progress / 100.0));
		this.progressBar_.setValue(progress);
		this.progressBar_.setString(message);
	}
	
	
}
