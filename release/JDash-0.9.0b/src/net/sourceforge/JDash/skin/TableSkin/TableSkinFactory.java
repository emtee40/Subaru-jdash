/*******************************************************
 * 
 *  @author spowell
 *  AllParametersSkinFactory.java
 *  Aug 29, 2006
 *  $Id: AllParametersSkinFactory.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
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
package net.sourceforge.JDash.skin.TableSkin;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.JDash.ecu.param.ParameterRegistry;
import net.sourceforge.JDash.skin.Skin;
import net.sourceforge.JDash.skin.SkinFactory;

/*******************************************************
 *
 ******************************************************/
public class TableSkinFactory extends SkinFactory
{


		private List<Skin> allSkins_ = new ArrayList<Skin>();
		
		/*******************************************************
		 * 
		 ******************************************************/
		public TableSkinFactory() throws Exception
		{
			super();
			this.allSkins_.add(new TableSkin(this));
		}
		
		/*******************************************************
		 * Override
		 * @see net.sourceforge.JDash.skin.SkinFactory#getAllSkins()
		 *******************************************************/
		@Override
		public List<Skin> getAllSkins() throws Exception
		{
			return this.allSkins_;
		}
		
		/*******************************************************
		 * Override
		 * @see net.sourceforge.JDash.skin.SkinFactory#getDefaultSkin()
		 *******************************************************/
		@Override
		public Skin getDefaultSkin() throws Exception
		{
			return this.allSkins_.get(0);
		}
		
	}
	
