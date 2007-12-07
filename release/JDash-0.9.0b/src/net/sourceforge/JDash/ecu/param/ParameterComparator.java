/*******************************************************
 * 
 *  @author spowell
 *  ParameterComparator
 *  Aug 28, 2007
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
package net.sourceforge.JDash.ecu.param;

import java.util.Comparator;

/*******************************************************
 * The parameter comparator.  At present only compares 
 * parameters by their name.
 *******************************************************/
public class ParameterComparator implements Comparator<Parameter>
{
	/*******************************************************
	 * Override
	 * @see java.util.Comparator#compare(T, T)
	 *******************************************************/
	public int compare(Parameter p0, Parameter p1)
	{
		
		String name0 = p0.getName();
		String name1 = p1.getName();
		
		return name0.compareToIgnoreCase(name1);
		
	}
}
