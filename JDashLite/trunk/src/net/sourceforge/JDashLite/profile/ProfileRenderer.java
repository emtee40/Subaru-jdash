/*********************************************************
 * 
 * @author spowell
 * ProfileDisplayContainer.java
 * Jul 26, 2008
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

package net.sourceforge.JDashLite.profile;

import net.sourceforge.JDashLite.Cleanable;
import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener;
import net.sourceforge.JDashLite.ecu.comm.ProtocolHandler;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter;
import net.sourceforge.JDashLite.error.ErrorLog;
import net.sourceforge.JDashLite.profile.color.ColorModel;
import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import waba.fx.Color;
import waba.fx.Coord;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Rect;
import waba.ui.PenEvent;
import waba.util.Hashtable;
import waba.util.Vector;

/*********************************************************
 * This is the container that does the heavy lifting of
 * setting up a container that represents a given 
 * profile object. 
 *
 *********************************************************/
public class ProfileRenderer 
{
	
	/* This is an array of decreasing sized fonts available to all renderers */
	public static final Font[] AVAILABLE_FONTS = 
	{
	
		Font.getFont("SW", false, Font.BIG_SIZE), 				/* height = 28 */
		Font.getFont("SW", false, Font.NORMAL_SIZE), 			/* height = 22 */
	//	Font.getFont("Tahoma", false, Font.BIG_SIZE),  			/* Height = 17 */
		Font.getFont("Verdana", false, Font.BIG_SIZE), 			/* Height = 16 */
	//	Font.getFont("Tahoma", false, Font.NORMAL_SIZE),  		/* Height = 15 Same size as TinyLarge NORMAl but this has tails!! */
	//	Font.getFont("TinyLarge", false, Font.NORMAL_SIZE), 	/* height = 14 */
		Font.getFont("Verdana", false, Font.NORMAL_SIZE), 		/* Height <= 14   Verdana also has tails */
	//	Font.getFont("Arial", false, Font.NORMAL_SIZE),  		/* Height = 13 */
		Font.getFont("TinySmall", false, Font.NORMAL_SIZE)  	/* Height = 6 (ugly) */

	};

	/* The percentage of the bottom of the screen that the status bar should take up */
	private static final double STATUS_BAR_HEIGHT_PERCENT = 0.06;
	
	/* Flag indicating that new ECU data is available */
//	private boolean refreshGauges_ = true;
//	private boolean refreshStatusBar_ = true;
//	private boolean forceGaugeCompleteRefresh_ = true;
	
	/* The profile this renderer is to render */
	private Profile profile_ = null;
	
	/* The list of parameters that are available for ECU values */
//	private Hashtable parameters_ = new Hashtable(25);
	
	/* The bottom of the page status bar */
	private StatusBar statusBar_ = new StatusBar();
	

	/* The active page */
	private int currentlyActivePage_ = 0;
	
	
	/* The previous rect used during the render process */
	private Rect previousRect_ = new Rect();
	
	/* The cache of rects to speed up re-calculating them each time */
	private Hashtable rectCache_ = new Hashtable(4);
	
	/* The event adapter used internaly */
//	private ProtocolEventListener eventAdapter_ = new RenderingProtocolEventAdapter();
	
	
	/* The Double buffer static image.  As in, there are parts of the page that do NOT change
	 * as parameters are read.  These values do NOT need to be re-drawn and re-computed each time.
	 * This image is rendered to the screen, then the dynamic values are drawn on top. */
	//private Image dblBufferdStaticImage_ = null;
	
	
//	/** If there is a status messgae to be displayed, put it here */
//	private String statusMessage_ = null;
	
