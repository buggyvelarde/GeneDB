
function initList(base,params) {
	var url = base + "GenesByCvTermAndCv";

	var list = new function() {
        this.formatUrl = function(elCell, oRecord, oColumn, sData) {
            elCell.innerHTML = "<a href='" + oRecord.getData("ClickUrl") + "' target='_blank'>" + sData + "</a>";
        };
        
        this.formatNameUrl = function(elCell, oRecord, oColumn, sData) {
            
        	var data = oRecord.getData("geneName");
        	elCell.innerHTML = "<a href='" + base + "NamedFeature?name=" + data + "'>" + data + "</a>";
        	//alert(elCell.innerHTML);
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
        
        function onDataTableInit() {
        	var i = paginator;
        	alert(paginator.getTotalPages());
        }
        
        var paginator = new YAHOO.widget.Paginator({ 
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
        		
        this.myDataTable = new YAHOO.widget.DataTable("list", myColumnDefs,
                this.myDataSource,oConfigs );
    };
}