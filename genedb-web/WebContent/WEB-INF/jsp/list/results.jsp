<%@ include file="/WEB-INF/jsp/topinclude.jspf" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<format:headerRound title="Number of TM domains Search">
    <st:init />
    <link rel="stylesheet" type="text/css" href="<c:url value="/includes/style/genedb/genePage.css"/>" />
</format:headerRound>

<c:set property="queryName" value="proteinLength"></c:set>

<jsp:include page="/WEB-INF/jsp/queries/${queryName}.jspf" />

<br><query:results />
<format:footer />