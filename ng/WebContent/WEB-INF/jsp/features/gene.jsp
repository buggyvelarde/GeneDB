<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<misc:url value="/includes/scripts/web-artemis" var="wa"/>
<misc:url value="/" var="base"/>

<!--<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined.js"/>"></script>
--><%-- <script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/contextMap.js"/>"></script>
 --%>


<format:header title="Feature: ${dto.uniqueName}">
    
	<link rel="stylesheet" type="text/css" href="${wa}/css/superfish.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/css/tablesorter.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/js/jquery.contextMenu-1.01/jquery.contextMenu.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/css/artemis.css" media="screen">
    
    <script type="text/javascript" src="${wa}/js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery-ui-1.8.4.custom.min.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.drawinglibrary/js/jquery.svg.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.drawinglibrary/js/jquery.drawinglibrary.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.flot.min.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.flot.selection.min.js"></script>

    <script type="text/javascript" src="${wa}/js/popup.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.contextMenu-1.01/jquery.contextMenu.js"></script>

    <script type="text/javascript" src="${wa}/js/observerable.js"></script>
    <script type="text/javascript" src="${wa}/js/utility.js"></script>
    <script type="text/javascript" src="${wa}/js/bases.js"></script>
    <script type="text/javascript" src="${wa}/js/aminoacid.js"></script>
    <script type="text/javascript" src="${wa}/js/superfish-1.4.8/hoverIntent.js"></script>
    <script type="text/javascript" src="${wa}/js/superfish-1.4.8/superfish.js"></script>
    <script type="text/javascript" src="${wa}/js/jquery.tablesorter.min.js"></script>
    <script type="text/javascript" src="${wa}/js/graph.js"></script>
    <script type="text/javascript" src="${wa}/js/scrolling.js"></script>
    <script type="text/javascript" src="${wa}/js/selection.js"></script>
    <script type="text/javascript" src="${wa}/js/zoom.js"></script>
    <script type="text/javascript" src="${wa}/js/featureCvTerm.js"></script>
    <script type="text/javascript" src="${wa}/js/bam.js"></script>
    <script type="text/javascript" src="${wa}/js/featureList.js"></script>
    <script type="text/javascript" src="${wa}/js/navigate.js"></script>
    <script type="text/javascript" src="${wa}/js/genome.js"></script>
    <script type="text/javascript" src="${wa}/js/samFlag.js"></script>
    
    
    <script>
    
    /*
     * jQuery history plugin
     * 
     * The MIT License
     * 
     * Copyright (c) 2006-2009 Taku Sano (Mikage Sawatari)
     * Copyright (c) 2010 Takayuki Miwa
     * 
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     * 
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     * 
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
     * THE SOFTWARE.
     */

    (function($) {
        var locationWrapper = {
            put: function(hash, win) {
                (win || window).location.hash = this.encoder(hash);
            },
            get: function(win) {
                var hash = ((win || window).location.hash).replace(/^#/, '');
                try {
                    return $.browser.mozilla ? hash : decodeURIComponent(hash);
                }
                catch (error) {
                    return hash;
                }
            },
            encoder: encodeURIComponent
        };

        var iframeWrapper = {
            id: "__jQuery_history",
            init: function() {
                var html = '<iframe id="'+ this.id +'" style="display:none" src="javascript:false;" />';
                $("body").prepend(html);
                return this;
            },
            _document: function() {
                return $("#"+ this.id)[0].contentWindow.document;
            },
            put: function(hash) {
                var doc = this._document();
                doc.open();
                doc.close();
                locationWrapper.put(hash, doc);
            },
            get: function() {
                return locationWrapper.get(this._document());
            }
        };

        function initObjects(options) {
            options = $.extend({
                    unescape: false
                }, options || {});

            locationWrapper.encoder = encoder(options.unescape);

            function encoder(unescape_) {
                if(unescape_ === true) {
                    return function(hash){ return hash; };
                }
                if(typeof unescape_ == "string" &&
                   (unescape_ = partialDecoder(unescape_.split("")))
                   || typeof unescape_ == "function") {
                    return function(hash) { return unescape_(encodeURIComponent(hash)); };
                }
                return encodeURIComponent;
            }

            function partialDecoder(chars) {
                var re = new RegExp($.map(chars, encodeURIComponent).join("|"), "ig");
                return function(enc) { return enc.replace(re, decodeURIComponent); };
            }
        }

        var implementations = {};

        implementations.base = {
            callback: undefined,
            type: undefined,

            check: function() {},
            load:  function(hash) {},
            init:  function(callback, options) {
                initObjects(options);
                self.callback = callback;
                self._options = options;
                self._init();
            },

            _init: function() {},
            _options: {}
        };

        implementations.timer = {
            _appState: undefined,
            _init: function() {
                var current_hash = locationWrapper.get();
                self._appState = current_hash;
                self.callback(current_hash);
                setInterval(self.check, 100);
            },
            check: function() {
                var current_hash = locationWrapper.get();
                if(current_hash != self._appState) {
                    self._appState = current_hash;
                    self.callback(current_hash);
                }
            },
            load: function(hash) {
                if(hash != self._appState) {
                    locationWrapper.put(hash);
                    self._appState = hash;
                    self.callback(hash);
                }
            }
        };

        implementations.iframeTimer = {
            _appState: undefined,
            _init: function() {
                var current_hash = locationWrapper.get();
                self._appState = current_hash;
                iframeWrapper.init().put(current_hash);
                self.callback(current_hash);
                setInterval(self.check, 100);
            },
            check: function() {
                var iframe_hash = iframeWrapper.get(),
                    location_hash = locationWrapper.get();

                if (location_hash != iframe_hash) {
                    if (location_hash == self._appState) {    // user used Back or Forward button
                        self._appState = iframe_hash;
                        locationWrapper.put(iframe_hash);
                        self.callback(iframe_hash); 
                    } else {                              // user loaded new bookmark
                        self._appState = location_hash;  
                        iframeWrapper.put(location_hash);
                        self.callback(location_hash);
                    }
                }
            },
            load: function(hash) {
                if(hash != self._appState) {
                    locationWrapper.put(hash);
                    iframeWrapper.put(hash);
                    self._appState = hash;
                    self.callback(hash);
                }
            }
        };

        implementations.hashchangeEvent = {
            _init: function() {
                self.callback(locationWrapper.get());
                $(window).bind('hashchange', self.check);
            },
            check: function() {
                self.callback(locationWrapper.get());
            },
            load: function(hash) {
                locationWrapper.put(hash);
            }
        };

        var self = $.extend({}, implementations.base);

        if($.browser.msie && ($.browser.version < 8 || document.documentMode < 8)) {
            self.type = 'iframeTimer';
        } else if("onhashchange" in window) {
            self.type = 'hashchangeEvent';
        } else {
            self.type = 'timer';
        }

        $.extend(self, implementations[self.type]);
        $.history = self;
    })(jQuery);

    
    excludes = ['match_part', 'repeat_region', 'repeat_unit', 'direct_repeat', 'EST_match', 'region', 'contig' ];
    includes = ['gene', 'exon', 'polypeptide', 'mRNA', 'pseudogenic_transcript', 'pseudogene', 'nucleotide_match', 'pseudogenic_exon', 'gap', 'ncRNA', 'tRNA', 'five_prime_UTR', 'three_prime_UTR', 'polypeptide_motif'];
    
		$(document).ready(function() {
			
			var loading = false;
			var base = "${base}";
			var loadedTranscriptName = "";
			
	        function changeLink(fmin,fmax) {
	            //var src = "http://www.genedb.org/web-artemis/?src=${dto.topLevelFeatureUniqueName}&base=" + (min -100) + "&bases=" + (max + 200);
	            //$("#web-artemis-link").attr("src", src);
	        }
			
			function reloadDetails(name) {
				
				console.log("reloading " + name);
				
				if (loading || name ==  null || name == loadedTranscriptName) {
					return;
				}
			    loading = true;
			    
			    //var loadingDetailsTimer = setTimeout('$("#geneDetailsLoading").show()', 2000); 
			   
			    $("#geneDetails").fadeTo("slow", 0.4).load(base + "gene/"+encodeURIComponent(name)+"?detailsOnly=true", null, function () {
			       //clearTimeout(loadingDetailsTimer);
			       loadedTranscriptName = name;
			       document.title = "Gene element "+name+" - GeneDB";
			       $("#geneDetails").stop().fadeTo("fast", 1);
			       //$("#geneDetailsLoading").hide();
			       loading = false;
			   });
			} 
			
			$('#webartemis').WebArtemis({
				source : '${dto.topLevelFeatureUniqueName}',
				start : '${dto.min-1000}',
				bases : '${dto.max-dto.min +2000}',
				showFeatureList : false,
				width : 950,
				directory : "${wa}",
				showOrganismsList : false,
				webService : '<misc:url value="/services" var="base"/>',
				draggable : false,
				mainMenu : false
			});
		    
			
			var obs = new function() {
	            this.redraw = function redraw(start, end) {
	                console.log("REDRAW DETECTED " + start + " " + end);
	            }
	            this.select = function(feature) {
	            	console.log("SELECT DETECTED " + feature);
	            	jQuery.history.load(feature);
	            }
	        };
	        
	        setTimeout(function() { 
	        	console.log("adding observer");
	        	$('#webartemis').WebArtemis('addObserver', obs);
	        }, 500);
	        

	        
			$.history.init(function(hash){
		        if(hash == "") {
		        	$("#geneDetailsLoading").hide();
		        } else {
		        	//changeLink(0, 1000);
		        	reloadDetails(hash);
		        }
		    },
		    { unescape: ",/" }); 
		    
			$("#wabutton").button({
                icons: {
                	secondary: 'ui-icon-arrow-4-diag'
                  }
                });
			
		});
		
		
		
	</script>
    
    <style>
    .wacontainer {
        position:relative;
        height:200px;
        width:800px;
        overflow:none;
    }
    
    </style>
    
</format:header>
<%-- initContextMap('${base}', '${dto.organismCommonName}', '${dto.topLevelFeatureUniqueName}', ${dto.topLevelFeatureLength}, ${dto.min}, ${dto.max}, '${dto.uniqueName}'); --%>

<format:page onLoad="">

<div style="text-align:right"> 
<button id="wabutton"  >
    <a target="web-artemis" id="web-artemis-link" href="http://www.genedb.org/web-artemis/?src=${dto.topLevelFeatureUniqueName}&base=${dto.min-100}&bases=${dto.max-dto.min +200}">${dto.uniqueName}</a>
</button>
</div>

<div class="wacontainer">
    <div id="webartemis"></div>
</div>

<div id="col-2-1">
<div id="navigatePages">
    <query:navigatePages />
</div>
<%-- Here we put those styles that contain URLs --%>
<!-- <style>
* html img#chromosomeThumbnailImage {
    position:relative;
    behavior: expression((this.runtimeStyle.behavior="none")&&(this.pngSet?this.pngSet=true:(this.nodeName == "IMG" && this.src.toLowerCase().indexOf('.png')>-1?(this.runtimeStyle.backgroundImage = "none",
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.src + "', sizingMethod='image')",
        this.src = "<misc:url value="/includes/images/transparentPixel.gif"/>"):(this.origBg = this.origBg? this.origBg :this.currentStyle.backgroundImage.toString().replace('url("','').replace('")',''),
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + this.origBg + "', sizingMethod='crop')",
        this.runtimeStyle.backgroundImage = "none")),this.pngSet=true)
    );
}

