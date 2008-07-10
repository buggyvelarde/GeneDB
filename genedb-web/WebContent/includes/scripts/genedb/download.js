function initDownload(base,history) {
	//empty data set for YUI datatable
	var data = {example: []}

	var downoad = new function() {

        var myColumnDefs = [
            {key:"organism.commonName",label:"Organism"},
            {key:"uniqueName",label:"SystematicID"},
            {key:"synonym",label:"Synonyms"},
            {key:"chr",label:"Chromosome"},
            {key:"locs",label:"Location"},
            {key:"product",label:"Product"},
            {key:"sequence",label:"Sequence",hidden: true},
        ];

        this.myDataSource = new YAHOO.util.DataSource(data.example);
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        this.myDataSource.responseSchema = {
            fields: ["organism.commonName","uniqueName","synonym","chr","locs","product","sequence"]
        }	;
        
        var oConfigs = {   
	                id:"download-list",
        			draggableColumns:true,
	                width: 600
	        }; 	 
        		
        var myDT = this.myDataTable = new YAHOO.widget.DataTable("download", myColumnDefs,
                this.myDataSource,oConfigs );
        
        YAHOO.widget.DataTable.MSG_EMPTY = "";
        myDT.hideTableMessage();
        
        function onClick(e) {
        	var value = this.get("id");
        	var column = myDT.getColumn(value);
        	var checked = this.get("checked");
        	if(value == "sequence") {
        		sequence.addClass("yui-checkbox-button-checked");
        		checked = column.hidden;
        	}
    		if(checked) {
        		myDT.showColumn(column);
        	} else {
        		if(value == "sequence") {
            		sequence.removeClass("yui-checkbox-button-checked");
        		}
        		myDT.hideColumn(column);
        	}
        }
        
        var sequenceType;
        
        function onMenuItemClick(p_sType, p_aArgs, p_oValue) {
        	for (var i=0; i<seqMenu.getItems().length;i++) {
        		seqMenu.getItem(i).cfg.setProperty("checked",false);
        	}
        	this.cfg.setProperty("checked", true);
        	sequenceType = this.value;
        }
        
        //Columns Show/ Hide Buttons
        var organism = new YAHOO.widget.Button({type: "checkbox", name:"Organism",label:"Organism",value:"organism", id:"organism.commonName",container: "buttons", 
            checked: true});
        
        organism.addListener("click", onClick);
        
        var systematicId = new YAHOO.widget.Button({type: "checkbox", name:"Systematic Id",label:"Systematic Id",value:"systematicId", id:"uniqueName", container: "buttons", 
            checked: true});
        
        systematicId.addListener("click", onClick);
        
        var synonym = new YAHOO.widget.Button({type: "checkbox", name:"Synonyms",label:"Synonyms",value:"synonym",id:"synonym", container: "buttons", 
            checked: true});
        
        synonym.addListener("click", onClick);
        
        var chromosome = new YAHOO.widget.Button({type: "checkbox", name:"Chromosome",label:"Chromosome",value:"chromosome",id:"chr", container: "buttons", 
            checked: true});
        
        chromosome.addListener("click", onClick);
        
        var location = new YAHOO.widget.Button({type: "checkbox", name:"Location",label:"Location",value:"location",id:"locs", container: "buttons", 
            checked: true});
        
        location.addListener("click", onClick);
        
        var product = new YAHOO.widget.Button({type: "checkbox", name:"Product",label:"Product",value:"product",id:"product", container: "buttons", 
            checked: true});
        
        product.addListener("click", onClick);
        
        //sequence options
        var sequenceOptions = [

                                 { text: "DNA (Unspliced sequence of CDS) or sequenced EST", value: "UNSPLICED_DNA", onclick: { fn: onMenuItemClick }},
                                 { text: "DNA (Spliced sequence)", value: "SPLICED_DNA", onclick: { fn: onMenuItemClick } },
                                 { text: "Intron sequence", value: "INTRON", onclick: { fn: onMenuItemClick } },
                                 { text: "Protein sequence", value: "PROTEIN", onclick: { fn: onMenuItemClick } },
                                 { text: "Intergenic Sequence (5' )", value: "INTERGENIC_5", onclick: { fn: onMenuItemClick } },
                                 { text: "Intergenic Sequence (3' )", value: "INTERGENIC_3", onclick: { fn: onMenuItemClick } },
                                 { text: "CDS/RNA with 5'/3' flanking sequence", value: "CDS_RNA", onclick: { fn: onMenuItemClick } }
                             ];
        var seqMenu = new YAHOO.widget.Menu('seqMenu');
        seqMenu.addItems(sequenceOptions);
        
        var sequence = new YAHOO.widget.Button({type: "split", name:"Sequence",label:"Sequence",value:"sequence",id:"sequence", container: "sequence", 
            menu:seqMenu, selectedMenuItem:0, disabled: true});
        seqMenu.render();
        sequence.addListener("click", onClick);
        
        function onOutputClick(e) {
        	var current = e.newValue;
        	var value = current.get("value");
        	if(value == "FASTA") {
        		sequence.set('disabled',false);
        		sequence.addClass("yui-checkbox-button-checked");
        		myDT.showColumn(myDT.getColumn("sequence"));
        	} else if (e.prevValue.get("value") == "FASTA") {
        		sequence.set('disabled',true);
                myDT.hideColumn(myDT.getColumn("sequence"));
        	}
        }
        
        //output format buttons
        var outputButtonGroup = new YAHOO.widget.ButtonGroup({ 
            id:  "buttongroup", 
            name:  "outputFormat", 
            container:  "outputFormat" });

		outputButtonGroup.addButtons([
		
		{ type: "radio", label: "Tab-delimited file", value: "TAB", checked: true },
		{ type: "radio", label: "CSV file", value: "CSV"},
		{ type: "radio", label: "HTML Table", value: "HTML" },  
		{ type: "radio", label: "Excel File", value: "EXCEL" },
		{ type: "radio", label: "Fasta File", value: "FASTA" }
		]);
		
		outputButtonGroup.addListener("checkedButtonChange",onOutputClick);
		
		//submit button
		var submitButton = new YAHOO.widget.Button({ 
            type: "submit", 
            label: "Submit", 
            id: "submitButton", 
            name: "submitButton", 
            value: "submit", 
            container: "submitButton" });
		
		submitButton.addListener("click",onSubmitClick);
		
		//div where the revolving image and html table are displayed
		var div = document.getElementById('container');
		
		
		var handleSuccess = function(o){
			if(o.responseText !== undefined){
				div.innerHTML = "";
				//window.location = base + o.getResponseHeader.filename;
				window.location = o.getAllResponseHeaders;
			}
		}

		var handleFailure = function(o){
			if(o.responseText !== undefined){
				if(o.status == 511) {
					div.innerHTML = "No history item corresponding to the history number " + history + " is available" +
							" in the database";
				} else {
					div.innerHTML = "Some error occurred. Please Try again.";
				}
			}
		}

		//callback definition for Async request
		var callback =
		{
		  success:handleSuccess,
		  failure: handleFailure,
		  argument: {}
		};
		
		//array to hold colum keys and labels
		var columns = new Array();
		
		//create table if output format is HTML Table
		function createTable(initialRequest) {
			
			var myColumns = new Array();
			var fields = new Array();
			for(var i=0;i<columns.length;i++) {
				var column = new YAHOO.widget.Column();
				var key = columns[i][0];
				
				column.label = columns[i][1];
				myColumns[i] = column;
				if(key == "uniqueName") {
					fields[i] = "name";
					column.key = "name";
				} else if(key == "organism.commonName") {
					fields[i] = "organism";
					column.key = "organism";
				} else if(key == "chr") {
					fields[i] = "chromosome";
					column.key = "chromosome";
				} else if(key == "locs") {
					fields[i] = "location";
					column.key = "location";
				} else {
					fields[i] = key;
					column.key = key;
				}
			}

	        this.dataSource = new YAHOO.util.DataSource(base+"DownloadFeatures");
	        this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
	        this.dataSource.responseSchema = {
	        		resultsList: "hits",
	        		fields: fields
	        }	;
	        
	        var configs = {   
		                id:"HTML-RESULTS",
		                paginator: new YAHOO.widget.Paginator({ 
		                    rowsPerPage: 25,
		                    template : "{FirstPageLink} {PreviousPageLink} <span>{CurrentPageReport}</span> {PageLinks} {NextPageLink}",
		                    previousPageLinkLabel : '&lt;',
		                    nextPageLinkLabel : '&gt;',
		                    pageReportTemplate : 'Showing records <strong>{startRecord} - {endRecord}</strong> of {totalRecords}'
		                }),  
		                initialRequest: initialRequest,
	        			draggableColumns:true,
		                width: 600
		        }; 	 
	        		
	        var dataTable = this.myDataTable = new YAHOO.widget.DataTable("container", myColumns,
	                this.dataSource,configs );
		}
		
		//called when submit button is clicked
		function onSubmitClick(e) {
			var columnSet = myDT.getColumnSet();
			var size = columnSet.keys.length;
			var cols = "";
			var j = 0;
			
			for(var i=0;i<size;i++) {
				var column = columnSet.keys[i];
				if(column.hidden == false) {
					columns[j] = new Array(2);
					columns[j][0] = column.key;
					columns[j][1] = column.label;
					cols += column.key+",";
					j++;
				}
			}
			
			var outputFormat;
			
			var bg = outputButtonGroup.getButtons();
			for(j=0; j<bg.length;j++) {
				var button = bg[j];
				if(button.get("checked")) {
					outputFormat = button.get("value");
					break;
				}
			}
			
			//send request
			var initialRequest = "?json=true&historyItem=" + history + "&columns=" + cols + 
				  "&outputFormat=" + outputFormat;
			
			if(outputFormat == "FASTA") {
				initialRequest += "&sequenceType=" + sequenceType;
			}
			
			var url = base + "DownloadFeatures" + initialRequest;
			div.innerHTML = "<img src=\"" + base  + "includes/YUI-2.5.2/assets/skins/sam/treeview-loading.gif\"></img>";
			
			if(history<=0) {
				div.innerHTML = "History number cannot be less than or equal to zero.";
			} else {
				if(outputFormat == "HTML") {
					createTable(initialRequest);
				} else {
					var request = YAHOO.util.Connect.asyncRequest('GET', url,callback);
				}
			}
		}
		
		
    };
} 