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
import net.sourceforge.JDashLite.ecu.param.DegCelToDegFarMetaParam;
import net.sourceforge.JDashLite.ecu.param.KpaToInHgMetaParam;
import net.sourceforge.JDashLite.ecu.param.KpaToPsiMetaParam;
import net.sourceforge.JDashLite.ecu.param.KphToMphMetaParam;
import net.sourceforge.JDashLite.error.ErrorDialog;
import net.sourceforge.JDashLite.error.ErrorLog;
import waba.io.SerialPort;
import waba.sys.Convert;
import waba.util.Vector;


/*********************************************************
 * This class can request and process the values from an ELM 
 * interface module.
 *
 *********************************************************/
public class ELMProtocol extends AbstractProtocol
{

	private static final int STAGE_STOP				= 0;
	private static final int STAGE_OPEN_PORT		= 10;
	private static final int STAGE_RESET 			= 20;
//	private static final int STAGE_2ND_RESET 		= 25;
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
	private ELMParameter PIDParam_ = new ELMParameter("PID", 1, 0x00, 4)
	{
		public double getValue() { return 0.0; }  
		public String getLabel() { return getName(); };
		public String getDescription() { return getName(); };
	};
	
	/* The supported parametes list */
	private static ECUParameter[] SUPPORTED_PARAMS = null;
	
	/* The index of the currently being fetched parameter */
	private int currentParameterIndex_ = 0;
	
	/* The response buffer offset pointer */
	private int responseBufferOffset_ = 0;
	
