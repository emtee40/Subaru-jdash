/*******************************************************
 * 
 *  @author spowell
 *  UTIL.java
 *  Aug 8, 2006
 *  $Id: UTIL.java,v 1.3 2006/09/14 02:03:45 shaneapowell Exp $
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
package net.sourceforge.JDash.util;

import gnu.io.CommPortIdentifier;

import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*******************************************************
 * A simple utility class.
 ******************************************************/
public class UTIL
{
	
	public enum SCALE {ASPECT, STRETCH};
	
	/********************************************************
	 *  Return a list of all comm ports.
	 *******************************************************/
    public static void listPorts()
    {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
            System.out.println(portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType()) );
        }        
    }
    
    
    /*******************************************************
     * Translate the comport identifier into a human readable string.
     * 
     * @param portType IN - one of the constants in CommPortIdentifier. 
     * @return
     *******************************************************/
    public static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
    
    
    /********************************************************
	 * A simple utility function that can adjust the values of
	 * a dimension based on the 2 fed into it.  For example, 
	 * if D1 is a JPanel, and D2 is an image. You can pass them
	 * into this method with a type code of "SCALE_ASPECT", and
	 * the returned dimension will contain the width and height
	 * needed to put the image into the JPanel streched to fit, but
	 * will maintain the aspect ratio.
	 * 
	 * @param e
	 * @return
	 *******************************************************/
	public static Dimension scale(JPanel panel, ImageIcon icon,  SCALE scale)
	{
		
		/* Stretch */
		if (scale == SCALE.STRETCH)
		{
			return new Dimension(panel.getWidth(), panel.getHeight());
		}
		
		
		/* Aspect */
		if (scale == SCALE.ASPECT)
		{
			return aspectScale(new Dimension(panel.getWidth(), panel.getHeight()), 
								new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		}
		
		
		throw new RuntimeException("Invalid Scale Type: " + scale);
		
	}
	
	
	/********************************************************
	 * Given the width and height panel (1) values, calculate the
	 * scaled width and height for values (2), and return a new 
	 * dimension object.
	 * @param d1 IN - the dimenstion to scale to
	 * @param d2 IN - the dimension to scale
	 * @return
	 *******************************************************/
	public static Dimension aspectScale(Dimension d1, Dimension d2)
	{
		double imageAspect = (double)d2.width / (double)d2.height;
		double panelApect = (double)d1.width / (double)d1.height;
		
		int width, height;
		
		if (imageAspect > panelApect)
		{
			height = (int)((double)d1.width / (double)d2.width * (double)d2.height);
			width = d1.width;
		}
		else
		{
			height = d1.height;
			width = (int)((double)d1.height / (double)d2.height * (double)d2.width);
		}
		
		
		return new Dimension(width, height);
	}
	
	
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
	
		
	/********************************************************
	 * @param args
	 *******************************************************/
	public static void main(String[] args)
	{
		Dimension imageSize = new Dimension(8500, 1000);
		Dimension panelSize = new Dimension(400,400);

		Dimension newSize = aspectScale(panelSize, imageSize);
		
		System.out.println("Scaled:" + newSize);
	}
	
}
