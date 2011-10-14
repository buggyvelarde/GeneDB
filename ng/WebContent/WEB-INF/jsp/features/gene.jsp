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

    <script type="text/javascript" src="${wa}/js/chromosoml/lib/jquery.tmpl.js"></script>
    <script type='text/javascript' src='${wa}/js/chromosoml/lib/knockout.js'></script>
    <script type='text/javascript' src='${wa}/js/chromosoml/lib/koExternalTemplateEngine.js'></script>
    <script type='text/javascript' src='${wa}/js/chromosoml/lib/jquery.history.js'></script>
    
    
    <script type="text/javascript" src="${wa}/js/chromosoml/chromosoml.js"></script>
    <script type="text/javascript" src="${wa}/js/chromosoml/gene_page.js"></script>

    
    <script>
    
    function addToBasket(geneid){
      $.get("<misc:url value="/Basket/"/>"+geneid, {}, function(content){
        $("#basketbutton")
            .attr('src', "<misc:url value="/includes/image/button-added-to-basket.gif" />")
            .unbind('click')
            .css({'cursor': 'default'});
      });
    }
    
    $(document).ready(function() {
        
    	// all spinners here will use the same defaults
        $.fn.CallStatusSpinner.defaults = {
                height : 11.5,
                width : 50,
                img : '<misc:url value="/includes/image/spinner.gif"/>'
        };
        
        $('.spinner').CallStatusSpinner();
        
        //new GenePage("${uniqueName}", "${wa}");
        var genePage = new wa.GenePage({
            uniqueName :"${uniqueName}",
            webArtemisPath : "${wa}",
            baseLinkURL : getBaseURL()
        });
        
    });
    </script>


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

</format:header>



<format:page >


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
	
	<div style="width:200px;margin:auto;text-align:center;" class="spinner" ></div>
	
	
	<div class="gene_page" style="float:left;" id="col-2-1" data-bind="template: 'gene_page' " ></div>
 

</format:page>
