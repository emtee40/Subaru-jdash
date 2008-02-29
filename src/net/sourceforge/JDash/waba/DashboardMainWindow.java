/*******************************************************
 * 
 *  @author spowell
 *  DashboardMainWindow
 *  Aug 1, 2007
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
package net.sourceforge.JDash.waba;


import net.sourceforge.JDash.ecu.comm.util.ELMUtil;
import net.sourceforge.JDash.waba.error.ErrorDialog;
import net.sourceforge.JDash.waba.error.ErrorLog;
import waba.io.SerialPort;
import waba.sys.Settings;
import waba.ui.MainWindow;


/*******************************************************
 *
 *****************************************************/
public class DashboardMainWindow extends MainWindow
{

	
	private SerialPort serialPort_ = null;

	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onStart()
	 *******************************************************/
	public void onStart()
	{
		try
		{
			/* Setup the look and feel */
			Settings.setUIStyle(Settings.Vista);
			
			
			/* Initialize the Logger instance */
			ErrorLog.init();

			try
			{

				//new gnu.io.RXTXCommDriver();
				//new com.sun.comm.Win32Driver();
				//Enumeration e = CommPortIdentifier.getPortIdentifiers();
				//System.out.println("--" + e.hasMoreElements());
				//while (e.hasMoreElements())
				//{
				//	System.out.println(">>>" + e.nextElement());
				//}
				
				//SerialPort sp = new SerialPort(0, 2400);
				
				
				/* Connect to the serial port */
				this.serialPort_ = new SerialPort(5, 9600, 8, false, 1);
				if (!this.serialPort_.isOpen())
				{
					ErrorDialog.showError("Unable to open serial port");
				}

				ELMUtil elmUtil = new ELMUtil();
				
				String tx = "010D\r";
				this.serialPort_.writeBytes(tx.getBytes(), 0, tx.length());
				Thread.sleep(10);
				byte[] buf = new byte[20];
				int count = this.serialPort_.readBytes(buf, 0, 10);
				String result = new String(buf, 0, count).trim();
				
				int[] intValues = elmUtil.convertELMResonseToIntArray(result);
				for (int index = 0; index < intValues.length; index++)
				{
					System.out.print("(" + String.format("%02x", intValues[index]) + ") ");
				}
				
				System.out.println("NO Mas");
				
				

			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			
			/* run the super onStart() */
			super.onStart();
		}
		catch(Exception e)
		{
			//e.printStackTrace();

			//ErrorLog.error("TEST", (Exception)e.getCause());
			//ErrorDialog.showError(e.getMessage(), e);
			exit(1);
		}
	}
	
	/*******************************************************
	 * Override
	 * @see waba.ui.MainWindow#onExit()
	 *******************************************************/
	public void onExit()
	{
		this.serialPort_.close();
		super.onExit();
	}
	
	
	
	
}

