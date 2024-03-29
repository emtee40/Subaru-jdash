/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge.java
 *  Aug 8, 2006
 *  $Id: LEDGauge.java,v 1.3 2006/09/14 02:03:43 shaneapowell Exp $
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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.skin.SkinEvent;
import net.sourceforge.JDash.skin.SkinEventListener;


/*******************************************************
 * This is a basic LED style of gauge. Although
 * the original intent of this gauge was to emulate
 * the type of LEd display you see on a stereo, it has
 * the ability to do so much more.  To put it simply, each LED
 * of this gauge is programmed to turn itself on and off according
 * the parameter value range it's been programmed with. An LED
 * gague can have as few as one LED, and as many as.. well... alot.
 * Perhaps you want a single LED to turn on and off with a simple
 * bitcheck value. OR perhaps you want a bunch of LEDs to grow
 * and shrink like an A/F gauge.  This is the clas for you.
 * 
 ******************************************************/
public class LEDGauge extends AbstractGauge implements PaintableGauge, SkinEventListener
{
	
	public static final long serialVersionUID = 0L;
	
	/** For convience only, this is simply a reference to the same object in AnalogGauge */
	public static final String ACTION_HIGH_RESET = AnalogGauge.ACTION_HIGH_RESET;

	/** For convience only, this is simply a reference to the same object in AnalogGauge */
	public static final String ACTION_LOW_RESET = AnalogGauge.ACTION_LOW_RESET;
	
	/** A position code */
	public static final String POSITION_CENTER = "center";

	/** A position code */
	public static final String POSITION_LEFT = "left";
	
	/** A position code */
	public static final String POSITION_RIGHT = "right";
	
	/** A position code */
	public static final String POSITION_TOP = "top";
	
	/** A position code */
	public static final String POSITION_BOTTOM = "bottom";
	
	
	private ArrayList<LED> leds_ = new ArrayList<LED>();
	
	private ArrayList<AbstractShape> lowNeedleShapes_ = new ArrayList<AbstractShape>();
	private ArrayList<AbstractShape> highNeedleShapes_ = new ArrayList<AbstractShape>();
	
	private ArrayList<AbstractShape> preGenShapes_ = null;
	private ArrayList<Shape> preGenAwtShapes_ = null;
	
	/* The last time the needle was reset */
	private long lowNeedleLastReset_ = 0l;
	private long highNeedleLastReset_ = 0l;
	
	/* The delay time between needle resets */
	private int lowNeedleResetDelay_ = 0;
	private int highNeedleResetDelay_ = 0;
	
	/* Remember which LED has been assigned the low and high indicators */
	private LED lowNeedleLed_ = null;
	private LED highNeedleLed_ = null;
	
	/* We'll need to remember the needle bounds for when the LED isn't litup */
	private Rectangle highNeedleLedBounds_ = null;
	private Rectangle lowNeedleLedBounds_ = null;
	
	/* The relative position codes for the high and low needles */
	private String highNeedlePosition_ = POSITION_CENTER;
	private String lowNeedlePosition_ = POSITION_CENTER;

	/******************************************************
	 * Create a new LED Gauge
	 * @param p IN - the parameter to display values for.
	 * @param parentPanel IN - the owner gauge panel.
	 ******************************************************/
	public LEDGauge(Parameter p, Point point)
	{
		super(p, point);
	}

	
	/*******************************************************
	 * Add an led to this led gauge.
	 * 
	 * @param led IN - the LED shapes that make up this gauge.
	 *******************************************************/
	public void addLed(LED led)
	{
		this.leds_.add(led);
		
		/* We need to start with something? This is as good an LED as any, 
		 * besides, the reset times are set to 0, so the low and high needles
		 * will be set at the first draw */
		if (lowNeedleLed_ == null)
		{
			this.lowNeedleLed_ = led;
			this.highNeedleLed_ = led;
		}
	}
	
	
	
	/*******************************************************
	 * Add the low needle shape.  This like an static indicator shwing the
	 * lowest LED that was light up.
	 * @param shape
	 *******************************************************/
	public void addLowNeedleShape(AbstractShape shape)
	{
		// TODO
		if (shape instanceof TextShape)
		{
			throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
		}
		
		this.lowNeedleShapes_.add(shape);
	}
	