	/* The reusable array of response bytes.  For max performance */
	private byte[] responseBuffer_ = new byte[512];
	
	
	/* PID request retry count */
	private int pidReadAttemptCount_ = 0;
	
	
	/** The static initializer that sets up the parameters */
	static
	{
		Vector pList = new Vector(20);

		ELMParameter fuelSystem = new ELMParameter("FuelSystemStatus", 1, 0x03, 2)
		{
			public double getValue() { throw new RuntimeException("Method Not Used"); }
			public String getLabel() { return getName(); }
			public String getDescription() {return "Bit Encoded Fuel System Status. ";}
		};
		pList.addElement(fuelSystem);
		
		pList.addElement(new BitCheckMetaParam("FS0", fuelSystem, 0, 0x80)
		{
			public String getLabel() { return "Fuel System F0"; }
			public String getDescription() {return "Fuel System #1 in Open Loop due to cold engine";}
		});
		
		pList.addElement(new BitCheckMetaParam("FS1", fuelSystem, 0, 0x40)
		{
			public String getLabel() { return "Fuel System F1"; }
			public String getDescription() {return "Fuel System #1 in Closed Loop";}
		});
		
		pList.addElement(new BitCheckMetaParam("FS2", fuelSystem, 0, 0x20)
		{
			public String getLabel() { return "Fuel System F2"; }
			public String getDescription() {return "Fuel System #1 in Open Loop due to engine load";}
		});
	
		pList.addElement(new BitCheckMetaParam("FS3", fuelSystem, 0, 0x10)
		{
			public String getLabel() { return "Fuel System F3"; }
			public String getDescription() {return "Fuel System #1 in Open Loop due to system failure";}
		});
		
		pList.addElement(new BitCheckMetaParam("FS4", fuelSystem, 0, 0x08)
		{
			public String getLabel() { return "Fuel System F4"; }
			public String getDescription() {return "Fuel System #1 in Closed Loop but there is a fault";}
		});
		
		pList.addElement(new ELMParameter("LOAD", 1, 0x04, 1) 		
		{ 
			public double getValue() { return getResponseDouble(0) * 100.0 / 255.0; }  
			public String getLabel() { return "Engine Load"; }
			public String getDescription() {return "The engine load percentage (0-100%) as calculated by the ECU";}
		});

		pList.addElement(new ELMParameter("COOLANT_TEMP_C", 1, 0x05, 1) 		
		{ 
			public double getValue() { return getResponseDouble(0) - 40.0; }  
			public String getLabel() { return "Coolant C"; }
			public String getDescription() {return "The Engine coolant temperature in degrees C (-40 to 215)";}
		});

		pList.addElement(new DegCelToDegFarMetaParam("COOLANT_TEMP_F",(ECUParameter)pList.items[pList.size() - 1])
		{
			public String getLabel() { return "Coolant F"; }
			public String getDescription() {return "The Engine coolant temperature in degrees F (-40 to 420)";}
		});


		pList.addElement(new ELMParameter("STFT1", 1, 0x06, 1) 	
		{
			public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
			public String getLabel() { return "Short Term Fuel Trim 1"; }
			public String getDescription() {return "The Short Term Fuel Trim percentage from 0-100% for Bank 1";}
		});
		
		pList.addElement(new ELMParameter("LTFT1", 1, 0x07, 1) 		
		{ 
			public double getValue() { return 0.7812 *  (getResponseDouble(0) - 128.0); }
			public String getLabel() { return "Long Term Fuel Trim 1"; }
			public String getDescription() {return "The Long Term Fuel Trim percentage from 0-100% for Bank 1";}
		});
		
		pList.addElement(new ELMParameter("MAF", 1, 0x10, 2) 		
		{ 
			public double getValue() { return ((256.0 * getResponseDouble(0)) + getResponseDouble(1)) / 100.0; }
			public String getLabel() { return getName(); }
			public String getDescription() {return "MAF Rate in g/s";}
		});
		
		pList.addElement(new ELMParameter("TPS", 1, 0x11, 2) 		
		{ 
			public double getValue() { return ((256.0 * getResponseDouble(0)) + getResponseDouble(1)) / 100.0; }
			public String getLabel() { return getName(); }
			public String getDescription() {return "Throttle Position Sensor Percentage 0-100%";}
		});

		pList.addElement(new ELMParameter("MAP_kpa", 1, 0x0B, 2)
		{
			public double getValue() { return ((getResponseDouble(0) * 256.0) + getResponseDouble(1)) / 4.0; }  
			public String getLabel() { return "MAP (kPa)"; }
			public String getDescription() {return "Absolute Intake Manifold Pressure in kPa";}
		});

		pList.addElement(new KpaToPsiMetaParam("MAP_psi",(ECUParameter)pList.items[pList.size() - 1])
		{
			public String getLabel() { return "MAP (psi)"; }
			public String getDescription() {return "Absolute Intake Manifold Pressure in psi";}
		});
		
		pList.addElement(new KpaToInHgMetaParam("MAP_inhg",(ECUParameter)pList.items[pList.size() - 2])
		{
			public String getLabel() { return "MAP (Hg)"; }
			public String getDescription() {return "Absolute Intake Manifold Pressure in inches of mercury";}
		});
		
		pList.addElement(new ELMParameter("RPM", 1, 0x0c, 2)
		{
			public double getValue() { return ((getResponseDouble(0) * 256.0) + getResponseDouble(1)) / 4.0; }  
			public String getLabel() { return getName(); }
			public String getDescription() {return "Engine Revolutions Per Minute";}
		});

		pList.addElement(new ELMParameter("KPH", 1, 0x0D, 1)
		{
			public double getValue() { return getResponseDouble(0); }  
			public String getLabel() { return getName(); }
			public String getDescription() {return "Vehicle speed in KPH";}
		});

		pList.addElement(new KphToMphMetaParam("MPH",(ECUParameter)pList.items[pList.size() - 1])
		{
			public String getLabel() { return getName(); }
			public String getDescription() {return "Vehicle speed in MPH";}
		});
		
		pList.addElement(new ELMParameter("INTAKE_TEMP_C", 1, 0x0F, 2)
		{
			public double getValue() { return ((getResponseDouble(0) * 256.0) + getResponseDouble(1)) / 4.0; }  
			public String getLabel() { return "Intake Air Temp C"; }
			public String getDescription() {return "Intake Air Temperature in Degrees C";}
		});

		pList.addElement(new DegCelToDegFarMetaParam("INTAKE_TEMP_F",(ECUParameter)pList.items[pList.size() - 1])
		{
			public String getLabel() { return "Intake Air Temp F"; }
			public String getDescription() {return "Intake Air Temperature in Degrees F";}
		});
	

		/* Populate our array */
		SUPPORTED_PARAMS = new ECUParameter[pList.size()];
		for (int index = 0; index < pList.size(); index++)
		{
			SUPPORTED_PARAMS[index] = (ECUParameter)pList.items[index];
		}
		
		
	}
	
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
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.AbstractProtocol#disconnect()
	 ********************************************************/
	public boolean disconnect()
	{
		ErrorLog.info("DOING ELM DISCONNECT");
		
		/* Read any pending data */
		if (getSerialPort() != null && getSerialPort().isOpen())
		{
			resetReadBuffer();
			readSeralData();
		}
		
		return super.disconnect();
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#getSupportedParameters()
	 ********************************************************/
	public ECUParameter[] getSupportedParameters()
	{
		return SUPPORTED_PARAMS;
	}
	
	
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
					doResetTask();
				break;
				
//				case STAGE_2ND_RESET:
//					doResetTask(STAGE_2ND_RESET);
//				break;
				
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
			
				setMode(MODE_RX);
				
				/* Put a 1 second delay into the read operation */
				setOperationTimer(1000);
			break;

			/* Read any stale data in the serial port buffer */
			case MODE_RX:
				/* We want to wait at least on second after opening the port */
				if (isOperationTimerExpired() == false)
				{
					return;
				}
				
				readSeralData();
				ErrorLog.info("Cleaned out " + this.responseBufferOffset_ + " stale bytes from serial buffer");
				
				/* Next stage */
				setStageAndMode(STAGE_RESET, MODE_READY);
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
		
		
	}
	
