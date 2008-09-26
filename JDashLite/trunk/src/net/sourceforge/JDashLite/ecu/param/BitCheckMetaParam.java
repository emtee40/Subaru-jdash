/*********************************************************
 * 
 * @author spowell
 * CtoFMetaParam.java
 * Sep 20, 2008
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

package net.sourceforge.JDashLite.ecu.param;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;

/*********************************************************
 * A BitCheck meta parameter takes the double from getValue()
 * and casts it directly to an int.  Then, the int is compared with
 * the bitmask passed to the constructor.  If they compare to a 
 * non Zero value, then 1 is returned.  Else 0 is returned.
 *
 *********************************************************/
public abstract class BitCheckMetaParam extends OneParamMetaParameter
{
	
	private int valueByte_ = 0;
	private int bitMask_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public BitCheckMetaParam(String name, ECUParameter bitmap, int bitMask)
	{
		super(name, bitmap);
		this.bitMask_ = bitMask;
		this.isSelectable_ = false;
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		int bitmap = (int)this.p1_.getValue();
		return (bitmap & this.bitMask_)==0?0:1;
	}

}
