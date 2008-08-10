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

import net.sourceforge.JDashLite.ecu.comm.ECUParameter;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener;
import net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter;
import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import waba.fx.Color;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Image;
import waba.fx.Rect;
import waba.sys.Convert;
import waba.util.Hashtable;

/*********************************************************
 * This is the container that does the heavy lifting of
 * setting up a container that represents a given 
 * profile object. 
 *
 *********************************************************/
public class ProfileRenderer implements RenderableProfileComponent
{
	
	public static final Color COLOR_LT_GRAY 	= Color.getColor(0xbb, 0xbb, 0xbb);
	public static final Color COLOR_GRAY 		= Color.getColor(0x88, 0x88, 0x88);
	public static final Color COLOR_DK_GRAY 	= Color.getColor(0x55, 0x55, 0x55);
	
	public static final Color COLOR_BACKGROUND	= Color.WHITE;
	public static final Color COLOR_BORDER		= Color.BLACK;

	/* This is an array of decreasing sized fonts available to all renderers */
	public static final Font[] AVAILABLE_FONTS = 
	{
		Font.getFont("SW", true, Font.BIG_SIZE),  		// H=30
		Font.getFont("SW", false, Font.BIG_SIZE), 		// H=28
		Font.getFont("SW", true, Font.NORMAL_SIZE), 	// H=22
		Font.getFont("SW", false, Font.NORMAL_SIZE),	// H=22
		Font.getFont("LSW", true, Font.BIG_SIZE),		// H=15
		Font.getFont("LSW", false, Font.BIG_SIZE),		// H=14
		Font.getFont("LSW", true, Font.NORMAL_SIZE),	// H=11
		Font.getFont("LSW", false, Font.NORMAL_SIZE),	// H=11
	};

	/* The percentage of the bottom of the screen that the status bar should take up */
	private static final double STATUS_BAR_HEIGHT_PERCENT = 0.06;

	/* Flag indicating that new ECU data is available */
	private boolean newDataReady_ = true;
	
	/* The profile this renderer is to render */
	private Profile profile_ = null;
	
	/* The list of parameters that are available for ECU values */
	private Hashtable parameters_ = new Hashtable(25);
	
	/* The bottom of the page status bar */
	private StatusBar statusBar_ = new StatusBar();
	

	/* The active page */
	private int activePage_ = 0;
	
	
	/* The previous rect used during the render process */
	private Rect previousRect_ = new Rect();
	
	/* The cache of rects to speed up re-calculating them each time */
	private Hashtable rectCache_ = new Hashtable(4);
	
	/* The event adapter used internaly */
	private ProtocolEventListener eventAdapter_ = new RenderingProtocolEventAdapter();
	
	
	/* The Double buffer static image.  As in, there are parts of the page that do NOT change
	 * as parameters are read.  These values do NOT need to be re-drawn and re-computed each time.
	 * This image is rendered to the screen, then the dynamic values are drawn on top. */
	private Image dblBufferdStaticImage_ = null;
	
	
	/** If there is a status messgae to be displayed, put it here */
	private String statusMessage_ = null;
	
	/********************************************************
	 * 
	 *******************************************************/
	public ProfileRenderer(Profile profile)
	{
		this.profile_ = profile;
		this.statusBar_.setPageCount(profile.getPageCount());
	}
	
	
	/*******************************************************
	 * @param parameters
	 ********************************************************/
	public void setParameters(ECUParameter[] parameters)
	{
		this.parameters_.clear();
		for (int index = 0; index < parameters.length; index++)
		{
			this.parameters_.put(parameters[index].getName(), parameters[index]);
		}
	}
	
	/********************************************************
	 * @return the eventAdapter
	 ********************************************************/
	public ProtocolEventListener getEventAdapter()
	{
		return this.eventAdapter_;
	}
	
//	/********************************************************
//	 * @return the newDataReady
//	 ********************************************************/
//	public boolean isNewDataReady()
//	{
//		return this.newDataReady_;
//	}
//	
//	
//	/********************************************************
//	 * @param newDataReady the newDataReady to set
//	 ********************************************************/
//	public void setNewDataReady(boolean newDataReady)
//	{
//		this.newDataReady_ = newDataReady;
//	}
	
	
	
	
	
