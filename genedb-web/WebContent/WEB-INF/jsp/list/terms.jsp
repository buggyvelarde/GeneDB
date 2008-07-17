<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<c:url value="/" var="base"/>
<c:url value="?" var="url">
<c:param name="organism" value="${organism}"/>
<c:param name="category" value="${category}"/>
<c:param name="term" value="${term}"/>
<c:param name="json" value="true"/>
</c:url> 
<format:headerRound title="Gene Results List" bodyClass="genePage" onLoad="makeDataTable('${base}','${url}');">
		<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/fonts/fonts-min.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/YUI-2.5.2/datatable/assets/skins/sam/datatable.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/connection/connection-min.js"/>"></script>

	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/json/json-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/element/element-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datasource/datasource-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/YUI-2.5.2/datatable/datatable-beta-min.js"/>"></script>
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/resultsPage.css"/>" />
	<script language="javascript" type="text/javascript" src="<c:url value="/includes/scripts/genedb/list.js"/>"></script>
</format:headerRound>
<br>
<div id="queryName" align="center" style="clear: both;">
Genes with Cv: ${category} and CvTerm: ${term}
</div>
<div align="center" style="float:right;font-size: 1.3em;">	
		<span>
			<a href="<c:url value="/"/>History/View">History View</a>
		</span>
</div>
<br><br>
<div id="list" style="clear: both;"></div>
<format:footer />