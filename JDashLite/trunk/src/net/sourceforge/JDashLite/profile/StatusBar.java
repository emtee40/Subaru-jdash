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

import net.sourceforge.JDashLite.error.ErrorLog;
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
	
	private static final Color ACTIVE_PAGE_BG_COLOR = ProfileRenderer.COLOR_DK_GRAY;
	private static final Color INACTIVE_PAGE_BG_COLOR = ProfileRenderer.COLOR_LT_GRAY;;
	private static final Color ACTIVE_PAGE_FG_COLOR = Color.WHITE;
	private static final Color INACTIVE_PAGE_FG_COLOR = Color.BLACK;
	
	private static final double RXTX_INDICATOR_WIDTH_PERCENT = 0.1;
	
	private int pageCount_ = 0;
	private int activePage_ = 0;
	
	private String[] pageLabels_ = null;

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
	
	/*********************************************************
	 * (non-Javadoc)
	 * @see net.sourceforge.JDashLite.profile.RenderableProfileComponent#render(waba.fx.Graphics, waba.fx.Rect)
	 ********************************************************/
	public void render(Graphics g, Rect r) throws Exception
	{
		
		/* Find the best fit font */
		Font f = ProfileRenderer.findFontBestFitHeight(r.height - 2);
		g.setFont(f);
		
		/* Draw the inactive background color */
		g.setBackColor(INACTIVE_PAGE_BG_COLOR);
		g.fillRect(r.x, r.y, r.width, r.height);

		/* Calculate the page widths, and the RXTX indicator width */
		int pageWidth = r.width / this.pageCount_;
		
		/* Draw the page lines */
		for (int index = 0; index < this.pageCount_; index++)
		{
			int xOffset = r.x + (index * pageWidth);
			
			/* Set the page rect colors and Draw the active page rect */
			if (index == this.activePage_)
			{
				g.setForeColor(ACTIVE_PAGE_BG_COLOR);
				g.setBackColor(ACTIVE_PAGE_BG_COLOR);
				g.fillRect(xOffset, r.y, pageWidth, r.height);
				g.setForeColor(ACTIVE_PAGE_FG_COLOR);

			}
			else
			{
				g.setForeColor(INACTIVE_PAGE_FG_COLOR);
				g.setBackColor(INACTIVE_PAGE_BG_COLOR);
			}
			
			/* Draw the page # centerd in the rect */
			g.drawText(this.pageLabels_[index], xOffset + (pageWidth / 2) - (f.fm.getTextWidth(this.pageLabels_[index]) / 4), r.y + ((r.height - f.fm.height) / 2));

		}

	
		/* Draw the separator lines */
		g.setForeColor(ProfileRenderer.COLOR_BORDER);
		for (int index = 1; index < this.pageCount_; index++)
		{
			g.drawLine(pageWidth * index, r.y, pageWidth * index, r.y + r.height);			
		}
			
		
		/* Draw the border */
		g.setForeColor(ProfileRenderer.COLOR_BORDER);
		g.drawRect(r.x, r.y, r.width, r.height);

	}

}
