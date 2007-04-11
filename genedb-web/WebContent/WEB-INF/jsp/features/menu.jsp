<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="sp" uri="http://www.springframework.org/tags/form" %>

<format:header name="Phylogeny Tree">
	<st:init />
	<link rel="stylesheet" href="<c:url value="/"/>includes/style/alternative.css" type="text/css"/>
</format:header>
<table>
<tr>
<td>
<db:phylogeny left="200" top="76"></db:phylogeny>
<script type="text/javascript" src="/genedb-web/includes/scripts/phylogeny.js"/></script>
</td>
</tr>
</table>
