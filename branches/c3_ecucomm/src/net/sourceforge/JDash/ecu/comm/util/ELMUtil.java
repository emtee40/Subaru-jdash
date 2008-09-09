/*******************************************************
 * 
 *  @author spowell
 * KWP2000Monitor
 *  Aug 8, 2006
 *  $Id: ELMScanMonitor.java,v 1.4 2006/12/31 16:59:08 shaneapowell Exp $
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
package net.sourceforge.JDash.ecu.comm.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.special.InternalParam;
import net.sourceforge.JDash.util.ByteUtil;



/******************************************************
 * Since the ELM monitor is used in both the main JDash
 * app, and the SuperWaba version.  I've broken out some
 * of the most common stuff that can be used by both.
 * This class MUST contain code that is compatible 
 * with the SuperWaba library.
 *****************************************************/
public class ELMUtil
{
	
	public static final String AT_OK = "OK";
	public static final String ERROR_BUS_BUSY = "BUS BUSY";
	public static final String ERROR_FB_ERROR = "FB ERROR";
	public static final String ERROR_DATA_ERROR = "DATA ERROR";
	public static final String ERROR_NO_DATA = "NO DATA";
	public static final String ERROR_UNKNOWN = "?";
	
	public static final int MODE_1 = 1;
	public static final int MODE_2 = 2;
	public static final int MODE_3 = 3;
	public static final int MODE_4 = 4;
	public static final int MODE_5 = 5;
	public static final int MODE_6 = 6;
	public static final int MODE_7 = 7;
	public static final int MODE_8 = 8;
	public static final int MODE_9 = 9;
	
	public static final int REQUEST_PID_BYTE = 0;
	public static final int RESPONSE_MODE_BYTE = 0;
	public static final int RESPONSE_PID_BYTE = 1;
	
	public static final int MODE_RESPONSE = 40;
		
