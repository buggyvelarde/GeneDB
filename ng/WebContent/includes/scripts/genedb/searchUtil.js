$(function(){
	
	var prehit_default = "<div class='ui-autocomplete-info ui-state-error-text ui-corner-all'>Hits</div>";
    var presuggest_default = "<div class='ui-autocomplete-info ui-state-error-text ui-corner-all' >Did you mean?</div>";
    
	$("#search input[name=searchText]").autocomplete({
	    source: function(requested, callback) {
	      $.ajax({
	          url: getBaseURL() + "service/search",
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
	            	  if (jQuery.isArray(response.results.suggestions)) {
    	            	  for (var ii in response.results.suggestions) {
        	            	  var result = response.results.suggestions[ii]["@name"];
        	            	  var presuggest = (ii == 0) ? presuggest_default : ""; 
                              arr.push({
                                  "label" : presuggest + result,
                                  "value" : result
                              });
                          }
                      } else {
                    	  var result = response.results.suggestions["@name"];
                    	  arr.push({
                              "label" : presuggest_default + result,
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