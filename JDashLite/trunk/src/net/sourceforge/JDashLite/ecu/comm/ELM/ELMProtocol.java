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

import net.sourceforge.JDashLite.ecu.comm.AbstractProtocol;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.InvalidStageModeException;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import waba.io.SerialPort;
import waba.sys.Convert;


/*********************************************************
 *
 
 <pre>
 
 >ATZ
 y
 ELM327 v1.2a
 
 >ATE0
 OK
 
 >
 
 </pre>

// TODO
 - 2-3 retries in PID mode
 - 2-3 re-connect tries for init mode
 - tweak status messages to indicated 1-3 attempts
 - Move Serial POrt into profile, and use in here.
  
 *
 *********************************************************/
public class ELMProtocol extends AbstractProtocol
{

	private static final int STAGE_STOP				= 0;
	private static final int STAGE_OPEN_PORT		= 10;
	private static final int STAGE_RESET 			= 20;
	private static final int STAGE_2ND_RESET 		= 25;
	private static final int STAGE_ECHO_OFF 		= 30;
	private static final int STAGE_LINEFEED_OFF		= 40;
	private static final int STAGE_GET_PIDS 		= 50;
	private static final int STAGE_PID_REQ			= 60;
	
	private static final int MODE_READY			= 0;
	private static final int MODE_TX			= 1;
	private static final int MODE_RX			= 2;
	
	
	private static final int COM_READ_TIME_IN_MS = 25;
	private static final int INIT_TIMEOUT_MS = 10000;
	private static final int READ_WRITE_TIMEOUT_MS = 2000;
	
	public static final String ELM_NEWLINE = "\r";
	
	public static final String AT_CMD_RESET 		= "ATZ" + ELM_NEWLINE;
	public static final String AT_CMD_ECHO_OFF		= "ATE0" + ELM_NEWLINE;
	public static final String AT_CMD_LINEFEED_OFF	= "ATL0" + ELM_NEWLINE;
	
	public static final String AT_RESPONSE_OK = "OK";
	public static final String AT_RESPONSE_ELM = "ELM";
	public static final String CMD_RESPONSE_READY = ">";
	
	public static final String ERROR_BUS_BUSY = "BUS BUSY";
	public static final String ERROR_FB_ERROR = "FB ERROR";
	public static final String ERROR_DATA_ERROR = "DATA ERROR";
	public static final String ERROR_NO_DATA = "NO DATA";
	public static final String ERROR_UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	public static final String ERROR_UNKNOWN = "?";
	
	public static final String[] KNOWN_ERRORS = {ERROR_BUS_BUSY, ERROR_DATA_ERROR, ERROR_FB_ERROR, ERROR_NO_DATA, ERROR_UNKNOWN, ERROR_UNABLE_TO_CONNECT};

	public static final int ELM_BAUD = 9600;
	public static final int ELM_DATA_BITS = 8;
	public static final int ELM_PARITY = SerialPort.PARITY_NONE;
	public static final int ELM_STOP_BITS = 1;
	
	/* Used to fetch the available PIDs */
	private ELMParameter PIDParam_ = new AllParameters.PID();
	
	/* The supported parametes list */
	private static final ELMParameter[] SUPPORTD_PARAMS = new ELMParameter[]
	{
		new AllParameters.RPM(),
		new AllParameters.STFT1(),
		new AllParameters.LTFT1(),
		new AllParameters.Coolant(),
		new AllParameters.Load()
	};
	
	
	/* The index of the currently being fetched parameter */
	private int currentParameterIndex_ = 0;
	
	/* The read timeout */
	private int readTimeoutMs_ = INIT_TIMEOUT_MS;
	
	/* The response buffer offset pointer */
	private int responseBufferOffset_ = 0;
	