	private boolean fetchVisibleOnly_ = false;
	
	
	/* The FIRST render call needs to render EVERYBODY at least once.. first. */
	private boolean firstTimeRender_ = true;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileRenderer(Profile profile, ProtocolHandler protocol)
	{
		this.profile_ = profile;
		
		/* for quick access and simplicity */
		Hashtable pHash = new Hashtable(protocol.getSupportedParameters().length);
		for (int index = 0; index < protocol.getSupportedParameters().length; index++)
		{
			pHash.put(protocol.getSupportedParameters()[index].getName(), protocol.getSupportedParameters()[index]);
			
			/* While we're here, disable ALL parameters to start off */
			protocol.getSupportedParameters()[index].setEnabled(false);
			
		} /* end parameter loop */
		
		
		/* Walk through each page/row/gauge, and hand out the ecu parameter needed to each of them */
		for (int pageIndex = 0; pageIndex < profile.getPageCount(); pageIndex++)
		{
			ProfilePage page = profile.getPage(pageIndex);
			for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
			{
				ProfileRow row = page.getRow(rowIndex);
				for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
				{
					ProfileGauge gauge = row.getGauge(gaugeIndex);
					ECUParameter ecuParam = (ECUParameter)pHash.get(gauge.getProperty(ProfileGauge.PROP_STR_PARAMETER_NAME));
					if (ecuParam == null)
					{
						throw new RuntimeException("A Gauge is configured to display an unknown parameter [" + gauge.getProperty(ProfileGauge.PROP_STR_PARAMETER_NAME) + "]");
					}
					gauge.setECUParameter(ecuParam);
				}
			}
		}
		
		
		/* Turn on ONLY the params that are at least part of this profile */
//		enableDisableParameters(false);
		
		this.statusBar_.setPageCount(profile.getPageCount());
	}
	
	
	
//	/********************************************************
//	 * @return the eventAdapter
//	 ********************************************************/
//	public ProtocolEventListener getEventAdapter()
//	{
//		return this.eventAdapter_;
//	}

	
	
	/*******************************************************
	 * Given the provided height, find the best font to fit inside that height.
	 * @param height
	 * @return
	 ********************************************************/
	public static Font findFontBestFitHeight(int height, boolean bold)
	{
		/* Find the best fit font */
		Font f = null;
		for (int index = 0; index < ProfileRenderer.AVAILABLE_FONTS.length; index++)
		{
			f = ProfileRenderer.AVAILABLE_FONTS[index];
			
			if (bold == true)
			{
				f = f.asBold();
			}
				
			if ((f.fm.height - f.fm.descent) <= height)
			{
				break;
			}
		}
		return f;
	}
	
	
	/*******************************************************
	 * Given the provided height, find the best font to fit inside that height.
	 * @param height
	 * @return
	 ********************************************************/
	public static Font findFontBestFitWidth(int width, String s, boolean bold)
	{
		/* Find the best fit font */
		Font f = null;
		for (int index = 0; index < ProfileRenderer.AVAILABLE_FONTS.length; index++)
		{
			f = ProfileRenderer.AVAILABLE_FONTS[index];
			if (bold == true)
			{
				f = f.asBold();
			}
			if (ProfileRenderer.AVAILABLE_FONTS[index].fm.getTextWidth(s) <= width)
			{
				break;
			}
		}
		return f;
	}
	
	
	/********************************************************
	 * @return
	 ********************************************************/
	public StatusBar getStatusBar()
	{
		return this.statusBar_;
	}
	
