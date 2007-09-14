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
        	id: 'id'
        }, [
            {name: 'index',mapping: 'index'},
            {name: 'name', mapping: 'name'},
            {name: 'type', mapping: 'type'},
            {name: 'noresults',mapping: 'noresults'},
            {name: 'tools',mapping:'tools'},
            {name: 'download',mapping:'download'}
        ]),

        // turn on remote sorting
        remoteSort: true
    });
	
	
	var fm = Ext.form, Ed = Ext.grid.GridEditor;
	
    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store
    var cm = new Ext.grid.ColumnModel([{
           id: 'index', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
           header: "Index",
           dataIndex: 'index',
           width: 50,
           css: 'white-space:normal;'
        },{
           id: 'name',
           header: "Name",
           dataIndex: 'name',
           editor: new Ed(new fm.TextField({
               allowBlank: false
           })),
           width: 300
        },{
        	id: 'type',
        	header: "Type",
        	dataIndex: 'type',
        	width: 75
        },{
        	id: 'results',
        	header: "No. Of Results",
        	dataIndex: 'noresults',
        	width: 75
        },{
        	id: 'tools',
        	header: "Tools",
        	dataIndex: 'tools',
        	width: 75
        },{
        	id: 'download',
        	header: "Download",
        	dataIndex: 'download',
        	renderer: link,
        	width: 75
        }]);
	
	function link(value) {
		return String.format('<a href="{0}">Download</a>',value);
	}
	function formatBoolean(value){
        return value ? 'Yes' : 'No';  
    }
    // create the editor grid
    var grid = new Ext.grid.EditorGrid('topic-grid', {
        ds: ds,
        cm: cm,
        selModel: new Ext.grid.CellSelectionModel({singleSelect:true}),
        enableColLock:false,
        loadMask: true
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
	})
    // make the grid resizable, do before render for better performance
    var rz = new Ext.Resizable('topic-grid', {
        wrap:true,
        minHeight:100,
        pinned:true,
        handles: 's'
    });
    rz.on('resize', grid.autoSize, grid);

    // render it
    grid.render();

    var gridFoot = grid.getView().getFooterPanel(true);
    
    // trigger the data store load
    ds.load({params:{change:'false'}});

});
