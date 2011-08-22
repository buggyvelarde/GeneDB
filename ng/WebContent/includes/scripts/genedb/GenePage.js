function mungeKey (key) {
	return key.split(" ").join("");
}

$(function(){
	
	/*
	 * Declare templates and Backbone model and view classes and attach them to the window. 
	 */
	
	window.FeatureSummaryTemplate = 
		"<% _.each( this.model.attributes, function( obj, key){ %> \
			<tr id='<%= mungeKey(key) %>Row' > \
			    <th><%= key %></th> \
			    <td class='erasable' id='<%= mungeKey(key) %>Value' ><%= obj %></td> \
			</tr> \
		<% }); %> ";
	
	
	window.FeatureSeeAlsoTemplate = 
		"<% _.each( this.model.attributes, function( dbxref, key){ %> \
				<li> <a href='<%= dbxref.urlprefix + dbxref.accession %>'><%= dbxref.accession %></a> (<%= (dbxref.description) ? dbxref.description : dbxref.database %>) </li> \
			<% }); %> ";
	
	window.FeatureProductSummaryTemplate = 
		"<% _.each( this.model.attributes, function( obj, key){ %> \
			<li><%= obj.name %> \
			<% _.each( obj.props, function( prop, pkey){ %> \
				<% if (prop.type.name == 'qualifier') { %> \
					<%= prop.value %> <% if (pkey >0) { %> | <% } %>  \
				<% } %> \
			<% }); %> \
			<% _.each( obj.props, function( prop, pkey){ %> \
				<% if (prop.type.name == 'evidence') { %> \
					<%= prop.value %>\
				<% } %> \
			<% }); %> \
			<% _.each( obj.pubs, function( pub, pkey){ %> \
				<a href='http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&dopt=Abstract&list_uids=<%= pub.uniqueName %>'><%= pub.uniqueName %></a> \
			<% }); %> \
			<% _.each( obj.dbxrefs, function( dbxref, pkey){ %> \
				<%= dbxref.uniqueName %>\
			<% }); %> \
				(<%= obj.count %> other<% if (obj.count >1) { %>s<% } %>) \
			</li> \
		<% }); %> ";
	
	
	window.PropertyTemplate = 
		"   <div class='full-light-grey-top'></div>\
			<div class='light-grey'> \
				<h2>Comments</h2> \
				<div  > \
					<ul id='NotesValue'> \
						<% _.each( notes, function( obj, key){ %>  <li> <%= obj %> </li>  <% }); %>  \
					</ul> \
				</div> \
				<div >\
					<ul id='CommentsValue'> \
						<% _.each( comments, function( obj, key){ %>  <li> <%= obj %> </li>  <% }); %>  \
					</ul>\
				</div> \
				<div > \
					<ul class='erasable' id='CurationValue'> \
						<% _.each( curations, function( obj, key){ %>  <li> <%= obj %> </li>  <% }); %>  \
				</ul></div> \
				<br /> \
				<div >Key information on this gene is available from\
					<span class='erasable' id='PublicationsValue'>\
						 <% _.each( publications, function( obj, key){ %>   <%= obj %>  <% }); %>  \
					</span>\
				</div> \
			</div> \
			<div class='full-light-grey-bot'></div><br>";
	
	
	window.GeneOntologyTemplate = 
		'<h2>Gene Ontology</h2>\
			<table cellpadding="0" cellspacing="4" border="0" class="sequence-table">\
			    <tr class="hideable" id="biologicalProcessRow">\
			      <th>Biological Process</th>\
			      <td id="biologicalProcessField" class="erasable term"></td>\
			    </tr>\
			    <tr class="hideable" id="molecularFunctionRow">\
			      <th>Molecular Function</th>\
			      <td id="molecularFunctionField" class="erasable term"></td>\
			    </tr>\
			    <tr class="hideable" id="cellularComponentRow">\
			      <th>Cellular Component</th>\
			      <td id="cellularComponentField" class="erasable term"></td>\
			    </tr>\
			</table>';
		
	
	
	
	window.FeatureModel = Backbone.Model.extend({
		defaults: {
			//Product: "" 
	    },
		initialize: function() {
//	      if (!this.get("uniqueName")) {
//	        this.set({"uniqueName": defaults.uniqueName});
//	      }
	    },
	    add: function(key,value) {
	    	this.set({key : { name: this.keyNames[key], value:value }});
	    }
	});
	
	window.FeatureProductModel = Backbone.Model.extend({});
	window.FeatureSeeAlsoModel = Backbone.Model.extend({});
	window.PropertyModel = Backbone.Model.extend({});
	
	window.FeatureSummaryView = Backbone.View.extend({
		tagName:  "ul",
		template: _.template(window.FeatureSummaryTemplate),
	    initialize: function(){
	        _.bindAll(this, 'render'); // fixes loss of context for 'this' within methods 
	         this.render(); // not all views are self-rendering. This one is.
	         this.model.bind('change', this.render); // rerender when the view changes
	    },
		// Re-render the contents of the todo item.
	    render: function() {
	    	$.log("rendering");
	    	$(this.el).html(this.template(this.model.toJSON()));
	    	return this;
	    },
	    addProduct: function(product_model) {
	    	var view = new FeatureProductSummaryView({model: product_model});
	    	this.$("#ProductValue").append(view.render().el);
	    },
	    addSeeAlso: function(see_also_model) {
	    	var view = new FeatureSeeAlsoSummaryView({model: see_also_model});
	    	this.$("#SeeAlsoValue").append(view.render().el);
	    }
	});
	
	window.FeatureProductSummaryView = Backbone.View.extend({
		tagName:  "ul",
		template: _.template(window.FeatureProductSummaryTemplate),
	    initialize: function(){
	        _.bindAll(this, 'render'); // fixes loss of context for 'this' within methods 
	         this.render(); // not all views are self-rendering. This one is.
	    },
		// Re-render the contents of the todo item.
	    render: function() {
	    	$(this.el).html(this.template(this.model.toJSON()));
	    	return this;
	    }
	});
	
	window.FeatureSeeAlsoSummaryView = Backbone.View.extend({
		tagName:  "ul",
		template: _.template(window.FeatureSeeAlsoTemplate),
	    initialize: function(){
	        _.bindAll(this, 'render'); // fixes loss of context for 'this' within methods 
	         this.render(); // not all views are self-rendering. This one is.
	    },
	    render: function() {
	    	$(this.el).html(this.template(this.model.toJSON()));
	    	return this;
	    }
	});
	
	window.PropertyView = Backbone.View.extend({
		tagName:  "ul",
		template: _.template(window.PropertyTemplate),
	    initialize: function(){
	        _.bindAll(this, 'render'); // fixes loss of context for 'this' within methods 
	         this.render(); // not all views are self-rendering. This one is.
	    },
	    render: function() {
	    	$(this.el).html(this.template(this.model.toJSON()));
	    	$(this.el).show();
	    	return this;
	    }
	});
	

	
	
	
});

