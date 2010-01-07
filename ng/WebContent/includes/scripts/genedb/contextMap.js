var loaded = false;
var contextMapDiv, contextMapThumbnailDiv, contextMapGeneInfo;
var originalTranscriptName = null;
var loadedTranscriptName = null;
var base;
var imageController;
var animationTimer = null;
var cruise = false;

function initContextMap(baseArg, organism, chromosome, chrlen, fmin, fmax, transcript) {
    originalTranscriptName = loadedTranscriptName = transcript;
    base = baseArg;
    imageController = base + "Image/";
    contextMapDiv = document.getElementById("contextMapDiv");
    contextMapThumbnailDiv = document.getElementById("contextMapThumbnailDiv");
    contextMapGeneInfo = document.getElementById("contextMapGeneInfo");

    getContextMapInfo(organism, chromosome, chrlen, fmin, fmax);

    $.historyInit(reloadDetails);
}

function getContextMapInfo(organism, chromosome, chrlen, fmin, fmax) {
	var url = base + "ContextMap?organism="+organism+"&chromosome="+chromosome+"&chromosomeLength="+chrlen+
	                   "&thumbnailDisplayWidth="+contextMapThumbnailDiv.clientWidth;
	var req;
	if (window.XMLHttpRequest)
       req = new XMLHttpRequest();
    else
       req = new ActiveXObject('Microsoft.XMLHTTP');
    req.open( "GET", url, true );

    req.onreadystatechange = function () {
        if ( req.readyState == 4 ) {
            if ( req.status == 200 ) {
                var tileData = eval( "(" + req.responseText + ")" );
                loadTiles(chrlen, tileData);
                contextMapDiv.onmousedown = startMove;
                document.onmousemove = doMove;
                document.onmouseup   = endMove; // Even if the mouse is released outside the image,
                                                // we still want to know about it! (Though if the mouse
                                                // is released outside the window, we're still screwed.)
                $().keydown(handleKeyDown).keyup(handleKeyUp);
                $("form").keydown(function(event){event.stopPropagation()});

                contextMapDiv.ondragstart = function() {return false;} // Apparently this is needed to work around IE's brokenness
                contextMapDiv.style.cursor = "move";
            } else {
                var loadingElement = document.getElementById("contextMapLoading");
                contextMapDiv.removeChild(loadingElement);
                var errorMessage = document.createElement("div");
                errorMessage.id = "errorMessage";
                errorMessage.innerText = errorMessage.textContent
                    = "Error: failed to load context map";
                contextMapDiv.appendChild(errorMessage);
            }
            req = null;
            loaded = true;
        }
    };
    req.send(null);
}

var selectedTranscript = null;
var selectedArea = null;
var turbo = false;

function handleKeyDown(event) {
    if (event.metaKey || event.ctrlKey)
        return true;
    switch(event.keyCode) {
    case 16: /* shift */
        turbo = true;
        return false;
    case 37: /* left arrow */
        if (velocity < 1)
            startDeceleration(1);
        cruise = true;
        return false;
    case 39: /* right arrow */
        if (velocity > -1)
            startDeceleration(-1);
        cruise = true;
        return false;
    case 27: /* escape */
        deselectTranscript();
        return false;
    case 219: /* left square bracket */
        if (!selectedArea) return true;
        var nextArea = selectedArea.previousSibling;
        if (nextArea != null && nextArea.transcript) {
            selectTranscript(nextArea);
            showDetailsOfSelectedTranscript();
            break;
        }
        for (var i=0; i < selectedArea.copies.length; i++) {
            nextArea = selectedArea.copies[i].previousSibling;
            if (nextArea != null && nextArea.transcript) {
                selectTranscript(nextArea);
                showDetailsOfSelectedTranscript();
                break;
            }
        }
        return false;
    case 221: /* right square bracket */
        if (!selectedArea) return true;
        var nextArea = selectedArea.nextSibling;
        if (nextArea != null) {
            selectTranscript(nextArea);
            showDetailsOfSelectedTranscript();
            break;
        }
        for (var i=0; i < selectedArea.copies.length; i++) {
            nextArea = selectedArea.copies[i].nextSibling;
            if (nextArea != null) {
                selectTranscript(nextArea);
                showDetailsOfSelectedTranscript();
                break;
            }
        }
        return false;
    case 10: case 13:
        if (selectedTranscript != null && $("#contextMapInfoPanel").is(":visible")
        && selectedTranscript[0] != loadedTranscriptName)
            $.historyLoad(selectedTranscript[0]);
        return false;
    }

    return true;
}

