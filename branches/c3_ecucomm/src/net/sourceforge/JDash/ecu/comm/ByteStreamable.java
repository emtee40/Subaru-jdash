/*******************************************************
 * 
 *  @author greg
 *  InitListener.java
 *  June 1, 2008
 *
Copyright (C) 2008  greg

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
/**
 * Interface for an object that can be represented as a byte stream.  This
 * differs from the Serializable object in that it will not output the
 * identity of the object's class.
 */


public interface ByteStreamable {
	public void write(OutputStream os) throws IOException;
	public void read(InputStream is) throws IOException;
}
