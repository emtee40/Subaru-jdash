/*******************************************************
 * 
 *  @author spowell
 *  GlyphFactory.java
 *  Aug 12, 2006
 *  $Id: GlyphFactory.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.gui.shapes.txt;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/*******************************************************
 * Get your glyphs from here.  Don't create a glyph
 * directly.  You can do it, but this factory will
 * cache glyphs and re-use them. This will
 * reduce the overhead of creating a new glyph every
 * time for every character.
 ******************************************************/
public class GlyphFactory
{

	/******************************************************
	 * This class is a static singleton.
	 ******************************************************/
	private GlyphFactory()
	{
	}
	
	/*******************************************************
	 * @param c
	 * @return
	 *******************************************************/
	public static Glyph getGlyph(char c, int charWidth, int charHeight)
	{
		return new DigitalGlyph(c, charWidth, charHeight);
	}
	

	/*******************************************************
	 * @param x
	 * @param y
	 * @return
	 ******************************************************/
	public static Glyph moveGlyph(Glyph glyph, int x, int y)
	{
		ArrayList<Shape> shapes = new ArrayList<Shape>(); 
		
		AffineTransform moveTransform = AffineTransform.getTranslateInstance(x, y);
		
		/* Translate each shape */
		for (Shape shape : glyph.getGlyphShapes())
		{
			shapes.add(moveTransform.createTransformedShape(shape));
		}
		
		/* Return the new glyph */
		return new XformedGlyph(shapes);
		
		
	}
	
	
	/******************************************************
	 * This class is just a simple quick wrapper class for a glyph class.
	 ******************************************************/
	private static class XformedGlyph implements Glyph
	{
		private List<Shape> shapes_ = null;
		
		/******************************************************
		 * @param shapes
		 *****************************************************/
		public XformedGlyph(List<Shape> shapes)
		{
			this.shapes_ = shapes;
		}
		
		/*******************************************************
		 * Override
		 * @see net.sourceforge.JDash.gui.shapes.txt.Glyph#getGlyphShapes()
		 *******************************************************/
		public List<Shape> getGlyphShapes()
		{
			return this.shapes_;
		}
	}
	
}
