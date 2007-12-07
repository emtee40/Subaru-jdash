/*******************************************************
 * 
 *  @author spowell
 *  CenterLayout.java
 *  Aug 14, 2006
 *  $Id: GaugeComponentLayout.java,v 1.4 2006/09/14 02:03:43 shaneapowell Exp $
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;



/*******************************************************
 * This layout manager will place components on the
 * panel according to their scalled relative location.
 ******************************************************/
public class XMLGaugeComponentLayout implements LayoutManager2
{
	
	private HashMap<Component, Rectangle> compMap_ = new HashMap<Component, Rectangle>();
	
	/*******************************************************
	 * create a new gauge layout.
	 ******************************************************/
	public XMLGaugeComponentLayout()
	{
	}
	

	/*******************************************************
	 * Override
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 *******************************************************/
	public void addLayoutComponent(Component comp, Object obj)
	{
		
		/* Get the current bounds, and store them as the original bounds rect */
		this.compMap_.put(comp, comp.getBounds());

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
		
		if (container instanceof XMLGaugePanel == false)
		{
			throw new RuntimeException("An XMLGaugeComponentLayout manager can only be applied to an XMLGaugePanel.");
		}
		
		/* we only opoerate on gague panels */
		XMLGaugePanel gaugePanel = (XMLGaugePanel)container;
		
		/* Place each child comonent according to it's preferred rectangle, 
		 * but adjusting for the scale */
		for (Component comp : container.getComponents())
		{
			/* Get the original rectangle */
			Rectangle origRect = this.compMap_.get(comp);

			/* Setup the scaled rect */
			Rectangle rect = new Rectangle();
			rect.x = (int)(origRect.getX() * gaugePanel.getXScale());
			rect.y = (int)(origRect.getY() * gaugePanel.getYScale());
			rect.width = (int)(origRect.getWidth() * gaugePanel.getXScale());
			rect.height = (int)(origRect.getHeight() * gaugePanel.getYScale());

			/* Apply the scaled bounds */
			comp.setBounds(rect);
			
		}

	}

}