	/*******************************************************
	 * Given the provided height, find the best font to fit inside that height.
	 * @param height
	 * @return
	 ********************************************************/
	public static Font findFontBestFitHeight(int height)
	{
		/* Find the best fit font */
		Font f = null;
		for (int index = 0; index < ProfileRenderer.AVAILABLE_FONTS.length; index++)
		{
			f = ProfileRenderer.AVAILABLE_FONTS[index];
			if (ProfileRenderer.AVAILABLE_FONTS[index].fm.height < height)
			{
				break;
			}
		}
		return f;
	}
	
	
	/********************************************************
	 * @return the activePage
	 ********************************************************/
	public int getActivePage()
	{
		return this.activePage_;
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
		
		this.activePage_ = activePage;
		this.newDataReady_ = true;
		this.dblBufferdStaticImage_ = null;
		this.statusBar_.setActivePage(this.activePage_);
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r) throws Exception
	{
		
		
		/* Not having a profile configured is a fatal exception */
		if (this.profile_ == null)
		{
			throw new Exception("No Profile Provided");
		}


		/* No parameters?  NO point! */
		if (this.parameters_ == null)
		{
			return;
		}
	

		/* Pre calculate ALL the drawable rects */
		calculateRects(g, r);

		
		/* Render any pending status message */
		renderStatusMessage(g, r.width, r.height / 2);
		
		/* If no data has been indicated as being ready, then don't draw the dynamic gauge bits */
		if (!this.newDataReady_)
		{
			return;
		}

				
		/* Set the new data flag back to false, since we're about to render it anyway */
		this.newDataReady_ = false;

		/* Start with a clean screen */
		g.clearScreen();
		
		
		/* Draw the static content */
		if (this.dblBufferdStaticImage_ == null)
		{
			this.dblBufferdStaticImage_ = new Image(r.width, r.height);
			renderActivePage(this.dblBufferdStaticImage_.getGraphics(), true);
		}
 

		/* Render the active page */
		g.drawImage(this.dblBufferdStaticImage_, r.x, r.y);
		renderActivePage(g, false);
		
		/* Draw the status bar */
		this.statusBar_.render(g, (Rect)this.rectCache_.get(this.statusBar_));

	}
	
	
	
	/*******************************************************
	 * @param g
	 * @param buildStaticImage IN - Render the static content ONLY to the graphics context.  The "g" should be the image GC.
	 * 			If true, static only, if false, dynamic only.  So, to do a complete gaguge draw, this method will
	 * 			need to be called twice.
	 * @throws Exception
	 ********************************************************/
	private void renderActivePage(Graphics g, boolean staticContentOnly) throws Exception
	{
		
	
		/* Get the page */
		ProfilePage page = this.profile_.getPage(this.activePage_);
		Rect pageRect = (Rect)this.rectCache_.get(page);
		if (pageRect == null)
		{
			throw new Exception("Cannot render page: " + this.activePage_ + " the RECT cache has no entry for it");
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
					throw new Exception("Cannot render gauge: " + rowIndex + " on row " + rowIndex + " the RECT cache has no entry for it");
				}

				/* Render this gauge */
				renderGauge(g, gauge, gaugeRect, staticContentOnly);
		
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
						if (row.getGauge(gaugeIndex).getWidthPercent() > 0)
						{
							gaugeWidths[gaugeIndex] = (int)(row.getGauge(gaugeIndex).getWidthPercent() * (double)rowRect.width);
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
	private void renderGauge(Graphics g, ProfileGauge gauge, Rect rect, boolean staticContentOnly) throws Exception
	{
	
		/* Draw a simple borer around the rect with a filled BG  */
		g.setForeColor(COLOR_BORDER);
		g.setBackColor(COLOR_BACKGROUND);
		Font f = ProfileRenderer.findFontBestFitHeight(rect.height / 8);
		g.setFont(f);
		
		if (staticContentOnly)
		{
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		
		/* Get the parameter for this gauge */
		ECUParameter p = (ECUParameter)this.parameters_.get(gauge.getParameterName());
		
		/* No Param? */
		if (p != null)
		{
			
			if (staticContentOnly)
			{
				g.drawText(p.getName(), rect.x + 2, rect.y + 2);
			}
			else
			{
				g.drawText(Convert.toString(p.getValue(), 2), rect.x + 2, rect.y + rect.height - (rect.height / 2));
			}
		}
		
	}
	
	
	
	/*******************************************************
	 * Draw the status message, if there is one, on top of
	 * and in the middle of everything.
	 * @param msg
	 ********************************************************/
	private void renderStatusMessage(Graphics g, int width, int yCenter)
	{
		if (this.statusMessage_ != null)
		{
			
			/* Calc positions */
			Font f = Font.getFont("SW", true, Font.NORMAL_SIZE);
			int textHeight = f.fm.height;
			int textWidth = f.fm.getTextWidth(this.statusMessage_);
			
			/* Draw a window box */
			g.setBackColor(Color.CYAN);
			g.fillRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
			g.setForeColor(COLOR_BORDER);
			g.drawRect(10, yCenter - (textHeight / 2) - 10, width - 20, textHeight + 20);
			
			/* Draw the text */
			g.drawText(this.statusMessage_, (width - textWidth) / 2, yCenter - (textHeight / 2));
			
		}
	}
	
	
	/********************************************************
	 * Not a static class, this event adapter acts on the messages
	 * to adjust what is displayed.
	 *
	 *********************************************************/
	private class RenderingProtocolEventAdapter extends ProtocolEventAdapter
	{
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initStarted()
		 ********************************************************/
		public void initStarted()
		{
			//System.out.println("init started " + System.currentTimeMillis());
		}
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initFinished()
		 ********************************************************/
		public void initFinished()
		{
			statusMessage_ = null;
			//System.out.println("init finished "  + System.currentTimeMillis());
		}
		
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#initStatus(java.lang.String)
		 ********************************************************/
		public void initStatus(String statusMessage)
		{
			statusMessage_ = statusMessage;
		}
		
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see net.sourceforge.JDashLite.ecu.comm.ProtocolEventListener.ProtocolEventAdapter#endParameterBatch()
		 ********************************************************/
		public void endParameterBatch()
		{
			//System.out.println("end parameter batch " + System.currentTimeMillis());
			newDataReady_  = true;
		}
	}
}
