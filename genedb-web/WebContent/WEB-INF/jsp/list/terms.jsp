<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<misc:url value="/" var="base"/>
<misc:url value="?" var="url">
<spring:param name="organism" value="${organism}"/>
<spring:param name="category" value="${category}"/>
<spring:param name="term" value="${term}"/>
<spring:param name="json" value="true"/>
</misc:url>
<format:headerRound title="Gene Results List" bodyClass="genePage" onLoad="makeDataTable('${base}','${url}');">
		<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/YUI-2.5.2/fonts/fonts-min.css"/>" />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/YUI-2.5.2/datatable/assets/skins/sam/datatable.css"/>" />
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/connection/connection-min.js"/>"></script>

	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/json/json-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/element/element-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/datasource/datasource-beta-min.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/YUI-2.5.2/datatable/datatable-beta-min.js"/>"></script>
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/resultsPage.css"/>" />
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/ArrayList.js"/>"></script>
	<script language="javascript" type="text/javascript" src="<misc:url value="/includes/scripts/genedb/list.js"/>"></script>
</format:headerRound>
<c:if test="${controller eq 'BrowseTerm'}">
	<div id="queryName">
		Genes containing CvTerm: <i style="color: blue;">${term}</i>  in Vocabulary: <i style="color: blue;">${category}</i>
	</div>
</c:if>
<c:if test="${controller eq 'AdvancedSearch'}">
	<div id="queryName">
		Genes containing <i style="color: blue;">${term}</i>  in: <i style="color: blue;">${category}</i><br>
		in Organism: <i style="color: blue;">${organism}</i><br>
	</div>
</c:if>
<div id="resultPage" align="center" style="clear:both;">
	<div id="list">
	</div>
</div>
<format:footer />