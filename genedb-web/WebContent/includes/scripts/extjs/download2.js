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
            url: 'http://localhost:8080/genedb-web/Search/test'
        }),

        // create reader that reads the Topic records
        reader: new Ext.data.JsonReader({
        	root: 'names',
        	totalProperty: 'total',
        	id: 'id'
        }, [
            {name: 'name', mapping: 'name'},
            {name: 'email', mapping: 'email'}
        ]),

        // turn on remote sorting
        remoteSort: true
    });


    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store
    var cm = new Ext.grid.ColumnModel([{
           id: 'name', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
           header: "Name",
           dataIndex: 'name',
           width: 490,
           css: 'white-space:normal;'
        },{
           header: "email",
           dataIndex: 'email',
           width: 150
        }]);

    

    // create the editor grid
    var grid = new Ext.grid.Grid('topic-grid', {
        ds: ds,
        cm: cm,
        selModel: new Ext.grid.RowSelectionModel({singleSelect:true}),
        enableColLock:false,
        loadMask: true
    });

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

    // add a paging toolbar to the grid's footer
    var paging = new Ext.PagingToolbar(gridFoot, ds, {
        pageSize: 2,
        displayInfo: true,
        displayMsg: 'Displaying topics {0} - {1} of {2}',
        emptyMsg: "No topics to display"
    });
    // add the detailed view button
    paging.add('-', {
        pressed: true,
        enableToggle:true,
        text: 'Detailed View',
        cls: 'x-btn-text-icon details',
        toggleHandler: toggleDetails
    });

    // trigger the data store load
    ds.load({params:{start:0, limit:2}});

    function toggleDetails(btn, pressed){
        var count = cm.getColumnCount();
        var hidden = new Array();
        var i=0;
        for(var index=0; index<count; index++) {
        	var id = cm.getColumnId(index);
        	if(!cm.getColumnById(id).isHidden()) {
        		hidden[i] = id;
        	}
        }
        ds.load({params:{start:0, limit:25,columns:hidden.join(',')}});
        cm.getColumnById('topic').renderer = pressed ? renderTopic : renderTopicPlain;
        cm.getColumnById('last').renderer = pressed ? renderLast : renderLastPlain;
        grid.getView().refresh();
    }
});
