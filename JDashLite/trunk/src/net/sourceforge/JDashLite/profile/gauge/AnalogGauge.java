/*********************************************************
 * 
 * @author spowell
 * DigitalGauge.java
 * Jul 30, 2008
 *
Copyright (C) 2008 Shane Powell

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
 *********************************************************/

package net.sourceforge.JDashLite.profile.gauge;

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import net.sourceforge.JDashLite.util.AffineTransform;
import waba.fx.Coord;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Image;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 *  Zero degrees is straight right.  Rotation is right to left
 *
 *********************************************************/
public class AnalogGauge extends ProfileGauge
{
	
	private static final double OUTER_RING_RADIUS = 0.48;
	private static final double OUTER_POINT_RADIUS = 0.46;
	private static final double INNTER_POINT_RADIUS = 0.40;
	private static final double NEEDLE_WIDTH = 0.045;
	
	private static final double MINIMUM_DEGREE = 50;
	private static final double MAXIMUM_DEGREE = 360 - 50;
	

	/* The range starting value of the ecu value */
	private double rangeStart_ = 0.0;
	
	/* The range ending value of the ecu value */
	private double rangeEnd_ = 0.0;

	/* The default number of decimal places to show */
	private int decimalPrecision_ = 0;
	
	/* The tick marks / Numbers are defined by a divisor */
	private double tickCount_ = -1;
	
	/* Include the ticks at all? */
	private boolean includeTicks_ = true;
	
	/* Include the nubmer label values */
	private boolean includeTickLabels_ = true;
	
	/* Include the digital value readout ? */
	private boolean includeDigitalValue_ = false;
	
	/* When the render method is called, we compare the previos rect to see if we need to re-generate the static content */
	private Rect lastRect_ = null;
	
	/* The image for the static background stuff */
	private Image staticContent_ = null;
	
	/* The current value font */
	private Font currentValueFont_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public AnalogGauge()
	{
	}
	
	
	/********************************************************
	 * @return the rangeEnd
	 ********************************************************/
	public double getRangeEnd()
	{
		return this.rangeEnd_;
	}
	
	
	/********************************************************
	 * @param rangeEnd the rangeEnd to set
	 ********************************************************/
	public void setRangeEnd(double rangeEnd)
	{
		this.rangeEnd_ = rangeEnd;
	}
	
	/********************************************************
	 * @return the rangeStart
	 ********************************************************/
	public double getRangeStart()
	{
		return this.rangeStart_;
	}
	
	/********************************************************
	 * @param rangeStart the rangeStart to set
	 ********************************************************/
	public void setRangeStart(double rangeStart)
	{
		this.rangeStart_ = rangeStart;
	}
	
	/********************************************************
	 * @return the decimalPlaces
	 ********************************************************/
	public int getDecimalPrecision()
	{
		return this.decimalPrecision_;
	}
	
	
	/********************************************************
	 * @param decimalPlaces the decimalPlaces to set
	 ********************************************************/
	public void setDecimalPrecision(int decimalPrecision)
	{
		this.decimalPrecision_ = decimalPrecision;
	}

	/********************************************************
	 * @return the tickCount
	 ********************************************************/
	public double getTickCount()
	{
		return this.tickCount_;
	}
	
	
	/********************************************************
	 * @param tickCount the tickCount to set
	 ********************************************************/
	public void setTickCount(double tickCount)
	{
		this.tickCount_ = tickCount;
	}
	
	
	/********************************************************
	 * @return the includeTickLabels
	 ********************************************************/
	public boolean getIncludeTickLabels()
	{
		return this.includeTickLabels_;
	}
	
	/********************************************************
	 * @param includeTickLabels the includeTickLabels to set
	 ********************************************************/
	public void setIncludeTickLabels(boolean includeTickLabels)
	{
		this.includeTickLabels_ = includeTickLabels;
	}
	
	
	/********************************************************
	 * @return the includeTicks
	 ********************************************************/
	public boolean getIncludeTicks()
	{
		return this.includeTicks_;
	}
	
