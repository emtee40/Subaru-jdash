/*******************************************************
 * 
 *  @author spowell
 *  XMLParameterLoader
 *  Aug 8, 2006
 *  $Id: XMLParameterLoader.java,v 1.4 2006/12/31 16:59:09 shaneapowell Exp $
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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/******************************************************
 * This class will read an XML based parameter file to create
 * parameters as needed.  Once created, call loadParams
 * with your parameter registry, to have it populated
 * with all of the parameters.
 ******************************************************/
public class XMLParameterLoader
{
    //XML Element names
    public static final String NODE_PARAMETERS 		= "parameters";
    public static final String NODE_PARAMETER 		= "parameter";
    public static final String NODE_ADDRESS 		= "address";
    public static final String NODE_BYTE 			= "byte";
    public static final String NODE_HANDLER 		= "handler";
    public static final String NODE_META_PARAM 		= "meta-parameter";
    public static final String NODE_ARGS 			= "args";
    public static final String NODE_DESC 			= "description";
    public static final String NODE_INCLUDE 		= "include";
    
    public static final String ATTR_MONITOR_CLASS 	= "monitor-class";
    public static final String ATTR_NAME 			= "name";
    public static final String ATTR_VALUE 			= "value";
    public static final String ATTR_FILE 			= "file";
    public static final String ATTR_RATE 			= "rate";

    private File file_ = null;
    
    private Element _root;

    private String monitorClass_ = null;
    
    private List<File> includes_ = null;
    
    
    /******************************************************
     * create an instance of an XML Parameter Loader.
     * 
     * @throws ParameterException
     ******************************************************/
    public XMLParameterLoader(File paramFile) throws Exception
    {
    	this.file_ = paramFile;
    	
    	if (this.file_.exists() == false)
    	{
    		throw new Exception("Cannot load parameter file ]" + paramFile.getAbsolutePath() + "].  It does not exist");
    	}
    	
    	if (this.file_.isFile() == false)
    	{
    		throw new Exception("Cannot load parameter file ]" + paramFile.getAbsolutePath() + "].  It does not appear to be a file");
    	}
    	
    	if (this.file_.canRead() == false)
    	{
    		throw new Exception("Cannot load parameter file ]" + paramFile.getAbsolutePath() + "].  It is not readable.");
    	}
    	
        try
        {
            SAXBuilder b = new SAXBuilder();
            Document d = b.build(paramFile);
            _root = d.getRootElement();
            
            this.includes_ = getIncludes(paramFile);
            
        }
        catch (JDOMException jde)
        {
            throw new ParameterException(jde);
        }
    }
    
    /*******************************************************
     * Get the file name parameter that this loader was constructed with.
     * @return
     ******************************************************/
    public File getFile()
    {
    	return this.file_;
    }

    
    /*******************************************************
     * Load all of the parameters into the provided registry.
     * 
     * @param reg IN - the registry to load the parameters into.
     * @throws Exception
     *******************************************************/
    public void loadParams(ParameterRegistry reg) throws Exception
    {
    	/* Load the raw parameters first */
    	loadEcuParams(reg);
    	
    	/* The metas after, because they are most likely going to use the RAW vales */
    	loadMetaParams(reg);
    }
    
    
    /********************************************************
     * Load the parameters from this loader into the provided ParameterRegistry
     * object.
     * 
     * @throws ParameterException
     *******************************************************/
    protected void loadEcuParams(ParameterRegistry reg) throws Exception
    {
    	/* Start with the includes */
    	for (File include : this.includes_)
    	{
    		XMLParameterLoader loader = new XMLParameterLoader(include);
    		loader.loadEcuParams(reg);
    	}

    	/* Parse the paramaters and add them as they are found */
    	parseParameters(_root.getChildren(NODE_PARAMETER), reg);

    }

    /*******************************************************
     * @return
     * @throws ParameterException
     *******************************************************/
    protected void loadMetaParams(ParameterRegistry reg) throws Exception
    {

    	/* Start with the includes */
    	for (File include : this.includes_)
    	{
    		XMLParameterLoader loader = new XMLParameterLoader(include);
    		loader.loadMetaParams(reg);
    	}

    	/* Parse the meta params, and add them as they are found */
    	parseMetaParameters(_root.getChildren(NODE_META_PARAM), reg);
    	
    }
    
    
    /********************************************************
     * Get the name of this parameter file.
     * @return the parameter file name. Not the FileName, but rather the
     * name value inside the parameter file.
     * @throws Exception if there was a problem reading the name attribute.
     *******************************************************/
    public String getName() throws Exception
    {
    	return this._root.getAttribute(ATTR_NAME).getValue();
    }
    
