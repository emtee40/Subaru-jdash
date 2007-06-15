/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge
 *  Aug 24, 2006
 *  $Id: AnalogGauge.java,v 1.5 2006/12/31 16:59:10 shaneapowell Exp $
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.gui.shapes.AbstractShape;
import net.sourceforge.JDash.gui.shapes.ButtonShape;
import net.sourceforge.JDash.gui.shapes.TextShape;


/*******************************************************
 * This is a basic analog gauge.  An analog gauge can be
 * configured to emulate almost any sweep hand gauge. 
 * A gauge of this type can be configured to limit the 
 * range of motion, control the low and high range positions,
 * define a set of important ranges.  
 * <br>
 * The gauges neele is
 * created by adding shapes with the addMainNeedleShape() addLowNeedleShape() and addHighNeedleShape()
 * methods.  Each added shape will be rotated arounds it's (0,0) axis 
 * to corrispond to the angle value that this gauge is going to display.  So,
 * the shapes you add need to have their coord space designed around (0,0) 
 * being the gauges pivot point.  Remember also, that in the Computer world,
 * the pixels start in the upper left corner of the panel.  So, a point with a
 * value if (10,20) is 1o pixels to the right, and 20 pixels down.   Also, the needle 
 * needs to be disigned with the points along the Y axis at X=0 as the length of the needle.
 * <br>
 * So, if you want to create a simple stright line needle with a 5 pixel tail, and a 20 pixel
 * needle, you'll want to add a line shape with it's 2 points set as (0,5)(0,-20).
 * <br>
 * The calculation for setting the angle of the needle is quite simple. You set the
 * min/max paramter values with the setValueMin() and setValueMax() methods.  You then
 * set the min/max needle angle in degrees with the setDegreeMin() and setDegreeMax()
 * methods.  As the parameter value moves between the min/max values, the needle angle
 * is calculated as the percentage between min/max value and min/max degree.  
 * <br>
 * eg. Set the min/max values to 5/40.  And set the min/max degrees to -90/+90.
 * If the parameter value is at or below 5, then the angle will be set to -90.
 * If the parameter value is at or above 40, then the angle will be set to +90.
 * If the parameter value is somewhere in between then the angle is calculated as  
 *<pre> 
 *  radians = (getValue() - getValueMin()) / (getValuemax() - getValueMin()) * (getDegreeMax().radians - getDegreeMin().radians);
 *</pre>
 *<br>  
 * The normal direction of the needle is left to right for increasing values. If you want the
 * needle to rotoate right ot left for increasing values, then the values you set with the setDegreeMin() and setDegreeMax()
 * simply need to be reversed.  IN otherwords, make the minimum greater than the maximum.
 ******************************************************/
public class AnalogGauge extends AbstractGauge
{
	
	
	
	public static final long serialVersionUID = 0L;
	
//	
//	private double previousTheta_ = -99D;
//	private double previousHighTheta_ = -99D;
//	private double previousLowTheta_ = -99D;
	
	private Point pivotPoint_ = null;
	private int needleDegreeMin_ = 0;
	private int needleDegreeMax_ = 0;
	private double valueMin_ = 0L;
	private double valueMax_ = 0L;

	
	/* The list of shapes that make up this gauge */
	private ArrayList<AbstractShape> mainNeedleShapes_ = new ArrayList<AbstractShape>();
	private ArrayList<AbstractShape> lowNeedleShapes_ = new ArrayList<AbstractShape>();
	private ArrayList<AbstractShape> highNeedleShapes_ = new ArrayList<AbstractShape>();
	
	private ArrayList<AbstractShape> preGenShapes_ = null;
	private ArrayList<Shape> preGenAwtShapes_ = null;

	
	/* Cached low and high needle values */
	private double lowNeedleValueInDegrees_ = Double.MAX_VALUE;
	private double highNeedleValueInDegrees_ = 0f;
	
	/* The last time the needle was reset */
	private long lowNeedleLastReset_ = 0l;
	private long highNedleLastReset_ = 0l;
	
	/* The delay time between needle resets */
	private int lowNeedleResetDelay_ = 0;
	private int highNeedleResetDelay_ = 0;
	
