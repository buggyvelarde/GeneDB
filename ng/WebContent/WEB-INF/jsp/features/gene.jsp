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
    <script type="text/javascript" src="${wa}/js/samFlag.js"></script>





</format:header>
    
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/jquery.tmpl.js"></script>
    <script type='text/javascript' src='${wa}/js/chromosoml/lib/knockout.js'></script>
    <script type='text/javascript' src='${wa}/js/chromosoml/lib/koExternalTemplateEngine.js'></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/jquery.tools.min.js"></script>
    
    <!-- <script type="text/javascript" src="${wa}/js/chromosoml/lib/spine.js"></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/spine.local.js"></script> -->
    
    
    <!-- <script type="text/javascript" src="${wa}/js/chromosoml/lib/jquery.tools.min.js"></script>
    
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/json2.js"></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/underscore.js"></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/lib/backbone.js"></script> -->
    
    
    
    
    <script type="text/javascript" src="${wa}/js/chromosoml/chromosoml.js"></script>
    
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
        
        .hideable {
            display:none;
        }
        
        .tooltip {
	        display:none;
	        background-color:#ffa;
	        border:1px solid #cc9;
	        padding:3px;
	        font-size:13px;
	        -moz-box-shadow: 2px 2px 11px #666;
	        -webkit-box-shadow: 2px 2px 11px #666;
	    }
	   
	    a.evidence {
	       text-decoration:none;
	       font-weight:bold;
	       color:#555;
	    }
	        
        
    </style>
    
    
    <!-- <script type="text/javascript" src="${wa}/js/chromosoml/gene_page_default_templates.js"></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/gene_page_default_models_and_views.js"></script> -->
    <script type="text/javascript" src="${wa}/js/chromosoml/gene_page.js"></script>
    
    <script>
    $(document).ready(function() {
    	//new GenePage("${uniqueName}", "${wa}");
    	var genePage = new wa.GenePage({
    		uniqueName :"${uniqueName}",
    		webArtemisPath : "${wa}",
    		baseLinkURL : getBaseURL()
    	});
    	
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



<div id="col-2-1" data-bind="template: 'gene_page' " ></div>


<!-- 

<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">

	<tr id="geneNameRow" class="hideable">
		<th>Gene Name</th>
		<td class="erasable" id="geneNameField"></td>
	</tr>
	<tr>
		<th>Systematic Name</th>
		<td class="erasable" id="systematicName"></td>
	</tr>
	<tr>
		<th>Feature Type</th>
		<td class="erasable" id="featureType"></td>
	</tr>
	<tr id="productRow" class="hideable">
		<th>Product</th>
		<td class="erasable" id="productField"></td>
	</tr>
	<tr id="previousSystematicRow" class="hideable">
		<th>Previous Systematic Id</th>
		<td class="erasable" id="previousSystematicField"></td>
	</tr>
	<tr id="synonymRow" class="hideable">
		<th>Previous Systematic Id</th>
		<td class="erasable" id="synonymField"></td>
	</tr>
	<tr id="productSynonymRow" class="hideable">
		<th>Product Synonym Id</th>
		<td class="erasable" id="productSynonymField"></td>
	</tr>
	<tr id="regionRow">
		<th>Location</th>
		<td class="erasable" id="regionField"></td>
	</tr>
	<tr id="dbxrefRow" class="hideable">
		<th>See Also</th>
		<td class="erasable" id="dbxrefField"></td>
	</tr>
	<tr id="plasmodbRow" class="hideable">
		<th>PlasmoDB</th>
		<td class="erasable" id="plasmodbField"></td>
	</tr>
	<tr id="tritrypdbRow" class="hideable">
		<th>TriTrypDB</th>
		<td class="erasable" id="tritrypdbField"></td>
	</tr>

</table>
 -->

<%-- 
<div class="hideable" id="comments" >
<format:genePageSection>
  <h2>Comments</h2>
  
  <div class="hideable" id="notesRow">Notes<ul class="erasable" id="notesField"></ul></div>
  <div class="hideable" id="commentsRow">Comments<ul class="erasable" id="commentsField"></ul></div>
  <div class="hideable" id="curationRow">Curation<ul class="erasable" id="curationField"></ul></div>
  <div class="hideable" id="publicationsRow">Key information on this gene is available from <span class="erasable" id="publicationsField"></span></div>
  
</format:genePageSection>
  </div>

<br class="clear" /><br />

<div class="hideable" id="controlledCurationRow">
<format:genePageSection id="controlCur">
  <h2>Phenotype</h2>
<div class="erasable" id="controlledcurationField"></div>
</format:genePageSection>
</div>

<br class="clear" /><br />

<div class="hideable" id="go">
<format:genePageSection >
<h2>Gene Ontology</h2>
<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">
	<tr class="hideable" id="biologicalProcessRow">
	  <th>Biological Process</th>
	  <td id="biologicalProcessField" class="erasable term"></td>
	</tr>
	<tr class="hideable" id="molecularFunctionRow">
      <th>Molecular Function</th>
      <td id="molecularFunctionField" class="erasable term"></td>
    </tr>
    <tr class="hideable" id="cellularComponentRow">
      <th>Cellular Component</th>
      <td id="cellularComponentField" class="erasable term"></td>
    </tr>
</table>
</format:genePageSection>
</div> --%>




 

</format:page>
