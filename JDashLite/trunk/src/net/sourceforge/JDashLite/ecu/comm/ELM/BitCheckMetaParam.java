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

package net.sourceforge.JDashLite.ecu.comm.ELM;

import net.sourceforge.JDashLite.ecu.param.OneParamMetaParameter;

/*********************************************************
 * This one is specific to the ELM parameters.   A bitmap parameter
 * can have any number of bits set in the bytes to represent.. well..
 * anything.  If the bit is set, then the call to getValue() will be 1.
 * If the bit is NOT set, the getValue() method will return 0.
 * The bitindex is the bit to check starting from left going to the right.
 *
 *********************************************************/
public abstract class BitCheckMetaParam extends OneParamMetaParameter
{
	
	private int valueByte_ = 0;
	private int bitMask_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public BitCheckMetaParam(String name, ELMParameter bitmap, int valueByte, int bitMask)
	{
		super(name, bitmap);
		this.valueByte_ = valueByte;
		this.bitMask_ = bitMask;
		this.isSelectable_ = false;
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
	 ********************************************************/
	public double getValue()
	{
		int bitmap = ((ELMParameter)this.p1_).getResponseByte(this.valueByte_);
		return (bitmap & this.bitMask_)==0?0:1;
	}

}
