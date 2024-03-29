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
import net.sourceforge.JDashLite.ecu.comm.ValueChangedListener;
import net.sourceforge.JDashLite.profile.ProfileRenderer;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import net.sourceforge.JDashLite.util.CircularIndex;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Image;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 * 
 *
 *********************************************************/
public class LineGraphGauge extends ProfileGauge implements ValueChangedListener
{
	
	
	private static final String PROP_D_RANGE_START 				= "range-start";
	private static final String PROP_D_RANGE_END				= "range-end";

	/* When the render method is called, we compare the previos rect to see if we need to re-generate the static content */
	private Rect lastRect_ = null;

	
	/* The image for the static background stuff */
	private Image staticContent_ = null;

	
	/* Holds the list of values to be drawn */
	private HistoryValue[] valueHistory_ = null;
	
	/* The history array pointer */
	private CircularIndex historyIndex_ = null;
	
//
//	private double rangeStart_ = 0.0;
//	private double rangeEnd_ = 0.0;
//	private double range_ = 0.0;
	
	private Font labelFont_ = null;
	
	private boolean doNextRender_ = true;
	
	/********************************************************
	 * 
	 *******************************************************/
	public LineGraphGauge()
	{
	}
	
	/********************************************************
	 * @return
	 ********************************************************/
	public double getRangeStart()
	{
		return getDoubleProperty(PROP_D_RANGE_START, NULL_DOUBLE);
	}
	
	/********************************************************
	 * @param r
	 ********************************************************/
	public void setRangeStart(double r)
	{
		setDoubleProperty(PROP_D_RANGE_START, r);
	}

	/********************************************************
	 * @return
	 ********************************************************/
	public double getRangeEnd()
	{
		return getDoubleProperty(PROP_D_RANGE_END, NULL_DOUBLE);
	}
	
