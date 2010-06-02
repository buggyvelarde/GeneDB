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
    <style>
        div.ui-autocomplete-info {
            float:right;
            font-size:0.8em;
            font-style:italic;
            border : 1px solid grey;
            padding-left:1em;
            padding-right:1em;
        }
        div.ui-autocomplete-see-all {
            font-weight:bold;
            font-style:italic;
            text-align:right;
        }
    </style>
    <script type="text/javascript">

	    var prehit_default = "<div class='ui-autocomplete-info ui-state-error-text ui-corner-all'>Hits</div>";
	    var presuggest_default = "<div class='ui-autocomplete-info ui-state-error-text ui-corner-all' >Did you mean?</div>";
	    
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
                          'max' : 10                                            // the max number of hits desired for display
                      },
        	          success: function(response) {
        	              var arr = Array();
        	              if (response.results.hasOwnProperty("hits")) {
	        	              if (jQuery.isArray(response.results.hits)) {
		        	              for (var i in response.results.hits) {
			        	              var result = response.results.hits[i];
			        	              var prehit = (i == 0) ? prehit_default : ""; 
			        	              arr.push({
			        	                  "label" : prehit + '<b>' + result['@displayId'] + "</b> - " + result['@product'],
			        	                  "value" : result['@systematicId']
			        	              });
		        	              }
		        	              arr.push({                                     // add a 'See all x hits' option (same as clicking on search button)
	                                  "label" : "<div class='ui-autocomplete-see-all'>See all " + response.results["@totalHits"] + " hits...</div>",
	                                  "value" : response.results["@term"]
	                              });
	        	              } else {
	        	            	  var result = response.results.hits;
	        	            	  arr.push({
                                      "label" : prehit_default + '<b>' + result['@displayId'] + "</b> - " + result['@product'],
                                      "value" : result['@systematicId']
                                  });
	        	              }
        	              }
        	              if (response.results.hasOwnProperty("suggestions")) {
        	            	  for (var ii in response.results.suggestions) {
            	            	  var result = response.results.suggestions[ii]["@name"];
            	            	  var presuggest = (ii == 0) ? presuggest_default : ""; 
                                  arr.push({
                                      "label" : presuggest + result,
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
                delay: 400                                                     // don't want it too frequent, as some queries take time, so account for slow-ish typers
        	});
        	
          
        });
    </script>

    <jsp:doBody />
</head>



