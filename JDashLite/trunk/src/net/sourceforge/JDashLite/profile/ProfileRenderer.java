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
import net.sourceforge.JDashLite.profile.gauge.ProfileGauge;
import waba.fx.Color;
import waba.fx.Font;
import waba.fx.Graphics;
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
	private boolean newDataReady_ = false;
	
	/* The profile this renderer is to render */
	private Profile profile_ = null;
	
	/* The list of parameters that are available for ECU values */
	private ECUParameter[] parameters_ = null;
	
	/* The bottom of the page status bar */
	private StatusBar statusBar_ = new StatusBar();
	

	/** The active page */
	private int activePage_ = 0;
	
	
	/* The previous rect used during the render process */
	private Rect previousRect_ = new Rect();
	
	/* The cache of rects to speed up re-calculating them each time */
	private Hashtable rectCache_ = new Hashtable(4);
	
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
		this.parameters_ = parameters;
	}
	
	/********************************************************
	 * @return the newDataReady
	 ********************************************************/
	public boolean isNewDataReady()
	{
		return this.newDataReady_;
	}
	
	
	/********************************************************
	 * @param newDataReady the newDataReady to set
	 ********************************************************/
	public void setNewDataReady(boolean newDataReady)
	{
		this.newDataReady_ = newDataReady;
	}
	
	
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
		this.statusBar_.setActivePage(this.activePage_);
	}
	
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r) throws Exception
	{
		
		/* Pre calculate ALL the drawable rects */
		calculateRects(g, r);
		
		
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
	
		/* If no data has been indicated as being ready, then don't draw the moveable gauge bits */
		if (!isNewDataReady())
		{
			return;
		}
		
		
		/* Set the new data flag back to false, since we're about to render it anyway */
		setNewDataReady(false);
		
		/* Draw the status bar */
		this.statusBar_.render(g, (Rect)this.rectCache_.get(this.statusBar_));
		
		/* Draw the active page */
		renderActivePage(g);
		
	}
	
	
	
	/********************************************************
	 * @param g
	 * @param r
	 * @throws Exception
	 ********************************************************/
	private void renderActivePage(Graphics g) throws Exception
	{
	
	
		/* Get the page */
		ProfilePage page = this.profile_.getPage(this.activePage_);
		Rect pageRect = (Rect)this.rectCache_.get(page);
		if (pageRect == null)
		{
			throw new Exception("Cannot render page: " + this.activePage_ + " the RECT cache has no entry for it");
		}

		if (page.getRowCount() <= 0)
		{
			return;
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
			
			switch(rowIndex)
			{
				case 0:
					g.setForeColor(Color.RED);
					break;
					
				case 1:
					g.setForeColor(Color.GREEN);
					break;
					
				case 2:
					g.setForeColor(Color.BLUE);
					break;
			}
			
			g.drawRect(rowRect.x, rowRect.y, rowRect.width, rowRect.height);
			
			
			/* For each gauge */
			for (int gaugeIndex = 0; gaugeIndex < row.getGaugeCount(); gaugeIndex++)
			{
	
				ProfileGauge gauge = row.getGauge(gaugeIndex);
				Rect gaugeRect = (Rect)this.rectCache_.get(gauge);
				
				if (gaugeRect == null)
				{
					throw new Exception("Cannot render gauge: " + rowIndex + " on row " + rowIndex + " the RECT cache has no entry for it");
				}
				
				switch(gaugeIndex)
				{
					case 0:
						g.setForeColor(Color.YELLOW);
						break;
						
					case 1:
						g.setForeColor(Color.CYAN);
						break;
						
					case 2:
						g.setForeColor(Color.PINK);
						break;
				}

				g.drawRect(gaugeRect.x, gaugeRect.y, gaugeRect.width, gaugeRect.height);
		
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
					heightOffset += rowHeights[rowIndex];
					
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
							widthOffset += gaugeWidths[gaugeIndex];
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
						widthOffset += gaugeWidths[gaugeIndex];
	
						/* If this is the last gauge, then look for the result of percentage rounding errors */
						if (gaugeIndex == row.getGaugeCount() - 1)
						{
							if (gaugeRect.x + gaugeRect.width != pageRect.width)
							{
								gaugeRect.width = pageRect.width - gaugeRect.x;
							}
						}
						
						
						System.out.println("row: " + rowIndex + " gauge: " + gaugeIndex + " " + gaugeRect);
					}
					
				}
			}
		
		} /* End for pageIndex loop */
		
	}
}
