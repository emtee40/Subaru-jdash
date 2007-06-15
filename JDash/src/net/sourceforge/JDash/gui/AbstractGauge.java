/*******************************************************
 * 
 *  @author spowell
 *  AnalogGauge.java
 *  Aug 8, 2006
 *  $Id: AbstractGauge.java,v 1.6 2006/12/31 16:59:10 shaneapowell Exp $
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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Observable;
import java.util.Observer;

import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * An abstract gauge component.  Most of what is displayed
 * on the screen is some sort of Gauge component.  A gauge
 * is nothing more than a component that can display a changing
 * value to the user.  It can be a digital value, and analog
 * sweep gauge, and LED panel.... etc.  Gauges ALL extend
 * this class.  It is the job of the extended version
 * of this class to do the heavy lifting and making it
 * look like it should.
 ******************************************************/
public abstract class AbstractGauge implements Observer
{
	
	
	/** The ECU parameter that this gauge is setup to display values for */
	private Parameter parameter_ = null;
	
	/** The owner panel */
	private GaugePanel parentPanel_ = null;
	
	/** The previous render bounds */
	private Rectangle previousBounds_ = null;
	
	
	/******************************************************
	 * Create a new default analog gauge.
	 * @param parameter IN - the parameter this gauge represents.
	 * @param parentPanel IN - the parent gauge panel this gauge will
	 * be drawn inside. We'll need it's getScalingTransform() method.
	 ******************************************************/
	public AbstractGauge(Parameter parameter, GaugePanel parentPanel)
	{
		this.parameter_ = parameter;
		
		/* It's possible for the parameter to be null */
		if (this.parameter_ != null)
		{
			this.parameter_.addObserver(this);
		}
		
		this.parentPanel_ = parentPanel;
		
	}

	
	/*******************************************************
	 * Get the parent gauge panel
	 * @return the parent gauge panel.
	 *******************************************************/
	public GaugePanel getParentPanel()
	{
		return this.parentPanel_;
	}
	
	/*******************************************************
	 * Drawing of a gauge requires a 2 step process.  First, when
	 * the sensor sends it's update, we get bounds that it will
	 * be rendered in.  This is to help the parent panel
	 * determine what rect to render.  Also, to max performance,
	 * this method gives the gauge the chance to preGenerate
	 * it's graphics.  Because the method paint() is going to be
	 * called right after this method.  If you return a null from
	 * this method, then your gauge will NOT get a paint() call. 
	 * This will increase performance because you can keep track
	 * of if the paramter value being watched actually changed or
	 * not.  If no change is detected, then return a null. But..
	 * watch for the force flag.  If this boolean is set to true,
	 * then ignore the fact that your parameter hasn't changed, and
	 * do a redraw anyway.
	 * <br>
	 * Note: You MUST make this method and the paint() abstract methods Thread safe. 
	 * They get called from different threads.  The easiest way should be to just
	 * make the methods synchronized. This should work for most cases. But, don't assume
	 * it's a perfect fix.
	 * 
	 * @param scalingTransform IN - the scaling transform that will be passed
	 * into the paint() method.
	 * @param force IN - force a preGen. In other words, when this is set, you should
	 * NOT be returning a null rect.
	 * @return the bounds rectangle.  If a null is returned, then this
	 * gaugae will not cause a repaint.  It will be assumed that a null
	 * means that nothing has changed, and a redraw is not needed.
	 *******************************************************/
	public abstract Rectangle preGenerate(AffineTransform scalingTransform, boolean force);
	
	
	/*******************************************************
	 * Once the panel is ready to draw itself, each gauge will
	 * get a change to draw itself also. Before this method
	 * is called preGenerate() will be called.  Giving
	 * a chance to speed things up.  The tranform passed
	 * in the call to preGenerate() will also be passed
	 * into this paint method. This way you won't have to 
	 * remember it for 2 sequencial calls.  When it comes time to
	 * actually draw the image, it is recomended that you
	 * use the paintShape() or paintShapes() method found
	 * witin the parent panel GaugePanel class. These methods
	 * are setup to already know the special ins/outs of each
	 * type of AbstractShape.  This way, you won't have to 
	 * worry about the special cases. But, it's up to you since
	 * not all shapes conform to the awtShape interface. Like 
	 * the text shape.  It uses fonts and glyphs.
	 * You can get to the parent panel by calling getParentPanel();
	 * <br>
	 * NOTE: You MUST make this method, and the preGenerate() methods
	 * both thread safe.  They both get called from different threads.
	 *   The easiest way should be to just
	 * make the methods synchronized. This should work for most cases. But, don't assume
	 * it's a perfect fix.
	 * 
	 * @param g2 IN - the graphics context to draw with.
	 * @param scalingTransform IN - the tranform to apply that will
	 * adjust to the size of the parent panel. This is the same
	 * transform that would be returned if calling getParent().getScalingTransform()
	 *******************************************************/
	public abstract void paint(Graphics2D g2, AffineTransform scalingTransform);


	
	/*******************************************************
	 * get the parameter object linked to this gauge.
	 * @return the ecu parameter.
	 *******************************************************/
	public Parameter getParameter()
	{
		return this.parameter_;
	}
	
	

	/*******************************************************
	 * This method is called when a gauges parameter value has changed.
	 * The underlying monitor is sending update messages as each 
	 * value comes in.  When an update is detected, then 
	 * each of the valueChanged listeners will get fired. 
	 * 
	 * @param obs IN - the observable paramerer. This object is not used since
	 * this gauge can only respond to one parameter anyway.
	 * @param obj IN - the extra object value. This is also not normally used except 
	 * for when a GaugePanel resize event occurs.  the gauge panel will
	 * call update() on all gauges, passing a Boolean.TRUE into the obj param.
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *******************************************************/
	public void update(Observable obs, Object obj)
	{
		
		/* If the parent gauge panel is flaged updates as suspended, then we'll stop here */
		if (getParentPanel().isGaugeDisplayUpdateSuspended() == true)
		{
			return;
		}
		
		
		/* Get the to-be-drawn bounds */
		Rectangle repaintBoundsActual = preGenerate(getParentPanel().getScalingTransform(),  Boolean.TRUE.equals(obj));
		
		/* If the bounds are null, then nothing new is needed to be drawn */
		if (repaintBoundsActual == null)
		{
			return;
		}
		
		/* Increase the rect by a bit to compensate for rounding errors in all directions */
		repaintBoundsActual = new Rectangle(repaintBoundsActual.x - 2,
											repaintBoundsActual.y - 2,
											repaintBoundsActual.width + 4,
											repaintBoundsActual.height + 4);
		
		Rectangle repaintBoundsNew = (Rectangle)repaintBoundsActual.clone();
		
		
		/* Add the previous bounds to the actual */
		if (this.previousBounds_ != null)
		{
			repaintBoundsActual.add(this.previousBounds_);
		}
		
		/* Remember the new bounds as the next previous */
		this.previousBounds_ = repaintBoundsNew; 
		
		/* Call the repaint */
		this.parentPanel_.repaint(repaintBoundsActual);
		
		
	}
	
}