	/*******************************************************
	 * @param stage
	 ********************************************************/
	private void doResetTask() throws InvalidStageModeException
	{
		switch(getMode())
		{
			case MODE_READY:
				//fireInitStatusEvent("Resetting ELM" + (stage == STAGE_2ND_RESET?"..":"."));
				fireInitStatusEvent("Resetting ELM");
				setMode(MODE_TX);
			break;
			
			case MODE_TX:
				readSeralData();
				resetReadBuffer();
				sendELMCommand(AT_CMD_RESET);
				setMode(MODE_RX);
				setOperationTimer(INIT_TIMEOUT_MS);
				setMode(MODE_RX);
			break;
			
			case MODE_RX:
				readSeralData();
				
				/* Timeout ? */
				if (isOperationTimerExpired())
				{
					fireInitFinishedEvent();
					ErrorLog.info("ELM reset timed out.");
					throw new RuntimeException("ELM Reset Timed Out");
				}
				
//				setStageAndMode(STAGE_ECHO_OFF, MODE_READY);
//				fireInitFinishedEvent();

				
				/* Look for the ready char */
				if (checkBufferForString(">"))
				{
					cleanUnwantedCharsFromBuffer();
					
					if (checkBufferForString(AT_RESPONSE_ELM) == false)
					{
						ErrorLog.warn("ELM Interface Not Recognized");
						throw new RuntimeException("Init Failure");
					}
					
					setStageAndMode(STAGE_ECHO_OFF, MODE_READY);
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
				readSeralData();
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
				readSeralData();
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
				readSeralData();
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
				setMode(MODE_TX);
			break;
			
			case MODE_TX:

				/* Next param */
				this.currentParameterIndex_ = getNextEnabledParamIndex(this.currentParameterIndex_);
				
				/* If the current index does NOT point to an ELM parameter (could be a meta param), then return to try the next one */
				if (getSupportedParameters()[this.currentParameterIndex_] instanceof ELMParameter == false)
				{
					return;
				}
				
				
				/* For quick reference */
				ELMParameter param = (ELMParameter)getSupportedParameters()[this.currentParameterIndex_];
					
				/* Send the elm request */
//				readSeralData();
				resetReadBuffer();
				sendELMCommand(param.getFullCommand());
				
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
					
					/* If we've already tried again, then go to the reset stage */
					if (this.pidReadAttemptCount_ >= 1)
					{
						this.pidReadAttemptCount_ = 0;
						setStageAndMode(STAGE_RESET, MODE_READY);
					}
					
					/* Reset the timer, incase we're going to try once more */
					setOperationTimer(READ_WRITE_TIMEOUT_MS);

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
						extractResponseBytes((ELMParameter)SUPPORTED_PARAMS[this.currentParameterIndex_]);
					}
					catch(Exception e2)
					{
						/* An exception here is not normal, nor expected.  If it happens, we'll do a re-init */
						ErrorLog.error("Parameter Fetch Exception, do reset.", e2);
						ErrorLog.error("RX: [" + responseToString() + "]");
						setStageAndMode(STAGE_RESET, MODE_READY);
						return;
					}
					
					setStageAndMode(STAGE_PID_REQ, MODE_READY);
					fireParemeterFetchedEvent(SUPPORTED_PARAMS[this.currentParameterIndex_]);
					fireEndParameterBatchEvent();
				}
				
			break;
			
			default:
				throw new InvalidStageModeException(getStage(), getMode());
		}
	}

	
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
		
		p.notifyValueChanged();
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
