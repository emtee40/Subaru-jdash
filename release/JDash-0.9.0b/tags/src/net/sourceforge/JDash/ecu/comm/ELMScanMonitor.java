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
package net.sourceforge.JDash.ecu.comm;

import gnu.io.RXTXPort;

import net.sourceforge.JDash.ecu.param.DTCMetaParam;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterEventListener;
import net.sourceforge.JDash.ecu.param.ParameterException;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/******************************************************
 * If you have an ELMScan module from ScanTool.net, then this
 * is the monitor for you.
 * 
 * <pre>
 *  http://en.wikipedia.org/wiki/OBD-II_PIDs
 * </pre>
 *****************************************************/
public class ELMScanMonitor extends RS232Monitor
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
	private static final int MAX_READ_BUFFER = 512;
	
	private String ecuId_ = "";
    
	/* Create readers and writers */
	private BufferedWriter writer_ = null;
	private BufferedReader reader_ = null;
	
	private Integer semaphore_ = new Integer(0);

	
	/*******************************************************
	 * Create a new SSP Monitor
	 * @throws ParameterException
	 ******************************************************/
	public ELMScanMonitor() throws Exception
	{
		super(DEFAULT_ELM_BAUD, RXTXPort.DATABITS_8, RXTXPort.PARITY_NONE, RXTXPort.STOPBITS_1);
		
		this.writer_ = new BufferedWriter(new OutputStreamWriter(getPort().getOutputStream()));
		this.reader_ = new BufferedReader(new InputStreamReader(getPort().getInputStream()));
	}

	
	

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#init(net.sourceforge.JDash.ecu.param.ParameterRegistry)
	 *******************************************************/
	public List<Parameter> init(ParameterRegistry reg, InitListener initListener) throws Exception
	{
		
		
		super.init(reg, initListener);
		
		String buffer = null;

		
		/* Have this monitor be an observer to the MIL_STATUS parameter.. if there even is one */
		Parameter milStatus = reg.getParamForName(ParameterRegistry.PARAM_NAME_MIL_STATUS);
		if (milStatus != null)
		{
			milStatus.addEventListener(new ParameterEventListener()
			{
				public void valueChanged(Parameter p)
				{
					doMilCheck();
				}
			});
		}
		
		getPort().setRTS(false);
		
		/* Perform a complete ELM reset */
		initListener.update("Reset ELM", 1, 5);
		buffer = sendELMString("ATZ");
		this.ecuId_ += buffer;

		
		/* Disable Echo */
		initListener.update("Disable Echo", 2, 5);
		buffer = sendELMString("ATE0");
		if (buffer.indexOf(AT_OK) == -1)
		{
			throw new Exception("Unable to set the ELM modules echo to off [ATE0].  Response was [" + buffer + "]");
		}
		
				
		/* Turn of the LineFeed */
		initListener.update("Set Command", 3, 5);
		buffer = sendELMString("ATL0");
		if (buffer.indexOf(AT_OK) == -1)
		{
			throw new Exception("Unable to set the ELM modules LF to off [ATL0].  Response was [" + buffer + "]");
		}
		
		/* Ask for the VID, this will also result in an INIT response. Then we'll just add it to the ecuID output string */
		initListener.update("Fetch VID", 4, 5);
		buffer = sendELMString("09 01");
		this.ecuId_ += "\nVID: " + buffer;

		
		/* attempt to set the fastes ELM timeout value */
		initListener.update("Setting best speed", 5, 5);
		setBestTimeout();
		
		
		/* Return the init results */
		return new ArrayList<Parameter>();
	}

	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.ECUMonitor#getEcuInfo()
	 *******************************************************/
	public String getEcuInfo() throws Exception
	{
		return "ELM Monitor:\n" + this.ecuId_;
	}


	/*********************************************************
	 * The default timeout for an ELM module is 200ms. This is not ideal.  Infact, it
	 * limits the ELM to about 2.5 values per second. We want to make this as fast as possible, so
	 * this method will try to set the timeout to the fastest of a set of timeout values.  The
	 * fastest one that doesn't return errors will be used.
	 * 
	 * @throws Exception
	 *******************************************************/
	private void setBestTimeout() throws Exception
	{
		String buffer = "";
		String[] timeoutValues = {	"0A",  /* 0A = 10 = 40ms */
									"14",  /* 14 = 20 = 80ms */
									"1E",  /* 1E = 30 = 120ms */
									"28",  /* 28 = 40 = 160ms */
									"32"}; /* 32 = 50 = 200ms (elm default_ */
		
		
		/* Try each speed, starting from the fastest */
		for (String timeoutSpeed : timeoutValues)
		{
		
			try
			{
				/* Set the timeout value */
				buffer = sendELMString("ATST" + timeoutSpeed);
				if (buffer.indexOf(AT_OK) == -1)
				{
					throw new Exception("Unable to set the ELM Timeout (ATST) value to " + timeoutSpeed + " Response was [" + buffer + "]");
				}
				
				
				/* Attempt to send 3 commands, if any of them return an error, then we'll assume the speed
				 * is too fast. We'll use the first PID block, since it's a 4 byte response. */
				buffer = sendELMString("01 00");
				buffer = sendELMString("01 00");
				buffer = sendELMString("01 00");
				
				
				/* If we got past all 3 without an error, then lets assume the speed is good */
				System.out.println("Timing SEt to: " + timeoutSpeed);
				return;
				
				
			}
			catch (Exception e)
			{
				/* An exception means that the set speed is not a good choice.  Try again */
				System.out.println("Timeout Speed: " + timeoutSpeed + " caused an error.  Trying next speed.\n" + e.getMessage());
				continue;
			}
			
		}
		
		
		/* If we got here, then NONE of the speeds worked??!! */
		throw new Exception("Unable to set the timeout speed to any reliable value.");
		

	}
	
	/******************************************************
	 * Kick off communications with the ECU.
	 * Override
	 * @see java.lang.Runnable#run()
	 ******************************************************/
	public void run()
	{
		
		int packetFailureCount = 0;
		
		try
		{
			
			while(doRun_.booleanValue())
			{
				
				
                /* define the list of parameters that will be fetched.  This is based on if the
                 * packets rate has it ready for an update or not. */
                List<ECUParameter> thisFetchList = new ArrayList<ECUParameter>();
                for (ECUParameter param : this.params_)
                {
                	if (param.getLastFetchTime() + param.getPreferedRate() < System.currentTimeMillis())
                	{
                		param.setLastFetchTime(System.currentTimeMillis());
                		thisFetchList.add(param);
                	}
                }

                
                try
                {
                	
                	fireProcessingStartedEvent();
					
					//for (int index = 0; index < thisFetchList.size(); index++)
					for (ECUParameter param : thisFetchList)
                    {
						retreiveMode1ParamValue(param);
                    }
					
                    /* Once all packets in this run are sent and received, mark the time */
                    fireProcessingFinishedEvent();
                    
                    /* Reset the packet failure count */
                    packetFailureCount = 0;

                }
                catch(Exception e)
                {
                	packetFailureCount++;  

                	if (packetFailureCount >= MAX_PACKET_FAILURES)
                	{
                		stop();
                		throw new RuntimeException("There was a problem with the ELM Request.  Too many failures in a row: " + packetFailureCount);
                	}
                	
                	
                	/* Log any failed packets */
                	e.printStackTrace();
                }

                
                
			} /* end while loop */
			    
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
        	{
        		closePort();
        	}
        	catch(Exception e)
        	{
        	}
		}
	}


	
	/*******************************************************
	 * Check if the MIL is on, and retreive any CEL codes too.
	 *******************************************************/
	private void doMilCheck() throws RuntimeException
	{

		try
		{
			
			/* This is where the current list of codes will be put */
			ArrayList<String> dtcCodes = null;
			
			/* Get the MIL status parameter */
			Parameter milStatus = getParameterRegistry().getParamForName(ParameterRegistry.PARAM_NAME_MIL_STATUS);
			
			System.out.println("MIL Status value: " + milStatus.getResult());
				
			/* If the MIL light is on, then populate the DTC_* parameters with the correct DTC codes */
			if (milStatus.getResult() > 0)
			{
				
				/* Request the CEL codes */
				String celResponse = sendELMString("0" + MODE_3);
//				System.out.println("Response to CEL code request is [" + celResponse + "]");
				
				/* Convert to an array of Strings */
				StringTokenizer strTok = new StringTokenizer(celResponse, " ");
				String[] responseValues = new String[strTok.countTokens()];
				for (int index = 0; index < responseValues.length; index++)
				{
					responseValues[index] = strTok.nextToken().toUpperCase();
				}
				
				
				/* We should get a 43 back */
				if (responseValues[0].equalsIgnoreCase("43") == false)
				{
					throw new Exception("Response from ELM did not start with the expected value of " + (MODE_RESPONSE + MODE_3) + " it was " + responseValues[0]);
				}
				
				
				/* Convert each pair of dtc values into a single DTC code */
				dtcCodes = new ArrayList<String>();
				int responseIndex = 1;
				while (responseIndex < responseValues.length)
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
					dtcCodes.add(responseValues[responseIndex] + responseValues[responseIndex+1]);
					
					/* Move down the response array */
					responseIndex+=2;
					
				} /* end while */

				
			} /* end if milStatus == 1 */


				
			/* Apply the list of DTCs to the DTC Parameters, null out the remaining parameters */
			int dtcIndex = 0;
			for(DTCMetaParam dtcParam : getParameterRegistry().getDTCParameters()) 
			{
				
				/* Set the DTC value within the DTC Parameter to our calculated DTC value.  If the list
				 * is null, then the MIL lamp must be off.. so.. reset all DTC parameters */
				if ((dtcCodes != null) && (dtcIndex < dtcCodes.size()))
				{
					dtcParam.setDTCCode(dtcCodes.get(dtcIndex));
				}
				else
				{
					/* Null out the rest of the DTC parametres */
					dtcParam.setDTCCode(null);
				}
				
				/* Next code */
				dtcIndex++;
				
			} /* end for */
				
			
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
	private void retreiveMode1ParamValue(ECUParameter param) throws Exception
	{
		/* Put together the command request.  byte 0 is the mode, byte 1 is the PID */
		String command = String.format("%02d %02x", MODE_1, param.getAddress()[REQUEST_PID_BYTE]);
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
		
		fireProcessingParameterEvent(param);
		
		
	}
	
	
	
	/*******************************************************
	 * Convert the space delimited string from a series of hex values to 
	 * an integer array.
	 * @param response
	 * @param asHex IN - convert values as HEX if true, as INT if false.
	 *******************************************************/
	private int[] convertELMResonseToIntArray(String response)
	{
		
		/* We'll need to put the individual bytes but into a int array to avoid signed problems */
		StringTokenizer strTok = new StringTokenizer(response, " ");
		int[] responseBytes = new int[strTok.countTokens()];
		for (int index = 0; index < responseBytes.length; index++)
		{
			responseBytes[index] = Integer.decode("0x" + strTok.nextToken());
		}
		
		return responseBytes;
	}
	

	/*******************************************************
	 * This simple utility method will send an ELM based command
	 * to the ELM module, and return it's response. The \r character
	 * will automatically be appened to your comand string, if you have
	 * not included it.
	 * 
	 * @param cmd
	 * @return
	 *******************************************************/
	private String sendELMString(String cmd) throws Exception
	{
		synchronized(this.semaphore_)
		{
			
			int responseCount = 0;
			char[] buffer = new char[MAX_READ_BUFFER];
			String bufferLine = null;
			String response = "";
			
	
			/* Trim any white space */
			cmd = cmd.trim();
			
			/* Add the ELM CR / Execute character */
			if (cmd.endsWith("\r") == false)
			{
				cmd += "\r";
			}
			
			
			/* Write the command */
			this.writer_.write(cmd);
			this.writer_.flush();
			
			/* Read the lines until we get to a character that breaks the readline loop */
			while (true)
			{
				
				/* Read a chunk of response */
				responseCount = this.reader_.read(buffer);
				bufferLine = new String(buffer, 0, responseCount);
				response += bufferLine;
				
				
				/* Timed out */
				if (bufferLine.startsWith("-1"))
				{
					throw new Exception("Read from ELM timed out waiting for response to command [" + cmd + "].\nThis is was has been received so far\n[" + response + "]");
				}
				
				
				/* Normal ">" prompt, means the command was processed, and the response received */
				if (bufferLine.indexOf(">") != -1)
				{
					break;
				}
				
				
			}
			
			/* strip the > and spaces and all other extra characters we don't need */
			response = response.replace('>', ' ');
			response = response.replace('\n', ' ');
			response = response.replace('\r', ' ');
			response = response.trim();
	
			
			/* ELM Didn't understand command, or was not ready */
			if (response.indexOf(ERROR_UNKNOWN) != -1)
			{
				throw new Exception("\n" + response + "\nELM module did not understand our command.  cmd[" + cmd + "]");
			}
		
			
			if (response.indexOf(ERROR_BUS_BUSY) != -1)
			{
				throw new Exception("\n" + response + "\nELM module responded with bus busy error.  cmd[" + cmd + "]");
			}
			
			if (response.indexOf(ERROR_FB_ERROR) != -1)
			{
				throw new Exception("\n" + response + "\nELM Feedback Error.  cmd[" + cmd + "]");
			}
					
					
			if (response.indexOf(ERROR_DATA_ERROR) != -1)
			{
				throw new Exception("\n" + response + "\nELM Module data error.  cmd[" + cmd + "]");
			}
						
			if (response.indexOf(ERROR_NO_DATA) != -1)
			{
				throw new Exception("\n" + response + "\nELM did not recieve any data.  cmd[" + cmd + "]");
			}
			
			
			
			/* Return the ELMs response */
			return response;
			
		}
		
	}
	
	
	 /*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.ecu.comm.BaseMonitor#resetDTCs()
	 *******************************************************/
	@Override
	public void resetDTCs() throws RuntimeException
	{
		try
		{
			String buffer = sendELMString("0" + MODE_4);
			
			if ("44".equals(buffer) == false)
			{
				throw new Exception("There was an error resetting the ECU\nELM Module Response was:\n[" + buffer + "]");
			}
			
			/* Get the low level MIL status parameter, and set it's last fetch time to 0, this will cause it
			 * to be re-fetched */
			Parameter p = getParameterRegistry().getParamForName(ParameterRegistry.PARAM_NAME_E_MIL_STATUS);
			if (p instanceof ECUParameter)
			{
				((ECUParameter)p).setResult(0);
				((ECUParameter)p).setLastFetchTime(0);
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
}
