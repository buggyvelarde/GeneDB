<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<c:url value="/" var="base"/>
<format:headerRound title="HistoryView" onLoad="initHistoryEdit('${base}','${history}')">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/fonts/fonts-min.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/datatable/assets/skins/sam/datatable.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/button/assets/skins/sam/button.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/connection/connection-min.js"/>"></script>
	
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/json/json-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/element/element-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datasource/datasource-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/editor/editor-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datatable/datatable-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/button/button-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/logger/logger-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/yuitest/yuitest-min.js"/>"></script>
	
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/resultsPage.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/ArrayList.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/historyEdit.js"/>"></script>
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:headerRound>
<br>
<div id="queryName" align="center">
<span>Editing History Item ${historyName}</span>
</div>
<br><br>
<div id="historyEditLeft">
	<div id="historyEditButtons" align="center">
		<br>
		<div id="historyEditActionButtons"></div>
		<br>
		<div id="historyEditSaveButtons"></div>
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
	<style type="text/css">
		span {margin: 0.25em}
	</style>
<format:footer/>