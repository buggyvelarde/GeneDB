<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<c:url value="/" var="base"/>
<format:headerRound title="HistoryView" onLoad="initHistory('${base}')">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/yui/build/fonts/fonts-min.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/yui/build/datatable/assets/skins/sam/datatable.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/yui/build/button/assets/skins/sam/button.css"/>" />
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/json/json-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/element/element-beta-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/datasource/datasource-beta-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/datatable/datatable-beta-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/button/button-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/resultsPage.css"/>" />
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/history.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<c:url value="/includes/yui/build/connection/connection-min.js"/>"></script>
    <link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:headerRound>
<br>
<div id="historyView" align="center"/></div>
<br>
<div id="logErrors" align="center" style="color:red;">
</div>
<format:footer/>
