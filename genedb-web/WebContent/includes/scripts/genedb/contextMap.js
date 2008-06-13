var loaded = false;
var response;
var contextMapDiv, contextMapThumbnailDiv, contextMapGeneInfo;
var originalTranscriptName = null;
var loadedTranscriptName = null;
var base;

function initContextMap(baseArg, organism, chromosome, chrlen, fmin, fmax, transcript) {
    originalTranscriptName = loadedTranscriptName = transcript;
    base = baseArg;
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
              response = eval( "(" + req.responseText + ")" );
              loadTiles(chrlen, (fmin+fmax)/2, response);
                contextMapDiv.onmousedown = startMove;
          document.onmousemove = doMove;
          document.onmouseup   = endMove; // Even if the mouse is released outside the image,
                                          // we still want to know about it! (Though if the mouse
                                          // is released outside the window, we're still screwed.)

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

var organism;
var chromosome;
var contextMapContent, chromosomeThumbnailWindow;
var basesPerPixel, thumbnailBasesPerPixel;
var selectedTranscript = null;

function loadTiles(chrlen, locus, tileData) {
    organism = tileData.organism;
    chromosome = tileData.chromosome;
    basesPerPixel = tileData.basesPerPixel;
    thumbnailBasesPerPixel = tileData.chromosomeThumbnail.basesPerPixel;

    var loadingElement = document.getElementById("contextMapLoading");
    contextMapDiv.removeChild(loadingElement);
    contextMapContent = document.getElementById("contextMapContent");
    contextMapContent.style.width = Math.round(chrlen / basesPerPixel)+"px";

    var geneTrackHeight = tileData.geneTrackHeight;
    var scaleTrackHeight = tileData.scaleTrackHeight;
    var exonRectHeight = tileData.exonRectHeight;

    for (var tileIndex = 0; tileIndex < tileData.tiles.length; tileIndex++) {
      var tile = tileData.tiles[tileIndex];

      var contextMapImage = document.createElement("img");
      contextMapImage.className  = "contextMapImage";
      contextMapImage.src    = tile.src;
      contextMapImage.width  = tile.width;
      contextMapImage.style.left = (tile.start / basesPerPixel) + "px";
      contextMapContent.appendChild(contextMapImage);
      contextMapDiv.appendChild(contextMapContent);
  }
  contextMapDiv.style.height = tileData.tileHeight + "px";

    var chromosomeThumbnailImage = document.createElement("img");
    chromosomeThumbnailImage.id  = "chromosomeThumbnailImage";
    chromosomeThumbnailImage.src = tileData.chromosomeThumbnail.src;
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailImage);

    var visibleBases = contextMapDiv.clientWidth * basesPerPixel;
    if (visibleBases > chrlen)
        visibleBases = chrlen;
    var windowPixelWidth = Math.floor(visibleBases / thumbnailBasesPerPixel);

    chromosomeThumbnailWindow = document.createElement("img");
    chromosomeThumbnailWindow.id = "chromosomeThumbnailWindow";
    chromosomeThumbnailWindow.src=base + "ContextMapWindow?width=" + windowPixelWidth;
    contextMapThumbnailDiv.appendChild(chromosomeThumbnailWindow);

    chromosomeThumbnailWindow.onmousedown = startDragWindow;
    chromosomeThumbnailWindow.ondragstart = function() {return false;};

    moveTo(locus/basesPerPixel - contextMapDiv.offsetWidth / 2);

    var numPositiveTracks = tileData.positiveTracks.length;
    for (var trackIndex = 0; trackIndex < tileData.positiveTracks.length; trackIndex++) {
       var track = tileData.positiveTracks[trackIndex];
       for (var transcriptIndex = 0; transcriptIndex < track.length; transcriptIndex++) {
           var transcript = track[transcriptIndex];
           var topPx = (numPositiveTracks - trackIndex - 1) * geneTrackHeight
                        + (geneTrackHeight - exonRectHeight) / 2;
           createArea(transcript, topPx, exonRectHeight);
        }
    }

    var topHalf = numPositiveTracks * geneTrackHeight + scaleTrackHeight;
    for (var trackIndex = 0; trackIndex < tileData.negativeTracks.length; trackIndex++) {
       var track = tileData.negativeTracks[trackIndex];
       for (var transcriptIndex = 0; transcriptIndex < track.length; transcriptIndex++) {
           var transcript = track[transcriptIndex];
           var topPx = topHalf + trackIndex * geneTrackHeight + (geneTrackHeight - exonRectHeight) / 2;
           createArea(transcript, topPx, exonRectHeight);
        }
    }

    $("#contextMapInfoPanel .closeButton").click(deselectTranscript);
    $("#contextMapInfoPanel").click(function(event) { event.stopPropagation(); });
    $("#geneDetails").click(deselectTranscript);
    $("#contextMapInfoPanel #loadDetails a").click(function() {
        var hash = this.href;
        hash = hash.replace(/^.*#/, '');
        // moves to a new page.
        // pageload is called at once.
        $.historyLoad(hash);
        return false;
    });
}

function reloadDetails(name) {
    if (name == null || name == "") {
        if (loadedTranscriptName == originalTranscriptName)
            return;
        else
            name = originalTranscriptName; // Going back to initial view
    }

    $("#contextMapInfoPanel:visible").slideUp(200);
    loadedTranscriptName = null;
    $("#geneDetails").fadeTo("slow", 0.4).load("/genedb-web/NamedFeature?name="+name+"&detailsOnly=true", null, function () {
        loadedTranscriptName = name;
        document.title = "Transcript "+name+" - GeneDB";
        $("#geneDetails").stop().fadeTo("fast", 1);
        $("#geneDetailsLoading").hide();
    });
    $("#geneDetailsLoading").show();
}

function deselectTranscript() {
    $("#contextMapInfoPanel:visible").slideUp(200);
    $("#highlighter").hide();
    selectedTranscript = null;
}

function createArea(transcript, topPx, heightPx) {
    // The only way I could get this to work in IE6 was to
    // use a transparent GIF here. (In proper browsers, you
    // can just use a div with no background colour.) -rh11
    var area = document.createElement("img");
    area.src = base + "includes/images/transparentPixel.gif";
    area.className = "transcriptBlock";
    var leftPx = Math.round(transcript.fmin / basesPerPixel);
    var widthPx = Math.round((transcript.fmax - transcript.fmin) / basesPerPixel);

    area.style.left = leftPx + "px";
    area.style.width = widthPx + "px";
    area.style.top = topPx + "px";
    area.style.height = heightPx + "px";
    area.onmousedown = function(event) {
        if (loadedTranscriptName == transcript.name)
            $("#contextMapInfoPanel #loadDetails:visible").hide();
        else
            $("#contextMapInfoPanel #loadDetails:hidden").show();

        if (selectedTranscript != transcript) {
	        selectedTranscript = transcript;
	        highlightTranscript(leftPx, topPx, widthPx, heightPx);
	    }
	    else {
	       // If the details have just been loaded, the transcript
	       // will be selected but the details pane hidden. Although
	       // it provides no new information to reopen it, it feels
	       // more intuitive to allow this.
           $("#contextMapInfoPanel:hidden").slideDown(200);
	    }
        return false;
    };
    area.ondblclick = function() {
        if (selectedTranscript.name != loadedTranscriptName)
            $.historyLoad(selectedTranscript.name);
    };

    contextMapContent.appendChild(area);

    // On initial chromosome load, highlight the transcript we're here for.
    if (transcript.name == originalTranscriptName)
         $("#highlighter").css('left', (leftPx-2) + "px")
			.css('top', (topPx-2) + "px")
			.width((widthPx+4) + "px")
			.height((heightPx+4) + "px")
			.show();
}

function highlightTranscript(left, top, width, height) {
    var highlighter = $("#highlighter");
    highlighter.hide();
    highlighter.css('left', (left-2) + "px")
               .css('top', (top-2) + "px")
               .width((width+4) + "px")
               .height((height+4) + "px");

    var gene = selectedTranscript.gene;
    var name = gene.name;
    if (name == null || name == "")
        name = gene.uniqueName;
    else
        name += " (" + gene.uniqueName + ")";

    var productArray = selectedTranscript.products;
    var products = "";
    if (productArray.length == 1)
        products = productArray[0];
    else {
        products = "";
        for (var i = 0; i < productArray.length; i++)
            products += "<div class='product'>"+productArray[i]+"</div>";
    }

    $("#contextMapInfoPanel:visible").Highlight("fast", "yellow");
    $("#contextMapInfoPanel:hidden").slideDown(200);
    $("#selectedGeneName").text(name);
    $("#selectedGeneProducts").html(products);
    $("#selectedGeneLocation").text(selectedTranscript.fmin + " to " +selectedTranscript.fmax);
    $("#contextMapInfoPanel #loadDetails a").attr("href", "#"+selectedTranscript.name);

    highlighter.show();
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
    if (event == null)
        event = window.event;

    dragging = false;
    draggingWindow = true;
    velocity = 0;
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
         beforeDragPos = parseFloat(contextMapContent.style.left);
     prevX = dragStartX;
     if (event.timeStamp != null)
         prevTimeStamp = event.timeStamp;
    else
        prevTimeStamp = new Date(); // IE, *sigh*
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
        var timeStamp;
        if (event.timeStamp != null)
            timeStamp = event.timeStamp;
        else
            timeStamp = new Date();

        newPos = Math.round(dragStartX - beforeDragPos - event.clientX);
        velocity = (event.clientX - prevX) / (timeStamp - prevTimeStamp);
        prevTimeStamp = timeStamp;
    }

    moveTo(newPos);
  prevX = event.clientX;
}

function moveTo(newPos) {
    var contextMapContentWidth = parseInt(contextMapContent.style.width);
    if (newPos < 0 || contextMapContentWidth <= contextMapDiv.clientWidth)
        newPos = 0;
    else if (newPos + contextMapDiv.clientWidth > contextMapContentWidth)
        newPos = contextMapContentWidth - contextMapDiv.clientWidth;

    contextMapContent.style.left = -newPos + "px";
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

    if (dragging && timeStamp - prevTimeStamp < 180) {
        if (animationTimer != null)
            clearInterval(animationTimer);
      animationTimer = setInterval('animateDeceleration()', animationInterval);
    }
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
        // Sometimes with IE6 we get ridiculous velocities
        if (velocity > 20)
            velocity = 20;
        else if (velocity < -20)
            velocity = -20;

        moveTo(-parseFloat(contextMapContent.style.left) - velocity * animationInterval);
        velocity *= 0.9;
    }
}