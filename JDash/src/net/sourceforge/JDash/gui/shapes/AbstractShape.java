/*******************************************************
 * 
 *  @author spowell
 *  AbstractShape.java
 *  Aug 10, 2006
 *  $Id: AbstractShape.java,v 1.4 2006/12/31 16:59:09 shaneapowell Exp $
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
import java.util.Properties;

/*******************************************************
 * This abstract class represents a holding place for all
 * shapes that we draw on the screen.  Our polygons, circles,
 * and arcs, etc.. are extensions of this class.
 ******************************************************/
public abstract class AbstractShape
{
	
	public static final String KEYWORD_NONE = "none"; 
	
	/** This is the list of standard attributes for a given shape */
	public static enum PROPS {COLOR, FILL_COLOR, LINE_WIDTH, SIZE};
	
	/* The shapes properties */
	private Properties attributes_ = new Properties();
	
	
	/*******************************************************
	 * Create a new abstract shape object.
	 ******************************************************/
	public AbstractShape()
	{
		
	}

	/********************************************************
	 * @param key
	 * @param value
	 *******************************************************/
	public void addAttribute(PROPS key, String value)
	{
		
		if ((value == null) || (value.length() <= 0))
		{
			throw new RuntimeException("Cannot add a null attribute of type: " + key + " to this shape");
		}
		
		this.attributes_.put(key, value);
	}
	
	/********************************************************
	 * @param key
	 * @return
	 *******************************************************/
	public String getAttribute(PROPS key)
	{
		if (this.attributes_.get(key) != null)
		{
			return this.attributes_.get(key).toString();
		}
		else
		{
			return null;
		}
		
	}
	
	/*******************************************************
	 * Returns the shape that this abstract shape represents.
	 * @return
	 *******************************************************/
	public abstract Shape createAWTShape();
	
	
	
}
