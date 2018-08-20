<%@ page language="java"  %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<!--
/**
 * Modified By: Glenn Herteg
 * Date: Apr 24 2013
 * This JSP displays a single iframe in the portlet.  The height of the iframe
 * is calculated so the content fits exactly within the available screen area,
 * taking into account certain surrounding spacing outside of the iframe.
 * The width of the iframe and enclosing elements has been restructured to work
 * in an HTML5 context.
 */
-->
<portlet:defineObjects/>

<table id="enclosure" style="width:100%" cellpadding=0 cellspacing=0 border=0>
<% if (request.getAttribute("openinwindow") == "true") {%>
    <tr>
	<td>
	    <a target="_new" href="<%= request.getAttribute("iframeurl") %>"><center>Open in new window</center></a>
	</td>
    </tr>
<%}%>
    <tr>   
	<td>
        <%      
        Object allow_remote_url = request.getAttribute("allow_remote_url");
        if (allow_remote_url == null || !allow_remote_url.equals("true")) {
        %>
	    <script language=JavaScript>
	    // FIX LATER:
	    // * I don't see any way to get an auto-triggered resize on header shrinkage due
	    //   to font-size reduction.  We still defer to the human then (click on gray).
	    var enclosure = document.getElementById('enclosure');
	    // In testing, we have seen variations in how much space the browser thinks is
	    // needed below our useful content, before it will completely kill the outside
	    // scrollbar:  IE8 => 6..9; FF3 => 7; FF2 => 8; IE7 => 24.  Why these differences,
	    // we haven't tracked down.  Presumably it's due to some variation in how the
	    // referenced CSS is interpreted and what the current browser font size is.
	    var surroundheight = 9;
	    function top_of(e) {
		var y = 0;
		while (e) {
		    y += e.offsetTop;
		    e = e.offsetParent;
		}
		return y;
	    }
	    function viewportHeight() {
		var viewHeight;
		if (window.innerHeight) {
		    viewHeight = window.innerHeight;
		}
		else if (document.documentElement && document.documentElement.clientWidth) {
		    // Perhaps not valid until the body is loaded.
		    viewHeight = document.documentElement.clientHeight;
		}
		else if (document.body) {
		    // Perhaps not valid until the body is loaded.
		    viewHeight = document.body.clientHeight;
		}
		return viewHeight;
	    }
	    function resize_iframe() {
		var myframe = document.getElementById('myframe');
		myframe.style.height = (viewportHeight() - top_of(enclosure) - surroundheight) + "px";
	    }
	    if (window.addEventListener) {
		// failsafe (human-triggered)
		window.addEventListener("click", resize_iframe, true);
		// Firefox
		window.addEventListener("overflow", resize_iframe, false);
		window.addEventListener("underflow", resize_iframe, false);
		// Safari
		window.addEventListener("overflowchanged", resize_iframe, false);
		// Firefox and Safari
		window.addEventListener("resize", resize_iframe, false);
		window.addEventListener("load", resize_iframe, false);
	    }
	    else if (window.attachEvent) {
		window.attachEvent("onclick", resize_iframe);
		window.attachEvent("onresize", resize_iframe);
		window.attachEvent("onload", resize_iframe);
	    }
	    else {
		window.onclick = resize_iframe;
		window.onresize = resize_iframe;
		window.onload = resize_iframe;
	    }
	    document.write('<iframe id="myframe" src="<%= request.getAttribute("iframeurl") %>" style="width:100%;"' +
		' height="' + (viewportHeight() - top_of(enclosure) - surroundheight) + '"' +
		' allowtransparency="false" frameborder="0" scrolling="auto"> Your browser does not support iframes </iframe>');
	    var exdate=new Date();
	    exdate.setDate(exdate.getDate()+1);
	    </script>
	<%} else {%>
	    <iframe src="<%= request.getAttribute("iframeurl") %>" style="width:100%;"
		height="<%= request.getAttribute("iframeheight") %>" allowtransparency="false" frameborder="0">
		    Your browser does not support iframes
	    </iframe>
	<%}%>      
	</td>	
    </tr>
</table>
