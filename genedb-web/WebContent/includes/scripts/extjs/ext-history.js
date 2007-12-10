/*
 * Ext JS Library 1.1
 * Copyright(c) 2006-2007, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://www.extjs.com/license
 */

Ext.onReady(function(){

    // create the Data Store
    var ds = new Ext.data.Store({
        // load using script tags for cross domain, if the data in on the same domain as
        // this page, an HttpProxy would be better
        proxy: new Ext.data.HttpProxy({
            url: 'http://localhost:8080/genedb-web/History/Data'
        }),

        // create reader that reads the Topic records
        reader: new Ext.data.JsonReader({
        	root: 'queries',
        	totalProperty: 'total',
        	id: 'id',
        	fields: [
            {name: 'index',mapping: 'index', type: 'int'},
            {name: 'name', mapping: 'name'},
            {name: 'type', mapping: 'type'},
            {name: 'noresults',mapping: 'noresults',type: 'int'},
            {name: 'tools',mapping:'tools'},
            {name: 'download',mapping:'download'}
        ]}),

        // turn on remote sorting
        remoteSort: true
    });
	

    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store
    var cm = new Ext.grid.ColumnModel([{
           id: 'index', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
           header: "Index",
           dataIndex: 'index'
        },{
           id: 'name',
           header: "Name",
           dataIndex: 'name',
           editor: new Ext.form.TextField({
               allowBlank: false
           }),
        },{
        	id: 'type',
        	header: "Type",
        	dataIndex: 'type'
        },{
        	id: 'results',
        	header: "No. Of Results",
        	dataIndex: 'noresults'
        },{
        	id: 'tools',
        	header: "Tools",
        	dataIndex: 'tools'
        },{
        	id: 'download',
        	header: "Download",
        	dataIndex: 'download',
        	renderer: link
        }]);
	
	cm.defaultSortable = true;
	
	function link(value) {
		return String.format('<a href="{0}">Download</a>',value);
	}
	function formatBoolean(value){
        return value ? 'Yes' : 'No';  
    }
    // create the editor grid
    var grid = new Ext.grid.EditorGridPanel({
        store: ds,
        cm: cm,
        title: 'History View',
        renderTo: 'topic-grid',
        autoHeight : true,
        height: 'auto',
        enableColumnHide : true,
        enableColumnMove : true,
        enableHdMenu : true,
        stripeRows: true,
        selModel: new Ext.grid.CellSelectionModel({singleSelect:true}),
        loadMask: true,
        bbar: new Ext.PagingToolbar({
            pageSize: 25,
            store: ds,
            displayInfo: true,
            displayMsg: 'Displaying history items {0} - {1} of {2}',
            emptyMsg: "No history items to display"
        })
    });
	
	grid.on('afteredit',function(e){
		var field = e.field;
		//var id = e.grid.colModel.config[field].id;
		var row = e.row;
		var value = e.value;
		if(field=="download") {
			row++;
			window.location="http://localhost:8080/genedb-web/DownloadFeatures?historyItem=" + row;	
		} else {
			ds.load({params:{change:true,row:row,value:value}});
		}
	});

    // render it
    grid.render();
    
    // trigger the data store load
    ds.load({params:{change:'false',start:0,limit:25}});
    
});

    