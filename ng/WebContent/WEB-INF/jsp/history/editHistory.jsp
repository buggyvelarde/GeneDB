<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:headerRound title="HistoryView" onLoad="initHistoryEdit('${base}','${history}')">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/fonts/fonts-min.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/datatable/assets/skins/sam/datatable.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/button/assets/skins/sam/button.css"/>" />
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/connection/connection-min.js"/>"></script>

    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/json/json-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/element/element-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datasource/datasource-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/editor/editor-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datatable/datatable-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/button/button-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/logger/logger-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/yuitest/yuitest-min.js"/>"></script>

    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/resultsPage.css"/>" />
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/ArrayList.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/historyEdit.js"/>"></script>
    <link rel="stylesheet" href="<misc:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:headerRound>
<div id="queryDetails" align="left">
    <span style="font-size:1.3em;">
        Query Name: <i><b>${historyName}</b></i>
    </span>
    <br>
    <div style="float: left;width: 300px;">
        <span style="font-size:1.3em;">Query Details:</span>
    </div>
    <div id="details">
        <table>
            <tr>
                <td>Organisms:</td>
                <td>${organism}</td>
            </tr>
            <tr>
                <td>Type:</td>
                <td>Indexed List</td>
            </tr>
            <tr>
                <td>Fields:</td>
                <td>${category}</td>
            </tr>
            <tr>
                <td>Term:</td>
                <td>${term}</td>
            </tr>
        </table>
    </div>
</div>
<div id="historyEditLeft" style="clear:both;">
    <div id="historyEditButtons" align="center">
        <br>
        <div id="historyEditSelect">
            <div id="select" style="float:left;">Selection: </div>
            <div id="historyEditActionButtons" style="float:right;">
            </div>
            <br><br>
            <div id="manipulate" style="clear:both;float:left;">Manipulate: </div>
            <div id="historyEditManipulateButtons" style="float:right;">
            </div>
            <br><br>
            <div id="forSaveButtons">
                <div style="float:left">
                    <span style="text-align: left">Save List:</span>
                </div >
                <div id="historyEditSaveButtons" style="float:right">

                </div>
            </div>
            <div id="temp" style="clear:both;"></div>
        </div>
    </div>
    <br>
    <div id="historyEditSelectedId" align="center">
        <span>Selected Ids</span>
        <br>
        <div id="selection">
        </div>
    </div>
</div>
<div id="historyEdit" align="center"></div>
<div id="img" align="center" style="clear: both;"></div>
<format:footer/>