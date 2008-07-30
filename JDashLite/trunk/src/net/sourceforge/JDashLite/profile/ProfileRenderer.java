/*********************************************************
 * 
 * @author spowell
 * ProfileDisplayContainer.java
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

package net.sourceforge.JDashLite.profile;

import net.sourceforge.JDashLite.ecu.comm.AbstractProtocol;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import waba.fx.Graphics;
import waba.sys.Vm;
import waba.ui.Container;

/*********************************************************
 * This is the container that does the heavy lifting of
 * setting up a container that represents a given 
 * profile object. 
 *
 *********************************************************/
public class ProfileRenderer extends Container
{

	private Profile profile_ = null;
	private ECUParameter[] parameters_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileRenderer(Profile profile)
	{
		this.profile_ = profile;
	}
	
	
	/*******************************************************
	 * @param parameters
	 ********************************************************/
	public void setParameters(ECUParameter[] parameters)
	{
		this.parameters_ = parameters;
	}
	
	
	/********************************************************
	 * Render this profile to the provided graphics obje.ct
	 * @param g
	 ********************************************************/
	public void render(Graphics g) throws Exception
	{
		if (this.profile_ == null)
		{
			throw new Exception("No Profile Provided");
		}
		
		if (this.parameters_ == null)
		{
			//throw new Exception("No Parameters Provided");
			return;
		}
			

		g.drawText(this.profile_.getName(), 20, 20);
		
		ECUParameter p = AbstractProtocol.getParameter(ECUParameter.RPM, this.parameters_);
		
		if (p == null)
		{
			throw new Exception("Protocol missing " + ECUParameter.RPM);
		}
			
		g.drawText(p.getName() + ": " + p.getValue() + "", 20, 45);
		
		g.drawText(ECUParameter.SPECIAL_PARAM_RATE.getName() + ": " + ECUParameter.SPECIAL_PARAM_RATE.getValue(), 20, 70);
	}
}
