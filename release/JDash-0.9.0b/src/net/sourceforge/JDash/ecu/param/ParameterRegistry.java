/*******************************************************
 * 
 *  @author spowell
 *  ParameterRegistry
 *  Aug 8, 2006
 *  $Id: ParameterRegistry.java,v 1.4 2006/12/31 16:59:09 shaneapowell Exp $
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
package net.sourceforge.JDash.ecu.param;

import net.sourceforge.JDash.ecu.param.special.RateParameter;
import net.sourceforge.JDash.ecu.param.special.TimeParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterRegistry
{
	
	/**
	 * The name of the TIME parameter 
	 */
	public static final String TIME_PARAM = "TIME";
	
	/**
	 * The name of the RATE parameter
	 */
    public static final String RATE_PARAM = "RATE";
    
    
    
    /** If you want to do DTC and MIL stuff with this monitor, then you MUST include in your parameter
	 * file the MIL_STATUS and E_MIL_STATUS parameters.  The E_MIL_STATUS parameter is the RAW mil status.
	 * Then the MIL_STATUS is the 1 or 0 value of the MIL light */
	public static final String PARAM_NAME_MIL_STATUS = "MIL_STATUS";
	
    /** If you want access to the low level MIL indicator parameter, it MUST be named this.  This
     * will give some monitors the ability to reset it's update time, and force a re-read of the
     * MIL status  */
    public static final String PARAM_NAME_E_MIL_STATUS = "E_" + PARAM_NAME_MIL_STATUS;

	
	private Map<String, Parameter> _paramMap;
	
	private List<DTCMetaParam> dtcParameters_ = null;
	
	/******************************************************
	 * create a new, bare registry.  Call init() to setup 
	 * the correct parameters. 
	 ******************************************************/
	public ParameterRegistry() throws Exception
	{
		this._paramMap = new HashMap<String, Parameter>();

		
		TimeParameter time = new TimeParameter();
		RateParameter rate = new RateParameter(time);
		
		add(time);
		add(rate);

	}
	
	/*******************************************************
	 * Initialize thie registry
	 * 
	 * @param loader IN - the loader to get the values from
	 * @throws Exception if there is a problem getting the values.
	 *******************************************************/
	public void init(XMLParameterLoader loader) throws Exception
	{
		loader.loadParams(this);
	}
	
	/*******************************************************
	 * Adds this parameter to this registiry. The setOwnerRegistry()
	 * method on the parameter will be called as it is added.
	 * 
	 * @param param IN - the parameter to add to this registry.
	 ******************************************************/
	public void add(Parameter param)
	{
		param.setOwnerRegistry(this);
		this._paramMap.put(param.getName(), param);
	}
	
	
	/*******************************************************
	 * @param name
	 * @return the parameter, or null if it didn't exist.
	 * @throws ParameterException
	 ******************************************************/
	public Parameter getParamForName(String name) throws ParameterException
	{
		if (_paramMap == null)
		{
			throw new ParameterException("The ParameterRegistry class has not yet been initialized with a loader");
		}
		
		return _paramMap.get(name);
		
	}
	

	/*******************************************************
	 * Return an orderd list of DTCParameters. The list is ordered
	 * by the parameter name.
	 * @return
	 *******************************************************/
	public List<DTCMetaParam> getDTCParameters()
	{
		
		/* Initialize the list */
		if (this.dtcParameters_ == null)
		{
			/* Create a new list */
			this.dtcParameters_ = new ArrayList<DTCMetaParam>();
			
			for (String key : this._paramMap.keySet())
			{
				if (this._paramMap.get(key) instanceof DTCMetaParam)
				{
					this.dtcParameters_.add((DTCMetaParam)this._paramMap.get(key));
				}
			}
		
			
			/* Sort the list by the DTC name */
			Collections.sort(this.dtcParameters_, new Comparator<DTCMetaParam>()
			{
				public int compare(DTCMetaParam p1, DTCMetaParam p2)
				{
					return p1.getName().compareTo(p2.getName());
				}
			});
			
			
			/* make the list unmodifaiable */
			this.dtcParameters_ = Collections.unmodifiableList(this.dtcParameters_);
		}
		
		
		
		return this.dtcParameters_;
		
	}
	
	
	
	/*******************************************************
	 * Returns an Unmodifiable Map of these parameters.
	 * @return
	 ******************************************************/
	public Map<String, Parameter> getAll()
	{
		return Collections.unmodifiableMap(_paramMap);
	}
	
}
