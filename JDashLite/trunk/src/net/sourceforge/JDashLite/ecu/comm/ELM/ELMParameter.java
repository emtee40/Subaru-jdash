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
	private int command_ = COMMAND_NULL;
	private int responseCharCount_ = 0;
	private char[] responseChars_ = null;
	
	private int responseOffset_ = 0;
	
	private String fullCommand_ = null;
	private String expectedResponsePrefix_ = null;
	
	/********************************************************
	 * 
	 * @param name IN - The uniqe name for this parameter.
	 * @param mode IN - the ELM mode.  1 is the most common.
	 * @param command IN - the PID command. eg 0x0c for RPM.  a command of -1 means it will NOT be sent.  Like when sending a mode 03 request.
	 * @param responseSize IN - the expected number of response bytes, including the leading 41 0c ....
	 *******************************************************/
	public ELMParameter(String name, int mode, int command, int responseCharCount)
	{
		super(name);
		this.mode_ = mode;
		this.command_ = command;
		this.responseCharCount_ = responseCharCount;
		
		/* Pre-calculate the location of the first byte of the response data */
		this.responseOffset_ = 2;
		if (this.command_ != COMMAND_NULL)
		{
			this.responseOffset_ += 2;
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
	
	/*******************************************************
	 * returns the value passed into the constructor for responseSize.
	 * This value is used to allocate space in the response buffer. 
	 * @return
	 ********************************************************/
	public int getResponseCharCount()
	{
		return this.responseCharCount_;
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
	private String getExpectedResponsePrefix()
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
	 * @param resp
	 * @param startIndex
	 * @param length
	 * @throws Exception if any part of the response byte was missing, formmated incorectly, 
	 * etc. etc.
	 ********************************************************/
	protected void extractResponseBytes(byte[] resp, int startIndex, int length) throws RuntimeException
	{
		
		/* Initialize the array if needed, or if resized */
		if (this.responseChars_ == null || (this.responseChars_.length != length))
		{
			this.responseChars_ = new char[length];
		}
		
		/* Copy each byte, and in the process convert to chars.  I know.. same thing.  This is done just for clarity */
		for (int index = 0; index < length; index++)
		{
			this.responseChars_[index] = (char)resp[startIndex + index];
		}
		
		
		/* Check the mode returned */
		
		
		/* Check the command returned */
		
		
		
		notifyValueChanged();
	}
	
	
	/*******************************************************
	 * Given the response characters identified between
	 * start and length, convert them to the equvelant INT 
	 * value is though they were all HEX chars.
	 * For example, if the buffer contained [41010345]
	 * And you called fromHexCharsToInt(0,4).  It would convert 0345 as if it was 0x0345 into 837
	 * @param start IN - the starting index withing the response data.  This method already
	 * knows how to skip the header prefix data. So, the actual data starts at 0 99.99% of the time
	 * @param length How many chars to use to make up the int.
	 * @return
	 ********************************************************/
	public int fromHexCharsToInt(int start, int length)
	{
		if (this.responseChars_ == null)
		{
			return 0;
		}
		
		int v = 0;
		boolean shift = false;
		
		for (int index = start + this.responseOffset_; index < start + this.responseOffset_ + length; index++)
		{
			/* Shift 4 bits for the single oct char */
			if (shift)
			{
				v = v << 4;
			}

			/* bitwise AND the next OCT into place */
			v = v | toOct(this.responseChars_[index]);
			
			/* From now on, we'll need to shift first */
			shift = true;
		}
		
		return v;
	}
	
	
	/*******************************************************
	 * Similar to fromHexCharsToInt except this method does
	 * not expect the chars to be HEX values, but instead
	 * to already be INT values.
	 * @param start
	 * @param length
	 * @return
	 ********************************************************/
	public int fromCharsToInt(int start, int length)
	{
		
		if (this.responseChars_ == null)
		{
			return 0;
		}
		
		int v = 0;
		boolean shift = false;
		
		for (int index = start + this.responseOffset_; index < start + this.responseOffset_ + length; index++)
		{
			/* Shift 4 bits for the single oct char */
			if (shift)
			{
				v = v * 10;
			}

			/* bitwise AND the next OCT into place */
			v = v | (this.responseChars_[index] - '0' );
			
			/* From now on, we'll need to shift first */
			shift = true;
		}
		
		return v;
		
	}
	
	/*******************************************************
	 * Since most ELM responses are a series of 2 char HEX
	 * values that make up a simple INT, this method gives us
	 * easy access to read these pair, and return the combined INT value.
	 * This is just a convience method to fromHexCharsToInt
	 * @param ndx
	 * @return
	 ********************************************************/
	public int getInt(int ndx)
	{
		return fromHexCharsToInt(ndx, 2);
	}
	
	
	/*******************************************************
	 * Just calls getInt(int) and casts it as a double.
	 * 
	 * @param ndx
	 * @return
	 ********************************************************/
	public double getDouble(int ndx)
	{
		return (double)getInt(ndx);
	}
	
	
	
//	/*******************************************************
//	 * @return
//	 ********************************************************/
//	public int[] getResponseBytes()
//	{
//		return this.responseBytes_;
//	}
	
	
//	
//	/*******************************************************
//	 * Parse the response string and return the ELM byte value
//	 * indicated with the index value.
//	 * @param index
//	 * @return
//	 ********************************************************/
//	public char getResponseChar(int index)
//	{
//		return this.responseChars_[index];
//	}
//
//	/*******************************************************
//	 * For convience, simply casts the byte into a float.
//	 * @param index
//	 * @return
//	 ********************************************************/
//	public double getResponseDouble(int index)
//	{
//		return (double)getResponseChar(index);
//	}
	
	
//	/*******************************************************
//	 * Unlike the call to getResponsedouble(int) that returns one of the 
//	 * bytes as a double. This method will assume that the 1-2 bytes that
//	 * make up this parameter infact are a 16 bit word that represents 
//	 * an int.  This int is then cast to a double
//	 * 
//	 * @return
//	 ********************************************************/
//	public double getResponseDouble()
//	{
//		if (responseBytes_.length > 8)
//		{
//			throw new RuntimeException("Cannot convert the array of " + responseBytes_.length + " bytes to a long.  A long cannot be more than 8 bytes.");
//		}
//		
//		/* Place and shift the bytes into a long */
//		long r = getResponseByte(0);
//		for (int shiftCount = 1; shiftCount < responseBytes_.length; shiftCount++)
//		{
//			r = r << 8;
//			r += getResponseByte(shiftCount);
//		}
//		
//		return (double)r;
//	}
//	
	
	/********************************************************
	 *  Convert the character into it's int octal form
	 * @param b
	 * @return
	 ********************************************************/
	private int toOct(char b) throws RuntimeException
	{
		
		/* convert the character number into the byte value */
		if (b >= '0' && b <= '9')
		{
			return b - 0x30;
		}
		else if (b >= 'A' && b <= 'F')
		{
			return b - 0x41 + 10;
		}
		else if (b >= 'a' && b <= 'f')
		{
			return b - 0x61 + 10;
		}
		else
		{
			throw new RuntimeException("the char [" + b + "] is not a valid octal character");
		}
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#setDemoValue()
	 ********************************************************/
	public void setDemoValue()
	{
		final byte[] demoValue = new  byte[this.getResponseCharCount() == RESONSE_SIZE_DYNAMIC?8:this.getResponseCharCount()];

		
		/* Increment each value in the int */
		for (int index = 0 ;index < demoValue.length; index++)
		{
			byte c = demoValue[index];
			c+=2;
			demoValue[index] = c;
		}
		
		extractResponseBytes(demoValue, 0, demoValue.length);
	}
	

	/********************************************************
	 * Unit Testing
	 * @param args
	 ********************************************************/
	public static void main(String[] args)
	{
		try
		{
			byte[] b = new byte[] {'4','3','0','3','0','2','0','3','0','3'};
			ELMParameter p = new ELMParameter("test", 1, ELMParameter.COMMAND_NULL, 1)
			{
				public String getDescription()
				{
					return null;
				}
				public String getLabel() 
				{
					return null;
				}
				public double getValue()
				{
					return 0;
				}
			};
			
			p.extractResponseBytes(b, 0, b.length);
			//System.out.println(">>" + p.fromHexCharsToInt(0,6));
			System.out.println(">>" + p.fromCharsToInt(0,4));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
