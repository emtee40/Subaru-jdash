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
import net.sourceforge.JDashLite.profile.gauge.AnalogGauge;
import net.sourceforge.JDashLite.profile.gauge.DigitalCummulativeAverageGauge;
import net.sourceforge.JDashLite.profile.gauge.DigitalGauge;
import net.sourceforge.JDashLite.profile.gauge.LineGraphGauge;
import net.sourceforge.JDashLite.profile.gauge.SweepAnalogGauge;
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
		
	}
	
	
	/********************************************************
	 * @return
	 ********************************************************/
	public static Profile createSampleProfile()
	{
		Profile sampleProfile = new Profile();
		sampleProfile.setName("Sample Profile");
		sampleProfile.setProtocolClass(ELMProtocol.class.getName());
		
		ProfilePage page = null;
		ProfileRow row = null;
		AnalogGauge analogGauge = null;
		DigitalGauge digitalGauge = null;
		LineGraphGauge lineGraphGauge = null;
//		SweepAnalogGauge sweepGauge = null;
//		LEDGauge ledGauge = null;
		
		/* Page 0 */
		page = new ProfilePage();
		sampleProfile.addPage(page);
		
		/* Page 0 row 0 */
		row = new ProfileRow(); 
		page.addRow(row);
		row.setHeightPercent(0.4);		

		
		/* Page 0 row 0 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, null);
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "RPM");
		analogGauge.setPrecision(0);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.25);
		analogGauge.setRangeStart(0);
		analogGauge.setRangeEnd(8000);
		analogGauge.setTickCount(9);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(false);
		analogGauge.setTickLabelDivisor(1000);
		analogGauge.setIncludeHighHold(true);
		analogGauge.setIncludeLowHold(true);
		
		/* Page 0 row 0 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "RPM");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME,  "RPM");
		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(0);
		lineGraphGauge.setRangeEnd(8000);
		


		/* Page 0 row 0 gauge 2 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL, null);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "RPM");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"RPM");

		
		
		/* Page 0 row 1 */
		row = new ProfileRow(); 
		page.addRow(row);
		
		/* Page 0 row 1 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, "STFT");
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "STFT1");
		analogGauge.setPrecision(2);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.25);
		analogGauge.setRangeStart(-30);
		analogGauge.setRangeEnd(30);
		analogGauge.setTickCount(5);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(false);

		
		/* Page 0 row 1 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "ST Fuel Trim %");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "STFT1");
		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(-30);
		lineGraphGauge.setRangeEnd(30);

		/* Page 0 row 1 gauge 2 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL, null);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME, "STFT1");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 2);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"STFT%");

		
		/* Page 0 row 2 */
		row = new ProfileRow(); 
		page.addRow(row);
		
		
		/* Page 0 row 2 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, null);
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "LTFT1");
		analogGauge.setPrecision(2);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.25);
		analogGauge.setRangeStart(-30);
		analogGauge.setRangeEnd(30);
		analogGauge.setTickCount(5);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(false);

		
		/* Page 0 row 2 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "LT Fuel Trim %");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "LTFT1");
		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(-30);
		lineGraphGauge.setRangeEnd(30);

		/* Page 0 row 2 gauge 2 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL, null);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME, "LTFT1");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 2);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"LTFT%");
		
		
		/* Page 1 */
		page = new ProfilePage();
		sampleProfile.addPage(page);

		/* Page 1 Row 0 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 1 row 0 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(DigitalGauge.PROP_STR_LABEL, "RPM");
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "RPM");
		analogGauge.setPrecision(0);
		analogGauge.setRangeStart(0);
		analogGauge.setRangeEnd(8000);
		analogGauge.setTickCount(9);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(true);
		analogGauge.setTickLabelDivisor(1000);
		analogGauge.setIncludeHighHold(true);
		analogGauge.setIncludeLowHold(true);


		/* Page 1 Row 1 */
		row = new ProfileRow();
		page.addRow(row);

		/* Page 1 row 1 gauge 0 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		row.setHeightPercent(0.2);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, null);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME,  "RPM");
		lineGraphGauge.setRangeStart(0);
		lineGraphGauge.setRangeEnd(8000);
		
		

		/* Page 2 */
		page = new ProfilePage();
		sampleProfile.addPage(page);

		/* Page 2 Row 0 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 2 row 0 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, null);
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "COOLANT_TEMP_C");
		analogGauge.setPrecision(0);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.4);
		analogGauge.setRangeStart(-40);
		analogGauge.setRangeEnd(220);
		analogGauge.setTickCount(14);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(true);
		
		/* Page 2 row 0 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "Coolant C");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "COOLANT_TEMP_C");
//		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(-40);
		lineGraphGauge.setRangeEnd(220);
		
		
		/* Page 2 Row 1 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 2 row 1 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, null);
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "COOLANT_TEMP_F");
		analogGauge.setPrecision(0);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.4);
		analogGauge.setRangeStart(-40);
		analogGauge.setRangeEnd(420);
		analogGauge.setTickCount(10);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(true);
	
		
		/* Page 2 row 1 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "Coolant F");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "COOLANT_TEMP_F");
//		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(-40);
		lineGraphGauge.setRangeEnd(420);
		

		/* Page 2 Row 2 */
		row = new ProfileRow();
		page.addRow(row);
		
		
		/* Page 2 row 2 gauge 0 */
		analogGauge = new AnalogGauge();
		row.addGauge(analogGauge);
		analogGauge.setProperty(AnalogGauge.PROP_STR_LABEL, null);
		analogGauge.setProperty(AnalogGauge.PROP_STR_PARAMETER_NAME, "LOAD");
		analogGauge.setPrecision(0);
		analogGauge.setDoubleProperty(AnalogGauge.PROP_D_WIDTH, 0.4);
		analogGauge.setRangeStart(0);
		analogGauge.setRangeEnd(100);
		analogGauge.setTickCount(5);
		analogGauge.setIncludeTicks(true);
		analogGauge.setIncludeTickLabels(true);
		analogGauge.setIncludeDigitalValue(true);
		
		/* Page 2 row 2 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "Load %");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "LOAD");
//		lineGraphGauge.setDoubleProperty(LineGraphGauge.PROP_D_WIDTH, 0.55);
		lineGraphGauge.setRangeStart(0);
		lineGraphGauge.setRangeEnd(100);

		

		/* Page 3 */
		page = new ProfilePage();
		sampleProfile.addPage(page);

		/* Page 3 Row 0 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 3 row 0 gauge 0 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MAF");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 2);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"MAF");
		
		/* Page 3 row 0 gauge 1 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MAP_kpa");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 1);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"kpa");
		
		/* Page 3 row 0 gauge 1 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MAP_psi");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 1);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"psi");
		
		/* Page 3 row 0 gauge 1 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MAP_inhg");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 1);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"Hg");
		
		/* Page 3 Row 1 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 3 row 1 gauge 0 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "KPH");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"KPH");
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MPH");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"MPH");
		
		
		/* Page 3 Row 2 */
		row = new ProfileRow();
		page.addRow(row);
		
		/* Page 3 row 2 gauge 0 */
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "INTAKE_TEMP_C");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"IAT C");
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "INTAKE_TEMP_F");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"IAT F");
		
		/* Page 3 Row 3 */
		row = new ProfileRow();
		page.addRow(row);
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "DTC_COUNT");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"CEL#");

