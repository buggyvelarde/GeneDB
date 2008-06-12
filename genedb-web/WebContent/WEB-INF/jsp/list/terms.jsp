<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:headerRound title="Gene Results List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<div id="geneDetails">
	<format:genePageSection className="whiteBox">
		<display:table name="results" uid="tmp" id="row" pagesize="30" requestURI="${controllerPath}" class="simple" cellspacing="0" cellpadding="4">
			<display:column property="uniqueName" title="Id" href="NamedFeature?" paramId="name"/>
		   	<display:column property="organism.abbreviation" title="Organism"/>
		   	<display:column property="cvTerm.name" title="Type"/>
		</display:table>
	</format:genePageSection>
</div>
<format:footer />