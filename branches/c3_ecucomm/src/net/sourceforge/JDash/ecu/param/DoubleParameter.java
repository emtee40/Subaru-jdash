/*******************************************************
 * 
 *  @author spowell
 *  DoubleParameter.java
 *  Dec 13, 2006
 *  $ID:$
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

/*******************************************************
 * Most parameters are of type Double. This interface is used just
 * to identify those params. Some parameters are Strings
 * and will implement the StringParameter interface.   This
 * interface is implemented by the lowest level Parameter object, 
 * so if your monitor should check for the StringParameter before
 * checkign for the the DoubleParameter interface.
 ******************************************************/
public interface DoubleParameter
{

}
