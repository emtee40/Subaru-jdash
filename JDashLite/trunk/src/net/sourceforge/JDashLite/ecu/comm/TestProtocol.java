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

package net.sourceforge.JDashLite.ecu.comm;

import waba.sys.Vm;


/*********************************************************
 * 
 *
 *********************************************************/

public class TestProtocol extends AbstractProtocol implements ProtocolHandler
{
	
	
	/* 
	 * 0 = open port
	 * 1 = init interface stage a
	 * 2 = init interface stage b
	 * 3 = init interface stage c
	 * 4 = init interface stage d
	 * 5 = init complete
	 * 6 = begin batch
	 * 7 = end batch
	 */
	private int initMode_ = 0;
	
	/* 0 = ready, 1 = TX, 2 = RS */
	private int RxTxMode_ = 0; 
	
	
	
	private StubbedParameter[] stubbedParameters_ = null;
	
	private int prevEventTS_ = 0;
	
	private int parameterIndex_ = 0;
	
	/********************************************************
	 * 
	 *******************************************************/
	public TestProtocol(ProtocolHandler primaryHandler)
	{
		
		this.stubbedParameters_ = new StubbedParameter[primaryHandler.getSupportedParameters().length];
		
		/* Setup our stubbed parameters */
		for (int index = 0; index < primaryHandler.getSupportedParameters().length; index++)
		{
			this.stubbedParameters_[index] = new StubbedParameter(primaryHandler.getSupportedParameters()[index]);
		}
	}
	

	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#getSupportedParameters()
	 ********************************************************/
	public ECUParameter[] getSupportedParameters()
	{
		return this.stubbedParameters_;
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#doTask()
	 ********************************************************/
	public void doTask()
	{
		
		
//		if (Vm.getTimeStamp() - 50 < this.prevEventTS_)
//		{
//			return;
//		}
//		
//		this.prevEventTS_ = Vm.getTimeStamp();
		
		switch(this.initMode_)
		{
			
			/* Open Port */
			case 0:
				fireInitStartedEvent();
				this.initMode_++;
			break;
			
			/* Init A */
			case 1:
				fireInitStatusEvent("Init.   ");
				this.initMode_++;
			break;
			
			/* Init B */
			case 2:
				fireInitStatusEvent("Init..  ");
				this.initMode_++;
			break;
			
			/* Init C */
			case 3:
				fireInitStatusEvent("Init... ");
				this.initMode_++;
			break;
			
			/* Init D */
			case 4:
				fireInitStatusEvent("Init....");
				this.initMode_++;
			break;
			
			/* Init Complete */
			case 5:
				fireInitFinishedEvent();
				this.initMode_++;
			break;
			
			/* Param Fetch Mode */
			case 6:
				fireBeginParameterBatchEvent(getSupportedParameters().length);
				this.initMode_++;
				this.parameterIndex_ = 0;
				this.prevEventTS_ = Vm.getTimeStamp();
			break;
				
			case 7:
				fireCommTXEvent();
				
				
				if (this.stubbedParameters_[this.parameterIndex_].isEnabled())
				{
					/* Wait for at least 50 ms.  If 50ms has not yet passed, just return to try again */
					if (Vm.getTimeStamp() - 200 < this.prevEventTS_)
					{
						return;
					}
					
					this.prevEventTS_ = Vm.getTimeStamp();

					this.stubbedParameters_[this.parameterIndex_].value_ += 95.1;
					this.stubbedParameters_[this.parameterIndex_].markTimeStamp();
					fireParemeterFetchedEvent(this.stubbedParameters_[this.parameterIndex_]);
					fireCommRXEvent();

				}

				/* Next param */
				this.parameterIndex_++;
				
				/* Last one?  finished the batch */
				if (this.parameterIndex_ >= this.stubbedParameters_.length)
				{
					this.initMode_++;
				}

			break;
			
			case 8:
				fireCommReady();
				fireEndParameterBatchEvent();
				this.initMode_ = 6;
			break;
			
		}
		
	}


	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#connect()
	 ********************************************************/
	public boolean connect()
	{
		
		this.initMode_ = 0;
		return true;
	}
	
	

	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class StubbedParameter extends ECUParameter
	{
		public double value_ = 1.0;
		
		/********************************************************
		 * 
		 *******************************************************/
		public StubbedParameter(ECUParameter p)
		{
			super(p.getName());
		}
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ECUParameter#getValue()
		 ********************************************************/
		public double getValue()
		{
			return this.value_;
		}
	}
	
}

