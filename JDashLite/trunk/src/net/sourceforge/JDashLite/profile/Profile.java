/*********************************************************
 * 
 * @author spowell
 * Const.java
 * Jul 23, 2008
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

import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import net.sourceforge.JDashLite.profile.color.DefaultColorModel;
import waba.fx.Color;
import waba.util.Vector;

/*********************************************************
 * A profile defines the comms and visual parts for use in
 * JDash lite.  A profile describes what communications 
 * protocol to use, and what display elements are visible.
 *
 * An example of a structured XML string.  Note, the 
 * white space is added for our readability, the actual XML
 * generated will not contain it.
 
 <pre>
 
 	<profile name="My profile" protocolClass="net.sourceforge.JDashLite.ecu.comm.ELMProtocol">
 		<page>
 			<row>
 				<gauge type="type" param="param"/>
 			</row>
 			<row>
 				<gauge/>
 				<gauge/>
 			</row>
 		</page>
 		<page>
 			<row>
 				<gauge/>
 			</row>
 		</page>
 	</profile>
 
 </pre>
  
  Different types of Gauges 
   - Digital with high/low
   - Analog with high/low and a timed reset
   - Line Graph over time with high/low
   - More TBA
  
 *
 *********************************************************/
public class Profile
{

	public static final String SAMPLE_PROFILE_XML =
" <profile name=\"Sample Profile\" protocolClass=\"" + ELMProtocol.class.getName() +  "\"> " +
"   <page> " +
"     <row>" +
"		<gauge type=\"line-graph\" param=\"RPM\" width=\"0.5\"/>" +
"       <gauge type=\"analog\" param=\"RPM\" label=\"RPM\" range-start=\"0\" range-end=\"800\"/> " +
"     </row> " +
"     <row> " +
"		<gauge type=\"line-graph\" param=\"STFT\"/>" +
"       <gauge type=\"analog\" param=\"STFT1\" label=\"STFT\" precision=\"2\" range-start=\"-30\" range-end=\"30\"/> " +
"       <gauge type=\"digital\" param=\"STFT1\" precision=\"2\" label=\"STFT%\"/> " +
"     </row> " +
"     <row> " +
"		<gauge type=\"line-graph\" param=\"LTFT\" width=\"0.3\" precision=\"2\"/>" +
"       <gauge type=\"analog\" param=\"LTFT1\" width=\"0.4\" label=\"STFT\" precision=\"2\" range-start=\"-30\" range-end=\"30\"/> " +
"       <gauge type=\"digital\" param=\"LTFT1\" precision=\"2\" label=\"LTFT%\"/> " +
"     </row> " +
"   </page> " +
"	<page> " +
"     <row> " +
"       <gauge type=\"digital\" param=\"RPM\"/> " +
"       <gauge type=\"digital\" param=\"RPM\"/> " +
"     </row> " +
"   </page> " +
" </profile> ";
		

	/* The unique name of this profile */
	private String name_ = null;
	
	/* The comms protocol class */
	private String protocolClass_ = null;
	
	/* The list of display pages.  each element is an instance of a Profile.Page */
	private Vector pages_ = new Vector(4);
	
	/* The list of parameters added to this profile */
	private Vector parameterNames_ = new Vector(4);
	
	/* The rendering color model */
	private ColorModel colorModel_ = ColorModel.DEFAULT_COLOR_MODEL;
	
	/********************************************************
	 * Create a new, empty profile.
	 *******************************************************/
	public Profile()
	{
//	// TODO
//		this.parameterNames_.addElement("RPM");
//		this.parameterNames_.addElement("STFT1");
//		this.parameterNames_.addElement("LTFT1");
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 ********************************************************/
	public String toString()
	{
		return getName();
	}
	
	/*******************************************************
	 * Setup the values in this profile according to the 
	 * provided XML file.
	 * @param xml
	 ********************************************************/
	public void loadFromXml(String xml) throws Exception
	{
		
		ProfileXMLContentHandler contentHandler = new ProfileXMLContentHandler(this);
		contentHandler.parse(xml);
		
		if (getName() == null)
		{
			throw new Exception("Profile is missing name \n" + xml);
		}
		
		if (getProtocolClass() == null)
		{
			throw new Exception("Profile is missign protocol\n" + xml);
		}
	}
	
	
	/*******************************************************
	 * @return
	 * @throws Exception
	 ********************************************************/
	public String toXml() throws Exception
	{
		ProfileXMLContentHandler contentHandler = new ProfileXMLContentHandler(this);
		return contentHandler.generateXml();
	}

	
	/********************************************************
	 * @return the name
	 ********************************************************/
	public String getName()
	{
		return this.name_;
	}
	
	/********************************************************
	 * @param name the name to set
	 ********************************************************/
	public void setName(String name)
	{
		this.name_ = name;
	}
	
	
	
	/********************************************************
	 * @return the protocolClass
	 ********************************************************/
	public String getProtocolClass()
	{
		return this.protocolClass_;
	}
	
	
	/********************************************************
	 * @param protocolClass the protocolClass to set
	 ********************************************************/
	public void setProtocolClass(String protocolClass)
	{
		this.protocolClass_ = protocolClass;
	}

	/*******************************************************
	 * @return
	 ********************************************************/
	public int getPageCount()
	{
		return this.pages_.size();
	}
	
	/********************************************************
	 * @param index
	 * @return
	 ********************************************************/
	public ProfilePage getPage(int index)
	{
		return (ProfilePage)this.pages_.items[index];
	}

	/********************************************************
	 * @param page
	 ********************************************************/
	public void addPage(ProfilePage page)
	{
		this.pages_.addElement(page);
	}

	/*******************************************************
	 * @param index
	 ********************************************************/
	public void removePage(int index)
	{
		this.pages_.removeElementAt(index);
	}
	
	/********************************************************
	 * 
	 ********************************************************/
	public void removeAllPages()
	{
		this.pages_.removeAllElements();
	}
	
	/********************************************************
	 * @return
	 ********************************************************/
	public int getParameterCount()
	{
		return this.parameterNames_.size();
	}
	
	
	/*******************************************************
	 * @param index
	 * @return
	 ********************************************************/
	public String getParameterName(int index)
	{
		return (String)this.parameterNames_.items[index];
	}
	
	
	/********************************************************
	 * Given one of the CLR_xxx codes, this method will
	 * return the color associated with it.
	 * @param colorCode
	 * @return
	 ********************************************************/
	public ColorModel getColorModel()
	{
		return this.colorModel_;
	}
}