	/********************************************************
	 * @param includeTicks the includeTicks to set
	 ********************************************************/
	public void setIncludeTicks(boolean includeTicks)
	{
		this.includeTicks_ = includeTicks;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ECUParameter p, ColorModel cm)
	{
		
		
		
		/* Calculate a few common needed values */
		int centerX = r.x + (r.width / 2);
		int centerY = r.y + (r.height / 2);
		int needleLength = (int)((Math.min(r.width, r.height) * OUTER_POINT_RADIUS) - (OUTER_POINT_RADIUS - INNTER_POINT_RADIUS));
		int needleWidth = (int)(needleLength * NEEDLE_WIDTH);
		needleWidth = Math.max(3, needleWidth);

		if (r.equals(this.lastRect_) == false)
		{
			this.staticContent_ = new Image(r.width, r.height);
			generateStaticImage(this.staticContent_.getGraphics(), new Rect(0, 0, r.width, r.height), p, cm, (int)(needleWidth * 1.5));
			this.lastRect_ = r;
		}

		/* Draw the static image */
		g.drawImage(this.staticContent_, r.x, r.y);

		
		/* Draw the needle */
		{

			/* Start with the ecu value, but watch for over/under values */
			double valueAngle = p.getValue();

			if (p.getValue() >= this.rangeEnd_)
			{
				valueAngle = this.rangeEnd_;
			}
			else if (p.getValue() <= this.rangeStart_)
			{
				valueAngle = this.rangeStart_;			
			}
			
			/* Calculate the distance of parameter value is from the minimum */
			valueAngle = valueAngle - this.rangeStart_;
			
			/* Now, what percentage is the offset within the range */
			valueAngle = valueAngle / (this.rangeEnd_ - this.rangeStart_);
			
			/* Now, calulcate the angle given the start and end angle */
			valueAngle = MINIMUM_DEGREE + ((MAXIMUM_DEGREE - MINIMUM_DEGREE) * valueAngle);
			
			/* Define the needle.  Origin pointing straight down */
			Coord[] needlePoints = new Coord[] {new Coord(needleWidth / 2 * -1,needleLength / 8 * -1), new Coord(needleWidth / 2 * -1, needleLength), new Coord(needleWidth / 2, needleLength), new Coord(needleWidth / 2, needleLength / 8 * -1)};
			
			/* Rotate and translate it the calculated angle. */
			AffineTransform trans = AffineTransform.rotateInstance(Math.toRadians(valueAngle));
			trans.addTranslate(centerX, centerY);
			
			/* Apply the transform */
			trans.apply(needlePoints);
			
			/* Draw the needle */
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_NEEDLE));
			g.setBackColor(cm.get(ColorModel.ANALOG_GAUGE_NEEDLE));
			g.fillPolygon(ProfileRenderer.toXArray(needlePoints), ProfileRenderer.toYArray(needlePoints), needlePoints.length);
			
			/* Draw the nub */
			g.fillCircle(centerX, centerY, (int)(needleWidth * 1.5));
		}
		
		
		/* Draw the current digital value */
		if (this.includeDigitalValue_)
		{
	//		Font f = null;
	//		f = ProfileRenderer.findFontBestFitHeight((int)(r.height * LABEL_HEIGHT), false);
			int mainRadius = (int)(Math.min(r.width, r.height) * OUTER_RING_RADIUS);
			g.setFont(this.currentValueFont_);
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
			String val = Convert.toString(p.getValue(), getDecimalPrecision());
			g.drawText(val, centerX - (this.currentValueFont_.fm.getTextWidth(val) / 2), (centerY + (mainRadius / 2)) - ((this.currentValueFont_.fm.height - this.currentValueFont_.fm.descent)));
	//		g.drawText(val, centerX - (this.currentValueAndLabelFont_.fm.getTextWidth(val) / 2), centerY + needleWidth * 2);
		}
			
	}
	
	
	
	/*******************************************************
	 * @param g
	 * @param r
	 * @param p
	 * @param cm
	 *******************************************************/
	private void generateStaticImage(Graphics g, Rect r, ECUParameter p, ColorModel cm, int tickLength)
	{
		
		/* Blank the gauge first */
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.DEFAULT_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);
		
		
		/* Calculate a few common needed values */
		Font tickFont = ProfileRenderer.findFontBestFitHeight(tickLength * 2, false);
		int centerX = r.x + (r.width / 2);
		int centerY = r.y + (r.height / 2);
		int mainRadius = (int)(Math.min(r.width, r.height) * OUTER_RING_RADIUS);
		
		/* setup the current value font.  It's the same as the label font. */
		this.currentValueFont_ = ProfileRenderer.findFontBestFitWidth((int)(mainRadius * 0.80), Convert.toString(this.rangeEnd_, getDecimalPrecision()), false);
