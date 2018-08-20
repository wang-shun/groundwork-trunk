/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.report.birt.data.oda.ws.ui.impl;

import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.groundwork.foundation.ws.model.impl.PropertyDataType;
import org.groundwork.report.birt.data.oda.ws.impl.EntityFilter;


/**
 * Label provider for the TableViewerExample
 * 
 * @see org.eclipse.jface.viewers.LabelProvider 
 */
public class FilterLabelProvider extends LabelProvider implements ITableLabelProvider 
{
	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) 
	{		
		String result = "";
		
		EntityFilter filter = (EntityFilter) element;
		
		// Note:  First column is empty to allow user to click to select row
		switch (columnIndex) {
			case 1:  // PROPERTY_COLUMN
				result = filter.getProperty().getName();
				break;
			case 2 :
				result = filter.getOperator().toString();
				break;
			case 3 :
			{
				Object value = filter.getValue();
				
				// Just return the String value - All parameters allow for "?" to be 
				// used to indicate a parameter value.
				if (value != null && value instanceof java.lang.String)
				{
					result = (String)value;
				}
				else if (filter.getProperty().getDataType() == PropertyDataType.DATE)
				{					
					SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
					result = sdf.format(value);
				}
				else
				{
					result = value.toString() + "";
				}
			}
				break;
			case 4 :
				result = filter.getLogicalOperator() + "";
				break;
			default :
				break; 	
		}
		return result;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}
}
