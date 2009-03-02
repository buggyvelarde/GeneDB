<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty param.index and not empty param.lastIndex}">
	<c:forEach var="parameter" items="${paramValues}">
		<c:forEach var="value" items="${parameter.value}">
			<c:set var="requestParams" value="${requestParams}&${parameter.key}=${value}"/>
		</c:forEach> 	
	</c:forEach>

	&nbsp;Gene ${param.index+1} of ${param.lastIndex+1} Search Results.&nbsp;
	<c:choose>
		<c:when test="${param.index != '0'}">
			<a href="ResultsNavigator?goto=first${requestParams}">First</a>
		</c:when>
		<c:otherwise>
			First
		</c:otherwise>
	</c:choose>
	&nbsp;|&nbsp;

	<c:if test="${param.index != '0'}">
		<a href="ResultsNavigator?goto=previous${requestParams }">Previous</a>
	 	&nbsp;|&nbsp;
	</c:if>

	<c:if test="${param.index != param.lastIndex}">
		<a href="ResultsNavigator?goto=next${requestParams }">Next</a>
	 	&nbsp;|&nbsp;
	</c:if>

	<c:choose>
		<c:when test="${param.index != param.lastIndex}">
			<a href="ResultsNavigator?goto=last${requestParams }">Last</a>
		</c:when>
		<c:otherwise>
			Last
		</c:otherwise>
	</c:choose>
	&nbsp;|&nbsp;

	<a href="ResultsNavigator?goto=results${requestParams }">Back to Search Results</a>
	<br>
</c:if>

