/*******************************************************
 * 
 *  @author spowell
 *  StartupConfigWrapper.java
 *  Aug 10, 2006
 *  $Id:$
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
package net.sourceforge.JDash;


/*******************************************************
 * This class is a simple main() method for starting the config screen
 * from the command line easily.
 ******************************************************/
public class StartupConfigWrapper
{

	/********************************************************
	 * does nothign more than call the Starutp.main() method with
	 * the "-config" option.
	 * @param args
	 *******************************************************/
	public static void main(String[] args)
	{
		Startup.main( new String[] {Startup.CONFIG_FLAG});
	}
	
}