function handleKeyUp(event) {
    switch (event.keyCode) {
    case 16: /*shift*/
        turbo = false;
        break;
    case 37:
        if (velocity > 0)
            cruise = false;
        break;
    case 39:
        if (velocity < 0)
            cruise = false;
        break;
    }
}

var organism, chromosome, products;
var contextMapContent, chromosomeThumbnailWindow;
var basesPerPixel, thumbnailBasesPerPixel;

var loading = false;
function loadTiles(chrlen, tileData) {
    loading = true;
    organism = tileData.organism;
    chromosome = tileData.chromosome;
    products = tileData.products;
    basesPerPixel = tileData.basesPerPixel;
    thumbnailBasesPerPixel = (tileData.end - tileData.start) / tileData.chromosomeThumbnail.width;

    var loadingElement = document.getElementById("contextMapLoading");
    contextMapDiv.removeChild(loadingElement);
    contextMapContent = document.getElementById("contextMapContent");
    contextMapContent.style.left="0px";
    contextMapDiv.style.height = tileData.tileHeight + "px";

    var chromosomeThumbnailImage = document.createElement("img");
    chromosomeThumbnailImage.id  = "chromosomeThumbnailImage";
    chromosomeThumbnailImage.src = imageController + tileData.chromosomeThumbnail.src;
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailImage);

    var visibleBases = contextMapDiv.clientWidth * basesPerPixel;
    if (visibleBases > chrlen)
        visibleBases = chrlen;
    var windowPixelWidth = Math.floor(visibleBases / thumbnailBasesPerPixel);

    chromosomeThumbnailWindow = document.createElement("img");
    chromosomeThumbnailWindow.id = "chromosomeThumbnailWindow";
    chromosomeThumbnailWindow.src=base + "ContextMapWindow/" + windowPixelWidth;
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailWindow);

    chromosomeThumbnailWindow.style.left="0px";
    chromosomeThumbnailWindow.onmousedown = startDragWindow;
    chromosomeThumbnailWindow.ondragstart = function() {return false;};

    $("#contextMapInfoPanel .closeButton").click(function(){
        deselectTranscript();
        return false;
    });
    $("#contextMapInfoPanel").click(function(event) { event.stopPropagation(); });
    $("#geneDetails").click(deselectTranscript);
    $("#contextMapInfoPanel #loadDetails a").click(function() {
        loadSelectedTranscript();
        return false;
    });

    var leftPx = 0;
    var tilePrefix = tileData.tilePrefix;
    contextMapContent.trueLeft = 0;
    for (var tileIndex = 0; tileIndex < tileData.tiles.length; tileIndex++) {
        var tile = tileData.tiles[tileIndex];

        var contextMapImage = document.createElement("img");
        contextMapImage.className  = "contextMapImage";
        contextMapImage.src    = imageController + tile[1];
        contextMapImage.width  = tile[0];
        contextMapImage.height = tileData.tileHeight; // IE6 needs this
        contextMapImage.style.left = (leftPx - contextMapContent.trueLeft) + "px";
        contextMapContent.appendChild(contextMapImage);

        if (leftPx + tile[0] > contextMapContent.trueLeft + 32767) {
            var newContent = document.createElement("div");
            newContent.style.display="none";
            newContent.className = "contextMapContent";

            var newContentHighlighter = document.createElement("div");
            newContentHighlighter.className="highlighter";
            newContent.appendChild(newContentHighlighter);

            var overlapTile = contextMapImage.previousSibling.cloneNode(false);
            newContent.trueLeft = leftPx - overlapTile.width;
            overlapTile.style.left="0px";
            contextMapImage.style.left = overlapTile.width + "px";
            newContent.appendChild(overlapTile);
            newContent.appendChild(contextMapImage);

            contextMapContent.style.width = (leftPx - contextMapContent.trueLeft) + "px";
            contextMapDiv.appendChild(newContent);
            contextMapContent = newContent;
        }
       leftPx += tile[0];
    }
    contextMapContent.style.width = (leftPx - contextMapContent.trueLeft) + "px";
    contextMapContent = document.getElementById("contextMapContent");

    loadFeatures(tileData, tileData.features);
    loading = false;
}