//		digitalGauge = new DigitalGauge();
//		row.addGauge(digitalGauge);
//		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "FS1");
//		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
//		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"CL");
//
//		digitalGauge = new DigitalGauge();
//		row.addGauge(digitalGauge);
//		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "FS2");
//		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
//		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"OL");
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "DTC1");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"DTC1");
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "DTC2");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"DTC2");
		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "DTC3");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"DTC3");

		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "DTC4");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 0);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"DTC4");

		
		/* Page 4 */
		page = new ProfilePage();
		sampleProfile.addPage(page);

		
		/* Page 4 Row 0 */
		row = new ProfileRow();
		page.addRow(row);

		
		digitalGauge = new DigitalGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MPG1");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 2);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"MPG");

		
		digitalGauge = new DigitalCummulativeAverageGauge();
		row.addGauge(digitalGauge);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_PARAMETER_NAME,  "MPG1");
		digitalGauge.setIntProperty(DigitalGauge.PROP_I_PRECISION, 2);
		digitalGauge.setProperty(DigitalGauge.PROP_STR_LABEL,"MPG Avg");
		
		

		/* Page 4 Row 1 */
		row = new ProfileRow();
		page.addRow(row);
		
		
		/* Page 2 row 2 gauge 1 */
		lineGraphGauge = new LineGraphGauge();
		row.addGauge(lineGraphGauge);
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_LABEL, "MPG");
		lineGraphGauge.setProperty(LineGraphGauge.PROP_STR_PARAMETER_NAME, "MPG1");
		lineGraphGauge.setRangeStart(0);
		lineGraphGauge.setRangeEnd(100);
		
		return sampleProfile;
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
	

}