function generateSummary(info) {
	
	var hash = {};
	
	var usedUniqueName = info.uniqueName;
	
	if (info.transcript_count < 2 && info.geneUniqueName != null) {
		usedUniqueName = info.geneUniqueName;
	}
	
	var systematicName = usedUniqueName;
	
    if (info.transcript_count > 2 && info.feature.type=="mRNA") {
    	systematicName += " (one splice form of " + info.geneUniqueName;
    }
    
    hash ["Systematic Name"] = systematicName;
    
    if (info.geneUniqueName != null && info.geneUniqueName != usedUniqueName) {
    	hash["Gene Name"] = info.geneUniqueName;
    }
    
    hash ["Feature Type"] = ( info.hierarchyType != null ) ? info.hierarchyType : info.feature.type.name;
    
    hash ["Product"] = "";
    
    
    if (info.synonyms != null) {
		
		var previous_systematic_ids = [];
		var synonyms = [];
		var product_synonyms = [];
		
    	$.each(info.synonyms, function(n,synonym) {
    		
    		var syn = synonym.synonym;
    		
    		if (synonym.synonymtype == "previous_systematic_id" && synonym.is_current == true) {
    			previous_systematic_ids.push(syn);
    		} else if (synonym.synonymtype == "synonym") {
    			synonyms.push(syn);
    		} else if (synonym.synonymtype == "product_synonym") {
    			product_synonyms.push(syn);
    		}
    		
    		$.log(synonym.synonym, synonym.synonymtype, synonym.is_current);
    	});
    	
    	if (previous_systematic_ids.length > 0)
    		hash["Previous Systematic ID"] = previous_systematic_ids.join (", ");
    	
    	if (synonyms.length > 0)
    		hash["Synonym"] = synonyms.join (", ");
    	
    	if (product_synonyms.length > 0)
    		hash["Product Synonym"] = product_synonyms.join (", ");
    	
    }
    
    hash["Location"] = info.regionInfo.type.name + " " + info.regionInfo.uniqueName + "; " + info.feature.fmin + "-" + info.feature.fmax;
    
    hash ["See Also"] = "";
    
    if (info.organism.common_name == "Pchabaudi" || info.organism.common_name == "Pberghei" ) {
		hash["PlasmoDB"] = "<a href=\"http://plasmodb.org/gene/"+info.geneUniqueName+ "\" >"+info.geneUniqueName+"</a>";
		
	} else if (
		info.organism.common_name == "Lmajor" ||
		info.organism.common_name == "Linfantum" ||
		info.organism.common_name == "Lbraziliensis" ||
		info.organism.common_name == 'Tbruceibrucei927' || 
		info.organism.common_name =='Tbruceibrucei427' || 
		info.organism.common_name =='Tbruceigambiense' || 
		info.organism.common_name =='Tvivax' || 
		info.organism.common_name =='Tcruzi'
	) {
		hash["TriTrypDB"] = "<a href=\"http://tritrypdb.org/gene/"+info.geneUniqueName+ "\" >"+info.geneUniqueName+"</a>";
	}
    
    window.featureModel = new FeatureModel(hash);
    window.featureSummary = new FeatureSummaryView({model : window.featureModel, el: "#GeneSummary" });
    
    
	$.log(window.featureSummary);
	
}

