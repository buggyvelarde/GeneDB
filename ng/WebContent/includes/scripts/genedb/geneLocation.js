
$(function(){
	
	
    /**
     * @param topLevelFeatureName - the name of the top level feature (chromosome or region) as passed to the view
     * @param topLevelFeatureNameSelector - a selector for the select box that will be used to populate features with
     * @param spinnerSelector - a selector for the CallStatusSpinner, which should already be instantiated
     * @param minLocation - a selector for an input box for storing the min location number
     * @param maxLocation - a selector for an input box for storing the max location number
     * @param sliderText - a selector for the container of the input boxes to be shown and hidden
     * @param toggleText - a selector for a checkbox that will be used to show and hide inputs
     */
	$.fn.GeneLocations = function(topLevelFeatureName, topLevelFeatureNameSelector, spinnerSelector, minLocation, maxLocation, sliderText, toggleText) {
		return this.each(function() {
			
			var lengths = {};
			
			$(sliderText).hide();
			$(toggleText).click(function(e) {
				$(sliderText).slideToggle('slow');
				if (this.checked) {
					$(this).button("option", "icons", {
						primary : 'ui-icon-triangle-1-s'
					});
				} else {
					$(this).button("option", "icons", {
						primary : 'ui-icon-triangle-1-n'
					});
				}
			}).button();
			$(toggleText).button("option", "icons", { primary: 'ui-icon-triangle-1-s' });

			
			$(topLevelFeatureNameSelector).click(function (e) {
				var val = $(e.currentTarget).val();
				var length = lengths[val];
				resetSlider(1,length,1,length);
			});
			
			
		    $(this).click(function (e) {
		    	var val = $(e.currentTarget).val();
		        loadTops(val, spinnerSelector, 0,0);
		    }); 
		    
		    $(minLocation).change(function(event) {
		    	resetSlider($(minLocation).val(), $(maxLocation).val(), $("#slider-range").slider('option','min'), $("#slider-range").slider('option','max'));
		    });
		    
		    $(maxLocation).change(function(event) {
		    	resetSlider($(minLocation).val(), $(maxLocation).val(), $("#slider-range").slider('option','min'), $("#slider-range").slider('option','max'));
		    });
		    
		    
		    loadTops($(this).val(), spinnerSelector, $(minLocation).val(), $(maxLocation).val(), topLevelFeatureName);
			
			function loadTops (organism, spinnerSelector, val1, val2, topLevelFeatureName) {
				$(spinnerSelector).CallStatusSpinner("addCall");
			    
			    $.ajax({
			        url: getBaseURL() + "service/top",
			        type: 'GET',
			        dataType: 'json',
			        data: {
			            'commonName' : organism
			        },
			        success: function(response) {
			            $(topLevelFeatureNameSelector).html('');

			            lengths = {};
			            var selectedLength = 0;

			            if (jQuery.isArray(response.results.feature)) {
			            
			                for (var i in response.results.feature) {
			                	
			                    var result = response.results.feature[i];
			                    var name = result["@name"];
			                    var length = result["@length"];
			                    lengths[name] = length;
			                    
			                    var selected = '';
			                    
			                    // if no topLevelFeatureName is provided, this is what it will default to
			                    if (i == 0) {
		                            selectedLength = length;
		                        }
			                    
			                    if (topLevelFeatureName == name) {
			                        selected = ' selected ';
			                        selectedLength = length;
			                    }
			                    
			                    
			                    
			                    $(topLevelFeatureNameSelector).append('<option length="'+length+'" ' + selected + ' value='+ name + ' >' + name + ' ('+length+' residues) </option>');
			                }
			                
			            } else {
			                
			                var result = response.results.feature;
			                var name = result["@name"];
			                var length = result["@length"];
			                lengths[name] = length;
			                
			                // if there is only one, select it
			                var selected = ' selected ';
			                selectedLength = length;
			                
			                $(topLevelFeatureNameSelector).append('<option length="'+length+'" ' + selected + ' value='+ name + ' >' + name + ' ('+length+' residues) </option>');
			            }

			            $(spinnerSelector).CallStatusSpinner("removeCall");
			            
			            resetSlider(val1, val2, 1,selectedLength);
			        }
			    });

			}
			
			
			
			
			function resetSlider(val1, val2, min, max) {
				// $.log(val1,val2,min,max);
				
				val1 = parseInt(val1);
				val2 = parseInt(val2);
				min = parseInt(min);
				max = parseInt(max);
				
				// $.log(val1,val2,min,max);
				
				if (val2 < 1 || val2 <= val1 || val2 > max) {
					val2 = max;
				}
				
				if (val1 >= val2) {
					val1 = 1;
				}
				
				if (val1 < 1) {
					val1 = 1;
				}
				
				// $.log(val1,val2,min,max);
				
				$("#slider-range").slider({
					range : true,
					'min' : parseInt(min),
					'max' : parseInt(max),
					values : [parseInt(val1),parseInt(val2)],
					slide: function( event, ui ) {
						$( "#boundaries" ).text( ui.values[ 0 ] + " - " + ui.values[ 1 ] );
						$(minLocation).val(ui.values[ 0 ]);
						$(maxLocation).val(ui.values[ 1 ]);
					}
				});
				
				$(minLocation).val(val1);
				$(maxLocation).val(val2);
				$( "#boundaries" ).text( val1+ " - " + val2 );
			}
			
			
		});
	};
	
	
    
});



