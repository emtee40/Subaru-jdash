/*******************************************************
 * 
 *  @author spowell
 *  TableGauge
 *  Aug 28, 2007
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.GaugePanel;

/*******************************************************
 * 
 ******************************************************/
public final class TableGauge extends AbstractGauge
{
	
	public static final long serialVersionUID = 0L;
	
	private JTable theTable_ = null;
	private JScrollPane wrapperScrollPane_ = null;
	
	/******************************************************
	 * Create a new button gauge from a button shape.
	 ******************************************************/
	public TableGauge(ArrayList<Parameter> params)
	{
		super(null);
		
		this.theTable_ = new JTable(new TableGaugeModel(params));
	}

	

	
	/******************************************************
	 * Override does nothing.  Just returns.
	 *
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 *******************************************************/
	public void paint(GaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{

		/* This is where this component gets placed onto the parent panel */
		
		/* don't add it, if ti's allreayd been added */
		if (this.wrapperScrollPane_ != null)
		{
			return;
		}
		
		/* Here is where we cheat big time.  We're going to get the owner frame that the
		 * GuagePanel is placed in.  Then, remove the GaugePanel from the frame, and insert
		 * our one and only jScrollPane */
		panel.setLayout(new BorderLayout());
		
		/* Add it to the gauge panel */
		this.wrapperScrollPane_ = new JScrollPane(this.theTable_);
		this.wrapperScrollPane_.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(this.wrapperScrollPane_, BorderLayout.CENTER);
		
		
	}
	
	
}