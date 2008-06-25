/*******************************************************
 * 
 *  @author spowell
 *  ECUParameter
 *  Aug 8, 2006
 *  $Id: ECUParameter.java,v 1.3 2006/12/31 16:59:09 shaneapowell Exp $
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

import net.sourceforge.JDash.util.ByteUtil;


/*****************************************************
 * An ECU Parameter is an extension to the minimum parameter.
 * The difference is that an ECU parameter has the info needed by
 * the monitor to identify this parameter to the ECU, namely the
 * address byte array. This array of bytes is setup by the
 * parameter loader, and identifies to the particular monitor
 * the necessary information to extract this parameters values.
 * In the case of the SSP monitor, this is a simple 3 byte array 
 * of a memory address.  In the case of an ELM scanner, this 
 * might be a 10 byte array of command codes. (Note: I do NOT 
 * have an ELM scanner, so I made that last bit up as an example).
 ******************************************************/
public class ECUParameter extends Parameter 
        implements Cloneable
{
	
    private byte[] _address;
    private Double _result;
    private String _name;
    private String _description;
    private int rate_ = 0;
    private long lastFetchTime_ = 0;
    

    /**
     * 
     * @param address A byte array representing the address to query
     * @param name A simple name for the parameter
     * @param description A longer description for the parameter
     * @param rate Preferred sampling rate in milliseconds per sample.
     */
    public ECUParameter(byte[] address, String name, String description, int rate)
    {
        this._address     = address;
        this._name        = name;
        this._description = description;
        this.rate_        = rate;
        this._result      = null;
        
        if (this._name == null)
        {
        	String error = "Cannot create an ECU parameter with a null name." +
                    this.toString();
            
        	throw new RuntimeException(error);  
        }
    }
    
    @Override
    public ECUParameter clone()
    {
        byte[] address = new byte[_address.length];
        for (int i=0; i < _address.length; i++) 
            address[i] = _address[i];
        
        return new ECUParameter(address, _name, _description, rate_);
    }
    
    @Override
    public String toString() {
        String s;

        
        s  =   "Name: " + this._name +
             "\nDesc: " + this._description + 
             "\nAddr: " + ByteUtil.bytesToString(this._address) + 
             "\nRate: " + this.rate_ + " ms/sample" + 
             "\nLastFetch: " + this.lastFetchTime_+ 
             "\nResult: " + this._result;
        return s;
    }

    /******************************************************
     * Override
     * @see net.sourceforge.JDash.ecu.param.Parameter#getName()
     *******************************************************/
    public String getName() {
        return _name;
    }

    /********************************************************
     * @return
     *******************************************************/
    public String getDescrption()
    {
    	return this._description;
    }
    
    /*******************************************************
     * set the value of the result variable, and notify all
     * observers of the change.
     * @param r
     *******************************************************/
    public void setResult(double r)
    {
        _result = r;
        fireValueChangedEvent();
    }
    
    
    /*******************************************************
     * Set the value of the result value using a byte value.
     * Bytes are ALWAYS assumed to be unsigned bytes.  No negative
     * values allowed, the are treated as ints.  0xff = 256, NOT -127
     * @param b
     *******************************************************/
    public void setResult(byte b)
    {
    	setResult((double)ByteUtil.unsignedByteToInt(b));
    }

    /******************************************************
     * return the current result value.  If nothing has
     * yet been fetchd from the ECU, return a 0
     * Override
     * @see net.sourceforge.JDash.ecu.param.Parameter#getResult()
     ******************************************************/
    public double getResult()
    {
    	if (this._result == null)
    	{
    		return 0;
    	}
    	
    	return _result;
    }

    /*******************************************************
     * @return
     *******************************************************/
    public byte[] getAddress()
    {
    	return this._address;
    }
    /**
     * Return the address as a long integer.  
     * We interpret the address as a big endian value.
     * @return
     */
    public long getAddressAsLong() 
    {
        return ByteUtil.byteArrayToLongBE(_address);
    }

    /*******************************************************
     * Get the desired update speed of this ecu parameter in milliseconds.
     * It is up to the monitor, but you can optionally support
     * the ONLY sending of parameter requests if within the speed
     * setting define.
     * @return
     ******************************************************/
    public int getPreferredRate()
    {
    	return this.rate_;
    }
    public void setPreferredRate(int rate)
    {
    	this.rate_ = rate;
    }
    
    /********************************************************
     * This value is used by the monitor to keep track of the 
     * last time this parameter was fetched. 
     * @param time
     * @return
     ******************************************************/
    public void setLastFetchTime(long time)
    {
    	this.lastFetchTime_ = time;
    }
    
    /*******************************************************
     * This value is used by the monitor to keep track of the
     * last time this parameter was fetched.
     * @return
     *******************************************************/
    public long getLastFetchTime()
    {
    	return lastFetchTime_;
    }
    
}
