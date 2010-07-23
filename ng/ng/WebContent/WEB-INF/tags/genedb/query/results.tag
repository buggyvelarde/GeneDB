<%@ tag display-name="results"
        body-content="scriptless" %>
<%@ attribute name="name" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting" %>
<%@ taglib prefix="db" uri="db" %>
<%@ taglib prefix="misc" uri="misc" %>


<c:if test="${not empty resultsSize}">
  <div id="col-2-1">
  <c:if test="${isMaxResultsReached}">
    <font color="red">Please note that these search results are limited by a maximum of ${resultsSize}.</font>
    <br/>
  </c:if>
  <display:table name="results"  id="row" pagesize="30" requestURI="/Results/${key}" class="search-data-table" sort="external" cellspacing="0" cellpadding="4" partialList="true" size="${resultsSize}">
    <display:column title="Systematic ids" style="width: 100px;">
      <a href="<misc:url value="/ResultsNavigator/${key}?index=${row_rowNum+firstResultIndex-1}&resultsLength=${fn:length(results)-1}"/>">${row.displayId}</a>
    </display:column>
    <display:column title="Organism" style="width: 150px;">
      <i><db:taxonname label="${row.taxonDisplayName}" taxonNameType="HTML_SHORT"/></i>
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
</div>
</c:if>
