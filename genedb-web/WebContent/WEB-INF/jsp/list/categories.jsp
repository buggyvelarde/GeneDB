<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<c:url value="/Search" var="url">
	<c:param name="organism" value="${organism}"/>
	<c:param name="category" value="${category}"/>
</c:url>
<div id="geneDetails">
	<format:genePageSection className="whiteBox">
		<display:table name="results"  id="row" pagesize="30" requestURI="/BrowseCategory" class="simple" cellspacing="0" cellpadding="4">
		   	<display:column title="Category - ${category}">
		   		<c:url value="${url}" var="final">
					<c:param name="term" value="${row.name}"/>
		   		</c:url>
				<a href="<c:url value="/Query?q=controlledCuration&cvTermName=${row.name}&cvName=${category}"/>"><c:out value="${row.name}"/></a>
		   	</display:column>
		   	<display:column property="count" title="Count"/>
		</display:table>
	</format:genePageSection>
</div>
<format:footer />