	/*******************************************************
	 * Add the high needle shape. This will show the highest LED that
	 * was light up.
	 * @param shape
	 *******************************************************/
	public void addHighNeedleShape(AbstractShape shape)
	{
		// TODO
		if (shape instanceof TextShape)
		{
			throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
		}
		
		this.highNeedleShapes_.add(shape);
	}
	
	
	/*******************************************************
	 * set the low and high needle reset delays.   This is the time
	 * in ms that will pass before the needle will be reset.  If a value of -1 is
	 * passed, then the needles will NOT reset.  You'll have to reset them manually,
	 * with the resetNeedle() methods, or add a user button with addButton()
	 * @param lowNeedleResetDelay IN -the delay in ms, -1 to never reset.
	 * @param highNeedleResetDelay IN - the delay in ms, -1 to never reset.
	 *******************************************************/
	public void setNeedleResetDelays(int lowNeedleResetDelay, int highNeedleResetDelay)
	{
		if (lowNeedleResetDelay == -1)
		{
			lowNeedleResetDelay = Integer.MAX_VALUE;
		}
				
		if (highNeedleResetDelay == -1)
		{
			highNeedleResetDelay = Integer.MAX_VALUE;
		}
		
		this.lowNeedleResetDelay_ = lowNeedleResetDelay;
		this.highNeedleResetDelay_ = highNeedleResetDelay;
	}
	
	
	/*******************************************************
	 * @param position
	 *******************************************************/
	public void setHighNeedlePosition(String position)
	{
		this.highNeedlePosition_ = position;
	}
	
	/*******************************************************
	 * @param position
	 *******************************************************/
	public void setLowNeedlePosition(String position)
	{
		this.lowNeedlePosition_ = position;
	}
	
