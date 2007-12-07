/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: RoundRectangleShape.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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
package net.sourceforge.JDash.gui.shapes;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/*******************************************************
 * Creates a Round Rectangle shape for use within a gauge.
 ******************************************************/
public class RoundRectangleShape extends AbstractShape
{
	
	private double x_ = 0;
	private double y_ = 0;
	private double w_ = 0;
	private double h_ = 0;
	private double aw_ = 0;
	private double ah_ = 0;
	
	/*******************************************************
	 * Create a new Round Rectangle shape
	 ******************************************************/
	public RoundRectangleShape(double x, double y, double width, double height, double arcw, double arch)
	{
		this.x_ = x;
		this.y_ = y;
		this.w_ = width;
		this.h_ = height;
		this.aw_ = arcw;
		this.ah_ = arch;
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.shapes.AbstractShape#getShape(java.util.List)
	 *******************************************************/
	@Override
	public Shape createAWTShape()
	{
	
		/* Create the elipse */
		return new RoundRectangle2D.Double(this.x_, this.y_, this.w_, this.h_, this.aw_, this.ah_);
		

	}


}
