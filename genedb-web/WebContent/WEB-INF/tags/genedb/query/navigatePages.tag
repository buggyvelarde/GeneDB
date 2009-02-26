<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:forEach var="parameter" items="${paramValues}">
	<c:forEach var="value" items="${parameter.value}">
		<c:set var="requestParams" value="${requestParams}&${parameter.key}=${value}"/>
	</c:forEach> 
</c:forEach>

&nbsp;Gene ${param.index+1} of ${param.lastIndex+1} Search Results.&nbsp;
<c:choose>
	<c:when test="${param.index != '0'}">
		<a href="ResultsNavigator?index=0&goto=first${requestParams}">First</a>
	</c:when>
	<c:otherwise>
		First
	</c:otherwise>
</c:choose>
&nbsp;|&nbsp;

<c:if test="${param.index != '0'}">
	<a href="ResultsNavigator?index=${param.index}&goto=previous&lastIndex=${param.lastIndex}&q=${param.q}&resultsUri=${param.resultsUri}">Previous</a>
	 &nbsp;|&nbsp;
</c:if>

<c:if test="${param.index != param.lastIndex}">
	<a href="ResultsNavigator?index=${param.index}&goto=next&lastIndex=${param.lastIndex}&q=${param.q}&resultsUri=${param.resultsUri}">Next</a>
	 &nbsp;|&nbsp;
</c:if>

<c:choose>
	<c:when test="${param.index != param.lastIndex}">
		<a href="ResultsNavigator?index=0&goto=last&lastIndex=${param.lastIndex}&q=${param.q}&resultsUri=${param.resultsUri}">Last</a>
	</c:when>
	<c:otherwise>
		Last
	</c:otherwise>
</c:choose>
&nbsp;|&nbsp;

<a href="ResultsNavigator?index=${param.index}&goto=results&lastIndex=${param.lastIndex}&q=${param.q}&resultsUri=${param.resultsUri}">Back to Search Results</a>

