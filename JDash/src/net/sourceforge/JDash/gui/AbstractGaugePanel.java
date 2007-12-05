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
package net.sourceforge.JDash.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sourceforge.JDash.Startup;
import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.comm.MonitorEventListener;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.gui.shapes.GlyphShape;
import net.sourceforge.JDash.gui.shapes.ImageShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinEvent;
import net.sourceforge.JDash.skin.SkinEventTrigger;
import net.sourceforge.JDash.util.UTIL;

/*******************************************************
 * This is the base class that ALL GaugePanel classes must
 * extend.  It is one of these that is placed into the
 * contentPanel of the DashboardFrame.  This class
 * automatically adds itself as a listener to to the monitor
 * for processingFinished events.  When this event is
 * received, this panel will call the updateDisplay method.
 *
 ******************************************************/
public abstract class AbstractGaugePanel extends JPanel 
{
	
	
	/**	This value defines the minimum number of milliseconds that must pass
	 * before a display update will be allowed to take place. */
	public static final int MINIMUM_UPDATE_INTEVAL = 100;
	
	public static final long serialVersionUID = 0L;
	
	/* The skin */
	private Skin skin_ = null;
	
	/* The data logger */
	private DataLogger logger_ = null;
	
	/* The owner dashboard frame */
	private DashboardFrame owner_ = null;
	
	
	/** this flag is sed by the logger playback by way of the owner frame. It's used to
	 * prevent screen redraws to increase performance on data exports */
	private boolean suspendGaugeDisplayUpdates_ = false;
	
	
	/*******************************************************
	 * Create a new gauge panel.   Make sure that when you initialize your panel, you
	 * add the necessary Parameter objects to both the monitor, and the logger.
	 * If you do NOT add them, then the monitor will not fetch any information, and the
	 * logger will not log any values.
	 * 
	 * @param backgroundShapes IN - the shapes to be drawn to make up
	 * the background of this entire panel
	 ******************************************************/
	public AbstractGaugePanel(DashboardFrame ownerFrame, Skin skin, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		this.owner_ = ownerFrame;
		this.skin_ = skin;
		this.logger_ = logger;

		
		/* Listen for Monitor events */
		monitor.addMonitorListener(new MonitorEventListener.MonitorEventAdapter()
		{
			@Override
			public void processingFinished()
			{
				updateDisplay();
			}
		});
		
		
		
	}

