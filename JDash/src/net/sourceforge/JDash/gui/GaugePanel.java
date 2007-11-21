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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sourceforge.JDash.ecu.comm.BaseMonitor;
import net.sourceforge.JDash.ecu.comm.ECUMonitor;
import net.sourceforge.JDash.ecu.comm.MonitorEventListener;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.ImageShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.logger.DataLogger;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.util.UTIL;

/*******************************************************
 * A simple extension to a JPanel.  This simply draws
 * the skin background in place.  Like most JPanels, you can
 * add swing components to it's main area. But, you MUST
 * add a component with an original outter rectangle size value.
 * do not just call add(comp).  you MUST use add(comp, Rectangle);  
 *
 ******************************************************/
public class GaugePanel extends JPanel 
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
	
	/* This array holds the list of shapes that make up the background */
	private List<AbstractShape> backgroundShapes_ = null;
	
//	/* This list holds the array of gauges displayed on this panel */
//	private ArrayList<AbstractGauge> gauges_ = new ArrayList<AbstractGauge>();
	
	/* The background icon */
	private Image backgroundImage_ = null;

	/* The original image size */
	private Dimension originalImageDimension_ = new Dimension(1,1);

	/* We want to watch for the paint component event having a different window dimension,
	 * this way we can re-scale the icon as needed */
	private Dimension previousPaintDimension_ = new Dimension(0,0);
	
	/** This is the double buffered image.  Rather than create a new one each time, 
	 * we'll re-use this one. */
	private BufferedImage doubleBufferedImage_ = null;
	
	/** this flag is sed by the logger playback by way of the owner frame. It's used to
	 * prevent screen redraws to increase performance on data exports */
	private boolean suspendGaugeDisplayUpdates_ = false;
	
	/*******************************************************
	 * Create a new gauge panel. 
	 * @param backgroundShapes IN - the shapes to be drawn to make up
	 * the background of this entire panel
	 ******************************************************/
	public GaugePanel(DashboardFrame ownerFrame, Skin skin, BaseMonitor monitor, DataLogger logger) throws Exception
	{
		this.owner_ = ownerFrame;
		this.skin_ = skin;
//		this.monitor_ = monitor;
		this.logger_ = logger;
		
		/* Set the original size of the panel */
		this.originalImageDimension_ = getSkin().getWindowSize();
		
		/* Setup our layout as a null layout, because we do absolute placement */
		this.setLayout(new GaugeComponentLayout());
		
		/* The background shapes. */
		this.backgroundShapes_ = getSkin().getWindowShapes();
		
		
		/* And the background color */
		setOpaque(false);

		/* Kick off the generation of the background image. But. we're gong to loop until it gets through one iteration */
		generateBackgroundImage();
		
		
		/* Listen for Monitor events */
		monitor.addMonitorListener(new MonitorEventListener.MonitorEventAdapter()
		{
			@Override
			public void processingFinished()
			{
				updateDisplay();
			}
		});
		
		
		
//		/* Add each gauge from the skin */
//		String gaugeProblems = "";
//		for (int gaugeIndex = 0; gaugeIndex < getSkin().getGaugeCount(); gaugeIndex++)
//		{
//			try
//			{
//				this.gauges_.add(getSkin().createGauge(gaugeIndex, this));
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				gaugeProblems += "Unable to add gauge [" + gaugeIndex + "] from this skin. " + e.getMessage() + "\n";
//			}
//			
//		}
//
//		/* Report all gauge generation problems at once. */
//		if (gaugeProblems.length() > 0)
//		{
//			getDashboardFrame().showMessage(DashboardFrame.MESSAGE_TYPE.WARNING, "Gauge Setup Problem", gaugeProblems);
//		}
//		
		
		
//		/* Startup a forced update timer.  This timer simply does a forced frame redraw just in case
//		 * some of our graphics got out of sync or clipped. */
//		new Timer().scheduleAtFixedRate(new TimerTask()
//				{
//					public void run()
//					{
//						setBounds(getBounds());
//					};
//				}, 
//				1,1);
//				//FORCED_UPDATE_INTERVAL, FORCED_UPDATE_INTERVAL);
//		
		
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
	
//	/*******************************************************
//	 * @return
//	 *******************************************************/
//	public ECUMonitor getMonitor()
//	{
//		return this.monitor_;
//	}
	
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
		
//		/* force a re-gen on each gauge */
//		for (AbstractGauge gauge : getGauges())
//		{
//			if (gauge.getParameter() != null)
//			{
//				gauge.update(null, Boolean.TRUE);
//			}
//		}

		/* Tell the layout manager to relayout this container */
		getLayout().layoutContainer(this);
		
	}
	
	
