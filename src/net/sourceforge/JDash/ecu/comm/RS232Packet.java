/*******************************************************
 * 
 *  @author spowell
 *  RS232Packet
 *  Aug 8, 2006
 *  $Id: RS232Packet.java,v 1.3 2006/09/14 02:03:42 shaneapowell Exp $
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
package net.sourceforge.JDash.ecu.comm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sourceforge.JDash.util.ByteUtil;


/******************************************************
 * This montior acts on it's version of the RS232 protocol.
 * Each packet consists of a header, followed by a
 * data length byte, followed by the data, and finally a
 * checksum byte.  It doesn't matter if your sending, or
 * receiveing a packet with this monitor, it uses this 
 * object to send and receive packets  In order to send a
 * packet you'll need to set the header, data and checksum.
 * The datalength is automatically set when the setData()
 * method is called.
 ******************************************************/
public class RS232Packet
{
	/** This is the maximum number of bytes allowed in the data array */
	public static final int MAX_DATA_LENGTH = 128;
	
	private byte[] header_ = null;
	private int dataLength_ = 0;
	private byte[] data_ = null;
	private byte checkSum_ = 0;
	
	
	/******************************************************
	 * Create a new empty packet
	 ******************************************************/
	public RS232Packet()
	{
	}
	
	/******************************************************
	 * @return
	 ******************************************************/
	public byte[] getHeader()
	{
		return this.header_;
	}
	
	/*******************************************************
	 * @param header
	 ******************************************************/
	public void setHeader(byte[] header)
	{
		this.header_ = header;
	}
	

	/*******************************************************
	 * @return
	 ******************************************************/
	public int getDataLength()
	{
		return this.dataLength_;
	}
	
	/*******************************************************
	 * @param b
	 ******************************************************/
	public void setDataLength(byte b)
	{
		setDataLength(ByteUtil.unsignedByteToInt(b));
	}
	
	/*******************************************************
	 * @param len
	 ******************************************************/
	public void setDataLength(int len)
	{
		if (len > MAX_DATA_LENGTH)
		{
			throw new RuntimeException("Maximum of [" + MAX_DATA_LENGTH + "] data bytes exceeded in RS232 Packet. [" + len + "]"); 
		}
		
		this.dataLength_ = len;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public byte[] getData()
	{
		return this.data_;
	}
	
	/********************************************************
	 * Set the data array to the given data array value. 
	 * The setDataLength() method will be called automatically.
	 * @param data
	 ******************************************************/
	public void setData(byte[] data)
	{
		this.data_ = data;
		this.setDataLength(data.length);
	}
	
	
	
	/*******************************************************
	 * @param data
	 ******************************************************/
	public void setData(List<Byte> data)
	{
		byte[] dataArray = new byte[data.size()];
		
		for (int index = 0; index < data.size(); index++)
		{
			dataArray[index] = data.get(index);
		}
		
		setData(dataArray);
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public byte getCheckSum()
	{
		return this.checkSum_;
	}
	
	/*******************************************************
	 * @param checkSum
	 ******************************************************/
	public void setCheckSum(byte checkSum)
	{
		this.checkSum_ = checkSum;
	}
	
	/******************************************************
	 * Given the current state of this packet, calcualte and set
	 * it's checksum.
	 ******************************************************/
	public void setCheckSum()
	{
		this.checkSum_ = calcCheckSum();
	}
	
	
	/*******************************************************
	 * Given the current state of the header, datalength, and data values,
	 * this method will calculate the checksum and return it.
	 *******************************************************/
	public byte calcCheckSum()
	{
		/* Check the checksum against the packet */
		byte checksum = 0;
		int i;
		for (i=0; i < header_.length; i++)
			checksum += header_[i];

		checksum += ((byte)this.dataLength_);

		for (i=0; i < data_.length; i++)
			checksum += data_[i];

		return checksum;
	}
	
	public boolean verifyCheckSum() {
		return (checkSum_ == calcCheckSum());
	}
	
	/*******************************************************
	 * Get the entire length of this packet. This
	 * is the length of the header array, plus 1 for the 
	 * data length byte, plus the length of the data array
	 * plus 1 for the checksum byte.
	 * @return
	 *******************************************************/
	public int length()
	{
		return this.header_.length + 1 + data_.length + 1;
	}

	
	/*******************************************************
	 * This is a simple convenience method to write the contents
	 * of this packet to the given output steam.
	 * @param os
	 *******************************************************/
	public void write(OutputStream os) throws IOException
	{
		
		
		/* Send the TX packet */
		os.write(getHeader());
		os.write((byte)getDataLength());
		os.write(getData());
		os.write(getCheckSum());

		
	}

	
	/******************************************************
	 * Override
	 * @see java.lang.Object#toString()
	 ******************************************************/
	public String toString()
	{
		String ret = "";
		
		ret = "\nRS232Packet";
		ret += "\nH: " + ByteUtil.bytesToString(getHeader());
		ret += "\nL: " + getDataLength();
		ret += "\nD: " + ByteUtil.bytesToString(getData());
		ret += "\nC: " + ByteUtil.unsignedByteToInt(getCheckSum());
		
		return ret;
	}
	
}

