/*******************************************************
 * 
 *  @author spowell
 *  CenterLayout.java
 *  Aug 14, 2006
 *  $Id: GaugeLayout.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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
package net.sourceforge.JDash.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;


import net.sourceforge.JDash.util.UTIL;

/*******************************************************
 * A simple implementation of the layout manager to place our 
 * gauge panel in the center of the owner panel.
 ******************************************************/
public class GaugeLayout implements LayoutManager2
{
	
	/*******************************************************
	 * create a new gauge layout.
	 ******************************************************/
	public GaugeLayout()
	{
	}
	

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 *******************************************************/
	public void addLayoutComponent(Component arg0, Object arg1)
	{
		// do nothing

	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 *******************************************************/
	public Dimension maximumLayoutSize(Container arg0)
	{
		throw new RuntimeException("Not Implemented");
	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 *******************************************************/
	public float getLayoutAlignmentX(Container arg0)
	{
		throw new RuntimeException("Not Implemented");
	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 *******************************************************/
	public float getLayoutAlignmentY(Container arg0)
	{
		throw new RuntimeException("Not Implemented");	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 *******************************************************/
	public void invalidateLayout(Container arg0)
	{
		// do nothing
	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 *******************************************************/
	public void addLayoutComponent(String arg0, Component arg1)
	{
		throw new RuntimeException("Not Implemented");
	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 *******************************************************/
	public void removeLayoutComponent(Component arg0)
	{
		throw new RuntimeException("Not Implemented");
	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 *******************************************************/
	public Dimension preferredLayoutSize(Container arg0)
	{
		throw new RuntimeException("Not Implemented");	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 *******************************************************/
	public Dimension minimumLayoutSize(Container arg0)
	{
		throw new RuntimeException("Not Implemented");	}

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 *******************************************************/
	public void layoutContainer(Container container)
	{
		/* We only work with a single gauge panel */
		if (container.getComponentCount() != 1)
		{
			throw new RuntimeException("Incorrect number of components in container.  There can be only one");
		}

		Component component = container.getComponent(0);
		
		if (component instanceof GaugePanel == false)
		{
			throw new RuntimeException("The Gauge Layout only works with GaugePanel objects.  This container is holding a: " + component.getClass().getName());
		}
		
		/* Cast our panel */
		GaugePanel panel = (GaugePanel)component;
		
		/* Get the frame dimensiont, and the prefered size of the panel */
		Dimension frameDimension = container.getSize();
		Dimension gaugeDimension = panel.getPreferredSize();
				

		/* Calculate the aspect ration change */
		Dimension scaledDimension = UTIL.aspectScale(frameDimension, gaugeDimension);
		
		/* Calculate the x and y to center our panel */
		int x = 0;
		int y = 0;
		
		/* Frame is wider than panel */
		if (frameDimension.width > scaledDimension.width)
		{
			x = (frameDimension.width - scaledDimension.width) / 2;
		}
		
		/* Frame height is higher than panel */
		if (frameDimension.height > scaledDimension.height)
		{
			y = (frameDimension.height - scaledDimension.height) / 2;
		}

		
		
		/* Set the bounds of our panle */
		panel.setBounds(x, y, scaledDimension.width, scaledDimension.height);

		
	}

}