function genePage(uniqueName, webArtemisPath, options) { 
	
	var defaults = {
		services : ["/services/"],
		hideable : ".hideable",
		erasable : ".erasable"
	};
	
	var settings = $.extend({}, defaults, options);
	
	var self = this;
	self.usedUniqueName = uniqueName;
	
	getInfo(uniqueName, initInfo);
	
	function getInfo(uniqueName, onResult) {
		$.log("getInfo");
		var info = new FeatureInfo(uniqueName, {services : ["/services/"]}, function(){
			$.log("getInfo onResult...");
			onResult(info);
		});
	}
	
	function resetPage(name, onComplete) {
		$.log("resetPage");
		$.log("resetPage", name, settings.erasable, settings.hideable);
		
		$.log($(settings.erasable));
		$.log($(settings.hideable));
		
		$(settings.erasable).html("");
		$(settings.hideable).hide();
		
		getInfo(name, function(info) {
			setPage(info);
			onComplete(info);
		});
		
		
	}
	
	function showGo(goTermSet, row, field) {
		$.each(goTermSet, function(g, go) {
			
			var  evidence = [],
				 qualifier = [],
				 pubs = [],
				 dbxrefs = [];
			
			$.each(go.props, function(p, prop) {
				if (prop.type.name == "evidence") {
					evidence.push(prop.value);
				}
				if (prop.type.name == "qualifier") {
					qualifier.push(prop.value);
				}
			});
			
			$.each(go.dbxrefs, function(d, dbxref) {
				var a = "<a href='" + dbxref.urlprefix + dbxref.accession + "' >" + dbxref.db + ":" + dbxref.accession + "</a>";
				dbxrefs.push (a);
			});
			
			$.each(go.pubs, function(p, pubs) {
				pubs.push(pub.uniqueName);
			});
			//alert([row,field]);
			$(field).append(go.name + " " + evidence.join(" | ") + " " + qualifier.join(" | ") + " " + pubs.join(" | ") + " " + dbxrefs.join(" | ") + " " + " (" + go.count + " others) <br>");
			$(row).show();
		});
		
		
	}
	
	
	function setPage(info) {
		
		$.log("setPage");
		
		generateSummary(info);
		
		if (info.peptideName != null) {
        	var poly_info = new PolypeptideInfo(info.peptideName, {service:info.service});
        	
        	poly_info.getDbxrefs(function(features){
        		$.log(features);
        		
        		var d = [];
        		
        		$.each(features, function(n,feature) {
        			
        			$.each(feature.dbxrefs, function(m,dbxref){
        				d.push(dbxref);
        			});
        		});
        		
        		if (d.length > 0) {
        			window.featureSeeAlsotModel = new FeatureSeeAlsoModel(d);
        			window.featureSummary.addSeeAlso(featureSeeAlsotModel);
        		}
        		
        	});
        	
        	
        	poly_info.getProducts(function(features){
        		$.log(features);
        		
        		var p = []
        		
        		$.each(features, function(n,feature) {
        			$.each(feature.terms, function(t,term) {
        				p.push(term);
        			});
        		});
        		
        		if (p.length > 0) {
        			window.featureProductModel = new FeatureProductModel(p);
        			window.featureSummary.addProduct(featureProductModel);
        		}
        		
        	});
        	
		}
		
		properties = {
			"comments" : [],
			"notes" : [],
			"curations" : [],
			"publications" : []
		};
		
		if (info.properties != null) {
        	
        	$.each(info.properties, function(n, feature) {
        		$.log("prop " + feature.uniqueName);
        		
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		
        		$.each(feature.properties, function(p, property) {
        			$.log(property);
        			
        			if (property.name == "comment") {
        				properties.comments.push(property.value);
        			}
        			
        			else if (property.name == "note") {
        				properties.notes.push(property.value);
        			}
        			
        			else if (property.name == "curation") {
        				properties.curations.push(property.value);
        			}
        			
        			propsShown=true;
        		});
        	});
        	
        }
		
		if (info.pubs != null) {
        	
        	$.each(info.pubs, function(n, feature) {
        		$.log("pub? " + feature.uniqueName);
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		$.each(feature.pubs, function(p, pub) {
        			properties.publications.push(pub.database+ ":" + pub.accession);
        		});
        	});
        		
        }
		
		$.log(properties);
		
		
		
		var propertyModel = new window.PropertyModel(properties)
		
		$.log("prepared model");
		$.log(propertyModel);
		
		window.propertySummary = new PropertyView({model : propertyModel , el: "#Properties" });
		
		
		
		if (info.terms != null) {
        	
        	
        	$.each(info.terms, function(n, feature) {
        		$.log("term? " + feature.uniqueName);
        		
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		
        		// make sure these ones exist
        		var terms_hash = {
    				biological_process : [],
       				molecular_function : [],
       				molecular_function : [],
       				CC_genedb_controlledcuration : []
        		};
        		
        		
        		$.each(feature.terms, function(p, term) {
        			$.log("term: ");
        			
        			if (! terms_hash.hasOwnProperty(term.cv.name))
        				terms_hash[term.cv.name] = []
        			terms_hash[term.cv.name].push(term);
        			
        		});
        		
        		///cgi-bin/amigo/term-details.cgi?term=GO%3A0048015&amp;speciesdb=GeneDB_Tbruceibrucei927
        		///Query/controlledCuration?taxons=Tbruceibrucei927&amp;cvTermName=phosphatidylinositol-mediated+signaling&amp;cv=biological_process&amp;suppress=Tb927.2.2260%3AmRNA
        		
        		
        	});
        	
        }
		
		
		
		
		/*
		var type = ( info.hierarchyType != null ) ? info.hierarchyType : info.feature.type.name;
		
		if (info.transcript_count < 2 && info.geneUniqueName != null) {
			self.usedUniqueName = info.geneUniqueName;
		}
		
		$("#featureType").html(type);
		
        $("#systematicName").html(self.usedUniqueName);
        if (info.transcript_count > 2 && info.feature.type=="mRNA") {
        	$("#systematicName").append(" (one splice form of " + info.geneUniqueName);
        }
        
        if (info.geneUniqueName!= null && info.geneUniqueName != self.usedUniqueName) {
            $("#geneNameRow").show();
            $('#geneNameField').html(info.geneUniqueName);
        }
        
        $("#regionField").append(info.regionInfo.type.name + " " + info.regionInfo.uniqueName + "; " + info.feature.fmin + "-" + info.feature.fmax);
        
        if (info.properties != null) {
        	
        	var propsShown = false;
        	
        	$.each(info.properties, function(n, feature) {
        		$.log("prop " + feature.uniqueName);
        		
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		
        		$.each(feature.properties, function(p, property) {
        			$.log(property);
        			
        			if (property.name == "comment") {
        				$("#commentsField").append("<li>"+property.value + "</li>");
        				$("#commentsRow").show();
        			}
        			
        			if (property.name == "note") {
        				$("#notesField").append("<li>"+property.value + "</li>");
        				$("#notesRow").show();
        			}
        			
        			if (property.name == "curation") {
        				$("#curationField").append("<li>"+property.value + "</li>");
        				$("#curationRow").show();
        			}
        			
        			propsShown=true;
        		});
        	});
        	
        	if (propsShown)
        		$("#comments").show();
        }
        
        if (info.pubs != null) {
        	var pubsShown = false;
        	$.each(info.pubs, function(n, feature) {
        		$.log("pub? " + feature.uniqueName);
        		
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		
        		$.each(feature.pubs, function(p, pub) {
        			$.log("pub:");
        			$.log(pub);
        			
        			
        			$("#publicationsField").append(" "+pub.database+ ":" + pub.accession );
        			
        			pubsShown = true;
        			
        		});
        		
        	});
        	
        	if (pubsShown) {
        		$("#comments").show();
        		$("#publicationsRow").show();
        	}
        		
        }
        
        if (info.terms != null) {
        	
        	
        	$.each(info.terms, function(n, feature) {
        		$.log("term? " + feature.uniqueName);
        		
        		// if we have a peptide, just use that
        		if (info.peptideName != null && info.peptideName != feature.uniqueName) {
    				return;
    			}
        		
        		var go = {
    				biological_process : [],
    				molecular_function : [],
    				molecular_function : []
        		}
        		
        		$.each(feature.terms, function(p, term) {
        			$.log("term: ");
        			
        			
        			
        			if (term.cv.name == "CC_genedb_controlledcuration") {
        				
        				$.log(term);
        				
        				var evidence = "", 
        					pub = "";
        				
        				$.each(term.props, function(pp, prop) {
        					
        					if (prop.name == "evidence") {
        						evidence = prop.value;
        					}
        					
        				});
        				
        				if (term.pubs.length > 0)
        					pub = term.pubs[0].uniqueName;
        				
        				$("#controlledcurationField").append(term.name + " " + evidence + pub + " (" + term.count + " others) <br>");
        				$("#controlledCurationRow").show();
        				
        			}
        			
        			
        			
        			
        			if (term.cv.name == "biological_process" || term.cv.name == "molecular_function" || term.cv.name == "cellular_component"  ) {
        				
        				go[term.cv.name].push(term);
        				
        				//alert("go" + term.cv.name);
        			}
        			
        			
        		});
        		
        		///cgi-bin/amigo/term-details.cgi?term=GO%3A0048015&amp;speciesdb=GeneDB_Tbruceibrucei927
        		///Query/controlledCuration?taxons=Tbruceibrucei927&amp;cvTermName=phosphatidylinositol-mediated+signaling&amp;cv=biological_process&amp;suppress=Tb927.2.2260%3AmRNA
        		if (go.biological_process.length > 0) {
        			showGo(go.biological_process, "#biologicalProcessRow", "#biologicalProcessField");
        			$("#go").show();
        		}
        		if (go.biological_process.length > 0) {
        			showGo(go.molecular_function, "#molecularFunctionRow", "#molecularFunctionField");
        			$("#go").show();
        		}
        		if (go.biological_process.length > 0) {
        			showGo(go.cellular_component, "#cellularComponentRow", "#cellularComponentField");
        			$("#go").show();
        		}
        		
        		
        		
        	});
        	
        }
        
        if (info.peptideName != null) {
        	
        	var poly_info = new PolypeptideInfo(info.peptideName, {service:info.service});
        	
        	poly_info.getDbxrefs(function(features){
        		$.log(features);
        		
        		$.each(features, function(n,feature) {
        			
        			var dbxrefs = feature.dbxrefs;
        			
        			$.each(dbxrefs, function(m,dbxref){
        				$.log(dbxref);
                        
        				//<li> <a href="${dbxref.urlPrefix}${dbxref.accession}${urlSuffix}">${dbxref.accession}</a> (<db:dbName db="${dbxref.dbName}"/>) </li>
        				
        				var href = dbxref.urlprefix + dbxref.accession;
        				var a = dbxref.accession ;
        				var db = (dbxref.description) ? dbxref.description : dbxref.database ;
        				
        				$('#dbxrefField').append("<li><a href='" + href + "' >" + a + "</a> (" + db + ")</li>");
        				$('#dbxrefRow').show();
        				
        			});
        		});
        		
        	});
        	
        	
        	if (info.organism.common_name == "Pchabaudi" || info.organism.common_name == "Pberghei" ) {
        		$("#plasmodbField").html("<a href=\"http://plasmodb.org/gene/"+info.geneUniqueName+ "\" >"+info.geneUniqueName+"</a>");
        		$("#plasmodbRow").show();
        	} else if (
    			info.organism.common_name == "Lmajor" ||
    			info.organism.common_name == "Linfantum" ||
    			info.organism.common_name == "Lbraziliensis" ||
    			info.organism.common_name == 'Tbruceibrucei927' || 
    			info.organism.common_name =='Tbruceibrucei427' || 
    			info.organism.common_name =='Tbruceigambiense' || 
    			info.organism.common_name =='Tvivax' || 
    			info.organism.common_name =='Tcruzi'
        	) {
        		$("#tritrypdbField").html("<a href=\"http://tritrypdb.org/gene/"+info.geneUniqueName+ "\" >"+info.geneUniqueName+"</a>");
        		$("#tritrypdbRow").show();
        	}
        	
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
                        
                        $("#productRow").show();
                        
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
        */
	}
	
	function initInfo(info) {
		
		$.log("initInfo");
		
		$.log(info.feature.uniqueName, info.feature.fmin, info.feature.fmax, info.feature.region, info.sequenceLength, info.geneUniqueName, info.feature.type, info.transcript_count);
		$.log(info.peptideName);
		
		$.log(info.feature.type, info.geneType);
		
		setPage(info);
		
		
		
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
                $('#webartemis').WebArtemis('addObserver', new GeneDBPageWebArtemisObserver(info.feature.region, info.feature.fmin-1000, info.feature.fmin +2000, resetPage));
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