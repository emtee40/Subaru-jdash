/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: ButtonShape.java,v 1.4 2006/09/14 02:03:43 shaneapowell Exp $
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import net.sourceforge.JDash.skin.SkinEvent;


/*******************************************************
 * Creates a Rectangle shape for use within a gauge as a button
 ******************************************************/
public class ButtonShape extends AbstractShape implements ComponentShape
{

	private String type_ = null;
	
	private double x_ = 0;
	private double y_ = 0;
	private double w_ = 0;
	private double h_ = 0;
	private ImageIcon upImage_ = null;
	private ImageIcon downImage_ = null;
	
	private List<SkinEvent> events_ = new ArrayList<SkinEvent>();

	
	/*******************************************************
	 * Create a new Rectangle shape
	 ******************************************************/
	public ButtonShape(String type, double x, double y, double width, double height, ImageIcon upImageName, ImageIcon downImageName)
	{
		this.type_ = type;
		this.x_ = x;
		this.y_ = y;
		this.w_ = width;
		this.h_ = height;
		this.upImage_ = upImageName;
		this.downImage_ = downImageName;
	}
	
	
	
	/********************************************************
	 * Returns a string type code for this button.  Referr to the gauge this
	 * button is a member of for a list of valid type codes.   for example,
	 * the AnalogGauge supports "high-reset" and "low-reset"
	 * @return
	 *******************************************************/
	public String getType()
	{
		return this.type_;
	}


	/********************************************************
	 * @param se
	 *******************************************************/
	public void addSkinEvent(SkinEvent se)
	{
		this.events_.add(se);
	}
	
	/********************************************************
	 * @return
	 *******************************************************/
	public List<SkinEvent> getSkinEvents()
	{
		return this.events_;
	}
	
	/*******************************************************
	 * Get the image for the up state.
	 * @return
	 ******************************************************/
	public ImageIcon getUpImage()
	{
		return this.upImage_;
	}
	
	/*******************************************************
	 * Get the image for the down state.
	 * @return
	 *******************************************************/
	public ImageIcon getDownImage()
	{
		return this.downImage_;
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
