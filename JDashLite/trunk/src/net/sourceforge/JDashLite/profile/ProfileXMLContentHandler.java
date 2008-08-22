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

import net.sourceforge.JDashLite.profile.gauge.AnalogGauge;
import net.sourceforge.JDashLite.profile.gauge.DigitalGauge;
import net.sourceforge.JDashLite.profile.gauge.LineGraphGauge;
import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import superwaba.ext.xplat.xml.AttributeList;
import superwaba.ext.xplat.xml.ContentHandler;
import superwaba.ext.xplat.xml.XmlReader;
import waba.sys.Convert;
import waba.util.Vector;

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
//	public static final String ATTR_PARAM			= "param";
	public static final String ATTR_HEIGHT			= "height";
//	public static final String ATTR_WIDTH			= "width";
//	public static final String ATTR_PRECISION		= "precision";
//	public static final String ATTR_LABEL			= "label";
//	public static final String ATTR_RANGE_START		= "range-start";
//	public static final String ATTR_RANGE_END		= "range-end";
//	public static final String ATTR_TICK_COUNT		= "tick-count";
//	public static final String ATTR_SHOW_TICKS		= "show-ticks";
//	public static final String ATTR_SHOW_TICK_LABEL	= "tick_labels";
	
	public static final String VALUE_DIGITAL		= "digital";
	public static final String VALUE_LINE_GRAPH		= "line-graph";
	public static final String VALUE_ANALOG			= "analog";
	
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
		
//		ErrorLog.debug("Loading Profile\n" + xmlString);
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
			//gauge = createDigitalGauge(atts);
			gauge = new DigitalGauge();
		}
		else if (VALUE_LINE_GRAPH.equals(type))
		{
			//gauge = createLineGraphGauge(atts);
			gauge = new LineGraphGauge();
		}
		else if (VALUE_ANALOG.equals(type))
		{
			//gauge = createAnalogGauge(atts);
			gauge = new AnalogGauge();
		}
		else
		{
			throw new RuntimeException("Invalid Gauge Type " + type);
		}
		
		
		/* Add the proprties to the gauge */
		Vector keys = atts.getKeys();
		for (int index = 0; index < keys.size(); index++)
		{
			String key = (String)keys.items[index];
			String value = atts.getAttributeValue(key);
			gauge.setProperty(key, value);
		}
		
//		
//		/* All gauges have a parameter name */
//		gauge.setParameterName(atts.getAttributeValue(ATTR_PARAM));
//		
//		/* Set the width */
//		if (gauge != null)
//		{
//			String height = atts.getAttributeValue(ATTR_WIDTH);
//			if (height != null)
//			{
//				gauge.setWidthPercent(Convert.toDouble(height));
//			}
//		}
//		
//		/* Not all gauges have a label, but it's supported in the base ProfileGauge class */
//		gauge.setLabel(atts.getAttributeValue(ATTR_LABEL));
		
		return gauge;
	}
	
	
