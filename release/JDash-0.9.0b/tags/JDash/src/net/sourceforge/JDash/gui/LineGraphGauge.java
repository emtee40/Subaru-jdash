/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge.java
 *  Aug 8, 2006
 *  $Id: LineGraphGauge.java,v 1.5 2006/12/31 16:59:10 shaneapowell Exp $
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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jcckit.GraphicsPlotCanvas;
import jcckit.data.DataCurve;
import jcckit.data.DataPlot;
import jcckit.data.DataPoint;
import jcckit.util.ConfigParameters;
import jcckit.util.PropertiesBasedConfigData;


import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.ecu.param.special.TimeParameter;
import net.sourceforge.JDash.gui.shapes.TextShape;
import net.sourceforge.JDash.gui.shapes.AbstractShape.PROPS;

/*******************************************************
 * Not yet implemented
 * http://jcckit.sourceforge.net/UserGuide/animatedChart.html
 ******************************************************/
public class LineGraphGauge extends AbstractGauge implements SwingComponentGauge, PaintableGauge
{
	
	public static final long serialVersionUID = 0L;
	
	private TimeParameter timeParam_ = null;
	
	/* The x axis range value in ms */
	private double timeRange_ = 30000;
	
	private ArrayList<DataPoint> plotValues_ = new ArrayList<DataPoint>();
	
	private GraphicsPlotCanvas plotCanvas_ = null;
	private Rectangle plotCanvasRect_ = null;
	
	
	private DigitalGauge valueGauge_ = null;
	private DigitalGauge lowGauge_ = null;
	private DigitalGauge highGauge_ = null;
	
	
	private HighLowParameter lowParameter_ = new HighLowParameter();
	private HighLowParameter highParameter_ = new HighLowParameter();
	
	/******************************************************
	 * @param p
	 * @param parentPanel
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param seconds
	 * @param min
	 * @param max
	 * @param format
	 * @param label
	 * @param valueGauge
	 * @param lowGauge
	 * @param highGauge
	 * @throws Exception
	 ******************************************************/
	public LineGraphGauge(Parameter p,
							double x, 
							double y, 
							double width, 
							double height, 
							double seconds, 
							double min, 
							double max,
							String format,
							String label,
							TextShape valueText,
							TextShape lowText,
							TextShape highText) throws Exception
							
