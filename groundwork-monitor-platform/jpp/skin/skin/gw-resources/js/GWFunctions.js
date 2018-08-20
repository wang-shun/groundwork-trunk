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

function datePicker(obj) {

	var id = obj.parentNode.childNodes[1].id;

	jQuery('#' + id.replace(/(:|\.)/g, '\\$1')).dynDateTime( {
		showsTime : true,
		ifFormat : "%m/%d/%Y %H:%M",
		daFormat : "%l;%M %p, %e %m,  %Y",
		align : "TL",
		electric : false,
		singleClick : false,
		displayArea : ".siblings('.dtcDisplayArea')",
		button : ".next()" // next sibling
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

// NOTE: init function - Initial code on page load goes here

jQuery(document).ready(function($) {

	// jQuery.noConflict();

		// From left and right scrolling arrow tabs, remove "onclick" attribute.
		// i.e. disable form submit on that tab click.
		jQuery("#frmNavigationTabset:icePnlTbSetLeft").find('.ptfd')
				.removeAttr("onclick");
		jQuery("#frmNavigationTabset:icePnlTbSetRight").find('.ptfd')
				.removeAttr("onclick");

		initTreeResize();
		// init tab bar width
		setTabWidth();
		// set context menu binding on Seurat divs
		initSeurat();
	});

function initSeurat() {
	
	//check if the element ".sbox" presents in page. if not, return
	if (jQuery('.sbox').length == 0) {
		return;
	}
	
	// Seurat View hangs over the bottom of the portlet border in Apple Safari Browser
	// Check for Apple Safari Browser
	// if yes then add padding-bottom to 15px.
	var isAppleSafari = navigator.userAgent.toLowerCase().indexOf('safari') > -1;
	if(isAppleSafari){
		jQuery("div.GWSeuratViewPnlSrs").css({'padding-bottom' : '25px'});	
	}
	
	jQuery('.sbox').contextMenu(
			'seuratMenu',
			{
				bindings : {
					'showdetails' : function(t) {
						var form = document.getElementById('contextForm');
						form['contextForm:hostName'].value = t.title
								.substring(t.title.indexOf(":") + 2);
						// simulate click on hidden form submit button
						document.getElementById('contextForm:buttonHidden')
								.click();
					}
				}
			});
}

function initTreeResize() {

	if (jQuery("#resizable").length == 0) {
		return;
	}

	// Initiate tree portlet resizing functionality
	jQuery("#resizable").resizable( {
		handles : 'e',
		minWidth : 235
	});

	// Bind this in-line function after user resizes tree portlet
	jQuery("#resizable").bind("resizestop", function(event, ui) {
		// on resizing, set the width of inner DIV as 8 less than width of
			// re-sizable container DIV
			jQuery("#resizable").width(jQuery("#divContents").width() + 8);
			// In hidden form, set the hidden input field to tree width
			document.getElementById("hiddenForm:TreeIpHiddn").value = jQuery(
					"#resizable").width();
			// simulate click on hidden form submit button
			document.getElementById('hiddenForm:buttonHidden').click();
		});

	jQuery("#resizable").bind("resize", function(event, ui) {
		if (ui != null) {
			// on resizing, set the width of inner DIV as 8 less than width of
			// re-sizable container DIV
			jQuery("#divContents").css('width', ui.size.width - 8 + 'px');
		}
	});
}

// NOTE: Keeping the code for future reference.
// This function toggles navigation tab 'close' button.
/*
 * function toggleNavTabCloseTabButton() { jQuery('.icePnlTbOff').bind(
 * 'mouseenter mouseleave', function(e) { if (e.type == 'mouseenter') {
 * jQuery(this).find('.nav_tab_close_button').css( 'visibility', 'visible'); }
 * else if (e.type == 'mouseleave') {
 * jQuery(this).find('.nav_tab_close_button').css( 'visibility', 'hidden'); }
 * }); jQuery('.icePnlTbOn').find('.nav_tab_close_button').css('visibility',
 * 'visible'); }
 */

/* Functions for scrolling left and right */

var index = 0;
var jump_step = 7; // actual value of jump step is 8, but used as 7 because of
// 0-based index
var previous_jump_step = jump_step;
var focus = "LeftSide";
var previousLeftMargin = 0;

function leftFocus() {
	if (focus == "RightSide") {
		if (index - jump_step >= 0)
			index -= jump_step;
		else
			index = 0;
		focus = "LeftSide";
	}

	if (index > 0) {
		if (document.getElementById('frmNavigationTabset:icePnlTbSet:'
				+ (index - 1) + ':icePnlTabClick') != null) {
			index--;

			document.getElementById(
					'frmNavigationTabset:icePnlTbSet:' + index
							+ ':icePnlTabClick').focus();

			var tabDiv = document.getElementById('tabsetDiv');
			tabDiv.scrollLeft = tabDiv.scrollLeft - 7;

			// store old left value
			previousLeftMargin = tabDiv.scrollLeft;
		}
	}
}

function rightFocus() {
	if (focus == "LeftSide") {
		index += jump_step;
		focus = "RightSide";
	}

	var tabDiv = document.getElementById('tabsetDiv');

	if (document.getElementById('frmNavigationTabset:icePnlTbSet:'
			+ (index + 1) + ':icePnlTabClick') != null) {
		index++;
		document.getElementById(
				'frmNavigationTabset:icePnlTbSet:' + index + ':icePnlTabClick')
				.focus();

		if (document.getElementById('frmNavigationTabset:icePnlTbSet:'
				+ (index + 1) + ':icePnlTabClick') == null) {
			tabDiv.scrollLeft = tabDiv.scrollLeft + 25;
		} else {
			tabDiv.scrollLeft = tabDiv.scrollLeft + 7;
		}
	} else {
		// last node might hav stuch, so scroll right by 100 pixels. it does not
		// scroll if its already at rightmost side.
		tabDiv.scrollLeft = tabDiv.scrollLeft + 100;
	}
	// store old left value
	previousLeftMargin = tabDiv.scrollLeft;
}

function selectTab(tabIndex, _jump_step, isNewTab) {

	// set tb bar width
	setTabWidth();

	var tabDiv = document.getElementById('tabsetDiv');

	// set focus to newly added/selected tab
	jQuery("#tabsetDiv").find('.icePnlTbOn').find('a')[1].focus();

	jump_step = _jump_step - 1; // -1 since 0 based tab index
	if (focus == "RightSide") {
		if (jump_step < previous_jump_step) {
			index = index - (previous_jump_step - jump_step);
		} else if (jump_step > previous_jump_step) {
			index = index + (jump_step - previous_jump_step);
		}
	}
	// copy to old jumpstep
	previous_jump_step = jump_step;

	if (isNewTab) {
		focus = "RightSide";
		index = tabIndex;
		tabDiv.scrollLeft = tabDiv.scrollLeft + 25;

		shiftForIE7();
		// store old left value
		previousLeftMargin = tabDiv.scrollLeft;
		return;
	} else {

		// sometimes, tab-bar automatically shifts to right for no apparent
		// reason.. restore old left value ONLY IF the new tab lies in visible
		// boundries (i.e. in between both scroll buttons)
		if ((focus == "LeftSide" && tabIndex >= index && tabIndex <= (index + jump_step))
				|| (focus == "RightSide" && tabIndex >= (index - jump_step) && tabIndex <= index)) {
			tabDiv.scrollLeft = previousLeftMargin;
			return;
		}

		if (focus == "LeftSide" && (tabIndex - index) >= jump_step) {
			focus = "RightSide";
			index = tabIndex; 
			shiftForIE7();
		} else if (focus == "RightSide" && (index - tabIndex) >= jump_step) {
			focus = "LeftSide";
			index = tabIndex;
			tabDiv.scrollLeft = tabDiv.scrollLeft - 7;
		} else {
			index = tabIndex;
		}

		if (focus == "RightSide") {
			shiftForIE7();
			tabDiv.scrollLeft = tabDiv.scrollLeft + 25;
		}
		// store old left value
		previousLeftMargin = tabDiv.scrollLeft;
	}
}

// set tab bar width in status viewer
function setTabWidth() {

	// we will set width of DIV "tabsetDiv" as per size of element styled
	// "portlet_maxwidth". Each page contains unique
	// "portlet_maxwidth" style element. its width is maximum in page and we
	// will set the same for tab bar.
//	jQuery('#tabsetDiv').width(jQuery(".portlet_maxwidth").width() - 82);

	// if there is single tab (default tab, so "close all tabs" button is not
	// rendered) so increase width by X pixels
//	if (jQuery('#tabsetDiv').find('.icePnlTbOff').length == 0) {
//		jQuery('#tabsetDiv').width(jQuery('#tabsetDiv').width() + 26);
//	}
}

// HACK for IE7 only : if IE7 , move to left 100 pixels
function shiftForIE7() {
	if (jQuery.browser.msie && jQuery.browser.version == '7.0') {
		var tabDiv = document.getElementById('tabsetDiv');
		tabDiv.scrollLeft = tabDiv.scrollLeft + 100;
	}
}

/* Functions for report server */

// This function is called from a script, located inside birtViewer.war AFTER
// the report containts is loaded into IFrame.
function resizeReportFrame() {
	var frame = jQuery("#birtViewer");
	frame.height(frame.contents().find(".style_0").height() + 170);
}

// resets height of IFrame
function resetReportFrame() {
	jQuery("#birtViewer").height(650);
}

/* end for Functions for report server */

/* Save current onload functions */
function addWindowLoadEvent(func) {

	// Save current onload functions
	var oldWindowOnLoad = window.onload;

	// If this is the first function then assign it to window.onload
	// else assign it along with the current functions
	if (typeof window.onload != 'function') {
		window.onload = func;
	} else {
		window.onload = function() {
			oldWindowOnLoad();
			func();
		};
	}
}

/* toggle visibility of legends box and text label, alongwith its left-margin */
function toggleLegends() {

	if (jQuery('#div-legends').css('display') == 'none') {
		jQuery('#div-legends').css('display', '');

		// add class "seuratLegendTextBorder" to div
		jQuery('#legendTextBox').addClass("seuratLegendTextBorder");

		// hide the "show legend" label and show "hide legends" label
		jQuery('#seuratTxtShowLegend').css('display', 'none');
		jQuery('#seuratTxtHideLegend').css('display', '');

		// make filler div visible
		jQuery('#fillerDiv').css('display', 'none');

	} else {
		jQuery('#div-legends').css('display', 'none');

		// remove class "seuratLegendTextBorder" to div
		jQuery('#legendTextBox').removeClass("seuratLegendTextBorder");

		// hide the "hide legend" label and show "show legends" label
		jQuery('#seuratTxtShowLegend').css('display', '');
		jQuery('#seuratTxtHideLegend').css('display', 'none');

		// make filler div visible
		jQuery('#fillerDiv').css('display', '');

	}
}

function playAlarm(fileName) {
	jQuery("#jquery_jplayer").jPlayer("setFile", fileName).jPlayer("play");
}

function initializeAlarm(swfContextPath, fileName) {
	jQuery(document).ready(function() {
		jQuery("#jquery_jplayer").jPlayer({
			ready: function () {
				this.element.jPlayer("setFile", fileName).jPlayer("play");								
			},				
			swfPath: swfContextPath
		});
	})
}
//this deletes the cookie when called.Especially nagvis and cacti cookies
function delete_cookies() {
    document.cookie= "nagvis_session=;path=/nagvis;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "Cacti=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "restriction_type=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "auth_hg_list=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "auth_sg_list=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "JOSSO_SESSIONID=;path=/portal;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "JSESSIONID=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "CGISESSID=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
    document.cookie= "PHPSESSID=;path=/;expires=Fri, 3 Aug 1970 20:47:11 UTC";
 return true;
}

function slowRefreshToBaseURL() {
    var timeout = window.setTimeout(function() {
        window.location.href = window.location.protocol + "//" + window.location.host + window.location.pathname;
    }, 500);
}
