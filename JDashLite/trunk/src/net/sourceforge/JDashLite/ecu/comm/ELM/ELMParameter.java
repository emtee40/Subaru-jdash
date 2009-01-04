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
 * http://en.wikipedia.org/wiki/OBD-II_PIDs
 *
 *********************************************************/
public abstract class ELMParameter extends ECUParameter
{
	
	
	public static final int MODE_1 = 1; 
	public static final int MODE_2 = 2;
	public static final int MODE_3 = 3;  /* Current Trouble Codes */
	public static final int MODE_4 = 4;  /* Clear Trouble Codes */
	public static final int MODE_5 = 5;
	public static final int MODE_6 = 6;
	public static final int MODE_7 = 7;  /* Pending Trouble Codes */
	public static final int MODE_8 = 8;
	public static final int MODE_9 = 9;
	public static final int MODE_A = 10;  /* Permanent Trouble Codes */
	
	public static final int COMMAND_NULL = -1;
	public static final int RESONSE_SIZE_DYNAMIC = -1;
	
	private int mode_ = -1;
	private int command_ = 01;
	private int responseSize_ = 0;
	private int[] responseBytes_ = null;
	protected boolean responseIsInHex_ = true; 
	
	private String fullCommand_ = null;
	private String expectedResponsePrefix_ = null;
	
	/********************************************************
	 * 
	 * @param name IN - The uniqe name for this parameter.
	 * @param mode IN - the ELM mode.  1 is the most common.
	 * @param command IN - the PID command. eg 0x0c for RPM.  a command of -1 means it will NOT be sent.  Like when sending a mode 03 request.
	 * @param responseSize IN - the expected number of response bytes, including the leading 41 0c ....
	 *******************************************************/
	public ELMParameter(String name, int mode, int command, int responseSize)
	{
		super(name);
		this.mode_ = mode;
		this.command_ = command;
		this.responseSize_ = responseSize;
		
		/* Look to see if a dynamic sized response is identified.  Ok.. so. we're not really making it dynamic, 
		 * but rather just givign it a nice big buffer. So far, this only seems needed for the DTCs anyway */
		if (RESONSE_SIZE_DYNAMIC == responseSize)
		{
			responseBytes_ = new int[ELMProtocol.MAX_RESPONSE_BUFFER];
		}
		else
		{
			responseBytes_ = new int[responseSize];
		}
		
		
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
	
	/********************************************************
	 * The default is true.  The response from an ELM module
	 * is usually in HEX form. Except for DTCs. They return in
	 * string/INT form.  If you are expecting a string of ints
	 * rather than a string of HEX, then return a false here.
	 * 
	 * @return
	 ********************************************************/
	public boolean isResponseInHex()
	{
		return responseIsInHex_;
	}
	
	/*******************************************************
	 * returns the value passed into the constructor for responseSize.
	 * This value is used to allocate space in the response buffer. 
	 * @return
	 ********************************************************/
	public int getResponseSize()
	{
		return this.responseSize_;
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
			this.fullCommand_ = Convert.unsigned2hex(getMode(), 2);
			
			/* A -1 command actually means to NOT send the command.  Like when sending mode 03 requests */
			if (getCommand() != COMMAND_NULL)
			{
				this.fullCommand_ += Convert.unsigned2hex(getCommand(), 2) + ELMProtocol.ELM_NEWLINE;
			}
		}
		
		return this.fullCommand_;
	}

	/*******************************************************
	 * @return
	 ********************************************************/
	public String getExpectedResponsePrefix()
	{
		if (this.expectedResponsePrefix_ == null)
		{
			this.expectedResponsePrefix_ = "4" + Convert.unsigned2hex(getMode(), 1);
			
			/* A -1 command actually means to NOT send the command.  Like when sending mode 03 requests */
			if (getCommand() != COMMAND_NULL)
			{
				this.expectedResponsePrefix_ += Convert.unsigned2hex(getCommand(), 2);
			}
		}
		
		return this.expectedResponsePrefix_;
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
		if (responseBytes_.length > 8)
		{
			throw new RuntimeException("Cannot convert the array of " + responseBytes_.length + " bytes to a long.  A long cannot be more than 8 bytes.");
		}
		
		/* Place and shift the bytes into a long */
		long r = getResponseByte(0);
		for (int shiftCount = 1; shiftCount < responseBytes_.length; shiftCount++)
		{
			r = r << 8;
			r += getResponseByte(shiftCount);
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