    /*******************************************************
     * Get the parameter files name attribute as the toString value.
     * Override
     * @see java.lang.Object#toString()
     *******************************************************/
    public String toString()
    {
    	try
    	{
    		return getName();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new RuntimeException("Unable to get parameter name, to String failure");
    	}
    }
    
    /*******************************************************
     * Get the parameter file description.
     * @return the description
     * @throws Exception if there was a problem reading the description
     ******************************************************/
    public String getDescription() throws Exception
    {
    	List elements = _root.getChildren(NODE_DESC);
    	if (elements.size() == 0)
    	{
    		throw new Exception("No " + NODE_DESC + " elements found in file");
    	}
    	
    	return ((Element)elements.get(0)).getTextTrim();
    }
    
    /********************************************************
     * Get the class name that this parameter file is designed for.
     * @return
     * @throws Exception
     *******************************************************/
    public String getMonitorClass() throws Exception
    {
    	if (this.monitorClass_ == null)
    	{
    		
    		/* Check EACH include, and in order, looking for a monitor class */
    		for (File include : this.includes_)
    		{
    			XMLParameterLoader loader = new XMLParameterLoader(include);
    			if (loader.getMonitorClass() != null)
    			{
    				this.monitorClass_ = loader.getMonitorClass();
    			}
    		}
    		
    		
    		/* Get the monitor class, if any, from this file. This is considered the final override */
    		if (this._root.getAttribute(ATTR_MONITOR_CLASS) != null)
    		{
    			this.monitorClass_ = this._root.getAttribute(ATTR_MONITOR_CLASS).getValue();
    		}

    	}
    	
    	return this.monitorClass_;
    }

    /********************************************************
     * Return the list of files that are identified as incldes in this paramter file.
     * 
     * @return the list of include files.
     * @throws ParameterException
     *******************************************************/
    private List<File> getIncludes(File thisFile) throws ParameterException
    {
    	File parentDir = thisFile.getParentFile();
    	
    	List<File> includes = new ArrayList<File>();
    	List elements = _root.getChildren(NODE_INCLUDE);
    	
    	Iterator i = elements.iterator();
    	while (i.hasNext())
    	{
    		Element e = (Element) i.next();
    		File includeFile = new File(parentDir, e.getAttribute(ATTR_FILE).getValue());
    		includes.add(includeFile);
    	}
    	
    	return includes;
    	
    }

    /********************************************************
     * Parse the list of parameters, and add each parameter to the
     * registry as it is read.
     * 
     * @param params IN - the list of param nodes.
     * @throws ParameterException
     *******************************************************/
    private void parseParameters(List params, ParameterRegistry reg) throws ParameterException
    {

        Iterator i = params.iterator();

        while(i.hasNext())
        {
            Element aParam = (Element) i.next();

            try
            {
            	/* Make sure the parameter has a name */
            	if ((aParam.getAttributeValue(ATTR_NAME) == null) || (aParam.getAttributeValue(ATTR_NAME).length() == 0))
            	{
            		throw new Exception("<parameter> tag cannot exist without a name");
            	}
            	
            	/* The speed is optional */
            	String strSpeed = aParam.getAttributeValue(ATTR_RATE);
            	int speed = 1;
            	if ((strSpeed != null) && (strSpeed.length() > 0))
            	{
            		speed = Integer.parseInt(aParam.getAttributeValue(ATTR_RATE));
            	}
            	
            	
	            // TODO - Description
	            ECUParameter ECUEcuP = new ECUParameter(getAddressBytes(aParam),
	                                                    aParam.getAttributeValue(ATTR_NAME),
	                                                    null,
	                                                    speed);
	            reg.add(ECUEcuP);
	            
            }
            catch(Exception e)
            {
            	throw new ParameterException("Unable to load parameter: " + aParam.getAttributeValue(ATTR_NAME), e);
            }
        }

    }

    /*******************************************************
     * * Parse the list of meta-parameters, and add each parameter to the
     * registry as it is read.
     * 
     * @param metaParams IN - the list of meta parameter nodes.
     * @throws ParameterException
     ******************************************************/
    private void parseMetaParameters(List metaParams, ParameterRegistry reg) throws Exception
    {
        Iterator i = metaParams.iterator();

        while(i.hasNext())
        {
            Element aParam = (Element) i.next();
            MetaParameter mp = (MetaParameter) getHandler(aParam);
            mp.setOwnerRegistry(reg);
            if (mp == null) throw new ParameterException("Meta-parameter must declare a handler");
            mp.setName(aParam.getAttributeValue(ATTR_NAME));
            mp.setArgs(getMetaArgs(aParam));
            reg.add(mp);
            
        }
        
    }

