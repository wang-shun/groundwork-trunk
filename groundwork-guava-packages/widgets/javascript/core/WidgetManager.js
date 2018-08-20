/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

dojo.provide("gwwidgets.core.WidgetManager");

gwwidgets.core.HTMLWidgetManager = function() { };
dojo.lang.extend(gwwidgets.core.HTMLWidgetManager, {
	
	elementToDrag : null,
	elementResize : null,
	elementContent : null,
	identifier : null,
	deltaX : null,
	deltaY : null,
	top : null,
	height: null,
	width : null,
	left : null,
	
	Move : function (identifier, event) {
		this.elementToDrag = document.getElementById(identifier);
		this.identifier = identifier;
		
		//alert("PARENT NODE WIDTH: "  + this.elementToDrag.parentNode.scrollHeight);
		//alert("PARENT NODE HEIGHT: "  + this.elementToDrag.parentNode.scrollHeight);
		
		parentPosition = dojo.html.getAbsolutePosition(this.elementToDrag.parentNode, true);
		parentTop = parentPosition[1];
		parentLeft = parentPosition[0];
		
		elementToHide = document.getElementById(identifier + "_content");
		
		//alert(dojo.style.getBorderBoxHeight(this.elementToDrag) + "," + dojo.style.getBorderBoxWidth(this.elementToDrag));
		
		var x = parseInt(this.elementToDrag.style.left);
		var y = parseInt(this.elementToDrag.style.top);
		
		this.deltaX = event.clientX - x;
		this.deltaY = event.clientY - y;
		  
		
		//document.addEventListener("mousemove", moveHandler, true);
		//document.addEventListener("mouseup", upHandler, true);
		
		//event.preventDefault();
		
		dojo.event.connect(document, "onmousemove", this, "moveHandler");
		dojo.event.connect(document, "onmouseup", this, "moveUpHandler");
		
		//elementToHide = document.getElementById(this.identifier + "__content");
		//elementToHide.style.visibility = "hidden";
		
		
		//event.stopPropagation();
	},
	
	moveHandler: function(event) {
		elementToHide.style.visibility = "hidden";
		
		
		//if((event.clientX - this.deltaX) >= 0 && ((event.clientX - this.deltaX) + parseInt(this.elementToDrag.style.width)) <= (dojo.style.getBorderBoxWidth(this.elementToDrag.parentNode)) ) {
		if((event.clientX - this.deltaX) >= 0) {
			this.elementToDrag.style.left = (event.clientX - this.deltaX) + "px";
		}
		else if((event.clientX - this.deltaX) < 0) {
			this.elementToDrag.style.left = "0px";
		}
		else {
			this.elementToDrag.style.left = dojo.html.getBorderBoxWidth(this.elementToDrag.parentNode) - parseInt(this.elementToDrag.style.width) + "px";
		}

		//if((event.clientY - this.deltaY) >= 0 && (event.clientY - this.deltaY) + (parseInt(this.elementToDrag.style.height)) <= dojo.style.getBorderBoxHeight(this.elementToDrag.parentNode)) {
		if((event.clientY - this.deltaY) >= 0) {
			this.elementToDrag.style.top = (event.clientY - this.deltaY) + "px";
		}
		else if((event.clientY - this.deltaY) < 0) {
			this.elementToDrag.style.top = "0px";
		}
		else {
			this.elementToDrag.style.top = dojo.html.getBorderBoxHeight(this.elementToDrag.parentNode) - parseInt(this.elementToDrag.style.height) + "px";
		}
		
		
		//event.stopPropagation();
	},
		
	moveUpHandler : function(event) {
		elementToHide.style.visibility="visible";

		dojo.event.disconnect(document, "onmousemove", this, "moveHandler");
		dojo.event.disconnect(document, "onmouseup", this, "moveUpHandler");
		
		//event.stopPropagation();
		//elementToHide.style.visibility = "visible";
		
		// Send message
		addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: this.identifier}, {name: 'action', type: 'string', value:  'move'}, {name: 'top', type: 'string', value:  parseInt(this.elementToDrag.style.top)}, {name: 'left', type: 'string', value:  parseInt(this.elementToDrag.style.left)}, {name: 'width', type: 'string', value:  parseInt(this.elementToDrag.style.width)}, {name: 'height', type: 'string', value:  parseInt(this.elementToDrag.style.height)}]);
		sendMessageQueue();
	},
	
	ResizeBottom : function (identifier, event) {
		this.identifier = identifier;
		this.elementResize = document.getElementById(this.identifier);
		this.elementContent = document.getElementById(this.identifier + "_content");
		
		
		this.height = parseInt(this.elementResize.style.height);
		
		this.top = parseInt(this.elementResize.style.top);
	   
		dojo.event.connect(document, "onmousemove", this, "resizeBottomMoveHandler");
		dojo.event.connect(document, "onmouseup", this, "resizeBottomUpHandler");
		

		
		//event.sthis.topPropagation();
		//event.preventDefault();
	},
	resizeBottomMoveHandler: function (event) {
		
		if((event.clientY - this.top) > 50) {
			
			// Determine different
			this.elementResize.style.height = (event.clientY - this.top) + "px";
			this.elementContent.style.height = parseInt(this.elementResize.style.height) - 21 + "px";
		}
		//event.sthis.topPropagation();
	},
	resizeBottomUpHandler: function (event) {
		dojo.event.disconnect(document, "onmousemove", this, "resizeBottomMoveHandler");
		dojo.event.disconnect(document, "onmouseup", this, "resizeBottomUpHandler");
		//event.sthis.topPropagation();

		// Send message
		addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: this.identifier}, {name: 'action', type: 'string', value:  'move'}, {name: 'top', type: 'string', value:  parseInt(this.elementResize.style.top)}, {name: 'left', type: 'string', value:  parseInt(this.elementResize.style.left)}, {name: 'width', type: 'string', value:  parseInt(this.elementResize.style.width)}, {name: 'height', type: 'string', value:  parseInt(this.elementResize.style.height)}]);
		sendMessageQueue();
	},
	
	
	ResizeLeft : function (identifier, event) {
		this.identifier = identifier;
		this.elementResize = document.getElementById(this.identifier);
		this.elementContent = document.getElementById(this.identifier + "_content");
		
		this.width = parseInt(this.elementResize.style.width);
		
		this.left = parseInt(this.elementResize.style.left);
		
		// Elements to resize
		
		dojo.event.connect(document, "onmousemove", this, "resizeLeftMoveHandler");
		dojo.event.connect(document, "onmouseup", this, "resizeLeftUpHandler");

					
		//event.sthis.topPropagation();
		//event.preventDefault();
	},
	resizeLeftMoveHandler: function(event) {
			
			if(event.clientX < (parseInt(this.elementResize.style.width) + parseInt(this.elementResize.style.left) - 50)) {
				// Determine different					

				this.elementResize.style.width = (parseInt(this.elementResize.style.width) + parseInt(this.elementResize.style.left) - event.clientX) + "px";
				
				this.elementResize.style.left = (event.clientX) + "px";
				
				this.elementContent.style.width = parseInt(this.elementResize.style.width) - 4 + "px";
			}
			//event.sthis.topPropagation();
	},
	resizeLeftUpHandler: function(event) {
		dojo.event.disconnect(document, "onmousemove", this, "resizeLeftMoveHandler");
		dojo.event.disconnect(document, "onmouseup", this, "resizeLeftUpHandler");
			//event.sthis.topPropagation();
			
			// Send message
			addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: this.identifier}, {name: 'action', type: 'string', value:  'move'}, {name: 'top', type: 'string', value:  parseInt(this.elementResize.style.top)}, {name: 'left', type: 'string', value:  parseInt(this.elementResize.style.left)}, {name: 'width', type: 'string', value:  parseInt(this.elementResize.style.width)}, {name: 'height', type: 'string', value:  parseInt(this.elementResize.style.height)}]);
			sendMessageQueue();

	},
	ResizeRight : function (identifier, event) {
		this.identifier = identifier;
		this.elementResize = document.getElementById(this.identifier);
		this.elementContent = document.getElementById(this.identifier + "_content");
		
		this.width = parseInt(this.elementResize.style.width);
		
		this.left = parseInt(this.elementResize.style.left);
		
		// Elements to resize
	   
		dojo.event.connect(document, "onmousemove", this, "resizeRightMoveHandler");
		dojo.event.connect(document, "onmouseup", this, "resizeRightUpHandler");;
		

		
		//event.sthis.topPropagation();
		//event.preventDefault();
	},
	resizeRightMoveHandler: function(event) {
			
			if(event.clientX > (parseInt(this.elementResize.style.left) + 50)) {
				// Determine different					


				this.elementResize.style.width = (event.clientX - parseInt(this.elementResize.style.left)) + "px";
				this.elementContent.style.width = parseInt(this.elementResize.style.width) - 4 + "px";
				
				//this.elementResize.style.left = (event.clientX) + "px";
			}
			//event.sthis.topPropagation();
	},
	resizeRightUpHandler: function(event) {
		dojo.event.disconnect(document, "onmousemove", this, "resizeRightMoveHandler");
		dojo.event.disconnect(document, "onmouseup", this, "resizeRightUpHandler");;

			// Send message
			addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: this.identifier}, {name: 'action', type: 'string', value:  'move'}, {name: 'top', type: 'string', value:  parseInt(this.elementResize.style.top)}, {name: 'left', type: 'string', value:  parseInt(this.elementResize.style.left)}, {name: 'width', type: 'string', value:  parseInt(this.elementResize.style.width)}, {name: 'height', type: 'string', value:  parseInt(this.elementResize.style.height)}]);
			sendMessageQueue();
			
			//event.sthis.topPropagation();
	},

	ResizeTop : function (identifier, event) {
		this.identifier = identifier;
		this.elementResize = document.getElementById(this.identifier);
		this.elementContent = document.getElementById(this.identifier + "_content");
		
		this.height = parseInt(this.elementResize.style.height);
		
		this.top = parseInt(this.elementResize.style.top);
		dojo.event.connect(document, "onmousemove", this, "resizeTopMoveHandler");
		dojo.event.connect(document, "onmouseup", this, "resizeTopUpHandler");;
		
		
		//event.sthis.topPropagation();
		//event.preventDefault();
	},
	resizeTopMoveHandler: function(event) {
			
			if( event.clientY < (parseInt(this.elementResize.style.top) + parseInt(this.elementResize.style.height) - 50) ) {
				// Determine different
				this.elementResize.style.height = (parseInt(this.elementResize.style.top) + parseInt(this.elementResize.style.height) - event.clientY)  + "px";
				this.elementResize.style.top = event.clientY + "px";
				
				this.elementContent.style.height = parseInt(this.elementResize.style.height) - 21 + "px";
				
			}
			//event.sthis.topPropagation();
	},
	resizeTopUpHandler: function(event) {
		dojo.event.disconnect(document, "onmousemove", this, "resizeTopMoveHandler");
		dojo.event.disconnect(document, "onmouseup", this, "resizeTopUpHandler");;
			//event.sthis.topPropagation();
			
			// Send message
			addMessage('framework', 'object', [{name: 'identifier', type: 'string', value: this.identifier}, {name: 'action', type: 'string', value:  'move'}, {name: 'top', type: 'string', value:  parseInt(this.elementResize.style.top)}, {name: 'left', type: 'string', value:  parseInt(this.elementResize.style.left)}, {name: 'width', type: 'string', value:  parseInt(this.elementResize.style.width)}, {name: 'height', type: 'string', value:  parseInt(this.elementResize.style.height)}]);
			sendMessageQueue();
	}
});

gwwidgets.core.WidgetManager = new gwwidgets.core.HTMLWidgetManager();