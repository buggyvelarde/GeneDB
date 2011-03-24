/**
 * jQuery Log
 * Fast & safe logging in Firebug console
 * 
 * @param mixed - as many parameters as needed
 * @return void
 * 
 * @url http://plugins.jquery.com/project/jQueryLog
 * @author Amal Samally [amal.samally(at)gmail.com]
 * @version 1.0
 * @example:
 * 		$.log(someObj, someVar);
 * 		$.log("%s is %d years old.", "Bob", 42);
 * 		$('div.someClass').log().hide();
 */
(function(a){a.log=function(){if(window.console&&window.console.log){console.log.apply(window.console,arguments)}};a.fn.log=function(){a.log(this);return this}})(jQuery);

if(!String.prototype.startsWith){
    String.prototype.startsWith = function (str) { return !this.indexOf(str); };
    String.prototype.contains = function(it) { return this.indexOf(it) != -1; };
}




/*
 * A simple rendering of a chromosome's principle features, replicating the maps available on the classic
 * genedb site. Some attempt has been made to do a canvas version, but it has several problems that need
 * to be adressed, namely :
 * 
 * - in Firefox there is an upper limit to the size of height of the canvas, which if exceeded nothing is rendered
 * - you can't currently draw text in canvas, which means using overlayed divs, which essentially means you still
 * 		have to draw the same number of text-divs for every feature, which means the advantage of using canvas
 * 		in this instance is questionable
 * - if the Canvas version is to be complete, clicking would have to be handled differently (not a problem, just
 * 		not worth doing if the above two issues aren't resolved first. 
 * 
 * Therefore, for the time being the canvas option should be treated as essentially broken, and the div-version 
 * should be used instead. 
 * 
 * Depends on the web services, and also on Modernizr.js (to detect canvas ability).
 * 
 * @author gv1
 * 
 * */
