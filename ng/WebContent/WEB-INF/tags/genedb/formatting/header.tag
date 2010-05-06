<%@ tag display-name="header" body-content="scriptless" %>
<%@ attribute name="name"%>
<%@ attribute name="organism"%>
<%@ attribute name="title"  required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>${title} - GeneDB</title>

    <script type="text/javascript" src="http://js.sanger.ac.uk/urchin.js"></script>

    <link rel="stylesheet" href="<misc:url value="/includes/style/genedb/main.css"/>" type="text/css" />
    <link href="<misc:url value="/includes/scripts/jquery/jquery-ui-1.8.1.custom/css/smoothness/jquery-ui-1.8.1.custom.css"/>" rel="stylesheet" type="text/css"/>
    
    <script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined-new.js"/>"></script>
    
    <script type="text/javascript"><!--
	    function resultAppend(arr, result) {
	        maxProductLength = 40;
	        product = result['@product'];
	        if (result['@product'].length > maxProductLength) {
	              product = product.substring(0,maxProductLength - 3) + "...";
	        }
	        arr.push({
	            "label" : '<b>' + result['@displayId'] + "</b> - " + product,
	            "value" : result['@systematicId']
	        });
	    }
    
        $(function(){
          $("#nav > li")
            .mouseover(function(){$(this).addClass("over");})
            .mouseout (function(){$(this).removeClass("over");});
          
          $("#search input.search-box").autocomplete({
        	    source: function(requested, callback) {
        	      $.ajax({
        	          url: "<misc:url value="/service/search"/>",
        	          type: 'GET',
        	          dataType: 'json',
        	          data: {
                          'term' : requested.term,                          // the requested parameter sent to this function is an object with only one property: "term"
                          'taxon' : $('#search select[name=taxons]').val(), // find the current organism
                          'max' : 30                                        // the max number of hits desired
                      },
        	          success: function(response) {
        	              arr = Array();
        	              if (response.results.hasOwnProperty("search")) { 
	        	              if (jQuery.isArray(response.results.search)) {
		        	              for (i in response.results.search) {
		        	                  result = response.results.search[i];
		        	                  resultAppend(arr, result);
		        	              }
	        	              } else {
	        	            	  result = response.results.search;
	        	            	  resultAppend(arr, result);
	        	              }
        	              }
        	              callback(arr);
        	          }
        	      });
        	      
        	    }
        	});
        	
          
        });
    </script>

    <jsp:doBody />
</head>