//	/*******************************************************
//	 * @param atts
//	 * @return
//	 ********************************************************/
//	private DigitalGauge createDigitalGauge(AttributeList atts)
//	{
//		
//		DigitalGauge gauge = new DigitalGauge();
//		String prec = atts.getAttributeValue(ATTR_PRECISION);
//
//		if ((prec != null && prec.length() > 0))
//		{
//			gauge.setDecimalPrecision(Convert.toInt(prec));
//		}
//
//		return gauge;
//	}
//	
//	
//	/*******************************************************
//	 * @param atts
//	 * @return
//	 ********************************************************/
//	private AnalogGauge createAnalogGauge(AttributeList atts)
//	{
//		AnalogGauge gauge = new AnalogGauge();
//		
//		String prec = atts.getAttributeValue(ATTR_PRECISION);
//		String rangeStart = atts.getAttributeValue(ATTR_RANGE_START);
//		String rangeEnd = atts.getAttributeValue(ATTR_RANGE_END);
//		String tickCount = atts.getAttributeValue(ATTR_TICK_COUNT);
//		String tickLabels = atts.getAttributeValue(ATTR_SHOW_TICK_LABEL);
//		String showTicks = atts.getAttributeValue(ATTR_SHOW_TICKS);
//
//		if ((prec != null && prec.length() > 0))
//		{
//			gauge.setDecimalPrecision(Convert.toInt(prec));
//		}
//		
//		gauge.setRangeStart(Convert.toDouble(rangeStart));
//		gauge.setRangeEnd(Convert.toDouble(rangeEnd));
//		gauge.setTickCount(Convert.toDouble(tickCount));
//		//gauge.setIncludeTickLabels(Convert.to)
//		
//		return gauge;
//	}
//	
//	/*******************************************************
//	 * @param atts
//	 * @return
//	 ********************************************************/
//	private LineGraphGauge createLineGraphGauge(AttributeList atts)
//	{
//		
//		LineGraphGauge gauge = new LineGraphGauge();
//		return gauge;
//	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public String generateXml()
	{
		StringBuffer sb = new StringBuffer();
		
		/* Add the profile tag */
		addOpenTag(sb, NODE_PROFILE);
		addTagAttribute(sb, ATTR_NAME, this.profile_.getName());
		addTagAttribute(sb, ATTR_PROTOCOL_CLASS, this.profile_.getProtocolClass());
		closeOpenTag(sb);
		
		/* Add each page */
		for (int pageIndex = 0; pageIndex < this.profile_.getPageCount(); pageIndex++)
		{
			ProfilePage page = this.profile_.getPage(pageIndex);
			
			/* Add the page tag */
			addOpenTag(sb, NODE_PAGE);
			closeOpenTag(sb);
			
			/* Add each row */
			for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
			{
				ProfileRow row = page.getRow(rowIndex);
				
				/* Add the row tag */
				addOpenTag(sb, NODE_ROW);
				if (row.getHeightPercent() > 0)
				{
					addTagAttribute(sb, ATTR_HEIGHT, Convert.toString(row.getHeightPercent(), 4));
				}
				closeOpenTag(sb);
				
				/* Add each gauge */
				for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
				{
					ProfileGauge gauge = row.getGauge(gaugeIndex);
					addGauge(sb, gauge);
				}
				
				/* close the row tag */
				addCloseTag(sb, NODE_ROW);
			}
			
			/* Close the page tag */
			addCloseTag(sb, NODE_PAGE);
		}
		
		/* Close the profile tag */
		addCloseTag(sb, NODE_PROFILE);
		
		return sb.toString();
	}
	
	
	/********************************************************
	 * @param sb
	 * @param tag
	 ********************************************************/
	private void addOpenTag(StringBuffer sb, String tag)
	{
		if (NODE_PAGE.equals(tag))
		{
			sb.append("  ");
		}
		
		if (NODE_ROW.equals(tag))
		{
			sb.append("    ");
		}
		
		if (NODE_GAUGE.equals(tag))
		{
			sb.append("      ");
		}
		
		sb.append("<" + tag);
	}
	
	
	/********************************************************
	 * @param sb
	 ********************************************************/
	private void closeOpenTag(StringBuffer sb)
	{
		sb.append(">\n");
	}
	
	/*******************************************************
	 * @param sb
	 * @param name
	 * @param value
	 ********************************************************/
	private void addTagAttribute(StringBuffer sb, String name, String value)
	{
		sb.append(" " + name + "=\"" + value + "\"");
	}
	
	
	/*******************************************************
	 * @param sb
	 * @param tag
	 ********************************************************/
	private void addCloseTag(StringBuffer sb, String tag)
	{
		if (NODE_PAGE.equals(tag))
		{
			sb.append("  ");
		}
		
		if (NODE_ROW.equals(tag))
		{
			sb.append("    ");
		}
		
		if (NODE_GAUGE.equals(tag))
		{
			sb.append("      ");
		}
		
		sb.append("</" + tag + ">\n");
	}
	

	/*******************************************************
	 * @param sb
	 * @param gauge
	 ********************************************************/
	private void addGauge(StringBuffer sb, ProfileGauge gauge)
	{
		addOpenTag(sb, NODE_GAUGE);
		
		/* Digital */
		if (gauge instanceof DigitalGauge)
		{
			DigitalGauge g = (DigitalGauge)gauge;
			addTagAttribute(sb, ATTR_TYPE, VALUE_DIGITAL);
//			addTagAttribute(sb, ATTR_PRECISION, Convert.toString(g.getDecimalPrecision()));
		}
		
		/* Line Graph */
		if (gauge instanceof LineGraphGauge)
		{
			addTagAttribute(sb, ATTR_TYPE, VALUE_LINE_GRAPH);
		}
		
		/* Analog */
		if (gauge instanceof AnalogGauge)
		{
			AnalogGauge g = (AnalogGauge)gauge;
			addTagAttribute(sb, ATTR_TYPE, VALUE_ANALOG);
//			addTagAttribute(sb, ATTR_PRECISION, Convert.toString(g.getDecimalPrecision()));
//			addTagAttribute(sb, ATTR_RANGE_START, Convert.toString(g.getRangeStart()));
//			addTagAttribute(sb, ATTR_RANGE_END, Convert.toString(g.getRangeEnd()));
//			addTagAttribute(sb, ATTR_TICK_COUNT, Convert.toString(g.getTickCount()));
//			addTagAttribute(sb, ATTR_SHOW_TICKS, Convert.toString(g.getIncludeTicks()));
//			addTagAttribute(sb, ATTR_SHOW_TICK_LABEL, Convert.toString(g.getIncludeTickLabels()));
		}
		
//		/* Width */
//		if (gauge.getWidthPercent() > 0)
//		{
//			addTagAttribute(sb, ATTR_WIDTH, Convert.toString(gauge.getWidthPercent(), 4));
//		}
//		
//		/* Param */
//		addTagAttribute(sb, ATTR_PARAM, gauge.getParameterName());
//		
//		/* Label */
//		addTagAttribute(sb, ATTR_LABEL, gauge.getLabel());
//
			
		/* Add all the known proprties */
		Vector keys = gauge.getPropertyKeys();
		for (int index = 0; index < keys.size(); index++)
		{
			String key = (String)keys.items[index];
			addTagAttribute(sb, key, gauge.getProperty(key));
		}
			
		closeOpenTag(sb);
		addCloseTag(sb, NODE_GAUGE);
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