	/*******************************************************
	 * When the main window gets a pen event, it will forward
	 * it to the renderer.
	 * @param evt
	 ********************************************************/
	public void onPenDown(PenEvent evt)
	{
		
		/* Check for, and respond to page clicks */
		int pageButtonClicked = getPageButtonAt(evt.x, evt.y);
		if (pageButtonClicked > -1)
		{
			setActivePage(pageButtonClicked);
		}
		
	}
	
	
	/********************************************************
	 * @return the activePage
	 ********************************************************/
	public int getActivePage()
	{
		return this.currentlyActivePage_;
	}
	
	
	/********************************************************
	 * @param activePage the activePage to set
	 ********************************************************/
	public void setActivePage(int activePage)
	{
		/* don't go past the last page */
		if (activePage >= this.profile_.getPageCount())
		{
			return;
		}
		
		/* And don't go before the first page */
		if (activePage < 0)
		{
			return;
		}
		
		this.currentlyActivePage_ = activePage;
//		this.refreshGauges_ = true;
//		this.forceGaugeCompleteRefresh_ = true;
//		this.refreshStatusBar_ = true;
//		this.dblBufferdStaticImage_ = null;
		this.statusBar_.setActivePage(this.currentlyActivePage_);
		
		/* enable or disable the desired parameters */
		enableDisableParameters(this.fetchVisibleOnly_);
			
			
	}
	

	/*******************************************************
	 * @param visibleOnly
	 ********************************************************/
	public void enableDisableParameters(boolean visibleOnly)
	{
		this.fetchVisibleOnly_ = visibleOnly;
		
		
		/* Since a gauge can be on multiple pages, we need to simply cache the ones to be enabled,
		 * all the while disabling all of them.  The, turn back on the ones we want */
		Vector toEnableParams = new Vector(5);
		
		
		/* By default, enable ONLY the parameters that are found in any gauge in the profile */
		for (int pageIndex = 0; pageIndex < this.profile_.getPageCount(); pageIndex++)
		{
			

			ProfilePage page = this.profile_.getPage(pageIndex);
			for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
			{
				ProfileRow row = page.getRow(rowIndex);
				for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
				{
					ProfileGauge gauge = row.getGauge(gaugeIndex);
					
					/* The default, is to disable */
					gauge.getECUParameter().setEnabled(false);

					/* If we want visable only, then we need to check the active page id */
					if (this.fetchVisibleOnly_)
					{
						if (pageIndex == this.currentlyActivePage_)
						{
							if (toEnableParams.indexOf(gauge.getECUParameter()) < 0)
							{
								toEnableParams.addElement(gauge.getECUParameter());
							}
						}
					}
					else
					{
						gauge.getECUParameter().setEnabled(true);
					}
					
				} /* end gauge loop */
				

			} /* end row loop */
			
			
		} /* end page loop */

		
		for (int index = 0; index < toEnableParams.size(); index++)
		{
			ECUParameter p = (ECUParameter)toEnableParams.items[index];
			p.setEnabled(true);
		}
	}
	
	
	/********************************************************
	 * @param pageIndex
	 * @param rowIndex
	 * @param gaugeIndex
	 * @return
	 ********************************************************/
	public Rect getGaugeRect(int pageIndex, int rowIndex, int gaugeIndex)
	{
		ProfilePage page = this.profile_.getPage(pageIndex);
		ProfileRow row = page.getRow(rowIndex);
		ProfileGauge gauge = row.getGauge(gaugeIndex);
		return (Rect)this.rectCache_.get(gauge);
	}
	
	
	/*******************************************************
	 * If any part of a visible gauge is located at the given
	 * x,y, then return it's index.
	 * @param pageIndex IN - the page to inspect.  -1 will inspect the currently visible page.
	 * @param x
	 * @param y
	 * @return the gauge index within the row, -1 for no gauge.
	 ********************************************************/
	public int getGaugeAt(int pageIndex, int x, int y)
	{
		
		int rowIndex = getRowAt(pageIndex, x, y);
		if (rowIndex == -1)
		{
			return -1;
		}
		
		
		ProfilePage page = this.profile_.getPage(pageIndex);		
		ProfileRow row = page.getRow(rowIndex);
		
		for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
		{
			ProfileGauge gauge = row.getGauge(gaugeIndex);
			Rect gaugeRect = (Rect)this.rectCache_.get(gauge);

			if (gaugeRect.contains(x, y))
			{
				return gaugeIndex;
			}
		}
		
		return -1;
	}
	
	
	/*******************************************************
	 * If any part of a row is located at the given index, then return its index.
	 * @param pageIndex IN - the page to inspect.  -1 will inspect the currently visible page.
	 * @param x
	 * @param y
	 * @return the row index on the page. -1 for no row.
	 ********************************************************/
	public int getRowAt(int pageIndex, int x, int y)
	{
		
		/* Walk through each gauge, an get it's rect.  If the x+y fall inside the rect, this is it! */
		ProfilePage page = this.profile_.getPage(pageIndex);
		
		for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
		{
			ProfileRow row = page.getRow(rowIndex);
			Rect rowRect = (Rect)this.rectCache_.get(row);
			
			if (rowRect.contains(x, y))
			{
				return rowIndex;
			}
			
		}

		return -1;

	}
	
