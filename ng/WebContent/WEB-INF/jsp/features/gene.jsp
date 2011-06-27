<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<misc:url value="/includes/scripts/web-artemis" var="wa"/>
<misc:url value="/" var="base"/>

<format:header title="Feature: ${dto.uniqueName}">
        
	<link rel="stylesheet" type="text/css" href="${wa}/css/superfish.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/css/tablesorter.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/js/jquery.contextMenu-1.01/jquery.contextMenu.css" media="screen">
	<link rel="stylesheet" type="text/css" href="${wa}/css/artemis.css" media="screen">
    
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
    <script type="text/javascript" src="${wa}/js/vcf.js"></script>
    <script type="text/javascript" src="${wa}/js/featureList.js"></script>
    <script type="text/javascript" src="${wa}/js/navigate.js"></script>
    <script type="text/javascript" src="${wa}/js/genome.js"></script>
    <script type="text/javascript" src="${wa}/js/samFlag.js"></script></format:header>
    
    
    <script type="text/javascript" src="${wa}/js/chromosoml.js"></script>
    
    <style>
        
        div.wacontainer {
            position:relative;
            height:230px;
            margin:0px;
            padding:0px;
            margin-top:-25px;
        }
        
        .chromosome_feature {
            border:0px;
        }
        
        #chromosome-map-container {
            margin-top:15px;
            margin-left:25px;
        }
        
        #chromosome-map-slider {
            margin-top:-26px;
            height:22px;
            z-index:100;
        }
        
        
        
    </style>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/GeneDBPageWebArtemisObserver.js"/>"></script>
    <script>
        $(document).ready(function() { 
        	
        	var topLevelFeatureLength = parseInt("${dto.topLevelFeatureLength}");
            var max = 100000;
            var needsSlider = true;
            if (max > topLevelFeatureLength) {
                max = topLevelFeatureLength;
                //needsSlider = false;
            }
            var zoomMaxRatio = max / parseInt("${dto.topLevelFeatureLength}");
        	
        	$("#chromosome-map").ChromosomeMap({
                region : "${dto.topLevelFeatureUniqueName}", 
                overideUseCanvas : false,
                bases_per_row: parseInt("${dto.topLevelFeatureLength}"),
                row_height : 10,
                row_width : 870,
                overideUseCanvas : true,
                loading_interval : 100000,
                axisLabels : false,
                row_vertical_space_sep : 10
            });
        	
        	$('#webartemis').WebArtemis({
                source : "${dto.topLevelFeatureUniqueName}",
                start : "${dto.min-1000}",
                bases : "${dto.max-dto.min +2000}",
                showFeatureList : false,
                width : 950,
                directory : "${wa}",
                showOrganismsList : false,
                webService : "/services",
                draggable : false,
                mainMenu : false, 
                zoomMaxRatio : zoomMaxRatio
            });
            
        	if (needsSlider) {
        		
        		$('#chromosome-map-slider').ChromosomeMapSlider({
                    windowWidth : 870,
                    max : parseInt("${dto.topLevelFeatureLength}"), 
                    observers : [new ChromosomeMapToWebArtemis()],
                    pos : "${dto.min-1000}",
                    width : "${dto.max-dto.min +2000}"
                });
        		
	            setTimeout(function() { 
	                $('#webartemis').WebArtemis('addObserver', new GeneDBPageWebArtemisObserver("${dto.topLevelFeatureUniqueName}", "${dto.min-1000}", "${dto.max-dto.min +2000}"));
	                $('#webartemis').WebArtemis('addObserver', new WebArtemisToChromosomeMap('#chromosome-map-slider'));
	            }, 500);
        	}
        	
        	$('.wacontainer').hover(
        		function(e) {
        			$("#web-artemis-link-container").show();        			
        		}, function(e) {
        			$("#web-artemis-link-container").hide();
        		}
        	);
        	
        	
            
    	});
    </script>
   

<format:page >

<%-- <div id="geneDetailsLoading" >
    <img src="<misc:url value="/includes/image/loading.gif"/>">
    Loading Gene Details...
</div> --%>



<div id="navigatePages">
    <query:navigatePages />
</div>







<div id="chromosome-map-container"  >
    <div id="chromosome-map" ></div>
    <div id="chromosome-map-slider" ></div> 
</div>

<div class="wacontainer">
    <div id="webartemis"></div>
    <div id="web-artemis-link-container" style="display:none;position:absolute;right:20px;top:30px;">
         <a title="View this region in a new Web-Artemis window" target="web-artemis" id="web-artemis-link"><span style="color: rgb(139, 3, 27);"><img src="<misc:url value="/includes/image/popup-web-artemis.png"/>" border="0"></span></a>
    </div>
</div>



<div id="col-2-1">


<br />
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>

</div>
 

</format:page>
