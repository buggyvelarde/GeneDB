function initHistory(base) {
	var url = base + "History";
	

	var history = new function() {

        this.formatDownloadButton = function(elCell, oRecord, oColumn, sData) {
        	var num = oRecord._sId.replace("yui-rec","");
        	num++;
        	elCell.innerHTML = "<span id=\"button" + oRecord._sId + "\" class=\"yui-button yui-link-button\">" +
	        						"<span class=\"first-child\">" +
	        							"<a href=\"" + base + "History/Download?history=" + num + "\">Download</a>" +
	        						"</span>" +
        						"</span>"; 
        };
        
        this.formatViewButton = function(elCell, oRecord, oColumn, sData) {
        	var num = oRecord._sId.replace("yui-rec","");
        	num++;
        	elCell.innerHTML = "<span id=\"button-view-" + oRecord._sId + "\" class=\"yui-button yui-link-button\">" +
	        						"<span class=\"first-child\">" +
	        							"<a href=\"" + base + "History/EditHistory?history=" + num + "\">View/Edit</a>" +
	        						"</span>" +
        						"</span>"; 
        };
        
        this.formatRemoveButton = function(elCell, oRecord, oColumn, sData) {
        	var num = oRecord._sId.replace("yui-rec","");
        	num++;
        	elCell.innerHTML = "<span id=\"button-view-" + oRecord._sId + "\" class=\"yui-button yui-link-button\">" +
	        						"<span class=\"first-child\">" +
	        							"<a href=\"" + base + "History/EditHistory?remove=true&history=" + num + "\">Remove</a>" +
	        						"</span>" +
        						"</span>"; 
        };
        
        var myColumnDefs = [
            {key:"historyType", label:"History Type", sortable:true,minWidth:30},
            {key:"name", label:"Name", sortable:true,minWidth:300},
            {key:"numberItems", label:"No. of Results",minWidth:30},
            {label:"Download",formatter:this.formatDownloadButton,minWidth:60},
            {label:"View/Edit",formatter:this.formatViewButton,minWidth:60},
            {label:"Remove",formatter:this.formatRemoveButton,minWidth:60}
        ];

        this.myDataSource = new YAHOO.util.DataSource(url);
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        this.myDataSource.responseSchema = {
            resultsList: "items",
            fields: ["historyType","name","numberItems"]
        };
        
        var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    alwaysVisible: false,
	                	rowsPerPage: 25,
	                    template : "{PreviousPageLink} <span>{CurrentPageReport}</span> {PageLinks} {NextPageLink}",
	                    previousPageLinkLabel : '&lt;',
	                    nextPageLinkLabel : '&gt;',
	                    pageReportTemplate : 'Showing records <strong>{startRecord} - {endRecord}</strong> of {totalRecords}'
	                }),  
	                initialRequest: "/viewData",
	                width: 600
	        }; 	 
        
        YAHOO.widget.DataTable.MSG_EMPTY = "No History Items Found.";
        
        this.myDataTable = new YAHOO.widget.DataTable("historyView", myColumnDefs,
                this.myDataSource,oConfigs );
    };
}