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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.StringParameter;

/*******************************************************
 * A simple table model that presents a list of parameters.
 ******************************************************/
public class GaugeTableModel implements TableModel
{

	/** The underlying data */
	protected List<Parameter> params_ = null;
	
	/*******************************************************
	 * Creates a new table gauge model
	 ******************************************************/
	public GaugeTableModel(List<Parameter> params)
	{
		this.params_ = params;
	}
	
	
	/*******************************************************
	 *
	 ******************************************************/
	public void addTableModelListener(TableModelListener arg0)
	{
		// TODO Auto-generated method stub

	}

	/*******************************************************
	 *
	 ******************************************************/
	public Class<?> getColumnClass(int col)
	{
		return String.class;
	}

	/*******************************************************
	 *
	 ******************************************************/
	public int getColumnCount()
	{
		return 2;
	}

	/*******************************************************
	 *
	 ******************************************************/
	public String getColumnName(int col)
	{
		switch(col)
		{
			case 0:
				return "ECU";
			
			case 1:
				return "Value";
				
			default:
				return "ERR";
		}
	}

	/*******************************************************
	 *
	 ******************************************************/
	public int getRowCount()
	{
		return this.params_.size();
	}

	/*******************************************************
	 *
	 ******************************************************/
	public Object getValueAt(int row, int col)
	{
		Parameter p = this.params_.get(row);
		
		switch(col)
		{
			case 0:
				return p.getName();
				
			case 1:
				if (p instanceof StringParameter)
				{
					return p.toString();
				}
				else
				{
					return String.format("%03.02f", p.getResult());
				}
				
			default:
				return null;
		}
	}

	/*******************************************************
	 * Only column 0 is editable.
	 ******************************************************/
	public boolean isCellEditable(int row, int col)
	{
		return false;
	}

	/*******************************************************
	 *
	 ******************************************************/
	public void removeTableModelListener(TableModelListener arg0)
	{
		// TODO Auto-generated method stub

	}

	/*******************************************************
	 *
	 ******************************************************/
	public void setValueAt(Object v, int row, int col)
	{
		
	}

}
