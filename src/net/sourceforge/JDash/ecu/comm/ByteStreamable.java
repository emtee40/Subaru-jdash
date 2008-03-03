/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.JDash.ecu.comm;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
/**
 * Interface for an object that can be represented as a byte stream.  This
 * differs from the Serializable object in that it will not output the
 * identity of the object's class.
 * @author greg
 */


public interface ByteStreamable {
	public void write(OutputStream os) throws IOException;
	public void read(InputStream is) throws IOException;
}