	/********************************************************
	 * If any of the page button is located in the given x,y, then return it's index.
	 * @param pageIndex IN - the page to inspect.  -1 will inspect the currently visible page.
	 * @param x
	 * @param y
	 * @return the page button index, -1 if no page was there.
	 ********************************************************/
	public int getPageButtonAt(int x, int y)
	{
		return this.statusBar_.getPageIndex(x, y);
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r, ColorModel cm, boolean forceRepaint) throws Exception
	{
		
		/* Not having a profile configured is a fatal exception */
		if (this.profile_ == null)
		{
			throw new Exception("No Profile Provided");
		}


		
		
		/* Pre calculate ALL the drawable rects */
		calculateRects(g, r);

		

		/* Render the active page  */
		//if (this.refreshGauges_  forceRedrawAll)
		{
//			this.refreshStatusBar_ = true;

			/* If this is the firstTiemRender, then we need to pre-render EVERY page.  Why? Some gauges need the
			 * RECT and Graphic data before even any ecu values come through. Becuase gauges like line graphs are dependant
			 * on ECU Value change evnets to get their data, but the size of the value history buffer is dependant on the
			 * rect of the gauge area.  I know, not ideal, but it's only at the first run of this renderer. */
			if (this.firstTimeRender_)
			{
				
				/* PreRender each page once */
				for (int pageIndex = 0; pageIndex < this.profile_.getPageCount(); pageIndex++)
				{
					/* Except the current page */
					if (pageIndex != this.currentlyActivePage_)
					{
						renderPage(g, cm, pageIndex, true);
					}
				}
				/* Now, force the current page last */
				renderPage(g, cm, this.currentlyActivePage_, true);
				
				/* Disable the first-render flag */
				this.firstTimeRender_ = false;
			}
			else
			{
				renderPage(g, cm, this.currentlyActivePage_, forceRepaint /*|| this.forceGaugeCompleteRefresh_*/);
			}
			
//			this.refreshGauges_ = false;
		}

//		
//		/* turn the first time off, and go again */
//		if (this.firstTimeRender_)
//		{
//			
//			this.firstTimeRender_ = false;
//			render(g,r, cm, forceRepaint);
//		}

		
		/* Draw the status bar */
//		if (refreshStatusBar_)
		{
			this.statusBar_.render(g, (Rect)this.rectCache_.get(this.statusBar_), cm);
//			this.refreshStatusBar_ = false;
		}


//		/* Render any pending status message */
//		renderStatusMessage(g, cm, r.width, r.height / 2);


		/* Always switch this flag back to off */
//		this.forceGaugeCompleteRefresh_ = false;

	}
	
	
	
