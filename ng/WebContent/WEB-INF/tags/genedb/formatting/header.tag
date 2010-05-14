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
    <link rel="stylesheet" href="<misc:url value="/includes/scripts/jquery/jquery-ui-1.8.1.custom/css/smoothness/jquery-ui-1.8.1.custom.css"/>" type="text/css"/>
    
    <script type="text/javascript" src="<misc:url value="/includes/scripts/jquery/jquery-genePage-combined-new.js"/>"></script>
    
    <script type="text/javascript"><!--

        function resultAppend(arr, result, maxProductLength) {                  // appends results returned from a quick search service to an array
            if (maxProductLength == null) {
                maxProductLength = 40;
            }
	        product = result['@product'];
	        if (product.length > maxProductLength) {
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

          $("#search input[name=searchText]").autocomplete({
        	    source: function(requested, callback) {
        	      $.ajax({
        	          url: "<misc:url value="/service/search"/>",
        	          type: 'GET',
        	          dataType: 'json',
        	          data: {
                          'term' : requested.term,                              // the requested parameter sent to this function is an object with only one property: "term"
                          'taxon' : $('#search select[name=taxons]').val(),     // find the current organism
                          'max' : 30                                            // the max number of hits desired for display
                      },
        	          success: function(response) {
        	              arr = Array();
        	              if (response.results.hasOwnProperty("hits")) {
	        	              if (jQuery.isArray(response.results.hits)) {
	        	            	  arr.push({                                     // add a see all results option (same as clicking on search button)
		        	            	  "label" : "<i>See all " + response.results["@totalSize"] + " hits...</i>",
		        	            	  "value" : response.results["@term"]
	        	            	  });
		        	              for (i in response.results.hits) {
		        	                  resultAppend(arr, response.results.hits[i]);
		        	              }
	        	              } else {
	        	            	  resultAppend(arr, response.results.hits);    // we only have one result
	        	              }
        	              }
        	              if (response.results.hasOwnProperty("suggestions")) {
        	            	  for (i in response.results.suggestions) {
            	            	  result = response.results.suggestions[i]["@name"];
                                  arr.push({
                                      "label" : result,
                                      "value" : result
                                  });
                              }
        	              }
        	              callback(arr);
        	          }
        	      });
        	    },
                select: function(event, ui) {
                     $("#search input[name=searchText]").val(ui.item.value);    // make sure the form is updated before submitting
                     $('#search form[name=quicksearch]').submit();
                },
                delay: 600                                                      // don't want it too frequent, as some queries take time, so account for slow-ish typers
        	});
        	
          
        });
    </script>

    <jsp:doBody />
</head>



