/*********************************************************
 * 
 * @author spowell
 * CircularIndex.java
 * Aug 24, 2008
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

package net.sourceforge.JDashLite.util;

/*********************************************************
 * This class provides a simple means to index a circular
 * buffer or array object.
 *
 *********************************************************/
public class CircularIndex
{

	/* The size of the index */
	private int size_ = 0;
	
	/* Always points to the current start/head of the index */
	private int head_ = 0;
	
	/* Is the current pointer index */
	private int index_ = 0;
	
	
	/********************************************************
	 * 
	 *******************************************************/
	public CircularIndex(int size)
	{
		this.size_ = size;
	}
	
	/******************************************************
	 * @return
	 ********************************************************/
	public int getSize()
	{
		return this.size_;
	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int incrementHead()
	{
		this.head_++;
		if (this.head_ >= this.size_)
		{
			this.head_ = 0;
		}
		return getHead();
	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int decrementHead()
	{
		this.head_--;
		if (this.head_ <= -1)
		{
			this.head_ = this.size_ - 1;
		}
		return getHead();
	}
	
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int getHead()
	{
		return this.head_;
	}
	
	/*******************************************************
	 * Sets the index pointer to the head/starting point
	 ********************************************************/
	public void resetIndex()
	{
		this.index_ = head_;
	}
	
	/*******************************************************
	 * Get the current index value.
	 * @return
	 ********************************************************/
	public int getIndex()
	{
		return this.index_;
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int incrementIndex()
	{
		this.index_++;

		if (this.index_ >= this.size_)
		{
			this.index_ = 0;
		}
		
		return getIndex();
	}
	
	/*******************************************************
	 * @return
	 ********************************************************/
	public int decrementIndex()
	{
		this.index_--;
		
		if (this.index_ <= -1)
		{
			this.index_ = this.size_ - 1;
		}
		
		return getIndex();
	}
	
	
}
