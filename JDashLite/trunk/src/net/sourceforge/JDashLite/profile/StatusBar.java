/*********************************************************
 * 
 * @author spowell
 * StatusBar.java
 * Aug 6, 2008
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

import net.sourceforge.JDashLite.profile.color.ColorModel;
import waba.fx.Color;
import waba.fx.Font;
import waba.fx.Graphics;
import waba.fx.Rect;

/*********************************************************
 * 
 *
 *********************************************************/
public class StatusBar implements RenderableProfileComponent
{
	
	public static final int RXTX_READY = 0;
	public static final int RXTX_SEND = 1;
	public static final int RXTX_RECEIVE = 2;
	
	private int pageCount_ = 0;
	private int activePage_ = 0;
	
	private Rect currentRect_ = null;
	private int pageAreaTotalWidth_ = 0;
	
	private String[] pageLabels_ = null;
	
	/* The display state of th RXTX indicator */
	private int rxtxMode_ = RXTX_READY;
	
	/********************************************************
	 * 
	 *******************************************************/
	public StatusBar()
	{
		
	}
	
	/********************************************************
	 * @return the pageCount
	 ********************************************************/
	public int getPageCount()
	{
		return this.pageCount_;
	}
	
	
	/********************************************************
	 * @param pageCount the pageCount to set
	 ********************************************************/
	public void setPageCount(int pageCount)
	{
		this.pageCount_ = pageCount;
		this.pageLabels_ = new String[pageCount];
		for (int index = 0; index < this.pageCount_; index++)
		{
			pageLabels_[index] = new String((index+1) + "");
		}
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
		this.activePage_ = activePage;
	}
	
	/********************************************************
	 * @param mode
	 ********************************************************/
	public void setRXTXMode(int mode)
	{
		this.rxtxMode_ = mode;
	}
	
	
	/********************************************************
	 * If the X and Y values are in a page button, return the 
	 * index of that page
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	public int getPageIndex(int x, int y)
	{
		if ((x >= this.currentRect_.x) && 
			(x <= this.currentRect_.x + this.pageAreaTotalWidth_) &&
			(y >= this.currentRect_.y) &&
			(y <= this.currentRect_.y + this.currentRect_.height))
		{
			int pageWidth = this.pageAreaTotalWidth_ / this.pageCount_;
			
			for (int index = 1; index <= this.pageCount_; index++)
			{
				if (x <= this.currentRect_.x + (index * pageWidth))
				{
					return index - 1;
				}
			}
			
		}
		
		return -1;
	}
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r, ColorModel cm) throws Exception
	{
		
		this.currentRect_ = r;
		
		/* Find the best fit font */
		Font f = ProfileRenderer.findFontBestFitHeight(r.height - 2, false);
		g.setFont(f);
		
		/* Draw the inactive background color */
		g.setBackColor(cm.get(ColorModel.STATUS_PAGE_INACTIVE_BG));
		g.fillRect(r.x, r.y, r.width, r.height);

		int rxtxAreaWidth = r.height * 2;//(int)(r.width * RXTX_INDICATOR_WIDTH_PERCENT);
		this.pageAreaTotalWidth_ = r.width - rxtxAreaWidth;

		/* Draw the pages, if any */
		if (this.pageCount_ > 0)
		{
			
			/* Calculate the page widths, and the RXTX indicator width */
			int pageWidth = this.pageAreaTotalWidth_ / this.pageCount_;
			
			/* Draw the page lines */
			for (int index = 0; index < this.pageCount_; index++)
			{
				int xOffset = r.x + (index * pageWidth);
				
				/* Set the page rect colors and Draw the active page rect */
				if (index == this.activePage_)
				{
					g.setForeColor(cm.get(ColorModel.STATUS_PAGE_ACTIVE_BG));
					g.setBackColor(cm.get(ColorModel.STATUS_PAGE_ACTIVE_BG));
					g.fillRect(xOffset, r.y, pageWidth, r.height);
					g.setForeColor(cm.get(ColorModel.STATUS_PAGE_ACTIVE_FG));
	
				}
				else
				{
					g.setForeColor(cm.get(ColorModel.STATUS_PAGE_INACTIVE_FG));
					g.setBackColor(cm.get(ColorModel.STATUS_PAGE_INACTIVE_BG));
				}
				
				/* Draw the page # centerd in the rect */
				g.drawText(this.pageLabels_[index], xOffset + (pageWidth / 2) - (f.fm.getTextWidth(this.pageLabels_[index]) / 4), r.y + ((r.height - f.fm.height) / 2));
	
			}
	
			/* Draw the RXTX lights */
			g.setForeColor(Color.BLACK);
			
			
			int indicatorRadius = r.height / 2 - 2;
			int txX = r.x + r.width - rxtxAreaWidth + (rxtxAreaWidth / 3) - 1;
			int rxX = r.x + r.width - (rxtxAreaWidth / 3) + 1;
			
			g.setBackColor(this.rxtxMode_==RXTX_SEND?cm.get(ColorModel.STATUS_TX_COLOR):cm.get(ColorModel.STATUS_TX_IDLE_COLOR));
			g.fillCircle(txX, r.y + (r.height / 2), indicatorRadius);
			g.drawCircle(txX, r.y + (r.height / 2), indicatorRadius);
			
			g.setBackColor(this.rxtxMode_==RXTX_RECEIVE?cm.get(ColorModel.STATUS_RX_COLOR):cm.get(ColorModel.STATUS_RX_IDLE_COLOR));
			g.fillCircle(rxX, r.y + (r.height / 2), indicatorRadius);
			g.drawCircle(rxX, r.y + (r.height / 2), indicatorRadius);
		
			/* Draw the page separator lines */
			g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
			for (int index = 1; index < this.pageCount_; index++)
			{
				g.drawLine(pageWidth * index, r.y, pageWidth * index, r.y + r.height);			
			}
			
			/* Draw the RXTX lights separator line */
			g.drawLine(r.width - rxtxAreaWidth, r.y, r.width - rxtxAreaWidth, r.y + r.height);
			
		}
		
		/* Draw the RX/TX section */
		
		/* Draw the border */
		g.setForeColor(cm.get(ColorModel.DEFAULT_BORDER));
		g.drawRect(r.x, r.y, r.width, r.height);

	}

}
