/*
	reflection.js for jQuery v1.03
	(c) 2006-2009 Christophe Beyls <http://www.digitalia.be>
	MIT-style license.
*/
(function(a){a.fn.extend({reflect:function(b){b=a.extend({height:1/3,opacity:0.5},b);return this.unreflect().each(function(){var c=this;if(/^img$/i.test(c.tagName)){function d(){var g=c.width,f=c.height,l,i,m,h,k;i=Math.floor((b.height>1)?Math.min(f,b.height):f*b.height);if(a.browser.msie){l=a("<img />").attr("src",c.src).css({width:g,height:f,marginBottom:i-f,filter:"flipv progid:DXImageTransform.Microsoft.Alpha(opacity="+(b.opacity*100)+", style=1, finishOpacity=0, startx=0, starty=0, finishx=0, finishy="+(i/f*100)+")"})[0]}else{l=a("<canvas />")[0];if(!l.getContext){return}h=l.getContext("2d");try{a(l).attr({width:g,height:i});h.save();h.translate(0,f-1);h.scale(1,-1);h.drawImage(c,0,0,g,f);h.restore();h.globalCompositeOperation="destination-out";k=h.createLinearGradient(0,0,0,i);k.addColorStop(0,"rgba(255, 255, 255, "+(1-b.opacity)+")");k.addColorStop(1,"rgba(255, 255, 255, 1.0)");h.fillStyle=k;h.rect(0,0,g,i);h.fill()}catch(j){return}}a(l).css({display:"block",border:0});m=a(/^a$/i.test(c.parentNode.tagName)?"<span />":"<div />").insertAfter(c).append([c,l])[0];m.className=c.className;a.data(c,"reflected",m.style.cssText=c.style.cssText);a(m).css({width:g,height:f+i,overflow:"hidden"});c.style.cssText="display: block; border: 0px";c.className="reflected"}if(c.complete){d()}else{a(c).load(d)}}})},unreflect:function(){return this.unbind("load").each(function(){var c=this,b=a.data(this,"reflected"),d;if(b!==undefined){d=c.parentNode;c.className=d.className;c.style.cssText=b;a.removeData(c,"reflected");d.parentNode.replaceChild(c,d)}})}})})(jQuery);

// AUTOLOAD CODE BLOCK (MAY BE CHANGED OR REMOVED)
jQuery(function($) {
	
	if (('img.reflect').length > 0) {
		$("img.reflect").reflect({height : 0.25});
	}
	
});


/*
 * jQuery UI Autocomplete HTML Extension
 *
 * Copyright 2010, Scott González (http://scottgonzalez.com)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * http://github.com/scottgonzalez/jquery-ui-extensions
 */
(function( $ ) {

var proto = $.ui.autocomplete.prototype,
	initSource = proto._initSource;

function filter( array, term ) {
	var matcher = new RegExp( $.ui.autocomplete.escapeRegex(term), "i" );
	return $.grep( array, function(value) {
		return matcher.test( $( "<div>" ).html( value.label || value.value || value ).text() );
	});
}

$.extend( proto, {
	_initSource: function() {
		if ( this.options.html && $.isArray(this.options.source) ) {
			this.source = function( request, response ) {
				response( filter( this.options.source, request.term ) );
			};
		} else {
			initSource.call( this );
		}
	},

	_renderItem: function( ul, item) {
		return $( "<li></li>" )
			.data( "item.autocomplete", item )
			.append( $( "<a></a>" )[ this.options.html ? "html" : "text" ]( item.label ) )
			.appendTo( ul );
	}
});

})( jQuery );


function scramble (text) {
    var text2 = text.replace(/[a-z0-9]/ig, function(chr) {
        var cc = chr.charCodeAt(0);
        if (cc >= 65 && cc <= 90) cc = 65 + ((cc - 52) % 26);
        else if (cc >= 97 && cc <= 122) cc = 97 + ((cc - 84) % 26);
        else if (cc >= 48 && cc <= 57) cc = 48 + ((cc - 43) % 10);
        return String.fromCharCode(cc);
    });
    return text2;
}




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
	        	                  "value" : result['@displayId']
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
                              "value" : result['@displayId']
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
        delay: 200,                                                     // don't want it too frequent, as some queries take time, so account for slow-ish typers
        html : true
	});
	
	
	$('div.did-you-mean-result').mouseover(function() {
    	$(this).removeClass('ui-state-default').addClass('ui-state-hover');
    }).mouseout(function() {
    	$(this).removeClass('ui-state-hover').addClass('ui-state-default');
    });
	
    $('div.breadcrumb-link').mouseover(function() {
        $(this).removeClass('ui-state-default').addClass('ui-state-hover');
    }).mouseout(function() {
        $(this).removeClass('ui-state-hover').addClass('ui-state-default');
    });
    
    
    
    $('.homepageselect').each(function() {
    	var firstOption = $(this).children("option").get(0);
    	if (firstOption != null) {
    		$(firstOption).attr("selected", "selected");
    	}
    });
    
    // used this http://stackoverflow.com/questions/206997/jquery-javascript-ie-hover-doesnt-cover-select-box-options
	if ($.browser.msie && $.browser.version < 9) {
	    
		$('select.homepageselect').each(function(n,i){
			var top = $(i).offset().top - $(i).height();
			var left = $(i).offset().left;
			$(i).css("position",'absolute').css("top", top).css("left", left);
		});
	    
	   var expand   = function(){ $(this).css("width", "auto"); };
       var contract = function(){ if (!this.noHide) $(this).css("width", ""); };
       var focus    = function(){ this.noHide = true; };
       var blur     = function(){ this.noHide = false; contract.call(this); };
		
		$('select.homepageselect')
               .hover(expand, contract)
               .focus(focus)
               .click(focus)
               .blur(blur)
               .change(blur);

	  }
    
	$("#nav > li")
        .mouseover(function(){$(this).addClass("over");})
        .mouseout (function(){$(this).removeClass("over");});
	
	$("a#contact").attr("href", scramble($("a#contact").attr("href")));
    
});



  