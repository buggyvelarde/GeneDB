<%@ tag display-name="results" body-content="scriptless"%>
<%@ attribute name="name"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/genedb/formatting"%>
<%@ taglib prefix="db" uri="db"%>
<%@ taglib prefix="misc" uri="misc"%>



<%--
<h2>RESULTS????</h2>
 <c:forEach items="${results}" var="result">
<p>${result.displayId}</p>
</c:forEach>

<p>${resultsSize}</p>
<p>${queryName}</p>
 --%>
<c:if test="${not empty resultsSize}">
	<div id="col-2-1">
		<c:if test="${isMaxResultsReached}">
			<font color="red">Please note that these search results are
				limited by a maximum of ${resultsSize}.</font>
			<br />
		</c:if>

		<!-- requestURI="/Query/${key}" 
		      partialList="true"
              size="${resultsSize}"
              requestURI="/Query/${queryName}"
              sort="external" 
              -->
		<display:table name="results" id="row" pagesize="30"
			size="${resultsSize}" sort="external" partialList="true"
			class="search-data-table" requestURI="/Query/${queryName}"
			cellspacing="0" cellpadding="4">
			<display:column title="Systematic ids" style="width: 100px;">
				<a
					href="<misc:url value="/gene/${row.displayId}"/>">${row.displayId}</a>
			</display:column>
			<display:column title="Organism" style="width: 150px;">
				<i><db:taxonname label="${row.taxonDisplayName}"
						taxonNameType="HTML_SHORT" /> </i>
			</display:column>
			<display:column title="Product">
			     <c:if test="${row.product}">
			         ${row.product}
			     </c:if>
                
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