	/********************************************************
	 * Force the reset of the low needle.
	 *******************************************************/
	private void resetLowNeedle()
	{
		this.lowNeedleLastReset_ = 0L;
	}
	
	
	/*******************************************************
	 * force the reset of the hight needle 
	 ******************************************************/
	private void resetHighNeedle()
	{
		this.highNeedleLastReset_ = 0L;
	}
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.skin.SkinEventListener#actionPerformed(net.sourceforge.JDash.skin.SkinEvent)
	 *******************************************************/
	public void actionPerformed(SkinEvent e)
	{
		if (SkinEvent.DESTINATION_ALL.equals(e.getDestination()))
		{
			
			if (ACTION_HIGH_RESET.equals(e.getAction()))
			{
				this.resetHighNeedle();
			}
			
			if (ACTION_LOW_RESET.equals(e.getAction()))
			{
				this.resetLowNeedle();
			}
		}
		
	}
	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.AbstractGauge#getBounds()
	 *******************************************************/
	private Rectangle preRender(AffineTransform scalingTransform, boolean force)
	{
		
		/* Clear the current list */
		this.preGenShapes_ = new ArrayList<AbstractShape>();
		this.preGenAwtShapes_ = new ArrayList<Shape>();
		

		/* Get the current value */
		double value = getParameter().getResult();
		
		/* The final outter bounds */
		Rectangle bounds = null;
		
		
		/* The Highet LED and it's AWT bounds */
		LED highestLitLED = null;
		Rectangle highestLitLEDBounds = null;
		
		/* The relative position translate transform */
		AffineTransform relativeTranslate = AffineTransform.getTranslateInstance(this.getPosition().getX(), getPosition().getY());
		
		/* For each LED, find out what ones need drawing */
		for (LED led : this.leds_)
		{
			
			boolean lightIt = false;
			
			/* If the parameter value is within the range for this LED, then light it up */
			if ((value >= led.getMin()) && (value <= led.getMax()))
			{
				lightIt = true;
			}
			
			/* Watch also for reverse LEDs */
			if (led.getMin() > led.getMax())
			{
				if ((value <= led.getMin() && value >= led.getMax()))
				{
					lightIt = true;
				}
			}
			
			if (lightIt)
			{
			
				
				/* Watch for the highest litup LED, We'll need this for the high and low needles. */
				if ((highestLitLED == null) || (led.getMin() > highestLitLED.getMin()))
				{
					highestLitLED = led;
					highestLitLEDBounds = null;
				}
				
				/* If this LEDs ranges are in range, then draw it's shapes */
				for (AbstractShape shape : led.getShapes())
				{
							
					/* Get the awt shape */
					Shape awtShape = shape.createAWTShape();

					/* Then the translate transform */
					awtShape = relativeTranslate.createTransformedShape(awtShape);

					/* Now apply the panels scaling transform */
					awtShape = scalingTransform.createTransformedShape(awtShape);

					/* Add it to our cache */
					this.preGenShapes_.add(shape);
					this.preGenAwtShapes_.add(awtShape);
					
					/* Add to the bounds */
					if (bounds == null)
					{
						bounds = (Rectangle)awtShape.getBounds().clone();
					}
					else
					{
						bounds.add(awtShape.getBounds());
					}
					
					/* Add up the highest LED bounds */
					if (highestLitLED == led)
					{
						if (highestLitLEDBounds == null)
						{
							highestLitLEDBounds = (Rectangle)awtShape.getBounds().clone();
						}
						else
						{
							highestLitLEDBounds.add(awtShape.getBounds());
						}
					}
					
					
				} /* end for shape */
				
			} /* end if draw LED */
			
		}
		
		
		
		/* Now that we know the highest light up LED, we can use it to calculate the high and low needles.
		 * Caution... At initial startup, we might not have a high LED yet light up */
		
		
		/* Calculate the high needle.  It's simply the LED with the highest MIN value, unless the timeout has been hit, then
		 * it's hew new high LED value.  Unless the timeout has fired, then it's the current High LED */
		if ((this.highNeedleShapes_.size() > 0 ) &&
			(highestLitLED != null) &&
			((highestLitLED.getMin() >= this.highNeedleLed_.getMin())  || 
			 (System.currentTimeMillis() > this.highNeedleLastReset_ + this.highNeedleResetDelay_)))
		{
			this.highNeedleLed_= highestLitLED;
			this.highNeedleLastReset_ = System.currentTimeMillis();
			this.highNeedleLedBounds_ = (Rectangle)highestLitLEDBounds.clone();
			
		}
		
		/* Calculate the low needle.  It's the current high LED if it's MIN value is lower than the currently set low LED.  
		 * Unless the timeout has fired, then it's the current HIGH LED */
		if ((this.lowNeedleShapes_.size() > 0 ) &&
			(highestLitLED != null) &&
			((highestLitLED.getMin() <= this.lowNeedleLed_.getMin())  || 
			 (System.currentTimeMillis() > this.lowNeedleLastReset_ + this.lowNeedleResetDelay_)))
		{
			this.lowNeedleLed_= highestLitLED;
			this.lowNeedleLastReset_ = System.currentTimeMillis();
			this.lowNeedleLedBounds_ = (Rectangle)highestLitLEDBounds.clone();
		}

		
	
		/* Add the high needle to the draw shapes */
		if ((this.highNeedleLedBounds_ != null) && (this.highNeedleShapes_.size() > 0))
		{
			
			for (AbstractShape shape : highNeedleShapes_)
			{
						
				/* Get the awt shape */
				Shape awtShape = shape.createAWTShape();
				
				/* Now apply the panels scaling transform */
				awtShape = scalingTransform.createTransformedShape(awtShape);
				
				/* Now, apply the necessary translate tranform to place over the desired LED */
				//AffineTransform translateTransform = AffineTransform.getTranslateInstance(this.highNeedleLedBounds_.getCenterX(), this.highNeedleLedBounds_.getCenterY());
				AffineTransform translateTransform = getHighLowTransform(this.highNeedleLedBounds_, this.highNeedlePosition_);
				awtShape = translateTransform.createTransformedShape(awtShape);
				
				/* Add it to our cache */
				this.preGenShapes_.add(shape);
				this.preGenAwtShapes_.add(awtShape);
				
				/* Add to the main bounds */
				if (bounds == null)
				{
					bounds = (Rectangle)awtShape.getBounds().clone();
				}
				else
				{
					bounds.add(awtShape.getBounds());
				}
				
			} /* end for shape */
		}
		
		
		
		/* Add the low needle to the draw shapes */
		if ((this.lowNeedleLedBounds_ != null) && (this.lowNeedleShapes_.size() > 0))
		{
			
			
			for (AbstractShape shape : lowNeedleShapes_)
			{
						
				/* Get the awt shape */
				Shape awtShape = shape.createAWTShape();
				
				/* Now apply the panels scaling transform */
				awtShape = scalingTransform.createTransformedShape(awtShape);
				
				/* Now, apply the necessary translate tranform to place over the desired LED */
				//AffineTransform translateTransform = AffineTransform.getTranslateInstance(this.lowNeedleLedBounds_.getCenterX(), this.lowNeedleLedBounds_.getCenterY());
				AffineTransform translateTransform = getHighLowTransform(this.lowNeedleLedBounds_, this.lowNeedlePosition_);
				awtShape = translateTransform.createTransformedShape(awtShape);
				
				/* Add it to our cache */
				this.preGenShapes_.add(shape);
				this.preGenAwtShapes_.add(awtShape);
				
				/* Add to the main bounds */
				if (bounds == null)
				{
					bounds = (Rectangle)awtShape.getBounds().clone();
				}
				else
				{
					bounds.add(awtShape.getBounds());
				}
				
				
			} /* end for shape */
		}
		

		/* we have to return something */
		if (bounds == null)
		{
			return new Rectangle(0,0,0,0);
		}
		else
		{
			return bounds;
		}

	}

	
	
