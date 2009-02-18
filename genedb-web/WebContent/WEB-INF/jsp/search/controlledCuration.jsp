<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<c:url value="BrowseTerm" var="url">
	<c:param name="category" value="${category}"/>
</c:url>
<div id="geneDetails">
	<format:genePageSection className="whiteBox">
		<display:table name="results"  id="row" pagesize="30" requestURI="/Query" class="simple" cellspacing="0" cellpadding="4">
		   	<display:column title="Systematic ids">
		   		<c:url value="${url}" var="final">
					<c:param name="term" value="${row}"/>
		   		</c:url>
				<a href="<c:url value="/NamedFeature"/>?name=${row}"><c:out value="${row}"/></a>
		   	</display:column>
		</display:table>
	</format:genePageSection>
</div>
<format:footer />