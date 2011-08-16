function GeneDBPageWebArtemisObserver(source,start,bases) {
	
	var loading = false;
	var loadedFeatureName = "";
	
	function changeLink(topLevelFeatureUniqueName, leftBase, basesDisplayWidth) {
	    var href = "http://www.genedb.org/web-artemis/?src=" + topLevelFeatureUniqueName + "&base=" 
	    	+ leftBase + "&bases=" + basesDisplayWidth;
	    $("#web-artemis-link").attr("href", href);
	}

	function reloadDetails(name) {
	    
		$.log(name);
		
	    if (loading || name ==  null || name == loadedFeatureName) {
	    	return;
	    }
	    
	    loading = true;
	    
	    
//	    
//	    $("#geneDetails").fadeTo("slow", 0.4).load(encodeURIComponent(name)+"?detailsOnly=true", null, function () {
//	    	loadedFeatureName = name;
//	    	document.title = "Gene element "+name+" - GeneDB";
//	    	$("#geneDetails").stop().fadeTo("fast", 1);
//	        loading = false;
//	   }); 
	   
	} 
	
    this.redraw = function redraw(start, end) {
    	//console.log("REDRAW DETECTED " + start + " " + end);
    	changeLink(source, start, end - start);
    };
    this.select = function(feature, fDisplay) {
    	if (feature == loadedFeatureName) {
    		return;
    	}
    	//console.log("SELECT DETECTED " + feature + " ON DISPLAY ");
    	$.historyLoad(feature);
    	//reloadDetails(feature);
	};
	
	$.historyInit(reloadDetails);
    changeLink(source,start,bases);
};

