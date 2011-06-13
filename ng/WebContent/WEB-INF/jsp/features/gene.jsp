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
    <script type="text/javascript" src="${wa}/js/featureList.js"></script>
    <script type="text/javascript" src="${wa}/js/navigate.js"></script>
    <script type="text/javascript" src="${wa}/js/genome.js"></script>
    <script type="text/javascript" src="${wa}/js/samFlag.js"></script></format:header>
    
    <script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/chromosoml.js"/>"></script>
    
    
    <style>
        
        div.wacontainer {
            position:relative;
            height:230px;
        }
        
        .chromosome_feature {
            border:0px;
        }
        
        #chromosome-map-container {
            margin-top:30px;
        }
        
        #chromosome-map {
            margin-left:25px;
        }
        
        #chromosome-map-slider-container {
            margin-top:-66px;
            height:22px;
            margin-left:25px;
        }
        
        #chromosome-map-background {
            height:22px;
            margin-top:-66px;
            background:rgb(240,240,228);
            float:left;
            width:100%;
            margin-top:-7px;
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
                needsSlider = false;
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
                axisLabels : false
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




<div style="margin-top:15px;text-align:right">
<span style="font-weight:bold;padding:10px 20px 10px 20px;" class="ui-state-default ui-corner-all"   >
 <a target="web-artemis" id="web-artemis-link">View<span style="color: rgb(139, 3, 27);"> Web Artemis </span>in a new window</a>
</span>
</div>


<div id="chromosome-map-container"  >
    <div id="chromosome-map-background"></div>
    <div id="chromosome-map" ></div> 
    <div id="chromosome-map-slider-container">
        <div id="chromosome-map-slider"   ></div>
    </div>
</div>

<br class="clear">

<div class="wacontainer">
    <div id="webartemis"></div>
</div>






<br />
<div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div>


 

</format:page>
