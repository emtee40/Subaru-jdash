/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: RectangleShape.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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
import java.awt.geom.Rectangle2D;

/*******************************************************
 * Creates a Rectangle shape for use within a gauge.
 ******************************************************/
public class RectangleShape extends AbstractShape
{
	
	private double x_ = 0;
	private double y_ = 0;
	private double w_ = 0;
	private double h_ = 0;

	
	/*******************************************************
	 * Create a new Rectangle shape
	 ******************************************************/
	public RectangleShape(double x, double y, double width, double height)
	{
		this.x_ = x;
		this.y_ = y;
		this.w_ = width;
		this.h_ = height;
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.shapes.AbstractShape#getShape(java.util.List)
	 *******************************************************/
	@Override
	public Shape createAWTShape()
	{
		
		/* Create the elipse */
		return new Rectangle2D.Double(this.x_, this.y_, this.w_, this.h_);
		

	}


}
