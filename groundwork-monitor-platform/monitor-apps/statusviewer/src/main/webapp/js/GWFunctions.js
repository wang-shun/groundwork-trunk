//		Coopyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All
//		rights reserved. This program is free software; you can redistribute
//		it and/or modify it under the terms of the GNU General Public License
//		version 2 as published by the Free Software Foundation. This program
//		is distributed in the hope that it will be useful, but WITHOUT ANY
//		WARRANTY; without even the implied warranty of MERCHANTABILITY or
//		FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
//		for more details. You should have received a copy of the GNU General
//		Public License along with this program; if not, write to the Free
//		Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
//		02110-1301, USA.

function seuratStyle(obj) {
	var browser = navigator.appName.toLowerCase();
	if (browser == "microsoft internet explorer") {
		obj.parentNode.childNodes[1].childNodes[0].style.position = "fixed";
		obj.parentNode.childNodes[1].childNodes[0].style.zIndex = 9999;
	} else {
		obj.parentNode.childNodes[2].childNodes[0].style.position = "fixed";
		obj.parentNode.childNodes[2].childNodes[0].style.zIndex = 9999;
	}

}

function datePicker(obj) {

	var id = obj.parentNode.childNodes[1].id;

	jQuery('#' + id.replace(/(:|\.)/g, '\\$1')).dynDateTime( {
		showsTime :true,
		ifFormat :"%m/%d/%Y %H:%M",
		daFormat :"%l;%M %p, %e %m,  %Y",
		align :"TL",
		electric :false,
		singleClick :false,
		displayArea :".siblings('.dtcDisplayArea')",
		button :".next()" // next sibling
	});

}

function hideShowCustomDates() {

	if (document.getElementById('perfMeasurementPortlet_menuTimeSelector').value == "-1") {
		document.getElementById('customDatesDiv').style.visibility = 'visible';
	} else {
		document.getElementById('customDatesDiv').style.visibility = 'hidden';
	}
}

function hideTooltip(obj) {
	// alert(obj.id);
	obj.parentNode.childNodes[2].style.visibility = 'hidden';
}

// This function is helper function, and necessory workaround for bug:
// Firefox not recognizing "click()" javascript function.

var ie = (navigator.appName.indexOf("Internet Explorer") != -1) ? true : false;
if (!ie) {
	HTMLElement.prototype.click = function() {
		var evt = this.ownerDocument.createEvent('MouseEvents');
		evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView,
				1, 0, 0, 0, 0, false, false, false, false, 0, null);
		this.dispatchEvent(evt);
	}
}

// This function is used to expand clicked node in tree view,
// if the node is not already expanded.
// This function is used to expand clicked node in tree view,
// if the node is not already expanded.
function expandNode(obj) {

	// traverse all siblings of given node and look for hyper link (tag name
	// "A", thats anchor)
	var arr = $(obj.parentNode.id).select('a');

	// got first hyper link. expand it, if not already expanded. we will
	// simulate click only if its in closed state, that is
	// it contains image, that have source as ".....open.gif"
	if (arr[0].firstDescendant().src != undefined
			&& arr[0].firstDescendant().src.indexOf("open.gif") != -1) {
		// simulate click
		$(arr[0].id).click();
		return;
	}

}
