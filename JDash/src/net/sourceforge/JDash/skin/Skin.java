/*******************************************************
 * 
 *  @author spowell
 *  Skin.java
 *  Aug 8, 2006
 *  $Id: Skin.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.skin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.GaugePanel;
import net.sourceforge.JDash.gui.shapes.AbstractShape;


/*******************************************************
 * This is the abstract skin class.  From this, we
 * can get all the skinnable attributes used
 * to define the user interface.
 ******************************************************/
public abstract class Skin
{
	
	public static final String STATE_WINDOW 		= "window";
	public static final String STATE_FULLSCREEN 	= "fullscreen";

	/* A human readable string name for this Skin */
	private SkinFactory ownerFactory_ = null;
	
	/* The cache of images */
	private HashMap<String, ImageIcon> imageCache_ = new HashMap<String, ImageIcon>();

	/******************************************************
	 * create a new skin class.  Don't do anything special
	 * in the constructor. Because you can't be sure that
	 * the necessary values are yet ready in the sEtup class.
	 * do you init in the init() method.s
	 * @param ownerFactory IN - the factory that created this skin
	 * @param id IN - a unique ID for this skin.
	 ******************************************************/
	public Skin(SkinFactory ownerFactory)
	{
		this.ownerFactory_ = ownerFactory;
	}
	
	
	/********************************************************
	 * Get the factory that created this skin.
	 * @return
	 *******************************************************/
	public SkinFactory getOwnerFactory()
	{
		return this.ownerFactory_;
	}
	
	/*******************************************************
	 * get the unique ID of this skin. You need to try very hard
	 * to make this unique among all possible skins.  I suggest
	 * you include the full package and class name in this if you
	 * can.
	 * @return
	 *******************************************************/
	public abstract String getId();
	
	
	/********************************************************
	 * @return
	 *******************************************************/
	public abstract String getName();
	
	/*******************************************************
	 * @param desc
	 *******************************************************/
	public abstract String getDescription();
	
	
	/*******************************************************
	 * Override
	 * @see java.lang.Object#toString()
	 *******************************************************/
	public String toString()
	{
		return getName();
	}
	
	
	/********************************************************
	 * Get the initial window startup state.  This will be one 
	 * of 2 words. "fullscreen" or "window". But, the "window"
	 * value can be suffixed with a scale value.  eg. "window:26"
	 * will start in window mode and scale the initial size to 26% 
	 * of the screen size.
	 * 
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract String getWindowStartupState() throws Exception;
	
	/*******************************************************
	 * return the original dimensions of the desired window. 
	 * This is the width and height that the gauge panel
	 * will prefer to be sized to. All auto-resizing will
	 * be scaled to maintain the aspect ratio that this
	 * window size represents.  When window shapes are drawn
	 * to the background window, this dimenstion defines the
	 * coords that they are drawn into.
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract Dimension getWindowSize() throws Exception;
	
	
	/*******************************************************
	 * get the list of shapes that make up the background for the
	 * primary gauge panel.
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract List<AbstractShape> getWindowShapes() throws Exception;
	
	/*******************************************************
	 * Get the default background color to be fill into the 
	 * entire frame.
	 * @return
	 * @throws Exception
	 *******************************************************/
	public abstract Color getBackgroundColor() throws Exception;
	
	/********************************************************
	 * Images are requested via resource names.  If an 
	 * image is requested, it is the job of the extended class
	 * to return the URL to the image desired according to
	 * the imageResource parameter.  Once the image for
	 * the given resoure has been loaded once, it will be
	 * cached by this class, and returned from the class for
	 * all future requests.
	 * 
	 * @param imageName IN - the name of the image to return
	 * @return the URL to the image.
	 * @throws If there was any sort of problem getting the image.
	 *******************************************************/
	public abstract URL getImageUrl(String imageName) throws Exception;
	
	/********************************************************
	 * the getImage() method is final to enforce caching of images.
	 * Once an image is loaded, it's cached into a hashmap, and returned
	 * from it for future calls.
	 * 
	 * @param imageName IN - the name of the image to be fetched.
	 * @return
	 *******************************************************/
	public final ImageIcon getImage(String imageName) throws Exception
	{
		
		try
		{
			
			/* Look first in the cache */
			if (this.imageCache_.containsKey(imageName) == true)
			{
				return this.imageCache_.get(imageName);
			}
			
			
			/* Load the image */
			URL imageUrl = getImageUrl(imageName);
			if (imageUrl == null)
			{
				throw new Exception("image [" + imageName + "] does not appear to exist");
			}
			ImageIcon image = new ImageIcon(getImageUrl(imageName));
			
			/* Wait for the image to load */
			while (image.getImageLoadStatus() == MediaTracker.LOADING);

			/* We gotta have something!! */
			if (image.getImageLoadStatus() != MediaTracker.COMPLETE)
			{
				throw new RuntimeException("Unable to load image: " + getImageUrl(imageName));
			}
			
			/* Cache the image and return it */
			this.imageCache_.put(imageName, image);
			return image;

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}
	
	
	/*******************************************************
	 * return the number of gauges present in this skin.
	 * Throw an exception if a problem occurs.
	 * @return
	 *******************************************************/
	public abstract int getGaugeCount() throws Exception;
	
	
	/********************************************************
	 * get the guage at the given index. Throw an exception if
	 * a problem occurs.  do NOT return null.
	 * @param index IN - the index of which gauge to create.
	 * @param parentPanel IN - the parent panel this guage will belong to
	 * @return
	 *******************************************************/
	public abstract AbstractGauge createGauge(int index, GaugePanel parentPanel) throws Exception;

}
