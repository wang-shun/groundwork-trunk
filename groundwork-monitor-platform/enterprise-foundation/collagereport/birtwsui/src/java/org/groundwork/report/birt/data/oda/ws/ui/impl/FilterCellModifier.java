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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.groundwork.report.birt.data.oda.ws.impl.EntityFilter;

/**
 * This class implements an ICellModifier
 * An ICellModifier is called when the user modifes a cell in the 
 * tableViewer
 */

public class FilterCellModifier implements ICellModifier 
{
	private CustomDataSetWizardPage dataSetWizardPage;	
	
	/**
	 * Constructor 
	 * @param TableViewerExample an instance of a TableViewerExample 
	 */
	public FilterCellModifier(CustomDataSetWizardPage dataSetWizardPage) {
		super();
		this.dataSetWizardPage = dataSetWizardPage;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		return true;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = dataSetWizardPage.getColumnIndex(property);

		Object result = null;
		EntityFilter filter = (EntityFilter) element;

		// Note:  First column is empty to allow user to click to select row
		switch (columnIndex) {
			case 1 : // PROPERTY_COLUMN 
			{
				String stringValue = filter.getProperty().getName();
				String[] choices = dataSetWizardPage.getChoices(property);
				int i = choices.length - 1;
				while (!stringValue.equals(choices[i]) && i > 0)
					--i;
				result = new Integer(i);	
			}
				break;
			case 2 : // OPERATOR_COLUMN 
			{
				String stringValue = filter.getOperator().toString();
				String[] choices = dataSetWizardPage.getChoices(property);
				int i = choices.length - 1;
				while (!stringValue.equals(choices[i]) && i > 0)
					--i;
				result = new Integer(i);	
			}
				break;				
			case 3 : // VALUE_COLUMN 

				Object value = filter.getValue();
				
				if (value instanceof java.util.Date || value instanceof java.sql.Date)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
					result = sdf.format(value);
				}
				else
				{
					result = (value == null) ? "" : value.toString() + "";
				}
								
				break;
			case 4 : // LOGICAL_OPERATOR_COLUMN 
			{
				String stringValue = filter.getOperator().toString();
				String[] choices = dataSetWizardPage.getChoices(property);
				int i = choices.length - 1;
				while (!stringValue.equals(choices[i]) && i > 0)
					--i;
				result = new Integer(i);	
			}
				break;
			default :
				result = "";
		}
		return result;	
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {	

		// Find the index of the column 
		int columnIndex	= dataSetWizardPage.getColumnIndex(property);
			
		TableItem item = (TableItem) element;
		EntityFilter filter = (EntityFilter) item.getData();
		String valueString;

		// Note:  First column is empty to allow user to click to select row
		switch (columnIndex) {
			case 1 : // PROPERTY_COLUMN 
				valueString = dataSetWizardPage.getChoices(property)[((Integer) value).intValue()].trim();
				if (!filter.getProperty().equals(valueString)) 
				{
					filter.setProperty(dataSetWizardPage.getEntityProperty(valueString));
				}
				break;
			case 2 : // OPERATOR_COLUMN 
				valueString = dataSetWizardPage.getChoices(property)[((Integer) value).intValue()].trim();
				if (!filter.getOperator().equals(valueString)) {
					filter.setOperator(valueString);
				}
				break;
			case 3 : // VALUE_COLUMN 
				valueString = ((String) value).trim();
				if (valueString != null && valueString.length() > 0)
				{
					filter.setValue(valueString);
				}
							
				break;
			case 4 : // LOGICAL_OPERATOR_COLUMN
				valueString = dataSetWizardPage.getChoices(property)[((Integer) value).intValue()].trim();
				if (!filter.getLogicalOperator().equals(valueString)) {
					filter.setLogicalOperator(valueString);
				}
				break;
			default :
				break;
		}
		
		dataSetWizardPage.getFilterList().filterChanged(filter);
	}
}