	/* Default to clockwise */
	private boolean clockwise_ = true;
	
	/******************************************************
	 * Create a new analog gauge.
	 * @param p IN - the parameter this gauge will display values for.
	 * @param parentPanel IN - the owner parent gauge panel.
	 ******************************************************/
	public AnalogGauge(Parameter p, GaugePanel parentPanel)
	{
		super(p, parentPanel);
	}

	/*******************************************************
	 * Add the main needle shape
	 * @param shape IN the shape to add to the main needle shapes.
	 *******************************************************/
	public void addMainNeedleShape(AbstractShape shape)
	{
		if (shape instanceof TextShape)
		{
			throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
		}
		
		this.mainNeedleShapes_.add(shape);
	}
	
	/*******************************************************
	 * Add the low needle shape
	 * @param shape IN the shape to add.
	 *******************************************************/
	public void addLowNeedleShape(AbstractShape shape)
	{
		if (shape instanceof TextShape)
		{
			throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
		}
		
		this.lowNeedleShapes_.add(shape);
	}
	
	/*******************************************************
	 * Add the high needle shape
	 * @param shape the shape to add.
	 *******************************************************/
	public void addHighNeedleShape(AbstractShape shape)
	{
		if (shape instanceof TextShape)
		{
			throw new RuntimeException(this.getClass().getName() + " does not support shapes of type " + shape.getClass().getName());
		}
		
		this.highNeedleShapes_.add(shape);
	}

	
	
