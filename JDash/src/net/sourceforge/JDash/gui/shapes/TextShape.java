/*******************************************************
 * 
 *  @author spowell
 *  PolygonShape.java
 *  Aug 10, 2006
 *  $Id: TextShape.java,v 1.4 2006/12/31 16:59:09 shaneapowell Exp $
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

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;


/*******************************************************
 * Creates a Text shape for use within a gauge.  The generated
 * shape is actually a Line shape.  That way we can still
 * apply the needed transforms for building the text.
 * 
 * Cool font tricks
 * http://www.java2s.com/Code/Java/2D-Graphics-GUI/Paints.htm
 ******************************************************/
public class TextShape extends LineShape
{
	
	private Object displayValue_ = null;
	
	private Font font_ = new Font("Arial", Font.BOLD, 10);
	private String format_ = "";
	
	
	/*******************************************************
	 * Create a new ellipse shape
	 ******************************************************/
	public TextShape(double x, double y, String format, Font font)
	{
		super(x, y, x, y);
		this.format_ = format;
		this.font_ = font;
		
	}

	
	/*******************************************************
	 * @return
	 *******************************************************/
	public Font getFont()
	{
		return this.font_;
	}
	
	/*******************************************************
	 * @return
	 *******************************************************/
	public GlyphVector getGlyphVector(FontRenderContext ctx)
	{
		Font f = getFont().deriveFont(Font.BOLD, Integer.parseInt(this.getAttribute(PROPS.SIZE)));
		return f.createGlyphVector(ctx, getFormattedValue());
	}
	
	
	
	/********************************************************
	 * @param value
	 *******************************************************/
	public void setValue(Object value)
	{
		this.displayValue_ = value;
	}
	
	/********************************************************
	 * Given 
	 * @return
	 *******************************************************/
	public String getFormattedValue()
	{
		try
		{
			return String.format(this.format_, displayValue_);
		}
		catch(Exception e)
		{
			RuntimeException e2 = new RuntimeException("Error formatting display value on format [" + this.format_ + 
																"] with value [" + displayValue_ + 
																"] of class type [" + displayValue_.getClass().getName() + "]");
			e2.initCause(e);
			throw e2;
		}
	}
	

}