	public static final int DEFAULT_ELM_BAUD = 9600;
	public static final int MAX_READ_BUFFER = 512;
	
	
	/*******************************************************
	 * Create a new SSP Monitor
	 * @throws ParameterException
	 ******************************************************/
	public ELMUtil()
	{
	}

	

	
	/*******************************************************
	 * Get the current DTC Codes.
	 * @param dtcCodes IN - this array of InternalParams will
	 * 	populated with the DTC Codes, in order from 0->length
	 *******************************************************/
	public static void getDTCCodes(InternalParam[] dtcCodes) throws RuntimeException
	{
		try
		{
			
			/* Request the CEL codes */
			String celResponse = sendELMString("0" + MODE_3);
			System.out.println("Response to CEL code request is [" + celResponse + "]");
			
			/* Convert to an array of Strings */
			StringTokenizer strTok = new StringTokenizer(celResponse, " ");
			ArrayList dtcVector = new ArrayList();
			while (strTok.hasMoreTokens())
			{
				dtcVector.add(strTok.nextToken().toUpperCase());
			}
			String[] responseValues = (String[])dtcVector.toArray(new String[0]);
			
			
			/* We should get a 43 back */
			if (responseValues[0].equalsIgnoreCase((MODE_RESPONSE + MODE_3)+"") == false)
			{
				throw new Exception("Response from ELM did not start with the expected value of " + (MODE_RESPONSE + MODE_3) + " it was " + responseValues[0]);
			}
			
			
			/* Convert each pair of dtc values into a single DTC code */
			int dtcIndex = 0;
			int responseIndex = 1;
			while ((responseIndex < responseValues.length) && (dtcIndex < dtcCodes.length))
			{
				
				/* If the values are both 00 00 , then this is NOT a DTC */
				if ("00".equals(responseValues[responseIndex]) && "00".equals(responseValues[responseIndex+1]))
				{
					responseIndex += 2;
					continue;
				}
				
				
				/* Given the first character of the DTC, convert it to the correct OBD prefix */
				switch(responseValues[responseIndex].charAt(0))
				{
					case '0':
						responseValues[responseIndex] = "P0" + responseValues[responseIndex].charAt(1);
					break;
					
					case '1':
						responseValues[responseIndex] = "P1" + responseValues[responseIndex].charAt(1);	
					break;
						
					case '2':
						responseValues[responseIndex] = "P2" + responseValues[responseIndex].charAt(1);
					break;
						
					case '3':
						responseValues[responseIndex] = "P3" + responseValues[responseIndex].charAt(1);
					break;
						
					case '4':
						responseValues[responseIndex] = "C0" + responseValues[responseIndex].charAt(1);
					break;
						
					case '5':
						responseValues[responseIndex] = "C1" + responseValues[responseIndex].charAt(1);
					break;
						
					case '6':
						responseValues[responseIndex] = "C2" + responseValues[responseIndex].charAt(1);
					break;
						
					case '7':
						responseValues[responseIndex] = "C3" + responseValues[responseIndex].charAt(1);
					break;
						
					case '8':
						responseValues[responseIndex] = "B0" + responseValues[responseIndex].charAt(1);
					break;
						
					case '9':
						responseValues[responseIndex] = "B1" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'A':
						responseValues[responseIndex] = "B2" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'B':
						responseValues[responseIndex] = "B3" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'C':
						responseValues[responseIndex] = "U0" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'D':
						responseValues[responseIndex] = "U1" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'E':
						responseValues[responseIndex] = "U2" + responseValues[responseIndex].charAt(1);
					break;
						
					case 'F':
						responseValues[responseIndex] = "U3" + responseValues[responseIndex].charAt(1);
					break;
					
					default:
						throw new RuntimeException("The first char of CEL response for code indexed at: " + responseIndex + " was not an expected value");
					
				} /* end switch */

				
				/* Add the DTC to the array of now converted DTC Strings */
				dtcCodes[dtcIndex].setValue(responseValues[responseIndex] + responseValues[responseIndex+1]);
				dtcIndex++;
				
				/* Move down the response array */
				responseIndex+=2;
				
				
			} /* end while */

			
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	/*******************************************************
	 * Take the given param value, and get it's value from the
	 * ELM module. Then distribute the value into the ECUParameter
	 * object.
	 * 
	 * @param param
	 * @throws Exception
	 *******************************************************/
	public static void retrieveMode1ParamValue(ECUParameter param) throws Exception
	{
		/* Put together the command request.  byte 0 is the mode, byte 1 is the PID */
		String command = String.format("%02d%02x", MODE_1, param.getAddress()[REQUEST_PID_BYTE]);
		command = command.toUpperCase();
		
		/* Send the command, and retreive the response */
		String result = sendELMString(command);
		
		int[] responseValues = convertELMResonseToIntArray(result);
		
		
		/* Bytes 0 and 1 should be the 40 + the mode and the PID. eg.  Mode 01 PID 06 will return [41 06] */
		if ((responseValues[RESPONSE_MODE_BYTE] != MODE_RESPONSE + MODE_1) &&
			(responseValues[RESPONSE_PID_BYTE] != param.getAddress()[REQUEST_PID_BYTE]))
		{
			throw new Exception("Response from ELM did not start with the expected byte pair [" + 
						MODE_RESPONSE + MODE_1 + " " + param.getAddress()[REQUEST_PID_BYTE] + "]" + 
						" it responded with [" + responseValues[RESPONSE_MODE_BYTE] + " " + responseValues[RESPONSE_PID_BYTE] + "]");
		}
			
		
		/* The remaining bytes represent the data */
		long resultValue = 0;
		for (int index = 2; index < responseValues.length; index++)
		{
			/* Shift the value left one byte */
			resultValue = resultValue << Byte.SIZE; 
			
			/* Add the next byte */
			resultValue += responseValues[index];
		}
		

		/* Put the double value into the ecu parameter */
		param.setResult((double)resultValue);
		
	}
	
	
	
	/*******************************************************
	 * Convert the space delimited string from a series of hex values to 
	 * an integer array.  WE do this the hard way, to ensure that this
	 * method is compatible with both the sun JDK and the SuperWaba library.
	 * 
	 * @param response IN - the array of bytes that was returned.
	 * @param asHex IN - convert values as HEX if true, as INT if false.
	 * @throws Exception When the response was NOT a byte[] of data.  It
	 * could have been one of the error string.s
	 *******************************************************/
	public static int[] convertELMResonseToIntArray(String response) throws Exception
	{
		
		response = response.trim().toUpperCase();
		
		int cleanByteCount = 0;
		char[] dirtyResponse = response.toCharArray();
		char[] cleanResponse = new char[dirtyResponse.length];
		
		/* Check for non-numeric response codes along the way */
		for (int index = 0; index < dirtyResponse.length; index++)
		{
			/* If it's a number, then add it */
			if (((dirtyResponse[index] >= '0') && (dirtyResponse[index] <= '9')) ||
				((dirtyResponse[index] >= 'A') && (dirtyResponse[index] <= 'F')))
			{
				cleanResponse[cleanByteCount++] = dirtyResponse[index];
			}
			
		}

		/* We'll put the resuting int values in here */
		int[] responseValues = new int[cleanByteCount];
		
		/* Process every pair of bytes */
		for (int index = 0; index < cleanByteCount; index+=2)
		{
			char c1 = cleanResponse[index];
			char c2 = cleanResponse[index+1];
			
			int i = 0;

			/* Byte 1 */
			if (c1 >= 'A')
			{
				i = ((int)cleanResponse[index]) - ((int)'A');
				i += 10;
			}
			else
			{
				i = ((int)cleanResponse[index]) - ((int)'0');
			}
			
			/* Shift left 4 bits (1 word) */
			i = i << 4;
			
			/* Byte 2 */
			if (c2 >= 'A')
			{
				i += (int)cleanResponse[index+1] - ((int)'A');
				i += 10;
			}
			else
			{
				i += (int)cleanResponse[index+1] - ((int)'0');
			}
			
		}
		
		return responseValues;
	}
	

	/*******************************************************
	 * This simple utility method will send an ELM based command
	 * to the ELM module, and return it's response. The \r character
	 * will automatically be appened to your comand string, if you have
	 * not included it.
	 * 
	 * @param cmd
	 * @return ELM string response
	 *******************************************************/
	public static String sendELMString(String cmd) throws Exception
	{
			
//			int responseCount = 0;
//			char[] buffer = new char[MAX_READ_BUFFER];
//			String bufferLine = null;
			String response = "";
//			
//	
//			/* Trim any white space */
//			cmd = cmd.trim();
//			
//			/* Add the ELM CR / Execute character */
//			if (cmd.endsWith("\r") == false)
//			{
//				cmd += "\r";
//			}
//			
//			
//			/* Write the command */
//			this.writer_.write(cmd);
//			
//			/* If using the emulator,then catch and ignore the resulting excepiton about the drain. 
//			 * it has NO impact on how things operate */
//			if(USING_ECUEMULATOR_INTERFACE)
//			{
//				try
//				{
//					this.writer_.flush();
//				}
//				catch(Exception e)
//				{ /* do nothing */ }
//			}
//			else
//			{
//				this.writer_.flush();
//			}
//			
//			/* Read the lines until we get to a character that breaks the readline loop */
//			while (true)
//			{
//				
//				/* Read a chunk of response */
//				responseCount = this.reader_.read(buffer);
//				bufferLine = new String(buffer, 0, responseCount);
//				response += bufferLine;
//				
//				
//				/* Timed out */
//				if (bufferLine.startsWith("-1"))
//				{
//					throw new Exception("Read from ELM timed out waiting for response to command [" + cmd + "].\nThis is was has been received so far\n[" + response + "]");
//				}
//				
//				
//				/* Normal ">" prompt, means the command was processed, and the response received */
//				if (bufferLine.indexOf(">") != -1)
//				{
//					break;
//				}
//				
//				
//			}
//			
//			/* strip the > and spaces and all other extra characters we don't need */
//			response = response.replace('>', ' ');
//			response = response.replace('\n', ' ');
//			response = response.replace('\r', ' ');
//			response = response.trim();
//	
//			
//			/* ELM Didn't understand command, or was not ready */
//			if (response.indexOf(ERROR_UNKNOWN) != -1)
//			{
//				throw new Exception("\n" + response + "\nELM module did not understand our command.  cmd[" + cmd + "]");
//			}
//		
//			
//			if (response.indexOf(ERROR_BUS_BUSY) != -1)
//			{
//				throw new Exception("\n" + response + "\nELM module responded with bus busy error.  cmd[" + cmd + "]");
//			}
//			
//			if (response.indexOf(ERROR_FB_ERROR) != -1)
//			{
//				throw new Exception("\n" + response + "\nELM Feedback Error.  cmd[" + cmd + "]");
//			}
//					
//					
//			if (response.indexOf(ERROR_DATA_ERROR) != -1)
//			{
//				throw new Exception("\n" + response + "\nELM Module data error.  cmd[" + cmd + "]");
//			}
//						
//			if (response.indexOf(ERROR_NO_DATA) != -1)
//			{
//				throw new Exception("\n" + response + "\nELM did not recieve any data.  cmd[" + cmd + "]");
//			}
//			
			
			
			/* Return the ELMs response */
			return response;
			
	}
	
	
	 /*******************************************************
	  * 
	 *******************************************************/
	public static void resetDTCs() throws RuntimeException
	{
		try
		{
			String buffer = sendELMString("0" + MODE_4);
			
			if ("44".equals(buffer) == false)
			{
				throw new Exception("There was an error resetting the ECU\nELM Module Response was:\n[" + buffer + "]");
			}
			
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
}