	/*******************************************************
	 * set the low and high needle reset delays.  This is the time
	 * in milliseconds that must pass for the needle to reset
	 * itself automatically.  So, if the current needle setting
	 * does not exceed the high-needle for x ms, then it will be 
	 * reset back to the current needles point.  Pass a value of -1
	 * to have the needles NEVER reset themselves.  You can manually
	 * reset the needles with the method resetHighNeedle() and resetLowNeedle().
	 * You can also put buttons on the screen that link to these reset methods.
	 * see the addButton() method.
	 * 
	 * @param lowNeedleResetDelay IN - reset delay time in ms. -1 to NOT reset.
	 * @param highNeedleResetDelay IN - reset delay time in ms. -1 to NOT reset.
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
	 * The default is to set the sweep of the hands to move clockwise, 
	 * but you can reverse this by setting this value to false.
	 * @param clockwise
	 *******************************************************/
	public void setClockwise(boolean clockwise)
	{
		this.clockwise_ = clockwise;
	}
	
	
	/*******************************************************
	 * Add a button to this gauge.  The button must have one of 
	 * our supported button action string codes.   These
	 * are the oNLY buttons this guage supportes.  The 
	 * action codes are defined in the AbstractGauge class.
	 * The static constants that start with "BUTTON_ACTION_xxxx"
	 * 
	 * @param button IN - the button to add.
	 ******************************************************/
	public void addButton(ButtonShape button)
	{

		/* Create the button */
		GaugeButton gaugeButton = new GaugeButton(getParentPanel().getSkin(), button);
		
		if (GaugeButton.BUTTON_ACTION_HIGH_RESET.equals(button.getAction()) == true)
		{
			gaugeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					resetHighNeedle();
				}
			});
		}
		else if (GaugeButton.BUTTON_ACTION_LOW_RESET.equals(button.getAction()) == true)
		{
			gaugeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					resetLowNeedle();
				}
			});
		}
		else
		{
			throw new RuntimeException("Cannot add a button of action [" + button.getAction() + "] to an analog gague.\nOnly support for buttons of action: [" + 
					GaugeButton.BUTTON_ACTION_HIGH_RESET + " / " + GaugeButton.BUTTON_ACTION_LOW_RESET + "]");
		}
		
		/* Add the button */		
		this.getParentPanel().add(gaugeButton, button.getShape().getBounds());
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
		this.highNedleLastReset_ = 0L; 
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.AbstractGauge#getBounds()
	 *******************************************************/
	@Override
	public synchronized Rectangle preGenerate(AffineTransform scalingTransform, boolean force)
	{
		this.preGenShapes_ = new ArrayList<AbstractShape>();
		this.preGenAwtShapes_ = new ArrayList<Shape>();

		/* The final outter bounds */
		Rectangle bounds = null;

		
		/* The starting value in degrees is the gauges minimum */
		double valueInDegrees = Math.min(getDegreeMin(), getDegreeMax());
		

		/* get the current value, but ensure it's in our bounds */
		double pValue = getParameter().getResult();
		pValue = Math.max(pValue, getValueMin());
		pValue = Math.min(pValue, getValueMax());
		
		
		/* Calculate what percent the current value is given our value sweep */
		double positionPercent = (pValue - getValueMin()) / (getValueMax() - getValueMin());
		
		/* Now, calculate what the angle should be, given the angle sweep */
		valueInDegrees += (positionPercent * (getDegreeMax() - getDegreeMin()));
		
		
		/* Just in case, lets make sure we're not out of range */
		valueInDegrees = Math.min(valueInDegrees, getDegreeMax());
		valueInDegrees = Math.max(valueInDegrees, getDegreeMin());
		
		/* The perceived direction is calculated basd on the degree min/max */
		if (this.clockwise_ == false)
		{
			valueInDegrees *= -1;
		}
		
		
		/* calculate the low and high needle values */
		if ((valueInDegrees > this.highNeedleValueInDegrees_) || (System.currentTimeMillis() > (this.highNedleLastReset_ + this.highNeedleResetDelay_)))
		{
			this.highNeedleValueInDegrees_ = valueInDegrees;
			this.highNedleLastReset_ = System.currentTimeMillis();
		}
		
		if ((valueInDegrees < this.lowNeedleValueInDegrees_) || (System.currentTimeMillis() > (this.lowNeedleLastReset_ + this.lowNeedleResetDelay_)))
		{
			this.lowNeedleValueInDegrees_ = valueInDegrees;
			this.lowNeedleLastReset_ = System.currentTimeMillis();
		}
		
		/* Convert into radians for the transform */
		double mainNeedleTheta = Math.toRadians(valueInDegrees);
		double lowNeedleTheta = Math.toRadians(this.lowNeedleValueInDegrees_);
		double highNeedleTheta = Math.toRadians(this.highNeedleValueInDegrees_);
		
//		/* Not yet working right */
//		/* If none of the needles has changed, then don't bother with a redraw.  If even one has changed
//		 * then keep going */
//		if ((force == false) &&
//			(mainNeedleTheta == this.previousTheta_) &&
//			(lowNeedleTheta == this.previousLowTheta_) &&
//			(highNeedleTheta == this.previousHighTheta_))
//		{
//			System.out.println(mainNeedleTheta + "  " + this.previousTheta_);
//			System.out.println(lowNeedleTheta + "  " + this.previousLowTheta_);
//			System.out.println(highNeedleTheta + "  " + this.previousHighTheta_);
//			System.out.println("Not redrawing for analgo gague: " + getParameter().getName());
//			return null;
//		}
//			
//		
//		this.previousTheta_ = mainNeedleTheta;
//		this.previousHighTheta_ = highNeedleTheta;
//		this.previousLowTheta_ = lowNeedleTheta;
//		
		
		/* Draw each low needle shape */
		for (AbstractShape shape : this.lowNeedleShapes_)
		{
			/* Get the awt shape */
			Shape awtShape = shape.getShape();
								
			/* First, rotate the shape */
			awtShape = AffineTransform.getRotateInstance(lowNeedleTheta).createTransformedShape(awtShape);
			
			/* Next, translate the shape */
			awtShape = AffineTransform.getTranslateInstance(getPivot().getX(), getPivot().getY()).createTransformedShape(awtShape);
			
			/* Now apply the panels scaling transform */
			awtShape = scalingTransform.createTransformedShape(awtShape);
			
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
			
		}
		

		/* Draw each high needle shape */
		for (AbstractShape shape : this.highNeedleShapes_)
		{
			/* Get the awt shape */
			Shape awtShape = shape.getShape();
								
			/* First, rotate the shape */
			awtShape = AffineTransform.getRotateInstance(highNeedleTheta).createTransformedShape(awtShape);
			
			/* Next, translate the shape */
			awtShape = AffineTransform.getTranslateInstance(getPivot().getX(), getPivot().getY()).createTransformedShape(awtShape);
			
			/* Now apply the panels scaling transform */
			awtShape = scalingTransform.createTransformedShape(awtShape);
			
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
			
		}
		


		/* Draw each main needle shape */
		for (AbstractShape shape : this.mainNeedleShapes_)
		{
			/* Get the awt shape */
			Shape awtShape = shape.getShape();
								
			/* First, rotate the shape */
			awtShape = AffineTransform.getRotateInstance(mainNeedleTheta).createTransformedShape(awtShape);
			
			/* Next, translate the shape */
			awtShape = AffineTransform.getTranslateInstance(getPivot().getX(), getPivot().getY()).createTransformedShape(awtShape);
			
			/* Now apply the panels scaling transform */
			awtShape = scalingTransform.createTransformedShape(awtShape);
			
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
	 * Override the paint method so we can draw the gauge.
	 *
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 *******************************************************/
	@Override
	public synchronized void paint(Graphics2D g2, AffineTransform scalingTransform)
	{
		/* It's possible to get a paint event without having a sensor event. so 
		 * we'll just draw a raw gauge */
		if (this.preGenAwtShapes_ == null)
		{
			preGenerate(scalingTransform, true);
		}
		
		for (int index = 0; index < this.preGenShapes_.size(); index++)
		{
			getParentPanel().paintShape(g2, this.preGenShapes_.get(index), this.preGenAwtShapes_.get(index));
		}



	}
	

	/********************************************************
	 * Set the needle pivot point in screen corrds. 
	 * 
	 * @param x IN - the x coord.
	 * @param y IN - the y coord.
	 *******************************************************/
	public void setPivot(Point p)
	{
		this.pivotPoint_ = p;
	}
	
	/*******************************************************
	 * return the pivot point.
	 * @return IN the pivot Point.
	 *******************************************************/
	public Point getPivot()
	{
		return this.pivotPoint_;
	}
	
	/********************************************************
	 * Set the minimum parameter value that this gague can display.
	 * This value will be linked to the setDegreeMin() value. 
	 * 
	 * @param value IN - the value to set as the minimum.
	 *******************************************************/
	public void setValueMin(double value)
	{
		this.valueMin_ = value;
	}
	
	/********************************************************
	 * Get the minimum display value.
	 * @return the minimum value.
	 *******************************************************/
	public double getValueMin()
	{
		return this.valueMin_;
	}
	
	/********************************************************
	 * Set the minimum angle in degrees. This value will be
	 * linked to the setValueMin() value.  When ever the 
	 * paramter contains this value, the needle will be set
	 * to this angle in degrees.  The angle is a 0-up reference. 
	 * so, a needle pointing straight left will have a degree value
	 * of -90.
	 * 
	 * @param value IN - the minimum needle angle in degrees.
	 *******************************************************/
	public void setDegreeMin(int value)
	{
		this.needleDegreeMin_ = value;
	}
	
	/********************************************************
	 * Get the minimum angle in degrees.
	 * 
	 * @return the minimum needle angle in degrees.
	 *******************************************************/
	public int getDegreeMin()
	{
		return this.needleDegreeMin_;
	}
	
	/********************************************************
	 * Set the maximum angle in degrees. Like the setDegreeMin()
	 * method, this value will be linked to the setValueMax()
	 * method.  When the paramter is at or above the max value, then
	 * the angle of the needle will be set to this value.
	 * 
	 * @param value IN - the maximum needle angle in degrees.
	 *******************************************************/
	public void setDegreeMax(int value)
	{
		this.needleDegreeMax_ = value;
	}
	
	/*******************************************************
	 * Get the maximum angle in degrees.
	 * @return the max angle in degrees.
	 *******************************************************/
	public int getDegreeMax()
	{
		return this.needleDegreeMax_;
	}
	
	/********************************************************
	 * Set the maximim parameter value that this gauge will display.
	 * This value will be linked to the maximum degree to set
	 * the needle angle.
	 * @param value IN - the max value.
	 *******************************************************/
	public void setValueMax(double value)
	{
		this.valueMax_ = value;
	}
	
	/*******************************************************
	 * Get the maximum value.
	 * @return the maximum value.
	 *******************************************************/
	public double getValueMax()
	{
		return this.valueMax_;
	}
}
