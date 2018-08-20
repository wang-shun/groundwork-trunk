/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.bean;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DualList implements Serializable {

	private Map selectItemMap;
	private List leftList;
	private List rightList;
	private Object[] selectedOnLeft = new Object[0];
	private Object[] selectedOnRight = new Object[0];

	public DualList() {
		selectItemMap = new HashMap();
		leftList = new ArrayList();
		rightList = new ArrayList();
	}

	public void setAvailableItems(List list) {
		leftList.clear();
		leftList.addAll(list);

		SelectItem si;
		for (Object aList : list) {
			si = (SelectItem) aList;
			selectItemMap.put(si.getValue().toString(), si);
		}
	}

	public void addAvailableItem(SelectItem si) {
		leftList.add(si);
		selectItemMap.put(si.getValue().toString(), si);
	}

	public void adjustDualList(SelectItem si) {

		for (Object itemObj : leftList) {
			SelectItem item = (SelectItem)itemObj;
			if (item.getValue().toString()
					.equalsIgnoreCase(si.getValue().toString())) {
				si = item;
				break;
			}
		}
		leftList.remove(si);
		// selectItemMap.remove(si.getValue().toString());
		// leftList = new ArrayList(selectItemMap.values());
		rightList.add(si);
	}

	public void addAvailableItemToRightList(SelectItem si) {
		rightList.add(si);
		selectItemMap.put(si.getValue().toString(), si);
	}

	public void addAll(ActionEvent evt) {
		rightList.addAll(leftList);
		leftList.clear();
	}

	public void removeAll(ActionEvent evt) {
		leftList.addAll(rightList);
		rightList.clear();
	}

	public void add(ActionEvent evt) {
		if (selectedOnLeft != null) {
			SelectItem si;
			for (Object aSelectedOnLeft : selectedOnLeft) {
				si = (SelectItem) selectItemMap.get(aSelectedOnLeft.toString());
				rightList.add(si);
				leftList.remove(si);
			}
		}
	}

	public void remove(ActionEvent evt) {
		if (selectedOnRight != null) {
			SelectItem si;
			for (Object aSelectedOnRight : selectedOnRight) {
				si = (SelectItem) selectItemMap
						.get(aSelectedOnRight.toString());
				leftList.add(si);
				rightList.remove(si);
			}
		}
	}

	public Object[] getSelectedOnLeft() {
		return selectedOnLeft;
	}

	public void setSelectedOnLeft(Object[] selectedOnLeft) {
		this.selectedOnLeft = selectedOnLeft;
	}

	public Object[] getSelectedOnRight() {
		return selectedOnRight;
	}

	public void setSelectedOnRight(Object[] selectedOnRight) {
		this.selectedOnRight = selectedOnRight;
	}

	public List getLeftList() {
		return leftList;
	}

	public List getRightList() {
		return rightList;
	}
}