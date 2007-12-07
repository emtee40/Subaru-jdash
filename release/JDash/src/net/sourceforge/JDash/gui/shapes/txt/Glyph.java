/*******************************************************
 * 
 *  @author spowell
 *  Glyph.java
 *  Aug 12, 2006
 *  $Id: Glyph.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
import java.util.List;

/*******************************************************
 * This interface is all that is needed to draw a 
 * text glyph to the screen.
 ******************************************************/
public interface Glyph
{

	/*******************************************************
	 * return the array list of shapes that willd raw this glyph.
	 * @return a list of shapes that will render this glyph. 
	 *******************************************************/
	public List<Shape> getGlyphShapes();
	
}

