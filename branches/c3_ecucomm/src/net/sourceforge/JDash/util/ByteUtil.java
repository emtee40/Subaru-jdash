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
    
    /**
     * Interpret the byte Array as a little-endian data value.
     * @param b a byte array
     * @return
     */
    public static long byteArrayToLongLE(byte[] b) 
    {
        long data = 0;
        if (b.length > 4) 
            throw new RuntimeException("Data value too large to be" +
                    "expressed as a long (requires " + b.length + " bytes).");
        for (int i=b.length-1; i >= 0; i--)
        {
            data <<= 8;
            data |= (long) b[i];
        }
        return data;           
    }
    /**
     * Interpret the byte array as a big endian data value
     * @param b a byte array
     * @return 
     */
    public static long byteArrayToLongBE(byte[] b)
    {
        long data = 0;
        if (b.length > 4) 
            throw new RuntimeException("Data value too large to be" +
                    "expressed as a long (requires " + b.length + " bytes).");
        for (int i=0; i < b.length; i++)
        {
            data <<= 8;
            data |= (long) b[i];
        }
        return data;        
    }
	
	
}
