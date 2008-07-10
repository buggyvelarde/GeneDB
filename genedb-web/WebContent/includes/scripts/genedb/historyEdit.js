
var ids = new Array();
var paginator;
var myDT;

function initHistoryEdit(base,history) {
	var params = "?columns=organism.commonName,uniqueName,product&outputFormat=HTML&json=true&historyItem=" + history;
	var url = base + "DownloadFeatures";
	
	makeDataTable(url,params);
	
	makeToolBar();
}

function makeToolBar() {
	var toolbar = new YAHOO.widget.Toolbar('toolBar', {
        buttons: [
            { id: 'tb_selectAllPage', type: 'push', label: 'Select All in page', value: 'selectAllPage' },
            { id: 'tb_selectAll', type: 'push', label: 'Select All', value: 'selectAll' },
            { type: 'separator' },
            { id: 'tb_delete', type: 'push', label: 'Delete', value: 'delete'},
            { type: 'separator' },
            { id: 'tb_save', type: 'push', label: 'Save & Exit', value: 'save' }
        ]
    });

    toolbar.addListener('buttonClick', toolbarButtonClicked);
}

function toolbarButtonClicked(ev) {
    var label = ev.button.value;
    if(label == "selectAllPage") {
    	var records = paginator.getPageRecords();
    	for(var i=records[0];i<records[1];i++) {
    		var row = myDT.getTrEl(i);
    		var chkBox = row.cells[0];
    	}
    }      
}

function makeDataTable(url, params) {
	var myColumnDefs = [
	                    {key:'',formatter:YAHOO.widget.DataTable.formatCheckbox, width: 10 },
	                    {key:"organism", label:"Organism", sortable:true},
	                    {key:"name", label:"Name", sortable:true},
	                    {key:"product", label:"Product",sortable:true,minWidth:500}
	                ];

	                this.myDataSource = new YAHOO.util.DataSource(url);
	                this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
	                this.myDataSource.responseSchema = {
	                    resultsList: "hits",
	                    fields: ["organism","name","product"]
	                };
	                
	                paginator = new YAHOO.widget.Paginator({ 
	        				            rowsPerPage: 25,
	        				            template : "{PreviousPageLink} <span>{CurrentPageReport}</span> {NextPageLink}",
	        				            previousPageLinkLabel : '&lt;',
	        				            nextPageLinkLabel : '&gt;',
	        				            pageReportTemplate : 'Showing records <strong>{startRecord} - {endRecord}</strong> of {totalRecords}'
	        				        });
	                
	                var oConfigs = { 
	        	                paginator: paginator,  
	        	                initialRequest: params,
	        	                width: 600
	        	        }; 	 
	                		
	                myDT = this.myDataTable = new YAHOO.widget.DataTable("historyEdit", myColumnDefs,
	                        this.myDataSource,oConfigs );
	                
	                this.myDataTable.subscribe("checkboxClickEvent", checkboxClicked);                
}

function checkboxClicked(e) {
    var checkbox = e.target;
    var record = this.getRecord(checkbox);
    var name = record.getData("name");
    if (checkbox.checked) {
    	this.selectRow(record);
    	ids[ids.length++] = name;
    	var t = new ArrayList();
    	t.add(name);
    } else {
    	this.unselectRow(record);
    }
}