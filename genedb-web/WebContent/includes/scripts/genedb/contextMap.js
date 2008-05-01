var dragging = false; // Are we currently dragging the image?
var loaded = false;
var dragStartX;
var response;

function getContextMapInfo(gene) {
	var url = "ContextMap?gene="+gene;
	var req = new XMLHttpRequest();
	req.open( "GET", url, true );

	req.onreadystatechange = function () {
	    if ( req.readyState == 4 ) {
	        if ( req.status == 200 ) {
	            response = eval( "(" + http_request.responseText + ")" );
	        } else {
	            alert( "Request failed." );
	        }
	        req = null;
	        loaded = true;
	    }
    };
    req.send(null);
}

function initContextMap(gene) {
	var contextMapImage = document.getElementById("contextMapImage");
	var contextMapInfo = getContextMapInfo(gene);
	
	contextMapImage.onmousedown = startMove;
	contextMapImage.onmousemove = doMove;
	document.onmouseup          = endMove; // Even if the mouse is released outside the image,
										   // we still want to know about it! (Though if the mouse
										   // is released outside the window, we're still screwed.)
	
	contextMapImage.ondragstart = function() {return false;} // Apparently this is needed to work around IE's brokenness
}

function startMove(event) {
	// If the mouse button was released outside the document window,
	// we are unable to detect that it's been released. Therefore
	// dragging behaviour continues when the mouse pointer is moved
	// back into the document. If the user then clicks again on the
	// context map, we don't want to start a new drag.
	if (dragging) return false;

	dragging = true;
	if (!event) event = window.event; // IE is so broken
	dragStartX = event.clientX;
	window.status = "Move started (" + dragStartX + ")";
	return false;
}

function doMove(event) {
	if (!dragging) return;

	if (!event) event = window.event; // IE is so broken

	window.status = event.clientX;
}

function endMove(event) {
	dragging = false;
	window.status = "Done dragging";
}