function loadFeatures (tileData, features) {
    var geneTrackHeight = tileData.geneTrackHeight;
    var scaleTrackHeight = tileData.scaleTrackHeight;
    var exonRectHeight = tileData.exonRectHeight;
    var numPositiveTracks = tileData.numberOfPositiveTracks;
    var topHalf = numPositiveTracks * geneTrackHeight + scaleTrackHeight;

    var numFeatures = features.length;
    for (var featureIndex = 0; featureIndex < numFeatures; featureIndex++) {
        var feature = features[featureIndex];
        var track = feature[3];

        var topPx = (geneTrackHeight - exonRectHeight) / 2;
        if (track > 0)
            topPx += (numPositiveTracks - track++) * geneTrackHeight;
        else if (track < 0)
            topPx += topHalf - (1 + track--) * geneTrackHeight;
        else
            topPx = numPositiveTracks * geneTrackHeight;

        createArea(feature, topPx, exonRectHeight);
    }

}

function reloadDetails(name) {
    if (loading) return;
    if (name == null || name == "") {
        if (loadedTranscriptName == originalTranscriptName)
            return;
        else
            name = originalTranscriptName; // Going back to initial view
        $("#navigatePages").show();
    }

    $("#contextMapInfoPanel:visible").slideUp(200);
    loadedTranscriptName = null;

    loading = true;
    var loadingDetailsTimer = setTimeout('$("#geneDetailsLoading").show()', 2000);
    $("#geneDetails").fadeTo("slow", 0.4).load(base + "gene/"+name+"?detailsOnly=true", null, function () {
        clearTimeout(loadingDetailsTimer);
        loadedTranscriptName = name;
        document.title = "Transcript "+name+" - GeneDB";
        $("#geneDetails").stop().fadeTo("fast", 1);
        if (name != originalTranscriptName)
        	$("#navigatePages").hide();
        $("#geneDetailsLoading").hide();
        loading = false;
        selectTranscriptByName(name);
    });
}

var areasByTranscriptName = new Array(), loadedArea = null;
function selectLoaded() {
    if (loadedArea != null)
        selectTranscript(loadedArea);
}
function createArea(transcript, topPx, heightPx) {
    // The only way I could get this to work in IE6 was to
    // use a transparent GIF here. (In proper browsers, you
    // can just use a div with no background colour.) -rh11
    var area = document.createElement("img");
    area.src = base + "includes/images/transparentPixel.gif";
    area.className = "transcriptBlock";

    area.style.width = transcript[5] + "px";
    area.style.top = topPx + "px";
    area.style.height = heightPx + "px";

    areasByTranscriptName[transcript[0]] = area;

    if (transcript[3] != 0) {
        // Really a transcript
        area.transcript = transcript;
        area.onmousedown = function(event) {
            if (transcript == selectedTranscript)
                $("#contextMapInfoPanel:hidden").slideDown(200);
            else {
                selectTranscript(this, transcript);
                showDetailsOfSelectedTranscript();
            }
            return false;
        };
        area.ondblclick = loadSelectedTranscript;

        area.setAttribute("title", geneName(transcript));
    }
    else {
        area.setAttribute("title", transcript[0]);
    }
    area.copies = [];
    var c = contextMapContent;
    do {
        if (c.trueLeft < transcript[4] + transcript[5] && transcript[4] < c.trueLeft + parseInt(c.style.width)) {
            var clonedArea = area.cloneNode(false);
            area.copies.push(clonedArea);
            clonedArea.copies = area.copies;
            clonedArea.onmousedown = area.onmousedown;
            clonedArea.ondblclick = area.ondblclick;
            clonedArea.transcript = area.transcript;
            clonedArea.style.left = (transcript[4] - c.trueLeft) + "px";
            c.appendChild(clonedArea);
        }
    } while (c = c.nextSibling);

    // On initial chromosome load, highlight the transcript we're here for.
    if (transcript[0] == loadedTranscriptName) {
        loadedArea = area;
        selectTranscript(area, transcript);
        populateInfoPanel(transcript);
    }
}

function loadSelectedTranscript() {
    if (selectedTranscript[0] != loadedTranscriptName) {
        $.historyLoad(selectedTranscript[0]);
        loadedArea = selectedArea;
    }
}

