/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: ImageShape.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

/*******************************************************
 * Creates a Rectangle shape for use within a gauge.
 ******************************************************/
public class ImageShape extends AbstractShape
{

	private double x_ = 0;
	private double y_ = 0;
	private double w_ = 0;
	private double h_ = 0;

	private ImageIcon scaledImage_ = null;
	
	/*******************************************************
	 * Create a new Rectangle shape
	 ******************************************************/
	public ImageShape(ImageIcon image, double x, double y, double width, double height) throws Exception
	{
		
		this.x_ = x;
		this.y_ = y;
		this.w_ = width;
		this.h_ = height;
		
		
		
		if (this.w_ == -1)
		{
			this.w_ = image.getIconWidth();
		}
		
		if (this.h_ == -1)
		{
			this.h_ = image.getIconHeight();
		}
		
		/* Loop until the image has been loaded */
		while (image.getImageLoadStatus() != MediaTracker.COMPLETE) { Thread.sleep(100); };
		
		/* Store our new scaled image for returning to the caller method */
		this.scaledImage_ = new ImageIcon(image.getImage().getScaledInstance((int)this.w_, (int)this.h_, Image.SCALE_SMOOTH));
		
	}

	/*******************************************************
	 * Override
	 * We'll return a simple rectangle that will be returned just to
	 * generate a bounds.  Inside of the AbstractGague classes paintShapes()
	 * method, we watch for ImageShape instances. we DON"T render the returned awt shape,
	 * but instead just use it's values to render the image.
	 * @see net.sourceforge.JDash.gui.shapes.AbstractShape#getShape(java.util.List)
	 *******************************************************/
	@Override
	public Shape getShape()
	{
		/* Create the elipse */
		return new Rectangle2D.Double(this.x_, this.y_, this.w_, this.h_);
		
	}


	/*******************************************************
	 * Return the image. There is no need to scale the image, as it will be
	 * scaled by the gauge doing the painting. 
	 * @return
	 *******************************************************/
	public ImageIcon getImageIcon()
	{
		return this.scaledImage_;
	}
	
}
