/*******************************************************
 * 
 *  @author spowell
 *  MainPanel.java
 *  Aug 9, 2006
 *  $Id: GaugePanel.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;

import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.gui.AbstractGauge;
import net.sourceforge.JDash.gui.AbstractGaugePanel;
import net.sourceforge.JDash.gui.DashboardFrame;
import net.sourceforge.JDash.gui.PaintableGauge;
import net.sourceforge.JDash.gui.SwingComponentGauge;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.util.UTIL;

/*******************************************************
 * A simple extension to a JPanel.  This simply draws
 * the skin background in place.  Like most JPanels, you can
 * add swing components to it's main area. But, you MUST
 * add a component with an original outter rectangle size value.
 * do not just call add(comp).  you MUST use add(comp, Rectangle);  
 *
 ******************************************************/
public class XMLGaugePanel extends AbstractGaugePanel 
{
	
	
	/**	This value defines the minimum number of milliseconds that must pass
	 * before a display update will be allowed to take place. */
	public static final int MINIMUM_UPDATE_INTEVAL = 100;
	
	public static final long serialVersionUID = 0L;
		
	/* This array holds the list of shapes that make up the background */
	private List<AbstractShape> backgroundShapes_ = null;
	
	/* The background icon */
	private Image backgroundImage_ = null;


	/* We want to watch for the paint component event having a different window dimension,
	 * this way we can re-scale the icon as needed */
	private Dimension previousPaintDimension_ = new Dimension(0,0);
	
