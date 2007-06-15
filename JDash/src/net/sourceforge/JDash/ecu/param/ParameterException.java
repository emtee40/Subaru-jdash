/*******************************************************
 * 
 *  @author spowell
 *  ParameterException
 *  Aug 8, 2006
 *  $Id: ParameterException.java,v 1.2 2006/09/14 02:03:42 shaneapowell Exp $
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

public class ParameterException extends Exception{
	
	public static final long serialVersionUID = 0l;
	
    public ParameterException() {
        super();
    }

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterException(Throwable cause) {
        super(cause);
    }

    public String getMessage() {
        return super.getMessage();
    }

    public Throwable getCause() {
        return super.getCause();
    }

    public void printStackTrace() {
        super.printStackTrace();
    }
}