	/********************************************************
	 * @param r
	 ********************************************************/
	public void setRangeEnd(double r)
	{
		setDoubleProperty(PROP_D_RANGE_END, r);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 ********************************************************/
	protected void finalize() throws Throwable
	{
		getECUParameter().removeValueChangedListener(this);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.ecu.comm.ValueChangedListener#onValueChanged()
	 ********************************************************/
	public void onValueChanged()
	{
//		try
//		{
//			throw new Exception("Value Change being fired for " + getECUParameter().getName());
//		}
//		catch(Exception e)
//		{
//			
//			e.printStackTrace();
//		}
		
		/* If the history array isn't initialized yet, then we don't care what the value is */
		if (this.valueHistory_ == null)
		{
			return;
		}
		
		/* If the history value at the current head is older than the TS indicates in the parameter, then move the head, and add the new value */
		if (this.valueHistory_[this.historyIndex_.getHead()].getTimestamp() < getECUParameter().getTimeStamp())
		{

			/* Move the head pointer */
			this.historyIndex_.decrementHead();

			/* Set the current parameter vlaue into the history */
			this.valueHistory_[this.historyIndex_.getHead()].setValue(getECUParameter().getValue());
			this.valueHistory_[this.historyIndex_.getHead()].setTimestamp(getECUParameter().getTimeStamp());
		}
		
		this.doNextRender_ = true;
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#setECUParameter(net.sourceforge.JDashLite.ecu.comm.ECUParameter)
	 ********************************************************/
	public void setECUParameter(ECUParameter param)
	{
		super.setECUParameter(param);
		param.addValueChangedListener(this);
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void render(Graphics g, Rect r, ColorModel cm, boolean forceRepaint)
	{
//		if (redrawAll) System.out.println("Forcing Redraw of " + getECUParameter().getName() + " " + getProperty(ProfileGauge.PROP_STR_LABEL));

		/* Create the value history array */
		if ((this.valueHistory_ == null) || (r.width != this.valueHistory_.length))
		{
			this.valueHistory_ = new HistoryValue[r.width];
			this.historyIndex_ = new CircularIndex(this.valueHistory_.length);
			this.historyIndex_.resetIndex();
			for (int index = 0; index < this.historyIndex_.getSize(); index++)
			{
				this.valueHistory_[this.historyIndex_.getIndex()] = new HistoryValue(NULL_DOUBLE, -1);
				this.historyIndex_.incrementIndex();
			}

		}

		
		/* Static Content */
		if (r.equals(this.lastRect_) == false || cm != this.currentColorModel_)
		{
			this.staticContent_ = new Image(r.width, r.height);
			generateStaticImage(cm);
		}


		/* Now, the dynamic image */
		if (forceRepaint || doNextRender_ || r.equals(this.lastRect_) == false || cm != this.currentColorModel_)
		{
			g.drawImage(this.staticContent_, r.x, r.y);
			renderDynamic(g, r, cm);
		}


		/* Remember the rect */
		this.lastRect_ = r;
		this.currentColorModel_ = cm;
		
		/* turn off the next render */
		this.doNextRender_ = false;
		
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.gauge.ProfileGauge#render(waba.fx.Graphics, waba.fx.Rect, net.sourceforge.JDashLite.ecu.comm.ECUParameter, net.sourceforge.JDashLite.profile.color.ColorModel, boolean)
	 ********************************************************/
	public void renderDynamic(Graphics g, Rect r, ColorModel cm)
	{
		

//		/* New Rect? New static image */
//		if (r.equals(this.lastRect_) == false)
//		{
//			this.staticContent_ = new Image(r.width, r.height);
//			generateStaticImage(this.staticContent_.getGraphics(), new Rect(0, 0, r.width, r.height), cm);
//			this.lastRect_ = r;
//		}

		
		double highValue = getRangeStart();
		double lowValue = getRangeEnd();
		int highValueY = r.y + r.height + 1;
		int lowValueY = r.y - 1;
		int valueCount = 0;
		double valueAverage = 0;
		
		
		/* Draw the static image */
		g.drawImage(this.staticContent_, r.x, r.y);

		/* Set the dynamic line colors */
		g.setBackColor(cm.get(ColorModel.LINE_GRAPH_LINE));
		g.setForeColor(cm.get(ColorModel.LINE_GRAPH_LINE));
		
		/* Draw the line graph pixels */
		this.historyIndex_.resetIndex();

		/* Loop down the value array */
		HistoryValue hv = null;
		for (int index = 0; index < this.historyIndex_.getSize(); index++)
		{
				
			hv = this.valueHistory_[this.historyIndex_.getIndex()];
			
			/* Outside our range?  Nothign to draw */
			if((hv.getValue() > NULL_DOUBLE) && (hv.getTimestamp() > 0))
			{
				/* For the average */
				valueCount++;
				valueAverage += hv.getValue();
				
				/* calc the pixel Y value, but stay under our range */
				int pxlY = (int)((double)r.height * ((Math.min(hv.getValue(), getRangeEnd()) - getRangeStart()) / (getRangeEnd() - getRangeStart())));
				pxlY = r.y + r.height - pxlY + 1; /* + 1 to STAY under the top of the rect */

				/* Track the min and max values */
				highValueY = Math.min(highValueY, pxlY);
				lowValueY = Math.max(lowValueY, pxlY);
				highValue = Math.max(highValue, hv.getValue());
				lowValue = Math.min(lowValue, hv.getValue());
				
				
				/* If the HV value is within our range, then draw it. */
				//if (hv.getValue() >= this.rangeStart_ && hv.getValue() <= this.rangeEnd_)
				if (pxlY > r.y && pxlY < r.y + r.height)
				{
					g.drawLine(r.x + r.width - index, pxlY, r.x + r.width - index, r.y + r.height);
				}
			}
			
			/* We MUuST increment the index */
			this.historyIndex_.incrementIndex();
		}
		
		
		
		/* Draw the high line */
		g.setFont(this.labelFont_);
		g.setForeColor(cm.get(ColorModel.LINE_GRAPH_HIGH_LINE));
		if (highValueY < r.y + r.height && highValueY > r.y)
		{
			g.drawLine(r.x, highValueY, r.x + r.width, highValueY);
			g.drawLine(r.x, highValueY + 1, r.x + r.width, highValueY + 1);
		}

		
		/* Low value */
		g.setForeColor(cm.get(ColorModel.LINE_GRAPH_LOW_LINE));
		if (lowValueY < r.y + r.height && lowValueY > r.y)
		{
			g.drawLine(r.x, lowValueY, r.x + r.width, lowValueY);
			g.drawLine(r.x, lowValueY - 1, r.x + r.width, lowValueY - 1);
		}

		if (valueCount != 0)
		{
			valueAverage = valueAverage / ((double)valueCount);
		}
		
		g.setForeColor(cm.get(ColorModel.DEFAULT_TEXT));
		g.drawText(Convert.toString(highValue, 0), r.x + 5, r.y + 5);
		g.drawText(Convert.toString(valueAverage, 0), r.x + 5, r.y + (r.height / 2) - (this.labelFont_.fm.height / 2) + this.labelFont_.fm.descent);
		g.drawText(Convert.toString(lowValue, 0), r.x + 5, r.y + r.height - 5 - this.labelFont_.fm.height + this.labelFont_.fm.descent);
	
	}

	
	/*******************************************************
	 * @param g
	 * @param r
	 * @param p
	 * @param cm
	 ********************************************************/
	private void generateStaticImage(ColorModel cm)
	{
		
		Graphics g = this.staticContent_.getGraphics();
		Rect r = new Rect(0, 0, this.staticContent_.getWidth(), this.staticContent_.getHeight());
		
		/* Remember the lable font cuz we'll need it for the digital values */
		this.labelFont_ = ProfileRenderer.findFontBestFitHeight((int)(r.height * 0.15), false);

		if (getRangeStart() == NULL_DOUBLE)
		{
			throw new RuntimeException("Range Start value not set in gauge profile");
		}
		
		if (getRangeEnd() == NULL_DOUBLE)
		{
			throw new RuntimeException("Range End value not set in gauge profile");
		}
		
//		this.range_ = this.rangeEnd_ - this.rangeStart_;
		
		
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.setBackColor(cm.get(ColorModel.LINE_GRAPH_BACKGROUND));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.drawRect(r.x, r.y, r.width, r.height);

		
////		/* Create the value history array */
////		if ((this.valueHistory_ == null) || (r.width != this.valueHistory_.length))
////		{
////			this.valueHistory_ = new HistoryValue[r.width];
////			this.historyIndex_ = new CircularIndex(this.valueHistory_.length);
////		}
//		
//		
//		/* Pre-pop the value array */
//		this.historyIndex_.resetIndex();
//		for (int index = 0; index < this.historyIndex_.getSize(); index++)
//		{
//			this.valueHistory_[this.historyIndex_.getIndex()] = new HistoryValue(NULL_DOUBLE, -1);
//			this.historyIndex_.incrementIndex();
//		}
//		
		 /* Just for debugging, start all the way to the end */
//		this.historyIndex_.decrementHead();

		
		/* Draw the label */
		String label = getProperty(PROP_STR_LABEL);
		if (label != null)
		{
			g.setFont(this.labelFont_);
			g.setForeColor(cm.get(ColorModel.DEFAULT_TEXT));
			g.drawText(label, r.x + r.width - this.labelFont_.fm.getTextWidth(label) - 5, r.y + 2);
		}

	}

	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class HistoryValue
	{
		private double value_ = 0.0;
		private int timestamp_ = 0;
		
		/********************************************************
		 * 
		 *******************************************************/
		public HistoryValue(double value, int timestamp)
		{
			this.value_ = value;
			this.timestamp_ = timestamp;
		}
		
		/********************************************************
		 * @return the value
		 ********************************************************/
		public double getValue()
		{
			return this.value_;
		}
		
		/********************************************************
		 * @param value the value to set
		 ********************************************************/
		public void setValue(double value)
		{
			this.value_ = value;
		}
		
		/********************************************************
		 * @return the timestamp
		 ********************************************************/
		public int getTimestamp()
		{
			return this.timestamp_;
		}
	
		/********************************************************
		 * @param timestamp the timestamp to set
		 ********************************************************/
		public void setTimestamp(int timestamp)
		{
			this.timestamp_ = timestamp;
		}
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 ********************************************************/
		public String toString()
		{
			return "[t" + getTimestamp() + "][v" + getValue() + "]";
		}
		
	}
}

