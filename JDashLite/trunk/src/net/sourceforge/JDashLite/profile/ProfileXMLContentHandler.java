/*********************************************************
 * 
 * @author spowell
 * ProfileXMLContentHandler.java
 * Aug 7, 2008
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

import net.sourceforge.JDashLite.profile.gauge.DigitalGauge;
import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import superwaba.ext.xplat.xml.AttributeList;
import superwaba.ext.xplat.xml.ContentHandler;
import superwaba.ext.xplat.xml.XmlReader;
import waba.sys.Convert;

/*********************************************************
 * 
 *
 *********************************************************/

public class ProfileXMLContentHandler implements ContentHandler
{
	
	public static final String NODE_PROFILE 	= "profile";
	public static final String NODE_PAGE 		= "page";
	public static final String NODE_ROW			= "row";
	public static final String NODE_GAUGE 		= "gauge";
	
	public static final String ATTR_NAME			= "name";
	public static final String ATTR_PROTOCOL_CLASS 	= "protocolClass";
	public static final String ATTR_TYPE			= "type";
	public static final String ATTR_PARAM			= "param";
	public static final String ATTR_HEIGHT			= "height";
	public static final String ATTR_WIDTH			= "width";
	
	public static final String VALUE_DIGITAL		= "digital";
	
	private static int TAG_PROFILE 	= 0;
	private static int TAG_PAGE 	= 0;
	private static int TAG_ROW		= 0;
	private static int TAG_GAUGE 	= 0;
	
	private Profile profile_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileXMLContentHandler(Profile profile)
	{
		this.profile_ = profile;
	}
	
	
	/*******************************************************
	 * @param xmlString
	 ********************************************************/
	public void parse(String xmlString) throws Exception
	{
		ProfileXmlReader xmlReader = new ProfileXmlReader();
		xmlReader.setContentHandler(this);
		
		if (TAG_PROFILE == 0)
		{
			TAG_PROFILE = xmlReader.getTagCode(NODE_PROFILE);
			TAG_PAGE = xmlReader.getTagCode(NODE_PAGE);
			TAG_ROW = xmlReader.getTagCode(NODE_ROW);
			TAG_GAUGE = xmlReader.getTagCode(NODE_GAUGE);
		}
		
		xmlReader.parse(xmlString.getBytes(), 0, xmlString.length());
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.xml.ContentHandler#characters(java.lang.String)
	 ********************************************************/
	public void characters(String s)
	{
		/* Do Nothing */
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.xml.ContentHandler#comment(java.lang.String)
	 ********************************************************/
	public void comment(String s)
	{
		/* Do nothing */
		
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.xml.ContentHandler#endElement(int)
	 ********************************************************/
	public void endElement(int tag)
	{
		/* do nothing */ 
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see superwaba.ext.xplat.xml.ContentHandler#startElement(int, superwaba.ext.xplat.xml.AttributeList)
	 ********************************************************/
	public void startElement(int tag, AttributeList atts)
	{

		/* Setup the profile */
		if (tag == TAG_PROFILE)
		{
			this.profile_.setName(atts.getAttributeValue(ATTR_NAME));
			this.profile_.setProtocolClass(atts.getAttributeValue(ATTR_PROTOCOL_CLASS));
		}
		else if (tag == TAG_PAGE)
		{
			this.profile_.addPage(new ProfilePage());
		}
		else if (tag == TAG_ROW)
		{
			ProfilePage page = this.profile_.getPage(this.profile_.getPageCount() - 1);
			ProfileRow row = new ProfileRow();
			String height = atts.getAttributeValue(ATTR_HEIGHT);
			if (height != null)
			{
				row.setHeightPercent(Convert.toDouble(height));
			}
			page.addRow(row);
		}
		else if (tag == TAG_GAUGE)
		{
			ProfilePage page = this.profile_.getPage(this.profile_.getPageCount() - 1);
			ProfileRow row = page.getRow(page.getRowCount() - 1);
			row.addGauge(createGauge(atts));
		}
		else
		{
			throw new RuntimeException("Invalid XML Tag [" + tag + "] in profile");
		}
	}
	
	
	/*******************************************************
	 * @param atts
	 ********************************************************/
	private ProfileGauge createGauge(AttributeList atts)
	{
		String type = atts.getAttributeValue(ATTR_TYPE);
		
		ProfileGauge gauge = null;
		
		if (VALUE_DIGITAL.equals(type))
		{
			gauge = createDigitalGauge(atts);
		}

		/* Set the width */
		if (gauge != null)
		{
			String height = atts.getAttributeValue(ATTR_WIDTH);
			if (height != null)
			{
				gauge.setWidthPercent(Convert.toDouble(height));
			}
		}
		
		return gauge;
	}
	
	
	/*******************************************************
	 * @param atts
	 * @return
	 ********************************************************/
	private DigitalGauge createDigitalGauge(AttributeList atts)
	{
		
		DigitalGauge gauge = new DigitalGauge();
		gauge.setParameterName(atts.getAttributeValue(ATTR_PARAM));
		return gauge;
	}
	
	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class ProfileXmlReader extends XmlReader
	{
		
		public int getTagCode(String node)
		{
			return getTagCode(node.getBytes(), 0, node.length());
		}
	}
	
}
