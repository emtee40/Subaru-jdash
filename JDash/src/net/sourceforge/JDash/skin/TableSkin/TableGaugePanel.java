/*******************************************************
 * 
 *  @author spowell
 *  TableGaugePanel
 *  Aug 9, 2006
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
package net.sourceforge.JDash.skin.TableSkin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;

import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterComparator;
import net.sourceforge.JDash.ecu.param.special.RateParameter;
import net.sourceforge.JDash.ecu.param.special.TimeParameter;
import net.sourceforge.JDash.gui.AbstractGaugePanel;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.logger.DataLogger;

/******************************************************
 *
 ******************************************************/
public class TableGaugePanel extends AbstractGaugePanel
{
	
	private static final String INSTRUCTIONS = 
		"The left pane represents ECU Values pulled over the communications line. " +
		"The right pane represents values that are calculated from actual ECU Values. " +
		"The check box onder under the \"Enable\" column allows you to identify what ECU " +
		"values are to be fetched.";
	
	public static final long serialVersionUID = 0L;
	
	private JTable ecuTableGauge_ = null;
	private EnableableGaugeTableModel ecuTableModel_ = null;
	
	private JTable staticTableGauge_ = null;
	private GaugeTableModel staticTableModel_ = null;
	
	/*******************************************************
	 * @param ownerFrame
	 * @param skin
	 * @param monitor
	 * @param logger
	 * @throws Exception
	 ******************************************************/
	public TableGaugePanel(DashboardFrame ownerFrame, TableSkin skin, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		super(ownerFrame, skin, monitor, logger);
		
		
		/* Setup the 2 lists of params */
		List<Parameter> ecuParams = new ArrayList<Parameter>();
		List<Parameter> staticParams = new ArrayList<Parameter>();
		
		/* We'll need all the params */
		List<Parameter> allParameters = new ArrayList<Parameter>(skin.getOwnerFactory().getParameterRegistry().getAll().values());
		
		
		/* Add each gauges parameter to both the monitor and the logger */
		for (Parameter p : allParameters)
		{
			monitor.addParam(p);
			logger.addParameter(p);
		}
		
		
		/* split them into the two lists */
		for (Parameter p : allParameters)
		{
			if (p instanceof ECUParameter)
			{
				ecuParams.add(p);
			}
			else
			{
				staticParams.add(p);
			}
		}
		
		
		/* Sort the lists by alpha name */
		Collections.sort(ecuParams, new ParameterComparator());
		Collections.sort(staticParams, new ParameterComparator());
		
		
		/* force the rate, and time params to the top of the static list */
		for (Parameter p : staticParams)
		{
			if (p instanceof RateParameter)
			{
				staticParams.remove(p);
				staticParams.add(0, p);
				break;
			}
		}
		
		for (Parameter p : staticParams)
		{
			if (p instanceof TimeParameter)
			{
				staticParams.remove(p);
				staticParams.add(1, p);
				break;
			}
		}
		
		

		{

			/* Use the border layout for this panel */
			this.setLayout(new BorderLayout());
			
			/* The center panel of parameter lists */
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			
			/* Add the ECU Parameter table */
			{
				this.ecuTableModel_ = new EnableableGaugeTableModel(ecuParams);
				this.ecuTableGauge_ = new JTable(this.ecuTableModel_);
				JScrollPane ecuScrollPane = new JScrollPane(this.ecuTableGauge_);
				splitPane.setLeftComponent(ecuScrollPane);
			}
			
			/* Add the calculated parameter table */
			{
				this.staticTableModel_ = new GaugeTableModel(staticParams);
				this.staticTableGauge_ = new JTable(this.staticTableModel_);
				JScrollPane staticScrollPane = new JScrollPane(this.staticTableGauge_);
				splitPane.setRightComponent(staticScrollPane);
			}
			
			/* Add the split pane of tables */
			this.add(splitPane, BorderLayout.CENTER);
			splitPane.setDividerLocation(((int)skin.getWindowSize().getWidth()) / 2);

			
			/* Put the command buttons in the top location */
			{
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
				
				JToggleButton loggerButton = new JToggleButton("Logging");
				buttonPanel.add(loggerButton);
				
				this.add(buttonPanel, BorderLayout.NORTH);
			}

			
			
			/* Put the instructions text in the south location */
			{
				JTextArea instructionsPane = new JTextArea();
				instructionsPane.setLineWrap(true);
				instructionsPane.setWrapStyleWord(true);
				instructionsPane.setRows(3);
				instructionsPane.setEditable(false);
				instructionsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(3,3,3,3)));
				instructionsPane.setText(INSTRUCTIONS);
				JScrollPane instructionsScrollPane = new JScrollPane(instructionsPane);
				instructionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				this.add(instructionsScrollPane, BorderLayout.SOUTH);
			}		
		}
		
	}

	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.AbstractGaugePanel#updateDisplay()
	 *******************************************************/
	public void updateDisplay()
	{
		this.ecuTableGauge_.tableChanged(new TableModelEvent(this.ecuTableModel_));
		this.staticTableGauge_.tableChanged(new TableModelEvent(this.staticTableModel_));
	}

}