	/* The reusable array of response bytes.  For max performance */
	private byte[] responseBuffer_ = new byte[512];
	
	
	/********************************************************
	 * 
	 *******************************************************/
	public ELMProtocol()
	{
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#connect()
	 ********************************************************/
	public boolean connect()
	{
		setStage(STAGE_OPEN_PORT);
		setMode(MODE_READY);
		return true;
//		boolean initSuccess = false;
//		
//		/* Open the serial port */
//		setSerialPort(new SerialPort(getSerialPortId(), ELM_BAUD, ELM_DATA_BITS, ELM_PARITY, ELM_STOP_BITS));
//		if (getSerialPort().isOpen() == false)
//		{
//			throw new RuntimeException("Serial Error [" + getSerialPort().lastErrorStr + "]");
//		}
//		
//		/* Tweak the serial port */
//		getSerialPort().setFlowControl(true);
//		getSerialPort().setReadTimeout(COM_READ_TIME_IN_MS);
//		getSerialPort().stopWriteCheckOnTimeout = true;
//		getSerialPort().writeTimeout = HARD_TIMEOUT_MS;
//
//		initSuccess = reInitElmInterface();
//		
//		/* Try again */
//		if (!initSuccess)
//		{
//			ErrorLog.error("Init failed, trying again.");
//			initSuccess = reInitElmInterface();
//		}
//		
//		if (!initSuccess)
//		{
//			ErrorLog.error("Protocol Init Failure");
//			return false;
//		}
//		
//		return true;
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#getSupportedParameters()
	 ********************************************************/
	public ECUParameter[] getSupportedParameters()
	{
		return SUPPORTD_PARAMS;
	}
	

//	/*******************************************************
//	 *  init or re-init the ELM interface. 
//	 *  Return true if the init succeeded, false if it failed.
//	 ********************************************************/
//	private boolean reInitElmInterface()
//	{
//		try
//		{
//			this.readTimeoutMs_ = INIT_TIMEOUT_MS;
//			fireInitStartedEvent();
//			
//			/* Initialize the interface */
//			fireInitStatusEvent("Init.    ");
//			sendELMCommand(AT_CMD_RESET);
//			if (checkBufferForString(AT_RESPONSE_ELM) == false)		
//			{
//				throw new Exception("ELM interface not recognized");
//			}
//
//			/* Yes, twice. for some reason it's necessary alot of the time.  And it doesn't hurt either */
//			fireInitStatusEvent("Init..   ");
//			sendELMCommand(AT_CMD_RESET);
//			if (checkBufferForString(AT_RESPONSE_ELM) == false)		
//			{
//				throw new Exception("ELM interface not recognized");
//			}
//
//			/* Disable Echo */
//			//buffer = fetchString("ATE0");
//			sendELMCommand(AT_CMD_ECHO_OFF);
//			fireInitStatusEvent("Init...  ");
//			if (checkBufferForString(AT_RESPONSE_OK) == false)
//			{
//				throw new Exception("Command Error\n[ATE0][" + responseToString() + "]");
//			}
//			
//			/* Turn of the LineFeed */
//			sendELMCommand(AT_CMD_LINEFEED_OFF);
//			fireInitStatusEvent("Init.... ");
//			if (checkBufferForString(AT_RESPONSE_OK) == false)
//			{
//				throw new Exception("Command Error\n[ATL0][" + responseToString() + "]");
//			}
//			
//			
//			/* Ask for the supported PIDs as an init call.  We need to extend the timeout for a moment */
//			this.readTimeoutMs_ = INIT_TIMEOUT_MS;
//			fireInitStatusEvent("Init.....");
//			PID pids = new PID();
//			if ((sendELMCommand(pids.getFullCommand()) == false) || (checkBufferForErrorString()))
//			{
//				throw new Exception("Error requesting PIDs");
//			}
//			
//			fireInitFinishedEvent();
//			this.readTimeoutMs_ = HARD_TIMEOUT_MS;
//			
//		}
//		catch(Exception e)
//		{
//			ErrorLog.error("Unable to init ELMProtocol", e);
//			return false;
//		}
//		
//		return true;
//	}
//	
	
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#doTask()
	 ********************************************************/
	public void doTask()
	{
		
		try
		{
			/* We only support 3 modes */
			if ((getMode() != MODE_READY) && (getMode() != MODE_TX) && (getMode() != MODE_RX))
			{
				throw new InvalidStageModeException(getStage(), getMode());
			}
			
			/* fire the mode event */
			switch(getMode())
			{
				case MODE_READY:
					fireCommReady();
				break;
				
				case MODE_TX:
					fireCommTXEvent();
				break;
				
				case MODE_RX:
					fireCommRXEvent();
				break;
			}
			
			/* Process the deisred stage */
			switch(getStage())
			{
				case STAGE_STOP:
					setMode(MODE_READY);
				break;
					
				case STAGE_OPEN_PORT:
					doOpenPortTask();
				break;
				
				case STAGE_RESET:
					doResetTask(STAGE_RESET);
				break;
				
				case STAGE_2ND_RESET:
					doResetTask(STAGE_2ND_RESET);
				break;
				
				case STAGE_ECHO_OFF:
					doEchoOffTask();
				break;
				
				case STAGE_LINEFEED_OFF:
					doLineFeedOffTask();
				break;
				
				case STAGE_GET_PIDS:
					doGetPIDTask();
				break;
				
				case STAGE_PID_REQ:
					doParameterFetchTask();
				break;
				
				default:
					ErrorLog.error("Invalid Stage [" + getStage() + "] in toTask()");
				break;
			}
		}
		catch(InvalidStageModeException ie)
		{
			setStageAndMode(STAGE_STOP,MODE_READY);
			ErrorLog.fatal("Invalid Stage/Mode [" + ie.stage_ + "/" + ie.mode_ + "]");
		}
		catch(Exception e)
		{
			setStageAndMode(STAGE_STOP, MODE_READY);
			fireStoppedEvent();
			fireInitStatusEvent(null);
			ErrorLog.fatal("Unexpected Error", e);
			ErrorDialog.showError("Unexpected Error", e);
		}
		
	}
	
	/******************************************************
	 * 
	 ********************************************************/
	private void doOpenPortTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireInitStartedEvent();
				fireInitStatusEvent("Opening Serial Port");
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				
				setSerialPort(new SerialPort(getSerialPortId(), ELM_BAUD, ELM_DATA_BITS, ELM_PARITY, ELM_STOP_BITS));
				if (getSerialPort().isOpen() == false)
				{
					throw new RuntimeException("Serial Error [" + getSerialPort().lastErrorStr + "]");
				}
				
				/* Tweak the serial port */
				getSerialPort().setFlowControl(true);
				getSerialPort().setReadTimeout(COM_READ_TIME_IN_MS);
				getSerialPort().stopWriteCheckOnTimeout = true;
				getSerialPort().writeTimeout = READ_WRITE_TIMEOUT_MS;
			
				/* Next stage */
				setStageAndMode(STAGE_RESET, MODE_READY);
			break;
			
//			case MODE_RX:
//			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
		
		
	}
	
	/*******************************************************
	 * @param stage
	 ********************************************************/
	private void doResetTask(int stage) throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireInitStatusEvent("Resetting ELM" + (stage == STAGE_2ND_RESET?"..":"."));
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				resetReadBuffer();
				sendELMCommand(AT_CMD_RESET);
				setMode(MODE_RX);
				setOperationTimer(INIT_TIMEOUT_MS);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					setStage(getStage()==STAGE_RESET?STAGE_2ND_RESET:STAGE_STOP);
					setMode(MODE_READY);
					fireInitFinishedEvent();
					ErrorLog.info("ELM reset timed out.");
					if (stage == STAGE_2ND_RESET)
					{
						throw new RuntimeException("Init Failure");
					}
				}
				