(function($) {
	
	// lifted from web-artemis!
	var colours = [ 
       '255,255,255',
       '100,100,100',
       '255,0,0',
       '0,255,0',
       '0,0,255',
       '0,255,255',
       '255,0,255',
       '245,245,0',
       '152,251,152',
       '135,206,250',
       '255,165,0',
       '200,150,100',
       '255,200,200',
       '170,170,170',
       '0,0,0',
       '255,63,63',
       '255,127,127',
       '255,191,191'
   ];
	
	var settings = {
		'region' : 'Pf3D7_01',
		'exclude' : 'dinucleotide_repeat_microsatellite_feature,PCR_product,match_part,contig,EST_match,direct_repeat,gap,nucleotide_match,repeat_region,region,repeat_unit,polypeptide,mRNA,exon,polypeptide_motif,pseudogenic_exon,ncRNA,pseudogenic_transcript,three_prime_UTR,five_prime_UTR,repeat',
		'bases_per_row' : 10000,
		'row_height' : 50,
		'row_width' : 800,
		'row_vertical_space_sep' : 50,
		'loading_interval' : 10000000,
		'mouseoverColor' : "rgb(34,145,220)",
		'mouseoutColor' : "rgb(168,202,235)",
		'mouseoverFontColor' : "rgb(50,50,50)",
		'mouseoutFontColor' : "rgb(0,0,0)",
		'overideUseCanvas' : false,
		'click' : function(event) {
			$.log(event);
		},
		'base_position_start' : 0,
		'web_service_root' : "/services/"
	};
	
	
	var self = null;
	var chromosomeCanvas = null;
	var chromosomeDiv = null;
	
	var aleadyLoaded = new Object();
	
	var useCanvas = false;
	var ctx = null;
	var length = 0;
	
	var base_position_start = 0;
	
	
	
	function getGridCoordinate(base_position) {
		
		var subtracted_position = base_position - base_position_start;
		
		var scaled_position = (subtracted_position / settings['bases_per_row']);
		var row_number = Math.floor(scaled_position);
		var x_position_in_bp = subtracted_position - (row_number * settings['bases_per_row']);
		var absolute_x_position = x_position_in_bp * ( settings.row_width / settings.bases_per_row );
		var absolute_y_position = (settings['row_vertical_space_sep'] + settings["row_height"]) * row_number;
		
		return {
			"x" : absolute_x_position,
			"y" : absolute_y_position, 
			"row_number" : row_number
		};
	}
	
	function append(x,y,w,h,color,label,text) {
		
		if (useCanvas) {
			ctx.fillStyle = color;
			ctx.fillRect(x,y,w,h);
			
			return "<div title='"+label +"' class='chromosome_feature "+label+"' style='position:absolute;left:" + x + "px;top:" + y + "px;height:"+ h +"px;'>"+text+"</div>";
		} else {
			
			var html = "<div title='"+label +"' class='chromosome_feature "+label+"' style='border:1px solid #bbb;background-color:"+color+";position:absolute;left:" + x + "px;top:" + y + "px;height:"+ h +"px;width:"+ w +"px;'>";
			html += "<div class='chromosome_feature_text' >"+text+"</div>";
			html += "</div>";
			
			return html;
		}
	}
	
	function drawGraphLines(x,y,w,h,color,text) {
		
		var axisHtml = "";
		
		if (useCanvas) {
			ctx.strokeRect(x,y,w,h);
		} else {
			axisHtml += "<hr class='chromosome_axis' style='border:1px solid grey;position:absolute;left:" + x + "px;top:" + y + "px;width:"+ w +"px;'></hr>";
		}
		
		axisHtml += "<div class='chromosome_axis_label' style='z-index:100000000;position:absolute;left:" + (x + 5) + "px;top:" + (y) + "px;'>"+text+"</div>";
		return axisHtml;
		
	}
	
	function escapeClassName(className) {
		//return className.replace(":", "_");
		// regex lifted from web-artemis
		return className.replace(/(:|\.|\|)/g,'\\$1');
	}
	
	function loadSequence(options, success) {
		$.ajax({
	        url: settings.web_service_root + "regions/sequence.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'region' : settings.region,
	            'metadata_only' : true
	        },
	        success: success
		});
	}
	
	
	
	function drawAxes(length, oncomplete) {
		
		
		
		var yscale = getYScale();
		
		var row_num = Math.ceil (length / settings.bases_per_row);
		
		var height = (settings.row_height + settings.row_vertical_space_sep ) * row_num;
		
		$(self).css("height", height);
		
		
		if (useCanvas) {
			
			// might need to set a max for firefox
			// var max_canvas_height = 1000;
			// var canvas_height = (height > max_canvas_height) ? max_canvas_height : height;
			chromosomeCanvas.height = height;
			chromosomeCanvas.width = settings.row_width;
	    	
		}
		
		var row =0;
		
		var max_base_pair = base_position_start + settings.loading_interval ;
		
		if (max_base_pair > length) {
			max_base_pair = length;
		}
		
		var buffer = "";
		for (var base_position = base_position_start; base_position <= max_base_pair; base_position += settings.bases_per_row) {
			row_y = (settings['row_vertical_space_sep'] + settings["row_height"]) * row / yscale;
			row_y += settings["row_height"] / 2;
			buffer += drawGraphLines(0,row_y,settings.row_width, 0, "", base_position);
			row++;
		}
		
		chromosomeDiv.append(buffer);
		
		
		oncomplete(length);
	}
	
	function loadInChunks(length) {
		
		loadChunk(base_position_start, base_position_start + settings.loading_interval);
		
	}
	
	// currently set to always return 1
	function getYScale() {
		return 1;
	}
	
	// currently set to always return 1
	function getXScale() {
		//return settings.bases_per_row / settings.row_width ;
		return 1;
	}
	
	function loadChunk(start, end) {
		$.ajax({
	        url: settings.web_service_root + "regions/locations.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'region' : settings.region, 
	            'exclude' : settings.exclude,
	            'start' : start,
	            'end' : end
	        },
	        success: function(returned) {
	        	
	        	var base_position_end = base_position_start + settings.loading_interval;
	        	
	        	var yscale = getXScale() ;
	        	var xscale = getYScale() ;
	        	
	        	var minimum_possible_coordinate = getGridCoordinate(base_position_start).y / yscale;
	        	var maximum_possible_coordinate= getGridCoordinate(base_position_end).y / yscale;
	        	
	        	//log([minimum_possible_coordinate,maximum_possible_coordinate]);
	        	
	        	//log([this,settings,base_position_end , base_position_start , settings.loading_interval]);
	        	
                
                var h = settings.row_height / 2 / yscale ;
	        	
	        	self.each(function() {
	        		
	        		var buffer = '';
	        		var max_height = 0;
	                
	        		$.each(returned.response.results.locations, function(index, feature) {
	        			
	        			
	        			var actual_height  = 0;
	        			
	        			var coordsFmin = getGridCoordinate(feature.fmin);
	        			var coordsFmax = getGridCoordinate(feature.fmax);
	        			
	        			//log([feature.uniqueName, coordsFmin, coordsFmax]);
	        			
	        			var color = settings.mouseoutColor;
	        			
	        			if (aleadyLoaded[feature.uniqueName]) {
	        				return;
        				}
	        			
	        			aleadyLoaded[feature.uniqueName] = 1;
	        			
	        			var text = feature.uniqueName ;
	        			
	        			if (coordsFmin.row_number != coordsFmax.row_number) {
	        				
	        				var starting_row = coordsFmin.row_number;
	        				var ending_row = coordsFmax.row_number;
	        				
	        				for (r = starting_row; r <= ending_row; r++) {
	        					
	        					var r_x = null;
	        					var r_y = null;
	        					
	        					r_w = settings.row_width;
	        					
	        					
	        					if (r == starting_row) {
	        						
	        						r_x = coordsFmin.x / xscale;
	        						r_y = coordsFmin.y / yscale;
	        						
	        						
	        						r_w = settings.row_width - (coordsFmin.x / xscale);
	        						
	        					} else if (r == ending_row) {
	        						
	        						
	        						r_x = 0;
	        						r_y = coordsFmax.y / yscale;
	        						
	        						
	        						var row_y_start = (settings['row_vertical_space_sep'] + settings["row_height"]) * r / yscale;
	        						
	        						r_w = (coordsFmax.x - row_y_start) / yscale;
	        						
	        					} else {
	        						
	        						r_y = (settings['row_vertical_space_sep'] + settings["row_height"]) * r / yscale;
	        					}
	        					
	        					//log([feature.uniqueName, minimum_possible_coordinate, r_y, maximum_possible_coordinate]);
	        					
	        					if (r_y > maximum_possible_coordinate) {
	        						continue;
	        					}
	        					
	        					if (r_y < minimum_possible_coordinate) {
        							continue;
        						}
	        					
	        					if (feature.strand == -1) {
			        				r_y += settings.row_height / 2;
			        			}
	        					
	        					buffer += append(r_x,r_y,r_w,h,color,feature.uniqueName,text);
	        					text = '';
	        					
	        					actual_height = r_y + h;
			        			
	        				}
	        				
	        			} else {
	        				
	        				if (feature.fmin >= base_position_start) {
	        					
	        					//log([feature.uniqueName, feature.strand, feature.fmin, base_position_start]);
    							
	        					var x = coordsFmin.x / xscale; 
			        			var y = coordsFmin.y /yscale ; 
			        			var w = (coordsFmax.x - coordsFmin.x) / yscale ; 
			        			
			        			
			        			if (feature.strand == -1) {
			        				y += settings.row_height / 2;
			        			}
			        			
			        			buffer += append(x,y,w,h,color,feature.uniqueName, text);
			        			actual_height = y + h;
			        			
    						}
	        				
	        				
		        			
	        			}
	        			
	        			if (actual_height > max_height) {
	        				max_height = actual_height;
	        			}
	        			
		    		});
	        		
	        		
	        		chromosomeDiv.append(buffer);
	        		
	        		// $('chromosome_feature').fadeTo(500,0.5);
	        		
	        		loadColors();
	        		
	        	});
	        	
	        	
	        	
	        }
	    });
	}
	
	function loadColors() {
		
		var bin = [];
		var n = 0;
		var max = 20;
		
		$.each (aleadyLoaded, function(featureNameAsIndex, number) {
			
			
			if (n > max) {
				n = 0;
				loadColorSet(bin);
				bin = [];
			}
			
			bin.push(featureNameAsIndex, featureNameAsIndex + ":pep");
			
			n++;
			
		});
		
		if (bin.length > 0) {
			loadColorSet(bin);
		}
		
	}
	
	function loadColorSet(bin) {
		$.ajax({
			traditional : true, // we're sending non string delimited arrays here, and we don't want [] either.
	        url: settings.web_service_root + "features/properties.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'features' : bin,
	            'types' : "colour"
	        },
	        success: function(returned) {
	        	
	        	if (! returned.response) {
	        		return;
	        	}
	        	
	        	$.each(returned.response.results.features, function(index, feature) {
	        		
	        		var uniqueName = feature.uniqueName;
	        		if (feature.uniqueName.contains(":pep")) {
	        			uniqueName = feature.uniqueName.replace("\:pep", "");
	        		} else if (feature.uniqueName.contains(":mRNA")) {
	        			uniqueName = feature.uniqueName.replace("\:mRNA", "");
	        		}
	        		
	        		var elements = $('.' + escapeClassName(uniqueName));
	        		
	        		if (elements.length > 0) {
	        			
	        			$.each(feature.properties, function(index, prop) {
		        			if (prop.name == "colour") {
		        				var colour = "rgb(" + colours[prop.value] + ")";
		        				// log([uniqueName , element, colour]);
		        				elements.css('backgroundColor', colour);
		        				coloured = true;
		        				return;
		        			}
		        		});
	        			
	        			//elements.css('opacity', 0.5);
	        		}
	        	});
	        	
	        }
		});
	}
	
	function reset() {
		
		$(self).html('');
		
		if (Modernizr.canvas && !settings.overideUseCanvas) {
			
	    	useCanvas = true;
	    	
	    	$(self).append("<canvas class='chromosome_canvas' style='position:absolute;' ></canvas>");
	    	chromosomeCanvas = $(self).children('.chromosome_canvas')[0];
	    	ctx = chromosomeCanvas.getContext("2d");
	    	
	    }
	    
	    // whether or not we are using canvas, we still need a div for text
	    $(self).append("<div class='chromosome_div' style='position:relative;'></div>");
	    chromosomeDiv = $($(self).children('.chromosome_div')[0]);
	    
	    aleadyLoaded = new Object();
	}
	
	var methods = {
		next : function() {
			return this.each(function() {
				reset();
				base_position_start = base_position_start + settings.loading_interval;
				
				drawAxes(length, loadInChunks);
			});
		},
		previous : function() {
			return this.each(function() {
				reset();
				base_position_start = base_position_start - settings.loading_interval;
				if (base_position_start < 0) {
					base_position_start = 0;
				}
				drawAxes(length, loadInChunks);
			});
		},
		init : function( options ) {
			
			if ( options ) { 
		       $.extend( settings, options );
		    }
		    
			self.base_position_start = settings.base_position_start;
			
		    $(self).click(function(event)  {
        		settings.click(event);
        	}).mouseover(function(event) {
                
        		var target = $(event.target);
        		
        		if (target.hasClass('chromosome_feature')) {
        			var title = target.attr('title');
                    $('.' + escapeClassName(title))
                    	.css('borderColor', settings.mouseoverColor)
                    	//.css('color', settings.mouseoverFontColor)
                    	.stop().fadeTo(500,1);
                    //log($('.' + escapeClassName(title)));
                    return false;
        		}
        		
        		
                
            }).mouseout(function(event) {
            	
            	var target = $(event.target);
        		
        		if (target.hasClass('chromosome_feature')) {
        			var title = target.attr('title');
                    $('.' + escapeClassName(title))
                    	.css('borderColor', settings.mouseoutColor)
                    	//.css('color', settings.mouseoutFontColor)
                    	.stop().fadeTo(1000,0.5);
                    return false;
        		}
            	
            });
		    
		    reset();
		    
			return this.each(function() {
				
				loadSequence(settings, function(returned) {
					length = returned.response.results.sequences[0].length;
					if (settings.bases_per_row >= length) {
						settings.bases_per_row = length * 5;
					}
					drawAxes(length, loadInChunks);
				});
				
			});
		}
	};
	
	$.fn.ChromosomeMap = function(method) {
		self = this;
		
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.tooltip');
		}
	};
	
})(jQuery);

	
/**
 * 
 * A simple chromosome picker. Uses buttons.
 * @author gv1
 * 
 */
