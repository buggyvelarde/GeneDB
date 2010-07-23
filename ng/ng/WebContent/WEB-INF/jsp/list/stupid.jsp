<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<format:headerRound title="Category List" bodyClass="genePage">
	<st:init />
	<link rel="stylesheet" type="text/css" href="<misc:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>
<misc:url value="BrowseTerm" var="url">
	<string:param name="category" value="${category}"/>
</misc:url>
<div id="geneDetails">
	<format:genePageSection className="whiteBox">
		<display:table name="results"  id="row" pagesize="30" requestURI="/Query" class="simple" cellspacing="0" cellpadding="4">
		   	<display:column title="Systematic ids">
		   		<misc:url value="${url}" var="final">
					<spring:param name="term" value="${row}"/>
		   		</misc:url>
				<a href="<misc:url value="/NamedFeature"/>?name=${row}"><c:out value="${row}"/></a>
		   	</display:column>
		</display:table>
	</format:genePageSection>
</div>
<format:footer />