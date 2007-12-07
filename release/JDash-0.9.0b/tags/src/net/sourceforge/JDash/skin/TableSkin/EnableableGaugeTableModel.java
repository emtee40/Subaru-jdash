/*******************************************************
 * 
 *  @author spowell
 *  TableGaugeModel
 *  Aug 28, 2007
 *  $Id:$
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

import java.util.List;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;

/*******************************************************
 * A simple table model that presents a list of parameters
 * that cannot be disabled, because they are calculated from
 * ECU Parameters.
 ******************************************************/
public class EnableableGaugeTableModel extends GaugeTableModel
{
	
	/*******************************************************
	 * Creates a new table gauge model
	 ******************************************************/
	public EnableableGaugeTableModel(List<Parameter> params)
	{
		super(params);
		
		/* Start each ECU Parameter in a disabled mode */
		for (Parameter p : params)
		{
			if (p instanceof ECUParameter)
			{
				((ECUParameter)p).setEnabled(false);
			}
		}
	}
	

	/*******************************************************
	 *
	 ******************************************************/
	public Class<?> getColumnClass(int col)
	{
		if (col == 0)
		{
			return Boolean.class;
		}
		else
		{
			return String.class;
		}
	}

	/*******************************************************
	 *
	 ******************************************************/
	public int getColumnCount()
	{
		return 3;
	}

	/*******************************************************
	 *
	 ******************************************************/
	public String getColumnName(int col)
	{
		
		if (col == 0)
		{
			return "Enable";
		}
		
		return super.getColumnName(col - 1);
		
	}


	/*******************************************************
	 *
	 ******************************************************/
	public Object getValueAt(int row, int col)
	{
		Parameter p = this.params_.get(row);
		
		if (col == 0)
		{
			return ((ECUParameter)p).isEnabled();
		}
		
		return super.getValueAt(row, col - 1);
		
	}

	/*******************************************************
	 * Only column 0 is editable.
	 ******************************************************/
	public boolean isCellEditable(int row, int col)
	{
		if (col == 0)
		{
			return true;
		}
		
		return super.isCellEditable(row, col - 1);
		
	}


	/*******************************************************
	 *
	 ******************************************************/
	public void setValueAt(Object v, int row, int col)
	{
		if (col != 0)
		{
			throw new RuntimeException("Cannot edit any columns except the Enable column");
		}

		Parameter p = this.params_.get(row);
		
		if (p instanceof ECUParameter)
		{
			((ECUParameter)p).setEnabled(new Boolean(v.toString()));
		}
		
	}

}
