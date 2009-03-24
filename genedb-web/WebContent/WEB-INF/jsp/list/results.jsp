<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Number of TM domains Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>

<c:set var="queryName" value="proteinLength"></c:set>

<c:import url="/WEB-INF/jsp/queries/${queryName}.jspf"></c:import>

<jsp:include page="/WEB-INF/jsp/queries/${queryName}.jspf" />

<br><query:results />
<format:footer />