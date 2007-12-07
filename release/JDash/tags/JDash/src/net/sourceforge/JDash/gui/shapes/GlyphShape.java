/*******************************************************
 * 
 *  @author spowell
 *  GlyphShape.java
 *  Dec 6, 2007
 *  $Id:$
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

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;


/*******************************************************
 * This interface simply defines a shape as a Glyph capable shape.
 * This is really just for TextShapes, but any shape can be a 
 * GlyphShape.  All they need to do is implement this.
 ******************************************************/
public interface GlyphShape
{

	/*******************************************************
	 * Return the GlyphVector that will be rendered to the screen
	 *******************************************************/
	public GlyphVector getGlyphVector(FontRenderContext ctx);
	
}