(function($) {
	
	var settings = {
		'web_service_root' : "/services/",
		"organism" : "com:Pfalciparum",
		"initial_offset" : 0,
		"initial_limit" : 5,
		"on_select" : function(region) {
			$.log(region);
		},
		"spinner" : null
	};
	
	var types = [];
	
	var region_types_container = null;
	var regions_container = null;
	var offset =0;
	var limit = 0;
	var self = null;
	
	$.fn.ChromosomePicker = function(method) {
		self = this;
		
		$(self).append("<div class='region_types'></div>");
		$(self).append("<div class='regions'></div>");
		
		region_types_container = $($(self).children('.region_types')[0]);
		regions_container = $($(self).children('.regions')[0]);
		
		region_types_container.addClass('ui-widget-header').addClass('fg-toolbar');
		
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.tooltip');
		}
	};
	
	var methods = {
		init : function( options ) {
			if ( options ) { 
		       $.extend( settings, options );
		    }
			$.log (options);
			$.log(settings);
			$.log (settings.spinner);
			loadRegionTypes();
		}
	};
	
	function addCall() {
		$.log (settings.spinner);
		if (settings.spinner) {
			settings.spinner.addCall();
		}
	}
	
	function removeCall() {
		if (settings.spinner) {
			settings.spinner.removeCall();
		}
	}
	
	function loadRegionTypes() {
		regions_container.html('');
		region_types_container.html('');
		addCall();
		$.ajax({
	        url: settings.web_service_root + "regions/typesinorganism.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'organism' : settings.organism
	        },
	        success: function(returned) {
	        	$.each(returned.response.results.regions, function(index, region) {
	        		var type = region.type.name;
	        		region_types_container.append("<button class='fg-button ui-state-default region_type' region_type='" + type + "' >" + type + "</button>");
	        	});
				onLoadTypes();
	        }
		});
	}
	
	function loadRegions(type, limit, offset, handler) {
		addCall();
		var data = {
            'organism' : settings.organism
        };
		
		if (type != null) {
			data["type"] = type;
		}
		if (limit != null) {
			data["limit"] = limit;
		}
		if (offset != null) {
			data["offset"] = offset;
		}
		
		$.ajax({
	        url: settings.web_service_root + "regions/inorganism.json",
	        type: 'GET',
	        dataType: 'json',
	        data: data,
	        success: function(returned) {
	        	var regions = [];
	        	$.each(returned.response.results.regions, function(index, region) {
	        		regions.push(region.uniqueName);
	        	});
	        	handler(regions);
	        }
		});

	}
	
	function onLoadTypes() {
		removeCall();
		$('.region_type').click(function(event) {
			var type = $(this).attr("region_type");
			
			offset = settings.initial_offset;
			limit = settings.initial_limit;
			
			loadRegions(type, limit, offset, function(regions) {
				//log(regions);
				
				region_types_container.html('');
				regions_container.html('');
				
				regions_container.append("<button class='ui-state-default fg-button region back' >Back</button>");
				
				fillRegions(regions);
				//
				
				regions_container.append("<button class='ui-state-default fg-button region more' >More</button>");
				
				onLoadRegions(type);
				
				
			});
		}).button({
		  icons: {
			    primary: 'ui-icon-gear',
			    secondary: "ui-icon-triangle-1-s"
			  }
			});
	}
	
	
	
	function onLoadRegions(type) {
		removeCall();
		$('button.region').click(function(event) {
			//log($(event.target));
			//log($(event.currentTarget));
			//log($(this));
			
			var region = $(event.currentTarget).attr("region");
			
			if (region != null) {
				settings.on_select(region);
			} else if ($(event.currentTarget).hasClass('more')){
				
				//offset = offset + limit;
				limit = limit * 10;
				
				loadRegions(type, limit, offset, function(regions) {
					//log(regions);
					regions_container.html('');
					regions_container.append("<button class='ui-state-default fg-button region back' >Back</button>");
					fillRegions(regions);
					regions_container.append("<button class='ui-state-default fg-button region more' >More</button>");
					onLoadRegions(type);
				});
			} else if ($(event.currentTarget).hasClass('back')){
				loadRegionTypes();
			}
		}).button({
			  icons: {
				    primary: 'ui-icon-document',
				    secondary: 'ui-icon-triangle-1-e'
				  }
				});
		$('button.back').button({
		  icons: {
			    primary: 'ui-icon-triangle-1-w'
			  }
			});
		$('button.more').button({
		  icons: {
			    primary: 'ui-icon-triangle-1-s'
			  }
			});
	}
	
	function fillRegions(regions) {
		
		$.each(regions, function(index, region) {
			regions_container
				.append("<button class='fg-button ui-state-default region' region='"+region+"' >" + region + "</button>");
		});
	}
	
	

	
})(jQuery);