function computedStyle(element, style)
{
  if (typeof element.currentStyle != 'undefined')
    return element.currentStyle[style]; // IE
  else
    return document.defaultView.getComputedStyle(element, null)[style];
}

function selectTranscriptByName(name) {
    if (areasByTranscriptName[name])
        selectTranscript(areasByTranscriptName[name]);
    else if (window.console)
        window.console.log("Did not find transcript '%s'", name);
}

function selectTranscript(area, transcript) {
    if (transcript == null)
        transcript = area.transcript;

    selectedTranscript = transcript;

    selectedArea = null;
    var areaCopy, leftPx, widthPx;
    for (var i=0; i < area.copies.length; i++) {
        areaCopy = area.copies[i];
        if (areaCopy.parentNode.style.display != 'none')
            selectedArea = areaCopy;

        leftPx   = parseInt(areaCopy.style.left);
        widthPx  = parseInt(areaCopy.style.width);
        var topPx    = parseInt(areaCopy.style.top);
        var heightPx = parseInt(areaCopy.style.height);
        highlightRectangle(areaCopy.parentNode, leftPx, topPx, widthPx, heightPx);
    }

    if (selectedArea == null) {
        /* The selected area is not on any visible layer */
        scrollTo(areaCopy.parentNode.trueLeft + leftPx + widthPx / 2,
            function() {selectTranscript(area, transcript);});
        return;
    }

    var visibleLeft = contextMapContent.trueLeft - parseFloat(contextMapContent.style.left);
    var visibleRight = visibleLeft + contextMapDiv.clientWidth;
    var absoluteLeftPx = leftPx + areaCopy.parentNode.trueLeft;
    if (absoluteLeftPx < visibleLeft || absoluteLeftPx + widthPx > visibleRight)
        scrollTo(absoluteLeftPx + widthPx / 2);
}

function deselectTranscript() {
    $("#contextMapInfoPanel:visible").slideUp(200, function() {$(".highlighter").hide();});
    selectedTranscript = null;
}

function highlightRectangle(within, left, top, width, height) {
    $(".highlighter", within)
        .hide()
        .css('left', (left-2) + "px").css('top', (top-2) + "px")
        .width((width+4) + "px").height((height+4) + "px")
        .show();
}

function geneName(transcript) {
    var name = transcript[2];
    if (name == null || name == "" || name == transcript[1])
        name = transcript[1];
    else
        name += " (" + transcript[1] + ")";
    return name;
}

function populateInfoPanel(transcript) {
    var productIndexArray = transcript[6];
    var productsHtml = "";
    for (var i = 0; i < productIndexArray.length; i++)
        productsHtml += "<div class='product'>"+products[productIndexArray[i]]+"</div>";

    $("#selectedGeneName").text(geneName(transcript));
    $("#selectedGeneProducts").html(productsHtml);
    $("#contextMapInfoPanel #loadDetails a").attr("href", "#"+transcript[0]);
}

function showDetailsOfSelectedTranscript() {
    populateInfoPanel(selectedTranscript);
    $("#contextMapInfoPanel:visible").effect("highlight", {color: "yellow"}, "fast");
    $("#contextMapInfoPanel:hidden").slideDown(200);
}

var beforeDragPos;
var dragStartX;

var prevX;
var prevTimestamp;

var dragging = false;       // Are we currently dragging the image?
var draggingWindow = false; // Are we dragging the window?
var velocity = 0;

function startDragWindow(event) {
    if (event == null)
        event = window.event;

    dragging = false;
    draggingWindow = true;
    startDeceleration(0);
    dragStartX = event.clientX;
    beforeDragPos = parseFloat(chromosomeThumbnailWindow.style.left);

    return false;
}

function startMove(event) {
    // If the mouse button was released outside the document window,
    // we are unable to detect that it's been released (except in Safari).
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
         beforeDragPos = contextMapContent.trueLeft - parseFloat(contextMapContent.style.left);
     prevX = dragStartX;
     if (event.timeStamp != null /* IE6 */ && event.timeStamp != 0 /* Opera */)
         prevTimeStamp = event.timeStamp;
    else
        prevTimeStamp = new Date();
     startDeceleration(0);

    return false;
}

