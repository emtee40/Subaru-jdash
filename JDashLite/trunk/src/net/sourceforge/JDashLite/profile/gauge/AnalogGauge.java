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
	
	private static final double MINIMUM_DEGREE = 225;
	private static final double MAXIMUM_DEGREE = -45;
	private static final double TICK_SPACES = 8;  /* The number of gaps between ticks, NOT the number of ticks */
	
	
	private double LABEL_HEIGHT = 0.2;

	
	private double rangeStart_ = 0.0;
	private double rangeEnd_ = 0.0;

	private boolean strechToFit_ = false;
	
	/* The default number of decimal places to show */
	private int decimalPrecision_ = 0;
	
	/* When the render method is called, we compare the previos rect to see if we need to re-generate the static content */
	private Rect lastRect_ = null;
	
	/* The image for the static background stuff */
	private Image staticContent_ = null;
	
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

	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ECUParameter p, ColorModel cm)
	{
		
		if (r.equals(this.lastRect_) == false)
		{
			this.staticContent_ = new Image(r.width, r.height);
			generateStaticImage(this.staticContent_.getGraphics(), new Rect(0, 0, r.width, r.height), p, cm);
			this.lastRect_ = r;
		}

		/* Draw the static image */
		g.drawImage(this.staticContent_, r.x, r.y);
		
		
		/* Calculate a few common needed values */
		int centerX = r.x + (r.width / 2);
		int centerY = r.y + (r.height / 2);
		int needleXRadius = (int)((r.width * OUTER_POINT_RADIUS) - (OUTER_POINT_RADIUS - INNTER_POINT_RADIUS)); 
		int needleYRadius = (int)((r.height * OUTER_POINT_RADIUS) - (OUTER_POINT_RADIUS - INNTER_POINT_RADIUS));
		
		if (!this.strechToFit_)
		{
			needleXRadius = (int)((Math.min(r.width, r.height) * OUTER_POINT_RADIUS) - (OUTER_POINT_RADIUS - INNTER_POINT_RADIUS)); 
			needleYRadius = (int)((Math.min(r.height, r.height) * OUTER_POINT_RADIUS) - (OUTER_POINT_RADIUS - INNTER_POINT_RADIUS));
		}
		
		/* Draw the needle */
		{
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_NEEDLE));
			g.setBackColor(cm.get(ColorModel.ANALOG_GAUGE_NEEDLE));
			
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
			
			/* Now, calulcate the angle given the start and end angles */
			valueAngle = (MINIMUM_DEGREE - MAXIMUM_DEGREE) * valueAngle;
			
			/* Now, subtract the valueAngle from the Minimu.. yes.. subtract from the minimum.  We rotate backwords in SuperWaba */
			valueAngle = MINIMUM_DEGREE - ((int)valueAngle);
			
			/* Calculate the 4 points of the needle */
			Coord needleLeftPoint = null;
			Coord needleRightPoint = null;
			Coord needleLeftBottom = null;
			Coord needleRightBottom = null;
			needleLeftPoint = g.getAnglePoint(centerX, centerY, needleXRadius, needleYRadius, (int)(valueAngle + 1.5));
			needleRightPoint = g.getAnglePoint(centerX, centerY, needleXRadius, needleYRadius, (int)(valueAngle - 1.5));
			needleLeftBottom  = g.getAnglePoint(centerX, centerY, 15, 15, (int)valueAngle + 180 - 6);
			needleRightBottom = g.getAnglePoint(centerX, centerY, 15, 15, (int)valueAngle - 180 + 6);
			
			/* Draw the needle */
			g.fillPolygon(new int[] {needleLeftBottom.x, needleLeftPoint.x, needleRightPoint.x, needleRightBottom.x}, new int[] {needleLeftBottom.y, needleLeftPoint.y, needleRightPoint.y, needleRightBottom.y}, 4);
		}
		
		
		/* Draw the current value */
		Font f = null;
		f = ProfileRenderer.findFontBestFitHeight((int)(r.height * LABEL_HEIGHT));
		g.setFont(f);
		g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
		String val = Convert.toString(p.getValue(), getDecimalPrecision());
		g.drawText(val, r.x + ((r.width - f.fm.getTextWidth(val)) / 2), r.y + (r.height - f.fm.height) - f.fm.height);
			
	}
	
	
	
	/*******************************************************
	 * @param g
	 * @param r
	 * @param p
	 * @param cm
	 *******************************************************/
	private void generateStaticImage(Graphics g, Rect r, ECUParameter p, ColorModel cm)
	{
		
		/* Blank the gauge first */
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.DEFAULT_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);
		
		
		/* Calculate a few common needed values */
		int centerX = r.x + (r.width / 2);
		int centerY = r.y + (r.height / 2);
		int mainXRadius = (int)(r.width * OUTER_RING_RADIUS);
		int mainYRadius = (int)(r.height * OUTER_RING_RADIUS);
		int tickInnerXRadius = (int)(r.width * INNTER_POINT_RADIUS);
		int tickOutterXRadius = (int)(r.width * OUTER_POINT_RADIUS);
		int tickInnerYRadius = (int)(r.height * INNTER_POINT_RADIUS);
		int tickOutterYRadius = (int)(r.height * OUTER_POINT_RADIUS);
		
		if (!this.strechToFit_)
		{
			mainXRadius = Math.min(mainXRadius, mainYRadius);
			mainYRadius = mainXRadius;
			tickInnerXRadius = Math.min(tickInnerXRadius, tickInnerYRadius);
			tickInnerYRadius = tickInnerXRadius;
			tickOutterXRadius = Math.min(tickOutterXRadius, tickOutterYRadius);
			tickOutterYRadius = tickOutterXRadius;
		}
		
		/* Draw the outer circle */
		g.setBackColor(cm.get(ColorModel.ANALOG_GAUGE_FACE));
		g.fillEllipse(centerX, centerY, mainXRadius, mainYRadius);
		g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_RING));
		g.drawEllipse(centerX, centerY, mainXRadius, mainYRadius);
	
		
		/* Draw the tick marks. */
		g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
		Coord innerXY = null;
		Coord outterXY = null;
		for (int index = 0; index <= TICK_SPACES; index++)
		{
			double degree = MINIMUM_DEGREE - (((MINIMUM_DEGREE - MAXIMUM_DEGREE) / TICK_SPACES)  * index);
			innerXY = g.getAnglePoint(centerX, centerY, tickInnerXRadius, tickInnerYRadius, (float)degree);
			outterXY = g.getAnglePoint(centerX, centerY, tickOutterXRadius, tickOutterYRadius, (float)degree);
			g.drawLine(innerXY.x, innerXY.y, outterXY.x, outterXY.y);
		}


		/* Draw the label */
		Font f = null;
		f = ProfileRenderer.findFontBestFitHeight((int)(r.height * LABEL_HEIGHT));
		g.setFont(f);
		if (getLabel() != null)
		{
			g.setForeColor(cm.get(ColorModel.ANALOG_GAUGE_TICK_MARK));
			f = ProfileRenderer.findFontBestFitHeight((int)(r.height * LABEL_HEIGHT));
			g.setFont(f);
			g.drawText(getLabel(), r.x + ((r.width - f.fm.getTextWidth(getLabel())) / 2), r.y + f.fm.height + 1);
		}

	}
}