	/*******************************************************
	 * @param g
	 * @param buildStaticImage IN - Render the static content ONLY to the graphics context.  The "g" should be the image GC.
	 * 			If true, static only, if false, dynamic only.  So, to do a complete gaguge draw, this method will
	 * 			need to be called twice.
	 * @throws Exception
	 ********************************************************/
	private void renderPage(Graphics g, ColorModel cm, int pageIndex, boolean forceRepaint) throws Exception
	{

		/* No Pages? */
		if (this.profile_.getPageCount() == 0)
		{
			return;
		}
	
		/* Get the page */
		ProfilePage page = this.profile_.getPage(pageIndex);
		Rect pageRect = (Rect)this.rectCache_.get(page);
		if (pageRect == null)
		{
			throw new Exception("Cannot render page: " + pageIndex + " the RECT cache has no entry for it");
		}

		
		/* For each row */
		for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
		{
			ProfileRow row = page.getRow(rowIndex);
			Rect rowRect = (Rect)this.rectCache_.get(row);
			if (rowRect == null)
			{
				throw new Exception("Cannot render row: " + rowIndex + " the RECT cache has no entry for it");
			}
			
			
			/* For each gauge in this row */
			for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
			{
	
				ProfileGauge gauge = row.getGauge(gaugeIndex);
				Rect gaugeRect = (Rect)this.rectCache_.get(gauge);
				
				if (gaugeRect == null)
				{
					ErrorLog.error("Cannot render gauge: " + rowIndex + " on row " + rowIndex + " the RECT cache has no entry for it");
				}

				/* Render this gauge */
				if (gauge != null)
				{
					renderGauge(g, gaugeRect, gauge, cm, forceRepaint);
				}
				else
				{
					ErrorLog.fatal("Row " + rowIndex + " returned a null gauge at " + gaugeIndex);
				}
		
			}
		}
	
	}
	
	
	/********************************************************
	 * @param r
	 * @param r
	 ********************************************************/
	private void calculateRects(Graphics g, Rect clientRect) throws Exception
	{
		
		/* If the rects are equal, then we've no need to re-calc */
		if (clientRect.equals(this.previousRect_))
		{
			return;
		}
	
		/* Cache the rect */
		this.previousRect_ = clientRect;
		this.rectCache_.clear();
		

		
		/* Calculate the page and status heights */
		int pageHeight = clientRect.height - (int)((double)clientRect.height * STATUS_BAR_HEIGHT_PERCENT);
		int statusHeight = clientRect.height - pageHeight;

		/* Calculate the status rect */
		Rect statusRect = new Rect();
		statusRect.set(clientRect);
		statusRect.height = statusHeight;
		statusRect.y = pageHeight;
		this.rectCache_.put(this.statusBar_, statusRect);

		/* Calcuate the page rect */
		Rect pageRect = new Rect();
		pageRect.set(clientRect);
		pageRect.height = pageHeight;
		
		/* For each page */
		for (int pageIndex = 0; pageIndex < this.profile_.getPageCount(); pageIndex++)
		{
		
			/* Get the page */
			ProfilePage page = this.profile_.getPage(pageIndex);
			this.rectCache_.put(page, pageRect);
	
			/* No rows?  Next page */
			if (page.getRowCount() <= 0)
			{
				continue;
			}
			
			
			{
				
				int[] rowHeights = new int[page.getRowCount()];
				int heightOffset = 0;
				int rowsWithNoPercentValue = 0;
				
				/* Calculate each row height for the ones that were provided with a percent value */
				for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
				{
					/* If it's not -1, then calculate the percent */
					if (page.getRow(rowIndex).getHeightPercent() > 0)
					{
						rowHeights[rowIndex] = (int)(page.getRow(rowIndex).getHeightPercent() * (double)pageRect.height);
						heightOffset += rowHeights[rowIndex];
					}
					else
					{
						rowsWithNoPercentValue++;
						rowHeights[rowIndex] = -1;
					}
				}
				
				/* make sure the percentages are not over our limit  */
				if (heightOffset > pageRect.height)
				{
					throw new Exception("The row percentage values exceed 100% for page " + pageIndex);
				}
				
				/* calculate the remaining unspecified row heights */
				for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
				{
					if (rowHeights[rowIndex] == -1)
					{
						rowHeights[rowIndex] = (pageRect.height - heightOffset) / rowsWithNoPercentValue;
					}
				}
				
		
				/* Calculate each row rect */
				heightOffset = 0;
				for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
				{
					Rect rowRect = new Rect(pageRect.x, heightOffset, pageRect.width, rowHeights[rowIndex]);
					this.rectCache_.put(page.getRow(rowIndex), rowRect);
					heightOffset += rowHeights[rowIndex] - 1;
					
					/* If this is the last row, then look for the result of percentage rounding errors */
					if (rowIndex == page.getRowCount() - 1)
					{
						if (rowRect.y + rowRect.height != pageRect.height)
						{
							rowRect.height = pageRect.height - rowRect.y;
						}
					}
				}
			}
	
			
			/* Calculate each gauge rect for each row */
			{
				for (int rowIndex = 0; rowIndex < page.getRowCount(); rowIndex++)
				{
					
					/* Get the row */
					ProfileRow row = page.getRow(rowIndex);
					
					/* No guages?  No need to continue this row */
					if (row.getGaugeCount() <= 0)
					{
						continue;
					}
	
					/* Get the row rect */
					Rect rowRect = (Rect)this.rectCache_.get(row);
					if (rowRect == null)
					{
						throw new Exception("The row[" + rowIndex + "] RECT was not in the cache.");
					}
					
		
					int[] gaugeWidths = new int[row.getGaugeCount()];
					int widthOffset = 0;
					int gaugesWithNoPercentValue = 0;
					
					/* Calculate each gauge widths for the ones that were provided with a percent value */
					for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
					{
						/* If it's not -1, then calculate the percent */
						if (row.getGauge(gaugeIndex).getDoubleProperty(ProfileGauge.PROP_D_WIDTH, -1) > 0)
						{
							gaugeWidths[gaugeIndex] = (int)(row.getGauge(gaugeIndex).getDoubleProperty(ProfileGauge.PROP_D_WIDTH, -1) * (double)rowRect.width);
							widthOffset += gaugeWidths[gaugeIndex] ;
						}
						else
						{
							gaugesWithNoPercentValue++;
							gaugeWidths[gaugeIndex] = -1;
						}
					}
					
					
					/* make sure the percentages are not over our limit  */
					if (widthOffset > pageRect.width)
					{
						throw new Exception("The gauge percentage values exceed 100% for row " + rowIndex + " page " + pageIndex);
					}
					
					/* calculate the remaining unspecified row heights */
					for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
					{
						if (gaugeWidths[gaugeIndex] == -1)
						{
							gaugeWidths[gaugeIndex] = (pageRect.width - widthOffset) / gaugesWithNoPercentValue;
						}
					}
					
					
					
					/* Calcuate the gauge rect */
					widthOffset = 0;
					for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
					{
						Rect gaugeRect = new Rect(widthOffset, rowRect.y, gaugeWidths[gaugeIndex], rowRect.height);
						ProfileGauge gauge = row.getGauge(gaugeIndex);
						this.rectCache_.put(gauge, gaugeRect);
						widthOffset += gaugeWidths[gaugeIndex] - 1;
	
						/* If this is the last gauge, then look for the result of percentage rounding errors */
						if (gaugeIndex == row.getGaugeCount() - 1)
						{
							if (gaugeRect.x + gaugeRect.width != pageRect.width)
							{
								gaugeRect.width = pageRect.width - gaugeRect.x;
							}
						}
						
						
					}
					
				}
			}
		
		} /* End for pageIndex loop */

	}
	
	
	
