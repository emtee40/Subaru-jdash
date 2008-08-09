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
	
	
	private ECUParameter[] stubbedParameters_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public TestProtocol(ProtocolHandler primaryHandler)
	{
		
		this.stubbedParameters_ = new ECUParameter[primaryHandler.getSupportedParameters().length];
		
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
		fireBeginParameterBatchEvent(1);
		
		/* Do Nothing */
		Vm.sleep(150);
		
		
		fireEndParameterBatchEvent();
	}


	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolHandler#connect()
	 ********************************************************/
	public boolean connect()
	{
		return true;
	}
	
	

	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class StubbedParameter extends ECUParameter
	{
		private double value_ = 1.0;
		
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
			this.value_ = this.value_ + 15.2;
			return this.value_;
		}
	}
	
}

