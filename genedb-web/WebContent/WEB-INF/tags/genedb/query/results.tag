<%@ tag display-name="results"
        body-content="scriptless" %>
<%@ attribute name="name" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>


<c:if test="${runQuery}">
	<br>
	<c:url value="BrowseTerm" var="url">
		<c:param name="category" value="${category}"/>
	</c:url>
	<div id="geneDetails">
		<format:genePageSection className="whiteBox">
			<display:table name="results"  id="row" pagesize="30" requestURI="/Query" class="simple" cellspacing="0" cellpadding="4">
		   		<display:column title="Systematic ids">
					<a href="<c:url value="/NamedFeature"/>?name=${row[0]}"><c:out value="${row[0]}"/></a>
			   	</display:column>
			    <display:column title="Organism ">
					<i>${row[1]}</i>
			   	</display:column>
			</display:table>
		</format:genePageSection>
	</div>
</c:if>