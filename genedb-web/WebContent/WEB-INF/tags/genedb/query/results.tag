<%@ tag display-name="results"
        body-content="scriptless" %>
<%@ attribute name="name" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>

<c:forEach var="parameter" items="${paramValues}">
    <c:forEach var="value" items="${parameter.value}">
        <c:set var="requestParams" value="${requestParams}&${parameter.key}=${value}"/>
    </c:forEach>
</c:forEach>


<c:if test="${runQuery}">
    <br>
    <c:url value="BrowseTerm" var="url">
        <c:param name="category" value="${category}"/>
    </c:url>

    <div id="geneResultsPanel">
        <format:genePageSection  className="whiteBox">
            <display:table name="sessionScope.results"  id="row" pagesize="30" requestURI="/Query" class="simple" cellspacing="0" cellpadding="4">
                   <display:column title="Systematic ids">
                    <a href="<c:url value="/NamedFeature"/>?name=${row.systematicId}&index=${row_rowNum-1}&lastIndex=${fn:length(results)-1}&${requestParams}"><c:out value="${row.systematicId}"/></a>
                   </display:column>
                <display:column title="Organism">
                    <i>${row.taxonDisplayName}</i>
                   </display:column>
                   <display:column title="Product">
                    ${row.product}
                </display:column>
                <%--
                <display:column title="Contig">
                    <i>${row.topLevelFeatureName}</i>
                </display:column>
                <display:column title="left">
                    <i>${row.left}</i>
                </display:column>
                --%>
            </display:table>
        </format:genePageSection>
    </div>
</c:if>
