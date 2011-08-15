<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<misc:url value="/includes/scripts/web-artemis" var="wa"/>
<misc:url value="/" var="base"/>

<format:header title="Feature: ${uniqueName}" >
        
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
    <script type="text/javascript" src="${wa}/js/jquery-ajax-queue_1.0.js"></script>

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
        
        .hiddenRow {
            display:none;
        }
        
        
    </style>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/GeneDBPageWebArtemisObserver.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/includes/scripts/genedb/GenePage.js"/>"></script>
    <script>
    $(document).ready(function() {
    	genePage("${uniqueName}", "${wa}");
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
<%-- <div id="geneDetails">
    <jsp:include page="geneDetails.jsp"/>
</div> --%>



<h2 style="padding-top:0px;margin-top:0px;">General Information</h2>
<div id="col-4-1">

<div class="main-grey-3-4-top"></div>
<div class="light-grey">
<span class="float-right grey-text"><misc:displayDate time="${dto.lastModified}" message="Last Modified" /></span>
<h2>Summary</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">


<tr id="geneNameRow" class="hiddenRow">
  <th>Gene Name</th><td id="geneNameField"></td>
</tr>

<tr >
  <th>Systematic Name</th>
  <td id="systematicName"></td> 
</tr>


<tr><th>Feature Type</th><td id="featureType"></td></tr>


<tr id="productRow"><th>Product</th><td id="productField"></td></tr>

<tr id="previousSystematicRow" class="hiddenRow"><th>Previous Systematic Id</th><td id="previousSystematicField"></td></tr>
<tr id="synonymRow" class="hiddenRow"><th>Previous Systematic Id</th><td id="synonymField"></td></tr>
<tr id="productSynonymRow" class="hiddenRow"><th>Product Synonym Id</th><td id="productSynonymField"></td></tr>
<tr id="regionRow" ><th>Location</th><td id="regionField"></td></tr>
<tr id="dbxrefRow" ><th>See Also</th><td id="dbxrefField"></td></tr>

</table>



</div>
<div class="main-grey-3-4-bot"></div>
</div><!-- end internal column -left -->

</div>
 

</format:page>
