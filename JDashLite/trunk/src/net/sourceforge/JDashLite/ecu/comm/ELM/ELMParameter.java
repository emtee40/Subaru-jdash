/*********************************************************
 * 
 * @author spowell
 * ELMProtocol.java
 * Jul 26, 2008
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

import waba.sys.Convert;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;


/*********************************************************
 * 
 *
 *********************************************************/
public abstract class ELMParameter extends ECUParameter
{
	
	private int mode_ = -1;
	private int command_ = 01;
	private int[] responseBytes_ = null;
	
	private String fullCommand_ = null;
	
	/********************************************************
	 * 
	 * @param name IN - The uniqe name for this parameter.
	 * @param mode IN - the ELM mode.  1 is the most common.
	 * @param command IN - the PID command. eg 0x0c for RPM
	 * @param responseSize IN - the expected number of response bytes, including the leading 41 0c ....
	 *******************************************************/
	public ELMParameter(String name, int mode, int command, int responseSize)
	{
		super(name);
		this.mode_ = mode;
		this.command_ = command;
		responseBytes_ = new int[responseSize];
		
		
	}
	
	/********************************************************
	 * @return
	 ********************************************************/
	public int getMode()
	{
		return this.mode_;
	}
	
	/********************************************************
	 * @return the command
	 ********************************************************/
	public int getCommand()
	{
		return this.command_;
	}
	
	
	/*******************************************************
	 * Given the mode and PID, generate the full ELM command, 
	 * including the trailing "\r" character.
	 * @return
	 ********************************************************/
	public String getFullCommand()
	{
		if (this.fullCommand_ == null)
		{
			this.fullCommand_ = Convert.unsigned2hex(getMode(), 2) + Convert.unsigned2hex(getCommand(), 2) + ELMProtocol.ELM_NEWLINE;
		}
		
		return this.fullCommand_;
	}

	/*******************************************************
	 * @return
	 ********************************************************/
	public int[] getResponseBytes()
	{
		return this.responseBytes_;
	}
	
	/*******************************************************
	 * Parse the response string and return the ELM byte value
	 * indicated with the index value.
	 * @param index
	 * @return
	 ********************************************************/
	public int getResponseByte(int index)
	{
		return this.responseBytes_[index];
	}

	/*******************************************************
	 * For convience, simply casts the byte into a float.
	 * @param index
	 * @return
	 ********************************************************/
	public double getResponseDouble(int index)
	{
		return (double)getResponseByte(index);
	}
	
	
	/*******************************************************
	 * Unlike the call to getResponsedouble(int) that returns one of the 
	 * bytes as a double. This method will assume that the 1-2 bytes that
	 * make up this parameter infact are a 16 bit word that represents 
	 * an int.  This int is then cast to a double
	 * 
	 * @return
	 ********************************************************/
	public double getResponseDouble()
	{
		int r = getResponseByte(0);
		if (responseBytes_.length == 2)
		{
			r = r << 8;
			r += getResponseByte(1);
		}
		return (double)r;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#setDemoValue()
	 ********************************************************/
	public void setDemoValue()
	{
		/* Increment each value in the int */
		for (int index = 0 ;index < this.responseBytes_.length; index++)
		{
			byte b = (byte)this.responseBytes_[index];
			b+=2;
			this.responseBytes_[index] = b;
		}
		
	}
}