				/* Look for the ready char */
				if (checkBufferForString(">") == true)
				{
					cleanUnwantedCharsFromBuffer();
					ErrorLog.debug("RX: [" + responseToString() + "]");
					
					if (checkBufferForString(AT_RESPONSE_ELM) == false)
					{
						ErrorLog.warn("ELM Interface Not Recognized");
					}
					
					/* If this is the first reset, then set to do the 2nd reset */
					if (stage == STAGE_RESET)
					{
						setStage(STAGE_2ND_RESET);
					}
					else
					{
						setStage(STAGE_ECHO_OFF);
					}
					
					setMode(MODE_READY);
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
		
	}
	
	/******************************************************
	 * 
	 ********************************************************/
	private void doEchoOffTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireInitStatusEvent("ELM Echo Off");
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				resetReadBuffer();
				sendELMCommand(AT_CMD_ECHO_OFF);
				setMode(MODE_RX);
				setOperationTimer(READ_WRITE_TIMEOUT_MS);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					fireInitFinishedEvent();
					throw new RuntimeException("Init Failure");
				}
				
				/* Look for the ready char */
				if (checkBufferForString(CMD_RESPONSE_READY) == true)
				{
					cleanUnwantedCharsFromBuffer();
					ErrorLog.debug("RX: [" + responseToString() + "]");
					
					if (checkBufferForString(AT_RESPONSE_OK) == false)
					{
						throw new RuntimeException("Command Error\n[" + AT_CMD_ECHO_OFF + "][" + responseToString() + "]");
					}
					
					setStageAndMode(STAGE_LINEFEED_OFF, MODE_READY);
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doLineFeedOffTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireInitStatusEvent("ELM LineFeed Off");
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				resetReadBuffer();
				sendELMCommand(AT_CMD_LINEFEED_OFF);
				setMode(MODE_RX);
				setOperationTimer(READ_WRITE_TIMEOUT_MS);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					fireInitFinishedEvent();
					throw new RuntimeException("Init Failure");
				}
				
				/* Look for the ready char */
				if (checkBufferForString(CMD_RESPONSE_READY) == true)
				{
					cleanUnwantedCharsFromBuffer();
					ErrorLog.debug("RX: [" + responseToString() + "]");
					
					if (checkBufferForString(AT_RESPONSE_OK) == false)
					{
						throw new RuntimeException("Command Error\n[" + AT_CMD_LINEFEED_OFF + "][" + responseToString() + "]");
					}
					
					setStageAndMode(STAGE_GET_PIDS, MODE_READY);
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doGetPIDTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireInitStatusEvent("Querying ECU");
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				resetReadBuffer();
				sendELMCommand(this.PIDParam_.getFullCommand());
				setMode(MODE_RX);
				setOperationTimer(INIT_TIMEOUT_MS);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					fireInitFinishedEvent();
					throw new RuntimeException("Init Failure");
				}
				
				/* Look for the ready char */
				if (checkBufferForString(CMD_RESPONSE_READY) == true)
				{
					cleanUnwantedCharsFromBuffer();
					
					/* Check the buffer for known error strings */
					boolean hasError = checkBufferForErrorString();
					ErrorLog.debug("RX: [" + responseToString() + "]");
					
					if (hasError)
					{
						throw new RuntimeException("Invalid response from ELM");
					}
					
					/* Try to extract the desired values.  An error will cause an attempted reset */
					// TODO Compensate for the SEARCHING... string.  We can do this by looking for the first instance of 4100
//					try
//					{
//						extractResponseBytes(this.PIDParam_);
//					}
//					catch(Exception e2)
//					{
//						/* An exception here is not normal, nor expected.  If it happens, we'll do a re-init */
//						ErrorLog.error("Parameter Fetch Exception, do reset.", e2);
//						setStageAndMode(STAGE_RESET, MODE_READY);
//						return;
//					}
					
					setStageAndMode(STAGE_PID_REQ, MODE_READY);
					fireInitFinishedEvent();
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void doParameterFetchTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				fireBeginParameterBatchEvent(1);
				setMode(MODE_TX);
			break;
			
			case MODE_TX:

				/* Move the index to the next parameter */
				this.currentParameterIndex_++;
				if (this.currentParameterIndex_ >= SUPPORTD_PARAMS.length)
				{
					this.currentParameterIndex_ = 0;
				}
				
				/* If the current param is not enabled, then return to try the next one */
				if (SUPPORTD_PARAMS[this.currentParameterIndex_].isEnabled() == false)
				{
					ErrorLog.debug("Skipping " + SUPPORTD_PARAMS[this.currentParameterIndex_].getName());
					return;
				}
				
				/* Send the elm request */
				resetReadBuffer();
				sendELMCommand(SUPPORTD_PARAMS[this.currentParameterIndex_].getFullCommand());
				
				/* Go to RX mode */
				setMode(MODE_RX);
				setOperationTimer(READ_WRITE_TIMEOUT_MS);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					ErrorLog.error("Timeout Fetching Parameter");
					setStageAndMode(STAGE_RESET, MODE_READY);
					return;
				}
				
				/* Look for the ready char, this means we got something back */
				if (checkBufferForString(CMD_RESPONSE_READY) == true)
				{
					cleanUnwantedCharsFromBuffer();
					
					/* Check the buffer for known error strings */
					boolean hasError = checkBufferForErrorString();
					
					/* Debug out put the RX buffer.  To prevent having to create a new string every time, we'll check the log level */
					if (ErrorLog.LOG_LEVEL_DEBUG.equals(ErrorLog.getCurrentLevel()))
					{
						ErrorLog.debug("RX: [" + responseToString() + "]");
					}
					
					/* If an error is in the string, then attempt a reset */
					if (hasError)
					{
						ErrorLog.error("Unexpected Response");
						setStageAndMode(STAGE_RESET, MODE_READY);
						return;
					}
					
					/* Try to extract the desired values.  An error will cause an attempted reset */
					try
					{
						extractResponseBytes(SUPPORTD_PARAMS[this.currentParameterIndex_]);
					}
					catch(Exception e2)
					{
						/* An exception here is not normal, nor expected.  If it happens, we'll do a re-init */
						ErrorLog.error("Parameter Fetch Exception, do reset.", e2);
						setStageAndMode(STAGE_RESET, MODE_READY);
						return;
					}
					
					setStageAndMode(STAGE_PID_REQ, MODE_READY);
					fireParemeterFetchedEvent(SUPPORTD_PARAMS[this.currentParameterIndex_]);
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
	}
