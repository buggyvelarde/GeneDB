<%@ tag display-name="results"
        body-content="scriptless" %>
<%@ attribute name="name" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>


<c:if test="${not empty resultsSize}">
    <div id="geneResultsPanel">

        <format:genePageSection  className="whiteBox">
            <display:table name="results"  id="row" pagesize="30" requestURI="/Results" class="simple" sort="external" cellspacing="0" cellpadding="4" partialList="true" size="${resultsSize}">
                   <display:column title="Systematic ids">
                    <a href="<c:url value="/ResultsNavigator"/>?index=${row_rowNum-1}&resultsLength=${fn:length(results)-1}&key=${key}"><c:out value="${row.systematicId}"/></a>
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