package net.sourceforge.JDash.util;

public class ByteUtil
{

	
	/********************************************************
	 * convert the byte array to a human readable string.
	 * @param bytes
	 * @return a string representation of this byte array.
	 *******************************************************/
	public static String bytesToString(byte[] bytes)
	{
		StringBuffer sb = new StringBuffer();
		
		for(byte b : bytes)
		{
			sb.append(String.format("0x%02x ", b));
		}
		
		return sb.toString();
	}	

    
	/*******************************************************
	 * Convert an unsigned byte to an int. This is different than
	 * simply casting your byte with (int), because a cast preserves
	 * the +- of the value.  This will convert the byte
	 * in an unsigned format.
	 * @param b
	 * @return
	 ******************************************************/
	public static int unsignedByteToInt(byte b)
	{
		return b & 0xff;
	}
	
	
}