.closeButton a, .homeButton a {
    position: absolute;
    top: 0px;
    left: 0px;
    width: 16px;
    height: 16px;
    cursor: pointer;
}
.closeButton a {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) 0px 0px no-repeat;
}
.closeButton a:hover {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) 0px -16px no-repeat;
}
.homeButton a {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) -16px 0px no-repeat;
}
.homeButton a:hover {
    background: transparent url(<misc:url value="/includes/images/chr-buttons.png"/>) -16px -16px no-repeat;
}
</style> -->


<!-- Context Map -->
<%-- <div id="contextMapOuterDiv">
    <div id="contextMapTopPanel">
        <div id="contextMapThumbnailDiv"></div>
    </div>
    <div id="contextMapDiv">
        <div id="contextMapLoading">
            <img src="<misc:url value="/includes/image/loading.gif"/>" id="contextMapLoadingImage">
            Loading...
        </div>
        <div class="homeButton"><a href="#" title="Home" onclick="selectLoaded(); return false;"></a></div>
        <div id="contextMapContent" class="contextMapContent"><div class="highlighter"></div></div>
    </div>
</div>
IE6 fails if this is nested within the contextMapOuterDiv.
<div id="contextMapInfoPanel">
    <div class="closeButton"><a href="#"></a></div>
    <div id="loadDetails"><a href="#">Load details &raquo;</a></div>
    <div class="value" id="selectedGeneName"></div>
    <div class="value" id="selectedGeneProducts"></div>
</div> --%>
<%--

<div id="geneDetailsLoading" >
    <img src="<misc:url value="/includes/image/loading.gif"/>">
    Loading Gene Details...
</div>


 --%>


<br />
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>

</div>
</format:page>
