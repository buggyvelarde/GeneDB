<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<c:url value="/" var="base"/>
<format:headerRound title="Gene Results List" bodyClass="genePage" onLoad="initList('${base}','${args}');">
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
<div id="geneDetails">
	<format:genePageSection id="listResults" className="whiteBox">
	<c:if test="${results != null}">
		<display:table name="results" uid="tmp" pagesize="30" requestURI="/Orthologs" class="simple" cellspacing="0" cellpadding="4">
			<display:column property="organism.abbreviation" title="Organism"/>
			<display:column property="cvTerm.name" title="Type"/> 
			<display:column property="uniqueName" href="./NamedFeature" paramId="name"/>
		</display:table>
	</c:if>
	<c:if test="${features != null}">
		<display:table name="features" uid="tmp" pagesize="30" requestURI="/GenesByCvTermAndCv" class="simple" cellspacing="0" cellpadding="4">
			<display:column property="organismName" title="Organism"/>
			<display:column property="geneName" href="./NamedFeature" paramId="name"/>
		</display:table>
	</c:if>
	<c:if test="${luceneResults != null}">
		<display:table name="luceneResults" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
			<display:column property="organism" title="Organism"/>
			<display:column property="name" href="./NamedFeature" paramId="name"/>
			<display:column property="product" title="Product"/>
		</display:table>
	</c:if>
	<c:if test="${luceneResults == null && features == null && results == null}">
		<div id="list" style="clear: both;"></div>
	</c:if>
	</format:genePageSection>
</div>

<format:footer />