//	/*******************************************************
//	 * Get the gauges this panel is holding.
//	 * @return the list of gauges.
//	 *******************************************************/
//	public List<AbstractGauge> getGauges()
//	{
//		return this.gauges_;
//	}
//	

	/*******************************************************
	 * Always return the size of the original background image.
	 * Override
	 * @see javax.swing.JComponent#getPreferredSize()
	 *******************************************************/
	@Override
	public Dimension getPreferredSize()
	{
		return this.originalImageDimension_;
	}
	
	
	/********************************************************
	 * Get the Width scaling ratio between the original background
	 * image and the current size of this panel.
	 * @return the current x axis scaling factor.
	 *******************************************************/
	public double getXScale()
	{
		Dimension d = UTIL.aspectScale(this.getSize(), this.originalImageDimension_);
		return d.getWidth() / this.originalImageDimension_.getWidth();
	}
	

	/*******************************************************
	 * Get the Height scaling ratio between the original background
	 * image and the current size of this panel.
	 * @return the current y axis scaling factor.
	 *******************************************************/
	public double getYScale()
	{
		Dimension d = UTIL.aspectScale(this.getSize(), this.originalImageDimension_);
		return d.getHeight() / this.originalImageDimension_.getHeight();
	}
	


	/*******************************************************
	 * Generate the background image, and put it into the member variable.
	 * Return a true if the image was loaded, and scaled.  False if it
	 * was not. A false isn't actually an error. It just means the image is
	 * still loading and scaling. An error will result in an exception
	 *******************************************************/
	private boolean generateBackgroundImage()
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
			BufferedImage bufferedImage = new BufferedImage(this.originalImageDimension_.width, this.originalImageDimension_.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bufferedImage.createGraphics();
			
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
	
	
	/*******************************************************
	 * This method is fired when something has happened, that requires 
	 * a redraw or refresh.  This... is the heavy lifter.
	 *******************************************************/
	private void updateDisplay()
	{

		repaint();

	}
	
	
	/******************************************************
	 * Override the paint method so we can draw the gauge.
	 *
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 *******************************************************/
	public void paintComponent(java.awt.Graphics g)
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

		/* Paint the background image */
		g2.drawImage(this.backgroundImage_, 0, 0, null);
		
		
		try
		{
			/* Paint each gauge */
			for (int index = 0; index < this.skin_.getGaugeCount(); index++)
			{
				AbstractGauge gauge = this.skin_.getGauge(index);
				gauge.paint(this, (Graphics2D)g2.create(), getScalingTransform());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
		
		/* Now, paint the double buffered image to the window */
		g.drawImage(this.doubleBufferedImage_, 0, 0, null);
		
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
	 * @param textShape IN - the text shape these glyphs represent. The color attibute is what is needed here.
	 * @param x IN - the x position to put the glyphs. They should have alrady been sized.
	 * @param y IN - the y position to put the glyphs, they should have already been sized.
	 * @param glyphs IN - the glyph vector to draw.
	 *******************************************************/
	protected void paintGlyphs(Graphics2D g2, TextShape textShape, int x, int y, GlyphVector glyphs)
	{
		
		/* Set the line color */
		String strColor = textShape.getAttribute(AbstractShape.PROPS.COLOR);
		g2.setColor(Color.decode(strColor));
		

		/* Draw it */
		if (glyphs != null)
		{
			try
			{
				g2.drawGlyphVector(glyphs, x, y);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.err.println("Warning, unable to draw the text glyphs for parameter value: " + textShape.getFormattedValue());
			}
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
	
	
}