	/*******************************************************
	 * @return
	 *******************************************************/
	public DashboardFrame getDashboardFrame()
	{
		return this.owner_;
	}
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public Skin getSkin()
	{
		return this.skin_;
	}
	
	
	/*******************************************************
	 * @return
	 ******************************************************/
	public DataLogger getLogger()
	{
		return this.logger_;
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
	
	
	/*******************************************************
	 * Always return the size of the original background image.
	 * Override
	 * @see javax.swing.JComponent#getPreferredSize()
	 *******************************************************/
	@Override
	public Dimension getPreferredSize()
	{
		try
		{
			return this.getSkin().getWindowSize();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	

	
	/*******************************************************
	 * This method is fired when something has happened, that requires 
	 * a redraw or refresh.  This... is the heavy lifter.
	 *******************************************************/
	public abstract void updateDisplay();
	
	
	
	/*******************************************************
	 * call this method to suspend gauge updates.  This is 
	 * used primarily by the logg playback capability.  In order
	 * to increase performance, this method is called to
	 * prevent screen re-draws.  Once done, this method will
	 * re-enable the updates.
	 * @param suspend
	 *******************************************************/
	protected void suspendGaugeDisplayUpdates(boolean suspend)
	{
		this.suspendGaugeDisplayUpdates_ = suspend;
	}

	/*******************************************************
	 * The AbstractGauge checks this value before propogating
	 * a redraw.
	 * 
	 * @return
	 *******************************************************/
	public boolean isGaugeDisplayUpdateSuspended()
	{
		return this.suspendGaugeDisplayUpdates_;
	}
	
	

	
	/********************************************************
	 * A utility method to the paintShapes() method when you only have
	 * one shape to paint, and dont' want to keep makeing an array list.
	 * 
	 * @param g2
	 * @param shape
	 * @param awtShape
	 * @param scalingTransform
	 *******************************************************/
	protected void paintShape(Graphics2D g2, AbstractShape shape, Shape awtShape)
	{
		ArrayList<Shape> shapes = new ArrayList<Shape>();
		shapes.add(awtShape);
		this.paintShapes(g2, shape, shapes);
	}
	
	
	
	/********************************************************
	 * Use this method to do the actual drawing of the awt shape.
	 * This method will do alot of the common attribute settings
	 * for a generated shape.  The AbstractShape and shape
	 * genered from it often need to have color, line and
	 * text values set.  This method will do that for you
	 * automatically.  The shape is NOT recreated from the
	 * AbstractShape.  And it is up you you to have already
	 * applied any transforms.
	 * 
	 * @param g2 IN - the GC to draw to.  This is a copy of the actual GC
	 * 	so there is it's parametrs should not requrre re-setting.
	 * @param shape IN - the shape that is being painted.
	 * @param awtShape IN - the array list of AWT shapes that will be rendered.
	 *******************************************************/
	protected void paintShapes(Graphics2D g2, AbstractShape shape, ArrayList<Shape> awtShapes)
	{
		
		/* Paint each shape */
		for (Shape awtShape : awtShapes)
		{
			
			
			/* Image Shapes act a bit different */
			if (shape instanceof ImageShape)
			{
				ImageIcon icon = ((ImageShape)shape).getImageIcon();
				g2.drawImage(icon.getImage(), awtShape.getBounds().x, awtShape.getBounds().y, awtShape.getBounds().width, awtShape.getBounds().height, null);
			}
			else if (shape instanceof TextShape)
			{
				throw new RuntimeException("you cannot paint text with the paintShapes() method, you must use the paintGlyphs() method.");
			}
			else if (shape instanceof ButtonShape)
			{
				throw new RuntimeException("you cannot paint a button with the paintShapes() method.  This should have been added as a component.");
			}
			else
			{
				/* Fill the shape, if requested */
				if (shape.getAttribute(AbstractShape.PROPS.FILL_COLOR).startsWith("#") == true)
				{
					g2.setColor(Color.decode(shape.getAttribute(AbstractShape.PROPS.FILL_COLOR)));
					g2.fill(awtShape);
				}
		
				/* Set the line color */
				g2.setColor(Color.decode(shape.getAttribute(AbstractShape.PROPS.COLOR)));
		
				
				/* Apply the line width */
				g2.setStroke(new BasicStroke(Float.parseFloat(shape.getAttribute(AbstractShape.PROPS.LINE_WIDTH))));

				g2.draw(awtShape);
			}
			
		}

	}

	
	/********************************************************
	 * This paint method acts like the paintShapes() method, except
	 * it's designed for text glyphs, rather than AWT shapes.
s	 * @param g2 IN - the graphics context to draw to.s
	 * @param color IN - the color string to paint the text with.
	 * @param x IN - the x position to put the glyphs. They should have alrady been sized.
	 * @param y IN - the y position to put the glyphs, they should have already been sized.
	 * @param glyphs IN - the glyph vector to draw.
	 *******************************************************/
	protected void paintGlyphs(Graphics2D g2, String color, int x, int y, GlyphVector glyphs)
	{
		
		/* Set the line color */
		g2.setColor(Color.decode(color));
		

		/* Draw it */
		if (glyphs != null)
		{
			try
			{
				g2.drawGlyphVector(glyphs, x, y);
			}
			catch(Exception e)
			{
				// TODO
				e.printStackTrace();
				System.err.println("Warning, unable to draw the text glyphs ");
			}
		}

	}
	
}