	/******************************************************
	 * 
	 *******************************************************/
	public void paint(AbstractGaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{

		preRender(scalingTransform, true);
		
		for (int index = 0; index < this.preGenAwtShapes_.size(); index++)
		{
			// TODO
			if (this.preGenShapes_.get(index) instanceof TextShape)
			{
				//panel.paintGlyphs(g2, this.textShape_, this.preRenderedPoint_.x, this.preRenderedPoint_.y, this.preRenderedGlyphVector_);
			}
			else
			{
				panel.paintShape(g2, this.preGenShapes_.get(index), this.preGenAwtShapes_.get(index));
			}
		
		}
		
		
	}

	
	/********************************************************
	 * This utility method will calculate the the correct x/y translate transform
	 * from the provided rect for the given position code.  The returned transform
	 * is intended to be used on the high or low needle shapes.
	 * @param rect
	 * @param position
	 * @return
	 *******************************************************/
	private AffineTransform getHighLowTransform(Rectangle rect, String position)
	{
	
		double x = 0.0;
		double y = 0.0;
		
		if (POSITION_CENTER.equals(position))
		{
			x = rect.getX() + (rect.getWidth() / 2);
			y = rect.getY() + (rect.getHeight() / 2);
		}
		else if (POSITION_LEFT.equals(position))
		{
			x = rect.getX();
			y = rect.getY() + (rect.getHeight() / 2);
		}
		else if (POSITION_RIGHT.equals(position))
		{
			x = rect.getX() + rect.getWidth();
			y = rect.getY() + (rect.getHeight() / 2);
		}
		else if (POSITION_TOP.equals(position))
		{
			x = rect.getX() + (rect.getWidth() / 2);
			y = rect.getY();
		}
		else if (POSITION_BOTTOM.equals(position))
		{
			x = rect.getX() + (rect.getWidth() / 2);
			y = rect.getY() + rect.getHeight();
		}
		else
		{
			throw new RuntimeException("The position code [" + position + "] provided for the high/low needle on the LED Gauge for parameter [" + this.getParameter().getName() + "] is not a valid code.");
		}
		
		return AffineTransform.getTranslateInstance(x, y);
	}
	
	/*******************************************************
	 * Since each LEd Gauge is made up of 1-n LEDs, this
	 * class will hold the configuration values to represent
	 * a single LED. The min/max values define a range that the 
	 * LED will respond to.  If you set the min to a value higher
	 * than the max, then this LEd will respond to values OUTSIDE
	 * the min/max range.
	 * <br>
	 * <pre>
	 * eg. min/max = 5-20
	 *   value = 10 : LED rendered
	 *   value = 20 : LED rendered
	 *   value = 2 : LED NOT rendered
	 *   value = 25 : LED NOT rendered
	 *   
	 *   min/max = 10-5
	 *     value = 6 : LED NOT rendered
	 *     value = 1 : LED Rendered
	 *  </pre>
	 ******************************************************/
	public static class LED
	{
		private double min_ = 0;
		private double max_ = 0;
		private ArrayList<AbstractShape> shapes_ = new ArrayList<AbstractShape>();
		
		/*******************************************************
		 * create a new LED.
		 * @param min IN - the minimum value that this LED will turn on with.
		 * @param max IN - the maximum value that this LEd will turn on with.
		 ******************************************************/
		public LED(double min, double max)
		{
			this.min_ = min;
			this.max_ = max;
		}
		
		/*******************************************************
		 * Add a shape that defines this LED.  Each LED can be made up of 
		 * one or more shapes.
		 * 
		 * @param shape IN - the shape to add.
		 *******************************************************/
		public void addShape(AbstractShape shape)
		{
			// TODO
			if (shape instanceof TextShape)
			{
				throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
			}
			
			this.shapes_.add(shape);
		}
		
		/*******************************************************
		 * Get the list of shapes.
		 * 
		 * @return IN - the shape list.
		 ******************************************************/
		protected ArrayList<AbstractShape> getShapes()
		{
			return this.shapes_;
		}
		
		/********************************************************
		 * Get the minimum value this LED responds to.
		 * @return
		 *******************************************************/
		public double getMin()
		{
			return this.min_;
		}
		
		/********************************************************
		 * get the maximum value this LED responds to.
		 * @return
		 ******************************************************/
		public double getMax()
		{
			return this.max_;
		}
	}
	


}
