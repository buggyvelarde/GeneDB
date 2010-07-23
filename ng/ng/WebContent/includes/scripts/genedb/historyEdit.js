
var ids = new ArrayList();
var paginator;
var myDT;
var base;
var hist;
var done = false;
var saveOptions;

function initHistoryEdit(b,h) {
	base = b;
	hist = h;
	var params = "?columns=organism.commonName,uniqueName,product&outputFormat=HTML&json=true&historyItem=" + hist;
	var url = base + "DownloadFeatures";

	makeDataTable(url,params);

	makeButtons();
}


function makeButtons() {
	var selectAll = new YAHOO.widget.Button({type: "push",label:"Select All",value:"selectAll",
							id:"selectAll",container: "historyEditActionButtons"});

	selectAll.addListener("click", onSelectAllClicked);

	var unselectAll = new YAHOO.widget.Button({type: "push",label:"Deselect All",value:"unselectAll",
							id:"unselectAll",container: "historyEditActionButtons"});

	unselectAll.addListener("click", onSelectAllClicked);

	var deleteSelected = new YAHOO.widget.Button({type: "push",label:"Delete items",value:"delete",
								id:"delete",container: "historyEditManipulateButtons"});

	deleteSelected.addListener("click", onDeleteClicked);


	var save = new YAHOO.widget.Button({type: "push",label:"Replace original query",value:"save",
					id:"save",container: "historyEditSaveButtons"});

	save.addListener("click", onSaveClicked);

	var newList = new YAHOO.widget.Button({type: "push",label:"Create new list",value:"newlist",
					id:"newlist",container: "historyEditSaveButtons"});

	newList.addListener("click", onSaveClicked);
}

function onSelectAllClicked(e) {
    	var records = paginator.getPageRecords();
		for(var i=records[0]; i<=records[1];i++) {
			var record = myDT.getRecord(i);
			var data = record.getData();
			if(e.currentTarget.id == "selectAll") {
				data.checkbox = true;
				myDT.selectRow(record);
				ids.add(data.name);
			} else {
				data.checkbox = false;
				myDT.unselectRow(record);
				ids.remove(data.name);
			}
			myDT.updateRow(record,data);
		}
		var div = document.getElementById("selection");
		div.innerHTML = ids.toStringWithSpace();
		myDT.refreshView();
}

function onDeleteClicked(e) {
    	var rows = myDT.getSelectedRows();
    	for(var i=0;i<rows.length;i++) {
    		var record = myDT.getRecord(rows[i]);
    		myDT.deleteRow(record);
    		ids.remove(record.getData().name);
    	}
    	var div = document.getElementById("selection");
		div.innerHTML = ids.toStringWithSpace();
}

function onInvertSelectClicked(e) {
	if(ids.length() > 0) {
		var records = paginator.getPageRecords();
		for(var i=records[0]; i<=records[1];i++) {
			var record = myDT.getRecord(i);
			var data = record.getData();
			if(data.checkbox) {
				data.checkbox = false;
				myDT.unselectRow(record);
				ids.remove(data.name);
			} else {
				data.checkbox = true;
				myDT.selectRow(record);
				ids.add(data.name);
			}
			myDT.updateRow(records[i],data);
		}
		var div = document.getElementById("selection");
		div.innerHTML = ids.toStringWithSpace();
		myDT.refreshView();
	}
}

var callback =
{
	success:function(o){alert ("success")},
	failure:function(o){}
};

function onSaveClicked(e) {
	var type = e.currentTarget.id;
	var url = base + "History/AddItem?history=" + hist + "&ids=" + ids.toString();
	if(type == 'save') {
		url += "&type=MODIFY";

	} else {
		url += "&type=NEW";
	}

	var div = document.getElementById('img');
	div.innerHTML = "<img src=\"" + base  + "includes/yui/build/assets/skins/sam/treeview-loading.gif\"></img>";

	var request = YAHOO.util.Connect.asyncRequest('GET', url,callback);

	window.location = base + "History/View";
}

function selectRows(element) {
	YAHOO.util.UserAction.click(element,myDT);
}

function checkElements(element) {
	if(element.type == "checkbox") {
		element.checked = true;
		return true;
	}

	return false;
}

function makeDataTable(url, params) {
	var myColumnDefs = [
	                    {key:"checkbox",label:'',formatter:YAHOO.widget.DataTable.formatCheckbox, width: 10 },
	                    {key:"organism", label:"Organism", sortable:true},
	                    {key:"name", label:"Name", sortable:true},
	                    {key:"product", label:"Product",sortable:true,minWidth:500}
	                ];

	                this.myDataSource = new YAHOO.util.DataSource(url);
	                this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
	                this.myDataSource.responseSchema = {
	                    resultsList: "hits",
	                    fields: ["checkbox","organism","name","product"]
	                };

	                paginator = new YAHOO.widget.Paginator({
                					alwaysVisible: false,
                					rowsPerPage: 25,
        				            template : "{PreviousPageLink} <span>{CurrentPageReport}</span> {PageLinks} {NextPageLink}",
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
    var div = document.getElementById("selection");
    var checkbox = e.target;
    var record = this.getRecord(checkbox);
    var data = record.getData();
    if (checkbox.checked) {
    	this.selectRow(record);
    	ids.add(data.name);
    	data.checkbox = true;
    } else {
    	this.unselectRow(record);
    	ids.remove(data.name);
    	data.checkbox = false;
    }
    div.innerHTML = ids.toStringWithSpace();
    myDT.updateRow(record,data);
}