	/********************************************************
	 * @param gauge
	 * @param rect
	 * @throws Exception
	 ********************************************************/
	private void renderGauge(Graphics g, Rect rect, ProfileGauge gauge, ColorModel cm, boolean forceRepaint) throws Exception
	{
	
		
		/* Get the parameter for this gauge */
		String parameteName = gauge.getProperty(ProfileGauge.PROP_STR_PARAMETER_NAME);
		if (parameteName == null)
		{
			ErrorLog.error("Gauge " + gauge + " does not have a parameter identified");
		}
		
		gauge.render(g, rect, cm, forceRepaint);
		
	}
	
	
	
//	/*******************************************************
//	 * Draw the status message, if there is one, on top of
//	 * and in the middle of everything.
//	 * @param msg
//	 ********************************************************/
//	private void renderStatusMessage(Graphics g, ColorModel cm, int width, int yCenter)
//	{
//		if (this.statusMessage_ != null)
//		{
//			
//			/* Calc positions */
//			//Font f = findFontBestFitWidth(width, this.statusMessage_, true);
//			Font f = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);
//			int textHeight = f.fm.height - f.fm.descent;
//			int textWidth = f.fm.getTextWidth(this.statusMessage_);
//			
//			/* Draw a window box */
//			g.setBackColor(Color.CYAN);
//			g.fillRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
//			g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
//			g.drawRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
//			
//			/* Draw the text */
//			g.drawText(this.statusMessage_, (width / 2) - (textWidth / 2), yCenter - (textHeight / 2));
//			
//		}
//	}
	
	
	/*******************************************************
	 * @param points
	 ********************************************************/
	public static int[] toXArray(Coord[] points)
	{
		int [] r = new int[points.length];
		
		for (int index = 0; index < r.length; index++)
		{
			r[index] = points[index].x;
		}
		
		return r;
	}
	
	
	/******************************************************
	 * @param points
	 * @return
	 ********************************************************/
	public static int[] toYArray(Coord[] points)
	{

		int [] r = new int[points.length];
		
		for (int index = 0; index < r.length; index++)
		{
			r[index] = points[index].y;
		}
		
		return r;

	}
	
	
	
	
//	
//	
//	/********************************************************
//	 * Not a static class, this event adapter acts on the messages
//	 * to adjust what is displayed.
//	 *
//	 *********************************************************/
//	private class RenderingProtocolEventAdapter extends ProtocolEventAdapter
//	{
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initStarted()
//		 ********************************************************/
//		public void initStarted()
//		{
//			statusMessage_ = null;
//			refreshGauges_ = true;
//			//System.out.println("init started " + System.currentTimeMillis());
//		}
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initFinished()
//		 ********************************************************/
//		public void initFinished()
//		{
//			statusMessage_ = null;
//			refreshGauges_ = true;
//			//System.out.println("init finished "  + System.currentTimeMillis());
//		}
//		
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initStatus(java.lang.String)
//		 ********************************************************/
//		public void initStatus(String statusMessage)
//		{
//			statusMessage_ = statusMessage;
//		}
//		
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#parameterFetched(net.sourceforge.JDashLite.ecu.comm.ECUParameter)
//		 ********************************************************/
//		public void parameterFetched(ECUParameter p)
//		{
//			refreshGauges_ = true;
//		}
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#endParameterBatch()
//		 ********************************************************/
//		public void endParameterBatch()
//		{
//			//System.out.println("end parameter batch " + System.currentTimeMillis());
//			refreshGauges_  = true;
//		}
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#commReady()
//		 ********************************************************/
//		public void commReady()
//		{
//			statusBar_.setRXTXMode(StatusBar.RXTX_READY);
//			refreshStatusBar_  = true;
//		}
//		
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#commRX()
//		 ********************************************************/
//		public void commRX()
//		{
//			statusBar_.setRXTXMode(StatusBar.RXTX_RECEIVE);
//			refreshStatusBar_  = true;
//		}
//		
//		
//		/*********************************************************
//		 * (non-Javadoc)
//		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#commTX()
//		 ********************************************************/
//		public void commTX()
//		{
//			statusBar_.setRXTXMode(StatusBar.RXTX_SEND);
//			refreshStatusBar_  = true;
//		}
//	}
}
