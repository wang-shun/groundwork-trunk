/*
 * OpenSocial jQuery autoHeight 1.0.0
 * http://code.google.com/p/opensocial-jquery/
 *
 * Copyright(C) 2009 Nakajiman Software Inc.
 * http://nakajiman.lrlab.to/
 *
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 */
function doIframe() {
	o = document.getElementsByTagName('iframe');
	for (i = 0; i < o.length; i++) {
		if (/\bautoHeight\b/.test(o[i].className)) {
			setHeight(o[i]);
			addEvent(o[i], 'load', doIframe);
		}
	}
}

function setHeight(e) {
	var IE7 = (navigator.appVersion.indexOf("MSIE 7.")==-1) ? false : true;
    if (IE7) {
            e.height = 2000;
    }
    else
    {
            if (e.contentDocument) {
                    // All monarch cgis are of height 150
                    if (e.contentDocument.body.offsetHeight <= 150) {
                            e.height = 2000;
                    } else {
                            e.height = e.contentDocument.body.offsetHeight + 300;
                    } // end if
            } else {
                    e.height = e.contentWindow.document.body.scrollHeight;
            } // end if
    } // end if
}

function addEvent(obj, evType, fn) {
	if (obj.addEventListener) {
		obj.addEventListener(evType, fn, false);
		return true;
	} else if (obj.attachEvent) {
		var r = obj.attachEvent("on" + evType, fn);
		return r;
	} else {
		return false;
	}
}

if (document.getElementById && document.createTextNode) {
	addEvent(window, 'load', doIframe);
}