    /*******************************************************
     * @param aParam
     * @return
     *******************************************************/
    private Map<String, String> getMetaArgs(Element aParam)
    {
    	
    	if (aParam.getChild(NODE_ARGS) != null)
    	{
	        List args = aParam.getChild(NODE_ARGS).getChildren();
	        Map<String, String> argMap = new HashMap<String, String>(args.size());
	        Iterator i = args.iterator();
	        while(i.hasNext())
	        {
	            Element anArg = (Element) i.next();
	            argMap.put(anArg.getAttributeValue(ATTR_NAME), anArg.getAttributeValue(ATTR_VALUE));
	        }
	        return argMap;
    	}
    	else
    	{
    		return new HashMap<String, String>();
    	}
    }

    /*******************************************************
     * @param aParam
     * @return
     * @throws ParameterException
     ******************************************************/
    private byte[] getAddressBytes(Element aParam) throws ParameterException
    {
        List elements = aParam.getChild(NODE_ADDRESS).getChildren(NODE_BYTE);
        

        byte[] b = new byte[elements.size()];
        for(int i = 0; i < b.length; i++)
        {
            Element e = (Element) elements.get(i);
            
            /* We convert the string using the int decode because the Byte.decode() method can't
        	 * handle the byte string 0xff. This string results in a negative value, and that 
        	 * throws a NumberFormatException in the Byte Class.  So, we convert the string as an
        	 * integer, and cast it to a byte. This will keep the 0xff AS a true 0xff value of 255 int.
        	 */
            b[i] = (byte)Integer.decode(e.getTextTrim()).byteValue();
        }

        return b;
    }

    /*******************************************************
     * @param aParam
     * @return
     * @throws ParameterException
     *******************************************************/
    private Object getHandler(Element aParam) throws ParameterException
    {
        Element handler = aParam.getChild(NODE_HANDLER);
        if (handler == null) return null;

        try
        {
            Object o = Class.forName(handler.getTextTrim()).newInstance();
            return o;
        }
        catch (InstantiationException ie)
        {
            throw new ParameterException(ie);
        }
        catch (IllegalAccessException iae)
        {
            throw new ParameterException(iae);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new ParameterException(cnfe);
        }
    }
    
    
    /********************************************************
     * Convert the params to an xml formatted string that
     * is compatible with this loader.
     * @param params
     * @return
     *******************************************************/
    public static String toXml(List<Parameter> params) throws Exception
    {
    	if (params == null)
    	{
    		return "No Parameters Provided";
    	}
    		
    	/* Create the document and root node */
    	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document xmlDoc = builder.newDocument(); 
    	org.w3c.dom.Element rootElement = xmlDoc.createElement(NODE_PARAMETERS);
    	xmlDoc.appendChild(rootElement);
    	
    	/* Add each parameter */
    	for (Parameter p : params)
    	{

    		/* We only currently support ecu parameters */
    		if (p instanceof ECUParameter)
    		{
    			ECUParameter param = (ECUParameter)p;
    		
	    		/* <parameter> */
	    		org.w3c.dom.Element paramNode = xmlDoc.createElement(NODE_PARAMETER);
	    		rootElement.appendChild(paramNode);
	    		
	    		/* Parameter name */
	    		paramNode.setAttribute(ATTR_NAME, param.getName());
	
	    		/* Parameter Description */
	    		//paramNode.setAttribute(ATTR_DESC, param.getDescrption());
	    		org.w3c.dom.Element descNode = xmlDoc.createElement(NODE_DESC);
	    		descNode.appendChild(xmlDoc.createTextNode(param.getDescrption()));
	    		paramNode.appendChild(descNode);
	    		
	    		
	    		/* <address> */
	    		org.w3c.dom.Element addressNode = xmlDoc.createElement(NODE_ADDRESS);
	    		paramNode.appendChild(addressNode);
	    		
	    		/* <byte> */
	    		for (byte b : param.getAddress())
	    		{
	    			org.w3c.dom.Element byteNode = xmlDoc.createElement(NODE_BYTE);
	    			addressNode.appendChild(byteNode);
	    			byteNode.appendChild(xmlDoc.createTextNode(String.format("0x%02x", b)));
	    		}
	    		
    		}
    		
    	}
        
    	
    	/* Write the xml to the return string */
        OutputStream os = new ByteArrayOutputStream();
        OutputFormat of = new OutputFormat();
        of.setOmitDocumentType(true);
        of.setOmitXMLDeclaration(true);
        of.setIndent(1);
        of.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(os,of);
        serializer.asDOMSerializer();
        serializer.serialize( xmlDoc.getDocumentElement() );
        os.close();
        
        
        /* Return the xml string */
    	return os.toString();
    }
}
