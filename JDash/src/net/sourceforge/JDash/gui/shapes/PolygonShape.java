/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: PolygonShape.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/*******************************************************
 * Creates a polygond shape for use within a gauge.
 ******************************************************/
public class PolygonShape extends AbstractShape
{
	
	
	/* The list of points */
	private ArrayList<Point2D> points_ = new ArrayList<Point2D>();

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.shapes.AbstractShape#getShape(java.util.List)
	 *******************************************************/
	@Override
	public Shape createAWTShape()
	{
		
		/* Create the polygon */
		Polygon polygon = new Polygon();
		
		for (Point2D point : this.points_)
		{
			polygon.addPoint((int)point.getX(), (int)point.getY());
		}
		
		return polygon;
	}

	/********************************************************
	 * @param x
	 * @param y
	 *******************************************************/
	public void addPoint(int x, int y)
	{
		this.points_.add(new Point2D.Double((double)x, (double)y));
	}

}
