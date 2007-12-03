/*******************************************************
 * 
 *  @author spowell
 *  XMLSkinFactory.java
 *  Aug 28, 2006
 *  $Id: XMLSkinFactory.java,v 1.3 2006/09/14 02:03:45 shaneapowell Exp $
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
package net.sourceforge.JDash.skin.XMLSkin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinFactory;

/*******************************************************
 * This skin factory knows about, and creates instances of
 * XML Skins.
 ******************************************************/
public class XMLSkinFactory extends SkinFactory
{
	
	public static final String XML_SKINS_DIR = "XMLSkins";
	
	public static final String SKIN_FILE_EXTENSION = ".skn";
	
	private ArrayList<Skin> skins_ = new ArrayList<Skin>();
	
	/******************************************************
	 * Create a new instance of this skin factory.  Pre-populate
	 * it's skin cash with all of the XML skins it can find.
	 ******************************************************/
	public XMLSkinFactory() throws Exception
	{
		/* Get the list of all files in the base directory */
		File configDir = new File(Setup.SETUP_CONFIG_SKINS_DIR + File.separatorChar + XML_SKINS_DIR);
		
//		/* Check each jar file */
//		for (File f : configDir.listFiles())
//		{
//			/* If the file is a jar file, then search it's contents for any .skn files */
//			if (f.getName().endsWith(".jar") == true)
//			{
//				
//				JarFile jarFile = new JarFile(f);
//				
//				/* Get the entries in this jar file, so we can check each one */
//				Enumeration e = jarFile.entries();
//				while (e.hasMoreElements())
//				{
//					/* Get the particular entry */
//					JarEntry jarEntry = (JarEntry)e.nextElement();
//					
//					/* If it's a .skn file, then we'll use it */
//					if (jarEntry.getName().endsWith(SKIN_FILE_EXTENSION) == true)
//					{
//						URL jarEntryUrl = this.getClass().getResource("/" + jarEntry.getName());
//						
//						/* Load the skin so we can get it's name */
//						XMLSkin skin = new XMLSkin(this, jarEntryUrl);
//						this.skins_.add(skin);
//					}
//				}
//			}
//		}
		
		
		/* Now..Lets check each sub directory */
		for (File f : configDir.listFiles())
		{
			if (f.isDirectory())
			{
				for (File sf : f.listFiles())
				{
					if (sf.getName().endsWith(SKIN_FILE_EXTENSION) == true)
					{
						String id = sf.getName();
						XMLSkin skin = new XMLSkin(this, sf.toURL(), sf.getParent() + File.separatorChar + sf.getName());
						this.skins_.add(skin);
					}
				}
			}
		}
		
		
//		/* Any files in the root directory with the .skn extenesion now get loaded, We do these
//		 * after the jar files to allow the override of an existing skin by it's name */
//		for (File f : configDir.listFiles())
//		{
//			/* If this file ends with the skin extension, then we'll add it */
//			if (f.getName().endsWith(SKIN_FILE_EXTENSION) == true)
//			{
//				XMLSkin skin = new XMLSkin(this, f.toURL());
//				this.skins_.add(skin);
//			}
//			
//		}
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.SkinFactory#getDefaultSkin()
	 *******************************************************/
	@Override
	public Skin getDefaultSkin() throws Exception
	{
		for (Skin skin : getAllSkins())
		{
			if (skin.getId().equals(this.getDefaultSkinId()) == true)
			{
				return skin;
			}
		}
		
		
		throw new Exception("This skin factory does not contain a valid skin with the ID: " + getDefaultSkinId()); 
	}

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.SkinFactory#getAllSkins()
	 *******************************************************/
	@Override
	public List<Skin> getAllSkins() throws Exception
	{
		return this.skins_;
	}
	
	

}
