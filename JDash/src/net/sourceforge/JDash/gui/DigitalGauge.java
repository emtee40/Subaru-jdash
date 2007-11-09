/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge.java
 *  Aug 8, 2006
 *  $Id: DigitalGauge.java,v 1.4 2006/12/31 16:59:10 shaneapowell Exp $
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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.StringParameter;
import net.sourceforge.JDash.gui.shapes.TextShape;


/*******************************************************
 * This is a basic digital gauge.  A gigital gauge
 * will display text on the screen.
 * 
 ******************************************************/
public class DigitalGauge extends AbstractGauge
{
	
	public static final long serialVersionUID = 0L;
	
	private TextShape textShape_ = null;
	
	private GlyphVector preRenderedGlyphVector_ = null;
	private Rectangle preRenderedBounds_ = null;
	private Point preRenderedPoint_ = null;
	
//	private AffineTransform previousScalingTransform_ = null;
	
	private Object displayValue_ = "";
	
	private Boolean lowOrHighHold_ = null;
	
	
	/*******************************************************
	 * Create a new digital gauges.
	 * 
	 * @param p IN - the parameter to display values for.
	 * @param parentPanel IN - the parent gauge panel.
	 * @param textShape IN - the text shape object this gauge will
	 * display.
	 ******************************************************/
	public DigitalGauge(Parameter p, TextShape textShape)
	{
		super(p);
		
		this.textShape_ = textShape;
	}

	
	/*******************************************************
	 * Enable the hold feature of this guage.  Rather than display the
	 * current value, the high or low values will be displayed.
	 * 
	 * @param lowOrHigh IN - false will cause this gauge to be a "low" holding
	 * gauge. True will make it a high holding gauge.  use diableLowHighHold()
	 * to disable this feature.
	 *******************************************************/
	public void  enableLowHighHold(boolean lowOrHigh)
	{
		this.lowOrHighHold_ = lowOrHigh;
		
		if (lowOrHigh == false)
		{
			this.displayValue_ = java.lang.Double.MAX_VALUE + "";
		}
		else
		{
			this.displayValue_ = java.lang.Double.MIN_VALUE + "";
		}
		
	}
	
	
	/*******************************************************
	 *  Disable the low high hold feature.
	 *******************************************************/
	public void disableLowHighHold()
	{
		this.lowOrHighHold_ = null;
	}
	
	
	/*******************************************************
	 * Get the text shape that was used to create this gauge.
	 * @return the text shape.
	 *******************************************************/
	public TextShape getTextShape()
	{
		return this.textShape_;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.AbstractGauge#getBounds()
	 *******************************************************/
	private Rectangle preRender(Graphics2D g2, AffineTransform scalingTransform)
	{

		/* We'll need this */
//		Graphics2D g2 = (Graphics2D)getParentPanel().getGraphics();

		/* Setup the display value */
		if (this.lowOrHighHold_ != null)
		{
			/* Low value hold */
			if (this.lowOrHighHold_.equals(Boolean.FALSE))
			{
				this.displayValue_ = Math.min(new Double(this.displayValue_+""), getParameter().getResult());
			}
			else /* High value hold */
			{
				this.displayValue_ = Math.max(new Double(this.displayValue_+""), getParameter().getResult());
			}
		}
		else
		{
			
			if (getParameter() instanceof StringParameter)
			{
				this.displayValue_ = getParameter().toString();
			}
			else
			{
				this.displayValue_ = getParameter().getResult();
			}
			
		}
		
		/* Set the display text */
		this.textShape_.setValue(this.displayValue_);


		/* This isn't working */
//		if ((this.previousScalingTransform_ != null) && 
//			(scalingTransform.hashCode() == this.previousScalingTransform_.hashCode()) && 
//			(!force))
//		{
//			
//			this.previousScalingTransform_ = scalingTransform;
//			return null;
//		}
//		else
//		{
//			this.previousScalingTransform_ = scalingTransform;
//		}
//		
		
		/* Pre-Render the glyphs */
		this.preRenderedGlyphVector_ = this.textShape_.getGlyphVector(g2.getFontRenderContext());
		
		/* Now, scale to the panel size */
		for (int index = 0; index < this.preRenderedGlyphVector_.getNumGlyphs(); index++)
		{
			this.preRenderedGlyphVector_.setGlyphTransform(index, scalingTransform);
		}

		/* The Position */
		Shape position = this.textShape_.getShape().getBounds();
		position = scalingTransform.createTransformedShape(position);
		this.preRenderedPoint_ = new Point(position.getBounds().x, position.getBounds().y);
		
		/* The bounds, with the previous bounds added for drawing overlap */
		Rectangle newBounds = this.preRenderedGlyphVector_.getPixelBounds(g2.getFontRenderContext(), position.getBounds().x, position.getBounds().y);
		if (this.preRenderedBounds_ == null)
		{
			this.preRenderedBounds_ = newBounds;
		}
		else
		{
			this.preRenderedBounds_.add(newBounds);
		}
		
		/* Send the re-draw area back */
		return this.preRenderedBounds_;
	}

	
	/******************************************************
	 * Paint this gauge to the panel.
	 *******************************************************/
	public void paint(GaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{
		
		/* Pre-render the parts of this gauge */
		preRender(g2, scalingTransform);
		
		/* Paint the text onto the main panel */
		panel.paintGlyphs(g2, this.textShape_, this.preRenderedPoint_.x, this.preRenderedPoint_.y, this.preRenderedGlyphVector_);
		
		
		/* Set the pre-renderd bounds to the size of this glyph vector. So the next preGen can take into account for
		 * the bounds rect being created */
		this.preRenderedBounds_ = this.preRenderedGlyphVector_.getPixelBounds(g2.getFontRenderContext(), this.preRenderedPoint_.x, this.preRenderedPoint_.y);
	}


}
