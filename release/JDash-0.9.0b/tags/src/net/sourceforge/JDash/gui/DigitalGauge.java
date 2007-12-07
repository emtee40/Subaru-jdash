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
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.StringParameter;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.TextShape;


/*******************************************************
 * This is a basic digital gauge.  A gigital gauge
 * will display text on the screen.
 * 
 ******************************************************/
public class DigitalGauge extends AbstractGauge implements PaintableGauge
{
	
	public static final long serialVersionUID = 0L;
	
	private TextShape textShape_ = null;
	
	private GlyphVector preRenderedGlyphVector_ = null;
//	private Rectangle preRenderedBounds_ = null;
	private Point preRenderedPoint_ = null;
	
//	private AffineTransform previousScalingTransform_ = null;
	
	private Double dDisplayValue_ = null;
	private String sDisplayValue_ = null;
	
	private Boolean lowOrHighHold_ = null;
	
	
	/*******************************************************
	 * Create a new digital gauges.
	 * 
	 * @param p IN - the parameter to display values for.
	 * @param parentPanel IN - the parent gauge panel.
	 * @param textShape IN - the text shape object this gauge will
	 * display.
	 ******************************************************/
	public DigitalGauge(Parameter p, Point point, TextShape textShape)
	{
		super(p, point);
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
			this.dDisplayValue_ = java.lang.Double.MAX_VALUE;
		}
		else
		{
			this.dDisplayValue_ = java.lang.Double.MIN_VALUE;
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
	private void preRender(Graphics2D g2, AffineTransform scalingTransform)
	{
		

		/* Setup the display value */
		if (this.lowOrHighHold_ != null)
		{
			/* Low value hold */
			if (this.lowOrHighHold_.equals(Boolean.FALSE))
			{
				this.dDisplayValue_ = Math.min(this.dDisplayValue_, getParameter().getResult());
			}
			else /* High value hold */
			{
				this.dDisplayValue_ = Math.max(this.dDisplayValue_, getParameter().getResult());
			}
		}
		else
		{
			
			if (getParameter() instanceof StringParameter)
			{
				this.sDisplayValue_ = getParameter().toString();
				this.textShape_.setValue(this.sDisplayValue_);
			}
			else
			{
				this.dDisplayValue_ = getParameter().getResult();
				this.textShape_.setValue(this.dDisplayValue_);
			}
			
		}
		

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
		Rectangle position = this.textShape_.createAWTShape().getBounds();
		position.x += getPosition().x;
		position.y += getPosition().y;
		position = scalingTransform.createTransformedShape(position).getBounds();
		this.preRenderedPoint_ = new Point(position.getBounds().x, position.getBounds().y);
		
//		/* The bounds, with the previous bounds added for drawing overlap */
//		Rectangle newBounds = this.preRenderedGlyphVector_.getPixelBounds(g2.getFontRenderContext(), position.getBounds().x, position.getBounds().y);
//		if (this.preRenderedBounds_ == null)
//		{
//			this.preRenderedBounds_ = newBounds;
//		}
//		else
//		{
//			this.preRenderedBounds_.add(newBounds);
//		}
//		
//		/* Send the re-draw area back */
//		return this.preRenderedBounds_;
	}

	
	/******************************************************
	 * Paint this gauge to the panel.
	 *******************************************************/
	public void paint(AbstractGaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{
		
		
		/* Pre-render the parts of this gauge */
		preRender(g2, scalingTransform);
		
		/* Paint the text onto the main panel */
		String color = this.textShape_.getAttribute(AbstractShape.PROPS.COLOR);
		panel.paintGlyphs(g2, color, this.preRenderedPoint_.x, this.preRenderedPoint_.y, this.preRenderedGlyphVector_);
		
		
		/* Set the pre-renderd bounds to the size of this glyph vector. So the next preGen can take into account for
		 * the bounds rect being created */
//		this.preRenderedBounds_ = this.preRenderedGlyphVector_.getPixelBounds(g2.getFontRenderContext(), this.preRenderedPoint_.x, this.preRenderedPoint_.y);
	}


}
