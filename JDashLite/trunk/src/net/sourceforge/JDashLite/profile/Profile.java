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

import superwaba.ext.xplat.xml.XmlReader;
import net.sourceforge.JDashLite.ProfilesContainer;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ELM.ELMProtocol;
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

	public static final String TEST_PROFILE_XML =
" <profile name=\"Test Profile\" protocolClass=\"" + ELMProtocol.class.getName() +  "\"> " +
"   <page> " +
"     <row height=\"0.5\"> " +
"       <gauge type=\"digital\" param=\"RPM\" width=\".2\"/> " +
"       <gauge type=\"digital\" param=\"RPM\" width=\".4\"/> " +
"       <gauge type=\"digital\" param=\"RPM\"/> " +
"     </row> " +
"     <row height=\"0.2\"> " +
"     </row> " +
"     <row> " +
"       <gauge type=\"digital\" param=\"RPM\" width=\"0.25\"/> " +
"       <gauge type=\"digital\" param=\"RPM\"/> " +
"     </row> " +
"   </page> " +
"	<page> " +
"     <row> " +
"     </row> " +
"   </page> " +
"   <page> " +
"   </page> " +
"   <page> " +
"     <row> " +
"     </row> " +
"     <row> " +
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
	
	/********************************************************
	 * Create a new, empty profile.
	 *******************************************************/
	public Profile()
	{
	// TODO
		this.parameterNames_.addElement("RPM");
		this.parameterNames_.addElement("STFT1");
		this.parameterNames_.addElement("LTFT1");
		
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
	}
	
	
	/*******************************************************
	 * Given this profile, generate an XML file that represents it.
	 * @return
	 ********************************************************/
	public String getAsXml()
	{
		return "";
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
	
}
