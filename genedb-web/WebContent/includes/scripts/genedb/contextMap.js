var loaded = false;
var response;
var contextMapDiv, contextMapThumbnailDiv, contextMapGeneInfo;

function initContextMap(base, organism, chromosome, chrlen, fmin, fmax) {
    contextMapDiv = document.getElementById("contextMapDiv");
    contextMapThumbnailDiv = document.getElementById("contextMapThumbnailDiv");
    contextMapGeneInfo = document.getElementById("contextMapGeneInfo");

    var contextMapInfo = getContextMapInfo(base, organism, chromosome, chrlen, fmin, fmax);
    
    contextMapDiv.onmousedown = startMove;
    document.onmousemove = doMove;
    document.onmouseup   = endMove; // Even if the mouse is released outside the image,
                                    // we still want to know about it! (Though if the mouse
                                    // is released outside the window, we're still screwed.)
    
    contextMapDiv.ondragstart = function() {return false;} // Apparently this is needed to work around IE's brokenness
}


function getContextMapInfo(base, organism, chromosome, chrlen, fmin, fmax) {
	var url = base + "ContextMap?organism="+organism+"&chromosome="+chromosome+"&chromosomeLength="+chrlen+
	                   "&thumbnailDisplayWidth="+contextMapThumbnailDiv.clientWidth;
	var req = new XMLHttpRequest();
	req.open( "GET", url, true );

	req.onreadystatechange = function () {
	    if ( req.readyState == 4 ) {
	        if ( req.status == 200 ) {
	            response = eval( "(" + req.responseText + ")" );
	            loadTile(base, chrlen, (fmin+fmax)/2, response);
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

function loadTile(base, chrlen, locus, tileData) {
    organism = tileData.organism;
    chromosome = tileData.chromosome;
    basesPerPixel = tileData.basesPerPixel;
    thumbnailBasesPerPixel = tileData.chromosomeThumbnail.basesPerPixel;

    var loadingImage = document.getElementById("contextMapLoadingImage");
    contextMapDiv.removeChild(loadingImage);
    contextMapContent = document.createElement("div");
    contextMapContent.id = "contextMapContent";
    contextMapContent.style.width = Math.floor(chrlen / basesPerPixel)+"px";
    contextMapContent.style.left = (contextMapDiv.getWidth() / 2 - locus / basesPerPixel)+"px";

    var geneTrackHeight = tileData.geneTrackHeight;
    var scaleTrackHeight = tileData.scaleTrackHeight;
    var exonRectHeight = tileData.exonRectHeight;
    
    for (var tileIndex = 0; tileIndex < tileData.tiles.length; tileIndex++) {
        var tile = tileData.tiles[tileIndex];
        
	    var contextMapImage = document.createElement("img");
	    contextMapImage.id     = "contextMapImage";
	    contextMapImage.src    = tile.src;
	    contextMapImage.width  = tile.width;
	    contextMapImage.style.left = (tile.start / basesPerPixel) + "px";
	    contextMapContent.appendChild(contextMapImage);
	    contextMapDiv.appendChild(contextMapContent);
	}
	contextMapDiv.style.height = tileData.tileHeight + "px";
    
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
    
    var numPositiveTracks = tileData.positiveTracks.length;
    for (var trackIndex = 0; trackIndex < tileData.positiveTracks.length; trackIndex++) {
       var track = tileData.positiveTracks[trackIndex];
       for (var transcriptIndex = 0; transcriptIndex < track.length; transcriptIndex++) {
           var transcript = track[transcriptIndex];
           var topPx = (numPositiveTracks - trackIndex - 1) * geneTrackHeight
                        + (geneTrackHeight - exonRectHeight) / 2;
           createArea(transcript, topPx, geneTrackHeight);
        }
    }

    var topHalf = numPositiveTracks * geneTrackHeight + scaleTrackHeight;
    for (var trackIndex = 0; trackIndex < tileData.negativeTracks.length; trackIndex++) {
       var track = tileData.negativeTracks[trackIndex];
       for (var transcriptIndex = 0; transcriptIndex < track.length; transcriptIndex++) {
           var transcript = track[transcriptIndex];
           var topPx = topHalf + trackIndex * geneTrackHeight + (geneTrackHeight - exonRectHeight) / 2;
           createArea(transcript, topPx, geneTrackHeight);
        }
    }
}

function createArea(transcript, topPx, heightPx) {
    var area = document.createElement("div");
    area.style.position = "absolute";
    var leftPx = transcript.fmin / basesPerPixel;
    area.style.left = leftPx + "px";
    area.style.width = (transcript.fmax / basesPerPixel - leftPx) + "px";
    area.style.top = topPx + "px";
    area.style.height = heightPx + "px";
    area.style.cursor = "pointer";
    area.onmouseover = function() {
        contextMapGeneInfo.textContent = transcript.name
            + " (" + transcript.products + ")";
    };
    
    contextMapContent.appendChild(area);
}

var beforeDragPos;
var dragStartX;

var prevX;
var prevTimestamp;

var animationTimer;

var dragging = false;       // Are we currently dragging the image?
var draggingWindow = false; // Are we dragging the window?
var velocity = 0;

function startDragWindow(event) {
    dragging = false;
    draggingWindow = true;
    velocity = 0;
    dragStartX = event.clientX;
    beforeDragPos = parseFloat(chromosomeThumbnailWindow.style.left);
    
    return false;
}

function startMove(event) {
	// If the mouse button was released outside the document window,
	// we are unable to detect that it's been released (in Firefox).
    // Therefore dragging behaviour continues when the mouse pointer
    // is moved back into the document. If the user then clicks again
    // on the context map, we don't want to start a new drag.
	if (dragging) return false;
	
	if (!event) event = window.event; // IE is so broken
	dragStartX = event.clientX;
    dragging = true;
    draggingWindow = false;
    if (contextMapContent.style.left == "")
       beforeDragPos = 0;
    else
   	    beforeDragPos = parseFloat(contextMapContent.style.left);
   	prevX = dragStartX;
   	prevTimeStamp = event.timeStamp;
   	velocity = 0;

	return false;
}

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
        velocity = (event.clientX - prevX) / (event.timeStamp - prevTimeStamp);
        prevTimeStamp = event.timeStamp;
    }
    
    moveTo(newPos);
	prevX = event.clientX;
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