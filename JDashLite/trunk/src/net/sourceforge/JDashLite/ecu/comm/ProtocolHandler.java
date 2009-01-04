/*********************************************************
 * 
 * @author spowell
 * ProtocolHandler.java
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

package net.sourceforge.JDashLite.ecu.comm;

import net.sourceforge.JDashLite.Cleanable;
import net.sourceforge.JDashLite.config.ListItem;
import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
import net.sourceforge.JDashLite.ecu.comm.SSM.SSMProtocol;
import waba.io.SerialPort;

/*********************************************************
 * A protocol handler must provide this interface
 * to handle events within JDashLite.  A protocol handler is
 * an instance of a Thread object.  The thread interfaces
 * started() and stopped() methods are used to connect
 * and initialize the serial interface, and also to close it.
 *
 *********************************************************/
public interface ProtocolHandler extends Cleanable
{
	
	public static final String E_RPM = "RPM";
	public static final String E_MPH = "MPH";
	public static final String E_KPH = "KPH"; 

	
	/** The list of all known protocol handlers. Each List Item contains a protocol class name, and
	 * the human readable name.  The class name is used through reflection to instanciate it */
	public static final ListItem[] PROTOCOL_LIST = new ListItem[]
	{
		new ListItem(ELMProtocol.class.getName(),	"ELM OBD2"),
		new ListItem(SSMProtocol.class.getName(),	"Subaru SSM")
	};

	
//	/*******************************************************
//	 * @param profile
//	 ********************************************************/
//	public void setProfile(Profile profile);
	
	/*******************************************************
	 * Start this protocol handler.  Pass in the serial port 
	 * ID to connect to.
	 * @param port IN - the port to connect to.
	 * @see SerialPort
	 ********************************************************/
	public void setSerialPortId(int port);

	
	/********************************************************
	 * Called in order to initiate and initialize the connection.
	 * But.. DON'T actually make the connection here.  Instead, 
	 * do it in a few of the opening calls to doTask();
	 * You can and should however, at least open the serial
	 * port here.
	 * @return
	 ********************************************************/
	public boolean connect();
	
	/*******************************************************
	 * Called to disengage the connection.
	 * @return
	 ********************************************************/
	public boolean disconnect();


	/*******************************************************
	 * This is the meat of a protocol handler.  This method will
	 * get called very regulary by a separate thread.  The thread
	 * will give this object control for the duration of this method,
	 * so make this method quick. I mean QUICK!  Otherwise it will
	 * slow down the whole application.  It's recommended to even
	 * break the processing in this method up into seperate
	 * TX/RX operations.  As in, on one call to doTask() 
	 * perform a TX. Then, wait for the next call to doTask()
	 * for the RX operation.
	 ********************************************************/
	public void doTask();

	/********************************************************
	 * This method will return a list of supported parameters.
	 * Make sure this is a FAST call.  Infact, it should be 
	 * a simple return of a member variable. Dont' computer
	 * the list of paramters each time because this method
	 * will get used in for(;;) loops quite often.
	 * @return
	 ********************************************************/
	public ECUParameter[] getSupportedParameters();
	
		
	/*******************************************************
	 * @param l
	 ********************************************************/
	public void addProtocolEventListener(ProtocolEventListener l);
	
	/*******************************************************
	 * Each protocol handler can provide a list of special 
	 * functions or tools.  This method will return an array
	 * of Strings that represent the tool names.  These names
	 * will be put into the "Tools" menu on the main screen.
	 * The index of each name in the array matches the index value
	 * passed to the executeSpecialTool(int) method.  If a null is
	 * returned, then this handler has no special tools available.
	 * 
	 * @return
	 ********************************************************/
	public String[] getSpecialToolNames();
	
	
	/*******************************************************
	 * Execute the special tool function identified by the
	 * index value.  Returning what ever should be displayed to the
	 * user.
	 * 
	 * @param index
	 * @return
	 ********************************************************/
	public String executeSpecialTool(int index);
	
//	
//	/*******************************************************
//	 * If the protocol handler returns a true here, then the GUI
//	 * will assume the getActiveDTCCodes() and getPendingDTCCodes() 
//	 * methods can be called.  The GUI will enable the ability
//	 * for the user to get DTC codes.
//	 * @return
//	 ********************************************************/
//	public boolean canProvideDTCCodes();
//	
//	
//	/********************************************************
//	 * Return a String array of all currently active DTC codes.
//	 * @return
//	 ******************************************************/
//	public String[] getActiveDTCCodes();
//	
//	
//	/*******************************************************
//	 * Return a String array of all currently pending DTC codes.
//	 * @return
//	 ********************************************************/
//	public String[] getPendingDTCCodes();
//	
//	

}
