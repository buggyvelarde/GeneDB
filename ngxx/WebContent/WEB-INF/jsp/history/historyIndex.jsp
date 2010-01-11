<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<format:headerRound title="HistoryView" onLoad="initHistory('${base}')">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/fonts/fonts-min.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/datatable/assets/skins/sam/datatable.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/yui/build/button/assets/skins/sam/button.css"/>" />
    <link rel="stylesheet" type="text/css" href="/includes/yui/build/paginator/assets/skins/sam/paginator.css">
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/json/json-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/element/element-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datasource/datasource-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/datatable/datatable-min.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/button/button-min.js"/>"></script>
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
    <link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/resultsPage.css"/>" />
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/history.js"/>"></script>
    <script language="javascript" type="text/javascript" src="<misc:url value="/includes/yui/build/connection/connection-min.js"/>"></script>
    <script type="text/javascript" src="<misc:url value="/"/>includes/yui/build/paginator/paginator-min.js"></script>
    <link rel="stylesheet" href="<misc:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:headerRound>
<br>
<div id="historyView" align="center"/></div>
<br>
<div id="logErrors" align="center" style="color:red;">
</div>
<format:footer/>
