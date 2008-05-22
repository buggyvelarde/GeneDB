var dragging = false; // Are we currently dragging the image?
var loaded = false;
var response;

function getContextMapInfo(base, organism, chromosome, gene) {
	var url = base + "ContextMap?organism="+organism+"&chromosome="+chromosome+"&gene="+gene;
	var req = new XMLHttpRequest();
	req.open( "GET", url, true );

	req.onreadystatechange = function () {
	    if ( req.readyState == 4 ) {
	        if ( req.status == 200 ) {
	            response = eval( "(" + req.responseText + ")" );
	            loadTile(response);
	        } else {
	            alert( "Request failed." );
	        }
	        req = null;
	        loaded = true;
	    }
    };
    req.send(null);
}

var organism;
var chromosome;
function loadTile(tileData) {
    organism = tileData.organism;
    chromosome = tileData.chromosome;

    var contextMapDiv = document.getElementById("contextMapDiv");
    var loadingImage = document.getElementById("contextMapLoadingImage");
    contextMapDiv.removeChild(loadingImage);
    var contextMapImage = document.createElement("img");
    contextMapImage.id = "contextMapImage";
    contextMapImage.src = tileData.imageSrc;
    contextMapImage.style.left = ((tileData.start - tileData.locus) / tileData.basesPerPixel + contextMapDiv.getWidth() / 2)+"px";
    contextMapDiv.appendChild(contextMapImage);
    
    var chromosomeThumbnailImage = document.createElement("img");
    chromosomeThumbnailImage.id = "chromosomeThumbnailImage";
    chromosomeThumbnailImage.src = tileData.chromosomeThumbnailSrc;
    contextMapDiv.appendChild(chromosomeThumbnailImage);

    contextMapDiv.style.height = tileData.imageHeight + 10;
}

function initContextMap(base, organism, chromosome, gene) {
	var contextMapDiv = document.getElementById("contextMapDiv");
	var contextMapInfo = getContextMapInfo(base, organism, chromosome, gene);
	
	contextMapDiv.onmousedown = startMove;
	//contextMapDiv.onmousemove = doMove;
	document.onmousemove = doMove;
	document.onmouseup          = endMove; // Even if the mouse is released outside the image,
										   // we still want to know about it! (Though if the mouse
										   // is released outside the window, we're still screwed.)
	
	contextMapDiv.ondragstart = function() {return false;} // Apparently this is needed to work around IE's brokenness
}

var beforeDragPos;
var dragStartX;
var dragImage;

function startMove(event) {
	// If the mouse button was released outside the document window,
	// we are unable to detect that it's been released. Therefore
	// dragging behaviour continues when the mouse pointer is moved
	// back into the document. If the user then clicks again on the
	// context map, we don't want to start a new drag.
	if (dragging) return false;

	if (!event) event = window.event; // IE is so broken
	dragStartX = event.clientX;
	dragImage = document.getElementById("contextMapImage");
	if (dragImage != null) {
	    dragging = true;
	    if (dragImage.style.left == "")
	       beforeDragPos = 0;
	    else
    	    beforeDragPos = parseFloat(dragImage.style.left);
    	window.status = "Move started (" + dragStartX + ")";
    }
	return false;
}

function doMove(event) {
	if (!dragging) return;

	if (!event) event = window.event; // IE is so broken

    dragImage.style.left = beforeDragPos + event.clientX - dragStartX;
	window.status = event.clientX;
}

function endMove(event) {
	dragging = false;
	window.status = "Done dragging";
}