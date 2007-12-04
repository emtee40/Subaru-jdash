/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge.java
 *  Aug 8, 2006
 *  $Id: AbstractGauge.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.shapes.AbstractShape;

/*******************************************************
 * An abstract gauge component.  Most of what is displayed
 * on the screen is some sort of Gauge component.  A gauge
 * is nothing more than a component that can display a changing
 * value to the user.  It can be a digital value, and analog
 * sweep gauge, and LED panel.... etc.  Gauges ALL extend
 * this class.  It is the job of the extended version
 * of this class to do the heavy lifting and making it
 * look like it should.
 ******************************************************/
public abstract class AbstractGauge
{
	
	
	/** The ECU parameter that this gauge is setup to display values for */
	private Parameter parameter_ = null;

	private Point position_ = null;
	
	private List<AbstractShape> staticShapes_ = null;
	
	/******************************************************
	 * Create a new default analog gauge.
	 * @param parameter IN - the parameter this gauge represents.
	 ******************************************************/
	public AbstractGauge(Parameter parameter, Point position)
	{
		this.parameter_ = parameter;
		this.position_ = position;
	}

	
	
	/*******************************************************
	 * get the parameter object linked to this gauge.
	 * @return the ecu parameter.
	 *******************************************************/
	public Parameter getParameter()
	{
		return this.parameter_;
	}
	

	/********************************************************
	 * @return
	 *******************************************************/
	public Point getPosition()
	{
		return this.position_;
	}

	
	/*******************************************************
	 * A gauge can optionally have a list of static shapes that
	 * are intended to be painted into the background.
	 * @return
	 *******************************************************/
	public List<AbstractShape> getStaticShapes()
	{
		return this.staticShapes_;
	}

	
	/*******************************************************
	 * Set the list of static background shapes.
	 * @param shapes
	 *******************************************************/
	public void addStaticShape(AbstractShape shape)
	{
		System.out.println("Adding Static Shape: " + shape.getClass().getName());
		
		if (this.staticShapes_ == null)
		{
			this.staticShapes_ = new ArrayList<AbstractShape>();
		}
		
		
		/* Adjust the shapes x and y */
		
		this.staticShapes_.add(shape);
	}
}
