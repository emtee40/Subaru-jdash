/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: LineShape.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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
import java.awt.geom.Line2D;

/*******************************************************
 * Creates a Line shape for use within a gauge.
 ******************************************************/
public class LineShape extends AbstractShape
{
	
	protected double x1_ = 0;
	protected double y1_ = 0;
	protected double x2_ = 0;
	protected double y2_ = 0;
	
	/*******************************************************
	 * Create a new Line shape
	 ******************************************************/
	public LineShape(double x1, double y1, double x2, double y2)
	{
		this.x1_ = x1;
		this.y1_ = y1;
		this.x2_ = x2;
		this.y2_ = y2;
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.shapes.AbstractShape#getShape(java.util.List)
	 *******************************************************/
	@Override
	public Shape getShape()
	{
		
		/* Create the elipse */
		return new Line2D.Double(this.x1_, this.y1_, this.x2_, this.y2_);
		

	}


}
