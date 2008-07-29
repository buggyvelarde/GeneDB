var myDT;

function makeDataTable(base,params) {
	
	var url = base + "BrowseTerm";
	
	this.formatUrl = function(elCell, oRecord, oColumn, sData) {
        elCell.innerHTML = "<a href='" + oRecord.getData("ClickUrl") + "' target='_blank'>" + sData + "</a>";
    };
    
    this.formatNameUrl = function(elCell, oRecord, oColumn, sData) {
        
    	var data = oRecord.getData("geneName");
    	elCell.innerHTML = "<a href='" + base + "NamedFeature?name=" + data + "'>" + data + "</a>";
    };
    
    var myColumnDefs = [
        {key:"organismName", label:"Organism", sortable:true},
        {key:"geneName", label:"Name", formatter:this.formatNameUrl, sortable:true},
        {key:"product",label:"Product",sortable:true,minWidth:600}
    ];

    this.myDataSource = new YAHOO.util.DataSource(url);
    this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
    this.myDataSource.connXhrMode = "queueRequests";
    this.myDataSource.responseSchema = {
        resultsList: "features",
        fields: ["organismName","geneName","product"]
    };
    
    var paginator = new YAHOO.widget.Paginator({ 
    	alwaysVisible: false,
    	rowsPerPage: 25,
        template : "{FirstPageLink} <span>{CurrentPageReport}</span> {PageLinks} {LastPageLink}",
        previousPageLinkLabel : '&lt;',
        nextPageLinkLabel : '&gt;',
        pageReportTemplate : 'Showing records <strong>{startRecord} - {endRecord}</strong> of {totalRecords}'
    });
    
    var oConfigs = { 
                paginator: paginator,  
                initialRequest: params,
                width: 600
    }; 	 
    		
    myDT = this.myDataTable = new YAHOO.widget.DataTable("list", myColumnDefs,
            this.myDataSource,oConfigs );	
    
}

YAHOO.util.Event.onDOMReady(function() { 

	var recordSet = myDT.getRecordSet();
	var orgName = new ArrayList();
	var orgCount = new Array();
	var count = 0;
	for(var i=0; i< recordSet.getLength(); i++) {
		var record = recordSet.getRecord(i);
		var organism = record.getData("organismName");
		if(!orgName.contains(organism)) {
			orgName.add(organism);
			orgCount[count] = 0;
			count++;
		} else {
			var index = orgName.getIndex(organism);
			orgCount[index]++;
		}
	}
	
	var string = "";
	for (var i=0; i< orgCount.length; i++) {
		var name = orgName.get(i);
		var count = orgCount[i];
		
		string += name + " (" + count + ")  "; 
	}
	
	var div = document.getElementById("organism");
	div.innerHTML = string;
});