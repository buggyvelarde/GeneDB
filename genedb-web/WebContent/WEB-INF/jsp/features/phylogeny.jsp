<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<format:header name="Phylogeny">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
${nodes}
<script type="text/javascript" src="/genedb-web/includes/scripts/phylogeny.js"/>
<format:footer/>