//	
//	/*********************************************************
//	 * (non-Javadoc)
//	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#doTask()
//	 ********************************************************/
//	public void olddoTask()
//	{
//		
//		
//		/* If the serial port is lost, then pause this thread */
//		if (getSerialPort() != null && getSerialPort().isOpen() == false)
//		{
//			return;
//		}
//		
//		
//		/* If the port is open and ready, then go */
//		if (getSerialPort() != null && getSerialPort().isOpen())
//		{
//			
//			fireBeginParameterBatchEvent(1);
//	
//			/* try to do the fetch.  An error will cause a re-init */
//			try
//			{
//				for (int index = 0; index < SUPPORTD_PARAMS.length; index++)
//				{
//		
//					/* Get the parameter */
//					ELMParameter p = SUPPORTD_PARAMS[index];
//					
//					/* Only fetch enabled parameters */
//					if (p.isEnabled() == false)
//					{
//						continue;
//					}
//		
//					
//					/* Process the desired parameter */
//					boolean success = sendELMCommand(p.getFullCommand());
//					
//					if (!success)
//					{
//						throw new Exception("Error Requesting " + p.getName());
//					}
//					
//
//					try
//					{
//						extractResponseBytes(p);
//					}
//					catch(Exception e2)
//					{
//						/* An exception here is not normal, nor expected.  If it happens, we'll do a re-init */
//						ErrorLog.error("Parameter Fetch Exception, do reset.", e2);
//						reInitElmInterface();
//					}
//					
//					
//				}
//			}
//			catch(Exception e)
//			{
//				ErrorLog.error("Getting ECU Value", e);
//	// TODO:  will this work?
//				reInitElmInterface();
//			}
//			
//			fireEndParameterBatchEvent();
//		}
//		
//	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#stop()
	 ********************************************************/
	public void stop()
	{
		if (getSerialPort() != null)
		{
			if (getSerialPort().isOpen())
			{
				getSerialPort().close();
			}
		}
		
	}
	

	/********************************************************
	 * This method, every time it's called, will simply check
	 * if there is any data ready for read.  And if there is, 
	 * copy it into the readBuffer at the current offset location.
	 ********************************************************/
	private void readSeralData()
	{
		int bytesRead = 0;
		
		if (getSerialPort().readCheck() <= 0)
		{
			return;
		}
		
		/* Read the bytes that are ready */
		bytesRead = getSerialPort().readBytes(this.responseBuffer_, this.responseBufferOffset_, getSerialPort().readCheck());
		this.responseBufferOffset_ += Math.max(bytesRead, 0);

	}
	
	
	/*******************************************************
	 * @param cmd
	 ********************************************************/
	private void sendELMCommand(String cmd)
	{
		/* To prevent a whole lot of new String object, pre-check the log level */
		if (ErrorLog.LOG_LEVEL_DEBUG.equals(ErrorLog.getCurrentLevel()))
		{
			ErrorLog.debug("TX: [" + cmd + "]");
		}
		
		if (cmd.endsWith(ELM_NEWLINE) == false)
		{
			cmd = cmd + ELM_NEWLINE;
		}
		
		/* Send the data bytes */
		getSerialPort().writeBytes(cmd.getBytes(), 0, cmd.length());
	}
	
