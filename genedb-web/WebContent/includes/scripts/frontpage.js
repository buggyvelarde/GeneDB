var ds;
Ext.onReady(function(){

        Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
       
       ds = new Ext.data.Store({
        // load using script tags for cross domain, if the data in on the same domain as
        // this page, an HttpProxy would be better
        proxy: new Ext.data.HttpProxy({
            url: 'http://localhost:8080/genedb-web/NamedFeature'
        }),

        // create reader that reads the Topic records
        reader: new Ext.data.JsonReader({
        	root: 'results',
        	totalProperty: 'total',
        	id: 'id',
        	fields: [
            {name: 'organism',mapping: 'organism'},
            {name: 'type', mapping: 'type'},
            {name: 'name', mapping: 'name'}
        ]}),

        // turn on remote sorting
        remoteSort: true
    });
    
    var cm = new Ext.grid.ColumnModel([{
           id: 'organism', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
           header: "Organism",
           dataIndex: 'organism'
        },{
           id: 'name',
           header: "Name",
           dataIndex: 'name',
           editor: new Ext.Button({
           		handler : clicked(),
           }),
           renderer: link
        },{
        	id: 'type',
        	header: "Type",
        	dataIndex: 'type'
        }]);
	
	cm.defaultSortable = true;
    
    function clicked(value) {
    	Ext.MessageBox.alert('Status', value);
    }
    
    function link(value) {
		return String.format('<a href="./Search/FeatureByName?name={0}">{0}</a>',value);
	}
	
    var grid = new Ext.grid.EditorGridPanel({
        store: ds,
        cm: cm,
        renderTo: 'results-grid',
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
    
    grid.on('cellclick',function(grid, rowIndex, columnIndex, e){
    	if(columnIndex == 1) {
    		var record = grid.getStore().getAt(rowIndex);  // Get the Record
        	var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        	var data = record.get(fieldName);
        	//Ext.MessageBox.alert('Data', data);
        	var result = document.getElementById("results-grid");
        	result.style.display = 'none';
        	var query = document.getElementById("query-form");
        	query.style.display = 'none';
        	var show = document.getElementById("gene-page");
        	show.innerHTML = '<iframe src="./Search/FeatureByName?name=' + data + '" width="100%" height="100%" />';
        	show.style.visibility = 'visible';
    	}
    });
    
    grid.render();
    
    
       var viewport = new Ext.Viewport({
            layout:'border',
            items:[
                new Ext.BoxComponent({ // raw
                    region:'north',
                    el: 'north',
                    height: 140
                }),{
                    region:'south',
                    contentEl: 'south',
                    split:true,
                    height: 100,
                    minSize: 100,
                    maxSize: 200,
                    collapsible: true,
                    margins:'0 0 0 0'
                }, {
                    region:'east',
                    title: 'News',
                    collapsible: true,
                    split:true,
                    width: 225,
                    minSize: 175,
                    maxSize: 400,
                    layout:'fit',
                    margins:'0 5 0 0',
                    items:
                        new Ext.TabPanel({
                            border:false,
                            activeTab:1,
                            tabPosition:'bottom',
                            items:[{
                                html:'<p>A TabPanel component can be a region.</p>',
                                title: 'A Tab',
                                autoScroll:true
                            },
                            new Ext.grid.PropertyGrid({
                                title: 'Property Grid',
                                closable: true,
                                source: {
                                    "(name)": "Properties Grid",
                                    "grouping": false,
                                    "autoFitColumns": true,
                                    "productionQuality": false,
                                    "created": new Date(Date.parse('10/15/2006')),
                                    "tested": false,
                                    "version": .01,
                                    "borderWidth": 1
                                }
                            })]
                        })
                 },{
                    region:'west',
                    id:'west-panel',
                    title:'Links',
                    split:true,
                    width: 200,
                    minSize: 175,
                    maxSize: 400,
                    collapsible: true,
                    margins:'0 0 0 5',
                    layout:'accordion',
                    layoutConfig:{
                        animate:true
                    },
                    items: [{
                        contentEl: 'west',
                        title:'Navigation',
                        border:false,
                        iconCls:'nav'
                    },{
                        title:'Search',
                        html:'<p>Some search links in here.</p>',
                        border:false,
                        iconCls:'settings'
                    },{
                        title:'Examples',
                        html:'<p>Some examples in here.</p>',
                        border:false,
                        iconCls:'settings'
                    }]
                },
                new Ext.TabPanel({
                    region:'center',
                    deferredRender:false,
                    activeTab:0,
                    items:[{
                        contentEl:'center2',
                        autoScroll:true
                    }]
                })
             ]
        });
    });
    
    function doSomething() {
		var obj = document.getElementById("start");
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		var items = document.getElementById("itemsLength");
		ilength = items.getAttribute("value");

		for (var i=0; i< ilength; i++) {
			var elementID = "mi_0_" + i;
			var element = document.getElementById(elementID);
			var depth = element.getAttribute("name").split('_');
			element.style.left = ( depth.length * 154 ) + 1;
			var top = -1;
			for (j=0;j<depth.length;j++) {
				top = top + depth[j] * 29;
 			}
 			element.style.top = top;
		}
		//alert(curleft);
		//alert(curtop);
	}

	function mouseclick(id) {
		var selected = document.getElementById("selected");
		selected.value = '';
		for (var i=0; i< this.ilength; i++) {
			var element = document.getElementById("check_" + i);
			if(element.checked) {
				if (selected.value == '') {
					selected.value =  document.getElementById("menu_" + i).textContent;
				} else {	
					selected.value = selected.value + ',' + document.getElementById("menu_" + i).textContent;
				}
			}	
		}
	}
	
	function resetall() {
		for (var i=0; i< ilength; i++) {
			this.checked[i] = false;
			var element = document.getElementById("menu_" + i);
			element.checked = false;
		}
		var selected = document.getElementById("selected");
		selected.value = '';
		var url = document.URL.split('?');
		window.location = url[0];
	}
	
	function check() {
		var selected = document.getElementById("selected");
		if (selected.value == '') {
			selected.style.border = "solid 1px red";
			alert ("Please select an organism from the tree");
			return false;
		} 
		var org = document.getElementById("textInput");
		if (org.value == '') {
			org.style.border = "solid 1px red";
			alert ("Please enter the search term in the input box");
			return false;
		} 
		
		
	}
	
	function SubmitClicked() {
		var div = document.getElementById("results-grid");
		div.style.visibility = 'visible';
		var orgs = document.getElementById("selected").value;
		var query = document.getElementById("query").value
		ds.load({params:{orgs:orgs,name:query,start:0,limit:25}});
	}