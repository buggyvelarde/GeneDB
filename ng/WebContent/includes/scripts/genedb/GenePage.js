function genePage(uniqueName, webArtemisPath) { 
	
	var usedUniqueName = uniqueName;
	
	var info = new FeatureInfo(uniqueName, {services : ["/services/", "/smansoni/"]}, initInfo);
	
	function initInfo() {
		
		$.log(info.feature.uniqueName, info.feature.fmin, info.feature.fmax, info.feature.region, info.sequenceLength, info.geneUniqueName, info.feature.type, info.transcript_count);
		$.log(info.peptideName);
		
		var type = info.feature.type;
        if (type == "polypeptide") {
            type = "Protein coding gene";
        } else if (type.contains("pseudo")) {
            type = "Pseudogene";
        }
        
		
		if (info.transcript_count < 2 && info.geneUniqueName != null) {
			usedUniqueName = info.geneUniqueName;
			type = "gene";
		}
		
		$("#featureType").html(type);
		
		
		
        $("#systematicName").html(usedUniqueName);
        if (info.transcript_count > 2 && info.feature.type=="mRNA") {
        	$("#systematicName").append(" (one splice form of " + info.geneUniqueName);
        }
        
        if (info.geneUniqueName != usedUniqueName) {
            $("#geneNameRow").show();
            $('#geneNameField').html(info.geneUniqueName);
        }
        
        
        
        if (info.peptideName != null) {
        	
        	var poly_info = new PolypeptideInfo(info.peptideName, {service:info.service});
        	
        	poly_info.getProducts(function(features){
        		$.log(features);
        		
        		$.each(features, function(n,feature) {
        			
        			var terms = feature.terms;
        			
        			$.each(terms, function(m,term){
        				$.log(term);
                        var product_description = [];
                        
                        product_description.push(term.name);
                        
                        var sep = "";
                        $.each(term.props, function(p,prop){
                            if (prop.type.name == "qualifier") {
                                product_description.push(sep + prop.value);
                                sep=" | ";
                            }
                        });
                        
                        
                        $.each(term.props, function(p,prop){
                            
                            if (prop.type.name == "evidence") {
                                product_description.push(prop.value);
                            }
                            
                        });
                        
                        $.each(term.pubs, function(p,pub){
                            product_description.push(pub.uniqueName);
                        });
                        
                        $.each(term.dbxrefs, function(p,dbxref){
                            product_description.push(pub.uniqueName);
                        });
                        
                        
                        
                        if (term.count > 0) {
                        	var other = (term.count > 1) ? "others" : "other";
                        	var href_1 = "<a href='" + getBaseURL() + "Query/controlledCuration?taxons="+info.organism.common_name+"&cvTermName="+term.name+"&cv=genedb_products' >";
                        	var href_2 = "</a>";
                        	var n_others = "(" + href_1 + term.count + " " + other + href_2 + ")";
                        	
                        	product_description.push(n_others);
                        	
                        	
                        }
                        
                        product_description.push("<br>");
                        $("#productField").append(product_description.join(" "));
                        
        			});
        			
        			
        		});
        		
        	});
        	
        	
        	if (info.synonyms != null) {
        		
        		var previous_systematic_ids = 0;
        		var synonyms = 0;
        		var product_synonyms = 0;
        		
            	$.each(info.synonyms, function(n,synonym) {
            		
            		var syn = synonym.synonym;
            		
            		if (synonym.synonymtype == "previous_systematic_id" && synonym.is_current == true) {
            			$("#previousSystematicField").append(" " + syn);
            			previous_systematic_ids++;
            		} else if (synonym.synonymtype == "synonym") {
            			$("#synonymField").append(" " + syn);
            			synonyms++;
            		} else if (synonym.synonymtype == "product_synonym") {
            			$("#productSynonymField").append(" " + syn);
            			product_synonyms++;
            		}
            			
            		$.log(synonym.synonym, synonym.synonymtype, synonym.is_current);
            	});
            	
            	if (previous_systematic_ids > 0)
            		$("#previousSystematicRow").show();
            	
            	if (synonyms > 0)
            		$("#synonymRow").show();
            	
            	if (product_synonyms > 0)
            		$("#productSynonymRow").show();
            	
            	
//            	$.each(synonyms, function(n,previous) {
//            		
//            	});
//				$.each(synonyms, function(n,previous) {
//					
//				});
//            	
//            	var th = "Synonym";
//            	th = "Previous Systematic Id";
//            	$("#synonymFields").append("<tr><th>" + th + "</th><td>" + synonym.synonym + "</td></tr>" );
            }
        	
        }
        
        
        
        
        
        
        
        
		
		var topLevelFeatureLength = parseInt(info.sequenceLength);
        var max = 100000;
        var needsSlider = true;
        if (max > topLevelFeatureLength) {
            max = topLevelFeatureLength;
            //needsSlider = false;
        }
        var zoomMaxRatio = max / parseInt(info.sequenceLength);
        
        $("#chromosome-map").ChromosomeMap({
            region : info.feature.region, 
            overideUseCanvas : false,
            bases_per_row: parseInt(info.sequenceLength),
            row_height : 10,
            row_width : 870,
            overideUseCanvas : true,
            loading_interval : 100000,
            axisLabels : false,
            row_vertical_space_sep : 10,
            web_service_root : info.service
        });
        
        $('#webartemis').WebArtemis({
            source : info.feature.region,
            start : info.feature.fmin-1000,
            bases : info.feature.fmax-info.feature.fmin +2000,
            showFeatureList : false,
            width : 950,
            directory : webArtemisPath,
            showOrganismsList : false,
            webService : info.service,
            draggable : false,
            mainMenu : false, 
            zoomMaxRatio : zoomMaxRatio
        });
        
        if (needsSlider) {
            
            $('#chromosome-map-slider').ChromosomeMapSlider({
                windowWidth : 870,
                max : parseInt(info.sequenceLength), 
                observers : [new ChromosomeMapToWebArtemis()],
                pos : info.feature.fmin-1000,
                width : info.feature.fmax-info.feature.fmin +2000
            });
            
            setTimeout(function() { 
                $('#webartemis').WebArtemis('addObserver', new GeneDBPageWebArtemisObserver(info.feature.region, info.feature.fmin-1000, info.feature.fmin +2000));
                $('#webartemis').WebArtemis('addObserver', new WebArtemisToChromosomeMap('#chromosome-map-slider'));
            }, 500);
        }
        
        $('.wacontainer').hover(
            function(e) {
                $("#web-artemis-link-container").show();                    
            }, function(e) {
                $("#web-artemis-link-container").hide();
            }
        );
        
		
	}
    
}