	{
		super(p, new Point((int)x,(int)y));
		
		this.lowParameter_.setName(label + " low");
		this.highParameter_.setName(label + " high");
		this.lowParameter_.setValue(Double.MAX_VALUE);
		this.highParameter_.setValue(Double.MIN_VALUE);
		
		this.timeRange_ = seconds * 1000;
		
		this.timeParam_ = (TimeParameter)getParameter().getOwnerRegistry().getParamForName(ParameterRegistry.TIME_PARAM);
		
		/* Setup the plot parameters */
		Properties props = new Properties();
		ConfigParameters config = new ConfigParameters(new PropertiesBasedConfigData(props));
		
		props.put("paper", "0 0 " + width + " " + height);
		
		double xInset = width / 8.0;
		double yInset = height / 8.0;

				
		props.put("background", "" + Color.WHITE.getRGB());
		props.put("foreground", "" + Color.BLACK.getRGB());

		props.put("plot/coordinateSystem/origin", xInset + " " + yInset);
		
		props.put("plot/coordinateSystem/xAxis/axisLength", "" + (width - xInset - (xInset / 3)));
		props.put("plot/coordinateSystem/xAxis/minimum", "-" + this.timeRange_ / 1000);
		props.put("plot/coordinateSystem/xAxis/maximum", "0");
		props.put("plot/coordinateSystem/xAxis/axisLabel", "Time (seconds)");
		props.put("plot/coordinateSystem/xAxis/axisLabelPosition", "0 " + (((yInset / 3) * -2) + (yInset / 3)));
		props.put("plot/coordinateSystem/xAxis/ticLabelFormat", "%f");
		
		props.put("plot/coordinateSystem/yAxis/axisLength", "" + (height - yInset - (yInset / 3)));
		props.put("plot/coordinateSystem/yAxis/axisLabel", label);
		props.put("plot/coordinateSystem/yAxis/axisLabelPosition", (((xInset / 4) * -3) + (yInset / 3)) + " 0");
		props.put("plot/coordinateSystem/yAxis/ticLabelFormat", format);
		props.put("plot/coordinateSystem/yAxis/minimum", "" + min);
		props.put("plot/coordinateSystem/yAxis/maximum", "" + max);
		
		
		props.put("plot/curveFactory/definitions", "value low high");
		
		props.put("plot/curveFactory/value/withLine", "true");
		props.put("plot/curveFactory/value/lineAttributes/className", "jcckit.graphic.BasicGraphicAttributes");
		props.put("plot/curveFactory/value/lineAttributes/lineColor", "12655383"); /* Dark Red */
		props.put("plot/curveFactory/value/lineAttributes/lineThickness", "10");
		
		props.put("plot/curveFactory/low/withLine", "true");
		props.put("plot/curveFactory/low/lineAttributes/className", "jcckit.graphic.BasicGraphicAttributes");
		props.put("plot/curveFactory/low/lineAttributes/lineColor", "2446535"); /* Royal Blue */
		props.put("plot/curveFactory/low/lineAttributes/lineThickness", "5");

		props.put("plot/curveFactory/high/withLine", "true");
		props.put("plot/curveFactory/high/lineAttributes/className", "jcckit.graphic.BasicGraphicAttributes");
		props.put("plot/curveFactory/high/lineAttributes/lineColor", "3437109"); /* Forest Green */
		props.put("plot/curveFactory/high/lineAttributes/lineThickness", "5");
		
		props.put("plot/legendVisible", "false");
		
		
		if (valueText != null)
		{
			this.valueGauge_ = new DigitalGauge(getParameter(), getPosition(), valueText);
			props.put("plot/curveFactory/value/lineAttributes/lineColor", "" + Color.decode(valueText.getAttribute(PROPS.COLOR)).getRGB());
		}
		
		if (lowText != null)
		{
			this.lowGauge_ = new DigitalGauge(this.lowParameter_, getPosition(), lowText);
			props.put("plot/curveFactory/low/lineAttributes/lineColor", "" + Color.decode(lowText.getAttribute(PROPS.COLOR)).getRGB());
		}
		
		if (highText != null)
		{
			this.highGauge_ = new DigitalGauge(this.highParameter_, getPosition(), highText);
			props.put("plot/curveFactory/high/lineAttributes/lineColor", "" + Color.decode(highText.getAttribute(PROPS.COLOR)).getRGB());
		}

				
		/* Setup the plot canvas, and it's rect */
		this.plotCanvas_ = new GraphicsPlotCanvas(config);
		this.plotCanvasRect_ = new Rectangle();
		this.plotCanvasRect_.x = (int)x;
		this.plotCanvasRect_.y = (int)y;
		this.plotCanvasRect_.width = (int)width;
		this.plotCanvasRect_.height = (int)height;

		
	}

	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.SwingComponentGauge#getGaugeComponent()
	 *******************************************************/
	public List<Component> getGaugeComponents()
	{
		List<Component> cList = new ArrayList<Component>();
		Canvas canvas = this.plotCanvas_.getGraphicsCanvas(); 
		canvas.setBounds(this.plotCanvasRect_);
		cList.add(canvas);
		return cList;
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.PaintableGauge#paint(net.sourceforge.JDash.gui.AbstractGaugePanel, java.awt.Graphics2D, java.awt.geom.AffineTransform)
	 *******************************************************/
	public void paint(AbstractGaugePanel panel, Graphics2D g2, AffineTransform scalingTransform)
	{
		if (this.valueGauge_ != null)
		{
			this.valueGauge_.paint(panel, g2, scalingTransform);
		}
		
		if (this.lowGauge_ != null)
		{
			this.lowGauge_.paint(panel, g2, scalingTransform);
		}
		
		if (this.highGauge_ != null)
		{
			this.highGauge_.paint(panel, g2, scalingTransform);
		}
		
	}
	


	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.SwingComponentGauge#updateDisplay()
	 *******************************************************/
	public void updateDisplay()
	{
		/* If the parent gauge panel is flaged updates as suspended, then we'll stop here */
//		if (getParentPanel().isGaugeDisplayUpdateSuspended() == true)
		// TODO
		
		/* Create the point */
		DataPoint newPoint = new DataPoint(this.timeParam_.getResult() + 
					this.timeRange_, 
					getParameter().getResult());
		

		/* If the new point has a time in the past from the latest printed point, then lets assume that this
		 * is some sort of new graph in playback mode */
		if ((this.plotValues_.size() > 0) && (newPoint.getX() < this.plotValues_.get(this.plotValues_.size() - 1).getX()))
		{
			this.plotValues_.clear();
		}
				
	
		/* Add the new point */
		this.plotValues_.add(newPoint);

		
		/* Trim all point values that are now out of visual range */
		for (int index = 0; index < this.plotValues_.size(); index++)
		{
			DataPoint p = this.plotValues_.get(index);
			
			/* If it's x(time) value is less than the latest point minus 10 secodns, then drop it */
			if (p.getX() + this.timeRange_ <= newPoint.getX())
			{
				this.plotValues_.remove(p);
				index--;
			}
		}
		
		
		
		/* Start a new data curve */
		DataPlot dataPlot = new DataPlot();
		DataCurve dataCurve = new DataCurve(getParameter().getName());
		DataCurve lowCurve = new DataCurve("Low");
		DataCurve highCurve = new DataCurve("High");

		double lowValue = Double.MAX_VALUE;
		double highValue = Double.MIN_VALUE;
		
		/* translate our list onto the graph */
		double tx = 0, ty = 0;
		for (DataPoint p : this.plotValues_)
		{
			/* I can't explain it, but for some reason, if I didn't get the values into a local variable
			 * first, the new oint was not working.  I have no idea why */
			tx = p.getX();
			ty = p.getY();
			
			/* translate the point back */
			if (tx >= this.timeRange_)
			{
				tx = tx - (newPoint.getX() - this.timeRange_);
			}
			
			/* Into the negative */
			tx -= this.timeRange_;
			tx /= 1000;
			
			DataPoint point = new DataPoint(tx,ty);
			dataCurve.addElement(point);
			
			lowValue = Math.min(lowValue, p.getY());
			highValue = Math.max(highValue, p.getY());
		}

		
		/* setup the low and high lines */
		lowCurve.addElement(new DataPoint(0 - this.timeRange_, lowValue));
		lowCurve.addElement(new DataPoint(0, lowValue));
		highCurve.addElement(new DataPoint(0 - this.timeRange_, highValue));
		highCurve.addElement(new DataPoint(0, highValue));

		/* set the high low parameters for the digital gauges */
		lowParameter_.setValue(lowValue);
		highParameter_.setValue(highValue);
		
		/* connect the curve to the canvas */
		dataPlot.addElement(dataCurve);
		dataPlot.addElement(lowCurve);
		dataPlot.addElement(highCurve);
		
		this.plotCanvas_.connect(dataPlot);


	}
	

	
	/******************************************************
	 * This internal class creates virtual parameters for the low and high
	 * value digital gauges.
	 *****************************************************/
	private static class HighLowParameter extends Parameter
	{
		private String name_ = "highlow";
		private double value_ = 0;
		
		
		/********************************************************
		 * @param name
		 *******************************************************/
		public void setName(String name)
		{
			this.name_ = name;
		}
		
		/*******************************************************
		 * Override
		 * @see net.sourceforge.JDash.ecu.param.Parameter#getName()
		 *******************************************************/
		@Override
		public String getName()
		{
			return this.name_;
		}
		
		/*******************************************************
		 * Override
		 * @see net.sourceforge.JDash.ecu.param.Parameter#getResult()
		 *******************************************************/
		@Override
		public double getResult()
		{
			return this.value_;
		}
		
		/*******************************************************
		 * @param v
		 *******************************************************/
		public void setValue(double v)
		{
			this.value_ = v;
			fireValueChangedEvent();
		}
	}
	

	
}
