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


/*********************************************************
 * Yeah , yeah.. I know.. this is NOT an object. 
 * But I just was not looking forwad to making a seperate 
 * .java file for each PID. So.. here ya ho.
 *
 *********************************************************/
public final class AllParameters
{
	
	/** Load **/
	public static class Load extends ELMParameter
	{
		public Load() { super("Load", 1, 0x04, 1); }
		public double getValue() { return getResponseDouble(0) * 100.0 / 255.0; }
	}
	
	/** Coolant **/
	public static class Coolant extends ELMParameter
	{
		public Coolant() { super("Coolant Temp", 1, 0x05, 1); }
		public double getValue() { return getResponseDouble(0) - 40.0; }
	}
	
	/** STFT **/
	public static class STFT1 extends ELMParameter
	{
		public STFT1() { super("STFT1", 1, 0x06, 1); }
		public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
	}
	
	/** LTFT **/
	public static class LTFT1 extends ELMParameter
	{
		public LTFT1() { super("LTFT1", 1, 0x07, 1); }
		public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
	}
	
	/** STFT **/
	public static class STFT2 extends ELMParameter
	{
		public STFT2() { super("STFT2", 1, 0x08, 1); }
		public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
	}
	
	/** LTFT **/
	public static class LTFT2 extends ELMParameter
	{
		public LTFT2() { super("LTFT2", 1, 0x09, 1); }
		public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
	}

	/** RPM **/
	public static class RPM extends ELMParameter
	{
		public RPM() { super("RPM", 1, 0x0c, 2); }
		public double getValue() { return ((getResponseDouble(0) * 256.0) + getResponseDouble(1)) / 4.0; }
	}
	
	

}