	/** This is the double buffered image.  Rather than create a new one each time, 
	 * we'll re-use this one. */
	private BufferedImage doubleBufferedImage_ = null;
	
	
	/*******************************************************
	 * Create a new gauge panel. 
	 * @param backgroundShapes IN - the shapes to be drawn to make up
	 * the background of this entire panel
	 ******************************************************/
	public XMLGaugePanel(DashboardFrame ownerFrame, XMLSkin skin, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		super(ownerFrame, skin, monitor, logger);
		
		/* Setup our layout as a null layout, because we do absolute placement */
		this.setLayout(new XMLGaugeComponentLayout());
		
		/* The background shapes. */
		this.backgroundShapes_ = skin.getWindowShapes();
		
		
		/* And the background color */
		setOpaque(false);

		/* Kick off the generation of the background image. But. we're gong to loop until it gets through one iteration */
		generateBackgroundImage();
		
		
		/* Add each gauges parameter to both the monitor and the logger */
		for (int index = 0; index < skin.getGaugeCount(); index++)
		{
			AbstractGauge gauge = skin.getGauge(index);
			monitor.addParam(gauge.getParameter());
			logger.addParameter(gauge.getParameter());
		}
		
		
		/* Add each SwingComponentGauge object */
		for (int index = 0; index < skin.getGaugeCount(); index++)
		{
			AbstractGauge gauge = skin.getGauge(index);
			if (gauge instanceof SwingComponentGauge)
			{
				for (Component c : ((SwingComponentGauge)gauge).getGaugeComponents())
				{
					this.add(c);
				}
			}
		}
		
		
	}
	
	
	
	
	/******************************************************
	 * When the updateDisplay message is received, we simply
	 * do a repaint of the panel.
	 * Override
	 * @see net.sourceforge.JDash.gui.AbstractGaugePanel#updateDisplay()
	 *******************************************************/
	@Override
	public void updateDisplay()
	{
		/* Tell this component to repaint itself, which results in each paintable
		 * gauge getting a paint() method call */
		repaint();
		
		/* Send each swing component gauge an updateDisplay message */
		try
		{
			for (int index = 0; index < ((XMLSkin)getSkin()).getGaugeCount(); index++)
			{
				AbstractGauge gauge = ((XMLSkin)getSkin()).getGauge(index);
				if (gauge instanceof SwingComponentGauge)
				{
					((SwingComponentGauge)gauge).updateDisplay();
				}
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	

	/*******************************************************
	 * Override
	 * @see java.awt.Component#setBounds(java.awt.Rectangle)
	 *******************************************************/
	@Override
	public void setBounds(Rectangle rect)
	{
		this.setBounds(rect.x, rect.y, rect.width, rect.height);
	}
	
	/*******************************************************
	 * Override
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 *******************************************************/
	@Override
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x, y, w, h);
		

		/* Tell the layout manager to relayout this container */
		getLayout().layoutContainer(this);
		
	}
	
	
	/********************************************************
	 * Get the Width scaling ratio between the original background
	 * image and the current size of this panel.
	 * @return the current x axis scaling factor.
	 *******************************************************/
	public double getXScale()
	{
		Dimension d = UTIL.aspectScale(this.getSize(), getPreferredSize());
		return d.getWidth() / getPreferredSize().getWidth();
	}
	

	/*******************************************************
	 * Get the Height scaling ratio between the original background
	 * image and the current size of this panel.
	 * @return the current y axis scaling factor.
	 *******************************************************/
	public double getYScale()
	{
		Dimension d = UTIL.aspectScale(this.getSize(), getPreferredSize());
		return d.getHeight() / getPreferredSize().getHeight();
	}
	


	/*******************************************************
	 * Generate the background image, and put it into the member variable.
	 * Return a true if the image was loaded, and scaled.  False if it
	 * was not. A false isn't actually an error. It just means the image is
	 * still loading and scaling. An error will result in an exception
	 *******************************************************/
	private boolean generateBackgroundImage() throws Exception
	{
		
		/* If the width isn't yet known, then the image isn't loaded yet.. but 
		 * what this tells us, is that an image is in the process of loading,
		 * so don't bother doing another load */
		if ((this.backgroundImage_ != null) && (this.backgroundImage_.getWidth(null) == -1))
		{
			return false;
		}
		
		
		/* If the dimensions have changed, then resize the image */
		if (this.previousPaintDimension_.equals(this.getSize()) == false)
		{

			/* Create a new GC to draw into */
			BufferedImage bufferedImage = new BufferedImage(getPreferredSize().width, getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bufferedImage.createGraphics();
			
			/* Draw a big rect for our initial background */
			g2.setColor(((XMLSkin)getSkin()).getBackgroundColor());
			g2.setBackground(((XMLSkin)getSkin()).getBackgroundColor());
			Rectangle bgRect = new Rectangle(getPreferredSize().width, getPreferredSize().height);
			g2.fill(bgRect);
			g2.draw(bgRect);

			
			/* Draw each shape */
			for (AbstractShape shape : this.backgroundShapes_)
			{
				
				/* Text Shape */
				if (shape instanceof TextShape)
				{
					TextShape textShape = (TextShape)shape;
					Shape awtShape = shape.getShape();
					paintGlyphs(g2, textShape, awtShape.getBounds().x, awtShape.getBounds().y, textShape.getGlyphVector(g2.getFontRenderContext()));
				}
				else
				{
					/* All other background shapes */
					paintShape(g2, shape, shape.getShape());
				}
			
			}
			
			/* Set our new background image */
			Dimension d = UTIL.scale(this, new ImageIcon(bufferedImage), UTIL.SCALE.ASPECT);
			this.backgroundImage_ = bufferedImage.getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH);
		
			/* force the creation of a new DB image */
			this.doubleBufferedImage_ = null;
		}

		/* Setup a new previous size for the next call to this method */
		this.previousPaintDimension_ = this.getSize();


		return true;

	}
	
	
	/******************************************************
	 * Override the paint method so we can draw the gauge.
	 *
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 *******************************************************/
	public void paintComponent(java.awt.Graphics g)
	{
		
		
		if (isGaugeDisplayUpdateSuspended())
		{
			return;
		}
		
		try
		{

		
			/* Generate the background image.. if needed.  This method knows if the panel has resized */
			generateBackgroundImage();
	
			/* Tell the panel to paint it's default background */
			super.paintComponent(g);
			
	
			
			/* Create an instance double buffer image, correctly sized */
			if (this.doubleBufferedImage_ == null)
			{
				this.doubleBufferedImage_ = new BufferedImage((int)this.getSize().getWidth(), (int)this.getSize().getHeight(), BufferedImage.TYPE_INT_RGB);
			}
			
			/* Get the g2 from the db image */
			Graphics2D g2 = (Graphics2D)this.doubleBufferedImage_.getGraphics();

	
			/* clear the image contents, and Paint the background image */
			g2.drawImage(this.backgroundImage_, 0, 0, null);
	
		
			/* Paint each PaintableGauge */
			for (int index = 0; index < ((XMLSkin)getSkin()).getGaugeCount(); index++)
			{
				AbstractGauge gauge = ((XMLSkin)getSkin()).getGauge(index);
				if (gauge instanceof PaintableGauge)
				{
					((PaintableGauge)gauge).paint(this, (Graphics2D)g2.create(), getScalingTransform());
				}
			}
			
			/* Now, paint the double buffered image to the window */
			((Graphics2D)g).drawImage(this.doubleBufferedImage_, 0, 0, null);

		}
		catch(Exception e)
		{
			
			this.suspendGaugeDisplayUpdates(true);
			Startup.showException(e, true);
		}
			
		
		
	}

	

	/*******************************************************
	 * Get the scaling transform for this panel
	 * @return the current scaling transform.
	 ******************************************************/
	public AffineTransform getScalingTransform()
	{
		AffineTransform at = AffineTransform.getScaleInstance(getXScale(), getYScale());
		return at;

	}
	

	
}