//	/********************************************************
//	 * Send the provided ELM command string, filling the
//	 * response byte[] with the ... result!!  The
//	 * return value from this method is the offset
//	 * value within the response buffer that makes up the 
//	 * response.  eg.. if the return is 11, then bytes 0-11
//	 * in the response buffer are the response bytes.
//	 * 
//	 * @param str
//	 * @return true of the command returned without an exception.  False if there was a fatal error.
//	 ********************************************************/
//	private boolean oldsendELMCommand(String cmd) throws Exception
//	{
//		int hardTimer = Vm.getTimeStamp();
//		int bytesRead = 0;
//		
//		/* reset the response buffer */
//		this.responseBufferOffset_ = 0;
//		
//		if (cmd.endsWith(ELM_NEWLINE) == false)
//		{
//			cmd = cmd + ELM_NEWLINE;
//		}
//	
//		
//		bytesRead = getSerialPort().readBytes(this.responseBuffer_, 0, getSerialPort().readCheck());
////		ErrorLog.info("Clearing Buffer: " + bytesRead + "\n" + responseToString());
//		ErrorLog.debug("TX: [" + cmd + "]");
//
//		
//		/* Send the data bytes */
//		getSerialPort().writeBytes(cmd.getBytes(), 0, cmd.length());
//		
//		/* Read the response */
//		while(true)
//		{
//			/* Watch the hard timer value */
//			if (Vm.getTimeStamp() > (hardTimer + this.readTimeoutMs_))
//			{
//				ErrorLog.info("hard timeout exceeded");
//				return false;
//			}
//			
//			if (getSerialPort().readCheck() <= 0)
//			{
//				Vm.sleep(10);
//				continue;
//			}
//			
//			/* Read the bytes that are ready */
//			bytesRead = getSerialPort().readBytes(this.responseBuffer_, this.responseBufferOffset_, getSerialPort().readCheck());
//			this.responseBufferOffset_ += Math.max(bytesRead, 0);
//
//			/* Look for the ">" character indicating the command has completed */
//			//if (elmResponse.indexOf('>') >= 0)
//			if (checkBufferForString(">") == true)
//			{
//				break;
//			}
//			
//		}
//		
//		/* clear any non-desireable characters */
//		for (int index = 0; index < this.responseBufferOffset_; index++)
//		{
//			boolean clear = false;
//			
//			if (this.responseBuffer_[index] == '\n')
//				clear = true;
//			
//			if (this.responseBuffer_[index] == '\r')
//				clear = true;
//			
//			if (this.responseBuffer_[index] == -1)
//				clear = true;
//				
//			if (this.responseBuffer_[index] == '>')
//				clear = true;
//			
//			if (clear)
//			{
//				this.responseBuffer_[index] = ' ';
//			}
//		}
//		
//		
//		/* Check the buffer for known error strings */
//		boolean hasError = checkBufferForErrorString();
//		
//		ErrorLog.debug("RX: [" + responseToString() + "]");
//			
//		return true;
//	}
//	
	
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void resetReadBuffer()
	{
		this.responseBufferOffset_ = 0;
		for (int index = 0; index < 48; index++)
		{
			this.responseBuffer_[index] = '0';
		}
	}
	
	/*******************************************************
	 * 
	 ********************************************************/
	private void cleanUnwantedCharsFromBuffer()
	{
		/* clear any non-desireable characters */
		for (int index = 0; index < this.responseBufferOffset_; index++)
		{
			boolean clear = false;
			
			if (this.responseBuffer_[index] == '\n')
				clear = true;
			
			if (this.responseBuffer_[index] == '\r')
				clear = true;
			
			if (this.responseBuffer_[index] == -1)
				clear = true;
				
			if (this.responseBuffer_[index] == '>')
				clear = true;
			
			if (clear)
			{
				this.responseBuffer_[index] = ' ';
			}
		}
	}
	
	/*******************************************************
	 * This method will assume the response buffer contains
	 * ELM formatted hex byte codes, with white space.  What will
	 * happen here is the white space will be removed, and the offset pointer
	 * will be adjusted accordingly.
	 ********************************************************/
	private void trimNonHexFromResponseBuffer() throws Exception
	{
		
		/* Starting at the first byte, and walking down to the last byte */
		for (int startOffset = 0; startOffset < this.responseBufferOffset_; startOffset++)
		{
		
			/* Starting at the current start byte, and walking down to the last byte */
			for (int index = startOffset; index < this.responseBufferOffset_; index++)
			{
				byte b = this.responseBuffer_[index];
				
				if (b >= '0' && b <= '9')
				{
					continue;
				}
				else if (b >= 'A' && b <= 'F')
				{
					continue;
				}
				else if (b >= 'a' && b <= 'f')
				{
					this.responseBuffer_[index] = (byte)Convert.toUpperCase((char)b);
					continue;
				}
				else if (b == ' ')
				{
					/* Pull each byte thats to the right, back one spot to the left */
					for (int moveIndex = Math.min(index + 1, this.responseBufferOffset_); moveIndex < this.responseBufferOffset_; moveIndex++)
					{
						this.responseBuffer_[moveIndex - 1] = this.responseBuffer_[moveIndex];
					}
					
					/* Set the new length offset */
					this.responseBufferOffset_--;
					
					/* reprocess this position now */
					index--;
				}
				else
				{
					throw new Exception("Unexpected non HEX character found in [" + new String(this.responseBuffer_, 0, this.responseBufferOffset_) + "]");
				}
				
			}
			
		}
		
		
	}
	
	/********************************************************
	 * Set the response from the ELM module.  The byte[] is basically
	 * a string response.  eg 41 0c oe d8
	 * This method will convert the 4 string byte value pairs into true bytes.
	 * @param length IN - The number of bytes in the response buffer that holds the desired byte string
	 * @param dest IN - the destination integer array.
	 * @return true if the response was processed sucessfully, false if it was not.
	 ********************************************************/
	public void extractResponseBytes(ELMParameter p) throws Exception
	{

		/* Lets clean out the response buffer of any unwanted characters */
		trimNonHexFromResponseBuffer();
		
		
		/* Check the buffer lenght. It should be a 2x the length of the dest array + 4 (2 for the mode and 2 for the command response) */
		if (this.responseBufferOffset_ != ((p.getResponseBytes().length * 2) + 4))
		{
			throw new Exception("Response Not Formatted Correctly\nTX: [" + p.getFullCommand() + "]\nRX: [" + responseToString() + "]\nExpecing " + ((p.getResponseBytes().length * 2) + 4) + " chars");
		}
		
		int rMode = -1;
		int rCmd = -1;
		
		rMode = toOct(this.responseBuffer_[0]) * 16;
		rMode += toOct(this.responseBuffer_[1]);
		rCmd = toOct(this.responseBuffer_[2]) * 16;
		rCmd += toOct(this.responseBuffer_[3]);
		
		/* For each expected byte */
		int offset = 4;
		for (int index = 0; index < p.getResponseBytes().length; index++)
		{
			int v = toOct(this.responseBuffer_[offset]) * 16;
			v += toOct(this.responseBuffer_[offset+1]);
			p.getResponseBytes()[index] = v;
			offset += 2;
		}
		
	}
	
	
	/*******************************************************
	 * Walk the buffer looking for any of the known error strings.
	 * @return
	 ********************************************************/
	private boolean checkBufferForErrorString()
	{
		
		/* Check each error string against the current buffer index */
		for (int errIndex = 0; errIndex < KNOWN_ERRORS.length; errIndex++)
		{
			if (checkBufferForString(KNOWN_ERRORS[errIndex]) == true)
			{
				return true;
			}
			
		}
		
		return false;
	}
	
	
	/********************************************************
	 * Check the buffer for the given string.
	 * 
	 * @param str
	 * @return
	 ********************************************************/
	private boolean checkBufferForString(String str)
	{

		/* Walk down each character in the buffer */
		for (int bufferIndex = 0; bufferIndex < this.responseBufferOffset_; bufferIndex++)
		{
			
			/* If the error string is longer than the remaining space in the buffer, then it's obviously not a match */
			if (str.length() > this.responseBufferOffset_ - bufferIndex)
			{
				continue;
			}
			
			/* Check each character of the error string against the current location in the buffer */
			int charIndex = 0;
			for (charIndex = 0; charIndex < str.length(); charIndex++)
			{
				/* If a character doesn't match, then break this for loop, and continue the KNOWN_ERRORS loop */
				if (str.charAt(charIndex) != this.responseBuffer_[bufferIndex + charIndex])
				{
					charIndex = str.length() + 1;
					continue;
				}
			}
		
			/* If we get to this point, and the charIndex == the length of the error string, then it obviously checked each character, and they all matched */
			if (charIndex == str.length())
			{
				return true;
			}
			
		}
		
		return false;
	}
	
	
	/********************************************************
	 * @return
	 ********************************************************/
	private String responseToString()
	{
		return new String(this.responseBuffer_, 0, this.responseBufferOffset_);
	}
	
	
	/********************************************************
	 *  Convert the character into it's int octal form
	 * @param b
	 * @return
	 ********************************************************/
	private int toOct(byte b) throws Exception
	{
		
		/* convert the character number into the byte value */
		if (b >= '0' && b <= '9')
		{
			return b - 0x30;
		}
		else if (b >= 'A' && b <= 'F')
		{
			return b - 0x41 + 10;
		}
		else if (b >= 'a' && b <= 'f')
		{
			return b - 0x11 + 10;
		}
		else
		{
			throw new Exception("the char [" + b + "] is not a valid octal character");
		}
		
	}
}
