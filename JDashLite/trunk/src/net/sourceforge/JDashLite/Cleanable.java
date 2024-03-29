/*********************************************************
 * 
 * @author spowell
 * Cleanable.java
 * Sep 7, 2008
 *
Copyright (C) 2008 Shane Powell

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
 *********************************************************/

package net.sourceforge.JDashLite;

/*********************************************************
 * Memory Leaks!!  hate 'em right?
 * Objects that implement this interface are helpint to 
 * keep memory leaks to a minimum.
 *
 *********************************************************/
public interface Cleanable
{

	/*******************************************************
	 * This object is about to be put out of refernce scope.  
	 * Prepeare it for GC.
	 ********************************************************/
	public void clean();
	
}