//		Font labelFont = ProfileRenderer.findFontBestFitWidth((int)(mainRadius * 0.80), getLabel(), false);
		
		
		/* Check the value font for max height too */
		if (this.currentValueFont_.fm.height - this.currentValueFont_.fm.descent > mainRadius / 3)
		{
			this.currentValueFont_ = ProfileRenderer.findFontBestFitHeight(mainRadius / 3, false); 
		}
		
		/* Draw the outer circle */
		g.setBackColor(cm.get(ColorModel.ANALOG_GAUGE_FACE));
		g.fillCircle(centerX, centerY, mainRadius);
		g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_RING));
		g.drawCircle(centerX, centerY, mainRadius);
	
		
		/* Calculate the tick mark range */
		double tickRange = ((this.rangeEnd_ - this.rangeStart_) / (double)(getTickCount() - 1.0));
		
		
		/* First, determine the normal length of the tick label values */
		//TODO
		
		
		
		/* Draw each tick mark */
		if (getIncludeTicks())
		{
			g.setFont(tickFont);
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
			for (double index = 0; index < getTickCount() - 0.5; index+=0.5)
			{
				Coord tickPoint1 = new Coord(0, mainRadius - (tickLength / 2));   /* outer tip of tick */
				Coord tickPoint2 = new Coord(0, tickPoint1.y - tickLength);       /* inner tip of tick */
				Coord tickPoint3 = new Coord(0, tickPoint2.y - (int)(tickLength * 1.5));  /* Center point of tick value */
				
				double tickAngle = MINIMUM_DEGREE + (((MAXIMUM_DEGREE - MINIMUM_DEGREE) / (double)(getTickCount() - 1.0)) * index);
				
				AffineTransform tickTxfm = AffineTransform.rotateInstance(Math.toRadians(tickAngle));
				tickTxfm.addTranslate(centerX, centerY);
				tickTxfm.apply(tickPoint1);
				tickTxfm.apply(tickPoint2);
				tickTxfm.apply(tickPoint3);

				/* Draw the tick */
				g.drawLine(tickPoint1.x, tickPoint1.y, tickPoint2.x, tickPoint2.y);
				
				/* Only draw the text on whole ticks */
				if (getIncludeTickLabels() && index % 1 == 0)
				{
					String tickValue = Convert.toString(this.rangeStart_ + (index * tickRange), getDecimalPrecision());
					
					/* No need to analize zero */
					if ((this.rangeStart_ + (index * tickRange)) == 0)
					{
						tickValue = "0";
					}
					else
					{
						
						/* While the last character is NOT a zero or a cecimal, trim them off */
						while ((tickValue.length() > 1) && (tickValue.charAt(tickValue.length() - 1) == '0') || ((tickValue.charAt(tickValue.length() - 1) == '.')))
						{
							tickValue = tickValue.substring(0, tickValue.length() - 1);
						}
					}

					/* Where to place the tick value */
					tickPoint3.x = tickPoint3.x - (tickFont.fm.getTextWidth(tickValue) / 2);
					tickPoint3.y = tickPoint3.y - ((tickFont.fm.height - tickFont.fm.descent) / 2);

					/* Draw it! */
					g.drawText(tickValue, tickPoint3.x, tickPoint3.y);
				}

			}
		}
		

		/* Draw the label */
		g.setFont(this.currentValueFont_);
		if (getLabel() != null)
		{
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
			//g.drawText(getLabel(), centerX - (labelFont.fm.getTextWidth(getLabel()) / 2), centerY - tickLength - labelFont.fm.height);
			g.drawText(getLabel(), centerX - (this.currentValueFont_.fm.getTextWidth(getLabel()) / 2), (centerY - (mainRadius / 2)) - ((this.currentValueFont_.fm.height - this.currentValueFont_.fm.descent) / 3));
		}

	}
}
