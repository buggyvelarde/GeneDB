


// reset these globally scoped arrays declared in web-artemis
excludes = ['match_part', 'repeat_region', 'repeat_unit', 'direct_repeat', 'EST_match', 'region', 'contig' ];
includes = ['gene', 'exon', 'polypeptide', 'mRNA', 'pseudogenic_transcript', 'pseudogene', 'nucleotide_match', 'pseudogenic_exon', 'gap', 'ncRNA', 'tRNA', 'five_prime_UTR', 'three_prime_UTR', 'polypeptide_motif'];

	
var waLinkText = ["View" , "in a new window"];
var loading = false;
var loadedFeatureName = "";

function changeLink(topLevelFeatureUniqueName, leftBase, basesDisplayWidth) {
    var href = "http://www.genedb.org/web-artemis/?src=" + topLevelFeatureUniqueName + "&base=" 
    	+ leftBase + "&bases=" + basesDisplayWidth;
    $("#web-artemis-link").attr("href", href);
}

function reloadDetails(name) {
    
    if (loading || name ==  null || name == loadedFeatureName) {
    	return;
    }
    
    loading = true;

    $("#geneDetails").fadeTo("slow", 0.4).load(encodeURIComponent(name)+"?detailsOnly=true", null, function () {
    	loadedFeatureName = name;
    	document.title = "Gene element "+name+" - GeneDB";
    	$("#geneDetails").stop().fadeTo("fast", 1);
        loading = false;
   }); 
   
} 

function embedWebArtemis(source,uniqueName,start,bases,directory,webService) {
	var parameters = {
    		source : source,
    		start : start,
    		bases : bases,
    		showFeatureList : false,
    		width : 950,
    		directory : directory,
    		showOrganismsList : false,
    		webService : webService,
    	    draggable : false,
    	    mainMenu : false
    	};
	$('#webartemis').WebArtemis(parameters);
	
	var obs = new function() {
	    this.redraw = function redraw(start, end) {
	    	//console.log("REDRAW DETECTED " + start + " " + end);
	    	changeLink(source, start, end - start);
	    };
	    this.select = function(feature, fDisplay) {
	    	if (feature == loadedFeatureName) {
	    		return;
	    	}
	    	//console.log("SELECT DETECTED " + feature + " ON DISPLAY ");
	    	//$.historyLoad(feature);
	    	reloadDetails(feature);
    	};
	};

	setTimeout(function() { 
	    $('#webartemis').WebArtemis('addObserver', obs);
    }, 500);
    	
    	
    $.historyInit(reloadDetails);
    changeLink(source,start,bases);
    
    	
}


 
