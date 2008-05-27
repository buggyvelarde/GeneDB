var loaded = false;
var response;
var contextMapDiv;

function initContextMap(base, organism, chromosome, chrlen, gene) {
    contextMapDiv = document.getElementById("contextMapDiv");
    var contextMapInfo = getContextMapInfo(base, organism, chromosome, chrlen, gene);
    
    contextMapDiv.onmousedown = startMove;
    document.onmousemove = doMove;
    document.onmouseup   = endMove; // Even if the mouse is released outside the image,
                                    // we still want to know about it! (Though if the mouse
                                    // is released outside the window, we're still screwed.)
    
    contextMapDiv.ondragstart = function() {return false;} // Apparently this is needed to work around IE's brokenness
}


function getContextMapInfo(base, organism, chromosome, chrlen, gene) {
	var url = base + "ContextMap?organism="+organism+"&chromosome="+chromosome+"&chromosomeLength="+chrlen+
	                   "&gene="+gene+"&displayWidth="+contextMapDiv.clientWidth;
	var req = new XMLHttpRequest();
	req.open( "GET", url, true );

	req.onreadystatechange = function () {
	    if ( req.readyState == 4 ) {
	        if ( req.status == 200 ) {
	            response = eval( "(" + req.responseText + ")" );
	            loadTile(base, chrlen, response);
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
var contextMapContent, chromosomeThumbnailWindow;
var basesPerPixel, thumbnailBasesPerPixel;

function loadTile(base, chrlen, tileData) {
    organism = tileData.organism;
    chromosome = tileData.chromosome;
    basesPerPixel = tileData.basesPerPixel;
    thumbnailBasesPerPixel = tileData.chromosomeThumbnail.basesPerPixel;

    var loadingImage = document.getElementById("contextMapLoadingImage");
    contextMapDiv.removeChild(loadingImage);
    contextMapContent = document.createElement("div");
    contextMapContent.id = "contextMapContent";
    contextMapContent.style.width = Math.floor(chrlen / basesPerPixel)+"px";
    contextMapContent.style.left = - (tileData.locus / basesPerPixel + contextMapDiv.getWidth() / 2)+"px";

    var contextMapImage = document.createElement("img");
    contextMapImage.id = "contextMapImage";
    contextMapImage.src = tileData.imageSrc;
    contextMapImage.style.left = (tileData.start / basesPerPixel) + "px";
    contextMapContent.appendChild(contextMapImage);
    contextMapDiv.appendChild(contextMapContent);
    
    var contextMapThumbnailDiv = document.getElementById("contextMapThumbnailDiv");
    var chromosomeThumbnailImage = document.createElement("img");
    chromosomeThumbnailImage.id = "chromosomeThumbnailImage";
    chromosomeThumbnailImage.src = tileData.chromosomeThumbnail.src;
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailImage);
    
    var visibleBases = contextMapDiv.clientWidth * basesPerPixel;
    var windowPixelWidth = visibleBases / tileData.chromosomeThumbnail.basesPerPixel;
    
    chromosomeThumbnailWindow = document.createElement("img");
    chromosomeThumbnailWindow.id = "chromosomeThumbnailWindow";
    chromosomeThumbnailWindow.src=base + "ContextMapWindow?width=" + Math.round(windowPixelWidth);
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailWindow);
    
    chromosomeThumbnailWindow.onmousedown = startDragWindow;
    
    chromosomeThumbnailWindow.style.left = Math.round(-parseFloat(contextMapContent.style.left) * basesPerPixel / thumbnailBasesPerPixel)+"px";

    contextMapDiv.style.height = tileData.imageHeight + "px";
}

var beforeDragPos;
var dragStartX;

var prevX;
var prevTimestamp;

var animationTimer;

var dragging = false;       // Are we currently dragging the image?
var draggingWindow = false; // Are we dragging the window?

function startDragWindow(event) {
    dragging = false;
    draggingWindow = true;
    dragStartX = event.clientX;
    beforeDragPos = parseFloat(chromosomeThumbnailWindow.style.left);
    
    return false;
}

function startMove(event) {
	// If the mouse button was released outside the document window,
	// we are unable to detect that it's been released. Therefore
	// dragging behaviour continues when the mouse pointer is moved
	// back into the document. If the user then clicks again on the
	// context map, we don't want to start a new drag.
	if (dragging) return false;
	
	if (animationTimer != null) {
    	clearInterval(animationTimer);
	   animationTimer = null;
	}

	if (!event) event = window.event; // IE is so broken
	dragStartX = event.clientX;
	dragImage = document.getElementById("contextMapContent");
	if (dragImage != null) {
	    dragging = true;
	    draggingWindow = false;
	    if (dragImage.style.left == "")
	       beforeDragPos = 0;
	    else
    	    beforeDragPos = parseFloat(dragImage.style.left);
    	window.status = "Move started (" + dragStartX + ")";
    	prevX = dragStartX;
    	prevTimeStamp = event.timeStamp;
    	velocity = 0;
    }
	return false;
}

var velocity;
function doMove(event) {
	if (!dragging && !draggingWindow) return;

	if (!event) event = window.event; // IE is so broken
	
	var newPos;
	if (draggingWindow) {
	    var newWindowPos = Math.round(beforeDragPos + event.clientX - dragStartX);
	    newPos = Math.round(newWindowPos * thumbnailBasesPerPixel / basesPerPixel);
    }
    else {
      newPos = Math.round(dragStartX - beforeDragPos - event.clientX);
    }
    
    moveTo(newPos);
	
	velocity = (event.clientX - prevX) / (event.timeStamp - prevTimeStamp);
	prevX = event.clientX;
	prevTimeStamp = event.timeStamp;
}

function moveTo(newPos) {
    if (newPos < 0)
        newPos = 0;
    else if (newPos + contextMapDiv.clientWidth > parseInt(contextMapContent.style.width))
        newPos = parseInt(contextMapContent.style.width) - contextMapDiv.clientWidth;

    contextMapContent.style.left = -newPos + "px";
    chromosomeThumbnailWindow.style.left = Math.round(newPos * basesPerPixel / thumbnailBasesPerPixel) + "px";
}

var animationInterval = 40; // milliseconds
function endMove(event) {
    if (dragging && event.timeStamp - prevTimeStamp < 180)
    	animationTimer = setInterval('animateDeceleration()', animationInterval);
    dragging = false;
    draggingWindow = false;
}

function animateDeceleration() {
    if (Math.abs(velocity) < 0.05) {
        velocity = 0;
        clearInterval(animationTimer);
        animationTimer = null;
    }
    else {
        moveTo(-parseFloat(contextMapContent.style.left) - velocity * animationInterval);
        velocity *= 0.9;
    }
}