/*******************************************************
 * 
 *  @author spowell
 *  DashboardFrame.java
 *  Aug 8, 2006
 *  $Id: ConfigureFrame.java,v 1.4 2006/12/31 16:59:10 shaneapowell Exp $
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.ecu.param.XMLParameterLoader;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinFactory;



/*******************************************************
 *  This class will display the main dashboard frame to the
 *  user. 
 ******************************************************/
public class ConfigureFrame extends JFrame
{
	
	/** This is the file extension for ecu monitor parameter files */
	public static final String MONITOR_FILE_SUFFIX = ".pml";
	
	public static final long serialVersionUID = 0L;
	
	private static final int DESC_TEXT_ROWS = 6;
	
	private JComboBox skinClass_ = null;
	private JTextField comPort_ = null;
	private JComboBox monitorParamFile_ = null;
	
	private JCheckBox testMonitor_ = null;
	private JCheckBox loggerMonitor_ = null;
	
	private JTextArea skinClassDesc_ = null;
	private JTextArea comPortDesc_ = null;
	private JTextArea monitorParamFileDesc_ = null;
	
	public static final URL ICON = Setup.class.getResource("config.png");

	/*******************************************************
	 *  Create a new frame instance.
	 *  
	 *  @param testMode IN - run this frame in test mode or not.  Test mode
	 *  will setup a test monitor which will simply provide random values
	 *  for each desired monitor.
	 ******************************************************/
	public ConfigureFrame()
	{
		super();
		
		
		try
		{
			
			/* Set the frame title */
			setTitle("Configure " + Setup.APPLICATION);
			setIconImage(new ImageIcon(ICON).getImage());
			
			/* Setup the display */
			initComponents();
			initLayout();

			/* Setup the screenlayout */
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = getSize();
	
			/* And the location in the center of the screen */
			setLocation(((screenSize.width - frameSize.width) / 2), ((screenSize.height - frameSize.height) / 2));
			

			
		}
		catch(Exception e)
		{
			showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, null, e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		
	}
	

	/*******************************************************
	 * @throws Exception
	 *******************************************************/
	private void initComponents() throws Exception
	{
		this.skinClass_ = new JComboBox();
		this.comPort_ = new JTextField();
		this.monitorParamFile_ = new JComboBox();
		
		this.testMonitor_ = new JCheckBox("Simulation Test Mode", new Boolean(Setup.getSetup().get(Setup.SETUP_CONFIG_ENABLE_TEST)));
		this.loggerMonitor_ = new JCheckBox("Logger Playback Mode", new Boolean(Setup.getSetup().get(Setup.SETUP_CONFIG_ENABLE_LOGGER_PLAYBACK)));
		
		this.testMonitor_.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				loggerMonitor_.setSelected(false);
			}
		});
		
		this.loggerMonitor_.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				testMonitor_.setSelected(false);
			}
		});
		
		this.skinClassDesc_ = new JTextArea();
		this.comPortDesc_ = new JTextArea("The serial com port to connect to. In windows" +
											" this is something like \"com1\" or \"com5\"." +
											"  In Unix this will be something like" +
											" \"/dev/ttyS0\" or \"/dev/tts/USB0\".");
		this.monitorParamFileDesc_ = new JTextArea("This is the xml Parameter file that the monitor will read it's addressing values from. " +
				"Different monitors use differently formatted parameter files, so be sure to select a parameter file that is compatible" +
				" with the monitor you've selected.");
		
		/* Listen for Parameter File changes */
		this.monitorParamFile_.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doMonitorParameterFileSelected();
			}
		});
		
		
		/* Listen for skin changes */
		this.skinClass_.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				doSkinSelected();
			}
		});
		
		
		
		/* Add the known SKINS */
		for (SkinFactory skinFactory : SkinFactory.getAllFactories())
		{
			for (Skin skin : skinFactory.getAllSkins())
			{
				this.skinClass_.addItem(skin);
				if (skin.getId().equals(Setup.getSetup().get(Setup.SETUP_CONFIG_SKIN_ID)) == true)
				{
					this.skinClass_.setSelectedItem(skin);
				}
			}
		}
		
		

		
		/* Add the monitor parameter files */
		File baseDir = new File(Setup.SETUP_CONFIG_ECU_PARAMS_DIR);
		for (File f : baseDir.listFiles())
		{
			if (f.getName().endsWith(MONITOR_FILE_SUFFIX) == true)
			{
				XMLParameterLoader loader = new XMLParameterLoader(f);
				this.monitorParamFile_.addItem(loader);
				
				if (loader.getFile().getName().equals(Setup.getSetup().get(Setup.SETUP_CONFIG_PARAMETER_FILE)))
				{
					this.monitorParamFile_.setSelectedItem(loader);
				}
			}
		}
		
		
		/* Set the comm port */
		this.comPort_.setText(Setup.getSetup().get(Setup.SETUP_CONFIG_MONITOR_PORT));

	}
	
	/********************************************************
	 * Initialize the graphics.
	 *******************************************************/
	private void initLayout() throws Exception
	{
		
		/* Init the content pane */
		JPanel contentPanel = new JPanel();
		setContentPane(contentPanel);
		contentPanel.setLayout(new BorderLayout());
		
		
		/* Setup the main content panel */
		JPanel mainPanel = new JPanel(new GridBagLayout());
		JScrollPane mainScrollPane = new JScrollPane(mainPanel);
		getContentPane().add(mainScrollPane);
		mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		

		/* Skin Class */
		addLabeledComp("Skin:", this.skinClass_, this.skinClassDesc_, mainPanel);
		
		/* Monitor File */
		addLabeledComp("Monitor:", this.monitorParamFile_, this.monitorParamFileDesc_, mainPanel);
		
		/* Test and logger Monitor */
		JPanel tlPanel = new JPanel();
		tlPanel.setLayout(new BoxLayout(tlPanel, BoxLayout.Y_AXIS));
		tlPanel.add(this.testMonitor_);
		tlPanel.add(this.loggerMonitor_);
		addLabeledComp("", tlPanel, new JTextArea("Enable monitor simulator or Logger Playback mode. No serial connection needed.  You can't select both together.\n\n" +
				" ** NOTE: If your using \"Logger Playback\" Mode, it is critical that you also select the monitor that was used to generate the selected log.  This is" +
				" becuase the specific monitor defines how the ECU Parameters are read and recorded and is used during log playback to format the values." +
				" A different monitor will not understand the format of log data from a another monitor. "), mainPanel);
		
		
		/* Com Port */
		addLabeledComp("Comm Port:", this.comPort_, this.comPortDesc_, mainPanel);
		
		
		/* Add buttons */
		{
			JPanel buttonPanel = new JPanel();
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			buttonPanel.add(Box.createGlue());
			
			JButton saveButton = new JButton("Save");
			buttonPanel.add(saveButton);
			saveButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					doExit(true);
				}
			});
			
			buttonPanel.add(Box.createHorizontalStrut(10));
			
			JButton cancelButton = new JButton("Cancel");
			buttonPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					doExit(false);
				}
			});
			
			buttonPanel.add(Box.createGlue());
		}

		/* Pack it up */
		pack();
		
	}
	
	
	/*******************************************************
	 * @param label
	 * @param comp
	 * @return
	 *******************************************************/
	private void addLabeledComp(String labelText, Component comp, JTextArea descText, Container cont)
	{
		descText.setEditable(false);
		descText.setLineWrap(true);
		descText.setWrapStyleWord(true);		
		descText.setColumns(DESC_TEXT_ROWS * 10);
		descText.setRows(DESC_TEXT_ROWS);

		
		JScrollPane descPane = new JScrollPane(descText);
		descPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		JLabel label = new JLabel(labelText, JLabel.TRAILING);
		
		GridBagLayout layout = (GridBagLayout)cont.getLayout();
		
		GridBagConstraints labelCon = new GridBagConstraints();
		GridBagConstraints compCon = new GridBagConstraints();
		GridBagConstraints descCon = new GridBagConstraints();
		
		labelCon.anchor = GridBagConstraints.FIRST_LINE_END;
		compCon.anchor = GridBagConstraints.PAGE_START;
		descCon.anchor = GridBagConstraints.CENTER;
		
		labelCon.fill = GridBagConstraints.NONE;
		compCon.fill = GridBagConstraints.HORIZONTAL;
		descCon.fill = GridBagConstraints.BOTH;
		
		labelCon.ipadx = labelCon.ipady = 5;
		compCon.ipadx = compCon.ipady = 5;
		descCon.ipadx = descCon.ipady = 5;
		
		Insets i = new Insets(15,4,15,4);
		labelCon.insets = i;
		compCon.insets = i;
		descCon.insets = i;
		
		labelCon.gridx = 0;
		compCon.gridx = 1;
		descCon.gridx = 2;
		
		labelCon.weightx = 1.0;
		compCon.weightx = 1.0;
		descCon.weightx = 2.0;
		
		labelCon.gridy = GridBagConstraints.RELATIVE;;
		compCon.gridy = GridBagConstraints.RELATIVE;
		descCon.gridy = GridBagConstraints.RELATIVE;
		
		layout.setConstraints(label, labelCon);
		layout.setConstraints(comp, compCon);
		layout.setConstraints(descPane, descCon);
		
		
		label.setLabelFor(comp);
		cont.add(label);
		cont.add(comp);
		cont.add(descPane);
		
	}
	
	
	/********************************************************
	 * Display to the user a basic message.
	 * @param type IN - the type of message.
	 * @param title IN - the Title of the message box
	 * @param message IN - the message itself
	 *******************************************************/
	private void showMessage(DashboardFrame.MESSAGE_TYPE type, String title, String message)
	{
		
		if (title == null)
		{
			title = type.toString();
		}
		
		int messageType = JOptionPane.ERROR_MESSAGE;
		
		if (type == DashboardFrame.MESSAGE_TYPE.INFO)
		{
			messageType = JOptionPane.INFORMATION_MESSAGE;
		}
		
		if (type == DashboardFrame.MESSAGE_TYPE.WARNING)
		{
			messageType = JOptionPane.WARNING_MESSAGE;
		}

		if (type == DashboardFrame.MESSAGE_TYPE.ERROR)
		{
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		if (type == DashboardFrame.MESSAGE_TYPE.DEBUG)
		{
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}
	
	
	/*******************************************************
	 *  When a parameter is selected, update the descritpion
	 *******************************************************/
	private void doMonitorParameterFileSelected()
	{
		try
		{
			XMLParameterLoader selectedLoader = (XMLParameterLoader)this.monitorParamFile_.getSelectedItem();
			this.monitorParamFileDesc_.setText(selectedLoader.getDescription());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			this.monitorParamFileDesc_.setText("Description not available");
		}
		
	}

	/*******************************************************
	 * when a skin is selected, change the list of skin IDs.
	 *******************************************************/
	private void doSkinSelected()
	{
		Skin selectedSkin = (Skin)this.skinClass_.getSelectedItem();
		this.skinClassDesc_.setText(selectedSkin.getDescription());
	}

	
	
	
	/*******************************************************
	 * Save and exit.
	 * 
	 * @param save IN - save or discard changes.
	 *******************************************************/
	private void doExit(boolean save)
	{
		
		
		
		try
		{

			if (save)
			{
				Skin skin = (Skin)this.skinClass_.getSelectedItem();
				XMLParameterLoader loader = (XMLParameterLoader)this.monitorParamFile_.getSelectedItem();
				
				
				Setup.getSetup().set(Setup.SETUP_CONFIG_SKINFACTORY_CLASS,  	skin.getOwnerFactory().getClass().getName());
				Setup.getSetup().set(Setup.SETUP_CONFIG_SKIN_ID,        		skin.getId());
				Setup.getSetup().set(Setup.SETUP_CONFIG_MONITOR_PORT,   		this.comPort_.getText());
				Setup.getSetup().set(Setup.SETUP_CONFIG_PARAMETER_FILE, 		loader.getFile().getName());
				Setup.getSetup().set(Setup.SETUP_CONFIG_ENABLE_TEST,			new Boolean(this.testMonitor_.isSelected()).toString());
				Setup.getSetup().set(Setup.SETUP_CONFIG_ENABLE_LOGGER_PLAYBACK,	new Boolean(this.loggerMonitor_.isSelected()).toString());
			
				Setup.getSetup().saveConfigFile();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			showMessage(DashboardFrame.MESSAGE_TYPE.ERROR, "Error", e.getMessage());
			return;
		}
		finally
		{
			setVisible(false);
			this.dispose();
		}
		
		
	}
	

}