(function($) {
	
	var settings = {
		'web_service_root' : "/services/",
		"organism" : "com:Pfalciparum", 
		"date" : "2011-01-01", 
		"defaultDateOffset" : 120, 
		"baseHREF" : "/",
		"spinner" : null
	};
	
	var self = null;
	
	var methods = {
		init : function(options) {
			if ( options ) { 
		       $.extend( settings, options );
		    }
				
			loadStatistics(settings.organism, getDefaultDate(), onLoadStatistics);
			
			return this.each(function() {
				$(this).html('<div class="activities" style="display:none;" class="ui-state-default ui-corner-all">' +
					'<div class="datepickercontainer" style="text-align:right">Change date: <input type="text" class="annotation_change_datepicker" ><br><span  class="spinner1" >&nbsp;&nbsp;&nbsp;</span></div>' +
					'<div style="font-size:small;" class="readableActivity"></div>' +
					'</div>');
				
				var readableActivity = $(this).find('div.readableActivity')[0];
				var activities = $(this).find('.activities')[0];
				var annotation_change_datepicker = $(activities).find('input.annotation_change_datepicker')[0];
				
				// we need references to these elements later as
				// the dialog will be moved out of this element
				$(this).data({
					readableActivity : readableActivity,
					activities : activities,
					annotation_change_datepicker : annotation_change_datepicker
				});
				
				$.log(this);
				$.log($(this).data());
			});
			
			
		}
	};
	
	$.fn.AnnotationModificationReporter = function(method) {
		self = this;
		
		
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.tooltip');
		}
	};
	
	function loadStatistics(organism, date, handler) {
		if (settings.spinner) {
			settings.spinner.addCall();
		}
		$.ajax({
	        url: settings.web_service_root + "features/annotation_changes_statistics.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'organism' : settings.organism,
	            'date' : getDateString(date)
	        },
	        success: handler
		});
	}
	
	function onLoadStatistics(returned) {
		if (settings.spinner) {
			settings.spinner.removeCall();
		}
		$.log("onLoadStatistics");
		$.log($(self));
		$.log($(this));
		$(self).each(function() {
			
			var element = this;
			//$(element).html('');
			
			$.log("!");
			$.log($(element));
			
			$(element).append ("<P>Over last "+settings.defaultDateOffset+" days : <br/>");
			var inserted = false;
			$.each(returned.response.results.statistics, function(index, statistic) {
        		$(element).append(" &raquo; " + statistic.name + " : " + statistic.value + " annotations<br>");
        		inserted = true;
        	});
			if (! inserted) {
				$(element).append ("&raquo; No changes. <br>");
			}
			$(element).append ("<a id='showstats' style='cursor:pointer;' >More details...</a> </P>");
			
			$('a#showstats').click(function(event) {
				loadAnnotationChanges(settings.organism, getDefaultDate(), onLoadAnnotationChanges);
			});
		});
	}
	
	function loadAnnotationChanges(organism, date, handler) {
		if (settings.spinner) {
			settings.spinner.addCall();
		}
		$.ajax({
	        url: settings.web_service_root + "features/annotation_changes.json",
	        type: 'GET',
	        dataType: 'json',
	        data: {
	            'organism' : settings.organism,
	            'date' : getDateString(date)
	        },
	        success: function (returned) {
	        	handler(returned, organism, date);
	        }
		});
	}
	
	function onLoadAnnotationChanges(returned, organism, date) {
		if (settings.spinner) {
			settings.spinner.removeCall();
		}
		$(self).each(function() {
			
			var element = this;
			
			var s = "<table cellpadding=10 cellspacing=10><tr><th>gene</th><th>type</th><th>details</th><th>date</th></tr>";
			
			var countFeatures = returned.response.results.features.length;
			var countAnnotationChanges = 0; 
			
			
			$.each(returned.response.results.features, function(index, feature) {
				var a = "<tr ><td><a style='text-decoration:underline;' href='" 
					+ settings.baseHREF + feature.uniqueName + "' >" + feature.uniqueName + "</a></td><td>";
				
				$.each(feature.changes, function(index, change) {
					a += "<tr><td>&nbsp;&raquo;&nbsp;</td>";
					a += "<td>" + change.type + "</td>";
					a += "<td>" + change.detail + "</td>";
					a += "<td>" + change.date + "</td>";
					a += "</tr>";
					countAnnotationChanges++;
				});
				
				a += "</tr>";
				
                s += a;
			});
			
			s += "</table>";
			
			$.log($(element));
			var readableActivity = $(element).data('readableActivity');
			$.log(readableActivity);
			$(readableActivity).html(s);
			
			var activities = $(element).data('activities');
			$.log(activities);
			
			$(activities).dialog({ 
				width: 700, 
				height: 530 , 
				title :  "Recent annotation activity (since "  + getDateString(date) + ", features " + countFeatures + ", annotations "+countAnnotationChanges+" )" 
			});;
			
			var annotation_change_datepicker = $(element).data('annotation_change_datepicker');
			$.log(annotation_change_datepicker);
			
			$(annotation_change_datepicker).datepicker({
				maxDate: '+0D', 
				dateFormat: 'yy-mm-dd',  
				selectedDate: date,
				onSelect: function(dateText, inst) {
					var newDate = new Date(inst.selectedYear, inst.selectedMonth, inst.selectedDay);
					loadAnnotationChanges(organism, newDate, onLoadAnnotationChanges);
              } 
			});
			
			
		});
	}
	
	function getDefaultDate() {
		return getDateAtXDaysAgo(settings.defaultDateOffset);
	}
	
	function getDateAtXDaysAgo(since) {
		var d = new Date();
		d.setDate(d.getDate() - since);
		return d;
	}
	
	function getDateString(date) {
	    
	    var year = pad(date.getFullYear(), 4);
	    var month = pad(date.getMonth() + 1, 2);
	    var day = pad(date.getDate(), 2);
	    
	    var dateString = year + "-" + month + "-" + day;
	    //log(dateString);
	    return dateString;
	    
	}
	
	function pad(number, length) {
	    var str = '' + number;
	    while (str.length < length) {
	        str = '0' + str;
	    }
	    return str;
	}

	
	
})(jQuery);


function SpinnerManager(selector, options) {
    
    var calls = 0;
    
    var settings = {
        height: 11, 
        width: 43, 
        position : 'right', 
        img: '/path/to/image' 
    
    };
    
    if ( options ) { 
       $.extend( settings, options );
    }
    
    
    var calling = false;
    
    this.addCall = function () {
        calls++;
        $.log(calls);
        if (! calling) {
            var img = "<img src='" + settings.img + "' height='"+ settings.height +"' width='"+ settings.width + "' >";
            $.log(img);
            $(selector).html(img);
        }
        calling = true;
    };
    
    this.removeCall = function() {
        calls--;
        if (calls < 0) {
            calls = 0;
        }
        $.log(calls);
        
        if (calls == 0) {
        	
            $(selector).html('');
            calling = false;
        }
        
    };
    
    this.reset = function() {
        calls = 0;
        calling = false;
    };
    
}






