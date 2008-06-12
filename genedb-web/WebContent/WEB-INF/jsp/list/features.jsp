<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>

<format:headerRound title="Gene Results List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection id="listResults" className="whiteBox">
	<c:if test="${results != null}">
		<display:table name="results" uid="tmp" pagesize="30" requestURI="/NamedFeature" class="simple" cellspacing="0" cellpadding="4">
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
	</format:genePageSection>
</div>
<format:footer />