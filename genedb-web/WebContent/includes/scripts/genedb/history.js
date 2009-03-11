function initHistory(base) {
    var url = base + "History";

    var history = new function() {

        this.formatDownload = function(elCell, oRecord, oColumn) {
            elCell.innerHTML = '<img src="' + base + 'includes/images/save.gif" title="Download Results" />';
            elCell.style.cursor = 'pointer';
        };

        this.formatView = function(elCell, oRecord, oColumn) {
            elCell.innerHTML = '<img src="' + base + 'includes/images/edit.gif" title="Edit Results" />';
            elCell.style.cursor = 'pointer';
        };

        this.formatRemove = function(elCell, oRecord, oColumn) {
            elCell.innerHTML = '<img src="' + base + 'includes/images/delete.gif" title="Remove Query From History" />';
            elCell.style.cursor = 'pointer';
        };

        this.formatName = function(elCell, oRecord, oColumn) {
            elCell.innerHTML = oRecord.getData().name +
                 '<img align="top" src="' + base + 'includes/images/pencil.png" title="Edit" />';
        }

        this.formatSerialNumber = function(elCell, oRecord, oColumn) {
            var num = myDT.getRecordIndex(oRecord);
            num++;
            elCell.innerHTML = num;
            var data = oRecord.getData();
            data.no = num;
            oRecord.setData(data);
        };

        var myColumnDefs = [
            {key:"no", label:"No.",formatter:this.formatSerialNumber},
            {key:"historyType", label:"History Type", sortable:true,minWidth:30},
            {key:"name", editor:"textbox",label:"Name", formatter:this.formatName,sortable:true,minWidth:300},
            {key:"numberItems", label:"No. of Results",minWidth:30},
            {key:"download",label:"Download",formatter:this.formatDownload,minWidth:60},
            {key:"edit",label:"View/Edit",formatter:this.formatView,minWidth:60},
            {key:"delete",label:"Remove",formatter:this.formatRemove,minWidth:60}
        ];

        this.myDataSource = new YAHOO.util.DataSource(url);
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        this.myDataSource.responseSchema = {
            resultsList: "items",
            fields: ["no","historyType","name","numberItems"]
        };

        var oConfigs = {
                    paginator: new YAHOO.widget.Paginator({
                        alwaysVisible: false,
                        rowsPerPage: 25,
                        template : "{PreviousPageLink} <span>{CurrentPageReport}</span> {PageLinks} {NextPageLink}"
                    }),
                    initialRequest: "/viewData",
                    width: 600
            };

        YAHOO.widget.DataTable.MSG_EMPTY = "No History Items Found.";

        var myDT = this.myDataTable = new YAHOO.widget.DataTable("historyView", myColumnDefs,
                this.myDataSource,oConfigs );

        this.highlightEditableCell = function(oArgs) {
            var elCell = oArgs.target;
            var cell = document.getElementById(elCell.id);
            if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) {
                cell.className = 'yui-dt-highlighted';
            }
        };

        var onCellClicked = function(e) {
            var target = e.target;
            var column = myDT.getColumn(target);
            var record = myDT.getRecord(target);
            var h = record.getData().no;
            if (column.key == 'delete') {
                    var url = base + 'History/EditHistory?remove=true&history=' + h;
                    var request = YAHOO.util.Connect.asyncRequest('GET', url,
                        {
                            success: function (o) {
                                myDT.deleteRow(target);
                                myDT.render();
                            },
                            failure: function (o) {
                                alert(o.statusText);
                            },
                            scope:this
                        }
                    );
                    if(request.conn.status == 200) {
                        myDT.deleteRow(target);
                    }
            } else if (column.key == 'edit') {
                window.location = base + 'History/EditHistory?history=' + h;
            }   else if (column.key == 'download') {
                window.location = base + 'History/Download?history=' + h;
            } else {
                myDT.onEventShowCellEditor(e);
            }
        }

        this.myDataTable.subscribe("cellMouseoverEvent", this.highlightEditableCell);
        this.myDataTable.subscribe("cellMouseoutEvent", this.myDataTable.onEventUnhighlightCell);
        this.myDataTable.subscribe("cellClickEvent", onCellClicked);

        var handleSuccess = function(o){
            myDT.resetCellEditor();
            myDT._oRecordSet.updateKey(myDT._oCellEditor.record, myDT._oCellEditor.column.key, newData);
            myDT.formatCell(myDT._oCellEditor.cell);
            myDT.fireEvent("editorSaveEvent",
                            {editor:myDT._oCellEditor, oldData:oldData, newData:newData}
                        );
            myDT._oCellEditor.cell.style.color = 'black';
            myDT.render();
            var div = document.getElementById('logErrors');
            div.innerHTML = '';
        }

        var handleFailure = function(o){
            var div = document.getElementById('logErrors');
            if(o.status == 511) {
                div.innerHTML = "A history item with this name already exists. <BR>" +
                        "Please provide a different name.";
                myDT.resetCellEditor();
                myDT._oCellEditor.cell.style.color = 'red';
                myDT._oCellEditor.record._oData.name = oldData;
                myDT.render();
            } else {
                div.innerHTML = "Some error occurred. Please Try again.";
            }
        }

        var oldData;
        var newData;

        var callback =
        {
          success:handleSuccess,
          failure: handleFailure,
          argument: [oldData,newData]
        };


        myDT.onEventSaveCellEditor = function (oArgs) {
            newData = this._oCellEditor.value;
            var record = this._oCellEditor.record;
            oldData = record.getData(this._oCellEditor.column.key);

            var url = base + "History/EditName?history=" + record.getData().no + "&value=" + newData;
            var request = YAHOO.util.Connect.asyncRequest('GET', url,callback);
        };

    };
}