function doMove(event) {
    if (!dragging && !draggingWindow) return;

    if (!event) event = window.event; // IE is so broken

    var newPos;
    if (draggingWindow) {
        var newWindowPos = Math.round(event.clientX - dragStartX + beforeDragPos);
        newPos = Math.round(newWindowPos * thumbnailBasesPerPixel / basesPerPixel);
    }
    else {
        var timeStamp;
        if (event.timeStamp != null /* IE6 */ && event.timeStamp != 0 /* Opera */)
            timeStamp = event.timeStamp;
        else
            timeStamp = new Date();

        newPos = Math.round(dragStartX + beforeDragPos - event.clientX);
        var newVelocity = (event.clientX - prevX) / (timeStamp - prevTimeStamp);
        if (!isNaN(newVelocity)) velocity = newVelocity;
        prevTimeStamp = timeStamp;
    }

    moveTo(newPos);
    prevX = event.clientX;
}

function moveTo(newPos) {
    var contextMapContentWidth = contextMapContent.trueLeft + parseInt(contextMapContent.style.width);
    var hitEnd = false;
    var newContent;
    if (newPos < contextMapContent.trueLeft || contextMapContentWidth <= contextMapDiv.clientWidth) {
        newContent = contextMapContent.previousSibling;
        if (newContent && newContent.nodeType == 3)
            newContent = newContent.previousSibling
        if (!newContent || newContent.nodeType != 1 || newContent.className != "contextMapContent") {
            newPos = 0;
            hitEnd = true;
        }
    }
    else if (newPos + contextMapDiv.clientWidth > contextMapContentWidth) {
        newContent = contextMapContent.nextSibling;
        if (newContent && newContent.nodeType == 3)
            newContent = newContent.nextSibling
        if (!newContent || newContent.nodeType != 1) {
            newPos = contextMapContentWidth - contextMapDiv.clientWidth;
            hitEnd = true;
        }
    }
    if (hitEnd && cruise) {
        velocity *= -0.1;
        cruise = false;
    }
    else if (newContent && !hitEnd) {
        contextMapContent.style.display="none";
        contextMapContent = newContent;
        contextMapContent.style.display="block";
    }

    contextMapContent.style.left = (contextMapContent.trueLeft-newPos) + "px";
    chromosomeThumbnailWindow.style.left = Math.round(newPos * basesPerPixel / thumbnailBasesPerPixel) + "px";
}

var animationInterval = 40; // milliseconds
function endMove(event) {
    if (event == null)
        event = window.event;
    if (event.timeStamp != null)
        timeStamp = event.timeStamp;
    else
        timeStamp = new Date();

    if (dragging && timeStamp - prevTimeStamp < 180)
        startDeceleration(velocity);

    dragging = false;
    draggingWindow = false;
    cruise = false;

    return true;
}

var scrollToPos = null;
var scrollToCallback = null;
function scrollTo(pos, callback) {
    scrollToPos = pos;
    scrollToCallback = callback;
    if (animationTimer == null)
        animationTimer = setInterval('animateDeceleration()', animationInterval);
}

function startDeceleration(startingVelocity) {
    scrollToPos = null;
    velocity = startingVelocity;
    if (velocity != 0 && animationTimer == null)
        animationTimer = setInterval('animateDeceleration()', animationInterval);
}

function animateDeceleration() {
    var leftPos = contextMapContent.trueLeft - parseFloat(contextMapContent.style.left);
    if (scrollToPos != null) {
        var centrePos = leftPos + contextMapDiv.clientWidth / 2;
        var delta = centrePos - scrollToPos;

        velocity = delta / 200;
    }
    else {
        // Sometimes with IE6 we get ridiculous velocities
        if (velocity > 20)
            velocity = 20;
        else if (velocity < -20)
            velocity = -20;
    }
    if (Math.abs(velocity) < 0.05) {
        if (scrollToPos != null && scrollToCallback != null)
            scrollToCallback();
        velocity = 0;
        scrollToPos = null;
        clearInterval(animationTimer);
        animationTimer = null;
    }
    else {
        moveTo(leftPos - velocity * animationInterval);

        if (cruise) {
            if (Math.abs(velocity) < 20) {
                velocity *= (turbo ? 1.10 : 1.01);
            }
        }
        else
            velocity *= 0.9;
    }
}
